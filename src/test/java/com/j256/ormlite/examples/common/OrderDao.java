package com.j256.ormlite.examples.common;

import com.j256.ormlite.dao.Dao;

/**
 * DAO interface is used to hide the JDBC-ness of the {@link OrderJdbcDao}. Not required, but a good pattern.
 */
public interface OrderDao extends Dao<Order, Integer> {
	// no additional methods necessary
}
