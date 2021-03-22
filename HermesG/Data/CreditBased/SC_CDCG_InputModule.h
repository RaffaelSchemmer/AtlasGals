#ifndef INMODULE
#define INMODULE

#define constFlitSize $tam_flit$
#define constNumRot $max_router$
#define constNumRotX $max_x$
#define constNumRotY $max_y$

#include <stdio.h>
#include <string.h>
#include <systemc.h>

$logic_app_var$

SC_MODULE(inputmodule)
{
	sc_in<sc_logic> clock;
	sc_in<sc_logic> reset;
	sc_in<sc_logic> incredit;
	
	sc_in<sc_logic> send;
	sc_out<sc_logic> receive;
	
	sc_out<sc_lv<constFlitSize> > outdata;
	sc_in<sc_bv<8> > RotID;
	sc_out<sc_logic> outtx;
	sc_out<sc_logic> outclock;
	sc_out<sc_logic> finish;
	
	unsigned long int CurrentTime;
	enum Estado{S0, S1, S2, S3, S4, FimArquivo};
	Estado EstadoAtual;
	FILE *Input;
    
    char temp1[constFlitSize+1], temp[constFlitSize+1], TimestampNet[constFlitSize/4+1];
	unsigned long int CurrentFlit;
	unsigned long int Source,Target,ProcTime,pcktSize,SeqNumber;
	unsigned long int* BigPacket;
	int FlitNumber, NumberofFlits, WaitTime;
	int contProcTime,time_request_noc,time_start_noc;
	int i,j,k, NumRot;
    bool init;
    
	void inline Timer();
	void inline TrafficGenerator();
	void inline port_assign();
    void InitGenerator();
    
	SC_CTOR(inputmodule):
	incredit("incredit"),
	outdata("outdata"),
	outtx("outtx"),
	outclock("outclock"),

	RotID("RotID"),


	reset("reset"),
 	clock("clock")
 	{
		CurrentTime = 0;
		init = false;

		SC_METHOD(TrafficGenerator);  
		sensitive_pos << clock;
		dont_initialize();

		SC_METHOD(InitGenerator);  
		sensitive_pos << reset;
		dont_initialize();

		SC_METHOD(Timer);
		sensitive_pos << clock;
		dont_initialize();

		SC_METHOD(port_assign);
		sensitive << clock;
		dont_initialize();
	}
};

void inline inputmodule::port_assign()
{
	outclock = clock;
}

void inline inputmodule::Timer()
{
	++CurrentTime;
}

void inputmodule::InitGenerator()
{
  	NumRot = RotID.read().to_uint();
	sprintf(temp,"in%d.txt",NumRot);
	// printf("Open input file. in%d.txt \n", NumRot);
	Input = fopen(temp,"r");
	if(Input != NULL)
	{
		printf("ABRIU INPUT %d \n", NumRot);
		init = true;
		$out_ini$
	}
	$out_end_0$
}
/*******************************************************************************************************************************************
** Pacote Trafego Entrada :
**
**  TimeStamp | Destino | Tampayload | Origem |   TSH    |   NS   | resto do Payload | 
**    1flit       1flit     1flit        1flit   4flits    2flits         .....
********************************************************************************************************************************************/

/*******************************************************************************************************************************************
	** Pacote Trafego Saída :
**
**     Destino | Tampayload |   Origem  |   TSG=TSH |   NS     | TSInjecao (em clocks de quem injeta) |  resto do Payload |
**       1flit      1flit       1flit       4flits     2flits                 4flits                          ......                          
**
********************************************************************************************************************************************/

void inline inputmodule::TrafficGenerator()
{
	if(reset==SC_LOGIC_1 || !init)
	{  
		outtx = SC_LOGIC_0;
		receive = SC_LOGIC_0;
		outdata = 0;
		EstadoAtual = S0;
		FlitNumber = 0;
        CurrentTime = 0;
	}
	else
	{
		if(Input != NULL && !feof(Input))
		{
			$logic_app_ini$
		}
		if(EstadoAtual == S1)
		{
			outtx = SC_LOGIC_0;
			outdata = 0;
			FlitNumber = 0;
			contProcTime = 0;
			time_request_noc=0;
			time_start_noc =0;
			FlitNumber = 0;
			
			long pos = ftell(Input);
			fscanf(Input,"%X",&CurrentFlit);
			if(feof(Input))
			{
				fclose(Input);
				outtx = SC_LOGIC_0;
				outdata = 0;
				EstadoAtual = FimArquivo;
			}
			else
			{
				fseek (Input,-pos,SEEK_SET);
				pos = ftell (Input);
			}
			
			if(send == SC_LOGIC_1)
			{
				receive = SC_LOGIC_0;
				fscanf(Input,"%X",&CurrentFlit);
				Source = CurrentFlit;
				fscanf(Input,"%X",&CurrentFlit);
				Target = CurrentFlit;
				fscanf(Input,"%X",&CurrentFlit);
				ProcTime = CurrentFlit;
				EstadoAtual = S2;
			}
		} 
		// Computa o tempo informado no pacote
		if(EstadoAtual == S2) 
		{
			
			if(contProcTime < ProcTime)
			{
				EstadoAtual=S2;
				contProcTime++;
			}
			else
			{
				// Captura o número de flits do pacote
				fscanf(Input,"%X",&CurrentFlit); 
				pcktSize = CurrentFlit;
				// Captura o número de sequência do pacote
				fscanf(Input,"%X",&CurrentFlit); 
				SeqNumber = CurrentFlit;
				
				// |Target|TamPayload|Source|TTE|NS|TER|Payload|
				BigPacket = (unsigned long int*)calloc( sizeof(unsigned long int),pcktSize+1);
				BigPacket[0] = Target; 
				BigPacket[1] = pcktSize-2; 
				BigPacket[2] = Source; 
				BigPacket[3] = SeqNumber; 
				EstadoAtual = S3;
			}
		}
		if(EstadoAtual== S3) 
		{
			if(incredit == 1)
            {
				time_start_noc = CurrentTime;
				if(time_request_noc == 0) time_request_noc = CurrentTime;
				BigPacket[4] = time_request_noc;
				BigPacket[5] = time_start_noc;
				for(int i=6,j=0;i <= pcktSize;i++,j++)
				{
					 BigPacket[i] = j;
				}
				EstadoAtual = S4;
			}
			else
			{
				time_request_noc = CurrentTime;
				EstadoAtual = S3;
			}
		}
		if(EstadoAtual==S4 && incredit==1) 
		{
			if(FlitNumber == pcktSize)
			{
				receive = SC_LOGIC_1;
			    outtx = SC_LOGIC_0;
			    outdata = 0;
				EstadoAtual = S1;
				free(BigPacket);
				$logic_app_end$
			}
			else
			{
				outtx = SC_LOGIC_1;
				outdata = BigPacket[FlitNumber];
                FlitNumber++;
                EstadoAtual=S4;
			}
		}
		if(EstadoAtual == FimArquivo)
		{
			outtx = SC_LOGIC_0;
			outdata = 0;
			init = false;
			$out_end_1$
			EstadoAtual= FimArquivo;
		}
	}
}
#endif// INMODULE
