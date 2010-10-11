package com.j256.ormlite.field;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

import com.j256.ormlite.BaseOrmLiteCoreTest;
import com.j256.ormlite.support.DatabaseResults;
import com.j256.ormlite.table.DatabaseTable;

public class DataTypeTest extends BaseOrmLiteCoreTest {

	private static final String TABLE_NAME = "foo";

	private static final String STRING_COLUMN = "id";
	private static final String BOOLEAN_COLUMN = "bool";
	private static final String DATE_COLUMN = "date";
	private static final String BYTE_COLUMN = "byteField";
	private static final String SHORT_COLUMN = "shortField";
	private static final String INT_COLUMN = "intField";
	private static final String LONG_COLUMN = "longField";
	private static final String FLOAT_COLUMN = "floatField";
	private static final String DOUBLE_COLUMN = "doubleField";
	private static final String ENUM_COLUMN = "enum";

	private static final String DATE_FORMAT = "MM/dd/yyyy";
	private static final String DATE_STRING = "10/10/2010";
	private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(DATE_FORMAT);
	private static Date DATE;
	private static Timestamp TIMESTAMP;
	private static final int COLUMN = 0;
	private static final String BAD_DATE_STRING = "badDateString";
	private static final String INT_STRING = "5";
	private static final String FLOAT_STRING = "5.0";

	static {
		try {
			DATE = DATE_FORMATTER.parse(DATE_STRING);
			TIMESTAMP = new Timestamp(DATE.getTime());
		} catch (ParseException pe) {
			new RuntimeException("Unable to parse date from " + DATE_STRING);
		}
	}

	@Test
	public void testUnknownClass() {
		assertEquals(DataType.UNKNOWN, DataType.lookupClass(getClass()));
	}

	@Test
	public void testSerializableLookup() {
		assertEquals(DataType.SERIALIZABLE, DataType.lookupClass(Serializable.class));
	}

	@Test
	public void testEnumLookup() {
		assertEquals(DataType.ENUM_STRING, DataType.lookupClass(Enum.class));
	}

	@Test
	public void testBoolean() throws Exception {
		DataType type = DataType.BOOLEAN;
		DatabaseResults results = (DatabaseResults) createMock(DatabaseResults.class);
		Boolean booleanResult = Boolean.TRUE;
		expect(results.getBoolean(COLUMN)).andReturn(booleanResult);
		replay(results);
		assertEquals(booleanResult, type.resultToJava(null, results, COLUMN));
		verify(results);

		assertFalse(type.isEscapeDefaultValue());
		assertTrue(type.isPrimitive());
		assertEquals(Boolean.TRUE, type.parseDefaultString(null, "true"));
		assertEquals(Boolean.FALSE, type.parseDefaultString(null, "false"));
		assertNull(type.resultToId(null, 0));
	}

	@Test
	public void testBooleanObj() throws Exception {
		DataType type = DataType.BOOLEAN_OBJ;
		DatabaseResults results = (DatabaseResults) createMock(DatabaseResults.class);
		Boolean booleanResult = Boolean.TRUE;
		expect(results.getBoolean(COLUMN)).andReturn(booleanResult);
		replay(results);
		assertEquals(booleanResult, type.resultToJava(null, results, COLUMN));
		verify(results);

		assertFalse(type.isEscapeDefaultValue());
		assertEquals(Boolean.TRUE, type.parseDefaultString(null, "true"));
		assertEquals(Boolean.FALSE, type.parseDefaultString(null, "false"));
	}

	@Test
	public void testJavaDate() throws Exception {
		DataType type = DataType.JAVA_DATE;
		DatabaseResults results = (DatabaseResults) createMock(DatabaseResults.class);
		expect(results.getTimestamp(COLUMN)).andReturn(TIMESTAMP);
		replay(results);
		assertEquals(DATE, type.resultToJava(null, results, COLUMN));
		verify(results);

		Timestamp timestamp = new Timestamp(DATE_FORMATTER.parse(DATE_STRING).getTime());
		assertEquals(timestamp, type.parseDefaultString(getFieldType("date"), DATE_STRING));

		timestamp = (Timestamp) type.javaToArg(null, DATE);
		assertEquals(TIMESTAMP, timestamp);
	}

