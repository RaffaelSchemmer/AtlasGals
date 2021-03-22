library IEEE;
use IEEE.std_logic_1164.all;
use IEEE.std_logic_arith.all;
use IEEE.std_logic_unsigned.all;
use work.Hermes_Package.all;

entity Hermes_outport is
port(
	-- sinal de sincronismo
	clock:    in  std_logic;
	reset:    in  std_logic;

	-- sinais para a requisicao de roteamento
	rr:       in  arrayNlane_regNport;
	ack_rr:   out arrayNlane_regNport;

	-- sinais para a transmissao interna ao roteador
	data_av:  in  arrayNlane_regNport;
	data:     in  matrixNlane_Nport_regflit;
	data_ack: out arrayNlane_regNport;
	EOP:  	  in  arrayNlane_regNport;

	-- sinais para a transmissao externa ao roteador
	clock_tx: out std_logic;
	tx:       out std_logic;
	data_out: out regflit;
	lane_tx:  out regNlane;
	credit_i: in  regNlane
);
end Hermes_outport;

architecture Hermes_outport of Hermes_outport is
	signal grant, tx_sgn, cge_sgn: regNlane;
	signal data_out_sgn: arrayNlane_regflit;
	signal index, lane_tx_sgn: regNlane;
	signal granted_cv: regNlane;
	signal credit_i_sgn: regNlane;
