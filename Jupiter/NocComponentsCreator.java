package Jupiter;

import java.io.*;
import javax.swing.*;
import java.util.*;
import java.nio.channels.*;

import AtlasPackage.Default;

/**
 * Create a Mercury NoC.
 * @author Gerson Scartezzini
 * @author Tiago Baptista Noronha
 * @version
 *
 */
public class NocComponentsCreator {

	private static String sourcePath = Default.atlashome + File.separator + "Jupiter" + File.separator + "Data" + File.separator;


	/**
	 * Generate the NoC with the selected parameters.
	 * @param dir The path of directory where the NoC will be generated.
	 * @param m The number of router in X-dimension.
	 * @param n The number of router in Y-dimension.
	 * @param phitSize The number of bits of a flit.
	 * @param queueSize The number of queue positions.
	 */
	public void geraNoc(String dir, int m, int n, int phitSize, int queueSize){
		try{

			File projDir=new File(dir + File.separator + "NOC");
			projDir.mkdirs();

			String name_noc_out = new String(dir + File.separator + "NOC" + File.separator + "NOC.vhd");
			FileOutputStream noc_vhdl=new FileOutputStream(name_noc_out);
			DataOutputStream data_output=new DataOutputStream(noc_vhdl);

			//geracao da biblioteca
			data_output.writeBytes("library IEEE;\nuse IEEE.std_logic_1164.all;\nuse IEEE.std_logic_unsigned.all;\nuse work.Mercury_package.all;\n\n");

			//geracao da entidade
			data_output.writeBytes("entity NOC is\n");
			data_output.writeBytes("port(\n");
			data_output.writeBytes("\tclock : in std_logic;\n");
			data_output.writeBytes("\treset : in std_logic;\n");
			data_output.writeBytes("\tdata_av_local_I : in std_logic_NROT;\n");
			data_output.writeBytes("\tdata_av_local_O : out std_logic_NROT;\n");
			data_output.writeBytes("\tdata_local_I : in data_NROT;\n");
			data_output.writeBytes("\tdata_local_O : out data_NROT;\n");
			data_output.writeBytes("\tack_nack_local_I : in ack_nack_NROT;\n");
			data_output.writeBytes("\tack_nack_local_O : out ack_nack_NROT;\n");
			data_output.writeBytes("\tsize_local_I : in size_NROT;\n");
			data_output.writeBytes("\tsize_local_O : out size_NROT);\n");
			data_output.writeBytes("end NOC;\n\n");


			//geracao da arquitetura
			data_output.writeBytes("architecture NOC of NOC is\n\n");

			//geracao dos sinais
			for (int y=0; y<n; y++)
			{
				data_output.writeBytes("\tsignal ");
				for (int x=0; x<m; x++){
					if (x==0)
						data_output.writeBytes("data_av_local_int_I_"+intToString(x)+intToString(y));

					else
						data_output.writeBytes(", data_av_local_int_I_"+intToString(x)+intToString(y));
				}
				data_output.writeBytes(" : std_logic;\n");

				data_output.writeBytes("\tsignal ");
				for (int x=0; x<m; x++){
					if (x==0)
						data_output.writeBytes("data_av_local_int_O_"+intToString(x)+intToString(y));
					else
						data_output.writeBytes(", data_av_local_int_O_"+intToString(x)+intToString(y));
				}
				data_output.writeBytes(" : std_logic;\n");

				data_output.writeBytes("\tsignal ");
				for (int x=0; x<m; x++){
					if (x==0)
						data_output.writeBytes("data_int_I_"+intToString(x)+intToString(y));
					else
						data_output.writeBytes(", data_int_I_"+intToString(x)+intToString(y));
				}
				data_output.writeBytes(" : data_bus;\n");

				data_output.writeBytes("\tsignal ");
				for (int x=0; x<m; x++){
					if (x==0)
						data_output.writeBytes("data_int_O_"+intToString(x)+intToString(y));
					else
						data_output.writeBytes(", data_int_O_"+intToString(x)+intToString(y));
				}
				data_output.writeBytes(" : data_bus;\n");

				data_output.writeBytes("\tsignal ");
				for (int x=0; x<m; x++){
					if (x==0)
						data_output.writeBytes("data_local_int_"+intToString(x)+intToString(y));
					else
						data_output.writeBytes(", data_local_int_"+intToString(x)+intToString(y));
				}
				data_output.writeBytes(" : phit;\n");

				data_output.writeBytes("\tsignal ");
				for (int x=0; x<m; x++){
					if (x==0)
						data_output.writeBytes("ack_nack_int_I_"+intToString(x)+intToString(y));
					else
						data_output.writeBytes(", ack_nack_int_I_"+intToString(x)+intToString(y));
				}
				data_output.writeBytes(" : ack_nack_bus;\n");

				data_output.writeBytes("\tsignal ");
				for (int x=0; x<m; x++){
					if (x==0)
						data_output.writeBytes("ack_nack_int_O_"+intToString(x)+intToString(y));
					else
						data_output.writeBytes(", ack_nack_int_O_"+intToString(x)+intToString(y));
				}
				data_output.writeBytes(" : ack_nack_bus;\n");

				data_output.writeBytes("\tsignal ");
				for (int x=0; x<m; x++){
					if (x==0)
						data_output.writeBytes("ack_nack_local_int_"+intToString(x)+intToString(y));
					else
						data_output.writeBytes(", ack_nack_local_int_"+intToString(x)+intToString(y));
				}
				data_output.writeBytes(" : ackNack;\n");

				data_output.writeBytes("\tsignal ");
				for (int x=0; x<m; x++){
					if (x==0)
						data_output.writeBytes("size_int_I_"+intToString(x)+intToString(y));
					else
						data_output.writeBytes(", size_int_I_"+intToString(x)+intToString(y));
				}
				data_output.writeBytes(" : size_bus;\n");

				data_output.writeBytes("\tsignal ");
				for (int x=0; x<m; x++){
					if (x==0)
						data_output.writeBytes("size_int_O_"+intToString(x)+intToString(y));
					else
						data_output.writeBytes(", size_int_O_"+intToString(x)+intToString(y));
				}
				data_output.writeBytes(" : size_bus;\n");

				data_output.writeBytes("\tsignal ");
				for (int x=0; x<m; x++){
					if (x==0)
						data_output.writeBytes("size_local_int_"+intToString(x)+intToString(y));
					else
						data_output.writeBytes(", size_local_int_"+intToString(x)+intToString(y));
				}
				data_output.writeBytes(" : phit;\n");

				data_output.writeBytes("\tsignal ");
				for (int x=0; x<m; x++){
					if (x==0)
						data_output.writeBytes("queue_addr_int_I_"+intToString(x)+intToString(y));
					else
						data_output.writeBytes(", queue_addr_int_I_"+intToString(x)+intToString(y));
				}
				data_output.writeBytes(" : queue_addr_bus;\n");

				data_output.writeBytes("\tsignal ");
				for (int x=0; x<m; x++){
					if (x==0)
						data_output.writeBytes("queue_addr_int_O_"+intToString(x)+intToString(y));
					else
						data_output.writeBytes(", queue_addr_int_O_"+intToString(x)+intToString(y));
				}
				data_output.writeBytes(" : queue_addr_bus;\n");
			}

			data_output.writeBytes("begin\n\n");

			int posicao_roteador = 0;

			//geracao dos port maps
			for (int y=0; y<n; y++){
				for (int x=0; x<m; x++){
					data_output.writeBytes("\tRouter"+intToString(x)+intToString(y)+" : Entity work.Router(Router)\n");
					//data_output.writeBytes("\tgeneric map(x\""+m+n+"\", x\""+x+y+"\" )\n");

					if (phitSize==8) data_output.writeBytes("\tgeneric map(x\""+decimal_hexa(posicao_roteador,1)+"\" )\n");
					else data_output.writeBytes("\tgeneric map(x\""+decimal_hexa(x,(phitSize/16))+ decimal_hexa(y,(phitSize/16))+"\" )\n");

					data_output.writeBytes("\tport map(\n");
					data_output.writeBytes("\t\tclock => clock,\n");
					data_output.writeBytes("\t\treset => reset,\n");
					data_output.writeBytes("\t\tdata_av_IL => data_av_local_int_I_"+intToString(x)+intToString(y)+",\n");
					data_output.writeBytes("\t\tdata_av_OL => data_av_local_int_O_"+intToString(x)+intToString(y)+",\n");
					data_output.writeBytes("\t\tack_nack_I => ack_nack_int_I_"+intToString(x)+intToString(y)+",\n");
					data_output.writeBytes("\t\tack_nack_O => ack_nack_int_O_"+intToString(x)+intToString(y)+",\n");
					data_output.writeBytes("\t\tdata_I => data_int_I_"+intToString(x)+intToString(y)+",\n");
					data_output.writeBytes("\t\tdata_O => data_int_O_"+intToString(x)+intToString(y)+",\n");
					data_output.writeBytes("\t\tsize_I => size_int_I_"+intToString(x)+intToString(y)+",\n");
					data_output.writeBytes("\t\tsize_O => size_int_O_"+intToString(x)+intToString(y)+",\n");
					data_output.writeBytes("\t\tqueue_addr_I => queue_addr_int_I_"+intToString(x)+intToString(y)+",\n");
					data_output.writeBytes("\t\tqueue_addr_O => queue_addr_int_O_"+intToString(x)+intToString(y)+"\n");
					data_output.writeBytes("\t);\n\n");
					posicao_roteador++;
				}
			}

			posicao_roteador = 0;

			//geracao das interconexoes
			for (int y=0; y<n; y++){
				for (int x=0; x<m; x++){
					data_output.writeBytes("\n\t-- entradas da chave"+intToString(x)+intToString(y)+"\n");

					/////////////////////// conecting a porta oeste a porta leste/////////////////////////////////
					if (x==0){
						data_output.writeBytes("\tdata_int_I_"+intToString(x)+intToString(y)+"(conv_integer(WEST))<=data_int_O_"+intToString(m-1)+intToString(y)+"(conv_integer(EAST));\n");
						data_output.writeBytes("\tsize_int_I_"+intToString(x)+intToString(y)+"(conv_integer(WEST))<=size_int_O_"+intToString(m-1)+intToString(y)+"(conv_integer(EAST));\n");
						data_output.writeBytes("\tack_nack_int_I_"+intToString(x)+intToString(y)+"(conv_integer(WEST))<=ack_nack_int_O_"+intToString(m-1)+intToString(y)+"(conv_integer(EAST));\n");
						data_output.writeBytes("\tqueue_addr_int_I_"+intToString(x)+intToString(y)+"(conv_integer(WEST))<=queue_addr_int_O_"+intToString(m-1)+intToString(y)+"(conv_integer(EAST));\n\n");
					}
					else{
						data_output.writeBytes("\tdata_int_I_"+intToString(x)+intToString(y)+"(conv_integer(WEST))<=data_int_O_"+intToString(x-1)+intToString(y)+"(conv_integer(EAST));\n");
						data_output.writeBytes("\tsize_int_I_"+intToString(x)+intToString(y)+"(conv_integer(WEST))<=size_int_O_"+intToString(x-1)+intToString(y)+"(conv_integer(EAST));\n");
						data_output.writeBytes("\tack_nack_int_I_"+intToString(x)+intToString(y)+"(conv_integer(WEST))<=ack_nack_int_O_"+intToString(x-1)+intToString(y)+"(conv_integer(EAST));\n");
						data_output.writeBytes("\tqueue_addr_int_I_"+intToString(x)+intToString(y)+"(conv_integer(WEST))<=queue_addr_int_O_"+intToString(x-1)+intToString(y)+"(conv_integer(EAST));\n\n");
					}
					/////////////////////// conecting a porta norte a porta sul//////////////////////////
					if (y==(n-1)){
						data_output.writeBytes("\tdata_int_I_"+intToString(x)+intToString(y)+"(conv_integer(NORTH))<=data_int_O_"+intToString(x)+"00(conv_integer(SOUTH));\n");
						data_output.writeBytes("\tsize_int_I_"+intToString(x)+intToString(y)+"(conv_integer(NORTH))<=size_int_O_"+intToString(x)+"00(conv_integer(SOUTH));\n");
						data_output.writeBytes("\tack_nack_int_I_"+intToString(x)+intToString(y)+"(conv_integer(NORTH))<=ack_nack_int_O_"+intToString(x)+"00(conv_integer(SOUTH));\n");
						data_output.writeBytes("\tqueue_addr_int_I_"+intToString(x)+intToString(y)+"(conv_integer(NORTH))<=queue_addr_int_O_"+intToString(x)+"00(conv_integer(SOUTH));\n\n");
					}
					else{
						data_output.writeBytes("\tdata_int_I_"+intToString(x)+intToString(y)+"(conv_integer(NORTH))<=data_int_O_"+intToString(x)+intToString(y+1)+"(conv_integer(SOUTH));\n");
						data_output.writeBytes("\tsize_int_I_"+intToString(x)+intToString(y)+"(conv_integer(NORTH))<=size_int_O_"+intToString(x)+intToString(y+1)+"(conv_integer(SOUTH));\n");
						data_output.writeBytes("\tack_nack_int_I_"+intToString(x)+intToString(y)+"(conv_integer(NORTH))<=ack_nack_int_O_"+intToString(x)+intToString(y+1)+"(conv_integer(SOUTH));\n");
						data_output.writeBytes("\tqueue_addr_int_I_"+intToString(x)+intToString(y)+"(conv_integer(NORTH))<=queue_addr_int_O_"+intToString(x)+intToString(y+1)+"(conv_integer(SOUTH));\n\n");
					}
					/////////////////////// conecting a porta leste a porta oeste//////////////////////////
					if (x==(m-1)){
						data_output.writeBytes("\tdata_int_I_"+intToString(x)+intToString(y)+"(conv_integer(EAST))<=data_int_O_00"+intToString(y)+"(conv_integer(WEST));\n");
						data_output.writeBytes("\tsize_int_I_"+intToString(x)+intToString(y)+"(conv_integer(EAST))<=size_int_O_00"+intToString(y)+"(conv_integer(WEST));\n");
						data_output.writeBytes("\tack_nack_int_I_"+intToString(x)+intToString(y)+"(conv_integer(EAST))<=ack_nack_int_O_00"+intToString(y)+"(conv_integer(WEST));\n");
						data_output.writeBytes("\tqueue_addr_int_I_"+intToString(x)+intToString(y)+"(conv_integer(EAST))<=queue_addr_int_O_00"+intToString(y)+"(conv_integer(WEST));\n\n");
					}
					else{
						data_output.writeBytes("\tdata_int_I_"+intToString(x)+intToString(y)+"(conv_integer(EAST))<=data_int_O_"+intToString(x+1)+intToString(y)+"(conv_integer(WEST));\n");
						data_output.writeBytes("\tsize_int_I_"+intToString(x)+intToString(y)+"(conv_integer(EAST))<=size_int_O_"+intToString(x+1)+intToString(y)+"(conv_integer(WEST));\n");
						data_output.writeBytes("\tack_nack_int_I_"+intToString(x)+intToString(y)+"(conv_integer(EAST))<=ack_nack_int_O_"+intToString(x+1)+intToString(y)+"(conv_integer(WEST));\n");
						data_output.writeBytes("\tqueue_addr_int_I_"+intToString(x)+intToString(y)+"(conv_integer(EAST))<=queue_addr_int_O_"+intToString(x+1)+intToString(y)+"(conv_integer(WEST));\n\n");
					}
					/////////////////////// conecting a porta sul com a porta norte//////////////////////////
					if (y==0){
						data_output.writeBytes("\tdata_int_I_"+intToString(x)+intToString(y)+"(conv_integer(SOUTH))<=data_int_O_"+intToString(x)+intToString(n-1)+"(conv_integer(NORTH));\n");
						data_output.writeBytes("\tsize_int_I_"+intToString(x)+intToString(y)+"(conv_integer(SOUTH))<=size_int_O_"+intToString(x)+intToString(n-1)+"(conv_integer(NORTH));\n");
						data_output.writeBytes("\tack_nack_int_I_"+intToString(x)+intToString(y)+"(conv_integer(SOUTH))<=ack_nack_int_O_"+intToString(x)+intToString(n-1)+"(conv_integer(NORTH));\n");
						data_output.writeBytes("\tqueue_addr_int_I_"+intToString(x)+intToString(y)+"(conv_integer(SOUTH))<=queue_addr_int_O_"+intToString(x)+intToString(n-1)+"(conv_integer(NORTH));\n\n");
					}
					else{
						data_output.writeBytes("\tdata_int_I_"+intToString(x)+intToString(y)+"(conv_integer(SOUTH))<=data_int_O_"+intToString(x)+intToString(y-1)+"(conv_integer(NORTH));\n");
						data_output.writeBytes("\tsize_int_I_"+intToString(x)+intToString(y)+"(conv_integer(SOUTH))<=size_int_O_"+intToString(x)+intToString(y-1)+"(conv_integer(NORTH));\n");
						data_output.writeBytes("\tack_nack_int_I_"+intToString(x)+intToString(y)+"(conv_integer(SOUTH))<=ack_nack_int_O_"+intToString(x)+intToString(y-1)+"(conv_integer(NORTH));\n");
						data_output.writeBytes("\tqueue_addr_int_I_"+intToString(x)+intToString(y)+"(conv_integer(SOUTH))<=queue_addr_int_O_"+intToString(x)+intToString(y-1)+"(conv_integer(NORTH));\n\n");
					}


					data_output.writeBytes("\tdata_int_I_"+intToString(x)+intToString(y)+"(conv_integer(LOCAL))<=data_local_I("+posicao_roteador+");\n");
					data_output.writeBytes("\tdata_av_local_int_I_"+intToString(x)+intToString(y)+"<=data_av_local_I("+posicao_roteador+");\n");
					data_output.writeBytes("\tack_nack_int_I_"+intToString(x)+intToString(y)+"(conv_integer(LOCAL))<=ack_nack_local_I("+posicao_roteador+");\n");
					data_output.writeBytes("\tsize_int_I_"+intToString(x)+intToString(y)+"(conv_integer(LOCAL))<=size_local_I("+posicao_roteador+");\n");

					data_output.writeBytes("\tdata_av_local_O("+posicao_roteador+")<=data_av_local_int_O_"+intToString(x)+intToString(y)+";\n");
					data_output.writeBytes("\tdata_local_O("+posicao_roteador+")<=data_int_O_"+intToString(x)+intToString(y)+"(conv_integer(LOCAL));\n");
					data_output.writeBytes("\tsize_local_O("+posicao_roteador+")<=size_int_O_"+intToString(x)+intToString(y)+"(conv_integer(LOCAL));\n");
					data_output.writeBytes("\tack_nack_local_O("+posicao_roteador+")<=ack_nack_int_O_"+intToString(x)+intToString(y)+"(conv_integer(LOCAL));\n");

					posicao_roteador++;
				}
			}
			data_output.writeBytes("end NOC;");

			data_output.close();

			geraPackage(dir,m,n,phitSize,queueSize);

			String targetPath = dir + File.separator + "NOC" + File.separator;
			copiaArquivos(targetPath, sourcePath, "acknackin.vhd");
			copiaArquivos(targetPath, sourcePath, "acknackout.vhd");
			copiaArquivos(targetPath, sourcePath, "algoritmo_package.vhd");
			copiaArquivos(targetPath, sourcePath, "arbiterin.vhd");
			copiaArquivos(targetPath, sourcePath, "arbiterout.vhd");
			copiaArquivos(targetPath, sourcePath, "arbqueue.vhd");
			copiaArquivos(targetPath, sourcePath, "datasizeout.vhd");
			copiaArquivos(targetPath, sourcePath, "intoarbiterin.vhd");
			copiaArquivos(targetPath, sourcePath, "queue.vhd");
			copiaArquivos(targetPath, sourcePath, "queuectrl.vhd");
			copiaArquivos(targetPath, sourcePath, "router.vhd");
			copiaArquivos(targetPath, sourcePath, "serialinterface.vhd");
			copiaArquivos(targetPath, sourcePath, "switchout.vhd");

			geraTesthBanch(dir,m,n,phitSize,queueSize,((m*n)-1),0);
			geraTesthBanch(dir,m,n,phitSize,queueSize,((m*n)-(m)),m-1);
			geraTesthBanch(dir,m,n,phitSize,queueSize,0,((m*n)-1)/2);
			geraTesthBanch(dir,m,n,phitSize,queueSize,m-1,((m*n)-1)/2);
			geraTesthBanch(dir,m,n,phitSize,queueSize,((m*n)-(m)),((m*n)-1)/2);
			geraTesthBanch(dir,m,n,phitSize,queueSize,((m*n)-1),((m*n)-1)/2);

			createSC(dir,m,n,phitSize);

		}//end try
		catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't write Noc.vhd","Output error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		catch(Exception e){}
	}//end public

