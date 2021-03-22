package AtlasPackage;

import java.awt.event.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * Esta classe armazena o clock do roteador e o clock do ip
 * @author Raffael Bottoli Schemmer
 * @version --
 */
public class Clock 
{
	// Guarda clock de roteador e clock de IP
	private double clock_ip_input;
	private double clock_ip_output;
	
	private double clock_router;
	
	// Guarda o número absoluto do roteador Ex : 00,22
	private String number_router;

	// Guarda o número (String) em relação ao eixo X e ao eixo Y do roteador
	private String routerX;
	private String routerY;
	
	// Guarda o número (Int) em relação ao eixo X e ao eixo Y do roteador
	private int routeriX;
	private int routeriY;
	
	// Guarda o nome do clock do roteador e do ip
	private String label_clock_router;
	private String label_clock_ip_input;
	private String label_clock_ip_output;
	
	// Guarda a unidade do clock do ip e do roteador
	private String clock_unit_router;
	
	private String clock_unit_ip_input;
	private String clock_unit_ip_output;
	
	// Nome de saida do Router : Ex Router00SSSSS
	private String name;
	private String routerInterfaces;
	public Clock(){}
	
	// --
	//
	// Métodos SET
	//
	// --
	public void setUnitIpInput(String uip)
	{
		clock_unit_ip_input = uip;
	}
	public void setUnitIpOutput(String uip)
	{
		clock_unit_ip_output = uip;
	}
	
	public void setUnitRouter(String urouter)
	{
		clock_unit_router = urouter;
	}
	
	public void setNumberRouterIntX(int tx)
	{
		routerX = String.valueOf(tx);
		routeriX = tx;
	}
	
	public void setNumberRouterIntY(int ty)
	{
		routerY = String.valueOf(ty);
		routeriY = ty;
	}
	
	// Seta todos os valores default para o roteador
	public void setRouter(String c_router)
	{
		Clock c = new Clock();
		String saidaf,saida;
		double clk;
		int total=0,x;
			
		saida  = new String(c_router);
		int cont=0,inter=0;
		saidaf = new String();
		/* Bloco que le o label do roteador */
		while(saida.charAt(cont) != ' ')
		{
			saidaf = ""+saidaf+ saida.charAt(cont); 
			cont++;
		}
		label_clock_router = saidaf; 

		saidaf="";
		cont++;
		/* Bloco que le o clock do roteador */
		while(saida.charAt(cont) != ' ')
		{
			saidaf = ""+saidaf+ saida.charAt(cont); 
			cont++;
		}
		clock_router = Double.parseDouble(saidaf);

		cont++;
		saidaf="";
		/* Bloco que le a unidade do clock do roteador */
		while(cont < saida.length())
		{
			saidaf = ""+saidaf+ saida.charAt(cont); 
			cont++;
		}
		clock_unit_router = saidaf;
		
	}
	// Seta todos os valores para o ip
	public void setIpInput(String c_ip)
	{
		Clock c = new Clock();
		String saidaf,saida;
		double clk;
		int total=0,x;
			
		saida  = new String(c_ip);
		int cont=0,inter=0;
		saidaf = new String();
		/* Bloco que le o label do roteador */
		while(saida.charAt(cont) != ' ')
		{
			saidaf = ""+saidaf+ saida.charAt(cont); 
			cont++;
		}
		label_clock_ip_input = saidaf; 

		saidaf="";
		cont++;
		/* Bloco que le o clock do roteador */
		while(saida.charAt(cont) != ' ')
		{
			saidaf = ""+saidaf+ saida.charAt(cont); 
			cont++;
		}
		clock_ip_input = Double.parseDouble(saidaf);

		cont++;
		saidaf="";
		/* Bloco que le a unidade do clock do roteador */
		while(cont < saida.length())
		{
			saidaf = ""+saidaf+ saida.charAt(cont); 
			cont++;
		}
		clock_unit_ip_input = saidaf;
	}
	// Seta todos os valores para o ip
	public void setIpOutput(String c_ip)
	{
		Clock c = new Clock();
		String saidaf,saida;
		double clk;
		int total=0,x;
			
		saida  = new String(c_ip);
		int cont=0,inter=0;
		saidaf = new String();
		/* Bloco que le o label do roteador */
		while(saida.charAt(cont) != ' ')
		{
			saidaf = ""+saidaf+ saida.charAt(cont); 
			cont++;
		}
		label_clock_ip_output = saidaf; 

		saidaf="";
		cont++;
		/* Bloco que le o clock do roteador */
		while(saida.charAt(cont) != ' ')
		{
			saidaf = ""+saidaf+ saida.charAt(cont); 
			cont++;
		}
		clock_ip_output = Double.parseDouble(saidaf);

		cont++;
		saidaf="";
		/* Bloco que le a unidade do clock do roteador */
		while(cont < saida.length())
		{
			saidaf = ""+saidaf+ saida.charAt(cont); 
			cont++;
		}
		clock_unit_ip_output = saidaf;
	}
	// Cadastra o label do clock do roteador
	public void setLabelClockRouter(String label)
	{
		label_clock_router = label;
	}
	// Cadastra o label do clock do ip
	public void setLabelClockIpInput(String label)
	{
		label_clock_ip_input = label;
	}
	public void setLabelClockIpOutput(String label)
	{
		label_clock_ip_output = label;
	}
	public void setOnlyClockRouter(double clock)
	{
		clock_router = clock;
	}
	public void setOnlyClockIpInput(double clock)
	{
		clock_ip_input = clock;
	}
	public void setOnlyClockIpOutput(double clock)
	{
		clock_ip_output = clock;
	}
	// Cadastra o clock do roteador
	public void setClockRouter(double clock,String unit)
	{
		clock_router = clock;
		clock_unit_router = unit;
	}
	
