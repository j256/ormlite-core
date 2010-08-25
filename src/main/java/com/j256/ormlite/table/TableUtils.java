package com.j256.ormlite.table;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;

/**
 * Couple utility methods for the creating, dropping, and maintenance of tables.
 * 
 * @author graywatson
 */
public class TableUtils {

	private static Logger logger = LoggerFactory.getLogger(TableUtils.class);

	/**
	 * For static methods only.
	 */
	private TableUtils() {
	}

	/**
	 * Issue the database statements to create the table associated with a class. Most likely this will be done
	 * <i>only</i> when a database is configured or in unit tests.
	 * 
	 * @param databaseType
	 *            Our database type.
	 * @param connectionSource
	 *            Associated connection source.
	 * @param dataClass
	 *            The class for which a table will be created.
	 * @return The number of statements executed to do so.
	 */
	public static <T> int createTable(DatabaseType databaseType, ConnectionSource connectionSource, Class<T> dataClass)
			throws SQLException {
		return createTable(databaseType, connectionSource, DatabaseTableConfig.fromClass(databaseType, dataClass));
	}

	/**
	 * Issue the database statements to create the table associated with a class. Most likely this will be done
	 * <i>only</i> when a database is configured or in unit tests.
	 * 
	 * @param databaseType
	 *            Our database type.
	 * @param connectionSource
	 *            connectionSource Associated connection source.
	 * @param tableConfig
	 *            Hand or spring wired table configuration. If null then the class must have {@link DatabaseField}
	 *            annotations.
	 * @return The number of statements executed to do so.
	 */
	public static <T> int createTable(DatabaseType databaseType, ConnectionSource connectionSource,
			DatabaseTableConfig<T> tableConfig) throws SQLException {
		TableInfo<T> tableInfo = new TableInfo<T>(databaseType, tableConfig);
		logger.debug("creating table '{}'", tableInfo.getTableName());
		List<String> statements = new ArrayList<String>();
		List<String> queriesAfter = new ArrayList<String>();
		createTableStatements(databaseType, tableInfo, statements, queriesAfter);
		int stmtC = 0;
		DatabaseConnection connection = connectionSource.getReadWriteConnection();
		for (String statement : statements) {
			int rowC;
			CompiledStatement prepStmt = null;
			try {
				logger.debug("executing create table statement: {}", statement);
				prepStmt = connection.compileStatement(statement);
				rowC = prepStmt.executeUpdate();
			} catch (SQLException e) {
				// we do this to make sure that the statement is in the exception
				throw SqlExceptionUtil.create("SQL statement failed: " + statement, e);
			} finally {
				if (prepStmt != null) {
					prepStmt.close();
				}
			}
			// sanity check
			if (rowC < 0) {
				throw new SQLException("SQL statement updated " + rowC + " rows, we were expecting >= 0: " + statement);
			} else if (rowC > 0 && databaseType.isCreateTableReturnsZero()) {
				throw new SQLException("SQL statement updated " + rowC + " rows, we were expecting == 0: " + statement);
			}

			stmtC++;
		}
		// now execute any test queries which test the newly created table
		for (String query : queriesAfter) {
			CompiledStatement prepStmt = null;
			DatabaseResults results = null;
			try {
				prepStmt = connection.compileStatement(query);
				results = prepStmt.executeQuery();
				int rowC = 0;
				// count the results
				while (results.next()) {
					rowC++;
				}
				logger.debug("executing create table after-query got {} results: {}", rowC, query);
			} catch (SQLException e) {
				// we do this to make sure that the statement is in the exception
				throw SqlExceptionUtil.create("executing create table after-query failed: " + query, e);
			} finally {
				// result set is closed by the statement being closed
				if (prepStmt != null) {
					prepStmt.close();
				}
			}
			stmtC++;
		}
		return stmtC;
	}

	/**
	 * Return an ordered collection of SQL statements that need to be run to create a table. To do the work of creating,
	 * you should call {@link #createTable}.
	 * 
	 * @param databaseType
	 *            Our database type.
	 * @param dataClass
	 *            The class for which a table will be created.
	 * @return The collection of table create statements.
	 */
	public static <T> List<String> getCreateTableStatements(DatabaseType databaseType, Class<T> dataClass)
			throws SQLException {
		return getCreateTableStatements(databaseType, DatabaseTableConfig.fromClass(databaseType, dataClass));
	}

