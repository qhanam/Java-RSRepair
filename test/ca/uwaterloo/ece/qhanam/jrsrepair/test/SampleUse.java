package ca.uwaterloo.ece.qhanam.jrsrepair.test;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

import ca.uwaterloo.ece.qhanam.jrsrepair.*;
import ca.uwaterloo.ece.qhanam.jrsrepair.compiler.JavaJDKCompiler;

/**
 * This class implements a program that attempts to automatically fix a
 * faulty program using JRSRepair. It takes one argument, the path to the
 * configuration file that is used to configure JRSRepair.
 * 
 * @author qhanam
 */
public class SampleUse {
	
	public static void main(String[] args) throws Exception {
		if(args.length > 0){
			JRSRepair repair = readConfigFile(new File(args[0]));
            repair.buildASTs();
            repair.repair();
		}
		else{
			System.out.println("Use: java SampleUse [/path/to/jrsrepair.properties]");
		}
	}
	
	/**
	 * Reads the configuration file and sets up the compiler, ant junit tests and JRSRepair.
	 * @param config The .properties file.
	 * @return An instance of JRSRepair configures using the settings in the .properties file.
	 * @throws Exception Throws an exception if a parameter is missing or incorrect.
	 */
	public static JRSRepair readConfigFile(File config) throws Exception{
		
		Properties properties = new Properties();
		properties.load(new FileReader(config));
		
        TestExecutor testExecutor;
        JavaJDKCompiler compiler;
		JRSRepair repair;

		/* Get the coverage files from fault localization. */
		
		if(!properties.containsKey("faulty_coverage")) throw new Exception("Parameter 'faulty_coverage' not found in properties");
		if(!properties.containsKey("seed_coverage")) throw new Exception("Parameter 'seed_coverage' not found in properties");

        File faultyCoverage = new File(properties.getProperty("faulty_coverage"));
        File seedCoverage = new File(properties.getProperty("faulty_coverage"));
        
        /* Get the settings for mutant generation. */

		if(!properties.containsKey("mutation_candidates")) throw new Exception("Parameter 'mutation_candidates' not found in properties");
		if(!properties.containsKey("mutation_generations")) throw new Exception("Parameter 'mutation_generations' not found in properties");
		if(!properties.containsKey("mutation_attempts")) throw new Exception("Parameter 'mutation_attempts' not found in properties");
	
        int mutationCandidates = Integer.parseInt(properties.getProperty("mutation_candidates"));
        int mutationGenerations = Integer.parseInt(properties.getProperty("mutation_generations"));
        int mutationAttempts = Integer.parseInt(properties.getProperty("mutation_attempts")); 

        /* Get the Ant settings (used to run test cases) */

		if(!properties.containsKey("ant_base_dir")) throw new Exception("Parameter 'ant_base_dir' not found in properties");
		if(!properties.containsKey("ant_path")) throw new Exception("Parameter 'ant_path' not found in properties");
		if(!properties.containsKey("ant_test_target")) throw new Exception("Parameter 'ant_test_target' not found in properties");

        File antBaseDirectory = new File(properties.getProperty("ant_base_dir"));
        String antPath = properties.getProperty("ant_path");
        String antTestTarget = properties.getProperty("ant_test_target");

        /* Get the location for the log files and class files. */

		if(!properties.containsKey("build_directory")) throw new Exception("Parameter 'build_directory' not found in properties");
		if(!properties.containsKey("class_directory")) throw new Exception("Parameter 'class_directory' not found in properties");

    	File buildDirectory = new File(properties.getProperty("build_directory"));
    	String classDirectory = properties.getProperty("class_directory");

    	/* get the path settings for parsing the AST (and resolving bindings) and compiling. */

		if(!properties.containsKey("classpath")) throw new Exception("Parameter 'classpath' not found in properties");
		if(!properties.containsKey("sourcepath")) throw new Exception("Parameter 'sourcepath' not found in properties");

    	String[] classPath = unpackArray(properties.getProperty("classpath"));
    	String[] sourcePath = unpackArray(properties.getProperty("sourcepath"));

        /* Get the random seed to use. 
         * Different random seeds will cause different mutation operation orders and different statement selections. */

        long randomSeed = Integer.parseInt(properties.getProperty("random_seed"));
        
        /* Set up the three repair components:
         * 	TestExecutor - executes JUnit test cases using an ant task
         * 	JavaJDKCompiler - runs javac to compile the mutated source (doesn't actually use JDT compiler)
         * 	JRSRepair - mutates the source code and oversees the compilation and test execution
         */
		
        testExecutor = new TestExecutor(antBaseDirectory, antPath, antTestTarget);
        compiler = new JavaJDKCompiler(classDirectory, classPath);
        repair = new JRSRepair(sourcePath, classPath, faultyCoverage, seedCoverage, 
                                 mutationCandidates, mutationGenerations, mutationAttempts, randomSeed, 
                                 buildDirectory, compiler, testExecutor);

		return repair;
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
			unpacked = packed.split(";");
		}
		else throw new Exception("Array not enclosed in parenthesis '{ }', cannot unpack.");

		return unpacked;
	}
}