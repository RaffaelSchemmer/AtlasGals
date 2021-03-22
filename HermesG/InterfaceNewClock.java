package HermesG;

import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.awt.*;
import java.util.*;
import java.text.*;
import java.math.*;  

import AtlasPackage.Project;
import AtlasPackage.AvailableClock;

/**
 * InterfaceClock implementa uma GUI para que o usuario selecione o clock do roteador
 * @author Raffael Bottoli Schemmer
 * @version
 */

public class InterfaceNewClock extends JFrame implements ActionListener
{

	private Project project;
	private HermesGInterface hermesg;
	private JLayeredPane panelClock;
	private JTextField rfrequency,rname;
	private JComboBox cbclockrouter,cbclockip;
	private JComboBox addaclock;
	private JButton ok;
	private int dimXNet,dimYNet;
	private double frequencia;
	private ArrayList<AvailableClock> clock_list = new ArrayList<AvailableClock>();
	
	/**
	* Create the GUI allowing configure the clock of router and IP.
	* @param project The NoC project.
	* @param x and y.
	*/
		
	public InterfaceNewClock(Project project,HermesGInterface g,ArrayList<AvailableClock> clock)
	{
		super("Enter a name and a value for the clock");
		this.project = project;
		hermesg = g;
		clock_list = clock;
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
		setSize(320,140);
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
		addNameRouter(x,y);
		y+=20;
		addFrequencyRouter(x,y);
		y+=20;
		addOk(x+80,y);
	}
	private void addNameRouter(int x,int y)
	{
		JLabel nameRouter = new JLabel("Clock Name ");
		nameRouter.setBounds(x+30,y+10,200,20);
		getContentPane().add(nameRouter);
		
		x += 160;
		rname = new JTextField("Clock0");
		rname.setHorizontalAlignment(JTextField.LEFT);
		rname.setBounds(x,y+10,105,20);
		rname.setToolTipText("Enter a new name for the clock");
		getContentPane().add(rname);

	}
	
	private void addFrequencyRouter(int x,int y)
	{
		JLabel routerfrequency = new JLabel("Clock Frequency");
		routerfrequency.setBounds(x+30,y+10,200,20);
		getContentPane().add(routerfrequency);
		
		x += 160;
		rfrequency = new JTextField("0");
		rfrequency.setHorizontalAlignment(JTextField.LEFT);
		rfrequency.setBounds(x,y+12,50,20);
		rfrequency.setToolTipText("Enter a new value for the frequency");
		getContentPane().add(rfrequency);

		x += 50;
		
		
		String[] unit = {"Mhz"};
		addaclock = new JComboBox(unit);
		addaclock.setBounds(x,y+12,55,20);
		addaclock.setToolTipText("Select a unit to the frequency informed");
		getContentPane().add(addaclock);
	}
	
