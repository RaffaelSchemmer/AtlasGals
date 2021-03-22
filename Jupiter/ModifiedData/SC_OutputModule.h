#ifndef OUTMODULE
#define OUTMODULE

#define constPhitSize $TPHIT$
#define constNumRot $NROT$
#define constNumRotX $WIDTH$
#define constNumRotY $HEIGHT$
#define constNumPort 4

#include "systemc.h"
#include <stdio.h>
#include <string.h>
#include <sys/timeb.h>

SC_MODULE(outmodule)
{
	sc_in<sc_logic> clock;
	sc_in<sc_logic> reset;
	unsigned long int CurrentTime;

/* FUNCOES */

	int inline inTx(int Indice)
	{
$INTX$
	}

	unsigned long int inline inData(int Indice)
	{
$INDATA$
	}

	unsigned long int inline inSize(int Indice)
	{
$INSIZE$
	}

	void inline outAck(int Indice, int Valor)
	{
$OUTACK$
	}

$SIGNALS$

	void inline TrafficStalker();
	void inline Timer();
	void inline port_assign();

	SC_CTOR(outmodule) :

$VARIABLES$

	reset("reset"),
	clock("clock")
	{
		CurrentTime = 0;

		SC_CTHREAD(TrafficStalker, clock.pos());
		//watching(reset.delayed()== true);

		SC_METHOD(Timer);
		sensitive_pos << clock;
		dont_initialize();

		SC_METHOD(port_assign);
		sensitive << clock;
		dont_initialize();

	}
};

void inline outmodule::Timer()
{
	++CurrentTime;
}

void inline outmodule::port_assign()
{
}

