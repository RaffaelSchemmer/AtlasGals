library IEEE;
use IEEE.STD_LOGIC_1164.all;
use IEEE.STD_LOGIC_unsigned.all;
use work.HermesPackage.all;

entity SwitchControl is
port(
	clock   : in  std_logic;
	reset   : in  std_logic;
	h       : in  regNport;
	ack_h   : out regNport;
	address : in  regmetadeflit;
	data    : in  arrayNport_regflit;
	sender  : in  regNport;
	free    : out regNport;
	mux_in  : out arrayNport_reg3;
	mux_out : out arrayNport_reg3);
end SwitchControl;

architecture AlgorithmXY of SwitchControl is

type state is (S0,S1,S2,S3,S4,S5,S6,S7);
signal ES, PES: state;

-- sinais do arbitro
signal ask: std_logic := '0';
signal sel,prox: integer range 0 to (NPORT-1) := 0;
signal incoming: reg3 := (others=>'0');
signal header : regflit := (others=>'0');

-- sinais do controle
signal dirx,diry: integer range 0 to (NPORT-1) := 0; 
signal lx,ly,tx,ty: regquartoflit := (others=>'0');
signal auxfree: regNport := (others=>'0');
signal source:  arrayNport_reg3 := (others=>(others=>'0'));
signal sender_ant: regNport := (others=>'0');

begin

	ask <= '1' when h(LOCAL)='1' or h(EAST)='1' or h(WEST)='1' or h(NORTH)='1' or h(SOUTH)='1' else '0';
	incoming <= CONV_VECTOR(sel);
	header <= data(CONV_INTEGER(incoming));

	process(sel,h)
	begin
		case sel is
			when LOCAL=> 
				if h(EAST)='1' then prox<=EAST;
				elsif h(WEST)='1' then  prox<=WEST;
				elsif h(NORTH)='1' then prox<=NORTH;
				elsif h(SOUTH)='1' then prox<=SOUTH;
				else prox<=LOCAL; end if;
			when EAST=> 
				if h(WEST)='1' then     prox<=WEST;
				elsif h(NORTH)='1' then prox<=NORTH;
				elsif h(SOUTH)='1' then prox<=SOUTH;
				elsif h(LOCAL)='1' then prox<=LOCAL;
				else prox<=EAST; end if;
			when WEST=> 
				if h(NORTH)='1' then prox<=NORTH;
				elsif h(SOUTH)='1' then prox<=SOUTH;
				elsif h(LOCAL)='1' then prox<=LOCAL;
				elsif h(EAST)='1' then prox<=EAST;
				else prox<=WEST; end if;
			when NORTH=> 
				if h(SOUTH)='1' then prox<=SOUTH;
				elsif h(LOCAL)='1' then prox<=LOCAL;
				elsif h(EAST)='1' then prox<=EAST;
				elsif h(WEST)='1' then prox<=WEST;
				else prox<=NORTH; end if;
			when SOUTH=> 
				if h(LOCAL)='1' then prox<=LOCAL;
				elsif h(EAST)='1' then prox<=EAST;
				elsif h(WEST)='1' then prox<=WEST;
				elsif h(NORTH)='1' then prox<=NORTH;
				else prox<=SOUTH; end if;
		end case;
	end process;

	lx <= address((METADEFLIT - 1) downto QUARTOFLIT);
	ly <= address((QUARTOFLIT - 1) downto 0);

	tx <= header((METADEFLIT - 1) downto QUARTOFLIT);
	ty <= header((QUARTOFLIT - 1) downto 0);

	dirx <= WEST when lx > tx else EAST;
	diry <= NORTH when ly < ty else SOUTH;

	process(reset,clock)
	begin
		if reset='1' then
			ES<=S0;
		elsif clock'event and clock='1' then
			ES<=PES;
		end if;
	end process;

	------------------------------------------------------------------------------------------------------  
	-- PARTE COMBINACIONAL PARA DEFINIR O PRÓXIMO ESTADO DA MÁQUINA.
	--
	-- SO -> O estado S0 é o estado de inicialização da máquina. Este estado somente é 
	--       atingido quando o sinal reset é ativado.
	-- S1 -> O estado S1 é o estado de espera por requisição de chaveamento. Quando o 
	--       árbitro recebe uma ou mais requisições o sinal ask é ativado fazendo a 
	--       máquina avançar para o estado S2. 
	-- S2 -> No estado S2 a porta de entrada que solicitou chaveamento é selecionada. Se 
	--       houver mais de uma, aquela com maior prioridade é a selecionada.
	-- S3 -> No estado S3 é realizado algoritmo de chaveamento XY. O algoritmo de chaveamento
	--       XY faz a comparação do endereço da chave atual com o endereço da chave destino do
	--       pacote (armazenado no primeiro flit do pacote). O pacote deve ser chaveado para a
	--       porta Local da chave quando o endereço xLyL* da chave atual for igual ao endereço
	--       xTyT* da chave destino do pacote. Caso contrário, é realizada, primeiramente, a 
	--       comparação horizontal de endereços. A comparação horizontal determina se o pacote
	--       deve ser chaveado para o Leste (xL<xT), para o Oeste (xL>xT), ou se o mesmo já 
	--       está horizontalmente alinhado à chave destino (xL=xT). Caso esta última condição 
	--       seja verdadeira é realizada a comparação vertical que determina se o pacote deve 
	--       ser chaveado para o Sul (yL<yT) ou para o Norte (yL>yT). Caso a porta vertical 
	--       escolhida esteja ocupada, é realizado o bloqueio dos flits do pacote até que o 
	--       pacote possa ser chaveado.
	-- S4, S5 e S6 -> Nestes estados é estabelecida a conexão da porta de entrada com a de
	--       de saída através do preenchimento dos sinais mux_in e mux_out.
	-- S7 -> O estado S7 é necessário para que a porta selecionada para roteamento baixe o sinal
	--       h.
	--       
	process(ES,ask,h,lx,ly,tx,ty,auxfree,dirx,diry)
	begin
		case ES is
			when S0 => PES <= S1;
			when S1 => if ask='1' then PES <= S2; else PES <= S1; end if;
			when S2 => PES <= S3;
			when S3 => if lx = tx and ly = ty and auxfree(LOCAL)='1' then PES<=S4;
					elsif lx /= tx and auxfree(dirx)='1' then PES<=S5;
					elsif lx = tx and ly /= ty and auxfree(diry)='1' then PES<=S6;
					else PES<=S1; end if;                                    
			when S4 => PES<=S7; 
			when S5 => PES<=S7; 
			when S6 => PES<=S7; 
			when S7 => PES<=S1;
		end case;
	end process;

	------------------------------------------------------------------------------------------------------  
	-- executa as ações correspondente ao estado atual da máquina de estados
	------------------------------------------------------------------------------------------------------  
	process (clock)
	begin
		if clock'event and clock='1' then
			case ES is
				-- Zera variáveis
				when S0 =>
					sel <= 0;
					ack_h <= (others => '0');
					auxfree <= (others=> '1');
					sender_ant    <= (others=> '0');
					mux_out <= (others=>(others=>'0'));
					source <= (others=>(others=>'0'));
				-- Chegou um header
				when S1=>
					ack_h <= (others => '0');
				-- Seleciona quem tera direito a requisitar roteamento
				when S2=>
					sel <= prox;
				-- Estabelece a conexão com a porta LOCAL
				when S4 =>
					source(CONV_INTEGER(incoming)) <= CONV_VECTOR(LOCAL);
					mux_out(LOCAL) <= incoming;
					auxfree(LOCAL) <= '0';
					ack_h(sel)<='1';
				-- Estabelece a conexão com a porta EAST ou WEST
				when S5 =>
					source(CONV_INTEGER(incoming)) <= CONV_VECTOR(dirx);
					mux_out(dirx) <= incoming;
					auxfree(dirx) <= '0'; 
					ack_h(sel)<='1';
				-- Estabelece a conexão com a porta NORTH ou SOUTH
				when S6 =>
					source(CONV_INTEGER(incoming)) <= CONV_VECTOR(diry);
					mux_out(diry) <= incoming;
					auxfree(diry) <= '0'; 
					ack_h(sel)<='1';
				when others => ack_h(sel)<='0';
			end case;

			sender_ant(LOCAL) <= sender(LOCAL);
			sender_ant(EAST)  <= sender(EAST); 
			sender_ant(WEST)  <= sender(WEST); 
			sender_ant(NORTH) <= sender(NORTH);
			sender_ant(SOUTH) <= sender(SOUTH);

			if sender(LOCAL)='0' and  sender_ant(LOCAL)='1' then auxfree(CONV_INTEGER(source(LOCAL))) <='1'; end if;
			if sender(EAST) ='0' and  sender_ant(EAST)='1'  then auxfree(CONV_INTEGER(source(EAST)))  <='1'; end if;
			if sender(WEST) ='0' and  sender_ant(WEST)='1'  then auxfree(CONV_INTEGER(source(WEST)))  <='1'; end if;
			if sender(NORTH)='0' and  sender_ant(NORTH)='1' then auxfree(CONV_INTEGER(source(NORTH))) <='1'; end if;
			if sender(SOUTH)='0' and  sender_ant(SOUTH)='1' then auxfree(CONV_INTEGER(source(SOUTH))) <='1'; end if;

		end if;
	end process;

	mux_in <= source;
	free <= auxfree;

