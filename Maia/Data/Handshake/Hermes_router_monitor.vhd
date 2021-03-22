-------------------------------------------------------------------------------
-- Title      : Traffic Monitor for Router CC
-- Project    : Power Model
-------------------------------------------------------------------------------
-- File       : Hermes_monitorCC.vhd
-- Author     : Guilherme Guindani
-- Company    : GAPH
-- Created    : 2008-03-04
-- Last update: 2008-03-17
-- Platform   : ASIC-NOC
-- Standard   : VHDL'87
-------------------------------------------------------------------------------
-- Description: Traffic Monitor for flit reception. Monitors the amount of
-- flits received in a parametrizable time window.
-------------------------------------------------------------------------------
-- Copyright (c) 2008 
-------------------------------------------------------------------------------
-- Revisions  :
-- Date        Version  Author          Description
-- 2008-03-04  1.0      Guindani        Created
-- 2008-03-17  1.5      Guindani        Implemented the suggested modifications
--                                      (Moraes's Modifications)
-------------------------------------------------------------------------------
library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_arith.all;
use ieee.std_logic_signed.all;

use work.HermesPackage.all;

entity $Chave$ is
generic( 
time_window : std_logic_vector(15 downto 0) := x"$Parametrizable_time_window$"); -- Parametrizable time window (in clock cycles)

port(
	clock         : in  std_logic;                       -- Monitor's clock
    reset         : in  std_logic;                       -- Monitor's reset

$portcounter$

    clock_rx_in   : in  regNport;                        -- Flow control clock_rx input signal
    rx_in         : in  regNport;                        -- Flow control rx input signal
    credit_o_in   : in  regNport;                        -- Credit based credit_o input signal (Must set to '0' if handshake based)
    ack_rx_in     : in  regNport);                       -- Handshake ack_rx input signal (Must set to '0' if credit based)

		
end $Chave$;

architecture $Chave$ of $Chave$ is

  signal count_rst      : std_logic := '0';                               -- Resets the flit counter in all ports
  signal reached        : std_logic := '0';                               -- Signal wich informs that the time window was reached
  signal reg_save       : std_logic := '0';                               -- Saves the counter output for all ports
  signal clk_count      : std_logic_vector(15 downto 0) := (others=>'0'); -- Clock counter

$signalcounter$  

begin

$enablingCounter$

 -- Reseting the flit counter at all port
  count_rst <= reached or reset;

$counters$

-- purpose: Clock counter and time window checking
  -- type   : sequential
  -- inputs : clock, reset, clk_count
  -- outputs: reached, reg_save
  CLOCK_COUNTER: process (clock, reset, clk_count)
  begin  -- process CLOCK_COUNTER
    if reset = '1' then                     -- asynchronous reset (active high)
      reg_save  <= '0';
      reached   <= '0';
      clk_count <= (others=>'0');
    elsif clock'event and clock = '1' then  -- rising clock edge
      clk_count <= clk_count + 1;
      if clk_count = time_window then
        reg_save <= '1';
        reached  <= '0';
      elsif clk_count = time_window + 1 then
        clk_count <= x"0001";
        reached   <= '1';
        reg_save  <= '0';
      else
        reg_save <= '0';
        reached  <= '0';        
      end if;
    end if;
  end process CLOCK_COUNTER;

end $Chave$;