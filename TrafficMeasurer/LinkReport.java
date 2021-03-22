package TrafficMeasurer;

import java.io.*;
import javax.swing.*;
import java.awt.*;

import AtlasPackage.NoC;
import AtlasPackage.Project;
import AtlasPackage.Router;
import AtlasPackage.Convert;

/**
 * This class is used by NoC without virtual channel to generate the link analysis report and show it using GUI.
 * @author Aline Vieira de Mello
 * @version
 */
public class LinkReport{

    private int numRot, numPort, numRotX, numRotY, flitSize;
    private String dirEntrada;
    private String dirSaida;
    private Channel[][] canal;

	/**
	 * Constructor class.
	 * @param project The project that will be evaluated.
	 */
    public LinkReport(Project project){
		NoC noc = project.getNoC();
		dirSaida = project.getPath()+File.separator + "Traffic" + File.separator+project.getSceneryName()+File.separator + "Out";
        dirEntrada = dirSaida;
		numRotX = noc.getNumRotX();
		numRotY = noc.getNumRotY();
		numRot  = numRotX * numRotY;
		numPort = 4; //temporary
		flitSize = noc.getFlitSize();

		canal = new Channel[numRot][numPort];
		for(int i=0;i<numRot;i++){
            for(int j=0;j<numPort;j++){
				canal[i][j] = new Channel(i,j,flitSize,dirEntrada);
            }
		}
    }

    /**
     * Generate the Link analysis report and show it using a GUI. 
     */
    public void generate(){
    	generateLinkReport();
    	showLinkReport();
    }

    /**
     * Generate the Link analysis report. 
     */
    private void generateLinkReport(){
		String aux = new String();

		try{
            FileOutputStream fos = new FileOutputStream(dirSaida.concat(File.separator + "reports" + File.separator + "rel" + File.separator + "enlace_informacoes_gerais.rel"));
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            Writer out = new BufferedWriter(osw);

            for(int i=0; i<numRot; i++){
				for(int j=0; j<numPort; j++){
                    if(new File(dirEntrada.concat(File.separator + "r"+i+"p"+j+".txt")).exists()){
						if(canal[i][j].getNumberOfPackets()!=0){
							aux = aux + "****** Router " + Convert.getXYAddress(i,numRotX,16) + " Port " + Router.getPortName(j) + " ******\n";
							aux = aux + "Number of Transmitted Packets: " + canal[i][j].getNumberOfPackets() + "\n";
							aux = aux + "Number of Transmitted Flits: " + canal[i][j].getNumberOfFlits() + "\n";
							aux = aux + "Number of Bits per Cycle: " + canal[i][j].getBitsPerCycle() + "\n";
							aux = aux + "Average Throughput: " + (canal[i][j].getAverageThroughput()*100) + "%\n";
							aux = aux + "Average Latency: " + canal[i][j].getAverageLatency() + " cycles\n";
							aux = aux + "Minimum Latency: " + canal[i][j].getMinimalLatency() + " cycles (Packet " + canal[i][j].getPktMinimalLatency() + ")\n";
							aux = aux + "Maximal Latency: " + canal[i][j].getMaximalLatency() + " cycles (Packet " + canal[i][j].getPktMaximalLatency() + ")\n";
							aux = aux + "Average CPF: " + canal[i][j].getAverageCPF() + "\n";
							aux = aux + "Minimum CPF: " + canal[i][j].getMinimalCPF() + " (Packet " + canal[i][j].getPktMinimalCPF() + ")\n";
							aux = aux + "Maximal CPF: " + canal[i][j].getMaximalCPF() + " (Packet " + canal[i][j].getPktMaximalCPF() + ")\n\n";
							out.write(aux);
							aux = "";
						}
                    }
				}
            }
            out.close();
			osw.close();
			fos.close();
		}
		catch(Exception ex){
            JOptionPane.showMessageDialog(null,ex.getMessage(),"ERRO",JOptionPane.ERROR_MESSAGE);
        }
    }

	/**
	 * Show the link analysis report.
	 */
	private void showLinkReport(){
        JFrame frame = new JFrame("Links Analysis Report");
		JTextArea area = new JTextArea();
		area.setRows(22);
		area.setColumns(30);
		area.setEditable(false);
		StringBuffer buffer = new StringBuffer();
   		try{
            FileInputStream fis = new FileInputStream(dirSaida.concat(File.separator + "reports" + File.separator + "rel" + File.separator + "enlace_informacoes_gerais.rel"));
            InputStreamReader isr = new InputStreamReader(fis);
            Reader in = new BufferedReader(isr);
            int ch;
            while((ch = in.read()) > -1) {
				buffer.append((char)ch);
            }
            in.close();
            isr.close();
            fis.close();
            area.append(buffer.toString());
            JPanel p = new JPanel(new BorderLayout());
            p.add(new JScrollPane(area),BorderLayout.CENTER);
            frame.getContentPane().add(p);
            frame.setBounds(300,200,0,0);
            frame.pack();
            frame.setVisible(true);
		}
        catch(Exception ex){
            JOptionPane.showMessageDialog(null,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
		}
    }

}