end AlgorithmXY;

architecture AlgorithmWFM of SwitchControl is

type state is (S0,S1,S2,S3,S4,S_LOCAL,S_EAST,S_WEST,S_SOUTH,S_NORTH);
signal ES, PES: state;

-- sinais do arbitro
signal ask: std_logic := '0';
signal sel,prox: integer range 0 to (NPORT-1) := 0; 
signal incoming: reg3 := (others=>'0');
signal header : regflit := (others=>'0');

-- sinais do controle
signal lx,ly,tx,ty: regquartoflit := (others=>'0');
signal auxfree: regNport := (others=>'0');
signal source:  arrayNport_reg3 := (others=>(others=>'0'));
signal sender_ant: regNport := (others=>'0');

begin

	ask <= '1' when h(LOCAL)='1' or h(EAST)='1' or h(WEST)='1' or h(NORTH)='1' or h(SOUTH)='1' else '0';
	incoming <= CONV_VECTOR(sel);
	header <= data(CONV_INTEGER(incoming));

	process(sel,h)
	begin
		case sel is
			when LOCAL=> 
				if h(EAST)='1' then prox<=EAST;
				elsif h(WEST)='1' then  prox<=WEST;
				elsif h(NORTH)='1' then prox<=NORTH;
				elsif h(SOUTH)='1' then prox<=SOUTH;
				else prox<=LOCAL; end if;
			when EAST=> 
				if h(WEST)='1' then     prox<=WEST;
				elsif h(NORTH)='1' then prox<=NORTH;
				elsif h(SOUTH)='1' then prox<=SOUTH;
				elsif h(LOCAL)='1' then prox<=LOCAL;
				else prox<=EAST; end if;
			when WEST=> 
				if h(NORTH)='1' then prox<=NORTH;
				elsif h(SOUTH)='1' then prox<=SOUTH;
				elsif h(LOCAL)='1' then prox<=LOCAL;
				elsif h(EAST)='1' then prox<=EAST;
				else prox<=WEST; end if;
			when NORTH=> 
				if h(SOUTH)='1' then prox<=SOUTH;
				elsif h(LOCAL)='1' then prox<=LOCAL;
				elsif h(EAST)='1' then prox<=EAST;
				elsif h(WEST)='1' then prox<=WEST;
				else prox<=NORTH; end if;
			when SOUTH=> 
				if h(LOCAL)='1' then prox<=LOCAL;
				elsif h(EAST)='1' then prox<=EAST;
				elsif h(WEST)='1' then prox<=WEST;
				elsif h(NORTH)='1' then prox<=NORTH;
				else prox<=SOUTH; end if;
		end case;
	end process;

	lx <= address((METADEFLIT - 1) downto QUARTOFLIT);
	ly <= address((QUARTOFLIT - 1) downto 0);

	tx <= header((METADEFLIT - 1) downto QUARTOFLIT); 
	ty <= header((QUARTOFLIT - 1) downto 0);

	process(reset,clock)
	begin
		if reset='1' then
			ES<=S0;
		elsif clock'event and clock='1' then
			ES<=PES;
		end if;
	end process;

	------------------------------------------------------------------------------------------------------  
	-- PARTE COMBINACIONAL PARA DEFINIR O PRÓXIMO ESTADO DA MÁQUINA.
	--
	-- SO -> O estado S0 é o estado de inicialização da máquina. Este estado somente é 
	--       atingido quando o sinal reset é ativado.
	-- S1 -> O estado S1 é o estado de espera por requisição de chaveamento. Quando o 
	--       árbitro recebe uma ou mais requisições o sinal ask é ativado fazendo a 
	--       máquina avançar para o estado S2. 
	-- S2 -> No estado S2 a porta de entrada que solicitou chaveamento é selecionada. Se 
	--       houver mais de uma, aquela com maior prioridade é a selecionada.
	-- S3 -> No estado S3 é realizado algoritmo de chaveamento West First Minimal.
	-- S_LOCAL,S_EAST,S_WEST,S_NORTH e S_SOUTH -> Nestes estados é estabelecida a conexão
	--       da porta de entrada com a de saída através do preenchimento dos sinais mux_in
	--       e mux_out.
	-- S4 -> O estado S4 é necessário para que a porta selecionada para roteamento baixe
	--       o sinal h.
	--       
	process(ES,ask,h,lx,ly,tx,ty,auxfree)
	begin
		case ES is
			when S0 => PES <= S1;
			when S1 => if ask='1' then PES <= S2; else PES <= S1; end if;
			when S2 => PES <= S3;
			when S3 => if lx = tx and ly = ty and auxfree(LOCAL)='1' then PES<=S_LOCAL;
				elsif lx > tx and auxfree(WEST)='1' then PES<=S_WEST; 
				elsif lx < tx and auxfree(EAST)='1' then PES<=S_EAST;
				elsif (lx = tx or lx < tx) and ly < ty and auxfree(NORTH)='1' then PES<=S_NORTH;
				elsif (lx = tx or lx < tx) and ly > ty and auxfree(SOUTH)='1' then PES<=S_SOUTH;
				else PES<= S1; end if;
			when S_LOCAL | S_EAST | S_WEST | S_NORTH | S_SOUTH => PES<=S4;
			when S4=> PES<=S1;
		end case;
	end process;

	------------------------------------------------------------------------------------------------------  
	-- executa as ações correspondente ao estado atual da máquina de estados
	------------------------------------------------------------------------------------------------------  
	process (clock)
	begin
		if clock'event and clock='1' then
			case ES is
				-- Zera variáveis
				when S0 =>
					sel <= 0;
					ack_h <= (others => '0');
					auxfree <= (others=> '1');
					sender_ant    <= (others=> '0');
					mux_out <= (others=>(others=>'0'));
					source <= (others=>(others=>'0'));
				-- Chegou um header
				when S1=>
					ack_h <= (others => '0');
				-- Seleciona quem tera direito a requisitar roteamento
				when S2=>
					sel <= prox;
				when S_LOCAL =>
					source(CONV_INTEGER(incoming)) <= CONV_VECTOR(LOCAL);
					mux_out(LOCAL) <= incoming;
					auxfree(LOCAL) <= '0';
					ack_h(sel)<='1';
				when S_EAST =>
					source(CONV_INTEGER(incoming)) <= CONV_VECTOR(EAST);
					mux_out(EAST) <= incoming;
					auxfree(EAST) <= '0'; 
					ack_h(sel)<='1';
				when S_WEST =>
					source(CONV_INTEGER(incoming)) <= CONV_VECTOR(WEST);
					mux_out(WEST) <= incoming;
					auxfree(WEST) <= '0'; 
					ack_h(sel)<='1';
				when S_NORTH =>
					source(CONV_INTEGER(incoming)) <= CONV_VECTOR(NORTH);
					mux_out(NORTH) <= incoming;
					auxfree(NORTH) <= '0'; 
					ack_h(sel)<='1';
				when S_SOUTH =>
					source(CONV_INTEGER(incoming)) <= CONV_VECTOR(SOUTH);
					mux_out(SOUTH) <= incoming;
					auxfree(SOUTH) <= '0'; 
					ack_h(sel)<='1';
				when others => ack_h(sel)<='0';
			end case;

			sender_ant(LOCAL) <= sender(LOCAL);
			sender_ant(EAST)  <= sender(EAST); 
			sender_ant(WEST)  <= sender(WEST); 
			sender_ant(NORTH) <= sender(NORTH);
			sender_ant(SOUTH) <= sender(SOUTH);

			if sender(LOCAL)='0' and  sender_ant(LOCAL)='1' then auxfree(CONV_INTEGER(source(LOCAL))) <='1'; end if;
			if sender(EAST) ='0' and  sender_ant(EAST)='1'  then auxfree(CONV_INTEGER(source(EAST)))  <='1'; end if;
			if sender(WEST) ='0' and  sender_ant(WEST)='1'  then auxfree(CONV_INTEGER(source(WEST)))  <='1'; end if;
			if sender(NORTH)='0' and  sender_ant(NORTH)='1' then auxfree(CONV_INTEGER(source(NORTH))) <='1'; end if;
			if sender(SOUTH)='0' and  sender_ant(SOUTH)='1' then auxfree(CONV_INTEGER(source(SOUTH))) <='1'; end if;

		end if;
	end process;

	mux_in <= source;
	free <= auxfree;

