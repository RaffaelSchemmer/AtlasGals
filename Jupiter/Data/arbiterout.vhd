-- *****************************************************************************
-- 	O módulo ArbiterOut implementa o algoritmo de roteamento sob a forma 
--  de três árbitros de saída que controlam a comunicação entre as filas 
--  A, B, C e as portas de saída do roteador. Os árbitros fazem chamandas de
--  funções do package Algoritmo_package.
-- *****************************************************************************
library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;
use work.Mercury_package.all;
use work.Algoritmo_package.all;

entity ArbiterOut is 
	port(
	clock,
	reset,
	data_av_A, 
	data_av_B, 
	data_av_C: in std_logic;
	ack_nack_L_A,
	ack_nack_L_B,
	ack_nack_L_C,
	ack_nack_N, 
	ack_nack_S, 
	ack_nack_E, 
	ack_nack_W: in ackNack;
	data_A,
	data_B,
	data_C: in std_logic_vector(HALF_PHIT_SIZE - 1 downto 0);			
	pos: in max_noc_size;
	queue_addr_L_A,
	queue_addr_L_B,
	queue_addr_L_C: out std_logic;
	queue_addr_N,
	queue_addr_S,
	queue_addr_E,
	queue_addr_W: out queue_addr	
	);
end ArbiterOut;

architecture AlgoritmoCG of ArbiterOut is

type states is (S_RESET, S_IDLE, S_CTRL_1, S_CTRL_2, S_CTRL_3, S_CTRL_4, S_TRYSEND, S_SEND, S_TRYSEND_LOCAL, S_SEND_LOCAL);
signal EA_A, PE_A, EA_B, PE_B, EA_C, PE_C: states;
signal caminhos_minimos_A, caminhos_minimos_B, caminhos_minimos_C: caminhos_minimos;
		-- queue_addr_out é o sinal conectado aos sinais de saida do arbitro (queue_addr_N, queue_addr_S, queue_addr_E
		-- e queue_addr_W).
signal queue_addr_out: queue_addr_door;
signal 	queue_addr_int_A, queue_addr_int_A_int,
		queue_addr_int_B, queue_addr_int_B_int,
		queue_addr_int_C, queue_addr_int_C_int: queue_addr_int;
signal ack_nack_door_int: ack_nack_door_int_t;
signal ack_nack_local_int: ack_nack_local_int_t;


	   -- Os sinais origem e destino armazenam as partes de origem e destino do sinal
	   -- data_A, data_B e data_C, que são usados pela maquina de estados no momento
	   -- em que o sinal data_av é ativado.
signal destino_x_A, destino_y_A, destino_x_B, destino_y_B, destino_x_C, destino_y_C, pos_atual_x, pos_atual_y,

	-- destino_x_A_sub_pos_atual_x armazena o valor de (destino_x_A - pos_atual_x)
	destino_x_A_sub_pos_atual_x,
	destino_y_A_sub_pos_atual_y,

	-- pos_atual_x_sub_destino_x_A armazena o valor de (pos_atual_x - destino_x_A)
	pos_atual_x_sub_destino_x_A,
	pos_atual_y_sub_destino_y_A,
   
	-- destino_x_B_sub_pos_atual_x armazena o valor de (destino_x_B - pos_atual_x)
	destino_x_B_sub_pos_atual_x,
	destino_y_B_sub_pos_atual_y,
	-- pos_atual_x_sub_destino_x_B armazena o valor de (pos_atual_x - destino_x_B)
	pos_atual_x_sub_destino_x_B,
	pos_atual_y_sub_destino_y_B,

	-- destino_x_B_sub_pos_atual_x armazena o valor de (destino_x_B - pos_atual_x)
	destino_x_C_sub_pos_atual_x,
	destino_y_C_sub_pos_atual_y,
	-- pos_atual_x_sub_destino_x_B armazena o valor de (pos_atual_x - destino_x_B)
	pos_atual_x_sub_destino_x_C,
	pos_atual_y_sub_destino_y_C: half_max_noc_size;
	   
signal ctrl_A, ctrl_B, ctrl_C, ctrl_A_int, ctrl_B_int, ctrl_C_int: door;   
---------------------------------------------------------------------------------
-- Sinais usados para setar os caminhos mínimos nas cordenadas X e Y da fila A --
---------------------------------------------------------------------------------
signal dxa_gt_pax 				: boolean; 	-- true when (destino_x_A > pos_atual_x)
signal dxa_lt_pax 				: boolean; 	-- true when (destino_x_A < pos_atual_x)
signal tnix_eq_0_a 				: boolean; 	-- true when (tam_noc_impar_em_x = '0')
signal dxaspax_eq_0tnx  		: boolean;	-- true when (destino_x_A_sub_pos_atual_x = ('0' & tam_noc_x(QUARTER_PHIT_SIZE - 1 downto 1)))
signal dxaspax_lt_tnxmdxaspax  	: boolean;	-- true when ((destino_x_A_sub_pos_atual_x) < (tam_noc_x - (destino_x_A_sub_pos_atual_x)))
signal paxsdxa_eq_0tnx  		: boolean;	-- true when ((pos_atual_x_sub_destino_x_A) = '0' & tam_noc_x(QUARTER_PHIT_SIZE - 1 downto 1))
signal paxsdxa_lt_tnxmpaxsdxa  	: boolean;	-- true when ((pos_atual_x_sub_destino_x_A) < (tam_noc_x - (pos_atual_x_sub_destino_x_A)))

signal dya_gt_pay 				: boolean; 	-- true when (destino_y_A > pos_atual_y)
signal dya_lt_pay 				: boolean; 	-- true when (destino_y_A < pos_atual_y)
signal tniy_eq_0_a 				: boolean; 	-- true when (tam_noc_impar_em_y = '0')
signal dyaspay_eq_tny  			: boolean;	-- true when (destino_y_A_sub_pos_atual_y = tam_noc_y(QUARTER_PHIT_SIZE - 1 downto 1))
signal dyaspay_lt_tnymdyaspay  	: boolean;	-- true when ((destino_y_A_sub_pos_atual_y) < (tam_noc_y - (destino_y_A_sub_pos_atual_y)))
signal paysdya_eq_tny  			: boolean;	-- true when ((pos_atual_y_sub_destino_y_A) = tam_noc_y(QUARTER_PHIT_SIZE - 1 downto 1))
signal paysdya_lt_tnympaysdya  	: boolean;	-- true when ((pos_atual_y_sub_destino_y_A) < (tam_noc_y - (pos_atual_y_sub_destino_y_A)))


---------------------------------------------------------------------------------
-- Sinais usados para setar os caminhos mínimos nas cordenadas X e Y da fila B --
---------------------------------------------------------------------------------	
signal dxb_gt_pax 				: boolean; 	-- true when (destino_x_B > pos_atual_x)
signal dxb_lt_pax 				: boolean; 	-- true when (destino_x_B < pos_atual_x)
signal tnix_eq_0_b  			: boolean; 	-- true when (tam_noc_impar_em_x = '0')
signal dxbspax_eq_tnx  			: boolean;	-- true when (destino_x_B_sub_pos_atual_x = tam_noc_x(QUARTER_PHIT_SIZE - 1 downto 1))
signal dxbspax_lt_tnxmdxbspax  	: boolean;	-- true when ((destino_x_B_sub_pos_atual_x) < (tam_noc_x - (destino_x_B_sub_pos_atual_x)))
signal paxsdxb_eq_tnx  			: boolean;	-- true when ((pos_atual_x_sub_destino_x_B) = tam_noc_x(QUARTER_PHIT_SIZE - 1 downto 1))
signal paxsdxb_lt_tnxmpaxsdxb  	: boolean;	-- true when ((pos_atual_x_sub_destino_x_B) < (tam_noc_x - (pos_atual_x_sub_destino_x_B)))

