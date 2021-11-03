package com.j256.ormlite.misc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.DatabaseConnection;

/**
 * A wrapped database connection returned by {@link WrappedConnectionSource}. This uses a proxy object so it can track
 * the statements and make sure everything has been closed appropriately.
 * 
 * @author graywatson
 */
public class WrappedDatabaseConnection implements InvocationHandler {

	private static final Logger logger = LoggerFactory.getLogger(WrappedDatabaseConnection.class);

	protected final DatabaseConnection connection;
	protected final DatabaseConnection connectionProxy;
	protected final List<WrappedStatement> wrappedStatements = new ArrayList<WrappedStatement>();

	public WrappedDatabaseConnection(DatabaseConnection connection) {
		this.connection = connection;
		this.connectionProxy = (DatabaseConnection) Proxy.newProxyInstance(getClass().getClassLoader(),
				new Class<?>[] { DatabaseConnection.class }, this);
	}

	public DatabaseConnection getDatabaseConnection() {
		return connection;
	}

	public DatabaseConnection getDatabaseConnectionProxy() {
		return connectionProxy;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		logger.trace("{}: running method on connection: {}", this, method.getName());
		try {
			Object obj = method.invoke(connection, args);
			if (method.getName().equals("compileStatement") && obj instanceof CompiledStatement) {
				WrappedStatement wrappedStatement = new WrappedStatement((CompiledStatement) obj);
				wrappedStatements.add(wrappedStatement);
				logger.trace("{}: connection is wrapping statement: {}", this, obj);
				obj = wrappedStatement.getStatementProxy();
			}
			return obj;
		} catch (InvocationTargetException e) {
			// pass on the exception
			throw e.getTargetException();
		}
	}

	/**
	 * See if all of the wrapped statements have been closed.
	 */
	public boolean isAllStatementsClosed() {
		for (WrappedStatement wrappedStatement : wrappedStatements) {
			if (!wrappedStatement.isClosed()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Close our connection.
	 */
	public void close() {
		wrappedStatements.clear();
	}
}
