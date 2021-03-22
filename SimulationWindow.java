import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.lang.Math;
import Hefestus.Hefestus;

import AtlasPackage.Help;
import AtlasPackage.Default;
import AtlasPackage.Scenery;
import AtlasPackage.NoC;
import AtlasPackage.Project;
import AtlasPackage.SR4Traffic;
import AtlasPackage.ExampleFileFilter;
import AtlasPackage.JPanelImage;

/**
 * This class implements a GUI allowing choice the simulation parameters and launch ModelSim simulation.
 * @author Aline Vieira de Mello
 *
 */
public class SimulationWindow extends JFrame implements ActionListener {

	private Project project;
	private NoC noc;
	private boolean isHermesSR;
	private int nCV;
	private Scenery scenery;
	private JTextField tName,tRouting,tCTRLRouting,tGSRouting,tBERouting,tTime,tTimeW;
	private JCheckBox cbInternal;
	private JComboBox cbUnity;
	private JButton	bName,bRouting,bCTRLRouting,bGSRouting,bBERouting,bOk,bCancel;
	private JMenu menuHelp;
	private String rotFile, CTRLRotFile, GSRotFile, BERotFile;	
	
	private final int SRVC_CTRL=SR4Traffic.CTRL;
	private final int SRVC_GS=SR4Traffic.GS;
	private final int SRVC_BE=SR4Traffic.BE;
	
