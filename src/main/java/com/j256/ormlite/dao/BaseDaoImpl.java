package com.j256.ormlite.dao;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.PreparedUpdate;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectIterator;
import com.j256.ormlite.stmt.StatementExecutor;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableInfo;

/**
 * Base class for the Database Access Objects that handle the reading and writing a class from the database.
 * 
 * <p>
 * This class is also {@link Iterable} which means you can do a {@code for (T obj : dao)} type of loop code to iterate
 * through the table of persisted objects. See {@link #iterator()}.
 * </p>
 * 
 * <p>
 * <b> NOTE: </b> If you are using the Spring type wiring, {@link #initialize} should be called after all of the set
 * methods. In Spring XML, init-method="initialize" should be used.
 * </p>
 * 
 * @param T
 *            The class that the code will be operating on.
 * @param ID
 *            The class of the ID column associated with the class. The T class does not require an ID field. The class
 *            needs an ID parameter however so you can use Void or Object to satisfy the compiler.
 * @author graywatson
 */
public abstract class BaseDaoImpl<T, ID> implements Dao<T, ID> {

	private DatabaseType databaseType;
	private ConnectionSource connectionSource;

	private final Class<T> dataClass;
	private DatabaseTableConfig<T> tableConfig;
	private TableInfo<T> tableInfo;
	private StatementExecutor<T, ID> statementExecutor;
	private boolean initialized = false;

	/**
	 * Construct our base DAO using Spring type wiring. The {@link ConnectionSource} must be set with the
	 * {@link #setConnectionSource} method afterwards and then the {@link #initialize()} method must be called. The
	 * dataClass provided must have its fields marked with {@link DatabaseField} annotations or the
	 * {@link #setTableConfig} method must be called before the {@link #initialize()} method is called.
	 * 
	 * <p>
	 * If you are using Spring then your should use: init-method="initialize"
	 * </p>
	 * 
	 * @param dataClass
	 *            Class associated with this Dao. This must match the T class parameter.
	 */
	protected BaseDaoImpl(Class<T> dataClass) throws SQLException {
		this(null, dataClass, null);
	}

	/**
	 * Construct our base DAO class. The dataClass provided must have its fields marked with {@link DatabaseField} or
	 * javax.persistance annotations.
	 * 
	 * @param connectionSource
	 *            Source of our database connections.
	 * @param dataClass
	 *            Class associated with this Dao. This must match the T class parameter.
	 */
	protected BaseDaoImpl(ConnectionSource connectionSource, Class<T> dataClass) throws SQLException {
		this(connectionSource, dataClass, null);
	}

	/**
	 * Construct our base DAO class for Spring type wiring. The {@link ConnectionSource} must be set with the
	 * {@link #setConnectionSource} method and then the {@link #initialize()} method must be called.
	 * 
	 * @param tableConfig
	 *            Hand or spring wired table configuration information.
	 */
	protected BaseDaoImpl(DatabaseTableConfig<T> tableConfig) throws SQLException {
		this(null, tableConfig.getDataClass(), tableConfig);
	}

	/**
	 * Construct our base DAO class.
	 * 
	 * @param connectionSource
	 *            Source of our database connections.
	 * @param tableConfig
	 *            Hand or Spring wired table configuration information.
	 */
	protected BaseDaoImpl(ConnectionSource connectionSource, DatabaseTableConfig<T> tableConfig) throws SQLException {
		this(connectionSource, tableConfig.getDataClass(), tableConfig);
	}

	private BaseDaoImpl(ConnectionSource connectionSource, Class<T> dataClass, DatabaseTableConfig<T> tableConfig)
			throws SQLException {
		this.dataClass = dataClass;
		this.tableConfig = tableConfig;
		if (connectionSource != null) {
			this.connectionSource = connectionSource;
			initialize();
		}
	}

	/**
	 * Initialize the various DAO configurations after the various setters have been called.
	 */
	public void initialize() throws SQLException {
		if (initialized) {
			// just skip it if already initialized
			return;
		}
		if (connectionSource == null) {
			throw new IllegalStateException("connectionSource was never set on " + getClass().getSimpleName());
		}

		databaseType = connectionSource.getDatabaseType();
		if (databaseType == null) {
			throw new IllegalStateException("connectionSource is getting a null DatabaseType in "
					+ getClass().getSimpleName());
		}
		if (tableConfig == null) {
			tableInfo = new TableInfo<T>(connectionSource, dataClass);
		} else {
			tableConfig.extractFieldTypes(connectionSource);
			tableInfo = new TableInfo<T>(databaseType, tableConfig);
		}
		statementExecutor = new StatementExecutor<T, ID>(databaseType, tableInfo);
		initialized = true;
	}

	public T queryForId(ID id) throws SQLException {
		checkForInitialized();
		DatabaseConnection connection = connectionSource.getReadOnlyConnection();
		try {
			return statementExecutor.queryForId(connection, id);
		} finally {
			connectionSource.releaseConnection(connection);
		}
	}