end AlgorithmWFM;

architecture AlgorithmNLM of SwitchControl is

type state is (S0,S1,S2,S3,S4,S_LOCAL,S_EAST,S_WEST,S_SOUTH,S_NORTH);
signal ES, PES: state;

-- sinais do arbitro
signal ask: std_logic := '0';
signal sel,prox: integer range 0 to (NPORT-1) := 0; 
signal incoming: reg3 := (others=>'0');
signal header : regflit := (others=>'0');

-- sinais do controle
signal lx,ly,tx,ty: regquartoflit := (others=>'0');
signal auxfree: regNport := (others=>'0');
signal source:  arrayNport_reg3 := (others=>(others=>'0'));
signal sender_ant: regNport := (others=>'0');

begin

	ask <= '1' when h(LOCAL)='1' or h(EAST)='1' or h(WEST)='1' or h(NORTH)='1' or h(SOUTH)='1' else '0';
	incoming <= CONV_VECTOR(sel);
	header <= data(CONV_INTEGER(incoming));

	process(sel,h)
	begin
		case sel is
			when LOCAL=> 
				if h(EAST)='1' then prox<=EAST;
				elsif h(WEST)='1' then  prox<=WEST;
				elsif h(NORTH)='1' then prox<=NORTH;
				elsif h(SOUTH)='1' then prox<=SOUTH;
				else prox<=LOCAL; end if;
			when EAST=> 
				if h(WEST)='1' then     prox<=WEST;
				elsif h(NORTH)='1' then prox<=NORTH;
				elsif h(SOUTH)='1' then prox<=SOUTH;
				elsif h(LOCAL)='1' then prox<=LOCAL;
				else prox<=EAST; end if;
			when WEST=> 
				if h(NORTH)='1' then prox<=NORTH;
				elsif h(SOUTH)='1' then prox<=SOUTH;
				elsif h(LOCAL)='1' then prox<=LOCAL;
				elsif h(EAST)='1' then prox<=EAST;
				else prox<=WEST; end if;
			when NORTH=> 
				if h(SOUTH)='1' then prox<=SOUTH;
				elsif h(LOCAL)='1' then prox<=LOCAL;
				elsif h(EAST)='1' then prox<=EAST;
				elsif h(WEST)='1' then prox<=WEST;
				else prox<=NORTH; end if;
			when SOUTH=> 
				if h(LOCAL)='1' then prox<=LOCAL;
				elsif h(EAST)='1' then prox<=EAST;
				elsif h(WEST)='1' then prox<=WEST;
				elsif h(NORTH)='1' then prox<=NORTH;
				else prox<=SOUTH; end if;
		end case;
	end process;

	lx <= address((METADEFLIT - 1) downto QUARTOFLIT);
	ly <= address((QUARTOFLIT - 1) downto 0);

	tx <= header((METADEFLIT - 1) downto QUARTOFLIT);
	ty <= header((QUARTOFLIT - 1) downto 0);

	process(reset,clock)
	begin
		if reset='1' then
			ES<=S0;
		elsif clock'event and clock='1' then
			ES<=PES;
		end if;
	end process;

	------------------------------------------------------------------------------------------------------  
	-- PARTE COMBINACIONAL PARA DEFINIR O PRÓXIMO ESTADO DA MÁQUINA.
	--
	-- SO -> O estado S0 é o estado de inicialização da máquina. Este estado somente é 
	--       atingido quando o sinal reset é ativado.
	-- S1 -> O estado S1 é o estado de espera por requisição de chaveamento. Quando o 
	--       árbitro recebe uma ou mais requisições o sinal ask é ativado fazendo a 
	--       máquina avançar para o estado S2. 
	-- S2 -> No estado S2 a porta de entrada que solicitou chaveamento é selecionada. Se 
	--       houver mais de uma, aquela com maior prioridade é a selecionada.
	-- S3 -> No estado S3 é realizado algoritmo de chaveamento North Last Minimal.
	-- S_LOCAL,S_EAST,S_WEST,S_NORTH e S_SOUTH -> Nestes estados é estabelecida a conexão
	--       da porta de entrada com a de saída através do preenchimento dos sinais mux_in
	--       e mux_out.
	-- S4 -> O estado S4 é necessário para que a porta selecionada para roteamento baixe
	--       o sinal h.
	--       
	process(ES,ask,h,lx,ly,tx,ty,auxfree)
	begin
		case ES is
			when S0 => PES <= S1;
			when S1 => if ask='1' then PES <= S2; else PES <= S1; end if;
			when S2 => PES <= S3;
			when S3 => if lx = tx and ly = ty and auxfree(LOCAL)='1' then PES<=S_LOCAL;
				elsif lx < tx and auxfree(EAST)='1' then PES<=S_EAST; 
				elsif lx > tx and auxfree(WEST)='1' then PES<=S_WEST; 
				elsif ly > ty and auxfree(SOUTH)='1' then PES<=S_SOUTH; 
				elsif lx = tx and ly < ty and auxfree(NORTH)='1' then PES<=S_NORTH; 
				else PES<= S1; end if;
			when S_LOCAL | S_EAST | S_WEST | S_NORTH | S_SOUTH => PES<=S4;
			when S4=> PES<=S1;
		end case;
	end process;

	------------------------------------------------------------------------------------------------------  
	-- executa as ações correspondente ao estado atual da máquina de estados
	------------------------------------------------------------------------------------------------------  
	process (clock)
	begin
		if clock'event and clock='1' then
			case ES is
				-- Zera variáveis
				when S0 =>
					sel <= 0;
					ack_h <= (others => '0');
					auxfree <= (others=> '1');
					sender_ant    <= (others=> '0');
					mux_out <= (others=>(others=>'0'));
					source <= (others=>(others=>'0'));
				-- Chegou um header
				when S1=>
					ack_h <= (others => '0');
				-- Seleciona quem tera direito a requisitar roteamento
				when S2=>
					sel <= prox;
				when S_LOCAL =>
					source(CONV_INTEGER(incoming)) <= CONV_VECTOR(LOCAL);
					mux_out(LOCAL) <= incoming;
					auxfree(LOCAL) <= '0';
					ack_h(sel)<='1';
				when S_EAST =>
					source(CONV_INTEGER(incoming)) <= CONV_VECTOR(EAST);
					mux_out(EAST) <= incoming;
					auxfree(EAST) <= '0'; 
					ack_h(sel)<='1';
				when S_WEST =>
					source(CONV_INTEGER(incoming)) <= CONV_VECTOR(WEST);
					mux_out(WEST) <= incoming;
					auxfree(WEST) <= '0';
					ack_h(sel)<='1';
				when S_NORTH =>
					source(CONV_INTEGER(incoming)) <= CONV_VECTOR(NORTH);
					mux_out(NORTH) <= incoming;
					auxfree(NORTH) <= '0';
					ack_h(sel)<='1';
				when S_SOUTH =>
					source(CONV_INTEGER(incoming)) <= CONV_VECTOR(SOUTH);
					mux_out(SOUTH) <= incoming;
					auxfree(SOUTH) <= '0';
					ack_h(sel)<='1';
				when others => ack_h(sel)<='0';
			end case;

			sender_ant(LOCAL) <= sender(LOCAL);
			sender_ant(EAST)  <= sender(EAST); 
			sender_ant(WEST)  <= sender(WEST); 
			sender_ant(NORTH) <= sender(NORTH);
			sender_ant(SOUTH) <= sender(SOUTH);
			
			if sender(LOCAL)='0' and  sender_ant(LOCAL)='1' then auxfree(CONV_INTEGER(source(LOCAL))) <='1'; end if;
			if sender(EAST) ='0' and  sender_ant(EAST)='1'  then auxfree(CONV_INTEGER(source(EAST)))  <='1'; end if;
			if sender(WEST) ='0' and  sender_ant(WEST)='1'  then auxfree(CONV_INTEGER(source(WEST)))  <='1'; end if;
			if sender(NORTH)='0' and  sender_ant(NORTH)='1' then auxfree(CONV_INTEGER(source(NORTH))) <='1'; end if;
			if sender(SOUTH)='0' and  sender_ant(SOUTH)='1' then auxfree(CONV_INTEGER(source(SOUTH))) <='1'; end if;

		end if;
	end process;

	mux_in <= source;
	free <= auxfree;

