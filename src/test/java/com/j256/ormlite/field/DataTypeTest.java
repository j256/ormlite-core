package com.j256.ormlite.field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.DatabaseResults;
import com.j256.ormlite.table.DatabaseTable;

public class DataTypeTest extends BaseCoreTest {

	private static Set<DataType> dataTypeSet = new HashSet<DataType>();

	private static final String TABLE_NAME = "foo";

	private static final String STRING_COLUMN = "string";
	private static final String BOOLEAN_COLUMN = "bool";
	private static final String DATE_COLUMN = "date";
	private static final String BYTE_COLUMN = "byteField";
	private static final String SHORT_COLUMN = "shortField";
	private static final String INT_COLUMN = "intField";
	private static final String LONG_COLUMN = "longField";
	private static final String FLOAT_COLUMN = "floatField";
	private static final String DOUBLE_COLUMN = "doubleField";
	private static final String SERIALIZABLE_COLUMN = "serializable";
	private static final String ENUM_COLUMN = "ourEnum";
	private static final String UUID_COLUMN = "uuid";
	private static final FieldType[] noFieldTypes = new FieldType[0];

	@AfterClass
	public static void afterClass() throws Exception {
		for (DataType dataType : DataType.values()) {
			if (!dataTypeSet.contains(dataType)) {
				throw new IllegalStateException("Did not properly test data type " + dataType);
			}
		}
	}

	@Test
	public void testString() throws Exception {
		Class<LocalString> clazz = LocalString.class;
		Dao<LocalString, Object> dao = createDao(clazz, true);
		String val = "str";
		String valStr = val;
		LocalString foo = new LocalString();
		foo.string = val;
		assertEquals(1, dao.create(foo));
		testType(clazz, val, val, val, valStr, DataType.STRING, STRING_COLUMN, false, true, true, false, false, false,
				true, false);
	}

	@Test
	public void testLongString() throws Exception {
		Class<LocalLongString> clazz = LocalLongString.class;
		Dao<LocalLongString, Object> dao = createDao(LocalLongString.class, true);
		String val = "str";
		String valStr = val;
		LocalLongString foo = new LocalLongString();
		foo.string = val;
		assertEquals(1, dao.create(foo));
		testType(clazz, val, val, val, valStr, DataType.LONG_STRING, STRING_COLUMN, false, false, true, false, false,
				false, true, false);
	}

	@Test
	public void testStringBytes() throws Exception {
		Class<LocalStringBytes> clazz = LocalStringBytes.class;
		Dao<LocalStringBytes, Object> dao = createDao(clazz, true);
		String val = "string with \u0185";
		LocalStringBytes foo = new LocalStringBytes();
		foo.string = val;
		assertEquals(1, dao.create(foo));
		byte[] valBytes = val.getBytes(Charset.forName(DataType.DEFAULT_STRING_BYTES_CHARSET_NAME));
		testType(clazz, val, val, valBytes, val, DataType.STRING_BYTES, STRING_COLUMN, false, false, true, false, true,
				false, true, false);
	}

	@Test
	public void testStringBytesFormat() throws Exception {
		Class<LocalStringBytesUtf8> clazz = LocalStringBytesUtf8.class;
		Dao<LocalStringBytesUtf8, Object> dao = createDao(clazz, true);
		String val = "string with \u0185";
		LocalStringBytesUtf8 foo = new LocalStringBytesUtf8();
		foo.string = val;
		assertEquals(1, dao.create(foo));
		testType(clazz, val, val, val.getBytes(Charset.forName("UTF-8")), val, DataType.STRING_BYTES, STRING_COLUMN,
				false, false, true, false, true, false, true, false);
	}

	@Test
	public void testStringBytesNull() throws Exception {
		Class<LocalStringBytes> clazz = LocalStringBytes.class;
		Dao<LocalStringBytes, Object> dao = createDao(clazz, true);
		assertEquals(1, dao.create(new LocalStringBytes()));
		testType(clazz, null, null, null, null, DataType.STRING_BYTES, STRING_COLUMN, false, false, true, false, true,
				false, true, false);
	}

	@Test
	public void testBoolean() throws Exception {
		Class<LocalBoolean> clazz = LocalBoolean.class;
		Dao<LocalBoolean, Object> dao = createDao(clazz, true);
		boolean val = true;
		String valStr = Boolean.toString(val);
		LocalBoolean foo = new LocalBoolean();
		foo.bool = val;
		assertEquals(1, dao.create(foo));
		testType(clazz, val, val, val, valStr, DataType.BOOLEAN, BOOLEAN_COLUMN, false, false, false, true, false,
				false, true, false);
	}

	@Test
	public void testBooleanObj() throws Exception {
		Class<LocalBooleanObj> clazz = LocalBooleanObj.class;
		Dao<LocalBooleanObj, Object> dao = createDao(clazz, true);
		Boolean val = true;
		String valStr = val.toString();
		LocalBooleanObj foo = new LocalBooleanObj();
		foo.bool = val;
		assertEquals(1, dao.create(foo));
		testType(clazz, val, val, val, valStr, DataType.BOOLEAN_OBJ, BOOLEAN_COLUMN, false, false, false, false, false,
				false, true, false);
	}

	@Test
	public void testBooleanObjNull() throws Exception {
		Class<LocalBooleanObj> clazz = LocalBooleanObj.class;
		Dao<LocalBooleanObj, Object> dao = createDao(clazz, true);
		assertEquals(1, dao.create(new LocalBooleanObj()));
		testType(clazz, null, null, null, null, DataType.BOOLEAN_OBJ, BOOLEAN_COLUMN, false, false, false, false,
				false, false, true, false);
	}

