---------------------------------------------------------------------------------------
--                            BUFFER
--                        --------------
--                   RX ->|            |-> H
--              DATA_IN ->|            |<- ACK_H
--             CLOCK_RX ->|            |
--             CREDIT_O <-|            |-> DATA_AV
--                        |            |-> DATA
--                        |            |<- DATA_ACK
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
--  As filas usadas contem dimensao e largura de flit parametrizaveis, para altera-las
--  modifique as constantes TAM_BUFFER e TAM_FLIT no arquivo "Hermes_packet.vhd".
--  As filas funcionam como FIFOs circulares. Cada fila possui dois ponteiros: first e 
--  last. First aponta para a posicao da fila onde se encontra o flit a ser consumido. 
--  Last aponta para a posicao onde deve ser inserido o proximo flit.
---------------------------------------------------------------------------------------
library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;
use work.HermesG_package.all;

-- interface da Hermes_buffer
entity HermesG_buffer is
port(
	clock:      in  std_logic;
	reset:      in  std_logic;
	clock_rx:   in  std_logic;
	rx:         in  std_logic;
	data_in:    in  regflit;
	credit_o:   out std_logic;
	h:          out std_logic;
	ack_h:      in  std_logic;
	data_av:    out std_logic;
	data:       out regflit;
		empty_buf:  out std_logic;

	data_ack:   in  std_logic;
	sender:     out std_logic);
end HermesG_buffer;


-- Versýo modificada da fila HERMES, capaz de operar de maneira conjunta com filas Async de codificaýýo Johnson e Gray
-- Para alguns cenýrios de teste esta fila nýo esta operacional

architecture Sync_Hermes_buffer of HermesG_buffer is

type fifo_out is (S_INIT, S_HEADER, S_SENDHEADER, S_PAYLOAD, S_END);
signal EA : fifo_out;

signal buf: buff := (others=>(others=>'0'));
signal read_pointer,write_pointer: pointer ;
signal counter_flit: regflit ;

signal data_available : std_logic;

