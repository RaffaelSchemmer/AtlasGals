#ifndef _NI_H
#define _NI_H

#include <stdlib.h>
#include <string>
#include <iostream>
#include <fstream>
#include <vector>

using namespace std;

class NI{
  private:
    vector<unsigned long int> headerFlit[$XY$][$XY$];
    int useLane[$XY$][$XY$];
		
		int flitSize;
    unsigned long int xFBits(int _amount);
		int ctoi(char _value);
		void createHeader(int _source, int _target, string _path, int _flitSize);
		
  public:
    int getHeaderSize(int source, int target);
    unsigned long int getHeaderFlit(int source, int target, int _pos);
    int getLane(int source, int target);
    NI(string _filename, int _flitSize);
};
#endif