	private static String intToString(int valor){
		String ret = "0";

		if(valor >= 10) ret = "";

		return ret + valor;
	}

	private static String decimal_hexa(int valor, int tamanho){
		String hexadecimal,hexa;
		int resto;

		if (tamanho==0) tamanho = 1;

		//Converte
		hexadecimal="";
		do	{
			resto=(valor%16);
			valor=valor/16;
			if(resto==0)
				hexa="0";
			else if(resto==1)
				hexa="1";
			else if(resto==2)
				hexa="2";
			else if(resto==3)
				hexa="3";
			else if(resto==4)
				hexa="4";
			else if(resto==5)
				hexa="5";
			else if(resto==6)
				hexa="6";
			else if(resto==7)
				hexa="7";
			else if(resto==8)
				hexa="8";
			else if(resto==9)
				hexa="9";
			else if(resto==10)
				hexa="A";
			else if(resto==11)
				hexa="B";
			else if(resto==12)
				hexa="C";
			else if(resto==13)
				hexa="D";
			else if(resto==14)
				hexa="E";
			else
				hexa="F";

			hexadecimal=hexa.concat(hexadecimal);
		}while(valor!=0);

		while (hexadecimal.length()!=tamanho){
			hexadecimal = "0" + hexadecimal;
		}

		return hexadecimal;
	}

