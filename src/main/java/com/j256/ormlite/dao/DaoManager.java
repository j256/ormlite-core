package com.j256.ormlite.dao;

import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.DatabaseTableConfig;

/**
 * Class which caches created DAOs. Sometimes internal DAOs are used to support such features as auto-refreshing of
 * foreign fields or collections of sub-objects. Since instantiation of the DAO is a bit expensive, this class is used
 * in an attempt to only create a DAO once for each class.
 * 
 * <p>
 * <b>NOTE:</b> To use this cache, you should make sure you've added a {@link DatabaseTable#daoClassName()} value to the
 * annotation to the top of your class.
 * </p>
 * 
 * @author graywatson
 */
public class DaoManager {

	private static Map<ClazzConnectionSource, Dao<?, ?>> classMap;
	private static Map<TableConfigConnectionSource, Dao<?, ?>> tableMap;

	/**
	 * Helper method to create a Dao object without having to define a class. This checks to see if the Dao has already
	 * been created. If not then it is a call through to {@link BaseDaoImpl#createDao(ConnectionSource, Class)}.
	 */
	public synchronized static <T, ID> Dao<T, ID> createDao(ConnectionSource connectionSource, Class<T> clazz)
			throws SQLException {
		if (classMap == null) {
			classMap = new HashMap<ClazzConnectionSource, Dao<?, ?>>();
		}
		ClazzConnectionSource key = new ClazzConnectionSource(connectionSource, clazz);
		@SuppressWarnings("unchecked")
		Dao<T, ID> dao = (Dao<T, ID>) classMap.get(key);
		if (dao != null) {
			return dao;
		}

		DatabaseTable databaseTable = clazz.getAnnotation(DatabaseTable.class);
		if (databaseTable == null || databaseTable.daoClass() == Void.class
				|| databaseTable.daoClass() == BaseDaoImpl.class) {
			dao = BaseDaoImpl.createDao(connectionSource, clazz);
		} else {
			Class<?> daoClass = databaseTable.daoClass();
			Constructor<?> daoConstructor = null;
			Object[] arguments = null;
			Constructor<?>[] constructors = daoClass.getConstructors();
			// look first for the constructor with a class parameter in case it is a generic dao
			for (Constructor<?> constructor : constructors) {
				Class<?>[] params = constructor.getParameterTypes();
				if (params.length == 2 && params[0] == ConnectionSource.class && params[1] == Class.class) {
					daoConstructor = constructor;
					arguments = new Object[] { connectionSource, clazz };
					break;
				}
			}
			// then look first for the constructor with just the ConnectionSource
			if (daoConstructor == null) {
				for (Constructor<?> constructor : constructors) {
					Class<?>[] params = constructor.getParameterTypes();
					if (params.length == 1 && params[0] == ConnectionSource.class) {
						daoConstructor = constructor;
						arguments = new Object[] { connectionSource };
						break;
					}
				}
			}
			if (daoConstructor == null) {
				throw new SQLException("Could not find public constructor with ConnectionSource parameter in class "
						+ daoClass);
			}
			try {
				@SuppressWarnings("unchecked")
				Dao<T, ID> castInstance = (Dao<T, ID>) daoConstructor.newInstance(arguments);
				dao = castInstance;
			} catch (Exception e) {
				throw new SQLException("Could not call the constructor in class " + daoClass, e);
			}
		}

		classMap.put(key, dao);
		return dao;
	}

	/**
	 * Same as {@link #createDao(ConnectionSource, Class)} but with an extra argument to help with casting.
	 */
	public static <T, ID> Dao<T, ID> createDao(ConnectionSource connectionSource, Class<T> clazz, Class<ID> idClass)
			throws SQLException {
		return createDao(connectionSource, clazz);
	}

