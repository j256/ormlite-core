package com.j256.ormlite.dao;

import java.sql.SQLException;
import java.util.Iterator;

/**
 * Extension to Iterator to provide a close() method. This should be in the JDK.
 * 
 * @author graywatson
 */
public interface CloseableIterator<T> extends Iterator<T> {

	/**
	 * Close any underlying SQL statements.
	 */
	public void close() throws SQLException;
}
