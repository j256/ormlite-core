package com.j256.ormlite.stmt.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

import com.j256.ormlite.stmt.SelectArg;

public class BetweenTest {

	private final static String COLUMN_NAME = "foo";

	@Test
	public void testAppendOperation() {
		int low = 10;
		int high = 20;
		Between btw = new Between(COLUMN_NAME, true, low, high);
		StringBuilder sb = new StringBuilder();
		btw.appendOperation(sb);
		assertTrue(sb.toString().contains("BETWEEN"));
		sb.setLength(0);
		btw.appendValue(null, sb, new ArrayList<SelectArg>());
		assertEquals(low + " AND " + high + " ", sb.toString());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAppendValueLowNull() {
		new Between(COLUMN_NAME, true, null, 20L).appendValue(null, new StringBuilder(), new ArrayList<SelectArg>());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAppendValueHighNull() {
		new Between(COLUMN_NAME, true, 10L, null).appendValue(null, new StringBuilder(), new ArrayList<SelectArg>());
	}
}
