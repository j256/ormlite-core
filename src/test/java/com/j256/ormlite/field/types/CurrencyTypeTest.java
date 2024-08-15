package com.j256.ormlite.field.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import java.sql.SQLException;
import java.util.Currency;
import java.util.Locale;

import org.junit.jupiter.api.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.table.DatabaseTable;

public class CurrencyTypeTest extends BaseTypeTest {

	private static final String CURRENCY_COLUMN = "currency";
	private static final String DEFAULT_VALUE = "USD";

	@Test
	public void testCurrency() throws Exception {
		Class<LocalCurrency> clazz = LocalCurrency.class;
		Dao<LocalCurrency, Object> dao = createDao(clazz, true);
		LocalCurrency foo = new LocalCurrency();
		foo.currency = Currency.getInstance(Locale.US);
		assertEquals(1, dao.create(foo));
		String valStr = foo.currency.getCurrencyCode();
		testType(dao, foo, clazz, foo.currency, valStr, valStr, valStr, DataType.CURRENCY, CURRENCY_COLUMN, false,
				false, true, false, false, false, true, false);
	}

	@Test
	public void testCurrencyDefault() throws Exception {
		Dao<CurrencyDefault, Object> dao = createDao(CurrencyDefault.class, true);
		CurrencyDefault foo = new CurrencyDefault();
		dao.create(foo);

		assertNull(foo.currency);
		dao.refresh(foo);
		assertNotNull(foo.currency);
		assertEquals(Currency.getInstance(DEFAULT_VALUE), foo.currency);
	}

	@Test
	public void testCurrencyInvalidDefault() {
		assertThrowsExactly(SQLException.class, () -> {
			createDao(CurrencyInvalidDefault.class, true);
		});
	}

	@Test
	public void testCoverage() {
		new CurrencyType(SqlType.STRING, new Class[0]);
	}

	/* ============================================================================================ */

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalCurrency {
		@DatabaseField(columnName = CURRENCY_COLUMN)
		Currency currency;
	}

	protected static class CurrencyDefault {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(defaultValue = DEFAULT_VALUE)
		Currency currency;
	}

	protected static class CurrencyInvalidDefault {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(defaultValue = "xxx")
		Currency currency;
	}
}
