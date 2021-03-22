package AtlasPackage;

import javax.swing.*;
import java.io.*;
import java.util.*;

/**
 * SCOutputModuleRouter has methods to generate the SC_OutputModuleRouter port map and file.
 * @author Aline Vieira de Mello
 * @version
 */
public class SCOutputModuleRouter{
    
	/**
	 * Return the OutModuleRouter port map. (This method is used in NoC file).
	 * @param nocType The NoC type and the NoC dimensions determine which ports are used in each router.
	 * @param type Each type has a distinct group of signals. Types: CB, HS, VC. 
	 * @param dimX The X-dimension of NoC.
	 * @param dimY The Y-dimension of NoC.
	 * @param flitSize The number of bits of a flit.
	 * @param nChannels The number of virtual channels.
	 * @return A String containing the SC_OutputModuleRouter port map.
	 */
	public static String getPortMap(String nocType, int type, int dimX, int dimY, int flitSize, int nChannels) {
		int nbytes = (flitSize/8);
		String data = "";
		String routerName = Convert.decToHex(0,nbytes) + Convert.decToHex(0,nbytes);
		
		data += ("\t-- the component below, router_output, must be commented to simulate without SystemC\n");
		data += ("\trouter_output: Entity work.outmodulerouter\n");
		data += ("\tport map(\n");
		
		if(type == NoC.HS)
			data += ("\t\tclock           => clock,\n");
		else
			data += ("\t\tclock           => clock(N"+routerName+"),\n");
		
		data += ("\t\treset           => reset,\n");
		
		if(nocType.equalsIgnoreCase(NoC.HERMESTU)){
			data += get1DTorusRouterConnection(type, dimX, dimY, flitSize, nChannels);
		}
		else if(nocType.equalsIgnoreCase(NoC.HERMESTB) || nocType.equalsIgnoreCase(NoC.MERCURY)){
			data += get2DTorusRouterConnection(type, dimX, dimY, flitSize, nChannels);
		}
		else{
			data += get2DMeshRouterConnection(type, dimX, dimY, flitSize, nChannels);
		}
		data = Convert.removeLast(data, ",");
		data += (");\n\n");
		return data;
	}
	
	/**
	 * Return a connection between the SOR and a specific TORUS 1D router.<i>All Routers have only EAST and NORTH ports.</i>
	 * @param type Each type has a distinct group of signals. Types: CB, HS, VC. 
	 * @param dimX The X-dimension of NoC.
	 * @param dimY The Y-dimension of NoC.
	 * @param flitSize The number of bits of a flit.
	 * @param nChannels The number of virtual channels.
	 * @return A String containing the connection. 
	 */
	public static String get1DTorusRouterConnection(int type, int dimX, int dimY, int flitSize, int nChannels) {
		int router;
		int nbytes = (flitSize/8);
		String xH, yH, data = "";
				
		for(int y =0; y < dimY; y++){
			yH = Convert.decToHex(y,nbytes);
			for(int x =0; x < dimX; x++){
				xH = Convert.decToHex(x,nbytes);
				router = y * dimX + x;
				data += getPortConnection(type, nChannels, xH + yH, router, Router.EAST);
				data += getPortConnection(type, nChannels, xH + yH, router, Router.NORTH);
			}
		}
		return data;
	}
	
	/**
	 * Return a connection between the SOR and a specific TORUS 2D router.<i>All Routers have all ports.</i>
	 * @param type Each type has a distinct group of signals. Types: CB, HS, VC. 
	 * @param dimX The X-dimension of NoC.
	 * @param dimY The Y-dimension of NoC.
	 * @param flitSize The number of bits of a flit.
	 * @param nChannels The number of virtual channels.
	 * @return A String containing the connection. 
	 */
	public static String get2DTorusRouterConnection(int type, int dimX, int dimY, int flitSize, int nChannels) {
		int router;
		int nbytes = (flitSize/8);
		String xH, yH, data = "";
				
		for(int y =0; y < dimY; y++){
			yH = Convert.decToHex(y,nbytes);
			for(int x =0; x < dimX; x++){
				xH = Convert.decToHex(x,nbytes);
				router = y * dimX + x;
				for(int port=0; port < (Router.NPORTS-1); port++){
					data += getPortConnection(type, nChannels, xH + yH, router, port);
				}
			}
		}
		return data;
	}
	
