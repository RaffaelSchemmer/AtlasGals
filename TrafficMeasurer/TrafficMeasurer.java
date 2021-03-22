package TrafficMeasurer;

import javax.swing.*;
import java.io.*;

import AtlasPackage.Project;

/**
 * TrafficMeasurer is a tool able to analyze a traffic scenery.
 * @author Aline Vieira de Mello
 * @version
 */
public class TrafficMeasurer{

	/**
	 * Show Traffic Measurer tool according to the informed parameters. 
	 * @param s
	 */
	public TrafficMeasurer(String s[]){
		if(s!=null && s.length==1){
			//s[0] = absolute path of the project. For example, c:\HardNoCs\NoC3x3
			File f = new File(s[0]);
			if(f.exists()){
				Project project = new Project(f);
				new OpenWindow(project);
			}
			else{
				JOptionPane.showMessageDialog(null,"The project informed does not exit.","Error",JOptionPane.ERROR_MESSAGE);
			}
		}
 	}

	/**
	 * Launch the Traffic Measurer tool.
	 * @param s
	 */
	public static void main(String s[]){
		JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
		new TrafficMeasurer(s);
	}
}
