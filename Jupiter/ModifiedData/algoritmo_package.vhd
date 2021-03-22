library IEEE;
use IEEE.STD_LOGIC_1164.all;
use IEEE.std_logic_unsigned.all;
use work.Mercury_package.all;

package Algoritmo_package is	
	type queue_addr_door is array(0 to 5) of queue_addr;
	type queue_addr_int is array(0 to 3) of std_logic;
	type ack_nack_door_int_t is array(0 to 5) of ackNack;
	type ack_nack_local_t is (LOCAL_A, LOCAL_B, LOCAL_C);
	type ack_nack_local_int_t is array(ack_nack_local_t) of ackNack;
	
	function RIGHT(pos_x: half_max_noc_size; pos_y: half_max_noc_size) return integer;
	function LEFT(pos_x: half_max_noc_size; pos_y: half_max_noc_size) return integer;
	function EXISTE_CAMINHO_A(caminhos_minimos_A: caminhos_minimos; pos_atual_x: half_max_noc_size; pos_atual_y: half_max_noc_size) return std_logic;
	function EXISTE_CAMINHO_B(caminhos_minimos_B: caminhos_minimos; pos_atual_x: half_max_noc_size; pos_atual_y: half_max_noc_size) return std_logic;
	function CHEGOU_DESTINO(pos_atual_x, pos_atual_y, destino_x, destino_y: half_max_noc_size) return std_logic;
	
	component regnbit is
		port(
		ce,
		reset: in std_logic;
		reg_in: in caminhos_minimos;
		reg_out: out caminhos_minimos);
	end component;
end Algoritmo_package;

package body Algoritmo_package is
	function RIGHT(pos_x: half_max_noc_size; pos_y: half_max_noc_size) return integer is
	begin							  
		return CONV_INTEGER(pos_x + (TAM_NOC_X * pos_y));		
	end RIGHT;
	
	function LEFT(pos_x: half_max_noc_size; pos_y: half_max_noc_size) return integer is
	begin
		return CONV_INTEGER((TAM_NOC_X * (TAM_NOC_Y - pos_y)) - (pos_x + 1));
	end LEFT;	  	
	
-- A função EXISTE_CAMINHO_A verifica se o RIGHT(origem) é menor do que pelo menos
-- um dos caminhos mínimos designados.
function EXISTE_CAMINHO_A(caminhos_minimos_A: caminhos_minimos; pos_atual_x: half_max_noc_size; pos_atual_y: half_max_noc_size) return std_logic is
	begin							
		if caminhos_minimos_A(conv_integer(EAST)) = '1' then
			if pos_atual_x = (TAM_NOC_X - 1) then
				if RIGHT(pos_atual_x, pos_atual_y) < RIGHT((others=>'0'), pos_atual_y) then
					return '0';
				end if;			
			elsif RIGHT(pos_atual_x, pos_atual_y) < RIGHT(pos_atual_x + 1, pos_atual_y) then
				return '0';			
			end if;
		end if;
		
		if caminhos_minimos_A(conv_integer(WEST)) = '1' then
			if pos_atual_x = 0 then
				if RIGHT(pos_atual_x, pos_atual_y) < RIGHT(TAM_NOC_X - 1, pos_atual_y) then				
					return '0';
				end if;
			elsif RIGHT(pos_atual_x, pos_atual_y) < RIGHT(pos_atual_x - 1, pos_atual_y) then
					return '0';
			end if;	
		end if;
		
		if caminhos_minimos_A(conv_integer(NORTH)) = '1' then
			if pos_atual_y = (TAM_NOC_Y - 1) then
				if RIGHT(pos_atual_x, pos_atual_y) < RIGHT(pos_atual_x, (others=>'0')) then					
					return '0';
				end if;
			elsif RIGHT(pos_atual_x, pos_atual_y) < RIGHT(pos_atual_x, pos_atual_y + 1) then
				return '0';			
			end if;								
		end if;
	
		if caminhos_minimos_A(conv_integer(SOUTH)) = '1' then
			if pos_atual_y = 0 then
				if RIGHT(pos_atual_x, pos_atual_y) < RIGHT(pos_atual_x, TAM_NOC_Y - 1) then				
					return '0';
				end if;
			elsif RIGHT(pos_atual_x, pos_atual_y) < RIGHT(pos_atual_x, pos_atual_y - 1) then
					return '0';
			end if;	
		end if;	
		
		return '1';
end EXISTE_CAMINHO_A;

-- A função EXISTE_CAMINHO_B verifica se o LEFT(origem) é menor do que pelo menos
-- um dos caminhos mínimos designados.
function EXISTE_CAMINHO_B(caminhos_minimos_B: caminhos_minimos; pos_atual_x: half_max_noc_size; pos_atual_y: half_max_noc_size) return std_logic is
	begin							
		if caminhos_minimos_B(conv_integer(EAST)) = '1' then
			if pos_atual_x = (TAM_NOC_X - 1) then
				if LEFT(pos_atual_x, pos_atual_y) < LEFT((others=>'0'), pos_atual_y) then
					return '0';
				end if;			
			elsif LEFT(pos_atual_x, pos_atual_y) < LEFT(pos_atual_x + 1, pos_atual_y) then
				return '0';			
			end if;
		end if;
		
		if caminhos_minimos_B(conv_integer(WEST)) = '1' then
			if pos_atual_x = 0 then
				if LEFT(pos_atual_x, pos_atual_y) < LEFT(TAM_NOC_X - 1, pos_atual_y) then				
					return '0';
				end if;
			elsif LEFT(pos_atual_x, pos_atual_y) < LEFT(pos_atual_x - 1, pos_atual_y) then
					return '0';
			end if;	
		end if;
		
		if caminhos_minimos_B(conv_integer(NORTH)) = '1' then
			if pos_atual_y = (TAM_NOC_Y - 1) then
				if LEFT(pos_atual_x, pos_atual_y) < LEFT(pos_atual_x, (others=>'0')) then					
					return '0';
				end if;
			elsif LEFT(pos_atual_x, pos_atual_y) < LEFT(pos_atual_x, pos_atual_y + 1) then
				return '0';
			end if;								
		end if;
	
		if caminhos_minimos_B(conv_integer(SOUTH)) = '1' then
			if pos_atual_y = 0 then
				if LEFT(pos_atual_x, pos_atual_y) < LEFT(pos_atual_x, TAM_NOC_Y - 1) then				
					return '0';
				end if;
			elsif LEFT(pos_atual_x, pos_atual_y) < LEFT(pos_atual_x, pos_atual_y - 1) then
					return '0';
			end if;	
		end if;	
		
		return '1';
end EXISTE_CAMINHO_B;

function CHEGOU_DESTINO(pos_atual_x, pos_atual_y, destino_x, destino_y: half_max_noc_size) return std_logic is
begin
	if (pos_atual_x = destino_x) and (pos_atual_y = destino_y) then
		return '1';
	else 
		return '0';
	end if;
end	CHEGOU_DESTINO;

end Algoritmo_package;