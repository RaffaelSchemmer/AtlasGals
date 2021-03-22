package TrafficMeasurer;

import java.io.*;
import javax.swing.*;

import AtlasPackage.Project;
import AtlasPackage.NoC;

/**
* This class is used by NoCs with virtual channel to generate TXT graph and show it using GUI.
* @author Aline Vieira de Mello
* @version
*/
public class GraphTXT{

    private int numRot, numPort, numRotX, numRotY, flitSize;
    private String dirEntrada;
    private String dirSaida;
    private Channel[][] canal;

	/**
	 * Constructor class.
	 * @param project The project that will be evaluated.
	 */
    public GraphTXT(Project project){
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
	 * Generate a TXT graph and show it using GUI.
	 * @param type The graph type.
	 */
	public void generate(String type){
		String name = "";
		if(type.equalsIgnoreCase("Number of flits transmitted")){
			name = writeTxtNBT();
		}
		else if(type.equalsIgnoreCase("Cycles per flit (CPF)")){
			name = writeTxtCPF();
		}
		else if(type.equalsIgnoreCase("Channel utilization")){
			name = writeTxtChannel();
		}
		else if(type.equalsIgnoreCase("Throughput")){
			name = writeTxtThroughput();
		}

		new TextAreaDemo(type,new File(name));
    }

	/**
	 * Write the TXT file that contains the data showed in the "Number of flits transmitted" graph.  
	 * @return The name file.
	 */
    private String writeTxtNBT(){
		String aux = new String();
		int cont;
		String espacos ="\t";
		String name = dirSaida.concat(File.separator + "reports" + File.separator + "graphs" + File.separator + "graf_carga.txt");
		try{
            FileOutputStream fos = new FileOutputStream(name);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            Writer out = new BufferedWriter(osw);
            for(int i=0; i<numRotY; i++){
                cont = (numRotX*numRotY)-(numRotX*(i+1));
				for(int j=0; j<numRotX; j++){
                    if(j==0){
						if(cont<10) aux = aux + "(00"+cont+")";
						else if(cont<100) aux = aux + "(0"+cont+")";
						else if(cont<1000) aux = aux + "("+cont+")";
						aux = aux + geraCarga(cont,0) + espacos + espacos;
                    }
                    else if(j!=numRotX-1){
						aux = aux + geraCarga(cont,1);
						if(cont<10) aux = aux + "(00"+cont+")";
						else if(cont<100) aux = aux + "(0"+cont+")";
						else if(cont<1000) aux = aux + "("+cont+")";
						aux = aux + geraCarga(cont,0) + espacos;
                    }
                    else{
                        aux = aux + geraCarga(cont,1);
						if(cont<10) aux = aux + "(00"+cont+")";
						else if(cont<100) aux = aux + "(0"+cont+")";
						else if(cont<1000) aux = aux + "("+cont+")";
                    }
                    cont++;
				}
				out.write(aux);
				aux = "\n";
				cont = (numRotX*numRotY)-(numRotX*(i+1));
				for(int j=0; j<numRotX; j++){
                    aux = aux + geraCarga(cont,3) + espacos + espacos + "         ";
                    cont++;
				}
				aux = aux + "\n\n";
				cont = (numRotX*(numRotY-1))-(numRotX*(i+1));
				if(cont>=0){
                    for(int j=0; j<numRotX; j++){
						aux = aux + geraCarga(cont,2) + espacos + espacos + "         ";
						cont++;
                    }
                    aux = aux + "\n";
				}
            }
            out.close();
            osw.close();
            fos.close();
		}
		catch(Exception ex){
            JOptionPane.showMessageDialog(null,ex.getMessage(),"ERRO",JOptionPane.ERROR_MESSAGE);
		}
		return name;
    }

    private String geraCarga(int i, int j){
		int carga = 0;
		String str = "";

		carga = canal[i][j].getNumberOfFlits();

		if(carga==0) return " 0000 ";
		else{
            Integer intg = new Integer(carga);
            str = intg.toString();
            while(str.length()<6) str = " " + str + " ";
            return str.substring(0,6);
		}
    }

	/**
	 * Write the TXT file that contains the data showed in the "Cycles per flit (CPF)" graph.  
	 * @return The name file.
	 */
	private String writeTxtCPF(){
		String aux = new String();
		int cont;
		String espacos ="\t";
        String name = dirSaida.concat(File.separator + "reports" + File.separator + "graphs" + File.separator + "graf_cpf.txt");
		try{
            FileOutputStream fos = new FileOutputStream(name);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            Writer out = new BufferedWriter(osw);
            for(int y=0; y<numRotY; y++){
				cont = (numRotX*numRotY)-(numRotX*(y+1));
				for(int x=0; x<numRotX; x++){
                    if(x==0){//Nodos a esquerda. Por exemplo: 00 01 02 03
						aux = aux + espacos + geraCpf(cont,0,"max") + espacos;
						cont++;
                    }
                    else if(x!=numRotX-1){//Nodos do meio
						aux = aux + geraCpf(cont,1,"max") + espacos + espacos + geraCpf(cont,0,"max") + espacos;
						cont++;
                    }
                    else{//Nodos a direita. Por exemplo: 30 31 32 33 (se numRotX=4)
						aux = aux + geraCpf(cont,1,"max");
						cont++;
                    }
				}
				aux = aux + "\n";


				cont = (numRotX*numRotY)-(numRotX*(y+1));
				for(int x=0; x<numRotX; x++)
				{
                    if(x==0){
						if(cont<10) aux = aux + "(00"+cont+")" + espacos;
						else if(cont<100) aux = aux + "(0"+cont+")" + espacos;
						else if(cont<1000) aux = aux + "("+cont+")" + espacos;
						aux = aux + geraCpf(cont,0,"med") + espacos;
                    }
                    else if(x!=numRotX-1){
						aux = aux + geraCpf(cont,1,"med") + espacos;
						if(cont<10) aux = aux + " (00"+cont+")" + espacos;
						else if(cont<100) aux = aux + " (0"+cont+")" + espacos;
						else if(cont<1000) aux = aux + " ("+cont+")" + espacos;
						aux = aux + geraCpf(cont,0,"med") + espacos;
                    }
                    else{
                        aux = aux + geraCpf(cont,1,"med") + espacos;
						if(cont<10) aux = aux + " (00"+cont+")" + espacos;
						else if(cont<100) aux = aux + " (0"+cont+")" + espacos;
						else if(cont<1000) aux = aux + " ("+cont+")" + espacos;
                    }
                    cont++;
				}
				aux = aux + "\n";

				cont = (numRotX*numRotY)-(numRotX*(y+1));
				for(int x=0; x<numRotX; x++){
                    if(x==0){//Nodos a esquerda. Por exemplo: 00 01 02 03
						aux = aux + espacos  + geraCpf(cont,0,"min") + espacos;
						cont++;
                    }
                    else if(x!=numRotX-1){//Nodos do meio
						aux = aux + geraCpf(cont,1,"min") + espacos  + espacos  + geraCpf(cont,0,"min") + espacos;
						cont++;
                    }
                    else{//Nodos a direita. Por exemplo: 30 31 32 33 (se numRotX=4)
                        aux = aux + geraCpf(cont,1,"min");
						cont++;
                    }
				}
				out.write(aux);
				aux = "\n";
				cont = (numRotX*numRotY)-(numRotX*(y+1));
				for(int x=0; x<numRotX; x++){
                    if(x==numRotX-1)
						aux = aux + geraCpf(cont,3,"max");
					else
						aux = aux + geraCpf(cont,3,"max") + espacos + espacos + espacos;
                    cont++;
				}
				aux = aux + "\n";
				cont = (numRotX*numRotY)-(numRotX*(y+1));
				for(int x=0; x<numRotX; x++){
                    if(x==numRotX-1)
                    	aux = aux + geraCpf(cont,3,"med");
					else
                    	aux = aux + geraCpf(cont,3,"med") + espacos + espacos + espacos;

                    cont++;
				}
				aux = aux + "\n";
				cont = (numRotX*numRotY)-(numRotX*(y+1));
				for(int x=0; x<numRotX; x++){
                    if(x==numRotX-1)
	                    aux = aux + geraCpf(cont,3,"min");
	                else
	                    aux = aux + geraCpf(cont,3,"min") + espacos + espacos + espacos;
                    cont++;
				}
				aux = aux + "\n\n\n";
				cont = (numRotX*(numRotY-1))-(numRotX*(y+1));
				if(cont>=0){
                    for(int x=0; x<numRotX; x++){
	                    if(x==numRotX-1)
							aux = aux + geraCpf(cont,2,"max");
						else
							aux = aux + geraCpf(cont,2,"max") + espacos + espacos + espacos;
						cont++;
                    }
                    aux = aux + "\n";
				}
				cont = (numRotX*(numRotY-1))-(numRotX*(y+1));
				if(cont>=0){
                    for(int x=0; x<numRotX; x++){
	                    if(x==numRotX-1)
							aux = aux + geraCpf(cont,2,"med");
						else
							aux = aux + geraCpf(cont,2,"med") + espacos + espacos + espacos;
						cont++;
                    }
                    aux = aux + "\n";
				}
				cont = (numRotX*(numRotY-1))-(numRotX*(y+1));
				if(cont>=0){
    		        for(int x=0; x<numRotX; x++){
	                    if(x==numRotX-1)
							aux = aux + geraCpf(cont,2,"min");
						else
							aux = aux + geraCpf(cont,2,"min") + espacos + espacos + espacos;
						cont++;
                    }
                    aux = aux + "\n";
				}
            }
            out.close();
            osw.close();
            fos.close();
		}
		catch(Exception ex){
            JOptionPane.showMessageDialog(null,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
		}
		return name;
    }

    private String geraCpf(int i, int j, String type){
        float cpf=0;
		String str;

		if(type == "min"){
            cpf = canal[i][j].getMinimalCPF();
		}
		else if(type == "med"){
            cpf = canal[i][j].getAverageCPF();
		}
		else if(type == "max"){
            cpf = canal[i][j].getMaximalCPF();
		}

		if(cpf==0) return " 0.00";
		Float f = new Float(cpf);
		str = f.toString();
		if(cpf<10) str = " " + str + "0";
        while(str.length()<5) str = " " + str + " ";
		return str.substring(0,5);
    }

	/**
	 * Write the TXT file that contains the data showed in the "Channel Utilization" graph.  
	 * @return The name file.
	 */
    private String writeTxtChannel(){
        String aux = new String();
		int cont;
		String espacos = "\t";
		String name = dirSaida.concat(File.separator + "reports" + File.separator + "graphs" + File.separator + "graf_carga_media.txt");
		try{
            FileOutputStream fos = new FileOutputStream(name);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            Writer out = new BufferedWriter(osw);
            for(int i=0; i<numRotY; i++){
				cont = (numRotX*numRotY)-(numRotX*(i+1));
				for(int j=0; j<numRotX; j++){
                    if(j==0){
						if(cont<10) aux = aux + "(00"+cont+")";
						else if(cont<100) aux = aux + "(0"+cont+")";
						else if(cont<1000) aux = aux + "("+cont+")";
						aux = aux + geraCargaMedia(cont,0) + espacos + espacos;
                    }
                    else if(j!=numRotX-1){
						aux = aux + geraCargaMedia(cont,1);
						if(cont<10) aux = aux + "(00"+cont+")";
						else if(cont<100) aux = aux + "(0"+cont+")";
						else if(cont<1000) aux = aux + "("+cont+")";
						aux = aux + geraCargaMedia(cont,0) + espacos + espacos;
                    }
                    else{
						aux = aux + geraCargaMedia(cont,1);
						if(cont<10) aux = aux + "(00"+cont+")";
						else if(cont<100) aux = aux + "(0"+cont+")";
						else if(cont<1000) aux = aux + "("+cont+")";
                    }
                    cont++;
				}
				out.write(aux);
				aux = "\n";
				cont = (numRotX*numRotY)-(numRotX*(i+1));
				for(int j=0; j<numRotX; j++){
                    aux = aux + geraCargaMedia(cont,3) + espacos + espacos + "         ";
                    cont++;
                }
				aux = aux + "\n\n";
				cont = (numRotX*(numRotY-1))-(numRotX*(i+1));
				if(cont>=0){
                    for(int j=0; j<numRotX; j++){
						aux = aux + geraCargaMedia(cont,2) + espacos + espacos + "         ";
						cont++;
                    }
                    aux = aux + "\n";
				}
            }
            out.close();
            osw.close();
            fos.close();
		}
		catch(Exception ex){
            JOptionPane.showMessageDialog(null,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
		}
		return name;
    }

    private String geraCargaMedia(int i, int j){
		float cargaMedia=0;
		String str;

		cargaMedia = canal[i][j].getAverageThroughput();

        if(cargaMedia==0) return "0.00";
		Float cm = new Float(cargaMedia);
		str = cm.toString();
        while(str.length()<4) str = str + "0";
		return str.substring(0,4);
    }

	/**
	 * Write the TXT file that contains the data showed in the "Throughput" graph.  
	 * @return The name file.
	 */
  	private String writeTxtThroughput(){
		String aux = new String();
		int cont;
		String espacos = "\t";
		String name = dirSaida.concat(File.separator + "reports" + File.separator + "graphs" + File.separator + "graf_throughput.txt");
		try{
            FileOutputStream fos = new FileOutputStream(name);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            Writer out = new BufferedWriter(osw);
            for(int i=0; i<numRotY; i++){
				cont = (numRotX*numRotY)-(numRotX*(i+1));
				for(int j=0; j<numRotX; j++){
                    if(j==0){
						if(cont<10) aux = aux + "(00"+cont+")";
						else if(cont<100) aux = aux + "(0"+cont+")";
						else if(cont<1000) aux = aux + "("+cont+")";
						aux = aux + geraThroughput(cont,0) + espacos + espacos;
                    }
                    else if(j!=numRotX-1){
						aux = aux + geraThroughput(cont,1);
						if(cont<10) aux = aux + "(00"+cont+")";
						else if(cont<100) aux = aux + "(0"+cont+")";
						else if(cont<1000) aux = aux + "("+cont+")";
						aux = aux + geraThroughput(cont,0) + espacos + espacos;
                    }
                    else{
						aux = aux + geraThroughput(cont,1);
						if(cont<10) aux = aux + "(00"+cont+")";
						else if(cont<100) aux = aux + "(0"+cont+")";
						else if(cont<1000) aux = aux +"("+cont+")";
                    }
                    cont++;
				}
				out.write(aux);
				aux = "\n";
				cont = (numRotX*numRotY)-(numRotX*(i+1));
				for(int j=0; j<numRotX; j++){
                    aux = aux + geraThroughput(cont,3) + espacos + espacos + "         ";
                    cont++;
				}
				aux = aux + "\n\n";
				cont = (numRotX*(numRotY-1))-(numRotX*(i+1));
				if(cont>=0){
                    for(int j=0; j<numRotX; j++){
						aux = aux + geraThroughput(cont,2) + espacos + espacos + "         ";
						cont++;
                    }
                    aux = aux + "\n";
				}
            }
            out.close();
            osw.close();
            fos.close();
		}
		catch(Exception ex){
            JOptionPane.showMessageDialog(null,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
		}
		return name;
    }

    private String geraThroughput(int i, int j){
		float throughput = 0;
		String str;

		throughput = canal[i][j].getBitsPerCycle();

		if(throughput==0) return "0.00";
		Float t = new Float(throughput);
		str = t.toString();
        while(str.length()<4) str = str + "0";
		return str.substring(0,4);
    }
}
