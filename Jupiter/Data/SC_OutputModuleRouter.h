#ifndef OUTMODULEROUTER
#define OUTMODULEROUTER

#define constPhitSize $TPHIT$
#define constNumPort 4
#define constNumRot $NROT$
#define constNumRotX $WIDTH$
#define constNumRotY $HEIGHT$

#include "systemc.h"
#include <stdio.h>
#include <string.h>

SC_MODULE(outmodulerouter)
{
	sc_in<sc_logic> clock;
	sc_in<sc_logic> reset;

$SIGNALS$

	int inline inTx(int Roteador, int Porta)
	{
$INTX$
	}

	unsigned long int inline inData(int Roteador, int Porta)
	{
$INDATA$
	}

	unsigned long int inline inSize(int Roteador, int Porta)
	{
$INSIZE$
	}

	void inline outAck(int Roteador, int Porta, int Valor)
	{
$OUTACK$
	}

	unsigned long int CurrentTime;

	void inline TrafficWatcher();
	void inline Timer();

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
	FILE* Output[constNumRot][constNumPort];
	unsigned long int cont[constNumRot][constNumPort];
	unsigned long int size[constNumRot][constNumPort];
	unsigned long int currentFlit[constNumRot][constNumPort];
	int rot, port;

	for(rot=0;rot<constNumRot;rot++){
			sprintf(temp, "r%dp0.txt", rot);
			sprintf(temp, "r%dp1.txt", rot);
			sprintf(temp, "r%dp2.txt", rot);
			sprintf(temp, "r%dp3.txt", rot);

			Output[rot][0] = fopen(temp, "w");
 			Output[rot][1] = fopen(temp, "w");
			Output[rot][2] = fopen(temp, "w");
			Output[rot][3] = fopen(temp, "w");

			cont[rot][0] = 0;
			cont[rot][1] = 0;
			cont[rot][2] = 0;
			cont[rot][3] = 0;
	}


	while(true){
		for(rot=0;rot<constNumRot;rot++){

			//roteador não é o limite da direita, logo tem a porta EAST
			if((rot%constNumRotX)!=(constNumRotX-1)){
				//if(inTx(rot,0) == 1 && inCredit(rot,0)==1){
				if(inTx(rot,0) == 1 && inAck(rot,0)==1){
					currentFlit[rot][0] = inData(rot,0);
					fprintf(Output[rot][0], "(%0*X %u)", (int)constPhitSize/4, currentFlit[rot][0], CurrentTime);
					cont[rot][0]++;

					if(cont[rot][0] == 2)
						size[rot][0] = currentFlit[rot][0] + 2;

					if(cont[rot][0]>2 && cont[rot][0]==size[rot][0]){
						fprintf(Output[rot][0], "\n");
						cont[rot][0]=0;
						size[rot][0]=0;
					}
				}
			}
			//roteador não é o limite da esquerda, logo tem a porta WEST
			if((rot%constNumRotX)!=0){
				//if(inTx(rot,1) == 1 && inCredit(rot,1)==1){
				if(inTx(rot,1) == 1 && inAck(rot,1)==1){
					currentFlit[rot][1] = inData(rot,1);
					fprintf(Output[rot][1], "(%0*X %u)", (int)constPhitSize/4, currentFlit[rot][1], CurrentTime);
					cont[rot][1]++;

					if(cont[rot][1] == 2)
						size[rot][1] = currentFlit[rot][1] + 2;

					if(cont[rot][1]>2 && cont[rot][1]==size[rot][1]){
						fprintf(Output[rot][1], "\n");
						cont[rot][1]=0;
						size[rot][1]=0;
					}
				}
			}
			//roteador não é o limite superior, logo tem a porta NORTH
			if((rot/constNumRotX)!=constNumRotY-1){
				//if(inTx(rot,2) == 1 && inCredit(rot,2)==1){
				if(inTx(rot,2) == 1 && inAck(rot,2)==1){
					currentFlit[rot][2] = inData(rot,2);
					fprintf(Output[rot][2], "(%0*X %u)", (int)constPhitSize/4, currentFlit[rot][2], CurrentTime);
					cont[rot][2]++;

					if(cont[rot][2] == 2)
						size[rot][2] = currentFlit[rot][2] + 2;

					if(cont[rot][2]>2 && cont[rot][2]==size[rot][2]){
						fprintf(Output[rot][2], "\n");
						cont[rot][2]=0;
						size[rot][2]=0;
					}

				}
			}
			//roteador não é o limite inferior, logo tem a porta SOUTH
			if((rot/constNumRotX)!=0){
				//if(inTx(rot,3) == 1 && inCredit(rot,3)==1){
				if(inTx(rot,3) == 1 && inAck(rot,3)==1){
					currentFlit[rot][3] = inData(rot,3);
					fprintf(Output[rot][3], "(%0*X %u)", (int)constPhitSize/4, currentFlit[rot][3], CurrentTime);
					cont[rot][3]++;

					if(cont[rot][3] == 2)
						size[rot][3] = currentFlit[rot][3] + 2;

					if(cont[rot][3]>2 && cont[rot][3]==size[rot][3]){
						fprintf(Output[rot][3], "\n");
						cont[rot][3]=0;
						size[rot][3]=0;
					}

				}
			}
		}
		wait();
	}
}

#endif //OUTMODULEROUTER