	/**
	 * Creates a GUI allowing configure and launch a ModelSim simulation.
	 * @param project The NoC project that will be simulated.
	 */
	public SimulationWindow(Project project){
		super();
		this.project = project;
		noc = project.getNoC();
		setTitle(project.getPath()+File.separator + "Traffic");
		int x=(this.noc.getType().equalsIgnoreCase("HermesSR"))?40:0;
		isHermesSR=(x==40);
		nCV=this.noc.getVirtualChannel();
		if((isHermesSR)&&(nCV==4)) x*=3;

		int y=(this.project.isPowerEstimated())?40:0;

		Dimension resolucao=Toolkit.getDefaultToolkit().getScreenSize();

		setSize(505,180+x+y);
		setLocation((resolucao.width-505)/2,(resolucao.height-(180+x+y))/2);

		rotFile=CTRLRotFile=GSRotFile=BERotFile="";

		setResizable(false);
		addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){end();}});
		getContentPane().setLayout(null);
		addComponents();
		
		String sceneryFile = project.getSceneryPath() + File.separator + project.getSceneryName() + ".traffic";
		scenery = project.getScenery();
		if(scenery == null)
			openScenery(sceneryFile);
		else
			showSceneryParameters(sceneryFile);
		setVisible(true);
	}

	/**
	 * Add component to GUI.
	 */
	private void addComponents(){
		int x=10;
		int y=10;
		int dimy=25;
		addMenu();
		addName(x,y,dimy);
		if(isHermesSR){
			if(nCV==4){
				y+=40;
				addCTRLRouting(x,y,dimy);
				y+=40;
				addGSRouting(x,y,dimy);
				y+=40;
				addBERouting(x,y,dimy);
			}
			else{
				y+=40;
				addRouting(x,y,dimy);
			}
		}
		y+=40;
		addTime(x,y,dimy);

		addEvaluation(x+280,y,dimy);
		
		addAutomatic(x+280,y,dimy);
		
		if(this.project.isPowerEstimated()){
			y+=40;
			addTimeWindow(x,y,dimy);		
		}
		
		y+=40;
		
		addButtons(x+150,y,dimy);
		addLogoGaph(400,y,90,27);
	}

 	/**
 	 * Add the Menu bar
 	 */
 	private void addMenu(){
		JMenuItem helpTopics=new JMenuItem("Documentation");
		helpTopics.addActionListener(this);
		
		menuHelp = new JMenu("Help");
		menuHelp.add(helpTopics);

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(menuHelp);
		setJMenuBar(menuBar);
	}

	/**
	 * Add the components to determine the scenery"s name
	 */
 	private void addName(int x,int y,int dimy){
		JLabel lName=new JLabel("Scenery's name:");
		lName.setBounds(x,y,100,dimy);
		getContentPane().add(lName);

		x+=105;
		tName=new JTextField(project.getSceneryName() + ".traffic", 10);
		tName.setToolTipText("Choose a folder where your scenery will be opened.");
		tName.setBounds(x,y,275,dimy);
		getContentPane().add(tName);

		x+=290;
		bName=new JButton("Browse");
		bName.setToolTipText("Choose a folder where your scenery will be opened.");
		bName.setBounds(x,y,80,dimy);
		bName.addActionListener(this);
		getContentPane().add(bName);
	}

	/**
	 * Add the components to determine the routing file.
	 */
 	private void addRouting(int x,int y,int dimy){
		JLabel lRouting=new JLabel("Routing file:");
		lRouting.setBounds(x,y,100,dimy);
		getContentPane().add(lRouting);

		x+=105;
		tRouting=new JTextField("",10);
		tRouting.setToolTipText("Choose a folder containing routing files for this scenario.");
		tRouting.setBounds(x,y,275,dimy);
		tRouting.setEnabled(false);
		getContentPane().add(tRouting);

		x+=290;
		bRouting=new JButton("Browse");
		bRouting.setToolTipText("Choose a folder containing routing files for this scenario.");
		bRouting.setBounds(x,y,80,dimy);
		bRouting.addActionListener(this);
		getContentPane().add(bRouting);
	}

	/**
	 * Add the components to determine the CTRL routing file.
	 */
	private void addCTRLRouting(int x,int y,int dimy){
		JLabel lCTRLRouting=new JLabel("CTRL Routing file:");
		lCTRLRouting.setBounds(x,y,100,dimy);
		getContentPane().add(lCTRLRouting);

		x+=105;
		tCTRLRouting=new JTextField("",10);
		tCTRLRouting.setToolTipText("Choose a folder containing routing files for CONTROL messages transmission.");
		tCTRLRouting.setBounds(x,y,275,dimy);
		tCTRLRouting.setEnabled(false);
		getContentPane().add(tCTRLRouting);

		x+=290;
		bCTRLRouting=new JButton("Browse");
		bCTRLRouting.setToolTipText("Choose a folder containing CONTROL routing files for this scenario.");
		bCTRLRouting.setBounds(x,y,80,dimy);
		bCTRLRouting.addActionListener(this);		
		getContentPane().add(bCTRLRouting);
	}

	/**
	 * Add the components to determine the GS routing file.
	 */
	private void addGSRouting(int x,int y,int dimy){
		JLabel lGSRouting=new JLabel("GS Routing file:");
		lGSRouting.setBounds(x,y,100,dimy);
		getContentPane().add(lGSRouting);

		x+=105;
		tGSRouting=new JTextField("",10);
		tGSRouting.setToolTipText("Choose a folder containing routing files for GS messages transmission.");
		tGSRouting.setBounds(x,y,275,dimy);
		tGSRouting.setEnabled(false);
		getContentPane().add(tGSRouting);

		x+=290;
		bGSRouting=new JButton("Browse");
		bGSRouting.setToolTipText("Choose a folder containing GS routing files for this scenario.");
		bGSRouting.setBounds(x,y,80,dimy);
		bGSRouting.addActionListener(this);
		getContentPane().add(bGSRouting);
	}

	/**
	 * Add the components to determine the BE routing file.
	 */
	private void addBERouting(int x,int y,int dimy){
		JLabel lBERouting=new JLabel("BE Routing file:");
		lBERouting.setBounds(x,y,100,dimy);
		getContentPane().add(lBERouting);

		x+=105;
		tBERouting=new JTextField("",10);
		tBERouting.setToolTipText("Choose a folder containing routing files for BE messages transmission.");
		tBERouting.setBounds(x,y,275,dimy);
		tBERouting.setEnabled(false);
		getContentPane().add(tBERouting);

		x+=290;
		bBERouting=new JButton("Browse");
		bBERouting.setToolTipText("Choose a folder containing BE routing files for this scenario.");
		bBERouting.setBounds(x,y,80,dimy);
		bBERouting.addActionListener(this);
		getContentPane().add(bBERouting);
	}

	/**
	 * Add the components to determine the simulation time.
	 */
	private void addTime(int x,int y,int dimy){
		JLabel lTime = new JLabel("Simulation Time:");
		lTime.setBounds(x,y,120,dimy);
		getContentPane().add(lTime);

		x+=105;
		tTime = new JTextField("1",5);
		
		tTime.setBounds(x,y,80,dimy);
		if(project.getNoC().getType().equals("HermesG"))
		{
			tTime.setEnabled(false);
			tTime.setVisible(false);
			lTime.setEnabled(false);
			lTime.setVisible(false);
		}
		else
		{
			tTime.setToolTipText("Choose the simulation time.");
			tTime.setEnabled(true);
		}
		getContentPane().add(tTime);

		String[] unity = { "fs", "ps", "ns", "us", "ms", "sec"};
		x+=85;
		cbUnity = new JComboBox(unity);
		cbUnity.setBounds(x,y,60,dimy);
		cbUnity.addActionListener(this);
		
		
		if(project.getNoC().getType().equals("HermesG"))
		{
			cbUnity.setEnabled(false);
			cbUnity.setVisible(false);
		}
        else
        {
			cbUnity.setToolTipText("Select the simulation resolution.");
			cbUnity.setEnabled(true);
		}
		getContentPane().add(cbUnity);
	}

	/**
	 * Add the components to determine if the internal evaluation should be considered.
	 */
	private void addEvaluation(int x,int y,int dimy){
		cbInternal = new JCheckBox("Internal Evaluation");
		cbInternal.setToolTipText("Assign the internal evaluation.");
	    cbInternal.setBounds(x,y,160,dimy);
        // Disabled this option when using Mercury or SR NoC
	    if (!(noc.getType().equalsIgnoreCase(NoC.MERCURY) || noc.getType().equalsIgnoreCase(NoC.HERMESSR)))
	    	getContentPane().add(cbInternal);
	    	
	   	// Oculta opção de avaliação interna em redes HERMES
	   	if(project.getNoC().getType().equals("HermesG"))
	   	{
			cbInternal.setEnabled(false);
			cbInternal.setVisible(false);
        }
	}

	/**
	 * Add the components to determine the interval between two samples.
	 */
	private void addTimeWindow(int x,int y,int dimy){
		JLabel lTimeW = new JLabel("Power Sampling Period:");
		lTimeW.setBounds(x,y,150,dimy);
		getContentPane().add(lTimeW);

		x+=155;
		tTimeW = new JTextField("1000",5);
		tTimeW.setToolTipText("Set the time window. Used by traffic monitors evaluation.");
		tTimeW.setBounds(x,y,80,dimy);
		getContentPane().add(tTimeW);
		
		x+=85;
		JLabel lCC = new JLabel("clock cycles");
		lCC.setBounds(x,y,120,dimy);
		getContentPane().add(lCC);
	}
	/**
	 * Add the components to determine if the Automatic Simulation should be considered.
	 */
	private void addAutomatic(int x,int y,int dimy)
	{
		cbInternal = new JCheckBox("Manual Simulation",false);
		cbInternal.setToolTipText("Assign the simulation.");
	    cbInternal.setBounds(x,y,160,dimy);
        getContentPane().add(cbInternal);	
	   	if(project.getNoC().getType().equals("HermesG"))
	   	{
			cbInternal.setEnabled(false);
			cbInternal.setVisible(false);
        }
        else
        {
			cbInternal.setEnabled(false);
			cbInternal.setVisible(false);
		}
	}
	/**
	 * Add the buttons.
	 */
	private void addButtons(int x,int y,int dimy){
		bOk=new JButton("Ok");
		//bOk.setToolTipText("Choose a folder where your project will be created.");
		bOk.setBounds(x,y,80,dimy);
		bOk.addActionListener(this);
		getContentPane().add(bOk);

		x+=100;
		bCancel=new JButton("Cancel");
		//bCancel.setToolTipText("Choose a folder where your project will be created.");
		bCancel.setBounds(x,y,80,dimy);
		bCancel.addActionListener(this);
		getContentPane().add(bCancel);
	}

	/**
	 * Add a JPanelImage with the GAPH image. 
	 */
	private void addLogoGaph(int x,int y,int dimx,int dimy){
		String gaphImage = System.getenv("ATLAS_HOME")+File.separator+"Images"+File.separator+"logo-gaph.gif";
		String gaphWeb = "http://www.inf.pucrs.br/~gaph";
		JPanelImage jpanel=new JPanelImage(gaphImage,gaphWeb);
		jpanel.setToolTipText("Double Click to Visit the Developer homepage.");
		jpanel.setBounds(x,y,dimx,dimy);
		jpanel.repaint();
		getContentPane().add(jpanel);
	}

	/**
	 * Show the scenery parameters in GUI. 
	 */
	private void showSceneryParameters(String name){
		setTitle(name.substring(0,name.lastIndexOf(File.separator)));
		tName.setText(name.substring(name.lastIndexOf(File.separator)+1));
		int time = scenery.getSimulationTime();
		if(time == 0)
			time = calcSuggestTime();
		tTime.setText(""+time);
		cbUnity.setSelectedItem(scenery.getTimeResolution());
	    if (!(noc.getType().equalsIgnoreCase(NoC.MERCURY) || noc.getType().equalsIgnoreCase(NoC.HERMESSR)))
	    	cbInternal.setSelected(scenery.isInternalSimulation());
	    else
	    {
			cbInternal.setSelected(false);
		}
	    defaultRoutingFiles();	
	}
	
	/**
	 * Select the scenery that will be simulated.
	 */
	private String selectScenario(String name){
		JFileChooser filechooser = new JFileChooser();
		filechooser.setFileFilter(new ExampleFileFilter("traffic",".Traffic Files"));
		filechooser.setCurrentDirectory(new File(name));
		int intOption = filechooser.showOpenDialog(null);
		if(intOption == JFileChooser.APPROVE_OPTION){
			File file = filechooser.getSelectedFile();
			return file.getAbsolutePath();
		}
		return "";
	}

	/**
	 * Select the routind file.
	 */
	private String selectRoutingFile(String name){
		JFileChooser filechooser = new JFileChooser();
		filechooser.setFileFilter(new ExampleFileFilter("rot",".Routing Files"));
		filechooser.setCurrentDirectory(new File(name));
		int intOption = filechooser.showOpenDialog(null);
		if(intOption == JFileChooser.APPROVE_OPTION){
			File file = filechooser.getSelectedFile();
			return file.getAbsolutePath();
		}
		return "";
	}

	/**
	 * Calculate the suggested simulation time.
	 */
	private int calcSuggestTime(){
		int nP = scenery.getNumberOfPackets();
		int pS = (int)scenery.getAveragePacketSize();
		int fS = noc.getFlitSize();
		double aR = scenery.getAverageRate();
		int time = (int)((((nP*pS*fS)/aR)/100000)*2)+1;
		return time;
	}

	private void copyFile(File fileIn,File fileOut){
		try{
			FileReader in = new FileReader(fileIn);
			FileWriter out = new FileWriter(fileOut);
			int c;
			while ((c = in.read()) != -1){
				out.write(c);
			}
			in.close();
			out.close();
		}
		catch(Exception e)
		{
			JOptionPane.showMessageDialog(null,"Can't copy the file "+fileIn.getName(),"IO error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}

	/**
	 * Update the simulation time in simulate.do file.
	 */
	private void updateSimulationTime(){
		DataOutputStream dos;

		StringTokenizer st;
		String line, word;

		String nameTemp = project.getPath()+File.separator + "temp.txt";
		String nameFile = project.getPath()+File.separator + "simulate.do";
		//create a temporary file, after update data
		copyFile(new File(nameFile),new File(nameTemp));

		try{
			FileInputStream fis=new FileInputStream(nameTemp);
			BufferedReader br=new BufferedReader(new InputStreamReader(fis));

			dos=new DataOutputStream(new FileOutputStream(nameFile));

			line=br.readLine();
			while(line!=null){
				st = new StringTokenizer(line, " ");
				int nTokens = st.countTokens();
				if(nTokens!=0){
					word = st.nextToken();
					
					if(project.getNoC().getType().equals("HermesG"))
					{
						
					}
					
					else
					{
						if(word.equalsIgnoreCase("run"))
						{
						   line = word + " "+scenery.getSimulationTime()+" "+scenery.getTimeResolution();
						}
					}
				}
				dos.writeBytes(line+"\n");
				line=br.readLine();
			}
			br.close();
			fis.close();
			dos.close();
			//delete the temporary file
			File file = new File(nameTemp);
			file.delete();
		}
		catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't open "+project.getPath()+File.separator + "simulate.do","Input Error", JOptionPane.ERROR_MESSAGE);
			//delete the temporary file
			File file = new File(nameTemp);
			file.delete();
			System.exit(0);
		}
		catch(Exception e){
			System.out.println("Error");
			//delete the temporary file
			File file = new File(nameTemp);
			file.delete();
			System.exit(0);
		}			
		
	}

	/**
	 * Update time window in project.
	 */
	private void updateTimeWindow(){
		int time = Integer.parseInt(tTimeW.getText());
		String TW;		
		TW = Integer.toHexString(time);
		
		project.setTimeWindow(TW);	
		project.write();		
	}
	
	/**
	 * Update the NoC.vhd and simulate.do according to Internal Simulation option.
	 */
	private void updateInternalSimulation(boolean isInternalSimulation){
		updateNoC(isInternalSimulation);
		updateSimulate(isInternalSimulation);
	}

	/**
	 * Update the NoC.vhd according to Internal Simulation option.
	 */
	private void updateNoC(boolean isInternalSimulation){
		String nameFile = project.getPath()+File.separator + "NOC" + File.separator + "NOC.vhd";
		updateNoC(isInternalSimulation, nameFile);
		if (project.isPowerEstimated()){
			nameFile = project.getPath()+File.separator + "NOC_monitores" + File.separator + "NOC_monitores.vhd";
			updateNoC(isInternalSimulation, nameFile);
		}
	}	
	
	/**
	 * Update the NoC.vhd according to Internal Simulation option.
	 */
	private void updateNoC(boolean isInternalSimulation, String nameFile){
		DataOutputStream dos;
		String line,comment;
		String nameTemp = project.getPath()+File.separator + "temp.txt";
		boolean replace=false;

		//create a temporary file
		copyFile(new File(nameFile),new File(nameTemp));

		try{
			FileInputStream fis=new FileInputStream(nameTemp);
			BufferedReader br=new BufferedReader(new InputStreamReader(fis));

			dos=new DataOutputStream(new FileOutputStream(nameFile));

			comment="";
			line=br.readLine();
			while(line!=null){
				if(!isInternalSimulation){
					//find the first line of the module
					if(line.lastIndexOf("outmodulerouter")!=-1){
						//test if it is not commented
						if(line.lastIndexOf("--")==-1)
							comment="--";
					}

					line=comment + line;

					//find the last line of the module
					if(comment.equals("--") && line.lastIndexOf(");")!=-1)
						comment="";
				}
				else{
					//find the first line of the module
					if(line.lastIndexOf("outmodulerouter")!=-1)
						replace=true;

					if(replace)
						line=line.replaceFirst("--","");

					//find the last line of the module
					if(replace && line.lastIndexOf(");")!=-1)
						replace = false;
				}

				dos.writeBytes(line+"\n");
				line=br.readLine();
			}
			br.close();
			fis.close();
			dos.close();
			//delete the temporary file
			File file = new File(nameTemp);
			file.delete();
		}
		catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't open "+project.getPath()+File.separator + "simulate.do","Input Error", JOptionPane.ERROR_MESSAGE);
			//delete the temporary file
			File file = new File(nameTemp);
			file.delete();
			System.exit(0);
		}
		catch(Exception e){
			JOptionPane.showMessageDialog(null,"Error in "+project.getPath()+File.separator + "simulate.do","Input Error", JOptionPane.ERROR_MESSAGE);
			//delete the temporary file
			File file = new File(nameTemp);
			file.delete();
			System.exit(0);
		}
	}
	/**
	 * Update the simulate.do according to Internal Simulation option.
	 */
	private void updateSimulate(boolean isInternalSimulation){
		String nameFile = project.getPath()+File.separator + "simulate.do";
		updateSimulate(isInternalSimulation, nameFile);
		if (project.isPowerEstimated()){
			nameFile = project.getPath()+File.separator + "simulate_monitores.do";
			updateSimulate(isInternalSimulation, nameFile);
		}
	}
	
	/**
	 * Update the simulate.do according to Internal Simulation option.
	 */
	private void updateSimulate(boolean isInternalSimulation, String nameFile){
		DataOutputStream dos;
		String line;

		String nameTemp = project.getPath()+File.separator + "temp.txt";
		//create a temporary file
		copyFile(new File(nameFile),new File(nameTemp));

		try{
			FileInputStream fis=new FileInputStream(nameTemp);
			BufferedReader br=new BufferedReader(new InputStreamReader(fis));

			dos=new DataOutputStream(new FileOutputStream(nameFile));

			line=br.readLine();
			while(line!=null){
				if(line.lastIndexOf("SC_OutputModuleRouter.cpp")!=-1){
					if(isInternalSimulation)
						line=line.replaceFirst("#","");
					else if(line.lastIndexOf("#")==-1) //Test if it is not commented
						line="#"+line;
				}
				dos.writeBytes(line+"\n");
				line=br.readLine();
			}
			br.close();
			fis.close();
			dos.close();
			//delete the temporary file
			File file = new File(nameTemp);
			file.delete();
		}
		catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't open "+project.getPath()+File.separator + "simulate.do","Input Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		catch(Exception e){
			System.out.println("Erro");
			System.exit(0);
		}
	}

	/**
	* Create the simulation scripts.
	*/
	private void createScriptsSC(){
		DataOutputStream dos;
		String projectPath = project.getPath() + File.separator;
		String sceneryPath = "Traffic" + File.separator + scenery.getName() + File.separator;
		String sceneryInPath =  sceneryPath + "In" + File.separator;
		String sceneryOutPath =  sceneryPath + "Out" + File.separator;
		String evaluationPath = "Power" + File.separator + "Evaluation" + File.separator;
		
		try{
		    if(Default.isWindows){
				dos=new DataOutputStream(new FileOutputStream(projectPath + "simulate_Win.bat"));
				dos.writeBytes("del /q in*.txt out*.txt r*.txt\n");
				dos.writeBytes("del /q " + sceneryOutPath + "* \n");
				dos.writeBytes("rmdir /s /q work\n");
				dos.writeBytes("copy " + sceneryInPath + "in* .\n");
				if(project.isPowerEstimated()){
					dos.writeBytes("vsim.exe -c -do simulate_monitores.do\n");
					dos.writeBytes("move list.txt " + evaluationPath + "\n");		
				}
				else{
				    dos.writeBytes("vsim.exe -c -do simulate.do\n");
				}
				dos.writeBytes("del in*\n");
				dos.writeBytes("move out* " + sceneryOutPath + "\n");
				if(cbInternal.isSelected() || project.isPowerEstimated())
					dos.writeBytes("move r*.txt " + sceneryOutPath + "\n");
				dos.writeBytes("exit");
				dos.close();
		    }
		    else{
				dos=new DataOutputStream(new FileOutputStream(projectPath + "simulate_linux"));
				dos.writeBytes("rm -rf work in*.txt out*.txt r*.txt \n");
				dos.writeBytes("rm -rf " + sceneryOutPath  + "* \n");
				dos.writeBytes("cp " + sceneryInPath + "* .\n");
				
				if(project.isPowerEstimated()){
				    dos.writeBytes("vsim -c -do simulate_monitores.do\n");			
				    dos.writeBytes("mv list.txt " + evaluationPath + "\n");
				}
				else{
				    dos.writeBytes("vsim -c -do simulate.do\n");			
				}
				
				dos.writeBytes("rm -rf in*\n");
				dos.writeBytes("mv out* " + sceneryOutPath  + "\n");
				if(cbInternal.isSelected())
				    dos.writeBytes("mv r*.txt " + sceneryOutPath + "\n");
	
				dos.close();
				
				//change file permission
				Process p=Runtime.getRuntime().exec("chmod 777 "+project.getPath()+File.separator+"simulate_linux");
				p.waitFor();
		    }
		}catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't write the simulation script","Output error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}catch(Exception e){
		    JOptionPane.showMessageDialog(null,e.getMessage(),"Error Message", JOptionPane.ERROR_MESSAGE);
		    System.exit(0);
		}
	}

	/**
	* Create the defs.h file.
	*/
	private void createSCNI(){
		try{
			String d_out = new String(project.getPath()+File.separator + "SC_NoC" + File.separator + "defs.h");
			FileOutputStream def_h_file=new FileOutputStream(d_out);
			DataOutputStream do_file=new DataOutputStream(def_h_file);
			do_file.writeBytes("//========== DEFINE ROUTING FILES TO ME USED ==========\n");
			do_file.writeBytes("#ifndef _ROTFILE_DEF\n");
			if(noc.getVirtualChannel()==4){
				do_file.writeBytes("\t#define _CTRL_ROTFILE_DEF \"Traffic" + File.separator + File.separator+scenery.getName()+File.separator + File.separator + "Routing" + File.separator + File.separator+CTRLRotFile+"\"\n");
				do_file.writeBytes("\t#define _GS_ROTFILE_DEF   \"Traffic" + File.separator + File.separator+scenery.getName()+File.separator + File.separator + "Routing" + File.separator + File.separator+GSRotFile+"\"\n");
				do_file.writeBytes("\t#define _BE_ROTFILE_DEF   \"Traffic" + File.separator + File.separator+scenery.getName()+File.separator + File.separator + "Routing" + File.separator + File.separator+BERotFile+"\"\n");

				scenery.setCTRLRouting(CTRLRotFile);
				noc.setCTRLRouting(project.getPath()+File.separator + "Traffic" + File.separator+scenery.getName()+File.separator + "Routing" + File.separator+CTRLRotFile);
				scenery.setGSRouting(GSRotFile);
				noc.setGSRouting(project.getPath()+File.separator + "Traffic" + File.separator+scenery.getName()+File.separator + "Routing" + File.separator+GSRotFile);
				scenery.setBERouting(BERotFile);
				noc.setBERouting(project.getPath()+File.separator + "Traffic" + File.separator+scenery.getName()+File.separator + "Routing" + File.separator+BERotFile);

			}
			else{			
				do_file.writeBytes("\t#define _ROTFILE_DEF \"Traffic" + File.separator + File.separator+scenery.getName()+File.separator + File.separator + "Routing" + File.separator + File.separator+rotFile+"\"\n");
				scenery.setRouting(rotFile);
				noc.setRouting(project.getPath()+File.separator + "Traffic" + File.separator+scenery.getName()+File.separator + "Routing" + File.separator+rotFile);
			}
			do_file.writeBytes("#endif\n");
			do_file.writeBytes("//========== ========== ========== ========== ==========\n");
			do_file.close();
			project.write();
		}
		catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't write defs.h","Input error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		catch(Exception e){
			JOptionPane.showMessageDialog(null,"Error while creating defs.h file.\n"+e.getMessage(),"Input error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}

	/**
	 * Open an existent scenery file.
	 * @param name The scenery name.
	 */
	private void openScenery(String name){
		if(name.lastIndexOf(project.getPath()+File.separator + "Traffic")==-1)
			JOptionPane.showMessageDialog(null,"The selected scenery isn't in the project's path.","Error Message",JOptionPane.ERROR_MESSAGE);
		else if(!new File(name).exists())
			JOptionPane.showMessageDialog(null,"The selected scenery doesn't exist.","Error Message",JOptionPane.ERROR_MESSAGE);
		else{
			scenery = new Scenery(noc.getNumRotX(),noc.getNumRotY());
			scenery.open(new File(name));
			project.setScenery(scenery);
			showSceneryParameters(name);
		}
	}
	
	/**
	 * Determine the default routing filenames. 
	 */
	private void defaultRoutingFiles(){
		if(isHermesSR){
			if(nCV==4){
				CTRLRotFile="pure_xy_CTRL.rot"; tCTRLRouting.setText(CTRLRotFile); 
				GSRotFile="pure_xy_GS.rot"; tGSRouting.setText(GSRotFile); 
				BERotFile="pure_xy_BE.rot"; tBERouting.setText(BERotFile); 
			}
			else{	
				rotFile="pure_xy.rot"; tRouting.setText(rotFile); 
			}			
		}
	}
	
	/**
	 * Open a routing file.
	 */
	private void openRoutingFile(String name, int _srvc){
		if(name.lastIndexOf(project.getPath()+File.separator + "Traffic" + File.separator+scenery.getName()+File.separator + "Routing")==-1) 
			JOptionPane.showMessageDialog(null,"The selected routing file isn't in the scenery's path.","Error Message",JOptionPane.ERROR_MESSAGE);
		else if(!new File(name).exists())
			JOptionPane.showMessageDialog(null,"The selected routing file doesn't exist.","Error Message",JOptionPane.ERROR_MESSAGE);
		else{

			switch(_srvc){
				case SRVC_CTRL: CTRLRotFile=name.substring(name.lastIndexOf(File.separator)+1); tCTRLRouting.setText(CTRLRotFile); break;
				case SRVC_GS: GSRotFile=name.substring(name.lastIndexOf(File.separator)+1); tGSRouting.setText(GSRotFile); break;
				case SRVC_BE: BERotFile=name.substring(name.lastIndexOf(File.separator)+1); tBERouting.setText(BERotFile); break;
				default: rotFile=name.substring(name.lastIndexOf(File.separator)+1); tRouting.setText(rotFile);
			}
			
		}
	}

	/**
	* Close the GUI.
	*/
	private void end(){
		dispose();
	}

	/**
	* Generate NoC with monitors.
	*/
	private void generateNoCMonitores(){	
		if(noc.getType().equalsIgnoreCase("Hermes")){
			String[] args = {project.getStringProjFile(), ""+scenery.getSimulationTime(), scenery.getTimeResolution()};
			new  Hefestus(args);
		}
	}
	
	/**
	 * Test the GUI parameters and launch the ModelSim simulation.
	 */
	private void simulate(){
		int time=0;
		boolean error = false;
		if(tName.getText().equals("")){
			error = true;
			JOptionPane.showMessageDialog(null,"The scenery's name must be informed.","Error Message",JOptionPane.ERROR_MESSAGE);
		}
		else if((isHermesSR)&&(nCV!=4)&&(tRouting.getText().equals(""))){
			error = true;
			JOptionPane.showMessageDialog(null,"Routing file not selected for this simulation.","Error Message",JOptionPane.ERROR_MESSAGE);
		}
		else if((isHermesSR)&&(nCV==4)&&(tCTRLRouting.getText().equals(""))&&(tGSRouting.getText().equals(""))&&(tBERouting.getText().equals(""))){
			error = true;
			JOptionPane.showMessageDialog(null,"Routing files not selected for this simulation.","Error Message",JOptionPane.ERROR_MESSAGE);
		}
		else if(tTime.getText().equals("0")){
			error = true;
			time = calcSuggestTime();
			tTime.setText(""+time);
			JOptionPane.showMessageDialog(null,"Suggest simulation time is "+time+".","Information Message",JOptionPane.INFORMATION_MESSAGE);
		}

		if (!error){
		    try{
		    	time = Integer.valueOf(tTime.getText()).intValue();
		    }catch(Exception exc){
				error = true;
				JOptionPane.showMessageDialog(null,"A integer number must be informed in the simulation time.","Error Message",JOptionPane.ERROR_MESSAGE);
				tTime.setText(""+calcSuggestTime());
		    }
		}

		// verify the number of files will be open during simulation
		if (!error){
			int nRots = noc.getNumRotX() * noc.getNumRotY();
			int internalFiles = 0;
			if(cbInternal.isSelected())
				internalFiles = nRots * noc.getVirtualChannel() * 4; //4 is the number of internal ports
			int externalFiles = 2 * nRots; //one input and one output files per router
			if(noc.isSR4())
				externalFiles = 7 * nRots; //three input and four output files per router
			int nFiles = externalFiles + internalFiles;
			if(nFiles > 999){ // too many files
				int option = JOptionPane.showConfirmDialog(null, "This simulation will open more than 1000 files.\nVerify if your operation system support it.\nDo you want continue?", "Too many files", JOptionPane.WARNING_MESSAGE);
				if(option==JOptionPane.CANCEL_OPTION)
					error = true;
			}			
		}
		
		if(!error){
			project.setSceneryName(tName.getText().substring(0,tName.getText().lastIndexOf('.')));
			project.write();
			scenery.setSimulated(true);
			scenery.setSimulationTime(time);
			scenery.setTimeResolution((String)cbUnity.getSelectedItem());
			if(project.getNoC().getType().equals("HermesG"))
				scenery.setInternalSimulation(false);
			else
				cbInternal.isSelected();
			
			scenery.save(project.getSceneryFile());

			if (project.isPowerEstimated()){
				updateTimeWindow();						
				generateNoCMonitores();
			}
			
			updateSimulationTime();
			
			dispose();
			repaint();
			if(noc.isSCTB())
            {
				updateInternalSimulation(cbInternal.isSelected());
				if(isHermesSR) 
					createSCNI();
				createScriptsSC();
                if(project.getNoC().getType().equals("HermesG"))
                {
					if(cbInternal.isSelected() == true)
					{
						scenery.setInternalSimulation(true); 
					}
					else
					{
						scenery.setInternalSimulation(false);
					}
					scenery.save(project.getSceneryFile());
                    Process pModelsim = null;
                    new ModelsimThread(project,pModelsim, getMax());
                }
                else
                {
                    try
                    {
					    Process p = Runtime.getRuntime().exec(Default.vsim);
					    p.destroy();
					
				        File dir=new File(project.getPath());
					    Process pModelsim;
					    if(Default.isWindows)
						    pModelsim = Runtime.getRuntime().exec("cmd.exe /k start Simulate_Win.bat", null, dir);
				        else{
				        	pModelsim=Runtime.getRuntime().exec(project.getPath()+ File.separator + "simulate_linux", null, dir);
				        	new ModelsimThread(project,pModelsim, getMax());
				        }
				    }catch(IOException f){
				        JOptionPane.showMessageDialog(null,"You did not add the Modelsim's path to PATH environment variable.\nWithout Modelsim's path, the simulation cannot be executed.","Error Message", JOptionPane.ERROR_MESSAGE);
				    }catch(Exception exc){
				        JOptionPane.showMessageDialog(this,exc.getMessage(),"Error Message",JOptionPane.ERROR_MESSAGE);
				    }
                }
			}			
		}
	}

    /**
     * Return the max of progress bar. The max is associate to the time simulation and resolution time simulation.
     */
    private int getMax(){
		double max = Integer.valueOf(tTime.getText()).intValue();
		String unity = (String)cbUnity.getSelectedItem();
		//convert time to nanoseconds
		if(unity.equalsIgnoreCase("sec")){
		    max = max * Math.pow(10,9);
		}
		else if(unity.equalsIgnoreCase("ms")){
		    max = max * Math.pow(10,6);
		}
		else if(unity.equalsIgnoreCase("us")){
		    max = max * Math.pow(10,3);
		}
		else if(unity.equalsIgnoreCase("ps")){
		    max = max * Math.pow(10,(-3));
		}
		else if(unity.equalsIgnoreCase("fs")){
		    max = max * Math.pow(10,(-6));
		}
		
		max = max / 200;
		if(max < 10) max = 10;
		return (int)max;
    }

	public void actionPerformed(ActionEvent e){
		if(e.getSource() == bName){
			openScenery(selectScenario(project.getSceneryPath()));
		}
		else if((isHermesSR)&&(nCV!=4)&&(e.getSource() == bRouting)){
			openRoutingFile(selectRoutingFile(project.getSceneryPath()+File.separator + "Routing"),-1);
		}
		else if((isHermesSR)&&(nCV==4)&&((e.getSource() == bCTRLRouting)||(e.getSource() == bGSRouting)||(e.getSource() == bBERouting))){
			int kind=-1;
			if(e.getSource() == bCTRLRouting) kind=SRVC_CTRL;
			else if(e.getSource() == bGSRouting) kind=SRVC_GS;
			else if(e.getSource() == bBERouting) kind=SRVC_BE;
			openRoutingFile(selectRoutingFile(project.getSceneryPath()+File.separator + "Routing"), kind);
		}
		else if(e.getSource() == bCancel)
			dispose();
		else if(e.getSource() == bOk)
			simulate();
		else if(e.getActionCommand().equalsIgnoreCase("Documentation")){
		    Help.show("https://corfu.pucrs.br/redmine/projects/atlas/wiki/ModelSim");
		}
	}
}
