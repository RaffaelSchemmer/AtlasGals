package TrafficMeasurer;

import java.io.*;
import javax.swing.*;

import AtlasPackage.NoC;
import AtlasPackage.Default;
import AtlasPackage.Project;

/**
 * This class is used by NoCs with virtual channel to generate 3D graph and show it using GNUPLOT.
 * @author Aline Vieira de Mello
 * @version
 */
public class Graph3D_VC{

    private int numRot, numPort, numCan;
    private int numRotX, numRotY, numBitsFlit;
    private String nocType;
    private String dirEntrada;
	private String dirSaida;
    private Channel[][][] canalVirt;

	/**
	 * Constructor class.
	 * @param project The project that will be evaluated.
	 */
    public Graph3D_VC(Project project){
    	NoC noc = project.getNoC();
    	dirEntrada = project.getSceneryPath()+File.separator + "Out";
    	dirSaida = project.getSceneryPath()+File.separator + "Out";
    	numRot = noc.getNumRotX()*noc.getNumRotY();
    	nocType= noc.getType();
    	numPort = 4; //temporary
    	numCan = noc.getVirtualChannel();
		numRotX = noc.getNumRotX();
		numRotY = noc.getNumRotY();
		numBitsFlit = noc.getFlitSize();
		
		canalVirt = new Channel[numRot][numPort][numCan];
		for(int i=0;i<numRot;i++){
			for(int j=0;j<numPort;j++){
				for(int k=0;k<numCan;k++){
					canalVirt[i][j][k] = new Channel(i,j,k,numBitsFlit,dirEntrada);
				}
			}
		}
    }