	@Test
	public void testBooleanPrimitiveNull() throws Exception {
		Dao<LocalBooleanObj, Object> objDao = createDao(LocalBooleanObj.class, true);
		LocalBooleanObj foo = new LocalBooleanObj();
		foo.bool = null;
		assertEquals(1, objDao.create(foo));
		Dao<LocalBoolean, Object> dao = createDao(LocalBoolean.class, false);
		List<LocalBoolean> all = dao.queryForAll();
		assertEquals(1, all.size());
		assertFalse(all.get(0).bool);
	}

	@Test
	public void testDate() throws Exception {
		Class<LocalDate> clazz = LocalDate.class;
		Dao<LocalDate, Object> dao = createDao(clazz, true);
		Date val = new Date();
		String format = "yyyy-MM-dd HH:mm:ss.SSSSSS";
		DateFormat dateFormat = new SimpleDateFormat(format);
		String valStr = dateFormat.format(val);
		LocalDate foo = new LocalDate();
		foo.date = val;
		assertEquals(1, dao.create(foo));
		testType(clazz, val, val, val, valStr, DataType.DATE, DATE_COLUMN, false, true, true, false, true, false, true,
				false);
	}

	@Test
	public void testDateNull() throws Exception {
		Class<LocalDate> clazz = LocalDate.class;
		Dao<LocalDate, Object> dao = createDao(clazz, true);
		assertEquals(1, dao.create(new LocalDate()));
		testType(clazz, null, null, null, null, DataType.DATE, DATE_COLUMN, false, true, true, false, true, false,
				true, false);
	}

