package ca.uwaterloo.ece.qhanam.jrsrepair;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Stack;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import ca.uwaterloo.ece.qhanam.jrsrepair.compiler.JavaJDKCompiler;
import ca.uwaterloo.ece.qhanam.jrsrepair.context.Context;
import ca.uwaterloo.ece.qhanam.jrsrepair.context.MutationContext;
import ca.uwaterloo.ece.qhanam.jrsrepair.mutation.*;

public class JRSRepair {
	
	/* Provides context for all tasks: repair, parsing, mutation, compilation and testing. */
	private Context context;
	
	/* Keep track of the mutation operations for logging. */
	private Stack<String> patches;
	
	/**
	 * Creates a JRSRepair object with the path to the source folder
	 * of the program we are mutating.
	 * @param sourcepaths The path to the source folder of the program we are mutating.
	 */
	public JRSRepair(Context context) throws Exception {

		/* The context for this repair. It's super important that this is set
		 * up properly! */
		this.context = context;

		/* Initialize the patch-building stack. */
		this.patches = new Stack<String>();
	}
	
	/**
	 * Builds ASTs for all the source files. Must be called before repair(). 
	 * 
	 * We could do this in the constructor
	 * of ParserContest, but this operation takes a while, so we leave it here
	 * so it is easier to print progress to the user.
	 */
	public void buildASTs() throws Exception{
		
		this.context.parser.buildASTs();

	}
	
	/**
	 * Attempts to repair the program using the RSRepair method.
	 * @throws Exception
	 */
	public void repair() throws Exception{

		try{
			for(int i = 0; i < this.context.repair.candidates; i++) {
				System.out.println("Running candidate " + (i + 1) + " ...");
                this.mutationIteration(i + 1, 1);
			}
            System.out.println("Finished!");
		}
		catch(Exception e){
			System.out.println("Error: " + e.getMessage());
			throw e;
		}
	}

