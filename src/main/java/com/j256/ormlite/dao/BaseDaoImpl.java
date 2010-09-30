package com.j256.ormlite.dao;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.stmt.PreparedStmt;
import com.j256.ormlite.stmt.SelectIterator;
import com.j256.ormlite.stmt.StatementBuilder;
import com.j256.ormlite.stmt.StatementExecutor;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableInfo;

/**
 * Base class for the Database Access Objects that handle the reading and writing a class from the database Kudos to
 * Robert A. for the general ideas of this hierarchy.
 * 
 * <p>
 * This class is also {@link Iterable} which means you can do a {@code for (T obj : dao)} type of loop code to iterate
 * through the table of persisted objects. See {@link #iterator()}.
 * </p>
 * 
 * <p>
 * <b> NOTE: </b> If you are using the Spring type wiring in Java, {@link #initialize} should be called after all of the
 * set methods. In Spring XML, init-method="initialize" should be used.
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
	 * Construct our base Jdbc class. The {@link DatabaseType} must be set with the {@link #setDatabaseType} method
	 * before {@link #initialize()} method is called. The dataClass provided must have its fields marked with
	 * {@link DatabaseField} annotations or the {@link #setTableConfig} method must be called before the
	 * {@link #initialize()} method is called.
	 * 
	 * @param dataClass
	 *            Class associated with this Dao. This must match the T class parameter.
	 */
	protected BaseDaoImpl(Class<T> dataClass) {
		this(null, dataClass, null);
	}

	/**
	 * Construct our base Jdbc class. The dataClass provided must have its fields marked with {@link DatabaseField}
	 * annotations or the {@link #setTableConfig} method must be called before the {@link #initialize()} method is
	 * called.
	 * 
	 * @param databaseType
	 *            Type of database.
	 * @param dataClass
	 *            Class associated with this Dao. This must match the T class parameter.
	 */
	protected BaseDaoImpl(DatabaseType databaseType, Class<T> dataClass) {
		this(databaseType, dataClass, null);
	}

	/**
	 * Construct our base Jdbc class. The {@link DatabaseType} must be set with the {@link #setDatabaseType} method
	 * before {@link #initialize()} method is called.
	 * 
	 * @param tableConfig
	 *            Hand or spring wired table configuration information.
	 */
	protected BaseDaoImpl(DatabaseTableConfig<T> tableConfig) {
		this(null, tableConfig.getDataClass(), tableConfig);
	}

	/**
	 * Construct our base Jdbc class.
	 * 
	 * @param databaseType
	 *            Type of database.
	 * @param tableConfig
	 *            Hand or spring wired table configuration information.
	 */
	protected BaseDaoImpl(DatabaseType databaseType, DatabaseTableConfig<T> tableConfig) {
		this(databaseType, tableConfig.getDataClass(), tableConfig);
	}

	private BaseDaoImpl(DatabaseType databaseType, Class<T> dataClass, DatabaseTableConfig<T> tableConfig) {
		this.databaseType = databaseType;
		this.dataClass = dataClass;
		this.tableConfig = tableConfig;
	}

	/**
	 * Initialize the various DAO configurations. This method is called from {@link #initialize()} which must be called
	 * after the Dao is configured (done by Spring automagically). This method should not be called directly by the
	 * Ormlite user.
	 */
	public void initialize() throws SQLException {
		if (initialized) {
			// just skip it if already initialized
			return;
		}
		if (databaseType == null) {
			throw new IllegalStateException("databaseType was never set on " + getClass().getSimpleName());
		}
		if (connectionSource == null) {
			throw new IllegalStateException("connectionSource was never set on " + getClass().getSimpleName());
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

	public T queryForFirst(PreparedStmt<T> preparedStmt) throws SQLException {
		checkForInitialized();
		if (preparedStmt.getType() != StatementType.SELECT) {
			throw new IllegalArgumentException("Cannot use a " + preparedStmt.getType()
					+ " statement in a query method");
		}
		DatabaseConnection connection = connectionSource.getReadOnlyConnection();
		try {
			return statementExecutor.queryForFirst(connection, preparedStmt);
		} finally {
			connectionSource.releaseConnection(connection);
		}
	}

	public List<T> queryForAll() throws SQLException {
		checkForInitialized();
		return statementExecutor.queryForAll(connectionSource);
	}

	/**
	 * @deprecated See {@link #queryBuilder()}
	 */
	@Deprecated
	public StatementBuilder<T, ID> statementBuilder() {
		return queryBuilder();
	}

	public StatementBuilder<T, ID> queryBuilder() {
		return statementBuilder(StatementType.SELECT);
	}

	public StatementBuilder<T, ID> updateBuilder() {
		return statementBuilder(StatementType.UPDATE);
	}

	public StatementBuilder<T, ID> deleteBuilder() {
		return statementBuilder(StatementType.DELETE);
	}

	public List<T> query(PreparedStmt<T> preparedStmt) throws SQLException {
		checkForInitialized();
		if (preparedStmt.getType() != StatementType.SELECT) {
			throw new IllegalArgumentException("Cannot use a " + preparedStmt.getType()
					+ " statement in a query method");
		}
		return statementExecutor.query(connectionSource, preparedStmt);
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

	public int update(PreparedStmt<T> preparedStmt) throws SQLException {
		checkForInitialized();
		if (preparedStmt.getType() != StatementType.UPDATE) {
			throw new IllegalArgumentException("Cannot use a " + preparedStmt.getType()
					+ " statement in an update method");
		}
		DatabaseConnection connection = connectionSource.getReadWriteConnection();
		try {
			return statementExecutor.update(connection, preparedStmt);
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

	public int delete(PreparedStmt<T> preparedStmt) throws SQLException {
		checkForInitialized();
		if (preparedStmt.getType() != StatementType.DELETE) {
			throw new IllegalArgumentException("Cannot use a " + preparedStmt.getType()
					+ " statement in a delete method");
		}
		DatabaseConnection connection = connectionSource.getReadWriteConnection();
		try {
			return statementExecutor.delete(connection, preparedStmt);
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

	public SelectIterator<T, ID> iterator(PreparedStmt<T> preparedStmt) throws SQLException {
		checkForInitialized();
		if (preparedStmt.getType() != StatementType.SELECT) {
			throw new IllegalArgumentException("Cannot use a " + preparedStmt.getType()
					+ " statement in a query method");
		}
		try {
			return statementExecutor.buildIterator(this, connectionSource, preparedStmt);
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

	/**
	 * Used if you want to wire the Dao with spring. In java you should use the
	 * {@link #BaseDaoImpl(DatabaseType, Class)} constructor. This must be called <i>before</i> {@link #initialize}.
	 */
	public void setDatabaseType(DatabaseType databaseType) {
		this.databaseType = databaseType;
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
	public static <T, ID> Dao<T, ID> createDao(DatabaseType databaseType, ConnectionSource connectionSource,
			Class<T> clazz) throws SQLException {
		BaseDaoImpl<T, ID> dao = new BaseDaoImpl<T, ID>(databaseType, clazz) {
		};
		dao.setConnectionSource(connectionSource);
		dao.initialize();
		return dao;
	}

	private StatementBuilder<T, ID> statementBuilder(StatementType type) {
		checkForInitialized();
		return new StatementBuilder<T, ID>(databaseType, tableInfo, type);
	}

	private void checkForInitialized() {
		if (!initialized) {
			throw new IllegalStateException("you must call initialize() before you can use the dao");
		}
	}
}
