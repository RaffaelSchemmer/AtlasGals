#ifndef INMODULE
#define INMODULE

#define constPhitSize $TPHIT$
#define constNumRot $NROT$
#define constNumRotX $WIDTH$
#define constNumRotY $HEIGHT$
#define constNumberOfChars 4

#include <stdio.h>
#include <string.h>
#include <systemc.h>

SC_MODULE(inputmodule)
{
	private:
	void inline outTx(int Indice, int Booleano)
	{
$OUTTX$
	}

	void inline outData(int Indice, unsigned long int Valor)
	{
$OUTDATA$
	}

	void inline outSize(int Indice, unsigned long int Valor)
	{
$OUTSIZE$
	}

	int inline inAck(int Indice)
	{
$INACK$
	}

	unsigned long int CurrentTime;
	int executou;
	int Index;

	public:
	sc_in<sc_logic> clock;
	sc_in<sc_logic> reset;

$SIGNALS$

	void inline Timer();
	void inline TrafficGenerator();
	void inline port_assign();

	SC_CTOR(inputmodule):

$VARIABLES$

	reset("reset"),
 	clock("clock")
	{
		CurrentTime = 0;
		SC_CTHREAD(TrafficGenerator, clock.pos());
		//watching(reset.delayed() == true);

		SC_METHOD(Timer);
		sensitive_pos << clock;
		dont_initialize();

		SC_METHOD(port_assign);
		sensitive << clock;
	}
};


void inline inputmodule::port_assign()
{
}

void inline inputmodule::Timer()
{

	++CurrentTime;
}


