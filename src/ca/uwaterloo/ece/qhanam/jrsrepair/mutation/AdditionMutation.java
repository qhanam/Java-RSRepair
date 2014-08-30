package ca.uwaterloo.ece.qhanam.jrsrepair.mutation;

import ca.uwaterloo.ece.qhanam.jrsrepair.SourceStatement;

import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.rewrite.*;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.text.edits.*;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Document;

public class AdditionMutation extends Mutation {
	
	public AdditionMutation(HashMap<String, IDocument> sourceFileContents){
		super(sourceFileContents);
	}

	/**
	 * Adds the seed statement to the AST right before the 
	 * faulty statement. 
	 */
	@Override
	public void mutate(SourceStatement faulty, SourceStatement seed) throws Exception {
		/* Strategy:
		 * Each block contains an ordered list of ASTNodes. We need to insert the seed node 
		 * into that list before the faulty node. We can use ASTRewrite to perform the 
		 * operations, write the changes to a Document and revert back.
		 * 
		 * Question: 	For statement addition, when we insert a statement into a node with
		 * 			 	parent type IF_STATEMENT, where does it insert? Before the if statement
		 * 			 	or can we not do this because there is no list?
		 * 
		 * Answer: 		I think we need to find an ancestor statement of type Block in order
		 * 				to get a list of statements. Otherwise, we can't use ListRewrite.
		 */
		ASTNode parent = faulty.statement.getParent();
		StructuralPropertyDescriptor location = faulty.statement.getLocationInParent();
		
		/* Start by assuming all parents are block statements. Later we can serch for an ancestor that
		 * is a Block statement */
		AST ast = faulty.statement.getRoot().getAST();
		ASTRewrite rewrite = ASTRewrite.create(ast);

		System.out.println("-------");
		System.out.println(ASTNode.nodeClassForType(parent.getNodeType()) + ": " + faulty.statement.getLocationInParent());
		System.out.println(ASTNode.nodeClassForType(faulty.statement.getNodeType()));
		System.out.println(ASTNode.nodeClassForType(seed.statement.getNodeType()));
		
		if(parent instanceof Block){
            /* Fetch the document from the map. */
            IDocument document = (IDocument) this.sourceMap.get(faulty.sourceFile);
            System.out.print(document.get());

			/* Here we get the statement list for the Block (hence Block.STATEMENTS_PROPERTY) */
            ListRewrite lrw = rewrite.getListRewrite(parent, Block.STATEMENTS_PROPERTY);
            List<ASTNode> nodes = (List<ASTNode>) lrw.getOriginalList();
            System.out.println("Faulty: " + faulty.statement);
            for(ASTNode node : nodes){
            	System.out.println("ListRewrite Node: " + node);
            }
            // Can we do this: rewrite.createCopyTarget(seed)? It may be in a different AST...
            ASTNode s = rewrite.createCopyTarget(seed.statement);
            lrw.insertBefore(rewrite.createCopyTarget(faulty.statement), faulty.statement, new TextEditGroup("TextEditGroup"));
            
            TextEdit edits = rewrite.rewriteAST(document, null);
            UndoEdit undo = edits.apply(document);

            System.out.print(document.get());
		}
		
		
		/* TEMP */
		ChildPropertyDescriptor cpd = IfStatement.THEN_STATEMENT_PROPERTY;
		/* TEMP */
		
		
		//parent.setStructuralProperty(new ChildPropertyDescriptor(), new Object());
	}

}
