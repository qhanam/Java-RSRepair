package ca.uwaterloo.ece.qhanam.jrsrepair.test;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

import ca.uwaterloo.ece.qhanam.jrsrepair.*;
import ca.uwaterloo.ece.qhanam.jrsrepair.compiler.JavaJDKCompiler;
import ca.uwaterloo.ece.qhanam.jrsrepair.context.Context;
import ca.uwaterloo.ece.qhanam.jrsrepair.context.ContextFactory;

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
		
        AbstractTestExecutor testExecutor;
        JavaJDKCompiler compiler;
		JRSRepair repair;

		/* Get the coverage files from fault localization. */
		
		if(!properties.containsKey("faulty_coverage")) throw new Exception("Parameter 'faulty_coverage' not found in properties");
		if(!properties.containsKey("seed_coverage")) throw new Exception("Parameter 'seed_coverage' not found in properties");

        File faultyCoverage = new File(properties.getProperty("faulty_coverage"));
        File seedCoverage = new File(properties.getProperty("faulty_coverage"));
        
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

    	/* classdirectories is optional (for multiple output directories) */
    	String[] classDirectories;
    	if(properties.containsKey("class_destination_directories")) classDirectories = unpackArray(properties.getProperty("class_destination_directories"));
    	else classDirectories = new String[] {};
    	
    	/* revertFailedCompile is optional (defaults to false). If true, if a compile fails, the last mutation is
    	 * undone before moving on to the next candidate. GenProg and JRSRepair's functionality would have this
    	 * setting false because they build patches BEFORE they execute them. */
    	
    	boolean revertFailedCompile = false;
    	if(properties.containsKey("revert_failed_compile")) revertFailedCompile = Boolean.parseBoolean(properties.getProperty("revert_failed_compile"));

        /* Get the random seed to use. 
         * Different random seeds will cause different mutation operation orders and different statement selections. */

        long randomSeed = Integer.parseInt(properties.getProperty("random_seed"));
        
        /* Set up the three repair components:
         * 	TestExecutor - executes JUnit test cases using an ant task
         * 	JavaJDKCompiler - runs javac to compile the mutated source (doesn't actually use JDT compiler)
         * 	JRSRepair - mutates the source code and oversees the compilation and test execution
         */
        
        Context context = ContextFactory.buildContext(properties);
		
        testExecutor = SampleUse.buildTestExecutor(properties);
        compiler = new JavaJDKCompiler(classDirectory, classPath);
        repair = new JRSRepair(sourcePath, classPath, faultyCoverage, seedCoverage, 
                                 randomSeed, 
                                 buildDirectory, compiler, testExecutor, classDirectories, revertFailedCompile, context);

		return repair;
	}
	
	/**
	 * A factory method that builds the appropriate TestExecutor from the details
	 * provided by the properties file. 
	 * @param properties The properties file provided by the user.
	 * @return The appropriate concrete instance of AbstractTestExecutor.
	 * @throws Exception Throws an exception if a parameter in the properties file
	 * 					 is missing.
	 */
	private static AbstractTestExecutor buildTestExecutor(Properties properties) throws Exception{
		
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
		
		return testExecutor;
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
	
	private enum TestScript { ANT, BASH }
}