	/**
	 * Return an ordered collection of SQL statements that need to be run to create a table. To do the work of creating,
	 * you should call {@link #createTable}.
	 * 
	 * @param databaseType
	 *            Our database type.
	 * @param tableConfig
	 *            Hand or spring wired table configuration. If null then the class must have {@link DatabaseField}
	 *            annotations.
	 * @return The collection of table create statements.
	 */
	public static <T> List<String> getCreateTableStatements(DatabaseType databaseType,
			DatabaseTableConfig<T> tableConfig) throws SQLException {
		TableInfo<T> tableInfo = new TableInfo<T>(databaseType, tableConfig);
		List<String> statements = new ArrayList<String>();
		List<String> queriesAfter = new ArrayList<String>();
		createTableStatements(databaseType, tableInfo, statements, queriesAfter);
		return statements;
	}
	/**
	 * Issue the database statements to create the table associated with a class. Most likely this will be done
	 * <i>only</i> in unit tests.
	 * 
	 * <p>
	 * <b>WARNING:</b> This is [obviously] very destructive and unrecoverable.
	 * </p>
	 * 
	 * @param databaseType
	 *            Our database type.
	 * @param connectionSource
	 *            Associated connection source.
	 * @param dataClass
	 *            The class for which a table will be created.
	 * @param ignoreErrors
	 *            If set to true then try each statement regardless of {@link SQLException} thrown previously.
	 * @return The number of statements executed to do so.
	 */
	public static <T> int dropTable(DatabaseType databaseType, ConnectionSource connectionSource, Class<T> dataClass,
			boolean ignoreErrors) throws SQLException {
		return dropTable(databaseType, connectionSource, DatabaseTableConfig.fromClass(databaseType, dataClass),
				ignoreErrors);
	}

	/**
	 * Issue the database statements to create the table associated with a class. Most likely this will be done
	 * <i>only</i> in unit tests.
	 * 
	 * <p>
	 * <b>WARNING:</b> This is [obviously] very destructive and unrecoverable.
	 * </p>
	 * 
	 * @param databaseType
	 *            Our database type.
	 * @param connectionSource
	 *            Associated connection source.
	 * @param tableConfig
	 *            Hand or spring wired table configuration. If null then the class must have {@link DatabaseField}
	 *            annotations.
	 * @param ignoreErrors
	 *            If set to true then try each statement regardless of {@link SQLException} thrown previously.
	 * @return The number of statements executed to do so.
	 */
	public static <T> int dropTable(DatabaseType databaseType, ConnectionSource connectionSource,
			DatabaseTableConfig<T> tableConfig, boolean ignoreErrors) throws SQLException {
		TableInfo<T> tableInfo = new TableInfo<T>(databaseType, tableConfig);
		logger.debug("dropping table '{}'", tableInfo.getTableName());
		Collection<String> statements = dropTableStatements(databaseType, tableInfo);
		int stmtC = 0;
		DatabaseConnection connection = connectionSource.getReadWriteConnection();
		for (String statement : statements) {
			int rowC = 0;
			CompiledStatement prepStmt = null;
			try {
				logger.debug("executing drop table statement: {}", statement);
				prepStmt = connection.compileStatement(statement);
				rowC = prepStmt.executeUpdate();
			} catch (SQLException e) {
				if (!ignoreErrors) {
					throw e;
				}
			} finally {
				if (prepStmt != null) {
					prepStmt.close();
				}
			}
			// sanity check
			if (rowC < 0) {
				throw new SQLException("SQL statement " + statement + " updated " + rowC
						+ " rows, we were expecting >= 0");
			}
			stmtC++;
		}
		return stmtC;
	}

	/**
	 * Generate and return the list of statements to create a database table and any associated features.
	 */
	private static <T> void createTableStatements(DatabaseType databaseType, TableInfo<T> tableInfo,
			List<String> statements, List<String> queriesAfter) {
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE ");
		databaseType.appendEscapedEntityName(sb, tableInfo.getTableName());
		sb.append(" (");
		List<String> additionalArgs = new ArrayList<String>();
		List<String> statementsBefore = new ArrayList<String>();
		List<String> statementsAfter = new ArrayList<String>();
		// our statement will be set here later
		boolean first = true;
		for (FieldType fieldType : tableInfo.getFieldTypes()) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			// we have to call back to the database type for the specific create syntax
			databaseType.appendColumnArg(sb, fieldType, additionalArgs, statementsBefore, statementsAfter, queriesAfter);
		}
		for (String arg : additionalArgs) {
			// we will have spat out one argument already so we don't have to do the first dance
			sb.append(", ").append(arg);
		}
		sb.append(") ");
		databaseType.appendCreateTableSuffix(sb);
		statements.addAll(statementsBefore);
		statements.add(sb.toString());
		statements.addAll(statementsAfter);
	}

	/**
	 * Generate and return the list of statements to drop a database table.
	 */
	private static <T> Collection<String> dropTableStatements(DatabaseType databaseType, TableInfo<T> tableInfo) {
		List<String> statementsBefore = new ArrayList<String>();
		List<String> statementsAfter = new ArrayList<String>();
		for (FieldType fieldType : tableInfo.getFieldTypes()) {
			databaseType.dropColumnArg(fieldType, statementsBefore, statementsAfter);
		}
		StringBuilder sb = new StringBuilder();
		sb.append("DROP TABLE ");
		databaseType.appendEscapedEntityName(sb, tableInfo.getTableName());
		sb.append(' ');
		List<String> statements = new ArrayList<String>();
		statements.addAll(statementsBefore);
		statements.add(sb.toString());
		statements.addAll(statementsAfter);
		return statements;
	}
}
