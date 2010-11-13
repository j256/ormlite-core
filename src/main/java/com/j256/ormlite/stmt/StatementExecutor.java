package com.j256.ormlite.stmt;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RawResults;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;
import com.j256.ormlite.stmt.mapped.MappedCreate;
import com.j256.ormlite.stmt.mapped.MappedDelete;
import com.j256.ormlite.stmt.mapped.MappedDeleteCollection;
import com.j256.ormlite.stmt.mapped.MappedQueryForId;
import com.j256.ormlite.stmt.mapped.MappedRefresh;
import com.j256.ormlite.stmt.mapped.MappedUpdate;
import com.j256.ormlite.stmt.mapped.MappedUpdateId;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;
import com.j256.ormlite.table.TableInfo;

/**
 * Executes SQL statements for a particular table in a particular database. Basically a call through to various mapped
 * statement methods.
 * 
 * @param T
 *            The class that the code will be operating on.
 * @param ID
 *            The class of the ID column associated with the class. The T class does not require an ID field. The class
 *            needs an ID parameter however so you can use Void or Object to satisfy the compiler.
 * @author graywatson
 */
public class StatementExecutor<T, ID> {

	private static Logger logger = LoggerFactory.getLogger(StatementExecutor.class);

	private final DatabaseType databaseType;
	private final TableInfo<T> tableInfo;
	private MappedQueryForId<T, ID> mappedQueryForId;
	private PreparedQuery<T> preparedQueryForAll;
	private MappedCreate<T, ID> mappedInsert;
	private MappedUpdate<T, ID> mappedUpdate;
	private MappedUpdateId<T, ID> mappedUpdateId;
	private MappedDelete<T, ID> mappedDelete;
	private MappedRefresh<T, ID> mappedRefresh;

	/**
	 * Provides statements for various SQL operations.
	 */
	public StatementExecutor(DatabaseType databaseType, TableInfo<T> tableInfo) throws SQLException {
		this.databaseType = databaseType;
		this.tableInfo = tableInfo;
	}

	/**
	 * Return the object associated with the id or null if none. This does a SQL
	 * <tt>select col1,col2,... from ... where ... = id</tt> type query.
	 */
	public T queryForId(DatabaseConnection databaseConnection, ID id) throws SQLException {
		if (mappedQueryForId == null) {
			mappedQueryForId = MappedQueryForId.build(databaseType, tableInfo);
		}
		return mappedQueryForId.execute(databaseConnection, id);
	}

