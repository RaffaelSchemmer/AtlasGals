package AtlasPackage;

import java.io.File;

import javax.swing.JOptionPane;

/**
 * This Class implements all parameters and methods related to a router of the NoC.  
 * @author Aline Vieira de Mello
 * @version
 */
public class Router{
	/** indicates the core associate to router */
	private String core;
	/** indicates the router address in format XY */
	private String address;
	/** indicates the router address in X-dimension of the NoC */
	private int addressX;
	/** indicates the router address in X-dimension of the NoC */
	private int addressY;
	
	//router position
	/** indicates the initial position of router in the X-coordinate */
	private int initialX;
	/** indicates the initial position of router in the Y-coordinate */
	private int initialY;
	/** indicates the final position of router in the X-coordinate */
	private int finalX;
	/** indicates the final position of router in the Y-coordinate */
	private int finalY;

	// traffic
	private static final int TAG_FRONT = 1;
	private RouterTraffic traffic; 
	
	// output port of a traffic
	private int port;
	
	
	/** Number of ports of a router */
	public final static int NPORTS = 5;
	/** EAST Port */
	public final static int EAST = 0;
	/** WEST Port */
	public final static int WEST = 1;
	/** NORTH Port */
	public final static int NORTH = 2;
	/** SOUTH Port */
	public final static int SOUTH = 3;
	/** LOCAL Port */
	public final static int LOCAL = 4;

	/** Router in Top-Left of NoC */
	public final static String ROUTERTL = "RouterTL";
	/** Router in Top-Center of NoC */
	public final static String ROUTERTC = "RouterTC";
	/** Router in Top-Right of NoC */
	public final static String ROUTERTR = "RouterTR";
	/** Router in Center-Left of NoC */
	public final static String ROUTERCL = "RouterCL";
	/** Router in Center-Center of NoC */
	public final static String ROUTERCC = "RouterCC";
	/** Router in Center-Right of NoC */
	public final static String ROUTERCR = "RouterCR";
	/** Router in Bottom-Left of NoC */
	public final static String ROUTERBL = "RouterBL";
	/** Router in Bottom-Center of NoC */
	public final static String ROUTERBC = "RouterBC";
	/** Router in Bottom-Right of NoC */
	public final static String ROUTERBR = "RouterBR";
	
	/**
	 * Creates a router (addressX,addressY) using output port to send a traffic.
	 * This constructor is used to build a traffic path.
	 * @param addressX The router address in X-dimension of the NoC 
	 * @param addressY The router address in X-dimension of the NoC
	 * @param port The output port used by a traffic
	 */
	public Router(int addressX,int addressY, int port){
		address = "" + addressX + addressY;
		this.addressX = addressX;
		this.addressY = addressY;
		this.port = port;
	}
	/**
	 * Creates a router (addressX,addressY) with the default parameters.
	 * @param addressX The router address in X-dimension of the NoC 
	 * @param addressY The router address in X-dimension of the NoC
	 */
	public Router(int addressX,int addressY){
		configure(addressX, addressY, 0, 0, 0, 0, false);
	}
	/**
	 * Creates a router (addressX,addressY) with the default parameters.
	 * @param addressX The router address in X-dimension of the NoC 
	 * @param addressY The router address in X-dimension of the NoC
	 * @param isSR4 Determine if the router belongs to a SR NoC with 4VCs
	 */
	public Router(int addressX,int addressY, boolean isSR4){
		configure(addressX, addressY, 0, 0, 0, 0, isSR4);
	}
	/**
	 * Creates a router with the default parameters
	 * @param addressX The router address in X-dimension of the NoC 
	 * @param addressY The router address in X-dimension of the NoC
	 * @param x1 The initial position of router in X-coordinate
	 * @param y1 The initial position of router in Y-coordinate
	 * @param x2 The final position of router in X-coordinate
	 * @param y2 The final position of router in Y-coordinate
	 */
	public Router(int addressX,int addressY, int x1, int y1, int x2, int y2){
		configure(addressX, addressY, x1, y1, x2, y2, false);
	}
	/**
	 * Creates a router with the default parameters
	 * @param addressX The router address in X-dimension of the NoC 
	 * @param addressY The router address in X-dimension of the NoC
	 * @param x1 The initial position of router in X-coordinate
	 * @param y1 The initial position of router in Y-coordinate
	 * @param x2 The final position of router in X-coordinate
	 * @param y2 The final position of router in Y-coordinate
	 * @param isSR4 Determine if the router belongs to a SR NoC with 4VCs
	 */
	public Router(int addressX,int addressY, int x1, int y1, int x2, int y2, boolean isSR4){
		configure(addressX, addressY, x1, y1, x2, y2, isSR4);
	}

