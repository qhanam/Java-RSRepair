package ca.uwaterloo.ece.qhanam.jrsrepair.mutation;

import ca.uwaterloo.ece.qhanam.jrsrepair.SourceStatement;
import ca.uwaterloo.ece.qhanam.jrsrepair.DocumentASTRewrite;

import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.rewrite.*;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.text.edits.*;

public class AdditionMutation extends Mutation {
	
	private ASTNode addedStatement; // The (copied) seed statement that was added before the faulty statement.
	private UndoEdit undoEdit;		// Memento to undo the text edit performed on the this.document.
	
	public AdditionMutation(HashMap<String, DocumentASTRewrite> sourceFileContents, SourceStatement faulty, SourceStatement seed){
		super(sourceFileContents, faulty, seed);
	}

	/**
	 * Adds the seed statement to the AST right before the 
	 * faulty statement. 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void mutate() throws Exception {
		/* TODO: Find the nearest ancestor that is a Block statement. */
		ASTNode parent = faulty.statement.getParent();
		
		/* Start by assuming all parents are block statements. Later we can serch for an ancestor that
		 * is a Block statement */
		AST ast = faulty.statement.getRoot().getAST();

		System.out.println("-------");
		System.out.println(ASTNode.nodeClassForType(parent.getNodeType()) + ": " + faulty.statement.getLocationInParent());
		System.out.println(ASTNode.nodeClassForType(faulty.statement.getNodeType()));
		System.out.println(ASTNode.nodeClassForType(seed.statement.getNodeType()));
		
		if(parent instanceof Block){
			/* Here we get the statement list for the Block (hence Block.STATEMENTS_PROPERTY) */
            ListRewrite lrw = rewrite.getListRewrite(parent, Block.STATEMENTS_PROPERTY);
            List<ASTNode> nodes = (List<ASTNode>) lrw.getOriginalList();
            System.out.println("Faulty: " + faulty.statement + " (" + faulty.sourceFile + ")");
            System.out.println("Seed: " + seed.statement + " (" + seed.sourceFile + ")");
            for(ASTNode node : nodes){
            	System.out.println("ListRewrite Node: " + node);
            }
            
            /* Make a copy of the seed statement and base it in the faulty statement's AST. */
            ASTNode s = ASTNode.copySubtree(ast, seed.statement);
            
            /* Store the added statement so we can undo the operation. */
            this.addedStatement = s;
            
            /* Insert the statement into the AST before the faulty statement. */
            lrw.insertBefore(s, faulty.statement, new TextEditGroup("TextEditGroup"));

            /* Modify the source code file. */
            TextEdit edits = rewrite.rewriteAST(this.document, null);
            this.undoEdit = edits.apply(this.document, TextEdit.CREATE_UNDO);

            System.out.print(this.document.get());
		}
	}
	
	/**
	 * Removes the statement added in mutate().
	 */
	@Override
	public void undo() throws Exception{
		if(this.addedStatement == null || this.undoEdit == null) return; // Nothing to do.
        
        /* Undo the edit to the AST. */
        this.rewrite.remove(this.addedStatement, null);
        this.undoEdit.apply(this.document);
        
        System.out.print(this.document.get());
	}

}
