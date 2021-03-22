package AtlasPackage;

import java.awt.*;
import java.util.*;
import javax.swing.JPanel;

/**
* <i>JPanel_Noc</i> is a JPanel that contains the NoC topology drawn.
* @author Aline Vieira de Mello
* @version
*/
public class JPanel_Noc extends JPanel{
	/** The Project contains all information about the NoC that will be drawn in the JPanel_Noc.*/ 
	private Project project;
	/** The Vector contains all routers of NoC */
	private Vector<Router> routerVector;
	/** This Vector contains all routers in a path.*/ 
	private Vector<Router> pathVector;
	/** The <i>width</i> of JPanel_Noc.*/
	private int dimx;
	/** The <i>height</i> of JPanel_Noc.*/
	private int dimy;
	/** The number of routers in X-dimension of NoC.*/
	private int maxX;
	/** The number of routers in Y-dimension of NoC.*/
	private int maxY;
	/** The NoC type. It defines the NoC topology.*/
	private String type="";
	/** If true the path between the source router and target router will be marked.*/
	private boolean mark=false;

	/**
	 * Creates a new JPanel_Noc with the location of the top-left corner specified by x and y, and the size specified by width and height.
	 * @param x The x-coordinate of this component.
	 * @param y The y-coordinate of this component.
	 * @param width The <i>width</i> of this component.
	 * @param height The <i>height</i> of this component.
	 */
	public JPanel_Noc(int x,int y,int width, int height){
	  super();
	  project = null;
	  this.dimx=width;
	  this.dimy=height;
	  setBounds(x,y,width,height);
	}

	/**
	 * Creates a new JPanel_Noc with the location of the top-left corner specified by x and y, and the size specified by width and height.
	 * @param x The x-coordinate of this component.
	 * @param y The y-coordinate of this component.
	 * @param width The <i>width</i> of this component.
	 * @param height The <i>height</i> of this component.
	 * @param nocType The <i>nocType</i> defines the NoC topology that will be drawn in JPanel_Noc. 
	 */
	public JPanel_Noc(int x,int y,int width, int height,String nocType){
	  super();
	  project = null;
	  this.dimx=width;
	  this.dimy=height;
	  this.type=nocType;
	  setBounds(x,y,width,height);
	}

	/**
	 * Creates a new JPanel_Noc with the location of the top-left corner specified by x and y, and the size specified by width and height.
	 * @param x The x-coordinate of this component.
	 * @param y The y-coordinate of this component.
	 * @param width The <i>width</i> of this component.
	 * @param height The <i>height</i> of this component.
	 * @param project The NoC <i>project</i>. 
	 */
	public JPanel_Noc(int x,int y,int width, int height,Project project){
	  super();
	  this.project = project;
	  this.maxX = project.getNoC().getNumRotX();
	  this.maxY = project.getNoC().getNumRotY();
	  this.type=project.getNoC().getType();
	  this.dimx=width;
	  this.dimy=height;
	  setBounds(x,y,width,height);
	  addRouters();
	}

	/**
	 * Creates a new JPanel_Noc with the location of the top-left corner specified by x and y, and the size specified by width and height.
	 * @param x The x-coordinate of this component.
	 * @param y The y-coordinate of this component.
	 * @param width The <i>width</i> of this component.
	 * @param height The <i>height</i> of this component.
	 * @param project The NoC <i>project</i>. 
	 * @param scenery The vector of routers.
	 */
	public JPanel_Noc(int x,int y,int width, int height,Project project, Scenery scenery){
	  super();
	  this.project = project;
	  this.maxX = project.getNoC().getNumRotX();
	  this.maxY = project.getNoC().getNumRotY();
	  this.type=project.getNoC().getType();
	  this.dimx=width;
	  this.dimy=height;
	  routerVector = scenery;
	  setBounds(x,y,width,height);
	}
	
	/**
	 * Sets the new NoC dimensions and repaint the JPanel_Noc.  
	 * @param nRoutersX The number of routers in X-dimension of NoC. 
	 * @param nRoutersY The number of routers in Y-dimension of NoC.
	 */
	public void setNoCDimension(int nRoutersX,int nRoutersY){
		int spaceXRot,spaceYRot,routerXDiv,routerYDiv,routerXInitial,routerYInitial;
		int x1,x2,y1,y2;
		maxX = nRoutersX;
		maxY = nRoutersY;
		routerVector=new Vector<Router>(nRoutersX*nRoutersY);
		//divide the X-dimension of JPanel to define the X-dimension of each router
		spaceXRot=dimx/maxX;
		//divide the Y-dimension of JPanel to define the Y-dimension of each router
		spaceYRot=dimy/maxY;
		//divide the X-dimension of router by four: (1)west wire (2 and 3) router (4)east wire
		routerXDiv=spaceXRot/4;
		//divide the Y-dimension of router by four: (1)north wire (2 and 3) router (4)south wire
		routerYDiv=spaceYRot/4;
		for(int i=0;i<maxX;i++)
		{
			int x = 0;
			for(int j=maxY-1;j>=0;j--)
			{
				
				routerXInitial = i * spaceXRot;
				routerYInitial = x * spaceYRot;
				x1 = routerXInitial + routerXDiv;
				y1 = routerYInitial + routerYDiv;
				x2 = x1 + 2 * routerXDiv;
				y2 = y1 + 2 * routerYDiv;
								
				routerVector.add(new Router(i,j,x1,y1,x2,y2));
				x++;
			}
		}
		repaint();
	}

