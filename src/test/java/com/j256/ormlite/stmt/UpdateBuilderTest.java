package com.j256.ormlite.stmt;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UpdateBuilderTest extends BaseCoreStmtTest {

	@Test
	public void testPrepareStatementUpdateValueString() throws Exception {
		UpdateBuilder<Foo, String> stmtb = new UpdateBuilder<Foo, String>(databaseType, baseFooTableInfo);
		String idVal = "blah";
		stmtb.updateColumnValue(Foo.ID_COLUMN_NAME, idVal);
		PreparedUpdate<Foo> stmt = stmtb.prepare();
		stmt.getStatement();
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(" SET ");
		databaseType.appendEscapedEntityName(sb, Foo.ID_COLUMN_NAME);
		sb.append(" = ");
		databaseType.appendEscapedWord(sb, idVal);
		sb.append(' ');
		assertEquals(sb.toString(), stmtb.prepareStatementString());
	}

	@Test
	public void testPrepareStatementUpdateValueNumber() throws Exception {
		UpdateBuilder<Foo, String> stmtb = new UpdateBuilder<Foo, String>(databaseType, baseFooTableInfo);
		int idVal = 13123;
		stmtb.updateColumnValue(Foo.VAL_COLUMN_NAME, idVal);
		PreparedUpdate<Foo> stmt = stmtb.prepare();
		stmt.getStatement();
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(" SET ");
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" = ").append(idVal).append(' ');
		assertEquals(sb.toString(), stmtb.prepareStatementString());
	}

	@Test
	public void testPrepareStatementUpdateValueExpression() throws Exception {
		UpdateBuilder<Foo, String> stmtb = new UpdateBuilder<Foo, String>(databaseType, baseFooTableInfo);
		String idVal = "blah";
		stmtb.updateColumnValue(Foo.ID_COLUMN_NAME, idVal);
		String expression = "blah + 1";
		stmtb.updateColumnExpression(Foo.VAL_COLUMN_NAME, expression);
		stmtb.where().eq(Foo.ID_COLUMN_NAME, idVal);

		PreparedUpdate<Foo> stmt = stmtb.prepare();
		stmt.getStatement();
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(" SET ");
		databaseType.appendEscapedEntityName(sb, Foo.ID_COLUMN_NAME);
		sb.append(" = ");
		databaseType.appendEscapedWord(sb, idVal);
		sb.append(" ,");
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" = ");
		sb.append(expression);
		sb.append(" WHERE ");
		databaseType.appendEscapedEntityName(sb, Foo.ID_COLUMN_NAME);
		sb.append(" = ");
		databaseType.appendEscapedWord(sb, idVal);
		sb.append(' ');
		assertEquals(sb.toString(), stmtb.prepareStatementString());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPrepareStatementUpdateNotSets() throws Exception {
		UpdateBuilder<Foo, String> stmtb = new UpdateBuilder<Foo, String>(databaseType, baseFooTableInfo);
		stmtb.prepare();
	}
}
