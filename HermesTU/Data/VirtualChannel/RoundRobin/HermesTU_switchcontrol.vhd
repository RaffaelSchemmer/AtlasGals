library IEEE;
use IEEE.STD_LOGIC_1164.all;
use IEEE.STD_LOGIC_unsigned.all;
use work.HermesTUPackage.all;

entity HermesTU_switchcontrol is
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
end HermesTU_switchcontrol;

architecture HermesTU_switchcontrol of HermesTU_switchcontrol is

type state is (S0, S1, S2, S3, S4, S5, S6, S7, S8, S9, S10);
signal ES, PES: state;

-- sinais do arbitro
signal ask: std_logic := '0';
signal sel,prox: integer range 0 to (NPORT-1) := 0;
signal sel_lane: integer range 0 to (NLANE-1) := 0;
signal header : regflit := (others=>'0');

-- sinais do controle
-- signal dirx,diry: integer range 0 to (NPORT-1) := 0;
signal lx,ly,tx,ty: regquartoflit := (others=>'0');
signal auxfree: arrayNport_regNlane := (others=>(others=>'0'));
signal source: matrixNport_Nlane_reg8 := (others=>(others=>(others=>'0')));
signal sender_ant: arrayNport_regNlane := (others=>(others=>'0'));

begin

-- in unidirectional tori, input ports EAST and NORTH are not used, corresponding h need not exist
    ask <= '1' when h(LOCAL)(L1)='1' or h(LOCAL)(L2)='1' or
                    h(WEST)(L1)='1'  or h(WEST)(L2)='1'  or
                    h(SOUTH)(L1)='1' or h(SOUTH)(L2)='1' else '0';

    header <= data(sel)(sel_lane);

    process(sel,h)
    begin
        case sel is
            when LOCAL=>
                if h(WEST)/="00" then prox<=WEST;
                elsif h(SOUTH)/="00" then prox<=SOUTH;
                else prox<=LOCAL; end if;
            when EAST=>
                if h(WEST)/="00" then prox<=WEST;
                elsif h(SOUTH)/="00" then prox<=SOUTH;
                elsif h(LOCAL)/="00" then prox<=LOCAL;
                else prox<=EAST; end if;
            when WEST=>
                if h(SOUTH)/="00" then prox<=SOUTH;
                elsif h(LOCAL)/="00" then prox<=LOCAL;
                else prox<=WEST; end if;
            when NORTH=>
                if h(SOUTH)/="00" then prox<=SOUTH;
                elsif h(LOCAL)/="00" then prox<=LOCAL;
                elsif h(WEST)/="00" then prox<=WEST;
                else prox<=NORTH; end if;
            when SOUTH=>
                if h(LOCAL)/="00" then prox<=LOCAL;
                elsif h(WEST)/="00" then prox<=WEST;
                else prox<=SOUTH; end if;
        end case;
    end process;

    lx <= address((METADEFLIT - 1) downto QUARTOFLIT);
    ly <= address((QUARTOFLIT - 1) downto 0);

    tx <= header((METADEFLIT - 1) downto QUARTOFLIT);
    ty <= header((QUARTOFLIT - 1) downto 0);

    process(reset,clock)
    begin
        if reset='1' then ES<=S0;
        elsif clock'event and clock='0' then ES<=PES;
        end if;
    end process;

    -- The unidrectional torus routing algorithm employed here was proposed by Duato in his book
    -- "Interconnection Networks" and works as follows:
    --
    -- xoffset = tx - lx; -- tx is the target router x coordinate. lx is the current router x coordinate
    -- yoffset = ty - ly; -- ty is the target router y coordinate. ly is the current router y coordinate
    -- if xoffset < 0 then dirx = WEST; lane = L1; end if; --c00
    -- if xoffset > 0 then dirx = WEST; lane = L2; end if; --c01
    -- if xoffset < 0 then dirx = NORTH; lane = L1; end if; --c10
    -- if xoffset > 0 then dirx = NORTH; lane = L1; end if; --c11
    -- if xoffset = 0 and yoffset = 0 then 'LOCAL' end if; --local

