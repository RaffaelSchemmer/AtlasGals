library IEEE;
use IEEE.std_logic_1164.all;  
use IEEE.std_logic_unsigned.all;
use work.Mercury_package.all;

entity IntoArbiterIn is
	generic (queue_addr_this : queue_addr);
	port(
	-- reset: in std_logic;     --->  comentei porque a sintese xst da warning de sinal nunca utilizado, se descomentar aqui tb descomentar no arbqueue.vhd
	queue_addr_N,
	queue_addr_S,
	queue_addr_E,
	queue_addr_W: in queue_addr;
	queue_addr_L: in std_logic;
	data_av_N,
	data_av_S,
	data_av_E,
	data_av_W,
	data_av_L: out std_logic);
end IntoArbiterIn;

architecture IntoArbiterIn of IntoArbiterIn is
begin
	data_av_N <= '1' when queue_addr_N = queue_addr_this else
   		         '0';
	data_av_S <= '1' when queue_addr_S = queue_addr_this else
				 '0';		
	data_av_E <= '1' when queue_addr_E = queue_addr_this else
				 '0';	
	data_av_W <= '1' when queue_addr_W = queue_addr_this else
				 '0';				 
	data_av_L <= queue_addr_L;				 				 
end IntoArbiterIn;
