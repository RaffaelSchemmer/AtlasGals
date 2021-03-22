/*

#------------------------------------------------------------------------#
#																		 #
#  HermesG Generator													 #
#  Data de Criação : 03/08/2011											 #
#  Última revisão  : 19/03/2013	/ 09:00 / R.Schemmer					 #
#  Codificação     : UTF-8 "Criado pela ferramenta Geany"				 #
#------------------------------------------------------------------------#

#------------------------------------------------------------------------------------------------------------#
# **Obs: Diferentes modificações foram feitas na rede HERMES-G após o desenvolvimento desta ferramenta.		 #
# Sendo assim, o autor não garante o total funcionamento desta ferramenta. Verifique sempre se para os casos #
# propóstos, se as saídas estão sendo geradas corretamente e a validação esta ocorrendo de maneira correta.  #
#------------------------------------------------------------------------------------------------------------#

#-------------------------------------------------------------------------------------------------------#
# Esta ferramenta foi criada com dois propósitos, sendo um deles em testar a rede Hermes-G (VHDL),      #
# e parte do código java da ferramenta de geração da rede feita em java () estrutura interna do         #
# gerador de rede feito em java(MainCreditBased). Além disso, ela também realiza a geração de um        #
# modelo de tráfego chamado aqui de "Broadcast", onde para um dado número X de pacotes, cada um dos     #
# transmissores irá enviar esta mesma quantidade para os demais receptores da rede. Fora isso, a        #
# ferramenta permite ainda verificar de maneira automática, se a simulação e a transmissão ocorreram    #
# de maneira adequada, verificando se os dados foram escritos na saída. Esta ferramenta permite através #
# de diferentes maneiras, definir quantidades parametrizadas de redes, tráfegos e lotes de redes a      #
# serem simuladas e posteriormente verificadas. Esta ferramenta executa única e exclusivamente através  #
# da linha de comando. Para executa-lá, na raiz da Atlas digite "java Generator.Generator".             #
#-------------------------------------------------------------------------------------------------------#

*/

package Generator;

import java.awt.*;
import java.io.*;
import javax.swing.*;
import java.util.*;
import java.text.*; 
import java.lang.*;

import AtlasPackage.*;
import HermesG.*;
import Maia.*;

