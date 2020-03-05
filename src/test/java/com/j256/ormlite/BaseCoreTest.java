package com.j256.ormlite;

import java.sql.SQLException;

import com.j256.ormlite.table.SchemaUtils;
import org.junit.After;
import org.junit.Before;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.h2.H2ConnectionSource;
import com.j256.ormlite.h2.H2DatabaseType;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;

public abstract class BaseCoreTest {

	public static final String FOO_TABLE_NAME = "foo";
	public static final String NOID_TABLE_NAME = "noid";

	protected DatabaseType databaseType;
	protected WrappedConnectionSource connectionSource;

	/**
	 * @throws Exception
	 *             For sub-classes.
	 */
	@Before
	public void before() throws Exception {
		connectionSource = new WrappedConnectionSource(new H2ConnectionSource());
		databaseType = connectionSource.getDatabaseType();
		DaoManager.clearCache();
	}

	@After
	public void after() throws Exception {
		connectionSource.close();
		connectionSource = null;
	}

	protected <T, ID> Dao<T, ID> createDao(Class<T> clazz, boolean createTable) throws SQLException {
		if (connectionSource == null) {
			throw new SQLException("Connection source is null");
		}
		@SuppressWarnings("unchecked")
		BaseDaoImpl<T, ID> dao = (BaseDaoImpl<T, ID>) DaoManager.createDao(connectionSource, clazz);
		return configDao(dao, createTable);
	}

	protected <T, ID> Dao<T, ID> createDao(DatabaseTableConfig<T> tableConfig, boolean createTable)
			throws SQLException {
		if (connectionSource == null) {
			throw new SQLException("Connection source is null");
		}
		@SuppressWarnings("unchecked")
		BaseDaoImpl<T, ID> dao = (BaseDaoImpl<T, ID>) DaoManager.createDao(connectionSource, tableConfig);
		return configDao(dao, createTable);
	}

	protected <T> void createTable(Class<T> clazz, boolean dropAtEnd) throws SQLException {
		createTable(DatabaseTableConfig.fromClass(databaseType, clazz), dropAtEnd);
	}

	protected <T> void createTable(DatabaseTableConfig<T> tableConfig, boolean dropAtEnd) throws SQLException {
		try {
			// first we drop it in case it existed before
			dropTable(tableConfig, true);
		} catch (SQLException ignored) {
			// ignore any errors about missing tables
		}
		TableUtils.createTable(connectionSource, tableConfig);
	}

	protected <T> void createSchema(DatabaseTableConfig<T> tableConfig) throws SQLException {
		SchemaUtils.createSchema(connectionSource, tableConfig.getSchemaName());
	}

	protected <T> void dropTable(Class<T> clazz, boolean ignoreErrors) throws SQLException {
		// drop the table and ignore any errors along the way
		TableUtils.dropTable(connectionSource, clazz, ignoreErrors);
	}

	protected <T> void dropTable(DatabaseTableConfig<T> tableConfig, boolean ignoreErrors) throws SQLException {
		// drop the table and ignore any errors along the way
		TableUtils.dropTable(connectionSource, tableConfig, ignoreErrors);
	}

	private <T, ID> Dao<T, ID> configDao(BaseDaoImpl<T, ID> dao, boolean createTable) throws SQLException {
		if (connectionSource == null) {
			throw new SQLException("Connection source is null");
		}
		if (createTable) {
			DatabaseTableConfig<T> tableConfig = dao.getTableConfig();
			if (tableConfig == null) {
				tableConfig = DatabaseTableConfig.fromClass(databaseType, dao.getDataClass());
			}
			if (tableConfig.getSchemaName() != null && tableConfig.getSchemaName().length() > 0){
				createSchema(tableConfig);
			}
			createTable(tableConfig, true);
		}
		return dao;
	}

	protected static class LimitAfterSelectDatabaseType extends H2DatabaseType {
		public LimitAfterSelectDatabaseType() throws SQLException {
			super();
		}

		@Override
		public boolean isLimitAfterSelect() {
			return true;
		}
	}

	@DatabaseTable(tableName = FOO_TABLE_NAME)
	protected static class Foo {
		public static final String ID_COLUMN_NAME = "id";
		public static final String VAL_COLUMN_NAME = "val";
		public static final String EQUAL_COLUMN_NAME = "equal";
		public static final String STRING_COLUMN_NAME = "string";
		@DatabaseField(generatedId = true, columnName = ID_COLUMN_NAME)
		public int id;
		@DatabaseField(columnName = VAL_COLUMN_NAME)
		public int val;
		@DatabaseField(columnName = EQUAL_COLUMN_NAME)
		public int equal;
		@DatabaseField(columnName = STRING_COLUMN_NAME)
		public String stringField;

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
			return id == ((Foo) other).id;
		}

		@Override
		public int hashCode() {
			return id;
		}
	}

	@DatabaseTable(schemaName = "FOO_SCHEMA", tableName = FOO_TABLE_NAME)
	protected static class SchemaFoo {
		public static final String ID_COLUMN_NAME = "id";
		public static final String VAL_COLUMN_NAME = "val";
		public static final String EQUAL_COLUMN_NAME = "equal";
		public static final String STRING_COLUMN_NAME = "string";
		@DatabaseField(generatedId = true, columnName = ID_COLUMN_NAME)
		public int id;
		@DatabaseField(columnName = VAL_COLUMN_NAME)
		public int val;
		@DatabaseField(columnName = EQUAL_COLUMN_NAME)
		public int equal;
		@DatabaseField(columnName = STRING_COLUMN_NAME)
		public String stringField;

		public SchemaFoo() {
		}

		@Override
		public String toString() {
			return "Foo:" + id;
		}

		@Override
		public boolean equals(Object other) {
			if (other == null || other.getClass() != getClass())
				return false;
			return id == ((Foo) other).id;
		}

		@Override
		public int hashCode() {
			return id;
		}
	}

	protected static class Foreign {
		public static final String FOO_COLUMN_NAME = "foo_id";
		@DatabaseField(generatedId = true)
		public int id;
		@DatabaseField(foreign = true, columnName = FOO_COLUMN_NAME)
		public Foo foo;

		public Foreign() {
		}
	}

	protected static class ForeignSchemaFoo {
		public static final String FOO_COLUMN_NAME = "foo_id";
		@DatabaseField(generatedId = true)
		public int id;
		@DatabaseField(foreign = true, columnName = FOO_COLUMN_NAME)
		public SchemaFoo foo;

		public ForeignSchemaFoo() {
		}
	}

	@DatabaseTable(tableName = NOID_TABLE_NAME)
	protected static class NoId {
		@DatabaseField
		public String stuff;

		public NoId() {
		}
	}
}
