package HermesG;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;


import AtlasPackage.Project;
import AtlasPackage.AvailableClock;
import AtlasPackage.Clock;
import AtlasPackage.Router;

/**
 * InterfaceClock implementa uma GUI para que o usuario selecione o clock do roteador
 * @author Raffael Bottoli Schemmer
 * @version
 */

public class InterfaceClock extends JFrame implements ActionListener
{

	private Project project;
	private Router r;
	private JLayeredPane panelClock;
	private JTextField rfrequency,ipfrequency;
	private JComboBox routerclock,ipinputclock,ipoutputclock;
	private JButton ok;
	private int dimXNet,dimYNet;
	private ArrayList<AvailableClock> clocks;
	private HermesGInterface hermesg;
	
	/**
	* Create the GUI allowing configure the clock of router and IP.
	* @param project The NoC project.
	* @param x and y.
	*/
		
	public InterfaceClock(Project project,Router router,ArrayList<AvailableClock> clock_list,HermesGInterface g)
	{
		super("Select the clock of the router " + router.getAddress() + " and its ip");
		this.project = project;
		this.r = router;
		this.clocks = clock_list;
		hermesg = g;
		initilize();
 	}
	
	/**
	 * Initialize the variables class.
	 */
	private void initilize()
	{
		dimXNet = project.getNoC().getNumRotX();
		dimYNet = project.getNoC().getNumRotY();
		
		addProperties();
		addComponents();
		setVisible(true);
	}