	// Cadastra o clock do ip
	public void setClockIpInput(double clock,String unit)
	{
		clock_ip_input = clock;
		clock_unit_ip_input = unit;
	}
	public void setClockIpOutput(double clock,String unit)
	{
		clock_ip_output = clock;
		clock_unit_ip_output = unit;
	}
	// Cadastra o número do roteador (Tanto absoluto : 00) como isolado para X e para Y
	public void setNumberRouter(int x,int y)
	{
		number_router = String.valueOf(x) + String.valueOf(y);
		name = String.valueOf(x) + String.valueOf(y);
		routerX = String.valueOf(x);
		routerY = String.valueOf(y);
	}
	// Cadastra o nome de saida do roteador Ex : Router00SSSSS
	public void setNameRouter(String n)
	{
		name = n;
	}
	public void setRouterInterfaces(String vRouterINterfaces)
	{
		routerInterfaces = vRouterINterfaces;
	}
	
	// Cadastra todos os valores do roteador (Number_Router Label_Router Clock_Router Unit_Router Label_Ip_Input Clock_Ip_Input Unit_Ip_Input Label_Ip_Input Clock_Ip_Input Unit_Ip_Input)
	public void setAllClocks(String clocks)
	{
		Clock c = new Clock();
		String saidaf,saida;
		double clk;
		int total=0,x;
		saida  = new String(clocks);
		int cont=0,inter=0;
		saidaf = new String();
		
		// routerX - routeriX
		while(saida.charAt(cont) != ' ')
		{
			saidaf = ""+saidaf+ saida.charAt(cont); 
			cont++;
		}
		routerX = saidaf;
		routeriX = Integer.parseInt(saidaf);
        
        // routerY - routeriY
        
		cont++;
		saidaf="";
		while(saida.charAt(cont) != ' ')
		{
			saidaf = ""+saidaf+ saida.charAt(cont); 
			cont++;
		}
		
		routerY = saidaf;
		routeriY = Integer.parseInt(saidaf);
        
		// Number Router
   		
   		number_router = "" + routerX + routerY; 
        
        // Label CLock Router
        
		saidaf="";
		cont++;
		while(saida.charAt(cont) != ' ')
		{
			saidaf = ""+saidaf+ saida.charAt(cont); 
			cont++;
		}

		label_clock_router = saidaf; 

        // Clock Router
        
		cont++;
		saidaf="";
		while(saida.charAt(cont) != ' ')
		{
			saidaf = ""+saidaf+ saida.charAt(cont); 
			cont++;
		}
		
		clock_router = Double.parseDouble(saidaf); 
		
		// Unit Clock Router
		
		cont++;
		saidaf="";
		while(saida.charAt(cont) != ' ')
		{
			saidaf = ""+saidaf+ saida.charAt(cont); 
			cont++;
		}
		clock_unit_router = saidaf; 
        
        // Label Clock Ip Input
        
		saidaf="";
		cont++;
		while(saida.charAt(cont) != ' ')
		{
			saidaf = ""+saidaf+ saida.charAt(cont); 
			cont++;
		}
		label_clock_ip_input = saidaf;
		
		// Clock Ip Input
		
		cont++;
		saidaf="";
		while(saida.charAt(cont) != ' ')
		{
			saidaf = ""+saidaf+ saida.charAt(cont); 
			cont++;
		}
		clock_ip_input = Double.parseDouble(saidaf);
		
        // Unit Clock Ip Output
                
		saidaf="";
		cont++;
		while(saida.charAt(cont) != ' ')
		{
			saidaf = ""+saidaf+ saida.charAt(cont); 
			cont++;
		}
		clock_unit_ip_input = saidaf;
		
		// Label Clock Ip Output
		
		saidaf="";
		cont++;
		while(saida.charAt(cont) != ' ')
		{
			saidaf = ""+saidaf+ saida.charAt(cont); 
			cont++;
		}
		label_clock_ip_output = saidaf;
		
		// Clock Ip Output
		
		saidaf="";
		cont++;
		while(saida.charAt(cont) != ' ')
		{
			saidaf = ""+saidaf+ saida.charAt(cont); 
			cont++;
		}
		clock_ip_output = Double.parseDouble(saidaf);
		
		// Unit Ip Output
		
		cont++;
		saidaf="";
		while(cont < saida.length())
		{
			saidaf = ""+saidaf+ saida.charAt(cont); 
			cont++;
		}
		clock_unit_ip_output = saidaf;
	}

