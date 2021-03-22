package AtlasPackage;

import java.io.File;

import javax.swing.JOptionPane;

/**
 * This Class implements the parameters and methods related to the router traffic. <br>
 * A router traffic can have one the following traffic distribution: Uniform, Normal and Pareto On/Off. <br>
 * The exception is the router belongs to SR NoC with 4 virtual channels.  
 * This router has 3 Uniform traffic: CTRL, GS and BE. <br>
 * The boolean isSR4 determines if the router belongs to SR NoC using 4 VCs. <br><br>
 *
 * Considering a <b>not SR4</B> router:
 * <li> The Frequency, Target, Priority, Number of Size and Packet Size parameters 
 * have the same value in Uniform, Normal and Pareto traffic.
 * <li> The others parameters are specified in each traffic. <br><br>
 * 
 * Considering a <b>SR4</b> router:
 * <li> The Frequency, Target, Priority parameters have the same value for the 3 Uniform traffics. 
 * <li> The others parameters are specified in each traffic. <br><br>
 *  
 * @author Aline Vieira de Mello
 * @version
 */
public class RouterTraffic{
	/** Indicates the traffic distribution used by the router */
	private String distribution;
	/** indicates if the router belongs to a SR4 (with 4VC) NoC */
	private boolean isSR4;
	/** SR4 Traffic is composed by three Uniform traffics: CTRL, GS and BE */
	private SR4Traffic sr4Traffic;
	/** Traffic with Uniform Distribution */
	private UniformTraffic uniformTraffic;
	/** Traffic with Normal Distribution */
	private NormalTraffic normalTraffic;
	/** Traffic with Pareto Distribution */
	private ParetoTraffic paretoTraffic;
	/** Traffic with Exponential Distribution */
	private ExponentialTraffic exponentialTraffic;
	
	private static final int CTRL = SR4Traffic.CTRL, GS = SR4Traffic.GS, BE = SR4Traffic.BE;
	private static final String UNIFORM = Traffic.UNIFORM, NORMAL = Traffic.NORMAL, PARETO = Traffic.PARETO, EXPONENTIAL =Traffic.EXPONENTIAL;

	/**
	 * Creates a router traffic with the default parameters.
	 * @param address The router address in XY format. 
	 * @param isSR4 Determine if the router belongs to a SR NoC with 4VCs
	 */
	public RouterTraffic(boolean isSR4){
		this.isSR4 = isSR4;
		distribution = UNIFORM;
		uniformTraffic = new UniformTraffic();
		normalTraffic = new NormalTraffic();
		paretoTraffic = new ParetoTraffic();
		exponentialTraffic = new ExponentialTraffic();
		sr4Traffic = new SR4Traffic();
	}
	/**
	 * Returns true if traffic belongs to a SR4 router. 
	 * @return boolean
	 */
	public boolean isSR4(){ return isSR4;}	
	
	/**
	 * Returns the traffic distribution used by the router. 
	 * @return distribution
	 */
	public String getDistribution(){ return distribution;}	
	
	/**
	 * Returns the transmission frequency (MHz). <br>
	 * The frequency has the same value in all traffics.
	 * @return n
	 */
	public double getFrequency(){
		return uniformTraffic.getFrequency();
	}	

	/**
	 * Return the target according to the traffic distribution. <br>
	 * The target has the same value in all traffics.
	 * @return The Target of packets
	 */
	public String getTarget(){
		return uniformTraffic.getTarget();
	}	

	/**
	 * Return the priority according to the traffic distribution.
	 * The priority has the same value in all traffics.
	 * @return The traffic Priority
	 */
	public int getPriority(){
		return uniformTraffic.getPriority();
	}	

	/**
	* Returns the number of packets transmitted by the router.
	* The number of packets has the same value in Uniform, Normal and Pareto traffics.
	* <b>This method is not applicable to SR4 router.</b>
	* @return n
	*/
	public int getNumberOfPackets(){
		if(isSR4){
			System.out.println("The getNumberOfPackets() method is not applicable to SR4 traffic.\n" +
								"Please use the getNumberOfPackets(int service)");
			System.exit(0);
			return 0;
		}
		return uniformTraffic.getNumberOfPackets();
	}	

	/**
	 * Return the number of packets according to the service informed.
	 * @param service CTRL=0 GS=2 BE=3
	 * @return The number of packets.
	 */
	public int getNumberOfPackets(int service){
		return sr4Traffic.getNumberOfPackets(service);
	}	
	
