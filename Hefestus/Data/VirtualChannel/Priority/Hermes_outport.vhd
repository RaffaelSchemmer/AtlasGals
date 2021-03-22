----------------------------------------------------------------
--                                      OUTPORT
--                                     ----------------
--               DATA_AV ->|                    |-> CLOCK_TX
--                       DATA ->|                   |-> TX
--             DATA_ACK <-|                   |-> LANE_TX
--                        FREE ->|                   |-> DATA_OUT
--                   TAB_IN ->|                   |<- CREDIT_I
--                TAB_OUT ->|                   |
--       ALL_LANE_TX ->|                   |
--                                     ----------------
----------------------------------------------------------------

library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;
use work.HermesPackage.all;

entity Hermes_outport is
port(
	clock:       in  std_logic;
	reset:       in  std_logic;
	data_av:     in  arrayNport_regNlane;
	data:        in  matrixNport_Nlane_regflit;
	data_ack:    out regNlane;
	free:        in  regNlane;
	all_lane_tx: in  arrayNport_regNlane;
	tableIn:     in  arrayNlane_reg8;
	tableOut:    in  arrayNlane_reg8;
	clock_tx:    out std_logic;
	tx:          out std_logic;
	lane_tx:     out regNlane;
	data_out:    out regflit;
	credit_i:    in  regNlane);
end Hermes_outport;

architecture Hermes_outport_local of Hermes_outport is

$signal$
signal aux_lane_tx: regNlane := (others=>'0');
signal indice: reg8 := (others=>'0');

begin

	clock_tx <= clock;

$cs_local$
$tx$

	lane_tx <= aux_lane_tx;

$data_out_local$

$data_ack_local$
$process$

end Hermes_outport_local;

architecture Hermes_outport_east of Hermes_outport is

$signal$
signal aux_lane_tx: regNlane := (others=>'0');
signal indice: reg8 := (others=>'0');

begin

	clock_tx <= clock;

$cs_east$
$tx$

	lane_tx <= aux_lane_tx;

$data_out_east$

$data_ack_east$
$process$

end Hermes_outport_east;

architecture Hermes_outport_west of Hermes_outport is

$signal$
signal aux_lane_tx: regNlane := (others=>'0');
signal indice: reg8 := (others=>'0');

begin

	clock_tx <= clock;

$cs_west$
$tx$

	lane_tx <= aux_lane_tx;

$data_out_west$

$data_ack_west$
$process$

end Hermes_outport_west;

architecture Hermes_outport_north of Hermes_outport is

$signal$
signal aux_lane_tx: regNlane := (others=>'0');
signal indice: reg8 := (others=>'0');

begin

	clock_tx <= clock;

$cs_north$
$tx$

	lane_tx <= aux_lane_tx;

$data_out_north$

$data_ack_north$
$process$

end Hermes_outport_north;

architecture Hermes_outport_south of Hermes_outport is

$signal$
signal aux_lane_tx: regNlane := (others=>'0');
signal indice: reg8 := (others=>'0');

begin

	clock_tx <= clock;

$cs_south$
$tx$

	lane_tx <= aux_lane_tx;

$data_out_south$

$data_ack_south$
$process$

end Hermes_outport_SOUTH;
