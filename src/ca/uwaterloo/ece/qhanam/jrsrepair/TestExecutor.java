package ca.uwaterloo.ece.qhanam.jrsrepair;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * TestExecutor compiles and runs the JUnit tests for the program under repair.
 * 
 * TODO: Since different programs have different builds, this class should be
 * 		 an interface or abstract (e.g., AntTestExecutor).
 * 
 * @author qhanam
 */
public class TestExecutor {
	
	private File baseDirectory;
	private String antPath;
	private String antCompileTarget;
	private String antTestTarget;

	public TestExecutor(File baseDirectory, String antPath, String antCompileTarget, String antTestTarget){
		this.baseDirectory = baseDirectory;
		this.antPath = antPath;
		this.antCompileTarget = antCompileTarget;
		this.antTestTarget = antTestTarget;
	}
	
	/**
	 * Run the script (e.g., ant) to compile the program and run the JUnit test cases.
	 * @return -1 = failed to compile, 0 = compiled, 1 = passed all test cases
	 * @throws Exception
	 */
	public int runTests() throws Exception{
          /* TODO: Why is this needed to prevent using old source or class files? Find a better solution. We want to ensure any writes
           * had time to get written to the disk. */
          //Thread.sleep(2000);
		
		/* Attempt to compile the program. */
//		try{
//            ProcessBuilder builder = new ProcessBuilder(this.antPath, this.antCompileTarget);
//            builder.directory(this.baseDirectory);
//            Process process = builder.start();
//            
//            BufferedReader stdInput = new BufferedReader(new 
//                       InputStreamReader(process.getInputStream()));
//
//            /* Read the output from the command. */
//            String output = "";
//            String s = null;
//            while ((s = stdInput.readLine()) != null) {
//                output += s;
//            }
//            
//            try{
//              process.waitFor();
//              
//              /* If the script output contains "BUILD SUCCESSFUL", then the program has compiled. */
//              if(output.indexOf("BUILD SUCCESSFUL") < 0) return -1;
//
//            }catch(InterruptedException e){ 
//              System.out.println("Interrupted Exception during cvsCheckout.");
//              throw e;
//            }  	
//		} finally { }
	    
	    /* The program has successfully compiled, so run the JUnit tests. */
		try{
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
              if(output.indexOf("BUILD SUCCESSFUL") >= 0) return 1;

            }catch(InterruptedException e){ 
              System.out.println("Interrupted Exception during JUnit run.");
              throw e;
            }  	
		} finally { }

        return 0;
	}
}
