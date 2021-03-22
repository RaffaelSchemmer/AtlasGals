package HermesG;

import javax.swing.*;
import java.io.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import java.text.*;
import java.math.*;  

import AtlasPackage.Project;
import AtlasPackage.AvailableClock;
import AtlasPackage.Clock;

/**
 * InterfaceEditClock implementa uma GUI para que o usuario edite um clock presente na NOC
 * @author Raffael Bottoli Schemmer/Ricardo Aquino Guazzelli
 * @version
 */

public class InterfaceEditClock extends JFrame implements ActionListener
{
	private Project project;
	private HermesGInterface hermesg;
	private ArrayList<AvailableClock> clock_list;
	private int dimXNet,dimYNet;
	private JComboBox view_clocks,addaclock;
	private JButton edit;
	private JTextField new_clock,rfrequency;
	private double frequencia;
	
	public InterfaceEditClock(Project project,HermesGInterface g,ArrayList<AvailableClock> clock)
	{
		super("Edit a Clock of Available Clocks");
		this.project = project;
		hermesg = g;
		clock_list = clock;
		initilize();
 	}

	private void initilize()
	{
		dimXNet = project.getNoC().getNumRotX();
		dimYNet = project.getNoC().getNumRotY();
		
		addProperties();
		addComponents();
		setVisible(true);
	}

