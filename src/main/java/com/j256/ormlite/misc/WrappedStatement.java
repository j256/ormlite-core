package com.j256.ormlite.misc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.logger.LoggerFactory;

/**
 * Wrapped statement so we can ensure we properly close all statements.
 * 
 * @author graywatson
 */
public class WrappedStatement implements InvocationHandler {

	private static final Logger logger = LoggerFactory.getLogger(WrappedStatement.class);

	private final Object statementProxy;
	private final Object statement;
	private boolean closeCalled;

	public WrappedStatement(Object statement, Class<?> statementClass) {
		this.statement = statement;
		this.statementProxy =
				Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { statementClass }, this);
	}

	public Object getPreparedStatement() {
		return statementProxy;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		logger.trace("{}: running method on CompiledStatement: {}", this, method.getName());
		try {
			Object obj = method.invoke(statement, args);
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
			logger.error("{}: CompiledStatement was not closed: {}", this, statement);
			return false;
		}
	}
}
