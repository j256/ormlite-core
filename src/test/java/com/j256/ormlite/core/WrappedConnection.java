package com.j256.ormlite.core;

import java.lang.reflect.InvocationHandler;

import com.j256.ormlite.core.support.DatabaseConnection;

public interface WrappedConnection extends InvocationHandler {

	public DatabaseConnection getDatabaseConnectionProxy();

	public boolean isOkay();

	public void close();
}
