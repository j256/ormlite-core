package com.j256.ormlite.dao;

import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.DatabaseTableConfig;

/**
 * Class which caches created DAOs. Sometimes internal DAOs are used to support such features as auto-refreshing of
 * foreign fields or collections of sub-objects. Since instantiation of the DAO is a bit expensive, this class is used
 * in an attempt to only create a DAO once for each class.
 * 
 * <p>
 * <b>NOTE:</b> To use this cache, you should make sure you've added a {@link DatabaseTable#daoClass()} value to the
 * annotation to the top of your class.
 * </p>
 * 
 * @author graywatson
 */
public class DaoManager {

	private static Map<Class<?>, DatabaseTableConfig<?>> configMap = null;
	private static Map<ClazzConnectionSource, Dao<?, ?>> classMap = null;
	private static Map<TableConfigConnectionSource, Dao<?, ?>> tableMap = null;

	private static Logger logger = LoggerFactory.getLogger(DaoManager.class);

	/**
	 * Helper method to create a Dao object without having to define a class. This checks to see if the Dao has already
	 * been created. If not then it is a call through to {@link BaseDaoImpl#createDao(ConnectionSource, Class)}.
	 */
	public synchronized static <D extends Dao<T, ?>, T> D createDao(ConnectionSource connectionSource, Class<T> clazz)
			throws SQLException {
		if (connectionSource == null) {
			throw new IllegalArgumentException("connectionSource argument cannot be null");
		}
		ClazzConnectionSource key = new ClazzConnectionSource(connectionSource, clazz);
		Dao<?, ?> dao = lookupDao(key);
		if (dao != null) {
			@SuppressWarnings("unchecked")
			D castDao = (D) dao;
			return castDao;
		}

		// if we have a config map
		if (configMap != null) {
			@SuppressWarnings("unchecked")
			DatabaseTableConfig<T> config = (DatabaseTableConfig<T>) configMap.get(clazz);
			// if we have config information cached
			if (config != null) {
				// create a dao using it
				Dao<T, ?> configedDao = createDao(connectionSource, config);
				logger.debug("created doa for class {} from loaded config", clazz);
				// we put it into the DAO map even though it came from a config
				classMap.put(key, configedDao);
				@SuppressWarnings("unchecked")
				D castDao = (D) configedDao;
				return castDao;
			}
		}

		DatabaseTable databaseTable = clazz.getAnnotation(DatabaseTable.class);
		if (databaseTable == null || databaseTable.daoClass() == Void.class
				|| databaseTable.daoClass() == BaseDaoImpl.class) {
			@SuppressWarnings("deprecation")
			Dao<T, ?> daoTmp = BaseDaoImpl.createDao(connectionSource, clazz);
			dao = daoTmp;
			logger.debug("created doa for class {} with reflection", clazz);
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
				dao = (Dao<?, ?>) daoConstructor.newInstance(arguments);
				logger.debug("created doa for class {} from constructor", clazz);
			} catch (Exception e) {
				throw SqlExceptionUtil.create("Could not call the constructor in class " + daoClass, e);
			}
		}

