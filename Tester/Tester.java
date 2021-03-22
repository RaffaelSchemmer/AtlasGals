package Tester;

import java.io.*;
import java.util.*;
import javax.swing.JOptionPane;

import AtlasPackage.Default;
import AtlasPackage.SR4Traffic;
import AtlasPackage.RouterTraffic;
import AtlasPackage.ManipulateFile;
import AtlasPackage.NoC;
import AtlasPackage.Scenery;
import AtlasPackage.Project;

import HermesCRC.CRCCreditBased;
import HermesSR.CV4_ctrl_gs_be;
import HermesSR.HermesSRCreditBased;
import HermesTB.HermesTBCreditBased;
import HermesTU.HermesTUVirtualChannel;
import Jupiter.NocComponentsCreator;
import Maia.FixedPriority;
import Maia.MainCreditBased;
import Maia.MainHandshake;
import Maia.MainVirtualChannel;
import TrafficMbps.Generate;

/**
 * Tester is a tool that allows verify if the Atlas project generation (NoC + traffic) is correct. <br>
 * One of the difficulties to test a project generation was the wasted time using Atlas graphical user interface (GUI).
 * Normally, the tests were done varying a limited number of parameters (for instance, the number of virtual channels). 
 * To solve this problem, Tester does not use the Atlas GUI. 
 * It uses two configuration files: <i>projects</i> configuration file and <i>sceneries</i> configuration file.
 * Each line of the projects configuration file corresponds to a project and contains its parameters. 
 * Each line of the sceneries configuration file corresponds to a scenery and contains its parameters. 
 * Tester generates all projects defined in the projects configuration file and 
 * for each project it generates all sceneries defined in the sceneries configuration file.
 * Moreover, it generates the simulation and verification scripts. 
 * The simulation script allows simulate all generated projects using Modelsim.
 * The verification script allows verify if all generated packets were correctly transmitted.
 * 
 * @author Aline Vieira de Mello
 * @version
 */
public class Tester{

/***********************************************************************************************************
 * CREATE PROJECTS FILES
 **********************************************************************************************************/
	
	/**
	 * Create the files of projects defined in the configuration file. 
	 * @param projectsConfigFile
	 * @param projectsFilesDir
	 */
	public static void createProjectsFiles(String projectsConfigFile, String projectsFilesDir){
		Project project;
		String projectName;
		File projectFile; 
		Map<String,String> map;
		int nProject = 1; //line 0 contains the order of project parameters appear in configuration file
		boolean none = true;
		
		try{
			//create the projectsFilesDir
			new File(projectsFilesDir).mkdirs();
			//delete exiting files
			ManipulateFile.deleteFiles(projectsFilesDir);
			
			do{
				//get the project parameters in configuration file
				map = getProjectParameters(projectsConfigFile, nProject);
				if(map!=null && map.size()!=0){
					projectName = map.get("Name");
					projectFile = new File(projectsFilesDir + File.separator + projectName + ".noc");
					//create the project
					project = new Project(projectName, map.get("NoCType"));
					//set the NoC parameters
					setNoCParameters(project.getNoC(), map);
					//write the project file
					project.write(projectFile);
					//System.out.println("The " + project.getName() + " project file was created.");
					none = false;
				}
				nProject++;
			}while(map!=null);
			
			if(none){
				JOptionPane.showMessageDialog(null,"There is no project in configution file.","Warning Message",JOptionPane.WARNING_MESSAGE);
			}
		}catch (Exception e){
			JOptionPane.showMessageDialog(null,"Error in createProjectsFiles\n"+ e.getMessage(),"Error Message",JOptionPane.ERROR_MESSAGE);
		}
	}
	