end AlgorithmNLM;

architecture AlgorithmNFM of SwitchControl is

type state is (S0,S1,S2,S3,S4,S_LOCAL,S_EAST,S_WEST,S_SOUTH,S_NORTH);
signal ES, PES: state;

-- sinais do arbitro
signal ask: std_logic := '0';
signal sel,prox: integer range 0 to (NPORT-1) := 0;
signal incoming: reg3 := (others=>'0');
signal header : regflit := (others=>'0');

-- sinais do controle
signal lx,ly,tx,ty: regquartoflit := (others=>'0');
signal auxfree: regNport := (others=>'0');
signal source:  arrayNport_reg3 := (others=>(others=>'0'));
signal sender_ant: regNport := (others=>'0');

begin

	ask <= '1' when h(LOCAL)='1' or h(EAST)='1' or h(WEST)='1' or h(NORTH)='1' or h(SOUTH)='1' else '0';
	incoming <= CONV_VECTOR(sel);
	header <= data(CONV_INTEGER(incoming));

	process(sel,h)
	begin
		case sel is
			when LOCAL=>
				if h(EAST)='1' then prox<=EAST;
				elsif h(WEST)='1' then  prox<=WEST;
				elsif h(NORTH)='1' then prox<=NORTH;
				elsif h(SOUTH)='1' then prox<=SOUTH;
				else prox<=LOCAL; end if;
			when EAST=>
				if h(WEST)='1' then     prox<=WEST;
				elsif h(NORTH)='1' then prox<=NORTH;
				elsif h(SOUTH)='1' then prox<=SOUTH;
				elsif h(LOCAL)='1' then prox<=LOCAL;
				else prox<=EAST; end if;
			when WEST=>
				if h(NORTH)='1' then prox<=NORTH;
				elsif h(SOUTH)='1' then prox<=SOUTH;
				elsif h(LOCAL)='1' then prox<=LOCAL;
				elsif h(EAST)='1' then prox<=EAST;
				else prox<=WEST; end if;
			when NORTH=>
				if h(SOUTH)='1' then prox<=SOUTH;
				elsif h(LOCAL)='1' then prox<=LOCAL;
				elsif h(EAST)='1' then prox<=EAST;
				elsif h(WEST)='1' then prox<=WEST;
				else prox<=NORTH; end if;
			when SOUTH=>
				if h(LOCAL)='1' then prox<=LOCAL;
				elsif h(EAST)='1' then prox<=EAST;
				elsif h(WEST)='1' then prox<=WEST;
				elsif h(NORTH)='1' then prox<=NORTH;
				else prox<=SOUTH; end if;
		end case;
	end process;

	lx <= address((METADEFLIT - 1) downto QUARTOFLIT);
	ly <= address((QUARTOFLIT - 1) downto 0);

	tx <= header((METADEFLIT - 1) downto QUARTOFLIT);
	ty <= header((QUARTOFLIT - 1) downto 0);

	process(reset,clock)
	begin
		if reset='1' then
			ES<=S0;
		elsif clock'event and clock='1' then
			ES<=PES;
		end if;
	end process;

	------------------------------------------------------------------------------------------------------  
	-- PARTE COMBINACIONAL PARA DEFINIR O PRÓXIMO ESTADO DA MÁQUINA.
	--
	-- SO -> O estado S0 é o estado de inicialização da máquina. Este estado somente é 
	--       atingido quando o sinal reset é ativado.
	-- S1 -> O estado S1 é o estado de espera por requisição de chaveamento. Quando o 
	--       árbitro recebe uma ou mais requisições o sinal ask é ativado fazendo a 
	--       máquina avançar para o estado S2. 
	-- S2 -> No estado S2 a porta de entrada que solicitou chaveamento é selecionada. Se 
	--       houver mais de uma, aquela com maior prioridade é a selecionada.
	-- S3 -> No estado S3 é realizado algoritmo de chaveamento North Last Minimal.
	-- S_LOCAL,S_EAST,S_WEST,S_NORTH e S_SOUTH -> Nestes estados é estabelecida a conexão
	--       da porta de entrada com a de saída através do preenchimento dos sinais mux_in
	--       e mux_out.
	-- S4 -> O estado S4 é necessário para que a porta selecionada para roteamento baixe
	--       o sinal h.
	--       
	process(ES,ask,h,lx,ly,tx,ty,auxfree)
	begin
		case ES is
			when S0 => PES <= S1;
			when S1 => if ask='1' then PES <= S2; else PES <= S1; end if;
			when S2 => PES <= S3;
			when S3 => if lx = tx and ly = ty and auxfree(LOCAL)='1' then PES<=S_LOCAL;
				elsif lx > tx and auxfree(WEST)='1' then PES<=S_WEST; 
				elsif ly > ty and auxfree(SOUTH)='1' then PES<=S_SOUTH; 
				elsif (ly = ty or ly < ty) and lx < tx and auxfree(EAST)='1' then PES<=S_EAST; 
				elsif (lx = tx or lx < tx) and ly < ty and auxfree(NORTH)='1' then PES<=S_NORTH; 
				else PES<= S1; end if;
			when S_LOCAL | S_EAST | S_WEST | S_NORTH | S_SOUTH => PES<=S4;
			when S4=> PES<=S1;
		end case;
	end process;

	------------------------------------------------------------------------------------------------------  
	-- executa as ações correspondente ao estado atual da máquina de estados
	------------------------------------------------------------------------------------------------------  
	process (clock)
	begin
		if clock'event and clock='1' then
			case ES is
				-- Zera variáveis
				when S0 =>
					sel <= 0;
					ack_h <= (others => '0');
					auxfree <= (others=> '1');
					sender_ant    <= (others=> '0');
					mux_out <= (others=>(others=>'0'));
					source <= (others=>(others=>'0'));
				-- Chegou um header
				when S1=>
					ack_h <= (others => '0');
				-- Seleciona quem tera direito a requisitar roteamento
				when S2=>
					sel <= prox;
				when S_LOCAL =>
					source(CONV_INTEGER(incoming)) <= CONV_VECTOR(LOCAL);
					mux_out(LOCAL) <= incoming;
					auxfree(LOCAL) <= '0';
					ack_h(sel)<='1';
				when S_EAST =>
					source(CONV_INTEGER(incoming)) <= CONV_VECTOR(EAST);
					mux_out(EAST) <= incoming;
					auxfree(EAST) <= '0';
					ack_h(sel)<='1';
				when S_WEST =>
					source(CONV_INTEGER(incoming)) <= CONV_VECTOR(WEST);
					mux_out(WEST) <= incoming;
					auxfree(WEST) <= '0';
					ack_h(sel)<='1';
				when S_NORTH =>
					source(CONV_INTEGER(incoming)) <= CONV_VECTOR(NORTH);
					mux_out(NORTH) <= incoming;
					auxfree(NORTH) <= '0';
					ack_h(sel)<='1';
				when S_SOUTH =>
					source(CONV_INTEGER(incoming)) <= CONV_VECTOR(SOUTH);
					mux_out(SOUTH) <= incoming;
					auxfree(SOUTH) <= '0';
					ack_h(sel)<='1';
				when others => ack_h(sel)<='0';
			end case;

			sender_ant(LOCAL) <= sender(LOCAL);
			sender_ant(EAST)  <= sender(EAST); 
			sender_ant(WEST)  <= sender(WEST); 
			sender_ant(NORTH) <= sender(NORTH);
			sender_ant(SOUTH) <= sender(SOUTH);

			if sender(LOCAL)='0' and  sender_ant(LOCAL)='1' then auxfree(CONV_INTEGER(source(LOCAL))) <='1'; end if;
			if sender(EAST) ='0' and  sender_ant(EAST)='1'  then auxfree(CONV_INTEGER(source(EAST)))  <='1'; end if;
			if sender(WEST) ='0' and  sender_ant(WEST)='1'  then auxfree(CONV_INTEGER(source(WEST)))  <='1'; end if;
			if sender(NORTH)='0' and  sender_ant(NORTH)='1' then auxfree(CONV_INTEGER(source(NORTH))) <='1'; end if;
			if sender(SOUTH)='0' and  sender_ant(SOUTH)='1' then auxfree(CONV_INTEGER(source(SOUTH))) <='1'; end if;

		end if;
	end process;

	mux_in <= source;
	free <= auxfree;

