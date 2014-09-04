package ca.uwaterloo.ece.qhanam.jrsrepair.mutation;

import ca.uwaterloo.ece.qhanam.jrsrepair.SourceStatement;
import ca.uwaterloo.ece.qhanam.jrsrepair.DocumentASTRewrite;

import java.util.HashMap;

public class NullMutation extends Mutation {
	
	public NullMutation(HashMap<String, DocumentASTRewrite> sourceFileContents, SourceStatement faulty, SourceStatement seed){
		super(sourceFileContents, faulty, seed);
	}

	/**
	 * Replaces the faulty statement with the seed statement.
	 */
	@Override
	public void concreteMutate() throws Exception {/* Don't do anything. */	}
	
	/**
	 * Replace the seed statement with the faulty statement.
	 */
	@Override
	public void concreteUndo() throws Exception{ /* Don't do anything. */ }
	
	@Override
	public String toString(){
		return "Null " + super.toString();
	}
}
