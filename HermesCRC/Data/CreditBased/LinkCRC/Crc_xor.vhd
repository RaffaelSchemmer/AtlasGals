library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;

entity crc4p is
	port(
		input:	in	std_logic_vector(15 downto 0);
		output: out std_logic_vector(3 downto 0)
	);
end crc4p;

architecture crc4p of crc4p is

	type shift_vector is array(0 to 15) of std_logic_vector(3 downto 0);
	signal shift: shift_vector;

begin
	
	output(3) <= input(15) xor input(14) xor input(10) xor input(7) xor input(6) xor input(4) xor input(2) xor input(1) xor input(0);
	output(2) <= input(13) xor input(10) xor input(9)  xor input(7) xor input(5) xor input(4) xor input(3) xor input(2);
	output(1) <= input(12) xor input(9)  xor input(8)  xor input(6) xor input(4) xor input(3) xor input(2) xor input(1);
	output(0) <= input(15) xor input(11) xor input(8)  xor input(7) xor input(5) xor input(3) xor input(2) xor input(1) xor input(0);


end crc4p;

library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;

entity deccrc is
	port(
		input:	in  std_logic_vector(15 downto 0);
		crc_in:	in  std_logic_vector(3 downto 0);
		rx:	in  std_logic;
		error:	out std_logic
	);
end deccrc;

architecture deccrc of deccrc is

	signal crc_sig: std_logic_vector(3 downto 0);

begin

	CRCU: entity work.crc4p port map(input=>input,output=>crc_sig);
	
	error <= '1' when rx='1' and crc_sig/=crc_in else '0';


end deccrc;


library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;

entity ec_module is
	port(
		crc_out:   out std_logic_vector(3 downto 0);
		data_inc:  in  std_logic_vector(15 downto 0);
		crc_in:    in  std_logic_vector(3 downto 0);
		data_ind:  in  std_logic_vector(15 downto 0);
		rx_in:	   in  std_logic;
		error:	   out std_logic;
		--
		error_in:   in  std_logic;
		credit_in:  in  std_logic;
		credit_out: out std_logic
	);
end ec_module;

architecture ec_module of ec_module is

begin
	coder: entity work.crc4p
	port map(
		input => data_inc,
		output => crc_out
	);

	decoder: entity work.deccrc
	port map(
		input => data_ind,
		crc_in => crc_in,
		rx => rx_in,
		error => error
	);

	credit_out <= credit_in;-- and (not error_in);


end ec_module;

library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;

entity hammcoder is
	port(
		input:	in  std_logic_vector(15 downto 0);
		output: out std_logic_vector(4 downto 0)
	);
end hammcoder;

architecture hammcoder of hammcoder is

begin	
	output(4) <= input(15) xor input(12) xor input(10) xor input(9) xor input(6) xor input(5) xor input(4) xor input(3) xor input(2);
	output(3) <= input(14) xor input(11) xor input(9)  xor input(8) xor input(5) xor input(4) xor input(3) xor input(2) xor input(1);
	output(2) <= input(15) xor input(13) xor input(12) xor input(9) xor input(8) xor input(7) xor input(6) xor input(5) xor input(1) xor input(0);
	output(1) <= input(14) xor input(12) xor input(11) xor input(8) xor input(7) xor input(6) xor input(5) xor input(4) xor input(0);
	output(0) <= input(13) xor input(11) xor input(10) xor input(7) xor input(6) xor input(5) xor input(4) xor input(3);

end hammcoder;

library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;

entity hammdec is
	port(
		input:	in  std_logic_vector(20 downto 0);
		output: out std_logic_vector(20 downto 0)
	);
end hammdec;

architecture hammdec of hammdec is

	signal sindrome: std_logic_vector(4 downto 0);
	signal mask: std_logic_vector(20 downto 0);

begin	
	sindrome(4) <= input(20) xor input(15) xor input(12) xor input(10)  xor input(9) xor input(6) xor input(5) xor input(4) xor input(3) xor input(2);
	sindrome(3) <= input(19) xor input(14) xor input(11) xor input(9)   xor input(8) xor input(5) xor input(4) xor input(3) xor input(2) xor input(1);
	sindrome(2) <= input(18) xor input(15) xor input(13) xor input(12)  xor input(9) xor input(8) xor input(7) xor input(6) xor input(5) xor input(1) xor input(0);
	sindrome(1) <= input(17) xor input(14) xor input(12) xor input(11)  xor input(8) xor input(7) xor input(6) xor input(5) xor input(4) xor input(0);
	sindrome(0) <= input(16) xor input(13) xor input(11) xor input(10)  xor input(7) xor input(6) xor input(5) xor input(4) xor input(3);

	mask <= "000000000000000000000" when sindrome="00000" else
		"100000000000000000000" when sindrome="10000" else
		"010000000000000000000" when sindrome="01000" else
		"001000000000000000000" when sindrome="00100" else
		"000100000000000000000" when sindrome="00010" else
		"000010000000000000000" when sindrome="00001" else
		"000001000000000000000" when sindrome="10100" else
		"000000100000000000000" when sindrome="01010" else
		"000000010000000000000" when sindrome="00101" else
		"000000001000000000000" when sindrome="10110" else
		"000000000100000000000" when sindrome="01011" else
		"000000000010000000000" when sindrome="10001" else
		"000000000001000000000" when sindrome="11100" else
		"000000000000100000000" when sindrome="01110" else
		"000000000000010000000" when sindrome="00111" else
		"000000000000001000000" when sindrome="10111" else
		"000000000000000100000" when sindrome="11111" else
		"000000000000000010000" when sindrome="11011" else
		"000000000000000001000" when sindrome="11001" else
		"000000000000000000100" when sindrome="11000" else
		"000000000000000000010" when sindrome="01100" else
		"000000000000000000001" when sindrome="00110" else
		(others =>'0');
		
	output <= input xor mask;

end hammdec;