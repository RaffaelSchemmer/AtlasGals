package HermesTB;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.*;

import AtlasPackage.NoCGenerationGUI;
import AtlasPackage.Project;
import AtlasPackage.NoC;
import AtlasPackage.Help;
import AtlasPackage.Router;

/**
 * This class creates the HermesTB GUI allowing select the Hermes Torus Bidirectional NoC parameters and generate it.
 * @author Aline Vieira de Mello
 * @version
 */
public class HermesTBInterface extends NoCGenerationGUI implements MouseListener,ActionListener{
	private Project project;
	private NoC noc;
	private Router r;
	/**
	 * Constructor class.
	 * @param project The project where the NoC will be generated.
	 */
	public HermesTBInterface(Project project){
		super("Hermes-TB   "+project.getStringProjFile());
		this.project = project;
		this.noc = project.getNoC();
		addComponents();
		super.setVisible(true);
	}

	/**
	 * Add components in the HermesTB GUI.
	 */
	private void addComponents(){
		int x=10;
		int y=2;
		int dimx=160;
		int dimy=56;
		addMenu("About Hermes-TB",this);
		addGaphIcon(690,y,90,27);
		y=y+20;
		String[] availableFlowControl = {"CreditBased"};
		addFlowControl(x, y, dimx, dimy, availableFlowControl, noc.getFlowControl(), this);
		y=y+63;
		String[] availableVirtualChannel = {"1"};
		addVirtualChannel(x, y, dimx, dimy, availableVirtualChannel, ""+noc.getVirtualChannel(), this);
		y=y+63;
		String[] availableScheduling = {"RoundRobin"};
		addScheduling(x, y, dimx, dimy, availableScheduling, noc.getScheduling(), this);
		y=y+63;
		String[] dimension = { "2", "3", "4", "5", "6", "7", "8", "9", "10", "11","12", "13", "14", "15", "16"};
		addDimensions(x, y, dimx, dimy, dimension, ""+noc.getNumRotX(), ""+noc.getNumRotY(), this);
		y=y+63;
		String[] availableFlitWidth = { "8", "16", "32", "64" };
		addFlitWidth(x, y, dimx, dimy, availableFlitWidth, ""+noc.getFlitSize(), this);
		y=y+63;
		String[] availableDepth = { "4", "8", "16", "32" };
		addBuffer(x, y, dimx, dimy, availableDepth, ""+noc.getBufferDepth());
		y=y+63;
		String[] availableAlgorithm = {"West-First Non-Minimal for Torus"};
		addRoutingAlgorithm(x, y, dimx, dimy, availableAlgorithm, noc.getRoutingAlgorithm());
		y=y+63;
//		addSCTestBench(x, y, dimx, 51, noc.isSCTB(), this);
		y=y+84;
		addGenerateButton(x, y, dimx, 40, this);
		addNoCPanel(180, 30, 600, 557, noc.getType());
		getNoCPanel().addMouseListener(this);
		//Fixed parameters
		getFlowControl().setEnabled(false);
		getVirtualChannel().setEnabled(false);
		getScheduling().setEnabled(false);
		getRoutingAlgorithm().setEnabled(false);
		setNoc(noc);
	}
	
	
		////////////////////////////////////////////////////////////////////////////////////
		
	        public void mouseClicked(MouseEvent e)
       	{
        	int x = e.getX();
		int y = e.getY();
		if(e.getSource() == getNoCPanel())
		{
			updateProject();
			Vector<Router> r =  new Vector<Router>(); /* Cria um Vector de Roteadores */
			r = getNoCPanel().getVetorRouter(); /* Pega o Vetor de roteadores criados */
			
			System.out.println("Eixo X: " + noc.getNumRotX() + "Eixo Y : " + noc.getNumRotY());
			for(int i=0;i< (noc.getNumRotX() * noc.getNumRotY()) ;i++)
			{
				if(r.get(i).verifyPoint(x,y) == true) /* Pesquisa por todo Vetor de roteadores até encontrar qual foi clicado */
				{
					System.out.println("Endereço Roteador selecionado : " + r.get(i).getAddress());
					// new InterfaceClock(project,r.get(i));
				}
			}
			
		}	
 	}

	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mousePressed(MouseEvent e){}
	public void mouseReleased(MouseEvent e){}

	////////////////////////////////////////////////////////////////////////////////////
		
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
//		noc.setSCTB(hasSCTestBench());
		project.setNoCGenerate(true);
		project.setTrafficGenerate(false);
		noc.setCyclesPerFlit(1);
		noc.setCyclesToRoute(5);
		//write the project file.
		project.write();
	}

	/**
	 * Generate the NoC with the selected parameters.
	 */
	private void generate(){
		updateProject();

		HermesTBCreditBased creditBased = new HermesTBCreditBased(project);
		creditBased.generate();

		super.dispose();
	}

	public void actionPerformed(ActionEvent e){
		if(e.getSource()==getDimX() || e.getSource()==getDimY() || e.getSource()==getFlitWidth()){
			verifyDimension(this);
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
		else if(e.getActionCommand().equalsIgnoreCase("Documentation"))
		    Help.show("https://corfu.pucrs.br/redmine/projects/atlas/wiki");
		else if(e.getActionCommand().equalsIgnoreCase("About Hermes-TB"))
			JOptionPane.showMessageDialog(this,"Hermes-TB          10.01.2007\nDeveloped by:\n\t        Carlos Adail Scherer Jr.","VERSION 1.0",JOptionPane.INFORMATION_MESSAGE);
	}
}
