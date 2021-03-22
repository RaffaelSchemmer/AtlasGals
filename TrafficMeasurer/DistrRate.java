package TrafficMeasurer;

import java.io.*;

import javax.swing.*;
import java.util.*;

import AtlasPackage.NoC;
import AtlasPackage.Convert;
import AtlasPackage.Router;
import AtlasPackage.Default;
import AtlasPackage.Project;

/**
 * This class evaluates the occupation of each (physical) channel in the traffic path. <p>
 * When NoC contains virtual channels, all virtual channels are analyzed. 
 * @author Aline Vieira de Mello
 * @version
 */
public class DistrRate{
	private NoC noc;
	private int numIntervalos, perc, numCV, numRot, numPort,flitSize;
	private String dirEntrada, dirSaida, origem, destino, type;
	private int nRotX,nRotY;
	private double media=0;
	private boolean[][][] isData;

	/**
	 * Constructor class.
	 * @param project The selected project.
	 * @param source The source router.
	 * @param target The target router.
	 * @param interval The interval.
	 * @param percentage The discarded percentage.
	 */
    public DistrRate(Project project, String source, String target, int interval, int percentage){
		origem = source;
		destino = target;
		numIntervalos = interval;
		perc = percentage;

    	noc = project.getNoC();
		nRotX = noc.getNumRotX();
		nRotY = noc.getNumRotY();
		flitSize = noc.getFlitSize();
		numCV = noc.getVirtualChannel();
		type = noc.getType();
		numRot = nRotX*nRotY;
		numPort = 4;
		dirSaida = project.getSceneryPath() + File.separator + "Out";
		dirEntrada = dirSaida;

		isData= new boolean[numRot][numPort][numCV];
		for(int rot=0;rot<numRot;rot++){
			for(int port=0;port<numPort;port++){
				for(int lane=0;lane<numCV;lane++){
					isData[rot][port][lane]=false;
				}
			}
		}
		//verify if the DAT file has been created
		if(writeDat())
			writeTxt();
		else
			JOptionPane.showMessageDialog(null,"There are no packets in this flow! "+ destino,"Information",JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Write the DAT file that contains the data showed in the distr_quant_pkts_r<<b>number_of_router</b>>p<<b>number_of_port</b>>.dat graph.
	 * @return True if the file was created.
	 */
	private boolean writeDat(){
		StringTokenizer st;
		String line;
		GraphPoint[] ponto= new GraphPoint[numIntervalos];
		String target="",source="";
		int size=0, initialTime = 0,finalTime = 0;
		int inferior=0,superior=0;
		int interval=0,limitInf=0,limitSup=0;
		int busyChannel,busyChannelMin=0,busyChannelMax=0,nPackets=0;
		int busyChannelMinVir=0,busyChannelMaxVir=0;
		float descarte=0;
		double busyChannelAcum=0,sum_var=0;
		boolean existFlow = false;

		FileOutputStream fos;
		OutputStreamWriter osw;
		Writer out;
		File fo = new File(dirSaida.concat(File.separator + "reports" + File.separator + "dat" + File.separator + "distr_quant_pkts_r0p0.dat"));

		FileInputStream fis;
		BufferedReader br;
		File fi = new File(dirEntrada.concat(File.separator + "r0p0.txt"));

		try{
			for(int rot=0;rot<numRot;rot++){
				for(int port=0;port<numPort;port++){
					for(int lane=0;lane<numCV;lane++){
						if(numCV == 1)
							fi = new File(dirEntrada.concat(File.separator + "r"+rot+"p"+port+".txt"));
						else
							fi = new File(dirEntrada.concat(File.separator + "r"+rot+"p"+port+"l"+lane+".txt"));

						if(fi.exists()){
							fis=new FileInputStream(fi);
							br=new BufferedReader(new InputStreamReader(fis));

							for(int i=0; i<numIntervalos; i++){
								ponto[i] = new GraphPoint();
							}
							inferior=0;
							superior=0;
							interval=0;
							limitInf=0;
							limitSup=0;
							busyChannelMin=0;
							busyChannelMax=0;
							nPackets=0;
							busyChannelMinVir=0;
							busyChannelMaxVir=0;
							descarte=0;
							busyChannelAcum=0;
							sum_var=0;
							initialTime=0;
							line=br.readLine();
							while(line!=null){
								st = new StringTokenizer(line, " ()");
								int nTokens = st.countTokens();

								if(nTokens!=0){
									//advance until end of line
									for(int count=0;count<nTokens;count++){
										if(count==0){
											target = st.nextToken();
											target = target.substring(target.length()/2);
										}
										else if(count==2)
											size = Integer.parseInt(st.nextToken(),16);
										else if(count==4){
											source = st.nextToken();
											source = source.substring(source.length()/2);
										}
										else if(count==nTokens-1){
											finalTime = Integer.parseInt(st.nextToken());
										}
										else
											st.nextToken();
									}

									if(target.equalsIgnoreCase(destino) && source.equalsIgnoreCase(origem)){

										isData[rot][port][lane]=true;

										busyChannel = (int)(((float)(size+2)/(float)(finalTime - initialTime))*100);

										//store the minimal channel occupation
										if(busyChannelMin ==0 || busyChannelMin>busyChannel)
											busyChannelMin=busyChannel;

										//store the maximal channel occupation
										if(busyChannelMax==0 || busyChannelMax<busyChannel)
											busyChannelMax=busyChannel;

										busyChannelAcum+=busyChannel;
										initialTime = finalTime;
										nPackets++;
										existFlow = true;
									}
								}
								line=br.readLine();
							}
							br.close();
							fis.close();
						}

						if(isData[rot][port][lane]){
							descarte=((busyChannelMax-busyChannelMin)*perc)/100;
							busyChannelMinVir=busyChannelMin+(int)descarte;
							busyChannelMaxVir=busyChannelMax-(int)descarte;
							interval=(int)(busyChannelMaxVir-busyChannelMinVir)/numIntervalos;
							media=busyChannelAcum/nPackets;

							for(int i=0; i<numIntervalos; i++){
								if(i==0){
									limitInf=busyChannelMinVir;
									limitSup=limitInf+interval;
								}
								else{
									limitInf=limitSup+1;
									limitSup=limitInf+interval;
								}
								ponto[i].setCoordX((int)((limitSup+limitInf)/2));
							}


							if(numCV == 1)
								fo = new File(dirEntrada.concat(File.separator + "reports" + File.separator + "dat" + File.separator + "distr_quant_pkts_r"+rot+"p"+port+".dat"));
							else
								fo = new File(dirEntrada.concat(File.separator + "reports" + File.separator + "dat" + File.separator + "distr_quant_pkts_r"+rot+"p"+port+"L"+lane+".dat"));

							fis=new FileInputStream(fi);
							br=new BufferedReader(new InputStreamReader(fis));


							initialTime = 0;
							line=br.readLine();
							while(line!=null){
								st = new StringTokenizer(line, " ()");
								int nTokens = st.countTokens();

								if(nTokens!=0){

									//advance until end of line
									for(int count=0;count<nTokens;count++){
										if(count==0){
											target = st.nextToken();
											target = target.substring(target.length()/2);
										}
										else if(count==2)
											size = Integer.parseInt(st.nextToken(),16);
										else if(count==4){
											source = st.nextToken();
											source = source.substring(source.length()/2);
										}
										else if(count==nTokens-1)
											finalTime = Integer.parseInt(st.nextToken());
										else
											st.nextToken();
									}

									if (target.equalsIgnoreCase(destino) && source.equalsIgnoreCase(origem)){

										busyChannel = (int)(((float)(size+2)/(float)(finalTime - initialTime))*100);

										inferior=busyChannelMinVir;
										superior=busyChannelMinVir+interval;
										for(int j=0; j<numIntervalos; j++){
											if(busyChannel>=inferior && busyChannel<=superior){
												ponto[j].setCoordY(ponto[j].getCoordY()+1);
											}
											inferior=superior+1;
											superior=inferior+interval;
										}

										sum_var+=((busyChannel-media)*(busyChannel-media));
										initialTime = finalTime;
									}
								}
								line=br.readLine();
							}

							fos = new FileOutputStream(fo);
							osw = new OutputStreamWriter(fos);
							out = new BufferedWriter(osw);
							for(int i=0; i<numIntervalos; i++){
								out.write(ponto[i].getCoordX() + " " + ponto[i].getCoordY() + "\n");
							}
							out.close();
							osw.close();
							fos.close();
							
							br.close();
							fis.close();
						}
					}
				}
			}
		}
		catch(FileNotFoundException exc){
			JOptionPane.showMessageDialog(null,"Can't open "+fi.getAbsolutePath(),"Input Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		catch(Exception e){
			JOptionPane.showMessageDialog(null,"Error in "+fi.getAbsolutePath(),"Input Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		return existFlow;
	}

	/**
	 * Write the TXT file used by GNUPLOT to show a graph. 
	 */
	private void writeTxt(){
		String aux = new String();
		String str = new String();
		int nportas;
		int roteador=0;
		int source = Convert.getNumberOfRouter(origem,nRotX,flitSize);
		int target = Convert.getNumberOfRouter(destino,nRotX,flitSize);
		int sourceX = Convert.getAddressX(origem,flitSize);
		int sourceY = Convert.getAddressY(origem,flitSize);
		int targetX = Convert.getAddressX(destino,flitSize);
		int targetY = Convert.getAddressY(destino,flitSize);
		int auxTarget=Convert.getNumberOfRouter(Convert.formatAddressXY(""+targetX+sourceY,flitSize),nRotX,flitSize);

		if(type.equalsIgnoreCase("Hermes") || type.equalsIgnoreCase("HermesCRC")){
			try{
				FileOutputStream fos = new FileOutputStream(dirSaida.concat(File.separator + "reports" + File.separator + "graphs_txt" + File.separator + "distr_quant_pkts.txt"));
				OutputStreamWriter osw = new OutputStreamWriter(fos);
				Writer out = new BufferedWriter(osw);

				aux = aux + "reset\n";
				aux = aux + "set dgrid3d 35,35,35\n";
				aux = aux + "set view 45,45,1,1\n";
				aux = aux + "set xrange [0:]\n";
				aux = aux + "set yrange [0:]\n";

				if(sourceX<targetX){
					//follow the path between source router until target router
					for(int rot=source;rot<auxTarget;rot++){
						for(int port=0;port<4;port++){
							for(int lane=0;lane<numCV;lane++){
								if(isData[rot][port][lane]){

									aux = aux + "set xlabel 'Load (%)'\n";
									aux = aux + "set ylabel 'Number of packets'\n";

									if(numCV==1){
										str = new String(dirSaida.concat(File.separator + "reports" + File.separator + "dat" + File.separator + "distr_quant_pkts_r"+rot+"p"+port+".dat"));
										aux = aux + "plot '"+str+"' using ($1):($2) t\"router "+Convert.getXYAddress(rot,nRotX,16)+" port "+Router.getPortName(port)+" source "+Convert.getXYAddress(source,nRotX,16)+" target "+Convert.getXYAddress(target,nRotX,16)+"\" with lines 1 \n";
									}
									else{
										str = new String(dirSaida.concat(File.separator + "reports" + File.separator + "dat" + File.separator + "distr_quant_pkts_r"+rot+"p"+port+"L"+lane+".dat"));
										aux = aux + "plot '"+str+"' using ($1):($2) t\"router "+Convert.getXYAddress(rot,nRotX,16)+" port "+Router.getPortName(port)+" channel L"+(lane+1)+" source "+Convert.getXYAddress(source,nRotX,16)+" target "+Convert.getXYAddress(target,nRotX,16)+"\" with lines 1 \n";
									}
									aux = aux + "pause -1 \"Press ENTER to continue\"\n";
								}
							}
						}
					}
				}
				else{
					for(int rot=source;rot>auxTarget;rot--){
						for(int port=0;port<4;port++){
							for(int lane=0;lane<numCV;lane++){
								if(isData[rot][port][lane]){

									aux = aux + "set xlabel 'Load (%)'\n";
									aux = aux + "set ylabel 'Number of packets'\n";

									if(numCV==1){
										str = new String(dirSaida.concat(File.separator + "reports" + File.separator + "dat" + File.separator + "distr_quant_pkts_r"+rot+"p"+port+".dat"));
										aux = aux + "plot '"+str+"' using ($1):($2) t\"router "+Convert.getXYAddress(rot,nRotX,16)+" port "+Router.getPortName(port)+" source "+Convert.getXYAddress(source,nRotX,16)+" target "+Convert.getXYAddress(target,nRotX,16)+"\" with lines 1 \n";
									}
									else{
										str = new String(dirSaida.concat(File.separator + "reports" + File.separator + "dat" + File.separator + "distr_quant_pkts_r"+rot+"p"+port+"L"+lane+".dat"));
										aux = aux + "plot '"+str+"' using ($1):($2) t\"router "+Convert.getXYAddress(rot,nRotX,16)+" port "+Router.getPortName(port)+" channel L"+(lane+1)+" source "+Convert.getXYAddress(source,nRotX,16)+" target "+Convert.getXYAddress(target,nRotX,16)+"\" with lines 1 \n";
									}
									aux = aux + "pause -1 \"Press ENTER to continue\"\n";
								}
							}
						}
					}
				}
				if(sourceY<targetY){
					//percorre os roteadores da linha do roteador source at� o target
					for(int rot=auxTarget;rot<=target;rot+=nRotX){
						for(int port=0;port<4;port++){
							for(int lane=0;lane<numCV;lane++){
								if(isData[rot][port][lane]){

									aux = aux + "set xlabel 'Load (%)'\n";
									aux = aux + "set ylabel 'Number of packets'\n";

									if(numCV==1){
										str = new String(dirSaida.concat(File.separator + "reports" + File.separator + "dat" + File.separator + "distr_quant_pkts_r"+rot+"p"+port+".dat"));
										aux = aux + "plot '"+str+"' using ($1):($2) t\"router "+Convert.getXYAddress(rot,nRotX,16)+" port "+Router.getPortName(port)+" source "+Convert.getXYAddress(source,nRotX,16)+" target "+Convert.getXYAddress(target,nRotX,16)+"\" with lines 1 \n";
									}
									else{
										str = new String(dirSaida.concat(File.separator + "reports" + File.separator + "dat" + File.separator + "distr_quant_pkts_r"+rot+"p"+port+"L"+lane+".dat"));
										aux = aux + "plot '"+str+"' using ($1):($2) t\"router "+Convert.getXYAddress(rot,nRotX,16)+" port "+Router.getPortName(port)+" channel L"+(lane+1)+" source "+Convert.getXYAddress(source,nRotX,16)+" target "+Convert.getXYAddress(target,nRotX,16)+"\" with lines 1 \n";
									}
									aux = aux + "pause -1 \"Press ENTER to continue\"\n";
								}
							}
						}
					}
				}
				else{
					for(int rot=auxTarget;rot>=target;rot-=nRotX){
						for(int port=0;port<4;port++){
							for(int lane=0;lane<numCV;lane++){
								if(isData[rot][port][lane]){

									aux = aux + "set xlabel 'Load (%)'\n";
									aux = aux + "set ylabel 'Number of packets'\n";

									if(numCV==1){
										str = new String(dirSaida.concat(File.separator + "reports" + File.separator + "dat" + File.separator + "distr_quant_pkts_r"+rot+"p"+port+".dat"));
										aux = aux + "plot '"+str+"' using ($1):($2) t\"router "+Convert.getXYAddress(rot,nRotX,16)+" port "+Router.getPortName(port)+" source "+Convert.getXYAddress(source,nRotX,16)+" target "+Convert.getXYAddress(target,nRotX,16)+"\" with lines 1 \n";
									}
									else{
										str = new String(dirSaida.concat(File.separator + "reports" + File.separator + "dat" + File.separator + "distr_quant_pkts_r"+rot+"p"+port+"L"+lane+".dat"));
										aux = aux + "plot '"+str+"' using ($1):($2) t\"router "+Convert.getXYAddress(rot,nRotX,16)+" port "+Router.getPortName(port)+" channel L"+(lane+1)+" source "+Convert.getXYAddress(source,nRotX,16)+" target "+Convert.getXYAddress(target,nRotX,16)+"\" with lines 1 \n";
									}
									aux = aux + "pause -1 \"Press ENTER to continue\"\n";
								}
							}
						}
					}
				}
				out.write(aux);
				out.close();
				osw.close();
				fos.close();

				String nameFile = dirSaida + File.separator + "reports" + File.separator + "graphs_txt" + File.separator + "distr_quant_pkts.txt";
				Default.showGraph(nameFile);
			}
			catch(Exception ex){
				JOptionPane.showMessageDialog(null,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
			}
		}
		else if(type.equalsIgnoreCase("HermesTU") || type.equalsIgnoreCase("HermesTB")){

			try{
				FileOutputStream fos = new FileOutputStream(dirSaida.concat(File.separator + "reports" + File.separator + "graphs_txt" + File.separator + "distr_quant_pkts.txt"));
				OutputStreamWriter osw = new OutputStreamWriter(fos);
				Writer out = new BufferedWriter(osw);

				aux = aux + "reset\n";
				aux = aux + "set dgrid3d 35,35,35\n";
				aux = aux + "set view 45,45,1,1\n";
				aux = aux + "set xrange [0:]\n";
				aux = aux + "set yrange [0:]\n";


				if ((targetX>sourceX)&&(targetY==sourceY)){

					nportas=targetX-sourceX;

					for (int j=0, rot=source ; j<nportas ; j++, rot++){
						for(int lane=0;lane<numCV;lane++){
							if(isData[rot][0][lane]){
							    aux = aux + getChannelGraph(source,target,numCV,rot,0,lane);
							}
						}
					}
				}

				else if ((targetX<sourceX)&&(targetY==sourceY)){

					for (int j=0, rot=source ; j <= ((nRotX-1) - sourceX) ; j++,rot++){

						for(int lane=0;lane<numCV;lane++){
							if(isData[rot][0][lane]){
							    aux = aux + getChannelGraph(source,target,numCV,rot,0,lane);
							}
						}

					}

					for ( int j=((int)(source/nRotX))*nRotX ;j<target;j++){
						for(int lane=0;lane<numCV;lane++){
							if(isData[j][0][lane]){
							    aux = aux + getChannelGraph(source,target,numCV,j,0,lane);
							}
						}
					}
				}

				else if ((targetX==sourceX)&&(targetY>sourceY)){

					nportas=targetY-sourceY;

					for (int j=0, rot=source; j<nportas ; j++, rot=rot+nRotX){
						for(int lane=0;lane<numCV;lane++){
							if(isData[rot][2][lane]){
							    aux = aux + getChannelGraph(source,target,numCV,rot,2,lane);
							}
						}
					}
				}

				else if ((targetX==sourceX)&&(targetY<sourceY)){

					for (int j=0, rot=source ; j <= ((nRotY-1) - sourceY) ; j++,rot=rot+nRotX){

						for(int lane=0;lane<numCV;lane++){
							if(isData[rot][2][lane]){
							    aux = aux + getChannelGraph(source,target,numCV,rot,2,lane);
							}
						}
					}

					for ( int j=(source%nRotX);j<target;j=j+nRotX){
						for(int lane=0;lane<numCV;lane++){
							if(isData[j][2][lane]){
							    aux = aux + getChannelGraph(source,target,numCV,j,2,lane);
							}
						}

					}


				}

				else if ((targetX>sourceX)&&(targetY>sourceY)){

					nportas=targetX-sourceX;

					for (int j=0, rot=source ; j<nportas ; j++, rot++,roteador=rot){
						for(int lane=0;lane<numCV;lane++){
							if(isData[rot][0][lane]){
							    aux = aux + getChannelGraph(source,target,numCV,rot,0,lane);
							}
						}

					}

					nportas=targetY-sourceY;

					for (int j=0; j<nportas ; j++, roteador=roteador+nRotX){
						for(int lane=0;lane<numCV;lane++){
							if(isData[roteador][2][lane]){
							    aux = aux + getChannelGraph(source,target,numCV,roteador,2,lane);
							}
						}
					}
				}

				else if ((targetX>sourceX)&&(targetY<sourceY)){

					nportas=targetX-sourceX;

					for (int j=0, rot=source ; j<nportas ; j++, rot++,roteador=rot){
						for(int lane=0;lane<numCV;lane++){
							if(isData[rot][0][lane]){
							    aux = aux + getChannelGraph(source,target,numCV,rot,0,lane);
							}
						}
					}

					for (int j=0, rot=roteador ; j <= ((nRotY-1) - sourceY) ; j++,rot=rot+nRotX,roteador=rot){
						for(int lane=0;lane<numCV;lane++){
							if(isData[rot][2][lane]){
							    aux = aux + getChannelGraph(source,target,numCV,rot,2,lane);
							}
						}
					}


					for ( int j=(roteador%nRotX);j<target;j=j+nRotX){
						for(int lane=0;lane<numCV;lane++){
							if(isData[j][2][lane]){
							    aux = aux + getChannelGraph(source,target,numCV,j,2,lane);
							}
						}
					}
				}

				else if ((targetX<sourceX)&&(targetY>sourceY)){

					for (int j=0, rot=source ; j <= ((nRotX-1) - sourceX) ; j++,rot++){
						for(int lane=0;lane<numCV;lane++){
							if(isData[rot][0][lane]){
							    aux = aux + getChannelGraph(source,target,numCV,rot,0,lane);
							}
						}
					}

					roteador=((int)(source/nRotX))*nRotX;

					for ( int i=0, j=((int)(source/nRotX))*nRotX ; i<targetX ; i++,j++, roteador=j){
						for(int lane=0;lane<numCV;lane++){
							if(isData[j][0][lane]){
							    aux = aux + getChannelGraph(source,target,numCV,j,0,lane);
							}
						}
					}

					nportas=targetY-sourceY;

					for (int j=0, rot=roteador; j<nportas ; j++, rot=rot+nRotX){
						for(int lane=0;lane<numCV;lane++){
							if(isData[rot][2][lane]){
							    aux = aux + getChannelGraph(source,target,numCV,rot,2,lane);
							}
						}
					}
				}
				else if ((targetX<sourceX)&&(targetY<sourceY)){

					for (int j=0, rot=source ; j <= ((nRotX-1) - sourceX) ; j++,rot++){
						for(int lane=0;lane<numCV;lane++){
							if(isData[rot][0][lane]){
							    aux = aux + getChannelGraph(source,target,numCV,rot,0,lane);
							}
						}
					}

					roteador=((int)(source/nRotX))*nRotX;

					for ( int j=((int)(source/nRotX))*nRotX, i=0 ; i<targetX ;j++, i++, roteador=j){
						for(int lane=0;lane<numCV;lane++){
							if(isData[j][0][lane]){
							    aux = aux + getChannelGraph(source,target,numCV,j,0,lane);
							}
						}
					}

					for (int j=0, rot=roteador ; j <= ((nRotY-1) - sourceY) ; j++,rot=rot+nRotX){
						for(int lane=0;lane<numCV;lane++){
							if(isData[rot][2][lane]){
							    aux = aux + getChannelGraph(source,target,numCV,rot,2,lane);
							}
						}
					}

					for ( int j=(roteador%nRotX);j<target;j=j+nRotX){
						for(int lane=0;lane<numCV;lane++){
							if(isData[j][2][lane]){
							    aux = aux + getChannelGraph(source,target,numCV,j,2,lane);
							}
						}
					}
				}

				out.write(aux);
				out.close();
				osw.close();
				fos.close();

				String nameFile = dirSaida + File.separator + "reports" + File.separator + "graphs_txt" + File.separator + "distr_quant_pkts.txt";
				Default.showGraph(nameFile);
			}
			catch(Exception ex){
				JOptionPane.showMessageDialog(null,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
			}

		}
	}

	/**
	 * Return a String corresponding a specific channel graph. 
	 * @param source The source router.
	 * @param target The target router
	 * @param numVC The number of virtual channels.
	 * @param rot The number of router used by packet
	 * @param port The number of the port used by packet.
	 * @param lane The number of the virtual channel used by packet.
	 * @return A String corresponding a specific channel graph. 
	 */
	private String getChannelGraph(int source, int target, int numVC, int rot, int port, int lane){
		String name;
		String aux = "set xlabel 'Load (%)'\nset ylabel 'Number of packets'\n";
	
		if(numVC==1){
		    name = dirSaida + File.separator + "reports" + File.separator + "dat" + File.separator + "distr_quant_pkts_r"+rot+"p"+port+".dat";
		    aux = aux + "plot '"+name+"' using ($1):($2) t\"router "+Convert.getXYAddress(rot,nRotX,16)+" port "+Router.getPortName(port)+" source "+Convert.getXYAddress(source,nRotX,16)+" target "+Convert.getXYAddress(target,nRotX,16)+"\" with lines 1 \n";
		}
		else{
		    name = dirSaida + File.separator + "reports" + File.separator + "dat" + File.separator + "distr_quant_pkts_r"+rot+"p"+port+"L"+lane+".dat";
		    aux = aux + "plot '"+name+"' using ($1):($2) t\"router "+Convert.getXYAddress(rot,nRotX,16)+" port "+Router.getPortName(port)+" channel L"+(lane+1)+" source "+Convert.getXYAddress(source,nRotX,16)+" target "+Convert.getXYAddress(target,nRotX,16)+"\" with lines 1 \n";
		}
		aux = aux + "pause -1 \"Press ENTER to continue\"\n";
		return aux;
    }
}
