package com.j256.ormlite.stmt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.j256.ormlite.dao.BaseJdbcDao;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RawResults;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.stmt.mapped.MappedCreate;
import com.j256.ormlite.stmt.mapped.MappedDelete;
import com.j256.ormlite.stmt.mapped.MappedDeleteCollection;
import com.j256.ormlite.stmt.mapped.MappedQueryForId;
import com.j256.ormlite.stmt.mapped.MappedRefresh;
import com.j256.ormlite.stmt.mapped.MappedUpdate;
import com.j256.ormlite.stmt.mapped.MappedUpdateId;
import com.j256.ormlite.support.JdbcTemplate;
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
	private final Class<T> dataClass;
	private final FieldType idField;
	private final MappedQueryForId<T, ID> mappedQueryForId;
	private final PreparedQuery<T> preparedQueryForAll;
	private final MappedCreate<T> mappedInsert;
	private final MappedUpdate<T> mappedUpdate;
	private final MappedUpdateId<T, ID> mappedUpdateId;
	private final MappedDelete<T> mappedDelete;
	private final MappedRefresh<T, ID> mappedRefresh;

	/**
	 * Provides statements for various SQL operations.
	 */
	public StatementExecutor(DatabaseType databaseType, TableInfo<T> tableInfo) throws SQLException {
		this.databaseType = databaseType;
		this.tableInfo = tableInfo;
		this.dataClass = tableInfo.getDataClass();
		this.idField = tableInfo.getIdField();
		this.mappedQueryForId = MappedQueryForId.build(databaseType, tableInfo);
		this.preparedQueryForAll = new QueryBuilder<T, ID>(databaseType, tableInfo).prepareQuery();
		this.mappedInsert = MappedCreate.build(databaseType, tableInfo);
		this.mappedUpdate = MappedUpdate.build(databaseType, tableInfo);
		this.mappedUpdateId = MappedUpdateId.build(databaseType, tableInfo);
		this.mappedDelete = MappedDelete.build(databaseType, tableInfo);
		this.mappedRefresh = MappedRefresh.build(databaseType, tableInfo);
	}

	/**
	 * Return the object associated with the id or null if none. This does a SQL
	 * <tt>select col1,col2,... from ... where ... = id</tt> type query.
	 */
	public T queryForId(JdbcTemplate template, ID id) throws SQLException {
		if (mappedQueryForId == null) {
			throw new SQLException("Cannot query-for-id with " + dataClass + " because it doesn't have an id field");
		}
		return mappedQueryForId.execute(template, id);
	}

	/**
	 * Return the first object that matches the {@link PreparedQuery} or null if none.
	 */
	public T queryForFirst(Connection connection, PreparedQuery<T> preparedQuery) throws SQLException {
		PreparedStatement stmt = null;
		try {
			stmt = preparedQuery.prepareSqlStatement(connection);
			if (!stmt.execute()) {
				throw new SQLException("Could not query for one of " + dataClass + " with warnings: "
						+ stmt.getWarnings());
			}
			ResultSet resultSet = stmt.getResultSet();
			if (resultSet.next()) {
				logger.debug("query-for-first of '{}' returned at least 1 result", preparedQuery.getStatement());
				return preparedQuery.mapRow(resultSet, 0);
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
	public List<T> queryForAll(Connection connection) throws SQLException {
		return query(connection, preparedQueryForAll);
	}

	/**
	 * Return a list of all of the data in the table that matches the {@link PreparedQuery}. Should be used carefully if
	 * the table is large. Consider using the {@link Dao#iterator} if this is the case.
	 */
	public List<T> query(Connection connection, PreparedQuery<T> preparedQuery) throws SQLException {
		SelectIterator<T, ID> iterator = null;
		try {
			iterator = buildIterator(/* no dao specified because no removes */null, connection, preparedQuery);
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
	 * Return a list of all of the data in the table that matches the {@link PreparedQuery}. Should be used carefully if
	 * the table is large. Consider using the {@link Dao#iterator} if this is the case.
	 */
	public RawResults queryRaw(Connection connection, String query) throws SQLException {
		SelectIterator<String[], Void> iterator = null;
		try {
			PreparedStatement preparedStatement = connection.prepareStatement(query);
			RawResultsList results = new RawResultsList(preparedStatement.getMetaData());
			// statement arg is null because we don't want it to double log below
			iterator = new SelectIterator<String[], Void>(String[].class, null, results, preparedStatement, null);
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
	 * Create and return an {@link SelectIterator} for the class with a connection an the default mapped query for all
	 * statement.
	 */
	public SelectIterator<T, ID> buildIterator(BaseJdbcDao<T, ID> classDao, Connection connection) throws SQLException {
		return buildIterator(classDao, connection, preparedQueryForAll);
	}

	/**
	 * Create and return an {@link SelectIterator} for the class with a connection and mapped statement.
	 */
	public SelectIterator<T, ID> buildIterator(BaseJdbcDao<T, ID> classDao, Connection connection,
			PreparedQuery<T> preparedQuery) throws SQLException {
		return new SelectIterator<T, ID>(dataClass, classDao, preparedQuery,
				preparedQuery.prepareSqlStatement(connection), preparedQuery.getStatement());
	}

	/**
	 * Return a RawResults object associated with an internal iterator that matches the query argument.
	 */
	public RawResults buildIterator(Connection connection, String query) throws SQLException {
		return new RawResultsIterator(query, connection.prepareStatement(query));
	}

	/**
	 * Create a new entry in the database from an object.
	 */
	public int create(JdbcTemplate template, T data) throws SQLException {
		return mappedInsert.execute(template, data);
	}

	/**
	 * Update an object in the database.
	 */
	public int update(JdbcTemplate template, T data) throws SQLException {
		if (mappedUpdate == null) {
			throw new SQLException("Cannot update " + dataClass
					+ " because it doesn't have an id field defined or only has id field");
		} else {
			return mappedUpdate.execute(template, data);
		}
	}

	/**
	 * Update an object in the database to change its id to the newId parameter.
	 */
	public int updateId(JdbcTemplate template, T data, ID newId) throws SQLException {
		if (mappedUpdateId == null) {
			throw new SQLException("Cannot update " + dataClass + " because it doesn't have an id field defined");
		} else {
			return mappedUpdateId.execute(template, data, newId);
		}
	}

	/**
	 * Does a query for the object's Id and copies in each of the field values from the database to refresh the data
	 * parameter.
	 */
	public int refresh(JdbcTemplate template, T data) throws SQLException {
		if (mappedQueryForId == null) {
			throw new SQLException("Cannot refresh " + dataClass + " because it doesn't have an id field defined");
		} else {
			T result = mappedRefresh.execute(template, data);
			if (result == null) {
				return 0;
			} else {
				return 1;
			}
		}
	}

	/**
	 * Delete an object from the database.
	 */
	public int delete(JdbcTemplate template, T data) throws SQLException {
		if (mappedDelete == null) {
			throw new SQLException("Cannot delete " + dataClass + " because it doesn't have an id field defined");
		} else {
			return mappedDelete.execute(template, data);
		}
	}

	/**
	 * Delete a collection of objects from the database.
	 */
	public int deleteObjects(JdbcTemplate template, Collection<T> datas) throws SQLException {
		if (idField == null) {
			throw new SQLException("Cannot delete " + dataClass + " because it doesn't have an id field defined");
		} else {
			// have to build this on the fly because the collection has variable number of args
			return MappedDeleteCollection.deleteObjects(databaseType, tableInfo, template, datas);
		}
	}

	/**
	 * Delete a collection of objects from the database.
	 */
	public int deleteIds(JdbcTemplate template, Collection<ID> ids) throws SQLException {
		if (idField == null) {
			throw new SQLException("Cannot delete " + dataClass + " because it doesn't have an id field defined");
		} else {
			// have to build this on the fly because the collection has variable number of args
			return MappedDeleteCollection.deleteIds(databaseType, tableInfo, template, ids);
		}
	}

	/**
	 * Base class for raw results objects. It is also a row mapper to save on another object.
	 */
	private abstract static class BaseRawResults implements RawResults, GenericRowMapper<String[]> {

		protected final int columnN;
		protected final String[] columnNames;

		protected BaseRawResults(ResultSetMetaData metaData) throws SQLException {
			this.columnN = metaData.getColumnCount();
			this.columnNames = new String[this.columnN];
			for (int colC = 0; colC < this.columnN; colC++) {
				this.columnNames[colC] = metaData.getColumnName(colC + 1);
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
		public String[] mapRow(ResultSet rs, int rowNum) throws SQLException {
			String[] result = new String[columnN];
			for (int colC = 0; colC < columnN; colC++) {
				result[colC] = rs.getString(colC + 1);
			}
			return result;
		}
	}

	/**
	 * Raw results from a list of results.
	 */
	private static class RawResultsList extends BaseRawResults {

		private final List<String[]> results = new ArrayList<String[]>();

		public RawResultsList(ResultSetMetaData metaData) throws SQLException {
			super(metaData);
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

		private final PreparedStatement statement;
		private final String query;

		public RawResultsIterator(String query, PreparedStatement statement) throws SQLException {
			super(statement.getMetaData());
			this.query = query;
			this.statement = statement;
		}

		public CloseableIterator<String[]> iterator() {
			try {
				// we do this so we can iterate through the results multiple times
				return new SelectIterator<String[], Void>(String[].class, null, this, statement, query);
			} catch (SQLException e) {
				// we have to do this because iterator can't throw Exceptions
				throw new RuntimeException(e);
			}
		}
	}
}