	/**
	* add the interface properties.
	*/
	private void addProperties()
	{
		getContentPane().setLayout(null);
		Dimension resolucao=Toolkit.getDefaultToolkit().getScreenSize();
		setSize(370,177);
		setLocation((resolucao.width-260)/2,(resolucao.height-380)/2);
		setResizable(false);
		addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){dispose();}});
 	}

	/**
	* add components to interface.
	*/
	private void addComponents()
	{
		int x = 10;
		int y = 10;
		int stepY = 25;
		addFrequencyRouter(x,y);
		y = y + stepY;
		addFrequencyIpInput(x,y);
		y = y + stepY;
		addFrequencyIpOutput(x,y);
		y = y + stepY;
		addOk(x+80,y);
	}

	private void addFrequencyRouter(int x,int y)
	{
		JLabel routerfrequency = new JLabel("Router Frequency");
		routerfrequency.setBounds(x+30,y+10,200,20);
		getContentPane().add(routerfrequency);
		
		x += 160;
		int b=0;
		ArrayList<String> word = new ArrayList<String> ();

		for(int i=0;i< project.getNoC().getClock().size();i++)
		{	
			if(r.getAddress().equals(project.getNoC().getClock().get(i).getNumberRouter()))
			{
				b = 1;
				int j;
				for(j=0;j<clocks.size();j++)
				{
					if(clocks.get(j).getAllAvailableValue().equals(project.getNoC().getClock().get(i).getRouter()))
					{
						word.add(clocks.get(j).getAllAvailableValue());
						for(int k = 0;k < clocks.size();k++)
						{
							if(!clocks.get(k).getAllAvailableValue().equals(word.get(0))) { word.add(clocks.get(k).getAllAvailableValue()); }	
						}
						break;
					}
				}
			}
		}
		if (b == 0)
		{
			for(int i=0;i<clocks.size();i++) word.add(clocks.get(i).getAllAvailableValue());
			routerclock = new JComboBox(word.toArray());
			routerclock.setBounds(x,y+12,150,20);
			routerclock.setToolTipText("");
			getContentPane().add(routerclock);
		}
		else
		{
			routerclock = new JComboBox(word.toArray());
			routerclock.setBounds(x,y+12,150,20);
			routerclock.setToolTipText("Available Clocks");
			getContentPane().add(routerclock);
		}
	}

	private void addFrequencyIpInput(int x,int y)
	{
		JLabel ipintputcorefrequency = new JLabel("Input Ip Frequency");
		ipintputcorefrequency.setBounds(x+30,y+10,200,20);
		getContentPane().add(ipintputcorefrequency);
		
		x += 160;
		int b=0;
		ArrayList<String> word = new ArrayList<String> ();


		/* Verifica se clock_list foi atualizado */
		for(int i=0;i< project.getNoC().getClock().size();i++)
		{	
			if(r.getAddress().equals(project.getNoC().getClock().get(i).getNumberRouter()))
			{
				b = 1;
				int j;
				for(j=0;j<clocks.size();j++)
				{
					if(clocks.get(j).getAllAvailableValue().equals(project.getNoC().getClock().get(i).getIpInput()))
					{
						word.add(clocks.get(j).getAllAvailableValue());
						for(int k = 0;k < clocks.size();k++)
						{
							if(!clocks.get(k).getAllAvailableValue().equals(word.get(0))) { word.add(clocks.get(k).getAllAvailableValue()); }	
						}
						break;
					}
				}
			}
		}
		if (b == 0)
		{
			for(int i=0;i<clocks.size();i++) word.add(clocks.get(i).getAllAvailableValue());
			ipinputclock = new JComboBox(word.toArray());
			ipinputclock.setBounds(x,y+12,150,20);
			ipinputclock.setToolTipText("Available Clocks");
			getContentPane().add(ipinputclock);
		}
		else
		{
			ipinputclock = new JComboBox(word.toArray());
			ipinputclock.setBounds(x,y+12,150,20);
			ipinputclock.setToolTipText("Available Clocks");
			getContentPane().add(ipinputclock);
		}
		
	}
	private void addFrequencyIpOutput(int x,int y)
	{
		JLabel ipoutputcorefrequency = new JLabel("Output Ip Frequency");
		ipoutputcorefrequency.setBounds(x+30,y+10,200,20);
		getContentPane().add(ipoutputcorefrequency);
		
		x += 160;
		int b=0;
		ArrayList<String> word = new ArrayList<String> ();


		/* Verifica se clock_list foi atualizado */
		for(int i=0;i< project.getNoC().getClock().size();i++)
		{	
			if(r.getAddress().equals(project.getNoC().getClock().get(i).getNumberRouter()))
			{
				b = 1;
				int j;
				for(j=0;j<clocks.size();j++)
				{
					if(clocks.get(j).getAllAvailableValue().equals(project.getNoC().getClock().get(i).getIpOutput()))
					{
						word.add(clocks.get(j).getAllAvailableValue());
						for(int k = 0;k < clocks.size();k++)
						{
							if(!clocks.get(k).getAllAvailableValue().equals(word.get(0))) { word.add(clocks.get(k).getAllAvailableValue()); }	
						}
						break;
					}
				}
			}
		}
		if (b == 0)
		{
			for(int i=0;i<clocks.size();i++) word.add(clocks.get(i).getAllAvailableValue());
			ipoutputclock = new JComboBox(word.toArray());
			ipoutputclock.setBounds(x,y+12,150,20);
			ipoutputclock.setToolTipText("Available Clocks");
			getContentPane().add(ipoutputclock);
		}
		else
		{
			ipoutputclock = new JComboBox(word.toArray());
			ipoutputclock.setBounds(x,y+12,150,20);
			ipoutputclock.setToolTipText("Available Clocks");
			getContentPane().add(ipoutputclock);
		}
		
	}
	private void addOk(int x,int y)
	{
		ok= new JButton("Ok");
		ok.setBounds(x+60,y+30,60,25);
		ok.addActionListener(this);
		getContentPane().add(ok);
	}
	/**
	 * Execute an action associated to the selected event
	*/
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == ok)
		{
		
		// Router Clock
			
		Clock c = new Clock();
		String saidaf,saida,clock_unit;
		double clk;
		int total=0,x;
			
		saida  = new String((String)routerclock.getSelectedItem());
		int cont=0,inter=0;
		saidaf = new String();

		/* Bloco que le o o label do clock */
		while(saida.charAt(cont) != ' ')
		{
			saidaf = ""+saidaf+ saida.charAt(cont); 
			cont++;
		}
		c.setLabelClockRouter(saidaf);
		//System.out.println("label_router : " + saidaf);

		saidaf="";
		cont++;
		
		/* Bloco que le o clock do roteador */
		while(saida.charAt(cont) != ' ')
		{
			saidaf = ""+saidaf+ saida.charAt(cont); 
			cont++;
		}
		clk = Double.parseDouble(saidaf); // 
		//System.out.println("clock_router : " + clk);

		cont++;
		saidaf="";
		/* Bloco que le a unidade do clock do roteador */
		while(cont < saida.length())
		{
			saidaf = ""+saidaf+ saida.charAt(cont); 
			cont++;
		}
		
		clock_unit = saidaf; // 
		//System.out.println("clock_unit_router : " + clock_unit);
		
		c.setClockRouter(clk,clock_unit);
		c.setNumberRouter(r.getAddressX(),r.getAddressY());

		// Ip Clock Input	 	

		cont=0;
		saidaf="";
		saida  = new String((String)ipinputclock.getSelectedItem());

		/* Bloco que le o o label do clock */
		while(saida.charAt(cont) != ' ')
		{
			saidaf = ""+saidaf+ saida.charAt(cont); 
			cont++;
		}
		c.setLabelClockIpInput(saidaf);
		//System.out.println("label_ip : " + saidaf);

		saidaf="";
		cont++;

		/* Bloco que le o clock do roteador */
		while(saida.charAt(cont) != ' ')
		{
			saidaf = ""+saidaf+ saida.charAt(cont); 
			cont++;
		}
		clk = Double.parseDouble(saidaf); // 
		//System.out.println("clock_ip : " + clk);

		cont++;
		saidaf="";
		/* Bloco que le a unidade do clock do roteador */
		while(cont < saida.length())
		{
			saidaf = ""+saidaf+ saida.charAt(cont); 
			cont++;
		}
		
		clock_unit = saidaf; // 
		//System.out.println("clock_unit : " + clock_unit);
		
		c.setClockIpInput(clk,clock_unit);
		c.setNumberRouter(r.getAddressX(),r.getAddressY());

		// Ip Clock Output

		cont=0;
		saidaf="";
		saida  = new String((String)ipoutputclock.getSelectedItem());

		/* Bloco que le o o label do clock */
		while(saida.charAt(cont) != ' ')
		{
			saidaf = ""+saidaf+ saida.charAt(cont); 
			cont++;
		}
		c.setLabelClockIpOutput(saidaf);
		//System.out.println("label_ip : " + saidaf);

		saidaf="";
		cont++;

		/* Bloco que le o clock do roteador */
		while(saida.charAt(cont) != ' ')
		{
			saidaf = ""+saidaf+ saida.charAt(cont); 
			cont++;
		}
		clk = Double.parseDouble(saidaf); // 
		//System.out.println("clock_ip : " + clk);

		cont++;
		saidaf="";
		/* Bloco que le a unidade do clock do roteador */
		while(cont < saida.length())
		{
			saidaf = ""+saidaf+ saida.charAt(cont); 
			cont++;
		}
		
		clock_unit = saidaf; // 
		//System.out.println("clock_unit : " + clock_unit);
		
		c.setClockIpOutput(clk,clock_unit);
		c.setNumberRouter(r.getAddressX(),r.getAddressY());
		
		
		for(int i=0;i<project.getNoC().getNumRotX()*project.getNoC().getNumRotY();i++)
		{
			if(project.getNoC().getClock().get(i).getNumberRouter().equals(r.getAddress()))
			{
				project.getNoC().getClock().set(i,c);
			}
		}

		hermesg.getNoCPanel().setEnabled(true);
		super.dispose();

		}
	}
}
