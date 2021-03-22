library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;
use work.HermesG_package.all;

entity NOC is
port(
	$clock_noc$
	reset         : in  std_logic;
	clock_rxLocal : in  regNrot;
	rxLocal       : in  regNrot;
	data_inLocal  : in  arrayNrot_regflit;
	credit_oLocal : out regNrot;
	clock_txLocal : out regNrot;
	txLocal       : out regNrot;
	data_outLocal : out arrayNrot_regflit;
	credit_iLocal : in  regNrot);
end NOC;

architecture NOC of NOC is

$signal_router$
begin

$map_router$

$port_router$

end NOC;