	/**
	 * Generate a 3D graph and show it using GNUPLOT.
	 * @param type The graph type.
	 */
	 public void generate(String type){
		String name="";
		//write the TXT and DAT files allowing the GNUPLOT exhibition.
		if(type.equalsIgnoreCase("Number of flits transmitted")){
			name = "enlace_informacoes_carga_virt";
			writeTxt(name);
			writeDatNFT(name);
		}
		if(type.equalsIgnoreCase("Cycles per flit (CPF)")){
			name = "enlace_informacoes_cpf_virt";
			writeTxt(name);
			writeDatCPF(name);
		}
		else if(type.equalsIgnoreCase("Channel utilization")){
			name = "enlace_informacoes_carga_media_virt";
			writeTxt(name);
			writeDatChannel(name);
		}
		else if(type.equalsIgnoreCase("Throughput")){
			name = "enlace_informacoes_throughput_virt";
			writeTxt(name);
			writeDatThroughput(name);
		}
		//show graph
		try{
		    String nameFile = dirSaida + File.separator + "reports" + File.separator + "graphs_txt" + File.separator + name + ".txt";
			Default.showGraph(nameFile);
		}
		catch(Exception exc){
			JOptionPane.showMessageDialog(null,exc.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
		}
    }

	/**
	 * Write the TXT file used by GNUPLOT to show a graph. 
	 * @param name The name file.
	 */
	private void writeTxt(String name){
		String aux = new String();
		String str = new String();
		String nameFile = dirSaida+File.separator + "reports" + File.separator + "graphs_txt" + File.separator + name+".txt";

		try{
			FileOutputStream fos = new FileOutputStream(nameFile);
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			Writer out = new BufferedWriter(osw);

			aux = aux + "reset\n";
			aux = aux + "set dgrid3d 35,35,35\n";
			aux = aux + "set view 45,45,1,1\n";
			aux = aux + "set hidden3d\n";
			aux = aux + "set xlabel \"Routers in x axis\"\n";
			aux = aux + "set ylabel \"Routers in y axis\"\n";

			if(name.equalsIgnoreCase("enlace_informacoes_carga_virt")){
				aux = aux + "set zlabel \"Number of flits transmitted \"\n";
			}
			else if(name.equalsIgnoreCase("enlace_informacoes_cpf_virt")){
				aux = aux + "set zlabel \"Average CPF \"\n";
			}
			else if(name.equalsIgnoreCase("enlace_informacoes_carga_media_virt")){
				aux = aux + "set zlabel \"Average load (normalized) \"\n";
			}
			else if(name.equalsIgnoreCase("enlace_informacoes_throughput_virt")){
				aux = aux + "set zlabel \"Average throughput (bpc) \"\n";
			}

			str = dirSaida.concat(File.separator + "reports" + File.separator + "dat" + File.separator+name+".dat");
			aux = aux + "splot '" + str + "' using ($1):($2):($3) t\"\" with lines 1\n";
			aux = aux + "pause -1 \"Press ENTER to continue\"\n";
			out.write(aux);
			out.close();
			osw.close();
			fos.close();
		}
		catch(Exception ex){
			JOptionPane.showMessageDialog(null,ex.getMessage(),"ERRO!!!",JOptionPane.ERROR_MESSAGE);
		}
    }

	/**
	 * Write the DAT file that contains the data showed in the "Number of flits transmitted" graph. 
	 * @param name The name file.
	 */
	private void writeDatNFT(String name){
		String aux = new String();
		int cont = 0;
		float[][] cargaTotal = new float[numRot][numPort];
		try{
			FileOutputStream fos = new FileOutputStream(dirSaida.concat(File.separator + "reports" + File.separator + "dat" + File.separator+name+".dat"));
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			Writer out = new BufferedWriter(osw);

			//routers
			for(int y=0; y<=((numRotY-1)*2); y=y+2){
				for(int x=0; x<=((numRotX-1)*2); x=x+2){
					aux = x + " " + y + " 0\n";
					out.write(aux);
				}
			}

			//empty space
			if(nocType.equalsIgnoreCase(NoC.HERMESTU)){
				for(int y=1; y<=((numRotY-1)*2)+2; y=y+2){
					for(int x=1; x<=((numRotX-1)*2)+2; x=x+2){
						aux = x + " " + y + " 0\n";
						out.write(aux);
					}
				}
			}
			else if((nocType.equalsIgnoreCase(NoC.HERMESSR))||(nocType.equalsIgnoreCase(NoC.HERMES))){
				for(int y=1; y<=((numRotY-1)*2); y=y+2){
					for(int x=1; x<=((numRotX-1)*2); x=x+2){
						aux = x + " " + y + " 0\n";
						out.write(aux);
					}
				}
			}

			for(int i=0; i<numRot; i++){
				for(int j=0; j<numPort; j++){
					for(int k=0; k<numCan; k++){
						if(canalVirt[i][j][k].getNumberOfFlits() > 0){
							cont++;
							cargaTotal[i][j]=(float)cargaTotal[i][j]+canalVirt[i][j][k].getNumberOfFlits();
						}
					}
					cont=0;
				}
			}

			//horizontal channels
			if(nocType.equalsIgnoreCase(NoC.HERMESTU)){
				for(int i=0,x=1,y=0; i<numRot ; i++){
					if( x< (((numRotX-1)*2) + 2) ){
						if(((new File(dirEntrada.concat(File.separator + "r"+i+"p0l0.txt"))).exists())){
							aux = x + " " + y + " " + cargaTotal[i][0] + "\n";
							out.write(aux);
						}
						x=x+2;
					}
					else{
						i--;
						x=1;
						y=y+2;
					}
				}
			}
			else if((nocType.equalsIgnoreCase(NoC.HERMESSR))||(nocType.equalsIgnoreCase(NoC.HERMES))){
				for(int i=0,x=1,y=0; i<numRot ; i++){
					if(x<((numRotX-1)*2)){
						if(((new File(dirEntrada.concat(File.separator + "r"+i+"p0l0.txt"))).exists())&&((new File(dirEntrada.concat(File.separator + "r"+(i+1)+"p1l0.txt"))).exists())){
							aux = x + " " + y + " " + (cargaTotal[i][0]+cargaTotal[i+1][1]) + "\n";
							out.write(aux);
						}
						x=x+2;
					}
					else{
						x=1;
						y=y+2;
					}
				}
			}

			//vertical channels
			if(nocType.equalsIgnoreCase(NoC.HERMESTU)){
				for(int i=0,x=0,y=1; i<numRot ; i++){
					if(x<((numRotX-1)*2)){
						if(((new File(dirEntrada.concat(File.separator + "r"+i+"p2l0.txt"))).exists())){
							aux = x + " " + y + " " + cargaTotal[i][2] + "\n";
							out.write(aux);
						}
						x=x+2;
					}
					else if(x==((numRotX-1)*2)){
						if(((new File(dirEntrada.concat(File.separator + "r"+i+"p2l0.txt"))).exists())){
							aux = x + " " + y + " " + cargaTotal[i][2] + "\n";
							out.write(aux);
						}
						x=0;
						y=y+2;
					}
				}
			}
			else if((nocType.equalsIgnoreCase(NoC.HERMESSR))||(nocType.equalsIgnoreCase(NoC.HERMES))){
				for(int i=0,x=0,y=1; i<numRot ; i++){
					if(x<((numRotX-1)*2)){
						if(((new File(dirEntrada.concat(File.separator + "r"+i+"p2l0.txt"))).exists())&&((new File(dirEntrada.concat(File.separator + "r"+(i+numRotX)+"p3l0.txt"))).exists())){
							aux = x + " " + y + " " + (cargaTotal[i][2]+cargaTotal[i+numRotX][3]) + "\n";
							out.write(aux);
						}
						x=x+2;
					}
					else if(x==((numRotX-1)*2)){
						if(((new File(dirEntrada.concat(File.separator + "r"+i+"p2l0.txt"))).exists())&&((new File(dirEntrada.concat(File.separator + "r"+(i+numRotX)+"p3l0.txt"))).exists())){
							aux = x + " " + y + " " + (cargaTotal[i][2]+cargaTotal[i+numRotX][3]) + "\n";
							out.write(aux);
						}
						x=0;
						y=y+2;
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
	 * Write the DAT file that contains the data showed in the "Cycles per flit (CPF)" graph. 
	 * @param name The name file.
	 */
	private void writeDatCPF(String name){
		String aux = new String();
		int cont = 0;
		float[][] cpfMedTotal = new float[numRot][numPort];
		try{
			FileOutputStream fos = new FileOutputStream(dirSaida.concat(File.separator + "reports" + File.separator + "dat" + File.separator+name+".dat"));
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			Writer out = new BufferedWriter(osw);

			//routers
			for(int y=0; y<=((numRotY-1)*2); y=y+2){
				for(int x=0; x<=((numRotX-1)*2); x=x+2){
					aux = x + " " + y + " 0\n";
					out.write(aux);
				}
			}
			//empty spaces
			if(nocType.equalsIgnoreCase(NoC.HERMESTU)){
				for(int y=1; y<=((numRotY-1)*2)+2; y=y+2){
					for(int x=1; x<=((numRotX-1)*2)+2; x=x+2){
						aux = x + " " + y + " 0\n";
						out.write(aux);
					}
				}
			}
			else if((nocType.equalsIgnoreCase(NoC.HERMESSR))||(nocType.equalsIgnoreCase(NoC.HERMES))){
				for(int y=1; y<=((numRotY-1)*2); y=y+2){
					for(int x=1; x<=((numRotX-1)*2); x=x+2){
						aux = x + " " + y + " 0\n";
						out.write(aux);
					}
				}
			}

			if((nocType.equalsIgnoreCase(NoC.HERMESSR))||(nocType.equalsIgnoreCase(NoC.HERMES))){
				for(int i=0; i<numRot; i++){
					for(int j=0; j<numPort; j++){
						for(int k=0; k<numCan; k++){
							if(canalVirt[i][j][k].getAverageCPF() > 0){
								cont++;
								cpfMedTotal[i][j]=(float)cpfMedTotal[i][j]+canalVirt[i][j][k].getAverageCPF();
							}
						}
						if(cont>0)
							cpfMedTotal[i][j]=(float)((cpfMedTotal[i][j])/cont)/2;
						else
							cpfMedTotal[i][j]=0;
						cont=0;
					}
				}
			}
			else if(nocType.equalsIgnoreCase(NoC.HERMESTU)){
				for(int i=0; i<numRot; i++){
					for(int j=0; j<numPort; j++){
						for(int k=0; k<numCan; k++){
							if(canalVirt[i][j][k].getAverageCPF() > 0){
								cont++;
								cpfMedTotal[i][j]=(float)cpfMedTotal[i][j]+canalVirt[i][j][k].getAverageCPF();
							}
						}
						if(cont>0)
							cpfMedTotal[i][j]=(float)((cpfMedTotal[i][j])/cont);
						else
							cpfMedTotal[i][j]=0;
						cont=0;
					}
				}
			}

			//horizontal channels
			if((nocType.equalsIgnoreCase(NoC.HERMESSR))||(nocType.equalsIgnoreCase(NoC.HERMES))){
				for(int i=0,x=1,y=0; i<numRot ; i++){
					if(x<((numRotX-1)*2)){
						if(((new File(dirEntrada.concat(File.separator + "r"+i+"p0l0.txt"))).exists())&&((new File(dirEntrada.concat(File.separator + "r"+(i+1)+"p1l0.txt"))).exists())){
							aux = x + " " + y + " " + (cpfMedTotal[i][0]+cpfMedTotal[i+1][1]) + "\n";
							out.write(aux);
						}
						x=x+2;
					}
					else{
						x=1;
						y=y+2;
					}
				}
			}
			else if (nocType.equalsIgnoreCase(NoC.HERMESTU)){
				for(int i=0,x=1,y=0; i<numRot ; i++){
					if( x< (((numRotX-1)*2) + 2)){
						if(((new File(dirEntrada.concat(File.separator + "r"+i+"p0l0.txt"))).exists())){
							aux = x + " " + y + " " + cpfMedTotal[i][0] + "\n";
							out.write(aux);
						}
						x=x+2;
					}
					else{
						i--;
						x=1;
						y=y+2;
					}
				}
			}

			// vertical channels
			if((nocType.equalsIgnoreCase(NoC.HERMESSR))||(nocType.equalsIgnoreCase(NoC.HERMES))){
				for(int i=0,x=0,y=1; i<numRot ; i++)
				{
					if(x<((numRotX-1)*2)){
						if(((new File(dirEntrada.concat(File.separator + "r"+i+"p2l0.txt"))).exists())&&((new File(dirEntrada.concat(File.separator + "r"+(i+numRotX)+"p3l0.txt"))).exists())){
							aux = x + " " + y + " " + (cpfMedTotal[i][2]+cpfMedTotal[i+numRotX][3]) + "\n";
							out.write(aux);
						}
						x=x+2;
					}
					else if(x==((numRotX-1)*2)){
						if(((new File(dirEntrada.concat(File.separator + "r"+i+"p2l0.txt"))).exists())&&((new File(dirEntrada.concat(File.separator + "r"+(i+numRotX)+"p3l0.txt"))).exists())){
							aux = x + " " + y + " " + (cpfMedTotal[i][2]+cpfMedTotal[i+numRotX][3]) + "\n";
							out.write(aux);
						}
						x=0;
						y=y+2;
					}
				}
			}
			else if (nocType.equalsIgnoreCase(NoC.HERMESTU)){
				for(int i=0,x=0,y=1; i<numRot ; i++){
					if(x<((numRotX-1)*2)){
						if(((new File(dirEntrada.concat(File.separator + "r"+i+"p2l0.txt"))).exists())){
							aux = x + " " + y + " " + cpfMedTotal[i][2] + "\n";
							out.write(aux);
						}
						x=x+2;
					}
					else if(x==((numRotX-1)*2)){
						if(((new File(dirEntrada.concat(File.separator + "r"+i+"p2l0.txt"))).exists())){
							aux = x + " " + y + " " + cpfMedTotal[i][2] + "\n";
							out.write(aux);
						}
						x=0;
						y=y+2;
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
	 * Write the DAT file that contains the data showed in the "Channel utilization" graph. 
	 * @param name The name file.
	 */
	private void writeDatChannel(String name){
		String aux = new String();
		int cont = 0;
		float[][] cargaMediaTotal = new float[numRot][numPort];
		try{
			FileOutputStream fos = new FileOutputStream(dirSaida.concat(File.separator + "reports" + File.separator + "dat" + File.separator+name+".dat"));
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			Writer out = new BufferedWriter(osw);

			//routers
			for(int y=0; y<=((numRotY-1)*2); y=y+2){
				for(int x=0; x<=((numRotX-1)*2); x=x+2){
					aux = x + " " + y + " 0\n";
					out.write(aux);
				}
			}

			//empty spaces
			if(nocType.equalsIgnoreCase(NoC.HERMESTU)){
				for(int y=1; y<=((numRotY-1)*2)+2; y=y+2){
					for(int x=1; x<=((numRotX-1)*2)+2; x=x+2){
						aux = x + " " + y + " 0\n";
						out.write(aux);
					}
				}
			}
			else if((nocType.equalsIgnoreCase(NoC.HERMESSR))||(nocType.equalsIgnoreCase(NoC.HERMES))){
				for(int y=1; y<=((numRotY-1)*2); y=y+2){
					for(int x=1; x<=((numRotX-1)*2); x=x+2){
						aux = x + " " + y + " 0\n";
						out.write(aux);
					}
				}
			}

			if((nocType.equalsIgnoreCase(NoC.HERMESSR))||(nocType.equalsIgnoreCase(NoC.HERMES))){
				for(int i=0; i<numRot; i++){
					for(int j=0; j<numPort; j++){
						for(int k=0; k<numCan; k++){
							if(canalVirt[i][j][k].getAverageThroughput() > 0){
								cont++;
								cargaMediaTotal[i][j]=(float)cargaMediaTotal[i][j]+canalVirt[i][j][k].getAverageThroughput();
							}
						}
						if(cont>0)
							cargaMediaTotal[i][j]=(float)((cargaMediaTotal[i][j])/cont)/2;
						else
							cargaMediaTotal[i][j]=0;
						cont=0;
					}
				}
			}
			else if(nocType.equalsIgnoreCase(NoC.HERMESTU)){
				for(int i=0; i<numRot; i++){
					for(int j=0; j<numPort; j++){
						for(int k=0; k<numCan; k++){
							if(canalVirt[i][j][k].getAverageThroughput() > 0){
								cont++;
								cargaMediaTotal[i][j]=(float)cargaMediaTotal[i][j]+canalVirt[i][j][k].getAverageThroughput();
							}
						}
						if(cont>0)
							cargaMediaTotal[i][j]=(float)((cargaMediaTotal[i][j])/cont);
						else
							cargaMediaTotal[i][j]=0;
						cont=0;
					}
				}
			}

			//horizontal channels
			if((nocType.equalsIgnoreCase(NoC.HERMESSR))||(nocType.equalsIgnoreCase(NoC.HERMES))){
				for(int i=0,x=1,y=0; i<numRot ; i++)
				{
					if(x<((numRotX-1)*2)){
						if(((new File(dirEntrada.concat(File.separator + "r"+i+"p0l0.txt"))).exists())&&((new File(dirEntrada.concat(File.separator + "r"+(i+1)+"p1l0.txt"))).exists())){
							aux = x + " " + y + " " + (cargaMediaTotal[i][0]+cargaMediaTotal[i+1][1]) + "\n";
							out.write(aux);
						}
						x=x+2;
					}
					else{
						x=1;
						y=y+2;
					}
				}
			}
			else if (nocType.equalsIgnoreCase(NoC.HERMESTU)){
				for(int i=0,x=1,y=0; i<numRot ; i++){
					if( x< (((numRotX-1)*2) + 2)){
						if(((new File(dirEntrada.concat(File.separator + "r"+i+"p0l0.txt"))).exists())){
							aux = x + " " + y + " " + cargaMediaTotal[i][0] + "\n";
							out.write(aux);
						}
						x=x+2;
					}
					else{
						i--;
						x=1;
						y=y+2;
					}
				}
			}

			//vertical channels
			if((nocType.equalsIgnoreCase(NoC.HERMESSR))||(nocType.equalsIgnoreCase(NoC.HERMES))){
				for(int i=0,x=0,y=1; i<numRot ; i++)
				{
					if(x<((numRotX-1)*2)){
						if(((new File(dirEntrada.concat(File.separator + "r"+i+"p2l0.txt"))).exists())&&((new File(dirEntrada.concat(File.separator + "r"+(i+numRotX)+"p3l0.txt"))).exists())){
							aux = x + " " + y + " " + (cargaMediaTotal[i][2]+cargaMediaTotal[i+numRotX][3]) + "\n";
							out.write(aux);
						}
						x=x+2;
					}
					else if(x==((numRotX-1)*2)){
						if(((new File(dirEntrada.concat(File.separator + "r"+i+"p2l0.txt"))).exists())&&((new File(dirEntrada.concat(File.separator + "r"+(i+numRotX)+"p3l0.txt"))).exists())){
							aux = x + " " + y + " " + (cargaMediaTotal[i][2]+cargaMediaTotal[i+numRotX][3]) + "\n";
							out.write(aux);
						}
						x=0;
						y=y+2;
					}
				}
			}
			else if (nocType.equalsIgnoreCase(NoC.HERMESTU)){
				for(int i=0,x=0,y=1; i<numRot ; i++){
					if(x<((numRotX-1)*2)){
						if(((new File(dirEntrada.concat(File.separator + "r"+i+"p2l0.txt"))).exists())){
							aux = x + " " + y + " " + cargaMediaTotal[i][2] + "\n";
							out.write(aux);
						}
						x=x+2;
					}
					else if(x==((numRotX-1)*2)){
						if(((new File(dirEntrada.concat(File.separator + "r"+i+"p2l0.txt"))).exists())){
							aux = x + " " + y + " " + cargaMediaTotal[i][2] + "\n";
							out.write(aux);
						}
						x=0;
						y=y+2;
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
	 * Write the DAT file that contains the data showed in the "Throughput" graph. 
	 * @param name The name file.
	 */
 	private void writeDatThroughput(String name){
		String aux = new String();
		int cont = 0;
		float[][] throughputTotal = new float[numRot][numPort];
		try{
		    FileOutputStream fos = new FileOutputStream(dirSaida.concat(File.separator + "reports" + File.separator + "dat" + File.separator+name+".dat"));
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			Writer out = new BufferedWriter(osw);

			//routers
			for(int y=0; y<=((numRotY-1)*2); y=y+2)	{
				for(int x=0; x<=((numRotX-1)*2); x=x+2){
					aux = x + " " + y + " 0\n";
					out.write(aux);
				}
			}

			//empty spaces
			if(nocType.equalsIgnoreCase(NoC.HERMESTU)){
				for(int y=1; y<=((numRotY-1)*2)+2; y=y+2){
					for(int x=1; x<=((numRotX-1)*2)+2; x=x+2){
						aux = x + " " + y + " 0\n";
						out.write(aux);
					}
				}
			}
			else if((nocType.equalsIgnoreCase(NoC.HERMESSR))||(nocType.equalsIgnoreCase(NoC.HERMES))){
				for(int y=1; y<=((numRotY-1)*2); y=y+2){
					for(int x=1; x<=((numRotX-1)*2); x=x+2){
						aux = x + " " + y + " 0\n";
						out.write(aux);
					}
				}
			}


			for(int i=0; i<numRot; i++){
				for(int j=0; j<numPort; j++){
					cont++;
					throughputTotal[i][j]= getThroughputNova(i,j,numCan);
				}
			}

			//horizontal channels
			if((nocType.equalsIgnoreCase(NoC.HERMESSR))||(nocType.equalsIgnoreCase(NoC.HERMES))){
				for(int i=0,x=1,y=0; i<numRot; i++){
					if(x<((numRotX-1)*2)){
						if(((new File(dirEntrada.concat(File.separator + "r"+i+"p0l0.txt"))).exists())&&((new File(dirEntrada.concat(File.separator + "r"+(i+1)+"p1l0.txt"))).exists())){
							aux = x + " " + y + " " + (throughputTotal[i][0]+throughputTotal[i+1][1]) + "\n";
							out.write(aux);
						}
						x=x+2;
					}
					else{
						x=1;
						y=y+2;
					}
				}
			}
			else if (nocType.equalsIgnoreCase(NoC.HERMESTU)){
				for(int i=0,x=1,y=0; i<numRot; i++){
					if( x< (((numRotX-1)*2) + 2)){
						if(((new File(dirEntrada.concat(File.separator + "r"+i+"p0l0.txt"))).exists())){
							aux = x + " " + y + " " + throughputTotal[i][0] + "\n";
							out.write(aux);
						}
						x=x+2;
					}
					else{
						i--;
						x=1;
						y=y+2;
					}
				}
			}

			//vertical channels
			if((nocType.equalsIgnoreCase(NoC.HERMESSR))||(nocType.equalsIgnoreCase(NoC.HERMES))){
				for(int i=0,x=0,y=1; i<numRot ; i++)
				{
					if(x<((numRotX-1)*2)){
						if(((new File(dirEntrada.concat(File.separator + "r"+i+"p2l0.txt"))).exists())&&((new File(dirEntrada.concat(File.separator + "r"+(i+numRotX)+"p3l0.txt"))).exists()))
						{
							aux = x + " " + y + " " + (throughputTotal[i][2]+throughputTotal[i+numRotX][3]) + "\n";
							out.write(aux);
						}
						x=x+2;
					}
					else if(x==((numRotX-1)*2)){
						if(((new File(dirEntrada.concat(File.separator + "r"+i+"p2l0.txt"))).exists())&&((new File(dirEntrada.concat(File.separator + "r"+(i+numRotX)+"p3l0.txt"))).exists())){
							aux = x + " " + y + " " + (throughputTotal[i][2]+throughputTotal[i+numRotX][3]) + "\n";
							out.write(aux);
						}
						x=0;
						y=y+2;
					}
				}
			}
			else if (nocType.equalsIgnoreCase(NoC.HERMESTU)){
				for(int i=0,x=0,y=1; i<numRot ; i++)
				{
					if(x<((numRotX-1)*2)){
						if(((new File(dirEntrada.concat(File.separator + "r"+i+"p2l0.txt"))).exists())){
							aux = x + " " + y + " " + throughputTotal[i][2] + "\n";
							out.write(aux);
						}
						x=x+2;
					}
					else if(x==((numRotX-1)*2)){
						if(((new File(dirEntrada.concat(File.separator + "r"+i+"p2l0.txt"))).exists())){
							aux = x + " " + y + " " + throughputTotal[i][2] + "\n";
							out.write(aux);
						}
						x=0;
						y=y+2;
					}
				}
			}

			out.close();
			osw.close();
			fos.close();
		}
		catch(Exception ex){
			JOptionPane.showMessageDialog(null,ex.getMessage(),"Error Message",JOptionPane.ERROR_MESSAGE);
		}
    }

 	/**
 	 * Return the throughput of a specific port.  
 	 * @param Router The router 
 	 * @param Port The port 
 	 * @param NumberOfLanes The number of virtual channels
 	 * @return The throughput.
 	 */
    private float getThroughputNova(int Router, int Port, int NumberOfLanes){
    	int intChar;
    	int FirstPacket = 0;//arbitrary number...
    	int LastPacket = 1;
    	int PacketSize = 0;
    	long NumberOfPackets=0;
    	String strAtual = new String();
    	int Lane;
    	FileInputStream fisEntrada;
    	InputStreamReader isrEntrada;
    	Reader rdrEntrada;
    	String PseudoBuffy;
		for(Lane=0;Lane<NumberOfLanes;Lane++)
		{
	    	try
			{
    			fisEntrada = new FileInputStream(dirEntrada + File.separator + "r" + Router + "p" + Port + "l" + Lane + ".txt" );
	    		isrEntrada = new InputStreamReader(fisEntrada);
	    		rdrEntrada = new BufferedReader(isrEntrada);
	    		intChar = rdrEntrada.read();
	    		PseudoBuffy = "uma especie de buffer de 50 posicoes!!! 0123456789 121 2154";
	    		strAtual="";
	    		if(intChar!='('){
	    			continue;
	    		}
    			intChar = rdrEntrada.read();
    			do
	    		{
	    			intChar = rdrEntrada.read();
	    		}while(intChar!= ' ');
	    		intChar = rdrEntrada.read();
	    		do
	    		{
	    			strAtual += (char)intChar;
	    			intChar = rdrEntrada.read();
	    		}while(intChar!= ')');
	    		if(Integer.parseInt(strAtual) < FirstPacket || Lane==0)
	    		{
	    			FirstPacket = Integer.parseInt(strAtual);
	    		}
	    		intChar = rdrEntrada.read();
	    		intChar = rdrEntrada.read();
	    		strAtual="";
	    		do
	    		{
	    			strAtual += (char)intChar;
	    			intChar = rdrEntrada.read();
	    		}while(intChar!= ' ');
	    		PacketSize = Integer.parseInt(strAtual, 16) + 2;
	    		while(intChar!=-1)
	    		{
	    			intChar = rdrEntrada.read();
	    			PseudoBuffy = PseudoBuffy.substring(1,49) + (char)intChar ;
	    			if(intChar=='\n')
	    			{
	    				++NumberOfPackets;
	    			}

	    		}

	    		PseudoBuffy = PseudoBuffy.substring(PseudoBuffy.lastIndexOf(' ') + 1, PseudoBuffy.lastIndexOf(')'));
	    		if(Integer.parseInt(PseudoBuffy) > LastPacket)
	    		{
	    			LastPacket =Integer.parseInt(PseudoBuffy);
	    		}
	    		rdrEntrada.close();
	    		isrEntrada.close();
	    		fisEntrada.close();
			}
	    	catch(FileNotFoundException hihi)
			{
	    		continue;
	    	}
	    	catch (Exception ex)
			{
	    		JOptionPane.showMessageDialog(null,"Error in Router = " + Router + " Port = " + Port + " VC = " + Lane + " \n"+ ex.getMessage(),"Error Message",JOptionPane.ERROR_MESSAGE);
	    	}
    	}
    	return PacketSize*NumberOfPackets*numBitsFlit/(LastPacket - FirstPacket);
    }

}
