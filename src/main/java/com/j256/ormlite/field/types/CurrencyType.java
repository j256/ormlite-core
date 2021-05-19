package com.j256.ormlite.field.types;

import java.sql.SQLException;
import java.util.Currency;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.support.DatabaseResults;

/**
 * Type that persists a {@link Currency} object.
 * 
 * @author Ian Kirk
 */
public class CurrencyType extends BaseDataType {

	public static int DEFAULT_WIDTH = 30;

	private static final CurrencyType singleTon = new CurrencyType();

	public static CurrencyType getSingleton() {
		return singleTon;
	}

	public CurrencyType() {
		super(SqlType.STRING, new Class<?>[] { Currency.class });
	}

	public CurrencyType(final SqlType sqlType, final Class<?>[] classes) {
		super(sqlType, classes);
	}

	@Override
	public int getDefaultWidth() {
		return DEFAULT_WIDTH;
	}

	@Override
	public Object javaToSqlArg(final FieldType fieldType, final Object javaObject) {
		final Currency currency = (Currency) javaObject;
		return currency.getCurrencyCode();
	}

	@Override
	public Object parseDefaultString(final FieldType fieldType, final String defaultStr) throws SQLException {
		try {
			return Currency.getInstance(defaultStr);
		} catch (IllegalArgumentException iae) {
			throw SqlExceptionUtil.create("Problems with field " + fieldType + " parsing default Currency '", iae);
		}
	}

	@Override
	public Object resultToSqlArg(final FieldType fieldType, final DatabaseResults results, final int columnPos)
			throws SQLException {
		return results.getString(columnPos);
	}

	@Override
	public Object sqlArgToJava(final FieldType fieldType, final Object sqlArg, final int columnPos)
			throws SQLException {
		final String currencyStr = (String) sqlArg;
		try {
			return Currency.getInstance(currencyStr);
		} catch (IllegalArgumentException iae) {
			throw SqlExceptionUtil
					.create("Problems with column " + columnPos + " parsing Currency '" + currencyStr + "'", iae);
		}
	}
}
