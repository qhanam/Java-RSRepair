package ca.uwaterloo.ece.qhanam.practice.lru;

import ca.uwaterloo.ece.qhanam.practice.lru.Node;

public class LRUList{
	private Node mru;
	private Node lru;
	
	/* Inserts a node into the list as the MRU node. */
	public void insert(Node node){
		if(this.mru == null){
			this.mru = node;
			this.lru = node;
		}
		else{
			node.next = mru;
			mru.previous = node;
			this.mru = node;
		}
	}
	
	/* Removes the LRU node. */
	public Node remove(){
		Node removed = this.lru;

		if(this.mru == this.lru){
			this.mru = null;
			this.lru = null;
		}
		else{
			this.lru.previous.next = null;
			this.lru = this.lru.previous;
		}

		return removed;
	}
	
	/* Make the given node the MRU node. */
	public void access(Node node){
		if(this.mru == node) { } // Do nothing
		else{
			if(node != lru){
                node.previous.next = node.next;
				node.next.previous = node.previous;
			}
			else{
				node.previous.next = null;
				this.lru = node.previous;
			}
			node.next = this.mru;
			node.previous = null;
			this.mru.previous = node;
			this.mru = node;
		}
	}
	
}
