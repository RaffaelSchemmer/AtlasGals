package TrafficMbps;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

import AtlasPackage.Project;
import AtlasPackage.Scenery;
import AtlasPackage.RouterTraffic;
import AtlasPackage.Router;
import AtlasPackage.SR4Traffic;

/**
 * This class shows the GUI allowing configure a SR4 router traffic or the SR4 standard traffic.
 * @author Edson Ifarraguirre Moreno
 * @version
 */
public class InterfaceConfigSRCV extends JFrame implements ActionListener{

	private Project project;
	private Router specificRouter;
	private Scenery scenery;
	private JLayeredPane panelUniform;
	private JTextField tfrequency;

	private JTextField tcnpacket, tgnpacket, tbnpacket;
	private JTextField tcpktSize, tgpktSize, tbpktSize;
	private JTextField tcrate, tgrate, tbrate;

	private JComboBox cbtarget,cbdist;
	private JButton ok;
	private int dimXNet,dimYNet;

	private static final int CTRL = SR4Traffic.CTRL, GS = SR4Traffic.GS, BE = SR4Traffic.BE;

	/**
	* Create the GUI allowing configure a standard traffic.
	* @param project The NoC project.
	*/
	public InterfaceConfigSRCV(Project project){
		super("Standard Configuration");
		this.project = project;
		this.dimXNet = project.getNoC().getNumRotX();
		this.dimYNet = project.getNoC().getNumRotY();
		specificRouter=null;
		scenery = project.getScenery();
		addProperties();
		addComponents();
		update();
		setVisible(true);
 	}

	/**
	* Create the GUI allowing configure a router traffic.
	* @param _proj The NoC project.
	* @param _ip The router which the traffic to be configured.
	*/
	public InterfaceConfigSRCV(Project _proj, Router _ip){
		super("Router ");
		this.project = _proj;
		this.dimXNet = _proj.getNoC().getNumRotX();
		this.dimYNet = _proj.getNoC().getNumRotY();
		specificRouter=_ip;
		scenery = project.getScenery();
		addProperties();
		addComponents();
		update();
		setVisible(true);
 	}

