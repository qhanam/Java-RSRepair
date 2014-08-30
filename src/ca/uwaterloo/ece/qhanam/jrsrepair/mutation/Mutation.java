package ca.uwaterloo.ece.qhanam.jrsrepair.mutation;

import ca.uwaterloo.ece.qhanam.jrsrepair.SourceStatement;

import org.eclipse.jdt.core.dom.*;

import java.util.HashMap;

public abstract class Mutation {
	
	protected HashMap sourceMap;
	
	/**
	 * Creates a new Mutation object.
	 * @param sourceMap The map of source file paths to source file contents.
	 */
	public Mutation(HashMap sourceMap){
		this.sourceMap = sourceMap;
	}
	
	/**
	 * Uses the seed statement to apply a mutation to the faulty statement.
	 * @param faulty A potentially faulty statement.
	 * @param seed A seed statement (from somewhere in the program source).
	 * @throws Exception 
	 */
	public abstract void mutate(SourceStatement faulty, SourceStatement seed) throws Exception;
}
