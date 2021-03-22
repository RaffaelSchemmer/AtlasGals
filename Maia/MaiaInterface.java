package Maia;

import java.awt.event.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;

import AtlasPackage.NoCGenerationGUI;
import AtlasPackage.Help;
import AtlasPackage.Project;
import AtlasPackage.NoC;

/**
 * This class creates the Maia GUI allowing select the Hermes Mesh 2D NoC parameters and generate it.
 * @author Aline Vieira de Mello
 * @version
 */
public class MaiaInterface extends NoCGenerationGUI implements ActionListener{
	private Project project;
	private NoC noc;
	
	/**
	 * Constructor class.
	 * @param project The project where the NoC will be generated.
	 */
	public MaiaInterface(Project project){
		super("MAIA   "+project.getPath());
		this.project = project;
		this.noc = project.getNoC();
		addComponents();
		super.setVisible(true);
	}

	/**
	 * Add components in the Maia GUI.
	 */
	private void addComponents(){
		int x=10;
		int y=2;
		int dimx=160;
		int dimy=56;
		//Menu Bar with Help
		addMenu("About Maia",this);
		//GAPH Icon
		addGaphIcon(690,y,90,27);
		//Flow control
		y=y+20;
		String[] availableFlowControl = {"Handshake","CreditBased"};
		addFlowControl(x, y, dimx, dimy, availableFlowControl, noc.getFlowControl(), this);
		//Number of Virtual channels
		y=y+63;
		String[] availableVirtualChannel = { "1","2","4"};
		addVirtualChannel(x, y, dimx, dimy, availableVirtualChannel, ""+noc.getVirtualChannel(), this);
		//Scheduling algorithm
		y=y+63;
		String[] availableScheduling = {"RoundRobin","Priority"};
		addScheduling(x, y, dimx, dimy, availableScheduling, noc.getScheduling(), this);
		//NoC dimensionsh
		y=y+63;
		String[] dimension = { "2", "3", "4", "5", "6", "7", "8", "9", "10", "11","12", "13", "14", "15", "16"};
		addDimensions(x, y, dimx, dimy, dimension, ""+noc.getNumRotX(), ""+noc.getNumRotY(), this);
		//Flit width
		y=y+63;
		String[] availableFlitWidth = { "8", "16", "32", "64" };
		addFlitWidth(x, y, dimx, dimy, availableFlitWidth, ""+noc.getFlitSize(), this);
		//Buffer depth
		y=y+63;
		String[] availableDepth = { "4", "8", "16", "32" };
		addBuffer(x, y, dimx, dimy, availableDepth, ""+noc.getBufferDepth());
		//routing algorithm
		y=y+63;
		String[] availableAlgorithm = {"Algorithm XY","Algorithm WFM","Algorithm WFNM","Algorithm NLM","Algorithm NLNM","Algorithm NFM","Algorithm NFNM"};
		String selectedAlgorithm = "";
		addRoutingAlgorithm(x, y, dimx, dimy, availableAlgorithm, selectedAlgorithm);
		//SC test bench
		y=y+63;
		//addSCTestBench(x, y, dimx, 51, noc.isSCTB(), this);
		//Generate button
		y=y+84;
		addGenerateButton(x, y, dimx, 40, this);
		//Panel with NoC topology
		addNoCPanel(180, 30, 600, 557, noc.getType());
		//disable scheduling or routing algorithm according to the number of VCs
		if(getVirtualChannelSelected().equalsIgnoreCase("1")){
			getScheduling().setEnabled(false);
		}
		else{
			getRoutingAlgorithm().setEnabled(true);
		}
		setNoc(noc);
	}
	
	/**
	 * Update the project with the NoC parameters.
	 */
	private void updateProject(){
		noc.setFlowControl(getFlowControlSelected());
		noc.setNumRotX(getDimXSelected());
		noc.setNumRotY(getDimYSelected());
		noc.setVirtualChannel(Integer.valueOf(getVirtualChannelSelected()).intValue());
		noc.setScheduling(getSchedulingSelected());
		noc.setFlitSize(Integer.valueOf(getFlitWidthSelected()).intValue());
		noc.setBufferDepth(Integer.valueOf(getBufferDepthSelected()).intValue());
		noc.setAlgorithm(getRoutingAlgorithmSelected());
		//noc.setSCTB(hasSCTestBench());
		noc.setSCTB(true);
		project.setNoCGenerate(true);

		if(getFlowControlSelected().equalsIgnoreCase("Handshake")){
			noc.setCyclesPerFlit(2);
			noc.setCyclesToRoute(7);
			project.setPowerEstimated(true);
		}
		else if(getFlowControlSelected().equalsIgnoreCase("CreditBased")){
			noc.setCyclesPerFlit(1);
			noc.setCyclesToRoute(5);
			
			if(noc.getScheduling().equalsIgnoreCase("RoundRobin"))
				project.setPowerEstimated(true);
			else
				project.setPowerEstimated(false);
		}

		//write the project file.
		project.write();
	}

