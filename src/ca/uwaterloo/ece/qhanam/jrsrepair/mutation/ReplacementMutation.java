package ca.uwaterloo.ece.qhanam.jrsrepair.mutation;

import ca.uwaterloo.ece.qhanam.jrsrepair.SourceStatement;
import ca.uwaterloo.ece.qhanam.jrsrepair.DocumentASTRewrite;

import java.util.HashMap;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.*;

public class ReplacementMutation extends Mutation {
	
	private ASTNode replacementNode;	// The copied seed statement that replaced the faulty statement
    private UndoEdit undoEdit;			// Memento to undo the text edit performed on the this.document.
	
	public ReplacementMutation(HashMap<String, DocumentASTRewrite> sourceFileContents, SourceStatement faulty, SourceStatement seed){
		super(sourceFileContents, faulty, seed);
	}

	/**
	 * Replaces the faulty statement with the seed statement.
	 */
	@Override
	public void concreteMutate() throws Exception {
		ASTNode parent = faulty.statement.getParent();
		
		/* Start by assuming all parents are block statements. Later we can search for an ancestor that
		 * is a Block statement */
		AST ast = faulty.statement.getRoot().getAST();

		System.out.println("Applying replacement mutation...");
		System.out.println("Faulty: " + this.faulty.statement);
		System.out.println("Seed: " + this.seed.statement);

		if(parent instanceof Block){

            /* Make a copy of the seed statement and base it in the faulty statement's AST. */
            this.replacementNode = ASTNode.copySubtree(ast, seed.statement);
            
            /* Replace the faulty statement with the seed statement. */
            rewrite.replace(faulty.statement, this.replacementNode, null);
            
            /* Modify the source code file. */
            try{
            	this.docrwt.resetModifiedDocument(); // Start with the original document to avoid the AST-doesn't-match-doc error.
                TextEdit edits = rewrite.rewriteAST(this.docrwt.modifiedDocument, null);
                this.undoEdit = edits.apply(this.docrwt.modifiedDocument, TextEdit.CREATE_UNDO);
            } catch(Exception e){
            	System.out.print("=========");
            	System.out.print(this.faulty.statement.getRoot());
            	System.out.print("=========");
            	System.out.print(this.document.get());
            	System.out.print("=========");
            	throw e;
            }
		}
	}
	
	/**
	 * Replace the seed statement with the faulty statement.
	 */
	@Override
	public void concreteUndo() throws Exception{
		if(this.undoEdit == null) return; // Nothing to do.
        
        /* Undo the edit to the AST. */
		//CompilationUnit cu = ((CompilationUnit) this.replacementNode.getRoot());
		this.rewrite.replace(this.replacementNode, this.faulty.statement, null);

		/* We need to write the undo changes back to the source file because of recursion. */
        this.docrwt.resetModifiedDocument(); // Start with the original document to avoid the AST-doesn't-match-doc error.
        TextEdit edits = rewrite.rewriteAST(this.docrwt.modifiedDocument, null);
        this.undoEdit = edits.apply(this.docrwt.modifiedDocument, TextEdit.CREATE_UNDO);
        //this.undoEdit.apply(this.docrwt.modifiedDocument);
        this.undoEdit = null;
	}
}
