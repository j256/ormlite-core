package com.j256.ormlite.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

/**
 * User test which tests some cache issues.
 */
public class DaoCacheTest extends BaseCoreTest {

	private Dao<Account, String> accountDao;
	private Dao<Order, String> orderDao;

	@Test
	public void testDaoCache() throws Exception {

		accountDao = createDao(Account.class, true);
		accountDao.setObjectCache(true);
		orderDao = createDao(Order.class, true);
		orderDao.setObjectCache(true);

		String accountName = "account";
		Account account = new Account(accountName);
		assertEquals(1, accountDao.create(account));

		Account account1 = accountDao.queryForId(accountName);
		assertNotSame(account, account1);

		String orderName = "order1";
		Order order = new Order(orderName);
		order.account = account1;
		assertEquals(1, orderDao.create(order));

		Account account2 = accountDao.queryForId(accountName);
		assertSame(account1, account2);

		Order order1 = account1.getOrders().get(0);
		assertEquals(order, order1);

		Order order2 = orderDao.queryForId(orderName);
		assertSame(order, order2);
		assertSame(order1, order2);

		accountDao.clearObjectCache();
		orderDao.clearObjectCache();
		BaseDaoImpl.clearAllInternalObjectCaches();

		Account account3 = accountDao.queryForId(accountName);
		assertNotSame(account, account3);
		assertNotSame(account1, account3);
		assertNotSame(account2, account3);

		Order order3 = orderDao.queryForId(orderName);
		assertNotNull(order3);
		assertNotSame(order, order3);
		assertNotSame(order1, order3);
		assertNotSame(order2, order3);

		Order order4 = account3.getOrders().get(0);
		assertNotNull(order4);
		assertNotSame(order, order4);
		assertNotSame(order1, order4);
		assertNotSame(order2, order4);
		assertSame(order4, order3);

		Order order5 = orderDao.queryForId(orderName);
		assertSame(order5, order5);
	}

	/******* Model *******/
	public static class Account {
		@DatabaseField(id = true)
		public String name;
		@ForeignCollectionField
		public ForeignCollection<Order> orders;

		public Account() {
			// ORMLite needs a no-arg constructor
		}

		public Account(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public ArrayList<Order> getOrders() {
			return new ArrayList<Order>(orders);
		}
	}

	public static class Order {
		@DatabaseField(id = true)
		public String name;
		@DatabaseField(foreign = true)
		public Account account;

		public Order() {
			// ORMLite needs a no-arg constructor
		}

		public Order(String name) {
			this.name = name;
		}

		public Account getAccount() {
			return account;
		}
	}
}
