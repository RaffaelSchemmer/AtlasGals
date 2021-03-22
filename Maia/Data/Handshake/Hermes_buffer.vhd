---------------------------------------------------------------------------------------    
--                             FILa
--                        --------------
--                   RX ->|            |-> H
--              DaTa_IN ->|            |<- aCK_H
--               aCK_RX <-|            |
--                        |            |-> DaTa_aV
--                        |            |-> DaTa
--                        |            |<- DaTa_aCK
--                        |            |
--                        |            |   
--                        |            |=> SENDER
--                        |            |   all ports
--                        --------------
--
--  Quando o algoritmo de chaveamento resulta no bloqueio dos flits de um pacote, 
--  ocorre uma perda de desempenho em toda rede de interconexao, porque os flits sao 
--  bloqueados nao somente na chave atual, mas em todas as intermediarias. 
--  Para diminuir a perda de desempenho foi adicionada uma fila em cada porta de 
--  entrada da chave, reduzindo as chaves afetadas com o bloqueio dos flits de um 
--  pacote. E importante observar que quanto maior for o tamanho da fila menor sera o 
--  numero de chaves intermediarias afetadas. 
--  as filas usadas contem dimensao e largura de flit parametrizaveis, para altera-las
--  modifique as constantes TaM_BUFFER e TaM_FLIT no arquivo "Hermes_packet.vhd".
--  as filas funcionam como FIFOs circulares. Cada fila possui dois ponteiros: first e 
--  last. First aponta para a posicao da fila onde se encontra o flit a ser consumido. 
--  Last aponta para a posicao onde deve ser inserido o proximo flit.
---------------------------------------------------------------------------------------    
library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;
use work.HermesPackage.all;

-- Fila interface da
entity Fila is
port(
	clock:          in  std_logic;
	reset:          in  std_logic;
	data_in:        in  regflit;
	rx:             in  std_logic;
	ack_rx:         out std_logic;
	h:              out std_logic;
	ack_h:          in  std_logic;
	data_av:        out std_logic;
	data:           out regflit;
	data_ack:       in  std_logic;
	sender:         out std_logic);
end Fila;

architecture Fila of Fila is

type fila_out is (S_INIT, S_PaYLOaD, S_SENDHEaDER, S_HEaDER, S_END, S_END2);
signal Ea : fila_out;

