package HermesTU;

import javax.swing.*;
import java.io.*;

import AtlasPackage.NoCGenerationVC;
import AtlasPackage.Project;
import AtlasPackage.NoC;
import AtlasPackage.SCOutputModuleRouter;
import AtlasPackage.SCOutputModule;
import AtlasPackage.SCInputModule;
import AtlasPackage.Convert;
import AtlasPackage.ManipulateFile;
import AtlasPackage.Default;

/**
 * Generate a Hermes Torus Unidirectional NoC.
 * @author Aline Vieira de Mello
 * @version
 */
public class HermesTUVirtualChannel extends NoCGenerationVC{

	private static String sourceDir = Default.atlashome + File.separator + "HermesTU" + File.separator + "Data" + File.separator + "VirtualChannel" + File.separator + "RoundRobin" + File.separator;

	private String projectDir, nocDir, scDir;
	private String  nocType;
	private int dimX, dimY, flitSize, nChannels;
	private boolean isSC;

	/**
	 * Generate a Hermes Torus Unidirectional NoC. 
	 * @param project The NoC project.
	 */
	public HermesTUVirtualChannel(Project project){
		super(project, sourceDir);
		nocType = project.getNoC().getType();
		dimX = project.getNoC().getNumRotX();
		dimY = project.getNoC().getNumRotY();
		flitSize = project.getNoC().getFlitSize();
		nChannels = project.getNoC().getVirtualChannel();
		isSC = project.getNoC().isSCTB();
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
		//copy files
		ManipulateFile.copy(new File(sourceDir+"HermesTU_buffer.vhd"), nocDir);
		ManipulateFile.copy(new File(sourceDir+"HermesTU_inport.vhd"), nocDir);
		ManipulateFile.copy(new File(sourceDir+"HermesTU_switchcontrol.vhd"), nocDir);
		ManipulateFile.copy(new File(sourceDir+"HermesTU_outport.vhd"), nocDir);
		ManipulateFile.copy(new File(sourceDir+"RouterCC.vhd"), nocDir);
		//create the Hermes_package.vhd file
		createPackage("HermesTU_package.vhd");
		//create the NoC
		createNoC();
		//If the SC test bench option is selected, create the SC file
		if(isSC)
			createSC();
	}

/*********************************************************************************
* NOC
*********************************************************************************/
	/**
	 * Create the NoC VHDL file. <br>
	 * The Hermes Torus Unidirectional has a different package and different connections between routers 
	 */
	public void createNoC(){
		try{
			DataOutputStream data_output=new DataOutputStream(new FileOutputStream(nocDir + "NOC.vhd"));

			//generate the libraries using HermesTUPackage
			writeNoCLibraries(data_output,"HermesTUPackage");

			//generate the NoC entity
			writeNoCEntity(data_output);

			//generate architecture
			data_output.writeBytes("architecture NOC of NOC is\n\n");

			//generate the signals
			writeAllSignals(data_output);

			data_output.writeBytes("begin\n\n");

			//generate routers port map (different only RouterCC)
			writeAllRoutersPortMap(data_output) ;

			//generate all routers connections (different connections)
			writeAllConnections(data_output);

			//If SC option is selected then generate SC entity
			if(isSC){
				data_output.writeBytes(SCOutputModuleRouter.getPortMap(nocType, NoC.VC, dimX, dimY, flitSize, nChannels));
			}

			data_output.writeBytes("end NOC;");
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
	 * Write all router connections.
	 * @param data_output
	 * @throws Exception 
	 */
	public void writeAllConnections(DataOutputStream data_output) throws Exception {
		String xHexa="",yHexa="",xmais1Hexa="",ymais1Hexa="",xmenos1Hexa="",ymenos1Hexa="",nodo1="",nodo2="";
		String maxXHexa = Convert.decToHex((dimX-1),(flitSize/8));
		String maxYHexa = Convert.decToHex((dimY-1),(flitSize/8));
		String minXHexa = Convert.decToHex(0,(flitSize/8));
		String minYHexa = Convert.decToHex(0,(flitSize/8));

		for (int y=0; y<dimY; y++){
			yHexa = Convert.decToHex(y,(flitSize/8));
			for (int x=0; x<dimX; x++){
				xHexa = Convert.decToHex(x,(flitSize/8));
				data_output.writeBytes("\t-- entradas do roteador"+xHexa+yHexa+"\n");

				xmais1Hexa = Convert.decToHex((x+1),(flitSize/8));
				xmenos1Hexa = Convert.decToHex((x-1),(flitSize/8));
				ymais1Hexa = Convert.decToHex((y+1),(flitSize/8));
				ymenos1Hexa = Convert.decToHex((y-1),(flitSize/8));

				nodo1=xHexa+yHexa;

				// EAST port
				data_output.writeBytes("\t-- port EAST \n");
				if(x!=(dimX-1)){
					nodo2=xmais1Hexa+yHexa;
					writeNullConnection(data_output, nodo1, nodo2, 0, 1);
				}
				else{
					nodo2=minXHexa+yHexa;
					writeNullConnection(data_output, nodo1, nodo2, 0, 1);
				}

				// WEST port
				data_output.writeBytes("\t-- port WEST \n");
				if(x!=0){
					nodo2=xmenos1Hexa+yHexa;
					writeConnection(data_output, nodo1, nodo2, 1, 0);
				}
				else{
					nodo2=maxXHexa+yHexa;
					writeConnection(data_output, nodo1, nodo2, 1, 0);
				}

				// NORTH port
				data_output.writeBytes("\t-- port NORTH \n");
				if(y!=(dimY-1)){
					nodo2=xHexa+ymais1Hexa;
					writeNullConnection(data_output, nodo1, nodo2, 2, 3);
				}
				else{
					nodo2=xHexa+minYHexa;
					writeNullConnection(data_output, nodo1, nodo2, 2, 3);
				}

				// SOUTH port
				data_output.writeBytes("\t-- port SOUTH \n");
				if(y!=0){
					nodo2=xHexa+ymenos1Hexa;
					writeConnection(data_output, nodo1, nodo2, 3, 2);
				}
				else{
					nodo2=xHexa+maxYHexa;
					writeConnection(data_output, nodo1, nodo2, 3, 2);
				}
				// LOCAL port
				data_output.writeBytes("\t-- port LOCAL \n");
				writeLocalConnection(data_output, xHexa, yHexa);
			}
		}
	}
	
	/**
	 * Connect zeros to all signal belongs to the informed portRouter1, 
	 * except the signal credit_i that is connect to the Router2.
	 * @param data_output
	 * @param router1 The Router1 address (router to the left).
	 * @param router2 The Router2 address (router to the right).
	 * @param portRouter1 The Router1 port (router to the left).
	 * @param portRouter2 The Router2 port (router to the right).
	 * @throws Exception 
	 */
	private void writeNullConnection(DataOutputStream data_output, String router1,String router2,int portRouter1,int portRouter2) throws Exception {
		data_output.writeBytes("\tclock_rxN"+router1+"("+portRouter1+")<='0';\n");
		data_output.writeBytes("\trxN"+router1+"("+portRouter1+")<='0';\n");
		data_output.writeBytes("\tlane_rxN"+router1+"("+portRouter1+")<=(others=>'0');\n");
		data_output.writeBytes("\tdata_inN"+router1+"("+portRouter1+")<=(others=>'0');\n");
		data_output.writeBytes("\tcredit_iN"+router1+"("+portRouter1+")<=credit_oN"+router2+"("+portRouter2+");\n\n");
	}

/*********************************************************************************
* SYSTEMC
*********************************************************************************/

	/**
	 * Creates the SystemC files
	 */
	public void createSC()	{
		//copy CPP files to SC_NoC directory
	   	copySCFiles();
		//replacing the flags in c_input_module.c file.
	   	SCInputModule.createFile(NoC.VC, sourceDir, scDir, dimX, dimY, flitSize, nChannels);
		//replacing the flags in c_output_module.c file.
	   	SCOutputModule.createFile(NoC.VC, sourceDir, scDir, dimX, dimY, flitSize, nChannels);
		//replacing the flags in c_output_module_router.c file.
		SCOutputModuleRouter.createFile(sourceDir, scDir, nocType, NoC.VC, dimX, dimY, flitSize, nChannels);
		//create the top.cpp file using SC files.
		createTopNoC("HermesTUPackage");
		//create the simulation scripts using SC files
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
		// vcom ROUTERCC
		writeVCOM(data_output,"NOC/RouterCC.vhd");
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
		writeVCOM(data_output, "NOC/HermesTU_package.vhd");
		writeVCOM(data_output, "NOC/HermesTU_buffer.vhd");
		writeVCOM(data_output, "NOC/HermesTU_inport.vhd");
		writeVCOM(data_output, "NOC/HermesTU_switchcontrol.vhd");
		writeVCOM(data_output, "NOC/HermesTU_outport.vhd");
	}
	
}
