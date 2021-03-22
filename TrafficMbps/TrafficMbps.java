package TrafficMbps;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.JOptionPane;

import AtlasPackage.Project;
import AtlasPackage.Help;
import AtlasPackage.NoC;
import AtlasPackage.ExampleFileFilter;
import AtlasPackage.Default;
import AtlasPackage.JPanel_Noc;
import AtlasPackage.Scenery;
import AtlasPackage.Router;
import AtlasPackage.ManipulateFile;
import AtlasPackage.NoCGeneration;
import AtlasPackage.Convert;
import HermesG.HermesGCreditBased;

/**
 * TrafficMbps implements the GUI allowing generate traffics.
 * @author Aline Vieira de Mello
 * @version
 */
public class TrafficMbps extends JFrame implements MouseListener,KeyListener,ActionListener{

	private Project project;
	private NoC noc;
	private Scenery scenery;
	private JMenu menuConfig;
	private JMenuItem menuExit, menuOpen,menuOpenCDCG, menuNew, menuStandard, menuDoc, menuAbout;
	private JFrame newSceneryFrame;
	private JPanel_Noc jpanel_noc;
	private JTextField tpath, tname;
	private JButton btn_newSceneryOk, btn_newSceneryCancel;
	private JButton btnGenerate;
	private int nRotX, nRotY;
	private static int dimXCanvas=600;
	private static int dimYCanvas=400;
	private JFileChooser filechooser;
	private ReadTrafficCDCM r;
	private int trafficCDCM = 0;
	private String graph_name;
	/**
	 * Creates the Traffic Generation GUI.
	 * @param project
	 */
	public TrafficMbps(Project project){
		super("Traffic Generation");
		this.project = project;
		initialize();
		setVisible(true);
 	}

	/**
	 * Creates the Traffic Generation GUI with the parameters informed in the file. 
	 * @param project
	 * @param file The scenery file
	 */
	public TrafficMbps(Project project, File file){
		super();
		this.project = project;
		initialize();
		openScenery(file);
		setVisible(true);
	}

	/**
	* Initialize parameters.
	*/
	private void initialize(){
		noc = project.getNoC();
		nRotX = noc.getNumRotX();
		nRotY = noc.getNumRotY();
		scenery = new Scenery(nRotX,nRotY,dimXCanvas,dimYCanvas, noc.isSR4());
		project.setScenery(scenery);
		addProperties();
		addComponents();
 	}

