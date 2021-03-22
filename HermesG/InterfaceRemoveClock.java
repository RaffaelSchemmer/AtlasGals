package HermesG;

import java.awt.event.*;
import javax.swing.*;

import java.io.*;
import java.awt.*;
import javax.swing.border.*;
import java.util.*;

import AtlasPackage.Project;
import AtlasPackage.AvailableClock;
import AtlasPackage.Clock;
/**
 * InterfaceClock implementa uma GUI para que o usuario remova um clock presente na NOC
 * @author Raffael Bottoli Schemmer/Ricardo Aquino Guazzelli
 * @version
 */

public class InterfaceRemoveClock extends JFrame implements ActionListener
{
	private Project project;
	private HermesGInterface hermesg;
	private ArrayList<AvailableClock> clock_list;
	private int dimXNet,dimYNet;
	private JComboBox view_clocks;
	private JButton remove;

	public InterfaceRemoveClock(Project project,HermesGInterface g,ArrayList<AvailableClock> clock)
	{
		super("Configuration Clock of Router and IP");
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
		setSize(320,120);
		setLocation((resolucao.width-260)/2,(resolucao.height-380)/2);
		setResizable(false);
		addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){dispose();}});
 	}

	private void addComponents()
	{
		int x = 10;
		int y = 10;
		addClockBox(x,y);
		y+=20;
		addRemove(x+80,y);
	}

	private void addClockBox(int x,int y)
	{
		JLabel selectClock = new JLabel("Select Clock: ");
		selectClock.setBounds(x+30,y+10,200,20);
		getContentPane().add(selectClock);
		
		x += 110;
		ArrayList<String> word = new ArrayList<String>();
		for(int i=0;i<clock_list.size();i++) word.add(clock_list.get(i).getAllAvailableValue());
		view_clocks = new JComboBox(word.toArray());
		view_clocks.setBounds(x,y+10,130,20);
		view_clocks.setToolTipText("List of available clocks to remove");
		getContentPane().add(view_clocks);

	}

	private void addRemove(int x,int y)
	{
		remove = new JButton("Remove");
		remove.setBounds(x+10,y+25,100,25);
		remove.addActionListener(this);
		remove.setToolTipText("Press OK to delete the selected clock");
		getContentPane().add(remove);
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if(e.getSource() == remove)
		{
			String clockLabel = (String) view_clocks.getSelectedItem();
			ArrayList<Clock> clocks = project.getNoC().getClock();
			/* Se o clock_default for selecionado para ser removido informa que por regra este não pode ser removido */
			if(clockLabel.equals(clock_list.get(0).getAllAvailableValue()))
			{
					JOptionPane.showMessageDialog(this,"Default clock cannot be removed","Warning",JOptionPane.INFORMATION_MESSAGE);
			}
			else
			{
				for(int i=0;i<clocks.size();i++) // Busca em todos os routers e ips pelo clock deletado e substitui pelo clock_default (Indice 0)
				{
					if(clockLabel.equals(clocks.get(i).getRouter()))
					{
						clocks.get(i).setRouter(clock_list.get(0).getAllAvailableValue());
					}
					if(clockLabel.equals(clocks.get(i).getIpOutput()))
					{
						clocks.get(i).setIpOutput(clock_list.get(0).getAllAvailableValue());
					}
					if(clockLabel.equals(clocks.get(i).getIpInput()))
					{
						clocks.get(i).setIpInput(clock_list.get(0).getAllAvailableValue());
					}
				}
				for(int i = 0; i<clock_list.size(); i++) 
				{
					if(clockLabel.equals(clock_list.get(i).getAllAvailableValue()))
					{
						project.getNoC().removeIndexClockList(i);
						hermesg.setAvaibleClocks(project.getNoC().getRefClockList());
					}
				}
				hermesg.getRemoveClockButton().setEnabled(true);
				super.dispose();
			}
		}
	}
}
