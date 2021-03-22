package AtlasPackage;

import java.util.*;
import java.io.*;

import javax.swing.*;

/**
 * Scenery is a router vector	
 * @author Aline Vieira de Mello
 * @version  
 * 
*/	
public class Scenery extends Vector<Router>{

    /** indicates the Scenery name */ 
	private String name;

    //NoC parameters
	/** indicates the X-dimension of NoC */
	private int dimXNet;
	/** indicates the Y-dimension of NoC */
	private int dimYNet;
	/** indicates the X-dimension of Canvas where the NoC is drawn */
	private int dimXCanvas;
	/** indicates the X-dimension of Canvas where the NoC is drawn */
	private int dimYCanvas;
	/** indicates if the router belongs to a SR4 (with 4VC) NoC */
	private boolean isSR4;
	
    //simulation parameters
    /** indicates if the Scenery has been already simulated */ 
    private boolean isSimulated;
    /** indicates if the Scenery has the option <i>Internal Simulation</i> active */ 
	private boolean internalSimulation;
	/** indicates the simulation time */
	private int simulationTime;
	/** indicates the simulation time resolution. For instance: ps, ns, us ... */
	private String timeResolution;

	//routing ????
	private String routing, CTRLRouting, GSRouting, BERouting;
	
	/** standard traffic configuration */
	private RouterTraffic standardTraffic;

	private static final int TAG_FRONT = 1;

	/**
	 * Creates a new scenery with (X * Y) routers, where X corresponds to X-dimension of NoC and Y corresponds to Y-dimension of NoC.
	 * Each scenery's router transmits a traffic flow with the standard traffic configuration.
	 * @param xDim X-dimension of the NoC.
	 * @param yDim Y-dimension of the NoC.
	 */
	public Scenery (int xDim, int yDim) {
		super();
		initialize("", xDim, yDim, 0, 0, false);
	}
	
	/**
	 * Creates a new scenery with (X * Y) routers, where X corresponds to X-dimension of NoC and Y corresponds to Y-dimension of NoC.
	 * Each scenery's router transmits a traffic flow with the standard traffic configuration.
	 * @param xDim X-dimension of the NoC.
	 * @param yDim Y-dimension of the NoC.
	 * @param isSR4 Determine if the router belongs to a SR NoC with 4VCs
	 */
	public Scenery (int xDim, int yDim, boolean isSR4) {
		super();
		initialize("",xDim,yDim,0,0,isSR4);
	}

	/**
	 * Creates a new scenery with (X * Y) routers, where X corresponds to X-dimension of NoC and Y corresponds to Y-dimension of NoC.
	 * Each scenery's router transmits a traffic flow with the standard traffic configuration.
	 * @param name The scenery name.
	 * @param xDim X-dimension of the NoC.
	 * @param yDim Y-dimension of the NoC.
	 * @param isSR4 Determine if the router belongs to a SR NoC with 4VCs
	 */
	public Scenery (String name, int xDim, int yDim, boolean isSR4) {
		super();
		initialize(name,xDim,yDim,0,0,isSR4);
	}
	/**
	 * Creates a new scenery with (X * Y) routers, where X corresponds to X-dimension of NoC and Y corresponds to Y-dimension of NoC.
	 * Each scenery's router transmits a traffic flow with the standard traffic configuration.
	 * The canvas where the NoC is drawing has dimension defined by (xDimCanvas, yDimCanvas). 
	 * @param xDimNoc X-dimension of the NoC.
	 * @param yDimNoc Y-dimension of the NoC.
	 * @param xDimCanvas X-dimension of the canvas where the NoC is drawing. 
	 * @param yDimCanvas Y-dimension of the canvas where the NoC is drawing.
	 */
	public Scenery(int xDimNoc,int yDimNoc,int xDimCanvas,int yDimCanvas){
		super();
		initialize("",xDimNoc,yDimNoc,xDimCanvas,yDimCanvas,false);
	}

	/**
	 * Creates a new scenery with (X * Y) routers, where X corresponds to X-dimension of NoC and Y corresponds to Y-dimension of NoC.
	 * Each scenery's router transmits a traffic flow with the standard traffic configuration.
	 * The canvas where the NoC is drawing has dimension defined by (xDimCanvas, yDimCanvas). 
	 * @param xDimNoc X-dimension of the NoC.
	 * @param yDimNoc Y-dimension of the NoC.
	 * @param xDimCanvas X-dimension of the canvas where the NoC is drawing. 
	 * @param yDimCanvas Y-dimension of the canvas where the NoC is drawing.
	 * @param isSR4 Determine if the router belongs to a SR NoC with 4VCs
	 */
	public Scenery(int xDimNoc,int yDimNoc,int xDimCanvas,int yDimCanvas, boolean isSR4){
		super();
		initialize("",xDimNoc,yDimNoc,xDimCanvas,yDimCanvas,isSR4);
	}
	
