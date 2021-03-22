package TrafficMbps;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import AtlasPackage.Project;
import AtlasPackage.Scenery;
import AtlasPackage.Router;
import AtlasPackage.RouterTraffic;
import AtlasPackage.Default;
import AtlasPackage.NoC;

/**
 * This class shows the GUI allowing configure a router traffic or the standard traffic.
 * @author Aline Vieira de Mello
 * @version
 */
public class InterfaceConfig extends JFrame implements ActionListener{

	private Project project;
	private Scenery scenery;
	private Router router;
	private RouterTraffic traffic;
	private JLayeredPane panelNormal,panelUniform,panelPareto,panelExponential;
	private JTextField tfrequency,tpacketSize,tnumberPackets;
	private JTextField trate,tminrateNormal,tminrateExponential,tmaxrateNormal,tmaxrateExponential,tincrateNormal,tincrateExponential,tavgrateNormal,tavgrateExponential,tdvrate,trateOn,tburst;
	private JComboBox cbtarget,cbdist,cbpriority;
	private JButton ok,graphNormal,graphExponential;
	private String scheduling;
	private int dimXNet,dimYNet;
	private int flitWidth,flitClockCycles;

	/**
	* Create the GUI allowing configure a standard traffic.
	* @param project The NoC project.
	* @param scenery The scenery that will be configured.
	*/
	public InterfaceConfig(Project project, Scenery scenery){
		super("Standard Configuration");
		this.project = project;
		this.scenery = scenery;
		this.router = null;
		
		this.traffic = scenery.getStandardTraffic();
		initilize();
 	}
	
	/**
	* Create the GUI allowing configure a router traffic.
	* @param project The NoC project.
	* @param router The router that will be configured.
	*/
	public InterfaceConfig(Project project, Router router){
		super("Router "+router.getAddress());
		this.project = project;
		this.scenery = null;
		this.router = router;
		this.traffic = router.getTraffic();
		initilize();
 	}

	/**
	 * Initialize the variables class.
	 */
	private void initilize(){
		dimXNet = project.getNoC().getNumRotX();
		dimYNet = project.getNoC().getNumRotY();
		flitWidth = project.getNoC().getFlitSize();
		flitClockCycles = project.getNoC().getCyclesPerFlit();
		scheduling = project.getNoC().getScheduling();
		addProperties();
		addComponents();
		update();
		
		setVisible(true);
	}