	/**
	 * Return the routing algorithm selected.
	 * @return The routing algorithm selected in the GUI.
	 */
	public String getRoutingAlgorithmSelected(){
		String algorithm = super.getRoutingAlgorithmSelected();
		if(algorithm.equalsIgnoreCase("XY"))
			return "AlgorithmXY";
		else if(algorithm.equalsIgnoreCase("West-First Minimal"))
			return "AlgorithmWFM";
		else if(algorithm.equalsIgnoreCase("West-First Non-Minimal"))
			return "AlgorithmWFNM";
		else if(algorithm.equalsIgnoreCase("North-Last Minimal"))
			return "AlgorithmNLM";
		else if(algorithm.equalsIgnoreCase("North-Last Non-Minimal"))
			return "AlgorithmNLNM";
		else if(algorithm.equalsIgnoreCase("Negative-First Minimal"))
			return "AlgorithmNFM";
		else if(algorithm.equalsIgnoreCase("Negative-First Non-Minimal"))
			return "AlgorithmNFNM";
		return "AlgorithmXY";
	}
	
	/**
	 * Generate the NoC with the selected parameters.
	 */
	private void generate(){
		updateProject();

		if(getFlowControlSelected().equalsIgnoreCase("Handshake")){
			MainHandshake handshake = new MainHandshake(project);
			handshake.generate();
		}
		else if(getFlowControlSelected().equalsIgnoreCase("CreditBased")){
			if(getVirtualChannelSelected().equalsIgnoreCase("1")){
				MainCreditBased creditBased = new MainCreditBased(project);
				creditBased.generate();
			}
			else{
			    if(getSchedulingSelected().equalsIgnoreCase("RoundRobin")){
					MainVirtualChannel virtual = new MainVirtualChannel(project);
					virtual.generate();
				}
				else{
					FixedPriority fp = new FixedPriority(project);
					fp.generate();
				}
			}
		}
		super.dispose();
	}
	
	public void actionPerformed(ActionEvent e){
		if(e.getSource()==getDimX() || e.getSource()==getDimY() || e.getSource()==getFlitWidth()){
			verifyDimension(this);
		}
		else if(e.getSource()==getFlowControl()){
			if(getFlowControlSelected().equalsIgnoreCase("Handshake")){
				if(getVirtualChannel()!=null){
					getVirtualChannel().setSelectedItem("1");
					getVirtualChannel().setEnabled(false);
					getScheduling().setSelectedItem("RoundRobin");
					getScheduling().setEnabled(false);
				}
			}
			else{
				if(getVirtualChannel()!=null){
					getVirtualChannel().setEnabled(true);
				}
			}
		}
		else if(e.getSource()==getVirtualChannel()){
			if(!getVirtualChannelSelected().equalsIgnoreCase("1"))
				getScheduling().setEnabled(true);
			else{
				getScheduling().setSelectedItem("RoundRobin");
				getScheduling().setEnabled(false);
			}
			if(getVirtualChannelSelected().equalsIgnoreCase("1"))
				getRoutingAlgorithm().setEnabled(true);
			else{
				getRoutingAlgorithm().setSelectedItem("XY");
				getRoutingAlgorithm().setEnabled(false);
			}
		}
		else if(e.getSource()==getGenerateButton()){
			if(project.isNoCGenerate()){
				int option = JOptionPane.showConfirmDialog(null,"This option clean throughout the project, removing all files generated by the Traffic Generation and Traffic Evaluation. Do you want to do this?","Question Message", JOptionPane.OK_CANCEL_OPTION);
				if(option == JOptionPane.OK_OPTION){
					project.delete();
					generate();
				}
			}
			else
				generate();
		}
		else if(e.getActionCommand().equalsIgnoreCase("Documentation")){
		    Help.show("https://corfu.pucrs.br/redmine/projects/atlas/wiki/maia");
		}
		else if(e.getActionCommand().equalsIgnoreCase("About Maia"))
			JOptionPane.showMessageDialog(this,"MAIA          10.08.2006\nDeveloped by:\n\t        Aline Vieira de Mello","VERSION 4.0",JOptionPane.INFORMATION_MESSAGE);
	}
}
