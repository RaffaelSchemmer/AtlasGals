#ifndef OUTMODULE
#define OUTMODULE

#define constFlitSize $TFLIT$
#define constNumRot $NROT$
#define constNumLane $NLANE$

#include "systemc.h"
#include <stdio.h>
#include <string.h>
#include <sys/timeb.h>


SC_MODULE(outmodule)
{
	sc_in<sc_logic> clock;
	sc_in<sc_logic> reset;
$SIGNALS$

	int inline inTx(int Indice){
$INTX$
	}

	int inline inLaneTx(int Indice, int Lane){
$INLANETX$
	}

	unsigned long int inline inData(int Indice){
$INDATA$
	}

	unsigned long int CurrentTime;

	void inline TrafficStalker();
	void inline Timer();
	void inline port_assign();

	SC_CTOR(outmodule) :

$VARIABLES$
	reset("reset"),
	clock("clock")
	{
		CurrentTime = 0;

		SC_CTHREAD(TrafficStalker, clock.neg());
		//watching(reset.delayed()== true);

		SC_METHOD(Timer);
		sensitive_pos << clock;
		dont_initialize();

		SC_METHOD(port_assign);
		sensitive << clock;
		dont_initialize();
	}
};

void inline outmodule::Timer(){
	++CurrentTime;
}

void inline outmodule::port_assign(){
$OUTMODULE$
}

