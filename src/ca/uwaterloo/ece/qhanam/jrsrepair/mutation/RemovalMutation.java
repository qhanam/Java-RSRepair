package ca.uwaterloo.ece.qhanam.jrsrepair.mutation;

import ca.uwaterloo.ece.qhanam.jrsrepair.SourceStatement;
import org.eclipse.jdt.core.dom.*;

public class RemovalMutation implements Mutation {

	/**
	 * Deletes the statement from the AST. No seed node is 
	 * required for this method (a null value is fine).
	 */
	@Override
	public void mutate(SourceStatement faulty, SourceStatement seed) {
		faulty.statement.delete();
	}

}