	/**
	 * Initialize the scenery with (X * Y) routers, where X corresponds to X-dimension of NoC and Y corresponds to Y-dimension of NoC.
	 * The canvas where the NoC is drawing has dimension defined by (xDimCanvas, yDimCanvas). 
	 * Each router transmits a traffic flow initialize with the standard configuration.
	 * @param name The scenery name.
	 * @param xDimNoC X-dimension of the NoC.
	 * @param yDimNoC Y-dimension of the NoC.
	 * @param xDimCanvas X-dimension of the canvas where the NoC is drawing. 
	 * @param yDimCanvas Y-dimension of the canvas where the NoC is drawing.
	 * @param isSR4 Determine if the router belongs to a SR NoC with 4VCs
	 */
	public void initialize(String name, int xDimNoC,int yDimNoC,int xDimCanvas,int yDimCanvas, boolean isSR4){
		this.name = name;
		this.isSR4 = isSR4;
		
		//noc dimension
		dimXNet = xDimNoC;
		dimYNet = yDimNoC;
		
		//canvas dimension
		dimXCanvas = xDimCanvas;
		dimYCanvas = yDimCanvas;

		routing = "0";
		CTRLRouting = "0";
		GSRouting = "0";
		BERouting = "0";
		
		// simulation
		isSimulated = false;
		internalSimulation = true;
		simulationTime = 0;
		timeResolution = "ms";
	
		// creates the standard configuration with default traffic parameters
		standardTraffic = new RouterTraffic(isSR4);
		
		//routers
		addRouters(isSR4);
	}
	
	/**
	 * Returns the scenery name.
	 * @return The scenery name.
	 */
	public String getName() { return name; } 

	/**
	 * Returns true if scenery belongs to a SR4 NoC. 
	 * @return boolean
	 */
	public boolean isSR4(){ return isSR4;}	

	/**
	 * Tests if the Scenery has been already simulated.
	 * @return True if traffic has been simulated.
	 */
	public boolean isSimulated(){return isSimulated;}
	
	/**
	 * Tests if the option <i>internal simulation</i> is active for this scenery.
	 * @return internalSimulation
	 */
	public boolean isInternalSimulation(){return internalSimulation;}

	/**
	 * Returns the simulation time for this Scenery.
	 * @return The simulation time
	 */
	public int getSimulationTime(){return simulationTime;}

	/**
	 * Returns the time resolution.
	 * @return The time resolution
	 */
	public String getTimeResolution(){return timeResolution;}

	/**
	 * Sets the scenery name.
	 * @param n
	 */
	public void setName(String n) {name = n; } 

	/**
	 * Determines if the Scenery has been simulated.
	 * @param b
	 */
	public void setSimulated(boolean b){isSimulated = b;}

	/**
	 * Determines if the option <i>Internal Simulation</i> is active for this Scenery.
	 * @param b
	 */
	public void setInternalSimulation(boolean b){internalSimulation=b;}

	/**
	 * Sets the simulation time.
	 * @param t
	 */
	public void setSimulationTime(int t){simulationTime = t;}

	/**
	 * Sets the time resolution. For instance: ps, ns, us, ms ... 
	 * @param s
	 */
	public void setTimeResolution(String s){timeResolution = s;}
	
	/**
	 * Add all routers in scenery.
	 */
	private void addRouters(boolean isSR4){
		int addX,addY;
		int spaceXRot,spaceYRot,divXRot,divYRot,inicioXRot,inicioYRot;
		int x1,x2,y1,y2;
		//divide the X-dimension of JPanel to define the X-dimension of each router
		spaceXRot=dimXCanvas/dimXNet;
		//divide the Y-dimension of JPanel to define the Y-dimension of each router
		spaceYRot=dimYCanvas/dimYNet;
		//divide the X-dimension of router by four: (1)west wire (2 and 3) router (4)east wire
		divXRot=spaceXRot/4;
		//divide the Y-dimension of router by four: (1)north wire (2 and 3) router (4)south wire
		divYRot=spaceYRot/4;
		
		for(int i=0;i<dimXNet;i++){
			for(int j=0;j<dimYNet;j++){
				addX = i;
				addY = j;
				inicioXRot = i * spaceXRot;
				inicioYRot = (dimYNet-1-j) * spaceYRot;
				x1 = inicioXRot + divXRot;
				y1 = inicioYRot + divYRot;
				x2 = x1 + 2 * divXRot;
				y2 = y1 + 2 * divYRot;
				add(new Router(addX,addY, x1,y1,x2,y2, isSR4));
			}
		}
	}

