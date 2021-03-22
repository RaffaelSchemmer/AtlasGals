package AtlasPackage;

import javax.swing.*;
import java.io.*;
import java.util.*;

/**
 * Generate a NoC with Handshake flow control (without virtual channels).
 * @author Aline Vieira de Mello
 * @version
 */
public class NoCGenerationHS extends NoCGeneration{

    private String sourceDir, projectDir, nocDir, scDir;
    private String algorithm, nocType;
	private int dimX, dimY, dimension, flitSize;
	private boolean isSC;

	/**
	 * Generate a NoC using Handshake flow control.
	 * @param project The NoC project.
	 * @param source The path where are the source files. 
	 */
	public NoCGenerationHS(Project project, String source){
		super(project, source);
		sourceDir = source;
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
		algorithm = noc.getRoutingAlgorithm();
		isSC = noc.isSCTB();
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
		//copy the Hermes_buffer.vhd file
		ManipulateFile.copy(new File(sourceDir + "Hermes_buffer.vhd"), nocDir);
		//copy the Hermes_switchcontrol.vhd file
		ManipulateFile.copy(new File(sourceDir + "Hermes_switchcontrol.vhd"), nocDir);
		//create the Hermes_package.vhd file
		createPackage("Hermes_package.vhd");
		//create the routers
		createRouters();
		//create the NoC
		createNoC();
		//If the SC test bench option is selected, create the SC file
		if(isSC)
			createSC();
	}

/*********************************************************************************
* ROUTER
*********************************************************************************/