	private static String decimal_hexa_f(int valor, int tamanho){
		String hexadecimal,hexa;
		int resto;

		//Converte
		hexadecimal="";
		do
		{
			resto=(valor%16);
			valor=valor/16;
			if(resto==0)
				hexa="0";
			else if(resto==1)
				hexa="1";
			else if(resto==2)
				hexa="2";
			else if(resto==3)
				hexa="3";
			else if(resto==4)
				hexa="4";
			else if(resto==5)
				hexa="5";
			else if(resto==6)
				hexa="6";
			else if(resto==7)
				hexa="7";
			else if(resto==8)
				hexa="8";
			else if(resto==9)
				hexa="9";
			else if(resto==10)
				hexa="A";
			else if(resto==11)
				hexa="B";
			else if(resto==12)
				hexa="C";
			else if(resto==13)
				hexa="D";
			else if(resto==14)
				hexa="E";
			else
				hexa="F";

			hexadecimal=hexa.concat(hexadecimal);
		}while(valor!=0);

		while (hexadecimal.length()!=tamanho){
			hexadecimal = "F" + hexadecimal;
		}

		return hexadecimal;
	}

	private static String completa_bits(String valor,int Tamanho){
		String aux_valor;

		aux_valor = valor;
		while (aux_valor.length()!=Tamanho){
			aux_valor = "0" + aux_valor;
		}

		return aux_valor;
	}

