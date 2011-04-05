package com.j256.ormlite.stmt;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;

import org.junit.Test;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.TableInfo;

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

	@Test
	public void testEscapeMethods() throws Exception {
		UpdateBuilder<Foo, String> stmtb = new UpdateBuilder<Foo, String>(databaseType, baseFooTableInfo);
		String idVal = "blah";
		stmtb.updateColumnValue(Foo.ID_COLUMN_NAME, idVal);
		String expression = "blah + 1";
		stmtb.updateColumnExpression(Foo.VAL_COLUMN_NAME, expression);
		stmtb.where().raw(stmtb.escapeColumnName(Foo.VAL_COLUMN_NAME) + " = " + stmtb.escapeValue(expression));

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
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" = ");
		databaseType.appendEscapedWord(sb, expression);
		sb.append(' ');
		assertEquals(sb.toString(), stmtb.prepareStatementString());

		StringBuilder whereBuilder = new StringBuilder();
		stmtb.escapeColumnName(whereBuilder, Foo.VAL_COLUMN_NAME);
		whereBuilder.append(" = ");
		stmtb.escapeValue(whereBuilder, expression);
		stmtb.where().raw(whereBuilder.toString());
	}

	@Test(expected = SQLException.class)
	public void testUpdateForeignCollection() throws Exception {
		UpdateBuilder<OurForeignCollection, Integer> stmtb =
				new UpdateBuilder<OurForeignCollection, Integer>(
						databaseType,
						new TableInfo<OurForeignCollection, Integer>(connectionSource, null, OurForeignCollection.class));
		stmtb.updateColumnValue(OurForeignCollection.FOOS_FIELD_NAME, null);
	}

	@Test(expected = SQLException.class)
	public void testUpdateForeignCollectionColumnExpression() throws Exception {
		UpdateBuilder<OurForeignCollection, Integer> stmtb =
				new UpdateBuilder<OurForeignCollection, Integer>(
						databaseType,
						new TableInfo<OurForeignCollection, Integer>(connectionSource, null, OurForeignCollection.class));
		stmtb.updateColumnExpression(OurForeignCollection.FOOS_FIELD_NAME, "1");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPrepareStatementUpdateNotSets() throws Exception {
		UpdateBuilder<Foo, String> stmtb = new UpdateBuilder<Foo, String>(databaseType, baseFooTableInfo);
		stmtb.prepare();
	}

	protected static class OurForeignCollection {
		public static final String FOOS_FIELD_NAME = "foos";
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		String stuff;
		@ForeignCollectionField
		ForeignCollection<OurForeign> foos;
		public OurForeignCollection() {
		}
	}

	protected static class OurForeign {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(foreign = true)
		OurForeignCollection foreign;
		public OurForeign() {
		}
	}
}
