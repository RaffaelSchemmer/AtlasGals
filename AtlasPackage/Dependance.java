package AtlasPackage;

import javax.swing.*;
import java.io.*;
import javax.swing.*;
import java.util.*;
import java.io.*;

public class Dependance
{

	// Dependence Data
	private String source;
	private ArrayList<String> target = new ArrayList<String>();

	// Methods of Dependence
	public void set_source(String t_source) { source = t_source; }
	public void set_target(String t_target)	{ target.add(t_target); }
	
	public String get_source() {return (source); }
	public String get_target(int index) { return(target.get(index)); }
	public int get_tam_array_target() { return(target.size()); }
}
