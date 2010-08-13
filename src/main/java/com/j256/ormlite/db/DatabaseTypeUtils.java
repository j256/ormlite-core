package com.j256.ormlite.db;

import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.support.SimpleDataSource;

/**
 * Utility class which helps with managing database specific classes.
 * 
 * @author graywatson
 */
public class DatabaseTypeUtils {

	private static Map<String, Constructor<? extends DatabaseType>> constructorMap =
			new HashMap<String, Constructor<? extends DatabaseType>>();
	private static Map<String, String> driverNameMap = new HashMap<String, String>();

	static {
		// new drivers need to be added here
		addDriver(MysqlDatabaseType.class);
		addDriver(PostgresDatabaseType.class);
		addDriver(H2DatabaseType.class);
		addDriver(DerbyEmbeddedDatabaseType.class);
		addDriver(SqliteDatabaseType.class);
		addDriver(HsqldbDatabaseType.class);
		addDriver(OracleDatabaseType.class);
		addDriver(SqlServerDatabaseType.class);
		addDriver(SqlServerJtdsDatabaseType.class);
		addDriver(Db2DatabaseType.class);
		addDriver(SqlDroidDatabaseType.class);
		/*
		 * duplicates to previous URL parts
		 */
		// same as DerbyEmbeddedDatabaseType
		addDriver(DerbyClientServerDatabaseType.class);
		// same as SqliteDatabaseType
		addDriver(SqliteAndroidDatabaseType.class);
	}

	/**
	 * For static methods only.
	 */
	private DatabaseTypeUtils() {
	}

	/**
	 * Examines the databaseUrl parameter and load the driver for the proper database type if it can. It is expecting a
	 * databaseUrl format like jdbc:db-type:... where db-type is one of "h2", "mysql", "postgresql", ...
	 * 
	 * @throws IllegalArgumentException
	 *             if the url format is not recognized or the database type is unknown.
	 * @throws ClassNotFoundException
	 *             If the database class is unknown.
	 */
	public static void loadDriver(String databaseUrl) throws ClassNotFoundException {
		String dbTypePart = extractDbType(databaseUrl);
		String driverClass = driverNameMap.get(dbTypePart);
		if (driverClass == null) {
			throw new IllegalArgumentException("Unknown database-type url part '" + dbTypePart + "' in: " + databaseUrl);
		}
		// this instantiates the driver class which wires in the JDBC glue
		Class.forName(driverClass);
	}

	/**
	 * Creates and returns a {@link SimpleDataSource} associated with the databaseUrl and optional userName and
	 * password. Calls {@link #loadDriver} as well. You can, of course, provide your own {@link DataSource} for use with
	 * the package.
	 * 
	 * @throws SQLException
	 *             If there are problems constructing the {@link DataSource}.
	 */
	public static SimpleDataSource createSimpleDataSource(String databaseUrl) throws SQLException {
		return createSimpleDataSource(databaseUrl, null, null);
	}

	/**
	 * Creates and returns a {@link SimpleDataSource} associated with the databaseUrl and optional userName and
	 * password. Calls {@link #loadDriver} as well.
	 * 
	 * @throws SQLException
	 *             If there are problems constructing the {@link DataSource}.
	 */
	public static SimpleDataSource createSimpleDataSource(String databaseUrl, String userName, String password)
			throws SQLException {
		try {
			loadDriver(databaseUrl);
			return new SimpleDataSource(databaseUrl, userName, password);
		} catch (Exception e) {
			throw SqlExceptionUtil.create("Problems creating simple dataSource from " + databaseUrl, e);
		}
	}

	/**
	 * Creates and returns a {@link DatabaseType} for the database URL.
	 * 
	 * @throws IllegalArgumentException
	 *             if the url format is not recognized, the database type is unknown, or the class could not be
	 *             constructed.
	 */
	public static DatabaseType createDatabaseType(String databaseUrl) {
		String dbTypePart = extractDbType(databaseUrl);
		Constructor<? extends DatabaseType> constructor = constructorMap.get(dbTypePart);
		if (constructor == null) {
			throw new IllegalArgumentException("Unknown database-type url part '" + dbTypePart + "' in: " + databaseUrl);
		}
		try {
			return constructor.newInstance();
		} catch (Exception e) {
			throw new IllegalArgumentException("Problems calling constructor " + constructor, e);
		}
	}

	/**
	 * Creates and returns a {@link DatabaseType} associated with a {@link DataSource}. This has to make a connection
	 * with the {@link DataSource} so it can get meta-data which has the URL. Use of {@link #createDatabaseType(String)}
	 * is recommended.
	 */
	public static DatabaseType createDatabaseType(DataSource dataSource) throws SQLException {
		return createDatabaseType(dataSource.getConnection().getMetaData().getURL());
	}

	private static void addDriver(Class<? extends DatabaseType> dbClass) {
		DatabaseType driverType;
		Constructor<? extends DatabaseType> constructor;
		try {
			constructor = dbClass.getConstructor();
			driverType = constructor.newInstance();
		} catch (Exception e) {
			throw new IllegalStateException("Could not construct driver class " + dbClass, e);
		}
		String urlPart = driverType.getDriverUrlPart();
		if (!constructorMap.containsKey(urlPart)) {
			constructorMap.put(urlPart, constructor);
		}
		String driverName = driverType.getDriverClassName();
		if (!driverNameMap.containsKey(urlPart)) {
			driverNameMap.put(urlPart, driverName);
		}
	}

	private static String extractDbType(String databaseUrl) {
		if (!databaseUrl.startsWith("jdbc:")) {
			throw new IllegalArgumentException("Database URL was expected to start with jdbc: but was " + databaseUrl);
		}
		String[] urlParts = databaseUrl.split(":");
		if (urlParts.length < 2) {
			throw new IllegalArgumentException("Database URL was expected to be in the form: jdbc:db-type:... but was "
					+ databaseUrl);
		}
		return urlParts[1];
	}
}
