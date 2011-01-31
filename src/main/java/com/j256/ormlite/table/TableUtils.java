package com.j256.ormlite.table;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;
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
	private static final FieldType[] noFieldTypes = new FieldType[0];

	/**
	 * For static methods only.
	 */
	private TableUtils() {
	}

	/**
	 * Issue the database statements to create the table associated with a class. Most likely this will be done
	 * <i>only</i> when a database is configured or in unit tests.
	 * 
	 * @param connectionSource
	 *            Associated connection source.
	 * @param dataClass
	 *            The class for which a table will be created.
	 * @return The number of statements executed to do so.
	 */
	public static <T> int createTable(ConnectionSource connectionSource, Class<T> dataClass) throws SQLException {
		return doCreateTable(connectionSource, DatabaseTableConfig.fromClass(connectionSource, dataClass));
	}

	/**
	 * Issue the database statements to create the table associated with a class. Most likely this will be done
	 * <i>only</i> when a database is configured or in unit tests.
	 * 
	 * @param connectionSource
	 *            connectionSource Associated connection source.
	 * @param tableConfig
	 *            Hand or spring wired table configuration. If null then the class must have {@link DatabaseField}
	 *            annotations.
	 * @return The number of statements executed to do so.
	 */
	public static <T> int createTable(ConnectionSource connectionSource, DatabaseTableConfig<T> tableConfig)
			throws SQLException {
		tableConfig.extractFieldTypes(connectionSource);
		return doCreateTable(connectionSource, tableConfig);
	}

	/**
	 * Return an ordered collection of SQL statements that need to be run to create a table. To do the work of creating,
	 * you should call {@link #createTable}.
	 * 
	 * @param connectionSource
	 *            Our connect source which is used to get the database type, not to apply the creates.
	 * @param dataClass
	 *            The class for which a table will be created.
	 * @return The collection of table create statements.
	 */
	public static <T> List<String> getCreateTableStatements(ConnectionSource connectionSource, Class<T> dataClass)
			throws SQLException {
		return doCreateTableStatements(connectionSource, DatabaseTableConfig.fromClass(connectionSource, dataClass));
	}

	/**
	 * Return an ordered collection of SQL statements that need to be run to create a table. To do the work of creating,
	 * you should call {@link #createTable}.
	 * 
	 * @param connectionSource
	 *            Our connect source which is used to get the database type, not to apply the creates.
	 * @param tableConfig
	 *            Hand or spring wired table configuration. If null then the class must have {@link DatabaseField}
	 *            annotations.
	 * @return The collection of table create statements.
	 */
	public static <T> List<String> getCreateTableStatements(ConnectionSource connectionSource,
			DatabaseTableConfig<T> tableConfig) throws SQLException {
		tableConfig.extractFieldTypes(connectionSource);
		return doCreateTableStatements(connectionSource, tableConfig);
	}

	/**
	 * Issue the database statements to create the table associated with a class. Most likely this will be done
	 * <i>only</i> in unit tests.
	 * 
	 * <p>
	 * <b>WARNING:</b> This is [obviously] very destructive and unrecoverable.
	 * </p>
	 * 
	 * @param connectionSource
	 *            Associated connection source.
	 * @param dataClass
	 *            The class for which a table will be created.
	 * @param ignoreErrors
	 *            If set to true then try each statement regardless of {@link SQLException} thrown previously.
	 * @return The number of statements executed to do so.
	 */
	public static <T> int dropTable(ConnectionSource connectionSource, Class<T> dataClass, boolean ignoreErrors)
			throws SQLException {
		DatabaseType databaseType = connectionSource.getDatabaseType();
		return doDropTable(databaseType, connectionSource, DatabaseTableConfig.fromClass(connectionSource, dataClass),
				ignoreErrors);
	}

	/**
	 * <p>
	 * <b>WARNING:</b> This is [obviously] very destructive and unrecoverable.
	 * </p>
	 * 
	 * @param connectionSource
	 *            Associated connection source.
	 * @param tableConfig
	 *            Hand or spring wired table configuration. If null then the class must have {@link DatabaseField}
	 *            annotations.
	 * @param ignoreErrors
	 *            If set to true then try each statement regardless of {@link SQLException} thrown previously.
	 * @return The number of statements executed to do so.
	 */
	public static <T> int dropTable(ConnectionSource connectionSource, DatabaseTableConfig<T> tableConfig,
			boolean ignoreErrors) throws SQLException {
		DatabaseType databaseType = connectionSource.getDatabaseType();
		tableConfig.extractFieldTypes(connectionSource);
		return doDropTable(databaseType, connectionSource, tableConfig, ignoreErrors);
	}

	private static <T> int doDropTable(DatabaseType databaseType, ConnectionSource connectionSource,
			DatabaseTableConfig<T> tableConfig, boolean ignoreErrors) throws SQLException {
		TableInfo<T> tableInfo = new TableInfo<T>(databaseType, tableConfig);
		logger.info("dropping table '{}'", tableInfo.getTableName());
		List<String> statements = new ArrayList<String>();
		addDropIndexStatements(databaseType, tableInfo, statements);
		dropTableStatements(databaseType, tableInfo, statements);
		DatabaseConnection connection = connectionSource.getReadWriteConnection();
		try {
			return doDropStatements(connection, statements, ignoreErrors);
		} finally {
			connectionSource.releaseConnection(connection);
		}
	}

	private static <T> void addDropIndexStatements(DatabaseType databaseType, TableInfo<T> tableInfo,
			List<String> statements) {
		// run through and look for index annotations
		Set<String> indexSet = new HashSet<String>();
		for (FieldType fieldType : tableInfo.getFieldTypes()) {
			String indexName = fieldType.getIndexName();
			if (indexName != null) {
				indexSet.add(indexName);
			}
			String uniqueIndexName = fieldType.getUniqueIndexName();
			if (uniqueIndexName != null) {
				indexSet.add(uniqueIndexName);
			}
		}

		StringBuilder sb = new StringBuilder();
		for (String indexName : indexSet) {
			logger.info("dropping index '{}' for table '{}", indexName, tableInfo.getTableName());
			sb.append("DROP INDEX ");
			databaseType.appendEscapedEntityName(sb, indexName);
			statements.add(sb.toString());
			sb.setLength(0);
		}
	}

	private static int doDropStatements(DatabaseConnection connection, Collection<String> statements,
			boolean ignoreErrors) throws SQLException {
		int stmtC = 0;
		for (String statement : statements) {
			int rowC = 0;
			CompiledStatement prepStmt = null;
			try {
				prepStmt = connection.compileStatement(statement, StatementType.EXECUTE, noFieldTypes, noFieldTypes);
				rowC = prepStmt.executeUpdate();
				logger.info("executed drop table statement changed {} rows: {}", rowC, statement);
			} catch (SQLException e) {
				if (ignoreErrors) {
					logger.info("ignoring drop error '" + e.getMessage() + "' for statement: " + statement);
				} else {
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
		addCreateIndexStatements(databaseType, tableInfo, statements, false);
		addCreateIndexStatements(databaseType, tableInfo, statements, true);
	}

	private static <T> void addCreateIndexStatements(DatabaseType databaseType, TableInfo<T> tableInfo,
			List<String> statements, boolean unique) {
		// run through and look for index annotations
		Map<String, List<String>> indexMap = new HashMap<String, List<String>>();
		for (FieldType fieldType : tableInfo.getFieldTypes()) {
			String indexName;
			if (unique) {
				indexName = fieldType.getUniqueIndexName();
			} else {
				indexName = fieldType.getIndexName();
			}
			if (indexName == null) {
				continue;
			}

			List<String> columnList = indexMap.get(indexName);
			if (columnList == null) {
				columnList = new ArrayList<String>();
				indexMap.put(indexName, columnList);
			}
			columnList.add(fieldType.getDbColumnName());
		}

		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, List<String>> indexEntry : indexMap.entrySet()) {
			logger.info("creating index '{}' for table '{}", indexEntry.getKey(), tableInfo.getTableName());
			sb.append("CREATE ");
			if (unique) {
				sb.append("UNIQUE ");
			}
			sb.append("INDEX ");
			databaseType.appendEscapedEntityName(sb, indexEntry.getKey());
			sb.append(" ON ");
			databaseType.appendEscapedEntityName(sb, tableInfo.getTableName());
			sb.append(" ( ");
			boolean first = true;
			for (String columnName : indexEntry.getValue()) {
				if (first) {
					first = false;
				} else {
					sb.append(", ");
				}
				databaseType.appendEscapedEntityName(sb, columnName);
			}
			sb.append(" )");
			statements.add(sb.toString());
			sb.setLength(0);
		}
	}

	/**
	 * Generate and return the list of statements to drop a database table.
	 */
	private static <T> Collection<String> dropTableStatements(DatabaseType databaseType, TableInfo<T> tableInfo,
			List<String> statements) {
		List<String> statementsBefore = new ArrayList<String>();
		List<String> statementsAfter = new ArrayList<String>();
		for (FieldType fieldType : tableInfo.getFieldTypes()) {
			databaseType.dropColumnArg(fieldType, statementsBefore, statementsAfter);
		}
		StringBuilder sb = new StringBuilder();
		sb.append("DROP TABLE ");
		databaseType.appendEscapedEntityName(sb, tableInfo.getTableName());
		sb.append(' ');
		statements.addAll(statementsBefore);
		statements.add(sb.toString());
		statements.addAll(statementsAfter);
		return statements;
	}

	private static <T> int doCreateTable(ConnectionSource connectionSource, DatabaseTableConfig<T> tableConfig)
			throws SQLException {
		DatabaseType databaseType = connectionSource.getDatabaseType();
		TableInfo<T> tableInfo = new TableInfo<T>(databaseType, tableConfig);
		logger.info("creating table '{}'", tableInfo.getTableName());
		List<String> statements = new ArrayList<String>();
		List<String> queriesAfter = new ArrayList<String>();
		createTableStatements(databaseType, tableInfo, statements, queriesAfter);
		DatabaseConnection connection = connectionSource.getReadWriteConnection();
		try {
			return doCreateStatements(connection, databaseType, statements, queriesAfter);
		} finally {
			connectionSource.releaseConnection(connection);
		}
	}

	private static int doCreateStatements(DatabaseConnection connection, DatabaseType databaseType,
			List<String> statements, List<String> queriesAfter) throws SQLException {
		int stmtC = 0;
		for (String statement : statements) {
			int rowC;
			CompiledStatement prepStmt = null;
			try {
				prepStmt = connection.compileStatement(statement, StatementType.EXECUTE, noFieldTypes, noFieldTypes);
				rowC = prepStmt.executeUpdate();
				logger.info("executed create table statement changed {} rows: {}", rowC, statement);
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
				prepStmt = connection.compileStatement(query, StatementType.EXECUTE, noFieldTypes, noFieldTypes);
				results = prepStmt.executeQuery();
				int rowC = 0;
				// count the results
				while (results.next()) {
					rowC++;
				}
				logger.info("executing create table after-query got {} results: {}", rowC, query);
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

	private static <T> List<String> doCreateTableStatements(ConnectionSource connectionSource,
			DatabaseTableConfig<T> tableConfig) throws SQLException {
		DatabaseType databaseType = connectionSource.getDatabaseType();
		TableInfo<T> tableInfo = new TableInfo<T>(databaseType, tableConfig);
		List<String> statements = new ArrayList<String>();
		List<String> queriesAfter = new ArrayList<String>();
		createTableStatements(databaseType, tableInfo, statements, queriesAfter);
		return statements;
	}
}
