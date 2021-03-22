library IEEE;
use IEEE.STD_LOGIC_1164.all;
use IEEE.STD_LOGIC_unsigned.all;
use IEEE.std_logic_arith.all;
use work.HermesPackage.all;

entity Hermes_switchcontrol is
port(
	clock :   in  std_logic;
	reset :   in  std_logic;
	h :       in  arrayNport_regNlane;
	ack_h :   out arrayNport_regNlane;
	address : in  regmetadeflit;
	data :    in  matrixNport_Nlane_regflit;
	sender :  in  arrayNport_regNlane;
	free :    out arrayNport_regNlane;
	mux_in :  out matrixNport_Nlane_reg8;
	mux_out : out matrixNport_Nlane_reg8);
end Hermes_switchcontrol;

architecture Hermes_switchcontrol of Hermes_switchcontrol is

$state$
signal ES, PES: state;

-- sinais do arbitro
signal ask: std_logic := '0';
signal sel,prox: integer range 0 to (NPORT-1) := 0;
signal sel_lane: integer range 0 to (NLANE-1) := 0;
signal header : regflit := (others=>'0');

-- sinais do controle
signal dirx,diry: integer range 0 to (NPORT-1) := 0;
signal lx,ly,tx,ty: regquartoflit := (others=>'0');
signal auxfree: arrayNport_regNlane := (others=>(others=>'0'));
signal source: matrixNport_Nlane_reg8 := (others=>(others=>(others=>'0')));
signal sender_ant: arrayNport_regNlane := (others=>(others=>'0'));

begin

$ask$

	header <= data(sel)(sel_lane);

	process(sel,h)
	begin
		case sel is
			when LOCAL=>
$localSel$
			when EAST=>
$eastSel$
			when WEST=>
$westSel$
			when NORTH=>
$northSel$
			when SOUTH=>
$southSel$
		end case;
	end process;

	lx <= address((METADEFLIT - 1) downto QUARTOFLIT);
	ly <= address((QUARTOFLIT - 1) downto 0);

	tx <= header((METADEFLIT - 1) downto QUARTOFLIT);
	ty <= header((QUARTOFLIT - 1) downto 0);

	dirx <= WEST when lx > tx else EAST;
	diry <= NORTH when ly < ty else SOUTH;

	process(reset,clock)
	begin
		if reset='1' then
			ES<=S0;
		elsif clock'event and clock='0' then
			ES<=PES;
		end if;
	end process;

	------------------------------------------------------------------------------------------------------
	-- PARTE COMBINACIONAL PARA DEFINIR O PRÓXIMO ESTADO DA MÁQUINA.
	--
	-- SO -> O estado S0 é o estado de inicialização da máquina. Este estado somente é
	--       atingido quando o sinal reset é ativado.
	-- S1 -> O estado S1 é o estado de espera por requisição de chaveamento. Quando o
	--       árbitro recebe uma ou mais requisições o sinal ask é ativado fazendo a
	--       máquina avançar para o estado S2.
	-- S2 -> No estado S2 a porta de entrada que solicitou chaveamento é selecionada. Se
	--       houver mais de uma, aquela com maior prioridade é a selecionada.
	-- S3 -> No estado S3 é realizado algoritmo de chaveamento XY. O algoritmo de chaveamento
	--       XY faz a comparação do endereço da chave atual com o endereço da chave destino do
	--       pacote (armazenado no primeiro flit do pacote). O pacote deve ser chaveado para a
	--       porta Local da chave quando o endereço xLyL* da chave atual for igual ao endereço
	--       xTyT* da chave destino do pacote. Caso contrário, é realizada, primeiramente, a
	--       comparação horizontal de endereços. A comparação horizontal determina se o pacote
	--       deve ser chaveado para o Leste (xL<xT), para o Oeste (xL>xT), ou se o mesmo já
	--       está horizontalmente alinhado à chave destino (xL=xT). Caso esta última condição
	--       seja verdadeira é realizada a comparação vertical que determina se o pacote deve
	--       ser chaveado para o Sul (yL<yT) ou para o Norte (yL>yT). Caso a porta vertical
	--       escolhida esteja ocupada, é realizado o bloqueio dos flits do pacote até que o
	--       pacote possa ser chaveado.
	-- $statesConnection$-> Nestes estados é estabelecida a conexão da porta de entrada com a de
	--       de saída através do preenchimento dos sinais mux_in e mux_out.
	-- S9 -> O estado S9 é necessário para que a porta selecionada para roteamento baixe o sinal
	--       h.
	--
	process(ES,ask,h,lx,ly,tx,ty,auxfree,dirx,diry)
	begin
		case ES is
			when S0 => PES <= S1;
			when S1 => if ask='1' then PES <= S2; else PES <= S1; end if;
			when S2 => PES <= S3;
			when S3 =>
$statesConditions$
				else PES<=S1; end if;
$statesFinal$
		end case;
	end process;

	------------------------------------------------------------------------------------------------------
	-- executa as ações correspondente ao estado atual da máquina de estados
	------------------------------------------------------------------------------------------------------
	process (clock)
	begin
		if clock'event and clock='1' then
		case ES is
			-- Zera variáveis
			when S0 =>
				sel <= 0;
				sel_lane <= 0;
				ack_h <= (others => (others=>'0'));
				auxfree <= (others => (others=>'1'));
				sender_ant <= (others => (others=>'0'));
				mux_out <= (others=>(others=>(others=>'0')));
				source <= (others=>(others=>(others=>'0')));
			-- Chegou um header
			when S1=>
				ack_h <= (others => (others=>'0'));
			-- Seleciona quem tera direito a requisitar roteamento
			when S2=>
				sel <= prox;
$state_s2$
$statesImplements$
			when others => ack_h(sel)(sel_lane)<='0';
		end case;

$sender_ant$

$senderConditions$
		end if;
	end process;

	mux_in <= source;
	free <= auxfree;

end Hermes_switchcontrol;