	/**
	 * Return a connection between the SOR and a specific MESH 2D router. <i>Routers in NoC border do not have all ports.</i>.
	 * @param type Each type has a distinct group of signals. Types: CB, HS, VC.
	 * @param dimX The X-dimension of NoC.
	 * @param dimY The Y-dimension of NoC.
	 * @param flitSize The number of bits of a flit.
	 * @param nChannels The number of virtual channels.
	 * @return A String containing the connection. 
	 */
	public static String get2DMeshRouterConnection(int type, int dimX, int dimY, int flitSize, int nChannels) {
		int router;
		int nbytes = (flitSize/8);
		String xH, yH, data = "";
				
		for(int y =0; y < dimY; y++){
			yH = Convert.decToHex(y,nbytes);
			for(int x =0; x < dimX; x++){
				xH = Convert.decToHex(x,nbytes);
				router = y * dimX + x;
				for(int port=0; port < (Router.NPORTS-1); port++){
					if(Router.hasPort(port, x, y, dimX, dimY)){
						data += getPortConnection(type, nChannels, xH + yH, router, port);
					}
				}
			}
		}
		return data;
	}

	/**
	 * Return a connection between the VC SOR and a specific port of a router.
	 * @param type Each type has a distinct group of signals. Types: CB, HS, VC 
	 * @param nChannels The number of virtual channels.
	 * @param routerAddress The router address (format XY).
	 * @param router The number of router.
	 * @param port The number of port.
	 * @return A String containing the connection. 
	 */
	public static String getPortConnection(int type, int nChannels, String routerAddress, int router, int port) {
		String portName = Router.getPortName(port);
		switch(type){
		case NoC.HS: return getPortConnection_HS(routerAddress, portName, router, port);
		case NoC.CB: return getPortConnection_CB(routerAddress, portName, router, port);
		case NoC.VC: return getPortConnection_VC(nChannels, routerAddress, portName, router, port);
		default:
			JOptionPane.showMessageDialog(null,"Indefined type : "+ type+"\nError in SCOutputModuleRouter.getPortConnection","Error Mesage" ,JOptionPane.ERROR_MESSAGE);
			System.exit(0);
			break;
		}
		return portName; //never here
	}
	
	/**
	 * Return a connection between the VC SOR and a specific port of a router.
	 * @param nChannels The number of virtual channels.
	 * @param routerAddress The router address (format XY).
	 * @param portName A String containing the port name. 
	 * @param router The number of router.
	 * @param port The number of port.
	 * @return A String containing the connection. 
	 */
	public static String getPortConnection_VC(int nChannels, String routerAddress, String portName, int router, int port) {
		String data = ("\t\ttx_r"+router+"p"+port+"         => txN"+routerAddress+"("+portName+"),\n");
		for(int channel=0;channel<nChannels;channel++){
			data += ("\t\tlane_tx_r"+router+"p"+port+"l"+channel+"  => lane_txN"+routerAddress+"("+portName+")(L"+(channel+1)+"),\n");
		}
		data += ("\t\tout_r"+router+"p"+port+"        => data_outN"+routerAddress+"("+portName+"),\n");
		for(int channel=0;channel<nChannels;channel++){
			data += ("\t\tcredit_ir"+router+"p"+port+"l"+channel+"  => credit_iN"+routerAddress+"("+portName+")(L"+(channel+1)+"),\n");
		}
		return data;
	}
	
	/**
	 * Return a connection between the CB SOR and a specific port of a router.
	 * @param routerAddress The router address (format XY).
	 * @param portName A String containing the port name. 
	 * @param router The number of router.
	 * @param port The number of port.
	 * @return A String containing the connection. 
	 */
	public static String getPortConnection_CB(String routerAddress, String portName,int router, int port) {
		String data = "";
		data += ("\t\ttx_r"+router+"p"+port+"         => txN"+routerAddress+"("+portName+"),\n");
		data += ("\t\tout_r"+router+"p"+port+"        => data_outN"+routerAddress+"("+portName+"),\n");
		data += ("\t\tcredit_ir"+router+"p"+port+"    => credit_iN"+routerAddress+"("+portName+"),\n");
		return data;
	}

