package com.j256.ormlite.field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

import org.junit.Test;

import com.j256.ormlite.BaseOrmLiteTest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.table.DatabaseTable;

public class JdbcTypeTest extends BaseOrmLiteTest {

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

		PreparedStatement stmt = dataSource.getConnection().prepareStatement("select * from " + TABLE_NAME);
		assertTrue(stmt.execute());

		ResultSet resultSet = stmt.getResultSet();
		assertTrue(resultSet.next());
		assertEquals(string, JdbcType.STRING.resultToJava(null, resultSet, resultSet.findColumn(STRING_COLUMN)));
		assertFalse(JdbcType.STRING.isValidGeneratedType());
	}

	@Test
	public void testBoolean() throws Exception {
		Dao<LocalBoolean, Object> fooDao = createDao(LocalBoolean.class, true);
		boolean bool = true;
		LocalBoolean foo = new LocalBoolean();
		foo.bool = bool;
		assertEquals(1, fooDao.create(foo));

		PreparedStatement stmt = dataSource.getConnection().prepareStatement("select * from " + TABLE_NAME);
		assertTrue(stmt.execute());

		ResultSet resultSet = stmt.getResultSet();
		assertTrue(resultSet.next());
		FieldType fieldType =
				FieldType.createFieldType(databaseType, "table", LocalBoolean.class.getDeclaredField(BOOLEAN_COLUMN));
		assertEquals(bool, JdbcType.BOOLEAN.resultToJava(fieldType, resultSet, resultSet.findColumn(BOOLEAN_COLUMN)));
		assertFalse(JdbcType.BOOLEAN.isValidGeneratedType());
	}

	@Test
	public void testDate() throws Exception {
		Dao<LocalDate, Object> fooDao = createDao(LocalDate.class, true);
		Date date = new Date();
		LocalDate foo = new LocalDate();
		foo.date = date;
		assertEquals(1, fooDao.create(foo));

		PreparedStatement stmt = dataSource.getConnection().prepareStatement("select * from " + TABLE_NAME);
		assertTrue(stmt.execute());

		ResultSet resultSet = stmt.getResultSet();
		assertTrue(resultSet.next());
		assertEquals(date, JdbcType.JAVA_DATE.resultToJava(null, resultSet, resultSet.findColumn(DATE_COLUMN)));
		assertFalse(JdbcType.JAVA_DATE.isValidGeneratedType());
	}

	@Test
	public void testByte() throws Exception {
		Dao<LocalByte, Object> fooDao = createDao(LocalByte.class, true);
		byte byteField = 123;
		LocalByte foo = new LocalByte();
		foo.byteField = byteField;
		assertEquals(1, fooDao.create(foo));

		PreparedStatement stmt = dataSource.getConnection().prepareStatement("select * from " + TABLE_NAME);
		assertTrue(stmt.execute());

		ResultSet resultSet = stmt.getResultSet();
		assertTrue(resultSet.next());
		FieldType fieldType =
				FieldType.createFieldType(databaseType, "table", LocalByte.class.getDeclaredField(BYTE_COLUMN));
		assertEquals(byteField, JdbcType.BYTE.resultToJava(fieldType, resultSet, resultSet.findColumn(BYTE_COLUMN)));
		assertFalse(JdbcType.BYTE.isValidGeneratedType());
	}

	@Test
	public void testShort() throws Exception {
		Dao<LocalShort, Object> fooDao = createDao(LocalShort.class, true);
		short shortField = 12312;
		LocalShort foo = new LocalShort();
		foo.shortField = shortField;
		assertEquals(1, fooDao.create(foo));

		PreparedStatement stmt = dataSource.getConnection().prepareStatement("select * from " + TABLE_NAME);
		assertTrue(stmt.execute());

		ResultSet resultSet = stmt.getResultSet();
		assertTrue(resultSet.next());
		FieldType fieldType =
				FieldType.createFieldType(databaseType, "table", LocalShort.class.getDeclaredField(SHORT_COLUMN));
		assertEquals(shortField, JdbcType.SHORT.resultToJava(fieldType, resultSet, resultSet.findColumn(SHORT_COLUMN)));
		assertFalse(JdbcType.SHORT.isValidGeneratedType());
	}

	@Test
	public void testInt() throws Exception {
		Dao<LocalInt, Object> fooDao = createDao(LocalInt.class, true);
		int integer = 313213123;
		LocalInt foo = new LocalInt();
		foo.intField = integer;
		assertEquals(1, fooDao.create(foo));

		PreparedStatement stmt = dataSource.getConnection().prepareStatement("select * from " + TABLE_NAME);
		assertTrue(stmt.execute());

		ResultSet resultSet = stmt.getResultSet();
		assertTrue(resultSet.next());
		FieldType fieldType =
				FieldType.createFieldType(databaseType, "table", LocalInt.class.getDeclaredField(INT_COLUMN));
		assertEquals(integer, JdbcType.INTEGER.resultToJava(fieldType, resultSet, resultSet.findColumn(INT_COLUMN)));
		assertTrue(JdbcType.INTEGER.isValidGeneratedType());
	}

	@Test
	public void testLong() throws Exception {
		Dao<LocalLong, Object> fooDao = createDao(LocalLong.class, true);
		long longInt = 13312321312312L;
		LocalLong foo = new LocalLong();
		foo.longField = longInt;
		assertEquals(1, fooDao.create(foo));

		PreparedStatement stmt = dataSource.getConnection().prepareStatement("select * from " + TABLE_NAME);
		assertTrue(stmt.execute());

		ResultSet resultSet = stmt.getResultSet();
		assertTrue(resultSet.next());
		FieldType fieldType =
				FieldType.createFieldType(databaseType, "table", LocalLong.class.getDeclaredField(LONG_COLUMN));
		assertEquals(longInt, JdbcType.LONG.resultToJava(fieldType, resultSet, resultSet.findColumn(LONG_COLUMN)));
		assertTrue(JdbcType.LONG.isValidGeneratedType());
	}

	@Test
	public void testFloat() throws Exception {
		Dao<LocalFloat, Object> fooDao = createDao(LocalFloat.class, true);
		float floatField = 1331.221F;
		LocalFloat foo = new LocalFloat();
		foo.floatField = floatField;
		assertEquals(1, fooDao.create(foo));

		PreparedStatement stmt = dataSource.getConnection().prepareStatement("select * from " + TABLE_NAME);
		assertTrue(stmt.execute());

		ResultSet resultSet = stmt.getResultSet();
		assertTrue(resultSet.next());
		FieldType fieldType =
				FieldType.createFieldType(databaseType, "table", LocalFloat.class.getDeclaredField(FLOAT_COLUMN));
		assertEquals(floatField, JdbcType.FLOAT.resultToJava(fieldType, resultSet, resultSet.findColumn(FLOAT_COLUMN)));
		assertFalse(JdbcType.FLOAT.isValidGeneratedType());
	}

	@Test
	public void testDouble() throws Exception {
		Dao<LocalDouble, Object> fooDao = createDao(LocalDouble.class, true);
		double doubleField = 13313323131.221;
		LocalDouble foo = new LocalDouble();
		foo.doubleField = doubleField;
		assertEquals(1, fooDao.create(foo));

		PreparedStatement stmt = dataSource.getConnection().prepareStatement("select * from " + TABLE_NAME);
		assertTrue(stmt.execute());

		ResultSet resultSet = stmt.getResultSet();
		assertTrue(resultSet.next());
		FieldType fieldType =
				FieldType.createFieldType(databaseType, "table", LocalDouble.class.getDeclaredField(DOUBLE_COLUMN));
		assertEquals(doubleField, JdbcType.DOUBLE.resultToJava(fieldType, resultSet,
				resultSet.findColumn(DOUBLE_COLUMN)));
		assertFalse(JdbcType.DOUBLE.isValidGeneratedType());
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

		PreparedStatement stmt = dataSource.getConnection().prepareStatement("select * from " + TABLE_NAME);
		assertTrue(stmt.execute());

		ResultSet resultSet = stmt.getResultSet();
		assertTrue(resultSet.next());
		assertEquals(ourEnum,
				JdbcType.ENUM_STRING.resultToJava(fieldType, resultSet, resultSet.findColumn(ENUM_COLUMN)));
		assertFalse(JdbcType.ENUM_STRING.isValidGeneratedType());
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

		PreparedStatement stmt = dataSource.getConnection().prepareStatement("select * from " + TABLE_NAME);
		assertTrue(stmt.execute());

		ResultSet resultSet = stmt.getResultSet();
		assertTrue(resultSet.next());
		assertEquals(ourEnum, JdbcType.ENUM_INTEGER.resultToJava(fieldType, resultSet,
				resultSet.findColumn(ENUM_COLUMN)));
		assertFalse(JdbcType.ENUM_INTEGER.isValidGeneratedType());
	}

	@Test
	public void testUnknownGetResult() throws Exception {
		Dao<LocalLong, Object> fooDao = createDao(LocalLong.class, true);
		LocalLong foo = new LocalLong();
		assertEquals(1, fooDao.create(foo));

		PreparedStatement stmt = dataSource.getConnection().prepareStatement("select * from " + TABLE_NAME);
		assertTrue(stmt.execute());

		ResultSet resultSet = stmt.getResultSet();
		assertTrue(resultSet.next());

		assertNull(JdbcType.UNKNOWN.resultToJava(null, resultSet, 1));
		assertFalse(JdbcType.UNKNOWN.isValidGeneratedType());
	}

	@Test
	public void testUnknownClass() {
		assertEquals(JdbcType.UNKNOWN, JdbcType.lookupClass(getClass()));
	}

	@Test
	public void testUnknownTypeVal() {
		assertEquals(JdbcType.UNKNOWN, JdbcType.lookupIdTypeVal(10120));
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
		@DatabaseField(columnName = ENUM_COLUMN, jdbcType = JdbcType.ENUM_INTEGER)
		OurEnum ourEnum;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalEnumInt2 {
		@DatabaseField(columnName = ENUM_COLUMN, jdbcType = JdbcType.ENUM_INTEGER)
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
