---------------------------------------------------------------------------------------
--                                                ROUTER
--
--                                                 NORTH                   LOCAL
--              ------------------------------------------------------------------
--              |                            ****** ******     ******  ****** |
--              |                          *FILA* *FILA*      *FILA* *FILA* |
--              |                          *   L1  * *  L2  *      *   L1  * *   L2  * |
--              |                           ******  ******      ******  ****** |
--              |                                                                                      |
--              | *********      ***************           *********  |
--              | *FILA L1*      * ARBITRAGEM  *          * FILA L1* |
--              | *********      ***************           *********  |
--  WEST | *********      ****************         ********* |
--              | *FILA L2*      * ROTEAMENTO  *        *FILA L2* |
--              | *********      ****************          ********* |
--              |                                                                                      |
--              |                            ****** ******                                 |
--              |                            *FILA* *FILA*                                |
--              |                            *   L1  * *  L2  *                                |
--              |                             ****** ******                                |
--              ------------------------------------------------------------------
--                                    SOUTH
--
--  Os roteadores realizam a transferência de mensagens entre núcleos.
--  O roteador possui uma lógica de controle de chaveamento e 5 portas bidirecionais:
--  East, West, North, South e Local. Cada porta possui dois canais virtuais, cada
--  um com uma fila para o armazenamento temporário de flits. A porta Local estabelece
--  a comunicação entre o roteador e seu núcleo. As demais portas ligam o roteador aos
--  roteadores vizinhos.
--  Os endereços dos roteadores são compostos pelas coordenadas XY da rede de
--  interconexão, onde X é a posição horizontal e Y a posição vertical. A atribuição de
--  endereços aos roteadores é necessária para a execução do algoritmo de chaveamento.
--  Os módulos que compõem o roteador são: Hermes_buffer (porta de entrada e filas),
--  Hermes_switchcontrol (arbitragem e roteamento) e Hermes_outport (porta de saída).
--  Cada uma das filas do roteador, ao receber um novo pacote requisita chaveamento ao
--  árbitro. O árbitro seleciona a requisição de maior prioridade, quando existem
--  requisições simultâneas, e encaminha o pedido à lógica de chaveamento. A lógica de
--  chaveamento verifica se é possível atender à solicitação. Sendo possível, a conexão
--  é estabelecida e a fila começa a enviar os flits armazenados. Quando todos os flits
--  do pacote são enviados, a fila sinaliza através do sinal sender que a conexão deve
--  ser finalizada.
---------------------------------------------------------------------------------------
library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;
use IEEE.std_logic_arith.all;
use work.HermesPackage.all;

entity $Chave$ is
generic( address: regmetadeflit);
port(
	clock:      in  std_logic;
	reset:      in  std_logic;
	clock_rx:   in  regNport;
	rx:         in  regNport;
	lane_rx:    in  arrayNport_regNlane;
	data_in:    in  arrayNport_regflit;
	credit_o:   out arrayNport_regNlane;
	clock_tx:   out regNport;
	tx:         out regNport;
	lane_tx:    out arrayNport_regNlane;
	data_out:   out arrayNport_regflit;
	credit_i:   in  arrayNport_regNlane);
end $Chave$;

architecture $Chave$ of $Chave$ is

signal h, ack_h, data_av, data_ack, sender, free: arrayNport_regNlane := (others=>(others=>'0'));
signal aux_lane_tx, last_lane_tx: arrayNport_regNlane := (others=>(others=>'0'));
signal data: matrixNport_Nlane_regflit := (others=>(others=>(others=>'0')));
signal tableIn, tableOut: matrixNport_Nlane_reg8 := (others=>(others=>(others=>'0')));

begin
	lane_tx <= aux_lane_tx;
$inports$
	SC : Entity work.Hermes_switchcontrol
	port map(
		clock => clock,
		reset => reset,
		h => h,
		ack_h => ack_h,
		address => address,
		data => data,
		sender => sender,
		free => free,
		mux_in => tableIn,
		mux_out => tableOut);
$outports$
end $Chave$;
