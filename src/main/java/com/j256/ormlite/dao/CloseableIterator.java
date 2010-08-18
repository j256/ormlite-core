package com.j256.ormlite.dao;

import java.sql.SQLException;
import java.util.Iterator;

/**
 * Extension to Iterator to provide a close() method. This should be in the JDK.
 * 
 * <p>
 * <b>NOTE:</b> You must call {@link CloseableIterator#close()} method when you are done otherwise the underlying SQL
 * statement and connection may be kept open.
 * </p>
 * 
 * @author graywatson
 */
public interface CloseableIterator<T> extends Iterator<T> {

	/**
	 * Close any underlying SQL statements.
	 */
	public void close() throws SQLException;
}
