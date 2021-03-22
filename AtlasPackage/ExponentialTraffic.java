package AtlasPackage;
import java.io.*;
import javax.swing.JOptionPane;

/**
 * This class contains the Exponential traffic specific parameters and methods.  	
 * @author Raffael Bottoli Schemmer
 * @version  
 * 
*/	
public class ExponentialTraffic extends Traffic
{
	/** indicates the transmission average rate of the cores in Mbps.*/
	private double averageRate;

	/** indicates the transmission minimum rate of the cores in Mbps.*/
	private double minimalRate;

	/** indicates the transmission maximum rate of the cores in Mbps*/
	private double maximalRate;

	/** indicates the increment used in the rate generation between the minimum and maximum rates*/
	private double increment;
	
	/**
	 * Creates a Exponential traffic object with the default configuration. 
	 */
	public ExponentialTraffic()
	{
		super();
		averageRate = 0.0;
		minimalRate = 150.0;
		maximalRate = 250.0;
		increment = 10.0;
	}

	/**
	 * Creates a Traffic with the informed parameters.
	 * @param frequency The transmission frequency (MHz)
	 * @param target The target router of the packets.
	 * @param priority The traffic priority.
	 * @param numberOfPackets The number of packets.
	 * @param packetSize The packet of flit that compose with packet.
	 * @param averageRate The transmission average rate of the cores in Mbps.
	 * @param minimalRate The transmission minimum rate of the cores in Mbps.
	 * @param maximalRate The transmission maximum rate of the cores in Mbps.
	 * @param increment The increment used in the rate generation between the minimum and maximum rates.
	 */
	public ExponentialTraffic(double frequency, String target, int priority, int numberOfPackets, int packetSize, double averageRate,double minimalRate,double maximalRate, double increment){
		super(frequency, target, priority, numberOfPackets, packetSize);
		this.averageRate = averageRate;
		this.minimalRate = minimalRate;
		this.maximalRate = maximalRate;
		this.increment = increment;
	}

	/**
	 * Returns the transmission average rate of the cores in Mbps.
	 * @return The average rate.
	 */
	public double getAverageRate(){ return averageRate; }
		
	/**
	 * Returns the transmission minimal rate of the cores in Mbps.
	 * @return The minimal rate
	 */
	public double getMinimalRate(){ return minimalRate; }

	/**
	 * Returns the transmission maximal rate of the cores in Mbps.
	 * @return The maximal rate.
	 */
	public double getMaximalRate(){ return maximalRate; }

	/**
	 * Returns the increment used in the rate generation between the minimum and maximum rates.
	 * @return The increment.
	 */
	public double getIncrement(){ return increment; }
	
	/**
	 * Sets the transmission average rate of the cores in Mbps.
	 * @param d
	 */
	public void setAverageRate(double d){ averageRate = d; }
		
	/**
	 * Sets the transmission minimal rate of the cores in Mbps.
	 * @param d
	 */
	public void setMinimalRate(double d){ minimalRate = d; }

	/**
	 * Sets the transmission maximal rate of the cores in Mbps.
	 * @param d
	 */
	public void setMaximalRate(double d){ maximalRate = d; }

	/**
	 * Sets the increment used in the rate generation between the minimum and maximum rates.
	 * @param d
	 */
	public void setIncrement(double d){ increment = d; }

	/**
	 * Sets the informed parameters to the traffic.
	 * @param frequency The transmission frequency (MHz)
	 * @param target The target router of the packets.
	 * @param priority The traffic priority.
	 * @param numberOfPackets The number of packets.
	 * @param packetSize The packet of flit that compose with packet.
	 * @param averageRate The transmission average rate of the cores in Mbps.
	 * @param minimalRate The transmission minimum rate of the cores in Mbps.
	 * @param maximalRate The transmission maximum rate of the cores in Mbps.
	 * @param standardDeviation the standard deviation of the average rate in Mbps.
	 * @param increment The increment used in the rate generation between the minimum and maximum rates.
	 */
	public void set(double frequency, String target, int priority, int numberOfPackets, int packetSize, double averageRate,double minimalRate,double maximalRate, double increment){
		super.set(frequency, target, priority, numberOfPackets, packetSize);
		this.averageRate = averageRate;
		this.minimalRate = minimalRate;
		this.maximalRate = maximalRate;
		this.increment = increment;
	}
	
	/**
	 * Sets the informed parameters to the traffic.
	 * @param averageRate The transmission average rate of the cores in Mbps.
	 * @param minimalRate The transmission minimum rate of the cores in Mbps.
	 * @param maximalRate The transmission maximum rate of the cores in Mbps.
	 * @param standardDeviation the standard deviation of the average rate in Mbps.
	 * @param increment The increment used in the rate generation between the minimum and maximum rates.
	 */
	public void set(double averageRate,double minimalRate,double maximalRate, double increment){
		this.averageRate = averageRate;
		this.minimalRate = minimalRate;
		this.maximalRate = maximalRate;
		this.increment = increment;
	}
	
	/**
	 * Sets the traffic parameters equals to the informed traffic. 
	 * @param t A Exponential traffic
	 */
	public void set(ExponentialTraffic t){
		set(t.getFrequency(),
			t.getTarget(),
			t.getPriority(),
			t.getNumberOfPackets(),
			t.getPacketSize(),
			t.getAverageRate(),
			t.getMinimalRate(),
			t.getMaximalRate(),
			t.getIncrement());
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
		if(pos==0) return getInfo("",tag);
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
		info = super.getInfo(tag1, tag2);
		info = info + "\n{" + tag1 + "AverageRate" + tag2 + "}" + averageRate + 
						"\n{" + tag1 + "MinimalRate" + tag2 + "}" + minimalRate +
						"\n{" + tag1 + "MaximalRate" + tag2 + "}" + maximalRate +
						"\n{" + tag1 + "Increment" + tag2 + "}" + increment;
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
			averageRate = Double.parseDouble(Default.readInfo(file, tag1 + "AverageRate" + tag2));
			minimalRate = Double.parseDouble(Default.readInfo(file, tag1 + "MinimalRate"+ tag2));
			maximalRate = Double.parseDouble(Default.readInfo(file, tag1 + "MaximalRate"+ tag2));
			increment = Double.parseDouble(Default.readInfo(file, tag1 + "Increment"+ tag2));
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
		ExponentialTraffic t = new ExponentialTraffic();
		System.out.println(t.getInfo());
		System.out.println(t.getInfo(1,"23"));
		System.out.println(t.getInfo());
		System.out.println("Frequency = " + t.getFrequency());
		System.out.println("Target = " + t.getTarget());
		System.out.println("Priority = " + t.getPriority());
		System.out.println("Number of Packets = " + t.getNumberOfPackets());
		System.out.println("Packet Size = " + t.getPacketSize());
		System.out.println("Average Rate = " + t.getAverageRate());
		System.out.println("Minimal Rate = " + t.getMinimalRate());
		System.out.println("Maximal Rate = " + t.getMaximalRate());
		System.out.println("Increment = " + t.getIncrement());
	}
}
