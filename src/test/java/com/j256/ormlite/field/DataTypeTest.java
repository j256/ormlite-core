package com.j256.ormlite.field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

import com.j256.ormlite.BaseOrmLiteTest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.DatabaseResults;
import com.j256.ormlite.table.DatabaseTable;

public class DataTypeTest extends BaseOrmLiteTest {

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

	@Test
	public void testString() throws Exception {
		Dao<LocalString, Object> fooDao = createDao(LocalString.class, true);
		String string = "str";
		LocalString foo = new LocalString();
		foo.string = string;
		assertEquals(1, fooDao.create(foo));

		CompiledStatement stmt =
				connectionSource.getReadOnlyConnection().compileStatement("select * from " + TABLE_NAME);
		DatabaseResults results = stmt.executeQuery();
		assertTrue(results.next());
		assertEquals(string, DataType.STRING.resultToJava(null, results, results.findColumn(STRING_COLUMN)));
		assertFalse(DataType.STRING.isValidGeneratedType());
	}

	@Test
	public void testBoolean() throws Exception {
		Dao<LocalBoolean, Object> fooDao = createDao(LocalBoolean.class, true);
		boolean bool = true;
		LocalBoolean foo = new LocalBoolean();
		foo.bool = bool;
		assertEquals(1, fooDao.create(foo));

		CompiledStatement stmt =
				connectionSource.getReadOnlyConnection().compileStatement("select * from " + TABLE_NAME);
		DatabaseResults results = stmt.executeQuery();
		assertTrue(results.next());
		FieldType fieldType =
				FieldType.createFieldType(databaseType, "table", LocalBoolean.class.getDeclaredField(BOOLEAN_COLUMN));
		assertEquals(bool, DataType.BOOLEAN.resultToJava(fieldType, results, results.findColumn(BOOLEAN_COLUMN)));
		assertFalse(DataType.BOOLEAN.isValidGeneratedType());
	}

	@Test
	public void testDate() throws Exception {
		Dao<LocalDate, Object> fooDao = createDao(LocalDate.class, true);
		Date date = new Date();
		LocalDate foo = new LocalDate();
		foo.date = date;
		assertEquals(1, fooDao.create(foo));
		Field[] fields = LocalDate.class.getDeclaredFields();
		assertTrue(fields.length > 0);
		Field dateField = fields[0];

		CompiledStatement stmt =
				connectionSource.getReadOnlyConnection().compileStatement("select * from " + TABLE_NAME);
		DatabaseResults results = stmt.executeQuery();
		assertTrue(results.next());
		assertEquals(date, DataType.JAVA_DATE.resultToJava(null, results, results.findColumn(DATE_COLUMN)));
		assertEquals(new Timestamp(date.getTime()), DataType.JAVA_DATE.javaToArg(null, date));
		assertFalse(DataType.JAVA_DATE.isValidGeneratedType());
		String format = "yyyy-MM-dd HH:mm:ss.SSSSSS";
		DateFormat dateFormat = new SimpleDateFormat(format);
		FieldType fieldType = FieldType.createFieldType(databaseType, TABLE_NAME, dateField);
		assertEquals(new Timestamp(date.getTime()), DataType.JAVA_DATE.parseDefaultString(fieldType,
				dateFormat.format(date)));
	}

	@Test
	public void testByte() throws Exception {
		Dao<LocalByte, Object> fooDao = createDao(LocalByte.class, true);
		byte byteField = 123;
		LocalByte foo = new LocalByte();
		foo.byteField = byteField;
		assertEquals(1, fooDao.create(foo));

		CompiledStatement stmt =
				connectionSource.getReadOnlyConnection().compileStatement("select * from " + TABLE_NAME);
		DatabaseResults results = stmt.executeQuery();
		assertTrue(results.next());
		FieldType fieldType =
				FieldType.createFieldType(databaseType, "table", LocalByte.class.getDeclaredField(BYTE_COLUMN));
		assertEquals(byteField, DataType.BYTE.resultToJava(fieldType, results, results.findColumn(BYTE_COLUMN)));
		assertFalse(DataType.BYTE.isValidGeneratedType());
	}

	@Test
	public void testShort() throws Exception {
		Dao<LocalShort, Object> fooDao = createDao(LocalShort.class, true);
		short shortField = 12312;
		LocalShort foo = new LocalShort();
		foo.shortField = shortField;
		assertEquals(1, fooDao.create(foo));

		CompiledStatement stmt =
				connectionSource.getReadOnlyConnection().compileStatement("select * from " + TABLE_NAME);
		DatabaseResults results = stmt.executeQuery();
		assertTrue(results.next());
		FieldType fieldType =
				FieldType.createFieldType(databaseType, "table", LocalShort.class.getDeclaredField(SHORT_COLUMN));
		assertEquals(shortField, DataType.SHORT.resultToJava(fieldType, results, results.findColumn(SHORT_COLUMN)));
		assertFalse(DataType.SHORT.isValidGeneratedType());
	}

	@Test
	public void testInt() throws Exception {
		Dao<LocalInt, Object> fooDao = createDao(LocalInt.class, true);
		int integer = 313213123;
		LocalInt foo = new LocalInt();
		foo.intField = integer;
		assertEquals(1, fooDao.create(foo));

		CompiledStatement stmt =
				connectionSource.getReadOnlyConnection().compileStatement("select * from " + TABLE_NAME);
		DatabaseResults results = stmt.executeQuery();
		assertTrue(results.next());
		FieldType fieldType =
				FieldType.createFieldType(databaseType, "table", LocalInt.class.getDeclaredField(INT_COLUMN));
		assertEquals(integer, DataType.INTEGER.resultToJava(fieldType, results, results.findColumn(INT_COLUMN)));
		assertTrue(DataType.INTEGER.isValidGeneratedType());
	}

