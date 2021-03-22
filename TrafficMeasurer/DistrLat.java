package TrafficMeasurer;

import java.io.*;

import javax.swing.*;
import java.util.*;

import AtlasPackage.Default;
import AtlasPackage.Convert;
import AtlasPackage.Project;
import AtlasPackage.NoC;

/**
 * This class evaluates the latency of a flow between a source and a target router.
 * @author Aline Vieira de Mello
 * @version
 */
public class DistrLat{

	private NoC noc;
	private int numIntervalos, perc, numCV, flitSize,i;
	private String path, nameFile, origem, destino;
	private GraphPoint[] ponto;
	private int nRotX;
	private int size,nPackets=0;
	private double averageLatency=0,minimumLatency=0,maximalLatency=0;
	private double standardDeviationLatency=0;
    private ArrayList<Point> pt;
    
    
	double dtideal=0,dtinject=0,dtcpf=0,dtcuf=0,pcklatency=0,stdvlatency=0,avglatency=0;
    
	/**
	 * Constructor class.
	 * @param project The selected project.
	 * @param nameFile The name file of graph.
	 * @param type The format type: 0 = TXT format showed in a GUI or !0 = GNUPLOT format showed by GNUPLOT.
	 * @param source The source router.
	 * @param target The target router.
	 * @param interval The interval.
	 * @param percentage The discarded percentage.
	 */
	public DistrLat(Project project, String nameFile, int type, String source, String target, int interval, int percentage){
		this.nameFile=nameFile;
		origem = source;
		destino = target;
		numIntervalos = interval;
		perc = percentage;

		path = project.getPath() + File.separator + "Traffic" + File.separator + project.getSceneryName() + File.separator + "Out";
		noc = project.getNoC();
		nRotX = noc.getNumRotX();
		numCV = noc.getVirtualChannel();
		flitSize = noc.getFlitSize();
        pt = new ArrayList<Point>();
		try
        {
			ponto = new GraphPoint[numIntervalos];
			for(int i=0; i<numIntervalos; i++){
				ponto[i] = new GraphPoint();
			}
			
            // Calculo e geração de valores para HermesG
            if(noc.getType().equals("HermesG"))
            {
				if(noc.getFlitSize() == 8)
				{
					origem  = "0" + origem;
					destino = "0" + destino;
				}
			    if(writeDatG())
				    writeTxt(type);
			    else if(type==0)
				    JOptionPane.showMessageDialog(null,"There are no packets in this flow!","Information",JOptionPane.INFORMATION_MESSAGE);
            }
            // Calculo e geração de valores para redes Hermes
            else
            {
			    if(writeDat())
				    writeTxt(type);
			    else if(type==0)
				    JOptionPane.showMessageDialog(null,"There are no packets in this flow!","Information",JOptionPane.INFORMATION_MESSAGE);                
            }
		}
		catch(Exception ex){
			JOptionPane.showMessageDialog(null,"The following error occurred in the DistLat class: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
		}
	}
    
    /**
	 * Generate the distr_lat.dat file that contains the throughput distribution for NoCs HERMESG. 
	 * @return True if the file was created.
	 */

	private boolean writeDatG()
    {
		StringTokenizer st;
		String line;
		double inferior=0,superior=0;
		double interval=0,limitInf=0,limitSup=0;
		double latency=0,latencyMinVir=0,latencyMaxVir=0;
		double descarte=0;
		double latencyAcum=0,sum_var=0;
        
        String target="",source="",ssize="",tideal="",nseq="",tinject="",tcpf="",tcuf="";
        
		FileInputStream fis;
		BufferedReader br;
        
        // Destino | Tampayload | Origem | TSG | NS | TSInjeção | FMS | resto do Payload | FMD | TCDPF | TCDUF |
		//    1         1           1       4    2        4        4           n            4      4       4
    
		// 0011 000B 0000 0000 0000 0000 0001 0000 0001 0000 0000 0000 0001 000C 0018
		// 0011 000B 0000 0000 0000 0000 0014 0000 0002 0000 0000 0000 0014 0019 0025

		File f = new File(path + File.separator + "out" + Convert.getNumberOfRouter(destino,nRotX,flitSize) + ".txt");
		try
        {
			fis=new FileInputStream(f);
            br=new BufferedReader(new InputStreamReader(fis));
			line=br.readLine();
			
			
			
			int flags = 1;
			
			while(line!=null)
            {
				if(flags == 1) {System.out.println("Traffic Latency of : " + origem + " - " + destino); System.out.println(""); flags = 0;}
				st = new StringTokenizer(line, " ");
				int nTokens = st.countTokens();
				if(nTokens!=0)
                {
					target = ""; source = ""; tcuf = ""; 
					tideal = ""; nseq = ""; tinject = ""; tcpf = ""; 
					
					// For responsável por ler todos os campos do pacote
					for(int count=0;count < (nTokens);count++)
                    {
						// Captura destino do tráfego em target
						if(count == 0) target = st.nextToken();
                        // Captura o tamanho do pacote
                        else if(count == 1) ssize = st.nextToken();
                        // Captura a origem do tráfego
						else if(count == 2) source = st.nextToken();
						// Captura o tempo ideal do pacote
						else if(count == 3 || count == 4 || count == 5 || count == 6) tideal = tideal + st.nextToken();
						// Captura o número de sequência do pacote
						else if(count == 7 || count == 8) nseq = nseq + st.nextToken();
						// Captura o tempo de injeção do pacote
						else if(count == 9 || count == 10 || count == 11 || count == 12) tinject = tinject + st.nextToken();
						// Captura o tempo de chegada do primeiro flit
						else if(count == nTokens-2) tcpf = st.nextToken();
						// Captura o tempo de chegada do último flit
						else if(count == nTokens-1) tcuf = st.nextToken();
						else {st.nextToken();}
						
					}
					// Converte os tempos de geração, injeção, primeiro e último flit da base Hexa(16) para a base decimal(10)
					
					size = Integer.parseInt(ssize,16) + 2;
					dtideal = Integer.parseInt(tideal,16);
					dtinject = Integer.parseInt(tinject,16);
					dtcpf = Integer.parseInt(tcpf,16);
					dtcuf = Integer.parseInt(tcuf,16);
					
					/*
					// Depurar os valores lidos
					System.out.println(target);
					System.out.println(ssize);
					System.out.println(source);
					System.out.println(tideal);
					System.out.println(nseq);
					System.out.println(tinject);
					System.out.println(tcpf);
					System.out.println(tcuf);
					
					System.out.println();
					
					System.out.println(dtideal);
					System.out.println(dtinject);
					System.out.println(dtcpf);
					System.out.println(dtcuf);
					
					System.out.println();
					
					System.out.println(origem);
					System.out.println(destino);
					*/
					
					origem = origem.toUpperCase();
					destino = destino.toUpperCase();
					
					// System.out.println(source + " " + origem);
					// System.out.println(target + " " + destino);
					
					// Se o pacote pertencer ao fluxo (Origem-Destino) processar, caso contrário descartar e continuar
					if(((source.substring(source.length()/2).equals(origem) && target.substring(source.length()/2).equals(destino)) || ((noc.getFlitSize() == 8) && source.equals(origem) && target.equals(destino)))) // Se for pacote do tráfego então processa
                    {
						// Converte dtideal e dtinject para tempo absoluto usando o clock origem
						
						// Descobrindo clock da origem
                        for(i=0;i<noc.getClock().size();i++)
                        {
                            // Procurar router origem com router equivalente cadastrado
                            String a = Integer.toString(Convert.getAddressX(source,noc.getFlitSize())) + Integer.toString(Convert.getAddressY(source,noc.getFlitSize()));
                            if(a.equals(noc.getClock().get(i).getNumberRouter())) break;
                        }
                        
                        // Depurando clock da origem
                        //System.out.println(noc.getClock().get(i).getClockIpInput());
                        //System.out.println("Here");
                        // Convertendo ciclos para tempo absoluto
                        dtideal = dtideal * (1000/noc.getClock().get(i).getClockIpInput());
                        dtinject = dtinject * (1000/noc.getClock().get(i).getClockIpInput());
                        
                        // System.out.println(dtideal);
                        // System.out.println(dtinject);
                                                                       
                        // Descobrindo clock do destino
                        for(i=0;i<noc.getClock().size();i++)
                        {
                            // Procurar router destino com router equivalente cadastrado
                            String a = Integer.toString(Convert.getAddressX(target,noc.getFlitSize())) + Integer.toString(Convert.getAddressY(target,noc.getFlitSize()));
                            if(a.equals(noc.getClock().get(i).getNumberRouter())) break;
                        }
						
						// Depurando clock do destino
                        // System.out.println(noc.getClock().get(i).getClockIpOutput());
                        
						// Convertendo ciclos para tempo absoluto
                        dtcpf = dtcpf * (1000/noc.getClock().get(i).getClockIpOutput());
                        dtcuf = dtcuf * (1000/noc.getClock().get(i).getClockIpOutput());
                        
                        // System.out.println(dtcpf);
                        // System.out.println(dtcuf);
                        
                        // Pesquisar se existe Fifo Output no meio do caminho
                        // Descobrindo clock do roteador acoplado no destino
                        int j = 0;
                        for(j=0;j<noc.getClock().size();j++)
                        {
                            // Procurar router destino com router equivalente cadastrado
                            String a = Integer.toString(Convert.getAddressX(target,noc.getFlitSize())) + Integer.toString(Convert.getAddressY(target,noc.getFlitSize()));
                            if(a.equals(noc.getClock().get(j).getNumberRouter())) break;
                        }
                        
                        // Verifica se o clock do IP Output é diferente ao clock do Router (Significa que existe um FifoOutput entre os componentes)
                        if(noc.getClock().get(i).getClockIpOutput() != noc.getClock().get(j).getClockRouter()) 
                        {
							double tmp;
							// Decrementar 4 ciclos do clock de leitura em tempo absoluto (Clock IP Output)
							tmp = 4 * (1000/noc.getClock().get(i).getClockIpOutput());
							dtcuf = dtcuf - tmp;
							// Decrementar meio ciclo do clock de escrita (Clock Router)
							tmp = (1000/noc.getClock().get(j).getClockRouter())/2;
							dtcuf = dtcuf - tmp;
						}
						
                        // Calculo da latência
                        pcklatency = dtcuf - dtideal;
                        System.out.println("Latency : " + pcklatency + " ns");
                        // System.out.println("Latency" + pcklatency);
                        // ArrayList(Componente java que armazena tipos abstratos) guarda valores de latências calculadas.
                        // É feita uma consulta neste ArrayList, se o novo valor a ser armazenado for encontrado, o contador de pacotes desse valor é incrementado
                        // Caso contrário, o novo valor de latência é adicionado na lista, e o contador de pacotes desse valor de latência inicializado em 1
                        int flag=0;
                        for(i=0;i < pt.size();i++)
                        {
							// Se o valor da latência for encontrada o contador desse valor é atualizado em +1
                            if(pt.get(i).get_latency() == pcklatency)
                            {
                                int t = pt.get(i).get_pckt();
                                t++;
                                pt.get(i).set_pckt(t);
                                flag=1;
                                break;
                            }
                        }

                        if(flag == 0) // Signigica que todo o ArrayList pt foi percorrido e não foi encontrada essa latency calculada
                        {
                            // Adiciona a nova latency calculada ao ArrayList pt
                            Point p = new Point();
                            p.set_pckt(1);
                            p.set_latency(pcklatency);
                            pt.add(p);
                        }
                    }
				}
				line=br.readLine();
			}
            br.close();
			fis.close();

            
			// Quando existirem valores cadastrados
            if(pt.size() > 0)
            {
				
				// Ordena a lista de latências em ordem crescente para plotar no grafo (Usa ordenação Bubble Sort)
                double d = 0;
                int e = 0;
                for(i=0;i<pt.size();i++)
                {
                    for(int j=i;j<pt.size();j++)
                    {
                        if(pt.get(i).get_latency() > pt.get(j).get_latency()) 
                        {
                            d = pt.get(i).get_latency();
                            e = pt.get(i).get_pckt();
                            pt.get(i).set_latency(pt.get(j).get_latency());
                            pt.get(i).set_pckt(pt.get(j).get_pckt());
                            pt.get(j).set_latency(d);
                            pt.get(j).set_pckt(e);
                        }
                    }
                }
                
                // Depurando os valores ordenados
                //System.out.println("\n\n");
                
			    //for(i=0;i < pt.size();i++)
				    //System.out.println("Lat : " + pt.get(i).get_latency() + " " + "Pckt : " + pt.get(i).get_pckt());
			
			    //System.out.println("\n\n");
			    
			    
			    
			    // Calculo da latência média
			    // Captura o somatório de todas as latências e o número total de pacotes do tráfego
			    double npackts=0;
			    for(i=0;i< pt.size(); i++)
                {
                    avglatency = avglatency + (pt.get(i).get_latency() * pt.get(i).get_pckt());
                    npackts = npackts + pt.get(i).get_pckt();
                }
                
                nPackets = (int)npackts;
			    avglatency = avglatency/npackts;
			    averageLatency = avglatency;
			    System.out.println("\nAvg Latency : " + avglatency + " ns");
			    
			    
			    // Calculo do desvio padrão
			    // Primeiro cálcula a média das latências (Usar avglatency)
			    // Efetuar o somatório de cada valor de latência decrementado da média elevado ao quadrado
			    // Dividir o somatório pelo número de pacotes total
			    // Elevar o resultado da divisão ao quadrado
			    
			    stdvlatency = 0;
			    for(i=0;i < pt.size();i++)
                {
                    for(int j=0;j<pt.get(i).get_pckt();j++)
                    {
                        stdvlatency = stdvlatency + Math.pow((pt.get(i).get_latency() - avglatency),2);
                    }
                }
                stdvlatency = stdvlatency / npackts;
			    
			    stdvlatency = Math.sqrt(stdvlatency);
			    standardDeviationLatency = stdvlatency;
                System.out.println("Std Dev Latency : " + stdvlatency + " ns");
                System.out.println();
                
                // Colocar a diferença de -10% na menor latência e +10% na maior latência

                // Quantidade de valores que vão ser acrescentados para mais e para menos na latência

				// Calcula número de pacotes adicionais a serem colocados no grafo. Se o resultado for zero pelo menos um valor será colocado.
				
                int dif = (nPackets * 10)/100;
                if(dif == 0) dif = 1;
                int dist = 0;
                
			    // Procura para o intervalo em ns entre cada latência ser criada
                for(i=0;i < pt.size();i++)
                {
                    if(i == pt.size()-1)
                    { 
					    if(dist == 0) // Se existir só uma latência a distancia de plotagem será de 1 ns para +10% e -10%
						    dist = 1;
					    break;  
				    }
                    else
                    {
					    dist = dist + (int)(pt.get(i+1).get_latency() - pt.get(i).get_latency());
                    }
                }
                // DUMP Mostra diferença e número de intervalos extras que vai criar
			    // System.out.println("Dif " + dif + " dist " + dist);


				// Captura latência minima e latência máxima
                minimumLatency = pt.get(0).get_latency();
                maximalLatency =  pt.get(pt.size()-1).get_latency();
                
                double min=0,max=0;
                for(i=0;i < dif;i++)
                {
                    // Verificar o intervalo entre as latencias, encontrar um número de variação média de intervalos
                    // Adicionar N vezes minimas para trás sempre que for > 0

                    min = pt.get(0).get_latency();
                    min = min - dist;
                    max = pt.get(pt.size()-1).get_latency();
                    max = max + dist;
				    if(min > 0)
                    {
                        Point p = new Point();
                        p.set_latency(min);
                        p.set_pckt(0);
                        pt.add(p);
                    }
                    // Verificar o intervalo entre as latencias, encontrar um número de variação média de intervalos
                    // Adicionar N vezes minimas para frente

                    Point p = new Point();
                    p.set_latency(max);
                    p.set_pckt(0);	
                    pt.add(p);

                    // Ordena a lista pt em ordem (menor-maior) bubble sort
                    d = 0;
                    e = 0;
                    for(i=0;i<pt.size();i++)
                    {
                        for(int j=i;j<pt.size();j++)
                        {
                            if(pt.get(i).get_latency() > pt.get(j).get_latency()) 
                            {
                                d = pt.get(i).get_latency();
                                e = pt.get(i).get_pckt();
                                pt.get(i).set_latency(pt.get(j).get_latency());
                                pt.get(i).set_pckt(pt.get(j).get_pckt());
                                pt.get(j).set_latency(d);
                                pt.get(j).set_pckt(e);
                            }
                        }
                    }
                }
                
                // Mostra as latências de pacotes calculadas
			    // for(i=0;i < pt.size();i++)
				    //System.out.println("Lat : " + pt.get(i).get_latency() + " " + "Pckt : " + pt.get(i).get_pckt());
			
			    //System.out.println("\n\n");
			    
                // Escreve o arquivo dist_lat.dat com os valores de latency e pckt armazenados em pt

                FileOutputStream fos = new FileOutputStream(path.concat(File.separator + "reports" + File.separator + "dat" + File.separator+nameFile+".dat"));
        		OutputStreamWriter osw = new OutputStreamWriter(fos);
			    Writer out = new BufferedWriter(osw);

                for(int i = 0; i < pt.size();i++)
			    {
				    out.write(pt.get(i).get_latency() + " " + pt.get(i).get_pckt() + "\n");
				    // DEBUG
				    // System.out.println("Lat : " + pt.get(i).get_latency() +  " " + "Pckt : " + pt.get(i).get_pckt());
			    }
                out.close();
                // nPackets = x;
            }
            
            // Retorna true se existem pacotes no fluxo, senão false
			if(pt.size() > 0)
            {
                return(true);
            }
            else
            {
               return(false);
            }
        }
        catch(FileNotFoundException exc)
        {
			JOptionPane.showMessageDialog(null,"Can't open "+f.getAbsolutePath(),"Input Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		catch(Exception e)
        {
			JOptionPane.showMessageDialog(null,"Error in "+f.getAbsolutePath(),"Input Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
        return(false);
    }
	/**
	 * Generate the distr_lat.dat file that contains the throughput distribution for NoCs HERMES. 
	 * @return True if the file was created.
	 */

	private boolean writeDat()
    {
		StringTokenizer st;
		String line;
		String target="",source="";
		double inferior=0,superior=0;
		double interval=0,limitInf=0,limitSup=0;
		double latency=0,latencyMinVir=0,latencyMaxVir=0;
		double descarte=0;
		double latencyAcum=0,sum_var=0;

		FileInputStream fis;
		BufferedReader br;
		File f = new File(path + File.separator + "out" + Convert.getNumberOfRouter(destino,nRotX,flitSize) + ".txt");

		try{
			for(int cont=0;cont<numCV;cont++){
				if(numCV > 1)
					f = new File(path + File.separator + "out" + Convert.getNumberOfRouter(destino,nRotX,flitSize) + "L" + cont + ".txt");
				fis=new FileInputStream(f);
				br=new BufferedReader(new InputStreamReader(fis));

				line=br.readLine();
				while(line!=null)
                {
					st = new StringTokenizer(line, " ");
					int nTokens = st.countTokens();
                    String s = "";
					if(nTokens!=0)
                    {
                        double tclock_source=0,tclock_target=0;
                        //advance until two tokens before the end of line
						for(int count=0;count<(nTokens-2);count++)
                        {
							if(count==0){
								target=destino;
								st.nextToken();
							}
							else if(count==1)
								size = Integer.parseInt(st.nextToken(),16)+2;
							else if(count==2)
                            {
								source = st.nextToken();
								source = source.substring(source.length()/2);
							}
							else
                            {
                                st.nextToken();
                            }
						}
                        //get the packet latency between the source and target
						latency = Integer.valueOf(st.nextToken()).intValue();
                        if (target.equalsIgnoreCase(destino) && source.equalsIgnoreCase(origem)){
							//store the smallest latency
							if(minimumLatency==0 || minimumLatency>latency)
								minimumLatency=latency;

							//store the greatest latency
							if(maximalLatency==0 || maximalLatency<latency)
								maximalLatency=latency;

							latencyAcum+=latency;
							nPackets++;
						}
					}
					line=br.readLine();
				}
				br.close();
				fis.close();
			}

			if(nPackets!=0)
            {
				descarte=((maximalLatency-minimumLatency)*perc)/100;
				latencyMinVir=minimumLatency+(int)descarte;
				latencyMaxVir=maximalLatency-(int)descarte;
				interval=(int)(latencyMaxVir-latencyMinVir)/numIntervalos;
				averageLatency=latencyAcum/nPackets;

               for(int i=0; i<numIntervalos; i++)
               {
					    if(i==0)
                        {
						    limitInf=latencyMinVir;
						    limitSup=limitInf+interval;
					    }
					    else
                        {
						    limitInf=limitSup+1;
						    limitSup=limitInf+interval;
					    }
					    ponto[i].setCoordX((int)((limitSup+limitInf)/2));
                        
				}
                FileOutputStream fos = new FileOutputStream(path.concat(File.separator + "reports" + File.separator + "dat" + File.separator+nameFile+".dat"));
				OutputStreamWriter osw = new OutputStreamWriter(fos);
				Writer out = new BufferedWriter(osw);

				for(int cont=0;cont<numCV;cont++){
					if(numCV > 1)
						f = new File(path + File.separator + "out" + Convert.getNumberOfRouter(destino,nRotX,flitSize) + "L"+cont + ".txt");
					fis=new FileInputStream(f);
					br=new BufferedReader(new InputStreamReader(fis));

					line=br.readLine();
					while(line!=null){
						st = new StringTokenizer(line, " ");
						int nTokens = st.countTokens();
                        String s = "";
						if(nTokens!=0)
                        {
                            double tclock_source=0,tclock_target=0;
							//advance until two tokens before the end of line
							for(int count=0;count<(nTokens-2);count++){
								if(count==0){
									target =destino;
									st.nextToken();
								}
								else if(count==1)
									size = Integer.parseInt(st.nextToken(),16)+2;
								else if(count==2){
									source = st.nextToken();
									source = source.substring(source.length()/2);
								}
                                else
									st.nextToken();
							}
                             // Var temp que armazenam os clocks absolutos da origem e do destino;
                             //get the packet latency between the source and target
						     latency = Integer.valueOf(st.nextToken()).intValue();
                             if (target.equalsIgnoreCase(destino) && source.equalsIgnoreCase(origem)){
								inferior=latencyMinVir;
								superior=latencyMinVir+interval;
								for(int j=0; j<numIntervalos; j++){
									if(latency>=inferior && latency<=superior){
										ponto[j].setCoordY(ponto[j].getCoordY()+1);
									}
									inferior=superior+1;
									superior=inferior+interval;
								}
								sum_var+=((latency-averageLatency)*(latency-averageLatency));
							}
						}
						line=br.readLine();
					}
					br.close();
					fis.close();
				}

				standardDeviationLatency=Math.sqrt(sum_var/nPackets);

				for(int i=0; i<numIntervalos; i++){
					out.write(ponto[i].getCoordX() + " " + ponto[i].getCoordY() + "\n");
                    // System.out.println(ponto[i].getCoordX() + " " + ponto[i].getCoordY());
				}
				
				out.close();
				osw.close();
				fos.close();
				return true;
			}
		}
		catch(FileNotFoundException exc){
			JOptionPane.showMessageDialog(null,"Can't open "+f.getAbsolutePath(),"Input Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		catch(Exception e){
			JOptionPane.showMessageDialog(null,"Error in "+f.getAbsolutePath(),"Input Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		return false;
	}

	
	
	/**
	 * Generate the TXT file used to show the latency distribution in a GUI or using GNUPLOT.
	 * @param type The format type: O = GUI and !0 = GNUPLOT.
	*/ 
	public void writeTxt(int type)
	{
		String aux = new String();

		try{
			FileOutputStream fos = new FileOutputStream(path.concat(File.separator + "reports" + File.separator + "graphs_txt" + File.separator + nameFile + ".txt"));
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			Writer out = new BufferedWriter(osw);

			if(type==0) // show in gnuplot format
            { 
				aux = aux + "reset\n";
                if(noc.getType().equals("HermesG"))
                {
					if(((new String(""+avglatency)).length()>=7)&&((new String(""+stdvlatency)).length()>=7))
					    aux = aux + "set title \"Latency distribution (In ns)" + "   Average = " + (new String(""+avglatency)).substring(0,7) + " (ns)   Standard deviation = " + (new String(""+stdvlatency)).substring(0,7) + " (ns)\"\n";
				    else aux = aux + "set title \"Latency distribution (In ns)" + "   Average = " + avglatency + " (ns)   Standard deviation = " + stdvlatency + " (ns)\"\n";
                }
                else
                {
				    if(((new String(""+averageLatency)).length()>=7)&&((new String(""+standardDeviationLatency)).length()>=7))
					    aux = aux + "set title \"Latency distribution (In Cycles)" + "   Average = " + (new String(""+averageLatency)).substring(0,7) + " (Cycles)   Standard deviation = " + (new String(""+standardDeviationLatency)).substring(0,7) + " (Cycles)\"\n";
				    else aux = aux + "set title \"Latency distribution (In Cycles)" + "   Average = " + averageLatency + " (Cycles)  Standard deviation = " + standardDeviationLatency + " (Cycles)\"\n";
                }
                // Calculando as faixas onde os valores são apresentados
                // Todos os valores dos pacotes em ytics
                // 10% para mais do máximo e 10% para menos do mínimo em xrange (Se o menor for < 0 colocar zero)
                // 0 até + 10% do máximo para yrange
                if(noc.getType().equals("HermesG"))
                {
                    aux = aux + "set xlabel \"Latency (ns)\"\n";
                    aux = aux + "set format x \"%.2f\";\n";
                    aux = aux + "set xrange [" + pt.get(0).get_latency() + ":" + pt.get(pt.size()-1).get_latency() + "]\n"; // 10% min - 10% max if 10% min < 0 colocar zero
                    // Procura a posição com maior número de pacotes
                    int pkt = 0;
                    for(i=0;i<pt.size();i++)
                    {
                        if(pt.get(i).get_pckt() >= pkt) pkt = pt.get(i).get_pckt();
                    }
                    pkt++;
                    
                    aux = aux + "set yrange [0:" + pkt + "]\n"; // Intervalo de pacotes 0 - 10% max

                    // Descomentado pois um número grande de valores pode inundar o grafo com valores
                    // aux = aux + "set ytics 1\n";
                    aux = aux + "set grid\n";

                    // Colocar os ticks de X
                    // aux = aux + "set xtics \n";
                }
                else
                {
                    aux = aux + "set xlabel \"Latency (Cycles)\"\n";
       				aux = aux + "set yrange [0:]\n";
    				aux = aux + "set xrange [0:]\n";
                }

				aux = aux + "set ylabel \"Number of packets\"\n";
				String str = path + File.separator + "reports" + File.separator + "dat" + File.separator + nameFile + ".dat";
				aux = aux + "plot '"+str+"' using ($1):($2) t \"\" with linespoints pointtype 5 \n";
				aux = aux + "pause -1 \"Press ENTER to continue\"";
			}
			else{// show in the figure format
				String source = Convert.getXYAddress(Convert.getNumberOfRouter(origem,nRotX,flitSize),nRotX,16);
				String target = Convert.getXYAddress(Convert.getNumberOfRouter(destino,nRotX,flitSize),nRotX,16);
				aux = aux + "reset\n";
				aux = aux + "cd '" + path + File.separator + "reports'\n";
				//Alright! here you can set how you want the Graphs to be!
				//just like this!        Type        ///Size!   bgcolor                  color of the 'curve'
				aux = aux + "set grid\n";
				if(Default.isWindows){
				    aux = aux + "set terminal png medium size 900,800 xffffff x000000 x404040 x0000CC x0000CC x0000CC x0000CC x0000CC x0000CC \n";
				}
				else{
				    aux = aux + "set terminal png medium size 900,800 xffffff x000000 x404040 x0000CC x0000CC x0000CC x0000CC x0000CC x0000CC \n";
				}
				aux = aux + "set yrange [0:]\n";
				aux = aux + "set xrange [0:]\n";
				aux = aux + "set pointsize 0.7\n";
				aux = aux + "set output 'images" + File.separator + "Latency"  + source+ "-" + target + ".png'\n";
                if(noc.getType().equals("HermesG"))
                {
				    aux = aux + "set title \" Latency Distribution (In ns) Source = " + source + " Target = " + target + " Average = " + avglatency + " (ns) Standard deviation = " + stdvlatency +  " (ns)\"\n";
				    aux = aux + "set xlabel \"Latency (ns)\"\n";
                }
                else
                {
				    aux = aux + "set title \" Latency Distribution (In Cycles) Source = " + source + " Target = " + target + " Average = " + averageLatency + " (Cycles) Standard deviation = " + standardDeviationLatency +  " (Cycles)\"\n";
				    aux = aux + "set xlabel \"Latency (Cycles)\"\n";

                }
				aux = aux + "set ylabel \"Number of packets\"\n";
				aux = aux + "plot 'dat" + File.separator+nameFile+".dat" + "' using ($1):($2) t \"\" with linespoints 5 5\n";
				aux = aux + "set output";
			}

			out.write(aux);
			out.close();
			osw.close();
			fos.close();

			String nameGraph = path + File.separator + "reports" + File.separator + "graphs_txt" + File.separator + nameFile + ".txt";
			Default.showGraph(nameGraph);
		}
		catch(Exception ex){
		    JOptionPane.showMessageDialog(null,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
		}
	}
	
	
	/**
	 * Return the average latency.
	 * @return The average latency.
	 */
	public double getAverageLatency(){return averageLatency;}

	/**
	 * Return the standard deviation latency.
	 * @return The standard deviation latency.
	 */
	public double getStandardDeviationLatency()
    {
        return standardDeviationLatency;
    }

	/**
	 * Return the minimal latency.
	 * @return The minimal latency.
	 */
	public double getMinimumLatency(){return minimumLatency;}

	/**
	 * Return the maximal latency.
	 * @return The maximal latency.
	 */
	public double getMaximalLatency(){return maximalLatency;}

	/**
	 * Return the number of generated packets.
	 * @return The number of generated packets.
	 */
	public int getNPackets(){return nPackets;}

	/**
	 * Return the number of flits of the last packet.<p>
	 * Ideal when all packets belong to the flow have the same number of flits.
	 * @return The number of flits of the last packet.
	 */
	public int getPacketSize(){return size;}
}

class Point
{
    private int pckt;
    private double latency;

    public void set_pckt(int t_pckt) { pckt = t_pckt; }
    public void set_latency(double t_latency) { latency = t_latency; }

    public int get_pckt() { return(pckt); }
    public double get_latency() { return(latency); }

}
