library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;
use work.Mercury_package.all;

entity NOC is
port(
	clock : in std_logic;
	reset : in std_logic;
	data_av_local_I : in std_logic_NROT;
	data_av_local_O : out std_logic_NROT;
	data_local_I : in data_NROT;
	data_local_O : out data_NROT;
	ack_nack_local_I : in ack_nack_NROT;
	ack_nack_local_O : out ack_nack_NROT;
	size_local_I : in size_NROT;
	size_local_O : out size_NROT);
end NOC;

architecture NOC of NOC is

	signal data_av_local_int_I_00, data_av_local_int_I_10, data_av_local_int_I_20 : std_logic;
	signal data_av_local_int_O_00, data_av_local_int_O_10, data_av_local_int_O_20 : std_logic;
	signal data_int_I_00, data_int_I_10, data_int_I_20 : data_bus;
	signal data_int_O_00, data_int_O_10, data_int_O_20 : data_bus;
	signal data_local_int_00, data_local_int_10, data_local_int_20 : phit;
	signal ack_nack_int_I_00, ack_nack_int_I_10, ack_nack_int_I_20 : ack_nack_bus;
	signal ack_nack_int_O_00, ack_nack_int_O_10, ack_nack_int_O_20 : ack_nack_bus;
	signal ack_nack_local_int_00, ack_nack_local_int_10, ack_nack_local_int_20 : ackNack;
	signal size_int_I_00, size_int_I_10, size_int_I_20 : size_bus;
	signal size_int_O_00, size_int_O_10, size_int_O_20 : size_bus;
	signal size_local_int_00, size_local_int_10, size_local_int_20 : phit;
	signal queue_addr_int_I_00, queue_addr_int_I_10, queue_addr_int_I_20 : queue_addr_bus;
	signal queue_addr_int_O_00, queue_addr_int_O_10, queue_addr_int_O_20 : queue_addr_bus;
	signal data_av_local_int_I_01, data_av_local_int_I_11, data_av_local_int_I_21 : std_logic;
	signal data_av_local_int_O_01, data_av_local_int_O_11, data_av_local_int_O_21 : std_logic;
	signal data_int_I_01, data_int_I_11, data_int_I_21 : data_bus;
	signal data_int_O_01, data_int_O_11, data_int_O_21 : data_bus;
	signal data_local_int_01, data_local_int_11, data_local_int_21 : phit;
	signal ack_nack_int_I_01, ack_nack_int_I_11, ack_nack_int_I_21 : ack_nack_bus;
	signal ack_nack_int_O_01, ack_nack_int_O_11, ack_nack_int_O_21 : ack_nack_bus;
	signal ack_nack_local_int_01, ack_nack_local_int_11, ack_nack_local_int_21 : ackNack;
	signal size_int_I_01, size_int_I_11, size_int_I_21 : size_bus;
	signal size_int_O_01, size_int_O_11, size_int_O_21 : size_bus;
	signal size_local_int_01, size_local_int_11, size_local_int_21 : phit;
	signal queue_addr_int_I_01, queue_addr_int_I_11, queue_addr_int_I_21 : queue_addr_bus;
	signal queue_addr_int_O_01, queue_addr_int_O_11, queue_addr_int_O_21 : queue_addr_bus;
	signal data_av_local_int_I_02, data_av_local_int_I_12, data_av_local_int_I_22 : std_logic;
	signal data_av_local_int_O_02, data_av_local_int_O_12, data_av_local_int_O_22 : std_logic;
	signal data_int_I_02, data_int_I_12, data_int_I_22 : data_bus;
	signal data_int_O_02, data_int_O_12, data_int_O_22 : data_bus;
	signal data_local_int_02, data_local_int_12, data_local_int_22 : phit;
	signal ack_nack_int_I_02, ack_nack_int_I_12, ack_nack_int_I_22 : ack_nack_bus;
	signal ack_nack_int_O_02, ack_nack_int_O_12, ack_nack_int_O_22 : ack_nack_bus;
	signal ack_nack_local_int_02, ack_nack_local_int_12, ack_nack_local_int_22 : ackNack;
	signal size_int_I_02, size_int_I_12, size_int_I_22 : size_bus;
	signal size_int_O_02, size_int_O_12, size_int_O_22 : size_bus;
	signal size_local_int_02, size_local_int_12, size_local_int_22 : phit;
	signal queue_addr_int_I_02, queue_addr_int_I_12, queue_addr_int_I_22 : queue_addr_bus;
	signal queue_addr_int_O_02, queue_addr_int_O_12, queue_addr_int_O_22 : queue_addr_bus;
