package com.j256.ormlite.stmt;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;

public class UpdateBuilderTest extends BaseCoreTest {

	@Test
	public void testPrepareStatementUpdateValueString() throws Exception {
		UpdateBuilder<BaseFoo, String> stmtb = new UpdateBuilder<BaseFoo, String>(databaseType, baseFooTableInfo);
		String idVal = "blah";
		stmtb.updateColumnValue(BaseFoo.ID_COLUMN_NAME, idVal);
		PreparedUpdate<BaseFoo> stmt = stmtb.prepare();
		stmt.getStatement();
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(" SET ");
		databaseType.appendEscapedEntityName(sb, BaseFoo.ID_COLUMN_NAME);
		sb.append(" = ");
		databaseType.appendEscapedWord(sb, idVal);
		sb.append(' ');
		assertEquals(sb.toString(), stmtb.prepareStatementString());
	}

	@Test
	public void testPrepareStatementUpdateValueNumber() throws Exception {
		UpdateBuilder<BaseFoo, String> stmtb = new UpdateBuilder<BaseFoo, String>(databaseType, baseFooTableInfo);
		int idVal = 13123;
		stmtb.updateColumnValue(BaseFoo.VAL_COLUMN_NAME, idVal);
		PreparedUpdate<BaseFoo> stmt = stmtb.prepare();
		stmt.getStatement();
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(" SET ");
		databaseType.appendEscapedEntityName(sb, BaseFoo.VAL_COLUMN_NAME);
		sb.append(" = ").append(idVal).append(' ');
		assertEquals(sb.toString(), stmtb.prepareStatementString());
	}

	@Test
	public void testPrepareStatementUpdateValueExpression() throws Exception {
		UpdateBuilder<BaseFoo, String> stmtb = new UpdateBuilder<BaseFoo, String>(databaseType, baseFooTableInfo);
		String idVal = "blah";
		stmtb.updateColumnValue(BaseFoo.ID_COLUMN_NAME, idVal);
		String expression = "blah + 1";
		stmtb.updateColumnExpression(BaseFoo.VAL_COLUMN_NAME, expression);
		stmtb.where().eq(BaseFoo.ID_COLUMN_NAME, idVal);

		PreparedUpdate<BaseFoo> stmt = stmtb.prepare();
		stmt.getStatement();
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(" SET ");
		databaseType.appendEscapedEntityName(sb, BaseFoo.ID_COLUMN_NAME);
		sb.append(" = ");
		databaseType.appendEscapedWord(sb, idVal);
		sb.append(" ,");
		databaseType.appendEscapedEntityName(sb, BaseFoo.VAL_COLUMN_NAME);
		sb.append(" = ");
		sb.append(expression);
		sb.append(" WHERE ");
		databaseType.appendEscapedEntityName(sb, BaseFoo.ID_COLUMN_NAME);
		sb.append(" = ");
		databaseType.appendEscapedWord(sb, idVal);
		sb.append(' ');
		assertEquals(sb.toString(), stmtb.prepareStatementString());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPrepareStatementUpdateNotSets() throws Exception {
		UpdateBuilder<BaseFoo, String> stmtb = new UpdateBuilder<BaseFoo, String>(databaseType, baseFooTableInfo);
		stmtb.prepare();
	}
}
