library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;
use work.Hermes_Package.all;

entity RouterBC is
port(
	clock:    in  std_logic;
	reset:    in  std_logic;
	
	clock_rx: in  regNport;
	rx:       in  regNport;
	lane_rx:  in  arrayNport_regNlane;
	data_in:  in  arrayNport_regflit;
	credit_o: out arrayNport_regNlane;
	
	clock_tx: out regNport;
	tx:       out regNport;
	lane_tx:	out arrayNport_regNlane;
	data_out: out arrayNport_regflit;
	credit_i: in  arrayNport_regNlane);
end RouterBC;

architecture RouterBC of RouterBC is

-- Sinais para interconexão das portas de entrada e saída do roteador
signal 	rr_sgn_i_East:  arrayNlane_regNport;
signal 	rr_sgn_i_North: arrayNlane_regNport;
signal 	rr_sgn_i_West:  arrayNlane_regNport;
-- signal 	rr_sgn_i_South: arrayNlane_regNport;
signal 	rr_sgn_i_Local: arrayNlane_regNport;

signal 	ack_rr_sgn_i: arrayNport_regNlane;

signal 	data_av_sgn_i_East:  arrayNlane_regNport;
signal 	data_av_sgn_i_North: arrayNlane_regNport;
signal 	data_av_sgn_i_West:  arrayNlane_regNport;
-- signal 	data_av_sgn_i_South: arrayNlane_regNport;
signal 	data_av_sgn_i_Local: arrayNlane_regNport;

signal 	data_sgn_i: matrixNport_Nlane_regflit;

signal 	data_ack_sgn_i:  arrayNport_regNlane;

signal 	EOP_sgn_i_East:  arrayNlane_regNport;
signal 	EOP_sgn_i_North:  arrayNlane_regNport;
signal 	EOP_sgn_i_West: arrayNlane_regNport;
-- signal 	EOP_sgn_i_South:  arrayNlane_regNport;
signal 	EOP_sgn_i_Local: arrayNlane_regNport;

signal 	rr_sgn_o_East:  arrayNlane_regNport;
signal 	rr_sgn_o_North: arrayNlane_regNport;
signal 	rr_sgn_o_West:  arrayNlane_regNport;
-- signal 	rr_sgn_o_South: arrayNlane_regNport;
signal 	rr_sgn_o_Local: arrayNlane_regNport;

signal 	ack_rr_sgn_o_East:  arrayNlane_regNport;
signal 	ack_rr_sgn_o_North: arrayNlane_regNport;
signal 	ack_rr_sgn_o_West:  arrayNlane_regNport;
-- signal 	ack_rr_sgn_o_South: arrayNlane_regNport;
signal 	ack_rr_sgn_o_Local: arrayNlane_regNport;

signal 	data_av_sgn_o_East:  arrayNlane_regNport;
signal 	data_av_sgn_o_North: arrayNlane_regNport;
signal 	data_av_sgn_o_West:  arrayNlane_regNport;
-- signal 	data_av_sgn_o_South: arrayNlane_regNport;
signal 	data_av_sgn_o_Local: arrayNlane_regNport;

signal 	data_sgn_o_East: matrixNlane_Nport_regflit;
signal 	data_sgn_o_North: matrixNlane_Nport_regflit;
signal 	data_sgn_o_West: matrixNlane_Nport_regflit;
-- signal 	data_sgn_o_South: matrixNlane_Nport_regflit;
signal 	data_sgn_o_Local: matrixNlane_Nport_regflit;

signal 	data_ack_sgn_o_East:  arrayNlane_regNport;
signal 	data_ack_sgn_o_North:  arrayNlane_regNport;
signal 	data_ack_sgn_o_West:  arrayNlane_regNport;
-- signal 	data_ack_sgn_o_South:  arrayNlane_regNport;
signal 	data_ack_sgn_o_Local:  arrayNlane_regNport;

signal 	EOP_sgn_o_East:  arrayNlane_regNport;
signal 	EOP_sgn_o_North:  arrayNlane_regNport;
signal 	EOP_sgn_o_West: arrayNlane_regNport;
-- signal 	EOP_sgn_o_South:  arrayNlane_regNport;
signal 	EOP_sgn_o_Local: arrayNlane_regNport;

