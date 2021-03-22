---------------------------------------------------------------------------------------
--                      HermesG_Fifo_Output
--                        --------------
--             clock_rx ->|            |-> data
--             data_in  ->|            |-> credit_r
--             rx       ->|            |<- clock
--             credit_o <-|            |<- rx
--                        --------------
---------------------------------------------------------------------------------------
library IEEE;
use ieee.STD_LOGIC_UNSIGNED.all;
use ieee.std_logic_1164.all;
use work.HermesG_package.all;
entity HermesG_Fifo_Output is
  GENERIC (data_depth: INTEGER :=TAM_BUFFER; -- Profundidade do buffer definida por TAM_BUFFER em HermesG_Package
	      tam: INTEGER := TAM_POINTER; -- Ponteiro da fila, definido em TAM_POINTER em HermesG_Package como (log(TAM_BUFFER) na base 2) Ex: Log(8) = 3
	  data_width: INTEGER := TAM_FLIT); -- Comprimento de cada slot no buffer, definido por TAM_FLIT em HermesG_Package
  port (
	reset:      in  std_logic;
	
	
	
	clock_rx:   in  std_logic;
	rx:         in  std_logic;
	data_in:    in  regflit;
	credit_o:   out std_logic;
	
	clock:      in  std_logic;
	data:       out regflit;
	credit_r:   out std_logic;
		idle_fifo:   out std_logic;
	tx:	    in std_logic);

end entity;

architecture HermesG_Fifo_Output of HermesG_Fifo_Output is

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
			-- Initialize the read pointer with one position before the write pointer
			read_pointer_gray(TAM_POINTER) <= '1';
			read_pointer_gray(TAM_POINTER-1 downto 0) <= (others => '0');
			EA <= S_INIT;
		elsif clock'event and clock='1' then
			if tx = '1' and empty = '0' then
				data_available <= '1';
				read_pointer_gray(0) <= not(read_pointer_gray(0));
				for i in 1 to (TAM_POINTER) loop
					if ((i = (TAM_POINTER) or read_pointer_gray(i-1) = '1') and no_ones_below_read_gray(i-1) = '0') then
						read_pointer_gray(i) <= not(read_pointer_gray(i));
					end if;
				end loop;
			-- If fifo is empty (protection clause)
			else
				data_available <= '0';
			end if;
		end if;
	end process;
	
	credit_r <= data_available;

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
	
	idle_fifo <= empty;

end HermesG_Fifo_Output;
