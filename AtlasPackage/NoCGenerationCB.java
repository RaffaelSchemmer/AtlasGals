package AtlasPackage;

import javax.swing.*;

import java.io.*;
import java.util.*;

/**
 * Generate a NoC with Credit Based flow control.
 * @author Aline Vieira de Mello
 * @version
 */
public class NoCGenerationCB extends NoCGeneration
{
	private String sourceDir, projectDir, nocDir, scDir; 
    	private String  algorithm, nocType;
    	private int dimX, dimY, dimension, flitSize;
	private boolean isSC;
	//private Vector<String> vectorSwitch;
    
    /**
	 * Generate a NoC with Credit Based flow control.
	 * @param project The NoC project.
	 * @param source The path where are the source files. 
	 */
    public NoCGenerationCB(Project project, String source){
    	super(project, source);
		sourceDir  = source;
		initialize(project);
    }
    
    /**
     * Initialize variable.
	 * @param project The NoC project.
     */
    private void initialize(Project project){
    	NoC noc = project.getNoC();
		nocType = noc.getType();
		dimX = noc.getNumRotX();
		dimY = noc.getNumRotY();
		dimension = dimX * dimY;
		flitSize = noc.getFlitSize();
		algorithm = noc.getRoutingAlgorithm();
		isSC = noc.isSCTB();
		projectDir = project.getPath() + File.separator;
		nocDir     = projectDir + "NOC" + File.separator;
		scDir      = projectDir + "SC_NoC" + File.separator;
    }
    
	/**
	 * Generates the NoC.
	 */
	public void generate(){
		//create the tree of project directories
		makeDiretories();
		//copy the vhdl files to the NoC directory
		copyNoCFiles();
		//create the vhdl file of package
		createPackage("Hermes_package.vhd");
		//create the vhdl files of routers
		createRouters();
		//create the vhdl file of NoC
		createNoC();
		//If the SC test bench option is selected, create the SC files
		if(isSC)
			createSC();
	}
  
/*********************************************************************************
* COPY FILES
*********************************************************************************/
	/**
	* copy the NoC VHDL files to the NoC project directory.
	*/
   	public void copyNoCFiles(){
		//copy the Hermes_buffer file
   		ManipulateFile.copy(new File(sourceDir + "Hermes_buffer.vhd"), nocDir);
		//copy the Hermes_switchcontrol file
   		ManipulateFile.copy(new File(sourceDir + "Hermes_switchcontrol.vhd"), nocDir);
		//copy the Hermes_crossbar file
   		ManipulateFile.copy(new File(sourceDir + "Hermes_crossbar.vhd"), nocDir);
	}
    
/*********************************************************************************
* ROUTER
*********************************************************************************/
	/**
	 * Create all routers.
	 */
	public void createRouters(){
		int xD = dimX;
		int yD = dimY;
		if (dimX>3) xD = 3;
		if (dimY>3) yD = 3;
		for(int y =0; y < yD; y++){
			for(int x =0; x < xD; x++){
				createRouter(x,y,xD,yD);
			}
		}
	}
	
