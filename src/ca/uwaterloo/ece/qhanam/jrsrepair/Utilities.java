package ca.uwaterloo.ece.qhanam.jrsrepair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Utilities {
	
//	/**
//	 * Write function for JVM >= 1.7
//	 * @param file
//	 * @param content
//	 * @throws Exception
//	 */
//	public static void writeToFile(File file, byte[] content) throws Exception{
//        Files.write(Paths.get(file.getPath()), content, 
//                    StandardOpenOption.SYNC, StandardOpenOption.WRITE, 
//                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
//	}

	/**
	 * Write function for JVM >= 1.7
	 * @param file
	 * @param content
	 * @throws Exception
	 */
	public static void writeToFileAppend(File file, byte[] content) throws Exception{
        Files.write(Paths.get(file.getPath()), content, 
                    StandardOpenOption.SYNC, StandardOpenOption.WRITE, 
                    StandardOpenOption.APPEND, StandardOpenOption.CREATE);
	}

	/**
	 * Write function for JVM <= 1.6
	 * @param file
	 * @param content
	 * @throws Exception
	 */
	public static void writeToFile(File file, byte[] content) throws Exception{
		FileOutputStream fos = new FileOutputStream(file);
		
		/* We need to synchronize the file on all devices before we
		 * move on. If the file system is not synchronized, JUnit might
		 * use old .class files and give incorrect results. */
		fos.write(content);
		fos.flush();
		fos.getFD().sync();
		fos.close();
	}
	
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
