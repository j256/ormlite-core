package com.j256.ormlite.field.types;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.support.DatabaseResults;

/**
 * Type that persists a {@link BigInteger} object.
 * 
 * @author graywatson
 */
public class BigDecimalType extends BaseDataType {

	public static int DEFAULT_WIDTH = 255;

	private static final BigDecimalType singleTon = new BigDecimalType();

	public static BigDecimalType getSingleton() {
		return singleTon;
	}

	private BigDecimalType() {
		super(SqlType.STRING, new Class<?>[] { BigDecimal.class });
	}

	@Override
	public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
		try {
			return new BigDecimal(defaultStr);
		} catch (IllegalArgumentException e) {
			throw SqlExceptionUtil.create("Problems with field " + fieldType + " parsing default BigDecimal string '"
					+ defaultStr + "'", e);
		}
	}

	@Override
	public Object resultToJava(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
		String value = results.getString(columnPos);
		if (value == null) {
			return null;
		}
		try {
			return new BigDecimal(value);
		} catch (IllegalArgumentException e) {
			throw SqlExceptionUtil.create("Problems with column " + columnPos + " parsing BigDecimal string '" + value
					+ "'", e);
		}
	}

	@Override
	public Object javaToSqlArg(FieldType fieldType, Object obj) {
		BigDecimal bigInteger = (BigDecimal) obj;
		return bigInteger.toString();
	}

	@Override
	public boolean isValidForField(Field field) {
		// by default this is a noop
		return true;
	}

	@Override
	public int getDefaultWidth() {
		return DEFAULT_WIDTH;
	}

	@Override
	public boolean isAppropriateId() {
		return false;
	}
}
