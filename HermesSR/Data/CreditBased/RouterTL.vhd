library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;
use work.HermesPackage.all;

entity RouterTL is
generic( address: regmetadeflit := (others=>'0'));
port(
	clock:     in  std_logic;
	reset:     in  std_logic;
	clock_rx:  in  regNport;
	rx:        in  regNport;
	data_in:   in  arrayNport_regflit;
	credit_o:  out regNport;    
	clock_tx:  out regNport;
	tx:        out regNport;
	data_out:  out arrayNport_regflit;
	credit_i:  in  regNport);
end RouterTL;

architecture RouterTL of RouterTL is

signal 	rr_i_East,  rr_o_East,
				--rr_i_West,  rr_o_West,
				--rr_i_North,  rr_o_North,
				rr_i_South,  rr_o_South,
				rr_i_Local,  rr_o_Local: regNport;
		
signal 	ack_rr_o_East, 
				--ack_rr_o_West, 
				--ack_rr_o_North, 
				ack_rr_o_South, 
				ack_rr_o_Local: regNport;		
				
signal 	ack_rr_i_East, 
				--ack_rr_i_West, 
				--ack_rr_i_North, 
				ack_rr_i_South, 
				ack_rr_i_Local: std_logic;
		
signal 	data_av_i_East,
				--data_av_i_West,
				--data_av_i_North,
				data_av_i_South,
				data_av_i_Local: regNport;
		
signal 	data_av_o_East,
				--data_av_o_West,
				--data_av_o_North,
				data_av_o_South,
				data_av_o_Local: std_logic;
		
signal 	data_i_East, 
				--data_i_West, 
				--data_i_North, 
				data_i_South, 
				data_i_Local: arrayNport_regflit;
				
signal 	data_o_East, 
				--data_o_West, 
				--data_o_North, 
				data_o_South, 
				data_o_Local: regflit;
		
signal 	data_ack_o_East, 
				--data_ack_o_West, 
				--data_ack_o_North, 
				data_ack_o_South, 
				data_ack_o_Local: regNport;
		
signal 	data_ack_i_East, 
				--data_ack_i_West, 
				--data_ack_i_North, 
				data_ack_i_South, 
				data_ack_i_Local: std_logic;
		
signal 	EOP_i_East,
				--EOP_i_West, 
				--EOP_i_North,
				EOP_i_South,
				EOP_i_Local: regNport;

signal 	EOP_o_East,
				--EOP_o_West,
				--EOP_o_North,
				EOP_o_South,
				EOP_o_Local: std_logic;

