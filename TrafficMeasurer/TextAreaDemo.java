package TrafficMeasurer;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;

/**
 * This class shows a GUI with the content of informed file. 
 * @author Aline Vieira de Mello
 * @version
 */
public class TextAreaDemo extends JFrame {

    JTextArea m_resultArea;

	/**
	 * Constructor class.
	 * @param title The GUI title.
	 * @param file The file containing the text that will be showed. 
	 */
    public TextAreaDemo(String title,File file) {
		super();
		setTitle(title);
		Dimension resolucao=Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((resolucao.width-900)/2,(resolucao.height-700)/2);
		setResizable(false);
		addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){dispose();}});
		getContentPane().setLayout(new BorderLayout());
		addComponent();
		addText(file);
		setVisible(true);
    }

    /**
	 * Add components in the GUI.
	 */
	private void addComponent(){
		m_resultArea = new JTextArea(38, 80);
		m_resultArea.setEditable(false);
		JScrollPane scrollingArea = new JScrollPane(m_resultArea);
		scrollingArea.setBorder(BorderFactory.createEmptyBorder(10,5,10,5));
		getContentPane().add(scrollingArea, BorderLayout.CENTER);
		pack();
	}

	/**
	 * Add content of informed file in JTextArea.
	 * @param file The file which the content will be showed.
	 */
	private void addText(File file){
		String line;

   		try{
			FileInputStream fis=new FileInputStream(file);
			BufferedReader br=new BufferedReader(new InputStreamReader(fis));

			line=br.readLine();
			while(line!=null){
				m_resultArea.append(line+"\n");
				line=br.readLine();
			}
			br.close();
		}
        catch(Exception exc){
            JOptionPane.showMessageDialog(null,exc.getMessage(),"IO Error",JOptionPane.ERROR_MESSAGE);
		}
	}
}