package com.j256.ormlite.examples.foreign;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.db.DatabaseTypeUtils;
import com.j256.ormlite.examples.common.Account;
import com.j256.ormlite.examples.common.AccountDao;
import com.j256.ormlite.examples.common.AccountDaoImpl;
import com.j256.ormlite.examples.common.Order;
import com.j256.ormlite.examples.common.OrderDao;
import com.j256.ormlite.examples.common.OrderDaoImpl;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.StatementBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

/**
 * Main sample routine to show how to do basic operations with the package.
 */
public class Main {

	// we are using the in-memory H2 database
	private final static String DATABASE_URL = "jdbc:h2:mem:account";

	private AccountDao accountDao;
	private OrderDao orderDao;

	public static void main(String[] args) throws Exception {
		// turn our static method into an instance of Main
		new Main().doMain(args);
	}

	private void doMain(String[] args) throws Exception {
		JdbcConnectionSource connectionSource = null;
		try {
			// create our data source
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
	private void setupDatabase(String databaseUrl, ConnectionSource connectionSource) throws Exception {

		DatabaseType databaseType = DatabaseTypeUtils.createDatabaseType(databaseUrl);
		databaseType.loadDriver();

		AccountDaoImpl accountJdbcDao = new AccountDaoImpl();
		accountJdbcDao.setDatabaseType(databaseType);
		accountJdbcDao.setConnectionSource(connectionSource);
		accountJdbcDao.initialize();
		accountDao = accountJdbcDao;

		OrderDaoImpl orderJdbcDao = new OrderDaoImpl();
		orderJdbcDao.setDatabaseType(databaseType);
		orderJdbcDao.setConnectionSource(connectionSource);
		orderJdbcDao.initialize();
		orderDao = orderJdbcDao;

		// if you need to create the table
		TableUtils.createTable(databaseType, connectionSource, Account.class);
		TableUtils.createTable(databaseType, connectionSource, Order.class);
	}

	private void readWriteData() throws Exception {
		// create an instance of Account
		String name = "Buzz Lightyear";
		Account account = new Account(name);

		// persist the account object to the database, it should return 1
		if (accountDao.create(account) != 1) {
			throw new Exception("Could not create Account in database");
		}

		// create an associated Order for the Account
		// Buzz bought 2 of item #21312 for a price of $12.32
		int quantity1 = 2;
		int itemNumber1 = 21312;
		float price1 = 12.32F;
		Order order1 = new Order(account, itemNumber1, price1, quantity1);
		if (orderDao.create(order1) != 1) {
			throw new Exception("Could not create Order in database");
		}

		// create another Order for the Account
		// Buzz also bought 1 of item #785 for a price of $7.98
		int quantity2 = 1;
		int itemNumber2 = 785;
		float price2 = 7.98F;
		Order order2 = new Order(account, itemNumber2, price2, quantity2);
		if (orderDao.create(order2) != 1) {
			throw new Exception("Could not create Order in database");
		}

		// construct a query using the QueryBuilder
		StatementBuilder<Order, Integer> statementBuilder = orderDao.statementBuilder();
		// should find both of the orders that match the account id
		statementBuilder.where().eq(Order.ACCOUNT_ID_FIELD_NAME, account.getId());
		List<Order> orders = orderDao.query(statementBuilder.prepareStatement());

		// sanity checks
		assertEquals("Should have found both of the orders for the account", 2, orders.size());
		assertTrue(orderDao.objectsEqual(order1, orders.get(0)));
		assertTrue(orderDao.objectsEqual(order2, orders.get(1)));

		/*
		 * Notice that in each of the orders that we got from the query, the Account id is good but the name field is
		 * null. With foreign object fields, only the id field is stored in the table for the order.
		 */
		assertEquals(account.getId(), orders.get(0).getAccount().getId());
		assertEquals(account.getId(), orders.get(1).getAccount().getId());
		assertNull(orders.get(0).getAccount().getName());
		assertNull(orders.get(1).getAccount().getName());

		/*
		 * To get the name field from the order's account field, we need to refresh each of the objects in the list
		 * which will lookup the id and load in all of the fields.
		 */
		assertEquals(1, accountDao.refresh(orders.get(0).getAccount()));
		assertEquals(1, accountDao.refresh(orders.get(1).getAccount()));

		// now the account name field has been filled in
		assertEquals(account.getName(), orders.get(0).getAccount().getName());
		assertEquals(account.getName(), orders.get(1).getAccount().getName());
	}
}
