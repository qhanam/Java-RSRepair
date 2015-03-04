package ca.uwaterloo.ece.qhanam.jrsrepair;

import java.io.File;

/**
 * BashTestExecutor runs the JUnit tests for the program under repair using
 * a bash shell script.
 * 
 * @author qhanam
 */
public class BashTestExecutor extends AbstractTestExecutor {
	
	private File baseDirectory;
	private String scriptPath;

	public BashTestExecutor(long timeout, File baseDirectory, String scriptPath){
		super(timeout);
		this.baseDirectory = baseDirectory;
		this.scriptPath = scriptPath;
	}
	
	/**
	 * Run the script that executes the JUnit test cases.
	 * @return NOT_COMPILED = failed to compile, TESTS_FAILED = failed one or more test cases, TESTS_PASSED = passed all test cases
	 * @throws Exception
	 */
	public Process runTests() throws Exception{
	    
	    /* The program has successfully compiled, so run the JUnit tests. */
        ProcessBuilder builder = new ProcessBuilder(this.scriptPath);
        builder.directory(this.baseDirectory);
        Process process = builder.start();
        return process;
	}

	/**
	 * Process the output captured from the process. 
	 * @param output The data written to stdout.
	 * @param errors The data written to stderr.
	 */
	protected void processOutput(String output, String errors) {
          /* JUnit will output "FAILURES!!!" if one or more tests fail or error out.
           * If we want more details, we can look for this message: "Tests run: 4,  Failures: 1,  Errors: 0" */
          if(output.indexOf("FAILURES!!!") >= 0) this.status = Status.FAILED;
          else this.status = Status.PASSED;
	}
}
