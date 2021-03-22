-----------------------------------------------------------------------------
--	Top Level da Roteador - Possui as ligações dos sinais entre os módulos --
-----------------------------------------------------------------------------

library IEEE;
use IEEE.std_logic_1164.all; 
use IEEE.std_logic_unsigned.all;
use work.Mercury_package.all;

entity Router is
	generic(pos: max_noc_size := (others=>'0'));
	port(
	clock,
	reset,
	data_av_IL: in std_logic;		
	data_av_OL: out std_logic;
	ack_nack_I: in ack_nack_bus;
	ack_nack_O: out ack_nack_bus;
	data_I: in data_bus;
	data_O: out data_bus;
	size_I: in size_bus;
	size_O: out size_bus;
	queue_addr_I: in queue_addr_bus;
	queue_addr_O: out queue_addr_bus);
end Router;

architecture Router of Router is
signal ctrl_A, ctrl_B, ctrl_C: door;
signal ack_A, ack_B, ack_C, ack_nack_L_B, ack_nack_L_C,
	   ack_nack_A, ack_nack_B, ack_nack_C: ackNack;
signal data_A, data_B, data_C, size_A, size_B, size_C: phit;
signal queue_addr_N, queue_addr_S, queue_addr_E, queue_addr_W: queue_addr;
signal queue_addr_L_A, queue_addr_L_B, queue_addr_L_C,
		data_av_A, data_av_B, data_av_C, crtl_ack_nack_A, crtl_ack_nack_B, crtl_ack_nack_C: std_logic;	

