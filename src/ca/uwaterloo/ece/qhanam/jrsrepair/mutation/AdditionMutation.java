package ca.uwaterloo.ece.qhanam.jrsrepair.mutation;

import org.eclipse.jdt.core.dom.rewrite.*;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.text.edits.*;


public class AdditionMutation implements IMutation {

	/**
	 * Adds the seed statement to the AST right before the 
	 * faulty statement. 
	 */
	@Override
	public void mutate(Statement faulty, Statement seed) {
		/* Strategy:
		 * Each block contains an ordered list of ASTNodes. We need to insert the seed node 
		 * into that list before the faulty node.
		 */
		StructuralPropertyDescriptor location = faulty.getLocationInParent();
		
		AST ast = faulty.getAST();
		ASTRewrite rewrite = ASTRewrite.create(ast);
		ChildPropertyDescriptor cpd = IfStatement.THEN_STATEMENT_PROPERTY;
		ListRewrite lrw = rewrite.getListRewrite(faulty, Block.STATEMENTS_PROPERTY);
		lrw.insertBefore(faulty, rewrite.createCopyTarget(seed), new TextEditGroup("TextEditGroup"));
		
		ASTNode parent = faulty.getParent();
		System.out.println("-------");
		System.out.println(ASTNode.nodeClassForType(parent.getNodeType()) + ": " + faulty.getLocationInParent());
		System.out.println(ASTNode.nodeClassForType(faulty.getNodeType()));
		System.out.println(ASTNode.nodeClassForType(seed.getNodeType()));
		
		//parent.setStructuralProperty(new ChildPropertyDescriptor(), new Object());
	}

}
