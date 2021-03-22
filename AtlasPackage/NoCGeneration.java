	package AtlasPackage;

	import javax.swing.*;
	import java.io.*;
	import java.util.*;
	import java.text.DecimalFormat; 
	import java.lang.Math.*;
	import java.text.*;
	
	/**
	 * Generate a NoC.
	 * @author Aline Vieira de Mello
	 * @version
	 */
	public class NoCGeneration
	{

		private String sourceDir, projectDir, nocDir, scDir;
		private String flitWidth, crcType;
		private int dimX, dimY, flitW, nChannels, bufferDepth;
		private boolean isSC, isSR4;
		private ArrayList<String> label = new ArrayList<String>();
		private char array[] = new char[5];
		private ArrayList<String> router_names = new ArrayList<String>();
		private Project p;

		// Variaveis utilizadas para escrita de arquivos
		private StringTokenizer st;
		private String addrX,addrY,addrXHexa,addrYHexa;
		private String line, word, change_parameter;
		private NoCGenerationCB c;

		/**
		 * Generate a NoC with Virtual Channels.
		 * @param project The NoC project.
		 * @param source The path where are the source files. 
		 */
		public NoCGeneration(Project project, String source)
		{
			sourceDir  = source;
			p = project;		
			initialize(project);
		}
		/**
		 * Initialize variables.
		 * @param project The NoC project.
		 */
		private void initialize(Project project){
			NoC noc = project.getNoC();
			dimX = noc.getNumRotX();
			dimY = noc.getNumRotY();
			flitW = noc.getFlitSize();
			flitWidth = ""+flitW;
			nChannels = noc.getVirtualChannel();
			bufferDepth = noc.getBufferDepth();
			isSC = noc.isSCTB();
			isSR4 = noc.isSR4();
			crcType = noc.getCrcType();
			projectDir = project.getPath() + File.separator;
			nocDir     = projectDir + "NOC" + File.separator;
			scDir      = projectDir + "SC_NoC" + File.separator;
		}
		
	/*********************************************************************************
	* DIRECTORIES AND FILES (HERMES_BUFFER AND HERMES_SWITCHCONTROL)
	*********************************************************************************/
		/**
		 * Create the project directory tree.
		 */
		public void makeDiretories()
		{
			//create the project directory
			File diretory = new File(projectDir);
			diretory.mkdirs();

			//create the NoC directory
			File nocDir=new File(diretory +File.separator + "NOC");
			nocDir.mkdirs();

			/* Se o projeto de rede for HERMES-G, a geração do diretório SC_NOC deverá ser feita durante a geração do tráfego */
			// if(p.getNoC().getType().equals("HermesG")) {}
			//else
			//{
				//If the SC test bench option is selected, create the SC_NoC directory
				if(isSC)
				{
					File scDir=new File(diretory +File.separator + "SC_NoC");
					scDir.mkdirs();
				}
			//}
		}
		public void generateAsyncFifo()
		{
			StringTokenizer st;
			String addrX,addrY,addrXHexa,addrYHexa;
			String line, word, change_parameter;

			try
			{
				FileInputStream inFile = new FileInputStream(new File(sourceDir + "HermesG_Fifo_Output.vhd"));
				BufferedReader buff=new BufferedReader(new InputStreamReader(inFile));	    
				DataOutputStream data_output=new DataOutputStream(new FileOutputStream(nocDir + "HermesG_Fifo_Output.vhd"));
				int n_lines=0;
				change_parameter="";
				line=buff.readLine();
				while(line!=null)
				{
					
					st = new StringTokenizer(line, "$");
					int vem = st.countTokens();
					for (int cont=0; cont<vem; cont++)
					{
						word = st.nextToken();
						change_parameter="";
						if(word.equalsIgnoreCase("tam_buffer"))
						{
							if(p.getNoC().getBufferDepth() < 8)
							{
								data_output.writeBytes("-- read_ptr_1flop(TAM_JOHNSON_POINTER-3 downto 0) <= (others=>'0');\n");
								data_output.writeBytes("\t\t\t-- read_ptr_2flop(TAM_JOHNSON_POINTER-3 downto 0) <= (others=>'0');");
							}
							else
							{
								data_output.writeBytes("read_ptr_2flop(TAM_JOHNSON_POINTER-3 downto 0) <= (others=>'0');\n");
								data_output.writeBytes("\t\t\tread_ptr_1flop(TAM_JOHNSON_POINTER-3 downto 0) <= (others=>'0');");
							}
						}
						else if(word.equalsIgnoreCase("idle_pin"))
						{
							// Caso a fila de saída voltar a ser usada, descomentar essa linha
							data_output.writeBytes("\tidle_fifo:   out std_logic;");
						}
						else if(word.equalsIgnoreCase("idle_fifo"))
						{
							// Caso a fila de saída voltar a ser usada, descomentar essa linha
							data_output.writeBytes("\tidle_fifo <= empty;");
						}
						else
						{
							change_parameter = change_parameter.concat(word);
							data_output.writeBytes(change_parameter);
						}
					}	
					data_output.writeBytes("\r\n");
					n_lines++;
					line=buff.readLine();
					
				}
				buff.close();
				data_output.close();
				inFile.close();
			}//end try
			catch(FileNotFoundException f)
			{
				JOptionPane.showMessageDialog(null,"Can't write HermesG_Fifo_Output.vhd"  + " file\n" + f.getMessage(),"Output error", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
			catch(Exception e)
			{
				JOptionPane.showMessageDialog(null,e.getMessage(),"Error message", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
		}
		public void generateBufferG()
		{
			StringTokenizer st;
			String addrX,addrY,addrXHexa,addrYHexa;
			String line, word, change_parameter;

			try
			{
				FileInputStream inFile = new FileInputStream(new File(sourceDir + "HermesG_buffer.vhd"));
				BufferedReader buff=new BufferedReader(new InputStreamReader(inFile));	    
				DataOutputStream data_output=new DataOutputStream(new FileOutputStream(nocDir + "HermesG_buffer.vhd"));
				
				int n_lines=0;
				change_parameter="";
				line=buff.readLine();
				while(line!=null)
				{
					st = new StringTokenizer(line, "$");
					int vem = st.countTokens();
					for (int cont=0; cont<vem; cont++)
					{
						word = st.nextToken();
						change_parameter="";
						if(word.equalsIgnoreCase("tam_buffer"))
						{
							if(p.getNoC().getBufferDepth() < 8)
							{
								data_output.writeBytes("-- read_ptr_1flop(TAM_JOHNSON_POINTER-3 downto 0) <= (others=>'0');\n");
								data_output.writeBytes("\t\t\t-- read_ptr_2flop(TAM_JOHNSON_POINTER-3 downto 0) <= (others=>'0');");
							}
							else
							{
								data_output.writeBytes("read_ptr_2flop(TAM_JOHNSON_POINTER-3 downto 0) <= (others=>'0');\n");
								data_output.writeBytes("\t\t\tread_ptr_1flop(TAM_JOHNSON_POINTER-3 downto 0) <= (others=>'0');");
							}
						}
						else if(word.equalsIgnoreCase("buff_pin"))
						{
							data_output.writeBytes("\tempty_buf:  out std_logic;\n");
						}
						else if(word.equalsIgnoreCase("buff_empty0"))
						{
							data_output.writeBytes("\tempty_buf <= tem_espaco;\n");
						}
						else if(word.equalsIgnoreCase("buff_empty1"))
						{
							data_output.writeBytes("\tempty_buf <= empty;\n");
						}
						else if(word.equalsIgnoreCase("buff_empty2"))
						{
							data_output.writeBytes("\tempty_buf <= empty;\n");
						}
						else
						{
							change_parameter = change_parameter.concat(word);
							data_output.writeBytes(change_parameter);
						}
					}	
					data_output.writeBytes("\r\n");
					n_lines++;
					line=buff.readLine();
				}
				buff.close();
				data_output.close();
				inFile.close();
			}//end try
			catch(FileNotFoundException f)
			{
				JOptionPane.showMessageDialog(null,"Can't write HermesG_buffer.vhd"  + " file\n" + f.getMessage(),"Output error", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
			catch(Exception e)
			{
				JOptionPane.showMessageDialog(null,e.getMessage(),"Error message", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
		}
		public String generateNameRouter(int i)
		{
				int x=0;
				array[0] = ' ';
				array[1] = ' ';
				array[2] = ' ';
				array[3] = ' ';
				array[4] = ' ';
				if(p.getNoC().getClock().get(i).getNumberRouterX().equals("0") && p.getNoC().getClock().get(i).getNumberRouterY().equals("0")) 
				{// Router BL North East Local
					x = Integer.parseInt(p.getNoC().getClock().get(i).getNumberRouterY()+1);
					String north = p.getNoC().getClock().get(i).getNumberRouterX() + x;
					x = Integer.parseInt(p.getNoC().getClock().get(i).getNumberRouterX()+1);
					String east = x + p.getNoC().getClock().get(i).getNumberRouterY();
					// Porta local é sempre o mesmo numero do roteador
					//System.out.println("Sou uma chave BL meu numero de roteador é " + p.getNoC().getClock().get(i).getNumberRouter());
					//System.out.println("North : " + north);
					//System.out.println("South" + south);
					//System.out.println("East" +  east);
					//System.out.println("West" + west);
					//
					// 0 east 1 west 2 north 3 south 4 Local
					//
					// Busca os respectivos roteadores informados pelas Strings e verifica seus respectivos clocks
					for(int j=0;j<p.getNoC().getNumRotX() *  p.getNoC().getNumRotY();j++)
					{
						if(north.equals(p.getNoC().getClock().get(j).getNumberRouter()))
						{
							if(p.getNoC().getClock().get(j).getLabelClockRouter().equals(p.getNoC().getClock().get(i).getLabelClockRouter()))
							{
								array[2] = 'S';			
								// Busca até encontrar $port_type$, quando encontrar gerar uma porta north com buffer sincrono
							}
							else
							{
								array[2] = 'A';			
								// Busca até encontrar $port_type$, quando encontrar gerar uma porta north com buffer assincrono
							}
						}	
						if(east.equals(p.getNoC().getClock().get(j).getNumberRouter()))
						{
							if(p.getNoC().getClock().get(j).getLabelClockRouter().equals(p.getNoC().getClock().get(i).getLabelClockRouter()))
							{
								array[0] = 'S';	
								// Busca até encontrar $port_type$, quando encontrar gerar uma porta east com buffer sincrono
							}
							else
							{
								array[0] = 'A';	
								// Busca até encontrar $port_type$, quando encontrar gerar uma porta east com buffer sincrono
							}
						}
					}
					
					if(p.getNoC().getClock().get(i).getLabelClockRouter().equals(p.getNoC().getClock().get(i).getLabelClockIpInput())) 
					{
						// label e clock do roteador e do ip forem iguais coloca buffer sincrono
						array[4] = 'S';	
					}
					else
					{
						array[4] = 'A';	
						// coloca buffer assincrono
					}
					array[1] = '0';
					array[3] = '0';
				}
				else if(Integer.parseInt(p.getNoC().getClock().get(i).getNumberRouterY()) < p.getNoC().getNumRotY()-1 && p.getNoC().getClock().get(i).getNumberRouterX().equals("0"))
				{// Router CL North East South Local
					x = Integer.parseInt(p.getNoC().getClock().get(i).getNumberRouterY())+1;
					String north = p.getNoC().getClock().get(i).getNumberRouterX() + x;

					x = Integer.parseInt(p.getNoC().getClock().get(i).getNumberRouterX())+1;
					String east =  x + p.getNoC().getClock().get(i).getNumberRouterY();

					x = Integer.parseInt(p.getNoC().getClock().get(i).getNumberRouterY())-1;
					String south = p.getNoC().getClock().get(i).getNumberRouterX() + x;
					// Porta local é sempre o mesmo numero do roteador
					//System.out.println("Sou uma chave CL meu numero de roteador é " + p.getNoC().getClock().get(i).getNumberRouter());
					//System.out.println("North : " + north);
					//System.out.println("South" + south);
					//System.out.println("East" +  east);
					//System.out.println("West" + west);
					// Busca os respectivos roteadores informados pelas Strings e verifica seus respectivos clocks
					for(int j=0;j<p.getNoC().getNumRotX() *  p.getNoC().getNumRotY();j++)
					{
						if(north.equals(p.getNoC().getClock().get(j).getNumberRouter()))
						{
							if(p.getNoC().getClock().get(j).getLabelClockRouter().equals(p.getNoC().getClock().get(i).getLabelClockRouter()))
							{
								array[2] = 'S';
								// Busca até encontrar $port_type$, quando encontrar gerar uma porta north com buffer sincrono
							}
							else
							{
								array[2] = 'A';
								// Busca até encontrar $port_type$, quando encontrar gerar uma porta north com buffer assincrono
							}
						}
						if(east.equals(p.getNoC().getClock().get(j).getNumberRouter()))
						{
							if(p.getNoC().getClock().get(j).getLabelClockRouter().equals(p.getNoC().getClock().get(i).getLabelClockRouter()))
							{
								array[0] = 'S';
								// Busca até encontrar $port_type$, quando encontrar gerar uma porta east com buffer sincrono
							}
							else
							{
								array[0] = 'A';
								// Busca até encontrar $port_type$, quando encontrar gerar uma porta east com buffer sincrono
							}
						}
						if(south.equals(p.getNoC().getClock().get(j).getNumberRouter()))
						{
							if(p.getNoC().getClock().get(j).getLabelClockRouter().equals(p.getNoC().getClock().get(i).getLabelClockRouter()))
							{
								array[3] = 'S';
								// Busca até encontrar $port_type$, quando encontrar gerar uma porta east com buffer sincrono
							}
							else
							{
								array[3] = 'A';
								// Busca até encontrar $port_type$, quando encontrar gerar uma porta east com buffer sincrono
							}
						}
					}
					if(p.getNoC().getClock().get(i).getLabelClockRouter().equals(p.getNoC().getClock().get(i).getLabelClockIpInput())) 
					{
						// label e clock do roteador e do ip forem iguais coloca buffer sincrono
						array[4] = 'S';
					}
					else
					{
						array[4] = 'A';
						// coloca buffer assincrono
					}
					array[1] = '0';
				}
				else if(Integer.parseInt(p.getNoC().getClock().get(i).getNumberRouterY()) == p.getNoC().getNumRotY()-1 && p.getNoC().getClock().get(i).getNumberRouterX().equals("0"))
				{// Router TL East South Local
					x = Integer.parseInt(p.getNoC().getClock().get(i).getNumberRouterX())+1;
					String east =  x + p.getNoC().getClock().get(i).getNumberRouterY();
					x = Integer.parseInt(p.getNoC().getClock().get(i).getNumberRouterY())-1;
					String south = p.getNoC().getClock().get(i).getNumberRouterX() + x;
					// Porta local é sempre o mesmo numero do roteador
					//System.out.println("Sou uma chave TL meu numero de roteador é " + p.getNoC().getClock().get(i).getNumberRouter());
					//System.out.println("North : " + north);
					//System.out.println("South" + south);
					//System.out.println("East" +  east);
					//System.out.println("West" + west);
					for(int j=0;j<p.getNoC().getNumRotX() *  p.getNoC().getNumRotY();j++)
					{
						// Busca os respectivos roteadores informados pelas Strings e verifica seus respectivos clocks
						if(east.equals(p.getNoC().getClock().get(j).getNumberRouter()))
						{
							if(p.getNoC().getClock().get(j).getLabelClockRouter().equals(p.getNoC().getClock().get(i).getLabelClockRouter()))
								{
									array[0] = 'S';
									// Busca até encontrar $port_type$, quando encontrar gerar uma porta east com buffer sincrono
								}
								else
								{
									array[0] = 'A';
									// Busca até encontrar $port_type$, quando encontrar gerar uma porta east com buffer sincrono
								}
							}
							if(south.equals(p.getNoC().getClock().get(j).getNumberRouter()))
							{
								if(p.getNoC().getClock().get(j).getLabelClockRouter().equals(p.getNoC().getClock().get(i).getLabelClockRouter()))
								{
									array[3] = 'S';
									// Busca até encontrar $port_type$, quando encontrar gerar uma porta east com buffer sincrono
								}
								else
								{
									array[3] = 'A';
									// Busca até encontrar $port_type$, quando encontrar gerar uma porta east com buffer sincrono
								}
							}
						}
						if(p.getNoC().getClock().get(i).getLabelClockRouter().equals(p.getNoC().getClock().get(i).getLabelClockIpInput())) 
						{
							// label e clock do roteador e do ip forem iguais coloca buffer sincrono
							array[4] = 'S';
						}
						else
						{
							array[4] = 'A';
							// coloca buffer assincrono
						}
						array[1] = '0';
						array[2] = '0';
					}
					else if(Integer.parseInt(p.getNoC().getClock().get(i).getNumberRouterX()) > 0 && Integer.parseInt(p.getNoC().getClock().get(i).getNumberRouterY()) == 0 && Integer.parseInt(p.getNoC().getClock().get(i).getNumberRouterX()) < p.getNoC().getNumRotX()-1)
					{ // Router BC West East North Local
						x = Integer.parseInt(p.getNoC().getClock().get(i).getNumberRouterX())-1;
						String west = x + p.getNoC().getClock().get(i).getNumberRouterY();	
						x = Integer.parseInt(p.getNoC().getClock().get(i).getNumberRouterX())+1;
						String east =  x + p.getNoC().getClock().get(i).getNumberRouterY();
						x = Integer.parseInt(p.getNoC().getClock().get(i).getNumberRouterY())+1;
						String north = p.getNoC().getClock().get(i).getNumberRouterX() + x;
						// Porta local é sempre o mesmo numero do roteador
						//System.out.println("North : " + north);
						//System.out.println("South" + south);
						//System.out.println("East" +  east);
						//System.out.println("West" + west);
						//System.out.println("Sou uma chave BC meu numero de roteador é " + p.getNoC().getClock().get(i).getNumberRouter());
						for(int j=0;j<p.getNoC().getNumRotX() *  p.getNoC().getNumRotY();j++)
						{
							if(north.equals(p.getNoC().getClock().get(j).getNumberRouter()))
							{
								if(p.getNoC().getClock().get(j).getLabelClockRouter().equals(p.getNoC().getClock().get(i).getLabelClockRouter()))
								{
									array[2] = 'S';
									// Busca até encontrar $port_type$, quando encontrar gerar uma porta north com buffer sincrono
								}
								else
								{
									array[2] = 'A';
									// Busca até encontrar $port_type$, quando encontrar gerar uma porta north com buffer assincrono
								}
							}
							if(east.equals(p.getNoC().getClock().get(j).getNumberRouter()))
							{
								if(p.getNoC().getClock().get(j).getLabelClockRouter().equals(p.getNoC().getClock().get(i).getLabelClockRouter()))
								{
									array[0] = 'S';
									// Busca até encontrar $port_type$, quando encontrar gerar uma porta east com buffer sincrono
								}
								else
								{
									array[0] = 'A';
									// Busca até encontrar $port_type$, quando encontrar gerar uma porta east com buffer sincrono
								}
							}
							if(west.equals(p.getNoC().getClock().get(j).getNumberRouter()))
							{
								if(p.getNoC().getClock().get(j).getLabelClockRouter().equals(p.getNoC().getClock().get(i).getLabelClockRouter()))
								{
									array[1] = 'S';
									// Busca até encontrar $port_type$, quando encontrar gerar uma porta east com buffer sincrono
								}
								else
								{
									array[1] = 'A';
									// Busca até encontrar $port_type$, quando encontrar gerar uma porta east com buffer sincrono
								}
							}
						}
						if(p.getNoC().getClock().get(i).getLabelClockRouter().equals(p.getNoC().getClock().get(i).getLabelClockIpInput())) 
						{
							// label e clock do roteador e do ip forem iguais coloca buffer sincrono
							array[4] = 'S';
						}
						else
						{
							array[4] = 'A';
							// coloca buffer assincrono
						}
						array[3] = '0';
					
					}
					else if(Integer.parseInt(p.getNoC().getClock().get(i).getNumberRouterX()) > 0 && Integer.parseInt(p.getNoC().getClock().get(i).getNumberRouterY()) > 0 && Integer.parseInt(p.getNoC().getClock().get(i).getNumberRouterX()) < p.getNoC().getNumRotX()-1 && Integer.parseInt(p.getNoC().getClock().get(i).getNumberRouterY()) < p.getNoC().getNumRotY()-1)
					{ // Router CC North South East West Local
						x = Integer.parseInt(p.getNoC().getClock().get(i).getNumberRouterY())-1;
						String south = p.getNoC().getClock().get(i).getNumberRouterX() + x;
						x = Integer.parseInt(p.getNoC().getClock().get(i).getNumberRouterX())-1;
						String west = x + p.getNoC().getClock().get(i).getNumberRouterY();
						x = Integer.parseInt(p.getNoC().getClock().get(i).getNumberRouterX())+1;
						String east = x + p.getNoC().getClock().get(i).getNumberRouterY();
						x = Integer.parseInt(p.getNoC().getClock().get(i).getNumberRouterY())+1;
						String north = p.getNoC().getClock().get(i).getNumberRouterX() + x;
						// Porta local é sempre o mesmo número do roteador
						//System.out.println("North : " + north);
						//System.out.println("South" + south);
						//System.out.println("East" +  east);
						//System.out.println("West" + west);
						//System.out.println("Sou uma chave CC meu numero de roteador é " + p.getNoC().getClock().get(i).getNumberRouter());
						for(int j=0;j<p.getNoC().getNumRotX() *  p.getNoC().getNumRotY();j++)
						{
							if(north.equals(p.getNoC().getClock().get(j).getNumberRouter()))
							{
								if(p.getNoC().getClock().get(j).getLabelClockRouter().equals(p.getNoC().getClock().get(i).getLabelClockRouter()))
								{
									array[2] = 'S';
									// Busca até encontrar $port_type$, quando encontrar gerar uma porta north com buffer sincrono
								}
								else
								{
									array[2] = 'A';
									// Busca até encontrar $port_type$, quando encontrar gerar uma porta north com buffer assincrono
								}
							}
							if(east.equals(p.getNoC().getClock().get(j).getNumberRouter()))
							{
								if(p.getNoC().getClock().get(j).getLabelClockRouter().equals(p.getNoC().getClock().get(i).getLabelClockRouter()))
								{
									array[0] = 'S';
									// Busca até encontrar $port_type$, quando encontrar gerar uma porta east com buffer sincrono
								}
								else
								{
									array[0] = 'A';
									// Busca até encontrar $port_type$, quando encontrar gerar uma porta east com buffer sincrono
								}
							}
							if(west.equals(p.getNoC().getClock().get(j).getNumberRouter()))
							{
								if(p.getNoC().getClock().get(j).getLabelClockRouter().equals(p.getNoC().getClock().get(i).getLabelClockRouter()))
								{
									array[1] = 'S';
									// Busca até encontrar $port_type$, quando encontrar gerar uma porta east com buffer sincrono
								}
								else
								{
									array[1] = 'A';
									// Busca até encontrar $port_type$, quando encontrar gerar uma porta east com buffer sincrono
								}
							}				
							if(south.equals(p.getNoC().getClock().get(j).getNumberRouter()))
							{
								if(p.getNoC().getClock().get(j).getLabelClockRouter().equals(p.getNoC().getClock().get(i).getLabelClockRouter()))
								{
									array[3] = 'S';
									// Busca até encontrar $port_type$, quando encontrar gerar uma porta east com buffer sincrono
								}
								else
								{
									array[3] = 'A';
									// Busca até encontrar $port_type$, quando encontrar gerar uma porta east com buffer sincrono
								}
							}
						}
						if(p.getNoC().getClock().get(i).getLabelClockRouter().equals(p.getNoC().getClock().get(i).getLabelClockIpInput())) 
						{
							// label e clock do roteador e do ip forem iguais coloca buffer sincrono
							array[4] = 'S';
						}
						else
						{
							array[4] = 'A';
							// coloca buffer assincrono
						}
					}
					else if(Integer.parseInt(p.getNoC().getClock().get(i).getNumberRouterY()) == p.getNoC().getNumRotY()-1 && Integer.parseInt(p.getNoC().getClock().get(i).getNumberRouterX()) > 0 && Integer.parseInt(p.getNoC().getClock().get(i).getNumberRouterX()) < p.getNoC().getNumRotX()-1)
					{ // Router TC South East West Local
						x = Integer.parseInt(p.getNoC().getClock().get(i).getNumberRouterY())-1;
						String south = p.getNoC().getClock().get(i).getNumberRouterX() + x;
						x = Integer.parseInt(p.getNoC().getClock().get(i).getNumberRouterX())-1;
						String west = x + p.getNoC().getClock().get(i).getNumberRouterY();
						x = Integer.parseInt(p.getNoC().getClock().get(i).getNumberRouterX())+1;
						String east = x + p.getNoC().getClock().get(i).getNumberRouterY();
						//System.out.println("North : " + north);
						//System.out.println("South" + south);
						//System.out.println("East" +  east);
						//System.out.println("West" + west);
						// Porta local é sempre o mesmo número do roteador
						//System.out.println("Sou uma chave TC meu numero de roteador é " + p.getNoC().getClock().get(i).getNumberRouter());
						for(int j=0;j<p.getNoC().getNumRotX() *  p.getNoC().getNumRotY();j++)
						{
							if(east.equals(p.getNoC().getClock().get(j).getNumberRouter()))
							{
								if(p.getNoC().getClock().get(j).getLabelClockRouter().equals(p.getNoC().getClock().get(i).getLabelClockRouter()))
								{
									array[0] = 'S';
									// Busca até encontrar $port_type$, quando encontrar gerar uma porta east com buffer sincrono
								}
								else
								{
									array[0] = 'A';
									// Busca até encontrar $port_type$, quando encontrar gerar uma porta east com buffer sincrono
								}
							}
							if(west.equals(p.getNoC().getClock().get(j).getNumberRouter()))
							{
								if(p.getNoC().getClock().get(j).getLabelClockRouter().equals(p.getNoC().getClock().get(i).getLabelClockRouter()))
								{
									array[1] = 'S';
									// Busca até encontrar $port_type$, quando encontrar gerar uma porta east com buffer sincrono
								}
								else
								{
									array[1] = 'A';
									// Busca até encontrar $port_type$, quando encontrar gerar uma porta east com buffer sincrono
								}
							}				
							if(south.equals(p.getNoC().getClock().get(j).getNumberRouter()))
							{
								if(p.getNoC().getClock().get(j).getLabelClockRouter().equals(p.getNoC().getClock().get(i).getLabelClockRouter()))
								{
									array[3] = 'S';
									// Busca até encontrar $port_type$, quando encontrar gerar uma porta east com buffer sincrono
								}
								else
								{
									array[3] = 'A';
									// Busca até encontrar $port_type$, quando encontrar gerar uma porta east com buffer sincrono
								}
							}
						}
						if(p.getNoC().getClock().get(i).getLabelClockRouter().equals(p.getNoC().getClock().get(i).getLabelClockIpInput())) 
						{
							// label e clock do roteador e do ip forem iguais coloca buffer sincrono
							array[4] = 'S';
						}
						else
						{
							array[4] = 'A';
							// coloca buffer assincrono
						}
						array[2] = '0';
					}
					else if(Integer.parseInt(p.getNoC().getClock().get(i).getNumberRouterX()) == p.getNoC().getNumRotX()-1 && Integer.parseInt(p.getNoC().getClock().get(i).getNumberRouterY()) == 0)
					{ // Router BR North West Local
						x = Integer.parseInt(p.getNoC().getClock().get(i).getNumberRouterX())-1;
						String west = x + p.getNoC().getClock().get(i).getNumberRouterY();
						x = Integer.parseInt(p.getNoC().getClock().get(i).getNumberRouterY())+1;
						String north = p.getNoC().getClock().get(i).getNumberRouterX() + x;
						//System.out.println("North : " + north);
						//System.out.println("South" + south);
						//System.out.println("East" +  east);
						//System.out.println("West" + west);
						// Porta local é sempre o mesmo número do roteador
						//System.out.println("Sou uma chave BR meu numero de roteador é " + p.getNoC().getClock().get(i).getNumberRouter());
						for(int j=0;j<p.getNoC().getNumRotX() *  p.getNoC().getNumRotY();j++)
						{
							if(north.equals(p.getNoC().getClock().get(j).getNumberRouter()))
							{
								if(p.getNoC().getClock().get(j).getLabelClockRouter().equals(p.getNoC().getClock().get(i).getLabelClockRouter()))
								{
									array[2] = 'S';
									// Busca até encontrar $port_type$, quando encontrar gerar uma porta north com buffer sincrono
								}
								else
								{
									array[2] = 'A';
									// Busca até encontrar $port_type$, quando encontrar gerar uma porta north com buffer assincrono
								}
							}
							if(west.equals(p.getNoC().getClock().get(j).getNumberRouter()))
							{
								if(p.getNoC().getClock().get(j).getLabelClockRouter().equals(p.getNoC().getClock().get(i).getLabelClockRouter()))
								{
									array[1] = 'S';
									// Busca até encontrar $port_type$, quando encontrar gerar uma porta east com buffer sincrono
								}
								else
								{
									array[1] = 'A';
									// Busca até encontrar $port_type$, quando encontrar gerar uma porta east com buffer sincrono
								}
							}
						}
						if(p.getNoC().getClock().get(i).getLabelClockRouter().equals(p.getNoC().getClock().get(i).getLabelClockIpInput())) 
						{
							// label e clock do roteador e do ip forem iguais coloca buffer sincrono
							array[4] = 'S';
						}
						else
						{
							array[4] = 'A';
							// coloca buffer assincrono
						}
						array[0] = '0';
						array[3] = '0';
					}
					else if(Integer.parseInt(p.getNoC().getClock().get(i).getNumberRouterX()) == p.getNoC().getNumRotX()-1 && Integer.parseInt(p.getNoC().getClock().get(i).getNumberRouterY()) < p.getNoC().getNumRotY()-1 && Integer.parseInt(p.getNoC().getClock().get(i).getNumberRouterY()) > 0)
					{ // Router CR North South West Local
						x = Integer.parseInt(p.getNoC().getClock().get(i).getNumberRouterY())-1;
						String south = p.getNoC().getClock().get(i).getNumberRouterX() + x;
						x = Integer.parseInt(p.getNoC().getClock().get(i).getNumberRouterX())-1;
						String west = x + p.getNoC().getClock().get(i).getNumberRouterY();
						x = Integer.parseInt(p.getNoC().getClock().get(i).getNumberRouterY())+1;
						String north = p.getNoC().getClock().get(i).getNumberRouterX() + x;
						// Porta local é sempre o mesmo número do roteador
						//System.out.println("North : " + north);
						//System.out.println("South" + south);
						//System.out.println("East" +  east);
						//System.out.println("West" + west);
						//System.out.println("Sou uma chave CR meu numero de roteador é " + p.getNoC().getClock().get(i).getNumberRouter());
						for(int j=0;j<p.getNoC().getNumRotX() *  p.getNoC().getNumRotY();j++)
						{
							if(north.equals(p.getNoC().getClock().get(j).getNumberRouter()))
							{
								if(p.getNoC().getClock().get(j).getLabelClockRouter().equals(p.getNoC().getClock().get(i).getLabelClockRouter()))
								{
									array[2] = 'S';
									// Busca até encontrar $port_type$, quando encontrar gerar uma porta north com buffer sincrono
								}
								else
								{
									array[2] = 'A';
									// Busca até encontrar $port_type$, quando encontrar gerar uma porta north com buffer assincrono
								}
							}
							if(south.equals(p.getNoC().getClock().get(j).getNumberRouter()))
							{
								if(p.getNoC().getClock().get(j).getLabelClockRouter().equals(p.getNoC().getClock().get(i).getLabelClockRouter()))
								{
									array[3] = 'S';
									// Busca até encontrar $port_type$, quando encontrar gerar uma porta east com buffer sincrono
								}
								else
								{
									array[3] = 'A';
									// Busca até encontrar $port_type$, quando encontrar gerar uma porta east com buffer sincrono
								}
							}
							if(west.equals(p.getNoC().getClock().get(j).getNumberRouter()))
							{
								if(p.getNoC().getClock().get(j).getLabelClockRouter().equals(p.getNoC().getClock().get(i).getLabelClockRouter()))
								{
									array[1] = 'S';
									// Busca até encontrar $port_type$, quando encontrar gerar uma porta east com buffer sincrono
								}
								else
								{
									array[1] = 'A';
									// Busca até encontrar $port_type$, quando encontrar gerar uma porta east com buffer sincrono
								}
							}
						}
						if(p.getNoC().getClock().get(i).getLabelClockRouter().equals(p.getNoC().getClock().get(i).getLabelClockIpInput())) 
						{
							// label e clock do roteador e do ip forem iguais coloca buffer sincrono
							array[4] = 'S';
						}
						else
						{
							array[4] = 'A';
							// coloca buffer assincrono
						}
						array[0] = '0';
					
					}
					else if(Integer.parseInt(p.getNoC().getClock().get(i).getNumberRouterX()) == p.getNoC().getNumRotX()-1 && Integer.parseInt(p.getNoC().getClock().get(i).getNumberRouterY()) == p.getNoC().getNumRotY()-1)
					{ // Router TR West South Local
						x = Integer.parseInt(p.getNoC().getClock().get(i).getNumberRouterY())-1;
						String south = p.getNoC().getClock().get(i).getNumberRouterX() + x;
						x = Integer.parseInt(p.getNoC().getClock().get(i).getNumberRouterX())-1;
						String west = x + p.getNoC().getClock().get(i).getNumberRouterY();
						// Porta local é sempre o mesmo número do roteador
						//System.out.println("North : " + north);
						//System.out.println("South" + south);
						//System.out.println("East" +  east);
						//System.out.println("West" + west);
						//System.out.println("Sou uma chave TR meu numero de roteador é " + p.getNoC().getClock().get(i).getNumberRouter());
						for(int j=0;j<p.getNoC().getNumRotX() *  p.getNoC().getNumRotY();j++)
						{
							if(south.equals(p.getNoC().getClock().get(j).getNumberRouter()))
							{
								if(p.getNoC().getClock().get(j).getLabelClockRouter().equals(p.getNoC().getClock().get(i).getLabelClockRouter()))
								{
									array[3] = 'S';
									// Busca até encontrar $port_type$, quando encontrar gerar uma porta east com buffer sincrono
								}
								else
								{
									array[3] = 'A';
									// Busca até encontrar $port_type$, quando encontrar gerar uma porta east com buffer sincrono
								}
							}
							if(west.equals(p.getNoC().getClock().get(j).getNumberRouter()))
							{
								if(p.getNoC().getClock().get(j).getLabelClockRouter().equals(p.getNoC().getClock().get(i).getLabelClockRouter()))
								{
									array[1] = 'S';
									// Busca até encontrar $port_type$, quando encontrar gerar uma porta east com buffer sincrono
								}
								else
								{
									array[1] = 'A';
									// Busca até encontrar $port_type$, quando encontrar gerar uma porta east com buffer sincrono
								}
							}
						}
					if(p.getNoC().getClock().get(i).getLabelClockRouter().equals(p.getNoC().getClock().get(i).getLabelClockIpInput())) 
					{
						// label e clock do roteador e do ip forem iguais coloca buffer sincrono
						array[4] = 'S';
					}
					else
					{
						array[4] = 'A';
						// coloca buffer assincrono
					}
					array[0] = '0';
					array[2] = '0';
				}
				String saida = new String(array);
				return(saida);
		}
		public void generateRouter()
		{
			int con=0;
			String xHexa,yHexa;
			for(int x=0;x<p.getNoC().getNumRotX();x++)
			{
				xHexa = Convert.decToHex(x,(p.getNoC().getFlitSize()/8));
				for(int y=0;y<p.getNoC().getNumRotY();y++,con++)
				{
				      yHexa = Convert.decToHex(y,(p.getNoC().getFlitSize()/8));
				      try
				      {
					      String saida = generateNameRouter(con);
					      
					      array[0] = saida.charAt(0);
						  array[1] = saida.charAt(1);
						  array[2] = saida.charAt(2);
						  array[3] = saida.charAt(3);
						  array[4] = saida.charAt(4);
						  
					      p.getNoC().getClock().get(con).setNameRouter("router" + xHexa + yHexa + saida);
					      p.getNoC().getClock().get(con).setRouterInterfaces(saida);
					      FileInputStream inFile = new FileInputStream(new File(sourceDir + "router" + ".vhd"));
					      BufferedReader buff=new BufferedReader(new InputStreamReader(inFile));
					      DataOutputStream data_output=new DataOutputStream(new FileOutputStream(nocDir + "router" + xHexa + yHexa + saida + ".vhd"));
					      
					      int n_lines=0;
					      change_parameter="";
					      line=buff.readLine();
					      while(line!=null)
					      {
						      st = new StringTokenizer(line, "$");
						      int vem = st.countTokens();
						      for (int cont=0; cont<vem; cont++)
						      {
							      word = st.nextToken();
							      change_parameter="";
							      if(word.equalsIgnoreCase("router_name"))
							      {
								      word = "" + "router" + xHexa + yHexa + saida;
							      }
							      else if(word.equalsIgnoreCase("port_type"))
							      {
								      word="";
								      int con2=0;
								      for(int j=0;j<5;j++)
								      {
									      if(array[j] != '0' && p.getNoC().getbufferCoding().equals("Gray"))
									      {
											  String clock="";
											  int flag=0;
											  for(int tmp=0;tmp < p.getNoC().getClock().size();tmp++)
											  {
													if(tmp == 0) clock = p.getNoC().getClock().get(0).getRouter();
													
													// Verifica o clock do primeiro roteador com todos os roteadores
													
													if(p.getNoC().getClock().get(tmp).getRouter().equals(clock) && p.getNoC().getClock().get(tmp).getIpInput().equals(clock) && p.getNoC().getClock().get(tmp).getIpOutput().equals(clock)) { }
													else { flag = 1; break; } 
																										
											  }
											  if(flag == 0)
											  {
												if(j == 0 && (array[j] == 'S' || array[j] == 'A'))
													  data_output.writeBytes("\tFEast : Entity work.HermesG_buffer(Hermes_buffer) \n");
												  else if(j == 1 && (array[j] == 'S'|| array[j] == 'A'))
													  data_output.writeBytes("\tFWest : Entity work.HermesG_buffer(Hermes_buffer) \n");
												  else if(j == 2 && (array[j] == 'S'|| array[j] == 'A'))
													  data_output.writeBytes("\tFNorth : Entity work.HermesG_buffer(Hermes_buffer) \n");
												  else if(j == 3 && (array[j] == 'S'|| array[j] == 'A'))
													  data_output.writeBytes("\tFSouth : Entity work.HermesG_buffer(Hermes_buffer) \n");
												  else if(j == 4 && (array[j] == 'S'|| array[j] == 'A'))
													  data_output.writeBytes("\tFLocal : Entity work.HermesG_buffer(Hermes_buffer) \n");
											  }
											  else if(flag == 1)
											  {
												  // TAG - SYNC/ASYNCG
												  // Quando a arquitetura Async_Hermes_Buffer estiver 100% operacional, comentar as linhas descomentadas a seguir e descomentar as linhas comentadas
												  // Assim, o gerador terá capacidade de gerar redes com filas síncronas e filas bi síncronas com codificação Gray
												  /*
												  if(j == 0 && (array[j] == 'S' || array[j] == 'A'))
													  data_output.writeBytes("\tFEast : Entity work.HermesG_buffer(Async_Gray_Hermes_buffer) \n");
												  else if(j == 1 && (array[j] == 'S'|| array[j] == 'A'))
													  data_output.writeBytes("\tFWest : Entity work.HermesG_buffer(Async_Gray_Hermes_buffer) \n");
												  else if(j == 2 && (array[j] == 'S'|| array[j] == 'A'))
													  data_output.writeBytes("\tFNorth : Entity work.HermesG_buffer(Async_Gray_Hermes_buffer) \n");
												  else if(j == 3 && (array[j] == 'S'|| array[j] == 'A'))
													  data_output.writeBytes("\tFSouth : Entity work.HermesG_buffer(Async_Gray_Hermes_buffer) \n");
												  else if(j == 4 && (array[j] == 'S'|| array[j] == 'A'))
													  data_output.writeBytes("\tFLocal : Entity work.HermesG_buffer(Async_Gray_Hermes_buffer) \n");
												  */	  
												  // /* 
												  if(j == 0 && array[j] == 'S')
													  data_output.writeBytes("\tFEast : Entity work.HermesG_buffer(Sync_Hermes_buffer) \n");
												  else if(j == 0 && array[j] == 'A')
													  data_output.writeBytes("\tFEast : Entity work.HermesG_buffer(Async_Gray_Hermes_buffer) \n");
												  else if(j == 1 && array[j] == 'S')
													  data_output.writeBytes("\tFWest : Entity work.HermesG_buffer(Sync_Hermes_buffer) \n");
												  else if(j == 1 && array[j] == 'A')
													  data_output.writeBytes("\tFWest : Entity work.HermesG_buffer(Async_Gray_Hermes_buffer) \n");
												  else if(j == 2 && array[j] == 'S')
													  data_output.writeBytes("\tFNorth : Entity work.HermesG_buffer(Sync_Hermes_buffer) \n");
												  else if(j == 2 && array[j] == 'A')
													  data_output.writeBytes("\tFNorth : Entity work.HermesG_buffer(Async_Gray_Hermes_buffer) \n");
												  else if(j == 3 && array[j] == 'S')
													  data_output.writeBytes("\tFSouth : Entity work.HermesG_buffer(Sync_Hermes_buffer) \n");
												  else if(j == 3 && array[j] == 'A')
													  data_output.writeBytes("\tFSouth : Entity work.HermesG_buffer(Async_Gray_Hermes_buffer) \n");
												  else if(j == 4 && array[j] == 'S')
													  data_output.writeBytes("\tFLocal : Entity work.HermesG_buffer(Sync_Hermes_buffer) \n");
												  else if(j == 4 && array[j] == 'A')
													  data_output.writeBytes("\tFLocal : Entity work.HermesG_buffer(Async_Gray_Hermes_buffer) \n");
												  // */
											  }	  
										      
										  }
									      else if(array[j] != '0' && p.getNoC().getbufferCoding().equals("Johnson"))
									      {
											  String clock="";
											  int flag=0;
											  for(int tmp=0;tmp < p.getNoC().getClock().size();tmp++)
											  {
													if(tmp == 0) clock = p.getNoC().getClock().get(0).getRouter();
													
													// Verifica o clock do primeiro roteador com todos os roteadores
													
													if(p.getNoC().getClock().get(tmp).getRouter().equals(clock) && p.getNoC().getClock().get(tmp).getIpInput().equals(clock) && p.getNoC().getClock().get(tmp).getIpOutput().equals(clock)) { }
													else { flag = 1; break; } 
																										
											  }
											  if(flag == 0)
											  {
												if(j == 0 && (array[j] == 'S' || array[j] == 'A'))
													  data_output.writeBytes("\tFEast : Entity work.HermesG_buffer(Hermes_buffer) \n");
												  else if(j == 1 && (array[j] == 'S'|| array[j] == 'A'))
													  data_output.writeBytes("\tFWest : Entity work.HermesG_buffer(Hermes_buffer) \n");
												  else if(j == 2 && (array[j] == 'S'|| array[j] == 'A'))
													  data_output.writeBytes("\tFNorth : Entity work.HermesG_buffer(Hermes_buffer) \n");
												  else if(j == 3 && (array[j] == 'S'|| array[j] == 'A'))
													  data_output.writeBytes("\tFSouth : Entity work.HermesG_buffer(Hermes_buffer) \n");
												  else if(j == 4 && (array[j] == 'S'|| array[j] == 'A'))
													  data_output.writeBytes("\tFLocal : Entity work.HermesG_buffer(Hermes_buffer) \n");
											  }
											  else if(flag == 1)
											  {
												  // TAG - SYNC/ASYNCJ
												  // Quando a arquitetura Async_Hermes_Buffer estiver 100% operacional, comentar as linhas descomentadas a seguir e descomentar as linhas comentadas
												  // Assim, o gerador terá capacidade de gerar redes com filas síncronas e filas bi síncronas com codificação Johnson
												  /*
												  if(j == 0 && (array[j] == 'S' || array[j] == 'A'))
													  data_output.writeBytes("\tFEast : Entity work.HermesG_buffer(Async_Johnson_Hermes_buffer) \n");
												  else if(j == 1 && (array[j] == 'S'|| array[j] == 'A'))
													  data_output.writeBytes("\tFWest : Entity work.HermesG_buffer(Async_Johnson_Hermes_buffer) \n");
												  else if(j == 2 && (array[j] == 'S'|| array[j] == 'A'))
													  data_output.writeBytes("\tFNorth : Entity work.HermesG_buffer(Async_Johnson_Hermes_buffer) \n");
												  else if(j == 3 && (array[j] == 'S'|| array[j] == 'A'))
													  data_output.writeBytes("\tFSouth : Entity work.HermesG_buffer(Async_Johnson_Hermes_buffer) \n");
												  else if(j == 4 && (array[j] == 'S'|| array[j] == 'A'))
													  data_output.writeBytes("\tFLocal : Entity work.HermesG_buffer(Async_Johnson_Hermes_buffer) \n");
												  */	  
												  // /*
												  if(j == 0 && array[j] == 'S')
													  data_output.writeBytes("\tFEast : Entity work.HermesG_buffer(Sync_Hermes_buffer) \n");
												  else if(j == 0 && array[j] == 'A')
													  data_output.writeBytes("\tFEast : Entity work.HermesG_buffer(Async_Johnson_Hermes_buffer) \n");
												  else if(j == 1 && array[j] == 'S')
													  data_output.writeBytes("\tFWest : Entity work.HermesG_buffer(Sync_Hermes_buffer) \n");
												  else if(j == 1 && array[j] == 'A')
													  data_output.writeBytes("\tFWest : Entity work.HermesG_buffer(Async_Johnson_Hermes_buffer) \n");
												  else if(j == 2 && array[j] == 'S')
													  data_output.writeBytes("\tFNorth : Entity work.HermesG_buffer(Sync_Hermes_buffer) \n");
												  else if(j == 2 && array[j] == 'A')
													  data_output.writeBytes("\tFNorth : Entity work.HermesG_buffer(Async_Johnson_Hermes_buffer) \n");
												  else if(j == 3 && array[j] == 'S')
													  data_output.writeBytes("\tFSouth : Entity work.HermesG_buffer(Sync_Hermes_buffer) \n");
												  else if(j == 3 && array[j] == 'A')
													  data_output.writeBytes("\tFSouth : Entity work.HermesG_buffer(Async_Johnson_Hermes_buffer) \n");
												  else if(j == 4 && array[j] == 'S')
													  data_output.writeBytes("\tFLocal : Entity work.HermesG_buffer(Sync_Hermes_buffer) \n");
												  else if(j == 4 && array[j] == 'A')
													  data_output.writeBytes("\tFLocal : Entity work.HermesG_buffer(Async_Johnson_Hermes_buffer) \n");
												  // */ 	  
											  }	
									      }
									      if(array[j] == 'A' || array[j] == 'S')
									      {
										      data_output.writeBytes("\tport map(\n\t\tclock => clock,\n\t\treset => reset,\n");
										      data_output.writeBytes("\t\tdata_in => data_in(" + j + "),\n");
										      data_output.writeBytes("\t\trx => rx(" + j + "),\n");
										      data_output.writeBytes("\t\th => h(" + j + "),\n");
										      data_output.writeBytes("\t\tack_h => ack_h(" + j + "),\n");
										      data_output.writeBytes("\t\tdata_av => data_av(" + j + "),\n");
										      data_output.writeBytes("\t\tdata => data(" + j + "),\n");
										      data_output.writeBytes("\t\tsender => sender(" + j + "),\n");
										      data_output.writeBytes("\t\tclock_rx => clock_rx(" + j + "),\n");
										      data_output.writeBytes("\t\tdata_ack => data_ack(" + j + "),\n");
										      data_output.writeBytes("\t\tcredit_o => credit_o(" + j + "),\n");
										      data_output.writeBytes("\t\tempty_buf => rot_empty_buf(" + con2 + "));\n\n");	
										      con2++;
									      }								
								      }
							      }
							      else if(word.equalsIgnoreCase("Crossbar_type"))
							      {
									 // NoC projetada só tem um clock, usar buffer HERMES e Crossbar Hermes_Crossbar
								     String clock="";
									 int flag=0;
									 for(int tmp=0;tmp < p.getNoC().getClock().size();tmp++)
									 {
											if(tmp == 0) clock = p.getNoC().getClock().get(0).getRouter();
											if(p.getNoC().getClock().get(tmp).getRouter().equals(clock) && p.getNoC().getClock().get(tmp).getIpInput().equals(clock)) { }
											else { flag = 1; break; } 
													
									  }
									  
									  word = "";
									  
									  // Geração do crossbar
									  // DIDI
									  if(array[0] != '0' && array[1] == '0' && array[2] != '0'&& array[3] == '0')      // BL
											word = word + "HermesG_crossbar_BL(";	
									  // DDDI
									  else if(array[0] != '0' && array[1] != '0' && array[2] != '0'&& array[3] == '0') // BC
											word = word + "HermesG_crossbar_BC(";	
									  // IDDI
									  else if(array[0] == '0' && array[1] != '0' && array[2] != '0'&& array[3] == '0') // BR
											word = word + "HermesG_crossbar_BR(";	
									  // DIDD
									  else if(array[0] != '0' && array[1] == '0' && array[2] != '0'&& array[3] != '0') // CL
											word = word + "HermesG_crossbar_CL(";	
									  // DDDD
									  else if(array[0] != '0' && array[1] != '0' && array[2] != '0'&& array[3] != '0') // CC
											word = word + "HermesG_crossbar_CC(";	
									  // IDDD
									  else if(array[0] == '0' && array[1] != '0' && array[2] != '0'&& array[3] != '0') // CR
											word = word + "HermesG_crossbar_CR(";	
									  // DIID
									  else if(array[0] != '0' && array[1] == '0' && array[2] == '0'&& array[3] != '0') // TL
											word = word + "HermesG_crossbar_TL(";	
									  // DDID
									  else if(array[0] != '0' && array[1] != '0' && array[2] == '0'&& array[3] != '0') // TC
											word = word + "HermesG_crossbar_TC(";	
									  // IDID
									  else if(array[0] == '0' && array[1] != '0' && array[2] == '0'&& array[3] != '0') // TR
											word = word + "HermesG_crossbar_TR(";	
										  
									  for(int c=0;c < p.getNoC().getRoutingAlgorithm().length();c++)
									  {
										  if(p.getNoC().getRoutingAlgorithm().charAt(c) != ' ')
										  {
											  word = word + p.getNoC().getRoutingAlgorithm().charAt(c);
										  }
									  }
									  
								      if(flag == 0) word = word + ")";
								      else if(flag == 1)  word = word + "G)";
								      
							      }
							      else if(word.equalsIgnoreCase("Algorithm_type"))
							      {
								      word = "";
								      for(int c=0;c < p.getNoC().getRoutingAlgorithm().length();c++)
								      {
									      if(p.getNoC().getRoutingAlgorithm().charAt(c) != ' ')
									      {
										      word = word + p.getNoC().getRoutingAlgorithm().charAt(c);
									      }
								      }
							      }
							      else if(word.equalsIgnoreCase("Pin_ground"))
							      {
								      word="";
								      for(int j=0;j<5;j++)
								      {
									      if(array[j] == '0')
									      {
										      data_output.writeBytes("\n\t\th(" + j + ")<='0';");
										      data_output.writeBytes("\n\t\tdata_av(" + j + ")<='0';");
										      data_output.writeBytes("\n\t\tdata(" + j + ")<=(others=>'0');");
										      data_output.writeBytes("\n\t\tsender(" + j + ")<='0';");
										      data_output.writeBytes("\n\t\tcredit_o(" + j + ")<='0';\n");
									      }	
								      }
							      }
							      else if(word.equalsIgnoreCase("router_pin"))
							      {
								      word="";
								      int num=0;
								      for(int j=0;j<5;j++)
								      {
									      if(array[j] != '0') num++;
								      }			
								      data_output.writeBytes("\trot_empty_buf:  out empty_buf_" + num + ";");	
							      }
							      change_parameter = change_parameter.concat(word);
							      data_output.writeBytes(change_parameter);
						      }
						      data_output.writeBytes("\r\n");
						      n_lines++;
						      line=buff.readLine();
					      } 
					      buff.close();
					      data_output.close();
					      inFile.close();
				      }//end try
				      catch(Exception e)
				      {
					      JOptionPane.showMessageDialog(null,e.getMessage(),"Error message", JOptionPane.ERROR_MESSAGE);
					      System.exit(0);
				      }
				}
			}
		}
		public void generateNOC()
		{
			try
			{
				FileInputStream inFile = new FileInputStream(new File(sourceDir + "NOC.vhd"));
				BufferedReader buff=new BufferedReader(new InputStreamReader(inFile));
				
				DataOutputStream data_output=new DataOutputStream(new FileOutputStream(nocDir + "NOC.vhd"));
			
				int n_lines=0;
				int con3=0,con4=0,con5=0;
				change_parameter="";
				line=buff.readLine();
				while(line!=null)
				{
					st = new StringTokenizer(line, "$");
					int vem = st.countTokens();
					for (int cont=0; cont<vem; cont++)
					{
						word = st.nextToken();
						change_parameter="";
						if(word.equalsIgnoreCase("map_router"))
						{
							String xHexa,yHexa,routerName;
							int con=0;
							for(int x=0;x<dimX;x++)
							{
								xHexa = Convert.decToHex(x,(p.getNoC().getFlitSize()/8));
								for(int y=0;y<dimY;y++,con++)
								{
										yHexa = Convert.decToHex(y,(p.getNoC().getFlitSize()/8));
										data_output.writeBytes("\t" + "router" + xHexa + yHexa + this.generateNameRouter(con) + ": Entity work.router" + xHexa + yHexa + this.generateNameRouter(con) + "\n");
										data_output.writeBytes("\tgeneric map( address => ADDRESSN"+xHexa+yHexa+" )\n");
										data_output.writeBytes("\tport map(\n");
										data_output.writeBytes("\t\tclock    => " + this.dotremove(p.getNoC().getClock().get(con).getLabelClockRouter()) + ",\n");
										data_output.writeBytes("\t\treset    => reset,\n");
										data_output.writeBytes("\t\tclock_rx => clock_rxN"+xHexa+yHexa+",\n");
										data_output.writeBytes("\t\trx       => rxN"+xHexa+yHexa+",\n");
										data_output.writeBytes("\t\tdata_in  => data_inN"+xHexa+yHexa+",\n");
										data_output.writeBytes("\t\tcredit_o => credit_oN"+xHexa+yHexa+",\n");
										data_output.writeBytes("\t\tclock_tx => clock_txN"+xHexa+yHexa+",\n");
										data_output.writeBytes("\t\ttx       => txN"+xHexa+yHexa+",\n");
										data_output.writeBytes("\t\tdata_out => data_outN"+xHexa+yHexa+",\n");
										String saida = generateNameRouter(con);
										int tmp=0;
										for(int j=0;j<saida.length();j++)
										{
											if(saida.charAt(j) == '0') tmp++;
										}
										if(tmp == 2) { data_output.writeBytes("\t\trot_empty_buf => noc_buf3(" + con3 + "),\n"); con3++; }
										else if(tmp == 1) { data_output.writeBytes("\t\trot_empty_buf => noc_buf4(" + con4 + "),\n"); con4++; }
										else if(tmp == 0) { data_output.writeBytes("\t\trot_empty_buf => noc_buf5(" + con5 + "),\n"); con5++; }
										
										data_output.writeBytes("\t\tcredit_i => credit_iN"+xHexa+yHexa+");\n\n");
								}
							}
						}
						
						/* Bloco de portmap da entidade de avaliação interna do tráfego sintético na NOC. 
						 * Inserir dentro de noc.VHD a TAG $out_router$
						else if(word.equalsIgnoreCase("out_router"))
						{
							// Bloco reponsavel por fazer o port map com os pinos do módulo de avaliacao interna, este cara devera ser modificado.
							String xHexa,yHexa,word;
							int con=0,j=0;
							data_output.writeBytes("\trouter_output: Entity work.outmodulerouter");
							data_output.writeBytes("\n\tport map(");
							data_output.writeBytes("\n\tref_clock      => ref_clock,"); 
							data_output.writeBytes("\n\treset          => reset,"); 
							for(int y=0;y < dimY;y++)
							{
								yHexa = Convert.decToHex(y,(p.getNoC().getFlitSize()/8));
								for(int x=0;x < dimX;x++,con++)
								{
									xHexa = Convert.decToHex(x,(p.getNoC().getFlitSize()/8));
									word = ""+x+y;
									for(j=0;j<dimX*dimY;j++) if(p.getNoC().getClock().get(j).getNumberRouter().equals(word)) break;
									String interfaces = p.getNoC().getClock().get(j).getRouterInterfaces();
									for(int i=0;i<4;i++)
									{
										if((int)interfaces.charAt(i) == 83 || (int)interfaces.charAt(i) == 65)
										{
											String label = "";
											if(i == 0) label = "EAST";
											else if(i == 1) label = "WEST";
											else if(i == 2) label = "NORTH";
											else if(i == 3) label = "SOUTH";
											data_output.writeBytes("\n\ttx_r" + con + "p" + i + "        => txN" + xHexa + yHexa + "(" + label + "),");
											data_output.writeBytes("\n\tout_r"+ con + "p" + i + "       => data_outN" + xHexa + yHexa + "(" + label + "),");
											data_output.writeBytes("\n\tcredit_ir" + con + "p" + i + "   => credit_iN" + xHexa + yHexa + "(" + label + "),");
										}
									}
									if(con == (dimX*dimY)-1) data_output.writeBytes("\n\tclk" + con + "           => " + p.getNoC().getClock().get(j).getLabelClockRouter());
									else data_output.writeBytes("\n\tclk" + con + "           => " + p.getNoC().getClock().get(j).getLabelClockRouter() + "," );
								}
							}
							data_output.writeBytes("\n\t);");
						}
						*/
						else if(word.equalsIgnoreCase("clock_noc"))
						{
							for(int x=0;x<p.getNoC().getRefClockList().size();x++)
							{
								for(int i=0;i < dimX*dimY;i++)
								{
									if(p.getNoC().getRefClockList().get(x).getAllAvailableValue().equals(p.getNoC().getClock().get(i).getRouter()))
									{
										data_output.writeBytes("\n\t" + this.dotremove(p.getNoC().getClock().get(i).getLabelClockRouter()) + "      : in std_logic;");
										break;
									}
								}
                            }
							int p3=0,p4=0,p5=0,tmp=0;
							for(int i=0;i<p.getNoC().getNumRotX() *  p.getNoC().getNumRotY();i++)
							{
								String saida = generateNameRouter(i);
								tmp=0;
								for(int j=0;j<saida.length();j++)
								{
									if(saida.charAt(j) == '0') tmp++;
								}
								if(tmp == 2) { p3++; }
								else if(tmp == 1) { p4++; }
								else if(tmp == 0) { p5++; }
							}
							if(p3 > 0) data_output.writeBytes("\n\tnoc_buf3      : out noc_buf_3;");
							if(p4 > 0) data_output.writeBytes("\n\tnoc_buf4 : out noc_buf_4;");
							if(p5 > 0) data_output.writeBytes("\n\tnoc_buf5 : out noc_buf_5;");
						}
						else if(word.equalsIgnoreCase("signal_router"))
						{
							String xHexa,yHexa,routerName;
							int con=0;
							for(int y=0;y<dimY;y++)
							{
								yHexa = Convert.decToHex(y,(p.getNoC().getFlitSize()/8));
								for(int x=0;x<dimX;x++,con++)
								{
										xHexa = Convert.decToHex(x,(p.getNoC().getFlitSize()/8));
										routerName = Router.getRouterType(x,y,dimX,dimY);
										data_output.writeBytes("\n\tsignal clock_rxN" + xHexa + yHexa + " : regNport;");
										data_output.writeBytes("\n\tsignal rxN" + xHexa + yHexa + "       : regNport;");
										data_output.writeBytes("\n\tsignal data_inN" + xHexa + yHexa + "  : arrayNport_regflit;");
										data_output.writeBytes("\n\tsignal credit_oN" + xHexa + yHexa + " : regNport;");
										data_output.writeBytes("\n\tsignal clock_txN" + xHexa + yHexa + " : regNport;");
										data_output.writeBytes("\n\tsignal txN" + xHexa + yHexa + "       : regNport;");
										data_output.writeBytes("\n\tsignal data_outN" + xHexa + yHexa + " : arrayNport_regflit;");
										data_output.writeBytes("\n\tsignal credit_iN" + xHexa + yHexa + " : regNport;\n");
								}
							}	
						}
						else if(word.equalsIgnoreCase("port_router")) 
						{
							String xHexa,yHexa,routerName;
							String xHexap,yHexap,xHexas,yHexas;
							int con=0;
							for(int y=0;y<dimY;y++)
							{
								for(int x=0;x<dimX;x++,con++)
								{
									xHexa = Convert.decToHex(Integer.parseInt(p.getNoC().getClock().get(con).getNumberRouterX()),(p.getNoC().getFlitSize()/8));
									xHexap = Convert.decToHex((Integer.parseInt(p.getNoC().getClock().get(con).getNumberRouterX())+1),(p.getNoC().getFlitSize()/8));
									xHexas = Convert.decToHex((Integer.parseInt(p.getNoC().getClock().get(con).getNumberRouterX())-1),(p.getNoC().getFlitSize()/8));
									
									yHexa = Convert.decToHex(Integer.parseInt(p.getNoC().getClock().get(con).getNumberRouterY()),(p.getNoC().getFlitSize()/8));
									yHexap = Convert.decToHex((Integer.parseInt(p.getNoC().getClock().get(con).getNumberRouterY())+1),(p.getNoC().getFlitSize()/8));
									yHexas = Convert.decToHex((Integer.parseInt(p.getNoC().getClock().get(con).getNumberRouterY())-1),(p.getNoC().getFlitSize()/8));
									routerName = Router.getRouterType(x,y,dimX,dimY);
									if(p.getNoC().getClock().get(con).getNumberRouterX().equals("0") && p.getNoC().getClock().get(con).getNumberRouterY().equals("0")) 
									{
										data_output.writeBytes("\t -- Router BL -- Port" + p.getNoC().getClock().get(con).getNumberRouterX() + p.getNoC().getClock().get(con).getNumberRouterY());
										// East(0) plug in West(1) 
										data_output.writeBytes("\n\tclock_rxN" + xHexa + yHexa + "(0) <= clock_txN" + xHexap + yHexa + "(1);");
										data_output.writeBytes("\n\trxN" + xHexa + yHexa + "(0)       <= txN" + xHexap + yHexa + "(1);");
										data_output.writeBytes("\n\tdata_inN" + xHexa + yHexa + "(0)  <= data_outN" + xHexap + yHexa + "(1);");
										data_output.writeBytes("\n\tcredit_iN" + xHexa + yHexa + "(0) <= credit_oN" + xHexap + yHexa + "(1);\n");
										// North(2) plug in South(3)
										data_output.writeBytes("\n\tclock_rxN" + xHexa + yHexa + "(2) <= clock_txN" + xHexa + yHexap + "(3);");
										data_output.writeBytes("\n\trxN" + xHexa + yHexa + "(2)       <= txN" + xHexa + yHexap + "(3);");
										data_output.writeBytes("\n\tdata_inN" + xHexa + yHexa + "(2)  <= data_outN" + xHexa + yHexap + "(3);");
										data_output.writeBytes("\n\tcredit_iN" + xHexa + yHexa + "(2) <= credit_oN" + xHexa + yHexap + "(3);\n");
										// South in ground
										data_output.writeBytes("\n\tclock_rxN" + xHexa + yHexa + "(3) <= '0';");
										data_output.writeBytes("\n\trxN" + xHexa + yHexa + "(3)       <= '0';");
										data_output.writeBytes("\n\tdata_inN" + xHexa + yHexa + "(3)  <= (others=>'0');");
										data_output.writeBytes("\n\tcredit_iN" + xHexa + yHexa + "(3) <= '0';\n");
										// West in ground
										data_output.writeBytes("\n\tclock_rxN" + xHexa + yHexa + "(1) <= '0';");
										data_output.writeBytes("\n\trxN" + xHexa + yHexa + "(1)       <= '0';");
										data_output.writeBytes("\n\tdata_inN" + xHexa + yHexa + "(1)  <= (others=>'0');");
										data_output.writeBytes("\n\tcredit_iN" + xHexa + yHexa + "(1) <= '0';\n");	
									}	
									else if(Integer.parseInt(p.getNoC().getClock().get(con).getNumberRouterY()) < p.getNoC().getNumRotY()-1 && p.getNoC().getClock().get(con).getNumberRouterX().equals("0"))
									{
										
										data_output.writeBytes("\t -- Router CL -- Port" + p.getNoC().getClock().get(con).getNumberRouterX() + p.getNoC().getClock().get(con).getNumberRouterY());
										// East(0) plug in West(1) 
										data_output.writeBytes("\n\tclock_rxN" + xHexa + yHexa + "(0) <= clock_txN" + xHexap + yHexa + "(1);");
										data_output.writeBytes("\n\trxN" + xHexa + yHexa + "(0)       <= txN" + xHexap + yHexa + "(1);");
										data_output.writeBytes("\n\tdata_inN" + xHexa + yHexa + "(0)  <= data_outN" + xHexap + yHexa + "(1);");
										data_output.writeBytes("\n\tcredit_iN" + xHexa + yHexa + "(0) <= credit_oN" + xHexap + yHexa + "(1);\n");
										// North(2) plug in South(3)
										data_output.writeBytes("\n\tclock_rxN" + xHexa + yHexa + "(2) <= clock_txN" + xHexa + yHexap + "(3);");
										data_output.writeBytes("\n\trxN" + xHexa + yHexa + "(2)       <= txN" + xHexa + yHexap + "(3);");
										data_output.writeBytes("\n\tdata_inN" + xHexa + yHexa + "(2)  <= data_outN" + xHexa + yHexap + "(3);");
										data_output.writeBytes("\n\tcredit_iN" + xHexa + yHexa + "(2) <= credit_oN" + xHexa + yHexap + "(3);\n");
										// South(3) plug in North(2)
										data_output.writeBytes("\n\tclock_rxN" + xHexa + yHexa + "(3) <= clock_txN" + xHexa + yHexas + "(2);");
										data_output.writeBytes("\n\trxN" + xHexa + yHexa + "(3)       <= txN" + xHexa + yHexas + "(2);");
										data_output.writeBytes("\n\tdata_inN" + xHexa + yHexa + "(3)  <= data_outN" + xHexa + yHexas + "(2);");
										data_output.writeBytes("\n\tcredit_iN" + xHexa + yHexa + "(3) <= credit_oN" + xHexa + yHexas + "(2);\n");
										// West in ground
										data_output.writeBytes("\n\tclock_rxN" + xHexa + yHexa + "(1) <= '0';");
										data_output.writeBytes("\n\trxN" + xHexa + yHexa + "(1)       <= '0';");
										data_output.writeBytes("\n\tdata_inN" + xHexa + yHexa + "(1)  <= (others=>'0');");
										data_output.writeBytes("\n\tcredit_iN" + xHexa + yHexa + "(1) <= '0';\n");
										
									}
									else if(Integer.parseInt(p.getNoC().getClock().get(con).getNumberRouterY()) == p.getNoC().getNumRotY()-1 && p.getNoC().getClock().get(con).getNumberRouterX().equals("0"))
									{
										
										data_output.writeBytes("\t -- Router TL -- Port" + p.getNoC().getClock().get(con).getNumberRouterX() + p.getNoC().getClock().get(con).getNumberRouterY());
										// East(0) plug in West(1) 
										data_output.writeBytes("\n\tclock_rxN" + xHexa + yHexa + "(0) <= clock_txN" + xHexap + yHexa + "(1);");
										data_output.writeBytes("\n\trxN" + xHexa + yHexa + "(0)       <= txN" + xHexap + yHexa + "(1);");
										data_output.writeBytes("\n\tdata_inN" + xHexa + yHexa + "(0)  <= data_outN" + xHexap + yHexa + "(1);");
										data_output.writeBytes("\n\tcredit_iN" + xHexa + yHexa + "(0) <= credit_oN" + xHexap + yHexa + "(1);\n");
										// North in ground
										data_output.writeBytes("\n\tclock_rxN" + xHexa + yHexa + "(2) <= '0';");
										data_output.writeBytes("\n\trxN" + xHexa + yHexa + "(2)       <= '0';");
										data_output.writeBytes("\n\tdata_inN" + xHexa + yHexa + "(2)  <= (others=>'0');");
										data_output.writeBytes("\n\tcredit_iN" + xHexa + yHexa + "(2) <= '0';\n");
										// South(3) plug in North(2)
										data_output.writeBytes("\n\tclock_rxN" + xHexa + yHexa + "(3) <= clock_txN" + xHexa + yHexas + "(2);");
										data_output.writeBytes("\n\trxN" + xHexa + yHexa + "(3)       <= txN" + xHexa + yHexas + "(2);");
										data_output.writeBytes("\n\tdata_inN" + xHexa + yHexa + "(3)  <= data_outN" + xHexa + yHexas + "(2);");
										data_output.writeBytes("\n\tcredit_iN" + xHexa + yHexa + "(3) <= credit_oN" + xHexa + yHexas + "(2);\n");
										// West in ground
										data_output.writeBytes("\n\tclock_rxN" + xHexa + yHexa + "(1) <= '0';");
										data_output.writeBytes("\n\trxN" + xHexa + yHexa + "(1)       <= '0';");
										data_output.writeBytes("\n\tdata_inN" + xHexa + yHexa + "(1)  <= (others=>'0');");
										data_output.writeBytes("\n\tcredit_iN" + xHexa + yHexa + "(1) <= '0';\n");
									}		
									else if(Integer.parseInt(p.getNoC().getClock().get(con).getNumberRouterX()) > 0 && Integer.parseInt(p.getNoC().getClock().get(con).getNumberRouterY()) == 0 && Integer.parseInt(p.getNoC().getClock().get(con).getNumberRouterX()) < p.getNoC().getNumRotX()-1)
									{ 
										data_output.writeBytes("\t -- Router BC -- Port" + p.getNoC().getClock().get(con).getNumberRouterX() + p.getNoC().getClock().get(con).getNumberRouterY());
										// East(0) plug in West(1) 
										data_output.writeBytes("\n\tclock_rxN" + xHexa + yHexa + "(0) <= clock_txN" + xHexap + yHexa + "(1);");
										data_output.writeBytes("\n\trxN" + xHexa + yHexa + "(0)       <= txN" + xHexap + yHexa + "(1);");
										data_output.writeBytes("\n\tdata_inN" + xHexa + yHexa + "(0)  <= data_outN" + xHexap + yHexa + "(1);");
										data_output.writeBytes("\n\tcredit_iN" + xHexa + yHexa + "(0) <= credit_oN" + xHexap + yHexa + "(1);\n");
										// West(1) in East(0)
										data_output.writeBytes("\n\tclock_rxN" + xHexa + yHexa + "(1) <= clock_txN" + xHexas + yHexa + "(0);");
										data_output.writeBytes("\n\trxN" + xHexa + yHexa + "(1)       <= txN" + xHexas + yHexa + "(0);");
										data_output.writeBytes("\n\tdata_inN" + xHexa + yHexa + "(1)  <= data_outN" + xHexas + yHexa + "(0);");
										data_output.writeBytes("\n\tcredit_iN" + xHexa + yHexa + "(1) <= credit_oN" + xHexas + yHexa + "(0);\n");
										// North(2) plug in South(3)
										data_output.writeBytes("\n\tclock_rxN" + xHexa + yHexa + "(2) <= clock_txN" + xHexa + yHexap + "(3);");
										data_output.writeBytes("\n\trxN" + xHexa + yHexa + "(2)       <= txN" + xHexa + yHexap + "(3);");
										data_output.writeBytes("\n\tdata_inN" + xHexa + yHexa + "(2)  <= data_outN" + xHexa + yHexap + "(3);");
										data_output.writeBytes("\n\tcredit_iN" + xHexa + yHexa + "(2) <= credit_oN" + xHexa + yHexap + "(3);\n");
										// South(3) plug in Ground
										data_output.writeBytes("\n\tclock_rxN" + xHexa + yHexa + "(3) <= '0';");
										data_output.writeBytes("\n\trxN" + xHexa + yHexa + "(3)       <= '0';");
										data_output.writeBytes("\n\tdata_inN" + xHexa + yHexa + "(3)  <= (others=>'0');");
										data_output.writeBytes("\n\tcredit_iN" + xHexa + yHexa + "(3) <= '0';\n");
									}
									else if(Integer.parseInt(p.getNoC().getClock().get(con).getNumberRouterX()) > 0 && Integer.parseInt(p.getNoC().getClock().get(con).getNumberRouterY()) > 0 && Integer.parseInt(p.getNoC().getClock().get(con).getNumberRouterX()) < p.getNoC().getNumRotX()-1 && Integer.parseInt(p.getNoC().getClock().get(con).getNumberRouterY()) < p.getNoC().getNumRotY()-1)
									{ 
										data_output.writeBytes("\t -- Router CC -- Port" + p.getNoC().getClock().get(con).getNumberRouterX() + p.getNoC().getClock().get(con).getNumberRouterY());
										// East(0) plug in West(1) 
										data_output.writeBytes("\n\tclock_rxN" + xHexa + yHexa + "(0) <= clock_txN" + xHexap + yHexa + "(1);");
										data_output.writeBytes("\n\trxN" + xHexa + yHexa + "(0)       <= txN" + xHexap + yHexa + "(1);");
										data_output.writeBytes("\n\tdata_inN" + xHexa + yHexa + "(0)  <= data_outN" + xHexap + yHexa + "(1);");
										data_output.writeBytes("\n\tcredit_iN" + xHexa + yHexa + "(0) <= credit_oN" + xHexap + yHexa + "(1);\n");
										// West(1) plug in East(0)
										data_output.writeBytes("\n\tclock_rxN" + xHexa + yHexa + "(1) <= clock_txN" + xHexas + yHexa + "(0);");
										data_output.writeBytes("\n\trxN" + xHexa + yHexa + "(1)       <= txN" + xHexas + yHexa + "(0);");
										data_output.writeBytes("\n\tdata_inN" + xHexa + yHexa + "(1)  <= data_outN" + xHexas + yHexa + "(0);");
										data_output.writeBytes("\n\tcredit_iN" + xHexa + yHexa + "(1) <= credit_oN" + xHexas + yHexa + "(0);\n");
										// North(2) plug in South(3)
										data_output.writeBytes("\n\tclock_rxN" + xHexa + yHexa + "(2) <= clock_txN" + xHexa + yHexap + "(3);");
										data_output.writeBytes("\n\trxN" + xHexa + yHexa + "(2)       <= txN" + xHexa + yHexap + "(3);");
										data_output.writeBytes("\n\tdata_inN" + xHexa + yHexa + "(2)  <= data_outN" + xHexa + yHexap + "(3);");
										data_output.writeBytes("\n\tcredit_iN" + xHexa + yHexa + "(2) <= credit_oN" + xHexa + yHexap + "(3);\n");
										// South(3) plug in North(2)
										data_output.writeBytes("\n\tclock_rxN" + xHexa + yHexa + "(3) <= clock_txN" + xHexa + yHexas + "(2);");
										data_output.writeBytes("\n\trxN" + xHexa + yHexa + "(3)       <= txN" + xHexa + yHexas + "(2);");
										data_output.writeBytes("\n\tdata_inN" + xHexa + yHexa + "(3)  <= data_outN" + xHexa + yHexas + "(2);");
										data_output.writeBytes("\n\tcredit_iN" + xHexa + yHexa + "(3) <= credit_oN" + xHexa + yHexas + "(2);\n");
									}
									else if(Integer.parseInt(p.getNoC().getClock().get(con).getNumberRouterY()) == p.getNoC().getNumRotY()-1 && Integer.parseInt(p.getNoC().getClock().get(con).getNumberRouterX()) > 0 && Integer.parseInt(p.getNoC().getClock().get(con).getNumberRouterX()) < p.getNoC().getNumRotX()-1)
									{ 
										data_output.writeBytes("\t -- Router TC -- Port" + p.getNoC().getClock().get(con).getNumberRouterX() + p.getNoC().getClock().get(con).getNumberRouterY());
										// East(0) in West(1)
										data_output.writeBytes("\n\tclock_rxN" + xHexa + yHexa + "(0) <= clock_txN" + xHexap + yHexa + "(1);");
										data_output.writeBytes("\n\trxN" + xHexa + yHexa + "(0)       <= txN" + xHexap + yHexa + "(1);");
										data_output.writeBytes("\n\tdata_inN" + xHexa + yHexa + "(0)  <= data_outN" + xHexap + yHexa + "(1);");
										data_output.writeBytes("\n\tcredit_iN" + xHexa + yHexa + "(0) <= credit_oN" + xHexap + yHexa + "(1);\n");
										// North(2) plug in Ground
										data_output.writeBytes("\n\tclock_rxN" + xHexa + yHexa + "(2) <= '0';");
										data_output.writeBytes("\n\trxN" + xHexa + yHexa + "(2)       <= '0';");
										data_output.writeBytes("\n\tdata_inN" + xHexa + yHexa + "(2)  <= (others=>'0');");
										data_output.writeBytes("\n\tcredit_iN" + xHexa + yHexa + "(2) <= '0';\n");
										// South(3) plug in North(2)
										data_output.writeBytes("\n\tclock_rxN" + xHexa + yHexa + "(3) <= clock_txN" + xHexa + yHexas + "(2);");
										data_output.writeBytes("\n\trxN" + xHexa + yHexa + "(3)       <= txN" + xHexa + yHexas + "(2);");
										data_output.writeBytes("\n\tdata_inN" + xHexa + yHexa + "(3)  <= data_outN" + xHexa + yHexas + "(2);");
										data_output.writeBytes("\n\tcredit_iN" + xHexa + yHexa + "(3) <= credit_oN" + xHexa + yHexas + "(2);\n");
										// West(1) plug in East(0)
										data_output.writeBytes("\n\tclock_rxN" + xHexa + yHexa + "(1) <= clock_txN" + xHexas + yHexa + "(0);");
										data_output.writeBytes("\n\trxN" + xHexa + yHexa + "(1)       <= txN" + xHexas + yHexa + "(0);");
										data_output.writeBytes("\n\tdata_inN" + xHexa + yHexa + "(1)  <= data_outN" + xHexas + yHexa + "(0);");
										data_output.writeBytes("\n\tcredit_iN" + xHexa + yHexa + "(1) <= credit_oN" + xHexas + yHexa + "(0);\n");
									}
									else if(Integer.parseInt(p.getNoC().getClock().get(con).getNumberRouterX()) == p.getNoC().getNumRotX()-1 && Integer.parseInt(p.getNoC().getClock().get(con).getNumberRouterY()) == 0)
									{ 
										data_output.writeBytes("\t -- Router BR -- Port" + p.getNoC().getClock().get(con).getNumberRouterX() + p.getNoC().getClock().get(con).getNumberRouterY());
										// East(0) plug in ground
										data_output.writeBytes("\n\tclock_rxN" + xHexa + yHexa + "(0) <= '0';");
										data_output.writeBytes("\n\trxN" + xHexa + yHexa + "(0)       <= '0';");
										data_output.writeBytes("\n\tdata_inN" + xHexa + yHexa + "(0)  <= (others=>'0');");
										data_output.writeBytes("\n\tcredit_iN" + xHexa + yHexa + "(0) <= '0';\n");
										// North(2) plug in South(3)
										data_output.writeBytes("\n\tclock_rxN" + xHexa + yHexa + "(2) <= clock_txN" + xHexa + yHexap + "(3);");
										data_output.writeBytes("\n\trxN" + xHexa + yHexa + "(2)       <= txN" + xHexa + yHexap + "(3);");
										data_output.writeBytes("\n\tdata_inN" + xHexa + yHexa + "(2)  <= data_outN" + xHexa + yHexap + "(3);");
										data_output.writeBytes("\n\tcredit_iN" + xHexa + yHexa + "(2) <= credit_oN" + xHexa + yHexap + "(3);\n");
										// South(3) plug in Ground
										data_output.writeBytes("\n\tclock_rxN" + xHexa + yHexa + "(3) <= '0';");
										data_output.writeBytes("\n\trxN" + xHexa + yHexa + "(3)       <= '0';");
										data_output.writeBytes("\n\tdata_inN" + xHexa + yHexa + "(3)  <= (others=>'0');");
										data_output.writeBytes("\n\tcredit_iN" + xHexa + yHexa + "(3) <= '0';\n");
										// West(1) plug in East(0)
										data_output.writeBytes("\n\tclock_rxN" + xHexa + yHexa + "(1) <= clock_txN" + xHexas + yHexa + "(0);");
										data_output.writeBytes("\n\trxN" + xHexa + yHexa + "(1)       <= txN" + xHexas + yHexa + "(0);");
										data_output.writeBytes("\n\tdata_inN" + xHexa + yHexa + "(1)  <= data_outN" + xHexas + yHexa + "(0);");
										data_output.writeBytes("\n\tcredit_iN" + xHexa + yHexa + "(1) <= credit_oN" + xHexas + yHexa + "(0);\n");
									}
									else if(Integer.parseInt(p.getNoC().getClock().get(con).getNumberRouterX()) == p.getNoC().getNumRotX()-1 && Integer.parseInt(p.getNoC().getClock().get(con).getNumberRouterY()) < p.getNoC().getNumRotY()-1 && Integer.parseInt(p.getNoC().getClock().get(con).getNumberRouterY()) > 0)
									{ 
										data_output.writeBytes("\t -- Router CR -- Port" + p.getNoC().getClock().get(con).getNumberRouterX() + p.getNoC().getClock().get(con).getNumberRouterY());
										// East(0) plug in West(1) 
										data_output.writeBytes("\n\tclock_rxN" + xHexa + yHexa + "(0) <= '0';");
										data_output.writeBytes("\n\trxN" + xHexa + yHexa + "(0)       <= '0';");
										data_output.writeBytes("\n\tdata_inN" + xHexa + yHexa + "(0)  <= (others=>'0');");
										data_output.writeBytes("\n\tcredit_iN" + xHexa + yHexa + "(0) <= '0';\n");
										// North(2) plug in South(3)
										data_output.writeBytes("\n\tclock_rxN" + xHexa + yHexa + "(2) <= clock_txN" + xHexa + yHexap + "(3);");
										data_output.writeBytes("\n\trxN" + xHexa + yHexa + "(2)       <= txN" + xHexa + yHexap + "(3);");
										data_output.writeBytes("\n\tdata_inN" + xHexa + yHexa + "(2)  <= data_outN" + xHexa + yHexap + "(3);");
										data_output.writeBytes("\n\tcredit_iN" + xHexa + yHexa + "(2) <= credit_oN" + xHexa + yHexap + "(3);\n");
										// South(3) plug in North(2)
										data_output.writeBytes("\n\tclock_rxN" + xHexa + yHexa + "(3) <= clock_txN" + xHexa + yHexas + "(2);");
										data_output.writeBytes("\n\trxN" + xHexa + yHexa + "(3)       <= txN" + xHexa + yHexas + "(2);");
										data_output.writeBytes("\n\tdata_inN" + xHexa + yHexa + "(3)  <= data_outN" + xHexa + yHexas + "(2);");
										data_output.writeBytes("\n\tcredit_iN" + xHexa + yHexa + "(3) <= credit_oN" + xHexa + yHexas + "(2);\n");
										// West(1) plug in East(0)
										data_output.writeBytes("\n\tclock_rxN" + xHexa + yHexa + "(1) <= clock_txN" + xHexas + yHexa + "(0);");
										data_output.writeBytes("\n\trxN" + xHexa + yHexa + "(1)       <= txN" + xHexas + yHexa + "(0);");
										data_output.writeBytes("\n\tdata_inN" + xHexa + yHexa + "(1)  <= data_outN" + xHexas + yHexa + "(0);");
										data_output.writeBytes("\n\tcredit_iN" + xHexa + yHexa + "(1) <= credit_oN" + xHexas + yHexa + "(0);\n");
									}
									else if(Integer.parseInt(p.getNoC().getClock().get(con).getNumberRouterX()) == p.getNoC().getNumRotX()-1 && Integer.parseInt(p.getNoC().getClock().get(con).getNumberRouterY()) == p.getNoC().getNumRotY()-1)
									{ 
										data_output.writeBytes("\t -- Router TR -- Port" + p.getNoC().getClock().get(con).getNumberRouterX() + p.getNoC().getClock().get(con).getNumberRouterY());
										// East(0) plug in Ground 
										data_output.writeBytes("\n\tclock_rxN" + xHexa + yHexa + "(0) <= '0';");
										data_output.writeBytes("\n\trxN" + xHexa + yHexa + "(0)       <= '0';");
										data_output.writeBytes("\n\tdata_inN" + xHexa + yHexa + "(0)  <= (others=>'0');");
										data_output.writeBytes("\n\tcredit_iN" + xHexa + yHexa + "(0) <= '0';\n");
										// North(2) plug ground
										data_output.writeBytes("\n\tclock_rxN" + xHexa + yHexa + "(2) <= '0';");
										data_output.writeBytes("\n\trxN" + xHexa + yHexa + "(2)       <= '0';");
										data_output.writeBytes("\n\tdata_inN" + xHexa + yHexa + "(2)  <= (others=>'0');");
										data_output.writeBytes("\n\tcredit_iN" + xHexa + yHexa + "(2) <= '0';\n");
										// South in ground
										data_output.writeBytes("\n\tclock_rxN" + xHexa + yHexa + "(3) <= clock_txN" + xHexa + yHexas + "(2);");
										data_output.writeBytes("\n\trxN" + xHexa + yHexa + "(3)       <= txN" + xHexa + yHexas + "(2);");
										data_output.writeBytes("\n\tdata_inN" + xHexa + yHexa + "(3)  <= data_outN" + xHexa + yHexas + "(2);");
										data_output.writeBytes("\n\tcredit_iN" + xHexa + yHexa + "(3) <= credit_oN" + xHexa + yHexas + "(2);\n");
										// West(1) plug in East(0)
										data_output.writeBytes("\n\tclock_rxN" + xHexa + yHexa + "(1) <= clock_txN" + xHexas + yHexa + "(0);");
										data_output.writeBytes("\n\trxN" + xHexa + yHexa + "(1)       <= txN" + xHexas + yHexa + "(0);");
										data_output.writeBytes("\n\tdata_inN" + xHexa + yHexa + "(1)  <= data_outN" + xHexas + yHexa + "(0);");
										data_output.writeBytes("\n\tcredit_iN" + xHexa + yHexa + "(1) <= credit_oN" + xHexas + yHexa + "(0);\n");
									}
									data_output.writeBytes("\n\tclock_rxN" + xHexa + yHexa + "(4)      <=  clock_rxLocal(N" + xHexa + yHexa + ");");
									data_output.writeBytes("\n\trxN" + xHexa + yHexa + "(4)            <=  rxLocal(N" + xHexa + yHexa + ");");
									data_output.writeBytes("\n\tdata_inN" + xHexa + yHexa + "(4)       <=  data_inLocal(N" + xHexa + yHexa + ");");
									data_output.writeBytes("\n\tcredit_iN" + xHexa + yHexa + "(4)      <=  credit_iLocal(N" + xHexa + yHexa + ");");
									data_output.writeBytes("\n\tclock_txLocal(N" + xHexa + yHexa + ")  <=  clock_txN" + xHexa + yHexa + "(4);");
									data_output.writeBytes("\n\ttxLocal(N" + xHexa + yHexa + ")  	  <=  txN" + xHexa + yHexa + "(4);");
									data_output.writeBytes("\n\tdata_outLocal(N" + xHexa + yHexa + ")  <=  data_outN" + xHexa + yHexa + "(4);");
									data_output.writeBytes("\n\tcredit_oLocal(N" + xHexa + yHexa + ")  <=  credit_oN" + xHexa + yHexa + "(4);\n\n");
								}
							}
						}
						else
						{
							change_parameter = change_parameter.concat(word);
							data_output.writeBytes(change_parameter);
						}
					}	
					data_output.writeBytes("\r\n");
					n_lines++;
					line=buff.readLine();
				}
				buff.close();
				data_output.close();
				inFile.close();
			}//end try
			catch(FileNotFoundException f)
			{
				JOptionPane.showMessageDialog(null,"Can't write "  + " file\n" + f.getMessage(),"Output error", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
			catch(Exception e)
			{
				JOptionPane.showMessageDialog(null,e.getMessage(),"Error message", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
		}
		
		public String dotremove(String s) /* Método que procura por ponto (.) ou virgula (,) em um nome e substitui pelo caracter "d" */
		{
			String f = new String();
			
			for(int tmp=0;tmp < s.length();tmp++)
			{
				// Quando for igual a "," substituir por "v"
				if((int)s.charAt(tmp) == 44) f = f + "v";
				// Quando for igual a "." substituir por "p"
				else if((int)s.charAt(tmp) == 46) f = f + "p";
				else f = f + s.charAt(tmp);
			}
			
			return(f);
		}
		
		public void generatetopNoC(int typeApp) 
		{
			FileInputStream inFile=null;
			BufferedReader buff=null;
			
			StringTokenizer st;
			String addrX,addrY,addrXHexa,addrYHexa;
			String line, word, change_parameter;
			DataOutputStream data_output=null;
			double reset=0;
			try
			{
				if(typeApp == 0)
				{
					inFile = new FileInputStream(new File(sourceDir + "topNoC.vhd"));
					buff=new BufferedReader(new InputStreamReader(inFile));	    
					data_output=new DataOutputStream(new FileOutputStream(projectDir + "topNoC.vhd"));
				}
				if(typeApp == 1)
				{
					inFile = new FileInputStream(new File(sourceDir + "CDCG_topNoC.vhd"));
					buff=new BufferedReader(new InputStreamReader(inFile));	    
					data_output=new DataOutputStream(new FileOutputStream(projectDir + "CDCG_topNoC.vhd"));
				}
				int n_lines=0;
				change_parameter="";
				line=buff.readLine();
				while(line!=null)
				{
					st = new StringTokenizer(line, "$");
					int vem = st.countTokens();
					for (int cont=0; cont<vem; cont++)
					{
						word = st.nextToken();
						change_parameter="";
						if(word.equalsIgnoreCase("reset"))
						{
							
							/* Calculo do reset */
							// R1 - Procurar pela lista de AvailableClocks
							// R2 - Pesquisar na NoC pelos Clocks cadastrados
							// R3 - Encontrar o menor valor de clock
							
							double sum=0,value=0;
							int n=0;
							
							// Procurar todos os clocks cadastrados.
							for(int tmp=0;tmp < p.getNoC().getClock().size();tmp++)
							{
								// Router
								value = 1000/p.getNoC().getClock().get(tmp).getClockRouter();
								if(tmp == 0) sum = value;
								value = 1000/p.getNoC().getClock().get(tmp).getClockIpInput();
								if(sum > value) sum = value;
								value = 1000/p.getNoC().getClock().get(tmp).getClockIpOutput();
								if(sum > value) sum = value;
							}
							reset = sum;
							data_output.writeBytes("\treset <= '1','0' after " + sum + " ns;\n\n");														
						}
						else if(word.equalsIgnoreCase("clock_names"))
						{
							
							data_output.writeBytes("\n\tsignal fifo_timming_t		: std_logic := '1';");
							data_output.writeBytes("\n\tsignal fifo_timming_f		: std_logic := '0';");
                            int p3=0,p4=0,p5=0,tmp=0;
							String saida = "";
							for(int i=0;i<p.getNoC().getNumRotX() *  p.getNoC().getNumRotY();i++)
							{
								saida = generateNameRouter(i);
								tmp=0;
								for(int j=0;j<saida.length();j++)
								{
									if(saida.charAt(j) == '0') tmp++;
								}
								if(tmp == 2) { p3++; }
								else if(tmp == 1) { p4++; }
								else if(tmp == 0) { p5++; }
							}
							
							if(p3 > 0) data_output.writeBytes("\n\tsignal noc_data3		:noc_buf_3;");
							if(p4 > 0) data_output.writeBytes("\n\tsignal noc_data4		:noc_buf_4;");
							if(p5 > 0) data_output.writeBytes("\n\tsignal noc_data5		:noc_buf_5;");
							
							int count=0;
							for(tmp=0;tmp < p.getNoC().getClock().size();tmp++)
							{
								if(p.getNoC().getClock().get(tmp).getClockRouter() != p.getNoC().getClock().get(tmp).getClockIpOutput())
									count++;
				
							}
							if(count == 1)
							{
								data_output.writeBytes("\n\tsignal idle_fifo		: std_logic;");
								data_output.writeBytes("\n\tsignal cond2		: std_logic := '1';");
								
							}
							else if(count > 1)
							{
								count--;
								data_output.writeBytes("\n\tsignal idle_fifo		: std_logic_vector(" + count + " downto 0);");
								data_output.writeBytes("\n\tsignal cond2			: std_logic_vector(" + count + " downto 0) := (others=>'1');");
								
							}
							
							// Caso a fila de saída voltar a ser utilizada, descomentar esta linha
							// P1
							// 
							
							data_output.writeBytes("\n\tsignal idle			    : std_logic_vector(" + ((p.getNoC().getNumRotX() *  p.getNoC().getNumRotY())-1) + " downto 0);");
							data_output.writeBytes("\n\tsignal finish 			: std_logic_vector(" + ((p.getNoC().getNumRotX() *  p.getNoC().getNumRotY())-1) + " downto 0);");
							
							data_output.writeBytes("\n\tsignal sim	 		    : std_logic := '0';"); 	
							data_output.writeBytes("\n\tsignal cont 			: integer := 0;");
							  
							data_output.writeBytes("\n\tsignal cond5			: std_logic_vector (4 downto 0) := (others=>'1');"); 	
							data_output.writeBytes("\n\tsignal cond4			: std_logic_vector (3 downto 0) := (others=>'1');"); 	
							data_output.writeBytes("\n\tsignal cond3			: std_logic_vector (2 downto 0) := (others=>'1');"); 	
							data_output.writeBytes("\n\tsignal cond0			: std_logic_vector(" + ((p.getNoC().getNumRotX() *  p.getNoC().getNumRotY())-1) + " downto 0) := (others=>'0');");
							
							data_output.writeBytes("\n\tsignal cond1			: std_logic_vector(" + ((p.getNoC().getNumRotX() *  p.getNoC().getNumRotY())-1) + " downto 0) := (others=>'1');");
														
							for(int x=0;x<p.getNoC().getRefClockList().size();x++)
							{
								for(int i=0;i < dimX*dimY;i++)
								{
									if(p.getNoC().getRefClockList().get(x).getAllAvailableValue().equals(p.getNoC().getClock().get(i).getRouter()))
									{
										data_output.writeBytes("\n\tsignal " + this.dotremove(p.getNoC().getClock().get(i).getLabelClockRouter()) + " \t\t: std_logic;");
										break;
									}
									else if(p.getNoC().getRefClockList().get(x).getAllAvailableValue().equals(p.getNoC().getClock().get(i).getIpInput()))
									{
										data_output.writeBytes("\n\tsignal " + this.dotremove(p.getNoC().getClock().get(i).getLabelClockIpInput()) + "\t\t: std_logic;");
										break;
									}
									else if(p.getNoC().getRefClockList().get(x).getAllAvailableValue().equals(p.getNoC().getClock().get(i).getIpOutput()))
									{
										data_output.writeBytes("\n\tsignal " + this.dotremove(p.getNoC().getClock().get(i).getLabelClockIpOutput()) + "\t\t: std_logic;");
										break;
									}
								}
							}
                            String xHexa,yHexa;
							// Sinais para Fifo Output Module (Podem ser removidos)
							// P2
							int con=0;
							for(int x=0;x < dimX;x++)
							{
								xHexa = Convert.decToHex(x,(p.getNoC().getFlitSize()/8));
								for(int y=0;y < dimY;y++,con++)
								{
									yHexa = Convert.decToHex(y,(p.getNoC().getFlitSize()/8));
									if(p.getNoC().getClock().get(con).getClockRouter() != p.getNoC().getClock().get(con).getClockIpOutput())
									{
										int flit = p.getNoC().getFlitSize()-1;
										data_output.writeBytes("\n\tsignal s_data" + xHexa + yHexa + "\t\t\t\t: std_logic_vector(" +  flit + " downto 0);\n");
										data_output.writeBytes("\tsignal s_RX" + xHexa + yHexa + ",s_full" + xHexa + yHexa + "\t\t: std_logic;");
										data_output.writeBytes("\n\tsignal s_credit" + xHexa + yHexa + ",s_empty" + xHexa + yHexa + "\t: std_logic;");
									}
								}
							}
							
                            int	 num=0;
							for(int x=0;x < dimX;x++)
							{
								xHexa = Convert.decToHex(x,1);
								for(int y=0;y < dimY;y++,num++)
								{
									yHexa = Convert.decToHex(y,1);
									
									data_output.writeBytes("\n\tsignal Rot" + num + "\t\t\t: std_logic_vector(" + "7 downto 0) := " + "\"" + Convert.decToBin(num,8) + "\"" + ";");
								}
							}
						}
						else if(word.equalsIgnoreCase("input_module"))
						{
							int con = 0;
							String xHexa,yHexa;
							
							for(int x=0;x < dimX;x++)
							{
								xHexa = Convert.decToHex(x,(p.getNoC().getFlitSize()/8));
								for(int y=0;y < dimY;y++,con++)
								{
									yHexa = Convert.decToHex(y,(p.getNoC().getFlitSize()/8));
									data_output.writeBytes("\n\tcim" + Integer.toHexString(x) + Integer.toHexString(y) + " : Entity work.inputmodule");
									data_output.writeBytes("\n\tport map(");
									data_output.writeBytes("\n\t\tclock      => " + this.dotremove(p.getNoC().getClock().get(con).getLabelClockIpInput()) + ",");
                                    data_output.writeBytes("\n\t\trotID      => Rot" + (y * dimX + x) + ",");
									data_output.writeBytes("\n\t\treset      => reset,");
									data_output.writeBytes("\n\t\toutclock   => clock_rx(N" + xHexa + yHexa + "),");
									data_output.writeBytes("\n\t\touttx      => rx(N" + xHexa + yHexa + "),");
									data_output.writeBytes("\n\t\toutdata    => data_in(N" + xHexa + yHexa + "),");
									data_output.writeBytes("\n\t\tfinish     => finish(" + con + "),");
									data_output.writeBytes("\n\t\tincredit   => credit_o(N" + xHexa + yHexa + ")\n\t);\n");
								}
							}
						}
						else if(word.equalsIgnoreCase("output_module"))
						{
							int con = 0,con1=0;
							String xHexa,yHexa;
							
							// Port Map da entidade Fifo
							cont=0;
							for(int tmp=0;tmp <  p.getNoC().getClock().size();tmp++)
							{
								if(p.getNoC().getClock().get(tmp).getClockRouter() != p.getNoC().getClock().get(tmp).getClockIpOutput())
								{
									cont++;
								}
							}
							con=0;
							for(int x=0;x < dimX;x++)
							{
								xHexa = Convert.decToHex(x,(p.getNoC().getFlitSize()/8));
								for(int y=0;y < dimY;y++,con++)
								{
									yHexa = Convert.decToHex(y,(p.getNoC().getFlitSize()/8));
									
									if(p.getNoC().getClock().get(con).getClockRouter() != p.getNoC().getClock().get(con).getClockIpOutput())
                                    {
										data_output.writeBytes("\n\tfifo" + Integer.toHexString(x) + Integer.toHexString(y) + ": entity work.HermesG_Fifo_Output");
										data_output.writeBytes("\n\tport map(");
										data_output.writeBytes("\n\t\tclock_rx => clock_tx(N" + xHexa + yHexa + "),");
										data_output.writeBytes("\n\t\tclock =>" + this.dotremove(p.getNoC().getClock().get(con).getLabelClockIpOutput()) + ",");
										data_output.writeBytes("\n\t\treset   => reset,");
										data_output.writeBytes("\n\t\ttx     => s_RX" + xHexa + yHexa + ",");
										data_output.writeBytes("\n\t\trx     => tx(N" + xHexa + yHexa + "),");
										data_output.writeBytes("\n\t\tdata_in      => data_out(N" + xHexa + yHexa + "),");
										data_output.writeBytes("\n\t\tdata      => s_data" + xHexa + yHexa + ",");
										if(cont == 1) data_output.writeBytes("\n\t\tidle_fifo  => idle_fifo,");
										else data_output.writeBytes("\n\t\tidle_fifo  => idle_fifo(" + con1 + "),");
										data_output.writeBytes("\n\t\tcredit_r  => s_credit" + xHexa + yHexa + ",");
										data_output.writeBytes("\n\t\tcredit_o   => credit_i(N" + xHexa + yHexa + ")" + "\n\t);\n");
										con1++;
									}
								}
							}
							con=0;
							
							// Port map das entidades Output, tanto ligadas nas portas locais dos roteadores como nas filas de saída
							for(int x=0;x < dimX;x++)
							{
								xHexa = Convert.decToHex(x,(p.getNoC().getFlitSize()/8));
								for(int y=0;y < dimY;y++,con++)
								{
									yHexa = Convert.decToHex(y,(p.getNoC().getFlitSize()/8));
									// Se clock do Output IP for diferente do roteador, interliga ele como uma Fifo Output
									if(p.getNoC().getClock().get(con).getClockRouter() != p.getNoC().getClock().get(con).getClockIpOutput())
									{
										data_output.writeBytes("\n\tcom" + Integer.toHexString(x) + Integer.toHexString(y) + ": Entity work.outputmodule");
										data_output.writeBytes("\n\tport map(");	
										data_output.writeBytes("\n\t\tclock => " + this.dotremove(p.getNoC().getClock().get(con).getLabelClockIpOutput()) + ",");
										data_output.writeBytes("\n\t\treset => reset,");
										data_output.writeBytes("\n\t\toutcredit       => s_RX" + xHexa + yHexa + ",");
										data_output.writeBytes("\n\t\tindata     => s_data" + xHexa + yHexa + ",");
										data_output.writeBytes("\n\t\tintx  => s_credit" + xHexa + yHexa + ",");
										data_output.writeBytes("\n\t\tidle      => idle(" + con + "),");
										data_output.writeBytes("\n\t\tfifo_signal       => fifo_timming_t,");
										data_output.writeBytes("\n\t\trotID      => Rot" + (y * dimX + x) + "\n\t);\n");
									}
									// Se clock do Output IP for igual ao clock do roteador, interliga ele diretamente ao roteador
									else
									{
										data_output.writeBytes("\n\tcom" + Integer.toHexString(x) + Integer.toHexString(y) + ": Entity work.outputmodule");
										data_output.writeBytes("\n\tport map(");	
										data_output.writeBytes("\n\t\tclock      => " + this.dotremove(p.getNoC().getClock().get(con).getLabelClockIpOutput()) + ",");
										data_output.writeBytes("\n\t\treset      => reset,");
										data_output.writeBytes("\n\t\toutcredit  => credit_i(N" + xHexa + yHexa + "),");
										data_output.writeBytes("\n\t\tindata     => data_out(N" + xHexa + yHexa + "),");
										data_output.writeBytes("\n\t\tintx       => tx(N" + xHexa + yHexa + ")" + ",");
										data_output.writeBytes("\n\t\tidle       => idle(" + con + "),");
										data_output.writeBytes("\n\t\tfifo_signal       => fifo_timming_f,");
										data_output.writeBytes("\n\t\trotID      => Rot" + (y * dimX + x) + "\n\t);\n");
									}	
								}
							}
						}
						else if(word.equalsIgnoreCase("clock_list"))
						{
							double tempo = 0;
							long tempo1 = 0;
							int b=0;
							String saida = "";
							for(int x=0;x<p.getNoC().getRefClockList().size();x++)
							{
								for(int i=0;i < dimX*dimY;i++)
								{
									NumberFormat formatter = new DecimalFormat("###.#########################");  
									
									if(p.getNoC().getRefClockList().get(x).getAllAvailableValue().equals(p.getNoC().getClock().get(i).getRouter()))
									{
										tempo = ((1/p.getNoC().getClock().get(i).getClockRouter()) * 1000);
										data_output.writeBytes("\tprocess\n\tbegin\n");
										// data_output.writeBytes("\t\tif(reset = '1') then\n");
										// data_output.writeBytes("\t\t\twait for " + reset + " ns;\n");
										// data_output.writeBytes("\t\tend if;\n");
										data_output.writeBytes("\t\t" + this.dotremove(p.getNoC().getClock().get(i).getLabelClockRouter()) + "  <=  '1', '0' after " + formatter.format(tempo/2) + " ns;\n");
										data_output.writeBytes("\t\twait for " + formatter.format(tempo) + " ns;\n\tend process;\n\n"); 
										
										break;
									}
									else if(p.getNoC().getRefClockList().get(x).getAllAvailableValue().equals(p.getNoC().getClock().get(i).getIpInput()))
									{
										tempo = ((1/p.getNoC().getClock().get(i).getClockIpInput()) * 1000);
										data_output.writeBytes("\tprocess\n\tbegin\n");
										// data_output.writeBytes("\t\tif(reset = '1') then\n");
										// data_output.writeBytes("\t\t\twait for " + reset + " ns;\n");
										// data_output.writeBytes("\t\tend if;\n");
										data_output.writeBytes("\t\t" + this.dotremove(p.getNoC().getClock().get(i).getLabelClockIpInput()) + "  <=  '1', '0' after " + formatter.format(tempo/2) + " ns;\n");
										data_output.writeBytes("\t\twait for " + formatter.format(tempo) + " ns;\n\tend process;\n\n"); 
										
										break;
									}
									else if(p.getNoC().getRefClockList().get(x).getAllAvailableValue().equals(p.getNoC().getClock().get(i).getIpOutput()))
									{
										tempo = ((1/p.getNoC().getClock().get(i).getClockIpOutput()) * 1000);
										data_output.writeBytes("\tprocess\n\tbegin\n");
										// data_output.writeBytes("\t\tif(reset = '1') then\n");
										// data_output.writeBytes("\t\t\twait for " + reset + " ns;\n");
										// data_output.writeBytes("\t\tend if;\n");
										data_output.writeBytes("\t\t" + this.dotremove(p.getNoC().getClock().get(i).getLabelClockIpOutput()) + "  <=  '1', '0' after " + formatter.format(tempo/2) + " ns;\n");
										data_output.writeBytes("\t\twait for " + formatter.format(tempo) + " ns;\n\tend process;\n\n"); 
										
										break;
									}
								}
							}
						}	
						else if(word.equalsIgnoreCase("clock_for_noc"))
						{
							for(int x=0;x<p.getNoC().getRefClockList().size();x++)
							{
								for(int i=0;i < dimX*dimY;i++)
								{
									if(p.getNoC().getRefClockList().get(x).getAllAvailableValue().equals(p.getNoC().getClock().get(i).getRouter()))
									{
										data_output.writeBytes("\n\t\t" + this.dotremove(p.getNoC().getClock().get(i).getLabelClockRouter()) + "        =>  " + this.dotremove(p.getNoC().getClock().get(i).getLabelClockRouter()) + ",");
										break;
									}
								}
							}
                           
							int p3=0,p4=0,p5=0,tmp=0;
							for(int i=0;i<p.getNoC().getNumRotX() *  p.getNoC().getNumRotY();i++)
							{
								String saida = generateNameRouter(i);
								tmp=0;
								for(int j=0;j<saida.length();j++)
								{
									if(saida.charAt(j) == '0') tmp++;
								}
								if(tmp == 2) { p3++; }
								if(tmp == 1) { p4++; }
								if(tmp == 0) { p5++; }
							}
							if(p3 > 0) data_output.writeBytes("\n\t\tnoc_buf3      => noc_data3,");
							if(p4 > 0) data_output.writeBytes("\n\t\tnoc_buf4      => noc_data4,");
							if(p5 > 0) data_output.writeBytes("\n\t\tnoc_buf5      => noc_data5,");
						}
						else if(word.equalsIgnoreCase("end_sim"))
						{
							
							// Caso Fifo_Output não for usado em nenhum Output IP, sinal idle_fifo deve ser removido da lista de sensibilidade do process
							
							int count=0;
							for(int tmp=0;tmp < p.getNoC().getClock().size();tmp++)
							{
								if(p.getNoC().getClock().get(tmp).getClockRouter() != p.getNoC().getClock().get(tmp).getClockIpOutput())
									count++;
				
							}
							if(count > 0) data_output.writeBytes("\tprocess(finish,idle,idle_fifo");
							else data_output.writeBytes("\tprocess(finish,idle");
							
							int p3=0,p4=0,p5=0,tmp=0;
							for(int i=0;i<p.getNoC().getNumRotX() *  p.getNoC().getNumRotY();i++)
							{
								String saida = generateNameRouter(i);
								tmp=0;
								for(int j=0;j<saida.length();j++)
								{
									if(saida.charAt(j) == '0') tmp++;
								}
								if(tmp == 2) { p3++; }
								else if(tmp == 1) { p4++; }
								else if(tmp == 0) { p5++; }
							}
							
							if(p3 > 0) data_output.writeBytes(",noc_data3");
							if(p4 > 0) data_output.writeBytes(",noc_data4");
							if(p5 > 0) data_output.writeBytes(",noc_data5");

							// Bloco de código que calcula o clock mais lento 
							// * Procurar clocks dos Roteadores, Ips Input e Ips Output, converter para clock absoluto e armazenar em um double.
							// * O nome do menor clock deve ser armazenado, ele será utilizado posteriormente.
							
							double time=0;
							
							String name = new String("");
							for(tmp=0;tmp < p.getNoC().getClock().size();tmp++)
							{
							
								if(tmp == 0)
								{
									time = 1000/p.getNoC().getClock().get(tmp).getClockRouter();
									name = p.getNoC().getClock().get(tmp).getLabelClockRouter();
								}
								if(1000/p.getNoC().getClock().get(tmp).getClockRouter() >= time) 
								{
									time = 1000/p.getNoC().getClock().get(tmp).getClockRouter(); 								
									name = p.getNoC().getClock().get(tmp).getLabelClockRouter();
								}
								if(1000/p.getNoC().getClock().get(tmp).getClockIpInput() >= time)
								{ 
									time = 1000/p.getNoC().getClock().get(tmp).getClockIpInput();
									name = p.getNoC().getClock().get(tmp).getLabelClockIpInput();
								}
								if(1000/p.getNoC().getClock().get(tmp).getClockIpOutput() >= time) 
								{
									time = 1000/p.getNoC().getClock().get(tmp).getClockIpOutput();
									name = p.getNoC().getClock().get(tmp).getLabelClockIpOutput();
								}
							}	
							
							data_output.writeBytes("," + this.dotremove(name) + ")");
							
							data_output.writeBytes("\n\tbegin");
							data_output.writeBytes("\n\t\tif(finish = cond1) then -- Condicao que todos os IPs injetaram trafego");
							
							// (IF) Se não existir entidades Fifo_Output "Condição em que todos os Output IPs possuirem clock mesmo clock do rotedor ao qual esta interconectado idle_fifo deve ser desconsiderado da condição "
							// (ELSE) Caso contrario deve ser considerado
							
							if(count == 0) data_output.writeBytes("\n\t\tif((idle = cond0)) then");
							else data_output.writeBytes("\n\t\tif((idle = cond0) and idle_fifo = cond2) then");
							
							// Saber quem são noc_data3 noc_data4 noc_data5
							p3=0;p4=0;p5=0;tmp=0;
							data_output.writeBytes("\n\t\t\tif((");
							for(int i=0;i<p.getNoC().getNumRotX() *  p.getNoC().getNumRotY();i++)
							{
								String saida = generateNameRouter(i);
								tmp=0;
								for(int j=0;j<saida.length();j++)
								{
									if(saida.charAt(j) == '0') tmp++;
								}
								// noc_data3
								if(tmp == 2 && p3 == 0) { data_output.writeBytes("(noc_data3(" + p3 + ") = cond3) and "); p3++; continue; } 
								if(tmp == 1 && p4 == 0) { data_output.writeBytes("(noc_data4(" + p4 + ") = cond4) and "); p4++; continue; } 
								if(tmp == 0 && p5 == 0) { data_output.writeBytes("(noc_data5(" + p5 + ") = cond5) and "); p5++; continue; } 
								
								if(i == (p.getNoC().getNumRotX() *  p.getNoC().getNumRotY())-1)
								{
									if(tmp == 2) { data_output.writeBytes(" (noc_data3(" + p3 + ") = cond3) "); p3++; } 
									if(tmp == 1) { data_output.writeBytes(" (noc_data4(" + p4 + ") = cond4) "); p4++; }
									if(tmp == 0) { data_output.writeBytes(" (noc_data5(" + p5 + ") = cond5) "); p5++; }
								}
								else
								{
									if(tmp == 2) { data_output.writeBytes(" (noc_data3(" + p3 + ") = cond3) and "); p3++; } 
									if(tmp == 1) { data_output.writeBytes(" (noc_data4(" + p4 + ") = cond4) and "); p4++; }
									if(tmp == 0) { data_output.writeBytes(" (noc_data5(" + p5 + ") = cond5) and "); p5++; }
								}
							}
							data_output.writeBytes(")) then");
							data_output.writeBytes("\n\t\t\t\tcont <= cont + 1;");
							data_output.writeBytes("\n\t\t\t\tif(cont = 128) then");
							data_output.writeBytes("\n\t\t\t\t\tsim <= '1';");
							data_output.writeBytes("\n\t\t\t\tend if;");
							data_output.writeBytes("\n\t\t\tend if;");
							data_output.writeBytes("\n\t\tend if;");
							data_output.writeBytes("\n\t\tend if;");
							data_output.writeBytes("\n\tend process;");
						}
						else
						{
							change_parameter = change_parameter.concat(word);
							data_output.writeBytes(change_parameter);
						}
					}
					data_output.writeBytes("\r\n");
					n_lines++;
					line=buff.readLine();
				} //end while
				buff.close();
				data_output.close();
				inFile.close();
				}//end try
				catch(FileNotFoundException f)
				{
					JOptionPane.showMessageDialog(null,"Can't write " + " file\n" + f.getMessage(),"Output error", JOptionPane.ERROR_MESSAGE);
					System.exit(0);
				}
				catch(Exception e)
				{
					JOptionPane.showMessageDialog(null,e.getMessage(),"Error message", JOptionPane.ERROR_MESSAGE);
					System.exit(0);
				}
		}
		public void generateSystemCIntputModule(int typeApp)
		{
			FileInputStream inFile=null;
			DataOutputStream data_output=null;
			BufferedReader buff=null;
			try
			{
				if(typeApp == 0)
				{
					inFile = new FileInputStream(new File(sourceDir + "SC_InputModule.h"));
					buff=new BufferedReader(new InputStreamReader(inFile));
					data_output=new DataOutputStream(new FileOutputStream(scDir + "SC_InputModule.h"));
				}
				if(typeApp == 1)
				{
					inFile = new FileInputStream(new File(sourceDir + "SC_CDCG_InputModule.h"));
					buff=new BufferedReader(new InputStreamReader(inFile));
					data_output=new DataOutputStream(new FileOutputStream(scDir + "SC_CDCG_InputModule.h"));
				}
				int n_lines=0;
				change_parameter="";
				line=buff.readLine();
				while(line!=null)
				{
					st = new StringTokenizer(line, "$");
					int vem = st.countTokens();
					for (int cont=0; cont<vem; cont++)
					{
						word = st.nextToken();
						change_parameter="";
						if(word.equalsIgnoreCase("tam_flit"))
						{
							word = flitWidth;
							change_parameter = change_parameter.concat(word);
							data_output.writeBytes(change_parameter);	
						}
						else if(word.equalsIgnoreCase("max_router"))
						{
							word = ""+(dimX * dimY);
							change_parameter = change_parameter.concat(word);
							data_output.writeBytes(change_parameter);
						}
						else if(word.equalsIgnoreCase("max_x"))
						{
							word = ""+(dimX);
							change_parameter = change_parameter.concat(word);
							data_output.writeBytes(change_parameter);
						}
						else if(word.equalsIgnoreCase("max_y"))
						{
							word = ""+(dimY);
							change_parameter = change_parameter.concat(word);
							data_output.writeBytes(change_parameter);
						}
						else if(word.equalsIgnoreCase("out_pin"))
						{
							word = ""+("\tsc_out<sc_logic> finish;");
							change_parameter = change_parameter.concat(word);
							data_output.writeBytes(change_parameter);
						}
						else if(word.equalsIgnoreCase("out_ini"))
						{
							word = ""+("\tfinish = SC_LOGIC_0;");
							change_parameter = change_parameter.concat(word);
							data_output.writeBytes(change_parameter);
						}
						else if(word.equalsIgnoreCase("out_end_0"))
						{
							word = ""+("\telse finish = SC_LOGIC_1;");
							change_parameter = change_parameter.concat(word);
							data_output.writeBytes(change_parameter);
						}
						else if(word.equalsIgnoreCase("out_end_1"))
						{
							word = ""+("\tfinish = SC_LOGIC_1;");
							change_parameter = change_parameter.concat(word);
							data_output.writeBytes(change_parameter);
						}
						else if(word.equalsIgnoreCase("logic_app_var"))
						{
							for(int i=0;i < p.getCost().size();i++)
							{
								word = "int var"+i+";\n";
								change_parameter = change_parameter.concat(word);
							}
							data_output.writeBytes(change_parameter);
						}
						else if(word.equalsIgnoreCase("logic_app_ini"))
						{
							System.out.println("Passei aqui2");
							for(int i=0;i < p.getMapping().size();i++)
							{
								//p.
							}
						}
						else if(word.equalsIgnoreCase("logic_app_end"))
						{
							//NoC noc = project.getNoC();
							for(int i=0;i < p.getDependance().size();i++)
							{
								//p.
							}
						}
						else
						{
							change_parameter = change_parameter.concat(word);
							data_output.writeBytes(change_parameter);
						}
					}	
					data_output.writeBytes("\r\n");
					n_lines++;
					line=buff.readLine();
				}
				buff.close();
				data_output.close();
				inFile.close();
			}//end try
			catch(FileNotFoundException f)
			{
				JOptionPane.showMessageDialog(null,"Can't write "  + " file\n" + f.getMessage(),"Output error", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
			catch(Exception e)
			{
				JOptionPane.showMessageDialog(null,e.getMessage(),"Error message", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
		}
		public void generateSystemCOutputModule(int typeApp)
		{
			FileInputStream inFile=null;
			DataOutputStream data_output=null;
			BufferedReader buff=null;
			try
			{
				if(typeApp == 0)
				{
					inFile = new FileInputStream(new File(sourceDir + "SC_OutputModule.h"));
					buff=new BufferedReader(new InputStreamReader(inFile));
					data_output=new DataOutputStream(new FileOutputStream(scDir + "SC_OutputModule.h"));
				}
				if(typeApp == 1)
				{
					inFile = new FileInputStream(new File(sourceDir + "SC_CDCG_OutputModule.h"));
					buff=new BufferedReader(new InputStreamReader(inFile));
					data_output=new DataOutputStream(new FileOutputStream(scDir + "SC_CDCG_OutputModule.h"));
				}
				int n_lines=0;
				change_parameter="";
				line=buff.readLine();
				while(line!=null)
				{
					st = new StringTokenizer(line, "$");
					int vem = st.countTokens();
					for (int cont=0; cont<vem; cont++)
					{
						word = st.nextToken();
						change_parameter="";
						if(word.equalsIgnoreCase("tam_flit"))
						{
							word = flitWidth;
							change_parameter = change_parameter.concat(word);
							data_output.writeBytes(change_parameter);	
						}
						else if(word.equalsIgnoreCase("max_router"))
						{
							word = ""+(dimX * dimY);
							change_parameter = change_parameter.concat(word);
							data_output.writeBytes(change_parameter);
						}
						else if(word.equalsIgnoreCase("idle_pin"))
						{
							word = ""+"\tsc_out<sc_logic> idle;";
							change_parameter = change_parameter.concat(word);
							data_output.writeBytes(change_parameter);    
						}
						else if(word.equalsIgnoreCase("idle_start"))
						{
							word = ""+"\tidle = SC_LOGIC_0;";
							change_parameter = change_parameter.concat(word);
							data_output.writeBytes(change_parameter);
						}
						else if(word.equalsIgnoreCase("idle_state_0"))
						{
							word = ""+"\tidle = SC_LOGIC_0;";
							change_parameter = change_parameter.concat(word);
							data_output.writeBytes(change_parameter);
						}
						else if(word.equalsIgnoreCase("idle_state_1"))
						{
							word = ""+"\tidle = SC_LOGIC_1;";
							change_parameter = change_parameter.concat(word);
							data_output.writeBytes(change_parameter);
						}
						else if(word.equalsIgnoreCase("fifo_out_cond"))
						{
							// Condição para decrementar tempo da Fifo Output diretamente no SystemC
							/*
							word = ""+"if(fifo_signal == SC_LOGIC_1)\n";
							word = word+"\t\t\t\t\t{\n";
							word = word+"\t\t\t\t\t\tTCDPF = TCDPF - 5;\n";
							word = word+"\t\t\t\t\t\tTCDUF = TCDUF - 5;\n";
							word = word+"\t\t\t\t\t}\n";
							change_parameter = change_parameter.concat(word);
							data_output.writeBytes(change_parameter);
							*/
						}
						else
						{
							change_parameter = change_parameter.concat(word);
							data_output.writeBytes(change_parameter);
						}
					}	
					data_output.writeBytes("\r\n");
					n_lines++;
					line=buff.readLine();
				}
				buff.close();
				data_output.close();
				inFile.close();
			}//end try
			catch(FileNotFoundException f)
			{
				JOptionPane.showMessageDialog(null,"Can't write "  + " file\n" + f.getMessage(),"Output error", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
			catch(Exception e)
			{
				JOptionPane.showMessageDialog(null,e.getMessage(),"Error message", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
		}
		public void generateSystemCOutputModuleRouter(int typeApp)
		{
			FileInputStream inFile=null;
			DataOutputStream data_output=null;
			BufferedReader buff=null;
			try
			{
				if(typeApp == 0)
				{
					inFile = new FileInputStream(new File(sourceDir + "SC_OutputModuleRouter.h"));
					buff=new BufferedReader(new InputStreamReader(inFile));
					data_output=new DataOutputStream(new FileOutputStream(scDir + "SC_OutputModuleRouter.h"));
				}
				if(typeApp == 1)
				{
					inFile = new FileInputStream(new File(sourceDir + "SC_CDCG_OutputModuleRouter.h"));
					buff=new BufferedReader(new InputStreamReader(inFile));
					data_output=new DataOutputStream(new FileOutputStream(scDir + "SC_CDCG_OutputModuleRouter.h"));
				}			
				int n_lines=0;
				change_parameter="";
				line=buff.readLine();
				while(line!=null)
				{
					st = new StringTokenizer(line, "$");
					int vem = st.countTokens();
					for (int cont=0; cont<vem; cont++)
					{
						word = st.nextToken();
						change_parameter="";
						if(word.equalsIgnoreCase("flit_size"))
						{
							word = ""+ flitWidth;
							change_parameter = change_parameter.concat(word);
							data_output.writeBytes(change_parameter);	
						}
						else if(word.equalsIgnoreCase("num_port"))
						{
							word = "5";
							change_parameter = change_parameter.concat(word);
							data_output.writeBytes(change_parameter);	
						}
						else if(word.equalsIgnoreCase("num_rot"))
						{
							word = ""+(dimX * dimY);
							change_parameter = change_parameter.concat(word);
							data_output.writeBytes(change_parameter);	
						}
						else if(word.equalsIgnoreCase("num_rot_x"))
						{
							word = "" + dimX;
							change_parameter = change_parameter.concat(word);
							data_output.writeBytes(change_parameter);	
						}
						else if(word.equalsIgnoreCase("num_rot_y"))
						{
							word = "" + dimY;
							change_parameter = change_parameter.concat(word);
							data_output.writeBytes(change_parameter);	
						}
						else if(word.equalsIgnoreCase("input_clock")) // Ok
						{
							for(int x=0;x<(dimX*dimY);x++)
							{
								data_output.writeBytes("\n\tsc_in<sc_logic> clk" + x + ";");
							}
						}
						else if(word.equalsIgnoreCase("input_port")) // Ok
						{
							int con=0,j=0;
							String word;
							for(int y=0;y<dimY;y++)
							{
								for(int x=0;x<dimX;x++,con++)
								{
									word = "" + x + y;
									for(j=0;j<dimX*dimY;j++) if(p.getNoC().getClock().get(j).getNumberRouter().equals(word)) break;
									String interfaces = p.getNoC().getClock().get(j).getRouterInterfaces();
									for(int i=0;i<4;i++)
									{
										if((int)interfaces.charAt(i) == 83 || (int)interfaces.charAt(i) == 65)
										{
											data_output.writeBytes("\n\tsc_in<sc_logic> credit_ir" + con + "p" + i + ";");
											data_output.writeBytes("\n\tsc_in<sc_logic> tx_r" + con + "p" + i + ";");
											data_output.writeBytes("\n\tsc_in<sc_lv<constFlitSize> > out_r" + con + "p" + i + ";");
										}
									}
								}
							}							
						}
						else if(word.equalsIgnoreCase("tx_router")) // Ok
						{
							int con=0,i=0,j=0;
							String word,interfaces;
							for(int y=0;y<dimY;y++)
							{
								for(int x=0;x<dimX;x++,con++)
								{
									if(con == 0) data_output.writeBytes("\n\t\tif (Roteador == " + con + ")\n\t\t{");
									else data_output.writeBytes("\n\t\telse if (Roteador == " + con + ")\n\t\t{");
									word = "" + x + y;
									for(j=0;j<dimX*dimY;j++) if(p.getNoC().getClock().get(j).getNumberRouter().equals(word)) break;
									interfaces = p.getNoC().getClock().get(j).getRouterInterfaces();
									for(i=0;i<4;i++)
									{
										if((int)interfaces.charAt(i) == 83 || (int)interfaces.charAt(i) == 65)
										{
											data_output.writeBytes("\n\t\t\tif(Porta == " + i + ") return (tx_r" + con + "p" + i + "==SC_LOGIC_1)? 1 : 0;\t");
										}
									}
									data_output.writeBytes("\n\t\t}");
								}
							}
						}
                        else if(word.equalsIgnoreCase("data_router"))
						{
							int con=0,i=0,j=0;
							String word,interfaces;
							for(int y=0;y<dimY;y++)
							{
								for(int x=0;x<dimX;x++,con++)
								{
									if(con == 0) data_output.writeBytes("\n\t\tif (Roteador == " + con + ")\n\t\t{");
									else data_output.writeBytes("\n\t\telse if (Roteador == " + con + ")\n\t\t{");
									word = "" + x + y;
									for(j=0;j<dimX*dimY;j++) if(p.getNoC().getClock().get(j).getNumberRouter().equals(word)) break;
									interfaces = p.getNoC().getClock().get(j).getRouterInterfaces();
									for(i=0;i<4;i++)
									{
										if((int)interfaces.charAt(i) == 83 || (int)interfaces.charAt(i) == 65)
										{
											data_output.writeBytes("\n\t\t\tif(Porta == " + i + ") return (out_r" + con + "p" + i + ".read().to_uint());\t");
										}
									}
									data_output.writeBytes("\n\t\t}");
								}
							}
						}
                        else if(word.equalsIgnoreCase("data_credit"))
						{
							int con=0,i=0,j=0;
							String word,interfaces;
							for(int y=0;y<dimY;y++)
							{
								for(int x=0;x<dimX;x++,con++)
								{
									if(con == 0) data_output.writeBytes("\n\t\tif (Roteador == " + con + ")\n\t\t{");
									else data_output.writeBytes("\n\t\telse if (Roteador == " + con + ")\n\t\t{");
									word = "" + x + y;
									for(j=0;j<dimX*dimY;j++) if(p.getNoC().getClock().get(j).getNumberRouter().equals(word)) break;
									interfaces = p.getNoC().getClock().get(j).getRouterInterfaces();
									for(i=0;i<4;i++)
									{
										if((int)interfaces.charAt(i) == 83 || (int)interfaces.charAt(i) == 65)
										{
											data_output.writeBytes("\n\t\t\tif(Porta == " + i + ") return (credit_ir" + con + "p" + i + "==SC_LOGIC_1)? 1 : 0;\t");
										}
									}
									data_output.writeBytes("\n\t\t}");
								}
							}
						}
                        else if(word.equalsIgnoreCase("cont_clock"))
						{
							for(int x=0;x<(dimX*dimY);x++)
							{
								data_output.writeBytes("\n\tunsigned long int CurrentTimeClk" + x + ";");
							}
						}

                        else if(word.equalsIgnoreCase("timer_clock"))
						{
							for(int x=0;x<(dimX*dimY);x++)
							{
								data_output.writeBytes("\n\tvoid inline TimerClk" + x + "();");
							}
							for(int x=0;x<(dimX*dimY);x++)
							{
								data_output.writeBytes("\n\tvoid inline TrafficWatcherRouter" + x + "();");
							}
						}
						
						else if(word.equalsIgnoreCase("mapping_port"))
						{
							int con=0,j=0;
							String word;
							for(int y=0;y<dimY;y++)
							{
								for(int x=0;x<dimX;x++,con++)
								{
									word = "" + x + y;
									for(j=0;j<dimX*dimY;j++) if(p.getNoC().getClock().get(j).getNumberRouter().equals(word)) break;
									String interfaces = p.getNoC().getClock().get(j).getRouterInterfaces();
									for(int i=0;i<4;i++)
									{
										if((int)interfaces.charAt(i) == 83 || (int)interfaces.charAt(i) == 65)
										{
											data_output.writeBytes("\n\tcredit_ir" + con + "p" + i + "(\"" + "credit_ir" + con + "p" + i + "\"),");
											data_output.writeBytes("\n\ttx_r" + con + "p" + i + "(\"tx_r" + con + "p" + i + "\"),");
											data_output.writeBytes("\n\tout_r" + con + "p" + i + "(\"out_r" + con + "p" + i + "\"),");
										}
									}
								}
							}
							int x=0;
							for(;x<(dimX*dimY)-1;x++)
							{
								data_output.writeBytes("\n\tclk" + x + "(\"" + "clk" + x + "\"),");
							}							
							data_output.writeBytes("\n\tclk" + x + "(\"" + "clk" + x + "\")\n\t{");
							for(x=0;x<(dimX*dimY);x++)
							{
								data_output.writeBytes("\n\t\tCurrentTimeClk" + x + "= 0;"); 
								data_output.writeBytes("\n\t\tSC_CTHREAD(TrafficWatcherRouter" + x + ",clk" + x + ".pos());");
								data_output.writeBytes("\n\t\tSC_METHOD(TimerClk" + x + ");");
								data_output.writeBytes("\n\t\tsensitive_pos << clk" + x + ";");
								data_output.writeBytes("\n\t\tdont_initialize();");
							}
                            data_output.writeBytes("\n\t\tCurrentRefTime = 0;");
							data_output.writeBytes("\n\t\tSC_METHOD(TimerRef_Clock);");
							data_output.writeBytes("\n\t\tsensitive_pos << ref_clock;");
							data_output.writeBytes("\n\t\tdont_initialize();");
							data_output.writeBytes("\n\t}");
						}
						else if(word.equalsIgnoreCase("func_timer_port"))
						{
							for(int x=0;x<(dimX*dimY);x++)
							{
								data_output.writeBytes("\nvoid inline outmodulerouter::TimerClk" + x + "()\n{"); 
								data_output.writeBytes("\n\t++CurrentTimeClk"  + x + ";\n}");
							}
						}
						else if(word.equalsIgnoreCase("decl_func_timer"))
						{
							for(int x=0;x<(dimX*dimY);x++)
							{
								data_output.writeBytes("\nvoid inline outmodulerouter::TrafficWatcherRouter" + x + "()\n{");
								data_output.writeBytes("\n\tchar temp[100];");
								data_output.writeBytes("\n\tFILE* Output[constNumRot][constNumPort];");
								data_output.writeBytes("\n\tunsigned long int cont[constNumRot][constNumPort];");
								data_output.writeBytes("\n\tunsigned long int size[constNumRot][constNumPort];");
								data_output.writeBytes("\n\tlong int currentFlit[constNumRot][constNumPort];");
								data_output.writeBytes("\n\tint rot, port;");
								data_output.writeBytes("\n\twhile(reset.read() != SC_LOGIC_0) wait(1,SC_NS);");
								data_output.writeBytes("\n\t\trot=" + x + ";");
								data_output.writeBytes("\n\t//roteador nao e o limite da direita, logo tem a porta EAST");
								data_output.writeBytes("\n\tif((rot%constNumRotX)!=(constNumRotX-1))\n\t\t{");
								data_output.writeBytes("\n\t\tsprintf(temp, \"r%dp0.txt\", rot);");
								data_output.writeBytes("\n\t\tOutput[rot][0] = fopen(temp, \"w\");");
								data_output.writeBytes("\n\t\tcont[rot][0] = 0;");
								data_output.writeBytes("\n\t}");
								data_output.writeBytes("\n\t\t//roteador nao e o limite da esquerda, logo tem a porta WEST");
								data_output.writeBytes("\n\tif((rot%constNumRotX)!=0)\n\t\t{");
								data_output.writeBytes("\n\t\tsprintf(temp, \"r%dp1.txt\", rot);");
								data_output.writeBytes("\n\t\tOutput[rot][1] = fopen(temp, \"w\");");
								data_output.writeBytes("\n\t\tcont[rot][1] = 0;");
								data_output.writeBytes("\n\t}");
								data_output.writeBytes("\n\t//roteador nao e o limite superior, logo tem a porta NORTH");
								data_output.writeBytes("\n\tif((rot/constNumRotX)!=(constNumRotY-1))\n\t\t{");
								data_output.writeBytes("\n\t\tsprintf(temp, \"r%dp2.txt\", rot);");
								data_output.writeBytes("\n\t\tOutput[rot][2] = fopen(temp, \"w\");");
								data_output.writeBytes("\n\t\tcont[rot][2] = 0;");
								data_output.writeBytes("\n\t}");
								data_output.writeBytes("\n\t//roteador nao e o limite inferior, logo tem a porta SOUTH");
								data_output.writeBytes("\n\tif((rot/constNumRotX)!=0)\n\t\t{");
								data_output.writeBytes("\n\t\tsprintf(temp, \"r%dp3.txt\", rot);");
								data_output.writeBytes("\n\t\tOutput[rot][3] = fopen(temp, \"w\");");
								data_output.writeBytes("\n\t\tcont[rot][3] = 0;");
								data_output.writeBytes("\n\t\n\t}");
								data_output.writeBytes("\n\twhile(true)");
								data_output.writeBytes("\n\t{");
								data_output.writeBytes("\n\t\trot=" + x + ";");
								data_output.writeBytes("\n\t\t\t//roteador nao e o limite da direita, logo tem a porta EAST");
								data_output.writeBytes("\n\t\tif((rot%constNumRotX)!=(constNumRotX-1))");
								data_output.writeBytes("\n\t\t{");
								data_output.writeBytes("\n\t\t\tif(inTx(rot,0) == 1 && inCredit(rot,0)==1)");
								data_output.writeBytes("\n\t\t\t{");
								data_output.writeBytes("\n\t\t\t\tcurrentFlit[rot][0] = inData(rot,0);");
								data_output.writeBytes("\n\t\t\t\tfprintf(Output[rot][0], \"(%0*X %u)\", (int)constFlitSize/4, currentFlit[rot][0],CurrentTimeClk"  + x + ");");
								data_output.writeBytes("\n\t\t\t\tcont[rot][0]++;");
								data_output.writeBytes("\n\t\t\t\tif(cont[rot][0] == 2)");
								data_output.writeBytes("\n\t\t\t\t\tsize[rot][0] = currentFlit[rot][0] + 2;");
								data_output.writeBytes("\n\t\t\t\tif(cont[rot][0]>2 && cont[rot][0]==size[rot][0])\n\t\t\t\t\t{");
								data_output.writeBytes("\n\t\t\t\t\tfprintf(Output[rot][0], \"\\n\");");
								data_output.writeBytes("\n\t\t\t\t\tcont[rot][0]=0;");
								data_output.writeBytes("\n\t\t\t\t\tsize[rot][0]=0;");
								data_output.writeBytes("\n\t\t\t\t}");
								data_output.writeBytes("\n\t\t\t}");
								data_output.writeBytes("\n\t\t}");
								
								data_output.writeBytes("\n\t\t//roteador nao e o limite da esquerda, logo tem a porta WEST");
								data_output.writeBytes("\n\t\tif((rot%constNumRotX)!=0)");
								data_output.writeBytes("\n\t\t{");
								data_output.writeBytes("\n\t\t\tif(inTx(rot,1) == 1 && inCredit(rot,1)==1)");
								data_output.writeBytes("\n\t\t\t{");
								data_output.writeBytes("\n\t\t\t\tcurrentFlit[rot][1] = inData(rot,1);");
								data_output.writeBytes("\n\t\t\t\tfprintf(Output[rot][1], \"(%0*X %u)\", (int)constFlitSize/4, currentFlit[rot][1],CurrentTimeClk"  + x + ");");
								data_output.writeBytes("\n\t\t\t\tcont[rot][1]++;");
								data_output.writeBytes("\n\t\t\t\tif(cont[rot][1] == 2)");
								data_output.writeBytes("\n\t\t\t\t\tsize[rot][1] = currentFlit[rot][1] + 2;");
								data_output.writeBytes("\n\t\t\t\tif(cont[rot][1]>2 && cont[rot][1]==size[rot][1])\n\t\t\t\t\t{");
								data_output.writeBytes("\n\t\t\t\t\tfprintf(Output[rot][1], \"\\n\");");
								data_output.writeBytes("\n\t\t\t\t\tcont[rot][1]=0;");
								data_output.writeBytes("\n\t\t\t\t\tsize[rot][1]=0;");
								data_output.writeBytes("\n\t\t\t\t}");
								data_output.writeBytes("\n\t\t\t}");
								data_output.writeBytes("\n\t\t}");
								
								data_output.writeBytes("\n\t\t//roteador nao e o limite superior, logo tem a porta NORTH");
								data_output.writeBytes("\n\t\tif((rot/constNumRotX)!=constNumRotY-1)");
								data_output.writeBytes("\n\t\t{");
								data_output.writeBytes("\n\t\t\tif(inTx(rot,2) == 1 && inCredit(rot,2)==1)");
								data_output.writeBytes("\n\t\t\t{");
								data_output.writeBytes("\n\t\t\t\tcurrentFlit[rot][2] = inData(rot,2);");
								data_output.writeBytes("\n\t\t\t\tfprintf(Output[rot][2], \"(%0*X %u)\", (int)constFlitSize/4, currentFlit[rot][2],CurrentTimeClk"  + x + ");");
								data_output.writeBytes("\n\t\t\t\tcont[rot][2]++;");
								data_output.writeBytes("\n\t\t\t\tif(cont[rot][2] == 2)");
								data_output.writeBytes("\n\t\t\t\t\tsize[rot][2] = currentFlit[rot][2] + 2;");
								data_output.writeBytes("\n\t\t\t\tif(cont[rot][2]>2 && cont[rot][2]==size[rot][2])\n\t\t\t\t\t{");
								data_output.writeBytes("\n\t\t\t\t\tfprintf(Output[rot][2], \"\\n\");");
								data_output.writeBytes("\n\t\t\t\t\tcont[rot][2]=0;");
								data_output.writeBytes("\n\t\t\t\t\tsize[rot][2]=0;");
								data_output.writeBytes("\n\t\t\t\t}");
								data_output.writeBytes("\n\t\t\t}");
								data_output.writeBytes("\n\t\t}");
								
								data_output.writeBytes("\n\t\t//roteador nao e o limite inferior, logo tem a porta SOUTH");
								data_output.writeBytes("\n\t\tif((rot/constNumRotX)!=0)");
								data_output.writeBytes("\n\t\t{");
								data_output.writeBytes("\n\t\t\tif(inTx(rot,3) == 1 && inCredit(rot,3)==1)");
								data_output.writeBytes("\n\t\t\t{");
								data_output.writeBytes("\n\t\t\t\tcurrentFlit[rot][3] = inData(rot,3);");
								data_output.writeBytes("\n\t\t\t\tfprintf(Output[rot][3], \"(%0*X %u)\", (int)constFlitSize/4, currentFlit[rot][3],CurrentTimeClk"  + x + ");");
								data_output.writeBytes("\n\t\t\t\tcont[rot][3]++;");
								data_output.writeBytes("\n\t\t\t\tif(cont[rot][3] == 2)");
								data_output.writeBytes("\n\t\t\t\t\tsize[rot][3] = currentFlit[rot][3] + 2;");
								data_output.writeBytes("\n\t\t\t\tif(cont[rot][3]>2 && cont[rot][3]==size[rot][3])\n\t\t\t\t\t{");
								data_output.writeBytes("\n\t\t\t\t\tfprintf(Output[rot][3], \"\\n\");");
								data_output.writeBytes("\n\t\t\t\t\tcont[rot][3]=0;");
								data_output.writeBytes("\n\t\t\t\t\tsize[rot][3]=0;");
								data_output.writeBytes("\n\t\t\t\t}");
								data_output.writeBytes("\n\t\t\t}");
								data_output.writeBytes("\n\t\t}\n\t\twait();\n\t}\n}");
							}
						}
						else
						{
							change_parameter = change_parameter.concat(word);
							data_output.writeBytes(change_parameter);
						}
					}	
					data_output.writeBytes("\r\n");
					n_lines++;
					line=buff.readLine();
				}
				buff.close();
				data_output.close();
				inFile.close();
			}//end try
			catch(FileNotFoundException f)
			{
				JOptionPane.showMessageDialog(null,"Can't write "  + " file\n" + f.getMessage(),"Output error", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
			catch(Exception e)
			{
				JOptionPane.showMessageDialog(null,e.getMessage(),"Error message", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
		}
		public void createPackageG(String nameFile)
		{
			StringTokenizer st;
			String addrX,addrY,addrXHexa,addrYHexa;
			String line, word, change_parameter;

			try
			{
				FileInputStream inFile = new FileInputStream(new File(sourceDir + nameFile));
				BufferedReader buff=new BufferedReader(new InputStreamReader(inFile));
				
				DataOutputStream data_output=new DataOutputStream(new FileOutputStream(nocDir + nameFile));
				
				int n_lines=0;
				change_parameter="";
				line=buff.readLine();
				while(line!=null)
				{
					st = new StringTokenizer(line, "$");
					int vem = st.countTokens();
					int p3=0,p4=0,p5=0,tmp=0;
					for(int i=0;i<p.getNoC().getNumRotX() *  p.getNoC().getNumRotY();i++)
					{
						String saida = generateNameRouter(i);
						tmp=0;
						for(int j=0;j<saida.length();j++)
						{
							if(saida.charAt(j) == '0') tmp++;
						}
						if(tmp == 2) { p3++; }
						else if(tmp == 1) { p4++; }
						else if(tmp == 0) { p5++; }
					}
					for (int cont=0; cont<vem; cont++)
					{
						word = st.nextToken();
						change_parameter="";
						if(word.equalsIgnoreCase("tam_flit"))
						{
							word = flitWidth;
							change_parameter = change_parameter.concat(word);
							data_output.writeBytes(change_parameter);
						}
						else if(word.equalsIgnoreCase("tam_buffer"))
						{
							word = ""+bufferDepth;
							change_parameter = change_parameter.concat(word);
							data_output.writeBytes(change_parameter);
						}
						else if(word.equalsIgnoreCase("max_dimX"))
						{
							word = ""+(dimX-1);
							change_parameter = change_parameter.concat(word);
							data_output.writeBytes(change_parameter);
						}
						else if(word.equalsIgnoreCase("max_dimY"))
						{
							word = ""+(dimY-1);
							change_parameter = change_parameter.concat(word);
							data_output.writeBytes(change_parameter);
						}
						else if(word.equalsIgnoreCase("num_routers"))
						{
							word = ""+(dimX * dimY);
							change_parameter = change_parameter.concat(word);
							data_output.writeBytes(change_parameter);
						}
						else if(word.equalsIgnoreCase("num_hexa_router"))
						{
								int contRouters=0;
								for(int y=0;y<dimY;y++)
								{
									addrY = Convert.decToBin(y,(flitW/4));
									addrYHexa = Convert.decToHex(y,(flitW/8));
									for(int x=0;x<dimX;x++)
									{
										addrX = Convert.decToBin(x,(flitW/4));
										addrXHexa = Convert.decToHex(x,(flitW/8));

										data_output.writeBytes("\tconstant N"+addrXHexa+addrYHexa+": integer :="+ contRouters + ";\n");
										data_output.writeBytes("\tconstant ADDRESSN"+addrXHexa+addrYHexa+": std_logic_vector("+((flitW/2)-1)+" downto 0) :=\""+addrX+addrY+"\";\n");
										contRouters++;
									}
								}
						}
						else if(word.equalsIgnoreCase("empty_type_3"))
						{
							if(p3 > 0)
							{
								word = ""+"subtype empty_buf_3 is std_logic_vector(2 downto 0);";
								change_parameter = change_parameter.concat(word);
								data_output.writeBytes(change_parameter);
							}
							else
							{
								word = "";
								change_parameter = change_parameter.concat(word);
								data_output.writeBytes(change_parameter);
							}
						}
						else if(word.equalsIgnoreCase("empty_type_4"))
						{
							if(p4 > 0)
							{
								word = ""+"subtype empty_buf_4 is std_logic_vector(3 downto 0);";
								change_parameter = change_parameter.concat(word);
								data_output.writeBytes(change_parameter);
							}
							else
							{
								word = "";
								change_parameter = change_parameter.concat(word);
								data_output.writeBytes(change_parameter);
							}
						}
						else if(word.equalsIgnoreCase("empty_type_5"))
						{
							if(p5 > 0)
							{
								word = ""+"subtype empty_buf_5 is std_logic_vector(4 downto 0);";
								change_parameter = change_parameter.concat(word);
								data_output.writeBytes(change_parameter);
							}
							else
							{
								word = "";
								change_parameter = change_parameter.concat(word);
								data_output.writeBytes(change_parameter);
							}
						}
						else if(word.equalsIgnoreCase("noc_type"))
						{
							p3=0;p4=0;p5=0;tmp=0;
							for(int i=0;i<p.getNoC().getNumRotX() *  p.getNoC().getNumRotY();i++)
							{
								String saida = generateNameRouter(i);
								tmp=0;
								for(int j=0;j<saida.length();j++)
								{
									if(saida.charAt(j) == '0') tmp++;
								}
								if(tmp == 2) { p3++; }
								else if(tmp == 1) { p4++; }
								else if(tmp == 0) { p5++; }
							}
							if(p3 > 0)
							{
								word = "";
								word = "type noc_buf_3 is array(" + (p3-1) + " downto 0) of empty_buf_3;\n";
								data_output.writeBytes(word);
							}
							if(p4 > 0)
							{
								word = "";
								word = "\ttype noc_buf_4 is array(" + (p4-1) + " downto 0) of empty_buf_4;\n";
								data_output.writeBytes(word);
							}
							if(p5 > 0)
							{
								word = "";
								word = "\ttype noc_buf_5 is array(" + (p5-1) + " downto 0) of empty_buf_5;\n";
								data_output.writeBytes(word);
							}
						}
						else
						{
							change_parameter = change_parameter.concat(word);
							data_output.writeBytes(change_parameter);
						}
					}
					data_output.writeBytes("\r\n");
					n_lines++;
					line=buff.readLine();
				} //end while
				buff.close();
				data_output.close();
				inFile.close();
			}//end try
			catch(FileNotFoundException f){
				JOptionPane.showMessageDialog(null,"Can't write " + nameFile + " file\n" + f.getMessage(),"Output error", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
			catch(Exception e){
				JOptionPane.showMessageDialog(null,e.getMessage(),"Error message", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
		}
		
	/*********************************************************************************
	* PACKAGE
	*********************************************************************************/
		/**
		 * Create the package vhdl file, replacing the flags.
		 * @param nameFile The name of package file.
		 */
		public void createPackage(String nameFile){
			StringTokenizer st;
			String addrX,addrY,addrXHexa,addrYHexa;
			String line, word, change_parameter;

			try{
				FileInputStream inFile = new FileInputStream(new File(sourceDir + nameFile));
				BufferedReader buff=new BufferedReader(new InputStreamReader(inFile));
				
				DataOutputStream data_output=new DataOutputStream(new FileOutputStream(nocDir + nameFile));
				
				int n_lines=0;
				change_parameter="";
				line=buff.readLine();
				while(line!=null){
					st = new StringTokenizer(line, "$");
					int vem = st.countTokens();
					for (int cont=0; cont<vem; cont++){
						word = st.nextToken();
						change_parameter="";
						if(word.equalsIgnoreCase("n_nodos")){
							int contRouters=0;

							for(int y=0;y<dimY;y++){
								addrY = Convert.decToBin(y,(flitW/4));
								addrYHexa = Convert.decToHex(y,(flitW/8));

								for(int x=0;x<dimX;x++){
									addrX = Convert.decToBin(x,(flitW/4));
									addrXHexa = Convert.decToHex(x,(flitW/8));

									data_output.writeBytes("\tconstant N"+addrXHexa+addrYHexa+": integer :="+ contRouters + ";\n");
									data_output.writeBytes("\tconstant ADDRESSN"+addrXHexa+addrYHexa+": std_logic_vector("+((flitW/2)-1)+" downto 0) :=\""+addrX+addrY+"\";\n");
									contRouters++;
								}
							}
						}
						else if(word.equalsIgnoreCase("n_lanes")){
							for(int i=0;i<nChannels;i++){
								if(isSR4)
									data_output.writeBytes("\tconstant ID_CV"+(i)+": integer := "+i+";\n");
								else
									data_output.writeBytes("\tconstant L"+(i+1)+": integer := "+i+";\n");
							}
						}
						else{
							if(word.equalsIgnoreCase("n_port"))
								word = "5";
							else if (word.equalsIgnoreCase("tam_line"))
								word = ""+(flitW/4);
							else if (word.equalsIgnoreCase("flit_size"))
								word = flitWidth;
							else if(word.equalsIgnoreCase("buff_depth"))
								word = ""+bufferDepth;
							else if(word.equalsIgnoreCase("pointer_size")){
								if (bufferDepth <= 4) word="3";
								else if (bufferDepth <= 8) word="4";
								else if (bufferDepth <= 16) word="5";
								else if (bufferDepth <= 32) word="6";
							}
							else if(word.equalsIgnoreCase("n_rot"))
								word = ""+(dimX*dimY);
							else if(word.equalsIgnoreCase("max_X"))
								word = ""+(dimX-1);
							else if(word.equalsIgnoreCase("max_Y"))
								word = ""+(dimY-1);
							else if(word.equalsIgnoreCase("n_lane"))
								word = ""+nChannels;
							else if (word.equalsIgnoreCase("buff1")){
								if (!crcType.equalsIgnoreCase(NoC.SOURCE_CRC))
									word = "";
								else
									word = "--";
							}
							else if (word.equalsIgnoreCase("buff2")){
								if (crcType.equalsIgnoreCase(NoC.SOURCE_CRC))
									word = "";
								else
									word = "--";
							}

							change_parameter = change_parameter.concat(word);
							data_output.writeBytes(change_parameter);
						}
					}
					data_output.writeBytes("\r\n");
					n_lines++;
					line=buff.readLine();
				} //end while
				buff.close();
				data_output.close();
				inFile.close();
			}//end try
			catch(FileNotFoundException f){
				JOptionPane.showMessageDialog(null,"Can't write " + nameFile + " file\n" + f.getMessage(),"Output error", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
			catch(Exception e){
				JOptionPane.showMessageDialog(null,e.getMessage(),"Error message", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
		}

	/*********************************************************************************
	* GENERAL FUNCTIONS
	*********************************************************************************/

		
		/**
		 * Return a vector containing the name of all routers.
		 * @return A vector containing the name of all routers.
		 */
		public Vector<String> getRoutersName(){
			String xHexa="", yHexa="";
			Vector<String> name = new Vector<String>();
			for (int y=0; y<dimY; y++){
				yHexa = Convert.decToHex(y,(flitW/8));
				for (int x=0; x<dimX; x++){
					xHexa = Convert.decToHex(x,(flitW/8));
					name.addElement("N"+xHexa+yHexa);
				}
			}
			return name;
		}
		
		/**
		 * Instance a signal of all router in the same X-dimension.
		 * @param data_output 
		 * @param name The signal name.
		 * @param type The signal type.
		 * @param yHexa The address in Y-dimension. 
		 * @throws Exception 
		 */
		public void writeSignal(DataOutputStream data_output,String name,String type,String yHexa) throws Exception {
			String xHexa;
			data_output.writeBytes("\tsignal ");
			for (int x=0; x<dimX; x++){
				xHexa = Convert.decToHex(x,(flitW/8));
				if (x==0)
					data_output.writeBytes(name+"N"+xHexa+yHexa);
				else
					data_output.writeBytes(", "+name+"N"+xHexa+yHexa);
			}
			data_output.writeBytes(" : "+type+";\n");
		}
		
	/*********************************************************************************
	* SystemC
	*********************************************************************************/
		
		/**
		* copy the SC files to the SC_NoC project directory.
		*/
		public void copySCFiles(){
			//copy CPP files to SC_NoC directory
			ManipulateFile.copy(new File(sourceDir+"SC_InputModule.cpp"), scDir);
			ManipulateFile.copy(new File(sourceDir+"SC_OutputModule.cpp"), scDir);
			ManipulateFile.copy(new File(sourceDir+"SC_OutputModuleRouter.cpp"), scDir);
		}

	/*********************************************************************************
	* SCRIPTS
	*********************************************************************************/
		
		public void writeSimulateHeader(DataOutputStream data_output) throws Exception {
			data_output.writeBytes("if {[file isdirectory work]} { vdel -all -lib work }\n\n");
			data_output.writeBytes("vlib work\n");
			data_output.writeBytes("vmap work work\n\n");
		}
		
		/**
		 * Write the SCCOM command (SystemC compilation) for all SC files and link.
		 * @param data_output 
		 * @throws Exception 
		 */
		public void writeSCCOMFiles(DataOutputStream data_output) throws Exception {
			writeSCCOM(data_output, "SC_NoC/SC_InputModule.cpp");
			writeSCCOM(data_output, "SC_NoC/SC_OutputModule.cpp");
			writeSCCOM(data_output, "SC_NoC/SC_OutputModuleRouter.cpp");
			writeSCCOMLink(data_output);
		}
		
		/**
		 * Write the SCCOM command (SystemC compilation) for a specific file.
		 * @param data_output 
		 * @param file The file path.
		 * @throws Exception 
		 */
		public void writeSCCOM(DataOutputStream data_output, String file) throws Exception {
			data_output.writeBytes("sccom -g "+file+"\n");
		}
		
		/**
		 * Write the SCCOM link.
		 * @param data_output 
		 * @throws Exception 
		 */
		public void writeSCCOMLink(DataOutputStream data_output) throws Exception {
			data_output.writeBytes("sccom -link\n\n");
		}
		
		/**
		 * Write the VCOM command (VHDL compilation) for all routers.
		 * @param data_output 
		 * @throws Exception 
		 */
		public void writeVCOMRouters(DataOutputStream data_output) throws Exception {
			String routerName;
			int xD = dimX;
			int yD = dimY;
			if (dimX>3) xD = 3;
			if (dimY>3) yD = 3;
			for(int y =0; y < yD; y++){
				for(int x =0; x < xD; x++){
					routerName = Router.getRouterType(x, y, xD, yD);
					writeVCOM(data_output, "NOC/"+routerName+".vhd");
				}
			}
		}
		
		/**
		 * Write the VCOM command (VHDL compilation) for a specific file.
		 * @param data_output 
		 * @param file The file path.
		 * @throws Exception 
		 */
		public void writeVCOM(DataOutputStream data_output, String file) throws Exception {
			data_output.writeBytes("vcom -work work -93 -explicit "+file+"\n");
		}

		/**
		 * Write the VSIM command (Modelsim Simulation) for a specific top entity.
		 * @param data_output 
		 * @param nameTop The name of top entity.
		 * @throws Exception 
		 */
		public void writeVSIM(DataOutputStream data_output, String nameTop) throws Exception {
			//data_output.writeBytes("\nvsim -valgrind -t 10ps work."+nameTop+"\n\n");
			if(p.getNoC().getType().equals("HermesG")) data_output.writeBytes("\nvsim -t 100fs work."+nameTop+"\n\n");
			else data_output.writeBytes("\nvsim -t 10ps work."+nameTop+"\n\n");
		}	
		

		/**
		 * Write set StdArithNoWarnings.
		 * @param data_output 
		 * @throws Exception 
		 */
		public void writeNoWarnings(DataOutputStream data_output) throws Exception {
			data_output.writeBytes("set StdArithNoWarnings 1\n");
			data_output.writeBytes("set StdVitalGlitchNoWarnings 1\n");
		}

		/**
		 * Write do for a specific file.
		 * @param data_output 
		 * @param file The file path.
		 * @throws Exception 
		 */
		public void writeDO(DataOutputStream data_output, String file) throws Exception {
			data_output.writeBytes("do "+file+"\n");
		}
		
		/**
		 * Write Add list for a specific signal.
		 * @param data_output 
		 * @param signal The all path signal.
		 * @throws Exception 
		 */
		public void writeAddList(DataOutputStream data_output, String signal) throws Exception {
			data_output.writeBytes("add list -radix decimal "+signal+"\n");
		}
		
		/**
		 * Write list result in a specific file.
		 * @param data_output 
		 * @param file The file name.
		 * @throws Exception 
		 */
		public void writeList(DataOutputStream data_output, String file) throws Exception {
			data_output.writeBytes("write list "+file+"\n");			
		}
		
		/**
		 * Write run for a default simulation time and time resolution (1 ms).
		 * @param data_output 
		 * @throws Exception 
		 */
		public void writeRUN(DataOutputStream data_output) throws Exception {
			writeRUN(data_output, 1, "ms");
		}

		/**
		 * Write run for a specific simulation time and time resolution.
		 * @param data_output 
		 * @param time The simulation time.
		 * @param resolution The time resolution? For instance; ms, us, ns, etc.
		 * @throws Exception 
		 */
		public void writeRUN(DataOutputStream data_output, int time, String resolution) throws Exception {
			data_output.writeBytes("\nrun "+time+" "+resolution+"\n\n");
		}

		
		/**
		 * Write the end of Simulate file.
		 * @param data_output 
		 * @throws Exception 
		 */
		public void writeSimulateFooter(DataOutputStream data_output) throws Exception {
			data_output.writeBytes("quit -sim\n");
			data_output.writeBytes("quit -f\n\n");
		}
		
	}
