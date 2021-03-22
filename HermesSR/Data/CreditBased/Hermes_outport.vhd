---------------------------------------------------------------------------------------
--                                      
--				--------------
--        	     CLOCK->	|                 |	->  CLOCK_TX
--	             RESET->	|                 |	->  TX	
--        	      	 RR-> 	|                 |	->  DATA_OUT	
--           	ACK_RR<-	|                 |	<-  CREDIT_I
--             			|                 |	
--        	DATA_AV->	|                 |	
--               	DATA->	|                 |
--     	DATA_ACK<-	|                 |
--     		          EOP->	|                 |
--                   	     	--------------
--
---------------------------------------------------------------------------------------
library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_arith.all;
use IEEE.std_logic_unsigned.all;
use work.HermesPackage.all;

-- interface da Hermes_outport
entity Hermes_outport is
port(
	clock:    in  std_logic;
	reset:    in  std_logic;
	
	rr:       in  regNport;
	ack_rr:   out regNport;
	
	data_av:  in  regNport;
	data:     in  arrayNport_regflit;
	data_ack: out regNport;
	EOP:  	  in  regNport;
	
	clock_tx: out std_logic;
	tx:       out std_logic;
	data_out: out regflit;
	credit_i: in  std_logic);
end Hermes_outport;

architecture Hermes_outport of Hermes_outport is

type fila_out is (S_INIT, S_FOWARD, S_EOP);
signal EA : fila_out;

signal buf: out_buff := (others=>(others=>'0'));
signal first,last: pointer := (others=>'0');
signal link_port: regIDport :=(others=>'1');
signal sel,prox: integer range 0 to (NPORT-1) := 0;

begin

	-- CONTROLA A ORDEM DE RECEPÇÃO DAS REQUISICOES
	-------------------------------------------------------------------------------------------
	process(sel,rr)
	begin
		case sel is
			when LOCAL=>
				if rr(EAST)='1' then prox<=EAST;
				elsif rr(WEST)='1' then  prox<=WEST;
				elsif rr(NORTH)='1' then prox<=NORTH;
				elsif rr(SOUTH)='1' then prox<=SOUTH;
				else prox<=LOCAL; end if;
			when EAST=>
				if rr(WEST)='1' then prox<=WEST;
				elsif rr(NORTH)='1' then prox<=NORTH;
				elsif rr(SOUTH)='1' then prox<=SOUTH;
				elsif rr(LOCAL)='1' then prox<=LOCAL;
				else prox<=EAST; end if;
			when WEST=>
				if rr(NORTH)='1' then prox<=NORTH;
				elsif rr(SOUTH)='1' then prox<=SOUTH;
				elsif rr(LOCAL)='1' then prox<=LOCAL;
				elsif rr(EAST)='1' then prox<=EAST;
				else prox<=WEST; end if;
			when NORTH=>
				if rr(SOUTH)='1' then prox<=SOUTH;
				elsif rr(LOCAL)='1' then prox<=LOCAL;
				elsif rr(EAST)='1' then prox<=EAST;
				elsif rr(WEST)='1' then prox<=WEST;
				else prox<=NORTH; end if;
			when SOUTH=>
				if rr(LOCAL)='1' then prox<=LOCAL;
				elsif rr(EAST)='1' then prox<=EAST;
				elsif rr(WEST)='1' then prox<=WEST;
				elsif rr(NORTH)='1' then prox<=NORTH;
				else prox<=SOUTH; end if;
		end case;
	end process;

	-- CONTROLA O ARMAZENAMENTO DE REQUISICOES DE ROTEAMENTO
	-------------------------------------------------------------------------------------------
	process(reset, clock)
	begin
		if reset='1' then
			last <= (others=>'0');
			sel  <= EAST;
			ack_rr<=(others=>'0');
		elsif clock'event and clock='0' then
			ack_rr<=(others=>'0');
			if (rr(sel)/='0') then
				buf(CONV_INTEGER(last)) <= CONV_STD_LOGIC_VECTOR(sel,PORT_DEF);
				ack_rr(sel)<='1';
				if(last = NPORT - 1) then 
					last <= (others=>'0');
				else
					last <= last + 1;
				end if;
			end if;
			sel<=prox;
		end if;
	end process;

	-- CONTROLE DO PACOTE E DO CONSUMO DOS DADOS DA FILA
	-------------------------------------------------------------------------------------------
	process(reset, clock)
		variable v_i_target_port : integer range 0 to 31;
		variable v_r_lnk_port : regIDport;
		variable v_r_aux_port : regIDport;
		variable v_l_descarta_flit : std_logic;
	begin
		if reset='1' then
			first <= (others=>'0');
			link_port<=(others=>'1');
			EA <= S_INIT;
		elsif clock'event and clock='1' then
			case EA is
				-- Estado inicial de aguarde de chegada de pacote
				when S_INIT =>					
					EA<= S_INIT;
					-- Se ha pacotes esperando para ser encaminhado na fila...
					if first /= last then
						link_port<=(buf(CONV_INTEGER(first)));
						
						if (first = NPORT-1) then first <= (others=>'0');
						else first <= first+1;
						end if;
						
						EA <= S_FOWARD;
					end if;
					
					
				-- Estado de pedido de roteamento
				when S_FOWARD =>
					EA <= S_FOWARD;
					if EOP(CONV_INTEGER(link_port))='1' then						
						EA <= S_EOP;
					end if;
				-- estado de envio de flit header
				when S_EOP =>
					EA <= S_EOP;
					if EOP(CONV_INTEGER(link_port))='0' and first = last then
						EA<= S_INIT;
					elsif EOP(CONV_INTEGER(link_port))='0' and first /= last then
							EA <= S_FOWARD;

							link_port<=(buf(CONV_INTEGER(first)));							
							
							if (first = NPORT-1) then first <= (others=>'0');
							else first <= first+1;
							end if;							

					end if;
			end case;
		end if;
	end process;

	-- CONTROLE DE RESPOSTA DE CAPTURA DO DADO
	-------------------------------------------------------------------------------------------
	process(EA, link_port, credit_i)
	begin		
		data_ack <= (others =>'0');
		if(EA/=S_INIT) then
			data_ack(CONV_INTEGER(link_port)) <= credit_i;
		end if;
	end process;

	data_out <= (others => '0') when (EA=S_INIT) else data(CONV_INTEGER(link_port));
	tx       <=            '0'  when (EA=S_INIT) else data_av(CONV_INTEGER(link_port));
	clock_tx <= clock;

end Hermes_outport;