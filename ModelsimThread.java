import javax.swing.*;
import java.io.*;

import AtlasPackage.Default;
import AtlasPackage.Project;
import AtlasPackage.ProgressBarFrame;

/**
 * Thread that monitor the Modelsim simulation using a progress bar.
 * @author Aline Vieira de Mello
 * @version
 */
public class ModelsimThread extends Thread{

    private int max;
    private Process process;
    private ProgressBarFrame pb;
    private Project project;
    /**
     * Launch the thread that monitor the Modelsim simulation.
     * @param p
     * @param max
     */
    public ModelsimThread(Project pr,Process p, int max){
        this.project = pr;
		this.process = p;
		this.max = max;
        if(project.getNoC().getType().equals("HermesG")) 
        {
       		pb = new ProgressBarFrame("Modelsim Simulation",max);
        }
        else
        {
            pb = new ProgressBarFrame("Modelsim Simulation",max);
        }
        start();
    }
    
    /**
     * Launch the thread to monitor the Modelsim simulation.
     */
    public void run()
    {
        if(project.getNoC().getType().equals("HermesG")) 
        {
            try
            {
			    File dir=new File(project.getPath());
				
                if(Default.isWindows)
                {
				    process = Runtime.getRuntime().exec("cmd.exe /k start Simulate_Win.bat", null, dir);
                }
				else
                {
                    process = Runtime.getRuntime().exec(project.getPath()+ File.separator + "simulate_linux", null, dir);
    			}
                process.waitFor();
                pb.setValue(1000);
                sleep(250);
                pb.setValue(2000);
                sleep(250);
                pb.setValue(3000);
                sleep(250);
                pb.setValue(4000);
                sleep(250);
                pb.setValue(5000);
                sleep(250);
                pb.end();
			}
            catch(IOException f) {}
            catch(Exception exc) {}
        }
        else
        {
        	modelsimSimulation();
        }
    }

    private void modelsimSimulation(){
		int i, exit;
		try{
		    for(i = 0; i< max; i++){
				try{
				    exit = process.exitValue();
				    if(exit!=0){
						pb.end();
						i = max;
						//get the error message
						BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()));
						String errorMsg = "";
						String line = br.readLine();
						while(line!=null){
						    errorMsg = errorMsg + "\n" + line;
						    line = br.readLine();
						}
						
						JOptionPane.showMessageDialog(null,"Error in ModelSim Simulation\n"+errorMsg,"Error message",JOptionPane.ERROR_MESSAGE);
				    }
				    else{
						i = max;
						pb.setValue(max);
				    }
				}catch(IllegalThreadStateException te){
				    pb.setValue(i);
				    if(i == max-1)  //the progress bar is not finished  before the process
					i = max-2;
				    sleep(100);
				}
		    }
		}catch(Exception e){
		    JOptionPane.showMessageDialog(null,e.getMessage(),"Error message",JOptionPane.ERROR_MESSAGE);
		}
    }
}
