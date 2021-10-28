package com.j256.ormlite.table;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.support.ConnectionSource;

/**
 * Schema utility class which will dump the schema statements needed by an array of classes.
 * 
 * @author graywatson
 */
public abstract class BaseSchemaUtils {

	/**
	 * Return the list of the classes we want to dump the schema of.
	 */
	protected abstract Class<?>[] getClasses();

	/**
	 * @deprecated Please use {@link #getConnectionSource()}.
	 */
	@Deprecated
	protected abstract DatabaseType getDatabaseType();

	/**
	 * Return the connection-source to be used to general the schema statements.
	 */
	protected ConnectionSource getConnectionSource() {
		// designed to be overridden but returning null here for backwards compatibility
		return null;
	}

	@SuppressWarnings("deprecation")
	protected void dumpSchema() throws SQLException {
		ConnectionSource connectionSource = getConnectionSource();
		DatabaseType databaseType = getDatabaseType();
		for (Class<?> clazz : getClasses()) {
			List<String> statements;
			if (connectionSource == null) {
				// here for backwards compatibility
				statements = TableUtils.getCreateTableStatements(databaseType, clazz);
			} else {
				statements = TableUtils.getCreateTableStatements(connectionSource, clazz);
			}
			for (String statement : statements) {
				System.out.println(statement + ";");
			}
		}
	}
}
