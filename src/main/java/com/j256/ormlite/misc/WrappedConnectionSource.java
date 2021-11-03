package com.j256.ormlite.misc;

import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;

/**
 * Wrapped connection source that uses proxy objects to track database connections and compiled statements. We use this
 * to insure that everything is closed appropriately.
 * 
 * @author graywatson
 */
public class WrappedConnectionSource implements ConnectionSource {

	protected static final Logger logger = LoggerFactory.getLogger(WrappedConnectionSource.class);

	private final AtomicInteger connectionCount = new AtomicInteger(0);
	protected final ConnectionSource cs;
	private final Map<DatabaseConnection, WrappedDatabaseConnection> wrappedConnections =
			new HashMap<DatabaseConnection, WrappedDatabaseConnection>();
	protected boolean nextForceOkay;

	public WrappedConnectionSource(ConnectionSource cs) {
		this.cs = cs;
	}

	@Override
	public DatabaseConnection getReadOnlyConnection(String tableName) throws SQLException {
		DatabaseConnection connection = cs.getReadOnlyConnection(tableName);
		connectionCount.incrementAndGet();
		WrappedDatabaseConnection wrapped = wrapConnection(connection);
		wrappedConnections.put(wrapped.getDatabaseConnection(), wrapped);
		logger.trace("{}: got wrapped read-only DatabaseConnection '{}', count = {}", this, wrapped, connectionCount);
		return wrapped.getDatabaseConnection();
	}

	@Override
	public DatabaseConnection getReadWriteConnection(String tableName) throws SQLException {
		DatabaseConnection connection = cs.getReadWriteConnection(tableName);
		connectionCount.incrementAndGet();
		WrappedDatabaseConnection wrapped = wrapConnection(connection);
		connection = wrapped.getDatabaseConnection();
		wrappedConnections.put(connection, wrapped);
		logger.trace("{}: got wrapped read-write DatabaseConnection '{}', count = {}", this, wrapped, connectionCount);
		return connection;
	}

	@Override
	public void releaseConnection(DatabaseConnection connection) throws SQLException {
		WrappedDatabaseConnection wrapped = wrappedConnections.remove(connection);
		if (wrapped == null) {
			if (nextForceOkay) {
				return;
			} else {
				throw new SQLException("Tried to release unknown connection");
			}
		} else if (!wrapped.isOkay()) {
			throw new SQLException("Wrapped connection was not okay when released");
		}
		cs.releaseConnection(wrapped.getDatabaseConnection());
		connectionCount.decrementAndGet();
		logger.trace("{}: released wrapped DatabaseConnection '{}', count = {}", this, wrapped, connectionCount);
	}

	@Override
	public void close() throws IOException {
		cs.close();
		if (!isOkay()) {
			throw new IOException("Wrapped connection was not okay on close");
		}
		for (WrappedDatabaseConnection wrapped : wrappedConnections.values()) {
			wrapped.close();
		}
		wrappedConnections.clear();
	}

	@Override
	public void closeQuietly() {
		IOUtils.closeQuietly(this);
	}

	protected WrappedDatabaseConnection wrapConnection(DatabaseConnection connection) {
		WrappedDatabaseConnection wrapped = new WrappedDatabaseConnection(connection, DatabaseConnection.class);
		return wrapped;
	}

	/**
	 * Used if we want to forcefully close a connection source without throwing errors.
	 */
	public void forceOkay() {
		nextForceOkay = true;
	}

	public boolean isOkay() {
		if (nextForceOkay) {
			nextForceOkay = false;
			return true;
		} else if (connectionCount.get() != 0) {
			logger.error("{}: ConnectionSource is not ok, connection-count = {}", this, connectionCount);
			for (WrappedDatabaseConnection wrapped : wrappedConnections.values()) {
				logger.error("{}: still has wrapped DatabaseConnection '{}'", this, wrapped);
			}
			return false;
		} else {
			for (WrappedDatabaseConnection wrapped : wrappedConnections.values()) {
				if (!wrapped.isOkay()) {
					return false;
				}
			}
			return true;
		}
	}

	@Override
	public boolean saveSpecialConnection(DatabaseConnection connection) throws SQLException {
		return cs.saveSpecialConnection(connection);
	}

	@Override
	public void clearSpecialConnection(DatabaseConnection connection) {
		cs.clearSpecialConnection(connection);
	}

	@Override
	public DatabaseConnection getSpecialConnection(String tableName) {
		return cs.getSpecialConnection(tableName);
	}

	@Override
	public DatabaseType getDatabaseType() {
		return cs.getDatabaseType();
	}

	@Override
	public boolean isOpen(String tableName) {
		return cs.isOpen(tableName);
	}

	@Override
	public boolean isSingleConnection(String tableName) {
		return cs.isSingleConnection(tableName);
	}

	public void setDatabaseType(DatabaseType databaseType) {
		Method method;
		try {
			method = cs.getClass().getMethod("setDatabaseType", new Class[] { DatabaseType.class });
			method.invoke(cs, databaseType);
		} catch (Exception e) {
			throw new RuntimeException("Could not set database type", e);
		}
	}

	public int getConnectionCount() {
		return connectionCount.get();
	}
}