	public T queryForFirst(PreparedQuery<T> preparedQuery) throws SQLException {
		checkForInitialized();
		DatabaseConnection connection = connectionSource.getReadOnlyConnection();
		try {
			return statementExecutor.queryForFirst(connection, preparedQuery);
		} finally {
			connectionSource.releaseConnection(connection);
		}
	}

	public List<T> queryForAll() throws SQLException {
		checkForInitialized();
		return statementExecutor.queryForAll(connectionSource);
	}

	public QueryBuilder<T, ID> queryBuilder() {
		checkForInitialized();
		return new QueryBuilder<T, ID>(databaseType, tableInfo);
	}

	public UpdateBuilder<T, ID> updateBuilder() {
		checkForInitialized();
		return new UpdateBuilder<T, ID>(databaseType, tableInfo);
	}

	public DeleteBuilder<T, ID> deleteBuilder() {
		checkForInitialized();
		return new DeleteBuilder<T, ID>(databaseType, tableInfo);
	}

	public List<T> query(PreparedQuery<T> preparedQuery) throws SQLException {
		checkForInitialized();
		return statementExecutor.query(connectionSource, preparedQuery);
	}

	@SuppressWarnings("deprecation")
	public RawResults queryForAllRaw(String queryString) throws SQLException {
		checkForInitialized();
		return statementExecutor.queryForAllRawOld(connectionSource, queryString);
	}

	public int create(T data) throws SQLException {
		checkForInitialized();
		// ignore creating a null object
		if (data == null) {
			return 0;
		} else {
			DatabaseConnection connection = connectionSource.getReadWriteConnection();
			try {
				return statementExecutor.create(connection, data);
			} finally {
				connectionSource.releaseConnection(connection);
			}
		}
	}

	public int update(T data) throws SQLException {
		checkForInitialized();
		// ignore updating a null object
		if (data == null) {
			return 0;
		} else {
			DatabaseConnection connection = connectionSource.getReadWriteConnection();
			try {
				return statementExecutor.update(connection, data);
			} finally {
				connectionSource.releaseConnection(connection);
			}
		}
	}

	public int updateId(T data, ID newId) throws SQLException {
		checkForInitialized();
		// ignore updating a null object
		if (data == null) {
			return 0;
		} else {
			DatabaseConnection connection = connectionSource.getReadWriteConnection();
			try {
				return statementExecutor.updateId(connection, data, newId);
			} finally {
				connectionSource.releaseConnection(connection);
			}
		}
	}

	public int update(PreparedUpdate<T> preparedUpdate) throws SQLException {
		checkForInitialized();
		DatabaseConnection connection = connectionSource.getReadWriteConnection();
		try {
			return statementExecutor.update(connection, preparedUpdate);
		} finally {
			connectionSource.releaseConnection(connection);
		}
	}

	public int refresh(T data) throws SQLException {
		checkForInitialized();
		// ignore refreshing a null object
		if (data == null) {
			return 0;
		} else {
			DatabaseConnection connection = connectionSource.getReadOnlyConnection();
			try {
				return statementExecutor.refresh(connection, data);
			} finally {
				connectionSource.releaseConnection(connection);
			}
		}
	}

	public int delete(T data) throws SQLException {
		checkForInitialized();
		// ignore deleting a null object
		if (data == null) {
			return 0;
		} else {
			DatabaseConnection connection = connectionSource.getReadWriteConnection();
			try {
				return statementExecutor.delete(connection, data);
			} finally {
				connectionSource.releaseConnection(connection);
			}
		}
	}
	public int delete(Collection<T> datas) throws SQLException {
		checkForInitialized();
		// ignore deleting a null object
		if (datas == null || datas.size() == 0) {
			return 0;
		} else {
			DatabaseConnection connection = connectionSource.getReadWriteConnection();
			try {
				return statementExecutor.deleteObjects(connection, datas);
			} finally {
				connectionSource.releaseConnection(connection);
			}
		}
	}

	public int deleteIds(Collection<ID> ids) throws SQLException {
		checkForInitialized();
		// ignore deleting a null object
		if (ids == null || ids.size() == 0) {
			return 0;
		} else {
			DatabaseConnection connection = connectionSource.getReadWriteConnection();
			try {
				return statementExecutor.deleteIds(connection, ids);
			} finally {
				connectionSource.releaseConnection(connection);
			}
		}
	}

	public int delete(PreparedDelete<T> preparedDelete) throws SQLException {
		checkForInitialized();
		DatabaseConnection connection = connectionSource.getReadWriteConnection();
		try {
			return statementExecutor.delete(connection, preparedDelete);
		} finally {
			connectionSource.releaseConnection(connection);
		}
	}

	public SelectIterator<T, ID> iterator() {
		checkForInitialized();
		try {
			return statementExecutor.buildIterator(this, connectionSource);
		} catch (Exception e) {
			throw new IllegalStateException("Could not build iterator for " + dataClass, e);
		}
	}

