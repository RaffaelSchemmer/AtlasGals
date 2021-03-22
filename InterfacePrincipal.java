import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.io.*;

import AtlasPackage.Project;
import AtlasPackage.Default;
import AtlasPackage.ExampleFileFilter;
import AtlasPackage.Help;
import AtlasPackage.JPanelImage;

import Maia.MaiaInterface;
import Jupiter.JupiterInterface;
import HermesTU.HermesTUInterface;
import HermesTB.HermesTBInterface;
import HermesCRC.CRCInterface;
import HermesSR.HermesSRInterface;
import HermesG.HermesGInterface;
import TrafficMbps.TrafficMbps;
import TrafficMeasurer.OpenWindow;
import Hefestus.Power;

/**
 * Creates the Atlas environment GUI allowing to launch all tools in the Atlas project flow. 
 * @author Aline Vieira de Mello
 * @version
 */
public class InterfacePrincipal extends JFrame implements ActionListener
{
	private Project project;
	private JFrame nocGenerate;
	private JFrame trafficGenerate;
	private JFrame simulation;
	private JFrame trafficMeasurer;
	private JFrame powerEvaluation;
	private ProjectWindow newProjectFrame;
		
	private JMenu menuProject;
	private JMenuItem menuNew,menuOpen,menuExit;
	private JMenuBar menuBar;
	private JButton	btnGenerate,btnTraffic,btnSimulation,btnEvaluation,btnPower;
	private JFileChooser filechooser;

    private boolean modelsimVariable;
    private boolean gnuplotVariable;
    private String NO_PROJECT_OPEN = "ATLAS     No project currently open";

