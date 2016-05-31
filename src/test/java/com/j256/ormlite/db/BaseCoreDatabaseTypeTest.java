package com.j256.ormlite.db;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.TestUtils;
import com.j256.ormlite.db.BaseDatabaseType.BooleanNumberFieldConverter;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.FieldConverter;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.DateStringType;
import com.j256.ormlite.field.types.EnumStringType;
import com.j256.ormlite.field.types.StringType;
import com.j256.ormlite.field.types.UuidType;
import com.j256.ormlite.support.DatabaseResults;

public class BaseCoreDatabaseTypeTest extends BaseCoreTest {

	private FieldConverter booleanFieldConverter = new BooleanNumberFieldConverter();

	@Test
	public void testBaseDatabaseType() {
		assertEquals("-- ", databaseType.getCommentLinePrefix());
		String word = "word";
		assertEquals("'" + word + "'", TestUtils.appendEscapedWord(databaseType, word));
	}

	@Test
	public void testLoadDriver() throws Exception {
		databaseType.loadDriver();
	}

	@Test
	public void testCreateTableReturnsZero() {
		assertTrue(databaseType.isCreateTableReturnsZero());
	}

	@Test
	public void testAppendColumnString() throws Exception {
		testFooColumn(databaseType, "string", "VARCHAR(" + StringType.DEFAULT_WIDTH + ")");
	}

	@Test
	public void testAppendColumnNoFieldWidth() throws Exception {
		testFooColumn(new OurDbTypeNoFieldWidth(), "string", "VARCHAR");
	}

	@Test
	public void testAppendColumnLongString() throws Exception {
		testFooColumn(databaseType, "longString", "TEXT");
	}

	@Test
	public void testAppendColumnBoolean() throws Exception {
		testFooColumn(databaseType, "bool", "BOOLEAN");
	}

	@Test
	public void testAppendColumnDate() throws Exception {
		testFooColumn(databaseType, "date", "TIMESTAMP");
	}

	@Test
	public void testAppendColumnDateLong() throws Exception {
		testFooColumn(databaseType, "dateLong", "BIGINT");
	}

	@Test
	public void testAppendColumnDateString() throws Exception {
		testFooColumn(databaseType, "dateString", "VARCHAR(" + DateStringType.DEFAULT_WIDTH + ")");
	}

	@Test
	public void testAppendColumnByte() throws Exception {
		testFooColumn(databaseType, "byteField", "TINYINT");
	}

	@Test
	public void testAppendColumnShort() throws Exception {
		testFooColumn(databaseType, "shortField", "SMALLINT");
	}

	@Test
	public void testAppendColumnInt() throws Exception {
		testFooColumn(databaseType, "intField", "INTEGER");
	}

	@Test
	public void testAppendColumnLong() throws Exception {
		testFooColumn(databaseType, "longField", "BIGINT");
	}

	@Test
	public void testAppendColumnFloat() throws Exception {
		testFooColumn(databaseType, "floatField", "FLOAT");
	}

	@Test
	public void testAppendColumnDouble() throws Exception {
		testFooColumn(databaseType, "doubleField", "DOUBLE PRECISION");
	}

	@Test
	public void testAppendColumnSerialized() throws Exception {
		testFooColumn(databaseType, "serialized", "BLOB");
	}

	@Test
	public void testAppendColumnEnumInt() throws Exception {
		testFooColumn(databaseType, "enumInt", "INTEGER");
	}

	@Test
	public void testAppendColumnEnumString() throws Exception {
		testFooColumn(databaseType, "enumString", "VARCHAR(" + EnumStringType.DEFAULT_WIDTH + ")");
	}

	@Test
	public void testAppendColumnUuidString() throws Exception {
		testFooColumn(databaseType, "uuid", "VARCHAR(" + UuidType.DEFAULT_WIDTH + ")");
	}

	@Test
	public void testAppendColumnId() throws Exception {
		testFooColumn(databaseType, "id", "BIGINT");
	}

	@Test
	public void testAppendColumnGenId() throws Exception {
		testFooColumn(databaseType, "genId", "BIGINT AUTO_INCREMENT");
	}

	@Test(expected = SQLException.class)
	public void testAppendColumnGenIdSeq() throws Exception {
		testFooColumn(databaseType, "genIdSeq", "");
	}

	private final static String DEFAULT_VALUE_NUMBER = "0";
	private final static String DEFAULT_VALUE_STRING = "table";

