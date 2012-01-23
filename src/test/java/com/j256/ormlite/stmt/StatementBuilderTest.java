package com.j256.ormlite.stmt;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.j256.ormlite.stmt.StatementBuilder.StatementType;

public class StatementBuilderTest {

	@Test
	public void testStuff() {
		assertTrue(StatementType.SELECT.isOkForStatementBuilder());
		assertTrue(StatementType.SELECT_LONG.isOkForStatementBuilder());
		assertTrue(StatementType.SELECT_RAW.isOkForStatementBuilder());
		assertTrue(StatementType.UPDATE.isOkForStatementBuilder());
		assertTrue(StatementType.DELETE.isOkForStatementBuilder());
		assertFalse(StatementType.EXECUTE.isOkForStatementBuilder());

		assertTrue(StatementType.SELECT.isOkForQuery());
		assertTrue(StatementType.SELECT_LONG.isOkForQuery());
		assertTrue(StatementType.SELECT_RAW.isOkForQuery());
		assertFalse(StatementType.UPDATE.isOkForQuery());
		assertFalse(StatementType.DELETE.isOkForQuery());
		assertFalse(StatementType.EXECUTE.isOkForQuery());

		assertFalse(StatementType.SELECT.isOkForUpdate());
		assertFalse(StatementType.SELECT_LONG.isOkForUpdate());
		assertFalse(StatementType.SELECT_RAW.isOkForUpdate());
		assertTrue(StatementType.UPDATE.isOkForUpdate());
		assertTrue(StatementType.DELETE.isOkForUpdate());
		assertFalse(StatementType.EXECUTE.isOkForUpdate());

		assertFalse(StatementType.SELECT.isOkForExecute());
		assertFalse(StatementType.SELECT_LONG.isOkForExecute());
		assertFalse(StatementType.SELECT_RAW.isOkForExecute());
		assertFalse(StatementType.UPDATE.isOkForExecute());
		assertFalse(StatementType.DELETE.isOkForExecute());
		assertTrue(StatementType.EXECUTE.isOkForExecute());
	}
}
