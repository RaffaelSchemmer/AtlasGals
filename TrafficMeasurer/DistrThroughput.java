package TrafficMeasurer;

import java.io.*;

import javax.swing.*;
import java.util.*;

import AtlasPackage.NoC;
import AtlasPackage.Convert;
import AtlasPackage.Default;
import AtlasPackage.Project;

/**
 * This class evaluates the throughput of a flow between a source and a target router.
 * @author Aline Vieira de Mello
 * @version
 */
public class DistrThroughput
{

	private NoC noc;
	private int numIntervalos, perc, numCiclosFlit,numCV, flitSize,nRotX;
	private String path, nameFile, origem, destino;
	private GraphPoint[] interval_vector;
	private double averageThroughput=0,sourceAverageThroughput=0;
	private double standardDeviationThroughput=0,minimumThroughput=0,maximalThroughput=0;
    public double averageGThroughput=0,minimumGThroughput=0,maximalGThroughput=0;
    private double averagePerGThroughput=0,minimumPerGThroughput=0,maximalPerGThroughput=0;
    
	private int sourcePackets=0,nPackets=0;
    private int size=0;
	private boolean isHermesSRVC;
    private ArrayList<PointT> pt = new ArrayList<PointT>();

	double dtideal=0,dtinject=0,dtcpf=0,dtcuf=0,pckthroughput=0,stdvthroughput=0,avgthroughput=0;
	
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
	public DistrThroughput(Project project, String nameFile, int type, String source, String target, int interval, int percentage){
		this.nameFile=nameFile;
		origem = source;
		destino = target;
		numIntervalos = interval;
		perc = percentage;
		path = project.getPath() + File.separator + "Traffic" + File.separator + project.getSceneryName() + File.separator + "Out";
		noc = project.getNoC();
		nRotX = noc.getNumRotX();
		numCV = noc.getVirtualChannel();
		numCiclosFlit = noc.getCyclesPerFlit();
		flitSize = noc.getFlitSize();
		isHermesSRVC = noc.isSR4();

		try
        {
			interval_vector = new GraphPoint[numIntervalos];
			for(int i=0; i<numIntervalos; i++){
				interval_vector[i] = new GraphPoint();
			}
			//get data about throughput in the traffic source
			getSourceThroughput();
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
            else
            {
			    //verify if the DAT file was created
			    if(writeDat()){
				    writeTxt(type);
			    }
			    else if(type==0)
				    JOptionPane.showMessageDialog(null,"There are no packets in this flow!","Information",JOptionPane.INFORMATION_MESSAGE);
            }
		}
		catch(Exception ex){
			JOptionPane.showMessageDialog(null,"The following error occurred in DistrThroughput class: "+ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
		}
	}


	/**
	 * Generate the distr_throughput.dat of HERMES-G file that contains the throughput distribution. 
	 * @return True if the file was created.
	 */
    private boolean writeDatG()
    {
		StringTokenizer st;
		String line;
		String target="",source="";
		double inferior=0,superior=0;
		double interval=0,limitInf=0,limitSup=0;
		double latency=0,latencyMinVir=0,latencyMaxVir=0;
		double descarte=0;
		double latencyAcum=0,sum_var=0;
       
        double tclock_source=0,tclock_target=0;
        double throughput=0,data=0;
        int i;

        // Variáveis temporarias do método writeDatG
        double time_source=0,time_target=0,temp=0,ftime_source=0;
        double temp_time=0,time_anterior=0,tam_pacote=0;
 
        temp=0;

		String size="",tideal="",nseq="",tinject="",tcpf="",tcuf="";
		
		FileInputStream fis;
		BufferedReader br;
		File f = new File(path + File.separator + "out" + Convert.getNumberOfRouter(destino,nRotX,flitSize) + ".txt");

		// Destino | Tampayload | Origem | TSG | NS | TSInjeção | FMS | resto do Payload | FMD | TCDPF | TCDUF |
		//    1         1           1       4    2        4        4           n            4      4       4
    
		// 0011 000B 0000 0000 0000 0000 0001 0000 0001 0000 0000 0000 0001 000C 0018
		// 0011 000B 0000 0000 0000 0000 0014 0000 0002 0000 0000 0000 0014 0019 0025
		
		
		/*
           * (OK) Armazenar campos 10 a 13 que armazena o tempo que o pacote entrou na rede, guardar esse valor em uma variável tempo.
           * (OK) Armazenar o tempo que o pacote chegou no destino.
           * (OK) Se uma váriavel temp for igual a 1 armazenar o tempo que o pacote saiu na rede, dar um continue, pois primeiro pacote não tem taxa.
           * (OK) Ainda na condição temp == 1, armazenar a quantidade de flits do pacote e o tamanho do pacote. Deve ser feito temp++ após esta condição.
           * (OK) Fazer size + 2 e multiplicar pelo comprimento dos flits, e colocar em somatório de dados.
           * (OK) Subtrair o tempo que chegou na rede pelo temp anterior.
           * (OK) Substituir tempo anterior pela váriável tempo atual, armazenar em tempo final.
           * (OK) Multiplicar o tempo final calculado pelo ciclo do clock do IP origem (source), para isso deve ser pesquisado o clock do IP origem.
           * (OK) Dividir a quantidade de dados pelo tempo final, valor da vazão foi calculado.
           * (OK) Multiplicar por 1000 para obter o valor em Mbit/s
           * (OK) Pesquisa para ver se vazão calculada já existe na lista de vazões, se sim adiciona pckt + 1, caso contrário cria mais uma posição na lista.
           * (OK) Se alguma vazão foi calculada, deve-se ordenar a lista de vazões calculadas
           * Efetuar o cálculo da vazão média, deve-se cálcular a quantidade de dados transmitidos multiplicando size +  pckt -1 "Pois o primeiro pacote não é considerado no cálculo" + flit_size.
           * (OK) Os tempos de transmissão devem ser convertidos para tempo absoluto, multiplicando o tempo de injeção do primeiro pacote pelo periodo do clock na origem e pelo tempo de recepção do último pacote no destino pelo periodo do clock no destino.
           * (OK) Para o cálculo da vazão média deve-se calcular o tempo de transmissão, subtraindo o tempo que chegou na rede "Que esta armazenando neste momento o tempo do último pacote"    pelo tempo do primeiro pacote, armazenado pela condição de temp == 1.
           * (OK) A vazão média de transmissão será a quantidade de dados transmitidos dividido pelo tempo de transmissão dos dados.
           * (VERIFICAR) O desvio padrão será cálculado da seguinte forma :
           * Média é calculada efetuando o somatório de vazões na estrutura de dados e dividindo pela quantidade de pacotes total.
           * Cada vazão deve ser subtraida da média e elevada ao quadrado. Um somatório dos valores resultantes deve ser feito.
           * Feito isso, o valor resultante deverá ser dividido pelo número total de pacotes existentes, e uma raiz quadrada feita do resultado da divisão.
           * (OK) As vazões devem ser ordenadas de maneira crescente.
           * (OK) Um percentual de 10% para menos e para mais deverá ser aplicado.
           * (OK) Uma vez pronto o vetor deverá ser escrito e a distribuição de vazões estará concluida.
        */
        
        // Throughput : Tamanho do pacote em bits (dividido) pelo tempo gasto pelo pacote em tempo absoluto (tcuf - tideal) **"Levar em consideração Fifo Output quando existir"
        // Stand dev  : Calculado a partir das vazões de cada pacote
        // Average    : Somar todas as vazões do link origem/destino e dividir pelo número total de pacotes
        
        // dtideal=0,dtinject=0,dtcpf=0,dtcuf=0,pckthroughput=0,stdvthroughput=0,avgthroughput=0;
        
        try
        {
			fis=new FileInputStream(f);
            br=new BufferedReader(new InputStreamReader(fis));
			line=br.readLine();
			int flags=1;
			while(line!=null)
            {
				if(flags == 1) {System.out.println("Traffic Throughput of : " + origem + " - " + destino); System.out.println(""); flags = 0;}
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
                        else if(count == 1) size = st.nextToken();
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
					
					
					dtideal = Integer.parseInt(tideal,16);
					dtinject = Integer.parseInt(tinject,16);
					dtcpf = Integer.parseInt(tcpf,16);
					dtcuf = Integer.parseInt(tcuf,16);
					
					/*
					// Depurar os valores lidos
					System.out.println(target);
					System.out.println(size);
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
					
					//System.out.println(source + " " + origem);
					//System.out.println(target + " " + destino);
					
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
						
						// Conhecendo tamanho do pacote em flits
						double tam = Integer.parseInt(size,16) + 2;
						tam = tam * noc.getFlitSize();
						
                        // Calculo da latência
                        pckthroughput = ((tam/(dtcuf - dtideal))*1000);
                        System.out.println("Throughput : " + pckthroughput + " Mbps");
                        
                        // System.out.println("Latency" + pcklatency);
                        // ArrayList(Componente java que armazena tipos abstratos) guarda valores de latências calculadas.
                        // É feita uma consulta neste ArrayList, se o novo valor a ser armazenado for encontrado, o contador de pacotes desse valor é incrementado
                        // Caso contrário, o novo valor de latência é adicionado na lista, e o contador de pacotes desse valor de latência inicializado em 1
                        int flag=0;
                        for(i=0;i < pt.size();i++)
                        {
							// Se o valor da latência for encontrada o contador desse valor é atualizado em +1
                            if(pt.get(i).get_throughput() == pckthroughput)
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
                            PointT p = new PointT();
                            p.set_pckt(1);
                            p.set_throughput(pckthroughput);
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
                        if(pt.get(i).get_throughput() > pt.get(j).get_throughput()) 
                        {
                            d = pt.get(i).get_throughput();
                            e = pt.get(i).get_pckt();
                            pt.get(i).set_throughput(pt.get(j).get_throughput());
                            pt.get(i).set_pckt(pt.get(j).get_pckt());
                            pt.get(j).set_throughput(d);
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
                    avgthroughput = avgthroughput + (pt.get(i).get_throughput() * pt.get(i).get_pckt());
                    npackts = npackts + pt.get(i).get_pckt();
                }
                
                
			    avgthroughput = avgthroughput/npackts;
			    averageThroughput = avgthroughput;
			    System.out.println("\nAvg Throughput : " + avgthroughput + " Mbps");
			    
			    
			    // Calculo do desvio padrão
			    // Primeiro cálcula a média das latências (Usar avglatency)
			    // Efetuar o somatório de cada valor de latência decrementado da média elevado ao quadrado
			    // Dividir o somatório pelo número de pacotes total
			    // Elevar o resultado da divisão ao quadrado
			    
			    stdvthroughput = 0;
			    for(i=0;i < pt.size();i++)
                {
                    for(int j=0;j<pt.get(i).get_pckt();j++)
                    {
                        stdvthroughput = stdvthroughput + Math.pow((pt.get(i).get_throughput() - avgthroughput),2);
                    }
                }
                stdvthroughput = stdvthroughput / npackts;
			    stdvthroughput = Math.sqrt(stdvthroughput);
               
                standardDeviationThroughput = stdvthroughput;
                System.out.println("Std Dev Throughput : " + stdvthroughput + " Mbps");
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
					    dist = dist + (int)(pt.get(i+1).get_throughput() - pt.get(i).get_throughput());
                    }
                }
                // DUMP Mostra diferença e número de intervalos extras que vai criar
			    // System.out.println("Dif " + dif + " dist " + dist);


				// Captura latência minima e latência máxima
                minimumThroughput = pt.get(0).get_throughput();
                maximalThroughput = pt.get(pt.size()-1).get_throughput();
                
                double min=0,max=0;
                for(i=0;i < dif;i++)
                {
                    // Verificar o intervalo entre as latencias, encontrar um número de variação média de intervalos
                    // Adicionar N vezes minimas para trás sempre que for > 0

                    min = pt.get(0).get_throughput();
                    min = min - dist;
                    max = pt.get(pt.size()-1).get_throughput();
                    max = max + dist;
				    if(min > 0)
                    {
                        PointT p = new PointT();
                        p.set_throughput(min);
                        p.set_pckt(0);
                        pt.add(p);
                    }
                    // Verificar o intervalo entre as latencias, encontrar um número de variação média de intervalos
                    // Adicionar N vezes minimas para frente

                    PointT p = new PointT();
                    p.set_throughput(max);
                    p.set_pckt(0);	
                    pt.add(p);

                    // Ordena a lista pt em ordem (menor-maior) bubble sort
                    d = 0;
                    e = 0;
                    for(i=0;i<pt.size();i++)
                    {
                        for(int j=i;j<pt.size();j++)
                        {
                            if(pt.get(i).get_throughput() > pt.get(j).get_throughput()) 
                            {
                                d = pt.get(i).get_throughput();
                                e = pt.get(i).get_pckt();
                                pt.get(i).set_throughput(pt.get(j).get_throughput());
                                pt.get(i).set_pckt(pt.get(j).get_pckt());
                                pt.get(j).set_throughput(d);
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

                for(i = 0; i < pt.size();i++)
			    {
				    out.write(pt.get(i).get_throughput() + " " + pt.get(i).get_pckt() + "\n");
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
	 * Generate the distr_throughput.dat of HERMES file that contains the throughput distribution. 
	 * @return True if the file was created.
	 */

	private boolean writeDat()
    {
		Vector<int[]> vetor = new Vector<int[]>();
		StringTokenizer st;
		String line;
		String target="",source="";
		int nTokens=0,nPackets=0;
		int size=0,time=0,lastTime=0;
		double throughput;
		double inferior=0,superior=0;
		double interval=0,limitInf=0,limitSup=0;
		double minimumThroughputVir=0,maximalThroughputVir=0;
		double descarte=0;
		double throughputAcum=0,sum_var=0;

		FileInputStream fis;
		BufferedReader br;

		File f = new File(path.concat(File.separator + "out"+Convert.getNumberOfRouter(destino,nRotX,flitSize)+".txt"));

		try
        {
			for(int cont=0;cont<numCV;cont++){
				if(numCV > 1)
					f = new File(path.concat(File.separator + "out"+Convert.getNumberOfRouter(destino,nRotX,flitSize)+"L"+cont+".txt"));
				fis=new FileInputStream(f);
				br=new BufferedReader(new InputStreamReader(fis));

				line=br.readLine();
				while(line!=null){
					st = new StringTokenizer(line, " ");
					nTokens = st.countTokens();

					if(nTokens!=0){
						//advance until three token before end of packet
						for(int count=0;count<(nTokens-3);count++){
							if(count==0){
								target = destino;
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

						//get time of delivery packet
						time = Integer.valueOf(st.nextToken()).intValue();
						
						if (target.equalsIgnoreCase(destino) && source.equalsIgnoreCase(origem)){
							//insert in a ordered vector
							int [] novo = {time,size};
							if(vetor.size()==0)
								vetor.add(novo);
							else{
								int i=0;
								boolean insert=false;
								while(!insert){
									int [] aux = (int [])vetor.get(i);
									if(aux[0]>novo[0]){
										vetor.insertElementAt(novo,i);
										insert=true;
									}
									else if(i==vetor.size()-1){
										vetor.add(novo);
										insert=true;
									}
									i++;
								}
							}
						}
					}
					line=br.readLine();
				}
				br.close();
			}

			if(vetor.size()!=0){

				//get the maximal and minimal throughput
				for(int i=0;i<vetor.size();i++){
					time = ((int[])vetor.get(i))[0];
					size = ((int[])vetor.get(i))[1];
					if(lastTime!=0){ //discarded the throughput of first packet
						throughput=((100*size*numCiclosFlit)/(time - lastTime));

						//store the minimal throughput
						if(minimumThroughput==0 || minimumThroughput>throughput)
							minimumThroughput=throughput;

						//store the maximal throughput
						if(maximalThroughput==0 || maximalThroughput<throughput)
							maximalThroughput=throughput;

						throughputAcum+=throughput;
						nPackets++;
					}
					lastTime = time;
				}
				descarte=((maximalThroughput-minimumThroughput)*perc)/100;
				minimumThroughputVir=minimumThroughput+(int)descarte;
				maximalThroughputVir=maximalThroughput-(int)descarte;
				interval=(int)(maximalThroughputVir-minimumThroughputVir)/numIntervalos;
				averageThroughput=throughputAcum/nPackets;

				for(int i=0; i<numIntervalos; i++){
					if(i==0){
						limitInf=minimumThroughputVir;
						limitSup=limitInf+interval;
					}
					else{
						limitInf=limitSup+1;
						limitSup=limitInf+interval;
					}
					interval_vector[i].setCoordX((int)((limitSup+limitInf)/2));
				}

				lastTime=0;
				//put each throughput packet in a graph point
				for(int i=0;i<vetor.size();i++){
					time = ((int[])vetor.get(i))[0];
					size = ((int[])vetor.get(i))[1];
					if(lastTime!=0){ //discarded the first packet
						throughput=((100*size*numCiclosFlit)/(time - lastTime));

						sum_var+=((throughput-averageThroughput)*(throughput-averageThroughput));

						inferior=minimumThroughputVir;
						superior=minimumThroughputVir+interval;
						for(int j=0; j<numIntervalos; j++){
							if(throughput>=inferior && throughput<=superior){
								interval_vector[j].setCoordY(interval_vector[j].getCoordY()+1);
							}
							inferior=superior+1;
							superior=inferior+interval;
						}
					}
					lastTime = time;
				}

				standardDeviationThroughput=Math.sqrt(sum_var/nPackets);

				FileOutputStream fos = new FileOutputStream(path.concat(File.separator + "reports" + File.separator + "dat" + File.separator+nameFile+".dat"));
				OutputStreamWriter osw = new OutputStreamWriter(fos);
				Writer out = new BufferedWriter(osw);

				for(int i=0; i<numIntervalos; i++){
					out.write(interval_vector[i].getCoordX() + " " + interval_vector[i].getCoordY() + "\n");
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
	 * Generate the TXT file used to show the throughput distribution in a GUI or using GNUPLOT.
	 * @param type The format type: O = GUI and !0 = GNUPLOT.
	 */
	private void writeTxt(int type){
		String aux = new String();

		try{
			FileOutputStream fos = new FileOutputStream(path.concat(File.separator + "reports" + File.separator + "graphs_txt" + File.separator+nameFile+".txt"));
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			Writer out = new BufferedWriter(osw);

			if(type==0)
			{
				 // show graph in TXT format
				aux = aux + "reset\n";
                if(noc.getType().equals("HermesG"))
                {
       				if(((new String(""+avgthroughput)).length()>=7)&&((new String(""+stdvthroughput)).length()>=7))
    				aux = aux + "set title \"Throughput distribution (In Mbps)" + "   Average = " + (new String(""+avgthroughput)).substring(0,7) + " (Mbps)   Standard deviation = " + (new String(""+stdvthroughput)).substring(0,7) + " (Mbps)\"\n";
    				else aux = aux + "set title \"Throughput distribution (In Mbps)" + "   Average = " + avgthroughput + " (Mbps)  Standard deviation = " + stdvthroughput + " (Mbps)\"\n";

                    aux = aux + "set xlabel \"Throughput (Mbps)\"\n";
                    aux = aux + "set format x \"%.2f\";\n";
                    aux = aux + "set xrange [" + pt.get(0).get_throughput() + ":" + pt.get(pt.size()-1).get_throughput() + "]\n"; // 10% min - 10% max if 10% min < 0 colocar zero
                    // Procura a posição com maior número de pacotes
                    int pkt = 0;
                    for(int i=0;i<pt.size();i++)
                    {
                        if(pt.get(i).get_pckt() >= pkt) pkt = pt.get(i).get_pckt();
                    }
                    pkt++;
                    
                    aux = aux + "set yrange [0:" + pkt + "]\n"; // Intervalo de pacotes 0 - 10% max
                    // aux = aux + "set ytics 1\n";
                    aux = aux + "set grid\n";

                    // Colocar os ticks de X
                    // aux = aux + "set xtics \n";
                }
                else
                {

       				if(((new String(""+averageThroughput)).length()>=7)&&((new String(""+standardDeviationThroughput)).length()>=7))
					aux = aux + "set title \"Throughput distribution " + "   Average = " + (new String(""+averageThroughput)).substring(0,7) + "   Standard deviation = " + (new String(""+standardDeviationThroughput)).substring(0,7) + "\"\n";
    				else aux = aux + "set title \"Throughput distribution " + "   Average = " + averageThroughput + "   Standard deviation = " + standardDeviationThroughput + "\"\n";

                    aux = aux + "set xlabel \"Load (%)\"\n";
				    aux = aux + "set xrange [0:]\n";
				    aux = aux + "set ylabel \"Number of packets\"\n";
                }

				String str = path + File.separator + "reports" + File.separator + "dat" + File.separator + nameFile + ".dat";
				aux = aux + "plot '" + str + "' using ($1):($2) t \"\" with linespoints pointtype 5\n";
				aux = aux + "pause -1 \"Press ENTER to continue\"\n";
			}
			else{ //show graph in GNUPLOT format
				String source = Convert.getXYAddress(Convert.getNumberOfRouter(origem,nRotX,flitSize),nRotX,16);
				String target = Convert.getXYAddress(Convert.getNumberOfRouter(destino,nRotX,flitSize),nRotX,16);
				aux = aux + "reset\n";
				aux = aux + "cd '" + path + File.separator + "reports'\n";
				aux = aux + "set grid\n";
				if(Default.isWindows){
				    aux = aux + "set terminal png medium size 950,800 xffffff x000000 x404040 x0000CC x0000CC x0000CC x0000CC x0000CC x0000CC \n";
				}
				else{
				    aux = aux + "set terminal png medium size 950,800 xffffff x000000 x404040 x0000CC x0000CC x0000CC x0000CC x0000CC x0000CC \n";
				}
				
				aux = aux + "set yrange [0:]\n";
				aux = aux + "set xrange [0:]\n";
				aux = aux + "set pointsize 0.7\n";
				aux = aux + "set output 'images" + File.separator + "Throughput"  + source  + "-" + target + ".png'\n";
                if(noc.getType().equals("HermesG"))
                {
				    aux = aux + "set title \" Throughput Distribution (In Mbps)  Source = " + source + " Target = " + target + " Average = " + avgthroughput+ " (Mbps) Standard deviation = " + stdvthroughput+  " (Mbps)\"\n";
				    aux = aux + "set xlabel \"Throughput (Mbps)\"\n";
                }
                else
                {
				    aux = aux + "set title \" Throughput Distribution  Source = " + source + " Target = " + target + " Average = " + avgthroughput+ " Standard deviation = " + stdvthroughput+  "     \"\n";
				    aux = aux + "set xlabel \"Throughput\"\n";
                }
				aux = aux + "set ylabel \"Number of packets\"\n";
				aux = aux + "plot 'dat" + File.separator+nameFile+".dat' using ($1):($2) t \"\" with linespoints 5 5\n";
				aux = aux + "set output";
			}

			out.write(aux);
			out.close();
			osw.close();
			fos.close();

			String nameGraph = path + File.separator + "reports" + File.separator + "graphs_txt" + File.separator+nameFile+".txt";
			Default.showGraph(nameGraph);
		}
		catch(Exception ex){
		    JOptionPane.showMessageDialog(null,ex.getMessage(),"Error Message",JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * verify the throughput in the source router. 
	 */
	private void getSourceThroughput(){
		StringTokenizer st;
		String line;
		String target="",source="";
		String pathIn, fileIn;
		double throughput=0,sumThroughtput=0;
		int nTokens=0, nPackets=0, lastTime=0, time=0, size=0, nLacos=1, nodo;
		File trafficF;
		FileInputStream trafficFIS;
		DataInputStream trafficDIS;
		BufferedReader trafficBR;
		
		pathIn = path.substring(0,path.indexOf("Out"))+"In" + File.separator;
		nodo = Convert.getNumberOfRouter(origem,nRotX,flitSize);
		
		if(isHermesSRVC)
			nLacos = 3;
		
		sourceAverageThroughput=0;
		for(int index=0; index<nLacos; index++){

			target="";
			source="";
			throughput=0;
			sumThroughtput=0;
			nTokens=0;
			nPackets=0;
			lastTime=0;
			time=0;
			size=0;
			
			switch(index){
				case 0:
					if(nLacos==1)
						fileIn = pathIn + "in" + nodo + ".txt";
					else
						fileIn = pathIn + "inCTRL" + nodo + ".txt";
					break;
				case 1: 
					fileIn = pathIn + "inGS" + nodo + ".txt";
					break;
				case 2: 
					fileIn = pathIn + "inBE" + nodo + ".txt";
					break;
				default: 
					fileIn = "INVALID FILE OPTION";
			}
			
			try{
				trafficF = new File(fileIn);
				if(!trafficF.exists()) continue;
				trafficFIS=new FileInputStream(trafficF);
				trafficDIS=new DataInputStream(trafficFIS);
				trafficBR=new BufferedReader(new InputStreamReader(trafficDIS));

				line=trafficBR.readLine();

				while(line!=null){
					st = new StringTokenizer(line, " ");
					nTokens = st.countTokens();

					if(nTokens!=0){
						//advance until two tokens before the end of line
						for(int count=0;count<nTokens;count++){
							if(count==0) time = Integer.parseInt(st.nextToken(),16);
							else if(count==1){
								target = st.nextToken();
								target = target.substring(target.length()/2);
							}
							else if(count==2)
								size = Integer.parseInt(st.nextToken(),16)+6; //6=> 2 header + 4 timestamp
							else if(count==3){
								source = st.nextToken();
								source = source.substring(source.length()/2);
							}
							else
								st.nextToken();
						}
						if (target.equalsIgnoreCase(destino) && source.equalsIgnoreCase(origem)){
							if(lastTime!=0){ //discarded the first packet
								throughput=((100*size*numCiclosFlit)/(time - lastTime));
								sumThroughtput = sumThroughtput + throughput;
								nPackets++;
							}
							lastTime = time;
							sourcePackets++;
						}
					}
					line=trafficBR.readLine();
				}
				
				trafficBR.close();
				trafficDIS.close();
				trafficFIS.close();

				if(nPackets!=0){
					sourceAverageThroughput+= sumThroughtput / nPackets;
				}
			}
			catch(FileNotFoundException exc){
				JOptionPane.showMessageDialog(null,"Can't Open File "+fileIn,"Input error", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
			catch(Exception e){
				JOptionPane.showMessageDialog(null,"ERROR: "+e.getMessage(),"Input Error", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}		
		}
	}

	/**
	 * Return the average throughput in the source router.
	 * @return The average throughput in the source router.
	 */
	public double getSourceAverageThroughput(){return sourceAverageThroughput;}

	/**
	 * Return the number of generated packets.
	 * @return The number of generated packets.
	 */
	public int getSourcePackets(){return sourcePackets;}
	
	/**
	 * Return the average throughput.
	 * @return The average throughput.
	 */
	public double getAverageThroughput(){return averageThroughput;}

	/**
	 * Return the standard deviation throughput.
	 * @return The standard deviation throughput.
	 */
	public double getStandardDeviationThroughput(){return standardDeviationThroughput;}

	/**
	 * Return the minimal throughput.
	 * @return The minimal throughput.
	 */
	public double getMinimumThroughput(){return minimumThroughput;}

	/**
	 * Return the maximal throughput.
	 * @return The maximal throughput.
	 */
	public double getMaximalThroughput(){return maximalThroughput;}

    public double getAverageGThroughput(){return averageGThroughput;}

    public double getMinimumGThroughput(){return minimumGThroughput;}

    public double getMaximalGThroughput(){return maximalGThroughput;}

    public double getAveragePerGThroughput(){return averagePerGThroughput;}
    
    public double getMinimumPerGThroughput(){return minimumPerGThroughput;}

    public double getMaximalPerGThroughput(){return maximalPerGThroughput;}

}

class PointT
{
    private int pckt;
    private double throughput;

    public void set_pckt(int t_pckt) { pckt = t_pckt; }
    public void set_throughput(double t_throughput) { throughput = t_throughput; }

    public int get_pckt() { return(pckt); }
    public double get_throughput() { return(throughput); }

}
