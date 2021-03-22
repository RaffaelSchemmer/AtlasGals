package Hefestus;

import javax.swing.*;
import java.io.*;
import java.util.StringTokenizer;

import AtlasPackage.Convert;
import AtlasPackage.Router;
import AtlasPackage.ManipulateFile;

/**
 * Monitor has all methods related the power monitoring.
 * @author Aline Vieira de Mello
 * @version
 */
public class Monitor {

	/** SC Files associated to a NoC with Handshake flow control. */  
	public final static int HS = 0;
	/** SC Files associated to aNoC with Credit based flow control and without virtual channels. */  
	public final static int CB = 1;
	/** SC Files associated to a NoC with Credit based flow control and virtual channels. */  
	public final static int VC = 2;
	
	/**
	 * Remove the substring beginning in the rightmost occurrence of str2.
	 * @param str The string. 
	 * @param str2 The str2 to be searched.
	 * @return The str without the last part beggining in st2.
	 */
	public static String removeLast(String str, String str2){
		int index = str.lastIndexOf(str2);
		if(index!=-1)
			str = str.substring(0, index);
		return str;
	}
	
/*********************************************************************************
* DIRECTORIES AND CONSTANT FILES
*********************************************************************************/
	
	/**
	 * Create the project directory tree.
	 * @param projectDir The absolute path to the project directory where the monitor directories will be created. 
	 */
	public static void makeDiretories(String projectDir){
		File nocMDir=new File(projectDir + "NOC_monitores");
		nocMDir.mkdirs();
		File nocPDir=new File(projectDir + "Power");
		nocPDir.mkdirs();
		File nocPEDir=new File(projectDir + "Power" + File.separator + "Evaluation");
		nocPEDir.mkdirs();
	}

	
	/**
	 * Copy monitor files from source directory to the NoC directory.
	 * @param srcDir The absolute path to the source directory where are the router file model (with flags). 
	 * @param nocDir The absolute path to the NoC directory where the router file will be written.
	 */ 
	public static void copyFiles(String srcDir, String nocDir){
		ManipulateFile.copy(new File(srcDir + "reg16bit.vhd"), nocDir);
		ManipulateFile.copy(new File(srcDir + "Monitor_package.vhd"), nocDir);
	}
	
/*********************************************************************************
* ROUTER
*********************************************************************************/
	
	/**
	 * Create all routers.
	 * @param srcDir The source path where are the router file model (with flags). 
	 * @param nocDir The NoC path where the router file will be written. 
	 * @param dimX The X-dimension of NoC.
	 * @param dimY The Y-dimension of NoC.
	 * @param routingAlgorithm The routing algorithm. For instance: XY. 
	 * @param timeWindow The interval of two samples.
	 * @param dimX The number of routers in X-dimension of NoC.
	 * @param dimY The number of routers in X-dimension of NoC.
	 */
	public static void createRouters(String srcDir, String nocDir, int dimX, int dimY, String routingAlgorithm, String timeWindow){
		if (dimX>3) dimX = 3;
		if (dimY>3) dimY = 3;
		for(int y =0; y < dimY; y++){
			for(int x =0; x < dimX; x++){				
				String routerType = Router.getRouterType(x,y,dimX,dimY);
				createRouter(routerType, srcDir, nocDir, routingAlgorithm, timeWindow);
			}
		}
	}

