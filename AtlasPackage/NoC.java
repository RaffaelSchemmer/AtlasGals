package AtlasPackage;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.awt.*;


/**
 * <i>NoC</i> class contains the <b>23</b> parameters of a Network on Chip (NoC). <p>
 * 
 *  {Type}Hermes <br>
 *  {NumRotX}2 <br>
 *  {NumRotY}2 <br>
 *  {FlitSize}16 <br>
 *  {BufferDepth}16 <br>
 *  {FlowControl}CreditBased <br>
 *  {VirtualChannel}2 <br>
 *  {RoutingAlgorithm}XY <br>
 *  {CyclesPerFlit}1 <br>
 *  {CyclesToRoute}5 <br>
 *  {Scheduling}RoundRobin <br>
 *  {AdmissionControl}false <br>
 *  {SCTB}true  <br>
 *  {Saboteur}false <br>
 *  {SaboteurTypes} <br>
 *  {CRCType} <br>
 */
public class NoC{

	private String type;
	private int numRotX,numRotY,mmc,traffic=0;
	private String flowControl,scheduling,routingAlgorithm;
	private String crcType;
	private String routing, CTRLRouting, GSRouting, BERouting;
	private int virtualChannel,flitSize,bufferDepth,cyclesPerFlit,cyclesToRoute;
	private boolean scTB,saboteur,admissionControl,dr,df,gn,gp;
	private ArrayList<Clock> clock = new ArrayList<Clock>();
	private ArrayList<AvailableClock> clock_list = new ArrayList<AvailableClock>();
	private String bufferCoding;
	
	/** Contains the NoC Hermes name */ 
	public static String HERMES = "Hermes";
	/** Contains the NoC HermesTU name */ 
	public static String HERMESTU = "HermesTU";
	/** Contains the NoC HermesTB name */ 
	public static String HERMESTB = "HermesTB";
	/** Contains the NoC HermesSR name */ 
	public static String HERMESSR = "HermesSR";
	/** Contains the NoC HermesCRC name */ 
	public static String HERMESCRC = "HermesCRC";
	/** Contains the NoC HermesG name */ 
	public static String HERMESG = "HermesG";
	/** Contains the NoC HermesGLP name */ 
	public static String HERMESGLP = "HermesGLP";
	/** Contains the NoC Mercury name */ 
	public static String MERCURY = "Mercury";
	/** Contains the Link CRC type name */ 
	public static String LINK_CRC = "Link CRC";
	/** Contains the Source CRC type name */ 
	public static String SOURCE_CRC = "Source CRC";
	/** Contains the Hamming CRC type name */ 
	public static String HAMMING_CRC = "Hamming";

	/** NoC with Handshake flow control. */  
	public final static int HS = 0;
	/** NoC with Credit based flow control and without virtual channels. */  
	public final static int CB = 1;
	/** NoC with Credit based flow control and virtual channels. */  
	public final static int VC = 2;
	
	/**
	 * Creates a new NoC <i>type</i> with the default parameters.
	 * @param type
	 */
	public NoC(String type){
		this.type = type;
		numRotX=3;
		numRotY=3;
		flitSize=16;
		bufferDepth=16;

		if(type.equalsIgnoreCase(MERCURY)){
			routingAlgorithm="CG";
			flowControl="HandShake";
			virtualChannel=1;
		}else {
			routingAlgorithm="XY";
			flowControl="CreditBased";
			virtualChannel=2;
		}
		
		cyclesPerFlit=1;
		cyclesToRoute=5;
		scTB=true;
		crcType="none";
		saboteur=false;
		dr=false;
		df=false;
		gn=false;
		gp=false;
		scheduling="RoundRobin";
		admissionControl=false;
		routing="0";
		CTRLRouting="0";
		GSRouting="0";
		BERouting="0";
	}

	/**
	 * Creates a new NoC with the parameters stored in file.
	 * @param file
	 */
	public NoC(File file){
		readParameters(file);
	}
	
	/**
	 * Returns the NoC type. 
	 * For instance: "Hermes", "Mercury", ...
	 * @return type
	 */
	public String getType(){return type;}
	
	/**
	 * Tests if the NoC is a HermesSR with 4 virtual channels. 
	 * @return True if the NoC is a HermesSR with 4 virtual channels.
	 */
	public boolean isSR4(){return (type.equalsIgnoreCase(HERMESSR) && virtualChannel==4);}
        
	/**
	 * Returns the NoC flow control.
	 * For instance: "CreditBased"
	 * @return flowControl
	 */
	public String getFlowControl(){return flowControl;}
	
	// 
	// Métodos de manipulação do Clock
	// 
	
