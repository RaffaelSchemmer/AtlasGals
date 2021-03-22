#------------------------------------------------------------------------#
#																		 #
#  Atlas Makefile														 #
#  Data de Criação : 08/03/2012											 #
#  Última revisão  : 08/03/2012	/ 18:30 / R.Schemmer					 #
#  Codificação     : UTF-8 "Criado pela ferramenta Geany"				 #
#------------------------------------------------------------------------#

#------------------------------------------------------------------------#
#																		 #
#  Lista de Diretórios													 #
#																		 #
#------------------------------------------------------------------------#

# Setar o caminho absoluto da Atlas antes de executar o makefile

# Caminho global onde a Atlas esta "Instalada"
GP= /home/raffael/gals/

#------------------------------------------------------------------------#
#																		 #
#  Lista de Flags para o compilador										 #
#																		 #
#------------------------------------------------------------------------#

JF= -classpath . -Xlint:none

#------------------------------------------------------------------------#
#																		 #
#  Lista de Comandos de Ferramentas										 #
#																		 #
#------------------------------------------------------------------------#

J= java
JC= javac
RM= rm -Rf

#------------------------------------------------------------------------#
#																		 #
#  Lista de Subdiretórios												 #
#																		 #
#------------------------------------------------------------------------#

APD= AtlasPackage/
HFD= Hefestus/
HCRCD= HermesCRC/
HGD= HermesG/
HSRD= HermesSR/
HTBD= HermesTB/
HTUD= HermesTU/
JPD= Jupiter/
MAD= Maia/
TSD= Generator/
TSRD= Tester/
TRMBD= TrafficMbps/
TRMEAD= TrafficMeasurer/

#------------------------------------------------------------------------#
#																		 #
#  atlas : Parametro para compilar todos os subdiretórios da Atlas		 #
#																		 #
#------------------------------------------------------------------------#

atlas: atlaspackage hefestus hermescrc hermesg hermessr hermestb hermestu jupiter maia tester trafficmbps trafficmeasurer root
atlasc: cleanatlas atlaspackage hefestus hermescrc hermesg hermessr hermestb hermestu jupiter maia generator tester trafficmbps trafficmeasurer root

#-------------------------------------------------------------------------------------#
#  Parametros para compilar subdiretórios								 		      #
#																		 		      #
#  atlaspackage    : Compila arquivos do pacote AtlasPackage			  		      #
#  hefestus		   : Compila arquivos do pacote de avaliação de potência da Hermes    #
#  hermescrc	   : Compila arquivos do pacote da rede HermesCRC   		 	      #
#  hermesg		   : Compila arquivos do pacote da rede	HermesG					      #
#  hermessr		   : Compila arquivos do pacote da rede	HermesSR				      #
#  hemestb		   : Compila arquivos do pacote da rede	HermesTB				      #
#  hemestu		   : Compila arquivos do pacote da rede	HermesTU				      #
#  jupiter		   : Compila arquivos do pacote da rede	Mercury					      #
#  maia			   : Compila arquivos do pacote da rede	Hermes					      #
#  generator	   : Compila arquivos do pacote da ferramenta de teste S/Interface    #
#  tester		   : Compila arquivos do pacote da ferramenta de teste C/Interface    #
#  trafficmbps     : Compila arquivos do pacote da ferramenta de Geração de tráfego   #
#  trafficmeasurer : Compila arquivos do pacote da ferramenta de Avaliação de tráfego #	
#  generator	   : Compila arquivos do gerador de redes e tráfego broadcast  	 	  #												 		 
#  root			   : Compila arquivos da interface grafica principal da Atlas  	 	  #
#-------------------------------------------------------------------------------------#