	// --
	//
	// Métodos GET
	//
	// --
	
	// Retorna todos os valores de clock do roteador e do IP In e Ip Out
	public String getAllClocks() // (Number_Router Label_Router Clock_Router Unit_Router Label_Ip_Input Clock_Ip_Input Unit_Ip_Input Label_Ip_Input Clock_Ip_Input Unit_Ip_Input)
	{
		return(routerX + " " + routerY + " " + label_clock_router + " " + clock_router + " " + clock_unit_router + " " + label_clock_ip_input + " " + clock_ip_input + " " + clock_unit_ip_input + " " + label_clock_ip_output + " " + clock_ip_output + " " + clock_unit_ip_output);
	}
	// Retorna o clock absoluto (label clock unit) do roteador Ex : clock_default 50 Mhz
	public String getRouter()
	{
		return(label_clock_router + " " + clock_router + " " + clock_unit_router);
	}
	// Retorna o clock absoluto (label clock unit) do ip Ex : clock_default 50 Mhz
	public String getIpInput()
	{
		return(label_clock_ip_input + " " + clock_ip_input + " " + clock_unit_ip_input);
	}
	public String getIpOutput()
	{
		return(label_clock_ip_output + " " + clock_ip_output + " " + clock_unit_ip_output);
	}
	// Retorna o label do clock do rotador Ex : clock_default
	public String getLabelClockRouter()
	{
		return(label_clock_router);
	}
	// Retorna o label do clock do ip Ex : clock_default
	public String getLabelClockIpInput()
	{
		return(label_clock_ip_input);
	}
	// Retorna o label do clock do ip Ex : clock_default
	public String getLabelClockIpOutput()
	{
		return(label_clock_ip_output);
	}
	// Retorna o clock do roteador
	public double getClockRouter()
	{
		return(clock_router);
	}
	// Retorna o clock do ip
	public double getClockIpInput()
	{
		return(clock_ip_input);
	}
	public double getClockIpOutput()
	{
		return(clock_ip_output);
	}
	// Retorna a unidade do clock do roteador
	public String getUnitRouter()
	{
		return(clock_unit_router);
	}
	// Retorna a unidade do clock do ip
	public String getUnitIpInput()
	{
		return(clock_unit_ip_input);
	}
	public String getUnitIpOutput()
	{
		return(clock_unit_ip_output);
	}
	// Retorna o número do roteador
	public String getNumberRouter()
	{
		return(number_router);
	}
	// Retorna o número referente ao eixo X do rotador
	public String getNumberRouterX()
	{
		return(routerX);
	}
	// Retorna o número referente ao eixo Y do roteador
	public String getNumberRouterY()
	{
		return(routerY);
	}
	// Retorna o nome do roteador com suas respectivas filas Ex : Router00AAAAA
	public String getNameRouter()
	{
		return(name);
	}
	public String getRouterInterfaces()
	{
		return(routerInterfaces);
	}
	public int getNumberRouterIntX() { return(routeriX); }
	public int getNumberRouterIntY() { return(routeriY); }
}
