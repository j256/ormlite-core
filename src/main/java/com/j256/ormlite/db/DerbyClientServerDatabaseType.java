package com.j256.ormlite.db;

/**
 * Derby database type information used to create the tables, etc.. This is for client connections to a remote Derby
 * server. For embedded databases, you should use {@link DerbyEmbeddedDatabaseType}.
 * 
 * @author graywatson
 */
public class DerbyClientServerDatabaseType extends DerbyEmbeddedDatabaseType implements DatabaseType {

	private final static String DRIVER_CLASS_NAME = "org.apache.derby.jdbc.ClientDriver";

	@Override
	public String getDriverClassName() {
		return DRIVER_CLASS_NAME;
	}
}
