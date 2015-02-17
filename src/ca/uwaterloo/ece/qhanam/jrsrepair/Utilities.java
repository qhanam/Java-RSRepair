package ca.uwaterloo.ece.qhanam.jrsrepair;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;

public class Utilities {
	
	/**
	 * The copyFiles method recursively replaces all the .class files in the
	 * destination directory with the corresponding .class file from the source
	 * directory. This is useful when the compilation task (e.g., ant or maven)
	 * typically compiles the source files into multiple class directories. If
	 * there is meta data in these directories, it is easier for us to copy the
	 * .class files back to these directories than try and set up a more complex
	 * build.
	 * @param source 	The newly compiled .class file directory which contains files
	 * 					from multiple source directories.
	 * @param destination	The class directory for which we want to replace the
	 * 						.class files. 
	 * @throws Exception Throws an exception if one of the destination .class files can't
	 * 			be found in the source folder.
	 */
	public static void copyFiles(File source, File destination) throws Exception{

		Iterator<File> iterator =  FileUtils.iterateFiles(destination, new String[] {"class"}, true);

		while(iterator.hasNext()){
			File destinationFile = iterator.next();
			Path relativeDestinationFilePath = Paths.get(destination.getPath()).relativize(Paths.get(destinationFile.getPath()));
			Path sourceFilePath = Paths.get(source.getPath(), relativeDestinationFilePath.toString());
			if(!sourceFilePath.toFile().exists()) 
			{
				// We should check this on the first run and ignore it if the files aren't used in the test case.
				// System.err.println("\nUtilities.copyFiles: The source file " + sourceFilePath.toString() + " does not exist and could not be copied.");
			}
			else{
				/* We should make sure this operation is synchronized with the file system.
				 * For some reason only FileUtils.copyFile works... the others cause a java
				 * 'magic number' exception.*/
                FileUtils.copyFile(sourceFilePath.toFile(), destinationFile); 
                //Utilities.writeToFile(destinationFile, IOUtils.toByteArray(new FileReader(sourceFilePath.toFile())));
                //Files.move(sourceFilePath, destinationFile.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
			}
		}
		
	}
	
	/**
	 * Write function for JVM >= 1.7
	 * @param file
	 * @param content
	 * @throws Exception
	 */
	public static void writeToFile(File file, byte[] content) throws Exception{
        Files.write(Paths.get(file.getPath()), content, 
                    StandardOpenOption.SYNC, StandardOpenOption.WRITE, 
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
	}

	/**
	 * Write function for JVM >= 1.7
	 * @param file
	 * @param content
	 * @throws Exception
	 */
	public static void writeToFileAppend(File file, byte[] content) throws Exception{
		if(!Files.exists(Paths.get(file.toString()))){
			Files.createDirectories(Paths.get(file.getParent()));
            Files.createFile(Paths.get(file.toString()));
		}
        Files.write(Paths.get(file.getPath()), content, 
                    StandardOpenOption.SYNC, StandardOpenOption.WRITE, 
                    StandardOpenOption.APPEND, StandardOpenOption.CREATE);
	}

//	/**
//	 * Write function for JVM <= 1.6
//	 * @param file
//	 * @param content
//	 * @throws Exception
//	 */
//	public static void writeToFile(File file, byte[] content) throws Exception{
//		FileOutputStream fos = new FileOutputStream(file);
//		
//		/* We need to synchronize the file on all devices before we
//		 * move on. If the file system is not synchronized, JUnit might
//		 * use old .class files and give incorrect results. */
//		fos.write(content);
//		fos.flush();
//		fos.getFD().sync();
//		fos.close();
//	}

//	/**
//	 * Write function for JVM <= 1.6
//	 * @param file
//	 * @param content
//	 * @throws Exception
//	 */
//	public static void writeToFileAppend(File file, byte[] content) throws Exception{
//		FileOutputStream fos = new FileOutputStream(file, true);
//		
//		/* We need to synchronize the file on all devices before we
//		 * move on. If the file system is not synchronized, JUnit might
//		 * use old .class files and give incorrect results. */
//		fos.write(content);
//		fos.flush();
//		fos.getFD().sync();
//		fos.close();
//	}
	
	/**
	 * Read function for JVM >= 1.7
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static byte[] readFromFile(File file) throws Exception{
        byte[] bytes = Files.readAllBytes(Paths.get(file.getPath()));
        return bytes;
	}

//	/**
//	 * Read function for JVM <= 1.6
//	 * @param file
//	 * @return
//	 * @throws Exception
//	 */
//	public static byte[] readFromFile(File file) throws Exception{
//		FileInputStream fis = new FileInputStream(file);
//		byte[] bytes = new byte[fis.available()];
//		fis.read(bytes);
//		fis.close();
//		return bytes;
//	}
}
