package com.j256.ormlite.stmt.query;

import java.util.List;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.Where;

/**
 * Internal class handling the SQL 'between' query part. Used by {@link Where#between}.
 * 
 * @author graywatson
 */
public class Between extends BaseComparison {

	private Object low;
	private Object high;

	public Between(String columnName, boolean isNumber, Object low, Object high) {
		super(columnName, isNumber, null);
		this.low = low;
		this.high = high;
	}

	@Override
	public StringBuilder appendOperation(StringBuilder sb) {
		sb.append("BETWEEN ");
		return sb;
	}

	@Override
	public StringBuilder appendValue(DatabaseType databaseType, StringBuilder sb, List<SelectArg> selectArgList) {
		if (low == null) {
			throw new IllegalArgumentException("BETWEEN low value for '" + columnName + "' is null");
		}
		if (high == null) {
			throw new IllegalArgumentException("BETWEEN high value for '" + columnName + "' is null");
		}
		appendArgOrValue(databaseType, sb, selectArgList, low);
		sb.append("AND ");
		appendArgOrValue(databaseType, sb, selectArgList, high);
		return sb;
	}
}