	private void addOk(int x,int y)
	{
		ok= new JButton("Ok");
		ok.setBounds(x+40,y+25,60,25);
		ok.addActionListener(this);
		ok.setToolTipText("Press OK to register the new clock");
		getContentPane().add(ok);
	}
	/**
	 * Execute an action associated to the selected event
	*/
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == ok)
		{	
			try
			{
				
				/* Captura nome e frequência do clock da interface */
				String name = rname.getText().toLowerCase();
				frequencia = Double.parseDouble(rfrequency.getText());
				
				/* Condição java de truncamento do valor do clock em duas casas depois da virgula */
				/*
					BigDecimal bd = new BigDecimal(frequencia);  
					bd = bd.setScale(2,BigDecimal.ROUND_HALF_UP);  
					frequencia = bd.doubleValue();  
				*/
				
				/* Condições para cadastrar um novo nome de clock C1 C2 C3 C4 C5 devem ser atendidas. */				
				 
				/* C1 Verifica se o nome para o novo clock já esta cadastrado */
				int c1=0,c2=0,c3=0,c4=0,c5=0;
				for(int i=0;i<clock_list.size();i++)
				{
					if(clock_list.get(i).getLabel().toLowerCase().equals(name.toLowerCase()))
					{
						c1 = 1;
						break;	
					}						
				}
				
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
				
				/* C4 - Frequência do clock deve ser maior ou igual a 0.1 e menor ou igual a 5000 */
				if(frequencia >= 0.1 && frequencia <= 5000) {}
				else { c4=1; } 
				
				/* C5 As seguintes palavras reservadas não devem ser aceitas como nomes de clock */
				if(name.equals("abs") || name.equals("access") || name.equals("after") || name.equals("alias") || name.equals("all") || name.equals("and") || name.equals("array") || name.equals("assert") || name.equals("attribute") || name.equals("architecture") || name.equals("begin") || name.equals("block") || name.equals("body") || name.equals("buffer") || name.equals("bus") || name.equals("case") || name.equals("constant") || name.equals("component") || name.equals("configuration") || name.equals("downto") || name.equals("disconnect") || name.equals("else") || name.equals("elsif") || name.equals("end") || name.equals("entity") || name.equals("exit") || name.equals("file") || name.equals("for") || name.equals("function") || name.equals("group") || name.equals("generic") || name.equals("generate") || name.equals("guarded") || name.equals("if") || name.equals("impure") || name.equals("in") || name.equals("inertial") || name.equals("inout") || name.equals("is") || name.equals("label") || name.equals("loop") || name.equals("literal") || name.equals("linkage") || name.equals("library") || name.equals("mod") || name.equals("map") || name.equals("nand") || name.equals("new") || name.equals("") || name.equals("next") || name.equals("nor") || name.equals("not") || name.equals("null") || name.equals("of") || name.equals("on") || name.equals("open") || name.equals("or") || name.equals("others") || name.equals("out") || name.equals("port") || name.equals("pure") || name.equals("process") || name.equals("package") || name.equals("postponed") || name.equals("procedure") || name.equals("range") || name.equals("record") || name.equals("rem") || name.equals("ror") || name.equals("rol") || name.equals("return") || name.equals("reject") || name.equals("report") || name.equals("register") || name.equals("select") || name.equals("sla") || name.equals("sll") || name.equals("sra") || name.equals("srl") || name.equals("signal") || name.equals("subtype") || name.equals("severity") || name.equals("shared") || name.equals("then") || name.equals("to") || name.equals("type") || name.equals("transport") || name.equals("use") || name.equals("until") || name.equals("units") || name.equals("unaffected") || name.equals("variable") || name.equals("wait") || name.equals("when") || name.equals("while") || name.equals("with") || name.equals("xnor") || name.equals("xor")) { c5=1; }
				
				
				/* C1 - Nome informado para o clock já esta cadastrado */
				if(c1 == 1) JOptionPane.showMessageDialog(this,"Choose another name for clock, is already registered","Warning",JOptionPane.INFORMATION_MESSAGE);			
				
				/* C2 - Primeiro caracter do nome do clock deve ser uma letra */
				else if(c2 == 1) JOptionPane.showMessageDialog(this,"First character of clock name must be a letter","Warning",JOptionPane.INFORMATION_MESSAGE);			
				
				/* C3 - Nomes para clocks devem ser possuir um ponto ou uma virgula e serem formados por letras e números */
				else if(c3 == 1) 
					JOptionPane.showMessageDialog(this,"Clock names must be have one point or comma and be composed of letters and numbers","Warning",JOptionPane.INFORMATION_MESSAGE);			
				
				/* C4 - Frequência do clock deve ser maior ou igual a 0.1 e menor ou igual a 5000 */
				else if(c4 == 1)
					JOptionPane.showMessageDialog(this,"A valid value for the clock must be greater or equal than 0.1 and less or equal than 5000","Warning",JOptionPane.INFORMATION_MESSAGE);			
				
				/* C5 - Nome do clock é igual a uma palavra reservada da linguagem VHDL */
				else if(c5 == 1)	JOptionPane.showMessageDialog(this,"Choose another name for clock. Same names of a reserved word in VHDL are not accepted","Warning",JOptionPane.INFORMATION_MESSAGE);
				
				/* Se todas as condições para cadastrar um novo clock estiverem corretas, o clock deve ser cadastrado */
				if(c1 == 0 && c2 == 0 && c3 == 0 && c4 == 0 && c5 == 0)
				{
					AvailableClock v = new AvailableClock();
					v.setLabel(name); 
					v.setClock(frequencia);
					v.setUnit((String)addaclock.getSelectedItem());
					clock_list.add(v);
					hermesg.setAvaibleClocks(clock_list);
					super.dispose();
				}
				hermesg.getClockButton().setEnabled(true);
			}
			catch(NumberFormatException x)
			{
				JOptionPane.showMessageDialog(this,"Enter a valid value for the clock","Warning",JOptionPane.INFORMATION_MESSAGE);	
			}		
		}
	}
}