	/**
	 * Return a connection between the HS SOR and a specific port of a router.
	 * @param routerAddress The router address (format XY).
	 * @param portName A String containing the port name. 
	 * @param router The number of router.
	 * @param port The number of port.
	 * @return A String containing the connection. 
	 */
	public static String getPortConnection_HS(String routerAddress, String portName,int router, int port) {
		String data = "";
		data += ("\t\ttx_r"+router+"p"+port+"         => txN"+routerAddress+"("+portName+"),\n");
		data += ("\t\tout_r"+router+"p"+port+"        => data_outN"+routerAddress+"("+portName+"),\n");
		data += ("\t\tack_ir"+router+"p"+port+"       => ack_txN"+routerAddress+"("+portName+"),\n");
		return data;
	}

	/**
	 * Create the outputModuleRouter C file.
	 * @param sourceDir The path of model file.
	 * @param scDir The path where the file will be created.
	 * @param nocType The NoC Type determines which ports are used. For instance: HermesTB.
	 * @param type Each type has a distinct group of signals. Types: CB, HS, VC. 
	 * @param dimX The X-dimension of NoC.
	 * @param dimY The Y-dimension of NoC.
	 * @param flitSize The number of bits of a flit.
	 * @param nChannels The number of virtual channels.
	 */
	public static void createFile(String sourceDir, String scDir,String nocType, int type, int dimX, int dimY, int flitSize, int nChannels){
		try{
			FileOutputStream outputFile = new FileOutputStream(scDir + "SC_OutputModuleRouter.h");
			DataOutputStream data_output=new DataOutputStream(outputFile);

			data_output.writeBytes(getFile(sourceDir, nocType, type, dimX, dimY, flitSize, nChannels));
			
			data_output.close();
			outputFile.close();
		}catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't write SC_OutputModuleRouter.h\n" + f.getMessage(),"Output error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}catch(Exception e){
			JOptionPane.showMessageDialog(null,"Can't write SC_OutputModuleRouter.h\n" + e.getMessage(), "Error Message", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}

	/**
	 * Return the SC_OutputModuleRouter file.
	 * @param sourceDir The path of model file.
	 * @param nocType The NoC Type determines which ports are used. For instance: HermesTB.
	 * @param type Each type has a distinct group of signals. Types: CB, HS, VC. 
	 * @param dimX The X-dimension of NoC.
	 * @param dimY The Y-dimension of NoC.
	 * @param flitSize The number of bits of a flit.
	 * @param nChannels The number of virtual channels.
	 * @return A String containing the SC_OutputModuleRouter file.
	 */
	public static String getFile(String sourceDir, String nocType, int type, int dimX, int dimY, int flitSize, int nChannels ){
		String line, word, data = "";
		StringTokenizer st;
		try{
			FileInputStream inputFile=new FileInputStream(new File(sourceDir + "SC_OutputModuleRouter.h"));
			BufferedReader buff=new BufferedReader(new InputStreamReader(inputFile));

			line=buff.readLine();
			while(line!=null){
				st = new StringTokenizer(line, "$");
				int nTokens = st.countTokens();
				for (int cont=0; cont<nTokens; cont++){
					word = st.nextToken();
					if(word.equalsIgnoreCase("NROT"))
						data += ""+(dimX*dimY);
					else if(word.equalsIgnoreCase("NLANE"))
						data += (""+nChannels+"\n");
					else if(word.equalsIgnoreCase("TFLIT"))
						data += ""+flitSize;
					else if(word.equalsIgnoreCase("WIDTH"))
						data += ""+dimX;
					else if(word.equalsIgnoreCase("HEIGHT"))
						data += ""+dimY;
					else if(word.equalsIgnoreCase("SIGNALS"))
						data += getSignals(nocType, type, dimX, dimY, nChannels);
					else if(word.equalsIgnoreCase("INCREDIT") || 
							word.equalsIgnoreCase("INACK") ||
							word.equalsIgnoreCase("INTX") ||
							word.equalsIgnoreCase("INLANETX") ||
							word.equalsIgnoreCase("INDATA"))
					{
						data += getTestSignals(nocType, type, word, dimX, dimY, nChannels);
					}
					else if(word.equalsIgnoreCase("VARIABLES"))
						data += getVariables(nocType, type, dimX, dimY, nChannels);
					else{
						data += word;
					}
				}//end for
				data += "\r\n";
				line=buff.readLine();
			} //end while
			buff.close();
			inputFile.close();
		}catch(Exception e){
			JOptionPane.showMessageDialog(null,"Can't write SC_OutputModuleRouter.h\n" + e.getMessage(), "Error Message", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		return data;
	}
	
	/**
	 * Return the OutModuleRouter test signals. <br>
	 * <i> This method is equal for all types: HS, CB and VC.</i>
	 * @param nocType The NoC type and the NoC dimensions determine which ports are used in each router.
	 * @param flag Flag determines which information is returned.
	 * @param type Each type has a distinct group of signals. Types: CB, HS, VC.
	 * @param dimX The X-dimension of NoC.
	 * @param dimY The Y-dimension of NoC.
	 * @param nChannels The number of virtual channels.
	 * @return A String containing the SC_OutputModuleRouter variables.
	 */
	public static String getTestSignals(String nocType, int type, String flag, int dimX, int dimY, int nChannels) {
		String data = "";
		
		if(nocType.equalsIgnoreCase(NoC.HERMESTU)){
			data += get1DTorusRouter(flag, type, dimX, dimY, nChannels); // HS is not considered
		}
		else if(nocType.equalsIgnoreCase(NoC.HERMESTB) || nocType.equalsIgnoreCase(NoC.MERCURY)){
			data += get2DTorusRouter(flag, type, dimX, dimY, nChannels); // HS is not considered
		}
		else{
			data += get2DMeshRouter(flag, type, dimX, dimY, nChannels); // HS is not considered
		}
		data = Convert.removeLast(data,"\n");
		return data;
	}

	
	/**
	 * Return the OutModuleRouter variables.
	 * @param nocType The NoC type and the NoC dimensions determine which ports are used in each router.
	 * @param type Each type has a distinct group of signals. Types: CB, HS, VC. 
	 * @param dimX The X-dimension of NoC.
	 * @param dimY The Y-dimension of NoC.
	 * @param nChannels The number of virtual channels.
	 * @return A String containing the SC_OutputModuleRouter variables.
	 */
	public static String getVariables(String nocType, int type, int dimX, int dimY, int nChannels) {
		String data = "";
		String flag = "variables";
		
		if(nocType.equalsIgnoreCase(NoC.HERMESTU)){
			data += get1DTorusRouter(flag, type, dimX, dimY, nChannels);
		}
		else if(nocType.equalsIgnoreCase(NoC.HERMESTB) || nocType.equalsIgnoreCase(NoC.MERCURY)){
			data += get2DTorusRouter(flag, type, dimX, dimY, nChannels);
		}
		else{
			data += get2DMeshRouter(flag, type, dimX, dimY, nChannels);
		}
		data = Convert.removeLast(data,"\n");
		return data;
	}

	/**
	 * Return the OutModuleRouter signals.
	 * @param nocType The NoC type and the NoC dimensions determine which ports are used in each router.
	 * @param type Each type has a distinct group of signals. Types: CB, HS, VC.
	 * @param dimX The X-dimension of NoC.
	 * @param dimY The Y-dimension of NoC.
	 * @param nChannels The number of virtual channels.
	 * @return A String containing the SC_OutputModuleRouter signals.
	 */
	public static String getSignals(String nocType, int type, int dimX, int dimY, int nChannels) {
		String data = "";
		String flag = "signals";
		if(nocType.equalsIgnoreCase(NoC.HERMESTU)){
			data += get1DTorusRouter(flag, type, dimX, dimY, nChannels);
		}
		else if(nocType.equalsIgnoreCase(NoC.HERMESTB) || nocType.equalsIgnoreCase(NoC.MERCURY)){
			data += get2DTorusRouter(flag, type, dimX, dimY, nChannels);
		}
		else{
			data += get2DMeshRouter(flag, type, dimX, dimY, nChannels);
		}
		data = Convert.removeLast(data,"\n");
		return data;
	}
	
	/**
	 * Return a SOR data of a specific 1D TORUS router according to the informed flag.
	 * @param flag Flag determines which information is returned.
	 * @param type Each type has a distinct group of signals. Types: CB, HS, VC. 
	 * @param dimX The X-dimension of NoC.
	 * @param dimY The Y-dimension of NoC.
	 * @param nChannels The number of virtual channels.
	 * @return A String containing the data requested by flag. 
	 */
	public static String get1DTorusRouter(String flag, int type, int dimX, int dimY, int nChannels) {
		int router;
		String data = "";
				
		for(int y =0; y < dimY; y++){
			for(int x =0; x < dimX; x++){
				router = y * dimX + x;
				if(isTestSignal(flag)){
					if(router==0)
						data += ("\t\t");
					else
						data += ("\t\telse ");
					data += ("if (Roteador == "+router+"){\n");
					data += getPort(flag, type, nChannels, router, Router.EAST);
					data += getPort(flag, type, nChannels, router, Router.NORTH);
					data += ("\t\t}\n");
				}
				else{
					data += getPort(flag, type, nChannels, router, Router.EAST);
					data += getPort(flag, type, nChannels, router, Router.NORTH);
				}
			}
		}
		return data;
	}

	
	/**
	 * Return a SOR data of a specific 2D TORUS router according to the informed flag.
	 * @param flag Flag determines which information is returned.
	 * @param type Each type has a distinct group of signals. Types: CB, HS, VC. 
	 * @param dimX The X-dimension of NoC.
	 * @param dimY The Y-dimension of NoC.
	 * @param nChannels The number of virtual channels.
	 * @return A String containing the data requested by flag. 
	 */
	public static String get2DTorusRouter(String flag, int type, int dimX, int dimY, int nChannels) {
		int router;
		String data = "";
				
		for(int y =0; y < dimY; y++){
			for(int x =0; x < dimX; x++){
				router = y * dimX + x;
				if(isTestSignal(flag)){
					if(router==0)
						data += ("\t\t");
					else
						data += ("\t\telse ");
					data += ("if (Roteador == "+router+"){\n");
				}
				for(int port=0; port < (Router.NPORTS-1); port++){
					data += getPort(flag, type, nChannels, router, port);
				}
				if(isTestSignal(flag)){
					data += ("\t\t}\n");
				}
			}
		}
		return data;
	}
	
	/**
	 * Return a SOR data of a specific 2D MESH router according to the informed flag.
	 * @param flag Flag determines which information is returned.
	 * Return the variables to a specific Mesh 2D router and port.
	 * @param type Each type has a distinct group of signals. Types: CB, HS, VC. 
	 * @param dimX The X-dimension of NoC.
	 * @param dimY The Y-dimension of NoC.
	 * @param nChannels The number of virtual channels.
	 * @return A String containing the data requested by flag. 
	 */
	public static String get2DMeshRouter(String flag, int type, int dimX, int dimY, int nChannels) {
		int router;
		String data = "";
				
		for(int y =0; y < dimY; y++){
			for(int x =0; x < dimX; x++){
				router = y * dimX + x;
				if(isTestSignal(flag)){
					if(router==0)
						data += ("\t\t");
					else
						data += ("\t\telse ");
					data += ("if (Roteador == "+router+"){\n");
				}				
				for(int port=0; port < (Router.NPORTS-1); port++){
					if(Router.hasPort(port, x, y, dimX, dimY)){
						data += getPort(flag, type, nChannels, router, port);
					}
				}
				if(isTestSignal(flag)){
					data += ("\t\t}\n");
				}
			}
		}
		return data;
	}
	
	/**
	 * Return a SOR data of a specific router and port according to the informed flag.
	 * @param flag Flag determines which information is returned.
	 * @param type Each type has a distinct group of signals. Types: CB, HS, VC 
	 * @param nChannels The number of virtual channels.
	 * @param router The number of router.
	 * @param port The number of port.
	 * @return A String containing the data requested by flag. 
	 */
	public static String getPort(String flag, int type, int nChannels, int router, int port) {
		String data="";
		if(flag.equalsIgnoreCase("variables")){			
			switch(type){
			case NoC.HS: return getPortVariables_HS(router, port); 
			case NoC.CB: return getPortVariables_CB(router, port); 
			case NoC.VC: return getPortVariables_VC(nChannels, router, port);
			default:
				JOptionPane.showMessageDialog(null,"Indefined type : "+ type+"\nError in SCOutputModuleRouter.getPortConnection","Error Mesage" ,JOptionPane.ERROR_MESSAGE);
				System.exit(0);
				break;
			}
		}
		else if(flag.equalsIgnoreCase("signals")){			
			switch(type){
			case NoC.HS: return getPortSignals_HS(router, port); 
			case NoC.CB: return getPortSignals_CB(router, port); 
			case NoC.VC: return getPortSignals_VC(nChannels, router, port);
			default:
				JOptionPane.showMessageDialog(null,"Indefined type : "+ type+"\nError in SCOutputModuleRouter.getPortConnection","Error Mesage" ,JOptionPane.ERROR_MESSAGE);
				System.exit(0);
				break;
			}
		}else if(flag.equalsIgnoreCase("INCREDIT")){
			if(type == NoC.VC)
				return getTestPortSignal_VC("credit_i", router, port, nChannels);
			else
				return getTestPortSignal("credit_i",router, port);
		}
		else if(flag.equalsIgnoreCase("INACK"))
			return getTestPortSignal("ack_i", router, port);
		else if(flag.equalsIgnoreCase("INLANETX"))
			return getTestPortSignal_VC("lane_tx_",router, port, nChannels);
		else if(flag.equalsIgnoreCase("INTX"))
			return getTestPortSignal("tx_",router, port);
		else if(flag.equalsIgnoreCase("INDATA"))
			return getTestPortInData(router, port);
		return data;
	}

	/**
	 * Return true if the informed flag represents a test signal. 
	 * @param flag
	 * @return True if the informed flag represents a test signal.
	 */
	private static boolean isTestSignal(String flag){
		if(flag.equalsIgnoreCase("INCREDIT") || flag.equalsIgnoreCase("INLANETX") || 
		   flag.equalsIgnoreCase("INTX") || flag.equalsIgnoreCase("INDATA") ||
		   flag.equalsIgnoreCase("INACK"))
			return true;
		return false;
	}

	/**
	 * Return the SOR variables of a specific <i>VC</i> router and port.
	 * @param nChannels The number of Virtual channels.
	 * @param router The number of router.
	 * @param port The number of port.
	 * @return A String the SOR variables to a specific router and port.
	 */
	public static String getPortVariables_VC(int nChannels, int router, int port){
		String data = "";
		data += ("\ttx_r"+router+"p"+port+"(\"tx_r"+router+"p"+port+"\"),\n");
		for (int i=0; i<nChannels; i++){
			data += ("\tlane_tx_r"+router+"p"+port+"l"+i+"(\"lane_tx_r"+router+"p"+port+"l"+i+"\"),\n");
		}
		data += ("\tout_r"+router+"p"+port+"(\"out_r"+router+"p"+port+"\"),\n");
		for (int i=0; i<nChannels; i++){
			data += ("\tcredit_ir"+router+"p"+port+"l"+i+"(\"credit_ir"+router+"p"+port+"l"+i+"\"),\n");
		}
		return data;
	}

	/**
	 * Return the SOR variables of a specific <i>CB</i> router and port.
	 * @param router The number of router.
	 * @param port The number of port.
	 * @return A String the SOR variables to a specific router and port.
	 */
	public static String getPortVariables_CB(int router, int port){
		String data = "";
		data += ("\ttx_r"+router+"p"+port+"(\"tx_r"+router+"p"+port+"\"),\n");
		data += ("\tout_r"+router+"p"+port+"(\"out_r"+router+"p"+port+"\"),\n");
		data += ("\tcredit_ir"+router+"p"+port+"(\"credit_ir"+router+"p"+port+"\"),\n");
		return data;
	}

	/**
	 * Return the SOR variables of a specific <i>HS</i> router and port.
	 * @param router The number of router.
	 * @param port The number of port.
	 * @return A String the SOR variables to a specific router and port.
	 */
	public static String getPortVariables_HS(int router, int port){
		String data = "";
		data += ("\ttx_r"+router+"p"+port+"(\"tx_r"+router+"p"+port+"\"),\n");
		data += ("\tout_r"+router+"p"+port+"(\"out_r"+router+"p"+port+"\"),\n");
		data += ("\tack_ir"+router+"p"+port+"(\"ack_ir"+router+"p"+port+"\"),\n");
		return data;
	}

	/**
	 * Return the SOR signals for a specific <i>VC</i> router and port.
	 * @param nChannels The number of Virtual channels.
	 * @param router The number of router.
	 * @param port The number of port.
	 * @return A String the SOR signals to a specific router and port.
	 */
	public static String getPortSignals_VC(int nChannels, int router, int port){
		String data = "";

		data += ("\tsc_in<sc_logic> tx_r"+router+"p"+port+";\n");
		for (int i=0; i<nChannels; i++){
			data += ("\tsc_in<sc_logic> lane_tx_r"+router+"p"+port+"l"+i+";\n");
		}
		data += ("\tsc_in<sc_lv<constFlitSize> > out_r"+router+"p"+port+";\n");
		for (int i=0; i<nChannels; i++){
			data += ("\tsc_in<sc_logic> credit_ir"+router+"p"+port+"l"+i+";\n");
		}
		
		return data;
	}

	/**
	 * Return the SOR signals for a specific <i>CB</i> router and port.
	 * @param router The number of router.
	 * @param port The number of port.
	 * @return A String the SOR signals to a specific router and port.
	 */
	public static String getPortSignals_CB(int router, int port){
		String data = "";

		data += ("\tsc_in<sc_logic> tx_r"+router+"p"+port+";\n");
		data += ("\tsc_in<sc_lv<constFlitSize> > out_r"+router+"p"+port+";\n");
		data += ("\tsc_in<sc_logic> credit_ir"+router+"p"+port+";\n");
		
		return data;
	}

	/**
	 * Return the SOR signals for a specific <i>HS</i> router and port.
	 * @param router The number of router.
	 * @param port The number of port.
	 * @return A String the SOR signals to a specific router and port.
	 */
	public static String getPortSignals_HS(int router, int port){
		String data = "";

		data += ("\tsc_in<sc_logic> tx_r"+router+"p"+port+";\n");
		data += ("\tsc_in<sc_lv<constFlitSize> > out_r"+router+"p"+port+";\n");
		data += ("\tsc_in<sc_logic> ack_ir"+router+"p"+port+";\n");

		return data;
	}

	/**
	 * Return the test in the InData signal of a specific router and port.
	 * @param router The number of router.
	 * @param port The number of port.
	 * @return the test in the InData signal.
	 */
	public static String getTestPortInData( int router, int port) {
		return ("\t\t\tif(Porta == "+port+") return out_r"+router+"p"+port+".read().to_uint();\n");
	}
	
	/**
	 * Return the test in the informed signal of a specific router and port.
	 * @param signal The name of signal.
	 * @param router The number of router.
	 * @param port The number of port.
	 * @return the test in the informed signal.
	 */
	public static String getTestPortSignal(String signal, int router, int port) {
		return ("\t\t\tif(Porta == "+port+") return ("+signal+"r"+router+"p"+port+"==SC_LOGIC_1)? 1 : 0;\n");
	}	

	/**
	 * Return the test in the informed signal of a specific router and port.
	 * @param signal The name of signal.
	 * @param router The number of router.
	 * @param port The number of port.
	 * @param nChannels The number of virtual channels.
	 * @return the test in the informed signal.
	 */
	public static String getTestPortSignal_VC(String signal, int router, int port, int nChannels) {
		String data = "";
		data += ("\t\t\tif (Porta == "+port+"){\n");
		for (int channel=0; channel<nChannels; channel++){
			data += ("\t\t\t\tif(Canal == "+channel+") return ("+signal+"r"+router+"p"+port+"l"+channel+"==SC_LOGIC_1)? 1 : 0;\n");
		}
		data += ("\t\t\t}\n");
		return data;
	}
	
	/**
	 * Used to test SCOutputModuleRouter methods.
	 * @param args The list of arguments
	 */
	public static void main(String args[]){
		System.out.println(SCOutputModuleRouter.getPortMap(NoC.HERMES, NoC.HS, 2, 2, 16, 4));
//		System.out.println(SCOutputModuleRouter.getPortMap(NoC.HERMESTB, NoC.CB, 3, 3, 16, 4));
//		System.out.println(SCOutputModuleRouter.getPortMap(NoC.HERMES, NoC.VC, 3, 3, 16, 4));
//		System.out.println(SCOutputModuleRouter.getVariables(NoC.HERMESTB, NoC.HS, 3, 3, 4));
//		System.out.println(SCOutputModuleRouter.getSignals(NoC.HERMES, NoC.CB, 3, 3, 4));
//		System.out.println(SCOutputModuleRouter.getTestSignals(NoC.HERMESTB, NoC.VC, "INLANETX", 3, 3, 4));
//		
//		String hsSourceDir = Default.atlashome + File.separator + "Maia" + File.separator + "Data" + File.separator + "Handshake" + File.separator;
//		String cbSourceDir = Default.atlashome + File.separator + "Maia" + File.separator + "Data" + File.separator + "CreditBased" + File.separator;
//		String vcSourceDir = Default.atlashome + File.separator + "Maia" + File.separator + "Data" + File.separator + "VirtualChannel" + File.separator + "RoundRobin" + File.separator;
//		String tuSourceDir = Default.atlashome + File.separator + "HermesTU" + File.separator + "Data" + File.separator + "VirtualChannel" + File.separator + "RoundRobin" + File.separator;
//		String tbSourceDir = Default.atlashome + File.separator + "HermesTB" + File.separator + "Data" + File.separator + "CreditBased" + File.separator;
//
//		String scDir = 	new File(Default.atlashome).getParent() + File.separator;
//
//		System.out.println(SCOutputModuleRouter.getFile(hsSourceDir, NoC.HERMES, NoC.HS, 3, 3, 16, 1));
//		SCOutputModuleRouter.createFile(hsSourceDir, scDir, NoC.HERMES, NoC.HS, 3, 3, 16, 1);
//
//		System.out.println(SCOutputModuleRouter.getFile(cbSourceDir, NoC.HERMES, NoC.CB, 3, 3, 16, 1));
//		SCOutputModuleRouter.createFile(cbSourceDir, scDir, NoC.HERMES, NoC.CB, 3, 3, 16, 1);
//		
//		System.out.println(SCOutputModuleRouter.getFile(vcSourceDir, NoC.HERMES, NoC.VC, 3, 3, 16, 4));
//		SCOutputModuleRouter.createFile(vcSourceDir, scDir, NoC.HERMES, NoC.VC, 3, 3, 16, 4);
//
//		System.out.println(SCOutputModuleRouter.getFile(tbSourceDir, NoC.HERMESTB, NoC.CB, 3, 3, 16, 1));
//		SCOutputModuleRouter.createFile(tbSourceDir, scDir, NoC.HERMESTB, NoC.CB, 3, 3, 16, 1);
//
//		System.out.println(SCOutputModuleRouter.getFile(tuSourceDir, NoC.HERMESTU, NoC.VC, 3, 3, 16, 2));
//		SCOutputModuleRouter.createFile(tuSourceDir, scDir,NoC.HERMESTU, NoC.VC, 3, 3, 16, 2);
	}
	
}