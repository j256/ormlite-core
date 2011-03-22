package com.j256.ormlite.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.j256.ormlite.field.ForeignCollectionField;

/**
 * Collection that is set on a field that as been marked with the {@link ForeignCollectionField} annotation when an
 * object is refreshed or queried (i.e. not created). Most of the methods here require a pass through the database.
 * Operations such as size() therefore should not be used. Only the iterator or toArray methods probably should be used.
 * 
 * <p>
 * <b>WARNING:</b> Most likely for(;;) loops should not be used here since we need to be careful about closing the
 * iterator.
 * </p>
 * 
 * @author graywatson
 */
public class LazyForeignCollection<T, ID> extends BaseForeignCollection<T, ID> implements ForeignCollection<T> {

	public LazyForeignCollection(Dao<T, ID> dao, String fieldName, Object fieldValue) throws SQLException {
		super(dao, fieldName, fieldValue);
	}

	public int size() {
		int sizeC = 0;
		CloseableIterator<T> iterator = iterator();
		try {
			while (iterator.hasNext()) {
				sizeC++;
			}
		} finally {
			try {
				iterator.close();
			} catch (SQLException e) {
				// ignored
			}
		}
		return sizeC;
	}

	public boolean isEmpty() {
		CloseableIterator<T> iterator = iterator();
		try {
			return !iterator.hasNext();
		} finally {
			try {
				iterator.close();
			} catch (SQLException e) {
				// ignored
			}
		}
	}

	public boolean contains(Object obj) {
		CloseableIterator<T> iterator = iterator();
		try {
			while (iterator.hasNext()) {
				if (iterator.next().equals(obj)) {
					return true;
				}
			}
			return false;
		} finally {
			try {
				iterator.close();
			} catch (SQLException e) {
				// ignored
			}
		}
	}

	public boolean containsAll(Collection<?> collection) {
		for (Object obj : collection) {
			boolean found = false;
			CloseableIterator<T> iterator = iterator();
			try {
				while (iterator.hasNext()) {
					if (iterator.next().equals(obj)) {
						found = true;
						break;
					}
				}
			} finally {
				try {
					iterator.close();
				} catch (SQLException e) {
					// ignored
				}
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}

	public Object[] toArray() {
		List<T> items = new ArrayList<T>();
		CloseableIterator<T> iterator = iterator();
		try {
			while (iterator.hasNext()) {
				items.add(iterator.next());
			}
		} finally {
			try {
				iterator.close();
			} catch (SQLException e) {
				// ignored
			}
		}
		return items.toArray();
	}

	public <E> E[] toArray(E[] array) {
		List<E> items = null;
		int itemC = 0;
		CloseableIterator<T> iterator = iterator();
		try {
			while (iterator.hasNext()) {
				@SuppressWarnings("unchecked")
				E castData = (E) iterator.next();
				// are we exceeding our capacity in the array?
				if (itemC >= array.length) {
					if (items == null) {
						items = new ArrayList<E>();
						for (E arrayData : array) {
							items.add(arrayData);
						}
					}
					items.add(castData);
				} else {
					array[itemC] = castData;
				}
			}
		} finally {
			try {
				iterator.close();
			} catch (SQLException e) {
				// ignored
			}
		}
		if (items == null) {
			if (itemC < array.length - 1) {
				array[itemC] = null;
			}
			return array;
		} else {
			@SuppressWarnings("unchecked")
			E[] castArray = (E[]) items.toArray();
			return castArray;
		}
	}

	/**
	 * This is just a call to {@link Object#equals(Object)}.
	 */
	@Override
	public boolean equals(Object other) {
		return super.equals(other);
	}

	/**
	 * This is just a call to {@link Object#hashCode()}.
	 */
	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
