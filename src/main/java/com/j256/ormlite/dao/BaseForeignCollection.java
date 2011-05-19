package com.j256.ormlite.dao;

import java.sql.SQLException;
import java.util.Collection;

import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.SelectArg;

/**
 * Base collection that is set on a field that as been marked with the {@link ForeignCollectionField} annotation when an
 * object is refreshed or queried (i.e. not created).
 * 
 * <p>
 * <b>WARNING:</b> Most likely for(;;) loops should not be used here since we need to be careful about closing the
 * iterator.
 * </p>
 * 
 * @author graywatson
 */
public abstract class BaseForeignCollection<T, ID> implements ForeignCollection<T> {

	protected final Dao<T, ID> dao;
	private final String fieldName;
	private final Object fieldValue;
	private PreparedQuery<T> preparedQuery;

	public BaseForeignCollection(Dao<T, ID> dao, String fieldName, Object fieldValue) {
		this.dao = dao;
		this.fieldName = fieldName;
		this.fieldValue = fieldValue;
	}

	public boolean add(T data) {
		try {
			dao.create(data);
			return true;
		} catch (SQLException e) {
			throw new IllegalStateException("Could not create data element in dao", e);
		}
	}

	public boolean addAll(Collection<? extends T> collection) {
		for (T data : collection) {
			try {
				dao.create(data);
			} catch (SQLException e) {
				throw new IllegalStateException("Could not create data elements in dao", e);
			}
		}
		return true;
	}

	public boolean remove(Object data) {
		@SuppressWarnings("unchecked")
		T castData = (T) data;
		try {
			return (dao.delete(castData) == 1);
		} catch (SQLException e) {
			throw new IllegalStateException("Could not delete data element from dao", e);
		}
	}

	public boolean removeAll(Collection<?> collection) {
		boolean changed = false;
		for (Object data : collection) {
			@SuppressWarnings("unchecked")
			T castData = (T) data;
			try {
				if (dao.delete(castData) > 0) {
					changed = true;
				}
			} catch (SQLException e) {
				throw new IllegalStateException("Could not create data elements in dao", e);
			}
		}
		return changed;
	}

	/**
	 * Uses the iterator to run through the dao and retain only the items that are in the passed in collection.
	 */
	public boolean retainAll(Collection<?> collection) {
		boolean changed = false;
		CloseableIterator<T> iterator = dao.iterator();
		try {
			while (iterator.hasNext()) {
				T data = iterator.next();
				if (!collection.contains(data)) {
					iterator.remove();
					changed = true;
				}
			}
		} finally {
			try {
				iterator.close();
			} catch (SQLException e) {
				// ignored
			}
		}
		return changed;
	}

	/**
	 * Uses the iterator to run through the dao and delete all of the items. This is different from removing all of the
	 * elements in the table since this iterator is across just one item's foreign objects.
	 */
	public void clear() {
		CloseableIterator<T> iterator = dao.iterator();
		try {
			while (iterator.hasNext()) {
				iterator.next();
				iterator.remove();
			}
		} finally {
			try {
				iterator.close();
			} catch (SQLException e) {
				// ignored
			}
		}
	}

	protected PreparedQuery<T> getPreparedQuery() throws SQLException {
		if (preparedQuery == null) {
			SelectArg fieldArg = new SelectArg();
			fieldArg.setValue(fieldValue);
			preparedQuery = dao.queryBuilder().where().eq(fieldName, fieldArg).prepare();
		}
		return preparedQuery;
	}
}
