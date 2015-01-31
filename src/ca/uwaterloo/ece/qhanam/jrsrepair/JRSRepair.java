package ca.uwaterloo.ece.qhanam.jrsrepair;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.LinkedList;
import java.util.Collection;
import java.util.Stack;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import ca.uwaterloo.ece.qhanam.jrsrepair.compiler.JavaJDKCompiler;
import ca.uwaterloo.ece.qhanam.jrsrepair.mutation.*;

public class JRSRepair {
	
	private String[] sourcepaths;
	private File faultyCoverageFile;
	private File seedCoverageFile;

	private String[] classpaths;

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
	
	private JavaJDKCompiler compiler;
	private TestExecutor testExecutor;
	
	private Stack<String> patches;
	private File buildDirectory;
	
	private ASTParser parser;
	
	/* currentMutation is used to keep track of what mutation has been chosen for a 
	 * mutation generation. This is needed because some mutations are more likely to
	 * compile than others. For example, a deletion operation is more likely to 
	 * compile than an insertion because an insertion may introduce variables that
	 * are out of scope. This has been mitigated a bit with better scope checking, 
	 * but is still good practice to ensure truly random mutations.
	 */
	private Integer currentMutation;
	
	/**
	 * Creates a JRSRepair object with the path to the source folder
	 * of the program we are mutating.
	 * @param sourcepaths The path to the source folder of the program we are mutating.
	 */
	public JRSRepair(String[] sourcepaths, String[] classpaths, File faultyCoverageFile, File seedCoverageFile, 
					 int mutationCandidates, int mutationGenerations, int mutationAttempts, 
					 long randomSeed, File buildDirectory, JavaJDKCompiler compiler, 
					 TestExecutor testExecutor) throws Exception {

		this.scope = new HashMap<String, HashSet<String>>();
		
		this.random = new Random(randomSeed);

		this.faultyStatements = new Statements(this.scope, this.random.nextLong());
		this.seedStatements = new Statements(this.scope, this.random.nextLong());

		this.classpaths = classpaths;
		this.sourcepaths = sourcepaths;
		this.faultyCoverageFile = faultyCoverageFile;
		this.seedCoverageFile = seedCoverageFile;
		
		this.mutationCandidates = mutationCandidates;
		this.mutationGenerations = mutationGenerations;
		this.mutationAttempts = mutationAttempts;
		
		this.compiler = compiler;
		this.testExecutor = testExecutor;

		/* Get the list of source files for us to mutate. */
		this.sourceFilesArray = JRSRepair.getSourceFiles(this.sourcepaths);
		
		/* Load the source code from the .java files. */
		this.sourceFileContents = JRSRepair.buildSourceDocumentMap(sourceFilesArray);
		
		/* Load the line coverage. */
		this.faultyLineCoverage = new LineCoverage(this.faultyCoverageFile);
		this.seedLineCoverage = new LineCoverage(this.seedCoverageFile);
		
		/* Initialize the patch-building stack. */
		this.patches = new Stack<String>();
		this.buildDirectory = buildDirectory;
		
		/* Initialize the compiler with context. */
		this.compiler.setContext(this.sourceFileContents, this.sourcepaths);
		
		this.currentMutation = null;
		
		this.parser = null; // Initialize to null since all the parsing is done in buildASTs();
	}
	
