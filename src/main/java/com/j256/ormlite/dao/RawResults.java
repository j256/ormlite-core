package com.j256.ormlite.dao;

/**
 * Results returned by a call to {@link Dao#queryForAllRaw} or {@link Dao#iteratorRaw} which handles each result as a
 * String[].
 * 
 * <p>
 * <b>NOTE:</b> If you use the iterator then you must call {@link CloseableIterator#close()} method when you are done
 * otherwise the underlying SQL statement and connection may be kept open.
 * </p>
 * 
 * @author graywatson
 */
public interface RawResults extends CloseableIterable<String[]> {

	/**
	 * Return the number of columns in each result row.
	 */
	public int getNumberColumns();

	/**
	 * Return the array of column names for each result row.
	 */
	public String[] getColumnNames();
}
