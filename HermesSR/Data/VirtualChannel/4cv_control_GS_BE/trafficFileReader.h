#ifndef _TRAFFICREADER_H
#define _TRAFFICREADER_H

#include <vector>
#include <iostream>
#include <fstream>
#include <string>

using namespace std;

class trafficFileReader{
private:
	unsigned long int injectionTime, header, size;
	int flitSize;
	vector<unsigned long int> payload;
	FILE *inTrafficFile;
	bool endOfTraffic, trafficExist;

public:
  trafficFileReader(int _flitSize);
  ~trafficFileReader();
	bool loadFile(string _filename);
	bool loadPacket();
	void printPacket();
	void addFlit(unsigned long int _flit);
	void addFlit(int _pos, unsigned long int _flit);
	void closeFile();
	unsigned long int getInjectionTime();
	unsigned long int getHeader();
	unsigned long int getSize();
	unsigned long int getPayloadFlit( int _pos);
	bool EOT();
	bool existTraffic();
};

#endif
