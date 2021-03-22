package HermesG;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

import AtlasPackage.Project;
import AtlasPackage.NoC;
import AtlasPackage.Clock;
import AtlasPackage.Router;
import AtlasPackage.NoCGenerationGUI;
import AtlasPackage.Help;

/**
 * This class creates the HermesG GUI allowing select the Hermes Unidirectional NoC parameters and generate it.
 * @author Aline Vieira de Mello
 * @version
 */
public class HermesGInterface extends NoCGenerationGUI implements MouseListener,ActionListener
{
	private Project project;
	private NoC noc;
	private Router r;
	private InterfaceRemoveClock removeClock;
	private InterfaceNewClock newClock;
	private InterfaceEditClock editClock;
	private InterfaceClock interClock;
	private int tmp = 0;
	/**
	 * Constructor class.
	 * @param project The project where the NoC will be generated.
	 */
	public HermesGInterface(Project project)
	{
		super("Hermes-G   "+project.getStringProjFile());
		this.project = project;
		this.noc = project.getNoC();
		addComponents();
		super.setVisible(true);	
		
	}

	/**
	 * Add components in the HermesG GUI.
	 */
	private void addComponents()
	{
		int x=10;
		int y=2;
		int dimx=160;
		int dimy=56;
		addMenu("About Hermes-G",this);
		addGaphIcon(690,y,90,27);
		y=y+20;
		String[] availableFlowControl = {"CreditBased"};
		addFlowControl(x, y, dimx, dimy, availableFlowControl, noc.getFlowControl(), this);
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
		// "Algorithm WFNM","Algorithm WFM","Algorithm NLNM","Algorithm NLM","Algorithm NFNM","Algorithm NFM" 
		String[] availableAlgorithm = {"Algorithm XY","Algorithm WFM","Algorithm WFNM","Algorithm NLM","Algorithm NLNM","Algorithm NFM","Algorithm NFNM"};
		
		addRoutingAlgorithm(x, y, dimx, dimy, availableAlgorithm, noc.getRoutingAlgorithm());
		y=y+63;	
		addAvaibleClock(x, y, dimx, dimy,noc.getRefClockListString());
		
		y=y+55;
		addClockButton(x, y, dimx, 40, this);
		editClockButton(x,y+60,dimx,40,this);
		removeClockButton(x,y+30,dimx,40,this);
		String[] availableCodes = {"Johnson","Gray"};
		y=y+103;
		selectCodeButton(x,y,dimx,10,availableCodes,noc.getbufferCoding());
		
		// addSCTestBench(x, y, dimx, 51, noc.isSCTB(), this);
		y=y+53;
		addGenerateButton(x, y, dimx, 40, this);
		addNoCPanel(180, 30, 600, 557, noc.getType());
		getNoCPanel().addMouseListener(this);
		setNoc(noc);
		
	}
	
	public void mouseClicked(MouseEvent e)
    {
		noc.setNumRotX(getDimXSelected());
		noc.setNumRotY(getDimYSelected());
		int x = e.getX();
		int y = e.getY();
		if(e.getSource() == getNoCPanel())
		{
			Vector<Router> r =  new Vector<Router>(); /* Cria um Vector de Roteadores */
			r = getNoCPanel().getVetorRouter();      /* Pega o Vetor de roteadores criados */
			
			for(int i=0;i< (noc.getNumRotX() * noc.getNumRotY()) ;i++)
			{
				if(r.get(i).verifyPoint(x,y) == true)
				{
					if(tmp == 0)
					{
						tmp = 1;
						this.getNoCPanel().setEnabled(false);
						interClock = new InterfaceClock(project,r.get(i),noc.getRefClockList(),this);
						interClock.addWindowListener(new WindowAdapter()
						{
							public void windowClosed(WindowEvent e)
							{
								tmp = 0;
							}
						});
					}
				}
			}
		}	
 	}
	
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mousePressed(MouseEvent e){}
	public void mouseReleased(MouseEvent e){}
		
	/**
	 * Update the project with the NoC parameters.
	 */
	private void updateProject()
	{
		noc.setbufferCoding(getCodeSelected());
		noc.setNumRotX(getDimXSelected());
		noc.setNumRotY(getDimYSelected());
		noc.setVirtualChannel(0);
		noc.setFlitSize(Integer.valueOf(getFlitWidthSelected()).intValue());
		noc.setBufferDepth(Integer.valueOf(getBufferDepthSelected()).intValue());
		noc.setAlgorithm(getRoutingAlgorithmSelected());
		// noc.setSCTB(hasSCTestBench());
		project.setNoCGenerate(true);
		project.setTrafficGenerate(false);
		noc.setCyclesPerFlit(1);
		noc.setCyclesToRoute(5);
		//write the project file.
		project.write();
	}
	
	/**
	 * Generate the NoC with the selected parameters
	 */
	private void generate()
	{
	
		updateProject();
		HermesGCreditBased creditBased = new HermesGCreditBased(project);
		creditBased.generate();
		super.dispose();
	}

	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource()==getDimX() || e.getSource()==getDimY())
		{
			verifyDimension(this);
			noc.clearClock();
			noc.initDefClocks(getDimXSelected(),getDimYSelected());
		}
		else if(e.getSource()==getFlitWidth())
		{
			verifyDimension(this);
		}
		else if(e.getSource()==getGenerateButton())
		{
			if(project.isNoCGenerate())
			{
				int option = JOptionPane.showConfirmDialog(null,"This option clean throughout the project, removing all files generated by the Traffic Generation and Traffic Evaluation. Do you want to do this?","Question Message", JOptionPane.OK_CANCEL_OPTION);
				if(option == JOptionPane.OK_OPTION)
				{
					project.delete();
					generate();
				}
			}
			else
			{
				generate();
			}
		}
		else if(e.getSource() == getClockButton())
		{
			this.getClockButton().setEnabled(false);
			newClock = new InterfaceNewClock(project,this,noc.getRefClockList());
			newClock.addWindowListener(new WindowAdapter()
			{
				public void windowClosed(WindowEvent e)
				{
					getClockButton().setEnabled(true);
				}
			});
			
			
		}
		else if(e.getSource() == getEditButton())
		{
			this.getEditButton().setEnabled(false);
			editClock = new InterfaceEditClock(project,this,noc.getRefClockList());
			editClock.addWindowListener(new WindowAdapter()
			{
				public void windowClosed(WindowEvent e)
				{
					getEditButton().setEnabled(true);
				}
			});
			
		}
		else if(e.getSource() == getRemoveClockButton())
		{
			this.getRemoveClockButton().setEnabled(false);
			removeClock = new InterfaceRemoveClock(project,this,noc.getRefClockList());
			
			removeClock.addWindowListener(new WindowAdapter()
			{
				public void windowClosed(WindowEvent e)
				{
					getRemoveClockButton().setEnabled(true);
				}
			});
			
			
		}
		
		else if(e.getActionCommand().equalsIgnoreCase("Documentation"))
		    Help.show("https://corfu.pucrs.br/redmine/projects/atlas/wiki");
		else if(e.getActionCommand().equalsIgnoreCase("About Hermes-G"))
			JOptionPane.showMessageDialog(this,"Hermes-G          19.03.2013\nDeveloped by:\nRaffael Bottoli Schemmer","Warning",JOptionPane.INFORMATION_MESSAGE);
	}
	
}
