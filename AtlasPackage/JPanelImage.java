package AtlasPackage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
* <i>JPanelImage</i> implements a JPanel with a image drawn.
* @author Aline Vieira de Mello
* @version
*/
public class JPanelImage extends JPanel implements MouseListener{

    private String imageName;
    private String webAddress;
  
	/**
	 * Creates a new JPanelImage.
	 * @param imageName The image name 
	 */
    public JPanelImage(String imageName){
		super();
		this.imageName = imageName;
	}

    /**
	 * Creates a new JPanelImage.
	 * @param imageName The image name 
	 * @param webAddress The web address showed when the image is clicked.
	 */
    public JPanelImage(String imageName, String webAddress){
		super();
		super.addMouseListener(this);
		this.imageName = imageName;
		this.webAddress = webAddress;
    }
    
	/**
	* Paints the image.
	* @param g The Graphics class
	*/
	public void paint(Graphics g){
		getToolkit();
		Image image = Toolkit.getDefaultToolkit().getImage(imageName);
		g.drawImage(image,0,0,this);
	}

	/**
	 * Overrides the update method to accelerate the repaint.  
	 * @param g The Graphics class
	 */
	public void update(Graphics g){
		paint(g);
	}

	/**
	* Show the web address when image is clicked twice.
	*/
	public void mouseClicked(MouseEvent e){
		if (e.getClickCount() == 2){
			Help.show(webAddress);
		}
	}

	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mousePressed(MouseEvent e){}
	public void mouseReleased(MouseEvent e){}
}