	/**
	* Add JFrame Properties.
	*/
	private void addProperties(){
		getContentPane().setLayout(null);
		setSize(740,480);
		Dimension resolucao=Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((resolucao.width-740)/2,(resolucao.height-480)/2);
		setResizable(false);
		addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){dispose();}});
 	}

	/**
	* add GUI components.
	*/
	private void addComponents(){
		addMenu();
		addCanvas(130,0,dimXCanvas,dimYCanvas);
		addInformation(135,400,450,20);
		addBtnGenerate(15,370,100,30);
	}

 	private void addMenu(){
		JMenuBar menuBar = new JMenuBar();

		menuNew = new JMenuItem("New Scenery");
		menuNew.addActionListener(this);
		menuOpenCDCG = new JMenuItem("Open CDCG Model...");
		menuOpenCDCG.addActionListener(this);
		menuOpen = new JMenuItem("Open...");
		menuOpen.addActionListener(this);
		menuExit = new JMenuItem("Exit");
		menuExit.addActionListener(this);

		JMenu menuTraffic = new JMenu("Manage Scenery");
		menuTraffic.add(menuNew);
		menuTraffic.add(menuOpen);
		if(project.getNoC().getType().equals("HermesG"))
			menuTraffic.add(menuOpenCDCG);
		menuTraffic.addSeparator();
		menuTraffic.add(menuExit);
		
		if(!project.isTrafficGenerate())
			menuOpen.setEnabled(false);

		menuStandard = new JMenuItem("Standard Configuration");
		menuStandard.addActionListener(this);

		menuConfig = new JMenu("Configuration");
		menuConfig.setEnabled(false);
		menuConfig.add(menuStandard);
			
		menuDoc = new JMenuItem("Documentation");
		menuDoc.addActionListener(this);
		menuAbout = new JMenuItem("About Traffic Generation");
		menuAbout.addActionListener(this);

		JMenu menuHelp = new JMenu("Help");
		menuHelp.add(menuDoc);
		menuHelp.add(menuAbout);

		menuBar.add(menuTraffic);
		menuBar.add(menuConfig);
		menuBar.add(menuHelp);
		setJMenuBar(menuBar);
	}

	/**
	* add JPanel to show the NoC topology.
	*/
	private void addCanvas(int x,int y,int dimx ,int dimy){
		jpanel_noc = new JPanel_Noc(x,y,dimx,dimy,project, scenery);
		jpanel_noc.addMouseListener(this);
		getContentPane().add(jpanel_noc);
	}

	private void addInformation(int x,int y,int dimx,int dimy){
		JLabel lInformation = new JLabel("Click on the canvas to configure the traffic parameters of each Router.");
		lInformation.setBounds(x,y,dimx,dimy);
		getContentPane().add(lInformation);
	}

	private void addBtnGenerate(int x,int y,int dimx,int dimy){
		btnGenerate=new JButton("Generate");
		btnGenerate.setToolTipText("Generates the Traffic.");
		btnGenerate.setBounds(x,y,dimx,dimy);
		btnGenerate.addActionListener(this);
		getContentPane().add(btnGenerate);
	}

	/**
	* Show the GUI allowing create a new traffic scenery.
	*/
	private void showJFrameNewScenery(){
		newSceneryFrame = new JFrame("New Scenery...");
		newSceneryFrame.getContentPane().setLayout(null);
		newSceneryFrame.setResizable(false);
		newSceneryFrame.setSize(410,160);
		Dimension resolucao=Toolkit.getDefaultToolkit().getScreenSize();
		newSceneryFrame.setLocation((resolucao.width-410)/2,(resolucao.height-160)/2);
		newSceneryFrame.addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){newSceneryFrame.dispose();}});

		int x=10;
		int y=10;
		int dimy=25;

		JLabel lname = new JLabel("Scenery's name:");
		lname.setBounds(x,y,100,dimy);
		newSceneryFrame.getContentPane().add(lname);
		tname = new JTextField();
		tname.setBounds(x+105,y,275,dimy);
		tname.addKeyListener(this);
		newSceneryFrame.getContentPane().add(tname);

		y+=40;
		JLabel lpath = new JLabel("Scenery's path:");
		lpath.setBounds(x,y,100,dimy);
		newSceneryFrame.getContentPane().add(lpath);
		tpath = new JTextField(project.getPath() +File.separator + "Traffic" + File.separator);
		tpath.setBounds(x+105,y,275,dimy);
		tpath.setEnabled(false);
		newSceneryFrame.getContentPane().add(tpath);

		y+=40;
		btn_newSceneryOk = new JButton("Ok");
		btn_newSceneryOk.setBounds(x+105,y,80,dimy);
		btn_newSceneryOk.addActionListener(this);
		newSceneryFrame.getContentPane().add(btn_newSceneryOk);

		btn_newSceneryCancel = new JButton("Cancel");
		btn_newSceneryCancel.setBounds(x+205,y,80,dimy);
		btn_newSceneryCancel.addActionListener(this);
		newSceneryFrame.getContentPane().add(btn_newSceneryCancel);

		newSceneryFrame.setVisible(true);
	}

	public void keyTyped(KeyEvent e){}
	public void keyPressed(KeyEvent e){}
	public void keyReleased(KeyEvent e){
		tpath.setText(project.getPath()+File.separator + "Traffic" + File.separator+tname.getText());
	}

	/**
	* Show the JFileChosser allowing select a existing scenery.
	*/
	private void selectScenery(){
		JFileChooser filechooser = new JFileChooser(project.getPath()+File.separator + "Traffic");
		if(trafficCDCM == 0)
			filechooser.setFileFilter(new ExampleFileFilter("traffic","Traffic Files"));
		else 
			filechooser.setFileFilter(new ExampleFileFilter("model",".model Files"));
		int intOption = filechooser.showOpenDialog(null);
		if(intOption == JFileChooser.APPROVE_OPTION){
			File file = filechooser.getSelectedFile();
			if(!file.exists()){
				JOptionPane.showMessageDialog(null,"This scenery does not exist.","Input Error", JOptionPane.ERROR_MESSAGE);
				selectScenery();
			}
			else if(trafficCDCM == 1){
				String s = file.getParent() + File.separator + file.getName();
				graph_name = s;
				r = new ReadTrafficCDCM(project,s,file.getName());
				if(r.openFileCDCG() == 0)
				{ 
					trafficCDCM = 1;
					JOptionPane.showMessageDialog(null,"Model load successfully!","Confirm Message",JOptionPane.WARNING_MESSAGE);
				}
				else { trafficCDCM = 0; }
			}
			else if(file.getPath().toLowerCase().lastIndexOf(new String(project.getPath() + File.separator + "Traffic").toLowerCase())==-1){
				JOptionPane.showMessageDialog(null,"This scenery does not belong to "+project.getName()+" project.","Input Error", JOptionPane.ERROR_MESSAGE);
				JOptionPane.showMessageDialog(null,"project traffic path: "+project.getPath()+File.separator + "Traffic","Input Error", JOptionPane.ERROR_MESSAGE);
				JOptionPane.showMessageDialog(null,"traffic file path: "+file.getPath(),"Input Error", JOptionPane.ERROR_MESSAGE);
				selectScenery();
			}
			else if(trafficCDCM == 0){
				openScenery(file);
			}
		}
	}

	/**
	 * Open an existent scenery and update the project and GUI.
	 */
	private void openScenery(File file){
		scenery.open(file);
		updateTitle();
		project.setScenery(scenery);
		project.setSceneryName(scenery.getName());
		project.setTrafficGenerate(true);
		menuConfig.setEnabled(true);
	}
		
	/**
	 * Update JFrame title: <i>Traffic Generation [scenery name] </i> 
	 */
	private void updateTitle(){
		setTitle("Traffic Generation " + scenery.getName());
	}
	
	/**
	 * Save the scenery
	 */
	private void saveScenery(){
		File f = new File(project.getSceneryPath());
		f.mkdirs();
		scenery.save(project.getSceneryFile());
	}

	/**
	 * Generates the traffic
	 */
	
	public void createTrafficInput() /* Geração de tráfego - OK */
	{
		
		/** Traffic SC
		- Proposta de um modelo de tráfego para cada IP : |Source|Target|ProcTime|FlitNumber|SeqNumber|
		 * */
		
		System.out.println(graph_name);
		
		File TrDir1 = new File(project.getPath() + File.separator + "Traffic" + File.separator + "model" + File.separator + "In" + File.separator);
		File TrDir2 = new File(project.getPath() + File.separator + "Traffic" + File.separator + "model" + File.separator + "Out" + File.separator);
		
		ManipulateFile.deleteFiles(""+project.getPath() + File.separator + "Traffic" + File.separator + "model" + File.separator + "In" + File.separator);
		ManipulateFile.deleteFiles(""+project.getPath() + File.separator + "Traffic" + File.separator + "model" + File.separator + "Out" + File.separator);
		
		TrDir1.mkdirs();
		TrDir2.mkdirs();
		
		String xHexa="",yHexa="";
		// Implementar a geração de tráfego aqui (Copiar do file)
		// SpatialDistribution.java
		// TimeDistribution.java	
	}
	public void generate(){
		
		String scDir = null;
		File sDir = null;
		String projectDir = project.getPath() + File.separator;
		if(project.getNoC().getType().equals("HermesG"))
		{
			String sourceDir = Default.atlashome + File.separator + "HermesG" + File.separator + "Data" + File.separator + "CreditBased" + File.separator;
			projectDir = project.getPath() + File.separator;
					
			scDir      = projectDir + "SC_NoC" + File.separator;
					
			sDir=new File(scDir);
			sDir.delete();
			sDir.mkdirs();
				
			HermesGCreditBased h = new HermesGCreditBased(project);
			NoCGeneration n = new NoCGeneration(project,sourceDir);
					
			if(trafficCDCM == 0)
			{
				ManipulateFile.copy(new File(sourceDir + "SC_InputModule.cpp"), scDir);
				ManipulateFile.copy(new File(sourceDir + "SC_OutputModule.cpp"), scDir);
			}
			else
			{
				ManipulateFile.copy(new File(sourceDir + "SC_CDCG_InputModule.cpp"), scDir);
				ManipulateFile.copy(new File(sourceDir + "SC_CDCG_OutputModule.cpp"), scDir);
			}
			int con=0;
			for(int x=0;x<project.getNoC().getNumRotX();x++)
			{
				String xHexa = Convert.decToHex(x,(project.getNoC().getFlitSize()/8));
				for(int y=0;y<project.getNoC().getNumRotY();y++,con++)
				{
					String yHexa = Convert.decToHex(y,(project.getNoC().getFlitSize()/8));
					
					project.getNoC().getClock().get(con).setNameRouter("router" + xHexa + yHexa + h.generateNameRouter(con));
					project.getNoC().getClock().get(con).setRouterInterfaces(h.generateNameRouter(con));
					
				}
			}
			if(trafficCDCM == 0)
			{
				String text = scenery.getSelectedInfo(project.isMapCores());
				JTextArea ta = new JTextArea(text,10,40);
				ta.setEditable(false);
				JScrollPane jsp = new JScrollPane(ta);
				int confirm = JOptionPane.showConfirmDialog(this,jsp, "Information", JOptionPane.OK_CANCEL_OPTION);
				if(confirm==JOptionPane.OK_OPTION)
				{
					scenery.delete(project.getPath() + File.separator + "Traffic");
					project.setSceneryName(scenery.getName());
					project.setTrafficGenerate(true);
					saveScenery();
					
					h.createSimulate(trafficCDCM);
					n.generatetopNoC(trafficCDCM);
					n.generateSystemCOutputModule(trafficCDCM);
					n.generateSystemCIntputModule(trafficCDCM);
					n.generateAsyncFifo();
					new Generate(project, scenery);
					project.write();
					dispose();
				}
			}
			else
			{
				scDir      = projectDir + "SC_NoC" + File.separator;
				sDir=new File(scDir);
				sDir.delete();
				sDir.mkdirs();
			
				String text = scenery.getSelectedInfo(project.isMapCores());
				scenery.delete(project.getPath() + File.separator + "Traffic");
				
				if(trafficCDCM == 1) project.setSceneryName("model");
				else project.setSceneryName(scenery.getName());
				
				project.setTrafficGenerate(true);
				saveScenery();
				
				createTrafficInput();
													
				h.createSimulate(trafficCDCM);
				n.generatetopNoC(trafficCDCM);
				n.generateSystemCOutputModule(trafficCDCM);
				n.generateSystemCIntputModule(trafficCDCM);
				n.generateAsyncFifo();
				new Generate(project, scenery);
				project.write();
				dispose();
			}
		}
		else
		{
			scDir      = projectDir + "SC_NoC" + File.separator;
			sDir=new File(scDir);
			sDir.delete();
			sDir.mkdirs();
			
			if(scenery.getNumberOfPackets()==0)
				JOptionPane.showMessageDialog(this,"There is no Traffic Scenery selected/created to be generated.","Warning Message",JOptionPane.WARNING_MESSAGE);
			else{
				String text = scenery.getSelectedInfo(project.isMapCores());
				JTextArea ta = new JTextArea(text,10,40);
				ta.setEditable(false);
				JScrollPane jsp = new JScrollPane(ta);
				int confirm = JOptionPane.showConfirmDialog(this,jsp, "Information", JOptionPane.OK_CANCEL_OPTION);
				if(confirm==JOptionPane.OK_OPTION)
				{
					scenery.delete(project.getPath() + File.separator + "Traffic");
					project.setSceneryName(scenery.getName());
					project.setTrafficGenerate(true);
					saveScenery();
					
					new Generate(project, scenery);
					project.write();
					dispose();
				}
			}
		}
	}
	
	public void actionPerformed(ActionEvent e){
		if(e.getActionCommand().equalsIgnoreCase("Generate"))
			generate();
		else if(e.getActionCommand().equalsIgnoreCase("New Scenery")){
			showJFrameNewScenery();
		}
		else if(e.getSource()==btn_newSceneryOk){
			if(tname.getText().equals("")) JOptionPane.showMessageDialog(null,"You must inform your project's name!\n","Error!",JOptionPane.ERROR_MESSAGE);
			else{
				scenery.setStandardConfigToRouters(project.isMapCores());
				scenery.setName(tname.getText());

				// change the packet size standard value if NoC type is Mercury
				if( noc.getType().equalsIgnoreCase(NoC.MERCURY))
					scenery.getStandardTraffic().setPacketSize(15);

				updateTitle();
				menuConfig.setEnabled(true);
				newSceneryFrame.dispose();
			}
		}
		else if(e.getSource()==btn_newSceneryCancel){
			newSceneryFrame.dispose();
		}
		else if(e.getSource()==menuOpen){
			trafficCDCM=0;
			selectScenery();
		}
		else if(e.getSource()==menuOpenCDCG){
			trafficCDCM=1;
			selectScenery();
		}
		else if(e.getSource() == menuExit){
			int intOption = JOptionPane.showConfirmDialog(null,"Are you sure you want to exit?", "Exit",  JOptionPane.YES_NO_OPTION);
			if(intOption == JOptionPane.YES_OPTION)	System.exit(1);
		}
		else if(e.getSource()==menuStandard){
			if(scenery.getName().equals("")) JOptionPane.showMessageDialog(null,"You must inform your project's name!\n","Error!",JOptionPane.ERROR_MESSAGE);
			else{
				if(noc.isSR4())
				  new InterfaceConfigSRCV(project);
				else
					new InterfaceConfig(project,scenery);
			}
		}
		else if(e.getSource()==menuDoc)
		    Help.show("https://corfu.pucrs.br/redmine/projects/atlas/wiki/Traffic_Mbps");
		else if(e.getSource()==menuAbout)
			JOptionPane.showMessageDialog(null,"Traffic Generation          27.02.2008\nDeveloped by:\n       GAPH - Hardware Design Support Group","VERSION 1.5",JOptionPane.INFORMATION_MESSAGE);
	}

	public void mouseClicked(MouseEvent e){
		if(e.getSource()==jpanel_noc){
			if(scenery.getName().equals("")) JOptionPane.showMessageDialog(null,"No scenery defined. A Traffic Scenery have to be created/selected.\n","Error!",JOptionPane.ERROR_MESSAGE);
			else{
				int x = e.getX();
				int y = e.getY();
				if(e.getSource()==jpanel_noc){
					Router router = scenery.getClickedRouter(x,y);
					if(router!=null){
						if(noc.isSR4())
						  new InterfaceConfigSRCV(project,router);
						else{
							if(!project.isMapCores() || (project.isMapCores() && !router.getCore().equalsIgnoreCase("none") && !router.getCore().equalsIgnoreCase("Serial")))
								new InterfaceConfig(project, router);
						}
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
	 * Allow test the class methods 
	 * @param s
	 */
	public static void main(String s[]){
		JFrame.setDefaultLookAndFeelDecorated(true);
		JDialog.setDefaultLookAndFeelDecorated(true);
		if(s!=null){
			if(s.length==1){
				//s[0] = absolute path of the project including project filename. For example, c:\HardNoCs\NoC3x3\tes.noc
				Project project = new Project(new File(s[0]));
				new TrafficMbps(project);
			}
			else if(s.length==2){
				//s[0] = absolute path of the project including project filename. For example, c:\HardNoCs\NoC3x3\tes.noc
				Project project = new Project(new File(s[0]));
				File fileScenery = new File(project.getPath()+File.separator + "Traffic" + File.separator+s[1]+File.separator+s[1]+".traffic");
				if(fileScenery.exists())
					new TrafficMbps(project,fileScenery);
				else
					JOptionPane.showMessageDialog(null,"Inform the scenery's name does not exist.","Error", JOptionPane.ERROR_MESSAGE);
			}
			else
				JOptionPane.showMessageDialog(null,"Inform the project directory and optionally the scenery's name .","Error", JOptionPane.ERROR_MESSAGE);
		}
		else
			JOptionPane.showMessageDialog(null,"Inform the project directory.","Error", JOptionPane.ERROR_MESSAGE);
	}
}
