package com.j256.ormlite.field.types;

import java.lang.reflect.Field;
import java.sql.SQLException;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.support.DatabaseResults;

/**
 * Marker class used to see if we have a customer persister defined.
 * 
 * @author graywatson
 */
public class VoidType extends BaseDataType {

	private VoidType() {
		super(null, new Class<?>[] {});
	}

	@Override
	public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
		return null;
	}

	@Override
	public Object resultToJava(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
		return null;
	}

	@Override
	public Object javaToSqlArg(FieldType fieldType, Object obj) {
		return null;
	}

	@Override
	public boolean isValidForField(Field field) {
		return false;
	}
}
