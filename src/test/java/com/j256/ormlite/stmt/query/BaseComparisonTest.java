package com.j256.ormlite.stmt.query;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.junit.Test;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.stmt.SelectArg;

public class BaseComparisonTest {

	private static final String INT_COLUMN_NAME = "column1";
	private static final String STRING_COLUMN_NAME = "column2";

	private BaseComparison cmpInt = new BaseComparison(INT_COLUMN_NAME, true, 10L) {
		@Override
		public StringBuilder appendOperation(StringBuilder sb) {
			sb.append("op");
			return sb;
		}
	};

	private BaseComparison cmpString = new BaseComparison(STRING_COLUMN_NAME, false, 10L) {
		@Override
		public StringBuilder appendOperation(StringBuilder sb) {
			sb.append("op");
			return sb;
		}
	};

	@Test(expected = IllegalArgumentException.class)
	public void testAppendArgOrValueNull() throws Exception {
		cmpInt.appendArgOrValue(null, new StringBuilder(), new ArrayList<SelectArg>(), null);
	}

	@Test
	public void testAppendArgOrValueLong() {
		long value = 23213L;
		StringBuilder sb = new StringBuilder();
		cmpInt.appendArgOrValue(null, sb, new ArrayList<SelectArg>(), value);
		assertEquals(Long.toString(value) + " ", sb.toString());
	}

	@Test
	public void testAppendArgOrValueInteger() {
		int value = 23213;
		StringBuilder sb = new StringBuilder();
		cmpInt.appendArgOrValue(null, sb, new ArrayList<SelectArg>(), value);
		assertEquals(Integer.toString(value) + " ", sb.toString());
	}

	@Test
	public void testAppendArgOrValueShort() {
		short value = 23213;
		StringBuilder sb = new StringBuilder();
		cmpInt.appendArgOrValue(null, sb, new ArrayList<SelectArg>(), value);
		assertEquals(Short.toString(value) + " ", sb.toString());
	}

	@Test
	public void testAppendArgOrValueString() {
		String value = "23wbdqwbdq13";
		StringBuilder sb = new StringBuilder();
		DatabaseType databaseType = createMock(DatabaseType.class);
		databaseType.appendEscapedWord(sb, value);
		replay(databaseType);
		cmpString.appendArgOrValue(databaseType, sb, new ArrayList<SelectArg>(), value);
		verify(databaseType);
	}

	@Test
	public void testAppendArgOrValueSelectArg() {
		SelectArg value = new SelectArg();
		StringBuilder sb = new StringBuilder();
		ArrayList<SelectArg> selectArgList = new ArrayList<SelectArg>();
		try {
			value.getColumnName();
			fail("Should have thrown");
		} catch (IllegalArgumentException e) {
			// expected
		}
		cmpInt.appendArgOrValue(null, sb, selectArgList, value);
		assertEquals(1, selectArgList.size());
		assertEquals(INT_COLUMN_NAME, value.getColumnName());
	}
}
