library IEEE;
use IEEE.std_logic_1164.all;
use work.Mercury_package.all;

entity DataSizeOut is
	port(
	data_A,
	data_B,
	data_C,
	size_A,
	size_B,
	size_C: in phit;
	queue_addr_N,
	queue_addr_S,
	queue_addr_E,
	queue_addr_W: in queue_addr;
	data_ON,
	data_OS,
	data_OE,
	data_OW,
	size_ON,
	size_OS,
	size_OE, 
	size_OW: out phit);
end DataSizeOut;

architecture DataSizeOut of DataSizeOut is
begin
	data_ON <= data_A when queue_addr_N = FILA_A else
			   data_B when queue_addr_N = FILA_B else		
			   data_C when queue_addr_N = FILA_C else		
			   (others=>'0');
			   
	data_OS <= data_A when queue_addr_S = FILA_A else
			   data_B when queue_addr_S = FILA_B else		
			   data_C when queue_addr_S = FILA_C else		
			   (others=>'0');			
			   
	data_OE <= data_A when queue_addr_E = FILA_A else
			   data_B when queue_addr_E = FILA_B else		
			   data_C when queue_addr_E = FILA_C else		
			   (others=>'0');			   
			   
	data_OW <= data_A when queue_addr_W = FILA_A else
			   data_B when queue_addr_W = FILA_B else		
			   data_C when queue_addr_W = FILA_C else		
			   (others=>'0');
			   
	size_ON <= size_A when queue_addr_N = FILA_A else
			   size_B when queue_addr_N = FILA_B else		
			   size_C when queue_addr_N = FILA_C else		
			   (others=>'0');
			   
	size_OS <= size_A when queue_addr_S = FILA_A else
			   size_B when queue_addr_S = FILA_B else		
			   size_C when queue_addr_S = FILA_C else		
			   (others=>'0');			
			   
	size_OE <= size_A when queue_addr_E = FILA_A else
			   size_B when queue_addr_E = FILA_B else		
			   size_C when queue_addr_E = FILA_C else		
			   (others=>'0');			
			   
	size_OW <= size_A when queue_addr_W = FILA_A else
			   size_B when queue_addr_W = FILA_B else		
			   size_C when queue_addr_W = FILA_C else		
			   (others=>'0');
end DataSizeOut;