begin

	IEast : Entity work.Hermes_inport(Hermes_inport)
	port map(
		clock=>clock,
		reset=>reset,

		clock_rx=>clock_rx(EAST),
		rx=>rx(EAST),
		data_in=>data_in(EAST),
		lane_rx=>lane_rx(EAST),
		credit_o=>credit_o(EAST),

		rr=>rr_sgn_i_east,
		ack_rr=>ack_rr_sgn_i(EAST),

		data_av=>data_av_sgn_i_east,
		data=>data_sgn_i(EAST),
		data_ack=>data_ack_sgn_i(EAST),
		eop=>EOP_sgn_i_east
	);


	INorth : Entity work.Hermes_inport(Hermes_inport)
	port map(
		clock=>clock,
		reset=>reset,

		clock_rx=>clock_rx(NORTH),
		rx=>rx(NORTH),
		data_in=>data_in(NORTH),
		lane_rx=>lane_rx(NORTH),
		credit_o=>credit_o(NORTH),

		rr=>rr_sgn_i_north,
		ack_rr=>ack_rr_sgn_i(NORTH),

		data_av=>data_av_sgn_i_north,
		data=>data_sgn_i(NORTH),
		data_ack=>data_ack_sgn_i(NORTH),
		eop=>EOP_sgn_i_north
	);


	IWEST : Entity work.Hermes_inport(Hermes_inport)
	port map(
		clock=>clock,
		reset=>reset,

		clock_rx=>clock_rx(WEST),
		rx=>rx(WEST),
		data_in=>data_in(WEST),
		lane_rx=>lane_rx(WEST),
		credit_o=>credit_o(WEST),

		rr=>rr_sgn_i_west,
		ack_rr=>ack_rr_sgn_i(WEST),

		data_av=>data_av_sgn_i_west,
		data=>data_sgn_i(WEST),
		data_ack=>data_ack_sgn_i(WEST),
		eop=>EOP_sgn_i_west
	);


	-- ISouth : Entity work.Hermes_inport(Hermes_inport)
	-- port map(
		-- clock=>clock,
		-- reset=>reset,

		-- clock_rx=>clock_rx(SOUTH),
		-- rx=>rx(SOUTH),
		-- data_in=>data_in(SOUTH),
		-- lane_rx=>lane_rx(SOUTH),
		-- credit_o=>credit_o(SOUTH),

		-- rr=>rr_sgn_i_south,
		-- ack_rr=>ack_rr_sgn_i(SOUTH),

		-- data_av=>data_av_sgn_i_south,
		-- data=>data_sgn_i(SOUTH),
		-- data_ack=>data_ack_sgn_i(SOUTH),
		-- eop=>EOP_sgn_i_south
	-- );


	ILocal : Entity work.Hermes_inport(Hermes_inport)
	port map(
		clock=>clock,
		reset=>reset,

		clock_rx=>clock_rx(LOCAL),
		rx=>rx(LOCAL),
		data_in=>data_in(LOCAL),
		lane_rx=>lane_rx(LOCAL),
		credit_o=>credit_o(LOCAL),

		rr=>rr_sgn_i_local,
		ack_rr=>ack_rr_sgn_i(LOCAL),

		data_av=>data_av_sgn_i_local,
		data=>data_sgn_i(LOCAL),
		data_ack=>data_ack_sgn_i(LOCAL),
		eop=>EOP_sgn_i_local
	);

	OEast : Entity work.Hermes_outport
	port map(
		clock => clock,
		reset => reset,
		
		rr => rr_sgn_o_East,
		ack_rr => ack_rr_sgn_o_East,

		data_av => data_av_sgn_o_East,
		data => data_sgn_o_East,
		data_ack => data_ack_sgn_o_East,
		EOP => EOP_sgn_o_East,

		clock_tx => clock_tx(EAST),
		tx => tx(EAST),
		lane_tx => lane_tx(EAST),
		data_out => data_out(EAST),
		credit_i => credit_i(EAST)
	);
	
	ONorth : Entity work.Hermes_outport
	port map(
		clock => clock,
		reset => reset,
		
		rr => rr_sgn_o_North,
		ack_rr => ack_rr_sgn_o_North,

		data_av => data_av_sgn_o_North,
		data => data_sgn_o_North,
		data_ack => data_ack_sgn_o_North,
		EOP => EOP_sgn_o_North,

		clock_tx => clock_tx(NORTH),
		tx => tx(NORTH),
		lane_tx => lane_tx(NORTH),
		data_out => data_out(NORTH),
		credit_i => credit_i(NORTH)
	);
	
	OWest : Entity work.Hermes_outport
	port map(
		clock => clock,
		reset => reset,
		
		rr => rr_sgn_o_West,
		ack_rr => ack_rr_sgn_o_West,

		data_av => data_av_sgn_o_West,
		data => data_sgn_o_West,
		data_ack => data_ack_sgn_o_West,
		EOP => EOP_sgn_o_West,

		clock_tx => clock_tx(WEST),
		tx => tx(WEST),
		lane_tx => lane_tx(WEST),
		data_out => data_out(WEST),
		credit_i => credit_i(WEST)
	);
	
	-- OSouth : Entity work.Hermes_outport
	-- port map(
		-- clock => clock,
		-- reset => reset,
		
		-- rr => rr_sgn_o_South,
		-- ack_rr => ack_rr_sgn_o_South,

		-- data_av => data_av_sgn_o_South,
		-- data => data_sgn_o_South,
		-- data_ack => data_ack_sgn_o_South,
		-- EOP => EOP_sgn_o_South,

		-- clock_tx => clock_tx(SOUTH),
		-- tx => tx(SOUTH),
		-- lane_tx => lane_tx(SOUTH),
		-- data_out => data_out(SOUTH),
		-- credit_i => credit_i(SOUTH)
	-- );
	
	OLocal : Entity work.Hermes_outport
	port map(
		clock => clock,
		reset => reset,
		
		rr => rr_sgn_o_Local,
		ack_rr => ack_rr_sgn_o_Local,

		data_av => data_av_sgn_o_Local,
		data => data_sgn_o_Local,
		data_ack => data_ack_sgn_o_Local,
		EOP => EOP_sgn_o_Local,

		clock_tx => clock_tx(LOCAL),
		tx => tx(LOCAL),
		lane_tx => lane_tx(LOCAL),
		data_out => data_out(LOCAL),
		credit_i => credit_i(LOCAL)
	);

	-- Ligação entre as portas do roteador
	-- SINAL DE REQUISIÇÃO DE ROTEAMENTO - RR
	---- EAST ----
	rr_sgn_o_East(ID_CV0)(EAST) <= rr_sgn_i_East(ID_CV0)(EAST);
	rr_sgn_o_East(ID_CV0)(WEST) <= rr_sgn_i_West(ID_CV0)(EAST);
	rr_sgn_o_East(ID_CV0)(NORTH) <= rr_sgn_i_North(ID_CV0)(EAST);
	-- rr_sgn_o_East(ID_CV0)(SOUTH) <= rr_sgn_i_South(ID_CV0)(EAST);
	rr_sgn_o_East(ID_CV0)(LOCAL) <= rr_sgn_i_Local(ID_CV0)(EAST);
	
	rr_sgn_o_East(ID_CV1)(EAST) <= rr_sgn_i_East(ID_CV1)(EAST);
	rr_sgn_o_East(ID_CV1)(WEST) <= rr_sgn_i_West(ID_CV1)(EAST);
	rr_sgn_o_East(ID_CV1)(NORTH) <= rr_sgn_i_North(ID_CV1)(EAST);
	-- rr_sgn_o_East(ID_CV1)(SOUTH) <= rr_sgn_i_South(ID_CV1)(EAST);
	rr_sgn_o_East(ID_CV1)(LOCAL) <= rr_sgn_i_Local(ID_CV1)(EAST);
	
	rr_sgn_o_East(ID_CV2)(EAST) <= rr_sgn_i_East(ID_CV2)(EAST);
	rr_sgn_o_East(ID_CV2)(WEST) <= rr_sgn_i_West(ID_CV2)(EAST);
	rr_sgn_o_East(ID_CV2)(NORTH) <= rr_sgn_i_North(ID_CV2)(EAST);
	-- rr_sgn_o_East(ID_CV2)(SOUTH) <= rr_sgn_i_South(ID_CV2)(EAST);
	rr_sgn_o_East(ID_CV2)(LOCAL) <= rr_sgn_i_Local(ID_CV2)(EAST);
	
	rr_sgn_o_East(ID_CV3)(EAST) <= rr_sgn_i_East(ID_CV3)(EAST);
	rr_sgn_o_East(ID_CV3)(WEST) <= rr_sgn_i_West(ID_CV3)(EAST);
	rr_sgn_o_East(ID_CV3)(NORTH) <= rr_sgn_i_North(ID_CV3)(EAST);
	-- rr_sgn_o_East(ID_CV3)(SOUTH) <= rr_sgn_i_South(ID_CV3)(EAST);
	rr_sgn_o_East(ID_CV3)(LOCAL) <= rr_sgn_i_Local(ID_CV3)(EAST);
	---- NORTH ----
	rr_sgn_o_North(ID_CV0)(EAST) <= rr_sgn_i_East(ID_CV0)(NORTH);
	rr_sgn_o_North(ID_CV0)(WEST) <= rr_sgn_i_West(ID_CV0)(NORTH);
	rr_sgn_o_North(ID_CV0)(NORTH) <= rr_sgn_i_North(ID_CV0)(NORTH);
	-- rr_sgn_o_North(ID_CV0)(SOUTH) <= rr_sgn_i_South(ID_CV0)(NORTH);
	rr_sgn_o_North(ID_CV0)(LOCAL) <= rr_sgn_i_Local(ID_CV0)(NORTH);
	
	rr_sgn_o_North(ID_CV1)(EAST) <= rr_sgn_i_East(ID_CV1)(NORTH);
	rr_sgn_o_North(ID_CV1)(WEST) <= rr_sgn_i_West(ID_CV1)(NORTH);
	rr_sgn_o_North(ID_CV1)(NORTH) <= rr_sgn_i_North(ID_CV1)(NORTH);
	-- rr_sgn_o_North(ID_CV1)(SOUTH) <= rr_sgn_i_South(ID_CV1)(NORTH);
	rr_sgn_o_North(ID_CV1)(LOCAL) <= rr_sgn_i_Local(ID_CV1)(NORTH);
	
	rr_sgn_o_North(ID_CV2)(EAST) <= rr_sgn_i_East(ID_CV2)(NORTH);
	rr_sgn_o_North(ID_CV2)(WEST) <= rr_sgn_i_West(ID_CV2)(NORTH);
	rr_sgn_o_North(ID_CV2)(NORTH) <= rr_sgn_i_North(ID_CV2)(NORTH);
	-- rr_sgn_o_North(ID_CV2)(SOUTH) <= rr_sgn_i_South(ID_CV2)(NORTH);
	rr_sgn_o_North(ID_CV2)(LOCAL) <= rr_sgn_i_Local(ID_CV2)(NORTH);
	
	rr_sgn_o_North(ID_CV3)(EAST) <= rr_sgn_i_East(ID_CV3)(NORTH);
	rr_sgn_o_North(ID_CV3)(WEST) <= rr_sgn_i_West(ID_CV3)(NORTH);
	rr_sgn_o_North(ID_CV3)(NORTH) <= rr_sgn_i_North(ID_CV3)(NORTH);
	-- rr_sgn_o_North(ID_CV3)(SOUTH) <= rr_sgn_i_South(ID_CV3)(NORTH);
	rr_sgn_o_North(ID_CV3)(LOCAL) <= rr_sgn_i_Local(ID_CV3)(NORTH);
	---- WEST ----
	rr_sgn_o_West(ID_CV0)(EAST) <= rr_sgn_i_East(ID_CV0)(WEST);
	rr_sgn_o_West(ID_CV0)(WEST) <= rr_sgn_i_West(ID_CV0)(WEST);
	rr_sgn_o_West(ID_CV0)(NORTH) <= rr_sgn_i_North(ID_CV0)(WEST);
	-- rr_sgn_o_West(ID_CV0)(SOUTH) <= rr_sgn_i_South(ID_CV0)(WEST);
	rr_sgn_o_West(ID_CV0)(LOCAL) <= rr_sgn_i_Local(ID_CV0)(WEST);
	
	rr_sgn_o_West(ID_CV1)(EAST) <= rr_sgn_i_East(ID_CV1)(WEST);
	rr_sgn_o_West(ID_CV1)(WEST) <= rr_sgn_i_West(ID_CV1)(WEST);
	rr_sgn_o_West(ID_CV1)(NORTH) <= rr_sgn_i_North(ID_CV1)(WEST);
	-- rr_sgn_o_West(ID_CV1)(SOUTH) <= rr_sgn_i_South(ID_CV1)(WEST);
	rr_sgn_o_West(ID_CV1)(LOCAL) <= rr_sgn_i_Local(ID_CV1)(WEST);
	
	rr_sgn_o_West(ID_CV2)(EAST) <= rr_sgn_i_East(ID_CV2)(WEST);
	rr_sgn_o_West(ID_CV2)(WEST) <= rr_sgn_i_West(ID_CV2)(WEST);
	rr_sgn_o_West(ID_CV2)(NORTH) <= rr_sgn_i_North(ID_CV2)(WEST);
	-- rr_sgn_o_West(ID_CV2)(SOUTH) <= rr_sgn_i_South(ID_CV2)(WEST);
	rr_sgn_o_West(ID_CV2)(LOCAL) <= rr_sgn_i_Local(ID_CV2)(WEST);
	
	rr_sgn_o_West(ID_CV3)(EAST) <= rr_sgn_i_East(ID_CV3)(WEST);
	rr_sgn_o_West(ID_CV3)(WEST) <= rr_sgn_i_West(ID_CV3)(WEST);
	rr_sgn_o_West(ID_CV3)(NORTH) <= rr_sgn_i_North(ID_CV3)(WEST);
	-- rr_sgn_o_West(ID_CV3)(SOUTH) <= rr_sgn_i_South(ID_CV3)(WEST);
	rr_sgn_o_West(ID_CV3)(LOCAL) <= rr_sgn_i_Local(ID_CV3)(WEST);
	---- SOUTH ----
	-- rr_sgn_o_South(ID_CV0)(EAST) <= rr_sgn_i_East(ID_CV0)(SOUTH);
	-- rr_sgn_o_South(ID_CV0)(WEST) <= rr_sgn_i_West(ID_CV0)(SOUTH);
	-- rr_sgn_o_South(ID_CV0)(NORTH) <= rr_sgn_i_North(ID_CV0)(SOUTH);
	-- rr_sgn_o_South(ID_CV0)(SOUTH) <= rr_sgn_i_South(ID_CV0)(SOUTH);
	-- rr_sgn_o_South(ID_CV0)(LOCAL) <= rr_sgn_i_Local(ID_CV0)(SOUTH);
	
	-- rr_sgn_o_South(ID_CV1)(EAST) <= rr_sgn_i_East(ID_CV1)(SOUTH);
	-- rr_sgn_o_South(ID_CV1)(WEST) <= rr_sgn_i_West(ID_CV1)(SOUTH);
	-- rr_sgn_o_South(ID_CV1)(NORTH) <= rr_sgn_i_North(ID_CV1)(SOUTH);
	-- rr_sgn_o_South(ID_CV1)(SOUTH) <= rr_sgn_i_South(ID_CV1)(SOUTH);
	-- rr_sgn_o_South(ID_CV1)(LOCAL) <= rr_sgn_i_Local(ID_CV1)(SOUTH);
	
	-- rr_sgn_o_South(ID_CV2)(EAST) <= rr_sgn_i_East(ID_CV2)(SOUTH);
	-- rr_sgn_o_South(ID_CV2)(WEST) <= rr_sgn_i_West(ID_CV2)(SOUTH);
	-- rr_sgn_o_South(ID_CV2)(NORTH) <= rr_sgn_i_North(ID_CV2)(SOUTH);
	-- rr_sgn_o_South(ID_CV2)(SOUTH) <= rr_sgn_i_South(ID_CV2)(SOUTH);
	-- rr_sgn_o_South(ID_CV2)(LOCAL) <= rr_sgn_i_Local(ID_CV2)(SOUTH);
	
	-- rr_sgn_o_South(ID_CV3)(EAST) <= rr_sgn_i_East(ID_CV3)(SOUTH);
	-- rr_sgn_o_South(ID_CV3)(WEST) <= rr_sgn_i_West(ID_CV3)(SOUTH);
	-- rr_sgn_o_South(ID_CV3)(NORTH) <= rr_sgn_i_North(ID_CV3)(SOUTH);
	-- rr_sgn_o_South(ID_CV3)(SOUTH) <= rr_sgn_i_South(ID_CV3)(SOUTH);
	-- rr_sgn_o_South(ID_CV3)(LOCAL) <= rr_sgn_i_Local(ID_CV3)(SOUTH);
	---- LOCAL ----
	rr_sgn_o_Local(ID_CV0)(EAST) <= rr_sgn_i_East(ID_CV0)(LOCAL);
	rr_sgn_o_Local(ID_CV0)(WEST) <= rr_sgn_i_West(ID_CV0)(LOCAL);
	rr_sgn_o_Local(ID_CV0)(NORTH) <= rr_sgn_i_North(ID_CV0)(LOCAL);
	-- rr_sgn_o_Local(ID_CV0)(SOUTH) <= rr_sgn_i_South(ID_CV0)(LOCAL);
	rr_sgn_o_Local(ID_CV0)(LOCAL) <= rr_sgn_i_Local(ID_CV0)(LOCAL);
	
	rr_sgn_o_Local(ID_CV1)(EAST) <= rr_sgn_i_East(ID_CV1)(LOCAL);
	rr_sgn_o_Local(ID_CV1)(WEST) <= rr_sgn_i_West(ID_CV1)(LOCAL);
	rr_sgn_o_Local(ID_CV1)(NORTH) <= rr_sgn_i_North(ID_CV1)(LOCAL);
	-- rr_sgn_o_Local(ID_CV1)(SOUTH) <= rr_sgn_i_South(ID_CV1)(LOCAL);
	rr_sgn_o_Local(ID_CV1)(LOCAL) <= rr_sgn_i_Local(ID_CV1)(LOCAL);
	
	rr_sgn_o_Local(ID_CV2)(EAST) <= rr_sgn_i_East(ID_CV2)(LOCAL);
	rr_sgn_o_Local(ID_CV2)(WEST) <= rr_sgn_i_West(ID_CV2)(LOCAL);
	rr_sgn_o_Local(ID_CV2)(NORTH) <= rr_sgn_i_North(ID_CV2)(LOCAL);
	-- rr_sgn_o_Local(ID_CV2)(SOUTH) <= rr_sgn_i_South(ID_CV2)(LOCAL);
	rr_sgn_o_Local(ID_CV2)(LOCAL) <= rr_sgn_i_Local(ID_CV2)(LOCAL);
	
	rr_sgn_o_Local(ID_CV3)(EAST) <= rr_sgn_i_East(ID_CV3)(LOCAL);
	rr_sgn_o_Local(ID_CV3)(WEST) <= rr_sgn_i_West(ID_CV3)(LOCAL);
	rr_sgn_o_Local(ID_CV3)(NORTH) <= rr_sgn_i_North(ID_CV3)(LOCAL);
	-- rr_sgn_o_Local(ID_CV3)(SOUTH) <= rr_sgn_i_South(ID_CV3)(LOCAL);
	rr_sgn_o_Local(ID_CV3)(LOCAL) <= rr_sgn_i_Local(ID_CV3)(LOCAL);
