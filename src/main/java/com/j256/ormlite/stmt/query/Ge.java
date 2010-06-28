package com.j256.ormlite.stmt.query;

import com.j256.ormlite.stmt.Where;

/**
 * Internal class handling the SQL '>=' comparison query part. Used by {@link Where#ge}.
 * 
 * @author graywatson
 */
public class Ge extends BaseComparison {

	public Ge(String columnName, boolean isNumber, Object value) {
		super(columnName, isNumber, value);
	}

	@Override
	public StringBuilder appendOperation(StringBuilder sb) {
		sb.append(">= ");
		return sb;
	}
}
