package com.j256.ormlite.h2;

import java.sql.DriverManager;
import java.sql.SQLException;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;

/**
 * H2 connection source.
 * 
 * @author graywatson
 */
public class H2ConnectionSource implements ConnectionSource {

	protected static final String DATABASE_URL = "jdbc:h2:mem:h2testdatabase";

	private DatabaseType databaseType = new H2DatabaseType();
	private DatabaseConnection connection = null;

	public H2ConnectionSource() throws SQLException {
		databaseType.loadDriver();
	}

	public void close() throws SQLException {
		if (connection != null) {
			connection.close();
			connection = null;
		}
	}

	public DatabaseConnection getReadOnlyConnection() throws SQLException {
		return getReadWriteConnection();
	}

	public DatabaseConnection getReadWriteConnection() throws SQLException {
		if (connection == null) {
			connection = new H2DatabaseConnection(DriverManager.getConnection(DATABASE_URL));
		}
		return connection;
	}

	public void releaseConnection(DatabaseConnection connection) throws SQLException {
		// noop right now
	}

	public boolean saveSpecialConnection(DatabaseConnection connection) throws SQLException {
		// noop since this is a single connection source
		return true;
	}

	public void clearSpecialConnection(DatabaseConnection connection) {
		// noop since this is a single connection source
	}

	public void setDatabaseType(DatabaseType databaseType) {
		this.databaseType = databaseType;
	}

	public DatabaseType getDatabaseType() {
		return databaseType;
	}

	public DatabaseConnection getSpecialConnection() {
		return null;
	}
}
