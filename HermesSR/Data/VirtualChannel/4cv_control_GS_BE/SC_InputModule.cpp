#include "SC_InputModule.h"

SC_MODULE_EXPORT(inputmodule);

void inline inputmodule::Timer(){
  ++CurrentTime; //variavel que conta no numero de clocks, eh resetada no reset.
}

void inline inputmodule::send_nothing(int _index, int _lane){
    outTx(_index, _lane, 0);
    outData(_index, 0);
}

void inline inputmodule::send_data(int _index, int _lane, unsigned long int _value){
	outTx(_index, _lane, 1);
	outData(_index, _value);
}

bool inline inputmodule::hasTraffic(int _index){
	return (tfr[_index][SRVC_CTRL]->existTraffic()|tfr[_index][SRVC_GS]->existTraffic()|tfr[_index][SRVC_BE]->existTraffic());
}

int inline inputmodule::getFirstToInject(int _index){

	unsigned long int itBE, itCTRL, itGS;
	int service=-1;
	
	itCTRL=(tfr[_index][SRVC_CTRL]->existTraffic())?tfr[_index][SRVC_CTRL]->getInjectionTime():0;
	itGS=(tfr[_index][SRVC_GS]->existTraffic())?tfr[_index][SRVC_GS]->getInjectionTime():0;
	itBE=(tfr[_index][SRVC_BE]->existTraffic())?tfr[_index][SRVC_BE]->getInjectionTime():0;

	     if((itCTRL!=0) && ((itGS  ==0)||(itCTRL<=itGS))   && ((itBE==0)||(itCTRL<=itBE))) service=SRVC_CTRL;
	else if((itGS  !=0) && ((itCTRL==0)||(itGS  < itCTRL)) && ((itBE==0)||(itGS  <=itBE))) service=SRVC_GS;
	else if((itBE  !=0) && ((itCTRL==0)||(itBE  < itCTRL)) && ((itGS==0)||(itBE  < itGS))) service=SRVC_BE;

	return service;
}
//xFBits:
//Returns a mask with the specified number of bits in '1'
//Param _nbits: specify the amount of lower significant bits to be set to 1
unsigned long int inline inputmodule::xFBits(int _nbits){
	unsigned long int value=0;
	for(int i=0;i<_nbits;i++)
		value=(value<<1)|0x01;
	return value;
}