	/**
	 * Configures the router with the default parameters
	 * @param addressX The router address in X-dimension of the NoC 
	 * @param addressY The router address in X-dimension of the NoC
	 * @param x1 The initial position of router in X-coordinate
	 * @param y1 The initial position of router in Y-coordinate
	 * @param x2 The final position of router in X-coordinate
	 * @param y2 The final position of router in Y-coordinate
	 * @param isSR4 Determine if the router belongs to a SR NoC with 4VCs
	 */
	public void configure(int addressX,int addressY, int x1, int y1, int x2, int y2, boolean isSR4){
		core="None";
		this.addressX=addressX;
		this.addressY=addressY;
		address=""+addressX+addressY;
		setPosition(x1,y1,x2,y2);
		traffic = new RouterTraffic(isSR4);
	}
	
	/**
	 * Returns the router address. 
	 * @return address
	 */
	public String getAddress(){ return address;}	
	
	/**
	 * Returns the router address in X-dimension of the NoC. 
	 * @return addressX
	 */
	public int getAddressX(){ return addressX;}	
	
	/**
	 * Returns the router address in Y-dimension of the NoC. 
	 * @return addressY
	 */
	public int getAddressY(){ return addressY;}	

	/**
	 * Returns the port used to send a traffic. 
	 * @return The output port
	 */
	public int getPort(){ return port;}	

	/**
	 * Returns the core name associate to the router. 
	 * @return core
	 */
	public String getCore(){ return core;}	

	/**
	 * Returns the initial position of router in X-coordinate. 
	 * @return initialX
	 */
	public int getInitialX(){ return initialX;}	
	
	/**
	 * Returns the initial position of router in Y-coordinate. 
	 * @return initialX
	 */
	public int getInitialY(){ return initialY;}
	
	/**
	 * Returns the final position of router in X-coordinate. 
	 * @return finalX
	 */
	public int getFinalX(){ return finalX;}	

	/**
	 * Returns the final position of router in Y-coordinate. 
	 * @return finalY
	 */
	public int getFinalY(){ return finalY;}	

	/**
	 * Return the router traffic. 
	 * @return A router traffic.
	 */
	public RouterTraffic getTraffic(){ return traffic;}	
	
	/**
	 * Sets the core name associate to the router. 
	 * @param s
	 */
	public void setCore(String s){ core = s;}	

	/**
	 * Sets the router position. 
	 * @param x1 The initial position of router in X-coordinate
	 * @param y1 The initial position of router in Y-coordinate
	 * @param x2 The final position of router in X-coordinate
	 * @param y2 The final position of router in Y-coordinate
	 */
	public void setPosition(int x1, int y1, int x2, int y2){
		initialX=x1;
		initialY=y1;
		finalX=x2;
		finalY=y2;
	}	
	
	/**
	* Returns the Router parameters in a single string. 
	* Each parameter is concatenated the String in a different line using the format: <i>{label}value</i> <br>
	* <i>label</i> corresponds to the parameter name with the upper-case first letter. 
	* @return s
	*/
	public String getInfo(){
		String info;
		info = "\n{Router" + address + "}" +
				"\n{Router" + address + "_Core}" + core;
		info = info + traffic.getInfo(TAG_FRONT, "Router" + address + "_");
		return info;
	}

