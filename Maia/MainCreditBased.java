package Maia;

import java.io.*;

import AtlasPackage.Default;
import AtlasPackage.Project;
import AtlasPackage.NoCGenerationCB;

/**
 * Generate a Hermes NoC with Credit based flow control (without virtual channels).
 * @author Aline Vieira de Mello
 * @version
 */
public class MainCreditBased extends NoCGenerationCB{
    private static String sourceDir = Default.atlashome + File.separator + "Maia" + File.separator + "Data" + File.separator + "CreditBased" + File.separator;
	
	/**
	 * Generate a Hermes NoC with Credit based flow control.
	 * @param project
	 */
	public MainCreditBased(Project project){
		super(project, sourceDir);
	}
}
