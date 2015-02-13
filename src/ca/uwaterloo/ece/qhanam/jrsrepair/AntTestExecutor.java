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
	public JRSRepair.TestStatus runTests() throws Exception{
	    
	    /* The program has successfully compiled, so run the JUnit tests. */
        ProcessBuilder builder = new ProcessBuilder(this.antPath, this.antTestTarget);
        builder.directory(this.baseDirectory);
        Process process = builder.start();
        
        BufferedReader stdInput = new BufferedReader(new 
                   InputStreamReader(process.getInputStream()));

        /* Read the output from the command. */
        String output = "";
        String s = null;
        while ((s = stdInput.readLine()) != null) {
            output += s;
        }
        
        try{
          process.waitFor();
          
          /* If the script output contains "BUILD SUCCESSFUL", then the program has passed all the test cases (if failonerror is on). */
          if(output.indexOf("BUILD SUCCESSFUL") >= 0) return JRSRepair.TestStatus.TESTS_PASSED;
          if(output.indexOf("BUILD FAILED") >= 0) return JRSRepair.TestStatus.TESTS_FAILED;

        }catch(InterruptedException e){ 
          System.out.println("Interrupted Exception during JUnit run.");
          throw e;
        }

        /* The program compiled, but failed one or more test cases. */
        return JRSRepair.TestStatus.TEST_ERROR;
	}
}