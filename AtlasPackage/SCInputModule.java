package AtlasPackage;

import javax.swing.*;

import java.io.*;
import java.util.*;

/**
 * SCInputModule has methods to generate the SC_InputModule port map and file.
 * @author Aline Vieira de Mello
 * @version
 */
public class SCInputModule{

	/**
	 * Create the SC_InputModule file.
	 * @param type Each type has a distinct group of signals. Types: CB, HS, VC. 
	 * @param sourceDir The path of model file.
	 * @param scDir The path where the file will be created.
	 * @param dimX The X-dimension of NoC.
	 * @param dimY The Y-dimension of NoC.
	 * @param flitSize The number of bits of a flit.
	 * @param nChannels The number of virtual channels.
	 */
	public static void createFile(int type, String sourceDir, String scDir, int dimX, int dimY, int flitSize, int nChannels){
		try{
			FileOutputStream outputFile  = new FileOutputStream(scDir + "SC_InputModule.h");
			DataOutputStream data_output = new DataOutputStream(outputFile);

			data_output.writeBytes(getFile(type, sourceDir, dimX, dimY, flitSize, nChannels));
		
			data_output.close();
			outputFile.close();
		}catch(Exception e){
			JOptionPane.showMessageDialog(null,"Can't write SC_InputModule.h\n"+e.getMessage(),"Error Message", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}

	/**
	 * Return the SC_InputModule file.
	 * @param type Each type has a distinct group of signals. Types: CB, HS, VC. 
	 * @param sourceDir The path of model file.
	 * @param dimX The X-dimension of NoC.
	 * @param dimY The Y-dimension of NoC.
	 * @param flitSize The number of bits of a flit.
	 * @param nChannels The number of virtual channels.
	 * @return A String containing the SC_InputModule file.
	 */
	public static String getFile(int type, String sourceDir, int dimX, int dimY, int flitSize, int nChannels){
		String line, word, data = "";
		StringTokenizer st;
		int nRouters = (dimX * dimY);

		try{
			FileInputStream inputFile=new FileInputStream(new File(sourceDir + "SC_InputModule.h"));
			BufferedReader buff=new BufferedReader(new InputStreamReader(inputFile));

			line=buff.readLine();
			while(line!=null){
				st = new StringTokenizer(line, "$");
				int nTokens = st.countTokens();
				for (int cont=0; cont<nTokens; cont++){
					word = st.nextToken();
					if(word.equalsIgnoreCase("NROT")){
						data += (""+nRouters);
					}
					else if(word.equalsIgnoreCase("NLANE"))
						data += (""+nChannels+"\n");
					else if(word.equalsIgnoreCase("TFLIT")){
						data += (""+flitSize);
					}
					else if(word.equalsIgnoreCase("WIDTH")){
						data += (""+dimX);
					}
					else if(word.equalsIgnoreCase("HEIGHT")){
						data += (""+dimY);
					}
					else if(word.equalsIgnoreCase("OUTTX")){
						data += getOutTxTest(nRouters);
					}
					else if(word.equalsIgnoreCase("INACK")){
						data += getInAckTest(nRouters);
					}
					else if(word.equalsIgnoreCase("LANETX")){
						data += getInLaneTxTest(nRouters);
					}
					else if(word.equalsIgnoreCase("OUTDATA")){
						data += getOutDataTest(nRouters);
					}
					else if(word.equalsIgnoreCase("INCREDIT")){
						data += getInCreditTest(type, nRouters, nChannels);
					}
					else if(word.equalsIgnoreCase("SIGNALS")){
						data += getSignalsDeclaration(type, nRouters);
					}
					else if(word.equalsIgnoreCase("VARIABLES")){
						data += getSignalsConnection(type, nRouters);
					}
					else if(word.equalsIgnoreCase("INPUTMODULE")){
						data += getOutClock(nRouters);
					}
					else {
						data += word;
					}
				}//end for
				data += "\n";
				line=buff.readLine();
			} //end while
			buff.close();
			inputFile.close();
		}catch(Exception e){
			JOptionPane.showMessageDialog(null,"Can't write SC_InputModule.h\n"+e.getMessage(),"Error Message", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		return data;
	}
	
	/**
	 * Return the OutTx signal test of all routers.
	 * <i>This methods is used equal for all types: HS, CB and VC.</i>
	 * @param nRouters The number of routers.
	 * @return A String containing the OutTx signal test of all routers.
	 */
	public static String getOutTxTest(int nRouters) {
		String data = "";
		for (int router=0; router<nRouters; router++){
			if(router==0)
				data += ("\t\t");
			else
				data += ("\t\telse ");
			data += ("if(Indice == "+router+") outtx"+router+" = (Booleano != 0)? SC_LOGIC_1: SC_LOGIC_0;\n");
		}
		data = Convert.removeLast(data,"\n");
		return data;
	}

	/**
	 * Return the InAck signal test of all routers.
	 * <i>This methods is used only by HS. The CB and VC does not have the InAck signal.</i>
	 * @param nRouters The number of routers.
	 * @return A String containing the InAck signal test of all routers.
	 */
	public static String getInAckTest(int nRouters) {
		String data = "";
		for (int router=0; router<nRouters; router++){
			data += ("\t\tif(Indice == "+router+") return (inack"+router+" == SC_LOGIC_1)? 1 : 0;\n");
		}
		data = Convert.removeLast(data,"\n");
		return data;
	}

	/**
	 * Return the InLaneTx signal test of all routers.
	 * <i>This methods is used only by VC. The CB and HS does not have the InLaneTx signal.</i>
	 * @param nRouters The number of routers.
	 * @return A String containing the InLaneTx signal test of all routers.
	 */
	public static String getInLaneTxTest(int nRouters) {
		String data = "";
		for (int router=0; router<nRouters; router++){
			if(router==0)
				data += ("\t\t");
			else
				data += ("\t\telse ");
			data += ("if(Indice == "+router+") lane_tx"+router+" = Valor;\n");
		}
		data = Convert.removeLast(data,"\n");
		return data;
	}
	
	/**
	 * Return the OutData signal test of all routers.<br>
	 * <i>This method is used by HS, CB and VC.</i>
	 * @param nRouters The number of routers.
	 * @return A String containing the OutData signal test of all routers.
	 */
	public static String getOutDataTest(int nRouters) {
		String data = "";
		for (int router=0; router<nRouters; router++){
			if(router==0)
				data += ("\t\t");
			else
				data += ("\t\telse ");
			data += ("if(Indice == "+router+") outdata"+router+" = Valor;\n");
		}
		data = Convert.removeLast(data,"\n");
		return data;
	}
	
	/**
	 * Return the InCredit signal test of all routers. <br>
	 * @param type Each type has a distinct group of signals. Types: CB, HS, VC 
	 * @param nRouters The number of routers.
	 * @param nChannels The number of virtual channels.
	 * @return A String containing the InCredit signal test of all routers.
	 */
	public static String getInCreditTest(int type, int nRouters, int nChannels) {
		String data = "";
		switch(type){
		case NoC.CB: data += getInCreditTest_CB(nRouters); break;
		case NoC.VC: data += getInCreditTest_VC(nRouters, nChannels); break;
		default:
			JOptionPane.showMessageDialog(null,"Indefined type : "+ type+"\nError in SCInputModule.getTestInCredit","Error Mesage" ,JOptionPane.ERROR_MESSAGE);
			System.exit(0);
			break;
		}
		data = Convert.removeLast(data,"\n");
		return data;
	}

	/**
	 * Return the <VC> InCredit signal test of all routers.
	 * @param nRouters The number of routers.
	 * @param nChannels The number of virtual channels.
	 * @return A String containing the <VC> InCredit signal test of all routers.
	 */
	public static String getInCreditTest_VC(int nRouters, int nChannels) {
		String data = "";
		for (int router=0; router<nRouters; router++){
			data += ("\t\tif(Indice == "+router+"){\n");
			for (int channel=0; channel<nChannels; channel++){
				data += ("\t\t\tif(Lane == "+channel+") return (incredit"+router+".read().get_bit("+channel+") == SC_LOGIC_1)? 1 : 0;\n");
			}
			data += ("\t\t}\n");
		}
		return data;
	}	

	/**
	 * Return the <CB> InCredit signal test of all routers.
	 * @param nRouters The number of routers.
	 * @return A String containing the <CB> InCredit signal test of all routers.
	 */
	public static String getInCreditTest_CB(int nRouters) {
		String data = "";
		for (int router=0; router<nRouters; router++){
			data += ("\t\tif(Indice == "+router+") return (incredit"+router+" == SC_LOGIC_1)? 1 : 0;\n");
		}
		return data;
	}	
	
	/**
	 * Return the declaration of signals of all routers.
	 * @param type Each type has a distinct group of signals. Types: CB, HS, VC 
	 * @param nRouters The number of routers.
	 * @return A String containing the declaration of signals of all routers.
	 */
	public static String getSignalsDeclaration(int type, int nRouters) {
		String data = "";
		switch(type){
		case NoC.HS: data += getSignalsDeclaration_HS(nRouters); break;
		case NoC.CB: data += getSignalsDeclaration_CB(nRouters); break;
		case NoC.VC: data += getSignalsDeclaration_VC(nRouters); break;
		default:
			JOptionPane.showMessageDialog(null,"Indefined type : "+ type+"\nError in SCInputModule.getSignalsDeclaration","Error Mesage" ,JOptionPane.ERROR_MESSAGE);
			System.exit(0);
			break;
		}
		data = Convert.removeLast(data,"\n");
		return data;
	}
	
	/**
	 * Return the declaration of <i>VC</i> signals of all routers.
	 * @param nRouters The number of routers.
	 * @return A String containing the declaration of <i>VC</i> signals of all routers.
	 */
	public static String getSignalsDeclaration_VC(int nRouters) {
		String data = "";
		for (int router=0; router<nRouters; router++){
			data += ("\tsc_out<sc_logic> outclock"+router+";\n");
			data += ("\tsc_out<sc_logic> outtx"+router+";\n");
			data += ("\tsc_out<sc_lv<constNumLane> > lane_tx"+router+";\n");
			data += ("\tsc_out<sc_lv<constFlitSize> > outdata"+router+";\n");
			data += ("\tsc_in<sc_lv<constNumLane> > incredit"+router+";\n");
		}
		return data;
	}

	/**
	 * Return the declaration of <i>CB</i> signals of all routers.
	 * @param nRouters The number of routers.
	 * @return A String containing the declaration of <i>CB</i> signals of all routers.
	 */
	public static String getSignalsDeclaration_CB(int nRouters) {
		String data = "";
		for (int router=0; router<nRouters; router++){
			data += ("\tsc_out<sc_logic> outclock"+router+";\n");
			data += ("\tsc_out<sc_logic> outtx"+router+";\n");
			data += ("\tsc_out<sc_lv<constFlitSize> > outdata"+router+";\n");
			data += ("\tsc_in<sc_logic> incredit"+router+";\n");
		}
		return data;
	}

	/**
	 * Return the declaration of <i>HS</i> signals of all routers.
	 * @param nRouters The number of routers.
	 * @return A String containing the declaration of <i>HS</i> signals of all routers.
	 */
	public static String getSignalsDeclaration_HS(int nRouters) {
		String data = "";
		for (int router=0; router<nRouters; router++){
			data += ("\tsc_out<sc_logic> outtx"+router+";\n");
			data += ("\tsc_out<sc_lv<constFlitSize> > outdata"+router+";\n");
			data += ("\tsc_in<sc_logic> inack"+router+";\n");
		}
		return data;
	}

	/**
	 * Return the connection of signals of all routers.
	 * @param type Each type has a distinct group of signals. Types: CB, HS, VC 
	 * @param nRouters The number of routers.
	 * @return A String containing the connection of signals of all routers.
	 */
	public static String getSignalsConnection(int type, int nRouters) {
		String data = "";
		switch(type){
		case NoC.HS: data += getSignalsConnection_HS(nRouters); break;
		case NoC.CB: data += getSignalsConnection_CB(nRouters); break;
		case NoC.VC: data += getSignalsConnection_VC(nRouters); break;
		default:
			JOptionPane.showMessageDialog(null,"Indefined type : "+ type+"\nError in SCInputModule.getSignalsConnection","Error Mesage" ,JOptionPane.ERROR_MESSAGE);
			System.exit(0);
			break;
		}
		data = Convert.removeLast(data,"\n");
		return data;
	}
		
	/**
	 * Return the connection of <i>VC</i> signals of all routers.
	 * @param nRouters The number of routers.
	 * @return A String containing the connection of <i>VC</i> signals of all routers.
	 */
	public static String getSignalsConnection_VC(int nRouters) {
		String data="";
		for (int router=0; router<nRouters; router++){
			data += ("\toutclock"+router+"(\"outclock"+router+"\"),\n");
			data += ("\touttx"+router+"(\"outtx"+router+"\"),\n");
			data += ("\tlane_tx"+router+"(\"lane_tx"+router+"\"),\n");
			data += ("\toutdata"+router+"(\"outdata"+router+"\"),\n");
			data += ("\tincredit"+router+"(\"incredit"+router+"\"),\n");
		}
		return data;
	}	

	/**
	 * Return the connection of <i>CB</i> signals of all routers.
	 * @param nRouters The number of routers.
	 * @return A String containing the connection of <i>CB</i> signals of all routers.
	 */
	public static String getSignalsConnection_CB(int nRouters) {
		String data="";
		for (int router=0; router<nRouters; router++){
			data += ("\toutclock"+router+"(\"outclock"+router+"\"),\n");
			data += ("\touttx"+router+"(\"outtx"+router+"\"),\n");
			data += ("\toutdata"+router+"(\"outdata"+router+"\"),\n");
			data += ("\tincredit"+router+"(\"incredit"+router+"\"),\n");
		}
		return data;
	}	

	/**
	 * Return the connection of <i>HS</i> signals of all routers.
	 * @param nRouters The number of routers.
	 * @return A String containing the connection of <i>HS</i> signals of all routers.
	 */
	public static String getSignalsConnection_HS(int nRouters) {
		String data="";
		for (int router=0; router<nRouters; router++){
			data += ("\touttx"+router+"(\"outtx"+router+"\"),\n");
			data += ("\toutdata"+router+"(\"outdata"+router+"\"),\n");
			data += ("\tinack"+router+"(\"inack"+router+"\"),\n");
		}
		return data;
	}	
	
	/**
	 * Return the OutClock signal of all routers.<br>
	 * <i>This method is used by CB and VC. HS does not have the OutClock signal.</i> 
	 * @param nRouters The number of routers.
	 * @return A String containing the OutClock signal of all routers.
	 */
	public static String getOutClock(int nRouters){
		String data="";
		for (int router=0; router<nRouters; router++){
			data += ("\toutclock"+router+" = clock;\n");
		}
		data = Convert.removeLast(data, "\n");
		return data;
	}

	
	/**
	 * Return the SC_InputModule port map. (This method is used in topNoC file).
 	 * @param type Each type has a distinct group of signals. Types: CB, HS, VC 
	 * @param dimX The X-dimension of NoC.
	 * @param dimY The Y-dimension of NoC.
	 * @param flitSize The number of bits of a flit.
	 * @return A String containing the SC_InputModule port map.
	 */
	public static String getPortMap(int type, int dimX, int dimY, int flitSize) {
		String xHexa, yHexa, data = "";
		int router, nbytes = (flitSize/8);
		String routerName = "N"+ Convert.decToHex(0, nbytes) + Convert.decToHex(0, nbytes);
		
		data += ("\tcim00: Entity work.inputmodule\n");
		data += ("\tport map(\n");
		if(type == NoC.HS)
			data += ("\t\tclock       => clk,\n");
		else
			data += ("\t\tclock       => clock(" + routerName + "),\n");
		
		data += ("\t\treset       => reset,\n");
		data += ("\t\tfinish      => finish,\n");
	
		for (int y=0; y<dimY; y++){
			yHexa = Convert.decToHex(y, nbytes);
			for (int x=0; x<dimX; x++){
				xHexa = Convert.decToHex(x, nbytes);
				routerName = "N"+xHexa+yHexa;
				router = y * dimX + x;
				switch(type){
				case NoC.HS: data += getPortMap_HS(routerName, router); break;
				case NoC.CB: data += getPortMap_CB(routerName, router); break;
				case NoC.VC: data += getPortMap_VC(routerName, router); break;
				default:
					JOptionPane.showMessageDialog(null,"Indefined type : "+ type+"\nError in SCInputModule.getPortMap","Error Mesage" ,JOptionPane.ERROR_MESSAGE);
					System.exit(0);
					break;
				}
			}
		}
		
		data = Convert.removeLast(data, ",");
		data += (");\n\n");
		return data;
	}	

	/**
	 * Return the SC_InputModule port map for a specific <i>VC</i> router.
	 * @param routerName The router name. For instance: N0000.
	 * @param router The number of router.
	 * @return A String containing the SC_InputModule port map for a specific <i>VC</i> router.
	 */
	public static String getPortMap_VC(String routerName, int router) {
		String data = "";
		data += ("\t\toutclock" + router + "   => clock_rx("+routerName+"),\n");
		data += ("\t\touttx" + router + "      => rx("+routerName+"),\n");
		data += ("\t\tlane_tx" + router + "    => lane_rx("+routerName+"),\n");
		data += ("\t\toutdata" + router + "    => data_in("+routerName+"),\n");
		data += ("\t\tincredit" + router + "   => credit_o("+routerName+"),\n");
		return data;
	}	
	
	/**
	 * Return the SC_InputModule port map for a specific <i>CB</i> router.
	 * @param routerName The router name. For instance: N0000.
	 * @param router The number of router.
	 * @return A String containing the SC_InputModule port map for a specific <i>CB</i> router.
	 */
	public static String getPortMap_CB(String routerName, int router) {
		String data = "";
		data += ("\t\toutclock" + router + "   => clock_rx("+routerName+"),\n");
		data += ("\t\touttx" + router + "      => rx("+routerName+"),\n");
		data += ("\t\toutdata" + router + "    => data_in("+routerName+"),\n");
		data += ("\t\tincredit" + router + "   => credit_o("+routerName+"),\n");
		return data;
	}	

	/**
	 * Return the SC_InputModule port map for a specific <i>HS</i> router.
	 * @param routerName The router name. For instance: N0000.
	 * @param router The number of router.
	 * @return A String containing the SC_InputModule port map for a specific <i>HS</i> router.
	 */
	public static String getPortMap_HS(String routerName, int router) {
		String data = "";
		data += ("\t\touttx" + router + "      => rx("+routerName+"),\n");
		data += ("\t\toutdata" + router + "    => data_in("+routerName+"),\n");
		data += ("\t\tinack" + router + "      => ack_rx("+routerName+"),\n");
		return data;
	}	
	
	/**
	 * Used to test SCOutputModuleRouter methods.
	 * @param args The list of arguments
	 */
	public static void main(String args[]){
//		System.out.println(SCInputModule.getOutClock(9));
//		System.out.println(SCInputModule.getSignalsConnection_VC(9));
//		System.out.println(SCInputModule.getInCreditTest(NoC.VC, 9, 4));
		System.out.println(SCInputModule.getPortMap(NoC.HS, 4, 4, 16));
		
//		String scDir = new File(Default.atlashome).getParent() + File.separator;
//		String hsSourceDir = Default.atlashome + File.separator + "Maia" + File.separator + "Data" + File.separator + "Handshake" + File.separator;
//		String cbSourceDir = Default.atlashome + File.separator + "Maia" + File.separator + "Data" + File.separator + "CreditBased" + File.separator;
//		String vcSourceDir = Default.atlashome + File.separator + "Maia" + File.separator + "Data" + File.separator + "VirtualChannel" + File.separator + "RoundRobin" + File.separator;

//		System.out.println(SCInputModule.getFile(NoC.HS, hsSourceDir, 3, 3, 16, 1));
//		SCInputModule.createFile(NoC.HS, hsSourceDir, scDir, 4, 4, 16, 1);

//		System.out.println(SCInputModule.getFile(NoC.CB, cbSourceDir, 3, 3, 16, 1));
//		SCInputModule.createFile(NoC.CB, cbSourceDir, scDir, 3, 3, 16, 1);

//		System.out.println(SCInputModule.getFile(NoC.VC, vcSourceDir, 3, 3, 16, 2));
//		SCInputModule.createFile(NoC.VC, vcSourceDir, scDir, 3, 3, 16, 2);
	
	}
}