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

#endif //OUTMODULEROUTER
