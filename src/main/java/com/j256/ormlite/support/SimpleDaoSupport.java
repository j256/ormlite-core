package com.j256.ormlite.support;

import java.sql.SQLException;

import javax.sql.DataSource;

import com.j256.ormlite.dao.BaseJdbcDao;
import com.j256.ormlite.db.DatabaseType;

/**
 * Replacement for Spring's SimpleJdbcDaoSupport that provides some DAO methods. This could be rolled into the
 * {@link BaseJdbcDao} (the only extender) but this mirrors the Spring hierarchy so should be left as is.
 * 
 * <p>
 * <b> NOTE: </b> If you are using the Spring type wiring in Java, {@link #initialize} should be called after all of the
 * set methods. In Spring XML, init-method="initialize" should be used.
 * </p>
 * 
 * @author graywatson
 */
public abstract class SimpleDaoSupport {

	protected DatabaseType databaseType;
	protected DatabaseAccess databaseAccess;
	private DataSource dataSource;

	/**
	 * Constructor for Spring type wiring if you are using the set methods.
	 */
	protected SimpleDaoSupport() {
		// for Spring wiring
	}

	protected SimpleDaoSupport(DatabaseType databaseType) {
		this.databaseType = databaseType;
	}

	/**
	 * Constructor if you have the dataSource already.
	 */
	protected SimpleDaoSupport(DatabaseType databaseType, DataSource dataSource) throws SQLException {
		this.dataSource = dataSource;
		initialize();
	}

	/**
	 * If you are using the Spring type wiring, this should be called after all of the set methods.
	 * 
	 * NOTE: this needs to throw because subclasses override it and might throw.
	 */
	public void initialize() throws SQLException {
		if (databaseType == null) {
			throw new IllegalStateException("databaseType was never set on " + getClass().getSimpleName());
		}
		databaseAccess = databaseType.buildDatabaseAccess(dataSource);
		if (databaseAccess == null) {
			if (dataSource == null) {
				throw new IllegalStateException("dataSource was never set on " + getClass().getSimpleName());
			} else {
				throw new IllegalStateException("Unable to build database access object for "
						+ getClass().getSimpleName());
			}
		}
	}

	/**
	 * Used if you want to wire the Dao with spring. In java you should use the
	 * {@link #BaseJdbcDao(DatabaseType, Class)} constructor. This must be called <i>before</i> {@link #initialize}.
	 */
	public void setDatabaseType(DatabaseType databaseType) {
		this.databaseType = databaseType;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
}
