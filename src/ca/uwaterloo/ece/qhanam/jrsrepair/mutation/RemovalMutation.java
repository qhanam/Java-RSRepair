package ca.uwaterloo.ece.qhanam.jrsrepair.mutation;

import org.eclipse.jdt.core.dom.Statement;

public class RemovalMutation implements IMutation {

	/**
	 * Deletes the statement from the AST. No seed node is 
	 * required for this method (a null value is fine).
	 */
	@Override
	public void mutate(Statement faulty, Statement seed) {
		faulty.delete();
	}

}
