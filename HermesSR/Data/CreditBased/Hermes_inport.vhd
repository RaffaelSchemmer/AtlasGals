---------------------------------------------------------------------------------------
--				BUFFER
--				--------------
-- 		     CLOCK->	|                 |
-- 		     RESET->	|                 |-> RR
--				|                 |<- ACK_RR
--	                      RX ->	|                 |
--                DATA_IN ->	|                 |
--                CLOCK_RX ->	|                 |-> DATA_AV
--	        CREDIT_O <-	|                 |-> DATA
--                       		|                 |<- DATA_ACK
--                            		|                 |->EOP
--				--------------
--
---------------------------------------------------------------------------------------
library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_arith.all;
use IEEE.std_logic_unsigned.all;
use work.HermesPackage.all;

-- interface da Hermes_inport
entity Hermes_inport is
port(
	clock:    in  std_logic;
	reset:    in  std_logic;
	
	clock_rx: in  std_logic;
	rx:       in  std_logic;
	data_in:  in  regflit;
	credit_o: out std_logic;
	
	rr:       out regNport;
	ack_rr:   in  std_logic;
	
	data_av:  out std_logic;
	data:     out regflit;
	data_ack: in  std_logic;
	EOP:      out std_logic);
end Hermes_inport;

architecture Hermes_inport of Hermes_inport is

type fila_out is (S_INIT, S_ROUTING, S_HEADER, S_PACKET);
signal EA : fila_out;