		classMap.put(key, dao);
		@SuppressWarnings("unchecked")
		D castDao = (D) dao;
		return castDao;
	}

	/**
	 * Helper method to lookup a Dao if it has already been associated with the class. Otherwise this returns null.
	 */
	public synchronized static <D extends Dao<T, ?>, T> D lookupDao(ConnectionSource connectionSource, Class<T> clazz)
			throws SQLException {
		if (connectionSource == null) {
			throw new IllegalArgumentException("connectionSource argument cannot be null");
		}
		ClazzConnectionSource key = new ClazzConnectionSource(connectionSource, clazz);
		Dao<?, ?> dao = lookupDao(key);
		if (dao == null) {
			return null;
		} else {
			@SuppressWarnings("unchecked")
			D castDao = (D) dao;
			return castDao;
		}
	}

	/**
	 * Helper method to create a Dao object without having to define a class. This checks to see if the Dao has already
	 * been created. If not then it is a call through to
	 * {@link BaseDaoImpl#createDao(ConnectionSource, DatabaseTableConfig)}.
	 */
	public synchronized static <D extends Dao<T, ?>, T> D createDao(ConnectionSource connectionSource,
			DatabaseTableConfig<T> tableConfig) throws SQLException {
		if (connectionSource == null) {
			throw new IllegalArgumentException("connectionSource argument cannot be null");
		}
		TableConfigConnectionSource key = new TableConfigConnectionSource(connectionSource, tableConfig);
		Dao<?, ?> dao = lookupDao(key);
		if (dao != null) {
			@SuppressWarnings("unchecked")
			D castDao = (D) dao;
			return castDao;
		}

		DatabaseTable databaseTable = tableConfig.getDataClass().getAnnotation(DatabaseTable.class);
		if (databaseTable == null || databaseTable.daoClass() == Void.class
				|| databaseTable.daoClass() == BaseDaoImpl.class) {
			@SuppressWarnings("deprecation")
			Dao<T, ?> daoTmp = BaseDaoImpl.createDao(connectionSource, tableConfig);
			dao = daoTmp;
		} else {
			Class<?> daoClass = databaseTable.daoClass();
			Constructor<?> constructor;
			try {
				constructor = daoClass.getConstructor(ConnectionSource.class, DatabaseTableConfig.class);
			} catch (Exception e) {
				throw SqlExceptionUtil.create(
						"Could not find public constructor with ConnectionSource, DatabaseTableConfig parameters in class "
								+ daoClass, e);
			}
			try {
				dao = (Dao<?, ?>) constructor.newInstance(connectionSource, tableConfig);
			} catch (Exception e) {
				throw SqlExceptionUtil.create("Could not call the constructor in class " + daoClass, e);
			}
		}

		tableMap.put(key, dao);
		@SuppressWarnings("unchecked")
		D castDao = (D) dao;
		return castDao;
	}

	/**
	 * Helper method to lookup a Dao if it has already been associated with the table-config. Otherwise this returns
	 * null.
	 */
	public synchronized static <D extends Dao<T, ?>, T> D lookupDao(ConnectionSource connectionSource,
			DatabaseTableConfig<T> tableConfig) throws SQLException {
		if (connectionSource == null) {
			throw new IllegalArgumentException("connectionSource argument cannot be null");
		}
		TableConfigConnectionSource key = new TableConfigConnectionSource(connectionSource, tableConfig);
		Dao<?, ?> dao = lookupDao(key);
		if (dao == null) {
			return null;
		} else {
			@SuppressWarnings("unchecked")
			D castDao = (D) dao;
			return castDao;
		}
	}

	/**
	 * Register the dao with the cache inside of this class. This will allow folks to build a DAO externally and then
	 * register so it can be used internally as necessary.
	 * 
	 * <p>
	 * <b>NOTE:</b> It is better to use the {@link DatabaseTable#daoClass()} and have the DaoManager construct the DAO
	 * if possible.
	 * </p>
	 */
	public static synchronized void registerDao(ConnectionSource connectionSource, Dao<?, ?> dao) {
		if (connectionSource == null) {
			throw new IllegalArgumentException("connectionSource argument cannot be null");
		}
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

	/**
	 * This adds database table configurations to the internal cache which can be used to speed up DAO construction.
	 * This is especially true of Android and other mobile platforms.
	 */
	public static void addCachedDatabaseConfigs(Collection<DatabaseTableConfig<?>> configs) throws SQLException {
		Map<Class<?>, DatabaseTableConfig<?>> newMap;
		if (configMap == null) {
			newMap = new HashMap<Class<?>, DatabaseTableConfig<?>>();
		} else {
			newMap = new HashMap<Class<?>, DatabaseTableConfig<?>>(configMap);
		}
		for (DatabaseTableConfig<?> config : configs) {
			newMap.put(config.getDataClass(), config);
			logger.info("Loaded configuration for {}", config.getDataClass());
		}
		configMap = newMap;
	}

	private static <T> Dao<?, ?> lookupDao(ClazzConnectionSource key) {
		if (classMap == null) {
			classMap = new HashMap<ClazzConnectionSource, Dao<?, ?>>();
		}
		Dao<?, ?> dao = classMap.get(key);
		if (dao == null) {
			return null;
		} else {
			return dao;
		}
	}

	private static <T> Dao<?, ?> lookupDao(TableConfigConnectionSource key) {
		if (tableMap == null) {
			tableMap = new HashMap<TableConfigConnectionSource, Dao<?, ?>>();
		}
		Dao<?, ?> dao = tableMap.get(key);
		if (dao == null) {
			return null;
		} else {
			return dao;
		}
	}

	/**
	 * Key for our class DAO map.
	 */
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

	/**
	 * Key for our table-config DAO map.
	 */
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
