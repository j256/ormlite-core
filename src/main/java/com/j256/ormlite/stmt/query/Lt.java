package com.j256.ormlite.stmt.query;

import java.sql.SQLException;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.stmt.Where;

/**
 * Internal class handling the SQL '<' comparison query part. Used by {@link Where#lt}.
 * 
 * @author graywatson
 */
public class Lt extends BaseComparison {

	public Lt(String columnName, FieldType fieldType, Object value) throws SQLException {
		super(columnName, fieldType, value);
	}

	@Override
	public StringBuilder appendOperation(StringBuilder sb) {
		sb.append("< ");
		return sb;
	}
}
