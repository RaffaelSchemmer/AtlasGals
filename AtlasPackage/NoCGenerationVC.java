package AtlasPackage;

import javax.swing.*;
import java.io.*;
import java.util.*;

/**
 * Generate a NoC with virtual channels.
 * @author Aline Vieira de Mello
 * @version
 */
public class NoCGenerationVC extends NoCGeneration{

	private String sourceDir, nocDir, scDir, projectDir;
	private String algorithm, nocType;
	private int dimX, dimY, dimension, flitSize, nChannels;
	private boolean isSC;
	private Vector<String> vectorSwitch;

	/**
	 * Generate a NoC with Virtual Channels.
	 * @param project The NoC project.
	 * @param source The path where are the source files. 
	 */
	public NoCGenerationVC(Project project, String source){
		super(project, source);
		sourceDir  = source;
		initialize(project);
	}

	/**
	 * Initialize variables.
	 * @param project The NoC project.
	 */
	private void initialize(Project project){
		NoC noc = project.getNoC();
		nocType = noc.getType();
		dimX = noc.getNumRotX();
		dimY = noc.getNumRotY();
		dimension = dimX * dimY;
		flitSize = noc.getFlitSize();
		nChannels = noc.getVirtualChannel();
		algorithm = noc.getRoutingAlgorithm();
		isSC = noc.isSCTB();
		vectorSwitch = new Vector<String>();
		projectDir = project.getPath() + File.separator;
		nocDir     = projectDir + "NOC" + File.separator;
		scDir      = projectDir + "SC_NoC" + File.separator;
	}
	
	/**
	 * Generate the NoC and SC files
	 */
	public void generate(){
		//create the project directory tree
		makeDiretories();
		//create the Hermes_package.vhd file
		createPackage("Hermes_package.vhd");
		//copy the Hermes_buffer file
		ManipulateFile.copy(new File(sourceDir+"Hermes_buffer.vhd"), nocDir);
		//create the Hermes_inport.vhd file
		createInport();
		//create the Hermes_switchcontrol.vhd file
		createSwitchcontrol();
		//create the Hermes_outport.vhd file
		createOutport();
		//create the routers
		createRouters();
		//create the NoC
		createNoC();
		//If the SC test bench option is selected, create the SC file
		if(isSC)
			createSC();
	}

/*********************************************************************************
* INPORT
*********************************************************************************/

