package HermesG;

import javax.swing.*;
import java.io.*;
import java.util.*;

import AtlasPackage.NoCGenerationCB;
import AtlasPackage.NoC;
import AtlasPackage.Default;
import AtlasPackage.ManipulateFile;
import AtlasPackage.Project;
import AtlasPackage.Convert;
/**
 * Generate a Hermes G NoC.
 * @author Raffael Bottoli Schemmer
 * @version
 */
public class HermesGCreditBased extends NoCGenerationCB
{
	private static String sourceDir = Default.atlashome + File.separator
			+ "HermesG" + File.separator + "Data" + File.separator
			+ "CreditBased" + File.separator;

	private String projectDir, nocDir, scDir;
	private String nocType;
	private int dimX, dimY, flitSize;
	private boolean isSC;
	private NoC noc;
	private Project p;
	private ArrayList<String> router_names = new ArrayList<String>();
	// Variavel que armazena o sulfixo de cada roteador

	/**
	 * Generate a Hermes G NoC.
	 * 
	 * @param project
	 *            The NoC p.
	 */
	public HermesGCreditBased(Project project)
	{
		super(project, sourceDir);
		p = project;
		noc = p.getNoC();
		nocType = noc.getType();
		dimX = noc.getNumRotX();
		dimY = noc.getNumRotY();
		flitSize = noc.getFlitSize();
		isSC = noc.isSCTB();
		projectDir = p.getPath() + File.separator;
		nocDir     = projectDir + "NOC" + File.separator;
		scDir      = projectDir + "SC_NoC" + File.separator;
		
	}
	/*
		Esta função efetua a geração de todos os arquivos referentes ao roteador HERMES-G
	*/
	public void generate() 
	{
		
		//create the project directory
		File diretory = new File(projectDir);
		diretory.mkdirs();

		//create the NoC directory
		File nDir = new File(projectDir + File.separator + "NOC");
		nDir.mkdirs();
		
					
		/* Responsavel pela cópia dos arquivos referentes ao Crossbar para a pasta NoC */
		
		// ManipulateFile.copy(new File(sourceDir + "HermesG_crossbar.vhd"), nocDir);
		// Minimização full do crossbar(Geração dos arquivos)
		
		ManipulateFile.copy(new File(sourceDir + "HermesG_crossbar_BL.vhd"), nocDir);
		ManipulateFile.copy(new File(sourceDir + "HermesG_crossbar_BC.vhd"), nocDir);
		ManipulateFile.copy(new File(sourceDir + "HermesG_crossbar_BR.vhd"), nocDir);
		ManipulateFile.copy(new File(sourceDir + "HermesG_crossbar_CL.vhd"), nocDir);
		ManipulateFile.copy(new File(sourceDir + "HermesG_crossbar_CC.vhd"), nocDir);
		ManipulateFile.copy(new File(sourceDir + "HermesG_crossbar_CR.vhd"), nocDir);
		ManipulateFile.copy(new File(sourceDir + "HermesG_crossbar_TL.vhd"), nocDir);
		ManipulateFile.copy(new File(sourceDir + "HermesG_crossbar_TC.vhd"), nocDir);
		ManipulateFile.copy(new File(sourceDir + "HermesG_crossbar_TR.vhd"), nocDir);
		
		
		/* Responsavel pela cópia do arquivo HermesG_switchcontrol.vhd para a pasta NoC */
		ManipulateFile.copy(new File(sourceDir + "HermesG_switchcontrol.vhd"), nocDir);
		
		/* Responsavel pela cópia do arquivo HermesG_package.vhd e geração para a pasta NoC com base na parametrização do template */
		createPackageG("HermesG_package.vhd");

		/* Método responsável pela geração do arquivo NOC.vhd */
		generateNOC();

		/* Responsavel pela cópia do arquivo Router.vhd e geração para a pasta NoC com base na parametrização do template */
		generateRouter();

		/* Método responsável pela geração do arquivo HermesG_Buffer */
		generateBufferG();
		
		
		/* Só gera tráfego se estiver usando um gerador externo
		if(p.getTb().equals("1"))
		{
			
			String scDir      = projectDir + "SC_NoC" + File.separator;
		
			File sDir=new File(scDir);
			sDir.mkdirs();

			// Responsavel pela cópia do arquivo SC_InputModule.cpp para a pasta SC_NOC 
						
			ManipulateFile.copy(new File(sourceDir + "SC_InputModule.cpp"), scDir);
						
			// Responsavel pela cópia do arquivo SC_OutputModule.cpp para a pasta SC_NOC 
						
			ManipulateFile.copy(new File(sourceDir + "SC_OutputModule.cpp"), scDir);
						
			// Responsavel pela cópia do arquivo SC_OutputModuleRouter.cpp para a pasta SC_NOC
			// ManipulateFile.copy(new File(sourceDir + "SC_OutputModuleRouter.cpp"), scDir);

			// Método responsável pela geração do arquivo simulate.do
			createSimulate();

			// Método responsável pela geração do arquivo topNOC.vhd 
			generatetopNoC();

			// Método responsável pela geração  dos arquivos SystemC OutputModule 
			generateSystemCOutputModule();

			// Método responsável pela geração dos arquivos SystemC InputModule 
			generateSystemCIntputModule();

			// Método responsável pela geração do arquivo Async_fifo2.vhd 
			generateAsyncFifo();

			// Método responsável pela geração dos arquivos SystemC OutputModuleRouter
			// generateSystemCOutputModuleRouter();	
		}
		*/		
	}
	public String calcSimulateWindow()
	{
		//
		// Passos para cálcular o maior tempo de simulação
		//
		// 1 - Encontrar maior clock
		// 2 - Converter para tempo absoluto em ns
		// 3 -  
		
		// Procura o maior clock cadastrado para os roteadores e ips
		double max=0;
		String value = new String();
		
		for(int tmp=0;tmp < noc.getClock().size();tmp++)
		{
			if(max < noc.getClock().get(tmp).getClockRouter()) max = noc.getClock().get(tmp).getClockRouter();
			if(max < noc.getClock().get(tmp).getClockIpInput()) max = noc.getClock().get(tmp).getClockIpInput();
			if(max < noc.getClock().get(tmp).getClockIpOutput()) max = noc.getClock().get(tmp).getClockIpOutput();
		}
		
		// Calcula o valor do tempo do reset
		double sum=0;
		int n=0;
					
		// Procurar todos os clocks cadastrados.
		for(int tmp=0;tmp < noc.getRefClockList().size();tmp++)
		{
			// Primeiro valor da lista de clocks, verificar se ele esta atribuido para um roteador ou IP.
			for(int tmp1=0;tmp1 < noc.getClock().size();tmp1++)
			{
				if(noc.getClock().get(tmp1).getRouter().equals(noc.getRefClockList().get(tmp).getAllAvailableValue()))
				{
					sum = sum + noc.getClock().get(tmp1).getClockRouter();
					n++;
					break;
				}
				else if(noc.getClock().get(tmp1).getIpInput().equals(noc.getRefClockList().get(tmp).getAllAvailableValue()))
				{
					sum = sum + noc.getClock().get(tmp1).getClockIpInput();
					n++;
					break;
				}
				else if(noc.getClock().get(tmp1).getIpOutput().equals(noc.getRefClockList().get(tmp).getAllAvailableValue()))
				{
					sum = sum + noc.getClock().get(tmp1).getClockIpOutput();
					n++;
					break;
				}
			}
		}
		
		sum = sum/n;
		
		// Verifica se o reset é maior que o tempo do maior clock
		if(max < sum) max = sum;
		
		max = 1000/max;
		max = max/2;
		
		// System.out.println("Valor de max a ser cálculado : " + max);
		
		/* Este conjunto de condições, converte o maior clock armazenado para a rede e os IPs de Mhz na unidade referente em tempo absoluto a seu conjunto de casas decimais.
		
			- Exemplos de calculo :
			
			- "Este número representa 500Ghz que em tempo absoluto representa 2ps". Forma como é calculado originalmente 500.000 Mhz = 0.002 ns 
			- O bloco de condições "descobre o valor e sua respectiva unidade, juntamente com o valor ideal entre valores dentre 1/10/100".
			 
		*/
		
		// Khz - Mhz - ms (1/10/100)
		if(max >= 1000000 && max <= 1000000000) // Menor que 1Khz(1ms) e maior que 1Hz(1000ms) "ms"
		{
			value = "1ms";
			// if(max >= 1000000 && max <= 10000000) value = "1ms";
			// else if(max >= 10000000 && max <= 100000000) value = "10ms";
			// else if(max >= 100000000) value = "100ms";
		}
		// Mhz - Ghz - us (1/10/100)
		else if(max >= 1000 && max <= 1000000) // Menor que 1Mhz(1us) e maior que 1Khz(1000us) "us"
		{
			value = "1us";
			// if(max >= 1000 && max <= 10000) value = "1us";
			// else if(max >= 10000 && max <= 100000) value = "10us";
			// else if(max >= 100000) value = "100us";
		}
		// Ghz - Thz - ns (1/10/100)
		else if(max >= 1 && max <= 1000) // Menor que 1Ghz(1ns) e maior que 1Mhz(1000ns) "ns"
		{
			value = "1ns";
			// if(max >= 1 && max <= 10) value = "1ns";
			// else if(max >= 10 && max <= 100) value = "10ns";
			// else if(max >= 100) value = "100ns";
		}
		// Thz - Phz - ps (1/10/100)
		else if(max >= 0.001 && max <= 1) // Menor que 1Thz(1ps) e maior que 1Ghz(1000ps) "ps"
		{
			value = "1ps";
			// if(max >= 0.001 && max <= 0.01) value = "1ps";
			// else if(max >= 0.01 && max <= 0.1) value = "10ps";
			// else if(max >= 0.1) value = "100ps";
		}
		// Phz - Ehz - fs (1/10/100)
		else if(max >= 0.000001 && max <= 0.001) // Menor que 1Phz(1fs) e maior que 1Thz(1000fs) "fs"
		{
			value = "1fs";
			// if(max >= 0.000001 && max <= 0.00001) value = "1fs";
			// else if(max >= 0.00001 && max <= 0.0001) value = "10fs";
			// else if(max >= 0.0001) value = "100fs";
		}
		
		// System.out.println("Value : " + value);
		return(value);
	}
	public void createSimulate(int typeApp)
	{

		try
		{
			FileOutputStream script = new FileOutputStream(projectDir + "simulate.do");
			DataOutputStream data_output = new DataOutputStream(script);
			// vlib and vmap
			writeSimulateHeader(data_output);
			
			// Geração das diretivas de sscom no arquivo simulate.do )
			if(typeApp == 0)
			{
				writeSCCOM(data_output, "SC_NoC/SC_InputModule.cpp");
				//writeSCCOM(data_output, "SC_NoC/SC_OutputModuleRouter.cpp");
				writeSCCOM(data_output, "SC_NoC/SC_OutputModule.cpp\nsccom -link\n");
			}
			if(typeApp == 1)
			{
				writeSCCOM(data_output, "SC_NoC/SC_CDCG_InputModule.cpp");
				//writeSCCOM(data_output, "SC_NoC/SC_OutputModuleRouter.cpp");
				writeSCCOM(data_output, "SC_NoC/SC_CDCG_OutputModule.cpp\nsccom -link\n");
			}
			writeVCOM(data_output, "NOC/HermesG_package.vhd");
			writeVCOM(data_output, "NOC/HermesG_buffer.vhd");
			writeVCOM(data_output, "NOC/HermesG_switchcontrol.vhd");
			// writeVCOM(data_output, "NOC/HermesG_crossbar.vhd");
			
			// Minimização full do crossbar(Geração do arquivo de compilação)
			
			writeVCOM(data_output, "NOC/HermesG_crossbar_BL.vhd");
			writeVCOM(data_output, "NOC/HermesG_crossbar_BC.vhd");
			writeVCOM(data_output, "NOC/HermesG_crossbar_BR.vhd");
			writeVCOM(data_output, "NOC/HermesG_crossbar_CL.vhd");
			writeVCOM(data_output, "NOC/HermesG_crossbar_CC.vhd");
			writeVCOM(data_output, "NOC/HermesG_crossbar_CR.vhd");
			writeVCOM(data_output, "NOC/HermesG_crossbar_TL.vhd");
			writeVCOM(data_output, "NOC/HermesG_crossbar_TC.vhd");
			writeVCOM(data_output, "NOC/HermesG_crossbar_TR.vhd");
			
			
			// writeVCOM(data_output, "NOC/HermesG_Fifo_Output.vhd");
			// writeVCOM(data_output, "NOC/HermesG_Fifo_SC_OutputModuleRouter.vhd");
			// Bloco responsavel por escrever as chaves no arquivo simulate.do
			for(int i=0;i<noc.getNumRotX() *  noc.getNumRotY();i++)
			{
				writeVCOM(data_output, "NOC/" + noc.getClock().get(i).getNameRouter() + ".vhd");
			}
			for(int tmp=0;tmp < noc.getClock().size();tmp++)
			{
				if(noc.getClock().get(tmp).getClockRouter() != noc.getClock().get(tmp).getClockIpOutput())
				{
					writeVCOM(data_output, "NOC/HermesG_Fifo_Output.vhd");
					break;
				}
			}
			writeVCOM(data_output, "NOC/NOC.vhd");
			if(typeApp == 0)
				writeVCOM(data_output, "topNoC.vhd");
			if(typeApp == 1)
				writeVCOM(data_output, "CDCG_topNoC.vhd");
			// vsim top Entity
			// writeVSIM(data_output, "topNoC"); 
			if(typeApp == 0)
				data_output.writeBytes("\nvsim -novopt -t 100fs work.topNoC\n\n");
			if(typeApp == 1)
				data_output.writeBytes("\nvsim -novopt -t 100fs work.CDCG_topNoC\n\n");
			// set StdArithNoWarnings
			writeNoWarnings(data_output);
			// run simulation
			if(typeApp == 0)
				data_output.writeBytes("\nwhen -label NocSim {/topnoc/sim=='1'} { echo \"parando\";quit -sim;quit -f;}\n\n");
			if(typeApp == 1)
				data_output.writeBytes("\nwhen -label NocSim {/CDCG_topNoC/sim=='1'} { echo \"parando\";quit -sim;quit -f;}\n\n");
			data_output.writeBytes("\nrun -all\n\n");
			
			// writeRUN(data_output);
			
			// quit simulation 
			// writeSimulateFooter(data_output);
			data_output.close();
		}
		catch(FileNotFoundException f)
		{
			JOptionPane.showMessageDialog(null,"Can't write simukate.do script","Output error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		catch(Exception e)
		{
			JOptionPane.showMessageDialog(null,"Can't write simukate.do script"+e.getMessage(),"Error Message", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}
	
	/*********************************************************************************
	 * SystemC
	 *********************************************************************************/

	/**
	 * Creates the SystemC files
	 */
	public void createSC()
	{
		// copy CPP files to SC_NoC directory
		// copySCFiles();
		// replacing the flags in c_input_module.c file.
	   	// SCInputModule.createFile(NoC.CB, sourceDir, scDir, dimX, dimY, flitSize, 1);
		// replacing the flags in c_output_module.c file.
	   	// SCOutputModule.createFile(NoC.CB, sourceDir, scDir, dimX, dimY, flitSize, 1);
		// replacing the flags in c_output_module_router.c file.
		// SCOutputModuleRouter.createFile(sourceDir, scDir, nocType, NoC.CB, dimX, dimY, flitSize, 1);
		// create the top.cpp file using SC files.
		// createTopNoC("HermesTBPackage");
		// create the simulation scripts using SC files
		
	}
	
	/*********************************************************************************
	 * SCRIPTS
	 *********************************************************************************/
	
	/**
	 * Create the simulation script used by Modelsim. 
	 */
	public void createSimulateScript()
	{
		try{
			FileOutputStream script = new FileOutputStream(projectDir + "simulate.do");
			DataOutputStream data_output = new DataOutputStream(script);
			// vlib and vmap
			writeSimulateHeader(data_output);
			// sccom SystemC files
			writeSCCOMFiles(data_output);
			// vcom VHDL files
			writeVCOMFiles(data_output);
			// vsim top Entity
			writeVSIM(data_output, "topNoC");
			// set StdArithNoWarnings
			writeNoWarnings(data_output);
			// run simulation
			writeRUN(data_output);
			// quit simulation 
			writeSimulateFooter(data_output);
			data_output.close();
		}catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't write simukate.do script","Output error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}catch(Exception e){
			JOptionPane.showMessageDialog(null,"Can't write simukate.do script"+e.getMessage(),"Error Message", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}
}
