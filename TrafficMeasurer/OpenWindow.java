package TrafficMeasurer;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.io.*;

import AtlasPackage.Scenery;
import AtlasPackage.Project;
import AtlasPackage.ExampleFileFilter;

/**
 * This class shows a GUI allowing select the traffic scenery that will be evaluate.
 * @author Aline Vieira de Mello
 * @version
 */
public class OpenWindow extends JFrame implements ActionListener{

	private Scenery scenery;
	private Project project;
	private boolean isHermesSR;
	private int nCV;
	private JTextField tName,tRouting,tCTRLRouting,tGSRouting,tBERouting;
	private JButton	bBrowse,bOk,bCancel;

	/**
	 * Constructor class.
	 * @param project The project that will be evaluated.
	 */
	public OpenWindow(Project project){
		super();
		setTitle("Traffic Measurer");
		isHermesSR=project.getNoC().getType().equalsIgnoreCase("Hermessr");		
		if(isHermesSR){
			nCV=project.getNoC().getVirtualChannel();
			if(nCV==1){ setSize(500,180); }
			else{ setSize(500,240); }
		}
		else		
			setSize(500,120);			
					
		Dimension resolucao=Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((resolucao.width-500)/2,(resolucao.height-120)/2);
		setResizable(false);
		addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){dispose();}});
		getContentPane().setLayout(null);
		this.project=project;
		addComponents();
		openScenery(project.getSceneryPath() + File.separator + project.getSceneryName() + ".traffic");
		setVisible(true);
	}

	/**
	 * Add components in the Maia GUI.
	 */
	private void addComponents(){
		int x=10;
		int y=10;
		int dimy=25;
		addName(x,y,dimy);
		if(isHermesSR){
			if(nCV==1){
				y+=40;
				addRouting(x,y,dimy);
			}
			else{
				y+=40;
				addCTRLRouting(x,y,dimy);
				y+=40;
				addGSRouting(x,y,dimy);
				y+=40;
				addBERouting(x,y,dimy);
			}
		}
		y+=40;
		addButtons(x+150,y,dimy);
	}

	/**
	 * Add the component allowing select the scenery's name.
	 * @param x Horizontal initial position of the component. 
	 * @param y Vertical initial position of the component.
	 * @param dimy The vertical dimension of the component.
	 */
	private void addName(int x,int y,int dimy){
		JLabel lName=new JLabel("Scenery's Name:");
		lName.setBounds(x,y,100,dimy);
		getContentPane().add(lName);

		x+=100;
		tName=new JTextField("",10);
		tName.setToolTipText("Scenery's name that will be evaluated.");
		tName.setBounds(x,y,275,dimy);
		tName.setEnabled(false);
		getContentPane().add(tName);

		x+=285;
		bBrowse=new JButton("Open");
		bBrowse.setToolTipText("Choose the scenery's name that will be evaluated.");
		bBrowse.setBounds(x,y,80,dimy);
		bBrowse.addActionListener(this);
		getContentPane().add(bBrowse);
	}

	/**
	 * Add the component allowing select the routing file name.
	 * @param x Horizontal initial position of the component. 
	 * @param y Vertical initial position of the component.
	 * @param dimy The vertical dimension of the component.
	 */
	private void addRouting(int x,int y,int dimy){
		JLabel lRouting=new JLabel("Routing's Name:");
		lRouting.setBounds(x,y,100,dimy);
		getContentPane().add(lRouting);
		x+=100;
		tRouting=new JTextField("",10);
		tRouting.setToolTipText("Routing's adopted during simulation.");
		tRouting.setBounds(x,y,275,dimy);
		tRouting.setEnabled(false);
		getContentPane().add(tRouting);
	}

	/**
	 * Add the component allowing select the CRC routing file name.
	 * @param x Horizontal initial position of the component. 
	 * @param y Vertical initial position of the component.
	 * @param dimy The vertical dimension of the component.
	 */
	private void addCTRLRouting(int x,int y,int dimy){
		JLabel lRouting=new JLabel("CTRL Routing file:");
		lRouting.setBounds(x,y,100,dimy);
		getContentPane().add(lRouting);
		x+=100;
		tCTRLRouting=new JTextField("",10);
		tCTRLRouting.setToolTipText("CTRL Routing file used during simulation.");
		tCTRLRouting.setBounds(x,y,275,dimy);
		tCTRLRouting.setEnabled(false);
		getContentPane().add(tCTRLRouting);
	}

	/**
	 * Add the component allowing select the GS routing file name.
	 * @param x Horizontal initial position of the component. 
	 * @param y Vertical initial position of the component.
	 * @param dimy The vertical dimension of the component.
	 */
	private void addGSRouting(int x,int y,int dimy){
		JLabel lRouting=new JLabel("GS Routing's Name:");
		lRouting.setBounds(x,y,100,dimy);
		getContentPane().add(lRouting);
		x+=100;
		tGSRouting=new JTextField("",10);
		tGSRouting.setToolTipText("GS Routing file used during simulation.");
		tGSRouting.setBounds(x,y,275,dimy);
		tGSRouting.setEnabled(false);
		getContentPane().add(tGSRouting);
	}

	/**
	 * Add the component allowing select the BE routing file name.
	 * @param x Horizontal initial position of the component. 
	 * @param y Vertical initial position of the component.
	 * @param dimy The vertical dimension of the component.
	 */
	private void addBERouting(int x,int y,int dimy){
		JLabel lRouting=new JLabel("BE Routing's Name:");
		lRouting.setBounds(x,y,100,dimy);
		getContentPane().add(lRouting);
		x+=100;
		tBERouting=new JTextField("",10);
		tBERouting.setToolTipText("BE Routing file used during simulation.");
		tBERouting.setBounds(x,y,275,dimy);
		tBERouting.setEnabled(false);
		getContentPane().add(tBERouting);
	}

	/**
	 * Add OK and CANCEL buttons allowing control this GUI.
	 * @param x Horizontal initial position of the component. 
	 * @param y Vertical initial position of the component.
	 * @param dimy The vertical dimension of the component.
	 */
	private void addButtons(int x,int y,int dimy){
		bOk=new JButton("Ok");
		bOk.setBounds(x,y,80,dimy);
		bOk.addActionListener(this);
		getContentPane().add(bOk);

		x+=100;
		bCancel=new JButton("Cancel");
		bCancel.setBounds(x,y,80,dimy);
		bCancel.addActionListener(this);
		getContentPane().add(bCancel);
	}

	/**
	 * Select a traffic scenery belong to the project.
	 * @param name The path where are the traffic sceneries.
	 * @return The traffic scenery selected.
	 */
	private String selectScenery(String name){
		JFileChooser filechooser = new JFileChooser();
		filechooser.setFileFilter(new ExampleFileFilter("traffic",".Traffic Files"));
		filechooser.setCurrentDirectory(new File(name));
		int intOption = filechooser.showOpenDialog(null);
		if(intOption == JFileChooser.APPROVE_OPTION){
			File file = filechooser.getSelectedFile();
			if(!file.exists())
				JOptionPane.showMessageDialog(null,"The selected scenery doesn't exist.","Error Message",JOptionPane.ERROR_MESSAGE);
			else if(file.getAbsolutePath().lastIndexOf(project.getPath()+File.separator + "Traffic")==-1)
				JOptionPane.showMessageDialog(null,"The selected scenery isn't in the project's path.","Error Message",JOptionPane.ERROR_MESSAGE);
			else return file.getAbsolutePath();
		}
		return "";
	}

	/**
	 * Open the traffic scenery informed.
	 * @param name The scenery's path.
	 * @return True if the scenery was opened. 
	 */
	private boolean openScenery(String name){
		scenery = new Scenery(project.getNoC().getNumRotX(),project.getNoC().getNumRotY());
		scenery.open(new File(name));
		File out = new File(project.getPath()+File.separator + "Traffic" + File.separator+scenery.getName()+File.separator + "Out");
		if(out.exists()){
			if(out.listFiles()!=null && out.listFiles().length!=0){
				tName.setText(name.substring(name.lastIndexOf(File.separator)+1));
				scenery.setSimulated(true);
				if(isHermesSR){
					if(nCV==1){
						tRouting.setText(scenery.getRouting());
					}
					else{
						tCTRLRouting.setText(scenery.getCTRLRouting());
						tGSRouting.setText(scenery.getGSRouting());
						tBERouting.setText(scenery.getBERouting());
					}
				}
				return true;
			}
		}
		return false;
	}

	public void actionPerformed(ActionEvent e){
		if(e.getSource() == bBrowse){
			String name = selectScenery(project.getSceneryPath());
			tName.setText("");
			if(!name.equals("")){
				if(!openScenery(name))
					JOptionPane.showMessageDialog(null,"The selected scenery was not simulated.","Error Message",JOptionPane.ERROR_MESSAGE);
			}
		}
		else if(e.getSource() == bCancel)
			dispose();
		else if(e.getSource() == bOk){
			String strErrorMsg = "";
			if(tName.getText().equals("")) strErrorMsg += " - You must inform your Scenario's name!\n";			
			if(strErrorMsg.equals("")){

				project.setSceneryName(tName.getText().substring(0,tName.getText().lastIndexOf(".")));
				project.write();
				dispose();
				new TrafficInterface(project,scenery);
			}
			else{
				JOptionPane.showMessageDialog(null,"The Following Errors occured: \n" + strErrorMsg,"Error Message",JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