	/**
	 * The main method for trying a mutation. It performs all the operations needed 
	 * to mutate, compile and test the program. It is recursive and will therefore
	 * attempt multiple mutations at a time before rolling back their changes. 
	 * 
	 * @param candidate The identifier for the current set of mutations.
	 * @param generation The number of mutations that have already been applied for the current candidate.
	 */
	private void mutationIteration(int candidate, int generation) throws Exception{
        /* If we can't find a solution within some number of iterations, abort. */
        int attemptCounter = 0;
        
        /* Let the user know our progress. */
        System.out.println("Running generation " + generation + " ...");
        
        /* Select the mutation type to use for this generation. */
        MutationContext.MutationType mutationType;
        if(this.context.repair.nullMutationOnlly){
        	/* Do not mutate. */
        	mutationType = MutationContext.MutationType.NULL;
        }
        else {
            mutationType = this.context.mutation.getRandomMutationType(); 
        }

        Mutation mutation = null;
        JavaJDKCompiler.Status compileStatus;
        AbstractTestExecutor.Status testStatus;
        
        /* The compiler loop. Attempt to compile until the counter reaches max
         * attempts set by the user. */
        do {
            
        	try {
        		
                /* First we need to get a mutation that will likely compile. To do this
                 * we randomly select a mutation, apply the mutation to the AST to get
                 * a new document, parse the new document into an AST (having the parser
                 * attempt to resolve bindings) and finally check that all variables have
                 * bindings.*/
                int ctr = 0;
                while(true){

                    /* Get a random mutation operation to apply. */
                    mutation = this.context.mutation.getRandomMutation(mutationType);
                    
                    /* Apply the mutation to the AST + Document. */
                    mutation.mutate();
                    
                    /* Check if all the variables are in scope in the new AST. */
                    if(this.context.parser.checkScope(mutation.getRewriter())) break;
                    
                    mutation.undo();
                    
                    /* Just in case... we should make sure we don't have an infinite loop. */
                    ctr++;
                    if(ctr > 1000) throw new Exception("Mutation search timed out after 1000 attempts without a passing scope check.");
                }
                
                this.logMutation(mutation, candidate, generation);
                
                /* Now that we have a mutation that is in-scope, we attempt to compile 
                 * the program. If the program compiles, we run the test cases. If it
                 * doesn't, we roll back the changes and loop to get another mutation. */

                compileStatus = this.context.compiler.compile();
                
                this.logCompileError(candidate, generation, this.context.compiler.dequeueCompileError());
                
                /* Did it compile? If it didn't we might need to undo the mutation before trying again.
                 * Either way, log what happened. */
                if(compileStatus == JavaJDKCompiler.Status.NOT_COMPILED && this.context.repair.revertFailedCompile) {
                    System.out.print(" - Did not compile\n");
                    mutation.undo(); 
                } else if(compileStatus == JavaJDKCompiler.Status.NOT_COMPILED) {
                    this.patches.push("Candidate " + candidate + ", Generation " + generation + "\n" + mutation.toString());
                    System.out.print(" - Did not compile\n");
                } else {
                    this.patches.push("Candidate " + candidate + ", Generation " + generation + "\n" + mutation.toString());
                    System.out.print(" - Compiled!");
                }
        	} 
        	catch (Exception e) {
        		System.out.print(e.getMessage());
        		compileStatus = JavaJDKCompiler.Status.NOT_COMPILED;
        	}

            attemptCounter++;

        } while(compileStatus == JavaJDKCompiler.Status.NOT_COMPILED && attemptCounter < this.context.repair.attempts);

        /* Did the program compile? If it did, run the test cases. */
        if(compileStatus == JavaJDKCompiler.Status.COMPILED){

        	/* We may also need to copy the .class files back to their 
             * class folders (for example, if we have a complex Maven
             * this just makes life easier than re-building ourselves). */
            if(this.context.repair.classDirectories.length > 0){
                for(String directory : this.context.repair.classDirectories){
                    Utilities.copyFiles(new File(this.context.repair.buildDirectory.getPath() + "/classes"), new File(directory));
                }
            }

            /* Run the test cases. */
            testStatus = this.context.test.runTests();

            /* Log what happened. If all tests passed, store the class files. */
            if(testStatus == AbstractTestExecutor.Status.PASSED) {
                this.logSuccesfullPatch(candidate, generation);
                System.out.print(" Passed!\n");
            }
            else if(testStatus == AbstractTestExecutor.Status.FAILED) 
                System.out.print(" Failed.\n");
            else if(testStatus == AbstractTestExecutor.Status.ERROR) 
                System.out.print(" Error - tests may not have run.\n");
        }
        
    
        /* Recurse to the next level of mutations. */
        if(generation < this.context.repair.generations){ 
            this.mutationIteration(candidate, generation + 1);
        }

        /* Since this.patches in a field, we need to unwind the patch stack. */
        if(compileStatus == JavaJDKCompiler.Status.COMPILED || !this.context.repair.revertFailedCompile) {
            this.patches.pop();
            mutation.undo();
        }
	}
	
	/**
	 * Writes the mutation operations to a file. These represent a (successful?) fix.
	 * @throws Exception
	 */
	private void logSuccesfullPatch(int candidate, int generation){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		Date date = new Date();
		File file = new File(this.context.repair.buildDirectory + "/patches", "Candidate" + candidate + "_Generation" + generation + "_" + dateFormat.format(date));
		BufferedWriter out = null;
		
		/* Store the .class files for the program so we can verify. */
		this.context.compiler.storeCompiled(this.context.repair.buildDirectory + "/classes_Candidate" + candidate + "_Generation" + generation + "_" + dateFormat.format(date));
		
		/* Log the mutation events that produced the patch. */
        try{
            file.createNewFile();
            out = new BufferedWriter(new FileWriter(file));
            for(String s : this.patches){
                out.write(s);
            }
        } catch(Exception e) { }

        try{
            if(out != null) out.close();
        } catch(Exception ignore) { System.out.println("Problem closing writer for " + file.getName() + "."); }
	}
	
    /**
     * Temp method for debugging.
	 * @throws Exception
	 */
	public void logMutation(Mutation m, int candidate, int generation) throws Exception{
        Utilities.writeToFileAppend(new File(this.context.repair.buildDirectory + "/mutation-log"), 
                                   ("Candidate " + candidate + ", Generation" + generation 
                                   + "\n" + m.toString()).getBytes());
	}
	
	public void logCompileError(int candidate, int generation, String message) throws Exception {
        Utilities.writeToFileAppend(new File(this.context.repair.buildDirectory + "/compile-log"), 
                                   ("Candidate " + candidate + ", Generation" + generation 
                                   + "\n" + message + "\n********************\n").getBytes());
	}
	
}
