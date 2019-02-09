package com.j256.ormlite.table;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.db.DatabaseType;

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
	 * Return the database-type to use to generate the schema statements.
	 */
	protected abstract DatabaseType getDatabaseType();

	protected void dumpSchema() throws SQLException {
		for (Class<?> clazz : getClasses()) {
			List<String> statements = TableUtils.getCreateTableStatements(getDatabaseType(), clazz);
			for (String statement : statements) {
				System.out.println(statement);
			}
		}
	}
}
