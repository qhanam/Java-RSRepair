package ca.uwaterloo.ece.qhanam.jrsrepair.mutation;

import java.util.HashMap;

import ca.uwaterloo.ece.qhanam.jrsrepair.SourceStatement;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jface.text.IDocument;

public class RemovalMutation extends Mutation {

	public RemovalMutation(HashMap<String, IDocument> sourceMap) {
		super(sourceMap);
	}

	/**
	 * Deletes the statement from the AST. No seed node is 
	 * required for this method (a null value is fine).
	 */
	@Override
	public void mutate(SourceStatement faulty, SourceStatement seed) throws Exception {
		faulty.statement.delete();
	}

}
