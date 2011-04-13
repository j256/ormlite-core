package com.j256.ormlite.stmt;

import java.sql.SQLException;

import com.j256.ormlite.field.FieldType;

/**
 * An argument to a select SQL statement. After the query is constructed, the caller can set the value on this argument
 * and run the query. Then the argument can be set again and the query re-executed. This is equivalent in SQL to a ?
 * argument.
 * 
 * @author graywatson
 */
public interface ArgumentHolder {

	/**
	 * Return the column-name associated with this argument. The name is set by the package internally.
	 */
	public String getColumnName();

	/**
	 * Used internally by the package to set the column-name associated with this argument.
	 */
	public void setMetaInfo(String columnName, FieldType fieldType);

	/**
	 * Set the value associated with this argument. The value should be set by the user after the query has been built
	 * but before it has been executed.
	 */
	public void setValue(Object value);

	/**
	 * Return the value associated with this argument suitable for passing to SQL. The value should be set by the user
	 * before it is consumed.
	 */
	public Object getSqlArgValue() throws SQLException;

	/**
	 * Return the field type associated with this class.
	 */
	public FieldType getFieldType();
}