	/**
	* Returns the total number of packets transmitted by the router. <br>
	* The traffic with rate equals to zero are not considered.
	* @return n
	*/
	public int getTotalNumberOfPackets(){
		if(isSR4)
			return sr4Traffic.getTotalNumberOfPackets();
		if(distribution.equalsIgnoreCase(UNIFORM) && (uniformTraffic.getRate()!=0))
			return uniformTraffic.getNumberOfPackets();
		else if(distribution.equalsIgnoreCase(NORMAL) && (normalTraffic.getAverageRate()!=0))
			return normalTraffic.getNumberOfPackets();
			
		else if(distribution.equalsIgnoreCase(EXPONENTIAL) && (exponentialTraffic.getAverageRate()!=0))
			return exponentialTraffic.getNumberOfPackets();	
			
		else if(distribution.equalsIgnoreCase(PARETO) && (paretoTraffic.getRateOnPeriod()!=0))
			return paretoTraffic.getNumberOfPackets();
		else 
			return 0;
	}	
	
	/**
	 * Return the packet size. <br>
	 * The packet size has the same value in Uniform, Normal and Pareto traffics.
	 * <b>This method is not applicable to SR4 router.</b>
	 * @return The traffic Priority
	 */
	public int getPacketSize(){
		if(isSR4){
			System.out.println("The getPacketSize() method is not applicable to SR4 traffic.\n" +
								"Please use the getPacketSze(int service)");
			System.exit(0);
			return 0;
		}
		return uniformTraffic.getPacketSize();
	}	

	/**
	 * Return the packet size according to the service informed.
	 * @param service CTRL=0 GS=2 BE=3
	 * @return The packet size
	 */
	public int getPacketSize(int service){
		return sr4Traffic.getPacketSize(service);
	}	
	
	/**
	 * Return the packet size according to the traffic distribution. <br>
	 * The traffic with rate equal to zero are not considered. <br>
	 * <b>If router is SR4 the average is returned.</b>
	 * @return The packet size. Except to SR4 router that return the average of packet size.
	 */
	public double getAveragePacketSize(){
		if(isSR4)
			return sr4Traffic.getAveragePacketSize();
		else if(distribution.equalsIgnoreCase(UNIFORM) && uniformTraffic.getRate()!=0)
			return uniformTraffic.getPacketSize();
		else if(distribution.equalsIgnoreCase(NORMAL) && normalTraffic.getAverageRate()!=0)
			return normalTraffic.getPacketSize();
		else if(distribution.equalsIgnoreCase(EXPONENTIAL) && exponentialTraffic.getAverageRate()!=0)
			return exponentialTraffic.getPacketSize();
		else if(distribution.equalsIgnoreCase(PARETO) && paretoTraffic.getRateOnPeriod()!=0)
			return paretoTraffic.getPacketSize();
		else
			return 0;
	}	

	/**
	 * Return the rate according to the traffic distribution. <br>
	 * If the router is a SR4 the average rate is return. <br>
	 * <b>Attention: Do not confuse with the rate parameter of Uniform traffic.</b>
	 * @return The rate, except to SR4 router. In this case the average rate among CTRL, GS and BE is returned.
	 */
	public double getRate(){
		if(isSR4)
			return sr4Traffic.getAverageRate();
		else if(distribution.equalsIgnoreCase(UNIFORM))
			return uniformTraffic.getRate();
		else if(distribution.equalsIgnoreCase(NORMAL))
			return normalTraffic.getAverageRate();
		else if(distribution.equalsIgnoreCase(EXPONENTIAL))
			return exponentialTraffic.getAverageRate();
		else if(distribution.equalsIgnoreCase(PARETO))
			return paretoTraffic.getRateOnPeriod();
		else
			return 0;
	}	

	/**
	 * Return the transmission rate of the Uniform traffic distribution. <br>
	 * <b>This method is not applicable to SR4 router.</b>
	 * @return n
	 */
	public double getUniformRate(){
		if (isSR4){
			System.out.println("The getUniformRate() method is not applicable to SR4 traffic.\n" +
								"Please use the getUniformRate(int service)");
			System.exit(0);
			return 0;
		}
		else
			return uniformTraffic.getRate();
	}	

	/**
	 * Return the transmission rate according to the informed service. <br>
	 * @param service CTRL=0 GS=2 BE=3
	 * @return The rate of service.
	 */
	public double getUniformRate(int service){
		return sr4Traffic.getRate(service);
	}	
	