atlaspackage: $(APD)AvailableClock.java $(APD)Clock.java $(APD)Convert.java $(APD)Default.java $(APD)ExampleFileFilter.java $(APD)ExponentialTraffic.java $(APD)FileChooserDemo.java $(APD)Help.java $(APD)JPanelImage.java $(APD)JPanel_Noc.java $(APD)ManipulateFile.java $(APD)NoC.java $(APD)NoCGeneration.java $(APD)NoCGenerationCB.java $(APD)NoCGenerationGUI.java $(APD)NoCGenerationHS.java $(APD)NoCGenerationVC.java $(APD)NormalTraffic.java $(APD)ParetoTraffic.java $(APD)ProgressBarFrame.java $(APD)Project.java $(APD)Router.java $(APD)RouterTraffic.java $(APD)Scenery.java $(APD)SCInputModule.java $(APD)SCOutputModule.java $(APD)SCOutputModuleRouter.java $(APD)SR4Traffic.java $(APD)Traffic.java $(APD)UniformTraffic.java
	$(JC) $(JF) $(GP)$(APD)AvailableClock.java $(GP)$(APD)Clock.java $(GP)$(APD)Convert.java $(GP)$(APD)Default.java $(GP)$(APD)ExampleFileFilter.java $(GP)$(APD)ExponentialTraffic.java $(GP)$(APD)FileChooserDemo.java $(GP)$(APD)Help.java $(GP)$(APD)JPanelImage.java $(GP)$(APD)JPanel_Noc.java $(GP)$(APD)ManipulateFile.java $(GP)$(APD)NoC.java $(GP)$(APD)NoCGeneration.java $(GP)$(APD)NoCGenerationCB.java $(GP)$(APD)NoCGenerationGUI.java $(GP)$(APD)NoCGenerationHS.java $(GP)$(APD)NoCGenerationVC.java $(GP)$(APD)NormalTraffic.java $(GP)$(APD)ParetoTraffic.java $(GP)$(APD)ProgressBarFrame.java $(GP)$(APD)Project.java $(GP)$(APD)Router.java $(GP)$(APD)RouterTraffic.java $(GP)$(APD)Scenery.java $(GP)$(APD)SCInputModule.java $(GP)$(APD)SCOutputModule.java $(GP)$(APD)SCOutputModuleRouter.java $(GP)$(APD)SR4Traffic.java $(GP)$(APD)Traffic.java $(GP)$(APD)UniformTraffic.java $(GP)$(APD)Cost.java $(GP)$(APD)Mapping.java $(GP)$(APD)Dependance.java
	
hefestus: $(HFD)CreditBased.java $(HFD)Handshake.java $(HFD)Hefestus.java $(HFD)Monitor.java $(HFD)Power.java $(HFD)VirtualChannel.java
	$(JC) $(JF) $(GP)$(HFD)CreditBased.java $(GP)$(HFD)Handshake.java $(GP)$(HFD)Hefestus.java $(GP)$(HFD)Monitor.java $(GP)$(HFD)Power.java $(GP)$(HFD)VirtualChannel.java
	
hermescrc: $(HCRCD)CRCCreditBased.java $(HCRCD)CRCInterface.java $(HCRCD)HermesCRC.java
	$(JC) $(JF) $(GP)$(HCRCD)CRCCreditBased.java $(GP)$(HCRCD)CRCInterface.java $(GP)$(HCRCD)HermesCRC.java
	
hermesg: $(HGD)HermesGCreditBased.java $(HGD)HermesGInterface.java $(HGD)InterfaceClock.java $(HGD)InterfaceEditClock.java $(HGD)InterfaceNewClock.java $(HGD)InterfaceRemoveClock.java
	$(JC) $(JF) $(GP)$(HGD)HermesGCreditBased.java $(GP)$(HGD)HermesGInterface.java $(GP)$(HGD)InterfaceClock.java $(GP)$(HGD)InterfaceEditClock.java $(GP)$(HGD)InterfaceNewClock.java $(GP)$(HGD)InterfaceRemoveClock.java
	
hermessr: $(HSRD)CV4_ctrl_gs_be.java $(HSRD)HermesSR.java $(HSRD)HermesSRCreditBased.java $(HSRD)HermesSRInterface.java
	$(JC) $(JF) $(GP)$(HSRD)CV4_ctrl_gs_be.java $(GP)$(HSRD)HermesSR.java $(GP)$(HSRD)HermesSRCreditBased.java $(GP)$(HSRD)HermesSRInterface.java
	