	private static void geraPackage(String diretorio, int dimX, int dimY, int phitSize, int queueSize){
		try{
			File projDir = new File(diretorio);
			projDir.mkdirs();

			String name_pack_out = new String(diretorio + File.separator + "NOC" + File.separator + "mercury_package.vhd");
			FileOutputStream pack_vhdl=new FileOutputStream(name_pack_out);
			DataOutputStream data_output=new DataOutputStream(pack_vhdl);

			//geracao da biblioteca
			data_output.writeBytes("library IEEE;\nuse IEEE.STD_LOGIC_1164.all;\nuse IEEE.std_logic_unsigned.all;\n\n");

			//geracao do header do package
			data_output.writeBytes("package Mercury_package is\n\n");

			//geracao das constantes referentes ao phit
			data_output.writeBytes("\t--Constantes referentes ao phit\n");
			data_output.writeBytes("\tconstant PHIT_SIZE: integer range 8 to 64 := " + String.valueOf(phitSize) + ";\n");
			data_output.writeBytes("\tsubtype phit is std_logic_vector((PHIT_SIZE - 1) downto 0);\n");
			data_output.writeBytes("\tconstant HALF_PHIT_SIZE: integer range 4 to 32 := (PHIT_SIZE/2);\n");
			data_output.writeBytes("\tconstant QUARTER_PHIT_SIZE: integer range 2 to 16 := (PHIT_SIZE/4);\n\n");

			//geracao das constantes referentes a fila
			data_output.writeBytes("\t--Constantes referentes � fila\n");
			data_output.writeBytes("\tconstant QUEUE_POINTER: integer range 2 to 5 := " + String.valueOf((int)calcQueuePointer(queueSize)) + ";\n");
			data_output.writeBytes("\tconstant QUEUE_SIZE: integer range 1 to 32 := " + String.valueOf(queueSize) + ";	-- deve ser (2 ^ QUEUE_POINTER)	\n");
			data_output.writeBytes("\tsubtype pointer is std_logic_vector((QUEUE_POINTER - 1) downto 0);\n\n");

			//geracao da enumeracao dos endere�os das portas
			data_output.writeBytes("\t--Enumera��o dos endere�os das portas\n");
			data_output.writeBytes("\t--type door is (NORTH, SOUTH, EAST, WEST, LOCAL, PORT_NONE);\n");
			data_output.writeBytes("\tsubtype door is std_logic_vector (2 downto 0);\n");
			data_output.writeBytes("\tconstant NORTH: std_logic_vector (2 downto 0) := \"000\";\n");
			data_output.writeBytes("\tconstant SOUTH: std_logic_vector (2 downto 0) := \"001\";\n");
			data_output.writeBytes("\tconstant EAST:  std_logic_vector (2 downto 0) := \"010\";\n");
			data_output.writeBytes("\tconstant WEST:  std_logic_vector (2 downto 0) := \"011\";\n");
			data_output.writeBytes("\tconstant LOCAL: std_logic_vector (2 downto 0) := \"100\";\n");
			data_output.writeBytes("\tconstant PORT_NONE: std_logic_vector (2 downto 0) := \"101\";\n\n");

			//geracao da enumeracao das filas
			data_output.writeBytes("\t--Enumera��o das filas\n");
			data_output.writeBytes("\ttype queue_addr is (FILA_A, FILA_B, FILA_C, FILA_NONE);\n\n");

			//geracao da enumeracao de ACK/NACK
			data_output.writeBytes("\t--Enumera��o de ACK/NACK \n");
			data_output.writeBytes("\t--Troquei os nomes de ACK para ACK_ME ... porque na simula��o com modelsim dava erro de dupla defini��o\n");
			data_output.writeBytes("\tsubtype ackNack is std_logic_vector (1 downto 0);\n");
			data_output.writeBytes("\tconstant ACK_ME: std_logic_vector (1 downto 0) := \"00\";\n");
			data_output.writeBytes("\tconstant NACK_ME: std_logic_vector (1 downto 0) := \"01\";\n");
			data_output.writeBytes("\tconstant NONE_ME: std_logic_vector (1 downto 0) := \"10\";\n\n");

			//geracao do tamanho maximo de um pacote
			data_output.writeBytes("\t--Tamanho m�ximo que um pacote pode ter, devido ao tamanho da fila\n");
			data_output.writeBytes("\tsubtype max_package_size is std_logic_vector(QUEUE_POINTER - 1 downto 0);\n\n");

			//geracao do tamanho maximo de uma NoC e do tipo caminhos m�nimos
			data_output.writeBytes("\t--Tamanho m�ximo de uma NoC\n");
			data_output.writeBytes("\tconstant TAM_NOC: std_logic_vector(HALF_PHIT_SIZE - 1 downto 0) := \"" + convDecBin(dimX,(phitSize/4)) + convDecBin(dimY,(phitSize/4)) + "\"; --Constante referente a defini��o do tamanho NoC\n");
			data_output.writeBytes("\tsubtype max_noc_size is std_logic_vector(HALF_PHIT_SIZE - 1 downto 0);\n");
			data_output.writeBytes("\tsubtype half_max_noc_size is std_logic_vector(QUARTER_PHIT_SIZE - 1 downto 0);\n\n");
			data_output.writeBytes("\ttype caminhos_minimos is array(0 to 5) of std_logic;\n\n");

			//geracao dos tipos de barramentos
			data_output.writeBytes("\t-- Barramentos\n");
			data_output.writeBytes("\ttype ack_nack_bus is array(0 to 5) of ackNack;\n");
			data_output.writeBytes("\ttype data_bus is array(0 to 5) of phit;\n");
			data_output.writeBytes("\ttype size_bus is array(0 to 5) of phit;\n");
			data_output.writeBytes("\ttype queue_addr_bus is array(0 to 5) of queue_addr;\n\n");

			//geracao da constante Numero de roteadores
			data_output.writeBytes("\t-- N�mero de roteadores\n");
			data_output.writeBytes("\tconstant NROT: integer range 2 to 256 := " + String.valueOf(dimX*dimY) + "; -- default value is 3x3=9 routers\n\n");

			//geracao dos barramentos utilizados na geracao da NoC
			data_output.writeBytes("\t-- Barramentos usados para a cria��o da NoC\n");
			data_output.writeBytes("\ttype ack_nack_bus_NROT is array((NROT-1) downto 0) of ack_nack_bus;\n");
			data_output.writeBytes("\ttype data_bus_NROT is array((NROT-1) downto 0) of data_bus;\n");
			data_output.writeBytes("\ttype size_bus_NROT is array((NROT-1) downto 0) of size_bus;\n");
			data_output.writeBytes("\ttype queue_addr_bus_NROT is array((NROT-1) downto 0) of queue_addr_bus;\n");
			data_output.writeBytes("\ttype ack_nack_NROT is array((NROT-1) downto 0) of ackNack;\n");
			data_output.writeBytes("\ttype data_NROT is array((NROT-1) downto 0) of phit;\n");
			data_output.writeBytes("\ttype size_NROT is array((NROT-1) downto 0) of phit;\n");
			data_output.writeBytes("\tsubtype std_logic_NROT is std_logic_vector((NROT-1) downto 0);\n\n");

			//geracao de constantes e flags referentes ao tamanho da NoC
			data_output.writeBytes("\t-- Constantes que armazenam o tamanho das dimens�es NoC.\n");
			data_output.writeBytes("\tconstant TAM_NOC_X :half_max_noc_size := TAM_NOC(HALF_PHIT_SIZE - 1 downto QUARTER_PHIT_SIZE);\n");
			data_output.writeBytes("\tconstant TAM_NOC_Y :half_max_noc_size := TAM_NOC((QUARTER_PHIT_SIZE - 1) downto 0);\n");
			data_output.writeBytes("\t-- Flag constante que define se dimens�es s�o �mpares ou pares\n");
			data_output.writeBytes("\tconstant TAM_NOC_IMPAR_EM_X :std_logic := TAM_NOC(QUARTER_PHIT_SIZE);\n");
			data_output.writeBytes("\tconstant TAM_NOC_IMPAR_EM_Y :std_logic := TAM_NOC(0);\n\n");

			//geracao do cabecalho de funcao do package
			data_output.writeBytes("\tfunction CONV_VECTOR(int: integer range 0 to 31; vector_size: integer range 2 to 6) return std_logic_vector;\n");
			data_output.writeBytes("\tfunction CONV_INTEGER_TO_DOOR(int: integer) return door;\n");
			data_output.writeBytes("\tfunction CONV_DOOR_TO_INTEGER(door_in: door) return integer;\n");
			data_output.writeBytes("\tfunction CONV_QUEUE_TO_VECTOR(queue: queue_addr) return std_logic_vector;\n");
			data_output.writeBytes("\tfunction CONV_VECTOR_TO_QUEUE(vetor: std_logic_vector(1 downto 0)) return queue_addr;\n");

			data_output.writeBytes("end Mercury_package;\n\n"); //fim do header do package

			//inicio do package_body
			data_output.writeBytes("package body Mercury_package is\n\n");

			//geracao da funcao CONV_VECTOR
			data_output.writeBytes("function CONV_VECTOR( int: integer range 0 to 31 ; vector_size: integer range 2 to 6) return std_logic_vector is \n");
			data_output.writeBytes("begin\n");
			data_output.writeBytes("\tcase(vector_size) is\n");

			for(int i = 2; i <= 5; i++){
				data_output.writeBytes("\t\twhen " + String.valueOf(i) + " =>\n");
				data_output.writeBytes("\t\t\tcase(int) is\n");

				for(int j = 0; j < Math.pow(2,i); j++)
					data_output.writeBytes("\t\t\twhen " + String.valueOf(j) + " => return \"" + convDecBin(j,i) + "\";\n");

				data_output.writeBytes("\t\t\twhen others => return \"" + convDecBin(0,i) + "\";\n");

				data_output.writeBytes("\t\tend case;\n");
			}

			data_output.writeBytes("\t\twhen others => return \"00000\";\n");
			data_output.writeBytes("\tend case;\n");
			data_output.writeBytes("end CONV_VECTOR;\n\n");

			//geracao da funcao CONV_INTEGER_TO_DOOR
			data_output.writeBytes("function CONV_INTEGER_TO_DOOR(int: integer) return door is\n");
			data_output.writeBytes("begin\n");
			data_output.writeBytes("\tcase int is\n");
			data_output.writeBytes("\t\twhen 0 => return NORTH;\n");
			data_output.writeBytes("\t\twhen 1 => return SOUTH;\n");
			data_output.writeBytes("\t\twhen 2 => return EAST;\n");
			data_output.writeBytes("\t\twhen 3 => return WEST;\n");
			data_output.writeBytes("\t\twhen 4 => return LOCAL;\n");
			data_output.writeBytes("\t\twhen 5 => return PORT_NONE;\n");
			data_output.writeBytes("\t\twhen others => return PORT_NONE;\n");
			data_output.writeBytes("\tend case;\n");
			data_output.writeBytes("end CONV_INTEGER_TO_DOOR;\n\n");

			//geracao da funcao CONV_DOOR_TO_INTEGER
			data_output.writeBytes("function CONV_DOOR_TO_INTEGER(door_in: door) return integer is\n");
			data_output.writeBytes("begin\n");
			data_output.writeBytes("\tcase door_in is\n");
			data_output.writeBytes("\t\twhen NORTH 	=> return 0;\n");
			data_output.writeBytes("\t\twhen SOUTH 	=> return 1;\n");
			data_output.writeBytes("\t\twhen EAST  	=> return 2;\n");
			data_output.writeBytes("\t\twhen WEST  	=> return 3;\n");
			data_output.writeBytes("\t\twhen LOCAL 	=> return 4;\n");
			data_output.writeBytes("\t\twhen others => return 5;\n");
			data_output.writeBytes("\tend case;\n");
			data_output.writeBytes("end CONV_DOOR_TO_INTEGER;\n\n");

			//geracao da funcao CONV_QUEUE_TO_VECTOR
			data_output.writeBytes("function CONV_QUEUE_TO_VECTOR(queue: queue_addr) return std_logic_vector is\n");
			data_output.writeBytes("begin\n");
			data_output.writeBytes("\tif queue = FILA_A then\n");
			data_output.writeBytes("\t\treturn \"00\";\n");
			data_output.writeBytes("\telsif queue = FILA_B then\n");
			data_output.writeBytes("\t\treturn \"01\";\n");
			data_output.writeBytes("\telsif queue = FILA_C then\n");
			data_output.writeBytes("\t\treturn \"10\";\n");
			data_output.writeBytes("\telsif queue = FILA_NONE then\n");
			data_output.writeBytes("\t\treturn \"11\";\n");
			data_output.writeBytes("\tend if;\n");
			data_output.writeBytes("end CONV_QUEUE_TO_VECTOR;\n\n");

			//geracao da funcao CONV_VECTOR_TO_QUEUE
			data_output.writeBytes("function CONV_VECTOR_TO_QUEUE(vetor: std_logic_vector(1 downto 0)) return queue_addr is\n");
			data_output.writeBytes("begin\n");
			data_output.writeBytes("\tif vetor = \"00\" then\n");
			data_output.writeBytes("\t\treturn FILA_A;\n");
			data_output.writeBytes("\telsif vetor = \"01\" then\n");
			data_output.writeBytes("\t\treturn FILA_B;\n");
			data_output.writeBytes("\telsif vetor = \"10\" then\n");
			data_output.writeBytes("\t\treturn FILA_C;\n");
			data_output.writeBytes("\telsif vetor = \"11\" then\n");
			data_output.writeBytes("\t\treturn FILA_NONE;\n");
			data_output.writeBytes("\tend if;\n");
			data_output.writeBytes("end CONV_VECTOR_TO_QUEUE;\n\n");

			data_output.writeBytes("end Mercury_package;\n"); //fim do arquivo de package

		}//end try
		catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't write mercury_package.vhd","Output error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		catch(Exception e){}
	}//fim do construtor;

