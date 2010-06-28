package com.j256.ormlite.examples.common;

import com.j256.ormlite.dao.BaseJdbcDao;

/**
 * JDBC implementation of the Order DAO which is used to read/write Order to/from the database.
 */
public class OrderJdbcDao extends BaseJdbcDao<Order, Integer> implements OrderDao {

	// used by Spring which injects the DatabaseType afterwards
	public OrderJdbcDao() {
		super(Order.class);
	}

	// no additional methods necessary unless you have per-Order specific DAO methods here
}