-----------------------------	
	-- SINAL DE ATENDIMENTO A REQUISIÇÃO DE ROTEAMENTO - ACK_RR
	ack_rr_sgn_i(EAST)(ID_CV0) <= ack_rr_sgn_o_East(ID_CV0)(EAST) or ack_rr_sgn_o_North(ID_CV0)(EAST) or ack_rr_sgn_o_West(ID_CV0)(EAST) or ack_rr_sgn_o_Local(ID_CV0)(EAST);
	ack_rr_sgn_i(EAST)(ID_CV1) <= ack_rr_sgn_o_East(ID_CV1)(EAST) or ack_rr_sgn_o_North(ID_CV1)(EAST) or ack_rr_sgn_o_West(ID_CV1)(EAST) or ack_rr_sgn_o_Local(ID_CV1)(EAST);
	ack_rr_sgn_i(EAST)(ID_CV2) <= ack_rr_sgn_o_East(ID_CV2)(EAST) or ack_rr_sgn_o_North(ID_CV2)(EAST) or ack_rr_sgn_o_West(ID_CV2)(EAST) or ack_rr_sgn_o_Local(ID_CV2)(EAST);
	ack_rr_sgn_i(EAST)(ID_CV3) <= ack_rr_sgn_o_East(ID_CV3)(EAST) or ack_rr_sgn_o_North(ID_CV3)(EAST) or ack_rr_sgn_o_West(ID_CV3)(EAST) or ack_rr_sgn_o_Local(ID_CV3)(EAST);

	ack_rr_sgn_i(WEST)(ID_CV0) <= ack_rr_sgn_o_East(ID_CV0)(WEST) or ack_rr_sgn_o_North(ID_CV0)(WEST) or ack_rr_sgn_o_West(ID_CV0)(WEST) or  ack_rr_sgn_o_Local(ID_CV0)(WEST);
	ack_rr_sgn_i(WEST)(ID_CV1) <= ack_rr_sgn_o_East(ID_CV1)(WEST) or ack_rr_sgn_o_North(ID_CV1)(WEST) or ack_rr_sgn_o_West(ID_CV1)(WEST) or  ack_rr_sgn_o_Local(ID_CV1)(WEST);
	ack_rr_sgn_i(WEST)(ID_CV2) <= ack_rr_sgn_o_East(ID_CV2)(WEST) or ack_rr_sgn_o_North(ID_CV2)(WEST) or ack_rr_sgn_o_West(ID_CV2)(WEST) or  ack_rr_sgn_o_Local(ID_CV2)(WEST);
	ack_rr_sgn_i(WEST)(ID_CV3) <= ack_rr_sgn_o_East(ID_CV3)(WEST) or ack_rr_sgn_o_North(ID_CV3)(WEST) or ack_rr_sgn_o_West(ID_CV3)(WEST) or  ack_rr_sgn_o_Local(ID_CV3)(WEST);

	ack_rr_sgn_i(NORTH)(ID_CV0) <= ack_rr_sgn_o_East(ID_CV0)(NORTH) or ack_rr_sgn_o_North(ID_CV0)(NORTH) or ack_rr_sgn_o_West(ID_CV0)(NORTH) or ack_rr_sgn_o_Local(ID_CV0)(NORTH);
	ack_rr_sgn_i(NORTH)(ID_CV1) <= ack_rr_sgn_o_East(ID_CV1)(NORTH) or ack_rr_sgn_o_North(ID_CV1)(NORTH) or ack_rr_sgn_o_West(ID_CV1)(NORTH) or ack_rr_sgn_o_Local(ID_CV1)(NORTH);
	ack_rr_sgn_i(NORTH)(ID_CV2) <= ack_rr_sgn_o_East(ID_CV2)(NORTH) or ack_rr_sgn_o_North(ID_CV2)(NORTH) or ack_rr_sgn_o_West(ID_CV2)(NORTH) or ack_rr_sgn_o_Local(ID_CV2)(NORTH);
	ack_rr_sgn_i(NORTH)(ID_CV3) <= ack_rr_sgn_o_East(ID_CV3)(NORTH) or ack_rr_sgn_o_North(ID_CV3)(NORTH) or ack_rr_sgn_o_West(ID_CV3)(NORTH) or ack_rr_sgn_o_Local(ID_CV3)(NORTH);

	ack_rr_sgn_i(SOUTH)(ID_CV0) <= '0';
	ack_rr_sgn_i(SOUTH)(ID_CV1) <= '0';
	ack_rr_sgn_i(SOUTH)(ID_CV2) <= '0';
	ack_rr_sgn_i(SOUTH)(ID_CV3) <= '0';

	ack_rr_sgn_i(LOCAL)(ID_CV0) <= ack_rr_sgn_o_East(ID_CV0)(LOCAL) or ack_rr_sgn_o_North(ID_CV0)(LOCAL) or ack_rr_sgn_o_West(ID_CV0)(LOCAL) or ack_rr_sgn_o_Local(ID_CV0)(LOCAL);
	ack_rr_sgn_i(LOCAL)(ID_CV1) <= ack_rr_sgn_o_East(ID_CV1)(LOCAL) or ack_rr_sgn_o_North(ID_CV1)(LOCAL) or ack_rr_sgn_o_West(ID_CV1)(LOCAL) or ack_rr_sgn_o_Local(ID_CV1)(LOCAL);
	ack_rr_sgn_i(LOCAL)(ID_CV2) <= ack_rr_sgn_o_East(ID_CV2)(LOCAL) or ack_rr_sgn_o_North(ID_CV2)(LOCAL) or ack_rr_sgn_o_West(ID_CV2)(LOCAL) or ack_rr_sgn_o_Local(ID_CV2)(LOCAL);
	ack_rr_sgn_i(LOCAL)(ID_CV3) <= ack_rr_sgn_o_East(ID_CV3)(LOCAL) or ack_rr_sgn_o_North(ID_CV3)(LOCAL) or ack_rr_sgn_o_West(ID_CV3)(LOCAL) or ack_rr_sgn_o_Local(ID_CV3)(LOCAL);
	