signal dyb_gt_pay 				: boolean; 	-- true when (destino_y_B > pos_atual_y)
signal dyb_lt_pay 				: boolean; 	-- true when (destino_y_B < pos_atual_y)
signal tniy_eq_0_b  			: boolean; 	-- true when (tam_noc_impar_em_y = '0')
signal dybspay_eq_tny 	 		: boolean;	-- true when (destino_y_B_sub_pos_atual_y = (tam_noc_y(QUARTER_PHIT_SIZE - 1 downto 1)))
signal dybspay_lt_tnymdybspay  	: boolean;	-- true when ((destino_y_B_sub_pos_atual_y) < (tam_noc_y - (destino_y_B_sub_pos_atual_y)))
signal paysdyb_eq_tny  			: boolean;	-- true when ((pos_atual_y_sub_destino_y_B) = tam_noc_y(QUARTER_PHIT_SIZE - 1 downto 1))
signal paysdyb_lt_tnympaysdyb  	: boolean;	-- true when ((pos_atual_y_sub_destino_y_B) < (tam_noc_y - (pos_atual_y_sub_destino_y_B)))


---------------------------------------------------------------------------------
-- Sinais usados para setar os caminhos mínimos nas cordenadas X e Y da fila C --
---------------------------------------------------------------------------------	
signal dxc_gt_pax 				: boolean; 	-- true when (destino_x_C > pos_atual_x)
signal dxc_lt_pax 				: boolean; 	-- true when (destino_x_C < pos_atual_x)
signal tnix_eq_0_c  			: boolean; 	-- true when (tam_noc_impar_em_x = '0')
signal dxcspax_eq_tnx  			: boolean;	-- true when (destino_x_C_sub_pos_atual_x = tam_noc_x(QUARTER_PHIT_SIZE - 1 downto 1))
signal dxcspax_lt_tnxmdxcspax  	: boolean;	-- true when ((destino_x_C_sub_pos_atual_x) < (tam_noc_x - (destino_x_C_sub_pos_atual_x)))
signal paxsdxc_eq_tnx  			: boolean;	-- true when ((pos_atual_x_sub_destino_x_C) = tam_noc_x(QUARTER_PHIT_SIZE - 1 downto 1))
signal paxsdxc_lt_tnxmpaxsdxc  	: boolean;	-- true when ((pos_atual_x_sub_destino_x_C) < (tam_noc_x - (pos_atual_x_sub_destino_x_C)))

signal dyc_gt_pay 				: boolean; 	-- true when (destino_y_C > pos_atual_y)
signal dyc_lt_pay 				: boolean; 	-- true when (destino_y_C < pos_atual_y)
signal tniy_eq_0_c  			: boolean; 	-- true when (tam_noc_impar_em_y = '0')
signal dycspay_eq_tny 	 		: boolean;	-- true when (destino_y_C_sub_pos_atual_y = (tam_noc_y(QUARTER_PHIT_SIZE - 1 downto 1)))
signal dycspay_lt_tnymdycspay  	: boolean;	-- true when ((destino_y_C_sub_pos_atual_y) < (tam_noc_y - (destino_y_C_sub_pos_atual_y)))
signal paysdyc_eq_tny  			: boolean;	-- true when ((pos_atual_y_sub_destino_y_C) = tam_noc_y(QUARTER_PHIT_SIZE - 1 downto 1))
signal paysdyc_lt_tnympaysdyc  	: boolean;	-- true when ((pos_atual_y_sub_destino_y_C) < (tam_noc_y - (pos_atual_y_sub_destino_y_C)))