	/**
	 * If <i>mark</i> is true marks the path among all routers in Vector.
	 * @param pathVector The Vector of routers contained in the marked path.
	 * @param mark Determines if the path will be mark or not.   
	 */
	public void markPath(Vector<Router> pathVector,boolean mark){
		this.pathVector=pathVector;
		this.mark=mark;
	}

	/**
	* Overrides of update method to acelerate the repaint.
	* @param g The Graphics class
	*/
	public void update(Graphics g){	paint(g);}

	/**
	 * Draws the NoC topology in JPanel_Noc.
	 * The NoC topology depends on the type of NoC used in Project.
	 * For instance: The Hermes NoC has 2D-Mesh topology.
	 * @param g The Graphics class
	 */
	public void paint(Graphics g){
		g.setColor(new Color(180,180,180));
		g.fillRect(0, 0, dimx, dimy);

		if(type.equalsIgnoreCase("HermesTU"))
			draw1DTorus(g);
		else if(type.equalsIgnoreCase("Mercury") || type.equalsIgnoreCase("HermesTB"))
			draw2DTorus(g);
		else
			draw2DMesh(g);
	}

	/**
	 * Draws in JPanel_Noc a NoC with 2D-Mesh topology.
	 * @param g The Graphics class
	 */
	private void draw2DMesh(Graphics g){
		Router ipAtual;
		String enderecoNodo;
		int routerXSpace,routerYSpace,routerXDiv,routerYDiv,routerXInitial=0,routerYInitial=0;
		int x1,y1;
		//divide the X-dimension of JPanel to define the X-dimension of each router
		routerXSpace=dimx/maxX;
		//divide the Y-dimension of JPanel to define the Y-dimension of each router
		routerYSpace=dimy/maxY;
		//divide the X-dimension of router by four: (1)west wire (2 and 3) router (4)east wire
		routerXDiv=routerXSpace/4;
		//divide the Y-dimension of router by four: (1)north wire (2 and 3) router (4)south wire
		routerYDiv=routerYSpace/4;
		for(int i=0;i<maxX;i++){
			for(int j=0;j<maxY;j++){
				enderecoNodo = ""+i+(maxY-1-j);
				routerXInitial = i * routerXSpace;
				routerYInitial = j * routerYSpace;
				x1 = routerXInitial + routerXDiv;
				y1 = routerYInitial + routerYDiv;

////////////////////////////////////////////////////////////////////////////////////////
//draws the router
////////////////////////////////////////////////////////////////////////////////////////
				//sets the router color, it depends on the Router connected to router
				if(project!=null){
					//get router
					ipAtual = project.getScenery().getRouter(i,(maxY-1-j));
					//verify the Router type connected to the router
					if(ipAtual.getCore().equalsIgnoreCase("Serial"))
						g.setColor(new Color(0,150,0));
					else if(ipAtual.getCore().equalsIgnoreCase("Tester"))
						g.setColor(Color.blue);
					else
						g.setColor(Color.black);
				}
				else
					g.setColor(Color.black);

				//draws the router
				g.fillRect(x1,y1,2*routerXDiv,2*routerYDiv);

////////////////////////////////////////////////////////////////////////////////////////
//draws the wires
////////////////////////////////////////////////////////////////////////////////////////
				
				g.setColor(Color.black);
				
				//east wire
				if(i!=(maxX-1))
				{
					drawArrow(g,routerXInitial+(5*routerXDiv),routerYInitial+(2*routerYDiv),routerXInitial+(3*routerXDiv),routerYInitial+(2*routerYDiv));
					drawArrow(g,routerXInitial+(3*routerXDiv),routerYInitial+(2*routerYDiv),routerXInitial+(5*routerXDiv),routerYInitial+(2*routerYDiv));
				}

				//north wire
				if(j!=(maxY-1))
				{	
					drawArrow(g,routerXInitial+(2*routerXDiv),routerYInitial+(5*routerYDiv),routerXInitial+(2*routerXDiv),routerYInitial+(3*routerYDiv));
					drawArrow(g,routerXInitial+(2*routerXDiv),routerYInitial+(3*routerYDiv),routerXInitial+(2*routerXDiv),routerYInitial+(5*routerYDiv));
				}

////////////////////////////////////////////////////////////////////////////////////////
//draws the router address
////////////////////////////////////////////////////////////////////////////////////////
				int Stringlength = enderecoNodo.length();
				g.setFont(new Font("TimesRoman",Font.BOLD,12));
				String routerXAddress = ""+i;
				String routerYAddress = ""+(maxY-1-j);

				if (Stringlength==4) {
					g.setColor(new Color(0,255,0));
					g.drawString(routerXAddress,routerXInitial+(2*routerXDiv)-(Stringlength+8),routerYInitial+(2*routerYDiv)+4);
					g.setColor(new Color(255,255,102));
					g.drawString(routerYAddress,(routerXInitial+(2*routerXDiv)-(Stringlength+8))+(routerXAddress.length()*6),routerYInitial+(2*routerYDiv)+4);
				}
				else{
					g.setColor(new Color(0,255,0));
					g.drawString(routerXAddress,routerXInitial+(2*routerXDiv)-(Stringlength*Stringlength),routerYInitial+(2*routerYDiv)+4);
					g.setColor(new Color(255,255,102));
					g.drawString(routerYAddress,(routerXInitial+(2*routerXDiv)-(Stringlength*Stringlength))+(routerXAddress.length()*6),routerYInitial+(2*routerYDiv)+4);
				}

				//setRouterPosition(enderecoNodo,x1,y1,x2,y2);
			}
		}
////////////////////////////////////////////////////////////////////////////////////////
// If mark is true marks the path between source router and target router
////////////////////////////////////////////////////////////////////////////////////////
		g.setColor(Color.red);
		if(mark){
			for(int cont=0;cont<pathVector.size();cont++){
				Router p = pathVector.get(cont);
				int i = p.getAddressX();
				int j = p.getAddressY();

				routerXInitial = i * routerXSpace;
				routerYInitial = ((maxY-1)-j) * routerYSpace;
				x1 = routerXInitial + routerXDiv;
				y1 = routerYInitial + routerYDiv;

				if(p.getPort()==0){ // east wire
					drawArrow(g, routerXInitial+(3*routerXDiv),routerYInitial+(2*routerYDiv),routerXInitial+(5*routerXDiv),routerYInitial+(2*routerYDiv));
				}
				else if(p.getPort()==1){ // west wire
					drawArrow(g, routerXInitial+routerXDiv,routerYInitial+(2*routerYDiv),routerXInitial-routerXDiv,routerYInitial+(2*routerYDiv));
				}
				else if(p.getPort()==2){ // north wire
					drawArrow(g, routerXInitial+(2*routerXDiv),routerYInitial+routerYDiv,routerXInitial+(2*routerXDiv),routerYInitial-routerYDiv);
				}
				else if(p.getPort()==3){ // south wire
					drawArrow(g, routerXInitial+(2*routerXDiv),routerYInitial+(3*routerYDiv),routerXInitial+(2*routerXDiv),routerYInitial+(5*routerYDiv));
				}
			}
		}
	}

