package AtlasPackage;

import java.io.*;

/**
 * This class contains the SR4 traffic specific parameters and methods. <br>
 * The SR4 Traffic is composed by three Uniform traffics: CTRL, GS and BE. <br>
 * The Uniform traffic parameters:
 * <li> Frequency, Target, Priority are equal in CTRL, GS and BE Traffics.
 * <li> Number of packets, Packet size and Rate are independents. <br>  <br>
 *   
 * @author Aline Vieira de Mello
 * @version  
 * 
*/	
public class SR4Traffic{
	private UniformTraffic ctrl;
	private UniformTraffic gs;
	private UniformTraffic be;
	/** Indicates the CTRL service */
	public static final int CTRL = 0;
	/** Indicates the GS service */
	public static final int GS = 1;
	/** Indicates the BE service */
	public static final int BE = 2;
	
	/**
	 * Creates a SR4 traffic object with the default configuration. <br>
	 */
	public SR4Traffic(){
		ctrl = new UniformTraffic();
		gs = new UniformTraffic();
		be = new UniformTraffic();
		ctrl.setNumberOfPackets(16);
		gs.setNumberOfPackets(30);
		be.setNumberOfPackets(200);
	}

	/**
	 * Returns the traffic of the service informed.
	 * @param service CTRL=0 GS=1 and BE=2
	 * @return traffic
	 */
	public UniformTraffic getTraffic(int service){
		switch(service) {
		case CTRL: return ctrl;
		case GS: return gs;
		case BE: return be;
		}
		return null;
	}
	
	/**
	 * Sets the informed parameters to CTRL, GS and BE traffic.
	 * @param frequency The transmission frequency (MHz)
	 * @param target The target router of the packets.
	 * @param priority The traffic priority.
	 * @param numberOfPackets The number of packets.
	 * @param packetSize The packet of flit that compose with packet.
	 * @param rate The transmission rate of the core in Mbps. 
	 */
	public void set(double frequency, String target, int priority, int numberOfPackets, int packetSize, double rate){
		ctrl.set(frequency, target, priority, numberOfPackets, packetSize, rate);
		gs.set(frequency, target, priority, numberOfPackets, packetSize, rate);
		be.set(frequency, target, priority, numberOfPackets, packetSize, rate);
	}

	/**
	 * Sets the traffic parameters equals to the informed traffic. 
	 * @param t A SR4 traffic.
	 */
	public void set(SR4Traffic t){
		ctrl.set(t.getTraffic(CTRL));
		gs.set(t.getTraffic(GS));
		be.set(t.getTraffic(BE));
	}	

	/**
	 * Sets the frequency to all traffics. 
	 * @param frequency The transmission frequency (MHz)
	 */
	public void setFrequency(double frequency){
		ctrl.setFrequency(frequency);
		gs.setFrequency(frequency);
		be.setFrequency(frequency);
	}	

	/**
	 * Sets the frequency to all traffics. 
	 * @param target The target router of the packets.
	 */
	public void setTarget(String target){
		ctrl.setTarget(target);
		gs.setTarget(target);
		be.setTarget(target);
	}	

	/**
	 * Sets the traffic priority to all traffics. <br>
	 * This parameter is visible only when the NoC has virtual channels and uses priority-based scheduling. <br>
	 * The traffic priority can be chosen among the values zero and n-1, where n is the number of virtual channels.
	 * @param p
	 */
	public void setPriority(int p){
		ctrl.setPriority(p);
		gs.setPriority(p);
		be.setPriority(p);
	}	
	
	/**
	 * Sets the number of transmitted packets to all traffics.
	 * @param n The number of packets
	 */
	public void setNumberOfPackets(int n){ 
		ctrl.setNumberOfPackets(n);
		gs.setNumberOfPackets(n);
		be.setNumberOfPackets(n);
	}	
	
	/**
	 * Sets the number of transmitted packets to the informed service.
	 * @param service CTRL=0 GS=1 and BE=2
	 * @param n The number of packets
	 */
	public void setNumberOfPackets(int service, int n){ 
		switch(service){
		case CTRL: ctrl.setNumberOfPackets(n); break;
		case GS: gs.setNumberOfPackets(n); break;
		case BE: be.setNumberOfPackets(n); break;
		}
	}	
	
	/**
	 * Sets the packet size to all traffics. 
	 * @param n packet size
	 */
	public void setPacketSize(int n){
		ctrl.setPacketSize(n);
		gs.setPacketSize(n);
		be.setPacketSize(n);
	}	

	/**
	 * Sets the packet size to the informed service. 
	 * @param service CTRL=0 GS=1 and BE=2
	 * @param n packet size
	 */
	public void setPacketSize(int service, int n){
		switch(service){
		case CTRL: ctrl.setPacketSize(n); break;
		case GS: gs.setPacketSize(n); break;
		case BE: be.setPacketSize(n); break;
		}
	}	

