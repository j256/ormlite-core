package com.j256.ormlite.stmt.query;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.stmt.Where;

/**
 * Internal class handling the SQL '>' comparison query part. Used by {@link Where#gt}.
 * 
 * @author graywatson
 */
public class Gt extends BaseComparison {

	public Gt(String columnName, FieldType fieldType, Object value) {
		super(columnName, fieldType, value);
	}

	@Override
	public StringBuilder appendOperation(StringBuilder sb) {
		sb.append("> ");
		return sb;
	}
}
