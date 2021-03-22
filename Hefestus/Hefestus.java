package Hefestus;

import javax.swing.*;
import java.io.*;

import AtlasPackage.Project;

/**
 * Hefestus is the Power Evaluation tool able to evaluate a Hermes NoC with round-robin routing algorithm.
 * @author Aline Vieira de Mello
 * @version
 */
public class Hefestus{

	/**
	 * Show Hefestus tool according to the informed parameters. 
	 * @param s
	 */
	public Hefestus(String s[]){
		if(s!=null && s.length==1){
			//s[0] = absolute path of the .noc file. For example, c:\HardNoCs\NoC3x3\NoC3x3.noc
			File f = new File(s[0]);
			if(f.exists()){
				Project p = new Project(f);
				new Power(p);
			}
			else
				JOptionPane.showMessageDialog(null,"The project informed does not exit.","Error",JOptionPane.ERROR_MESSAGE);
		}
		else
		 if(s!=null && s.length==3){
			File f = new File(s[0]);
			
			if(f.exists()){
				Project p = new Project(f);
				int time = Integer.valueOf(s[1]).intValue();
				String resolution = s[2];
				if(p.getNoC().getFlowControl().equalsIgnoreCase("Handshake")){
				    Handshake handshake = new Handshake(p, time, resolution);
						handshake.generate();
				}
				else{ //creditBased	
					if(p.getNoC().getVirtualChannel() == 1){
						CreditBased creditBased = new CreditBased(p, time, resolution);
						creditBased.generate();		
					}
					else{
						VirtualChannel virtualChannel = new VirtualChannel(p, time, resolution);
						virtualChannel.generate();
					}
				}
			}
			else{
				JOptionPane.showMessageDialog(null,"The project informed does not exit.","Error",JOptionPane.ERROR_MESSAGE);
			}
		 }			
		 else
			JOptionPane.showMessageDialog(null,"Inform the project's name.","Error", JOptionPane.ERROR_MESSAGE);
 	}

	/**
	 * Launch the Hefestus (Power Evaluation) tool.
	 * @param s
	 */
	public static void main(String s[]){
		JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);
		new Hefestus(s);
	}
}
