package AtlasPackage;

import javax.swing.*;
import java.io.*;
import javax.swing.*;
import java.util.*;
import java.io.*;

public class Mapping
{
	// Mapping Data
	private String tasking;
	private String node;
	private String node_x,node_y;
	
	// Methods of Map
	public void set_tasking(String t_tasking) { tasking = t_tasking; }
	public void set_node(String t_node) { node = t_node;}
	public void set_node_x(String x_node) { node_x = x_node; }
	public void set_node_y(String y_node) { node_y = y_node; }
	
	public String get_tasking() { return(tasking); }
	public String  get_node() {return(node); }
	public String  get_node_x() {return(node_x); }
	public String  get_node_y() {return(node_y); }
	
}
