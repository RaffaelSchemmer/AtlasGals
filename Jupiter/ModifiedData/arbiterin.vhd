library IEEE;
use IEEE.STD_LOGIC_1164.all;
use IEEE.std_logic_unsigned.all;
use work.Mercury_package.all;

entity ArbiterIn is
port(
	clock,
	reset, 				   
	data_av_N,
	data_av_S,
	data_av_E,
	data_av_W,
	data_av_L: in std_logic;
	enqueue,
	crtl_ack_nack: out std_logic;	
	free_size: in max_package_size; 
	ctrl: out door;
	ack_nack: out ackNack;
	size: in phit);
end ArbiterIn;

architecture ArbiterIn of ArbiterIn is

type states is (S_IDLE, S_WAIT_COUNT, S_ENQUEUE, S_NACK, S_WAIT_ACK);
signal EA: states;				
signal count: integer;	   
signal ctrl_sig, ctrl_sig_int, ultimo_atendido: door;
signal tem_espaco, reqQ, crtl_ack_nack_int: std_logic;
type door_av is array(0 to 4) of std_logic;
signal door_av1: door_av;

begin
	tem_espaco <= '1' when (free_size >= size) else
				  '0';
	
	crtl_ack_nack <= crtl_ack_nack_int;
	
	reqQ <= '1' when data_av_N = '1' or data_av_S = '1' or data_av_E = '1' or data_av_W = '1' or data_av_L = '1' else
		    '0';
	
	door_av1(0) <= data_av_N;
	door_av1(1) <= data_av_S;
	door_av1(2) <= data_av_E;
	door_av1(3) <= data_av_W;
	door_av1(4) <= data_av_L;

	
	-- Processo que controla a disputa por prioridade
		ctrl_sig_int <=	
				NORTH 
					when (door_av1(0)='1'  and EA = S_IDLE and (
					(ultimo_atendido=PORT_NONE) or 
					(ultimo_atendido=LOCAL) or 
					(ultimo_atendido=WEST  and door_av1(4)='0') or
					(ultimo_atendido=EAST  and door_av1(3 to 4)="00") or
					(ultimo_atendido=SOUTH and door_av1(2 to 4)="000") or
					(ultimo_atendido=NORTH and door_av1(1 to 4)="0000"))) else
				SOUTH
					when (door_av1(1)='1'  and EA = S_IDLE and (
					(ultimo_atendido=NORTH) or 
					(ultimo_atendido=LOCAL and door_av1(0)='0') or
					(ultimo_atendido=WEST  and door_av1(0)='0' and door_av1(4)='0') or
					(ultimo_atendido=EAST  and door_av1(3 to 4)="00" and door_av1(0)='0') or
					(ultimo_atendido=SOUTH  and door_av1(2 to 4)="000" and door_av1(0)='0'))) else
				EAST
					when (door_av1(2)='1'  and EA = S_IDLE and (
					(ultimo_atendido=SOUTH) or
					(ultimo_atendido=NORTH and door_av1(1)='0') or
					(ultimo_atendido=LOCAL and door_av1(0 to 1)="00") or
					(ultimo_atendido=WEST  and door_av1(0 to 1)="00" and door_av1(4)='0') or
					(ultimo_atendido=EAST  and door_av1(0 to 1)="00" and door_av1(3 to 4)="00"))) else
				WEST
					when (door_av1(3)='1'  and EA = S_IDLE and (
					(ultimo_atendido=EAST) or 
					(ultimo_atendido=SOUTH and door_av1(2)='0') or
					(ultimo_atendido=NORTH and door_av1(1 to 2)="00") or
					(ultimo_atendido=LOCAL and door_av1(0 to 2)="000") or
					(ultimo_atendido=WEST and door_av1(0 to 2)="000" and door_av1(4)='0'))) else
				LOCAL
					when (door_av1(4)='1'  and EA = S_IDLE and (
					(ultimo_atendido=WEST) or 
					(ultimo_atendido=EAST  and door_av1(3)='0') or		
					(ultimo_atendido=SOUTH and door_av1(2 to 3)="00") or
					(ultimo_atendido=NORTH and door_av1(1 to 3)="000") or
					(ultimo_atendido=LOCAL and door_av1(0 to 3)="0000")))else
			ctrl_sig; 

	ctrl <= ctrl_sig_int;	 

	process (clock, reset)
	begin
		if reset='1' then ctrl_sig <= PORT_NONE;
		elsif clock'event and clock='1' then
			ctrl_sig <= ctrl_sig_int;
		end if;
	end process;
	
	-- Máquina de estados do árbitro
	process(clock, reset)-- ver
	begin
		if reset = '1' then
			EA <= S_IDLE;
 		   crtl_ack_nack_int <= '0';
			ack_nack <= NONE_ME;
			enqueue <= '0';	 
			ultimo_atendido <= LOCAL;
			count <= 0;
		elsif clock'event and clock = '1' then
			case EA is
				when S_IDLE =>
				   crtl_ack_nack_int <= '0';
					if reqQ = '1' and tem_espaco = '1' then
						EA <= S_WAIT_COUNT;
						ack_nack <= ACK_ME;
						enqueue <= '1';
					elsif reqQ = '1' and tem_espaco = '0' then
						EA <= S_NACK;
						ack_nack <= NACK_ME;
					else
						EA <= S_IDLE;
					end if;		 
				when S_WAIT_COUNT =>				   
					EA <= S_ENQUEUE;
					crtl_ack_nack_int <= '1';
					count <= CONV_INTEGER(size) - 1;  	
				when S_ENQUEUE =>
				   crtl_ack_nack_int <= not crtl_ack_nack_int;
					count <= count - 1;					
					if count > 1 then
						EA <= S_ENQUEUE;
					else
						EA <= S_WAIT_ACK;	
						ack_nack <= NONE_ME;
						enqueue <= '0';
						ultimo_atendido <= ctrl_sig;
					end if;		   
				when S_WAIT_ACK =>
					EA <= S_IDLE;			
				when S_NACK =>
					ack_nack <= NONE_ME;
					crtl_ack_nack_int <= '1';
					ultimo_atendido <= ctrl_sig;
					EA <= S_IDLE;
			end case;
		end if;
	end process;
end ArbiterIn;