hermestb: $(HTBD)HermesTB.java $(HTBD)HermesTBCreditBased.java $(HTBD)HermesTBInterface.java
	$(JC) $(JF) $(GP)$(HTBD)HermesTB.java $(GP)$(HTBD)HermesTBCreditBased.java $(GP)$(HTBD)HermesTBInterface.java
		
hermestu: $(HTUD)HermesTU.java $(HTUD)HermesTUInterface.java $(HTUD)HermesTUVirtualChannel.java
	$(JC) $(JF) $(GP)$(HTUD)HermesTU.java $(GP)$(HTUD)HermesTUInterface.java $(GP)$(HTUD)HermesTUVirtualChannel.java
	
jupiter: $(JPD)Jupiter.java $(JPD)JupiterInterface.java $(JPD)NocComponentsCreator.java
	$(JC) $(JF) $(GP)$(JPD)Jupiter.java $(GP)$(JPD)JupiterInterface.java $(GP)$(JPD)NocComponentsCreator.java
	
maia: $(MAD)FixedPriority.java $(MAD)Maia.java $(MAD)MaiaInterface.java $(MAD)MainCreditBased.java $(MAD)MainHandshake.java $(MAD)MainVirtualChannel.java
	$(JC) $(JF) $(GP)$(MAD)FixedPriority.java $(GP)$(MAD)Maia.java $(GP)$(MAD)MaiaInterface.java $(GP)$(MAD)MainCreditBased.java $(GP)$(MAD)MainHandshake.java $(GP)$(MAD)MainVirtualChannel.java

generator: $(TSD)Generator.java
	$(JC) $(JF) $(GP)$(TSD)Generator.java
	
tester: $(TSRD)Tester.java
	$(JC) $(JF) $(GP)$(TSRD)Tester.java

