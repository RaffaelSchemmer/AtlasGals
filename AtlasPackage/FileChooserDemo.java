package AtlasPackage;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * <i>FileChooserDemo</i> implements a JFrame that allows to choose a file.
 * @author Aline Vieira Mello
 * @version
 */
public class FileChooserDemo extends JFrame implements WindowListener{
	
    /**
     * Class constructor.
     */
	public FileChooserDemo(){
        super("FileChooserDemo");
		addWindowListener(this);
		//Create the log first, because the action listeners
		//need to refer to it.
		final JTextArea log= new JTextArea(5,20);
		log.setMargin(new Insets(5,5,5,5));
		log.setEditable(false);
		new JScrollPane(log);
    }

    public void windowActivated(WindowEvent e) {}
    public void windowClosed(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}
    public void windowClosing(WindowEvent e){this.dispose();}
}