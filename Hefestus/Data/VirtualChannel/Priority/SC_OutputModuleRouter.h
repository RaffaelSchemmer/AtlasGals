#ifndef OUTMODULEROUTER
#define OUTMODULEROUTER

#define constFlitSize $TFLIT$
#define constNumPort 4
#define constNumRot $NROT$
#define constNumRotX $WIDTH$
#define constNumRotY $HEIGHT$
#define constNumVC $NLANE$

#include "systemc.h"
#include <stdio.h>
#include <string.h>

SC_MODULE(outmodulerouter)
{
	sc_in<sc_logic> clock;
	sc_in<sc_logic> reset;
$SIGNALS$

	int inline inTx(int Roteador, int Porta){
$INTX$
	}

	int inline inLaneTx(int Roteador,int Porta, int Canal){
$INLANETX$
	}

	unsigned long inline inData(int Roteador, int Porta){
$INDATA$
	}

	int inline inCredit(int Roteador, int Porta, int Canal){
$INCREDIT$
	}

	unsigned long int CurrentTime;

	void inline TrafficWatcher();
	void inline Timer();
	void inline port_assign();

	SC_CTOR(outmodulerouter) :
$VARIABLES$
	reset("reset"),
	clock("clock")
	{
		CurrentTime = 0;

		SC_CTHREAD(TrafficWatcher, clock.pos());
		//watching(reset.delayed()== true);

		SC_METHOD(Timer);
		sensitive_pos << clock;
		dont_initialize();
	}
};

void inline outmodulerouter::Timer(){
	++CurrentTime;
}

void inline outmodulerouter::TrafficWatcher(){
	char temp[100];
	FILE* Output[constNumRot][constNumPort][constNumVC];
	unsigned long int cont[constNumRot][constNumPort][constNumVC];
	unsigned long int size[constNumRot][constNumPort][constNumVC];
	unsigned long int currentFlit[constNumRot][constNumPort][constNumVC];
	int rot,port,lane;

	for(rot=0;rot<constNumRot;rot++){
		//roteador não é o limite da direita, logo tem a porta EAST
 		if((rot%constNumRotX)!=(constNumRotX-1)){
			for(lane=0;lane<constNumVC;lane++){
				sprintf(temp, "r%dp0l%d.txt", rot,lane);
				Output[rot][0][lane] = fopen(temp, "w");
				cont[rot][0][lane] = 0;
			}
		}
		//roteador não é o limite da esquerda, logo tem a porta WEST
 		if((rot%constNumRotX)!=0){
			for(lane=0;lane<constNumVC;lane++){
				sprintf(temp, "r%dp1l%d.txt", rot,lane);
				Output[rot][1][lane] = fopen(temp, "w");
				cont[rot][1][lane] = 0;
			}
		}
		//roteador não é o limite superior, logo tem a porta NORTH
 		if((rot/constNumRotX)!=(constNumRotY-1)){
			for(lane=0;lane<constNumVC;lane++){
				sprintf(temp, "r%dp2l%d.txt", rot,lane);
				Output[rot][2][lane] = fopen(temp, "w");
				cont[rot][2][lane] = 0;
			}
		}
		//roteador não é o limite inferior, logo tem a porta SOUTH
 		if((rot/constNumRotX)!=0){
			for(lane=0;lane<constNumVC;lane++){
				sprintf(temp, "r%dp3l%d.txt", rot,lane);
				Output[rot][3][lane] = fopen(temp, "w");
				cont[rot][3][lane] = 0;
			}
		}
	}

	while(true){
		for(rot=0;rot<constNumRot;rot++){

			//roteador não é o limite da direita, logo tem a porta EAST
			if((rot%constNumRotX)!=(constNumRotX-1)){
				for(lane=0;lane<constNumVC;lane++){
					if(inTx(rot,0) == 1 && inLaneTx(rot,0,lane) == 1 && inCredit(rot,0,lane)==1){
						currentFlit[rot][0][lane] = inData(rot,0);
						fprintf(Output[rot][0][lane], "(%0*X %u)", (int)constFlitSize/4, currentFlit[rot][0][lane], CurrentTime);
						cont[rot][0][lane]++;

						if(cont[rot][0][lane] == 2)
							size[rot][0][lane] = currentFlit[rot][0][lane] + 2;

						if(cont[rot][0][lane]>2 && cont[rot][0][lane]==size[rot][0][lane]){
							fprintf(Output[rot][0][lane], "\n");
							cont[rot][0][lane]=0;
							size[rot][0][lane]=0;
						}
					}
				}
			}
			//roteador não é o limite da esquerda, logo tem a porta WEST
			if((rot%constNumRotX)!=0){
				for(lane=0;lane<constNumVC;lane++){
					if(inTx(rot,1) == 1 && inLaneTx(rot,1,lane) == 1 && inCredit(rot,1,lane)==1){
						currentFlit[rot][1][lane] = inData(rot,1);
						fprintf(Output[rot][1][lane], "(%0*X %u)", (int)constFlitSize/4, currentFlit[rot][1][lane], CurrentTime);
						cont[rot][1][lane]++;

						if(cont[rot][1][lane] == 2)
							size[rot][1][lane] = currentFlit[rot][1][lane] + 2;

						if(cont[rot][1][lane]>2 && cont[rot][1][lane]==size[rot][1][lane]){
							fprintf(Output[rot][1][lane], "\n");
							cont[rot][1][lane]=0;
							size[rot][1][lane]=0;
						}
					}
				}
			}
			//roteador não é o limite superior, logo tem a porta NORTH
			if((rot/constNumRotX)!=constNumRotY-1){
				for(lane=0;lane<constNumVC;lane++){
					if(inTx(rot,2) == 1 && inLaneTx(rot,2,lane) == 1 && inCredit(rot,2,lane)==1){
						currentFlit[rot][2][lane] = inData(rot,2);
						fprintf(Output[rot][2][lane], "(%0*X %u)", (int)constFlitSize/4, currentFlit[rot][2][lane], CurrentTime);
						cont[rot][2][lane]++;

						if(cont[rot][2][lane] == 2)
							size[rot][2][lane] = currentFlit[rot][2][lane] + 2;

						if(cont[rot][2][lane]>2 && cont[rot][2][lane]==size[rot][2][lane]){
							fprintf(Output[rot][2][lane], "\n");
							cont[rot][2][lane]=0;
							size[rot][2][lane]=0;
						}
					}
				}
			}
			//roteador não é o limite inferior, logo tem a porta SOUTH
			if((rot/constNumRotX)!=0){
				for(lane=0;lane<constNumVC;lane++){
					if(inTx(rot,3) == 1 && inLaneTx(rot,3,lane) == 1 && inCredit(rot,3,lane)==1){
						currentFlit[rot][3][lane] = inData(rot,3);
						fprintf(Output[rot][3][lane], "(%0*X %u)", (int)constFlitSize/4, currentFlit[rot][3][lane], CurrentTime);
						cont[rot][3][lane]++;

						if(cont[rot][3][lane] == 2)
							size[rot][3][lane] = currentFlit[rot][3][lane] + 2;

						if(cont[rot][3][lane]>2 && cont[rot][3][lane]==size[rot][3][lane]){
							fprintf(Output[rot][3][lane], "\n");
							cont[rot][3][lane]=0;
							size[rot][3][lane]=0;
						}
					}
				}
			}
		}
		wait();
	}
}

#endif //OUTMODULEROUTER
