--------------------------------------------------------------------------
-- package com tipos basicos
--------------------------------------------------------------------------

library IEEE;
use IEEE.Std_Logic_1164.all;
use IEEE.std_logic_unsigned.all;
use IEEE.std_logic_arith.all;

package Hermes_Package is

---------------------------------------------------------
-- CONSTANTS INDEPENDENTES
---------------------------------------------------------

	-- Numero maximo de portas por roteador
	constant NPORT: integer := $n_port$;

	-- Numero de bits usados para representar as portas no roteamento na origem
	constant PORT_DEF: integer := 4;

	-- Identificacao de porta invalida
	constant INVALID_PORT: std_logic_vector(PORT_DEF-1 downto 0) := (others=>'1');

	constant EAST  : integer := 0;
	constant WEST  : integer := 1;
	constant NORTH : integer := 2;
	constant SOUTH : integer := 3;
	constant LOCAL : integer := 4;

	constant NLANE: integer := $n_lane$;
$n_lanes$

---------------------------------------------------------
-- CONSTANT DEPENDENTE DA LARGURA DE BANDA DA REDE
---------------------------------------------------------

	constant TAM_FLIT : integer range 1 to 64 := $flit_size$;
	constant FLIT_TERMINATOR: std_logic_vector(TAM_FLIT-1 downto 0) := (others=>'1');

---------------------------------------------------------
-- CONSTANTS DEPENDENTES DA PROFUNDIDADE DA FILA
---------------------------------------------------------

	constant TAM_BUFFER: integer := $buff_depth$;
	constant TAM_POINTER : integer range 1 to 32 := $pointer_size$;

---------------------------------------------------------
-- CONSTANTS DEPENDENTES DO NUMERO DE ROTEADORES
---------------------------------------------------------

	constant NROT: integer := $n_rot$;

---------------------------------------------------------
-- CONSTANT TB
---------------------------------------------------------
	constant TAM_LINHA : integer := 2; --4;

$n_nodos$
---------------------------------------------------------
-- SUBTIPOS, TIPOS E FUNCOES
---------------------------------------------------------

	subtype regIDport is std_logic_vector(PORT_DEF-1 downto 0);
	subtype regNrot is std_logic_vector((NROT-1) downto 0);
	subtype regNport is std_logic_vector((NPORT-1) downto 0);
	subtype regNlane is std_logic_vector((NLANE-1) downto 0);
	subtype regflit is std_logic_vector((TAM_FLIT-1) downto 0);
	subtype pointer is std_logic_vector((TAM_POINTER-1) downto 0);

	type out_buff is array(0 to NPORT) of regIDport;
	
	type arrayNport_regflit is array((NPORT-1) downto 0) of regflit;
	-- REGISTRO DIVIDIDO EM N PORTAS de tamanho igual a TAM_FLIT
  subtype regNportNflit is std_logic_vector (((NPORT*TAM_FLIT)-1) downto 0);
	
	type arrayNport_regNlane is array((NPORT-1) downto 0) of regNlane;
	type arrayNlane_regNport is array((NLANE-1) downto 0) of regNport;
	subtype regNportNlane is std_logic_vector(((NPORT*NLANE)-1) downto 0);

	type arrayNrot_regflit is array((NROT-1) downto 0) of regflit;
	subtype regNrotNflit is std_logic_vector(((NROT*TAM_FLIT)-1) downto 0);

	type arrayNrot_regNlane is array((NROT-1) downto 0) of regNlane;
	subtype regNrotNlane is std_logic_vector(((NROT*NLANE)-1) downto 0);

	type arrayNlane_regflit is array((NLANE-1) downto 0) of regflit;
	-- REGISTRO DIVIDIDO EM N Lanes de tamanho igual a TAM_FLIT
	subtype regNlaneNflit is std_logic_vector(((NLANE*TAM_FLIT)-1) downto 0);

	type matrixNport_Nlane_regflit is array((NPORT-1) downto 0) of arrayNlane_regflit;
	type matrixNlane_Nport_regflit is array((NLANE-1) downto 0) of arrayNport_regflit;
	
	-- REGISTRO DIVIDIDO EM N PORTS, cada NPORT dividida em NLANES de tamanho igual a TAM_FLIT
	subtype regNportNlaneNflit is std_logic_vector(((NLANE*NPORT*TAM_FLIT)-1) downto 0);

---------------------------------------------------------
-- FUNCOES TB
---------------------------------------------------------
	function CONV_VECTOR( letra : string(1 to TAM_LINHA);  pos: integer ) return std_logic_vector;
	function CONV_HEX( int : integer ) return string;
	function CONV_STRING_4BITS( dado : std_logic_vector(3 downto 0)) return string;
	function CONV_STRING_8BITS( dado : std_logic_vector(7 downto 0)) return string;
	function CONV_STRING_16BITS( dado : std_logic_vector(15 downto 0)) return string;
	function CONV_STRING_32BITS( dado : std_logic_vector(31 downto 0)) return string;

end Hermes_Package;

package body Hermes_Package is

---------------------------------------------------------
-- FUNCOES TB
---------------------------------------------------------
	--
	-- converte um caracter de uma dada linha em um std_logic_vector
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

	-- converte um inteiro em um string
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

end Hermes_Package;
