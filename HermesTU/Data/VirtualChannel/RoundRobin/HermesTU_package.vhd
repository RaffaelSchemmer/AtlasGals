--------------------------------------------------------------------------
-- PACKAGE WITH BASIC TYPES
--------------------------------------------------------------------------
library IEEE;
use IEEE.Std_Logic_1164.all;
use IEEE.std_logic_unsigned.all;
use IEEE.std_logic_arith.all;

package HermesTUPackage is

-----------------------------------------------------------------------
-- OCP PARAMETERS
-----------------------------------------------------------------------
------------------ Command Enconding - p. 13 --------------------------
constant IDLE: Std_Logic_Vector(2 downto 0) :="000";
constant WR:   Std_Logic_Vector(2 downto 0) :="001";
constant RD:   Std_Logic_Vector(2 downto 0) :="010";
constant RDEX: Std_Logic_Vector(2 downto 0) :="011";
constant BCST: Std_Logic_Vector(2 downto 0) :="111";
------------------ Response Enconding ---------------------------------
constant DVA:  Std_Logic_Vector(1 downto 0) :="01";
constant ERR:  Std_Logic_Vector(1 downto 0) :="11";
constant NULO: Std_Logic_Vector(1 downto 0) :="00";
constant ALVO: Std_Logic_Vector(7 downto 0) :="00000000";

---------------------------------------------------------
-- INDEPENDENT CONSTANTS
---------------------------------------------------------
constant NPORT: integer := $n_port$;

constant EAST  : integer := 0;
constant WEST  : integer := 1;
constant NORTH : integer := 2;
constant SOUTH : integer := 3;
constant LOCAL : integer := 4;

constant NLANE: integer := $n_lane$;
$n_lanes$

---------------------------------------------------------
-- CONSTANTS RELATED TO THE NET BANDWIDTH
---------------------------------------------------------
constant TAM_FLIT : integer range 1 to 64 := $flit_size$;
constant METADEFLIT : integer range 1 to 32 := (TAM_FLIT/2);
constant QUARTOFLIT : integer range 1 to 16 := (TAM_FLIT/4);

---------------------------------------------------------
-- CONSTANTS RELATED TO THE BUFFER DEPTH
---------------------------------------------------------
constant TAM_BUFFER: integer := $buff_depth$;
constant TAM_POINTER : integer range 1 to 32 := $pointer_size$;

---------------------------------------------------------
-- CONSTANTS RELATED TO THE NUMBER OF ROUTERS
---------------------------------------------------------
constant NROT: integer := $n_rot$;

constant MIN_X : integer := 0;
constant MIN_Y : integer := 0;
constant MAX_X : integer := $max_X$;
constant MAX_Y : integer := $max_Y$;

---------------------------------------------------------
-- TB CONSTANTS
---------------------------------------------------------
constant TAM_LINHA : integer := $tam_line$;

$n_nodos$

---------------------------------------------------------
-- SUBTYPES, TYPES AND FUNCTIONS
---------------------------------------------------------
subtype reg3 is std_logic_vector(2 downto 0);
subtype reg4 is std_logic_vector(3 downto 0);
subtype reg8 is std_logic_vector(7 downto 0);
subtype reg32 is std_logic_vector(31 downto 0);
subtype regNrot is std_logic_vector((NROT-1) downto 0);
subtype regNport is std_logic_vector((NPORT-1) downto 0);
subtype regNlane is std_logic_vector((NLANE-1) downto 0);
subtype regflit is std_logic_vector((TAM_FLIT-1) downto 0);
subtype regmetadeflit is std_logic_vector(((TAM_FLIT/2)-1) downto 0);
subtype regquartoflit is std_logic_vector((QUARTOFLIT-1) downto 0);
subtype pointer is std_logic_vector((TAM_POINTER-1) downto 0);

type buff is array(0 to TAM_BUFFER-1) of regflit;

type arrayNport_reg3 is array((NPORT-1) downto 0) of reg3;
type arrayNport_reg8 is array((NPORT-1) downto 0) of reg8;
type arrayNport_regflit is array((NPORT-1) downto 0) of regflit;
type arrayNport_regNlane is array((NPORT-1) downto 0) of regNlane;
type arrayNrot_reg3 is array((NROT-1) downto 0) of reg3;
type arrayNrot_regflit is array((NROT-1) downto 0) of regflit;
type arrayNrot_regNlane is array((NROT-1) downto 0) of regNlane;
type arrayNrot_regmetadeflit is array((NROT-1) downto 0) of regmetadeflit;
type arrayNlane_regflit is array((NLANE-1) downto 0) of regflit;
type arrayNlane_reg3 is array((NLANE-1) downto 0) of reg3;
type arrayNlane_reg8 is array((NLANE-1) downto 0) of reg8;

type matrixNport_Nlane_regflit is array((NPORT-1) downto 0) of arrayNlane_regflit;
type matrixNport_Nlane_reg8 is array((NPORT-1) downto 0) of arrayNlane_reg8;

function CONV_VECTOR( int: integer ) return std_logic_vector;

---------------------------------------------------------
-- VIRTUAL CHANNELS FUNCTIONS
---------------------------------------------------------
function conv_channel( porta: integer;lane:integer ) return std_logic_vector;
function conv_port( channel: reg8) return integer;
function conv_lane( channel: reg8) return integer;

