package com.j256.ormlite.field.converter;

import java.sql.SQLException;

import com.j256.ormlite.field.BaseFieldConverter;
import com.j256.ormlite.field.FieldConverter;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.support.DatabaseResults;

/**
 * Some drivers do not support setObject on Character objects!
 */
public class CharacterCompatFieldConverter extends BaseFieldConverter {

	private final FieldConverter wrappedConverter;

	public CharacterCompatFieldConverter(FieldConverter wrappedConverter) {
		this.wrappedConverter = wrappedConverter;
	}

	@Override
	public SqlType getSqlType() {
		return SqlType.CHAR;
	}

	@Override
	public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
		Object character = wrappedConverter.parseDefaultString(fieldType, defaultStr);
		if (character == null)
			return null;
		return character.toString(); // Convert to string
	}

	@Override
	public Object javaToSqlArg(FieldType fieldType, Object obj) throws SQLException {
		Object character = wrappedConverter.javaToSqlArg(fieldType, obj);
		if (character == null)
			return null;
		return character.toString(); // Convert to string
	}

	@Override
	public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
		return wrappedConverter.resultToSqlArg(fieldType, results, columnPos);
	}

	@Override
	public Object resultToJava(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
		return wrappedConverter.resultToJava(fieldType, results, columnPos);
	}

	@Override
	public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) throws SQLException {
		return wrappedConverter.sqlArgToJava(fieldType, sqlArg, columnPos);
	}

	@Override
	public Object resultStringToJava(FieldType fieldType, String stringValue, int columnPos) throws SQLException {
		return wrappedConverter.resultStringToJava(fieldType, stringValue, columnPos);
	}

	@Override
	public Object makeConfigObject(FieldType fieldType) throws SQLException {
		return wrappedConverter.makeConfigObject(fieldType);
	}
}
