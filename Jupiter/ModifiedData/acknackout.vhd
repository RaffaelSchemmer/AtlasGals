library IEEE;
use IEEE.std_logic_1164.all;
use work.Mercury_package.all;

entity AckNackOut is
	port(
	queue_addr_N,
	queue_addr_S,
	queue_addr_E,
	queue_addr_W: in queue_addr;
	ctrl_A,
	ctrl_B,
	ctrl_C: in door;
	ack_nack_A,
	ack_nack_B,
	ack_nack_C: in ackNack;
	crtl_ack_nack_A,
	crtl_ack_nack_B,
	crtl_ack_nack_C: in std_logic;
	ack_nack_N,
	ack_nack_S,
	ack_nack_E,
	ack_nack_W,
	ack_nack_L_A,
	ack_nack_L_B,
	ack_nack_L_C: out ackNack);
end AckNackOut;

architecture AckNackOut of AckNackOut is
   signal ack_nack_N_int,
          ack_nack_S_int,
          ack_nack_E_int,
          ack_nack_W_int : ackNack;
begin


--   ack_nack_N <= ack_nack_A when (ctrl_A = NORTH) and (queue_addr_N = FILA_A) else
   ack_nack_N_int<= ack_nack_A when (ctrl_A = NORTH) and (queue_addr_N = FILA_A) else
   				        ack_nack_B when (ctrl_B = NORTH) and (queue_addr_N = FILA_B) else
				        ack_nack_C when (ctrl_C = NORTH) and (queue_addr_N = FILA_C) else
				        NONE_ME;

--	ack_nack_S <= ack_nack_A when (ctrl_A = SOUTH) and (queue_addr_S = FILA_A) else
  ack_nack_S_int<= ack_nack_A when (ctrl_A = SOUTH) and (queue_addr_S = FILA_A) else
				        ack_nack_B when (ctrl_B = SOUTH) and (queue_addr_S = FILA_B) else
				        ack_nack_C when (ctrl_C = SOUTH) and (queue_addr_S = FILA_C) else
				        NONE_ME;

--	ack_nack_E <= ack_nack_A when (ctrl_A = EAST) and (queue_addr_E = FILA_A) else
   ack_nack_E_int<= ack_nack_A when (ctrl_A = EAST) and (queue_addr_E = FILA_A) else
				        ack_nack_B when (ctrl_B = EAST) and (queue_addr_E = FILA_B) else
				        ack_nack_C when (ctrl_C = EAST) and (queue_addr_E = FILA_C) else
				        NONE_ME;

--   ack_nack_W <= ack_nack_A when (ctrl_A = WEST) and (queue_addr_W = FILA_A) else
   ack_nack_W_int<= ack_nack_A when (ctrl_A = WEST) and (queue_addr_W = FILA_A) else
				        ack_nack_B when (ctrl_B = WEST) and (queue_addr_W = FILA_B) else
				        ack_nack_C when (ctrl_C = WEST) and (queue_addr_W = FILA_C) else
				        NONE_ME;



  ack_nack_N <= NACK_ME when (((ctrl_A /= NORTH) and (crtl_ack_nack_A='1') and (queue_addr_N = FILA_A)) or
                              ((ctrl_B /= NORTH) and (crtl_ack_nack_B='1') and (queue_addr_N = FILA_B)) or
                              ((ctrl_C /= NORTH) and (crtl_ack_nack_C='1') and (queue_addr_N = FILA_C)))
                        else ack_nack_N_int;

  ack_nack_S <= NACK_ME when (((ctrl_A /= SOUTH) and (crtl_ack_nack_A='1') and (queue_addr_S = FILA_A)) or
                              ((ctrl_B /= SOUTH) and (crtl_ack_nack_B='1') and (queue_addr_S = FILA_B)) or
                              ((ctrl_C /= SOUTH) and (crtl_ack_nack_C='1') and (queue_addr_S = FILA_C)))
                        else ack_nack_S_int;

  ack_nack_E <= NACK_ME when (((ctrl_A /= EAST) and (crtl_ack_nack_A='1') and (queue_addr_E = FILA_A)) or
                              ((ctrl_B /= EAST) and (crtl_ack_nack_B='1') and (queue_addr_E = FILA_B)) or
                              ((ctrl_C /= EAST) and (crtl_ack_nack_C='1') and (queue_addr_E = FILA_C)))
                        else ack_nack_E_int;

  ack_nack_W <= NACK_ME when (((ctrl_A /= WEST) and (crtl_ack_nack_A='1') and (queue_addr_W = FILA_A)) or
                              ((ctrl_B /= WEST) and (crtl_ack_nack_B='1') and (queue_addr_W = FILA_B)) or
                              ((ctrl_C /= WEST) and (crtl_ack_nack_C='1') and (queue_addr_W = FILA_C)))
                        else ack_nack_W_int;



	ack_nack_L_A <= ack_nack_A when (ctrl_A = LOCAL) else
					NONE_ME;
	ack_nack_L_B <= ack_nack_B when (ctrl_B = LOCAL) else
					NONE_ME;
	ack_nack_L_C <= ack_nack_C when (ctrl_C = LOCAL) else
				    NONE_ME;
end AckNackOut;
