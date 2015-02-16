package ca.uwaterloo.ece.qhanam.jrsrepair.context;

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

import ca.uwaterloo.ece.qhanam.jrsrepair.AbstractTestExecutor;
import ca.uwaterloo.ece.qhanam.jrsrepair.AntTestExecutor;
import ca.uwaterloo.ece.qhanam.jrsrepair.BashTestExecutor;
import ca.uwaterloo.ece.qhanam.jrsrepair.DocumentASTRewrite;
import ca.uwaterloo.ece.qhanam.jrsrepair.LineCoverage;
import ca.uwaterloo.ece.qhanam.jrsrepair.Statements;
import ca.uwaterloo.ece.qhanam.jrsrepair.Utilities;
import ca.uwaterloo.ece.qhanam.jrsrepair.compiler.JavaJDKCompiler;

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
         * Set Up Common Structures from Properties
         */

		/* Get the random seed to use in our random number generators.
		 * Default = 1*/
        long randomSeed = 1;
        if(properties.containsKey("random_seed"))
        		randomSeed = Integer.parseInt(properties.getProperty("random_seed"));
		Random random = new Random(randomSeed);

		/* Initialize the scope. This is the lightweight scope check of the two checks. */
		HashMap<String, HashSet<String>> scope = new HashMap<String, HashSet<String>>();

		/* Build the lists of faulty and seed statements. */
		Statements faultyStatements = new Statements(scope, random.nextLong());
		Statements seedStatements = new Statements(scope, random.nextLong());

    	/* Get the path settings for parsing the AST (and resolving bindings) and compiling. */
		if(!properties.containsKey("sourcepath")) throw new Exception("Parameter 'sourcepath' not found in properties");
		if(!properties.containsKey("classpath")) throw new Exception("Parameter 'classpath' not found in properties");
    	String[] sourcepaths = unpackArray(properties.getProperty("sourcepath"));
    	String[] classpaths = unpackArray(properties.getProperty("classpath"));

		/* Get the list of source files for us to mutate. */
		String[] sourceFilesArray = ContextFactory.getSourceFiles(sourcepaths);
		
		/* Load the source code from the .java files. */
		HashMap<String, DocumentASTRewrite> sourceFileContents = ContextFactory.buildSourceDocumentMap(sourceFilesArray);

        /* ***
         * Set Up Repair Context
         */
		
		RepairContext repair = ContextFactory.buildRepairContext(properties);

        /* ***
         * Set Up Parser Context
         */
		
		ParserContext parser = ContextFactory.buildParserContext(properties, scope, sourceFileContents, classpaths, sourcepaths, sourceFilesArray, faultyStatements, seedStatements);

        /* ***
         * Set Up Mutation Context
         */

        MutationContext mutation = ContextFactory.buildMutationContext(faultyStatements, seedStatements, sourceFileContents, random);
		
		/* ***
		 * Set Up Compiler Context
		 */
		
		CompilerContext compiler = ContextFactory.buildCompilerContext(properties, classpaths, sourcepaths, sourceFileContents);

		/* ***
		 * Set Up Test Context
		 */
		
		TestContext test = ContextFactory.buildTestContext(properties);

        /* ***
         * Return the context.
         */
		
		return new Context(repair, parser, mutation, compiler, test);
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
	 * A factory method that sets up the JDT parser from the details provided
	 * by the properties file.
	 * @param properties
	 * @param scope
	 * @param sourceFileContents
	 * @param classpaths
	 * @param sourcepaths
	 * @param sourceFilesArray
	 * @param faultyStatements
	 * @param seedStatements
	 * @return
	 * @throws Exception
	 */
	private static ParserContext buildParserContext(Properties properties,
													HashMap<String, HashSet<String>> scope,
													HashMap<String, DocumentASTRewrite> sourceFileContents,
													String[] classpaths, String[] sourcepaths,
													String[] sourceFilesArray,
													Statements faultyStatements, Statements seedStatements) throws Exception {

		/* Get the coverage files from fault localization. */
		
		if(!properties.containsKey("faulty_coverage")) throw new Exception("Parameter 'faulty_coverage' not found in properties");
		if(!properties.containsKey("seed_coverage")) throw new Exception("Parameter 'seed_coverage' not found in properties");

        File faultyCoverageFile = new File(properties.getProperty("faulty_coverage"));
        File seedCoverageFile = new File(properties.getProperty("faulty_coverage"));

		/* Load the line coverage files. */
		LineCoverage faultyLineCoverage = new LineCoverage(faultyCoverageFile);
		LineCoverage seedLineCoverage = new LineCoverage(seedCoverageFile);
		
		/* Build a ParserContext object. */
		return new ParserContext(scope, 
                                 sourceFileContents,
                                 classpaths, sourcepaths, 
                                 sourceFilesArray, 
                                 faultyLineCoverage, seedLineCoverage, 
    					 		 faultyStatements, seedStatements);
	}
	
	/**
	 * A factory method that builds the appropriate Compiler from the details
	 * provided by the properties file.
	 * @param properties
	 * @param classpaths
	 * @param sourcepaths
	 * @param sourceFileContents
	 * @return
	 * @throws Exception
	 */
	private static CompilerContext buildCompilerContext(Properties properties, String[] classpaths, String[] sourcepaths, HashMap<String, DocumentASTRewrite> sourceFileContents) throws Exception {

        /* Get the location for the class files. */
		if(!properties.containsKey("class_directory")) throw new Exception("Parameter 'class_directory' not found in properties");
    	String classDirectory = properties.getProperty("class_directory");
		
    	/* Make the compiler we will use. */
        JavaJDKCompiler compiler = new JavaJDKCompiler(classDirectory, classpaths, sourceFileContents, sourcepaths);
        
        return new CompilerContext(compiler);
	}
	
	/**
	 * A factory method that sets up MutationContext from the details provided
	 * by the properties file.
	 * @param properties The user specified properties.
	 * @param faultyStatements The list of faulty statements.
	 * @param seedStatements The list of seed statements.
	 * @param random The random number generator.
	 * @return
	 * @throws Exception Throws an exception if properties are missing or not
	 * 					 formatted properly.
	 */
	private static MutationContext buildMutationContext(Statements faultyStatements, 
														Statements seedStatements, 
                                                        HashMap<String, DocumentASTRewrite> sourceFileContents,
														Random random) throws Exception {

		/* Build a MutationContext object. */
		return new MutationContext(sourceFileContents, faultyStatements, seedStatements, random);
	}
	
	/**
	 * A factory method that builds the appropriate TestExecutor from the details
	 * provided by the properties file. 
	 * @param properties The properties file provided by the user.
	 * @return The appropriate concrete instance of AbstractTestExecutor.
	 * @throws Exception Throws an exception if a parameter in the properties file
	 * 					 is missing.
	 */
	private static TestContext buildTestContext(Properties properties) throws Exception {
		AbstractTestExecutor testExecutor;
		
		if(!properties.containsKey("test_script")) throw new Exception("Parameter 'test_script' not found in properties");
		String testScript = properties.getProperty("test_script");
		
		switch(TestScript.valueOf(testScript)){
            case ANT:
                if(!properties.containsKey("ant_base_dir")) throw new Exception("Parameter 'ant_base_dir' not found in properties");
                if(!properties.containsKey("ant_path")) throw new Exception("Parameter 'ant_path' not found in properties");
                if(!properties.containsKey("ant_test_target")) throw new Exception("Parameter 'ant_test_target' not found in properties");

                testExecutor = new AntTestExecutor(new File(properties.getProperty("ant_base_dir")), 
                                                   properties.getProperty("ant_path"), 
                                                   properties.getProperty("ant_test_target"));
            	break;
            case BASH:
                if(!properties.containsKey("bash_script_base_dir")) throw new Exception("Parameter 'bash_script_base_dir' not found in properties");
                if(!properties.containsKey("bash_script_path")) throw new Exception("Parameter 'bash_script_path' not found in properties");
                
                testExecutor = new BashTestExecutor(new File(properties.getProperty("bash_script_base_dir")),
                									properties.getProperty("bash_script_path"));
            	break;
			default:
				throw new Exception("Unknown test script type: " + testScript);
		}
		
		/* Build a TestContext object. */
		return new TestContext(testExecutor);
	}

	/**
	 * Options for the type of TestExecutor to build.
	 * 	ANT: Apache Ant will execute the junit test cases.
	 * 	BASH: A custom Bash shell script will execute the junit test cases.
	 * @author qhanam
	 */
	private enum TestScript { ANT, BASH }

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
