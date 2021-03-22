#include "NI.h"

int NI::getHeaderSize(int _source, int _target){
  return headerFlit[_source][_target].size();
}

unsigned long int NI::getHeaderFlit(int _source, int _target, int _pos){
  return headerFlit[_source][_target].at(_pos);
}

int NI::getLane(int _source, int _target){
  return useLane[_source][_target];
}

NI::NI(string _filename, int _flitSize){

	// O arquivo a ser carregado devera respeitar a seguinte estrutura	
	// fonte;destino;lane;numero de direcoes a serem usados na definição do caminho; direcoes
	// Sendo que:
	//  * A separação entre os campos é feita com o caracter ';'
	//  * Fonte referencia um valor único que varia de 0(zero) até o nro de roteadores da rede - REPRESENTACAO EM DECIMAL
	//  * Destino referencia um valor único que varia de 0(zero) até o nro de roteadores da rede - REPRESENTACAO EM DECIMAL
	//  * Lane varia de acordo com a rede adotada. Para a Hermes SR 4CV, as lanes são 0(Zero), 1 (Um), 2(Dois) e 3(Três)
	//  * Numero de direcoes a serem usados na definição do caminho varia de 1 até o nro de canais necessarios para chegar ao roteador destino - REPRESENTACAO EM DECIMAL
	//  * Direcoes contém a sequencia de portas a serem adotadas até chegar ao roteador destino	- CADA CARACTER EH REPRESENTADO EM HEXA(4 bits)

	string line;
	int source, target, lane, ndirs;
	string dirs;
	int charPosition;
	char c_line[1000];
	
	ifstream routingFile(_filename.c_str(), ifstream::in);
	
	routingFile.getline(c_line, 1000);

	while(!routingFile.eof()){
		line=c_line;
	// separa fonte de injecao de pacote
		charPosition=line.find(';');
		source = atoi(line.substr(0,charPosition).c_str());
		line.erase(0, charPosition+1);

		// separa destino da comunicacao
		charPosition=line.find(';');
		target = atoi(line.substr(0,charPosition).c_str());
		line.erase(0, charPosition+1);

		// separa a lane a ser utilizada
		charPosition=line.find(';');
		lane = atoi(line.substr(0,charPosition).c_str());
		line.erase(0, charPosition+1);

		// separa o nro de direcoes a serem usados
		charPosition=line.find(';');
		ndirs = atoi(line.substr(0,charPosition).c_str());
		line.erase(0, charPosition+1);

		// separa as direcoes a serem utilizadas
		dirs=line;

		// ARMAZENA A INFORMACAO
		useLane[source][target]=lane;
		createHeader(source, target, dirs, _flitSize);

		routingFile.getline(c_line, 1000);
	}
	routingFile.close();

}

unsigned long int NI::xFBits(int _amount){
	unsigned long int value=0;
	for(int i=0; i<_amount; i++) value=(value<<1)|0x1;
	return value;
}

void NI::createHeader(int _source, int _target, string _path, int _flitSize){
		unsigned long int tmpFlit=0;
		int counter=0;
		
		for(int i=0; i<_path.size(); i++){		
			tmpFlit=(tmpFlit<<4)|ctoi(_path.at(i));
			counter++;
			if(counter==(_flitSize/4)){
				headerFlit[_source][_target].push_back(tmpFlit);
				counter=0;
				tmpFlit=0;
			}
		}

		if(counter!=0){
			while(counter!=(_flitSize/4)){
				tmpFlit=(tmpFlit<<4)|xFBits(4);
				counter++;
			}
			headerFlit[_source][_target].push_back(tmpFlit);
		}

		headerFlit[_source][_target].push_back(xFBits(_flitSize));
}

int NI::ctoi(char _value){
	int value;
	switch(_value){
		case '0': value=0; break;
		case '1': value=1; break;
		case '2': value=2; break;
		case '3': value=3; break;
		case '4': value=4; break;
		case '5': value=5; break;
		case '6': value=6; break;
		case '7': value=7; break;
		case '8': value=8; break;
		case '9': value=9; break;
		case 'A': 
		case 'a': value=10; break;
		case 'B': 
		case 'b': value=11; break;
		case 'C': 
		case 'c': value=12; break;
		case 'D': 
		case 'd': value=13; break;
		case 'E': 
		case 'e': value=14; break;
		case 'F': 
		case 'f': value=15; break;
		default: value=-1;
	};
	
	return value;
}