    /**
	 * Creates the Atlas environment GUI  
	 */
    public InterfacePrincipal(){
		super();
		setTitle(NO_PROJECT_OPEN);
		int dimx=800;
		setSize(dimx,100);
		Dimension resolucao=Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((resolucao.width-dimx)/2,(resolucao.height-160)/15);
		setResizable(false);
		addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){end();}});
		getContentPane().setLayout(null);
		modelsimVariable = false;
		gnuplotVariable = false;
		initialTests();
		addComponents();
		repaint();
		setVisible(true);
	}
    
    /**
     * Test the required environment variables and paths:
     * <li> ATLAS_HOME set to Atlas installation path
     * <li> ModelSim path add to PATH environment variable
     * <li> Gnuplot path add to PATH environment variable
     * <li> Web browser executable file
     * Additional information in README file.    
     */
    private void initialTests(){
		testAtlasHome();
		modelsimVariable = testModelsim();
		gnuplotVariable = testGnuplot();
		testBrowser();
    }

    /**
     * Test if ATLAS_HOME has been set. In negative case, closes the Atlas environment.
     */
    private void testAtlasHome(){
        String home = Default.atlashome;
    	if(home==null || home.equals("")){
		    JOptionPane.showMessageDialog(this,"ATLAS_HOME environment variable do not exists.\n" +
		    		                           "Please set the ATLAS_HOME environment variable to the Atlas installation diectory.\n Additional information in README file.","Warning Message", JOptionPane.YES_NO_OPTION);
	    	System.exit(0);
    	}
    	else{
    		File atlas = new File(home + File.separator + "Atlas.java");
    		if(!atlas.exists()){
    		    JOptionPane.showMessageDialog(this,"The ATLAS_HOME environment variable does not set to the Atlas installation directory.\n " +
    		    								   "Please set the ATLAS_HOME environment variable to the Atlas installation directory.\n " +
    		    								   "Additional information in README file.","Warning Message", JOptionPane.YES_NO_OPTION);
    	    	System.exit(0);
    		}
    	}
    }
		    
	/**
     * Test if Modelsim's path is included in path.
     * @return b
     */
    private boolean testModelsim(){
		try{
			Process p = Runtime.getRuntime().exec(Default.vsim);
			p.destroy();
		}catch(IOException f){
		    int option = JOptionPane.showConfirmDialog(this,"You must add the Modelsim's path to PATH environment variable.\nWithout Modelsim's path, the simulation cannot be executed.\nDo you want continue?","Warning Message", JOptionPane.YES_NO_OPTION);
		    if(option==JOptionPane.NO_OPTION)
		    	System.exit(0);
		}
		return true;
    }
    
    /**
     * Test if Gnuplot's path is included in path.
     */
    private boolean testGnuplot(){
		try{
			Process p = Runtime.getRuntime().exec(Default.gnuplot);
			p.destroy();
		}catch(IOException f){
		    int option = JOptionPane.showConfirmDialog(this,"You must add the Gnuplot's path to PATH environment variable.\nWithout Gnuplot's Path, the evaluation phase will be disabled.\nDo you want continue?","Warning Message", JOptionPane.YES_NO_OPTION);
		    if(option==JOptionPane.NO_OPTION)
		    	System.exit(0);
		    else
		    	return false;
		}
		return true;
    }
    
    /**
     * Test if the browser executable file is defined. 
     * In negative case, a JFileChooser is showed allowing the file selection. 
     */
    private void testBrowser(){
		boolean error;
		File defaultFile = Default.getConfigFile();
		if(!defaultFile.exists() || Default.getBrowserFile().equals("")){
		    JOptionPane.showMessageDialog(this,"Atlas needs a web browser to show its help files.\nYou must select a browser executable file.","Warning Message",JOptionPane.WARNING_MESSAGE);
		    do{
				error = false;
				JFileChooser filechooser = new JFileChooser();
				filechooser.setDialogTitle("Select the browser executable file"); 
				int intOption = filechooser.showOpenDialog(null);
				if(intOption == JFileChooser.APPROVE_OPTION){
				    File file = filechooser.getSelectedFile();
				    String browser = file.getAbsolutePath();
				    try{
						Process p = Runtime.getRuntime().exec(browser);
						p.destroy();
						Default.setBrowserFile(browser);
				    } catch(Exception exc){
						JOptionPane.showMessageDialog(this,"The selected file is not a browser executable file.","Error Message",JOptionPane.ERROR_MESSAGE);
						error = true;
				    }
				}
				else{
					Default.setBrowserFile("");
				    JOptionPane.showMessageDialog(this,"The help files wont be showed.","Warning Message",JOptionPane.WARNING_MESSAGE);
				}
		    }while(error);
		}
    }
    
    /**
     * add all components to Atlas environment GUI.
	*/
    private void addComponents(){
		int y=10;
		int dimx=140;
		int dimy=30;
		int x=15;
		addMenuBar();
		//addLogoGaph((1000-110),10,90,27);
		addBtnGenerate(x,y,dimx,dimy);
		x+=dimx+10;
		addBtnTraffic(x,y,dimx,dimy);
		x+=dimx+10;
		addBtnSimulation(x,y,dimx,dimy);
		x+=dimx+10;
		dimx=160;
		addBtnEvaluation(x,y,dimx,dimy);
		x+=dimx+10;
		dimx=140;
		addBtnPower(x,y,dimx,dimy);
	}

	private void addMenuBar(){
		menuProject = new JMenu("Projects");

		menuNew = new JMenuItem("New Project");
		menuNew.addActionListener(this);
		menuProject.add(menuNew);

		menuOpen = new JMenuItem("Open...");
		menuOpen.addActionListener(this);
		menuProject.add(menuOpen);

		menuProject.addSeparator();

		menuExit = new JMenuItem("Exit");
		menuExit.addActionListener(this);
		menuProject.add(menuExit);

		JMenuItem helpTopics=new JMenuItem("Documentation");
		helpTopics.addActionListener(this);
		JMenuItem aboutAtlas=new JMenuItem("About Atlas");
		aboutAtlas.addActionListener(this);

		JMenu menuHelp=new JMenu("Help");
		menuHelp.add(helpTopics);
		menuHelp.add(aboutAtlas);

		menuBar = new JMenuBar();
		menuBar.add(menuProject);
		menuBar.add(menuHelp);
		setJMenuBar(menuBar);
	}

	private void addLogoGaph(int x,int y,int dimx,int dimy){
		String gaphImage = System.getenv("ATLAS_HOME")+File.separator+"Images"+File.separator+"logo-gaph.gif";
		String gaphWeb = "http://www.inf.pucrs.br/~gaph";
		JPanelImage jpanel=new JPanelImage(gaphImage,gaphWeb);
		jpanel.setToolTipText("Double Click to Visit the Developer homepage.");
		jpanel.setBounds(x,y,dimx,dimy);
		jpanel.repaint();
		getContentPane().add(jpanel);
	}

	private void addBtnGenerate(int x,int y,int dimx,int dimy){
		btnGenerate=new JButton("NoC Generation");
		btnGenerate.setToolTipText("Generates a custom NOC.");
		btnGenerate.setBounds(x,y,dimx,dimy);
		btnGenerate.addActionListener(this);
		btnGenerate.setEnabled(false);
		getContentPane().add(btnGenerate);
	}

	private void addBtnTraffic(int x,int y,int dimx,int dimy){
		btnTraffic=new JButton("Traffic Generation");
		btnTraffic.setToolTipText("Generates a NoC traffic.");
		btnTraffic.setBounds(x,y,dimx,dimy);
		btnTraffic.addActionListener(this);
		btnTraffic.setEnabled(false);
		getContentPane().add(btnTraffic);
	}

	private void addBtnSimulation(int x,int y,int dimx,int dimy){
		btnSimulation=new JButton("Simulation");
		btnSimulation.setToolTipText("Simulate the NoC generated.");
		btnSimulation.setBounds(x,y,dimx,dimy);
		btnSimulation.addActionListener(this);
		btnSimulation.setEnabled(false);
	    getContentPane().add(btnSimulation);
	}

	private void addBtnEvaluation(int x,int y,int dimx,int dimy){
		btnEvaluation=new JButton("Performance Evaluation");
		btnEvaluation.setToolTipText("Evaluate the NoC performance.");
		btnEvaluation.setBounds(x,y,dimx,dimy);
		btnEvaluation.addActionListener(this);
		btnEvaluation.setEnabled(false);
		if(modelsimVariable && gnuplotVariable)
		    getContentPane().add(btnEvaluation);
	}

	private void addBtnPower(int x,int y,int dimx,int dimy){
		btnPower=new JButton("Power Evaluation");
		btnPower.setToolTipText("Evaluate the NoC power consumption.");
		btnPower.setBounds(x,y,dimx,dimy);
		btnPower.addActionListener(this);
		btnPower.setEnabled(false);
		if(modelsimVariable && gnuplotVariable)
		    getContentPane().add(btnPower);
	}

	public void actionPerformed(ActionEvent e){
		if(e.getSource() == menuExit){
			int intOption = JOptionPane.showConfirmDialog(null,"Are you sure you want to exit?", "Exit",  JOptionPane.YES_NO_OPTION);
			if(intOption == JOptionPane.YES_OPTION)
				end();
		}
		else if(e.getSource() == menuNew){
			closeTools();
			newProjectFrame = new ProjectWindow();
			newProjectFrame.addWindowListener(new WindowAdapter(){
				public void windowClosed(WindowEvent e){
					if(newProjectFrame.hasNewProject()){
						project = new Project(Default.getProjectFile());
						setButtonsEnabled();
					}
				}
			});
		}
		else if(e.getSource() == menuOpen){
			closeTools();
			openProject();
		}
		else if(e.getSource() == btnGenerate)
		{
			
			btnGenerate.setEnabled(true);
			if(project.getNoC().getType().equalsIgnoreCase("HermesG")) { project.getNoC().setTraffic(1); }
			else project.refresh();
			generateNoC();
		}
		else if(e.getSource() == btnTraffic)
		{
			btnTraffic.setEnabled(true);
			if(project.getNoC().getType().equalsIgnoreCase("HermesG")) { project.getNoC().setTraffic(1); }
			else project.refresh();
			generateTraffic();
		}
		else if(e.getSource() == btnSimulation)
		{
			btnSimulation.setEnabled(true);
			if(project.getNoC().getType().equalsIgnoreCase("HermesG")) { project.getNoC().setTraffic(1); }
			else project.refresh();
			simulateNoC();
		}
		else if(e.getSource() == btnEvaluation)
		{
			btnEvaluation.setEnabled(true);
			if(project.getNoC().getType().equalsIgnoreCase("HermesG")) { project.getNoC().setTraffic(1); }
			else project.refresh();
			evaluateTraffic();
		}
		else if(e.getSource() == btnPower)
		{
			btnSimulation.setEnabled(true);
			if(project.isPowerEstimated())
			{
				if(project.getNoC().getType().equalsIgnoreCase("HermesG")) {}
				else project.refresh();
				evaluatePower();
			}
			else
				JOptionPane.showMessageDialog(null,"This NoC configuration has no power estimation available.","Information",JOptionPane.INFORMATION_MESSAGE);
		}
		else if(e.getActionCommand().equalsIgnoreCase("Documentation"))
			Help.show("https://corfu.pucrs.br/redmine/projects/atlas/wiki");
		else if(e.getActionCommand().equalsIgnoreCase("About Atlas"))
			JOptionPane.showMessageDialog(null,"ATLAS          10.03.2013\nDeveloped by:\n       GAPH - Hardware Design Support Group","Based in 1.0.1(Gals Branch)",JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	* Enables the Atlas GUI buttons after open or create a project.
	*/
	private void setButtonsEnabled(){
		setTitle("ATLAS     " + project.getStringProjFile());
		btnGenerate.setEnabled(true);
		btnTraffic.setEnabled(true);
		btnSimulation.setEnabled(true);
		btnEvaluation.setEnabled(true);
		btnPower.setEnabled(false);
	}

	/**
	 * Shows a JFileChooser to allow the file project selection.
	 */
	private void openProject(){
		//selects the file project to be open
		filechooser = new JFileChooser(Default.getProjectPath());
		filechooser.setFileFilter(new ExampleFileFilter("noc",".NoC Files"));
		int intOption = filechooser.showOpenDialog(null);
		if(intOption == JFileChooser.APPROVE_OPTION){
			File file = filechooser.getSelectedFile();
			if(!file.exists()){
				JOptionPane.showMessageDialog(null,"The selected project doesn't exist.","Error Message",JOptionPane.ERROR_MESSAGE);
				openProject();
			}
			else{
				// parent path where the project directory 
				String workspaceDir = file.getParentFile().getParent();
				// path of project directory
				String projectDir = file.getParent();
			    Default.setWorkspacePath(workspaceDir);
			    Default.setProjectPath(projectDir);
			    project = new Project(file);
			    setButtonsEnabled();
			}
		}
	}

	/**
	 * Launches the NoC Generation tool.
	 */
	public void generateNoC(){
		setTitle("ATLAS     " +  project.getStringProjFile());
		
		String type = project.getNoC().getType();
		if(type.equalsIgnoreCase("Hermes"))
			nocGenerate = new MaiaInterface(project);
		else if(type.equalsIgnoreCase("HermesTU"))
			nocGenerate = new HermesTUInterface(project);
		else if(type.equalsIgnoreCase("HermesTB"))
			nocGenerate = new HermesTBInterface(project);
		else if(type.equalsIgnoreCase("HermesSR"))
			nocGenerate = new HermesSRInterface(project);
		else if(type.equalsIgnoreCase("HermesCRC"))
			nocGenerate = new CRCInterface(project);
		else if (type.equalsIgnoreCase("Mercury"))
			nocGenerate = new JupiterInterface(project);
		else if(type.equalsIgnoreCase("HermesG"))
			nocGenerate = new HermesGInterface(project);
		
		nocGenerate.addWindowListener(new WindowAdapter(){
		public void windowClosed(WindowEvent e)
		{
			if(project.getNoC().getType().equalsIgnoreCase("HermesG")) {}
			else { project.refresh(); }
			//the power evaluation is available only for Hermes NoC and with a limited parameter types.
			btnPower.setEnabled(false);
			btnGenerate.setEnabled(true);
			btnTraffic.setEnabled(true);
			btnSimulation.setEnabled(true);
			btnEvaluation.setEnabled(true);
		}
		});
	}

	/**
	 * Launches the Traffic Generation tool.
	 */
	public void generateTraffic()
	{
		project.refresh();
		if(project.isNoCGenerate())
		{
			trafficGenerate = new TrafficMbps(project);
			trafficGenerate.addWindowListener(new WindowAdapter(){
			public void windowClosed(WindowEvent e)
			{
				btnPower.setEnabled(false);
				btnGenerate.setEnabled(true);
				btnTraffic.setEnabled(true);
				btnSimulation.setEnabled(true);
				btnEvaluation.setEnabled(true);
			}
			});
		}
		else
		{
			JOptionPane.showMessageDialog(null,"Generate the NoC first.","Information",JOptionPane.INFORMATION_MESSAGE);
			btnPower.setEnabled(false);
			btnGenerate.setEnabled(true);
			btnTraffic.setEnabled(true);
			btnSimulation.setEnabled(true);
			btnEvaluation.setEnabled(true);

		}
		
	}

	/**
	 * Launches the ModelSim simulation.
	 */
	public void simulateNoC()
	{
		if(project.getNoC().getType().equalsIgnoreCase("HermesG")) {}
		else { project.refresh(); }
		if(project.isTrafficGenerate())
		{
			simulation = new SimulationWindow(project);
			simulation.addWindowListener(new WindowAdapter()
			{
				public void windowClosed(WindowEvent e)
				{
					btnPower.setEnabled(false);
					btnGenerate.setEnabled(true);
					btnTraffic.setEnabled(true);
					btnSimulation.setEnabled(true);
					btnEvaluation.setEnabled(true);
				}
			});
		}
		else
		{
			JOptionPane.showMessageDialog(null,"Generate the NoC's Traffic first.","Information",JOptionPane.INFORMATION_MESSAGE);
			btnPower.setEnabled(false);
			btnGenerate.setEnabled(true);
			btnTraffic.setEnabled(true);
			btnSimulation.setEnabled(true);
			btnEvaluation.setEnabled(true);
		}
		
	}

	/**
	 * Launches the Traffic Evaluation tool.
	 */
	public void evaluateTraffic(){
		if(project.isSimulate())
		{
			trafficMeasurer = new OpenWindow(project);
			trafficMeasurer.addWindowListener(new WindowAdapter()
			{
				public void windowClosed(WindowEvent e)
				{
					btnPower.setEnabled(false);
					btnGenerate.setEnabled(true);
					btnTraffic.setEnabled(true);
					btnSimulation.setEnabled(true);
					btnEvaluation.setEnabled(true);
				}
			});
		}
		else
		{
			JOptionPane.showMessageDialog(null,"Simulate the NoC's Traffic first.","Information",JOptionPane.INFORMATION_MESSAGE);
			btnPower.setEnabled(false);
			btnGenerate.setEnabled(true);
			btnTraffic.setEnabled(true);
			btnSimulation.setEnabled(true);
			btnEvaluation.setEnabled(true);
		}
		
	}

	/**
	 * Launches the Power Evaluation tool.
	 */
	public void evaluatePower(){
		project.refresh();
		if(project.getNoC().getType().equalsIgnoreCase("Hermes")){
			powerEvaluation = new Power(project);
		}
	}

	/**
	 * Close the Atlas tools GUI.
	 */
	private void closeTools(){
		close(newProjectFrame);
		close(nocGenerate);
		close(trafficGenerate);
		close(simulation);
		close(trafficMeasurer);
		close(powerEvaluation);
	}
	
	/**
	 * If JFrame object is not null then calls the dispose method.
	 * @param frame
	 */
	private void close(JFrame frame){
		if(frame != null)
			frame.dispose();
	}

	/**
	* Method called when the Atlas GUI is closed.
	* Before finishing Atlas environment: the project parameters are saved and all Atlas tool GUI are closed.
	*/
	private void end(){
		closeTools();
		if(project != null)
			// project.write();
		System.exit(0);
	}
}
