package ca.uwaterloo.ece.qhanam.jrsrepair.mutation;

import ca.uwaterloo.ece.qhanam.jrsrepair.SourceStatement;
import ca.uwaterloo.ece.qhanam.jrsrepair.DocumentASTRewrite;

import java.util.HashMap;

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
	@Override
	public void concreteMutate() throws Exception {
		ASTNode parent = faulty.statement.getParent();
		
		/* Start by assuming all parents are block statements. Later we can serch for an ancestor that
		 * is a Block statement */
		AST ast = faulty.statement.getRoot().getAST();
//		AST ast = this.rewrite.getAST();

		System.out.println("Applying addition mutation...");
		
		if(parent instanceof Block){
			/* Here we get the statement list for the Block (hence Block.STATEMENTS_PROPERTY) */
            ListRewrite lrw = rewrite.getListRewrite(parent, Block.STATEMENTS_PROPERTY);
            
            /* Make a copy of the seed statement and base it in the faulty statement's AST. */
            ASTNode s = ASTNode.copySubtree(ast, seed.statement);
            
            /* Store the added statement so we can undo the operation. */
            this.addedStatement = s;
            
            /* Insert the statement into the AST before the faulty statement. */
            lrw.insertBefore(s, faulty.statement, new TextEditGroup("TextEditGroup"));

            /* Modify the source code file. */
            TextEdit edits = rewrite.rewriteAST(this.document, null);
            this.undoEdit = edits.apply(this.document, TextEdit.CREATE_UNDO);
		}
	}
	
	/**
	 * Removes the statement added in mutate().
	 */
	@Override
	public void concreteUndo() throws Exception{
		if(this.addedStatement == null || this.undoEdit == null) return; // Nothing to do.
        
        /* Undo the edit to the AST. */
        this.rewrite.remove(this.addedStatement, null);
        this.undoEdit.apply(this.document);
        this.undoEdit = null;
	}

}
