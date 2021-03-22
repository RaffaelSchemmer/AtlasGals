package AtlasPackage;

/**
* <i>Convert</i> has some methods to convert a data type to another data type. <p> 
* For instance: a decimal value to hexadecimal String with a determined length.
* @author Aline Vieira de Mello
* @version
*/
public class Convert{

	/**
	* Converts a decimal value in a hexadecimal String with <i>length</i> informed.
	* If the hexadecimal String has more bytes than <i>length</i> then returns the less significant bytes are considered. 
	* If the hexadecimal String has less bytes than <i>length</i> then the more significant bytes are filled with zeros.
	* @param val The decimal value
	* @param length The String <i>length</i>
	* @return hexadecimal String
	*/
	public static String decToHex(int val, int length){
		String hex = Integer.toHexString(val);
		return formatStringLength(hex,length);
	}

	/**
	* Converts a decimal value in a binary String with <i>length</i> informed.
	* If the binary String has more bytes than <i>length</i> then returns the less significant bytes are considered. 
	* If the binary String has less bytes than <i>length</i> then the more significant bytes are filled with zeros.
	* @param val The decimal value
	* @param length The String <i>length</i>
	* @return binary String
	*/
	public static String decToBin(int val,int length){
		String bin = Integer.toBinaryString(val);
		return formatStringLength(bin,length);
	}

	/**
	* Converts a hexadecimal String in a binary String.
	* @param hex The hexadecimal String
	* @return binary String
	*/
	public static String hexToBin(String hex){
		int decimal = Integer.parseInt(hex,16);
		return Integer.toBinaryString(decimal);
	}

	/**
	* Converts a hexadecimal String in a binary String with <i>length</i> informed.
	* If the binary String has more bytes than <i>length</i> then returns the less significant bytes are considered. 
	* If the binary String has less bytes than <i>length</i> then the more significant bytes are filled with zeros.
	* @param hex The hexadecimal String
	* @param length The String <i>length</i>
	* @return binary String
	*/
	public static String hexToBin(String hex,int length){
		int decimal=Integer.parseInt(hex,16);
		return decToBin(decimal,length);
	}

	/**
	* Converts a binary String in a hexadecimal String.
	* @param bin The binary String
	* @return hexadecimal String
	*/
	public static String binToHex(String bin){
		int decimal = Integer.parseInt(bin, 2);
		return Integer.toHexString(decimal);
	}

	/**
	* Converts a binary String in a hexadecimal String with <i>length</i> informed.
	* If the hexadecimal String has more bytes than <i>length</i> then returns the less significant bytes are considered. 
	* If the hexadecimal String has less bytes than <i>length</i> then the more significant bytes are filled with zeros.
	* @param bin The binary String
	* @param length The String <i>length</i>
	* @return hexadecimal String
	*/
	public static String binToHex(String bin,int length){
		int decimal = Integer.parseInt(bin, 2);
		return decToHex(decimal, length);
	}

	/**
	* Returns the result of the binary subtraction between two hexadecimal values.
	* @param hex1
	* @param hex2
	* @return binary String
	*/
	public static String subBin(String hex1,String hex2)
	{
		hex2=twoComplement(hex2);
		return sumBin(hex1,hex2);
	}

	/**
	* Returns the result of the binary addition between two hexadecimal values.
	* @param hex1
	* @param hex2
	* @return binary String
	*/
	public static String sumBin(String hex1,String hex2){
		String valorBin1,valorBin2,resultadoBin,resultadoHex,carry;
		valorBin1=hexToBin(hex1);
		valorBin2=hexToBin(hex2);
		resultadoBin="";
		carry="0";
		for(int i=16;i>0;i--){
			if(valorBin1.substring(i-1,i).equals("1")){
				if(valorBin2.substring(i-1,i).equals("1")){
					if(carry.equals("1")){ // 1 1 1 -> 1 1
						resultadoBin="1".concat(resultadoBin);
						carry="1";
					}
					else{ // 1 1 0 -> 0 1
						resultadoBin="0".concat(resultadoBin);
						carry="1";
					}
				}
				else{
					if(carry.equals("1")){ // 1 0 1 -> 0 1
						resultadoBin="0".concat(resultadoBin);
						carry="1";
					}
					else{ // 1 0 0 -> 1 0
						resultadoBin="1".concat(resultadoBin);
						carry="0";
					}
				}
			}
			else{
				if(valorBin2.substring(i-1,i).equals("1")){
					if(carry.equals("1")){ // 0 1 1 -> 0 1
						resultadoBin="0".concat(resultadoBin);
						carry="1";
					}
					else{ // 0 1 0 -> 1 0
						resultadoBin="1".concat(resultadoBin);
						carry="0";
					}
				}
				else{
					if(carry.equals("1")){ // 0 0 1 -> 1 0
						resultadoBin="1".concat(resultadoBin);
						carry="0";
					}
					else{ // 0 0 0 -> 0 0
						resultadoBin="0".concat(resultadoBin);
						carry="0";
					}
				}
			}
		}
		resultadoHex = binToHex(resultadoBin);
		return resultadoHex;
	}

