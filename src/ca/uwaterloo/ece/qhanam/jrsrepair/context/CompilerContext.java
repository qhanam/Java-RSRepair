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
	
	public JavaJDKCompiler.Status compile() throws Exception{
		return this.compiler.compile();
	}
	
	public void storeCompiled(String directory){
		this.compiler.storeCompiled(directory);
	}

}
