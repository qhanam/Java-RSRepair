package ca.uwaterloo.ece.qhanam.jrsrepair.compiler;

import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * From https://weblogs.java.net/blog/malenkov/archive/2008/12/how_to_compile.html
 */
public class JavaJDKCompiler {
	private String buildDirectory;
	private String classpath;
	
	public JavaJDKCompiler(String buildDirectory, String classpath){
		this.classpath = classpath;
	}
	
	/**
	 * Compiles the Java source file and writes the resulting .class file to the build/classes
	 * directory. Returns the result of the compilation (true = compiled, false = compilation
	 * error).
	 * @param packageName
	 * @param className
	 * @param document
	 * @return true if there were no compilation errors.
	 * @throws Exception
	 */
	public boolean compile(String packageName, String className, String document) throws Exception{
		StringWriter output = new StringWriter();
		String fullyQualifiedClassName = packageName + "." + className;
		
		/* Compile the Java file. */
	    MemoryClassLoader mcl = new MemoryClassLoader(fullyQualifiedClassName, document, this.classpath, output);
	    
	    /* Get the bytes from the class. */
	    byte[] classBytes = mcl.getClassBytes(this.classpath);
	    
	    /* Write the class to disk. */
        Files.write(Paths.get(this.buildDirectory, packageName.replace(".", "/"), className), 
        					  classBytes, StandardOpenOption.WRITE, 
        					  StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        
        System.out.print(output.toString());
        return true;
	}
	
}
