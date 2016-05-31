package com.j256.ormlite;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.DatabaseConnection;

public class WrappedDatabaseConnection implements WrappedConnection, InvocationHandler {

	private final Object connectionProxy;
	private final DatabaseConnection connection;
	private List<WrappedCompiledStatement> wrappedStatements = new ArrayList<WrappedCompiledStatement>();

	public WrappedDatabaseConnection(DatabaseConnection connection) {
		this.connection = connection;
		this.connectionProxy =
				Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { DatabaseConnection.class }, this);
	}

	@Override
	public DatabaseConnection getDatabaseConnectionProxy() {
		return (DatabaseConnection) connectionProxy;
	}

	public DatabaseConnection getRealConnection() {
		return connection;
	}

	@Override
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
			throw e.getTargetException();
		}
	}

	@Override
	public boolean isOkay() {
		for (WrappedCompiledStatement wrappedStatement : wrappedStatements) {
			if (!wrappedStatement.isOkay()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void close() {
		wrappedStatements.clear();
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

		@Override
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
				throw e.getTargetException();
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