	/**
	 * Helper method to create a Dao object without having to define a class. This checks to see if the Dao has already
	 * been created. If not then it is a call through to
	 * {@link BaseDaoImpl#createDao(ConnectionSource, DatabaseTableConfig)}.
	 */
	public synchronized static <T, ID> Dao<T, ID> createDao(ConnectionSource connectionSource,
			DatabaseTableConfig<T> tableConfig) throws SQLException {
		if (tableMap == null) {
			tableMap = new HashMap<TableConfigConnectionSource, Dao<?, ?>>();
		}
		TableConfigConnectionSource key = new TableConfigConnectionSource(connectionSource, tableConfig);
		@SuppressWarnings("unchecked")
		Dao<T, ID> dao = (Dao<T, ID>) tableMap.get(key);
		if (dao != null) {
			return dao;
		}

		DatabaseTable databaseTable = tableConfig.getDataClass().getAnnotation(DatabaseTable.class);
		if (databaseTable == null || databaseTable.daoClass() == Void.class
				|| databaseTable.daoClass() == BaseDaoImpl.class) {
			dao = BaseDaoImpl.createDao(connectionSource, tableConfig);
		} else {
			Class<?> daoClass = databaseTable.daoClass();
			Constructor<?> constructor;
			try {
				constructor = daoClass.getConstructor(ConnectionSource.class, DatabaseTableConfig.class);
			} catch (Exception e) {
				throw new SQLException(
						"Could not find public constructor with ConnectionSource, DatabaseTableConfig parameters in class "
								+ daoClass, e);
			}
			try {
				@SuppressWarnings("unchecked")
				Dao<T, ID> castInstance = (Dao<T, ID>) constructor.newInstance(connectionSource, tableConfig);
				dao = castInstance;
			} catch (Exception e) {
				throw new SQLException("Could not call the constructor in class " + daoClass, e);
			}
		}

		tableMap.put(key, dao);
		return dao;
	}

	/**
	 * Same as {@link #createDao(ConnectionSource, DatabaseTableConfig)} but with an extra argument to help with
	 * casting.
	 */
	public static <T, ID> Dao<T, ID> createDao(ConnectionSource connectionSource, DatabaseTableConfig<T> tableConfig,
			Class<ID> idClass) throws SQLException {
		return createDao(connectionSource, tableConfig);
	}

	/**
	 * Register the dao with the cache inside of this class. This will allow folks to build a DAO externally and then
	 * register so it can be used internally as necessary.
	 * 
	 * <p>
	 * <b>NOTE:</b> It is better to use the {@link DatabaseTable#daoClassName()} and have the DaoManager construct the
	 * DAO if possible.
	 * </p>
	 */
	public static synchronized void registerDao(ConnectionSource connectionSource, Dao<?, ?> dao) {
		if (dao instanceof BaseDaoImpl) {
			DatabaseTableConfig<?> tableConfig = ((BaseDaoImpl<?, ?>) dao).getTableConfig();
			if (tableConfig != null) {
				tableMap.put(new TableConfigConnectionSource(connectionSource, tableConfig), dao);
				return;
			}
		}
		classMap.put(new ClazzConnectionSource(connectionSource, dao.getDataClass()), dao);
	}

	/**
	 * Clear out the cache.
	 */
	public static synchronized void clearCache() {
		if (classMap != null) {
			classMap.clear();
			classMap = null;
		}
		if (tableMap != null) {
			tableMap.clear();
			tableMap = null;
		}
	}

	private static class ClazzConnectionSource {
		ConnectionSource connectionSource;
		Class<?> clazz;
		public ClazzConnectionSource(ConnectionSource connectionSource, Class<?> clazz) {
			this.connectionSource = connectionSource;
			this.clazz = clazz;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = prime + clazz.hashCode();
			result = prime * result + connectionSource.hashCode();
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			ClazzConnectionSource other = (ClazzConnectionSource) obj;
			if (!clazz.equals(other.clazz)) {
				return false;
			} else if (!connectionSource.equals(other.connectionSource)) {
				return false;
			} else {
				return true;
			}
		}
	}

	private static class TableConfigConnectionSource {
		ConnectionSource connectionSource;
		DatabaseTableConfig<?> tableConfig;
		public TableConfigConnectionSource(ConnectionSource connectionSource, DatabaseTableConfig<?> tableConfig) {
			this.connectionSource = connectionSource;
			this.tableConfig = tableConfig;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = prime + tableConfig.hashCode();
			result = prime * result + connectionSource.hashCode();
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			TableConfigConnectionSource other = (TableConfigConnectionSource) obj;
			if (!tableConfig.equals(other.tableConfig)) {
				return false;
			} else if (!connectionSource.equals(other.connectionSource)) {
				return false;
			} else {
				return true;
			}
		}
	}
}
