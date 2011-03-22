package com.j256.ormlite.dao;

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
 * {@link #toArray(Object[])} methods should be used if you are using a lazy collection.
 * </p>
 * 
 * @author graywatson
 */
public interface ForeignCollection<T> extends Collection<T> {

	/**
	 * Like {@link Collection#iterator()} but returns a closable iterator instead.
	 */
	public CloseableIterator<T> iterator();
}
