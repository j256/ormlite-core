package com.j256.ormlite.examples.common;

import com.j256.ormlite.dao.BaseJdbcDao;

/**
 * JDBC implementation of the Account DAO which is used to read/write Account to/from the database.
 */
public class DeliveryJdbcDao extends BaseJdbcDao<Delivery, Integer> implements DeliveryDao {

	// used by Spring which injects the DatabaseType afterwards
	public DeliveryJdbcDao() {
		super(Delivery.class);
	}

	// no additional methods necessary unless you have per-Account specific DAO methods here
}
