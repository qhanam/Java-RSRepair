package ca.uwaterloo.ece.qhanam.jrsrepair.mutation;

import ca.uwaterloo.ece.qhanam.jrsrepair.SourceStatement;
import ca.uwaterloo.ece.qhanam.jrsrepair.DocumentASTRewrite;

import java.util.HashMap;

import org.eclipse.jdt.core.dom.*;
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
	public void mutate() throws Exception {
		ASTNode parent = faulty.statement.getParent();
		
		/* Start by assuming all parents are block statements. Later we can search for an ancestor that
		 * is a Block statement */
		AST ast = faulty.statement.getRoot().getAST();

		System.out.println("------- Replacement Mutation");
		System.out.println(ASTNode.nodeClassForType(parent.getNodeType()) + ": " + faulty.statement.getLocationInParent());
		System.out.println(ASTNode.nodeClassForType(faulty.statement.getNodeType()));
		System.out.println(ASTNode.nodeClassForType(seed.statement.getNodeType()));
		
		if(parent instanceof Block){

            System.out.println("Faulty: " + faulty.statement + " (" + faulty.sourceFile + ")");
            System.out.println("Seed: " + seed.statement + " (" + seed.sourceFile + ")");
            
            /* Make a copy of the seed statement and base it in the faulty statement's AST. */
            this.replacementNode = ASTNode.copySubtree(ast, seed.statement);
            
            /* Replace the faulty statement with the seed statement. */
            rewrite.replace(faulty.statement, this.replacementNode, null);
            
            /* Modify the source code file. */
            TextEdit edits = rewrite.rewriteAST(this.document, null);
            this.undoEdit = edits.apply(this.document, TextEdit.CREATE_UNDO);

            System.out.print(this.document.get());
		}
	}
	
	/**
	 * Replace the seed statement with the faulty statement.
	 */
	@Override
	public void undo() throws Exception{
		if(this.undoEdit == null) return; // Nothing to do.
        
        /* Undo the edit to the AST. */
		this.rewrite.replace(this.replacementNode, this.faulty.statement, null);
        this.undoEdit.apply(this.document);
        this.undoEdit = null;
        
        System.out.print(this.document.get());
	}

}
