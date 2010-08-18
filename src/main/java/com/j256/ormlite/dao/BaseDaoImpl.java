package com.j256.ormlite.dao;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectIterator;
import com.j256.ormlite.stmt.StatementExecutor;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseAccess;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableInfo;

/**
 * Base class for the Database Access Objects that handle the reading and writing a class from the database. This is the
 * implementation of the {@link Dao} and extends Spring's {@link SimpleDaoSupport}. Kudos to Robert A. for the general
 * ideas of this hierarchy.
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
	private DatabaseAccess databaseAccess;
	private ConnectionSource connectionSource;

	private final Class<T> dataClass;
	private DatabaseTableConfig<T> tableConfig;
	private TableInfo<T> tableInfo;
	private StatementExecutor<T, ID> statementExecutor;

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
		if (databaseType == null) {
			throw new IllegalStateException("databaseType was never set on " + getClass().getSimpleName());
		}
		databaseAccess = databaseType.buildDatabaseAccess(connectionSource);
		if (databaseAccess == null) {
			if (connectionSource == null) {
				throw new IllegalStateException("connectionSource was never set on " + getClass().getSimpleName());
			} else {
				throw new IllegalStateException("Unable to build database access object for "
						+ getClass().getSimpleName());
			}
		}

		if (tableConfig == null) {
			tableConfig = DatabaseTableConfig.fromClass(databaseType, dataClass);
		}
		this.tableInfo = new TableInfo<T>(databaseType, tableConfig);
		this.statementExecutor = new StatementExecutor<T, ID>(databaseType, tableInfo);
	}

	public T queryForId(ID id) throws SQLException {
		return statementExecutor.queryForId(databaseAccess, id);
	}

	public T queryForFirst(PreparedQuery<T> preparedQuery) throws SQLException {
		return statementExecutor.queryForFirst(databaseAccess, preparedQuery);
	}

	public List<T> queryForAll() throws SQLException {
		return statementExecutor.queryForAll(databaseAccess);
	}

	public QueryBuilder<T, ID> queryBuilder() {
		return new QueryBuilder<T, ID>(databaseType, tableInfo);
	}

	public List<T> query(PreparedQuery<T> preparedQuery) throws SQLException {
		return statementExecutor.query(databaseAccess, preparedQuery);
	}

	public RawResults queryForAllRaw(String queryString) throws SQLException {
		return statementExecutor.queryRaw(databaseAccess, queryString);
	}

	public int create(T data) throws SQLException {
		// ignore creating a null object
		if (data == null) {
			return 0;
		} else {
			return statementExecutor.create(databaseAccess, data);
		}
	}

	public int update(T data) throws SQLException {
		// ignore updating a null object
		if (data == null) {
			return 0;
		} else {
			return statementExecutor.update(databaseAccess, data);
		}
	}

	public int updateId(T data, ID newId) throws SQLException {
		// ignore updating a null object
		if (data == null) {
			return 0;
		} else {
			return statementExecutor.updateId(databaseAccess, data, newId);
		}
	}

	public int refresh(T data) throws SQLException {
		// ignore refreshing a null object
		if (data == null) {
			return 0;
		} else {
			return statementExecutor.refresh(databaseAccess, data);
		}
	}

	public int delete(T data) throws SQLException {
		// ignore deleting a null object
		if (data == null) {
			return 0;
		} else {
			return statementExecutor.delete(databaseAccess, data);
		}
	}

	public int delete(Collection<T> datas) throws SQLException {
		// ignore deleting a null object
		if (datas == null || datas.size() == 0) {
			return 0;
		} else {
			return statementExecutor.deleteObjects(databaseAccess, datas);
		}
	}

	public int deleteIds(Collection<ID> ids) throws SQLException {
		// ignore deleting a null object
		if (ids == null || ids.size() == 0) {
			return 0;
		} else {
			return statementExecutor.deleteIds(databaseAccess, ids);
		}
	}

	public SelectIterator<T, ID> iterator() {
		try {
			return statementExecutor.buildIterator(this, databaseAccess);
		} catch (Exception e) {
			throw new IllegalStateException("Could not build iterator for " + dataClass, e);
		}
	}

	public SelectIterator<T, ID> iterator(PreparedQuery<T> preparedQuery) throws SQLException {
		try {
			return statementExecutor.buildIterator(this, databaseAccess, preparedQuery);
		} catch (SQLException e) {
			throw SqlExceptionUtil.create("Could not build iterator for " + dataClass, e);
		}
	}

	public RawResults iteratorRaw(String query) throws SQLException {
		try {
			return statementExecutor.buildIterator(databaseAccess, query);
		} catch (SQLException e) {
			throw SqlExceptionUtil.create("Could not build iterator for " + query, e);
		}
	}

	public String objectToString(T data) {
		return tableInfo.objectToString(data);
	}

	public boolean objectsEqual(T data1, T data2) throws SQLException {
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
	 * {@link #BaseJdbcDao(DatabaseType, Class)} constructor. This must be called <i>before</i> {@link #initialize}.
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
}