	/**
	 * Set routers in position.
	 */
	private void setRoutersPosition(){
		int enderecoX,enderecoY;
		int espacoXRot,espacoYRot,divXRot,divYRot,inicioXRot,inicioYRot;
		int x1,x2,y1,y2;
		//divide the X-dimension of JPanel to define the X-dimension of each router
		espacoXRot=dimXCanvas/dimXNet;
		//divide the Y-dimension of JPanel to define the Y-dimension of each router
		espacoYRot=dimYCanvas/dimYNet;
		//divide the X-dimension of router by four: (1)west wire (2 and 3) router (4)east wire
		divXRot=espacoXRot/4;
		//divide the Y-dimension of router by four: (1)north wire (2 and 3) router (4)south wire
		divYRot=espacoYRot/4;
		
		for(int i=0;i<dimXNet;i++){
			for(int j=0;j<dimYNet;j++){
				enderecoX = i;
				enderecoY = (dimYNet-1-j);
				inicioXRot = i * espacoXRot;
				inicioYRot = j * espacoYRot;
				x1 = inicioXRot + divXRot;
				y1 = inicioYRot + divYRot;
				x2 = x1 + 2 * divXRot;
				y2 = y1 + 2 * divYRot;
				getRouter(enderecoX,enderecoY).setPosition(x1,y1,x2,y2);
			}
		}
	}
	
	/**
	 * Returns the routing file. 
	 * @return The routing file.
	 */
	public String getRouting(){return routing;}

	/**
	 * Returns the CTRL routing file. 
	 * @return The routing file.
	 */
	public String getCTRLRouting(){return CTRLRouting;}

	/**
	 * Returns the GS routing file. 
	 * @return The routing file.
	 */
	public String getGSRouting(){return GSRouting;}

	/**
	 * Returns the BE routing file. 
	 * @return The routing file.
	 */
	public String getBERouting(){return BERouting;}	

	/**
	 * Return the traffic standard configuration. 
	 * @return The standard traffic.
	 */
	public RouterTraffic getStandardTraffic(){ return standardTraffic;}	

	/**
	 * Sets the routing file. 
	 * @param routingFile
	 */
	public void setRouting(String routingFile){routing=routingFile;}

	/**
	 * Sets the CTRL routing file. 
	 * @param routingFile
	 */
	public void setCTRLRouting(String routingFile){CTRLRouting=routingFile;}

	/**
	 * Sets the GS routing file. 
	 * @param routingFile
	 */
	public void setGSRouting(String routingFile){GSRouting=routingFile;}

	/**
	 * Sets the BE routing file. 
	 * @param routingFile
	 */
	public void setBERouting(String routingFile){BERouting=routingFile;}	
	
	/**
	* Returns the Scenery parameters in a single string.
	* @return s
	*/
	public String getInfo(){
		String info;
		info = "{SceneryName}" + name +
				"\n{IsSimulated}" + isSimulated +
				"\n{InternalSimulation}" + internalSimulation +
				"\n{SimulationTime}" + simulationTime +
				"\n{TimeResolution}" + timeResolution+ "\n\n";
		info = info + "{TrafficStandardConfiguration}" + getTrafficStandardConfiguration();
		info = info + "{Routing}" + routing +
					  "\n{CTRLRouting}" + CTRLRouting +
					  "\n{GSRouting}" + GSRouting +
					  "\n{BERouting}" + BERouting +  "\n";
		info = info + getRoutersInfo();
		return info;
	}

	/**
	* Returns the parameters of all routers in a single string.
	* @return s
	*/
	public String getRoutersInfo(){
		Router router;
		String info = "";
		for(int i=0;i<dimXNet;i++){
			for(int j=0;j<dimYNet;j++){
				router = get( j * dimXNet + i);
				info = info + router.getInfo(); 
			}
		}
		return info;
	}
	
