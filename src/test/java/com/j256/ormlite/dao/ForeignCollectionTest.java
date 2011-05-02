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

	@Test
	public void testForeignAutoRefreshWithCollection() throws Exception {
		createDao(Order.class, true);
		Dao<Account, Object> accountDao = createDao(Account.class, true);
		Dao<ForeignAutoRefresh, Object> farDao = createDao(ForeignAutoRefresh.class, true);
		Account account = new Account();
		String name1 = "fwepfjewfew";
		account.name = name1;
		assertEquals(1, accountDao.create(account));

		ForeignAutoRefresh far = new ForeignAutoRefresh();
		far.account = account;
		assertEquals(1, farDao.create(far));

		List<ForeignAutoRefresh> results = farDao.queryForAll();
		assertEquals(1, results.size());
		assertNotNull(results.get(0).account);
		assertNotNull(results.get(0).account.orders);
		assertEquals(0, results.get(0).account.orders.size());
	}

	@Test
	public void testQuestionAndAnswers() throws Exception {
		createDao(Order.class, true);
		Dao<Question, Object> questionDao = createDao(Question.class, true);
		Dao<Answer, Object> answerDao = createDao(Answer.class, true);

		Question question = new Question();
		String name = "some question";
		question.name = name;
		assertEquals(1, questionDao.create(question));

		Answer answer1 = new Answer();
		int val1 = 1234313123;
		answer1.val = val1;
		answer1.question = question;
		assertEquals(1, answerDao.create(answer1));

		Answer answer2 = new Answer();
		int val2 = 345543;
		answer2.val = val2;
		answer2.question = question;
		assertEquals(1, answerDao.create(answer2));

		assertEquals(1, questionDao.refresh(question));
		assertNotNull(question.answers);
		assertEquals(2, question.answers.size());
		
	}

	private void testCollection(Dao<Account, Integer> accountDao, boolean eager) throws Exception {
		Dao<Order, Integer> orderDao = createDao(Order.class, true);

		Account account1 = new Account();
		String name1 = "fwepfjewfew";
		account1.name = name1;
		assertEquals(1, accountDao.create(account1));

		Order order1 = new Order();
		int val1 = 13123441;
		order1.val = val1;
		order1.account = account1;
		assertEquals(1, orderDao.create(order1));

		Order order2 = new Order();
		int val2 = 113787097;
		order2.val = val2;
		order2.account = account1;
		assertEquals(1, orderDao.create(order2));

		// insert some other stuff just to confuse matters
		Account account2 = new Account();
		String name2 = "another name";
		account1.name = name2;
		assertEquals(1, accountDao.create(account2));

		Order order3 = new Order();
		int val3 = 17097;
		order3.val = val3;
		order3.account = account2;
		assertEquals(1, orderDao.create(order3));

		Account account3 = accountDao.queryForId(account1.id);
		assertEquals(name1, account3.name);
		assertNotNull(account3.orders);
		int orderC = 0;
		for (Order order : account3.orders) {
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
		assertFalse(account3.orders.isEmpty());
		assertTrue(account3.orders.contains(order1));
		assertTrue(account3.orders.containsAll(Arrays.asList(order1, order2)));
		Object[] orders = account3.orders.toArray();
		assertNotNull(orders);
		assertEquals(2, orders.length);
		assertEquals(order1, orders[0]);
		assertEquals(order2, orders[1]);
		orders = account3.orders.toArray(new Order[0]);
		assertNotNull(orders);
		assertEquals(2, orders.length);
		assertEquals(order1, orders[0]);
		assertEquals(order2, orders[1]);
		orders = account3.orders.toArray(new Order[1]);
		assertNotNull(orders);
		assertEquals(2, orders.length);
		assertEquals(order1, orders[0]);
		assertEquals(order2, orders[1]);
		orders = account3.orders.toArray(new Order[2]);
		assertNotNull(orders);
		assertEquals(2, orders.length);
		assertEquals(order1, orders[0]);
		assertEquals(order2, orders[1]);
		orders = account3.orders.toArray(new Order[3]);
		assertNotNull(orders);
		assertEquals(3, orders.length);
		assertEquals(order1, orders[0]);
		assertEquals(order2, orders[1]);
		assertNull(orders[2]);

		// insert it via the collection
		Order order5 = new Order();
		int val5 = 76557654;
		order5.val = val5;
		order5.account = account1;
		account3.orders.add(order5);
		// the size should change immediately
		assertEquals(3, account3.orders.size());

		// now insert it behind the collections back
		Order order6 = new Order();
		int val6 = 1123587097;
		order6.val = val6;
		order6.account = account1;
		assertEquals(1, orderDao.create(order6));
		if (eager) {
			// account2's collection should not have changed
			assertEquals(3, account3.orders.size());
		} else {
			// lazy does another query
			assertEquals(4, account3.orders.size());
		}

		// now we refresh the collection
		assertEquals(1, accountDao.refresh(account3));
		assertEquals(name1, account3.name);
		assertNotNull(account3.orders);
		orderC = 0;
		for (Order order : account3.orders) {
			orderC++;
			switch (orderC) {
				case 1 :
					assertEquals(val1, order.val);
					break;
				case 2 :
					assertEquals(val2, order.val);
					break;
				case 3 :
					assertEquals(val5, order.val);
					break;
				case 4 :
					assertEquals(val6, order.val);
					break;
			}
		}
		assertEquals(4, orderC);

		assertTrue(account3.orders.remove(order5));
		assertEquals(3, account3.orders.size());
		assertTrue(account3.orders.removeAll(Arrays.asList(order5, order6)));
		assertEquals(2, account3.orders.size());
		assertEquals(1, accountDao.refresh(account3));
		orderC = 0;
		for (Order order : account3.orders) {
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

		assertTrue(account3.orders.retainAll(Arrays.asList(order1)));
		orderC = 0;
		for (Order order : account3.orders) {
			orderC++;
			switch (orderC) {
				case 1 :
					assertEquals(val1, order.val);
					break;
			}
		}
		assertEquals(1, orderC);

		CloseableIterator<Order> iterator = account3.orders.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(order1, iterator.next());
		iterator.remove();
		assertFalse(iterator.hasNext());
		iterator.close();
		assertEquals(0, account3.orders.size());

		account3.orders.addAll(Arrays.asList(order1, order2, order5, order6));

		orderC = 0;
		for (Order order : account3.orders) {
			orderC++;
			switch (orderC) {
				case 1 :
					assertEquals(val1, order.val);
					break;
				case 2 :
					assertEquals(val2, order.val);
					break;
				case 3 :
					assertEquals(val5, order.val);
					break;
				case 4 :
					assertEquals(val6, order.val);
					break;
			}
		}
		assertEquals(4, orderC);

		account3.orders.clear();
		assertEquals(0, account3.orders.size());

		orders = account3.orders.toArray(new Order[2]);
		assertNotNull(orders);
		assertEquals(2, orders.length);
		assertNull(orders[0]);
		assertNull(orders[1]);

		assertFalse(account3.orders.contains(order1));
		assertFalse(account3.orders.containsAll(Arrays.asList(order1, order2, order5, order6)));

		assertEquals(name1, account3.name);
		String name3 = "gjrogejroregjpo";
		account3.name = name3;
		assertEquals(1, accountDao.update(account3));

		account3 = accountDao.queryForId(account1.id);
		assertNotNull(account3);
		assertEquals(name3, account3.name);

		int newId = account1.id + 100;
		assertEquals(1, accountDao.updateId(account3, newId));
		account3 = accountDao.queryForId(newId);
		assertNotNull(account3);

		assertEquals(1, accountDao.delete(account3));
		account3 = accountDao.queryForId(newId);
		assertNull(account3);
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

	protected static class Question {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		String name;
		@DatabaseField(foreign = true, foreignAutoRefresh = true)
		Answer bestAnswer;
		@ForeignCollectionField(eager = true)
		ForeignCollection<Answer> answers;
		protected Question() {
		}
	}

	protected static class Answer {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(unique = true)
		int val;
		@DatabaseField(foreign = true, foreignAutoRefresh = true)
		Question question;
		protected Answer() {
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

	protected static class ForeignAutoRefresh {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(foreign = true, foreignAutoRefresh = true)
		Account account;
		protected ForeignAutoRefresh() {
		}
	}
}
