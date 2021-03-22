package AtlasPackage;

import java.io.*;

import javax.swing.JOptionPane;

/**
 * This class contains the uniform traffic specific parameters and methods.  	
 * @author Aline Vieira de Mello
 * @version  
 * 
*/	
public class UniformTraffic extends Traffic{
	/** indicates the transmission rate in Mbps */
	private double rate;
    
	/**
	 * Creates a Uniform traffic object with the default configuration. 
	 */
	public UniformTraffic(){
		super();
		rate = 0;
	}

	/**
	 * Creates a Traffic with the informed parameters.
	 * @param frequency The transmission frequency (MHz)
	 * @param target The target router of the packets.
	 * @param priority The traffic priority.
	 * @param numberOfPackets The number of packets.
	 * @param packetSize The packet of flit that compose with packet.
	 * @param rate The transmission rate of the core in Mbps. 
	 */
	public UniformTraffic(double frequency, String target, int priority, int numberOfPackets, int packetSize, double rate){
		super(frequency, target, priority, numberOfPackets, packetSize);
		this.rate = rate;
	}

	/**
	 * Returns the transmission rate of the core in Mbps.
	 * @return The rate.
	 */
	public double getRate(){ return rate; }
		
	/**
	 * Sets the transmission rate in Mbps (Megabit per second).<br>
	 * @param r
	 */
	public void setRate(double r){ rate = r; }
		
	/**
	 * Sets the informed parameters to the traffic.
	 * @param frequency The transmission frequency (MHz)
	 * @param target The target router of the packets.
	 * @param priority The traffic priority.
	 * @param numberOfPackets The number of packets.
	 * @param packetSize The packet of flit that compose with packet.
	 * @param rate The transmission rate of the core in Mbps. 
	 */
	public void set(double frequency, String target, int priority, int numberOfPackets, int packetSize, double rate){
		super.set(frequency, target, priority, numberOfPackets, packetSize);
		this.rate = rate;
	}
	
	/**
	 * Sets the traffic parameters equals to the informed traffic. 
	 * @param t An Uniform traffic
	 */
	public void set(UniformTraffic t){
		set(t.getFrequency(), 
			t.getTarget(),
			t.getPriority(),
			t.getNumberOfPackets(),
			t.getPacketSize(),
			t.getRate());
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
		if(pos==0) return getInfo("", tag);
		else return getInfo(tag, "");
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
		String info;
		info = super.getInfo(tag1,tag2);
		info = info + ("\n{" + tag1 + "Rate" + tag2 + "}" + rate);
		return info;
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
		super.readParameters(file, tag1, tag2);
		try{
			rate = Double.parseDouble(Default.readInfo(file, tag1 + "Rate" + tag2));
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
		UniformTraffic t = new UniformTraffic();
		System.out.println(t.getInfo());
		System.out.println(t.getInfo(1,"23"));
		t.set(30, "10", 1, 5, 16, 100);
		System.out.println(t.getInfo());
		System.out.println("Frequency = " + t.getFrequency());
		System.out.println("Target = " + t.getTarget());
		System.out.println("Priority = " + t.getPriority());
		System.out.println("Number of Packets = " + t.getNumberOfPackets());
		System.out.println("Packet Size = " + t.getPacketSize());
		System.out.println("Rate = " + t.getRate());
	}
}
