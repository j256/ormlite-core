package com.j256.ormlite.stmt;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.j256.ormlite.BaseOrmLiteCoreTest;

public class QueryBuilderTest extends BaseOrmLiteCoreTest {

	@SuppressWarnings("deprecation")
	@Test
	public void testSelectAll() throws Exception {
		QueryBuilder<BaseFoo, String> query = new QueryBuilder<BaseFoo, String>(databaseType, baseFooTableInfo);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(' ');
		assertEquals(sb.toString(), query.prepareStatementString());
	}
}
