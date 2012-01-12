package com.j256.ormlite.field.types;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.support.DatabaseResults;

/**
 * Persists an Enum Java class as its ordinal integer value. You can also specify the {@link EnumStringType} as the
 * type.
 * 
 * @author graywatson
 */
public class EnumIntegerType extends BaseEnumType {

	private static final EnumIntegerType singleTon = new EnumIntegerType();

	public static EnumIntegerType getSingleton() {
		return singleTon;
	}

	private EnumIntegerType() {
		super(SqlType.INTEGER, new Class<?>[0]);
	}

	@Override
	public Object parseDefaultString(FieldType fieldType, String defaultStr) {
		return Integer.parseInt(defaultStr);
	}

	@Override
	public Object resultToJava(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
		int value = results.getInt(columnPos);
		if (fieldType == null) {
			return value;
		} else {
			return sqlArgToJava(fieldType, value, columnPos);
		}
	}

	@Override
	public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) throws SQLException {
		// do this once
		Integer valInteger = (Integer) sqlArg;
		@SuppressWarnings("unchecked")
		Map<Integer, Enum<?>> enumIntMap = (Map<Integer, Enum<?>>) fieldType.getDataTypeConfigObj();
		if (enumIntMap == null) {
			return enumVal(fieldType, valInteger, null, fieldType.getUnknownEnumVal());
		} else {
			return enumVal(fieldType, valInteger, enumIntMap.get(valInteger), fieldType.getUnknownEnumVal());
		}
	}

	@Override
	public Object javaToSqlArg(FieldType fieldType, Object obj) {
		Enum<?> enumVal = (Enum<?>) obj;
		return (Integer) enumVal.ordinal();
	}

	@Override
	public boolean isEscapedValue() {
		return false;
	}

	@Override
	public Object makeConfigObject(FieldType fieldType) throws SQLException {
		Map<Integer, Enum<?>> enumIntMap = new HashMap<Integer, Enum<?>>();
		Enum<?>[] constants = (Enum<?>[]) fieldType.getType().getEnumConstants();
		if (constants == null) {
			throw new SQLException("Field " + fieldType + " improperly configured as type " + this);
		}
		for (Enum<?> enumVal : constants) {
			enumIntMap.put(enumVal.ordinal(), enumVal);
		}
		return enumIntMap;
	}
}
