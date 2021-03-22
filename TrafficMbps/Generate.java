package TrafficMbps;

import java.io.*;
import java.util.*;
import javax.swing.*;

import AtlasPackage.Convert;
import AtlasPackage.RouterTraffic;
import AtlasPackage.Router;
import AtlasPackage.NoC;
import AtlasPackage.SR4Traffic;
import AtlasPackage.Scenery;
import AtlasPackage.Project;
import java.util.ArrayList;

/**
 * This class generates the traffic scenery.
 * @author Aline Vieira de Mello
 * @version
 */
public class Generate
{
	private FileOutputStream file;
	private DataOutputStream dataOutput;
	private Project project;
	private Scenery scenery;
	private String sceneryPath;
	private int dimX;
	private int flitWidth;
	private int flitClockCycles;

	private SpatialDistribution distSpatial;
	private String sACG;
	/** Indicates the sequence number of a packets (must be global)*/
	private int sequenceNumberH=0,sequenceNumberL=1;
	private int [] nPackets;
	private int [] nFlits;
	private int totalPackets, totalFlits;

	private static final int CTRL = SR4Traffic.CTRL, GS = SR4Traffic.GS, BE = SR4Traffic.BE;

	/**
	 * Generates the traffic 
	 * @param project The project
	 * @param scenery The scenery
	 */
	public Generate(Project project, Scenery scenery){
		super();
		this.project=project;
		this.scenery=scenery;
		sceneryPath=project.getSceneryPath();
		dimX=project.getNoC().getNumRotX();
		flitWidth=project.getNoC().getFlitSize();
		flitClockCycles=project.getNoC().getCyclesPerFlit();
		distSpatial = new SpatialDistribution(project.getNoC().getNumRotX(),project.getNoC().getNumRotY(),project.getNoC().getFlitSize());
		nPackets=new int[scenery.size()];
		nFlits=new int[scenery.size()];
		if(!(project.getNoC().isSR4()))
			writeTraffic();
		else
			writeTrafficHSRCV4();
 	}

