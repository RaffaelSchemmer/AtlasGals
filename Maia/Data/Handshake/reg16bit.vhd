-------------------------------------------------------------------------------
-- Title      : Generic 16 bit Register
-- Project    : MR4
-------------------------------------------------------------------------------
-- File       : reg16bit.vhd
-- Author     : Guilherme Guindani
-- Company    : GAPH
-- Created    : 2008-03-04
-- Last update: 2008-03-17
-- Platform   : ASIC-NOC
-- Standard   : VHDL'87
-------------------------------------------------------------------------------
-- Description: Generic 16 bit register
-------------------------------------------------------------------------------
-- Copyright (c) 2008 
-------------------------------------------------------------------------------
-- Revisions  :
-- Date        Version  Author          Description
-- 2008-03-04  1.0      Moraes          Created
-------------------------------------------------------------------------------
library IEEE;
use IEEE.std_logic_1164.all;

entity reg16bit is
           generic(
             INIT_VALUE : std_logic_vector(15 downto 0) := (others=>'0'));
           port(
             ck, rst, ce : in std_logic;
             D : in  std_logic_vector (15 downto 0);
             Q : out std_logic_vector (15 downto 0));
end reg16bit;

architecture reg16bit of reg16bit is 
begin

  process(ck, rst)
  begin
       if rst = '1' then
              Q <= INIT_VALUE(15 downto 0);
       elsif ck'event and ck = '0' then
           if ce = '1' then
              Q <= D; 
           end if;
       end if;
  end process;
        
end reg16bit;