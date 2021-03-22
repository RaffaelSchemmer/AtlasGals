#ifndef INMODULE
#define INMODULE

#define constFlitSize 16
#define constNumRot 16
#define constNumRotX 4
#define constNumRotY 4

#include <stdio.h>
#include <string.h>
#include <systemc.h>

SC_MODULE(inputmodule)
{
	sc_in<sc_logic> clock;
	sc_in<sc_logic> reset;
	sc_in<sc_logic> incredit;
	sc_out<sc_lv<constFlitSize> > outdata;
	sc_in<sc_bv<8> > RotID;
	sc_out<sc_logic> outtx;
	sc_out<sc_logic> outclock;
		sc_out<sc_logic> finish;
	unsigned long int CurrentTime;
	enum Estado{S1, S2, S3, S4, S5, FimArquivo};
	Estado EstadoAtual;
	FILE *Input;
    
    char temp1[constFlitSize+1], temp[constFlitSize+1], TimestampNet[constFlitSize/4+1];
	unsigned long int CurrentFlit,Target,Size;
	unsigned long int* BigPacket;
	int FlitNumber, NumberofFlits, WaitTime;
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
			finish = SC_LOGIC_0;
	}
		else finish = SC_LOGIC_1;
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
		outdata = 0;
		EstadoAtual = S1;
		FlitNumber = 0;
        CurrentTime = 0;
	}
	else
	{
		if(Input != NULL && !feof(Input))
		{
			if(EstadoAtual == S1) 
			{
				outtx = SC_LOGIC_0;
				outdata = 0;
				FlitNumber = 0;
				fscanf(Input,"%X",&CurrentFlit); 
				WaitTime = CurrentFlit;
				EstadoAtual = S2;
				if(feof(Input))
				{
					fclose(Input);
					outtx = SC_LOGIC_0;
					outdata = 0;
					EstadoAtual = FimArquivo;
				}
			}
		}
		if(EstadoAtual == S2) 
		{
			if(CurrentTime<WaitTime)
				EstadoAtual=S2;
			else
				EstadoAtual = S3;
		}
		if(EstadoAtual== S3) 
		{
			fscanf(Input,"%X",&CurrentFlit); 
			Target = CurrentFlit;
			FlitNumber++;
			
			fscanf(Input,"%X",&CurrentFlit); 
			Size = CurrentFlit;
            Size += 4;
            NumberofFlits = Size + 2;
			BigPacket=(unsigned long int*)calloc( sizeof(unsigned long int) , NumberofFlits);
			BigPacket[0] = Target; 
            BigPacket[1] = Size; 
            FlitNumber++;
			while(FlitNumber < 9 ) 
			{
				fscanf(Input, "%X", &CurrentFlit);
                BigPacket[FlitNumber] = CurrentFlit;
				FlitNumber++;
			}
     		FlitNumber+=4;
            
            // Efetua a leitura do payload
     		while(FlitNumber < NumberofFlits)
			{
                fscanf(Input, "%X", &CurrentFlit);
                BigPacket[FlitNumber] = CurrentFlit;
				FlitNumber++;
			}
            EstadoAtual = S4;
			FlitNumber = 0;
		}
		if(EstadoAtual==S4 && incredit==1) 
		{
			if(FlitNumber == 0){
				sprintf(temp,"%0*X",constFlitSize,CurrentTime);
				k = 9; 
				for(i=0,j=0;i<constFlitSize;i++,j++){
					TimestampNet[j]=temp[i];
					if(j == constFlitSize/4-1){
						sscanf(TimestampNet, "%X", &BigPacket[k]);
						j=-1;
						k++;
					}
				}
			}
			outtx = SC_LOGIC_1;
			outdata = BigPacket[FlitNumber];
			FlitNumber++;
            		if(FlitNumber == NumberofFlits){
				EstadoAtual = S5;
				return;
			}
		}
		if(EstadoAtual==S5 && incredit==1){
			free(BigPacket);
			EstadoAtual = S1;
			outtx = SC_LOGIC_0;
		}
		if(EstadoAtual == FimArquivo)
		{
			outtx = SC_LOGIC_0;
			outdata = 0;
			init = false;
				finish = SC_LOGIC_1;
		}
	}
}
#endif// INMODULE