	/**
	 * Create the NoC routers.
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
	 * Create a router file.
	 * @param x The router address in X-dimension of NoC.
	 * @param y The router address in Y-dimension of NoC.
	 * @param dimX The X-dimension of NoC.
	 * @param dimY The Y-dimension of NoC.
	 * @return The code corresponding to a router.
	 */
	public String createRouter(int x, int y,int dimX,int dimY){
		String line, word, change_parameter;
		StringTokenizer st;
		String routerType = Router.getRouterType(x,y,dimX,dimY);

		try{
			FileInputStream inFile = new FileInputStream(sourceDir + "Hermes_router.vhd");
			BufferedReader buff=new BufferedReader(new InputStreamReader(inFile));

			DataOutputStream data_output=new DataOutputStream(new FileOutputStream(nocDir + routerType + ".vhd"));

			change_parameter="";
			line=buff.readLine();
			while(line!=null){
				st = new StringTokenizer(line, "$");
				int vem = st.countTokens();
				for (int cont=0; cont<vem; cont++){
					word = st.nextToken();
					change_parameter="";
					if(word.equalsIgnoreCase("Chave"))
						word = routerType;
					else if(word.equalsIgnoreCase("algorithm"))
						word = algorithm;
					else if(word.equalsIgnoreCase("filas"))
						word = getAllBuffersRouter(routerType);
					else if (word.equalsIgnoreCase("zeros"))
						word = getAllNullBuffersRouter(routerType);;

					change_parameter = change_parameter.concat(word);
					data_output.writeBytes(change_parameter);
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
		}catch(Exception e){
			JOptionPane.showMessageDialog(null,"Can't write "+routerType+".vdh\n"+e.getMessage(),"Error Message", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		return routerType;
	}

	/**
	 * Return the port map for all buffers of a specific router.
	 * @param routerType The type of router. For instance: ChaveBL (bottom-left).
	 * @return A String containing the port map for all buffers of a specific router.
	 */
	public String getAllBuffersRouter(String routerType){
		String fila = "";
		if(routerType == "ChaveCC")
			fila = getBufferInstantiate("all",0);
		else{
			for(int port=0; port<Router.NPORTS; port++){
				if(Router.hasPort(routerType, port)){
					String portName = Router.getPortName(port,1); // 1 = Capitalize
					fila = fila.concat(getBufferInstantiate("F" + portName, port));
				}
			}
		}
		return fila;
	}
	
	/**
	 * Return the port map for all buffers connected to zeros of a specific router.
	 * @param routerType The type of router. For instance: ChaveBL (bottom-left).
	 * @return A String containing the port map for all buffers connected to zeros of a specific router.
	 */
	public String getAllNullBuffersRouter(String routerType){
		String fila = "";
		for(int port=0; port<Router.NPORTS; port++){
			if(!Router.hasPort(routerType, port)){
				fila = fila.concat(getBufferNull(port));
			}
		}
		return fila;
	}

	/**
	 * Generate the input buffer instance according to the informed name and index. <br>
	 * When the informed name is ALL, all input port are instanced. 
	 * @param name The router type.
	 * @param index The index of router.
	 * @return The input port instance.
	 */
	private String getBufferInstantiate(String name,int indice){
		String instantiate="";
		if(name.equalsIgnoreCase("all"))
			instantiate = ("\tFILAS : for i in 0 to 4 generate\n\t\tF : Entity work.Fila(Fila)\n\t\tport map(\n\t\t\tclock => clock,\n\t\t\treset => reset,\n\t\t\tdata_in => data_in(i),\n\t\t\trx => rx(i),\n\t\t\tack_rx => ack_rx(i),\n\t\t\th => h(i),\n\t\t\tack_h => ack_h(i),\n\t\t\tdata_av => data_av(i),\n\t\t\tdata => data(i),\n\t\t\tdata_ack => data_ack(i),\n\t\t\tsender=>sender(i));\n\tend generate FILAS;");
		else
			instantiate = ("\t"+name+" : Entity work.Fila(Fila)\n\tport map(\n\t\tclock => clock,\n\t\treset => reset,\n\t\tdata_in => data_in("+indice+"),\n\t\trx => rx("+indice+"),\n\t\tack_rx => ack_rx("+indice+"),\n\t\th => h("+indice+"),\n\t\tack_h => ack_h("+indice+"),\n\t\tdata_av => data_av("+indice+"),\n\t\tdata => data("+indice+"),\n\t\tdata_ack => data_ack("+indice+"),\n\t\tsender=>sender("+indice+"));\n\n");
		return instantiate;
	}

	/**
	 * Connect zeros to the removed input buffer.
	 * @param index The index of removed input port.
	 * @return The instance of removed input port.
	 */
	private String getBufferNull(int indice){
		return ("\t--aterrando os sinais de entrada do buffer "+indice+" removido\n\th("+indice+")<='0';\n\tdata_av("+indice+")<='0';\n\tdata("+indice+")<=(others=>'0');\n\tsender("+indice+")<='0';\n");
	}

/*********************************************************************************
* NOC
*********************************************************************************/

	/**
	 * Create the NoC vhdl file.
	 */
	public void createNoC(){
		try{
			DataOutputStream data_output=new DataOutputStream(new FileOutputStream(nocDir+"NOC.vhd"));

			//generate the libraries
			writeNoCLibraries(data_output);			

			//generate the NoC entity
			writeNoCEntity(data_output);

			//generate the architecture
			data_output.writeBytes("architecture NOC of NOC is\n\n");

			//generate the signals
			writeAllNoCSignals(data_output);

			data_output.writeBytes("begin\n\n");

			//generate routers port map
			writeAllRoutersPortMap(data_output);

			//generate all routers connections
			writeAllConnections(data_output);

			//If SC option is selected then generate SC entity
			if(isSC){
				data_output.writeBytes(SCOutputModuleRouter.getPortMap(nocType, NoC.HS, dimX, dimY, flitSize, 1));
			}

			data_output.writeBytes("end NOC;");
			data_output.close();

		}//end try
		catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't write Noc.vhd\n" + f.getMessage(),"Output error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}catch(Exception e){
			JOptionPane.showMessageDialog(null,"Can't write Noc.vhd\n" + e.getMessage(), "Error Message", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}

	/**
	 * write NoC libraries.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeNoCLibraries(DataOutputStream data_output) throws Exception{
		data_output.writeBytes("library IEEE;\nuse IEEE.std_logic_1164.all;\nuse IEEE.std_logic_unsigned.all;\n");
		data_output.writeBytes("use work.HermesPackage.all;\n\n");
	}
	
	/**
	 * write NoC entity.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeNoCEntity(DataOutputStream data_output) throws Exception{
		data_output.writeBytes("entity NOC is\n");
		data_output.writeBytes("port(\n");
		data_output.writeBytes("\tclock         : in  std_logic;\n");
		data_output.writeBytes("\treset         : in  std_logic;\n");
		data_output.writeBytes("\trxLocal       : in  regNrot;\n");
		data_output.writeBytes("\tdata_inLocal  : in  arrayNrot_regflit;\n");
		data_output.writeBytes("\tack_rxLocal   : out regNrot;\n");
		data_output.writeBytes("\ttxLocal       : out regNrot;\n");
		data_output.writeBytes("\tdata_outLocal : out arrayNrot_regflit;\n");
		data_output.writeBytes("\tack_txLocal   : in  regNrot);\n");
		data_output.writeBytes("end NOC;\n\n");
	}

	/**
	 * Instance all NoC signals.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeAllNoCSignals(DataOutputStream data_output) throws Exception{
		String yHexa;
		for (int y=0; y<dimY; y++){
			yHexa = Convert.decToHex(y,(flitSize/8));
			writeSignals(data_output, yHexa);
		}
	}	
		
	/**
	 * Instance all signals of a specific line of NoC.
	 * @param data_output 
	 * @param yHexa The address in Y-dimension.
	 * @throws Exception 
	 */
	public void writeSignals(DataOutputStream data_output, String yHexa) throws Exception{
		writeSignal(data_output,"rx","regNport",yHexa);
		writeSignal(data_output,"data_in","arrayNport_regflit",yHexa);
		writeSignal(data_output,"ack_rx","regNport",yHexa);
		writeSignal(data_output,"tx","regNport",yHexa);
		writeSignal(data_output,"data_out","arrayNport_regflit",yHexa);
		writeSignal(data_output,"ack_tx","regNport",yHexa);
	}

	/**
	 * Write the port map of all routers.
	 * @param data_output 
	 * @param routerName The router name.
	 * @throws Exception 
	 */
	public void writeAllRoutersPortMap(DataOutputStream data_output) throws Exception{
		String xHexa, yHexa, routerType;
		for (int y=0; y<dimY; y++){
			yHexa = Convert.decToHex(y,(flitSize/8));
			for (int x=0; x<dimX; x++){
				xHexa = Convert.decToHex(x,(flitSize/8));
				routerType = Router.getRouterType(x,y,dimX,dimY);
				writeRouterPortMap(data_output, routerType, xHexa, yHexa);
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
	public void writeRouterPortMap(DataOutputStream data_output, String routerName, String xHexa, String yHexa) throws Exception{
		data_output.writeBytes("\tRouter"+xHexa+yHexa+" : Entity work."+routerName+"("+routerName+")\n");
		data_output.writeBytes("\tgeneric map( address => ADDRESSN"+xHexa+yHexa+" )\n");
		data_output.writeBytes("\tport map(\n");
		data_output.writeBytes("\t\tclock    => clock,\n");
		data_output.writeBytes("\t\treset    => reset,\n");
		data_output.writeBytes("\t\trx       => rxN"+xHexa+yHexa+",\n");
		data_output.writeBytes("\t\tdata_in  => data_inN"+xHexa+yHexa+",\n");
		data_output.writeBytes("\t\tack_rx   => ack_rxN"+xHexa+yHexa+",\n");
		data_output.writeBytes("\t\ttx       => txN"+xHexa+yHexa+",\n");
		data_output.writeBytes("\t\tdata_out => data_outN"+xHexa+yHexa+",\n");
		data_output.writeBytes("\t\tack_tx   => ack_txN"+xHexa+yHexa+");\n\n");
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
				writeLocalConnection(data_output, xHexa+yHexa);
			}
		}
	}
	
	/**
	 * Write the connection between two routers.
	 * @param data_output
	 * @param router1 The router1 address (router to the left).
	 * @param router2 The router2 address (router to the right).
	 * @param portRouter1 The router1 port (router to the left).
	 * @param portRouter2 The router2 port (router to the right).
	 * @throws Exception 
	 */
	public void writeConnection(DataOutputStream data_output,String router1,String router2,int portRouter1,int portRouter2) throws Exception {
		data_output.writeBytes("\tdata_inN"+router1+"("+portRouter1+")<=data_outN"+router2+"("+portRouter2+");\n");
		data_output.writeBytes("\trxN"+router1+"("+portRouter1+")<=txN"+router2+"("+portRouter2+");\n");
		data_output.writeBytes("\tack_txN"+router1+"("+portRouter1+")<=ack_rxN"+router2+"("+portRouter2+");\n");
	}

	/**
	 * Connect zeros to a router port of the NoC limit.
	 * @param data_output
	 * @param router The router address.
	 * @param port The number of port.
	 * @throws Exception 
	 */
	public void writeNullConnection(DataOutputStream data_output, String router, int port) throws Exception {
		data_output.writeBytes("\tdata_inN"+router+"("+port+")<=(others=>'0');\n");
		data_output.writeBytes("\trxN"+router+"("+port+")<='0';\n");
		data_output.writeBytes("\tack_txN"+router+"("+port+")<='0';\n");
	}	

	/**
	 * Write the connection between two routers.
	 * @param data_output
	 * @param router The router address. 
	 * @throws Exception 
	 */
	public void writeLocalConnection(DataOutputStream data_output,String router) throws Exception {
		data_output.writeBytes("\trxN"+router+"(4)<=rxLocal(N"+router+");\n");
		data_output.writeBytes("\tack_txN"+router+"(4)<=ack_txLocal(N"+router+");\n");
		data_output.writeBytes("\tdata_inN"+router+"(4)<=data_inLocal(N"+router+");\n");
		data_output.writeBytes("\ttxLocal(N"+router+")<=txN"+router+"(4);\n");
		data_output.writeBytes("\tack_rxLocal(N"+router+")<=ack_rxN"+router+"(4);\n");
		data_output.writeBytes("\tdata_outLocal(N"+router+")<=data_outN"+router+"(4);\n\n");
	}
	
	/**
	 * Write the OutModuleRouter SC Entity. 
	 * @param data_output
	 * @throws Exception
	 */
	public void writeSCEntity(DataOutputStream data_output) throws Exception{
		String xHexa, yHexa;
		data_output.writeBytes("\t-- the component below, router_output, must be commented to simulate without SystemC\n");
		data_output.writeBytes("\trouter_output: Entity work.outmodulerouter\n");
		data_output.writeBytes("\tport map(\n");
		data_output.writeBytes("\t\tclock     => clock,\n");
		data_output.writeBytes("\t\treset     => reset,\n");

		int router;
		for (int y=0; y<dimY; y++){
			yHexa = Convert.decToHex(y,(flitSize/8));
			for (int x=0; x<dimX; x++){
				xHexa = Convert.decToHex(x,(flitSize/8));
				router = y * dimX + x;
				if(x!=(dimX-1)) // EAST port
					writeSCConnection(data_output, router, xHexa+yHexa, 0, "EAST");
				if(x!=0) // WEST port
					writeSCConnection(data_output, router, xHexa+yHexa, 1, "WEST");
				if(y!=(dimY-1)) // NORTH port
					writeSCConnection(data_output, router, xHexa+yHexa, 2, "NORTH");
				if(y!=0)// SOUTH port
					writeSCConnection(data_output, router, xHexa+yHexa, 3, "SOUTH");
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
		data_output.writeBytes("\t\ttx_r"+router+"p"+port+"   => txN"+routerAddress+"("+namePort+"),\n");
		data_output.writeBytes("\t\tout_r"+router+"p"+port+"  => data_outN"+routerAddress+"("+namePort+"),\n");
		data_output.writeBytes("\t\tack_ir"+router+"p"+port+" => ack_txN"+routerAddress+"("+namePort+")");
		if (port == 3 && router==(dimension-1)) // SOUTH port and Last router
			data_output.writeBytes(");\n\n");
		else
			data_output.writeBytes(",\n");
	}	

/*********************************************************************************
* SystemC
*********************************************************************************/

	/**
	 * Creates the SystemC files
	 */
	public void createSC()	{
		//copy the .cpp files to SC_NoC directory
	   	copySCFiles();
		//replacing the flags in SC_InputModule.h file.
	   	SCInputModule.createFile(NoC.HS, sourceDir, scDir, dimX, dimY, flitSize, 1);
		//replacing the flags in SC_OutputModule.h file.
	   	SCOutputModule.createFile(NoC.HS, sourceDir, scDir, dimX, dimY, flitSize, 1);
		//replacing the flags in SC_OutputModuleRouter.h file.
		SCOutputModuleRouter.createFile(sourceDir, scDir, nocType, NoC.HS, dimX, dimY, flitSize, 1);
		//create the top.cpp file using SC files.
		createTopNoC();
		//create the simulation script used by Modelsim
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
			FileOutputStream outputFile = new FileOutputStream(projectDir + "topNoC.vhd");
			DataOutputStream data_output = new DataOutputStream(outputFile);

			//libraries, entity and architecture
			writeTopHeader(data_output);
				
			//signals generation
			writeTopSignals(data_output);

			//write signals
			data_output.writeBytes("begin\n");

			//generate reset and clock
			generateResetAndClock(data_output);
			
			//NoC port map
			writeNoCPortMap(data_output);

			if(isSC){
				data_output.writeBytes("\tack_tx <= tx;\n\n");
				//InputModule port map
				data_output.writeBytes(SCInputModule.getPortMap(NoC.HS, dimX, dimY, flitSize));
				//OutputModule port map
				data_output.writeBytes(SCOutputModule.getPortMap(NoC.HS, dimX, dimY, flitSize));
			}
			
			data_output.writeBytes("end topNoC;\n");
			data_output.close();
			outputFile.close();
		}catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't write the TopLevel (top.vhd)","Output error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}catch(Exception e){}
	}

	/**
	 * Write Top header: libraries, entity and architecture.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeTopHeader(DataOutputStream data_output) throws Exception {
		//Top libraries
		data_output.writeBytes("library IEEE;\nuse IEEE.std_logic_1164.all;\nuse ieee.std_logic_arith.CONV_STD_LOGIC_VECTOR;\n");
		data_output.writeBytes("use work.HermesPackage.all;\n\n");
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
		data_output.writeBytes("\tsignal clk, reset, finish : std_logic;\n");
		data_output.writeBytes("\tsignal data_in, data_out : arrayNrot_regflit;\n");
		data_output.writeBytes("\tsignal rx, ack_rx, tx, ack_tx : regNrot;\n\n");
	}	

	/**
	 * Generate reset and clock signals.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void generateResetAndClock(DataOutputStream data_output) throws Exception {
		data_output.writeBytes("\treset <= '1', '0' after 15 ns;\n");
		data_output.writeBytes("\tprocess\n");
		data_output.writeBytes("\tbegin\n");
		data_output.writeBytes("\t\tclk <= '1', '0' after 10 ns;\n");
		data_output.writeBytes("\t\twait for 20 ns;\n");
		data_output.writeBytes("\tend process;\n\n");
	}
	
	/**
	 * Write the NoC port map.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeNoCPortMap(DataOutputStream data_output) throws Exception {
		data_output.writeBytes("\tNOC: Entity work.NOC\n");
		data_output.writeBytes("\tport map(\n");
		data_output.writeBytes("\t\tclock        => clk,\n");
		data_output.writeBytes("\t\treset        => reset,\n");
		data_output.writeBytes("\t\tdata_outLocal=> data_out,\n");
		data_output.writeBytes("\t\ttxLocal      => tx,\n");
		data_output.writeBytes("\t\tack_txLocal  => ack_tx,\n");
		data_output.writeBytes("\t\tdata_inLocal => data_in,\n");
		data_output.writeBytes("\t\trxLocal      => rx,\n");
		data_output.writeBytes("\t\tack_rxLocal  => ack_rx);\n\n");
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
		// vcom the internak router files
		writeVCOMInternalRouter(data_output);
		// vcom All routers files
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
	}
}