------------------------------------------------------------------------------------------------------
-- PARTE COMBINACIONAL PARA DEFINIR O PRÓXIMO ESTADO DA MÁQUINA.
--
-- S0 -> O estado S0 é o estado de inicialização da máquina. Este estado somente é
--       atingido quando o sinal reset é ativado.
-- S1 -> O estado S1 é o estado de espera por requisição de chaveamento. Quando o
--       árbitro recebe uma ou mais requisições, o sinal ask é ativado, fazendo a
--       máquina avançar para o estado S2.
-- S2 -> No estado S2 a porta de entrada que solicitou chaveamento é selecionada. Se
--       houver mais de uma, aquela atualmente com maior prioridade é a selecionada.
-- S3 -> No estado S3 é realizado algoritmo de roteamento XY (fase de seleção).
--       O algoritmo inicia pela comparação do endereço do roteador atual com o endereço
--       do roteador destino do pacote, contido no primeiro flit
--       do pacote. O pacote deve ser chaveado para a porta LOCAL do roteador quando
--       o endereço (lx,ly) do roteador atual for igual ao endereço (tx,ty) do roteador
--       destino do pacote. Em qualquer outra situação, o roteamento é realizado
--       baseando-se na diferença entre os valores absolutos de lx e tx e ly e ty.
--       De acordo com o valor obtido, o pacote é roteado em uma das direções em
--       um determinado canal virtual. O roteamento procede da seguinte forma:
--       (i) ao LESTE no canal virtual L1, se lx-tx maior que zero;
--       (ii) ao LESTE no canal virtual L2, se lx-tx menor que zero;
--       (iii) ao NORTE no canal virtual L1, se lx-tx igual a zero E ly-ty maior que zero; e
--       (iv) ao NORTE no canal virtual L2, se lx-tx igual a zero E ly-ty menor que zero.
--       Caso a porta escolhida pelo roteamento esteja ocupada, é realizado o bloqueio
--       dos flits do pacote até que este possa ser chaveado (teste do sinal auxfree).
-- S4, S5, S6, S7, S8, S9 -> Estes estados são usados para estabelecer a conexão da porta
--       de entrada específica com uma porta de saída específica, através do preenchimento
--	 dos campos mux_in e mux_out da tabela de conexões.
-- S10 -> O estado S10 é necessário para que a porta de entrada selecionada desative
--       o sinal h.
--
    process(ES,ask,h,lx,ly,tx,ty,auxfree) -- ,dirx,diry)
        begin
            case ES is
                when S0 => PES <= S1;
                when S1 => if ask='1' then PES <= S2; else PES <= S1; end if;
                when S2 => PES <= S3;
                when S3 =>
                    -- xoffset = 0 e yoffset = 0
                    if lx = tx and ly = ty and auxfree(LOCAL)(L1)='1' then PES<=S4;
                    -- xoffset = 0 e yoffset = 0
                    elsif lx = tx and ly = ty and auxfree(LOCAL)(L2)='1' then PES<=S5;
                    -- xoffset < 0
                    elsif lx > tx and auxfree(EAST)(L1)='1' then PES<=S6;
                    -- xoffset > 0
                    elsif lx < tx and auxfree(EAST)(L2)='1' then PES<=S7;
                    -- xoffset = 0 e yoffset < 0
                    elsif lx = tx and ly > ty and auxfree(NORTH)(L1)='1' then PES<=S8;
                    -- xoffset = 0 e yoffset > 0
                    elsif lx = tx and ly < ty and auxfree(NORTH)(L2)='1' then PES<=S9;
                    else PES<=S1; end if;
                when S10 => PES<=S1;
                when others => PES<=S10;
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
                        if h(prox)(L1)='1' then sel_lane <= L1;
                        else sel_lane <= L2; end if;
                    -- Estabelece a conexão com o canal L1 da porta LOCAL
                    when S4 =>
                        source(sel)(sel_lane) <= conv_channel(LOCAL,L1);
                        mux_out(LOCAL)(L1) <= conv_channel(sel,sel_lane);
                        auxfree(LOCAL)(L1) <= '0';
                        ack_h(sel)(sel_lane)<='1';
                    -- Estabelece a conexão com o canal L2 da porta LOCAL
                    when S5 =>
                        source(sel)(sel_lane) <= conv_channel(LOCAL,L2);
                        mux_out(LOCAL)(L2) <= conv_channel(sel,sel_lane);
                        auxfree(LOCAL)(L2) <= '0';
                        ack_h(sel)(sel_lane)<='1';
                    -- Estabelece a conexão com o canal L1 da porta EAST
                    when S6 =>
                        source(sel)(sel_lane) <= conv_channel(EAST,L1);
                        mux_out(EAST)(L1) <= conv_channel(sel,sel_lane);
                        auxfree(EAST)(L1) <= '0';
                        ack_h(sel)(sel_lane)<='1';
                    -- Estabelece a conexão com o canal L2 da porta EAST
                    when S7 =>
                        source(sel)(sel_lane) <= conv_channel(EAST,L2);
                        mux_out(EAST)(L2) <= conv_channel(sel,sel_lane);
                        auxfree(EAST)(L2) <= '0';
                        ack_h(sel)(sel_lane)<='1';
                    -- Estabelece a conexão com o canal L1 da porta NORTH
                    when S8 =>
                        source(sel)(sel_lane) <= conv_channel(NORTH,L1);
                        mux_out(NORTH)(L1) <= conv_channel(sel,sel_lane);
                        auxfree(NORTH)(L1) <= '0';
                        ack_h(sel)(sel_lane)<='1';
                    -- Estabelece a conexão com o canal L2 da porta NORTH
                    when S9 =>
                        source(sel)(sel_lane) <= conv_channel(NORTH,L2);
                        mux_out(NORTH)(L2) <= conv_channel(sel,sel_lane);
                        auxfree(NORTH)(L2) <= '0';
                        ack_h(sel)(sel_lane)<='1';
                    when others => ack_h(sel)(sel_lane)<='0';
                end case;

            sender_ant(LOCAL)(L1) <= sender(LOCAL)(L1);
            sender_ant(LOCAL)(L2) <= sender(LOCAL)(L2);
            sender_ant(WEST)(L1) <= sender(WEST)(L1);
            sender_ant(WEST)(L2) <= sender(WEST)(L2);
            sender_ant(SOUTH)(L1) <= sender(SOUTH)(L1);
            sender_ant(SOUTH)(L2) <= sender(SOUTH)(L2);

            if sender(LOCAL)(L1)='0' and sender_ant(LOCAL)(L1)='1' then auxfree(conv_port(source(LOCAL)(L1)))(conv_lane(source(LOCAL)(L1))) <='1'; end if;
            if sender(LOCAL)(L2)='0' and sender_ant(LOCAL)(L2)='1' then auxfree(conv_port(source(LOCAL)(L2)))(conv_lane(source(LOCAL)(L2))) <='1'; end if;
            if sender(WEST)(L1)='0' and sender_ant(WEST)(L1)='1' then auxfree(conv_port(source(WEST)(L1)))(conv_lane(source(WEST)(L1))) <='1'; end if;
            if sender(WEST)(L2)='0' and sender_ant(WEST)(L2)='1' then auxfree(conv_port(source(WEST)(L2)))(conv_lane(source(WEST)(L2))) <='1'; end if;
            if sender(SOUTH)(L1)='0' and sender_ant(SOUTH)(L1)='1' then auxfree(conv_port(source(SOUTH)(L1)))(conv_lane(source(SOUTH)(L1))) <='1'; end if;
            if sender(SOUTH)(L2)='0' and sender_ant(SOUTH)(L2)='1' then auxfree(conv_port(source(SOUTH)(L2)))(conv_lane(source(SOUTH)(L2))) <='1'; end if;

        end if;
    end process;

    mux_in <= source;
    free <= auxfree;

end HermesTU_switchcontrol;
