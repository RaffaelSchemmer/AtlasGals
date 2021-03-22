package AtlasPackage;

import javax.swing.*;
import java.io.*;
import java.util.*;

/**
 * SCOutputModule has methods to generate the SC_OutputModule port map and file.
 * @author Aline Vieira de Mello
 * @version
 */
public class SCOutputModule {

	/**
	 * Create the SC_OutputModule file.
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
			FileOutputStream outputFile  = new FileOutputStream(scDir + "SC_OutputModule.h");
			DataOutputStream data_output = new DataOutputStream(outputFile);

			data_output.writeBytes(getFile(type, sourceDir, dimX, dimY, flitSize, nChannels));
		
			data_output.close();
			outputFile.close();
		}catch(Exception e){
			JOptionPane.showMessageDialog(null,"Can't write SC_OutputModule.h\n"+e.getMessage(),"Error Message", JOptionPane.ERROR_MESSAGE);
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
			FileInputStream inputFile = new FileInputStream(new File(sourceDir + "SC_OutputModule.h"));
			BufferedReader buff=new BufferedReader(new InputStreamReader(inputFile));

			line=buff.readLine();
			while(line!=null){
				st = new StringTokenizer(line, "$");
				int vem = st.countTokens();
				for (int cont=0; cont<vem; cont++){
					word = st.nextToken();
					if(word.equalsIgnoreCase("NROT"))
						data += ("" + nRouters);
					else if(word.equalsIgnoreCase("NLANE"))
						data += ("" + nChannels+"\n");
					else if(word.equalsIgnoreCase("TFLIT"))
						data += ("" + flitSize);
					else if(word.equalsIgnoreCase("WIDTH"))
						word += ("" + dimX);
					else if(word.equalsIgnoreCase("HEIGHT"))
						data += ("" + dimY);
					else if(word.equalsIgnoreCase("INTX"))
						data += getInTxTest(nRouters);
					else if(word.equalsIgnoreCase("INLANETX"))
						data += getInLaneTxTest(nRouters, nChannels);
					else if(word.equalsIgnoreCase("INDATA"))
						data += getInDataTest(nRouters);
					else if(word.equalsIgnoreCase("SIGNALS"))
						data += getSignalsDeclaration(type, nRouters);
					else if(word.equalsIgnoreCase("VARIABLES"))
						data += getSignalsConnection(type, nRouters);
					else if(word.equalsIgnoreCase("OUTMODULE"))
						data += getOutCredit(type, nRouters);
					else if(word.equalsIgnoreCase("IF_INLANETX"))
						data += getInLaneTxTest(nChannels);
					else
						data += word;
				}
				data += ("\n");
				line=buff.readLine();
			} //end while
			buff.close();
			inputFile.close();
		}catch(Exception e){
			JOptionPane.showMessageDialog(null,"Can't write SC_OutputModule.h\n"+e.getMessage(),"Error Message", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		return data;
	}
	
	/**
	 * Return the InTx signal test of all routers.
	 * @param nRouters The number of routers.
	 * @return A String containing the InTx signal test of all <i>HS</i> routers.
	 */
	public static String getInTxTest(int nRouters){
		String data="";
		for (int router=0; router<nRouters; router++){
			data += ("\t\tif(Indice == " + router + ") return (intx" + router + " == SC_LOGIC_1)?1:0;\n");
		}
		data = Convert.removeLast(data, "\n");
		return data;
	}

	/**
	 * Return the InLaneTx signal test of all routers.
	 * @param nRouters The number of routers.
	 * @param nChannels The number of virtual channels.
	 * @return A String containing the InLaneTx signal test of all routers.
	 */
	public static String getInLaneTxTest(int nRouters, int nChannels) {
		String data = "";
		for (int router=0; router<nRouters; router++){
			data += ("\t\tif(Indice == "+router+"){\n");
			for (int channel=0; channel<nChannels; channel++){
				data += ("\t\t\tif(Lane == "+channel+") return (inlane_tx"+router+".read().get_bit("+channel+") == SC_LOGIC_1)? 1 : 0;\n");
			}
			data += ("\t\t}\n");
		}
		return data;
	}	

	/**
	 * Return the InData signal test of all routers.
	 * @param nRouters The number of routers.
	 * @return A String containing the InData signal test of all routers.
	 */
	public static String getInDataTest(int nRouters){
		String data="";
		for (int router=0; router<nRouters; router++){
			data += ("\t\tif(Indice == " + router + ") return indata" + router + ".read().to_uint();\n");
		}
		data = Convert.removeLast(data, "\n");
		return data;
	}
	