begin

	-------------------------------------------------------------------------------------------
	-- INPUT FIFO
	-------------------------------------------------------------------------------------------
	process(reset, clock)
	begin
		if reset='1' then
			write_pointer <= (others => '0');
		elsif clock'event and clock='1' then
	-- If receiving data and fifo isn`t empty, record data on fifo and increase write pointer
			if rx = '1' and write_pointer /= read_pointer then
				buf(CONV_INTEGER(write_pointer)) <= data_in;
				write_pointer <= write_pointer + 1;
			end if;
		end if;
	end process;
	
	-- If fifo isn`t empty, credit is high. Else, low
	credit_o <= '1' when write_pointer /= read_pointer else '0';

	-------------------------------------------------------------------------------------------
	-- OUTPUT FIFO
	-------------------------------------------------------------------------------------------

	-- Available the data to transmition (asynchronous read).
	data <= buf(CONV_INTEGER(read_pointer));

	process(reset, clock)
	begin
		if reset='1' then
			counter_flit <= (others=>'0');
			h <= '0';
			data_available <= '0';
			sender <=  '0';
			-- Initialize the read pointer with one position before the write pointer
			read_pointer <= (others=>'1'); 
			EA <= S_INIT;
		elsif clock'event and clock='1' then
			case EA is
				when S_INIT =>
					counter_flit <= (others=>'0');
					h<='0';
					data_available <= '0';
					-- If fifo isn`t empty
					if (read_pointer + 1 /= write_pointer) then
						-- Routing request to Switch Control
						h<='1';
						-- Increase the read pointer position with a valid data
						read_pointer <= read_pointer + 1;
						EA <= S_HEADER;
					end if;

				when S_HEADER =>
					-- When the Switch Control confirm the routing
					if ack_h='1' then
						-- Disable the routing request 
						h       <= '0';
						-- Enable wrapper signal to packet transmition
						sender  <= '1';
						data_available <= '1'; 
						EA      <= S_SENDHEADER ;
					end if;

				when S_SENDHEADER  =>
					-- If the data available is read or was read 
					if data_ack = '1' or data_available = '0' then
						-- If fifo isn`t empty 
						if (read_pointer + 1 /= write_pointer) then
							data_available   <= '1';
							read_pointer   <= read_pointer + 1;
							EA <= S_PAYLOAD;
						-- If fifo is empty (protection clause)
						else
							data_available <= '0';
						end if;
					end if;

				when S_PAYLOAD =>
					-- If the data available is read or was read 
					if data_ack = '1' or data_available = '0' then
						-- If fifo isn`t empty or is tail
						if (read_pointer + 1 /= write_pointer) or counter_flit = x"1" then
							-- If the second flit, memorize the packet size
							if counter_flit = x"0"   then   
						                counter_flit <=  buf(CONV_INTEGER(read_pointer));
							elsif counter_flit /= x"1" then 
								counter_flit <=  counter_flit - 1;
							end if;
							-- If the tail flit
				                        if counter_flit = x"1" then
								-- If tail is send
								if data_ack = '1' then
									data_available <= '0';
									sender <= '0';
						                        EA <= S_INIT;
								else
									EA <= S_END;
								end if;
							-- Else read the next position
        	                			else
								data_available <= '1';
								read_pointer <= read_pointer + 1;
					                end if;
						-- If fifo is empty (protection clause)
						else
							data_available <= '0';
						end if;
					end if;
				when S_END =>
					-- When tail is send
					if data_ack = '1' then
						data_available <= '0';
						sender <= '0';
			                        EA <= S_INIT;
					end if;
			end case;
		end if;
	end process;
	
	data_av <= data_available;

	empty_buf <= '1' when read_pointer + 1 = write_pointer else '0';

end Sync_Hermes_buffer;

-- Fila sýncrona HERMES
-- ý utilizada pelo gerador quando somente um sinal de relýgio for usado durante a simulaýýo

architecture Hermes_buffer of HermesG_buffer is

type fifo_out is (S_INIT, S_HEADER, S_SENDHEADER, S_PAYLOAD, S_END);
signal EA : fifo_out;

signal buf: buff := (others=>(others=>'0'));
signal read_pointer,write_pointer: pointer ;
signal counter_flit: regflit ;

signal data_available : std_logic;

begin

	-------------------------------------------------------------------------------------------
	-- INPUT FIFO
	-------------------------------------------------------------------------------------------
	process(reset, clock)
	begin
		if reset='1' then
			write_pointer <= (others => '0');
		elsif clock'event and clock='1' then
	-- If receiving data and fifo isn`t empty, record data on fifo and increase write pointer
			if rx = '1' and write_pointer /= read_pointer then
				buf(CONV_INTEGER(write_pointer)) <= data_in;
				write_pointer <= write_pointer + 1;
			end if;
		end if;
	end process;
	
	-- If fifo isn`t empty, credit is high. Else, low
	credit_o <= '1' when write_pointer /= read_pointer else '0';

	-------------------------------------------------------------------------------------------
	-- OUTPUT FIFO
	-------------------------------------------------------------------------------------------

	-- Available the data to transmition (asynchronous read).
	data <= buf(CONV_INTEGER(read_pointer));

	process(reset, clock)
	begin
		if reset='1' then
			counter_flit <= (others=>'0');
			h <= '0';
			data_available <= '0';
			sender <=  '0';
			-- Initialize the read pointer with one position before the write pointer
			read_pointer <= (others=>'1'); 
			EA <= S_INIT;
		elsif clock'event and clock='1' then
			case EA is
				when S_INIT =>
					counter_flit <= (others=>'0');
					h<='0';
					data_available <= '0';
					-- If fifo isn`t empty
					if (read_pointer + 1 /= write_pointer) then
						-- Routing request to Switch Control
						h<='1';
						-- Increase the read pointer position with a valid data
						read_pointer <= read_pointer + 1;
						EA <= S_HEADER;
					end if;

				when S_HEADER =>
					-- When the Switch Control confirm the routing
					if ack_h='1' then
						-- Disable the routing request 
						h       <= '0';
						-- Enable wrapper signal to packet transmition
						sender  <= '1';
						data_available <= '1'; 
						EA      <= S_SENDHEADER ;
					end if;

				when S_SENDHEADER  =>
					-- If the data available is read or was read 
					if data_ack = '1' or data_available = '0' then
						-- If fifo isn`t empty 
						if (read_pointer + 1 /= write_pointer) then
							data_available   <= '1';
							read_pointer   <= read_pointer + 1;
							EA <= S_PAYLOAD;
						-- If fifo is empty (protection clause)
						else
							data_available <= '0';
						end if;
					end if;

				when S_PAYLOAD =>
					-- If the data available is read or was read 
					if data_ack = '1' or data_available = '0' then
						-- If fifo isn`t empty or is tail
						if (read_pointer + 1 /= write_pointer) or counter_flit = x"1" then
							-- If the second flit, memorize the packet size
							if counter_flit = x"0"   then   
						                counter_flit <=  buf(CONV_INTEGER(read_pointer));
							elsif counter_flit /= x"1" then 
								counter_flit <=  counter_flit - 1;
							end if;
							-- If the tail flit
				                        if counter_flit = x"1" then
								-- If tail is send
								if data_ack = '1' then
									data_available <= '0';
									sender <= '0';
						                        EA <= S_INIT;
								else
									EA <= S_END;
								end if;
							-- Else read the next position
        	                			else
								data_available <= '1';
								read_pointer <= read_pointer + 1;
					                end if;
						-- If fifo is empty (protection clause)
						else
							data_available <= '0';
						end if;
					end if;
				when S_END =>
					-- When tail is send
					if data_ack = '1' then
						data_available <= '0';
						sender <= '0';
			                        EA <= S_INIT;
					end if;
			end case;
		end if;
	end process;
	
	data_av <= data_available;

	empty_buf <= '1' when read_pointer + 1 = write_pointer else '0';

