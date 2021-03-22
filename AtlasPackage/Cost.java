package AtlasPackage;

import javax.swing.*;
import java.io.*;
import javax.swing.*;
import java.util.*;
import java.io.*;

public class Cost
{

	// Cost Data
	private int number_task;
	private String source;
	private String target;
	private double number_comunication;
	private double number_computation;
	private double task_deadline;

	// Methods of Cost
	public int get_number_task() { return(number_task); }
	public String get_source() { return(source); }
	public String get_target() { return(target); }
	public double get_number_comunication() { return(number_comunication); }
	public double get_number_computation() { return(number_computation); }
	public double get_task_deadline() { return(task_deadline); }
	
	public void set_number_task(int t_number_task) { number_task = t_number_task; }
	public void set_source(String t_source) { source = t_source; }
	public void set_target(String t_target) { target = t_target; }
	public void set_number_comunication(double t_number_comunication) { number_comunication = t_number_comunication; }
	public void set_number_computation(double t_number_computation) { number_computation = t_number_computation; }
	public void set_task_deadline(double t_task_deadline) { task_deadline = t_task_deadline; }
}
