package HermesSR;

import javax.swing.*;

import java.io.*;
import java.util.*;

import AtlasPackage.NoCGenerationCB;
import AtlasPackage.NoC;
import AtlasPackage.Project;
import AtlasPackage.SCOutputModuleRouter;
import AtlasPackage.SCOutputModule;
import AtlasPackage.SCInputModule;
import AtlasPackage.ManipulateFile;
import AtlasPackage.Default;

/**
 * Generate a HermesSR NoC without virtual channels.
 * @author Edson Ifarraguirre Moreno
 * @version
 */
public class HermesSRCreditBased extends NoCGenerationCB{

	private static String sourceDir = Default.atlashome + File.separator + "HermesSR" + File.separator + "Data" + File.separator + "CreditBased" + File.separator;

	private String projectDir, nocDir, scDir;
	private String nocType;
	private int dimX, dimY, dimension, flitSize;
	private boolean isSC;
	private NoC noc;

	/**
	 * Generate a HermesSR NoC without virtual channels. 
	 * @param project The NoC project.
	 */
	public HermesSRCreditBased(Project project){
		super(project, sourceDir);
		noc = project.getNoC();
		nocType = noc.getType();
		dimX = noc.getNumRotX();
		dimY = noc.getNumRotY();
		dimension = dimX * dimY;
		flitSize = noc.getFlitSize();
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
		//create HermesPackage.vhd 
		createPackage("HermesPackage.vhd");
		//create NoC file without internal evaluation
		createNoC(false);
		//create the .h that determines the used routing algorithm 
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
  	public void copyNoCFiles(){
		ManipulateFile.copy(new File(sourceDir+"Hermes_inport.vhd"), nocDir);
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
	 * Create the .h file. This file ines the used routing algorithm. 
	 */
	private void createDefRouting(){
		try{
			String d_out = new String(scDir + "defs.h");
			FileOutputStream def_h_file=new FileOutputStream(d_out);
			DataOutputStream do_file=new DataOutputStream(def_h_file);

			do_file.writeBytes("//========== DEFINE ROUTING FILES TO ME USED ==========\n");
			do_file.writeBytes("#ifndef _ROTFILE_DEF\n");
			do_file.writeBytes("\t#define _ROTFILE_DEF \"Routing" + File.separator + File.separator + "pure_xy.rot\"\n");
			do_file.writeBytes("#endif\n");
			do_file.writeBytes("//========== ========== ========== ========== ==========\n");
			do_file.close();
			def_h_file.close();

		}
		catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't write s.h","Input error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		catch(Exception e){
			JOptionPane.showMessageDialog(null,"Error while creating s.h file.\n"+e.getMessage(),"Input error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}

	/**
	 * Create the XY routing algorithm file.
	 */
	private void createXYRouting(){
		String sPath;
		String routing = projectDir + "Routing" + File.separator + "pure_xy.rot";

		try{
			FileOutputStream cool_routing=new FileOutputStream(routing);
			DataOutputStream data_routing=new DataOutputStream(cool_routing);

			for (int source=0; source<dimension; source++){
				for (int target=0; target<dimension; target++){
					sPath=new String(XYPath(source,target));
					if(sPath.length()!=0)
						data_routing.writeBytes(source+";"+target+";"+sPath.length()+";"+sPath+"\n");
				}
			}
			data_routing.close();

		}catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't write "+routing+" file\n" + f.getMessage(),"Output error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}catch(Exception e){
			JOptionPane.showMessageDialog(null,e.getMessage(),"Error Message", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}

		noc.setRouting(routing);
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
					if(sourceX<targetX){ // go to EAST
						sPath+="0";
						sourceX++;
					}
					else{
						sPath+="1";
						sourceX--;
					}
				}
				else if(sourceY!=targetY){
					if(sourceY<targetY){ // go to NORTH
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
* SystemC
*********************************************************************************/

	/**
	 * Creates the SystemC files
	 */
	public void createSC(){
		//copy the .cpp files to SC_NoC directory
		copySCFiles();
		//copy NI.cpp file to SC_NoC directory
		ManipulateFile.copy(new File(sourceDir+"NI.cpp"), scDir);
		//create Ni.h
		createSCNI(noc.getRouting());
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
		
	/**
	 * Create the NI.h file.
	 * @param _rotFile The file of the used routing algorithm. 
	 */
	private void createSCNI(String _rotFile){
		String line, word, change_parameter;
		String lineRot;
		String sHeaderSize, sPath, sMallocPathVector, sStaticPath, sCopyPath;
		String sSource, sTarget, path;
		int nFlits;
		StringTokenizer st, stRot;

		try{
			FileInputStream inFile = new FileInputStream(new File(sourceDir + "NI.h"));
			BufferedReader buff=new BufferedReader(new InputStreamReader(inFile));

			FileOutputStream outFile = new FileOutputStream(scDir + "NI.h");
			DataOutputStream data_output=new DataOutputStream(outFile);

			FileInputStream cool_routing=new FileInputStream(new File(_rotFile));
			DataInputStream data_routing=new DataInputStream(cool_routing);
			BufferedReader buffRot=new BufferedReader(new InputStreamReader(cool_routing));

			change_parameter="";
			line=buff.readLine();
			while(line!=null){
				st = new StringTokenizer(line, "$");
				int vem = st.countTokens();

				for (int cont=0; cont<vem; cont++){
					change_parameter="";
					word = st.nextToken();
					if(word.equalsIgnoreCase("XY")){
						word = change_parameter.concat(Integer.toString(dimension));
						data_output.writeBytes(word);
					}
					else if(word.equalsIgnoreCase("NI_CTOR")){
						word = change_parameter;
						data_output.writeBytes(word);						
						
						// read used routing file
						do{
							lineRot=buffRot.readLine();
							if(lineRot!=null){
								stRot = new StringTokenizer(lineRot, ";");
								sSource=stRot.nextToken();
								sTarget=stRot.nextToken();
								stRot.nextToken(); //discarded the number of hops in the path
								path = stRot.nextToken();
								sPath  = getHeader(path);
								nFlits = getNFlits(path);
								sHeaderSize=new String("\tHeaderSize["+sSource+"]["+sTarget+"]="+ nFlits +";\n");
								data_output.writeBytes("\n");
								data_output.writeBytes(sHeaderSize);
								sMallocPathVector=new String("\trouting["+sSource+"]["+sTarget+"]=(unsigned long int*)calloc( sizeof(unsigned long int) ,"+ nFlits +");\n");
								data_output.writeBytes(sMallocPathVector);
								sStaticPath=new String("\tstatic unsigned long int path_"+sSource+"_"+sTarget+"["+nFlits+"]={"+ sPath + "};\n");
								data_output.writeBytes(sStaticPath);
								sCopyPath=new String("\tmemcpy( routing["+sSource+"]["+sTarget+"], path_"+sSource+"_"+sTarget+", sizeof(unsigned long int)*"+ nFlits +"); \n");
								data_output.writeBytes(sCopyPath);
							}							
						}while(lineRot!=null);
						data_routing.close();
						cool_routing.close();
						buffRot.close();
					}
					else {
						data_output.writeBytes(word);
					}
				}//end for
				data_output.writeBytes("\r\n");
				line=buff.readLine();
			} //end while
			buff.close();
			data_output.close();
			inFile.close();
			outFile.close();

		}catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't write NI.h","Output error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}catch(Exception e){
			JOptionPane.showMessageDialog(null,"Can't write NI.h\n"+e.getMessage(),"Error Message", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}
	
	/**
	 * Return the flits of packet header dedicate to the routing.
	 * @param path The list of ports in the packet path.
	 * @return A String containing the flits of packet header dedicate to the routing.
	 */
	private String getHeader(String path){
		int counter=0;
		int nbytes = flitSize/4;
		String sPath = new String("");
		String sFlit = new String("");
		for(int i=0; i<path.length(); i++){
			sFlit+=path.charAt(i);
			counter++;
			if(counter==nbytes){
				counter=0;
				sPath+= "0x"+sFlit+", ";
				sFlit="";
			}
		}
		//if the last flit is not completed
		if(counter!=0){
			while((counter<nbytes)){
				sFlit+="f";
				counter++;
			}
			sPath+= "0x"+sFlit+", ";
		}

		//generate end of header
		counter=0;
		sFlit="";
		while(counter<nbytes){
			sFlit+="f";
			counter++;
		}
		sPath+= "0x"+sFlit+", ";

		return sPath;
	}

	/**
	 * Return the number of flits in packet header dedicate to the routing.
	 * @param path The list of ports in the packet path.
	 * @return The number of flits in packet header dedicate to the routing.
	 */
	private int getNFlits(String path){
		int counter=0;
		int nFlits=0;
		int nbytes = flitSize/4;
		for(int i=0; i<path.length(); i++){
			counter++;
			if(counter==nbytes){
				counter=0;
				nFlits++;	
			}
		}
		if(counter!=0){ // if the last flit is not completed
			nFlits++;
		}
		nFlits++; //flit filled with ffff.. indicating the end of header
		return nFlits;
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
		writeVCOM(data_output, "NOC/HermesPackage.vhd");
		writeVCOM(data_output, "NOC/Hermes_inport.vhd");
		writeVCOM(data_output, "NOC/Hermes_outport.vhd");
	}

}
