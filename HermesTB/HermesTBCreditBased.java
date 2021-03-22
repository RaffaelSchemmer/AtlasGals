package HermesTB;

import javax.swing.*;
import java.io.*;
import java.util.*;

import AtlasPackage.NoCGenerationCB;
import AtlasPackage.Project;
import AtlasPackage.NoC;
import AtlasPackage.SCOutputModuleRouter;
import AtlasPackage.SCOutputModule;
import AtlasPackage.SCInputModule;;
import AtlasPackage.Convert;
import AtlasPackage.ManipulateFile;
import AtlasPackage.Default;

/**
 * Generate a Hermes Torus Bidirectional NoC.
 * @author Aline Vieira de Mello
 * @version
 */
public class HermesTBCreditBased extends NoCGenerationCB {
	private static String sourceDir = Default.atlashome + File.separator
			+ "HermesTB" + File.separator + "Data" + File.separator
			+ "CreditBased" + File.separator;

	private String projectDir, nocDir, scDir;
	private String nocType;
	private int dimX, dimY, flitSize;
	private boolean isSC;

	/**
	 * Generate a Hermes Torus Bidirectional NoC.
	 * 
	 * @param project
	 *            The NoC project.
	 */
	public HermesTBCreditBased(Project project) {
		super(project, sourceDir);
		NoC noc = project.getNoC();
		nocType = noc.getType();
		dimX = noc.getNumRotX();
		dimY = noc.getNumRotY();
		flitSize = noc.getFlitSize();
		isSC = noc.isSCTB();
		projectDir = project.getPath() + File.separator;
		nocDir     = projectDir + "NOC" + File.separator;
		scDir      = projectDir + "SC_NoC" + File.separator;
	}

	/**
	 * Generate the NoC and SC files
	 */
	public void generate() {
		makeDiretories();
		ManipulateFile.copy(new File(sourceDir + "HermesTB_buffer.vhd"), nocDir);
		ManipulateFile.copy(new File(sourceDir + "HermesTB_crossbar.vhd"), nocDir);
		ManipulateFile.copy(new File(sourceDir + "RouterCC.vhd"), nocDir);
		createPackage("HermesTB_package.vhd");
		createSwitchcontrol();
		createNoC();
		if (isSC)
			createSC();
	}

	/*********************************************************************************
	 * SWITCHCONTROL
	 *********************************************************************************/