	@Test
	public void testJavaDateLong() throws Exception {
		DataType type = DataType.JAVA_DATE_LONG;
		long millis = 5;
		Date date = new Date(millis);

		DatabaseResults results = (DatabaseResults) createMock(DatabaseResults.class);
		expect(results.getLong(COLUMN)).andReturn(new Long(millis));
		replay(results);
		assertEquals(date, type.resultToJava(null, results, COLUMN));
		verify(results);

		Long expectedLong = (Long) type.javaToArg(null, date);
		assertEquals(millis, expectedLong.longValue());
		assertTrue(type.isNumber());
		String longString = "255";
		assertEquals(new Long(longString), type.parseDefaultString(null, longString));
	}

	@Test(expected = SQLException.class)
	public void testBadJavaDateLong() throws Exception {
		DataType.JAVA_DATE_LONG.parseDefaultString(null, "notALong");
	}

	@Test
	public void testJavaDateString() throws Exception {
		DataType type = DataType.JAVA_DATE_STRING;
		FieldType fieldType = getFieldType("date");
		assertEquals(DATE_STRING, type.javaToArg(fieldType, DATE));
		DatabaseResults results = (DatabaseResults) createMock(DatabaseResults.class);
		expect(results.getString(COLUMN)).andReturn(DATE_STRING);
		replay(results);
		assertEquals(DATE, type.resultToJava(fieldType, results, COLUMN));
		verify(results);
	}

	@Test(expected = SQLException.class)
	public void testJavaBadDateString() throws Exception {
		DataType type = DataType.JAVA_DATE_STRING;
		FieldType fieldType = getFieldType("date");
		DatabaseResults results = (DatabaseResults) createMock(DatabaseResults.class);
		expect(results.getString(COLUMN)).andReturn(BAD_DATE_STRING);
		replay(results);
		type.resultToJava(fieldType, results, COLUMN);
		verify(results);
	}

	@Test
	public void testJavaDateStringParseDefaultString() throws Exception {
		DataType type = DataType.JAVA_DATE_STRING;
		FieldType fieldType = getFieldType("date");
		assertEquals(DATE_STRING, type.parseDefaultString(fieldType, DATE_STRING));
	}

	@Test(expected = SQLException.class)
	public void testJavaDateStringParseBadDefaultString() throws Exception {
		DataType type = DataType.JAVA_DATE_STRING;
		FieldType fieldType = getFieldType("date");
		type.parseDefaultString(fieldType, BAD_DATE_STRING);
	}

	@Test
	public void testByte() throws Exception {
		DataType type = DataType.BYTE;
		byte testByte = Byte.parseByte(INT_STRING);
		assertEquals(new Byte(INT_STRING), type.parseDefaultString(null, INT_STRING));

		DatabaseResults results = (DatabaseResults) createMock(DatabaseResults.class);
		expect(results.getByte(COLUMN)).andReturn(testByte);
		replay(results);
		assertEquals(testByte, type.resultToJava(getFieldType("count"), results, COLUMN));
		verify(results);

		assertTrue(type.isNumber());
		assertTrue(type.isPrimitive());
	}

	@Test
	public void testByteObj() throws Exception {
		DataType type = DataType.BYTE_OBJ;
		byte testByte = Byte.parseByte(INT_STRING);
		assertEquals(new Byte(INT_STRING), type.parseDefaultString(null, INT_STRING));

		DatabaseResults results = (DatabaseResults) createMock(DatabaseResults.class);
		expect(results.getByte(COLUMN)).andReturn(testByte);
		replay(results);
		assertEquals(testByte, type.resultToJava(getFieldType("count"), results, COLUMN));
		verify(results);
		assertTrue(type.isNumber());
		assertFalse(type.isPrimitive());
	}

	@Test
	public void testShort() throws Exception {
		DataType type = DataType.SHORT;
		short testShort = Short.parseShort(INT_STRING);
		assertEquals(new Short(INT_STRING), type.parseDefaultString(null, INT_STRING));

		DatabaseResults results = (DatabaseResults) createMock(DatabaseResults.class);
		expect(results.getShort(COLUMN)).andReturn(testShort);
		replay(results);
		assertEquals(testShort, type.resultToJava(getFieldType("count"), results, COLUMN));
		verify(results);
		assertTrue(type.isNumber());
		assertTrue(type.isPrimitive());
	}

