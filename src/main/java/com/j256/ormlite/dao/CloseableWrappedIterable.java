package com.j256.ormlite.dao;

/**
 * Extension to CloseableIterable which defines a class which has an iterator() method that returns a
 * {@link CloseableIterator} but also can be closed itself. This allows us to do something like this pattern:
 * 
 * <pre>
 * CloseableWrappedIterable&lt;Foo&gt; wrapperIterable = fooDao.getCloseableIterable();
 * try {
 *   for (Foo foo : wrapperIterable) {
 *       ...
 *   }
 * } finally {
 *   wrapperIterable.close();
 * }
 * </pre>
 * 
 * @author graywatson
 */
public interface CloseableWrappedIterable<T> extends CloseableIterable<T>, AutoCloseable {

	/**
	 * This will close the last iterator returned by the {@link #iterator()} method.
	 */
	@Override
	public void close() throws Exception;
}
