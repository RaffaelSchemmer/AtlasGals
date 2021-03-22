package AtlasPackage;

import java.io.*;
import javax.swing.*;

/**
 * <i>Help</i> class shows a help file in a web Browser.
 * @author Aline Vieira de Mello
 * @version
 */
public class Help{
	
    /**
     * shows a file in a web browser.
     * @param file
     */
    public static void show(File file){
		String browser = Default.getBrowserFile();
		try{
		    Runtime.getRuntime().exec(browser+" file:///"+file.getPath());
		}catch(Exception e){
		    JOptionPane.showMessageDialog(null,e.getMessage(),"Warning Message",JOptionPane.WARNING_MESSAGE);
		}
    }
    
    /**
     * show a web address in a web browser.
     * @param s The web address.
     */
    public static void show(String s){
		String browser = Default.getBrowserFile();
		try{
		    Runtime.getRuntime().exec(browser+" "+s);
		} catch(Exception e){
		    JOptionPane.showMessageDialog(null,e.getMessage(),"Warning Message",JOptionPane.WARNING_MESSAGE);
		}
    }
	
}