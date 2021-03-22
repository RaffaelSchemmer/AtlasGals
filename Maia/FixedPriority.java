package Maia;

import javax.swing.*;

import java.io.*;
import java.util.*;

import AtlasPackage.NoCGenerationVC;
import AtlasPackage.Convert;
import AtlasPackage.ManipulateFile;
import AtlasPackage.Default;
import AtlasPackage.Project;

/**
 * Generate a Hermes NoC with virtual channels and fixed priority scheduling.
 * @author Aline Vieira de Mello
 * @version
 */
public class FixedPriority extends NoCGenerationVC{

	private static String sourceDir = Default.atlashome + File.separator + "Maia" + File.separator + "Data" + File.separator + "VirtualChannel" + File.separator + "Priority" + File.separator;

	private String nocDir;
	private int nChannels;
	private boolean isSC;

	/**
	 * Generate the Hermes NoC with virtual channel and fixed priority scheduling.
	 * @param project
	 */
	public FixedPriority(Project project){
		super(project, sourceDir);
		nChannels= project.getNoC().getVirtualChannel();
		isSC = project.getNoC().isSCTB();
		nocDir = project.getPath() + File.separator + "NOC" + File.separator;
	}

	/**
	 * Generate the NoC and SC files
	 */
	public void generate(){
		//create the project directory tree
		makeDiretories();
		//copy the Hermes_buffer file
		ManipulateFile.copy(new File(sourceDir+"Hermes_buffer.vhd"), nocDir);
		//create the Hermes_package.vhd file
		createPackage("Hermes_package.vhd");
		//create the Hermes_inport.vhd file
		createInport();
		//create the Hermes_switchcontrol.vhd file
		createSwitchcontrol();
		//create the Hermes_outport.vhd file
		createOutport();
		//create the routers
		createRouters();
		//create the NoC
		createNoC();
		//If the SC test bench option is selected, create the SC file
		if(isSC)
			createSC();
	}

/*********************************************************************************
* SWITCHCONTROL
*********************************************************************************/