	/**
	* Returns the two's complement result.
	* @param hex
	* @return binary String
	*/
	public static String twoComplement(String hex){
		String valorBin,resultadoBin,resultadoHex;
		resultadoBin="";
		valorBin=hexToBin(hex);
		for (int i=0;i<16;i++){
			if(valorBin.substring(i,i+1).equals("1"))
				resultadoBin=resultadoBin.concat("0");
			else
				resultadoBin=resultadoBin.concat("1");
		}
		resultadoHex=binToHex(resultadoBin);
		resultadoHex=sumBin(resultadoHex,"0001"); //adiciona 1
		return resultadoHex;
	}

	/**
	* Returns the String with the length informed.
	* If the String has more bytes than <i>length</i> then returns the less significant bytes. 
	* If the String has less bytes than <i>length</i> then fills the more significant bytes with zeros.
	* @param s The String
	* @param length
	* @return String with the length informed
	*/
	public static String formatStringLength(String s,int length){
		//if String has length less than the length informed
		//then fills the more significant bytes with zeros.  
		if(s.length()<length){
		   	int tam=length-s.length();
		   	for(int i=0;i<tam;i++)
		   		s= "0" + s;
		}
		//if String has length more than the length informed
		//then returns the less significant bytes
		s=s.substring(s.length()-length,s.length());
		return s;
	}

	/**
	 * Returns the router address (in XY format)  associated to the number of router informed.
	 * The routers are numbered from left to right and from bottom to up.
	 * Each router has an address in XY format, where 
	 * X corresponds to the router position in X-dimension NoC and 
	 * Y corresponds to the router position in Y-dimension NoC.
	 * For instance, considering a NoC 3x3, 
	 * the router 0 corresponds to the address 00 and 
	 * the router 4 corresponds to the address 11.
	 * @param n The number of router.
	 * @param dimXNoC The X-dimension of NoC
	 * @param flitSize The number of bits of a flit.
	 * @return The router address.
	 */
	public static String getXYAddress(int n, int dimXNoC, int flitSize){
		int x = n % dimXNoC;
		int y = n / dimXNoC;

		//gets the address in X-dimmension
		String nodoXBin=decToBin(x,(flitSize/4));
		//gets the address in Y-dimmension
		String nodoYBin=decToBin(y,(flitSize/4));
		//concatenates the address X and address Y
		String nodoBin=nodoXBin+nodoYBin;
		//Converts to hexadecimal
		return binToHex(nodoBin,(flitSize/8));
	}

	/**
	* Returns the X-dimension of router address in a packet flit (hexadecimal format).
	* The router address is stored in the second half of a packet flit.
	* The router address is also divided in two-halves. 
	* The first one corresponds to the address X and the second one corresponds to address Y.
	* For instance, considering a flitSize equal to 16-bits, the flit 0012 has address X equals to 1. 
	* @param packetFlit A packet flit in hexadecimal format. For instance: 0012
	* @param flitSize The number of bits of a flit.
	* @return addressX the router address in X-dimension. 
	*/
	public static int getAddressX(String packetFlit,int flitSize){
		// gets the second half of the packet flit in binary format
		// the binary format is necessary because
		// the same byte could have X and Y address, case the flit size is inferior to 16-bits
		String addressXYBin = hexToBin(packetFlit,flitSize/2);
		// gets the first half of the addressXY
		String addressXBin = addressXYBin.substring(0,addressXYBin.length()/2);
		// converts from binary to decimal
		return Integer.parseInt(addressXBin, 2);
	}

