package com.j256.ormlite.stmt;

import java.sql.SQLException;

/**
 * An argument to a select SQL statement. After the query is constructed, the caller can set the value on this argument
 * and run the query. Then the argument can be set again and the query re-executed. This is equivalent in JDBC to a ?
 * type argument.
 * 
 * <p>
 * NOTE: If the argument has not been set by the time the query is executed, an exception will be thrown.
 * </p>
 * 
 * <p>
 * NOTE: For protections sake, the object cannot be reused with different column names.
 * </p>
 * 
 * <blockquote>
 * 
 * <pre>
 * // build a query using the Account DAO
 * QueryBuilder&lt;Account, String&gt; qb = accountDao.queryBuilder();
 * 
 * // create an argument which will be set later
 * SelectArg passwordSelectArg = new SelectArg();
 * qb.where().eq(Account.PASSWORD_FIELD_NAME, passwordSelectArg);
 * // prepare the query
 * PreparedQuery&lt;Account&gt; preparedQuery = qb.prepareQuery();
 * // ...
 * 
 * // some time later we set the value and run the query 
 * passwordSelectArg.setValue(&quot;_secret&quot;);
 * List&lt;Account&gt; results = accountDao.query(preparedQuery);
 * // we can then re-set the value and re-run the query 
 * passwordSelectArg.setValue(&quot;qwerty&quot;);
 * List&lt;Account&gt; results = accountDao.query(preparedQuery);
 * </pre>
 * 
 * </blockquote>
 * 
 * @author graywatson
 */
public class SelectArg {

	private boolean hasBeenSet = false;
	private String columnName = null;
	private Object value = null;

	/**
	 * Return the column-name associated with this argument. The name is set by the package internally.
	 */
	public String getColumnName() {
		if (columnName == null) {
			throw new IllegalArgumentException("Column name has not been set");
		} else {
			return columnName;
		}
	}

	/**
	 * Used internally by the package to set the column-name associated with this argument.
	 */
	public void setColumnName(String columnName) {
		if (this.columnName == null) {
			// not set yet
		} else if (this.columnName.equals(columnName)) {
			// set to the same value as before
		} else {
			throw new IllegalArgumentException("Column name cannot be set twice from " + this.columnName + " to "
					+ columnName);
		}
		this.columnName = columnName;
	}

	/**
	 * Return the value associated with this argument. The value should be set by the user before it is consumed.
	 */
	public Object getValue() throws SQLException {
		if (hasBeenSet) {
			return value;
		} else {
			throw new SQLException("Column value has not been set for " + columnName);
		}
	}

	/**
	 * Set the value associated with this argument. The value should be set by the user after the query has been built
	 * but before it has been executed.
	 */
	public void setValue(Object value) {
		this.hasBeenSet = true;
		this.value = value;
	}

	@Override
	public String toString() {
		if (hasBeenSet) {
			return "set arg(" + value + ")";
		} else {
			return "unset arg()";
		}
	}
}