	/**
	 * Draws in JPanel_Noc a NoC with 2D-Torus topology.
	 * @param g The Graphics class
	 */
	private void draw2DTorus(Graphics g){
		String enderecoNodo;
		int routerXSpace,routerYSpace,routerXDiv,routerYDiv,routerXInitial,routerYInitial;
		int x1,y1;
		//divide the X-dimension of JPanel to define the X-dimension of each router
		routerXSpace=dimx/maxX;
		//divide the Y-dimension of JPanel to define the Y-dimension of each router
		routerYSpace=dimy/maxY;
		//divide the X-dimension of router by four: (1)west wire (2 and 3) router (4)east wire
		routerXDiv=routerXSpace/4;
		//divide the Y-dimension of router by four: (1)north wire (2 and 3) router (4)south wire
		routerYDiv=routerYSpace/4;

		for(int i=0;i<maxX;i++){
			for(int j=0;j<maxY;j++){

				enderecoNodo = ""+i+(maxY-1-j);
				routerXInitial = i * routerXSpace;
				routerYInitial = j * routerYSpace;
				x1 = routerXInitial + routerXDiv;
				y1 = routerYInitial + routerYDiv;

////////////////////////////////////////////////////////////////////////////////////////
//draws the router
////////////////////////////////////////////////////////////////////////////////////////
				g.setColor(Color.black);
				g.fillRect(x1,y1,2*routerXDiv,2*routerYDiv);

////////////////////////////////////////////////////////////////////////////////////////
//draws the wires
////////////////////////////////////////////////////////////////////////////////////////
				
				//east wire
				if(i!=(maxX-1)){
					drawArrow(g,routerXInitial+(5*routerXDiv),routerYInitial+(2*routerYDiv),routerXInitial+(3*routerXDiv),routerYInitial+(2*routerYDiv));
					drawArrow(g,routerXInitial+(3*routerXDiv),routerYInitial+(2*routerYDiv),routerXInitial+(5*routerXDiv),routerYInitial+(2*routerYDiv));
				}

				//north wire
				if(j!=(maxY-1)){
					drawArrow(g,routerXInitial+(2*routerXDiv),routerYInitial+(5*routerYDiv),routerXInitial+(2*routerXDiv),routerYInitial+(3*routerYDiv));
					drawArrow(g,routerXInitial+(2*routerXDiv),routerYInitial+(3*routerYDiv),routerXInitial+(2*routerXDiv),routerYInitial+(5*routerYDiv));
				}

////////////////////////////////////////////////////////////////////////////////////////
//draws the router address
////////////////////////////////////////////////////////////////////////////////////////

				int Stringlength = enderecoNodo.length();
				g.setFont(new Font("TimesRoman",Font.BOLD,12));
				String routerXAddress = ""+i;
				String routerYAddress = ""+(maxY-1-j);

				if (Stringlength==4) {
					g.setColor(new Color(0,255,0));
					g.drawString(routerXAddress,routerXInitial+(2*routerXDiv)-(Stringlength+8),routerYInitial+(2*routerYDiv)+4);
					g.setColor(new Color(255,255,102));
					g.drawString(routerYAddress,(routerXInitial+(2*routerXDiv)-(Stringlength+8))+(routerXAddress.length()*6),routerYInitial+(2*routerYDiv)+4);
				}
				else{
					g.setColor(new Color(0,255,0));
					g.drawString(routerXAddress,routerXInitial+(2*routerXDiv)-(Stringlength*Stringlength),routerYInitial+(2*routerYDiv)+4);
					g.setColor(new Color(255,255,102));
					g.drawString(routerYAddress,(routerXInitial+(2*routerXDiv)-(Stringlength*Stringlength))+(routerXAddress.length()*6),routerYInitial+(2*routerYDiv)+4);
				}

				//setRouterPosition(enderecoNodo,x1,y1,x2,y2);
				
////////////////////////////////////////////////////////////////////////////////////////
//draws the horizontal wires between external routers
////////////////////////////////////////////////////////////////////////////////////////
				g.setColor(new Color(120,120,120));
				if(i==0){
					//right vertical short line
					g.drawLine((3*routerXDiv)+(routerXDiv/2)+((maxX-1)*routerXSpace),2*routerYDiv+(j*routerYSpace),(3*routerXDiv)+(routerXDiv/2)+((maxX-1)*routerXSpace),(3*routerYDiv)+(routerYDiv/4)+(j*routerYSpace));

					//left vertical short line
					g.drawLine(routerXInitial+(routerXDiv/2),2*routerYDiv+(j*routerYSpace),routerXInitial+(routerXDiv/2),(3*routerYDiv)+(routerYDiv/4)+(j*routerYSpace));

					//right horizontal short arrow
					drawArrow(g,((maxX-1)*routerXSpace)+3*routerXDiv+(routerXDiv/2),2*routerYDiv+(j*routerYSpace),((maxX-1)*routerXSpace)+3*routerXDiv,2*routerYDiv+(j*routerYSpace));

					//horizontal long line
					g.drawLine(routerXInitial+(routerXDiv/2),(3*routerYDiv)+(routerYDiv/4)+(j*routerYSpace),((maxX-1)*routerXSpace)+3*routerXDiv+(routerXDiv/2),(3*routerYDiv)+(routerYDiv/4)+(j*routerYSpace));

					//left horizontal short arrow
					drawArrow(g,routerXInitial+(routerXDiv/2),2*routerYDiv+(j*routerYSpace),routerXInitial+routerXDiv-1,2*routerYDiv+(j*routerYSpace));
				}

////////////////////////////////////////////////////////////////////////////////////////
//draws the vertical wires between external routers
////////////////////////////////////////////////////////////////////////////////////////
				if(j==0){
					//up horizontal short line

					g.drawLine(2*routerXDiv+(i*routerXSpace),(routerYDiv/2),(3*routerXDiv)+(routerXDiv/4)+(i*routerXSpace),(routerYDiv/2));

					//bottom horizontal short line
					g.drawLine(2*routerXDiv+(i*routerXSpace),((maxY-1)*routerYSpace)+3*routerYDiv+(routerYDiv/2),(3*routerXDiv)+(routerXDiv/4)+(i*routerXSpace),((maxY-1)*routerYSpace)+3*routerYDiv+(routerYDiv/2));

					//up vertical short arrow
					drawArrow(g,2*routerXDiv+(i*routerXSpace),(routerYDiv/2),2*routerXDiv+(i*routerXSpace),routerYDiv-1);

					//vertical long line
					g.drawLine((3*routerXDiv)+(routerXDiv/4)+(i*routerXSpace),(routerYDiv/2),(3*routerXDiv)+(routerXDiv/4)+(i*routerXSpace),((maxY-1)*routerYSpace)+3*routerYDiv+(routerYDiv/2));

					//bottom vertical short arrow
					drawArrow(g,2*routerXDiv+(i*routerXSpace),((maxY-1)*routerYSpace)+3*routerYDiv+(routerYDiv/2),2*routerXDiv+(i*routerXSpace),((maxY-1)*routerYSpace)+3*routerYDiv);
				}
			}
		}

////////////////////////////////////////////////////////////////////////////////////////
// If mark is true marks the path between source router and target router
////////////////////////////////////////////////////////////////////////////////////////
		g.setColor(Color.red);
		if(mark){
			for(int cont=0;cont<pathVector.size();cont++){
				Router p = pathVector.get(cont);
				int i = p.getAddressX();
				int j = p.getAddressY();

				routerXInitial = i * routerXSpace;
				routerYInitial = ((maxY-1)-j) * routerYSpace;
				x1 = routerXInitial + routerXDiv;
				y1 = routerYInitial + routerYDiv;
				
				//east wire 
				if(p.getPort() == Router.EAST){
					if(i!=(maxX-1)){
						drawArrow(g,routerXInitial+(3*routerXDiv),routerYInitial+(2*routerYDiv),routerXInitial+(5*routerXDiv),routerYInitial+(2*routerYDiv));
					}
					else{
						//right horizontal short line
						g.drawLine(routerXInitial+(3*routerXDiv), routerYInitial+(2*routerYDiv), routerXInitial+(3*routerXDiv)+(routerXDiv/2), routerYInitial+(2*routerYDiv));
						//horizontal long line
						g.drawLine((routerXDiv/2), routerYInitial+(3*routerYDiv)+(routerYDiv/4), routerXInitial+(3*routerXDiv)+(routerXDiv/2), routerYInitial+(3*routerYDiv)+(routerYDiv/4));
						//left horizontal short arrow
						drawArrow(g,(routerXDiv/2), routerYInitial+(2*routerYDiv), routerXDiv-1, routerYInitial+(2*routerYDiv));
						//left vertical short line
						g.drawLine((routerXDiv/2), routerYInitial+(3*routerYDiv)+(routerYDiv/4), (routerXDiv/2), routerYInitial+(2*routerYDiv));
						//right vertical short line
						g.drawLine(routerXInitial+(3*routerXDiv)+(routerXDiv/2), routerYInitial+(3*routerYDiv)+(routerYDiv/4), routerXInitial+(3*routerXDiv)+(routerXDiv/2), routerYInitial+(2*routerYDiv));						
					}
				}
				//west wire 
				if(p.getPort() == Router.WEST){
					if(i!=0){
						drawArrow(g,routerXInitial+(routerXDiv),routerYInitial+(2*routerYDiv),routerXInitial-(routerXDiv),routerYInitial+(2*routerYDiv));
					}
					else{
						//right horizontal short line
						drawArrow(g,((maxX-1)*routerXSpace)+(3*routerXDiv)+(routerXDiv/2), routerYInitial+(2*routerYDiv), ((maxX-1)*routerXSpace)+(3*routerXDiv), routerYInitial+(2*routerYDiv));
						//horizontal long line
						g.drawLine(routerXInitial+(routerXDiv/2), routerYInitial+(3*routerYDiv)+(routerYDiv/4), ((maxX-1)*routerXSpace)+(3*routerXDiv)+(routerXDiv/2), routerYInitial+(3*routerYDiv)+(routerYDiv/4));
						//left horizontal short arrow
						g.drawLine(routerXInitial+(routerXDiv/2), routerYInitial+(2*routerYDiv), routerXInitial+(routerXDiv) , routerYInitial+(2*routerYDiv));
						//left vertical short line
						g.drawLine(routerXInitial+(routerXDiv/2), routerYInitial+(3*routerYDiv)+(routerYDiv/4), routerXInitial+(routerXDiv/2), routerYInitial+(2*routerYDiv));
						//right vertical short line
						g.drawLine(((maxX-1)*routerXSpace)+(3*routerXDiv)+(routerXDiv/2), routerYInitial+(3*routerYDiv)+(routerYDiv/4), ((maxX-1)*routerXSpace)+(3*routerXDiv)+(routerXDiv/2), routerYInitial+(2*routerYDiv));						
						
					}
				}
				//north wire 
				if(p.getPort() == Router.NORTH){
					if(j!=(maxY-1)){
						drawArrow(g,routerXInitial+(2*routerXDiv),routerYInitial+(routerYDiv),routerXInitial+(2*routerXDiv),routerYInitial-(routerYDiv));
					}
					else{
						//vertical long line
						g.drawLine((3*routerXDiv)+(routerXDiv/4)+(i*routerXSpace),(routerYDiv/2),(3*routerXDiv)+(routerXDiv/4)+(i*routerXSpace),((maxY-1)*routerYSpace)+(3*routerYDiv)+(routerYDiv/2));
						//up vertical short line
						g.drawLine(2*routerXDiv+(i*routerXSpace),(routerYDiv/2),2*routerXDiv+(i*routerXSpace),routerYDiv-1);
						//bottom vertical short line
						drawArrow(g,2*routerXDiv+(i*routerXSpace),((maxY-1)*routerYSpace)+3*routerYDiv+(routerYDiv/2),2*routerXDiv+(i*routerXSpace),((maxY-1)*routerYSpace)+3*routerYDiv);
						//up horizontal short line
						g.drawLine(2*routerXDiv+(i*routerXSpace),(routerYDiv/2),(3*routerXDiv)+(routerXDiv/4)+(i*routerXSpace),(routerYDiv/2));
						//bottom horizontal short line
						g.drawLine(2*routerXDiv+(i*routerXSpace),((maxY-1)*routerYSpace)+3*routerYDiv+(routerYDiv/2),(3*routerXDiv)+(routerXDiv/4)+(i*routerXSpace),((maxY-1)*routerYSpace)+3*routerYDiv+(routerYDiv/2));
					}
				}
				//south wire 
				if(p.getPort() == Router.SOUTH){
					if(j!=0){
						drawArrow(g,routerXInitial+(2*routerXDiv),routerYInitial+(3*routerYDiv),routerXInitial+(2*routerXDiv),routerYInitial+(5*routerYDiv));
					}
					else{
						//vertical long line
						g.drawLine((3*routerXDiv)+(routerXDiv/4)+(i*routerXSpace),(routerYDiv/2),(3*routerXDiv)+(routerXDiv/4)+(i*routerXSpace),((maxY-1)*routerYSpace)+(3*routerYDiv)+(routerYDiv/2));
						//up vertical short line
						drawArrow(g,2*routerXDiv+(i*routerXSpace),(routerYDiv/2),2*routerXDiv+(i*routerXSpace),routerYDiv-1);
						//bottom vertical short line
						g.drawLine(2*routerXDiv+(i*routerXSpace),((maxY-1)*routerYSpace)+3*routerYDiv+(routerYDiv/2),2*routerXDiv+(i*routerXSpace),((maxY-1)*routerYSpace)+3*routerYDiv);
						//up horizontal short line
						g.drawLine(2*routerXDiv+(i*routerXSpace),(routerYDiv/2),(3*routerXDiv)+(routerXDiv/4)+(i*routerXSpace),(routerYDiv/2));
						//bottom horizontal short line
						g.drawLine(2*routerXDiv+(i*routerXSpace),((maxY-1)*routerYSpace)+3*routerYDiv+(routerYDiv/2),(3*routerXDiv)+(routerXDiv/4)+(i*routerXSpace),((maxY-1)*routerYSpace)+3*routerYDiv+(routerYDiv/2));
					}
				}
			}
		}
				
	}

