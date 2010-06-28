package com.j256.ormlite.spring;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.db.DatabaseTypeUtils;

/**
 * Factory class suitable for Spring injections of the database type classes.
 * 
 * <p>
 * <b> NOTE: </b> If you are using the Spring type wiring in Java, {@link #initialize} should be called after all of the
 * set methods. In Spring XML, init-method="initialize" should be used.
 * </p>
 * 
 * <p>
 * Here is an example of spring wiring. It expects the database URL to be in ${database.url}.
 * 
 * <blockquote>
 * 
 * <pre>
 * &lt;!-- our database type factory-bean --&gt;
 * &lt;bean id="databaseTypeFactory" class="com.j256.ormlite.db.DatabaseTypeFactory" init-method="initialize"&gt;
 * 	&lt;property name="databaseUrl" value="${database.url}" /&gt;
 * &lt;/bean&gt;
 * &lt;bean id="databaseType" class="com.j256.ormlite.db.DatabaseType" factory-bean="databaseTypeFactory"
 * 	factory-method="getDatabaseType" /&gt;
 * &lt;bean id="driverClassName" class="java.lang.String" factory-bean="databaseTypeFactory"
 * 	factory-method="getDriverClassName" /&gt;
 * </pre>
 * 
 * </blockquote>
 * 
 * @author graywatson
 */
public class DatabaseTypeFactory {

	private String databaseUrl;
	private DatabaseType databaseType;

	/**
	 * If you are using the Spring type wiring, this should be called after all of the set methods.
	 */
	public void initialize() throws Exception {
		if (databaseUrl == null) {
			throw new IllegalStateException("databaseUrl was not set on " + getClass().getSimpleName());
		}
		databaseType = DatabaseTypeUtils.createDatabaseType(databaseUrl);
		// load the driver at the start
		databaseType.loadDriver();
	}

	/**
	 * Return the class name of the database driver that was determined from the URL.
	 */
	public String getDriverClassName() {
		return databaseType.getDriverClassName();
	}

	/**
	 * Return the wired in database URL.
	 */
	public String getDatabaseUrl() {
		return databaseUrl;
	}

	// @Required
	public void setDatabaseUrl(String databaseUrl) {
		this.databaseUrl = databaseUrl;
	}

	/**
	 * Return the database type we got from the URL.
	 */
	public DatabaseType getDatabaseType() {
		return databaseType;
	}
}
