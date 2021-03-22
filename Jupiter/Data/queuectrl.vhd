library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;
use work.Mercury_package.all;

entity QueueCtrl is
	generic (queue_addr_ctrl : queue_addr);
	port(
	queue_addr_N,
	queue_addr_S,
	queue_addr_E,
	queue_addr_W: in queue_addr;
	queue_addr_L: in std_logic;
	queue_ctrl: out door
	);
end QueueCtrl;

architecture QueueCtrl of QueueCtrl is
begin
	queue_ctrl <= LOCAL when queue_addr_L = '1' else
				  NORTH when queue_addr_N = queue_addr_ctrl else
				  SOUTH when queue_addr_S = queue_addr_ctrl else
				  EAST when queue_addr_E = queue_addr_ctrl else
			      WEST when queue_addr_W = queue_addr_ctrl else
				  PORT_NONE;				 
end QueueCtrl;