	/**
	 * Draws in JPanel_Noc a NoC with Unidirectional Mesh topology.
	 * @param g The Graphics class
	 */
	private void draw1DTorus(Graphics g){
		String enderecoNodo;
		int routerXSpace,routerYSpace,routerXDiv,routerYDiv,routerXInitial,routerYInitial;
		int x1,y1;
		//divide the X-dimension of JPanel to define the X-dimension of each router
		routerXSpace=dimx/maxX;
		//divide the Y-dimension of JPanel to define the Y-dimension of each router
		routerYSpace=dimy/maxY;
		//divide the X-dimension of router by four: (1)west wire (2 and 3) router (4)east wire
		routerXDiv=routerXSpace/4;
		//divide the Y-dimension of router by four: (1)north wire (2 and 3) router (4)south wire
		routerYDiv=routerYSpace/4;

		for(int i=0;i<maxX;i++){
			for(int j=0;j<maxY;j++){
				enderecoNodo = ""+i+(maxY-1-j);
				routerXInitial = i * routerXSpace;
				routerYInitial = j * routerYSpace;
				x1 = routerXInitial + routerXDiv;
				y1 = routerYInitial + routerYDiv;

////////////////////////////////////////////////////////////////////////////////////////
//draws the router
////////////////////////////////////////////////////////////////////////////////////////
				g.setColor(Color.black);
				g.fillRect(x1,y1,2*routerXDiv,2*routerYDiv);

////////////////////////////////////////////////////////////////////////////////////////
//draws the wires
////////////////////////////////////////////////////////////////////////////////////////
				//east wire
				if(i!=(maxX-1))
					drawArrow(g,routerXInitial+(3*routerXDiv),routerYInitial+(2*routerYDiv),routerXInitial+(5*routerXDiv)+2,routerYInitial+(2*routerYDiv));

				//north wire
				if(j!=(maxY-1))
					drawArrow(g,routerXInitial+(2*routerXDiv),routerYInitial+(5*routerYDiv),routerXInitial+(2*routerXDiv),routerYInitial+(3*routerYDiv));

////////////////////////////////////////////////////////////////////////////////////////
//draws the address router
////////////////////////////////////////////////////////////////////////////////////////
				int Stringlength = enderecoNodo.length();
				g.setFont(new Font("TimesRoman",Font.BOLD,12));
				String routerXAddress = ""+i;
				String routerYAddress = ""+(maxY-1-j);

				if (Stringlength==4) {
					g.setColor(new Color(0,255,0));
					g.drawString(routerXAddress,routerXInitial+(2*routerXDiv)-(Stringlength+8),routerYInitial+(2*routerYDiv)+4);
					g.setColor(new Color(255,255,102));
					g.drawString(routerYAddress,(routerXInitial+(2*routerXDiv)-(Stringlength+8))+(routerXAddress.length()*6),routerYInitial+(2*routerYDiv)+4);
				}
				else{
					g.setColor(new Color(0,255,0));
					g.drawString(routerXAddress,routerXInitial+(2*routerXDiv)-(Stringlength*Stringlength),routerYInitial+(2*routerYDiv)+4);
					g.setColor(new Color(255,255,102));
					g.drawString(routerYAddress,(routerXInitial+(2*routerXDiv)-(Stringlength*Stringlength))+(routerXAddress.length()*6),routerYInitial+(2*routerYDiv)+4);
				}

////////////////////////////////////////////////////////////////////////////////////////
//draws the horizontal wires between external routers
////////////////////////////////////////////////////////////////////////////////////////
				g.setColor(new Color(120,120,120));
				
				if(i==0){
					//right vertical short line
					g.drawLine((3*routerXDiv)+(routerXDiv/2)+((maxX-1)*routerXSpace),2*routerYDiv+(j*routerYSpace),(3*routerXDiv)+(routerXDiv/2)+((maxX-1)*routerXSpace),(3*routerYDiv)+(routerYDiv/4)+(j*routerYSpace));
					//left vertical short line
					g.drawLine(routerXInitial+(routerXDiv/2),2*routerYDiv+(j*routerYSpace),routerXInitial+(routerXDiv/2),(3*routerYDiv)+(routerYDiv/4)+(j*routerYSpace));
					//right horizontal short line
					g.drawLine(((maxX-1)*routerXSpace)+3*routerXDiv,2*routerYDiv+(j*routerYSpace),((maxX-1)*routerXSpace)+3*routerXDiv+(routerXDiv/2),2*routerYDiv+(j*routerYSpace));
					//horizontal long line
					g.drawLine(routerXInitial+(routerXDiv/2),(3*routerYDiv)+(routerYDiv/4)+(j*routerYSpace),((maxX-1)*routerXSpace)+3*routerXDiv+(routerXDiv/2),(3*routerYDiv)+(routerYDiv/4)+(j*routerYSpace));
					//left horizontal short arrow
					drawArrow(g,routerXInitial+(routerXDiv/2),2*routerYDiv+(j*routerYSpace),routerXInitial+routerXDiv-1,2*routerYDiv+(j*routerYSpace));
				}

////////////////////////////////////////////////////////////////////////////////////////
//draws the vertical wires between external routers
////////////////////////////////////////////////////////////////////////////////////////
				if(j==0){
					//up horizontal short line
					g.drawLine(2*routerXDiv+(i*routerXSpace),(routerYDiv/2),(3*routerXDiv)+(routerXDiv/4)+(i*routerXSpace),(routerYDiv/2));
					//bottom horizontal short line
					g.drawLine(2*routerXDiv+(i*routerXSpace),((maxY-1)*routerYSpace)+3*routerYDiv+(routerYDiv/2),(3*routerXDiv)+(routerXDiv/4)+(i*routerXSpace),((maxY-1)*routerYSpace)+3*routerYDiv+(routerYDiv/2));
					//up vertical short line
					g.drawLine(2*routerXDiv+(i*routerXSpace),(routerYDiv/2),2*routerXDiv+(i*routerXSpace),routerYDiv-1);
					//vertical long line
					g.drawLine((3*routerXDiv)+(routerXDiv/4)+(i*routerXSpace),(routerYDiv/2),(3*routerXDiv)+(routerXDiv/4)+(i*routerXSpace),((maxY-1)*routerYSpace)+3*routerYDiv+(routerYDiv/2));
					//bottom vertical short line
					drawArrow(g,2*routerXDiv+(i*routerXSpace),((maxY-1)*routerYSpace)+3*routerYDiv+(routerYDiv/2),2*routerXDiv+(i*routerXSpace),((maxY-1)*routerYSpace)+3*routerYDiv);
				}
			}
		}

////////////////////////////////////////////////////////////////////////////////////////
// If mark is true marks the path between source router and target router
////////////////////////////////////////////////////////////////////////////////////////
		g.setColor(Color.red);
		if(mark){
			for(int cont=0;cont<pathVector.size();cont++){
				Router p = pathVector.get(cont);
				int i = p.getAddressX();
				int j = p.getAddressY();

				routerXInitial = i * routerXSpace;
				routerYInitial = ((maxY-1)-j) * routerYSpace;
				x1 = routerXInitial + routerXDiv;
				y1 = routerYInitial + routerYDiv;
				
				//east wire 
				if(p.getPort() == Router.EAST){
					if(i!=(maxX-1))
						drawArrow(g,routerXInitial+(3*routerXDiv), routerYInitial+(2*routerYDiv), routerXInitial+(5*routerXDiv)+2, routerYInitial+(2*routerYDiv));
					else{
						//right horizontal short line
						g.drawLine(routerXInitial+(3*routerXDiv), routerYInitial+(2*routerYDiv), routerXInitial+(3*routerXDiv)+(routerXDiv/2), routerYInitial+(2*routerYDiv));
						//horizontal long line
						g.drawLine((routerXDiv/2), routerYInitial+(3*routerYDiv)+(routerYDiv/4), routerXInitial+(3*routerXDiv)+(routerXDiv/2), routerYInitial+(3*routerYDiv)+(routerYDiv/4));
						//left horizontal short arrow
						drawArrow(g,(routerXDiv/2), routerYInitial+(2*routerYDiv), routerXDiv-1, routerYInitial+(2*routerYDiv));
						//left vertical short line
						g.drawLine((routerXDiv/2), routerYInitial+(3*routerYDiv)+(routerYDiv/4), (routerXDiv/2), routerYInitial+(2*routerYDiv));
						//right vertical short line
						g.drawLine(routerXInitial+(3*routerXDiv)+(routerXDiv/2), routerYInitial+(3*routerYDiv)+(routerYDiv/4), routerXInitial+(3*routerXDiv)+(routerXDiv/2), routerYInitial+(2*routerYDiv));
					}
				}
				//north wire
				if(p.getPort() == Router.NORTH){
					if(j!=(maxY-1))
						drawArrow(g,routerXInitial+(2*routerXDiv),routerYInitial+(routerYDiv),routerXInitial+(2*routerXDiv),routerYInitial-(routerYDiv));
					else{
						//vertical long line
						g.drawLine((3*routerXDiv)+(routerXDiv/4)+(i*routerXSpace),(routerYDiv/2),(3*routerXDiv)+(routerXDiv/4)+(i*routerXSpace),((maxY-1)*routerYSpace)+(3*routerYDiv)+(routerYDiv/2));
						//up vertical short line
						g.drawLine(2*routerXDiv+(i*routerXSpace),(routerYDiv/2),2*routerXDiv+(i*routerXSpace),routerYDiv-1);
						//bottom vertical short line
						drawArrow(g,2*routerXDiv+(i*routerXSpace),((maxY-1)*routerYSpace)+3*routerYDiv+(routerYDiv/2),2*routerXDiv+(i*routerXSpace),((maxY-1)*routerYSpace)+3*routerYDiv);
						//up horizontal short line
						g.drawLine(2*routerXDiv+(i*routerXSpace),(routerYDiv/2),(3*routerXDiv)+(routerXDiv/4)+(i*routerXSpace),(routerYDiv/2));
						//bottom horizontal short line
						g.drawLine(2*routerXDiv+(i*routerXSpace),((maxY-1)*routerYSpace)+3*routerYDiv+(routerYDiv/2),(3*routerXDiv)+(routerXDiv/4)+(i*routerXSpace),((maxY-1)*routerYSpace)+3*routerYDiv+(routerYDiv/2));
					}
				}
			}
		}
	}

