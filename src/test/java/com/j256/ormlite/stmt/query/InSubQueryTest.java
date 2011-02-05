package com.j256.ormlite.stmt.query;

import java.sql.SQLException;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

public class InSubQueryTest extends BaseCoreTest {

	private static final String ID_COLUMN_NAME = "id";
	private static final String FOREIGN_COLUMN_NAME = "foreign";
	private static final String STRING_COLUMN_NAME = "stuff";

	@Test(expected = SQLException.class)
	public void testTwoResultsInSubQuery() throws Exception {
		BaseDaoImpl<ForeignFoo, Integer> foreignDao =
				new BaseDaoImpl<ForeignFoo, Integer>(connectionSource, ForeignFoo.class) {
				};
		QueryBuilder<ForeignFoo, Integer> qbInner = foreignDao.queryBuilder();
		qbInner.selectColumns(ID_COLUMN_NAME);
		QueryBuilder<ForeignFoo, Integer> qbOuter = foreignDao.queryBuilder();
		qbInner.selectColumns(FOREIGN_COLUMN_NAME);

		Where<ForeignFoo, Integer> where = qbOuter.where();
		where.in(ID_COLUMN_NAME, qbInner);
		where.prepare();
	}

	@Test(expected = SQLException.class)
	public void testResultColumnNoMatchWhere() throws Exception {
		BaseDaoImpl<ForeignFoo, Integer> foreignDao =
				new BaseDaoImpl<ForeignFoo, Integer>(connectionSource, ForeignFoo.class) {
				};
		QueryBuilder<ForeignFoo, Integer> qbInner = foreignDao.queryBuilder();
		qbInner.selectColumns(STRING_COLUMN_NAME);
		QueryBuilder<ForeignFoo, Integer> qbOuter = foreignDao.queryBuilder();
		Where<ForeignFoo, Integer> where = qbOuter.where();
		where.in(ID_COLUMN_NAME, qbInner);
		where.prepare();
	}

	protected static class FooId {
		@DatabaseField(id = true, columnName = ID_COLUMN_NAME)
		int id;
		FooId() {
		}
	}

	protected static class ForeignFoo {
		@DatabaseField(id = true, columnName = ID_COLUMN_NAME)
		int id;
		@DatabaseField(columnName = STRING_COLUMN_NAME)
		String stuff;
		@DatabaseField(foreign = true, columnName = FOREIGN_COLUMN_NAME)
		FooId foo;
		ForeignFoo() {
		}
	}
}