	public double getExponentialAverageRate(){ return exponentialTraffic.getAverageRate(); }
	public double getExponentialMinimalRate(){ return exponentialTraffic.getMinimalRate(); }	
	public double getExponentialMaximalRate(){ return exponentialTraffic.getMaximalRate(); }
	public double getExponentialIncrement(){ return exponentialTraffic.getIncrement(); }
	
	/**
	 * Returns the transmission average rate in the Normal traffic distribution.
	 * @return The average rate.
	 */
	public double getNormalAverageRate(){ return normalTraffic.getAverageRate(); }
		
	/**
	 * Returns the transmission minimal rate.
	 * @return the minimal rate.
	 */
	public double getNormalMinimalRate(){ return normalTraffic.getMinimalRate(); }

	/**
	 * Returns the transmission maximal rate.
	 * @return The maximal rate.
	 */
	public double getNormalMaximalRate(){ return normalTraffic.getMaximalRate(); }

	/**
	 * Returns the standard deviation of the average rate.
	 * @return The standard deviation.
	 */
	public double getNormalStandardDeviation(){ return normalTraffic.getStandardDeviation(); }

	/**
	 * Returns the increment used in the rate generation between the minimum and maximum rates.
	 * @return The increment.
	 */
	public double getNormalIncrement(){ return normalTraffic.getIncrement(); }

	/**
	 * Returns the transmission rate during the On period.
	 * @return rateOnPeriod
	 */
	public double getParetoRateOnPeriod(){ return paretoTraffic.getRateOnPeriod(); }

	/**
	 * Returns the number of packets bursts.
	 * @return The number of packets bursts.
	 */
	public int getParetoBurstSize(){ return paretoTraffic.getBurstSize(); }
	
	/**
	 * Returns the Uniform traffic. 
	 * @return An Uniform traffic
	 */
	public UniformTraffic getUniformTraffic(){ return uniformTraffic;}	
	
	/**
	 * Returns the Normal traffic. 
	 * @return An Normal traffic
	 */
	public NormalTraffic getNormalTraffic(){ return normalTraffic;}	

	public ExponentialTraffic getExponentialTraffic(){ return exponentialTraffic;}	
	
	/**
	 * Returns the Pareto traffic. 
	 * @return An Pareto traffic
	 */
	public ParetoTraffic getParetoTraffic(){ return paretoTraffic;}	
	
	/**
	 * Returns the SR4 traffic. This traffic is composed by three Uniform traffics: CTRL, GS and BE. 
	 * @return A SR4 traffic
	 */
	public SR4Traffic getSR4Traffic(){ return sr4Traffic;}	

	/**
	 * Sets the configuration from informed router traffic to this object. 
	 * @param rt A router traffic object.
	 */
	public void setTraffics(RouterTraffic rt){
		distribution = rt.getDistribution();
		setFrequency(rt.getFrequency());
		setTarget(rt.getTarget());
		setPriority(rt.getPriority());
		if(isSR4){
			setNumberOfPackets(CTRL, rt.getNumberOfPackets(CTRL));
			setNumberOfPackets(GS, rt.getNumberOfPackets(GS));
			setNumberOfPackets(BE, rt.getNumberOfPackets(BE));
			setPacketSize(CTRL,rt.getPacketSize(CTRL));
			setPacketSize(GS,rt.getPacketSize(GS));
			setPacketSize(BE,rt.getPacketSize(BE));
			setUniformRate(CTRL, rt.getUniformRate(CTRL));
			setUniformRate(GS, rt.getUniformRate(GS));
			setUniformRate(BE, rt.getUniformRate(BE));
		}
		else{
			setNumberOfPackets(rt.getNumberOfPackets());
			setPacketSize(rt.getPacketSize());
			setUniformRate(rt.getUniformRate());
		}
		setNormalAverageRate(rt.getNormalAverageRate());
		setNormalMinimalRate(rt.getNormalMinimalRate());
		setNormalMaximalRate(rt.getNormalMaximalRate());
		setNormalStandardDeviation(rt.getNormalStandardDeviation());
		setNormalIncrement(rt.getNormalIncrement());
		setParetoRateOnPeriod(rt.getParetoRateOnPeriod());
		setParetoBurstSize(rt.getParetoBurstSize());
		
		setExponentialAverageRate(rt.getExponentialAverageRate());
		setExponentialMinimalRate(rt.getExponentialMinimalRate());
		setExponentialMaximalRate(rt.getExponentialMaximalRate());
		setExponentialIncrement(rt.getExponentialIncrement());
	}	
	
