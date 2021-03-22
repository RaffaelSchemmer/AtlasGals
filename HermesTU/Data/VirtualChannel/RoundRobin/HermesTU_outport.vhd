----------------------------------------------------------------
--                    OUTPORT
--                --------------
--      DATA_AV ->|            |-> CLOCK_TX
--         DATA ->|            |-> TX
--     DATA_ACK <-|            |-> LANE_TX
--         FREE ->|            |-> DATA_OUT
--     TABLE_IN ->|            |<- CREDIT_I
--    TABLE_OUT ->|            |
--  ALL_LANE_TX ->|            |
--                --------------
----------------------------------------------------------------

library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;
use work.HermesTUPackage.all;

entity HermesTU_outport is
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
end HermesTU_outport;

architecture HermesTU_outport_local of HermesTU_outport is

signal c1, c2 : std_logic := '0';
signal aux_lane_tx, last_lane_tx: regNlane := (others=>'0');
signal indice: reg8 := (others=>'0');

begin

    clock_tx <= clock;

    c1 <= '1' when free(L1)='0' and credit_i(L1)='1' and
            ((tableOut(L1)=x"00" and data_av(EAST)(L1)='1')   or
             (tableOut(L1)=x"01" and data_av(EAST)(L2)='1')   or
             (tableOut(L1)=x"02" and data_av(WEST)(L1)='1')   or
             (tableOut(L1)=x"03" and data_av(WEST)(L2)='1')   or
             (tableOut(L1)=x"04" and data_av(NORTH)(L1)='1')  or
             (tableOut(L1)=x"05" and data_av(NORTH)(L2)='1')  or
             (tableOut(L1)=x"06" and data_av(SOUTH)(L1)='1')  or
             (tableOut(L1)=x"07" and data_av(SOUTH)(L2)='1')) else
            '0';

    c2 <= '1' when free(L2)='0' and credit_i(L2)='1' and
            ((tableOut(L2)=x"00" and data_av(EAST)(L1)='1')   or
             (tableOut(L2)=x"01" and data_av(EAST)(L2)='1')   or
             (tableOut(L2)=x"02" and data_av(WEST)(L1)='1')   or
             (tableOut(L2)=x"03" and data_av(WEST)(L2)='1')   or
             (tableOut(L2)=x"04" and data_av(NORTH)(L1)='1')  or
             (tableOut(L2)=x"05" and data_av(NORTH)(L2)='1')  or
             (tableOut(L2)=x"06" and data_av(SOUTH)(L1)='1')  or
             (tableOut(L2)=x"07" and data_av(SOUTH)(L2)='1')) else
            '0';

    tx <= '1' when (c1='1' or c2='1') else '0';

    lane_tx <= aux_lane_tx;

    data_out <= data(EAST)(L1)  when indice=x"00" and (c1='1' or c2='1') else
            data(EAST)(L2)  when indice=x"01" and (c1='1' or c2='1') else
            data(WEST)(L1)  when indice=x"02" and (c1='1' or c2='1') else
            data(WEST)(L2)  when indice=x"03" and (c1='1' or c2='1') else
            data(NORTH)(L1) when indice=x"04" and (c1='1' or c2='1') else
            data(NORTH)(L2) when indice=x"05" and (c1='1' or c2='1') else
            data(SOUTH)(L1) when indice=x"06" and (c1='1' or c2='1') else
            data(SOUTH)(L2) when indice=x"07" and (c1='1' or c2='1') else
            (others=>'0');

    data_ack(L1) <= all_lane_tx(EAST)(L1)  when tableIn(L1)=x"00" and data_av(LOCAL)(L1)='1' else
            all_lane_tx(EAST)(L2)  when tableIn(L1)=x"01" and data_av(LOCAL)(L1)='1' else
            all_lane_tx(WEST)(L1)  when tableIn(L1)=x"02" and data_av(LOCAL)(L1)='1' else
            all_lane_tx(WEST)(L2)  when tableIn(L1)=x"03" and data_av(LOCAL)(L1)='1' else
            all_lane_tx(NORTH)(L1) when tableIn(L1)=x"04" and data_av(LOCAL)(L1)='1' else
            all_lane_tx(NORTH)(L2) when tableIn(L1)=x"05" and data_av(LOCAL)(L1)='1' else
            all_lane_tx(SOUTH)(L1) when tableIn(L1)=x"06" and data_av(LOCAL)(L1)='1' else
            all_lane_tx(SOUTH)(L2) when tableIn(L1)=x"07" and data_av(LOCAL)(L1)='1' else
            '0';

    data_ack(L2) <= all_lane_tx(EAST)(L1)  when tableIn(L2)=x"00" and data_av(LOCAL)(L2)='1' else
            all_lane_tx(EAST)(L2)  when tableIn(L2)=x"01" and data_av(LOCAL)(L2)='1' else
            all_lane_tx(WEST)(L1)  when tableIn(L2)=x"02" and data_av(LOCAL)(L2)='1' else
            all_lane_tx(WEST)(L2)  when tableIn(L2)=x"03" and data_av(LOCAL)(L2)='1' else
            all_lane_tx(NORTH)(L1) when tableIn(L2)=x"04" and data_av(LOCAL)(L2)='1' else
            all_lane_tx(NORTH)(L2) when tableIn(L2)=x"05" and data_av(LOCAL)(L2)='1' else
            all_lane_tx(SOUTH)(L1) when tableIn(L2)=x"06" and data_av(LOCAL)(L2)='1' else
            all_lane_tx(SOUTH)(L2) when tableIn(L2)=x"07" and data_av(LOCAL)(L2)='1' else
            '0';

    process(reset, clock)
    begin
        if reset='1' then
            last_lane_tx <= (others=>'0');
        elsif clock'event and clock='1' then
            last_lane_tx <= aux_lane_tx;
        end if;
    end process;

    process(last_lane_tx,c1,c2)
    begin
        case last_lane_tx is
        when "01" =>
            if c2='1'    then aux_lane_tx<="10"; indice <= tableOut(L2);
            elsif c1='1' then aux_lane_tx<="01"; indice <= tableOut(L1);
            else aux_lane_tx<="00"; end if;
        when "00" | "10" =>
            if c1='1'    then aux_lane_tx<="01"; indice <= tableOut(L1);
            elsif c2='1' then aux_lane_tx<="10"; indice <= tableOut(L2);
            else aux_lane_tx<="00"; end if;
        when others => aux_lane_tx<="00";
        end case;
    end process;

