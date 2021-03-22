import java.awt.Font;
import java.util.Enumeration;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;

/**
 * Launch the Atlas environment.
 * @author Aline Vieira de Mello
 * @version 
 */
public class Atlas{

	/**
	 * Change the font of all components.
	 * @param f
	 */
    public static void setUIFont(FontUIResource f) {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                FontUIResource orig = (FontUIResource) value;
                Font font = new Font(f.getFontName(), orig.getStyle(), f.getSize());
                UIManager.put(key, new FontUIResource(font));
            }
        }
    }

    /**
     * Launch the ATLAS environment.
     * @param args
     */
    public static void main(String[] args){
    	// TODO Auto-generated method stub
    	JFrame.setDefaultLookAndFeelDecorated(true);
    	JDialog.setDefaultLookAndFeelDecorated(true);

    	setUIFont(new FontUIResource(new Font("Arial", 0, 10)));

    	new InterfacePrincipal();
    }
}
