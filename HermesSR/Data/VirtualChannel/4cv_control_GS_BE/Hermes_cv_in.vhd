library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_arith.all;
use IEEE.std_logic_unsigned.all;
use work.Hermes_Package.all;

-- interface da Hermes_inport
entity Hermes_cv_in is
generic (buf_size: integer := TAM_BUFFER);
port(
	-- Sinais de sincronismo com o restante do sistema
	clock:    in  std_logic;
	reset:    in  std_logic;
	
	-- Sinais de sincronismo para recepção de pacotes
	clock_rx: in  std_logic;
	rx:       in  std_logic;
	data_in:  in  regflit;
	credit_o: out std_logic;
	
	-- Sinais de requisicao de roteamento
	rr:       out regNport;
	ack_rr:   in  std_logic;
	
	-- Sinais de sincronismo para a transmissao de pacotes
	data_av:  out regNport;
	data:     out regflit;
	data_ack: in  std_logic;
	eop:      out regNport
	);
end Hermes_cv_in;

architecture Hermes_cv_in of Hermes_cv_in is

type fila_out is (S_INIT, S_ROUTING, S_WAIT_HEADER, S_TRANSFORMED_HEADER_FLIT, S_HEADER, S_WAIT_PACKET, S_PACKET, S_WAIT_EOP, S_EOP);
signal EA : fila_out;

type buff is array(0 to buf_size-1) of regflit;
signal buf: buff := (others=>(others=>'0'));
signal first,last: pointer := (others=>'0');
signal tem_espaco, discarding, size_flit: std_logic := '0';
signal counter_flit: regflit := (others=>'0');
signal transfer_flit: regflit := (others=>'0');
signal target_port: regIDport :=(others=>'0');
signal transmitting: std_logic;


