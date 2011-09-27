package com.j256.ormlite.field.types;

import java.lang.reflect.Field;
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
public class BigIntegerType extends BaseDataType {

	public static int DEFAULT_WIDTH = 255;

	private static final BigIntegerType singleTon = new BigIntegerType();

	public static BigIntegerType getSingleton() {
		return singleTon;
	}

	private BigIntegerType() {
		super(SqlType.STRING, new Class<?>[] { BigInteger.class });
	}

	@Override
	public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
		try {
			return new BigInteger(defaultStr);
		} catch (IllegalArgumentException e) {
			throw SqlExceptionUtil.create("Problems with field " + fieldType + " parsing default BigInteger string '"
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
			return new BigInteger(value);
		} catch (IllegalArgumentException e) {
			throw SqlExceptionUtil.create("Problems with column " + columnPos + " parsing BigInteger string '" + value
					+ "'", e);
		}
	}

	@Override
	public Object javaToSqlArg(FieldType fieldType, Object obj) {
		BigInteger bigInteger = (BigInteger) obj;
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