	/**
	 * Sets the traffic distribution.
	 * @param t
	 */
	public void setDistribution(String t){distribution=t;}
	
	/**
	 * Sets the transmission frequency (MHz).
	 * The frequency has the same value in all traffics.
	 * @param frequency
	 */
	public void setFrequency(double frequency){
		uniformTraffic.setFrequency(frequency);
		normalTraffic.setFrequency(frequency);
		exponentialTraffic.setFrequency(frequency);
		paretoTraffic.setFrequency(frequency);
		sr4Traffic.setFrequency(frequency);
	}
	
	/**
	 * Sets the target router of the packets. <br>
	 * Amongst the target options are:
	 * <li> random, where each generated packet has a target router chosen randomly;
	 * <li> complement, where all packets have the same target router and this target router is defined according to the complement traffic pattern:
	 * <li> one of the routers of the NoC (excluding the source router), where all packets have the same target router.
	 * <br>
	 * The target has the same value in all traffics.
	 * @param target
	 */
	public void setTarget(String target){
		uniformTraffic.setTarget(target);
		exponentialTraffic.setTarget(target);
		normalTraffic.setTarget(target);
		paretoTraffic.setTarget(target);
		sr4Traffic.setTarget(target);
	}

	/**
	 * Sets the traffic priority. <br>
	 * This parameter is visible only when the NoC has virtual channels and uses priority-based scheduling. <br>
	 * The traffic priority can be chosen among the values zero and n-1, where n is the number of virtual channels.
	 * The priority has the same value in all traffics.
	 * @param priority
	 */
	public void setPriority(int priority){
		uniformTraffic.setPriority(priority);
		normalTraffic.setPriority(priority);
		exponentialTraffic.setPriority(priority);
		paretoTraffic.setPriority(priority);
		sr4Traffic.setPriority(priority);
	}

	/**
	 * Sets the number of transmitted packets.
	 * The number of packets has the same value in Uniform, Normal and Pareto traffics.
	 * <b>This method is not applicable to SR4 router.</b>
	 * @param n The number of packets.
	 */
	public void setNumberOfPackets(int n){
		if(isSR4){
			System.out.println("The setNumberOfPackets() method is not applicable to SR4 traffic.\n" +
								"Please use the setNumberOfPackets(int service)");
			System.exit(0);
		}
		uniformTraffic.setNumberOfPackets(n);
		exponentialTraffic.setNumberOfPackets(n);
		normalTraffic.setNumberOfPackets(n);
		paretoTraffic.setNumberOfPackets(n);
	}
	
	/**
	 * Sets the number of transmitted packets in the informed service.
	 * @param service CTRL=0 GS=2 BE=3
	 * @param n The number of packets.
	 */
	public void setNumberOfPackets(int service, int n){
		sr4Traffic.setNumberOfPackets(service,n);
	}
	
	/**
	 * Sets the packet size.
	 * The packet size has the same value in Uniform, Normal and Pareto traffics.
	 * <b>This method is not applicable to SR4 router.</b>
	 * @param n The packet size.
	 */
	public void setPacketSize(int n){
		if(isSR4){
			System.out.println("The setPacketSize() method is not applicable to SR4 traffic.\n" +
								"Please use the setPacketSize(int service)");
			System.exit(0);
		}
		uniformTraffic.setPacketSize(n);
		exponentialTraffic.setPacketSize(n);
		normalTraffic.setPacketSize(n);
		paretoTraffic.setPacketSize(n);
	}

	/**
	 * Sets the packet size in the informed service.
	 * @param service CTRL=0 GS=2 BE=3
	 * @param n The packet size
	 */
	public void setPacketSize(int service, int n){
		sr4Traffic.setPacketSize(service,n);
	}
	
	/**
	 * Sets the transmission rate in Mbps (Megabit per second).<br>
	 * <b>This method is not applicable to SR4 router.</b>
	 * @param rate
	 */
	public void setUniformRate(double rate){
		if(isSR4){
			System.out.println("The setUniformRate() method is not applicable to SR4 traffic.\n" +
								"Please use the setUniformRate(int service)");
			System.exit(0);
		}
		uniformTraffic.setRate(rate);
	}

