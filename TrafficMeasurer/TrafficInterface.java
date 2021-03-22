package TrafficMeasurer;

import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import AtlasPackage.NoC;
import AtlasPackage.Project;
import AtlasPackage.Scenery;
import AtlasPackage.Convert;
import AtlasPackage.Help;
import AtlasPackage.JPanel_Noc;

/**
 * This class creates the Traffic Measurer GUI allowing select a traffic scenery and analyze it.
 * @author Aline Vieira de Mello
 * @version
 */
public class TrafficInterface extends JFrame implements ActionListener{

	private Project project;
	private NoC noc;
	private Scenery scenery;

	private JMenuBar menubar;
	private JMenu mReport, mHelp, mFile;
	private JMenuItem mLatency,mLink,mGlobal,documentation,about,mOpen;
	private JLayeredPane pbExternal,pbInternal,pbRateInternal;
	private JComboBox cbEvaluation,cbExternal,cbInternal,cbRateInternal;
	private JCheckBox cbTextGraph,cb3DGraph;
	private JTextField tsourceExternal,ttargetExternal,tpercentualExternal,tintervalExternal;
	private JTextField tsourceInternal,ttargetInternal,tpercentualInternal,tintervalInternal;
	private JButton bOk;
	private JPanel_Noc jpanel_noc;
	
	/** the scenery pathOut */
	private String pathOut;
	private int nRots;
	private double [][] linkUtilization;
	private int nCV;
	private boolean isHermesSR;

