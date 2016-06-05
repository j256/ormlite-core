package com.j256.ormlite.h2;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.misc.IOUtils;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseConnectionProxyFactory;

/**
 * H2 connection source.
 * 
 * @author graywatson
 */
public class H2ConnectionSource implements ConnectionSource {

	public DatabaseType databaseType = new H2DatabaseType();
	private DatabaseConnection connection = null;
	private final String databaseUrl;
	private static DatabaseConnectionProxyFactory connectionProxyFactory;

	public H2ConnectionSource() throws SQLException {
		this(H2DatabaseType.DATABASE_URL);
	}

	public H2ConnectionSource(String databaseUrl) throws SQLException {
		databaseType.loadDriver();
		this.databaseUrl = databaseUrl;
	}

	@Override
	public void close() throws IOException {
		if (connection != null) {
			connection.close();
			connection = null;
		}
	}

	@Override
	public void closeQuietly() {
		IOUtils.closeQuietly(this);
	}

	@Override
	public DatabaseConnection getReadOnlyConnection(String tableName) throws SQLException {
		return getReadWriteConnection(tableName);
	}

	@Override
	public DatabaseConnection getReadWriteConnection(String tableName) throws SQLException {
		if (connection == null) {
			connection = new H2DatabaseConnection(DriverManager.getConnection(databaseUrl));
			if (connectionProxyFactory != null) {
				connection = connectionProxyFactory.createProxy(connection);
			}
		}
		return connection;
	}

	@Override
	public void releaseConnection(DatabaseConnection connection) {
		// noop right now
	}

	@Override
	public boolean saveSpecialConnection(DatabaseConnection connection) {
		// noop since this is a single connection source
		return true;
	}

	@Override
	public void clearSpecialConnection(DatabaseConnection connection) {
		// noop since this is a single connection source
	}

	public void setDatabaseType(DatabaseType databaseType) {
		this.databaseType = databaseType;
	}

	@Override
	public DatabaseType getDatabaseType() {
		return databaseType;
	}

	@Override
	public DatabaseConnection getSpecialConnection(String tableName) {
		return null;
	}

	@Override
	public boolean isOpen(String tableName) {
		return connection != null;
	}

	@Override
	public boolean isSingleConnection(String tableName) {
		return true;
	}

	/**
	 * Set to enable connection proxying. Set to null to disable.
	 */
	public static void setDatabaseConnectionProxyFactory(DatabaseConnectionProxyFactory connectionProxyFactory) {
		H2ConnectionSource.connectionProxyFactory = connectionProxyFactory;
	}
}
