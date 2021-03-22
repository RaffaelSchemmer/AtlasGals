package HermesCRC;

import javax.swing.*;

import java.io.*;
import java.util.*;

import AtlasPackage.NoC;
import AtlasPackage.ManipulateFile;
import AtlasPackage.NoCGenerationCB;
import AtlasPackage.Project;
import AtlasPackage.Convert;
import AtlasPackage.Router;
import AtlasPackage.Default;
import AtlasPackage.SCOutputModuleRouter;
import AtlasPackage.SCOutputModule;
import AtlasPackage.SCInputModule;

/**
 * Generate a HermesCRC NoC.
 * @author Aline Vieira de Mello
 * @version
 */
public class CRCCreditBased extends NoCGenerationCB
{
	private static String sourceDir = Default.atlashome + File.separator + "HermesCRC" + File.separator + "Data" + File.separator + "CreditBased" + File.separator;

	private String projectDir, nocDir, scDir, crcDir; 
    private String algorithm, nocType;
    private int dimX, dimY, dimension, flitSize;
	private boolean isSC, isSabot, isDr, isDf, isGn, isGp;
	private boolean isLinkCRC, isSourceCRC, isHammingCRC;
	private Vector<String> vectorSwitch;

	/**
	 * Generate a HermesCRC NoC. 
	 * @param project The NoC project.
	 */
	public CRCCreditBased (Project project){
		super(project, sourceDir);
		NoC noc = project.getNoC();
		nocType = noc.getType();
		dimX = noc.getNumRotX();
		dimY = noc.getNumRotY();
		dimension = dimX * dimY;
		flitSize = noc.getFlitSize();
		algorithm = noc.getRoutingAlgorithm();
		isLinkCRC = noc.isLinkCrc();
		isSourceCRC = noc.isSourceCrc();
		isHammingCRC = noc.isHammingCrc();
		isSC = noc.isSCTB();
		isSabot = noc.isSaboteur();
		isDr = noc.isDr();
		isDf = noc.isDf();
		isGn = noc.isGn();
		isGp = noc.isGp();
		vectorSwitch = new Vector<String>();
		projectDir = project.getPath() + File.separator;
		nocDir     = projectDir + "NOC" + File.separator; 
		scDir      = projectDir + "SC_NoC" + File.separator;
		
		crcDir = sourceDir;
		if(isLinkCRC){
 			crcDir = sourceDir + "LinkCRC" + File.separator;
  		}
  		else if (isSourceCRC){
  			crcDir = sourceDir + "SourceCRC" + File.separator;
  		}
		else if (isHammingCRC){
			crcDir = sourceDir +  "Hamming" + File.separator;
		}
	}

	/**
	 * Generate the NoC and SC files
	 */
	public void generate(){
		//create the project directory tree
		makeDiretories();
		// Copy and create NoC VHDL files for synthesis
		copyNoCFiles();		
		//create HermesPackage.vhd 
		createPackage("Hermes_package.vhd");
		//create saboteur
		if(isSabot)
    		createSaboteur();
		//create routers
		createRouters();
		//create the NoC
		createNoC();
		//If the SC test bench option is selected, create the SC file
		if(isSC)
			createSC();
	}

/*********************************************************************************
* DIRECTORIES AND FILES (HERMES_BUFFER AND HERMES_SWITCHCONTROL)
*********************************************************************************/

