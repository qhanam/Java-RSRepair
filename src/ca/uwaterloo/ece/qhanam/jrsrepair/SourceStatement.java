package ca.uwaterloo.ece.qhanam.jrsrepair;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Statement;

/**
 * Holds a Statement object and the source file the statement came from.
 * 
 * This is a bit inefficient to do, since we could store AST objects and
 * the location of the source file that was parsed to generate it. We don't
 * actually need to store the ASTs because we can get them from the
 * Statement, so by doing this we save ourselves from building that AST
 * holding data structure.
 * @author qhanam
 *
 */
public class SourceStatement {
	public String sourceFile;
	public Statement statement;
	
	public SourceStatement(String sourceFile, Statement statement){
		this.sourceFile = sourceFile;
		this.statement = statement;
	}
}