trafficmbps: $(TRMBD)Generate.java $(TRMBD)InterfaceConfig.java $(TRMBD)InterfaceConfigSRCV.java $(TRMBD)ReadTrafficCDCM.java $(TRMBDSpatialDistribution.java $(TRMBD)TimeDistribution.java $(TRMBD)TrafficMbps.java
	$(JC) $(JF) $(GP)$(TRMBD)Generate.java $(GP)$(TRMBD)InterfaceConfig.java $(GP)$(TRMBD)InterfaceConfigSRCV.java $(GP)$(TRMBD)ReadTrafficCDCM.java $(GP)$(TRMBD)SpatialDistribution.java $(GP)$(TRMBD)TimeDistribution.java $(GP)$(TRMBD)TrafficMbps.java
	
trafficmeasurer: $(TRMEAD)Channel.java $(TRMEAD)DistrLat.java $(TRMEAD)DistrRate.java $(TRMEAD)DistrThroughput.java $(TRMEAD)GlobalReport.java $(TRMEAD)Graph3D.java $(TRMEAD)Graph3D_VC.java $(TRMEAD)GraphPoint.java $(TRMEAD)GraphTXT.java $(TRMEAD)GraphTXT_VC.java $(TRMEAD)LatencyReport.java $(TRMEAD)LinkReport.java $(TRMEAD)LinkReport_VC.java $(TRMEAD)OpenWindow.java $(TRMEAD)TextAreaDemo.java $(TRMEAD)TrafficInterface.java $(TRMEAD)TrafficMeasurer.java
	$(JC) $(JF) $(GP)$(TRMEAD)Channel.java $(GP)$(TRMEAD)DistrLat.java $(GP)$(TRMEAD)DistrRate.java $(GP)$(TRMEAD)DistrThroughput.java $(GP)$(TRMEAD)GlobalReport.java $(GP)$(TRMEAD)Graph3D.java $(GP)$(TRMEAD)Graph3D_VC.java $(GP)$(TRMEAD)GraphPoint.java $(GP)$(TRMEAD)GraphTXT.java $(GP)$(TRMEAD)GraphTXT_VC.java $(GP)$(TRMEAD)LatencyReport.java $(GP)$(TRMEAD)LinkReport.java $(GP)$(TRMEAD)LinkReport_VC.java $(GP)$(TRMEAD)OpenWindow.java $(GP)$(TRMEAD)TextAreaDemo.java $(GP)$(TRMEAD)TrafficInterface.java $(GP)$(TRMEAD)TrafficMeasurer.java

root: $(GP)Atlas.java $(GP)InterfacePrincipal.java $(GP)ModelsimThread.java $(GP)ProjectWindow.java $(GP)SimulationWindow.java
	$(JC) $(JF) $(GP)Atlas.java $(GP)InterfacePrincipal.java $(GP)ModelsimThread.java $(GP)ProjectWindow.java $(GP)SimulationWindow.java

#--------------------------------------------------------------------------------------------#
#  Parametros para deletar arquivos de compilação de sub diretórios		 		    	  	 #
#																		 		    	  	 #
#  cleanatlas    	    : Deleta os arquivos binários de todos os arquivos da Atlas 	  	 #
#  cleanatlaspackage    : Deleta os arquivos binários do pacote AtlasPackage 			  	 #
#  cleanhefestus   	    : Deleta os arquivos binários da ferramenta de avaliação de potência #
#  cleanhermescrc	    : Deleta os arquivos binários da rede HermesCRC 					 #
#  cleanhermesg	        : Deleta os arquivos binários da rede HermesG 						 #
#  cleanhermessr	    : Deleta os arquivos binários da rede HermesSR 						 #
#  cleanhermestb	    : Deleta os arquivos binários da rede HermesTB 					 	 #
#  cleanhermestu	    : Deleta os arquivos binários da rede HermesTU 					 	 #
#  cleanjupiter		    : Deleta os arquivos binários da rede Mercury 					 	 #
#  cleanmaia	        : Deleta os arquivos binários da rede Hermes 						 #
#  cleangenerator       : Deleta os arquivos binários da ferramenta de testes S/Interface    #
#  cleantester          : Deleta os arquivos binários da ferramenta de testes C/Interface    #
#  cleantrafficmbps     : Deleta os arquivos binários da ferramenta de Geração de tráfego    #
#  cleantrafficmeasurer : Deleta os arquivos binários da ferramenta de Avaliação de tráfego  #
#--------------------------------------------------------------------------------------------#

cleanatlas: $(GP)*.class $(APD)*.class $(HFD)*.class $(HCRCD)*.class $(HGD)*.class $(HSRD)*.class $(HTBD)*.class $(HTUD)*.class $(JPD)*.class $(MAD)*.class $(TSD)*.class $(TSRD)*.class $(TRMBD)*.class $(TRMEAD)*.class
	$(RM) $(GP)*.class $(GP)$(APD)*.class $(GP)$(HFD)*.class $(GP)$(HCRCD)*.class $(GP)$(HGD)*.class $(GP)$(HSRD)*.class $(GP)$(HTBD)*.class $(GP)$(HTUD)*.class $(GP)$(JPD)*.class $(GP)$(MAD)*.class $(GP)$(TSD)*.class $(GP)$(TSRD)*.class $(GP)$(TRMBD)*.class $(GP)$(TRMEAD)*.class
	
cleanatlaspackage: $(APD)*.class
	$(RM) $(GP)$(APD)*.class
	
cleanhefestus: $(HFD)*.class
	$(RM) $(GP)$(HFD)*.class

cleanhermescrc:$(HCRCD)*.class
	$(RM) $(HCRCD)*.class

cleanhermesg: $(HGD)*.class 
	$(RM) $(HGD)*.class 

cleanhermessr: $(HSRD)*.class 
	$(RM) $(HSRD)*.class 

cleanhermestb: $(HTBD)*.class 
	$(RM) $(HTBD)*.class 

cleanhermestu: $(HTUD)*.class 
	$(RM) $(HTUD)*.class 

cleanjupiter: $(JPD)*.class 
	$(RM) $(JPD)*.class 

cleanmaia: $(MAD)*.class 
	$(RM) $(MAD)*.class 

cleangenerator : $(TSD)*.class 
	$(RM) $(TSD)*.class 

cleantester: $(TSRD)*.class 
	$(RM) $(TSRD)*.class 

cleantrafficmbps: $(TRMBD)*.class 
	$(RM) $(TRMBD)*.class 

cleantrafficmeasurer: $(TRMEAD)*.class
	$(RM) $(TRMEAD)*.class
