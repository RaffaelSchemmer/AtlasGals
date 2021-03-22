package TrafficMeasurer;

import java.io.*;
import javax.swing.*;
import java.awt.*;

import AtlasPackage.Router;
import AtlasPackage.Project;
import AtlasPackage.NoC;
import AtlasPackage.Convert;

/**
 * This class is used by NoC with virtual channel to generate the link analysis report and show it using GUI.
 * @author Aline Vieira de Mello
 * @version
 */
public class LinkReport_VC{

    private int numRot,numPort, numCan, numRotX, numBitsFlit;
    private String dirEntrada;
    private String dirSaida;
    private Channel[][][] canalVirt;

	/**
	 * Constructor class.
	 * @param project The project that will be evaluated.
	 */
    public LinkReport_VC(Project project){
    	NoC noc = project.getNoC();
			dirEntrada = project.getSceneryPath()+File.separator + "Out";
			dirSaida = project.getSceneryPath()+File.separator + "Out";
			numRot = noc.getNumRotX()*noc.getNumRotY();
			numPort = 4; //temporary
			numCan = noc.getVirtualChannel();
			numRotX = noc.getNumRotX();
			numBitsFlit = noc.getFlitSize();
			canalVirt = new Channel[numRot][numPort][numCan];
			for(int i=0;i<numRot;i++)
				for(int j=0;j<numPort;j++)
					for(int k=0;k<numCan;k++)
						canalVirt[i][j][k] = new Channel(i,j,k,numBitsFlit,dirEntrada);
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
			FileOutputStream fos = new FileOutputStream(dirSaida.concat(File.separator + "reports" + File.separator + "rel" + File.separator + "enlace_informacoes_gerais_virt.rel"));
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			Writer out = new BufferedWriter(osw);

			for(int i=0; i<numRot; i++){
				for(int j=0; j<numPort; j++){
					for(int k=0; k<numCan; k++){

						if(new File(dirEntrada.concat(File.separator + "r"+i+"p"+j+"l"+k+".txt")).exists()){
							if(canalVirt[i][j][k].getNumberOfPackets()!=0){
								aux = aux + "****** Router " + Convert.getXYAddress(i,numRotX,16) + " Port " + Router.getPortName(j) + " Lane "+(k+1)+" ******\n";
								aux = aux + "Number of Transmitted Packets: " + canalVirt[i][j][k].getNumberOfPackets() + "\n";
								aux = aux + "Number of Transmitted Flits: " + canalVirt[i][j][k].getNumberOfFlits() + "\n";
								aux = aux + "Number of Bits per Cycle: " + canalVirt[i][j][k].getBitsPerCycle() + "\n";
								aux = aux + "Average Throughput: " + (canalVirt[i][j][k].getAverageThroughput()*100) + "%\n";
								aux = aux + "Average Latency: " + canalVirt[i][j][k].getAverageLatency() + " cycles\n";
								aux = aux + "Minimum Latency: " + canalVirt[i][j][k].getMinimalLatency() + " cycles (Packet " + canalVirt[i][j][k].getPktMinimalLatency() + ")\n";
								aux = aux + "Maximal Latency: " + canalVirt[i][j][k].getMaximalLatency() + " cycles (Packet " + canalVirt[i][j][k].getPktMaximalLatency() + ")\n";
								aux = aux + "Average CPF: " + canalVirt[i][j][k].getAverageCPF() + "\n";
								aux = aux + "Minimum CPF: " + canalVirt[i][j][k].getMinimalCPF() + " (Packet " + canalVirt[i][j][k].getPktMinimalCPF() + ")\n";
								aux = aux + "Maximal CPF: " + canalVirt[i][j][k].getMaximalCPF() + " (Packet " + canalVirt[i][j][k].getPktMaximalCPF() + ")\n\n";
								out.write(aux);
								aux = "";
							}
						}
					}
				}
			}
			out.close();
			osw.close();
			fos.close();
		}
		catch(Exception ex){
			JOptionPane.showMessageDialog(null,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
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
			FileInputStream fis = new FileInputStream(dirSaida.concat(File.separator + "reports" + File.separator + "rel" + File.separator + "enlace_informacoes_gerais_virt.rel"));
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
