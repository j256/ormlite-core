package com.j256.ormlite.field.types;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.UUID;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.support.DatabaseResults;

/**
 * Type that persists a {@link UUID} object.
 * 
 * @author graywatson
 */
public class UuidType extends BaseDataType {

	public static int DEFAULT_WIDTH = 48;

	private static final UuidType singleTon = new UuidType();

	public static UuidType getSingleton() {
		return singleTon;
	}

	private UuidType() {
		super(SqlType.STRING, new Class<?>[] { UUID.class });
	}

	@Override
	public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
		try {
			return java.util.UUID.fromString(defaultStr);
		} catch (IllegalArgumentException e) {
			throw SqlExceptionUtil.create("Problems with field " + fieldType + " parsing default UUID-string '"
					+ defaultStr + "'", e);
		}
	}

	@Override
	public Object resultToJava(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
		String uuidStr = results.getString(columnPos);
		if (uuidStr == null) {
			return null;
		}
		try {
			return java.util.UUID.fromString(uuidStr);
		} catch (IllegalArgumentException e) {
			throw SqlExceptionUtil.create("Problems with column " + columnPos + " parsing UUID-string '" + uuidStr
					+ "'", e);
		}
	}

	@Override
	public Object javaToSqlArg(FieldType fieldType, Object obj) {
		UUID uuid = (UUID) obj;
		return uuid.toString();
	}

	@Override
	public boolean isValidForField(Field field) {
		// by default this is a noop
		return true;
	}

	@Override
	public boolean isValidGeneratedType() {
		return true;
	}

	@Override
	public boolean isSelfGeneratedId() {
		return true;
	}

	@Override
	public Object generateId() {
		return java.util.UUID.randomUUID();
	}

	@Override
	public int getDefaultWidth() {
		return DEFAULT_WIDTH;
	}
}