end AlgorithmNFM;

--------------------------------------------------------------------------------
-- ALGORITMOS NÃO MINIMOS
--------------------------------------------------------------------------------
-- Algoritmo West-First Não Minimo
architecture AlgorithmWFNM of SwitchControl is

type state is (S0,S1,S2,S3,S4,S_LOCAL,S_EAST,S_WEST,S_SOUTH,S_NORTH);
signal ES, PES: state;

-- sinais do arbitro
signal ask: std_logic := '0';
signal sel,prox: integer range 0 to (NPORT-1) := 0;
signal incoming: reg3 := (others=>'0');
signal header : regflit := (others=>'0');

-- sinais do controle
signal lx,ly,tx,ty: regquartoflit := (others=>'0');
signal auxfree: regNport := (others=>'0');
signal source:  arrayNport_reg3 := (others=>(others=>'0'));
signal sender_ant: regNport := (others=>'0');

begin

	ask <= '1' when h(LOCAL)='1' or h(EAST)='1' or h(WEST)='1' or h(NORTH)='1' or h(SOUTH)='1' else '0';
	incoming <= CONV_VECTOR(sel);
	header <= data(CONV_INTEGER(incoming));

	process(sel,h)
	begin
		case sel is
			when LOCAL=> 
				if h(EAST)='1' then prox<=EAST;
				elsif h(WEST)='1' then  prox<=WEST;
				elsif h(NORTH)='1' then prox<=NORTH;
				elsif h(SOUTH)='1' then prox<=SOUTH;
				else prox<=LOCAL; end if;
			when EAST=> 
				if h(WEST)='1' then     prox<=WEST;
				elsif h(NORTH)='1' then prox<=NORTH;
				elsif h(SOUTH)='1' then prox<=SOUTH;
				elsif h(LOCAL)='1' then prox<=LOCAL;
				else prox<=EAST; end if;
			when WEST=> 
				if h(NORTH)='1' then prox<=NORTH;
				elsif h(SOUTH)='1' then prox<=SOUTH;
				elsif h(LOCAL)='1' then prox<=LOCAL;
				elsif h(EAST)='1' then prox<=EAST;
				else prox<=WEST; end if;
			when NORTH=> 
				if h(SOUTH)='1' then prox<=SOUTH;
				elsif h(LOCAL)='1' then prox<=LOCAL;
				elsif h(EAST)='1' then prox<=EAST;
				elsif h(WEST)='1' then prox<=WEST;
				else prox<=NORTH; end if;
			when SOUTH=> 
				if h(LOCAL)='1' then prox<=LOCAL;
				elsif h(EAST)='1' then prox<=EAST;
				elsif h(WEST)='1' then prox<=WEST;
				elsif h(NORTH)='1' then prox<=NORTH;
				else prox<=SOUTH; end if;
		end case;
	end process;

	lx <= address((METADEFLIT - 1) downto QUARTOFLIT);
	ly <= address((QUARTOFLIT - 1) downto 0);

	tx <= header((METADEFLIT - 1) downto QUARTOFLIT);
	ty <= header((QUARTOFLIT - 1) downto 0);

	process(reset,clock)
	begin
		if reset='1' then
			ES<=S0;
		elsif clock'event and clock='1' then
			ES<=PES;
		end if;
	end process;

	------------------------------------------------------------------------------------------------------  
	-- PARTE COMBINACIONAL PARA DEFINIR O PRÓXIMO ESTADO DA MÁQUINA.
	--
	-- SO -> O estado S0 é o estado de inicialização da máquina. Este estado somente é 
	--       atingido quando o sinal reset é ativado.
	-- S1 -> O estado S1 é o estado de espera por requisição de chaveamento. Quando o 
	--       árbitro recebe uma ou mais requisições o sinal ask é ativado fazendo a 
	--       máquina avançar para o estado S2. 
	-- S2 -> No estado S2 a porta de entrada que solicitou chaveamento é selecionada. Se 
	--       houver mais de uma, aquela com maior prioridade é a selecionada.
	-- S3 -> No estado S3 é realizado algoritmo de chaveamento West First Non Minimal.
	-- S_LOCAL,S_EAST,S_WEST,S_NORTH e S_SOUTH -> Nestes estados é estabelecida a conexão
	--       da porta de entrada com a de saída através do preenchimento dos sinais mux_in
	--       e mux_out.
	-- S4 -> O estado S4 é necessário para que a porta selecionada para roteamento baixe
	--       o sinal h.
	--       
	process(ES,ask,h,lx,ly,tx,ty,auxfree,incoming)
	begin
		case ES is
			when S0 => PES <= S1;
			when S1 => if ask='1' then PES <= S2; else PES <= S1; end if;
			when S2 => PES <= S3;
			when S3 => if lx = tx and ly = ty and auxfree(LOCAL)='1' then PES<=S_LOCAL;
					elsif (lx /= tx or ly /= ty) and (incoming=LOCAL or incoming=EAST) then
						if lx > tx and auxfree(WEST)='1' then PES<=S_WEST;
						elsif incoming=LOCAL and lx < tx and auxfree(EAST)='1' then PES<=S_EAST;
						elsif (lx = tx or lx < tx) and ly < ty and auxfree(NORTH)='1' then PES<=S_NORTH;
						elsif (lx = tx or lx < tx) and ly > ty and auxfree(SOUTH)='1' then PES<=S_SOUTH;
						elsif ly /= ty and lx > CONV_VECTOR(MIN_X) and auxfree(WEST)='1' then PES<=S_WEST;
						elsif lx < tx and ly < CONV_VECTOR(MAX_Y) and auxfree(NORTH)='1' then PES<=S_NORTH;
						elsif lx < tx and ly > CONV_VECTOR(MIN_Y) and auxfree(SOUTH)='1' then PES<=S_SOUTH;
						else PES<= S1;
						end if;
					elsif (lx /= tx or ly /= ty) and incoming=WEST then
						if lx < tx and auxfree(EAST)='1' then PES<=S_EAST;
						elsif ly < ty and auxfree(NORTH)='1' then PES<=S_NORTH;
						elsif ly > ty and auxfree(SOUTH)='1' then PES<=S_SOUTH;
						elsif lx /= tx and ly > CONV_VECTOR(MIN_Y) and auxfree(SOUTH)='1' then PES<=S_SOUTH;
						elsif lx /= tx and ly < CONV_VECTOR(MAX_Y) and auxfree(NORTH)='1' then PES<=S_NORTH;
						else PES<= S1;
						end if;
					elsif (lx /= tx or ly /= ty) and incoming=NORTH then
						if ly > ty and auxfree(SOUTH)='1' then PES<=S_SOUTH;
						elsif lx < tx and auxfree(EAST)='1' then PES<=S_EAST;
						elsif lx /= tx and ly > CONV_VECTOR(MIN_Y) and auxfree(SOUTH)='1' then PES<=S_SOUTH;
						else PES <= S1;
						end if;
					elsif (lx /= tx or ly /= ty) and incoming=SOUTH then
						if ly < ty and auxfree(NORTH)='1' then PES<=S_NORTH;
						elsif lx < tx and auxfree(EAST)='1' then PES<=S_EAST;
						elsif lx /= tx and ly < CONV_VECTOR(MAX_Y) and auxfree(NORTH)='1' then PES<=S_NORTH;
						else PES <= S1;
						end if;
					else PES <= S1;
					end if;
			when S_LOCAL | S_EAST | S_WEST | S_NORTH | S_SOUTH => PES<=S4;
			when S4 => PES<=S1;
		end case;
		end process;

	------------------------------------------------------------------------------------------------------  
	-- executa as ações correspondente ao estado atual da máquina de estados
	------------------------------------------------------------------------------------------------------  
	process (clock)
	begin
		if clock'event and clock='1' then
			case ES is
				-- Zera variáveis
				when S0 =>
					sel <= 0;
					ack_h <= (others => '0');
					auxfree <= (others=> '1');
					sender_ant    <= (others=> '0');
					mux_out <= (others=>(others=>'0'));
					source <= (others=>(others=>'0'));
				-- Chegou um header
				when S1=>
					ack_h <= (others => '0');
				-- Seleciona quem tera direito a requisitar roteamento
				when S2=>
					sel <= prox;
				when S_LOCAL =>
					source(CONV_INTEGER(incoming)) <= CONV_VECTOR(LOCAL);
					mux_out(LOCAL) <= incoming;
					auxfree(LOCAL) <= '0';
					ack_h(sel)<='1';
				when S_EAST =>
					source(CONV_INTEGER(incoming)) <= CONV_VECTOR(EAST);
					mux_out(EAST) <= incoming;
					auxfree(EAST) <= '0'; 
					ack_h(sel)<='1';
				when S_WEST =>
					source(CONV_INTEGER(incoming)) <= CONV_VECTOR(WEST);
					mux_out(WEST) <= incoming;
					auxfree(WEST) <= '0'; 
					ack_h(sel)<='1';
				when S_NORTH =>
					source(CONV_INTEGER(incoming)) <= CONV_VECTOR(NORTH);
					mux_out(NORTH) <= incoming;
					auxfree(NORTH) <= '0'; 
					ack_h(sel)<='1';
				when S_SOUTH =>
					source(CONV_INTEGER(incoming)) <= CONV_VECTOR(SOUTH);
					mux_out(SOUTH) <= incoming;
					auxfree(SOUTH) <= '0'; 
					ack_h(sel)<='1';
				when others => ack_h(sel)<='0';
			end case;

			sender_ant(LOCAL) <= sender(LOCAL);
			sender_ant(EAST)  <= sender(EAST); 
			sender_ant(WEST)  <= sender(WEST); 
			sender_ant(NORTH) <= sender(NORTH);
			sender_ant(SOUTH) <= sender(SOUTH);

			if sender(LOCAL)='0' and  sender_ant(LOCAL)='1' then auxfree(CONV_INTEGER(source(LOCAL))) <='1'; end if;
			if sender(EAST) ='0' and  sender_ant(EAST)='1'  then auxfree(CONV_INTEGER(source(EAST)))  <='1'; end if;
			if sender(WEST) ='0' and  sender_ant(WEST)='1'  then auxfree(CONV_INTEGER(source(WEST)))  <='1'; end if;
			if sender(NORTH)='0' and  sender_ant(NORTH)='1' then auxfree(CONV_INTEGER(source(NORTH))) <='1'; end if;
			if sender(SOUTH)='0' and  sender_ant(SOUTH)='1' then auxfree(CONV_INTEGER(source(SOUTH))) <='1'; end if;

		end if;
	end process;

	mux_in <= source;
	free <= auxfree;

