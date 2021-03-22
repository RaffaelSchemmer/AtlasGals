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
#include "defs.h"
#include "NI.h"

SC_MODULE(inputmodule)
{
  sc_in<sc_logic> clock;
  sc_in<sc_logic> reset;
  sc_out<sc_logic> finish;
$SIGNALS$

  void inline outTx(int Indice, int Booleano){
$OUTTX$
  }

  void inline outData(int Indice, unsigned long int Valor){
$OUTDATA$
  }

  int inline inCredit(int Indice){
$INCREDIT$
  }

  unsigned long int CurrentTime;
  NI *myNI;

  void inline Timer();
  void inline TrafficGenerator();
  void inline port_assign();

  SC_CTOR(inputmodule):

$VARIABLES$
  reset("reset"),
  clock("clock"){
    CurrentTime = 0;
    myNI = new NI(_ROTFILE_DEF, constFlitSize);

    SC_CTHREAD(TrafficGenerator, clock.pos());  //uma CTHREAD, comeca a executar na primeira subida de clock e. (por tal razao tem um loop infinito dentro dela)
    //watching(reset.delayed() == true); //caso o sinal do reset seja 1, ele volta pro comeco da CTHREAD.

    SC_METHOD(Timer); // pro timer
    sensitive_pos << clock;
    dont_initialize();

    SC_METHOD(port_assign); // pra deixar os sinais sempre atualizados...
    sensitive << clock;
  }
};


void inline inputmodule::port_assign(){
$INPUTMODULE$
}

void inline inputmodule::Timer(){
  ++CurrentTime; //variavel que conta no numero de clocks, eh resetada no reset.
}

