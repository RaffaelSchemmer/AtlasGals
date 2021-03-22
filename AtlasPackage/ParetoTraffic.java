package AtlasPackage;

import java.io.*;

import javax.swing.JOptionPane;

/**
 * This class contains the Pareto traffic specific parameters and methods.  	
 * @author Aline Vieira de Mello
 * @version  
 * 
*/	
public class ParetoTraffic extends Traffic{
	/** indicates the transmission rate (in megabit per second) during the On period.*/ 
	private double rateOnPeriod;
	/** indicates the number of packets bursts.*/
	private int burstSize;

	/**
	 * Creates a Uniform traffic object with the default configuration. 
	 */
	public ParetoTraffic(){
		super();
		rateOnPeriod = 0;
		burstSize = 10;
	}

	/**
	 * Creates a Traffic with the informed parameters.
	 * @param frequency The transmission frequency (MHz)
	 * @param target The target router of the packets.
	 * @param priority The traffic priority.
	 * @param numberOfPackets The number of packets.
	 * @param packetSize The packet of flit that compose with packet.
	 * @param rateOnPeriod The transmission rate (in megabit per second) during the On period.
	 * @param burstSize The number of packets bursts.
	 */
	public ParetoTraffic(double frequency, String target, int priority, int numberOfPackets, int packetSize, double rateOnPeriod, int burstSize){
		super(frequency, target, priority, numberOfPackets, packetSize);
		this.rateOnPeriod = rateOnPeriod;
		this.burstSize = burstSize;
	}

	/**
	 * Returns the transmission rate (in megabit per second) during the On period.
	 * @return The rate during on period.
	 */
	public double getRateOnPeriod(){ return rateOnPeriod; }

	/**
	 * Returns the number of packets bursts.
	 * @return The number of packets bursts.
	 */
	public int getBurstSize(){ return burstSize; }

	/**
	/* Sets the transmission rate in Mbps (Megabit per second).
	 * @param r
	 */
	public void setRateOnPeriod(double r){ rateOnPeriod = r; }

	/**
	 * Sets the number of packets bursts.
	 * @param n
	 */
	public void setBurstSize(int n){ burstSize = n; }
	
	/**
	 * Sets the informed parameters to the traffic.
	 * @param frequency The transmission frequency (MHz)
	 * @param target The target router of the packets.
	 * @param priority The traffic priority.
	 * @param numberOfPackets The number of packets.
	 * @param packetSize The packet of flit that compose with packet.
	 * @param rateOnPeriod The transmission rate (in megabit per second) during the On period.
	 * @param burstSize The number of packets bursts.
	 */
	public void set(double frequency, String target, int priority, int numberOfPackets, int packetSize, double rateOnPeriod, int burstSize){
		set(frequency, target, priority, numberOfPackets, packetSize);
		this.rateOnPeriod = rateOnPeriod;
		this.burstSize = burstSize;
	}
	
	/**
	 * Sets the traffic parameters equals to the informed traffic. 
	 * @param t A Pareto traffic.
	 */
	public void set(ParetoTraffic t){
		set(t.getFrequency(),
			t.getTarget(),
			t.getPriority(),
			t.getNumberOfPackets(),
			t.getPacketSize(),
			t.getRateOnPeriod(),
			t.getBurstSize());
	}	
	
	/**
	 * Returns a String with all traffic parameters. <br>
	 * Each parameter is concatenated the String in a different line using the format: <i>{label}value</i> <br>
	 * <i>label</i> corresponds to the parameter name with the upper-case first letter. 
	 * @return info
	 */
	public String getInfo(){
		return getInfo("", "");
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
		else  return getInfo(tag, "");
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
		String info = super.getInfo(tag1, tag2);
		info = info + "\n{" + tag1 + "RateOnPeriod" + tag2 + "}" + rateOnPeriod + 
					  "\n{" + tag1 + "BurstSize" + tag2 + "}" + burstSize;
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
			rateOnPeriod = Double.parseDouble(Default.readInfo(file, tag1 + "RateOnPeriod" + tag2));
			burstSize = Integer.parseInt(Default.readInfo(file, tag1 + "BurstSize" + tag2));
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
		ParetoTraffic t = new ParetoTraffic();
		System.out.println(t.getInfo());
		System.out.println(t.getInfo(1,"23"));
		t.set(30, "10", 1, 5, 16, 100, 20);
		System.out.println(t.getInfo());
		System.out.println("Frequency = " + t.getFrequency());
		System.out.println("Target = " + t.getTarget());
		System.out.println("Priority = " + t.getPriority());
		System.out.println("Number of Packets = " + t.getNumberOfPackets());
		System.out.println("Packet Size = " + t.getPacketSize());
		System.out.println("Rate on Period = " + t.getRateOnPeriod());
		System.out.println("Burst Size = " + t.getBurstSize());
		
	}
}