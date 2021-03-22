package Hefestus;

import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import AtlasPackage.Project;
import AtlasPackage.NoC;
import AtlasPackage.Scenery;
import AtlasPackage.Help;
import AtlasPackage.Default;
import AtlasPackage.Router;
import AtlasPackage.JPanel_Noc;

/**
 * This class creates the Power Evaluation GUI allowing evaluate a Hermes NoC with round-robin routing algorithm.
 * @author Aline Vieira de Mello
 * @version
 */
public class Power extends JFrame implements MouseListener,ActionListener{

	private Project project;
	private NoC noc;
	private JPanel_Noc jpanel_noc;
	private JMenuBar menubar;
    private JMenu mReport, mHelp, mFile;
    private JMenuItem mLink,mGlobal,documentation,about,mOpen;
	private JLayeredPane pbExternal;
	private JTextField nwText;	
	private JButton bRPCG,bNAPC,bNAPCD;
	private File fW;		
	private int nw;
	private Scenery scenery;
    private String sourcePath;

	private static int dimXCanvas = 400;
	private static int dimYCanvas = 400;
    
	/**
	 * Constructor class.
	 * @param project The project where the NoC will be generated.
	 */
	public Power(Project project){
		super("Power");
		this.project = project;
		noc = project.getNoC();
		scenery = new Scenery(noc.getNumRotX(),noc.getNumRotY(),dimXCanvas,dimYCanvas, noc.isSR4());

		sourcePath = Default.atlashome + File.separator + "Hefestus" + File.separator;
		nw = Integer.parseInt(project.getTimeWindow(),16);
		
		getContentPane().setLayout(null);		
		setSize(715,490);		
		Dimension resolucao=Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((resolucao.width-800)/2,(resolucao.height-456)/2);
		setResizable(false);
		setVisible(true);		
		
					
		addComponents();
		
		File list = new File(project.getPath()+File.separator + "Power" + File.separator + "Evaluation" + File.separator + "list.txt");
		
		if(list.exists())
		{		
			bNAPC.setEnabled(true);
			bNAPCD.setEnabled(true);
			
			if(noc.getType().equalsIgnoreCase("Hermes")){
				if (noc.getFlowControl().equalsIgnoreCase("Handshake"))			
					calculatePosPowerHandshake();
				else {
					if ( noc.getVirtualChannel() == 1 )
						calculatePosPowerCreditBased_1CV();
					else
						calculatePosPowerCreditBased();
				}
			}
			else
				JOptionPane.showMessageDialog(null,"Power Evaluation just with Hermes","Error",JOptionPane.ERROR_MESSAGE);		
		}
		else
		{			
			bNAPC.setEnabled(false);
			bNAPCD.setEnabled(false);
		}
 	}
	
	private void addComponents(){		
		int x=0;
		int y=0;
		addMenu();						
		addExternalPanePRE(x,y,300,130);
		addExternalPanePOS(x,y+131,300,290);
		JLabel labelPOS = new JLabel("Post-Simulation Analysis");
		labelPOS.setBounds(x+440,y,200,20);
		getContentPane().add(labelPOS);
		addNoCPane(x+300,y+21,dimXCanvas,dimYCanvas);
	}
	
	private void addMenu(){
		mFile = new JMenu("File");
		mOpen = new JMenuItem("Open Equations");
		mOpen.setEnabled(true);
		mOpen.addActionListener(this);
		mFile.add(mOpen);

		mReport = new JMenu("Reports");
		if(!project.isSimulate())
			mReport.setEnabled(false);
		mLink = new JMenuItem("NoC Average Power Consumption Report");
		mLink.setEnabled(true);
		mLink.addActionListener(this);
		mReport.add(mLink);
		
		mGlobal = new JMenuItem("Global Power Report");
		mGlobal.setEnabled(true);
		mGlobal.addActionListener(this);
		mReport.add(mGlobal);


		mHelp = new JMenu("Help");
		documentation = new JMenuItem("Documentation");
		documentation.addActionListener(this);
		mHelp.add(documentation);
		about = new JMenuItem("About Hefestus");
		about.addActionListener(this);
		mHelp.add(about);

		menubar = new JMenuBar();
		menubar.add(mFile);
		menubar.add(mReport);
		menubar.add(mHelp);

		setJMenuBar(menubar);
	}
		
	private void addExternalPanePRE(int x,int y,int dimx,int dimy){
		pbExternal=new JLayeredPane();
		pbExternal.setBounds(x,y,dimx,dimy);
		pbExternal.setBorder(BorderFactory.createTitledBorder(BorderFactory.createRaisedBevelBorder(),"Pre-Simulation Analisys",TitledBorder.CENTER,TitledBorder.TOP));
		pbExternal.setVisible(true);

		getContentPane().add(pbExternal);

		x=10;
		y=50;
		addButtonPRE(pbExternal,x,y,dimx-30,30);
	}
	
	private void addExternalPanePOS(int x,int y,int dimx,int dimy){
		pbExternal=new JLayeredPane();
		pbExternal.setBounds(x,y,dimx,dimy);
		pbExternal.setBorder(BorderFactory.createTitledBorder(BorderFactory.createRaisedBevelBorder(),"Post-Simulation Analisys",TitledBorder.CENTER,TitledBorder.TOP));
		pbExternal.setVisible(true);

		getContentPane().add(pbExternal);

		x=10;
		y=70;
		
		addButtonPOS(pbExternal,x,y,dimx-30,30);
	}
	
	private void addButtonPRE(JLayeredPane pane,int x,int y,int dimx,int dimy){
		// create button to show the pre-analisys power graphics
		bRPCG = new JButton("Routers Power Comsumption Analysis");	
		bRPCG.setBounds(x,y,dimx,dimy);
		bRPCG.addActionListener(this);
		bRPCG.setToolTipText("Show Graphic");		
		pane.add(bRPCG);
	}
		
	private void addButtonPOS(JLayeredPane pane,int x,int y,int dimx,int dimy){
		// create button to show the pre-analisys power graphics
		bNAPC = new JButton("NoC Average Power Consumption");
		bNAPC.setBounds(x,y,dimx,dimy);
		bNAPC.addActionListener(this);
		bNAPC.setToolTipText("Show Graphic");		
		pane.add(bNAPC);
		
		y+=80;
				
		bNAPCD = new JButton("NoC Power Distribution");	
		bNAPCD.setBounds(x,y,dimx,dimy);
		bNAPCD.addActionListener(this);
		bNAPCD.setToolTipText("Show Graphic of Network-on-Chip Average Power Consumption Distribution");		
		pane.add(bNAPCD);
					
		y+=60;
				
		addNWset(pane,x,y);
				
	}
	
