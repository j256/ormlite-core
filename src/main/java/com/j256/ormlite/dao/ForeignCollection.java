package com.j256.ormlite.dao;

import java.sql.SQLException;
import java.util.Collection;

import com.j256.ormlite.field.ForeignCollectionField;

/**
 * Collection that is set on a field that as been marked with the {@link ForeignCollectionField} annotation when an
 * object is refreshed or queried (i.e. not created).
 * 
 * <p>
 * <blockquote>
 * 
 * <pre>
 * &#064;ForeignCollectionField(eager = false)
 * private ForeignCollection&lt;Order&gt; orders;
 * </pre>
 * 
 * </blockquote>
 * 
 * </p>
 * 
 * <p>
 * <b>NOTE:</b> If the collection has been marked as being "lazy" then just about all methods in this class result in a
 * pass through the database using the {@link #iterator()}. Even {@link #size()} and other seemingly simple calls can
 * cause a lot of database I/O. Most likely just the {@link #iterator()}, {@link #toArray()}, and
 * {@link #toArray(Object[])} methods should be used if you are using a lazy collection. Any other methods have no
 * guarantee to be at all efficient. Take a look at the source if you have any question.
 * </p>
 * 
 * <p>
 * <b>NOTE:</b> It is also important to remember that lazy iterators hold a connection open to the database which needs
 * to be closed. See {@link LazyForeignCollection#iterator()}.
 * </p>
 * 
 * @author graywatson
 */
public interface ForeignCollection<T> extends Collection<T>, CloseableIterable<T> {

	/**
	 * Like {@link Collection#iterator()} but returns a closeable iterator instead and can throw a SQLException.
	 */
	public CloseableIterator<T> iteratorThrow() throws SQLException;

	/**
	 * This makes a one time use iterable class that can be closed afterwards. The ForeignCollection itself is
	 * {@link CloseableWrappedIterable} but multiple threads can each call this to get their own closeable iterable.
	 */
	public CloseableWrappedIterable<T> getWrappedIterable();

	/**
	 * This will close the last iterator returned by the {@link #iterator()} method.
	 * 
	 * <p>
	 * <b>NOTE:</b> For lazy collections, this is not reentrant. If multiple threads are getting iterators from a lazy
	 * collection from the same object then you should use {@link #getWrappedIterable()} to get a reentrant wrapped
	 * iterable for each thread instead.
	 * </p>
	 */
	public void closeLastIterator() throws SQLException;

	/**
	 * Returns true if this an eager collection otherwise false.
	 */
	public boolean isEager();
}