	/**
	 * Return the signals declaration of all routers.
	 * @param type Each type has a distinct group of signals. Types: CB, HS, VC 
	 * @param nRouters The number of routers.
	 * @return A String containing the signals declaration of all routers.
	 */
	public static String getSignalsDeclaration(int type, int nRouters) {
		String data = "";
		switch(type){
		case NoC.HS: data += getSignalsDeclaration_HS(nRouters); break;
		case NoC.CB: data += getSignalsDeclaration_CB(nRouters); break;
		case NoC.VC: data += getSignalsDeclaration_VC(nRouters); break;
		default:
			JOptionPane.showMessageDialog(null,"Indefined type : "+ type+"\nError in SCOutputModule.getSignalsDeclaration","Error Mesage" ,JOptionPane.ERROR_MESSAGE);
			System.exit(0);
			break;
		}
		data = Convert.removeLast(data,"\n");
		return data;
	}
	
	/**
	 * Return the signals declaration of all <i>HS</i> routers.
	 * @param nRouters The number of routers.
	 * @return A String containing the signals declaration of all <i>HS</i> routers.
	 */
	public static String getSignalsDeclaration_HS(int nRouters){
		String data="";
		for (int router=0; router<nRouters; router++){
			data += ("\tsc_in<sc_logic> intx" + router + ";\n");
			data += ("\tsc_in<sc_lv<constFlitSize> > indata" + router + ";\n");
		}
		return data;
	}

	/**
	 * Return the signals declaration of all <i>CB</i> routers.
	 * @param nRouters The number of routers.
	 * @return A String containing the signals declaration of all <i>CB</i> routers.
	 */
	public static String getSignalsDeclaration_CB(int nRouters){
		String data="";
		for (int router=0; router<nRouters; router++){
			data += ("\tsc_in<sc_logic> inclock" + router + ";\n");
			data += ("\tsc_in<sc_logic> intx" + router + ";\n");
			data += ("\tsc_in<sc_lv<constFlitSize> > indata" + router + ";\n");
			data += ("\tsc_out<sc_logic> outcredit" + router + ";\n");
		}
		return data;
	}

	/**
	 * Return the signals declaration of all <i>VC</i> routers.
	 * @param nRouters The number of routers.
	 * @return A String containing the signals declaration of all <i>VC</i> routers.
	 */
	public static String getSignalsDeclaration_VC(int nRouters){
		String data="";
		for (int router=0; router<nRouters; router++){
			data += ("\tsc_in<sc_logic> inclock" + router + ";\n");
			data += ("\tsc_in<sc_logic> intx" + router + ";\n");
			data += ("\tsc_in<sc_lv<constNumLane> > inlane_tx" + router + ";\n");
			data += ("\tsc_in<sc_lv<constFlitSize> > indata" + router + ";\n");
			data += ("\tsc_out<sc_lv<constNumLane> > outcredit" + router + ";\n");
		}
		return data;
	}
	
	/**
	 * Return the signals connection of all routers.
	 * @param type Each type has a distinct group of signals. Types: CB, HS, VC 
	 * @param nRouters The number of routers.
	 * @return A String containing the signals connection of all routers.
	 */
	public static String getSignalsConnection(int type, int nRouters) {
		String data = "";
		switch(type){
		case NoC.HS: data += getSignalsConnection_HS(nRouters); break;
		case NoC.CB: data += getSignalsConnection_CB(nRouters); break;
		case NoC.VC: data += getSignalsConnection_VC(nRouters); break;
		default:
			JOptionPane.showMessageDialog(null,"Indefined type : "+ type+"\nError in SCOutputModule.getSignalsConnection","Error Mesage" ,JOptionPane.ERROR_MESSAGE);
			System.exit(0);
			break;
		}
		data = Convert.removeLast(data,"\n");
		return data;
	}

	/**
	 * Return the signals connection of all <i>HS</i> routers.
	 * @param nRouters The number of routers.
	 * @return A String containing the signals connection of all <i>HS</i> routers.
	 */
	public static String getSignalsConnection_HS(int nRouters){
		String data="";
		for (int router=0; router<nRouters; router++){
			data += ("\tintx" + router + "(\"intx" + router + "\"),\n");
			data += ("\tindata" + router + "(\"indata" + router + "\"),\n");
		}
		return data;
	}
	
