package TrafficMbps;

import AtlasPackage.Convert;

/**
 * This class is able to determine a router address according to a informed Spatial distribution. 
 * @author Aline Vieira de Mello
 * @version
 *
 */
public class SpatialDistribution{

	private int tam;
	private int dimX;
	private int dimY;
	private int flitWidth;
	private char c[];

	/**
	 * Creates a spatial distribution object.
	 * @param dimX The X-dimension of NoC.
	 * @param dimY The Y-dimension of NoC.
	 * @param flitWidth The number of bits of a flit.
	 */
	public SpatialDistribution(int dimX,int dimY,int flitWidth){
		this.dimX = dimX;
		this.dimY = dimY;
		this.flitWidth = flitWidth;

		tam = 0; //minimal number of bits allowing represent a router address
		if((dimX*dimY)==4) tam = 2;
		else if((dimX*dimY)==8) tam = 3;
		else if((dimX*dimY)==16) tam = 4;
		else if((dimX*dimY)==32) tam = 5;
		else if((dimX*dimY)==64) tam = 6;
		else if((dimX*dimY)==128) tam = 7;
		else tam = 8;

		c = new char[tam];
	}

	/**
	 * Define a specific target according to the informed traffic spatial distribution.
	 * @param target The traffic spatial distribution.
	 * @param sourceX The router address in X-dimension of NoC.
	 * @param sourceY The router address in Y-dimension of NoC.
	 * @return A specific target router
	 */
	public String defineTarget(String target,int sourceX,int sourceY){
		String t;
		if(target.equalsIgnoreCase("random"))
			t=random(sourceX,sourceY);
		else if(target.equalsIgnoreCase("bitReversal"))
			t=bitReversal(sourceX,sourceY);
		else if(target.equalsIgnoreCase("butterfly"))
			t=butterfly(sourceX,sourceY);
		else if(target.equalsIgnoreCase("complemento"))
			t=complemento(sourceX,sourceY);
		else if(target.equalsIgnoreCase("matrixTranspose"))
			t=matrixTranspose(sourceX,sourceY);
		else if(target.equalsIgnoreCase("perfectShuffle"))
			t=perfectShuffle(sourceX,sourceY);
		else
			return target;

		return t;
	}

	/**
	 * Define the target router using random. <br>
	 * The target router must be different of the informed source router.
	 * @param addX The source router address in X-dimension of NoC. 
	 * @param addY The source router address in Y-dimension of NoC.
	 * @return The target router address. 
	 */
	private String random(int addX,int addY){
		String targetX="",targetY="";
		int X,Y;
		String a="",b="";
		String high="",low="";
		// System.out.print("" + addX + addY);
		if(flitWidth == 8)
		{
			do
			{
				// Source 30
				X = (int)(Math.random()*dimX);
				Y = (int)(Math.random()*dimY);
				
				targetX = Integer.toString(X);
				targetY = Integer.toString(Y);
				
				int value = Convert.getNumberOfRouter(""+X+Y,dimX);
				String bin = Convert.decToBin(value,4);
				
				high = "" + bin.charAt(0) + bin.charAt(1);
				low = ""  + bin.charAt(2) + bin.charAt(3);
				
				high = Convert.binToHex(high);
				low  = Convert.binToHex(low);
				
			}while(high.equals(Integer.toString(addX)) && low.equals(Integer.toString(addY)));
		}
		else
		{
			do{
				//Generate the target in X dimension
				X=(int)(Math.random()*dimX);
				targetX = ""+X;
				if(X < 10)
					targetX = "0" + X;
				//Generate the target in Y dimension
				Y=(int)(Math.random()*dimY);
				targetY = ""+Y;
				if(Y < 10)
					targetY = "0" + Y;
			}while(X==addX && Y==addY);
		}
		// System.out.println(" : " + targetX + targetY);
		// return Convert.formatAddressXY(targetX, targetY, flitWidth);
		return (""+targetX+targetY);
	}

	/**
	 * Define the target router using bit reversal.
	 * @param addX The source router address in X-dimension of NoC. 
	 * @param addY The source router address in Y-dimension of NoC.
	 * @return The target router address. 
	 */
	private String bitReversal(int addX,int addY){
		String targetXBin,targetYBin,targetBin;
		int targetX,targetY;

		targetXBin=Convert.decToBin(addX,(tam/2));
		targetYBin=Convert.decToBin(addY,(tam/2));
		targetBin=targetXBin+targetYBin; //concatenate targetX and targetY;
		//System.out.print("targetBin="+targetBin);
		for(int i=0; i<tam; i++){ //invert the bits
			if(targetBin.charAt(tam-1-i)=='1') c[i] = '1';
			else c[i] = '0';
		}
		targetBin = new String(c); //convert char to string
		//System.out.println(" Bit reversal targetBin="+targetBin);
		targetXBin=targetBin.substring(0,tam/2);
		targetYBin=targetBin.substring(tam/2);
		targetX = Integer.parseInt(targetXBin, 2);
		targetY = Integer.parseInt(targetYBin, 2);
		return Convert.formatAddressXY(targetX, targetY, flitWidth);
	}

