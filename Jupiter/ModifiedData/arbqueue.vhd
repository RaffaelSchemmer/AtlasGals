library IEEE;
use IEEE.std_logic_1164.all;
use work.Mercury_package.all;

entity ArbQueue is
	generic (queue_addr_ctrl : queue_addr);
	port(
	clock, 
	reset,
	queue_addr_L: in std_logic;
	queue_addr_IN,
	queue_addr_IS, 
	queue_addr_IE,
	queue_addr_IW: in queue_addr;	
	size_IN,
	size_IS,
	size_IE,
	size_IW,
	size_IL: in phit;
	ack_in: in ackNack;
	ack_out: out ackNack;
	data_av,
   crtl_ack_nack: out std_logic;
	data_IN,
	data_IS,
	data_IE,
	data_IW, 
	data_IL: in phit;
	data,
	size: out phit;
	ctrl: out door);
end ArbQueue;

architecture ArbQueue of ArbQueue is
signal free_size: max_package_size;
signal size_int, data_int: phit;
signal enqueue, data_av_N, data_av_S, data_av_E, data_av_W, data_av_L, crtl_ack_nack_int: std_logic;
signal ctrl_int: door;
begin
	size_int <= size_IN when ctrl_int = NORTH else
			    size_IS when ctrl_int = SOUTH else
			    size_IE when ctrl_int = EAST else
			    size_IW when ctrl_int = WEST else
			    size_IL when ctrl_int = LOCAL else
			    (others=>'0');
				   
	data_int <= data_IN when ctrl_int = NORTH else
				data_IS when ctrl_int = SOUTH else
				data_IE when ctrl_int = EAST else
				data_IW when ctrl_int = WEST else
				data_IL when ctrl_int = LOCAL else
				(others=>'0');
				
	ctrl <= ctrl_int;
   
   crtl_ack_nack <= crtl_ack_nack_int;
				   
	QUEUE_ABC: entity work.Queue(Queue)
		port map(
		clock=>clock, 
		reset=>reset,
		free_size=>free_size,
		enqueue=>enqueue, 
		data_av=>data_av,
		data=>data,	
		data_in=>data_int,
		size=>size,
		ack_in=>ack_in);
	
	INTOARBITERIN_ABC: entity work.IntoArbiterIn(IntoArbiterIn)
		generic map (queue_addr_this => queue_addr_ctrl)
		port map(		   
		--reset=>reset, 		--->  comentei porque a sintese xst dava warning de sinal nunca utilizado, se descomentar aqui tb descomentar no intoarbiterin.vhd
		queue_addr_N=>queue_addr_IN,
		queue_addr_S=>queue_addr_IS,
		queue_addr_E=>queue_addr_IE,
		queue_addr_W=>queue_addr_IW,
		queue_addr_L=>queue_addr_L,
		data_av_N=>data_av_N,
		data_av_S=>data_av_S,
		data_av_E=>data_av_E,
		data_av_W=>data_av_W,
		data_av_L=>data_av_L);
	
	ARBITERIN_ABC: entity work.ArbiterIn(ArbiterIn)
		port map(
		clock=>clock,
		reset=>reset,
		data_av_N=>data_av_N,
		data_av_S=>data_av_S,
		data_av_E=>data_av_E,
		data_av_W=>data_av_W,
		data_av_L=>data_av_L,
		enqueue=>enqueue,
 	   crtl_ack_nack=> crtl_ack_nack_int,
		free_size=>free_size,
		ctrl=>ctrl_int,
		ack_nack=>ack_out,
		size=>size_int);		
end ArbQueue;