end AlgorithmWFNM;

-- Algoritmo North-Last Não Minimo
architecture AlgorithmNLNM of SwitchControl is

type state is (S0,S1,S2,S3,S4,S_LOCAL,S_EAST,S_WEST,S_SOUTH,S_NORTH);
signal ES, PES: state;

-- sinais do arbitro
signal ask: std_logic := '0';
signal sel,prox: integer range 0 to (NPORT-1) := 0;
signal incoming: reg3 := (others=>'0');
signal header : regflit := (others=>'0');

-- sinais do controle
signal lx,ly,tx,ty: regquartoflit := (others=>'0');
signal auxfree: regNport := (others=>'0');
signal source:  arrayNport_reg3 := (others=>(others=>'0'));
signal sender_ant: regNport := (others=>'0');

begin

	ask <= '1' when h(LOCAL)='1' or h(EAST)='1' or h(WEST)='1' or h(NORTH)='1' or h(SOUTH)='1' else '0';
	incoming <= CONV_VECTOR(sel);
	header <= data(CONV_INTEGER(incoming));

	process(sel,h)
	begin
		case sel is
			when LOCAL=> 
				if h(EAST)='1' then prox<=EAST;
				elsif h(WEST)='1' then  prox<=WEST;
				elsif h(NORTH)='1' then prox<=NORTH;
				elsif h(SOUTH)='1' then prox<=SOUTH;
				else prox<=LOCAL; end if;
			when EAST=> 
				if h(WEST)='1' then     prox<=WEST;
				elsif h(NORTH)='1' then prox<=NORTH;
				elsif h(SOUTH)='1' then prox<=SOUTH;
				elsif h(LOCAL)='1' then prox<=LOCAL;
				else prox<=EAST; end if;
			when WEST=> 
				if h(NORTH)='1' then prox<=NORTH;
				elsif h(SOUTH)='1' then prox<=SOUTH;
				elsif h(LOCAL)='1' then prox<=LOCAL;
				elsif h(EAST)='1' then prox<=EAST;
				else prox<=WEST; end if;
			when NORTH=> 
				if h(SOUTH)='1' then prox<=SOUTH;
				elsif h(LOCAL)='1' then prox<=LOCAL;
				elsif h(EAST)='1' then prox<=EAST;
				elsif h(WEST)='1' then prox<=WEST;
				else prox<=NORTH; end if;
			when SOUTH=> 
				if h(LOCAL)='1' then prox<=LOCAL;
				elsif h(EAST)='1' then prox<=EAST;
				elsif h(WEST)='1' then prox<=WEST;
				elsif h(NORTH)='1' then prox<=NORTH;
				else prox<=SOUTH; end if;
		end case;
	end process;

	lx <= address((METADEFLIT - 1) downto QUARTOFLIT);
	ly <= address((QUARTOFLIT - 1) downto 0);

	tx <= header((METADEFLIT - 1) downto QUARTOFLIT); 
	ty <= header((QUARTOFLIT - 1) downto 0);

	process(reset,clock)
	begin
		if reset='1' then
			ES<=S0;
		elsif clock'event and clock='1' then
			ES<=PES;
		end if;
	end process;

	------------------------------------------------------------------------------------------------------  
	-- PARTE COMBINACIONAL PARA DEFINIR O PRÓXIMO ESTADO DA MÁQUINA.
	--
	-- SO -> O estado S0 é o estado de inicialização da máquina. Este estado somente é 
	--       atingido quando o sinal reset é ativado.
	-- S1 -> O estado S1 é o estado de espera por requisição de chaveamento. Quando o 
	--       árbitro recebe uma ou mais requisições o sinal ask é ativado fazendo a 
	--       máquina avançar para o estado S2. 
	-- S2 -> No estado S2 a porta de entrada que solicitou chaveamento é selecionada. Se 
	--       houver mais de uma, aquela com maior prioridade é a selecionada.
	-- S3 -> No estado S3 é realizado algoritmo de chaveamento North Last Non Minimal.
	-- S_LOCAL,S_EAST,S_WEST,S_NORTH e S_SOUTH -> Nestes estados é estabelecida a conexão
	--       da porta de entrada com a de saída através do preenchimento dos sinais mux_in
	--       e mux_out.
	-- S4 -> O estado S4 é necessário para que a porta selecionada para roteamento baixe
	--       o sinal h.
	--       
	process(ES,ask,h,lx,ly,tx,ty,auxfree,incoming)
	begin
		case ES is
			when S0 => PES <= S1;
			when S1 => if ask='1' then PES <= S2; else PES <= S1; end if;
			when S2 => PES <= S3;
			when S3 => if lx = tx and ly = ty and auxfree(LOCAL)='1' then PES<=S_LOCAL;
						elsif (lx /= tx or ly /= ty) and incoming=EAST then
							if lx > tx and auxfree(WEST)='1' then PES<=S_WEST;
							elsif ly > ty and auxfree(SOUTH)='1' then PES<=S_SOUTH;
							elsif lx = tx and ly < ty and auxfree(NORTH)='1' then PES<=S_NORTH;
							elsif ly > ty and lx > CONV_VECTOR(MIN_X) and auxfree(WEST)='1' then PES<=S_WEST;
							elsif lx /= tx and ly > CONV_VECTOR(MIN_Y) and auxfree(SOUTH)='1' then PES<=S_SOUTH;
							else PES<= S1;
							end if;
						elsif (lx /= tx or ly /= ty) and incoming=WEST then
							if lx < tx and auxfree(EAST)='1' then PES<=S_EAST;
							elsif ly > ty and auxfree(SOUTH)='1' then PES<=S_SOUTH;
							elsif lx = tx and ly < ty and auxfree(NORTH)='1' then PES<=S_NORTH;
							elsif ly > ty and lx < CONV_VECTOR(MAX_X) and auxfree(EAST)='1' then PES<=S_EAST;
							elsif lx /= tx and ly > CONV_VECTOR(MIN_Y) and auxfree(SOUTH)='1' then PES<=S_SOUTH;
							else PES<= S1;
							end if;
						elsif (lx /= tx or ly /= ty) and (incoming=LOCAL or incoming=NORTH) then
							if ly > ty and auxfree(SOUTH)='1' then PES<=S_SOUTH;
							elsif lx < tx and auxfree(EAST)='1' then PES<=S_EAST;
							elsif lx > tx and auxfree(WEST)='1' then PES<=S_WEST;
							elsif lx = tx and ly < ty and auxfree(NORTH)='1' then PES<=S_NORTH;
							elsif lx /= tx and ly > CONV_VECTOR(MIN_Y) and auxfree(SOUTH)='1' then PES<=S_SOUTH;
							elsif ly > ty and lx < CONV_VECTOR(MAX_X) and auxfree(EAST)='1' then PES<=S_EAST;
							elsif ly > ty and lx > CONV_VECTOR(MIN_X) and auxfree(WEST)='1' then PES<=S_WEST;
							else PES<= S1;
							end if;
						elsif (lx /= tx or ly /= ty) and incoming=SOUTH then
							if ly < ty and auxfree(NORTH)='1' then PES<=S_NORTH;
							else PES <= S1;
							end if;
						else PES <= S1;
						end if;
			when S_LOCAL | S_EAST | S_WEST | S_NORTH | S_SOUTH => PES<=S4;
			when S4 => PES<=S1;
		end case;
		end process;

	------------------------------------------------------------------------------------------------------  
	-- executa as ações correspondente ao estado atual da máquina de estados
	------------------------------------------------------------------------------------------------------  
	process (clock)
	begin
		if clock'event and clock='1' then
			case ES is
				-- Zera variáveis
				when S0 =>
					sel <= 0;
					ack_h <= (others => '0');
					auxfree <= (others=> '1');
					sender_ant    <= (others=> '0');
					mux_out <= (others=>(others=>'0'));
					source <= (others=>(others=>'0'));
				-- Chegou um header
				when S1=>
					ack_h <= (others => '0');
				-- Seleciona quem tera direito a requisitar roteamento
				when S2=>
					sel <= prox;
				when S_LOCAL =>
					source(CONV_INTEGER(incoming)) <= CONV_VECTOR(LOCAL);
					mux_out(LOCAL) <= incoming;
					auxfree(LOCAL) <= '0';
					ack_h(sel)<='1';
				when S_EAST =>
					source(CONV_INTEGER(incoming)) <= CONV_VECTOR(EAST);
					mux_out(EAST) <= incoming;
					auxfree(EAST) <= '0'; 
					ack_h(sel)<='1';
				when S_WEST =>
					source(CONV_INTEGER(incoming)) <= CONV_VECTOR(WEST);
					mux_out(WEST) <= incoming;
					auxfree(WEST) <= '0'; 
					ack_h(sel)<='1';
				when S_NORTH =>
					source(CONV_INTEGER(incoming)) <= CONV_VECTOR(NORTH);
					mux_out(NORTH) <= incoming;
					auxfree(NORTH) <= '0'; 
					ack_h(sel)<='1';
				when S_SOUTH =>
					source(CONV_INTEGER(incoming)) <= CONV_VECTOR(SOUTH);
					mux_out(SOUTH) <= incoming;
					auxfree(SOUTH) <= '0'; 
					ack_h(sel)<='1';
				when others => ack_h(sel)<='0';
			end case;

			sender_ant(LOCAL) <= sender(LOCAL);
			sender_ant(EAST)  <= sender(EAST);
			sender_ant(WEST)  <= sender(WEST);
			sender_ant(NORTH) <= sender(NORTH);
			sender_ant(SOUTH) <= sender(SOUTH);

			if sender(LOCAL)='0' and  sender_ant(LOCAL)='1' then auxfree(CONV_INTEGER(source(LOCAL))) <='1'; end if;
			if sender(EAST) ='0' and  sender_ant(EAST)='1'  then auxfree(CONV_INTEGER(source(EAST)))  <='1'; end if;
			if sender(WEST) ='0' and  sender_ant(WEST)='1'  then auxfree(CONV_INTEGER(source(WEST)))  <='1'; end if;
			if sender(NORTH)='0' and  sender_ant(NORTH)='1' then auxfree(CONV_INTEGER(source(NORTH))) <='1'; end if;
			if sender(SOUTH)='0' and  sender_ant(SOUTH)='1' then auxfree(CONV_INTEGER(source(SOUTH))) <='1'; end if;

		end if;
	end process;

	mux_in <= source;
	free <= auxfree;

