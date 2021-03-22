package AtlasPackage;

import java.awt.event.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * Esta classe armazena um valor de clock da lista de clocks
 * @author Raffael Bottoli Schemmer
 * @version --
 */
public class AvailableClock 
{
	
	private String label;
	private double clock;
	private String unit;
	
	// --
	//
	// Métodos SET
	//
	// --

	public void setLabel(String vLabel)
	{
		label = vLabel;
	}
	public void setClock(double vClock)
	{
		clock = vClock;
	}
	public void setUnit(String vUnit)
	{
		unit = vUnit;
	}
	public void setAllAvailableValue(String vLabel,double vClock,String vUnit)
	{
		label = vLabel;
		clock = vClock;
		unit = vUnit;
	}
	public void setAllAvailableValueString(String allData)
	{
		String saidaf,saida;
		double clk;
		int total=0,x;
			
		saida  = new String(allData);
		int cont=0,inter=0;
		saidaf = new String();
		
		saidaf="";
		/* Bloco que le o label do roteador */
		while(saida.charAt(cont) != ' ')
		{
			saidaf = ""+saidaf+ saida.charAt(cont); 
			cont++;
		}

		label = saidaf; // 
		// System.out.println("label : " + label);

		cont++;
		saidaf="";
		/* Bloco que le o clock do roteador */
		while(saida.charAt(cont) != ' ')
		{
			saidaf = ""+saidaf+ saida.charAt(cont); 
			cont++;
		}
		clock = Double.parseDouble(saidaf); // 
		// System.out.println("clock : " + clock);

		cont++;
		saidaf="";
		/* Bloco que le a unidade do clock do roteador */
		while(cont < saida.length())
		{
			saidaf = ""+saidaf+ saida.charAt(cont); 
			cont++;
		}
		
		unit = saidaf; // 
		// System.out.println("unit : " + unit);
	}
	
	// --
	//
	// Métodos GET
	//
	// --
	
	public String getLabel()
	{
		return(label);
	}
	public double getClock()
	{
		return(clock);
	}
	public String getUnit()
	{
		return(unit);
	}
	public String getAllAvailableValue()
	{
		return(label + " " + clock + " " + unit);
	}
}
