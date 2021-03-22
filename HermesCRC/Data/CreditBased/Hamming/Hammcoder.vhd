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


--       4     3     2     1     0
--
--15     1     0     1     0     0 
--14     0     1     0     1     0 
--13     0     0     1     0     1 
--12     1     0     1     1     0 
--11     0     1     0     1     1 
--10     1     0     0     0     1 
--09     1     1     1     0     0 
--08     0     1     1     1     0 
--07     0     0     1     1     1 
--06     1     0     1     1     1 
--05     1     1     1     1     1 
--04     1     1     0     1     1 
--03     1     1     0     0     1 
--02     1     1     0     0     0 
--01     0     1     1     0     0 
--00     0     0     1     1     0 
--16     0     0     0     1     1 
--17     1     0     1     0     1 
--18     1     1     1     1     0 
--19     0     1     1     1     1 
--20     1     0     0     1     1 
--21     1     1     1     0     1 
--22     1     1     0     1     0 
--23     0     1     1     0     1 
--24     1     0     0     1     0 
--25     0     1     0     0     1


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


--       4     3     2     1     0
--
--20     1     0     0     0     0
--19     0     1     0     0     0
--18     0     0     1     0     0
--17     0     0     0     1     0
--16     0     0     0     0     1
--15     1     0     1     0     0
--14     0     1     0     1     0
--13     0     0     1     0     1
--12     1     0     1     1     0
--11     0     1     0     1     1
--10     1     0     0     0     1
--09     1     1     1     0     0
--08     0     1     1     1     0
--07     0     0     1     1     1
--06     1     0     1     1     1
--05     1     1     1     1     1
--04     1     1     0     1     1
--03     1     1     0     0     1
--02     1     1     0     0     0
--01     0     1     1     0     0
--00     0     0     1     1     0
--21     0     0     0     1     1
--22     1     0     1     0     1
--23     1     1     1     1     0
--24     0     1     1     1     1
--25     1     0     0     1     1
--26     1     1     1     0     1
--27     1     1     0     1     0
--28     0     1     1     0     1
--29     1     0     0     1     0
--30     0     1     0     0     1