end AlgorithmNLNM;


-- Algoritmo Negative-First Não Minimo
architecture AlgorithmNFNM of SwitchControl is

type state is (S0,S1,S2,S3,S4,S_LOCAL,S_EAST,S_WEST,S_SOUTH,S_NORTH);
signal ES, PES: state;

-- sinais do arbitro
signal ask: std_logic := '0';
signal sel,prox: integer range 0 to (NPORT-1) := 0; 
signal incoming: reg3 := (others=>'0');
signal header : regflit := (others=>'0');

-- sinais do controle
signal lx,ly,tx,ty: regquartoflit := (others=>'0');
signal auxfree: regNport := (others=>'0');
signal source:  arrayNport_reg3 := (others=>(others=>'0'));
signal sender_ant: regNport := (others=>'0');

begin

	ask <= '1' when h(LOCAL)='1' or h(EAST)='1' or h(WEST)='1' or h(NORTH)='1' or h(SOUTH)='1' else '0';
	incoming <= CONV_VECTOR(sel);
	header <= data(CONV_INTEGER(incoming));

	process(sel,h)
	begin
		case sel is
			when LOCAL=> 
				if h(EAST)='1' then prox<=EAST;
				elsif h(WEST)='1' then  prox<=WEST;
				elsif h(NORTH)='1' then prox<=NORTH;
				elsif h(SOUTH)='1' then prox<=SOUTH;
				else prox<=LOCAL; end if;
			when EAST=> 
				if h(WEST)='1' then     prox<=WEST;
				elsif h(NORTH)='1' then prox<=NORTH;
				elsif h(SOUTH)='1' then prox<=SOUTH;
				elsif h(LOCAL)='1' then prox<=LOCAL;
				else prox<=EAST; end if;
			when WEST=> 
				if h(NORTH)='1' then prox<=NORTH;
				elsif h(SOUTH)='1' then prox<=SOUTH;
				elsif h(LOCAL)='1' then prox<=LOCAL;
				elsif h(EAST)='1' then prox<=EAST;
				else prox<=WEST; end if;
			when NORTH=> 
				if h(SOUTH)='1' then prox<=SOUTH;
				elsif h(LOCAL)='1' then prox<=LOCAL;
				elsif h(EAST)='1' then prox<=EAST;
				elsif h(WEST)='1' then prox<=WEST;
				else prox<=NORTH; end if;
			when SOUTH=> 
				if h(LOCAL)='1' then prox<=LOCAL;
				elsif h(EAST)='1' then prox<=EAST;
				elsif h(WEST)='1' then prox<=WEST;
				elsif h(NORTH)='1' then prox<=NORTH;
				else prox<=SOUTH; end if;
		end case;
	end process;

	lx <= address((METADEFLIT - 1) downto QUARTOFLIT);
	ly <= address((QUARTOFLIT - 1) downto 0);

	tx <= header((METADEFLIT - 1) downto QUARTOFLIT);
	ty <= header((QUARTOFLIT - 1) downto 0);

	process(reset,clock)
	begin
		if reset='1' then
			ES<=S0;
		elsif clock'event and clock='1' then
			ES<=PES;
		end if;
	end process;

	------------------------------------------------------------------------------------------------------  
	-- PARTE COMBINACIONAL PARA DEFINIR O PRÓXIMO ESTADO DA MÁQUINA.
	--
	-- SO -> O estado S0 é o estado de inicialização da máquina. Este estado somente é 
	--       atingido quando o sinal reset é ativado.
	-- S1 -> O estado S1 é o estado de espera por requisição de chaveamento. Quando o 
	--       árbitro recebe uma ou mais requisições o sinal ask é ativado fazendo a 
	--       máquina avançar para o estado S2. 
	-- S2 -> No estado S2 a porta de entrada que solicitou chaveamento é selecionada. Se 
	--       houver mais de uma, aquela com maior prioridade é a selecionada.
	-- S3 -> No estado S3 é realizado algoritmo de chaveamento North Last Non Minimal.
	-- S_LOCAL,S_EAST,S_WEST,S_NORTH e S_SOUTH -> Nestes estados é estabelecida a conexão
	--       da porta de entrada com a de saída através do preenchimento dos sinais mux_in
	--       e mux_out.
	-- S4 -> O estado S4 é necessário para que a porta selecionada para roteamento baixe
	--       o sinal h.
	--       
	process(ES,ask,h,lx,ly,tx,ty,auxfree,incoming)
	begin
		case ES is
			when S0 => PES <= S1;
			when S1 => if ask='1' then PES <= S2; else PES <= S1; end if;
			when S2 => PES <= S3;
			when S3 => if lx = tx and ly = ty and auxfree(LOCAL)='1' then PES<=S_LOCAL;
				elsif (lx /= tx or ly /= ty) and (incoming=LOCAL or incoming=EAST) then
					if lx > tx and auxfree(WEST)='1' then PES<=S_WEST;
					elsif ly > ty and auxfree(SOUTH)='1' then PES<=S_SOUTH;
					elsif ly < ty and (lx = tx or lx < tx) and auxfree(NORTH)='1' then PES<=S_NORTH;
					elsif incoming=LOCAL and lx < tx and (ly = ty or ly < ty) and auxfree(EAST)='1' then PES<=S_EAST;
					else PES<= S1;
					end if;
				elsif (lx /= tx or ly /= ty) and incoming=NORTH then
					if ly > ty and auxfree(SOUTH)='1' then PES<=S_SOUTH;
					elsif lx > tx and auxfree(WEST)='1' then PES<=S_WEST;
					elsif lx < tx and (ly = ty or ly < ty) and auxfree(EAST)='1' then PES<=S_EAST;
					else PES<= S1;
					end if;
				elsif (lx /= tx or ly /= ty) and incoming=WEST then
					if lx < tx and auxfree(EAST)='1' then PES<=S_EAST;
					elsif ly < ty and auxfree(NORTH)='1' then PES<=S_NORTH;
					else PES<= S1;
					end if;
				elsif (lx /= tx or ly /= ty) and incoming=SOUTH then
					if ly < ty and auxfree(NORTH)='1' then PES<=S_NORTH;
					elsif lx < tx and auxfree(EAST)='1' then PES<=S_EAST;
					else PES <= S1;
					end if;
				else PES <= S1;
				end if;
			when S_LOCAL | S_EAST | S_WEST | S_NORTH | S_SOUTH => PES<=S4;
			when S4 => PES<=S1;
		end case;
		end process;

	------------------------------------------------------------------------------------------------------  
	-- executa as ações correspondente ao estado atual da máquina de estados
	------------------------------------------------------------------------------------------------------  
	process (clock)
	begin
		if clock'event and clock='1' then
			case ES is
				-- Zera variáveis
				when S0 =>
					sel <= 0;
					ack_h <= (others => '0');
					auxfree <= (others=> '1');
					sender_ant    <= (others=> '0');
					mux_out <= (others=>(others=>'0'));
					source <= (others=>(others=>'0'));
				-- Chegou um header
				when S1=>
					ack_h <= (others => '0');
				-- Seleciona quem tera direito a requisitar roteamento
				when S2=>
					sel <= prox;
				when S_LOCAL =>
					source(CONV_INTEGER(incoming)) <= CONV_VECTOR(LOCAL);
					mux_out(LOCAL) <= incoming;
					auxfree(LOCAL) <= '0';
					ack_h(sel)<='1';
				when S_EAST =>
					source(CONV_INTEGER(incoming)) <= CONV_VECTOR(EAST);
					mux_out(EAST) <= incoming;
					auxfree(EAST) <= '0'; 
					ack_h(sel)<='1';
				when S_WEST =>
					source(CONV_INTEGER(incoming)) <= CONV_VECTOR(WEST);
					mux_out(WEST) <= incoming;
					auxfree(WEST) <= '0'; 
					ack_h(sel)<='1';
				when S_NORTH =>
					source(CONV_INTEGER(incoming)) <= CONV_VECTOR(NORTH);
					mux_out(NORTH) <= incoming;
					auxfree(NORTH) <= '0'; 
					ack_h(sel)<='1';
				when S_SOUTH =>
					source(CONV_INTEGER(incoming)) <= CONV_VECTOR(SOUTH);
					mux_out(SOUTH) <= incoming;
					auxfree(SOUTH) <= '0'; 
					ack_h(sel)<='1';
				when others => ack_h(sel)<='0';
			end case;

			sender_ant(LOCAL) <= sender(LOCAL);
			sender_ant(EAST)  <= sender(EAST); 
			sender_ant(WEST)  <= sender(WEST); 
			sender_ant(NORTH) <= sender(NORTH);
			sender_ant(SOUTH) <= sender(SOUTH);

			if sender(LOCAL)='0' and  sender_ant(LOCAL)='1' then auxfree(CONV_INTEGER(source(LOCAL))) <='1'; end if;
			if sender(EAST) ='0' and  sender_ant(EAST)='1'  then auxfree(CONV_INTEGER(source(EAST)))  <='1'; end if;
			if sender(WEST) ='0' and  sender_ant(WEST)='1'  then auxfree(CONV_INTEGER(source(WEST)))  <='1'; end if;
			if sender(NORTH)='0' and  sender_ant(NORTH)='1' then auxfree(CONV_INTEGER(source(NORTH))) <='1'; end if;
			if sender(SOUTH)='0' and  sender_ant(SOUTH)='1' then auxfree(CONV_INTEGER(source(SOUTH))) <='1'; end if;

		end if;
	end process;

	mux_in <= source;
	free <= auxfree;

end AlgorithmNFNM;