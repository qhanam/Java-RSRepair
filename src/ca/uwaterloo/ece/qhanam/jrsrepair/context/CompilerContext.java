package ca.uwaterloo.ece.qhanam.jrsrepair.context;

import ca.uwaterloo.ece.qhanam.jrsrepair.compiler.JavaJDKCompiler;

/**
 * CompilerContext stores the context for the compiler. Right now there is only
 * one compiler, so this class doesn't do much except hide the compiler a bit.
 * If we add the Eclipse compiler, this will be useful.
 * 
 * @author qhanam
 */
public class CompilerContext {

	private JavaJDKCompiler compiler;	// Can make this abstract if we add more compilers.
	
	public CompilerContext(JavaJDKCompiler compiler){
		this.compiler = compiler;
	}
	
	/**
	 * Compiles the Java source file and writes the resulting .class file to the build/classes
	 * directory. Returns the result of the compilation (COMPILED if ok or NOT_COMPILED if
	 * there was an error).
	 * @param packageName
	 * @param className
	 * @param document
	 * @return JRSRepair.TestStatus (NOT_COMPILED or COMPILED)
	 * @throws Exception
	 */
	public JavaJDKCompiler.Status compile() throws Exception{
		return this.compiler.compile();
	}
	
	/**
	 * Stores the class files in the given directory.
	 * @param directory Base directory for .class files.
	 */
	public void storeCompiled(String directory){
		this.compiler.storeCompiled(directory);
	}

	/**
	 * Returns the error message at the head of the queue.
	 * 
	 * Recommended use is to dequeue after each compile, or
	 * wait until execution has finished and iterate through
	 * the queue.
	 * @return
	 */
	public String dequeueCompileError() {
		return this.compiler.dequeueCompileError();
	}

}
