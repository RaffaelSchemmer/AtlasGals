library IEEE;
use IEEE.STD_LOGIC_1164.all;
use IEEE.std_logic_unsigned.all;

package Mercury_package is		
	
	--Constantes referentes ao phit
	constant PHIT_SIZE: integer range 8 to 64 := 16;
	subtype phit is std_logic_vector((PHIT_SIZE - 1) downto 0);
	constant HALF_PHIT_SIZE: integer range 4 to 32 := (PHIT_SIZE/2);
	constant QUARTER_PHIT_SIZE: integer range 2 to 16 := (PHIT_SIZE/4);
	
	--Constantes referentes à fila
	constant QUEUE_POINTER: integer range 2 to 5 := 5;
	constant QUEUE_SIZE: integer range 1 to 32 := 32;	-- deve ser (2 ^ QUEUE_POINTER)	
	subtype pointer is std_logic_vector((QUEUE_POINTER - 1) downto 0);
	
	--Enumeração dos endereços das portas
	--type door is (NORTH, SOUTH, EAST, WEST, LOCAL, PORT_NONE);
	subtype door is std_logic_vector (2 downto 0);
	constant NORTH: std_logic_vector (2 downto 0) := "000";
	constant SOUTH: std_logic_vector (2 downto 0) := "001";
	constant EAST:  std_logic_vector (2 downto 0) := "010";
	constant WEST:  std_logic_vector (2 downto 0) := "011";
	constant LOCAL: std_logic_vector (2 downto 0) := "100";
	constant PORT_NONE: std_logic_vector (2 downto 0) := "101";
	
	--Enumeração das filas
	type queue_addr is (FILA_A, FILA_B, FILA_C, FILA_NONE);
	
	--Enumeração de ACK/NACK  
	--Troquei os nomes de ACK para ACK_ME ... porque na simulação com modelsim dava erro de dupla definição
	subtype ackNack is std_logic_vector (1 downto 0);
	constant ACK_ME: std_logic_vector (1 downto 0) := "00";
	constant NACK_ME: std_logic_vector (1 downto 0) := "01";
	constant NONE_ME:  std_logic_vector (1 downto 0) := "10";
	
	
	--Tamanho máximo que um pacote pode ter, devido ao tamanho da fila
	subtype max_package_size is std_logic_vector(QUEUE_POINTER - 1 downto 0);

	--Tamanho máximo de uma NoC	   
	constant TAM_NOC: std_logic_vector(HALF_PHIT_SIZE - 1 downto 0) := "00110011"; --Constante referente a definição do tamanho NoC
	subtype max_noc_size is std_logic_vector(HALF_PHIT_SIZE - 1 downto 0);		
	subtype half_max_noc_size is std_logic_vector(QUARTER_PHIT_SIZE - 1 downto 0);

	type caminhos_minimos is array(0 to 5) of std_logic;
	
	-- Barramentos
	type ack_nack_bus is array(0 to 5) of ackNack;
	type data_bus is array(0 to 5) of phit;
	type size_bus is array(0 to 5) of phit;
	type queue_addr_bus is array(0 to 5) of queue_addr;
	
	-- Número de roteadores
	constant NROT: integer range 2 to 256 := 9; -- default value is 3x3=9 routers

	-- Barramentos usados para a criação da NoC
	type ack_nack_bus_NROT is array((NROT-1) downto 0) of ack_nack_bus;
	type data_bus_NROT is array((NROT-1) downto 0) of data_bus;
	type size_bus_NROT is array((NROT-1) downto 0) of size_bus;
	type queue_addr_bus_NROT is array((NROT-1) downto 0) of queue_addr_bus;	
	type ack_nack_NROT is array((NROT-1) downto 0) of ackNack;
	type data_NROT is array((NROT-1) downto 0) of phit;
	type size_NROT is array((NROT-1) downto 0) of phit;	
	subtype std_logic_NROT is std_logic_vector((NROT-1) downto 0);

	-- Constantes que armazenam o tamanho das dimensões NoC.
	constant TAM_NOC_X :half_max_noc_size := TAM_NOC(HALF_PHIT_SIZE - 1 downto QUARTER_PHIT_SIZE);
	constant TAM_NOC_Y :half_max_noc_size := TAM_NOC((QUARTER_PHIT_SIZE - 1) downto 0);
	-- Flag constante que define se dimensões são ímpares ou pares	
	constant TAM_NOC_IMPAR_EM_X :std_logic := TAM_NOC(QUARTER_PHIT_SIZE);
	constant TAM_NOC_IMPAR_EM_Y :std_logic := TAM_NOC(0);

	function CONV_VECTOR(int: integer range 0 to 31; vector_size: integer range 2 to 6) return std_logic_vector; 		
	function CONV_INTEGER_TO_DOOR(int: integer) return door;
	function CONV_DOOR_TO_INTEGER(door_in: door) return integer;
	function CONV_QUEUE_TO_VECTOR(queue: queue_addr) return std_logic_vector;
	function CONV_VECTOR_TO_QUEUE(vetor: std_logic_vector(1 downto 0)) return queue_addr;
