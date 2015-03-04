package ca.uwaterloo.ece.qhanam.jrsrepair.context;

import ca.uwaterloo.ece.qhanam.jrsrepair.AbstractTestExecutor;

/**
 * TestContext stores the context for running an AbstractTestExecutor.
 * 
 * @author qhanam
 */
public class TestContext {
	
	private AbstractTestExecutor testExecutor;
	
	public TestContext(AbstractTestExecutor testExecutor){
		this.testExecutor = testExecutor;
	}

	/**
	 * Runs the the test cases and returns the status.
	 * @return NOT_COMPILED = failed to compile, TESTS_FAILED = failed one or more test cases, TESTS_PASSED = passed all test cases
	 * @throws Exception
	 */
	public AbstractTestExecutor.Status runTests() throws Exception {
		return this.testExecutor.runTests();
	}

}