	private static void drawArrow(Graphics g, int xCenter, int yCenter, int x, int y) {
		double aDir=Math.atan2(xCenter-x,yCenter-y);
		g.drawLine(x,y,xCenter,yCenter);
		Polygon tmpPoly=new Polygon();
		int i1=6;
		int i2=3;                                       // make the arrow head the same size regardless of the length length
		tmpPoly.addPoint(x,y);							// arrow tip
		tmpPoly.addPoint(x+xCor(i1,aDir+.5),y+yCor(i1,aDir+.5));
		tmpPoly.addPoint(x+xCor(i2,aDir),y+yCor(i2,aDir));
		tmpPoly.addPoint(x+xCor(i1,aDir-.5),y+yCor(i1,aDir-.5));
		tmpPoly.addPoint(x,y);							// arrow tip
		g.drawPolygon(tmpPoly);
		g.fillPolygon(tmpPoly);						// remove this line to leave arrow head unpainted
	}
	public Vector<Router> getVetorRouter()
	{
		return(routerVector);
	} 
	/**
	 * Add routers to routerVector. 
	 */
	private void addRouters(){
		System.out.println("set router position");
		int routerXAddress,routerYAddress;
		int routerXSpace,routerYSpace,routerXDiv,routerYDiv,routerXInitial,routerYInitial;
		int x1,x2,y1,y2;
		//divide the X-dimension of JPanel to define the X-dimension of each router
		routerXSpace=dimx/maxX;
		//divide the Y-dimension of JPanel to define the Y-dimension of each router
		routerYSpace=dimy/maxY;
		//divide the X-dimension of router by four: (1)west wire (2 and 3) router (4)east wire
		routerXDiv=routerXSpace/4;
		//divide the Y-dimension of router by four: (1)north wire (2 and 3) router (4)south wire
		routerYDiv=routerYSpace/4;
		
		routerVector = new Vector<Router>();
		for(int i=0;i<maxX;i++){
			for(int j=0;j<maxY;j++){
				routerXAddress = i;
				routerYAddress = (maxY-1-j);
				routerXInitial = i * routerXSpace;
				routerYInitial = j * routerYSpace;
				x1 = routerXInitial + routerXDiv;
				y1 = routerYInitial + routerYDiv;
				x2 = x1 + 2 * routerXDiv;
				y2 = y1 + 2 * routerYDiv;
				routerVector.add(new Router(routerXAddress,routerYAddress, x1,y1,x2,y2));
			}
		}
	}
	
	private static int yCor(int len, double dir) {return (int)(len * Math.cos(dir));}
	private static int xCor(int len, double dir) {return (int)(len * Math.sin(dir));}

}
