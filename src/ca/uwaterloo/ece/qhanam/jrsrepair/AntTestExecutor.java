package ca.uwaterloo.ece.qhanam.jrsrepair;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * AntTestExecutor compiles and runs the JUnit tests for the program under repair using
 * Apache Ant and a build.xml build file.
 * 
 * @author qhanam
 */
public class AntTestExecutor extends AbstractTestExecutor {
	
	private File baseDirectory;
	private String antPath;
	private String antTestTarget;

	public AntTestExecutor(File baseDirectory, String antPath, String antTestTarget){
		this.baseDirectory = baseDirectory;
		this.antPath = antPath;
		this.antTestTarget = antTestTarget;
	}
	
	/**
	 * Run the script (e.g., ant) to run the JUnit test cases.
	 * @return NOT_COMPILED = failed to compile, TESTS_FAILED = failed one or more test cases, TESTS_PASSED = passed all test cases
	 * @throws Exception
	 */
	public Status runTests() throws Exception{
	    
	    /* The program has successfully compiled, so run the JUnit tests. */
        ProcessBuilder builder = new ProcessBuilder(this.antPath, this.antTestTarget);
        builder.directory(this.baseDirectory);
        Process process = builder.start();
        
        BufferedReader stdInput = new BufferedReader(new 
                   InputStreamReader(process.getInputStream()));

        BufferedReader stdError = new BufferedReader(new 
            InputStreamReader(process.getErrorStream()));

        /* Read the output from the command. */
        String output = "", error = "";
        String o = null, e = null;
        while ((o = stdInput.readLine()) != null | (e = stdError.readLine()) != null) {
            if(o != null) output += o;
            if(e != null) error += e;
        }
        
        try{
          process.waitFor();
          
          /* If the script output contains "BUILD SUCCESSFUL", then the program has passed all the test cases (if failonerror is on). */
          if(output.indexOf("BUILD SUCCESSFUL") >= 0) return Status.PASSED;
          if(error.indexOf("BUILD FAILED") >= 0) return Status.FAILED;

        }catch(InterruptedException ie){ 
          System.out.println("Interrupted Exception during JUnit run.");
          throw ie;
        }

        /* The program compiled, but failed one or more test cases. */
        return Status.ERROR;
	}
}
