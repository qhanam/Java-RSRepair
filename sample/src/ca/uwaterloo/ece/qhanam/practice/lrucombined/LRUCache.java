package ca.uwaterloo.ece.qhanam.practice.lrucombined;

import java.util.Hashtable;

public class LRUCache {

    Hashtable<Integer, Node> hash;
    LRUList list;
    int capacity;
    int currentLoad;
    
    public LRUCache(int capacity) {
        hash = new Hashtable<Integer, Node>(capacity);
        list = new LRUList();
        this.capacity = capacity;
        this.currentLoad = 0;
    }
    
    public int get(int key) {
        /* Get the node from the hash table. */
        Node n = hash.get(new Integer(key));
        if(n == null) return -1;
        
        /* Set the node as most recently used. */
        list.access(n);
        
        return n.value;
    }
    
    public void set(int key, int value) {
        Integer k = new Integer(key);
        Node n;
        
        /* Might allready be in the list. */
        n = hash.get(new Integer(key));
        if(n != null){
            n.value = value;

            /* Set the node as most recently used. */
            list.access(n);
        }
        else{
            n = new Node(k, value);
            
            /* We might need to make room. */
            if(currentLoad == capacity + 1/* Bug Here */){
            	Node lru = list.remove();
                hash.remove(lru.key);
            }
            else{
                currentLoad++;
            }
            
            /* Set the node as the most recently used. */
            hash.put(k, n);
            list.insert(n);
        }
    }
    
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
    
    public class Node{
        Integer key;
        int value;
        Node previous;
        Node next;
        
        public Node(Integer key, int value){
            this.key = key;
            this.value = value;
        }
    }
}