	/**
	 * Return the signals connection of all <i>CB</i> routers.
	 * @param nRouters The number of routers.
	 * @return A String containing the signals connection of all <i>CB</i> routers.
	 */
	public static String getSignalsConnection_CB(int nRouters){
		String data="";
		for (int router=0; router<nRouters; router++){
			data += ("\tinclock" + router + "(\"inclock" + router + "\"),\n");
			data += ("\tintx" + router + "(\"intx" + router + "\"),\n");
			data += ("\tindata" + router + "(\"indata" + router + "\"),\n");
			data += ("\toutcredit" + router + "(\"outcredit" + router + "\"),\n");
		}
		return data;
	}

	/**
	 * Return the signals connection of all <i>VC</i> routers.
	 * @param nRouters The number of routers.
	 * @return A String containing the signals connection of all <i>VC</i> routers.
	 */
	public static String getSignalsConnection_VC(int nRouters){
		String data="";
		for (int router=0; router<nRouters; router++){
			data += ("\tinclock" + router + "(\"inclock" + router + "\"),\n");
			data += ("\tintx" + router + "(\"intx" + router + "\"),\n");
			data += ("\tinlane_tx" + router + "(\"inlane_tx" + router + "\"),\n");
			data += ("\tindata" + router + "(\"indata" + router + "\"),\n");
			data += ("\toutcredit" + router + "(\"outcredit" + router + "\"),\n");
		}
		return data;
	}
	
	/**
	 * Return the OutCredit signal test of all routers. <br>
	 * <i>This method is used by CB and VC. HS does not have the OutCredit signal.</i> 
	 * @param type Each type has a distinct group of signals. Types: CB, HS, VC 
	 * @param nRouters The number of routers.
	 * @return A String containing the OutCredit signal test of all routers.
	 */
	public static String getOutCredit(int type, int nRouters) {
		String data = "";
		switch(type){
		case NoC.CB: data += getOutCredit_CB(nRouters); break;
		case NoC.VC: data += getOutCredit_VC(nRouters); break;
		default:
			JOptionPane.showMessageDialog(null,"Indefined type : "+ type+"\nError in SCOutputModule.getOutCredit","Error Mesage" ,JOptionPane.ERROR_MESSAGE);
			System.exit(0);
			break;
		}
		data = Convert.removeLast(data,"\n");
		return data;
	}
	
	/**
	 * Return the OutCredit signal of all <i>VC</i> routers.
	 * @param nRouters The number of routers.
	 * @return A String containing the OutCredit signal of all <i>VC</i> routers.
	 */
	public static String getOutCredit_VC(int nRouters){
		String data="";
		for (int router=0; router<nRouters; router++){
			data += ("\toutcredit"+router+" = 0xF;\n");
		}
		return data;
	}

	/**
	 * Return the OutCredit signal of all <i>CB</i> routers.
	 * @param nRouters The number of routers.
	 * @return A String containing the OutCredit signal of all <i>CB</i> routers.
	 */
	public static String getOutCredit_CB(int nRouters){
		String data="";
		for (int router=0; router<nRouters; router++){
			data += ("\toutcredit"+router+" = SC_LOGIC_1;\n");
		}
		return data;
	}

	/**
	 * Return the InLaneTx signal test. <br>
	 * <i>This methods is used only by VC. The CB and HS does not have the InLaneTx signal.</i>
	 * @param nChannels The number of virtual channels.
	 * @return A String containing the InLaneTx signal test.
	 */
	public static String getInLaneTxTest( int nChannels) {
		String data = "";
		for (int channel=0; channel<nChannels; channel++){
			if(channel==0)
				data += ("\t\t\t\tif(inLaneTx(Index,"+channel+") == 1)\n");
			else if(channel==nChannels-1)
				data += ("\t\t\t\telse\n");
			else
				data += ("\t\t\t\telse if(inLaneTx(Index,"+channel+") == 1)\n");
			data += ("\t\t\t\t\tlane = "+channel+";\n");
		}
		return data;
	}

