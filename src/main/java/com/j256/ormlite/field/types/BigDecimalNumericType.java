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
 * Type that persists a {@link BigInteger} object as a NUMERIC SQL database field.
 * 
 * @author graywatson
 */
public class BigDecimalNumericType extends BaseDataType {

	private static final BigDecimalNumericType singleTon = new BigDecimalNumericType();

	public static BigDecimalNumericType getSingleton() {
		return singleTon;
	}

	private BigDecimalNumericType() {
		super(SqlType.BIG_DECIMAL, new Class<?>[0]);
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
		BigDecimal value = results.getBigDecimal(columnPos);
		if (value == null) {
			return null;
		} else {
			return sqlArgToJava(fieldType, value, columnPos);
		}
	}

	@Override
	public boolean isValidForField(Field field) {
		// by default this is a noop
		return true;
	}

	@Override
	public boolean isAppropriateId() {
		return false;
	}

	@Override
	public boolean isEscapedValue() {
		return false;
	}
}
