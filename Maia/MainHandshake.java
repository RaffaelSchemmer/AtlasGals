package Maia;

import java.io.*;

import AtlasPackage.NoCGenerationHS;
import AtlasPackage.Default;
import AtlasPackage.Project;

/**
 * Generate a Hermes NoC with Handshake flow control (without virtual channels).
 * @author Aline Vieira de Mello
 * @version
 */
public class MainHandshake extends NoCGenerationHS{
	private static String sourceDir = Default.atlashome + File.separator + "Maia" + File.separator + "Data" + File.separator + "Handshake" + File.separator;

	/**
	 * Generate a Hermes NoC with Handshake flow control.
	 * @param project The NoC project.
	 */
	public MainHandshake(Project project){
		super(project, sourceDir);
	}
}