	/**
	 * M�todo que calcula o queuePointer baseado no tamanho da fila.
	 *
	 * @param qS	QueueSize: � o tamanho da fila em phit's.
	 *
	 * @return um valor double que � obtido pela seguinte f�rmula log2(qS)
	 */
	private static double calcQueuePointer(int qS){return (double) Math.log(qS)/Math.log(2);}

	/**
	 * M�todo que converte um n�mero decimal em um n�mero bin�rio com um n�mero
	 * determinado de bits.
	 *
	 * @param dec	N�mero decimal a ser convertido em bin�rio.
	 * @param bits	N�mero de bits que dever� possuir o n�mero
	 *				(OBS: o m�todo utiliza o n�mero de bits necess�rios
	 *				para representa��o de certo decimal, mesmo que o usu�rio
	 *				especifique um n�mero menor para o bin�rio)
	 *
	 * @return	Retorna uma String com o valor binario solicitado
	 */
	private static String convDecBin(int dec, int bits){
		String binario = "";
		int quoc = dec;
		int rest;

		while(quoc != 0){
			rest = quoc % 2;
			quoc /= 2;

			binario = String.valueOf(rest) + binario;
		}

		while(binario.length() < bits) binario = "0" + binario;

		return binario;
	}

	private void geraTesthBanch(String diretorio, int dimX, int dimY, int phitSize, int queueSize, int destino, int origem){

		try{
			File projDir = new File(diretorio + File.separator + "TB_NoC");

			projDir.mkdir();

			String name_pack_out = new String(diretorio + File.separator + "TB_NoC" + File.separator + "noc_tb_("+calcX(dimX,origem)+","+calcY(dimX,origem)+")("+calcX(dimX,destino)+","+calcY(dimX,destino)+").vhd");
			FileOutputStream pack_vhdl=new FileOutputStream(name_pack_out);
			DataOutputStream data_output=new DataOutputStream(pack_vhdl);

			//geracao da biblioteca
			data_output.writeBytes("library IEEE;\n");
			data_output.writeBytes("use IEEE.std_logic_1164.all;\n");
			data_output.writeBytes("use work.Mercury_package.all;\n\n");


			data_output.writeBytes("entity noc_tb_"+calcX(dimX,origem)+calcY(dimX,origem)+"_"+calcX(dimX,destino)+calcY(dimX,destino)+" is\n");
			data_output.writeBytes("end noc_tb_"+calcX(dimX,origem)+calcY(dimX,origem)+"_"+calcX(dimX,destino)+calcY(dimX,destino)+";\n\n");

			data_output.writeBytes("architecture noc_tb_"+calcX(dimX,origem)+calcY(dimX,origem)+"_"+calcX(dimX,destino)+calcY(dimX,destino)+" of noc_tb_"+calcX(dimX,origem)+calcY(dimX,origem)+"_"+calcX(dimX,destino)+calcY(dimX,destino)+" is\n");

			data_output.writeBytes("signal clock, reset: std_logic;\n");
			data_output.writeBytes("signal data_av_local_I: std_logic_NROT;\n");
			data_output.writeBytes("signal data_av_local_O: std_logic_NROT;\n");
			data_output.writeBytes("signal data_local_I : data_NROT;\n");
			data_output.writeBytes("signal data_local_O : data_NROT;\n");
			data_output.writeBytes("signal ack_nack_local_I : ack_nack_NROT;\n");
			data_output.writeBytes("signal ack_nack_local_O : ack_nack_NROT;\n");
			data_output.writeBytes("signal size_local_I : size_NROT;\n");
			data_output.writeBytes("signal size_local_O : size_NROT;\n\n");


			data_output.writeBytes("begin\n");
			data_output.writeBytes("\tNOC: entity work.NOC(NOC)\n");
			data_output.writeBytes("\t\tport map(\n");
			data_output.writeBytes("\t\tclock=>clock,\n");
			data_output.writeBytes("\t\treset=>reset,\n");
			data_output.writeBytes("\t\tdata_av_local_I=>data_av_local_I,\n");
			data_output.writeBytes("\t\tdata_av_local_O=>data_av_local_O,\n");
			data_output.writeBytes("\t\tdata_local_I=>data_local_I,\n");
			data_output.writeBytes("\t\tdata_local_O=>data_local_O,\n");
			data_output.writeBytes("\t\tack_nack_local_I=>ack_nack_local_I,\n");
			data_output.writeBytes("\t\tack_nack_local_O=>ack_nack_local_O,\n");
			data_output.writeBytes("\t\tsize_local_I=>size_local_I,\n");
			data_output.writeBytes("\t\tsize_local_O=>size_local_O\n");
			data_output.writeBytes("\t\t);\n\n");

			data_output.writeBytes("\tprocess\n");
			data_output.writeBytes("\tbegin\n");
			data_output.writeBytes("\t\tclock <= '1', '0' after 10ns;\n");
			data_output.writeBytes("\t\twait for 20ns;\n");
			data_output.writeBytes("\tend process;\n\n");

			data_output.writeBytes("\treset <= '1', '0' after 35ns;\n\n");



			int n = 72;
			for (int nodo=0; nodo<(dimX*dimY);nodo++){
				if (origem==nodo) {

					data_output.writeBytes("\tdata_local_I("+ nodo+") <= ");
					for (int x=0; x<queueSize; x++){
						if (x == 0) {

							if (phitSize==8){
								data_output.writeBytes ("x\""+ completa_bits(intToString(destino), phitSize/4)+"\" after "+ (n) + "ns,\n");
							}
							else {
								String completaBit;
								completaBit = completa_bits(decimal_hexa(calcX(dimX,destino),(phitSize/16)) +  decimal_hexa(calcY(dimX,destino),(phitSize/16)), phitSize/4 );
								data_output.writeBytes ("x\""+completaBit+"\" after "+ (n) + "ns,\n");
							}

							n += 10;
						}

						if (x == 1) data_output.writeBytes ("\t\t\t   x\""+ decimal_hexa(queueSize-1, phitSize/4) +"\" after "+ n + "ns,\n");

						if ((x != queueSize-1)&&(x>1))
							data_output.writeBytes ("\t\t\t   x\""+ decimal_hexa_f(x, phitSize/4)  +"\" after "+ n + "ns,\n");
						if ((x == queueSize-1)&&(x>1))
							data_output.writeBytes("\t\t\t  (others=>'0') after "+ n + " ns; \n");

						n += 20;
					}
					if (nodo==(dimX*dimY)-1) data_output.writeBytes("\n");
				}
				else{
					data_output.writeBytes("\tdata_local_I("+ nodo+") <= (others=>'0');\n");
					if (nodo==(dimX*dimY)-1) data_output.writeBytes("\n");
				}

			}

			n -= 40;


			for (int nodo=0; nodo<(dimX*dimY);nodo++){
				if (origem==nodo){
					data_output.writeBytes("\tdata_av_local_I("+nodo+") <= '0', '1' after 72ns, '0' after "+(n)+"ns; \n");
					if (nodo==(dimX*dimY)-1) data_output.writeBytes("\n");
				}
				else{
					data_output.writeBytes("\tdata_av_local_I("+nodo+") <= '0';\n");
					if (nodo==(dimX*dimY)-1) data_output.writeBytes("\n");
				}
			}




			for (int nodo=0; nodo<(dimX*dimY);nodo++){
				if (origem==nodo){
					data_output.writeBytes("\tsize_local_I("+nodo+") <= (others=>'0'), x\"" + decimal_hexa(queueSize-1, phitSize/4) + "\" after 72ns, (others=>'0') after 112ns;\n");
					if (nodo==(dimX*dimY)-1) data_output.writeBytes("\n");
				}
				else{
					data_output.writeBytes("\tsize_local_I("+nodo+") <= (others=>'0');\n");
					if (nodo==(dimX*dimY)-1) data_output.writeBytes("\n");
				}
			}

			n+=200;

			for (int nodo=0; nodo<(dimX*dimY);nodo++){
				if (destino==nodo){
					data_output.writeBytes("\tack_nack_local_I("+nodo+") <= NONE_ME, ACK_ME after "+(n)+"ns, NONE_ME after "+(n+(queueSize-1)*20)+"ns;\n");
					if (nodo==(dimX*dimY)-1) data_output.writeBytes("\n");
				}
				else{
					data_output.writeBytes("\tack_nack_local_I("+nodo+") <= NONE_ME;\n");
					if (nodo==(dimX*dimY)-1) data_output.writeBytes("\n");
				}
			}

			data_output.writeBytes("end noc_tb_"+calcX(dimX,origem)+calcY(dimX,origem)+"_"+calcX(dimX,destino)+calcY(dimX,destino)+";\n");

		}//end try
		catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't write mercury_package.vhd","Output error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		catch(Exception e){}
	}//fim do construtor;

