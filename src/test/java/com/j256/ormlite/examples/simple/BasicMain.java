package com.j256.ormlite.examples.simple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.db.DatabaseTypeUtils;
import com.j256.ormlite.examples.common.Account;
import com.j256.ormlite.examples.common.AccountDao;
import com.j256.ormlite.examples.common.AccountDaoImpl;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.PreparedStmt;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.StatementBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

/**
 * Main sample routine to show how to do basic operations with the package.
 */
public class BasicMain {

	// we are using the in-memory H2 database
	private final static String DATABASE_URL = "jdbc:h2:mem:account";

	private AccountDao accountDao;

	public static void main(String[] args) throws Exception {
		// turn our static method into an instance of Main
		new BasicMain().doMain(args);
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
			// do a bunch of bulk operations
			readWriteBunch();
			// show how to use the SelectArg object
			useSelectArgFeature();
			// show how to use the SelectArg object
			useTransactions(connectionSource);
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

		// if you need to create the table
		TableUtils.createTable(databaseType, connectionSource, Account.class);
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
		int id = account.getId();
		verifyDb(id, account);

		// assign a password
		account.setPassword("_secret");
		// update the database after changing the object
		if (accountDao.update(account) != 1) {
			throw new Exception("Could not update Account in the database");
		}
		verifyDb(id, account);

		// query for all items in the database
		List<Account> accounts = accountDao.queryForAll();
		assertEquals("Should have found 1 account matching our query", 1, accounts.size());
		verifyAccount(account, accounts.get(0));

		// loop through items in the database
		int accountC = 0;
		for (Account account2 : accountDao) {
			verifyAccount(account, account2);
			accountC++;
		}
		assertEquals("Should have found 1 account in for loop", 1, accountC);

		// construct a query using the QueryBuilder
		StatementBuilder<Account, Integer> statementBuilder = accountDao.statementBuilder();
		// shouldn't find anything: name LIKE 'hello" does not match our account
		statementBuilder.where().like(Account.NAME_FIELD_NAME, "hello");
		accounts = accountDao.query(statementBuilder.prepareStatement());
		assertEquals("Should not have found any accounts matching our query", 0, accounts.size());

		// should find our account: name LIKE 'Jim%' should match our account
		statementBuilder.where().like(Account.NAME_FIELD_NAME, name.substring(0, 3) + "%");
		accounts = accountDao.query(statementBuilder.prepareStatement());
		assertEquals("Should have found 1 account matching our query", 1, accounts.size());
		verifyAccount(account, accounts.get(0));

		// delete the account since we are done with it
		if (accountDao.delete(account) != 1) {
			throw new Exception("Could not delete Account from the database");
		}
		// we shouldn't find it now
		assertNull("account was deleted, shouldn't find any", accountDao.queryForId(id));
	}

	/**
	 * Example of reading and writing a large(r) number of objects.
	 */
	private void readWriteBunch() throws Exception {

		Map<String, Account> accounts = new HashMap<String, Account>();
		for (int i = 1; i <= 100; i++) {
			String name = Integer.toString(i);
			Account account = new Account(name);
			// persist the account object to the database, it should return 1
			if (accountDao.create(account) != 1) {
				throw new Exception("Could not create Account in database");
			}
			accounts.put(name, account);
		}

		// query for all items in the database
		List<Account> all = accountDao.queryForAll();
		assertEquals("Should have found same number of accounts in map", accounts.size(), all.size());
		for (Account account : all) {
			assertTrue("Should have found account in map", accounts.containsValue(account));
			verifyAccount(accounts.get(account.getName()), account);
		}

		// loop through items in the database
		int accountC = 0;
		for (Account account : accountDao) {
			assertTrue("Should have found account in map", accounts.containsValue(account));
			verifyAccount(accounts.get(account.getName()), account);
			accountC++;
		}
		assertEquals("Should have found the right number of accounts in for loop", accounts.size(), accountC);
	}

	/**
	 * Example of created a query with a ? argument using the {@link SelectArg} object. You then can set the value of
	 * this object at a later time.
	 */
	private void useSelectArgFeature() throws Exception {

		String name1 = "foo";
		String name2 = "bar";
		String name3 = "baz";
		assertEquals(1, accountDao.create(new Account(name1)));
		assertEquals(1, accountDao.create(new Account(name2)));
		assertEquals(1, accountDao.create(new Account(name3)));

		StatementBuilder<Account, Integer> statementBuilder = accountDao.statementBuilder();
		SelectArg selectArg = new SelectArg();
		// build a query with the WHERE clause set to 'name = ?'
		statementBuilder.where().like(Account.NAME_FIELD_NAME, selectArg);
		PreparedStmt<Account> preparedQuery = statementBuilder.prepareStatement();

		// now we can set the select arg (?) and run the query
		selectArg.setValue(name1);
		List<Account> results = accountDao.query(preparedQuery);
		assertEquals("Should have found 1 account matching our query", 1, results.size());
		assertEquals(name1, results.get(0).getName());

		selectArg.setValue(name2);
		results = accountDao.query(preparedQuery);
		assertEquals("Should have found 1 account matching our query", 1, results.size());
		assertEquals(name2, results.get(0).getName());

		selectArg.setValue(name3);
		results = accountDao.query(preparedQuery);
		assertEquals("Should have found 1 account matching our query", 1, results.size());
		assertEquals(name3, results.get(0).getName());
	}

	/**
	 * Example of created a query with a ? argument using the {@link SelectArg} object. You then can set the value of
	 * this object at a later time.
	 */
	private void useTransactions(ConnectionSource connectionSource) throws Exception {
		String name = "trans1";
		final Account account = new Account(name);
		assertEquals(1, accountDao.create(account));

		TransactionManager transactionManager = new TransactionManager(connectionSource);
		try {
			// try something in a transaction
			transactionManager.callInTransaction(new Callable<Void>() {
				public Void call() throws Exception {
					// we do the delete
					assertEquals(1, accountDao.delete(account));
					assertNull(accountDao.queryForId(account.getId()));
					// but then (as an example) we throw an exception which rolls back the delete
					throw new Exception("We throw to roll back!!");
				}
			});
			fail("This should have thrown");
		} catch (SQLException e) {
			// expected
		}

		assertNotNull(accountDao.queryForId(account.getId()));
	}

	/**
	 * Verify that the account stored in the database was the same as the expected object.
	 */
	private void verifyDb(int id, Account expected) throws SQLException, Exception {
		// make sure we can read it back
		Account account2 = accountDao.queryForId(id);
		if (account2 == null) {
			throw new Exception("Should have found id '" + id + "' in the database");
		}
		verifyAccount(expected, account2);
	}

	/**
	 * Verify that the account is the same as expected.
	 */
	private static void verifyAccount(Account expected, Account account2) {
		assertEquals("expected name does not equal account name", expected, account2);
		assertEquals("expected password does not equal account name", expected.getPassword(), account2.getPassword());
	}
}
