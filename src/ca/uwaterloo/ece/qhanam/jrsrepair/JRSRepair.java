package ca.uwaterloo.ece.qhanam.jrsrepair;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.LinkedList;
import java.util.Collection;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import ca.uwaterloo.ece.qhanam.jrsrepair.mutation.*;

public class JRSRepair {
	
	private String[] sourcePaths;
	private File faultyCoverageFile;
	private File seedCoverageFile;

	private String[] sourceFilesArray;
    private HashMap<String, DocumentASTRewrite> sourceFileContents;
    
    private HashMap<String, HashSet<String>> scope;
    
    private Statements faultyStatements;
    private Statements seedStatements;

	private LineCoverage faultyLineCoverage;
	private LineCoverage seedLineCoverage;

	private int mutationCandidates;
	private int mutationGenerations;
	private int mutationAttempts;
	
	private Random random;
	
	private TestExecutor testExecutor;
	
	/**
	 * Creates a JRSRepair object with the path to the source folder
	 * of the program we are mutating.
	 * @param sourcePaths The path to the source folder of the program we are mutating.
	 */
	public JRSRepair(String[] sourcePaths, File faultyCoverageFile, File seedCoverageFile, 
					 int mutationCandidates, int mutationGenerations, int mutationAttempts, 
					 long randomSeed, TestExecutor testExecutor) throws Exception {
		this.scope = new HashMap<String, HashSet<String>>();
		
		this.random = new Random(randomSeed);

		this.faultyStatements = new Statements(this.scope, this.random.nextLong());
		this.seedStatements = new Statements(this.scope, this.random.nextLong());

		this.sourcePaths = sourcePaths;
		this.faultyCoverageFile = faultyCoverageFile;
		this.seedCoverageFile = seedCoverageFile;
		
		this.mutationCandidates = mutationCandidates;
		this.mutationGenerations = mutationGenerations;
		this.mutationAttempts = mutationAttempts;
		
		this.testExecutor = testExecutor;

		/* Get the list of source files for us to mutate. */
		this.sourceFilesArray = JRSRepair.getSourceFiles(this.sourcePaths);
		
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
		FileASTRequestor fileASTRequestor = new MutationASTRequestor(sourceFileContents, scope, faultyLineCoverage, seedLineCoverage, faultyStatements, seedStatements);
		
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
		
		try{
			for(int i = 0; i < this.mutationCandidates; i++) {
				System.out.println("Running candidate " + (i + 1) + " ...");
                this.mutationIteration(1);
			}
		}
		finally {
            this.restoreOriginalProgram(); // Leave the program in its original state (hopefully)
		}
	}

	/**
	 * The main method for trying a mutation. It performs all the operations needed 
	 * to mutate, compile and test the program. It is recursive and will therefore
	 * attempts multiple mutations at a time before rolling back their changes. 
	 * @param generation The number of mutations that have already been applied.
	 */
	private void mutationIteration(int generation) throws Exception{
        /* If we can't find a solution within some number of iterations, abort. */
        int attemptCounter = 0;
        
        /* Let the user know our progress. */
        System.out.println("Running generation " + generation + " ...");
        
        Mutation mutation = null;
        boolean compiled = false;
        
        /* We need to ensure the first levels compile or else the rest of the
         * mutations won't be useful. */
        do {

            /* Get a random mutation operation to apply. */
            mutation = this.getRandomMutation();
            
            /* Apply the mutation to the AST + Document. */
            mutation.mutate();
            this.writeChangesToDisk();
            
            try{
                /* Compile the program and execute the test cases. */
                compiled = this.testExecutor.runTests();
            } catch (Exception e){
                System.err.println("JRSRepair: Exception thrown during compilation/test execution.");
                System.err.println(e.getMessage());
            }
            finally { 
                /* Roll back the current mutation. */
                if(!compiled) mutation.undo(); 
            }

            attemptCounter++;

        } while(!compiled && attemptCounter < this.mutationAttempts);
        
        try{
            /* Recurse to the next level of mutations. */
            if(generation < this.mutationGenerations){ 
                this.mutationIteration(generation + 1);
            }
        } catch (Exception e) {
            System.err.println("JRSRepair: Exception thrown during mutation recursion.");
        } finally { 
            /* Roll back the current mutation. */
            if(compiled) mutation.undo();
        }
	}
	
	/**
	 * Returns a random mutation operation.
	 * @return A Mutation memento object for applying one mutation to a faulty statement.
	 */
	private Mutation getRandomMutation(){
		Mutation mutation;
		int index = (new Double(Math.ceil((this.random.nextDouble() * 3)))).intValue();
		
		switch(index){
		case 1:
			System.out.println("Applying addition mutation...");
			mutation = new AdditionMutation(sourceFileContents, faultyStatements.getRandomStatement(), seedStatements.getRandomStatement());
			break;
		case 2:
			System.out.println("Applying deletion mutation...");
			mutation = new DeletionMutation(sourceFileContents, faultyStatements.getRandomStatement(), null);
			break;
		default:
			System.out.println("Applying replacement mutation...");
			mutation = new ReplacementMutation(sourceFileContents, faultyStatements.getRandomStatement(), seedStatements.getRandomStatement());
			break;
		}
		
		return mutation;
	}

    /**
	 * Restores the program's original state.
	 * @throws Exception
	 */
	private void restoreOriginalProgram() throws Exception{
		Set<String> sourcePaths = this.sourceFileContents.keySet();
		for(String sourcePath : sourcePaths){
			DocumentASTRewrite drwt = this.sourceFileContents.get(sourcePath);
			if(drwt.isDocumentModified()){
				/* Since the document is tainted, we need to write it to disk. */
				Files.write(Paths.get(sourcePath), drwt.document.get().getBytes(), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
				drwt.untaintDocument();
			}
		}
	}
	
	/**
	 * Writes the source file changes back to disk. Only writes the documents that are marked
	 * as tainted.
	 * @throws Exception
	 */
	private void writeChangesToDisk() throws Exception{
		Set<String> sourcePaths = this.sourceFileContents.keySet();
		for(String sourcePath : sourcePaths){
			DocumentASTRewrite drwt = this.sourceFileContents.get(sourcePath);
			if(drwt.isDocumentTainted()){
				/* Since the document is tainted, we need to write it to disk. */
				Files.write(Paths.get(sourcePath), drwt.modifiedDocument.get().getBytes(), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
				drwt.untaintDocument();
			}
		}
	}
	
	/**
	 * Generates a list of java source files given a directory, or returns the
	 * file specified in an array.
	 * @param sourcePaths The path to the file/directory.
	 * @return An array of paths to Java source files.
	 * @throws Exception
	 */
	private static String[] getSourceFiles(String[] sourcePaths) throws Exception{
		Collection<File> sourceFiles;
		String[] sourceFilesArray = null;;

		for(String sourcePath : sourcePaths){
			File sourceFile = new File(sourcePath);
			
            /* If the buggy file is a directory, get all the java files in that directory. */
            if(sourceFile.isDirectory()){
                sourceFiles = FileUtils.listFiles(sourceFile, new SuffixFileFilter(".java"), TrueFileFilter.INSTANCE);
            }
            /* The buggy file may also be a source code file. */
            else{
                sourceFiles = new LinkedList<File>();
                sourceFiles.add(sourceFile);
            }
            
            /* Create the String array. */
            sourceFilesArray = new String[sourceFiles.size()];
            int i = 0;
            for(File file : sourceFiles){
                sourceFilesArray[i] = file.getCanonicalPath();
                i++;
            }
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
