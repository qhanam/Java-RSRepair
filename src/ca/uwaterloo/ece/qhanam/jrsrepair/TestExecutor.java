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
	private String antTarget;

	public TestExecutor(File baseDirectory, String antPath, String antTarget){
		this.baseDirectory = baseDirectory;
		this.antPath = antPath;
		this.antTarget = antTarget;
	}
	
	/**
	 * Run the script (e.g., ant) to compile the program and run the JUnit test cases.
	 * @return true if the program under repair compiled, false otherwise.
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	public boolean runTests() throws Exception{
		ProcessBuilder builder = new ProcessBuilder(this.antPath, this.antTarget);
		builder.directory(this.baseDirectory);
		Process process = builder.start();
	    
	    BufferedReader stdInput = new BufferedReader(new 
	               InputStreamReader(process.getInputStream()));

        BufferedReader stdError = new BufferedReader(new 
             InputStreamReader(process.getErrorStream()));
	    
	    /* Read the output from the command. */
        String output = "";
        String s = null;
        while ((s = stdInput.readLine()) != null) {
        	output += s;
        }
        
	        // read any errors from the attempted command
//	        System.out.println("Checkout script error output:");
//	        while ((s = stdError.readLine()) != null) {
//	            System.out.println(s);
//	        }
	    
	    try{
	      process.waitFor();
	      
	      /* If the script output contains "BUILD SUCCESSFUL", then the program has compiled. */
	      if(output.indexOf("BUILD SUCCESSFUL") < 0) return false;
	      return true;

	    }catch(InterruptedException e){ 
	      System.out.println("Interrupted Exception during cvsCheckout.");
	      throw e;
	    }  	
	}
}
