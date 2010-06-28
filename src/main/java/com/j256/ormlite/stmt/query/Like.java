package com.j256.ormlite.stmt.query;

import com.j256.ormlite.stmt.Where;

/**
 * Internal class handling the SQL 'like' comparison query part. Used by {@link Where#like}.
 * 
 * @author graywatson
 */
public class Like extends BaseComparison {

	public Like(String columnName, boolean isNumber, Object value) {
		super(columnName, isNumber, value);
	}

	@Override
	public StringBuilder appendOperation(StringBuilder sb) {
		sb.append("LIKE ");
		return sb;
	}
}
