package com.j256.ormlite.support;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.logger.LoggerFactory;

/**
 * Implementation of the {@link DataSource} interface that supports what is needed by ORMLite. This is not thread-safe
 * nor synchronized. For multi-threaded, high-performance data sources, see Apache DBCP, CP30, or BoneCP.
 * 
 * <p>
 * <b> NOTE: </b> If you are using the Spring type wiring in Java, {@link #initialize} should be called after all of the
 * set methods. In Spring XML, init-method="initialize" should be used.
 * </p>
 * 
 * @author graywatson
 */
public class SimpleDataSource implements DataSource {

	private static Logger logger = LoggerFactory.getLogger(SimpleDataSource.class);

	private PrintWriter printWriter = new PrintWriter(System.out);
	private int loginTimeoutSecs = 3600;

	private String url;
	private String username;
	private String password;
	private Connection connection;

	/**
	 * Constructor for Spring type wiring if you are using the set methods.
	 */
	public SimpleDataSource() {
		// for spring wiring
	}

	/**
	 * Create a data source for a particular database URL.
	 */
	public SimpleDataSource(String url) {
		this.url = url;
	}

	/**
	 * Create a data source for a particular database URL with username and password permissions.
	 */
	public SimpleDataSource(String url, String username, String password) {
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

	/**
	 * Cleanup method to close any open connections and do other cleanups.
	 */
	public void destroy() throws SQLException {
		close();
	}

	/**
	 * Close any connections opened by the data source.
	 */
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

	public Connection getConnection() throws SQLException {
		return getConnection(username, password);
	}

	public Connection getConnection(String username, String password) throws SQLException {
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
		connection = DriverManager.getConnection(url, properties);
		if (connection == null) {
			// may never get here but let's be careful
			throw new SQLException("Could not establish connection to database URL: " + url);
		} else {
			return connection;
		}
	}

	public PrintWriter getLogWriter() {
		return printWriter;
	}

	public void setLogWriter(PrintWriter printWriter) {
		this.printWriter = printWriter;
	}

	public int getLoginTimeout() {
		return loginTimeoutSecs;
	}

	public void setLoginTimeout(int loginTimeoutSecs) {
		this.loginTimeoutSecs = loginTimeoutSecs;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * NOTE: this is part of the Java6 JDK definition for {@link DataSource}.
	 */
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}

	/**
	 * NOTE: this is part of the Java6 JDK definition for {@link DataSource}.
	 */
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return null;
	}
}