	/**
	* Returns the Y-dimension of router address in a packet flit (hexadecimal format).
	* The router address is stored in the second half of a packet flit.
	* The router address is also divided in two-halves. 
	* The first one corresponds to the address X and the second one corresponds to address Y.
	* For instance, considering a flitSize equal to 16-bits, the flit 0012 has address Y equals to 2. 
	* @param packetFlit A packet flit in hexadecimal format. For instance: 0012
	* @param flitSize The number of bits of a flit.
	* @return addressY the router address in Y-dimension. 
	*/
	public static int getAddressY(String packetFlit,int flitSize){
		// gets the second half of the packet flit in binary format.
		// the binary format is necessary because
		// the same byte could have X and Y address, case the flit size is inferior to 16-bits
		String adressXYBin = hexToBin(packetFlit,flitSize/2);
		// gets the second half of the addressXY
		String adressYBin = adressXYBin.substring(adressXYBin.length()/2);
		// converts from binary to decimal
		return Integer.parseInt(adressYBin, 2);
	}

	/**
	 * Returns the number of router associated to the informed router address XY in a packet flit (hexadecimal format).
	 * The routers are numbered from left to right and from bottom to up.
	 * Each router has an address in XY format, where 
	 * X corresponds to the router position in X-dimension NoC and 
	 * Y corresponds to the router position in Y-dimension NoC.
	 * For instance, considering a NoC 3x3, 
	 * the router address 00 corresponds to the router 0 and 
	 * the router address 11 corresponds to the router 4.
	 * @param packetFlit The packet flit containing the router address
	 * @param dimXNoC The X-dimension of NoC
	 * @param flitSize The number of bits of a flit
	 * @return The number of router
	 */
	public static int getNumberOfRouter(String packetFlit,int dimXNoC,int flitSize){
		int x = getAddressX(packetFlit,flitSize);
		int y = getAddressY(packetFlit,flitSize);
		return (y * dimXNoC + x);
	}

	/**
	 * Returns the number of router associated to the informed router address XY.
	 * The routers are numbered from left to right and from bottom to up.
	 * Each router has an address in XY format, where 
	 * X corresponds to the router position in X-dimension NoC and 
	 * Y corresponds to the router position in Y-dimension NoC.
	 * For instance, considering a NoC 3x3, 
	 * the router address 00 corresponds to the router 0 and 
	 * the router address 11 corresponds to the router 4.
	 * @param addressXY The router address
	 * @param dimXNoC The X-dimension of NoC
	 * @return The number of router
	 */
	public static int getNumberOfRouter(String addressXY,int dimXNoC){
		String addressXHex = addressXY.substring(0, addressXY.length()/2);
		String addressYHex = addressXY.substring(addressXY.length()/2);
		int addressX = Integer.parseInt(addressXHex, 16);
		int addressY = Integer.parseInt(addressYHex, 16);
		return (addressY * dimXNoC + addressX);
	}
	
	
	/**
	 * Returns the router address in X-dimension of NoC associated to the informed router number.
	 * The routers are numbered from left to right and from bottom to up.
	 * Each router has an address in XY format, where 
	 * X corresponds to the router position in X-dimension NoC and 
	 * Y corresponds to the router position in Y-dimension NoC.
	 * For instance, considering a NoC 3x3, 
	 * the router address 00 corresponds to the router 0 and 
	 * the router address 11 corresponds to the router 4.
	 * @param nRouter The router number.
	 * @param dimXNoC The X-dimension of NoC
	 * @return The address router in X-dimension of NoC.
	 */
	public static int getAddressX(int nRouter,int dimXNoC){
		return (nRouter%dimXNoC);
	}
	
	/**
	 * Returns the router address in Y-dimension of NoC associated to the informed router number.
	 * The routers are numbered from left to right and from bottom to up.
	 * Each router has an address in XY format, where 
	 * X corresponds to the router position in X-dimension NoC and 
	 * Y corresponds to the router position in Y-dimension NoC.
	 * For instance, considering a NoC 3x3, 
	 * the router address 00 corresponds to the router 0 and 
	 * the router address 11 corresponds to the router 4.
	 * @param nRouter The router number.
	 * @param dimXNoC The X-dimension of NoC
	 * @return The address router in Y-dimension of NoC.
	 */
	public static int getAddressY(int nRouter,int dimXNoC){
		return (nRouter/dimXNoC);
	}