void inline inputmodule::TrafficGenerator(){

/*******************************************************************************************************************************************
** pacote BE:
**
**  timestamp   target  size   source  timestamp de saida do nodo  nro de sequencia  timestamp de entrada na rede     payload
**      0        00XX   XXXX    00XX      XXXX XXXX XXXX XXXX         XXXX XXXX          XXXX XXXX XXXX XXXX            XXXX ...
**
********************************************************************************************************************************************/

  enum Estado{S1, S2, S3, S4, FimArquivo};
  Estado EstadoAtual[constNumRot];
  FILE* Input[constNumRot];
  char temp[constFlitSize+1], TimestampNet[constFlitSize/4+1];
  unsigned long int CurrentFlit[constNumRot],Target[constNumRot],Size[constNumRot];
  unsigned long int* BigPacket[constNumRot];
  //
  unsigned long int* path[constNumRot];
  int FlitNumber[constNumRot], NumberOfFlits[constNumRot], WaitTime[constNumRot];
  int Index,i,j,k;
  bool active[constNumRot];
  int n_active = 0;

  //
  unsigned long int timeStamp[constNumRot], HeaderSize[constNumRot];

  for(Index=0;Index<constNumRot;Index++){
    sprintf(temp,"in%d.txt",Index);
    Input[Index] = fopen(temp,"r");
    if(Input[Index] != NULL){
	active[Index] = true;
	n_active++;
    }
    else{
	active[Index] = false;
    }

    outTx(Index,0);
    outData(Index,0);
    EstadoAtual[Index] = S1;
    FlitNumber[Index] = 0;
  }

  while(true){
    for(Index=0;Index<constNumRot;Index++){
      if(reset!=SC_LOGIC_1 && active[Index]){
        //captura o tempo para entrada na rede
        if(EstadoAtual[Index] == S1){
            outTx(Index,0);
            outData(Index,0);
            FlitNumber[Index] = 0;
            HeaderSize[Index]=0;
            fscanf(Input[Index],"%X",&CurrentFlit[Index]);
            WaitTime[Index] = CurrentFlit[Index];
            EstadoAtual[Index] = S2;
            if(feof(Input[Index])){
              fclose(Input[Index]);
              active[Index] = false;
              n_active--;
              outTx(Index,0);
              outData(Index,0);
              EstadoAtual[Index] = FimArquivo;
            }
        }
        //espera até o tempo para entrar na rede
        if(EstadoAtual[Index] == S2){
          if(CurrentTime<WaitTime[Index])
            EstadoAtual[Index]=S2;
          else
            EstadoAtual[Index] = S3;
        }
        if(EstadoAtual[Index]== S3){
          //Captura o target
          fscanf(Input[Index],"%X",&CurrentFlit[Index]);
          Target[Index] = ((CurrentFlit[Index] >> 4)&0x0f)+((CurrentFlit[Index]&0x0f)*constNumRotX);
          HeaderSize[Index]=myNI->getHeaderSize(Index,(int) Target[Index]);

          //Captura o size
          fscanf(Input[Index],"%X",&CurrentFlit[Index]);
          Size[Index] = CurrentFlit[Index];
          Size[Index] += 4; //4 = Reserva espaco do timestamp de entrada na rede
          //
          NumberOfFlits[Index] = Size[Index] + HeaderSize[Index]+1; //1 = size
          BigPacket[Index]=(unsigned long int*)calloc( sizeof(unsigned long int) , NumberOfFlits[Index]);
          //path[Index]=(unsigned long int*)calloc( sizeof(unsigned long int) , HeaderSize[Index]);
          //
          //memcpy(BigPacket[Index],myNI->getpath(Index,Target[Index]),sizeof(unsigned long int)*HeaderSize[Index]);
          for(int z=0; z<HeaderSize[Index]; z++)
						BigPacket[Index][z]=myNI->getHeaderFlit(Index,Target[Index],z);
          FlitNumber[Index]+=HeaderSize[Index];

          BigPacket[Index][FlitNumber[Index]] = Size[Index];
          FlitNumber[Index]++;

          timeStamp[Index]=FlitNumber[Index]+7;

          //Captura a origem, o timestamp de saida nodo (4 flits) e o número de sequência
          while(FlitNumber[Index] < timeStamp[Index]){
            fscanf(Input[Index], "%X", &CurrentFlit[Index]);
            BigPacket[Index][FlitNumber[Index]] = CurrentFlit[Index];
            FlitNumber[Index]++;
          }

          //Insere espaço para o timestamp de entrada na rede (4 flits)
          FlitNumber[Index]+=4;

          //Captura o payload
          while(FlitNumber[Index] < NumberOfFlits[Index]){
            fscanf(Input[Index], "%X", &CurrentFlit[Index]);
            BigPacket[Index][FlitNumber[Index]] = CurrentFlit[Index];
            FlitNumber[Index]++;
          }
          EstadoAtual[Index] = S4;
          FlitNumber[Index] = 0;
        }
        //comeca a transmitir os dados
        if(EstadoAtual[Index]==S4 && inCredit(Index)==1){
          if(FlitNumber[Index]>=NumberOfFlits[Index]){
            outTx(Index,0);
            outData(Index,0);
            EstadoAtual[Index] = S1;
            free(BigPacket[Index]);
          }
          else{
            if(FlitNumber[Index] == 0){
              sprintf(temp, "%0*X",constFlitSize, CurrentTime);
              k = HeaderSize[Index]+1+7; //tamanho do header + size + controle
              for(i=0,j=0;i<constFlitSize;i++,j++){
                TimestampNet[j]=temp[i];
                if(j==constFlitSize/4-1){
                  sscanf(TimestampNet, "%X", &BigPacket[Index][k]);
                  j=-1; //  porque na iteracao seguinte vai aumentar 1.
                  k++;
                }
              }
            }

            outTx(Index,1);
            outData(Index, BigPacket[Index][FlitNumber[Index] ]);
            FlitNumber[Index]++;
          }
        }
        if(EstadoAtual[Index] == FimArquivo){
          outTx(Index,0);
          outData(Index,0);
        }
      }
    }
    finish = (n_active==0)? SC_LOGIC_1: SC_LOGIC_0;
    wait();
  }
}

#endif// INMODULE