end HermesTU_outport_local;

architecture HermesTU_outport_east of HermesTU_outport is

signal c1, c2 : std_logic := '0';
signal aux_lane_tx, last_lane_tx: regNlane := (others=>'0');
signal indice: reg8 := (others=>'0');

begin

    clock_tx <= clock;

    c1 <= '1' when free(L1)='0' and credit_i(L1)='1' and
            ((tableOut(L1)=x"02" and data_av(WEST)(L1)='1')   or
             (tableOut(L1)=x"03" and data_av(WEST)(L2)='1')   or
             (tableOut(L1)=x"08" and data_av(LOCAL)(L1)='1')  or
             (tableOut(L1)=x"09" and data_av(LOCAL)(L2)='1')) else
            '0';

    c2 <= '1' when free(L2)='0' and credit_i(L2)='1' and
            ((tableOut(L2)=x"02" and data_av(WEST)(L1)='1')   or
             (tableOut(L2)=x"03" and data_av(WEST)(L2)='1')   or
             (tableOut(L2)=x"08" and data_av(LOCAL)(L1)='1')  or
             (tableOut(L2)=x"09" and data_av(LOCAL)(L2)='1')) else
            '0';

    tx <= '1' when (c1='1' or c2='1') else '0';

    lane_tx <= aux_lane_tx;

    data_out <= data(WEST)(L1)  when indice=x"02" and(c1='1' or c2='1') else
            data(WEST)(L2)  when indice=x"03" and(c1='1' or c2='1') else
            data(LOCAL)(L1) when indice=x"08" and (c1='1' or c2='1') else
            data(LOCAL)(L2) when indice=x"09" and (c1='1' or c2='1') else
            (others=>'0');

    data_ack(L1) <= all_lane_tx(WEST)(L1)  when tableIn(L1)=x"02" and data_av(EAST)(L1)='1' else
            all_lane_tx(WEST)(L2)  when tableIn(L1)=x"03" and data_av(EAST)(L1)='1' else
            all_lane_tx(NORTH)(L1) when tableIn(L1)=x"04" and data_av(EAST)(L1)='1' else
            all_lane_tx(NORTH)(L2) when tableIn(L1)=x"05" and data_av(EAST)(L1)='1' else
            all_lane_tx(SOUTH)(L1) when tableIn(L1)=x"06" and data_av(EAST)(L1)='1' else
            all_lane_tx(SOUTH)(L2) when tableIn(L1)=x"07" and data_av(EAST)(L1)='1' else
            all_lane_tx(LOCAL)(L1) when tableIn(L1)=x"08" and data_av(EAST)(L1)='1' else
            all_lane_tx(LOCAL)(L2) when tableIn(L1)=x"09" and data_av(EAST)(L1)='1' else
            '0';

    data_ack(L2) <= all_lane_tx(WEST)(L1)  when tableIn(L2)=x"02" and data_av(EAST)(L2)='1' else
            all_lane_tx(WEST)(L2)  when tableIn(L2)=x"03" and data_av(EAST)(L2)='1' else
            all_lane_tx(NORTH)(L1) when tableIn(L2)=x"04" and data_av(EAST)(L2)='1' else
            all_lane_tx(NORTH)(L2) when tableIn(L2)=x"05" and data_av(EAST)(L2)='1' else
            all_lane_tx(SOUTH)(L1) when tableIn(L2)=x"06" and data_av(EAST)(L2)='1' else
            all_lane_tx(SOUTH)(L2) when tableIn(L2)=x"07" and data_av(EAST)(L2)='1' else
            all_lane_tx(LOCAL)(L1) when tableIn(L2)=x"08" and data_av(EAST)(L2)='1' else
            all_lane_tx(LOCAL)(L2) when tableIn(L2)=x"09" and data_av(EAST)(L2)='1' else
            '0';

    process(reset, clock)
    begin
        if reset='1' then
            last_lane_tx <= (others=>'0');
        elsif clock'event and clock='1' then
            last_lane_tx <= aux_lane_tx;
        end if;
    end process;

    process(last_lane_tx,c1,c2)
    begin
        case last_lane_tx is
        when "01" =>
            if c2='1'    then aux_lane_tx<="10"; indice <= tableOut(L2);
            elsif c1='1' then aux_lane_tx<="01"; indice <= tableOut(L1);
            else aux_lane_tx<="00"; end if;
        when "00" | "10" =>
            if c1='1'    then aux_lane_tx<="01"; indice <= tableOut(L1);
            elsif c2='1' then aux_lane_tx<="10"; indice <= tableOut(L2);
            else aux_lane_tx<="00"; end if;
        when others => aux_lane_tx<="00";
        end case;
    end process;

