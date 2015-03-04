package ca.uwaterloo.ece.qhanam.jrsrepair;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * TestExecutor specifies the interface to test the program under repair.
 * 
 * This class is implemented as a thread and accepts a timeout value in case
 * the process hangs.
 * 
 * Since different programs have different builds, there are different concrete
 * 	classes (e.g., AntTestExecutor, BashTestExecutor, etc.).
 * 
 * @author qhanam
 */
public abstract class AbstractTestExecutor extends Thread {
	
	private long timeout;
	protected Status status;
	
	public AbstractTestExecutor(long timeout) {
		this.timeout = timeout;
		this.status = Status.ERROR;
	}
	
	/**
	 * Runs the the test cases and returns the status.
	 * @return The process that is running the tests.
	 * @throws Exception
	 */
	protected abstract Process runTests() throws Exception;

	/**
	 * Process the output captured from the process. 
	 * @param output The data written to stdout.
	 * @param errors The data written to stderr.
	 */
	protected abstract void processOutput(String output, String errors);
	
	/**
	 * Runs the the test cases and returns the status.
	 * @return NOT_COMPILED = failed to compile, TESTS_FAILED = failed one or more test cases, TESTS_PASSED = passed all test cases
	 * @throws Exception
	 */
	public Status getStatus(){
		return this.status;
	}
	
	/**
	 * Runs the test cases and stores the status (accessible by getStatus). The
	 * test process is terminated if the timeout is hit. In this case the
	 * status will be Status.EXCEPTION.
	 */
	public void run() {
		long timedOut = System.currentTimeMillis() + this.timeout;
		Process process = null;
		
		try{
            process = runTests();

            /* Set up the output readers. */
            BufferedReader stdInput = new BufferedReader(new 
                       InputStreamReader(process.getInputStream()));

            BufferedReader stdError = new BufferedReader(new 
                InputStreamReader(process.getErrorStream()));

            /* Read the output from the command. */
            String output = "", errors = "";
            String o = null, e = null;
            while ((o = stdInput.readLine()) != null | (e = stdError.readLine()) != null) {
            	if(o != null)	output += o;
            	if(e != null)	errors += e;

                if(System.currentTimeMillis() > timedOut) {
                    process.destroy();
                    return;
                }
            }

            /* Wait until the process has finished or a timout occurs. */
            while(true){
            	Thread.sleep(10);
            	try {
                    process.exitValue();
                    this.processOutput(output, errors);
                    return;
            	} catch (IllegalThreadStateException itse) {
                    if(System.currentTimeMillis() > timedOut) {
                        process.destroy();
                        return;
                    }
            	}
            }
            
		} catch (Exception e) {
			if(process != null) {
				process.destroy();
			}
			return;
		}
	}
	
	/**
	 * The possible results from runTests()
	 */
	public enum Status{
		FAILED, PASSED, ERROR
	}
}
