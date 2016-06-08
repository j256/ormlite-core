package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;
import com.j256.ormlite.table.DatabaseTable;

public class DateStringTypeTest extends BaseTypeTest {

	private static final String DATE_COLUMN = "date";
	private static final String STRING_COLUMN = "string";

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
		testType(dao, foo, clazz, val, valStr, sqlVal, sqlVal, DataType.DATE_STRING, DATE_COLUMN, false, true, true,
				false, false, false, true, false);
	}

	@Test
	public void testDateStringNull() throws Exception {
		Class<LocalDateString> clazz = LocalDateString.class;
		Dao<LocalDateString, Object> dao = createDao(clazz, true);
		LocalDateString foo = new LocalDateString();
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, null, null, null, null, DataType.DATE_STRING, DATE_COLUMN, false, true, true, false,
				false, false, true, false);
	}

	@Test(expected = SQLException.class)
	public void testDateStringParseInvalid() throws Exception {
		FieldType fieldType =
				FieldType.createFieldType(connectionSource, TABLE_NAME,
						LocalDateString.class.getDeclaredField(DATE_COLUMN), LocalDateString.class);
		DataType.DATE_STRING.getDataPersister().parseDefaultString(fieldType, "not valid date string");
	}

	@Test(expected = SQLException.class)
	public void testDateStringResultInvalid() throws Exception {
		Class<LocalString> clazz = LocalString.class;
		Dao<LocalString, Object> dao = createDao(clazz, true);
		LocalString foo = new LocalString();
		foo.string = "not a date format";
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadOnlyConnection(FOO_TABLE_NAME);
		CompiledStatement stmt = null;
		try {
			stmt =
					conn.compileStatement("select * from " + TABLE_NAME, StatementType.SELECT, noFieldTypes,
							DatabaseConnection.DEFAULT_RESULT_FLAGS, true);
			DatabaseResults results = stmt.runQuery(null);
			assertTrue(results.next());
			int colNum = results.findColumn(STRING_COLUMN);
			DataType.DATE_STRING.getDataPersister().resultToJava(null, results, colNum);
		} finally {
			if (stmt != null) {
				stmt.close();
			}
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testDateStringFormat() throws Exception {
		Dao<DateStringFormat, Object> dao = createDao(DateStringFormat.class, true);
		DateStringFormat dateStringFormat = new DateStringFormat();
		dateStringFormat.date = new SimpleDateFormat("yyyy-MM-dd").parse("2012-09-01");
		assertEquals(1, dao.create(dateStringFormat));

		List<DateStringFormat> results = dao.queryForAll();
		assertEquals(1, results.size());
		assertEquals(dateStringFormat.date, results.get(0).date);
	}

	@Test
	public void testDateStringFormatNotDayAlign() throws Exception {
		Dao<DateStringFormat, Object> dao = createDao(DateStringFormat.class, true);
		DateStringFormat dateStringFormat = new DateStringFormat();
		dateStringFormat.date = new SimpleDateFormat("yyyy-MM-dd HH").parse("2012-09-01 12");
		assertEquals(1, dao.create(dateStringFormat));

		List<DateStringFormat> results = dao.queryForAll();
		assertEquals(1, results.size());
		assertFalse(dateStringFormat.date.equals(results.get(0).date));
	}

	@Test
	public void testCoverage() {
		new DateStringType(SqlType.STRING, new Class[0]);
	}

	@Test
	public void testDateStringBackwardsCompatibility() throws Exception {
		Dao<VersionString, Object> stringDao = createDao(VersionString.class, true);
		Dao<VersionDate, Object> dateDao = createDao(VersionDate.class, true);

		VersionString string = new VersionString();
		/*
		 * WARNING, do not change this test otherwise you break backwards compatibility with string equality checking.
		 * 
		 * Changing this would _also_ break the {@code version=true} feature with dates which _also_ uses equality.
		 */
		final String formatStr = "yyyy-MM-dd HH:mm:ss.SSSSSS";
		final SimpleDateFormat format = new SimpleDateFormat(formatStr);
		Date date = new Date();
		string.date = format.format(date);
		assertEquals(1, stringDao.create(string));

		VersionDate result = dateDao.queryForId(string.id);
		assertNotNull(result);

		/*
		 * Check equality testing of dates.
		 */
		List<VersionDate> results = dateDao.queryForEq(DATE_COLUMN, result.date);
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(date, results.get(0).date);

		assertEquals(date, result.date);

		/*
		 * Check updating which was affected if the field is a version field.
		 */
		Thread.sleep(1);
		result.stuff = 12312312;
		assertEquals(1, dateDao.update(result));

		VersionDate newVersionDate = dateDao.queryForId(result.id);
		assertNotNull(newVersionDate);
		assertEquals(result.stuff, newVersionDate.stuff);
		assertTrue(newVersionDate.date.after(date));
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalDateString {
		@DatabaseField(columnName = DATE_COLUMN, dataType = DataType.DATE_STRING)
		Date date;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class VersionString {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(columnName = DATE_COLUMN)
		String date;
		@DatabaseField
		int stuff;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class VersionDate {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(columnName = DATE_COLUMN, dataType = DataType.DATE_STRING, version = true)
		Date date;
		@DatabaseField
		int stuff;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class DateStringFormat {
		@DatabaseField(columnName = DATE_COLUMN, dataType = DataType.DATE_STRING, format = "yyyy-MM-dd")
		Date date;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalString {
		@DatabaseField(columnName = STRING_COLUMN)
		String string;
	}
}
