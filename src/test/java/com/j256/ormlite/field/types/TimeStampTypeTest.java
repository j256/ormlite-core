package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.table.DatabaseTable;

public class TimeStampTypeTest extends BaseTypeTest {

	private static final String TIME_STAMP_COLUMN = "timestamp";

	@Test
	public void testTimeStamp() throws Exception {
		Class<LocalTimeStamp> clazz = LocalTimeStamp.class;
		Dao<LocalTimeStamp, Object> dao = createDao(clazz, true);
		java.sql.Timestamp val = new java.sql.Timestamp(System.currentTimeMillis());
		String format = "yyyy-MM-dd HH:mm:ss.SSSSSS";
		DateFormat dateFormat = new SimpleDateFormat(format);
		String valStr = dateFormat.format(val);
		LocalTimeStamp foo = new LocalTimeStamp();
		foo.timestamp = val;
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, val, val, val, valStr, DataType.DATE, TIME_STAMP_COLUMN, false, true, true, false,
				true, false, true, false);
	}

	@Test
	public void testTimeStampNull() throws Exception {
		Class<LocalTimeStamp> clazz = LocalTimeStamp.class;
		Dao<LocalTimeStamp, Object> dao = createDao(clazz, true);
		LocalTimeStamp foo = new LocalTimeStamp();
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, null, null, null, null, DataType.DATE, TIME_STAMP_COLUMN, false, true, true, false,
				true, false, true, false);
	}

	@Test(expected = SQLException.class)
	public void testTimeStampParseInvalid() throws Exception {
		FieldType fieldType =
				FieldType.createFieldType(connectionSource, TABLE_NAME,
						LocalTimeStamp.class.getDeclaredField(TIME_STAMP_COLUMN), LocalTimeStamp.class);
		DataType.DATE.getDataPersister().parseDefaultString(fieldType, "not valid date string");
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalTimeStamp {
		@DatabaseField(columnName = TIME_STAMP_COLUMN)
		java.sql.Timestamp timestamp;
	}
}
