package com.j256.ormlite.support;

import java.sql.SQLException;

/**
 * Defines a class that creates connection proxies. This can be set on the JdbcConnectionSource or
 * AndroidConnectionSource static setters.
 * 
 * @author graywatson
 */
public interface DatabaseConnectionProxyFactory {

	/**
	 * Create a proxy database connection that may extend {@link DatabaseConnectionProxy}. This method should instatiate
	 * the proxy and set the real-connection on it.
	 */
	public DatabaseConnection createProxy(DatabaseConnection realConnection) throws SQLException;
}