	@Test
	public void testShortObj() throws Exception {
		DataType type = DataType.SHORT_OBJ;
		short testShort = Short.parseShort(INT_STRING);
		assertEquals(new Short(INT_STRING), type.parseDefaultString(null, INT_STRING));

		DatabaseResults results = (DatabaseResults) createMock(DatabaseResults.class);
		expect(results.getShort(COLUMN)).andReturn(testShort);
		replay(results);
		assertEquals(testShort, type.resultToJava(getFieldType("count"), results, COLUMN));
		verify(results);
		assertTrue(type.isNumber());
		assertFalse(type.isPrimitive());
	}

	@Test
	public void testInteger() throws Exception {
		DataType type = DataType.INTEGER;
		int testInt = Integer.parseInt(INT_STRING);
		assertEquals(new Integer(INT_STRING), type.parseDefaultString(null, INT_STRING));

		DatabaseResults results = (DatabaseResults) createMock(DatabaseResults.class);
		expect(results.getInt(COLUMN)).andReturn(testInt);
		expect(results.getInt(COLUMN)).andReturn(testInt);
		replay(results);
		assertEquals(testInt, type.resultToJava(getFieldType("count"), results, COLUMN));
		assertEquals(testInt, type.resultToId(results, COLUMN));
		verify(results);
		assertTrue(type.isNumber());
		assertTrue(type.isPrimitive());
	}

	@Test
	public void testIntegerObj() throws Exception {
		DataType type = DataType.INTEGER_OBJ;
		int testInt = Integer.parseInt(INT_STRING);
		assertEquals(new Integer(INT_STRING), type.parseDefaultString(null, INT_STRING));

		DatabaseResults results = (DatabaseResults) createMock(DatabaseResults.class);
		expect(results.getInt(COLUMN)).andReturn(testInt);
		expect(results.getInt(COLUMN)).andReturn(testInt);
		replay(results);
		assertEquals(testInt, type.resultToJava(getFieldType("count"), results, COLUMN));
		assertEquals(testInt, type.resultToId(results, COLUMN));
		verify(results);
		assertTrue(type.isNumber());
		assertFalse(type.isPrimitive());
	}

	@Test
	public void testLong() throws Exception {
		DataType type = DataType.LONG;
		long testLong = Long.parseLong(INT_STRING);
		assertEquals(new Long(INT_STRING), type.parseDefaultString(null, INT_STRING));

		DatabaseResults results = (DatabaseResults) createMock(DatabaseResults.class);
		expect(results.getLong(COLUMN)).andReturn(testLong);
		expect(results.getLong(COLUMN)).andReturn(testLong);
		replay(results);
		assertEquals(testLong, type.resultToJava(getFieldType("count"), results, COLUMN));
		assertEquals(testLong, type.resultToId(results, COLUMN));
		verify(results);
		assertTrue(type.isNumber());
		assertTrue(type.isPrimitive());
	}

	@Test
	public void testLongObj() throws Exception {
		DataType type = DataType.LONG_OBJ;
		long testLong = Long.parseLong(INT_STRING);
		assertEquals(new Long(INT_STRING), type.parseDefaultString(null, INT_STRING));

		DatabaseResults results = (DatabaseResults) createMock(DatabaseResults.class);
		expect(results.getLong(COLUMN)).andReturn(testLong);
		expect(results.getLong(COLUMN)).andReturn(testLong);
		replay(results);
		assertEquals(testLong, type.resultToJava(getFieldType("count"), results, COLUMN));
		assertEquals(testLong, type.resultToId(results, COLUMN));
		verify(results);
		assertTrue(type.isNumber());
		assertFalse(type.isPrimitive());
	}