	/**
	 * Get the project parameters in the configuration file. <br>
	 * Each line of file describes a different project.
	 * @param projectsConfigFile The configuration file.
	 * @param nProject The project number determines which line contains its parameters.
	 * @return The project parameters.
	 */
	private static Map<String,String> getProjectParameters(String projectsConfigFile, int nProject){
		Map<String,String> map = new HashMap<String,String>();
		StringTokenizer st;
		String line, token;
		int nTokens, nLines=0;
		
		try{
			FileInputStream fis=new FileInputStream(projectsConfigFile);
			BufferedReader br=new BufferedReader(new InputStreamReader(fis));
			line=br.readLine();
		    while(line!=null){
		    	if(nLines == nProject){
					st = new StringTokenizer(line, " ");
					nTokens = st.countTokens();
					if(nTokens!=0){
						for(int i=0; i<nTokens; i++){
							token = st.nextToken();
							if(i==0 && token.startsWith("#")){ //lines is commented
								return map; //return map without elements
							}
							switch(i){
							case 0 : map.put("Name", token); break;
							case 1 : map.put("NoCType", token); break;
							case 2 : map.put("NumRotX", token); break;
							case 3 : map.put("NumRotY", token); break;
							case 4 : map.put("FlitSize", token); break;
							case 5 : map.put("BufferDepth", token); break;
							case 6 : map.put("FlowControl", token); break;
							case 7 : map.put("VirtualChannel", token); break;
							case 8 : map.put("Algorithm", token); break;
							case 9 : map.put("CyclesPerFlit", token); break;
							case 10: map.put("CyclesToRoute", token); break;
							case 11: map.put("Scheduling", token); break;
							case 12: map.put("SCTB", token); break;
							case 13: map.put("CRCType", token); break;
							case 14: map.put("Saboteur", token); break;
							case 15: map.put("DR", token); break;
							case 16: map.put("DF", token); break;
							case 17: map.put("GN", token); break;
							case 18: map.put("GP", token); break;
							case 19: map.put("Routing", token); break;
							case 20: map.put("CTRLRouting", token); break;
							case 21: map.put("GSRouting", token); break;
							case 22: map.put("BERouting", token); break;
							default: break;
							}
					    }
					}
				    br.close();
				    fis.close();
				    return map;
		    	}
				line=br.readLine();
				nLines++;
		    }
		    br.close();
		    fis.close();
		    return null;
		}catch(Exception ecx){
			JOptionPane.showMessageDialog(null,"Error in getProjectParameters.","Error Message",JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		return null;
	}
	
	/**
	 * Set the NoC parameters.
	 * @param noc The NoC.
	 * @param map The NoC parameters. 
	 */
	private static void setNoCParameters(NoC noc, Map <String,String> map){
		noc.setNumRotX(Integer.parseInt(map.get("NumRotX")));
		noc.setNumRotY(Integer.parseInt(map.get("NumRotY")));
		noc.setFlitSize(Integer.parseInt(map.get("FlitSize")));
		noc.setBufferDepth(Integer.parseInt(map.get("BufferDepth")));
		noc.setFlowControl(map.get("FlowControl"));
		noc.setVirtualChannel(Integer.parseInt(map.get("VirtualChannel")));
		noc.setAlgorithm(map.get("Algorithm"));
		noc.setCyclesPerFlit(Integer.parseInt(map.get("CyclesPerFlit")));
		noc.setCyclesToRoute(Integer.parseInt(map.get("CyclesToRoute")));
		noc.setScheduling(map.get("Scheduling"));
		noc.setSCTB(Boolean.parseBoolean(map.get("SCTB")));
		
		String crcType = map.get("CRCType");
		if(crcType.equalsIgnoreCase("Link"))
			noc.setCrcType(NoC.LINK_CRC);
		else if(crcType.equalsIgnoreCase("Source"))
			noc.setCrcType(NoC.SOURCE_CRC);
		else if(crcType.equalsIgnoreCase("Hamming"))
			noc.setCrcType(NoC.HAMMING_CRC);
		else
			noc.setCrcType(crcType);
		
		noc.setSaboteur(Boolean.parseBoolean(map.get("Saboteur")));
		noc.setDr(Boolean.parseBoolean(map.get("DR")));
		noc.setDf(Boolean.parseBoolean(map.get("DF")));
		noc.setGn(Boolean.parseBoolean(map.get("GN")));
		noc.setGp(Boolean.parseBoolean(map.get("GP")));
		noc.setRouting(map.get("Routing"));
		noc.setCTRLRouting(map.get("CTRLRouting"));
		noc.setGSRouting(map.get("GSRouting"));
		noc.setBERouting(map.get("BERouting"));
	}

/***********************************************************************************************************
 * CREATE PROJECTS
 **********************************************************************************************************/
	
	/**
	 * Create all projects defined in the configuration file. 
	 * @param projectsFilesDir The directory that contains the projects files.
	 * @param projectsDir The parent directory where all projects will be created.
	 * @param sceneriesConfigFile The configuration file that describe all sceneries.
	 */
	public static void createProjects(String projectsFilesDir, String projectsDir, String sceneriesConfigFile){
		Project project;
		String simulateScript = projectsDir + File.separator + "simulate_all";
		String verifyScript = projectsDir + File.separator + "verify_all";
		File dir = new File(projectsFilesDir);
		if(!dir.exists()){
			JOptionPane.showMessageDialog(null,"The "+dir.getName()+" does not exist.","Error in createProjects", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		else if(!dir.isDirectory()){
			JOptionPane.showMessageDialog(null,"The "+dir.getName()+" is not a directory.","Error in createProjects", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}

		File[] list = dir.listFiles();
		if(list.length == 0){
			JOptionPane.showMessageDialog(null,"The "+dir.getName()+" does not have projects files.","Error in createProjects", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	    //order the list by name
		list = ManipulateFile.orderByName(list);

	    //create the projectsDir
		new File(projectsDir).mkdirs();
		//delete exiting files
		ManipulateFile.deleteFiles(projectsDir);
		
		for(int i=0;i<list.length;i++){
			project = createProject(projectsDir, list[i]);
			//create the sceneries in project directory
			createSceneries(project, sceneriesConfigFile);
			//add the this script to simulate_all 
			addScript(project, simulateScript, "simulate_all");			
			//add the this script to simulate_all 
			addScript(project, verifyScript, "verify_all");			
			System.out.println("The " + project.getName() + " project was created.");
		}
		if(list.length != 0){
			//delete the projectFiles directory
			ManipulateFile.deleteAll(dir);
			System.out.println("All projects were created.");
		}
	}

	/**
	 * Create a project using the parameters defined in project file. 
	 * @param projectsDir The parent directory where the project will be created.
	 * @param File The project file.
	 */
	private static Project createProject(String projectsDir, File file){
		Project project;
		String projectName, projectDir;
		File projectFile;
		
		projectName = new Project(file).getName();
		projectDir = projectsDir + File.separator + projectName + File.separator;
		projectFile = new File(projectDir  + projectName + ".noc");
		//create the project directory
		new File(projectDir).mkdirs();
		//delete exiting files
		ManipulateFile.deleteFiles(projectDir);
		//copy project file from projectsFilesDir to projectDir
		ManipulateFile.copy(file, projectDir);
		//open file in projectDir
		project = new Project(projectFile);
		//generate the NoC
		generateNoC(project);
		
		return project;
	}
	
	/**
	 * Generate the NoC : NoC directory, SC_NoC directory and topNoC.vhd 
	 * @param project
	 */
	private static void generateNoC(Project project){
		NoC noc = project.getNoC();
		String nocType = noc.getType();
		if(nocType.equalsIgnoreCase(NoC.HERMES)){
			if(noc.getFlowControl().equalsIgnoreCase("Handshake")){
				new MainHandshake(project).generate();
			}
			else if(noc.getFlowControl().equalsIgnoreCase("CreditBased")){
				if(noc.getVirtualChannel() == 1 ){
					new MainCreditBased(project).generate();
				}
				else{
				    if(noc.getScheduling().equalsIgnoreCase("RoundRobin")){
						new MainVirtualChannel(project).generate();
					}
					else{
						new FixedPriority(project).generate();
					}
				}
			}
		}
		else if(nocType.equalsIgnoreCase(NoC.HERMESTU)){
			new HermesTUVirtualChannel(project).generate();
		}
		else if(nocType.equalsIgnoreCase(NoC.HERMESTB)){
			new HermesTBCreditBased(project).generate();
		}
		else if(nocType.equalsIgnoreCase(NoC.HERMESSR)){
			if(noc.getVirtualChannel() == 1){
				new HermesSRCreditBased(project).generate();
			}
			else{
				new CV4_ctrl_gs_be(project).generate();
			}
		}
		else if(nocType.equalsIgnoreCase(NoC.HERMESCRC)){
			new CRCCreditBased(project).generate();
		}
		else if(nocType.equalsIgnoreCase(NoC.MERCURY)){
			new NocComponentsCreator().geraNoc(project.getPath(), noc.getNumRotX(), noc.getNumRotY(), noc.getFlitSize(), noc.getBufferDepth());
		}
		else{
			JOptionPane.showMessageDialog(null,"The " + nocType + " does not exist or it is not supported.","Error in generateNoC", JOptionPane.ERROR_MESSAGE);
			System.exit(0);	
		}
	}

	private static void addScript(Project project, String script, String add){
		if(!Default.isWindows){
			try{
			    FileWriter outFile = new FileWriter(script, true);
			    BufferedWriter out = new BufferedWriter(outFile);
			    out.write("cd " + project.getName() + "\n");
			    out.write("./" + add + "\n");
			    out.write("cd ..\n");
			    out.close();
			    outFile.close();
	
				//change file permission
				Process p=Runtime.getRuntime().exec("chmod 777 " + script);
				p.waitFor();
			} catch (Exception e){
				JOptionPane.showMessageDialog(null,"Error in addScript.\n"+ e.getMessage(),"Error Message",JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
		}
	}

/***********************************************************************************************************
 * CREATE SCENERIES
 **********************************************************************************************************/

	/**
	 * Create the sceneries defined in the configuration file in the project directory. 
	 * @param project
	 * @param sceneriesConfigFile The configuration file that describe all sceneries.
	 */
	public static void createSceneries(Project project, String sceneriesConfigFile){
		Scenery scenery;
		String sceneryScript;
		String simulateScript = project.getPath() + File.separator + "simulate_all";
		String verifyScript = project.getPath() + File.separator + "verify_all";
		Map<String,String> map;
		int nScenery = 1; //line 0 contains the order of scenery parameters appears in configuration file
		boolean none = true;
		//delete the existing simulation script
		new File(simulateScript).delete();
		//delete the existing verification script
		new File(verifyScript).delete();
		try{
			do{
				//get the project parameters in configuration file
				map = getSceneryParameters(sceneriesConfigFile, nScenery);
				if(map!=null && map.size()!=0){
					scenery = createScenery(project,map);
					//write the script to simulate this specific scenery
					sceneryScript = createSceneryScript(project, scenery);
					//add the this script to simulate_all 
					addSceneryScript(project, simulateScript, sceneryScript);
					//add the this scenery verification to verify_all 
					addVerifyScript(project, scenery, verifyScript);
					none = false;
				}
				nScenery++;
			}while(map!=null);
			
			if(none){
				JOptionPane.showMessageDialog(null,"There is no scenery in configution file.","Warning Message",JOptionPane.WARNING_MESSAGE);
			}
		}catch (Exception e){
			JOptionPane.showMessageDialog(null,"Error in createSceneries\n"+ e.getMessage(),"Error Message",JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Create a scenery using the informed parameters in the project directory. 
	 * @param project
	 * @param map The scenery parameters
	 * @throws Exception
	 */
	private static Scenery createScenery(Project project, Map<String, String> map) throws Exception{
		Scenery scenery;
		String sceneryName;
		NoC noc = project.getNoC();
		//create a new Scenery
		sceneryName = map.get("Name");
		scenery = new Scenery(sceneryName, noc.getNumRotX(), noc.getNumRotY(), noc.isSR4());
	    if(noc.getType().equalsIgnoreCase(NoC.MERCURY) || noc.getType().equalsIgnoreCase(NoC.HERMESSR)){
	    	scenery.setInternalSimulation(false);
	    }
		if(noc.getType().equalsIgnoreCase(NoC.MERCURY))
			map.put("PacketSize", "15");
	    //set the Scenery parameters
		setSceneryParameters(scenery, map);

		//associate the scenery to project 
		project.setSceneryName(scenery.getName());
		project.setTrafficGenerate(true);
		project.write();
		//create scenery directory
		new File(project.getSceneryPath()).mkdirs();
		//delete all existing files
		ManipulateFile.deleteFiles(project.getSceneryPath());
		//write the scenery file
		scenery.save(project.getSceneryFile());
		//generate the traffic
		new Generate(project, scenery);
		//System.out.println("The " + sceneryName + " scenery was created.");
		return scenery;
	}
	
	/**
	 * Get the scenery parameters in the configuration file. <br>
	 * Each line of file describes a different scenery.
	 * @param sceneriesConfigFile The configuration file.
	 * @param nScenery The scenery determines which line contains its parameters.
	 * @return The scenery parameters.
	 */
	private static Map<String,String> getSceneryParameters(String sceneriesConfigFile, int nScenery){
		Map<String,String> map = new HashMap<String,String>();
		StringTokenizer st;
		String line, token;
		int nTokens, nLines=0;
		
		try{
			FileInputStream fis=new FileInputStream(sceneriesConfigFile);
			BufferedReader br=new BufferedReader(new InputStreamReader(fis));
			line=br.readLine();
		    while(line!=null){
		    	if(nLines == nScenery){
					st = new StringTokenizer(line, " ");
					nTokens = st.countTokens();
					if(nTokens!=0){
						for(int i=0; i<nTokens; i++){
							token = st.nextToken();
							if(i==0 && token.startsWith("#")){ //lines is commented
								return map; //return map without elements
							}
							switch(i){
							case 0 : map.put("Name", token); break;
							case 1 : map.put("Distribution", token); break;
							case 2 : map.put("Frequency", token); break;
							case 3 : map.put("Target", token); break;
							case 4 : map.put("Priority", token); break;
							case 5 : map.put("NumberOfPackets", token); break;
							case 6 : map.put("PacketSize", token); break;
							case 7 : map.put("UniformRate", token); break;
							case 8 : map.put("NormalAverageRate", token); break;
							case 9 : map.put("NormalMinimalRate", token); break;
							case 10: map.put("NormalMaximalRate", token); break;
							case 11: map.put("NormalStandardDeviation", token); break;
							case 12: map.put("NormalIncrement", token); break;
							case 13: map.put("ParetoRateOnPeriod", token); break;
							case 14: map.put("ParetoBurstSize", token); break;
							case 15: map.put("CTRL_NumberOfPackets", token); break;
							case 16: map.put("GS_NumberOfPackets", token); break;
							case 17: map.put("BE_NumberOfPackets", token); break;
							case 18: map.put("CTRL_PacketSize", token); break;
							case 19: map.put("GS_PacketSize", token); break;
							case 20: map.put("BE_PacketSize", token); break;
							case 21: map.put("CTRL_UniformRate", token); break;
							case 22: map.put("GS_UniformRate", token); break;
							case 23: map.put("BE_UniformRate", token); break;
							default: break;
							}
					    }
					}
				    br.close();
				    fis.close();
				    return map;
		    	}
				line=br.readLine();
				nLines++;
		    }
		    br.close();
		    fis.close();
		    return null;
		}catch(Exception ecx){
			JOptionPane.showMessageDialog(null,"Error in getSceneryParameters.","Error Message",JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		return null;
	}
	
	/**
	 * Set the Scenery parameters.
	 * @param scenery The scenery.
	 * @param map The scenery parameters. 
	 */
	private static void setSceneryParameters(Scenery scenery, Map <String,String> map){
		RouterTraffic traffic = scenery.getStandardTraffic();

		traffic.setDistribution(map.get("Distribution"));
		traffic.setFrequency(Double.parseDouble(map.get("Frequency")));
		traffic.setTarget(map.get("Target"));
		traffic.setPriority(Integer.parseInt(map.get("Priority")));
		if(scenery.isSR4()){
			traffic.setNumberOfPackets(SR4Traffic.CTRL, Integer.parseInt(map.get("CTRL_NumberOfPackets")));
			traffic.setNumberOfPackets(SR4Traffic.GS, Integer.parseInt(map.get("GS_NumberOfPackets")));
			traffic.setNumberOfPackets(SR4Traffic.BE, Integer.parseInt(map.get("BE_NumberOfPackets")));
			traffic.setPacketSize(SR4Traffic.CTRL, Integer.parseInt(map.get("CTRL_PacketSize")));
			traffic.setPacketSize(SR4Traffic.GS, Integer.parseInt(map.get("GS_PacketSize")));
			traffic.setPacketSize(SR4Traffic.BE, Integer.parseInt(map.get("BE_PacketSize")));
			traffic.setUniformRate(SR4Traffic.CTRL, Double.parseDouble(map.get("CTRL_UniformRate")));			
			traffic.setUniformRate(SR4Traffic.GS, Double.parseDouble(map.get("GS_UniformRate")));			
			traffic.setUniformRate(SR4Traffic.BE, Double.parseDouble(map.get("BE_UniformRate")));			
		}
		else{
			traffic.setNumberOfPackets(Integer.parseInt(map.get("NumberOfPackets")));
			traffic.setPacketSize(Integer.parseInt(map.get("PacketSize")));
			traffic.setUniformRate(Double.parseDouble(map.get("UniformRate")));
			traffic.setNormalAverageRate(Double.parseDouble(map.get("NormalAverageRate")));
			traffic.setNormalMinimalRate(Double.parseDouble(map.get("NormalMinimalRate")));
			traffic.setNormalMaximalRate(Double.parseDouble(map.get("NormalMaximalRate")));
			traffic.setNormalStandardDeviation(Double.parseDouble(map.get("NormalStandardDeviation")));
			traffic.setNormalIncrement(Double.parseDouble(map.get("NormalIncrement")));
			traffic.setParetoRateOnPeriod(Double.parseDouble(map.get("ParetoRateOnPeriod")));
			traffic.setParetoBurstSize(Integer.parseInt(map.get("ParetoBurstSize")));
		}
		scenery.setStandardConfigToRouters(false);
	}

	private static String createSceneryScript(Project project, Scenery scenery){
		DataOutputStream dos;
		String projectDir = project.getPath();
		String sceneryPath = "Traffic" + File.separator + scenery.getName() + File.separator;
		String sceneryInPath =  sceneryPath + "In" + File.separator;
		String sceneryOutPath =  sceneryPath + "Out" + File.separator;
		String scriptName = "simulate_" + scenery.getName();
		
		try{
		    if(Default.isWindows){
				dos=new DataOutputStream(new FileOutputStream(projectDir + File.separator + scriptName + ".bat"));
				dos.writeBytes("del /q in*.txt out*.txt r*.txt\n");
				dos.writeBytes("del /q " + sceneryOutPath + "* \n");
				dos.writeBytes("rmdir /s /q work\n");
				dos.writeBytes("copy " + sceneryInPath + "in* .\n");
				dos.writeBytes("vsim.exe -c -do simulate.do\n");
				dos.writeBytes("del in*\n");
				dos.writeBytes("move out* " + sceneryOutPath + "\n");
				if(scenery.isInternalSimulation())
					dos.writeBytes("move r*.txt " + sceneryOutPath + "\n");
				dos.writeBytes("exit");
				dos.close();
		    }
		    else{
				dos=new DataOutputStream(new FileOutputStream(projectDir + File.separator + scriptName));
				dos.writeBytes("echo -e '\\033[34m'Simulating " + project.getName() + " with " + scenery.getName() + " traffic'\\033[0m';\n");
				dos.writeBytes("rm -rf work in*.txt out*.txt r*.txt \n");
				dos.writeBytes("rm -rf " + sceneryOutPath  + "* \n");
				dos.writeBytes("cp " + sceneryInPath + "* .\n");
				dos.writeBytes("vsim -c -do simulate.do\n");			
				dos.writeBytes("rm -rf in*\n");
				dos.writeBytes("mv out* " + sceneryOutPath + "\n");
				if(scenery.isInternalSimulation())
				    dos.writeBytes("mv r*.txt " + sceneryOutPath + "\n");

				dos.writeBytes(getSceneryVerification(project, scenery));
				dos.close();
				
				//change file permission
				Process p=Runtime.getRuntime().exec("chmod 777 " + projectDir + File.separator + scriptName);
				p.waitFor();
				
		    }
		}catch(Exception e){
			JOptionPane.showMessageDialog(null,"Can't write the " + scriptName+ " script","Error Message", JOptionPane.ERROR_MESSAGE);
		    System.exit(0);
		}
		return scriptName;
	}

	private static String getSceneryVerification(Project project, Scenery scenery){
		String sceneryPath = "Traffic" + File.separator + scenery.getName() + File.separator;
		String sceneryInPath =  sceneryPath + "In" + File.separator;
		String sceneryOutPath =  sceneryPath + "Out" + File.separator;
		String data = "";
		data +="exist=$(find " + sceneryOutPath + " -name \"*.txt\");\n";
		data +="if [ \"$exist\" = \"\" ];\n";
		data +="then\n";
		data +="\techo -e \'\\033[31m'" + project.getName() + " with " + scenery.getName() + " traffic Error: There is no file in output directory'\\033[0m\';\n";
		data +="\texit;\n";
		data +="fi\n";
		data +="in=$(wc -l " + sceneryInPath + "* | grep -s \"total\" | sed -e \"s/total//g\" );\n";
		data +="out=$(wc -l " + sceneryOutPath + "o* | grep -s \"total\" | sed -e \"s/total//g\" );\n";
		data +="if [ \"$out\" = \"\" ]; then out=\"0\"; fi\n";
		data +="if [ $in = $out ]\n";
		data +="then\n";
		data +="\techo -e '\\033[32m'" + project.getName() + " with " + scenery.getName() + " traffic Success: $in packets generated and $out packets received'\\033[0m';\n";
		data +="else\n";
		data +="\techo -e '\\033[31m'" + project.getName() + " wiht " + scenery.getName() + " traffic Error: $in packets generated and $out packets received'\\033[0m';\n";
		data +="fi\n";
		return data;

	}
	
	private static void addSceneryScript(Project project, String simulateScript, String sceneryScript){
		if(!Default.isWindows){
			try{
			    FileWriter outFile = new FileWriter(simulateScript, true);
			    BufferedWriter out = new BufferedWriter(outFile);
			    out.write("./" + sceneryScript + "\n");
			    out.close();
			    outFile.close();
	
				//change file permission
				Process p=Runtime.getRuntime().exec("chmod 777 " + simulateScript);
				p.waitFor();
			} catch (Exception e){
				JOptionPane.showMessageDialog(null,"Error in addSceneriesScript\n"+ e.getMessage(),"Error Message",JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
		}
	}
	
	private static void addVerifyScript(Project project, Scenery scenery, String verifyScript){
		if(!Default.isWindows){
			try{
			    FileWriter outFile = new FileWriter(verifyScript, true);
			    BufferedWriter out = new BufferedWriter(outFile);
			    out.write(getSceneryVerification(project, scenery));
			    out.close();
			    outFile.close();
	
				//change file permission
				Process p=Runtime.getRuntime().exec("chmod 777 " + verifyScript);
				p.waitFor();
			} catch (Exception e){
				JOptionPane.showMessageDialog(null,"Error in addSceneriesScript\n"+ e.getMessage(),"Error Message",JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
		}
	}
	
	/**
	 * Launch the Tester tool. <br><br>
	 * 
	 * The Tester has three <b>optional</b> parameters:
	 * <li> -pConfig file: The configuration file that contains the projects parameters.
	 * <li> -sConfig file: The configuration file that contains the sceneries parameters.
	 * <li> -pDir: The parent directory where the projects directory will be created.
	 * <br><br>
	 * The value by default of these parameters are:
	 * <li> -pConfigFile=$ATLAS_HOME/Tester/Data/projects.txt 
	 * <li> -sceneriesConfigFile=$ATLAS_HOME/Tester/Data/sceneries.txt 
	 * <li> -pDir=$ATLAS_HOME/Tester/Data 
	 * <br>
	 * Examples: <br>
	 * <ol>
	 * <li> ./tester
	 * <li> ./tester -pConfigFile=/home/user/atlas_projects.txt -sConfigFile=/home/user/atlas_sceneries.txt <br>
	 * <li> ./tester -pDir=/home/
	 * </ol>
	 * @param s The parameters.
	 */
	public static void main(String s[]){
	    String testerDir = Default.atlashome + File.separator + "Tester" + File.separator + "Data";
	    String projectsConfigFile  = testerDir + File.separator + "projects.txt";
	    String sceneriesConfigFile = testerDir + File.separator + "sceneries.txt";
	    String projectsFilesDir    = testerDir + File.separator + "projectsFiles";
	    String projectsDir         = testerDir + File.separator + "atlas_projects";
	    
	    if(s!=null){
	    	for(int i=0; i<s.length; i++){
	    		if(s[i].startsWith("-pConfigFile")){
	    			projectsConfigFile = s[i].substring(s[i].indexOf("=")+1);
	    		}
	    		else if(s[i].startsWith("-sConfigFile")){
	    			sceneriesConfigFile = s[i].substring(s[i].indexOf("=")+1);
	    		}
	    		else if(s[i].startsWith("-pDir")){
	    			projectsDir = s[i].substring(s[i].indexOf("=")+1) + File.separator + "atlas_projects";
	    		}
	    	}
	    }
		System.out.println("projectsConfigFile = " + projectsConfigFile);
		System.out.println("sceneriesConfigFile = " + sceneriesConfigFile);
		System.out.println("projectsDir = " + projectsDir);
	    
	    Tester.createProjectsFiles( projectsConfigFile, projectsFilesDir);
	    Tester.createProjects( projectsFilesDir, projectsDir, sceneriesConfigFile);
	}
}
