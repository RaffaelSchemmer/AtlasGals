package TrafficMeasurer;

import java.io.*;
import javax.swing.*;

import AtlasPackage.Default;
import AtlasPackage.NoC;
import AtlasPackage.Project;

/**
 * This class is used by NoCs without virtual channels to generate 3D graph and show it using GNUPLOT.
 * @author Aline Vieira de Mello
 * @version
 */
public class Graph3D{

    private int numRot, numPort, numRotX, numRotY, flitSize;
    private String dirEntrada;
    private String dirSaida;
    private Channel[][] canal;

	/**
	 * Constructor class.
	 * @param project The project that will be evaluated.
	 */
    public Graph3D(Project project){
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
	 * Generate a 3D graph and show it using GNUPLOT.
	 * @param type The graph type.
	 */
    public void generate(String type){
		String name="";
		if(type.equalsIgnoreCase("Number of flits transmitted")){
			name = "enlace_informacoes_carga";
			writeTxt(name);
			writeDatNFT(name);
		}
		else if(type.equalsIgnoreCase("Cycles per flit (CPF)")){
			name = "enlace_informacoes_cpf";
			writeTxt(name);
			writeDatCPF(name);
		}
		else if(type.equalsIgnoreCase("Channel utilization")){
			name = "enlace_informacoes_carga_media";
			writeTxt(name);
			writeDatChannel(name);
		}
		else if(type.equalsIgnoreCase("Throughput")){
			name = "enlace_informacoes_throughput";
			writeTxt(name);
			writeDatThroughput(name);
		}

		//show graph
		try{
		    String nameFile = dirSaida + File.separator + "reports" + File.separator + "graphs_txt" + File.separator + name + ".txt";
			Default.showGraph(nameFile);
		}
		catch(Exception exc){
            JOptionPane.showMessageDialog(null,exc.getMessage(),"Error Message",JOptionPane.ERROR_MESSAGE);
		}
    }

	/**
	 * Write the TXT file used by GNUPLOT to show a graph. 
	 * @param name The name file.
	 */
	private void writeTxt(String name){

		String nameFile = dirSaida+File.separator + "reports" + File.separator + "graphs_txt" + File.separator+name+".txt";

		try{

            FileOutputStream fos = new FileOutputStream(nameFile);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            Writer out = new BufferedWriter(osw);

			String aux = "";
			String str = "";

            aux = aux + "reset\n";
            aux = aux + "set dgrid3d 35,35,35\n";
            aux = aux + "set view 45,45,1,1\n";
            aux = aux + "set hidden3d\n";
            aux = aux + "set xlabel \"Routers in x axis\"\n";
            aux = aux + "set ylabel \"Routers in y axis\"\n";

            if(name.equalsIgnoreCase("enlace_informacoes_carga")){
				aux = aux + "set zlabel \"Number of flits transmitted \"\n";
            }
            else if(name.equalsIgnoreCase("enlace_informacoes_cpf")){
				aux = aux + "set zlabel \"Average CPF \"\n";
            }
            else if(name.equalsIgnoreCase("enlace_informacoes_carga_media")){
				aux = aux + "set zlabel \"Average load (normalized) \"\n";
            }
            else if(name.equalsIgnoreCase("enlace_informacoes_throughput")){
				aux = aux + "set zlabel \"Average throughput (bpc) \"\n";
            }

            str = dirSaida.concat(File.separator + "reports" + File.separator + "dat" + File.separator+name+".dat");
			aux = aux + "splot '"+str+"' using ($1):($2):($3) t\"\" with lines 1\n";
            aux = aux + "pause -1 \"Press ENTER to continue\"\n";
            out.write(aux);
            out.close();
            osw.close();
            fos.close();
		}
		catch(Exception exc){
            JOptionPane.showMessageDialog(null,exc.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
		}
    }

	/**
	 * Write the DAT file that contains the data showed in the "Number of flits transmitted" graph. 
	 * @param name The name file.
	 */
	private void writeDatNFT(String name){
		String aux = new String();
		try{
            FileOutputStream fos = new FileOutputStream(dirSaida+File.separator + "reports" + File.separator + "dat" + File.separator+name+".dat");
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            Writer out = new BufferedWriter(osw);

            //roteadores
            for(int y=0; y<=((numRotY-1)*2); y=y+2){
				for(int x=0; x<=((numRotX-1)*2); x=x+2){
                    aux = x + " " + y + " 0\n";
                    out.write(aux);
				}
            }
            //espacos vazios
            for(int y=1; y<=((numRotY-1)*2); y=y+2){
				for(int x=1; x<=((numRotX-1)*2); x=x+2){
                    aux = x + " " + y + " 0\n";
                    out.write(aux);
				}
            }

            //enlaces horizontais
            for(int i=0,x=1,y=0; i<numRot ; i++){
				if(x<((numRotX-1)*2)){
                    if(((new File(dirEntrada.concat(File.separator + "r"+i+"p0.txt"))).exists())&&((new File(dirEntrada.concat(File.separator + "r"+(i+1)+"p1.txt"))).exists())){
						aux = x + " " + y + " " + (canal[i][0].getNumberOfFlits()+canal[i+1][1].getNumberOfFlits()) + "\n";
						out.write(aux);
                    }
                    x=x+2;
				}
				else{
                    x=1;
                    y=y+2;
				}
            }
            //enlaces verticais
            for(int i=0,x=0,y=1; i<numRot ; i++){
				if(x<((numRotX-1)*2)){
                    if(((new File(dirEntrada.concat(File.separator + "r"+i+"p2.txt"))).exists())&&((new File(dirEntrada.concat(File.separator + "r"+(i+numRotX)+"p3.txt"))).exists())){
						aux = x + " " + y + " " + (canal[i][2].getNumberOfFlits()+canal[i+numRotX][3].getNumberOfFlits()) + "\n";
						out.write(aux);
                    }
                    x=x+2;
				}
				else if(x==((numRotX-1)*2)){
                    if(((new File(dirEntrada.concat(File.separator + "r"+i+"p2.txt"))).exists())&&((new File(dirEntrada.concat(File.separator + "r"+(i+numRotX)+"p3.txt"))).exists())){
						aux = x + " " + y + " " + (canal[i][2].getNumberOfFlits()+canal[i+numRotX][3].getNumberOfFlits()) + "\n";
						out.write(aux);
                    }
                    x=0;
                    y=y+2;
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
	 * Write the DAT file that contains the data showed in the "Cycles per flit (CPF)" graph. 
	 * @param name The name file.
	 */
	private void writeDatCPF(String name){
		String aux = new String();
		try{
            FileOutputStream fos = new FileOutputStream(dirSaida+File.separator + "reports" + File.separator + "dat" + File.separator+name+".dat");
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            Writer out = new BufferedWriter(osw);

            //roteadores
            for(int y=0; y<=((numRotY-1)*2); y=y+2){
                for(int x=0; x<=((numRotX-1)*2); x=x+2){
                    aux = x + " " + y + " 0\n";
                    out.write(aux);
				}
            }
            //espacos vazios
            for(int y=1; y<=((numRotY-1)*2); y=y+2){
				for(int x=1; x<=((numRotX-1)*2); x=x+2){
                    aux = x + " " + y + " 0\n";
                    out.write(aux);
				}
            }

            //enlaces horizontais
            for(int i=0,x=1,y=0; i<numRot ; i++){
				if(x<((numRotX-1)*2)){
                    if(((new File(dirEntrada.concat(File.separator + "r"+i+"p0.txt"))).exists())&&((new File(dirEntrada.concat(File.separator + "r"+(i+1)+"p1.txt"))).exists())){
						aux = x + " " + y + " " + ((canal[i][0].getAverageCPF()+canal[i+1][1].getAverageCPF())/2) + "\n";
						out.write(aux);
                    }
                    x=x+2;
				}
				else{
                    x=1;
                    y=y+2;
				}
            }
            //enlaces verticais
            for(int i=0,x=0,y=1; i<numRot ; i++){
				if(x<((numRotX-1)*2)){
                    if(((new File(dirEntrada.concat(File.separator + "r"+i+"p2.txt"))).exists())&&((new File(dirEntrada.concat(File.separator + "r"+(i+numRotX)+"p3.txt"))).exists())){
						aux = x + " " + y + " " + ((canal[i][2].getAverageCPF()+canal[i+numRotX][3].getAverageCPF())/2) + "\n";
						out.write(aux);
                    }
                    x=x+2;
				}
				else if(x==((numRotX-1)*2)){
                    if(((new File(dirEntrada.concat(File.separator + "r"+i+"p2.txt"))).exists())&&((new File(dirEntrada.concat(File.separator + "r"+(i+numRotX)+"p3.txt"))).exists())){
						aux = x + " " + y + " " + ((canal[i][2].getAverageCPF()+canal[i+numRotX][3].getAverageCPF())/2) + "\n";
						out.write(aux);
                    }
                    x=0;
                    y=y+2;
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
	 * Write the DAT file that contains the data showed in the "Channel utilization" graph. 
	 * @param name The name file.
	 */
    private void writeDatChannel(String name){
        String aux = new String();
		try{
            FileOutputStream fos = new FileOutputStream(dirSaida+File.separator + "reports" + File.separator + "dat" + File.separator+name+".dat");
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            Writer out = new BufferedWriter(osw);

            //roteadores
            for(int y=0; y<=((numRotY-1)*2); y=y+2){
                for(int x=0; x<=((numRotX-1)*2); x=x+2){
                    aux = x + " " + y + " 0\n";
    	            out.write(aux);
				}
    	    }
            //espacos vazios
            for(int y=1; y<=((numRotY-1)*2); y=y+2){
				for(int x=1; x<=((numRotX-1)*2); x=x+2){
                    aux = x + " " + y + " 0\n";
                    out.write(aux);
				}
            }

            //enlaces horizontais
            for(int i=0,x=1,y=0; i<numRot ; i++){
				if(x<((numRotX-1)*2)){
                    if(((new File(dirEntrada.concat(File.separator + "r"+i+"p0.txt"))).exists())&&((new File(dirEntrada.concat(File.separator + "r"+(i+1)+"p1.txt"))).exists())){
						aux = x + " " + y + " " + ((canal[i][0].getAverageThroughput()+canal[i+1][1].getAverageThroughput())/2) + "\n";
						out.write(aux);
                    }
                    x=x+2;
				}
				else{
                    x=1;
                    y=y+2;
				}
            }
            //enlaces verticais
            for(int i=0,x=0,y=1; i<numRot ; i++){
				if(x<((numRotX-1)*2)){
                    if(((new File(dirEntrada.concat(File.separator + "r"+i+"p2.txt"))).exists())&&((new File(dirEntrada.concat(File.separator + "r"+(i+numRotX)+"p3.txt"))).exists())){
						aux = x + " " + y + " " + ((canal[i][2].getAverageThroughput()+canal[i+numRotX][3].getAverageThroughput())/2) + "\n";
						out.write(aux);
                    }
                    x=x+2;
				}
				else if(x==((numRotX-1)*2)){
                    if(((new File(dirEntrada.concat(File.separator + "r"+i+"p2.txt"))).exists())&&((new File(dirEntrada.concat(File.separator + "r"+(i+numRotX)+"p3.txt"))).exists())){
						aux = x + " " + y + " " + ((canal[i][2].getAverageThroughput()+canal[i+numRotX][3].getAverageThroughput())/2) + "\n";
						out.write(aux);
                    }
                    x=0;
                    y=y+2;
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
	 * Write the DAT file that contains the data showed in the "Throughput" graph. 
	 * @param name The name file.
	 */
 	private void writeDatThroughput(String name){
		String aux = new String();
		try{
            FileOutputStream fos = new FileOutputStream(dirSaida+File.separator + "reports" + File.separator + "dat" + File.separator+name+".dat");
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            Writer out = new BufferedWriter(osw);

            //roteadores
            for(int y=0; y<=((numRotY-1)*2); y=y+2){
				for(int x=0; x<=((numRotX-1)*2); x=x+2){
                    aux = x + " " + y + " 0\n";
                    out.write(aux);
				}
            }
            //espacos vazios
            for(int y=1; y<=((numRotY-1)*2); y=y+2){
				for(int x=1; x<=((numRotX-1)*2); x=x+2){
                    aux = x + " " + y + " 0\n";
                    out.write(aux);
				}
            }

            //enlaces horizontais
            for(int i=0,x=1,y=0; i<numRot ; i++){
				if(x<((numRotX-1)*2)){
                    if(((new File(dirEntrada.concat(File.separator + "r"+i+"p0.txt"))).exists())&&((new File(dirEntrada.concat(File.separator + "r"+(i+1)+"p1.txt"))).exists())){
						aux = x + " " + y + " " + ((canal[i][0].getBitsPerCycle()+canal[i+1][1].getBitsPerCycle())/2) + "\n";
						out.write(aux);
                    }
                    x=x+2;
				}
				else{
                    x=1;
                    y=y+2;
				}
            }
            //enlaces verticais
            for(int i=0,x=0,y=1; i<numRot ; i++){
				if(x<((numRotX-1)*2)){
                    if(((new File(dirEntrada.concat(File.separator + "r"+i+"p2.txt"))).exists())&&((new File(dirEntrada.concat(File.separator + "r"+(i+numRotX)+"p3.txt"))).exists()))
                    {
						aux = x + " " + y + " " + ((canal[i][2].getBitsPerCycle()+canal[i+numRotX][3].getBitsPerCycle())/2) + "\n";
						out.write(aux);
                    }
                    x=x+2;
				}
				else if(x==((numRotX-1)*2)){
                    if(((new File(dirEntrada.concat(File.separator + "r"+i+"p2.txt"))).exists())&&((new File(dirEntrada.concat(File.separator + "r"+(i+numRotX)+"p3.txt"))).exists())){
						aux = x + " " + y + " " + ((canal[i][2].getBitsPerCycle()+canal[i+numRotX][3].getBitsPerCycle())/2) + "\n";
						out.write(aux);
                    }
                    x=0;
                    y=y+2;
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

}
