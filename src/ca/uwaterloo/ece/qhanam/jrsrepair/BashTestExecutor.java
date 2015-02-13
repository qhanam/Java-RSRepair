package ca.uwaterloo.ece.qhanam.jrsrepair;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * BashTestExecutor runs the JUnit tests for the program under repair using
 * a bash shell script.
 * 
 * @author qhanam
 */
public class BashTestExecutor extends AbstractTestExecutor {
	
	private File baseDirectory;
	private String scriptPath;

	public BashTestExecutor(File baseDirectory, String scriptPath){
		this.baseDirectory = baseDirectory;
		this.scriptPath = scriptPath;
	}
	
	/**
	 * Run the script that executes the JUnit test cases.
	 * @return NOT_COMPILED = failed to compile, TESTS_FAILED = failed one or more test cases, TESTS_PASSED = passed all test cases
	 * @throws Exception
	 */
	public JRSRepair.TestStatus runTests() throws Exception{
	    
	    /* The program has successfully compiled, so run the JUnit tests. */
        ProcessBuilder builder = new ProcessBuilder(this.scriptPath);
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
          
          /* JUnit will output "FAILURES!!!" if one or more tests fail or error out.
           * If we want more details, we can look for this message: "Tests run: 4,  Failures: 1,  Errors: 0" */
          if(output.indexOf("FAILURES!!!") >= 0) return JRSRepair.TestStatus.TESTS_FAILED;
          else return JRSRepair.TestStatus.TESTS_PASSED;

        }catch(InterruptedException e){ 
          System.out.println("Interrupted Exception during JUnit run.");
          throw e;
        }

	}
}