	@Test
	public void testAppendColumnDefaultValueNumber() throws Exception {
		testFooColumn(databaseType, "defaultValueNumber", "BIGINT DEFAULT " + DEFAULT_VALUE_NUMBER);
	}

	@Test
	public void testAppendColumnDefaultValueString() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("VARCHAR(").append(StringType.DEFAULT_WIDTH).append(") DEFAULT ");
		databaseType.appendEscapedWord(sb, DEFAULT_VALUE_STRING);
		testFooColumn(databaseType, "defaultValueString", sb.toString());
	}

	@Test
	public void testAppendColumnCanBeNull() throws Exception {
		testFooColumn(databaseType, "canBeNull", "BIGINT NOT NULL");
	}

	@Test
	public void testIsLimitSqlSupported() {
		assertTrue(databaseType.isLimitSqlSupported());
	}

	@Test
	public void testIsLimitAfterSelect() {
		assertFalse(databaseType.isLimitAfterSelect());
	}

	@Test
	public void testBooleanConverterJavaToArg() throws Exception {
		FieldType fieldType =
				FieldType.createFieldType(connectionSource, "foo", ManyFields.class.getDeclaredField("bool"),
						ManyFields.class);
		assertEquals(Byte.valueOf((byte) 1), booleanFieldConverter.javaToSqlArg(fieldType, Boolean.TRUE));
		assertEquals(Byte.valueOf((byte) 0), booleanFieldConverter.javaToSqlArg(fieldType, Boolean.FALSE));
	}

	@Test
	public void testBooleanConverterResultToJava() throws Exception {
		DatabaseResults results = createMock(DatabaseResults.class);
		boolean first = Boolean.TRUE;
		boolean second = Boolean.FALSE;
		expect(results.getByte(1)).andReturn((byte) 1);
		expect(results.getByte(2)).andReturn((byte) 0);
		replay(results);
		FieldType fieldType =
				FieldType.createFieldType(connectionSource, "foo", ManyFields.class.getDeclaredField("bool"),
						ManyFields.class);
		assertEquals(first, booleanFieldConverter.resultToJava(fieldType, results, 1));
		assertEquals(second, booleanFieldConverter.resultToJava(fieldType, results, 2));
		verify(results);
	}

	@Test
	public void testBooleanConverterParseDefaultString() throws Exception {
		FieldType fieldType =
				FieldType.createFieldType(connectionSource, "foo", ManyFields.class.getDeclaredField("bool"),
						ManyFields.class);
		assertEquals(Byte.valueOf((byte) 1),
				booleanFieldConverter.parseDefaultString(fieldType, Boolean.TRUE.toString()));
		assertEquals(Byte.valueOf((byte) 0),
				booleanFieldConverter.parseDefaultString(fieldType, Boolean.FALSE.toString()));
	}

	@Test
	public void testBooleanConverterIsStreamType() {
		assertFalse(booleanFieldConverter.isStreamType());
	}

	@Test
	public void testBooleanConverterGetSqlType() {
		assertEquals(SqlType.BOOLEAN, booleanFieldConverter.getSqlType());
	}

	@Test
	public void testDropColumnNoop() {
		databaseType.dropColumnArg(null, null, null);
	}

	@Test
	public void testAppendSelectNextValFromSequence() {
		databaseType.appendSelectNextValFromSequence(null, null);
	}

	@Test
	public void testAppendCreateTableSuffix() {
		databaseType.appendCreateTableSuffix(null);
	}

	@Test
	public void testGenerateIdSequenceName() {
		String table = "foo";
		assertEquals(table + BaseDatabaseType.DEFAULT_SEQUENCE_SUFFIX, databaseType.generateIdSequenceName(table, null));
	}

	@Test
	public void testGenerateIdSequenceNameUppercaseEntities() {
		String table = "foo";
		String name = table + BaseDatabaseType.DEFAULT_SEQUENCE_SUFFIX;
		assertEquals(name.toUpperCase(), new OurDbTypeUppercaseEntities().generateIdSequenceName(table, null));
	}

	@Test
	public void testGeneratedIdWorks() throws Exception {
		OurDbTypeGeneratedId ourDbType = new OurDbTypeGeneratedId();
		String fieldName = "genId";
		testFooColumn(ourDbType, fieldName, "BIGINT");
		assertEquals(fieldName, ourDbType.fieldType.getFieldName());
	}

	@Test
	public void testGeneratedIdSeqWorks() throws Exception {
		OurDbTypeGeneratedId ourDbType = new OurDbTypeGeneratedId();
		String fieldName = "genIdSeq";
		testFooColumn(ourDbType, fieldName, "BIGINT");
		assertEquals(fieldName, ourDbType.fieldType.getFieldName());
	}

	@Test
	public void testAppendLimitValue() {
		StringBuilder sb = new StringBuilder();
		long limit = 122;
		databaseType.appendLimitValue(sb, limit, null);
		assertEquals("LIMIT " + limit + " ", sb.toString());
	}

	/* ================================================================================================================ */

	private void testFooColumn(DatabaseType databaseType, String fieldName, String expected) throws Exception {
		StringBuilder sb = new StringBuilder();
		List<String> additionalArgs = new ArrayList<String>();
		List<String> stmtsBefore = new ArrayList<String>();
		List<String> stmtsAfter = new ArrayList<String>();
		List<String> queriesAfter = new ArrayList<String>();
		FieldType fieldType =
				FieldType.createFieldType(connectionSource, "foo", ManyFields.class.getDeclaredField(fieldName),
						ManyFields.class);
		databaseType.appendColumnArg(null, sb, fieldType, additionalArgs, stmtsBefore, stmtsAfter, queriesAfter);
		StringBuilder expectedSb = new StringBuilder();
		databaseType.appendEscapedEntityName(expectedSb, fieldName);
		expectedSb.append(' ').append(expected).append(' ');
		assertEquals(expectedSb.toString(), sb.toString());
	}

	private static class OurDbType extends BaseDatabaseType {
		@Override
		public String getDriverClassName() {
			return "driver.class";
		}
		@Override
		public String getDatabaseName() {
			return "fake";
		}
		@Override
		public boolean isDatabaseUrlThisType(String url, String dbTypePart) {
			return false;
		}
	}

	private static class OurDbTypeNoFieldWidth extends OurDbType {
		@Override
		public boolean isVarcharFieldWidthSupported() {
			return false;
		}
	}

	private static class OurDbTypeUppercaseEntities extends OurDbType {
		@Override
		public boolean isEntityNamesMustBeUpCase() {
			return true;
		}
	}

	private static class OurDbTypeGeneratedId extends OurDbType {
		FieldType fieldType;
		@Override
		protected void configureGeneratedId(String tableName, StringBuilder sb, FieldType fieldType,
				List<String> statementsBefore, List<String> statementsAfter, List<String> additionalArgs,
				List<String> queriesAfter) {
			this.fieldType = fieldType;
		}
		@Override
		protected void configureGeneratedIdSequence(StringBuilder sb, FieldType fieldType,
				List<String> statementsBefore, List<String> additionalArgs, List<String> queriesAfter) {
			this.fieldType = fieldType;
		}
	}

	protected class ManyFields {
		@DatabaseField
		String string;
		@DatabaseField(dataType = DataType.LONG_STRING)
		String longString;
		@DatabaseField
		boolean bool;
		@DatabaseField
		Date date;
		@DatabaseField(dataType = DataType.DATE_LONG)
		Date dateLong;
		@DatabaseField(dataType = DataType.DATE_STRING)
		Date dateString;
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
		@DatabaseField(dataType = DataType.SERIALIZABLE)
		SerialClass serialized;
		@DatabaseField(dataType = DataType.ENUM_INTEGER)
		OurEnum enumInt;
		@DatabaseField
		OurEnum enumString;
		@DatabaseField
		UUID uuid;

		@DatabaseField(id = true)
		long id;
		@DatabaseField(generatedId = true)
		long genId;
		@DatabaseField(generatedIdSequence = "foo")
		long genIdSeq;

		@DatabaseField(defaultValue = DEFAULT_VALUE_NUMBER)
		long defaultValueNumber;
		@DatabaseField(defaultValue = DEFAULT_VALUE_STRING)
		String defaultValueString;
		@DatabaseField(canBeNull = false)
		long canBeNull;
		@DatabaseField(unique = true)
		long unique;
	}

	private static class SerialClass implements Serializable {
		private static final long serialVersionUID = 4092506968116021313L;
	}

	private enum OurEnum {
		BIG,
		LITTLE,
		// end
		;
	}
}
