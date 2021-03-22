---------------------------------------------------------------------------------------	
--                                    ROUTER
--
--
--                                    NORTH         LOCAL
--                      -----------------------------------
--                      |             ******       ****** |
--                      |             *FILA*       *FILA* |
--                      |             ******       ****** |
--                      |          *************          |
--                      |          *  ARBITRO  *          |
--                      | ******   *************   ****** |
--                 WEST | *FILA*   *************   *FILA* | EAST
--                      | ******   *  CONTROLE *   ****** |
--                      |          *************          |
--                      |             ******              |
--                      |             *FILA*              |
--                      |             ******              |
--                      -----------------------------------
--                                    SOUTH
--
--  As chaves realizam a transferencia de mensagens entre nucleos. 
--  A chave possui uma logica de controle de chaveamento e 5 portas bidirecionais:
--  East, West, North, South e Local. Cada porta possui uma fila para o armazenamento 
--  temporario de flits. A porta Local estabelece a comunicacao entre a chave e seu 
--  nucleo. As demais portas ligam a chave as chaves vizinhas.
--  Os enderecos das chaves sao compostos pelas coordenadas XY da rede de interconexao, 
--  onde X e a posicao horizontal e Y a posicao vertical. A atribuicao de enderecos os 
--  chaves e necessaria para a execucao do algoritmo de chaveamento.
--  Os modulos principais que compeem a chave sao: fila, arbitro e logica de 
--  chaveamento implementada pelo controle_mux. Cada uma das filas da chave (E, W, N, 
--  S e L), ao receber um novo pacote requisita chaveamento ao arbitro. O arbitro 
--  seleciona a requisicao de maior prioridade, quando existem requisicoes simultaneas, 
--  e encaminha o pedido de chaveamento e logica de chaveamento. A logica de 
--  chaveamento verifica se e possivel atender e solicitacao. Sendo possivel, a conexao
--  e estabelecida e o arbitro e informado. Por sua vez, o arbitro informa a fila que 
--  comeca a enviar os flits armazenados. Quando todos os flits do pacote foram 
--  enviados, a conexao e concluida pela sinalizacao, por parte da fila, atraves do 
--  sinal sender.
---------------------------------------------------------------------------------------
library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;
use work.HermesG_package.all;

entity $router_name$ is
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
	$router_pin$
	data_out:  out arrayNport_regflit;
	credit_i:  in  regNport);
end $router_name$;

architecture $router_name$ of $router_name$ is

signal h, ack_h, data_av, sender, data_ack: regNport := (others=>'0');
signal data: arrayNport_regflit := (others=>(others=>'0'));
signal mux_in, mux_out: arrayNport_reg3 := (others=>(others=>'0'));
signal free: regNport := (others=>'0');

begin

$port_type$

	SwitchControl : Entity work.HermesG_switchcontrol($Algorithm_type$)
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

	CrossBar : Entity  work.$Crossbar_type$
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

$Pin_ground$

	CLK_TX : for i in 0 to(NPORT-1) generate
		clock_tx(i) <= clock;
	end generate CLK_TX;  

end $router_name$;
