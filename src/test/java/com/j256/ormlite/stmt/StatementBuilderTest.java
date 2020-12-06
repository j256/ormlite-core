package com.j256.ormlite.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.List;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.StatementBuilder.StatementInfo;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;

public class StatementBuilderTest extends BaseCoreTest {

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

	@Test
	public void testPrepareStatementInfo() throws SQLException {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		QueryBuilder<Foo, Integer> qb = dao.queryBuilder();
		SelectArg arg = new SelectArg();
		qb.where().eq(Foo.ID_COLUMN_NAME, arg);
		StatementInfo info = qb.prepareStatementInfo();
		assertEquals("SELECT * FROM `foo` WHERE `id` = ?", info.getStatement());
		List<ArgumentHolder> argList = info.getArgList();
		assertEquals(1, argList.size());
		assertEquals(arg, argList.get(0));
	}
}
