package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.DateTypeTest.LocalDate;
import com.j256.ormlite.table.DatabaseTable;

public class TimeStampTypeTest extends BaseTypeTest {

	private static final String TIME_STAMP_COLUMN = "timestamp";
	private DataType dataType = DataType.TIME_STAMP;

	@Test
	public void testTimeStamp() throws Exception {
		Class<LocalTimeStamp> clazz = LocalTimeStamp.class;
		Dao<LocalTimeStamp, Object> dao = createDao(clazz, true);
		GregorianCalendar c = new GregorianCalendar();
		c.set(GregorianCalendar.MILLISECOND, 0);
		long millis = c.getTimeInMillis();
		java.sql.Timestamp val = new java.sql.Timestamp(millis);
		String format = "yyyy-MM-dd HH:mm:ss.SSSSSS";
		DateFormat dateFormat = new SimpleDateFormat(format);
		String valStr = dateFormat.format(val);
		LocalTimeStamp foo = new LocalTimeStamp();
		foo.timestamp = val;
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, val, val, val, valStr, dataType, TIME_STAMP_COLUMN, false, true, true, false, true,
				false, true, false);
	}

	@Test
	public void testTimeStampNull() throws Exception {
		Class<LocalTimeStamp> clazz = LocalTimeStamp.class;
		Dao<LocalTimeStamp, Object> dao = createDao(clazz, true);
		LocalTimeStamp foo = new LocalTimeStamp();
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, null, null, null, null, dataType, TIME_STAMP_COLUMN, false, true, true, false, true,
				false, true, false);
	}

	@Test(expected = SQLException.class)
	public void testTimeStampParseInvalid() throws Exception {
		FieldType fieldType =
				FieldType.createFieldType(connectionSource, TABLE_NAME,
						LocalTimeStamp.class.getDeclaredField(TIME_STAMP_COLUMN), LocalTimeStamp.class);
		dataType.getDataPersister().parseDefaultString(fieldType, "not valid date string");
	}

	@Test
	public void testCoverage() {
		new TimeStampType(SqlType.DATE, new Class[0]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidDateField() throws Exception {
		FieldType.createFieldType(connectionSource, TABLE_NAME, InvalidDate.class.getDeclaredField("invalidType"),
				LocalDate.class);
	}

	@Test
	public void testTimeStampDefault() throws Exception {
		Dao<TimeStampDefault, Object> dao = createDao(TimeStampDefault.class, true);
		TimeStampDefault foo = new TimeStampDefault();
		Timestamp before = new Timestamp(System.currentTimeMillis());
		Thread.sleep(1);
		assertEquals(1, dao.create(foo));
		Thread.sleep(1);
		Timestamp after = new Timestamp(System.currentTimeMillis());

		TimeStampDefault result = dao.queryForId(foo.id);
		assertTrue(result.timestamp.after(before));
		assertTrue(result.timestamp.before(after));
	}

	@Test
	public void testTimeStampVersion() throws Exception {
		Dao<TimeStampVersion, Object> dao = createDao(TimeStampVersion.class, true);
		TimeStampVersion foo = new TimeStampVersion();
		Timestamp before = new Timestamp(System.currentTimeMillis());
		Thread.sleep(1);
		assertEquals(1, dao.create(foo));
		Thread.sleep(1);
		Timestamp after = new Timestamp(System.currentTimeMillis());

		TimeStampVersion result = dao.queryForId(foo.id);
		assertTrue(result.timestamp.after(before));
		assertTrue(result.timestamp.before(after));

		before = new Timestamp(System.currentTimeMillis());
		Thread.sleep(1);
		assertEquals(1, dao.update(foo));
		Thread.sleep(1);
		after = new Timestamp(System.currentTimeMillis());

		result = dao.queryForId(foo.id);
		assertTrue(result.timestamp.after(before));
		assertTrue(result.timestamp.before(after));
	}

	/* =================================================================================== */

	@DatabaseTable
	protected static class InvalidDate {
		@DatabaseField(dataType = DataType.TIME_STAMP)
		String invalidType;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalTimeStamp {
		@DatabaseField(columnName = TIME_STAMP_COLUMN)
		java.sql.Timestamp timestamp;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class TimeStampVersion {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(columnName = TIME_STAMP_COLUMN, version = true)
		java.sql.Timestamp timestamp;
		@DatabaseField
		String stuff;
	}

	@DatabaseTable
	protected static class TimeStampDefault {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(columnName = TIME_STAMP_COLUMN, persisterClass = LocalCurrentTimeStampType.class,
				defaultValue = "CURRENT_TIMESTAMP()", readOnly = true)
		java.sql.Timestamp timestamp;
		@DatabaseField
		String stuff;
	}

	protected static class LocalCurrentTimeStampType extends TimeStampType {
		private static final LocalCurrentTimeStampType singleton = new LocalCurrentTimeStampType();
		private String defaultStr;
		public LocalCurrentTimeStampType() {
			super(SqlType.DATE, new Class<?>[] { java.sql.Timestamp.class });
		}
		public static LocalCurrentTimeStampType getSingleton() {
			return singleton;
		}
		@Override
		public boolean isEscapedDefaultValue() {
			if ("CURRENT_TIMESTAMP()".equals(defaultStr)) {
				return false;
			} else {
				return super.isEscapedDefaultValue();
			}
		}
		@Override
		public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
			this.defaultStr = defaultStr;
			if ("CURRENT_TIMESTAMP()".equals(defaultStr)) {
				return defaultStr;
			} else {
				return super.parseDefaultString(fieldType, defaultStr);
			}
		}
	}
}