	/**
	* Create the Hermes_inport.vhdl file, replacing the flags.
	*/
	public void createInport(){
		StringTokenizer st;
		String line, word;

		try{
			FileInputStream inFile=new FileInputStream(sourceDir+"Hermes_inport.vhd");
			BufferedReader buff=new BufferedReader(new InputStreamReader(inFile));

			DataOutputStream data_output=new DataOutputStream(new FileOutputStream(nocDir + "Hermes_inport.vhd"));

			int n_lines=0;
			line=buff.readLine();
			while(line!=null){
				st = new StringTokenizer(line, "$");
				int nTokens = st.countTokens();
				for (int cont=0; cont<nTokens; cont++){
					word = st.nextToken();
					if(word.equalsIgnoreCase("channelsLocal")){
						writeInputChannelsLocal(data_output);
					}
					else if(word.equalsIgnoreCase("rxSignals")){
						writeInputRxSignals(data_output);
					}
					else if(word.equalsIgnoreCase("rxChannels")){
						writeInputRxChannels(data_output);
					}
					else if(word.equalsIgnoreCase("bufChannels")){
						writeInputBufChannels(data_output);
					}
					else{
						if(cont==nTokens-1)
							data_output.writeBytes(word+"\n");
						else
							data_output.writeBytes(word);
					}
				}
				if(nTokens==0) //white line
					data_output.writeBytes("\n");
				n_lines++;
				line=buff.readLine();
			} //end while
			buff.close();
			data_output.close();
			inFile.close();
		}catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't write Hermes_inport.vhd","Output error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}catch(Exception e){
			JOptionPane.showMessageDialog(null,"Can't write Hermes_inport.vhd\n"+e.getMessage(),"Error Message", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}

	/**
	 * Write zeros in the local signals.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeInputChannelsLocal(DataOutputStream data_output) throws Exception{
		for(int i=1;i<nChannels;i++){
			data_output.writeBytes("\tcredit_o(L"+(i+1)+") <= '0';\n");
			data_output.writeBytes("\th(L"+(i+1)+") <= '0';\n");
			data_output.writeBytes("\tdata_av(L"+(i+1)+") <= '0';\n");
			data_output.writeBytes("\tdata(L"+(i+1)+") <= (others=>'0');\n");
			data_output.writeBytes("\tsender(L"+(i+1)+") <= '0';\n\n");
		}
	}	

	/**
	 * Write the Rx signals for all virtual channels.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeInputRxSignals(DataOutputStream data_output) throws Exception{
		data_output.writeBytes("signal ");
		for(int i=0;i<nChannels;i++){
			if(i!=(nChannels-1))
				data_output.writeBytes("rxL"+(i+1)+", ");
			else
				data_output.writeBytes("rxL"+(i+1)+": std_logic := '0';\n");
		}
	}	

	/**
	 * Write test of Rx signal in all virtual channels.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeInputRxChannels(DataOutputStream data_output) throws Exception{
		for(int i=0;i<nChannels;i++)
			data_output.writeBytes("\trxL"+(i+1)+"<= '1' when rx='1' and lane_rx(L"+(i+1)+")='1' else '0';\n");
	}

	/**
	 * Write the buffer port maps for all virtual channels.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeInputBufChannels(DataOutputStream data_output) throws Exception{
		for(int i=0;i<nChannels;i++){
			data_output.writeBytes("\n\tBUFL"+(i+1)+": entity work.Hermes_buffer\n\tport map(\n");
			data_output.writeBytes("\t\tclock    => clock,\n");
			data_output.writeBytes("\t\treset    => reset,\n");
			data_output.writeBytes("\t\tclock_rx => clock_rx,\n");
			data_output.writeBytes("\t\trx       => rxL"+(i+1)+",\n");
			data_output.writeBytes("\t\tdata_in  => data_in,\n");
			data_output.writeBytes("\t\tcredit_o => credit_o(L"+(i+1)+"),\n");
			data_output.writeBytes("\t\th        => h(L"+(i+1)+"),\n");
			data_output.writeBytes("\t\tack_h    => ack_h(L"+(i+1)+"),\n");
			data_output.writeBytes("\t\tdata_av  => data_av(L"+(i+1)+"),\n");
			data_output.writeBytes("\t\tdata     => data(L"+(i+1)+"),\n");
			data_output.writeBytes("\t\tdata_ack => data_ack(L"+(i+1)+"),\n");
			data_output.writeBytes("\t\tsender   => sender(L"+(i+1)+"));\n");
		}
	}
	
/*********************************************************************************
* OUTPORT
*********************************************************************************/

	/**
	 * Create the Hermes_outport.vhdl file, replacing the flags.
	 */
	public void createOutport(){
		StringTokenizer st;
		String line, word;

		try{

			FileInputStream inFile = new FileInputStream(sourceDir + "Hermes_outport.vhd");
			BufferedReader buff = new BufferedReader(new InputStreamReader(inFile));

			DataOutputStream data_output=new DataOutputStream(new FileOutputStream(nocDir + "Hermes_outport.vhd"));

			int n_lines=0;
			line=buff.readLine();
			while(line!=null){
				st = new StringTokenizer(line, "$");
				int nTokens = st.countTokens();
				for (int cont=0; cont<nTokens; cont++){
					word = st.nextToken();
					if(word.equalsIgnoreCase("signal")){
						writeOutputSignal(data_output);
					}
					else if(word.equalsIgnoreCase("cs_local")){
						writeOutputCS_Local(data_output);
					}
					else if(word.equalsIgnoreCase("cs_east")){
						writeOutputCS_East(data_output);
					}
					else if(word.equalsIgnoreCase("cs_west")){
						writeOutputCS_West(data_output);
					}
					else if(word.equalsIgnoreCase("cs_north")){
						writeOutputCS_North(data_output); 
					}
					else if(word.equalsIgnoreCase("cs_south")){
						writeOutputCS_South(data_output); 
					}
					else if(word.equalsIgnoreCase("tx")){
						writeOutputTx(data_output);
					}
					else if(word.equalsIgnoreCase("data_out_local")){
						writeOutputDataOut_Local(data_output);
					}
					else if(word.equalsIgnoreCase("data_out_east")){
						writeOutputDataOut_East(data_output);
					}
					else if(word.equalsIgnoreCase("data_out_west")){
						writeOutputDataOut_West(data_output);
					}
					else if(word.equalsIgnoreCase("data_out_north")){
						writeOutputDataOut_North(data_output);
					}
					else if(word.equalsIgnoreCase("data_out_south")){
						writeOutputDataOut_South(data_output);
					}
					else if(word.equalsIgnoreCase("data_ack_local")){
						writeOutputDataAck_Local(data_output);
					}
					else if(word.equalsIgnoreCase("data_ack_east")){
						writeOutputDataAck_East(data_output);
					}
					else if(word.equalsIgnoreCase("data_ack_west")){
						writeOutputDataAck_West(data_output);
					}
					else if(word.equalsIgnoreCase("data_ack_north")){
						writeOutputDataAck_North(data_output);
					}
					else if(word.equalsIgnoreCase("data_ack_south")){
						writeOutputDataAck_South(data_output);
					}
					else if(word.equalsIgnoreCase("process")){
						writeOutputProcess(data_output);
					}
					else{
						if(cont==nTokens-1)
							data_output.writeBytes(word+"\n");
						else
							data_output.writeBytes(word);
					}
				}
				if(nTokens==0) //white line
					data_output.writeBytes("\n");
				n_lines++;
				line=buff.readLine();
			} //end while
			buff.close();
			data_output.close();
			inFile.close();
		}catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't write Hermes_outport.vhd","Output error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}catch(Exception e){
			JOptionPane.showMessageDialog(null,"Can't write Hermes_outport.vhd\n"+e.getMessage(),"Output error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}

	/**
	 * Write signal c for all virtual channels.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeOutputSignal(DataOutputStream data_output) throws Exception{
		data_output.writeBytes("signal ");
		for(int i=0;i<(nChannels-1);i++)
			data_output.writeBytes("c"+(i+1)+", ");
		data_output.writeBytes("c"+nChannels+" : std_logic := '0';\n");
	}
	
	/**
	 * Write test C signal in the Local port.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeOutputCS_Local(DataOutputStream data_output) throws Exception{
		int initialPort = 0;
		int finalPort = 3;
		for(int i=0;i<nChannels;i++){
			data_output.writeBytes("\tc"+(i+1)+" <= '1' when free(L"+(i+1)+")='0' and credit_i(L"+(i+1)+")='1' and\n");
			for(int j=0;j<nChannels;j++){
				writeOutputTestTableOut(data_output, i, j, "EAST", 0, initialPort, finalPort);
			}
			for(int j=0;j<nChannels;j++){
				writeOutputTestTableOut(data_output, i, j, "WEST", 1, initialPort, finalPort);
			}
			for(int j=0;j<nChannels;j++){
				writeOutputTestTableOut(data_output, i, j, "NORTH", 2, initialPort, finalPort);
			}
			for(int j=0;j<nChannels;j++){
				writeOutputTestTableOut(data_output, i, j, "SOUTH", 3, initialPort, finalPort);
			}
			data_output.writeBytes("\t\t\t'0';\n\n");
		}
	}	
	
	/**
	 * Write test C signal in the EAST port.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeOutputCS_East(DataOutputStream data_output) throws Exception{
		int initialPort = 1;
		int finalPort = 4;
		for(int i=0;i<nChannels;i++){
			data_output.writeBytes("\tc"+(i+1)+" <= '1' when free(L"+(i+1)+")='0' and credit_i(L"+(i+1)+")='1' and\n");
			for(int j=0;j<nChannels;j++){
				writeOutputTestTableOut(data_output, i, j, "WEST", 1, initialPort, finalPort);
			}
			for(int j=0;j<nChannels;j++){
				writeOutputTestTableOut(data_output, i, j, "LOCAL", 4, initialPort, finalPort);
			}
			data_output.writeBytes("\t\t\t'0';\n\n");
		}
	}	
	
	/**
	 * Write test C signal in the WEST port.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeOutputCS_West(DataOutputStream data_output) throws Exception{
		int initialPort = 0;
		int finalPort = 4;
		
		for(int i=0;i<nChannels;i++){
			data_output.writeBytes("\tc"+(i+1)+" <= '1' when free(L"+(i+1)+")='0' and credit_i(L"+(i+1)+")='1' and\n");
			for(int j=0;j<nChannels;j++){
				writeOutputTestTableOut(data_output, i, j, "EAST", 0, initialPort, finalPort);
			}
			for(int j=0;j<nChannels;j++){
				writeOutputTestTableOut(data_output, i, j, "LOCAL", 4, initialPort, finalPort);
			}
			data_output.writeBytes("\t\t\t'0';\n\n");
		}

	}

	/**
	 * Write test C signal in the NORTH port.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeOutputCS_North(DataOutputStream data_output) throws Exception{
		int initialPort = 0;
		int finalPort = 4;
		
		for(int i=0;i<nChannels;i++){
			data_output.writeBytes("\tc"+(i+1)+" <= '1' when free(L"+(i+1)+")='0' and credit_i(L"+(i+1)+")='1' and\n");
			for(int j=0;j<nChannels;j++){
				writeOutputTestTableOut(data_output, i, j, "EAST", 0, initialPort, finalPort);
			}
			for(int j=0;j<nChannels;j++){
				writeOutputTestTableOut(data_output, i, j, "WEST", 1, initialPort, finalPort);
			}
			for(int j=0;j<nChannels;j++){
				writeOutputTestTableOut(data_output, i, j, "SOUTH", 3, initialPort, finalPort);
			}
			for(int j=0;j<nChannels;j++){
				writeOutputTestTableOut(data_output, i, j, "LOCAL", 4, initialPort, finalPort);
			}
			data_output.writeBytes("\t\t\t'0';\n\n");
		}
	}

	/**
	 * Write test C signal in the SOUTH port.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeOutputCS_South(DataOutputStream data_output) throws Exception{
		int initialPort = 0;
		int finalPort = 4;
		for(int i=0;i<nChannels;i++){
			data_output.writeBytes("\tc"+(i+1)+" <= '1' when free(L"+(i+1)+")='0' and credit_i(L"+(i+1)+")='1' and\n");
			for(int j=0;j<nChannels;j++){
				writeOutputTestTableOut(data_output, i, j, "EAST", 0, initialPort, finalPort);
			}
			for(int j=0;j<nChannels;j++){
				writeOutputTestTableOut(data_output, i, j, "WEST", 1, initialPort, finalPort);
			}
			for(int j=0;j<nChannels;j++){
				writeOutputTestTableOut(data_output, i, j, "NORTH", 2, initialPort, finalPort);
			}
			for(int j=0;j<nChannels;j++){
				writeOutputTestTableOut(data_output, i, j, "LOCAL", 4, initialPort, finalPort);
			}
			data_output.writeBytes("\t\t\t'0';\n\n");
		}
	}	
		
	/**
	 * Write test if there is a packet in a specific channel of a specific port.
	 * @param data_output
	 * @param lane1 The number of virtual channel of tableOut signal. 
	 * @param lane2 The number of virtual channel of data_av signal.
	 * @param portName The specific port name. For instance: LOCAL.
	 * @param port The number of the specific port. For instance: LOCAL = 4.
	 * @param initialPort The initial port informs when open (.
	 * @param finalPort The initial port informs when close ).
	 * @throws Exception 
	 */
	public void writeOutputTestTableOut(DataOutputStream data_output, int lane1, int lane2, String portName, int port, int initialPort, int finalPort) throws Exception{
		if(port==initialPort && lane2==0) // if initial port and first virtual channel
			data_output.writeBytes("\t\t\t(");
		else
			data_output.writeBytes("\t\t\t ");

		data_output.writeBytes("(tableOut(L"+(lane1+1)+")=x\""+port+""+lane2+"\" and data_av("+portName+")(L"+(lane2+1)+")='1')");

		if(port==finalPort && lane2==nChannels-1) //if final port and last virtual channel
			data_output.writeBytes(") else\n");
		else
			data_output.writeBytes(" or\n");

	}	

	/**
	 * Write test TX .
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeOutputTx(DataOutputStream data_output) throws Exception{
		data_output.writeBytes("\ttx <= '1' when (");
		for(int i=0;i<nChannels;i++){
			if(i!=nChannels-1)
				data_output.writeBytes("c"+(i+1)+"='1' or ");
			else
				data_output.writeBytes("c"+(i+1)+"='1') else '0';\n");
		}
	}	
	
	/**
	 * Write test DataOut in the LOCAL port.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeOutputDataOut_Local(DataOutputStream data_output) throws Exception{
		int initialPort = 0;
		data_output.writeBytes("\tdata_out <= ");
		writeOutputTestDataOut(data_output, "EAST",  0, initialPort);
		writeOutputTestDataOut(data_output, "WEST",  1, initialPort);
		writeOutputTestDataOut(data_output, "NORTH", 2, initialPort);
		writeOutputTestDataOut(data_output, "SOUTH", 3, initialPort);
		data_output.writeBytes("\t\t\t(others=>'0');\n");
	}

	/**
	 * Write test DataOut in the EAST port.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeOutputDataOut_East(DataOutputStream data_output) throws Exception{
		int initialPort = 1;
		data_output.writeBytes("\tdata_out <= ");
		writeOutputTestDataOut(data_output, "WEST",  1, initialPort);
		writeOutputTestDataOut(data_output, "LOCAL", 4, initialPort);
		data_output.writeBytes("\t\t\t(others=>'0');\n");
	}	

	/**
	 * Write test DataOut in the WEST port.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeOutputDataOut_West(DataOutputStream data_output) throws Exception{
		int initialPort = 0;
		data_output.writeBytes("\tdata_out <= ");
		writeOutputTestDataOut(data_output, "EAST",  0, initialPort);
		writeOutputTestDataOut(data_output, "LOCAL", 4, initialPort);
		data_output.writeBytes("\t\t\t(others=>'0');\n");
	}	

	/**
	 * Write test DataOut in the NORTH port.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeOutputDataOut_North(DataOutputStream data_output) throws Exception{
		int initialPort = 0;
		data_output.writeBytes("\tdata_out <= ");
		writeOutputTestDataOut(data_output, "EAST",  0, initialPort);
		writeOutputTestDataOut(data_output, "WEST",  1, initialPort);
		writeOutputTestDataOut(data_output, "SOUTH", 3, initialPort);
		writeOutputTestDataOut(data_output, "LOCAL", 4, initialPort);
		data_output.writeBytes("\t\t\t(others=>'0');\n");
	}
	
	/**
	 * Write test DataOut in the SOUTH port.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeOutputDataOut_South(DataOutputStream data_output) throws Exception{
		int initialPort = 0;
		data_output.writeBytes("\tdata_out <= ");
		writeOutputTestDataOut(data_output, "EAST",  0, initialPort);
		writeOutputTestDataOut(data_output, "WEST",  1, initialPort);
		writeOutputTestDataOut(data_output, "NORTH", 2, initialPort);
		writeOutputTestDataOut(data_output, "LOCAL", 4, initialPort);
		data_output.writeBytes("\t\t\t(others=>'0');\n");
	}
	
	/**
	 * Write test if there is a packet in a specific channel of a specific port.
	 * @param data_output
	 * @param portName The specific port name. For instance: LOCAL.
	 * @param port The number of the specific port. For instance: LOCAL = 4.
	 * @param initialPort The initial port informs when open (.
	 * @throws Exception 
	 */
	public void writeOutputTestDataOut(DataOutputStream data_output, String portName, int port, int initialPort) throws Exception{
		for(int i=0;i<nChannels;i++){
			if(!(port==initialPort && i==0)) //does not write if it is a initial port and first virtual channel 
				data_output.writeBytes("\t\t\t");
		
			data_output.writeBytes("data("+portName+")(L"+(i+1)+") when indice=x\""+port+""+i+"\" and (");
			for(int j=0;j<nChannels;j++){
				if(j==nChannels-1)
					data_output.writeBytes("c"+(j+1)+"='1'");
				else
					data_output.writeBytes("c"+(j+1)+"='1' or ");
			}
			data_output.writeBytes(") else\n");
		}
	}	
	
	/**
	 * Write test DataAck in the LOCAL port.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeOutputDataAck_Local(DataOutputStream data_output) throws Exception{
		int initialPort = 0;
		String portName = "LOCAL";
		for(int i=0;i<nChannels;i++){
			data_output.writeBytes("\tdata_ack(L"+(i+1)+") <= ");
			writeOutputTestAllLaneTx(data_output, i, "EAST",  0, portName, initialPort);
			writeOutputTestAllLaneTx(data_output, i, "WEST",  1, portName, initialPort);
			writeOutputTestAllLaneTx(data_output, i, "NORTH", 2, portName, initialPort);
			writeOutputTestAllLaneTx(data_output, i, "SOUTH", 3, portName, initialPort);
			data_output.writeBytes("\t\t\t'0';\n\n");
		}
	}

	/**
	 * Write test DataAck in the EAST port.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeOutputDataAck_East(DataOutputStream data_output) throws Exception{
		int initialPort = 1;
		String portName = "EAST";
		for(int i=0;i<nChannels;i++){
			data_output.writeBytes("\tdata_ack(L"+(i+1)+") <= ");
			writeOutputTestAllLaneTx(data_output, i, "WEST",  1, portName, initialPort);
			writeOutputTestAllLaneTx(data_output, i, "NORTH", 2, portName, initialPort);
			writeOutputTestAllLaneTx(data_output, i, "SOUTH", 3, portName, initialPort);
			writeOutputTestAllLaneTx(data_output, i, "LOCAL", 4, portName, initialPort);
			data_output.writeBytes("\t\t\t'0';\n\n");
		}
	}
	
	/**
	 * Write test DataAck in the WEST port.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeOutputDataAck_West(DataOutputStream data_output) throws Exception{
		int initialPort = 0;
		String portName = "WEST";
		for(int i=0;i<nChannels;i++){
			data_output.writeBytes("\tdata_ack(L"+(i+1)+") <= ");
			writeOutputTestAllLaneTx(data_output, i, "EAST",  0, portName, initialPort);
			writeOutputTestAllLaneTx(data_output, i, "NORTH", 2, portName, initialPort);
			writeOutputTestAllLaneTx(data_output, i, "SOUTH", 3, portName, initialPort);
			writeOutputTestAllLaneTx(data_output, i, "LOCAL", 4, portName, initialPort);
			data_output.writeBytes("\t\t\t'0';\n\n");
		}
	}	
	
	/**
	 * Write test DataAck in the NORTH port.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeOutputDataAck_North(DataOutputStream data_output) throws Exception{
		int initialPort = 3;
		String portName = "NORTH";
		for(int i=0;i<nChannels;i++){
			data_output.writeBytes("\tdata_ack(L"+(i+1)+") <= ");
			writeOutputTestAllLaneTx(data_output, i, "SOUTH", 3, portName, initialPort);
			writeOutputTestAllLaneTx(data_output, i, "LOCAL", 4, portName, initialPort);
			data_output.writeBytes("\t\t\t'0';\n\n");
		}
	}	

	/**
	 * Write test DataAck in the SOUTH port.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeOutputDataAck_South(DataOutputStream data_output) throws Exception{
		int initialPort = 2;
		String portName = "SOUTH";
		for(int i=0;i<nChannels;i++){
			data_output.writeBytes("\tdata_ack(L"+(i+1)+") <= ");
			writeOutputTestAllLaneTx(data_output, i, "NORTH", 2, portName, initialPort);
			writeOutputTestAllLaneTx(data_output, i, "LOCAL", 4, portName, initialPort);
			data_output.writeBytes("\t\t\t'0';\n\n");
		}
	}	
	
	/**
	 * Write test all_lane_tx signal.
	 * @param data_output
	 * @param lane The number of virtual channel. 
	 * @param portName1 The port name of all_lane_tx signal. For instance: LOCAL.
	 * @param port1 The number of port1. For instance: LOCAL = 4.
	 * @param portName2 The port name of all_lane_tx signal. For instance: LOCAL.
	 * @param initialPort The initial port informs when open (.
	 * @throws Exception 
	 */
	public void writeOutputTestAllLaneTx(DataOutputStream data_output, int lane, String portName1, int port1, String portName2, int initialPort) throws Exception{
		for(int j=0;j<nChannels;j++){
			if(!(port1==initialPort && j==0))  //does not write if it is a initial port and first virtual channel
				data_output.writeBytes("\t\t\t");
			data_output.writeBytes("all_lane_tx("+portName1+")(L"+(j+1)+") when tableIn(L"+(lane+1)+")=x\""+port1+""+j+"\" and data_av("+portName2+")(L"+(lane+1)+")='1' else\n");
		}

	}	
	
	/**
	 * Write process.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeOutputProcess(DataOutputStream data_output) throws Exception{
		data_output.writeBytes("\tprocess(last_lane_tx,");
		for(int i=0;i<nChannels;i++){
			if(i!=nChannels-1)
				data_output.writeBytes("c"+(i+1)+",");
			else
				data_output.writeBytes("c"+(i+1)+")\n");
		}
		
		data_output.writeBytes("\tbegin\n");

		writeOutputCaseLastLaneTx(data_output);

		data_output.writeBytes("\tend process;\n");
	}

	/**
	 * Write case last_lane_tx signal.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeOutputCaseLastLaneTx(DataOutputStream data_output) throws Exception{
		data_output.writeBytes("\t\tcase last_lane_tx is\n");
		for(int i=0;i<nChannels;i++){
			if(i!=nChannels-1){
				data_output.writeBytes("\t\twhen \""+Convert.decToBin((int)Math.pow(2,i),nChannels)+"\" =>\n");
				for(int k=i+1;k<nChannels;k++){
					writeOutputTestC(data_output, k, (k==i+1));
				}
				for(int k=0;k<i+1;k++){
					writeOutputTestC(data_output, k, (i+1>=nChannels));
				}
				data_output.writeBytes("\t\t\telse aux_lane_tx<=\""+Convert.decToBin(0,nChannels)+"\"; end if;\n");
			}
			else{
				data_output.writeBytes("\t\twhen \""+Convert.decToBin(0,nChannels)+"\" | \""+Convert.decToBin((int)Math.pow(2,i),nChannels)+"\" =>\n");
				for(int k=0;k<i+1;k++){
					writeOutputTestC(data_output, k, (k==0));
				}
				data_output.writeBytes("\t\t\telse aux_lane_tx<=\""+Convert.decToBin(0,nChannels)+"\"; end if;\n");
			}
		}
		data_output.writeBytes("\t\twhen others => aux_lane_tx<=\""+Convert.decToBin(0,nChannels)+"\";\n");
		data_output.writeBytes("\t\tend case;\n");
	}
	
	/**
	 * Write test C signal in a specific lane.
	 * @param data_output 
	 * @param lane The virtual channel.
	 * @param isIF True if is a IF conditional. False if is a ELSIF conditional.
	 * @throws Exception 
	 */
	public void writeOutputTestC(DataOutputStream data_output, int lane, boolean isIF) throws Exception{
		if(isIF)
			data_output.writeBytes("\t\t\t");
		else			
			data_output.writeBytes("\t\t\tels");

		data_output.writeBytes("if c"+(lane+1)+"='1' then aux_lane_tx<=\""+Convert.decToBin((int)Math.pow(2,lane),nChannels)+"\"; indice <= tableOut(L"+(lane+1)+");\n");
	}
	
/*********************************************************************************
* SWITCHCONTROL
*********************************************************************************/

	/**
	 * Create the Hermes_switchcontrol.vhdl file, replacing the flags.
	 */
	public void createSwitchcontrol(){
		StringTokenizer st;
		String line, word;

		try{
			FileInputStream inFile = new FileInputStream(sourceDir + "Hermes_switchcontrol.vhd");
			BufferedReader buff = new BufferedReader(new InputStreamReader(inFile));

			DataOutputStream data_output=new DataOutputStream(new FileOutputStream(nocDir + "Hermes_switchcontrol.vhd"));

			int n_lines=0;
			line=buff.readLine();
			while(line!=null){
				st = new StringTokenizer(line, "$");
				int nTokens = st.countTokens();
				for (int cont=0; cont<nTokens; cont++){
					word = st.nextToken();

					if(word.equalsIgnoreCase("state")){
						writeHSCType(data_output);
					}
					else if(word.equalsIgnoreCase("ask")){
						writeHSCAsk(data_output);
					}
					else if(word.equalsIgnoreCase("localSel")){
						writeHSCLocalSet(data_output);
					}
					else if(word.equalsIgnoreCase("eastSel")){
						writeHSCEastSet(data_output);
					}
					else if(word.equalsIgnoreCase("westSel")){
						writeHSCWestSet(data_output);
					}
					else if(word.equalsIgnoreCase("northSel")){
						writeHSCNorthSet(data_output);
					}
					else if(word.equalsIgnoreCase("southSel")){
						writeHSCSouthSet(data_output);
					}
					else if(word.equalsIgnoreCase("statesConditions")){
						writeHSCStatesConditions(data_output);
					}
					else if(word.equalsIgnoreCase("statesFinal")){
						writeHSCStatesFinal(data_output);
					}
					else if(word.equalsIgnoreCase("state_s2")){
						writeHSCStateS2(data_output);
					}
					else if(word.equalsIgnoreCase("statesImplements")){
						writeHSCStatesImplements(data_output);
					}
					else if(word.equalsIgnoreCase("sender_ant")){
						writeHSCSenderAnt(data_output);
					}
					else if(word.equalsIgnoreCase("senderConditions")){
						writeHSCSenderConditions(data_output);
					}
					else{
						if(cont==nTokens-1)
							data_output.writeBytes(word+"\n");
						else
							data_output.writeBytes(word);
					}
				}
				if(nTokens==0) //white line
					data_output.writeBytes("\n");
				n_lines++;
				line=buff.readLine();
			} //end while
			buff.close();
			data_output.close();
			inFile.close();
		}catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't write Hermes_outport.vhd","Output error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}catch(Exception e){
			JOptionPane.showMessageDialog(null,"Can't write Hermes_outport.vhd\n"+e.getMessage(),"Error Message", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}
	
	/**
	 * Write type state in Hermes_switchcontrol file.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeHSCType(DataOutputStream data_output) throws Exception{
		data_output.writeBytes("type state is (");
		int nStates = 5 + (3 * nChannels);
		for(int i=0;i<nStates;i++){
			if(i!=(nStates-1))
				data_output.writeBytes("S"+i+", ");
			else
				data_output.writeBytes("S"+i+");\n");
		}
	}

	/**
	 * Write ask in Hermes_switchcontrol file.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeHSCAsk(DataOutputStream data_output) throws Exception{
		data_output.writeBytes("\task <= '1' when ");
		writeHSCPortAsk(data_output, "LOCAL", 4);
		writeHSCPortAsk(data_output, "EAST",  0);
		writeHSCPortAsk(data_output, "WEST",  1);
		writeHSCPortAsk(data_output, "NORTH", 2);
		writeHSCPortAsk(data_output, "SOUTH", 3);
	}	
	
	/**
	 * Write ask signal of a specific port.
	 * @param data_output
	 * @param portName The port name. For Instance: LOCAL.
	 * @param port The number of port. For Instance: LOCAL =4.
	 * @throws Exception 
	 */
	public void writeHSCPortAsk(DataOutputStream data_output, String portName, int port) throws Exception{
		for(int i=0;i<nChannels;i++){
			data_output.writeBytes("h("+portName+")(L"+(i+1)+")='1'");

			if(i!=(nChannels-1))
				data_output.writeBytes(" or ");
			else if(port == 3) // if SOUTH port
				data_output.writeBytes(" else '0';\n");
			else
				data_output.writeBytes(" or\n\t\t\t\t\t");
		}
	}	
	
	/**
	 * Write prox signal in the LOCAL port.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeHSCLocalSet(DataOutputStream data_output) throws Exception{
		int initialPort = 0;
		int finalPort = 4;
		writeHSCPortProx(data_output, "EAST",  0, initialPort, finalPort);
		writeHSCPortProx(data_output, "WEST",  1, initialPort, finalPort);
		writeHSCPortProx(data_output, "NORTH", 2, initialPort, finalPort);
		writeHSCPortProx(data_output, "SOUTH", 3, initialPort, finalPort);
		writeHSCPortProx(data_output, "LOCAL", 4, initialPort, finalPort);
	}
	
	/**
	 * Write prox signal in the EAST port.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeHSCEastSet(DataOutputStream data_output) throws Exception{
		int initialPort = 1;
		int finalPort = 0;
		writeHSCPortProx(data_output, "WEST",  1, initialPort, finalPort);
		writeHSCPortProx(data_output, "NORTH", 2, initialPort, finalPort);
		writeHSCPortProx(data_output, "SOUTH", 3, initialPort, finalPort);
		writeHSCPortProx(data_output, "LOCAL", 4, initialPort, finalPort);
		writeHSCPortProx(data_output, "EAST",  0, initialPort, finalPort);
	}	

	/**
	 * Write prox signal in the WEST port.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeHSCWestSet(DataOutputStream data_output) throws Exception{
		int initialPort = 2;
		int finalPort = 1;
		writeHSCPortProx(data_output, "NORTH", 2, initialPort, finalPort);
		writeHSCPortProx(data_output, "SOUTH", 3, initialPort, finalPort);
		writeHSCPortProx(data_output, "LOCAL", 4, initialPort, finalPort);
		writeHSCPortProx(data_output, "EAST",  0, initialPort, finalPort);
		writeHSCPortProx(data_output, "WEST",  1, initialPort, finalPort);
	}	

	/**
	 * Write prox signal in the NORTH port.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeHSCNorthSet(DataOutputStream data_output) throws Exception{
		int initialPort = 3;
		int finalPort = 2;
		writeHSCPortProx(data_output, "SOUTH", 3, initialPort, finalPort);
		writeHSCPortProx(data_output, "LOCAL", 4, initialPort, finalPort);
		writeHSCPortProx(data_output, "EAST",  0, initialPort, finalPort);
		writeHSCPortProx(data_output, "WEST",  1, initialPort, finalPort);
		writeHSCPortProx(data_output, "NORTH", 2, initialPort, finalPort);
	}
	
	/**
	 * Write prox signal in the SOUTH port.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeHSCSouthSet(DataOutputStream data_output) throws Exception{
		int initialPort = 4;
		int finalPort = 3;
		writeHSCPortProx(data_output, "LOCAL", 4, initialPort, finalPort);
		writeHSCPortProx(data_output, "EAST",  0, initialPort, finalPort);
		writeHSCPortProx(data_output, "WEST",  1, initialPort, finalPort);
		writeHSCPortProx(data_output, "NORTH", 2, initialPort, finalPort);
		writeHSCPortProx(data_output, "SOUTH", 3, initialPort, finalPort);
	}	

	/**
	 * Write prox signal of a specific port.
	 * @param data_output
	 * @param portName The port name. For Instance: LOCAL.
	 * @param port The number of port. For Instance: LOCAL = 4.
	 * @param initialPort The number of initial port. For Instance: EAST = 0.
	 * @param finalPort The number of final port. For Instance: SOUTH = 3.
	 * @throws Exception 
	 */
	public void writeHSCPortProx(DataOutputStream data_output, String portName, int port, int initialPort, int finalPort) throws Exception{
		if(port == finalPort)
			data_output.writeBytes("\t\t\t\telse prox<="+portName+"; end if;\n");
		else{
			if(port == initialPort)
				data_output.writeBytes("\t\t\t\t");
			else
				data_output.writeBytes("\t\t\t\tels");

			data_output.writeBytes("if h("+portName+")/=\"");
			for(int i=0;i<nChannels;i++)
				data_output.writeBytes("0");
			data_output.writeBytes("\" then prox<="+portName+";\n");
		}		
	}
	
	/**
	 * Write state condition in Hermes_switchcontrol file.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeHSCStatesConditions(DataOutputStream data_output) throws Exception{
		if(algorithm.equalsIgnoreCase("AlgorithmXY")){
			for(int i=0;i<nChannels;i++){
				if(i==0)
					data_output.writeBytes("\t\t\t\tif lx = tx and ly = ty and auxfree(LOCAL)(L"+(i+1)+")='1' then PES<=S"+(i+4)+";\n");
				else
					data_output.writeBytes("\t\t\t\telsif lx = tx and ly = ty and auxfree(LOCAL)(L"+(i+1)+")='1' then PES<=S"+(i+4)+";\n");
			}
			for(int i=0;i<nChannels;i++)
				data_output.writeBytes("\t\t\t\telsif lx /= tx and auxfree(dirx)(L"+(i+1)+")='1' then PES<=S"+(i+4+nChannels)+";\n");
			for(int i=0;i<nChannels;i++)
				data_output.writeBytes("\t\t\t\telsif lx = tx and ly /= ty and auxfree(diry)(L"+(i+1)+")='1' then PES<=S"+(i+4+(2*nChannels))+";\n");
		}
	}
		
	/**
	 * Write state condition in Hermes_switchcontrol file.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeHSCStatesFinal(DataOutputStream data_output) throws Exception{
		int state = 0;
		if(algorithm.equalsIgnoreCase("AlgorithmXY"))
			state = (4+3*nChannels);
		else if(algorithm.equalsIgnoreCase("AlgorithmWFM") || algorithm.equalsIgnoreCase("AlgorithmWFNM"))
			state = (5+4*nChannels);
		
		data_output.writeBytes("\t\t\twhen S"+state+" => PES<=S1;\n");
		data_output.writeBytes("\t\t\twhen others => PES<=S"+state+";\n");
	}
	
	/**
	 * Write state condition in Hermes_switchcontrol file.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeHSCStateS2(DataOutputStream data_output) throws Exception{
		for(int i=0;i<nChannels;i++){
			if(i==0)
				data_output.writeBytes("\t\t\t\tif h(prox)(L"+(i+1)+")='1' then sel_lane <= L"+(i+1)+";\n");
			else if(i<(nChannels-1))
				data_output.writeBytes("\t\t\t\telsif h(prox)(L"+(i+1)+")='1' then sel_lane <= L"+(i+1)+";\n");
			else
				data_output.writeBytes("\t\t\t\telse sel_lane <= L"+(i+1)+"; end if;\n");
		}
	}	

	/**
	 * Write state condition in Hermes_switchcontrol file.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeHSCStatesImplements(DataOutputStream data_output) throws Exception{
		if(algorithm.equalsIgnoreCase("AlgorithmXY")){
			writeHSCXYConnection(data_output, "LOCAL");
			writeHSCXYConnection(data_output, "dirx");
			writeHSCXYConnection(data_output, "diry");
		}
		else if(algorithm.equalsIgnoreCase("AlgorithmWFM") || algorithm.equalsIgnoreCase("AlgorithmWFNM")){
			writeHSCWFConnection(data_output, "LOCAL");
			writeHSCWFConnection(data_output, "WEST");
			writeHSCWFConnection(data_output, "EAST");
			writeHSCWFConnection(data_output, "NORTH");
			writeHSCWFConnection(data_output, "SOUTH");
		}
	}
	
	/**
	 * Write the XY connections for a specific port.
	 * @param data_output 
	 * @param portName The port name. In this case, EAST and WEST are named dirx and NORTH and SOUTH are named diry.
	 * @throws Exception 
	 */
	public void writeHSCXYConnection(DataOutputStream data_output, String portName) throws Exception{
		int state = 0;
		for(int i=0;i<nChannels;i++){
			if(portName.equalsIgnoreCase("dirx")){
				state = i+4+nChannels;
				data_output.writeBytes("\t\t\t-- Connection EAST or WEST port - Channel L"+(i+1)+"\n");
			}
			else if(portName.equalsIgnoreCase("diry")){
				state = i+4+2*nChannels;
				data_output.writeBytes("\t\t\t-- Connection NORTH or SOUTH port - Channel L"+(i+1)+"\n");
			}
			else{
				state = i+4;
				data_output.writeBytes("\t\t\t-- Connection "+portName+" port - Channel L"+(i+1)+"\n");
			}
			
			writeHSCPortConnection(data_output, portName, (i+1), state);
		}
	}
	
	/**
	 * Write the West First connections for a specific port.
	 * @param data_output 
	 * @param portName The port name. For instance: EAST.
	 * @throws Exception 
	 */
	public void writeHSCWFConnection(DataOutputStream data_output, String portName) throws Exception{
		int state = 0;

		for(int i=0;i<nChannels;i++){
			if(portName.equalsIgnoreCase("LOCAL"))
				state = i+4;
			else if(portName.equalsIgnoreCase("WEST"))
				state = i+4+nChannels;
			else if(portName.equalsIgnoreCase("EAST"))
				state = i+4+2*nChannels;
			else if(portName.equalsIgnoreCase("NORTH"))
				state = i+4+3*nChannels;
			else if(portName.equalsIgnoreCase("SOUTH"))
				state = i+4+4*nChannels;

			data_output.writeBytes("\t\t\t-- Connection "+portName+" port - Channel L"+(i+1)+"\n");
			writeHSCPortConnection(data_output, portName, (i+1), state);
		}
	}	

	/**
	 * Write the connections for a specific port and virtual channel.
	 * @param data_output 
	 * @param portName The port name. In this case, EAST and WEST are named dirx and NORTH and SOUTH are named diry.
	 * @param lane The number of virtual channel used in this connection.
	 * @param state The state number in the FSM.
	 * @throws Exception 
	 */
	public void writeHSCPortConnection(DataOutputStream data_output, String portName, int lane, int state) throws Exception{
		data_output.writeBytes("\t\t\twhen S"+state+" =>\n");
		data_output.writeBytes("\t\t\t\tsource(sel)(sel_lane) <= CONV_STD_LOGIC_VECTOR("+portName+",4) & CONV_STD_LOGIC_VECTOR(L"+lane+",4);\n");
		data_output.writeBytes("\t\t\t\tmux_out("+portName+")(L"+lane+") <= CONV_STD_LOGIC_VECTOR(sel,4) & CONV_STD_LOGIC_VECTOR(sel_lane,4);\n");
		data_output.writeBytes("\t\t\t\tauxfree("+portName+")(L"+lane+") <= '0';\n");
		data_output.writeBytes("\t\t\t\tack_h(sel)(sel_lane)<='1';\n");
	}

	/**
	 * Write sender_ant signal in Hermes_switchcontrol file.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeHSCSenderAnt(DataOutputStream data_output) throws Exception{
		writeHSCPortSenderAnt(data_output, "LOCAL");
		writeHSCPortSenderAnt(data_output, "EAST");
		writeHSCPortSenderAnt(data_output, "WEST");
		writeHSCPortSenderAnt(data_output, "NORTH");
		writeHSCPortSenderAnt(data_output, "SOUTH");
	}
	
	/**
	 * Write sender_ant signal for a specific port.
	 * @param data_output 
	 * @param portName The port name. For instance: EAST.
	 * @throws Exception 
	 */
	public void writeHSCPortSenderAnt(DataOutputStream data_output, String portName) throws Exception{
		for(int i=0;i<nChannels;i++)
			data_output.writeBytes("\t\tsender_ant("+portName+")(L"+(i+1)+") <= sender("+portName+")(L"+(i+1)+");\n");
	}	

	/**
	 * Write sender conditions in Hermes_switchcontrol file.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeHSCSenderConditions(DataOutputStream data_output) throws Exception{
		writeHSCPortSenderConditions(data_output, "LOCAL");
		writeHSCPortSenderConditions(data_output, "EAST");
		writeHSCPortSenderConditions(data_output, "WEST");
		writeHSCPortSenderConditions(data_output, "NORTH");
		writeHSCPortSenderConditions(data_output, "SOUTH");
	}	

	/**
	 * Write sender conditions for a specific port.
	 * @param data_output 
	 * @param portName The port name. For instance: EAST.
	 * @throws Exception 
	 */
	public void writeHSCPortSenderConditions(DataOutputStream data_output, String portName) throws Exception{
		for(int i=0;i<nChannels;i++){
			data_output.writeBytes(
					"\t\tif sender("+portName+")(L"+(i+1)+")='0' and " +
					"sender_ant("+portName+")(L"+(i+1)+")='1' then " +
					"auxfree(CONV_INTEGER(source("+portName+")(L"+(i+1)+")(7 downto 4)))" +
					"(CONV_INTEGER(source("+portName+")(L"+(i+1)+")(3 downto 0))) <='1'; " +
					"end if;\n"
			);
		}
	}
	
/*********************************************************************************
* ROUTER
*********************************************************************************/

	/**
	 * Create the NoC routers.
	 */
	public void createRouters(){
		int m = dimX;
		int n = dimY;

		if (m>3) m = 3;
		if (n>3) n = 3;
		for(int y =0; y < n; y++){
			for(int x =0; x < m; x++){
				vectorSwitch.addElement(createRouter(x,y,m,n));
			}
		}
	}

	/**
	 * Create a router.
	 * @param x The router address in X-dimension of NoC.
	 * @param y The router address in Y-dimension of NoC.
	 * @param dimX The X-dimension of NoC.
	 * @param dimY The Y-dimension of NoC.
	 * @return The code corresponding to a router.
	 */
	public String createRouter(int x, int y,int dimX,int dimY){
		String line, word;
		StringTokenizer st;
		String routerType = Router.getRouterType(x,y,dimX,dimY);

		try{
			FileInputStream inFile = new FileInputStream(sourceDir +"Hermes_router.vhd");
			BufferedReader buff=new BufferedReader(new InputStreamReader(inFile));

			DataOutputStream data_output=new DataOutputStream(new FileOutputStream(nocDir + routerType+".vhd"));

			line=buff.readLine();
			while(line!=null){
				st = new StringTokenizer(line, "$");
				int vem = st.countTokens();
				for (int cont=0; cont<vem; cont++){
					word = st.nextToken();
					if(word.equalsIgnoreCase("Chave"))
						word = routerType;
					else if(word.equalsIgnoreCase("algorithm"))
						word = algorithm;
					else if(word.equalsIgnoreCase("inports")){
						word = getRoutersInport(routerType);
					}
					else if (word.equalsIgnoreCase("outports")){
						word = getRoutersOutport(routerType);
					}
					data_output.writeBytes(word);
				}//end for
				data_output.writeBytes("\r\n");
				line=buff.readLine();
			} //end while
			buff.close();
			data_output.close();
			inFile.close();
		}catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't write "+routerType+".vdh","Output error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}catch(Exception e){}
		return routerType;
	}
	
	/**
	 * Write the input port map for a specific router.
	 * @param routerType The router type determine if a port is used or not.
	 * @return A String containing the input port map for a specific router.
	 */
	public String getRoutersInport(String routerType) {
		String word = "", portName;
		for(int port=0; port<Router.NPORTS; port++){
			if(Router.hasPort(routerType, port)){
				portName = Router.getPortName(port, 0); // 0 is lower case 
				word = word + getInportPortMap(portName,port);
			}
			else{
				word = word + getNullInport(port);
			}
		}
		return word;
	}

	/**
	* Return the input port map for the informed router and port.
	* @param routerName The router name.
	* @param port The number of port.
	* @return The input port map for the informed router and port
	*/
	public String getInportPortMap(String routerName,int port){
		return ("\n\tRouter_"+routerName.toUpperCase()+" : Entity work.Hermes_inport(Hermes_inport)\n" +
				"\tport map(\n" +
				"\t\tclock    => clock,\n" +
				"\t\treset    => reset,\n\t\tclock_rx => clock_rx("+port+"),\n" +
				"\t\trx       => rx("+port+"),\n" +
				"\t\tlane_rx  => lane_rx("+port+"),\n" +
				"\t\tdata_in  => data_in("+port+"),\n" +
				"\t\tcredit_o => credit_o("+port+"),\n" +
				"\t\th        => h("+port+"),\n" +
				"\t\tack_h    => ack_h("+port+"),\n" +
				"\t\tdata_av  => data_av("+port+"),\n" +
				"\t\tdata     => data("+port+"),\n" +
				"\t\tdata_ack => data_ack("+port+"),\n" +
				"\t\tsender   => sender("+port+"));\n");
	}

	/**
	* Connect zeros to the removed input port.
	* @param port The number of removed input port.
	* @return The instance of removed input port.
	*/
	public String getNullInport(int port){
		return ("\n\t--Connecting zeros to the removed input port "+port+"\n" +
				"\th("+port+")<=(others=>'0');\n" +
				"\tdata_av("+port+")<=(others=>'0');\n" +
				"\tdata("+port+")<=(others=>(others=>'0'));\n" +
				"\tsender("+port+")<=(others=>'0');\n" +
				"\tcredit_o("+port+")<=(others=>'0');\n");
	}
	
	/**
	 * Write the output port map for a specific router.
	 * @param routerType The router type determine if a port is used or not.
	 * @return A String containing the input port map for a specific router.
	 */
	public String getRoutersOutport(String routerType) {
		String word = "", portName;
		for(int port=0; port<Router.NPORTS; port++){
			if(Router.hasPort(routerType, port)){
				portName = Router.getPortName(port, 0); // 0 is lower case 
				word = word + getOutportPortMap(portName, port);
			}
			else{
				word = word + getNullOutport(port);
			}
		}
		return word;
	}
	
	/**
	* Return the output port map for a specific router and port.
	* @param routerName The router name.
	* @param port The number of router port.
	* @return The output port  map for a specific router and port.
	*/
	public String getOutportPortMap(String routerName,int port){
		return ("\n\tOP_"+routerName.toUpperCase()+" : Entity work.Hermes_outport(Hermes_outport_"+routerName+")\n" +
				"\tport map(\n" +
				"\t\tclock       => clock,\n" +
				"\t\treset       => reset,\n" +
				"\t\tdata_av     => data_av,\n" +
				"\t\tdata        => data,\n" +
				"\t\tdata_ack    => data_ack("+port+"),\n" +
				"\t\tfree        => free("+port+"),\n" +
				"\t\tall_lane_tx => aux_lane_tx,\n" +
				"\t\ttableIn     => tableIn("+port+"),\n" +
				"\t\ttableOut    => tableOut("+port+"),\n" +
				"\t\tclock_tx    => clock_tx("+port+"),\n" +
				"\t\ttx          => tx("+port+"),\n" +
				"\t\tlane_tx     => aux_lane_tx("+port+"),\n" +
				"\t\tdata_out    => data_out("+port+"),\n" +
				"\t\tcredit_i    => credit_i("+port+"));\n");
	}

	/**
	* Connect zeros to the removed output port.
	* @param port The number of removed output port.
	* @return The instance of removed output port.
	*/
	public String getNullOutport(int port){
		return ("\n\t--connecting zeros to the removed output port "+port+"\n" +
				"\tdata_ack("+port+") <= (others=>'0');\n" +
				"\tclock_tx("+port+") <= clock;\n" +
				"\ttx("+port+") <= '0';\n" +
				"\taux_lane_tx("+port+") <= (others=>'0');\n" +
				"\tdata_out("+port+") <= (others=>'0');\n");
	}
		
/*********************************************************************************
* NOC
*********************************************************************************/

	/**
	 * Create the NoC VHDL file using the default package (HermesPackage).
	 */
	public void createNoC(){
		createNoC("HermesPackage");
	}
	
	/**
	 * Create the NoC VHDL file using the informed package.
	 * @param packageName The package name.
	 */
	public void createNoC(String packageName){
		try{
			DataOutputStream data_output=new DataOutputStream(new FileOutputStream(nocDir + "NOC.vhd"));

			//generate the libraries
			writeNoCLibraries(data_output,packageName);

			//generate the NoC entity
			writeNoCEntity(data_output);

			//generate architecture
			data_output.writeBytes("architecture NOC of NOC is\n\n");
			
			//generate the signals
			writeAllSignals(data_output);

			data_output.writeBytes("begin\n\n");

			//generate routers port map
			writeAllRoutersPortMap(data_output) ;

			//generate all routers connections
			writeAllConnections(data_output);
			
			//If SC option is selected then generate SC entity
			if(isSC){
				data_output.writeBytes(SCOutputModuleRouter.getPortMap(nocType, NoC.VC, dimX, dimY, flitSize, nChannels));
			}
			
			data_output.writeBytes("end NOC;\n");
			data_output.close();
		}//end try
		catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't write Noc.vhd\n" + f.getMessage(),"Output error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}catch(Exception e){
			JOptionPane.showMessageDialog(null,e.getMessage(), "Error Message", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}

	/**
	 * write NoC libraries using the default package (HermesPackage).
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeNoCLibraries(DataOutputStream data_output) throws Exception{
		writeNoCLibraries(data_output, "HermesPackage");
	}
	
	/**
	 * write NoC libraries using the informed package.
	 * @param data_output 
	 * @param packageName The package name.
	 * @throws Exception 
	 */
	public void writeNoCLibraries(DataOutputStream data_output, String packageName) throws Exception{
		data_output.writeBytes("library IEEE;\nuse IEEE.std_logic_1164.all;\nuse IEEE.std_logic_unsigned.all;\n");
		data_output.writeBytes("use work."+packageName+".all;\n\n");
	}
	
	/**
	 * write NoC entity.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeNoCEntity(DataOutputStream data_output) throws Exception{
		data_output.writeBytes("entity NOC is\n");
		data_output.writeBytes("port(\n");
		data_output.writeBytes("\tclock         : in  regNrot;\n");
		data_output.writeBytes("\treset         : in  std_logic;\n");
		data_output.writeBytes("\tclock_rxLocal : in  regNrot;\n");
		data_output.writeBytes("\trxLocal       : in  regNrot;\n");
		data_output.writeBytes("\tlane_rxLocal  : in  arrayNrot_regNlane;\n");
		data_output.writeBytes("\tdata_inLocal  : in  arrayNrot_regflit;\n");
		data_output.writeBytes("\tcredit_oLocal : out arrayNrot_regNlane;\n");
		data_output.writeBytes("\tclock_txLocal : out regNrot;\n");
		data_output.writeBytes("\ttxLocal       : out regNrot;\n");
		data_output.writeBytes("\tlane_txLocal  : out arrayNrot_regNlane;\n");
		data_output.writeBytes("\tdata_outLocal : out arrayNrot_regflit;\n");
		data_output.writeBytes("\tcredit_iLocal : in  arrayNrot_regNlane);\n");
		data_output.writeBytes("end NOC;\n\n");
	}
	
	/**
	 * Instance all NoC signals.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeAllSignals(DataOutputStream data_output) throws Exception{
		String yHexa;
		for (int y=0; y<dimY; y++){
			yHexa = Convert.decToHex(y,(flitSize/8));
			writeSignals(data_output, yHexa);
		}
	}

	/**
	 * Write the port map for all routers.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeAllRoutersPortMap(DataOutputStream data_output) throws Exception{
		String xHexa, yHexa, routerName;
		for (int y=0; y<dimY; y++){
			yHexa = Convert.decToHex(y,(flitSize/8));
			for (int x=0; x<dimX; x++){
				xHexa = Convert.decToHex(x,(flitSize/8));
				routerName = Router.getRouterType(x,y,dimX,dimY);
				writeRouterPortMap(data_output, routerName, xHexa, yHexa);
			}
		}
	}
	
	/**
	 * Write a router entity.
	 * @param data_output 
	 * @param routerName The router name.
	 * @param xHexa The address in X-dimension. 
	 * @param yHexa The address in Y-dimension.
	 * @throws Exception 
	 */
	public void writeRouterPortMap(DataOutputStream data_output,String routerName,String xHexa,String yHexa) throws Exception{
		data_output.writeBytes("\tRouter"+xHexa+yHexa+" : Entity work."+routerName+"\n");
		data_output.writeBytes("\tgeneric map( address => ADDRESSN"+xHexa+yHexa+" )\n");
		data_output.writeBytes("\tport map(\n");
		data_output.writeBytes("\t\tclock    => clock(N"+xHexa+yHexa+"),\n");
		data_output.writeBytes("\t\treset    => reset,\n");
		data_output.writeBytes("\t\tclock_rx => clock_rxN"+xHexa+yHexa+",\n");
		data_output.writeBytes("\t\trx       => rxN"+xHexa+yHexa+",\n");
		data_output.writeBytes("\t\tlane_rx  => lane_rxN"+xHexa+yHexa+",\n");
		data_output.writeBytes("\t\tdata_in  => data_inN"+xHexa+yHexa+",\n");
		data_output.writeBytes("\t\tcredit_o => credit_oN"+xHexa+yHexa+",\n");
		data_output.writeBytes("\t\tclock_tx => clock_txN"+xHexa+yHexa+",\n");
		data_output.writeBytes("\t\ttx       => txN"+xHexa+yHexa+",\n");
		data_output.writeBytes("\t\tlane_tx  => lane_txN"+xHexa+yHexa+",\n");
		data_output.writeBytes("\t\tdata_out => data_outN"+xHexa+yHexa+",\n");
		data_output.writeBytes("\t\tcredit_i => credit_iN"+xHexa+yHexa+");\n\n");
	}

	/**
	 * Instance all NoC signals.
	 * @param data_output 
	 * @param yHexa The address in Y-dimension.
	 * @throws Exception 
	 */
	public void writeSignals(DataOutputStream data_output, String yHexa) throws Exception{
		writeSignal(data_output,"clock_rx","regNport",yHexa);
		writeSignal(data_output,"rx","regNport",yHexa);
		writeSignal(data_output,"lane_rx","arrayNport_regNlane",yHexa);
		writeSignal(data_output,"data_in","arrayNport_regflit",yHexa);
		writeSignal(data_output,"credit_o","arrayNport_regNlane",yHexa);
		writeSignal(data_output,"clock_tx","regNport",yHexa);
		writeSignal(data_output,"tx","regNport",yHexa);
		writeSignal(data_output,"lane_tx","arrayNport_regNlane",yHexa);
		writeSignal(data_output,"data_out","arrayNport_regflit",yHexa);
		writeSignal(data_output,"credit_i","arrayNport_regNlane",yHexa);
	}
	
	/**
	 * Write all router connections.
	 * @param data_output
	 * @throws Exception 
	 */
	public void writeAllConnections(DataOutputStream data_output) throws Exception {
		String xHexa="",yHexa="",xmais1Hexa="",ymais1Hexa="",xmenos1Hexa="",ymenos1Hexa="";

		for (int y=0; y<dimY; y++){
			yHexa = Convert.decToHex(y,(flitSize/8));
			for (int x=0; x<dimX; x++){
				xHexa = Convert.decToHex(x,(flitSize/8));
				data_output.writeBytes("\t-- ROUTER "+xHexa+yHexa+"\n");
	
				//EAST PORT
				data_output.writeBytes("\t-- EAST port\n");
				if (x==(dimX-1))
					writeNullConnection(data_output,xHexa+yHexa,0);
				else{
					xmais1Hexa = Convert.decToHex((x+1),(flitSize/8));
					writeConnection(data_output,xHexa+yHexa,xmais1Hexa+yHexa,0,1);
				}
	
				//WEST PORT
				data_output.writeBytes("\t-- WEST port\n");
				if (x==0)
					writeNullConnection(data_output,xHexa+yHexa,1);
				else{
					xmenos1Hexa = Convert.decToHex((x-1),(flitSize/8));
					writeConnection(data_output,xHexa+yHexa,xmenos1Hexa+yHexa,1,0);
				}
	
				//NORTH PORT
				data_output.writeBytes("\t-- NORTH port\n");
				if (y==(dimY-1))
					writeNullConnection(data_output,xHexa+yHexa,2);
				else{
					ymais1Hexa = Convert.decToHex((y+1),(flitSize/8));
					writeConnection(data_output,xHexa+yHexa,xHexa+ymais1Hexa,2,3);
				}
	
				//SOUTH PORT
				data_output.writeBytes("\t-- SOUTH port\n");
				if (y==0)
					writeNullConnection(data_output,xHexa+yHexa,3);
				else{
					ymenos1Hexa = Convert.decToHex((y-1),(flitSize/8));
					writeConnection(data_output,xHexa+yHexa,xHexa+ymenos1Hexa,3,2);
				}
	
				//LOCAL PORT
				data_output.writeBytes("\t-- LOCAL port\n");
				writeLocalConnection(data_output, xHexa, yHexa);
			}
		}
	}

	/**
	 * Write the connection between two routers.
	 * @param data_output
	 * @param router1 The Router1 address (router to the left).
	 * @param router2 The Router2 address (router to the right).
	 * @param portRouter1 The Router1 port (router to the left).
	 * @param portRouter2 The Router2 port (router to the right).
	 * @throws Exception 
	 */
	public void writeConnection(DataOutputStream data_output, String router1,String router2,int portRouter1,int portRouter2) throws Exception {
		data_output.writeBytes("\tclock_rxN"+router1+"("+portRouter1+")<=clock_txN"+router2+"("+portRouter2+");\n");
		data_output.writeBytes("\trxN"+router1+"("+portRouter1+")<=txN"+router2+"("+portRouter2+");\n");
		data_output.writeBytes("\tlane_rxN"+router1+"("+portRouter1+")<=lane_txN"+router2+"("+portRouter2+");\n");
		data_output.writeBytes("\tdata_inN"+router1+"("+portRouter1+")<=data_outN"+router2+"("+portRouter2+");\n");
		data_output.writeBytes("\tcredit_iN"+router1+"("+portRouter1+")<=credit_oN"+router2+"("+portRouter2+");\n");
	}

	/**
	 * Connect zeros to a router port of the NoC limit.
	 * @param data_output
	 * @param router The router address.
	 * @param port The port.
	 * @throws Exception 
	 */
	public void writeNullConnection(DataOutputStream data_output, String router, int port) throws Exception {
		data_output.writeBytes("\tclock_rxN"+router+"("+port+")<='0';\n");
		data_output.writeBytes("\trxN"+router+"("+port+")<='0';\n");
		data_output.writeBytes("\tlane_rxN"+router+"("+port+")<=(others=>'0');\n");
		data_output.writeBytes("\tdata_inN"+router+"("+port+")<=(others=>'0');\n");
		data_output.writeBytes("\tcredit_iN"+router+"("+port+")<=(others=>'0');\n");
	}
	
	/**
	 * Write the connection between two routers.
	 * @param data_output
	 * @param xHexa The address in X-dimension. 
	 * @param yHexa The address in Y-dimension.
	 * @throws Exception 
	 */
	public void writeLocalConnection(DataOutputStream data_output,String xHexa,String yHexa) throws Exception {
		data_output.writeBytes("\tclock_rxN"+xHexa+yHexa+"(4)<=clock_rxLocal(N"+xHexa+yHexa+");\n");
		data_output.writeBytes("\trxN"+xHexa+yHexa+"(4)<=rxLocal(N"+xHexa+yHexa+");\n");
		data_output.writeBytes("\tlane_rxN"+xHexa+yHexa+"(4)<=lane_rxLocal(N"+xHexa+yHexa+");\n");
		data_output.writeBytes("\tdata_inN"+xHexa+yHexa+"(4)<=data_inLocal(N"+xHexa+yHexa+");\n");
		data_output.writeBytes("\tcredit_iN"+xHexa+yHexa+"(4)<=credit_iLocal(N"+xHexa+yHexa+");\n");
		data_output.writeBytes("\tclock_txLocal(N"+xHexa+yHexa+")<=clock_txN"+xHexa+yHexa+"(4);\n");
		data_output.writeBytes("\ttxLocal(N"+xHexa+yHexa+")<=txN"+xHexa+yHexa+"(4);\n");
		data_output.writeBytes("\tlane_txLocal(N"+xHexa+yHexa+")<=lane_txN"+xHexa+yHexa+"(4);\n");
		data_output.writeBytes("\tdata_outLocal(N"+xHexa+yHexa+")<=data_outN"+xHexa+yHexa+"(4);\n");
		data_output.writeBytes("\tcredit_oLocal(N"+xHexa+yHexa+")<=credit_oN"+xHexa+yHexa+"(4);\n\n");
	}
	
/*********************************************************************************
* SYSTEMC
*********************************************************************************/

	/**
	 * Creates the SystemC files
	 */
	public void createSC(){
		//copy the .cpp files to SC_NoC directory
	   	copySCFiles();
		//replacing the flags in c_input_module.c file.
	   	SCInputModule.createFile(NoC.VC, sourceDir, scDir, dimX, dimY, flitSize, nChannels);
		//replacing the flags in c_output_module.c file.
	   	SCOutputModule.createFile(NoC.VC, sourceDir, scDir, dimX, dimY, flitSize, nChannels);
		//replacing the flags in c_output_module_router.c file.
		SCOutputModuleRouter.createFile(sourceDir, scDir, nocType, NoC.VC, dimX, dimY, flitSize, nChannels);
		//create the top.cpp file using SC files.
		createTopNoC();
		//create the simulation script used by Modelsim
		createSimulateScript();
	}
	
/*********************************************************************************
* TOP
*********************************************************************************/
	
	/**
	 * Create the TopNoC file using the default package (HermesPackage).<br>
	 * This file connects the NoC to the SC files.
	 */
	public void createTopNoC(){
		createTopNoC("HermesPackage");
	}

	/**
	 * Create the TopNoC file using the informed package.<br>
	 * This file connects the NoC to the SC files.
	 * @param packageName The package name.
	 */
	public void createTopNoC(String packageName){
		try{
			FileOutputStream outFile =new FileOutputStream(projectDir + "topNoC.vhd");
			DataOutputStream data_output=new DataOutputStream(outFile);

			//libraries, entity and architecture
			writeTopHeader(data_output, packageName);

			//write signals
			writeTopSignals(data_output);

			data_output.writeBytes("begin\n");

			//generate reset and clock for all routers
			generateResetAndClock(data_output);

			//NoC port map
			writeNoCPortMap(data_output);

			if(isSC){
				// InputModule port map
				data_output.writeBytes(SCInputModule.getPortMap(NoC.VC, dimX, dimY, flitSize));
				// OutputModule port map
				data_output.writeBytes(SCOutputModule.getPortMap(NoC.VC, dimX, dimY, flitSize));
			}

			data_output.writeBytes("end topNoC;\n");
			data_output.close();
			outFile.close();
		}catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't write the TopLevel (top.vhd)","Output error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}catch(Exception e){
			JOptionPane.showMessageDialog(null,"Can't write the TopLevel (top.vhd)\n"+ e.getMessage(),"Error Message", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}
	
	/**
	 * Write Top header: libraries, entity and architecture using default package (HermesPackage).
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeTopHeader(DataOutputStream data_output) throws Exception {
		writeTopHeader(data_output, "HermesPackage");
	}
	
	/**
	 * Write Top header: libraries, entity and architecture using the informed package.
	 * @param data_output 
	 * @param packageName The package name.
	 * @throws Exception 
	 */
	public void writeTopHeader(DataOutputStream data_output, String packageName) throws Exception {
		//libraries
		data_output.writeBytes("library IEEE;\nuse IEEE.std_logic_1164.all;\nuse ieee.std_logic_arith.CONV_STD_LOGIC_VECTOR;\n");
		data_output.writeBytes("use work."+packageName+".all;\n\n");
		//Top entity
		data_output.writeBytes("entity topNoC is\nend;\n\n");
		//Top architecture
		data_output.writeBytes("architecture topNoC of topNoC is\n\n");
	}
	
	/**
	 * Write all Top signals.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeTopSignals(DataOutputStream data_output) throws Exception {
		data_output.writeBytes("\tsignal clock : regNrot;\n");
		data_output.writeBytes("\tsignal reset, finish : std_logic;\n");
		data_output.writeBytes("\tsignal clock_rx, clock_tx: regNrot;\n");
		data_output.writeBytes("\tsignal rx, tx: regNrot;\n");
		data_output.writeBytes("\tsignal lane_rx, lane_tx: arrayNrot_regNlane;\n");
		data_output.writeBytes("\tsignal data_in, data_out : arrayNrot_regflit;\n");
		data_output.writeBytes("\tsignal credit_o, credit_i: arrayNrot_regNlane;\n\n");
	}
	
	/**
	 * Generate reset and clock signals.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void generateResetAndClock(DataOutputStream data_output) throws Exception {
		String routerName;
		Vector<String> routers = getRoutersName();
		
		//generate reset
		data_output.writeBytes("\treset <= '1', '0' after 15 ns;\n\n");
		
		//generate clock for all routers
		for (int l=0; l<dimension; l++){
			routerName =(String)routers.elementAt(l);
			data_output.writeBytes("\t-- clock process of router "+routerName+"\n");
			data_output.writeBytes("\tprocess\n");
			data_output.writeBytes("\tbegin\n");
			data_output.writeBytes("\t\tclock("+routerName+") <= '1', '0' after 10 ns;\n");
			data_output.writeBytes("\t\twait for 20 ns;\n");
			data_output.writeBytes("\tend process;\n\n");
		}
	}
	
	/**
	 * Write the NoC port map.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeNoCPortMap(DataOutputStream data_output) throws Exception {
		data_output.writeBytes("\tNOC: Entity work.NOC\n");
		data_output.writeBytes("\tport map(\n");
		data_output.writeBytes("\t\tclock         => clock,\n");
		data_output.writeBytes("\t\treset         => reset,\n");
		data_output.writeBytes("\t\tclock_rxLocal => clock_rx,\n");
		data_output.writeBytes("\t\trxLocal       => rx,\n");
		data_output.writeBytes("\t\tlane_rxLocal  => lane_rx,\n");
		data_output.writeBytes("\t\tdata_inLocal  => data_in,\n");
		data_output.writeBytes("\t\tcredit_oLocal => credit_o,\n");
		data_output.writeBytes("\t\tclock_txLocal => clock_tx,\n");
		data_output.writeBytes("\t\ttxLocal       => tx,\n");
		data_output.writeBytes("\t\tlane_txLocal  => lane_tx,\n");
		data_output.writeBytes("\t\tdata_outLocal => data_out,\n");
		data_output.writeBytes("\t\tcredit_iLocal => credit_i);\n\n");
	}	
	
/*********************************************************************************
* SCRIPTS
*********************************************************************************/

	/**
	 * Create the simulation script used by Modelsim. 
	 */
	public void createSimulateScript(){
		try{
			FileOutputStream script = new FileOutputStream(projectDir + "simulate.do");
			DataOutputStream data_output = new DataOutputStream(script);
			// vlib and vmap
			writeSimulateHeader(data_output);
			// sccom SystemC files
			writeSCCOMFiles(data_output);
			// vcom VHDL files
			writeVCOMFiles(data_output);
			// vsim top Entity
			writeVSIM(data_output, "topNoC");
			// set StdArithNoWarnings
			writeNoWarnings(data_output);
			// run simulation
			writeRUN(data_output);
			// quit simulation 
			writeSimulateFooter(data_output);
			data_output.close();
			script.close();
		}catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't write simukate.do script","Output error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}catch(Exception e){
			JOptionPane.showMessageDialog(null,"Can't write simukate.do script"+e.getMessage(),"Error Message", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}
		
	/**
	 * Write the VCOM command (VHDL compilation) for all VHDL files.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeVCOMFiles(DataOutputStream data_output) throws Exception {
		// vcom the internal router files: Hermes_buffer...
		writeVCOMInternalRouter(data_output);
		// vcom All routers files: ROUTERCC ...
		writeVCOMRouters(data_output);
		// vcom NoC file
		writeVCOM(data_output, "NOC/NOC.vhd");
		// vcom topNoC file
		writeVCOM(data_output, "topNoC.vhd");
	}

	/**
	 * Write the VCOM command (VHDL compilation) for all internal router files.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeVCOMInternalRouter(DataOutputStream data_output) throws Exception {
		writeVCOM(data_output, "NOC/Hermes_package.vhd");
		writeVCOM(data_output, "NOC/Hermes_buffer.vhd");
		writeVCOM(data_output, "NOC/Hermes_inport.vhd");
		writeVCOM(data_output, "NOC/Hermes_switchcontrol.vhd");
		writeVCOM(data_output, "NOC/Hermes_outport.vhd");
	}
}