	/**
	 * Create a router.
	 * @param x The router position in X-dimension of NoC. 
	 * @param y The router position in Y-dimension of NoC.
	 * @param dimX The number of routers in X-dimension of NoC.
	 * @param dimY The number of routers in X-dimension of NoC.
	 * @return The router type. For instance: RouterCC.  
	 */
	public String createRouter(int x, int y,int dimX,int dimY){
		String line, word;
		StringTokenizer st;
		String routerType = Router.getRouterType(x,y,dimX,dimY);

		try{
			FileInputStream inFile = new FileInputStream(sourceDir + "Hermes_router.vhd");
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
			JOptionPane.showMessageDialog(null,"Can't write "+routerType+".vdh\n" + e.getMessage() ,"Error Message", JOptionPane.ERROR_MESSAGE);
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
		String word = "";
		for(int port=0; port<Router.NPORTS; port++){
			if(Router.hasPort(routerType, port)){
				String portName = Router.getPortName(port,1); // 1 = Capitalize
				word = word.concat(getBufferInstantiate("F" + portName, port));
			}
			else
				word = word.concat(getBufferNull(port));
		}
		return word;
	}
	
	/**
	* Generate the buffer instance according to the informed name and index. <br>
	* @param name The buffer name.
	* @param index The index of router.
	* @return The buffer instance.
	*/
	public String getBufferInstantiate(String name,int index){
		return ("\n\t"+name+" : Entity work.Hermes_buffer\n\tport map(\n\t\tclock => clock,\n\t\treset => reset,\n\t\tdata_in => data_in("+index+"),\n\t\trx => rx("+index+"),\n\t\th => h("+index+"),\n\t\tack_h => ack_h("+index+"),\n\t\tdata_av => data_av("+index+"),\n\t\tdata => data("+index+"),\n\t\tsender => sender("+index+"),\n\t\tclock_rx => clock_rx("+index+"),\n\t\tdata_ack => data_ack("+index+"),\n\t\tcredit_o => credit_o("+index+"));\n");
	}

	/**
	* Connect zeros to the removed buffer.
	* @param index The index of removed buffer.
	* @return The instance of removed buffer.
	*/
	public String getBufferNull(int index){
		return ("\n\t--aterrando os sinais de entrada do buffer "+index+" removido\n\th("+index+")<='0';\n\tdata_av("+index+")<='0';\n\tdata("+index+")<=(others=>'0');\n\tsender("+index+")<='0';\n\tcredit_o("+index+")<='0';\n");
	}

/*********************************************************************************
* NOC
*********************************************************************************/
	/**
	 * Create the NoC VHDL file.
	 */
	public void createNoC(){
		createNoC(true);
	}

	/**
	 * Create the NoC VHDL file.
	 * @param internalEvaluation Determines if the SC_OutputModuleRouter port map will be created.
	 */
	public void createNoC(boolean internalEvaluation){
		try{
			DataOutputStream data_output=new DataOutputStream(new FileOutputStream(nocDir + "NOC.vhd"));

			//generate the libraries
			writeNoCLibraries(data_output);

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
			
			//If SC option and internal evaluation are selected then generate SC_OutputModuleRouters port map
			if(isSC && internalEvaluation){
				data_output.writeBytes(SCOutputModuleRouter.getPortMap(nocType, NoC.CB, dimX, dimY, flitSize, 1));
			}

			data_output.writeBytes("end NOC;\n");
			data_output.close();

		}catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't write Noc.vhd\n" + f.getMessage(),"Output error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}catch(Exception e){
			JOptionPane.showMessageDialog(null,"Can't write Noc.vhd\n"+ e.getMessage(), "Error Message", JOptionPane.ERROR_MESSAGE);
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
		data_output.writeBytes("\tdata_inLocal  : in  arrayNrot_regflit;\n");
		data_output.writeBytes("\tcredit_oLocal : out regNrot;\n");
		data_output.writeBytes("\tclock_txLocal : out regNrot;\n");
		data_output.writeBytes("\ttxLocal       : out regNrot;\n");
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
		writeSignal(data_output,"data_in","arrayNport_regflit",yHexa);
		writeSignal(data_output,"credit_o","regNport",yHexa);
		writeSignal(data_output,"clock_tx","regNport",yHexa);
		writeSignal(data_output,"tx","regNport",yHexa);
		writeSignal(data_output,"data_out","arrayNport_regflit",yHexa);
		writeSignal(data_output,"credit_i","regNport",yHexa);
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
		data_output.writeBytes("\tRouter"+xHexa+yHexa+" : Entity work."+routerName+"\n");
		data_output.writeBytes("\tgeneric map( address => ADDRESSN"+xHexa+yHexa+" )\n");
		data_output.writeBytes("\tport map(\n");
		data_output.writeBytes("\t\tclock    => clock(N"+xHexa+yHexa+"),\n");
		data_output.writeBytes("\t\treset    => reset,\n");
		data_output.writeBytes("\t\tclock_rx => clock_rxN"+xHexa+yHexa+",\n");
		data_output.writeBytes("\t\trx       => rxN"+xHexa+yHexa+",\n");
		data_output.writeBytes("\t\tdata_in  => data_inN"+xHexa+yHexa+",\n");
		data_output.writeBytes("\t\tcredit_o => credit_oN"+xHexa+yHexa+",\n");
		data_output.writeBytes("\t\tclock_tx => clock_txN"+xHexa+yHexa+",\n");
		data_output.writeBytes("\t\ttx       => txN"+xHexa+yHexa+",\n");
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
				data_output.writeBytes("\t-- ROUTER "+xHexa+yHexa+"\n");
				// EAST port
				data_output.writeBytes("\t-- EAST port\n");
				if (x==(dimX-1)){
					writeNullConnection(data_output,xHexa+yHexa,0);
				}
				else{
					xmais1Hexa = Convert.decToHex((x+1),(flitSize/8));
					writeConnection(data_output,xHexa+yHexa,xmais1Hexa+yHexa,0,1);
				}
				// WEST port
				data_output.writeBytes("\t-- WEST port\n");
				if (x==0){
					writeNullConnection(data_output,xHexa+yHexa,1);
				}
				else{
					xmenos1Hexa = Convert.decToHex((x-1),(flitSize/8));
					writeConnection(data_output,xHexa+yHexa,xmenos1Hexa+yHexa,1,0);
				}
				// NORTH port
				data_output.writeBytes("\t-- NORTH port\n");
				if (y==(dimY-1)){
					writeNullConnection(data_output,xHexa+yHexa,2);
				}
				else{
					ymais1Hexa = Convert.decToHex((y+1),(flitSize/8));
					writeConnection(data_output,xHexa+yHexa,xHexa+ymais1Hexa,2,3);
				}
				// SOUTH port
				data_output.writeBytes("\t-- SOUTH port\n");
				if (y==0){
					writeNullConnection(data_output,xHexa+yHexa,3);
				}
				else{
					ymenos1Hexa = Convert.decToHex((y-1),(flitSize/8));
					writeConnection(data_output,xHexa+yHexa,xHexa+ymenos1Hexa,3,2);
				}
				// LOCAL port
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
	public void writeNullConnection(DataOutputStream data_output,String router, int port) throws Exception {
		data_output.writeBytes("\tclock_rxN"+router+"("+port+")<='0';\n");
		data_output.writeBytes("\trxN"+router+"("+port+")<='0';\n");
		data_output.writeBytes("\tdata_inN"+router+"("+port+")<=(others=>'0');\n");
		data_output.writeBytes("\tcredit_iN"+router+"("+port+")<='0';\n");
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
		data_output.writeBytes("\tdata_inN"+xHexa+yHexa+"(4)<=data_inLocal(N"+xHexa+yHexa+");\n");
		data_output.writeBytes("\tcredit_iN"+xHexa+yHexa+"(4)<=credit_iLocal(N"+xHexa+yHexa+");\n");
		data_output.writeBytes("\tclock_txLocal(N"+xHexa+yHexa+")<=clock_txN"+xHexa+yHexa+"(4);\n");
		data_output.writeBytes("\ttxLocal(N"+xHexa+yHexa+")<=txN"+xHexa+yHexa+"(4);\n");
		data_output.writeBytes("\tdata_outLocal(N"+xHexa+yHexa+")<=data_outN"+xHexa+yHexa+"(4);\n");
		data_output.writeBytes("\tcredit_oLocal(N"+xHexa+yHexa+")<=credit_oN"+xHexa+yHexa+"(4);\n\n");
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
			FileOutputStream outFile = new FileOutputStream(projectDir + "topNoC.vhd");
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
				data_output.writeBytes(SCInputModule.getPortMap(NoC.CB, dimX, dimY, flitSize));
				// OutputModule port map
				data_output.writeBytes(SCOutputModule.getPortMap(NoC.CB, dimX, dimY, flitSize));
			}

			data_output.writeBytes("end topNoC;\n");
			data_output.close();
			outFile.close();
		}catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't write the TopLevel (top.vhd)","Output error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}catch(Exception e){
			JOptionPane.showMessageDialog(null,"Can't write the TopLevel (top.vhd)\n"+ e.getMessage(),"Output error", JOptionPane.ERROR_MESSAGE);
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
		data_output.writeBytes("\tsignal clock_rx, rx, credit_o: regNrot;\n");
		data_output.writeBytes("\tsignal clock_tx, tx, credit_i: regNrot;\n");
		data_output.writeBytes("\tsignal data_in, data_out : arrayNrot_regflit;\n\n");
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
		data_output.writeBytes("\t\tdata_inLocal  => data_in,\n");
		data_output.writeBytes("\t\tcredit_oLocal => credit_o,\n");
		data_output.writeBytes("\t\tclock_txLocal => clock_tx,\n");
		data_output.writeBytes("\t\ttxLocal       => tx,\n");
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
		writeVCOM(data_output, "NOC/Hermes_switchcontrol.vhd");
		writeVCOM(data_output, "NOC/Hermes_crossbar.vhd");
	}
	
}