	private static int calcX(int Dimx,int pos){
		return (pos - ((pos / Dimx)*Dimx));
	}

	private static int calcY(int Dimx,int pos){
		return (pos / Dimx);
	}

	/*********************************************************************************
	 * SystemC
	 *********************************************************************************/

	/**
	 * cria os arquivos para simular com SC.
	 */
	private void createSC(String diretorio, int dimX, int dimY, int phitSize){

		File SCDir=new File(diretorio + File.separator + "SC_NoC");
		SCDir.mkdirs();

		//copia os arquivos .cpp para o diretorio do SC_NoC
		String targetPath = diretorio + File.separator + "SC_NoC" + File.separator;
		copiaArquivos(targetPath, sourcePath, "SC_InputModule.cpp");
		copiaArquivos(targetPath, sourcePath, "SC_OutputModule.cpp");
		copiaArquivos(targetPath, sourcePath, "SC_OutputModuleRouter.cpp");

		// Substituindo os flags do arquivo SC_InputModule.h para NoC sem interface OCP.
		createInputModuleNoC(diretorio, dimX, dimY, phitSize);

		// Substituindo os flags do arquivo SC_OutputModule.h para NoC sem interface OCP.
		createOutputModuleNoC(diretorio, dimX, dimY, phitSize);

		// Substituindo os flags do arquivo SC_OutputModuleRouter.h para NoC sem interface OCP.
		createOutputModuleRouter(diretorio, dimX, dimY, phitSize);


		//cria o arquivo que integra a NOC sem interface OCP e os m�dulos SC
		createTopNoC(diretorio, dimX, dimY);

		//cria o script para simula��o da NOC sem interface OCP
		createScriptSCNoC(diretorio);
	}


	/*********************************************************************************
	 * INPUT MODULES
	 *********************************************************************************/

	/**
	 * cria o arquivo C do inputModule da NoC sem interface OCP.
	 */
	private void createInputModuleNoC(String diretorio, int dimX, int dimY, int phitSize){
		String line, word, change_parameter, sdimension;
		StringTokenizer st;

		int dimension = dimX*dimY;

		try{
			File inputFile = new File(sourcePath + "SC_InputModule.h");

			FileInputStream c_file=new FileInputStream(inputFile);
			BufferedReader buff=new BufferedReader(new InputStreamReader(c_file));

			String c_out = new String(diretorio+File.separator + "SC_NoC" + File.separator + "SC_InputModule.h");
			FileOutputStream cool_vhdl=new FileOutputStream(c_out);
			DataOutputStream data_output=new DataOutputStream(cool_vhdl);
			sdimension = Integer.toString(dimension);		// dimens�o pode ser obtida apartir da multiplica��o do numero de roteadores em X e Y
			change_parameter="";

			do{
				line=buff.readLine();
				st = new StringTokenizer(line, "$");
				int vem = st.countTokens();

				for (int cont=0; cont<vem; cont++){
					change_parameter="";
					word = st.nextToken();
					if(word.equalsIgnoreCase("NROT")){
						word = change_parameter.concat(sdimension);
						data_output.writeBytes(word);
					}
					else if(word.equalsIgnoreCase("TPHIT")){
						word = change_parameter.concat(Integer.toString(phitSize));
						data_output.writeBytes(word);
					}
					else if(word.equalsIgnoreCase("WIDTH")){
						word = change_parameter.concat(Integer.toString(dimX));
						data_output.writeBytes(word);
					}
					else if(word.equalsIgnoreCase("HEIGHT")){
						word = change_parameter.concat(Integer.toString(dimY));
						data_output.writeBytes(word);
					}
					else if(word.equalsIgnoreCase("OUTTX")){
						for (int x=0; x<dimension; x++){
							data_output.writeBytes("\t\t");
							data_output.writeBytes("if(Indice == "+x+") outtx"+x+" = (Booleano != 0)? SC_LOGIC_1: SC_LOGIC_0;\n");
						}
					}
					else if(word.equalsIgnoreCase("OUTDATA")){
						for (int x=0; x<dimension; x++){
							data_output.writeBytes("\t\t");
							data_output.writeBytes("if(Indice == "+x+") outdata"+x+" = Valor;\n");
						}
					}
					else if(word.equalsIgnoreCase("INACK")){
						for (int x=0; x<dimension; x++)
							data_output.writeBytes("\t\tif(Indice == "+x+") return inack"+x+".read().to_uint();\n");
					}
					else if(word.equalsIgnoreCase("OUTSIZE")){
						for (int x=0; x<dimension; x++){
							data_output.writeBytes("\t\t");
							data_output.writeBytes("if(Indice == "+x+") outsize"+x+" = Valor;\n");
						}
					}
					else if(word.equalsIgnoreCase("SIGNALS")){
						for (int x=0; x<dimension; x++){
							data_output.writeBytes("\tsc_in<sc_lv<2> > inack"+x+";\n");
							data_output.writeBytes("\tsc_out<sc_lv<constPhitSize> > outdata"+x+";\n");
							data_output.writeBytes("\tsc_out<sc_lv<constPhitSize> > outsize"+x+";\n");
							data_output.writeBytes("\tsc_out<sc_logic> outtx"+x+";\n");
						}
					}
					else if(word.equalsIgnoreCase("VARIABLES")){
						for (int x=0; x<dimension; x++){
							data_output.writeBytes("\tinack"+x+"(\"inack"+x+"\"),\n");
							data_output.writeBytes("\toutdata"+x+"(\"outdata"+x+"\"),\n");
							data_output.writeBytes("\toutsize"+x+"(\"outsize"+x+"\"),\n");
							data_output.writeBytes("\touttx"+x+"(\"outtx"+x+"\"),\n");
						}
					}
					else {
						data_output.writeBytes(word);
					}
				}//end for
				data_output.writeBytes("\r\n");
			}while(line != null); //end do

			buff.close();
			data_output.close();

		}catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't write SC_InputModule.h","Input error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}catch(Exception e){}
	}

	/*********************************************************************************
	 * OUTPUT MODULES
	 *********************************************************************************/

	/**
	 * cria o arquivo C do outputModule da NoC sem interface OCP.
	 */
	private void createOutputModuleNoC(String diretorio, int dimX, int dimY, int phitSize){
		String line, word, change_parameter, sdimension;
		StringTokenizer st;

		int dimension = dimX*dimY;


		try{
			File inputFile = new File(sourcePath + "SC_OutputModule.h");

			FileInputStream c_file=new FileInputStream(inputFile);
			BufferedReader buff=new BufferedReader(new InputStreamReader(c_file));

			String c_out = new String(diretorio+File.separator + "SC_NoC" + File.separator + "SC_OutputModule.h");
			FileOutputStream cool_vhdl=new FileOutputStream(c_out);
			DataOutputStream data_output=new DataOutputStream(cool_vhdl);
			sdimension = Integer.toString(dimension);
			change_parameter="";

			do{
				line=buff.readLine();
				st = new StringTokenizer(line, "$");
				int vem = st.countTokens();
				for (int cont=0; cont<vem; cont++){
					change_parameter="";
					word = st.nextToken();
					if(word.equalsIgnoreCase("NROT")){
						word = change_parameter.concat(sdimension);
						data_output.writeBytes(word);
					}
					else if(word.equalsIgnoreCase("TPHIT")){
						word = change_parameter.concat(Integer.toString(phitSize));
						data_output.writeBytes(word);
					}
					else if(word.equalsIgnoreCase("WIDTH")){
						word = change_parameter.concat(Integer.toString(dimX));
						data_output.writeBytes(word);
					}
					else if(word.equalsIgnoreCase("HEIGHT")){
						word = change_parameter.concat(Integer.toString(dimY));
						data_output.writeBytes(word);
					}
					else if(word.equalsIgnoreCase("INTX")){
						for (int x=0; x<dimension; x++)
							data_output.writeBytes("\t\tif(Indice == "+x+") return (intx"+x+" == SC_LOGIC_1)?1:0;\n");
					}
					else if(word.equalsIgnoreCase("INDATA")){
						for (int x=0; x<dimension; x++)
							data_output.writeBytes("\t\tif(Indice == "+x+") return indata"+x+".read().to_uint();\n");
					}
					else if(word.equalsIgnoreCase("INSIZE")){
						for (int x=0; x<dimension; x++)
							data_output.writeBytes("\t\tif(Indice == "+x+") return insize"+x+".read().to_uint();\n");
					}
					else if(word.equalsIgnoreCase("OUTACK")){
						for (int x=0; x<dimension; x++)
							data_output.writeBytes("\t\tif(Indice == "+x+")  outack"+x+"  = Valor;\n");
					}
					else if(word.equalsIgnoreCase("SIGNALS")){
						for (int x=0; x<dimension; x++){
							data_output.writeBytes("\tsc_in<sc_logic> intx"+x+";\n");
							data_output.writeBytes("\tsc_in<sc_lv<constPhitSize> > indata"+x+";\n");
							data_output.writeBytes("\tsc_in<sc_lv<constPhitSize> > insize"+x+";\n");
							data_output.writeBytes("\tsc_out<sc_lv<2> > outack"+x+";\n");
						}
					}
					else if(word.equalsIgnoreCase("VARIABLES")){
						for (int x=0; x<dimension; x++){
							data_output.writeBytes("\tindata"+x+"(\"indata"+x+"\"),\n");
							data_output.writeBytes("\tintx"+x+"(\"intx"+x+"\"),\n");
							data_output.writeBytes("\tinsize"+x+"(\"insize"+x+"\"),\n");
							data_output.writeBytes("\toutack"+x+"(\"outack"+x+"\"),\n");
						}
					}
					else{
						data_output.writeBytes(word);
					}
				}//end for
				data_output.writeBytes("\r\n");
			}while(line != null); //end do

			buff.close();
			data_output.close();

		}catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't write SC_OutputModule.h","Output error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}catch(Exception e){}
	}

