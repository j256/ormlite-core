package com.j256.ormlite.stmt.query;

import com.j256.ormlite.stmt.QueryBuilder;

/**
 * Internal class handling the SQL 'GROUP BY' operation. Used by {@link QueryBuilder#groupBy(String)} and
 * {@link QueryBuilder#groupByRaw(String)}.
 * 
 * @author graywatson
 */
public class GroupBy {

	private final String columnName;
	private final String rawSql;

	public static GroupBy byColumnName(String columnName) {
		return new GroupBy(columnName, null);
	}

	public static GroupBy byRawSql(String rawSql) {
		return new GroupBy(null, rawSql);
	}

	private GroupBy(String columnName, String rawSql) {
		this.columnName = columnName;
		this.rawSql = rawSql;
	}

	public String getColumnName() {
		return columnName;
	}

	public String getRawSql() {
		return rawSql;
	}
}
