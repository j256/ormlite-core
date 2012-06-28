package com.j256.ormlite.table;

import java.sql.SQLException;

/**
 * Interface to support the {@link DatabaseTable#constructorFactory()} method.
 * 
 * @author graywatson
 */
public interface ObjectFactory<T> {

	/**
	 * Construct and return an object of a certain class.
	 * 
	 * @throws SQLException
	 *             if there was a problem creating the object.
	 */
	public T createObject() throws SQLException;
}
