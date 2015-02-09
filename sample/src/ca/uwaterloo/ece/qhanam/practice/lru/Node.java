package ca.uwaterloo.ece.qhanam.practice.lru;

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
