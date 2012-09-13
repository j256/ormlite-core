package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.table.DatabaseTable;

public class BigDecimalNumericTypeTest extends BaseTypeTest {

	private final static String BIGDECIMAL_COLUMN = "bigDecimal";

	@Test
	public void testBigDecimal() throws Exception {
		Class<LocalBigDecimalNumeric> clazz = LocalBigDecimalNumeric.class;
		Dao<LocalBigDecimalNumeric, Object> dao = createDao(clazz, true);
		BigDecimal val = new BigDecimal("1.345345435345345345345345345345345345345345346356524234234");
		String valStr = val.toString();
		LocalBigDecimalNumeric foo = new LocalBigDecimalNumeric();
		foo.bigDecimal = val;
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, val, val, val, valStr, DataType.BIG_DECIMAL_NUMERIC, BIGDECIMAL_COLUMN, false, false,
				false, false, false, false, true, false);
	}

	@Test(expected = SQLException.class)
	public void testBigDecimalBadDefault() throws Exception {
		createDao(BigDecimalNumericBadDefault.class, true);
	}

	@Test
	public void testBigDecimalNull() throws Exception {
		Dao<LocalBigDecimalNumeric, Object> dao = createDao(LocalBigDecimalNumeric.class, true);
		LocalBigDecimalNumeric foo = new LocalBigDecimalNumeric();
		assertEquals(1, dao.create(foo));

		List<LocalBigDecimalNumeric> results = dao.queryForAll();
		assertEquals(1, results.size());
		assertNull(results.get(0).bigDecimal);
	}

	@Test(expected = SQLException.class)
	public void testBigDecimalInvalidDbValue() throws Exception {
		Dao<LocalBigDecimalNumeric, Object> dao = createDao(LocalBigDecimalNumeric.class, true);
		Dao<NotBigDecimalNumeric, Object> notDao = createDao(NotBigDecimalNumeric.class, false);

		NotBigDecimalNumeric notFoo = new NotBigDecimalNumeric();
		notFoo.bigDecimal = "not valid form";
		assertEquals(1, notDao.create(notFoo));

		dao.queryForAll();
	}

	@Test
	public void testCoverage() {
		new BigDecimalNumericType(SqlType.BIG_DECIMAL, new Class[0]);
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalBigDecimalNumeric {
		@DatabaseField(columnName = BIGDECIMAL_COLUMN, dataType = DataType.BIG_DECIMAL_NUMERIC)
		BigDecimal bigDecimal;
	}

	protected static class BigDecimalNumericBadDefault {
		@DatabaseField(defaultValue = "not valid form", dataType = DataType.BIG_DECIMAL_NUMERIC)
		BigDecimal bigDecimal;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class NotBigDecimalNumeric {
		@DatabaseField(columnName = BIGDECIMAL_COLUMN, dataType = DataType.BIG_DECIMAL_NUMERIC)
		String bigDecimal;
	}
}