	/**
	* add the interface properties.
	*/
	private void addProperties(){
		getContentPane().setLayout(null);
		Dimension resolucao=Toolkit.getDefaultToolkit().getScreenSize();
		//the interface is bigger when the scheduling is priority based. 
		if(scheduling.equalsIgnoreCase("Priority")){
			setSize(260,410);
			setLocation((resolucao.width-260)/2,(resolucao.height-410)/2);
		}
		else
		{
			if(project.getNoC().getType().equals("HermesG"))
			{
				if(router == null)
				{
					setSize(260,360);
				}
				else
				{
					setSize(260,380);
				}
			}
			else
			{
				setSize(260,380);
			}
			setLocation((resolucao.width-260)/2,(resolucao.height-380)/2);
		}
		setResizable(false);
		addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){dispose();}});
 	}

	/**
	* add components to interface.
	*/
	private void addComponents(){
		int x = 10;
		int y = 10;
		int stepY = 25;
		
		
		if(project.getNoC().getType().equals("HermesG"))
		{
			if(router != null)
			{
				addFrequency(x,y);
				y = y + stepY;
			}
		}
		else
		{
			addFrequency(x,y);
			y = y + stepY;
		}
		addTarget(x,y);
		//when the scheduling is priority based, the traffic priority should be informed.
		if(scheduling.equalsIgnoreCase("Priority")){
			y = y + stepY;
			addPriority(x,y);
		}
		
		y = y + stepY;
		addNumberPackets(x,y);
		y = y + stepY;
		addPacketSize(x,y);
		y = y + stepY;
		addDistTime(x,y);
		y = y + stepY;
		addPanelUniform(7,y,240,180);
		addPanelNormal(7,y,240,180);
		addPanelPareto(7,y,240,180);
		addPanelExponential(7,y,240,180);
		y = y + 185;
		addOk(x+80,y);
	}

	private void addFrequency(int x,int y)
	{
		if(project.getNoC().getType().equals("HermesG"))
		{
			int i=0;
			if(router != null)
			{
				for(;i<project.getNoC().getClock().size();i++)
				{
					if(project.getNoC().getClock().get(i).getNumberRouter().equals(router.getAddress()))
					break;
				}
				JLabel lfrequency = new JLabel("Frequency");
				lfrequency.setBounds(x,y,70,20);
				getContentPane().add(lfrequency);
				x += 115;
				tfrequency = new JTextField(String.valueOf(project.getNoC().getClock().get(i).getClockIpInput()));
				tfrequency.setHorizontalAlignment(JTextField.RIGHT);
				tfrequency.setEnabled(false);
				tfrequency.setBounds(x,y,50,20);
				getContentPane().add(tfrequency);
				x += 55;
				JLabel lmhz = new JLabel(String.valueOf(project.getNoC().getClock().get(i).getUnitIpInput()));
				lmhz.setBounds(x,y,40,20);
				getContentPane().add(lmhz);
			}
		}
		else
		{
			JLabel lfrequency = new JLabel("Frequency");
			lfrequency.setBounds(x,y,70,20);
			getContentPane().add(lfrequency);
			x += 115;
			tfrequency = new JTextField();
			tfrequency.setHorizontalAlignment(JTextField.RIGHT);
			tfrequency.setEnabled(true);
			tfrequency.setBounds(x,y,50,20);
			getContentPane().add(tfrequency);
			x += 55;
			JLabel lmhz = new JLabel("MHz");
			lmhz.setBounds(x,y,40,20);
			getContentPane().add(lmhz);
		}		
	}
	private void addTarget(int x,int y){
		JLabel ltarget = new JLabel("Target");
		ltarget.setBounds(x,y,50,20);
		getContentPane().add(ltarget);

		x += 115;
		cbtarget= new JComboBox();
		cbtarget.setBounds(x,y,120,20);
		getContentPane().add(cbtarget);
		cbtarget.addItem("random");
		if(dimYNet%2==0 && dimXNet%2==0)
			cbtarget.addItem("complemento");
		
		//cbtarget.addItem("bitReversal");
		//cbtarget.addItem("butterfly");
		//cbtarget.addItem("matrixTranspose");
		//cbtarget.addItem("perfectShuffle");
		for(int j=0;j<dimYNet;j++){
			for(int i=0;i<dimXNet;i++){
				if(router == null)
				{
					if(i<10 && j<10) cbtarget.addItem("0"+i+"0"+j);
					else if(i<10) cbtarget.addItem("0"+i+j);
					else if(j<10) cbtarget.addItem(""+i+"0"+j);
					else cbtarget.addItem(""+i+j);
				}
				else
				{
					if((project.getNoC().getType().equals("HermesG")) && router.getAddress().equals(""+i+j)) {}
					else
					{
						if(i<10 && j<10) cbtarget.addItem("0"+i+"0"+j);
						else if(i<10) cbtarget.addItem("0"+i+j);
						else if(j<10) cbtarget.addItem(""+i+"0"+j);
						else cbtarget.addItem(""+i+j);
					}
				}
				
			}
		}
		
		if(router!=null) { //router traffic configuration
			//remove this address router from target JCombobox
			// A router cannot send a traffic to itself
			cbtarget.removeItem(router.getAddress());
	
			// If the target selected in the standard configuration is this router
			// this router must not generate traffic 
			if(router.getTraffic().getTarget().equalsIgnoreCase(router.getAddress())){
				traffic.disable();
			}
			else{
				cbtarget.setSelectedItem(router.getTraffic().getTarget());
			}
		}		
	}

	private void addNumberPackets(int x,int y){
		JLabel lnumberPackets = new JLabel("Number of Packets");
		lnumberPackets.setBounds(x,y,110,20);
		getContentPane().add(lnumberPackets);

		x += 115;
		tnumberPackets= new JTextField();
		tnumberPackets.setHorizontalAlignment(JTextField.RIGHT);
		tnumberPackets.setBounds(x,y,50,20);
		getContentPane().add(tnumberPackets);
	}

	private void addPacketSize(int x,int y){
		JLabel lpacketSize = new JLabel("Packet Size");
		lpacketSize.setBounds(x,y,70,20);
		getContentPane().add(lpacketSize);

		x += 115;
		tpacketSize= new JTextField();
		tpacketSize.setHorizontalAlignment(JTextField.RIGHT);
		tpacketSize.setBounds(x,y,50,20);
		getContentPane().add(tpacketSize);

		x += 55;

		if( project.getNoC().getType().equalsIgnoreCase(NoC.MERCURY)){
			JLabel lflits = new JLabel("Phits");			
			tpacketSize.setEnabled(false);
			lflits.setBounds(x,y,40,20);
			getContentPane().add(lflits);
		}
		else{
			JLabel lflits = new JLabel("Flits");
			lflits.setBounds(x,y,40,20);
			lflits.setToolTipText("Each flit contains "+project.getNoC().getFlitSize()+" bits.");
			getContentPane().add(lflits);
		}


	}

	private void addPriority(int x,int y){
		JLabel lpriority = new JLabel("Priority");
		lpriority.setBounds(x,y,70,20);
		getContentPane().add(lpriority);

		x += 115;
		cbpriority = new JComboBox();
		cbpriority.setBounds(x,y,120,20);
		getContentPane().add(cbpriority);
		for(int i=0;i<project.getNoC().getVirtualChannel();i++)
			cbpriority.addItem(""+i);
	}
	
	private void addDistTime(int x,int y){
		JLabel ldist = new JLabel("Distribution");
		ldist.setBounds(x,y,100,20);
		getContentPane().add(ldist);

		x += 115;
		cbdist= new JComboBox();
		cbdist.setBounds(x,y,120,20);
		cbdist.addItem("uniform");
		cbdist.addItem("normal");
		cbdist.addItem("paretoOn/Off");
		cbdist.addItem("exponential");
		getContentPane().add(cbdist);
		cbdist.addActionListener(this);
		cbdist.setEnabled(!project.getNoC().getType().equalsIgnoreCase("hermessr"));
	}

	/**
	* Add the Panel with the parameters of NORMAL traffic distribution.
	*/
	private void addPanelNormal(int x,int y,int dimx,int dimy){
		//Panel Border
		panelNormal=new JLayeredPane();
		panelNormal.setBounds(x,y,dimx,dimy);
		panelNormal.setBorder(BorderFactory.createTitledBorder(BorderFactory.createRaisedBevelBorder(),"Normal Distribution",TitledBorder.CENTER,TitledBorder.TOP));
		panelNormal.setVisible(false);
		getContentPane().add(panelNormal);

		int i = 10, j = 20;
		addAvgRateNormal(panelNormal,i,j);
		j = j + 25;
		addMinRateNormal(panelNormal,i,j);
		j = j + 25;
		addMaxRateNormal(panelNormal,i,j);
		j = j + 25;
		addDvRate(panelNormal,i,j);
		j = j + 25;
		addIncRateNormal(panelNormal,i,j);
		j = j + 25;
		addGraphNormal(panelNormal,i+125,j);
	}

	/**
	* Add the Panel with the parameters of UNIFORME traffic distribution.
	*/
	private void addPanelUniform(int x,int y,int dimx,int dimy){
		//Panel Border
		panelUniform=new JLayeredPane();
		panelUniform.setBounds(x,y,dimx,dimy);
		panelUniform.setBorder(BorderFactory.createTitledBorder(BorderFactory.createRaisedBevelBorder(),"Uniform Distribution",TitledBorder.CENTER,TitledBorder.TOP));
		getContentPane().add(panelUniform);

		addRate(panelUniform,10,20);
	}

	/**
	* Add the Panel with the parameters of PARETO traffic distribution.
	*/
	private void addPanelPareto(int x,int y,int dimx,int dimy){
		//Panel Border
		panelPareto=new JLayeredPane();
		panelPareto.setBounds(x,y,dimx,dimy);
		panelPareto.setBorder(BorderFactory.createTitledBorder(BorderFactory.createRaisedBevelBorder(),"Pareto ON/OFF Distribution",TitledBorder.CENTER,TitledBorder.TOP));
		panelPareto.setVisible(false);
		getContentPane().add(panelPareto);

		int i = 10, j = 20;
		addRateOn(panelPareto,i,j);
		j = j + 25;
		addBurst(panelPareto,i,j);
	}
	
	/**
	* Add the Panel with the parameters of EXPONENTIAL traffic distribution.
	*/
	private void addPanelExponential(int x,int y,int dimx,int dimy)
	{
		panelExponential=new JLayeredPane();
		panelExponential.setBounds(x,y,dimx,dimy);
		panelExponential.setBorder(BorderFactory.createTitledBorder(BorderFactory.createRaisedBevelBorder(),"Exponential Distribution",TitledBorder.CENTER,TitledBorder.TOP));
		panelExponential.setVisible(false);
		getContentPane().add(panelExponential);
		int i = 10, j = 20;
		addAvgRateExponential(panelExponential,i,j);
		j = j + 25;
		addMinRateExponential(panelExponential,i,j);
		j = j + 25;
		addMaxRateExponential(panelExponential,i,j);
		j = j + 25;
		addIncRateExponential(panelExponential,i,j);
		j = j + 25;
		addGraphExponential(panelExponential,i+125,j);
		
	}
	private void addRate(JLayeredPane panel,int x,int y){
		JLabel lrate = new JLabel("Rate");
		lrate.setBounds(x,y,30,20);
		panel.add(lrate);

		x += 125;
		trate = new JTextField("100");
		trate.setHorizontalAlignment(JTextField.RIGHT);
		trate.setBounds(x,y,50,20);
		panel.add(trate);

		x += 55;
		JLabel lmbps = new JLabel();
		if(project.getNoC().getType().equals("HermesG"))
		{
			if(router != null)
			{
				int i=0;
				for(;i<project.getNoC().getClock().size();i++)
				{
					if(project.getNoC().getClock().get(i).getNumberRouter().equals(router.getAddress()))
					break;
				}
				if(project.getNoC().getClock().get(i).getUnitIpInput().equals("Mhz")) lmbps = new JLabel("Mbps");
				else lmbps = new JLabel("Kbps");
			}
			else
			{
				int i=0,b=0,c=0;
				for(;i<project.getNoC().getClock().size();i++)
				{
					if(project.getNoC().getClock().get(i).getUnitIpInput().equals("Mhz")) b = 1;
					else c = 1;
				}
				if(b == 1 && c == 1) lmbps = new JLabel("Mbps/Kbps");
				else if(b == 1) lmbps = new JLabel("Mbps");
				else if(c == 1) lmbps = new JLabel("Kbps");
			}
		}
		else lmbps = new JLabel("Mbps");
		lmbps.setBounds(x,y,40,20);
		panel.add(lmbps);
	}

	private void addMinRateNormal(JLayeredPane panel,int x,int y){
		JLabel lminrateNormal = new JLabel("Minimal Rate");
		lminrateNormal.setBounds(x,y,100,20);
		panel.add(lminrateNormal);

		x += 125;
		tminrateNormal = new JTextField();
		tminrateNormal.setHorizontalAlignment(JTextField.RIGHT);
		tminrateNormal.setBounds(x,y,50,20);
		panel.add(tminrateNormal);

		x += 55;
		JLabel lmbpsNormal = new JLabel();
		if(project.getNoC().getType().equals("HermesG"))
		{
			if(router != null)
			{
				int i=0;
				for(;i<project.getNoC().getClock().size();i++)
				{
					if(project.getNoC().getClock().get(i).getNumberRouter().equals(router.getAddress()))
					break;
				}
				if(project.getNoC().getClock().get(i).getUnitIpInput().equals("Mhz")) lmbpsNormal = new JLabel("Mbps");
				else lmbpsNormal = new JLabel("Kbps");
			}
			else
			{
				int i=0,b=0,c=0;
				for(;i<project.getNoC().getClock().size();i++)
				{
					if(project.getNoC().getClock().get(i).getUnitIpInput().equals("Mhz")) b = 1;
					else c = 1;
				}
				if(b == 1 && c == 1) lmbpsNormal = new JLabel("Mbps/Kbps");
				else if(b == 1) lmbpsNormal = new JLabel("Mbps");
				else if(c == 1) lmbpsNormal = new JLabel("Kbps");
			}
		}
		else lmbpsNormal = new JLabel("Mbps");
		lmbpsNormal.setBounds(x,y,40,20);
		panel.add(lmbpsNormal);
	}

	private void addMinRateExponential(JLayeredPane panel,int x,int y){
		JLabel lminrateExponential = new JLabel("Minimal Rate");
		lminrateExponential.setBounds(x,y,100,20);
		panel.add(lminrateExponential);

		x += 125;
		tminrateExponential = new JTextField();
		tminrateExponential.setHorizontalAlignment(JTextField.RIGHT);
		tminrateExponential.setBounds(x,y,50,20);
		panel.add(tminrateExponential);

		x += 55;
		JLabel lmbpsExponential = new JLabel();
		if(project.getNoC().getType().equals("HermesG"))
		{
			if(router != null)
			{
				int i=0;
				for(;i<project.getNoC().getClock().size();i++)
				{
					if(project.getNoC().getClock().get(i).getNumberRouter().equals(router.getAddress()))
					break;
				}
				if(project.getNoC().getClock().get(i).getUnitIpInput().equals("Mhz")) lmbpsExponential = new JLabel("Mbps");
				else lmbpsExponential = new JLabel("Kbps");
			}
			else
			{
				int i=0,b=0,c=0;
				for(;i<project.getNoC().getClock().size();i++)
				{
					if(project.getNoC().getClock().get(i).getUnitIpInput().equals("Mhz")) b = 1;
					else c = 1;
				}
				if(b == 1 && c == 1) lmbpsExponential = new JLabel("Mbps/Kbps");
				else if(b == 1) lmbpsExponential = new JLabel("Mbps");
				else if(c == 1) lmbpsExponential = new JLabel("Kbps");
			}
		}
		else lmbpsExponential = new JLabel("Mbps");
		lmbpsExponential.setBounds(x,y,40,20);
		panel.add(lmbpsExponential);
	}
	private void addMaxRateNormal(JLayeredPane panel,int x,int y){
		JLabel lmaxrateNormal = new JLabel("Maximal Rate");
		lmaxrateNormal.setBounds(x,y,100,20);
		panel.add(lmaxrateNormal);

		x += 125;
		tmaxrateNormal = new JTextField();
		tmaxrateNormal.setHorizontalAlignment(JTextField.RIGHT);
		tmaxrateNormal.setBounds(x,y,50,20);
		panel.add(tmaxrateNormal);

		x += 55;
		JLabel lmbpsNormal = new JLabel();
		if(project.getNoC().getType().equals("HermesG"))
		{
			if(router != null)
			{
				int i=0;
				for(;i<project.getNoC().getClock().size();i++)
				{
					if(project.getNoC().getClock().get(i).getNumberRouter().equals(router.getAddress()))
					break;
				}
				if(project.getNoC().getClock().get(i).getUnitIpInput().equals("Mhz")) lmbpsNormal = new JLabel("Mbps");
				else lmbpsNormal = new JLabel("Kbps");
			}
			else
			{
				int i=0,b=0,c=0;
				for(;i<project.getNoC().getClock().size();i++)
				{
					if(project.getNoC().getClock().get(i).getUnitIpInput().equals("Mhz")) b = 1;
					else c = 1;
				}
				if(b == 1 && c == 1) lmbpsNormal = new JLabel("Mbps/Kbps");
				else if(b == 1) lmbpsNormal = new JLabel("Mbps");
				else if(c == 1) lmbpsNormal = new JLabel("Kbps");
			}
		}
		else lmbpsNormal = new JLabel("Mbps");
		lmbpsNormal.setBounds(x,y,40,20);
		panel.add(lmbpsNormal);
	}
	private void addMaxRateExponential(JLayeredPane panel,int x,int y){
		JLabel lmaxrateExponential = new JLabel("Maximal Rate");
		lmaxrateExponential.setBounds(x,y,100,20);
		panel.add(lmaxrateExponential);

		x += 125;
		tmaxrateExponential = new JTextField();
		tmaxrateExponential.setHorizontalAlignment(JTextField.RIGHT);
		tmaxrateExponential.setBounds(x,y,50,20);
		panel.add(tmaxrateExponential);

		x += 55;
		JLabel lmbpsExponential = new JLabel();
		if(project.getNoC().getType().equals("HermesG"))
		{
			if(router != null)
			{
				int i=0;
				for(;i<project.getNoC().getClock().size();i++)
				{
					if(project.getNoC().getClock().get(i).getNumberRouter().equals(router.getAddress()))
					break;
				}
				if(project.getNoC().getClock().get(i).getUnitIpInput().equals("Mhz")) lmbpsExponential = new JLabel("Mbps");
				else lmbpsExponential = new JLabel("Kbps");
			}
			else
			{
				int i=0,b=0,c=0;
				for(;i<project.getNoC().getClock().size();i++)
				{
					if(project.getNoC().getClock().get(i).getUnitIpInput().equals("Mhz")) b = 1;
					else c = 1;
				}
				if(b == 1 && c == 1) lmbpsExponential = new JLabel("Mbps/Kbps");
				else if(b == 1) lmbpsExponential = new JLabel("Mbps");
				else if(c == 1) lmbpsExponential = new JLabel("Kbps");
			}
		}
		else lmbpsExponential = new JLabel("Mbps");
		lmbpsExponential.setBounds(x,y,40,20);
		panel.add(lmbpsExponential);
	}

	private void addIncRateNormal(JLayeredPane panel,int x,int y){
		JLabel lincrateNormal = new JLabel("Increment");
		lincrateNormal.setBounds(x,y,100,20);
		panel.add(lincrateNormal);

		x += 125;
		tincrateNormal = new JTextField();
		tincrateNormal.setHorizontalAlignment(JTextField.RIGHT);
		tincrateNormal.setBounds(x,y,50,20);
		panel.add(tincrateNormal);

		x += 55;
		JLabel lmbpsNormal = new JLabel();
		if(project.getNoC().getType().equals("HermesG"))
		{
			if(router != null)
			{
				int i=0;
				for(;i<project.getNoC().getClock().size();i++)
				{
					if(project.getNoC().getClock().get(i).getNumberRouter().equals(router.getAddress()))
					break;
				}
				if(project.getNoC().getClock().get(i).getUnitIpInput().equals("Mhz")) lmbpsNormal = new JLabel("Mbps");
				else lmbpsNormal = new JLabel("Kbps");
			}
			else
			{
				int i=0,b=0,c=0;
				for(;i<project.getNoC().getClock().size();i++)
				{
					if(project.getNoC().getClock().get(i).getUnitIpInput().equals("Mhz")) b = 1;
					else c = 1;
				}
				if(b == 1 && c == 1) lmbpsNormal = new JLabel("Mbps/Kbps");
				else if(b == 1) lmbpsNormal = new JLabel("Mbps");
				else if(c == 1) lmbpsNormal = new JLabel("Kbps");
			}
		}
		else lmbpsNormal = new JLabel("Mbps");
		lmbpsNormal.setBounds(x,y,40,20);
		panel.add(lmbpsNormal);
	}

	private void addIncRateExponential(JLayeredPane panel,int x,int y){
		JLabel lincrateExponential = new JLabel("Increment");
		lincrateExponential.setBounds(x,y,100,20);
		panel.add(lincrateExponential);

		x += 125;
		tincrateExponential = new JTextField();
		tincrateExponential.setHorizontalAlignment(JTextField.RIGHT);
		tincrateExponential.setBounds(x,y,50,20);
		panel.add(tincrateExponential);

		x += 55;
		JLabel lmbpsExponential = new JLabel();
		if(project.getNoC().getType().equals("HermesG"))
		{
			if(router != null)
			{
				int i=0;
				for(;i<project.getNoC().getClock().size();i++)
				{
					if(project.getNoC().getClock().get(i).getNumberRouter().equals(router.getAddress()))
					break;
				}
				if(project.getNoC().getClock().get(i).getUnitIpInput().equals("Mhz")) lmbpsExponential = new JLabel("Mbps");
				else lmbpsExponential = new JLabel("Kbps");
			}
			else
			{
				int i=0,b=0,c=0;
				for(;i<project.getNoC().getClock().size();i++)
				{
					if(project.getNoC().getClock().get(i).getUnitIpInput().equals("Mhz")) b = 1;
					else c = 1;
				}
				if(b == 1 && c == 1) lmbpsExponential = new JLabel("Mbps/Kbps");
				else if(b == 1) lmbpsExponential = new JLabel("Mbps");
				else if(c == 1) lmbpsExponential = new JLabel("Kbps");
			}
		}
		else lmbpsExponential = new JLabel("Mbps");
		lmbpsExponential.setBounds(x,y,40,20);
		panel.add(lmbpsExponential);
	}
	
	private void addAvgRateNormal(JLayeredPane panel,int x,int y){
		JLabel lavgrateNormal = new JLabel("Average Rate");
		lavgrateNormal.setBounds(x,y,100,20);
		panel.add(lavgrateNormal);

		x += 125;
		tavgrateNormal = new JTextField();
		tavgrateNormal.setHorizontalAlignment(JTextField.RIGHT);
		tavgrateNormal.setBounds(x,y,50,20);
		panel.add(tavgrateNormal);

		x += 55;
		JLabel lmbpsNormal = new JLabel();
		if(project.getNoC().getType().equals("HermesG"))
		{
			if(router != null)
			{
				int i=0;
				for(;i<project.getNoC().getClock().size();i++)
				{
					if(project.getNoC().getClock().get(i).getNumberRouter().equals(router.getAddress()))
					break;
				}
				if(project.getNoC().getClock().get(i).getUnitIpInput().equals("Mhz")) lmbpsNormal = new JLabel("Mbps");
				else lmbpsNormal = new JLabel("Kbps");
			}
			else
			{
				int i=0,b=0,c=0;
				for(;i<project.getNoC().getClock().size();i++)
				{
					if(project.getNoC().getClock().get(i).getUnitIpInput().equals("Mhz")) b = 1;
					else c = 1;
				}
				if(b == 1 && c == 1) lmbpsNormal = new JLabel("Mbps/Kbps");
				else if(b == 1) lmbpsNormal = new JLabel("Mbps");
				else if(c == 1) lmbpsNormal = new JLabel("Kbps");
			}
		}
		else lmbpsNormal = new JLabel("Mbps");
		lmbpsNormal.setBounds(x,y,40,20);
		panel.add(lmbpsNormal);
	}

	private void addAvgRateExponential(JLayeredPane panel,int x,int y){
		JLabel lavgrateExponential = new JLabel("Average Rate");
		lavgrateExponential.setBounds(x,y,100,20);
		panel.add(lavgrateExponential);

		x += 125;
		tavgrateExponential = new JTextField();
		tavgrateExponential.setHorizontalAlignment(JTextField.RIGHT);
		tavgrateExponential.setBounds(x,y,50,20);
		panel.add(tavgrateExponential);

		x += 55;
		JLabel lmbpsExponential = new JLabel();
		if(project.getNoC().getType().equals("HermesG"))
		{
			if(router != null)
			{
				int i=0;
				for(;i<project.getNoC().getClock().size();i++)
				{
					if(project.getNoC().getClock().get(i).getNumberRouter().equals(router.getAddress()))
					break;
				}
				if(project.getNoC().getClock().get(i).getUnitIpInput().equals("Mhz")) lmbpsExponential = new JLabel("Mbps");
				else lmbpsExponential = new JLabel("Kbps");
			}
			else
			{
				int i=0,b=0,c=0;
				for(;i<project.getNoC().getClock().size();i++)
				{
					if(project.getNoC().getClock().get(i).getUnitIpInput().equals("Mhz")) b = 1;
					else c = 1;
				}
				if(b == 1 && c == 1) lmbpsExponential = new JLabel("Mbps/Kbps");
				else if(b == 1) lmbpsExponential = new JLabel("Mbps");
				else if(c == 1) lmbpsExponential = new JLabel("Kbps");
			}
		}
		else lmbpsExponential = new JLabel("Mbps");
		lmbpsExponential.setBounds(x,y,40,20);

		panel.add(lmbpsExponential);
	}
	
	private void addDvRate(JLayeredPane panel,int x,int y){
		JLabel ldvrate = new JLabel("Standard Deviation");
		ldvrate.setBounds(x,y,110,20);
		panel.add(ldvrate);

		x += 125;
		tdvrate = new JTextField();
		tdvrate.setHorizontalAlignment(JTextField.RIGHT);
		tdvrate.setBounds(x,y,50,20);
		panel.add(tdvrate);

		x += 55;
		JLabel lmbps = new JLabel();
		if(project.getNoC().getType().equals("HermesG"))
		{
			if(router != null)
			{
				int i=0;
				for(;i<project.getNoC().getClock().size();i++)
				{
					if(project.getNoC().getClock().get(i).getNumberRouter().equals(router.getAddress()))
					break;
				}
					if(project.getNoC().getClock().get(i).getUnitIpInput().equals("Mhz")) lmbps = new JLabel("Mbps");
					else lmbps = new JLabel("Kbps");
			}
			else
			{
				int i=0,b=0,c=0;
				for(;i<project.getNoC().getClock().size();i++)
				{
					if(project.getNoC().getClock().get(i).getUnitIpInput().equals("Mhz")) b = 1;
					else c = 1;
				}
				if(b == 1 && c == 1) lmbps = new JLabel("Mbps/Kbps");
				else if(b == 1) lmbps = new JLabel("Mbps");
				else if(c == 1) lmbps = new JLabel("Kbps");
			}
		}
		else lmbps = new JLabel("Mbps");
		lmbps.setBounds(x,y,40,20);
		panel.add(lmbps);
	}

	private void addGraphNormal(JLayeredPane panel,int x,int y){
		graphNormal = new JButton("Graph");
		graphNormal.setBounds(x,y,85,25);
		graphNormal.addActionListener(this);
		panel.add(graphNormal);
	}

	private void addGraphExponential(JLayeredPane panel,int x,int y){
		graphExponential = new JButton("Graph");
		graphExponential.setBounds(x,y,85,25);
		graphExponential.addActionListener(this);
		panel.add(graphExponential);
	}


	private void addRateOn(JLayeredPane panel,int x,int y){
		JLabel lrateOn = new JLabel("Rate of On Period");
		lrateOn.setBounds(x,y,100,20);
		panel.add(lrateOn);

		x += 125;
		trateOn = new JTextField();
		trateOn.setHorizontalAlignment(JTextField.RIGHT);
		trateOn.setBounds(x,y,50,20);
		panel.add(trateOn);

		x += 55;
		JLabel lmbps = new JLabel();
		if(project.getNoC().getType().equals("HermesG"))
		{
			if(router != null)
			{
				int i=0;
				for(;i<project.getNoC().getClock().size();i++)
				{
					if(project.getNoC().getClock().get(i).getNumberRouter().equals(router.getAddress()))
					break;
				}
					if(project.getNoC().getClock().get(i).getUnitIpInput().equals("Mhz")) lmbps = new JLabel("Mbps");
					else lmbps = new JLabel("Kbps");
			}
			else
			{
				int i=0,b=0,c=0;
				for(;i<project.getNoC().getClock().size();i++)
				{
					if(project.getNoC().getClock().get(i).getUnitIpInput().equals("Mhz")) b = 1;
					else c = 1;
				}
				if(b == 1 && c == 1) lmbps = new JLabel("Mbps/Kbps");
				else if(b == 1) lmbps = new JLabel("Mbps");
				else if(c == 1) lmbps = new JLabel("Kbps");
				
			}
		}
		else lmbps = new JLabel("Mbps");
		lmbps.setBounds(x,y,40,20);
		panel.add(lmbps);
	}

	private void addBurst(JLayeredPane panel,int x,int y){
		JLabel lburst = new JLabel("Number of Bursts");
		lburst.setBounds(x,y,100,20);
		panel.add(lburst);

		x += 125;
		tburst= new JTextField();
		tburst.setHorizontalAlignment(JTextField.RIGHT);
		tburst.setBounds(x,y,50,20);
		panel.add(tburst);
	}

	private void addOk(int x,int y){
		ok= new JButton("Ok");
		ok.setBounds(x,y,60,25);
		ok.addActionListener(this);
		getContentPane().add(ok);
	}

	/**
	* show the GUI with the traffic parameters.
	*/
	private void update(){
		if(router!=null){
			setLocation(router.getInitialX()+15,router.getInitialY());
		}
		if(!project.getNoC().getType().equals("HermesG")) tfrequency.setText(""+traffic.getFrequency());
		cbtarget.setSelectedItem(traffic.getTarget());
		tnumberPackets.setText(""+traffic.getNumberOfPackets());
		tpacketSize.setText(""+(traffic.getPacketSize()));
		if(scheduling.equalsIgnoreCase("Priority"))
			cbpriority.setSelectedItem(""+traffic.getPriority());
		cbdist.setSelectedItem(traffic.getDistribution());
		if(router!=null)
		{
			if(project.getNoC().getType().equals("HermesG"))
			{
				int i;
				for(i=0;i<project.getNoC().getClock().size();i++)
				{
					if(project.getNoC().getClock().get(i).getNumberRouter().equals(router.getAddress()))
					break;
				}
				if(traffic.getUniformRate() == 0.0) trate.setText(""+(project.getNoC().getClock().get(i).getClockIpInput() * project.getNoC().getFlitSize()));
				else trate.setText(""+traffic.getUniformRate());
			}	
			else trate.setText(""+traffic.getUniformRate());
		}
		tminrateNormal.setText(""+traffic.getNormalMinimalRate());
		tmaxrateNormal.setText(""+traffic.getNormalMaximalRate());
		tavgrateNormal.setText(""+traffic.getNormalAverageRate());
		tdvrate.setText(""+traffic.getNormalStandardDeviation());
		tincrateNormal.setText(""+traffic.getNormalIncrement());
		
		
		tminrateExponential.setText(""+traffic.getExponentialMinimalRate());
		tmaxrateExponential.setText(""+traffic.getExponentialMaximalRate());
		tavgrateExponential.setText(""+traffic.getExponentialAverageRate());
		tincrateExponential.setText(""+traffic.getExponentialIncrement());
		
		trateOn.setText(""+traffic.getParetoRateOnPeriod());
		tburst.setText(""+traffic.getParetoBurstSize());
	}

	/**
	* Verify if the selected traffic is adapted to NoC dimension.
	*/
	private boolean verifyTarget(){
		if(getTarget().equalsIgnoreCase("bitReversal") || getTarget().equalsIgnoreCase("butterfly") || getTarget().equalsIgnoreCase("complemento") || getTarget().equalsIgnoreCase("perfectShuffle")){
			return (dimXNet%2==0 && dimXNet == dimYNet);
		}
		return true;
	}

	private String getTarget(){return (String)cbtarget.getSelectedItem();}

	/**
	* Verify the parameters and configure the traffic.
	*/
	private void configTraffic(){
		if(!verifyTarget()){
			JOptionPane.showMessageDialog(this,"It is not possible to use this "+getTarget()+" standard destination generation\nbecause of the selected network dimension.","Error Message",JOptionPane.ERROR_MESSAGE);
			cbtarget.setSelectedItem("random");
		}
		else{
			String distTime = (String)cbdist.getSelectedItem();
			boolean ok;
			if(distTime.equalsIgnoreCase("uniform"))
				ok = configUniformTraffic();
			else if(distTime.equalsIgnoreCase("normal"))
			{
				ok = configNormalTraffic();
				
			}
			else if(distTime.equalsIgnoreCase("exponential"))
			{
				
				ok = configExponentialTraffic();
			}
			else
				ok = configParetoTraffic();
			if(ok)
				dispose();
		}
	}

	/**
	* Configure the parameters of Exponential traffic distribution
	* @return True if traffic has been configured
	*/
	private boolean configExponentialTraffic()
	{
		try
		{
			int i=0;
			double frequency;
			if(project.getNoC().getType().equals("HermesG") && router != null)
			{
				for(;i<project.getNoC().getClock().size();i++)
				{
					if(project.getNoC().getClock().get(i).getNumberRouter().equals(router.getAddress()))
					break;
				}
				frequency = project.getNoC().getClock().get(i).getClockIpInput();
			}
			else if(project.getNoC().getType().equals("HermesG") && router == null)
			{
				double cont=0;
				for(;i<project.getNoC().getClock().size();i++)
				{
					if(cont < project.getNoC().getClock().get(i).getClockIpInput())
					{
						cont = project.getNoC().getClock().get(i).getClockIpInput();
					} 
				}
				frequency = cont;
			}
			else frequency = Double.parseDouble(tfrequency.getText());
			try
			{
				int numberPackets = Integer.valueOf(tnumberPackets.getText()).intValue();
				try
				{
					int packetSize = Integer.valueOf(tpacketSize.getText()).intValue();
					try
					{
						double avgRate = Double.valueOf(tavgrateExponential.getText()).doubleValue();
						try
						{
							double minRate = Double.valueOf(tminrateExponential.getText()).doubleValue();
							try
							{
								double maxRate = Double.valueOf(tmaxrateExponential.getText()).doubleValue();
								try
								{
									double incRate = Double.valueOf(tincrateExponential.getText()).doubleValue();
									/* Tratamento de excessão quando a frequencia informada for negativa */
									if(frequency <= 0 && !project.getNoC().getType().equals("HermesG"))
									{
										JOptionPane.showMessageDialog(this,"Informed frequency or is zero or less than zero.","Error Message",JOptionPane.ERROR_MESSAGE);
									}
									/* Tratamento de excessão quando o numero de pacotes informado for igual ou menor que zero */
									else if(numberPackets <= 0)
									{
										JOptionPane.showMessageDialog(this,"Informed number of packets or is zero or less than zero.","Error Message",JOptionPane.ERROR_MESSAGE);
									}
									/* Tratamento de excessão quando o tamanho dos pacotes for menor que 15 na rede HERMES */
									else if(packetSize<13 && !project.getNoC().getType().equals("HermesG"))
									{
										JOptionPane.showMessageDialog(this,"The minimum number of packet size in flits to the router HERMES should be 13 flits.","Error Message",JOptionPane.ERROR_MESSAGE);
									}
                                    /* Tratamento de excessão quando o tamanho dos pacotes for menor que 16 na rede HERMESG */
                                    else if(project.getNoC().getType().equals("HermesG") && packetSize < 13) JOptionPane.showMessageDialog(this,"The minimum number of packet size in flits to the router HERMESG should be 13 flits.","Error Message",JOptionPane.ERROR_MESSAGE);		
									/* Tratamento de excessão quando a taxa minima for maior que (frequency * flit length ) */
									else if(minRate > (frequency * project.getNoC().getFlitSize()) || minRate <=0)
									{
										JOptionPane.showMessageDialog(this,"Informed minimun rate or is zero or less than zero or greater than maxium Ip rate.","Error Message",JOptionPane.ERROR_MESSAGE);		
									}
									/* Tratamento de excessão quando a taxa maxima for maior que (frequency * flit length ) */
									else if(maxRate > (frequency * project.getNoC().getFlitSize()) || minRate <=0)
									{
										JOptionPane.showMessageDialog(this,"Informed maximum rate or is zero or less than zero or greater than maxium Ip rate.","Error Message",JOptionPane.ERROR_MESSAGE);		
									}
									else if(avgRate <=0 || avgRate<minRate || avgRate>maxRate)
									{
										JOptionPane.showMessageDialog(this,"Informed average rate or is zero or less than zero or less than minimum or greater than maximum Rate.","Error Message",JOptionPane.ERROR_MESSAGE);	
									}
									/* Tratamento de excessão quando a taxa maxima for menor que a taxa minima*/
									else if(maxRate<minRate)
									{
										JOptionPane.showMessageDialog(this,"Informed maximum rate is smaller than minimal rate.","Error Message",JOptionPane.ERROR_MESSAGE);
									}
									/* Tratamento de excessão quando o incremento for zero ou negativo */
									else if(incRate <= 0)
									{
										JOptionPane.showMessageDialog(this,"Informed increment or is zero or less than zero.","Error Message",JOptionPane.ERROR_MESSAGE);
									}
									else									
									{
										int priority = 0;
										if(scheduling.equalsIgnoreCase("Priority"))
											priority = Integer.parseInt(""+cbpriority.getSelectedItem());
											
											traffic.setDistribution((String)cbdist.getSelectedItem());
											if(project.getNoC().getType().equals("HermesG") && router == null) frequency = -1;
											traffic.setFrequency(frequency);
											traffic.setTarget((String)cbtarget.getSelectedItem());
											traffic.setPriority(priority);
											traffic.setNumberOfPackets(numberPackets);
											traffic.setPacketSize(packetSize);
											traffic.setExponentialAverageRate(avgRate);
											traffic.setExponentialMinimalRate(minRate);
											traffic.setExponentialMaximalRate(maxRate);
											traffic.setExponentialIncrement(incRate);
											if(router==null)
											{
												
												scenery.setStandardConfigToRouters(project.isMapCores());
											}
											return true;
										}
									}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed rate increment.","Error Message",JOptionPane.ERROR_MESSAGE);}
							}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed maximal rate.","Error Message",JOptionPane.ERROR_MESSAGE);}
						}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed minimal rate.","Error Message",JOptionPane.ERROR_MESSAGE);}
					}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed average rate.","Error Message",JOptionPane.ERROR_MESSAGE);}
				}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed packet size.","Error Message",JOptionPane.ERROR_MESSAGE);}
			}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed number of packets.","Error Message",JOptionPane.ERROR_MESSAGE);}
		}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed frequency.","Error Message",JOptionPane.ERROR_MESSAGE);}
		return false;
	}
	
	/**
	* Configure the parameters of Uniform traffic distribution
	* @return True if traffic has been configured
	*/
	private boolean configUniformTraffic(){
		try{
			double rate = Double.valueOf(trate.getText()).doubleValue();
			try
			{
				int i=0;
				double frequency;
				if(project.getNoC().getType().equals("HermesG") && router != null)
				{
					for(;i<project.getNoC().getClock().size();i++)
					{
						if(project.getNoC().getClock().get(i).getNumberRouter().equals(router.getAddress()))
						break;
					}
					frequency = project.getNoC().getClock().get(i).getClockIpInput();
				}
				else if(project.getNoC().getType().equals("HermesG") && router == null)
				{
					double cont=0;
					for(;i<project.getNoC().getClock().size();i++)
					{
						if(cont < project.getNoC().getClock().get(i).getClockIpInput())
						{
							cont = project.getNoC().getClock().get(i).getClockIpInput();
						} 
					}
					frequency = cont;
				}
				else frequency = Double.parseDouble(tfrequency.getText());
				try
				{
					int numberPackets = Integer.valueOf(tnumberPackets.getText()).intValue();
					int packetSize = Integer.valueOf(tpacketSize.getText()).intValue();
					/* Tratamento de excessão quando a frequencia informada for negativa */
					if(frequency <= 0 && !project.getNoC().getType().equals("HermesG"))
					{
						JOptionPane.showMessageDialog(this,"Informed frequency or is zero or less than zero.","Error Message",JOptionPane.ERROR_MESSAGE);
					}
					/* Tratamento de excessão quando o numero de pacotes informado for igual ou menor que zero */
					else if(numberPackets <= 0)
					{
						JOptionPane.showMessageDialog(this,"Informed number of packets or is zero or less than zero.","Error Message",JOptionPane.ERROR_MESSAGE);
					}
                    /* Tratamento de excessão quando o tamanho dos pacotes for menor que 16 na rede HERMESG */
                    else if(project.getNoC().getType().equals("HermesG") && packetSize < 13) JOptionPane.showMessageDialog(this,"The minimum number of packet size in flits to the router HERMESG should be 13 flits.","Error Message",JOptionPane.ERROR_MESSAGE);		
                    /* Tratamento de excessão quando o rate for igual ou menor que zero ou maior que a taxa do Ip */
					else if(rate < 0 || rate > (frequency * project.getNoC().getFlitSize()))
					{
						JOptionPane.showMessageDialog(this,"Informed rate is less than zero or greater than maxium Ip rate.","Error Message",JOptionPane.ERROR_MESSAGE);		
					}
					else{
						int priority = 0;
						if(scheduling.equalsIgnoreCase("Priority"))
							priority = Integer.parseInt(""+cbpriority.getSelectedItem());
						traffic.setDistribution((String)cbdist.getSelectedItem());
						if(project.getNoC().getType().equals("HermesG") && router == null) frequency = -1;
						traffic.setFrequency(frequency);
						traffic.setTarget((String)cbtarget.getSelectedItem());
						traffic.setPriority(priority);
						traffic.setNumberOfPackets(numberPackets);
						traffic.setPacketSize(packetSize);
						traffic.setUniformRate(rate);
						if(router==null){
							scenery.setStandardConfigToRouters(project.isMapCores());
						}
						return true;
					}
				}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed packet size.","Error Message",JOptionPane.ERROR_MESSAGE);}
			}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed frequency.","Error Message",JOptionPane.ERROR_MESSAGE);}
		}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed rate.","Error Message",JOptionPane.ERROR_MESSAGE);}
		return false;
	}

	/**
	* Configure the parameters of Normal traffic distribution
	* @return True if traffic has been configured
	*/
	private boolean configNormalTraffic(){
		try
		{
			int i=0;
			double frequency;
			if(project.getNoC().getType().equals("HermesG") && router != null)
			{
				for(;i<project.getNoC().getClock().size();i++)
				{
					if(project.getNoC().getClock().get(i).getNumberRouter().equals(router.getAddress()))
					break;
				}
				frequency = project.getNoC().getClock().get(i).getClockIpInput();
			}
			else if(project.getNoC().getType().equals("HermesG") && router == null)
			{
				double cont=0;
				for(;i<project.getNoC().getClock().size();i++)
				{
					if(cont < project.getNoC().getClock().get(i).getClockIpInput())
					{
						cont = project.getNoC().getClock().get(i).getClockIpInput();
					} 
				}
				frequency = cont;
			}
			else frequency = Double.parseDouble(tfrequency.getText());
			try{
				int numberPackets = Integer.valueOf(tnumberPackets.getText()).intValue();
				try{
					int packetSize = Integer.valueOf(tpacketSize.getText()).intValue();
					try{
						double avgRate = Double.valueOf(tavgrateNormal.getText()).doubleValue();
						try{
							double minRate = Double.valueOf(tminrateNormal.getText()).doubleValue();
							try{
								double maxRate = Double.valueOf(tmaxrateNormal.getText()).doubleValue();
								try{
									double dvRate = Double.valueOf(tdvrate.getText()).doubleValue();
									try{
										double incRate = Double.valueOf(tincrateNormal.getText()).doubleValue();
										/* Tratamento de excessão quando a frequencia informada for negativa */
										if(frequency <= 0 && !project.getNoC().getType().equals("HermesG"))
										{
											JOptionPane.showMessageDialog(this,"Informed frequency or is zero or less than zero.","Error Message",JOptionPane.ERROR_MESSAGE);
										}
										/* Tratamento de excessão quando o numero de pacotes informado for igual ou menor que zero */
										else if(numberPackets <= 0)
										{
											JOptionPane.showMessageDialog(this,"Informed number of packets or is zero or less than zero.","Error Message",JOptionPane.ERROR_MESSAGE);
										}
										/* Tratamento de excessão quando o tamanho dos pacotes for menor que 15 na rede HERMES */
										else if(packetSize<13 && !project.getNoC().getType().equals("HermesG"))
										{
											JOptionPane.showMessageDialog(this,"The minimum number of packet size in flits to the router HERMES should be 13 flits.","Error Message",JOptionPane.ERROR_MESSAGE);
										}
                                        /* Tratamento de excessão quando o tamanho dos pacotes for menor que 16 na rede HERMESG */
                                        else if(project.getNoC().getType().equals("HermesG") && packetSize < 13) JOptionPane.showMessageDialog(this,"The minimum number of packet size in flits to the router HERMESG should be 13 flits.","Error Message",JOptionPane.ERROR_MESSAGE);		
                                        /* Tratamento de excessão quando a taxa minima for maior que (frequency * flit length ) */
										else if(minRate > (frequency * project.getNoC().getFlitSize()) || minRate <=0)
										{
											JOptionPane.showMessageDialog(this,"Informed minimun rate or is zero or less than zero or greater than maxium Ip rate.","Error Message",JOptionPane.ERROR_MESSAGE);		
										}
										/* Tratamento de excessão quando a taxa maxima for maior que (frequency * flit length ) */
										else if(maxRate > (frequency * project.getNoC().getFlitSize()) || minRate <=0)
										{
											JOptionPane.showMessageDialog(this,"Informed maximum rate or is zero or less than zero or greater than maxium Ip rate.","Error Message",JOptionPane.ERROR_MESSAGE);		
										}
										else if(avgRate <=0 || avgRate<minRate || avgRate>maxRate)
										{
											JOptionPane.showMessageDialog(this,"Informed average rate or is zero or less than zero or less than minimum or greater than maximum Rate.","Error Message",JOptionPane.ERROR_MESSAGE);	
										}
										/* Tratamento de excessão quando a taxa maxima for menor que a taxa minima */
										else if(maxRate<minRate)
										{
											JOptionPane.showMessageDialog(this,"Informed maximum rate is smaller than minimal rate.","Error Message",JOptionPane.ERROR_MESSAGE);
										}
										/* Tratamento de excessão quando o desvio padrão for menor ou igual a zero, maior que a taxa maxima ou taxa do IP */
										else if(dvRate <= 0 || dvRate > maxRate  || dvRate > (frequency * project.getNoC().getFlitSize()))
										{
											JOptionPane.showMessageDialog(this,"Informed standard deviation rate or is zero or less than zero or is greater than maximum rate or is greater than maximum Ip Rate.","Error Message",JOptionPane.ERROR_MESSAGE);
										}
										/* Tratamento de excessão quando o incremento for zero ou negativo */
										else if(incRate <= 0)
										{
											JOptionPane.showMessageDialog(this,"Informed increment or is zero or less than zero.","Error Message",JOptionPane.ERROR_MESSAGE);
										}
										else{
											int priority = 0;
											if(scheduling.equalsIgnoreCase("Priority"))
												priority = Integer.parseInt(""+cbpriority.getSelectedItem());
											
											traffic.setDistribution((String)cbdist.getSelectedItem());
											if(project.getNoC().getType().equals("HermesG") && router == null) frequency = -1;
											traffic.setFrequency(frequency);
											traffic.setTarget((String)cbtarget.getSelectedItem());
											traffic.setPriority(priority);
											traffic.setNumberOfPackets(numberPackets);
											traffic.setPacketSize(packetSize);
											traffic.setNormalAverageRate(avgRate);
											traffic.setNormalMinimalRate(minRate);
											traffic.setNormalMaximalRate(maxRate);
											traffic.setNormalStandardDeviation(dvRate);
											traffic.setNormalIncrement(incRate);
											
											if(router==null)
											{
												scenery.setStandardConfigToRouters(project.isMapCores());
											}
											return true;
										}
									}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed rate increment.","Error Message",JOptionPane.ERROR_MESSAGE);}
								}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed rate standart deviation .","Error Message",JOptionPane.ERROR_MESSAGE);}
							}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed maximal rate.","Error Message",JOptionPane.ERROR_MESSAGE);}
						}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed minimal rate.","Error Message",JOptionPane.ERROR_MESSAGE);}
					}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed average rate.","Error Message",JOptionPane.ERROR_MESSAGE);}
				}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed packet size.","Error Message",JOptionPane.ERROR_MESSAGE);}
			}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed number of packets.","Error Message",JOptionPane.ERROR_MESSAGE);}
		}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed frequency.","Error Message",JOptionPane.ERROR_MESSAGE);}
		return false;
	}

	/**
	* Configure the parameters of Pareto traffic distribution.
	* @return True if traffic has been configured.
	*/
	private boolean configParetoTraffic(){
		try{
			Double.valueOf(trate.getText()).doubleValue();
			try
			{
				int i=0;
				double frequency;
				if(project.getNoC().getType().equals("HermesG") && router != null)
				{
					for(;i<project.getNoC().getClock().size();i++)
					{
						if(project.getNoC().getClock().get(i).getNumberRouter().equals(router.getAddress()))
						break;
					}
					frequency = project.getNoC().getClock().get(i).getClockIpInput();
				}
				else if(project.getNoC().getType().equals("HermesG") && router == null)
				{
					double cont=0;
					for(;i<project.getNoC().getClock().size();i++)
					{
						if(cont < project.getNoC().getClock().get(i).getClockIpInput())
						{
							cont = project.getNoC().getClock().get(i).getClockIpInput();
						} 
					}
					frequency = cont;
				}
				else frequency = Double.parseDouble(tfrequency.getText());
				try{
					int numberPackets = Integer.valueOf(tnumberPackets.getText()).intValue();
					try{
						int packetSize = Integer.valueOf(tpacketSize.getText()).intValue();
						try{
							double rateOn = Double.valueOf(trateOn.getText()).doubleValue();
							try{
								int burstSize = Integer.valueOf(tburst.getText()).intValue();
								/* Tratamento de excessão quando a frequencia informada for negativa */
								if(frequency <= 0 && !project.getNoC().getType().equals("HermesG"))
								{
									JOptionPane.showMessageDialog(this,"Informed frequency or is zero or less than zero.","Error Message",JOptionPane.ERROR_MESSAGE);
								}
								/* Tratamento de excessão quando o numero de pacotes informado for igual ou menor que zero */
								else if(numberPackets <= 0)
								{
									JOptionPane.showMessageDialog(this,"Informed number of packets or is zero or less than zero.","Error Message",JOptionPane.ERROR_MESSAGE);
								}
                                /* Tratamento de excessão quando o tamanho dos pacotes for menor que 16 na rede HERMESG */
                                else if(project.getNoC().getType().equals("HermesG") && packetSize < 13) JOptionPane.showMessageDialog(this,"The minimum number of packet size in flits to the router HERMESG should be 13 flits.","Error Message",JOptionPane.ERROR_MESSAGE);		
                                /* Tratamento de excessão quando o rate for igual ou menor que zero ou maior que a taxa do Ip */
								else if(rateOn < 0 || rateOn > (frequency * project.getNoC().getFlitSize()))
								{
									JOptionPane.showMessageDialog(this,"Informed rate or is less than zero or greater than maxium Ip rate.","Error Message",JOptionPane.ERROR_MESSAGE);		
								}
								/* Tratamento de excessão quando o burst size for igual ou menor que zero */
								else if(burstSize <= 0)
								{
									JOptionPane.showMessageDialog(this,"Informed burst size or is zero or less than zero.","Error Message",JOptionPane.ERROR_MESSAGE);		
								}	
								else{
									int priority = 0;
									if(scheduling.equalsIgnoreCase("Priority"))
										priority = Integer.parseInt(""+cbpriority.getSelectedItem());

									traffic.setDistribution((String)cbdist.getSelectedItem());
									if(project.getNoC().getType().equals("HermesG") && router == null) frequency = -1;
									traffic.setFrequency(frequency);
									traffic.setTarget((String)cbtarget.getSelectedItem());
									traffic.setPriority(priority);
									traffic.setNumberOfPackets(numberPackets);
									traffic.setPacketSize(packetSize);
									traffic.setParetoRateOnPeriod(rateOn);
									traffic.setParetoBurstSize(burstSize);
									
									if(router==null){
										scenery.setStandardConfigToRouters(project.isMapCores());
									}

									return true;
								}
							}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed burst size.","Error Message",JOptionPane.ERROR_MESSAGE);}
						}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed rate in the on period.","Error Message",JOptionPane.ERROR_MESSAGE);}
					}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed packet size.","Error Message",JOptionPane.ERROR_MESSAGE);}
				}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed number of packets.","Error Message",JOptionPane.ERROR_MESSAGE);}
			}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed frequency.","Error Message",JOptionPane.ERROR_MESSAGE);}
		}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed rate.","Error Message",JOptionPane.ERROR_MESSAGE);}
		return false;
	}

	/**
	 * Execute an action associated to the selected event
	 */
	public void actionPerformed(ActionEvent e){
		if(e.getSource()==ok)
			configTraffic();
		else if(e.getSource()==cbdist){
			if(((String)cbdist.getSelectedItem()).equalsIgnoreCase("uniform")){
				panelUniform.setVisible(true);
				panelNormal.setVisible(false);
				panelPareto.setVisible(false);
				panelExponential.setVisible(false);
			}
			else if(((String)cbdist.getSelectedItem()).equalsIgnoreCase("normal")){
				panelUniform.setVisible(false);
				panelNormal.setVisible(true);
				panelPareto.setVisible(false);
				panelExponential.setVisible(false);
			}
			else if(((String)cbdist.getSelectedItem()).equalsIgnoreCase("paretoOn/Off")){
				panelUniform.setVisible(false);
				panelNormal.setVisible(false);
				panelPareto.setVisible(true);
				panelExponential.setVisible(false);
			}
			else if(((String)cbdist.getSelectedItem()).equalsIgnoreCase("exponential")){
				panelUniform.setVisible(false);
				panelNormal.setVisible(false);
				panelPareto.setVisible(false);
				panelExponential.setVisible(true);
			}
		}
		else if(e.getSource()== graphNormal)
		{		
			if(configNormalTraffic() == true)
			{
				if(router==null)
				{
					TimeDistribution timeDistribution = new TimeDistribution(traffic, flitWidth,flitClockCycles);
					timeDistribution.normalGraph("normalStandard");
					Default.showGraph("normalStandard.txt");
				}
				else
				{ //router traffic configuration
					TimeDistribution timeDistribution = new TimeDistribution(traffic, flitWidth,flitClockCycles);
					timeDistribution.normalGraph("normal" + router.getAddress());
					Default.showGraph("normal" + router.getAddress() + ".txt");
				}				
			}
		}
		else if(e.getSource()== graphExponential)
		{
				if(configExponentialTraffic() == true)
				{
					if(router==null) //router traffic configuration
					{
						TimeDistribution timeDistribution = new TimeDistribution(traffic, flitWidth,flitClockCycles);
						timeDistribution.exponentialGraph("exponentialStandard");
						Default.showGraph("exponentialStandard.txt");
					}
					else
					{ 
						TimeDistribution timeDistribution = new TimeDistribution(traffic, flitWidth,flitClockCycles);
						timeDistribution.exponentialGraph("exponential" + router.getAddress());
						Default.showGraph("exponential" + router.getAddress() + ".txt");
					}
				}
		}
	}
}