begin			   
	queue_addr_O(conv_integer(NORTH)) <= queue_addr_N;
	queue_addr_O(conv_integer(SOUTH)) <= queue_addr_S;
	queue_addr_O(conv_integer(EAST)) <= queue_addr_E;
	queue_addr_O(conv_integer(WEST)) <= queue_addr_W;
	
	data_O(conv_integer(LOCAL)) <= data_C;
	data_av_OL <= queue_addr_L_C;
	size_O(conv_integer(LOCAL)) <= size_C;
			
	ACKNACK_OUT: entity work.AckNackOut(AckNackOut)
		port map(
		queue_addr_N=>queue_addr_I(conv_integer(NORTH)),
		queue_addr_S=>queue_addr_I(conv_integer(SOUTH)),
		queue_addr_E=>queue_addr_I(conv_integer(EAST)),
		queue_addr_W=>queue_addr_I(conv_integer(WEST)),
		ctrl_A=>ctrl_A,
		ctrl_B=>ctrl_B,			
		ctrl_C=>ctrl_C,
		ack_nack_A=>ack_nack_A,
		ack_nack_B=>ack_nack_B,
		ack_nack_C=>ack_nack_C,
		crtl_ack_nack_A=>crtl_ack_nack_A,
	   crtl_ack_nack_B=>crtl_ack_nack_B,
	   crtl_ack_nack_C=>crtl_ack_nack_C,
		ack_nack_N=>ack_nack_O(conv_integer(NORTH)),
		ack_nack_S=>ack_nack_O(conv_integer(SOUTH)),
		ack_nack_E=>ack_nack_O(conv_integer(EAST)),
		ack_nack_W=>ack_nack_O(conv_integer(WEST)),
		ack_nack_L_A=>ack_nack_O(conv_integer(LOCAL)),
		ack_nack_L_B=>ack_nack_L_B,
		ack_nack_L_C=>ack_nack_L_C);
			
	ACKNACK_IN: entity work.AckNackIn(AckNackIn)
		port map(
		ack_L_A=>ack_nack_L_B,
		ack_L_B=>ack_nack_L_C,
		ack_L_C=>ack_nack_I(conv_integer(LOCAL)),
		ack_N=>ack_nack_I(conv_integer(NORTH)),		
		ack_S=>ack_nack_I(conv_integer(SOUTH)),
		ack_E=>ack_nack_I(conv_integer(EAST)),
		ack_W=>ack_nack_I(conv_integer(WEST)),
		queue_addr_N=>queue_addr_N,
		queue_addr_S=>queue_addr_S,
		queue_addr_E=>queue_addr_E,
		queue_addr_W=>queue_addr_W,
		queue_addr_L_A=>queue_addr_L_A,
		queue_addr_L_B=>queue_addr_L_B,
		queue_addr_L_C=>queue_addr_L_C,
		ack_A=>ack_A,
		ack_B=>ack_B,
		ack_C=>ack_C);
	
	DATASIZE_OUT: entity work.DataSizeOut(DataSizeOut)
		port map(
		data_A=>data_A,
		data_B=>data_B,
		data_C=>data_C,
		size_A=>size_A,		
		size_B=>size_B,
		size_C=>size_C,
		queue_addr_N=>queue_addr_N,
		queue_addr_S=>queue_addr_S,
		queue_addr_E=>queue_addr_E,
		queue_addr_W=>queue_addr_W,
		data_ON=>data_O(conv_integer(NORTH)),
		data_OS=>data_O(conv_integer(SOUTH)),
		data_OE=>data_O(conv_integer(EAST)),
		data_OW=>data_O(conv_integer(WEST)),
		size_ON=>size_O(conv_integer(NORTH)),
		size_OS=>size_O(conv_integer(SOUTH)),
		size_OE=>size_O(conv_integer(EAST)),		
		size_OW=>size_O(conv_integer(WEST)));
		
	ARBQUEUE_A: entity work.ArbQueue(ArbQueue)
		generic map (queue_addr_ctrl => FILA_A)
		port map(
		clock=>clock,
		reset=>reset,
		queue_addr_L=>data_av_IL,
		queue_addr_IN=>queue_addr_I(conv_integer(NORTH)),
		queue_addr_IS=>queue_addr_I(conv_integer(SOUTH)),
		queue_addr_IE=>queue_addr_I(conv_integer(EAST)),
		queue_addr_IW=>queue_addr_I(conv_integer(WEST)),
		size_IN=>size_I(conv_integer(NORTH)),
		size_IS=>size_I(conv_integer(SOUTH)),
		size_IE=>size_I(conv_integer(EAST)),
		size_IW=>size_I(conv_integer(WEST)),
		size_IL=>size_I(conv_integer(LOCAL)),
		ack_in=>ack_A,
		ack_out=>ack_nack_A,
		data_av=>data_av_A,
		crtl_ack_nack=>crtl_ack_nack_A,
		data_IN=>data_I(conv_integer(NORTH)),
		data_IS=>data_I(conv_integer(SOUTH)),
		data_IE=>data_I(conv_integer(EAST)),
		data_IW=>data_I(conv_integer(WEST)),
		data_IL=>data_I(conv_integer(LOCAL)),
		data=>data_A,
		size=>size_A,
		ctrl=>ctrl_A);

	ARBQUEUE_B: entity work.ArbQueue(ArbQueue)
		generic map (queue_addr_ctrl => FILA_B)
		port map(
		clock=>clock,
		reset=>reset,
		queue_addr_L=>queue_addr_L_A,
		queue_addr_IN=>queue_addr_I(conv_integer(NORTH)),
		queue_addr_IS=>queue_addr_I(conv_integer(SOUTH)),
		queue_addr_IE=>queue_addr_I(conv_integer(EAST)),
		queue_addr_IW=>queue_addr_I(conv_integer(WEST)),
		size_IN=>size_I(conv_integer(NORTH)),
		size_IS=>size_I(conv_integer(SOUTH)),
		size_IE=>size_I(conv_integer(EAST)),
		size_IW=>size_I(conv_integer(WEST)),
		size_IL=>size_A,
		ack_in=>ack_B,
		ack_out=>ack_nack_B,
		data_av=>data_av_B,
		crtl_ack_nack=>crtl_ack_nack_B,
		data_IN=>data_I(conv_integer(NORTH)),
		data_IS=>data_I(conv_integer(SOUTH)),
		data_IE=>data_I(conv_integer(EAST)),
		data_IW=>data_I(conv_integer(WEST)),
		data_IL=>data_A,
		data=>data_B,
		size=>size_B,
		ctrl=>ctrl_B);		
		
	ARBQUEUE_C: entity work.ArbQueue(ArbQueue)
		generic map (queue_addr_ctrl => FILA_C)
		port map(
		clock=>clock,
		reset=>reset,
		queue_addr_L=>queue_addr_L_B,
		queue_addr_IN=>queue_addr_I(conv_integer(NORTH)),
		queue_addr_IS=>queue_addr_I(conv_integer(SOUTH)),
		queue_addr_IE=>queue_addr_I(conv_integer(EAST)),
		queue_addr_IW=>queue_addr_I(conv_integer(WEST)),
		size_IN=>size_I(conv_integer(NORTH)),
		size_IS=>size_I(conv_integer(SOUTH)),
		size_IE=>size_I(conv_integer(EAST)),
		size_IW=>size_I(conv_integer(WEST)),
		size_IL=>size_B,
		ack_in=>ack_C,
		ack_out=>ack_nack_C,
		data_av=>data_av_C,
		crtl_ack_nack=>crtl_ack_nack_C,
		data_IN=>data_I(conv_integer(NORTH)),
		data_IS=>data_I(conv_integer(SOUTH)),
		data_IE=>data_I(conv_integer(EAST)),
		data_IW=>data_I(conv_integer(WEST)),
		data_IL=>data_B,
		data=>data_C,
		size=>size_C,
		ctrl=>ctrl_C);				
		
	ARBITEROUT: entity work.ArbiterOut(AlgoritmoCG) 
		port map(
		clock=>clock,
		reset=>reset,
		data_av_A=>data_av_A,
		data_av_B=>data_av_B,
		data_av_C=>data_av_C,
		ack_nack_L_A=>ack_nack_L_B,
		ack_nack_L_B=>ack_nack_L_C,
		ack_nack_L_C=>ack_nack_I(conv_integer(LOCAL)),
		ack_nack_N=>ack_nack_I(conv_integer(NORTH)),
		ack_nack_S=>ack_nack_I(conv_integer(SOUTH)),
		ack_nack_E=>ack_nack_I(conv_integer(EAST)),
		ack_nack_W=>ack_nack_I(conv_integer(WEST)),
		data_A=>data_A(HALF_PHIT_SIZE - 1 downto 0),
		data_B=>data_B(HALF_PHIT_SIZE - 1 downto 0),
		data_C=>data_C(HALF_PHIT_SIZE - 1 downto 0),
		pos=>pos,
		queue_addr_L_A=>queue_addr_L_A,
		queue_addr_L_B=>queue_addr_L_B,
		queue_addr_L_C=>queue_addr_L_C,
		queue_addr_N=>queue_addr_N,
		queue_addr_S=>queue_addr_S,
		queue_addr_E=>queue_addr_E,
		queue_addr_W=>queue_addr_W);
end Router;