package com.j256.ormlite.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.TableInfo;

public class SqliteDatabaseTypeTest extends BaseDatabaseTest {

	@Override
	protected void setDatabaseParams() throws SQLException {
		databaseUrl = "jdbc:sqlite:";
		connectionSource = DatabaseTypeUtils.createJdbcConnectionSource(DEFAULT_DATABASE_URL);
	}

	@Override
	protected boolean isDriverClassExpected() {
		return false;
	}

	@Test
	public void testGetDriverClassName() {
		assertEquals("org.sqlite.JDBC", databaseType.getDriverClassName());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGeneratedIdSequenceNotSupported() throws Exception {
		TableInfo<GeneratedIdSequence> tableInfo =
				new TableInfo<GeneratedIdSequence>(databaseType, GeneratedIdSequence.class);
		assertEquals(2, tableInfo.getFieldTypes().length);
		StringBuilder sb = new StringBuilder();
		ArrayList<String> additionalArgs = new ArrayList<String>();
		ArrayList<String> statementsBefore = new ArrayList<String>();
		databaseType.appendColumnArg(sb, tableInfo.getFieldTypes()[0], additionalArgs, statementsBefore, null, null);
	}

	@Test
	public void testGeneratedId() throws Exception {
		TableInfo<GeneratedId> tableInfo = new TableInfo<GeneratedId>(databaseType, GeneratedId.class);
		StringBuilder sb = new StringBuilder();
		List<String> additionalArgs = new ArrayList<String>();
		List<String> statementsBefore = new ArrayList<String>();
		databaseType.appendColumnArg(sb, tableInfo.getFieldTypes()[0], additionalArgs, statementsBefore, null, null);
		assertTrue(sb + "should contain the stuff", sb.toString().contains(" INTEGER PRIMARY KEY AUTOINCREMENT"));
		assertEquals(0, statementsBefore.size());
		assertEquals(0, additionalArgs.size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGeneratedIdLong() throws Exception {
		TableInfo<GeneratedIdLong> tableInfo = new TableInfo<GeneratedIdLong>(databaseType, GeneratedIdLong.class);
		StringBuilder sb = new StringBuilder();
		List<String> additionalArgs = new ArrayList<String>();
		List<String> statementsBefore = new ArrayList<String>();
		databaseType.appendColumnArg(sb, tableInfo.getFieldTypes()[0], additionalArgs, statementsBefore, null, null);
	}

	@Test
	public void testUsernamePassword() throws Exception {
		closeConnection();
		databaseType = new DerbyEmbeddedDatabaseType();
	}

	@Override
	@Test
	public void testFieldWidthSupport() throws Exception {
		assertFalse(databaseType.isVarcharFieldWidthSupported());
	}

	@Test
	public void testCreateTableReturnsZero() throws Exception {
		assertFalse(databaseType.isCreateTableReturnsZero());
	}

	@Test
	public void testSerialField() throws Exception {
		TableInfo<SerialField> tableInfo = new TableInfo<SerialField>(databaseType, SerialField.class);
		StringBuilder sb = new StringBuilder();
		List<String> additionalArgs = new ArrayList<String>();
		List<String> statementsBefore = new ArrayList<String>();
		databaseType.appendColumnArg(sb, tableInfo.getFieldTypes()[0], additionalArgs, statementsBefore, null, null);
		assertTrue(sb.toString().contains("VARBINARY"));
	}

	protected static class GeneratedIdLong {
		@DatabaseField(generatedId = true)
		public long id;
		@DatabaseField
		String other;
		public GeneratedIdLong() {
		}
	}

	protected static class SerialField {
		@DatabaseField
		SerializedThing other;
		public SerialField() {
		}
	}

	protected static class SerializedThing implements Serializable {
		private static final long serialVersionUID = -7989929665216767119L;
		@DatabaseField
		String other;
		public SerializedThing() {
		}
	}
}