	@Test
	public void testLong() throws Exception {
		Dao<LocalLong, Object> fooDao = createDao(LocalLong.class, true);
		long longInt = 13312321312312L;
		LocalLong foo = new LocalLong();
		foo.longField = longInt;
		assertEquals(1, fooDao.create(foo));

		CompiledStatement stmt =
				connectionSource.getReadOnlyConnection().compileStatement("select * from " + TABLE_NAME);
		DatabaseResults results = stmt.executeQuery();
		assertTrue(results.next());
		FieldType fieldType =
				FieldType.createFieldType(databaseType, "table", LocalLong.class.getDeclaredField(LONG_COLUMN));
		assertEquals(longInt, DataType.LONG.resultToJava(fieldType, results, results.findColumn(LONG_COLUMN)));
		assertTrue(DataType.LONG.isValidGeneratedType());
	}

	@Test
	public void testFloat() throws Exception {
		Dao<LocalFloat, Object> fooDao = createDao(LocalFloat.class, true);
		float floatField = 1331.221F;
		LocalFloat foo = new LocalFloat();
		foo.floatField = floatField;
		assertEquals(1, fooDao.create(foo));

		CompiledStatement stmt =
				connectionSource.getReadOnlyConnection().compileStatement("select * from " + TABLE_NAME);
		DatabaseResults results = stmt.executeQuery();
		assertTrue(results.next());
		FieldType fieldType =
				FieldType.createFieldType(databaseType, "table", LocalFloat.class.getDeclaredField(FLOAT_COLUMN));
		assertEquals(floatField, DataType.FLOAT.resultToJava(fieldType, results, results.findColumn(FLOAT_COLUMN)));
		assertFalse(DataType.FLOAT.isValidGeneratedType());
	}

	@Test
	public void testDouble() throws Exception {
		Dao<LocalDouble, Object> fooDao = createDao(LocalDouble.class, true);
		double doubleField = 13313323131.221;
		LocalDouble foo = new LocalDouble();
		foo.doubleField = doubleField;
		assertEquals(1, fooDao.create(foo));

		CompiledStatement stmt =
				connectionSource.getReadOnlyConnection().compileStatement("select * from " + TABLE_NAME);
		DatabaseResults results = stmt.executeQuery();
		assertTrue(results.next());
		FieldType fieldType =
				FieldType.createFieldType(databaseType, "table", LocalDouble.class.getDeclaredField(DOUBLE_COLUMN));
		assertEquals(doubleField, DataType.DOUBLE.resultToJava(fieldType, results, results.findColumn(DOUBLE_COLUMN)));
		assertFalse(DataType.DOUBLE.isValidGeneratedType());
	}

	@Test
	public void testEnum() throws Exception {
		Dao<LocalEnum, Object> fooDao = createDao(LocalEnum.class, true);
		OurEnum ourEnum = OurEnum.SECOND;
		LocalEnum foo = new LocalEnum();
		foo.ourEnum = ourEnum;
		assertEquals(1, fooDao.create(foo));

		Field[] fields = LocalEnum.class.getDeclaredFields();
		assertTrue(fields.length > 0);
		FieldType fieldType = FieldType.createFieldType(databaseType, TABLE_NAME, fields[0]);

		CompiledStatement stmt =
				connectionSource.getReadOnlyConnection().compileStatement("select * from " + TABLE_NAME);
		DatabaseResults results = stmt.executeQuery();
		assertTrue(results.next());
		assertEquals(ourEnum, DataType.ENUM_STRING.resultToJava(fieldType, results, results.findColumn(ENUM_COLUMN)));
		assertFalse(DataType.ENUM_STRING.isValidGeneratedType());
	}

	@Test
	public void testEnumInt() throws Exception {
		Dao<LocalEnumInt, Object> fooDao = createDao(LocalEnumInt.class, true);
		OurEnum ourEnum = OurEnum.SECOND;
		LocalEnumInt foo = new LocalEnumInt();
		foo.ourEnum = ourEnum;
		assertEquals(1, fooDao.create(foo));

		Field[] fields = LocalEnum.class.getDeclaredFields();
		assertTrue(fields.length > 0);
		FieldType fieldType = FieldType.createFieldType(databaseType, TABLE_NAME, fields[0]);

		CompiledStatement stmt =
				connectionSource.getReadOnlyConnection().compileStatement("select * from " + TABLE_NAME);
		DatabaseResults results = stmt.executeQuery();
		assertTrue(results.next());
		assertEquals(ourEnum, DataType.ENUM_INTEGER.resultToJava(fieldType, results, results.findColumn(ENUM_COLUMN)));
		assertFalse(DataType.ENUM_INTEGER.isValidGeneratedType());
	}

	@Test
	public void testUnknownGetResult() throws Exception {
		Dao<LocalLong, Object> fooDao = createDao(LocalLong.class, true);
		LocalLong foo = new LocalLong();
		assertEquals(1, fooDao.create(foo));

		CompiledStatement stmt =
				connectionSource.getReadOnlyConnection().compileStatement("select * from " + TABLE_NAME);
		DatabaseResults results = stmt.executeQuery();
		assertTrue(results.next());

		assertNull(DataType.UNKNOWN.resultToJava(null, results, 1));
		assertFalse(DataType.UNKNOWN.isValidGeneratedType());
	}

	@Test
	public void testUnknownClass() {
		assertEquals(DataType.UNKNOWN, DataType.lookupClass(getClass()));
	}

	@Test
	public void testUnknownTypeVal() {
		assertEquals(DataType.UNKNOWN, DataType.lookupIdTypeVal(10120));
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
}