	public SelectIterator<T, ID> iterator(PreparedQuery<T> preparedQuery) throws SQLException {
		checkForInitialized();
		try {
			return statementExecutor.buildIterator(this, connectionSource, preparedQuery);
		} catch (SQLException e) {
			throw SqlExceptionUtil.create("Could not build iterator for " + dataClass, e);
		}
	}

	/*
	 * When this gets removed, I should rename the GenericRawResults one to this name and deprecate it.
	 */
	@SuppressWarnings("deprecation")
	public RawResults iteratorRaw(String query) throws SQLException {
		checkForInitialized();
		try {
			return statementExecutor.buildOldIterator(connectionSource, query);
		} catch (SQLException e) {
			throw SqlExceptionUtil.create("Could not build iterator for " + query, e);
		}
	}

	public GenericRawResults<String[]> queryRaw(String query) throws SQLException {
		checkForInitialized();
		try {
			return statementExecutor.queryRaw(connectionSource, query);
		} catch (SQLException e) {
			throw SqlExceptionUtil.create("Could not build iterator for " + query, e);
		}
	}

	public <GR> GenericRawResults<GR> queryRaw(String query, RawRowMapper<GR> mapper) throws SQLException {
		checkForInitialized();
		try {
			return statementExecutor.queryRaw(connectionSource, query, mapper);
		} catch (SQLException e) {
			throw SqlExceptionUtil.create("Could not build iterator for " + query, e);
		}
	}

	public GenericRawResults<Object[]> queryRaw(String query, DataType[] columnTypes) throws SQLException {
		checkForInitialized();
		try {
			return statementExecutor.queryRaw(connectionSource, query, columnTypes);
		} catch (SQLException e) {
			throw SqlExceptionUtil.create("Could not build iterator for " + query, e);
		}
	}

	public int executeRaw(String statement) throws SQLException {
		checkForInitialized();
		try {
			return statementExecutor.executeRaw(connectionSource, statement);
		} catch (SQLException e) {
			throw SqlExceptionUtil.create("Could not run raw execute statement " + statement, e);
		}
	}

	public int updateRaw(String statement) throws SQLException {
		checkForInitialized();
		try {
			return statementExecutor.updateRaw(connectionSource, statement);
		} catch (SQLException e) {
			throw SqlExceptionUtil.create("Could not run raw update statement " + statement, e);
		}
	}

	public <CT> CT callBatchTasks(Callable<CT> callable) throws Exception {
		checkForInitialized();
		DatabaseConnection connection = connectionSource.getReadWriteConnection();
		try {
			return statementExecutor.callBatchTasks(connection, callable);
		} finally {
			connectionSource.releaseConnection(connection);
		}
	}

	public String objectToString(T data) {
		checkForInitialized();
		return tableInfo.objectToString(data);
	}

	public boolean objectsEqual(T data1, T data2) throws SQLException {
		checkForInitialized();
		for (FieldType fieldType : tableInfo.getFieldTypes()) {
			Object fieldObj1 = fieldType.extractJavaFieldValue(data1);
			Object fieldObj2 = fieldType.extractJavaFieldValue(data2);
			if (fieldObj1 == null) {
				if (fieldObj2 != null) {
					return false;
				}
			} else if (fieldObj2 == null) {
				return false;
			} else if (!fieldObj1.equals(fieldObj2)) {
				return false;
			}
		}
		return true;
	}

	public ID extractId(T data) throws SQLException {
		checkForInitialized();
		FieldType idField = tableInfo.getIdField();
		return idField.extractJavaFieldValue(data);
	}

	public Class<T> getDataClass() {
		return dataClass;
	}

	public FieldType findForeignFieldType(Class<?> clazz) {
		checkForInitialized();
		for (FieldType fieldType : tableInfo.getFieldTypes()) {
			if (fieldType.getFieldType() == clazz) {
				return fieldType;
			}
		}
		return null;
	}

	/**
	 * Returns the table configuration information associated with the Dao's class.
	 */
	public DatabaseTableConfig<T> getTableConfig() {
		return tableConfig;
	}

	public void setConnectionSource(ConnectionSource connectionSource) {
		this.connectionSource = connectionSource;
	}

	/**
	 * Used if you want to configure the class for the Dao by hand or with spring instead of using the
	 * {@link DatabaseField} annotation in the class. This must be called <i>before</i> {@link #initialize}.
	 */
	public void setTableConfig(DatabaseTableConfig<T> tableConfig) {
		this.tableConfig = tableConfig;
	}

	/**
	 * Helper method to create a Dao object without having to define a class. Dao classes are supposed to be convenient
	 * but if you have a lot of classes, they can seem to be a pain.
	 */
	public static <T, ID> Dao<T, ID> createDao(ConnectionSource connectionSource, Class<T> clazz) throws SQLException {
		return new BaseDaoImpl<T, ID>(connectionSource, clazz) {
		};
	}

	private void checkForInitialized() {
		if (!initialized) {
			throw new IllegalStateException("you must call initialize() before you can use the dao");
		}
	}
}
