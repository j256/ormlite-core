package com.j256.ormlite.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.db.BaseDatabaseType;

public class QueryBuilderTest extends BaseCoreStmtTest {

	@Test
	public void testSelectAll() throws Exception {
		QueryBuilder<Foo, String> qb = new QueryBuilder<Foo, String>(databaseType, baseFooTableInfo, null);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(' ');
		assertEquals(sb.toString(), qb.prepareStatementString());
	}

	@Test
	public void testAddColumns() throws Exception {
		QueryBuilder<Foo, String> qb = new QueryBuilder<Foo, String>(databaseType, baseFooTableInfo, null);
		String[] columns1 = new String[] { Foo.ID_COLUMN_NAME, Foo.VAL_COLUMN_NAME };
		String column2 = "equal";
		qb.selectColumns(columns1);
		qb.selectColumns(column2);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		for (String column : columns1) {
			databaseType.appendEscapedEntityName(sb, column);
			sb.append(',');
		}
		databaseType.appendEscapedEntityName(sb, column2);
		sb.append(" FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(' ');
		assertEquals(sb.toString(), qb.prepareStatementString());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddBadColumn() {
		QueryBuilder<Foo, String> qb = new QueryBuilder<Foo, String>(databaseType, baseFooTableInfo, null);
		qb.selectColumns("unknown-column");
	}

	@Test
	public void testDontAddIdColumn() throws Exception {
		QueryBuilder<Foo, String> qb = new QueryBuilder<Foo, String>(databaseType, baseFooTableInfo, null);
		String column = Foo.VAL_COLUMN_NAME;
		String idColumn = Foo.ID_COLUMN_NAME;
		qb.selectColumns(column);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		databaseType.appendEscapedEntityName(sb, column);
		sb.append(',');
		databaseType.appendEscapedEntityName(sb, idColumn);
		sb.append(" FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(' ');
		assertEquals(sb.toString(), qb.prepareStatementString());
	}

	@Test
	public void testAddColumnsIterable() throws Exception {
		QueryBuilder<Foo, String> qb = new QueryBuilder<Foo, String>(databaseType, baseFooTableInfo, null);
		List<String> columns1 = new ArrayList<String>();
		columns1.add(Foo.ID_COLUMN_NAME);
		columns1.add(Foo.VAL_COLUMN_NAME);
		String column2 = "equal";
		qb.selectColumns(columns1);
		qb.selectColumns(column2);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		for (String column : columns1) {
			databaseType.appendEscapedEntityName(sb, column);
			sb.append(',');
		}
		databaseType.appendEscapedEntityName(sb, column2);
		sb.append(" FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(' ');
		assertEquals(sb.toString(), qb.prepareStatementString());
	}

	@Test
	public void testGroupBy() throws Exception {
		QueryBuilder<Foo, String> qb = new QueryBuilder<Foo, String>(databaseType, baseFooTableInfo, null);
		String field1 = Foo.VAL_COLUMN_NAME;
		qb.groupBy(field1);
		String field2 = Foo.ID_COLUMN_NAME;
		qb.groupBy(field2);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(" GROUP BY ");
		databaseType.appendEscapedEntityName(sb, field1);
		sb.append(',');
		databaseType.appendEscapedEntityName(sb, field2);
		sb.append(' ');
		assertEquals(sb.toString(), qb.prepareStatementString());
	}

	@Test
	public void testOrderBy() throws Exception {
		QueryBuilder<Foo, String> qb = new QueryBuilder<Foo, String>(databaseType, baseFooTableInfo, null);
		String field1 = Foo.VAL_COLUMN_NAME;
		qb.orderBy(field1, true);
		String field2 = Foo.ID_COLUMN_NAME;
		qb.orderBy(field2, true);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(" ORDER BY ");
		databaseType.appendEscapedEntityName(sb, field1);
		sb.append(',');
		databaseType.appendEscapedEntityName(sb, field2);
		sb.append(' ');
		assertEquals(sb.toString(), qb.prepareStatementString());
	}

	@Test
	public void testOrderByDesc() throws Exception {
		QueryBuilder<Foo, String> qb = new QueryBuilder<Foo, String>(databaseType, baseFooTableInfo, null);
		String field = Foo.VAL_COLUMN_NAME;
		qb.orderBy(field, false);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(" ORDER BY ");
		databaseType.appendEscapedEntityName(sb, field);
		sb.append(" DESC ");
		assertEquals(sb.toString(), qb.prepareStatementString());
	}

	@Test
	public void testDistinct() throws Exception {
		QueryBuilder<Foo, String> qb = new QueryBuilder<Foo, String>(databaseType, baseFooTableInfo, null);
		qb.distinct();
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT DISTINCT * FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(' ');
		assertEquals(sb.toString(), qb.prepareStatementString());
	}

	@Test
	public void testLimit() throws Exception {
		QueryBuilder<Foo, String> qb = new QueryBuilder<Foo, String>(databaseType, baseFooTableInfo, null);
		long limit = 103;
		qb.limit(limit);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(" LIMIT ").append(limit).append(' ');
		assertEquals(sb.toString(), qb.prepareStatementString());
	}

	@Test
	public void testOffset() throws Exception {
		QueryBuilder<Foo, String> qb = new QueryBuilder<Foo, String>(databaseType, baseFooTableInfo, null);
		long offset = 1;
		long limit = 2;
		qb.offset(offset);
		qb.limit(limit);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(" LIMIT ").append(offset).append(',').append(limit).append(' ');
		assertEquals(sb.toString(), qb.prepareStatementString());
	}

	@Test
	public void testOffsetWorks() throws Exception {
		Dao<Foo, Object> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		foo1.id = "stuff1";
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		foo2.id = "stuff2";
		assertEquals(1, dao.create(foo2));

		assertEquals(2, dao.queryForAll().size());

		QueryBuilder<Foo, Object> qb = dao.queryBuilder();
		long offset = 1;
		long limit = 2;
		qb.offset(offset);
		qb.limit(limit);
		List<Foo> results = dao.query(qb.prepare());

		assertEquals(1, results.size());
	}

	@Test
	public void testLimitAfterSelect() throws Exception {
		QueryBuilder<Foo, String> qb =
				new QueryBuilder<Foo, String>(new LimitAfterSelectDatabaseType(), baseFooTableInfo, null);
		long limit = 103;
		qb.limit(limit);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT LIMIT ").append(limit);
		sb.append(" * FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(' ');
		assertEquals(sb.toString(), qb.prepareStatementString());
	}

	@Test
	public void testWhere() throws Exception {
		QueryBuilder<Foo, String> qb = new QueryBuilder<Foo, String>(databaseType, baseFooTableInfo, null);
		Where<Foo, String> where = qb.where();
		String val = "1";
		where.eq(Foo.ID_COLUMN_NAME, val);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(" WHERE ");
		databaseType.appendEscapedEntityName(sb, Foo.ID_COLUMN_NAME);
		sb.append(" = ");
		databaseType.appendEscapedWord(sb, val);
		sb.append(' ');
		assertEquals(sb.toString(), qb.prepareStatementString());
	}

	@Test
	public void testWhereSelectArg() throws Exception {
		QueryBuilder<Foo, String> qb = new QueryBuilder<Foo, String>(databaseType, baseFooTableInfo, null);
		Where<Foo, String> where = qb.where();
		SelectArg val = new SelectArg();
		where.eq(Foo.ID_COLUMN_NAME, val);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(" WHERE ");
		databaseType.appendEscapedEntityName(sb, Foo.ID_COLUMN_NAME);
		sb.append(" = ? ");
		assertEquals(sb.toString(), qb.prepareStatementString());

		// set the where to the previous where
		qb.setWhere(where);
		assertEquals(sb.toString(), qb.prepareStatementString());
	}

	@Test
	public void testPrepareStatement() throws Exception {
		QueryBuilder<Foo, String> qb = new QueryBuilder<Foo, String>(databaseType, baseFooTableInfo, null);
		PreparedQuery<Foo> stmt = qb.prepare();
		stmt.getStatement();
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(' ');
		assertEquals(sb.toString(), qb.prepareStatementString());
	}

	@Test
	public void testLimitInline() throws Exception {
		QueryBuilder<Foo, String> qb = new QueryBuilder<Foo, String>(new LimitInline(), baseFooTableInfo, null);
		long limit = 213;
		qb.limit(limit);
		PreparedQuery<Foo> stmt = qb.prepare();
		stmt.getStatement();
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(" LIMIT ").append(limit).append(' ');
		assertEquals(sb.toString(), qb.prepareStatementString());
	}

	@Test
	public void testOffsetAndLimit() throws Exception {
		QueryBuilder<Foo, String> qb = new QueryBuilder<Foo, String>(new LimitInline(), baseFooTableInfo, null);
		long offset = 200;
		long limit = 213;
		qb.offset(offset);
		qb.limit(limit);
		PreparedQuery<Foo> stmt = qb.prepare();
		stmt.getStatement();
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(" LIMIT ").append(limit);
		sb.append(" OFFSET ").append(offset).append(' ');
		assertEquals(sb.toString(), qb.prepareStatementString());
	}

	@Test
	public void testShortCuts() throws Exception {
		Dao<Foo, Object> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		foo1.id = "stuff1";
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		foo2.id = "stuff2";
		assertEquals(1, dao.create(foo2));
		List<Foo> results = dao.queryBuilder().where().eq(Foo.ID_COLUMN_NAME, foo2.id).query();
		assertEquals(1, results.size());
		assertEquals(foo2.id, results.get(0).id);
		Iterator<Foo> iterator = dao.queryBuilder().where().eq(Foo.ID_COLUMN_NAME, foo2.id).iterator();
		assertTrue(iterator.hasNext());
		assertEquals(foo2.id, iterator.next().id);
		assertFalse(iterator.hasNext());
	}

	@Test
	public void testOrderByRaw() throws Exception {
		Dao<Foo, Object> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		foo1.id = "stuff1";
		foo1.val = 1;
		foo1.equal = 10;
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		foo2.id = "stuff2";
		foo2.val = 5;
		foo2.equal = 7;
		assertEquals(1, dao.create(foo2));
		List<Foo> results =
				dao.queryBuilder()
						.orderByRaw("(" + Foo.VAL_COLUMN_NAME + "+" + Foo.EQUAL_COLUMN_NAME + ") DESC")
						.query();
		assertEquals(2, results.size());
		assertEquals(foo2.id, results.get(0).id);
		assertEquals(foo1.id, results.get(1).id);
	}

	@Test
	public void testQueryForForeign() throws Exception {
		Dao<Foo, Object> fooDao = createDao(Foo.class, true);
		Dao<Foreign, Object> foreignDao = createDao(Foreign.class, true);

		Foo foo = new Foo();
		foo.id = "id1";
		foo.val = 1231;
		assertEquals(1, fooDao.create(foo));

		Foreign foreign = new Foreign();
		foreign.foo = foo;
		assertEquals(1, foreignDao.create(foreign));

		// use the auto-extract method to extract by id
		List<Foreign> results = foreignDao.queryBuilder().where().eq(Foreign.FOREIGN_COLUMN_NAME, foo).query();
		assertEquals(1, results.size());
		assertEquals(foreign.id, results.get(0).id);

		// query for the id directly
		List<Foreign> results2 = foreignDao.queryBuilder().where().eq(Foreign.FOREIGN_COLUMN_NAME, foo.id).query();
		assertEquals(1, results2.size());
		assertEquals(foreign.id, results2.get(0).id);
	}

	@Test(expected = SQLException.class)
	public void testQueryRawColumnsNotQuery() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		QueryBuilder<Foo, String> qb = dao.queryBuilder();
		qb.selectRaw("COUNT(*)");
		// we can't get Foo objects with the COUNT(*)
		dao.query(qb.prepare());
	}

	@Test
	public void testClear() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, false);
		QueryBuilder<Foo, String> qb = dao.queryBuilder();
		qb.selectColumns(Foo.VAL_COLUMN_NAME);
		qb.groupBy(Foo.VAL_COLUMN_NAME);
		qb.having("COUNT(VAL) > 1");
		qb.where().eq(Foo.ID_COLUMN_NAME, 1);
		qb.clear();
		assertEquals("SELECT * FROM `foo` ", qb.prepareStatementString());
	}

	private static class LimitInline extends BaseDatabaseType {
		public boolean isDatabaseUrlThisType(String url, String dbTypePart) {
			return true;
		}
		@Override
		protected String getDriverClassName() {
			return "foo.bar.baz";
		}
		public String getDatabaseName() {
			return "zipper";
		}
	}
}
