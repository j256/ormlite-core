package com.j256.ormlite.examples.common;

import com.j256.ormlite.dao.Dao;

/**
 * DAO interface is used to hide the JDBC-ness of the {@link AccountJdbcDao}. Not required, but a good pattern.
 */
public interface AccountDao extends Dao<Account, Integer> {
	// no additional methods necessary
}
