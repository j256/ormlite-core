package com.j256.ormlite.examples.common;

import com.j256.ormlite.dao.Dao;

/**
 * DAO interface is used to hide the implementation details of the {@link DeliveryDaoImpl}. Not required, but a good
 * pattern.
 */
public interface DeliveryDao extends Dao<Delivery, Integer> {
	// no additional methods necessary
}
