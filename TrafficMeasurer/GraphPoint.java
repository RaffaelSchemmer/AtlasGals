package TrafficMeasurer;

/**
 * This class contains the information about a point of a graph. 
 * @author Aline Vieira de Mello
 * @version
 */
public class GraphPoint{
	private int coordX;
	private int coordY;

	/**
	 * Constructor of class.
	 */
	public GraphPoint(){
		coordX = 0;
		coordY = 0;
	}

	/**
	 * Return the graph position in X-coordinate.
	 * @return The graph position in X-coordinate.
	 */
	public int getCoordX(){return coordX;}

	/**
	 * Return the graph position in Y-coordinate.
	 * @return The graph position in Y-coordinate.
	 */
	public int getCoordY(){return coordY;}

	/**
	 * Set the graph position in X-coordinate.
	 * @param c The graph position in X-coordinate.
	 */
	public void setCoordX(int c){coordX = c;}

	/**
	 * Set the graph position in Y-coordinate.
	 * @param c The graph position in Y-coordinate.
	 */
	public void setCoordY(int c){coordY = c;}
}