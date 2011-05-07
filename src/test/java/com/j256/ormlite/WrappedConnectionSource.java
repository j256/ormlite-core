package com.j256.ormlite;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;

/**
 * Wrapped connection source for testing purposes.
 * 
 * @author graywatson
 */
public class WrappedConnectionSource implements ConnectionSource {

	private AtomicInteger getReleaseCount = new AtomicInteger(0);
	private static boolean nextForceOkay = false;
	private ConnectionSource cs;
	private final Map<DatabaseConnection, WrappedDatabaseConnection> wrappedConnections =
			new HashMap<DatabaseConnection, WrappedDatabaseConnection>();

	public WrappedConnectionSource(ConnectionSource cs) throws SQLException {
		this.cs = cs;
	}

	public DatabaseConnection getReadOnlyConnection() throws SQLException {
		DatabaseConnection connection = cs.getReadOnlyConnection();
		getReleaseCount.incrementAndGet();
		// System.out.println("get/release count is " + getReleaseCount);
		WrappedDatabaseConnection wrapped = new WrappedDatabaseConnection(connection);
		wrappedConnections.put(wrapped.getProxy(), wrapped);
		return wrapped.getProxy();
	}

	public DatabaseConnection getReadWriteConnection() throws SQLException {
		DatabaseConnection connection = cs.getReadWriteConnection();
		getReleaseCount.incrementAndGet();
		// System.out.println("get/release count is " + getReleaseCount);
		WrappedDatabaseConnection wrapped = new WrappedDatabaseConnection(connection);
		wrappedConnections.put(wrapped.getProxy(), wrapped);
		return wrapped.getProxy();
	}

	public void releaseConnection(DatabaseConnection connection) throws SQLException {
		WrappedDatabaseConnection wrapped = wrappedConnections.remove(connection);
		if (wrapped == null) {
			throw new SQLException("Tried to release unknown connection");
		}
		cs.releaseConnection(wrapped.getProxy());
		getReleaseCount.decrementAndGet();
		// System.out.println("get/release count is " + getReleaseCount);
	}

	public void close() throws SQLException {
		cs.close();
		if (!isOkay()) {
			throw new SQLException("Wrapped connection was not okay on close");
		}
	}

	/**
	 * Used if we want to forcefully close a connection source
	 */
	public static void forceOkay() {
		nextForceOkay = true;
	}

	public boolean isOkay() {
		if (nextForceOkay) {
			nextForceOkay = false;
			return true;
		} else if (getReleaseCount.get() != 0) {
			System.err.println("get/release count is " + getReleaseCount.get());
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

	public boolean saveSpecialConnection(DatabaseConnection connection) throws SQLException {
		return cs.saveSpecialConnection(connection);
	}

	public void clearSpecialConnection(DatabaseConnection connection) {
		cs.clearSpecialConnection(connection);
	}

	public DatabaseConnection getSpecialConnection() {
		return cs.getSpecialConnection();
	}

	public DatabaseType getDatabaseType() {
		return cs.getDatabaseType();
	}

	public boolean isOpen() {
		return cs.isOpen();
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

	private static class WrappedDatabaseConnection implements InvocationHandler {

		private final Object connectionProxy;
		private final DatabaseConnection connection;
		private List<WrappedCompiledStatement> wrappedStatements = new ArrayList<WrappedCompiledStatement>();

		public WrappedDatabaseConnection(DatabaseConnection connection) {
			this.connection = connection;
			this.connectionProxy =
					Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { DatabaseConnection.class },
							this);
		}

		public DatabaseConnection getProxy() {
			return (DatabaseConnection) connectionProxy;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// System.err.println("Running method on Connection." + method.getName());
			try {
				Object obj = method.invoke(connection, args);
				if (method.getName().equals("compileStatement") && obj instanceof CompiledStatement) {
					WrappedCompiledStatement wrappedStatement = new WrappedCompiledStatement((CompiledStatement) obj);
					wrappedStatements.add(wrappedStatement);
					obj = wrappedStatement.getPreparedStatement();
				}
				return obj;
			} catch (InvocationTargetException e) {
				// pass on the exception
				throw e.getCause();
			}
		}

		public boolean isOkay() {
			for (WrappedCompiledStatement wrappedStatement : wrappedStatements) {
				if (!wrappedStatement.isOkay()) {
					return false;
				}
			}
			return true;
		}
	}

	private static class WrappedCompiledStatement implements InvocationHandler {

		private final Object statementProxy;
		private final CompiledStatement compiledStatement;
		private boolean closeCalled = false;

		public WrappedCompiledStatement(CompiledStatement compiledStatement) {
			this.compiledStatement = compiledStatement;
			this.statementProxy =
					Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { CompiledStatement.class },
							this);
		}

		public CompiledStatement getPreparedStatement() {
			return (CompiledStatement) statementProxy;
		}

		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// System.err.println("Running method on CompiledStatement." + method.getName());
			try {
				Object obj = method.invoke(compiledStatement, args);
				if (method.getName().equals("close")) {
					closeCalled = true;
				}
				return obj;
			} catch (InvocationTargetException e) {
				// pass on the exception
				throw e.getCause();
			}
		}

		public boolean isOkay() {
			if (closeCalled) {
				return true;
			} else {
				System.err.println("PreparedStatement was not closed: " + compiledStatement.toString());
				return false;
			}
		}
	}
}
