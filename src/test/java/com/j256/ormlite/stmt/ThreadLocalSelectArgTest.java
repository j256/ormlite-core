package com.j256.ormlite.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.j256.ormlite.field.SqlType;

public class ThreadLocalSelectArgTest {

	@Test
	public void testStuff() {
		ThreadLocalSelectArg arg = new ThreadLocalSelectArg();
		assertNull(arg.getValue());
		assertFalse(arg.isValueSet());
		arg.setValue(null);
		assertNull(arg.getValue());
		assertTrue(arg.isValueSet());
	}

	@Test
	public void testValueConst() {
		int val = 12;
		ThreadLocalSelectArg arg = new ThreadLocalSelectArg(val);
		assertTrue(arg.isValueSet());
		assertEquals(val, arg.getValue());
	}

	@Test
	public void testSqlTypeValueConst() {
		int val = 12;
		SqlType type = SqlType.INTEGER;
		ThreadLocalSelectArg arg = new ThreadLocalSelectArg(type, val);
		assertTrue(arg.isValueSet());
		assertEquals(val, arg.getValue());
		assertEquals(type, arg.getSqlType());
	}

	@Test
	public void testColumnNameTypeValueConst() {
		int val = 12;
		String columnName = "fewopjfewpfjwe";
		ThreadLocalSelectArg arg = new ThreadLocalSelectArg(columnName, val);
		assertTrue(arg.isValueSet());
		assertEquals(val, arg.getValue());
		assertEquals(columnName, arg.getColumnName());
	}
}