	/**
	 * cria o arquivo C do outputModule do roteador.
	 */
	private void createOutputModuleRouter(String diretorio, int dimX, int dimY, int phitSize){
		String line, word, change_parameter, sdimension;
		StringTokenizer st;

		int router;

		int dimension = dimX*dimY;

		try{
			File inputFile = new File(sourcePath + "SC_OutputModuleRouter.h");

			FileInputStream c_file=new FileInputStream(inputFile);
			BufferedReader buff=new BufferedReader(new InputStreamReader(c_file));

			String c_out = new String(diretorio+File.separator + "SC_NoC" + File.separator + "SC_OutputModuleRouter.h");
			FileOutputStream cool_vhdl=new FileOutputStream(c_out);
			DataOutputStream data_output=new DataOutputStream(cool_vhdl);
			sdimension = Integer.toString(dimension);
			change_parameter="";

			do{
				line=buff.readLine();
				st = new StringTokenizer(line, "$");
				int vem = st.countTokens();
				for (int cont=0; cont<vem; cont++){
					change_parameter="";
					word = st.nextToken();
					if(word.equalsIgnoreCase("NROT")){
						word = change_parameter.concat(sdimension);
						data_output.writeBytes(word);
					}
					else if(word.equalsIgnoreCase("TPHIT")){
						word = change_parameter.concat(Integer.toString(phitSize));
						data_output.writeBytes(word);
					}
					else if(word.equalsIgnoreCase("WIDTH")){
						word = change_parameter.concat(Integer.toString(dimX));
						data_output.writeBytes(word);
					}
					else if(word.equalsIgnoreCase("HEIGHT")){
						word = change_parameter.concat(Integer.toString(dimY));
						data_output.writeBytes(word);
					}
					else if(word.equalsIgnoreCase("SIGNALS")){
						for (int y=0; y<dimY; y++){
							for (int x=0; x<dimX; x++){
								router = y * dimX + x;
								if(x!=(dimX-1)){ // porta EAST
									data_output.writeBytes("\tsc_in<sc_logic> tx_r"+router+"p0;\n");
									data_output.writeBytes("\tsc_in<sc_lv<constFlitSize> > out_r"+router+"p0;\n");
									data_output.writeBytes("\tsc_in<sc_logic> ack_ir"+router+"p0;\n");
								}
								if(x!=0){        // porta WEST
									data_output.writeBytes("\tsc_in<sc_logic> tx_r"+router+"p1;\n");
									data_output.writeBytes("\tsc_in<sc_logic> ack_ir"+router+"p1;\n");
									data_output.writeBytes("\tsc_in<sc_lv<constFlitSize> > out_r"+router+"p1;\n");
								}
								if(y!=(dimY-1)){ // porta NORTH
									data_output.writeBytes("\tsc_in<sc_logic> tx_r"+router+"p2;\n");
									data_output.writeBytes("\tsc_in<sc_lv<constFlitSize> > out_r"+router+"p2;\n");
									data_output.writeBytes("\tsc_in<sc_logic> ack_ir"+router+"p2;\n");
								}
								if(y!=0){        // porta SOUTH
									data_output.writeBytes("\tsc_in<sc_logic> tx_r"+router+"p3;\n");
									data_output.writeBytes("\tsc_in<sc_lv<constFlitSize> > out_r"+router+"p3;\n");
									data_output.writeBytes("\tsc_in<sc_logic> ack_ir"+router+"p3;\n");
								}
							}
						}
					}
					else if(word.equalsIgnoreCase("INTX")){
						for (int y=0; y<dimY; y++){
							for (int x=0; x<dimX; x++){
								router = y * dimX + x;
								if(y==0 && x==0)
									data_output.writeBytes("\t\t");
								else
									data_output.writeBytes("\t\telse ");
								data_output.writeBytes("if (Roteador == "+router+"){\n");
								data_output.writeBytes("\t\t\tif(Porta == 0) return (tx_r"+router+"p0==SC_LOGIC_1)? 1 : 0;\n");
								data_output.writeBytes("\t\t\tif(Porta == 1) return (tx_r"+router+"p1==SC_LOGIC_1)? 1 : 0;\n");
								data_output.writeBytes("\t\t\tif(Porta == 2) return (tx_r"+router+"p2==SC_LOGIC_1)? 1 : 0;\n");
								data_output.writeBytes("\t\t\tif(Porta == 3) return (tx_r"+router+"p3==SC_LOGIC_1)? 1 : 0;\n");
								data_output.writeBytes("\t\t}\n");
							}
						}
					}
					else if(word.equalsIgnoreCase("INACK")){
						for (int y=0; y<dimY; y++){
							for (int x=0; x<dimX; x++){
								router = y * dimX + x;
								if(y==0 && x==0)
									data_output.writeBytes("\t\t");
								else
									data_output.writeBytes("\t\telse ");
								data_output.writeBytes("if (Roteador == "+router+"){\n");
								data_output.writeBytes("\t\t\tif(Porta == 0) return (ack_ir"+router+"p0==SC_LOGIC_1)? 1 : 0;\n");
								data_output.writeBytes("\t\t\tif(Porta == 1) return (ack_ir"+router+"p1==SC_LOGIC_1)? 1 : 0;\n");
								data_output.writeBytes("\t\t\tif(Porta == 2) return (ack_ir"+router+"p2==SC_LOGIC_1)? 1 : 0;\n");
								data_output.writeBytes("\t\t\tif(Porta == 3) return (ack_ir"+router+"p3==SC_LOGIC_1)? 1 : 0;\n");
								data_output.writeBytes("\t\t}\n");
							}
						}
					}
					else if(word.equalsIgnoreCase("INDATA")){
						for (int y=0; y<dimY; y++){
							for (int x=0; x<dimX; x++){
								router = y * dimX + x;
								if(y==0 && x==0)
									data_output.writeBytes("\t\t");
								else
									data_output.writeBytes("\t\telse ");
								data_output.writeBytes("if(Roteador == "+router+"){\n");
								data_output.writeBytes("\t\t\tif(Porta == 0) return out_r"+router+"p0.read().to_uint();\n");
								data_output.writeBytes("\t\t\tif(Porta == 1) return out_r"+router+"p1.read().to_uint();\n");
								data_output.writeBytes("\t\t\tif(Porta == 2) return out_r"+router+"p2.read().to_uint();\n");
								data_output.writeBytes("\t\t\tif(Porta == 3) return out_r"+router+"p3.read().to_uint();\n");
								data_output.writeBytes("\t\t}\n");
							}
						}
					}
					else if(word.equalsIgnoreCase("VARIABLES")){
						for (int y=0; y<dimY; y++){
							for (int x=0; x<dimX; x++){
								router = y * dimX + x;
								if(x!=(dimX-1)){ // porta EAST
									data_output.writeBytes("\ttx_r"+router+"p0(\"tx_r"+router+"p0\"),\n");
									data_output.writeBytes("\tout_r"+router+"p0(\"out_r"+router+"p0\"),\n");
									data_output.writeBytes("\tack_ir"+router+"p0(\"ack_ir"+router+"p0\"),\n");
								}
								if(x!=0){        // porta WEST
									data_output.writeBytes("\ttx_r"+router+"p1(\"tx_r"+router+"p1\"),\n");
									data_output.writeBytes("\tout_r"+router+"p1(\"out_r"+router+"p1\"),\n");
									data_output.writeBytes("\tack_ir"+router+"p1(\"ack_ir"+router+"p1\"),\n");
								}
								if(y!=(dimY-1)){ // porta NORTH
									data_output.writeBytes("\ttx_r"+router+"p2(\"tx_r"+router+"p2\"),\n");
									data_output.writeBytes("\tout_r"+router+"p2(\"out_r"+router+"p2\"),\n");
									data_output.writeBytes("\tack_ir"+router+"p2(\"ack_ir"+router+"p2\"),\n");
								}
								if(y!=0){        // porta SOUTH
									data_output.writeBytes("\ttx_r"+router+"p3(\"tx_r"+router+"p3\"),\n");
									data_output.writeBytes("\tout_r"+router+"p3(\"out_r"+router+"p3\"),\n");
									data_output.writeBytes("\tack_ir"+router+"p3(\"ack_ir"+router+"p3\"),\n");
								}
							}
						}
					}
					else{
						data_output.writeBytes(word);
					}
				}//end for
				data_output.writeBytes("\r\n");
			}while(line != null); //end do

			buff.close();
			data_output.close();

		}catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't write SC_OutputModuleRouter.h","Output error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}catch(Exception e){}
	}

