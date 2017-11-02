package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import java.util.Date;

import org.junit.Test;

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
		Date val = new Date();
		int sqlVal = (int)(val.getTime() / 1000L);
		String valStr = Integer.toString(sqlVal);
		LocalDateInteger foo = new LocalDateInteger();
		foo.date = val;
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, val, sqlVal, sqlVal, valStr, DataType.DATE_INT, DATE_COLUMN, false, true, false,
				false, false, false, true, false);
	}

	@Test
	public void testDateIntegerNull() throws Exception {
		Class<LocalDateInteger> clazz = LocalDateInteger.class;
		Dao<LocalDateInteger, Object> dao = createDao(clazz, true);
		LocalDateInteger foo = new LocalDateInteger();
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, null, null, null, null, DataType.DATE_INT, DATE_COLUMN, false, true, false, false,
				false, false, true, false);
	}

	@Test(expected = SQLException.class)
	public void testDateIntegerParseInvalid() throws Exception {
		FieldType fieldType =
				FieldType.createFieldType(connectionSource, TABLE_NAME,
						LocalDateInteger.class.getDeclaredField(DATE_COLUMN), LocalDateInteger.class);
		DataType.DATE_INT.getDataPersister().parseDefaultString(fieldType, "not valid int number");
	}

	@Test
	public void testCoverage() {
		new DateIntegerType(SqlType.INTEGER, new Class[0]);
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalDateInteger {
		@DatabaseField(columnName = DATE_COLUMN, dataType = DataType.DATE_INT)
		Date date;
	}
}