	private void addProperties(){
		getContentPane().setLayout(null);
		Dimension resolucao=Toolkit.getDefaultToolkit().getScreenSize();
		setSize(480,320);
		setLocation((resolucao.width-480)/2,(resolucao.height-320)/2);
		setResizable(false);
		addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){dispose();}});
 	}

	private void addComponents(){
		int x = 10;
		int y = 10;
		int stepY = 25;
		
		addFrequency(x+50,y);
		
		y = y + stepY;
		addTarget(x+50,y);
		
		y = y + stepY;
		addDistTime(x+50,y);
		
		y = y + stepY;
		addPanelUniform(7,y,460,170);
		
		y = y + 175;
		addOk(x+180,y);
	}

	private void addFrequency(int x,int y){
		JLabel lfrequency = new JLabel("Frequency");
		lfrequency.setBounds(x,y,70,20);
		getContentPane().add(lfrequency);

		x += 115;
		tfrequency = new JTextField();
		tfrequency.setHorizontalAlignment(JTextField.RIGHT);
		tfrequency.setBounds(x,y,50,20);
		getContentPane().add(tfrequency);

		x += 55;
		JLabel lmhz = new JLabel("MHz");
		lmhz.setBounds(x,y,40,20);
		getContentPane().add(lmhz);
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
		//if(dimYNet%2==0 && dimXNet%2==0)
		//	cbtarget.addItem("complemento");
		//cbtarget.addItem("bitReversal");
		//cbtarget.addItem("butterfly");
		//cbtarget.addItem("matrixTranspose");
		//cbtarget.addItem("perfectShuffle");
		for(int j=0;j<dimYNet;j++){
			for(int i=0;i<dimXNet;i++){
				if((dimXNet>9 || dimYNet>9) && i<10 && j<10) cbtarget.addItem("0"+i+"0"+j);
				else if((dimXNet>9 || dimYNet>9) && i<10) cbtarget.addItem("0"+i+j);
				else if((dimXNet>9 || dimYNet>9) && j<10) cbtarget.addItem(""+i+"0"+j);
				else cbtarget.addItem(""+i+j);
			}
		}
	}

	private void addDistTime(int x,int y){
		JLabel ldist = new JLabel("Distribution");
		ldist.setBounds(x,y,100,20);
		getContentPane().add(ldist);

		x += 115;
		cbdist= new JComboBox();
		cbdist.setBounds(x,y,120,20);
		cbdist.addItem("uniform");
		cbdist.setEnabled(false);
		//cbdist.addItem("normal");
		//cbdist.addItem("paretoOn/Off");
		getContentPane().add(cbdist);
		cbdist.addActionListener(this);
	}

	private void addPanelUniform(int x,int y,int dimx,int dimy){
		//Panel Border
		panelUniform=new JLayeredPane();
		panelUniform.setBounds(x,y,dimx,dimy);
		panelUniform.setBorder(BorderFactory.createTitledBorder(BorderFactory.createRaisedBevelBorder(),"Uniform Distribution",TitledBorder.CENTER,TitledBorder.TOP));
		getContentPane().add(panelUniform);
		
		addCtrlpkt(panelUniform,10,20);
		addGSpkt(panelUniform,10,70);
		addBEpkt(panelUniform,10,120);		
	}
	
	private void addCtrlpkt(JLayeredPane panel,int x,int y){
	  // LABEL DO TRouterO DE TRAFEGO
		JLabel lc = new JLabel("- Control Message -");
		lc.setBounds(x,y,150,20);
		panel.add(lc);

	  // LABEL NUMERO DE PACOTES
		y += 20;
		JLabel lnpacket = new JLabel("# Packets:");
		lnpacket.setBounds(x,y,70,20);
		panel.add(lnpacket);

		x += 65;
		tcnpacket = new JTextField();
		tcnpacket.setHorizontalAlignment(JTextField.RIGHT);
		tcnpacket.setBounds(x,y,50,20);
		panel.add(tcnpacket);

	  // LABEL TAMANHO DO PACOTE
		x += 60;
		JLabel lpcktSize = new JLabel("Packet Size(Flits):");
		lpcktSize.setBounds(x,y,120,20);
		panel.add(lpcktSize);

		x += 105;		
		tcpktSize = new JTextField();
		tcpktSize.setHorizontalAlignment(JTextField.RIGHT);
		tcpktSize.setBounds(x,y,40,20);
		panel.add(tcpktSize);

	  // LABEL DA TAXA DE TRANSMISSAO
		x += 45;
		JLabel lrate = new JLabel("Injection rate(Mbps):");
		lrate.setBounds(x,y,120,20);
		panel.add(lrate);

		x += 120;		
		tcrate = new JTextField();
		tcrate.setHorizontalAlignment(JTextField.RIGHT);
		tcrate.setBounds(x,y,40,20);
		panel.add(tcrate);
	}
	
	private void addGSpkt(JLayeredPane panel,int x,int y){
	  // LABEL DO TRouterO DE TRAFEGO
		JLabel lc = new JLabel("- GS Message -");
		lc.setBounds(x,y,150,20);
		panel.add(lc);

	  // LABEL NUMERO DE PACOTES
		y += 20;
		JLabel lcnpacket = new JLabel("# Packets:");
		lcnpacket.setBounds(x,y,70,20);
		panel.add(lcnpacket);

		x += 65;
		tgnpacket = new JTextField();
		tgnpacket.setHorizontalAlignment(JTextField.RIGHT);
		tgnpacket.setBounds(x,y,50,20);
		panel.add(tgnpacket);

	  // LABEL TAMANHO DO PACOTE
		x += 60;
		JLabel lpcktSize = new JLabel("Packet Size(Flits):");
		lpcktSize.setBounds(x,y,120,20);
		panel.add(lpcktSize);

		x += 105;		
		tgpktSize = new JTextField();
		tgpktSize.setHorizontalAlignment(JTextField.RIGHT);
		tgpktSize.setBounds(x,y,40,20);
		panel.add(tgpktSize);

	  // LABEL DA TAXA DE TRANSMISSAO
		x += 45;
		JLabel lrate = new JLabel("Injection rate(Mbps):");
		lrate.setBounds(x,y,120,20);
		panel.add(lrate);

		x += 120;		
		tgrate = new JTextField();
		tgrate.setHorizontalAlignment(JTextField.RIGHT);
		tgrate.setBounds(x,y,40,20);
		panel.add(tgrate);
	}
	
	private void addBEpkt(JLayeredPane panel,int x,int y){
	  // LABEL DO TRouterO DE TRAFEGO
		JLabel lc = new JLabel("- BE Message -");
		lc.setBounds(x,y,150,20);
		panel.add(lc);

	  // LABEL NUMERO DE PACOTES
		y += 20;
		JLabel lnpacket = new JLabel("# Packets:");
		lnpacket.setBounds(x,y,70,20);
		panel.add(lnpacket);

		x += 65;
		tbnpacket = new JTextField();
		tbnpacket.setHorizontalAlignment(JTextField.RIGHT);
		tbnpacket.setBounds(x,y,50,20);
		panel.add(tbnpacket);

	  // LABEL TAMANHO DO PACOTE
		x += 60;
		JLabel lpcktSize = new JLabel("Packet Size(Flits):");
		lpcktSize.setBounds(x,y,120,20);
		panel.add(lpcktSize);

		x += 105;		
		tbpktSize = new JTextField();
		tbpktSize.setHorizontalAlignment(JTextField.RIGHT);
		tbpktSize.setBounds(x,y,40,20);
		panel.add(tbpktSize);

	  // LABEL DA TAXA DE TRANSMISSAO
	  // LABEL DA TAXA DE TRANSMISSAO
		x += 45;
		JLabel lrate = new JLabel("Injection rate(Mbps):");
		lrate.setBounds(x,y,120,20);
		panel.add(lrate);

		x += 120;		
		tbrate = new JTextField();
		tbrate.setHorizontalAlignment(JTextField.RIGHT);
		tbrate.setBounds(x,y,40,20);
		panel.add(tbrate);
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
		if(specificRouter==null){
			RouterTraffic standard = scenery.getStandardTraffic();
			
			tfrequency.setText(""+standard.getFrequency());
			cbtarget.setSelectedItem(standard.getTarget());		

			tcnpacket.setText(""+standard.getNumberOfPackets(CTRL));
			tgnpacket.setText(""+standard.getNumberOfPackets(GS));
			tbnpacket.setText(""+standard.getNumberOfPackets(BE));

			tcpktSize.setText(""+standard.getPacketSize(CTRL));
			tgpktSize.setText(""+standard.getPacketSize(GS));
			tbpktSize.setText(""+standard.getPacketSize(BE));

			tcrate.setText(""+standard.getUniformRate(CTRL));
			tgrate.setText(""+standard.getUniformRate(GS));
			tbrate.setText(""+standard.getUniformRate(BE));
		}
		else{
			RouterTraffic router = specificRouter.getTraffic();

			tfrequency.setText(""+router.getFrequency());
			cbtarget.setSelectedItem(router.getTarget());		

			tcnpacket.setText(""+router.getNumberOfPackets(CTRL));
			tgnpacket.setText(""+router.getNumberOfPackets(GS));
			tbnpacket.setText(""+router.getNumberOfPackets(BE));

			tcpktSize.setText(""+router.getPacketSize(CTRL));
			tgpktSize.setText(""+router.getPacketSize(GS));
			tbpktSize.setText(""+router.getPacketSize(BE));

			tcrate.setText(""+router.getUniformRate(CTRL));
			tgrate.setText(""+router.getUniformRate(GS));
			tbrate.setText(""+router.getUniformRate(BE));
	
			//Remove traffic when target and source Router are the same
			if(router.getTarget().equalsIgnoreCase(specificRouter.getAddress())){
				cbtarget.setSelectedItem("random");
				tcrate.setText("0");
				tgrate.setText("0");
				tbrate.setText("0");
			}
		}
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
			if(distTime.equalsIgnoreCase("uniform")){
				if(configUniformTraffic())
					dispose();
			}
		}
	}

	/**
	* Configure the parameters of Uniform traffic distribution.
	* @return True if traffic has been configured.
	*/
	private boolean configUniformTraffic(){

		try{
			float frequency=Float.valueOf(tfrequency.getText()).floatValue();
			try{
				int ctrl_nrPkt = Integer.valueOf(tcnpacket.getText()).intValue();
				int gs_nrPkt = Integer.valueOf(tgnpacket.getText()).intValue();
				int be_nrPkt = Integer.valueOf(tbnpacket.getText()).intValue();
				try{
					int ctrl_pktSize = Integer.valueOf(tcpktSize.getText()).intValue();
					int gs_pktSize = Integer.valueOf(tgpktSize.getText()).intValue();
					int be_pktSize = Integer.valueOf(tbpktSize.getText()).intValue();
					try{					
						double ctrl_rate = Double.valueOf(tcrate.getText()).doubleValue();
						double gs_rate = Double.valueOf(tgrate.getText()).doubleValue();
						double be_rate = Double.valueOf(tbrate.getText()).doubleValue();

						if((ctrl_rate!=0 || be_rate!=0 || gs_rate!=0) && frequency==0)
							JOptionPane.showMessageDialog(this,"Error in the informed frequency.","Error Message",JOptionPane.ERROR_MESSAGE);
						else if(ctrl_rate!=0 && ctrl_nrPkt==0)
							JOptionPane.showMessageDialog(this,"Error in the informed number of packets for Control messages.","Error Message",JOptionPane.ERROR_MESSAGE);
						else if(gs_rate!=0 && gs_nrPkt==0)
							JOptionPane.showMessageDialog(this,"Error in the informed number of packets for GS messages.","Error Message",JOptionPane.ERROR_MESSAGE);
						else if(be_rate!=0 && be_nrPkt==0)
							JOptionPane.showMessageDialog(this,"Error in the informed number of packets for BE messages.","Error Message",JOptionPane.ERROR_MESSAGE);
						else if(ctrl_rate!=0 && ctrl_pktSize<15)
							JOptionPane.showMessageDialog(this,"Error in the informed packet size for control messages.","Error Message",JOptionPane.ERROR_MESSAGE);
						else if(gs_rate!=0 && gs_pktSize<15)
							JOptionPane.showMessageDialog(this,"Error in the informed packet size for GS messages.","Error Message",JOptionPane.ERROR_MESSAGE);
						else if(be_rate!=0 && be_pktSize<15)
							JOptionPane.showMessageDialog(this,"Error in the informed packet size for BE messages.","Error Message",JOptionPane.ERROR_MESSAGE);
						else{
							if(specificRouter==null){
								RouterTraffic standard = scenery.getStandardTraffic();
								standard.setDistribution(""+cbdist.getSelectedItem());
								standard.setFrequency(frequency);
								standard.setTarget(""+cbtarget.getSelectedItem());
								standard.setPriority(0);
								standard.setNumberOfPackets(CTRL, ctrl_nrPkt);
								standard.setNumberOfPackets(GS, gs_nrPkt);
								standard.setNumberOfPackets(BE, be_nrPkt);								
								standard.setPacketSize(CTRL, ctrl_pktSize);
								standard.setPacketSize(GS, gs_pktSize);
								standard.setPacketSize(BE, be_pktSize);
								standard.setUniformRate(CTRL, ctrl_rate);
								standard.setUniformRate(GS, gs_rate);
								standard.setUniformRate(BE, be_rate);
								
								scenery.setStandardConfigToRouters(project.isMapCores());
							}
							else{
								RouterTraffic router = specificRouter.getTraffic();
								router.setDistribution(""+cbdist.getSelectedItem());
								router.setFrequency(frequency);
								router.setTarget(""+cbtarget.getSelectedItem());
								router.setPriority(0);
								router.setNumberOfPackets(CTRL, ctrl_nrPkt);
								router.setNumberOfPackets(GS, gs_nrPkt);
								router.setNumberOfPackets(BE, be_nrPkt);								
								router.setPacketSize(CTRL, ctrl_pktSize);
								router.setPacketSize(GS, gs_pktSize);
								router.setPacketSize(BE, be_pktSize);
								router.setUniformRate(CTRL, ctrl_rate);
								router.setUniformRate(GS, gs_rate);
								router.setUniformRate(BE, be_rate);
							}
							return true;
						}
					}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed packet injection rate.","Error Message",JOptionPane.ERROR_MESSAGE);}
				}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed packet size.","Error Message",JOptionPane.ERROR_MESSAGE);}
			}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed number of packets.","Error Message",JOptionPane.ERROR_MESSAGE);}
		}catch(NumberFormatException exc){JOptionPane.showMessageDialog(this,"Error in the informed frequency.","Error Message",JOptionPane.ERROR_MESSAGE);}
		return false;
	}

	/**
	 * Execute an action associated to the selected event
	 */
	public void actionPerformed(ActionEvent e){
		if(e.getSource()==ok)
			configTraffic();
	}
}
