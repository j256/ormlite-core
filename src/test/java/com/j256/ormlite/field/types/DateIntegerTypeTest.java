package com.j256.ormlite.field.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.table.DatabaseTable;

public class DateIntegerTypeTest extends BaseTypeTest {

	private static final String DATE_COLUMN = "date";

	@Test
	public void testDateInteger() throws Exception {
		Class<LocalDateInteger> clazz = LocalDateInteger.class;
		Dao<LocalDateInteger, Object> dao = createDao(clazz, true);
		long now = System.currentTimeMillis();
		Date val = new Date(now - (now % 1000));
		int sqlVal = (int) (val.getTime() / 1000L);
		String valStr = Integer.toString(sqlVal);
		LocalDateInteger foo = new LocalDateInteger();
		foo.date = val;
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, val, sqlVal, sqlVal, valStr, DataType.DATE_INTEGER, DATE_COLUMN, false, true, false,
				false, false, false, true, false);
	}

	@Test
	public void testDateIntegerSeconds() throws Exception {
		Dao<LocalDateInteger, Object> dao = createDao(LocalDateInteger.class, true);
		long now = System.currentTimeMillis();
		Date val = new Date(now - (now % 1000) + 1 /* 1 extra ms here */);
		LocalDateInteger foo = new LocalDateInteger();
		foo.date = val;
		assertEquals(1, dao.create(foo));
		List<LocalDateInteger> results = dao.queryForAll();
		assertNotNull(results);
		assertEquals(1, results.size());
		// this is always false because the above date has some millis
		assertFalse(results.get(0).date.equals(val));
		assertEquals(new Date(now - (now % 1000)), results.get(0).date);
	}

	@Test
	public void testDateIntegerNull() throws Exception {
		Class<LocalDateInteger> clazz = LocalDateInteger.class;
		Dao<LocalDateInteger, Object> dao = createDao(clazz, true);
		LocalDateInteger foo = new LocalDateInteger();
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, null, null, null, null, DataType.DATE_INTEGER, DATE_COLUMN, false, true, false, false,
				false, false, true, false);
	}

	@Test
	public void testDateIntegerParseInvalid() throws Exception {
		FieldType fieldType = FieldType.createFieldType(databaseType, TABLE_NAME,
				LocalDateInteger.class.getDeclaredField(DATE_COLUMN), LocalDateInteger.class);
		assertThrowsExactly(SQLException.class, () -> {
			DataType.DATE_INTEGER.getDataPersister().parseDefaultString(fieldType, "not valid int number");
		});
	}

	@Test
	public void testCoverage() {
		new DateIntegerType(SqlType.INTEGER, new Class[0]);
	}

	/* ============================================================================================ */

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalDateInteger {
		@DatabaseField(columnName = DATE_COLUMN, dataType = DataType.DATE_INTEGER)
		Date date;
	}
}
