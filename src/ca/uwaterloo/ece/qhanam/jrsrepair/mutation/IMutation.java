package ca.uwaterloo.ece.qhanam.jrsrepair.mutation;

import org.eclipse.jdt.core.dom.*;

public interface IMutation {
	/**
	 * Uses the seed statement to apply a mutation to the faulty statement.
	 * @param faulty A potentially faulty statement.
	 * @param seed A seed statement (from somewhere in the program source).
	 */
	void mutate(Statement faulty, Statement seed);
}
