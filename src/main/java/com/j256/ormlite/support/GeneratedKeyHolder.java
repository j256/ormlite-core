package com.j256.ormlite.support;

import java.sql.SQLException;

/**
 * The holder of a generated key so we can return the value of generated keys from update methods. Used by the
 * {@link DatabaseConnection#insert(String, Object[], com.j256.ormlite.field.FieldType[], GeneratedKeyHolder)} method.
 * 
 * @author graywatson
 */
public interface GeneratedKeyHolder {

	/**
	 * Return the name of the generated column we are interested in.
	 */
	public String getColumnName();

	/**
	 * Add the key number on the key holder. May be called multiple times.
	 */
	public void addKey(Number key) throws SQLException;
}
