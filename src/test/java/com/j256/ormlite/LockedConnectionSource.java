package com.j256.ormlite;

import java.io.IOException;
import java.sql.SQLException;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.misc.IOUtils;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;

/**
 * Connection source that has a simple lock around it the delegate.
 * 
 * @author graywatson
 */
public class LockedConnectionSource implements ConnectionSource {

	protected ConnectionSource delegate;

	public LockedConnectionSource(ConnectionSource cs) {
		this.delegate = cs;
	}

	@Override
	public DatabaseConnection getReadOnlyConnection(String tableName) throws SQLException {
		synchronized (delegate) {
			return delegate.getReadOnlyConnection(tableName);
		}
	}

	@Override
	public DatabaseConnection getReadWriteConnection(String tableName) throws SQLException {
		synchronized (delegate) {
			return delegate.getReadWriteConnection(tableName);
		}
	}

	@Override
	public void releaseConnection(DatabaseConnection connection) throws SQLException {
		synchronized (delegate) {
			delegate.releaseConnection(connection);
		}
	}

	@Override
	public void close() throws IOException {
		synchronized (delegate) {
			delegate.close();
		}
	}

	@Override
	public void closeQuietly() {
		IOUtils.closeQuietly(this);
	}

	@Override
	public boolean saveSpecialConnection(DatabaseConnection connection) throws SQLException {
		synchronized (delegate) {
			return delegate.saveSpecialConnection(connection);
		}
	}

	@Override
	public void clearSpecialConnection(DatabaseConnection connection) {
		synchronized (delegate) {
			delegate.clearSpecialConnection(connection);
		}
	}

	@Override
	public DatabaseConnection getSpecialConnection(String tableName) {
		synchronized (delegate) {
			return delegate.getSpecialConnection(tableName);
		}
	}

	@Override
	public DatabaseType getDatabaseType() {
		synchronized (delegate) {
			return delegate.getDatabaseType();
		}
	}

	@Override
	public boolean isOpen(String tableName) {
		synchronized (delegate) {
			return delegate.isOpen(tableName);
		}
	}

	@Override
	public boolean isSingleConnection(String tableName) {
		synchronized (delegate) {
			return delegate.isSingleConnection(tableName);
		}
	}
}
