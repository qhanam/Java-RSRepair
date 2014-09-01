package ca.uwaterloo.ece.qhanam.jrsrepair;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class TestExecutor {
	
	private File baseDirectory;
	private String antPath;
	private String antTarget;

	public TestExecutor(File baseDirectory, String antPath, String antTarget){
		this.baseDirectory = baseDirectory;
		this.antPath = antPath;
		this.antTarget = antTarget;
	}
	
	public void runTests() throws Exception{
	    /* Let's make a script file that gets executed and have the revision be a parameter. */
		ProcessBuilder builder = new ProcessBuilder(this.antPath, this.antTarget);
		builder.directory(new File("/Users/qhanam/Documents/workspace_faultlocalization/ca.uwaterloo.ece.qhanam.localization/"));
		Process process = builder.start();
	    
	    BufferedReader stdInput = new BufferedReader(new 
	               InputStreamReader(process.getInputStream()));

        BufferedReader stdError = new BufferedReader(new 
             InputStreamReader(process.getErrorStream()));
	    
	      // read the output from the command
//	        System.out.println("Checkout script output:");
//	        String s = null;
//	        while ((s = stdInput.readLine()) != null) {
//	            System.out.println(s);
//	        }

	        // read any errors from the attempted command
//	        System.out.println("Checkout script error output:");
//	        while ((s = stdError.readLine()) != null) {
//	            System.out.println(s);
//	        }
	    
	    try{
	      process.waitFor();
	    }catch(InterruptedException e){ 
	      System.out.println("Interrupted Exception during cvsCheckout.");
	      throw e;
	    }  	
	}
}
