package ca.uwaterloo.ece.qhanam.practice.lru;

import java.util.Hashtable;
import java.util.LinkedList;

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
        
        /* Might already be in the list. */
        n = hash.get(new Integer(key));
        if(n != null){
            n.value = value;

            /* Set the node as most recently used. */
            list.access(n);
        }
        else{
            n = new Node(k, value);
            
            /* We might need to make room. */
            currentLoad--; /* Bug Here (this statement should be deleted. */
            if(currentLoad == capacity){
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
}
