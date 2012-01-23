package com.j256.ormlite.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.sql.SQLException;
import java.util.Date;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.TableInfo;

public class UpdateBuilderTest extends BaseCoreStmtTest {

	@Test
	public void testPrepareStatementUpdateValueString() throws Exception {
		UpdateBuilder<Foo, Integer> stmtb = new UpdateBuilder<Foo, Integer>(databaseType, baseFooTableInfo, null);
		int idVal = 1312;
		stmtb.updateColumnValue(Foo.ID_COLUMN_NAME, idVal);
		PreparedUpdate<Foo> stmt = stmtb.prepare();
		stmt.getStatement();
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(" SET ");
		databaseType.appendEscapedEntityName(sb, Foo.ID_COLUMN_NAME);
		sb.append(" = ").append(idVal).append(' ');
		assertEquals(sb.toString(), stmtb.prepareStatementString());
	}

	@Test
	public void testPrepareStatementUpdateValueNumber() throws Exception {
		UpdateBuilder<Foo, Integer> stmtb = new UpdateBuilder<Foo, Integer>(databaseType, baseFooTableInfo, null);
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
		UpdateBuilder<Foo, Integer> stmtb = new UpdateBuilder<Foo, Integer>(databaseType, baseFooTableInfo, null);
		int idVal = 78654;
		stmtb.updateColumnValue(Foo.ID_COLUMN_NAME, idVal);
		String expression = "blah + 1";
		stmtb.updateColumnExpression(Foo.VAL_COLUMN_NAME, expression);
		stmtb.where().eq(Foo.ID_COLUMN_NAME, idVal);

		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(" SET ");
		databaseType.appendEscapedEntityName(sb, Foo.ID_COLUMN_NAME);
		sb.append(" = ").append(idVal).append(" ,");
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" = ");
		sb.append(expression);
		sb.append(" WHERE ");
		databaseType.appendEscapedEntityName(sb, Foo.ID_COLUMN_NAME);
		sb.append(" = ").append(idVal).append(' ');
		assertEquals(sb.toString(), stmtb.prepareStatementString());
	}

	@Test
	public void testEscapeMethods() throws Exception {
		UpdateBuilder<Foo, Integer> stmtb = new UpdateBuilder<Foo, Integer>(databaseType, baseFooTableInfo, null);
		int idVal = 32524;
		stmtb.updateColumnValue(Foo.ID_COLUMN_NAME, idVal);
		String expression = "blah + 1";
		stmtb.updateColumnExpression(Foo.VAL_COLUMN_NAME, expression);
		stmtb.where().raw(stmtb.escapeColumnName(Foo.VAL_COLUMN_NAME) + " = " + stmtb.escapeValue(expression));

		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(" SET ");
		databaseType.appendEscapedEntityName(sb, Foo.ID_COLUMN_NAME);
		sb.append(" = ").append(idVal).append(" ,");
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
						new TableInfo<OurForeignCollection, Integer>(connectionSource, null, OurForeignCollection.class),
						null);
		stmtb.updateColumnValue(OurForeignCollection.FOOS_FIELD_NAME, null);
	}

	@Test(expected = SQLException.class)
	public void testUpdateForeignCollectionColumnExpression() throws Exception {
		UpdateBuilder<OurForeignCollection, Integer> stmtb =
				new UpdateBuilder<OurForeignCollection, Integer>(
						databaseType,
						new TableInfo<OurForeignCollection, Integer>(connectionSource, null, OurForeignCollection.class),
						null);
		stmtb.updateColumnExpression(OurForeignCollection.FOOS_FIELD_NAME, "1");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPrepareStatementUpdateNotSets() throws Exception {
		UpdateBuilder<Foo, Integer> stmtb = new UpdateBuilder<Foo, Integer>(databaseType, baseFooTableInfo, null);
		stmtb.prepare();
	}

	@Test
	public void testUpdateDate() throws Exception {
		Dao<UpdateDate, Integer> dao = createDao(UpdateDate.class, true);
		UpdateDate updateDate = new UpdateDate();
		updateDate.date = new Date();
		assertEquals(1, dao.create(updateDate));
		TableInfo<UpdateDate, Integer> tableInfo =
				new TableInfo<UpdateDate, Integer>(connectionSource, null, UpdateDate.class);
		UpdateBuilder<UpdateDate, Integer> stmtb = new UpdateBuilder<UpdateDate, Integer>(databaseType, tableInfo, dao);
		Date newDate = new Date(System.currentTimeMillis() + 10);
		stmtb.updateColumnValue(UpdateDate.DATE_FIELD, newDate);
		// this used to cause a NPE because of a missing args
		assertEquals(1, dao.update(stmtb.prepare()));
		// make sure the update worked
		UpdateDate updateDate2 = dao.queryForId(updateDate.id);
		assertNotNull(updateDate2);
		assertEquals(newDate, updateDate2.date);
	}

	@Test
	public void testUpdateBuildUpdateMathod() throws Exception {
		Dao<UpdateDate, Integer> dao = createDao(UpdateDate.class, true);
		UpdateDate updateDate = new UpdateDate();
		updateDate.date = new Date();
		assertEquals(1, dao.create(updateDate));
		Date newDate = new Date(System.currentTimeMillis() + 10);

		UpdateBuilder<UpdateDate, Integer> ub = dao.updateBuilder();
		ub.updateColumnValue(UpdateDate.DATE_FIELD, newDate);
		// this used to cause a NPE because of a missing args
		assertEquals(1, ub.update());
		// make sure the update worked
		UpdateDate result = dao.queryForId(updateDate.id);
		assertNotNull(result);
		assertEquals(newDate, result.date);
	}

	@Test
	public void testUpdateNull() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		String nullField = "not really that null";
		foo.stringField = nullField;
		assertEquals(dao.create(foo), 1);

		Foo result = dao.queryForId(foo.id);
		assertNotNull(result);
		assertEquals(nullField, result.stringField);

		// try setting to null
		UpdateBuilder<Foo, Integer> ub = dao.updateBuilder();
		SelectArg arg = new SelectArg();
		arg.setValue(null);
		ub.updateColumnValue(Foo.STRING_COLUMN_NAME, arg);
		assertEquals(1, dao.update(ub.prepare()));

		result = dao.queryForId(foo.id);
		assertNotNull(result);
		assertNull(result.stringField);

		// now back to value
		ub = dao.updateBuilder();
		ub.updateColumnValue(Foo.STRING_COLUMN_NAME, nullField);
		assertEquals(1, dao.update(ub.prepare()));

		result = dao.queryForId(foo.id);
		assertNotNull(result);
		assertEquals(nullField, result.stringField);

		// now back to null
		ub = dao.updateBuilder();
		ub.updateColumnExpression(Foo.STRING_COLUMN_NAME, "null");
		assertEquals(1, dao.update(ub.prepare()));

		result = dao.queryForId(foo.id);
		assertNotNull(result);
		assertNull(result.stringField);

		// now back to value
		ub = dao.updateBuilder();
		ub.updateColumnValue(Foo.STRING_COLUMN_NAME, nullField);
		assertEquals(1, dao.update(ub.prepare()));

		result = dao.queryForId(foo.id);
		assertNotNull(result);
		assertEquals(nullField, result.stringField);

		// now back to null
		ub = dao.updateBuilder();
		ub.updateColumnValue(Foo.STRING_COLUMN_NAME, null);
		assertEquals(1, dao.update(ub.prepare()));

		result = dao.queryForId(foo.id);
		assertNotNull(result);
		assertNull(result.stringField);
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

	protected static class UpdateDate {
		public static final String DATE_FIELD = "date";
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(columnName = DATE_FIELD)
		Date date;
		public UpdateDate() {
		}
	}
}
