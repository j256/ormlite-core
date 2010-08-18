package com.j256.ormlite.examples.common;

import com.j256.ormlite.dao.Dao;

/**
 * DAO interface is used to hide the implementation details of the {@link OrderDaoImpl}. Not required, but a good
 * pattern.
 */
public interface OrderDao extends Dao<Order, Integer> {
	// no additional methods necessary
}