begin

	SR_out_CV0: entity work.Hermes_cv_out
	port map(
		clock=>clock,
		reset=>reset,

		rr=>rr(ID_CV0),
		ack_rr=>ack_rr(ID_CV0),	

		data_av=>data_av(ID_CV0),
		data=>data(ID_CV0),
		data_ack=>data_ack(ID_CV0),
		EOP=>EOP(ID_CV0),		

		tx=>tx_sgn(ID_CV0),
		data_out=>data_out_sgn(ID_CV0),
		credit_i=>credit_i_sgn(ID_CV0)
	);

	SR_out_CV1: entity work.Hermes_cv_out
	port map(
		clock=>clock,
		reset=>reset,

		rr=>rr(ID_CV1),
		ack_rr=>ack_rr(ID_CV1),	

		data_av=>data_av(ID_CV1),
		data=>data(ID_CV1),
		data_ack=>data_ack(ID_CV1),
		EOP=>EOP(ID_CV1),		

		tx=>tx_sgn(ID_CV1),
		data_out=>data_out_sgn(ID_CV1),
		credit_i=>credit_i_sgn(ID_CV1)
	);

	SR_out_CV2: entity work.Hermes_cv_out
	port map(
		clock=>clock,
		reset=>reset,

		rr=>rr(ID_CV2),
		ack_rr=>ack_rr(ID_CV2),	

		data_av=>data_av(ID_CV2),
		data=>data(ID_CV2),
		data_ack=>data_ack(ID_CV2),
		EOP=>EOP(ID_CV2),		

		tx=>tx_sgn(ID_CV2),
		data_out=>data_out_sgn(ID_CV2),
		credit_i=>credit_i_sgn(ID_CV2)
	);

	SR_out_CV3: entity work.Hermes_cv_out
	port map(
		clock=>clock,
		reset=>reset,

		rr=>rr(ID_CV3),
		ack_rr=>ack_rr(ID_CV3),	

		data_av=>data_av(ID_CV3),
		data=>data(ID_CV3),
		data_ack=>data_ack(ID_CV3),
		EOP=>EOP(ID_CV3),		

		tx=>tx_sgn(ID_CV3),
		data_out=>data_out_sgn(ID_CV3),
		credit_i=>credit_i_sgn(ID_CV3)
	);

	-- Maquina de definição de prioridades de transmissão
	--  CV0 = pacotes de controle (Mais alta prioridade na rede)
	--  CV1 e CV2 = pacotes de serviço garantido (Pacotes que competem igualmente pela transmissão. Só perdem para o canal CV0)
	--  CV3 = pacotes de melhor esforço (Menor prioridade na rede)
	-- Sinal de grant é atribuido na borda de descida
	process(reset, clock)
		variable l_grant: regNlane;
	begin
	
		if(reset/='0')then
			grant<=(others=>'0');
			granted_cv<=conv_std_logic_vector(ID_CV2, NLANE);
		elsif(clock'event and clock='0')then
			l_grant:=(others=>'0');
			if(credit_i=x"0000")then
				if(tx_sgn(ID_CV0)='1')then
					l_grant(ID_CV0):='1';
				elsif(tx_sgn(ID_CV1)='1' and (granted_cv=conv_std_logic_vector(ID_CV1, NLANE) or (tx_sgn(ID_CV2)='0' and granted_cv=conv_std_logic_vector(ID_CV2, NLANE) )))then
					l_grant(ID_CV1):='1';
					granted_cv<=conv_std_logic_vector(ID_CV1, NLANE);
				elsif(tx_sgn(ID_CV2)='1' and (granted_cv=conv_std_logic_vector(ID_CV2, NLANE) or (tx_sgn(ID_CV1)='0' and granted_cv=conv_std_logic_vector(ID_CV1, NLANE) )))then
					l_grant(ID_CV2):='1';
					granted_cv<=conv_std_logic_vector(ID_CV2, NLANE);
				elsif(tx_sgn(ID_CV3)='1')then
					l_grant(ID_CV3):='1';
				end if;
			else
				if(tx_sgn(ID_CV0)='1' and credit_i(ID_CV0)='1' and ((EOP(ID_CV0)=x"0000")or(EOP(ID_CV0)/=x"0000" and grant(ID_CV0)='0')))then
					l_grant(ID_CV0):='1';
				elsif((tx_sgn(ID_CV1)='1' and credit_i(ID_CV1)='1' and ((EOP(ID_CV1)=x"0000")or(EOP(ID_CV1)/=x"0000" and grant(ID_CV1)='0'))) or (tx_sgn(ID_CV2)='1' and credit_i(ID_CV2)='1' and ((EOP(ID_CV2)=x"0000")or(EOP(ID_CV2)/=x"0000" and grant(ID_CV2)='0'))))then
				  if (tx_sgn(ID_CV1)='1' and credit_i(ID_CV1)='1' and ((EOP(ID_CV1)=x"0000")or(EOP(ID_CV1)/=x"0000" and grant(ID_CV1)='0')) and ((granted_cv/=conv_std_logic_vector(ID_CV1, NLANE)) or (granted_cv=conv_std_logic_vector(ID_CV1, NLANE) and tx_sgn(ID_CV2)/='1'))) then
						l_grant(ID_CV1):='1';
						granted_cv<=conv_std_logic_vector(ID_CV1, NLANE);
					elsif (tx_sgn(ID_CV2)='1' and credit_i(ID_CV2)='1' and ((EOP(ID_CV2)=x"0000")or(EOP(ID_CV2)/=x"0000" and grant(ID_CV2)='0')) and ((granted_cv/=conv_std_logic_vector(ID_CV2, NLANE)) or (granted_cv=conv_std_logic_vector(ID_CV2, NLANE) and tx_sgn(ID_CV1)/='1'))) then
						l_grant(ID_CV2):='1';
						granted_cv<=conv_std_logic_vector(ID_CV2, NLANE);
					end if;
				elsif(tx_sgn(ID_CV3)='1' and credit_i(ID_CV3)='1' and ((EOP(ID_CV3)=x"0000")or(EOP(ID_CV3)/=x"0000" and grant(ID_CV3)='0')))then
					l_grant(ID_CV3):='1';
				end if;
			end if;
			
			if(l_grant=x"0000") then
				if(tx_sgn(ID_CV0)='1' and ((EOP(ID_CV0)=x"0000")or(EOP(ID_CV0)/=x"0000" and grant(ID_CV0)='0')))then				
					l_grant(ID_CV0):='1';
				elsif(tx_sgn(ID_CV1)='1' and ((EOP(ID_CV1)=x"0000")or(EOP(ID_CV1)/=x"0000" and grant(ID_CV1)='0')))then
					l_grant(ID_CV1):='1';
					granted_cv<=conv_std_logic_vector(ID_CV1, NLANE);
				elsif(tx_sgn(ID_CV2)='1' and ((EOP(ID_CV1)=x"0000")or(EOP(ID_CV2)/=x"0000" and grant(ID_CV2)='0')))then
					l_grant(ID_CV2):='1';
					granted_cv<=conv_std_logic_vector(ID_CV2, NLANE);
				elsif(tx_sgn(ID_CV3)='1')then
					l_grant(ID_CV3):='1';
				end if;
			end if;
			
			grant<=l_grant;
			
		end if;
		
	end process;
	
	process(reset, clock)
	variable l_tx: std_logic;
	begin
		if(reset/='0')then
			index<=x"F";
			lane_tx_sgn<=(others=>'0');
		elsif(clock'event and clock='1')then
			lane_tx_sgn<=grant;
			if(grant=x"0000")then
				index<=x"F";
			else
				if(grant(ID_CV0)='1')then
					index<=conv_std_logic_vector(ID_CV0, NLANE);
				elsif(grant(ID_CV1)='1')then
					index<=conv_std_logic_vector(ID_CV1, NLANE);
				elsif(grant(ID_CV2)='1')then
					index<=conv_std_logic_vector(ID_CV2, NLANE);
				elsif(grant(ID_CV3)='1')then
					index<=conv_std_logic_vector(ID_CV3, NLANE);
				end if;
			end if;
			
		end if;
		
	end process;
	
	tx <= '0' when index=x"F" or reset='1' else tx_sgn(CONV_INTEGER(index));

	data_out <= (others=>'0') when index=x"F" or reset='1'  else data_out_sgn(CONV_INTEGER(index));

	lane_tx<=	(others=>'0') when index=x"F" or reset='1' else 
						(others=>'0')	when tx_sgn(CONV_INTEGER(index))='0' else
						lane_tx_sgn;
	
	clock_tx <= clock;
	
	credit_i_sgn <= (others=>'0') when index=x"F" or reset='1' else 
					(others=>'0')	when tx_sgn(CONV_INTEGER(index))='0' else
					(lane_tx_sgn and credit_i);

end Hermes_outport;