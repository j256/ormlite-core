package com.j256.ormlite.stmt;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.ObjectCache;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.dao.RawRowObjectMapper;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.misc.SqlExceptionUtil;
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
 * @param <T>
 *            The class that the code will be operating on.
 * @param <ID>
 *            The class of the ID column associated with the class. The T class does not require an ID field. The class
 *            needs an ID parameter however so you can use Void or Object to satisfy the compiler.
 * @author graywatson
 */
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
	private String countStarQuery;
	private String ifExistsQuery;
	private FieldType[] ifExistsFieldTypes;
	private RawRowMapper<T> rawRowMapper;

	/**
	 * Provides statements for various SQL operations.
	 */
	public StatementExecutor(DatabaseType databaseType, TableInfo<T, ID> tableInfo, Dao<T, ID> dao) {
		this.databaseType = databaseType;
		this.tableInfo = tableInfo;
		this.dao = dao;
	}

	/**
	 * Return the object associated with the id or null if none. This does a SQL
	 * <tt>select col1,col2,... from ... where ... = id</tt> type query.
	 */
	public T queryForId(DatabaseConnection databaseConnection, ID id, ObjectCache objectCache) throws SQLException {
		if (mappedQueryForId == null) {
			mappedQueryForId = MappedQueryForId.build(databaseType, tableInfo, null);
		}
		return mappedQueryForId.execute(databaseConnection, id, objectCache);
	}

	/**
	 * Return the first object that matches the {@link PreparedStmt} or null if none.
	 */
	public T queryForFirst(DatabaseConnection databaseConnection, PreparedStmt<T> preparedStmt, ObjectCache objectCache)
			throws SQLException {
		CompiledStatement stmt = preparedStmt.compile(databaseConnection, StatementType.SELECT);
		DatabaseResults results = null;
		try {
			results = stmt.runQuery(objectCache);
			if (results.first()) {
				logger.debug("query-for-first of '{}' returned at least 1 result", preparedStmt.getStatement());
				return preparedStmt.mapRow(results);
			} else {
				logger.debug("query-for-first of '{}' returned at 0 results", preparedStmt.getStatement());
				return null;
			}
		} finally {
			if (results != null) {
				results.close();
			}
			stmt.close();
		}
	}

	/**
	 * Return a list of all of the data in the table. Should be used carefully if the table is large. Consider using the
	 * {@link Dao#iterator} if this is the case.
	 */
	public List<T> queryForAll(ConnectionSource connectionSource, ObjectCache objectCache) throws SQLException {
		prepareQueryForAll();
		return query(connectionSource, preparedQueryForAll, objectCache);
	}

	/**
	 * Return a long value which is the number of rows in the table.
	 */
	public long queryForCountStar(DatabaseConnection databaseConnection) throws SQLException {
		if (countStarQuery == null) {
			StringBuilder sb = new StringBuilder(64);
			sb.append("SELECT COUNT(*) FROM ");
			databaseType.appendEscapedEntityName(sb, tableInfo.getTableName());
			countStarQuery = sb.toString();
		}
		long count = databaseConnection.queryForLong(countStarQuery);
		logger.debug("query of '{}' returned {}", countStarQuery, count);
		return count;
	}

	/**
	 * Return a long value from a prepared query.
	 */
	public long queryForLong(DatabaseConnection databaseConnection, PreparedStmt<T> preparedStmt) throws SQLException {
		CompiledStatement stmt = preparedStmt.compile(databaseConnection, StatementType.SELECT_LONG);
		DatabaseResults results = null;
		try {
			results = stmt.runQuery(null);
			if (results.first()) {
				return results.getLong(0);
			} else {
				throw new SQLException("No result found in queryForLong: " + preparedStmt.getStatement());
			}
		} finally {
			if (results != null) {
				results.close();
			}
			stmt.close();
		}
	}

	/**
	 * Return a long from a raw query with String[] arguments.
	 */
	public long queryForLong(DatabaseConnection databaseConnection, String query, String[] arguments)
			throws SQLException {
		logger.debug("executing raw query for long: {}", query);
		if (arguments.length > 0) {
			// need to do the (Object) cast to force args to be a single object
			logger.trace("query arguments: {}", (Object) arguments);
		}
		CompiledStatement stmt = null;
		DatabaseResults results = null;
		try {
			stmt =
					databaseConnection.compileStatement(query, StatementType.SELECT, noFieldTypes,
							DatabaseConnection.DEFAULT_RESULT_FLAGS);
			assignStatementArguments(stmt, arguments);
			results = stmt.runQuery(null);
			if (results.first()) {
				return results.getLong(0);
			} else {
				throw new SQLException("No result found in queryForLong: " + query);
			}
		} finally {
			if (results != null) {
				results.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	/**
	 * Return a list of all of the data in the table that matches the {@link PreparedStmt}. Should be used carefully if
	 * the table is large. Consider using the {@link Dao#iterator} if this is the case.
	 */
	public List<T> query(ConnectionSource connectionSource, PreparedStmt<T> preparedStmt, ObjectCache objectCache)
			throws SQLException {
		SelectIterator<T, ID> iterator =
				buildIterator(/* no dao specified because no removes */null, connectionSource, preparedStmt, objectCache,
						DatabaseConnection.DEFAULT_RESULT_FLAGS);
		try {
			List<T> results = new ArrayList<T>();
			while (iterator.hasNextThrow()) {
				results.add(iterator.nextThrow());
			}
			logger.debug("query of '{}' returned {} results", preparedStmt.getStatement(), results.size());
			return results;
		} finally {
			iterator.close();
		}
	}

	/**
	 * Create and return a SelectIterator for the class using the default mapped query for all statement.
	 */
	public SelectIterator<T, ID> buildIterator(BaseDaoImpl<T, ID> classDao, ConnectionSource connectionSource,
			int resultFlags, ObjectCache objectCache) throws SQLException {
		prepareQueryForAll();
		return buildIterator(classDao, connectionSource, preparedQueryForAll, objectCache, resultFlags);
	}

	/**
	 * Return a row mapper suitable for mapping 'select *' queries.
	 */
	public GenericRowMapper<T> getSelectStarRowMapper() throws SQLException {
		prepareQueryForAll();
		return preparedQueryForAll;
	}

	/**
	 * Return a raw row mapper suitable for use with {@link Dao#queryRaw(String, RawRowMapper, String...)}.
	 */
	public RawRowMapper<T> getRawRowMapper() {
		if (rawRowMapper == null) {
			rawRowMapper = new RawRowMapperImpl<T, ID>(tableInfo);
		}
		return rawRowMapper;
	}

	/**
	 * Create and return an {@link SelectIterator} for the class using a prepared statement.
	 */
	public SelectIterator<T, ID> buildIterator(BaseDaoImpl<T, ID> classDao, ConnectionSource connectionSource,
			PreparedStmt<T> preparedStmt, ObjectCache objectCache, int resultFlags) throws SQLException {
		DatabaseConnection connection = connectionSource.getReadOnlyConnection();
		CompiledStatement compiledStatement = null;
		try {
			compiledStatement = preparedStmt.compile(connection, StatementType.SELECT, resultFlags);
			SelectIterator<T, ID> iterator =
					new SelectIterator<T, ID>(tableInfo.getDataClass(), classDao, preparedStmt, connectionSource,
							connection, compiledStatement, preparedStmt.getStatement(), objectCache);
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
	 * Return a results object associated with an internal iterator that returns String[] results.
	 */
	public GenericRawResults<String[]> queryRaw(ConnectionSource connectionSource, String query, String[] arguments,
			ObjectCache objectCache) throws SQLException {
		logger.debug("executing raw query for: {}", query);
		if (arguments.length > 0) {
			// need to do the (Object) cast to force args to be a single object
			logger.trace("query arguments: {}", (Object) arguments);
		}
		DatabaseConnection connection = connectionSource.getReadOnlyConnection();
		CompiledStatement compiledStatement = null;
		try {
			compiledStatement =
					connection.compileStatement(query, StatementType.SELECT, noFieldTypes,
							DatabaseConnection.DEFAULT_RESULT_FLAGS);
			assignStatementArguments(compiledStatement, arguments);
			GenericRawResults<String[]> rawResults =
					new RawResultsImpl<String[]>(connectionSource, connection, query, String[].class,
							compiledStatement, this, objectCache);
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
			RawRowMapper<UO> rowMapper, String[] arguments, ObjectCache objectCache) throws SQLException {
		logger.debug("executing raw query for: {}", query);
		if (arguments.length > 0) {
			// need to do the (Object) cast to force args to be a single object
			logger.trace("query arguments: {}", (Object) arguments);
		}
		DatabaseConnection connection = connectionSource.getReadOnlyConnection();
		CompiledStatement compiledStatement = null;
		try {
			compiledStatement =
					connection.compileStatement(query, StatementType.SELECT, noFieldTypes,
							DatabaseConnection.DEFAULT_RESULT_FLAGS);
			assignStatementArguments(compiledStatement, arguments);
			RawResultsImpl<UO> rawResults =
					new RawResultsImpl<UO>(connectionSource, connection, query, String[].class, compiledStatement,
							new UserRawRowMapper<UO>(rowMapper, this), objectCache);
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
	public <UO> GenericRawResults<UO> queryRaw(ConnectionSource connectionSource, String query, DataType[] columnTypes,
			RawRowObjectMapper<UO> rowMapper, String[] arguments, ObjectCache objectCache) throws SQLException {
		logger.debug("executing raw query for: {}", query);
		if (arguments.length > 0) {
			// need to do the (Object) cast to force args to be a single object
			logger.trace("query arguments: {}", (Object) arguments);
		}
		DatabaseConnection connection = connectionSource.getReadOnlyConnection();
		CompiledStatement compiledStatement = null;
		try {
			compiledStatement =
					connection.compileStatement(query, StatementType.SELECT, noFieldTypes,
							DatabaseConnection.DEFAULT_RESULT_FLAGS);
			assignStatementArguments(compiledStatement, arguments);
			RawResultsImpl<UO> rawResults =
					new RawResultsImpl<UO>(connectionSource, connection, query, String[].class, compiledStatement,
							new UserRawRowObjectMapper<UO>(rowMapper, columnTypes), objectCache);
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
			DataType[] columnTypes, String[] arguments, ObjectCache objectCache) throws SQLException {
		logger.debug("executing raw query for: {}", query);
		if (arguments.length > 0) {
			// need to do the (Object) cast to force args to be a single object
			logger.trace("query arguments: {}", (Object) arguments);
		}
		DatabaseConnection connection = connectionSource.getReadOnlyConnection();
		CompiledStatement compiledStatement = null;
		try {
			compiledStatement =
					connection.compileStatement(query, StatementType.SELECT, noFieldTypes,
							DatabaseConnection.DEFAULT_RESULT_FLAGS);
			assignStatementArguments(compiledStatement, arguments);
			RawResultsImpl<Object[]> rawResults =
					new RawResultsImpl<Object[]>(connectionSource, connection, query, Object[].class,
							compiledStatement, new ObjectArrayRowMapper(columnTypes), objectCache);
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
				connection.compileStatement(statement, StatementType.UPDATE, noFieldTypes,
						DatabaseConnection.DEFAULT_RESULT_FLAGS);
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
	public int executeRawNoArgs(DatabaseConnection connection, String statement) throws SQLException {
		logger.debug("running raw execute statement: {}", statement);
		return connection.executeStatement(statement, DatabaseConnection.DEFAULT_RESULT_FLAGS);
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
				connection.compileStatement(statement, StatementType.EXECUTE, noFieldTypes,
						DatabaseConnection.DEFAULT_RESULT_FLAGS);
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
	public int create(DatabaseConnection databaseConnection, T data, ObjectCache objectCache) throws SQLException {
		if (mappedInsert == null) {
			mappedInsert = MappedCreate.build(databaseType, tableInfo);
		}
		return mappedInsert.insert(databaseType, databaseConnection, data, objectCache);
	}

	/**
	 * Update an object in the database.
	 */
	public int update(DatabaseConnection databaseConnection, T data, ObjectCache objectCache) throws SQLException {
		if (mappedUpdate == null) {
			mappedUpdate = MappedUpdate.build(databaseType, tableInfo);
		}
		return mappedUpdate.update(databaseConnection, data, objectCache);
	}

	/**
	 * Update an object in the database to change its id to the newId parameter.
	 */
	public int updateId(DatabaseConnection databaseConnection, T data, ID newId, ObjectCache objectCache)
			throws SQLException {
		if (mappedUpdateId == null) {
			mappedUpdateId = MappedUpdateId.build(databaseType, tableInfo);
		}
		return mappedUpdateId.execute(databaseConnection, data, newId, objectCache);
	}

	/**
	 * Update rows in the database.
	 */
	public int update(DatabaseConnection databaseConnection, PreparedUpdate<T> preparedUpdate) throws SQLException {
		CompiledStatement stmt = preparedUpdate.compile(databaseConnection, StatementType.UPDATE);
		try {
			return stmt.runUpdate();
		} finally {
			stmt.close();
		}
	}

	/**
	 * Does a query for the object's Id and copies in each of the field values from the database to refresh the data
	 * parameter.
	 */
	public int refresh(DatabaseConnection databaseConnection, T data, ObjectCache objectCache) throws SQLException {
		if (mappedRefresh == null) {
			mappedRefresh = MappedRefresh.build(databaseType, tableInfo);
		}
		return mappedRefresh.executeRefresh(databaseConnection, data, objectCache);
	}

	/**
	 * Delete an object from the database.
	 */
	public int delete(DatabaseConnection databaseConnection, T data, ObjectCache objectCache) throws SQLException {
		if (mappedDelete == null) {
			mappedDelete = MappedDelete.build(databaseType, tableInfo);
		}
		return mappedDelete.delete(databaseConnection, data, objectCache);
	}

	/**
	 * Delete an object from the database by id.
	 */
	public int deleteById(DatabaseConnection databaseConnection, ID id, ObjectCache objectCache) throws SQLException {
		if (mappedDelete == null) {
			mappedDelete = MappedDelete.build(databaseType, tableInfo);
		}
		return mappedDelete.deleteById(databaseConnection, id, objectCache);
	}

	/**
	 * Delete a collection of objects from the database.
	 */
	public int deleteObjects(DatabaseConnection databaseConnection, Collection<T> datas, ObjectCache objectCache)
			throws SQLException {
		// have to build this on the fly because the collection has variable number of args
		return MappedDeleteCollection.deleteObjects(databaseType, tableInfo, databaseConnection, datas, objectCache);
	}

	/**
	 * Delete a collection of objects from the database.
	 */
	public int deleteIds(DatabaseConnection databaseConnection, Collection<ID> ids, ObjectCache objectCache)
			throws SQLException {
		// have to build this on the fly because the collection has variable number of args
		return MappedDeleteCollection.deleteIds(databaseType, tableInfo, databaseConnection, ids, objectCache);
	}

	/**
	 * Delete rows that match the prepared statement.
	 */
	public int delete(DatabaseConnection databaseConnection, PreparedDelete<T> preparedDelete) throws SQLException {
		CompiledStatement stmt = preparedDelete.compile(databaseConnection, StatementType.DELETE);
		try {
			return stmt.runUpdate();
		} finally {
			stmt.close();
		}
	}

	/**
	 * Call batch tasks inside of a connection which may, or may not, have been "saved".
	 */
	public <CT> CT callBatchTasks(DatabaseConnection connection, boolean saved, Callable<CT> callable)
			throws SQLException {
		if (databaseType.isBatchUseTransaction()) {
			return TransactionManager.callInTransaction(connection, saved, databaseType, callable);
		}
		boolean autoCommitAtStart = false;
		try {
			if (connection.isAutoCommitSupported()) {
				autoCommitAtStart = connection.isAutoCommit();
				if (autoCommitAtStart) {
					// disable auto-commit mode if supported and enabled at start
					connection.setAutoCommit(false);
					logger.debug("disabled auto-commit on table {} before batch tasks", tableInfo.getTableName());
				}
			}
			try {
				return callable.call();
			} catch (SQLException e) {
				throw e;
			} catch (Exception e) {
				throw SqlExceptionUtil.create("Batch tasks callable threw non-SQL exception", e);
			}
		} finally {
			if (autoCommitAtStart) {
				/**
				 * Try to restore if we are in auto-commit mode.
				 * 
				 * NOTE: we do _not_ have to do a commit here. According to {@link Connection#setAutoCommit(boolean)},
				 * this will start a transaction when auto-commit is turned off and it will be committed here.
				 */
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

	public boolean ifExists(DatabaseConnection connection, ID id) throws SQLException {
		if (ifExistsQuery == null) {
			QueryBuilder<T, ID> qb = new QueryBuilder<T, ID>(databaseType, tableInfo, dao);
			qb.selectRaw("COUNT(*)");
			qb.where().eq(tableInfo.getIdField().getColumnName(), new SelectArg());
			ifExistsQuery = qb.prepareStatementString();
			ifExistsFieldTypes = new FieldType[] { tableInfo.getIdField() };
		}
		long count = connection.queryForLong(ifExistsQuery, new Object[] { id }, ifExistsFieldTypes);
		logger.debug("query of '{}' returned {}", ifExistsQuery, count);
		return (count != 0);
	}

	private void assignStatementArguments(CompiledStatement compiledStatement, String[] arguments) throws SQLException {
		for (int i = 0; i < arguments.length; i++) {
			compiledStatement.setObject(i, arguments[i], SqlType.STRING);
		}
	}

	private void prepareQueryForAll() throws SQLException {
		if (preparedQueryForAll == null) {
			preparedQueryForAll = new QueryBuilder<T, ID>(databaseType, tableInfo, dao).prepare();
		}
	}

	/**
	 * Map raw results to return a user object from a String array.
	 */
	private static class UserRawRowMapper<UO> implements GenericRowMapper<UO> {

		private final RawRowMapper<UO> mapper;
		private final GenericRowMapper<String[]> stringRowMapper;
		private String[] columnNames;

		public UserRawRowMapper(RawRowMapper<UO> mapper, GenericRowMapper<String[]> stringMapper) {
			this.mapper = mapper;
			this.stringRowMapper = stringMapper;
		}

		public UO mapRow(DatabaseResults results) throws SQLException {
			String[] stringResults = stringRowMapper.mapRow(results);
			return mapper.mapRow(getColumnNames(results), stringResults);
		}

		private String[] getColumnNames(DatabaseResults results) throws SQLException {
			if (columnNames != null) {
				return columnNames;
			}
			columnNames = results.getColumnNames();
			return columnNames;
		}
	}

	/**
	 * Map raw results to return a user object from an Object array.
	 */
	private static class UserRawRowObjectMapper<UO> implements GenericRowMapper<UO> {

		private final RawRowObjectMapper<UO> mapper;
		private final DataType[] columnTypes;
		private String[] columnNames;

		public UserRawRowObjectMapper(RawRowObjectMapper<UO> mapper, DataType[] columnTypes) {
			this.mapper = mapper;
			this.columnTypes = columnTypes;
		}

		public UO mapRow(DatabaseResults results) throws SQLException {
			int columnN = results.getColumnCount();
			Object[] objectResults = new Object[columnN];
			for (int colC = 0; colC < columnN; colC++) {
				if (colC >= columnTypes.length) {
					objectResults[colC] = null;
				} else {
					objectResults[colC] = columnTypes[colC].getDataPersister().resultToJava(null, results, colC);
				}
			}
			return mapper.mapRow(getColumnNames(results), columnTypes, objectResults);
		}

		private String[] getColumnNames(DatabaseResults results) throws SQLException {
			if (columnNames != null) {
				return columnNames;
			}
			columnNames = results.getColumnNames();
			return columnNames;
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
				DataType dataType;
				if (colC >= columnTypes.length) {
					dataType = DataType.STRING;
				} else {
					dataType = columnTypes[colC];
				}
				result[colC] = dataType.getDataPersister().resultToJava(null, results, colC);
			}
			return result;
		}
	}
}
