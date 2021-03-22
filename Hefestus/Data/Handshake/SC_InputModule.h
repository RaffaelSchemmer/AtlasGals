#ifndef INMODULE
#define INMODULE

#define constFlitSize $TFLIT$
#define constNumRot $NROT$
#define constNumRotX $WIDTH$
#define constNumRotY $HEIGHT$
#define constNumberOfChars 4

#include <stdio.h>
#include <string.h>
#include <systemc.h>


SC_MODULE(inputmodule)
{
	sc_in<sc_logic> clock;
	sc_in<sc_logic> reset;
$SIGNALS$

	void inline outTx(int Indice, int Booleano){
$OUTTX$
	}

	void inline outData(int Indice, unsigned long int Valor){
$OUTDATA$
	}

	int inline inAck(int Indice){
$INACK$
	}

	unsigned long int CurrentTime;

	void inline Timer();
	void inline TrafficGenerator();

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
	}
};

void inline inputmodule::Timer(){
	++CurrentTime;
}

void inline inputmodule::TrafficGenerator(){
	enum Estado{S1, S2, S3, S4, S5, FimArquivo};
	CurrentTime = 0;
	FILE* Input[constNumRot];
	char temp[100];
	char Destino[constFlitSize/4+1];
	unsigned long int CurrentFlit[constNumRot], Desnecessaria[constNumRot][100], TSSaida[constNumRot], Temp;
	unsigned long int* BigPacket[constNumRot];
	bool Mandando[constNumRot], Ajeitando[constNumRot];
	Estado EstadoAtual[constNumRot];
	int FlitNumber[constNumRot], NumberofFlits[constNumRot], WaitTime[constNumRot];
	int Index,i,j,k;

	for(i=0;i<constNumRot;i++){
		outTx(i,0);
	}

	for(Index=0;Index<constNumRot;Index++){
		sprintf(temp,"in%d.txt",Index);
		Input[Index] = fopen(temp,"r");
		EstadoAtual[Index] = S1;
		FlitNumber[Index] = 0;
	}

	while(true){
		for(Index=0;Index<constNumRot;Index++){
			if(Input[Index] != NULL && !feof(Input[Index]) && reset!=SC_LOGIC_1){
				//captura o tempo para entrada na rede
				if(EstadoAtual[Index] == S1){
						outTx(Index,0);
						FlitNumber[Index] = 0;
						fscanf(Input[Index],"%X",&CurrentFlit[Index]);
						WaitTime[Index] = CurrentFlit[Index];
						EstadoAtual[Index]=S2;
						if(feof(Input[Index])){
							fclose(Input[Index]);
							outTx(Index, 0);
							EstadoAtual[Index] = FimArquivo;
						}
				}
				//espera até o tempo para entrar na rede
				if(EstadoAtual[Index] == S2){
					outTx(Index,0);

					// eh na verdade o Wait Time do Pacote Atual, do roteador escolhido.
					if(CurrentTime<WaitTime[Index]){
						EstadoAtual[Index]=S2;
					}
					else{
						EstadoAtual[Index] = S3;
					}
				}
				if(EstadoAtual[Index]== S3){
					outTx(Index,0);
					//lendo os dois primeiros flits
					while(FlitNumber[Index] < 2){
						fscanf(Input[Index],"%X",&CurrentFlit[Index]);
						Desnecessaria[Index][FlitNumber[Index] ] = CurrentFlit[Index] ;
						if(FlitNumber[Index] == 1){
							// numero de flits que vai ter o TS de Entrada na Rede.
							Desnecessaria[Index][1]+= 4;
							NumberofFlits[Index] = Desnecessaria[Index][1];///////////////////////////precisa de +2 para o destino e tamanhoq ue são flits que nao contam,
							BigPacket[Index]=(unsigned long int*)calloc( sizeof(unsigned long int) , (2 + Desnecessaria[Index][1]));
							BigPacket[Index][0] = Desnecessaria[Index][0];
							BigPacket[Index][1] = Desnecessaria[Index][1];
						}
						FlitNumber[Index]++;
					}

					//lendo os flits até o número de sequencia
					while(FlitNumber[Index] < 9 ){
						fscanf(Input[Index], "%X", &CurrentFlit[Index]);
						BigPacket[Index][FlitNumber[Index] ] = CurrentFlit[Index];
						FlitNumber[Index]++;
					}

					FlitNumber[Index]+=4; //eh o espaco que depois vai ter o TS de entrada na rede =)

					//lendo os flits de payload
					while(FlitNumber[Index] < NumberofFlits[Index] + 2 ){ //2 é Target + Size
						fscanf(Input[Index], "%X", &CurrentFlit[Index]);
						BigPacket[Index][FlitNumber[Index] ] = CurrentFlit[Index];
						FlitNumber[Index]++;
					}
					EstadoAtual[Index] = S4;

					FlitNumber[Index]=-1; //Necessario, porque ele incrementa isso antes de mandar o primero, assim ele consegue realmente mandar todos os flits.
				}
				//comeca a transmitir os dados
				if(EstadoAtual[Index]==S4){
					if(inAck(Index) == 0){
						if(FlitNumber[Index]>NumberofFlits[Index]){
							outTx(Index, 0);
							EstadoAtual[Index] = S1;
							free(BigPacket[Index]);
						}
						else{
							FlitNumber[Index]++;
							if(FlitNumber[Index] == 0)  TSSaida[Index] = CurrentTime;
							if(FlitNumber[Index] == 9){
								Temp = FlitNumber[Index];
								sprintf(temp, "%0*X",constFlitSize, TSSaida[Index]); // Obs que nao atrapalha mas tambem nao ajuda: o maximo do unsigned long int é 32 bits, ou seja FFFF FFFF.

								for(i=0,j=0;i<constFlitSize;i++,j++){
									Destino[j]=temp[i];
									if(j==constFlitSize/4-1){
										sscanf(Destino, "%X", &BigPacket[Index][Temp ]); // (5 + constNumberOfChars) + k]);
										j=-1; //  porque na iteracao seguinte vai aumentar 1.
										Temp++;
									}
								}
							} //Aqui ele efetivamente Adicona o TS de Saida

							if( !feof(Input[Index]) ) outTx(Index, 1);
							outData(Index, BigPacket[Index][FlitNumber[Index] ]);
							EstadoAtual[Index]=S5;
						}
					}
					else{
						outTx(Index, 0);
						EstadoAtual[Index] = S5;
					}
				}
				else if(EstadoAtual[Index]==S5){
					if(inAck(Index)==1){
						outTx(Index, 0);
						if(FlitNumber[Index]>NumberofFlits[Index]){
							EstadoAtual[Index] = S1;
							free(BigPacket[Index]);
						}
						else{
							EstadoAtual[Index] = S4;
						}
					}
					else EstadoAtual[Index] = S5;

				}
				if(EstadoAtual[Index] == FimArquivo){
					outTx(Index, 0);
				}
			}
		}
		wait();
	}
}

#endif// INMODULE