	/**
	* Returns a selected group of traffic distribution parameters in a single string.
	* @return s
	*/
	public String getSelectedInfo(){
		if(traffic.getRate()!=0){ //test if the rate of selected distribution is different to zero
			return "Router " + address + traffic.getSelectedInfo();
		}
		return "";
	}
	
	/**
	 * Read from file all traffic parameters. <br>
	 * Each file line has the format: <i>{label}value</i> <br>
	 * <i>label</i> corresponds to the parameter name with the upper-case first letter. 
	 * @param file
	 */
	public void readParameters(File file){
		try{
			core = Default.readInfo(file, "Router" + address + "_Core");
			traffic.readParameters(file, TAG_FRONT, "Router" + address + "_");
		} catch(Exception e){
			JOptionPane.showMessageDialog(null,e.getMessage(), "Error Message", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}

	/**
	* Tests if x,y position belongs to router.
	* @param x 
	* @param y 
	* @return True is point belongs to router.
	*/
	public boolean verifyPoint(int x,int y){
		if(x>=initialX && x<=finalX && y>=initialY && y<=finalY)
			return true;
		return false;
	}
	
	/**
	 * Return the router type. <br>
	 * Considering the X-dimension, a router can be: Left(L), Center(C) or Right(R). <br>
	 * Considering the Y-dimension, a router can be: Bottom(B), Center(C) or Top(T). <br>
	 * The combination of these possibilities generates the follows router types:
	 * RouterBL, RouterBC, RouterBR,
	 * RouterCL, RouterCC, RouterCR,
	 * RouterTL, RouterTC, RouterTR. <br>
	 * @param x The router address in X-dimension of NoC.  
	 * @param y The router address in Y-dimension of NoC.
	 * @param dimX The X-dimension of NoC.  
	 * @param dimY The Y-dimension of NoC.
	 * @return The router type.  
  	 */
	public static String getRouterType(int x,int y,int dimX,int dimY){
		String routerType;
		if (x==0){
			if (y==0)             routerType = ROUTERBL;
			else if (y==(dimY-1)) routerType = ROUTERTL;
			else                  routerType = ROUTERCL;
		}
		else if(x==(dimX-1)){
			if(y==0)             routerType = ROUTERBR;
			else if(y==(dimY-1)) routerType = ROUTERTR;
			else                 routerType = ROUTERCR;
		}
		else{
			if(y==0)             routerType = ROUTERBC;
			else if(y==(dimY-1)) routerType = ROUTERTC;
			else                 routerType = ROUTERCC;
		}
		return routerType;
	}

	/**
	 * Return the upper-case port name associate to the number of informed port.
	 * @param port The number of port.
	 * @param format The letter case. 0 = lowercase; 1 = Capitalize;
	 * 2 = !cAPITALIZE; others = UPPERCASE.
	 * @return The port name associate to the number of informed port.
	 */
	public static String getPortName(int port, int format){
		String upper, lower;
		String name = getPortName(port);
		switch(format) {
		case 0: name = name.toLowerCase(); break;
		case 1: // first in upper case (capitalize)
			upper = name.substring(0,1).toUpperCase();
			lower = name.substring(1).toLowerCase();
			name  = upper + lower;
			break;
		case 2: // first in lower case
			lower = name.substring(0,1).toLowerCase();
			upper = name.substring(1).toUpperCase();
			name  = lower + upper;
			break;
		default: name = name.toUpperCase(); break;
		}
		return name;
	}

	
	
	/**
	 * Return the port name associate to the number of informed port.
	 * @param port The number of port.
	 * @return The port name associate to the number of informed port.
	 */
	public static String getPortName(int port){
		switch(port){
		case EAST: return "EAST";
		case WEST: return "WEST";
		case NORTH: return "NORTH";
		case SOUTH: return "SOUTH";
		case LOCAL: return "LOCAL";
		default: 
			System.out.println("Error in Router.getPortName : Indefined port number "+ port);
			System.exit(0);
			break;
		}
		return "";
	}
	

	/**
	 * Return true if the router has the informed port.
	 * @param port The number of port.
	 * @param x The router position in X-dimension of NoC.  
	 * @param y The router position in Y-dimension of NoC.
	 * @param dimX The X-dimension of NoC.  
	 * @param dimY The Y-dimension of NoC.
	 * @return True if the router has the informed port.
  	 */
	public static boolean hasPort(int port, int x,int y,int dimX,int dimY){
		String routerType;
		if (x==0){
			if (y==0)             routerType = ROUTERBL;
			else if (y==(dimY-1)) routerType = ROUTERTL;
			else                  routerType = ROUTERCL;
		}
		else if(x==(dimX-1)){
			if(y==0)             routerType = ROUTERBR;
			else if(y==(dimY-1)) routerType = ROUTERTR;
			else                 routerType = ROUTERCR;
		}
		else{
			if(y==0)             routerType = ROUTERBC;
			else if(y==(dimY-1)) routerType = ROUTERTC;
			else                 routerType = ROUTERCC;
		}
		return hasPort(routerType,port);
	}
	
	
	
	/**
	 * Return true if the informed router has the informed port.
	 * @param routerType The type of router. For instance: ROUTERBL (Bottom-Left).
	 * @param port The number of port.
	 * @return True if the informed router has the informed port.
	 */
	public static boolean hasPort(String routerType, int port){
		switch(port){
		case EAST: return hasEastPort(routerType);
		case WEST: return hasWestPort(routerType);
		case NORTH: return hasNorthPort(routerType);
		case SOUTH: return hasSouthPort(routerType);
		case LOCAL: return true;
		default: 
			System.out.println("Error in Router.hasPort : Indefined port number "+ port);
			System.exit(0);
			break;
		}
		return false;
	}
	
	/**
	 * Return true if the informed router has the EAST port.
	 * @param routerType The type of router. For instance: ROUTERBL (Bottom-Left).
	 * @return True if the informed router has the EAST port.
	 */
	public static boolean hasEastPort(String routerType){
		if(routerType.endsWith("R")) //router in the right has no EAST port
			return false;
		return true;
	}

	/**
	 * Return true if the informed router has the WEST port.
	 * @param routerType The type of router. For instance: ROUTERBL (Bottom-Left).
	 * @return True if the informed router has the WEST port.
	 */
	public static boolean hasWestPort(String routerType){
		if(routerType.endsWith("L")) //router in the left has no WEST port
			return false;
		return true;
	}

	/**
	 * Return true if the informed router has the NORTH port.
	 * @param routerType The type of router. For instance: ROUTERBL (Bottom-Left).
	 * @return True if the informed router has the NORTH port.
	 */
	public static boolean hasNorthPort(String routerType){
		 //router in the top has no NORTH port
		if(routerType.endsWith("TR") || routerType.endsWith("TC") || routerType.endsWith("TL"))
			return false;
		return true;
	}

	/**
	 * Return true if the informed router has the SOUTH port.
	 * @param routerType The type of router. For instance: ROUTERBL (Bottom-Left).
	 * @return True if the informed router has the SOUTH port.
	 */
	public static boolean hasSouthPort(String routerType){
		 //router in the bottom has no SOUTH port
		if(routerType.endsWith("BR") || routerType.endsWith("BC") || routerType.endsWith("BL"))
			return false;
		return true;
	}

	/**
	 * Use to test Scenery methods.
	 * @param args The list of arguments
	 */
	public static void main(String args[]){
		//System.out.println(Router.removeLast("aaaaaa t,abc\n", ","));
		
	}

}
