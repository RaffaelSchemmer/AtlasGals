library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_unsigned.all;
use work.Hermes_Package.all;

entity Hermes_inport is
port(
	-- Sinais de sincronismo
	clock:    in  std_logic;
	reset:    in  std_logic;

	-- Sinais para a transmissao de dados
	clock_rx: in  std_logic;
	rx:       in  std_logic;
	data_in:  in  regflit;
	lane_rx:  in  regNlane;
	credit_o: out regNlane;

	-- sinais para a requisicao de roteamento
	rr:       out arrayNlane_regNport; -- cada sinal rr deve ir para cada porta destino e para cada e cada canal correspondente *
	ack_rr:   in  regNlane; -- *

	-- sinais para a transmissao
	data_av:  out arrayNlane_regNport; --*
	data:     out arrayNlane_regflit; -- cada flit deve estar conectado a cada porta destino e a cada canal daquela porta correspondente
	data_ack: in  regNlane; -- *
	eop:      out arrayNlane_regNport -- cada sinal rr deve ir para cada porta destino e para cada e cada canal correspondente *
	);
end Hermes_inport;

architecture Hermes_inport of Hermes_inport is
	signal rx_cv: regNlane;
begin

	rx_cv <= lane_rx when rx='1' else (others=>'0');

	-- Canal BE de menor prioridade (mensagens de melhor esforço)
	SR_in_CV0: entity work.Hermes_cv_in
	port map(
		-- sinais de sincronismo com a interface do roteador
		clock=>clock,
		reset=>reset,
		clock_rx=>clock_rx,
		rx=>rx_cv(ID_CV0),
		data_in=>data_in,
		credit_o=>credit_o(ID_CV0),
		-- sinais de sincronismo para a parte interna do roteador
		rr=>rr(ID_CV0),
		ack_rr=>ack_rr(ID_CV0),
		data_av=>data_av(ID_CV0),
		data=>data(ID_CV0),
		data_ack=>data_ack(ID_CV0),
		EOP=>eop(ID_CV0)
	);
	
	-- Canal GS de prioridade média (mensagens de servico garantido)
	SR_in_CV1: entity work.Hermes_cv_in
	port map(
		-- sinais de sincronismo com a interface do roteador
		clock=>clock,
		reset=>reset,
		clock_rx=>clock_rx,
		rx=>rx_cv(ID_CV1),
		data_in=>data_in,
		credit_o=>credit_o(ID_CV1),
		-- sinais de sincronismo para a parte interna do roteador
		rr=>rr(ID_CV1),
		ack_rr=>ack_rr(ID_CV1),
		data_av=>data_av(ID_CV1),
		data=>data(ID_CV1),
		data_ack=>data_ack(ID_CV1),
		EOP=>eop(ID_CV1)
	);
	
	-- Canal GS de prioridade média (mensagens de servico garantido)
	SR_in_CV2: entity work.Hermes_cv_in
	port map(
		-- sinais de sincronismo com a interface do roteador
		clock=>clock,
		reset=>reset,
		clock_rx=>clock_rx,
		rx=>rx_cv(ID_CV2),
		data_in=>data_in,
		credit_o=>credit_o(ID_CV2),
		-- sinais de sincronismo para a parte interna do roteador
		rr=>rr(ID_CV2),
		ack_rr=>ack_rr(ID_CV2),
		data_av=>data_av(ID_CV2),
		data=>data(ID_CV2),
		data_ack=>data_ack(ID_CV2),
		EOP=>eop(ID_CV2)
	);
	
	-- Canal GS de prioridade máxima (mensagens de controle)
	SR_in_CV3: entity work.Hermes_cv_in
	port map(
		-- sinais de sincronismo com a interface do roteador
		clock=>clock,
		reset=>reset,
		clock_rx=>clock_rx,
		rx=>rx_cv(ID_CV3),
		data_in=>data_in,
		credit_o=>credit_o(ID_CV3),
		-- sinais de sincronismo para a parte interna do roteador
		rr=>rr(ID_CV3),
		ack_rr=>ack_rr(ID_CV3),
		data_av=>data_av(ID_CV3),
		data=>data(ID_CV3),
		data_ack=>data_ack(ID_CV3),
		EOP=>eop(ID_CV3)
	);

