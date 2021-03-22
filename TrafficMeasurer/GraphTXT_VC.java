package TrafficMeasurer;

import java.io.*;

import javax.swing.*;

import AtlasPackage.NoC;
import AtlasPackage.Project;

/**
 * This class is used by NoCs with virtual channel to generate TXT graph and show it using GUI.
 * @author Aline Vieira de Mello
 * @version
 */
public class GraphTXT_VC{

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
    public GraphTXT_VC(Project project){
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
		int cont, cont_aux;
		String espaco = "        ";
		String name = dirSaida.concat(File.separator + "reports" + File.separator + "graphs" + File.separator + "graf_carga_virt.txt");
		try{
			FileOutputStream fos = new FileOutputStream(name);
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			Writer out = new BufferedWriter(osw);
			for(int i=0; i<numRotY; i++)
			{
				cont = (numRotX*numRotY)-(numRotX*(i+1));

				for(int j=0; j<numRotX; j++)
				{
					if(j==0){

						if(nocType.equalsIgnoreCase(NoC.HERMESTU)){
							if (i==0){
								cont_aux = (numRotX*numRotY)-(numRotX*(i+1));
								for(int m=0; m<numRotX; m++){
									for(int n=0; n<numCan; n++){
										if(n==numCan-1){
											if(numCan==1) aux = aux + geraCargaCV(cont_aux,2,n);
											else aux = aux + geraCargaCV(cont_aux,2,n);
											for(int a=0;a<((numCan*4)+(numCan-1));a++);
										}
										else{
											if(m==0) aux = aux + geraCargaCV(cont_aux,2,n) + "-";
											else aux = aux + espaco + espaco + espaco + espaco + espaco + espaco + "  " + geraCargaCV(cont_aux,2,n) + "-";
										}
									}
									cont_aux++;
								}
								aux = aux + "\n";
							}
						}

						if(cont<10) aux = aux + espaco + "(00"+cont+")";
						else if(cont<100) aux = aux + espaco + "(0"+cont+")";
						else if(cont<1000) aux = aux + espaco + "("+cont+")";
						for(int k=0; k<numCan; k++){
							if(k==0) aux = aux + geraCargaCV(cont,0,k);
							else if(k==numCan-1) aux = aux + "-" + geraCargaCV(cont,0,k) + espaco + espaco;
							else aux = aux + "-" + geraCargaCV(cont,0,k) + espaco;
						}
					}
					else if(j!=numRotX-1){

						if(nocType.equalsIgnoreCase(NoC.HERMESTU)){
							aux=aux+espaco+espaco+espaco;
						}
						else if((nocType.equalsIgnoreCase(NoC.HERMESSR))||(nocType.equalsIgnoreCase(NoC.HERMES))){
							for(int k=0; k<numCan; k++){
								if(k==0) aux = aux + geraCargaCV(cont,1,k);
								else if(k==numCan-1) aux = aux + "-" + geraCargaCV(cont,1,k);
								else aux = aux + "-" + geraCargaCV(cont,1,k);
							}
						}

						if(cont<10) aux = aux + "(00"+cont+")";
						else if(cont<100) aux = aux + "(0"+cont+")";
						else if(cont<1000) aux = aux + "("+cont+")";

						for(int k=0; k<numCan; k++){
							if(k==0) aux = aux + geraCargaCV(cont,0,k);
							else if(k==numCan-1) aux = aux + "-" + geraCargaCV(cont,0,k) + espaco + espaco;
							else aux = aux + "-" + geraCargaCV(cont,0,k) ;
						}
					}
					else{

						if(nocType.equalsIgnoreCase(NoC.HERMESTU)){
							aux=aux+espaco+espaco+espaco;
						}
						else if((nocType.equalsIgnoreCase(NoC.HERMESSR))||(nocType.equalsIgnoreCase(NoC.HERMES))){
							for(int k=0; k<numCan; k++){
								if(k==0) aux = aux + geraCargaCV(cont,1,k);
								else if(k==numCan-1) aux = aux + "-" + geraCargaCV(cont,1,k);
								else aux = aux + "-" + geraCargaCV(cont,1,k);
							}
						}
						if(cont<10) aux = aux + "(00"+cont+")";
						else if(cont<100) aux = aux + "(0"+cont+")";
						else if(cont<1000) aux = aux + "("+cont+")";

						if(nocType.equalsIgnoreCase(NoC.HERMESTU)){
							for(int k=0; k<numCan; k++){
								if(k==0) aux = aux + geraCargaCV(cont,0,k);
								else if(k==numCan-1) aux = aux + "-" + geraCargaCV(cont,0,k) + espaco + espaco;
								else aux = aux + "-" + geraCargaCV(cont,0,k) ;
							}
						}
					}
					cont++;
				}
				out.write(aux);
				aux = "\n";
				cont = (numRotX*numRotY)-(numRotX*(i+1));
				if((nocType.equalsIgnoreCase(NoC.HERMESSR))||(nocType.equalsIgnoreCase(NoC.HERMES))){
					for(int j=0; j<numRotX; j++){
						for(int k=0; k<numCan; k++){
							if(k==numCan-1){
								if(numCan==1) aux = aux + geraCargaCV(cont,3,k);
								else aux = aux + geraCargaCV(cont,3,k);
								for(int a=0;a<((numCan*4)+(numCan-1));a++);
							}
							else{
								if(j==0) aux = aux + geraCargaCV(cont,3,k) + "-";
								else aux = aux + espaco + espaco + espaco + espaco + espaco + espaco + "  " + geraCargaCV(cont,3,k) + "-";
							}
						}
						cont++;
					}
				}
				aux = aux + "\n\n";
				cont = (numRotX*(numRotY-1))-(numRotX*(i+1));
				if(cont>=0){
					for(int j=0; j<numRotX; j++){
						for(int k=0; k<numCan; k++){
							if(k==numCan-1){
								if(numCan==1) aux = aux + geraCargaCV(cont,2,k);
								else aux = aux + geraCargaCV(cont,2,k);
								for(int a=0;a<((numCan*4)+(numCan-1));a++);
							}
							else{
								if(j==0) aux = aux + geraCargaCV(cont,2,k) + "-";
								else aux = aux + espaco + espaco + espaco + espaco + espaco + espaco + "  " + geraCargaCV(cont,2,k) + "-";
							}
						}
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

	private String geraCargaCV(int i, int j, int k){
		int carga=0;
		String str="";

		carga = canalVirt[i][j][k].getNumberOfFlits();

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
		int cont, cont_aux;
		String name = dirSaida.concat(File.separator + "reports" + File.separator + "graphs" + File.separator + "graf_cpf_virt.txt");
		String espaco = "        ";
		try{
			FileOutputStream fos = new FileOutputStream(dirSaida.concat(File.separator + "reports" + File.separator + "graphs" + File.separator + "graf_cpf_virt.txt"));
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			Writer out = new BufferedWriter(osw);
			for(int i=0; i<numRotY; i++)
			{

				cont = (numRotX*numRotY)-(numRotX*(i+1));
				for(int j=0; j<numRotX; j++)
				{
					//escrita dos valores m�ximos de CPF
					if(j==0){
						if (nocType.equalsIgnoreCase(NoC.HERMESTU)){
							if (i==0){//para imprimir soh na primeira linha
								cont_aux=(numRotX*numRotY)-(numRotX*(i+1));
								if(cont_aux>=0){
									for(int m=0; m<numRotX; m++){
										for(int n=0; n<numCan; n++){
											if(n==numCan-1){
												if(numCan==1) aux = aux + geraCpfCV(cont_aux,2,n,"max") + espaco + espaco + espaco + espaco + espaco + espaco + "    ";
												else aux = aux + geraCpfCV(cont_aux,2,n,"max") + espaco + espaco + espaco + espaco + espaco + espaco + "    ";
												for(int a=0;a<((numCan*4)+(numCan-1));a++) aux = aux + " ";
											}
											else aux = aux + geraCpfCV(cont_aux,2,n,"max") + "-";
										}
										cont_aux++;
									}
									aux = aux + "\n";
								}
								cont_aux = (numRotX*(numRotY-1))-(numRotX*(i+1));
								if(cont_aux>=0){
									for(int m=0; m<numRotX; m++){
										for(int n=0; n<numCan; n++){
											if(n==numCan-1){
												if(numCan==1) aux = aux + geraCpfCV(cont_aux,2,n,"med") + espaco + espaco + espaco + espaco + espaco + espaco + "    ";
												else aux = aux + geraCpfCV(cont_aux,2,n,"med") + espaco + espaco + espaco + espaco + espaco + espaco + "    ";
												for(int a=0;a<((numCan*4)+(numCan-1));a++) aux = aux + " ";
											}
											else aux = aux + geraCpfCV(cont_aux,2,n,"med") + "-";
										}
										cont_aux++;
									}
									aux = aux + "\n";
								}
								cont_aux = (numRotX*(numRotY-1))-(numRotX*(i+1));
								if(cont_aux>=0){
									for(int m=0; m<numRotX; m++){
										for(int n=0; n<numCan; n++){
											if(n==numCan-1){
												if(numCan==1) aux = aux + geraCpfCV(cont_aux,2,n,"min") + espaco + espaco + espaco + espaco + espaco + espaco + "    ";
												else aux = aux + geraCpfCV(cont_aux,2,n,"min") + espaco + espaco + espaco + espaco + espaco + espaco + "    ";
												for(int a=0;a<((numCan*4)+(numCan-1));a++) aux = aux + " ";
											}
											else aux = aux + geraCpfCV(cont_aux,2,n,"min") + "-";
										}
										cont_aux++;
									}
									aux = aux + "\n";
								}
							}
						}

						aux = aux + espaco + espaco + espaco + "   ";
						for(int k=0; k<numCan; k++){
							if(k==0) aux = aux + geraCpfCV(cont,0,k,"max");
							else if(k==numCan-1) aux = aux + "-" + geraCpfCV(cont,0,k,"max") + espaco;
							else aux = aux + "-" + geraCpfCV(cont,0,k,"max");
						}
					}//fim if(j==0)

					else if(j!=numRotX-1){

						if (nocType.equalsIgnoreCase(NoC.HERMESTU)){
							aux = aux + espaco + espaco + espaco + espaco + espaco + espaco + "    ";
						}
						else if((nocType.equalsIgnoreCase(NoC.HERMESSR))||(nocType.equalsIgnoreCase(NoC.HERMES))){

							for(int k=0; k<numCan; k++){
								if(k==0) aux = aux + geraCpfCV(cont,1,k,"max");
								else if(k==numCan-1) aux = aux + "-" + geraCpfCV(cont,1,k,"max") + espaco + espaco + espaco + espaco + " ";
								else aux = aux + "-" + geraCpfCV(cont,1,k,"max");
							}
						}

						for(int k=0; k<numCan; k++){
							if(k==0) aux = aux + geraCpfCV(cont,0,k,"max");
							else if(k==numCan-1) aux = aux + "-" + geraCpfCV(cont,0,k,"max") + espaco;
							else aux = aux + "-" + geraCpfCV(cont,0,k,"max");
						}

					}
					else{

						if (nocType.equalsIgnoreCase(NoC.HERMESTU)){
							aux = aux + espaco + espaco + espaco + espaco + espaco + espaco + "    ";
							for(int k=0; k<numCan; k++){
								if(k==0) aux = aux + geraCpfCV(cont,0,k,"max");
								else if(k==numCan-1) aux = aux + "-" + geraCpfCV(cont,0,k,"max") + espaco;
								else aux = aux + "-" + geraCpfCV(cont,0,k,"max");
							}
						}
						else if((nocType.equalsIgnoreCase(NoC.HERMESSR))||(nocType.equalsIgnoreCase(NoC.HERMES))){
							for(int k=0; k<numCan; k++){
								if(k==0) aux = aux + geraCpfCV(cont,1,k,"max");
								else if(k==numCan-1) aux = aux + "-" + geraCpfCV(cont,1,k,"max") + espaco + espaco + espaco + espaco + " ";
								else aux = aux + "-" + geraCpfCV(cont,1,k,"max");
							}
						}
					}
					cont++;
				}
				aux = aux + "\n";

				cont = (numRotX*numRotY)-(numRotX*(i+1));
				for(int j=0; j<numRotX; j++)
				{
					//escrita dos valores m�dios de CPF
					if(j==0){
						if(cont<10) aux = aux + "     " + "(00"+cont+")" + espaco + "    ";
						else if(cont<100) aux = aux + "     " + "(0"+cont+")" + espaco + "    ";
						else if(cont<1000) aux = aux + "     " + "("+cont+")" + espaco + "    ";
						for(int k=0; k<numCan; k++){
							if(k==0) aux = aux + geraCpfCV(cont,0,k,"med");
							else if(k==numCan-1) aux = aux + "-" + geraCpfCV(cont,0,k,"med") + espaco;
							else aux = aux + "-" + geraCpfCV(cont,0,k,"med");
						}
					}
					else if(j!=numRotX-1){

						if (nocType.equalsIgnoreCase(NoC.HERMESTU)){
							aux = aux + espaco + espaco + "   ";
						}
						else if((nocType.equalsIgnoreCase(NoC.HERMESSR))||(nocType.equalsIgnoreCase(NoC.HERMES))){
							for(int k=0; k<numCan; k++){
								if(k==0) aux = aux + geraCpfCV(cont,1,k,"med");
								else if(k==numCan-1) aux = aux + "-" + geraCpfCV(cont,1,k,"med");
								else aux = aux + "-" + geraCpfCV(cont,1,k,"med");
							}
						}

						if(cont<10) aux = aux + espaco + "    " + "(00"+cont+")" + espaco + "    ";
						else if(cont<100) aux = aux + espaco + "    " + "(0"+cont+")" + espaco + "    ";
						else if(cont<1000) aux = aux + espaco + "    " + "("+cont+")" + espaco + "    ";
						for(int k=0; k<numCan; k++){
							if(k==0) aux = aux + geraCpfCV(cont,0,k,"med");
							else if(k==numCan-1) aux = aux + "-" + geraCpfCV(cont,0,k,"med") + espaco;
							else aux = aux + "-" + geraCpfCV(cont,0,k,"med");
						}
					}
					else{

						if (nocType.equalsIgnoreCase(NoC.HERMESTU)){
							aux = aux + espaco + espaco + "   ";
						}

						else if((nocType.equalsIgnoreCase(NoC.HERMESSR))||(nocType.equalsIgnoreCase(NoC.HERMES))){
							for(int k=0; k<numCan; k++){
								if(k==0) aux = aux + geraCpfCV(cont,1,k,"med");
								else if(k==numCan-1) aux = aux + "-" + geraCpfCV(cont,1,k,"med");
								else aux = aux + "-" + geraCpfCV(cont,1,k,"med");
							}
						}

						if(cont<10) aux = aux + espaco + "    " + "(00"+cont+")"+ espaco + "   ";
						else if(cont<100) aux = aux + espaco + "    " + "(0"+cont+")" + espaco + "   ";
						else if(cont<1000) aux = aux + espaco + "    " + "("+cont+")" + espaco + "   ";

						if (nocType.equalsIgnoreCase(NoC.HERMESTU)){
							for(int k=0; k<numCan; k++){
								if(k==0) aux = aux + geraCpfCV(cont,0,k,"med");
								else if(k==numCan-1) aux = aux + "-" + geraCpfCV(cont,0,k,"med") + espaco;
								else aux = aux + "-" + geraCpfCV(cont,0,k,"med");
							}
						}


					}
					cont++;
				}
				aux = aux + "\n" + espaco + espaco + espaco + "   ";

				cont = (numRotX*numRotY)-(numRotX*(i+1));
				for(int j=0; j<numRotX; j++)
				{
					//escrita dos valores m�nimos de CPF
					if(j==0){
						for(int k=0; k<numCan; k++){
							if(k==0) aux = aux + geraCpfCV(cont,0,k,"min");
							else if(k==numCan-1) aux = aux + "-" + geraCpfCV(cont,0,k,"min") + espaco;
							else aux = aux + "-" + geraCpfCV(cont,0,k,"min");
						}
					}
					else if(j!=numRotX-1){

						if (nocType.equalsIgnoreCase(NoC.HERMESTU)){
							aux = aux + espaco + espaco + espaco + espaco + espaco + espaco + "    ";
						}
						else if((nocType.equalsIgnoreCase(NoC.HERMESSR))||(nocType.equalsIgnoreCase(NoC.HERMES))){
							for(int k=0; k<numCan; k++){
								if(k==0) aux = aux + geraCpfCV(cont,1,k,"min");
								else if(k==numCan-1) aux = aux + "-" + geraCpfCV(cont,1,k,"min") + espaco + espaco + espaco + espaco + " ";
								else aux = aux + "-" + geraCpfCV(cont,1,k,"min");
							}
						}

						for(int k=0; k<numCan; k++){
							if(k==0) aux = aux + geraCpfCV(cont,0,k,"min");
							else if(k==numCan-1) aux = aux + "-" + geraCpfCV(cont,0,k,"min") + espaco;
							else aux = aux + "-" + geraCpfCV(cont,0,k,"min");
						}

					}
					else{
						if (nocType.equalsIgnoreCase(NoC.HERMESTU)){
							aux = aux + espaco + espaco + espaco + espaco + espaco + espaco + "    ";
						}
						else if((nocType.equalsIgnoreCase(NoC.HERMESSR))||(nocType.equalsIgnoreCase(NoC.HERMES))){
							for(int k=0; k<numCan; k++){
								if(k==0) aux = aux + geraCpfCV(cont,1,k,"min");
								else if(k==numCan-1) aux = aux + "-" + geraCpfCV(cont,1,k,"min") + espaco + espaco + espaco + espaco + " ";
								else aux = aux + "-" + geraCpfCV(cont,1,k,"min");
							}
						}

						if (nocType.equalsIgnoreCase(NoC.HERMESTU)){
							for(int k=0; k<numCan; k++){
								if(k==0) aux = aux + geraCpfCV(cont,0,k,"min");
								else if(k==numCan-1) aux = aux + "-" + geraCpfCV(cont,0,k,"min") + espaco;
								else aux = aux + "-" + geraCpfCV(cont,0,k,"min");
							}
						}



					}
					cont++;
				}
				out.write(aux);
				aux = "\n";
				cont = (numRotX*numRotY)-(numRotX*(i+1));


				if (nocType.equalsIgnoreCase(NoC.HERMESTU)){
					aux = aux + espaco + espaco + espaco + espaco + espaco + espaco + "    ";
				}
				else if((nocType.equalsIgnoreCase(NoC.HERMESSR))||(nocType.equalsIgnoreCase(NoC.HERMES))){
					for(int j=0; j<numRotX; j++){
						for(int k=0; k<numCan; k++){
							if(k==numCan-1){
								if(numCan==1) aux = aux + geraCpfCV(cont,3,k,"max") + espaco + espaco + espaco + espaco + espaco + espaco + espaco + "    ";
								else aux = aux + geraCpfCV(cont,3,k,"max") + espaco + espaco + espaco + espaco + espaco + espaco + "    ";
								for(int a=0;a<((numCan*4)+(numCan-1));a++) aux = aux + " ";
							}
							else aux = aux + geraCpfCV(cont,3,k,"max") + "-";
						}
						cont++;
					}
				}

				aux = aux + "\n";
				cont = (numRotX*numRotY)-(numRotX*(i+1));

				if (nocType.equalsIgnoreCase(NoC.HERMESTU)){
					aux = aux + espaco + espaco + espaco + espaco + espaco + espaco + "    ";
				}
				else if((nocType.equalsIgnoreCase(NoC.HERMESSR))||(nocType.equalsIgnoreCase(NoC.HERMES))){
					for(int j=0; j<numRotX; j++){
						for(int k=0; k<numCan; k++){
							if(k==numCan-1){
								if(numCan==1) aux = aux + geraCpfCV(cont,3,k,"med") + espaco + espaco + espaco + espaco + espaco + espaco + espaco + "    ";
								else aux = aux + geraCpfCV(cont,3,k,"med") + espaco + espaco + espaco + espaco + espaco + espaco + "    ";
								for(int a=0;a<((numCan*4)+(numCan-1));a++) aux = aux + " ";
							}
							else aux = aux + geraCpfCV(cont,3,k,"med") + "-";
						}
						cont++;
					}
				}

				aux = aux + "\n";
				cont = (numRotX*numRotY)-(numRotX*(i+1));

				if (nocType.equalsIgnoreCase(NoC.HERMESTU)){
					aux = aux + espaco + espaco + espaco + espaco + espaco + espaco + "    ";
				}
				else if((nocType.equalsIgnoreCase(NoC.HERMESSR))||(nocType.equalsIgnoreCase(NoC.HERMES))){
					for(int j=0; j<numRotX; j++){
						for(int k=0; k<numCan; k++){
							if(k==numCan-1){
								if(numCan==1) aux = aux + geraCpfCV(cont,3,k,"min") + espaco + espaco + espaco + espaco + espaco + espaco + "    ";
								else aux = aux + geraCpfCV(cont,3,k,"min") + espaco + espaco + espaco + espaco + espaco + espaco + "    ";
								for(int a=0;a<((numCan*4)+(numCan-1));a++) aux = aux + " ";
							}
							else aux = aux + geraCpfCV(cont,3,k,"min") + "-";
						}
						cont++;
					}
				}

				aux = aux + "\n\n";
				cont = (numRotX*(numRotY-1))-(numRotX*(i+1));

				/********************************************************************
				* valores porta norte
				*********************************************************************/
				if(cont>=0){
					for(int j=0; j<numRotX; j++){
						for(int k=0; k<numCan; k++){
							if(k==numCan-1){
								if(numCan==1) aux = aux + geraCpfCV(cont,2,k,"max") + espaco + espaco + espaco + espaco + espaco + espaco + "    ";
								else aux = aux + geraCpfCV(cont,2,k,"max") + espaco + espaco + espaco + espaco + espaco + espaco + "    ";
								for(int a=0;a<((numCan*4)+(numCan-1));a++) aux = aux + " ";
							}
							else aux = aux + geraCpfCV(cont,2,k,"max") + "-";
						}
						cont++;
					}
					aux = aux + "\n";
				}
				cont = (numRotX*(numRotY-1))-(numRotX*(i+1));
				if(cont>=0){
					for(int j=0; j<numRotX; j++){
						for(int k=0; k<numCan; k++){
							if(k==numCan-1){
								if(numCan==1) aux = aux + geraCpfCV(cont,2,k,"med") + espaco + espaco + espaco + espaco + espaco + espaco + "    ";
								else aux = aux + geraCpfCV(cont,2,k,"med") + espaco + espaco + espaco + espaco + espaco + espaco + "    ";
								for(int a=0;a<((numCan*4)+(numCan-1));a++) aux = aux + " ";
							}
							else aux = aux + geraCpfCV(cont,2,k,"med") + "-";
						}
						cont++;
					}
					aux = aux + "\n";
				}
				cont = (numRotX*(numRotY-1))-(numRotX*(i+1));
				if(cont>=0){
					for(int j=0; j<numRotX; j++){
						for(int k=0; k<numCan; k++){
							if(k==numCan-1){
								if(numCan==1) aux = aux + geraCpfCV(cont,2,k,"min") + espaco + espaco + espaco + espaco + espaco + espaco + "    ";
								else aux = aux + geraCpfCV(cont,2,k,"min") + espaco + espaco + espaco + espaco + espaco + espaco + "    ";
								for(int a=0;a<((numCan*4)+(numCan-1));a++) aux = aux + " ";
							}
							else aux = aux + geraCpfCV(cont,2,k,"min") + "-";
						}
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

    private String geraCpfCV(int i, int j, int k, String type){
		float cpf=0;
		String str;

		if(type == "min"){
    		cpf = canalVirt[i][j][k].getMinimalCPF();
		}
		else if(type == "med"){
            cpf = canalVirt[i][j][k].getAverageCPF();
		}
		else if(type == "max"){
            cpf = canalVirt[i][j][k].getMaximalCPF();
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
		int cont, cont_aux;
		String name = dirSaida.concat(File.separator + "reports" + File.separator + "graphs" + File.separator + "graf_carga_media_virt.txt");
		String espaco = "        ";
		try{
			FileOutputStream fos = new FileOutputStream(dirSaida.concat(File.separator + "reports" + File.separator + "graphs" + File.separator + "graf_carga_media_virt.txt"));
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			Writer out = new BufferedWriter(osw);
			for(int i=0; i<numRotY; i++)
			{
				cont = (numRotX*numRotY)-(numRotX*(i+1));
				for(int j=0; j<numRotX; j++)
				{
					if(j==0){

						if(nocType.equalsIgnoreCase(NoC.HERMESTU)){
							if (i==0){
								cont_aux = (numRotX*numRotY)-(numRotX*(i+1));
								for(int m=0; m<numRotX; m++){
									for(int n=0; n<numCan; n++){
										if(n==numCan-1){
											if(numCan==1) aux = aux + geraCargaMediaCV(cont_aux,2,n);
											else aux = aux + geraCargaMediaCV(cont_aux,2,n);
											for(int a=0;a<((numCan*4)+(numCan-1));a++);
										}
										else{
											if(m==0) aux = aux + geraCargaMediaCV(cont_aux,2,n) + "-";
											else aux = aux + espaco + espaco + espaco + espaco + espaco + espaco + "   " + geraCargaMediaCV(cont_aux,2,n) + "-";
										}
									}
									cont_aux++;
								}
								aux = aux + "\n";
							}
						}

						if(cont<10) aux = aux + "    " + "(00"+cont+")";
						else if(cont<100) aux = aux + "    " + "(0"+cont+")";
						else if(cont<1000) aux = aux + "    " + "("+cont+")";
						for(int k=0; k<numCan; k++){
							if(k==0) aux = aux + geraCargaMediaCV(cont,0,k);
							else if(k==numCan-1) aux = aux + "-" + geraCargaMediaCV(cont,0,k) + espaco + espaco;
							else aux = aux + "-" + geraCargaMediaCV(cont,0,k);
						}
					}
					else if(j!=numRotX-1){

						if (nocType.equalsIgnoreCase(NoC.HERMESTU)){
							aux=aux+espaco+espaco+espaco;
						}
						else if((nocType.equalsIgnoreCase(NoC.HERMESSR))||(nocType.equalsIgnoreCase(NoC.HERMES))){
							for(int k=0; k<numCan; k++){
								if(k==0) aux = aux + geraCargaMediaCV(cont,1,k);
								else if(k==numCan-1) aux = aux + "-" + geraCargaMediaCV(cont,1,k);
								else aux = aux + "-" + geraCargaMediaCV(cont,1,k);
							}
						}

						if(cont<10) aux = aux + "(00"+cont+")";
						else if(cont<100) aux = aux + "(0"+cont+")";
						else if(cont<1000) aux = aux + "("+cont+")";

						for(int k=0; k<numCan; k++){
							if(k==0) aux = aux + geraCargaMediaCV(cont,0,k);
							else if(k==numCan-1) aux = aux + "-" + geraCargaMediaCV(cont,0,k) + espaco + espaco;
							else aux = aux + "-" + geraCargaMediaCV(cont,0,k);
						}
					}
					else{

						if (nocType.equalsIgnoreCase(NoC.HERMESTU)){
							aux=aux+espaco+espaco+espaco;
						}
						else if((nocType.equalsIgnoreCase(NoC.HERMESSR))||(nocType.equalsIgnoreCase(NoC.HERMES))){
							for(int k=0; k<numCan; k++){
								if(k==0) aux = aux + geraCargaMediaCV(cont,1,k);
								else if(k==numCan-1) aux = aux + "-" + geraCargaMediaCV(cont,1,k);
								else aux = aux + "-" + geraCargaMediaCV(cont,1,k);
							}
						}

						if(cont<10) aux = aux + "(00"+cont+")";
						else if(cont<100) aux = aux + "(0"+cont+")";
						else if(cont<1000) aux = aux + "("+cont+")";

						if (nocType.equalsIgnoreCase(NoC.HERMESTU)){
							for(int k=0; k<numCan; k++){
								if(k==0) aux = aux + geraCargaMediaCV(cont,0,k);
								else if(k==numCan-1) aux = aux + "-" + geraCargaMediaCV(cont,0,k) + espaco + espaco;
								else aux = aux + "-" + geraCargaMediaCV(cont,0,k);
							}
						}
					}
					cont++;
				}
				out.write(aux);
				aux = "\n";
				cont = (numRotX*numRotY)-(numRotX*(i+1));

				if((nocType.equalsIgnoreCase(NoC.HERMESSR))||(nocType.equalsIgnoreCase(NoC.HERMES))){
					for(int j=0; j<numRotX; j++){
						for(int k=0; k<numCan; k++){
							if(k==numCan-1){
								if(numCan==1) aux = aux + geraCargaMediaCV(cont,3,k) + "     ";
								else aux = aux + geraCargaMediaCV(cont,3,k) + "        ";
								for(int a=0;a<((numCan*4)+(numCan-1));a++) aux = aux + " ";
							}
							else{
								if(j==0) aux = aux + geraCargaMediaCV(cont,3,k) + "-";
								else aux = aux + espaco + espaco + espaco + espaco + espaco + espaco + "  " + geraCargaMediaCV(cont,3,k) + "-";
							}
						}
						cont++;
					}
				}

				aux = aux + "\n\n";
				cont = (numRotX*(numRotY-1))-(numRotX*(i+1));
				if(cont>=0){
					for(int j=0; j<numRotX; j++){
						for(int k=0; k<numCan; k++){
							if(k==numCan-1){
								if(numCan==1) aux = aux + geraCargaMediaCV(cont,2,k) + "     ";
								else aux = aux + geraCargaMediaCV(cont,2,k) + "        ";
								for(int a=0;a<((numCan*4)+(numCan-1));a++) aux = aux + " ";
							}
							else{
								if(j==0) aux = aux + geraCargaMediaCV(cont,2,k) + "-";
								else aux = aux + espaco + espaco + espaco + espaco + "  " + geraCargaMediaCV(cont,2,k) + "-";
							}
						}
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
			JOptionPane.showMessageDialog(null,ex.getMessage(),"Error Message",JOptionPane.ERROR_MESSAGE);
		}
		return name;
    }

    private String geraCargaMediaCV(int i, int j, int k){
		float cargaMedia=0;
		String str;

		cargaMedia = canalVirt[i][j][k].getAverageThroughput();

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
		int cont, cont_aux;
		String name = dirSaida.concat(File.separator + "reports" + File.separator + "graphs" + File.separator + "graf_throughput_virt.txt");
		String espaco = "        ";
		try{
			FileOutputStream fos = new FileOutputStream(dirSaida.concat(File.separator + "reports" + File.separator + "graphs" + File.separator + "graf_throughput_virt.txt"));
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			Writer out = new BufferedWriter(osw);
			for(int i=0; i<numRotY; i++)
			{
				cont = (numRotX*numRotY)-(numRotX*(i+1));
				for(int j=0; j<numRotX; j++)
				{
					if(j==0){

						if(nocType.equalsIgnoreCase(NoC.HERMESTU)){
							if (i==0){
								cont_aux = (numRotX*numRotY)-(numRotX*(i+1));
								for(int m=0; m<numRotX; m++){
									for(int n=0; n<numCan; n++){
										if(n==numCan-1){
											if(numCan==1) aux = aux + geraThroughputCV(cont_aux,2,n);
											else aux = aux + geraThroughputCV(cont_aux,2,n);
											for(int a=0;a<((numCan*4)+(numCan-1));a++);
										}
										else{
											if(m==0) aux = aux + geraThroughputCV(cont_aux,2,n) + "-";
											else aux = aux + espaco + espaco + espaco + espaco + espaco + espaco + "   " + geraThroughputCV(cont_aux,2,n) + "-";
										}
									}
									cont_aux++;
								}
								aux = aux + "\n";
							}
						}

						if(cont<10) aux = aux + "    " + "(00"+cont+")";
						else if(cont<100) aux = aux + "    " + "(0"+cont+")";
						else if(cont<1000) aux = aux + "    " + "("+cont+")";
						for(int k=0; k<numCan; k++){
							if(k==0) aux = aux + geraThroughputCV(cont,0,k);
							else if(k==numCan-1) aux = aux + "-" + geraThroughputCV(cont,0,k) + espaco + espaco;
							else aux = aux + "-" + geraThroughputCV(cont,0,k);
						}
					}
					else if(j!=numRotX-1){

						if (nocType.equalsIgnoreCase(NoC.HERMESTU)){
							aux=aux+espaco+espaco+espaco;
						}
						else if((nocType.equalsIgnoreCase(NoC.HERMESSR))||(nocType.equalsIgnoreCase(NoC.HERMES))){
							for(int k=0; k<numCan; k++){
								if(k==0) aux = aux + geraThroughputCV(cont,1,k);
								else if(k==numCan-1) aux = aux + "-" + geraThroughputCV(cont,1,k);
								else aux = aux + "-" + geraThroughputCV(cont,1,k);
							}
						}

						if(cont<10) aux = aux + "(00"+cont+")";
						else if(cont<100) aux = aux + "(0"+cont+")";
						else if(cont<1000) aux = aux + "("+cont+")";
						for(int k=0; k<numCan; k++){
							if(k==0) aux = aux + geraThroughputCV(cont,0,k);
							else if(k==numCan-1) aux = aux + "-" + geraThroughputCV(cont,0,k) + espaco + espaco;
							else aux = aux + "-" + geraThroughputCV(cont,0,k);
						}
					}
					else{

						if (nocType.equalsIgnoreCase(NoC.HERMESTU)){
							aux=aux+espaco+espaco+espaco;
						}
						else if((nocType.equalsIgnoreCase(NoC.HERMESSR))||(nocType.equalsIgnoreCase(NoC.HERMES))){
							for(int k=0; k<numCan; k++){
								if(k==0) aux = aux + geraThroughputCV(cont,1,k);
								else if(k==numCan-1) aux = aux + "-" + geraThroughputCV(cont,1,k);
								else aux = aux + "-" + geraThroughputCV(cont,1,k);
							}
						}
						if(cont<10) aux = aux + "(00"+cont+")";
						else if(cont<100) aux = aux + "(0"+cont+")";
						else if(cont<1000) aux = aux + "("+cont+")";

						if (nocType.equalsIgnoreCase(NoC.HERMESTU)){
							for(int k=0; k<numCan; k++){
								if(k==0) aux = aux + geraThroughputCV(cont,0,k);
								else if(k==numCan-1) aux = aux + "-" + geraThroughputCV(cont,0,k) + espaco + espaco;
								else aux = aux + "-" + geraThroughputCV(cont,0,k);
							}
						}
					}
					cont++;
				}
				out.write(aux);
				aux = "\n";
				cont = (numRotX*numRotY)-(numRotX*(i+1));

				if((nocType.equalsIgnoreCase(NoC.HERMESSR))||(nocType.equalsIgnoreCase(NoC.HERMES))){
					for(int j=0; j<numRotX; j++){
						for(int k=0; k<numCan; k++){
							if(k==numCan-1){
								if(numCan==1) aux = aux + geraThroughputCV(cont,3,k) + "     ";
								else aux = aux + geraThroughputCV(cont,3,k) + "        ";
								for(int a=0;a<((numCan*4)+(numCan-1));a++) aux = aux + " ";
							}
							else{
								if(j==0) aux = aux + geraThroughputCV(cont,3,k) + "-";
								else aux = aux + espaco + espaco + espaco + "  " + geraThroughputCV(cont,3,k) + "-";
							}
						}
						cont++;
					}
				}

				aux = aux + "\n\n";
				cont = (numRotX*(numRotY-1))-(numRotX*(i+1));
				if(cont>=0){
					for(int j=0; j<numRotX; j++){
						for(int k=0; k<numCan; k++){
							if(k==numCan-1){
								if(numCan==1) aux = aux + geraThroughputCV(cont,2,k) + "     ";
								else aux = aux + geraThroughputCV(cont,2,k) + "        ";
								for(int a=0;a<((numCan*4)+(numCan-1));a++) aux = aux + " ";
							}
							else{
								if(j==0) aux = aux + geraThroughputCV(cont,2,k) + "-";
								else aux = aux + espaco + espaco + espaco + espaco + "  " + geraThroughputCV(cont,2,k) + "-";
							}
						}
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
			JOptionPane.showMessageDialog(null,ex.getMessage(),"Error Message",JOptionPane.ERROR_MESSAGE);
		}
		return name;
	}

    private String geraThroughputCV(int i, int j, int k){
		float throughput=0;
		String str;

		throughput = canalVirt[i][j][k].getBitsPerCycle();

		if(throughput==0) return "0.00";
		Float t = new Float(throughput);
		str = t.toString();
		while(str.length()<4) str = str + "0";
		return str.substring(0,4);
    }
}
