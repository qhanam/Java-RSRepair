package ca.uwaterloo.ece.qhanam.jrsrepair;

class LCNode implements Comparable<LCNode>{
	public String pathName;
	public String className;
	public int lineNumber;
	
	/**
	 * Constructor
	 * @param pathName
	 * @param className
	 * @param lineNumber
	 */
	public LCNode(String pathName, String className, int lineNumber){
		this.pathName = pathName;
		this.className = className;
		this.lineNumber = lineNumber;
	}

	@Override
	public int compareTo(LCNode node) {
		String a = this.pathName + "." + this.className + "." + this.lineNumber;
		String b = node.pathName + "." + node.className + "." + node.lineNumber;
		return a.compareTo(b);
	}
}