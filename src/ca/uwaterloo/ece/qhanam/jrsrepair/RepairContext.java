package ca.uwaterloo.ece.qhanam.jrsrepair;

/**
 * Holds the context for the main repair operation.
 * 
 * @author qhanam
 */
public class RepairContext {

	private int numCandidates; 		// The number of candidates to attempt
	private int numGenerations; 	// The number of generations for each candidate
	private int numAttempts; 		// The number of compilation attempts before moving on to the next generation or candidate.
	
	public RepairContext(int candidates, int generations, int attempts){
		this.numCandidates = candidates;
		this.numGenerations = generations;
		this.numAttempts = attempts;
	}
	
	public int candidates() { return this.numCandidates; }
	public int generations() { return this.numGenerations; }
	public int attempts() { return this.numAttempts; }
	
}
