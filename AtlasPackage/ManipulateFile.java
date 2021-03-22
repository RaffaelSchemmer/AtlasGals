package AtlasPackage;

import java.io.*;
import java.util.*;
import javax.swing.JOptionPane;

/**
* <i>ManipulateFile</i> has some methods to manipulate files. <p> 
* @author Aline Vieira de Mello
* @version
*/
public class ManipulateFile{

	/**
	 * Delete all files and sub directories in the informed directory.
	 * <i> The informed directory is not deleted. </i>
	 * @param directory The path where all files and sub directories will be deleted.
	 */
	public static void deleteFiles(String directory){
		File f = new File(directory);
		if(f.isDirectory()){
			File[] list = f.listFiles();
			for(int i=0;i<list.length;i++){
				deleteAll(list[i]);
			}
		}
		else if(f.exists()){
			JOptionPane.showMessageDialog(null,"The "+f.getName()+" is not a directory.","Error in ManipulateFile.deleteFiles", JOptionPane.WARNING_MESSAGE);
		}
	}

	/**
	 * Delete all files and sub directories in the informed directory, except the informed file.
	 * <i> The informed directory is not deleted. </i>
	 * @param directory The path where all files and sub directories will be deleted.
	 * @param file The file name that will be preserved.  
	 */
	public static void deleteFiles(String directory, String file){
		File f = new File(directory);
		File preservedFile = new File(file);
		if(f.isDirectory()){
			File[] list = f.listFiles();
			for(int i=0;i<list.length;i++){
				if(list[i].isDirectory()){
					deleteAll(list[i]);
				}
				else if(!list[i].getAbsoluteFile().equals(preservedFile.getAbsoluteFile())){
					list[i].delete();
				}
			}
		}
		else if(f.exists()){
			JOptionPane.showMessageDialog(null,"The "+f.getName()+" is not a directory.","Error in ManipulateFile.deleteFiles", JOptionPane.WARNING_MESSAGE);
		}
	}
	
	/**
	 * Delete all files and directories, including the informed file.
	 * @param f The path where all files and directories will be deleted.
	 */
	public static void deleteAll(File f){
		if(f.isDirectory()){
			File[] list = f.listFiles();
			for(int i=0;i<list.length;i++){
				deleteAll(list[i]);
			}
			f.delete();
		}
		else{
			f.delete();
		}
	}
	
	/**
	 * Copy a file to the informed directory.
	 * @param file The file to be copy
	 * @param dir The path where the file will be copy.
	 */
	public static void copy(File file, String dir){
		try{
			FileReader in = new FileReader(file);
			String filename = new String(file.getName());
			File outputFile = new File(dir+filename);
			FileWriter out = new FileWriter(outputFile);

			int c;
			while ((c = in.read()) != -1){
				out.write(c);
			}

			in.close();
			out.close();
		}catch(Exception e){
			JOptionPane.showMessageDialog(null,"Can't copy the file "+file.getName()+"\n"+ e.getMessage(),"Error Message", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}
	
	/**
	 * Order the list of files by name. 
	 * @param files
	 * @return The ordered list. 
	 */
	public static File[] orderByName(File[] files) {
		Arrays.sort( files, 
					new Comparator<File>() {
						public int compare(final File o1, final File o2) {
							return (o1.getName().compareToIgnoreCase(o2.getName())); 
						}
					}
		); 
		return files;
	}  

	
}