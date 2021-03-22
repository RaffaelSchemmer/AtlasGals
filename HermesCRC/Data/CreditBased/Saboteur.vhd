library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;
use work.HermesPackage.all;

entity saboteur is
port(
	clock	: in  std_logic;
	reset	: in  std_logic;
	input	: in  regflit;
	output	: out regflit;
	tx	: in  std_logic;
	credit_i: in  std_logic;
	nerror	: out regflit;
	nflits	: out reg32
	);
end saboteur;

architecture saboteur of saboteur is

signal inputd, outputs, bitmask: regflit;
signal error: std_logic;
signal numf,numfreg,double: std_logic;

signal counter,numerr: std_logic_vector(15 downto 0);


begin
	nerror <= numerr;
	nflits <= x"0000" & counter;


	process(clock,reset)
	begin
		if reset='1' then
			inputd <= (others => '0');
		elsif clock'event and clock='1' then
			inputd <= input;
		end if;	
	end process;
	
		
	outputs(0) <= $Dr$'0' when inputd(4 downto 0)="11110" and input(4 downto 0)="00001" else
		      $Df$'1' when inputd(4 downto 0)="00001" and input(4 downto 0)="11110" else
		      $Gn$'0' when inputd(4 downto 0)="11111" and input(4 downto 0)="00001" else
		      $Gp$'1' when inputd(4 downto 0)="00000" and input(4 downto 0)="11110" else
		      input(0);
		      
	outputs(1) <= $Dr$'0' when inputd(4 downto 0)="11101" and input(4 downto 0)="00010" else
		      $Df$'1' when inputd(4 downto 0)="00010" and input(4 downto 0)="11101" else
		      $Gn$'0' when inputd(4 downto 0)="11111" and input(4 downto 0)="00010" else
		      $Gp$'1' when inputd(4 downto 0)="00000" and input(4 downto 0)="11101" else
		      input(1);
		      
	outputs(2) <= $Dr$'0' when inputd(4 downto 0)="11011" and input(4 downto 0)="00100" else
		      $Df$'1' when inputd(4 downto 0)="00100" and input(4 downto 0)="11011" else
		      $Gn$'0' when inputd(4 downto 0)="11111" and input(4 downto 0)="00100" else
		      $Gp$'1' when inputd(4 downto 0)="00000" and input(4 downto 0)="11011" else
		      input(2);
		      
	outputs(3) <= $Dr$'0' when inputd(5 downto 1)="11011" and input(5 downto 1)="00100" else
		      $Df$'1' when inputd(5 downto 1)="00100" and input(5 downto 1)="11011" else
		      $Gn$'0' when inputd(5 downto 1)="11111" and input(5 downto 1)="00100" else
		      $Gp$'1' when inputd(5 downto 1)="00000" and input(5 downto 1)="11011" else
		      input(3);
		      
	outputs(4) <= $Dr$'0' when inputd(6 downto 2)="11011" and input(6 downto 2)="00100" else
		      $Df$'1' when inputd(6 downto 2)="00100" and input(6 downto 2)="11011" else
		      $Gn$'0' when inputd(6 downto 2)="11111" and input(6 downto 2)="00100" else
		      $Gp$'1' when inputd(6 downto 2)="00000" and input(6 downto 2)="11011" else
		      input(4);
		      
	outputs(5) <= $Dr$'0' when inputd(7 downto 3)="11011" and input(7 downto 3)="00100" else
		      $Df$'1' when inputd(7 downto 3)="00100" and input(7 downto 3)="11011" else
		      $Gn$'0' when inputd(7 downto 3)="11111" and input(7 downto 3)="00100" else
		      $Gp$'1' when inputd(7 downto 3)="00000" and input(7 downto 3)="11011" else
		      input(5);
		      
	outputs(6) <= $Dr$'0' when inputd(8 downto 4)="11011" and input(8 downto 4)="00100" else
		      $Df$'1' when inputd(8 downto 4)="00100" and input(8 downto 4)="11011" else
		      $Gn$'0' when inputd(8 downto 4)="11111" and input(8 downto 4)="00100" else
		      $Gp$'1' when inputd(8 downto 4)="00000" and input(8 downto 4)="11011" else
		      input(6);
		      
	outputs(7) <= $Dr$'0' when inputd(9 downto 5)="11011" and input(9 downto 5)="00100" else
		      $Df$'1' when inputd(9 downto 5)="00100" and input(9 downto 5)="11011" else
		      $Gn$'0' when inputd(9 downto 5)="11111" and input(9 downto 5)="00100" else
		      $Gp$'1' when inputd(9 downto 5)="00000" and input(9 downto 5)="11011" else
		      input(7);
		      
	outputs(8) <= $Dr$'0' when inputd(10 downto 6)="11011" and input(10 downto 6)="00100" else
		      $Df$'1' when inputd(10 downto 6)="00100" and input(10 downto 6)="11011" else
		      $Gn$'0' when inputd(10 downto 6)="11111" and input(10 downto 6)="00100" else
		      $Gp$'1' when inputd(10 downto 6)="00000" and input(10 downto 6)="11011" else
		      input(8);
		      
	outputs(9) <= $Dr$'0' when inputd(11 downto 7)="11011" and input(11 downto 7)="00100" else
		      $Df$'1' when inputd(11 downto 7)="00100" and input(11 downto 7)="11011" else
		      $Gn$'0' when inputd(11 downto 7)="11111" and input(11 downto 7)="00100" else
		      $Gp$'1' when inputd(11 downto 7)="00000" and input(11 downto 7)="11011" else
		      input(9);
		      
	outputs(10) <= $Dr$'0' when inputd(12 downto 8)="11011" and input(12 downto 8)="00100" else
		       $Df$'1' when inputd(12 downto 8)="00100" and input(12 downto 8)="11011" else
		       $Gn$'0' when inputd(12 downto 8)="11111" and input(12 downto 8)="00100" else
		       $Gp$'1' when inputd(12 downto 8)="00000" and input(12 downto 8)="11011" else
		       input(10);
		       
	outputs(11) <= $Dr$'0' when inputd(13 downto 9)="11011" and input(13 downto 9)="00100" else
		       $Df$'1' when inputd(13 downto 9)="00100" and input(13 downto 9)="11011" else
		       $Gn$'0' when inputd(13 downto 9)="11111" and input(13 downto 9)="00100" else
		       $Gp$'1' when inputd(13 downto 9)="00000" and input(13 downto 9)="11011" else
		       input(11);
		       
	outputs(12) <= $Dr$'0' when inputd(14 downto 10)="11011" and input(14 downto 10)="00100" else
		       $Df$'1' when inputd(14 downto 10)="00100" and input(14 downto 10)="11011" else
		       $Gn$'0' when inputd(14 downto 10)="11111" and input(14 downto 10)="00100" else
		       $Gp$'1' when inputd(14 downto 10)="00000" and input(14 downto 10)="11011" else
		       input(12);
		       
	outputs(13) <= $Dr$'0' when inputd(15 downto 11)="11011" and input(15 downto 11)="00100" else
		       $Df$'1' when inputd(15 downto 11)="00100" and input(15 downto 11)="11011" else
		       $Gn$'0' when inputd(15 downto 11)="11111" and input(15 downto 11)="00100" else
		       $Gp$'1' when inputd(15 downto 11)="00000" and input(15 downto 11)="11011" else
		       input(13);
		       
	outputs(14) <= $Dr$'0' when inputd(15 downto 11)="10111" and input(15 downto 11)="01000" else
		       $Df$'1' when inputd(15 downto 11)="01000" and input(15 downto 11)="10111" else
		       $Gn$'0' when inputd(15 downto 11)="11111" and input(15 downto 11)="01000" else
		       $Gp$'1' when inputd(15 downto 11)="00000" and input(15 downto 11)="10111" else
		       input(14);
		       
	outputs(15) <= $Dr$'0' when inputd(15 downto 11)="01111" and input(15 downto 11)="10000" else
		       $Df$'1' when inputd(15 downto 11)="10000" and input(15 downto 11)="01111" else
		       $Gn$'0' when inputd(15 downto 11)="11111" and input(15 downto 11)="10000" else
		       $Gp$'1' when inputd(15 downto 11)="00000" and input(15 downto 11)="01111" else
		       input(15);
	
 	output <= outputs;
 	
 	bitmask <= input xor outputs;
 	
 	process(clock,reset)
 	begin
 		if reset='1' then
 			error <= '0';
 			numfreg <= '0';
 			counter <= (others => '0');
 			numerr <= (others => '0');
 		elsif clock'event and clock='1' then
 			if tx='1' and credit_i='1' then
 				counter <= counter + '1';
 			end if; 		
 		
 			if input/=outputs then
 				numfreg <= numf;
 				error <= '1';
 				numerr <= numerr + '1';
 			else
 				error <= '0';
 			end if;
 		end if;
 	end process;
 	
 	assert double='1' report "falha dupla!" severity warning;
 	numf <= bitmask(0) xor bitmask(1) xor bitmask(2) xor bitmask(3) xor bitmask(4) xor bitmask(5) xor bitmask(6) xor bitmask(7) xor bitmask(8) xor bitmask(9) xor bitmask(10) xor bitmask(11) xor bitmask(12) xor bitmask(13) xor bitmask(14) xor bitmask(15);
	double <= error and not numfreg;
		
end saboteur;