	public void clearClock()
	{
		if(clock.size() > 0)
			clock.clear();
	}
	public void addClock(Clock c)
	{
		clock.add(c);
	}
	public ArrayList<Clock> getClock()
	{
		return(clock);
	}
	
	// 
	// Métodos de manipulação do AvailableClock
	// 
	
	public void setClockList(AvailableClock value)
	{
		clock_list.add(value);
	}
	public void addClockList(int index,AvailableClock value)
	{
		clock_list.add(index,value);
	}
	public void setClockList(int index,AvailableClock value)
	{
		clock_list.set(index,value);
	}
	public AvailableClock getClockList(int index)
	{
		return(clock_list.get(index));
	}
	public ArrayList<AvailableClock> getRefClockList()
	{
		return(clock_list);
	}
	public ArrayList<String> getRefClockListString()
	{
		ArrayList<String> word = new ArrayList<String>();
		for(int i=0;i<clock_list.size();i++) word.add(clock_list.get(i).getAllAvailableValue());
		return(word);
	}
	public void removeIndexClockList(int index)
	{
		clock_list.remove(index);
	}
	/* Inicializa a lista de clocks */
	public void initDefClocks(int selectX, int selectY)
	{
		for(int x=0;x<selectX;x++)
		{
			for(int y=0;y<selectY;y++)
			{
				Clock c = new Clock();
				c.setLabelClockRouter("defClock");
				c.setLabelClockIpInput("defClock");
				c.setLabelClockIpOutput("defClock");
				c.setClockRouter(50,"Mhz");
				c.setClockIpInput(50,"Mhz");
				c.setClockIpOutput(50,"Mhz");
				String word = "" + x + y;
				c.setNumberRouter(x,y);
				addClock(c);
			}
		}
	}
	/**
	 * Returns the number of virtual channels.
	 * @return n
	 */
	public int getVirtualChannel(){return virtualChannel;}
	
	/**
	 * Returns the number of routers in X-dimension of NoC.
	 * @return dimXNoC
	 */
	public int getNumRotX(){return numRotX;}
	
	/**
	 * Returns the number of routers in Y-dimension of NoC.
	 * @return dimYNoC
	 */
	public int getNumRotY(){return numRotY;}
	
	/**
	 * Returns the flit size.
	 * The ﬂit size is the same as the channel width of the NoC.   
	 * @return flitSize
	 */
	public int getFlitSize(){return flitSize;}

	/**
	 * Returns the router buffer depth. 
	 * @return bufferDepth
	 */
	public int getBufferDepth(){return bufferDepth;}
	
	/**
	 * Returns the routing algorithm.
	 * For instance: XY
	 * @return routingAlgorithm
	 */
	public String getRoutingAlgorithm(){return routingAlgorithm;}
	
	/**
	 * Returns the number of cycles used to transmit a flit. 
	 * For instance, the Hermes NoC with Handshake flow control uses two-clock cycles to transmit a flit. 
	 * @return cyclesPerFlit
	 */
	public int getCyclesPerFlit(){return cyclesPerFlit;}

	/**
	 * Returns the number of cycles used to route. 
	 * @return cyclesToRoute
	 */
	public int getCyclesToRoute(){return cyclesToRoute;}
	
	/**
	 * Returns the scheduling name.
	 * For instance: RoundRobin
	 * @return scheduling
	 */
	public String getScheduling(){return scheduling;}

	/**
	 * Test whether project has SystemC test benches. 
	 * @return b
	 */
	public boolean isSCTB(){return scTB;}
	
	/**
	 * Returns the CRC type. For instance: Link CRC
	 * Returns <i>none<\i> when CRC is not used. 
	 * @return crcType
	 */
	public String getCrcType(){return crcType;}
	
	/**
	 * Return true if the CRC type is Link.
	 * @return boolean
	 */
	public boolean isLinkCrc(){return crcType.equalsIgnoreCase(LINK_CRC);}

	/**
	 * Return true if the CRC type is Source.
	 * @return boolean
	 */
	public boolean isSourceCrc(){return crcType.equalsIgnoreCase(SOURCE_CRC);}

	/**
	 * Return true if the CRC type is Hamming.
	 * @return boolean
	 */
	public boolean isHammingCrc(){return crcType.equalsIgnoreCase(HAMMING_CRC);}
	
	/**
	 * Tests whether the saboteur is active.
	 * @return b
	 */
	public boolean isSaboteur(){return saboteur;}
	
	/**
	 * Tests whether the DR saboteur is active.
	 * @return b
	 */
	public boolean isDr(){return dr;}

	/**
	 * Tests whether the DF saboteur is active.
	 * @return b
	 */
	public boolean isDf(){return df;}
	
	/**
	 * Tests whether the GN saboteur is active.
	 * @return b
	 */
	public boolean isGn(){return gn;}
	
