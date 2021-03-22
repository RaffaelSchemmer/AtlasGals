package TrafficMbps;

import java.awt.*;
import java.io.*;
import javax.swing.*;
import java.util.*;
import java.text.*; 
import java.lang.*;

import AtlasPackage.Project;
import AtlasPackage.Cost;
import AtlasPackage.Mapping;
import AtlasPackage.Dependance;
import AtlasPackage.Convert;
import AtlasPackage.Default;
import AtlasPackage.Router;
import AtlasPackage.AvailableClock;
import AtlasPackage.ManipulateFile;
import AtlasPackage.NoCGeneration;

/**
 * This class Read the CDCG model and modify the testbenchs of traffic generation
 * @author Raffael Bottoli Schemmer
 * @version
 */
 
 /*
  * Classe responsável pela leitura do arquivo .CDCG.
 */
public class ReadTrafficCDCM
{

	private Project project;
	private String filePath;
	private String graph_name;
	
	/*
	 * Método efetua a leitura do arquivo CDCG(.model) e captura :
	 * Tamanho da NoC
	 * Custo das aplicacões
	 * Dependencia das aplicacões
	 * Mapeamento das aplicacões
	*/
	
	public int openFileCDCG() /* (OK) Método que varre e captura os dados em (Cost/Dependence/Mapping)*/
	{
		StringTokenizer st;
		String line, word, change_parameter;
		try
		{
			FileInputStream inFile = new FileInputStream(new File(filePath));
			BufferedReader buff=new BufferedReader(new InputStreamReader(inFile));	    
			int n_lines=0;
			change_parameter="";
			line=buff.readLine();
			while(line!=null)
			{
				
				st = new StringTokenizer(line, "#");
				int vem = st.countTokens();
				for (int cont=0; cont<vem; cont++)
				{
					word = st.nextToken();
					change_parameter="";
					if(word.equalsIgnoreCase("Noc Size"))
					{
						line=buff.readLine();
						if(!line.equals(Integer.toString(project.getNoC().getNumRotX()) + " " + Integer.toString(project.getNoC().getNumRotY())))
						{
							JOptionPane.showMessageDialog (null, "NoC dimension in model is diferent of NoC defined in the project", "Error message", JOptionPane.ERROR_MESSAGE);
							return(1);
						}
						line=buff.readLine();
					}
					else if(word.equalsIgnoreCase("Deadline Frequency"))
					{
						line=buff.readLine();
					}
					else if(word.equalsIgnoreCase("Cost of Applications"))
					{
						line=buff.readLine();
						while(!line.equals("#"))
						{
							Cost c = new Cost();
							String dado = "";
							int cont1 =0;
							cont1++;
							while(line.charAt(cont1) != ' ') {dado = dado + line.charAt(cont1); cont1++;}
							c.set_number_task(Integer.parseInt(dado));
							cont1++;
							dado = "";
							while(line.charAt(cont1) != ' ') {dado = dado + line.charAt(cont1); cont1++;}
							c.set_source(dado);
							dado = "";
							cont1=cont1+3;
							while(line.charAt(cont1) != ' ') {dado = dado + line.charAt(cont1); cont1++;}
							c.set_target(dado);
							dado = "";
							cont1++;
							while(line.charAt(cont1) != ' ') {dado = dado + line.charAt(cont1); cont1++;}
							if(Double.parseDouble(dado) > (Math.pow(2,project.getNoC().getFlitSize())-1))
							{
								JOptionPane.showMessageDialog (null, "The maximum number of flits that can be transmitted through this network is " + (Math.pow(2,project.getNoC().getFlitSize())-1) + " flits");
								return(1);
							}
							else if(Double.parseDouble(dado) < 5)
							{
								JOptionPane.showMessageDialog (null, "The minimum number of flits that can be transmitted through this network is 5 flits");
								return(1);
							}
							c.set_number_comunication(Integer.parseInt(dado));
							cont1=cont1+3;
							dado = "";
							while(line.charAt(cont1) != ' ') {dado = dado + line.charAt(cont1); cont1++;}
							if(Integer.parseInt(dado) > (Math.pow(2,project.getNoC().getFlitSize())-1))
							{
								JOptionPane.showMessageDialog (null, "The maximum number of computer cicles supported in traffic packet is " + (Math.pow(2,project.getNoC().getFlitSize())-1) + " cicles");
								return(1);
							}
							else if(Integer.parseInt(dado) < 0)
							{
								JOptionPane.showMessageDialog (null, "The minimum number of computer cicles supported in traffic packet is 0 cicles");
								return(1);
							}
							c.set_number_computation(Integer.parseInt(dado));
							dado  = "";
							cont1=cont1+3;
							while(cont1 < line.length()) {dado = dado + line.charAt(cont1); cont1++;}
							if(Integer.parseInt(dado) < 0)
							{
								JOptionPane.showMessageDialog (null, "The minimum deadline of application is 0 ns");
								return(1);
							}
							c.set_task_deadline(Integer.parseInt(dado));
							
							project.setCost(c);
							line=buff.readLine();
						}	
					}
					else if(word.equalsIgnoreCase("Dependence of Applications"))
					{
						line=buff.readLine();
						while(!line.equals("#"))
						{
							Dependance d = new Dependance();
							String[] palavras = line.split(" ");
							for (int i = 1; i < palavras.length; i++)
							{
								if(i == 1) { d.set_source(palavras[i]); }
								else { d.set_target(palavras[i]); }
							}
							project.setDependance(d);						
													
							line=buff.readLine();
						}						 
					}
					else if(word.equalsIgnoreCase("Mapping of Applications"))
					{
						line=buff.readLine();
						while(!line.equals("#"))
						{
							Mapping m = new Mapping();
							String[] palavras = line.split(" ");
							for (int i = 0; i < palavras.length; i++)
							{
								if(i == 0) { m.set_tasking(palavras[i]); }
								else if(i == 1){ m.set_node_x(palavras[i]); }
								else if(i == 2)
								{ 
									m.set_node_y(palavras[i]);
									m.set_node("" + m.get_node_x() + m.get_node_y());
								}
							}
							
							project.setMapping(m);
							
							line=buff.readLine();
						}
					}
				}
				line=buff.readLine();	
			}
			buff.close();
			inFile.close();
		}//end try
		catch(FileNotFoundException f)
		{
			JOptionPane.showMessageDialog(null,"Can't read the CDCG model"  + " file\n" + f.getMessage(),"Output error", JOptionPane.ERROR_MESSAGE);
			return(1);
		}
		catch(Exception e)
		{
			JOptionPane.showMessageDialog(null,"Can't read the CDCG model"  + " file\n"+e.getMessage(),"Error message", JOptionPane.ERROR_MESSAGE);
			return(1);
		}
		return(0);
	}

	public ReadTrafficCDCM(Project p,String fp,String fname)
	{
		project = p;
		filePath = fp;
		graph_name = fname;
 	}
}