	/**
	 * Create a router with monitor.
	 * @param routerType The router Type. For instance: ROUTERTC.
	 * @param srcDir The source path where are the router file model (with flags). 
	 * @param nocDir The NoC path where the router file will be written. 
	 * @param routingAlgorithm The routing algorithm. For instance: XY. 
	 * @param timeWindow The interval of two samples.
	 * @param dimX The number of routers in X-dimension of NoC.
	 * @param dimY The number of routers in X-dimension of NoC.
	 */
	public static void createRouter(String routerType, String srcDir, String nocDir, String routingAlgorithm, String timeWindow){
		String line, word;
		StringTokenizer st;

		try{
			FileInputStream inFile = new FileInputStream(srcDir + "Hermes_router_monitor.vhd");
			BufferedReader buff = new BufferedReader(new InputStreamReader(inFile));

			DataOutputStream data_output=new DataOutputStream(new FileOutputStream(nocDir + routerType + "_monitor.vhd"));

			line=buff.readLine();
			while(line!=null){
				st = new StringTokenizer(line, "$");
				int nTokens = st.countTokens();
				for (int cont=0; cont<nTokens; cont++){
					word = st.nextToken();
					if(word.equalsIgnoreCase("Chave"))
						data_output.writeBytes(routerType);
					else if(word.equalsIgnoreCase("algorithm"))
						data_output.writeBytes(routingAlgorithm);
					else if(word.equalsIgnoreCase("Parametrizable_time_window"))
						data_output.writeBytes(timeWindow);
					else if(word.equalsIgnoreCase("Parametrizable_size_vector"))
						data_output.writeBytes(""+((timeWindow.length() * 4)-1));
					else if(word.equalsIgnoreCase("portcounter") || 
							word.equalsIgnoreCase("signalcounter") || 
							word.equalsIgnoreCase("enablingCounter") ||
							word.equalsIgnoreCase("counters"))
					{
						data_output.writeBytes(Monitor.getCounterFlag(word, routerType));
					}
					else{
						if(cont==nTokens-1)
							data_output.writeBytes(word+"\n");
						else
							data_output.writeBytes(word);
					}
				}//end for
				if(nTokens==0) //white line
					data_output.writeBytes("\n");
				line=buff.readLine();
			}//end while
			buff.close();
			data_output.close();
			inFile.close();
		}catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't write "+routerType+".vdh","Output error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}catch(Exception e){
			JOptionPane.showMessageDialog(null,"Can't write "+routerType+".vdh\n" + e.getMessage(),"Error Message", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}
	
	/**
	 * Return the counter for all used ports according to the informed flag. <br>
	 * <i>This method is equal for all monitor type</i>. 
	 * @param flag The flag determines the counter that will be returned.
	 * @param routerType The router type determines which ports are used.
	 * @return A String containing the counter according to the flag.
	 */
	public static String getCounterFlag(String flag, String routerType){
		String counter = "";
		for(int port = 0; port < Router.NPORTS; port++){
			if(Router.hasPort(routerType, port)){
				counter += getCounterFlag(flag, Router.getPortName(port).toLowerCase(), port);
			}
		}
		removeLast(counter, "\n");
		return counter;
	}

	/**
	 * Return the counter for a specific port according to the informed flag. <br>
	 * <i>This method is equal for all monitor type</i>. 
	 * @param flag The flag determines the counter that will be returned.
	 * @param portName The port name. For instance: EAST.
	 * @param port The number of port. For instance: EAST = 0.
	 * @return A String containing the counter according to the flag.
	 */
	public static String getCounterFlag(String flag, String portName, int port){
		String counter = "";
		if(flag.equalsIgnoreCase("portcounter"))
			counter = getPortCounter(portName);
		else if(flag.equalsIgnoreCase("signalcounter"))
			counter = getSignalCounter(portName);
		else if(flag.equalsIgnoreCase("enablingCounter"))
			counter = getEnablingCounter(portName, port);
		else if(flag.equalsIgnoreCase("counters"))
			counter = getCounter(portName);
		return counter;
	}	
	
	/**
	* Return the signal counter for a specific port.<br>
	* <i>This method is equal for all monitor type</i>. 
	* @param portName The port name. For instance: LOCAL.
	* @return The signal counter.
	*/
	public static String getPortCounter(String portName){
		return ("\tcount_o_"+portName+" : out std_logic_vector(15 downto 0);\t-- Flit count result for "+portName+" port\n");
	}

	/**
	* Return the signal enabling counter for a specific port. <br>
	* <i>This method is equal for all monitor type</i>. 
	* @param portName The port name. For instance: LOCAL.
	* @return A String containing a signal enabling counter.
	*/
	public static String getSignalCounter(String portName){
		String signal="";
		signal += "\tsignal count_en_"+portName+"  : std_logic := '0';\t\t-- Enables the flit count at the "+portName+" counter\n";
		signal += "\tsignal reg_"+portName+"_D     : std_logic_vector(15 downto 0) := (others=>'0'); -- Input signal for the flit count register at the "+portName+" port\n";
		signal += "\tsignal reg_"+portName+"_Q     : std_logic_vector(15 downto 0) := (others=>'0'); -- Output signal for the flit count register at the "+portName+" port\n";

		for(int i=0; i<4;i++)
			signal += "\tsignal count_"+i+"cv_"+portName+" : std_logic := '0';\t\t-- Virtual channel "+i+" count enable\n";
		
		signal += "\tsignal count_cv_"+portName+"  : std_logic := '0';\t\t-- Virtual channels  count enable\n\n";
		return signal;
	}

	/**
	* Return the enabling counter for a specific port.<br>
	* <i>This method is equal for all monitor type</i>. 
	* @param portName The port name. For instance: LOCAL.
	* @param port The number of port. For instance: 4 = LOCAL.
	* @return The enabling counter.
	*/
	public static String getEnablingCounter(String portName,int port){
		String counter = "\t-- Probing the virtual channels at the east port\n";
		
		for(int i=0; i<4;i++)
			counter += "\tcount_"+i+"cv_"+portName+" <= (credit_o_in("+port+")("+i+") and lane_in("+port+")("+i+"));\n";

		counter += "\tcount_cv_"+portName+"  <= (count_0cv_"+portName+" or count_1cv_"+portName+" or count_2cv_"+portName+" or count_3cv_"+portName+");\n\n";
		counter += "\t-- Enabling the counter at the "+portName+" port\n\tcount_en_"+portName+"  <= (count_cv_"+portName+" and rx_in("+port+") and (not clock_rx_in("+port+"))) or ack_rx_in("+port+");\n\n";

		return counter;
	}

	/**
	* Return a counter port map for a specific port.<br>
	* <i>This method is equal for all monitor type</i>. 
	* @param portName The port name. For instance: LOCAL.
	* @return A String containing a counter port map.
	*/
	public static String getCounter(String portName){
		String counter = "\t-- Flit counter at "+portName+" port\n" +
						 "\tCOUNTER_"+portName+": entity work.reg16bit\n" +
						 "\tport map (\n" +
						 "\t\tck  => count_en_"+portName+",\n" +
						 "\t\trst => count_rst,\n" +
						 "\t\tce  => '1',\n" +
						 "\t\tD   => reg_"+portName+"_D,\n" +
						 "\t\tQ   => reg_"+portName+"_Q);\n\n" +
						 "\t-- Increasing the count at "+portName+" port\n" +
						 "\treg_"+portName+"_D <= reg_"+portName+"_Q + 1;\n\n" +
						 "\t-- Output register at "+portName+" port\n" +
						 "\t"+portName+"_OUT: entity work.reg16bit\n" +
						 "\tport map (\n" +
						 "\t\tck  => clock,\n" +
						 "\t\trst => reset,\n" +
						 "\t\tce  => reg_save,\n" +
						 "\t\tD   => reg_"+portName+"_Q,\n" +
						 "\t\tQ   => count_o_"+portName+");\n\n";
		
		return counter;
	}
	
/*********************************************************************************
* NOC 
*********************************************************************************/
	
	/**
	 * Return the monitor signals for all routers.<br>
	 * <i>This method is equal for all monitor type</i>.
	 * @param dimX The X-dimension of NoC
	 * @param dimY The Y-dimension of NoC
	 * @param flitSize The number of bits of a flit
	 * @return A String containing the monitor signals for all routers.
	 */
	public static String getSignals(int dimX, int dimY, int flitSize) {
		String yHexa, data="";
		int nbytes = (flitSize/8);
		for(int y =0; y < dimY; y++){
			yHexa = Convert.decToHex(y, nbytes);
			data += getSignals( dimX, nbytes, yHexa);
		}
		data += "\n";
		return data;
	}
	
	/**
	 * Return the monitor signals for all router in the same Y-coordinate.<br>
	 * <i>This method is equal for all monitor type</i>.
	 * @param dimX The X-dimension of NoC
	 * @param nbytes The number of bytes used to define the router address.
	 * @param yHexa The router address in Y-dimension.
	 * @return A String containing the monitor signals for all routers in the same Y-coordinate.
	 */
	public static String getSignals(int dimX, int nbytes, String yHexa) {
		String xHexa, data = "";
		data += "\tsignal ";
		for(int x =0; x < dimX; x++){
			xHexa = Convert.decToHex(x, nbytes);
			data += ("R"+xHexa+yHexa+"_count, ");
		}
		data = removeLast(data,",");
		data += ": router_5port;\n";

		data += "\tsignal ";
		for(int x =0; x < dimX; x++){
			xHexa = Convert.decToHex(x, nbytes);
			data += ("credit_o_in_R"+xHexa+yHexa+", ");
		}
		data = removeLast(data,",");
		data += ": reg_cv;\n";

		data += "\tsignal ";
		for(int x =0; x < dimX; x++){
			xHexa = Convert.decToHex(x, nbytes);
			data += ("lane_in_R"+xHexa+yHexa+", ");
		}
		data = removeLast(data,",");
		data += ": reg_cv;\n";

		return data;
	}

	
	/**
	 * Return the definition of monitor signals for all routers.
	 * <i>This method is <b>NOT</b> equal for all monitor type</i>.
	 * @param type The monitor type: HS, CB or VC.
	 * @param dimX The X-dimension of NoC
	 * @param dimY The Y-dimension of NoC
	 * @param flitSize The number of bits of a flit
	 * @param numberOfVCs The number of virtual channels.
	 * @return A String containing the definition of monitor signals for all routers.
	 */
	public static String getInitSignals(int type, int dimX, int dimY, int flitSize, int numberOfVCs) {
		String xHexa, yHexa, data = "";
		int nbytes = (flitSize/8);

		data += ("\t-- Definitions of signals for the correct port map (CREDIT_O)\n");
		for(int y =0; y < dimY; y++){
			yHexa = Convert.decToHex(y, nbytes);
			for(int x =0; x < dimX; x++){
				xHexa = Convert.decToHex(x, nbytes);
				data+= getInitSignals(type, xHexa+yHexa, numberOfVCs);
			}
		}
		data += "\n";
		return data;
	}

	/**
	 * Return the definition of monitor signals for a specific router.
	 * <i>This method is <b>NOT</b> equal for all monitor type</i>.
	 * @param type The monitor type: HS, CB or VC.
	 * @param router The router address. 
	 * @param numberOfVCs The number of virtual channels.
	 * @return A String containing the definition of monitor signals for a specific router.
	 */
	public static String getInitSignals(int type, String router, int numberOfVCs) {
		String data="";
		data += ("\t-- ROUTER "+router+"\n");
		switch(type){
		case HS: data += getInitSignals_HS(router); break;
		case CB: data += getInitSignals_CB(router); break;
		case VC: data += getInitSignals_VC(router, numberOfVCs); break;
		default:
			JOptionPane.showMessageDialog(null,"Monitor type undetermined: "+type,"Error in Monitor.initSignals()", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
			break;
		}
		return data;
	}
	
	/**
	 * Return the initialization of <b>HS</b> monitor signals for a specific router.
	 * @param router The router address. 
	 * @return A String containing the initialization of <b>HS</b> monitor signals for a specific router.
	 */
	public static String getInitSignals_HS(String router) {
		String data="";
		for(int port=0; port< Router.NPORTS; port++){
			data += "\t\tcredit_o_in_R"+router+"("+port+")(3 downto 0) <= (others=>'0');\n";						
		}
		for(int port=0; port< Router.NPORTS; port++){
			data += "\t\tlane_in_R"+router+"("+port+")(3 downto 0) <= (others=>'0');\n";						
		}
		return data;
	}

	/**
	 * Return the initialization of <b>CB</b> monitor signals for a specific router.
	 * @param router The router address. 
	 * @return A String containing the initialization of <b>CB</b> monitor signals for a specific router.
	 */
	public static String getInitSignals_CB(String router) {
		String data = "";
		for(int port=0; port< Router.NPORTS; port++){
			data += ("\t\tcredit_o_in_R"+router+"("+port+")(3 downto 1) <= (others=>'0');\n");
			data += ("\t\tcredit_o_in_R"+router+"("+port+")(0) <= credit_oN"+router+"("+port+");\n");
		}
		for(int port=0; port< Router.NPORTS; port++){
			data += ("\t\tlane_in_R"+router+"("+port+")(3 downto 1) <= (others=>'0');\n");
			data += ("\t\tlane_in_R"+router+"("+port+")(0) <= '1';\n");
		}
		return data;
	}

	/**
	 * Return the initialization of <b>VC</b> monitor signals for a specific router.
	 * @param router The router address. 
	 * @param numberOfVCs The number of virtual channels.
	 * @return A String containing the initialization of <b>VC</b> monitor signals for a specific router.
	 */
	public static String getInitSignals_VC(String router, int numberOfVCs) {
		String data="";
		if(numberOfVCs == 4){
			for(int port=0; port< Router.NPORTS; port++){
				data += ("\tcredit_o_in_R"+router+"("+port+")(3 downto 0) <= credit_oN"+router+"("+port+");\n");
			}
			for(int port=0; port< Router.NPORTS; port++){
				data += ("\tlane_in_R"+router+"("+port+")(3 downto 0) <= lane_rxN"+router+"("+port+");\n");
			}
		}
		else{
			for(int port=0; port< Router.NPORTS; port++){
				data += ("\tcredit_o_in_R"+router+"("+port+")(3 downto 2) <= (others=>'0');\n");
				data += ("\tcredit_o_in_R"+router+"("+port+")(1 downto 0) <= credit_oN"+router+"("+port+");\n");
			}
			for(int port=0; port< Router.NPORTS; port++){
				data += ("\tlane_in_R"+router+"("+port+")(3 downto 2) <= (others=>'0');\n");
				data += ("\tlane_in_R"+router+"("+port+")(1 downto 0) <= lane_rxN"+router+"("+port+");\n");
			}
		}
		return data;
	}
	
	/**
	 * Return a monitor port map for all router. <br>
	 * <i>This method is NOT equal for all monitor type</i>.
	 * @param type The monitor type: HS, CB or VC.
	 * @param dimX The X-dimension of NoC
	 * @param dimY The Y-dimension of NoC
	 * @param flitSize The number of bits of a flit
	 * @return A String containing a monitor port map for all router.
	 */
	public static String getPortMap(int type, int dimX, int dimY, int flitSize) {
		String xHexa, yHexa, routerName, data= "";
		int nbytes = (flitSize/8);

		for(int y =0; y < dimY; y++){
			yHexa = Convert.decToHex(y, nbytes);
			for(int x =0; x < dimX; x++){
				xHexa = Convert.decToHex(x, nbytes);
				routerName = Router.getRouterType(x,y,dimX,dimY);
				switch(type){
				case HS: data += getPortMap_HS( routerName, xHexa+yHexa); break;
				case CB: data += getPortMap_CB( routerName, xHexa+yHexa); break;
				case VC: data += getPortMap_VC( routerName, xHexa+yHexa); break;
				default:
					JOptionPane.showMessageDialog(null,"Monitor type undetermined: "+type,"Error in Monitor.getPortMap", JOptionPane.ERROR_MESSAGE);
					System.exit(0);
					break;
				}
			}
		}
		return data;
	}
	
	/**
	 * Return the <i>VC</i> monitor port map for a specific router. <br>
	 * @param routerName The router name. For instance: ROUTERTL.
	 * @param router The router address.
	 * @return A String containing the VC monitor port map for a specific router.
	 */
	public static String getPortMap_VC(String routerName, String router) {
		String data = "";
		data += ("\tMonitor_Router"+router+" : Entity work."+routerName+"_monitor\n");
		data += ("\tport map(\n");
		data += ("\t\tclock          => clock(N"+router+"),\n");
		data += ("\t\treset          => reset,\n");
		data += ("\t\tclock_rx_in    => clock_rxN"+router+",\n");
		data += ("\t\trx_in          => rxN"+router+",\n");
		data += ("\t\tcredit_o_in    => credit_o_in_R"+router+",\n");
		data += ("\t\tlane_in        => lane_in_R"+router+",\n");
		data += ("\t\tack_rx_in      => (others=>'0'),\n");
		data += getCountPorts(routerName,router);					
		data = removeLast(data, ",");
		data += (");\n\n");
		return data;
	}

	/**
	 * Return the <i>CB</i> monitor port map for a specific router. <br>
	 * @param data_output 
	 * @param routerName The router name. For instance: ROUTERTL.
	 * @param router The router address.
	 * @return A String containing the CB monitor port map for a specific router.
	 */
	public static String getPortMap_CB(String routerName, String router) {
		String data = "";
		data += ("\tMonitor_Router"+router+" : Entity work."+routerName+"_monitor\n");
		data += ("\tport map(\n");
		data += ("\t\tclock          => clock(N"+router+"),\n");
		data += ("\t\treset          => reset,\n");
		data += ("\t\tclock_rx_in    => clock_rxN"+router+",\n");
		data += ("\t\trx_in          => rxN"+router+",\n");
		data += ("\t\tcredit_o_in    => credit_o_in_R"+router+",\n");
		data += ("\t\tlane_in        => lane_in_R"+router+",\n");
		data += ("\t\tack_rx_in      => (others=>'0'),\n");
		data += getCountPorts(routerName,router);					
		data = removeLast(data, ",");
		data += (");\n\n");
		return data;
	}
	
	/**
	 * Write the <i>HS</i> monitor port map for a specific router.
	 * @param data_output 
	 * @param routerName The router name.. For instance: ROUTERTL.
	 * @param router The router address.
	 * @return A String containing the HS monitor port map for a specific router.
	 */
	public static String getPortMap_HS(String routerName, String router) {
		String data = "";
		data += ("\tMonitor_Router"+router+" : Entity work."+routerName+"_monitor\n");
		data += ("\tport map(\n");
		data += ("\t\tclock          => clock,\n");
		data += ("\t\treset          => reset,\n");
		data += ("\t\tclock_rx_in    => (others=>'0'),\n");
		data += ("\t\trx_in          => rxN"+router+",\n");
		data += ("\t\tcredit_o_in    => credit_o_in_R"+router+",\n");				
		data += ("\t\tlane_in        => lane_in_R"+router+",\n");
		data += ("\t\tack_rx_in      => ack_rxN"+router+",\n");
		data += getCountPorts(routerName,router);
		data = removeLast(data, ",");
		data += (");\n\n");
		return data;
	}

	
	/**
	* Return the count ports.<br>
	* <i>This method is equal for all monitor type</i>. 
	* @param routerType The router type.
	* @param router The router address. 
	* @return The count ports.
	*/
	public static String getCountPorts(String routerType, String router){
		String ports = "";
		for(int port = 0; port < Router.NPORTS; port++){
			if(Router.hasPort(routerType, port)){
				ports += getCountSignal(router, Router.getPortName(port).toLowerCase(), port);
			}
		}
		return ports;
	}

	/**
	* Return the count signal for a specific router and port. <br>
	* <i>This method is equal for all monitor type</i>. 
	* @param router The address router (format XY).
	* @param portName The port name. For instance: EAST. 
	* @param port The number of port. For instance: EAST = 0.
	* @return The count signal.
	*/
	public static String getCountSignal(String router, String portName, int port){
		String data ="";
		if(port == Router.EAST || port == Router.WEST)
			data = ("\t\tcount_o_"+portName+"   => R"+router+"_count("+port+"),\n");
		else
			data = ("\t\tcount_o_"+portName+"  => R"+router+"_count("+port+"),\n");
		return data;
	}	

		
/*********************************************************************************
* SCRIPTS
*********************************************************************************/

	/**
	 * Create the TXT file used by GNUPLOT to show the NoC power consumption graph.
	 * @param projectDir The project directory where the file will be written. 
	 */
	public static void createGnuplotScript(String projectDir){
		try{
			FileOutputStream script=new FileOutputStream(projectDir + "Power" + File.separator + "script_noc.txt");
			DataOutputStream data_output=new DataOutputStream(script);

			data_output.writeBytes(getGnuplotScript(projectDir));
			
			data_output.close();
			script.close();
		}catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Cannot write gnuplot script\n" + f.getMessage(),"Output error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}catch(Exception e){
			JOptionPane.showMessageDialog(null,"Cannot write gnuplot script\n" + e.getMessage(),"Error Message", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}		
	}

	/**
	 * Return the TXT file used by GNUPLOT to show the NoC power consumption graph.
	 * @param projectDir The project directory where the file will be written. 
	 * @return A String containing the TXT file used by GNUPLOT.
	 */
	public static String getGnuplotScript(String projectDir){
		String data="";
		data += ("unset title\n");
		data += ("unset label\n");
		data += ("set autoscale\n");
		data += ("set title \"NoC Average Power consumption vs Total Simulation Time\"\n");
		data += ("set xlabel \"Time (us)\"\n");
		data += ("set ylabel \"Average Power Consumption (mW)\"\n");
		data += ("set autoscale\n");
		data += ("plot '"+ projectDir + "Power" + File.separator + "Evaluation" + File.separator + "noc.dat' using 1:2 title \"Power Consumption\" with lines\n");
		data += ("pause -1 \"Press ENTER to continue\"\n");
		return data;
	}
	
	/**
	 * Return the VCOM command (VHDL compilation) for VHDL monitor files.
	 * @return A String containing VCOM command (VHDL compilation) for VHDL monitor files.
	 */
	public static String getVCOMInternalRouter() {
		String data = "";
		data += ("vcom -work work -93 -explicit NOC_monitores/reg16bit.vhd\n");
		data += ("vcom -work work -93 -explicit NOC_monitores/Monitor_package.vhd\n");
		return data;
	}

	/**
	 * Return the VCOM command (VHDL compilation) for all routers.
	 * @param data_output 
	 * @param dimX The X-dimension of NoC
	 * @param dimY The Y-dimension of NoC
	 * @return A String containing the VCOM command (VHDL compilation) for all routers.
	 */
	public static String getVCOMRouters(int dimX, int dimY) {
		String routerType, data = "";
		if (dimX > 3) dimX=3;
		if (dimY > 3) dimY=3;
		for(int y =0; y < dimY; y++){
			for(int x =0; x < dimX; x++){
				routerType = Router.getRouterType(x,y,dimX,dimY);
				data += getVCOMRouter( routerType);
			}
		}
		return data;
	}
	
	/**
	 * Return the VCOM command (VHDL compilation) for a specific router.
	 * @param data_output 
	 * @param routerType The router type.
	 * @return A String containing the VCOM command (VHDL compilation) for a specific router.
	 */
	public static String getVCOMRouter(String routerType) {
		String data="";
		data += ("vcom -work work -93 -explicit NOC_monitores/"+routerType+"_monitor.vhd\n");
		return data;
	}

	/**
	 * Return the VCOM command (VHDL compilation) for a NoC file.
	 * @return A String containing the VCOM command (VHDL compilation) for a NoC file.
	 */
	public static String getVCOMNoC() {
		return ("vcom -work work -93 -explicit NOC_monitores/NOC_monitores.vhd\n");
	}
	
	/**
	 * Create the list script used by Modelsim.
	 * @param projectDir The project directory where the file will be written. 
	 * @param timeWindow The interval between two samples.
	 * @param dimX The X-dimension of NoC
	 * @param dimY The Y-dimension of NoC
	 * @param flitSize The number of bits of a flit
	 */
	public static void createListScript(String projectDir, String timeWindow, int dimX, int dimY, int flitSize){
		try{
			FileOutputStream list = new FileOutputStream(projectDir + "list.do");
			DataOutputStream data_output = new DataOutputStream(list);
			
			data_output.writeBytes(getListScript(timeWindow, dimX, dimY, flitSize));

			data_output.close();
			list.close();
		}catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't write list.do script\n" + f.getMessage(),"Output error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}catch(Exception e){
			JOptionPane.showMessageDialog(null,"Can't write list.do script\n" + e.getMessage(),"Error Message", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}

	/**
	 * Return the list script used by Modelsim.
	 * @param timeWindow The interval between two samples.
	 * @param dimX The X-dimension of NoC
	 * @param dimY The Y-dimension of NoC
	 * @param flitSize The number of bits of a flit
	 * @return A String containing the list script.
	 */
	public static String getListScript(String timeWindow, int dimX, int dimY, int flitSize){
		int simTime = (Integer.parseInt(timeWindow,16) + 1)*(20);
		String data = "";
		data += ("onerror {resume}\n\n");

		data += getAddListAllRouters( dimX, dimY, flitSize);
		
		data += ("\n\nconfigure list -usestrobe 1\n");
		data += ("\n#Abaixo o strobeperiod depende do numero de janelas e do periodo do clock. Strobeperiod = (janelas+1)*periodo\n");
		data += ("configure list -strobestart {0 ps} -strobeperiod {"+simTime+"ns}\n");
		data += ("configure list -usesignaltrigger 0\n");			
		data += ("configure list -delta none\n");
		data += ("configure list -signalnamewidth 0\n");
		data += ("configure list -datasetprefix 0\n");
		data += ("configure list -namelimit 5\n\n");
		return data;
	}
	
	/**
	 * Return Add list for a specific signal.
	 * @param data_output
	 * @param dimX The X-dimension of NoC
	 * @param dimY The Y-dimension of NoC
	 * @param flitSize The number of bits of a flit
	 * @return A String containing Add list for all routers.
	 */
	public static String getAddListAllRouters( int dimX, int dimY, int flitSize) {
		String xHexa, yHexa, routerType, data = "";
		int nbytes = (flitSize/8);
		for(int y =0; y < dimY; y++){
			yHexa = Convert.decToHex(y, nbytes);
			for(int x =0; x < dimX; x++){
				xHexa = Convert.decToHex(x, nbytes);
				routerType = Router.getRouterType(x,y,dimX,dimY);
				data += getAddListRouter( routerType, xHexa+yHexa);
			}
		}
		return data;
	}
				
	/**
	 * Return Add list for a specific router.
	 * @param data_output
	 * @param routerType The router type determines the used ports. 
	 * @param routerName The router address.
	 * @return A String containing Add list for a specific router.
	 * @throws Exception 
	 */
	public static String getAddListRouter(String routerType, String routerName ) {
		String data = "";
		for(int port = 0; port < Router.NPORTS; port++){
			//write if the router has this port
			if(Router.hasPort(routerType, port)){
				String portName = Router.getPortName(port);
				data += getAddListSignal("R_"+routerName+"_"+portName, "/topnoc/noc/monitor_router"+routerName+"/reg_"+portName.toLowerCase()+"_Q");
			}
		}
		return data;
	}
		
	/**
	 * Return Add list for a specific signal.
	 * @param data_output 
	 * @param label The label that will be used by signal.
	 * @param signal The absolute path of signal.
	 * @return A String containing Add list for a specific signal.
	 */
	public static String getAddListSignal(String label, String signal ) {
		return ("add list -dec -width 46 -label "+label+"  "+signal+"\n");
	}
	
	/**
	 * Test the Monitor methods.
	 * @param s 
	 */
	public static void main(String s[]){
		//System.out.println(Monitor.getCounterFlag("portcounter", Router.ROUTERBL));
		//System.out.println(Monitor.getCounterFlag("signalcounter", Router.ROUTERCC));
		//System.out.println(Monitor.getCounterFlag("enablingCounter", Router.ROUTERCC));
		//System.out.println(Monitor.getCounterFlag("counters", Router.ROUTERCC));
		//System.out.println(Monitor.getSignals(3, 3, 16));
		//System.out.println(Monitor.getInitSignals(HS, 3, 3, 16, 1));
		//System.out.println(Monitor.getInitSignals(CB, 3, 3, 16, 1));
		//System.out.println(Monitor.getInitSignals(VC, 3, 3, 16, 4));
		//System.out.println(Monitor.getPortMap(Monitor.VC, 3, 3, 16));
		//System.out.println(Monitor.getSignals(5, 3, 16));
		System.out.println(Monitor.getVCOMRouters(3, 2));
	}
	
}
