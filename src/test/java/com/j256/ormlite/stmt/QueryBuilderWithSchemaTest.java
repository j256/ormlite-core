package com.j256.ormlite.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import java.sql.SQLException;

import org.junit.jupiter.api.Test;

import com.j256.ormlite.dao.Dao;

public class QueryBuilderWithSchemaTest extends BaseCoreStmtTest {

	@Test
	public void testSelectAll() throws Exception {
		QueryBuilder<SchemaFoo, Integer> qb =
				new QueryBuilder<SchemaFoo, Integer>(databaseType, baseSchemaFooTableInfo, null);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, baseSchemaFooTableInfo.getSchemaName());
		sb.append('.');
		databaseType.appendEscapedEntityName(sb, baseSchemaFooTableInfo.getTableName());
		assertEquals(sb.toString(), qb.prepareStatementString());
	}

	@Test
	public void testAlias() throws Exception {
		QueryBuilder<SchemaFoo, Integer> qb =
				new QueryBuilder<SchemaFoo, Integer>(databaseType, baseSchemaFooTableInfo, null);
		String alias = "zing";
		qb.setAlias(alias);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, baseSchemaFooTableInfo.getSchemaName());
		sb.append('.');
		databaseType.appendEscapedEntityName(sb, baseSchemaFooTableInfo.getTableName());
		sb.append(" AS ");
		databaseType.appendEscapedEntityName(sb, alias);
		assertEquals(sb.toString(), qb.prepareStatementString());
	}

	@Test
	public void testQueryRawColumnsNotQuery() throws Exception {
		Dao<SchemaFoo, String> dao = createDao(SchemaFoo.class, true);
		QueryBuilder<SchemaFoo, String> qb = dao.queryBuilder();
		qb.selectRaw("COUNT(*)");
		// we can't get SchemaFoo objects with the COUNT(*)
		assertThrowsExactly(SQLException.class, () -> {
			dao.query(qb.prepare());
		});
	}

	@Test
	public void testClear() throws Exception {
		Dao<SchemaFoo, String> dao = createDao(SchemaFoo.class, false);
		QueryBuilder<SchemaFoo, String> qb = dao.queryBuilder();
		qb.selectColumns(SchemaFoo.VAL_COLUMN_NAME);
		qb.groupBy(SchemaFoo.VAL_COLUMN_NAME);
		qb.having("COUNT(VAL) > 1");
		qb.where().eq(SchemaFoo.ID_COLUMN_NAME, 1);
		qb.reset();
		assertEquals("SELECT * FROM `FOO_SCHEMA`.`foo`", qb.prepareStatementString());
	}

}
