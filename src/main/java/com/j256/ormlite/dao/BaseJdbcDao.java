package com.j256.ormlite.dao;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.table.DatabaseTableConfig;

/**
 * @deprecated Use {@link BaseDaoImpl}
 */
@Deprecated
public abstract class BaseJdbcDao<T, ID> extends BaseDaoImpl<T, ID> {

	public BaseJdbcDao(Class<T> dataClass) {
		super(dataClass);
	}

	public BaseJdbcDao(DatabaseTableConfig<T> tableConfig) {
		super(tableConfig);
	}

	public BaseJdbcDao(DatabaseType databaseType, Class<T> dataClass) {
		super(databaseType, dataClass);
	}

	public BaseJdbcDao(DatabaseType databaseType, DatabaseTableConfig<T> tableConfig) {
		super(databaseType, tableConfig);
	}
}
