package com.hzjbbis.fas.module.gate;

import java.util.Collection;
import java.util.Iterator;
import java.util.TreeMap;

import junit.framework.TestCase;

public class TreeMapTest extends TestCase {
	
	public void testMap(){
		TreeMap map = new TreeMap();
		map.put("key1","1");
		map.put("key2","2");
		map.put("key3","3");
		map.put("key4","4");
		Collection cl = map.values();
		Iterator it = cl.iterator();
		it.next();
		it.next();
		it.remove();
		cl = map.keySet();
		System.out.println(cl);
		System.out.println(map);
	}

}