begin

	Router00 : Entity work.Router(Router)
	generic map(x"00" )
	port map(
		clock => clock,
		reset => reset,
		data_av_IL => data_av_local_int_I_00,
		data_av_OL => data_av_local_int_O_00,
		ack_nack_I => ack_nack_int_I_00,
		ack_nack_O => ack_nack_int_O_00,
		data_I => data_int_I_00,
		data_O => data_int_O_00,
		size_I => size_int_I_00,
		size_O => size_int_O_00,
		queue_addr_I => queue_addr_int_I_00,
		queue_addr_O => queue_addr_int_O_00
	);

	Router10 : Entity work.Router(Router)
	generic map(x"10" )
	port map(
		clock => clock,
		reset => reset,
		data_av_IL => data_av_local_int_I_10,
		data_av_OL => data_av_local_int_O_10,
		ack_nack_I => ack_nack_int_I_10,
		ack_nack_O => ack_nack_int_O_10,
		data_I => data_int_I_10,
		data_O => data_int_O_10,
		size_I => size_int_I_10,
		size_O => size_int_O_10,
		queue_addr_I => queue_addr_int_I_10,
		queue_addr_O => queue_addr_int_O_10
	);

	Router20 : Entity work.Router(Router)
	generic map(x"20" )
	port map(
		clock => clock,
		reset => reset,
		data_av_IL => data_av_local_int_I_20,
		data_av_OL => data_av_local_int_O_20,
		ack_nack_I => ack_nack_int_I_20,
		ack_nack_O => ack_nack_int_O_20,
		data_I => data_int_I_20,
		data_O => data_int_O_20,
		size_I => size_int_I_20,
		size_O => size_int_O_20,
		queue_addr_I => queue_addr_int_I_20,
		queue_addr_O => queue_addr_int_O_20
	);

	Router01 : Entity work.Router(Router)
	generic map(x"01" )
	port map(
		clock => clock,
		reset => reset,
		data_av_IL => data_av_local_int_I_01,
		data_av_OL => data_av_local_int_O_01,
		ack_nack_I => ack_nack_int_I_01,
		ack_nack_O => ack_nack_int_O_01,
		data_I => data_int_I_01,
		data_O => data_int_O_01,
		size_I => size_int_I_01,
		size_O => size_int_O_01,
		queue_addr_I => queue_addr_int_I_01,
		queue_addr_O => queue_addr_int_O_01
	);

	Router11 : Entity work.Router(Router)
	generic map(x"11" )
	port map(
		clock => clock,
		reset => reset,
		data_av_IL => data_av_local_int_I_11,
		data_av_OL => data_av_local_int_O_11,
		ack_nack_I => ack_nack_int_I_11,
		ack_nack_O => ack_nack_int_O_11,
		data_I => data_int_I_11,
		data_O => data_int_O_11,
		size_I => size_int_I_11,
		size_O => size_int_O_11,
		queue_addr_I => queue_addr_int_I_11,
		queue_addr_O => queue_addr_int_O_11
	);

	Router21 : Entity work.Router(Router)
	generic map(x"21" )
	port map(
		clock => clock,
		reset => reset,
		data_av_IL => data_av_local_int_I_21,
		data_av_OL => data_av_local_int_O_21,
		ack_nack_I => ack_nack_int_I_21,
		ack_nack_O => ack_nack_int_O_21,
		data_I => data_int_I_21,
		data_O => data_int_O_21,
		size_I => size_int_I_21,
		size_O => size_int_O_21,
		queue_addr_I => queue_addr_int_I_21,
		queue_addr_O => queue_addr_int_O_21
	);

	Router02 : Entity work.Router(Router)
	generic map(x"02" )
	port map(
		clock => clock,
		reset => reset,
		data_av_IL => data_av_local_int_I_02,
		data_av_OL => data_av_local_int_O_02,
		ack_nack_I => ack_nack_int_I_02,
		ack_nack_O => ack_nack_int_O_02,
		data_I => data_int_I_02,
		data_O => data_int_O_02,
		size_I => size_int_I_02,
		size_O => size_int_O_02,
		queue_addr_I => queue_addr_int_I_02,
		queue_addr_O => queue_addr_int_O_02
	);

	Router12 : Entity work.Router(Router)
	generic map(x"12" )
	port map(
		clock => clock,
		reset => reset,
		data_av_IL => data_av_local_int_I_12,
		data_av_OL => data_av_local_int_O_12,
		ack_nack_I => ack_nack_int_I_12,
		ack_nack_O => ack_nack_int_O_12,
		data_I => data_int_I_12,
		data_O => data_int_O_12,
		size_I => size_int_I_12,
		size_O => size_int_O_12,
		queue_addr_I => queue_addr_int_I_12,
		queue_addr_O => queue_addr_int_O_12
	);

	Router22 : Entity work.Router(Router)
	generic map(x"22" )
	port map(
		clock => clock,
		reset => reset,
		data_av_IL => data_av_local_int_I_22,
		data_av_OL => data_av_local_int_O_22,
		ack_nack_I => ack_nack_int_I_22,
		ack_nack_O => ack_nack_int_O_22,
		data_I => data_int_I_22,
		data_O => data_int_O_22,
		size_I => size_int_I_22,
		size_O => size_int_O_22,
		queue_addr_I => queue_addr_int_I_22,
		queue_addr_O => queue_addr_int_O_22
	);


	-- entradas da chave00
	data_int_I_00(conv_integer(WEST))<=data_int_O_20(conv_integer(EAST));
	size_int_I_00(conv_integer(WEST))<=size_int_O_20(conv_integer(EAST));
	ack_nack_int_I_00(conv_integer(WEST))<=ack_nack_int_O_20(conv_integer(EAST));
	queue_addr_int_I_00(conv_integer(WEST))<=queue_addr_int_O_20(conv_integer(EAST));

	data_int_I_00(conv_integer(NORTH))<=data_int_O_01(conv_integer(SOUTH));
	size_int_I_00(conv_integer(NORTH))<=size_int_O_01(conv_integer(SOUTH));
	ack_nack_int_I_00(conv_integer(NORTH))<=ack_nack_int_O_01(conv_integer(SOUTH));
	queue_addr_int_I_00(conv_integer(NORTH))<=queue_addr_int_O_01(conv_integer(SOUTH));

	data_int_I_00(conv_integer(EAST))<=data_int_O_10(conv_integer(WEST));
	size_int_I_00(conv_integer(EAST))<=size_int_O_10(conv_integer(WEST));
	ack_nack_int_I_00(conv_integer(EAST))<=ack_nack_int_O_10(conv_integer(WEST));
	queue_addr_int_I_00(conv_integer(EAST))<=queue_addr_int_O_10(conv_integer(WEST));

	data_int_I_00(conv_integer(SOUTH))<=data_int_O_02(conv_integer(NORTH));
	size_int_I_00(conv_integer(SOUTH))<=size_int_O_02(conv_integer(NORTH));
	ack_nack_int_I_00(conv_integer(SOUTH))<=ack_nack_int_O_02(conv_integer(NORTH));
	queue_addr_int_I_00(conv_integer(SOUTH))<=queue_addr_int_O_02(conv_integer(NORTH));

	data_int_I_00(conv_integer(LOCAL))<=data_local_I(0);
	data_av_local_int_I_00<=data_av_local_I(0);
	ack_nack_int_I_00(conv_integer(LOCAL))<=ack_nack_local_I(0);
	size_int_I_00(conv_integer(LOCAL))<=size_local_I(0);
	data_av_local_O(0)<=data_av_local_int_O_00;
	data_local_O(0)<=data_int_O_00(conv_integer(LOCAL));
	size_local_O(0)<=size_int_O_00(conv_integer(LOCAL));
	ack_nack_local_O(0)<=ack_nack_int_O_00(conv_integer(LOCAL));

	-- entradas da chave10
	data_int_I_10(conv_integer(WEST))<=data_int_O_00(conv_integer(EAST));
	size_int_I_10(conv_integer(WEST))<=size_int_O_00(conv_integer(EAST));
	ack_nack_int_I_10(conv_integer(WEST))<=ack_nack_int_O_00(conv_integer(EAST));
	queue_addr_int_I_10(conv_integer(WEST))<=queue_addr_int_O_00(conv_integer(EAST));

	data_int_I_10(conv_integer(NORTH))<=data_int_O_11(conv_integer(SOUTH));
	size_int_I_10(conv_integer(NORTH))<=size_int_O_11(conv_integer(SOUTH));
	ack_nack_int_I_10(conv_integer(NORTH))<=ack_nack_int_O_11(conv_integer(SOUTH));
	queue_addr_int_I_10(conv_integer(NORTH))<=queue_addr_int_O_11(conv_integer(SOUTH));

	data_int_I_10(conv_integer(EAST))<=data_int_O_20(conv_integer(WEST));
	size_int_I_10(conv_integer(EAST))<=size_int_O_20(conv_integer(WEST));
	ack_nack_int_I_10(conv_integer(EAST))<=ack_nack_int_O_20(conv_integer(WEST));
	queue_addr_int_I_10(conv_integer(EAST))<=queue_addr_int_O_20(conv_integer(WEST));

	data_int_I_10(conv_integer(SOUTH))<=data_int_O_12(conv_integer(NORTH));
	size_int_I_10(conv_integer(SOUTH))<=size_int_O_12(conv_integer(NORTH));
	ack_nack_int_I_10(conv_integer(SOUTH))<=ack_nack_int_O_12(conv_integer(NORTH));
	queue_addr_int_I_10(conv_integer(SOUTH))<=queue_addr_int_O_12(conv_integer(NORTH));

	data_int_I_10(conv_integer(LOCAL))<=data_local_I(1);
	data_av_local_int_I_10<=data_av_local_I(1);
	ack_nack_int_I_10(conv_integer(LOCAL))<=ack_nack_local_I(1);
	size_int_I_10(conv_integer(LOCAL))<=size_local_I(1);
	data_av_local_O(1)<=data_av_local_int_O_10;
	data_local_O(1)<=data_int_O_10(conv_integer(LOCAL));
	size_local_O(1)<=size_int_O_10(conv_integer(LOCAL));
	ack_nack_local_O(1)<=ack_nack_int_O_10(conv_integer(LOCAL));

	-- entradas da chave20
	data_int_I_20(conv_integer(WEST))<=data_int_O_10(conv_integer(EAST));
	size_int_I_20(conv_integer(WEST))<=size_int_O_10(conv_integer(EAST));
	ack_nack_int_I_20(conv_integer(WEST))<=ack_nack_int_O_10(conv_integer(EAST));
	queue_addr_int_I_20(conv_integer(WEST))<=queue_addr_int_O_10(conv_integer(EAST));

	data_int_I_20(conv_integer(NORTH))<=data_int_O_21(conv_integer(SOUTH));
	size_int_I_20(conv_integer(NORTH))<=size_int_O_21(conv_integer(SOUTH));
	ack_nack_int_I_20(conv_integer(NORTH))<=ack_nack_int_O_21(conv_integer(SOUTH));
	queue_addr_int_I_20(conv_integer(NORTH))<=queue_addr_int_O_21(conv_integer(SOUTH));

	data_int_I_20(conv_integer(EAST))<=data_int_O_00(conv_integer(WEST));
	size_int_I_20(conv_integer(EAST))<=size_int_O_00(conv_integer(WEST));
	ack_nack_int_I_20(conv_integer(EAST))<=ack_nack_int_O_00(conv_integer(WEST));
	queue_addr_int_I_20(conv_integer(EAST))<=queue_addr_int_O_00(conv_integer(WEST));

	data_int_I_20(conv_integer(SOUTH))<=data_int_O_22(conv_integer(NORTH));
	size_int_I_20(conv_integer(SOUTH))<=size_int_O_22(conv_integer(NORTH));
	ack_nack_int_I_20(conv_integer(SOUTH))<=ack_nack_int_O_22(conv_integer(NORTH));
	queue_addr_int_I_20(conv_integer(SOUTH))<=queue_addr_int_O_22(conv_integer(NORTH));

	data_int_I_20(conv_integer(LOCAL))<=data_local_I(2);
	data_av_local_int_I_20<=data_av_local_I(2);
	ack_nack_int_I_20(conv_integer(LOCAL))<=ack_nack_local_I(2);
	size_int_I_20(conv_integer(LOCAL))<=size_local_I(2);
	data_av_local_O(2)<=data_av_local_int_O_20;
	data_local_O(2)<=data_int_O_20(conv_integer(LOCAL));
	size_local_O(2)<=size_int_O_20(conv_integer(LOCAL));
	ack_nack_local_O(2)<=ack_nack_int_O_20(conv_integer(LOCAL));

	-- entradas da chave01
	data_int_I_01(conv_integer(WEST))<=data_int_O_21(conv_integer(EAST));
	size_int_I_01(conv_integer(WEST))<=size_int_O_21(conv_integer(EAST));
	ack_nack_int_I_01(conv_integer(WEST))<=ack_nack_int_O_21(conv_integer(EAST));
	queue_addr_int_I_01(conv_integer(WEST))<=queue_addr_int_O_21(conv_integer(EAST));

	data_int_I_01(conv_integer(NORTH))<=data_int_O_02(conv_integer(SOUTH));
	size_int_I_01(conv_integer(NORTH))<=size_int_O_02(conv_integer(SOUTH));
	ack_nack_int_I_01(conv_integer(NORTH))<=ack_nack_int_O_02(conv_integer(SOUTH));
	queue_addr_int_I_01(conv_integer(NORTH))<=queue_addr_int_O_02(conv_integer(SOUTH));

	data_int_I_01(conv_integer(EAST))<=data_int_O_11(conv_integer(WEST));
	size_int_I_01(conv_integer(EAST))<=size_int_O_11(conv_integer(WEST));
	ack_nack_int_I_01(conv_integer(EAST))<=ack_nack_int_O_11(conv_integer(WEST));
	queue_addr_int_I_01(conv_integer(EAST))<=queue_addr_int_O_11(conv_integer(WEST));

	data_int_I_01(conv_integer(SOUTH))<=data_int_O_00(conv_integer(NORTH));
	size_int_I_01(conv_integer(SOUTH))<=size_int_O_00(conv_integer(NORTH));
	ack_nack_int_I_01(conv_integer(SOUTH))<=ack_nack_int_O_00(conv_integer(NORTH));
	queue_addr_int_I_01(conv_integer(SOUTH))<=queue_addr_int_O_00(conv_integer(NORTH));

	data_int_I_01(conv_integer(LOCAL))<=data_local_I(3);
	data_av_local_int_I_01<=data_av_local_I(3);
	ack_nack_int_I_01(conv_integer(LOCAL))<=ack_nack_local_I(3);
	size_int_I_01(conv_integer(LOCAL))<=size_local_I(3);
	data_av_local_O(3)<=data_av_local_int_O_01;
	data_local_O(3)<=data_int_O_01(conv_integer(LOCAL));
	size_local_O(3)<=size_int_O_01(conv_integer(LOCAL));
	ack_nack_local_O(3)<=ack_nack_int_O_01(conv_integer(LOCAL));

	-- entradas da chave11
	data_int_I_11(conv_integer(WEST))<=data_int_O_01(conv_integer(EAST));
	size_int_I_11(conv_integer(WEST))<=size_int_O_01(conv_integer(EAST));
	ack_nack_int_I_11(conv_integer(WEST))<=ack_nack_int_O_01(conv_integer(EAST));
	queue_addr_int_I_11(conv_integer(WEST))<=queue_addr_int_O_01(conv_integer(EAST));

	data_int_I_11(conv_integer(NORTH))<=data_int_O_12(conv_integer(SOUTH));
	size_int_I_11(conv_integer(NORTH))<=size_int_O_12(conv_integer(SOUTH));
	ack_nack_int_I_11(conv_integer(NORTH))<=ack_nack_int_O_12(conv_integer(SOUTH));
	queue_addr_int_I_11(conv_integer(NORTH))<=queue_addr_int_O_12(conv_integer(SOUTH));

	data_int_I_11(conv_integer(EAST))<=data_int_O_21(conv_integer(WEST));
	size_int_I_11(conv_integer(EAST))<=size_int_O_21(conv_integer(WEST));
	ack_nack_int_I_11(conv_integer(EAST))<=ack_nack_int_O_21(conv_integer(WEST));
	queue_addr_int_I_11(conv_integer(EAST))<=queue_addr_int_O_21(conv_integer(WEST));

	data_int_I_11(conv_integer(SOUTH))<=data_int_O_10(conv_integer(NORTH));
	size_int_I_11(conv_integer(SOUTH))<=size_int_O_10(conv_integer(NORTH));
	ack_nack_int_I_11(conv_integer(SOUTH))<=ack_nack_int_O_10(conv_integer(NORTH));
	queue_addr_int_I_11(conv_integer(SOUTH))<=queue_addr_int_O_10(conv_integer(NORTH));

	data_int_I_11(conv_integer(LOCAL))<=data_local_I(4);
	data_av_local_int_I_11<=data_av_local_I(4);
	ack_nack_int_I_11(conv_integer(LOCAL))<=ack_nack_local_I(4);
	size_int_I_11(conv_integer(LOCAL))<=size_local_I(4);
	data_av_local_O(4)<=data_av_local_int_O_11;
	data_local_O(4)<=data_int_O_11(conv_integer(LOCAL));
	size_local_O(4)<=size_int_O_11(conv_integer(LOCAL));
	ack_nack_local_O(4)<=ack_nack_int_O_11(conv_integer(LOCAL));

	-- entradas da chave21
	data_int_I_21(conv_integer(WEST))<=data_int_O_11(conv_integer(EAST));
	size_int_I_21(conv_integer(WEST))<=size_int_O_11(conv_integer(EAST));
	ack_nack_int_I_21(conv_integer(WEST))<=ack_nack_int_O_11(conv_integer(EAST));
	queue_addr_int_I_21(conv_integer(WEST))<=queue_addr_int_O_11(conv_integer(EAST));

	data_int_I_21(conv_integer(NORTH))<=data_int_O_22(conv_integer(SOUTH));
	size_int_I_21(conv_integer(NORTH))<=size_int_O_22(conv_integer(SOUTH));
	ack_nack_int_I_21(conv_integer(NORTH))<=ack_nack_int_O_22(conv_integer(SOUTH));
	queue_addr_int_I_21(conv_integer(NORTH))<=queue_addr_int_O_22(conv_integer(SOUTH));

	data_int_I_21(conv_integer(EAST))<=data_int_O_01(conv_integer(WEST));
	size_int_I_21(conv_integer(EAST))<=size_int_O_01(conv_integer(WEST));
	ack_nack_int_I_21(conv_integer(EAST))<=ack_nack_int_O_01(conv_integer(WEST));
	queue_addr_int_I_21(conv_integer(EAST))<=queue_addr_int_O_01(conv_integer(WEST));

	data_int_I_21(conv_integer(SOUTH))<=data_int_O_20(conv_integer(NORTH));
	size_int_I_21(conv_integer(SOUTH))<=size_int_O_20(conv_integer(NORTH));
	ack_nack_int_I_21(conv_integer(SOUTH))<=ack_nack_int_O_20(conv_integer(NORTH));
	queue_addr_int_I_21(conv_integer(SOUTH))<=queue_addr_int_O_20(conv_integer(NORTH));

	data_int_I_21(conv_integer(LOCAL))<=data_local_I(5);
	data_av_local_int_I_21<=data_av_local_I(5);
	ack_nack_int_I_21(conv_integer(LOCAL))<=ack_nack_local_I(5);
	size_int_I_21(conv_integer(LOCAL))<=size_local_I(5);
	data_av_local_O(5)<=data_av_local_int_O_21;
	data_local_O(5)<=data_int_O_21(conv_integer(LOCAL));
	size_local_O(5)<=size_int_O_21(conv_integer(LOCAL));
	ack_nack_local_O(5)<=ack_nack_int_O_21(conv_integer(LOCAL));

	-- entradas da chave02
	data_int_I_02(conv_integer(WEST))<=data_int_O_22(conv_integer(EAST));
	size_int_I_02(conv_integer(WEST))<=size_int_O_22(conv_integer(EAST));
	ack_nack_int_I_02(conv_integer(WEST))<=ack_nack_int_O_22(conv_integer(EAST));
	queue_addr_int_I_02(conv_integer(WEST))<=queue_addr_int_O_22(conv_integer(EAST));

	data_int_I_02(conv_integer(NORTH))<=data_int_O_00(conv_integer(SOUTH));
	size_int_I_02(conv_integer(NORTH))<=size_int_O_00(conv_integer(SOUTH));
	ack_nack_int_I_02(conv_integer(NORTH))<=ack_nack_int_O_00(conv_integer(SOUTH));
	queue_addr_int_I_02(conv_integer(NORTH))<=queue_addr_int_O_00(conv_integer(SOUTH));

	data_int_I_02(conv_integer(EAST))<=data_int_O_12(conv_integer(WEST));
	size_int_I_02(conv_integer(EAST))<=size_int_O_12(conv_integer(WEST));
	ack_nack_int_I_02(conv_integer(EAST))<=ack_nack_int_O_12(conv_integer(WEST));
	queue_addr_int_I_02(conv_integer(EAST))<=queue_addr_int_O_12(conv_integer(WEST));

	data_int_I_02(conv_integer(SOUTH))<=data_int_O_01(conv_integer(NORTH));
	size_int_I_02(conv_integer(SOUTH))<=size_int_O_01(conv_integer(NORTH));
	ack_nack_int_I_02(conv_integer(SOUTH))<=ack_nack_int_O_01(conv_integer(NORTH));
	queue_addr_int_I_02(conv_integer(SOUTH))<=queue_addr_int_O_01(conv_integer(NORTH));

	data_int_I_02(conv_integer(LOCAL))<=data_local_I(6);
	data_av_local_int_I_02<=data_av_local_I(6);
	ack_nack_int_I_02(conv_integer(LOCAL))<=ack_nack_local_I(6);
	size_int_I_02(conv_integer(LOCAL))<=size_local_I(6);
	data_av_local_O(6)<=data_av_local_int_O_02;
	data_local_O(6)<=data_int_O_02(conv_integer(LOCAL));
	size_local_O(6)<=size_int_O_02(conv_integer(LOCAL));
	ack_nack_local_O(6)<=ack_nack_int_O_02(conv_integer(LOCAL));

	-- entradas da chave12
	data_int_I_12(conv_integer(WEST))<=data_int_O_02(conv_integer(EAST));
	size_int_I_12(conv_integer(WEST))<=size_int_O_02(conv_integer(EAST));
	ack_nack_int_I_12(conv_integer(WEST))<=ack_nack_int_O_02(conv_integer(EAST));
	queue_addr_int_I_12(conv_integer(WEST))<=queue_addr_int_O_02(conv_integer(EAST));

	data_int_I_12(conv_integer(NORTH))<=data_int_O_10(conv_integer(SOUTH));
	size_int_I_12(conv_integer(NORTH))<=size_int_O_10(conv_integer(SOUTH));
	ack_nack_int_I_12(conv_integer(NORTH))<=ack_nack_int_O_10(conv_integer(SOUTH));
	queue_addr_int_I_12(conv_integer(NORTH))<=queue_addr_int_O_10(conv_integer(SOUTH));

	data_int_I_12(conv_integer(EAST))<=data_int_O_22(conv_integer(WEST));
	size_int_I_12(conv_integer(EAST))<=size_int_O_22(conv_integer(WEST));
	ack_nack_int_I_12(conv_integer(EAST))<=ack_nack_int_O_22(conv_integer(WEST));
	queue_addr_int_I_12(conv_integer(EAST))<=queue_addr_int_O_22(conv_integer(WEST));

	data_int_I_12(conv_integer(SOUTH))<=data_int_O_11(conv_integer(NORTH));
	size_int_I_12(conv_integer(SOUTH))<=size_int_O_11(conv_integer(NORTH));
	ack_nack_int_I_12(conv_integer(SOUTH))<=ack_nack_int_O_11(conv_integer(NORTH));
	queue_addr_int_I_12(conv_integer(SOUTH))<=queue_addr_int_O_11(conv_integer(NORTH));

	data_int_I_12(conv_integer(LOCAL))<=data_local_I(7);
	data_av_local_int_I_12<=data_av_local_I(7);
	ack_nack_int_I_12(conv_integer(LOCAL))<=ack_nack_local_I(7);
	size_int_I_12(conv_integer(LOCAL))<=size_local_I(7);
	data_av_local_O(7)<=data_av_local_int_O_12;
	data_local_O(7)<=data_int_O_12(conv_integer(LOCAL));
	size_local_O(7)<=size_int_O_12(conv_integer(LOCAL));
	ack_nack_local_O(7)<=ack_nack_int_O_12(conv_integer(LOCAL));

	-- entradas da chave22
	data_int_I_22(conv_integer(WEST))<=data_int_O_12(conv_integer(EAST));
	size_int_I_22(conv_integer(WEST))<=size_int_O_12(conv_integer(EAST));
	ack_nack_int_I_22(conv_integer(WEST))<=ack_nack_int_O_12(conv_integer(EAST));
	queue_addr_int_I_22(conv_integer(WEST))<=queue_addr_int_O_12(conv_integer(EAST));

	data_int_I_22(conv_integer(NORTH))<=data_int_O_20(conv_integer(SOUTH));
	size_int_I_22(conv_integer(NORTH))<=size_int_O_20(conv_integer(SOUTH));
	ack_nack_int_I_22(conv_integer(NORTH))<=ack_nack_int_O_20(conv_integer(SOUTH));
	queue_addr_int_I_22(conv_integer(NORTH))<=queue_addr_int_O_20(conv_integer(SOUTH));

	data_int_I_22(conv_integer(EAST))<=data_int_O_02(conv_integer(WEST));
	size_int_I_22(conv_integer(EAST))<=size_int_O_02(conv_integer(WEST));
	ack_nack_int_I_22(conv_integer(EAST))<=ack_nack_int_O_02(conv_integer(WEST));
	queue_addr_int_I_22(conv_integer(EAST))<=queue_addr_int_O_02(conv_integer(WEST));

	data_int_I_22(conv_integer(SOUTH))<=data_int_O_21(conv_integer(NORTH));
	size_int_I_22(conv_integer(SOUTH))<=size_int_O_21(conv_integer(NORTH));
	ack_nack_int_I_22(conv_integer(SOUTH))<=ack_nack_int_O_21(conv_integer(NORTH));
	queue_addr_int_I_22(conv_integer(SOUTH))<=queue_addr_int_O_21(conv_integer(NORTH));

	data_int_I_22(conv_integer(LOCAL))<=data_local_I(8);
	data_av_local_int_I_22<=data_av_local_I(8);
	ack_nack_int_I_22(conv_integer(LOCAL))<=ack_nack_local_I(8);
	size_int_I_22(conv_integer(LOCAL))<=size_local_I(8);
	data_av_local_O(8)<=data_av_local_int_O_22;
	data_local_O(8)<=data_int_O_22(conv_integer(LOCAL));
	size_local_O(8)<=size_int_O_22(conv_integer(LOCAL));
	ack_nack_local_O(8)<=ack_nack_int_O_22(conv_integer(LOCAL));
end NOC;