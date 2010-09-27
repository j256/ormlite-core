package com.j256.ormlite.stmt.query;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.stmt.Where;

/**
 * Internal class handling the SQL '<=' comparison query part. Used by {@link Where#le}.
 * 
 * @author graywatson
 */
public class Le extends BaseComparison {

	public Le(String columnName, FieldType fieldType, Object value) {
		super(columnName, fieldType, value);
	}

	@Override
	public StringBuilder appendOperation(StringBuilder sb) {
		sb.append("<= ");
		return sb;
	}
}
