package com.j256.ormlite.dao;

/**
 * Parameterized row mapper that takes output from the {@link RawResults} and returns a T. Is used in the
 * {@link RawResults#iterator(RawRowMapper)} method.
 * 
 * @param <T>
 *            Type that the mapRow returns.
 * @author graywatson
 */
public interface RawRowMapper<T> {

	/**
	 * Used to convert a raw results row to an object.
	 * 
	 * @return The created object with all of the fields set from the results;
	 * @param columnNames
	 *            Array of names of columns.
	 * @param resultColumns
	 *            Array of result columns. 
	 */
	public T mapRow(String[] columnNames, String[] resultColumns);
}
