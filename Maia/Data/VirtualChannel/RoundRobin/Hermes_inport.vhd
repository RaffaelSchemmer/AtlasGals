----------------------------------------------------------------------------------------
--                                             INPORT
--                                           ------------------
--                                           |                      |-> H
--                                           | ********** |<- ACK_H
--                                  RX ->| *  BUFFER * |
--                      LANE_RX ->| ********** |-> DATA_AV
--                      DATA_IN ->| ********** |-> DATA
--                         ACK_RX <-| *  BUFFER * |<- DATA_ACK
--                                            | ********** |
--                                            |                      |-> SENDER
--                                             -----------------
--  
----------------------------------------------------------------------------------------
library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;
use work.HermesPackage.all;

-- interface da Hermes_inport
entity Hermes_inport is
port(
	clock:     in  std_logic;
	reset:     in  std_logic;
	clock_rx:  in  std_logic;
	rx:        in  std_logic;
	data_in:   in  regflit;
	lane_rx:   in  regNlane;
	credit_o:  out regNlane;
	h:         out regNlane;
	ack_h:     in  regNlane;
	data_av:   out regNlane;
	data:      out arrayNlane_regflit;
	data_ack:  in  regNlane;
	sender:    out regNlane);
end Hermes_inport;

architecture Hermes_inport of Hermes_inport is
$rxSignals$
begin 

$rxChannels$
$bufChannels$

end Hermes_inport;