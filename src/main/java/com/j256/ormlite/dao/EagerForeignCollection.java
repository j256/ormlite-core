package com.j256.ormlite.dao;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.j256.ormlite.field.ForeignCollectionField;

/**
 * Collection that is set on a field that as been marked with the {@link ForeignCollectionField} annotation when an
 * object is refreshed or queried (i.e. not created).
 * 
 * @author graywatson
 */
public class EagerForeignCollection<T, ID> extends BaseForeignCollection<T, ID> implements ForeignCollection<T> {

	private final List<T> results;

	public EagerForeignCollection(Dao<T, ID> dao, String fieldName, Object fieldValue) throws SQLException {
		super(dao, fieldName, fieldValue);
		// go ahead and do the query if eager
		results = dao.query(preparedQuery);
	}

	@Override
	public CloseableIterator<T> iterator() {
		final Iterator<T> iterator = results.iterator();
		// we have to wrap the iterator since we are returning the List's iterator
		return new CloseableIterator<T>() {
			public boolean hasNext() {
				return iterator.hasNext();
			}
			public T next() {
				return iterator.next();
			}
			public void remove() {
				iterator.remove();
			}
			public void close() {
				// noop
			}
		};
	}

	public int size() {
		return results.size();
	}

	public boolean isEmpty() {
		return results.isEmpty();
	}

	public boolean contains(Object o) {
		return results.contains(o);
	}

	public boolean containsAll(Collection<?> c) {
		return results.containsAll(c);
	}

	public Object[] toArray() {
		return results.toArray();
	}

	public <E> E[] toArray(E[] array) {
		return results.toArray(array);
	}

	@Override
	public boolean add(T data) {
		results.add(data);
		return super.add(data);
	}

	@Override
	public boolean addAll(Collection<? extends T> collection) {
		results.addAll(collection);
		return super.addAll(collection);
	}

	@Override
	public boolean remove(Object data) {
		results.remove(data);
		return super.remove(data);
	}

	@Override
	public boolean removeAll(Collection<?> collection) {
		results.removeAll(collection);
		return super.removeAll(collection);
	}

	@Override
	public boolean retainAll(Collection<?> collection) {
		results.retainAll(collection);
		return super.retainAll(collection);
	}

	@Override
	public void clear() {
		results.clear();
		super.clear();
	}

	@Override
	public boolean equals(Object other) {
		return results.equals(other);
	}

	@Override
	public int hashCode() {
		return results.hashCode();
	}
}
