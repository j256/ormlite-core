package com.j256.ormlite.core.stmt.query;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.core.db.DatabaseType;
import com.j256.ormlite.core.stmt.ArgumentHolder;
import com.j256.ormlite.core.stmt.QueryBuilder.InternalQueryBuilderWrapper;
import com.j256.ormlite.core.stmt.Where;

/**
 * Internal class handling the SQL 'EXISTS' query part. Used by {@link Where#exists}.
 * 
 * @author graywatson
 */
public class Exists implements Clause {

	private final InternalQueryBuilderWrapper subQueryBuilder;

	public Exists(InternalQueryBuilderWrapper subQueryBuilder) {
		this.subQueryBuilder = subQueryBuilder;
	}

	@Override
	public void appendSql(DatabaseType databaseType, String tableName, StringBuilder sb, List<ArgumentHolder> argList)
			throws SQLException {
		sb.append("EXISTS (");
		subQueryBuilder.appendStatementString(sb, argList);
		sb.append(") ");
	}
}