signal buf: buff := (others=>(others=>'0'));
signal first,last: pointer := (others=>'0');
signal tem_espaco_na_fila : std_logic := '0';
signal counter_flit : regflit := (others=>'0');
signal auxack_rx : std_logic := '0';
begin 

	-------------------------------------------------------------------------------------------
	-- ENTRaDa DE DaDOS Na FILa
	-------------------------------------------------------------------------------------------

	-- Verifica se existe espaco na fila para armazenamento de flits.
	-- Se existe espaco na fila o sinal tem_espaco_na_fila eh igual 1.
	process(reset, clock)
	begin
		if reset='1' then
			tem_espaco_na_fila <= '1';  
		elsif clock'event and clock='0' then
			if not((first=x"0" and last=TaM_BUFFER - 1) or (first=last+1)) then
				tem_espaco_na_fila <= '1'; 
			else 
				tem_espaco_na_fila <= '0';
			end if;
		end if;
	end process;


	-- usado porque quando ack_rx ja esta em 1 ele deve ser mantido mesmo que a 
	-- tem_espaco_fila baixe
	auxack_rx <= '1' when (tem_espaco_na_fila='1' and rx='1') or (auxack_rx='1' and rx='1')
				else '0';
	ack_rx <= auxack_rx;

	-- O ponteiro last eh inicializado com o valor zero quando o reset eh ativado.
	-- Quando o sinal rx eh ativado indicando que existe um flit na porta de entrada eh 
	-- verificado se existe espaco na fila para armazena-lo. Se existir espaco na fila o 
	-- flit recebido eh armazenado na posicao apontada pelo ponteiro last e o mesmo eh 
	-- incrementado. Quando last atingir o tamanho da fila, ele recebe zero. 
	process(reset, clock)
	begin
		if reset='1' then
			last <= (others=>'0');  
		elsif clock'event and clock='1' then
			if tem_espaco_na_fila='1' and rx='1' then
				buf(CONV_INTEGER(last)) <= data_in;
				--incrementa o last
				if(last = TaM_BUFFER - 1) then last <= (others=>'0');
				else last <= last + 1;
				end if;
			end if;
		end if;
	end process;

	-------------------------------------------------------------------------------------------
	-- SaÍDa DE DaDOS Na FILa
	-------------------------------------------------------------------------------------------

	-- disponibiliza o dado para transmissao.
	data <= buf(CONV_INTEGER(first));

	-- Quando sinal reset eh ativado a maquina de estados avanca para o estado S_INIT.
	-- No estado S_INIT os sinais counter_flit (contador de flits do corpo do pacote), h (que 
	-- indica requisicao de chaveamento) e data_av (que indica a existencia de flit a ser 
	-- transmitido) sao inicializados com zero. Se existir algum flit na fila, ou seja, os 
	-- ponteiros first e last apontarem para posicões diferentes, a maquina de estados avanca
	-- para o estado S_HEaDER.
	-- No estado S_HEaDER e requisitado o chaveamento (h='1'), porque o flit na posicao 
	-- apontada pelo ponteiro first, quando a maquina encontra-se nesse estado, eh sempre o 
	-- header do pacote. a maquina permanece neste estado ateh que receba a confirmacao do 
	-- chaveamento (ack_h='1') entao o sinal h recebe o valor zero e a maquina avanca para 
	-- S_SENDHEaDER.
	-- Em S_SENDHEaDER eh indicado que existe um flit a ser transmitido (data_av='1'). a maquina de 
	-- estados permanece em S_SENDHEaDER ateh receber a confirmacao da transmissao (data_ack='1') 
	-- entao o ponteiro first aponta para o segundo flit do pacote e avanca para o estado S_PaYLOaD.
	-- No estado S_PaYLOaD eh indicado que existe um flit a ser transmitido (data_av='1') quando 
	-- eh recebida a confirmacao da transmissao (data_ack='1') eh verificado qual o valor do sinal
	-- counter_flit. Se counter_flit eh igual a um, a maquina avanca para o estado S_END. Caso
	-- counter_flit seja igual a zero, o sinal counter_flit eh inicializado com o valor do flit, pois
	-- este ao número de flits do corpo do pacote. Caso counter_flit seja diferente de um e de zero 
	-- o mesmo eh decrementado e a maquina de estados permanece em S_PaYLOaD enviando o próximo flit
	-- do pacote.
	-- Em S_END eh indicado que o último flit deve ser transmitido (data_av='1') quando eh recebida a
	-- confirmacao da transmissao (data_ack='1') a maquina retorna ao estado S_INIT.

	process(reset, clock)
	begin
		if reset='1' then
			counter_flit <= (others=>'0');
			h <= '0';
			data_av <= '0';
			sender <=  '0';
			first <= (others=>'0');
			Ea <= S_INIT;
		elsif clock'event and clock='1' then
			case Ea is                  
				when S_INIT =>  
					counter_flit <= (others=>'0');
					h<='0';                 
					data_av <= '0';
					if first /= last then        -- detectou dado na fila
						h<='1';             -- pede roteamento
						Ea <= S_HEaDER;
					else
						Ea<= S_INIT;
					end if;
				when S_HEaDER =>
					if ack_h='1' then
						Ea <= S_SENDHEaDER ;      -- depois de rotear envia o pacote 
						h<='0';
						data_av <= '1';
						sender <=  '1';
					else
						Ea <= S_HEaDER;
					end if;
				when S_SENDHEaDER  => 
					if data_ack='1' then        -- mantehm este dado ateh a resposta
						Ea <= S_PaYLOaD;
						data_av <= '0';   

						-- retira um dado do buffer
						if (first = TaM_BUFFER -1) then    first <= (others=>'0');
						else first <= first+1;
						end if;

					elsif first /= last  then
						data_av <= '1';
						Ea <= S_SENDHEaDER;
					else
						data_av <= '0';
						Ea <= S_SENDHEaDER;
					end if;

				when S_PaYLOaD =>
					if data_ack = '1' and counter_flit /= x"1" then -- confirmacao do envio de um dado que nao eh o tail

						-- se counter_flit eh zero indica recepcao do size do payload
						if counter_flit = x"0" then    counter_flit <=  buf(CONV_INTEGER(first)); 
						else counter_flit <= counter_flit - 1; 
						end if;

						-- retira um dado do buffer
						if (first = TaM_BUFFER -1) then    first <= (others=>'0');
						else first <= first+1;
						end if;

						data_av <= '0'; 
						Ea <= S_PaYLOaD;
						
					elsif data_ack = '1' and counter_flit = x"1" then -- confirmacao do envio do tail

						-- retira um dado do buffer
						if (first = TaM_BUFFER -1) then    first <= (others=>'0');
						else first <= first+1;
						end if;

						data_av <= '0'; 
						sender <=  '0';
						Ea <= S_END;

					elsif first /= last  then
						data_av <= '1';
						Ea <= S_PaYLOaD;
					else
						data_av <= '0';
						Ea <= S_PaYLOaD;
					end if;
				when S_END => 
					data_av <= '0';
					Ea <= S_END2;
				when S_END2 => -- estado necessario para permitir a liberacao da porta antes da solicitacao de novo envio
					data_av <= '0';
					Ea <= S_INIT;
			end case;
		end if;
	end process;

end Fila;