	/**
	 * Create the Hermes_switchcontrol.vhdl file, replacing the flags.
	 */
	private void createSwitchcontrol() {
		StringTokenizer st;
		String line, word;

		try {
			FileInputStream inFile = new FileInputStream(new File(sourceDir + "HermesTB_switchcontrol.vhd"));
			BufferedReader buff = new BufferedReader(new InputStreamReader(inFile));

			DataOutputStream data_output = new DataOutputStream(new FileOutputStream(nocDir	+ "HermesTB_switchcontrol.vhd"));

			int n_lines = 0;
			line=buff.readLine();
			while(line!=null){
				st = new StringTokenizer(line, "$");
				int nTokens = st.countTokens();
				for (int cont = 0; cont < nTokens; cont++) {
					word = st.nextToken();
					if (word.equalsIgnoreCase("net_limit_x_by_2")) {
						word = "" + (dimX / 2);
					} else if (word.equalsIgnoreCase("net_limit_y_by_2")) {
						word = "" + (dimY / 2);
					}
					data_output.writeBytes(word);
				}// end for
				data_output.writeBytes("\r\n");
				n_lines++;
				line=buff.readLine();
			} //end while
			buff.close();
			data_output.close();
			inFile.close();
		} catch (FileNotFoundException f) {
			JOptionPane.showMessageDialog(null,"Can't write HermesTB_switchcontrol.vhd", "Output error",JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,"Can't write HermesTB_switchcontrol.vhd\n"+e.getMessage(), "Error Message",JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}

	/*********************************************************************************
	 * NOC
	 *********************************************************************************/
	
	/**
	 * Create the NoC VHDL file. <br>
 	 * In HERMES Torus Bidirectional, all routers have five ports, even the routers in the limit of NoC.
 	 * Therefore, the connection between routers and the SCOutputModuleRouter are different.
 	 * The name of package is also different: HermesTBPackage.
	 */
	public void createNoC(){
		try{
			DataOutputStream data_output=new DataOutputStream(new FileOutputStream(nocDir + "NOC.vhd"));

			//generate the libraries (HermesTBPackage)
			writeNoCLibraries(data_output, "HermesTBPackage");

			//generate the NoC entity
			writeNoCEntity(data_output);

			//generate architecture
			data_output.writeBytes("architecture NOC of NOC is\n\n");

			//generate the signals
			writeAllSignals(data_output);

			data_output.writeBytes("begin\n\n");

			//generate routers port map (All RouterCC)
			writeAllRoutersPortMap(data_output) ;

			//generate all routers connections (Torus connections)
			writeAllConnections(data_output);
			
			//If SC option is selected then generate SC entity
			if(isSC){
				data_output.writeBytes(SCOutputModuleRouter.getPortMap(nocType, NoC.CB, dimX, dimY, flitSize, 1));
			}

			data_output.writeBytes("end NOC;");
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
	 * Write RouterCC port map for all routers. 
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeAllRoutersPortMap(DataOutputStream data_output) throws Exception{
		String xHexa, yHexa;
		for (int y=0; y<dimY; y++){
			yHexa = Convert.decToHex(y,(flitSize/8));
			for (int x=0; x<dimX; x++){
				xHexa = Convert.decToHex(x,(flitSize/8));
				writeRouterPortMap(data_output, "RouterCC", xHexa, yHexa);
			}
		}
	}

	/**
	 * Write all router connections. <br>
 	 * In HERMES Torus Bidirectional, all routers have five ports, even the routers in the limit of NoC.  
	 * @param data_output
	 * @throws Exception
	 */
	public void writeAllConnections(DataOutputStream data_output) throws Exception {
		String xHexa = "", yHexa = "", xmais1Hexa = "", ymais1Hexa = "", xmenos1Hexa = "", ymenos1Hexa = "", nodo1 = "", nodo2 = "";
		String maxXHexa = Convert.decToHex((dimX - 1), (flitSize / 8));
		String maxYHexa = Convert.decToHex((dimY - 1), (flitSize / 8));
		String minXHexa = Convert.decToHex(0, (flitSize / 8));
		String minYHexa = Convert.decToHex(0, (flitSize / 8));

		for (int y = 0; y < dimY; y++) {
			yHexa = Convert.decToHex(y, (flitSize / 8));
			for (int x = 0; x < dimX; x++) {
				xHexa = Convert.decToHex(x, (flitSize / 8));
				data_output.writeBytes("\t-- ROUTER " + xHexa + yHexa + "\n");

				xmais1Hexa = Convert.decToHex((x + 1), (flitSize / 8));
				xmenos1Hexa = Convert.decToHex((x - 1), (flitSize / 8));
				ymais1Hexa = Convert.decToHex((y + 1), (flitSize / 8));
				ymenos1Hexa = Convert.decToHex((y - 1), (flitSize / 8));

				nodo1 = xHexa + yHexa;

				// EAST port
				data_output.writeBytes("\t-- EAST port\n");
				if (x != (dimX - 1))
					nodo2 = xmais1Hexa + yHexa;
				else
					nodo2 = minXHexa + yHexa;
				writeConnection(data_output, nodo1, nodo2, 0, 1);

				// WEST port
				data_output.writeBytes("\t-- WEST port\n");
				if (x != 0)
					nodo2 = xmenos1Hexa + yHexa;
				else
					nodo2 = maxXHexa + yHexa;
				writeConnection(data_output, nodo1, nodo2, 1, 0);

				// NORTH port
				data_output.writeBytes("\t-- NORTH port\n");
				if (y != (dimY - 1))
					nodo2 = xHexa + ymais1Hexa;
				else
					nodo2 = xHexa + minYHexa;
				writeConnection(data_output, nodo1, nodo2, 2, 3);

				// SOUTH port
				data_output.writeBytes("\t-- SOUTH port\n");
				if (y != 0)
					nodo2 = xHexa + ymenos1Hexa;
				else
					nodo2 = xHexa + maxYHexa;
				writeConnection(data_output, nodo1, nodo2, 3, 2);

				// LOCAL port
				data_output.writeBytes("\t-- LOCAL port\n");
				writeLocalConnection(data_output, xHexa, yHexa);
			}
		}
	}
	
	/*********************************************************************************
	 * SystemC
	 *********************************************************************************/

	/**
	 * Creates the SystemC files
	 */
	public void createSC() {
		// copy CPP files to SC_NoC directory
		copySCFiles();
		// replacing the flags in c_input_module.c file.
	   	SCInputModule.createFile(NoC.CB, sourceDir, scDir, dimX, dimY, flitSize, 1);
		// replacing the flags in c_output_module.c file.
	   	SCOutputModule.createFile(NoC.CB, sourceDir, scDir, dimX, dimY, flitSize, 1);
		// replacing the flags in c_output_module_router.c file.
		SCOutputModuleRouter.createFile(sourceDir, scDir, nocType, NoC.CB, dimX, dimY, flitSize, 1);
		// create the top.cpp file using SC files.
		createTopNoC("HermesTBPackage");
		// create the simulation scripts using SC files
		createSimulateScript();
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
		// vcom ROUTERCC file
		writeVCOM(data_output, "NOC/RouterCC.vhd");
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
		writeVCOM(data_output, "NOC/HermesTB_package.vhd");
		writeVCOM(data_output, "NOC/HermesTB_buffer.vhd");
		writeVCOM(data_output, "NOC/HermesTB_switchcontrol.vhd");
		writeVCOM(data_output, "NOC/HermesTB_crossbar.vhd");
	}
	
}
