-------------------------------------------------------------------------------
-- Title      : Traffic Monitor for Router CC
-- Project    : Power Model
-------------------------------------------------------------------------------
-- File       : Hermes_monitorCC.vhd
-- Author     : Guilherme Guindani
-- Company    : GAPH
-- Created    : 2008-03-04
-- Last update: 2010-10-27
-- Platform   : ASIC-NOC
-- Standard   : VHDL'87
-------------------------------------------------------------------------------
-- Description: Traffic Monitor for flit reception. Monitors the amount of
-- flits received in a parametrizable time window.
-- How to use: (ONLY IN THE HERMES NoC)
-- Place the monitor in the file NoC.vhd, intanciating one
-- monitor per router. Note there is a monitor architecture for
-- each Router type presented in the Hermes NoC. There are
-- some modifications to be made on the port map for the 
-- HANDSHAKE, CREDIT NO VIRTUAL CHANNELS,
-- CREDIT 2 VIRTUAL CHANNELS and CREDIT 4 VIRTUAL
-- CHANNELS respectivelly. These modfications are:
--
-- Hermes NoC HANDSHAKE:
-- The signals "clock_rx_in", "credit_o_in" and "lane_in"
-- must be set to "(others=>'0')".
--
-- Hermes NoC CREDIT NO VIRTUAL CHANNELS:
-- The signal "ack_rx_in" must be set to (others=>'0');
-- In the singal "credit_o_in" only the first bit of all positions
-- of the must be mapped, the others must be set to "(others=>'0');
-- In the signal "lane_in" the first bit of all positions must set
-- set to '1', all other bits must be set to '0'.
--
-- Hermes NoC CREDIT 2 VIRTUAL CHANNELS
-- The signal "ack_rx_in" must be set to (others=>'0');
-- In the signal "credit_o_in" only the positions 1 and 0 of the
-- array must be mapped, the others must be set to "(others=>'0')";
-- In the signal "lane_in" only the positions 1 and 0 of the array
-- must be mapped, the others must be set to "(others=>'0')".
--
-- Hermes NoC CREDIT 4 VIRTUAL CHANNELS
-- The signal "ack_rx_in" must be set to (others=>'0');
-- In the signal "credit_o_in" all positions of the array must be
-- mapped;
-- In the signal "lane_in" all positions of the array must be
-- mapped.
-------------------------------------------------------------------------------
-- Copyright (c) 2008 
-------------------------------------------------------------------------------
-- Revisions  :
-- Date        Version  Author          Description
-- 2008-03-04  1.0      Guindani        Created
-- 2008-03-17  1.5      Guindani        Implemented the suggested modifications  (Moraes's Modifications)
-- 2008-04-29 1.6      Guindani        Introduced the virtual channels handling
-------------------------------------------------------------------------------
library ieee;
use ieee.std_logic_1164.all;
use ieee.std_logic_arith.all;
use ieee.std_logic_signed.all;

use work.HermesPackage.all;
use work.MonitorPackage.all;

entity $Chave$_monitor is
  
	generic (
	time_window : std_logic_vector($Parametrizable_size_vector$ downto 0) := x"$Parametrizable_time_window$"); -- Parametrizable time window (in clock cycles)

	port (
	clock         : in  std_logic;				-- Monitor's clock
	reset         : in  std_logic;				-- Monitor's reset
$portcounter$    
	clock_rx_in   : in  regNport;				-- Flow control clock_rx input signal
	rx_in         : in  regNport;				-- Flow control rx input signal
	credit_o_in   : in  reg_cv;				-- Credit based credit_o input signal	                     
	lane_in       : in  reg_cv;				-- Virtual channels input signals
	ack_rx_in     : in  regNport);				-- Handshake ack_rx input signal (Must set to '0' if credit based)
    
end $Chave$_monitor;

architecture $Chave$_monitor of $Chave$_monitor is

$signalcounter$
  
	signal count_rst      : std_logic := '0';		-- Resets the flit counter in all ports
	signal reached        : std_logic := '0';		-- Signal wich informs that the time window was reached
	signal reg_save       : std_logic := '0';		-- Saves the counter output for all ports
	signal clk_count      : std_logic_vector($Parametrizable_size_vector$ downto 0) := (others=>'0'); -- Clock counter
 
begin  -- flit_monitor

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
		if reset = '1' then				-- asynchronous reset (active high)
			reg_save  <= '0';
			reached   <= '0';
			clk_count <= (others=>'0');
		elsif clock'event and clock = '1' then		-- rising clock edge
			clk_count <= clk_count + 1;
			if clk_count = time_window then
				reg_save <= '1';
				reached  <= '0';
			elsif clk_count = time_window + 1 then
				clk_count <= (others=>'0');
				reached   <= '1';
				reg_save  <= '0';
			else
				reg_save <= '0';
				reached  <= '0';        
			end if;
		end if;
	end process CLOCK_COUNTER;
  
end $Chave$_monitor;