	@Test(expected = SQLException.class)
	public void testDateParseInvalid() throws Exception {
		FieldType fieldType =
				FieldType.createFieldType(connectionSource, TABLE_NAME, LocalDate.class.getDeclaredField(DATE_COLUMN),
						0);
		DataType.DATE.parseDefaultString(fieldType, "not valid date string");
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testJavaDate() throws Exception {
		Class<LocalDate> clazz = LocalDate.class;
		Dao<LocalDate, Object> dao = createDao(clazz, true);
		Date val = new Date();
		String format = "yyyy-MM-dd HH:mm:ss.SSSSSS";
		DateFormat dateFormat = new SimpleDateFormat(format);
		String valStr = dateFormat.format(val);
		LocalDate foo = new LocalDate();
		foo.date = val;
		assertEquals(1, dao.create(foo));
		testType(clazz, val, val, val, valStr, DataType.JAVA_DATE, DATE_COLUMN, false, true, true, false, true, false,
				true, false);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testJavaDateNull() throws Exception {
		Class<LocalDate> clazz = LocalDate.class;
		Dao<LocalDate, Object> dao = createDao(clazz, true);
		assertEquals(1, dao.create(new LocalDate()));
		testType(clazz, null, null, null, null, DataType.JAVA_DATE, DATE_COLUMN, false, true, true, false, true, false,
				true, false);
	}

	@SuppressWarnings("deprecation")
	@Test(expected = SQLException.class)
	public void testJavaDateParseInvalid() throws Exception {
		FieldType fieldType =
				FieldType.createFieldType(connectionSource, TABLE_NAME, LocalDate.class.getDeclaredField(DATE_COLUMN),
						0);
		DataType.JAVA_DATE.parseDefaultString(fieldType, "not valid date string");
	}

	@Test
	public void testDateString() throws Exception {
		Class<LocalDateString> clazz = LocalDateString.class;
		Dao<LocalDateString, Object> dao = createDao(clazz, true);
		Date val = new Date();
		String format = "yyyy-MM-dd HH:mm:ss.SSSSSS";
		DateFormat dateFormat = new SimpleDateFormat(format);
		String valStr = dateFormat.format(val);
		String sqlVal = valStr;
		LocalDateString foo = new LocalDateString();
		foo.date = val;
		assertEquals(1, dao.create(foo));
		testType(clazz, val, valStr, sqlVal, sqlVal, DataType.DATE_STRING, DATE_COLUMN, false, true, true, false,
				false, false, true, false);
	}

	@Test
	public void testDateStringNull() throws Exception {
		Class<LocalDateString> clazz = LocalDateString.class;
		Dao<LocalDateString, Object> dao = createDao(clazz, true);
		assertEquals(1, dao.create(new LocalDateString()));
		testType(clazz, null, null, null, null, DataType.DATE_STRING, DATE_COLUMN, false, true, true, false, false,
				false, true, false);
	}

	@Test(expected = SQLException.class)
	public void testDateStringResultInvalid() throws Exception {
		Class<LocalString> clazz = LocalString.class;
		Dao<LocalString, Object> dao = createDao(clazz, true);
		LocalString foo = new LocalString();
		foo.string = "not a date format";
		assertEquals(1, dao.create(foo));
		CompiledStatement stmt =
				connectionSource.getReadOnlyConnection().compileStatement("select * from " + TABLE_NAME,
						StatementType.SELECT, noFieldTypes, noFieldTypes);
		DatabaseResults results = stmt.runQuery();
		assertTrue(results.next());
		int colNum = results.findColumn(STRING_COLUMN);
		DataType.DATE_STRING.resultToJava(null, results, colNum);
	}

	@Test(expected = SQLException.class)
	public void testDateStringParseInvalid() throws Exception {
		FieldType fieldType =
				FieldType.createFieldType(connectionSource, TABLE_NAME,
						LocalDateString.class.getDeclaredField(DATE_COLUMN), 0);
		DataType.DATE_STRING.parseDefaultString(fieldType, "not valid date string");
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testJavaDateString() throws Exception {
		Class<LocalDateString> clazz = LocalDateString.class;
		Dao<LocalDateString, Object> dao = createDao(LocalDateString.class, true);
		Date val = new Date();
		String format = "yyyy-MM-dd HH:mm:ss.SSSSSS";
		DateFormat dateFormat = new SimpleDateFormat(format);
		String valStr = dateFormat.format(val);
		String sqlVal = valStr;
		LocalDateString foo = new LocalDateString();
		foo.date = val;
		assertEquals(1, dao.create(foo));
		testType(clazz, val, sqlVal, sqlVal, valStr, DataType.JAVA_DATE_STRING, DATE_COLUMN, false, true, true, false,
				false, false, true, false);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testJavaDateStringNull() throws Exception {
		Class<LocalDateString> clazz = LocalDateString.class;
		Dao<LocalDateString, Object> dao = createDao(clazz, true);
		assertEquals(1, dao.create(new LocalDateString()));
		testType(clazz, null, null, null, null, DataType.JAVA_DATE_STRING, DATE_COLUMN, false, true, true, false,
				false, false, true, false);
	}

	@SuppressWarnings("deprecation")
	@Test(expected = SQLException.class)
	public void testJavaDateStringResultInvalid() throws Exception {
		Class<LocalString> clazz = LocalString.class;
		Dao<LocalString, Object> dao = createDao(clazz, true);
		LocalString foo = new LocalString();
		foo.string = "not a date format";
		assertEquals(1, dao.create(foo));
		CompiledStatement stmt =
				connectionSource.getReadOnlyConnection().compileStatement("select * from " + TABLE_NAME,
						StatementType.SELECT, noFieldTypes, noFieldTypes);
		DatabaseResults results = stmt.runQuery();
		assertTrue(results.next());
		int colNum = results.findColumn(STRING_COLUMN);
		DataType.JAVA_DATE_STRING.resultToJava(null, results, colNum);
	}

	@SuppressWarnings("deprecation")
	@Test(expected = SQLException.class)
	public void testJavaDateStringParseInvalid() throws Exception {
		FieldType fieldType =
				FieldType.createFieldType(connectionSource, TABLE_NAME,
						LocalDateString.class.getDeclaredField(DATE_COLUMN), 0);
		DataType.JAVA_DATE_STRING.parseDefaultString(fieldType, "not valid date string");
	}

	@Test
	public void testDateLong() throws Exception {
		Class<LocalDateLong> clazz = LocalDateLong.class;
		Dao<LocalDateLong, Object> dao = createDao(clazz, true);
		Date val = new Date();
		long sqlVal = val.getTime();
		String valStr = Long.toString(val.getTime());
		LocalDateLong foo = new LocalDateLong();
		foo.date = val;
		assertEquals(1, dao.create(foo));
		testType(clazz, val, sqlVal, sqlVal, valStr, DataType.DATE_LONG, DATE_COLUMN, false, true, false, false, false,
				false, true, false);
	}

	@Test
	public void testDateLongNull() throws Exception {
		Class<LocalDateLong> clazz = LocalDateLong.class;
		Dao<LocalDateLong, Object> dao = createDao(clazz, true);
		assertEquals(1, dao.create(new LocalDateLong()));
		testType(clazz, null, null, null, null, DataType.DATE_LONG, DATE_COLUMN, false, true, false, false, false,
				false, true, false);
	}

	@Test(expected = SQLException.class)
	public void testDateLongParseInvalid() throws Exception {
		FieldType fieldType =
				FieldType.createFieldType(connectionSource, TABLE_NAME,
						LocalDateLong.class.getDeclaredField(DATE_COLUMN), 0);
		DataType.DATE_LONG.parseDefaultString(fieldType, "not valid long number");
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testJavaDateLong() throws Exception {
		Class<LocalDateLong> clazz = LocalDateLong.class;
		Dao<LocalDateLong, Object> dao = createDao(clazz, true);
		Date val = new Date();
		long sqlVal = val.getTime();
		String valStr = Long.toString(val.getTime());
		LocalDateLong foo = new LocalDateLong();
		foo.date = val;
		assertEquals(1, dao.create(foo));
		testType(clazz, val, sqlVal, sqlVal, valStr, DataType.JAVA_DATE_LONG, DATE_COLUMN, false, true, false, false,
				false, false, true, false);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testJavaDateLongNull() throws Exception {
		Class<LocalDateLong> clazz = LocalDateLong.class;
		Dao<LocalDateLong, Object> dao = createDao(clazz, true);
		assertEquals(1, dao.create(new LocalDateLong()));
		testType(clazz, null, null, null, null, DataType.JAVA_DATE_LONG, DATE_COLUMN, false, true, false, false, false,
				false, true, false);
	}

	@SuppressWarnings("deprecation")
	@Test(expected = SQLException.class)
	public void testJavaDateLongParseInvalid() throws Exception {
		FieldType fieldType =
				FieldType.createFieldType(connectionSource, TABLE_NAME,
						LocalDateLong.class.getDeclaredField(DATE_COLUMN), 0);
		DataType.JAVA_DATE_LONG.parseDefaultString(fieldType, "not valid long number");
	}

	@Test
	public void testByte() throws Exception {
		Class<LocalByte> clazz = LocalByte.class;
		Dao<LocalByte, Object> dao = createDao(clazz, true);
		byte val = 123;
		String valStr = Byte.toString(val);
		LocalByte foo = new LocalByte();
		foo.byteField = val;
		assertEquals(1, dao.create(foo));
		testType(clazz, val, val, val, valStr, DataType.BYTE, BYTE_COLUMN, false, true, false, true, false, false,
				true, false);
	}

	@Test
	public void testByteObj() throws Exception {
		Class<LocalByteObj> clazz = LocalByteObj.class;
		Dao<LocalByteObj, Object> dao = createDao(clazz, true);
		byte val = 123;
		String valStr = Byte.toString(val);
		LocalByteObj foo = new LocalByteObj();
		foo.byteField = val;
		assertEquals(1, dao.create(foo));
		testType(clazz, val, val, val, valStr, DataType.BYTE_OBJ, BYTE_COLUMN, false, true, false, false, false, false,
				true, false);
	}

	@Test
	public void testByteObjNull() throws Exception {
		Class<LocalByteObj> clazz = LocalByteObj.class;
		Dao<LocalByteObj, Object> dao = createDao(clazz, true);
		assertEquals(1, dao.create(new LocalByteObj()));
		testType(clazz, null, null, null, null, DataType.BYTE_OBJ, BYTE_COLUMN, false, true, false, false, false,
				false, true, false);
	}

	@Test
	public void testBytePrimitiveNull() throws Exception {
		Dao<LocalByteObj, Object> objDao = createDao(LocalByteObj.class, true);
		LocalByteObj foo = new LocalByteObj();
		foo.byteField = null;
		assertEquals(1, objDao.create(foo));
		Dao<LocalByte, Object> dao = createDao(LocalByte.class, false);
		List<LocalByte> all = dao.queryForAll();
		assertEquals(1, all.size());
		assertEquals(0, all.get(0).byteField);
	}

	@Test
	public void testByteArray() throws Exception {
		Class<LocalByteArray> clazz = LocalByteArray.class;
		Dao<LocalByteArray, Object> dao = createDao(clazz, true);
		byte[] val = new byte[] { 123, 4, 124, 1, 0, 72 };
		String valStr = Arrays.toString(val);
		LocalByteArray foo = new LocalByteArray();
		foo.byteField = val;
		assertEquals(1, dao.create(foo));
		testType(clazz, val, val, val, valStr, DataType.BYTE_ARRAY, BYTE_COLUMN, false, false, true, false, true,
				false, true, false);
	}

	@Test
	public void testByteArrayNull() throws Exception {
		Class<LocalByteArray> clazz = LocalByteArray.class;
		Dao<LocalByteArray, Object> dao = createDao(clazz, true);
		assertEquals(1, dao.create(new LocalByteArray()));
		testType(clazz, null, null, null, null, DataType.BYTE_ARRAY, BYTE_COLUMN, false, false, true, false, true,
				false, true, false);
	}

	@Test(expected = SQLException.class)
	public void testByteArrayParseDefault() throws Exception {
		DataType.BYTE_ARRAY.parseDefaultString(null, null);
	}

	@Test
	public void testShort() throws Exception {
		Class<LocalShort> clazz = LocalShort.class;
		Dao<LocalShort, Object> dao = createDao(clazz, true);
		short val = 12312;
		String valStr = Short.toString(val);
		LocalShort foo = new LocalShort();
		foo.shortField = val;
		assertEquals(1, dao.create(foo));
		testType(clazz, val, val, val, valStr, DataType.SHORT, SHORT_COLUMN, false, true, false, true, false, false,
				true, false);
	}

	@Test
	public void testShortObj() throws Exception {
		Class<LocalShortObj> clazz = LocalShortObj.class;
		Dao<LocalShortObj, Object> dao = createDao(clazz, true);
		Short val = 12312;
		String valStr = val.toString();
		LocalShortObj foo = new LocalShortObj();
		foo.shortField = val;
		assertEquals(1, dao.create(foo));
		testType(clazz, val, val, val, valStr, DataType.SHORT_OBJ, SHORT_COLUMN, false, true, false, false, false,
				false, true, false);
	}

	@Test
	public void testShortObjNull() throws Exception {
		Class<LocalShortObj> clazz = LocalShortObj.class;
		Dao<LocalShortObj, Object> dao = createDao(clazz, true);
		assertEquals(1, dao.create(new LocalShortObj()));
		testType(clazz, null, null, null, null, DataType.SHORT_OBJ, SHORT_COLUMN, false, true, false, false, false,
				false, true, false);
	}

	@Test
	public void testShortPrimitiveNull() throws Exception {
		Dao<LocalShortObj, Object> objDao = createDao(LocalShortObj.class, true);
		LocalShortObj foo = new LocalShortObj();
		foo.shortField = null;
		assertEquals(1, objDao.create(foo));
		Dao<LocalShort, Object> dao = createDao(LocalShort.class, false);
		List<LocalShort> all = dao.queryForAll();
		assertEquals(1, all.size());
		assertEquals(0, all.get(0).shortField);
	}

	@Test
	public void testInt() throws Exception {
		Class<LocalInt> clazz = LocalInt.class;
		Dao<LocalInt, Object> dao = createDao(clazz, true);
		int val = 313213123;
		String valStr = Integer.toString(val);
		LocalInt foo = new LocalInt();
		foo.intField = val;
		assertEquals(1, dao.create(foo));
		testType(clazz, val, val, val, valStr, DataType.INTEGER, INT_COLUMN, true, true, false, true, false, false,
				true, true);
	}

	@Test
	public void testIntObj() throws Exception {
		Class<LocalIntObj> clazz = LocalIntObj.class;
		Dao<LocalIntObj, Object> dao = createDao(clazz, true);
		Integer val = 313213123;
		String valStr = val.toString();
		LocalIntObj foo = new LocalIntObj();
		foo.intField = val;
		assertEquals(1, dao.create(foo));
		testType(clazz, val, val, val, valStr, DataType.INTEGER_OBJ, INT_COLUMN, true, true, false, false, false,
				false, true, true);
	}

	@Test
	public void testIntObjNull() throws Exception {
		Class<LocalIntObj> clazz = LocalIntObj.class;
		Dao<LocalIntObj, Object> dao = createDao(clazz, true);
		assertEquals(1, dao.create(new LocalIntObj()));
		testType(clazz, null, null, null, null, DataType.INTEGER_OBJ, INT_COLUMN, true, true, false, false, false,
				false, true, true);
	}

	@Test
	public void testIntPrimitiveNull() throws Exception {
		Dao<LocalIntObj, Object> objDao = createDao(LocalIntObj.class, true);
		LocalIntObj foo = new LocalIntObj();
		foo.intField = null;
		assertEquals(1, objDao.create(foo));
		Dao<LocalInt, Object> dao = createDao(LocalInt.class, false);
		List<LocalInt> all = dao.queryForAll();
		assertEquals(1, all.size());
		assertEquals(0, all.get(0).intField);
	}

	@Test
	public void testIntConvertId() throws Exception {
		int intId = 213123123;
		long longId = new Long(intId);
		assertEquals(intId, DataType.INTEGER.convertIdNumber(longId));
	}

	@Test
	public void testLong() throws Exception {
		Class<LocalLong> clazz = LocalLong.class;
		Dao<LocalLong, Object> dao = createDao(clazz, true);
		long val = 13312321312312L;
		String valStr = Long.toString(val);
		LocalLong foo = new LocalLong();
		foo.longField = val;
		assertEquals(1, dao.create(foo));
		testType(clazz, val, val, val, valStr, DataType.LONG, LONG_COLUMN, true, true, false, true, false, false, true,
				true);
	}

	@Test
	public void testLongObj() throws Exception {
		Class<LocalLongObj> clazz = LocalLongObj.class;
		Dao<LocalLongObj, Object> dao = createDao(clazz, true);
		Long val = 13312321312312L;
		String valStr = val.toString();
		LocalLongObj foo = new LocalLongObj();
		foo.longField = val;
		assertEquals(1, dao.create(foo));
		testType(clazz, val, val, val, valStr, DataType.LONG_OBJ, LONG_COLUMN, true, true, false, false, false, false,
				true, true);
	}

	@Test
	public void testLongObjNull() throws Exception {
		Class<LocalLongObj> clazz = LocalLongObj.class;
		Dao<LocalLongObj, Object> dao = createDao(clazz, true);
		assertEquals(1, dao.create(new LocalLongObj()));
		testType(clazz, null, null, null, null, DataType.LONG_OBJ, LONG_COLUMN, true, true, false, false, false, false,
				true, true);
	}

	@Test
	public void testLongPrimitiveNull() throws Exception {
		Dao<LocalLongObj, Object> objDao = createDao(LocalLongObj.class, true);
		LocalLongObj foo = new LocalLongObj();
		foo.longField = null;
		assertEquals(1, objDao.create(foo));
		Dao<LocalLong, Object> dao = createDao(LocalLong.class, false);
		List<LocalLong> all = dao.queryForAll();
		assertEquals(1, all.size());
		assertEquals(0, all.get(0).longField);
	}

	@Test
	public void testLongConvertId() throws Exception {
		long longId = new Long(1312313123131L);
		assertEquals(longId, DataType.LONG.convertIdNumber(longId));
	}

	@Test
	public void testFloat() throws Exception {
		Class<LocalFloat> clazz = LocalFloat.class;
		Dao<LocalFloat, Object> dao = createDao(clazz, true);
		float val = 1331.221F;
		String valStr = Float.toString(val);
		LocalFloat foo = new LocalFloat();
		foo.floatField = val;
		assertEquals(1, dao.create(foo));
		testType(clazz, val, val, val, valStr, DataType.FLOAT, FLOAT_COLUMN, false, true, false, true, false, false,
				true, false);
	}

	@Test
	public void testFloatObj() throws Exception {
		Class<LocalFloatObj> clazz = LocalFloatObj.class;
		Dao<LocalFloatObj, Object> dao = createDao(clazz, true);
		Float val = 1331.221F;
		String valStr = val.toString();
		LocalFloatObj foo = new LocalFloatObj();
		foo.floatField = val;
		assertEquals(1, dao.create(foo));
		testType(clazz, val, val, val, valStr, DataType.FLOAT_OBJ, FLOAT_COLUMN, false, true, false, false, false,
				false, true, false);
	}

	@Test
	public void testFloatObjNull() throws Exception {
		Class<LocalFloatObj> clazz = LocalFloatObj.class;
		Dao<LocalFloatObj, Object> dao = createDao(clazz, true);
		assertEquals(1, dao.create(new LocalFloatObj()));
		testType(clazz, null, null, null, null, DataType.FLOAT_OBJ, FLOAT_COLUMN, false, true, false, false, false,
				false, true, false);
	}

	@Test
	public void testFloatPrimitiveNull() throws Exception {
		Dao<LocalFloatObj, Object> objDao = createDao(LocalFloatObj.class, true);
		LocalFloatObj foo = new LocalFloatObj();
		foo.floatField = null;
		assertEquals(1, objDao.create(foo));
		Dao<LocalFloat, Object> dao = createDao(LocalFloat.class, false);
		List<LocalFloat> all = dao.queryForAll();
		assertEquals(1, all.size());
		assertEquals(0.0F, all.get(0).floatField, 0.0F);
	}

	@Test
	public void testDouble() throws Exception {
		Class<LocalDouble> clazz = LocalDouble.class;
		Dao<LocalDouble, Object> dao = createDao(clazz, true);
		double val = 13313323131.221;
		String valStr = Double.toString(val);
		LocalDouble foo = new LocalDouble();
		foo.doubleField = val;
		assertEquals(1, dao.create(foo));
		testType(clazz, val, val, val, valStr, DataType.DOUBLE, DOUBLE_COLUMN, false, true, false, true, false, false,
				true, false);
	}

	@Test
	public void testDoubleObj() throws Exception {
		Class<LocalDoubleObj> clazz = LocalDoubleObj.class;
		Dao<LocalDoubleObj, Object> dao = createDao(clazz, true);
		Double val = 13313323131.221;
		String valStr = val.toString();
		LocalDoubleObj foo = new LocalDoubleObj();
		foo.doubleField = val;
		assertEquals(1, dao.create(foo));
		testType(clazz, val, val, val, valStr, DataType.DOUBLE_OBJ, DOUBLE_COLUMN, false, true, false, false, false,
				false, true, false);
	}

	@Test
	public void testDoubleObjNull() throws Exception {
		Class<LocalDoubleObj> clazz = LocalDoubleObj.class;
		Dao<LocalDoubleObj, Object> dao = createDao(clazz, true);
		assertEquals(1, dao.create(new LocalDoubleObj()));
		testType(clazz, null, null, null, null, DataType.DOUBLE_OBJ, DOUBLE_COLUMN, false, true, false, false, false,
				false, true, false);
	}

	@Test
	public void testDoublePrimitiveNull() throws Exception {
		Dao<LocalDoubleObj, Object> objDao = createDao(LocalDoubleObj.class, true);
		LocalDoubleObj foo = new LocalDoubleObj();
		foo.doubleField = null;
		assertEquals(1, objDao.create(foo));
		Dao<LocalDouble, Object> dao = createDao(LocalDouble.class, false);
		List<LocalDouble> all = dao.queryForAll();
		assertEquals(1, all.size());
		assertEquals(0.0F, all.get(0).doubleField, 0.0F);
	}

	@Test
	public void testSerializable() throws Exception {
		Class<LocalSerializable> clazz = LocalSerializable.class;
		Dao<LocalSerializable, Object> dao = createDao(clazz, true);
		Integer val = 1331333131;
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		ObjectOutputStream objOutStream = new ObjectOutputStream(outStream);
		objOutStream.writeObject(val);
		byte[] sqlArg = outStream.toByteArray();
		String valStr = val.toString();
		LocalSerializable foo = new LocalSerializable();
		foo.serializable = val;
		assertEquals(1, dao.create(foo));
		testType(clazz, val, val, sqlArg, valStr, DataType.SERIALIZABLE, SERIALIZABLE_COLUMN, false, false, true,
				false, true, true, false, false);
	}

	@Test
	public void testSerializableNull() throws Exception {
		Class<LocalSerializable> clazz = LocalSerializable.class;
		Dao<LocalSerializable, Object> dao = createDao(clazz, true);
		assertEquals(1, dao.create(new LocalSerializable()));
		testType(clazz, null, null, null, null, DataType.SERIALIZABLE, SERIALIZABLE_COLUMN, false, false, true, false,
				true, true, false, false);
	}

	@Test
	public void testSerializableNoValue() throws Exception {
		Class<LocalSerializable> clazz = LocalSerializable.class;
		Dao<LocalSerializable, Object> dao = createDao(clazz, true);
		LocalSerializable foo = new LocalSerializable();
		foo.serializable = null;
		assertEquals(1, dao.create(foo));
		CompiledStatement stmt =
				connectionSource.getReadOnlyConnection().compileStatement("select * from " + TABLE_NAME,
						StatementType.SELECT, noFieldTypes, noFieldTypes);
		DatabaseResults results = stmt.runQuery();
		assertTrue(results.next());
		FieldType fieldType =
				FieldType.createFieldType(connectionSource, TABLE_NAME, clazz.getDeclaredField(SERIALIZABLE_COLUMN), 0);
		assertNull(DataType.SERIALIZABLE.resultToJava(fieldType, results, results.findColumn(SERIALIZABLE_COLUMN)));
	}

	@Test(expected = SQLException.class)
	public void testSerializableParseDefault() throws Exception {
		DataType.SERIALIZABLE.parseDefaultString(null, null);
	}

	@Test(expected = SQLException.class)
	public void testSerializableInvalidResult() throws Exception {
		Class<LocalByteArray> clazz = LocalByteArray.class;
		Dao<LocalByteArray, Object> dao = createDao(clazz, true);
		LocalByteArray foo = new LocalByteArray();
		foo.byteField = new byte[] { 1, 2, 3, 4, 5 };
		assertEquals(1, dao.create(foo));
		CompiledStatement stmt =
				connectionSource.getReadOnlyConnection().compileStatement("select * from " + TABLE_NAME,
						StatementType.SELECT, noFieldTypes, noFieldTypes);
		DatabaseResults results = stmt.runQuery();
		assertTrue(results.next());
		FieldType fieldType =
				FieldType.createFieldType(connectionSource, TABLE_NAME,
						LocalSerializable.class.getDeclaredField(SERIALIZABLE_COLUMN), 0);
		DataType.SERIALIZABLE.resultToJava(fieldType, results, results.findColumn(BYTE_COLUMN));
	}

	@Test
	public void testEnumString() throws Exception {
		Class<LocalEnumString> clazz = LocalEnumString.class;
		Dao<LocalEnumString, Object> dao = createDao(clazz, true);
		OurEnum val = OurEnum.SECOND;
		String valStr = val.toString();
		String sqlVal = valStr;
		LocalEnumString foo = new LocalEnumString();
		foo.ourEnum = val;
		assertEquals(1, dao.create(foo));
		testType(clazz, val, sqlVal, sqlVal, valStr, DataType.ENUM_STRING, ENUM_COLUMN, false, true, true, false,
				false, false, true, false);
	}

	@Test
	public void testEnumStringNull() throws Exception {
		Class<LocalEnumString> clazz = LocalEnumString.class;
		Dao<LocalEnumString, Object> dao = createDao(clazz, true);
		assertEquals(1, dao.create(new LocalEnumString()));
		testType(clazz, null, null, null, null, DataType.ENUM_STRING, ENUM_COLUMN, false, true, true, false, false,
				false, true, false);
	}

	@Test
	public void testEnumStringResultsNoFieldType() throws Exception {
		Class<LocalEnumString> clazz = LocalEnumString.class;
		Dao<LocalEnumString, Object> dao = createDao(clazz, true);
		OurEnum val = OurEnum.SECOND;
		LocalEnumString foo = new LocalEnumString();
		foo.ourEnum = val;
		assertEquals(1, dao.create(foo));
		CompiledStatement stmt =
				connectionSource.getReadOnlyConnection().compileStatement("select * from " + TABLE_NAME,
						StatementType.SELECT, noFieldTypes, noFieldTypes);
		DatabaseResults results = stmt.runQuery();
		assertTrue(results.next());
		assertEquals(val.toString(), DataType.ENUM_STRING.resultToJava(null, results, results.findColumn(ENUM_COLUMN)));
	}

	@Test
	public void testEnumInt() throws Exception {
		Class<LocalEnumInt> clazz = LocalEnumInt.class;
		Dao<LocalEnumInt, Object> dao = createDao(clazz, true);
		OurEnum val = OurEnum.SECOND;
		int sqlVal = val.ordinal();
		String valStr = Integer.toString(sqlVal);
		LocalEnumInt foo = new LocalEnumInt();
		foo.ourEnum = val;
		assertEquals(1, dao.create(foo));
		testType(clazz, val, sqlVal, sqlVal, valStr, DataType.ENUM_INTEGER, ENUM_COLUMN, false, true, false, false,
				false, false, true, false);
	}

	@Test
	public void testEnumIntNull() throws Exception {
		Class<LocalEnumInt> clazz = LocalEnumInt.class;
		Dao<LocalEnumInt, Object> dao = createDao(clazz, true);
		assertEquals(1, dao.create(new LocalEnumInt()));
		testType(clazz, null, null, null, null, DataType.ENUM_INTEGER, ENUM_COLUMN, false, true, false, false, false,
				false, true, false);
	}

	@Test
	public void testEnumIntResultsNoFieldType() throws Exception {
		Class<LocalEnumInt> clazz = LocalEnumInt.class;
		Dao<LocalEnumInt, Object> dao = createDao(clazz, true);
		OurEnum val = OurEnum.SECOND;
		LocalEnumInt foo = new LocalEnumInt();
		foo.ourEnum = val;
		assertEquals(1, dao.create(foo));
		CompiledStatement stmt =
				connectionSource.getReadOnlyConnection().compileStatement("select * from " + TABLE_NAME,
						StatementType.SELECT, noFieldTypes, noFieldTypes);
		DatabaseResults results = stmt.runQuery();
		assertTrue(results.next());
		assertEquals(val.ordinal(), DataType.ENUM_INTEGER.resultToJava(null, results, results.findColumn(ENUM_COLUMN)));
	}

	@Test
	public void testUuid() throws Exception {
		Class<LocalUuid> clazz = LocalUuid.class;
		Dao<LocalUuid, Object> dao = createDao(clazz, true);
		LocalUuid foo = new LocalUuid();
		UUID val = UUID.randomUUID();
		foo.uuid = val;
		assertEquals(1, dao.create(foo));
		String valStr = val.toString();
		testType(clazz, val, val, valStr, valStr, DataType.UUID, UUID_COLUMN, true, true, true, false, false, false,
				true, false);
	}

	@Test
	public void testUnknownGetResult() throws Exception {
		DataType dataType = DataType.UNKNOWN;
		assertNull(dataType.resultToJava(null, null, 0));
		assertNull(dataType.parseDefaultString(null, null));
		assertNull(dataType.javaToSqlArg(null, null));
		assertNull(dataType.convertIdNumber(null));
		assertFalse(dataType.isValidGeneratedType());
		assertFalse(dataType.isAppropriateId());
		assertFalse(dataType.isEscapedValue());
		assertFalse(dataType.isEscapedDefaultValue());
		assertFalse(dataType.isPrimitive());
		assertFalse(dataType.isSelectArgRequired());
		assertFalse(dataType.isStreamType());
		assertFalse(dataType.isComparable());
		assertNull(dataType.convertIdNumber(21312312L));
		dataTypeSet.add(dataType);
	}

	@Test
	public void testUnknownClass() throws Exception {
		assertEquals(DataType.UNKNOWN, DataType.lookupClass(getClass()));
	}

	@Test
	public void testSerializableClass() throws Exception {
		assertEquals(DataType.UNKNOWN, DataType.lookupClass(Serializable.class));
	}

	@Test
	public void testClassLookupByteArray() throws Exception {
		assertEquals(DataType.UNKNOWN, DataType.lookupClass(byte[].class));
	}

	private void testType(Class<?> clazz, Object javaVal, Object defaultSqlVal, Object sqlArg, String defaultValStr,
			DataType dataType, String columnName, boolean isValidGeneratedType, boolean isAppropriateId,
			boolean isEscapedValue, boolean isPrimitive, boolean isSelectArgRequired, boolean isStreamType,
			boolean isComparable, boolean isConvertableId) throws Exception {
		CompiledStatement stmt =
				connectionSource.getReadOnlyConnection().compileStatement("select * from " + TABLE_NAME,
						StatementType.SELECT, noFieldTypes, noFieldTypes);
		DatabaseResults results = stmt.runQuery();
		assertTrue(results.next());
		int colNum = results.findColumn(columnName);
		FieldType fieldType =
				FieldType.createFieldType(connectionSource, TABLE_NAME, clazz.getDeclaredField(columnName), 0);
		if (javaVal instanceof byte[]) {
			assertTrue(Arrays.equals((byte[]) javaVal, (byte[]) dataType.resultToJava(fieldType, results, colNum)));
		} else {
			Map<String, Integer> colMap = new HashMap<String, Integer>();
			colMap.put(columnName, colNum);
			Object result = fieldType.resultToJava(results, colMap);
			assertEquals(javaVal, result);
		}
		if (dataType == DataType.STRING_BYTES || dataType == DataType.BYTE_ARRAY || dataType == DataType.SERIALIZABLE) {
			try {
				dataType.parseDefaultString(fieldType, "");
				fail("parseDefaultString should have thrown for " + dataType);
			} catch (SQLException e) {
				// expected
			}
		} else if (defaultValStr != null) {
			assertEquals(defaultSqlVal, dataType.parseDefaultString(fieldType, defaultValStr));
		}
		if (sqlArg == null) {
			// noop
		} else if (sqlArg instanceof byte[]) {
			assertTrue(Arrays.equals((byte[]) sqlArg, (byte[]) dataType.javaToSqlArg(fieldType, javaVal)));
		} else {
			assertEquals(sqlArg, dataType.javaToSqlArg(fieldType, javaVal));
		}
		assertEquals(isValidGeneratedType, dataType.isValidGeneratedType());
		assertEquals(isAppropriateId, dataType.isAppropriateId());
		assertEquals(isEscapedValue, dataType.isEscapedValue());
		assertEquals(isEscapedValue, dataType.isEscapedDefaultValue());
		assertEquals(isPrimitive, dataType.isPrimitive());
		assertEquals(isSelectArgRequired, dataType.isSelectArgRequired());
		assertEquals(isStreamType, dataType.isStreamType());
		assertEquals(isComparable, dataType.isComparable());
		if (isConvertableId) {
			assertNotNull(dataType.convertIdNumber(10));
		} else {
			assertNull(dataType.convertIdNumber(10));
		}
		dataTypeSet.add(dataType);
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalString {
		@DatabaseField(columnName = STRING_COLUMN)
		String string;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalLongString {
		@DatabaseField(columnName = STRING_COLUMN, dataType = DataType.LONG_STRING)
		String string;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalStringBytes {
		@DatabaseField(columnName = STRING_COLUMN, dataType = DataType.STRING_BYTES)
		String string;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalStringBytesUtf8 {
		@DatabaseField(columnName = STRING_COLUMN, dataType = DataType.STRING_BYTES, format = "UTF-8")
		String string;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalBoolean {
		@DatabaseField(columnName = BOOLEAN_COLUMN)
		boolean bool;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalBooleanObj {
		@DatabaseField(columnName = BOOLEAN_COLUMN)
		Boolean bool;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalDate {
		@DatabaseField(columnName = DATE_COLUMN)
		Date date;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalDateString {
		@DatabaseField(columnName = DATE_COLUMN, dataType = DataType.DATE_STRING)
		Date date;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalDateLong {
		@DatabaseField(columnName = DATE_COLUMN, dataType = DataType.DATE_LONG)
		Date date;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalByte {
		@DatabaseField(columnName = BYTE_COLUMN)
		byte byteField;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalByteObj {
		@DatabaseField(columnName = BYTE_COLUMN)
		Byte byteField;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalByteArray {
		@DatabaseField(columnName = BYTE_COLUMN, dataType = DataType.BYTE_ARRAY)
		byte[] byteField;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalShort {
		@DatabaseField(columnName = SHORT_COLUMN)
		short shortField;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalShortObj {
		@DatabaseField(columnName = SHORT_COLUMN)
		Short shortField;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalInt {
		@DatabaseField(columnName = INT_COLUMN)
		int intField;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalIntObj {
		@DatabaseField(columnName = INT_COLUMN)
		Integer intField;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalLong {
		@DatabaseField(columnName = LONG_COLUMN)
		long longField;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalLongObj {
		@DatabaseField(columnName = LONG_COLUMN)
		Long longField;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalFloat {
		@DatabaseField(columnName = FLOAT_COLUMN)
		float floatField;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalFloatObj {
		@DatabaseField(columnName = FLOAT_COLUMN)
		Float floatField;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalDouble {
		@DatabaseField(columnName = DOUBLE_COLUMN)
		double doubleField;;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalDoubleObj {
		@DatabaseField(columnName = DOUBLE_COLUMN)
		Double doubleField;;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalSerializable {
		@DatabaseField(columnName = SERIALIZABLE_COLUMN, dataType = DataType.SERIALIZABLE)
		Integer serializable;;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalEnumString {
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

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalUuid {
		@DatabaseField(columnName = UUID_COLUMN)
		UUID uuid;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalUnknown {
		@DatabaseField
		LocalUnknown unkown;
	}

	private enum OurEnum {
		FIRST,
		SECOND, ;
	}

	private enum OurEnum2 {
		FIRST, ;
	}
}
