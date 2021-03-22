#ifndef OUTMODULE
#define OUTMODULE

#define constFlitSize $TFLIT$
#define constNumRot $NROT$

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

	FILE* Output[constNumRot];

	unsigned long int CurrentFlit[constNumRot];
	int EstadoAtual[constNumRot],Size[constNumRot];
	int i, j, Index;
	char temp[100];

	char TimeTargetHex[constNumRot][100];
	unsigned long int TimeTarget[constNumRot];
	unsigned long int TimeSourceCore[constNumRot];
	unsigned long int TimeSourceNet[constNumRot];

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
		sprintf(temp,"out%d.txt",i);
		Output[i] = fopen(temp,"w");
		Size[i] = 0;
		EstadoAtual[i] = 1;
	}

	while(true){
		for(Index = 0; Index<constNumRot;Index++){

			if(inTx(Index)==1){
				if(EstadoAtual[Index] == 1){
					//captura o header do pacote
					CurrentFlit[Index] = (unsigned long int)inData(Index);
					fprintf(Output[Index],"%0*X",(int)constFlitSize/4,CurrentFlit[Index]);

					EstadoAtual[Index]++;
				}
				else if(EstadoAtual[Index] == 2){
					//captura o tamanho do payload
					CurrentFlit[Index] = (unsigned long int)inData(Index);
					fprintf(Output[Index]," %0*X",(int)constFlitSize/4,CurrentFlit[Index]);

					Size[Index] = CurrentFlit[Index];
					EstadoAtual[Index]++;
				}
				else if(EstadoAtual[Index] == 3){
					//captura o nodo origem
					CurrentFlit[Index] = (unsigned long int)inData(Index);
					fprintf(Output[Index]," %0*X",(int)constFlitSize/4,CurrentFlit[Index]);

					Size[Index]--;
					EstadoAtual[Index]++;
				}
				else if(EstadoAtual[Index]>=4 && EstadoAtual[Index]<=7){
					//captura o timestamp do nodo origem
					CurrentFlit[Index] = (unsigned long int)inData(Index);
					fprintf(Output[Index]," %0*X",(int)constFlitSize/4,CurrentFlit[Index]);

					if(EstadoAtual[Index]==4) TimeSourceCore[Index]=0;

					TimeSourceCore[Index] += (unsigned long int)(CurrentFlit[Index] * pow(2,((7 - EstadoAtual[Index])*constFlitSize)));

					Size[Index]--;
					EstadoAtual[Index]++;
				}
				else if(EstadoAtual[Index] == 8 || EstadoAtual[Index] == 9){
					//captura o número de sequencia do pacote
					CurrentFlit[Index] = (unsigned long int)inData(Index);
					fprintf(Output[Index]," %0*X",(int)constFlitSize/4,CurrentFlit[Index]);

					Size[Index]--;
					EstadoAtual[Index]++;
				}
				else if(EstadoAtual[Index]>=10 && EstadoAtual[Index]<=13){
					//captura o timestamp do entrada na rede
					CurrentFlit[Index] = (unsigned long int)inData(Index);
					fprintf(Output[Index]," %0*X",(int)constFlitSize/4,CurrentFlit[Index]);

					if(EstadoAtual[Index]==10) TimeSourceNet[Index]=0;

					TimeSourceNet[Index] += (unsigned long int)(CurrentFlit[Index] * pow(2,((13 - EstadoAtual[Index])*constFlitSize)));

					Size[Index]--;
					EstadoAtual[Index]++;
				}
				else if(EstadoAtual[Index]==14){
					//captura o payload
					CurrentFlit[Index] = (unsigned long int)inData(Index);
					fprintf(Output[Index]," %0*X",(int)constFlitSize/4,CurrentFlit[Index]);

					Size[Index]--;

					//fim do pacote
					if(Size[Index]==0){

						//Tempo de chegada no destino
						TimeTarget[Index]= CurrentTime;
						sprintf(TimeTargetHex[Index], "%0*X",constFlitSize,TimeTarget[Index]);
						for(i=0,j=0;i<constFlitSize;i++,j++){
							temp[j]=TimeTargetHex[Index][i];
							if(j==constFlitSize/4-1)
							{
								temp[constFlitSize/4]='\0';
								fprintf(Output[Index]," %s",temp);
								j=-1; //  porque na iteracao seguinte j será 0.
							}
						}

						//Tempo em que o nodo origem deveria inserir o pacote na rede (em decimal)
						fprintf(Output[Index]," %d",TimeSourceCore[Index]);

						//Tempo em que o pacote entrou na rede (em decimal)
						fprintf(Output[Index]," %d",TimeSourceNet[Index]);

						//Tempo de chegada do pacote no destino (em decimal)
						fprintf(Output[Index]," %d",TimeTarget[Index]);

						//latência desde o tempo de criação do pacote (em decimal)
						fprintf(Output[Index]," %d",(TimeTarget[Index]-TimeSourceCore[Index]));

					//-----------------TIME--------------------------------//
						//captura o tempo de simulacao em milisegundos
						ftime(&tp);

						//armazena o tempo final
						segundos_final=tp.time;
						milisegundos_final=tp.millitm;

						TimeFinal=(segundos_final*1000 + milisegundos_final) - (segundos_inicial*1000+milisegundos_inicial);
					//-----------------------------------------------------//

						fprintf(Output[Index]," %ld\n",TimeFinal);
						EstadoAtual[Index] = 1;
					}
				}
			}
		}
		wait();
	}
}

#endif
