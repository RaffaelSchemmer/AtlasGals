---------------------------------------------------------------------------------------
--                    INPORT
--                --------------
--                |            |-> H
--                | ********** |<- ACK_H
--           RX ->| * BUFFER * |
--      LANE_RX ->| ********** |-> DATA_AV
--      DATA_IN ->| ********** |-> DATA
--       ACK_RX <-| * BUFFER * |<- DATA_ACK
--                | ********** |
--                |            |-> SENDER
--                --------------
--
---------------------------------------------------------------------------------------
library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;
use work.HermesTUPackage.all;

-- interface da HermesTU_inport
entity HermesTU_inport is
port(
    clock:          in  std_logic;
    reset:          in  std_logic;
    clock_rx:       in  std_logic;
    rx:             in  std_logic;
    data_in:        in  regflit;
    lane_rx:        in  regNlane;
    credit_o:       out regNlane;
    h:              out regNlane;
    ack_h:          in  regNlane;
    data_av:        out regNlane;
    data:           out arrayNlane_regflit;
    data_ack:       in  regNlane;
    sender:         out regNlane);
end HermesTU_inport;

architecture HermesTU_inport of HermesTU_inport is
signal rxL1, rxL2: std_logic := '0';
begin

    rxL1<= '1' when rx='1' and lane_rx(L1)='1' else '0';
    rxL2<= '1' when rx='1' and lane_rx(L2)='1' else '0';

    BUFL1: entity work.HermesTU_buffer
    port map(
        clock => clock,
        reset => reset,
        clock_rx => clock_rx,
        rx => rxL1,
        data_in => data_in,
        credit_o => credit_o(L1),
        h => h(L1),
        ack_h => ack_h(L1),
        data_av => data_av(L1),
        data => data(L1),
        data_ack => data_ack(L1),
        sender => sender(L1));

    BUFL2: entity work.HermesTU_buffer
    port map(
        clock => clock,
        reset => reset,
        clock_rx => clock_rx,
        rx => rxL2,
        data_in => data_in,
        credit_o => credit_o(L2),
        h => h(L2),
        ack_h => ack_h(L2),
        data_av => data_av(L2),
        data => data(L2),
        data_ack => data_ack(L2),
        sender => sender(L2));

end HermesTU_inport;
