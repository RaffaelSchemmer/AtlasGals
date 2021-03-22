---------------------------------------------------------------------------------------
--                      UNIDIRECTIONAL TORUS ROUTER
--
--                             NORTH           LOCAL
--       -------------------------------------------------
--       |                                 ****** ****** |
--       |                                 *FILA* *FILA* |
--       |                                 * L1 * * L2 * |
--       |                                 ****** ****** |
--       |                                               |
--       | *********      ***************                |
--       | *FILA L1*      * ARBITRAGEM  *                |
--       | *********      ***************                | EAST
--  WEST | *********      ***************                |
--       | *FILA L2*      * ROTEAMENTO  *                |
--       | *********      ***************                |
--       |                                               |
--       |                 ****** ******                 |
--       |                 *FILA* *FILA*                 |
--       |                 * L1 * * L2 *                 |
--       |                 ****** ******                 |
--       -------------------------------------------------
--                             SOUTH
--
--  Os roteadores realizam a transferência de mensagens entre núcleos.
--  O roteador possui uma lógica de arbitragen e roteamento 3 portas de entrada
--  West, South e Local. Existem ainda 5 portas de saída, East, West, North South e Local.
--  Contudo, as portas de saída West e South jamais são usadas para transmitir dados.
--  Estas existem apenas para produzir informação de créditos para os roteadores vizinhos
--  ao Sul e a Oeste . Cada porta de entrada possui dois canais virtuais, cada
--  um com uma fila para o armazenamento temporário de flits. A porta Local estabelece
--  a comunicação entre o roteador e seu núcleo. As demais portas ligam o roteador a
--  roteadores vizinhos.
--  Os endereços dos roteadores são compostos pelas coordenadas XY da rede de
--  interconexão, onde X é a posição horizontal e Y a posição vertical. A atribuição de
--  endereços aos roteadores é necessária para a execução do algoritmo de roteamento.
--  Os módulos que compõem o roteador são: HermesTU_buffer (porta de entrada e filas),
--  HermesTU_switchcontrol (arbitragem e roteamento) e HermesTU_outport (porta de saída).
--  Cada uma das filas do roteador, ao receber um novo pacote requisita chaveamento ao
--  árbitro/roteador. O árbitro/roteador seleciona a requisição de maior prioridade,
--  quando existem requisições simultâneas, e encaminha o pedido à lógica de roteamento.
--  Esta verifica se é possível atender à solicitação. Sendo possível, a conexão
--  é estabelecida e a fila começa a enviar os flits armazenados. Quando todos os flits
--  do pacote são enviados, a fila sinaliza através do sinal sender que a conexão deve
--  ser finalizada.
---------------------------------------------------------------------------------------
library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;
use IEEE.std_logic_arith.all;
use work.HermesTUPackage.all;

entity RouterCC is
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
end RouterCC;

architecture RouterCC of RouterCC is

signal h, ack_h, data_av, data_ack, sender, free: arrayNport_regNlane := (others=>(others=>'0'));
signal aux_lane_tx, last_lane_tx: arrayNport_regNlane := (others=>(others=>'0'));
signal data: matrixNport_Nlane_regflit := (others=>(others=>(others=>'0')));
signal tableIn, tableOut: matrixNport_Nlane_reg8 := (others=>(others=>(others=>'0')));

begin
    lane_tx <= aux_lane_tx;

    h(0) <= (others=>'0');
    data_av(0) <= (others=>'0');
    data(0) <= (others=>(others=>'0'));
    sender(0) <= (others=>'0');
    credit_o(0) <= (others=>'0');

    IP_WEST : Entity work.HermesTU_inport(HermesTU_inport)
    port map(
        clock => clock,
        reset => reset,
        clock_rx => clock_rx(1),
        rx => rx(1),
        lane_rx => lane_rx(1),
        data_in => data_in(1),
        credit_o => credit_o(1),
        h => h(1),
        ack_h => ack_h(1),
        data_av => data_av(1),
        data => data(1),
        data_ack => data_ack(1),
        sender=>sender(1));

    h(2) <= (others=>'0');
    data_av(2) <= (others=>'0');
    data(2) <= (others=>(others=>'0'));
    sender(2) <= (others=>'0');
    credit_o(2) <= (others=>'0');

    IP_SOUTH : Entity work.HermesTU_inport
    port map(
        clock => clock,
        reset => reset,
        clock_rx => clock_rx(3),
        rx => rx(3),
        lane_rx => lane_rx(3),
        data_in => data_in(3),
        credit_o => credit_o(3),
        h => h(3),
        ack_h => ack_h(3),
        data_av => data_av(3),
        data => data(3),
        data_ack => data_ack(3),
        sender=>sender(3));

    IP_LOCAL : Entity work.HermesTU_inport
    port map(
        clock => clock,
        reset => reset,
        clock_rx => clock_rx(4),
        rx => rx(4),
        lane_rx => lane_rx(4),
        data_in => data_in(4),
        credit_o => credit_o(4),
        h => h(4),
        ack_h => ack_h(4),
        data_av => data_av(4),
        data => data(4),
        data_ack => data_ack(4),
        sender=>sender(4));

    SC : Entity work.HermesTU_switchcontrol
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

    OP_EAST : Entity work.HermesTU_outport(HermesTU_outport_east)
    port map(
        clock => clock,
        reset => reset,
        data_av => data_av,
        data => data,
        data_ack => data_ack(0),
        free => free(0),
        all_lane_tx => aux_lane_tx,
        tableIn => tableIn(0),
        tableOut => tableOut(0),
        clock_tx => clock_tx(0),
        tx => tx(0),
        lane_tx => aux_lane_tx(0),
        data_out => data_out(0),
        credit_i => credit_i(0));

    OP_WEST : Entity work.HermesTU_outport_minimal(HermesTU_outport_west)
    port map(
        data_av => data_av,
        data_ack => data_ack(1),
        all_lane_tx => aux_lane_tx,
        tableIn => tableIn(1),
        credit_i => credit_i(1));

    OP_NORTH : Entity work.HermesTU_outport(HermesTU_outport_north)
    port map(
        clock => clock,
        reset => reset,
        data_av => data_av,
        data => data,
        data_ack => data_ack(2),
        free => free(2),
        all_lane_tx => aux_lane_tx,
        tableIn => tableIn(2),
        tableOut => tableOut(2),
        clock_tx => clock_tx(2),
        tx => tx(2),
        lane_tx => aux_lane_tx(2),
        data_out => data_out(2),
        credit_i => credit_i(2));

    OP_SOUTH : Entity work.HermesTU_outport_minimal(HermesTU_outport_south)
    port map(
        data_av => data_av,
        data_ack => data_ack(3),
        all_lane_tx => aux_lane_tx,
        tableIn => tableIn(3),
        credit_i => credit_i(3));

    OP_LOCAL : Entity work.HermesTU_outport(HermesTU_outport_local)
    port map(
        clock => clock,
        reset => reset,
        data_av => data_av,
        data => data,
        data_ack => data_ack(4),
        free => free(4),
        all_lane_tx => aux_lane_tx,
        tableIn => tableIn(4),
        tableOut => tableOut(4),
        clock_tx => clock_tx(4),
        tx => tx(4),
        lane_tx => aux_lane_tx(4),
        data_out => data_out(4),
        credit_i => credit_i(4));

end RouterCC;
