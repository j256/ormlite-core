package com.j256.ormlite.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

public class EagerForeignCollectionTest extends BaseCoreTest {

	@Test
	public void testBasicEagerCollection() throws Exception {
		Dao<Account, Integer> accountDao = createDao(Account.class, true);
		Dao<Order, Integer> orderDao = createDao(Order.class, true);

		Account account = new Account();
		String name = "fwepfjewfew";
		account.name = name;
		assertEquals(1, accountDao.create(account));

		Order order1 = new Order();
		int val1 = 13123441;
		order1.val = val1;
		order1.account = account;
		assertEquals(1, orderDao.create(order1));

		Order order2 = new Order();
		int val2 = 113787097;
		order2.val = val2;
		order2.account = account;
		assertEquals(1, orderDao.create(order2));

		Account account2 = accountDao.queryForId(account.id);
		assertEquals(name, account2.name);
		assertNotNull(account2.orders);
		int orderC = 0;
		for (Order order : account2.orders) {
			orderC++;
			switch (orderC) {
				case 1 :
					assertEquals(val1, order.val);
					break;
				case 2 :
					assertEquals(val2, order.val);
					break;
			}
		}
		assertEquals(2, orderC);

		// insert it via the collection
		Order order3 = new Order();
		int val3 = 76557654;
		order3.val = val3;
		order3.account = account;
		account2.orders.add(order3);
		// the size should change immediately
		assertEquals(3, account2.orders.size());

		// now insert it behind the collections back
		Order order4 = new Order();
		int val4 = 1123587097;
		order4.val = val4;
		order4.account = account;
		assertEquals(1, orderDao.create(order4));
		// account2's collection should not have changed
		assertEquals(3, account2.orders.size());

		// now we refresh the collection
		assertEquals(1, accountDao.refresh(account2));
		assertEquals(name, account2.name);
		assertNotNull(account2.orders);
		orderC = 0;
		for (Order order : account2.orders) {
			orderC++;
			switch (orderC) {
				case 1 :
					assertEquals(val1, order.val);
					break;
				case 2 :
					assertEquals(val2, order.val);
					break;
				case 3 :
					assertEquals(val3, order.val);
					break;
				case 4 :
					assertEquals(val4, order.val);
					break;
			}
		}
		assertEquals(4, orderC);
	}

	protected static class Account {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		String name;
		@ForeignCollectionField(eager = true)
		ForeignCollection<Order> orders;
		protected Account() {
		}
	}

	protected static class Order {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		int val;
		@DatabaseField(foreign = true)
		Account account;
		protected Order() {
		}
	}
}
