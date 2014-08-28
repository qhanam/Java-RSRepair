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
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof LCNode)) return false;
		LCNode node = (LCNode) o;
		String a = this.pathName + "." + this.className + "." + this.lineNumber;
		String b = node.pathName + "." + node.className + "." + node.lineNumber;
		return a.equals(b);
	}
	
	@Override
	public int hashCode(){
		String a = this.pathName + "." + this.className + "." + this.lineNumber;
		return a.hashCode();
	}
	
	@Override
	public String toString(){
		return this.pathName + "." + this.className + "." + this.lineNumber;
	}
}