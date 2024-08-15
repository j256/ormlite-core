package com.j256.ormlite.stmt.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.j256.ormlite.stmt.ArgumentHolder;
import com.j256.ormlite.stmt.BaseCoreStmtTest;

public class BetweenTest extends BaseCoreStmtTest {

	private final static String COLUMN_NAME = "foo";

	@Test
	public void testAppendOperation() throws Exception {
		int low = 10;
		int high = 20;
		Between btw = new Between(COLUMN_NAME, numberFieldType, low, high);
		StringBuilder sb = new StringBuilder();
		btw.appendOperation(sb);
		assertTrue(sb.toString().contains("BETWEEN"));
		sb.setLength(0);
		btw.appendValue(null, sb, new ArrayList<ArgumentHolder>());
		assertEquals(low + " AND " + high + " ", sb.toString());
	}

	@Test
	public void testAppendValueLowNull() {
		assertThrowsExactly(IllegalArgumentException.class, () -> {
			new Between(COLUMN_NAME, numberFieldType, null, 20L).appendValue(null, new StringBuilder(),
					new ArrayList<ArgumentHolder>());
		});
	}

	@Test
	public void testAppendValueHighNull() {
		assertThrowsExactly(IllegalArgumentException.class, () -> {
			new Between(COLUMN_NAME, numberFieldType, 10L, null).appendValue(null, new StringBuilder(),
					new ArrayList<ArgumentHolder>());
		});
	}
}
