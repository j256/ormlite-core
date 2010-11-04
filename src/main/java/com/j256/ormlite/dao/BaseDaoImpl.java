package com.j256.ormlite.dao;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import com.j256.ormlite.db.DatabaseType;
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
	 * @deprecated Use {@link #BaseDaoImpl(ConnectionSource, Class)}
	 */
	@Deprecated
	protected BaseDaoImpl(DatabaseType databaseType, Class<T> dataClass) throws SQLException {
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
	 * @deprecated Use {@link #BaseDaoImpl(DatabaseTableConfig)}
	 */
	@Deprecated
	protected BaseDaoImpl(DatabaseType databaseType, DatabaseTableConfig<T> tableConfig) throws SQLException {
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

		this.databaseType = connectionSource.getDatabaseType();
		if (this.databaseType == null) {
			throw new IllegalStateException("connectionSource is getting a null DatabaseType in "
					+ getClass().getSimpleName());
		}
		if (tableConfig == null) {
			tableConfig = DatabaseTableConfig.fromClass(databaseType, dataClass);
		}
		this.tableInfo = new TableInfo<T>(databaseType, tableConfig);
		this.statementExecutor = new StatementExecutor<T, ID>(databaseType, tableInfo);
		this.initialized = true;
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

	public RawResults queryForAllRaw(String queryString) throws SQLException {
		checkForInitialized();
		return statementExecutor.queryRaw(connectionSource, queryString);
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

	public RawResults iteratorRaw(String query) throws SQLException {
		checkForInitialized();
		try {
			return statementExecutor.buildIterator(connectionSource, query);
		} catch (SQLException e) {
			throw SqlExceptionUtil.create("Could not build iterator for " + query, e);
		}
	}

	public String objectToString(T data) {
		checkForInitialized();
		return tableInfo.objectToString(data);
	}

	public boolean objectsEqual(T data1, T data2) throws SQLException {
		checkForInitialized();
		for (FieldType fieldType : tableInfo.getFieldTypes()) {
			Object fieldObj1 = fieldType.getFieldValue(data1);
			Object fieldObj2 = fieldType.getFieldValue(data2);
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

	/**
	 * Returns the class associated with this DAO.
	 */
	public Class<T> getDataClass() {
		return dataClass;
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
	 * @deprecated Use {@link #createDao(ConnectionSource, Class)}
	 */
	@Deprecated
	public static <T, ID> Dao<T, ID> createDao(DatabaseType databaseType, ConnectionSource connectionSource,
			Class<T> clazz) throws SQLException {
		return createDao(connectionSource, clazz);
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
