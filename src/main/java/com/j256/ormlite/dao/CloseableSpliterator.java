package com.j256.ormlite.dao;

import java.util.Spliterator;

/**
 * Extension to Spliterator to provide a close() method.
 *
 * <p>
 * <b>NOTE:</b> You must call {@link CloseableSpliterator#close()} method when you are done otherwise the underlying SQL
 * statement and connection may be kept open.
 * </p>
 * 
 * @author zhemaituk
 */
public interface CloseableSpliterator<T> extends Spliterator<T>, AutoCloseable {

	/**
	 * Close any underlying SQL statements but swallow any SQLExceptions.
	 */
	public void closeQuietly();
}
