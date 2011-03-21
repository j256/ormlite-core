package com.j256.ormlite.dao;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;

/**
 * Class which caches created DAOs. Sometimes internal DAOs are used to support such features as auto-refreshing of
 * foreign fields or collections of sub-objects. Since instantiation of the DAO is a bit expensive, this class is used
 * in an attempt to only create a DAO once for each class.
 * 
 * @author graywatson
 */
public class DaoManager {

	private static Map<Class<?>, Dao<?, ?>> classMap;

	/**
	 * Helper method to create a Dao object without having to define a class. This checks to see if the Dao has already
	 * been created. If not then it is a call through to {@link BaseDaoImpl#createDao(ConnectionSource, Class)}.
	 */
	public synchronized static <T, ID> Dao<T, ID> createDao(ConnectionSource connectionSource, Class<T> clazz)
			throws SQLException {
		if (classMap == null) {
			classMap = new HashMap<Class<?>, Dao<?, ?>>();
		}
		@SuppressWarnings("unchecked")
		Dao<T, ID> dao = (Dao<T, ID>) classMap.get(clazz);
		if (dao == null) {
			dao = BaseDaoImpl.createDao(connectionSource, clazz);
			classMap.put(clazz, dao);
		}
		return dao;
	}

	/**
	 * Helper method to create a Dao object without having to define a class. This checks to see if the Dao has already
	 * been created. If not then it is a call through to
	 * {@link BaseDaoImpl#createDao(ConnectionSource, DatabaseTableConfig)}.
	 */
	public synchronized static <T, ID> Dao<T, ID> createDao(ConnectionSource connectionSource,
			DatabaseTableConfig<T> tableConfig) throws SQLException {
		if (classMap == null) {
			classMap = new HashMap<Class<?>, Dao<?, ?>>();
		}
		@SuppressWarnings("unchecked")
		Dao<T, ID> dao = (Dao<T, ID>) classMap.get(tableConfig.getDataClass());
		if (dao == null) {
			dao = BaseDaoImpl.createDao(connectionSource, tableConfig);
			classMap.put(tableConfig.getDataClass(), dao);
		}
		return dao;
	}
}