	/**
	 * Return the router address XY with length equals to half of a flit.
	 * Considering the router address 12.
	 * For flit size equals to 8-bits, the formated address is 6. 
	 * For flit size equals to 16-bits, the formated address is 12. 
	 * For flit size equals to 32-bits, the formated address is 0102.
	 * @param addressXY The router address in XY format.
	 * @param flitSize The number of bits of a flit. 
	 * @return The formated address router.
	 */
	public static String formatAddressXY(String addressXY, int flitSize){
		//gets the address X in hexadecimal format (first half of addressXY)
		String addressXHex = addressXY.substring(0,addressXY.length()/2);
		//gets the address Y in hexadecimal format (second half of addressXY)
		String addressYHex = addressXY.substring(addressXY.length()/2);
		//converts the address X from hexadecimal to binary format
		String addressXBin = hexToBin(addressXHex,flitSize/4);
		//converts the address X from hexadecimal to binary format
		String addressYBin = hexToBin(addressYHex,flitSize/4);
		//concatenates the address X and address Y in binary format
		String addressXYBin = addressXBin + addressYBin;
		//converts the addressXY from binary to hexadecimal format
		return binToHex(addressXYBin,flitSize/8);
	}

	/**
	 * Return the router address XY with length equals to half of a flit.
	 * Considering the router address 12.
	 * For flit size equals to 8-bits, the formated address is 6. 
	 * For flit size equals to 16-bits, the formated address is 12. 
	 * For flit size equals to 32-bits, the formated address is 0102.
	 * @param addressX The router address in X-dimension.
	 * @param addressY The router address in Y-dimension.
	 * @param flitSize The number of bits of a flit. 
	 * @return The formated address router.
	 */
	public static String formatAddressXY(int addressX, int addressY, int flitSize){
		//converts the address X from decimal to binary format
		String addressXBin = decToBin(addressX,flitSize/4);
		//converts the address X from decimal to binary format
		String addressYBin = decToBin(addressY,flitSize/4);
		//concatenates the address X and address Y in binary format
		String addressXYBin = addressXBin + addressYBin;
		//converts the addressXY from binary to hexadecimal format
		return binToHex(addressXYBin,flitSize/8);
	}
	
	/**
	 * Return the informed address in address flit format. An address flit is divided in two halves: 
	 * The first one is filled by zeros and the second one contains the router address in XY. <br><br>
	 * Considering the router address 12.
	 * For flit size equals to 8-bits, the formated address flit is 06. 
	 * For flit size equals to 16-bits, the formated address flit is 0012. 
	 * For flit size equals to 32-bits, the formated address flit is 00000102.
	 * @param addressX The router address in X-dimension of NoC.
	 * @param addressY The router address in Y-dimension of NoC.
	 * @param flitSize The number of bits of a flit. 
	 * @return The formated address flit.
	 */
	public static String formatAddressFlit(int addressX,int addressY, int flitSize){
		//converts the address X from decimal to binary format
		String addressXBin=Convert.decToBin(addressX,(flitSize/4));
		//converts the address Y from decimal to binary format
		String addressYBin=Convert.decToBin(addressY,(flitSize/4));
		//concatenates the address X and address Y in binary format
		String addressXYBin=addressXBin+addressYBin;
		String addressXYHex = Convert.binToHex(addressXYBin,(flitSize/8));
		String zeros = Convert.decToHex(0,(flitSize/8));
		String flitHex = zeros + addressXYHex;
		return flitHex;
	}

	/**
	 * Return the informed address in address flit format. An address flit is divided in two halves: 
	 * The first one is filled by zeros and the second one contains the router address in XY. <br><br>
	 * Considering the router address 12.
	 * For flit size equals to 8-bits, the formated address flit is 06. 
	 * For flit size equals to 16-bits, the formated address flit is 0012. 
	 * For flit size equals to 32-bits, the formated address flit is 00000102.
	 * @param addressXY The router address.
	 * @param flitSize The number of bits of a flit. 
	 * @return The formated address flit.
	 */
	public static String formatAddressFlit(String addressXY, int flitSize){
		int nbytes = flitSize/4;
		//certify that address has one half of flit 
		addressXY = formatStringLength(addressXY, nbytes/4);
		//the more significant bytes are filles with zeros
		return formatFlit(addressXY, flitSize);
	}
	
