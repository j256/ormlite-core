package com.j256.ormlite.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTableConfig;

public class ForeignCollectionTest extends BaseCoreTest {

	@Test
	public void testEagerCollection() throws Exception {
		Dao<Account, Integer> accountDao = createDao(Account.class, true);
		testCollection(accountDao, true);
	}

	@Test
	public void testLazyCollection() throws Exception {
		List<DatabaseFieldConfig> fieldConfigs = new ArrayList<DatabaseFieldConfig>();
		for (Field field : Account.class.getDeclaredFields()) {
			DatabaseFieldConfig fieldConfig = DatabaseFieldConfig.fromField(databaseType, "account", field);
			if (fieldConfig != null) {
				if (fieldConfig.isForeignCollection()) {
					fieldConfig.setForeignCollectionEager(false);
				}
				fieldConfigs.add(fieldConfig);
			}
		}
		DatabaseTableConfig<Account> tableConfig = new DatabaseTableConfig<Account>(Account.class, fieldConfigs);
		Dao<Account, Integer> accountDao = createDao(tableConfig, true);
		testCollection(accountDao, false);
	}

	private void testCollection(Dao<Account, Integer> accountDao, boolean eager) throws Exception {
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
		assertFalse(account2.orders.isEmpty());
		assertTrue(account2.orders.contains(order1));
		assertTrue(account2.orders.containsAll(Arrays.asList(order1, order2)));
		Object[] orders = account2.orders.toArray();
		assertNotNull(orders);
		assertEquals(2, orders.length);
		assertEquals(order1, orders[0]);
		assertEquals(order2, orders[1]);
		orders = account2.orders.toArray(new Order[0]);
		assertNotNull(orders);
		assertEquals(2, orders.length);
		assertEquals(order1, orders[0]);
		assertEquals(order2, orders[1]);
		orders = account2.orders.toArray(new Order[1]);
		assertNotNull(orders);
		assertEquals(2, orders.length);
		assertEquals(order1, orders[0]);
		assertEquals(order2, orders[1]);
		orders = account2.orders.toArray(new Order[2]);
		assertNotNull(orders);
		assertEquals(2, orders.length);
		assertEquals(order1, orders[0]);
		assertEquals(order2, orders[1]);
		orders = account2.orders.toArray(new Order[3]);
		assertNotNull(orders);
		assertEquals(3, orders.length);
		assertEquals(order1, orders[0]);
		assertEquals(order2, orders[1]);
		assertNull(orders[2]);

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
		if (eager) {
			// account2's collection should not have changed
			assertEquals(3, account2.orders.size());
		} else {
			// lazy does another query
			assertEquals(4, account2.orders.size());
		}

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

		assertTrue(account2.orders.remove(order3));
		assertEquals(3, account2.orders.size());
		assertTrue(account2.orders.removeAll(Arrays.asList(order3, order4)));
		assertEquals(2, account2.orders.size());
		assertEquals(1, accountDao.refresh(account2));
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
			}
		}
		assertEquals(2, orderC);

		assertTrue(account2.orders.retainAll(Arrays.asList(order1)));
		orderC = 0;
		for (Order order : account2.orders) {
			orderC++;
			switch (orderC) {
				case 1 :
					assertEquals(val1, order.val);
					break;
			}
		}
		assertEquals(1, orderC);

		CloseableIterator<Order> iterator = account2.orders.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(order1, iterator.next());
		iterator.remove();
		assertFalse(iterator.hasNext());
		iterator.close();
		assertEquals(0, account2.orders.size());

		account2.orders.addAll(Arrays.asList(order1, order2, order3, order4));

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

		account2.orders.clear();
		assertEquals(0, account2.orders.size());
		
		orders = account2.orders.toArray(new Order[2]);
		assertNotNull(orders);
		assertEquals(2, orders.length);
		assertNull(orders[0]);
		assertNull(orders[1]);
		
		assertFalse(account2.orders.contains(order1));
		assertFalse(account2.orders.containsAll(Arrays.asList(order1, order2, order3, order4)));
	}

	@Test(expected = SQLException.class)
	public void testNotProperCollection() throws Exception {
		createDao(NoProperCollection.class, true);
	}

	@Test(expected = SQLException.class)
	public void testNotParamaterized() throws Exception {
		createDao(NotParamaterized.class, true);
	}

	@Test(expected = SQLException.class)
	public void testNoForeignRelationship() throws Exception {
		createDao(NoForeign.class, true);
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
		@DatabaseField(unique = true)
		int val;
		@DatabaseField(foreign = true)
		Account account;
		protected Order() {
		}
		@Override
		public boolean equals(Object obj) {
			if (obj == null || obj.getClass() != getClass()) {
				return false;
			}
			return id == ((Order) obj).id;
		}
		@Override
		public int hashCode() {
			return id;
		}
	}

	protected static class NoProperCollection {
		@DatabaseField(generatedId = true)
		int id;
		@ForeignCollectionField(eager = true)
		List<Order> orders;
		protected NoProperCollection() {
		}
	}

	protected static class NotParamaterized {
		@DatabaseField(generatedId = true)
		int id;
		@SuppressWarnings("rawtypes")
		@ForeignCollectionField(eager = true)
		Collection orders;
		protected NotParamaterized() {
		}
	}

	protected static class NoForeign {
		@DatabaseField(generatedId = true)
		int id;
		@ForeignCollectionField(eager = true)
		Collection<Order> orders;
		protected NoForeign() {
		}
	}
}