	/**
	 * Sets the transmission rate to the informed service.
	 * @param service CTRL=0 GS=2 BE=3
	 * @param rate
	 */
	public void setUniformRate(int service, double rate){
		sr4Traffic.setRate(service, rate);
	}
	
	
	public void setExponentialAverageRate(double averageRate){ exponentialTraffic.setAverageRate(averageRate); }
	public void setExponentialMinimalRate(double minimalRate){ exponentialTraffic.setMinimalRate(minimalRate); }
	public void setExponentialMaximalRate(double maximalRate){ exponentialTraffic.setMaximalRate(maximalRate); }
	public void setExponentialIncrement(double increment){ exponentialTraffic.setIncrement(increment); }
	
	/**
	 * Sets the transmission average rate in Mbps.
	 * @param averageRate
	 */
	public void setNormalAverageRate(double averageRate){ normalTraffic.setAverageRate(averageRate); }
		
	/**
	 * Sets the transmission minimal rate in Mbps.
	 * @param minimalRate
	 */
	public void setNormalMinimalRate(double minimalRate){ normalTraffic.setMinimalRate(minimalRate); }

	/**
	 * Sets the transmission maximal rate in Mbps.
	 * @param maximalRate
	 */
	public void setNormalMaximalRate(double maximalRate){ normalTraffic.setMaximalRate(maximalRate); }

	/**
	 * Sets the standard deviation of the average rate in Mbps.
	 * @param standardDeviation
	 */
	public void setNormalStandardDeviation(double standardDeviation){ normalTraffic.setStandardDeviation(standardDeviation); }

	/**
	 * Sets the increment used in the rate generation between the minimum and maximum rates.
	 * @param increment
	 */
	public void setNormalIncrement(double increment){ normalTraffic.setIncrement(increment); }

	/**
	/* Sets the transmission rate during the On period.
	 * @param rateOnPeriod
	 */
	public void setParetoRateOnPeriod(double rateOnPeriod){ paretoTraffic.setRateOnPeriod(rateOnPeriod); }