	@Test
	public void testFloat() throws Exception {
		DataType type = DataType.FLOAT;
		float testFloat = Float.parseFloat(FLOAT_STRING);
		assertEquals(new Float(FLOAT_STRING), type.parseDefaultString(null, FLOAT_STRING));

		DatabaseResults results = (DatabaseResults) createMock(DatabaseResults.class);
		expect(results.getFloat(COLUMN)).andReturn(testFloat);
		replay(results);
		assertEquals(testFloat, type.resultToJava(getFieldType("count"), results, COLUMN));
		verify(results);
		assertTrue(type.isNumber());
		assertTrue(type.isPrimitive());
	}

	@Test
	public void testFloatObj() throws Exception {
		DataType type = DataType.FLOAT_OBJ;
		float testFloat = Float.parseFloat(FLOAT_STRING);
		assertEquals(new Float(FLOAT_STRING), type.parseDefaultString(null, FLOAT_STRING));

		DatabaseResults results = (DatabaseResults) createMock(DatabaseResults.class);
		expect(results.getFloat(COLUMN)).andReturn(testFloat);
		replay(results);
		assertEquals(testFloat, type.resultToJava(getFieldType("count"), results, COLUMN));
		verify(results);
		assertTrue(type.isNumber());
		assertFalse(type.isPrimitive());
	}

	@Test
	public void testDouble() throws Exception {
		DataType type = DataType.DOUBLE;
		double testDouble = Double.parseDouble(FLOAT_STRING);
		assertEquals(new Double(FLOAT_STRING), type.parseDefaultString(null, FLOAT_STRING));

		DatabaseResults results = (DatabaseResults) createMock(DatabaseResults.class);
		expect(results.getDouble(COLUMN)).andReturn(testDouble);
		replay(results);
		assertEquals(testDouble, type.resultToJava(getFieldType("count"), results, COLUMN));
		verify(results);
		assertTrue(type.isNumber());
		assertTrue(type.isPrimitive());
	}

	@Test
	public void testDoubleObj() throws Exception {
		DataType type = DataType.DOUBLE_OBJ;
		double testDouble = Double.parseDouble(FLOAT_STRING);
		assertEquals(new Double(FLOAT_STRING), type.parseDefaultString(null, FLOAT_STRING));

		DatabaseResults results = (DatabaseResults) createMock(DatabaseResults.class);
		expect(results.getDouble(COLUMN)).andReturn(testDouble);
		replay(results);
		assertEquals(testDouble, type.resultToJava(getFieldType("count"), results, COLUMN));
		verify(results);
		assertTrue(type.isNumber());
		assertFalse(type.isPrimitive());
	}

	@Test(expected = SQLException.class)
	public void testSerializableDefaultNull() throws Exception {
		DataType type = DataType.SERIALIZABLE;
		assertTrue(type.isStreamType());
		type.parseDefaultString(null, null);
	}

	@Test
	public void testSerializable() throws Exception {
		DataType type = DataType.SERIALIZABLE;
		Integer serializable = new Integer(0);
		byte[] bytes = (byte[]) type.javaToArg(null, serializable);
		ObjectInputStream stream = new ObjectInputStream(new ByteArrayInputStream(bytes));
		Object val = stream.readObject();
		assertEquals(serializable, val);

		DatabaseResults results = (DatabaseResults) createMock(DatabaseResults.class);
		expect(results.getBytes(COLUMN)).andReturn(bytes);
		expect(results.getBytes(COLUMN)).andReturn(null);
		replay(results);
		assertEquals(serializable, type.resultToJava(getFieldType("serializable"), results, COLUMN));
		assertNull(type.resultToJava(getFieldType("serializable"), results, COLUMN));
		verify(results);
		assertFalse(type.isNumber());
		assertFalse(type.isPrimitive());
	}

	@Test(expected = SQLException.class)
	public void testBadSerializableBytes() throws Exception {
		DataType type = DataType.SERIALIZABLE;
		DatabaseResults results = (DatabaseResults) createMock(DatabaseResults.class);
		expect(results.getBytes(COLUMN)).andReturn(new byte[] { 1, 2, 3 });
		replay(results);
		type.resultToJava(getFieldType("serializable"), results, COLUMN);
	}

	@Test(expected = SQLException.class)
	public void testSerializableBadObject() throws Exception {
		DataType type = DataType.SERIALIZABLE;
		type.javaToArg(null, new LocalString());
	}