---------------------------------------------------------
-- TB FUNCTIONS
---------------------------------------------------------
function CONV_VECTOR( letra : string(1 to TAM_LINHA);  pos: integer ) return std_logic_vector;
function CONV_HEX( int : integer ) return string;
function CONV_STRING_4BITS( dado : std_logic_vector(3 downto 0)) return string;
function CONV_STRING_8BITS( dado : std_logic_vector(7 downto 0)) return string;
function CONV_STRING_16BITS( dado : std_logic_vector(15 downto 0)) return string;
function CONV_STRING_32BITS( dado : std_logic_vector(31 downto 0)) return string;

end HermesTUPackage;

package body HermesTUPackage is

function conv_channel( porta: integer;lane:integer ) return std_logic_vector is
    variable bin: reg8 := (others=>'0');
    variable int: integer range 0 to (NPORT * NLANE);
    begin
        int := (porta * NLANE) + lane;
        bin := CONV_STD_LOGIC_VECTOR(int,8);
        return bin;
end conv_channel;

function conv_port( channel: reg8) return integer is
    variable porta: integer range 0 to 4 := 0;
    begin
        porta := CONV_INTEGER(channel) / NLANE;
        return porta;
end conv_port;

function conv_lane( channel: reg8) return integer is
    variable lane: integer range 0 to (NLANE-1) := 0;
    begin
        lane := CONV_INTEGER(channel) mod NLANE;
        return lane;
end conv_lane;

-- 
-- converts an integer to a std_logic_vector(2 downto 0)
-- 
function CONV_VECTOR( int: integer ) return std_logic_vector is
    variable bin: reg3 := (others=>'0');
    begin
        case(int) is
            when 0 => bin := "000";
            when 1 => bin := "001";
            when 2 => bin := "010";
            when 3 => bin := "011";
            when 4 => bin := "100";
            when 5 => bin := "101";
            when 6 => bin := "110";
            when 7 => bin := "111";
            when others => bin := "000";
        end case;
        return bin;
end CONV_VECTOR;

-- 
-- converts a character of a line to a std_logic_vector
-- 
function CONV_VECTOR( letra:string(1 to TAM_LINHA);  pos: integer ) return std_logic_vector is
    variable bin: std_logic_vector(3 downto 0) := (others=>'0');
    begin
        case (letra(pos)) is
            when '0' => bin := "0000";
            when '1' => bin := "0001";
            when '2' => bin := "0010";
            when '3' => bin := "0011";
            when '4' => bin := "0100";
            when '5' => bin := "0101";
            when '6' => bin := "0110";
            when '7' => bin := "0111";
            when '8' => bin := "1000";
            when '9' => bin := "1001";
            when 'A' => bin := "1010";
            when 'B' => bin := "1011";
            when 'C' => bin := "1100";
            when 'D' => bin := "1101";
            when 'E' => bin := "1110";
            when 'F' => bin := "1111";
            when others =>  bin := "0000";
        end case;
        return bin;
end CONV_VECTOR;

-- 
-- converts an integer to a string
-- 
function CONV_HEX( int: integer ) return string is
    variable str: string(1 to 1);
    begin
        case(int) is
            when 0 => str := "0";
            when 1 => str := "1";
            when 2 => str := "2";
            when 3 => str := "3";
            when 4 => str := "4";
            when 5 => str := "5";
            when 6 => str := "6";
            when 7 => str := "7";
            when 8 => str := "8";
            when 9 => str := "9";
            when 10 => str := "A";
            when 11 => str := "B";
            when 12 => str := "C";
            when 13 => str := "D";
            when 14 => str := "E";
            when 15 => str := "F";
            when others =>  str := "U";
        end case;
        return str;
end CONV_HEX;

function CONV_STRING_4BITS(dado : std_logic_vector(3 downto 0)) return string is
    variable str: string(1 to 1);
    begin
        str := CONV_HEX(CONV_INTEGER(dado));
        return str;
end CONV_STRING_4BITS;

function CONV_STRING_8BITS(dado : std_logic_vector(7 downto 0)) return string is
    variable str1,str2: string(1 to 1);
    variable str: string(1 to 2);
    begin
        str1 := CONV_STRING_4BITS(dado(7 downto 4));
        str2 := CONV_STRING_4BITS(dado(3 downto 0));
        str := str1 & str2;
        return str;
end CONV_STRING_8BITS;

function CONV_STRING_16BITS(dado : std_logic_vector(15 downto 0)) return string is
    variable str1,str2: string(1 to 2);
    variable str: string(1 to 4);
    begin
        str1 := CONV_STRING_8BITS(dado(15 downto 8));
        str2 := CONV_STRING_8BITS(dado(7 downto 0));
        str := str1 & str2;
        return str;
end CONV_STRING_16BITS;

function CONV_STRING_32BITS(dado : std_logic_vector(31 downto 0)) return string is
    variable str1,str2: string(1 to 4);
    variable str: string(1 to 8);
    begin
        str1 := CONV_STRING_16BITS(dado(31 downto 16));
        str2 := CONV_STRING_16BITS(dado(15 downto 0));
        str := str1 & str2;
        return str;
end CONV_STRING_32BITS;

end HermesTUPackage;