void inline inputmodule::TrafficGenerator(){

/*******************************************************************************************************************************************
** pacote BE:
**
**  timestamp   target  size   source  timestamp de saida do nodo  nro de sequencia  timestamp de entrada na rede     payload
**      0        00XX   XXXX    00XX      XXXX XXXX XXXX XXXX         XXXX XXXX          XXXX XXXX XXXX XXXX            XXXX ...
**
********************************************************************************************************************************************/

  enum Estado{SWait, SHeader, SSize, SPayload, SExtra, FimArquivo};
  Estado EstadoAtual[constNumRot];
	int usingService[constNumRot], usingLane[constNumRot];
	
  char temp[constFlitSize+1], TimestampNet[constFlitSize/4+1];
  unsigned long int CurrentFlit,Target[constNumRot],Size[constNumRot];
  int FlitNumber[constNumRot];
  int Index,i,j,k,counter;
  unsigned long int timeStamp[constNumRot], HeaderSize[constNumRot];
	
	finish=SC_LOGIC_0;

	// inicializa os objetos que consumirão os arquivos de tráfego...
  for(Index=0;Index<constNumRot;Index++){

    tfr[Index][SRVC_CTRL] = new trafficFileReader(constFlitSize);
    sprintf(temp,"inCTRL%d.txt",Index);
		if(tfr[Index][SRVC_CTRL]->loadFile(temp)) tfr[Index][SRVC_CTRL]->loadPacket();

    tfr[Index][SRVC_GS] = new trafficFileReader(constFlitSize);
    sprintf(temp,"inGS%d.txt",Index);
		if(tfr[Index][SRVC_GS]->loadFile(temp)) tfr[Index][SRVC_GS]->loadPacket();

    tfr[Index][SRVC_BE] = new trafficFileReader(constFlitSize);
    sprintf(temp,"inBE%d.txt",Index);
		if(tfr[Index][SRVC_BE]->loadFile(temp)) tfr[Index][SRVC_BE]->loadPacket();		

    EstadoAtual[Index] = SWait;
		usingService[Index]=usingLane[Index]=-1;
		for(int _lane=0;_lane<constNumLane;_lane++) send_nothing(Index, _lane);

	}

	// Trata o consumo dos pacotes
  while(true){
		counter=0;
	  for(Index=0;Index<constNumRot;Index++){
			if(reset!=SC_LOGIC_1){

				if(!hasTraffic(Index)) EstadoAtual[Index]=FimArquivo;					

				//espera até o tempo para entrar na rede
				if(EstadoAtual[Index] == SWait){
					if(CurrentTime<tfr[Index][getFirstToInject(Index)]->getInjectionTime()) EstadoAtual[Index]=SWait;
					else{
						EstadoAtual[Index] = SHeader;
						usingService[Index]=getFirstToInject(Index); // Armazena o servico que sera tratado (SRVC_CTRL, SRVC_GS, SRVC_BE)

						//Captura o destino e o nro de flits necessarios para representar o caminho ateh o destino
						CurrentFlit=tfr[Index][usingService[Index]]->getHeader();
						Target[Index] = ((CurrentFlit >> (constFlitSize/4)) & xFBits(constFlitSize/4) )+((CurrentFlit & xFBits(constFlitSize/4))*constNumRotX);
						
						HeaderSize[Index]=myNI[usingService[Index]]->getHeaderSize(Index,(int) Target[Index]);
						if(HeaderSize[Index]==0){
							cout << "ERROR: Route between source " << Index << " and target " << Target[Index] << " was not defined in file " << myNI[usingService[Index]] << endl;
							cout << "\tSimulation will be stoped" << endl;
							sc_stop();
						}
						
						usingLane[Index]=myNI[usingService[Index]]->getLane(Index,(int) Target[Index]);
						
						switch(usingService[Index]){
							case SRVC_CTRL:
								if(usingLane[Index]!=0){
									cout << "ERROR: Wrong lane assigned for CTRL communication between source " << Index << " and target " << Target[Index] << " in file " << myNI[usingService[Index]] << endl;
									cout << "\tSimulation will be stoped." << endl;
									sc_stop();
								}
							break;
							case SRVC_GS:
								if(usingLane[Index]!=1 && usingLane[Index]!=2){
									cout << "ERROR: Wrong lane assigned for GS communication between source " << Index << " and target " << Target[Index] << " in file " << myNI[usingService[Index]] << endl;
									cout << "\tSimulation will be stoped." << endl;
									sc_stop();
								}
							break;
							case SRVC_BE:
								if(usingLane[Index]!=3){
									cout << "ERROR: Wrong lane assigned for BE communication between source " << Index << " and target " << Target[Index] << " in file " << myNI[usingService[Index]] << endl;
									cout << "\tSimulation will be stoped." << endl;
									sc_stop();
								}
							break;
						}


						//Captura o size
						// Onde,	4 é o nro adicional de flits contendo o timestamp de inicio de injecao do pacote na rede
						Size[Index]=tfr[Index][usingService[Index]]->getSize() + 4;

						// NumberOfFlits contem o número de flits que compoem o presente pacote
						//Onde,	HeaderSize é o nro de flits necessarios para representar o caminho no cabecalho do pacote,
						//			Size é o tamanho do payload do pacote e
						//			1 é o flit adicional necessario para representar o  flit que contem o size do pacote que não foi contabilizado
						FlitNumber[Index]=0;
					}
				}

				// Transmite o Header
				else if(EstadoAtual[Index]==SHeader && inCredit(Index,usingLane[Index])==1){

					//Se for o primeiro flit a ser injetado na rede, guarda o timestamp de injecao do pacote na rede
					// Esta funcao garantidamente sempre vai inserir 4 flits para representar o timestamp
					if(FlitNumber[Index] == 0){
						sprintf(temp, "%0*X",constFlitSize, CurrentTime);
						k=7; // = 7ª posicao do payload...
						for(i=0,j=0;i<constFlitSize;i++,j++){
							TimestampNet[j]=temp[i];							
							// Se o nro de valores hex possiveis de serem representados em um flit chegou ao limite
							//		(e.g. 8bits de flit permite 2 valores em hexa, jah 16 bits de flit permite 4 valores em hexa)
							if(j==constFlitSize/4-1){
								sscanf(TimestampNet, "%X", &CurrentFlit);
								CurrentFlit&=xFBits(constFlitSize);
								tfr[Index][usingService[Index]]->addFlit(k++,CurrentFlit);
								j=-1; //  porque na iteracao seguinte vai aumentar 1.
							}
						}
					}

					send_data(Index, usingLane[Index], myNI[usingService[Index]]->getHeaderFlit(Index,(int) Target[Index],FlitNumber[Index]));
					FlitNumber[Index]++;

					if(FlitNumber[Index]>=HeaderSize[Index]){
						FlitNumber[Index]=0;
						EstadoAtual[Index] = SSize;
					}
					else EstadoAtual[Index] = SHeader;

				}

				//Transmitir o size
				else if(EstadoAtual[Index]==SSize && inCredit(Index,usingLane[Index])==1){
					send_data(Index, usingLane[Index], Size[Index]);
					EstadoAtual[Index] = SPayload;
				}

				// Transmite o payload
				else if(EstadoAtual[Index]==SPayload && inCredit(Index,usingLane[Index])==1){
				
					if(FlitNumber[Index]>=Size[Index]){
						send_nothing(Index, usingLane[Index]);
						tfr[Index][usingService[Index]]->loadPacket();
						usingService[Index]=-1;
						FlitNumber[Index]=0;
						EstadoAtual[Index] = SWait;
					}
					else
						send_data(Index, usingLane[Index], tfr[Index][usingService[Index]]->getPayloadFlit(FlitNumber[Index]++));

				}

				else if(EstadoAtual[Index] == FimArquivo) send_nothing(Index, usingLane[Index]);

			}

			if(EstadoAtual[Index] == FimArquivo)counter++;

		}

		finish=(counter==constNumRot)? SC_LOGIC_1: SC_LOGIC_0;
		wait();
  }
}
