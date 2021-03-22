package HermesSR;

import javax.swing.*;

import java.io.*;
import java.util.*;

import AtlasPackage.NoCGenerationVC;
import AtlasPackage.NoC;
import AtlasPackage.Project;
import AtlasPackage.Router;
import AtlasPackage.SCOutputModuleRouter;
import AtlasPackage.ManipulateFile;
import AtlasPackage.Convert;
import AtlasPackage.Default;

/**
 * Generate a HermesSR NoC with 4 virtual channels.
 * @author Aline Vieira de Mello
 * @version
 */
public class CV4_ctrl_gs_be extends NoCGenerationVC
{

	private static String sourceDir = Default.atlashome + File.separator + "HermesSR" + File.separator + "Data" + File.separator + "VirtualChannel" + File.separator + "4cv_control_GS_BE" + File.separator;

	private String projectDir, nocDir, scDir;
	private String nocType;
	private int dimX, dimY, dimension, flitSize, nChannels;
	private boolean isSC;

	/**
	 * Class constructor. 
	 * @param project The NoC project.
	 */
	public CV4_ctrl_gs_be(Project project){
		super(project, sourceDir);
		NoC noc = project.getNoC();
		nocType = noc.getType();
		dimX = noc.getNumRotX();
		dimY = noc.getNumRotY();
		dimension = dimX * dimY;
		flitSize = noc.getFlitSize();
		nChannels = noc.getVirtualChannel();
		isSC = noc.isSCTB();
		projectDir = project.getPath() + File.separator;
		nocDir     = projectDir + "NOC" + File.separator ;
		scDir      = projectDir + "SC_NoC" + File.separator ;
	}

	/**
	 * Generate the NoC and SC files
	 */
	public void generate(){
		//create the project directory tree
		makeDiretories();
		//copy NoC VHDL files
		copyNoCFiles();		
		//create Hermes_package.vhd 
		createPackage("Hermes_package.vhd");
		//create NoC file
		createNoC();
		//create the def.h that determines the used routing algorithm 
		createDefRouting();
		//create XY routing file
		createXYRouting();
		//If the SC test bench option is selected, create the SC file
		if(isSC)
			createSC();
	}
		
/*********************************************************************************
* DIRECTORIES AND FILES (HERMES_BUFFER AND HERMES_SWITCHCONTROL)
*********************************************************************************/
	/**
	 * Create the project directory tree.
	 */
	public void makeDiretories(){
		// create project, NOC and SC_NoC directories
		super.makeDiretories();

		// create the routing directory
		File routingDir=new File(projectDir + "Routing");
		routingDir.mkdirs();
	}

