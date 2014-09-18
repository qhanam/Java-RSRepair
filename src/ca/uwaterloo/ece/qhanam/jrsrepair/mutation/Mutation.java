package ca.uwaterloo.ece.qhanam.jrsrepair.mutation;

import ca.uwaterloo.ece.qhanam.jrsrepair.DocumentASTRewrite;
import ca.uwaterloo.ece.qhanam.jrsrepair.JRSRepair;
import ca.uwaterloo.ece.qhanam.jrsrepair.SourceStatement;

import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.IDocument;

import java.util.HashMap;

public abstract class Mutation {
	
	protected HashMap<String, DocumentASTRewrite> sourceMap;
    protected DocumentASTRewrite docrwt;
	protected SourceStatement faulty;
	protected SourceStatement seed;
	protected IDocument document;
	protected ASTRewrite rewrite;
	private boolean mutated;
	
	/**
	 * Creates a new Mutation object.
	 * @param sourceMap The map of source file paths to source file contents.
	 * @param faulty A potentially faulty statement.
	 * @param seed A seed statement (from somewhere in the program source).
	 */
	public Mutation(HashMap<String, DocumentASTRewrite> sourceMap, SourceStatement faulty, SourceStatement seed){
		this.docrwt = sourceMap.get(faulty.sourceFile);
		this.rewrite = docrwt.rewriter;
		this.document = docrwt.document;
		this.sourceMap = sourceMap;
		this.faulty = faulty;
		this.seed = seed;
		this.mutated = false;
	}
	
	/**
	 * Uses the seed statement to apply a mutation to the faulty statement.
	 * @throws Exception 
	 */
	public void mutate() throws Exception {
		if(mutated) throw new Exception("A mutate operation has allready been applied. Must call undo() before mutating again.");
		this.docrwt.taintDocument();
		this.concreteMutate();
//		JRSRepair.logMutation(this);
	}

	protected abstract void concreteMutate() throws Exception;
	
	/**
	 * Undoes the mutation that was applied in mutate().
	 * 
	 * This will undo both the change to the text file (Document) as
	 * well as the change to the AST. 
	 * 
	 * Best used with memento pattern.
	 */
	public void undo() throws Exception {
		if(mutated) throw new Exception("The mutate operation has not been applied. Must call mutate() before undo().");

		this.docrwt.taintDocument();
		this.concreteUndo();
		
		/* Relinquish use of the statements. */
		this.faulty.inUse = false;
		if(this.seed != null) this.seed.inUse = false;
	}

	protected abstract void concreteUndo() throws Exception;
	
	@Override
	public String toString(){
		String s = "\nFaulty = " + this.faulty.sourceFile + "@" + this.faulty.statement.getStartPosition() + ": " + this.faulty.statement + "\n\n";
		s += "Seed = ";
		if(this.seed == null) s += "null" + ": " + "\n============================\n";
		else s += this.seed.sourceFile + "@" + this.seed.statement.getStartPosition() + ": " + this.seed.statement + "\n============================\n";

		return s;
	}
}
