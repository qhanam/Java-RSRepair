package ca.uwaterloo.ece.qhanam.jrsrepair.context;

import java.io.File;

/**
 * Holds the context for the main repair operation.
 * 
 * @author qhanam
 */
public class RepairContext {

	public int candidates; 				// The number of candidates to attempt
	public int generations; 			// The number of generations for each candidate
	public int attempts; 				// The number of compilation attempts before moving on to the next generation or candidate.
	public File buildDirectory;			// The directory for the class files, log files and patches
	public boolean revertFailedCompile;	// Should we undo mutations that don't compile right away?
	public String[] classDirectories;	// OPTIONAL - If there are multiple output directories, specify them here (class files will be copied back).
	public boolean nullMutationOnlly; 	// Only perform the null mutation (does not mutate the program... useful for debugging)
	
	public RepairContext(int candidates, int generations, int attempts, 
						 File buildDirectory, boolean revertFailedCompile, 
						 String[] classDirectories, boolean nullMutationOnly){
		this.candidates = candidates;
		this.generations = generations;
		this.attempts = attempts;
		this.buildDirectory = buildDirectory;
		this.revertFailedCompile = revertFailedCompile;
		this.classDirectories = classDirectories;
		this.nullMutationOnlly = nullMutationOnly;
	}
	
}
