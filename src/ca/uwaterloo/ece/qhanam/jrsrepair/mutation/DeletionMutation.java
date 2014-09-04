package ca.uwaterloo.ece.qhanam.jrsrepair.mutation;

import ca.uwaterloo.ece.qhanam.jrsrepair.SourceStatement;
import ca.uwaterloo.ece.qhanam.jrsrepair.DocumentASTRewrite;

import java.util.HashMap;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.text.edits.*;

public class DeletionMutation extends Mutation {
	
	private ASTNode emptyBlock;
	
	public DeletionMutation(HashMap<String, DocumentASTRewrite> sourceFileContents, SourceStatement faulty, SourceStatement seed){
		super(sourceFileContents, faulty, seed);
		this.emptyBlock = null;
	}

	/**
	 * Adds the seed statement to the AST right before the 
	 * faulty statement. 
	 */
	@Override
	public void concreteMutate() throws Exception {
        /* Create a new block to insert in place of the deleted statement. */
        this.emptyBlock = (Block) this.rewrite.getAST().createInstance(Block.class);

        /* Replace the faulty statement with the empty Block. */
        rewrite.replace(faulty.statement, this.emptyBlock, null);
        
        /* Modify the source code file. */
        this.docrwt.resetModifiedDocument(); // Start with the original document to avoid the AST-doesn't-match-doc error.
        TextEdit edits = rewrite.rewriteAST(this.docrwt.modifiedDocument, null);
        edits.apply(this.docrwt.modifiedDocument, TextEdit.NONE);
	}
	
	/**
	 * Removes the statement added in mutate().
	 */
	@Override
	public void concreteUndo() throws Exception{
        /* Undo the edit to the AST. */
        this.rewrite.replace(this.emptyBlock, this.faulty.statement, null);

		/* We need to write the undo changes back to the source file because of recursion. */
        this.docrwt.resetModifiedDocument(); // Start with the original document to avoid the AST-doesn't-match-doc error.
        TextEdit edits = rewrite.rewriteAST(this.docrwt.modifiedDocument, null);
        edits.apply(this.docrwt.modifiedDocument, TextEdit.NONE);
	}

	@Override
	public String toString(){
		return "Deletion " + super.toString();
	}
}