-----------------------------	
	-- SINAL DE PEDIDO DE TRANSMISSAO - DATA_AV
	-- EAST
	data_av_sgn_o_East(ID_CV0)(EAST) <= data_av_sgn_i_East(ID_CV0)(EAST);
	data_av_sgn_o_East(ID_CV0)(WEST) <= data_av_sgn_i_West(ID_CV0)(EAST);
	data_av_sgn_o_East(ID_CV0)(NORTH) <= data_av_sgn_i_North(ID_CV0)(EAST);
	data_av_sgn_o_East(ID_CV0)(SOUTH) <= '0';
	data_av_sgn_o_East(ID_CV0)(LOCAL) <= data_av_sgn_i_Local(ID_CV0)(EAST);

	data_av_sgn_o_East(ID_CV1)(EAST) <= data_av_sgn_i_East(ID_CV1)(EAST);
	data_av_sgn_o_East(ID_CV1)(WEST) <= data_av_sgn_i_West(ID_CV1)(EAST);
	data_av_sgn_o_East(ID_CV1)(NORTH) <= data_av_sgn_i_North(ID_CV1)(EAST);
	data_av_sgn_o_East(ID_CV1)(SOUTH) <= '0';
	data_av_sgn_o_East(ID_CV1)(LOCAL) <= data_av_sgn_i_Local(ID_CV1)(EAST);

	data_av_sgn_o_East(ID_CV2)(EAST) <= data_av_sgn_i_East(ID_CV2)(EAST);
	data_av_sgn_o_East(ID_CV2)(WEST) <= data_av_sgn_i_West(ID_CV2)(EAST);
	data_av_sgn_o_East(ID_CV2)(NORTH) <= data_av_sgn_i_North(ID_CV2)(EAST);
	data_av_sgn_o_East(ID_CV2)(SOUTH) <= '0';
	data_av_sgn_o_East(ID_CV2)(LOCAL) <= data_av_sgn_i_Local(ID_CV2)(EAST);

	data_av_sgn_o_East(ID_CV3)(EAST) <= data_av_sgn_i_East(ID_CV3)(EAST);
	data_av_sgn_o_East(ID_CV3)(WEST) <= data_av_sgn_i_West(ID_CV3)(EAST);
	data_av_sgn_o_East(ID_CV3)(NORTH) <= data_av_sgn_i_North(ID_CV3)(EAST);
	data_av_sgn_o_East(ID_CV3)(SOUTH) <= '0';
	data_av_sgn_o_East(ID_CV3)(LOCAL) <= data_av_sgn_i_Local(ID_CV3)(EAST);

	---- NORTH ----
	data_av_sgn_o_North(ID_CV0)(EAST) <= data_av_sgn_i_East(ID_CV0)(NORTH);
	data_av_sgn_o_North(ID_CV0)(WEST) <= data_av_sgn_i_West(ID_CV0)(NORTH);
	data_av_sgn_o_North(ID_CV0)(NORTH) <= data_av_sgn_i_North(ID_CV0)(NORTH);
	data_av_sgn_o_North(ID_CV0)(SOUTH) <= '0';
	data_av_sgn_o_North(ID_CV0)(LOCAL) <= data_av_sgn_i_Local(ID_CV0)(NORTH);

	data_av_sgn_o_North(ID_CV1)(EAST) <= data_av_sgn_i_East(ID_CV1)(NORTH);
	data_av_sgn_o_North(ID_CV1)(WEST) <= data_av_sgn_i_West(ID_CV1)(NORTH);
	data_av_sgn_o_North(ID_CV1)(NORTH) <= data_av_sgn_i_North(ID_CV1)(NORTH);
	data_av_sgn_o_North(ID_CV1)(SOUTH) <= '0';
	data_av_sgn_o_North(ID_CV1)(LOCAL) <= data_av_sgn_i_Local(ID_CV1)(NORTH);

	data_av_sgn_o_North(ID_CV2)(EAST) <= data_av_sgn_i_East(ID_CV2)(NORTH);
	data_av_sgn_o_North(ID_CV2)(WEST) <= data_av_sgn_i_West(ID_CV2)(NORTH);
	data_av_sgn_o_North(ID_CV2)(NORTH) <= data_av_sgn_i_North(ID_CV2)(NORTH);
	data_av_sgn_o_North(ID_CV2)(SOUTH) <= '0';
	data_av_sgn_o_North(ID_CV2)(LOCAL) <= data_av_sgn_i_Local(ID_CV2)(NORTH);

	data_av_sgn_o_North(ID_CV3)(EAST) <= data_av_sgn_i_East(ID_CV3)(NORTH);
	data_av_sgn_o_North(ID_CV3)(WEST) <= data_av_sgn_i_West(ID_CV3)(NORTH);
	data_av_sgn_o_North(ID_CV3)(NORTH) <= data_av_sgn_i_North(ID_CV3)(NORTH);
	data_av_sgn_o_North(ID_CV3)(SOUTH) <= '0';
	data_av_sgn_o_North(ID_CV3)(LOCAL) <= data_av_sgn_i_Local(ID_CV3)(NORTH);

	---- WEST ----
	data_av_sgn_o_West(ID_CV0)(EAST) <= data_av_sgn_i_East(ID_CV0)(WEST);
	data_av_sgn_o_West(ID_CV0)(WEST) <= data_av_sgn_i_West(ID_CV0)(WEST);
	data_av_sgn_o_West(ID_CV0)(NORTH) <= data_av_sgn_i_North(ID_CV0)(WEST);
	data_av_sgn_o_West(ID_CV0)(SOUTH) <= '0';
	data_av_sgn_o_West(ID_CV0)(LOCAL) <= data_av_sgn_i_Local(ID_CV0)(WEST);

	data_av_sgn_o_West(ID_CV1)(EAST) <= data_av_sgn_i_East(ID_CV1)(WEST);
	data_av_sgn_o_West(ID_CV1)(WEST) <= data_av_sgn_i_West(ID_CV1)(WEST);
	data_av_sgn_o_West(ID_CV1)(NORTH) <= data_av_sgn_i_North(ID_CV1)(WEST);
	data_av_sgn_o_West(ID_CV1)(SOUTH) <= '0';
	data_av_sgn_o_West(ID_CV1)(LOCAL) <= data_av_sgn_i_Local(ID_CV1)(WEST);

	data_av_sgn_o_West(ID_CV2)(EAST) <= data_av_sgn_i_East(ID_CV2)(WEST);
	data_av_sgn_o_West(ID_CV2)(WEST) <= data_av_sgn_i_West(ID_CV2)(WEST);
	data_av_sgn_o_West(ID_CV2)(NORTH) <= data_av_sgn_i_North(ID_CV2)(WEST);
	data_av_sgn_o_West(ID_CV2)(SOUTH) <= '0';
	data_av_sgn_o_West(ID_CV2)(LOCAL) <= data_av_sgn_i_Local(ID_CV2)(WEST);

	data_av_sgn_o_West(ID_CV3)(EAST) <= data_av_sgn_i_East(ID_CV3)(WEST);
	data_av_sgn_o_West(ID_CV3)(WEST) <= data_av_sgn_i_West(ID_CV3)(WEST);
	data_av_sgn_o_West(ID_CV3)(NORTH) <= data_av_sgn_i_North(ID_CV3)(WEST);
	data_av_sgn_o_West(ID_CV3)(SOUTH) <= '0';
	data_av_sgn_o_West(ID_CV3)(LOCAL) <= data_av_sgn_i_Local(ID_CV3)(WEST);

	----SOUTH ----
	-- data_av_sgn_o_South(ID_CV0)(EAST) <= data_av_sgn_i_East(ID_CV0)(SOUTH);
	-- data_av_sgn_o_South(ID_CV0)(WEST) <= data_av_sgn_i_West(ID_CV0)(SOUTH);
	-- data_av_sgn_o_South(ID_CV0)(NORTH) <= data_av_sgn_i_North(ID_CV0)(SOUTH);
	-- data_av_sgn_o_South(ID_CV0)(SOUTH) <= data_av_sgn_i_South(ID_CV0)(SOUTH);
	-- data_av_sgn_o_South(ID_CV0)(LOCAL) <= data_av_sgn_i_Local(ID_CV0)(SOUTH);

	-- data_av_sgn_o_South(ID_CV1)(EAST) <= data_av_sgn_i_East(ID_CV1)(SOUTH);
	-- data_av_sgn_o_South(ID_CV1)(WEST) <= data_av_sgn_i_West(ID_CV1)(SOUTH);
	-- data_av_sgn_o_South(ID_CV1)(NORTH) <= data_av_sgn_i_North(ID_CV1)(SOUTH);
	-- data_av_sgn_o_South(ID_CV1)(SOUTH) <= data_av_sgn_i_South(ID_CV1)(SOUTH);
	-- data_av_sgn_o_South(ID_CV1)(LOCAL) <= data_av_sgn_i_Local(ID_CV1)(SOUTH);

	-- data_av_sgn_o_South(ID_CV2)(EAST) <= data_av_sgn_i_East(ID_CV2)(SOUTH);
	-- data_av_sgn_o_South(ID_CV2)(WEST) <= data_av_sgn_i_West(ID_CV2)(SOUTH);
	-- data_av_sgn_o_South(ID_CV2)(NORTH) <= data_av_sgn_i_North(ID_CV2)(SOUTH);
	-- data_av_sgn_o_South(ID_CV2)(SOUTH) <= data_av_sgn_i_South(ID_CV2)(SOUTH);
	-- data_av_sgn_o_South(ID_CV2)(LOCAL) <= data_av_sgn_i_Local(ID_CV2)(SOUTH);

	-- data_av_sgn_o_South(ID_CV3)(EAST) <= data_av_sgn_i_East(ID_CV3)(SOUTH);
	-- data_av_sgn_o_South(ID_CV3)(WEST) <= data_av_sgn_i_West(ID_CV3)(SOUTH);
	-- data_av_sgn_o_South(ID_CV3)(NORTH) <= data_av_sgn_i_North(ID_CV3)(SOUTH);
	-- data_av_sgn_o_South(ID_CV3)(SOUTH) <= data_av_sgn_i_South(ID_CV3)(SOUTH);
	-- data_av_sgn_o_South(ID_CV3)(LOCAL) <= data_av_sgn_i_Local(ID_CV3)(SOUTH);

	---- LOCAL ----
	data_av_sgn_o_Local(ID_CV0)(EAST) <= data_av_sgn_i_East(ID_CV0)(LOCAL);
	data_av_sgn_o_Local(ID_CV0)(WEST) <= data_av_sgn_i_West(ID_CV0)(LOCAL);
	data_av_sgn_o_Local(ID_CV0)(NORTH) <= data_av_sgn_i_North(ID_CV0)(LOCAL);
	data_av_sgn_o_Local(ID_CV0)(SOUTH) <= '0';
	data_av_sgn_o_Local(ID_CV0)(LOCAL) <= data_av_sgn_i_Local(ID_CV0)(LOCAL);

	data_av_sgn_o_Local(ID_CV1)(EAST) <= data_av_sgn_i_East(ID_CV1)(LOCAL);
	data_av_sgn_o_Local(ID_CV1)(WEST) <= data_av_sgn_i_West(ID_CV1)(LOCAL);
	data_av_sgn_o_Local(ID_CV1)(NORTH) <= data_av_sgn_i_North(ID_CV1)(LOCAL);
	data_av_sgn_o_Local(ID_CV1)(SOUTH) <= '0';
	data_av_sgn_o_Local(ID_CV1)(LOCAL) <= data_av_sgn_i_Local(ID_CV1)(LOCAL);

	data_av_sgn_o_Local(ID_CV2)(EAST) <= data_av_sgn_i_East(ID_CV2)(LOCAL);
	data_av_sgn_o_Local(ID_CV2)(WEST) <= data_av_sgn_i_West(ID_CV2)(LOCAL);
	data_av_sgn_o_Local(ID_CV2)(NORTH) <= data_av_sgn_i_North(ID_CV2)(LOCAL);
	data_av_sgn_o_Local(ID_CV2)(SOUTH) <= '0';
	data_av_sgn_o_Local(ID_CV2)(LOCAL) <= data_av_sgn_i_Local(ID_CV2)(LOCAL);

	data_av_sgn_o_Local(ID_CV3)(EAST) <= data_av_sgn_i_East(ID_CV3)(LOCAL);
	data_av_sgn_o_Local(ID_CV3)(WEST) <= data_av_sgn_i_West(ID_CV3)(LOCAL);
	data_av_sgn_o_Local(ID_CV3)(NORTH) <= data_av_sgn_i_North(ID_CV3)(LOCAL);
	data_av_sgn_o_Local(ID_CV3)(SOUTH) <= '0';
	data_av_sgn_o_Local(ID_CV3)(LOCAL) <= data_av_sgn_i_Local(ID_CV3)(LOCAL);
	
