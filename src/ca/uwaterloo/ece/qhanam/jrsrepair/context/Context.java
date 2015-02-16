package ca.uwaterloo.ece.qhanam.jrsrepair.context;


public class Context {
	public RepairContext repair;
	public ParserContext parser;
	public MutationContext mutation;
	public CompilerContext compiler;
	public TestContext test;
	
	public Context(RepairContext repair, ParserContext parser, MutationContext mutation, CompilerContext compiler, TestContext test) {
		this.repair = repair;
		this.parser = parser;
		this.mutation = mutation;
		this.compiler = compiler;
		this.test = test;
	}
	
}
