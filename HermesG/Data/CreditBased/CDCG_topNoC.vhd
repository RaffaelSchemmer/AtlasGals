library IEEE;
use IEEE.std_logic_1164.all;
use ieee.std_logic_arith.CONV_STD_LOGIC_VECTOR;
use work.HermesG_package.all;

entity CDCG_topNoC is
end;

architecture CDCG_topNoC of CDCG_topNoC is

$clock_names$
$send_rec$
	signal reset 	  		: std_logic;
	signal clock_rx, rx, credit_o  : regNrot;
	signal clock_tx, tx, credit_i  : regNrot;
	signal data_in, data_out 	: arrayNrot_regflit;

    begin

$reset$

$clock_list$
	NOC: Entity work.NOC(NOC)
	port map(
$clock_for_noc$
		reset         => reset,
		clock_rxLocal => clock_rx,
		rxLocal       => rx,
		data_inLocal  => data_in,
		credit_oLocal => credit_o,
		clock_txLocal => clock_tx,
		txLocal       => tx,
		data_outLocal => data_out,
		credit_iLocal => credit_i
	);	

$input_module$

$output_module$

$manager_apps$

$end_sim$

end CDCG_topNoC;