begin

	IEast : Entity work.Hermes_inport
	port map(
		clock => clock,
		reset => reset,
		
		clock_rx => clock_rx(EAST),
		rx => rx(EAST),
		data_in => data_in(EAST),
		credit_o => credit_o(EAST),

		rr => rr_o_East,
		ack_rr => ack_rr_i_East,
		
		data_av => data_av_o_East,
		data => data_o_East,
		data_ack => data_ack_i_East,
		EOP => EOP_o_East);

	-- IWest : Entity work.Hermes_inport
	-- port map(
		-- clock => clock,
		-- reset => reset,
		
		-- clock_rx => clock_rx(WEST),
		-- rx => rx(WEST),
		-- data_in => data_in(WEST),
		-- credit_o => credit_o(WEST),

		-- ack_rr => ack_rr_i_West,
		-- rr => rr_o_West,
		
		-- data_av => data_av_o_West,
		-- data => data_o_West,
		-- data_ack => data_ack_i_West,
		-- EOP => EOP_o_West);

	-- INorth : Entity work.Hermes_inport
	-- port map(
		-- clock => clock,
		-- reset => reset,
		
		-- clock_rx => clock_rx(NORTH),
		-- rx => rx(NORTH),
		-- data_in => data_in(NORTH),
		-- credit_o => credit_o(NORTH),

		-- rr => rr_o_North,
		-- ack_rr => ack_rr_i_North,
		
		-- data_av => data_av_o_North,
		-- data => data_o_North,
		-- data_ack => data_ack_i_North,
		-- EOP => EOP_o_North);

	ISouth : Entity work.Hermes_inport
	port map(
		clock => clock,
		reset => reset,
		
		clock_rx => clock_rx(SOUTH),
		rx => rx(SOUTH),
		data_in => data_in(SOUTH),
		credit_o => credit_o(SOUTH),

		rr => rr_o_South,
		ack_rr => ack_rr_i_South,
		
		data_av => data_av_o_South,
		data => data_o_South,
		data_ack => data_ack_i_South,
		EOP => EOP_o_South);

	ILocal : Entity work.Hermes_inport
	port map(
		clock => clock,
		reset => reset,
		
		clock_rx => clock_rx(LOCAL),
		rx => rx(LOCAL),
		data_in => data_in(LOCAL),
		credit_o => credit_o(LOCAL),

		rr => rr_o_Local,
		ack_rr => ack_rr_i_Local,
		
		data_av => data_av_o_Local,
		data => data_o_Local,
		data_ack => data_ack_i_Local,
		EOP => EOP_o_Local);

	OEast : Entity work.Hermes_outport
	port map(
		clock => clock,
		reset => reset,
		
		rr => rr_i_East,
		ack_rr => ack_rr_o_East,

		data_av => data_av_i_East,
		data => data_i_East,
		data_ack => data_ack_o_East,
		EOP => EOP_i_East,

		clock_tx => clock_tx(EAST),
		tx => tx(EAST),
		data_out => data_out(EAST),
		credit_i => credit_i(EAST));

	-- OWest : Entity work.Hermes_outport
	-- port map(
		-- clock => clock,
		-- reset => reset,
		
		-- rr => rr_i_West,
		-- ack_rr => ack_rr_o_West,

		-- data_av => data_av_i_West,
		-- data => data_i_West,
		-- data_ack => data_ack_o_West,
		-- EOP => EOP_i_West,

		-- clock_tx => clock_tx(WEST),
		-- tx => tx(WEST),
		-- data_out => data_out(WEST),
		-- credit_i => credit_i(WEST));

	-- ONorth : Entity work.Hermes_outport
	-- port map(
		-- clock => clock,
		-- reset => reset,
		
		-- rr => rr_i_North,
		-- ack_rr => ack_rr_o_North,

		-- data_av => data_av_i_North,
		-- data => data_i_North,
		-- data_ack => data_ack_o_North,
		-- EOP => EOP_i_North,

		-- clock_tx => clock_tx(NORTH),
		-- tx => tx(NORTH),
		-- data_out => data_out(NORTH),
		-- credit_i => credit_i(NORTH));

	OSouth : Entity work.Hermes_outport
	port map(
		clock => clock,
		reset => reset,
		
		rr => rr_i_South,
		ack_rr => ack_rr_o_South,

		data_av => data_av_i_South,
		data => data_i_South,
		data_ack => data_ack_o_South,
		EOP => EOP_i_South,

		clock_tx => clock_tx(SOUTH),
		tx => tx(SOUTH),
		data_out => data_out(SOUTH),
		credit_i => credit_i(SOUTH));

	OLocal : Entity work.Hermes_outport
	port map(
		clock => clock,
		reset => reset,
		
		rr => rr_i_Local,
		ack_rr => ack_rr_o_Local,

		data_av => data_av_i_Local,
		data => data_i_Local,
		data_ack => data_ack_o_Local,
		EOP => EOP_i_Local,

		clock_tx => clock_tx(LOCAL),
		tx => tx(LOCAL),
		data_out => data_out(LOCAL),
		credit_i => credit_i(LOCAL));
		
	-- Ligando sinal rr
	rr_i_East(EAST) <= rr_o_East(EAST);
	--rr_i_West(EAST) <= rr_o_East(WEST);
	--rr_i_North(EAST) <= rr_o_East(NORTH);
	rr_i_South(EAST) <= rr_o_East(SOUTH);
	rr_i_Local(EAST) <= rr_o_East(LOCAL);

	rr_i_East(WEST) <= '0';
	--rr_i_West(WEST) <= rr_o_West(WEST);
	--rr_i_North(WEST) <= rr_o_West(NORTH);
	rr_i_South(WEST) <= '0';
	rr_i_Local(WEST) <= '0';

	rr_i_East(NORTH) <= '0';
	--rr_i_West(NORTH) <= rr_o_North(WEST);
	--rr_i_North(NORTH) <= rr_o_North(NORTH);
	rr_i_South(NORTH) <= '0';
	rr_i_Local(NORTH) <= '0';

	rr_i_East(SOUTH) <= rr_o_South(EAST);
	--rr_i_West(SOUTH) <= rr_o_South(WEST);
	--rr_i_North(SOUTH) <= rr_o_South(NORTH);
	rr_i_South(SOUTH) <= rr_o_South(SOUTH);
	rr_i_Local(SOUTH) <= rr_o_South(LOCAL);

	rr_i_East(LOCAL) <= rr_o_Local(EAST);
	--rr_i_West(LOCAL) <= rr_o_Local(WEST);
	--rr_i_North(LOCAL) <= rr_o_Local(NORTH);
	rr_i_South(LOCAL) <= rr_o_Local(SOUTH);
	rr_i_Local(LOCAL) <= rr_o_Local(LOCAL);

	-- Ligando sinal ack_rr
	ack_rr_i_East  <= ack_rr_o_East(EAST)  or '0'  or '0'  or ack_rr_o_South(EAST)  or ack_rr_o_Local(EAST);
	--ack_rr_i_West  <= ack_rr_o_East(WEST)  or ack_rr_o_West(WEST)  or ack_rr_o_North(WEST)  or ack_rr_o_South(WEST)  or ack_rr_o_Local(WEST);
	--ack_rr_i_North <= ack_rr_o_East(NORTH) or ack_rr_o_West(NORTH) or ack_rr_o_North(NORTH) or ack_rr_o_South(NORTH) or ack_rr_o_Local(NORTH);
	ack_rr_i_South <= ack_rr_o_East(SOUTH) or '0' or '0' or ack_rr_o_South(SOUTH) or ack_rr_o_Local(SOUTH);
	ack_rr_i_Local <= ack_rr_o_East(LOCAL) or '0' or '0' or ack_rr_o_South(LOCAL) or ack_rr_o_Local(LOCAL);

	-- Ligando sinal data_av
	data_av_i_East(EAST) <= data_av_o_East;
	--data_av_i_West(EAST) <= data_av_o_East;
	--data_av_i_North(EAST) <= data_av_o_East;
	data_av_i_South(EAST) <= data_av_o_East;
	data_av_i_Local(EAST) <= data_av_o_East;

	data_av_i_East(WEST) <= '0';
	--data_av_i_West(WEST) <= data_av_o_West;
	--data_av_i_North(WEST) <= data_av_o_West;
	data_av_i_South(WEST) <= '0';
	data_av_i_Local(WEST) <= '0';

	data_av_i_East(NORTH) <= '0';
	--data_av_i_West(NORTH) <= data_av_o_North;
	--data_av_i_North(NORTH) <= data_av_o_North;
	data_av_i_South(NORTH) <= '0';
	data_av_i_Local(NORTH) <= '0';

	data_av_i_East(SOUTH) <= data_av_o_South;
	--data_av_i_West(SOUTH) <= data_av_o_South;
	--data_av_i_North(SOUTH) <= data_av_o_South;
	data_av_i_South(SOUTH) <= data_av_o_South;
	data_av_i_Local(SOUTH) <= data_av_o_South;

	data_av_i_East(LOCAL) <= data_av_o_Local;
	--data_av_i_West(LOCAL) <= data_av_o_Local;
	--data_av_i_North(LOCAL) <= data_av_o_Local;
	data_av_i_South(LOCAL) <= data_av_o_Local;
	data_av_i_Local(LOCAL) <= data_av_o_Local;

	-- ligando sinal data
	data_i_East(EAST) <= data_o_East;
	--data_i_West(EAST) <= data_o_East(WEST);
	--data_i_North(EAST) <= data_o_East(NORTH);
	data_i_South(EAST) <= data_o_East;
	data_i_Local(EAST) <= data_o_East;

	data_i_East(WEST) <= (others=>'0');
	--data_i_West(WEST) <= data_o_West(WEST);
	--data_i_North(WEST) <= data_o_West(NORTH);
	data_i_South(WEST) <= (others=>'0');
	data_i_Local(WEST) <= (others=>'0');

	data_i_East(NORTH) <= (others=>'0');
	--data_i_West(NORTH) <= data_o_North(WEST);
	--data_i_North(NORTH) <= data_o_North(NORTH);
	data_i_South(NORTH) <= (others=>'0');
	data_i_Local(NORTH) <= (others=>'0');

	data_i_East(SOUTH) <= data_o_South;
	--data_i_West(SOUTH) <= data_o_South(WEST);
	--data_i_North(SOUTH) <= data_o_South(NORTH);
	data_i_South(SOUTH) <= data_o_South;
	data_i_Local(SOUTH) <= data_o_South;

	data_i_East(LOCAL) <= data_o_Local;
	--data_i_West(LOCAL) <= data_o_Local(WEST);
	--data_i_North(LOCAL) <= data_o_Local(NORTH);
	data_i_South(LOCAL) <= data_o_Local;
	data_i_Local(LOCAL) <= data_o_Local;
	
	-- ligando sinal data_ack
	data_ack_i_East  <= data_ack_o_East(EAST)  or '0'  or '0'  or data_ack_o_South(EAST)  or data_ack_o_Local(EAST);
	--data_ack_i_West  <= data_ack_o_East(WEST)  or data_ack_o_West(WEST)  or data_ack_o_North(WEST)  or data_ack_o_South(WEST)  or data_ack_o_Local(WEST);
	--data_ack_i_North <= data_ack_o_East(NORTH) or data_ack_o_West(NORTH) or data_ack_o_North(NORTH) or data_ack_o_South(NORTH) or data_ack_o_Local(NORTH);
	data_ack_i_South <= data_ack_o_East(SOUTH) or '0' or '0' or data_ack_o_South(SOUTH) or data_ack_o_Local(SOUTH);
	data_ack_i_Local <= data_ack_o_East(LOCAL) or '0' or '0' or data_ack_o_South(LOCAL) or data_ack_o_Local(LOCAL);

	-- ligando o sinal EOP
	EOP_i_East(EAST) <= EOP_o_East;
	--EOP_i_West(EAST) <= EOP_o_East;
	--EOP_i_North(EAST) <= EOP_o_East;
	EOP_i_South(EAST) <= EOP_o_East;
	EOP_i_Local(EAST) <= EOP_o_East;

	EOP_i_East(WEST) <= '0';
	--EOP_i_West(WEST) <= EOP_o_West;
	--EOP_i_North(WEST) <= EOP_o_West;
	EOP_i_South(WEST) <= '0';
	EOP_i_Local(WEST) <= '0';

	EOP_i_East(NORTH) <= '0';
	--EOP_i_West(NORTH) <= EOP_o_North;
	--EOP_i_North(NORTH) <= EOP_o_North;
	EOP_i_South(NORTH) <= '0';
	EOP_i_Local(NORTH) <= '0';

	EOP_i_East(SOUTH) <= EOP_o_South;
	--EOP_i_West(SOUTH) <= EOP_o_South;
	--EOP_i_North(SOUTH) <= EOP_o_South;
	EOP_i_South(SOUTH) <= EOP_o_South;
	EOP_i_Local(SOUTH) <= EOP_o_South;

	EOP_i_East(LOCAL) <= EOP_o_Local;
	--EOP_i_West(LOCAL) <= EOP_o_Local;
	--EOP_i_North(LOCAL) <= EOP_o_Local;
	EOP_i_South(LOCAL) <= EOP_o_Local;
	EOP_i_Local(LOCAL) <= EOP_o_Local;

end RouterTL;