	/**
	 * Return the first object that matches the {@link PreparedStmt} or null if none.
	 */
	public T queryForFirst(DatabaseConnection databaseConnection, PreparedStmt<T> preparedQuery) throws SQLException {
		CompiledStatement stmt = null;
		try {
			stmt = preparedQuery.compile(databaseConnection);
			DatabaseResults results = stmt.executeQuery();
			if (results.next()) {
				logger.debug("query-for-first of '{}' returned at least 1 result", preparedQuery.getStatement());
				return preparedQuery.mapRow(results);
			} else {
				logger.debug("query-for-first of '{}' returned at 0 results", preparedQuery.getStatement());
				return null;
			}
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	/**
	 * Return a list of all of the data in the table. Should be used carefully if the table is large. Consider using the
	 * {@link Dao#iterator} if this is the case.
	 */
	public List<T> queryForAll(ConnectionSource connectionSource) throws SQLException {
		prepareQueryForAll();
		return query(connectionSource, preparedQueryForAll);
	}

	/**
	 * Return a list of all of the data in the table that matches the {@link PreparedStmt}. Should be used carefully if
	 * the table is large. Consider using the {@link Dao#iterator} if this is the case.
	 */
	public List<T> query(ConnectionSource connectionSource, PreparedStmt<T> preparedQuery) throws SQLException {
		SelectIterator<T, ID> iterator = null;
		try {
			iterator = buildIterator(/* no dao specified because no removes */null, connectionSource, preparedQuery);
			List<T> results = new ArrayList<T>();
			while (iterator.hasNextThrow()) {
				results.add(iterator.nextThrow());
			}
			logger.debug("query of '{}' returned {} results", preparedQuery.getStatement(), results.size());
			return results;
		} finally {
			if (iterator != null) {
				iterator.close();
			}
		}
	}

	/**
	 * Return a list of all of the data in the table that matches the {@link PreparedStmt}. Should be used carefully if
	 * the table is large. Consider using the {@link Dao#iterator} if this is the case.
	 */
	public RawResults queryRaw(ConnectionSource connectionSource, String query) throws SQLException {
		SelectIterator<String[], Void> iterator = null;
		try {
			DatabaseConnection connection = connectionSource.getReadOnlyConnection();
			CompiledStatement compiledStatement = connection.compileStatement(query, StatementType.SELECT);
			RawResultsList results = new RawResultsList(compiledStatement);
			// statement arg is null because we don't want it to double log below
			iterator =
					new SelectIterator<String[], Void>(String[].class, null, results, connectionSource, connection,
							compiledStatement, null);
			while (iterator.hasNextThrow()) {
				results.add(iterator.nextThrow());
			}
			logger.debug("query of '{}' returned {} results", query, results.size());
			return results;
		} finally {
			if (iterator != null) {
				iterator.close();
			}
		}
	}
	/**
	 * Create and return a SelectIterator for the class using the default mapped query for all statement.
	 */
	public SelectIterator<T, ID> buildIterator(BaseDaoImpl<T, ID> classDao, ConnectionSource connectionSource)
			throws SQLException {
		prepareQueryForAll();
		return buildIterator(classDao, connectionSource, preparedQueryForAll);
	}

	/**
	 * Create and return an {@link SelectIterator} for the class using a prepared statement.
	 */
	public SelectIterator<T, ID> buildIterator(BaseDaoImpl<T, ID> classDao, ConnectionSource connectionSource,
			PreparedStmt<T> preparedQuery) throws SQLException {
		DatabaseConnection connection = connectionSource.getReadOnlyConnection();
		return new SelectIterator<T, ID>(tableInfo.getDataClass(), classDao, preparedQuery, connectionSource,
				connection, preparedQuery.compile(connection), preparedQuery.getStatement());
	}

	/**
	 * Return a RawResults object associated with an internal iterator that matches the query argument.
	 */
	public RawResults buildIterator(ConnectionSource connectionSource, String query) throws SQLException {
		DatabaseConnection connection = connectionSource.getReadOnlyConnection();
		return new RawResultsIterator(query, connectionSource, connection, connection.compileStatement(query,
				StatementType.SELECT));
	}

	/**
	 * Create a new entry in the database from an object.
	 */
	public int create(DatabaseConnection databaseConnection, T data) throws SQLException {
		if (mappedInsert == null) {
			mappedInsert = MappedCreate.build(databaseType, tableInfo);
		}
		return mappedInsert.insert(databaseConnection, data);
	}

	/**
	 * Update an object in the database.
	 */
	public int update(DatabaseConnection databaseConnection, T data) throws SQLException {
		if (mappedUpdate == null) {
			mappedUpdate = MappedUpdate.build(databaseType, tableInfo);
		}
		return mappedUpdate.update(databaseConnection, data);
	}

	/**
	 * Update an object in the database to change its id to the newId parameter.
	 */
	public int updateId(DatabaseConnection databaseConnection, T data, ID newId) throws SQLException {
		if (mappedUpdateId == null) {
			mappedUpdateId = MappedUpdateId.build(databaseType, tableInfo);
		}
		return mappedUpdateId.execute(databaseConnection, data, newId);
	}

	/**
	 * Update rows in the database.
	 */
	public int update(DatabaseConnection databaseConnection, PreparedUpdate<T> preparedUpdate) throws SQLException {
		CompiledStatement stmt = preparedUpdate.compile(databaseConnection);
		return stmt.executeUpdate();
	}

	/**
	 * Does a query for the object's Id and copies in each of the field values from the database to refresh the data
	 * parameter.
	 */
	public int refresh(DatabaseConnection databaseConnection, T data) throws SQLException {
		if (mappedRefresh == null) {
			mappedRefresh = MappedRefresh.build(databaseType, tableInfo);
		}
		return mappedRefresh.execute(databaseConnection, data);
	}

	/**
	 * Delete an object from the database.
	 */
	public int delete(DatabaseConnection databaseConnection, T data) throws SQLException {
		if (mappedDelete == null) {
			mappedDelete = MappedDelete.build(databaseType, tableInfo);
		}
		return mappedDelete.delete(databaseConnection, data);

	}

	/**
	 * Delete a collection of objects from the database.
	 */
	public int deleteObjects(DatabaseConnection databaseConnection, Collection<T> datas) throws SQLException {
		// have to build this on the fly because the collection has variable number of args
		return MappedDeleteCollection.deleteObjects(databaseType, tableInfo, databaseConnection, datas);
	}

	/**
	 * Delete a collection of objects from the database.
	 */
	public int deleteIds(DatabaseConnection databaseConnection, Collection<ID> ids) throws SQLException {
		// have to build this on the fly because the collection has variable number of args
		return MappedDeleteCollection.deleteIds(databaseType, tableInfo, databaseConnection, ids);
	}

	/**
	 * Delete rows that match the prepared statement.
	 */
	public int delete(DatabaseConnection databaseConnection, PreparedDelete<T> preparedDelete) throws SQLException {
		CompiledStatement stmt = preparedDelete.compile(databaseConnection);
		return stmt.executeUpdate();
	}

	public <CT> CT callBatchTasks(DatabaseConnection connection, Callable<CT> callable) throws Exception {
		boolean autoCommitAtStart = false;
		try {
			if (connection.isAutoCommitSupported()) {
				autoCommitAtStart = connection.getAutoCommit();
				if (autoCommitAtStart) {
					// disable auto-commit mode if supported and enabled at start
					connection.setAutoCommit(false);
					logger.debug("disabled auto-commit on table {} before batch tasks", tableInfo.getTableName());
				}
			}
			return callable.call();
		} finally {
			if (autoCommitAtStart) {
				// try to restore if we are in auto-commit mode
				connection.setAutoCommit(true);
				logger.debug("re-enabled auto-commit on table {} after batch tasks", tableInfo.getTableName());
			}
		}
	}

	private void prepareQueryForAll() throws SQLException {
		if (preparedQueryForAll == null) {
			preparedQueryForAll = new QueryBuilder<T, ID>(databaseType, tableInfo).prepare();
		}
	}

	/**
	 * Base class for raw results objects. It is also a row mapper to save on another object.
	 */
	private abstract static class BaseRawResults implements RawResults, GenericRowMapper<String[]> {

		protected final int columnN;
		protected final String[] columnNames;

		protected BaseRawResults(CompiledStatement compiledStmt) throws SQLException {
			this.columnN = compiledStmt.getColumnCount();
			this.columnNames = new String[this.columnN];
			for (int colC = 0; colC < this.columnN; colC++) {
				this.columnNames[colC] = compiledStmt.getColumnName(colC + 1);
			}
		}

		public int getNumberColumns() {
			return columnN;
		}

		public String[] getColumnNames() {
			return columnNames;
		}

		/**
		 * Row mapper which handles our String[] raw results.
		 */
		public String[] mapRow(DatabaseResults rs) throws SQLException {
			String[] result = new String[columnN];
			for (int colC = 0; colC < columnN; colC++) {
				result[colC] = rs.getString(colC + 1);
			}
			return result;
		}

		public List<String[]> getResults() throws SQLException {
			List<String[]> results = new ArrayList<String[]>();
			CloseableIterator<String[]> iterator = iterator();
			try {
				while (iterator.hasNext()) {
					results.add(iterator.next());
				}
				return results;
			} finally {
				iterator.close();
			}
		}

		public <T> List<T> getMappedResults(RawRowMapper<T> mapper) throws SQLException {
			List<T> results = new ArrayList<T>();
			RawRowMapperIterator<T> iterator = new RawRowMapperIterator<T>(columnNames, iterator(), mapper);
			try {
				while (iterator.hasNext()) {
					results.add(iterator.next());
				}
				return results;
			} finally {
				iterator.close();
			}
		}

		public <T> CloseableIterator<T> iterator(RawRowMapper<T> mapper) {
			return new RawRowMapperIterator<T>(columnNames, iterator(), mapper);
		}
	}

	/**
	 * Raw results from a list of results.
	 */
	private static class RawResultsList extends BaseRawResults {

		private final List<String[]> results = new ArrayList<String[]>();

		public RawResultsList(CompiledStatement preparedStmt) throws SQLException {
			super(preparedStmt);
		}

		void add(String[] result) throws SQLException {
			results.add(result);
		}

		int size() {
			return results.size();
		}

		public CloseableIterator<String[]> iterator() {
			return new RawResultsListIterator();
		}

		/**
		 * Internal iterator to work on our list.
		 */
		private class RawResultsListIterator implements CloseableIterator<String[]> {

			private int resultC = 0;

			public boolean hasNext() {
				return results.size() > resultC;
			}

			public String[] next() {
				return results.get(resultC++);
			}

			public void remove() {
				// noop
			}

			public void close() {
				// noop
			}
		}
	}

	/**
	 * Raw results from an iterator.
	 */
	private static class RawResultsIterator extends BaseRawResults {

		private final CompiledStatement statement;
		private final String query;
		private final ConnectionSource connectionSource;
		private final DatabaseConnection connection;

		public RawResultsIterator(String query, ConnectionSource connectionSource, DatabaseConnection connection,
				CompiledStatement statement) throws SQLException {
			super(statement);
			this.query = query;
			this.statement = statement;
			this.connectionSource = connectionSource;
			this.connection = connection;
		}

		public CloseableIterator<String[]> iterator() {
			try {
				// we do this so we can iterate through the results multiple times
				return new SelectIterator<String[], Void>(String[].class, null, this, connectionSource, connection,
						statement, query);
			} catch (SQLException e) {
				// we have to do this because iterator can't throw Exceptions
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Internal iterator to work on our list.
	 */
	private static class RawRowMapperIterator<T> implements CloseableIterator<T> {

		private String[] columnNames;
		private CloseableIterator<String[]> stringIterator;
		private RawRowMapper<T> rowMapper;

		public RawRowMapperIterator(String[] columnNames, CloseableIterator<String[]> stringIterator,
				RawRowMapper<T> rowMapper) {
			this.columnNames = columnNames;
			this.stringIterator = stringIterator;
			this.rowMapper = rowMapper;
		}

		public boolean hasNext() {
			return stringIterator.hasNext();
		}

		public T next() {
			String[] stringResult = stringIterator.next();
			return rowMapper.mapRow(columnNames, stringResult);
		}

		public void remove() {
			stringIterator.remove();
		}

		public void close() throws SQLException {
			stringIterator.close();
		}
	}
}
