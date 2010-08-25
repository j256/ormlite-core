package com.j256.ormlite.stmt.query;

import com.j256.ormlite.stmt.StatementBuilder;

/**
 * Internal class handling the SQL 'ORDER BY' operation. Used by {@link StatementBuilder#orderBy}.
 * 
 * @author graywatson
 */
public class OrderBy {

	private final String columnName;
	private final boolean ascending;

	public OrderBy(String columnName, boolean ascending) {
		this.columnName = columnName;
		this.ascending = ascending;
	}

	/**
	 * Return the associated column-name.
	 */
	public String getColumnName() {
		return columnName;
	}

	/**
	 * Are we ordering in ascending order. False is descending.
	 */
	public boolean isAscending() {
		return ascending;
	}
}