	/**
	* Returns all traffic standard configuration in a single string.
	* @return s
	*/
	public String getTrafficStandardConfiguration(){
		return (standardTraffic.getInfo( TAG_FRONT,"Standard_") + "\n");
	}
	
	/**
	 * Save the scenery's information in scenery file.
	 * @param file a scenery file.
	 */
	public void save(File file){
		BufferedWriter bw;
		try{
			bw = new BufferedWriter(new FileWriter(file));
			bw.write(getInfo());
			bw.close();
		}
		catch(Exception e){
			JOptionPane.showMessageDialog(null,e.getMessage(), "Write project file", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Read from file all Scenery parameters. <br>
	 * Each file line has the format: <i>{label}value</i> <br>
	 * <i>label</i> corresponds to the parameter name with the upper-case first letter. 
	 * @param file
	 */
	public void open(File file){
		try{
			name =Default.readInfo(file, "SceneryName");
			isSimulated = Boolean.parseBoolean(Default.readInfo(file, "IsSimulated" ));
			internalSimulation = Boolean.parseBoolean(Default.readInfo(file, "InternalSimulation" ));
			simulationTime = Integer.parseInt(Default.readInfo(file, "SimulationTime" ));
			timeResolution = Default.readInfo(file, "TimeResolution" );
			
			standardTraffic.readParameters(file, TAG_FRONT, "Standard_");

			routing = Default.readInfo(file, "Routing" );
			CTRLRouting = Default.readInfo(file, "CTRLRouting" );
			GSRouting = Default.readInfo(file, "GSRouting" );
			BERouting = Default.readInfo(file, "BERouting" );
			
			readRouters(file);
		} catch(Exception e){
			JOptionPane.showMessageDialog(null,e.getMessage(), "Error Message", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}

	/**
	 * Read from file the parameter of all Routers. <br>
	 * Each file line has the format: <i>{label}value</i> <br>
	 * <i>label</i> corresponds to the parameter name with the upper-case first letter. 
	 * @param file
	 */
	public void readRouters(File file){
		Router router;
		for(int i=0;i<dimXNet;i++){
			for(int j=0;j<dimYNet;j++){
				router = get( j * dimXNet + i);
				router.readParameters(file);
			}
		}
	}

	/**
	 * Returns the router that is drawing in x,y position.<br> 
	 * If there is no router in x,y position then returns null.
	 * @param x The X-coordinate from mouse clicked.
	 * @param y The Y-coordinate from mouse clicked.
	 * @return router
	 */
	public Router getClickedRouter(int x,int y){
		Router router;
		boolean clicked=false;
		for(int i=0;i<size();i++){
			router = get(i);
			clicked = router.verifyPoint(x,y);
			if(clicked)
				return router;
		}
		return null;
	}

	/**
	* Returns the router with the informed address.
	* @param addressX The address in X-dimension of the NoC.
	* @param addressY The address in Y-dimension of the NoC.
	* @return router
	*/
	public Router getRouter(int addressX, int addressY){
		Router router;
		for(int i=0;i<this.size();i++){
			router = get(i);
			if(router.getAddressX()==addressX && router.getAddressY()==addressY)
				return router;
		}
		return null;
	}

	/**
	* Returns the router traffic of the informed address.
	* @param addressX The address in X-dimension of the NoC.
	* @param addressY The address in Y-dimension of the NoC.
	* @return The router traffic.
	*/
	public RouterTraffic getRouterTraffic(int addressX, int addressY){
		Router router;
		for(int i=0;i<this.size();i++){
			router = get(i);
			if(router.getAddressX()==addressX && router.getAddressY()==addressY)
				return router.getTraffic();
		}
		return null;
	}

	
	/**
	* Returns the index of vector which contains the router with the informed address.
	* @param addressX The address in X-dimension of the NoC.
	* @param addressY The address in Y-dimension of the NOC.
	* @return The index of vector 
	*/
	public int getIndexRouter(int addressX, int addressY){
		Router router;
		for(int i=0;i<this.size();i++){
			router = get(i);
			if(router.getAddressX()==addressX && router.getAddressY()==addressY)
				return i;
		}
		return -1;
	}

	/**
	* Returns the total number of packets.
	* @return the total number of packets.
	*/
	public int getNumberOfPackets(){
		int total = 0;
		for(int x=0;x<dimXNet;x++){
			for(int y=0;y<dimYNet;y++){
				total = total + getRouterTraffic(x,y).getTotalNumberOfPackets();
			}
		}
		return total;
	}
	
	/**
	* Returns the number of packets of a specific service.
	* @param service CTRL=0 GS=1 BE=2 
	* @return The total number of packets of a specific service.
	*/
	public int getNumberOfPackets(int service){
		int total = 0;
		for(int x=0;x<dimXNet;x++){
			for(int y=0;y<dimYNet;y++){
				total = total + getRouterTraffic(x,y).getNumberOfPackets(service);
			}
		}
		return total;
	}

	/**
	 * Return the average packet size.
	 * The traffic with rate equal to zero are not considered.
	 * @return The average packet size
	 */
	public double getAveragePacketSize(){
		double pS, total = 0;
		int n = 0;
		for(int x=0;x<dimXNet;x++){
			for(int y=0;y<dimYNet;y++){
				pS = getRouterTraffic(x,y).getAveragePacketSize();
				if(pS != 0){
					total = total + pS;
					n++;
				}
			}
		}
		return total/n;
	}
	
	/**
	 * Return the average rate.
	 * The traffic with rate equal to zero are not considered.
	 * @return The average rate
	 */
	public double getAverageRate(){
		double pS, total = 0;
		int n = 0;
		for(int x=0;x<dimXNet;x++){
			for(int y=0;y<dimYNet;y++){
				pS = getRouterTraffic(x,y).getRate();
				if(pS != 0){
					total = total + pS;
					n++;
				}
			}
		}
		return total/n;
	}
	
	/**
	 * Sets the Canvas dimension.
	 * @param dimXCanvas The dimension in the X-coordinate.
	 * @param dimYCanvas The dimension in the Y-coordinate.
	 */
	public void setDimCanvas(int dimXCanvas, int dimYCanvas){
		this.dimXCanvas=dimXCanvas;
		this.dimYCanvas=dimYCanvas;
		setRoutersPosition();
	}

	/**
	 * Sets the standard configuration to all routers
	 * @param isMapCores
	 */
	public void setStandardConfigToRouters(boolean isMapCores){
		Router r;
		for(int i=0;i<size();i++){
			r=get(i);
			//The serial core does not send traffic
			if(!isMapCores || (isMapCores && !r.getCore().equalsIgnoreCase("None") && !r.getCore().equalsIgnoreCase("Serial"))){
				r.getTraffic().setTraffics(standardTraffic);
				//it is not possible to send traffic to itself
				if(r.getAddress().equalsIgnoreCase(standardTraffic.getTarget())){
					r.getTraffic().disable();
				}
			}
		}
	}

	/**
	 * Returns a selected group of parameters in a single string.
	 * @param isMapCores
	 * @return s
	 */
	public String getSelectedInfo(boolean isMapCores){
		String s = "";
		Router router;
		for(int i=0; i<size(); i++){
			router = get(i);
			//The serial core does not send traffic
			if(!isMapCores || (isMapCores && !router.getCore().equalsIgnoreCase("None") && !router.getCore().equalsIgnoreCase("Serial"))){
				s = s + router.getSelectedInfo();
			}
		}
		return s;
	}

	/**
	 * Verify if exists a router associate to core. 
	 * @param core
	 * @return True if exists a router associate to core.
	 */
	public boolean existsCore(String core){
		Router router;
		for (int i=0; i<this.size(); i++){
			router = get(i);
			if(router.getCore().equalsIgnoreCase(core))
				return true;
		}
		return false;
	}
	
	/**
	* delete all files and sub directories containing in the scenery directory, except the scenery description file.
	* @param parent The scenery parent directory. <i> The scenery does not know its path.</i> 
	*/
	public void delete(String parent){
		String sceneryDir = parent + File.separator + getName();
		String sceneryFile = sceneryDir + File.separator + getName() + ".traffic";
		ManipulateFile.deleteFiles(sceneryDir, sceneryFile);
	}


	/**
	 * Use to test Scenery methods.
	 * @param args The list of arguments
	 */
	public static void main(String args[]){
		Scenery s = new Scenery(3,3,false);
		s.getStandardTraffic().setUniformRate(100);
		s.setStandardConfigToRouters(false);
		System.out.println(s.getSelectedInfo(false));
		
//		File f = new File("/home/aline/Programmes/scenery.txt");
//		s.setName("traffic");
//		System.out.println(s.getInfo());
//		s.save(f);
//		s.open(f);
//		System.out.println(s.getInfo());
		

	}

}
