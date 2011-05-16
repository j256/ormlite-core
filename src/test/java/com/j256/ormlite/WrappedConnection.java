package com.j256.ormlite;

import java.lang.reflect.InvocationHandler;

import com.j256.ormlite.support.DatabaseConnection;

public interface WrappedConnection extends InvocationHandler {

	public DatabaseConnection getDatabaseConnectionProxy();

	public boolean isOkay();

	public void close();
}