	private void addNWset(JLayeredPane pane,int x,int y){
		// create button to show the pre-analisys power graphics
		JLabel labeltt1 = new JLabel("Time:");
		labeltt1.setBounds(x,y-5,40,40);
		pane.add(labeltt1);
				
		nwText = new JTextField((nw/20)+"");
		nwText.setBounds(x+50,y,50,25);				
		nwText.setEnabled(true);
		pane.add(nwText);
		
		JLabel labeltt2 = new JLabel("us");
		labeltt2.setBounds(x+100,y-5,40,40);
		pane.add(labeltt2);
	
	}		
		
	private void addNoCPane(int x,int y,int dimx,int dimy){
		jpanel_noc = new JPanel_Noc(x,y,dimx,dimy,"Hermes");
		jpanel_noc.addMouseListener(this);
		getContentPane().add(jpanel_noc);		
		jpanel_noc.setNoCDimension(noc.getNumRotX(),noc.getNumRotY());
	}
	
	private void showPrePowerGraphic(){
	    String nameFile = "";
	    
	    if ( noc.getFlowControl().equalsIgnoreCase("CreditBased") ){
			if ( noc.getVirtualChannel() == 1 ){
			    nameFile = sourcePath + "gnuplot" + File.separator + "gnuplot_noc_"+noc.getFlowControl()+File.separator + "1cv" + File.separator+noc.getFlowControl()+"_"+noc.getFlitSize()+"_"+noc.getBufferDepth()+".txt";
			}
			else if ( noc.getVirtualChannel() == 2 ){
			    nameFile = sourcePath + "gnuplot" + File.separator + "gnuplot_noc_"+noc.getFlowControl()+File.separator + "2cv" + File.separator + "credit_nocv_"+noc.getFlitSize()+"_"+noc.getBufferDepth()+".txt";
			}
			else if ( noc.getVirtualChannel() == 4 ){
			    nameFile = sourcePath + "gnuplot" + File.separator + "gnuplot_noc_"+noc.getFlowControl()+File.separator + "4cv" + File.separator + "credit_nocv_"+noc.getFlitSize()+"_"+noc.getBufferDepth()+".txt";
			}
	    }
	    else if ( noc.getFlowControl().equalsIgnoreCase("Handshake") ){
	    	nameFile = sourcePath + "gnuplot" + File.separator + "gnuplot_noc_handshake"+File.separator+"handshake_"+noc.getFlitSize()+"_"+noc.getBufferDepth()+".txt";
	    }
	    
	    Default.showGraph(nameFile);
	}
	
