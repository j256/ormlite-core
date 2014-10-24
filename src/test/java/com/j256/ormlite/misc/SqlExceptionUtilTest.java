package com.j256.ormlite.misc;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Constructor;
import java.sql.SQLException;

import org.junit.Test;

public class SqlExceptionUtilTest {

	@Test
	public void testException() {
		Throwable cause = new Throwable();
		String msg = "hello";
		SQLException e = SqlExceptionUtil.create(msg, cause);
		assertEquals(msg, e.getMessage());
		assertEquals(cause, e.getCause());
	}

	@Test
	public void testExceptionWithSQLException() {
		String sqlReason = "sql exception message";
		String sqlState = "sql exception state";
		Throwable cause = new SQLException(sqlReason, sqlState);
		String msg = "hello";
		SQLException e = SqlExceptionUtil.create(msg, cause);
		assertEquals(msg, e.getMessage());
		assertEquals(sqlState, e.getSQLState());
		assertEquals(cause, e.getCause());
	}

	@Test
	public void testConstructor() throws Exception {
		@SuppressWarnings({ "rawtypes" })
		Constructor[] constructors = SqlExceptionUtil.class.getDeclaredConstructors();
		assertEquals(1, constructors.length);
		constructors[0].setAccessible(true);
		constructors[0].newInstance();
	}
}