	/**
	 * Builds ASTs for all the source files.
	 */
	public void buildASTs() throws Exception{
		/* Create the ASTParser with the source files to generate ASTs for, and set up the
		 * environment using ASTParser.setEnvironment.
		 */
		this.parser = ASTParser.newParser(AST.JLS8);
		
		/* setEnvironment(
		 * String[] classpathEntries,
		 * String[] sourcepathEntries, 
		 * String[] encodings,
		 * boolean includeRunningVMBootclasspath) */
		parser.setEnvironment(this.classpaths, this.sourcepaths, null, true); 
		parser.setResolveBindings(true); // ISSUE: Throws an error when set to 'true' for some reason. SOLVED: was passing list of source files, not the source directory.
		
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
                this.mutationIteration(i + 1, 1);
			}
		}
		finally {
            System.out.println("Finished!");
		}
	}

	/**
	 * The main method for trying a mutation. It performs all the operations needed 
	 * to mutate, compile and test the program. It is recursive and will therefore
	 * attempt multiple mutations at a time before rolling back their changes. 
	 * 
	 * @param candidate The identifier for the current set of mutations.
	 * @param generation The number of mutations that have already been applied for the current candidate.
	 */
	private void mutationIteration(int candidate, int generation) throws Exception{
        /* If we can't find a solution within some number of iterations, abort. */
        int attemptCounter = 0;
        
        /* Let the user know our progress. */
        System.out.println("Running generation " + generation + " ...");
        
        Mutation mutation = null;
        TestStatus compiled = TestStatus.NOT_COMPILED;
        
        /* We need to ensure the first levels compile or else the rest of the
         * mutations won't be useful. */
        do {
            compiled = TestStatus.NOT_COMPILED;
            
            /* First we need to get a mutation that will likely compile. To do this
             * we randomly select a mutation, apply the mutation to the AST to get
             * a new document, parse the new document into an AST (having the parser
             * attempt to resolve bindings) and finally check that all variables have
             * bindings.*/

            do{
            	if(mutation != null && mutation.mutated()) mutation.undo();
            	
                /* Get a random mutation operation to apply. */
                mutation = this.getRandomMutation();
                
                /* Apply the mutation to the AST + Document. */
                mutation.mutate();
                
                /* Check if all the variables are in scope in the new AST. */
            } while(!this.checkScope(mutation.getRewriter()));
            
            this.logMutation(mutation);
            
            /* Now that we have a mutation that is in-scope, we attempt to compile 
             * the program. If the program compiles, we run the test cases. If it
             * doesn't, we roll back the changes and loop to get another mutation. */

            compiled = this.compiler.compile();
            
            try{
                /* Compile the program and execute the test cases. */
                if(compiled == TestStatus.COMPILED) compiled = this.testExecutor.runTests();
            } catch (Exception e){
                System.err.println("JRSRepair: Exception thrown during test execution.");
                System.err.println(e.getMessage());
            }
            finally { 
                /* Roll back the current mutation. */
                if(compiled == TestStatus.NOT_COMPILED) {
                    System.out.print(" - Did not compile\n");
                    mutation.undo(); 
                } else {
                    this.patches.push("Candidate " + candidate + ", Generation " + generation + "\n" + mutation.toString());
                    System.out.print(" - Compiled!");
                }
            }

            attemptCounter++;

        } while(compiled == TestStatus.NOT_COMPILED && attemptCounter < this.mutationAttempts);
        
        this.currentMutation = null;
        
        if(compiled == TestStatus.TESTS_PASSED) {
            this.logSuccesfullPatch(candidate, generation);
            System.out.print(" Passed!\n");
        }
        else if(compiled == TestStatus.TESTS_FAILED) System.out.print("\n");
    
        /* Recurse to the next level of mutations. */
        if(generation < this.mutationGenerations){ 
            this.mutationIteration(candidate, generation + 1);
        }

        /* Since this.patches in a field, we need to unwind the patch stack. */
        if(compiled == TestStatus.TESTS_FAILED || compiled == TestStatus.TESTS_PASSED) {
            this.patches.pop();
            mutation.undo();
        }
	}
	
	/**
	 * Writes the mutation operations to a file. These represent a (successful?) fix.
	 * @throws Exception
	 */
	private void logSuccesfullPatch(int candidate, int generation){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		Date date = new Date();
		File file = new File(this.buildDirectory + "/patches", "Candidate" + candidate + "_Generation" + generation + "_" + dateFormat.format(date));
		BufferedWriter out = null;
		
		/* Store the .class files for the program so we can verify. */
		this.compiler.storeCompiled(this.buildDirectory + "/classes_Candidate" + candidate + "_Generation" + generation + "_" + dateFormat.format(date));
		
		/* Log the mutation events that produced the patch. */
        try{
            file.createNewFile();
            out = new BufferedWriter(new FileWriter(file));
            for(String s : this.patches){
                out.write(s);
            }
        } catch(Exception e) { }

        try{
            if(out != null) out.close();
        } catch(Exception ignore) { System.out.println("Problem closing writer for " + file.getName() + "."); }
	}
	
	/**
	 * Returns a random mutation operation.
	 * 
	 * @return A Mutation memento object for applying one mutation to a faulty statement.
	 */
	private Mutation getRandomMutation(){
		SourceStatement faultyStatement;
		Mutation mutation;
		if(this.currentMutation == null) this.currentMutation = (new Double(Math.ceil((this.random.nextDouble() * 3)))).intValue();
		
		switch(this.currentMutation){
		case 100: // Use for testing (e.g., to make sure the program compiles without mutations).
			System.out.print("Applying null mutation...");
			mutation = new NullMutation(sourceFileContents, faultyStatements.getRandomStatement(), null);
			break;
		case 1:
			System.out.print("Applying addition mutation...");
			faultyStatement = faultyStatements.getRandomStatement();
			mutation = new AdditionMutation(sourceFileContents, faultyStatement, seedStatements.getRandomStatement(faultyStatement));
			break;
		case 2:
			System.out.print("Applying replacement mutation...");
			faultyStatement = faultyStatements.getRandomStatement();
			mutation = new ReplacementMutation(sourceFileContents, faultyStatement, seedStatements.getRandomStatement(faultyStatement));
			break;
		default:
			System.out.print("Applying deletion mutation...");
			mutation = new DeletionMutation(sourceFileContents, faultyStatements.getRandomStatement(), null);
			break;
		}
		
		return mutation;
	}
	
	/**
	 * Generates a list of java source files given a directory, or returns the
	 * file specified in an array.
	 * @param sourcePaths The path to the file/directory.
	 * @return An array of paths to Java source files.
	 * @throws Exception
	 */
	private static String[] getSourceFiles(String[] sourcePaths) throws Exception{
		Collection<File> sourceFiles = new LinkedList<File>();
		String[] sourceFilesArray = null;

		for(String sourcePath : sourcePaths){
			File sourceFile = new File(sourcePath);
			
            /* If the buggy file is a directory, get all the java files in that directory. */
            if(sourceFile.isDirectory()){
                sourceFiles.addAll(FileUtils.listFiles(sourceFile, new SuffixFileFilter(".java"), TrueFileFilter.INSTANCE));
            }
            /* The buggy file may also be a source code file. */
            else{
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
			File backingFile = new File(sourceFile);
            byte[] encoded = Utilities.readFromFile(backingFile);
            IDocument contents = new Document(new String(encoded));
            DocumentASTRewrite docrw = new DocumentASTRewrite(contents, backingFile, null);
            map.put(sourceFile, docrw);
		}
		return map;
	}

	/**
	 * Checks that the AST produced by the mutation has all variables in-scope.
	 * 
	 * To do this, we create a new AST by parsing the mutated source and compute
	 * bindings. We then look to see if there are any bindings missing. If bindings
	 * are missing, there is something not in-scope and the program will not compile.
	 * 
	 * @param rewriter The AST rewriter that contains the mutated document.
	 */
	private boolean checkScope(DocumentASTRewrite rewriter) {
		String source = rewriter.modifiedDocument.get();

		this.parser.setSource(source.toCharArray());
		this.parser.setEnvironment(this.classpaths, this.sourcepaths, null, true); 
		this.parser.setResolveBindings(true);
		this.parser.setUnitName(rewriter.backingFile.getName());

		ASTNode node = this.parser.createAST(null);

		ScopeASTVisitor scopeASTVisitor = new ScopeASTVisitor();
		node.accept(scopeASTVisitor);

		if(!scopeASTVisitor.inScope){
            System.out.println(" - Some variables are out of scope.");
            return false;
		}

		return true;
	}
	
    /**
     * Temp method for debugging.
	 * @throws Exception
	 */
	public void logMutation(Mutation m) throws Exception{
		try{
			Utilities.writeToFileAppend(new File(this.buildDirectory + "/log"), 
									   m.toString().getBytes());
		} catch (Exception e){
			System.out.println(e.getMessage());
			throw e;
		}
	}
	
	/**
	 * Used for keeping track of compilation and test progress.
	 */
	public enum TestStatus{
		NOT_COMPILED, COMPILED, TESTS_FAILED, TESTS_PASSED
	}

	/**
	 * Checks that each variable, field, method and type has a binding. If
	 * one of these does not have a binding, we assume it is out of scope.
	 * This is used for checking to make sure mutated ASTs will be compilable.
	 * @author qhanam
	 */
	private class ScopeASTVisitor extends ASTVisitor{

		public boolean inScope;
		
		public ScopeASTVisitor(){ 
			this.inScope = true;
		}

		/**
		 * Check that each QualifiedName has a binding. This
		 * will ensure that the fully qualified name gets
		 * checked instead of it's parts.
		 */
		public boolean visit(QualifiedName qn){
	
			if(!this.inScope) return false; // No point in checking if we're already out of scope

			if(qn.resolveBinding() == null) {
				this.inScope = false;
			}
			
			return false;
		}

		/**
		 * Check that each SimpleName ASTNode has a binding. These
		 * will be variables, fields, methods and types.
		 */
		public boolean visit(SimpleName s) {

			if(!this.inScope) return false; // No point in checking if we're already out of scope

			if(s.resolveBinding() == null) {
				this.inScope = false;
			}
			
			return false;
		}
	}
}
