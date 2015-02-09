package ca.uwaterloo.ece.qhanam.localization;

import static org.junit.Assert.*;

import org.junit.Test;

import ca.uwaterloo.ece.qhanam.practice.lru.LRUCache;

public class TestLRU {

	@Test
	public void testEmpty() {
		LRUCache cache = new LRUCache(2);
		assertEquals(-1, cache.get(1));
	}
	
	@Test
	public void testSetGet() {
		LRUCache cache = new LRUCache(2);		
		
		cache.set(2, 1);
		cache.set(3, 2);
		
		assertEquals(2, cache.get(3));
		assertEquals(1, cache.get(2));
	}
	
	@Test
	public void testRemove(){
		LRUCache cache = new LRUCache(2);
		
		cache.set(2, 1);
		cache.set(3, 2);
		cache.set(4,3);
	
		assertEquals(-1, cache.get(2));
	}
	
	@Test
	public void testUse(){
		LRUCache cache = new LRUCache(2);
		
		cache.set(1, 1);
		cache.set(2, 2);
		cache.get(1);
		cache.set(3, 3);
		
		assertEquals(1, cache.get(1));
	}
	
	@Test
	public void testOverwrite(){
		LRUCache cache = new LRUCache(2);
		
		cache.set(1, 1);
		cache.set(1, 2);
		
		assertEquals(2, cache.get(1));	
	}
	
	@Test
	public void testOverwriteUse(){
		LRUCache cache = new LRUCache(2);
		
		cache.set(1, 1);
		cache.set(2, 2);
		cache.set(1, 4);
		cache.set(3, 3);
		
		assertEquals(4, cache.get(1));
	}

}