public class Generator
{
	public static class ManipulateFile
	{
		private void deleteAll(File f)
		{
			if(f.isDirectory())
			{
				File[] list = f.listFiles();
				for(int i=0;i<list.length;i++)
				{
					deleteAll(list[i]);
				}
				f.delete();
			}
			else
			{
				f.delete();
			}
		}
		public static void copy(File file, String dir)
		{
			try
			{
				FileReader in = new FileReader(file);
				String filename = new String(file.getName());
				File outputFile = new File(dir+filename);
				FileWriter out = new FileWriter(outputFile);

				int c;
				while ((c = in.read()) != -1)
				{
					out.write(c);
				}

				in.close();
				out.close();
			}
			catch(Exception e)
			{
				JOptionPane.showMessageDialog(null,"Can't copy the file "+file.getName()+"\n"+ e.getMessage(),"Error Message", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
		}	
		private void deleteFiles(String directory)
		{
			File f = new File(directory);
			if(f.isDirectory()){
				File[] list = f.listFiles();
				for(int i=0;i<list.length;i++)
				{
					deleteAll(list[i]);
				}
			}
			else if(f.exists())
			{
				JOptionPane.showMessageDialog(null,"The "+f.getName()+" is not a directory.","Error in ManipulateFile.deleteFiles", JOptionPane.WARNING_MESSAGE);
			}
		}
	}
	public static class FormatFlit
	{	
		private String formatFlit(int value,int flitSize)
		{
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
		private int getNumberOfRouter(String addressXY,int dimXNoC)
		{
			String addressXHex = addressXY.substring(0, addressXY.length()/2);
			String addressYHex = addressXY.substring(addressXY.length()/2);
			int addressX = Integer.parseInt(addressXHex, 16);
			int addressY = Integer.parseInt(addressYHex, 16);
			return (addressY * dimXNoC + addressX);
		}
		private String getTarget(int x,int y,int dimX,int flitWidth,String nocType)
		{
			String source="",saida="";
			if(Integer.toString(x).length() == 2) source = Integer.toHexString(x);
			else source = Integer.toString(x);
						
			if(Integer.toString(y).length() == 2) source = source + Integer.toHexString(y);
			else source = source + Integer.toString(y);
						
			String x1  = "",y1 = "";
			if(source.length() == 4)
			{
				 x1 = ""+source.charAt(0) + source.charAt(1);
				 x1 = Integer.toHexString(Integer.parseInt(x1));
				 y1 = ""+source.charAt(2) + source.charAt(3);
				 y1 = Integer.toHexString(Integer.parseInt(y1));
			}
			else if(source.length() == 2)
			{
				x1 = Integer.toHexString(Integer.parseInt(""+source.charAt(0)));
				y1 = Integer.toHexString(Integer.parseInt(""+source.charAt(1)));
			}
			int iTarget = this.getNumberOfRouter(""+x1+y1, dimX);
			if(flitWidth == 8)
			{
				// Converte x para bin
				x = Integer.parseInt(""+source.charAt(0));
				String xs = Convert.decToBin(x,2);
				// Converte y para bin
				y = Integer.parseInt(""+source.charAt(1));
				String ys = Convert.decToBin(y,2);
				// Junta e converte para decimal
				String fs = "" + xs + ys;
				int decimal = Integer.parseInt(fs, 2);
				saida = ("0" + Convert.decToHex(decimal,1).toUpperCase());
			}
			else if(flitWidth == 16)
				saida = ("00" + x1 + y1);
			else if(flitWidth == 32) 
				saida = ("00000" + x1 + "0" + y1);
			else if(flitWidth == 64) 
				saida = ("00000000000" + x1 + "000" + y1);					
			return(saida);
		}
		private String getSource(int x,int y,int dimX,int flitWidth,String nocType)
		{

			String source="",saida="";
			if(Integer.toString(x).length() == 2) source = Integer.toHexString(x);
			else source = Integer.toString(x);
						
			if(Integer.toString(y).length() == 2) source = source + Integer.toHexString(y);
			else source = source + Integer.toString(y);
						
			String x1  = "",y1 = "";
			if(source.length() == 4)
			{
				 x1 = ""+source.charAt(0) + source.charAt(1);
				 x1 = Integer.toHexString(Integer.parseInt(x1));
				 y1 = ""+source.charAt(2) + source.charAt(3);
				 y1 = Integer.toHexString(Integer.parseInt(y1));
			}
			else if(source.length() == 2)
			{
				x1 = Integer.toHexString(Integer.parseInt(""+source.charAt(0)));
				y1 = Integer.toHexString(Integer.parseInt(""+source.charAt(1)));
			}
			int iTarget = this.getNumberOfRouter(""+x1+y1, dimX);
			if(flitWidth == 8)
			{
				// Converte x para bin
				x = Integer.parseInt(""+source.charAt(0));
				String xs = Convert.decToBin(x,2);
				// Converte y para bin
				y = Integer.parseInt(""+source.charAt(1));
				String ys = Convert.decToBin(y,2);
				// Junta e converte para decimal
				String fs = "" + xs + ys;
				int decimal = Integer.parseInt(fs, 2);
				saida = ("0" + Convert.decToHex(decimal,1).toUpperCase());
			}
			else if(flitWidth == 16)
				saida = ("00" + x1 + y1);
			else if(flitWidth == 32) 
				saida = ("00000" + x1 + "0" + y1);
			else if(flitWidth == 64) 
				saida = ("00000000000" + x1 + "000" + y1);					
			return(saida);	
		}
		private String formatStringLength(String s,int length)
		{
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
		private String[] formatTimestamp(String value, int flitSize)
		{
			String[] timestampHex=new String[4];
			value = formatStringLength(value,flitSize);
			for(int i=0,j=flitSize;i<4;i++,j=j-(flitSize/4)){
				timestampHex[i] = value.substring(j-(flitSize/4),j);
			}
			return timestampHex;
		}
		public String decToHex(int val, int length)
		{
			String hex = Integer.toHexString(val);
			return formatStringLength(hex,length);
		}
		public String binToHex(String bin,int length)
		{
			int decimal = Integer.parseInt(bin, 2);
			return decToHex(decimal, length);
		}
		public String decToBin(int val,int length)
		{
			String bin = Integer.toBinaryString(val);
			return formatStringLength(bin,length);
		}
		public String getXYAddress(int n, int dimXNoC, int flitSize)
		{
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
		public String getRoutingAlgorithmSelected(String opt)
		{
			if(opt.equals("0"))
				return "AlgorithmXY";
			else if(opt.equals("1"))
				return "AlgorithmWFM";
			else if(opt.equals("2"))
				return "AlgorithmWFNM";
			else if(opt.equals("3"))
				return "AlgorithmNLM";
			else if(opt.equals("4"))
				return "AlgorithmNLNM";
			else if(opt.equals("5"))
				return "AlgorithmNFM";
			else if(opt.equals("6"))
				return "AlgorithmNFNM";
			return "";
		}
		public void getNoc_Ip_Clocks(int type,Project project)
		{
			if(type == 1)
			{
				// Sorteia um número entre 0 e (X*Y)
				int num_clocks = (int)(Math.random()*(project.getNoC().getNumRotX()*project.getNoC().getNumRotY()));
				
				if(num_clocks == 1) num_clocks = 2;
				// Cria por padrão clock default
				AvailableClock v = new AvailableClock();
				v.setAllAvailableValue("defClock",50,"Mhz");
				project.getNoC().setClockList(v);
				
				// Vincula clocks para os elementos da rede
				for(int x=0;x<project.getNoC().getNumRotY();x++)
				{
					for(int y=0;y<project.getNoC().getNumRotX();y++)
					{
						Clock c = new Clock();
						
						c.setAllClocks("" + x + " " + y + " " + "defClock" + " " + 50 + " " + "Mhz" + " " + "defClock" + " " + 50 + " " + "Mhz" + " " + "defClock" + " " + 50 + " " + "Mhz");
						
						project.getNoC().addClock(c);
					}
				}
				
				ArrayList<String> label = new ArrayList<String>();
				// Cria clocks em availableClock
				for(int tmp=0;tmp < num_clocks;tmp++)
				{
					AvailableClock a = new AvailableClock();
					
					int value = (int)(Math.random()*5000);
					
					int flag=0;
					for(int tmp1=0;tmp1 < label.size();tmp1++)
					{
						if(label.get(tmp1).equals("Clock"+value)) { flag = 1; break; }
					}
					
					if(flag == 0)
					{
						a.setAllAvailableValue("Clock"+value,value,"Mhz");
						project.getNoC().setClockList(a);
						label.add(""+"Clock"+value);
					}
					else tmp--;
				}
				
				// Vincula os clocks cadastrados
				for(int tmp1=0;tmp1 < num_clocks;tmp1++)
				{
					// Endereço que vai receber o clock availableclock posição tmp1
					
					int target = (int)(Math.random()*(project.getNoC().getNumRotX()*project.getNoC().getNumRotY()));
					// Cálcula de 0-3 quantos elementos da chave (Router/Ip in/Ip Out vão receber novo clock)
					int num_elem = (int)(Math.random()*3);
					if(num_elem == 0)
					{
						project.getNoC().getClock().get(target).setRouter(project.getNoC().getClockList(tmp1).getAllAvailableValue());
					} 
					else if(num_elem == 1)
					{
						project.getNoC().getClock().get(target).setRouter(project.getNoC().getClockList(tmp1).getAllAvailableValue());
						project.getNoC().getClock().get(target).setIpInput(project.getNoC().getClockList(tmp1).getAllAvailableValue());
					}
					else if(num_elem == 2)
					{
						project.getNoC().getClock().get(target).setRouter(project.getNoC().getClockList(tmp1).getAllAvailableValue());
						project.getNoC().getClock().get(target).setIpInput(project.getNoC().getClockList(tmp1).getAllAvailableValue());
						project.getNoC().getClock().get(target).setIpOutput(project.getNoC().getClockList(tmp1).getAllAvailableValue());
					}
				}
						
			}
			else if(type == 0)
			{
				AvailableClock v = new AvailableClock();
				v.setAllAvailableValue("defClock",50,"Mhz");
				project.getNoC().setClockList(v);
				
				for(int x=0;x<project.getNoC().getNumRotY();x++)
				{
					for(int y=0;y<project.getNoC().getNumRotX();y++)
					{
						Clock c = new Clock();
						
						c.setAllClocks("" + x + " " + y + " " + "defClock" + " " + 50 + " " + "Mhz" + " " + "defClock" + " " + 50 + " " + "Mhz" + " " + "defClock" + " " + 50 + " " + "Mhz");
						
						project.getNoC().addClock(c);
					}
				}
			}
		}
		public void genScriptExec(String path,ArrayList<String> names,int part)
		{
			if(part == 0)
			{
				try
				{
					FileOutputStream f = new FileOutputStream(path+ File.separator + "simulate");
					Process p=Runtime.getRuntime().exec("chmod 777 -Rf " + path + File.separator + "simulate");
					DataOutputStream dos =new DataOutputStream(f);	
					
					for(int tmp=0;tmp < names.size();tmp++)
					{		
						dos.writeBytes("cd " + names.get(tmp) + File.separator + "\n");
						dos.writeBytes("vsim -c -do simulate.do\n");
						if(tmp < names.size()-1) dos.writeBytes("cd ..\n");
					}
					dos.close();	
				}
				catch(FileNotFoundException f)
				{
					JOptionPane.showMessageDialog(null,"","Input Error", JOptionPane.ERROR_MESSAGE);
				}
				catch(Exception e)
				{
					JOptionPane.showMessageDialog(null,"","Input Error", JOptionPane.ERROR_MESSAGE);	
				}
			}
			else
			{
				try
				{
					int total = names.size()/4;
					for(int tmp1=1,cont=0;tmp1 <= 4;tmp1++)
					{
						FileOutputStream f = new FileOutputStream(path+ File.separator + "simulate" + tmp1);
						Process p=Runtime.getRuntime().exec("chmod 777 -Rf " + path + File.separator + "simulate" + tmp1);
						DataOutputStream dos =new DataOutputStream(f);	
						
						for(int tmp=0;tmp < total;tmp++,cont++)
						{		
							dos.writeBytes("cd " + names.get(cont) + File.separator + "\n");
							dos.writeBytes("vsim -c -do simulate.do\n");
							if(cont < names.size()-1) dos.writeBytes("cd ..\n");
						}
					
						if(names.size()%4 != 0)
						{	
							dos.writeBytes("cd " + names.get(cont) + File.separator + "\n");
							dos.writeBytes("vsim -c -do simulate.do\n");
							if(cont < names.size()-1) dos.writeBytes("cd ..\n");
						}	
						dos.close();	
					}
				}
				catch(FileNotFoundException f)
				{
					JOptionPane.showMessageDialog(null,"","Input Error", JOptionPane.ERROR_MESSAGE);
				}
				catch(Exception e)
				{
					JOptionPane.showMessageDialog(null,"","Input Error", JOptionPane.ERROR_MESSAGE);	
				}
			}
		}
		public void genScriptVis(String path,ArrayList<String> names,int part,int noc_type)
		{
			if(part == 0)
			{
				try
				{
					FileOutputStream f = new FileOutputStream(path+ File.separator + "visualize");
					Process p=Runtime.getRuntime().exec("chmod 777 -Rf " + path + File.separator + "visualize");
					DataOutputStream dos =new DataOutputStream(f);	
					int total = names.size();
					for(int tmp=0;tmp < names.size();tmp++)
					{		
						dos.writeBytes("java Generator/Generator 4 ");
						for(int tmp2=0;tmp2 < names.get(tmp).length();tmp2++)
						{
							if(names.get(tmp).charAt(tmp2) == '_')
								dos.writeBytes(" ");
							else	
							dos.writeBytes(""+names.get(tmp).charAt(tmp2));
						}
						if( noc_type == 1) dos.writeBytes(" 0 0");
						dos.writeBytes("\n");
					}
					dos.close();	
				}
				catch(FileNotFoundException f)
				{
					JOptionPane.showMessageDialog(null,"","Input Error", JOptionPane.ERROR_MESSAGE);
				}
				catch(Exception e)
				{
					JOptionPane.showMessageDialog(null,"","Input Error", JOptionPane.ERROR_MESSAGE);	
				}
			}
			else
			{
				try
				{
					int total = names.size()/4;
					for(int tmp1=1,cont=0;tmp1 <= 4;tmp1++)
					{
						FileOutputStream f = new FileOutputStream(path+ File.separator + "visualize" + tmp1);
						Process p=Runtime.getRuntime().exec("chmod 777 -Rf " + path + File.separator + "visualize" + tmp1);
						DataOutputStream dos =new DataOutputStream(f);	
						
						for(int tmp=0;tmp < total;tmp++,cont++)
						{		
							dos.writeBytes("java Generator/Generator 4 ");
							for(int tmp2=0;tmp2 < names.get(cont).length();tmp2++)
							{
								if(names.get(cont).charAt(tmp2) == '_')
									dos.writeBytes(" ");
								else	
								dos.writeBytes(""+names.get(cont).charAt(tmp2));
							}
							if( noc_type == 1) dos.writeBytes(" 0 0");
							dos.writeBytes("\n");
						}
					
						if(names.size()%4 != 0)
						{	
							dos.writeBytes("java Gerador 4 ");
							for(int tmp2=0;tmp2 < names.get(cont).length();tmp2++)
							{
								if(names.get(cont).charAt(tmp2) == '_')
									dos.writeBytes(" ");
								else	
									dos.writeBytes(""+names.get(cont).charAt(tmp2));
							}
							if( noc_type == 1) dos.writeBytes(" 0 0");
							dos.writeBytes("\n");
						}	
						dos.close();	
					}
				}
				catch(FileNotFoundException f)
				{
					JOptionPane.showMessageDialog(null,"","Input Error", JOptionPane.ERROR_MESSAGE);
				}
				catch(Exception e)
				{
					JOptionPane.showMessageDialog(null,"","Input Error", JOptionPane.ERROR_MESSAGE);	
				}
			}
		}
	}
	public static void main(String s[])
	{
		if(s.length ==  0 || s.length < 10) 
		{ 
			System.out.println("\n\n\n");
			
			System.out.print("|| --------------------------------------------------------------------------------------------------------------------------------- ||\n");
			System.out.print("|| |||||                        Gerador e verificador de tráfego sintético para redes HERMES e HERMES-G                        ||||| ||\n");
			System.out.print("|| --------------------------------------------------------------------------------------------------------------------------------- ||\n");
			System.out.print("||                                                                                                                                   ||\n");
			System.out.print("|| --------------------------------------------------------------------------------------------------------------------------------- ||\n");
			System.out.print("||  Java Gerador   :     TrafficOp | NumMaxX | NumMaxY | FlitWidth | TamPckt | NoCType | BufDepth | BufType | AlgoType | ClockType   ||\n");
			System.out.print("|| --------------------------------------------------------------------------------------------------------------------------------- ||\n");
			System.out.print("||                                                                                                                                   ||\n");
			System.out.print("|| Example : java Gerador 2 2 16 16 0 4 J 1 1                                                                                        ||\n");
			System.out.print("||                                                                                                                                   ||\n");
			System.out.print("|| Obs :                                                                                                                             ||\n");
			System.out.print("||                                                                                                                                   ||\n");
			System.out.print("||          TrafficOp :  (0) Gen Traffic (1) Ver Traffic (2) Gen NoCs (3) Gen NoCs + Traffic (4) Ver Traffic Ger (5) Gen Auto Cenery ||\n");
			System.out.print("||          FlitWidth :  (8) bits (16) bits (32) bits (64) bits                                                                      ||\n");
			System.out.print("||          NocType   :  (0) HERMES-G    (1) HERMES                                                                                  ||\n");
			System.out.print("||          BufDepth  :  (8) flits (16) flits (32) flits                                                                             ||\n");
			System.out.print("||          BufType   :  (J) Johnson Coding (G) Gray Coding                                                                          ||\n");
			System.out.print("||          ClockType :  (0) DefClock 50 Mhz (1) Random 0Mhz - 5Ghz                                                                  ||\n");
			System.out.print("||          AlgoType  :  (0) XY (1) WFM (2) WFNM (3) NLM (4) NLNM (5) NFM (6) NFNM                                                   ||\n");
			System.out.print("||                                                                                                                                   ||\n");
			System.out.print("||                           *********** Warning : This Software dont verifies wrong values !!! ***********                          ||\n");
			System.out.print("|| |||||                                                                                                                       ||||| ||\n");
			System.out.print("|| --------------------------------------------------------------------------------------------------------------------------------- ||\n");
			System.out.println("\n\n\n");
			
		}
		else
		{
			String Path = "/home/raffael/Desktop/trafegos/";
			String Dir = Path;
			// Aceita de 2 a 16
			int X = Integer.parseInt(s[1]);
			// Aceita de 2 a 16
			int Y = Integer.parseInt(s[2]);
			// Aceita de 4 16 32 64
			int flitWidth = Integer.parseInt(s[3]); 
			
			FormatFlit ff = new FormatFlit();
			ManipulateFile mf = new ManipulateFile();
			
			// Número de flits por pacote
			int tamPckt = Integer.parseInt(s[4]);
			Path = Path + s[1] + "_" + s[2] + "_" + s[3] + "_" + s[4];
			if(Integer.parseInt(s[5]) == 0) Path = Path + "_" + "HG" + File.separator;
			else if(Integer.parseInt(s[5]) == 1) Path = Path + "_" + "H" + File.separator;
			
			if(Integer.parseInt(s[0]) == 0) // Gerador de tráfego broadcast para redes HERMES/HERMES-G
			{
				File TrDir = new File(Path);
				mf.deleteFiles(Path);
				TrDir.mkdirs();
				int sequenceNumberH=0,sequenceNumberL=1;
				int timestamp = 1;
				try
				{
					for(int x1=0;x1 < X;x1++)
					{
						for(int y2=0;y2 < Y;y2++)
						{
							int end = y2 * X + x1;
							timestamp = 1;
							FileOutputStream f = new FileOutputStream(Path + File.separator + "in" + end +".txt");
							DataOutputStream data_output =new DataOutputStream(f);	
							for(int x=0;x < X;x++)
							{
								for(int y=0;y < Y;y++)
								{
									String[] timestampHex;
									
									int tmp3 = y * X + x;
									if(end != tmp3)
									{
										// TS || Target || Tam || Source || TimestampAbs || NumSeq || Payload
										// 1 0001 000A 0000 0000 0000 0000 0001 0000 0001 0008 0009 000A
										
										// TS
										data_output.writeBytes(""+timestamp+" ");
										timestamp = timestamp + 1;
										
										// Target
										data_output.writeBytes(""+ff.getTarget(x,y,X,flitWidth,s[5])+" ");
										
										// Tam
										data_output.writeBytes(""+ff.formatFlit((tamPckt-6),flitWidth)+" ");

										// Source
										data_output.writeBytes("" + ff.getSource(x1,y2,X,flitWidth,s[5]) + " ");
									
										// TimestampAbs
										timestampHex = ff.formatTimestamp(Integer.toString(timestamp), flitWidth);
										data_output.writeBytes(""+timestampHex[3]+" "+timestampHex[2]+" "+timestampHex[1]+" "+timestampHex[0]+" ");
										
										// NumSeq
										
									
										data_output.writeBytes(""+ff.formatFlit(sequenceNumberH, flitWidth) + " " + ff.formatFlit(sequenceNumberL, flitWidth) + " ");

										//increments the sequence number
										if(sequenceNumberL == (Math.pow(2,flitWidth)-1))
										{
											sequenceNumberH++;
											sequenceNumberL=0;
										}
										else
											sequenceNumberL++;
										
										// Payload
										for(int tmp2=13,cont=0;tmp2 < tamPckt;tmp2++,cont++)
										{
											data_output.writeBytes(ff.formatFlit(tmp2,flitWidth)+" ");
										}
										data_output.writeBytes("\n");
										
									}
								}
							}
						}
					}
					System.out.println("\nTráfego solicitado gerado com sucesso !!\n");
				}
				catch(Exception e)
				{
					JOptionPane.showMessageDialog(null,e.getMessage(),"Error Message", JOptionPane.ERROR_MESSAGE);
				}
			}
			else if(Integer.parseInt(s[0]) == 1) // Verificador de redes HERMES/HERMES-G (1)
			{
				System.out.println("\n");
				int sum=0,total=0;
				for(int x1=0;x1 < X;x1++)
				{
					for(int y2=0;y2 < Y;y2++)
					{
						int end = y2 * X + x1;
						StringTokenizer st;
						String line, word, change_parameter;
						try
						{
							FileInputStream inFile = new FileInputStream(new File(Path + "out" + end + ".txt"));
							BufferedReader buff=new BufferedReader(new InputStreamReader(inFile));	    
							
							int n_linesg=0,n_linesl=0;
							change_parameter="";
							line=buff.readLine();
							ArrayList<String> source = new ArrayList<String>();
							while(line!=null)
							{
								String data[] = line.split(" ");
								int cont=0;
								
								// Target -- precisa ser ele mesmo
								
								String tmps1 = ff.getTarget(x1,y2,X,flitWidth,s[5]).toUpperCase();
								String tmps2 = data[0].toUpperCase();
								
								if(tmps1.equals(tmps2)) cont++;
								int x = (tamPckt-2);
								
								tmps1 = ff.formatFlit(x,flitWidth).toUpperCase();
								tmps2 = data[1].toUpperCase();
								
								if(tmps1.equals(tmps2)) cont++;
								
								int flag = 1;
								for(int tmp=0;tmp < source.size();tmp++)
								{
									if(source.get(tmp).equals(data[2])) { flag = 0; break; } 
								}
								
								if(flag == 1) cont++;
								
								if(Integer.parseInt(s[5]) == 0)
									x = (tamPckt+2);	
								else if(Integer.parseInt(s[5]) == 1)
									x = (tamPckt+9);	
								// Tamanho total do pacote
								if(data.length == x) cont++;
								line = buff.readLine();
								if(cont == 4) n_linesg++;
								else
								{
									n_linesl++;
								}
							}
							if(n_linesg != ((X*Y)-1)) { System.out.println("Arquivo out" + end + ".txt deveria ter " + ((X*Y)-1) + " linhas, foram encontradas " + n_linesg + " linhas !!"); sum = sum + n_linesg; } 
							else { System.out.println("Arquivo out" + end + ".txt OK"); total++;}
						}
						catch(FileNotFoundException f)
						{
							JOptionPane.showMessageDialog(null,"Não foi encontrado tráfego (Out0.txt) na rede selecionada","Read error", JOptionPane.ERROR_MESSAGE);
							System.exit(0);
						}
						catch(Exception e)
						{
							JOptionPane.showMessageDialog(null,e.getMessage(),"Error message", JOptionPane.ERROR_MESSAGE);
							System.exit(0);
						}
					}
				}
				if(total == (X*Y)) { System.out.println("\n\n || || || TRÁFEGO TRANSMITIDO COM SUCESSO || || ||\n\n"); } 
				else
				{
					System.out.println("\n\n**!!**       ERRO AO TRANSMITIR O TRÁFEGO DE " + (((X*Y)-1)*(X*Y)) + "       **!!**"); 
					System.out.println("**!!**      " + sum + " Pacotes não chegaram aos destinos      **!!**\n\n");
				} 
			}
			else if(Integer.parseInt(s[0]) == 2) // Gerador de redes HERMES/HERMES-G
			{
				
				// ****************************************************************************** //
				//                  Bloco de geração de redes HERMES/HERMESG                      //
				// ***************************************************************************** //
				
				Project project = null;
				
				String workspaceDir = Path;
				String projectDir = Path;

				Default.setWorkspacePath(workspaceDir);
				Default.setProjectPath(projectDir);
				
				String name = s[1] + "_" + s[2] + "_" + s[3] + "_" + s[4] + "_" + s[5];
				
				if(Integer.parseInt(s[5]) == 0)
				{
					name = name + "_" + s[6] + "_" + s[7] + "_" + s[8] + "_" + s[9];
					project = new Project(Dir,name,"HermesG");
				}
				else if(Integer.parseInt(s[5]) == 1)
				{
					name = name + "_" + s[6] + "_" + s[8];
					project = new Project(Dir,name,"Hermes");
				}
				
				project.getNoC().setNumRotX(X);
				project.getNoC().setNumRotY(Y);
				
				project.getNoC().setFlitSize(Integer.parseInt(s[3]));
				project.getNoC().setBufferDepth(Integer.parseInt(s[6]));
				
				project.getNoC().setAlgorithm(ff.getRoutingAlgorithmSelected(s[8]));
				
				project.setNoCGenerate(true);
				project.setTrafficGenerate(false);
				
				project.getNoC().setCyclesPerFlit(1);
				project.getNoC().setCyclesToRoute(5);
	
				if(Integer.parseInt(s[5]) == 0)
				{
					if(s[7].equals("J")) { project.getNoC().setbufferCoding("Johnson"); } 
					else if(s[7].equals("G")) project.getNoC().setbufferCoding("Gray");
					ff.getNoc_Ip_Clocks(Integer.parseInt(s[9]),project);
				}
				project.setTb("1");
					
				project.write();
				
				if(Integer.parseInt(s[5]) == 0)
				{
					
					String sourceDir = Default.atlashome + File.separator + "HermesG" + File.separator + "Data" + File.separator + "CreditBased" + File.separator;
					
					String scDir = Dir + name + File.separator + "SC_NoC" + File.separator;
					
					
					HermesGCreditBased creditBased = new HermesGCreditBased(project);
					NoCGeneration n = new NoCGeneration(project,sourceDir);
					creditBased.generate();
					
					File sDir=new File(scDir);
					sDir.mkdirs();
					
					ManipulateFile.copy(new File(sourceDir + "SC_InputModule.cpp"), scDir);
					ManipulateFile.copy(new File(sourceDir + "SC_OutputModule.cpp"), scDir);
					
					creditBased.createSimulate();
					n.generatetopNoC();
					n.generateSystemCOutputModule();
					n.generateSystemCIntputModule();
					n.generateAsyncFifo();
				}
				else if (Integer.parseInt(s[5]) == 1)
				{
					MainCreditBased creditBased = new MainCreditBased(project);
					creditBased.generate();
				}
				project.write();
				
				// ff.genScript(Dir,name);
				
				System.out.println("\nRede gerada com sucesso !!");
			}
			else if(Integer.parseInt(s[0]) == 3) // Gerador de redes HERMES/HERMES-G e gerador de tráfego broadcast para redes HERMES/HERMES-G
			{
				
				// ****************************************************************************** //
				//                  Bloco de geração de redes HERMES/HERMESG                      //
				// ***************************************************************************** //
				
				Project project = null;
				
				String workspaceDir = Path;
				String projectDir = Path;

				Default.setWorkspacePath(workspaceDir);
				Default.setProjectPath(projectDir);
				
				String name = s[1] + "_" + s[2] + "_" + s[3] + "_" + s[4] + "_" + s[5];
				
				if(Integer.parseInt(s[5]) == 0)
				{
					name = name + "_" + s[6] + "_" + s[7] + "_" + s[8] + "_" + s[9];
					project = new Project(Dir,name,"HermesG");
				}
				else if(Integer.parseInt(s[5]) == 1)
				{
					name = name + "_" + s[6] + "_" + s[8];
					project = new Project(Dir,name,"Hermes");
				}
				
				project.getNoC().setNumRotX(X);
				project.getNoC().setNumRotY(Y);
				
				project.getNoC().setFlitSize(Integer.parseInt(s[3]));
				project.getNoC().setBufferDepth(Integer.parseInt(s[6]));
				
				project.getNoC().setAlgorithm(ff.getRoutingAlgorithmSelected(s[8]));
				
				project.setNoCGenerate(true);
				project.setTrafficGenerate(false);
				
				project.getNoC().setCyclesPerFlit(1);
				project.getNoC().setCyclesToRoute(5);
	
				if(Integer.parseInt(s[5]) == 0)
				{
					if(s[7].equals("J")) { project.getNoC().setbufferCoding("Johnson"); } 
					else if(s[7].equals("G")) project.getNoC().setbufferCoding("Gray");
					ff.getNoc_Ip_Clocks(Integer.parseInt(s[9]),project);
				}
				project.setTb("1");
					
				project.write();
				
				if(Integer.parseInt(s[5]) == 0)
				{
					
					String sourceDir = Default.atlashome + File.separator + "HermesG" + File.separator + "Data" + File.separator + "CreditBased" + File.separator;
					
					String scDir = Dir + name + File.separator + "SC_NoC" + File.separator;
					
					
					HermesGCreditBased creditBased = new HermesGCreditBased(project);
					NoCGeneration n = new NoCGeneration(project,sourceDir);
					creditBased.generate();
					
					File sDir=new File(scDir);
					sDir.mkdirs();
					
					ManipulateFile.copy(new File(sourceDir + "SC_InputModule.cpp"), scDir);
					ManipulateFile.copy(new File(sourceDir + "SC_OutputModule.cpp"), scDir);
					
					creditBased.createSimulate();
					n.generatetopNoC();
					n.generateSystemCOutputModule();
					n.generateSystemCIntputModule();
					n.generateAsyncFifo();
				}
				else if (Integer.parseInt(s[5]) == 1)
				{
					MainCreditBased creditBased = new MainCreditBased(project);
					creditBased.generate();
				}
				project.write();
				
				// ff.genScript(Dir,name);
				
				System.out.println("\nRede gerada com sucesso !!");
				
				// ***************************************************************************************************** //
				//                  Bloco de geração de tráfego broadcast para redes HERMES/HERMESG                      //
				// ***************************************************************************************************** //
				
				
				int sequenceNumberH=0,sequenceNumberL=1;
				int timestamp = 1;
				try
				{
					for(int x1=0;x1 < X;x1++)
					{
						for(int y2=0;y2 < Y;y2++)
						{
							int end = y2 * X + x1;
							timestamp = 1;
							FileOutputStream f = new FileOutputStream(Dir + name + File.separator + "in" + end +".txt");
							DataOutputStream data_output =new DataOutputStream(f);	
							for(int x=0;x < X;x++)
							{
								for(int y=0;y < Y;y++)
								{
									String[] timestampHex;
									
									int tmp3 = y * X + x;
									if(end != tmp3)
									{
										// TS || Target || Tam || Source || TimestampAbs || NumSeq || Payload
										// 1 0001 000A 0000 0000 0000 0000 0001 0000 0001 0008 0009 000A
										
										// TS
										data_output.writeBytes(""+timestamp+" ");
										timestamp = timestamp + 10;
										
										// Target
										data_output.writeBytes(""+ff.getTarget(x,y,X,flitWidth,s[5])+" ");
										
										// Tam
										data_output.writeBytes(""+ff.formatFlit((tamPckt-6),flitWidth)+" ");

										// Source
										data_output.writeBytes("" + ff.getSource(x1,y2,X,flitWidth,s[5]) + " ");
									
										// TimestampAbs
										timestampHex = ff.formatTimestamp(Integer.toString(timestamp), flitWidth);
										data_output.writeBytes(""+timestampHex[3]+" "+timestampHex[2]+" "+timestampHex[1]+" "+timestampHex[0]+" ");
										
										// NumSeq
										
									
										data_output.writeBytes(""+ff.formatFlit(sequenceNumberH, flitWidth) + " " + ff.formatFlit(sequenceNumberL, flitWidth) + " ");

										//increments the sequence number
										if(sequenceNumberL == (Math.pow(2,flitWidth)-1))
										{
											sequenceNumberH++;
											sequenceNumberL=0;
										}
										else
											sequenceNumberL++;
										
										// Payload
										for(int tmp2=13,cont=0;tmp2 < tamPckt;tmp2++,cont++)
										{
											data_output.writeBytes(ff.formatFlit(tmp2,flitWidth)+" ");
										}
										data_output.writeBytes("\n");
										
									}
								}
							}
						}
					}
					System.out.println("\nTráfego solicitado gerado com sucesso !!\n");
				}
				catch(Exception e)
				{
					JOptionPane.showMessageDialog(null,e.getMessage(),"Error Message", JOptionPane.ERROR_MESSAGE);
				}				
			}
			else if(Integer.parseInt(s[0]) == 4) // Verificador de tráfego de redes HERMES/HERMES-G geradas pelo item (3)
			{
				System.out.println("\n");
				
				String name = s[1] + "_" + s[2] + "_" + s[3] + "_" + s[4] + "_" + s[5];
				
				if(Integer.parseInt(s[5]) == 0)
				{
					name = name + "_" + s[6] + "_" + s[7] + "_" + s[8] + "_" + s[9];
				}
				else if(Integer.parseInt(s[5]) == 1)
				{
					name = name + "_" + s[6] + "_" + s[8];
				}
				int sum=0,total=0;
				for(int x1=0;x1 < X;x1++)
				{
					for(int y2=0;y2 < Y;y2++)
					{
						int end = y2 * X + x1;
						StringTokenizer st;
						String line, word, change_parameter;
						try
						{
							FileInputStream inFile = new FileInputStream(new File(Dir + name + File.separator + "out" + end + ".txt"));
							BufferedReader buff=new BufferedReader(new InputStreamReader(inFile));	    
							
							int n_linesg=0,n_linesl=0;
							change_parameter="";
							line=buff.readLine();
							ArrayList<String> source = new ArrayList<String>();
							while(line!=null)
							{
								String data[] = line.split(" ");
								int cont=0;
								
								// Target -- precisa ser ele mesmo
								
								String tmps1 = "";
								String tmps2 = "";
								
								tmps1 = ff.getTarget(x1,y2,X,flitWidth,s[5]).toUpperCase();
								tmps2 = data[0].toUpperCase();
								
								if(tmps1.equals(tmps2)) cont++;
								
								int x = (tamPckt-2);
								
								tmps1 = ff.formatFlit(x,flitWidth).toUpperCase();
								tmps2 = data[1].toUpperCase();
								
								if(tmps1.equals(tmps2)) cont++;
								
								// Source
								int flag = 1;
								for(int tmp=0;tmp < source.size();tmp++)
								{
									if(source.get(tmp).equals(data[2])) { flag = 0; break; } 
								}
								
								if(flag == 1) cont++;
								
								if(Integer.parseInt(s[5]) == 0)
									x = (tamPckt+2);	
								else if(Integer.parseInt(s[5]) == 1)
									x = (tamPckt+9);	
								
								// Tamanho total do pacote
								if(data.length == x) cont++;
								line = buff.readLine();
								if(cont == 4) n_linesg++;
								else
								{
									n_linesl++;
								}
							}
							if(n_linesg != ((X*Y)-1)) { System.out.println("Arquivo out" + end + ".txt deveria ter " + ((X*Y)-1) + " linhas, foram encontradas " + n_linesg + " linhas !!"); sum = sum + n_linesg; } 
							else { System.out.println("Arquivo out" + end + ".txt OK"); total++;}
						}
						catch(FileNotFoundException f)
						{
							JOptionPane.showMessageDialog(null,"Não foi encontrado tráfego (Out0.txt) na rede selecionada","Read error", JOptionPane.ERROR_MESSAGE);
							System.exit(0);
						}
						catch(Exception e)
						{
							JOptionPane.showMessageDialog(null,e.getMessage(),"Error message", JOptionPane.ERROR_MESSAGE);
							System.exit(0);
						}
					}
				}
				if(total == (X*Y)) { System.out.println("\n\n || || || Rede : " + name + "TRÁFEGO TRANSMITIDO COM SUCESSO || || ||\n\n"); } 
				else
				{
					System.out.println("\n\n**!!**       ERRO AO TRANSMITIR O TRÁFEGO DE " + (((X*Y)-1)*(X*Y)) + "       **!!**"); 
					System.out.println("**!!**      " + sum + " Pacotes não chegaram aos destinos      **!!**\n\n");
				} 
			}
			else if(Integer.parseInt(s[0]) == 5) // Gera cenários de rede definidos em constantes
			{
				
				
				// ------------------------------------------------------------------------------------------------------//
				// ------------------------------------------------------------------------------------------------------//
				// Definicão de parametros: (MODIFIQUE ABAIXO OS VALORES E RECOMPILE NOVAMENTE A FERRAMENTA!)
				// ------------------------------------------------------------------------------------------------------//
				// ------------------------------------------------------------------------------------------------------//
				
				// TrafficOp | NumMaxX | NumMaxY | FlitWidth | TamPckt | NoCType | BufDepth | BufType | AlgoType | ClockType
				 
				int dim_x_min   = 2;
				int dim_x_max   = 5;
				
				int dim_y_min   = 2;
				int dim_y_max   = 5;
				
				int flit_w_min = 16;
				int flit_w_max = 16;
				
				int buf_depth_min = 4;
				int buf_depth_max = 4;
				
				int tam_pckt = 16;
				
				int noc_type = 0; // 0 HERMES-G 1 HERMES

				int buf_type = 0; // 0 - Jonhson 1 - Gray
				
				int algo_type_min = 0; // (0) XY (1) WFM (2) WFNM (3) NLM (4) NLNM (5) NFM (6) NFNM 
				int algo_type_max = 6; // Redes HERMES-G não suportam outros algoritimos de roteamento
				
				int clock_type = 0; // (0) DefClock 50 Mhz (1) Random 0Mhz - 5Ghz
				
				int part = 1; // Particionar scripts
				
				// ------------------------------------------------------------------------------------------------------//
				// ------------------------------------------------------------------------------------------------------//
				// ------------------------------------------------------------------------------------------------------//
				int cont2=0;
				ArrayList<String> noc_names = new ArrayList<String>(); 
				for(int x=dim_x_min;x<=dim_x_max;x++)
				{
					for(int y=dim_y_min;y<=dim_y_max;y++)
					{
						for(int fw=flit_w_min;fw<=flit_w_max;fw=fw+fw)
						{
							for(int bd=buf_depth_min;bd<=buf_depth_max;bd=bd+bd)
							{
								for(int algo=algo_type_min;algo<=algo_type_max;algo++)
								{
									// ****************************************************************************** //
									//                  Bloco de geração de redes HERMES/HERMESG                      //
									// ***************************************************************************** //
									
									Project project = null;
									
									String workspaceDir = Path;
									String projectDir = Path;

									Default.setWorkspacePath(workspaceDir);
									Default.setProjectPath(projectDir);
									
									
									// Noc Name 
									String name = "" + x + "_" + y + "_" + fw + "_" + tam_pckt + "_" + noc_type;
									if(noc_type == 0)
									{
										name = name + "_" + bd + "_" + buf_type + "_" + algo + "_" + clock_type;
										project = new Project(Dir,name,"HermesG");
									}
									else if(noc_type == 1)
									{
										name = name + "_" + bd + "_" + algo;
										project = new Project(Dir,name,"Hermes");
									}
									
									noc_names.add(name);
									
									project.getNoC().setNumRotX(x);
									project.getNoC().setNumRotY(y);
									
									project.getNoC().setFlitSize(fw);
									project.getNoC().setBufferDepth(bd);
									
									project.getNoC().setAlgorithm(ff.getRoutingAlgorithmSelected(""+algo));
									
									project.setNoCGenerate(true);
									project.setTrafficGenerate(false);
									
									project.getNoC().setCyclesPerFlit(1);
									project.getNoC().setCyclesToRoute(5);
						
									if(noc_type == 0)
									{
										if(buf_type == 0) { project.getNoC().setbufferCoding("Johnson"); } 
										else if(buf_type == 1) project.getNoC().setbufferCoding("Gray");
										ff.getNoc_Ip_Clocks(clock_type,project);
									}
									
									project.setTb("1");
										
									project.write();
									
									if(noc_type == 0)
									{
										String sourceDir = Default.atlashome + File.separator + "HermesG" + File.separator + "Data" + File.separator + "CreditBased" + File.separator;
					
										String scDir = Dir + name + File.separator + "SC_NoC" + File.separator;
										
										
										HermesGCreditBased creditBased = new HermesGCreditBased(project);
										NoCGeneration n = new NoCGeneration(project,sourceDir);
										creditBased.generate();
										
										File sDir=new File(scDir);
										sDir.mkdirs();
										
										ManipulateFile.copy(new File(sourceDir + "SC_InputModule.cpp"), scDir);
										ManipulateFile.copy(new File(sourceDir + "SC_OutputModule.cpp"), scDir);
										
										creditBased.createSimulate();
										n.generatetopNoC();
										n.generateSystemCOutputModule();
										n.generateSystemCIntputModule();
										n.generateAsyncFifo();
									}
									else if (noc_type == 1)
									{
										MainCreditBased creditBased = new MainCreditBased(project);
										creditBased.generate();
									}
									project.write();
									
									System.out.println("\nRede (" + name + ") gerada com sucesso !!");
									
									// ***************************************************************************************************** //
									//                  Bloco de geração de tráfego broadcast para redes HERMES/HERMESG                      //
									// ***************************************************************************************************** //
									
									int sequenceNumberH=0,sequenceNumberL=1;
									int timestamp = 1;
									try
									{
										for(int x1=0;x1 < x;x1++)
										{
											for(int y2=0;y2 < y;y2++)
											{
												int end = y2 * x + x1;
												timestamp = 1;
												FileOutputStream f = new FileOutputStream(Dir + name + File.separator + "in" + end +".txt");
												DataOutputStream data_output =new DataOutputStream(f);	
												for(int x2=0;x2 < x;x2++)
												{
													for(int y3=0;y3 < y;y3++)
													{
														String[] timestampHex;
														
														int tmp3 = y3 * x + x2;
														if(end != tmp3)
														{
															// TS || Target || Tam || Source || TimestampAbs || NumSeq || Payload
															// 1 0001 000A 0000 0000 0000 0000 0001 0000 0001 0008 0009 000A
															
															// TS
															data_output.writeBytes(""+timestamp+" ");
															timestamp = timestamp + 10;
															// Target
															data_output.writeBytes(""+ff.getTarget(x2,y3,x,fw,""+noc_type)+" ");
															
															// Tam
															data_output.writeBytes(""+ff.formatFlit((tam_pckt-6),fw)+" ");
															// Source
															data_output.writeBytes("" + ff.getSource(x1,y2,x,fw,""+noc_type) + " ");
														
															// TimestampAbs
															timestampHex = ff.formatTimestamp(Integer.toString(timestamp), fw);
															data_output.writeBytes(""+timestampHex[3]+" "+timestampHex[2]+" "+timestampHex[1]+" "+timestampHex[0]+" ");
															
															// NumSeq
															
															data_output.writeBytes(""+ff.formatFlit(sequenceNumberH, fw) + " " + ff.formatFlit(sequenceNumberL, fw) + " ");

															//increments the sequence number
															if(sequenceNumberL == (Math.pow(2,fw)-1))
															{
																sequenceNumberH++;
																sequenceNumberL=0;
															}
															else
																sequenceNumberL++;
															
															// Payload
															for(int tmp2=13,cont=0;tmp2 < tam_pckt;tmp2++,cont++)
															{
																data_output.writeBytes(ff.formatFlit(tmp2,fw)+" ");
															}
															data_output.writeBytes("\n");
															
														}
													}
												}
											}
										}
										System.out.println("\nTráfego para rede (" + name + ") gerado com sucesso !!\n");
									}
									catch(Exception e)
									{
										JOptionPane.showMessageDialog(null,e.getMessage(),"Error Message", JOptionPane.ERROR_MESSAGE);
									}
									cont2++;
								}
							}				
						}
					}
				}
				System.out.println("\nForam geradas " + cont2 +" redes!\n");
				ff.genScriptExec(Dir,noc_names,part);
				ff.genScriptVis(Dir,noc_names,part,noc_type);
			}
		}
	}
}
