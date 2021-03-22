#ifndef OUTPUTMODULE
#define OUTPUTMODULE

#define constFlitSize $tam_flit$
#define constNumRot $max_router$

#include "systemc.h"
#include <stdio.h>
#include <string.h>
#include <sys/timeb.h>

SC_MODULE(outputmodule)
{
	sc_in<sc_logic> clock;
	sc_in<sc_logic> reset;
	$idle_pin$

	sc_in<sc_bv<8> > RotID;
	sc_out<sc_logic> outcredit;
	sc_in<sc_lv<constFlitSize> > indata;
	sc_in<sc_logic> intx;
	sc_in<sc_logic> fifo_signal;
	int CurrentTime;
	void inline TrafficStalker();
	void inline port_assign();
	void inline Timer();

	SC_CTOR(outputmodule) :

	outcredit("outcredit"),
	indata("indata"),
	intx("intx"),
	RotID("RotID"),
	fifo_signal("fifo_signal"),
	reset("reset"),
	clock("clock")
	{
		CurrentTime = 0;
		SC_THREAD(TrafficStalker);
		//watching(reset.delayed()== true);
		sensitive_pos << clock;
		
        SC_METHOD(Timer);
		sensitive_pos << clock;
		dont_initialize();
		
        SC_METHOD(port_assign);
		sensitive << clock;
		dont_initialize();
	}
};

void inline outputmodule::port_assign()
{
	outcredit = SC_LOGIC_1;
}
void inline outputmodule::Timer()
{
    if(reset.read() == SC_LOGIC_0 ) ++CurrentTime;
}

void inline outputmodule::TrafficStalker()
{

    /*******************************************************************************************************************************************************************************************
    ** pacote BE:
    **
    ** O que chega da rede
    **
    ** Destino | Tampayload | Origem | TSG=TSH | NS | TSInjeção (em clocks de quem injeta) | FMS | resto do Payload |
    **
    ** O que o receptor(output) gera como arquivo de saida
    **
    ** Destino | Tampayload | Origem | TSG | NS | TSInjeção | FMS | resto do Payload | FMD | TCDPF | TCDUF |
    **    1         1           1       4    2        4        4           n            4      4       4
    **
    ********************************************************************************************************************************************************************************************/

	FILE* Output;

	unsigned long int CurrentFlit;
    unsigned long int TCDPF,TCDUF;
	int EstadoAtual,Size;
	int i, j, Index, NumRot;
	char temp[100];

	char TimeTargetHex[100];
	unsigned long int TimeTarget;
	unsigned long int TimeSourceCore;
	unsigned long int TimeSourceNet;

	struct timeb tp;
	int segundos_inicial, milisegundos_inicial;
	int segundos_final, milisegundos_final;
	unsigned long int TimeFinal;
	
	while(reset.read() != SC_LOGIC_0) wait(1,SC_NS);
	
	NumRot = RotID.read().to_uint();

    //-----------------TIME--------------------------------//
	//captura o tempo
	ftime(&tp);
	//armazena o tempo inicial
	segundos_inicial=tp.time;
	milisegundos_inicial=tp.millitm;
    //-----------------------------------------------------//

	sprintf(temp,"out%d.txt",NumRot);
	Output = fopen(temp,"w");
	Size = 0;
	EstadoAtual = 1;
	$idle_start$
    while(true)
    {
		$idle_state_0$
		if(intx == SC_LOGIC_1)
		{
			$idle_state_1$
			if(EstadoAtual == 1) // Destino
			{
				CurrentFlit = (unsigned long int)indata.read().to_uint();
                TCDPF = CurrentTime; // Anota o tempo em que recebeu o primeiro flit
				fprintf(Output,"%0*X",(int)constFlitSize/4,CurrentFlit);
				EstadoAtual++;
			}
			else if(EstadoAtual == 2) // Tamanho do payload
            {
		    	CurrentFlit = (unsigned long int)indata.read().to_uint();
				fprintf(Output," %0*X",(int)constFlitSize/4,CurrentFlit);
				Size = CurrentFlit;
				EstadoAtual++;
			}
			else if(EstadoAtual == 3) // Origem
            {
				CurrentFlit = (unsigned long int)indata.read().to_uint();
				fprintf(Output," %0*X",(int)constFlitSize/4,CurrentFlit);
				Size--;
				EstadoAtual++;
			}
			else if(EstadoAtual>=4 && EstadoAtual<=7) // Timestamp de geracao
            {
				CurrentFlit = (unsigned long int)indata.read().to_uint();
				fprintf(Output," %0*X",(int)constFlitSize/4,CurrentFlit);
				if(EstadoAtual==4) TimeSourceCore=0;
				Size--;
				EstadoAtual++;
			}
			else if(EstadoAtual == 8 || EstadoAtual == 9) // Numero de sequencia
            {
				CurrentFlit = (unsigned long int)indata.read().to_uint();
				fprintf(Output," %0*X",(int)constFlitSize/4,CurrentFlit);
				Size--;
				EstadoAtual++;
			}
			else if(EstadoAtual>=10 && EstadoAtual<=13) // Timestamp de injecao
            {
				CurrentFlit = (unsigned long int)indata.read().to_uint();
				fprintf(Output," %0*X",(int)constFlitSize/4,CurrentFlit);
				Size--;
				EstadoAtual++;
                if(Size == 0)
                {
					TCDUF = CurrentTime; // Anota o tempo em que recebeu o ultimo flit
					$fifo_out_cond$
                    fprintf(Output," %0*X",(int)constFlitSize/4,TCDPF);
                    fprintf(Output," %0*X\n",(int)constFlitSize/4,TCDUF);
		    		EstadoAtual = 1;
                }
			}
			else if(EstadoAtual == 14) // Payload
            {
                if(Size > 0)
                {
                    CurrentFlit = (unsigned long int)indata.read().to_uint();
				    fprintf(Output," %0*X",(int)constFlitSize/4,CurrentFlit);
                    Size--;
                }
                if(Size == 0)
				{
                    TCDUF = CurrentTime; // Anota o tempo em que recebeu o ultimo flit
                    $fifo_out_cond$
                    fprintf(Output," %0*X",(int)constFlitSize/4,TCDPF);
                    fprintf(Output," %0*X\n",(int)constFlitSize/4,TCDUF);
		    		EstadoAtual = 1;
				}               
			}
		}
		wait();
    }
}
#endif
