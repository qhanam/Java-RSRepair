package ca.uwaterloo.ece.qhanam.jrsrepair;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

/**
 * ContextFactory sets up the context for JRSRepair with the createContext 
 * method. Context contains all the context classes required by JRSRepair.
 * 
 * The input to ContextFactory.buildMutationContext is the Properties object
 * from the user-specified properties file.
 * 
 * @author qhanam
 */
public class ContextFactory {
	
	/**
	 * Builds the context classes for the repair task. The context classes
	 * are stored in the main context class Context.
	 * 
	 * @param properties The Properties given as input by the user.
	 * @return The context for the repair task.
	 * @throws Exception Throws an exception when a property is missing
	 * 					 or not formatted properly.
	 */
	public static Context buildContext(Properties properties) throws Exception {

        /* ***
         * Set Up Common Structures
         */

		/* Get the random seed to use in our random number generators.
		 * Default = 1*/

        long randomSeed = 1;
        if(properties.containsKey("random_seed"))
        		randomSeed = Integer.parseInt(properties.getProperty("random_seed"));
        
        /* Build the random number generator. */
        
		Random random = new Random(randomSeed);

		/* Initialize the scope. This is the lightweight scope check of the two checks. */

		HashMap<String, HashSet<String>> scope = new HashMap<String, HashSet<String>>();

		/* Build the lists of faulty and seed statements. */

		Statements faultyStatements = new Statements(scope, random.nextLong());
		Statements seedStatements = new Statements(scope, random.nextLong());

        /* ***
         * Set Up Repair Context
         */
		
		RepairContext repair = ContextFactory.buildRepairContext(properties);
        
        /* ***
         * Set Up Mutation Context
         */

        MutationContext mutation = ContextFactory.buildMutationContext(properties, faultyStatements, seedStatements, random);

        /* ***
         * Return the context.
         */
		
		return new Context(repair, mutation);
	}
	
	/**
	 * Sets up the RepairContext
	 * @param properties The user specified properties.
	 * @return
	 * @throws Exception Throws an exception if properties are missing or not
	 * 					 formatted properly.
	 */
	private static RepairContext buildRepairContext(Properties properties)
													throws Exception {
		
        /* Get the settings for mutant generation. */

		if(!properties.containsKey("mutation_candidates")) throw new Exception("Parameter 'mutation_candidates' not found in properties");
		if(!properties.containsKey("mutation_generations")) throw new Exception("Parameter 'mutation_generations' not found in properties");
		if(!properties.containsKey("mutation_attempts")) throw new Exception("Parameter 'mutation_attempts' not found in properties");
	
        int mutationCandidates = Integer.parseInt(properties.getProperty("mutation_candidates"));
        int mutationGenerations = Integer.parseInt(properties.getProperty("mutation_generations"));
        int mutationAttempts = Integer.parseInt(properties.getProperty("mutation_attempts")); 
        
        /* Build a RepairContext object. */
        return new RepairContext(mutationCandidates, mutationGenerations, mutationAttempts);
	}
	
	
	/**
	 * Sets up the MutationContext
	 * @param properties The user specified properties.
	 * @param faultyStatements The list of faulty statements.
	 * @param seedStatements The list of seed statements.
	 * @param random The random number generator.
	 * @return
	 * @throws Exception Throws an exception if properties are missing or not
	 * 					 formatted properly.
	 */
	private static MutationContext buildMutationContext(Properties properties, 
														Statements faultyStatements, 
														Statements seedStatements, 
														Random random) throws Exception {

    	/* Get the path settings for parsing the AST. */
		if(!properties.containsKey("sourcepath")) throw new Exception("Parameter 'sourcepath' not found in properties");
    	String[] sourcepaths = unpackArray(properties.getProperty("sourcepath"));

		/* Get the list of source files for us to mutate. */
		String[] sourceFilesArray = ContextFactory.getSourceFiles(sourcepaths);
		
		/* Load the source code from the .java files. */
		HashMap<String, DocumentASTRewrite> sourceFileContents = ContextFactory.buildSourceDocumentMap(sourceFilesArray);
		
		/* Build a MutationContext object. */
		return new MutationContext(sourceFileContents, faultyStatements, seedStatements, random);
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
	 * Converts a serialized array in the format "{string1,string2,...,stringN}" 
	 * to a String array.
	 * @param packed The seralized array.
	 * @return The String[] array
	 */
	private static String[] unpackArray(String packed) throws Exception{
		String[] unpacked = null;

		if(packed.substring(0, 1).equals("{") && packed.substring(packed.length() - 1, packed.length()).equals("}")){
			packed = packed.substring(1, packed.length() - 1);
			unpacked = packed.split(",");
		}
		else throw new Exception("Array not enclosed in parenthesis '{ }', cannot unpack.");

		return unpacked;
	}

}
