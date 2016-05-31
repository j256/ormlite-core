package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.List;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.table.DatabaseTable;

public class BigIntegerTypeTest extends BaseTypeTest {

	private final static String BIGINTEGER_COLUMN = "bigInteger";
	private final static String DEFAULT_VALUE = "4724724378237982347983478932478923478934789342473892342789";

	@Test
	public void testBigInteger() throws Exception {
		Class<LocalBigInteger> clazz = LocalBigInteger.class;
		Dao<LocalBigInteger, Object> dao = createDao(clazz, true);
		BigInteger val = new BigInteger(
				"324234234234234234234234246467647647463345345435345345345345345345345345345345345346356524234234");
		String valStr = val.toString();
		LocalBigInteger foo = new LocalBigInteger();
		foo.bigInteger = val;
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, val, valStr, valStr, valStr, DataType.BIG_INTEGER, BIGINTEGER_COLUMN, true, true,
				true, false, false, false, true, true);
	}

	@Test(expected = SQLException.class)
	public void testBigIntegerBadDefault() throws Exception {
		createDao(BigIntegerBadDefault.class, true);
	}

	@Test
	public void testBigIntegerNull() throws Exception {
		Dao<LocalBigInteger, Object> dao = createDao(LocalBigInteger.class, true);
		LocalBigInteger foo = new LocalBigInteger();
		assertEquals(1, dao.create(foo));

		List<LocalBigInteger> results = dao.queryForAll();
		assertEquals(1, results.size());
		assertNull(results.get(0).bigInteger);
	}

	@Test(expected = SQLException.class)
	public void testBigIntegerInvalidDbValue() throws Exception {
		Dao<LocalBigInteger, Object> dao = createDao(LocalBigInteger.class, true);
		Dao<NotBigInteger, Object> notDao = createDao(NotBigInteger.class, false);

		NotBigInteger notFoo = new NotBigInteger();
		notFoo.bigInteger = "not valid form";
		assertEquals(1, notDao.create(notFoo));

		dao.queryForAll();
	}

	@Test
	public void testDefaultValue() throws Exception {
		Dao<BigIntegerDefaultValue, Object> dao = createDao(BigIntegerDefaultValue.class, true);
		BigIntegerDefaultValue foo = new BigIntegerDefaultValue();
		dao.create(foo);

		assertNull(foo.bigInteger);
		dao.refresh(foo);
		assertEquals(new BigInteger(DEFAULT_VALUE), foo.bigInteger);
	}

	@Test
	public void testBigIntegerId() throws Exception {
		Dao<BigIntegerId, BigInteger> dao = createDao(BigIntegerId.class, true);
		BigIntegerId foo = new BigIntegerId();
		dao.create(foo);
		assertEquals(BigInteger.ONE, foo.id);
		BigIntegerId result = dao.queryForId(BigInteger.ONE);
		assertNotNull(result);
		assertEquals(foo.id, result.id);
	}

	@Test
	public void testCoverage() {
		new BigIntegerType(SqlType.BIG_DECIMAL, new Class[0]);
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalBigInteger {
		@DatabaseField(columnName = BIGINTEGER_COLUMN)
		BigInteger bigInteger;
	}

	protected static class BigIntegerBadDefault {
		@DatabaseField(defaultValue = "not valid form")
		BigInteger bigInteger;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class NotBigInteger {
		@DatabaseField(columnName = BIGINTEGER_COLUMN)
		String bigInteger;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class BigIntegerDefaultValue {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(columnName = BIGINTEGER_COLUMN, defaultValue = DEFAULT_VALUE)
		BigInteger bigInteger;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class BigIntegerId {
		@DatabaseField(generatedId = true)
		BigInteger id;
	}
}
