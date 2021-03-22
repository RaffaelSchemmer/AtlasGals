package AtlasPackage;

import java.awt.event.*;
import javax.swing.*;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import AtlasPackage.*;
import java.util.*;

/**
 * 
 * @author Aline Vieira de Mello
 *
 */
public class NoCGenerationGUI extends JFrame{
	private JComboBox flowControl, virtualChannel, scheduling, addaclock,addacode;
	private JComboBox dimensionX, dimensionY, flitWidth, bufferDepth, routingAlgorithm, crcType;
	private JCheckBox saboteur, drSaboteur, dfSaboteur, ngSaboteur, pgSaboteur;
	private JTextField rclockdef,ipclockdef;
	private JCheckBox testBenchSC;
	private JButton bGenerate,addclockbutton,removeclockbutton,editClockButton;
	private JPanel_Noc panelNoC;
	private NoC noc;
	/**
	 * Constructor class.
	 * @param title The JFrame title.
	 */
	public NoCGenerationGUI(String title){
		super();
		setTitle(title);
		getContentPane().setLayout(null);
		setSize(800,650);
		Dimension resolucao=Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((resolucao.width-800)/2,(resolucao.height-650)/2);
		setResizable(false);
		addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){dispose();}});
	}

	/**
	 * Add the menu bar containing the Help menu.
	 * @param aboutTitle The title of about menuItem.
	 * @param actionListener Where the menu action will be treated.
	 */
	public void addMenu(String aboutTitle, ActionListener actionListener){
		JMenuItem helpTopics=new JMenuItem("Documentation");
		helpTopics.addActionListener(actionListener);
		JMenuItem aboutMaia=new JMenuItem(aboutTitle);
		aboutMaia.addActionListener(actionListener);

		JMenu help=new JMenu("Help");
		help.add(helpTopics);
		help.add(aboutMaia);

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(help);
		setJMenuBar(menuBar);
	}

	/**
	 * Add the component allowing show the GAPH icon.
	 * @param x Horizontal initial position of the GAPH icon.
	 * @param y Vertical initial position of the GAPH icon.
	 * @param dimx The Horizontal dimension of the GAPH icon.
	 * @param dimy The vertical dimension of the GAPH icon.
	 */
	public void addGaphIcon(int x,int y,int dimx,int dimy){
		String gaphImage = System.getenv("ATLAS_HOME")+File.separator+"Images"+File.separator+"logo-gaph.gif";
		String gaphWeb = "http://www.inf.pucrs.br/~gaph";
		JPanelImage jpanel=new JPanelImage(gaphImage,gaphWeb);
		jpanel.setToolTipText("Double Click to Visit the Developer homepage.");
		jpanel.setBounds(x,y,dimx,dimy);
		jpanel.repaint();
		getContentPane().add(jpanel);
	}

	/**
	 * Add the component allowing show the FACIN icon.
	 * @param x Horizontal initial position of the FACIN icon.
	 * @param y Vertical initial position of the FACIN icon.
	 * @param dimx The Horizontal dimension of the FACIN icon.
	 * @param dimy The vertical dimension of the FACIN icon.
	 */
	public void addFacinIcon(int x, int y, int dimx, int dimy){
		String facinImage = System.getenv("ATLAS_HOME")+File.separator+"Images"+File.separator+"logo-facin.gif";
		String facinWeb = "http://www.pucrs.br/inf";
		JPanelImage jpanel=new JPanelImage(facinImage,facinWeb);
		jpanel.setToolTipText("Double Click to Visit the FACIN homepage.");
		jpanel.setBounds(x,y,dimx,dimy);
		getContentPane().add(jpanel);
	}
	
	/**
	 * Add the components allowing select the flow control option.
	 * @param x Horizontal initial position of the flow control panel border. 
	 * @param y Vertical initial position of the flow control panel border.
	 * @param dimx The Horizontal dimension of the flow control panel border.
	 * @param dimy The vertical dimension of the flow control panel border.
	 * @param availableFlowControl The list of flow control possibilities.
	 * @param selectedFlowControl The flow control selected in the list of possibilities.
	 * @param actionListener Where the action listener will be treated.
	 */
	public void addFlowControl(int x,int y,int dimx,int dimy, String[] availableFlowControl, String selectedFlowControl, ActionListener actionListener){

		//flow control panel Border
		JLayeredPane pbFlowControl=new JLayeredPane();
		pbFlowControl.setBounds(x,y,dimx,dimy);
		pbFlowControl.setBorder(BorderFactory.createTitledBorder("Flow Control"));
		getContentPane().add(pbFlowControl);

		// create JComboBox with Flow Control choices
		flowControl = new JComboBox(availableFlowControl);
		flowControl.setBounds(x+20,y+22,dimx-40,20);
		flowControl.setSelectedItem(selectedFlowControl);
		flowControl.addActionListener(actionListener);
		flowControl.setToolTipText("Select the flow control.");
		getContentPane().add(flowControl);
	}

	/**
	 * Add the components allowing select the number of virtual channels.
	 * @param x Horizontal initial position of the virtual channels panel border. 
	 * @param y Vertical initial position of the virtual channels panel border.
	 * @param dimx The Horizontal dimension of the virtual channels panel border.
	 * @param dimy The vertical dimension of the virtual channels panel border.
	 * @param availableVC The list of number of virtual channels.
	 * @param selectedVC The number of virtual channels selected in the list.
	 * @param actionListener Where the action listener will be treated.
	 */
	public void addVirtualChannel(int x,int y,int dimx,int dimy, String[] availableVC, String selectedVC, ActionListener actionListener){

		//virtual channel panel border
		JLayeredPane pbVirtualChannel=new JLayeredPane();
		pbVirtualChannel.setBounds(x,y,dimx,dimy);
		pbVirtualChannel.setBorder(BorderFactory.createTitledBorder("Virtual Channel"));
		getContentPane().add(pbVirtualChannel);

		// create combo box with Virtual Channel choices
		virtualChannel = new JComboBox(availableVC);
		virtualChannel.setBounds(x+20,y+22,dimx-40,20);
		virtualChannel.setSelectedItem(selectedVC);
		virtualChannel.addActionListener(actionListener);
		virtualChannel.setToolTipText("Select the number of virtual channels.");
		getContentPane().add(virtualChannel);
	}

	/**
	 * Add the components allowing select the scheduling.
	 * @param x Horizontal initial position of the scheduling panel border. 
	 * @param y Vertical initial position of the scheduling panel border.
	 * @param dimx The Horizontal dimension of the scheduling panel border.
	 * @param dimy The vertical dimension of the scheduling panel border.
	 * @param availableScheduling The list of scheduling possibilities.
	 * @param selectedScheduling The scheduling selected in the list.
	 * @param actionListener Where the action listener will be treated.
	 */
	public void addScheduling(int x,int y,int dimx,int dimy, String[] availableScheduling, String selectedScheduling, ActionListener actionListener){
		//Scheduling panel border
		JLayeredPane pbScheduling=new JLayeredPane();
		pbScheduling.setBounds(x,y,dimx,dimy);
		pbScheduling.setBorder(BorderFactory.createTitledBorder("Scheduling"));
		getContentPane().add(pbScheduling);

		// create JComboBox with Scheduling choices
		scheduling= new JComboBox(availableScheduling);
		scheduling.setBounds(x+20,y+22,dimx-40,20);
		scheduling.setSelectedItem(selectedScheduling);
		scheduling.addActionListener(actionListener);
		scheduling.setToolTipText("Select the HERMES scheduling.");
		getContentPane().add(scheduling);
	}
	
	/**
	 * Add the components allowing select the NoC dimensions.
	 * @param x Horizontal initial position of the NoC dimensions panel border. 
	 * @param y Vertical initial position of the NoC dimensions panel border.
	 * @param dimx The Horizontal dimension of the NoC dimensions panel border.
	 * @param dimy The vertical dimension of the NoC dimensions panel border.
	 * @param dimension The list of dimension possibilities.
	 * @param dimXNoC The X-dimension selected in the list.
	 * @param dimYNoC The Y-dimension selected in the list.
	 * @param actionListener Where the action listener will be treated.
	 */
	public void addDimensions(int x,int y,int dimx,int dimy, String[] dimension, String dimXNoC, String dimYNoC, ActionListener actionListener){
		//NoC dimension panel border
		JLayeredPane pbDimensions=new JLayeredPane();
		pbDimensions.setBounds(x,y,dimx,dimy);
		pbDimensions.setBorder(BorderFactory.createTitledBorder("Dimensions"));
		getContentPane().add(pbDimensions);

		// create JComboBox with X dimensions
		dimensionX = new JComboBox(dimension);
		dimensionX.setBounds(x+20,y+22,50,20);
		dimensionX.setSelectedItem(dimXNoC);
		dimensionX.addActionListener(actionListener);
		dimensionX.setToolTipText("Select the X dimension.");
		getContentPane().add(dimensionX);

		JLabel labelX = new JLabel("x");
		labelX.setBounds(x+75,y+22,15,20);
		getContentPane().add(labelX);

		// create JComboBox with Y dimensions
		dimensionY = new JComboBox(dimension);
		dimensionY.setBounds(x+90,y+22,50,20);
		dimensionY.setSelectedItem(dimYNoC);
		dimensionY.addActionListener(actionListener);
		dimensionY.setToolTipText("Select the Y dimension.");
		getContentPane().add(dimensionY);
	}
	
	/**
	 * Add the components allowing select the flit width.
	 * @param x Horizontal initial position of the flit width panel border. 
	 * @param y Vertical initial position of the flit width panel border.
	 * @param dimx The Horizontal dimension of the flit width panel border.
	 * @param dimy The vertical dimension of the flit width panel border.
	 * @param availableFlitWidth The list of flit width possibilities.
	 * @param selectedFlitWidth The flit width selected in the list.
	 * @param actionListener Where the action listener will be treated.
	 */
	public void addFlitWidth(int x,int y,int dimx,int dimy, String[] availableFlitWidth, String selectedFlitWidth, ActionListener actionListener){
		//Flit width panel border
		JLayeredPane pbFlitWidth=new JLayeredPane();
		pbFlitWidth.setBounds(x,y,dimx,dimy);
		pbFlitWidth.setBorder(BorderFactory.createTitledBorder("Flit Width"));
		getContentPane().add(pbFlitWidth);

		// create JComboBox with flit width possibilities
		flitWidth = new JComboBox(availableFlitWidth);
		flitWidth.setBounds(x+20,y+22,dimx-40,20);
		flitWidth.setSelectedItem(selectedFlitWidth);
		flitWidth.addActionListener(actionListener);
		flitWidth.setToolTipText("Select the number of bits of a flit.");
		getContentPane().add(flitWidth);
	}

	/**
	 * Add the components allowing select the buffer depth.
	 * @param x Horizontal initial position of the buffer depth panel border. 
	 * @param y Vertical initial position of the buffer depth panel border.
	 * @param dimx The Horizontal dimension of the buffer depth panel border.
	 * @param dimy The vertical dimension of the buffer depth panel border.
	 * @param availableDepth The list of buffer depth possibilities.
	 * @param selectedDepth The buffer depth selected in the list.
	 */
	public void addBuffer(int x,int y,int dimx,int dimy, String[] availableDepth, String selectedDepth){
		//Buffer depth panel border
		JLayeredPane pbBuffer=new JLayeredPane();
		pbBuffer.setBounds(x,y,dimx,dimy);
		pbBuffer.setBorder(BorderFactory.createTitledBorder("Buffer Depth"));
		getContentPane().add(pbBuffer);

		// create JComboBox with buffer depth possibilities
		bufferDepth = new JComboBox(availableDepth);
		bufferDepth.setBounds(x+20,y+22,dimx-40,20);
		bufferDepth.setSelectedItem(selectedDepth);
		bufferDepth.setToolTipText("Select the buffers depth.");
		getContentPane().add(bufferDepth);
	}
	
	/**
	 * Add the components allowing select the routing algorithm.
	 * @param x Horizontal initial position of the routing algorithm panel border. 
	 * @param y Vertical initial position of the routing algorithm panel border.
	 * @param dimx The Horizontal dimension of the routing algorithm panel border.
	 * @param dimy The vertical dimension of the routing algorithm panel border.
	 * @param availableAlgorithm The list of routing algorithm possibilities.
	 * @param selectedAlgorithm The routing algorithm selected in the list.
	 */
	public void addRoutingAlgorithm(int x,int y,int dimx,int dimy, String[] availableAlgorithm, String selectedAlgorithm){
		//Routing algorithm panel border.
		JLayeredPane pbAlgorithm=new JLayeredPane();
		pbAlgorithm.setBounds(x,y,dimx,dimy);
		pbAlgorithm.setBorder(BorderFactory.createTitledBorder("Routing Algorithm"));
		getContentPane().add(pbAlgorithm);

		// create JComboBox with routing algorithm possibilities
		routingAlgorithm = new JComboBox(availableAlgorithm);
		routingAlgorithm.setBounds(x+20,y+22,dimx-40,20);
		routingAlgorithm.setSelectedItem(selectedAlgorithm);
		routingAlgorithm.setToolTipText("Select the routing algorithm.");
		getContentPane().add(routingAlgorithm);
	}
	
	/**
	 * Add the components allowing select the CRC type.
	 * @param x Horizontal initial position of the CRC type panel border. 
	 * @param y Vertical initial position of the CRC type panel border.
	 * @param dimx The Horizontal dimension of the CRC type panel border.
	 * @param dimy The vertical dimension of the CRC type panel border.
	 * @param availableCrcType The list of CRC type possibilities.
	 * @param selectedType The CRC type selected in the list.
	 * @param actionListener Where the action listener will be treated.
	 */
	public void addCrcType(int x,int y, int dimx, int dimy, String[] availableCrcType, String selectedType, ActionListener actionListener){
		//CRC type panel border
		JLayeredPane pbCrcType=new JLayeredPane();
		pbCrcType.setBounds(x,y,dimx,dimy);
		pbCrcType.setBorder(BorderFactory.createTitledBorder("CRC Type"));
		getContentPane().add(pbCrcType);

		// create JComboBox with CRC type
		crcType = new JComboBox(availableCrcType);
		crcType.setBounds(x+15,y+22,dimx-30,20);
		crcType.setSelectedItem(selectedType);
		crcType.addActionListener(actionListener);
		crcType.setToolTipText("Select the CRC Type.");
		getContentPane().add(crcType);
	}
	
	/**
	 * Add the components allowing select the saboteur.
	 * @param x Horizontal initial position of the saboteur panel border. 
	 * @param y Vertical initial position of the saboteur panel border.
	 * @param dimx The Horizontal dimension of the saboteur panel border.
	 * @param dimy The vertical dimension of the saboteur panel border.
	 * @param isSelected Determine if option saboteur is active.
	 * @param actionListener Where the JCheckBox action will be treated.
	 */
	public void addSaboteur(int x, int y, int dimx, int dimy, boolean isSelected, ActionListener actionListener){
		//Saboteur panel border
		JLayeredPane pbSabot=new JLayeredPane();
		pbSabot.setBounds(x,y,dimx,dimy);
		pbSabot.setBorder(BorderFactory.createTitledBorder("Saboteur"));
		getContentPane().add(pbSabot);

		// create JCheckBox with the saboteur option
		saboteur = new JCheckBox("Saboteur",isSelected);
		saboteur.setBounds(x+20,y+22,dimx-30,20);
		saboteur.addActionListener(actionListener);
		saboteur.setToolTipText("Set the Saboteur.");
		getContentPane().add(saboteur);
	}

	
	/**
	 * Add the components allowing select the saboteur types.
	 * @param x Horizontal initial position of the saboteur types panel border. 
	 * @param y Vertical initial position of the saboteur types panel border.
	 * @param dimx The Horizontal dimension of the saboteur types panel border.
	 * @param dimy The vertical dimension of the saboteur types panel border.
	 * @param isDR Determine if option DR saboteur is active.
	 * @param isDF Determine if option DF saboteur is active.
	 * @param isPG Determine if option PG saboteur is active.
	 * @param isNG Determine if option NG saboteur is active.
	 * @param actionListener Where the JCheckBox action will be treated.
	 */
	public void addSaboteurType(int x, int y, int dimx, int dimy, 
			boolean isDR, boolean isDF, boolean isNG, boolean isPG, 
			ActionListener actionListener){
		
		//Saboteur type panel border
		JLayeredPane pbSabot=new JLayeredPane();
		pbSabot.setBounds(x,y,dimx,dimy);
		pbSabot.setBorder(BorderFactory.createTitledBorder("Types"));
		getContentPane().add(pbSabot);

		drSaboteur = new JCheckBox("DR",isDR);
		drSaboteur.setBounds(x+15,y+22,dimx-40,20);
		drSaboteur.addActionListener(actionListener);
		drSaboteur.setToolTipText("Delay in a rising transition.");
		getContentPane().add(drSaboteur);
		
		dfSaboteur = new JCheckBox("DF",isDF);
		dfSaboteur.setBounds(x+15,y+42,dimx-40,20);
		dfSaboteur.addActionListener(actionListener);
		dfSaboteur.setToolTipText("Delay in a falling transition.");
		getContentPane().add(dfSaboteur);
		
		ngSaboteur = new JCheckBox("NG",isNG);
		ngSaboteur.setBounds(x+15,y+62,dimx-40,20);
		ngSaboteur.addActionListener(actionListener);
		ngSaboteur.setToolTipText("Negative glitch.");
		getContentPane().add(ngSaboteur);
		
		pgSaboteur = new JCheckBox("PG",isPG);
		pgSaboteur.setBounds(x+15,y+82,dimx-40,20);
		pgSaboteur.addActionListener(actionListener);
		pgSaboteur.setToolTipText("Positive glitch.");
		getContentPane().add(pgSaboteur);
	}
	
	/**
	 * Add the components allowing select the SystemC test bench.
	 * @param x Horizontal initial position of the SystemC test bench panel  border. 
	 * @param y Vertical initial position of the SystemC test bench panel border.
	 * @param dimx The Horizontal dimension of the SystemC test bench panel border.
	 * @param dimy The vertical dimension of the SystemC test bench panel border.
	 * @param isSelected Determine if option is active.
	 * @param actionListener Where the JCheckBox action will be treated.
	 */
	public void addSCTestBench(int x,int y,int dimx,int dimy, boolean isSelected, ActionListener actionListener){
		//SC test bench panel border
		JLayeredPane pbTeste=new JLayeredPane();
		pbTeste.setBounds(x,y,dimx,dimy);
		pbTeste.setBorder(BorderFactory.createTitledBorder("Testbench"));
		getContentPane().add(pbTeste);

		// create JCheckBox with the SystemC test bench option
		testBenchSC = new JCheckBox("SystemC", isSelected);
		testBenchSC.setBounds(x+20,y+22,dimx-40,20);
		testBenchSC.addActionListener(actionListener);
		testBenchSC.setToolTipText("Select the SystemC test bench.");
		getContentPane().add(testBenchSC);
	}
	
	public void addClockButton(int x,int y,int dimx,int dimy, ActionListener actionListener)
	{
		addclockbutton = new JButton("Add");
		addclockbutton.setToolTipText("Adding a clock for IP or router");
		addclockbutton.setBounds(x+10,y,dimx-20,dimy-14);
		addclockbutton.addActionListener(actionListener);
		getContentPane().add(addclockbutton);
	}

	public void removeClockButton(int x, int y, int dimx, int dimy, ActionListener actionListener)
	{
		removeclockbutton = new JButton("Remove");
		removeclockbutton.setToolTipText("Remove a registered clock");
		removeclockbutton.setBounds(x+10,y,dimx-20,dimy-14);
		removeclockbutton.addActionListener(actionListener);
		getContentPane().add(removeclockbutton);
	}
	public void editClockButton(int x, int y, int dimx, int dimy, ActionListener actionListener)
	{
		editClockButton = new JButton("Edit");
		editClockButton.setToolTipText("Edit a registered clock");
		editClockButton.setBounds(x+10,y,dimx-20,dimy-14);
		editClockButton.addActionListener(actionListener);
		getContentPane().add(editClockButton);
	}
	public void selectCodeButton(int x, int y, int dimx, int dimy, String[] codes, String code)
	{
		JLayeredPane addCode=new JLayeredPane();
		addCode.setBounds(x,y,dimx,dimy+40);
		addCode.setBorder(BorderFactory.createTitledBorder("Async Buffer Pointer Encoding"));
		
		getContentPane().add(addCode);
		addacode = new JComboBox(codes);
		addacode.setSelectedItem(code);
		addacode.setBounds(x+10,y+22,dimx-20,20);
		addacode.setToolTipText("List of Available Encodings");
		getContentPane().add(addacode);
	}
	public void addAvaibleClock(int x,int y,int dimx,int dimy, ArrayList<String> clocks)
	{
		JLayeredPane addClock=new JLayeredPane();
		addClock.setBounds(x,y,dimx,dimy+95);
		addClock.setBorder(BorderFactory.createTitledBorder("Clock Panel"));
		getContentPane().add(addClock);
		
		addaclock = new JComboBox(clocks.toArray());
		addaclock.setBounds(x+10,y+22,dimx-20,20);
		addaclock.setToolTipText("List of Available Clocks");
		getContentPane().add(addaclock);
	}
	
	public void setAvaibleClocks(ArrayList<AvailableClock> clocks)
	{
		addaclock.removeAllItems();
		for(int i=0;i<clocks.size();i++) addaclock.addItem(clocks.get(i).getAllAvailableValue());
	}	
		
	/**
	 * Add the button allowing generate a NoC with the selected parameters.
	 * @param x Horizontal initial position of the generate button. 
	 * @param y Vertical initial position of the generate button.
	 * @param dimx The Horizontal dimension of the generate button.
	 * @param dimy The vertical dimension of the generate button.
	 * @param actionListener Where the button action will be treated.
	 */
	public void addGenerateButton(int x,int y,int dimx,int dimy, ActionListener actionListener){
		bGenerate=new JButton("Generate");
		bGenerate.setToolTipText("Generates a custom NOC.");
		bGenerate.setBounds(x,y,dimx,dimy);
		bGenerate.addActionListener(actionListener);
		getContentPane().add(bGenerate);
	}
	
	
	/**
	 * Add the panel allowing show the NoC topology.
	 * @param x Horizontal initial position of the panel. 
	 * @param y Vertical initial position of the panel.
	 * @param dimx The Horizontal dimension of the panel.
	 * @param dimy The vertical dimension of the panel.
	 * @param nocType The NoC type defines the NoC topology. For instance: The <b>Hermes</b> NoC type has Mesh 2D topology.
	 */
	public void addNoCPanel(int x,int y,int dimx ,int dimy, String nocType){
		panelNoC = new JPanel_Noc(x, y, dimx, dimy, nocType);
		getContentPane().add(panelNoC);
		panelNoC.setNoCDimension(getDimXSelected(),getDimYSelected());
	}

	/**
	 * Return the flow control object.
	 * @return The flow control JComboBox.
	 */
	public JComboBox getFlowControl() {return flowControl;}

	/**
	 * Return the flow control selected.
	 * @return The flow control selected in the GUI.
	 */
	public String getFlowControlSelected() {return (String)flowControl.getSelectedItem();}

	/**
	 * Return the virtual channels object.
	 * @return The virtual channels JComboBox.
	 */
	public JComboBox getVirtualChannel() {return virtualChannel;}

	/**
	 * Return the number of virtual channels selected.
	 * @return The number of virtual channels selected in the GUI.
	 */
	public String getVirtualChannelSelected() {return (String)virtualChannel.getSelectedItem();}

	/**
	 * Return the scheduling object.
	 * @return The scheduling JComboBox.
	 */
	public JComboBox getScheduling() {return scheduling;}

	/**
	 * Return the scheduling selected.
	 * @return The scheduling selected in the GUI.
	 */
	public String getSchedulingSelected() {return (String)scheduling.getSelectedItem();}

	/**
	 * Return the X-dimension object.
	 * @return The X-dimension JComboBox.
	 */
	public JComboBox getDimX(){return dimensionX;}

	/**
	 * Return the X-dimension selected.
	 * @return The X-dimension selected in the GUI.
	 */
	public int getDimXSelected(){return Integer.valueOf((String)dimensionX.getSelectedItem()).intValue();}
	
	/**
	 * Return the Y-dimension object.
	 * @return The Y-dimension JComboBox.
	 */
	public JComboBox getDimY(){return dimensionY;}

	/**
	 * Return the Y-dimension selected.
	 * @return The Y-dimension selected in the GUI.
	 */
	public int getDimYSelected(){return Integer.valueOf((String)dimensionY.getSelectedItem()).intValue();}

	/**
	 * Return the flit width object.
	 * @return The flit width JComboBox.
	 */
	public JComboBox getFlitWidth(){return flitWidth;}

	/**
	 * Return the flit width selected.
	 * @return The flit width selected in the GUI.
	 */
	public String getFlitWidthSelected(){return (String)flitWidth.getSelectedItem();}

	/**
	 * Return the buffer depth object.
	 * @return The buffer depth JComboBox.
	 */
	public JComboBox getBufferDepth(){return bufferDepth;}

	/**
	 * Return the buffer depth selected.
	 * @return The buffer depth selected in the GUI.
	 */
	public String getBufferDepthSelected(){return (String)bufferDepth.getSelectedItem();}
	
	/**
	 * Return the routing algorithm object.
	 * @return The routing algorithm JComboBox.
	 */
	public JComboBox getRoutingAlgorithm(){return routingAlgorithm;}

	/**
	 * Return the routing algorithm selected.
	 * @return The routing algorithm selected in the GUI.
	 */
	public String getRoutingAlgorithmSelected(){ return (String)routingAlgorithm.getSelectedItem(); }

	/**
	 * Return true if SystemC test bench option is selected.
	 * @return True if SystemC test bench option is selected.
	 */
	public boolean hasSCTestBench(){ return testBenchSC.isSelected();}

	public String getCodeSelected() {return (String)addacode.getSelectedItem();}
	
	public JComboBox getCode(){return addacode;}
	
	/**
	 * Return the generate button object.
	 * @return JButton.
	 */
	public JButton getGenerateButton(){return bGenerate;}
	
	public JButton getClockButton()
	{
		return addclockbutton;
	}
	public JButton getEditButton()
	{
		return editClockButton;
	}
	public JButton getRemoveClockButton(){ return removeclockbutton; }
	/**
	 * Return the NoC panel object.
	 * @return JPanel_Noc.
	 */
	public JPanel_Noc getNoCPanel(){return panelNoC;}

	/**
	 * Return the CRC type object.
	 * @return The CRC type JComboBox.
	 */
	public JComboBox getCrcType(){return crcType;}

	/**
	 * Return the CRC type selected.
	 * @return The CRC type selected in the GUI.
	 */
	public String getCrcTypeSelected(){return (String)crcType.getSelectedItem();}
	
	/**
	 * Return true if saboteur option is selected.
	 * @return True if saboteur option is selected.
	 */
	public boolean hasSaboteur(){ return saboteur.isSelected();}
	
	/**
	 * Return the saboteur object.
	 * @return The saboteur JCheckBox.
	 */
	public JCheckBox getSaboteur(){return saboteur;}

	/**
	 * Return true if Delay in a Rising transaction saboteur option is selected.
	 * @return True if Delay in a Rising transaction saboteur option is selected.
	 */
	public boolean hasDrSaboteur(){ return drSaboteur.isSelected();}

	/**
	 * Return true if Delay in a Falling transaction saboteur option is selected.
	 * @return True if Delay in a Falling transaction saboteur option is selected.
	 */
	public boolean hasDfSaboteur(){ return dfSaboteur.isSelected();}

	/**
	 * Return true if Positive Glitch saboteur option is selected.
	 * @return True if Positive Glitch saboteur option is selected.
	 */
	public boolean hasPgSaboteur(){ return pgSaboteur.isSelected();}
	
	/**
	 * Return true if Negative Glitch saboteur option is selected.
	 * @return True if Negative Glitch saboteur option is selected.
	 */
	public boolean hasNgSaboteur(){ return ngSaboteur.isSelected();}

	/**
	 * Return true if at least one saboteur type is selected.
	 * @return True if at least one saboteur type is selected.
	 */
	public boolean hasSelectedSaboteurType(){
		return (hasDrSaboteur() || hasDfSaboteur() || hasNgSaboteur() || hasPgSaboteur());
	}

	/**
	 * Enabled or disabled the saboteur types.
	 * @param b
	 */
	public void setEnabledSaboteurType(boolean b){
		drSaboteur.setEnabled(b);
		dfSaboteur.setEnabled(b);
		ngSaboteur.setEnabled(b);
		pgSaboteur.setEnabled(b);
	}

	/**
	 * Selected or not the saboteur types.
	 * @param b
	 */
	public void setSelectedSaboteurType(boolean b){
		drSaboteur.setSelected(b);
		dfSaboteur.setSelected(b);
		ngSaboteur.setSelected(b);
		pgSaboteur.setSelected(b);
	}

	public void setNoc(NoC snoc) {noc = snoc;}
	/**
	 * Verify if the flit width allows the NoC dimension. 
	 * @param actionListener Where the action will be treated.
	 */
	public void verifyDimension(ActionListener actionListener)
	{
		getNoCPanel().setNoCDimension(getDimXSelected(),getDimYSelected());
		if(flitWidth!=null && getFlitWidthSelected().equalsIgnoreCase("8")){
			boolean changeDimension = false;
			if(getDimXSelected()>4){
				changeDimension = true;
				getDimX().removeActionListener(actionListener);
				getDimX().setSelectedItem(""+4);
				getDimX().addActionListener(actionListener);
			}
			if(getDimYSelected()>4){
				changeDimension = true;
				getDimY().removeActionListener(actionListener);
				getDimY().setSelectedItem(""+4);
				getDimY().addActionListener(actionListener);
			}
			if(changeDimension)
			{
				if(noc.getType().equals("HermesG"))
				{
					noc.clearClock();
					noc.initDefClocks(getDimXSelected(),getDimYSelected());
				}
				JOptionPane.showMessageDialog(this,"The maximum dimension of net for flit width equal 8 is 4.","Information Message",JOptionPane.INFORMATION_MESSAGE);
			}
		}
		if(getNoCPanel()!=null) getNoCPanel().setNoCDimension(getDimXSelected(),getDimYSelected());
	}
}
