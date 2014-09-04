package ca.uwaterloo.ece.qhanam.jrsrepair;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Document;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

/**
 * Stores a Document/ASTRewrite pair so that we can synchronize changes to Documents
 * and ASTs.
 * @author qhanam
 *
 */
public class DocumentASTRewrite {
	public IDocument document;
	public IDocument modifiedDocument;
	public ASTRewrite rewriter;
	private boolean modified;
	private boolean tainted;

	public DocumentASTRewrite(IDocument document, ASTRewrite rewriter){
		this.document = document;
		this.rewriter = rewriter;
		this.tainted = false;
		this.modified = false;
		
		this.resetModifiedDocument();
	}
	
	public void taintDocument(){
		this.modified = true;
		this.tainted = true;
	}
	
	public void untaintDocument(){
		this.tainted = false;
	}
	
	public boolean isDocumentTainted(){
		return this.tainted;
	}
	
	public void resetModifiedDocument(){
		this.modifiedDocument = new Document(document.get());
	}
	
	public boolean isDocumentModified(){
		return this.modified;
	}
}
