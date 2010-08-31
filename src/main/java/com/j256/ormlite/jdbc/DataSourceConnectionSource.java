package com.j256.ormlite.jdbc;

import java.sql.SQLException;

import javax.sql.DataSource;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;

/**
 * Wrapper around a {@link DataSource} that supports our ConnectionSource interface. This allows you to wrap other
 * multi-threaded, high-performance data sources, see Apache DBCP, CP30, or BoneCP.
 * 
 * <p>
 * <b> NOTE: </b> If you are using the Spring type wiring in Java, {@link #initialize} should be called after all of the
 * set methods. In Spring XML, init-method="initialize" should be used.
 * </p>
 * 
 * @author graywatson
 */
public class DataSourceConnectionSource implements ConnectionSource {

	private DataSource dataSource;

	/**
	 * Constructor for Spring type wiring if you are using the set methods.
	 */
	public DataSourceConnectionSource() {
		// for spring wiring
	}

	/**
	 * Create a data source for a particular database URL.
	 */
	public DataSourceConnectionSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * If you are using the Spring type wiring, this should be called after all of the set methods.
	 */
	public void initialize() {
		if (dataSource == null) {
			throw new IllegalStateException("dataSource was never set on " + getClass().getSimpleName());
		}
	}

	public DatabaseConnection getReadOnlyConnection() throws SQLException {
		return getReadWriteConnection();
	}

	public DatabaseConnection getReadOnlyConnection(String username, String password) throws SQLException {
		return getReadWriteConnection(username, password);
	}

	public DatabaseConnection getReadWriteConnection() throws SQLException {
		return new JdbcDatabaseConnection(dataSource.getConnection());
	}

	public void releaseConnection(DatabaseConnection connection) throws SQLException {
		// noop right now
	}

	public DatabaseConnection getReadWriteConnection(String username, String password) throws SQLException {
		return new JdbcDatabaseConnection(dataSource.getConnection(username, password));
	}

	public void close() throws SQLException {
		// unfortunately, you will need to close the DataSource directly since there is no close on the interface
	}
}