package com.j256.ormlite.dao;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.support.DatabaseResults;

/**
 * Collection that is set on a field that as been marked with the {@link ForeignCollectionField} annotation when an
 * object is refreshed or queried (i.e. not created).
 * 
 * @author graywatson
 */
public class EagerForeignCollection<T, ID> extends BaseForeignCollection<T, ID> implements ForeignCollection<T>,
		CloseableWrappedIterable<T>, Serializable {

	private static final long serialVersionUID = -2523335606983317721L;

	private final List<T> results;

	public EagerForeignCollection(Dao<T, ID> dao, Object parent, FieldType foreignFieldType, Object fieldValue,
			String orderColumn) throws SQLException {
		super(dao, foreignFieldType.getColumnName(), fieldValue, orderColumn, parent);
		if (fieldValue == null) {
			/*
			 * If we have no field value then just create an empty list. This is for when we need to create an empty
			 * eager collection.
			 */
			results = new ArrayList<T>();
		} else {
			// go ahead and do the query if eager
			results = dao.query(getPreparedQuery());
		}
	}

	public CloseableIterator<T> iterator() {
		return iteratorThrow();
	}

	public CloseableIterator<T> closeableIterator() {
		return iteratorThrow();
	}

	public CloseableIterator<T> iteratorThrow() {
		// we have to wrap the iterator since we are returning the List's iterator
		return new CloseableIterator<T>() {
			private Iterator<T> iterator = results.iterator();
			private T last = null;
			public boolean hasNext() {
				return iterator.hasNext();
			}
			public T next() {
				last = iterator.next();
				return last;
			}
			public void remove() {
				iterator.remove();
				if (dao != null) {
					try {
						dao.delete(last);
					} catch (SQLException e) {
						// have to demote this to be runtime
						throw new RuntimeException(e);
					}
				}
			}
			public void close() {
				// noop
			}
			public DatabaseResults getRawResults() {
				// no results object
				return null;
			}
			public void moveToNext() {
				last = iterator.next();
			}
		};
	}

	public CloseableWrappedIterable<T> getWrappedIterable() {
		// since the iterators don't have any connections, the collection can be a wrapped iterable
		return this;
	}

	public void close() {
		// noop since the iterators aren't holding open a connection
	}

	public void closeLastIterator() {
		// noop since the iterators aren't holding open a connection
	}

	public boolean isEager() {
		return true;
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
		if (results.add(data)) {
			return super.add(data);
		} else {
			return false;
		}
	}

	@Override
	public boolean addAll(Collection<? extends T> collection) {
		if (results.addAll(collection)) {
			return super.addAll(collection);
		} else {
			return false;
		}
	}

	@Override
	public boolean remove(Object data) {
		if (!results.remove(data) || dao == null) {
			return false;
		}

		@SuppressWarnings("unchecked")
		T castData = (T) data;
		try {
			return (dao.delete(castData) == 1);
		} catch (SQLException e) {
			throw new IllegalStateException("Could not delete data element from dao", e);
		}
	}

	@Override
	public boolean removeAll(Collection<?> collection) {
		boolean changed = false;
		for (Object data : collection) {
			if (remove(data)) {
				changed = true;
			}
		}
		return changed;
	}

	@Override
	public boolean retainAll(Collection<?> collection) {
		// delete from the iterate removes from the eager list and dao
		return super.retainAll(collection);
	}

	@Override
	public void clear() {
		results.clear();
		super.clear();
	}

	/**
	 * This is just a call to the equals method of the internal results list.
	 */
	@Override
	public boolean equals(Object other) {
		return results.equals(other);
	}

	/**
	 * This is just a call to the hashcode method of the internal results list.
	 */
	@Override
	public int hashCode() {
		return results.hashCode();
	}
}
