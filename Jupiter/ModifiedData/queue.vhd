library IEEE;
use IEEE.STD_LOGIC_1164.all;
use IEEE.std_logic_unsigned.all;
use work.Mercury_package.all;

entity Queue is	
	port(	
	clock, 
	reset,
	enqueue: in std_logic;
	ack_in: in ackNack;
	data_av: out std_logic;	  
	data_in: in phit;
	free_size: out max_package_size;	
	size,
	data: out phit);
end Queue;

architecture Queue of Queue is

type buf is array(0 to QUEUE_SIZE - 1) of phit;				   
type fsm_out is (S_IDLE, S_WAIT_ACK, S_SENDING);
signal EA: fsm_out;
signal buf1: buf := (others => (others => '0'));
--signal count: std_logic_vector(QUEUE_POINTER - 1 downto 0);	 --->  comentei porque a sintese xst da warning de sinal nunca utilizado
signal p_write, p_read: pointer;

begin
	
	--free_size informa o espaço livre existente na fila--
	free_size <= ((QUEUE_SIZE - 1) - (p_write - p_read)) when (p_write > p_read) else
				 ((p_read - p_write) - 1) when (p_write < p_read) else
		         CONV_VECTOR(QUEUE_SIZE - 1, QUEUE_POINTER);
	
	--size informa o tamanho do pacote, que está armazenado no segundo phit de cada
	--pacote. No momento que o algoritmo for informado de que há um novo pacote a ser
	--enviado (ou seja, o pacote , esse sinal será lido
	--size <= buf1(CONV_INTEGER(p_read + 1)) when (p_write >= p_read + 2) else (others => '0');				 
		
	size <= buf1(0000) when (p_read = QUEUE_SIZE - 1) else buf1(CONV_INTEGER(p_read + 1));
				 
	process(clock, reset)
	begin		
		if reset = '1' then
			p_write <= (others => '0');			
		elsif clock'event and clock = '1' then
			if enqueue = '1' then
				if p_write = QUEUE_SIZE - 1 then
					p_write <= (others => '0');
				else					
					p_write <= p_write + 1;
				end if;					   
				buf1(CONV_INTEGER(p_write)) <= data_in;
			end if;
		end if;		
	end process;

	
	--PROCESSO DE SAIDA--
	
	--Disponibilizacao do dado para a porta de saida
	data <= buf1(CONV_INTEGER(p_read));	
	
	process(clock, reset)
	begin
		if reset = '1' then
			EA <= S_IDLE;
			data_av <= '0';
			p_read <= (others => '0');
		elsif clock'event and clock = '1' then
			case EA is	   
				when S_IDLE => 										
					if p_write /= p_read then
						EA <= S_WAIT_ACK;
						data_av <= '1';
					else
						EA <= S_IDLE;
					end if;	 
				when S_WAIT_ACK => 
					if ack_in = ACK_ME then
						data_av <= '0';
						p_read <= p_read + 1;
						EA <= S_SENDING;
					else
						EA <= S_WAIT_ACK;
					end if;	
				when S_SENDING =>					
					if ack_in = NONE_ME then
						EA <= S_IDLE;
					else		 
						p_read <= p_read + 1;
						EA <= S_SENDING;
					end if;
			end case;
		end if;
	end process;													
end Queue;
