package com.j256.ormlite.field.converter;

import java.sql.SQLException;

import com.j256.ormlite.field.BaseFieldConverter;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.support.DatabaseResults;

/**
 * Conversion to/from the Boolean Java field as a number because some databases like the true/false.
 */
public class BooleanNumberFieldConverter extends BaseFieldConverter {

	private static final BooleanNumberFieldConverter singleTon = new BooleanNumberFieldConverter();

	public static BooleanNumberFieldConverter getSingleton() {
		return singleTon;
	}

	@Override
	public SqlType getSqlType() {
		return SqlType.BYTE;
	}

	@Override
	public Object parseDefaultString(FieldType fieldType, String defaultStr) {
		boolean bool = Boolean.parseBoolean(defaultStr);
		return (bool ? Byte.valueOf((byte) 1) : Byte.valueOf((byte) 0));
	}

	@Override
	public Object javaToSqlArg(FieldType fieldType, Object obj) {
		Boolean bool = (Boolean) obj;
		return (bool ? Byte.valueOf((byte) 1) : Byte.valueOf((byte) 0));
	}

	@Override
	public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
		return results.getByte(columnPos);
	}

	@Override
	public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) {
		byte arg = (Byte) sqlArg;
		return (arg == 1 ? (Boolean) true : (Boolean) false);
	}

	@Override
	public Object resultStringToJava(FieldType fieldType, String stringValue, int columnPos) {
		return sqlArgToJava(fieldType, Byte.parseByte(stringValue), columnPos);
	}
}