	/*********************************************************************************
	 * TOP
	 *********************************************************************************/

	/**
	 * Cria o arquivo TopNoC que integra a NoC sem interface OCP aos arquivos fli.
	 */
	private void createTopNoC(String diretorio, int dimX, int dimY){

		int dimension = dimX * dimY;
		int y, l;

		try{
			FileOutputStream cool_vhdl=new FileOutputStream(diretorio+File.separator + "topNoC.vhd");
			DataOutputStream data_output=new DataOutputStream(cool_vhdl);

			data_output.writeBytes("library IEEE;\nuse IEEE.std_logic_1164.all;\nuse work.Mercury_package.all;\n");
			data_output.writeBytes("\nentity topNoC is\nend topNoC;\n");
			data_output.writeBytes("architecture topNoC of topNoC is\n\n");
			//signals generation
			data_output.writeBytes("\tsignal clock, reset 		:	std_logic;\n");
			data_output.writeBytes("\tsignal data_av_local_I	:	std_logic_NROT;\n");
			data_output.writeBytes("\tsignal data_av_local_O	:	std_logic_NROT;\n");
			data_output.writeBytes("\tsignal data_local_I 		:	data_NROT;\n");
			data_output.writeBytes("\tsignal data_local_O 	  	:	data_NROT;\n");
			data_output.writeBytes("\tsignal ack_nack_local_I 	:	ack_nack_NROT;\n");
			data_output.writeBytes("\tsignal ack_nack_local_O 	:	ack_nack_NROT;\n");
			data_output.writeBytes("\tsignal size_local_I 		:	size_NROT;\n");
			data_output.writeBytes("\tsignal size_local_O	 	:	size_NROT;\n");

			//NoC
			data_output.writeBytes("begin\n");
			data_output.writeBytes("\tNOC: entity work.NOC(NOC)\n");
			data_output.writeBytes("\t\tport map(\n");
			data_output.writeBytes("\t\tclock	=>	clock,\n");
			data_output.writeBytes("\t\treset	=>	reset,\n");
			data_output.writeBytes("\t\tdata_av_local_I		=>	data_av_local_I,\n");
			data_output.writeBytes("\t\tdata_av_local_O		=>	data_av_local_O,\n");
			data_output.writeBytes("\t\tdata_local_I		=>	data_local_I,\n");
			data_output.writeBytes("\t\tdata_local_O		=>	data_local_O,\n");
			data_output.writeBytes("\t\tack_nack_local_I	=>	ack_nack_local_I,\n");
			data_output.writeBytes("\t\tack_nack_local_O	=>	ack_nack_local_O,\n");
			data_output.writeBytes("\t\tsize_local_I		=>	size_local_I,\n");
			data_output.writeBytes("\t\tsize_local_O		=>	size_local_O\n");
			data_output.writeBytes("\t\t);\n");

			data_output.writeBytes("\n\tprocess\n");
			data_output.writeBytes("\tbegin\n");
			data_output.writeBytes("\t\tclock <= '1', '0' after 10 ns;\n");
			data_output.writeBytes("\t\twait for 20 ns;\n");
			data_output.writeBytes("\tend process;\n");
			data_output.writeBytes("\n\treset <= '1', '0' after 35 ns;\n");

			// instancia modulos do systemC, defaut � true...
			if(true){


				//input module
				data_output.writeBytes("\n\tSC_InputModule: entity work.inputmodule\n");
				data_output.writeBytes("\tport map(\n");

				for (y=0; y<dimension; y++){
					data_output.writeBytes("\t\touttx"+y+"		=>	data_av_local_I("+y+"),\n");
					data_output.writeBytes("\t\toutdata"+y+"	=>	data_local_I("+y+"),\n");
					data_output.writeBytes("\t\toutsize"+y+"	=>	size_local_I("+y+"),\n");
					data_output.writeBytes("\t\tinack"+y+"  	=>	ack_nack_local_O("+y+"),\n");
				}

				data_output.writeBytes("\t\tclock	=>	clock,\n");
				data_output.writeBytes("\t\treset	=>	reset\n");
				data_output.writeBytes("\t);\n");


				//output module
				data_output.writeBytes("\n\tSC_OutputModule: entity work.outmodule\n");
				data_output.writeBytes("\tport map(\n");

				for (l=0; l<dimension; l++){
					data_output.writeBytes("\t\tintx"+l+"		=>	data_av_local_O("+l+"),\n");
					data_output.writeBytes("\t\tindata"+l+"		=>	data_local_O("+l+"),\n");
					data_output.writeBytes("\t\tinsize"+l+"		=>	size_local_O("+l+"),\n");
					data_output.writeBytes("\t\toutack"+l+"		=>	ack_nack_local_I("+l+"),\n");
				}

				data_output.writeBytes("\t\tclock	=>	clock,\n");
				data_output.writeBytes("\t\treset	=>	reset\n");
				data_output.writeBytes("\t);\n");
			}
			data_output.writeBytes("end topNoC;");
		}catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't write the TopLevel (top.vhd)","Output error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}catch(Exception e){}
	}

	/*********************************************************************************
	 * SCRRouterTS
	 *********************************************************************************/

	/**
	 * cria os scripts para simular com SC a NoC sem interface OCP.
	 */
	private void createScriptSCNoC(String diretorio){
		FileOutputStream script;
		DataOutputStream data_output;
		try{
			script=new FileOutputStream(diretorio+File.separator + "simulate.do");
			data_output=new DataOutputStream(script);
			data_output.writeBytes("vlib work\n");
			data_output.writeBytes("vmap work work\n\n");
			data_output.writeBytes("sccom -g SC_NoC/SC_InputModule.cpp\n");
			data_output.writeBytes("sccom -g SC_NoC/SC_OutputModule.cpp\n");
			data_output.writeBytes("sccom -link\n\n");
			data_output.writeBytes("vcom -work work -93 -explicit NOC/mercury_package.vhd\n");
			data_output.writeBytes("vcom -work work -93 -explicit NOC/datasizeout.vhd\n");
			data_output.writeBytes("vcom -work work -93 -explicit NOC/algoritmo_package.vhd\n");
			data_output.writeBytes("vcom -work work -93 -explicit NOC/acknackout.vhd\n");
			data_output.writeBytes("vcom -work work -93 -explicit NOC/arbiterin.vhd\n");
			data_output.writeBytes("vcom -work work -93 -explicit NOC/intoarbiterin.vhd\n");
			data_output.writeBytes("vcom -work work -93 -explicit NOC/queue.vhd\n");
			data_output.writeBytes("vcom -work work -93 -explicit NOC/queuectrl.vhd\n");
			data_output.writeBytes("vcom -work work -93 -explicit NOC/switchout.vhd\n");
			data_output.writeBytes("vcom -work work -93 -explicit NOC/acknackin.vhd\n");
			data_output.writeBytes("vcom -work work -93 -explicit NOC/arbiterout.vhd\n");
			data_output.writeBytes("vcom -work work -93 -explicit NOC/arbqueue.vhd\n");
			data_output.writeBytes("vcom -work work -93 -explicit NOC/router.vhd\n");
			data_output.writeBytes("vcom -work work -93 -explicit NOC/NOC.vhd\n");
			data_output.writeBytes("vcom -work work -93 -explicit topNoC.vhd\n");
			data_output.writeBytes("vsim work.topNoC\n");
			data_output.writeBytes("set StdArithNoWarnings 1\n");
			data_output.writeBytes("run 1 ms\n");
			data_output.writeBytes("quit -sim\n");
			data_output.writeBytes("quit -f");
			data_output.close();
		}catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't write the Script for simulation","Output error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}catch(Exception e){}
	}

	private void copiaArquivos(String diretorioOut, String diretorioIn, String arquivo){
		try {

			// Create channel on the source
			FileChannel srcChannel = new FileInputStream(diretorioIn + arquivo).getChannel();

			// Create channel on the destination
			FileChannel dstChannel = new FileOutputStream(diretorioOut +arquivo).getChannel();

			// Copy file contents from source to destination
			dstChannel.transferFrom(srcChannel, 0, srcChannel.size());

			// Close the channels
			srcChannel.close();
			dstChannel.close();
		} catch (IOException e) {
		}

	}

}//end class