	private void copyRoutingFile(){
		File diretory;
		diretory = new File(sceneryPath+File.separator + "Routing");
		diretory.mkdirs();

		if(project.getNoC().getVirtualChannel()==1){
			copyFile(sceneryPath+File.separator + ".." + File.separator + ".." + File.separator + "Routing" + File.separator + "pure_xy.rot", sceneryPath+File.separator + "Routing" + File.separator + "pure_xy.rot");
		}
		else if(project.getNoC().getVirtualChannel()==4){
			copyFile(sceneryPath+File.separator + ".." + File.separator + ".." + File.separator + "Routing" + File.separator + "pure_xy_CTRL.rot", sceneryPath+File.separator + "Routing" + File.separator + "pure_xy_CTRL.rot");
			copyFile(sceneryPath+File.separator + ".." + File.separator + ".." + File.separator + "Routing" + File.separator + "pure_xy_GS.rot", sceneryPath+File.separator + "Routing" + File.separator + "pure_xy_GS.rot");
			copyFile(sceneryPath+File.separator + ".." + File.separator + ".." + File.separator + "Routing" + File.separator + "pure_xy_BE.rot", sceneryPath+File.separator + "Routing" + File.separator + "pure_xy_BE.rot");
			try{
					FileOutputStream routing=new FileOutputStream(project.getPath()+File.separator + "SC_NoC" + File.separator + "defs.h");
					DataOutputStream data_routing=new DataOutputStream(routing);

					data_routing.writeBytes("//========== DEFINE ROUTING FILES  TO ME USED ==========\n");
					data_routing.writeBytes("#define _CTRL_ROTFILE_DEF \"Traffic" + File.separator + File.separator+scenery.getName()+File.separator + File.separator + "Routing" + File.separator + "" + File.separator + "pure_xy_CTRL.rot\"\n");
					data_routing.writeBytes("#define _GS_ROTFILE_DEF \"Traffic" + File.separator + File.separator+scenery.getName()+File.separator + File.separator + "Routing" + File.separator + "" + File.separator + "pure_xy_GS.rot\"\n");
					data_routing.writeBytes("#define _BE_ROTFILE_DEF \"Traffic" + File.separator + File.separator+scenery.getName()+File.separator + File.separator + "Routing" + File.separator + "" + File.separator + "pure_xy_BE.rot\"\n");
					data_routing.writeBytes("//========== ========== ========== ========== ==========\n");
					
					data_routing.close();
					routing.close();
			}catch(Exception e){
				JOptionPane.showMessageDialog(null,"Cannot write defs.h file.\n"+e.getMessage(),"Output error", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
		}
		else{
			JOptionPane.showMessageDialog(null,"ERROR: Could not copy routing files. Hermes SR design with "+project.getNoC().getVirtualChannel()+" does not exist." ,"Output error", JOptionPane.ERROR_MESSAGE);
		}
	}
		
	private void copyFile(String _source, String _target){
		int c;
		try{
			File inputFile = new File(_source);
			FileReader in = new FileReader(inputFile); 
			File outputFile = new File(_target);
			FileWriter out = new FileWriter(outputFile);				
			while ((c = in.read()) != -1) out.write(c);			
			in.close(); out.close();
		} catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't copy file "+_source+" to "+_target,"Output error", JOptionPane.ERROR_MESSAGE);
			System.out.println("erro a");
			System.exit(0);
		} catch(Exception e){
			System.out.println("erro aqui" + e.getMessage());
		}	
	}

	private void save2File(String _filename, String _containt){	
		try{
			FileOutputStream targetFile = new FileOutputStream(_filename);
			DataOutputStream dtout = new DataOutputStream(targetFile);
			dtout.writeBytes(_containt);
			dtout.close();
			targetFile.close();
		}
		catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't write file "+ _filename,"Output error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		catch(Exception e){System.out.println("Error in save2File = " + e.getMessage());}
	}
	
	/**
	 * Create the traffic directories:
	 * <li> <b>In</b> The input traffic directory.
	 * <li> <b>Out</b> The output traffic directory.
	 */
	private void createTrafficDirectories(){
		File diretory;
		diretory = new File(sceneryPath+File.separator + "In");
		diretory.mkdirs();
		diretory = new File(sceneryPath+File.separator + "Out");
		diretory.mkdirs();
	}
	
	/**
	 * Write the traffic scenery when the NoC is not SR4.
	 */
	public void writeTraffic(){
		Router router;
		RouterTraffic traffic;
		double pkt_tx, flt_tx, localRate;

		//create the traffic directories: In and Out
		createTrafficDirectories();
		
		//copy the routing XY defined in the source
		if(project.getNoC().getType().equalsIgnoreCase(NoC.HERMESSR))
			copyRoutingFile();
		
		//generate the traffic scenery
		if(!(project.getNoC().isSR4())){
			sACG="";
			for(int i=0; i<scenery.size();i++)
			{
				try{
					router = scenery.get(i);
					traffic = router.getTraffic();
					localRate = traffic.getRate();
					//test if the router has any traffic  
					if(localRate!=0)
					{
						int numberOfRouter;
						
						if(project.getNoC().getType().equals("HermesG"))
							numberOfRouter = (router.getAddressY() * project.getNoC().getNumRotX()) + router.getAddressX();
						else
							numberOfRouter =  Convert.getNumberOfRouter(router.getAddress(), project.getNoC().getNumRotX());
						
						file = new FileOutputStream(sceneryPath+File.separator + "In" + File.separator + "in" + numberOfRouter + ".txt");
						
						dataOutput = new DataOutputStream(file);
						if(!project.getNoC().getScheduling().equalsIgnoreCase("CircuitSwitching")){
							totalPackets=traffic.getNumberOfPackets();
							//set zeros to the rates variables
							totalFlits=0;
							for(int j=0;j<i;j++){
								nPackets[j]=nFlits[j]=0;
							}
						}
						// -1 indicates the service, i.e. this parameter is not applicable to this router 
						writeLines(-1, router);
						
						if((! project.getNoC().getScheduling().equalsIgnoreCase("CircuitSwitching")) && (localRate!=0) && (totalFlits!=0) && (totalPackets!=0)){
							for(int j=0;j<scenery.size();j++){
								if(nPackets[j]!=0){
									pkt_tx=nPackets[j];
									pkt_tx=(pkt_tx/totalPackets)*(localRate);
									flt_tx=(nFlits[j]);
									flt_tx=(flt_tx/totalFlits)*(localRate);

									sACG+= (i + ";" + j + ";" +
										   (double)pkt_tx+";"+
										   (double)flt_tx+"\n");
								}
							}
						}
						
						dataOutput.close();
						file.close();
					}

				}catch(FileNotFoundException f){
					JOptionPane.showMessageDialog(null,"Can't write the TXT files","Output error", JOptionPane.ERROR_MESSAGE);
					System.exit(0);
				}catch(Exception e){
					System.out.println("Error in write Traffic = " + e.getMessage());
				}
			}

			if(! project.getNoC().getScheduling().equalsIgnoreCase("CircuitSwitching")){
				//scenery.setACG(sACG);
				try{
					file=new FileOutputStream(sceneryPath+File.separator+scenery.getName()+".acg");
					dataOutput=new DataOutputStream(file);
					dataOutput.writeBytes(sACG);
					dataOutput.close();
					file.close();
				}catch(FileNotFoundException f){
					JOptionPane.showMessageDialog(null,"Can't write the ACG file","Output error", JOptionPane.ERROR_MESSAGE);
					System.exit(0);
				}catch(Exception e){
					System.out.println("Error in save2File = " + e.getMessage());
				}
			}
		}
	}
	
	/**
	 * Write the traffic scenery when the NoC is SR4, i.e, SR with 4 virtual channels
	 */
	public void writeTrafficHSRCV4(){
		Router router;
		RouterTraffic traffic;
		double pkt_tx, flt_tx, localRate;
		int currentService;
		
		//create the traffic directories: In and Out
		createTrafficDirectories();

		//copy the routing XY defined in the source
		if(project.getNoC().getType().equalsIgnoreCase(NoC.HERMESSR))
			copyRoutingFile();
		
		//generate the traffic file
		String [] sACG = new String[3];
		//sACG[Scenery.CTRL]=""; sACG[Scenery.GS]=""; sACG[Scenery.BE]="";
		sACG[0]=""; sACG[1]=""; sACG[2]="";
		String lname="";
		for(int i=0; i<scenery.size();i++){
			currentService=-1;
			while(true){
				if(project.getNoC().isSR4()){
					switch(currentService){
					case CTRL: currentService=GS; lname="GS"; break;
					case GS: currentService=BE; lname="BE"; break;
					default: currentService=CTRL; lname="CTRL"; break;
					}
				}
				try{
					router = scenery.get(i);
					traffic = router.getTraffic();
					localRate=traffic.getUniformRate(currentService);
					if(localRate!=0){
						int numberOfRouter =  Convert.getNumberOfRouter(router.getAddress(), project.getNoC().getNumRotX());
						if(currentService==-1){ file=new FileOutputStream(sceneryPath+File.separator + "In" + File.separator + "in"+ numberOfRouter +".txt"); }
						else{ file=new FileOutputStream(sceneryPath+File.separator + "In" + File.separator + "in" + lname + numberOfRouter +".txt"); }

						dataOutput=new DataOutputStream(file);

						totalPackets=traffic.getNumberOfPackets(currentService);
						totalFlits=0;
						
						for(int j=0;j<scenery.size();j++)
							nPackets[j]=nFlits[j]=0;
						
						writeLines(currentService,router);

						localRate=traffic.getUniformRate(currentService);

						if((localRate!=0) && (totalFlits!=0) && (totalPackets!=0)){
							for(int j=0;j<scenery.size();j++){
								if(nPackets[j]!=0){
									pkt_tx=nPackets[j];
									pkt_tx=(pkt_tx/totalPackets)*(localRate);
									flt_tx=(nFlits[j]);
									flt_tx=(flt_tx/totalFlits)*(localRate);
									sACG[currentService]+=	(
											i+";"+
											j+";"+
											(double)pkt_tx+";"+
											(double)flt_tx+"\n"
									);
								}
							}
						}
						dataOutput.close();
						file.close();
					}
				}
				catch(FileNotFoundException f){
					JOptionPane.showMessageDialog(null,"Can't write the TXT files.\n" + f.getMessage(),"Output error", JOptionPane.ERROR_MESSAGE);
					System.exit(0);
				}
				catch(Exception e){
					JOptionPane.showMessageDialog(null,e.getMessage(),"Error Message", JOptionPane.ERROR_MESSAGE);
				}
				if((currentService==-1) || ((currentService==BE))) break;
			}
		}

		if(! project.getNoC().getScheduling().equalsIgnoreCase("CircuitSwitching")){
			save2File(sceneryPath+File.separator+scenery.getName()+"_CTRL.acg",sACG[CTRL]);
			save2File(sceneryPath+File.separator+scenery.getName()+"_GS.acg",  sACG[GS]);
			save2File(sceneryPath+File.separator+scenery.getName()+"_BE.acg",  sACG[BE]);
		}
	}
	
	/**
	 * Write the lines 
	 * @param currentService This parameter is valid only for SR4 NoC. 
	 * @param router The router owner of traffic file.
	 */
	public void writeLines(int currentService, Router router){
		if(project.getNoC().getScheduling().equalsIgnoreCase("CircuitSwitching"))
			writeLinesCS(router);
		else
			writeLinesPS(currentService,router);
	}

	
	/**
	 * Write the lines in the router traffic file when Packet Switching is used 
	 * @param service The service in a SR4 traffic (CRTL=0 GS=1 BE=2). <i>This parameter is used only when NoC is SR4</i>
	 * @param router
	 */
	public void writeLinesPS(int service, Router router){
		String linha,payload="";
		String target,priority;
		String[] timestampHex;
		int sourceX,sourceY,iTarget;
		int counter,packetSize,payloadSize=0, lnpackets;
		RouterTraffic traffic = router.getTraffic();

		//generate the time that each packet should be transmitted to NoC
		TimeDistribution distTime = new TimeDistribution(project,router,traffic,flitWidth,flitClockCycles);
		Vector<String> vet = distTime.defineTime();
		
		// System.out.println(router.getAddress());
		lnpackets = (service==-1)?traffic.getNumberOfPackets():traffic.getNumberOfPackets(service);

		try{
			// fix this for...
			for(int j=0;j<lnpackets;j++){
				linha="";
/***************************************************************************************/
/*    INITIAL TIMESTAMP
/***************************************************************************************/
				linha=linha.concat((String)vet.get(j)+" ");

/***************************************************************************************/
/*    1 FLIT = PRIORITY (HIGH) +  TARGET (LOW)
/***************************************************************************************/
				priority = Convert.formatFlit(traffic.getPriority(),flitWidth/2);

				sourceX = router.getAddressX();
				sourceY = router.getAddressY();
				target  = traffic.getTarget();
				
				if(project.getNoC().getType().equals("HermesG") || project.getNoC().getType().equals("Hermes"))
				{
					sourceX = router.getAddressX();
					sourceY = router.getAddressY();
					target  = traffic.getTarget();
					target = distSpatial.defineTarget(target,sourceX,sourceY);
					String x1  = "",y1 = "";
					if(target.length() == 4){
						 x1 = ""+target.charAt(0) + target.charAt(1);
						 x1 = Integer.toHexString(Integer.parseInt(x1));
						 y1 = ""+target.charAt(2) + target.charAt(3);
						 y1 = Integer.toHexString(Integer.parseInt(y1));
					}
					else if(target.length() == 2){
						x1 = Integer.toHexString(Integer.parseInt(""+target.charAt(0)));
						y1 = Integer.toHexString(Integer.parseInt(""+target.charAt(1)));
					}
					
					iTarget = Convert.getNumberOfRouter(""+x1+y1, dimX);
					
					if(flitWidth == 8)
					{
						// Converte x para bin
						int x = Integer.parseInt(""+target.charAt(0));
						String xs = Convert.decToBin(x,2);
						// Converte y para bin
						int y = Integer.parseInt(""+target.charAt(1));
						String ys = Convert.decToBin(y,2);
						// Junta e converte para decimal
						String fs = "" + xs + ys;
						int decimal = Integer.parseInt(fs, 2);
						linha=linha.concat("0" + Convert.decToHex(decimal,1).toUpperCase() + " ");
					}
					else if(flitWidth == 16)
						linha=linha.concat("00" + x1 + y1 + " ");
					else if(flitWidth == 32) 
						linha=linha.concat("00000" + x1 + "0" + y1 + " ");
					else if(flitWidth == 64) 
						linha=linha.concat("00000000000" + x1 + "000" + y1 + " ");					
				}
				else
				{
					target = distSpatial.defineTarget(target,sourceX,sourceY);
					iTarget = Convert.getNumberOfRouter(target, dimX);
					linha=linha.concat(priority);
					linha=linha.concat(target + " ");
				}
/***************************************************************************************/
/*    2 FLIT = SIZE
/***************************************************************************************/
				packetSize=(service==-1)?traffic.getPacketSize():traffic.getPacketSize(service);
				payloadSize = packetSize - 6;
				linha=linha + Convert.formatFlit(payloadSize, flitWidth) +" ";
				nPackets[iTarget]++;
				nFlits[iTarget]+=packetSize;
				totalFlits+=packetSize;
/***************************************************************************************/
/*    3 FLIT = SOURCE
/***************************************************************************************/
				if(project.getNoC().getType().equals("HermesG") || project.getNoC().getType().equals("Hermes"))
				{
					String source="";
					if(Integer.toString(router.getAddressX()).length() == 2) source = Integer.toHexString(router.getAddressX());
					else source = Integer.toString(router.getAddressX());
					
					if(Integer.toString(router.getAddressY()).length() == 2) source = source + Integer.toHexString(router.getAddressY());
					else source = source + Integer.toString(router.getAddressY());
					
					if(flitWidth == 8)
					{
						// Converte x para bin
						int x = Integer.parseInt(""+source.charAt(0));
						String xs = Convert.decToBin(x,2);
						// Converte y para bin
						int y = Integer.parseInt(""+source.charAt(1));
						String ys = Convert.decToBin(y,2);
						// Junta e converte para decimal
						String fs = "" + xs + ys;
						int decimal = Integer.parseInt(fs, 2);
						linha=linha.concat("0" + Convert.decToHex(decimal,1).toUpperCase() + " ");
					}
					else if(flitWidth == 16)
						linha=linha.concat("00" + source.charAt(0) + source.charAt(1) + " ");
					else if(flitWidth == 32) 
						linha=linha.concat("00000" + source.charAt(0) + "0" + source.charAt(1) + " ");
					else if(flitWidth == 64) 
						linha=linha.concat("00000000000" + source.charAt(0) + "000" + source.charAt(1) + " ");					
				
				}
				else
					linha=linha.concat(Convert.formatAddressFlit(router.getAddressX(), router.getAddressY(), flitWidth)+" ");
/***************************************************************************************/
/*    4 TO 7 FLITS = TIMESTAMP HEXA
/***************************************************************************************/
				timestampHex = Convert.formatTimestamp((String)vet.get(j), flitWidth);
				linha=linha.concat(timestampHex[3]+" "+timestampHex[2]+" "+timestampHex[1]+" "+timestampHex[0]+" ");

/***************************************************************************************/
/*    8 AND 9 FLIT = SEQUENCE NUMBER
/***************************************************************************************/
				linha=linha + Convert.formatFlit(sequenceNumberH, flitWidth) + " ";
				linha=linha + Convert.formatFlit(sequenceNumberL, flitWidth) + " ";

				//increments the sequence number
				if(sequenceNumberL==(Math.pow(2,flitWidth)-1)){
					sequenceNumberH++;
					sequenceNumberL=0;
				}
				else
					sequenceNumberL++;
/***************************************************************************************/
/*    PAYLOAD
/***************************************************************************************/
				if(j==0){
					counter=8; // 8 = 1 flit source, 2 to 5 flits timestamp and 6 and 7 sequence number
					for(int l=counter;l<=payloadSize;l++){
						if(l==payloadSize) //do not put space in the last flit payload
							payload=payload + Convert.formatFlit(counter, flitWidth);
						else
							payload=payload + Convert.formatFlit(counter, flitWidth) + " ";
						//varies the payload data
						if(counter==Math.pow(2,flitWidth)) counter=0;
						else counter++;
					}

				}
				linha=linha.concat(payload);
				dataOutput.writeBytes(linha+"\n");
			}
		}catch(Exception e){System.out.println("error in writeLinePS = " + e.getMessage());}
	}

	/**
	 * Write the lines in the router traffic file when Circuit Switching is used 
	 * @param router The router owner of router traffic
	 */
	public void writeLinesCS(Router router)
	{
		String linha,payload="";
		String target;
		int sourceX,sourceY;
		int counter,payloadSize=0;
		RouterTraffic traffic = router.getTraffic();

		//generate the time that each packet should be transmitted to NoC
		TimeDistribution distTime = new TimeDistribution(traffic,flitWidth,flitClockCycles);
		Vector<String> vet = distTime.defineTime();

		try{

			for(int j=0;j<traffic.getNumberOfPackets();j++){
				linha="";
/***************************************************************************************/
/*    FIRST LINE    - TIMESTAMP INICIAL
/***************************************************************************************/
				linha=linha.concat((String)vet.get(j)+" ");
/***************************************************************************************/
/*                     VIRTUAL CHANNEL
/***************************************************************************************/
				linha=linha.concat("1 "); //indicates the transmission channel
/***************************************************************************************/
/*                     TARGET
/***************************************************************************************/
				sourceX = router.getAddressX();
				sourceY = router.getAddressY();
				target  = traffic.getTarget();
				target = distSpatial.defineTarget(target,sourceX,sourceY);
				//the bytes more significants of flit are filled with zeros 
				target = Convert.formatAddressFlit(target, flitWidth); 
				//set 1 to the bit more significant
				Convert.set1ToBitMoreSignificant(target, flitWidth);
/***************************************************************************************/
/*                      COMMAND
/***************************************************************************************/
				//the bytes more significants of flit are filled with zeros 
				linha=linha + Convert.formatFlit(1, flitWidth);
				dataOutput.writeBytes(linha+"\n");
/***************************************************************************************/
/*    SECOND LINE  -   TIMESTAMP INICIAL
/***************************************************************************************/
				linha="";
				linha=linha.concat((String)vet.get(j)+" ");
/***************************************************************************************/
/*                      VIRTUAL CHANNEL
/***************************************************************************************/
				linha=linha.concat("0 "); //indicates the transmission channel
/***************************************************************************************/
/*                      PAYLOAD
/***************************************************************************************/
				payloadSize=traffic.getPacketSize();
				if(j==0){
					counter=1;
					for(int l=counter;l<=payloadSize;l++){
						if(l==payloadSize) //do not put space in the last payload flit
							payload=payload + Convert.formatFlit(counter, flitWidth);
						else
							payload=payload + Convert.formatFlit(counter, flitWidth) + " ";

						//varies the packet data
						if(counter==Math.pow(2,flitWidth)) counter=0;
						else counter++;
					}

				}
				linha=linha.concat(payload);
				dataOutput.writeBytes(linha+"\n");
/***************************************************************************************/
/*    THIRD LINE     - TIMESTAMP INICIAL
/***************************************************************************************/
				linha="";
				linha=linha.concat((String)vet.get(j)+" ");
/***************************************************************************************/
/*                     VIRTUAL CHANNEL
/***************************************************************************************/
				linha=linha.concat("1 "); //indicates the transmission channel
/***************************************************************************************/
/*                     TARGET
/***************************************************************************************/
				linha=linha.concat(target+" ");
/***************************************************************************************/
/*                      COMMAND
/***************************************************************************************/
				linha=linha.concat(Convert.decToHex(2,flitWidth/4));
				dataOutput.writeBytes(linha+"\n");
			}
		}catch(Exception e){System.out.println("escrita da linha");}
	}

	/**
	 * Test the class methods.
	 * @param s
	 */
	public static void main(String s[]){
//		System.out.println(Generate.addLineByte(10, "K", 16));
//		System.out.println(Generate.addLineByte("A", "K", 16));
/*
		Generate e= new Generate(4,4,16);
		String timestampHex[] = new String[4];
		timestampHex=e.getTimestamp("0FE2",16);

		System.out.println(timestampHex[3]+" "+timestampHex[2]+" "+timestampHex[1]+" "+timestampHex[0]+" ");
*/
	}

}
