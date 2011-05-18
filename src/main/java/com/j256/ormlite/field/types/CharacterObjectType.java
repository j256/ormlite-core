package com.j256.ormlite.field.types;

import java.sql.SQLException;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.support.DatabaseResults;

/**
 * Type that persists a Character object.
 * 
 * @author graywatson
 */
public class CharacterObjectType extends BaseDataType {

	private static final CharacterObjectType singleTon = new CharacterObjectType();

	public static CharacterObjectType createType() {
		return singleTon;
	}

	private CharacterObjectType() {
		super(SqlType.CHAR, new Class<?>[] { Character.class });
	}

	protected CharacterObjectType(SqlType sqlType, Class<?>[] classes) {
		super(sqlType, classes);
	}

	@Override
	public Object resultToJava(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
		return (Character) results.getChar(columnPos);
	}

	@Override
	public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
		if (defaultStr.length() != 1) {
			throw new SQLException("Problems with field " + fieldType + ", default string to long: '" + defaultStr
					+ "'");
		}
		return (Character) defaultStr.charAt(0);
	}
}
