package com.j256.ormlite.stmt.query;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import com.j256.ormlite.stmt.ArgumentHolder;
import com.j256.ormlite.stmt.BaseCoreStmtTest;

public class InTest extends BaseCoreStmtTest {

	@Test(expected = IllegalArgumentException.class)
	public void testAppendValueNull() throws Exception {
		List<Object> objList = new ArrayList<Object>();
		objList.add(null);
		In in = new In("foo", numberFieldType, objList, true);
		in.appendValue(null, new StringBuilder(), null);
	}

	@Test
	public void testAppendValue() throws Exception {
		List<Object> objList = new ArrayList<Object>();
		Random random = new Random();
		int numArgs = 100;
		for (int i = 0; i < numArgs; i++) {
			objList.add((Integer) random.nextInt());
		}
		In in = new In("foo", numberFieldType, objList, true);
		StringBuilder sb = new StringBuilder();
		in.appendValue(null, sb, new ArrayList<ArgumentHolder>());
		String[] args = sb.toString().split(",");
		assertEquals("(" + objList.get(0) + " ", args[0]);
		for (int i = 1; i < numArgs - 1; i++) {
			assertEquals(objList.get(i) + " ", args[i]);
		}
		assertEquals(objList.get(numArgs - 1) + " ) ", args[numArgs - 1]);
	}
}
