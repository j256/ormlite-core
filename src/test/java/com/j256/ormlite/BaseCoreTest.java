package com.j256.ormlite;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Before;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.h2.H2ConnectionSource;
import com.j256.ormlite.h2.H2DatabaseType;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;

public abstract class BaseCoreTest {

	protected final DatabaseType databaseType = new H2DatabaseType();
	protected H2ConnectionSource connectionSource;

	private Set<DatabaseTableConfig<?>> dropClassSet = new HashSet<DatabaseTableConfig<?>>();

	@Before
	public void before() throws Exception {
		connectionSource = new H2ConnectionSource();
	}

	@After
	public void after() throws Exception {
		connectionSource.close();
	}

	protected class LimitAfterSelectDatabaseType extends H2DatabaseType {
		public LimitAfterSelectDatabaseType() {
		}
		@Override
		public boolean isLimitAfterSelect() {
			return true;
		}
	}

	protected static class Foo {
		public static final String ID_COLUMN_NAME = "id";
		public static final String VAL_COLUMN_NAME = "val";
		public static final String EQUAL_COLUMN_NAME = "equal";
		public static final String NULL_COLUMN_NAME = "null";
		@DatabaseField(id = true, columnName = ID_COLUMN_NAME)
		public String id;
		@DatabaseField(columnName = VAL_COLUMN_NAME)
		public int val;
		@DatabaseField(columnName = EQUAL_COLUMN_NAME)
		public int equal;
		@DatabaseField(columnName = NULL_COLUMN_NAME)
		public String nullField;
		public Foo() {
		}
		@Override
		public String toString() {
			return "Foo:" + id;
		}
		@Override
		public boolean equals(Object other) {
			if (other == null || other.getClass() != getClass())
				return false;
			return id.equals(((Foo) other).id);
		}
	}

	protected static class Foreign {
		@DatabaseField(generatedId = true)
		public int id;
		@DatabaseField(foreign = true)
		public Foo foo;
		public Foreign() {
		}
	}

	protected <T, ID> Dao<T, ID> createDao(Class<T> clazz, boolean createTable) throws Exception {
		if (connectionSource == null) {
			throw new SQLException("Connection source is null");
		}
		@SuppressWarnings("unchecked")
		BaseDaoImpl<T, ID> dao = (BaseDaoImpl<T, ID>) DaoManager.createDao(connectionSource, clazz);
		return configDao(dao, createTable);
	}

	protected <T, ID> Dao<T, ID> createDao(DatabaseTableConfig<T> tableConfig, boolean createTable) throws Exception {
		if (connectionSource == null) {
			throw new SQLException("Connection source is null");
		}
		@SuppressWarnings("unchecked")
		BaseDaoImpl<T, ID> dao = (BaseDaoImpl<T, ID>) DaoManager.createDao(connectionSource, tableConfig);
		return configDao(dao, createTable);
	}

	protected <T> void createTable(Class<T> clazz, boolean dropAtEnd) throws Exception {
		createTable(DatabaseTableConfig.fromClass(connectionSource, clazz), dropAtEnd);
	}

	protected <T> void createTable(DatabaseTableConfig<T> tableConfig, boolean dropAtEnd) throws Exception {
		try {
			// first we drop it in case it existed before
			dropTable(tableConfig, true);
		} catch (SQLException ignored) {
			// ignore any errors about missing tables
		}
		TableUtils.createTable(connectionSource, tableConfig);
		if (dropAtEnd) {
			dropClassSet.add(tableConfig);
		}
	}

	protected <T> void dropTable(Class<T> clazz, boolean ignoreErrors) throws Exception {
		// drop the table and ignore any errors along the way
		TableUtils.dropTable(connectionSource, clazz, ignoreErrors);
	}

	protected <T> void dropTable(DatabaseTableConfig<T> tableConfig, boolean ignoreErrors) throws Exception {
		// drop the table and ignore any errors along the way
		TableUtils.dropTable(connectionSource, tableConfig, ignoreErrors);
	}

	private <T, ID> Dao<T, ID> configDao(BaseDaoImpl<T, ID> dao, boolean createTable) throws Exception {
		if (connectionSource == null) {
			throw new SQLException("Connection source is null");
		}
		dao.setConnectionSource(connectionSource);
		if (createTable) {
			DatabaseTableConfig<T> tableConfig = dao.getTableConfig();
			if (tableConfig == null) {
				tableConfig = DatabaseTableConfig.fromClass(connectionSource, dao.getDataClass());
			}
			createTable(tableConfig, true);
		}
		dao.initialize();
		return dao;
	}
}