	/**
	 * Create the Hermes_switchcontrol.vhdl file, replacing the flags.
	 */
	public void createSwitchcontrol(){
		StringTokenizer st;
		String line, word;

		try{
			FileInputStream inFile=new FileInputStream(sourceDir+"Hermes_switchcontrol.vhd");
			BufferedReader buff=new BufferedReader(new InputStreamReader(inFile));

			DataOutputStream data_output=new DataOutputStream(new FileOutputStream(nocDir + "Hermes_switchcontrol.vhd"));

			int n_lines=0;
			line=buff.readLine();
			while(line!=null){
				st = new StringTokenizer(line, "$");
				int nTokens = st.countTokens();
				for (int cont=0; cont<nTokens; cont++){
					word = st.nextToken();
					if(word.equalsIgnoreCase("ask")){
						writeHSCAsk(data_output);
					}
					else if(word.equalsIgnoreCase("localSel")){
						writeHSCLocalSet(data_output);
					}
					else if(word.equalsIgnoreCase("eastSel")){
						writeHSCEastSet(data_output);
					}
					else if(word.equalsIgnoreCase("westSel")){
						writeHSCWestSet(data_output);
					}
					else if(word.equalsIgnoreCase("northSel")){
						writeHSCNorthSet(data_output);
					}
					else if(word.equalsIgnoreCase("southSel")){
						writeHSCSouthSet(data_output);
					}
					else if(word.equalsIgnoreCase("state_s2")){
						writeHSCStateS2(data_output);
					}					
					else if(word.equalsIgnoreCase("sender_ant")){
						writeHSCSenderAnt(data_output);
					}
					else if(word.equalsIgnoreCase("senderConditions")){
						writeHSCSenderConditions(data_output);
					}
					else{
						if(cont==nTokens-1)
							data_output.writeBytes(word+"\n");
						else
							data_output.writeBytes(word);
					}
				}
				if(nTokens==0) //white line
					data_output.writeBytes("\n");
				n_lines++;
				line=buff.readLine();
			} //end while
			buff.close();
			data_output.close();
			inFile.close();
		}catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't write Hermes_outport.vhd","Output error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}catch(Exception e){
			JOptionPane.showMessageDialog(null,"Can't write Hermes_outport.vhd\n" + e.getMessage(),"Error Message", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}
	
	/**
	 * Write state condition in Hermes_switchcontrol file.
	 * @param data_output 
	 * @throws Exception 
	 */
	public void writeHSCStateS2(DataOutputStream data_output) throws Exception{
		for(int i=nChannels; i>0; i--){
			if(i==nChannels)
				data_output.writeBytes("\t\t\t\tif h(prox)(L"+i+")='1' then sel_lane <= L"+i+";\n");
			else if(i>1)
				data_output.writeBytes("\t\t\t\telsif h(prox)(L"+i+")='1' then sel_lane <= L"+i+";\n");
			else
				data_output.writeBytes("\t\t\t\telse sel_lane <= L"+i+"; end if;\n");
		}
	}	
	
/*********************************************************************************
* OUTPORT
*********************************************************************************/	
	/**
	 * Create the Hermes_outport.vhdl file, replacing the flags.
	 */
	public void createOutport(){
		StringTokenizer st;
		String line, word;

		try{
			FileInputStream inFile=new FileInputStream(sourceDir+"Hermes_outport.vhd");
			BufferedReader buff=new BufferedReader(new InputStreamReader(inFile));
			
			DataOutputStream data_output=new DataOutputStream(new FileOutputStream(nocDir + "Hermes_outport.vhd"));

			int n_lines=0;
			line=buff.readLine();
			while(line!=null){
				st = new StringTokenizer(line, "$");
				int nTokens = st.countTokens();
				for (int cont=0; cont<nTokens; cont++){
					word = st.nextToken();
					/*
					if(word.equalsIgnoreCase("signal")){
						writeOutputSignal(data_output);
					}
					else if(word.equalsIgnoreCase("cs_local")){
						writeOutputCS_Local(data_output);
					}
					else if(word.equalsIgnoreCase("cs_east")){
						writeOutputCS_East(data_output);
					}
					else if(word.equalsIgnoreCase("cs_west")){
						writeOutputCS_West(data_output);
					}
					else if(word.equalsIgnoreCase("cs_north")){
						writeOutputCS_North(data_output); 
					}
					else if(word.equalsIgnoreCase("cs_south")){
						writeOutputCS_South(data_output); 
					}
					else if(word.equalsIgnoreCase("tx")){
						writeOutputTx(data_output);
					}
					else if(word.equalsIgnoreCase("data_out_local")){
						writeOutputDataOut_Local(data_output);
					}
					else if(word.equalsIgnoreCase("data_out_east")){
						writeOutputDataOut_East(data_output);
					}
					else if(word.equalsIgnoreCase("data_out_west")){
						writeOutputDataOut_West(data_output);
					}
					else if(word.equalsIgnoreCase("data_out_north")){
						writeOutputDataOut_North(data_output);
					}
					else if(word.equalsIgnoreCase("data_out_south")){
						writeOutputDataOut_South(data_output);
					}
					else if(word.equalsIgnoreCase("data_ack_local")){
						writeOutputDataAck_Local(data_output);
					}
					else if(word.equalsIgnoreCase("data_ack_east")){
						writeOutputDataAck_East(data_output);
					}
					else if(word.equalsIgnoreCase("data_ack_west")){
						writeOutputDataAck_West(data_output);
					}
					else if(word.equalsIgnoreCase("data_ack_north")){
						writeOutputDataAck_North(data_output);
					}
					else if(word.equalsIgnoreCase("data_ack_south")){
						writeOutputDataAck_South(data_output);
					}
					else if(word.equalsIgnoreCase("process")){
						writeOutputProcess(data_output);
					}
					*/
					
					if(word.equalsIgnoreCase("signal")){
						data_output.writeBytes("signal ");
						for(int i=0;i<(nChannels-1);i++)
							data_output.writeBytes("c"+(i+1)+", ");
						data_output.writeBytes("c"+nChannels+" : std_logic := '0';\n");

					}
					else if(word.equalsIgnoreCase("cs_local")){
						for(int i=0;i<nChannels;i++){
							data_output.writeBytes("\tc"+(i+1)+" <= '1' when free(L"+(i+1)+")='0' and credit_i(L"+(i+1)+")='1' and\n");
							for(int j=0;j<nChannels;j++){
								if(j==0)
									data_output.writeBytes("\t\t\t(");
								else
									data_output.writeBytes("\t\t\t ");
								data_output.writeBytes("(tableOut(L"+(i+1)+")=x\"0"+j+"\" and data_av(EAST)(L"+(j+1)+")='1')   or\n");
							}
							for(int j=0;j<nChannels;j++){
								data_output.writeBytes("\t\t\t (tableOut(L"+(i+1)+")=x\"1"+j+"\" and data_av(WEST)(L"+(j+1)+")='1')   or\n");
							}
							for(int j=0;j<nChannels;j++){
								data_output.writeBytes("\t\t\t (tableOut(L"+(i+1)+")=x\"2"+j+"\" and data_av(NORTH)(L"+(j+1)+")='1')  or\n");
							}
							for(int j=0;j<nChannels;j++){
								if(j==nChannels-1)
									data_output.writeBytes("\t\t\t (tableOut(L"+(i+1)+")=x\"3"+j+"\" and data_av(SOUTH)(L"+(j+1)+")='1')) else\n");
								else
									data_output.writeBytes("\t\t\t (tableOut(L"+(i+1)+")=x\"3"+j+"\" and data_av(SOUTH)(L"+(j+1)+")='1')  or\n");
							}
							data_output.writeBytes("\t\t\t'0';\n\n");
						}
					}
					else if(word.equalsIgnoreCase("cs_east")){
						for(int i=0;i<nChannels;i++){
							data_output.writeBytes("\tc"+(i+1)+" <= '1' when free(L"+(i+1)+")='0' and credit_i(L"+(i+1)+")='1' and\n");
							for(int j=0;j<nChannels;j++){
								if(j==0)
									data_output.writeBytes("\t\t\t(");
								else
									data_output.writeBytes("\t\t\t ");
								data_output.writeBytes("(tableOut(L"+(i+1)+")=x\"1"+j+"\" and data_av(WEST)(L"+(j+1)+")='1')   or\n");
							}
							for(int j=0;j<nChannels;j++){
								if(j==nChannels-1)
									data_output.writeBytes("\t\t\t (tableOut(L"+(i+1)+")=x\"4"+j+"\" and data_av(LOCAL)(L"+(j+1)+")='1')) else\n");
								else
									data_output.writeBytes("\t\t\t (tableOut(L"+(i+1)+")=x\"4"+j+"\" and data_av(LOCAL)(L"+(j+1)+")='1')  or\n");
							}
							data_output.writeBytes("\t\t\t'0';\n\n");
						}
					}
					else if(word.equalsIgnoreCase("cs_west")){
						for(int i=0;i<nChannels;i++){
							data_output.writeBytes("\tc"+(i+1)+" <= '1' when free(L"+(i+1)+")='0' and credit_i(L"+(i+1)+")='1' and\n");
							for(int j=0;j<nChannels;j++){
								if(j==0)
									data_output.writeBytes("\t\t\t(");
								else
									data_output.writeBytes("\t\t\t ");
								data_output.writeBytes("(tableOut(L"+(i+1)+")=x\"0"+j+"\" and data_av(EAST)(L"+(j+1)+")='1')   or\n");
							}
							for(int j=0;j<nChannels;j++){
								if(j==nChannels-1)
									data_output.writeBytes("\t\t\t (tableOut(L"+(i+1)+")=x\"4"+j+"\" and data_av(LOCAL)(L"+(j+1)+")='1')) else\n");
								else
									data_output.writeBytes("\t\t\t (tableOut(L"+(i+1)+")=x\"4"+j+"\" and data_av(LOCAL)(L"+(j+1)+")='1')  or\n");
							}
							data_output.writeBytes("\t\t\t'0';\n\n");
						}
					}
					else if(word.equalsIgnoreCase("cs_north")){
						for(int i=0;i<nChannels;i++){
							data_output.writeBytes("\tc"+(i+1)+" <= '1' when free(L"+(i+1)+")='0' and credit_i(L"+(i+1)+")='1' and\n");
							for(int j=0;j<nChannels;j++){
								if(j==0)
									data_output.writeBytes("\t\t\t(");
								else
									data_output.writeBytes("\t\t\t ");
								data_output.writeBytes("(tableOut(L"+(i+1)+")=x\"0"+j+"\" and data_av(EAST)(L"+(j+1)+")='1')   or\n");
							}
							for(int j=0;j<nChannels;j++){
								data_output.writeBytes("\t\t\t (tableOut(L"+(i+1)+")=x\"1"+j+"\" and data_av(WEST)(L"+(j+1)+")='1')   or\n");
							}
							for(int j=0;j<nChannels;j++){
								data_output.writeBytes("\t\t\t (tableOut(L"+(i+1)+")=x\"3"+j+"\" and data_av(SOUTH)(L"+(j+1)+")='1')  or\n");
							}
							for(int j=0;j<nChannels;j++){
								if(j==nChannels-1)
									data_output.writeBytes("\t\t\t (tableOut(L"+(i+1)+")=x\"4"+j+"\" and data_av(LOCAL)(L"+(j+1)+")='1')) else\n");
								else
									data_output.writeBytes("\t\t\t (tableOut(L"+(i+1)+")=x\"4"+j+"\" and data_av(LOCAL)(L"+(j+1)+")='1')  or\n");
							}
							data_output.writeBytes("\t\t\t'0';\n\n");
						}
					}
					else if(word.equalsIgnoreCase("cs_south")){
						for(int i=0;i<nChannels;i++){
							data_output.writeBytes("\tc"+(i+1)+" <= '1' when free(L"+(i+1)+")='0' and credit_i(L"+(i+1)+")='1' and\n");
							for(int j=0;j<nChannels;j++){
								if(j==0)
									data_output.writeBytes("\t\t\t(");
								else
									data_output.writeBytes("\t\t\t ");
								data_output.writeBytes("(tableOut(L"+(i+1)+")=x\"0"+j+"\" and data_av(EAST)(L"+(j+1)+")='1')   or\n");
							}
							for(int j=0;j<nChannels;j++){
								data_output.writeBytes("\t\t\t (tableOut(L"+(i+1)+")=x\"1"+j+"\" and data_av(WEST)(L"+(j+1)+")='1')   or\n");
							}
							for(int j=0;j<nChannels;j++){
								data_output.writeBytes("\t\t\t (tableOut(L"+(i+1)+")=x\"2"+j+"\" and data_av(NORTH)(L"+(j+1)+")='1')  or\n");
							}
							for(int j=0;j<nChannels;j++){
								if(j==nChannels-1)
									data_output.writeBytes("\t\t\t (tableOut(L"+(i+1)+")=x\"4"+j+"\" and data_av(LOCAL)(L"+(j+1)+")='1')) else\n");
								else
									data_output.writeBytes("\t\t\t (tableOut(L"+(i+1)+")=x\"4"+j+"\" and data_av(LOCAL)(L"+(j+1)+")='1')  or\n");
							}
							data_output.writeBytes("\t\t\t'0';\n\n");
						}
					}
					else if(word.equalsIgnoreCase("tx")){
						data_output.writeBytes("\ttx <= '1' when (");
						for(int i=0;i<nChannels;i++){
							if(i!=nChannels-1)
								data_output.writeBytes("c"+(i+1)+"='1' or ");
							else
								data_output.writeBytes("c"+(i+1)+"='1') else '0';\n");
						}
					}
					else if(word.equalsIgnoreCase("data_out_local")){
						data_output.writeBytes("\tdata_out <= ");
						for(int i=0;i<nChannels;i++){
							if(i!=0)
								data_output.writeBytes("\t\t\t");
							data_output.writeBytes("data(EAST)(L"+(i+1)+")  when indice=x\"0"+i+"\" and (");
							for(int j=0;j<nChannels;j++){
								if(j==nChannels-1)
									data_output.writeBytes("c"+(j+1)+"='1'");
								else
									data_output.writeBytes("c"+(j+1)+"='1' or ");
							}
							data_output.writeBytes(") else\n");
						}
						for(int i=0;i<nChannels;i++){
							data_output.writeBytes("\t\t\tdata(WEST)(L"+(i+1)+")  when indice=x\"1"+i+"\" and (");
							for(int j=0;j<nChannels;j++){
								if(j==nChannels-1)
									data_output.writeBytes("c"+(j+1)+"='1'");
								else
									data_output.writeBytes("c"+(j+1)+"='1' or ");
							}
							data_output.writeBytes(") else\n");
						}
						for(int i=0;i<nChannels;i++){
							data_output.writeBytes("\t\t\tdata(NORTH)(L"+(i+1)+") when indice=x\"2"+i+"\" and (");
							for(int j=0;j<nChannels;j++){
								if(j==nChannels-1)
									data_output.writeBytes("c"+(j+1)+"='1'");
								else
									data_output.writeBytes("c"+(j+1)+"='1' or ");
							}
							data_output.writeBytes(") else\n");
						}
						for(int i=0;i<nChannels;i++){
							data_output.writeBytes("\t\t\tdata(SOUTH)(L"+(i+1)+") when indice=x\"3"+i+"\" and (");
							for(int j=0;j<nChannels;j++){
								if(j==nChannels-1)
									data_output.writeBytes("c"+(j+1)+"='1'");
								else
									data_output.writeBytes("c"+(j+1)+"='1' or ");
							}
							data_output.writeBytes(") else\n");
						}
						data_output.writeBytes("\t\t\t(others=>'0');\n");
					}
					else if(word.equalsIgnoreCase("data_out_east")){
						data_output.writeBytes("\tdata_out <= ");
						for(int i=0;i<nChannels;i++){
							if(i!=0)
								data_output.writeBytes("\t\t\t");
							data_output.writeBytes("data(WEST)(L"+(i+1)+")  when indice=x\"1"+i+"\" and(");
							for(int j=0;j<nChannels;j++){
								if(j==nChannels-1)
									data_output.writeBytes("c"+(j+1)+"='1'");
								else
									data_output.writeBytes("c"+(j+1)+"='1' or ");
							}
							data_output.writeBytes(") else\n");
						}
						for(int i=0;i<nChannels;i++){
							data_output.writeBytes("\t\t\tdata(LOCAL)(L"+(i+1)+") when indice=x\"4"+i+"\" and (");
							for(int j=0;j<nChannels;j++){
								if(j==nChannels-1)
									data_output.writeBytes("c"+(j+1)+"='1'");
								else
									data_output.writeBytes("c"+(j+1)+"='1' or ");
							}
							data_output.writeBytes(") else\n");
						}
						data_output.writeBytes("\t\t\t(others=>'0');\n");
					}
					else if(word.equalsIgnoreCase("data_out_west")){
						data_output.writeBytes("\tdata_out <= ");
						for(int i=0;i<nChannels;i++){
							if(i!=0)
								data_output.writeBytes("\t\t\t");
							data_output.writeBytes("data(EAST)(L"+(i+1)+")  when indice=x\"0"+i+"\" and (");
							for(int j=0;j<nChannels;j++){
								if(j==nChannels-1)
									data_output.writeBytes("c"+(j+1)+"='1'");
								else
									data_output.writeBytes("c"+(j+1)+"='1' or ");
							}
							data_output.writeBytes(") else\n");
						}
						for(int i=0;i<nChannels;i++){
							data_output.writeBytes("\t\t\tdata(LOCAL)(L"+(i+1)+") when indice=x\"4"+i+"\" and (");
							for(int j=0;j<nChannels;j++){
								if(j==nChannels-1)
									data_output.writeBytes("c"+(j+1)+"='1'");
								else
									data_output.writeBytes("c"+(j+1)+"='1' or ");
							}
							data_output.writeBytes(") else\n");
						}
						data_output.writeBytes("\t\t\t(others=>'0');\n");
					}
					else if(word.equalsIgnoreCase("data_out_north")){
						data_output.writeBytes("\tdata_out <= ");
						for(int i=0;i<nChannels;i++){
							if(i!=0)
								data_output.writeBytes("\t\t\t");
							data_output.writeBytes("data(EAST)(L"+(i+1)+")  when indice=x\"0"+i+"\" and (");
							for(int j=0;j<nChannels;j++){
								if(j==nChannels-1)
									data_output.writeBytes("c"+(j+1)+"='1'");
								else
									data_output.writeBytes("c"+(j+1)+"='1' or ");
							}
							data_output.writeBytes(") else\n");
						}
						for(int i=0;i<nChannels;i++){
							data_output.writeBytes("\t\t\tdata(WEST)(L"+(i+1)+")  when indice=x\"1"+i+"\" and (");
							for(int j=0;j<nChannels;j++){
								if(j==nChannels-1)
									data_output.writeBytes("c"+(j+1)+"='1'");
								else
									data_output.writeBytes("c"+(j+1)+"='1' or ");
							}
							data_output.writeBytes(") else\n");
						}
						for(int i=0;i<nChannels;i++){
							data_output.writeBytes("\t\t\tdata(SOUTH)(L"+(i+1)+") when indice=x\"3"+i+"\" and (");
							for(int j=0;j<nChannels;j++){
								if(j==nChannels-1)
									data_output.writeBytes("c"+(j+1)+"='1'");
								else
									data_output.writeBytes("c"+(j+1)+"='1' or ");
							}
							data_output.writeBytes(") else\n");
						}
						for(int i=0;i<nChannels;i++){
							data_output.writeBytes("\t\t\tdata(LOCAL)(L"+(i+1)+") when indice=x\"4"+i+"\" and (");
							for(int j=0;j<nChannels;j++){
								if(j==nChannels-1)
									data_output.writeBytes("c"+(j+1)+"='1'");
								else
									data_output.writeBytes("c"+(j+1)+"='1' or ");
							}
							data_output.writeBytes(") else\n");
						}
						data_output.writeBytes("\t\t\t(others=>'0');\n");
					}
					else if(word.equalsIgnoreCase("data_out_south")){
						data_output.writeBytes("\tdata_out <= ");
						for(int i=0;i<nChannels;i++){
							if(i!=0)
								data_output.writeBytes("\t\t\t");
							data_output.writeBytes("data(EAST)(L"+(i+1)+")  when indice=x\"0"+i+"\" and (");
							for(int j=0;j<nChannels;j++){
								if(j==nChannels-1)
									data_output.writeBytes("c"+(j+1)+"='1'");
								else
									data_output.writeBytes("c"+(j+1)+"='1' or ");
							}
							data_output.writeBytes(") else\n");
						}
						for(int i=0;i<nChannels;i++){
							data_output.writeBytes("\t\t\tdata(WEST)(L"+(i+1)+")  when indice=x\"1"+i+"\" and (");
							for(int j=0;j<nChannels;j++){
								if(j==nChannels-1)
									data_output.writeBytes("c"+(j+1)+"='1'");
								else
									data_output.writeBytes("c"+(j+1)+"='1' or ");
							}
							data_output.writeBytes(") else\n");
						}
						for(int i=0;i<nChannels;i++){
							data_output.writeBytes("\t\t\tdata(NORTH)(L"+(i+1)+") when indice=x\"2"+i+"\" and (");
							for(int j=0;j<nChannels;j++){
								if(j==nChannels-1)
									data_output.writeBytes("c"+(j+1)+"='1'");
								else
									data_output.writeBytes("c"+(j+1)+"='1' or ");
							}
							data_output.writeBytes(") else\n");
						}
						for(int i=0;i<nChannels;i++){
							data_output.writeBytes("\t\t\tdata(LOCAL)(L"+(i+1)+") when indice=x\"4"+i+"\" and (");
							for(int j=0;j<nChannels;j++){
								if(j==nChannels-1)
									data_output.writeBytes("c"+(j+1)+"='1'");
								else
									data_output.writeBytes("c"+(j+1)+"='1' or ");
							}
							data_output.writeBytes(") else\n");
						}
						data_output.writeBytes("\t\t\t(others=>'0');\n");
					}
					else if(word.equalsIgnoreCase("data_ack_local")){
						for(int i=0;i<nChannels;i++){
							data_output.writeBytes("\tdata_ack(L"+(i+1)+") <= ");
							for(int j=0;j<nChannels;j++){
								if(j!=0)
									data_output.writeBytes("\t\t\t");
								data_output.writeBytes("all_lane_tx(EAST)(L"+(j+1)+")  when tableIn(L"+(i+1)+")=x\"0"+j+"\" and data_av(LOCAL)(L"+(i+1)+")='1' else\n");
							}
							for(int j=0;j<nChannels;j++)
								data_output.writeBytes("\t\t\tall_lane_tx(WEST)(L"+(j+1)+")  when tableIn(L"+(i+1)+")=x\"1"+j+"\" and data_av(LOCAL)(L"+(i+1)+")='1' else\n");
							for(int j=0;j<nChannels;j++)
								data_output.writeBytes("\t\t\tall_lane_tx(NORTH)(L"+(j+1)+") when tableIn(L"+(i+1)+")=x\"2"+j+"\" and data_av(LOCAL)(L"+(i+1)+")='1' else\n");
							for(int j=0;j<nChannels;j++)
								data_output.writeBytes("\t\t\tall_lane_tx(SOUTH)(L"+(j+1)+") when tableIn(L"+(i+1)+")=x\"3"+j+"\" and data_av(LOCAL)(L"+(i+1)+")='1' else\n");
							data_output.writeBytes("\t\t\t'0';\n\n");
						}
					}
					else if(word.equalsIgnoreCase("data_ack_east")){
						for(int i=0;i<nChannels;i++){
							data_output.writeBytes("\tdata_ack(L"+(i+1)+") <= ");
							for(int j=0;j<nChannels;j++){
								if(j!=0)
									data_output.writeBytes("\t\t\t");
								data_output.writeBytes("all_lane_tx(WEST)(L"+(j+1)+")  when tableIn(L"+(i+1)+")=x\"1"+j+"\" and data_av(EAST)(L"+(i+1)+")='1' else\n");
							}
							for(int j=0;j<nChannels;j++)
								data_output.writeBytes("\t\t\tall_lane_tx(NORTH)(L"+(j+1)+") when tableIn(L"+(i+1)+")=x\"2"+j+"\" and data_av(EAST)(L"+(i+1)+")='1' else\n");
							for(int j=0;j<nChannels;j++)
								data_output.writeBytes("\t\t\tall_lane_tx(SOUTH)(L"+(j+1)+") when tableIn(L"+(i+1)+")=x\"3"+j+"\" and data_av(EAST)(L"+(i+1)+")='1' else\n");
							for(int j=0;j<nChannels;j++)
								data_output.writeBytes("\t\t\tall_lane_tx(LOCAL)(L"+(j+1)+") when tableIn(L"+(i+1)+")=x\"4"+j+"\" and data_av(EAST)(L"+(i+1)+")='1' else\n");
							data_output.writeBytes("\t\t\t'0';\n\n");
						}
					}
					else if(word.equalsIgnoreCase("data_ack_west")){
						for(int i=0;i<nChannels;i++){
							data_output.writeBytes("\tdata_ack(L"+(i+1)+") <= ");
							for(int j=0;j<nChannels;j++){
								if(j!=0)
									data_output.writeBytes("\t\t\t");
								data_output.writeBytes("all_lane_tx(EAST)(L"+(j+1)+")  when tableIn(L"+(i+1)+")=x\"0"+j+"\" and data_av(WEST)(L"+(i+1)+")='1' else\n");
							}
							for(int j=0;j<nChannels;j++)
								data_output.writeBytes("\t\t\tall_lane_tx(NORTH)(L"+(j+1)+") when tableIn(L"+(i+1)+")=x\"2"+j+"\" and data_av(WEST)(L"+(i+1)+")='1' else\n");
							for(int j=0;j<nChannels;j++)
								data_output.writeBytes("\t\t\tall_lane_tx(SOUTH)(L"+(j+1)+") when tableIn(L"+(i+1)+")=x\"3"+j+"\" and data_av(WEST)(L"+(i+1)+")='1' else\n");
							for(int j=0;j<nChannels;j++)
								data_output.writeBytes("\t\t\tall_lane_tx(LOCAL)(L"+(j+1)+") when tableIn(L"+(i+1)+")=x\"4"+j+"\" and data_av(WEST)(L"+(i+1)+")='1' else\n");
							data_output.writeBytes("\t\t\t'0';\n\n");
						}
					}
					else if(word.equalsIgnoreCase("data_ack_north")){
						for(int i=0;i<nChannels;i++){
							data_output.writeBytes("\tdata_ack(L"+(i+1)+") <= ");
							for(int j=0;j<nChannels;j++){
								if(j!=0)
									data_output.writeBytes("\t\t\t");
								data_output.writeBytes("all_lane_tx(SOUTH)(L"+(j+1)+") when tableIn(L"+(i+1)+")=x\"3"+j+"\" and data_av(NORTH)(L"+(i+1)+")='1' else\n");
							}
							for(int j=0;j<nChannels;j++)
								data_output.writeBytes("\t\t\tall_lane_tx(LOCAL)(L"+(j+1)+") when tableIn(L"+(i+1)+")=x\"4"+j+"\" and data_av(NORTH)(L"+(i+1)+")='1' else\n");
							data_output.writeBytes("\t\t\t'0';\n\n");
						}
					}
					else if(word.equalsIgnoreCase("data_ack_south")){
						for(int i=0;i<nChannels;i++){
							data_output.writeBytes("\tdata_ack(L"+(i+1)+") <= ");
							for(int j=0;j<nChannels;j++){
								if(j!=0)
									data_output.writeBytes("\t\t\t");
								data_output.writeBytes("all_lane_tx(NORTH)(L"+(j+1)+") when tableIn(L"+(i+1)+")=x\"2"+j+"\" and data_av(SOUTH)(L"+(i+1)+")='1' else\n");
							}
							for(int j=0;j<nChannels;j++)
								data_output.writeBytes("\t\t\tall_lane_tx(LOCAL)(L"+(j+1)+") when tableIn(L"+(i+1)+")=x\"4"+j+"\" and data_av(SOUTH)(L"+(i+1)+")='1' else\n");
							data_output.writeBytes("\t\t\t'0';\n\n");
						}
					}
					else if(word.equalsIgnoreCase("process")){
						data_output.writeBytes("\tprocess(");
						for(int i=0;i<nChannels;i++){
							if(i!=nChannels-1)
								data_output.writeBytes("c"+(i+1)+",");
							else
								data_output.writeBytes("c"+(i+1)+")\n");
						}
						data_output.writeBytes("\tbegin\n");
						for(int i=nChannels;i>0;i--){
							if(i==nChannels)
								data_output.writeBytes("\t\tif c"+i+"='1' then aux_lane_tx<=\""+Convert.decToBin((int)Math.pow(2,i-1),nChannels)+"\"; indice <= tableOut(L"+i+");\n");
							else
								data_output.writeBytes("\t\telsif c"+i+"='1' then aux_lane_tx<=\""+Convert.decToBin((int)Math.pow(2,i-1),nChannels)+"\"; indice <= tableOut(L"+i+");\n");
						}
						data_output.writeBytes("\t\telse aux_lane_tx<=\""+Convert.decToBin(0,nChannels)+"\"; end if;\n");
						data_output.writeBytes("\tend process;\n");
					}
					else{
						if(cont==nTokens-1)
							data_output.writeBytes(word+"\n");
						else
							data_output.writeBytes(word);
					}
				}
				if(nTokens==0) //linha em branco
					data_output.writeBytes("\n");
				n_lines++;
				line=buff.readLine();
			} //end while
			buff.close();
			data_output.close();
			inFile.close();
		}catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't write Hermes_outport.vhd","Output error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}catch(Exception e){
			JOptionPane.showMessageDialog(null,"Can't write Hermes_outport.vhd\n" + e.getMessage(),"Error Message", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}

}
