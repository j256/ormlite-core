package com.j256.ormlite.spring;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;

/**
 * Spring bean that auto-creates any tables that it finds DAOs for if the property name in
 * TableCreator.AUTO_CREATE_TABLES property has been set to true. It will also auto-drop any tables that were
 * auto-created if the property name in TableCreator.AUTO_DROP_TABLES property has been set to true.
 * 
 * <p>
 * <b> NOTE: </b> If you are using the Spring type wiring in Java, {@link #initialize} should be called after all of the
 * set methods. In Spring XML, init-method="initialize" should be used.
 * </p>
 * 
 * <p>
 * Here is an example of spring wiring.
 * 
 * <blockquote>
 * 
 * <pre>
 * &lt;!-- our database type factory-bean --&gt;
 * &lt;bean id="tableCreator" class="com.j256.ormlite.db.TableCreator" init-method="initialize"&gt;
 * 	&lt;property name="databaseType" ref="databaseType" /&gt;
 * 	&lt;property name="dataSource" ref="dataSource" /&gt;
 * 	&lt;property name="configuredDaos"&gt;
 * 		&lt;list&gt;
 * 			&lt;ref bean="accountDao" /&gt;
 * 			&lt;ref bean="orderDao" /&gt;
 * 		&lt;/list&gt;
 * 	&lt;/property&gt;
 * &lt;/bean&gt;
 * </pre>
 * 
 * </blockquote>
 * 
 * 
 * @author graywatson
 */
public class TableCreator {

	public final static String AUTO_CREATE_TABLES = "ormlite.auto.create.tables";
	public final static String AUTO_DROP_TABLES = "ormlite.auto.drop.tables";

	private DatabaseType databaseType;
	private ConnectionSource dataSource;
	private List<BaseDaoImpl<?, ?>> configuredDaos;
	private Set<DatabaseTableConfig<?>> createdClasses = new HashSet<DatabaseTableConfig<?>>();

	/**
	 * If you are using the Spring type wiring, this should be called after all of the set methods.
	 */
	public void initialize() throws Exception {
		if (!Boolean.parseBoolean(System.getProperty(AUTO_CREATE_TABLES))) {
			return;
		}

		if (configuredDaos == null) {
			throw new IllegalStateException("configuredDaos was not set in " + getClass().getSimpleName());
		}

		// find all of the daos and create the tables
		for (BaseDaoImpl<?, ?> dao : configuredDaos) {
			Class<?> clazz = dao.getDataClass();
			try {
				DatabaseTableConfig<?> tableConfig = dao.getTableConfig();
				TableUtils.createTable(databaseType, dataSource, tableConfig);
				createdClasses.add(tableConfig);
			} catch (Exception e) {
				// we don't stop because the table might already exist
				System.err.println("Was unable to auto-create table for " + clazz);
			}
		}
	}

	public void destroy() throws Exception {
		if (!Boolean.parseBoolean(System.getProperty(AUTO_DROP_TABLES))) {
			return;
		}
		for (DatabaseTableConfig<?> tableConfig : createdClasses) {
			try {
				TableUtils.dropTable(databaseType, dataSource, tableConfig, false);
			} catch (Exception e) {
				// we don't stop because the table might already exist
				System.err.println("Was unable to auto-drop table for " + tableConfig.getDataClass());
			}
		}
		createdClasses.clear();
	}

	// @Required
	public void setDatabaseType(DatabaseType databaseType) {
		this.databaseType = databaseType;
	}

	// @Required
	public void setConnectionSource(ConnectionSource dataSource) {
		this.dataSource = dataSource;
	}

	public void setConfiguredDaos(List<BaseDaoImpl<?, ?>> configuredDaos) {
		this.configuredDaos = configuredDaos;
	}
}
