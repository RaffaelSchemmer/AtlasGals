package AtlasPackage;

import java.io.*;
import javax.swing.JOptionPane;

/**
 * This class contains the traffic common parameters.  	
 * @author Aline Vieira de Mello
 * @version  
 * 
*/	
public class Traffic{
	/** indicates the transmission frequency (MHz)*/
	private double frequency;
    /** indicates the target router of the packets.*/
	private String target;
	/** indicates the traffic priority.*/
	private int priority;
    /** indicates the number of packets.*/
	private int numberOfPackets;
	/** indicates the number of flits that compose each packet.*/
	private int packetSize;
 
	/** Uniform traffic */ 
	public static String UNIFORM = "Uniform";
	/** Normal traffic */ 
	public static String NORMAL = "Normal";
	/** Uniform traffic */ 
	public static String PARETO = "ParetoOn/Off";
	/** Uniform traffic */ 
	public static String EXPONENTIAL = "Exponential";
	
	
	/**
	 * Creates a Traffic object with the default configuration. 
	 */
	public Traffic(){
		frequency = 50.0;
		target = "random";
		priority = 0;
		numberOfPackets=10;
		packetSize = 16;
	}

	/**
	 * Creates a Traffic with the informed parameters.
	 * @param frequency The transmission frequency (MHz)
	 * @param target The target router of the packets.
	 * @param priority The traffic priority.
	 * @param numberOfPackets The number of packets.
	 * @param packetSize The packet of flit that compose with packet.
	 */
	public Traffic(double frequency, String target, int priority, int numberOfPackets, int packetSize){
		this.frequency = frequency;
		this.target = target;
		this.priority = priority;
		this.numberOfPackets=numberOfPackets;
		this.packetSize = packetSize;
	}

	/**
	 * Returns the transmission frequency (MHz).
	 * @return frequency
	 */
	public double getFrequency(){ return frequency; }
	
	/**
	 * Returns the target router of the packets.
	 * @return target
	 */
	public String getTarget(){ return target; }
	
	/**
	 * Returns the traffic priority.
	 * @return priority
	 */
	public int getPriority(){ return priority; }
	
	/**
	 * Returns the number of transmitted packets.
	 * @return numberOfPackets
	 */
	public int getNumberOfPackets(){ return numberOfPackets; }
	
	/**
	 * Returns the number of flits that compose each packet.
	 * @return packetSize
	 */
	public int getPacketSize(){ return packetSize; }
	
	/**
	 * Sets the transmission frequency (MHz).
	 * @param f
	 */
	public void setFrequency(double f){ frequency = f; }

	/**
	 * Sets the target router of the packets. <br>
	 * Amongst the target options are:
	 * <li> random, where each generated packet has a target router chosen randomly;
	 * <li> complement, where all packets have the same target router and this target router is defined according to the complement traffic pattern:
	 * <li> one of the routers of the NoC (excluding the source router), where all packets have the same target router.
	 * @param s
	 */
	public void setTarget(String s){ target=s; }
	
	/**
	 * Sets the traffic priority. <br>
	 * This parameter is visible only when the NoC has virtual channels and uses priority-based scheduling. <br>
	 * The traffic priority can be chosen among the values zero and n-1, where n is the number of virtual channels.
	 * @param p
	 */
	public void setPriority(int p){ priority = p; }

	/**
	 * Sets the number of transmitted packets.
	 * @param n
	 */
	public void setNumberOfPackets(int n){ numberOfPackets = n; }
	
	/**
	 * Sets the number of flits that compose each packet.
	 * @param n
	 */
	public void setPacketSize(int n){ packetSize = n; }
		
	/**
	 * Sets the informed parameters to the traffic.
	 * @param frequency The transmission frequency (MHz)
	 * @param target The target router of the packets.
	 * @param priority The traffic priority.
	 * @param numberOfPackets The number of packets.
	 * @param packetSize The packet of flit that compose with packet.
	 */
	public void set(double frequency, String target, int priority, int numberOfPackets, int packetSize){
		this.frequency = frequency;
		this.target = target;
		this.priority = priority;
		this.numberOfPackets=numberOfPackets;
		this.packetSize = packetSize;
	}
	
	/**
	 * Returns a String with all traffic parameters. <br>
	 * Each parameter is concatenated the String in a different line using the format: <i>{label}value</i> <br>
	 * <i>label</i> corresponds to the parameter name with the upper-case first letter. 
	 * @return info
	 */
	public String getInfo(){
		return getInfo("","");
	}

