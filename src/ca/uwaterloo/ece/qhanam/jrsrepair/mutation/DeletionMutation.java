package ca.uwaterloo.ece.qhanam.jrsrepair.mutation;

import ca.uwaterloo.ece.qhanam.jrsrepair.SourceStatement;
import ca.uwaterloo.ece.qhanam.jrsrepair.DocumentASTRewrite;

import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.rewrite.*;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.text.edits.*;

public class DeletionMutation extends Mutation {
	
	private ASTNode deletedStatement; // The faulty statement that was removed. We also need to keep track of its position.
	private Integer deletedStatementIndex;
	private ListRewrite listRewriter;
	private UndoEdit undoEdit;		// Memento to undo the text edit performed on the this.document.
	
	public DeletionMutation(HashMap<String, DocumentASTRewrite> sourceFileContents, SourceStatement faulty, SourceStatement seed){
		super(sourceFileContents, faulty, seed);
		this.deletedStatementIndex = null;
		this.listRewriter = null;
	}

	/**
	 * Adds the seed statement to the AST right before the 
	 * faulty statement. 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void concreteMutate() throws Exception {
		ASTNode parent = faulty.statement.getParent();

		System.out.println("Applying deletion mutation...");
		
		if(parent instanceof Block){
			/* Here we get the statement list for the Block (hence Block.STATEMENTS_PROPERTY) */
            this.listRewriter = rewrite.getListRewrite(parent, Block.STATEMENTS_PROPERTY);
            
            /* Find the index of the statement we want to delete. */
            List<ASTNode> nodes = (List<ASTNode>) this.listRewriter.getOriginalList();
            for(int i = 0; i < nodes.size(); i++){
            	if(nodes.get(i).equals(faulty.statement)) {
            		this.deletedStatementIndex = i; 
            		break;
            	}
            }
            if(this.deletedStatementIndex == null) throw new Exception("DeletionMutation: Deleted statement index not found.");
            
            /* Store the added statement so we can undo the operation. */
            this.deletedStatement = faulty.statement;
            
            /* Insert the statement into the AST before the faulty statement. */
            this.listRewriter.remove(faulty.statement, null);

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
		if(this.deletedStatement == null || this.undoEdit == null || this.listRewriter == null) return; // Nothing to do.
        
        /* Undo the edit to the AST. */
		this.listRewriter.insertAt(this.deletedStatement, this.deletedStatementIndex, null);
        this.undoEdit.apply(this.document);
        this.undoEdit = null;
	}

}