	/**
	 * Tests whether the saboteur is GP.
	 * @return b
	 */
	public boolean isGp(){return gp;}
	
	/**
	 * Tests whether the NoC has admission control.
	 * @return b
	 */
	public boolean isAdmissionControl(){return admissionControl;}
	
	/**
	 * 
	 * @return routing
	 */
	public String getRouting(){return routing;}
	
	/**
	 * Returns the CTRL routing. This parameter is valid only for HermesSR NoC.
	 * @return CTRLRouting
	 */
	public String getCTRLRouting(){return CTRLRouting;}

	/**
	 * Returns the GS routing. This parameter is valid only for HermesSR NoC.
	 * @return GSRouting
	 */
	public String getGSRouting(){return GSRouting;}

	/**
	 * Returns the BE routing. This parameter is valid only for HermesSR NoC.
	 * @return BERouting
	 */
	public String getBERouting(){return BERouting;}
		
	/**
	 * Set the flow control. For instance: CreditBased.
	 * @param s
	 */
	public void setFlowControl(String s){ flowControl=s;}

	/**
	 * Set the number of virtual channels.
	 * @param n
	 */
	public void setVirtualChannel(int n){ virtualChannel=n;}

	/**
	 * Set the number of routers in X-dimension of NoC.
	 * @param n
	 */
	public void setNumRotX(int n){numRotX=n;}

	/**
	 * Set the number of routers in Y-dimension of NoC.
	 * @param n
	 */
	public void setNumRotY(int n){numRotY=n;}
	
	/**
	 * Set the flit size, i.e. the number of bits of a flit (equals to channel width).
	 * @param n
	 */
	public void setFlitSize(int n){ flitSize=n;}
	
	/**
	 * Set the buffer depth.
	 * @param d
	 */
	public void setBufferDepth(int d){ bufferDepth=d;}
	
	/**
	 * Set the routing algorithm. For instance: XY.
	 * @param s
	 */
	public void setAlgorithm(String s){ routingAlgorithm=s;}

	/**
	 * Set the routing ??????
	 * @param r
	 */
	public void setRouting(String r){routing=r;}

	/**
	 * Set the CRTL routing. This parameter is set only by the HermesSR NoC
	 * @param r
	 */
	public void setCTRLRouting(String r){CTRLRouting=r;}

	/**
	 * Set the GS routing. This parameter is set only by the HermesSR NoC
	 * @param r
	 */
	public void setGSRouting(String r){GSRouting=r;}

	/**
	 * Set the BE routing. This parameter is set only by the HermesSR NoC
	 * @param r
	 */
	public void setBERouting(String r){BERouting=r;}
	
	/**
	 * Set the number of cycles waste to send a flit.
	 * @param n
	 */
	public void setCyclesPerFlit(int n){cyclesPerFlit=n;}

	/**
	 * Set the number of cycles waste to route a flit.
	 * @param n
	 */
	public void setCyclesToRoute(int n){cyclesToRoute=n;}
	
	/**
	 * Determines whether the SystemC test bench is generated.
	 * @param b
	 */
	public void setSCTB(boolean b){ scTB=b;}
	
	/**
	 * Set the CRC type. For instance: hamming.
	 * @param s
	 */
	public void setCrcType(String s){ crcType=s;}
	
	/**
	 * Determines whether the saboteur is active.
	 * @param b
	 */
	public void setSaboteur(boolean b){ saboteur=b;}
	
	/**
	 * Determines whether the DR saboteur is active.
	 * @param b
	 */
	public void setDr(boolean b){ dr=b;}

	/**
	 * Determines whether the DF saboteur is active.
	 * @param b
	 */
	public void setDf(boolean b){ df=b;}

	/**
	 * Determines whether the GN saboteur is active.
	 * @param b
	 */
	public void setGn(boolean b){ gn=b;}

	/**
	 * Determines whether the GP saboteur is active.
	 * @param b
	 */
	public void setGp(boolean b){ gp=b;}

	/**
	 * Set the scheduling police. For instance: RoundRobin.
	 * @param s
	 */
	public void setScheduling(String s){ scheduling=s;}
	
	/**
	 * Determines whether the admission control is active.
	 * @param b
	 */
	public void setAdmissionControl(boolean b){admissionControl=b;}
	
	public String getbufferCoding(){
		return(bufferCoding);
	}
	public void setbufferCoding(String vbufferCoding){
		bufferCoding = vbufferCoding;
	}
	