	/**
	 * Sets the number of packets bursts.
	 * @param burstSize
	 */
	public void setParetoBurstSize(int burstSize){ paretoTraffic.setBurstSize(burstSize); }


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
		String info = "\n{" + tag1 + "Distribution" + tag2 + "}" + distribution;
		info = info + uniformTraffic.getInfo(tag1 + "Uniform_", tag2);
		info = info + exponentialTraffic.getInfo(tag1 + "Exponential_", tag2);
		info = info + normalTraffic.getInfo(tag1 + "Normal_" , tag2);
		info = info + paretoTraffic.getInfo(tag1 + "Pareto_" , tag2);
		info = info + sr4Traffic.getInfo(CTRL, tag1 + "CTRL_", tag2);
		info = info + sr4Traffic.getInfo(GS, tag1 + "GS_", tag2);
		info = info + sr4Traffic.getInfo(BE, tag1 + "BE_", tag2) + "\n";
		return info;
	}
	
	/**
	* Returns a selected group of traffic distribution parameters in a single string.
	* @return s
	*/
	public String getSelectedInfo(){
		if(isSR4)
			return getSR4SelectedInfo();
		else if(distribution.equalsIgnoreCase(UNIFORM))
			return getUniformSelectedInfo();
		else if(distribution.equalsIgnoreCase(NORMAL))
			return getNormalSelectedInfo();
		else if(distribution.equalsIgnoreCase(PARETO))
			return getParetoSelectedInfo();
		else if(distribution.equalsIgnoreCase(EXPONENTIAL))
			return getExponentialSelectedInfo();
		return ("");
	}

	/**
	* Returns a selected group of Uniform parameters  if the <i>Rate</i> parameter is different to zero.
	* All selected parameters are concatenated in the same line.
	* The selected parameters are:
	* <li> router address
	* <li> traffic distribution
	* <li> target
	* <li> packet size
	* <li> rate
	* @return s
	*/
	public String getUniformSelectedInfo(){
		String s = "";
		if(uniformTraffic.getRate()!=0){
			s = " Distribution=" + distribution + 
				" Target=" + uniformTraffic.getTarget() + 
				" Packet Size=" + uniformTraffic.getPacketSize() +
				" Rate=" + uniformTraffic.getRate() + "\n";
		}
		return s;
	}

	/**
	* Returns a selected group of Normal parameters if the <i>Average Rate</i> parameter is different to zero.
	* All selected parameters are concatenated in the same line.
	* The selected parameters are:
	* <li> router address
	* <li> traffic distribution
	* <li> target
	* <li> packet size
	* <li> rate
	* @return s
	*/
	public String getNormalSelectedInfo(){
		String s = "";
		if(normalTraffic.getAverageRate()!=0){
			s = " Distribution=" + distribution + 
				" Target=" + normalTraffic.getTarget() + 
				" Packet Size=" + normalTraffic.getPacketSize() +
				" Average Rate=" + normalTraffic.getAverageRate() + "\n";
		}
		return s;
	}

	public String getExponentialSelectedInfo()
	{
		String s = "";
		if(exponentialTraffic.getAverageRate()!=0)
		{
			s = " Distribution=" + distribution + 
				" Target=" + exponentialTraffic.getTarget() + 
				" Packet Size=" + exponentialTraffic.getPacketSize() +
				" Average Rate=" + exponentialTraffic.getAverageRate() + "\n";
		}
		return s;
	}
	
	/**
	* Returns a selected group of Pareto parameters if the <i>Rate On Period</i> parameter is different to zero.
	* All selected parameters are concatenated in the same line.
	* The selected parameters are:
	* <li> router address
	* <li> traffic distribution
	* <li> target
	* <li> packet size
	* <li> rate
	* @return s
	*/
	public String getParetoSelectedInfo(){
		String s = "";
		if(paretoTraffic.getRateOnPeriod()!=0){
			s = " Distribution=" + distribution + 
				" Target=" + paretoTraffic.getTarget() + 
				" Packet Size=" + paretoTraffic.getPacketSize() +
				" Rate Of on Period=" + paretoTraffic.getRateOnPeriod() + "\n";
		}
		return s;
	}
	
	/**
	* Returns a selected group of SR4 parameters if the <i>Rate</i> parameter is different to zero.
	* The selected parameters are:
	* <li> router address
	* <li> traffic distribution
	* <li> target
	* <li> number of packets
	* <li> packet size
	* <li> rate
	* @return s
	*/
	public String getSR4SelectedInfo(){
		String s = "";
		if(sr4Traffic.getRate(CTRL)!=0 || sr4Traffic.getRate(GS)!=0 || sr4Traffic.getRate(BE)!=0){
			s = " Distribution=" + distribution + " Target=" + uniformTraffic.getTarget() + "\n"; 
	
			if(sr4Traffic.getRate(CTRL)!=0){
				s = s + "\tControl Messages: #Packets=" + sr4Traffic.getNumberOfPackets(CTRL) +
						" Packet Size=" + sr4Traffic.getPacketSize(CTRL) +
						" Rate="+sr4Traffic.getRate(CTRL) + "\n";
			}
						
			if(sr4Traffic.getRate(GS)!=0){
				s = s + "\tGS Messages: #Packets=" + sr4Traffic.getNumberOfPackets(GS) +
						" Packet Size="+sr4Traffic.getPacketSize(GS) +
						" Rate="+sr4Traffic.getRate(GS) + "\n";
			}
	
			if(sr4Traffic.getRate(BE)!=0){
				s = s + "\tBE Messages: #Packets=" + sr4Traffic.getNumberOfPackets(BE) +
						" Packet Size="+sr4Traffic.getPacketSize(BE) +
						" Rate="+sr4Traffic.getRate(BE) + "\n";
			}
		}
		return s;
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
			distribution = Default.readInfo(file, tag1 + "Distribution");
			uniformTraffic.readParameters(file, tag1 + "Uniform_", tag2);
			exponentialTraffic.readParameters(file, tag1 + "Exponential_", tag2);
			normalTraffic.readParameters(file, tag1 + "Normal_", tag2);
			paretoTraffic.readParameters(file, tag1 + "Pareto_", tag2);
			sr4Traffic.readParameters(CTRL, file, tag1 + "CTRL_", tag2);
			sr4Traffic.readParameters(GS, file, tag1 + "GS_", tag2);
			sr4Traffic.readParameters(BE, file, tag1 + "BE_", tag2);
		} catch(Exception e){
			JOptionPane.showMessageDialog(null,e.getMessage(), "Error Message", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}

	/**
	 * Disables all router traffics.
	 */
	public void disable(){
		uniformTraffic.setRate(0);
		normalTraffic.set(0, 0, 0, 0, 0);
		exponentialTraffic.set(0, 0, 0, 0);
		paretoTraffic.setRateOnPeriod(0);
		sr4Traffic.setRate(0);
	}

	/**
	 * Use to test Scenery methods.
	 * @param args The list of arguments
	 */
	public static void main(String args[]){
		RouterTraffic r = new RouterTraffic(false);
		System.out.println(r.getInfo());
	}

}
