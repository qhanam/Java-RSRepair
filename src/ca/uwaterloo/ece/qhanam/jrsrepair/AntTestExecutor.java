package ca.uwaterloo.ece.qhanam.jrsrepair;

import java.io.File;

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

	public AntTestExecutor(long timeout, File baseDirectory, String antPath, String antTestTarget){
		super(timeout);
		this.baseDirectory = baseDirectory;
		this.antPath = antPath;
		this.antTestTarget = antTestTarget;
	}
	
	/**
	 * Run the script (e.g., ant) to run the JUnit test cases.
	 * @return NOT_COMPILED = failed to compile, TESTS_FAILED = failed one or more test cases, TESTS_PASSED = passed all test cases
	 * @throws Exception
	 */
	public Process runTests() throws Exception {
        ProcessBuilder builder = new ProcessBuilder(this.antPath, this.antTestTarget);
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
          /* If the script output contains "BUILD SUCCESSFUL", then the program has passed all the test cases (if failonerror is on). */
          if(output.indexOf("BUILD SUCCESSFUL") >= 0) this.status = Status.PASSED;
          if(errors.indexOf("BUILD FAILED") >= 0) this.status = Status.FAILED;
	}
}