	private void addProperties()
	{
		getContentPane().setLayout(null);
		Dimension resolucao=Toolkit.getDefaultToolkit().getScreenSize();
		setSize(320,170);
		setLocation((resolucao.width-260)/2,(resolucao.height-380)/2);
		setResizable(false);
		addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){dispose();}});
 	}

	private void addComponents()
	{
		int x = 10;
		int y = 10;
		addAvailableClocks(x,y);
		y+=20;
		addClockBox(x,y);
		y+=20;
		addFrequencyRouter(x,y);
		y+=40;
		addEdit(x+80,y);
	}

	private void addAvailableClocks(int x,int y)
	{
		JLabel selectClock = new JLabel("Select Clock: ");
		selectClock.setBounds(x+30,y+10,200,20);
		getContentPane().add(selectClock);
		
		x += 150;
		ArrayList<String> word = new ArrayList<String>(); 
		for(int i=0;i<clock_list.size();i++) word.add(clock_list.get(i).getAllAvailableValue());
		view_clocks = new JComboBox(word.toArray());
		view_clocks.setBounds(x,y+10,120,20);
		view_clocks.setToolTipText("List of available clocks to edit");
		getContentPane().add(view_clocks);

	}

	private void addClockBox(int x,int y)
	{
		JLabel selectClock = new JLabel("New name for Clock: ");
		selectClock.setBounds(x+30,y+10,200,20);
		getContentPane().add(selectClock);
		
		x += 150;
		new_clock = new JTextField("Clock0");
		new_clock.setHorizontalAlignment(JTextField.LEFT);
		new_clock.setBounds(x,y+12,120,20);
		new_clock.setToolTipText("Enter a new name for select clock");
		getContentPane().add(new_clock);
	}
	
	private void addFrequencyRouter(int x,int y)
	{
		JLabel routerfrequency = new JLabel("Clock Frequency");
		routerfrequency.setBounds(x+30,y+10,200,20);
		getContentPane().add(routerfrequency);
		
		x += 160;
		rfrequency = new JTextField("0");
		rfrequency.setHorizontalAlignment(JTextField.LEFT);
		rfrequency.setBounds(x-10,y+12,60,20);
		rfrequency.setToolTipText("Enter a new valid value for the frequency");
		getContentPane().add(rfrequency);

		x += 50;
		
		
		String[] unit = {"Mhz"};
		addaclock = new JComboBox(unit);
		addaclock.setBounds(x,y+12,60,20);
		addaclock.setToolTipText("Select a new unit to the frequency informed");
		getContentPane().add(addaclock);
	}
	
	private void addEdit(int x,int y)
	{
		edit = new JButton("Edit");
		edit.setBounds(x+10,y+10,100,25);
		edit.addActionListener(this);
		edit.setToolTipText("Press EDIT to edit the selected clock");
		getContentPane().add(edit);
	}
	
	/* Método que verifica se nome informado é valido */
	public int checkName()
	{
		String name = "";
		int c2=0,c3=0,c4=0,c5=0;
		name  = new_clock.getText().toLowerCase();
						
		/* C2 Nome do novo clock deve iniciar por uma letra (A-Z) ou (a-z). */
						
		if(!name.toLowerCase().equals("") && (((int)name.charAt(0) >= 65 && (int)name.charAt(0) <= 90) || ((int)name.charAt(0) >= 97 && (int)name.charAt(0) <= 122)))
		{
			/* C3 Todo nome pode após contemplar item 2 pode conter números (0-9) e um ponto ou uma virgula. */
				
			int cp=0,cv=0; // Conta o número de pontos e de virgulas "Por regra só deve existir um ponto ou uma virgula"
			for(int tmp=0;tmp < name.length();tmp++)
			{
								
				/* Se for A-Z ou a-z ou 0-9 */
				if(((int)name.charAt(tmp) >= 65 && (int)name.charAt(tmp) <= 90) || ((int)name.charAt(tmp) >= 97 && (int)name.charAt(tmp) <= 122) || ((int)name.charAt(tmp) >= 48 && (int)name.charAt(tmp) <= 57)) { }
								
				/* Se o caracter da coordenada X for ponto e não existir ponto (cp == 0) */
				else if((int)name.charAt(tmp) == 46 && (cp == 0)) cp++;
								
				/* Se o caracter da coordenada X for virgula e não existir virgula (cp == 0) */
				else if((int)name.charAt(tmp) == 44 && (cv == 0)) cv++;
								
				else c3=1;
								
			}
		}
		else c2=1;
						
		/* C5 As seguintes palavras reservadas não devem ser aceitas como nomes de clock */
		if(name.equals("abs") || name.equals("access") || name.equals("after") || name.equals("alias") || name.equals("all") || name.equals("and") || name.equals("array") || name.equals("assert") || name.equals("attribute") || name.equals("architecture") || name.equals("begin") || name.equals("block") || name.equals("body") || name.equals("buffer") || name.equals("bus") || name.equals("case") || name.equals("constant") || name.equals("component") || name.equals("configuration") || name.equals("downto") || name.equals("disconnect") || name.equals("else") || name.equals("elsif") || name.equals("end") || name.equals("entity") || name.equals("exit") || name.equals("file") || name.equals("for") || name.equals("function") || name.equals("group") || name.equals("generic") || name.equals("generate") || name.equals("guarded") || name.equals("if") || name.equals("impure") || name.equals("in") || name.equals("inertial") || name.equals("inout") || name.equals("is") || name.equals("label") || name.equals("loop") || name.equals("literal") || name.equals("linkage") || name.equals("library") || name.equals("mod") || name.equals("map") || name.equals("nand") || name.equals("new") || name.equals("") || name.equals("next") || name.equals("nor") || name.equals("not") || name.equals("null") || name.equals("of") || name.equals("on") || name.equals("open") || name.equals("or") || name.equals("others") || name.equals("out") || name.equals("port") || name.equals("pure") || name.equals("process") || name.equals("package") || name.equals("postponed") || name.equals("procedure") || name.equals("range") || name.equals("record") || name.equals("rem") || name.equals("ror") || name.equals("rol") || name.equals("return") || name.equals("reject") || name.equals("report") || name.equals("register") || name.equals("select") || name.equals("sla") || name.equals("sll") || name.equals("sra") || name.equals("srl") || name.equals("signal") || name.equals("subtype") || name.equals("severity") || name.equals("shared") || name.equals("then") || name.equals("to") || name.equals("type") || name.equals("transport") || name.equals("use") || name.equals("until") || name.equals("units") || name.equals("unaffected") || name.equals("variable") || name.equals("wait") || name.equals("when") || name.equals("while") || name.equals("with") || name.equals("xnor") || name.equals("xor")) { c5=1; }
								
		/* C2 - Primeiro caracter do nome do clock deve ser uma letra */
		else if(c2 == 1) JOptionPane.showMessageDialog(this,"First character of clock name must be a letter","Warning",JOptionPane.INFORMATION_MESSAGE);			
						
		/* C3 - Nomes para clocks devem ser possuir um ponto ou uma virgula e serem formados por letras e números */
		else if(c3 == 1) JOptionPane.showMessageDialog(this,"Clock names must be have one point or comma and be composed of letters and numbers","Warning",JOptionPane.INFORMATION_MESSAGE);									
		/* C5 - Nome do clock é igual a uma palavra reservada da linguagem VHDL */
		else if(c5 == 1) JOptionPane.showMessageDialog(this,"Choose another name for clock. Same names of a reserved word in VHDL are not accepted","Warning",JOptionPane.INFORMATION_MESSAGE);	
	
		/* Se todas as condições para cadastrar um novo clock estiverem corretas, os passos descritos acima devem ser seguidos */
		if(c2 == 0 && c3 == 0 && c5 == 0) return(1);
		else return(0);
	}
	/* Método que verifica se a frequência informada é valida */				
	public int checkFrequency()
	{
		int c4=0;
		frequencia = Double.parseDouble(rfrequency.getText());
		
		/* C4 - Frequência do clock deve ser maior ou igual a 0.1 e menor ou igual a 5000 */
		if(frequencia >= 0.1 && frequencia <= 5000) {}
		else c4=1;
		
		/* C4 - Frequência do clock deve ser maior ou igual a 0.1 e menor ou igual a 5000 */
		if(c4 == 1)	JOptionPane.showMessageDialog(this,"A valid value for the clock must be greater or equal than 0.1 and less or equal than 5000","Warning",JOptionPane.INFORMATION_MESSAGE);
				
		if(c4 == 0) return(1);
		else return(0);
	}
	/* Método que cadastra um novo nome para o clock */
	public void addName()
	{
		// 3
		// Procurar na lista de AvailableClocks pelo AvailableClock selecionado e substituir com os novos dados
						
		ArrayList<AvailableClock> clocks = project.getNoC().getRefClockList(); // AvailableClockList
		AvailableClock tClockOld = new AvailableClock(),tClockNew = new AvailableClock();
		String name = new String();
		name  = new_clock.getText().toLowerCase();
		String selAClock   = (String) view_clocks.getSelectedItem(); // Clock selecionado na lista
		
		for(int tmp=0;tmp < clocks.size();tmp++)
		{
			if(clocks.get(tmp).getAllAvailableValue().equals(selAClock))
			{
				tClockOld.setAllAvailableValueString(selAClock); // tClockOld contem o valor antigo de AvailableClock
				clocks.get(tmp).setLabel(name);
								
				tClockNew = clocks.get(tmp); // tClockNew contem o novo valor de AvailableClock
				hermesg.setAvaibleClocks(clocks);	
				break;
			}
		}
					
		// 4
		// Pesquisar na lista de Clocks dos Roteadores/IPs pelo antigo AvailableClock e substituir pelo novo.
					
		/* Buscar os valores cadastrados nos IPs e nos roteadores */
		ArrayList<Clock> Nclocks = project.getNoC().getClock();
					
		for(int tmp=0;tmp < Nclocks.size();tmp++)
		{
			/* Se encontrar Old AvailableClock no roteador substituir pelo New AvailableClock */
			if(Nclocks.get(tmp).getLabelClockRouter().equals(tClockOld.getLabel()) && Nclocks.get(tmp).getClockRouter() == tClockOld.getClock() && Nclocks.get(tmp).getUnitRouter().equals(tClockOld.getUnit())) 
			{
				Nclocks.get(tmp).setLabelClockRouter(tClockNew.getLabel());
			}
						
			/* Se encontrar Old AvailableClock no IP Input substituir pelo New AvailableClock */
			if(Nclocks.get(tmp).getLabelClockIpInput().equals(tClockOld.getLabel()) &&  Nclocks.get(tmp).getClockIpInput() == tClockOld.getClock() && Nclocks.get(tmp).getUnitIpInput().equals(tClockOld.getUnit())) 
			{
				Nclocks.get(tmp).setLabelClockIpInput(tClockNew.getLabel());
			}					
			/* Se encontrar Old AvailableClock no IP Output substituir pelo New AvailableClock */
			if(Nclocks.get(tmp).getLabelClockIpOutput().equals(tClockOld.getLabel()) &&  Nclocks.get(tmp).getClockIpOutput() == tClockOld.getClock() && Nclocks.get(tmp).getUnitIpOutput().equals(tClockOld.getUnit())) 
			{
				Nclocks.get(tmp).setLabelClockIpOutput(tClockNew.getLabel());
			}
			
		}
	}
	/* Método que cadastra uma nova frequência para um clock editado */
	public void addFrequency()
	{
		String selAClock   = (String) view_clocks.getSelectedItem(); // Clock selecionado na lista
		frequencia = Double.parseDouble(rfrequency.getText());				
		ArrayList<AvailableClock> clocks = project.getNoC().getRefClockList(); // AvailableClockList
		AvailableClock tClockOld = new AvailableClock(),tClockNew = new AvailableClock();
		
		// Procurar na lista de AvailableClocks pelo AvailableClock selecionado e substituir com os novos dados
		for(int tmp=0;tmp < clocks.size();tmp++)
		{
			if(clocks.get(tmp).getAllAvailableValue().equals(selAClock))
			{
				tClockOld.setAllAvailableValueString(selAClock); // tClockOld contem o valor antigo de AvailableClock
				clocks.get(tmp).setClock(frequencia);
				tClockNew = clocks.get(tmp); // tClockNew contem o novo valor de AvailableClock
				hermesg.setAvaibleClocks(clocks);	
				break;
			}
		}
					
		// Pesquisar na lista de Clocks dos Roteadores/IPs pelo antigo AvailableClock e substituir pelo novo.
					
		/* Buscar os valores cadastrados nos IPs e nos roteadores */
		ArrayList<Clock> Nclocks = project.getNoC().getClock();
					
		for(int tmp=0;tmp < Nclocks.size();tmp++)
		{
			/* Se encontrar Old AvailableClock no roteador substituir pelo New AvailableClock */
			if(Nclocks.get(tmp).getLabelClockRouter().equals(tClockOld.getLabel()) && Nclocks.get(tmp).getClockRouter() == tClockOld.getClock() && Nclocks.get(tmp).getUnitRouter().equals(tClockOld.getUnit())) 
			{
				Nclocks.get(tmp).setOnlyClockRouter(tClockNew.getClock());
			}
						
			/* Se encontrar Old AvailableClock no IP substituir pelo New AvailableClock */
			if(Nclocks.get(tmp).getLabelClockIpOutput().equals(tClockOld.getLabel()) &&  Nclocks.get(tmp).getClockIpOutput() == tClockOld.getClock() && Nclocks.get(tmp).getUnitIpOutput().equals(tClockOld.getUnit())) 
			{
				Nclocks.get(tmp).setOnlyClockIpInput(tClockNew.getClock());
			}					
			
			/* Se encontrar Old AvailableClock no IP substituir pelo New AvailableClock */
			if(Nclocks.get(tmp).getLabelClockIpOutput().equals(tClockOld.getLabel()) &&  Nclocks.get(tmp).getClockIpOutput() == tClockOld.getClock() && Nclocks.get(tmp).getUnitIpOutput().equals(tClockOld.getUnit())) 
			{
				Nclocks.get(tmp).setOnlyClockIpOutput(tClockNew.getClock());
			}
			
		}
	}
	public void addAll()
	{
		// Procurar na lista de AvailableClocks pelo AvailableClock selecionado e substituir com os novos dados
						
		ArrayList<AvailableClock> clocks = project.getNoC().getRefClockList(); // AvailableClockList
		AvailableClock tClockOld = new AvailableClock(),tClockNew = new AvailableClock();
		String name = new String();
		frequencia = Double.parseDouble(rfrequency.getText());				
		name  = new_clock.getText().toLowerCase();
		String selAClock   = (String) view_clocks.getSelectedItem(); // Clock selecionado na lista
		
		for(int tmp=0;tmp < clocks.size();tmp++)
		{
			if(clocks.get(tmp).getAllAvailableValue().equals(selAClock))
			{
				tClockOld.setAllAvailableValueString(selAClock); // tClockOld contem o valor antigo de AvailableClock
				clocks.get(tmp).setLabel(name);
				clocks.get(tmp).setClock(frequencia);				
				tClockNew = clocks.get(tmp); // tClockNew contem o novo valor de AvailableClock
				hermesg.setAvaibleClocks(clocks);	
				break;
			}
		}
					
		// 4
		// Pesquisar na lista de Clocks dos Roteadores/IPs pelo antigo AvailableClock e substituir pelo novo.
					
		/* Buscar os valores cadastrados nos IPs e nos roteadores */
		ArrayList<Clock> Nclocks = project.getNoC().getClock();
		
		for(int tmp=0;tmp < Nclocks.size();tmp++)
		{
			/* Se encontrar Old AvailableClock no roteador substituir pelo New AvailableClock */
			if(Nclocks.get(tmp).getLabelClockRouter().equals(tClockOld.getLabel()) && Nclocks.get(tmp).getClockRouter() == tClockOld.getClock() && Nclocks.get(tmp).getUnitRouter().equals(tClockOld.getUnit())) 
			{
				Nclocks.get(tmp).setLabelClockRouter(tClockNew.getLabel());
				Nclocks.get(tmp).setOnlyClockRouter(tClockNew.getClock());
				Nclocks.get(tmp).setUnitIpInput(tClockNew.getUnit());
			}		
			/* Se encontrar Old AvailableClock no IP Input substituir pelo New AvailableClock */
			if(Nclocks.get(tmp).getLabelClockIpInput().equals(tClockOld.getLabel()) &&  Nclocks.get(tmp).getClockIpInput() == tClockOld.getClock() && Nclocks.get(tmp).getUnitIpInput().equals(tClockOld.getUnit())) 
			{
				Nclocks.get(tmp).setLabelClockIpInput(tClockNew.getLabel());
				Nclocks.get(tmp).setOnlyClockIpInput(tClockNew.getClock());
				Nclocks.get(tmp).setUnitIpOutput(tClockNew.getUnit());
			}					
			/* Se encontrar Old AvailableClock no IP Output substituir pelo New AvailableClock */
			if(Nclocks.get(tmp).getLabelClockIpOutput().equals(tClockOld.getLabel()) &&  Nclocks.get(tmp).getClockIpOutput() == tClockOld.getClock() && Nclocks.get(tmp).getUnitIpOutput().equals(tClockOld.getUnit())) 
			{
				Nclocks.get(tmp).setLabelClockIpOutput(tClockNew.getLabel());
				Nclocks.get(tmp).setOnlyClockIpOutput(tClockNew.getClock());
				Nclocks.get(tmp).setUnitRouter(tClockNew.getUnit());
			}
			
		}
	}
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == edit)
		{

			try
			{
				/* Critérios a serem cuidados durante a edição de um clock 
				  C1 = Uma vez um valor de AvailableClock for modificado, procurar se já não existe um clock com esse nome cadastrado em AvailableClocks
				  C2 = As condições para um clock válido devem seguir as condições C2/C3/C4/C5 do arquivo "InterfaceNewClock.java" 
				  C3 = Uma vez a condição C1 atendida, a lista de clocks dos roteadores e dos IPs com o valor do clock antigo devem ser modificadas para os novos valores.
				*/
				
				String name = new String();
				name  = new_clock.getText().toLowerCase();
				// Nome não frequência não - Deixa como esta
				if(name.toLowerCase().equals("") && rfrequency.getText().equals(""))
				{
					hermesg.getEditButton().setEnabled(true);
					super.dispose();
				}
				// Nome sim frequência não
				else if(!name.toLowerCase().equals("") && rfrequency.getText().equals("")) 
				{
					if(this.checkName() == 1)
					{
						addName();		
						hermesg.getEditButton().setEnabled(true);
						super.dispose();
					}
				}
				// Nome não frequência sim
				else if(name.toLowerCase().equals("") && !rfrequency.getText().equals("")) 
				{
					if(this.checkFrequency() == 1)
					{
						addFrequency();
						hermesg.getEditButton().setEnabled(true);
						super.dispose();
					}
				}
				// Nome sim frequência sim
				else if(!name.toLowerCase().equals("") && !rfrequency.getText().equals("")) 
				{
					if(this.checkName() == 1)
					{
						if(this.checkFrequency() == 1)
						{
							addAll();
							hermesg.getEditButton().setEnabled(true);
							super.dispose();
						}
					}		
				}
			}
			catch(NumberFormatException x)
			{
				JOptionPane.showMessageDialog(this,"Enter a valid value for the clock","Warning",JOptionPane.INFORMATION_MESSAGE);	
			}
		}	
	}
}
