package com.j256.ormlite;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import com.j256.ormlite.dao.BaseJdbcDao;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.db.DatabaseTypeUtils;
import com.j256.ormlite.jdbc.JdbcDatabaseAccess;
import com.j256.ormlite.support.DatabaseAccess;
import com.j256.ormlite.support.SimpleDataSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;

public abstract class BaseOrmLiteTest {

	private static final String DATASOURCE_ERROR = "Property 'dataSource' is required";
	@Rule
	public PossibleException possibleException = new PossibleException();

	protected static final String DEFAULT_DATABASE_URL = "jdbc:h2:mem:ormlite";

	protected String databaseHost = null;
	protected String databaseUrl = null;
	protected String userName = null;
	protected String password = null;

	protected static DataSource dataSource = null;
	protected static DatabaseAccess jdbcTemplate = null;
	protected DatabaseType databaseType = null;
	protected boolean isConnectionExpected = false;

	private Set<DatabaseTableConfig<?>> dropClassSet = new HashSet<DatabaseTableConfig<?>>();

	@Before
	public void before() throws Exception {
		if (databaseType != null) {
			return;
		}
		// do this for everyone
		System.setProperty("derby.stream.error.file", "target/derby.log");
		setDatabaseParams();
		String url;
		if (databaseUrl == null) {
			url = DEFAULT_DATABASE_URL;
		} else {
			url = databaseUrl;
		}
		databaseType = DatabaseTypeUtils.createDatabaseType(url);
		if (dataSource == null) {
			Class.forName(databaseType.getDriverClassName());
			isConnectionExpected = isConnectionExpected();
			if (isConnectionExpected) {
				if (userName == null && password == null) {
					dataSource = DatabaseTypeUtils.createSimpleDataSource(url);
				} else {
					dataSource = DatabaseTypeUtils.createSimpleDataSource(url, userName, password);
				}
			}
			jdbcTemplate = new JdbcDatabaseAccess(dataSource);
		}
	}

	/**
	 * Set the database parameters for this db type.
	 */
	protected void setDatabaseParams() throws Exception {
		// noop here -- designed to be overridden
	}

	@After
	public void after() throws Exception {
		closeConnection();
	}

	/**
	 * Return if this test was expecting to be able to load the driver class
	 */
	protected boolean isDriverClassExpected() {
		return true;
	}

	/**
	 * Return if this test was expecting to be able to connect to the database
	 */
	protected boolean isConnectionExpected() throws IOException {
		try {
			if (databaseHost == null) {
				return true;
			} else {
				return InetAddress.getByName(databaseHost).isReachable(500);
			}
		} catch (UnknownHostException e) {
			return false;
		} catch (IOException e) {
			throw e;
		}
	}

	protected void closeConnection() throws Exception {
		if (dataSource != null) {
			for (DatabaseTableConfig<?> tableConfig : dropClassSet) {
				dropTable(tableConfig, true);
			}
			try {
				if (dataSource instanceof SimpleDataSource) {
					((SimpleDataSource) dataSource).close();
				}
			} catch (Exception e) {
				// oh well, we tried
			}
			dataSource = null;
		}
		databaseType = null;
	}

	protected <T, ID> Dao<T, ID> createDao(Class<T> clazz, boolean createTable) throws Exception {
		return createDao(DatabaseTableConfig.fromClass(databaseType, clazz), createTable);
	}

	protected <T, ID> Dao<T, ID> createDao(DatabaseTableConfig<T> tableConfig, boolean createTable) throws Exception {
		BaseJdbcDao<T, ID> dao = new BaseJdbcDao<T, ID>(databaseType, tableConfig) {
		};
		return configDao(tableConfig, createTable, dao);
	}

	protected <T> void createTable(Class<T> clazz, boolean dropAtEnd) throws Exception {
		createTable(DatabaseTableConfig.fromClass(databaseType, clazz), dropAtEnd);
	}

	protected <T> void createTable(DatabaseTableConfig<T> tableConfig, boolean dropAtEnd) throws Exception {
		try {
			// first we drop it in case it existed before
			dropTable(tableConfig, true);
		} catch (SQLException ignored) {
			// ignore any errors about missing tables
		}
		TableUtils.createTable(databaseType, dataSource, tableConfig);
		if (dropAtEnd) {
			dropClassSet.add(tableConfig);
		}
	}

	protected <T> void dropTable(Class<T> clazz, boolean ignoreErrors) throws Exception {
		// drop the table and ignore any errors along the way
		TableUtils.dropTable(databaseType, dataSource, clazz, ignoreErrors);
	}

	protected <T> void dropTable(DatabaseTableConfig<T> tableConfig, boolean ignoreErrors) throws Exception {
		// drop the table and ignore any errors along the way
		TableUtils.dropTable(databaseType, dataSource, tableConfig, ignoreErrors);
	}

	private <T, ID> Dao<T, ID> configDao(DatabaseTableConfig<T> tableConfig, boolean createTable, BaseJdbcDao<T, ID> dao)
			throws Exception {
		if (dataSource == null) {
			throw new SQLException(DATASOURCE_ERROR);
		}
		dao.setDataSource(dataSource);
		if (createTable) {
			createTable(tableConfig, true);
		}
		dao.initialize();
		return dao;
	}

	/**
	 * Our own junit rule which adds in an optional exception matcher if the db host is not available.
	 */
	public class PossibleException implements MethodRule {

		private Class<? extends Throwable> tClass = null;

		public Statement apply(Statement statement, FrameworkMethod method, Object junitClassObject) {
			for (Annotation annotation : method.getAnnotations()) {
				if (annotation.annotationType() == ExpectedBehavior.class) {
					ExpectedBehavior test = (ExpectedBehavior) annotation;
					tClass = test.expected();
					break;
				}
			}
			return new StatementWrapper(statement);
		}

		/**
		 * Specify the expected throwable class or you can use the {@link ExpectedBehavior} annotation.
		 */
		public void expect(Class<? extends Throwable> tClass) {
			this.tClass = tClass;
		}

		private class StatementWrapper extends Statement {
			private final Statement statement;

			public StatementWrapper(Statement statement) {
				this.statement = statement;
			}

			@Override
			public void evaluate() throws Throwable {
				try {
					statement.evaluate();
				} catch (Throwable t) {
					String assertMsg;
					if (t instanceof AssertionError) {
						throw t;
					} else if ((!isConnectionExpected) && t.getMessage() != null
							&& t.getMessage().contains(DATASOURCE_ERROR)) {
						// if we throw because of missing data-source and the db server isn't available, ignore it
						return;
					} else if (tClass == null) {
						assertMsg = "Test threw unexpected exception: " + t;
					} else if (tClass == t.getClass()) {
						// we matched our expected exception
						return;
					} else {
						assertMsg = "Expected test to throw " + tClass + " but it threw: " + t;
					}
					Error error = new AssertionError(assertMsg);
					error.initCause(t);
					throw error;
				}
				// can't be in the throw block
				if (tClass != null) {
					throw new AssertionError("Expected test to throw " + tClass);
				}
			}
		}
	}

	/**
	 * We can't use the @Test(expected) with the {@link PossibleException} rule because it masks the exception and
	 * doesn't pass it up to our statement wrapper.
	 */
	@Target(METHOD)
	@Retention(RUNTIME)
	public @interface ExpectedBehavior {
		Class<? extends Throwable> expected();
	}
}
