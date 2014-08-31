package ca.uwaterloo.ece.qhanam.jrsrepair;

import java.util.LinkedList;
import java.util.Collection;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import ca.uwaterloo.ece.qhanam.jrsrepair.mutation.*;

public class JRSRepair {
	
	private File sourcePath;
	private File faultyCoverageFile;
	private File seedCoverageFile;

	private String[] sourceFilesArray;
    private HashMap<String, DocumentASTRewrite> sourceFileContents;
    
    private Statements faultyStatements;
    private Statements seedStatements;

	private LineCoverage faultyLineCoverage;
	private LineCoverage seedLineCoverage;

	private int mutationIterations;
	private int mutationDepth;
	
	/**
	 * Creates a JRSRepair object with the path to the source folder
	 * of the program we are mutating.
	 * @param sourcePath The path to the source folder of the program we are mutating.
	 */
	public JRSRepair(File sourcePath, File faultyCoverageFile, File seedCoverageFile, int mutationIterations, int mutationDepth) throws Exception{
		this.faultyStatements = new Statements();
		this.seedStatements = new Statements();

		this.sourcePath = sourcePath;
		this.faultyCoverageFile = faultyCoverageFile;
		this.seedCoverageFile = seedCoverageFile;
		
		this.mutationIterations = mutationIterations;
		this.mutationDepth = mutationDepth;

		/* Get the list of source files for us to mutate. */
		this.sourceFilesArray = JRSRepair.getSourceFiles(this.sourcePath);
		
		/* Load the source code from the .java files. */
		this.sourceFileContents = JRSRepair.buildSourceDocumentMap(sourceFilesArray);
		
		/* Load the line coverage. */
		this.faultyLineCoverage = new LineCoverage(this.faultyCoverageFile);
		this.seedLineCoverage = new LineCoverage(this.seedCoverageFile);
	}
	
	/**
	 * Builds ASTs for all the source files.
	 */
	public void buildASTs() throws Exception{
		/* Create the ASTParser with the source files to generate ASTs for, and set up the
		 * environment using ASTParser.setEnvironment.
		 */
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		
		/* setEnvironment(
		 * String[] classpathEntries,
		 * String[] sourcepathEntries, 
		 * String[] encodings,
		 * boolean includeRunningVMBootclasspath) */
		parser.setEnvironment(new String[] {}, sourceFilesArray, null, false);
		parser.setResolveBindings(false); // Throws an error when set to 'true' for some reason.
		
		/* Set up the AST handler. We need to create LineCoverage and Statements classes to store 
		 * and filter the statements from the ASTs. */
		FileASTRequestor fileASTRequestor = new MutationASTRequestor(sourceFileContents, faultyLineCoverage, seedLineCoverage, faultyStatements, seedStatements);
		
		/* createASTs(
		 * String[] sourceFilePaths, 
		 * String[] encodings, - the source file encodings (e.g., "ASCII", "UTF8", "UTF-16"). Can be set to null if platform encoding is sufficient.
		 * String[] bindingKeys, 
		 * FileASTRequestor requestor, 
		 * IProgressMonitor monitor) */
		parser.createASTs(sourceFilesArray, null, new String[] {}, fileASTRequestor, null);
	}
	
	/**
	 * Attempts to repair the program using the RSRepair method.
	 * @throws Exception
	 */
	public void repair() throws Exception{
		if(this.sourceFileContents.isEmpty() || this.faultyStatements.isEmpty() || this.seedStatements.isEmpty()) throw new Exception("The ASTs have not been built.");
		this.mutationIteration(0);
	}

	/**
	 * The main method for trying a mutation. It performs all the operations needed 
	 * to mutate, compile and test the program. It is recursive and will therefore
	 * attempts multiple mutations at a time before rolling back their changes. 
	 * @param depth The number of mutations that have already been applied.
	 */
	private void mutationIteration(int depth) throws Exception{
		for(int i = 0; i < this.mutationIterations; i++){ 
			/* Get a random mutation operation to apply. */
			Mutation mutation = this.getRandomMutation();
			
			/* Apply the mutation to the AST + Document. */
			mutation.mutate();
			
			/* Compile the program and execute the test cases. */
			this.testCurrentMutation();
			
			// Store the results
			
			/* Recurse to the next level of mutations. */
			if(depth < this.mutationDepth){ 
				this.mutationIteration(depth + 1);
			}
			
			/* Roll back the current mutation. */
			mutation.undo();
		}
	}
	
	/**
	 * Returns a random mutation operation.
	 * @return A Mutation memento object for applying one mutation to a faulty statement.
	 */
	private Mutation getRandomMutation(){
		Mutation mutation;
		int index = (new Double(Math.ceil((Math.random() * 3)))).intValue();
		
		switch(index){
		case 1:
			mutation = new AdditionMutation(sourceFileContents, faultyStatements.getRandomStatement(), seedStatements.getRandomStatement());
			break;
		case 2:
			mutation = new DeletionMutation(sourceFileContents, faultyStatements.getRandomStatement(), seedStatements.getRandomStatement());
			break;
		default:
			mutation = new ReplacementMutation(sourceFileContents, faultyStatements.getRandomStatement(), seedStatements.getRandomStatement());
			break;
		}
		
		return mutation;
	}
	
	/* TODO: Should we move this to another class or is it ok here? */
	private void testCurrentMutation(){
		/* TODO: Write the source file changes back to disk. Maybe use taint tracking for this? */
		
		/* TODO: Compile and execute the program. */
	}
	
	/**
	 * Generates a list of java source files given a directory, or returns the
	 * file specified in an array.
	 * @param sourcePath The path to the file/directory.
	 * @return An array of paths to Java source files.
	 * @throws Exception
	 */
	private static String[] getSourceFiles(File sourcePath) throws Exception{
		Collection<File> sourceFiles;
		String[] sourceFilesArray;

		/* If the buggy file is a directory, get all the java files in that directory. */
		if(sourcePath.isDirectory()){
			sourceFiles = FileUtils.listFiles(sourcePath, new SuffixFileFilter(".java"), TrueFileFilter.INSTANCE);
		}
		/* The buggy file may also be a source code file. */
		else{
			sourceFiles = new LinkedList<File>();
			sourceFiles.add(sourcePath);
		}
		
		/* Create the String array. */
		sourceFilesArray = new String[sourceFiles.size()];
		int i = 0;
		for(File sourceFile : sourceFiles){
			sourceFilesArray[i] = sourceFile.getCanonicalPath();
			i++;
		}
		
		return sourceFilesArray;
	}
	
	/**
	 * Builds a HashMap with Java file paths as keys and Java file text contents as values.
	 * @param sourceFilesArray
	 * @return A HashMap containing the text of the source Java files.
	 */
	private static HashMap<String, DocumentASTRewrite> buildSourceDocumentMap(String[] sourceFilesArray) throws Exception{
		HashMap<String, DocumentASTRewrite> map = new HashMap<String, DocumentASTRewrite>();
		for(String sourceFile : sourceFilesArray){
            byte[] encoded = Files.readAllBytes(Paths.get(sourceFile));
            IDocument contents = new Document(new String(encoded));
            DocumentASTRewrite docrw = new DocumentASTRewrite(contents, null);
            map.put(sourceFile, docrw);
		}
		return map;
	}
		
}
