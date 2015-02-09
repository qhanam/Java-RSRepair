package ca.uwaterloo.ece.qhanam.practice.lru;

import java.util.ArrayList;

public class LRU {

	/**
	 * Run LRU cache.
	 */
	public static void main(String[] args) {
		String s = "";
		
		LRUCache cache = new LRUCache(2);
		cache.set(2, 1);
		cache.set(3, 2);
		s += cache.get(3);
		s += cache.get(2);
		cache.set(4,3);
		s += cache.get(2);
		s += cache.get(3);
		s += cache.get(4);
		
		System.out.println(s);
	}

}