end Hermes_buffer;

-- Fila bi sýncrona com codificaýýo de ponteiros Johnson

architecture Async_Johnson_Hermes_buffer of HermesG_buffer is

type fifo_out is (S_INIT, S_HEADER, S_SENDHEADER, S_PAYLOAD, S_END);
signal EA : fifo_out;

signal buf: buff := (others=>(others=>'0'));
signal read_pointer,write_pointer: pointer ;

signal read_ptr_2flop, read_ptr_1flop, wjohn 	: std_logic_vector (TAM_JOHNSON_POINTER-1 downto 0);
signal write_ptr_2flop, write_ptr_1flop, rjohn 	: std_logic_vector (TAM_JOHNSON_POINTER-1 downto 0);

signal full, empty, data_available : std_logic;

signal counter_flit: regflit ;

begin

	-------------------------------------------------------------------------------------------
	-- INPUT FIFO
	-------------------------------------------------------------------------------------------
	process(reset, clock_rx)
	begin
		if reset='1' then
			write_pointer <= (others => '0');
			wjohn <= (others => '0');
		elsif clock_rx'event and clock_rx='1' then
	-- If receiving data and fifo isn`t empty, record data on fifo and increase write pointer
			if rx = '1' and full = '0' then
				buf(CONV_INTEGER(write_pointer)) <= data_in;
				write_pointer <= write_pointer + 1;
				wjohn <= wjohn(TAM_JOHNSON_POINTER-2 downto 0) & not wjohn(TAM_JOHNSON_POINTER-1);
			end if;
		end if;
	end process;

	process (reset,clock_rx) -- read pointer synchronizer with write clock
	begin 
		if (reset = '1') then
			read_ptr_2flop(TAM_JOHNSON_POINTER-1) <= '1';
			read_ptr_2flop(TAM_JOHNSON_POINTER-2 downto 0) <= (others=>'0');
			read_ptr_1flop(TAM_JOHNSON_POINTER-1) <= '1';
			read_ptr_1flop(TAM_JOHNSON_POINTER-2 downto 0) <= (others=>'0');
		elsif clock_rx'event and clock_rx = '1'  then 
			read_ptr_1flop <= rjohn;
			read_ptr_2flop <= read_ptr_1flop;
		end if;
	end process;

	full <= '1' when (wjohn = read_ptr_2flop) else '0';
	
	-- If fifo isn`t full, credit is high. Else, low
	credit_o <= not full;

	-------------------------------------------------------------------------------------------
	-- OUTPUT FIFO
	-------------------------------------------------------------------------------------------

	-- Available the data to transmition (asynchronous read).
	data <= buf(CONV_INTEGER(read_pointer));

	process(reset, clock)
	begin
		if reset='1' then
			counter_flit <= (others=>'0');
			h <= '0';
			data_available <= '0';
			sender <=  '0';
			-- Initialize the read pointer with one position before the write pointer
			read_pointer <= (others=>'1'); 
			rjohn(TAM_JOHNSON_POINTER-1) <= '1';
			rjohn(TAM_JOHNSON_POINTER-2 downto 0) <= (others=>'0');
			EA <= S_INIT;
		elsif clock'event and clock='1' then
			case EA is
				when S_INIT =>
					counter_flit <= (others=>'0');
					h<='0';
					data_available <= '0';
					-- If fifo isn`t empty
					if empty = '0' then
						-- Routing request to Switch Control
						h<='1';
						-- Increase the read pointer position with a valid data
						read_pointer <= read_pointer + 1;
						rjohn <= rjohn(TAM_JOHNSON_POINTER-2 downto 0) & not rjohn(TAM_JOHNSON_POINTER-1);
						EA <= S_HEADER;
					end if;

				when S_HEADER =>
					-- When the Switch Control confirm the routing
					if ack_h='1' then
						-- Disable the routing request 
						h       <= '0';
						-- Enable wrapper signal to packet transmition
						sender  <= '1';
						data_available <= '1'; 
						EA      <= S_SENDHEADER ;
					end if;

				when S_SENDHEADER  =>
					-- If the data available is read or was read 
					if data_ack = '1' or data_available = '0' then
						-- If fifo isn`t empty 
						if empty = '0' then
							data_available   <= '1';
							read_pointer   <= read_pointer + 1;
							rjohn <= rjohn(TAM_JOHNSON_POINTER-2 downto 0) & not rjohn(TAM_JOHNSON_POINTER-1);
							EA <= S_PAYLOAD;
						-- If fifo is empty (protection clause)
						else
							data_available <= '0';
						end if;
					end if;

				when S_PAYLOAD =>
					-- If the data available is read or was read 
					if data_ack = '1' or data_available = '0' then
						-- If fifo isn`t empty or is tail
						if empty = '0' or counter_flit = x"1" then
							-- If the second flit, memorize the packet size
							if counter_flit = x"0"   then   
						                counter_flit <=  buf(CONV_INTEGER(read_pointer));
							elsif counter_flit /= x"1" then 
								counter_flit <=  counter_flit - 1;
							end if;
							-- If the tail flit
				                        if counter_flit = x"1" then
								-- If tail is send
								if data_ack = '1' then
									data_available <= '0';
									sender <= '0';
						                        EA <= S_INIT;
								else
									EA <= S_END;
								end if;
							-- Else read the next position
        	                			else
								data_available <= '1';
								read_pointer <= read_pointer + 1;
								rjohn <= rjohn(TAM_JOHNSON_POINTER-2 downto 0) & not rjohn(TAM_JOHNSON_POINTER-1);
					                end if;
						-- If fifo is empty (protection clause)
						else
							data_available <= '0';
						end if;
					end if;
				when S_END =>
					-- When tail is send
					if data_ack = '1' then
						data_available <= '0';
						sender <= '0';
			                        EA <= S_INIT;
					end if;
			end case;
		end if;
	end process;
	
	data_av <= data_available;

	process (clock,reset)  -- write pointer synchronizer with read clock
	begin 
		if (reset = '1') then
			write_ptr_2flop(TAM_JOHNSON_POINTER-1) <= '1';
			write_ptr_2flop(TAM_JOHNSON_POINTER-2 downto 0) <= (others=>'0');
			write_ptr_1flop(TAM_JOHNSON_POINTER-1) <= '1';
			write_ptr_1flop(TAM_JOHNSON_POINTER-2 downto 0) <= (others=>'0');
		elsif clock'event and clock = '1'  then --
			write_ptr_1flop <= not wjohn(0) & wjohn(TAM_JOHNSON_POINTER-1 downto 1);
			write_ptr_2flop <= write_ptr_1flop;	
		end if;
	end process;

	empty <= '1' when (rjohn = write_ptr_2flop) else '0';
	empty_buf <= empty;

end Async_Johnson_Hermes_buffer;



-- Fila bi sýncrona com codificaýýo de ponteiros Gray

architecture Async_Gray_Hermes_buffer of HermesG_buffer is


type fifo_out is (S_INIT, S_HEADER, S_SENDHEADER, S_PAYLOAD, S_END);
signal EA : fifo_out;

signal buf: buff := (others=>(others=>'0'));
signal read_ptr_2flop, read_ptr_1flop : pointer ;
signal write_ptr_2flop, write_ptr_1flop, write_pointer_gray_before : pointer;

signal read_pointer_gray, no_ones_below_read_gray	: std_logic_vector (TAM_POINTER downto 0);
signal write_pointer_gray, no_ones_below_write_gray	: std_logic_vector (TAM_POINTER downto 0);

signal full, empty, data_available : std_logic;

signal counter_flit: regflit ;

begin

	-------------------------------------------------------------------------------------------
	-- INPUT FIFO
	-------------------------------------------------------------------------------------------
	process(reset, clock_rx)
	begin
		if reset='1' then
			write_pointer_gray(0) <= '1'; -- Parity bit
			write_pointer_gray(TAM_POINTER downto 1) <= (others => '0');
			write_pointer_gray_before(TAM_POINTER-1) <= '1';
			write_pointer_gray_before(TAM_POINTER-2 downto 0) <= (others=>'0');
		elsif clock_rx'event and clock_rx='1' then
	-- If receiving data and fifo isn`t empty, record data on fifo and increase write pointer
			if rx = '1' and full = '0' then
				buf(CONV_INTEGER(write_pointer_gray(TAM_POINTER downto 1))) <= data_in;
				write_pointer_gray_before <= write_pointer_gray(TAM_POINTER downto 1);
				write_pointer_gray(0) <= not(write_pointer_gray(0));
				for i in 1 to (TAM_POINTER) loop
					if ((i = (TAM_POINTER) or write_pointer_gray(i-1) = '1') and no_ones_below_write_gray(i-1) = '0') then
						write_pointer_gray(i) <= not(write_pointer_gray(i));
					end if;
				end loop;
			end if;
		end if;
	end process;

	-- Gray counter verification
	no_ones_below_write_gray(0) <= '0';
	no_ones_below_write_gray(TAM_POINTER downto 1) <= write_pointer_gray(TAM_POINTER-1 downto 0) or no_ones_below_write_gray(TAM_POINTER-1 downto 0);


	process (clock_rx,reset) -- read pointer synchronizer with write clock
	begin 
		if (reset = '1') then
			read_ptr_2flop(TAM_POINTER-1) <= '1';
			read_ptr_2flop(TAM_POINTER-2 downto 0) <= (others=>'0');
			read_ptr_1flop(TAM_POINTER-1) <= '1';
			read_ptr_1flop(TAM_POINTER-2 downto 0) <= (others=>'0');
		elsif clock_rx'event and clock_rx = '1'  then 
			read_ptr_1flop <= read_pointer_gray(TAM_POINTER downto 1);
			read_ptr_2flop <= read_ptr_1flop;
		end if;
	end process;	

	full <= '1' when (write_pointer_gray(TAM_POINTER downto 1) = read_ptr_2flop) else '0';

	-- If fifo isn`t full, credit is high. Else, low
	credit_o <= not full;

	-------------------------------------------------------------------------------------------
	-- OUTPUT FIFO
	-------------------------------------------------------------------------------------------

	-- Available the data to transmition (asynchronous read).
	data <= buf(CONV_INTEGER(read_pointer_gray(TAM_POINTER downto 1)));

	process(reset, clock)
	begin
		if reset='1' then
			counter_flit <= (others=>'0');
			h <= '0';
			data_available <= '0';
			sender <=  '0';
			-- Initialize the read pointer with one position before the write pointer
			read_pointer_gray(TAM_POINTER) <= '1';
			read_pointer_gray(TAM_POINTER-1 downto 0) <= (others => '0');
			EA <= S_INIT;
		elsif clock'event and clock='1' then
			case EA is
				when S_INIT =>
					counter_flit <= (others=>'0');
					h<='0';
					data_available <= '0';
					-- If fifo isn`t empty
					if empty = '0' then
						-- Routing request to Switch Control
						h<='1';
						-- Increase the read pointer position with a valid data
						read_pointer_gray(0) <= not(read_pointer_gray(0));
						for i in 1 to (TAM_POINTER) loop
							if ((i = (TAM_POINTER) or read_pointer_gray(i-1) = '1') and no_ones_below_read_gray(i-1) = '0') then
								read_pointer_gray(i) <= not(read_pointer_gray(i));
							end if;
						end loop;
						EA <= S_HEADER;
					end if;

				when S_HEADER =>
					-- When the Switch Control confirm the routing
					if ack_h='1' then
						-- Disable the routing request 
						h       <= '0';
						-- Enable wrapper signal to packet transmition
						sender  <= '1';
						data_available <= '1'; 
						EA      <= S_SENDHEADER ;
					end if;

				when S_SENDHEADER  =>
					-- If the data available is read or was read 
					if data_ack = '1' or data_available = '0' then
						-- If fifo isn`t empty 
						if empty = '0' then
							data_available   <= '1';
							read_pointer_gray(0) <= not(read_pointer_gray(0));
							for i in 1 to (TAM_POINTER) loop
								if ((i = (TAM_POINTER) or read_pointer_gray(i-1) = '1') and no_ones_below_read_gray(i-1) = '0') then
									read_pointer_gray(i) <= not(read_pointer_gray(i));
								end if;
							end loop;
							EA <= S_PAYLOAD;
						-- If fifo is empty (protection clause)
						else
							data_available <= '0';
						end if;
					end if;

				when S_PAYLOAD =>
					-- If the data available is read or was read 
					if data_ack = '1' or data_available = '0' then
						-- If fifo isn`t empty or is tail
						if empty = '0' or counter_flit = x"1" then
							-- If the second flit, memorize the packet size
							if counter_flit = x"0"   then   
						                counter_flit <=  buf(CONV_INTEGER(read_pointer_gray(TAM_POINTER downto 1)));
							elsif counter_flit /= x"1" then 
								counter_flit <=  counter_flit - 1;
							end if;
							-- If the tail flit
				                        if counter_flit = x"1" then
								-- If tail is send
								if data_ack = '1' then
									data_available <= '0';
									sender <= '0';
						                        EA <= S_INIT;
								else
									EA <= S_END;
								end if;
							-- Else read the next position
        	                			else
								data_available <= '1';
								read_pointer_gray(0) <= not(read_pointer_gray(0));
								for i in 1 to (TAM_POINTER) loop
									if ((i = (TAM_POINTER) or read_pointer_gray(i-1) = '1') and no_ones_below_read_gray(i-1) = '0') then
										read_pointer_gray(i) <= not(read_pointer_gray(i));
									end if;
								end loop;
					                end if;
						-- If fifo is empty (protection clause)
						else
							data_available <= '0';
						end if;
					end if;
				when S_END =>
					-- When tail is send
					if data_ack = '1' then
						data_available <= '0';
						sender <= '0';
			                        EA <= S_INIT;
					end if;
			end case;
		end if;
	end process;
	
	data_av <= data_available;

  -- Gray counter verification
	no_ones_below_read_gray(0) <= '0';
	no_ones_below_read_gray(TAM_POINTER downto 1) <= read_pointer_gray(TAM_POINTER-1 downto 0) or no_ones_below_read_gray(TAM_POINTER-1 downto 0);


	process (clock,reset)  -- write pointer synchronizer with read clock
	begin 
		if (reset = '1') then
			write_ptr_2flop(TAM_POINTER-1) <= '1';
			write_ptr_2flop(TAM_POINTER-2 downto 0) <= (others=>'0');
			write_ptr_1flop(TAM_POINTER-1) <= '1';
			write_ptr_1flop(TAM_POINTER-2 downto 0) <= (others=>'0');
		elsif clock'event and clock = '1'  then 
			write_ptr_1flop <= write_pointer_gray_before;
			write_ptr_2flop <= write_ptr_1flop;	
		end if;
	end process;

	empty <= '1' when (read_pointer_gray(TAM_POINTER downto 1) = write_ptr_2flop) else '0';
	empty_buf <= empty;

end Async_Gray_Hermes_buffer;
