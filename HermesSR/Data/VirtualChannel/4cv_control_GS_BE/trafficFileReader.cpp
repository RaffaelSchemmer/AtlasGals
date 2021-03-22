#include "trafficFileReader.h"

trafficFileReader::trafficFileReader(int _flitSize){
	inTrafficFile=NULL;
	flitSize=_flitSize;
}

trafficFileReader::~trafficFileReader(){
	if(inTrafficFile!=NULL) fclose(inTrafficFile);
}

void trafficFileReader::closeFile(){
	endOfTraffic=true;
	injectionTime=header=size=0;
	fclose(inTrafficFile);
	inTrafficFile=NULL;
}

bool trafficFileReader::loadFile(string _filename){
	if(inTrafficFile!=NULL){fclose(inTrafficFile); inTrafficFile=NULL;}
	inTrafficFile=fopen(_filename.c_str(), "r");
	if (inTrafficFile!=NULL){
		endOfTraffic=false;
#ifdef _DEBUG
cout << "DEBUG: Traffic file " << _filename << " successfully loaded..."<< endl;
#endif
	}
	else{
		endOfTraffic=true;
#ifdef _DEBUG
		cout << "ERROR: File " << _filename << " cannot be openned..."<< endl;
#endif
	}
	return !endOfTraffic;
}
bool trafficFileReader::loadPacket(){
	unsigned long int flit=0;
	size=0;
	if((inTrafficFile==NULL)||(endOfTraffic)){
		return false;
#ifdef _DEBUG
cout << "DEBUG: Error loading packet. Load its traffic file first..."<< endl;
#endif
	}
	else{
		payload.clear();
		if(fscanf(inTrafficFile,"%X",&injectionTime)==EOF){ closeFile(); return false;};
		if(fscanf(inTrafficFile,"%X",&header)==EOF){ closeFile(); return false;};
		if(fscanf(inTrafficFile,"%X",&size)==EOF){ closeFile(); return false;};
		for(int i=0; i<size; i++){
			if(fscanf(inTrafficFile,"%X",&flit)==EOF){ payload.clear(); closeFile(); return false;};
			payload.push_back(flit);
		}
		return true;
	}
}

void trafficFileReader::printPacket(){
	if(payload.size()==0){
		cout << "No packet loaded" << endl;
	}
	else{
		cout << "Header:" << hex << header << endl;
		cout << "Size:" << hex << size << endl;
		for(int i=0;i <payload.size(); i++)
			cout << "Payload[" << hex << i << "]:" << hex << payload.at(i) << endl;
	}
}

void trafficFileReader::addFlit(unsigned long int _flit){
	if(payload.size()==0){
#ifdef _DEBUG
		cout << "Loaded packet first." << endl;
#endif
	}
	else if(payload.size()!=size){
#ifdef _DEBUG
		cout << "Packet already consumed. It is now allowed to add new flits." << endl;
#endif
	}
	else{
		size++;
		payload.push_back(_flit);
	}
}

void trafficFileReader::addFlit(int _pos, unsigned long int _flit){
	if(!(_pos<payload.size() and _pos>=0)){
#ifdef _DEBUG
		cout << "Flit cannot be loaded." << endl;
#endif
	}
	else if(payload.size()!=size){
#ifdef _DEBUG
		cout << "Packet already consumed. It is now allowed to add new flits." << endl;
#endif
	}
	else{
		size++;
		payload.insert(payload.begin() + _pos,_flit);
	}
}

unsigned long int trafficFileReader::getInjectionTime(){
	return injectionTime;
}

unsigned long int trafficFileReader::getHeader(){
	return header;
}

unsigned long int trafficFileReader::getSize(){
	return size;
}

unsigned long int trafficFileReader::getPayloadFlit( int _pos){
	unsigned long int value;	
	if(_pos<payload.size() and _pos>=0){
		value=payload.at(_pos);
	}
	else{
		value=0;
#ifdef _DEBUG
		cout << "ERROR: Position  " << _pos << " does not exist. Maximum position limited to " << payload.size()-1 << endl;
#endif
	}
	return value;
}

bool trafficFileReader::EOT(){
	return endOfTraffic;
}

bool trafficFileReader::existTraffic(){
	return (inTrafficFile!=NULL);
}