	/**
	 * Returns the informed value in Timestamp format. <br>
	 * The timestamp is composed of 4 flits and the more significant bytes are filled with zeros. 
	 * @param value 
	 * @param flitSize The number of bits of a flit.
	 * @return The 4 flits Timestamp 
	 */
	public static String[] formatTimestamp(String value, int flitSize){
		String[] timestampHex=new String[4];
		value = formatStringLength(value,flitSize);
		for(int i=0,j=flitSize;i<4;i++,j=j-(flitSize/4)){
			timestampHex[i] = value.substring(j-(flitSize/4),j);
		}
		return timestampHex;
	}

	/**
	* Returns the informed value in flit format. <br> 
	* The number of bytes (<i>nbytes</i>) of a flit is equal to flitSize/4. <br>
	* If the value has more bytes than <i>nbytes</i> then returns the less significant bytes. <br>
	* If the value has less bytes than <i>nbytes</i> then the more significant bytes are filled with zeros.
	* @param value The value to be formatted
	* @param flitSize The number of bits of a flit
	* @return String The value in the flit format.
	*/
	public static String formatFlit(int value,int flitSize){
		int nbytes = flitSize/4;
		String s = Integer.toHexString(value).toUpperCase();
		//if String has length less than the length informed
		//then fills the more significant bytes with zeros.  
		if(s.length()<nbytes){
		   	int tam=nbytes-s.length();
		   	for(int i=0;i<tam;i++)
		   		s= "0" + s;
		}
		//if String has length more than the length informed
		//then returns the less significant bytes
		s=s.substring(s.length()-nbytes,s.length());
		return s;
	}

	/**
	* Returns the informed value in flit format. <br> 
	* The number of bytes (<i>nbytes</i>) of a flit is equal to flitSize/4. <br>
	* If the value has more bytes than <i>nbytes</i> then returns the less significant bytes. <br>
	* If the value has less bytes than <i>nbytes</i> then the more significant bytes are filled with zeros.
	* @param value The value to be formatted
	* @param flitSize The number of bits of a flit
	* @return String The value in the flit format.
	*/
	public static String formatFlit(String value,int flitSize){
		int nbytes = flitSize/4;
		//if String has length less than the length informed
		//then fills the more significant bytes with zeros.  
		if(value.length()<nbytes){
		   	int tam=nbytes-value.length();
		   	for(int i=0;i<tam;i++)
		   		value= "0" + value;
		}
		//if String has length more than the length informed
		//then returns the less significant bytes
		value=value.substring(value.length()-nbytes,value.length());
		return value;
	}

	/**
	 * Sets 1 to the <b>bit</b> more significant of the informed hexadecimal value.
	 * @param value A hexadecimal value
	 * @param flitSize The number of bits of a flit.
	 * @return The informed hexadecimal value with 1 in the bit more significant.
	 */
	public static String set1ToBitMoreSignificant(String value, int flitSize){
		String valueBin=Convert.hexToBin(value,flitSize);
		valueBin="1"+valueBin.substring(1);
		return Convert.binToHex(valueBin,flitSize/4);
	}
	
	/**
	 * Remove the substring beginning in the rightmost occurrence of str2.
	 * @param str The string. 
	 * @param str2 The str2 to be searched.
	 * @return The str without the last part beginning in st2.
	 */
	public static String removeLast(String str, String str2){
		int index = str.lastIndexOf(str2);
		if(index!=-1)
			str = str.substring(0, index);
		return str;
	}
	
	/**
	 * Test the class methods.
	 * @param s
	 */
	public static void main(String s[]){
		System.out.println(Convert.getXYAddress(2, 1, 16));
		System.out.println(Convert.getAddressY(3,4));
		System.out.println(Convert.getAddressX(3,3));
		System.out.println(Convert.getAddressY(3,3));
//		System.out.println(Convert.set1ToBitMoreSignificant("22", 16));
		
		
//		System.out.println(Convert.formatAddressFlit(1,2,8));
//		System.out.println(Convert.formatAddressFlit(1,2,16));
//		System.out.println(Convert.formatAddressFlit(1,2,32));
//		String[] timestamp = Convert.formatTimestamp("56",16);
//		for(int i=0; i<timestamp.length; i++)
//			System.out.println(timestamp[i]);

	}
}