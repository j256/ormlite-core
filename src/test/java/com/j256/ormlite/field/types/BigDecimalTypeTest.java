package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

public class BigDecimalTypeTest extends BaseTypeTest {

	private final static String BIGDECIMAL_COLUMN = "bigDecimal";

	@Test
	public void testBigDecimal() throws Exception {
		Class<LocalBigDecimal> clazz = LocalBigDecimal.class;
		Dao<LocalBigDecimal, Object> dao = createDao(clazz, true);
		BigDecimal val = new BigDecimal("1.345345435345345345345345345345345345345345346356524234234");
		String valStr = val.toString();
		LocalBigDecimal foo = new LocalBigDecimal();
		foo.bigDecimal = val;
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, val, val, val.toString(), valStr, DataType.BIGDECIMAL, BIGDECIMAL_COLUMN, false,
				false, true, false, false, false, true, false);
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalBigDecimal {
		@DatabaseField(columnName = BIGDECIMAL_COLUMN)
		BigDecimal bigDecimal;
	}
}