	private void showNAPCDGraphics(String file,int aux)	{		
		try{
		
			String script = new String(project.getPath()+File.separator + "Power" + File.separator + "Evaluation" + File.separator + "scriptNAPCD.txt");
			DataOutputStream script_output = new DataOutputStream(new FileOutputStream(script));
					
			script_output.writeBytes("unset title\n");
			script_output.writeBytes("unset label\n");
			script_output.writeBytes("set dgrid3d 45,45,45\n");
			script_output.writeBytes("set view 15,325\n");
			script_output.writeBytes("set xrange [0:"+((noc.getNumRotX()*2)+2)+"]\n");
			script_output.writeBytes("set yrange [0:"+((noc.getNumRotY()*2)+3)+"]\n");
			
			script_output.writeBytes("set hidden3d\n");	
			
			script_output.writeBytes("set title \"NoC Average Power consumption at window "+aux+"\"\n");
			script_output.writeBytes("set xlabel \"Position X\"\n");
			script_output.writeBytes("set ylabel \"Position Y\"\n");
			script_output.writeBytes("set zlabel \"Average Power Consumption Distribution(mW)\"\n");
			script_output.writeBytes("set pm3d\n");		
			
			script_output.writeBytes("splot '"+file+"' using 1:2:3 title \"Power Consumption\" with lines\n");
			script_output.writeBytes("pause -1 \"Press ENTER to continue\"");
			script_output.close();
	
		    Default.showGraph(script);
		}
		catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't open Evaluation 0","Input Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		catch(Exception e){
			JOptionPane.showMessageDialog(null,"Error in Evaluation 1","Input Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}
	
	private void showRouterGraphics(String file,String aux)	{		
		try{
			String script = new String(project.getPath()+File.separator + "Power" + File.separator + "Evaluation" + File.separator + "scriptR.txt");
			DataOutputStream script_output = new DataOutputStream(new FileOutputStream(script));
					
			script_output.writeBytes("unset title\n");
			script_output.writeBytes("unset label\n");
			script_output.writeBytes("set autoscale\n");
			script_output.writeBytes("set title \"NoC Average Power consumption at "+aux+"\"\n");
			script_output.writeBytes("set xlabel \"Time (us)\"\n");
			script_output.writeBytes("set ylabel \"Average Power Consumption Distribution(mW)\"\n");
			script_output.writeBytes("set autoscale\n");
			script_output.writeBytes("plot '"+file+"' using 1:2 title \"Power Consumption\" with lines\n");
			script_output.writeBytes("pause -1 \"Press ENTER to continue\"");
			script_output.close();
			
		    Default.showGraph(script);
		}
		catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't open Evaluation 0","Input Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		catch(Exception e){
			JOptionPane.showMessageDialog(null,"Error in Evaluation 1","Input Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}		
	}
	
	private void showReport(String path){
		JFrame frame = new JFrame(path.substring(path.lastIndexOf(File.separator)));
		JTextArea area = new JTextArea();
		area.setRows(22);
		area.setColumns(30);
		area.setEditable(false);
		StringBuffer buffer = new StringBuffer();
   		try{
            FileInputStream fis = new FileInputStream(path);
            InputStreamReader isr = new InputStreamReader(fis);
            Reader in = new BufferedReader(isr);
            int ch;
            while((ch = in.read()) > -1) {
				buffer.append((char)ch);
            }
            in.close();
            isr.close();
            fis.close();
            area.append(buffer.toString());
            JPanel p = new JPanel(new BorderLayout());
            p.add(new JScrollPane(area),BorderLayout.CENTER);
            frame.getContentPane().add(p);
            frame.setBounds(300,200,0,0);
            frame.pack();
            frame.setVisible(true);
		}
        catch(Exception ex){
            JOptionPane.showMessageDialog(null,ex.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void calculatePosPowerHandshake(){

		StringTokenizer st, coefs;
		String line, word;
		int rotx, roty, num, flag, tamflit;
		float window;
		float a_buf=0, b_buf=0, a_sc=0, b_sc=0;
		int nTokens = 0;
		try{

			FileInputStream fis=new FileInputStream(project.getPath()+File.separator + "Power" + File.separator + "Evaluation" + File.separator + "list.txt");
			BufferedReader br=new BufferedReader(new InputStreamReader(fis));

			FileInputStream eqs=new FileInputStream(sourcePath + "Equations" + File.separator + "Handshake" + File.separator + "noc_"+noc.getFlitSize()+"_"+noc.getBufferDepth()+"_eqs.txt");
			BufferedReader br2=new BufferedReader(new InputStreamReader(eqs));

			line=br2.readLine();

			//
			String msg;
			String janelas_str;
			String janelas;
			DataOutputStream janela_output;

			//

			num = 0;

			while(line!=null){
				flag = 0;
				coefs = new StringTokenizer(line, " ");
				nTokens = coefs.countTokens();
				if(nTokens!=0){
					word = coefs.nextToken();

					 while(flag == 0){
						if(word.equalsIgnoreCase("=")){
							coefs.nextToken();
							word = coefs.nextToken();
							if(num == 0){
								try {
									a_buf = Float.parseFloat( word );
									//JOptionPane.showMessageDialog(null, msg = String.valueOf( a_buf ),"msg1",JOptionPane.ERROR_MESSAGE);
								}
								catch( NumberFormatException nfe ) {
									System.out.println( nfe );
								}
							}
							else{
								try{
									a_sc = Float.parseFloat( word );
									//JOptionPane.showMessageDialog(null,msg = String.valueOf( a_sc ),"msg1",JOptionPane.ERROR_MESSAGE);
								}
								catch( NumberFormatException nfe ) {
									System.out.println( nfe );
								}
							}
						}
						if(word.equalsIgnoreCase("+")){
							word = coefs.nextToken();
							flag = 1;
							if(num == 0){
								try {
									b_buf = Float.parseFloat( word );
									//JOptionPane.showMessageDialog(null,msg = String.valueOf(b_buf),"msg1",JOptionPane.ERROR_MESSAGE);
								}
								catch( NumberFormatException nfe ) {
									System.out.println( nfe );
								}
							}
							else{
								try{
									b_sc = Float.parseFloat( word );
									//JOptionPane.showMessageDialog(null,msg = String.valueOf(b_sc),"msg1",JOptionPane.ERROR_MESSAGE);
								}
								catch( NumberFormatException nfe ) {
									System.out.println( nfe );
								}
							}
						}
						if(flag==0)
							word = coefs.nextToken();
					}
				}

				line=br2.readLine();

				num++;

			}

			rotx = noc.getNumRotX();
			roty = noc.getNumRotY();
			tamflit = noc.getFlitSize();
			//window = (float) conv.hexa_decimal(project.getTimeWindow());
			window = (float) Integer.parseInt(project.getTimeWindow(),16);
			
			float[][][] noc = new float[rotx][roty][5];
			double[][] power = new double[rotx][roty];
			double[][] instantPower = new double[rotx][roty];
			int flits;
			double power_noc;
			float taxa, media_taxa, power_sc, power_router, power_sum=0, simtime;
			String rate;
			int i=0, j=0, k=0, l=0, lines;


			String noc_time = new String(project.getPath() +File.separator + "Power" + File.separator + "Evaluation" + File.separator + "noc.dat");
			DataOutputStream noc_time_output = new DataOutputStream(new FileOutputStream(noc_time));



			String routers_time;
			Writer wr;


			for(i=0;i<rotx;i++){
				for(j=0;j<roty;j++){
					routers_time = project.getPath() + File.separator + "Power" + File.separator + "Evaluation" + File.separator + "router" + i + j + ".dat";
					new DataOutputStream(new FileOutputStream(routers_time));
				}
			}




			power_noc = 0;
			lines = 0;

			//Zerando a matriz que ira auxiliar no calculo
			for(i=0;i<rotx;i++){
				for(j=0;j<roty;j++){
					power[i][j] = 0;
				}
			}

			line = br.readLine();
			line = br.readLine();
			line = br.readLine();


			while(line!=null){
				st = new StringTokenizer(line, " ");
				nTokens = st.countTokens();
				if(nTokens!=0){
					st.nextToken();
					for(j=0;j<roty;j++){
						for(i=0;i<rotx;i++){

							if( ((i == 0)&&(j == 0)) || ((i == (rotx-1))&&(j == (roty-1))) ||
								((j == 0)&&(i == rotx-1))  || ((i == 0)&&(j == roty-1)) )
									k = 3;
							else if( ((i>0)&&(i<(rotx-1))&&((j==0)||(j==(roty-1)))) ||
									 ((j>0)&&(j<(roty-1))&&((i==0)||(i==(rotx-1))))	)
									 k = 4;
							else
										k = 5;

							l=0;
							while(l<k){
								rate = st.nextToken();
								flits = Integer.parseInt( rate );
								taxa = (flits*tamflit)/(0.02f*window);//In Mbps and clock period of 20ns

								noc[i][j][l] = taxa;
								l++;
							}

						}
					}

				}


				//FAZ O CALCULO
				for(j=0;j<roty;j++){
					for(i=0;i<rotx;i++){

							if( ((i == 0)&&(j == 0)) || ((i == (rotx-1))&&(j == (roty-1))) ||
								((j == 0)&&(i == rotx-1))  || ((i == 0)&&(j == roty-1)) ){

										media_taxa = (noc[i][j][0] + noc[i][j][1] + noc[i][j][2])/3;
										power_sc = (a_sc*media_taxa) + b_sc;
										power_router = power_sc + ((a_buf*noc[i][j][0]) + b_buf)+
										((a_buf*noc[i][j][1]) + b_buf) + ((a_buf*noc[i][j][2]) + b_buf);

							}
							else if( ((i>0)&&(i<(rotx-1))&&((j==0)||(j==(roty-1)))) ||
									 ((j>0)&&(j<(roty-1))&&((i==0)||(i==(rotx-1))))	){

								 		media_taxa = (noc[i][j][0] + noc[i][j][1] + noc[i][j][2] + noc[i][j][3])/4;
										power_sc = (a_sc*media_taxa) + b_sc;
										power_router = power_sc + ((a_buf*noc[i][j][0]) + b_buf) +
										((a_buf*noc[i][j][1]) + b_buf) + ((a_buf*noc[i][j][2]) + b_buf)
										+ ((a_buf*noc[i][j][3]) + b_buf);


								}
							else{

								media_taxa = (noc[i][j][0] + noc[i][j][1] + noc[i][j][2] + noc[i][j][3] + noc[i][j][4])/5;
								power_sc = (a_sc*media_taxa) + b_sc;
								power_router = power_sc + ((a_buf*noc[i][j][0]) + b_buf) + ((a_buf*noc[i][j][1]) + b_buf)
											   + ((a_buf*noc[i][j][2]) + b_buf) + ((a_buf*noc[i][j][3]) + b_buf) +
											   ((a_buf*noc[i][j][4]) + b_buf);
							}


							power_sum = power_sum + power_router;
							power[i][j] = power[i][j] + power_router;
							instantPower[i][j] = power_router;



					}
				}

				power_noc = power_noc + power_sum;
				simtime = (lines+1)*(0.02f*window);
				msg = String.valueOf(simtime);
				noc_time_output.writeBytes(msg + "\t\t" + power_sum + "\n");

				janelas_str = String.valueOf(lines+1);
				janelas = new String(project.getPath()+File.separator + "Power" + File.separator + "Evaluation" + File.separator + "janela" + janelas_str + ".dat");
				janela_output = new DataOutputStream(new FileOutputStream(janelas));


				for(i=0;i<rotx;i++){
					for(j=0;j<roty;j++){
						routers_time = project.getPath() + File.separator + "Power" + File.separator + "Evaluation" + File.separator + "router" + i + j + ".dat";
						wr = new FileWriter(routers_time, true);
						wr.write(msg + "\t\t" + instantPower[i][j] + "\n");
						wr.close();



						janela_output.writeBytes((i*3) + "\t\t" + (j*3) + "\t\t" + instantPower[i][j] + "\n");
						janela_output.writeBytes((i*3) + "\t\t" + ((j*3)+1) + "\t\t" + instantPower[i][j] + "\n");
						janela_output.writeBytes((i*3) + "\t\t" + ((j*3)+2) + "\t\t" + "0" + "\n");
						janela_output.writeBytes(((i*3)+1) + "\t\t" + (j*3) + "\t\t" + instantPower[i][j] + "\n");
						janela_output.writeBytes(((i*3)+1) + "\t\t" + ((j*3)+1) + "\t\t" + instantPower[i][j] + "\n");
						janela_output.writeBytes(((i*3)+1) + "\t\t" + ((j*3)+2) + "\t\t" + "0" + "\n");
						janela_output.writeBytes(((i*3)+2) + "\t\t" + ((j*3)+2) + "\t\t" + "0" + "\n");
						janela_output.writeBytes(((i*3)+2) + "\t\t" + ((j*3)+1) + "\t\t" + "0" + "\n");
						janela_output.writeBytes(((i*3)+2) + "\t\t" + (j*3) + "\t\t" + "0" + "\n");
					}
				}

				power_sum = 0;
				line = br.readLine();
				lines++;

				janela_output.close();
			}
		
		String media_sim = new String(project.getPath()+File.separator + "Power" + File.separator + "Evaluation" + File.separator + "AveragePowerComsuption.txt");
		DataOutputStream media_sim_output = new DataOutputStream(new FileOutputStream(media_sim));

		media_sim_output.writeBytes("NOC: \t\t\t" + (power_noc/(lines-1)) + "\n");

		for(j=0;j<roty;j++){
			for(i=0;i<rotx;i++){
				media_sim_output.writeBytes("Router"+i+j+": \t\t\t" + (power[i][j]/(lines-1)) + "\n");
			}
		}
		media_sim_output.close();
		noc_time_output.close();
		
		}
		catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't open Evaluation 0","Input Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		catch(Exception e){
			JOptionPane.showMessageDialog(null,"Error in Evaluation 1","Input Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}
	
	private void calculatePosPowerCreditBased_1CV(){

		StringTokenizer st, coefs;
		String line, word;
		int rotx, roty, num, flag, tamflit;
		float window;
		float a_buf=0, b_buf=0, a_sc=0, b_sc=0, a_xbar=0, b_xbar=0;
		int nTokens = 0;
		try{

			FileInputStream fis=new FileInputStream(project.getPath()+File.separator + "Power" + File.separator + "Evaluation" + File.separator + "list.txt");
			BufferedReader br=new BufferedReader(new InputStreamReader(fis));

			FileInputStream eqs=new FileInputStream(sourcePath + "Equations" + File.separator + "CreditBased" + File.separator + "1cv" + File.separator + "eqs_noc_"+noc.getFlitSize()+"_"+noc.getBufferDepth()+".txt");
			BufferedReader br2=new BufferedReader(new InputStreamReader(eqs));

			line=br2.readLine();

			//
			String msg;
			String janelas_str;
			String janelas;
			DataOutputStream janela_output;

			//

			num = 0;

			while(line!=null){
				flag = 0;
				coefs = new StringTokenizer(line, " ");
				nTokens = coefs.countTokens();
				if(nTokens!=0){
					word = coefs.nextToken();

					 while(flag == 0){
						if(word.equalsIgnoreCase("=")){
							coefs.nextToken();
							word = coefs.nextToken();
							if(num == 0){
								try {
									a_buf = Float.parseFloat( word );
									//JOptionPane.showMessageDialog(null, msg = String.valueOf( a_buf ),"msg1",JOptionPane.ERROR_MESSAGE);
								}
								catch( NumberFormatException nfe ) {
									System.out.println( nfe );
								}
							}
							else{
								if(num == 1){
									try{
										a_sc = Float.parseFloat( word );
										//JOptionPane.showMessageDialog(null,msg = String.valueOf( a_sc ),"msg1",JOptionPane.ERROR_MESSAGE);
									}
									catch( NumberFormatException nfe ) {
										System.out.println( nfe );
									}
								}
								else{
									try{
										a_xbar = Float.parseFloat( word );
										//JOptionPane.showMessageDialog(null,msg = String.valueOf( a_xbar ),"msg1",JOptionPane.ERROR_MESSAGE);
									}
									catch( NumberFormatException nfe ) {
										System.out.println( nfe );
									}

								}
							}
						}
						if(word.equalsIgnoreCase("+")){
							word = coefs.nextToken();
							flag = 1;
							if(num == 0){
								try {
									b_buf = Float.parseFloat( word );
									//JOptionPane.showMessageDialog(null,msg = String.valueOf(b_buf),"msg1",JOptionPane.ERROR_MESSAGE);
								}
								catch( NumberFormatException nfe ) {
									System.out.println( nfe );
								}
							}
							else{
								if(num == 1){
									try{
										b_sc = Float.parseFloat( word );
										//JOptionPane.showMessageDialog(null,msg = String.valueOf(b_sc),"msg1",JOptionPane.ERROR_MESSAGE);
									}
									catch( NumberFormatException nfe ) {
										System.out.println( nfe );
									}
								}
								else{
									try{
										b_xbar = Float.parseFloat( word );
										//JOptionPane.showMessageDialog(null,msg = String.valueOf(b_xbar),"msg1",JOptionPane.ERROR_MESSAGE);
									}
									catch( NumberFormatException nfe ) {
										System.out.println( nfe );
									}
								}
							}
						}
						if(flag==0)
							word = coefs.nextToken();
					}
				}

				line=br2.readLine();

				num++;

			}

			rotx = noc.getNumRotX();
			roty = noc.getNumRotY();
			tamflit = noc.getFlitSize();
			//window = (float) conv.hexa_decimal(project.getTimeWindow());
			window = (float) Integer.parseInt(project.getTimeWindow(),16);

			float[][][] noc = new float[rotx][roty][5];
			double[][] power = new double[rotx][roty];
			double[][] instantPower = new double[rotx][roty];
			int flits;
			double power_noc;
			float taxa, media_taxa, power_sc, power_xbar, power_router, power_sum=0, simtime;
			String rate;
			int i=0, j=0, k=0, l=0, lines;

			String noc_time = new String(project.getPath() +File.separator + "Power" + File.separator + "Evaluation" + File.separator + "noc.dat");
			DataOutputStream noc_time_output = new DataOutputStream(new FileOutputStream(noc_time));

			String routers_time;
			Writer wr;

			for(i=0;i<rotx;i++){
				for(j=0;j<roty;j++){
					routers_time = project.getPath() + File.separator + "Power" + File.separator + "Evaluation" + File.separator + "router" + i + j + ".dat";
					new DataOutputStream(new FileOutputStream(routers_time));
				}
			}

			power_noc = 0;
			lines = 0;

			//Zerando a matriz que ira auxiliar no calculo
			for(i=0;i<rotx;i++){
				for(j=0;j<roty;j++){
					power[i][j] = 0;
				}
			}

			line = br.readLine();
			line = br.readLine();
			line = br.readLine();

			while(line!=null){
				st = new StringTokenizer(line, " ");
				nTokens = st.countTokens();
				if(nTokens!=0){
					st.nextToken();
					for(j=0;j<roty;j++){
						for(i=0;i<rotx;i++){

							if( ((i == 0)&&(j == 0)) || ((i == (rotx-1))&&(j == (roty-1))) ||
								((j == 0)&&(i == rotx-1))  || ((i == 0)&&(j == roty-1)) )
								k = 3;
							else if( ((i>0)&&(i<(rotx-1))&&((j==0)||(j==(roty-1)))) ||
									 ((j>0)&&(j<(roty-1))&&((i==0)||(i==(rotx-1))))	)
								k = 4;
							else
								k = 5;

							l=0;
							
							while(l<k){
								rate = st.nextToken();
								flits = Integer.parseInt( rate );
								taxa = (flits*tamflit)/(0.02f*window);//In Mbps and clock period of 20ns

								noc[i][j][l] = taxa;
								l++;
							}
						}
					}
				}

				//FAZ O CALCULO
				for(j=0;j<roty;j++){
					for(i=0;i<rotx;i++){

						if( ((i == 0)&&(j == 0)) || ((i == (rotx-1))&&(j == (roty-1))) ||
							((j == 0)&&(i == rotx-1))  || ((i == 0)&&(j == roty-1)) ){

									media_taxa = (noc[i][j][0] + noc[i][j][1] + noc[i][j][2])/3;
									power_sc = (a_sc*media_taxa) + b_sc;
									power_xbar = (a_xbar*media_taxa) + b_xbar;
									power_router = power_sc + power_xbar + ((a_buf*noc[i][j][0]) + b_buf)+
									((a_buf*noc[i][j][1]) + b_buf) + ((a_buf*noc[i][j][2]) + b_buf);

						}
						else if( ((i>0)&&(i<(rotx-1))&&((j==0)||(j==(roty-1)))) ||
								 ((j>0)&&(j<(roty-1))&&((i==0)||(i==(rotx-1))))	){

							 		media_taxa = (noc[i][j][0] + noc[i][j][1] + noc[i][j][2] + noc[i][j][3])/4;
									power_sc = (a_sc*media_taxa) + b_sc;
									power_xbar = (a_xbar*media_taxa) + b_xbar;
									power_router = power_sc + power_xbar + ((a_buf*noc[i][j][0]) + b_buf) +
									((a_buf*noc[i][j][1]) + b_buf) + ((a_buf*noc[i][j][2]) + b_buf)
									+ ((a_buf*noc[i][j][3]) + b_buf);


							}
						else{

							media_taxa = (noc[i][j][0] + noc[i][j][1] + noc[i][j][2] + noc[i][j][3] + noc[i][j][4])/5;
							power_sc = (a_sc*media_taxa) + b_sc;
							power_xbar = (a_xbar*media_taxa) + b_xbar;
							power_router = power_sc + power_xbar +
										   ((a_buf*noc[i][j][0]) + b_buf) + ((a_buf*noc[i][j][1]) + b_buf)
										   + ((a_buf*noc[i][j][2]) + b_buf) + ((a_buf*noc[i][j][3]) + b_buf) +
										   ((a_buf*noc[i][j][4]) + b_buf);
						}

						power_sum = power_sum + power_router;
						power[i][j] = power[i][j] + power_router;
						instantPower[i][j] = power_router;
					}
				}

				power_noc = power_noc + power_sum;
				simtime = (lines+1)*(0.02f*window);
				msg = String.valueOf(simtime);
				noc_time_output.writeBytes(msg + "\t\t" + power_sum + "\n");

				janelas_str = String.valueOf(lines+1);
				janelas = new String(project.getPath()+File.separator + "Power" + File.separator + "Evaluation" + File.separator + "janela" + janelas_str + ".dat");
				janela_output = new DataOutputStream(new FileOutputStream(janelas));

				for(i=0;i<rotx;i++){
					for(j=0;j<roty;j++){
						routers_time = project.getPath() + File.separator + "Power" + File.separator + "Evaluation" + File.separator + "router" + i + j + ".dat";
						wr = new FileWriter(routers_time, true);
						wr.write(msg + "\t\t" + instantPower[i][j] + "\n");
						wr.close();

						janela_output.writeBytes((i*3) + "\t\t" + (j*3) + "\t\t" + instantPower[i][j] + "\n");
						janela_output.writeBytes((i*3) + "\t\t" + ((j*3)+1) + "\t\t" + instantPower[i][j] + "\n");
						janela_output.writeBytes((i*3) + "\t\t" + ((j*3)+2) + "\t\t" + "0" + "\n");
						janela_output.writeBytes(((i*3)+1) + "\t\t" + (j*3) + "\t\t" + instantPower[i][j] + "\n");
						janela_output.writeBytes(((i*3)+1) + "\t\t" + ((j*3)+1) + "\t\t" + instantPower[i][j] + "\n");
						janela_output.writeBytes(((i*3)+1) + "\t\t" + ((j*3)+2) + "\t\t" + "0" + "\n");
						janela_output.writeBytes(((i*3)+2) + "\t\t" + ((j*3)+2) + "\t\t" + "0" + "\n");
						janela_output.writeBytes(((i*3)+2) + "\t\t" + ((j*3)+1) + "\t\t" + "0" + "\n");
						janela_output.writeBytes(((i*3)+2) + "\t\t" + (j*3) + "\t\t" + "0" + "\n");
					}
				}

				power_sum = 0;
				line = br.readLine();
				lines++;

				janela_output.close();
			}

			String media_sim = new String(project.getPath()+File.separator + "Power" + File.separator + "Evaluation" + File.separator + "AveragePowerComsuption.txt");
			DataOutputStream media_sim_output = new DataOutputStream(new FileOutputStream(media_sim));
	
			media_sim_output.writeBytes("NOC: \t\t\t" + (power_noc/(lines-1)) + "\n");
	
			for(j=0;j<roty;j++){
				for(i=0;i<rotx;i++){
					media_sim_output.writeBytes("Router"+i+j+": \t\t\t" + (power[i][j]/(lines-1)) + "\n");
				}
			}
	
			media_sim_output.close();
			noc_time_output.close();

		}
		catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't open Evaluation 0","Input Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		catch(Exception e){
			JOptionPane.showMessageDialog(null,"Error in Evaluation 1","Input Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}
	
	private void calculatePosPowerCreditBased(){

		StringTokenizer st, coefs;
		String line, word;
		int rotx, roty, num, flag, tamflit;
		float window;
		float a_buf=0, b_buf=0, a_sc=0, b_sc=0, a_xbar=0, b_xbar=0;
		int nTokens = 0, cv;
		try{

			FileInputStream fis=new FileInputStream(project.getPath()+File.separator + "Power" + File.separator + "Evaluation" + File.separator + "list.txt");
			BufferedReader br=new BufferedReader(new InputStreamReader(fis));

			cv = noc.getVirtualChannel();

			FileInputStream eqs=new FileInputStream(sourcePath + "Equations" + File.separator + "CreditBased" + File.separator+cv+"cv" + File.separator + "eqs_noc_"+noc.getFlitSize()+"_"+noc.getBufferDepth()+".txt");
			BufferedReader br2=new BufferedReader(new InputStreamReader(eqs));

			line=br2.readLine();

			//
			String msg;
			String janelas_str;
			String janelas;
			DataOutputStream janela_output;
			//

			num = 0;

			while(line!=null){
				flag = 0;
				coefs = new StringTokenizer(line, " ");
				nTokens = coefs.countTokens();
				if(nTokens!=0){
					word = coefs.nextToken();

					 while(flag == 0){
						if(word.equalsIgnoreCase("=")){
							coefs.nextToken();
							word = coefs.nextToken();
							if(num == 0){
								try {
									a_buf = Float.parseFloat( word );
									//JOptionPane.showMessageDialog(null, msg = String.valueOf( a_buf ),"msg1",JOptionPane.ERROR_MESSAGE);
								}
								catch( NumberFormatException nfe ) {
									System.out.println( nfe );
								}
							}
							else{
								if(num == 1){
									try{
										a_sc = Float.parseFloat( word );
										//JOptionPane.showMessageDialog(null,msg = String.valueOf( a_sc ),"msg1",JOptionPane.ERROR_MESSAGE);
									}
									catch( NumberFormatException nfe ) {
										System.out.println( nfe );
									}
								}
								else{
									try{
										a_xbar = Float.parseFloat( word );
										//JOptionPane.showMessageDialog(null,msg = String.valueOf( a_xbar ),"msg1",JOptionPane.ERROR_MESSAGE);
									}
									catch( NumberFormatException nfe ) {
										System.out.println( nfe );
									}

								}
							}
						}
						if(word.equalsIgnoreCase("+")){
							word = coefs.nextToken();
							flag = 1;
							if(num == 0){
								try {
									b_buf = Float.parseFloat( word );
									//JOptionPane.showMessageDialog(null,msg = String.valueOf(b_buf),"msg1",JOptionPane.ERROR_MESSAGE);
								}
								catch( NumberFormatException nfe ) {
									System.out.println( nfe );
								}
							}
							else{
								if(num == 1){
									try{
										b_sc = Float.parseFloat( word );
										//JOptionPane.showMessageDialog(null,msg = String.valueOf(b_sc),"msg1",JOptionPane.ERROR_MESSAGE);
									}
									catch( NumberFormatException nfe ) {
										System.out.println( nfe );
									}
								}
								else{
									try{
										b_xbar = Float.parseFloat( word );
										//JOptionPane.showMessageDialog(null,msg = String.valueOf(b_xbar),"msg1",JOptionPane.ERROR_MESSAGE);
									}
									catch( NumberFormatException nfe ) {
										System.out.println( nfe );
									}
								}
							}
						}
						if(flag==0)
							word = coefs.nextToken();
					}
				}

				line=br2.readLine();

				num++;

			}

			rotx = noc.getNumRotX();
			roty = noc.getNumRotY();
			tamflit = noc.getFlitSize();
			//window = (float) conv.hexa_decimal(project.getTimeWindow());
			window = (float) Integer.parseInt(project.getTimeWindow(),16);

			float[][][] noc = new float[rotx][roty][5];
			double[][] power = new double[rotx][roty];
			double[][] instantPower = new double[rotx][roty];
			int flits;
			double power_noc;
			float taxa, media_taxa, power_sc, power_xbar, power_router, power_sum=0, simtime;
			String rate;
			int i=0, j=0, k=0, l=0, lines;


			String noc_time = new String(project.getPath() +File.separator + "Power" + File.separator + "Evaluation" + File.separator + "noc.dat");
			DataOutputStream noc_time_output = new DataOutputStream(new FileOutputStream(noc_time));

			String routers_time;
			Writer wr;


			for(i=0;i<rotx;i++){
				for(j=0;j<roty;j++){
					routers_time = project.getPath() + File.separator + "Power" + File.separator + "Evaluation" + File.separator + "router" + i + j + ".dat";
					new DataOutputStream(new FileOutputStream(routers_time));
				}
			}




			power_noc = 0;
			lines = 0;

			//Zerando a matriz que ira auxiliar no calculo
			for(i=0;i<rotx;i++){
				for(j=0;j<roty;j++){
					power[i][j] = 0;
				}
			}

			line = br.readLine();
			line = br.readLine();
			line = br.readLine();


			while(line!=null){
				st = new StringTokenizer(line, " ");
				nTokens = st.countTokens();
				if(nTokens!=0){
					st.nextToken();
					for(j=0;j<roty;j++){
						for(i=0;i<rotx;i++){

							if( ((i == 0)&&(j == 0)) || ((i == (rotx-1))&&(j == (roty-1))) ||
								((j == 0)&&(i == rotx-1))  || ((i == 0)&&(j == roty-1)) )
									k = 3;
							else if( ((i>0)&&(i<(rotx-1))&&((j==0)||(j==(roty-1)))) ||
									 ((j>0)&&(j<(roty-1))&&((i==0)||(i==(rotx-1))))	)
									 k = 4;
							else
										k = 5;

							l=0;
							while(l<k){
								rate = st.nextToken();
								flits = Integer.parseInt( rate );
								taxa = (flits*tamflit)/(0.02f*window);//In Mbps and clock period of 20ns

								noc[i][j][l] = taxa;
								l++;
							}

						}
					}

				}

				//FAZ O CALCULO
				for(j=0;j<roty;j++){
					for(i=0;i<rotx;i++){

							if( ((i == 0)&&(j == 0)) || ((i == (rotx-1))&&(j == (roty-1))) ||
								((j == 0)&&(i == rotx-1))  || ((i == 0)&&(j == roty-1)) ){

										media_taxa = (noc[i][j][0] + noc[i][j][1] + noc[i][j][2])/3;
										power_sc = (a_sc*media_taxa) + b_sc;
										power_xbar = ((a_xbar*media_taxa) + b_xbar)*5;
										power_router = power_sc + power_xbar + ((a_buf*noc[i][j][0]) + b_buf)+
										((a_buf*noc[i][j][1]) + b_buf) + ((a_buf*noc[i][j][2]) + b_buf);

							}
							else if( ((i>0)&&(i<(rotx-1))&&((j==0)||(j==(roty-1)))) ||
									 ((j>0)&&(j<(roty-1))&&((i==0)||(i==(rotx-1))))	){

								 		media_taxa = (noc[i][j][0] + noc[i][j][1] + noc[i][j][2] + noc[i][j][3])/4;
										power_sc = (a_sc*media_taxa) + b_sc;
										power_xbar = ((a_xbar*media_taxa) + b_xbar)*5;
										power_router = power_sc + power_xbar + ((a_buf*noc[i][j][0]) + b_buf) +
										((a_buf*noc[i][j][1]) + b_buf) + ((a_buf*noc[i][j][2]) + b_buf)
										+ ((a_buf*noc[i][j][3]) + b_buf);


								}
							else{

								media_taxa = (noc[i][j][0] + noc[i][j][1] + noc[i][j][2] + noc[i][j][3] + noc[i][j][4])/5;
								power_sc = (a_sc*media_taxa) + b_sc;
								power_xbar = ((a_xbar*media_taxa) + b_xbar)*5;
								power_router = power_sc + power_xbar +
											   ((a_buf*noc[i][j][0]) + b_buf) + ((a_buf*noc[i][j][1]) + b_buf)
											   + ((a_buf*noc[i][j][2]) + b_buf) + ((a_buf*noc[i][j][3]) + b_buf) +
											   ((a_buf*noc[i][j][4]) + b_buf);
							}


							power_sum = power_sum + power_router;
							power[i][j] = power[i][j] + power_router;
							instantPower[i][j] = power_router;



					}
				}

				power_noc = power_noc + power_sum;
				simtime = (lines+1)*(0.02f*window);
				msg = String.valueOf(simtime);
				noc_time_output.writeBytes(msg + "\t\t" + power_sum + "\n");

				janelas_str = String.valueOf(lines+1);
				janelas = new String(project.getPath()+File.separator + "Power" + File.separator + "Evaluation" + File.separator + "janela" + janelas_str + ".dat");
				janela_output = new DataOutputStream(new FileOutputStream(janelas));

				for(i=0;i<rotx;i++){
					for(j=0;j<roty;j++){
						routers_time = project.getPath() + File.separator + "Power" + File.separator + "Evaluation" + File.separator + "router" + i + j + ".dat";
						wr = new FileWriter(routers_time, true);
						wr.write(msg + "\t\t" + instantPower[i][j] + "\n");
						wr.close();

						janela_output.writeBytes((i*3) + "\t\t" + (j*3) + "\t\t" + instantPower[i][j] + "\n");
						janela_output.writeBytes((i*3) + "\t\t" + ((j*3)+1) + "\t\t" + instantPower[i][j] + "\n");
						janela_output.writeBytes((i*3) + "\t\t" + ((j*3)+2) + "\t\t" + "0" + "\n");
						janela_output.writeBytes(((i*3)+1) + "\t\t" + (j*3) + "\t\t" + instantPower[i][j] + "\n");
						janela_output.writeBytes(((i*3)+1) + "\t\t" + ((j*3)+1) + "\t\t" + instantPower[i][j] + "\n");
						janela_output.writeBytes(((i*3)+1) + "\t\t" + ((j*3)+2) + "\t\t" + "0" + "\n");
						janela_output.writeBytes(((i*3)+2) + "\t\t" + ((j*3)+2) + "\t\t" + "0" + "\n");
						janela_output.writeBytes(((i*3)+2) + "\t\t" + ((j*3)+1) + "\t\t" + "0" + "\n");
						janela_output.writeBytes(((i*3)+2) + "\t\t" + (j*3) + "\t\t" + "0" + "\n");
					}
				}

				power_sum = 0;
				line = br.readLine();
				lines++;

				janela_output.close();
			}

			String media_sim = new String(project.getPath()+File.separator + "Power" + File.separator + "Evaluation" + File.separator + "AveragePowerComsuption.txt");
			DataOutputStream media_sim_output = new DataOutputStream(new FileOutputStream(media_sim));
	
			media_sim_output.writeBytes("NOC: \t\t\t" + (power_noc/(lines-1)) + "\n");
	
			for(j=0;j<roty;j++){
				for(i=0;i<rotx;i++){
					media_sim_output.writeBytes("Router"+i+j+": \t\t\t" + (power[i][j]/(lines-1)) + "\n");
				}
			}
	
			media_sim_output.close();
			noc_time_output.close();

		}
		catch(FileNotFoundException f){
			JOptionPane.showMessageDialog(null,"Can't open Evaluation 0","Input Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
		catch(Exception e){
			JOptionPane.showMessageDialog(null,"Error in Evaluation 1","Input Error", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}

	}
	
	/**
	 * Return the router address.
	 * @param x The position in X-dimension of NoC.
	 * @param y The position in Y-dimension of NoC.
	 * @return The router address.
	 */
	public String getRouterOnXY(int x,int y){
		Router ip;
		ip = scenery.getClickedRouter(x,y);
		if (ip!=null)
			return ("router"+ip.getAddress());
		return "nada";
	}
	
	/**
	 * Show router graph.
	 * @param x The router position in X-dimension of NoC.
	 * @param y The router position in Y-dimension of NoC.
	 */
	public void showRouterGraphic(int x, int y)	{			
			String aux = getRouterOnXY(x,y);
			if(aux!="nada"){			
				fW = new File(project.getPath()+File.separator + "Power" + File.separator + "Evaluation" + File.separator+aux+".dat");				
				if(fW.exists())					
					showRouterGraphics((project.getPath()+File.separator + "Power" + File.separator + "Evaluation" + File.separator+aux+".dat"),aux);							
			}					
	}
		
	public void actionPerformed(ActionEvent e){
		int a=0;
		if(e.getSource() == bRPCG){
			showPrePowerGraphic();
		}
		if(e.getSource() == bNAPC){
			Default.showGraph(project.getPath()+File.separator + "Power" + File.separator + "script_noc.txt");										
		}
		if(e.getSource() == bNAPCD){
			if(noc.getType().equalsIgnoreCase(NoC.HERMES)){				
				String i = nwText.getText();
				float tempo = Float.parseFloat(i);
				float timePerWindow = ((nw)*20)/1000;
				int janela = (int)((tempo/timePerWindow));
				float janelaY = ((tempo/timePerWindow));
				
				if((janelaY > ((float)janela)) || (janela == 0))
					janela++; 
				
				fW = new File(project.getPath()+File.separator + "Power" + File.separator + "Evaluation" + File.separator + "janela"+janela+".dat");				
				if(fW.exists())
				    showNAPCDGraphics((project.getPath()+File.separator + "Power" + File.separator + "Evaluation" + File.separator + "janela"+janela+".dat"),janela);
				else
					JOptionPane.showMessageDialog(null,"Time out of range.","Error",JOptionPane.ERROR_MESSAGE);
			}
			else
				JOptionPane.showMessageDialog(null,"Power Evaluation just with Hermes","Error",JOptionPane.ERROR_MESSAGE);
		
		}		
		if(e.getSource() == mOpen){
			if (noc.getFlowControl().equalsIgnoreCase("Handshake"))			
				showReport(sourcePath + "Equations" + File.separator+noc.getFlowControl()+File.separator + "noc_"+noc.getFlitSize()+"_"+noc.getBufferDepth()+"_eqs.txt");
			else {
				if ( noc.getVirtualChannel() == 1 )
					showReport(sourcePath + "Equations" + File.separator+noc.getFlowControl()+File.separator + "1cv" + File.separator + "eqs_noc_"+noc.getFlitSize()+"_"+noc.getBufferDepth()+".txt");
				else if ( noc.getVirtualChannel() == 2 )
						showReport(sourcePath + "Equations" + File.separator+noc.getFlowControl()+File.separator + "2cv" + File.separator + "eqs_noc_"+noc.getFlitSize()+"_"+noc.getBufferDepth()+".txt");
					else
						showReport(sourcePath + "Equations" + File.separator+noc.getFlowControl()+File.separator + "4cv" + File.separator + "eqs_noc_"+noc.getFlitSize()+"_"+noc.getBufferDepth()+".txt");
			}			
		}	
		else if(e.getSource() == mGlobal){
     		showReport(project.getPath()+File.separator + "Power" + File.separator + "Evaluation" + File.separator + "AveragePowerComsuption.txt");
		}
		else if(e.getSource() == mLink){
			showReport(project.getPath()+File.separator + "Power" + File.separator + "Evaluation" + File.separator + "noc.dat");
		}		
		else if(e.getSource() == documentation)
		    Help.show("https://corfu.pucrs.br/redmine/projects/atlas/wiki/");
		else if(e.getSource() == about)
			JOptionPane.showMessageDialog(this,"POWER - Hefestus module          12.05.2008\nDeveloped by:\n        Cezar Rodolfo Wedig Reinbrecht\n           Thiago Raupp da Rosa\n           Guilherme Montez Guindani","VERSION 1.0",JOptionPane.INFORMATION_MESSAGE);
		else if(e.getSource() == mOpen){
			this.dispose();
			a++;
		}
    }
	
	/**
	 * Show the router power analysis when click on Router 
	 */
	public void mouseClicked(MouseEvent e){		
		if(e.getSource()==jpanel_noc){
			int x = e.getX();
			int y = e.getY();
			showRouterGraphic(x,y);
		}
	}		
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){}
	public void mousePressed(MouseEvent e){}
	public void mouseReleased(MouseEvent e){}	
}
