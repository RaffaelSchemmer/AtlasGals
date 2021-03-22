package AtlasPackage;

import java.io.*;
import javax.swing.*;
import java.util.*;

/**
* The <i>Default</i> class manipulates the Atlas configuration file.
* @author Aline Vieira de Mello
* @version
*/
public class Default{
  
	/** The atlas home path */
    public static String atlashome=System.getenv("ATLAS_HOME");
    /** Returns true when runs on Windows Operating System */
    public static boolean isWindows = (System.getProperty("os.name").toLowerCase().indexOf( "win" ) >= 0);
    /** The Gnuplot executable file name*/
    public static String gnuplot = System.getenv("GNUPLOT_HOME")+((isWindows)?"wgnuplot":"/gnuplot");
	/** The MoldelSim executable file name*/
    public static String vsim = (isWindows)?"vsim.exe":"vsim";
    /** The absolute form of configuration file*/
    private static File configFile = new File(System.getProperties().getProperty("user.home")+File.separator+".atlas");
    /** The path where the last project was created.*/
    private static String workspacePath = readInfo("WorkspacePath");
    /** The path of the last open project.*/
    private static String projectPath  = readInfo("ProjectPath");
    /** The absolute path of the default browser executable file.*/
    private static String browserFile = readInfo("BrowserFile");
        	
    /**
     * Return the path where the last project was created.
     * @return The workspace path
     */
    public static String getWorkspacePath(){return workspacePath;}
    
    /**
     * Returns the path of the last open project.
     * @return The project path
     */
    public static String getProjectPath(){return projectPath;}

    /**
     * Returns the absolute path of the project file. It has extension <o>.noc</i>.
     * @return The project file
     */
    public static File getProjectFile(){
    	String projectDir = projectPath;
    	String projectName = new File(projectDir).getName(); 
    	return new File(projectDir + File.separator + projectName + ".noc");
    }
    
    /**
     * Returns the absolute path of the default browser executable file.
     * @return defaultBrowserFile
     */
    public static String getBrowserFile(){return browserFile;}

    /**
     * Returns the information associated to a label. The label syntax is: <i>{label}information</i>.  
     * @param label The label name. 
     * @return info
     */
    private static String readInfo(String label){
    	String info;
		try{
		    info = readInfo(configFile, label);
		}catch(Exception f){
		    //return the current directory
		    info = System.getProperties().getProperty("user.dir");
		}
		return info;
	}

    /**
     * Returns the information associated to a label. The label syntax is: <i>{label}information</i>.
     * @param file The file where the label is searched.  
     * @param label The label name. 
     * @return information
     * @throws Exception
     */
    public static String readInfo(File file, String label) throws Exception {
		StringTokenizer st;
		String line, word, info="";

		FileInputStream fis=new FileInputStream(file);
	    BufferedReader br=new BufferedReader(new InputStreamReader(fis));
	    line=br.readLine();
	    while(line!=null){
			st = new StringTokenizer(line, "{}");
			int nTokens = st.countTokens();
			if(nTokens!=0){
			    word = st.nextToken();
			    if(word.equalsIgnoreCase(label)){
					if(nTokens == 1)
					    return "";
					else{
					    info = st.nextToken();
					    return info;
					}
			    }
			}
			line=br.readLine();
	    }
	    br.close();
	    fis.close();
		return info;
	}

    /**
     * Return the absolute path of configuration file.
     * @return The configuration file
     */
    public static File getConfigFile(){ return configFile;}

	/**
	 * Sets the path where the last open project was created and writes the configuration file.
	 * @param s
	*/
    public static void setWorkspacePath(String s){
		workspacePath = s;
		save();
	}

	/**
	 * Sets the absolute path of the last open project and writes the configuration file.
	 * @param s
	*/
	public static void setProjectPath(String s){
		projectPath = s;
		save();
	}

	/**
	 * Sets the absolute path of the default browser executable file and writes the configuration file.
	 * @param s
	*/
	public static void setBrowserFile(String s){
		browserFile = s;
		save();
	}
	
	/**
	 * Writes the configuration file.
	*/
	private static void save(){
		try{
		    DataOutputStream data_output=new DataOutputStream(new FileOutputStream(configFile));
			data_output.writeBytes("{WorkspacePath}"+workspacePath+"\n");
			data_output.writeBytes("{ProjectPath}"+projectPath+"\n");
			data_output.writeBytes("{BrowserFile}"+browserFile+"\n");
			data_output.close();
		}catch(Exception ecx){
			JOptionPane.showMessageDialog(null,"It's not possible to create the " + configFile +" file","Error Message",JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/**
	 * Show a graph using Gnuplot tool.
	 * @param graph
	 */
	public static void showGraph(String graph)
	{
		try
    	    {
	    	if(isWindows)
    			Runtime.getRuntime().exec(gnuplot + " -persist \"" + graph + "\"");
		else
    			Runtime.getRuntime().exec(gnuplot + " -persist " + graph);
	    }
	    catch(Exception exc){
	    	JOptionPane.showMessageDialog(null,exc.getMessage(),"Error Message",JOptionPane.ERROR_MESSAGE);
	    }
	}
}
