library IEEE;
use IEEE.Std_Logic_1164.all;
use IEEE.std_logic_unsigned.all;
use IEEE.std_logic_arith.all;
use work.HermesPackage.all;

package MonitorPackage is

  type arrayNrot_regNlane is array((NROT-1) downto 0) of regflit;
  type reg_cv is array ((NPORT-1) downto 0) of std_logic_vector(3 downto 0);
  type router_3port is array (2 downto 0) of std_logic_vector(15 downto 0);
  type router_4port is array (3 downto 0) of std_logic_vector(15 downto 0);
  type router_5port is array (4 downto 0) of std_logic_vector(15 downto 0);

end MonitorPackage;