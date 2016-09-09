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

public class DateIntTypeTest extends BaseTypeTest {

	private static final String DATE_COLUMN = "date";

	@Test
	public void testDateInt() throws Exception {
		Class<LocalDateInt> clazz = LocalDateInt.class;
		Dao<LocalDateInt, Object> dao = createDao(clazz, true);
		Date val = new Date();
		int sqlVal = (int)(val.getTime() / 1000L);
		String valStr = Integer.toString(sqlVal);
		LocalDateInt foo = new LocalDateInt();
		foo.date = val;
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, val, sqlVal, sqlVal, valStr, DataType.DATE_INT, DATE_COLUMN, false, true, false,
				false, false, false, true, false);
	}

	@Test
	public void testDateIntNull() throws Exception {
		Class<LocalDateInt> clazz = LocalDateInt.class;
		Dao<LocalDateInt, Object> dao = createDao(clazz, true);
		LocalDateInt foo = new LocalDateInt();
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, null, null, null, null, DataType.DATE_INT, DATE_COLUMN, false, true, false, false,
				false, false, true, false);
	}

	@Test(expected = SQLException.class)
	public void testDateIntParseInvalid() throws Exception {
		FieldType fieldType =
				FieldType.createFieldType(connectionSource, TABLE_NAME,
						LocalDateInt.class.getDeclaredField(DATE_COLUMN), LocalDateInt.class);
		DataType.DATE_INT.getDataPersister().parseDefaultString(fieldType, "not valid int number");
	}

	@Test
	public void testCoverage() {
		new DateIntType(SqlType.INTEGER, new Class[0]);
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalDateInt {
		@DatabaseField(columnName = DATE_COLUMN, dataType = DataType.DATE_INT)
		Date date;
	}
}
