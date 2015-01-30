package ca.uwaterloo.ece.qhanam.jrsrepair.compiler;

import java.io.File;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.uwaterloo.ece.qhanam.jrsrepair.DocumentASTRewrite;
import ca.uwaterloo.ece.qhanam.jrsrepair.Utilities;

/**
 * From https://weblogs.java.net/blog/malenkov/archive/2008/12/how_to_compile.html
 */
public class JavaJDKCompiler {
	private MemoryClassLoader mcl;
	private Map<String, DocumentASTRewrite> sourceFileContents;
	private String[] sourcePaths;
	private String classDirectory;
	private String[] classpath;
	
	public JavaJDKCompiler(String classDirectory, String[] classpath){
		this.classDirectory = classDirectory;
		this.classpath = classpath;
		this.mcl = null;
	}
	
	public void setContext(Map<String, DocumentASTRewrite> sourceFileContents, String[] sourcePaths){
		this.sourceFileContents = sourceFileContents;
		this.sourcePaths = sourcePaths;
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
	public int compile() throws Exception{
		StringWriter output = new StringWriter();
		Map<String, String> sourceMap = this.buildSourceMap();
		
		/* Compile the Java file. */
	    mcl = new MemoryClassLoader(sourceMap, this.classpath, output);

	    /* Check the compilation went ok. */
	    //System.out.println(output.toString());
	    if(output.toString().matches("(?s).*\\d+ errors?.*")){
	    	return -1;
	    }
	    
	    /* Write the class files to disk. */
	    this.storeCompiled(this.classDirectory);
        return 0;
	}
	
	/**
	 * Stores the class files in the given directory.
	 * @param directory Base directory for .class files.
	 */
	public void storeCompiled(String directory){
	    /* Write the class files to disk. */
	    List<Output> classFiles = mcl.getAllClasses();
	    
	    /* Write the class to disk. */
	    try{
            for(Output classFile : classFiles){
                File f = new File(directory, classFile.getName());
                f.getParentFile().mkdirs();
                Utilities.writeToFile(new File(directory, classFile.getName()), classFile.toByteArray());
            }
	    }catch (Exception e){
	    	System.out.println(e.getMessage());
	    }
	}
	
	/**
	 * Writes the source file changes back to disk. Only writes the documents that are marked
	 * as tainted.
	 * @throws Exception
	 */
	private Map<String, String> buildSourceMap() throws Exception{
		Map<String, String> map = new HashMap<String, String>();
		for(String sourcePath : this.sourceFileContents.keySet()){
			DocumentASTRewrite drwt = this.sourceFileContents.get(sourcePath);
			map.put(this.getRelativePath(sourcePath), drwt.modifiedDocument.get());
			drwt.untaintDocument();
		}
		return map;
	}

	/**
	 * Gets the relative path of the source (.java) file from its source
	 * directory. We need to do this because JRSRepair handles multiple
	 * source directory locations.
	 * @param sourceFile
	 * @return Relative path to .java file (i.e. [package]/[class])
	 * @throws Exception
	 */
	private String getRelativePath(String sourceFile) throws Exception{
		for(String path : this.sourcePaths){
			File directory = new File(path);
			File file = new File(sourceFile);

			if(isSubDirectory(directory, file)){
				String relativePath = directory.toURI().relativize(file.toURI()).getPath();
				relativePath = relativePath.substring(0, relativePath.length() - 5);
				return relativePath;
			}
		}
		return  null;
	}
	
	  /**
	   * Checks, whether the child directory is a subdirectory of the base 
	   * directory.
	   * 
	   * http://www.java2s.com/Tutorial/Java/0180__File/Checkswhetherthechilddirectoryisasubdirectoryofthebasedirectory.htm
	   *
	   * @param base the base directory.
	   * @param child the suspected child directory.
	   * @return true, if the child is a subdirectory of the base directory.
	   * @throws IOException if an IOError occured during the test.
	   */
	  private boolean isSubDirectory(File base, File child) throws Exception {
	      base = base.getCanonicalFile();
	      child = child.getCanonicalFile();

	      File parentFile = child;
	      while (parentFile != null) {
	          if (base.equals(parentFile)) {
	              return true;
	          }
	          parentFile = parentFile.getParentFile();
	      }
	      return false;
	  }
	
}