	/**
	 * copy VHDL files to the NoC directory.
	 */
  	private void copyNoCFiles(){
  		ManipulateFile.copy(new File(sourceDir+"Hermes_cv_in.vhd"), nocDir);
  		ManipulateFile.copy(new File(sourceDir+"Hermes_inport.vhd"), nocDir);
		ManipulateFile.copy(new File(sourceDir+"Hermes_cv_out.vhd"), nocDir);
		ManipulateFile.copy(new File(sourceDir+"Hermes_outport.vhd"), nocDir);
		ManipulateFile.copy(new File(sourceDir+"RouterBL.vhd"), nocDir);
		ManipulateFile.copy(new File(sourceDir+"RouterBR.vhd"), nocDir);
		ManipulateFile.copy(new File(sourceDir+"RouterTL.vhd"), nocDir);
		ManipulateFile.copy(new File(sourceDir+"RouterTR.vhd"), nocDir);

		if(dimX>2){
			ManipulateFile.copy(new File(sourceDir+"RouterBC.vhd"), nocDir);
			ManipulateFile.copy(new File(sourceDir+"RouterTC.vhd"), nocDir);
		}

		if(dimY>2){
			ManipulateFile.copy(new File(sourceDir+"RouterCL.vhd"), nocDir);
			ManipulateFile.copy(new File(sourceDir+"RouterCR.vhd"), nocDir);
		}

		if((dimY>2) && (dimX>2)){
			ManipulateFile.copy(new File(sourceDir+"RouterCC.vhd"), nocDir);
		}
	}

/*********************************************************************************
* ROUTING FILES
*********************************************************************************/
	/**
	 * Create the def.h file. This file defines the used routing algorithm. 
	 */
	private void createDefRouting(){
		String routing;
		FileOutputStream cool_routing; 
		DataOutputStream data_routing;
		try{
				routing = new String(scDir + "defs.h");
				cool_routing=new FileOutputStream(routing);
				data_routing=new DataOutputStream(cool_routing);

				data_routing.writeBytes("//========== DEFINE ROUTING FILES  TO ME USED ==========\n");
				data_routing.writeBytes("#ifndef _ROTFILE_DEF\n");
				data_routing.writeBytes("\t#define _CTRL_ROTFILE_DEF \"Routing" + File.separator + "pure_xy_CTRL.rot\"\n");
				data_routing.writeBytes("\t#define _GS_ROTFILE_DEF \"Routing" + File.separator + "pure_xy_GS.rot\"\n");
				data_routing.writeBytes("\t#define _BE_ROTFILE_DEF \"Routing" + File.separator + "pure_xy_BE.rot\"\n");
				data_routing.writeBytes("#endif\n");
				data_routing.writeBytes("//========== ========== ========== ========== ==========\n");
				
				data_routing.close();
		}catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Cannot write defs.h file.","Output error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}catch(Exception e){
			JOptionPane.showMessageDialog(null,"Cannot write defs.h file.\n"+e.getMessage(),"Error Message", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}

	/**
	 * Create the XY routing algorithm file.
	 */
	private void createXYRouting(){
		String sPath, srvc, routing = "";
		FileOutputStream cool_routing; 
		DataOutputStream data_routing;
		int source, target, i, lane;
		try{
			for(i=0; i<3; i++){
				switch(i){
					case 0: lane=0;srvc="CTRL";break;
					case 1: lane=1;srvc="GS";break;
					case 2: lane=3;srvc="BE";break;
					default: lane=3;srvc="BE";break;
				}
				routing = new String(projectDir + "Routing" + File.separator + "pure_xy_"+srvc+".rot");
				cool_routing=new FileOutputStream(routing);
				data_routing=new DataOutputStream(cool_routing);

				for (source=0; source<dimension; source++){
					for (target=0; target<dimension; target++){
						sPath=new String(XYPath(source,target));
						if(sPath.length()!=0)
							data_routing.writeBytes(source+";"+target+";"+lane+";"+sPath.length()+";"+sPath+"\n");
					}
				}
				data_routing.close();
			}
		}catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Cannot write "+routing+" file\n"+f.getMessage(),"Output error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}catch(Exception e){
			JOptionPane.showMessageDialog(null,"Cannot write "+routing+" file\n"+e.getMessage(),"Error Message", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}
	
  	/**
  	 * Return the list of ports in the path between two routers.
  	 * @param _source The number of source router. 
  	 * @param _target The number of target router.
  	 * @return A String containing all ports in the path. 
  	 */
	private String XYPath(int _source, int _target){
		int sourceX, sourceY, targetX, targetY;
		String sPath = new String("");
		if(_source!=_target){		
			sourceY=(int)(_source/dimX);
			sourceX=(int)(_source%dimX);
			targetY=(int)(_target/dimX);
			targetX=(int)(_target%dimX);
			
			while(true){
				if(sourceX!=targetX){
					if(sourceX<targetX){ // EAST
						sPath+="0";
						sourceX++;
					}
					else{
						sPath+="1";
						sourceX--;
					}
				}
				else if(sourceY!=targetY){
					if(sourceY<targetY){ // NORTH
						sPath+="2";
						sourceY++;
					}
					else{
						sPath+="3";
						sourceY--;
					}
				}
				else{
					break;
				}
			}
		}		
		return sPath;
	}
	
/*********************************************************************************
* NoC
*********************************************************************************/
	/**
	 * Create the NoC VHDL file. <br>
	 * <b>The router of HermesSR4 has not generic map (address).<b>
	 */
	public void createNoC(){
		try{
			DataOutputStream data_output=new DataOutputStream(new FileOutputStream(nocDir + "NOC.vhd"));

			//generate the libraries
			writeNoCLibraries(data_output, "Hermes_Package");

			//generate the NoC entity
			writeNoCEntity(data_output);

			//generate architecture
			data_output.writeBytes("architecture NOC of NOC is\n\n");
			
			//generate the signals ( This router has not generic map <address> )
			writeAllSignals(data_output);

			data_output.writeBytes("begin\n\n");

			//generate routers port map
			writeAllRoutersPortMap(data_output) ;

			//generate all routers connections
			writeAllConnections(data_output);
			
			// WITHOUT INTERNAL EVALUATION
			
			data_output.writeBytes("end NOC;\n");
			data_output.close();
		} catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't write Noc.vhd\n" + f.getMessage(),"Output error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		} catch(Exception e){
			JOptionPane.showMessageDialog(null,"Can't write Noc.vhd\n" + e.getMessage(), "Error Message", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}

	/**
	 * Write the port map for all routers.
	 * This router has not generic map (address).
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
	 * This router has not generic map (address).
	 * @param data_output 
	 * @param routerName The router name.
	 * @param xHexa The address in X-dimension. 
	 * @param yHexa The address in Y-dimension.
	 * @throws Exception 
	 */
	public void writeRouterPortMap(DataOutputStream data_output,String routerName,String xHexa,String yHexa) throws Exception{
		data_output.writeBytes("\tRouter"+xHexa+yHexa+" : Entity work."+routerName+"\n");
		data_output.writeBytes("\tport map(\n");
		data_output.writeBytes("\t\tclock => clock(N"+xHexa+yHexa+"),\n");
		data_output.writeBytes("\t\treset => reset,\n");
		data_output.writeBytes("\t\tclock_rx => clock_rxN"+xHexa+yHexa+",\n");
		data_output.writeBytes("\t\trx => rxN"+xHexa+yHexa+",\n");
		data_output.writeBytes("\t\tlane_rx => lane_rxN"+xHexa+yHexa+",\n");
		data_output.writeBytes("\t\tdata_in => data_inN"+xHexa+yHexa+",\n");
		data_output.writeBytes("\t\tcredit_o => credit_oN"+xHexa+yHexa+",\n");
		data_output.writeBytes("\t\tclock_tx => clock_txN"+xHexa+yHexa+",\n");
		data_output.writeBytes("\t\ttx => txN"+xHexa+yHexa+",\n");
		data_output.writeBytes("\t\tlane_tx => lane_txN"+xHexa+yHexa+",\n");
		data_output.writeBytes("\t\tdata_out => data_outN"+xHexa+yHexa+",\n");
		data_output.writeBytes("\t\tcredit_i => credit_iN"+xHexa+yHexa+");\n\n");
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
		//copy NI.cpp file to SC_NoC directory
		ManipulateFile.copy(new File(sourceDir+"trafficFileReader.h"), scDir);
		ManipulateFile.copy(new File(sourceDir+"trafficFileReader.cpp"), scDir);
		ManipulateFile.copy(new File(sourceDir+"NI.cpp"), scDir);
		//create NI.h file
		createSCNI();
		//replacing the flags in c_input_module.c file.
		createCInputModuleNoC();
		//replacing the flags in c_output_module.c file.
		createCOutputModuleNoC();
		//replacing the flags in c_output_module_router.c file.
		//createCSCOutputModuleRouter();
		SCOutputModuleRouter.createFile(sourceDir, scDir, nocType, NoC.VC, dimX, dimY, flitSize, nChannels);
		//create the top.cpp file using SC files.
		createTopNoC("Hermes_Package");
		//create the simulation script used by Modelsim
		createSimulateScript();
	}

	/**
	 * Create the NI.h file.
	 */
	private void createSCNI(){
		String line, word;
		StringTokenizer st;

		try{
			FileInputStream inFile = new FileInputStream(new File(sourceDir+"NI.h"));
			BufferedReader buff = new BufferedReader(new InputStreamReader(inFile));

			FileOutputStream outFile = new FileOutputStream(scDir + "NI.h");
			DataOutputStream data_output=new DataOutputStream(outFile);

			line=buff.readLine();
			while(line!=null){
				st = new StringTokenizer(line, "$");
				int vem = st.countTokens();
				for (int cont=0; cont<vem; cont++){
					word = st.nextToken();
					if(word.equalsIgnoreCase("XY")){
						data_output.writeBytes(""+dimension);
					}
					else {
						data_output.writeBytes(word);
					}
				}//end for
				data_output.writeBytes("\r\n");
				line=buff.readLine();
			} //end while
			buff.close();
			inFile.close();
			data_output.close();
			outFile.close();
		}catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Cannot write NI.h","Input error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}catch(Exception e){
			JOptionPane.showMessageDialog(null,"Cannot write NI.h\n"+e.getMessage(),"Error Message", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}

	/*********************************************************************************
	* INPUT MODULES
	*********************************************************************************/

	/**
	 * Create the inputModule C file. <br> 
	 * <b>This NoC uses different variable names.<b>  
	 */
	public void createCInputModuleNoC(){
		String line, word;
		StringTokenizer st;

		try{
			FileInputStream inFile = new FileInputStream(new File(sourceDir+"SC_InputModule.h"));
			BufferedReader buff=new BufferedReader(new InputStreamReader(inFile));

			FileOutputStream outFile = new FileOutputStream(scDir + "SC_InputModule.h");
			DataOutputStream data_output=new DataOutputStream(outFile);

			line=buff.readLine();
			while(line!=null){
				st = new StringTokenizer(line, "$");
				int vem = st.countTokens();

				for (int cont=0; cont<vem; cont++){
					word = st.nextToken();
					if(word.equalsIgnoreCase("NROT"))
						data_output.writeBytes(""+dimension);
					else if(word.equalsIgnoreCase("NLANE"))
						data_output.writeBytes(""+nChannels+"\n");
					else if(word.equalsIgnoreCase("TFLIT"))
						data_output.writeBytes(""+flitSize);
					else if(word.equalsIgnoreCase("WIDTH"))
						data_output.writeBytes(""+dimX);
					else if(word.equalsIgnoreCase("HEIGHT"))
						data_output.writeBytes(""+dimY);
					else if(word.equalsIgnoreCase("OUTTX"))
						writeSCIAllOutTx(data_output);
					else if(word.equalsIgnoreCase("OUTDATA"))
						writeSCIAllOutData(data_output);
					else if(word.equalsIgnoreCase("INCREDIT"))
						writeSCIAllInCredit(data_output);
					else if(word.equalsIgnoreCase("SIGNALS"))
						writeSCIAllSignals(data_output);
					else if(word.equalsIgnoreCase("VARIABLES"))
						writeSCIAllVariables(data_output);
					else if(word.equalsIgnoreCase("INPUTMODULE"))
						writeSCIAllInputModule(data_output);
					else
						data_output.writeBytes(word);
				}//end for
				data_output.writeBytes("\r\n");
				line=buff.readLine();
			} //end while
			buff.close();
			inFile.close();
			data_output.close();
			outFile.close();
		}catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Cannot write SC_InputModule.h","Input error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}catch(Exception e){
			JOptionPane.showMessageDialog(null,"Cannot write SC_InputModule.h\n"+e.getMessage(),"Error Message", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}
	
	/**
	 * Write the OutTx for all routers in SC_InputModule.
	 * @param data_output
	 * @throws Exception
	 */
	public void writeSCIAllOutTx(DataOutputStream data_output) throws Exception {
		for (int x=0; x<dimension; x++){
			if(x!=0)
				data_output.writeBytes("\t\telse ");
			else
				data_output.writeBytes("\t\t");
				data_output.writeBytes("if(_index == "+x+"){ if(_value!=0){outtx"+x+"=SC_LOGIC_1;lane_tx"+x+"=(1<<_lane);} else{outtx"+x+"=SC_LOGIC_0;lane_tx"+x+"=0;} }\n");
		}
	}	

	/**
	 * Write the OutData for all routers in SC_InputModule.
	 * @param data_output
	 * @throws Exception
	 */
	public void writeSCIAllOutData(DataOutputStream data_output) throws Exception {
		for (int x=0; x<dimension; x++){
			if(x!=0)
				data_output.writeBytes("\t\telse ");
			else
				data_output.writeBytes("\t\t");
			data_output.writeBytes("if(_index == "+x+") outdata"+x+" = _value;\n");
		}
	}
	
	/**
	 * Write the InCredit for all routers in SC_InputModule.
	 * @param data_output
	 * @throws Exception
	 */
	public void writeSCIAllInCredit(DataOutputStream data_output) throws Exception {
		for (int x=0; x<dimension; x++){
			if(x!=0)
				data_output.writeBytes("\t\telse ");
			else
				data_output.writeBytes("\t\t");
			data_output.writeBytes("if(_index == "+x+"){ return (incredit"+x+".read().get_bit(_lane) == SC_LOGIC_1)? 1 : 0; }\n");
		}
	}
	
	/**
	 * Write the signals for all routers in SC_InputModule.
	 * @param data_output
	 * @throws Exception
	 */
	public void writeSCIAllSignals(DataOutputStream data_output) throws Exception {
		for (int x=0; x<dimension; x++){
			data_output.writeBytes("\tsc_out<sc_logic> outclock"+x+";\n");
			data_output.writeBytes("\tsc_out<sc_logic> outtx"+x+";\n");
			data_output.writeBytes("\tsc_out<sc_lv<constNumLane> > lane_tx"+x+";\n");
			data_output.writeBytes("\tsc_out<sc_lv<constFlitSize> > outdata"+x+";\n");
			data_output.writeBytes("\tsc_in<sc_lv<constNumLane> > incredit"+x+";\n");
		}
	}	
	
	/**
	 * Write the variables for all routers in SC_InputModule.
	 * @param data_output
	 * @throws Exception
	 */
	public void writeSCIAllVariables(DataOutputStream data_output) throws Exception {
		for (int x=0; x<dimension; x++){
			data_output.writeBytes("\toutclock"+x+"(\"outclock"+x+"\"),\n");
			data_output.writeBytes("\touttx"+x+"(\"outtx"+x+"\"),\n");
			data_output.writeBytes("\tlane_tx"+x+"(\"lane_tx"+x+"\"),\n");
			data_output.writeBytes("\toutdata"+x+"(\"outdata"+x+"\"),\n");
			data_output.writeBytes("\tincredit"+x+"(\"incredit"+x+"\"),\n");
		}
	}
	
	/**
	 * Write the InputModule for all routers in SC_InputModule.
	 * @param data_output
	 * @throws Exception
	 */
	public void writeSCIAllInputModule(DataOutputStream data_output) throws Exception {
		for (int x=0; x<dimension; x++){
			data_output.writeBytes("\toutclock"+x+" = clock;\n");
		}
	}	
	
	/*********************************************************************************
	* OUTPUT MODULES
	*********************************************************************************/

	/**
	 * Create the outputModule C file.<br>
	 * <b>This NoC uses different variable names.<b>  
	 */
	public void createCOutputModuleNoC(){
		String line, word;
		StringTokenizer st;
		try{
			FileInputStream inFile =new FileInputStream(new File(sourceDir+"SC_OutputModule.h"));
			BufferedReader buff=new BufferedReader(new InputStreamReader(inFile ));

			FileOutputStream outFile = new FileOutputStream(scDir + "SC_OutputModule.h");
			DataOutputStream data_output=new DataOutputStream(outFile);
			line=buff.readLine();
			while(line!=null){
				st = new StringTokenizer(line, "$");
				int vem = st.countTokens();
				for (int cont=0; cont<vem; cont++){
					word = st.nextToken();
					if(word.equalsIgnoreCase("NROT"))
						data_output.writeBytes(""+dimension);
					else if(word.equalsIgnoreCase("NLANE"))
						data_output.writeBytes(""+nChannels+"\n");
					else if(word.equalsIgnoreCase("TFLIT"))
						data_output.writeBytes(""+flitSize);
					else if(word.equalsIgnoreCase("INTX"))
						writeSCOAllInTx(data_output);
					else if(word.equalsIgnoreCase("INDATA"))
						writeSCOAllInData(data_output);
					else if(word.equalsIgnoreCase("SIGNALS"))
						writeSCOAllSignals(data_output);
					else if(word.equalsIgnoreCase("VARIABLES"))
						writeSCOAllVariables(data_output);
					else if(word.equalsIgnoreCase("OUTMODULE"))
						writeSCOAllOutModule(data_output);
					else
						data_output.writeBytes(word);
				}
				data_output.writeBytes("\r\n");
				line=buff.readLine();
			} //end while
			buff.close();
			inFile.close();
			data_output.close();
			outFile.close();
		}catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't write SC_OutputModule.h","Output error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}catch(Exception e){
			JOptionPane.showMessageDialog(null,"Can't write SC_OutputModule.h\n"+e.getMessage(),"Error Message", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}
	
	/**
	 * Write the InTx for all routers in SC_OutputModule.
	 * @param data_output
	 * @throws Exception
	 */
	public void writeSCOAllInTx(DataOutputStream data_output) throws Exception {
		for (int x=0; x<dimension; x++){
			if(x!=0) data_output.writeBytes("\t\telse ");
			else data_output.writeBytes("\t\t");
			data_output.writeBytes("if(_index == "+x+") return ((intx"+x+" == SC_LOGIC_1)&&(inlane_tx"+x+".read().get_bit(_lane) == SC_LOGIC_1))?1:0;\n");
		}
		data_output.writeBytes("\t\treturn 0;");
	}

	/**
	 * Write the InData for all routers in SC_OutputModule.
	 * @param data_output
	 * @throws Exception
	 */
	public void writeSCOAllInData(DataOutputStream data_output) throws Exception {
		for (int x=0; x<dimension; x++)
			data_output.writeBytes("\t\tif(_index == "+x+") return indata"+x+".read().to_uint();\n");
	}	

	/**
	 * Write the signals for all routers in SC_OutputModule.
	 * @param data_output
	 * @throws Exception
	 */
	public void writeSCOAllSignals(DataOutputStream data_output) throws Exception {
		for (int x=0; x<dimension; x++){
			data_output.writeBytes("\tsc_in<sc_logic> inclock"+x+";\n");
			data_output.writeBytes("\tsc_in<sc_logic> intx"+x+";\n");
			data_output.writeBytes("\tsc_in<sc_lv<constNumLane> > inlane_tx"+x+";\n");
			data_output.writeBytes("\tsc_in<sc_lv<constFlitSize> > indata"+x+";\n");
			data_output.writeBytes("\tsc_out<sc_lv<constNumLane> > outcredit"+x+";\n");
		}
	}	

	/**
	 * Write the variables for all routers in SC_OutputModule.
	 * @param data_output
	 * @throws Exception
	 */
	public void writeSCOAllVariables(DataOutputStream data_output) throws Exception {
		for (int x=0; x<dimension; x++){
			data_output.writeBytes("\tinclock"+x+"(\"inclock"+x+"\"),\n");
			data_output.writeBytes("\tintx"+x+"(\"intx"+x+"\"),\n");
			data_output.writeBytes("\tinlane_tx"+x+"(\"inlane_tx"+x+"\"),\n");
			data_output.writeBytes("\tindata"+x+"(\"indata"+x+"\"),\n");
			data_output.writeBytes("\toutcredit"+x+"(\"outcredit"+x+"\"),\n");
		}
	}	

	/**
	 * Write the OutModule for all routers in SC_OutputModule.
	 * @param data_output
	 * @throws Exception
	 */
	public void writeSCOAllOutModule(DataOutputStream data_output) throws Exception {
		for (int x=0; x<dimension; x++)
			data_output.writeBytes("\t\toutcredit"+x+" = 0xF;\n");
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
	 * Write the SCCOM command (SystemC compilation) for all SC files and link.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeSCCOMFiles(DataOutputStream data_output) throws Exception {
		writeSCCOM(data_output, "SC_NoC/NI.cpp");
		writeSCCOM(data_output, "SC_NoC/trafficFileReader.cpp");
		super.writeSCCOMFiles(data_output);
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
		writeVCOM(data_output, "NOC/Hermes_cv_in.vhd");
		writeVCOM(data_output, "NOC/Hermes_inport.vhd");
		writeVCOM(data_output, "NOC/Hermes_cv_out.vhd");
		writeVCOM(data_output, "NOC/Hermes_outport.vhd");
	}

}