alias ocup: queue_addr_door is queue_addr_out;
begin
	-- Armazena a posicao desta chave em relação a rede
	pos_atual_x <= pos((HALF_PHIT_SIZE - 1) downto QUARTER_PHIT_SIZE);
	pos_atual_y <= pos((QUARTER_PHIT_SIZE - 1) downto 0);
		
	ack_nack_door_int(conv_integer(NORTH)) <= ack_nack_N;
	ack_nack_door_int(conv_integer(SOUTH)) <= ack_nack_S;
	ack_nack_door_int(conv_integer(EAST)) <= ack_nack_E;
	ack_nack_door_int(conv_integer(WEST)) <= ack_nack_W;
	ack_nack_local_int(LOCAL_A) <= ack_nack_L_A;
	ack_nack_local_int(LOCAL_B) <= ack_nack_L_B;
	ack_nack_local_int(LOCAL_C) <= ack_nack_L_C;		
	
	queue_addr_N <= queue_addr_out(conv_integer(NORTH));
	queue_addr_S <= queue_addr_out(conv_integer(SOUTH));	
	queue_addr_E <= queue_addr_out(conv_integer(EAST));
	queue_addr_W <= queue_addr_out(conv_integer(WEST));

	-- Os módulos SwitchOut definem qual fila vai ganhar a prioridade de acesso a uma determinada 
	-- porta de saída, se duas ou mais filas requisitarem a mesma porta simultaneamente
	SWITCHOUT_N: entity work.SwitchOut
		port map(
		reset=>reset,
		clock => clock,
		queue_addr_in_A=>queue_addr_int_A_int(conv_integer(NORTH)),
		queue_addr_in_B=>queue_addr_int_B_int(conv_integer(NORTH)),
		queue_addr_in_C=>queue_addr_int_C_int(conv_integer(NORTH)),
		queue_addr_out=>queue_addr_out(conv_integer(NORTH))
		);
		
	SWITCHOUT_S: entity work.SwitchOut
		port map(
		reset=>reset,
		clock => clock,
		queue_addr_in_A=>queue_addr_int_A_int(conv_integer(SOUTH)),
		queue_addr_in_B=>queue_addr_int_B_int(conv_integer(SOUTH)),
		queue_addr_in_C=>queue_addr_int_C_int(conv_integer(SOUTH)),
		queue_addr_out=>queue_addr_out(conv_integer(SOUTH))
		);							
		
	SWITCHOUT_E: entity work.SwitchOut
		port map(
		reset=>reset,
		clock => clock,
		queue_addr_in_A=>queue_addr_int_A_int(conv_integer(EAST)),
		queue_addr_in_B=>queue_addr_int_B_int(conv_integer(EAST)),
		queue_addr_in_C=>queue_addr_int_C_int(conv_integer(EAST)),
		queue_addr_out=>queue_addr_out(conv_integer(EAST))
		);		
		
	SWITCHOUT_W: entity work.SwitchOut
		port map(
		reset=>reset,
		clock => clock,
		queue_addr_in_A=>queue_addr_int_A_int(conv_integer(WEST)),
		queue_addr_in_B=>queue_addr_int_B_int(conv_integer(WEST)),
		queue_addr_in_C=>queue_addr_int_C_int(conv_integer(WEST)),
		queue_addr_out=>queue_addr_out(conv_integer(WEST))
		);		
	

	-----------------------------------------------------------
	--  Processo que implementa todos registradores de estado
	--  dos árbitros de saída das filas A, B e C.
	-----------------------------------------------------------
	process(clock, reset)
	begin
		if reset = '1' then
			EA_A <= S_RESET;
			EA_B <= S_RESET;
			EA_C <= S_RESET;
		elsif clock'event and clock = '1' then
			EA_A <= PE_A;  
			EA_B <= PE_B;
			EA_C <= PE_C;
		end if;
	end process;		
		
	-- ********************************************************
	--  ÁRBITRO DE SAÍDA DA FILA A
	--		
	-- Armazena o phit de origem e destino em sinais.
	-- O phit é dividido em quatro partes na ordem: 
	--	origem_x, origem_y, destino_x, destino_y.
	-- A constante PHIT_SIZE contem o número de bits do phit.
	-- ********************************************************		

	destino_x_A <= data_A(HALF_PHIT_SIZE - 1 downto QUARTER_PHIT_SIZE);
	destino_y_A <= data_A(QUARTER_PHIT_SIZE - 1 downto 0);
	
	destino_x_A_sub_pos_atual_x <= destino_x_A - pos_atual_x;
	pos_atual_x_sub_destino_x_A <= pos_atual_x - destino_x_A;
	destino_y_A_sub_pos_atual_y <= destino_y_A - pos_atual_y;
	pos_atual_y_sub_destino_y_A <= pos_atual_y - destino_y_A;
	
	-----------------------------------------------------------
	-- Comandos auxiliares para a máquina de estados do árbitro 
	-- de saída da fila A. (módulo combinacional)
	--
	-- Estes comandos determinam as direções possíveis para
	-- seguir um dos caminhos mínimos para o dado na fila A
	-- alcançar seu destino. O resultado líquido de executar
	-- este trecho consiste em definir o valor do sinal
	-- caminhos_minimos_A(door), que será a entrada de 
	-- REG_CAMINHOS_MINIMOS.
	-----------------------------------------------------------
	
	------------------------------------------------------
	-- Preparação no eixo X, comparações compartilhadas	--
	------------------------------------------------------
	dxa_gt_pax 	<= true when (destino_x_A > pos_atual_x) else false; 					
	dxa_lt_pax 	<= true when (destino_x_A < pos_atual_x) else false;					
	tnix_eq_0_a	<= true when (tam_noc_impar_em_x = '0') else false;						
	dxaspax_eq_0tnx <= true when (destino_x_A_sub_pos_atual_x = 	   		
						('0' & tam_noc_x(QUARTER_PHIT_SIZE - 1 downto 1))) else false; 	
		
	dxaspax_lt_tnxmdxaspax <= true when ((destino_x_A_sub_pos_atual_x) <
						(tam_noc_x - (destino_x_A_sub_pos_atual_x))) else false;		
		
	paxsdxa_eq_0tnx <= true when ((pos_atual_x_sub_destino_x_A) =
						'0' & tam_noc_x(QUARTER_PHIT_SIZE - 1 downto 1)) else false; 	
		
	paxsdxa_lt_tnxmpaxsdxa <= true when ((pos_atual_x_sub_destino_x_A) <
						(tam_noc_x - (pos_atual_x_sub_destino_x_A))) else false;  		
	
	--------------------------------------------
	-- Seta caminhos mínimos na coordenada X. --
	--------------------------------------------
	caminhos_minimos_A(conv_integer(EAST)) <= '1' when
		(dxa_gt_pax and tnix_eq_0_a and dxaspax_eq_0tnx)
		or
		(dxa_gt_pax and not (tnix_eq_0_a and dxaspax_eq_0tnx) and dxaspax_lt_tnxmdxaspax)
		or
		(dxa_lt_pax and tnix_eq_0_a and paxsdxa_eq_0tnx)
		or 
		(dxa_lt_pax and not (tnix_eq_0_a and paxsdxa_eq_0tnx) and not paxsdxa_lt_tnxmpaxsdxa)
		else '0';
	caminhos_minimos_A(conv_integer(WEST)) <= '1' when
		(dxa_gt_pax and tnix_eq_0_a and dxaspax_eq_0tnx)
		or
		(dxa_gt_pax and not (tnix_eq_0_a and dxaspax_eq_0tnx) and not dxaspax_lt_tnxmdxaspax)
		or
		(dxa_lt_pax and tnix_eq_0_a and paxsdxa_eq_0tnx)
		or 
		(dxa_lt_pax and not (tnix_eq_0_a and paxsdxa_eq_0tnx) and paxsdxa_lt_tnxmpaxsdxa)
		else '0';										  
			
    ------------------------------------------------------
	-- Preparação no eixo Y, comparações compartilhadas	--
	------------------------------------------------------
	dya_gt_pay 	<= true when (destino_y_A > pos_atual_y) else false;	  			
	dya_lt_pay 	<= true when (destino_y_A < pos_atual_y) else false;				
	tniy_eq_0_a	<= true when (tam_noc_impar_em_y = '0') else false;					
		
	dyaspay_eq_tny <= true when (destino_y_A_sub_pos_atual_y = 
					tam_noc_y(QUARTER_PHIT_SIZE - 1 downto 1)) else false;			
		
	dyaspay_lt_tnymdyaspay <= true when ((destino_y_A_sub_pos_atual_y) <
					(tam_noc_y - (destino_y_A_sub_pos_atual_y))) else false;		
						
	
	paysdya_eq_tny <= true when ((pos_atual_y_sub_destino_y_A) =
					tam_noc_y(QUARTER_PHIT_SIZE - 1 downto 1)) else false;   		
						
	paysdya_lt_tnympaysdya <= true when ((pos_atual_y_sub_destino_y_A) <
						(tam_noc_y - (pos_atual_y_sub_destino_y_A))) else false; 	
							
	--------------------------------------------
	-- Seta caminhos mínimos na coordenada Y. --
	--------------------------------------------
	caminhos_minimos_A(conv_integer(NORTH)) <= '1' when
		(dya_gt_pay and tniy_eq_0_a and dyaspay_eq_tny)
		or
		(dya_gt_pay and not (tniy_eq_0_a and dyaspay_eq_tny) and dyaspay_lt_tnymdyaspay)
		or
		(dya_lt_pay and tniy_eq_0_a and paysdya_eq_tny)
		or 
		(dya_lt_pay and not (tniy_eq_0_a and paysdya_eq_tny) and not paysdya_lt_tnympaysdya)
		else '0';
	caminhos_minimos_A(conv_integer(SOUTH)) <= '1' when
		(dya_gt_pay and tniy_eq_0_a and dyaspay_eq_tny)
		or
		(dya_gt_pay and not (tniy_eq_0_a and dyaspay_eq_tny) and not dyaspay_lt_tnymdyaspay)
		or
		(dya_lt_pay and tniy_eq_0_a and paysdya_eq_tny)
		or 
		(dya_lt_pay and not (tniy_eq_0_a and paysdya_eq_tny) and paysdya_lt_tnympaysdya)
		else '0';
			
	-- Seta caminhos mínimos para a porta LOCAL e PORTA_NONE.
	caminhos_minimos_A(conv_integer(LOCAL)) <= EXISTE_CAMINHO_A(caminhos_minimos_A, pos_atual_x, pos_atual_y);			
	caminhos_minimos_A(conv_integer(PORT_NONE)) <= '0';

	-----------------------------------------------------------
	-- Função próximo estado para a máquina de estados do  
	-- árbitro de saída da fila A. (módulo combinacional)
	-- Máquina de Moore PE depende do EA e das entradas
	-----------------------------------------------------------	 
	process(EA_A, data_av_A, ack_nack_local_int, caminhos_minimos_A, ocup, ack_nack_door_int, ctrl_A_int)
	begin
		case EA_A is  
			
			when S_RESET =>
				PE_A <= S_IDLE;
		
			when S_IDLE =>
				if data_av_A = '1' and caminhos_minimos_A(conv_integer(LOCAL)) = '1' then
					PE_A <= S_TRYSEND_LOCAL;												
				elsif data_av_A = '1' and caminhos_minimos_A(conv_integer((ctrl_A_int+1) and "011"))='1' and ocup(conv_integer((ctrl_A_int+1) and "011")) = FILA_NONE then
					PE_A <= S_CTRL_1;
				elsif data_av_A = '1' and caminhos_minimos_A(conv_integer((ctrl_A_int+2) and "011"))='1' and ocup(conv_integer((ctrl_A_int+2) and "011")) = FILA_NONE then
					PE_A <= S_CTRL_2;
				elsif data_av_A = '1' and caminhos_minimos_A(conv_integer((ctrl_A_int+3) and "011"))='1' and ocup(conv_integer((ctrl_A_int+3) and "011")) = FILA_NONE then
					PE_A <= S_CTRL_3;
				elsif data_av_A = '1' and caminhos_minimos_A(conv_integer(ctrl_A_int))='1' and ocup(conv_integer(ctrl_A_int)) = FILA_NONE then
					PE_A <= S_CTRL_4;
				else 				 
					PE_A <= S_IDLE;
				end if;	 
			 
			when S_CTRL_1 => 
				if ocup(conv_integer(ctrl_A_int)) = FILA_A then
					PE_A <= S_TRYSEND;
				else
					PE_A <= S_IDLE;
				end if;
			
			when S_CTRL_2 => 
				if ocup(conv_integer(ctrl_A_int)) = FILA_A then
					PE_A <= S_TRYSEND;
				else
					PE_A <= S_IDLE;
				end if;
			
			when S_CTRL_3 =>
				if ocup(conv_integer(ctrl_A_int)) = FILA_A then
					PE_A <= S_TRYSEND;
				else
					PE_A <= S_IDLE;
				end if;
			
			when S_CTRL_4 =>
				if ocup(conv_integer(ctrl_A_int)) = FILA_A then
					PE_A <= S_TRYSEND;
				else
					PE_A <= S_IDLE;
				end if;
	
			when S_TRYSEND =>
				if ocup(conv_integer(ctrl_A_int)) = FILA_A and ack_nack_door_int(conv_integer(ctrl_A_int)) = ACK_ME then
					PE_A <= S_SEND;
				elsif (ack_nack_door_int(conv_integer(ctrl_A_int)) = NACK_ME or ocup(conv_integer(ctrl_A_int)) /= FILA_A) and caminhos_minimos_A(conv_integer((ctrl_A_int+1) and "011"))='1' then
					PE_A <= S_CTRL_1;
				elsif (ack_nack_door_int(conv_integer(ctrl_A_int)) = NACK_ME or ocup(conv_integer(ctrl_A_int)) /= FILA_A) and caminhos_minimos_A(conv_integer((ctrl_A_int+2) and "011"))='1' then
					PE_A <= S_CTRL_2;
				elsif (ack_nack_door_int(conv_integer(ctrl_A_int)) = NACK_ME or ocup(conv_integer(ctrl_A_int)) /= FILA_A) and caminhos_minimos_A(conv_integer((ctrl_A_int+3) and "011"))='1' then
					PE_A <= S_CTRL_3;
				elsif (ack_nack_door_int(conv_integer(ctrl_A_int)) = NACK_ME or ocup(conv_integer(ctrl_A_int)) /= FILA_A) and caminhos_minimos_A(conv_integer(ctrl_A_int))='1' then
					PE_A <= S_CTRL_4; 
				else
					PE_A <= S_TRYSEND;
				end if;	
				
			when S_SEND =>	
				if ack_nack_door_int(conv_integer(ctrl_A_int)) = ACK_ME then
					PE_A <= S_SEND;
				else	
					PE_A <= S_RESET;
				end if;
	
			when S_TRYSEND_LOCAL =>
				if ack_nack_local_int(LOCAL_A)= ACK_ME	 then
					PE_A <= S_SEND_LOCAL;
				else
					PE_A <= S_TRYSEND_LOCAL;	
				end if;
			
			when S_SEND_LOCAL =>
				if ack_nack_local_int(LOCAL_A)= ACK_ME then
					PE_A <= S_SEND_LOCAL;  
				else
					PE_A <= S_RESET;
				end if;
		end case;
	end process;
	
	-----------------------------------------------------------
	-- Função de saída para a máquina de estados do  
	-- árbitro de saída da fila A. (módulo combinacional)
	-- Máquina de Moore - Saída depende do EA apenas
	-----------------------------------------------------------	
	process (clock)
	begin
		if clock'event and clock='1' then
			ctrl_A <= ctrl_A_int;
			queue_addr_int_A <= queue_addr_int_A_int;
		end if;
	end process;
		
	process(EA_A)
	begin
		case EA_A is  			
			when S_RESET =>
				ctrl_A_int <= WEST;							
				queue_addr_L_A <= '0'; 					
				queue_addr_int_A_int <= (others=>'0');		
		
			when S_IDLE =>
				ctrl_A_int <= ctrl_A;
				queue_addr_L_A <= '0'; 					
				queue_addr_int_A_int <= (others=>'0');	
			 
			when S_CTRL_1 => 
				ctrl_A_int <= ((ctrl_A+1) and "011");					
				queue_addr_L_A <= '0';			
				queue_addr_int_A_int(conv_integer(ctrl_A)) <= '0';
				queue_addr_int_A_int(conv_integer((ctrl_A+1) and "011")) <= '1';	
				queue_addr_int_A_int(conv_integer((ctrl_A+2) and "011")) <= queue_addr_int_A(conv_integer((ctrl_A+2) and "011"));	
				queue_addr_int_A_int(conv_integer((ctrl_A+3) and "011")) <= queue_addr_int_A(conv_integer((ctrl_A+3) and "011"));	
			
			when S_CTRL_2 => 
				ctrl_A_int <= ((ctrl_A+2) and "011");
				queue_addr_L_A <= '0'; 					
				queue_addr_int_A_int(conv_integer(ctrl_A)) <= '0';	   	
				queue_addr_int_A_int(conv_integer((ctrl_A+1) and "011")) <= queue_addr_int_A(conv_integer((ctrl_A+1) and "011"));	
				queue_addr_int_A_int(conv_integer((ctrl_A+2) and "011")) <= '1'; 
				queue_addr_int_A_int(conv_integer((ctrl_A+3) and "011")) <= queue_addr_int_A(conv_integer((ctrl_A+3) and "011"));	
			
			when S_CTRL_3 =>
				ctrl_A_int <= ((ctrl_A+3) and "011");					
				queue_addr_L_A <= '0'; 					
				queue_addr_int_A_int(conv_integer(ctrl_A)) <= '0';
				queue_addr_int_A_int(conv_integer((ctrl_A+1) and "011")) <= queue_addr_int_A(conv_integer((ctrl_A+1) and "011"));	
				queue_addr_int_A_int(conv_integer((ctrl_A+2) and "011")) <= queue_addr_int_A(conv_integer((ctrl_A+2) and "011"));	
				queue_addr_int_A_int(conv_integer((ctrl_A+3) and "011")) <= '1';  
			
			when S_CTRL_4 =>
				ctrl_A_int <= ctrl_A;
				queue_addr_L_A <= '0'; 					
				queue_addr_int_A_int(conv_integer(ctrl_A)) <= '1';
				queue_addr_int_A_int(conv_integer((ctrl_A+1) and "011")) <= queue_addr_int_A(conv_integer((ctrl_A+1) and "011"));	
				queue_addr_int_A_int(conv_integer((ctrl_A+2) and "011")) <= queue_addr_int_A(conv_integer((ctrl_A+2) and "011"));	
				queue_addr_int_A_int(conv_integer((ctrl_A+3) and "011")) <= queue_addr_int_A(conv_integer((ctrl_A+3) and "011"));	
	
			when S_TRYSEND => 
				ctrl_A_int <= ctrl_A;
				queue_addr_L_A <= '0';
				queue_addr_int_A_int <= queue_addr_int_A;
				
			when S_SEND =>	
				ctrl_A_int <= ctrl_A;
				queue_addr_L_A <= '0';	  
				queue_addr_int_A_int <= queue_addr_int_A;	
				
			when S_TRYSEND_LOCAL =>
				ctrl_A_int <= ctrl_A;
				queue_addr_L_A <= '1';	
				queue_addr_int_A_int <= queue_addr_int_A;
			
			when S_SEND_LOCAL =>
				ctrl_A_int <= ctrl_A;
				queue_addr_L_A <= '1';
				queue_addr_int_A_int <= queue_addr_int_A;
		end case;
	end process;
	
	
	-- ********************************************************
	--  ÁRBITRO DE SAÍDA DA FILA B
	--		
	-- Armazena o phit de origem e destino em sinais.
	-- O phit é dividido em quatro partes na ordem: 
	--	origem_x, origem_y, destino_x, destino_y.
	-- A constante PHIT_SIZE contem o número de bits do phit.
	-- ********************************************************

	destino_x_B <= data_B(HALF_PHIT_SIZE - 1 downto QUARTER_PHIT_SIZE);
	destino_y_B <= data_B(QUARTER_PHIT_SIZE - 1 downto 0);
	
	destino_x_B_sub_pos_atual_x <= destino_x_B - pos_atual_x;
	pos_atual_x_sub_destino_x_B <= pos_atual_x - destino_x_B;
	destino_y_B_sub_pos_atual_y <= destino_y_B - pos_atual_y;
	pos_atual_y_sub_destino_y_B <= pos_atual_y - destino_y_B;	
	
	-----------------------------------------------------------
	-- Processo auxiliar para a máquina de estados do árbitro 
	-- de saída da fila B. (módulo combinacional)
	--
	-- Determina as direções possíveis para seguir um dos
	-- caminhos mínimos para o dado na fila B alcançar seu 
	-- destino. O resultado líquido de executar este processo
	-- consiste em definir o valor do sinal
	-- caminhos_minimos_B(door), que será a entrada de 
	-- REG_CAMINHOS_MINIMOS.
	-----------------------------------------------------------
		
	------------------------------------------------------
	-- Preparação no eixo X, comparações compartilhadas	--
	------------------------------------------------------
	dxb_gt_pax 	<= true when (destino_x_B > pos_atual_x) else false; 					
	dxb_lt_pax 	<= true when (destino_x_B < pos_atual_x) else false;					
	tnix_eq_0_b	<= true when (tam_noc_impar_em_x = '0') else false;						
	dxbspax_eq_tnx <= true when (destino_x_B_sub_pos_atual_x = 	   		
						tam_noc_x(QUARTER_PHIT_SIZE - 1 downto 1)) else false; 			
		
	dxbspax_lt_tnxmdxbspax <= true when ((destino_x_B_sub_pos_atual_x) <
						(tam_noc_x - (destino_x_B_sub_pos_atual_x))) else false;		
		
	paxsdxb_eq_tnx <= true when ((pos_atual_x_sub_destino_x_B) =
						tam_noc_x(QUARTER_PHIT_SIZE - 1 downto 1)) else false; 			
		
	paxsdxb_lt_tnxmpaxsdxb <= true when ((pos_atual_x_sub_destino_x_B) <
						(tam_noc_x - (pos_atual_x_sub_destino_x_B))) else false;  		
	
	--------------------------------------------
	-- Seta caminhos mínimos na coordenada X. --
	--------------------------------------------
	caminhos_minimos_B(conv_integer(EAST)) <= '1' when
		(dxb_gt_pax and tnix_eq_0_b and dxbspax_eq_tnx)
		or
		(dxb_gt_pax and not (tnix_eq_0_b and dxbspax_eq_tnx) and dxbspax_lt_tnxmdxbspax)
		or
		(dxb_lt_pax and tnix_eq_0_b and paxsdxb_eq_tnx)
		or 
		(dxb_lt_pax and not (tnix_eq_0_b and paxsdxb_eq_tnx) and not paxsdxb_lt_tnxmpaxsdxb)
		else '0';
	caminhos_minimos_B(conv_integer(WEST)) <= '1' when
		(dxb_gt_pax and tnix_eq_0_b and dxbspax_eq_tnx)
		or
		(dxb_gt_pax and not (tnix_eq_0_b and dxbspax_eq_tnx) and not dxbspax_lt_tnxmdxbspax)
		or
		(dxb_lt_pax and tnix_eq_0_b and paxsdxb_eq_tnx)
		or 
		(dxb_lt_pax and not (tnix_eq_0_b and paxsdxb_eq_tnx) and paxsdxb_lt_tnxmpaxsdxb)
		else '0';										  
			
    ------------------------------------------------------
	-- Preparação no eixo Y, comparações compartilhadas	--
	------------------------------------------------------
	dyb_gt_pay 	<= true when (destino_y_B > pos_atual_y) else false;	  			
	dyb_lt_pay 	<= true when (destino_y_B < pos_atual_y) else false;				
	tniy_eq_0_b	<= true when (tam_noc_impar_em_y = '0') else false;					
		
	dybspay_eq_tny <= true when (destino_y_B_sub_pos_atual_y = 
					tam_noc_y(QUARTER_PHIT_SIZE - 1 downto 1)) else false;			
		
	dybspay_lt_tnymdybspay <= true when ((destino_y_B_sub_pos_atual_y) <
					(tam_noc_y - (destino_y_B_sub_pos_atual_y))) else false;		
						
	paysdyb_eq_tny <= true when ((pos_atual_y_sub_destino_y_B) =
					tam_noc_y(QUARTER_PHIT_SIZE - 1 downto 1)) else false;   		
						
	paysdyb_lt_tnympaysdyb <= true when ((pos_atual_y_sub_destino_y_B) <
						(tam_noc_y - (pos_atual_y_sub_destino_y_B))) else false; 	
							
	--------------------------------------------
	-- Seta caminhos mínimos na coordenada Y. --
	--------------------------------------------
	caminhos_minimos_B(conv_integer(NORTH)) <= '1' when
		(dyb_gt_pay and tniy_eq_0_b and dybspay_eq_tny)
		or
		(dyb_gt_pay and not (tniy_eq_0_b and dybspay_eq_tny) and dybspay_lt_tnymdybspay)
		or
		(dyb_lt_pay and tniy_eq_0_b and paysdyb_eq_tny)
		or 
		(dyb_lt_pay and not (tniy_eq_0_b and paysdyb_eq_tny) and not paysdyb_lt_tnympaysdyb)
		else '0';
	caminhos_minimos_B(conv_integer(SOUTH)) <= '1' when
		(dyb_gt_pay and tniy_eq_0_b and dybspay_eq_tny)
		or
		(dyb_gt_pay and not (tniy_eq_0_b and dybspay_eq_tny) and not dybspay_lt_tnymdybspay)
		or
		(dyb_lt_pay and tniy_eq_0_b and paysdyb_eq_tny)
		or 
		(dyb_lt_pay and not (tniy_eq_0_b and paysdyb_eq_tny) and paysdyb_lt_tnympaysdyb)
		else '0';
			
	-- Seta caminhos mínimos para a porta LOCAL e PORTA_NONE. 
	caminhos_minimos_B(conv_integer(LOCAL)) <= EXISTE_CAMINHO_B(caminhos_minimos_B, pos_atual_x, pos_atual_y);			
	caminhos_minimos_B(conv_integer(PORT_NONE)) <= '0';
	
	-- versao abaixo original da de cima
	--caminhos_minimos_int(conv_integer(LOCAL)) := EXISTE_CAMINHO_B(caminhos_minimos_int, pos_atual_x, pos_atual_y, tam_noc_x, tam_noc_y);
	--caminhos_minimos_B <= caminhos_minimos_int;	 

	-----------------------------------------------------------
	-- Função próximo estado para a máquina de estados do  
	-- árbitro de saída da fila B. (módulo combinacional)
	-- Máquina de Moore PE depende do EB e das entradas
	-----------------------------------------------------------	 
	process(EA_B, data_av_B, ack_nack_local_int, caminhos_minimos_B, ocup, ack_nack_door_int, ctrl_B_int)
	begin
		case EA_B is  
			
			when S_RESET =>
				PE_B <= S_IDLE;
		
			when S_IDLE =>
				if data_av_B = '1' and caminhos_minimos_B(conv_integer(LOCAL)) = '1' then
					PE_B <= S_TRYSEND_LOCAL;													   
				elsif data_av_B = '1' and caminhos_minimos_B(conv_integer((ctrl_B_int+1) and "011"))='1' and ocup(conv_integer((ctrl_B_int+1) and "011")) = FILA_NONE then
					PE_B <= S_CTRL_1;
				elsif data_av_B = '1' and caminhos_minimos_B(conv_integer((ctrl_B_int+2) and "011"))='1' and ocup(conv_integer((ctrl_B_int+2) and "011")) = FILA_NONE then
					PE_B <= S_CTRL_2;
				elsif data_av_B = '1' and caminhos_minimos_B(conv_integer((ctrl_B_int+3) and "011"))='1' and ocup(conv_integer((ctrl_B_int+3) and "011")) = FILA_NONE then
					PE_B <= S_CTRL_3;
				elsif data_av_B = '1' and caminhos_minimos_B(conv_integer(ctrl_B_int))='1' and ocup(conv_integer(ctrl_B_int)) = FILA_NONE then
					PE_B <= S_CTRL_4;
				else 				 
					PE_B <= S_IDLE;
				end if;	 
			 
			when S_CTRL_1 => 
				if ocup(conv_integer(ctrl_B_int)) = FILA_B then
					PE_B <= S_TRYSEND;
				else
					PE_B <= S_IDLE;
				end if;
			
			when S_CTRL_2 => 
				if ocup(conv_integer(ctrl_B_int)) = FILA_B then
					PE_B <= S_TRYSEND;
				else
					PE_B <= S_IDLE;
				end if;
			
			when S_CTRL_3 =>
				if ocup(conv_integer(ctrl_B_int)) = FILA_B then
					PE_B <= S_TRYSEND;
				else
					PE_B <= S_IDLE;
				end if;
			
			when S_CTRL_4 =>
				if ocup(conv_integer(ctrl_B_int)) = FILA_B then
					PE_B <= S_TRYSEND;
				else
					PE_B <= S_IDLE;
				end if;
	
			when S_TRYSEND =>
				if ocup(conv_integer(ctrl_B_int)) = FILA_B and ack_nack_door_int(conv_integer(ctrl_B_int)) = ACK_ME then
					PE_B <= S_SEND;
				elsif (ack_nack_door_int(conv_integer(ctrl_B_int)) = NACK_ME or ocup(conv_integer(ctrl_B_int)) /= FILA_B) and caminhos_minimos_B(conv_integer((ctrl_B_int+1) and "011"))='1' then
					PE_B <= S_CTRL_1;
				elsif (ack_nack_door_int(conv_integer(ctrl_B_int)) = NACK_ME or ocup(conv_integer(ctrl_B_int)) /= FILA_B) and caminhos_minimos_B(conv_integer((ctrl_B_int+2) and "011"))='1' then
					PE_B <= S_CTRL_2;
				elsif (ack_nack_door_int(conv_integer(ctrl_B_int)) = NACK_ME or ocup(conv_integer(ctrl_B_int)) /= FILA_B) and caminhos_minimos_B(conv_integer((ctrl_B_int+3) and "011"))='1' then
					PE_B <= S_CTRL_3;
				elsif (ack_nack_door_int(conv_integer(ctrl_B_int)) = NACK_ME or ocup(conv_integer(ctrl_B_int)) /= FILA_B) and caminhos_minimos_B(conv_integer(ctrl_B_int))='1' then
					PE_B <= S_CTRL_4; 
				else
					PE_B <= S_TRYSEND;
				end if;	
				
			when S_SEND =>	
				if ack_nack_door_int(conv_integer(ctrl_B_int)) = ACK_ME then
					PE_B <= S_SEND;
				else
					PE_B <= S_RESET;
				end if;
	
			when S_TRYSEND_LOCAL =>
				if ack_nack_local_int(LOCAL_B)= ACK_ME	 then
					PE_B <= S_SEND_LOCAL;
				else
					PE_B <= S_TRYSEND_LOCAL;	
				end if;
			
			when S_SEND_LOCAL =>
				if ack_nack_local_int(LOCAL_B) = ACK_ME then
					PE_B <= S_SEND_LOCAL; 
				else
					PE_B <= S_RESET;
				end if;
		end case;
	end process;
	
	-----------------------------------------------------------
	-- Função de saída para a máquina de estados do  
	-- árbitro de saída da fila B. (módulo combinacional)
	-- Máquina de Moore - Saída depende do EB apenas
	-----------------------------------------------------------	 
	process (clock)
	begin
		if clock'event and clock='1' then
			ctrl_B <= ctrl_B_int;
			queue_addr_int_B <= queue_addr_int_B_int;
		end if;
	end process;

	process(EA_B)
	begin
		case EA_B is  
			
			when S_RESET =>
				ctrl_B_int <= WEST;							
				queue_addr_L_B <= '0'; 					
				queue_addr_int_B_int <= (others=>'0');		
		
			when S_IDLE =>
				ctrl_B_int <= ctrl_B;
				queue_addr_L_B <= '0'; 					
				queue_addr_int_B_int <= (others=>'0');	
			 
			when S_CTRL_1 => 
				ctrl_B_int <= ((ctrl_B+1) and "011");					
				queue_addr_L_B <= '0'; 					
				queue_addr_int_B_int(conv_integer(ctrl_B)) <= '0';
				queue_addr_int_B_int(conv_integer((ctrl_B+1) and "011")) <= '1';	
				queue_addr_int_B_int(conv_integer((ctrl_B+2) and "011")) <= queue_addr_int_B(conv_integer((ctrl_B+2) and "011"));	
				queue_addr_int_B_int(conv_integer((ctrl_B+3) and "011")) <= queue_addr_int_B(conv_integer((ctrl_B+3) and "011"));	
				
			
			when S_CTRL_2 => 
				ctrl_B_int <= ((ctrl_B+2) and "011");					
				queue_addr_L_B <= '0'; 					
				queue_addr_int_B_int(conv_integer(ctrl_B)) <= '0';	   	
				queue_addr_int_B_int(conv_integer((ctrl_B+1) and "011")) <= queue_addr_int_B(conv_integer((ctrl_B+1) and "011"));	
				queue_addr_int_B_int(conv_integer((ctrl_B+2) and "011")) <= '1'; 
				queue_addr_int_B_int(conv_integer((ctrl_B+3) and "011")) <= queue_addr_int_B(conv_integer((ctrl_B+3) and "011"));	
			
			when S_CTRL_3 =>
				ctrl_B_int <= ((ctrl_B+3) and "011");					
				queue_addr_L_B <= '0'; 					
				queue_addr_int_B_int(conv_integer(ctrl_B)) <= '0';
				queue_addr_int_B_int(conv_integer((ctrl_B+1) and "011")) <= queue_addr_int_B(conv_integer((ctrl_B+1) and "011"));	
				queue_addr_int_B_int(conv_integer((ctrl_B+2) and "011")) <= queue_addr_int_B(conv_integer((ctrl_B+2) and "011"));	
				queue_addr_int_B_int(conv_integer((ctrl_B+3) and "011")) <= '1';  
			
			when S_CTRL_4 =>  
				ctrl_B_int <= ctrl_B;
				queue_addr_L_B <= '0'; 					
				queue_addr_int_B_int(conv_integer(ctrl_B)) <= '1';
				queue_addr_int_B_int(conv_integer((ctrl_B+1) and "011")) <= queue_addr_int_B(conv_integer((ctrl_B+1) and "011"));	
				queue_addr_int_B_int(conv_integer((ctrl_B+2) and "011")) <= queue_addr_int_B(conv_integer((ctrl_B+2) and "011"));	
				queue_addr_int_B_int(conv_integer((ctrl_B+3) and "011")) <= queue_addr_int_B(conv_integer((ctrl_B+3) and "011"));	
	
			when S_TRYSEND =>	 
				ctrl_B_int <= ctrl_B;
				queue_addr_L_B <= '0'; 				 
				queue_addr_int_B_int <= queue_addr_int_B;
				
			when S_SEND =>	
				ctrl_B_int <= ctrl_B;
				queue_addr_L_B <= '0'; 				 
				queue_addr_int_B_int <= queue_addr_int_B;
	
			when S_TRYSEND_LOCAL =>
				ctrl_B_int <= ctrl_B;
				queue_addr_L_B <= '1';					 
				queue_addr_int_B_int <= queue_addr_int_B;
			
			when S_SEND_LOCAL =>
				ctrl_B_int <= ctrl_B;
				queue_addr_L_B <= '1';	
				queue_addr_int_B_int <= queue_addr_int_B;
		end case;
	end process;	
	
	-- ********************************************************
	--  ÁRBITRO DE SAÍDA DA FILA C
	--		
	-- Armazena o phit de origem e destino em sinais.
	-- O phit é dividido em quatro partes na ordem: 
	--	origem_x, origem_y, destino_x, destino_y.
	-- A constante PHIT_SIZE contem o número de bits do phit.
	-- ********************************************************

	destino_x_C <= data_C(HALF_PHIT_SIZE - 1 downto QUARTER_PHIT_SIZE);
	destino_y_C <= data_C(QUARTER_PHIT_SIZE - 1 downto 0);
	
	destino_x_C_sub_pos_atual_x <= destino_x_C - pos_atual_x;
	pos_atual_x_sub_destino_x_C <= pos_atual_x - destino_x_C;
	destino_y_C_sub_pos_atual_y <= destino_y_C - pos_atual_y;
	pos_atual_y_sub_destino_y_C <= pos_atual_y - destino_y_C;

	-----------------------------------------------------------
	-- Processo auxiliar para a máquina de estados do árbitro 
	-- de saída da fila C. (módulo combinacional)
	--
	-- Determina as direções possíveis para seguir um dos
	-- caminhos mínimos para o dado na fila C alcançar seu 
	-- destino. O resultado líquido de executar este processo
	-- consiste em definir o valor do sinal
	-- caminhos_minimos_C(door), que será a entrada de 
	-- REG_CAMINHOS_MINIMOS.
	-----------------------------------------------------------
	
	
	------------------------------------------------------
	-- Preparação no eixo X, comparações compartilhadas	--
	------------------------------------------------------
	dxc_gt_pax 	<= true when (destino_x_C > pos_atual_x) else false; 					
	dxc_lt_pax 	<= true when (destino_x_C < pos_atual_x) else false;					
	tnix_eq_0_c	<= true when (tam_noc_impar_em_x = '0') else false;						
	dxcspax_eq_tnx <= true when (destino_x_C_sub_pos_atual_x = 	   		
						tam_noc_x(QUARTER_PHIT_SIZE - 1 downto 1)) else false; 			
		
	dxcspax_lt_tnxmdxcspax <= true when ((destino_x_C_sub_pos_atual_x) <
						(tam_noc_x - (destino_x_C_sub_pos_atual_x))) else false;		
		
	paxsdxc_eq_tnx <= true when ((pos_atual_x_sub_destino_x_C) =
						tam_noc_x(QUARTER_PHIT_SIZE - 1 downto 1)) else false; 			
		
	paxsdxc_lt_tnxmpaxsdxc <= true when ((pos_atual_x_sub_destino_x_C) <
						(tam_noc_x - (pos_atual_x_sub_destino_x_C))) else false;  		
	
	--------------------------------------------
	-- Seta caminhos mínimos na coordenada X. --
	--------------------------------------------
	caminhos_minimos_C(conv_integer(EAST)) <= '1' when
		(dxc_gt_pax and tnix_eq_0_c and dxcspax_eq_tnx)
		or
		(dxc_gt_pax and not (tnix_eq_0_c and dxcspax_eq_tnx) and dxcspax_lt_tnxmdxcspax)
		or
		(dxc_lt_pax and tnix_eq_0_c and paxsdxc_eq_tnx)
		or 
		(dxc_lt_pax and not (tnix_eq_0_c and paxsdxc_eq_tnx) and not paxsdxc_lt_tnxmpaxsdxc)
		else '0';
	caminhos_minimos_C(conv_integer(WEST)) <= '1' when
		(dxc_gt_pax and tnix_eq_0_c and dxcspax_eq_tnx)
		or
		(dxc_gt_pax and not (tnix_eq_0_c and dxcspax_eq_tnx) and not dxcspax_lt_tnxmdxcspax)
		or
		(dxc_lt_pax and tnix_eq_0_c and paxsdxc_eq_tnx)
		or 
		(dxc_lt_pax and not (tnix_eq_0_c and paxsdxc_eq_tnx) and paxsdxc_lt_tnxmpaxsdxc)
		else '0';										  
			
    ------------------------------------------------------
	-- Preparação no eixo Y, comparações compartilhadas	--
	------------------------------------------------------
	dyc_gt_pay 	<= true when (destino_y_C > pos_atual_y) else false;	  			
	dyc_lt_pay 	<= true when (destino_y_C < pos_atual_y) else false;				
	tniy_eq_0_c	<= true when (tam_noc_impar_em_y = '0') else false;					
		
	dycspay_eq_tny <= true when (destino_y_C_sub_pos_atual_y = 
					tam_noc_y(QUARTER_PHIT_SIZE - 1 downto 1)) else false;			
		
	dycspay_lt_tnymdycspay <= true when ((destino_y_C_sub_pos_atual_y) <
					(tam_noc_y - (destino_y_C_sub_pos_atual_y))) else false;		
						
	paysdyc_eq_tny <= true when ((pos_atual_y_sub_destino_y_C) =
					tam_noc_y(QUARTER_PHIT_SIZE - 1 downto 1)) else false;   		
						
	paysdyc_lt_tnympaysdyc <= true when ((pos_atual_y_sub_destino_y_C) <
						(tam_noc_y - (pos_atual_y_sub_destino_y_C))) else false; 	
							
	--------------------------------------------
	-- Seta caminhos mínimos na coordenada Y. --
	--------------------------------------------
	caminhos_minimos_C(conv_integer(NORTH)) <= '1' when
		(dyc_gt_pay and tniy_eq_0_c and dycspay_eq_tny)
		or
		(dyc_gt_pay and not (tniy_eq_0_c and dycspay_eq_tny) and dycspay_lt_tnymdycspay)
		or
		(dyc_lt_pay and tniy_eq_0_c and paysdyc_eq_tny)
		or 
		(dyc_lt_pay and not (tniy_eq_0_c and paysdyc_eq_tny) and not paysdyc_lt_tnympaysdyc)
		else '0';
	caminhos_minimos_C(conv_integer(SOUTH)) <= '1' when
		(dyc_gt_pay and tniy_eq_0_c and dycspay_eq_tny)
		or
		(dyc_gt_pay and not (tniy_eq_0_c and dycspay_eq_tny) and not dycspay_lt_tnymdycspay)
		or
		(dyc_lt_pay and tniy_eq_0_c and paysdyc_eq_tny)
		or 
		(dyc_lt_pay and not (tniy_eq_0_c and paysdyc_eq_tny) and paysdyc_lt_tnympaysdyc)
		else '0';
			
	-- Seta caminhos mínimos para a porta LOCAL e PORTA_NONE. 
	caminhos_minimos_C(conv_integer(LOCAL)) <= CHEGOU_DESTINO(pos_atual_x, pos_atual_y, destino_x_c, destino_y_c);			
	caminhos_minimos_C(conv_integer(PORT_NONE)) <= '0';
	

	-----------------------------------------------------------
	-- Função próximo estado para a máquina de estados do  
	-- árbitro de saída da fila C. (módulo combinacional)
	-- Máquina de Moore PE depende do EC e das entradas
	-----------------------------------------------------------	 
	process(EA_C, data_av_C, ack_nack_local_int, caminhos_minimos_C, ocup, ack_nack_door_int, ctrl_C_int)
	begin
		case EA_C is  
			
			when S_RESET =>
				PE_C <= S_IDLE;
		
			when S_IDLE =>
				if data_av_C = '1' and caminhos_minimos_C(conv_integer(LOCAL)) = '1' then
						PE_C <= S_TRYSEND_LOCAL;													   	
				elsif data_av_C = '1' and caminhos_minimos_C(conv_integer((ctrl_C_int+1) and "011"))='1' and ocup(conv_integer((ctrl_C_int+1) and "011")) = FILA_NONE  then
						PE_C <= S_CTRL_1;
				elsif data_av_C = '1' and caminhos_minimos_C(conv_integer((ctrl_C_int+2) and "011"))='1' and ocup(conv_integer((ctrl_C_int+2) and "011")) = FILA_NONE then
						PE_C <= S_CTRL_2;
				elsif data_av_C = '1' and caminhos_minimos_C(conv_integer((ctrl_C_int+3) and "011"))='1' and ocup(conv_integer((ctrl_C_int+3) and "011")) = FILA_NONE then
						PE_C <= S_CTRL_3;
				elsif data_av_C = '1' and caminhos_minimos_C(conv_integer(ctrl_C_int))='1' and ocup(conv_integer(ctrl_C_int)) = FILA_NONE then
						PE_C <= S_CTRL_4;
				else 				 
					PE_C <= S_IDLE;
				end if;	 
			 
			when S_CTRL_1 => 
				if ocup(conv_integer(ctrl_C_int)) = FILA_C then
					PE_C <= S_TRYSEND;
				else
					PE_C <= S_IDLE;
				end if;
			
			when S_CTRL_2 => 
				if ocup(conv_integer(ctrl_C_int)) = FILA_C then
					PE_C <= S_TRYSEND;
				else
					PE_C <= S_IDLE;
				end if;
			
			when S_CTRL_3 =>
				if ocup(conv_integer(ctrl_C_int)) = FILA_C then
					PE_C <= S_TRYSEND;
				else
					PE_C <= S_IDLE;
				end if;
			
			when S_CTRL_4 =>
				if ocup(conv_integer(ctrl_C_int)) = FILA_C then
					PE_C <= S_TRYSEND;
				else
					PE_C <= S_IDLE;
				end if;
	
			when S_TRYSEND =>
				if ocup(conv_integer(ctrl_C_int)) = FILA_C and ack_nack_door_int(conv_integer(ctrl_C_int)) = ACK_ME then
					PE_C <= S_SEND;
				elsif (ack_nack_door_int(conv_integer(ctrl_C_int)) = NACK_ME or ocup(conv_integer(ctrl_C_int)) /= FILA_C) and caminhos_minimos_C(conv_integer((ctrl_C_int+1) and "011"))='1' then
					PE_C <= S_CTRL_1;
				elsif (ack_nack_door_int(conv_integer(ctrl_C_int)) = NACK_ME or ocup(conv_integer(ctrl_C_int)) /= FILA_C) and caminhos_minimos_C(conv_integer((ctrl_C_int+2) and "011"))='1' then
					PE_C <= S_CTRL_2;
				elsif (ack_nack_door_int(conv_integer(ctrl_C_int)) = NACK_ME or ocup(conv_integer(ctrl_C_int)) /= FILA_C) and caminhos_minimos_C(conv_integer((ctrl_C_int+3) and "011"))='1' then
					PE_C <= S_CTRL_3;
				elsif (ack_nack_door_int(conv_integer(ctrl_C_int)) = NACK_ME or ocup(conv_integer(ctrl_C_int)) /= FILA_C) and caminhos_minimos_C(conv_integer(ctrl_C_int))='1' then
					PE_C <= S_CTRL_4;	 
				else
					PE_C <= S_TRYSEND;
				end if;	
				
			when S_SEND =>	
				if ack_nack_door_int(conv_integer(ctrl_C_int)) = ACK_ME then
					PE_C <= S_SEND;
				else
					PE_C <= S_RESET;
				end if;
	
			when S_TRYSEND_LOCAL =>
				if ack_nack_local_int(LOCAL_C)= ACK_ME	 then
					PE_C <= S_SEND_LOCAL;
				else
					PE_C <= S_TRYSEND_LOCAL;	
				end if;
			
			when S_SEND_LOCAL =>
				if ack_nack_local_int(LOCAL_C)= ACK_ME then
					PE_C <= S_SEND_LOCAL; 
				else
					PE_C <= S_RESET;
				end if;
		end case;
	end process;
	
	-----------------------------------------------------------
	-- Função de saída para a máquina de estados do  
	-- árbitro de saída da fila B. (módulo combinacional)
	-- Máquina de Moore - Saída depende do EB apenas
	-----------------------------------------------------------	 
	process (clock)
	begin
		if clock'event and clock='1' then
			ctrl_C <= ctrl_C_int;
			queue_addr_int_C <= queue_addr_int_C_int;
		end if;
	end process;

	process(EA_C)
	begin
		case EA_C is  
			
			when S_RESET =>
				ctrl_C_int <= WEST;							
				queue_addr_L_C <= '0'; 					
				queue_addr_int_C_int <= (others=>'0');		
		
			when S_IDLE =>
			    ctrl_C_int <= ctrl_C;
				queue_addr_L_C <= '0'; 					
				queue_addr_int_C_int <= (others=>'0');	
			 
			when S_CTRL_1 => 
				ctrl_C_int <= ((ctrl_C+1) and "011");					
				queue_addr_L_C <= '0'; 					
				queue_addr_int_C_int(conv_integer(ctrl_C)) <= '0';
				queue_addr_int_C_int(conv_integer((ctrl_C+1) and "011")) <= '1';	
				queue_addr_int_C_int(conv_integer((ctrl_C+2) and "011")) <= queue_addr_int_C(conv_integer((ctrl_C+2) and "011"));	
				queue_addr_int_C_int(conv_integer((ctrl_C+3) and "011")) <= queue_addr_int_C(conv_integer((ctrl_C+3) and "011"));	
			
			when S_CTRL_2 => 
				ctrl_C_int <= ((ctrl_C+2) and "011");					
				queue_addr_L_C <= '0'; 					
				queue_addr_int_C_int(conv_integer(ctrl_C)) <= '0';	   	
				queue_addr_int_C_int(conv_integer((ctrl_C+1) and "011")) <= queue_addr_int_C(conv_integer((ctrl_C+1) and "011"));	
				queue_addr_int_C_int(conv_integer((ctrl_C+2) and "011")) <= '1'; 
				queue_addr_int_C_int(conv_integer((ctrl_C+3) and "011")) <= queue_addr_int_C(conv_integer((ctrl_C+3) and "011"));	
			
			when S_CTRL_3 =>
				ctrl_C_int <= ((ctrl_C+3) and "011");					
				queue_addr_L_C <= '0'; 					
				queue_addr_int_C_int(conv_integer(ctrl_C)) <= '0'; 
				queue_addr_int_C_int(conv_integer((ctrl_C+1) and "011")) <= queue_addr_int_C(conv_integer((ctrl_C+1) and "011"));	
				queue_addr_int_C_int(conv_integer((ctrl_C+2) and "011")) <= queue_addr_int_C(conv_integer((ctrl_C+2) and "011"));	
				queue_addr_int_C_int(conv_integer((ctrl_C+3) and "011")) <= '1';  
				
			when S_CTRL_4 =>  
			    ctrl_C_int <= ctrl_C;
				queue_addr_L_C <= '0'; 					
				queue_addr_int_C_int(conv_integer(ctrl_C)) <= '1';
				queue_addr_int_C_int(conv_integer((ctrl_C+1) and "011")) <= queue_addr_int_C(conv_integer((ctrl_C+1) and "011"));	
				queue_addr_int_C_int(conv_integer((ctrl_C+2) and "011")) <= queue_addr_int_C(conv_integer((ctrl_C+2) and "011"));	
			   	queue_addr_int_C_int(conv_integer((ctrl_C+3) and "011")) <= queue_addr_int_C(conv_integer((ctrl_C+3) and "011"));	
				
			when S_TRYSEND =>	 
			   	ctrl_C_int <= ctrl_C;
				queue_addr_L_C <= '0'; 
				queue_addr_int_C_int <= queue_addr_int_C;
				
			when S_SEND =>	 
			   ctrl_C_int <= ctrl_C;
			   queue_addr_L_C <= '0'; 	
			   queue_addr_int_C_int <= queue_addr_int_C;
	
			when S_TRYSEND_LOCAL =>
			   ctrl_C_int <= ctrl_C;
			   queue_addr_L_C <= '1';	
			   queue_addr_int_C_int <= queue_addr_int_C;
			
			when S_SEND_LOCAL =>
			  	ctrl_C_int <= ctrl_C;
				queue_addr_L_C <= '1';	
				queue_addr_int_C_int <= queue_addr_int_C;
		end case;
	end process;	
		
end AlgoritmoCG;