begin
	-- Processo de controle de ocupacao da FIFO
	-- O controle da disponibilidade de espaco eh realizado na borda de subida
	process(reset, clock_rx)
	begin
		if reset='1' then
			tem_espaco <= '1';
		elsif (clock_rx'event and clock_rx='1') then
			if not((first=x"0" and last=TAM_BUFFER - 1) or (first=last+1)) then
				tem_espaco <= '1';
			else
				tem_espaco <= '0';
			end if;
		end if;
	end process;
	
	credit_o <= tem_espaco;

	-- Processo de controle de escrita na FIFO
	-- Os dados sao sempre armazenados na borda de descida
	process(reset, clock_rx)
	begin
		if reset='1' then
			last <= (others=>'0');
		elsif (clock_rx'event and clock_rx='0') then
			if tem_espaco='1' and rx='1' then
				buf(CONV_INTEGER(last)) <= data_in;
				if(last = TAM_BUFFER - 1) then last <= (others=>'0');
				else last <= last + 1;
				end if;
			end if;
		end if;
	end process;
	
	-- Processo de controle do pacote
	-- O caminhamento da FSM eh realizado na borda de subida
	process(reset, clock)
		variable v_i_target_port : integer range 0 to 31;
		variable v_r_id_port : regIDport;
		variable v_l_aux : std_logic;
		variable v_lv_counter_flit : regflit;
		variable v_lv_flit : regflit;
		variable v_lv_index : pointer;
	begin
		if reset='1' then
			transfer_flit <= (others=>'0');
			counter_flit <= (others=>'0');
			rr <= (others=>'0');
			size_flit <= '0';
			data_av <= (others=>'0');
			eop <=(others=>'0');
			first <= (others=>'0');
			EA <= S_INIT;
			transmitting<='0';
			
		elsif (clock'event and clock='1') then
			case EA is
				when S_INIT =>
					transmitting<='0';
					data_av <= (others=>'0');
					eop <=(others=>'0');
					size_flit<='1';
					discarding <= '0';
					rr<=(others=>'0');

					if (first/=last) then
						transmitting<='1';
						discarding<='0';

						-- se a identificacao da porta for de uma porta invalida, significa que este flit eh o terminador e que cheguei ao roteador destino						
						if(buf(CONV_INTEGER(first))=FLIT_TERMINATOR) then
							v_i_target_port := LOCAL;
							transfer_flit<=(others=>'1');
						else
							-- armazena a porta de destino
							v_i_target_port:=CONV_INTEGER(buf(CONV_INTEGER(first))((TAM_FLIT-1) downto (TAM_FLIT-PORT_DEF)));
							v_r_id_port:=buf(CONV_INTEGER(first))((TAM_FLIT-(PORT_DEF+1)) downto (TAM_FLIT-(PORT_DEF+PORT_DEF)));

							if((PORT_DEF=TAM_FLIT) or (v_r_id_port=INVALID_PORT))then

								discarding<='1';
								transfer_flit<=buf(CONV_INTEGER(first));

							else
								discarding<='0';
								transfer_flit<=(buf(CONV_INTEGER(first))((TAM_FLIT-(PORT_DEF+1)) downto 0)) & INVALID_PORT;
							end if;
						end if;
						
						-- armazena o flit de header
						target_port<=CONV_STD_LOGIC_VECTOR(v_i_target_port,PORT_DEF);
						rr(v_i_target_port)<='1';						

						if (first=TAM_BUFFER-1) then first<=(others=>'0');
						else first<=first+1;
						end if;			

						EA <= S_ROUTING;
					else
						EA<= S_INIT;
					end if;
					
				when S_ROUTING =>
					if (ack_rr='1') then
						rr<=(others=>'0');
						
						if(discarding='1')then
							if(first=last)then
								EA<=S_WAIT_HEADER;
							else
								data_av(CONV_INTEGER(target_port))<='1';
								EA <= S_HEADER;
							end if;
						else
							data_av(CONV_INTEGER(target_port))<='1';
							EA <= S_TRANSFORMED_HEADER_FLIT;
						end if;
					end if;

				when S_WAIT_HEADER  =>
					if(first=last)then
						EA<=S_WAIT_HEADER;
					else
						data_av(CONV_INTEGER(target_port))<='1';
						EA<=S_HEADER;
					end if;
					
				when S_TRANSFORMED_HEADER_FLIT => 
					if (data_ack/='1')then
						EA <= S_TRANSFORMED_HEADER_FLIT;
					else						
						if(first=last)then							
							data_av(CONV_INTEGER(target_port))<='0';
							if(transfer_flit=FLIT_TERMINATOR)then
								EA<=S_WAIT_PACKET;
							else
								EA<=S_WAIT_HEADER;
							end if;
						else
							if(transfer_flit=FLIT_TERMINATOR)then
								EA<=S_PACKET;
							else
								EA<=S_HEADER;
							end if;
						end if;
					end if;				
				
				when S_HEADER  =>
				
					if(data_ack/='1')then
						EA <= S_HEADER;
					else

						if (first = TAM_BUFFER -1) then
							if last /= 0 then v_l_aux:='1';								
							else v_l_aux:='0';
							end if;
							first <= (others=>'0');
						else
							if first+1 /= last then v_l_aux:='1';
							else v_l_aux:='0';
							end if;
							first <= first+1;
						end if;
						data_av(CONV_INTEGER(target_port))<=v_l_aux;
					
						if(v_l_aux='1')then
							if(buf(CONV_INTEGER(first))=FLIT_TERMINATOR)then EA<=S_PACKET;
							else EA<=S_HEADER;
							end if;
						else
							if(buf(CONV_INTEGER(first))=FLIT_TERMINATOR)then EA<=S_WAIT_PACKET;
							else EA<=S_WAIT_HEADER;
							end if;
						end if;
					
					end if;

				when S_WAIT_PACKET =>
					if(first=last)then
						EA<=S_WAIT_PACKET;
					else
						data_av(CONV_INTEGER(target_port))<='1';
						EA<=S_PACKET;
					end if;

				when S_PACKET =>
					if(data_ack/='1')then
						EA <= S_PACKET;
					else

						if(size_flit='1') then
							v_lv_counter_flit:=buf(CONV_INTEGER(first));
							size_flit<='0';
						else
							v_lv_counter_flit:=counter_flit-1;
						end if;
						counter_flit<=v_lv_counter_flit;

						if (first = TAM_BUFFER -1) then
							if last /= 0 then v_l_aux:='1';								
							else v_l_aux:='0';
							end if;
							first <= (others=>'0');
						else
							if first+1 /= last then v_l_aux:='1';
							else v_l_aux:='0';
							end if;
							first <= first+1;
						end if;
						data_av(CONV_INTEGER(target_port))<=v_l_aux;
						
						if(v_l_aux='1')then
							if(v_lv_counter_flit=1)then
								eop(CONV_INTEGER(target_port))<='1';
								EA<=S_EOP;
							else
								EA<=S_PACKET;
							end if;
						else
							if(v_lv_counter_flit=1)then
								EA<=S_WAIT_EOP;
							else
								EA<=S_WAIT_PACKET;
							end if;							
						end if;
					end if;
					
				when S_WAIT_EOP =>
					if(first=last)then
						EA<=S_WAIT_EOP;
					else
						data_av(CONV_INTEGER(target_port))<='1';
						eop(CONV_INTEGER(target_port))<='1';
						EA<=S_EOP;
					end if;

				when S_EOP =>
					
					if(data_ack/='1')then
						EA <= S_EOP;
					else
						data_av<=(others=>'0');
						eop<=(others=>'0');
						
						if (first=TAM_BUFFER-1) then first<=(others=>'0');
						else first<=first+1;
						end if;			

						EA<=S_INIT;
					end if;
			end case;
		end if;
	end process;
	
	data <= transfer_flit when EA=S_TRANSFORMED_HEADER_FLIT else buf(CONV_INTEGER(first));
	
end Hermes_cv_in;