	/**
	/* Sets the transmission rate to all traffics. 
	 * @param rate The transmission rate in Mbps
	 */
	public void setRate(double rate){
		ctrl.setRate(rate);
		gs.setRate(rate);
		be.setRate(rate);
	}	

	/**
	/* Sets the transmission rate to the informed service. 
	 * @param service CTRL=0 GS=1 and BE=2
	 * @param rate The transmission rate in Mbps
	 */
	public void setRate(int service, double rate){
		switch(service){
		case CTRL: ctrl.setRate(rate); break;
		case GS: gs.setRate(rate); break;
		case BE: be.setRate(rate); break;
		}
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
	 * Returns a String with the parameters of the service informed. <br> 
	 * Each parameter is concatenated the String in a different line using the format: <i>{label}value</i> <br>
	 * <i>label</i> corresponds to the parameter name with the upper-case first letter. 
	 * @param service CTRL=0 GS=1 and BE=2
	 * @return info
	 */
	public String getInfo(int service){
		String s = "";
		switch(service){
		case CTRL: s = ctrl.getInfo(); break;
		case GS: s = gs.getInfo(); break;
		case BE: s = be.getInfo(); break;
		}
		return s;
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
		return ctrl.getInfo(pos, tag) + gs.getInfo(pos, tag) + be.getInfo(pos, tag);
	}

	/**
	 * Returns a String with the parameters of the service informed. <br> 
	 * Each parameter is concatenated the String in a different line using the format: <i>{label}value</i> <br>
	 * <i>label</i> corresponds to the parameter name with the upper-case first letter. 
	 * @param service CTRL=0 GS=1 and BE=2
	 * @param pos The position where the <i>tag</i> will be add. When pos=0 tag is add in the end of each label,  parameter.
	 * @param tag An extension option for <b>all</b> labels.
	 * @return info
	 */
	public String getInfo(int service, int pos, String tag){
		String s = "";
		switch(service){
		case CTRL: s = ctrl.getInfo(pos, tag); break;
		case GS: s = gs.getInfo(pos, tag); break;
		case BE: s = be.getInfo(pos, tag); break;
		}
		return s;
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
		return ctrl.getInfo(tag1, tag2) + gs.getInfo(tag1, tag2) + be.getInfo(tag1, tag2);
	}

	/**
	 * Returns a String with the parameters of the service informed. <br> 
	 * Each parameter is concatenated the String in a different line using the format: <i>{label}value</i> <br>
	 * <i>label</i> corresponds to the parameter name with the upper-case first letter. 
	 * @param service CTRL=0 GS=1 and BE=2
	 * @param tag1 An extension option for <b>all</b> labels. <i>tag1</i> is add in the front of each label.
	 * @param tag2 An extension option for <b>all</b> labels. <i>tag2</i> is add in the end of each label.
	 * @return info
	 */
	public String getInfo(int service, String tag1, String tag2){
		String s = "";
		switch(service){
		case CTRL: s = ctrl.getInfo(tag1, tag2); break;
		case GS: s = gs.getInfo(tag1, tag2); break;
		case BE: s = be.getInfo(tag1, tag2); break;
		}
		return s;
	}
	
	/**
	 * Read from file all traffic parameters.
	 * Each file line has the format: <i>{label}value</i> <br>
	 * <i>label</i> corresponds to the parameter name with the upper-case first letter. 
	 * @param file
	 */
	public void readParameters(File file){
		readParameters(file, "", "");
	}

	/**
	 * Read from file the parameters of the service informed. <br> 
	 * Each file line has the format: <i>{label}value</i> <br>
	 * <i>label</i> corresponds to the parameter name with the upper-case first letter. 
	 * @param service CTRL=0 GS=1 and BE=2
	 * @param file
	 */
	public void readParameters(int service, File file){
		readParameters(service, file, "", "");
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
	 * Read from file the parameters of the service informed. <br>
	 * Each file line has the format: <i>{label}value</i> <br>
	 * <i>label</i> corresponds to the parameter name with the upper-case first letter. 
	 * @param service CTRL=0 GS=1 and BE=2
	 * @param file
	 * @param pos The position where the <i>tag</i> will be add. When pos=0 tag is add in the end of each label,  parameter.
	 * @param tag An extension option for <b>all</b> labels.
	 */
	public void readParameters(int service, File file, int pos, String tag){
		if(pos==0)
			readParameters(service, file, "", tag);
		else
			readParameters(service, file, tag, "");
	}
	
	/**
	 * Read from file the parameters of all traffics. <br> 
	 * Each file line has the format: <i>{label}value</i> <br>
	 * <i>label</i> corresponds to the parameter name with the upper-case first letter. 
	 * @param file
	 * @param tag1 An extension option for <b>all</b> labels. <i>tag1</i> is add in the front of each label.
	 * @param tag2 An extension option for <b>all</b> labels. <i>tag2</i> is add in the end of each label.
	 */
	public void readParameters(File file, String tag1, String tag2){
		ctrl.readParameters(file, tag1, tag2);
		gs.readParameters(file, tag1, tag2);
		be.readParameters(file, tag1, tag2);
	}
	
	/**
	 * Read from file the parameters of the service informed. <br> 
	 * Each file line has the format: <i>{label}value</i> <br>
	 * <i>label</i> corresponds to the parameter name with the upper-case first letter. 
	 * @param service CTRL=0 GS=1 and BE=2
	 * @param file
	 * @param tag1 An extension option for <b>all</b> labels. <i>tag1</i> is add in the front of each label.
	 * @param tag2 An extension option for <b>all</b> labels. <i>tag2</i> is add in the end of each label.
	 */
	public void readParameters(int service, File file, String tag1, String tag2){
		switch(service){
		case CTRL: ctrl.readParameters(file, tag1, tag2); break;
		case GS: gs.readParameters(file, tag1, tag2); break;
		case BE: be.readParameters(file, tag1, tag2); break;
		}
	}

	/**
	 * Returns the transmission frequency (MHz). <br>
	 * <i>The Frequency parameter is the same in all traffics.</i>
	 * @return frequency
	 */
	public double getFrequency(){ return ctrl.getFrequency(); }
	
	/**
	 * Returns the target router of the packets. <br>
	 * <i>The target parameter is the same in all traffics.</i>
	 * @return target
	 */
	public String getTarget(){ return ctrl.getTarget(); }
	
	/**
	 * Returns the traffic priority.
	 * <i>The priority parameter is the same in all traffics.</i>
	 * @return priority
	 */
	public int getPriority(){ return ctrl.getPriority(); }
	
	/**
	* Returns the number of packets of the informed service.
	* @param service CTRL=0 GS=2 BE=3
	* @return n
	*/
	public int getNumberOfPackets(int service){
		switch(service){
		case CTRL: 
			return ctrl.getNumberOfPackets();
		case GS: 
			return gs.getNumberOfPackets();
		case BE: 
			return be.getNumberOfPackets();
		}
		return 0;
	}	

	/**
	* Returns the total number of packets.
	* The traffic with rate equals to zero are not considered.
	* @return n
	*/
	public int getTotalNumberOfPackets(){
		int n = 0;
		if(ctrl.getRate()!=0)
			n = n + ctrl.getNumberOfPackets();
		if(gs.getRate()!=0)
			n = n + gs.getNumberOfPackets();
		if(be.getRate()!=0)
			n = n + be.getNumberOfPackets();
		return n;
	}	

	/**
	* Returns the packet size of the informed service.
	* @param service CTRL=0 GS=2 BE=3
	* @return n
	*/
	public int getPacketSize(int service){
		switch(service){
		case CTRL: 
			return ctrl.getPacketSize();
		case GS: 
			return gs.getPacketSize();
		case BE: 
			return be.getPacketSize();
		}
		return 0;
	}	
	
	/**
	* Returns the average packet size. 
	* The traffic with rate equals to zero are not considered.
	* @return The average packet size
	*/
	public double getAveragePacketSize(){
		int n = 0, i = 0;
		if(ctrl.getRate()!=0){ 
			n = n + ctrl.getPacketSize();
			i++;
		}
		if(gs.getRate()!=0){
			n = n + gs.getPacketSize();
			i++;
		}
		if(be.getRate()!=0){
			n = n + be.getPacketSize();
			i++;
		}
		if (n==0)
			return 0;
		return n/i;
	}	

	/**
	* Returns the rate of the informed service.
	* @param service CTRL=0 GS=2 BE=3
	* @return n
	*/
	public double getRate(int service){
		switch(service){
		case CTRL: return ctrl.getRate();
		case GS: return gs.getRate();
		case BE: return be.getRate();
		}
		return 0;
	}	
	
	/**
	* Returns the average rate. 
	* The traffic with rate equal to zero are not considered.
	* @return The average rate
	*/
	public double getAverageRate(){
		double n = 0;
		int i = 0;
		if(ctrl.getRate()!=0){ 
			n = n + ctrl.getRate();
			i++;
		}
		if(gs.getRate()!=0){
			n = n + gs.getRate();
			i++;
		}
		if(be.getRate()!=0){
			n = n + be.getRate();
			i++;
		}
		if (n==0)
			return 0;
		return n/i;
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