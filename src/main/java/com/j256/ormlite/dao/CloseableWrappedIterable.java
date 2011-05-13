package com.j256.ormlite.dao;

import java.sql.SQLException;

/**
 * Extension to CloseableIterable which defines a class which has an iterator() method that returns a
 * {@link CloseableIterator} but also can be closed itself. This allows us to do something like this pattern:
 * 
 * <blockquote>
 * 
 * <pre>
 * CloseableWrappedIterable<Foo> wrapperIterable = fooDao.getCloseableIterable();
 * try {
 *   for (Foo foo : wrapperIterable) {
 *       ...
 *   }
 * } finally {
 *   wrapperIterable.close();
 * }
 * </pre>
 * 
 * </blockquote>
 * 
 * @author graywatson
 */
public interface CloseableWrappedIterable<T> extends CloseableIterable<T> {

	/**
	 * This will close the last iterator returned by the {@link #iterator()} method.
	 */
	public void close() throws SQLException;
}