end HermesTU_outport_east;

architecture HermesTU_outport_north of HermesTU_outport is

signal c1, c2 : std_logic := '0';
signal aux_lane_tx, last_lane_tx: regNlane := (others=>'0');
signal indice: reg8 := (others=>'0');

begin

    clock_tx <= clock;

    c1 <= '1' when free(L1)='0' and credit_i(L1)='1' and
            ((tableOut(L1)=x"00" and data_av(EAST)(L1)='1')   or
             (tableOut(L1)=x"01" and data_av(EAST)(L2)='1')   or
             (tableOut(L1)=x"02" and data_av(WEST)(L1)='1')   or
             (tableOut(L1)=x"03" and data_av(WEST)(L2)='1')   or
             (tableOut(L1)=x"06" and data_av(SOUTH)(L1)='1')  or
             (tableOut(L1)=x"07" and data_av(SOUTH)(L2)='1')  or
             (tableOut(L1)=x"08" and data_av(LOCAL)(L1)='1')  or
             (tableOut(L1)=x"09" and data_av(LOCAL)(L2)='1')) else
            '0';

    c2 <= '1' when free(L2)='0' and credit_i(L2)='1' and
            ((tableOut(L2)=x"00" and data_av(EAST)(L1)='1')   or
             (tableOut(L2)=x"01" and data_av(EAST)(L2)='1')   or
             (tableOut(L2)=x"02" and data_av(WEST)(L1)='1')   or
             (tableOut(L2)=x"03" and data_av(WEST)(L2)='1')   or
             (tableOut(L2)=x"06" and data_av(SOUTH)(L1)='1')  or
             (tableOut(L2)=x"07" and data_av(SOUTH)(L2)='1')  or
             (tableOut(L2)=x"08" and data_av(LOCAL)(L1)='1')  or
             (tableOut(L2)=x"09" and data_av(LOCAL)(L2)='1')) else
            '0';

    tx <= '1' when (c1='1' or c2='1') else '0';

    lane_tx <= aux_lane_tx;

    data_out <= data(EAST)(L1)  when indice=x"00" and (c1='1' or c2='1') else
            data(EAST)(L2)  when indice=x"01" and (c1='1' or c2='1') else
            data(WEST)(L1)  when indice=x"02" and (c1='1' or c2='1') else
            data(WEST)(L2)  when indice=x"03" and (c1='1' or c2='1') else
            data(SOUTH)(L1) when indice=x"06" and (c1='1' or c2='1') else
            data(SOUTH)(L2) when indice=x"07" and (c1='1' or c2='1') else
            data(LOCAL)(L1) when indice=x"08" and (c1='1' or c2='1') else
            data(LOCAL)(L2) when indice=x"09" and (c1='1' or c2='1') else
            (others=>'0');

    data_ack(L1) <= all_lane_tx(SOUTH)(L1) when tableIn(L1)=x"06" and data_av(NORTH)(L1)='1' else
            all_lane_tx(SOUTH)(L2) when tableIn(L1)=x"07" and data_av(NORTH)(L1)='1' else
            all_lane_tx(LOCAL)(L1) when tableIn(L1)=x"08" and data_av(NORTH)(L1)='1' else
            all_lane_tx(LOCAL)(L2) when tableIn(L1)=x"09" and data_av(NORTH)(L1)='1' else
            '0';

    data_ack(L2) <= all_lane_tx(SOUTH)(L1) when tableIn(L2)=x"06" and data_av(NORTH)(L2)='1' else
            all_lane_tx(SOUTH)(L2) when tableIn(L2)=x"07" and data_av(NORTH)(L2)='1' else
            all_lane_tx(LOCAL)(L1) when tableIn(L2)=x"08" and data_av(NORTH)(L2)='1' else
            all_lane_tx(LOCAL)(L2) when tableIn(L2)=x"09" and data_av(NORTH)(L2)='1' else
            '0';

    process(reset, clock)
    begin
        if reset='1' then
            last_lane_tx <= (others=>'0');
        elsif clock'event and clock='1' then
            last_lane_tx <= aux_lane_tx;
        end if;
    end process;

    process(last_lane_tx,c1,c2)
    begin
        case last_lane_tx is
        when "01" =>
            if c2='1'    then aux_lane_tx<="10"; indice <= tableOut(L2);
            elsif c1='1' then aux_lane_tx<="01"; indice <= tableOut(L1);
            else aux_lane_tx<="00"; end if;
        when "00" | "10" =>
            if c1='1'    then aux_lane_tx<="01"; indice <= tableOut(L1);
            elsif c2='1' then aux_lane_tx<="10"; indice <= tableOut(L2);
            else aux_lane_tx<="00"; end if;
        when others => aux_lane_tx<="00";
        end case;
    end process;

