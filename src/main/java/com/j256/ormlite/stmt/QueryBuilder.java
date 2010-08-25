package com.j256.ormlite.stmt;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.table.TableInfo;

/**
 * @deprecated Use {@link StatementBuilder}
 */
@Deprecated
public class QueryBuilder<T, ID> extends StatementBuilder<T, ID> {

	public QueryBuilder(DatabaseType databaseType, TableInfo<T> tableInfo) {
		super(databaseType, tableInfo);
	}
}
