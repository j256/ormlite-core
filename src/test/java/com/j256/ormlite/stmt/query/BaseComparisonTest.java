package com.j256.ormlite.stmt.query;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.ArrayList;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.stmt.SelectArg;

public class BaseComparisonTest extends BaseCoreTest {

	private static final String INT_COLUMN_NAME = "column1";
	private static final String STRING_COLUMN_NAME = "column2";

	private BaseComparison cmpInt = new BaseComparison(INT_COLUMN_NAME, numberFieldType, 10L) {
		@Override
		public StringBuilder appendOperation(StringBuilder sb) {
			sb.append("op");
			return sb;
		}
	};

	private BaseComparison cmpString = new BaseComparison(STRING_COLUMN_NAME, stringFieldType, 10L) {
		@Override
		public StringBuilder appendOperation(StringBuilder sb) {
			sb.append("op");
			return sb;
		}
	};

	private BaseComparison cmpForeign = new BaseComparison(INT_COLUMN_NAME, foreignFieldType, 10L) {
		@Override
		public StringBuilder appendOperation(StringBuilder sb) {
			sb.append("op");
			return sb;
		}
	};

	@Test(expected = IllegalArgumentException.class)
	public void testAppendArgOrValueNull() throws Exception {
		cmpInt.appendArgOrValue(null, numberFieldType, new StringBuilder(), new ArrayList<SelectArg>(), null);
	}

	@Test
	public void testAppendArgOrValueLong() throws SQLException {
		long value = 23213L;
		StringBuilder sb = new StringBuilder();
		cmpInt.appendArgOrValue(null, numberFieldType, sb, new ArrayList<SelectArg>(), value);
		assertEquals(Long.toString(value) + " ", sb.toString());
	}

	@Test
	public void testAppendArgOrValueInteger() throws SQLException {
		int value = 23213;
		StringBuilder sb = new StringBuilder();
		cmpInt.appendArgOrValue(null, numberFieldType, sb, new ArrayList<SelectArg>(), value);
		assertEquals(Integer.toString(value) + " ", sb.toString());
	}

	@Test
	public void testAppendArgOrValueShort() throws SQLException {
		short value = 23213;
		StringBuilder sb = new StringBuilder();
		cmpInt.appendArgOrValue(null, numberFieldType, sb, new ArrayList<SelectArg>(), value);
		assertEquals(Short.toString(value) + " ", sb.toString());
	}

	@Test
	public void testAppendArgOrValueString() throws SQLException {
		String value = "23wbdqwbdq13";
		StringBuilder sb = new StringBuilder();
		DatabaseType databaseType = createMock(DatabaseType.class);
		databaseType.appendEscapedWord(sb, value);
		replay(databaseType);
		cmpString.appendArgOrValue(databaseType, stringFieldType, sb, new ArrayList<SelectArg>(), value);
		verify(databaseType);
	}

	@Test
	public void testAppendArgOrValueSelectArg() throws SQLException {
		SelectArg value = new SelectArg();
		StringBuilder sb = new StringBuilder();
		ArrayList<SelectArg> selectArgList = new ArrayList<SelectArg>();
		try {
			value.getColumnName();
			fail("Should have thrown");
		} catch (IllegalArgumentException e) {
			// expected
		}
		cmpInt.appendArgOrValue(null, numberFieldType, sb, selectArgList, value);
		assertEquals(1, selectArgList.size());
		assertEquals(INT_COLUMN_NAME, value.getColumnName());
	}

	@Test
	public void testForeignId() throws SQLException {
		StringBuilder sb = new StringBuilder();
		ArrayList<SelectArg> selectArgList = new ArrayList<SelectArg>();
		BaseFoo baseFoo = new BaseFoo();
		String id = "zebra";
		baseFoo.id = id;
		cmpForeign.appendArgOrValue(databaseType, foreignFieldType, sb, selectArgList, baseFoo);
		StringBuilder expectSb = new StringBuilder();
		databaseType.appendEscapedWord(expectSb, id);
		expectSb.append(' ');
		assertEquals(expectSb.toString(), sb.toString());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testForeignIdNull() throws SQLException {
		StringBuilder sb = new StringBuilder();
		ArrayList<SelectArg> selectArgList = new ArrayList<SelectArg>();
		BaseFoo baseFoo = new BaseFoo();
		baseFoo.id = null;
		cmpForeign.appendArgOrValue(databaseType, foreignFieldType, sb, selectArgList, baseFoo);
	}
}
