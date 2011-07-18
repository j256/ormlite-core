package com.j256.ormlite.dao;

import java.sql.SQLException;
import java.util.List;

/**
 * You should be using {@link GenericRawResults} with &lt;String[]&gt; argument.
 * 
 * @deprecated
 */
@Deprecated
public interface RawResults extends CloseableIterable<String[]> {

	/**
	 * Return the number of columns in each result row.
	 */
	public int getNumberColumns();

	/**
	 * Return the array of column names for each result row.
	 */
	public String[] getColumnNames();

	/**
	 * Return a list of results mapped by the mapper argument.
	 */
	public List<String[]> getResults() throws SQLException;

	/**
	 * Return a list of results mapped by the mapper argument.
	 */
	public <T> List<T> getMappedResults(RawRowMapper<T> mapper) throws SQLException;

	/**
	 * Get an iterator which will return a T which is mapped from the String[] array raw results my the mapper argument.
	 */
	public <T> CloseableIterator<T> iterator(RawRowMapper<T> mapper) throws SQLException;

	/**
	 * Close any open database connections associated with the RawResults. This is only applicable if the
	 * {@link Dao#iterator()} or another iterator method was called.
	 */
	public void close() throws SQLException;
}
