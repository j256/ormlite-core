package com.j256.ormlite.stmt.query;

import java.sql.SQLException;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.stmt.Where;

/**
 * Internal class handling the SQL '<>' comparison query part. Used by {@link Where#ne}.
 * 
 * @author graywatson
 */
public class Ne extends BaseComparison {

	public Ne(String columnName, FieldType fieldType, Object value) throws SQLException {
		super(columnName, fieldType, value);
	}

	@Override
	public StringBuilder appendOperation(StringBuilder sb) {
		sb.append("<> ");
		return sb;
	}
}