-----------------------------	
	-- DADO A SER TRANSMITIDO ENTRE AS PORTAS - DATA
	-- EAST
	data_sgn_o_East(ID_CV0)(EAST) <= data_sgn_i(EAST)(ID_CV0);
	data_sgn_o_East(ID_CV0)(WEST) <= data_sgn_i(WEST)(ID_CV0);
	data_sgn_o_East(ID_CV0)(NORTH) <= data_sgn_i(NORTH)(ID_CV0);
	data_sgn_o_East(ID_CV0)(SOUTH) <= (others=>'0');
	data_sgn_o_East(ID_CV0)(LOCAL) <= data_sgn_i(LOCAL)(ID_CV0);

	data_sgn_o_East(ID_CV1)(EAST) <= data_sgn_i(EAST)(ID_CV1);
	data_sgn_o_East(ID_CV1)(WEST) <= data_sgn_i(WEST)(ID_CV1);
	data_sgn_o_East(ID_CV1)(NORTH) <= data_sgn_i(NORTH)(ID_CV1);
	data_sgn_o_East(ID_CV1)(SOUTH) <= (others=>'0');
	data_sgn_o_East(ID_CV1)(LOCAL) <= data_sgn_i(LOCAL)(ID_CV1);

	data_sgn_o_East(ID_CV2)(EAST) <= data_sgn_i(EAST)(ID_CV2);
	data_sgn_o_East(ID_CV2)(WEST) <= data_sgn_i(WEST)(ID_CV2);
	data_sgn_o_East(ID_CV2)(NORTH) <= data_sgn_i(NORTH)(ID_CV2);
	data_sgn_o_East(ID_CV2)(SOUTH) <= (others=>'0');
	data_sgn_o_East(ID_CV2)(LOCAL) <= data_sgn_i(LOCAL)(ID_CV2);

	data_sgn_o_East(ID_CV3)(EAST) <= data_sgn_i(EAST)(ID_CV3);
	data_sgn_o_East(ID_CV3)(WEST) <= data_sgn_i(WEST)(ID_CV3);
	data_sgn_o_East(ID_CV3)(NORTH) <= data_sgn_i(NORTH)(ID_CV3);
	data_sgn_o_East(ID_CV3)(SOUTH) <= (others=>'0');
	data_sgn_o_East(ID_CV3)(LOCAL) <= data_sgn_i(LOCAL)(ID_CV3);

	-- NORTH
	data_sgn_o_North(ID_CV0)(EAST) <= data_sgn_i(EAST)(ID_CV0);
	data_sgn_o_North(ID_CV0)(WEST) <= data_sgn_i(WEST)(ID_CV0);
	data_sgn_o_North(ID_CV0)(NORTH) <= data_sgn_i(NORTH)(ID_CV0);
	data_sgn_o_North(ID_CV0)(SOUTH) <= (others=>'0');
	data_sgn_o_North(ID_CV0)(LOCAL) <= data_sgn_i(LOCAL)(ID_CV0);

	data_sgn_o_North(ID_CV1)(EAST) <= data_sgn_i(EAST)(ID_CV1);
	data_sgn_o_North(ID_CV1)(WEST) <= data_sgn_i(WEST)(ID_CV1);
	data_sgn_o_North(ID_CV1)(NORTH) <= data_sgn_i(NORTH)(ID_CV1);
	data_sgn_o_North(ID_CV1)(SOUTH) <= (others=>'0');
	data_sgn_o_North(ID_CV1)(LOCAL) <= data_sgn_i(LOCAL)(ID_CV1);

	data_sgn_o_North(ID_CV2)(EAST) <= data_sgn_i(EAST)(ID_CV2);
	data_sgn_o_North(ID_CV2)(WEST) <= data_sgn_i(WEST)(ID_CV2);
	data_sgn_o_North(ID_CV2)(NORTH) <= data_sgn_i(NORTH)(ID_CV2);
	data_sgn_o_North(ID_CV2)(SOUTH) <= (others=>'0');
	data_sgn_o_North(ID_CV2)(LOCAL) <= data_sgn_i(LOCAL)(ID_CV2);

	data_sgn_o_North(ID_CV3)(EAST) <= data_sgn_i(EAST)(ID_CV3);
	data_sgn_o_North(ID_CV3)(WEST) <= data_sgn_i(WEST)(ID_CV3);
	data_sgn_o_North(ID_CV3)(NORTH) <= data_sgn_i(NORTH)(ID_CV3);
	data_sgn_o_North(ID_CV3)(SOUTH) <= (others=>'0');
	data_sgn_o_North(ID_CV3)(LOCAL) <= data_sgn_i(LOCAL)(ID_CV3);

	-- WEST
	data_sgn_o_West(ID_CV0)(EAST) <= data_sgn_i(EAST)(ID_CV0);
	data_sgn_o_West(ID_CV0)(WEST) <= data_sgn_i(WEST)(ID_CV0);
	data_sgn_o_West(ID_CV0)(NORTH) <= data_sgn_i(NORTH)(ID_CV0);
	data_sgn_o_West(ID_CV0)(SOUTH) <= (others=>'0');
	data_sgn_o_West(ID_CV0)(LOCAL) <= data_sgn_i(LOCAL)(ID_CV0);

	data_sgn_o_West(ID_CV1)(EAST) <= data_sgn_i(EAST)(ID_CV1);
	data_sgn_o_West(ID_CV1)(WEST) <= data_sgn_i(WEST)(ID_CV1);
	data_sgn_o_West(ID_CV1)(NORTH) <= data_sgn_i(NORTH)(ID_CV1);
	data_sgn_o_West(ID_CV1)(SOUTH) <= (others=>'0');
	data_sgn_o_West(ID_CV1)(LOCAL) <= data_sgn_i(LOCAL)(ID_CV1);

	data_sgn_o_West(ID_CV2)(EAST) <= data_sgn_i(EAST)(ID_CV2);
	data_sgn_o_West(ID_CV2)(WEST) <= data_sgn_i(WEST)(ID_CV2);
	data_sgn_o_West(ID_CV2)(NORTH) <= data_sgn_i(NORTH)(ID_CV2);
	data_sgn_o_West(ID_CV2)(SOUTH) <= (others=>'0');
	data_sgn_o_West(ID_CV2)(LOCAL) <= data_sgn_i(LOCAL)(ID_CV2);

	data_sgn_o_West(ID_CV3)(EAST) <= data_sgn_i(EAST)(ID_CV3);
	data_sgn_o_West(ID_CV3)(WEST) <= data_sgn_i(WEST)(ID_CV3);
	data_sgn_o_West(ID_CV3)(NORTH) <= data_sgn_i(NORTH)(ID_CV3);
	data_sgn_o_West(ID_CV3)(SOUTH) <= (others=>'0');
	data_sgn_o_West(ID_CV3)(LOCAL) <= data_sgn_i(LOCAL)(ID_CV3);

	-- SOUTH
	-- data_sgn_o_South(ID_CV0)(EAST) <= data_sgn_i(EAST)(ID_CV0);
	-- data_sgn_o_South(ID_CV0)(WEST) <= data_sgn_i(WEST)(ID_CV0);
	-- data_sgn_o_South(ID_CV0)(NORTH) <= data_sgn_i(NORTH)(ID_CV0);
	-- data_sgn_o_South(ID_CV0)(SOUTH) <= data_sgn_i(SOUTH)(ID_CV0);
	-- data_sgn_o_South(ID_CV0)(LOCAL) <= data_sgn_i(LOCAL)(ID_CV0);

	-- data_sgn_o_South(ID_CV1)(EAST) <= data_sgn_i(EAST)(ID_CV1);
	-- data_sgn_o_South(ID_CV1)(WEST) <= data_sgn_i(WEST)(ID_CV1);
	-- data_sgn_o_South(ID_CV1)(NORTH) <= data_sgn_i(NORTH)(ID_CV1);
	-- data_sgn_o_South(ID_CV1)(SOUTH) <= data_sgn_i(SOUTH)(ID_CV1);
	-- data_sgn_o_South(ID_CV1)(LOCAL) <= data_sgn_i(LOCAL)(ID_CV1);

	-- data_sgn_o_South(ID_CV2)(EAST) <= data_sgn_i(EAST)(ID_CV2);
	-- data_sgn_o_South(ID_CV2)(WEST) <= data_sgn_i(WEST)(ID_CV2);
	-- data_sgn_o_South(ID_CV2)(NORTH) <= data_sgn_i(NORTH)(ID_CV2);
	-- data_sgn_o_South(ID_CV2)(SOUTH) <= data_sgn_i(SOUTH)(ID_CV2);
	-- data_sgn_o_South(ID_CV2)(LOCAL) <= data_sgn_i(LOCAL)(ID_CV2);

	-- data_sgn_o_South(ID_CV3)(EAST) <= data_sgn_i(EAST)(ID_CV3);
	-- data_sgn_o_South(ID_CV3)(WEST) <= data_sgn_i(WEST)(ID_CV3);
	-- data_sgn_o_South(ID_CV3)(NORTH) <= data_sgn_i(NORTH)(ID_CV3);
	-- data_sgn_o_South(ID_CV3)(SOUTH) <= data_sgn_i(SOUTH)(ID_CV3);
	-- data_sgn_o_South(ID_CV3)(LOCAL) <= data_sgn_i(LOCAL)(ID_CV3);

	-- LOCAL
	data_sgn_o_Local(ID_CV0)(EAST) <= data_sgn_i(EAST)(ID_CV0);
	data_sgn_o_Local(ID_CV0)(WEST) <= data_sgn_i(WEST)(ID_CV0);
	data_sgn_o_Local(ID_CV0)(NORTH) <= data_sgn_i(NORTH)(ID_CV0);
	data_sgn_o_Local(ID_CV0)(SOUTH) <= (others=>'0');
	data_sgn_o_Local(ID_CV0)(LOCAL) <= data_sgn_i(LOCAL)(ID_CV0);

	data_sgn_o_Local(ID_CV1)(EAST) <= data_sgn_i(EAST)(ID_CV1);
	data_sgn_o_Local(ID_CV1)(WEST) <= data_sgn_i(WEST)(ID_CV1);
	data_sgn_o_Local(ID_CV1)(NORTH) <= data_sgn_i(NORTH)(ID_CV1);
	data_sgn_o_Local(ID_CV1)(SOUTH) <= (others=>'0');
	data_sgn_o_Local(ID_CV1)(LOCAL) <= data_sgn_i(LOCAL)(ID_CV1);

	data_sgn_o_Local(ID_CV2)(EAST) <= data_sgn_i(EAST)(ID_CV2);
	data_sgn_o_Local(ID_CV2)(WEST) <= data_sgn_i(WEST)(ID_CV2);
	data_sgn_o_Local(ID_CV2)(NORTH) <= data_sgn_i(NORTH)(ID_CV2);
	data_sgn_o_Local(ID_CV2)(SOUTH) <= (others=>'0');
	data_sgn_o_Local(ID_CV2)(LOCAL) <= data_sgn_i(LOCAL)(ID_CV2);

	data_sgn_o_Local(ID_CV3)(EAST) <= data_sgn_i(EAST)(ID_CV3);
	data_sgn_o_Local(ID_CV3)(WEST) <= data_sgn_i(WEST)(ID_CV3);
	data_sgn_o_Local(ID_CV3)(NORTH) <= data_sgn_i(NORTH)(ID_CV3);
	data_sgn_o_Local(ID_CV3)(SOUTH) <= (others=>'0');
	data_sgn_o_Local(ID_CV3)(LOCAL) <= data_sgn_i(LOCAL)(ID_CV3);

	-- SINAL DE ATENDIMENTO A TRANSMISSAO DO FLIT - DATA_ACK
	data_ack_sgn_i(EAST)(ID_CV0) <= data_ack_sgn_o_East(ID_CV0)(EAST) or data_ack_sgn_o_North(ID_CV0)(EAST) or data_ack_sgn_o_West(ID_CV0)(EAST) or data_ack_sgn_o_Local(ID_CV0)(EAST);
	data_ack_sgn_i(EAST)(ID_CV1) <= data_ack_sgn_o_East(ID_CV1)(EAST) or data_ack_sgn_o_North(ID_CV1)(EAST) or data_ack_sgn_o_West(ID_CV1)(EAST) or data_ack_sgn_o_Local(ID_CV1)(EAST);
	data_ack_sgn_i(EAST)(ID_CV2) <= data_ack_sgn_o_East(ID_CV2)(EAST) or data_ack_sgn_o_North(ID_CV2)(EAST) or data_ack_sgn_o_West(ID_CV2)(EAST) or data_ack_sgn_o_Local(ID_CV2)(EAST);
	data_ack_sgn_i(EAST)(ID_CV3) <= data_ack_sgn_o_East(ID_CV3)(EAST) or data_ack_sgn_o_North(ID_CV3)(EAST) or data_ack_sgn_o_West(ID_CV3)(EAST) or data_ack_sgn_o_Local(ID_CV3)(EAST);

	data_ack_sgn_i(WEST)(ID_CV0) <= data_ack_sgn_o_East(ID_CV0)(WEST) or data_ack_sgn_o_North(ID_CV0)(WEST) or data_ack_sgn_o_West(ID_CV0)(WEST) or data_ack_sgn_o_Local(ID_CV0)(WEST);
	data_ack_sgn_i(WEST)(ID_CV1) <= data_ack_sgn_o_East(ID_CV1)(WEST) or data_ack_sgn_o_North(ID_CV1)(WEST) or data_ack_sgn_o_West(ID_CV1)(WEST) or data_ack_sgn_o_Local(ID_CV1)(WEST);
	data_ack_sgn_i(WEST)(ID_CV2) <= data_ack_sgn_o_East(ID_CV2)(WEST) or data_ack_sgn_o_North(ID_CV2)(WEST) or data_ack_sgn_o_West(ID_CV2)(WEST) or data_ack_sgn_o_Local(ID_CV2)(WEST);
	data_ack_sgn_i(WEST)(ID_CV3) <= data_ack_sgn_o_East(ID_CV3)(WEST) or data_ack_sgn_o_North(ID_CV3)(WEST) or data_ack_sgn_o_West(ID_CV3)(WEST) or data_ack_sgn_o_Local(ID_CV3)(WEST);

	data_ack_sgn_i(NORTH)(ID_CV0) <= data_ack_sgn_o_East(ID_CV0)(NORTH) or data_ack_sgn_o_North(ID_CV0)(NORTH) or data_ack_sgn_o_West(ID_CV0)(NORTH) or data_ack_sgn_o_Local(ID_CV0)(NORTH);
	data_ack_sgn_i(NORTH)(ID_CV1) <= data_ack_sgn_o_East(ID_CV1)(NORTH) or data_ack_sgn_o_North(ID_CV1)(NORTH) or data_ack_sgn_o_West(ID_CV1)(NORTH) or data_ack_sgn_o_Local(ID_CV1)(NORTH);
	data_ack_sgn_i(NORTH)(ID_CV2) <= data_ack_sgn_o_East(ID_CV2)(NORTH) or data_ack_sgn_o_North(ID_CV2)(NORTH) or data_ack_sgn_o_West(ID_CV2)(NORTH) or data_ack_sgn_o_Local(ID_CV2)(NORTH);
	data_ack_sgn_i(NORTH)(ID_CV3) <= data_ack_sgn_o_East(ID_CV3)(NORTH) or data_ack_sgn_o_North(ID_CV3)(NORTH) or data_ack_sgn_o_West(ID_CV3)(NORTH) or data_ack_sgn_o_Local(ID_CV3)(NORTH);

	data_ack_sgn_i(SOUTH)(ID_CV0) <= '0';
	data_ack_sgn_i(SOUTH)(ID_CV1) <= '0';
	data_ack_sgn_i(SOUTH)(ID_CV2) <= '0';
	data_ack_sgn_i(SOUTH)(ID_CV3) <= '0';

	data_ack_sgn_i(LOCAL)(ID_CV0) <= data_ack_sgn_o_East(ID_CV0)(LOCAL) or data_ack_sgn_o_North(ID_CV0)(LOCAL) or data_ack_sgn_o_West(ID_CV0)(LOCAL) or data_ack_sgn_o_Local(ID_CV0)(LOCAL);
	data_ack_sgn_i(LOCAL)(ID_CV1) <= data_ack_sgn_o_East(ID_CV1)(LOCAL) or data_ack_sgn_o_North(ID_CV1)(LOCAL) or data_ack_sgn_o_West(ID_CV1)(LOCAL) or data_ack_sgn_o_Local(ID_CV1)(LOCAL);
	data_ack_sgn_i(LOCAL)(ID_CV2) <= data_ack_sgn_o_East(ID_CV2)(LOCAL) or data_ack_sgn_o_North(ID_CV2)(LOCAL) or data_ack_sgn_o_West(ID_CV2)(LOCAL) or data_ack_sgn_o_Local(ID_CV2)(LOCAL);
	data_ack_sgn_i(LOCAL)(ID_CV3) <= data_ack_sgn_o_East(ID_CV3)(LOCAL) or data_ack_sgn_o_North(ID_CV3)(LOCAL) or data_ack_sgn_o_West(ID_CV3)(LOCAL) or data_ack_sgn_o_Local(ID_CV3)(LOCAL);
                                                                                                                                                                                             
	-- SINAL DE REQUISIÇÃO DE ROTEAMENTO - RR
	---- EAST ----
	EOP_sgn_o_East(ID_CV0)(EAST) <= EOP_sgn_i_East(ID_CV0)(EAST);
	EOP_sgn_o_East(ID_CV0)(WEST) <= EOP_sgn_i_West(ID_CV0)(EAST);
	EOP_sgn_o_East(ID_CV0)(NORTH) <= EOP_sgn_i_North(ID_CV0)(EAST);
	EOP_sgn_o_East(ID_CV0)(SOUTH) <= '0';
	EOP_sgn_o_East(ID_CV0)(LOCAL) <= EOP_sgn_i_Local(ID_CV0)(EAST);
	
	EOP_sgn_o_East(ID_CV1)(EAST) <= EOP_sgn_i_East(ID_CV1)(EAST);
	EOP_sgn_o_East(ID_CV1)(WEST) <= EOP_sgn_i_West(ID_CV1)(EAST);
	EOP_sgn_o_East(ID_CV1)(NORTH) <= EOP_sgn_i_North(ID_CV1)(EAST);
	EOP_sgn_o_East(ID_CV1)(SOUTH) <= '0';
	EOP_sgn_o_East(ID_CV1)(LOCAL) <= EOP_sgn_i_Local(ID_CV1)(EAST);
	
	EOP_sgn_o_East(ID_CV2)(EAST) <= EOP_sgn_i_East(ID_CV2)(EAST);
	EOP_sgn_o_East(ID_CV2)(WEST) <= EOP_sgn_i_West(ID_CV2)(EAST);
	EOP_sgn_o_East(ID_CV2)(NORTH) <= EOP_sgn_i_North(ID_CV2)(EAST);
	EOP_sgn_o_East(ID_CV2)(SOUTH) <= '0';
	EOP_sgn_o_East(ID_CV2)(LOCAL) <= EOP_sgn_i_Local(ID_CV2)(EAST);
	
	EOP_sgn_o_East(ID_CV3)(EAST) <= EOP_sgn_i_East(ID_CV3)(EAST);
	EOP_sgn_o_East(ID_CV3)(WEST) <= EOP_sgn_i_West(ID_CV3)(EAST);
	EOP_sgn_o_East(ID_CV3)(NORTH) <= EOP_sgn_i_North(ID_CV3)(EAST);
	EOP_sgn_o_East(ID_CV3)(SOUTH) <= '0';
	EOP_sgn_o_East(ID_CV3)(LOCAL) <= EOP_sgn_i_Local(ID_CV3)(EAST);
	---- NORTH ----
	EOP_sgn_o_North(ID_CV0)(EAST) <= EOP_sgn_i_East(ID_CV0)(NORTH);
	EOP_sgn_o_North(ID_CV0)(WEST) <= EOP_sgn_i_West(ID_CV0)(NORTH);
	EOP_sgn_o_North(ID_CV0)(NORTH) <= EOP_sgn_i_North(ID_CV0)(NORTH);
	EOP_sgn_o_North(ID_CV0)(SOUTH) <= '0';
	EOP_sgn_o_North(ID_CV0)(LOCAL) <= EOP_sgn_i_Local(ID_CV0)(NORTH);
	
	EOP_sgn_o_North(ID_CV1)(EAST) <= EOP_sgn_i_East(ID_CV1)(NORTH);
	EOP_sgn_o_North(ID_CV1)(WEST) <= EOP_sgn_i_West(ID_CV1)(NORTH);
	EOP_sgn_o_North(ID_CV1)(NORTH) <= EOP_sgn_i_North(ID_CV1)(NORTH);
	EOP_sgn_o_North(ID_CV1)(SOUTH) <= '0';
	EOP_sgn_o_North(ID_CV1)(LOCAL) <= EOP_sgn_i_Local(ID_CV1)(NORTH);
	
	EOP_sgn_o_North(ID_CV2)(EAST) <= EOP_sgn_i_East(ID_CV2)(NORTH);
	EOP_sgn_o_North(ID_CV2)(WEST) <= EOP_sgn_i_West(ID_CV2)(NORTH);
	EOP_sgn_o_North(ID_CV2)(NORTH) <= EOP_sgn_i_North(ID_CV2)(NORTH);
	EOP_sgn_o_North(ID_CV2)(SOUTH) <= '0';
	EOP_sgn_o_North(ID_CV2)(LOCAL) <= EOP_sgn_i_Local(ID_CV2)(NORTH);
	
	EOP_sgn_o_North(ID_CV3)(EAST) <= EOP_sgn_i_East(ID_CV3)(NORTH);
	EOP_sgn_o_North(ID_CV3)(WEST) <= EOP_sgn_i_West(ID_CV3)(NORTH);
	EOP_sgn_o_North(ID_CV3)(NORTH) <= EOP_sgn_i_North(ID_CV3)(NORTH);
	EOP_sgn_o_North(ID_CV3)(SOUTH) <= '0';
	EOP_sgn_o_North(ID_CV3)(LOCAL) <= EOP_sgn_i_Local(ID_CV3)(NORTH);
	---- WEST ----
	EOP_sgn_o_West(ID_CV0)(EAST) <= EOP_sgn_i_East(ID_CV0)(WEST);
	EOP_sgn_o_West(ID_CV0)(WEST) <= EOP_sgn_i_West(ID_CV0)(WEST);
	EOP_sgn_o_West(ID_CV0)(NORTH) <= EOP_sgn_i_North(ID_CV0)(WEST);
	EOP_sgn_o_West(ID_CV0)(SOUTH) <= '0';
	EOP_sgn_o_West(ID_CV0)(LOCAL) <= EOP_sgn_i_Local(ID_CV0)(WEST);
	
	EOP_sgn_o_West(ID_CV1)(EAST) <= EOP_sgn_i_East(ID_CV1)(WEST);
	EOP_sgn_o_West(ID_CV1)(WEST) <= EOP_sgn_i_West(ID_CV1)(WEST);
	EOP_sgn_o_West(ID_CV1)(NORTH) <= EOP_sgn_i_North(ID_CV1)(WEST);
	EOP_sgn_o_West(ID_CV1)(SOUTH) <= '0';
	EOP_sgn_o_West(ID_CV1)(LOCAL) <= EOP_sgn_i_Local(ID_CV1)(WEST);
	
	EOP_sgn_o_West(ID_CV2)(EAST) <= EOP_sgn_i_East(ID_CV2)(WEST);
	EOP_sgn_o_West(ID_CV2)(WEST) <= EOP_sgn_i_West(ID_CV2)(WEST);
	EOP_sgn_o_West(ID_CV2)(NORTH) <= EOP_sgn_i_North(ID_CV2)(WEST);
	EOP_sgn_o_West(ID_CV2)(SOUTH) <= '0';
	EOP_sgn_o_West(ID_CV2)(LOCAL) <= EOP_sgn_i_Local(ID_CV2)(WEST);
	
	EOP_sgn_o_West(ID_CV3)(EAST) <= EOP_sgn_i_East(ID_CV3)(WEST);
	EOP_sgn_o_West(ID_CV3)(WEST) <= EOP_sgn_i_West(ID_CV3)(WEST);
	EOP_sgn_o_West(ID_CV3)(NORTH) <= EOP_sgn_i_North(ID_CV3)(WEST);
	EOP_sgn_o_West(ID_CV3)(SOUTH) <= '0';
	EOP_sgn_o_West(ID_CV3)(LOCAL) <= EOP_sgn_i_Local(ID_CV3)(WEST);
	---- SOUTH ----
	-- EOP_sgn_o_South(ID_CV0)(EAST) <= EOP_sgn_i_East(ID_CV0)(SOUTH);
	-- EOP_sgn_o_South(ID_CV0)(WEST) <= EOP_sgn_i_West(ID_CV0)(SOUTH);
	-- EOP_sgn_o_South(ID_CV0)(NORTH) <= EOP_sgn_i_North(ID_CV0)(SOUTH);
	-- EOP_sgn_o_South(ID_CV0)(SOUTH) <= EOP_sgn_i_South(ID_CV0)(SOUTH);
	-- EOP_sgn_o_South(ID_CV0)(LOCAL) <= EOP_sgn_i_Local(ID_CV0)(SOUTH);
	
	-- EOP_sgn_o_South(ID_CV1)(EAST) <= EOP_sgn_i_East(ID_CV1)(SOUTH);
	-- EOP_sgn_o_South(ID_CV1)(WEST) <= EOP_sgn_i_West(ID_CV1)(SOUTH);
	-- EOP_sgn_o_South(ID_CV1)(NORTH) <= EOP_sgn_i_North(ID_CV1)(SOUTH);
	-- EOP_sgn_o_South(ID_CV1)(SOUTH) <= EOP_sgn_i_South(ID_CV1)(SOUTH);
	-- EOP_sgn_o_South(ID_CV1)(LOCAL) <= EOP_sgn_i_Local(ID_CV1)(SOUTH);
	
	-- EOP_sgn_o_South(ID_CV2)(EAST) <= EOP_sgn_i_East(ID_CV2)(SOUTH);
	-- EOP_sgn_o_South(ID_CV2)(WEST) <= EOP_sgn_i_West(ID_CV2)(SOUTH);
	-- EOP_sgn_o_South(ID_CV2)(NORTH) <= EOP_sgn_i_North(ID_CV2)(SOUTH);
	-- EOP_sgn_o_South(ID_CV2)(SOUTH) <= EOP_sgn_i_South(ID_CV2)(SOUTH);
	-- EOP_sgn_o_South(ID_CV2)(LOCAL) <= EOP_sgn_i_Local(ID_CV2)(SOUTH);
	
	-- EOP_sgn_o_South(ID_CV3)(EAST) <= EOP_sgn_i_East(ID_CV3)(SOUTH);
	-- EOP_sgn_o_South(ID_CV3)(WEST) <= EOP_sgn_i_West(ID_CV3)(SOUTH);
	-- EOP_sgn_o_South(ID_CV3)(NORTH) <= EOP_sgn_i_North(ID_CV3)(SOUTH);
	-- EOP_sgn_o_South(ID_CV3)(SOUTH) <= EOP_sgn_i_South(ID_CV3)(SOUTH);
	-- EOP_sgn_o_South(ID_CV3)(LOCAL) <= EOP_sgn_i_Local(ID_CV3)(SOUTH);
	---- LOCAL ----
	EOP_sgn_o_Local(ID_CV0)(EAST) <= EOP_sgn_i_East(ID_CV0)(LOCAL);
	EOP_sgn_o_Local(ID_CV0)(WEST) <= EOP_sgn_i_West(ID_CV0)(LOCAL);
	EOP_sgn_o_Local(ID_CV0)(NORTH) <= EOP_sgn_i_North(ID_CV0)(LOCAL);
	EOP_sgn_o_Local(ID_CV0)(SOUTH) <= '0';
	EOP_sgn_o_Local(ID_CV0)(LOCAL) <= EOP_sgn_i_Local(ID_CV0)(LOCAL);
	
	EOP_sgn_o_Local(ID_CV1)(EAST) <= EOP_sgn_i_East(ID_CV1)(LOCAL);
	EOP_sgn_o_Local(ID_CV1)(WEST) <= EOP_sgn_i_West(ID_CV1)(LOCAL);
	EOP_sgn_o_Local(ID_CV1)(NORTH) <= EOP_sgn_i_North(ID_CV1)(LOCAL);
	EOP_sgn_o_Local(ID_CV1)(SOUTH) <= '0';
	EOP_sgn_o_Local(ID_CV1)(LOCAL) <= EOP_sgn_i_Local(ID_CV1)(LOCAL);
	
	EOP_sgn_o_Local(ID_CV2)(EAST) <= EOP_sgn_i_East(ID_CV2)(LOCAL);
	EOP_sgn_o_Local(ID_CV2)(WEST) <= EOP_sgn_i_West(ID_CV2)(LOCAL);
	EOP_sgn_o_Local(ID_CV2)(NORTH) <= EOP_sgn_i_North(ID_CV2)(LOCAL);
	EOP_sgn_o_Local(ID_CV2)(SOUTH) <= '0';
	EOP_sgn_o_Local(ID_CV2)(LOCAL) <= EOP_sgn_i_Local(ID_CV2)(LOCAL);
	
	EOP_sgn_o_Local(ID_CV3)(EAST) <= EOP_sgn_i_East(ID_CV3)(LOCAL);
	EOP_sgn_o_Local(ID_CV3)(WEST) <= EOP_sgn_i_West(ID_CV3)(LOCAL);
	EOP_sgn_o_Local(ID_CV3)(NORTH) <= EOP_sgn_i_North(ID_CV3)(LOCAL);
	EOP_sgn_o_Local(ID_CV3)(SOUTH) <= '0';
	EOP_sgn_o_Local(ID_CV3)(LOCAL) <= EOP_sgn_i_Local(ID_CV3)(LOCAL);
-----------------------------	
-- Matando sinais de saída que não serão utilizados
	credit_o(SOUTH)<=(others=>'0');
	
	clock_tx(SOUTH)<='0';
	
	tx(SOUTH)<='0';

	lane_tx(SOUTH)<=(others=>'0');
        
	data_out(SOUTH)<=(others=>'0');
end RouterBC;
