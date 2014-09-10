package ca.uwaterloo.ece.qhanam.jrsrepair.compiler;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;

/**
 * From https://weblogs.java.net/blog/malenkov/archive/2008/12/how_to_compile.html
 */
public class TestCompiler {
	private static final String CLASS = "Test";
	private static final String CLASSPATH = ".";
	private static final String METHOD = "execute";
	private static final String EXPRESSION = "Math.cos(Math.PI/6)";
	private static final String CONTENT    
	        = "public class " + CLASS + " {"
	        + "    public static void main(String[] args) {"
	        + "        System.out.println(\"Hello World!\");"
	        + "    }"
	        + "}";
	private static final String MULTIPLE_CONTENT 
	        = "public class " + CLASS + " {"
	        + "    public static void main(String[] args) {"
	        + "        System.out.println(\"Hello World!\");"
	        + " 	   Test test = new Test();"
	        + " 	   System.out.println(\"Result: \" + test.add(1, 2));"
	        + "    }"
	        + "    public int add(int a, int b) { TestSub sub = new TestSub(); return sub.add(a,b); }"
	        + " private class TestSub { public int add(int a, int b){ return a + b; } } "
	        + "}";
	private static final String PATH = "/Users/qhanam/Documents/workspace_repair/ca.uwaterloo.ece.qhanam.jrsrepair/Test.class";
	private static final String CLASSFILES = "/Users/qhanam/Documents/workspace_repair/ca.uwaterloo.ece.qhanam.jrsrepair";

	public static void main(String[] args) throws Exception {
		/* Note: CLASS should be fully qualified (i.e. include the package). */
	    MemoryClassLoader mcl = new MemoryClassLoader(CLASS, MULTIPLE_CONTENT, CLASSPATH, null);
	    
	    /* Get the bytes from the class. */
	    //byte[] classBytes = mcl.getClassBytes(CLASS);
	    List<Output> classFiles = mcl.getAllClasses();
	    
	    /* Write the class to disk. */
	    for(Output classFile : classFiles){
            Files.write(Paths.get(CLASSFILES, classFile.getName()), classFile.toByteArray(), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
	    }

        /* We could also instantiate the class here... although that's not
         * needed for this program.
         * 
         * TODO: Could we keep a test program running, hot swap class files
         * 		 and re-test? That would be cool.
         */
//	    System.out.println(mcl.loadClass(CLASS).getMethod(METHOD).invoke(null));
	}
}