	/**
	 * Return the SC_OutputModule port map. (This method is used in topNoC file).
 	 * @param type Each type has a distinct group of signals. Types: CB, HS, VC 
	 * @param dimX The X-dimension of NoC.
	 * @param dimY The Y-dimension of NoC.
	 * @param flitSize The number of bits of a flit.
	 * @return A String containing the SC_OutputModule port map.
	 */
	public static String getPortMap(int type, int dimX, int dimY, int flitSize) {
		String xHexa, yHexa, data = "";
		int router, nbytes = (flitSize/8);
		String routerName = "N"+ Convert.decToHex(0, nbytes) + Convert.decToHex(0, nbytes);
		
		data += ("\tcom00: Entity work.outmodule\n");
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
					JOptionPane.showMessageDialog(null,"Indefined type : "+ type+"\nError in SCOutputModule.getPortMap","Error Mesage" ,JOptionPane.ERROR_MESSAGE);
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
	 * Return the SC_OutputModule port map for a specific <i>VC</i> router.
	 * @param routerName The router name. For instance: N0000.
	 * @param router The number of router.
	 * @return A String containing the SC_OutputModule port map for a specific <i>VC</i> router.
	 */
	public static String getPortMap_VC(String routerName, int router) {
		String data = "";
		data += ("\t\tinclock" + router + "    => clock_tx("+routerName+"),\n");
		data += ("\t\tinTx" + router + "       => tx("+routerName+"),\n");
		data += ("\t\tinlane_tx" + router + "  => lane_tx("+routerName+"),\n");
		data += ("\t\tinData" + router + "     => data_out("+routerName+"),\n");
		data += ("\t\toutCredit" + router + "  => credit_i("+routerName+"),\n");
		return data;
	}	
	
	/**
	 * Return the SC_OutputModule port map for a specific <i>CB</i> router.
	 * @param routerName The router name. For instance: N0000.
	 * @param router The number of router.
	 * @return A String containing the SC_OutputModule port map for a specific <i>CB</i> router.
	 */
	public static String getPortMap_CB(String routerName, int router) {
		String data = "";
		data += ("\t\tinClock" + router + "    => clock_tx("+routerName+"),\n");
		data += ("\t\tinTx" + router + "       => tx("+routerName+"),\n");
		data += ("\t\tinData" + router + "     => data_out("+routerName+"),\n");
		data += ("\t\toutCredit" + router + "  => credit_i("+routerName+"),\n");
		return data;
	}	

	/**
	 * Return the SC_OutputModule port map for a specific <i>HS</i> router.
	 * @param routerName The router name. For instance: N0000.
	 * @param router The number of router.
	 * @return A String containing the SC_OutputModule port map for a specific <i>HS</i> router.
	 */
	public static String getPortMap_HS(String routerName, int router) {
		String data = "";
		data += ("\t\tinTx" + router + "       => tx("+routerName+"),\n");
		data += ("\t\tinData" + router + "     => data_out("+routerName+"),\n");
		return data;
	}
	
	/**
	 * Used to test SCOutputModuleRouter methods.
	 * @param args The list of arguments
	 */
	public static void main(String args[]){
//		System.out.println(SCOutputModule.getPortMap(NoC.HS, 4, 4, 16));
//		System.out.println(SCOutputModule.getInLaneTxTest(4));
//		
//		String scDir = new File(Default.atlashome).getParent() + File.separator;
//		String hsSourceDir = Default.atlashome + File.separator + "Maia" + File.separator + "Data" + File.separator + "Handshake" + File.separator;
//		String cbSourceDir = Default.atlashome + File.separator + "Maia" + File.separator + "Data" + File.separator + "CreditBased" + File.separator;
//		String vcSourceDir = Default.atlashome + File.separator + "Maia" + File.separator + "Data" + File.separator + "VirtualChannel" + File.separator + "RoundRobin" + File.separator;
//		
//		System.out.println(SCInputModule.getFile(NoC.HS, hsSourceDir, 3, 3, 16, 1));
//		SCOutputModule.createFile(NoC.HS, hsSourceDir, scDir, 4, 4, 16, 1);
//		
//		System.out.println(SCInputModule.getFile(NoC.CB, cbSourceDir, 3, 3, 16, 1));
//		SCOutputModule.createFile(NoC.CB, cbSourceDir, scDir, 3, 3, 16, 1);
//		
//		System.out.println(SCOutputModule.getFile(NoC.VC, vcSourceDir, 3, 3, 16, 2));
//		SCOutputModule.createFile(NoC.VC, vcSourceDir, scDir, 3, 3, 16, 2);

	}
}