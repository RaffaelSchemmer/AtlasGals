package Maia;

import java.io.*;

import AtlasPackage.NoCGenerationVC;
import AtlasPackage.Default;
import AtlasPackage.Project;

/**
 * Create a Hermes NoC with virtual channels and Round-robin scheduling algorithm.
 * @author Aline Vieira de Mello
 * @version
 */
public class MainVirtualChannel extends NoCGenerationVC {

	private static String sourceDir = Default.atlashome + File.separator + "Maia" + File.separator + "Data" + File.separator + "VirtualChannel" + File.separator + "RoundRobin" + File.separator;

	/**
	 * Create a Hermes NoC with virtual channels and Round-robin scheduling algorithm.
	 * @param project The NoC project.
	 */
	public MainVirtualChannel(Project project){
		super(project, sourceDir);
	}
}