void inline outmodule::TrafficStalker(){

/*******************************************************************************************************************************************************************************************
** pacote BE:
**
**  target  size   source  timestamp de saida do nodo  nro de sequencia  timestamp de entrada na rede     payload
**   00XX   XXXX    00XX      XXXX XXXX XXXX XXXX         XXXX XXXX          XXXX XXXX XXXX XXXX            XXXX ...
**    S1     S2      S3             S4 a S7                S8 e S9               S10 a S13             S14 até size = 0
**
**
**     escrito      => timestamp de saída na rede   timestamp de saida do nodo   timestamp de entrada na rede    timestamp de saída da rede    latência    tempo de simulação
** no fim do pacote        XXXX XXXX XXXX XXXX     	       em decimal    	              em decimal			          em decimal		  em decimal     em milisegundos
**
********************************************************************************************************************************************************************************************/

	FILE* Output[constNumRot][constNumLane];

	unsigned long int CurrentFlit[constNumRot][constNumLane];
	int EstadoAtual[constNumRot][constNumLane];
	int Size[constNumRot][constNumLane];
	int i, j, Index, lane;
	char temp[100];

	char TimeTargetHex[constNumRot][constNumLane][100];
	unsigned long int TimeTarget[constNumRot][constNumLane];
	unsigned long int TimeSourceCore[constNumRot][constNumLane];
	unsigned long int TimeSourceNet[constNumRot][constNumLane];

	struct timeb tp;
	int segundos_inicial, milisegundos_inicial;
	int segundos_final, milisegundos_final;
	unsigned long int TimeFinal;

//-----------------TIME--------------------------------//
	//captura o tempo
	ftime(&tp);
	//armazena o tempo inicial
	segundos_inicial=tp.time;
	milisegundos_inicial=tp.millitm;
//-----------------------------------------------------//

	for(i=0; i<constNumRot; i++){
		for(lane = 0; lane < constNumLane; lane++){
			sprintf(temp,"out%dL%d.txt",i,lane);
			Output[i][lane] = fopen(temp,"w");
			Size[i][lane] = 0;
			EstadoAtual[i][lane] = 1;
		}
	}

	while(true){
		for(Index = 0; Index<constNumRot;Index++){
			if(inTx(Index)==1){
				if(inLaneTx(Index,1) == 1)
					lane = 1;
				else
					lane = 0;

				if(EstadoAtual[Index][lane] == 1){
					//captura o header do pacote
					CurrentFlit[Index][lane] = (unsigned long int)inData(Index);
					fprintf(Output[Index][lane],"%0*X",(int)constFlitSize/4,CurrentFlit[Index][lane]);

					EstadoAtual[Index][lane]++;
				}
				else if(EstadoAtual[Index][lane] == 2){
					//captura o tamanho do payload
					CurrentFlit[Index][lane] = (unsigned long int)inData(Index);
					fprintf(Output[Index][lane]," %0*X",(int)constFlitSize/4,CurrentFlit[Index][lane]);

					Size[Index][lane] = CurrentFlit[Index][lane];
					EstadoAtual[Index][lane]++;
				}
				else if(EstadoAtual[Index][lane] == 3){
					//captura o nodo origem
					CurrentFlit[Index][lane] = (unsigned long int)inData(Index);
					fprintf(Output[Index][lane]," %0*X",(int)constFlitSize/4,CurrentFlit[Index][lane]);

					Size[Index][lane]--;
					EstadoAtual[Index][lane]++;
				}
				else if(EstadoAtual[Index][lane]>=4 && EstadoAtual[Index][lane]<=7){
					//captura o timestamp do nodo origem
					CurrentFlit[Index][lane] = (unsigned long int)inData(Index);
					fprintf(Output[Index][lane]," %0*X",(int)constFlitSize/4,CurrentFlit[Index][lane]);

					if(EstadoAtual[Index][lane]==4) TimeSourceCore[Index][lane]=0;

					TimeSourceCore[Index][lane] += (unsigned long int)(CurrentFlit[Index][lane] * pow(2,((7 - EstadoAtual[Index][lane])*constFlitSize)));

					Size[Index][lane]--;
					EstadoAtual[Index][lane]++;
				}
				else if(EstadoAtual[Index][lane] == 8 || EstadoAtual[Index][lane] == 9){
					//captura o número de sequencia do pacote
					CurrentFlit[Index][lane] = (unsigned long int)inData(Index);
					fprintf(Output[Index][lane]," %0*X",(int)constFlitSize/4,CurrentFlit[Index][lane]);

					Size[Index][lane]--;
					EstadoAtual[Index][lane]++;
				}
				else if(EstadoAtual[Index][lane]>=10 && EstadoAtual[Index][lane]<=13){
					//captura o timestamp do entrada na rede
					CurrentFlit[Index][lane] = (unsigned long int)inData(Index);
					fprintf(Output[Index][lane]," %0*X",(int)constFlitSize/4,CurrentFlit[Index][lane]);

					if(EstadoAtual[Index][lane]==10) TimeSourceNet[Index][lane]=0;

					TimeSourceNet[Index][lane] += (unsigned long int)(CurrentFlit[Index][lane] * pow(2,((13 - EstadoAtual[Index][lane])*constFlitSize)));

					Size[Index][lane]--;
					EstadoAtual[Index][lane]++;
				}
				else if(EstadoAtual[Index][lane]==14){
					//captura o payload
					CurrentFlit[Index][lane] = (unsigned long int)inData(Index);
					fprintf(Output[Index][lane]," %0*X",(int)constFlitSize/4,CurrentFlit[Index][lane]);

					Size[Index][lane]--;

					//fim do pacote
					if(Size[Index][lane]==0){
						//Tempo de chegada no destino
						TimeTarget[Index][lane]= CurrentTime;
						sprintf(TimeTargetHex[Index][lane], "%0*X",constFlitSize,TimeTarget[Index][lane]);
						for(i=0,j=0;i<constFlitSize;i++,j++){
							temp[j]=TimeTargetHex[Index][lane][i];
							if(j==constFlitSize/4-1){
								temp[constFlitSize/4]='\0';
								fprintf(Output[Index][lane]," %s",temp);
								j=-1; //  porque na iteracao seguinte j será 0.
							}
						}

						//Tempo em que o nodo origem deveria inserir o pacote na rede (em decimal)
						fprintf(Output[Index][lane]," %d",TimeSourceCore[Index][lane]);

						//Tempo em que o pacote entrou na rede (em decimal)
						fprintf(Output[Index][lane]," %d",TimeSourceNet[Index][lane]);

						//Tempo de chegada do pacote no destino (em decimal)
						fprintf(Output[Index][lane]," %d",TimeTarget[Index][lane]);

						//latência desde o tempo de criação do pacote (em decimal)
						fprintf(Output[Index][lane]," %d",(TimeTarget[Index][lane]-TimeSourceCore[Index][lane]));

					//-----------------TIME--------------------------------//
						//captura o tempo de simulacao em milisegundos
						ftime(&tp);

						//armazena o tempo final
						segundos_final=tp.time;
						milisegundos_final=tp.millitm;

						TimeFinal=(segundos_final*1000 + milisegundos_final) - (segundos_inicial*1000+milisegundos_inicial);
					//-----------------------------------------------------//

						fprintf(Output[Index][lane]," %ld\n",TimeFinal);
						EstadoAtual[Index][lane] = 1;
					}
				}
			}
		}
		wait();
	}
}

#endif
