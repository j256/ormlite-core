package com.j256.ormlite.stmt.query;

import com.j256.ormlite.stmt.ArgumentHolder;
import com.j256.ormlite.stmt.QueryBuilder;

/**
 * Internal class handling the SQL 'ORDER BY' operation. Used by {@link QueryBuilder#orderBy(String, boolean)} and
 * {@link QueryBuilder#orderByRaw(String)}.
 * 
 * @author graywatson
 */
public class OrderBy {

	private final String columnName;
	private final boolean ascending;
	private final String rawSql;
	private final ArgumentHolder[] orderByArgs;
	private final boolean nullsFirst;
	private final boolean nullsLast;

	public OrderBy(String columnName, boolean ascending) {
		this(columnName, ascending, null, null, false, false);
	}

	public OrderBy(String columnName, boolean ascending, boolean nullsFirst) {
		this(columnName, ascending, null, null, nullsFirst, !nullsFirst);
	}

	public OrderBy(String rawSql, ArgumentHolder[] orderByArgs) {
		this(null, true, rawSql, orderByArgs, false, false);
	}

	private OrderBy(String columnName, boolean ascending, String rawSql, ArgumentHolder[] orderByArgs,
			boolean nullsFirst, boolean nullsLast) {
		this.columnName = columnName;
		this.ascending = ascending;
		this.rawSql = rawSql;
		this.orderByArgs = orderByArgs;
		this.nullsFirst = nullsFirst;
		this.nullsLast = nullsLast;
	}

	public String getColumnName() {
		return columnName;
	}

	public boolean isAscending() {
		return ascending;
	}

	public String getRawSql() {
		return rawSql;
	}

	public ArgumentHolder[] getOrderByArgs() {
		return orderByArgs;
	}

	public boolean isNullsFirst() {
		return nullsFirst;
	}

	public boolean isNullsLast() {
		return nullsLast;
	}
}
