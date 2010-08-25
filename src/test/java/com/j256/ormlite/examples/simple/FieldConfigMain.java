package com.j256.ormlite.examples.simple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Date;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.db.DatabaseTypeUtils;
import com.j256.ormlite.examples.common.Account;
import com.j256.ormlite.examples.common.AccountDao;
import com.j256.ormlite.examples.common.AccountDaoImpl;
import com.j256.ormlite.examples.common.Delivery;
import com.j256.ormlite.examples.common.DeliveryDao;
import com.j256.ormlite.examples.common.DeliveryDaoImpl;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;

/**
 * Main sample routine to show how to do basic operations with the package.
 */
public class FieldConfigMain {

	// we are using the in-memory H2 database
	private final static String DATABASE_URL = "jdbc:h2:mem:account";

	private AccountDao accountDao;
	private DeliveryDao deliveryDao;

	public static void main(String[] args) throws Exception {
		// turn our static method into an instance of Main
		new FieldConfigMain().doMain(args);
	}

	private void doMain(String[] args) throws Exception {
		JdbcConnectionSource connectionSource = null;
		try {
			// create our data-source for the database
			connectionSource = DatabaseTypeUtils.createJdbcConnectionSource(DATABASE_URL);
			// setup our database and DAOs
			setupDatabase(DATABASE_URL, connectionSource);
			// read and write some data
			readWriteData();
		} finally {
			// destroy the data source which should close underlying connections
			if (connectionSource != null) {
				connectionSource.close();
			}
		}
	}

	/**
	 * Setup our database and DAOs
	 */
	private void setupDatabase(String databaseUrl, ConnectionSource dataSource) throws Exception {

		DatabaseType databaseType = DatabaseTypeUtils.createDatabaseType(databaseUrl);
		databaseType.loadDriver();

		AccountDaoImpl accountJdbcDao = new AccountDaoImpl();
		accountJdbcDao.setDatabaseType(databaseType);
		accountJdbcDao.setConnectionSource(dataSource);
		accountJdbcDao.initialize();
		accountDao = accountJdbcDao;

		DatabaseTableConfig<Delivery> tableConfig = buildTableConfig();
		DeliveryDaoImpl deliveryJdbcDao = new DeliveryDaoImpl();
		deliveryJdbcDao.setTableConfig(tableConfig);
		deliveryJdbcDao.setDatabaseType(databaseType);
		deliveryJdbcDao.setConnectionSource(dataSource);
		deliveryJdbcDao.initialize();
		deliveryDao = deliveryJdbcDao;

		// if you need to create the table
		TableUtils.createTable(databaseType, dataSource, Account.class);
		TableUtils.createTable(databaseType, dataSource, tableConfig);
	}

	private DatabaseTableConfig<Delivery> buildTableConfig() {
		ArrayList<DatabaseFieldConfig> fieldConfigs = new ArrayList<DatabaseFieldConfig>();
		fieldConfigs.add(new DatabaseFieldConfig("id", null, DataType.UNKNOWN, null, 0, false, false, true, null,
				false, null, false, null, false, null));
		fieldConfigs.add(new DatabaseFieldConfig("when", null, DataType.UNKNOWN, null, 0, false, false, false, null,
				false, null, false, null, false, null));
		fieldConfigs.add(new DatabaseFieldConfig("signedBy", null, DataType.UNKNOWN, null, 0, false, false, false,
				null, false, null, false, null, false, null));
		fieldConfigs.add(new DatabaseFieldConfig("account", null, DataType.UNKNOWN, null, 0, false, false, false, null,
				true, null, false, null, false, null));
		DatabaseTableConfig<Delivery> tableConfig = new DatabaseTableConfig<Delivery>(Delivery.class, fieldConfigs);
		return tableConfig;
	}

	/**
	 * Read and write some example data.
	 */
	private void readWriteData() throws Exception {
		// create an instance of Account
		String name = "Jim Coakley";
		Account account = new Account(name);
		// persist the account object to the database, it should return 1
		if (accountDao.create(account) != 1) {
			throw new Exception("Could not create Account in database");
		}

		Delivery delivery = new Delivery(new Date(), "Mr. Ed", account);
		// persist the account object to the database, it should return 1
		if (deliveryDao.create(delivery) != 1) {
			throw new Exception("Could not create Delivery in database");
		}

		Delivery delivery2 = deliveryDao.queryForId(delivery.getId());
		assertNotNull(delivery2);
		assertEquals(delivery.getId(), delivery2.getId());
		assertEquals(account.getId(), delivery2.getAccount().getId());
	}
}
