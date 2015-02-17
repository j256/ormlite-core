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
import com.j256.ormlite.table.DatabaseTable;

public class BigDecimalTypeTest extends BaseTypeTest {

	private final static String BIGDECIMAL_COLUMN = "bigDecimal";
	private final static String DEFAULT_VALUE = "1.3452904234234732472343454353453453453453453453453453453";

	@Test
	public void testBigDecimal() throws Exception {
		Class<LocalBigDecimal> clazz = LocalBigDecimal.class;
		Dao<LocalBigDecimal, Object> dao = createDao(clazz, true);
		BigDecimal val = new BigDecimal("1.345345435345345345345345345345345345345345346356524234234");
		String valStr = val.toString();
		LocalBigDecimal foo = new LocalBigDecimal();
		foo.bigDecimal = val;
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, val, valStr, valStr, valStr, DataType.BIG_DECIMAL, BIGDECIMAL_COLUMN, false, false,
				true, false, false, false, true, false);
	}

	@Test(expected = SQLException.class)
	public void testBigDecimalBadDefault() throws Exception {
		createDao(BigDecimalBadDefault.class, true);
	}

	@Test
	public void testBigDecimalNull() throws Exception {
		Dao<LocalBigDecimal, Object> dao = createDao(LocalBigDecimal.class, true);
		LocalBigDecimal foo = new LocalBigDecimal();
		assertEquals(1, dao.create(foo));

		List<LocalBigDecimal> results = dao.queryForAll();
		assertEquals(1, results.size());
		assertNull(results.get(0).bigDecimal);
	}

	@Test(expected = SQLException.class)
	public void testBigDecimalInvalidDbValue() throws Exception {
		Dao<LocalBigDecimal, Object> dao = createDao(LocalBigDecimal.class, true);
		Dao<NotBigDecimal, Object> notDao = createDao(NotBigDecimal.class, false);

		NotBigDecimal notFoo = new NotBigDecimal();
		notFoo.bigDecimal = "not valid form";
		assertEquals(1, notDao.create(notFoo));

		dao.queryForAll();
	}

	@Test
	public void testDefaultValue() throws Exception {
		Dao<BigDecimalDefaultValue, Object> dao = createDao(BigDecimalDefaultValue.class, true);
		BigDecimalDefaultValue foo = new BigDecimalDefaultValue();
		dao.create(foo);

		assertNull(foo.bigDecimal);
		dao.refresh(foo);
		assertEquals(new BigDecimal(DEFAULT_VALUE), foo.bigDecimal);
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalBigDecimal {
		@DatabaseField(columnName = BIGDECIMAL_COLUMN)
		BigDecimal bigDecimal;
	}

	protected static class BigDecimalBadDefault {
		@DatabaseField(defaultValue = "not valid form")
		BigDecimal bigDecimal;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class NotBigDecimal {
		@DatabaseField(columnName = BIGDECIMAL_COLUMN)
		String bigDecimal;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class BigDecimalDefaultValue {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(columnName = BIGDECIMAL_COLUMN, defaultValue = DEFAULT_VALUE)
		BigDecimal bigDecimal;
	}
}
