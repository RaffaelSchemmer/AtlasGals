package Hefestus;

import javax.swing.*;
import java.io.*;

import AtlasPackage.NoCGenerationHS;
import AtlasPackage.NoC;
import AtlasPackage.Default;
import AtlasPackage.Project;


/**
 * Generate a Hermes NoC with Handshake flow control and monitors.
 * @author Aline Vieira de Mello
 * @version
 */
public class Handshake extends NoCGenerationHS{
    private static String sourceDir = Default.atlashome + File.separator + "Hefestus" + File.separator + "Data" + File.separator + "Handshake" + File.separator;

    private String projectDir, nocDir; 
    private String algorithm;
    private int dimX, dimY, flitW;
    private boolean isSC;
    private String timeWindow,simulationResolution;
    private int simulationTime;

	/**
	 * Generate a Hermes NoC with Handshake flow control and monitors.
	 * @param project The NoC project.
	 * @param simTime The simulation time.
	 * @param simResolution The resolution of simulation time. For instance: ns, ps or ms.   
	 */
    public Handshake(Project project,int simTime, String simResolution){
    	super(project, sourceDir);
		timeWindow = project.getTimeWindow();
    	NoC noc = project.getNoC();
		simulationTime = simTime;
		simulationResolution = simResolution;
		dimX = noc.getNumRotX();
		dimY = noc.getNumRotY();
		flitW = noc.getFlitSize();
		algorithm = noc.getRoutingAlgorithm();
		isSC = noc.isSCTB();
		projectDir = project.getPath() + File.separator;
		nocDir     = projectDir + "NOC_monitores" + File.separator;
	}

    /**
	 * Generate the NoC and SC files
	 */
	public void generate(){
		Monitor.makeDiretories(projectDir);
		//copy NoC VHDL files
		Monitor.copyFiles(sourceDir, nocDir);
		//create routers with monitors
		Monitor.createRouters(sourceDir, nocDir, dimX, dimY, algorithm, timeWindow);
		//create NOC with monitors
		createNoCMonitor();
		//create the TXT file used by GNUPLOT to show the NoC power consumption graph
		Monitor.createGnuplotScript(projectDir);
		//create the simulate_monitor.do file
		createSimulateScript();
		//create the list.do file
		Monitor.createListScript(projectDir, timeWindow, dimX, dimY, flitW);
	}

/*********************************************************************************
* NOC
*********************************************************************************/
	/**
	 * Create the NoC with monitor.
	 */
	private void createNoCMonitor(){
		try{
			DataOutputStream data_output=new DataOutputStream(new FileOutputStream(nocDir + "NOC_monitores.vhd"));

			//NoC libraries (add monitor package)
			writeNoCLibraries(data_output);
						
			//generate the NoC entity
			writeNoCEntity(data_output);

			//generate the architecture
			data_output.writeBytes("architecture NOC of NOC is\n\n");

			//generate the NoC signals
			writeAllNoCSignals(data_output);

			//monitor signals
			data_output.writeBytes(Monitor.getSignals(dimX, dimY, flitW));
			
			data_output.writeBytes("begin\n\n");
											
			//generate definition of monitor signals
			data_output.writeBytes(Monitor.getInitSignals(Monitor.HS, dimX, dimY, flitW, 0));
			
			//generate routers port map
			writeAllRoutersPortMap(data_output);
			
			//generate monitors port map
			data_output.writeBytes(Monitor.getPortMap(Monitor.HS, dimX, dimY, flitW));

			//generate all routers connections
			writeAllConnections(data_output);

			//If SC option is selected then generate SC entity
			if(isSC){
				writeSCEntity(data_output);
			}

			data_output.writeBytes("end NOC;\n");
			data_output.close();
			
		} catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't write Noc.vhd\n" + f.getMessage(),"Output error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		} catch(Exception e){
			JOptionPane.showMessageDialog(null,"Can't write Noc.vhd\n" + e.getMessage(),"Error Message", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}

	/**
	 * write NoC libraries.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeNoCLibraries(DataOutputStream data_output) throws Exception{
		super.writeNoCLibraries(data_output);
		data_output.writeBytes("use work.MonitorPackage.all;\n\n");
	}	
	
/*********************************************************************************
* SCRIPTS
*********************************************************************************/
	
	/**
	 * Create the simulation script used by Modelsim. 
	 */
	public void createSimulateScript(){
		try{
			FileOutputStream script = new FileOutputStream(projectDir + "simulate_monitores.do");
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
			// do list.do
			writeDO(data_output, "list.do");
			// run simulation
			writeRUN(data_output, simulationTime, simulationResolution);
			// write list
			writeList(data_output, "list.txt");
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
		// vcom internal modules of router
		writeVCOMInternalRouter(data_output);
		// vcom internal modules of router with monitor
		data_output.writeBytes(Monitor.getVCOMInternalRouter());
		// vcom All routers
		writeVCOMRouters(data_output);
		// vcom All routers with monitor
		data_output.writeBytes(Monitor.getVCOMRouters(dimX, dimY));
		// vcom NoC with monitor
		data_output.writeBytes(Monitor.getVCOMNoC());
		// vcom topNoC file
		writeVCOM(data_output, "topNoC.vhd");
	}

}
