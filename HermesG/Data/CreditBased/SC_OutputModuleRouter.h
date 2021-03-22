#ifndef OUTMODULEROUTER
#define OUTMODULEROUTER

#define constFlitSize $flit_size$
#define constNumPort $num_port$
#define constNumRot $num_rot$
#define constNumRotX $num_rot_x$
#define constNumRotY $num_rot_y$

#include "systemc.h"
#include <stdio.h>
#include <string.h>


SC_MODULE(outmodulerouter)
{
	$input_clock$
	sc_in<sc_logic> ref_clock;
	sc_in<sc_logic> reset;
	$input_port$

	int inline inTx(int Roteador, int Porta)
	{
		$tx_router$	
	}

	unsigned long inline inData(int Roteador, int Porta)
	{
		$data_router$
	}

	int inline inCredit(int Roteador, int Porta)
	{		
		$data_credit$	
	}

	unsigned long int CurrentRefTime;
	$cont_clock$

    void inline TimerRef_Clock();	
	$timer_clock$


	SC_CTOR(outmodulerouter) :	
	ref_clock("ref_clock"),
	reset("reset"),

	$mapping_port$
	
};

void inline outmodulerouter::TimerRef_Clock()
{
	++CurrentRefTime;
}
$func_timer_port$

$decl_func_timer$

#endif //OUTMODULEROUTER
