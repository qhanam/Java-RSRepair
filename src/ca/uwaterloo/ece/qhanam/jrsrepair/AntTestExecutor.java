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
        builder.redirectErrorStream(true);
        Process process = builder.start();
	    
        /* Handle the output. */
        StreamReaderThread streamReaderThread = new StreamReaderThread(process.getInputStream());
        streamReaderThread.start();

        /* Wait for the process to finish or timeout. */
		ProcessWithTimeout processWithTimeout = new ProcessWithTimeout(process);
		int exitCode = processWithTimeout.waitForProcess(10000);

		/* Handle the result. */
		if (exitCode == Integer.MIN_VALUE)
		{
		    return Status.FAILED;
		}
		else
		{
			streamReaderThread.join(100);
			String output = streamReaderThread.getOutput();
			
            /* If the script output contains "BUILD SUCCESSFUL", then the program has passed all the test cases (if failonerror is on). */
            if(output.indexOf("BUILD SUCCESSFUL") >= 0) return Status.PASSED;
            if(output.indexOf("BUILD FAILED") >= 0) return Status.FAILED;
		}

        /* The program compiled, but failed one or more test cases. */
        return Status.ERROR;
	}
}