	/**
	 * Constructor class.
	 * @param project The selected project.
	 * @param scenery The traffic scenery that will be analyzed.
	 */
    public TrafficInterface(Project project,Scenery scenery) {
		super("Traffic Measurer");
		this.project=project;
		noc = project.getNoC();
		this.scenery=scenery;
		getContentPane().setLayout(null);
		setSize(800,456);
		Dimension resolucao=Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((resolucao.width-800)/2,(resolucao.height-456)/2);
		setResizable(false);
		addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){end();}});
		
		isHermesSR=noc.getType().equalsIgnoreCase("hermessr");		
		nCV=noc.getVirtualChannel();
			
		initialize();
		addComponents();
		computeLinkUtilization();
		setVisible(true);
	}

	private void computeLinkUtilization(){
		String [][] lPath;
		String line, lRunPath, filename;
		StringTokenizer st;
		int lSource, lTarget, lAux;
		double lRate;
		File rotIF;
		FileInputStream rotFIS;
		DataInputStream rotDIS;
		BufferedReader rotBR;

		String sceneryName = scenery.getName();
		String sceneryPath = project.getPath() + File.separator + "Traffic" + File.separator + sceneryName + File.separator;

		linkUtilization=new double[nRots][nRots];
		lPath=new String[nRots][nRots];
		int index, nLacos=(nCV==1)?1:3;			
		for(int a=0; a<nRots; a++)
			for(int b=0; b<nRots; b++)
				linkUtilization[a][b]=0;
		if(isHermesSR){
			for(index=0; index< nLacos; index++){
				switch(index){
					case 0: filename=(nCV==1)?noc.getRouting():noc.getCTRLRouting(); break;
					case 1: filename=noc.getGSRouting(); break;
					case 2: filename=noc.getBERouting(); break;
					default: filename="INVALID OPTION FILE";
				}
				try{
					rotIF = new File(filename);
					if(!rotIF.exists()) continue;
					rotFIS=new FileInputStream(rotIF);
					rotDIS=new DataInputStream(rotFIS);
					rotBR=new BufferedReader(new InputStreamReader(rotDIS));
					do{
						line=rotBR.readLine();
						st = new StringTokenizer(line, ";");
						if(st.countTokens()>0){
							lSource=Integer.parseInt(st.nextToken());
							lTarget=Integer.parseInt(st.nextToken());
							if(nCV!=1) st.nextToken(); // CONSOME A COLUNA REFERENTE A LANE
							st.nextToken(); // CONSOME O NRO DE HOPS
							lPath[lSource][lTarget]=st.nextToken();
						}
					}while(line != null);
					rotBR.close();
					rotDIS.close();
					rotFIS.close();
				}//end try
				catch(FileNotFoundException f){
					JOptionPane.showMessageDialog(null,"Can't Open File "+filename,"Input error", JOptionPane.ERROR_MESSAGE);
					System.exit(0);
				}
				catch(Exception e){}

				try{
					String sceneryFile = sceneryPath + sceneryName; 
					switch(index){
						case 0: filename= (nCV==1)? sceneryFile + ".acg" : sceneryFile + "_CTRL.acg"; break;
						case 1: filename= sceneryFile + "_GS.acg"; break;
						case 2: filename= sceneryFile + "_BE.acg"; break;
						default: filename="INVALID OPTION FILE";
					}
					rotIF = new File(filename);
					if(!rotIF.exists()){ JOptionPane.showMessageDialog(null,"File "+filename +" does not exist", "FILE ERROR", JOptionPane.ERROR_MESSAGE); continue; }
					rotFIS=new FileInputStream(rotIF);
					rotDIS=new DataInputStream(rotFIS);
					rotBR=new BufferedReader(new InputStreamReader(rotDIS));
					do{
						line=rotBR.readLine();
						st = new StringTokenizer(line, ";");
						if(st.countTokens()>0){
							lSource=Integer.parseInt(st.nextToken());
							lTarget=Integer.parseInt(st.nextToken());
							lRate=Double.parseDouble(st.nextToken()); 
							st.nextToken();
							lAux=lSource;
							lRunPath=lPath[lSource][lTarget];
							for(int i=0; i<lRunPath.length(); i++){
								switch(Integer.parseInt((String)(lRunPath.charAt(i)+""))){
									case 0:
										lAux+=1;
										break;
									case 1:
										lAux-=1;
										break;
									case 2:
										lAux+=noc.getNumRotX();
										break;
									case 3:
										lAux-=noc.getNumRotX();
										break;
								}
								linkUtilization[lSource][lAux]+=lRate;
								lSource=lAux;
							}
						}
					}while(line != null);
					rotBR.close();
					rotDIS.close();
					rotFIS.close();
				}//end try
				catch(FileNotFoundException f){
					JOptionPane.showMessageDialog(null,"Can't Open File "+filename,"Input error", JOptionPane.ERROR_MESSAGE);
					System.exit(0);
				}
				catch(Exception e){}
			}

			try{
				FileOutputStream  fos =  new FileOutputStream(sceneryPath + sceneryName + ".luf");
				OutputStreamWriter osw =  new OutputStreamWriter(fos);
				Writer on = new BufferedWriter(osw);
				for(int a=0; a<nRots; a++)
					for(int b=0; b<nRots; b++)
						if((a==b+1)||(a==b-1)||(a==b+noc.getNumRotX())||(a==b-noc.getNumRotX()))
							on.write(a+";"+b+";"+linkUtilization[a][b]+"\n");
				on.close();
				osw.close();
				fos.close();				
			}//end try
			catch(FileNotFoundException f){
				JOptionPane.showMessageDialog(null,f.getMessage(),"Error Message", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
			catch(Exception e){}

		}
	}

	/**
	 * Initialize the variables, create the directories and instantiate the measurer
	 */
	private void initialize(){
		//initialize the variables
		pathOut = project.getPath()+File.separator + "Traffic" + File.separator+scenery.getName()+File.separator + "Out";
		nRots = noc.getNumRotX()*noc.getNumRotY();
		//create the directories
		new File(pathOut+File.separator + "reports" + File.separator + "rel").mkdirs();
		new File(pathOut+File.separator + "reports" + File.separator + "graphs_txt").mkdirs();
		new File(pathOut+File.separator + "reports" + File.separator + "dat").mkdirs();
		new File(pathOut+File.separator + "reports" + File.separator + "graphs").mkdirs();
		new File(pathOut+File.separator + "reports" + File.separator + "images").mkdirs();
	}

	/**
	 * Add components to GUI.
	 */
	private void addComponents(){
		int x=5;
		int y=60;
		addMenu();
		addEvaluation(x+5,y,20);
		y+=30;
		addExternalPane(x,y,190,175);
		addInternalPane(x,y,190,175);
		addRateInternalPane(x,y,190,175);
		y+=190;
		addButtons(x+65,y,60,20);
		addCanvas(200,0,590,400);
	}

	/**
	 * Add the menu bar.
	 */
	private void addMenu(){
		mFile = new JMenu("File");
		mOpen = new JMenuItem("Open scenery");
		mOpen.addActionListener(this);
		mFile.add(mOpen);

		mReport = new JMenu("Reports");
		mLink = new JMenuItem("Links Analysis Report");
		
		if(project.getNoC().getType().equals("HermesG"))
		{
			mLink.setEnabled(false);
		}
		else
		{
			if(!scenery.isInternalSimulation())
			mLink.setEnabled(false);
		}	
		mLink.addActionListener(this);
		mReport.add(mLink);
		mLatency = new JMenuItem("Latency Analysis Report");
		mLatency.addActionListener(this);
		mReport.add(mLatency);
		mGlobal = new JMenuItem("Global Report");
		mGlobal.addActionListener(this);
		mReport.add(mGlobal);

		mHelp = new JMenu("Help");
		documentation = new JMenuItem("Documentation");
		documentation.addActionListener(this);
		mHelp.add(documentation);
		about = new JMenuItem("About Traffic Measurer");
		about.addActionListener(this);
		mHelp.add(about);

		menubar = new JMenuBar();
		menubar.add(mFile);
		menubar.add(mReport);
		menubar.add(mHelp);

		setJMenuBar(menubar);
	}

	/**
	 * Add the component allowing select the internal or external evaluation.
	 * @param x Horizontal initial position of the component.
	 * @param y Vertical initial position of the component.
	 * @param dimx The Horizontal dimension of the component.
	 * @param dimy The vertical dimension of the component.
	 */
	private void addEvaluation(int x,int y,int dimy){
		JLabel lEvaluation = new JLabel("Evaluation:");
		lEvaluation.setBounds(x,y,80,dimy);
		getContentPane().add(lEvaluation);

		// create JComboBox with Evaluation evaluation choices
		cbEvaluation = new JComboBox();
		cbEvaluation.setBounds(x+80,y,100,dimy);
		cbEvaluation.addActionListener(this);
		cbEvaluation.setToolTipText("Select the Evaluation.");

		cbEvaluation.addItem("External");
		if(scenery.isInternalSimulation())
			if(project.getNoC().getType().equals("HermesG")) {}
			else
				cbEvaluation.addItem("Internal");
		else
			cbEvaluation.setEnabled(false);
		getContentPane().add(cbEvaluation);
	}

	/**
	 * Add the button allowing start the traffic evaluation.
	 * @param x Horizontal initial position of the button.
	 * @param y Vertical initial position of the button.
	 * @param dimx The Horizontal dimension of the button.
	 * @param dimy The vertical dimension of the button.
	 */
	private void addButtons(int x,int y,int dimx,int dimy){
		bOk = new JButton("Ok");
		bOk.setBounds(x,y,dimx,dimy);
		bOk.addActionListener(this);
		bOk.setToolTipText("Start the traffic evaluation.");
		getContentPane().add(bOk);
	}

/************************************************************************************************
* EXTERNAL PANE
************************************************************************************************/
	/**
	 * Add the panel allowing select the external evaluation parameters.
	 * @param x Horizontal initial position of the panel.
	 * @param y Vertical initial position of the panel.
	 * @param dimx The Horizontal dimension of the panel.
	 * @param dimy The vertical dimension of the panel.
	 */
	private void addExternalPane(int x,int y,int dimx,int dimy){
		pbExternal=new JLayeredPane();
		pbExternal.setBounds(x,y,dimx,dimy);
		pbExternal.setBorder(BorderFactory.createTitledBorder(BorderFactory.createRaisedBevelBorder(),"External Evaluation",TitledBorder.CENTER,TitledBorder.TOP));
		pbExternal.setVisible(true);
		getContentPane().add(pbExternal);

		x=10;
		y=20;
		addExternal(pbExternal,x,y,dimx-20,20);
		y+=30;
		addSourceTargetExternal(pbExternal,x,y,20);
	}

	/**
	 * Add the component allowing select the external evaluation type.
	 * @param pane The panel where this component will be add.
	 * @param x Horizontal initial position of the component.
	 * @param y Vertical initial position of the component.
	 * @param dimx The Horizontal dimension of the component.
	 * @param dimy The vertical dimension of the component.
	 */
	private void addExternal(JLayeredPane pane,int x,int y,int dimx,int dimy){
		// create JComboBox with External evaluation choices
		cbExternal = new JComboBox();
		cbExternal.addItem("Throughput Distribution");
		cbExternal.addItem("Latency Distribution");
		cbExternal.setBounds(x,y,dimx,dimy);
		cbExternal.addActionListener(this);
		cbExternal.setToolTipText("Select the Type of Evaluation External.");
		pane.add(cbExternal);
	}

	/**
	 * Add the components when a specific flow will be analyzed.
	 * @param pane The panel where these component will be add.
	 * @param x Horizontal initial position of these component.
	 * @param y Vertical initial position of these component.
	 * @param dimy The vertical dimension of these component.
	 */
	public void addSourceTargetExternal(JLayeredPane pane,int x,int y,int dimy){
		int dimx=80;
		addSourceExternal(pane,x,y,dimx,dimy);
		y+=30;
		addTargetExternal(pane,x,y,dimx,dimy);
		y+=30;
        if(noc.getType().equals("HermesG")) { y+=30; }
        else		
        {
            addIntervalExternal(pane,x,y,dimx,dimy);
		    y+=30;
		    addPercentualExternal(pane,x,y,dimx,dimy);
        }
	}

	/**
	 * Add the component allowing select the source router.
	 * @param pane The panel where this component will be add.
	 * @param x Horizontal initial position of the component.
	 * @param y Vertical initial position of the component.
	 * @param dimx The Horizontal dimension of the component.
	 * @param dimy The vertical dimension of the component.
	 */
	private void addSourceExternal(JLayeredPane pane,int x,int y,int dimx,int dimy){
		JLabel lsourceExternal = new JLabel("Source:");
		lsourceExternal.setBounds(x,y,dimx,dimy);
		lsourceExternal.setToolTipText("Select the source nodo.");
		pane.add(lsourceExternal);

		x += dimx;
		tsourceExternal = new JTextField("00");
		tsourceExternal.setHorizontalAlignment(JTextField.RIGHT);
		tsourceExternal.setBounds(x,y,50,dimy);
		tsourceExternal.setToolTipText("Select the source nodo.");
		pane.add(tsourceExternal);
	}

	/**
	 * Add the component allowing select the target router.
	 * @param pane The panel where this component will be add.
	 * @param x Horizontal initial position of the component.
	 * @param y Vertical initial position of the component.
	 * @param dimx The Horizontal dimension of the component.
	 * @param dimy The vertical dimension of the component.
	 */
	private void addTargetExternal(JLayeredPane pane,int x,int y,int dimx,int dimy){
		JLabel ltargetExternal = new JLabel("Target:");
		ltargetExternal.setBounds(x,y,dimx,dimy);
		ltargetExternal.setToolTipText("Select the target nodo.");
		pane.add(ltargetExternal);

		x += dimx;
		ttargetExternal = new JTextField("11");
		ttargetExternal.setHorizontalAlignment(JTextField.RIGHT);
		ttargetExternal.setToolTipText("Select the target nodo.");
		ttargetExternal.setBounds(x,y,50,dimy);
		pane.add(ttargetExternal);
	}

	/**
	 * Add the component allowing select the interval.
	 * @param pane The panel where this component will be add.
	 * @param x Horizontal initial position of the component.
	 * @param y Vertical initial position of the component.
	 * @param dimx The Horizontal dimension of the component.
	 * @param dimy The vertical dimension of the component.
	 */
	private void addIntervalExternal(JLayeredPane pane,int x,int y,int dimx,int dimy){
		JLabel lintervalExternal = new JLabel("Interval:");
		lintervalExternal.setBounds(x,y,dimx,dimy);
		lintervalExternal.setToolTipText("Select the print interval.");
		pane.add(lintervalExternal);

		x += dimx;
		tintervalExternal = new JTextField("30");
		tintervalExternal.setHorizontalAlignment(JTextField.RIGHT);
		tintervalExternal.setToolTipText("Select the print interval.");
		tintervalExternal.setBounds(x,y,50,dimy);
		pane.add(tintervalExternal);
	}

	/**
	 * Add the component allowing select the discarded percentage.
	 * @param pane The panel where this component will be add.
	 * @param x Horizontal initial position of the component.
	 * @param y Vertical initial position of the component.
	 * @param dimx The Horizontal dimension of the component.
	 * @param dimy The vertical dimension of the component.
	 */
	private void addPercentualExternal(JLayeredPane pane,int x,int y,int dimx,int dimy){
		JLabel lpercentualExternal = new JLabel("Percentual:");
		lpercentualExternal.setBounds(x,y,dimx,dimy);
		lpercentualExternal.setToolTipText("Select the percentual of discarded packets.");
		pane.add(lpercentualExternal);

		x += dimx;
		tpercentualExternal = new JTextField("0");
		tpercentualExternal.setHorizontalAlignment(JTextField.RIGHT);
		tpercentualExternal.setToolTipText("Select the percentual of discarded packets.");
		tpercentualExternal.setBounds(x,y,50,dimy);
		pane.add(tpercentualExternal);
	}

/************************************************************************************************
* INTERNAL PANE
************************************************************************************************/
	/**
	 * Add the panel allowing select the internal evaluation parameters.
	 * @param x Horizontal initial position of the panel.
	 * @param y Vertical initial position of the panel.
	 * @param dimx The Horizontal dimension of the panel.
	 * @param dimy The vertical dimension of the panel.
	 */
	private void addInternalPane(int x,int y,int dimx,int dimy){
		pbInternal=new JLayeredPane();
		pbInternal.setBounds(x,y,dimx,dimy);
		pbInternal.setBorder(BorderFactory.createTitledBorder(BorderFactory.createRaisedBevelBorder(),"Internal Evaluation",TitledBorder.CENTER,TitledBorder.TOP));
		pbInternal.setVisible(false);
		getContentPane().add(pbInternal);

		x=10;
		y=20;
		addInternal(pbInternal,x,y,dimx-20,20);
		y+=30;
		addTextGraph(pbInternal,x,y,100,20);
		y+=30;
		add3DGraph(pbInternal,x,y,100,20);
	}

	/**
	 * Add the component allowing select the internal evaluation type.
	 * @param pane The panel where this component will be add.
	 * @param x Horizontal initial position of the component.
	 * @param y Vertical initial position of the component.
	 * @param dimx The Horizontal dimension of the component.
	 * @param dimy The vertical dimension of the component.
	 */
	public void addInternal(JLayeredPane pane,int x,int y,int dimx,int dimy){
		// create JComboBox with Internal evaluation choices
		cbInternal = new JComboBox();
		cbInternal.addItem("Number of flits transmitted");
		cbInternal.addItem("Cycles per flit (CPF)");
		cbInternal.addItem("Channel utilization");
		cbInternal.addItem("Throughput");
		cbInternal.addItem("Rates Distribution");
		cbInternal.setBounds(x,y,dimx,dimy);
		cbInternal.addActionListener(this);
		cbInternal.setToolTipText("Select the Type of Internal Evaluation.");
		pane.add(cbInternal);
	}

	/**
	 * Add the component allowing generate graphs in text format.
	 * @param pane The panel where these component will be add.
	 * @param x Horizontal initial position of these component.
	 * @param y Vertical initial position of these component.
	 * @param dimx The Horizontal dimension of the component.
	 * @param dimy The vertical dimension of these component.
	 */
	private void addTextGraph(JLayeredPane pane,int x,int y,int dimx,int dimy){
		cbTextGraph = new JCheckBox("Text Graph");
		cbTextGraph.setBounds(x,y,dimx,dimy);
		cbTextGraph.setToolTipText("Generate The Graphs In Format Text.");
		pane.add(cbTextGraph);
	}

	/**
	 * Add the component allowing generate graphs in 3D format.
	 * @param pane The panel where these component will be add.
	 * @param x Horizontal initial position of these component.
	 * @param y Vertical initial position of these component.
	 * @param dimx The Horizontal dimension of the component.
	 * @param dimy The vertical dimension of these component.
	 */
	private void add3DGraph(JLayeredPane pane,int x,int y,int dimx,int dimy){
		cb3DGraph = new JCheckBox("3D Graph");
		cb3DGraph.setBounds(x,y,dimx,dimy);
		cb3DGraph.setToolTipText("Generate The Graphs In Format 3D.");
		pane.add(cb3DGraph);
	}

/************************************************************************************************
* RATE INTERNAL PANE
************************************************************************************************/

	/**
	 * Add the panel allowing select the rate internal evaluation parameters.
	 * @param x Horizontal initial position of the panel.
	 * @param y Vertical initial position of the panel.
	 * @param dimx The Horizontal dimension of the panel.
	 * @param dimy The vertical dimension of the panel.
	 */
	private void addRateInternalPane(int x,int y,int dimx,int dimy){
		pbRateInternal=new JLayeredPane();
		pbRateInternal.setBounds(x,y,dimx,dimy);
		pbRateInternal.setBorder(BorderFactory.createTitledBorder(BorderFactory.createRaisedBevelBorder(),"Internal Evaluation",TitledBorder.CENTER,TitledBorder.TOP));
		pbRateInternal.setVisible(false);
		getContentPane().add(pbRateInternal);

		x=10;
		y=20;
		addRateInternal(pbRateInternal,x,y,dimx-20,20);
		y+=30;
		addSourceTargetInternal(pbRateInternal,x,y,20);
	}

	/**
	 * Add the component allowing select the rate internal evaluation type.
	 * @param pane The panel where this component will be add.
	 * @param x Horizontal initial position of the component.
	 * @param y Vertical initial position of the component.
	 * @param dimx The Horizontal dimension of the component.
	 * @param dimy The vertical dimension of the component.
	 */
	private void addRateInternal(JLayeredPane pane,int x,int y,int dimx,int dimy){
		cbRateInternal = new JComboBox();
		cbRateInternal.addItem("Number of flits transmitted");
		cbRateInternal.addItem("Cycles per flit (CPF)");
		cbRateInternal.addItem("Channel utilization");
		cbRateInternal.addItem("Throughput");
		cbRateInternal.addItem("Rates Distribution");
		cbRateInternal.setBounds(x,y,dimx,dimy);
		cbRateInternal.addActionListener(this);
		cbRateInternal.setToolTipText("Select the Type of Internal Evaluation.");
		pane.add(cbRateInternal);
	}

	/**
	 * Add the components when a specific flow will be analyzed.
	 * @param pane The panel where these component will be add.
	 * @param x Horizontal initial position of these component.
	 * @param y Vertical initial position of these component.
	 * @param dimy The vertical dimension of these component.
	 */
	private void addSourceTargetInternal(JLayeredPane pane,int x,int y,int dimy){
		int dimx=80;
		addSourceInternal(pane,x,y,dimx,dimy);
		y+=30;
		addTargetInternal(pane,x,y,dimx,dimy);
		y+=30;
		addIntervalInternal(pane,x,y,dimx,dimy);
		y+=30;
		addPercentualInternal(pane,x,y,dimx,dimy);
	}

	/**
	 * Add the component allowing select the source router.
	 * @param pane The panel where this component will be add.
	 * @param x Horizontal initial position of the component.
	 * @param y Vertical initial position of the component.
	 * @param dimx The Horizontal dimension of the component.
	 * @param dimy The vertical dimension of the component.
	 */
	private void addSourceInternal(JLayeredPane pane,int x,int y,int dimx,int dimy){
		JLabel lsourceInternal = new JLabel("Source:");
		lsourceInternal.setBounds(x,y,dimx,dimy);
		lsourceInternal.setToolTipText("Select the source nodo.");
		pane.add(lsourceInternal);

		x += dimx;
		tsourceInternal = new JTextField("00");
		tsourceInternal.setHorizontalAlignment(JTextField.RIGHT);
		tsourceInternal.setBounds(x,y,50,dimy);
		tsourceInternal.setToolTipText("Select the source nodo.");
		pane.add(tsourceInternal);
	}

	/**
	 * Add the component allowing select the target router.
	 * @param pane The panel where this component will be add.
	 * @param x Horizontal initial position of the component.
	 * @param y Vertical initial position of the component.
	 * @param dimx The Horizontal dimension of the component.
	 * @param dimy The vertical dimension of the component.
	 */
	private void addTargetInternal(JLayeredPane pane,int x,int y,int dimx,int dimy){
		JLabel ltargetInternal = new JLabel("Target:");
		ltargetInternal.setBounds(x,y,dimx,dimy);
		ltargetInternal.setToolTipText("Select the target nodo.");
		pane.add(ltargetInternal);

		x += dimx;
		ttargetInternal = new JTextField("11");
		ttargetInternal.setHorizontalAlignment(JTextField.RIGHT);
		ttargetInternal.setToolTipText("Select the target nodo.");
		ttargetInternal.setBounds(x,y,50,dimy);
		pane.add(ttargetInternal);
	}

	/**
	 * Add the component allowing select the interval.
	 * @param pane The panel where this component will be add.
	 * @param x Horizontal initial position of the component.
	 * @param y Vertical initial position of the component.
	 * @param dimx The Horizontal dimension of the component.
	 * @param dimy The vertical dimension of the component.
	 */
	private void addIntervalInternal(JLayeredPane pane,int x,int y,int dimx,int dimy){
		JLabel lintervalInternal = new JLabel("Interval:");
		lintervalInternal.setBounds(x,y,dimx,dimy);
		lintervalInternal.setToolTipText("Select the print interval.");
		pane.add(lintervalInternal);

		x += dimx;
		tintervalInternal = new JTextField("30");
		tintervalInternal.setHorizontalAlignment(JTextField.RIGHT);
		tintervalInternal.setToolTipText("Select the print interval.");
		tintervalInternal.setBounds(x,y,50,dimy);
		pane.add(tintervalInternal);
	}

	/**
	 * Add the component allowing select the discarded percentage.
	 * @param pane The panel where this component will be add.
	 * @param x Horizontal initial position of the component.
	 * @param y Vertical initial position of the component.
	 * @param dimx The Horizontal dimension of the component.
	 * @param dimy The vertical dimension of the component.
	 */
	private void addPercentualInternal(JLayeredPane pane,int x,int y,int dimx,int dimy){
		JLabel lpercentualInternal = new JLabel("Percentual:");
		lpercentualInternal.setBounds(x,y,dimx,dimy);
		lpercentualInternal.setToolTipText("Select the percentual of discarded packets.");
		pane.add(lpercentualInternal);

		x += dimx;
		tpercentualInternal = new JTextField("0");
		tpercentualInternal.setHorizontalAlignment(JTextField.RIGHT);
		tpercentualInternal.setToolTipText("Select the percentual of discarded packets.");
		tpercentualInternal.setBounds(x,y,50,dimy);
		pane.add(tpercentualInternal);
	}

	/**
	 * Add the panel allowing show the NoC topology.
	 * @param x Horizontal initial position of the panel. 
	 * @param y Vertical initial position of the panel.
	 * @param dimx The Horizontal dimension of the panel.
	 * @param dimy The vertical dimension of the panel.
	 */
	private void addCanvas(int x,int y,int dimx ,int dimy){
		jpanel_noc = new JPanel_Noc(x,y,dimx,dimy,noc.getType());
		getContentPane().add(jpanel_noc);
		jpanel_noc.setNoCDimension(noc.getNumRotX(),noc.getNumRotY());
	}

/************************************************************************************************
* METODOS
************************************************************************************************/
	/**
	 * Return true if the informed JTextField contains an integer value.
	 * @param tf The JTextfield that will be tested.
	 * @param name The name of parameter associated to the JTextField.
	 * @return True if the JTextField contains an integer value.   
	 */
	private boolean verifyInteger(JTextField tf,String name){
		if(tf.getText().equals("")) JOptionPane.showMessageDialog(null,"Inform the "+name+".","Error",JOptionPane.ERROR_MESSAGE);
		else{
			try{
				Integer.parseInt(tf.getText());
				return true;
			}catch(NumberFormatException exc){
				JOptionPane.showMessageDialog(null,name+" Invalid.","Error Message",JOptionPane.ERROR_MESSAGE);
				tf.setText("");
			}
		}
		return false;
	}

	/**
	 * Return true if the informed JTextField contains a valid router address.
	 * @param tf The JTextfield that will be tested.
	 * @param name The name of parameter associated to the JTextField.
	 * @return True if the JTextField contains a value router address.   
	 */
	private boolean verifyNodo(JTextField tf,String name)
	{
		boolean ok = verifyInteger(tf,name);
		if(ok){
			String nodo = tf.getText();
			if(nodo.length()!=2){
				JOptionPane.showMessageDialog(null,name+" Invalid. The value must have length igual to two. For example: 10 or AA","Error",JOptionPane.ERROR_MESSAGE);
			}
			else{
				int x = Integer.parseInt(nodo.substring(0,nodo.length()/2));
				int y = Integer.parseInt(nodo.substring(nodo.length()/2));
				int n = x + (y * noc.getNumRotX());
				int dim = noc.getNumRotX()*noc.getNumRotY();
				if(n>=dim)
					JOptionPane.showMessageDialog(null,name+" Invalid. The value cannot be bigger than "+dim+"(total number of nodos).","Error",JOptionPane.ERROR_MESSAGE);
				else
					return true;
			}
		}
		return false;
	}

	/**
	 * Generate the External evaluation graphs.
	 */
	private void generateExternal(){
		if(verifyNodo(tsourceExternal,"Source Nodo"))
        {
			if(verifyNodo(ttargetExternal,"Target Nodo"))
            {
                if(noc.getType().equals("HermesG"))
                {
                    String source = Convert.formatAddressXY(tsourceExternal.getText(),noc.getFlitSize());
					String target = Convert.formatAddressXY(ttargetExternal.getText(),noc.getFlitSize());
                    int interval = 0;
                    int percentual = 0;
                    try
                    {
					    int type = 0;
					    String aux = (String)cbExternal.getSelectedItem();
					    if(aux.equalsIgnoreCase("Latency Distribution"))
                        {
						    new DistrLat(project, "distr_lat", type, source, target, interval, percentual);
					    }
					    else if(aux.equalsIgnoreCase("Throughput Distribution"))
                        {									
						    new DistrThroughput(project, "distr_throughput", type, source, target, interval, percentual);
					    }
				    }
				    catch(Exception exc)
                    {
					    JOptionPane.showMessageDialog(null,exc.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
                    }
                }
                else
                {
				    if(verifyInteger(tintervalExternal,"Print Interval"))
                    {
					    if(verifyInteger(tpercentualExternal,"Percentual of Discarded Packets"))
                        {
						    String source = Convert.formatAddressXY(tsourceExternal.getText(),noc.getFlitSize());
						    String target = Convert.formatAddressXY(ttargetExternal.getText(),noc.getFlitSize());
                            int interval;
                            int percentual;
					        interval = Integer.parseInt(tintervalExternal.getText());
					        percentual = Integer.parseInt(tpercentualExternal.getText());
						    if(percentual<0 || percentual>100) JOptionPane.showMessageDialog(null,"Percentual of discarded packets invalid. Inform a value between 0 and 100%.","Error",JOptionPane.ERROR_MESSAGE);
						    else{
							    try{
								    int type = 0;
								    String aux = (String)cbExternal.getSelectedItem();
								    if(aux.equalsIgnoreCase("Latency Distribution")){
									    new DistrLat(project, "distr_lat", type, source, target, interval, percentual);
								    }
								    else if(aux.equalsIgnoreCase("Throughput Distribution")){									
									    new DistrThroughput(project, "distr_throughput", type, source, target, interval, percentual);
								    }
							    }
							    catch(Exception exc){
								    JOptionPane.showMessageDialog(null,exc.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
							    }
						    }
					    }
				    }
                }
			}
		}
	}

	/**
	 * generate the internal evaluation graphs. 
	 */
	private void generateInternalRates(){
		if(verifyNodo(tsourceInternal,"Source nodo")){
			if(verifyNodo(ttargetInternal,"Target nodo")){
				if(verifyInteger(tintervalInternal,"Print Interval")){
					if(verifyInteger(tpercentualInternal,"Percentual of Discarded Packets")){
						String source = Convert.formatAddressXY(tsourceInternal.getText(),noc.getFlitSize());
						String target = Convert.formatAddressXY(ttargetInternal.getText(),noc.getFlitSize());
						int interval = Integer.parseInt(tintervalInternal.getText());
						int percentual = Integer.parseInt(tpercentualInternal.getText());
						if(percentual<0 || percentual>100) JOptionPane.showMessageDialog(null,"Percentual of discarded packets invalid. Inform a value between 0 and 100%.","Error",JOptionPane.ERROR_MESSAGE);
						else{
							try{
								new DistrRate(project, source,target,interval,percentual);
							}
							catch(Exception exc){
								JOptionPane.showMessageDialog(null,exc.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Close the GUI.
	 */
	private void end(){
		dispose();
	}

    public void actionPerformed(ActionEvent e){
		if(e.getSource() == cbEvaluation){
			String aux = (String)cbEvaluation.getSelectedItem();
			if(aux.equalsIgnoreCase("External")){
				if(pbInternal!=null) pbInternal.setVisible(false);
				if(pbRateInternal!=null) pbRateInternal.setVisible(false);
				if(pbExternal!=null) pbExternal.setVisible(true);
			}
			else if(aux.equalsIgnoreCase("Internal")){
				if(pbExternal!=null) pbExternal.setVisible(false);
				if(pbInternal!=null) {
					aux = (String)cbInternal.getSelectedItem();
					if(aux.equalsIgnoreCase("Rates Distribution")){
						if(pbInternal!=null) pbInternal.setVisible(false);
						if(pbRateInternal!=null){
							cbRateInternal.setSelectedItem(aux);
							pbRateInternal.setVisible(true);
						}
					}
					else{
						if(pbRateInternal!=null) pbRateInternal.setVisible(false);
						if(pbInternal!=null) pbInternal.setVisible(true);
					}
				}
			}
		}
		else if(e.getSource() == cbInternal){
			String aux = (String)cbInternal.getSelectedItem();
			if(aux.equalsIgnoreCase("Rates Distribution")){
				if(pbInternal!=null) pbInternal.setVisible(false);
				if(pbRateInternal!=null){
					cbRateInternal.setSelectedItem(aux);
					pbRateInternal.setVisible(true);
				}
			}
		}
		else if(e.getSource() == cbRateInternal){
			String aux = (String)cbRateInternal.getSelectedItem();
			if(!aux.equalsIgnoreCase("Rates Distribution")){
				if(pbInternal!=null){
					cbInternal.setSelectedItem(aux);
					pbInternal.setVisible(true);
				}
				if(pbRateInternal!=null) pbRateInternal.setVisible(false);
			}
		}
		else if(e.getSource() == bOk){
			String aux = (String)cbEvaluation.getSelectedItem();
			if(aux.equalsIgnoreCase("External")){
				generateExternal();
				tsourceInternal.setText(tsourceExternal.getText());
				ttargetInternal.setText(ttargetExternal.getText());
			}
			else if(aux.equalsIgnoreCase("Internal")){
				aux = (String)cbInternal.getSelectedItem();
				if(aux.equalsIgnoreCase("Rates Distribution")){
					generateInternalRates();
					tsourceExternal.setText(tsourceInternal.getText());
					ttargetExternal.setText(ttargetInternal.getText());
				}
				else{
					if(!cbTextGraph.isSelected() && !cb3DGraph.isSelected())
						JOptionPane.showMessageDialog(null,"Select the type of graph first.","Error",JOptionPane.ERROR_MESSAGE);
					else{
						if(noc.getVirtualChannel()==1){
							if(cb3DGraph.isSelected())   new Graph3D(project).generate(aux);
							if(cbTextGraph.isSelected()) new GraphTXT(project).generate(aux);
						}
						else{
							if(cb3DGraph.isSelected())   new Graph3D_VC(project).generate(aux);
							if(cbTextGraph.isSelected()) new GraphTXT_VC(project).generate(aux);
						}
					}
				}
			}
		}
		else if(e.getSource() == mLatency){
			new LatencyReport(project);
		}
		else if(e.getSource() == mGlobal){
     		new GlobalReport(project);
		}
		else if(e.getSource() == mLink){
			if(noc.getVirtualChannel() == 1){
				new LinkReport(project).generate();
			}
			else{
				new LinkReport_VC(project).generate();
			}
		}
		else if(e.getSource() == documentation){
		    Help.show("https://corfu.pucrs.br/redmine/projects/atlas/wiki/Traffic_Measurer");
		}
		else if(e.getSource() == about)
			JOptionPane.showMessageDialog(this,"Traffic Measurer          16.08.2006\nDeveloped by:\n        Aline Vieira de Mello\n           Leonel Tedesco","VERSION 1.0",JOptionPane.INFORMATION_MESSAGE);
		else if(e.getSource() == mOpen){
			this.dispose();
			new OpenWindow(project);
		}
    }
}
