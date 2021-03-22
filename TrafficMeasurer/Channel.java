package TrafficMeasurer;

import java.io.*;
import javax.swing.*;

/**
 * This class has all information about a specific channel.
 * @author Aline Vieira de Mello
 * @version
 */
public class Channel
{
    private int rot;
    private int cyclesPerFlit;
    private int nFlits;
    private float cargaMedia;
    private float bitsPerCycle;
    private float atrasoMedio;
    private int atrasoAcumulado;
    private int pktsTrans;
    private float cpfMin;
    private float cpfMed;
    private float cpfMax;
    private int pktMenorCpf;
    private int pktMaiorCpf;
    private int menorAtraso;
    private int maiorAtraso;
    private int pktMenorAtraso;
    private int pktMaiorAtraso;

    /**
     * Constructor of class.
     * @param router The number of router.
     * @param port The number of port. For instance: EAST = 0.
     * @param lane The number of lane. For instance: L1 = 0;
     * @param cpf The number of cycles used to transmit a flit.
     * @param path The path of the channel traffic file.
     */
    public Channel(int router, int port, int cpf, String path){
			rot = router;
			cyclesPerFlit = cpf; 
		    initialize();

		    String nameFile = path + File.separator + "r" + rot + "p" + port + ".txt";
		    evaluate(nameFile);
    }

    /**
     * Constructor of class.
     * @param router The number of router.
     * @param port The number of port. For instance: EAST = 0.
     * @param lane The number of lane. For instance: L1 = 0;
     * @param cpf The number of cycles used to transmit a flit.
     * @param path The path of the channel traffic file.
     */
    public Channel(int router, int port, int lane, int cpf, String path){
			rot = router;
			cyclesPerFlit = cpf; 
		    initialize();

		    String nameFile = path + File.separator + "r" + rot + "p" + port + "l" + lane + ".txt";
		    evaluate(nameFile);
    }
    
    /**
     * Initialize variables.
     */
    private void initialize(){
		nFlits = 0;
		cargaMedia = 0;
		bitsPerCycle = 0;
		atrasoMedio = 0;
		atrasoAcumulado = 0;
		pktsTrans = 0;
		cpfMin = 0;
		cpfMed = 0;
		cpfMax = 0;
		pktMenorCpf = 0;
		pktMaiorCpf = 0;
		menorAtraso = 0;
		maiorAtraso = 0;
		pktMenorAtraso = 0;
		pktMaiorAtraso = 0;
    }

    /**
     * Return the number of transmitted flits in this virtual channel. 
     * @return The number of transmitted flits.
     */
    public int getNumberOfFlits(){ return nFlits; }

    /**
     * Return the average throughput in this virtual channel. 
     * @return The average throughput.
     */    
    public float getAverageThroughput(){ return cargaMedia; }

    /**
     * Return the number of bits transmitted per cycle in this virtual channel. 
     * @return The number of bits transmitted per cycle.
     */
    public float getBitsPerCycle(){	return bitsPerCycle; }

    /**
     * Return the minimal latency in this virtual channel. 
     * @return The minimal latency.
     */
    public int getMinimalLatency(){ return menorAtraso; }

    /**
     * Return the maximal latency in this virtual channel. 
     * @return The maximal latency.
     */
    public int getMaximalLatency(){	return maiorAtraso; }

    /**
     * Return the average latency in this virtual channel. 
     * @return The average latency.
     */
    public float getAverageLatency(){ return atrasoMedio; }

    /**
     * Return the sequence number of packet with the minimal latency in this virtual channel. 
     * @return The sequence number of packet.
     */
    public int getPktMinimalLatency(){ return pktMenorAtraso; }

    /**
     * Return the sequence number of packet with the maximal latency in this virtual channel. 
     * @return The sequence number of packet.
     */
    public int getPktMaximalLatency(){ return pktMaiorAtraso; }
    
    /**
     * Return the number of transmitted packets in this virtual channel. 
     * @return The number of transmitted packets.
     */
    public int getNumberOfPackets(){ return pktsTrans; }

    /**
     * Return the minimal number of cycles per flit in this virtual channel. 
     * @return The minimal number of cycles per flit.
     */
    public float getMinimalCPF(){ return cpfMin; }

    /**
     * Return the average number of cycles per flit in this virtual channel.
     * @return The average number of cycles per flit.
     */
    public float getAverageCPF(){ return cpfMed; }

    /**
     * Return the maximal number of cycles per flit in this virtual channel. 
     * @return The maximal number of cycles per flit.
     */
    public float getMaximalCPF(){ return cpfMax; }

    /**
     * Return the sequence number of packet with the minimal number of cycles per flit in this virtual channel. 
     * @return The sequence number of packet.
     */
    public int getPktMinimalCPF(){ return pktMenorCpf; }

    /**
     * Return the sequence number of packet with the maximal number of cycles per flit in this virtual channel. 
     * @return The sequence number of packet.
     */
    public int getPktMaximalCPF(){ return pktMaiorCpf; }
    
