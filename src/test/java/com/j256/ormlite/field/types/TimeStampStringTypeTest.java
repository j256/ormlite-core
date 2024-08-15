package com.j256.ormlite.field.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.junit.jupiter.api.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.table.DatabaseTable;

public class TimeStampStringTypeTest extends BaseTypeTest {

	private static final String TIMESTAMP_COLUMN = "timestamp";

	@Test
	public void testTimeStampString() throws Exception {
		Class<LocalTimeStampString> clazz = LocalTimeStampString.class;
		Dao<LocalTimeStampString, Object> dao = createDao(clazz, true);
		Timestamp val = new Timestamp(System.currentTimeMillis());
		String format = "yyyy-MM-dd HH:mm:ss.SSSSSS";
		DateFormat dateFormat = new SimpleDateFormat(format);
		String valStr = dateFormat.format(val);
		String sqlVal = valStr;
		LocalTimeStampString foo = new LocalTimeStampString();
		foo.timestamp = val;
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, val, valStr, sqlVal, sqlVal, DataType.TIME_STAMP_STRING, TIMESTAMP_COLUMN, false,
				true, true, false, false, false, true, false);
	}

	@Test
	public void testTimeStampStringNull() throws Exception {
		Class<LocalTimeStampString> clazz = LocalTimeStampString.class;
		Dao<LocalTimeStampString, Object> dao = createDao(clazz, true);
		LocalTimeStampString foo = new LocalTimeStampString();
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, null, null, null, null, DataType.TIME_STAMP_STRING, TIMESTAMP_COLUMN, false, true,
				true, false, false, false, true, false);
	}

	@Test
	public void testTimeStampStringParseInvalid() throws Exception {
		FieldType fieldType = FieldType.createFieldType(databaseType, TABLE_NAME,
				LocalTimeStampString.class.getDeclaredField(TIMESTAMP_COLUMN), LocalTimeStampString.class);
		assertThrowsExactly(SQLException.class, () -> {
			TimeStampStringType.getSingleton().parseDefaultString(fieldType, "not valid date string");
		});
	}

	@Test
	public void testDateVersion() throws Exception {
		Class<LocalDateVersion> clazz = LocalDateVersion.class;
		Dao<LocalDateVersion, Object> dao = createDao(clazz, true);
		LocalDateVersion foo = new LocalDateVersion();
		long before = System.currentTimeMillis();
		assertEquals(1, dao.create(foo));
		long after = System.currentTimeMillis();
		assertNotNull(foo.timestamp);
		assertTrue(foo.timestamp.getTime() >= before);
		assertTrue(foo.timestamp.getTime() <= after);
	}

	/* ============================================================================================ */

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalTimeStampString {
		@DatabaseField(columnName = TIMESTAMP_COLUMN, dataType = DataType.TIME_STAMP_STRING)
		Timestamp timestamp;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalDateVersion {
		@DatabaseField(columnName = TIMESTAMP_COLUMN, version = true)
		Timestamp timestamp;
	}
}
