package com.j256.ormlite.stmt.query;

import java.sql.SQLException;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.stmt.Where;

/**
 * Internal class handling the SQL 'like' comparison query part. Used by {@link Where#like}.
 * 
 * @author graywatson
 */
public class Like extends BaseComparison {

	public Like(String columnName, FieldType fieldType, Object value) throws SQLException {
		super(columnName, fieldType, value);
	}

	@Override
	public StringBuilder appendOperation(StringBuilder sb) {
		sb.append("LIKE ");
		return sb;
	}
}
