package AtlasPackage;

import java.awt.*;   
import java.awt.event.*;   
import javax.swing.*;   
  
/**
 * <i>ProgressBarFrame</i> contains a text message, a JProgressBar and a timer to monitor the activity.
 * @author Aline Vieira de Mello
 * @version
 */
public  class ProgressBarFrame extends JFrame implements ActionListener{
	private javax.swing.Timer activityMonitor;   
	private JProgressBar progressBar;   
	private int max;

	/**
	 * Creates a new ProgressBarFrame containing a text, 
	 * a JProgressBar with the <i>max</i> size and
	 * a timer to monitor the activity.
	 * @param text The text message
	 * @param max The maximum size of JProgressBar.
	 */
	public ProgressBarFrame(String text, int max){  
		this.max=max;
		setTitle(text);
		setSize(328,77);   
		Dimension resolucao=Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((resolucao.width-328)/2,(resolucao.height-77)/2);
		setResizable(false);
		addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){ end();}} );   
		getContentPane().setLayout(null);
		addBar(10,10);
		addMonitor(500);
		setVisible(true);
	}   

	/**
	 * Add the JProgressBar in the coordinate (x,y) of JFrame.
	 * @param x
	 * @param y
	 */
	private void addBar(int x,int y){
		progressBar = new JProgressBar();   
		progressBar.setBounds(x,y,300,25);
		progressBar.setStringPainted(true);   
		progressBar.setMaximum(max);   
		getContentPane().add(progressBar);
	}
	
	/**
	 * Set up the timer action.
	 * @param time The time in milliseconds
	 */
	private void addMonitor(int time){
		// set up the timer action   
		activityMonitor = new javax.swing.Timer(time,this); 
		activityMonitor.start();
	}
	
	/**
	 * Set the current value to the JProgressBar.
	 * If the current value is equals to maximum of JProgressBar then this JProgressBarFrame is disposed.
	 * @param current
	 */
	public void setValue(int current){
	    // show progress   
	    progressBar.setValue(current);   
	    // check if task is completed   
	    if (current == max){
	    	end();
	    }   
	}

	/**
	 * Stop the activity monitor and close the JProgressBarFrame.
	 */
    public void end(){
    	if(activityMonitor!=null)
    		activityMonitor.stop();   
    	dispose();
    }

    public void actionPerformed(ActionEvent evt) { 
    	repaint();
    }   
}   