	/**
	 * Define the target router using butterfly. <br>
	 * Butterfly changes the first bit with the last bit. For instance: from 1010 to 0011.
	 * @param addX The source router address in X-dimension of NoC. 
	 * @param addY The source router address in Y-dimension of NoC.
	 * @return The target router address. 
	 */
	private String butterfly(int addX,int addY){
		String targetXBin,targetYBin,targetBin;
		int targetX,targetY;

		targetXBin=Convert.decToBin(addX,(tam/2));
		targetYBin=Convert.decToBin(addY,(tam/2));
		targetBin=targetXBin+targetYBin; //concatenates targetX and targetY;
		for(int i=0; i<tam; i++){ //copy all binary address
			c[i] = targetBin.charAt(i);
		}
		c[0] = targetBin.charAt(tam-1); //write the last bit in the place of first one
		c[tam-1] = targetBin.charAt(0); //write the first bit in the place of last one

		targetBin = new String(c); //convert char to string
		targetXBin=targetBin.substring(0,tam/2);
		targetYBin=targetBin.substring(tam/2);
		targetX = Integer.parseInt(targetXBin, 2);
		targetY = Integer.parseInt(targetYBin, 2);
		return Convert.formatAddressXY(targetX, targetY, flitWidth);
	}

	/**
	 * Define the target router using complement. <br>
	 * Complement invert the bit value. For instance: from 1010 to 0101.
	 * @param addX The source router address in X-dimension of NoC. 
	 * @param addY The source router address in Y-dimension of NoC.
	 * @return The target router address. 
	 */
	private String complemento(int addX,int addY){
		String targetXBin,targetYBin,targetBin;
		int targetX,targetY;

		targetXBin=Convert.decToBin(addX,(tam/2));
		targetYBin=Convert.decToBin(addY,(tam/2));
		targetBin=targetXBin+targetYBin; //concatenate targetX and targetY;

		for(int i=0; i<tam; i++){ //copy binary address
			if(targetBin.charAt(i)=='1') c[i] = '0';
			else c[i] = '1';
		}

		targetBin = new String(c); //convert char to string
		targetXBin=targetBin.substring(0,tam/2);
		targetYBin=targetBin.substring(tam/2);
		targetX = Integer.parseInt(targetXBin, 2);
		targetY = Integer.parseInt(targetYBin, 2);
		return(""+targetX+targetY);
	}

	/**
	 * Define the target router using Matrix Transpose. <br>
	 * Matrix Transpose changes the X-address by Y-address. For instance: from 0010 to 1000.
	 * @param addX The source router address in X-dimension of NoC. 
	 * @param addY The source router address in Y-dimension of NoC.
	 * @return The target router address. 
	 */
	private String matrixTranspose(int addX,int addY){
		return Convert.formatAddressXY(addY, addX, flitWidth);
	}

	/**
	 * Define the target router using Perfect Shuffle. <br>
	 * Perfect Shuffle shift the address to left. For instance: from 1010 to 0101.
	 * @param addX The source router address in X-dimension of NoC. 
	 * @param addY The source router address in Y-dimension of NoC.
	 * @return The target router address. 
	 */
	private String perfectShuffle(int addX,int addY){
		String targetXBin,targetYBin,targetBin;
		int targetX,targetY;

		targetXBin=Convert.decToBin(addX,(tam/2));
		targetYBin=Convert.decToBin(addY,(tam/2));
		targetBin=targetXBin+targetYBin; //concatenate targetX and targetY;

		for(int i=0; i<tam; i++){ //copy binary address
			if(i==0){
				if(targetBin.charAt(i)=='1') c[tam-1] = '1';
				else c[tam-1] = '0';
			}
			else{
				if(targetBin.charAt(i)=='1') c[i-1] = '1';
				else c[i-1] = '0';
			}
		}

		targetBin = new String(c); //convert char to string
		targetXBin=targetBin.substring(0,tam/2);
		targetYBin=targetBin.substring(tam/2);
		targetX = Integer.parseInt(targetXBin, 2);
		targetY = Integer.parseInt(targetYBin, 2);
		return Convert.formatAddressXY(targetX, targetY, flitWidth);
	}

	/**
	 * Test the class methods.
	 * @param s
	 */
	public static void main(String s[]){
		SpatialDistribution g =new SpatialDistribution(4,4,16);
		System.out.println("Target="+g.bitReversal(1,0));
		System.out.println("Target="+g.bitReversal(0,0));
		System.out.println("Target="+g.bitReversal(1,1));
		System.out.println("Target="+g.bitReversal(2,2));
	}
}