	/* Retorna todos os valores do ArrayList Clocks a serem escritos no arquivo .noc */
	public String getClockParameters()
	{
		String word = new String();
		int i;
		word = word + "{Noc Clocks}";
		for(i=0;i < clock.size();i++)
		{
			word = word + "\n" + clock.get(i).getNumberRouterX() + " " + clock.get(i).getNumberRouterY() + " " + clock.get(i).getLabelClockRouter() + " " + clock.get(i).getClockRouter() + " " + clock.get(i).getUnitRouter() + " " + clock.get(i).getLabelClockIpInput() + " " + clock.get(i).getClockIpInput() + " " + clock.get(i).getUnitIpInput() + " " + clock.get(i).getLabelClockIpOutput() + " " + clock.get(i).getClockIpOutput() + " " + clock.get(i).getUnitIpOutput();
		}
		return(word + "\n");
	}		
	
	/* Retorna todos os valores do ArrayList Clocks a serem escritos no arquivo .noc */
	public String getAvailableClockParameters()
	{
		String word = new String();
		int i;
		word = word + "{Available Clocks}";
		for(i=0;i < clock_list.size();i++)
		{
			word = word + "\n" + clock_list.get(i).getAllAvailableValue();
		}
		return(word);
	}
	public String getBufferCodingParameter(){
		return("{BufferCoding}" + bufferCoding + "\n");
	}
	/**
	* Returns the all NoC parameters in a String.
	* @return s
	*/
	public String getParameters(){
		return(
				"{NoC}" +
				"\n{Type}"+type+
				"\n{NumRotX}"+numRotX+
				"\n{NumRotY}"+numRotY+
				"\n{FlitSize}"+flitSize+
				"\n{BufferDepth}"+bufferDepth+
				"\n{FlowControl}"+flowControl+
				"\n{VirtualChannel}"+virtualChannel+
				"\n{Algorithm}"+routingAlgorithm+
				"\n{Routing}"+routing+
				"\n{CTRLRouting}"+CTRLRouting+
				"\n{GSRouting}"+GSRouting+
				"\n{BERouting}"+BERouting+
				"\n{CyclesPerFlit}"+cyclesPerFlit+
				"\n{CyclesToRoute}"+cyclesToRoute+
				"\n{Scheduling}"+scheduling+
				"\n{AdmissionControl}"+admissionControl+
				"\n{CRCType}"+crcType+
				"\n{Saboteur}"+Boolean.toString(saboteur)+
				"\n{DR}"+Boolean.toString(dr)+
				"\n{DF}"+Boolean.toString(df)+
				"\n{GN}"+Boolean.toString(gn)+
				"\n{GP}"+Boolean.toString(gp)+
				"\n{SCTB}"+Boolean.toString(scTB));
	}
	public int getTraffic()
	{
		if(traffic == 1) return(1);
		return(0);
	}
	public void setTraffic(int vTraffic)
	{
		traffic = vTraffic;
	}
	/**
	 * Read NoC parameters in informed file.
	 * @param file
	 */
	public void readParameters(File file){
		try{
			type = Default.readInfo(file, "Type");
			numRotX = Integer.parseInt(Default.readInfo(file, "NumRotX"));
			numRotY = Integer.parseInt(Default.readInfo(file, "NumRotY"));
			flitSize = Integer.parseInt(Default.readInfo(file, "FlitSize"));
			bufferDepth = Integer.parseInt(Default.readInfo(file, "BufferDepth"));
			flowControl = Default.readInfo(file, "FlowControl");
			virtualChannel = Integer.parseInt(Default.readInfo(file, "VirtualChannel"));
			routingAlgorithm = Default.readInfo(file, "Algorithm");
			routing = Default.readInfo(file, "Routing");
			CTRLRouting = Default.readInfo(file, "CTRLRouting");
			GSRouting = Default.readInfo(file, "GSRouting");
			BERouting = Default.readInfo(file, "BERouting");
			cyclesPerFlit = Integer.parseInt(Default.readInfo(file, "CyclesPerFlit"));
			cyclesToRoute = Integer.parseInt(Default.readInfo(file, "CyclesToRoute"));
			scheduling = Default.readInfo(file, "Scheduling");
			admissionControl = Boolean.parseBoolean(Default.readInfo(file, "AdmissionControl"));
			crcType = Default.readInfo(file, "CRCType");
			saboteur = Boolean.parseBoolean(Default.readInfo(file, "Saboteur"));
			dr = Boolean.parseBoolean(Default.readInfo(file, "DR"));
			df = Boolean.parseBoolean(Default.readInfo(file, "DF"));
			gn = Boolean.parseBoolean(Default.readInfo(file, "GN"));
			gp = Boolean.parseBoolean(Default.readInfo(file, "GP"));
			scTB = Boolean.parseBoolean(Default.readInfo(file, "SCTB"));
		} catch(Exception e){
			JOptionPane.showMessageDialog(null,e.getMessage(),"Error in read NoC Parameters", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}
}
