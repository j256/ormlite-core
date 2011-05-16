package com.j256.ormlite.stmt;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RawResults;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.misc.TransactionManager;
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
@SuppressWarnings("deprecation")
public class StatementExecutor<T, ID> implements GenericRowMapper<String[]> {

	private static Logger logger = LoggerFactory.getLogger(StatementExecutor.class);
	private static final FieldType[] noFieldTypes = new FieldType[0];

	private final DatabaseType databaseType;
	private final TableInfo<T, ID> tableInfo;
	private final Dao<T, ID> dao;
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
	public StatementExecutor(DatabaseType databaseType, TableInfo<T, ID> tableInfo, Dao<T, ID> dao) throws SQLException {
		this.databaseType = databaseType;
		this.tableInfo = tableInfo;
		this.dao = dao;
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
	public T queryForFirst(DatabaseConnection databaseConnection, PreparedStmt<T> preparedStmt) throws SQLException {
		CompiledStatement stmt = preparedStmt.compile(databaseConnection);
		try {
			DatabaseResults results = stmt.runQuery();
			if (results.next()) {
				logger.debug("query-for-first of '{}' returned at least 1 result", preparedStmt.getStatement());
				return preparedStmt.mapRow(results);
			} else {
				logger.debug("query-for-first of '{}' returned at 0 results", preparedStmt.getStatement());
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
	 * Return a long value which is the number of rows in the table.
	 */
	public long queryForCountStar(DatabaseConnection databaseConnection) throws SQLException {
		StringBuilder sb = new StringBuilder(64);
		sb.append("SELECT COUNT(*) FROM ");
		databaseType.appendEscapedEntityName(sb, tableInfo.getTableName());
		String statement = sb.toString();
		long count = databaseConnection.queryForLong(statement);
		logger.debug("query of '{}' returned {}", statement, count);
		return count;
	}

	/**
	 * Return a list of all of the data in the table that matches the {@link PreparedStmt}. Should be used carefully if
	 * the table is large. Consider using the {@link Dao#iterator} if this is the case.
	 */
	public List<T> query(ConnectionSource connectionSource, PreparedStmt<T> preparedStmt) throws SQLException {
		SelectIterator<T, ID> iterator = null;
		try {
			iterator = buildIterator(/* no dao specified because no removes */null, connectionSource, preparedStmt);
			List<T> results = new ArrayList<T>();
			while (iterator.hasNextThrow()) {
				results.add(iterator.nextThrow());
			}
			logger.debug("query of '{}' returned {} results", preparedStmt.getStatement(), results.size());
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
	public RawResults queryForAllRawOld(ConnectionSource connectionSource, String query) throws SQLException {
		DatabaseConnection connection = connectionSource.getReadOnlyConnection();
		CompiledStatement compiledStatement = null;
		try {
			compiledStatement = connection.compileStatement(query, StatementType.SELECT, noFieldTypes);
			String[] columnNames = extractColumnNames(compiledStatement);
			RawResultsWrapper rawResults =
					new RawResultsWrapper(connectionSource, connection, query, compiledStatement, columnNames, this);
			compiledStatement = null;
			connection = null;
			return rawResults;
		} finally {
			if (compiledStatement != null) {
				compiledStatement.close();
			}
			if (connection != null) {
				connectionSource.releaseConnection(connection);
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
			PreparedStmt<T> preparedStmt) throws SQLException {
		DatabaseConnection connection = connectionSource.getReadOnlyConnection();
		CompiledStatement compiledStatement = null;
		try {
			compiledStatement = preparedStmt.compile(connection);
			SelectIterator<T, ID> iterator =
					new SelectIterator<T, ID>(tableInfo.getDataClass(), classDao, preparedStmt, connectionSource,
							connection, compiledStatement, preparedStmt.getStatement());
			connection = null;
			compiledStatement = null;
			return iterator;
		} finally {
			if (compiledStatement != null) {
				compiledStatement.close();
			}
			if (connection != null) {
				connectionSource.releaseConnection(connection);
			}
		}
	}

	/**
	 * Return on old RawResults object associated with an internal iterator that matches the query argument.
	 */
	public RawResults buildOldIterator(ConnectionSource connectionSource, String query) throws SQLException {
		logger.debug("executing raw results iterator for: {}", query);
		DatabaseConnection connection = connectionSource.getReadOnlyConnection();
		CompiledStatement compiledStatement = null;
		try {
			compiledStatement = connection.compileStatement(query, StatementType.SELECT, noFieldTypes);
			String[] columnNames = extractColumnNames(compiledStatement);
			RawResultsWrapper rawResults =
					new RawResultsWrapper(connectionSource, connection, query, compiledStatement, columnNames, this);
			compiledStatement = null;
			connection = null;
			return rawResults;
		} finally {
			if (compiledStatement != null) {
				compiledStatement.close();
			}
			if (connection != null) {
				connectionSource.releaseConnection(connection);
			}
		}
	}

	/**
	 * Return a results object associated with an internal iterator that returns String[] results.
	 */
	public GenericRawResults<String[]> queryRaw(ConnectionSource connectionSource, String query, String[] arguments)
			throws SQLException {
		logger.debug("executing raw query for: {}", query);
		if (arguments.length > 0) {
			// need to do the (Object) cast to force args to be a single object
			logger.trace("query arguments: {}", (Object) arguments);
		}
		DatabaseConnection connection = connectionSource.getReadOnlyConnection();
		CompiledStatement compiledStatement = null;
		try {
			compiledStatement = connection.compileStatement(query, StatementType.SELECT, noFieldTypes);
			assignStatementArguments(compiledStatement, arguments);
			String[] columnNames = extractColumnNames(compiledStatement);
			GenericRawResults<String[]> rawResults =
					new RawResultsImpl<String[]>(connectionSource, connection, query, String[].class,
							compiledStatement, columnNames, this);
			compiledStatement = null;
			connection = null;
			return rawResults;
		} finally {
			if (compiledStatement != null) {
				compiledStatement.close();
			}
			if (connection != null) {
				connectionSource.releaseConnection(connection);
			}
		}
	}

	/**
	 * Return a results object associated with an internal iterator is mapped by the user's rowMapper.
	 */
	public <UO> GenericRawResults<UO> queryRaw(ConnectionSource connectionSource, String query,
			RawRowMapper<UO> rowMapper, String[] arguments) throws SQLException {
		logger.debug("executing raw query for: {}", query);
		if (arguments.length > 0) {
			// need to do the (Object) cast to force args to be a single object
			logger.trace("query arguments: {}", (Object) arguments);
		}
		DatabaseConnection connection = connectionSource.getReadOnlyConnection();
		CompiledStatement compiledStatement = null;
		try {
			compiledStatement = connection.compileStatement(query, StatementType.SELECT, noFieldTypes);
			assignStatementArguments(compiledStatement, arguments);
			String[] columnNames = extractColumnNames(compiledStatement);
			RawResultsImpl<UO> rawResults =
					new RawResultsImpl<UO>(connectionSource, connection, query, String[].class, compiledStatement,
							columnNames, new UserObjectRowMapper<UO>(rowMapper, columnNames, this));
			compiledStatement = null;
			connection = null;
			return rawResults;
		} finally {
			if (compiledStatement != null) {
				compiledStatement.close();
			}
			if (connection != null) {
				connectionSource.releaseConnection(connection);
			}
		}
	}

	/**
	 * Return a results object associated with an internal iterator that returns Object[] results.
	 */
	public GenericRawResults<Object[]> queryRaw(ConnectionSource connectionSource, String query,
			DataType[] columnTypes, String[] arguments) throws SQLException {
		logger.debug("executing raw query for: {}", query);
		if (arguments.length > 0) {
			// need to do the (Object) cast to force args to be a single object
			logger.trace("query arguments: {}", (Object) arguments);
		}
		DatabaseConnection connection = connectionSource.getReadOnlyConnection();
		CompiledStatement compiledStatement = null;
		try {
			compiledStatement = connection.compileStatement(query, StatementType.SELECT, noFieldTypes);
			assignStatementArguments(compiledStatement, arguments);
			String[] columnNames = extractColumnNames(compiledStatement);
			RawResultsImpl<Object[]> rawResults =
					new RawResultsImpl<Object[]>(connectionSource, connection, query, Object[].class,
							compiledStatement, columnNames, new ObjectArrayRowMapper(columnTypes));
			compiledStatement = null;
			connection = null;
			return rawResults;
		} finally {
			if (compiledStatement != null) {
				compiledStatement.close();
			}
			if (connection != null) {
				connectionSource.releaseConnection(connection);
			}
		}
	}

	/**
	 * Return the number of rows affected.
	 */
	public int updateRaw(DatabaseConnection connection, String statement, String[] arguments) throws SQLException {
		logger.debug("running raw update statement: {}", statement);
		if (arguments.length > 0) {
			// need to do the (Object) cast to force args to be a single object
			logger.trace("update arguments: {}", (Object) arguments);
		}
		CompiledStatement compiledStatement =
				connection.compileStatement(statement, StatementType.UPDATE, noFieldTypes);
		try {
			assignStatementArguments(compiledStatement, arguments);
			return compiledStatement.runUpdate();
		} finally {
			compiledStatement.close();
		}
	}

	/**
	 * Return true if it worked else false.
	 */
	public int executeRaw(DatabaseConnection connection, String statement, String[] arguments) throws SQLException {
		logger.debug("running raw execute statement: {}", statement);
		if (arguments.length > 0) {
			// need to do the (Object) cast to force args to be a single object
			logger.trace("execute arguments: {}", (Object) arguments);
		}
		CompiledStatement compiledStatement =
				connection.compileStatement(statement, StatementType.EXECUTE, noFieldTypes);
		try {
			assignStatementArguments(compiledStatement, arguments);
			return compiledStatement.runExecute();
		} finally {
			compiledStatement.close();
		}
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
		try {
			return stmt.runUpdate();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	/**
	 * Does a query for the object's Id and copies in each of the field values from the database to refresh the data
	 * parameter.
	 */
	public int refresh(DatabaseConnection databaseConnection, T data) throws SQLException {
		if (mappedRefresh == null) {
			mappedRefresh = MappedRefresh.build(databaseType, tableInfo);
		}
		return mappedRefresh.executeRefresh(databaseConnection, data);
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
		try {
			return stmt.runUpdate();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	/**
	 * Call batch tasks insude of a connection which may, or may not, have been "saved".
	 */
	public <CT> CT callBatchTasks(DatabaseConnection connection, boolean saved, Callable<CT> callable) throws Exception {
		if (databaseType.isBatchUseTransaction()) {
			return TransactionManager.callInTransaction(connection, saved, databaseType, callable);
		}
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

	public String[] mapRow(DatabaseResults results) throws SQLException {
		int columnN = results.getColumnCount();
		String[] result = new String[columnN];
		for (int colC = 0; colC < columnN; colC++) {
			result[colC] = results.getString(colC);
		}
		return result;
	}

	private void assignStatementArguments(CompiledStatement compiledStatement, String[] arguments) throws SQLException {
		for (int i = 0; i < arguments.length; i++) {
			if (arguments[i] == null) {
				compiledStatement.setNull(i, SqlType.STRING);
			} else {
				compiledStatement.setObject(i, arguments[i], SqlType.STRING);
			}
		}
	}

	private String[] extractColumnNames(CompiledStatement compiledStatement) throws SQLException {
		int colN = compiledStatement.getColumnCount();
		String[] columnNames = new String[colN];
		for (int colC = 0; colC < colN; colC++) {
			columnNames[colC] = compiledStatement.getColumnName(colC);
		}
		return columnNames;
	}

	private void prepareQueryForAll() throws SQLException {
		if (preparedQueryForAll == null) {
			preparedQueryForAll = new QueryBuilder<T, ID>(databaseType, tableInfo, dao).prepare();
		}
	}

	/**
	 * Map raw results to return a user object;
	 */
	private static class UserObjectRowMapper<UO> implements GenericRowMapper<UO> {

		private final RawRowMapper<UO> mapper;
		private final String[] columnNames;
		private final GenericRowMapper<String[]> stringRowMapper;

		public UserObjectRowMapper(RawRowMapper<UO> mapper, String[] columnNames,
				GenericRowMapper<String[]> stringMapper) {
			this.mapper = mapper;
			this.columnNames = columnNames;
			this.stringRowMapper = stringMapper;
		}

		public UO mapRow(DatabaseResults results) throws SQLException {
			String[] stringResults = stringRowMapper.mapRow(results);
			return mapper.mapRow(columnNames, stringResults);
		}
	}

	/**
	 * Map raw results to return Object[].
	 */
	private static class ObjectArrayRowMapper implements GenericRowMapper<Object[]> {

		private final DataType[] columnTypes;

		public ObjectArrayRowMapper(DataType[] columnTypes) {
			this.columnTypes = columnTypes;
		}

		public Object[] mapRow(DatabaseResults results) throws SQLException {
			int columnN = results.getColumnCount();
			Object[] result = new Object[columnN];
			for (int colC = 0; colC < columnN; colC++) {
				if (colC >= columnTypes.length) {
					result[colC] = DataType.STRING.resultToJava(null, results, colC);
				} else {
					result[colC] = columnTypes[colC].resultToJava(null, results, colC);
				}
			}
			return result;
		}
	}

	/**
	 * To be removed once the deprecated RawResults goes away.
	 */
	private static class RawResultsWrapper implements RawResults {

		private final GenericRawResults<String[]> rawResults;
		private final DatabaseConnection connection;
		private final ConnectionSource connectionSource;
		private final String query;
		private final CompiledStatement compiledStatement;
		private final String[] columnNames;
		private final GenericRowMapper<String[]> stringMapper;

		public RawResultsWrapper(ConnectionSource connectionSource, DatabaseConnection connection, String query,
				CompiledStatement compiledStatement, String[] columnNames, GenericRowMapper<String[]> stringMapper)
				throws SQLException {
			this.connectionSource = connectionSource;
			this.connection = connection;
			this.query = query;
			this.compiledStatement = compiledStatement;
			this.columnNames = columnNames;
			this.stringMapper = stringMapper;
			this.rawResults =
					new RawResultsImpl<String[]>(connectionSource, connection, query, String[].class,
							compiledStatement, columnNames, stringMapper);
		}

		public int getNumberColumns() {
			return rawResults.getNumberColumns();
		}

		public String[] getColumnNames() {
			return rawResults.getColumnNames();
		}

		public CloseableIterator<String[]> iterator() {
			return rawResults.iterator();
		}

		public List<String[]> getResults() throws SQLException {
			return rawResults.getResults();
		}

		public void close() throws SQLException {
			rawResults.close();
		}

		public <UO> List<UO> getMappedResults(RawRowMapper<UO> rowMapper) throws SQLException {
			List<UO> results = new ArrayList<UO>();
			String[] columnNames = rawResults.getColumnNames();
			for (String[] strings : this) {
				UO result = rowMapper.mapRow(columnNames, strings);
				if (result != null) {
					results.add(result);
				}
			}
			return results;
		}

		public <UO> CloseableIterator<UO> iterator(RawRowMapper<UO> rowMapper) throws SQLException {
			return new RawResultsImpl<UO>(connectionSource, connection, query, String[].class, compiledStatement,
					columnNames, new UserObjectRowMapper<UO>(rowMapper, columnNames, stringMapper)).iterator();
		}
	}
}
