package ca.uwaterloo.ece.qhanam.jrsrepair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.NavigableMap;
import java.util.Random;
import java.util.Stack;
import java.util.TreeMap;
import java.util.Set;

import org.eclipse.jdt.core.dom.*;

/**
 * Stores a list of statements and their weights. Randomly selects statements (with probabilities
 * determined by their weights).
 * 
 * Seed statements and faulty statements inserted here should have already been filtered according
 * to statement coverage. Seed statements should be in the execution path of one or more test cases,
 * while faulty statements should be in the execution path of one or more faulty test cases.
 * 
 * TODO: Add functionality to support filtering seed statements by variable scope.
 * 
 * @author qhanam
 *
 */
public class Statements {
	
	private NavigableMap<Double, SourceStatement> statements;
	private double totalWeight; // We track the total weight so that we can randomly select a statement with weighting.
	private HashMap<String, HashSet<String>> scope;
	private Random random;

	public Statements(HashMap<String, HashSet<String>> scope, long randomSeed) { 
		this.statements = new TreeMap<Double, SourceStatement>();
		this.totalWeight = 0;
		this.scope = scope;
		this.random = new Random(randomSeed);
    }
	
	/**
	 * Add a statement to our statement maps. The statements are inserted according to their weight.
	 * @param s The statement to insert.
	 * @param weight The weight used to calculate the probability of this statement being selected.
	 * @param faulty Indicates the statement should be stored in the faulty statement list as well.
	 */
	public void addStatement(SourceStatement s, double weight){
        this.totalWeight += weight;
        this.statements.put(this.totalWeight, s);
	}

	/**
	 * Randomly selects and returns a faulty statement (to be mutated).
	 * @return The faulty statement to be mutated.
	 */
	public SourceStatement getRandomStatement(){
		SourceStatement statement = null;

		do{
            /* Compute a random spot. */
			double random = this.random.nextDouble() * this.totalWeight;
            
            /* Find the statement at that random spot. */
            statement = this.statements.ceilingEntry(random).getValue();

		} while(statement.inUse);

		/* This statement is now in use. */
		statement.inUse = true;

		return statement;
	}
	
	/**
	 * Randomly selects and returns a faulty statement (to be mutated). Checks scope against
	 * a destination (faulty) statement.
	 * @return The faulty statement to be mutated.
	 */
	public SourceStatement getRandomStatement(SourceStatement destinationScope){
		SourceStatement statement = null;
		boolean inScope = false;

		do{
            /* Compute a random spot. */
			double random = this.random.nextDouble() * this.totalWeight;
            
            /* Find the statement at that random spot. */
            statement = this.statements.ceilingEntry(random).getValue();
            
            /* Check that the statement variables are in scope. */
            inScope = this.inScope(statement, destinationScope);

		} while(statement.inUse || !inScope);

		/* This statement is now in use. */
		statement.inUse = true;

		return statement;
	}
	
	/**
	 * Check that all the variables used in the statement are in the scope.
	 * @param statement
	 * @return
	 */
	private boolean inScope(SourceStatement statement, SourceStatement destinationScope){
        HashSet<String> methodScope = this.scope.get(destinationScope.sourceFile + "." + this.getMethodName(destinationScope.statement));
		VarASTVisitor vav = new VarASTVisitor();
		statement.statement.accept(vav);
		
		/* If there are no variables used, the statement is in scope. */
		if(vav.variableNames.size() == 0) return true;
		
		/* If there is at least one SimpleName matching a variable in scope,
		 * the statement might be in scope so we return true. */
		for(String variableName : vav.variableNames){
			if(!methodScope.contains(variableName)) return true;
		}
		
		/* The statement isn't in scope. */
		return false;
	}

    private String getMethodName(ASTNode node){
        while(!(node instanceof MethodDeclaration)){
        	if(node.equals(node.getRoot())) return "";
            node = node.getParent();
        }
        MethodDeclaration md = (MethodDeclaration) node;
        return md.getName().toString();
    }
	
	/**
	 * Returns a string containing the statements in this set and their weights.
	 */
	@Override
	public String toString(){
		String s = "";
		Set<NavigableMap.Entry<Double, SourceStatement>> entrySet = statements.entrySet();
		for(NavigableMap.Entry<Double, SourceStatement> entry : entrySet){
			s += (Math.round(entry.getKey()*10.0)/10.0) + " : " + entry.getValue() + "\n";
		}
		return s;
	}
	
	/**
	 * Checks if the statement list is empty.
	 * @return True if there are zero elements in the statement map.
	 */
	public boolean isEmpty(){
		if(this.statements.size() == 0) return true;
		return false;
	}	
	
	/**
	 * Collects potential variable usages in a statement.
	 * @author qhanam
	 */
	private class VarASTVisitor extends ASTVisitor{
		public Stack<String> variableNames;
		
		public VarASTVisitor(){
			this.variableNames = new Stack<String>();
		}
		
		/**
		 * Get the names of potential variables used in this statement.
		 */
		public boolean visit(SimpleName var) {
			this.variableNames.add(var.toString());
			return false;
		}
	}
}