	/**
	* copy the VHDL files to the project directory.
	*/
  	public void copyNoCFiles(){
  		
  		
  		//copy the Hermes_buffer file
  		ManipulateFile.copy(new File(crcDir + "Hermes_buffer.vhd"), nocDir);
  		//copy the Hermes_switchcontrol file
  		ManipulateFile.copy(new File(crcDir + "Hermes_switchcontrol.vhd"), nocDir);
  		//copy the Hermes_crossbar file
  		ManipulateFile.copy(new File(crcDir + "Hermes_crossbar.vhd"), nocDir);
  		
  		
  		if(isHammingCRC){
  			//copy the Hammcoder file
  			ManipulateFile.copy(new File(crcDir + "Hammcoder.vhd"), nocDir);
  		}
  		else{
  			//copy the Crc_xor file
  			ManipulateFile.copy(new File(crcDir + "Crc_xor.vhd"), nocDir);
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
			FileInputStream inFile = new FileInputStream(crcDir + "Hermes_router.vhd");
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
					else if(word.equalsIgnoreCase("filas")){
						word = getAllBuffersPortMap(data_output,routerType);
					}
					data_output.writeBytes(word);
				}//end for
				data_output.writeBytes("\r\n");
				line=buff.readLine();
			}//end while
			buff.close();
			data_output.close();
			inFile.close();
		}catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't write "+routerType+".vdh","Output error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}catch(Exception e){
			JOptionPane.showMessageDialog(null,e.getMessage(),"Error Message", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		return routerType;
	}
	
	/**
	 * Write the all buffer port map.
	 * @param data_output 
	 * @param routerType The router type determines which ports the router uses.
	 * @return A String with all buffer port map. 
	 * @throws Exception 
	 */
	public String getAllBuffersPortMap(DataOutputStream data_output,String routerType) throws Exception{
		String word = "", portName;
		for(int port=0; port<Router.NPORTS; port++){
			if(port == Router.LOCAL){ // different options
				// All routers have LOCAL port
				portName = "F" + Router.getPortName(port, 1); // 1 is Capitalize 
				if (isHammingCRC)
					word += getBufferHammInstantiate(portName, port);
				else
					word += getBufferInstantiate(portName, port);
			}
			else if(Router.hasPort(routerType, port)){
				portName = Router.getPortName(port, 1); // 1 is Capitalize 
				word += getPortBufferPortMap(data_output, portName, port);
			}
			else{
				word += getPortNullBufferPortMap(data_output, port);
			}
		}
		return word;
	}	

	
	/**
	 * Write the port map for a specific buffer.
	 * @param data_output 
	 * @param portName The port name. For instance: East.
	 * @param port The number of port. For instance: East = 0.
	 * @return A String with all buffer port map. 
	 * @throws Exception 
	 */
	public String getPortBufferPortMap(DataOutputStream data_output,String portName, int port) throws Exception{
		String word = "";
		if (isHammingCRC)
			word += getBufferHammInstantiate("F"+portName,port);
		else
			word += getBufferInstantiate("F"+portName,port);
		
		if(isLinkCRC)
			word += getEcInstantiate("EC"+portName,port);
		else if(isSourceCRC)
			word += getDecInstantiate("DEC"+portName,port);
		else
			word += getCodDecInstantiate("DEC"+portName,port);
		
		return word;
	}

	/**
	 * Write the port map of a specific null buffer (buffer is connected to zeros).
	 * @param data_output 
	 * @param port The number of router port which the buffer is connected to zeros.
	 * @return A String containing the port map of buffer connected to zeros. 
	 * @throws Exception 
	 */
	public String getPortNullBufferPortMap(DataOutputStream data_output, int port) throws Exception{
		String word = "";
		if(isSourceCRC)
			word = getBufferNull2(port);
		else
			word = getBufferNull1(port);
		return word;
	}

	/**
	* Generate the Buffer instance according to the informed name and index.
	* @param routerType The router type.
	* @param index The index of router.
	* @return The Buffer instance.
	*/
	public String getBufferInstantiate(String name,int index){
		return 		("\n\t"+name+" : Entity work.Hermes_buffer\n\tport map(\n\t\tclock => clock,\n\t\treset => reset,\n\t\tdata_in => data_in("+index+"),\n\t\terror => error("+index+"),\n\t\trx => rx("+index+"),\n\t\th => h("+index+"),\n\t\tack_h => ack_h("+index+"),\n\t\tdata_av => data_av("+index+"),\n\t\tdata => data("+index+"),\n\t\tsender => sender("+index+"),\n\t\tclock_rx => clock_rx("+index+"),\n\t\tdata_ack => data_ack("+index+"),\n\t\terror_i => error_oxbar("+index+"),\n\t\tcredit_o => credit_o("+index+"));\n");
	}

	/**
	* Generate the Buffer Hamming instance according to the informed name and index.
	* @param routerType The router type.
	* @param index The index of router.
	* @return The Buffer Hamming instance.
	*/
	private String getBufferHammInstantiate(String name,int index){
		return 		("\n\t"+name+" : Entity work.Hermes_buffer\n\tport map(\n\t\tclock => clock,\n\t\treset => reset,\n\t\tdata_in => data_in("+index+"),\n\t\trx => rx("+index+"),\n\t\th => h("+index+"),\n\t\tack_h => ack_h("+index+"),\n\t\tdata_av => data_av("+index+"),\n\t\tdata => data("+index+"),\n\t\tsender => sender("+index+"),\n\t\tclock_rx => clock_rx("+index+"),\n\t\tdata_ack => data_ack("+index+"),\n\t\tcredit_o => credit_o("+index+"));\n");
	}

	/**
	* Generate the EC instance according to the informed name and index.
	* @param routerType The router type.
	* @param index The index of router.
	* @return The EC instance.
	*/
	private String getEcInstantiate(String name,int index){
		return 		("\n\t"+name+": entity work.ec_module\n\tport map(\n\t\tcrc_out => crc_out("+index+"),\n\t\tdata_inc => crossbar_out("+index+"),\n\t\tcrc_in => crc_in("+index+"),\n\t\tdata_ind   => data_in("+index+"),\n\t\trx_in => rx("+index+"),\n\t\terror => error("+index+"),\n\t\t--\n\t\terror_in => error_in("+index+"),\n\t\tcredit_in  => credit_i("+index+"),\n\t\tcredit_out => credit_ixbar("+index+"));\n");
	}

	/**
	* Generate the DECCRC instance according to the informed name and index.
	* @param routerType The router type.
	* @param index The index of router.
	* @return The DECCRC instance.
	*/
	private String getDecInstantiate(String name,int index){
		return 		("\n\t"+name+": entity work.deccrc\n\tport map(\n\t\tinput => data_in("+index+")(19 downto 4),\n\t\tcrc_in => data_in("+index+")(3 downto 0),\n\t\trx => rx("+index+"),\n\t\terror => error("+index+"));\n");
	}

	/**
	* Generate the Hamming Code instance according to the informed name and index.
	* @param routerType The router type.
	* @param index The index of router.
	* @return The Hamming Code instance.
	*/
	private String getCodDecInstantiate(String name,int index){
		return ("\n\tCoder"+name+": Entity work.hammcoder\n\tport map(\n\t\tinput => crossbar_out("+index+"),\n\t\toutput => par_out("+index+"));\n\n\tDecoder"+name+": Entity work.hammdec\n\tport map(\n\t\tinput => indec("+index+"),\n\t\toutput => outdec("+index+"));\n\t\tindec("+index+") <= par_in("+index+") & data_in("+index+");\n");
	}

	/**
	* Connect zeros to the removed buffer.
	* @param index The index of removed buffer.
	* @return The instance of removed buffer.
	*/
	private String getBufferNull1(int index){
		return ("\n\t--aterrando os sinais de entrada do buffer "+index+" removido\n\th("+index+")<='0';\n\tdata_av("+index+")<='0';\n\tdata("+index+")<=(others=>'0');\n\tsender("+index+")<='0';\n\tcredit_o("+index+")<='0';\n");
	}

	/**
	* Connect zeros to the removed buffer.
	* @param index The index of removed buffer.
	* @return The instance of removed buffer.
	*/
	private String getBufferNull2(int index){
		return ("\n\t--aterrando os sinais de entrada do buffer "+index+" removido\n\th("+index+")<='0';\n\tdata_av("+index+")<='0';\n\tdata("+index+")<=(others=>'0');\n\tsender("+index+")<='0';\n\tcredit_o("+index+")<='0';\n\terror("+index+")<='0';\n");
	}

