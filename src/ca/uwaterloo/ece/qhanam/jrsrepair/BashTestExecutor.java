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

	public BashTestExecutor(File baseDirectory, String scriptPath){
		this.baseDirectory = baseDirectory;
		this.scriptPath = scriptPath;
	}
	
	/**
	 * Run the script that executes the JUnit test cases.
	 * @return NOT_COMPILED = failed to compile, TESTS_FAILED = failed one or more test cases, TESTS_PASSED = passed all test cases
	 * @throws Exception
	 */
	public Status runTests() throws Exception{

	    /* The program has successfully compiled, so run the JUnit tests. */
        ProcessBuilder builder = new ProcessBuilder(this.scriptPath);
        builder.directory(this.baseDirectory);
        builder.redirectErrorStream(true);
        Process process = builder.start();

        /* Handle the output. */
        StreamReaderThread streamReaderThread = new StreamReaderThread(process.getInputStream());
        streamReaderThread.start();

        /* Wait for the process to finish or timeout. */
		ProcessWithTimeout processWithTimeout = new ProcessWithTimeout(process);
		int exitCode = processWithTimeout.waitForProcess(30000);

		/* Handle the result. */
		if (exitCode == Integer.MIN_VALUE)
		{
		    return Status.FAILED;
		}
		else
		{
			streamReaderThread.join(100);
			String output = streamReaderThread.getOutput();
			
          /* JUnit will output "FAILURES!!!" if one or more tests fail or error out.
           * If we want more details, we can look for this message: "Tests run: 4,  Failures: 1,  Errors: 0" */
          if(output.indexOf("FAILURES!!!") >= 0) return Status.FAILED;
          else return Status.PASSED;
		}

	}
	
}
