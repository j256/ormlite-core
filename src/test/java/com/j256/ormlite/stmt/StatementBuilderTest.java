package com.j256.ormlite.stmt;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.j256.ormlite.BaseOrmLiteCoreTest;

public class StatementBuilderTest extends BaseOrmLiteCoreTest {

	@Test
	public void testSelectAll() throws Exception {
		QueryBuilder<BaseFoo, String> stmtb = new QueryBuilder<BaseFoo, String>(databaseType, baseFooTableInfo);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(' ');
		assertEquals(sb.toString(), stmtb.prepareStatementString());
	}

	@Test
	public void testAddColumns() throws Exception {
		QueryBuilder<BaseFoo, String> stmtb = new QueryBuilder<BaseFoo, String>(databaseType, baseFooTableInfo);
		String[] columns1 = new String[] { BaseFoo.ID_COLUMN_NAME, BaseFoo.VAL_COLUMN_NAME };
		String column2 = "equal";
		stmtb.selectColumns(columns1);
		stmtb.selectColumns(column2);
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
		assertEquals(sb.toString(), stmtb.prepareStatementString());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddBadColumn() throws Exception {
		QueryBuilder<BaseFoo, String> stmtb = new QueryBuilder<BaseFoo, String>(databaseType, baseFooTableInfo);
		stmtb.selectColumns("unknown-column");
	}

	@Test
	public void testDontAddIdColumn() throws Exception {
		QueryBuilder<BaseFoo, String> stmtb = new QueryBuilder<BaseFoo, String>(databaseType, baseFooTableInfo);
		String column = BaseFoo.VAL_COLUMN_NAME;
		String idColumn = BaseFoo.ID_COLUMN_NAME;
		stmtb.selectColumns(column);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		databaseType.appendEscapedEntityName(sb, column);
		sb.append(',');
		databaseType.appendEscapedEntityName(sb, idColumn);
		sb.append(" FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(' ');
		assertEquals(sb.toString(), stmtb.prepareStatementString());
	}

	@Test
	public void testAddColumnsIterable() throws Exception {
		QueryBuilder<BaseFoo, String> stmtb = new QueryBuilder<BaseFoo, String>(databaseType, baseFooTableInfo);
		List<String> columns1 = new ArrayList<String>();
		columns1.add(BaseFoo.ID_COLUMN_NAME);
		columns1.add(BaseFoo.VAL_COLUMN_NAME);
		String column2 = "equal";
		stmtb.selectColumns(columns1);
		stmtb.selectColumns(column2);
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
		assertEquals(sb.toString(), stmtb.prepareStatementString());
	}

	@Test
	public void testGroupBy() throws Exception {
		QueryBuilder<BaseFoo, String> stmtb = new QueryBuilder<BaseFoo, String>(databaseType, baseFooTableInfo);
		String field1 = BaseFoo.VAL_COLUMN_NAME;
		stmtb.groupBy(field1);
		String field2 = BaseFoo.ID_COLUMN_NAME;
		stmtb.groupBy(field2);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(" GROUP BY ");
		databaseType.appendEscapedEntityName(sb, field1);
		sb.append(',');
		databaseType.appendEscapedEntityName(sb, field2);
		sb.append(' ');
		assertEquals(sb.toString(), stmtb.prepareStatementString());
	}

	@Test
	public void testOrderBy() throws Exception {
		QueryBuilder<BaseFoo, String> stmtb = new QueryBuilder<BaseFoo, String>(databaseType, baseFooTableInfo);
		String field1 = BaseFoo.VAL_COLUMN_NAME;
		stmtb.orderBy(field1, true);
		String field2 = BaseFoo.ID_COLUMN_NAME;
		stmtb.orderBy(field2, true);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(" ORDER BY ");
		databaseType.appendEscapedEntityName(sb, field1);
		sb.append(',');
		databaseType.appendEscapedEntityName(sb, field2);
		sb.append(' ');
		assertEquals(sb.toString(), stmtb.prepareStatementString());
	}

	@Test
	public void testOrderByDesc() throws Exception {
		QueryBuilder<BaseFoo, String> stmtb = new QueryBuilder<BaseFoo, String>(databaseType, baseFooTableInfo);
		String field = BaseFoo.VAL_COLUMN_NAME;
		stmtb.orderBy(field, false);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(" ORDER BY ");
		databaseType.appendEscapedEntityName(sb, field);
		sb.append(" DESC ");
		assertEquals(sb.toString(), stmtb.prepareStatementString());
	}

	@Test
	public void testDistinct() throws Exception {
		QueryBuilder<BaseFoo, String> stmtb = new QueryBuilder<BaseFoo, String>(databaseType, baseFooTableInfo);
		stmtb.distinct();
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT DISTINCT * FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(' ');
		assertEquals(sb.toString(), stmtb.prepareStatementString());
	}

	@Test
	public void testLimit() throws Exception {
		QueryBuilder<BaseFoo, String> stmtb = new QueryBuilder<BaseFoo, String>(databaseType, baseFooTableInfo);
		int limit = 103;
		stmtb.limit(limit);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(" LIMIT ").append(limit).append(' ');
		assertEquals(sb.toString(), stmtb.prepareStatementString());
	}

	@Test
	public void testLimitAfterSelect() throws Exception {
		QueryBuilder<BaseFoo, String> stmtb =
				new QueryBuilder<BaseFoo, String>(new LimitAfterSelectDatabaseType(), baseFooTableInfo);
		int limit = 103;
		stmtb.limit(limit);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT LIMIT ").append(limit);
		sb.append(" * FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(' ');
		assertEquals(sb.toString(), stmtb.prepareStatementString());
	}

	@Test
	public void testWhere() throws Exception {
		QueryBuilder<BaseFoo, String> stmtb = new QueryBuilder<BaseFoo, String>(databaseType, baseFooTableInfo);
		Where where = stmtb.where();
		String val = "1";
		where.eq(BaseFoo.ID_COLUMN_NAME, val);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(" WHERE ");
		databaseType.appendEscapedEntityName(sb, BaseFoo.ID_COLUMN_NAME);
		sb.append(" = ");
		databaseType.appendEscapedWord(sb, val);
		sb.append(' ');
		assertEquals(sb.toString(), stmtb.prepareStatementString());
	}

	@Test
	public void testWhereSelectArg() throws Exception {
		QueryBuilder<BaseFoo, String> stmtb = new QueryBuilder<BaseFoo, String>(databaseType, baseFooTableInfo);
		Where where = stmtb.where();
		SelectArg val = new SelectArg();
		where.eq(BaseFoo.ID_COLUMN_NAME, val);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(" WHERE ");
		databaseType.appendEscapedEntityName(sb, BaseFoo.ID_COLUMN_NAME);
		sb.append(" = ? ");
		assertEquals(sb.toString(), stmtb.prepareStatementString());

		// set the where to the previous where
		stmtb.setWhere(where);
		assertEquals(sb.toString(), stmtb.prepareStatementString());
	}

	@Test
	public void testPrepareStatement() throws Exception {
		QueryBuilder<BaseFoo, String> stmtb = new QueryBuilder<BaseFoo, String>(databaseType, baseFooTableInfo);
		PreparedQuery<BaseFoo> stmt = stmtb.prepare();
		stmt.getStatement();
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(' ');
		assertEquals(sb.toString(), stmtb.prepareStatementString());
	}
}
