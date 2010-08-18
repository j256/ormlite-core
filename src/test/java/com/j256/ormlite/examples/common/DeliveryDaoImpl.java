package com.j256.ormlite.examples.common;

import com.j256.ormlite.dao.BaseDaoImpl;

/**
 * Implementation of the Account DAO which is used to read/write Account to/from the database.
 */
public class DeliveryDaoImpl extends BaseDaoImpl<Delivery, Integer> implements DeliveryDao {

	// used by Spring which injects the DatabaseType afterwards
	public DeliveryDaoImpl() {
		super(Delivery.class);
	}

	// no additional methods necessary unless you have per-Account specific DAO methods here
}