	/**
	 * Returns a String with all traffic parameters. <br>
	 * Each parameter is concatenated the String in a different line using the format: <i>{label}value</i> <br>
	 * <i>label</i> corresponds to the parameter name with the upper-case first letter. 
	 * @param pos The position where the <i>tag</i> will be add. When pos=0 tag is add in the end of each label,  parameter.
	 * @param tag An extension option for <b>all</b> labels.
	 * @return info
	 */
	public String getInfo(int pos, String tag){
		if(pos==0) return getInfo("",tag);
		return getInfo(tag,"");
	}

	/**
	 * Returns a String with all traffic parameters. <br>
	 * Each parameter is concatenated the String in a different line using the format: <i>{label}value</i> <br>
	 * <i>label</i> corresponds to the parameter name with the upper-case first letter. 
	 * @param tag1 An extension option for <b>all</b> labels. <i>tag1</i> is add in the front of each label.
	 * @param tag2 An extension option for <b>all</b> labels. <i>tag2</i> is add in the end of each label.
	 * @return info
	 */
	public String getInfo(String tag1, String tag2){
			return ("\n{" + tag1 + "Frequency" + tag2 + "}"+frequency+
			"\n{" + tag1 + "Target" + tag2 + "}"+target+
			"\n{" + tag1 + "Priority" + tag2 + "}"+priority+
			"\n{" + tag1 + "NumberOfPackets" + tag2 + "}"+numberOfPackets+
			"\n{" + tag1 + "PacketSize" + tag2 + "}"+packetSize);
	}
	
	/**
	 * Read from file all traffic parameters. <br>
	 * Each file line has the format: <i>{label}value</i> <br>
	 * <i>label</i> corresponds to the parameter name with the upper-case first letter. 
	 * @param file
	 */
	public void readParameters(File file){
		readParameters(file, "", "");
	}

	/**
	 * Read from file all traffic parameters. <br>
	 * Each file line has the format: <i>{label}value</i> <br>
	 * <i>label</i> corresponds to the parameter name with the upper-case first letter. 
	 * @param file
	 * @param pos The position where the <i>tag</i> will be add. When pos=0 tag is add in the end of each label,  parameter.
	 * @param tag An extension option for <b>all</b> labels.
	 */
	public void readParameters(File file, int pos, String tag){
		if(pos==0)
			readParameters(file, "", tag);
		else
			readParameters(file, tag, "");
	}

	/**
	 * Read from file all traffic parameters. <br>
	 * Each file line has the format: <i>{label}value</i> <br>
	 * <i>label</i> corresponds to the parameter name with the upper-case first letter. 
	 * @param file
	 * @param tag1 An extension option for <b>all</b> labels. <i>tag1</i> is add in the front of each label.
	 * @param tag2 An extension option for <b>all</b> labels. <i>tag2</i> is add in the end of each label.
	 */
	public void readParameters(File file, String tag1, String tag2){
		try{
			frequency = Double.parseDouble(Default.readInfo(file, tag1 + "Frequency" + tag2));
			target = Default.readInfo(file, tag1 + "Target" + tag2);
			priority = Integer.parseInt(Default.readInfo(file, tag1 + "Priority" + tag2));
			numberOfPackets = Integer.parseInt(Default.readInfo(file, tag1 + "NumberOfPackets" + tag2));
			packetSize = Integer.parseInt(Default.readInfo(file, tag1 + "PacketSize" + tag2));
		} catch(Exception e){
			JOptionPane.showMessageDialog(null,e.getMessage(), "Error Message", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}
	
	/**
	 * Use to test Traffic methods.
	 * @param s The list of arguments
	 */
	public static void main(String s[]){
		Traffic t = new Traffic();
		System.out.println(t.getInfo());
		System.out.println(t.getInfo(0,"23"));
		t.set(30, "10", 1, 4, 16);
		System.out.println(t.getInfo());
		System.out.println("Frequency = " + t.getFrequency());
		System.out.println("Target = " + t.getTarget());
		System.out.println("Priority = " + t.getPriority());
		System.out.println("Number of Packets = " + t.getNumberOfPackets());
		System.out.println("Packet Size = " + t.getPacketSize());
	}
}