	@Test
	public void testSerializableIsValid() throws Exception {
		DataType type = DataType.SERIALIZABLE;
		assertFalse(type.isValidForType(LocalString.class));
		assertTrue(type.isValidForType(Serializable.class));
	}

	@Test
	public void testEnumString() throws Exception {
		DataType type = DataType.ENUM_STRING;
		AnotherEnum anotherEnum = AnotherEnum.A;
		assertEquals(anotherEnum.name(), type.javaToArg(null, anotherEnum));
		String defaultString = "default";
		assertEquals(defaultString, type.parseDefaultString(null, defaultString));

		DatabaseResults results = (DatabaseResults) createMock(DatabaseResults.class);
		expect(results.getString(COLUMN)).andReturn(AnotherEnum.A.name());
		replay(results);
		assertEquals(AnotherEnum.A, type.resultToJava(getFieldType("grade"), results, COLUMN));
		verify(results);
		assertFalse(type.isNumber());
	}

	@Test
	public void testEnumInteger() throws Exception {
		DataType type = DataType.ENUM_INTEGER;
		AnotherEnum anotherEnum = AnotherEnum.A;
		assertEquals(anotherEnum.ordinal(), type.javaToArg(null, anotherEnum));
		String integerString = "5";
		Integer defaultInteger = new Integer(integerString);
		assertEquals(defaultInteger, type.parseDefaultString(null, integerString));

		DatabaseResults results = (DatabaseResults) createMock(DatabaseResults.class);
		expect(results.getInt(COLUMN)).andReturn(AnotherEnum.A.ordinal());
		replay(results);
		assertEquals(AnotherEnum.A, type.resultToJava(getFieldType("grade"), results, COLUMN));
		verify(results);

		assertTrue(type.isNumber());
	}

	@Test
	public void testUnknown() throws Exception {
		DataType type = DataType.UNKNOWN;
		String defaultString = "5";
		assertNull(type.javaToArg(null, defaultString));
		assertNull(type.parseDefaultString(null, defaultString));
		assertNull(type.resultToJava(null, null, 0));
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalString {
		@DatabaseField(id = true, columnName = STRING_COLUMN)
		String string;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalBoolean {
		@DatabaseField(columnName = BOOLEAN_COLUMN)
		boolean bool;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalDate {
		@DatabaseField(columnName = DATE_COLUMN)
		Date date;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class DateBadFormat {
		@DatabaseField(columnName = DATE_COLUMN, format = "yyyy")
		Date date;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalByte {
		@DatabaseField(columnName = BYTE_COLUMN)
		byte byteField;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalShort {
		@DatabaseField(columnName = SHORT_COLUMN)
		short shortField;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalInt {
		@DatabaseField(columnName = INT_COLUMN)
		int intField;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalLong {
		@DatabaseField(columnName = LONG_COLUMN)
		long longField;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalFloat {
		@DatabaseField(columnName = FLOAT_COLUMN)
		float floatField;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalDouble {
		@DatabaseField(columnName = DOUBLE_COLUMN)
		double doubleField;;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalEnum {
		@DatabaseField(columnName = ENUM_COLUMN)
		OurEnum ourEnum;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalEnumInt {
		@DatabaseField(columnName = ENUM_COLUMN, dataType = DataType.ENUM_INTEGER)
		OurEnum ourEnum;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalEnumInt2 {
		@DatabaseField(columnName = ENUM_COLUMN, dataType = DataType.ENUM_INTEGER)
		OurEnum2 ourEnum;
	}

	private enum OurEnum {
		FIRST,
		SECOND, ;
	}

	private enum OurEnum2 {
		FIRST, ;
	}

	protected enum AnotherEnum {
		A,
		B,
		// end
		;
	}

	private FieldType getFieldType(String fieldName) throws Exception {
		return FieldType.createFieldType(databaseType, "Foo", Foo.class.getDeclaredField(fieldName));
	}

	protected static class Foo {
		@DatabaseField
		String name;
		@DatabaseField
		int count;
		@DatabaseField
		AnotherEnum grade;
		@DatabaseField(format = DATE_FORMAT)
		Date date;
		@DatabaseField
		Integer serializable;
	}
}