end Hermes_inport;

architecture Hermes_inport_cg of Hermes_inport is
	signal rx_cv: regNlane;
	signal clock_rx_cv0, clock_rx_cv1, clock_rx_cv2, clock_rx_cv3: std_logic;
begin

	rx_cv <= lane_rx when rx='1' else (others=>'0');
	clock_rx_cv0 <= clock_rx when lane_rx(ID_CV0)='1' and rx='1' else '1';
	clock_rx_cv1 <= clock_rx when lane_rx(ID_CV1)='1' and rx='1' else '1';
	clock_rx_cv2 <= clock_rx when lane_rx(ID_CV2)='1' and rx='1' else '1';
	clock_rx_cv3 <= clock_rx when lane_rx(ID_CV3)='1' and rx='1' else '1';

	-- Canal BE de menor prioridade (mensagens de melhor esforço)
	SR_in_CV0: entity work.Hermes_cv_in
	port map(
		-- sinais de sincronismo com a interface do roteador
		clock=>clock,
		reset=>reset,
		clock_rx=>clock_rx_cv0,
		rx=>rx_cv(ID_CV0),
		data_in=>data_in,
		credit_o=>credit_o(ID_CV0),
		-- sinais de sincronismo para a parte interna do roteador
		rr=>rr(ID_CV0),
		ack_rr=>ack_rr(ID_CV0),
		data_av=>data_av(ID_CV0),
		data=>data(ID_CV0),
		data_ack=>data_ack(ID_CV0),
		EOP=>eop(ID_CV0)
	);
	
	-- Canal GS de prioridade média (mensagens de servico garantido)
	SR_in_CV1: entity work.Hermes_cv_in
	port map(
		-- sinais de sincronismo com a interface do roteador
		clock=>clock,
		reset=>reset,
		clock_rx=>clock_rx_cv1,
		rx=>rx_cv(ID_CV1),
		data_in=>data_in,
		credit_o=>credit_o(ID_CV1),
		-- sinais de sincronismo para a parte interna do roteador
		rr=>rr(ID_CV1),
		ack_rr=>ack_rr(ID_CV1),
		data_av=>data_av(ID_CV1),
		data=>data(ID_CV1),
		data_ack=>data_ack(ID_CV1),
		EOP=>eop(ID_CV1)
	);
	
	-- Canal GS de prioridade média (mensagens de servico garantido)
	SR_in_CV2: entity work.Hermes_cv_in
	port map(
		-- sinais de sincronismo com a interface do roteador
		clock=>clock,
		reset=>reset,
		clock_rx=>clock_rx_cv2,
		rx=>rx_cv(ID_CV2),
		data_in=>data_in,
		credit_o=>credit_o(ID_CV2),
		-- sinais de sincronismo para a parte interna do roteador
		rr=>rr(ID_CV2),
		ack_rr=>ack_rr(ID_CV2),
		data_av=>data_av(ID_CV2),
		data=>data(ID_CV2),
		data_ack=>data_ack(ID_CV2),
		EOP=>eop(ID_CV2)
	);
	
	-- Canal GS de prioridade máxima (mensagens de controle)
	SR_in_CV3: entity work.Hermes_cv_in
	port map(
		-- sinais de sincronismo com a interface do roteador
		clock=>clock,
		reset=>reset,
		clock_rx=>clock_rx_cv3,
		rx=>rx_cv(ID_CV3),
		data_in=>data_in,
		credit_o=>credit_o(ID_CV3),
		-- sinais de sincronismo para a parte interna do roteador
		rr=>rr(ID_CV3),
		ack_rr=>ack_rr(ID_CV3),
		data_av=>data_av(ID_CV3),
		data=>data(ID_CV3),
		data_ack=>data_ack(ID_CV3),
		EOP=>eop(ID_CV3)
	);

end Hermes_inport_cg;
