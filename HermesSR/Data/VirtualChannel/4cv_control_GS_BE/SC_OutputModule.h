#ifndef OUTMODULE
#define OUTMODULE

#define constFlitSize $TFLIT$
#define constNumRot $NROT$
#define constNumLane $NLANE$

#include "systemc.h"
#include <stdio.h>
#include <string.h>
#include <sys/timeb.h>

SC_MODULE(outmodule){
	sc_in<sc_logic> clock;
	sc_in<sc_logic> reset;
	sc_in<sc_logic> finish;
$SIGNALS$

	int inline inTx(int _index, int _lane){
$INTX$
	}

	unsigned long int inline inData(int _index){
$INDATA$
	}

	void inline port_assign(){
$OUTMODULE$
	}

	unsigned long int CurrentTime;

	void inline TrafficStalker();
	void inline Timer();

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

#endif
