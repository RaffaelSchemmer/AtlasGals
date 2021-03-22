import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.io.*;

import AtlasPackage.Project;
import AtlasPackage.Default;

/**
 * Implements a GUI allowing to create a new NoC project.   
 * @author Aline Vieira de Mello
 * @version
 */
public class ProjectWindow extends JFrame implements ActionListener{

	private JTextField tName,tPath;
	private JComboBox cbType;
	private JButton	bBrowse,bOk,bCancel;
	private String workspace,name,type;
	private boolean newProject;
	
	/**
	 * Creates the GUI allowing to create a new NoC project. 
	 */
	public ProjectWindow(){
		super();
		setTitle("New Project...");
		setSize(520,160);
		Dimension resolucao=Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((resolucao.width-520)/2,(resolucao.height-160)/2);
		setResizable(false);
		addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){dispose();}});
		getContentPane().setLayout(null);
		newProject = false;
		workspace = Default.getWorkspacePath();
		name="";
		type="Hermes";
		addComponents();
		setVisible(true);
	}
	
	/**
	 * add all components in the JFrame.  
	 */
	private void addComponents(){
		int x=10;
		int y=10;
		int dimy=25;
		addName(x,y,dimy);
		addType(x+320,y,dimy);
		y+=40;
		addPath(x,y,dimy);
		y+=40;
		addButtons(x+170,y,dimy);
	}

	/**
	* add components associated to the project name in JFrame position (x,y)
	* @param x The X-coordinate position
	* @param y The Y-coordinate position
	* @param dimy The dimension of Y-coordinate. 
	*/
	private void addName(int x,int y,int dimy){
		JLabel lName=new JLabel("Project's Name:");
		lName.setBounds(x,y,90,dimy);
		getContentPane().add(lName);

		x+=100;
		tName=new JTextField(name,10);
		tName.setToolTipText("Choose a name for your project.");
		tName.setBounds(x,y,200,dimy);
		getContentPane().add(tName);
	}

	/**
	* add components associated to the NoC type in position (x,y) of JFrame. 
	* @param x The X-coordinate position
	* @param y The Y-coordinate position
	* @param dimy The dimension of Y-coordinate. 
	*/
	private void addType(int x,int y,int dimy){
		JLabel lType=new JLabel("NoC Type:");
		lType.setBounds(x,y,60,dimy);
		getContentPane().add(lType);

		x+=65;
		cbType=new JComboBox();
		cbType.setToolTipText("Select a NoC type.");
		cbType.setBounds(x,y,100,dimy);
		cbType.addActionListener(this);
		cbType.addItem("-Choose-");
		cbType.addItem("Hermes");
		cbType.addItem("HermesCRC");
		cbType.addItem("HermesSR");
		cbType.addItem("HermesTB");
		cbType.addItem("HermesTU");
		cbType.addItem("Mercury");
		cbType.addItem("HermesG");
		cbType.setSelectedItem(type);
		getContentPane().add(cbType);
	}

	/**
	 * 	add components associated to the project path.
	 * @param x The X-coordinate position
	 * @param y The Y-coordinate position
	 * @param dimy The dimension of Y-coordinate. 
	 */
	private void addPath(int x,int y,int dimy){
		JLabel lPath = new JLabel("Project's Path:");
		lPath.setBounds(x,y,95,dimy);
		getContentPane().add(lPath);

		x+=100;
		tPath = new JTextField(workspace,5);
		tPath.setToolTipText("Choose a folder where your project will be created.");
		tPath.setBounds(x,y,275,dimy);
		getContentPane().add(tPath);

		x+=285;
		bBrowse=new JButton("Browse");
		bBrowse.setToolTipText("Choose a folder where your project will be created.");
		bBrowse.setBounds(x,y,100,dimy);
		bBrowse.addActionListener(this);
		getContentPane().add(bBrowse);
	}

	/**
	 * add JButtons labeled Ok and Cancel. 
	 * @param x
	 * @param y
	 * @param dimy
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
	* Show a JFileChoose allowing to select the directory where the projects will be created.
	* @return directory
	*/
	private String selectPath(){
		//open in the workspace  
	    JFileChooser filechooser = new JFileChooser(workspace);
		filechooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		filechooser.setCurrentDirectory(new File(tPath.getText()));
		int intOption = filechooser.showOpenDialog(null);
		if(intOption == JFileChooser.APPROVE_OPTION){
			File file = filechooser.getSelectedFile();
			return file.getAbsolutePath();
		}
		return "";
	}
	
	/**
	 * Returns true if a new project has been created.
	 * @return b
	 */
	public boolean hasNewProject(){
		return newProject;
	}

	public void actionPerformed(ActionEvent e){
		if(e.getSource() == bBrowse){
			String name = selectPath();
			if(!new File(name).exists())
				JOptionPane.showMessageDialog(null,"The selected path doesn't exist.","Error Message",JOptionPane.ERROR_MESSAGE);
			else
				tPath.setText(name);
		}
		else if(e.getSource() == bCancel)
			dispose();
		else if(e.getSource() == bOk){
			if(tName.getText().equals(""))
				JOptionPane.showMessageDialog(null,"Inform the project name.","Error Message",JOptionPane.ERROR_MESSAGE);
			else if(tPath.getText().equals(""))
				JOptionPane.showMessageDialog(null,"Inform the project path.","Error Message",JOptionPane.ERROR_MESSAGE);
			else if(cbType.getSelectedItem().toString().equalsIgnoreCase("-Choose-"))
				JOptionPane.showMessageDialog(null,"Choose a type of NoC.","Error Message",JOptionPane.ERROR_MESSAGE);
			else if(new File(tPath.getText()).exists() == false){
				int option = JOptionPane.showConfirmDialog(null,"The project path does not exit. Do you want to create it?","Question Message", JOptionPane.YES_NO_OPTION);
				if(option == JOptionPane.YES_OPTION){
					new File(tPath.getText()).mkdirs();
				}
			}
			else{
			    workspace=tPath.getText();
			    String name = tName.getText().trim();
			    String type = cbType.getSelectedItem().toString();
			    Default.setWorkspacePath(workspace);
			    Default.setProjectPath(workspace+File.separator+name);
			    Project project = new Project(workspace,name,type);
				project.write();
				newProject = true;
			    dispose();
			}
		}
	}
}
