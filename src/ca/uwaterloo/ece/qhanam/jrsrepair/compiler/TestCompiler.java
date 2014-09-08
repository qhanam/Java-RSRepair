package ca.uwaterloo.ece.qhanam.jrsrepair.compiler;

/**
 * From https://weblogs.java.net/blog/malenkov/archive/2008/12/how_to_compile.html
 */
public class TestCompiler {
	private static final String CLASS = "Test";
	private static final String METHOD = "execute";
	private static final String EXPRESSION = "Math.cos(Math.PI/6)";
	private static final String CONTENT    
	        = "public class " + CLASS + " {"
	        + "    public static Object " + METHOD + "() {"
	        + "        return " + EXPRESSION + ";"
	        + "    }"
	        + "}";

	public static void main(String[] args) throws Exception {
	    MemoryClassLoader mcl = new MemoryClassLoader(CLASS, CONTENT);
	    System.out.println(mcl.loadClass(CLASS).getMethod(METHOD).invoke(null));
	}
}
