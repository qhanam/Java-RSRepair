package ca.uwaterloo.ece.qhanam.jrsrepair;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class Utilities {

	public static void writeToFile(File file, String content) throws Exception{
	    Writer out = new OutputStreamWriter(new FileOutputStream(file));
	    try {
	      out.write(content);
	      out.flush();
	    }
	    finally {
	      out.close();
	    }	
	}
}
