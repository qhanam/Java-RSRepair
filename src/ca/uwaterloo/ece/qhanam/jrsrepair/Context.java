package ca.uwaterloo.ece.qhanam.jrsrepair;

public class Context {
	private RepairContext repair;
	private MutationContext mutation;
	
	public Context(RepairContext repair, MutationContext mutation) {
		this.repair = repair;
		this.mutation = mutation;
	}
	
	public RepairContext repairContext() {
		return this.repair;
	}
	
	public MutationContext mutationContext() {
		return this.mutation;
	}
}
