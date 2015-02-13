package ca.uwaterloo.ece.qhanam.jrsrepair;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * TestExecutor specifies the interface to test the program under repair.
 * 
 * Since different programs have different builds, there are different concrete
 * 	classes (e.g., AntTestExecutor, BashTestExecutor, etc.).
 * 
 * @author qhanam
 */
public abstract class AbstractTestExecutor {
	
	/**
	 * Runs the the test cases and returns the status.
	 * @return NOT_COMPILED = failed to compile, TESTS_FAILED = failed one or more test cases, TESTS_PASSED = passed all test cases
	 * @throws Exception
	 */
	public abstract JRSRepair.TestStatus runTests() throws Exception;
}
