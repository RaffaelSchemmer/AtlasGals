---------------------------------------------------------------------------------------	
--                                              ROUTER
--
--                                              NORTH        LOCAL
--                       ---------------------------------------------
--                      |                        ******           ****** |
--                      |                       *FILA*          *FILA* |
--                      |                        ******          ****** |
--                      |                 *************                |
--                      |                 *  ARBITRO  *                  |
--                      | ******    *************    ****** |
--          WEST | *FILA*   *************   *FILA* | EAST
--                      | ******    *  CONTROLE *  ****** |
--                      |                 *************                 |
--                      |                       ******                         |
--                      |                       *FILA*                         |
--                      |                       ******                         |
--                      -----------------------------------------------
--                                              SOUTH
--
--  As chaves realizam a transferência de mensagens entre núcleos. 
--  A chave possui uma lógica de controle de chaveamento e 5 portas bidirecionais:
--  East, West, North, South e Local. Cada porta possui uma fila para o armazenamento 
--  temporário de flits. A porta Local estabelece a comunicação entre a chave e seu 
--  núcleo. As demais portas ligam a chave às chaves vizinhas.
--  Os endereços das chaves são compostos pelas coordenadas XY da rede de interconexão, 
--  onde X é a posição horizontal e Y a posição vertical. A atribuição de endereços às 
--  chaves é necessária para a execução do algoritmo de chaveamento.
--  Os módulos principais que compõem a chave são: fila, árbitro e lógica de 
--  chaveamento implementada pelo controle_mux. Cada uma das filas da chave (E, W, N, 
--  S e L), ao receber um novo pacote requisita chaveamento ao árbitro. O árbitro 
--  seleciona a requisição de maior prioridade, quando existem requisições simultâneas, 
--  e encaminha o pedido de chaveamento à lógica de chaveamento. A lógica de 
--  chaveamento verifica se é possível atender à solicitação. Sendo possível, a conexão
--  é estabelecida e o árbitro é informado. Por sua vez, o árbitro informa a fila que 
--  começa a enviar os flits armazenados. Quando todos os flits do pacote foram 
--  enviados, a conexão é concluída pela sinalização, por parte da fila, através do 
--  sinal sender.
---------------------------------------------------------------------------------------
library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;
use work.HermesPackage.all;

entity $Chave$ is
generic( address: regmetadeflit);
port(
	clock:     in  std_logic;
	reset:     in  std_logic;
	clock_rx:  in  regNport;
	rx:        in  regNport;
	data_in:   in  arrayNport_regflit;
	credit_o:  out regNport;    
	clock_tx:  out regNport;
	tx:        out regNport;
	data_out:  out arrayNport_regflit;
	credit_i:  in  regNport);
end $Chave$;

architecture $Chave$ of $Chave$ is

signal h, ack_h, data_av, sender, data_ack: regNport := (others=>'0');
signal data: arrayNport_regflit := (others=>(others=>'0'));
signal mux_in, mux_out: arrayNport_reg3 := (others=>(others=>'0'));
signal free: regNport := (others=>'0');

begin
$filas$
	SwitchControl : Entity work.SwitchControl($algorithm$)
	port map(
		clock => clock,
		reset => reset,
		h => h,
		ack_h => ack_h,
		address => address,
		data => data,
		sender => sender,
		free => free,
		mux_in => mux_in,
		mux_out => mux_out);

	CrossBar : Entity work.Hermes_crossbar
	port map(
		data_av => data_av,
		data_in => data,
		data_ack => data_ack,
		sender => sender,
		free => free,
		tab_in => mux_in,
		tab_out => mux_out,
		tx => tx,
		data_out => data_out,
		credit_i => credit_i);

	CLK_TX : for i in 0 to(NPORT-1) generate
		clock_tx(i) <= clock;
	end generate CLK_TX;  

end $Chave$;