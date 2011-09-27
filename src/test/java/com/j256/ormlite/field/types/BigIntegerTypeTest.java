package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

public class BigIntegerTypeTest extends BaseTypeTest {

	private final static String BIGINTEGER_COLUMN = "bigInteger";

	@Test
	public void testBigDecimal() throws Exception {
		Class<LocalBigInteger> clazz = LocalBigInteger.class;
		Dao<LocalBigInteger, Object> dao = createDao(clazz, true);
		BigInteger val =
				new BigInteger(
						"324234234234234234234234246467647647463345345435345345345345345345345345345345345346356524234234");
		String valStr = val.toString();
		LocalBigInteger foo = new LocalBigInteger();
		foo.bigInteger = val;
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, val, val, val.toString(), valStr, DataType.BIGINTEGER, BIGINTEGER_COLUMN, false,
				false, true, false, false, false, true, false);
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalBigInteger {
		@DatabaseField(columnName = BIGINTEGER_COLUMN)
		BigInteger bigInteger;
	}
}
