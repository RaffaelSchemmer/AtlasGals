library IEEE;
use IEEE.std_logic_1164.all;
use work.Mercury_package.all;

entity AckNackIn is	
	port(
	ack_L_A,
	ack_L_B,
	ack_L_C,
	ack_N,
	ack_S,
	ack_E,
	ack_W: in ackNack;
	queue_addr_N,
	queue_addr_S,
	queue_addr_E,
	queue_addr_W: in queue_addr;
	queue_addr_L_A,
	queue_addr_L_B,
	queue_addr_L_C: in std_logic;
	ack_A,
	ack_B,
	ack_C: out ackNack);
end AckNackIn;

architecture AckNackIn of AckNackIn is
signal ctrl_A, ctrl_B, ctrl_C: door;
begin							  							
	ack_A <= ack_L_A when ctrl_A = LOCAL else	 
			 ack_N when ctrl_A = NORTH else
			 ack_S when ctrl_A = SOUTH else
			 ack_E when ctrl_A = EAST else
			 ack_W when ctrl_A = WEST else			 
			 NONE_ME;	 
			 
	ack_B <= ack_L_B when ctrl_B = LOCAL else
			 ack_N when ctrl_B = NORTH else
			 ack_S when ctrl_B = SOUTH else
			 ack_E when ctrl_B = EAST else
			 ack_W when ctrl_B = WEST else			 
			 NONE_ME;	 			 
		
	ack_C <= ack_L_C when ctrl_C = LOCAL else
			 ack_N when ctrl_C = NORTH else
			 ack_S when ctrl_C = SOUTH else
			 ack_E when ctrl_C = EAST else
			 ack_W when ctrl_C = WEST else			 
			 NONE_ME;	 
			 
	QUEUECTRL_A: entity work.QueueCtrl(QueueCtrl)
		generic	map (queue_addr_ctrl => FILA_A)
		port map(
		queue_addr_N=>queue_addr_N,
		queue_addr_S=>queue_addr_S,
		queue_addr_E=>queue_addr_E,
		queue_addr_W=>queue_addr_W,
		queue_addr_L=>queue_addr_L_A,
		queue_ctrl=>ctrl_A);
		
	QUEUECTRL_B: entity work.QueueCtrl(QueueCtrl)
		generic	map (queue_addr_ctrl => FILA_B)
		port map(
		queue_addr_N=>queue_addr_N,
		queue_addr_S=>queue_addr_S,
		queue_addr_E=>queue_addr_E,
		queue_addr_W=>queue_addr_W,
		queue_addr_L=>queue_addr_L_B,
		queue_ctrl=>ctrl_B);
		
	QUEUECTRL_C: entity work.QueueCtrl(QueueCtrl)
		generic	map (queue_addr_ctrl => FILA_C)
		port map(
		queue_addr_N=>queue_addr_N,
		queue_addr_S=>queue_addr_S,
		queue_addr_E=>queue_addr_E,
		queue_addr_W=>queue_addr_W,
		queue_addr_L=>queue_addr_L_C,
		queue_ctrl=>ctrl_C);		
		
end AckNackIn;