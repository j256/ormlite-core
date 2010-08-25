package com.j256.ormlite.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.j256.ormlite.BaseOrmLiteTest;
import com.j256.ormlite.TestUtils;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.stmt.StatementBuilder;
import com.j256.ormlite.table.TableInfo;

/**
 * Base test for other database tests which perform specific functionality tests on all databases.
 */
public abstract class BaseDatabaseTest extends BaseOrmLiteTest {

	private final static String DATABASE_NAME = "ormlite";
	private final String DB_DIRECTORY = "target/" + getClass().getSimpleName();

	protected final static String GENERATED_ID_SEQ = "genId_seq";

	@Test
	public void testCommentLinePrefix() throws Exception {
		assertEquals("-- ", databaseType.getCommentLinePrefix());
	}

	@Test
	public void testEscapedEntityName() throws Exception {
		String word = "word";
		assertEquals("`" + word + "`", TestUtils.appendEscapedEntityName(databaseType, word));
	}

	@Test
	public void testEscapedWord() throws Exception {
		String word = "word";
		assertEquals("'" + word + "'", TestUtils.appendEscapedWord(databaseType, word));
	}

	@Test
	public void testCreateColumnArg() throws Exception {
		List<String> additionalArgs = new ArrayList<String>();
		List<String> moreStmts = new ArrayList<String>();
		List<String> queriesAfter = new ArrayList<String>();
		TableInfo<Foo> tableInfo = new TableInfo<Foo>(databaseType, Foo.class);
		FieldType fieldType = tableInfo.getIdField();
		StringBuilder sb = new StringBuilder();
		databaseType.appendColumnArg(sb, fieldType, additionalArgs, null, moreStmts, queriesAfter);
		assertTrue(sb.toString().contains(fieldType.getDbColumnName()));
		if (!sb.toString().contains("PRIMARY KEY")) {
			assertEquals(1, additionalArgs.size());
			assertTrue(additionalArgs.get(0).contains("PRIMARY KEY"));
		}
	}

	@Test
	public void testFileSystem() throws Exception {
		File dbDir = new File(DB_DIRECTORY);
		TestUtils.deleteDirectory(dbDir);
		dbDir.mkdirs();
		assertEquals(0, dbDir.list().length);
		closeConnection();
		String dbUrl = "jdbc:h2:" + dbDir.getPath() + "/" + DATABASE_NAME;
		connectionSource = DatabaseTypeUtils.createJdbcConnectionSource(dbUrl);
		connectionSource.getReadWriteConnection();
		databaseType = DatabaseTypeUtils.createDatabaseType(dbUrl);
		assertTrue(dbDir.list().length != 0);
	}

	@Test
	public void testFieldWidthSupport() throws Exception {
		assertTrue(databaseType.isVarcharFieldWidthSupported());
	}

	@Test
	public void testLimitSupport() throws Exception {
		assertTrue(databaseType.isLimitSqlSupported());
	}

	@Test
	public void testLimitAfterSelect() throws Exception {
		assertFalse(databaseType.isLimitAfterSelect());
	}

	@Test
	public void testLimitFormat() throws Exception {
		if (!databaseType.isLimitSqlSupported()) {
			return;
		}
		TableInfo<Foo> tableInfo = new TableInfo<Foo>(databaseType, Foo.class);
		StatementBuilder<Foo, String> qb = new StatementBuilder<Foo, String>(databaseType, tableInfo);
		int limit = 1232;
		qb.limit(limit);
		String query = qb.prepareQueryString();
		assertTrue(query + " should contain LIMIT", query.contains(" LIMIT " + limit + " "));
	}

	@Test(expected = ClassNotFoundException.class)
	public void testLoadDriver() throws Exception {
		if (isDriverClassExpected()) {
			throw new ClassNotFoundException("We have the class so simulate a failure");
		} else {
			databaseType.loadDriver();
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGeneratedIdSequence() throws Exception {
		TableInfo<GeneratedIdSequence> tableInfo =
				new TableInfo<GeneratedIdSequence>(databaseType, GeneratedIdSequence.class);
		assertEquals(2, tableInfo.getFieldTypes().length);
		StringBuilder sb = new StringBuilder();
		ArrayList<String> additionalArgs = new ArrayList<String>();
		ArrayList<String> statementsBefore = new ArrayList<String>();
		ArrayList<String> statementsAfter = new ArrayList<String>();
		List<String> queriesAfter = new ArrayList<String>();
		databaseType.appendColumnArg(sb, tableInfo.getFieldTypes()[0], additionalArgs, statementsBefore,
				statementsAfter, queriesAfter);
	}

	protected static class Foo {
		@DatabaseField(id = true)
		String id;
	}

	protected static class GeneratedId {
		@DatabaseField(generatedId = true)
		public int id;
		@DatabaseField
		String other;
		public GeneratedId() {
		}
	}

	protected static class GeneratedIdSequence {
		@DatabaseField(generatedIdSequence = GENERATED_ID_SEQ)
		public int genId;
		@DatabaseField
		public String stuff;
		protected GeneratedIdSequence() {
		}
	}

	protected static class GeneratedIdSequenceAutoName {
		@DatabaseField(generatedId = true)
		int genId;
		@DatabaseField
		public String stuff;
	}

	protected static class AllTypes {
		@DatabaseField
		String stringField;
		@DatabaseField
		boolean booleanField;
		@DatabaseField
		Date dateField;
		@DatabaseField
		byte byteField;
		@DatabaseField
		short shortField;
		@DatabaseField
		int intField;
		@DatabaseField
		long longField;
		@DatabaseField
		float floatField;
		@DatabaseField
		double doubleField;
		AllTypes() {
		}
	}
}