end HermesTU_outport_north;

----------------------------------------------------------------
--                OUTPORT_MINIMAL
--                ---------------
--      DATA_AV ->|             |
--     DATA_ACK <-|             |
--     TABLE_IN ->|             |<- CREDIT_I
--  ALL_LANE_TX ->|             |
--                ---------------
----------------------------------------------------------------

library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;
use work.HermesTUPackage.all;

entity HermesTU_outport_minimal is
port(
    data_av:     in  arrayNport_regNlane;
    data_ack:    out regNlane;
    all_lane_tx: in  arrayNport_regNlane;
    tableIn:     in  arrayNlane_reg8;
    credit_i:    in  regNlane);
end HermesTU_outport_minimal;

architecture HermesTU_outport_west of HermesTU_outport_minimal is

begin

    data_ack(L1) <= all_lane_tx(EAST)(L1)  when tableIn(L1)=x"00" and data_av(WEST)(L1)='1' else
            all_lane_tx(EAST)(L2)  when tableIn(L1)=x"01" and data_av(WEST)(L1)='1' else
            all_lane_tx(NORTH)(L1) when tableIn(L1)=x"04" and data_av(WEST)(L1)='1' else
            all_lane_tx(NORTH)(L2) when tableIn(L1)=x"05" and data_av(WEST)(L1)='1' else
            all_lane_tx(SOUTH)(L1) when tableIn(L1)=x"06" and data_av(WEST)(L1)='1' else
            all_lane_tx(SOUTH)(L2) when tableIn(L1)=x"07" and data_av(WEST)(L1)='1' else
            all_lane_tx(LOCAL)(L1) when tableIn(L1)=x"08" and data_av(WEST)(L1)='1' else
            all_lane_tx(LOCAL)(L2) when tableIn(L1)=x"09" and data_av(WEST)(L1)='1' else
            '0';

    data_ack(L2) <= all_lane_tx(EAST)(L1)  when tableIn(L2)=x"00" and data_av(WEST)(L2)='1' else
            all_lane_tx(EAST)(L2)  when tableIn(L2)=x"01" and data_av(WEST)(L2)='1' else
            all_lane_tx(NORTH)(L1) when tableIn(L2)=x"04" and data_av(WEST)(L2)='1' else
            all_lane_tx(NORTH)(L2) when tableIn(L2)=x"05" and data_av(WEST)(L2)='1' else
            all_lane_tx(SOUTH)(L1) when tableIn(L2)=x"06" and data_av(WEST)(L2)='1' else
            all_lane_tx(SOUTH)(L2) when tableIn(L2)=x"07" and data_av(WEST)(L2)='1' else
            all_lane_tx(LOCAL)(L1) when tableIn(L2)=x"08" and data_av(WEST)(L2)='1' else
            all_lane_tx(LOCAL)(L2) when tableIn(L2)=x"09" and data_av(WEST)(L2)='1' else
            '0';

end HermesTU_outport_west;

architecture HermesTU_outport_south of HermesTU_outport_minimal is

begin

    data_ack(L1) <= all_lane_tx(NORTH)(L1) when tableIn(L1)=x"04" and data_av(SOUTH)(L1)='1' else
            all_lane_tx(NORTH)(L2) when tableIn(L1)=x"05" and data_av(SOUTH)(L1)='1' else
            all_lane_tx(LOCAL)(L1) when tableIn(L1)=x"08" and data_av(SOUTH)(L1)='1' else
            all_lane_tx(LOCAL)(L2) when tableIn(L1)=x"09" and data_av(SOUTH)(L1)='1' else
            '0';

    data_ack(L2) <= all_lane_tx(NORTH)(L1) when tableIn(L2)=x"04" and data_av(SOUTH)(L2)='1' else
            all_lane_tx(NORTH)(L2) when tableIn(L2)=x"05" and data_av(SOUTH)(L2)='1' else
            all_lane_tx(LOCAL)(L1) when tableIn(L2)=x"08" and data_av(SOUTH)(L2)='1' else
            all_lane_tx(LOCAL)(L2) when tableIn(L2)=x"09" and data_av(SOUTH)(L2)='1' else
            '0';

end HermesTU_outport_SOUTH;