signal buf: buff := (others=>(others=>'0'));
signal first,last: pointer := (others=>'0');
signal tem_espaco, shifta: std_logic := '0';
signal counter_flit: regflit := (others=>'0');
signal currentFlit: regflit := (others=>'0');
--alias currentFlit: regflit is buf(CONV_INTEGER(first));
alias nibble1: regIDport is currentFlit(currentFlit'length-1 downto currentFlit'length-4);
alias nibble2: regIDport is currentFlit(currentFlit'length-5 downto currentFlit'length-8);
alias shifted_flit: std_logic_vector(TAM_FLIT-1 downto PORT_DEF) is currentFlit(currentFlit'length-5 downto 0);

signal target_port: regIDport :=(others=>'1');

procedure inc_first(signal l_first: inout pointer) is
begin
	if(l_first = TAM_BUFFER -1) then l_first <= (others=>'0');
	else l_first <= l_first+1;
	end if;
end procedure inc_first;

begin

	-- CONTROLE DO ESTADO DA PREENCHIMENTO DA FILA (SE ESTA CHEIA OU NAO)
	-------------------------------------------------------------------------------------------
	-- Verifica se existe espaço na fila para armazenamento de flits.
	-- Se existe espaço na fila o sinal tem_espaco_na_fila é igual 1.
	process(reset, clock_rx)
	begin
		if reset='1' then
			tem_espaco <= '1';
		elsif clock_rx'event and clock_rx='1' then
			if not((first=x"0" and last=TAM_BUFFER - 1) or (first=last+1)) then
				tem_espaco <= '1';
			else
				tem_espaco <= '0';
			end if;
		end if;
	end process;
	credit_o <= tem_espaco;

	-- CONTROLE DE PONTEIRO PARA ESCRITA NA FILA
	-- O ponteiro last é inicializado com o valor zero quando o reset é ativado.
	-- Quando o sinal rx é ativado indicando que existe um flit na porta de entrada é
	-- verificado se existe espaço na fila para armazená-lo. Se existir espaço na fila o
	-- flit recebido é armazenado na posição apontada pelo ponteiro last e o mesmo é
	-- incrementado. Quando last atingir o tamanho da fila, ele recebe zero.
	process(reset, clock_rx)
	begin
		if reset='1' then
			last <= (others=>'0');
		elsif clock_rx'event and clock_rx='0' then
			if tem_espaco='1' and rx='1' then
				buf(CONV_INTEGER(last)) <= data_in;
				--incrementa o last
				if(last = TAM_BUFFER - 1) then last <= (others=>'0');
				else last <= last + 1;
				end if;
			end if;
		end if;
	end process;	

	process(reset, clock)
		variable v_i_target_port : regIDport;
	begin
		if reset='1' then
			counter_flit <= (others=>'0');
			shifta <= '0';
			target_port <= (others=>'1');
			EA <= S_INIT;
		elsif clock'event and clock='1' then
			case EA is
				-- Estado inicial de aguarde de chegada de pacote
				when S_INIT =>
				
					counter_flit <= (others=>'0');
					shifta <= '0';
					target_port <= (others=>'1');

					EA<= S_INIT;

					--Abordagem 1
					-- if first /= last then
						-- if(CONV_INTEGER(nibble1)=INVALID_PORT)then							
							-- v_i_target_port:=CONV_STD_LOGIC_VECTOR(LOCAL,PORT_DEF);
						-- else
							-- v_i_target_port:=nibble1;
							-- if(CONV_INTEGER(nibble2)=INVALID_PORT)then -- (2)								
								-- if(first = TAM_BUFFER -1) then first <= (others=>'0');
								-- else first <= first+1;
								-- end if;
							-- else -- (3)								
								-- shifta <= '1';
							-- end if;
						-- end if;
						-- target_port<=v_i_target_port;
						-- EA <= S_ROUTING;
					-- end if;

					--Abordagem 2
					-- if (first/=last) and (CONV_INTEGER(nibble1)=INVALID_PORT)then
						-- target_port<=CONV_STD_LOGIC_VECTOR(LOCAL,PORT_DEF);
						-- EA <= S_ROUTING;
					-- elsif(first/=last)then
						-- target_port<=nibble1;
						-- if(CONV_INTEGER(nibble2)=INVALID_PORT)then
							-- if(first = TAM_BUFFER -1) then first <= (others=>'0');
							-- else first <= first+1;
							-- end if;
						-- else
							-- shifta <= '1';
						-- end if;
						-- EA <= S_ROUTING;
					-- end if;

					--Abordagem 3
					-- if (first/=last) and (CONV_INTEGER(nibble1)=INVALID_PORT)then
						-- target_port<=CONV_STD_LOGIC_VECTOR(LOCAL,PORT_DEF);
						-- EA <= S_ROUTING;
					-- elsif(first/=last)then
						-- target_port<=nibble1;
						-- if(CONV_INTEGER(nibble2)=INVALID_PORT) and (first = TAM_BUFFER -1) then first <= (others=>'0');
						-- elsif(CONV_INTEGER(nibble2)=INVALID_PORT)then first <= first+1;
						-- else shifta <= '1';
						-- end if;
						-- EA <= S_ROUTING;
					-- end if;

					--Abordagem 4
					-- if (first/=last) and (CONV_INTEGER(nibble1)=INVALID_PORT)then
						-- target_port<=CONV_STD_LOGIC_VECTOR(LOCAL,PORT_DEF);
						-- EA <= S_ROUTING;
					-- elsif(first/=last) and (CONV_INTEGER(nibble2)=INVALID_PORT) and (first = TAM_BUFFER -1)then
						-- target_port<=nibble1;
						-- first <= (others=>'0');
						-- EA <= S_ROUTING;
					-- elsif(first/=last) and (CONV_INTEGER(nibble2)=INVALID_PORT)then
						-- target_port<=nibble1;
						-- first <= first+1;
						-- EA <= S_ROUTING;
					-- elsif(first/=last)then
						-- target_port<=nibble1;
						-- shifta <= '1';
						-- EA <= S_ROUTING;
					-- end if;

					--Abordagem 5
					if (first/=last)then
						EA<=S_ROUTING;
					end if;
					
					if (first/=last) and (CONV_INTEGER(nibble1)=INVALID_PORT)then
						target_port<=CONV_STD_LOGIC_VECTOR(LOCAL,PORT_DEF);
					elsif(first/=last) and (CONV_INTEGER(nibble2)=INVALID_PORT) and (first = TAM_BUFFER -1)then
						target_port<=nibble1;
						first <= (others=>'0');
					elsif(first/=last) and (CONV_INTEGER(nibble2)=INVALID_PORT)then
						target_port<=nibble1;
						first <= first+1;
					elsif(first/=last)then
						target_port<=nibble1;
						shifta <= '1';
					end if;
					
					--Abordagem 6
					-- if (first/=last)then
						-- EA<=S_ROUTING;
					-- end if;
					
					-- if (first/=last) and (CONV_INTEGER(nibble1)=INVALID_PORT)then
						-- target_port<=CONV_STD_LOGIC_VECTOR(LOCAL,PORT_DEF);
					-- elsif (first/=last)then
						-- target_port<=nibble1;
					-- end if;
					
					-- if(first/=last) and (CONV_INTEGER(nibble1)/=INVALID_PORT)and (CONV_INTEGER(nibble2)=INVALID_PORT) and (first = TAM_BUFFER -1)then
						-- first <= (others=>'0');
					-- elsif(first/=last) and (CONV_INTEGER(nibble1)/=INVALID_PORT) and (CONV_INTEGER(nibble2)=INVALID_PORT) then
						-- first <= first+1;
					-- elsif(first/=last) and (CONV_INTEGER(nibble1)/=INVALID_PORT) then
						-- shifta <= '1';
					-- end if;
					
					--Abordagem 7
					-- if (first/=last)then
						-- EA<=S_ROUTING;
					-- end if;
					
					-- if (first/=last) and (CONV_INTEGER(nibble1)=INVALID_PORT)then
						-- target_port<=CONV_STD_LOGIC_VECTOR(LOCAL,PORT_DEF);
					-- elsif (first/=last)then
						-- target_port<=nibble1;
					-- end if;
					
					-- if(first/=last) and (CONV_INTEGER(nibble1)/=INVALID_PORT) and (CONV_INTEGER(nibble2)=INVALID_PORT)then
						-- if(first = TAM_BUFFER -1) then first <= (others=>'0');
						-- else first <= first+1;
						-- end if;
					-- elsif(first/=last) and (CONV_INTEGER(nibble1)/=INVALID_PORT) then
						-- shifta <= '1';
					-- end if;
					
				-- Estado de pedido de roteamento
				when S_ROUTING =>
					EA <= S_ROUTING;
					if ack_rr='1' then
						EA <= S_HEADER;
					end if;
				-- estado de envio de flit header
				when S_HEADER  =>
					EA <= S_HEADER;

					-- Abordagem 1
					-- if(data_ack='1') then
						-- shifta<='0';
					-- end if;
					
					-- if((data_ack='1') and (CONV_INTEGER(nibble1)=INVALID_PORT))then
						-- EA <= S_PACKET;
					-- end if;

					-- if((data_ack='1') and (first=TAM_BUFFER-1))then
						-- first<=(others=>'0');
					-- elsif(data_ack='1')then
						-- first<=first+1;
					-- end if;

					-- Abordagem 2
					if(data_ack='1') and  (CONV_INTEGER(nibble1)=INVALID_PORT) then
						shifta<='0';
						EA <= S_PACKET;
					elsif (data_ack='1') then
						shifta<='0';
					end if;

					if((data_ack='1') and (first=TAM_BUFFER-1))then
						first<=(others=>'0');
					elsif(data_ack='1')then
						first<=first+1;
					end if;

					-- Abordagem 3
					-- if(data_ack='1')then
						-- shifta<='0';
					-- end if;
					
					-- if(data_ack='1') and (CONV_INTEGER(nibble1)=INVALID_PORT) and (first=TAM_BUFFER-1) then
						-- first<=(others=>'0');
						-- EA <= S_PACKET;
					-- elsif(data_ack='1') and (CONV_INTEGER(nibble1)=INVALID_PORT) then
						-- first<=first+1;
						-- EA <= S_PACKET;
					-- elsif (data_ack='1') and (first=TAM_BUFFER-1) then
						-- first<=(others=>'0');
					-- elsif(data_ack='1')then
						-- first<=first+1;
					-- end if;

				-- estado de controle de envio de corpo do pacote
					
				when S_PACKET =>
					EA <= S_PACKET;
					--ABORDAGEM 1
					-- if((data_ack='1') and (counter_flit=x"0"))then
						-- counter_flit <= currentFlit;
					-- elsif((data_ack='1') and (counter_flit/=x"1"))then
						-- counter_flit <= counter_flit-1;
					-- end if;
					
					-- if((data_ack='1') and (first=TAM_BUFFER-1))then
						-- first<=(others=>'0');
					-- elsif(data_ack='1')then
						-- first<=first+1;
					-- end if;

					-- if((data_ack='1') and (counter_flit=x"1"))then
						-- EA <= S_INIT;						
					-- end if;

					--ABORDAGEM 2
					if((data_ack='1') and (counter_flit=x"0") and (first=TAM_BUFFER-1))then
						counter_flit <= currentFlit;
						first<=(others=>'0');
						
					elsif((data_ack='1') and (counter_flit=x"0"))then
						counter_flit <= currentFlit;
						first<=first+1;
						
					elsif((data_ack='1') and (counter_flit/=x"1") and (first=TAM_BUFFER-1))then
						counter_flit <= counter_flit - 1;
						first<=(others=>'0');

					elsif((data_ack='1') and (counter_flit/=x"1"))then
						counter_flit <= counter_flit - 1;
						first<=first+1;

					elsif((data_ack='1') and (counter_flit=x"1") and (first=TAM_BUFFER-1))then
						counter_flit<=(others=>'0');
						first<=(others=>'0');
						EA <= S_INIT;

					elsif((data_ack='1') and (counter_flit=x"1"))then
						counter_flit<=(others=>'0');
						first<=first+1;
						EA <= S_INIT;
						
					end if;

					--ABORDAGEM 3 - CONSOME 3 REGISTRADORES A MAIS DO QUE A ABORDAGEM 2
					-- if((data_ack='1') and (counter_flit=x"0"))then
						-- counter_flit <= currentFlit;
						-- if(first=TAM_BUFFER-1) then first<=(others=>'0');
						-- else first<=first+1;
						-- end if;

					-- elsif((data_ack='1') and (counter_flit/=x"1"))then
						-- counter_flit <= counter_flit - 1;
						-- if(first=TAM_BUFFER-1) then first<=(others=>'0');
						-- else first<=first+1;
						-- end if;

					-- elsif((data_ack='1') and (counter_flit=x"1") and (first=TAM_BUFFER-1))then
						-- counter_flit<=(others=>'0');
						-- if(first=TAM_BUFFER-1) then first<=(others=>'0');
						-- else first<=first+1;
						-- end if;
						-- EA <= S_INIT;
						
					-- end if;
					
			end case;
		end if;
	end process;
	
	data<=(others=>'0')                 when (target_port=INVALID_PORT) else
				(shifted_flit & INVALID_PORT) when (shifta='1') else
				(currentFlit);
	
	EOP <= '1' when counter_flit=x"1" else '0';
	
	data_av <= '1' when (first/=last) and (EA=S_HEADER or EA=S_PACKET) else '0';	
	
	currentFlit <= buf(CONV_INTEGER(first));	
	
	process(EA, target_port)
	begin
		rr <= (others=>'0');
		if(EA=S_ROUTING)then
			rr(CONV_INTEGER(target_port)) <= '1';
		end if;
	end process;	

end Hermes_inport;