package com.j256.ormlite.jdbc;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;

/**
 * Implementation of the ConnectionSource interface that supports what is needed by ORMLite. This is not thread-safe nor
 * synchronized. For other dataSources, see the {@link DataSourceConnectionSource} class.
 * 
 * <p>
 * <b> NOTE: </b> If you are using the Spring type wiring in Java, {@link #initialize} should be called after all of the
 * set methods. In Spring XML, init-method="initialize" should be used.
 * </p>
 * 
 * @author graywatson
 */
public class JdbcConnectionSource implements ConnectionSource {

	private static Logger logger = LoggerFactory.getLogger(JdbcConnectionSource.class);

	private String url;
	private String username;
	private String password;
	private JdbcDatabaseConnection connection;

	/**
	 * Constructor for Spring type wiring if you are using the set methods.
	 */
	public JdbcConnectionSource() {
		// for spring wiring
	}

	/**
	 * Create a data source for a particular database URL.
	 */
	public JdbcConnectionSource(String url) {
		this.url = url;
	}

	/**
	 * Create a data source for a particular database URL with username and password permissions.
	 */
	public JdbcConnectionSource(String url, String username, String password) {
		this.url = url;
		this.username = username;
		this.password = password;
	}

	/**
	 * If you are using the Spring type wiring, this should be called after all of the set methods.
	 */
	public void initialize() {
		if (url == null) {
			throw new IllegalStateException("url was never set on " + getClass().getSimpleName());
		}
	}

	public void close() throws SQLException {
		if (connection != null) {
			connection.close();
			connection = null;
			logger.debug("closed connection to {}", url);
		}
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public DatabaseConnection getReadOnlyConnection() throws SQLException {
		return getReadWriteConnection();
	}

	public DatabaseConnection getReadWriteConnection() throws SQLException {
		if (connection != null) {
			if (connection.isClosed()) {
				throw new SQLException("Connection has already been closed");
			} else {
				return connection;
			}
		}
		Properties properties = new Properties();
		if (username != null) {
			properties.setProperty("user", username);
		}
		if (password != null) {
			properties.setProperty("password", password);
		}
		logger.debug("opening connection to {}", url);
		connection = new JdbcDatabaseConnection(DriverManager.getConnection(url, properties));
		if (connection == null) {
			// may never get here but let's be careful
			throw new SQLException("Could not establish connection to database URL: " + url);
		} else {
			return connection;
		}
	}

	public void releaseConnection(DatabaseConnection connection) throws SQLException {
		// noop right now
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}