#ifndef INMODULE
#define INMODULE

#define constFlitSize $TFLIT$
#define constNumRot $NROT$
#define constNumRotX $WIDTH$
#define constNumRotY $HEIGHT$
#define constNumLane $NLANE$

#define SRVC_CTRL 0
#define SRVC_GS 1
#define SRVC_BE 2

#define constNumberOfChars 4

#include <stdio.h>
#include <string.h>
#include <systemc.h>
#include "defs.h"
#include "trafficFileReader.h"
#include "NI.h"

SC_MODULE(inputmodule)
{
  sc_in<sc_logic> clock;
  sc_in<sc_logic> reset;
  sc_out<sc_logic> finish;

$SIGNALS$

  void inline outTx(int _index, int _lane, int _value){
$OUTTX$
  }

  void inline outData(int _index, unsigned long int _value){
$OUTDATA$
  }

  int inline inCredit(int _index, int _lane){
$INCREDIT$
  }

	void inline port_assign(){
$INPUTMODULE$
	}

  unsigned long int CurrentTime;
  NI *myNI[3];
	trafficFileReader *tfr[constNumRot][3];

  void inline Timer();
  void inline TrafficGenerator();

	void inline send_nothing(int _index, int _lane); 
  void inline send_data(int _index, int _lane, unsigned long int _data);
	bool inline hasTraffic(int _index);
	int inline getFirstToInject(int _index);
	unsigned long int inline xFBits(int _amount);
	

  SC_CTOR(inputmodule):
$VARIABLES$
  reset("reset"),
  clock("clock"){
    CurrentTime = 0;

    myNI[SRVC_CTRL] = new NI(_CTRL_ROTFILE_DEF,constFlitSize);
    myNI[SRVC_GS] = new NI(_GS_ROTFILE_DEF,constFlitSize);
    myNI[SRVC_BE] = new NI(_BE_ROTFILE_DEF,constFlitSize);

    SC_CTHREAD(TrafficGenerator, clock.pos());  //uma CTHREAD, comeca a executar na primeira subida de clock e. (por tal razao tem um loop infinito dentro dela)
    //watching(reset.delayed() == true); //caso o sinal do reset seja 1, ele volta pro comeco da CTHREAD.

    SC_METHOD(Timer); // pro timer
    sensitive_pos << clock;
    dont_initialize();

    SC_METHOD(port_assign); // pra deixar os sinais sempre atualizados...
    sensitive << clock;
  }
};

#endif// INMODULE