void inline inputmodule::TrafficGenerator()
{
	wait(); //??
	CurrentTime = 0;
	enum Estado{S1, S2, S3, S4, S5};
	FILE* Input[constNumRot];
	char temp[50];
	char Destino[constNumberOfChars];
	unsigned long int CurrentFlit[constNumRot], Desnecessaria[constNumRot][4], TSSaida[constNumRot], Temp;
	unsigned long int* BigPacket[constNumRot];
	bool Mandando[constNumRot], Ajeitando[constNumRot];
	Estado EstadoAtual[constNumRot];
	int FlitNumber[constNumRot], NumberofFlits[constNumRot], WaitTime[constNumRot];
	int i,j,k;

	for(i=0;i<constNumRot;i++)
	{
		outTx(i,0);
	}//Inicializar os Tx em 0...

	for(Index=0;Index<constNumRot;Index++)
	{
		sprintf(temp,"in%d.txt",Index);
		Input[Index] = fopen(temp,"r");
		EstadoAtual[Index] = S1;
	}

	while(true)
	{
		for(Index=0;Index<constNumRot;Index++)
		{
			if(Input[Index] != NULL && !feof(Input[Index]))
			{
				if(EstadoAtual[Index] == S1)
				{
						outTx(Index,0);

						FlitNumber[Index] = 0;
						fscanf(Input[Index],"%X",&CurrentFlit[Index]);
						WaitTime[Index] = CurrentFlit[Index];
						EstadoAtual[Index]=S2;
				}
				if(EstadoAtual[Index] == S2)
				{
					if(CurrentTime<WaitTime[Index]) // eh na verdade o Wait Time do Pacote Atual, do roteador escolhido.
					{
						outTx(Index, 0);//0;
						EstadoAtual[Index]=S2;
					}
					else
					{
						EstadoAtual[Index] = S3;
					}
				}
				if(EstadoAtual[Index]== S3)
				{
					//Executou(Index, 269);
					while(FlitNumber[Index] < 2)
					{
						fscanf(Input[Index],"%X",&CurrentFlit[Index]);
						Desnecessaria[Index][FlitNumber[Index] ] = CurrentFlit[Index] ;
						if(FlitNumber[Index] == 1)
						{                    // numero de flits que vai ter o TS de Entrada na Rede.
							Desnecessaria[Index][1]+= constNumberOfChars + 2;
							NumberofFlits[Index] = Desnecessaria[Index][1];///////////////////////////precisa de +2 para o destino e tamanhoq ue são flits que nao contam,
							//outSize(Index, Desnecessaria[Index][1]); //Legal, a desnecessaria ficou necessaria...
							BigPacket[Index]=(unsigned long int*)calloc( sizeof(unsigned long int) , (Desnecessaria[Index][1])+2);
							BigPacket[Index][0] = Desnecessaria[Index][0];
							BigPacket[Index][1] = Desnecessaria[Index][1];
						}
						//DEBUG
						FlitNumber[Index]++;

					}
					//FlitNumber[Index]--; // Eh necessario pq o BigPacket vai continuar no 2 e naum no 3, como seria sem o --;
					//Executou(Index, 284);
					////////////////////////////// eh o numero de Flits que tem o TS de Criacao (FlitSize/4)
					while(FlitNumber[Index] < (5 + constNumberOfChars) )
					{
						fscanf(Input[Index], "%X", &CurrentFlit[Index]);
						BigPacket[Index][FlitNumber[Index] ] = CurrentFlit[Index];

						FlitNumber[Index]++;
					}

					FlitNumber[Index]+=constNumberOfChars; //eh o espaco que depois vai ter o TS de entrada na rede =)


					while(FlitNumber[Index] < NumberofFlits[Index]) // +1 (antes tinha +1, por que?)
					{
						fscanf(Input[Index], "%X", &CurrentFlit[Index]);
						BigPacket[Index][FlitNumber[Index] ] = CurrentFlit[Index];

						FlitNumber[Index]++;
					}

					EstadoAtual[Index] = S4;

					FlitNumber[Index]=0;

				}
				if(EstadoAtual[Index]==S4)
				{
					if(!feof(Input[Index]))	outTx(Index, 1);
					outData(Index, Desnecessaria[Index][0]); //destino
					outSize(Index, Desnecessaria[Index][1]); //tamanho
					if(inAck(Index)==0) //Aceitou
					{
						EstadoAtual[Index]=S5;
						TSSaida[Index] = CurrentTime-1;//pórque o primeiro PHiT efetivo, vem 1 clock antes.
						Temp = 5 + (int)constPhitSize/4; //Lugar do TS de Entrada na Rede
						sprintf(temp, "%0*X",constPhitSize, TSSaida[Index]); // Obs que nao atrapalha mas tambem nao ajuda: o maximo do unsigned long int é 32 bits, ou seja FFFF FFFF.

						for(i=0,j=0,k=0;i<constPhitSize;i++,j++)
						{
							Destino[j]=temp[i];
							if(j==constNumberOfChars-1)
							{
								sscanf(Destino, "%X", &BigPacket[Index][Temp ]); // (5 + constNumberOfChars) + k]);
								j=-1; //  porque na iteracao seguinte vai aumentar 1.
								k++;
								Temp++;
							}
						}
					}
					else //Nao aceitou
					{
						EstadoAtual[Index]=S4;
					}
				}
				if(EstadoAtual[Index]==S5)
				{
					FlitNumber[Index]++;
					if(FlitNumber[Index]>NumberofFlits[Index])
					{
						outTx(Index, 0);

						EstadoAtual[Index] = S1;
						free(BigPacket[Index]);
					}
					else if(FlitNumber[Index]==NumberofFlits[Index])
					{
						outTx(Index, 1); //continua em 1, mesmo dps de acabar, por 1 clock, pra NOC nao se perder.
					}
					else
					{
						outTx(Index, 1);
						outData(Index, BigPacket[Index][FlitNumber[Index] ]); //se notar bem, o primero fli(destino) nunca eh mandado por aqui.
						outSize(Index, Desnecessaria[Index][1] );
						EstadoAtual[Index] = S5;
					}
				}
			}
			else
			{
				outTx(Index, 0);
			}
		}
	wait();
	}
}

#endif// INMODULE