end Mercury_package;

package body Mercury_package is

function CONV_VECTOR( int: integer range 0 to 31 ; vector_size: integer range 2 to 6) return std_logic_vector is 
begin
	case(vector_size) is
		when 2 =>
			case(int) is 
    			when 0 => return "00"; 
        		when 1 => return "01"; 
        		when 2 => return "10"; 
        		when 3 => return "11"; 
        		when others => return "00"; 
    		end case; 	 
		when 3 =>
			case(int) is 
    			when 0 => return "000"; 
        		when 1 => return "001"; 
        		when 2 => return "010"; 
        		when 3 => return "011"; 
        		when 4 => return "100"; 
        		when 5 => return "101"; 
        		when 6 => return "110"; 
        		when 7 => return "111"; 
        		when others => return "000"; 
    		end case; 		
		when 4 =>		
			case(int) is 
    			when 0 => return "0000"; 
        		when 1 => return "0001"; 
        		when 2 => return "0010"; 
        		when 3 => return "0011"; 
        		when 4 => return "0100"; 
        		when 5 => return "0101"; 
        		when 6 => return "0110"; 
        		when 7 => return "0111"; 
    			when 8 => return "1000"; 
        		when 9 => return "1001"; 
        		when 10 => return "1010"; 
        		when 11 => return "1011"; 
        		when 12 => return "1100"; 
        		when 13 => return "1101"; 
        		when 14 => return "1110"; 
        		when 15 => return "1111"; 				
        		when others => return "0000"; 
    		end case; 			
		when 5 =>
			case(int) is 
    			when 0 => return "00000"; 
        		when 1 => return "00001"; 
        		when 2 => return "00010"; 
        		when 3 => return "00011"; 
        		when 4 => return "00100"; 
        		when 5 => return "00101"; 
        		when 6 => return "00110"; 
        		when 7 => return "00111"; 
    			when 8 => return "01000"; 
        		when 9 => return "01001"; 
        		when 10 => return "01010"; 
        		when 11 => return "01011"; 
        		when 12 => return "01100"; 
        		when 13 => return "01101"; 
        		when 14 => return "01110"; 
        		when 15 => return "01111"; 				
    			when 16 => return "10000"; 
        		when 17 => return "10001"; 
        		when 18 => return "10010"; 
        		when 19 => return "10011"; 
        		when 20 => return "10100"; 
        		when 21 => return "10101"; 
        		when 22 => return "10110"; 
        		when 23 => return "10111"; 
    			when 24 => return "11000"; 
        		when 25 => return "11001"; 
        		when 26 => return "11010"; 
        		when 27 => return "11011"; 
        		when 28 => return "11100"; 
        		when 29 => return "11101"; 
        		when 30 => return "11110"; 
        		when 31 => return "11111";				
        		when others => return "00000"; 
    		end case; 
		when others => return "00000";
	end case;
end CONV_VECTOR; 		  

function CONV_INTEGER_TO_DOOR(int: integer) return door is
begin
	case int is
		when 0 => return NORTH;
		when 1 => return SOUTH;
		when 2 => return EAST;
		when 3 => return WEST;
		when 4 => return LOCAL;
		when 5 => return PORT_NONE;
		when others => return PORT_NONE;
	end case;
end CONV_INTEGER_TO_DOOR;

function CONV_DOOR_TO_INTEGER(door_in: door) return integer is
begin
	case door_in is
		when NORTH 	=> return 0;
		when SOUTH 	=> return 1;
		when EAST  	=> return 2;
		when WEST  	=> return 3;
		when LOCAL 	=> return 4;
		when others => return 5;
	end case;
end CONV_DOOR_TO_INTEGER;

function CONV_QUEUE_TO_VECTOR(queue: queue_addr) return std_logic_vector is
begin
	if queue = FILA_A then
		return "00";
	elsif queue = FILA_B then
		return "01";
	elsif queue = FILA_C then
		return "10";
	elsif queue = FILA_NONE then
		return "11";
	end if;
end CONV_QUEUE_TO_VECTOR;

function CONV_VECTOR_TO_QUEUE(vetor: std_logic_vector(1 downto 0)) return queue_addr is
begin
	if vetor = "00" then
		return FILA_A;
	elsif vetor = "01" then
		return FILA_B;
	elsif vetor = "10" then
		return FILA_C;
	elsif vetor = "11" then
		return FILA_NONE;
	end if;
end CONV_VECTOR_TO_QUEUE;

end Mercury_package;