/*********************************************************************************
* SABOTEURS
*********************************************************************************/

	/**
	 * Create the Saboteur.vhd file, replacing the flags.
	 */
	private void createSaboteur(){
		StringTokenizer st;
		String line, word, change_parameter;

		try{
			File inputFile;
	  		if(isLinkCRC || isSourceCRC)
	  			inputFile = new File(crcDir + "Saboteur.vhd");
	  		else
	  			inputFile = new File(sourceDir + "Saboteur.vhd");
	  		
	  		FileInputStream inFile = new FileInputStream(inputFile);
	  		BufferedReader buff=new BufferedReader(new InputStreamReader(inFile));

			DataOutputStream data_output=new DataOutputStream(new FileOutputStream(nocDir + "Saboteur.vhd"));

			int n_lines=0;
			change_parameter="";
			line=buff.readLine();
			while(line!=null){
				st = new StringTokenizer(line, "$");
				int nTokens = st.countTokens();
				for (int cont=0; cont<nTokens; cont++){
					word = st.nextToken();
					change_parameter="";
					if(word.equalsIgnoreCase("Dr")){
					if(isDr==false)	 word = "--";
					else word ="";}
					else if(word.equalsIgnoreCase("Df")){
					if(isDf==false)	 word = "--";
					else word ="";}
					else if(word.equalsIgnoreCase("Gn")){
					if(isGn==false)	 word = "--";
					else word ="";}
					else if(word.equalsIgnoreCase("Gp")){
					if(isGp==false)	 word = "--";
					else word ="";}
					change_parameter = change_parameter.concat(word);
					data_output.writeBytes(change_parameter);
				}//end for
				n_lines++;
				data_output.writeBytes("\r\n");
				line=buff.readLine();
			} //end while
			buff.close();
			data_output.close();
			inFile.close();
		}//end try
		catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't write Saboteur.vhd\n" + f.getMessage(),"Output error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		catch(Exception e){
			JOptionPane.showMessageDialog(null,"Can't write Saboteur.vhd\n" + e.getMessage(),"Error Message", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}

/*********************************************************************************
* NOC
*********************************************************************************/

	/**
	 * Create the NoC VHDL file.
	 * This NoC has different signals because of CRC.
	 */
	public void createNoC(){
		try{
			DataOutputStream data_output=new DataOutputStream(new FileOutputStream(nocDir + "NOC.vhd"));

			//generate the libraries
			writeNoCLibraries(data_output);

			//generate the NoC entity (different signals)
			writeNoCEntity(data_output);

			//NoC architecture
			data_output.writeBytes("architecture NOC of NOC is\n\n");

			//generate all NoC signals (different signals)
			writeAllSignals(data_output);

			data_output.writeBytes("\nbegin\n\n");

			//routers port map (different signals)
			writeAllRoutersPortMap(data_output);
			
			//generate connection (different signals)
			writeAllConnections(data_output);

			//XXX: unknown code
			writeTestSaboteur(data_output);

			//If SC option is selected then generate SC entity (different signals)
			if(isSC){
				writeSCPortMap(data_output);
			}
			data_output.writeBytes("end NOC;");
			data_output.close();
		}catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't write Noc.vhd","Output error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}catch(Exception e){
			JOptionPane.showMessageDialog(null,"Can't write Noc.vhd\n" + e.getMessage(),"Error Message", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
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
		if (isSourceCRC)
			data_output.writeBytes("\tdata_inLocal  : in  arrayNrot_regflit_crc;\n");
		else
			data_output.writeBytes("\tdata_inLocal  : in  arrayNrot_regflit;\n");
		data_output.writeBytes("\tcredit_oLocal : out regNrot;\n");
		data_output.writeBytes("\tclock_txLocal : out regNrot;\n");
		data_output.writeBytes("\ttxLocal       : out regNrot;\n");
		if (isSourceCRC)
			data_output.writeBytes("\tdata_outLocal : out arrayNrot_regflit_crc;\n");
		else
			data_output.writeBytes("\tdata_outLocal : out arrayNrot_regflit;\n");
		data_output.writeBytes("\tcredit_iLocal : in  regNrot);\n");
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
		if(isSabot){
			data_output.writeBytes("\tsignal tnflits : reg32;\n\tsignal tnerror : regflit;" );
			if (!(isHammingCRC))
				data_output.writeBytes("\n\tsignal tcrc_fail : regflit;\n\tsignal tham_fail : regflit;");
		}
	}
	
	/**
	 * Instance all NoC signals.
	 * @param data_output 
	 * @param yHexa The address in Y-dimension.
	 * @throws Exception 
	 */
	public void writeSignals(DataOutputStream data_output, String yHexa) throws Exception{
		//CLOCK_RX
		writeSignal(data_output,"clock_rx","regNport",yHexa);
		//RX
		writeSignal(data_output,"rx","regNport",yHexa);
		//DATA_IN
		if (isSourceCRC)
			writeSignal(data_output,"data_in","arrayNport_regflit_crc",yHexa);
		else
			writeSignal(data_output,"data_in","arrayNport_regflit",yHexa);
		//PAR_IN
		if (isHammingCRC)
			writeSignal(data_output,"par_in","arrayPar",yHexa);
		//CREDIT_O
		writeSignal(data_output,"credit_o","regNport",yHexa);
		//CRC_IN
		if (isLinkCRC)
			writeSignal(data_output,"crc_in","arrayCrc",yHexa);
		//ERROR_IN
		if (!(isHammingCRC))
			writeSignal(data_output,"error_in","reg4",yHexa);
		//CLOCK_TX
		writeSignal(data_output,"clock_tx","regNport",yHexa);
		//TX
		writeSignal(data_output,"tx","regNport",yHexa);
		//DATA_OUT
		if (isSourceCRC)
			writeSignal(data_output,"data_out","arrayNport_regflit_crc",yHexa);
		else
			writeSignal(data_output,"data_out","arrayNport_regflit",yHexa);
		//PAR_OUT
		if (isHammingCRC)
			writeSignal(data_output,"par_out","arrayPar",yHexa);
		//CREDIT_I
		writeSignal(data_output,"credit_i","regNport",yHexa);
		//CRC_OUT
		if (isLinkCRC)
			writeSignal(data_output,"crc_out","arrayCrc",yHexa);
		//ERROR_OUT
		if (!(isHammingCRC))
			writeSignal(data_output,"error_out","reg4",yHexa);
		if(isSabot){
			//NERROR
			writeSignal(data_output,"nerror","arrayNport_regflit := (others => (others => '0'))",yHexa);
			//NFLITS
			writeSignal(data_output,"nflits","arrayNport_reg32 := (others => (others => '0'))",yHexa);
			if (!(isHammingCRC)){
				//CRC_FAIL
				writeSignal(data_output,"crc_fail","arrayNport_regflit := (others => (others => '0'))",yHexa);
				//HAM_FAIL
				writeSignal(data_output,"ham_fail","arrayNport_regflit := (others => (others => '0'))",yHexa);
			}
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
	 * Write a router port map.
	 * @param data_output 
	 * @param routerName The router name.
	 * @param xHexa The address in X-dimension. 
	 * @param yHexa The address in Y-dimension.
	 * @throws Exception 
	 */
	public void writeRouterPortMap(DataOutputStream data_output,String routerName,String xHexa,String yHexa) throws Exception{
		data_output.writeBytes("\tRouter"+xHexa+yHexa+" : Entity work."+routerName+"("+routerName+")\n");
		data_output.writeBytes("\tgeneric map( address => ADDRESSN"+xHexa+yHexa+" )\n");
		data_output.writeBytes("\tport map(\n");
		data_output.writeBytes("\t\tclock => clock(N"+xHexa+yHexa+"),\n");
		data_output.writeBytes("\t\treset => reset,\n");
		data_output.writeBytes("\t\tclock_rx => clock_rxN"+xHexa+yHexa+",\n");
		data_output.writeBytes("\t\trx => rxN"+xHexa+yHexa+",\n");
		data_output.writeBytes("\t\tdata_in => data_inN"+xHexa+yHexa+",\n");
		if (isHammingCRC){
		data_output.writeBytes("\t\tpar_in => par_inN"+xHexa+yHexa+",\n");
		data_output.writeBytes("\t\tpar_out => par_outN"+xHexa+yHexa+",\n");}
		if (isLinkCRC)
			data_output.writeBytes("\t\tcrc_in => crc_inN"+xHexa+yHexa+",\n");
		if (!(isHammingCRC))
			data_output.writeBytes("\t\terror_in => error_inN"+xHexa+yHexa+",\n");
		if (isLinkCRC)
			data_output.writeBytes("\t\tcrc_out => crc_outN"+xHexa+yHexa+",\n");
		if (!(isHammingCRC))
		data_output.writeBytes("\t\terror_out => error_outN"+xHexa+yHexa+",\n");
		data_output.writeBytes("\t\tcredit_o => credit_oN"+xHexa+yHexa+",\n");
		data_output.writeBytes("\t\tclock_tx => clock_txN"+xHexa+yHexa+",\n");
		data_output.writeBytes("\t\ttx => txN"+xHexa+yHexa+",\n");
		data_output.writeBytes("\t\tdata_out => data_outN"+xHexa+yHexa+",\n");
		data_output.writeBytes("\t\tcredit_i => credit_iN"+xHexa+yHexa+");\n\n");
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
				data_output.writeBytes("\t-- entradas do roteador"+xHexa+yHexa+"\n");

				if (x==(dimX-1)){
					writeNullConnection(data_output,xHexa+yHexa,0);
				}
				else{
					xmais1Hexa = Convert.decToHex((x+1),(flitSize/8));
					if(isSabot){
						writeSabouteur01(data_output,xHexa+yHexa,xmais1Hexa+yHexa,x,y,(1+x),0,1);
						writeConnectionCreditBasedNoData(data_output,xHexa+yHexa,xmais1Hexa+yHexa,0,1);
					}
					else
						writeConnection(data_output,xHexa+yHexa,xmais1Hexa+yHexa,0,1);

				}
				if (x==0){
					writeNullConnection(data_output,xHexa+yHexa,1);
				}
				else{
					xmenos1Hexa = Convert.decToHex((x-1),(flitSize/8));
					if(isSabot){
						writeSabouteur01(data_output,xHexa+yHexa,xmenos1Hexa+yHexa,x,y,(x-1),1,0);
						writeConnectionCreditBasedNoData(data_output,xHexa+yHexa,xmenos1Hexa+yHexa,1,0);
					}
					else
						writeConnection(data_output,xHexa+yHexa,xmenos1Hexa+yHexa,1,0);
				}


				if (y==0){
					writeNullConnection(data_output,xHexa+yHexa,3);
				}
				else{
					ymenos1Hexa = Convert.decToHex((y-1),(flitSize/8));
					if(isSabot){
						writeSabouteur23(data_output,xHexa+yHexa,xHexa+ymenos1Hexa,x,y,(y-1),3,2);
						writeConnectionCreditBasedNoData(data_output,xHexa+yHexa,xHexa+ymenos1Hexa,3,2);
					}
					else
						writeConnection(data_output,xHexa+yHexa,xHexa+ymenos1Hexa,3,2);
				}
				if (y==(dimY-1)){
					writeNullConnection(data_output,xHexa+yHexa,2);
				}
				else{
					ymais1Hexa = Convert.decToHex((y+1),(flitSize/8));
					if(isSabot){
						writeSabouteur23(data_output,xHexa+yHexa,xHexa+ymais1Hexa,x,y,(y+1),2,3);
						writeConnectionCreditBasedNoData(data_output,xHexa+yHexa,xHexa+ymais1Hexa,2,3);
					}
					else
						writeConnection(data_output,xHexa+yHexa,xHexa+ymais1Hexa,2,3);
				}

				// LOCAL port
				writeLocalConnection(data_output, xHexa, yHexa);
			}
		}
	}	
	
	/**
	 * Write code when there is a saboteur. (unknown code!!!)
	 * @param data_output
	 * @throws Exception 
	 */
	private void writeTestSaboteur(DataOutputStream data_output) throws Exception {
		String xHexa, yHexa;
		if(isSabot){
			data_output.writeBytes("tnerror	<=\n");
			for (int y=0; y<dimY; y++){
				yHexa = Convert.decToHex(y,(flitSize/8));
				for (int x=0; x<dimX; x++){
					xHexa = Convert.decToHex(x,(flitSize/8));
					for(int z=0;z<4;z++){
						if(x==(dimX-1)){
							if (y==(dimY-1)){
								if(z==(3)){
									break;
								}
							}
						}
						data_output.writeBytes("\tnerrorN"+xHexa+yHexa+"("+z+") + ");
					}
					if(x==(dimX-1)){
						if (y==(dimY-1)){
							break;
						}
					}
					data_output.writeBytes("\n");
				}
			}
			yHexa = Convert.decToHex(dimY-1,(flitSize/8));
			xHexa = Convert.decToHex(dimX-1,(flitSize/8));
			data_output.writeBytes("\tnerrorN"+xHexa+yHexa+"(3)\n");
			data_output.writeBytes("\n;\n");
		
			data_output.writeBytes("tnflits	<=\n");
			for (int y=0; y<dimY; y++){
				yHexa = Convert.decToHex(y,(flitSize/8));
				for (int x=0; x<dimX; x++){
					xHexa = Convert.decToHex(x,(flitSize/8));
					for(int z=0;z<4;z++){
						if(x==(dimX-1)){
							if (y==(dimY-1)){
								if(z==(3)){
									break;
								}
							}
						}
						data_output.writeBytes("\tnflitsN"+xHexa+yHexa+"("+z+") + ");
					}
					if(x==(dimX-1)){
						if (y==(dimY-1)){
							break;
						}
					}
					data_output.writeBytes("\n");
				}
			}
			yHexa = Convert.decToHex(dimY-1,(flitSize/8));
			xHexa = Convert.decToHex(dimX-1,(flitSize/8));
			data_output.writeBytes("\tnflitsN"+xHexa+yHexa+"(3)\n");
			data_output.writeBytes("\n;\n");
		
			if (!(isHammingCRC)){
				data_output.writeBytes("tcrc_fail	<=\n");
				for (int y=0; y<dimY; y++){
					yHexa = Convert.decToHex(y,(flitSize/8));
						for (int x=0; x<dimX; x++){
							xHexa = Convert.decToHex(x,(flitSize/8));
							for(int z=0;z<4;z++){
								if(x==(dimX-1)){
									if (y==(dimY-1)){
										if(z==(3)){
											break;
										}
									}
								}
								data_output.writeBytes("\tcrc_failN"+xHexa+yHexa+"("+z+") + ");
							}
							if(x==(dimX-1)){
								if (y==(dimY-1)){
									break;
								}
							}
							data_output.writeBytes("\n");
						}
				}
				yHexa = Convert.decToHex(dimY-1,(flitSize/8));
				xHexa = Convert.decToHex(dimX-1,(flitSize/8));
				data_output.writeBytes("\tcrc_failN"+xHexa+yHexa+"(3)\n");
				data_output.writeBytes("\n;\n");
		
		
				data_output.writeBytes("tham_fail	<=\n");
				for (int y=0; y<dimY; y++){
				yHexa = Convert.decToHex(y,(flitSize/8));
					for (int x=0; x<dimX; x++){
					xHexa = Convert.decToHex(x,(flitSize/8));
						for(int z=0;z<4;z++){
							if(x==(dimX-1)){
								if (y==(dimY-1)){
									if(z==(3)){
										break;
									}
								}
							}
							data_output.writeBytes("\tham_failN"+xHexa+yHexa+"("+z+") + ");
						}
						if(x==(dimX-1)){
							if (y==(dimY-1)){
								break;
							}
						}
						data_output.writeBytes("\n");
					}
				}
				yHexa = Convert.decToHex(dimY-1,(flitSize/8));
				xHexa = Convert.decToHex(dimX-1,(flitSize/8));
				data_output.writeBytes("\tham_failN"+xHexa+yHexa+"(3)\n");
				data_output.writeBytes("\n;\n");
			}
		}
	}	
	
	/**
	 * Write the OutModuleRouter SC Entity. 
	 * @param data_output
	 * @throws Exception
	 */
	public void writeSCPortMap(DataOutputStream data_output) throws Exception{
		String yHexa = Convert.decToHex(0,(flitSize/8));
		String xHexa = Convert.decToHex(0,(flitSize/8));
		data_output.writeBytes("\t-- the component below, router_output, must be commented to simulate without SystemC\n");
		data_output.writeBytes("\trouter_output: Entity work.outmodulerouter\n");
		data_output.writeBytes("\tport map(\n");
		data_output.writeBytes("\t\tclock          => clock(N"+xHexa+yHexa+"),\n");
		data_output.writeBytes("\t\treset          => reset,\n");

		int router;
		for (int y=0; y<dimY; y++){
			yHexa = Convert.decToHex(y,(flitSize/8));
			for (int x=0; x<dimX; x++){
				xHexa = Convert.decToHex(x,(flitSize/8));
				router = y * dimX + x;
				if(x!=(dimX-1)){ // EAST port
					writeSCConnection(data_output, router, xHexa+yHexa, 0, "EAST");
				}
				if(x!=0){        // WEST port
					writeSCConnection(data_output, router, xHexa+yHexa, 1, "WEST");
				}
				if(y!=(dimY-1)){ // NORTH port
					writeSCConnection(data_output, router, xHexa+yHexa, 2, "NORTH");
				}
				if(y!=0){        // SOUTH port
					writeSCConnection(data_output, router, xHexa+yHexa, 3, "SOUTH");
				}
			}
		}
	}
	
	/**
	 * Write the connection between two routers.
	 * @param data_output
	 * @param router The number of router.
	 * @param routerAddress The router address (format XY).
	 * @param port The router port.
	 * @param namePort The name of port. For instance: 0 = EAST
	 * @throws Exception 
	 */
	public void writeSCConnection(DataOutputStream data_output, int router, String routerAddress, int port, String namePort) throws Exception {
		data_output.writeBytes("\t\ttx_r"+router+"p"+port+"        => txN"+routerAddress+"("+namePort+"),\n");
		if(isSourceCRC)
			data_output.writeBytes("\t\tout_r"+router+"p"+port+"       => data_outN"+routerAddress+"("+namePort+")(19 downto 4),\n");
		else data_output.writeBytes("\t\tout_r"+router+"p"+port+"       => data_outN"+routerAddress+"("+namePort+"),\n");
		data_output.writeBytes("\t\tcredit_ir"+router+"p"+port+"   => credit_iN"+routerAddress+"("+namePort+")");

		if (port == 3 && router==(dimension-1)) // SOUTH port and Last router
			data_output.writeBytes(");\n\n");
		else
			data_output.writeBytes(",\n");
		
	}	
	
	private void writeSabouteur01(DataOutputStream data_output,String nodo1,String nodo2,int i, int j, int k, int l, int m) throws Exception {
		if (k<0){ k=0; }
		
		data_output.writeBytes("\n\tSabouteur"+k+j+i+j+": entity work.saboteur\n");
		data_output.writeBytes("\tport map(");
		data_output.writeBytes("\n\tclock => clock(N"+nodo1+"),");
		data_output.writeBytes("\n\treset => reset,");
		if(isSourceCRC){
			data_output.writeBytes("\n\tinput => data_outN"+nodo2+"("+m+")(19 downto 4),");
			data_output.writeBytes("\n\toutput => data_inN"+nodo1+"("+l+")(19 downto 4),");}
		else {
			data_output.writeBytes("\n\tinput => data_outN"+nodo2+"("+m+"),");
			data_output.writeBytes("\n\toutput => data_inN"+nodo1+"("+l+"),");}
		data_output.writeBytes("\n\ttx => txN"+nodo2+"("+m+"),");
		data_output.writeBytes("\n\tcredit_i => credit_iN"+nodo2+"("+m+"),");
		data_output.writeBytes("\n\tnerror => nerrorN"+nodo2+"("+m+"),");

		if (!(isHammingCRC)){
			data_output.writeBytes("\n\tcrc_fail => crc_failN"+nodo2+"("+m+"),");
			data_output.writeBytes("\n\tham_fail => ham_failN"+nodo2+"("+m+"),");
		}
		data_output.writeBytes("\n\tnflits => nflitsN"+nodo2+"("+m+"));\n");
	}

	private void writeSabouteur23(DataOutputStream data_output,String nodo1,String nodo2,int i, int j, int k, int l, int m) throws Exception {
		if (k<0){	k=0;}
			
		data_output.writeBytes("\n\tSabouteur"+i+k+i+j+": entity work.saboteur\n");
		data_output.writeBytes("\tport map(");
		data_output.writeBytes("\n\tclock => clock(N"+nodo1+"),");
		data_output.writeBytes("\n\treset => reset,");
		if(isSourceCRC){
			data_output.writeBytes("\n\tinput => data_outN"+nodo2+"("+m+")(19 downto 4),");
			data_output.writeBytes("\n\toutput => data_inN"+nodo1+"("+l+")(19 downto 4),");}
		else {
			data_output.writeBytes("\n\tinput => data_outN"+nodo2+"("+m+"),");
			data_output.writeBytes("\n\toutput => data_inN"+nodo1+"("+l+"),");}
		data_output.writeBytes("\n\ttx => txN"+nodo2+"("+m+"),");
		data_output.writeBytes("\n\tcredit_i => credit_iN"+nodo2+"("+m+"),");
		data_output.writeBytes("\n\tnerror => nerrorN"+nodo2+"("+m+"),");
		if (!(isHammingCRC)){
			data_output.writeBytes("\n\tcrc_fail => crc_failN"+nodo2+"("+m+"),");
			data_output.writeBytes("\n\tham_fail => ham_failN"+nodo2+"("+m+"),");
		}
		data_output.writeBytes("\n\tnflits => nflitsN"+nodo2+"("+m+"));\n");
	}

	/**
	 * Write the connection between two routers.
	 * @param data_output
	 * @param nodo1 The router address in the left.
	 * @param nodo2 The router address in the right.
	 * @param i The router port in the left.
	 * @param j The router port in the right.
	 * @throws Exception 
	 */
	public void writeConnection(DataOutputStream data_output,String nodo1,String nodo2,int i,int j) throws Exception {
		data_output.writeBytes("\n\tclock_rxN"+nodo1+"("+i+")<=clock_txN"+nodo2+"("+j+");\n");
		data_output.writeBytes("\trxN"+nodo1+"("+i+")<=txN"+nodo2+"("+j+");\n");
		data_output.writeBytes("\tdata_inN"+nodo1+"("+i+")<=data_outN"+nodo2+"("+j+");\n");
		data_output.writeBytes("\tcredit_iN"+nodo1+"("+i+")<=credit_oN"+nodo2+"("+j+");\n");
		if (isLinkCRC)
			data_output.writeBytes("\tcrc_inN"+nodo1+"("+i+")<=crc_outN"+nodo2+"("+j+");\n");
		if (isHammingCRC)
			data_output.writeBytes("\tpar_inN"+nodo1+"("+i+")<=par_outN"+nodo2+"("+j+");\n");
		else
			data_output.writeBytes("\terror_inN"+nodo1+"("+i+")<=error_outN"+nodo2+"("+j+");\n");
	}

	/**
	 * Connect zeros to a router port of the NoC limit.
	 * @param data_output
	 * @param nodo The router.
	 * @param i The port.
	 * @throws Exception 
	 */
	public void writeNullConnection(DataOutputStream data_output,String nodo,int i) throws Exception {
		data_output.writeBytes("\n\tclock_rxN"+nodo+"("+i+")<='0';\n");
		data_output.writeBytes("\trxN"+nodo+"("+i+")<='0';\n");
		data_output.writeBytes("\tdata_inN"+nodo+"("+i+")<=(others=>'0');\n");
		data_output.writeBytes("\tcredit_iN"+nodo+"("+i+")<='0';\n");
		if (isLinkCRC)
			data_output.writeBytes("\tcrc_inN"+nodo+"("+i+")<=(others => '0');\n");
		if (isHammingCRC)
			data_output.writeBytes("\tpar_inN"+nodo+"("+i+")<=(others =>'0');\n");
		else
			data_output.writeBytes("\terror_inN"+nodo+"("+i+")<='0';\n");
	}

	/**
	 * Write the connection between a router in limit of NoC and a saboteur.
	 * @param data_output
	 * @param nodo1 The router address in the left.
	 * @param nodo2 The router address in the right.
	 * @param i The router port in the left.
	 * @param j The router port in the right.
	 * @throws Exception 
	 */
	private void writeConnectionCreditBasedNoData(DataOutputStream data_output,String nodo1,String nodo2,int i,int j) throws Exception{
		data_output.writeBytes("\n\tclock_rxN"+nodo1+"("+i+")<=clock_txN"+nodo2+"("+j+");\n");
		data_output.writeBytes("\trxN"+nodo1+"("+i+")<=txN"+nodo2+"("+j+");\n");
		data_output.writeBytes("\tcredit_iN"+nodo1+"("+i+")<=credit_oN"+nodo2+"("+j+");\n");
		if (isLinkCRC)
			data_output.writeBytes("\tcrc_inN"+nodo1+"("+i+")<=crc_outN"+nodo2+"("+j+");\n");
		else
			data_output.writeBytes("\tdata_inN"+nodo1+"("+i+")(3 downto 0)<=data_outN"+nodo2+"("+j+")(3 downto 0);\n");
		if (isHammingCRC)
			data_output.writeBytes("\tpar_inN"+nodo1+"("+i+")<=par_outN"+nodo2+"("+j+");\n");
		else
			data_output.writeBytes("\terror_inN"+nodo1+"("+i+")<=error_outN"+nodo2+"("+j+");\n");
	}

/*********************************************************************************
* SystemC
*********************************************************************************/

	/**
	 * Creates the SystemC files
	 */
	public void createSC(){
		//copy the .cpp files to SC_NoC directory
	   	copySCFiles();
		//replacing the flags in c_input_module.c file.
	   	SCInputModule.createFile(NoC.CB, sourceDir, scDir, dimX, dimY, flitSize, 1);
		//replacing the flags in c_output_module.c file.
	   	SCOutputModule.createFile(NoC.CB, sourceDir, scDir, dimX, dimY, flitSize, 1);
		//replacing the flags in c_output_module_router.c file.
		SCOutputModuleRouter.createFile(sourceDir, scDir, nocType, NoC.CB, dimX, dimY, flitSize, 1);

		//create the top.cpp file using SC files.
		createTopNoC();
		//create the simulation scripts using SC files
		createSimulateScript();
	}

/*********************************************************************************
* TOP
*********************************************************************************/

	/**
	 * Create the TopNoC file that integrate the NoC to the SC files.
	 */
	public void createTopNoC(){
		try{
			FileOutputStream cool_vhdl=new FileOutputStream(projectDir + "topNoC.vhd");
			DataOutputStream data_output=new DataOutputStream(cool_vhdl);
			
			//libraries, entity and architecture
			writeTopHeader(data_output);

			//write signals (different signals)
			writeTopSignals(data_output);
			
			data_output.writeBytes("begin\n");

			//generate reset and clock for all routers
			generateResetAndClock(data_output);

			//NoC port map (different signals)
			writeNoCPortMap(data_output);
			
			//CRC port map for all routers
			writeTopAllCRCPortMap(data_output);

			if(isSC){
				// InputModule port map (different signals)
				writeSCInputPortMap(data_output);
				// OutputModule port map (different signals)
				writeSCOutputPortMap(data_output);
			}

			data_output.writeBytes("end topNoC;\n");
			data_output.close();
		}catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't write the TopLevel (top.vhd)","Output error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}catch(Exception e){
			JOptionPane.showMessageDialog(null,"Can't write the TopLevel (top.vhd)\n"+ e.getMessage(),"Output error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}

	/**
	 * Write all Top signals.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeTopSignals(DataOutputStream data_output) throws Exception {
		super.writeTopSignals(data_output);
		if (isSourceCRC){
			data_output.writeBytes("\tsignal data_in_noc, data_out_noc : arrayNrot_regflit_crc;\n\tsignal crc_sig: arrayNrot_reg4;\n\n");
		}
	}
	
	/**
	 * Write the NoC port map.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeNoCPortMap(DataOutputStream data_output) throws Exception {
		data_output.writeBytes("\tNOC: Entity work.NOC(NOC)\n");
		data_output.writeBytes("\tport map(\n");
		data_output.writeBytes("\t\tclock         => clock,\n");
		data_output.writeBytes("\t\treset         => reset,\n");
		data_output.writeBytes("\t\tclock_rxLocal => clock_rx,\n");
		data_output.writeBytes("\t\trxLocal       => rx,\n");
		if (isSourceCRC)
		data_output.writeBytes("\t\tdata_inLocal  => data_in_noc,\n");
		else
			data_output.writeBytes("\t\tdata_inLocal  => data_in,\n");
		data_output.writeBytes("\t\tcredit_oLocal => credit_o,\n");
		data_output.writeBytes("\t\tclock_txLocal => clock_tx,\n");
		data_output.writeBytes("\t\ttxLocal       => tx,\n");
		if (isSourceCRC)
		data_output.writeBytes("\t\tdata_outLocal => data_out_noc,\n");
		else
			data_output.writeBytes("\t\tdata_outLocal  => data_out,\n");
		data_output.writeBytes("\t\tcredit_iLocal => credit_i);\n\n");
	}
	
	/**
	 * Write CRC signals for all routers.
	 * @param data_output 
	 * @throws Exception 
	 */
	private void writeTopAllCRCPortMap(DataOutputStream data_output) throws Exception {
		if (isSourceCRC){
			for (int x=0; x<(dimX*dimY); x++){
				data_output.writeBytes("\n\tcrc"+x+": entity work.crc4p port map(input=>data_in("+x+"),output=>crc_sig("+x+"));");
				}
			data_output.writeBytes("\n\n");
			for (int x=0; x<(dimX*dimY); x++){
				data_output.writeBytes("\n\tdata_in_noc("+x+") <= data_in("+x+") & crc_sig("+x+");");
				}
			data_output.writeBytes("\n\n");
			for (int x=0; x<(dimX*dimY); x++){
				data_output.writeBytes("\n\tdata_out("+x+") <= data_out_noc("+x+")(19 downto 4);");
				}
			data_output.writeBytes("\n\n");
		}
	}
	
	/**
	 * Write the InputModule port map.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeSCInputPortMap(DataOutputStream data_output) throws Exception {
		String routerName;
		Vector<String> routers = getRoutersName();

		data_output.writeBytes("\tcim00: Entity work.inputmodule\n");
		data_output.writeBytes("\tport map(\n");
		data_output.writeBytes("\t\tclock       => clock("+(String)routers.elementAt(0)+"),\n");
		data_output.writeBytes("\t\treset       => reset,\n");
		data_output.writeBytes("\t\tfinish      => finish,\n");

		for (int l=0; l<dimension; l++){
			routerName =(String)routers.elementAt(l);
			data_output.writeBytes("\t\toutclock"+l+"   => clock_rx("+routerName+"),\n");
			data_output.writeBytes("\t\touttx"+l+"      => rx("+routerName+"),\n");
			if(isSourceCRC)
				data_output.writeBytes("\t\toutdata"+l+"    => data_in("+routerName+"),\n");
			else
				data_output.writeBytes("\t\toutdata"+l+"    => data_in("+routerName+"),\n");
			if(l==(dimension-1))  //without comma in the end
				data_output.writeBytes("\t\tincredit"+l+"   => credit_o("+routerName+"));\n");
			else
				data_output.writeBytes("\t\tincredit"+l+"   => credit_o("+routerName+"),\n");
		}
		data_output.writeBytes("\n");
	}

	/**
	 * Write the OutputModule port map.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeSCOutputPortMap(DataOutputStream data_output) throws Exception {
		String routerName;
		Vector<String> routers = getRoutersName();
	
		data_output.writeBytes("\tcom00: Entity work.outmodule\n");
		data_output.writeBytes("\tport map(\n");
		data_output.writeBytes("\t\tclock       => clock("+(String)routers.elementAt(0)+"),\n");
		data_output.writeBytes("\t\treset       => reset,\n");
		data_output.writeBytes("\t\tfinish      => finish,\n");
	
		for (int l=0; l<dimension; l++){
			routerName =(String)routers.elementAt(l);
			data_output.writeBytes("\t\tinClock"+l+"    => clock_tx("+routerName+"),\n");
			data_output.writeBytes("\t\tinTx"+l+"       => tx("+routerName+"),\n");
			if(isSourceCRC)
				data_output.writeBytes("\t\tinData"+l+"     => data_out("+routerName+"),\n");
			else
				data_output.writeBytes("\t\tinData"+l+"     => data_out("+routerName+"),\n");
			if(l==(dimension-1)) //without comma in the end
				data_output.writeBytes("\t\toutCredit"+l+"  => credit_i("+routerName+"));\n");
			else
				data_output.writeBytes("\t\toutCredit"+l+"  => credit_i("+routerName+"),\n");
		}
		data_output.writeBytes("\n");
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
			//add List to crc signals
			writeAddList(data_output);
			// run simulation
			writeRUN(data_output);
			if(isSabot)
				writeList(data_output, "out.txt");
			// quit simulation 
			writeSimulateFooter(data_output);
			data_output.close();
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
		if (isHammingCRC)
			writeVCOM(data_output, "NOC/Hammcoder.vhd");
		else
			writeVCOM(data_output, "NOC/Crc_xor.vhd");
		if(isSabot)
			writeVCOM(data_output, "NOC/Saboteur.vhd");

		writeVCOM(data_output, "NOC/Hermes_buffer.vhd");
		writeVCOM(data_output, "NOC/Hermes_switchcontrol.vhd");
		writeVCOM(data_output, "NOC/Hermes_crossbar.vhd");
	}
	
	/**
	 * Write Add list.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeAddList(DataOutputStream data_output) throws Exception {
		if(isSabot){
			writeAddList(data_output,"/topnoc/noc/tnflits");
			writeAddList(data_output,"/topnoc/noc/tnerror");
			if(!(isHammingCRC)){
				writeAddList(data_output,"/topnoc/noc/tcrc_fail");
				writeAddList(data_output,"/topnoc/noc/tham_fail");
			}
		}
	}

}