    /**
     * Evaluate all packets transmitted in this virtual channel and get and calculate information.
     */
	private void evaluate(String nameFile){
		int n_abrepar_linha=0;
		int n_espacos_linha=0;
		int n_linhas=0;
		int nseq_alto=0;
		int nseq_baixo=0;
		int tempo_inicial=0;
		int tempo_final=0;
		int tempo_inicial_porta=0;
		int tempo_final_porta=0;
		int size=0;
		float cpf_temp=0;
		float cpf_total_temp=0;
		int atraso_temp=0;
		String str="";

		try{
			if(new File(nameFile).exists()){
				FileInputStream fis = new FileInputStream(nameFile);
				InputStreamReader isr = new InputStreamReader(fis);
				Reader in = new BufferedReader(isr);
				int ch;
				while((ch = in.read()) > -1){
					if(ch=='('){
						n_abrepar_linha++;
						if(n_abrepar_linha==2){
							while((ch = in.read()) != ' ')
							str = str + (char)ch;
							size = Integer.parseInt(str,16) + 2;
							str = "";
							nFlits = nFlits+size;
						}
						else if(n_abrepar_linha==8){
							while((ch = in.read()) != ' ')
							str = str + (char)ch;
							nseq_alto = Integer.parseInt(str,16);
							str = "";
						}
						else if(n_abrepar_linha==9){
							while((ch = in.read()) != ' ')
							str = str + (char)ch;
							nseq_baixo = Integer.parseInt(str,16);
							str = "";
						}
					}
					if(ch==' '){
						n_espacos_linha++;
						if(n_espacos_linha==1){
							while((ch = in.read()) != ')')
							str = str + (char)ch;
							tempo_inicial = Integer.parseInt(str);
							str = "";
							if(n_linhas==0)
							tempo_inicial_porta=tempo_inicial;
						}
						else if(n_espacos_linha==(size)){
							while((ch = in.read()) != ')')
							str = str + (char)ch;
							tempo_final = Integer.parseInt(str);
							str = "";
							tempo_final_porta=tempo_final;
						}
					}
					if(ch=='\n'){
						atrasoAcumulado = atrasoAcumulado+(tempo_final-tempo_inicial);
						atraso_temp = tempo_final-tempo_inicial;
						cpf_temp = (float)(tempo_final-tempo_inicial)/n_espacos_linha;
						cpf_total_temp = cpf_total_temp+cpf_temp;
						tempo_final = 0;
						tempo_inicial = 0;
						n_abrepar_linha = 0;
						n_espacos_linha = 0;
						n_linhas++;
						if(n_linhas==1){
							cpfMin = cpf_temp;
							cpfMax = cpf_temp;
							pktMenorCpf = nseq_baixo+(int)(Math.pow(2,cyclesPerFlit)*nseq_alto);
							pktMaiorCpf = nseq_baixo+(int)(Math.pow(2,cyclesPerFlit)*nseq_alto);
							menorAtraso = atraso_temp;
							maiorAtraso = atraso_temp;
							pktMenorAtraso = nseq_baixo+(int)(Math.pow(2,cyclesPerFlit)*nseq_alto);
							pktMaiorAtraso = nseq_baixo+(int)(Math.pow(2,cyclesPerFlit)*nseq_alto);
						}
						else{
							if(cpf_temp<=cpfMin){
								cpfMin = cpf_temp;
								pktMenorCpf = nseq_baixo+(int)(Math.pow(2,cyclesPerFlit)*nseq_alto);
							}
							if(cpf_temp>=cpfMax){
								cpfMax = cpf_temp;
								pktMaiorCpf = nseq_baixo+(int)(Math.pow(2,cyclesPerFlit)*nseq_alto);
							}
							if(atraso_temp<=menorAtraso){
								menorAtraso = atraso_temp;
								pktMenorAtraso = nseq_baixo+(int)(Math.pow(2,cyclesPerFlit)*nseq_alto);
							}
							if(atraso_temp>=maiorAtraso){
								maiorAtraso = atraso_temp;
								pktMaiorAtraso = nseq_baixo+(int)(Math.pow(2,cyclesPerFlit)*nseq_alto);
							}
						}
						cpf_temp = 0;
						atraso_temp = 0;
						nseq_alto = 0;
						nseq_baixo = 0;
					}
				}
				if(n_linhas==0){
					atrasoMedio = 0;
					cpfMed = 0;
					cargaMedia = 0;
					bitsPerCycle = 0;
				}
				else{
					atrasoMedio = (float)atrasoAcumulado/n_linhas;
					cpfMed = (float)cpf_total_temp/n_linhas;
					cargaMedia = (float)atrasoAcumulado/(tempo_final_porta-tempo_inicial_porta);
					pktsTrans = n_linhas;
					bitsPerCycle = (float)(n_linhas*size*cyclesPerFlit)/(tempo_final_porta-tempo_inicial_porta);
				}
				tempo_inicial_porta = 0;
				tempo_final_porta = 0;
				size = 0;
				n_linhas = 0;
				cpf_total_temp = 0;
			}
		}
		catch(Exception ex){
			JOptionPane.showMessageDialog(null,ex.getMessage(),"Error in CanalVirt.class",JOptionPane.ERROR_MESSAGE);
		}
	}
}

