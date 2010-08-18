package com.j256.ormlite.db;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.j256.ormlite.TestUtils;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.table.TableInfo;

public class PostgresDatabaseTypeTest extends BaseDatabaseTest {

	@Override
	protected void setDatabaseParams() throws SQLException {
		databaseUrl = "jdbc:postgresql:ormlitepostgres";
		connectionSource = DatabaseTypeUtils.createJdbcConnectionSource(DEFAULT_DATABASE_URL);
	}

	@Override
	protected boolean isDriverClassExpected() {
		return false;
	}

	@Override
	@Test
	public void testEscapedEntityName() throws Exception {
		String word = "word";
		assertEquals("\"" + word + "\"", TestUtils.appendEscapedEntityName(databaseType, word));
	}

	@Test(expected = IllegalStateException.class)
	public void testBadGeneratedId() throws Exception {
		Field field = GeneratedId.class.getField("id");
		DatabaseType mockDb = createMock(DatabaseType.class);
		expect(mockDb.isIdSequenceNeeded()).andReturn(false);
		expect(mockDb.getFieldConverter(isA(FieldType.class))).andReturn(null);
		expect(mockDb.convertColumnName(isA(String.class))).andReturn("id");
		expect(mockDb.isEntityNamesMustBeUpCase()).andReturn(false);
		replay(mockDb);
		FieldType fieldType = FieldType.createFieldType(mockDb, "foo", field);
		verify(mockDb);
		StringBuilder sb = new StringBuilder();
		List<String> statementsBefore = new ArrayList<String>();
		databaseType.appendColumnArg(sb, fieldType, null, statementsBefore, null, null);
	}

	@Test
	public void testDropSequence() throws Exception {
		Field field = GeneratedId.class.getField("id");
		FieldType fieldType = FieldType.createFieldType(databaseType, "foo", field);
		List<String> statementsBefore = new ArrayList<String>();
		List<String> statementsAfter = new ArrayList<String>();
		databaseType.dropColumnArg(fieldType, statementsBefore, statementsAfter);
		assertEquals(0, statementsBefore.size());
		assertEquals(1, statementsAfter.size());
		assertTrue(statementsAfter.get(0).contains("DROP SEQUENCE "));
	}

	@Test
	@Override
	public void testGeneratedIdSequence() throws Exception {
		TableInfo<GeneratedIdSequence> tableInfo =
				new TableInfo<GeneratedIdSequence>(databaseType, GeneratedIdSequence.class);
		assertEquals(2, tableInfo.getFieldTypes().length);
		StringBuilder sb = new StringBuilder();
		List<String> additionalArgs = new ArrayList<String>();
		List<String> statementsBefore = new ArrayList<String>();
		List<String> queriesAfter = new ArrayList<String>();
		databaseType.appendColumnArg(sb, tableInfo.getFieldTypes()[0], additionalArgs, statementsBefore, null,
				queriesAfter);
		assertTrue(sb.toString().contains(" DEFAULT NEXTVAL('\"" + GENERATED_ID_SEQ + "\"')"));
		assertEquals(1, statementsBefore.size());
		assertTrue(statementsBefore.get(0).contains(GENERATED_ID_SEQ));
		assertEquals(1, additionalArgs.size());
		assertTrue(additionalArgs.get(0).contains("PRIMARY KEY"));
		assertEquals(0, queriesAfter.size());
	}

	@Test
	public void testGeneratedIdSequenceAutoName() throws Exception {
		TableInfo<GeneratedIdSequenceAutoName> tableInfo =
				new TableInfo<GeneratedIdSequenceAutoName>(databaseType, GeneratedIdSequenceAutoName.class);
		assertEquals(2, tableInfo.getFieldTypes().length);
		FieldType idField = tableInfo.getFieldTypes()[0];
		StringBuilder sb = new StringBuilder();
		List<String> additionalArgs = new ArrayList<String>();
		List<String> statementsBefore = new ArrayList<String>();
		List<String> queriesAfter = new ArrayList<String>();
		databaseType.appendColumnArg(sb, idField, additionalArgs, statementsBefore, null, queriesAfter);
		String seqName =
				databaseType.generateIdSequenceName(GeneratedIdSequenceAutoName.class.getSimpleName().toLowerCase(),
						idField);
		assertTrue(sb.toString().contains(" DEFAULT NEXTVAL('\"" + seqName + "\"')"));
		assertEquals(1, statementsBefore.size());
		assertTrue(statementsBefore.get(0).contains(seqName));
		assertEquals(1, additionalArgs.size());
		assertTrue(additionalArgs.get(0).contains("PRIMARY KEY"));
		assertEquals(0, queriesAfter.size());
	}

	@Test
	public void testBoolean() throws Exception {
		TableInfo<AllTypes> tableInfo = new TableInfo<AllTypes>(databaseType, AllTypes.class);
		assertEquals(9, tableInfo.getFieldTypes().length);
		FieldType booleanField = tableInfo.getFieldTypes()[1];
		assertEquals("booleanField", booleanField.getDbColumnName());
		StringBuilder sb = new StringBuilder();
		List<String> additionalArgs = new ArrayList<String>();
		List<String> statementsBefore = new ArrayList<String>();
		databaseType.appendColumnArg(sb, booleanField, additionalArgs, statementsBefore, null, null);
		assertTrue(sb.toString().contains("BOOLEAN"));
	}

	@Test
	public void testByte() throws Exception {
		TableInfo<AllTypes> tableInfo = new TableInfo<AllTypes>(databaseType, AllTypes.class);
		assertEquals(9, tableInfo.getFieldTypes().length);
		FieldType byteField = tableInfo.getFieldTypes()[3];
		assertEquals("byteField", byteField.getDbColumnName());
		StringBuilder sb = new StringBuilder();
		List<String> additionalArgs = new ArrayList<String>();
		List<String> statementsBefore = new ArrayList<String>();
		databaseType.appendColumnArg(sb, byteField, additionalArgs, statementsBefore, null, null);
		assertTrue(sb.toString().contains("SMALLINT"));
	}

	@Test
	public void testGetDriverClassName() {
		assertEquals("org.postgresql.Driver", databaseType.getDriverClassName());
	}
}