void inline outmodule::TrafficStalker() // Stalker é tipo.. perseguidor.. Enfimm eu nao pensei em nada melhor :)
{
//-----------------TIME--------------------------------//
	//declaracoes
	int segundos_inicial, milisegundos_inicial;
	int segundos_final, milisegundos_final;
	struct timeb tp;

	//captura o tempo
	ftime(&tp);

	//armazena o tempo inicial
	segundos_inicial=tp.time;
	milisegundos_inicial=tp.millitm;

//-----------------------------------------------------//
	//sc_set_default_time_unit(1,SC_NS);
	FILE* Output[constNumRot];

	CurrentTime = 0;
	char temp[50], temp2[50], Destino[(int)constPhitSize/4];
	unsigned long int* Packet[constNumRot];
	unsigned long int CurrentFlit[constNumRot];
	unsigned long int PacketSize[constNumRot], J;
	unsigned long int Temp[constNumRot];
	unsigned long int Desnecessaria[constNumRot];
	unsigned long int TSCriacao[constNumRot], TSSaida[constNumRot];
	bool Espera[constNumRot];



//Criando arquivos r*p*.txt pra funcionar o medidor

// Analise de tráfego interno!!!
/*
	FILE* fileTemp;
	int RIndex, PIndex;
	for(RIndex=0;RIndex<constNumRot;RIndex++)
	{
		for(PIndex=0;PIndex<constNumPort;PIndex++)
		{
			if( (RIndex%constNumRotX==0 && PIndex == 1) || (RIndex%constNumRotX== constNumRotX -1 && PIndex == 0) ) continue;
			if( ((int)(RIndex-(RIndex%constNumRotY))/constNumRotY==0 && PIndex == 3) || ((int)(RIndex-(RIndex%constNumRotY))/constNumRotY== constNumRotY -1 && PIndex == 2) ) continue;
			sprintf(temp, "r%dp%d.txt", RIndex, PIndex);
			fileTemp = fopen(temp, "w");
			fclose(fileTemp);
		}
	}
*/

	int Useless[constNumRot],i,j,k, Marca, Index; // TSCriacao[constNumRot], TSSaida[constNumRot], TSChegada[constNumRot], Index;

	for(i=0; i<constNumRot; i++)
	{
		sprintf(temp,"");
		sprintf(temp2,"out%d.txt",i);
		strcat(temp, temp2);

		Output[i] = fopen(temp,"w");
		Useless[i] = 0;
		PacketSize[i] = 0;
	}

	while(true)
	{
		for(Index = 0; Index<constNumRot;Index++)
		{
			if(inTx(Index)==1)//inTx(Index)==1)
			{

				CurrentFlit[Index] = (unsigned long int)inData(Index);
				if(Useless[Index] == 0)
				{
					Desnecessaria[Index] = CurrentFlit[Index];
					outAck(Index, 0);
				}
				else if(Useless[Index] == 1)
				{
					outAck(Index, 0);
				}
				else if(Useless[Index] == 2)
				{																							// Esses anexos sao para os dados que vem no fooooooter ( e alguns de tolerancia por via das duvidas ).
					//CurrentFlit[Index] = (unsigned long int)inSize(Index);
					Packet[Index] = (unsigned long int*)calloc(sizeof(unsigned long int), CurrentFlit[Index] + (int)constPhitSize/4 + 7 +3);
					if(Packet[Index] == NULL)
					{
						//insira aqui a sua logica para quando faltar memoria
					}
					PacketSize[Index] = CurrentFlit[Index]; //+2; //porque os dois flits iniciais, Destino e tamanho, nao contam.
					Packet[Index][0] = Desnecessaria[Index]; //para o medidor!
					Packet[Index][1] = CurrentFlit[Index]-2;
					outAck(Index, 0);
				}
				else if(Useless[Index] < PacketSize[Index] && Useless[Index] > 2)
				{ //Porque, na verdade, o segundo flit vem onde "deveria" vir o tercero e tal e assim por diante
					Packet[Index][Useless[Index]-1 ] = CurrentFlit[Index]; //inData(Index);
					outAck(Index, 0);
				}

				else if(Useless[Index] == PacketSize[Index])
				{
					Packet[Index][Useless[Index]-1 ] = CurrentFlit[Index]; //inData(Index);
					outAck(Index,2);

				}



				++Useless[Index];
			}
			else
			{
				outAck(Index, 2);
			}


			if((Useless[Index] > PacketSize[Index]) &&(Useless[Index] > 3))
			{
				Useless[Index]--;
				Temp[Index] = CurrentTime;
				////////////// TIMESTAMP de Saida da rede E TAL
				sprintf(temp, "%0*X",constPhitSize, CurrentTime); // Obs que nao atrapalha mas tambem nao ajuda: o maximo do unsigned long int é 32 bits, ou seja FFFF FFFF.

				for(i=0,j=0,k=0;i<constPhitSize;i++,j++)
				{
					Destino[j]=temp[i];
					if(j==(int)(constPhitSize/4)-1)
					{
						sscanf(Destino, "%X", &Packet[Index][Useless[Index] ]); // (5 + constNumberOfChars) + k]);
						j=-1; //  porque na iteracao seguinte vai aumentar 1.
						k++;
						//
						Useless[Index]++;
					}
				}

				//TS de Criacao em decimal.
				Marca = 3;
				sprintf(temp, "");
				for(i=0;i<constPhitSize/4;i++)
				{														//Marca eh o indice inicial em que esta o valor no header do pacote.
					sprintf(temp2, "%0*X",constPhitSize/4 ,Packet[Index][Marca + i]);
					strcat(temp, temp2);
				}

				sscanf(temp, "%X", &TSCriacao[Index] );//&Packet[Index][Useless[Index] ] );
				Packet[Index][Useless[Index] ] = TSCriacao[Index];
				Useless[Index]++;

				//TS de Entrada na rede em decimal
				Marca = (constPhitSize/4) + 5;
				sprintf(temp, "");
				for(i=0;i<constPhitSize/4;i++)
				{
					sprintf(temp2, "%0*X",constPhitSize/4 ,Packet[Index][Marca + i]);
					strcat(temp, temp2);
				}
				sscanf(temp, "%X", &Packet[Index][Useless[Index] ] );
				Useless[Index]++;

				//TS de saida da rede em decimal
				TSSaida[Index] = (unsigned long int)CurrentTime;
				Packet[Index][Useless[Index] ] = TSSaida[Index];
				Useless[Index]++;

				Packet[Index][Useless[Index] ] = TSSaida[Index] - TSCriacao[Index];

				Useless[Index]++;


//-----------------TIME--------------------------------//
				//captura o tempo de simulacao
				ftime(&tp);
				//armazena o tempo final
				segundos_final=tp.time;
				milisegundos_final=tp.millitm;
				//tempo_final=(segundos_final*1000 + milisegundos_final) - (segundos_inicial*1000+milisegundos_inicial);



				Packet[Index][Useless[Index] ] = (unsigned long int)(segundos_final*1000 + milisegundos_final) - (segundos_inicial*1000+milisegundos_inicial);
				Useless[Index]++;
//-----------------/TIME--------------------------------//

				for(i=0;i<Useless[Index];i++)
				{

					if(i<Useless[Index]-5)
					{
						fprintf(Output[Index], "%0*X", (int)constPhitSize/4, Packet[Index][i]);
					}
					else
					{
						fprintf(Output[Index], "%u", Packet[Index][i]);
					}
					if(i<Useless[Index]-1)
					{
						fprintf(Output[Index]," ");
					}
				}

				fprintf(Output[Index], "\n");
				Useless[Index] = -1;
				free (Packet[Index]);
				PacketSize[Index] = 0;
			}
		}
		wait();
	}
}

#endif
