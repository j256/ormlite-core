package com.j256.ormlite.stmt;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;

public class QueryBuilderTest extends BaseCoreStmtTest {

	@Test
	public void testSelectAll() throws Exception {
		QueryBuilder<Foo, String> qb = new QueryBuilder<Foo, String>(databaseType, baseFooTableInfo);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(' ');
		assertEquals(sb.toString(), qb.prepareStatementString());
	}

	@Test
	public void testAddColumns() throws Exception {
		QueryBuilder<Foo, String> qb = new QueryBuilder<Foo, String>(databaseType, baseFooTableInfo);
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
	public void testAddBadColumn() throws Exception {
		QueryBuilder<Foo, String> qb = new QueryBuilder<Foo, String>(databaseType, baseFooTableInfo);
		qb.selectColumns("unknown-column");
	}

	@Test
	public void testDontAddIdColumn() throws Exception {
		QueryBuilder<Foo, String> qb = new QueryBuilder<Foo, String>(databaseType, baseFooTableInfo);
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
		QueryBuilder<Foo, String> qb = new QueryBuilder<Foo, String>(databaseType, baseFooTableInfo);
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
		QueryBuilder<Foo, String> qb = new QueryBuilder<Foo, String>(databaseType, baseFooTableInfo);
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
		QueryBuilder<Foo, String> qb = new QueryBuilder<Foo, String>(databaseType, baseFooTableInfo);
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
		QueryBuilder<Foo, String> qb = new QueryBuilder<Foo, String>(databaseType, baseFooTableInfo);
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
		QueryBuilder<Foo, String> qb = new QueryBuilder<Foo, String>(databaseType, baseFooTableInfo);
		qb.distinct();
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT DISTINCT * FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(' ');
		assertEquals(sb.toString(), qb.prepareStatementString());
	}

	@Test
	public void testLimit() throws Exception {
		QueryBuilder<Foo, String> qb = new QueryBuilder<Foo, String>(databaseType, baseFooTableInfo);
		int limit = 103;
		qb.limit(limit);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(" LIMIT ").append(limit).append(' ');
		assertEquals(sb.toString(), qb.prepareStatementString());
	}

	@Test
	public void testOffset() throws Exception {
		QueryBuilder<Foo, String> qb = new QueryBuilder<Foo, String>(databaseType, baseFooTableInfo);
		int offset = 1;
		int limit = 2;
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
		int offset = 1;
		int limit = 2;
		qb.offset(offset);
		qb.limit(limit);
		List<Foo> results = dao.query(qb.prepare());
		
		assertEquals(1, results.size());
	}

	@Test
	public void testLimitAfterSelect() throws Exception {
		QueryBuilder<Foo, String> qb =
				new QueryBuilder<Foo, String>(new LimitAfterSelectDatabaseType(), baseFooTableInfo);
		int limit = 103;
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
		QueryBuilder<Foo, String> qb = new QueryBuilder<Foo, String>(databaseType, baseFooTableInfo);
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
		QueryBuilder<Foo, String> qb = new QueryBuilder<Foo, String>(databaseType, baseFooTableInfo);
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
		QueryBuilder<Foo, String> qb = new QueryBuilder<Foo, String>(databaseType, baseFooTableInfo);
		PreparedQuery<Foo> stmt = qb.prepare();
		stmt.getStatement();
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(' ');
		assertEquals(sb.toString(), qb.prepareStatementString());
	}
}
