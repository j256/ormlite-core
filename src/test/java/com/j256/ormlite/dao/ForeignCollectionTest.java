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

		Account accountResult = accountDao.queryForId(account1.id);
		assertEquals(name1, accountResult.name);
		assertNotNull(accountResult.orders);
		int orderC = 0;
		for (Order order : accountResult.orders) {
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
		assertFalse(accountResult.orders.isEmpty());
		assertTrue(accountResult.orders.contains(order1));
		assertTrue(accountResult.orders.containsAll(Arrays.asList(order1, order2)));
		Object[] objs = accountResult.orders.toArray();
		assertNotNull(objs);
		assertEquals(2, objs.length);
		assertEquals(order1, objs[0]);
		assertEquals(order2, objs[1]);
		Order[] orders = accountResult.orders.toArray(new Order[0]);
		assertNotNull(orders);
		assertEquals(2, orders.length);
		assertEquals(order1, orders[0]);
		assertEquals(order2, orders[1]);
		orders = accountResult.orders.toArray(new Order[1]);
		assertNotNull(orders);
		assertEquals(2, orders.length);
		assertEquals(order1, orders[0]);
		assertEquals(order2, orders[1]);
		orders = accountResult.orders.toArray(new Order[2]);
		assertNotNull(orders);
		assertEquals(2, orders.length);
		assertEquals(order1, orders[0]);
		assertEquals(order2, orders[1]);
		orders = accountResult.orders.toArray(new Order[3]);
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
		accountResult.orders.add(order5);
		// the size should change immediately
		assertEquals(3, accountResult.orders.size());

		// now insert it behind the collections back
		Order order6 = new Order();
		int val6 = 1123587097;
		order6.val = val6;
		order6.account = account1;
		assertEquals(1, orderDao.create(order6));
		if (eager) {
			// account2's collection should not have changed
			assertEquals(3, accountResult.orders.size());
		} else {
			// lazy does another query
			assertEquals(4, accountResult.orders.size());
		}

		// now we refresh the collection
		assertEquals(1, accountDao.refresh(accountResult));
		assertEquals(name1, accountResult.name);
		assertNotNull(accountResult.orders);
		orderC = 0;
		for (Order order : accountResult.orders) {
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

		assertTrue(accountResult.orders.remove(order5));
		assertEquals(3, accountResult.orders.size());
		assertTrue(accountResult.orders.removeAll(Arrays.asList(order5, order6)));
		assertEquals(2, accountResult.orders.size());
		assertEquals(1, accountDao.refresh(accountResult));
		orderC = 0;
		for (Order order : accountResult.orders) {
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

		assertTrue(accountResult.orders.retainAll(Arrays.asList(order1)));
		orderC = 0;
		for (Order order : accountResult.orders) {
			orderC++;
			switch (orderC) {
				case 1 :
					assertEquals(val1, order.val);
					break;
			}
		}
		assertEquals(1, orderC);

		CloseableIterator<Order> iterator = accountResult.orders.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(order1, iterator.next());
		iterator.remove();
		assertFalse(iterator.hasNext());
		iterator.close();
		assertEquals(0, accountResult.orders.size());

		accountResult.orders.addAll(Arrays.asList(order1, order2, order5, order6));

		orderC = 0;
		for (Order order : accountResult.orders) {
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

		accountResult.orders.clear();
		assertEquals(0, accountResult.orders.size());

		orders = accountResult.orders.toArray(new Order[2]);
		assertNotNull(orders);
		assertEquals(2, orders.length);
		assertNull(orders[0]);
		assertNull(orders[1]);

		assertFalse(accountResult.orders.contains(order1));
		assertFalse(accountResult.orders.containsAll(Arrays.asList(order1, order2, order5, order6)));

		assertEquals(name1, accountResult.name);
		String name3 = "gjrogejroregjpo";
		accountResult.name = name3;
		assertEquals(1, accountDao.update(accountResult));

		accountResult = accountDao.queryForId(account1.id);
		assertNotNull(accountResult);
		assertEquals(name3, accountResult.name);

		int newId = account1.id + 100;
		assertEquals(1, accountDao.updateId(accountResult, newId));
		accountResult = accountDao.queryForId(newId);
		assertNotNull(accountResult);

		assertEquals(1, accountDao.delete(accountResult));
		accountResult = accountDao.queryForId(newId);
		assertNull(accountResult);
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

	@Test
	public void testRecursiveReference() throws Exception {
		Dao<RecursiveReference, Object> dao = createDao(RecursiveReference.class, true);
		RecursiveReference rr1 = new RecursiveReference();
		rr1.stuff = "fpeewihwhgwofjwe";
		assertEquals(1, dao.create(rr1));

		RecursiveReference rr2 = new RecursiveReference();
		rr2.parent = rr1;
		rr2.stuff = "fpewofjwe";
		assertEquals(1, dao.create(rr2));

		RecursiveReference result = dao.queryForId(rr1.id);
		assertNotNull(result);
		assertNotNull(result.related);
		assertEquals(1, result.related.size());
		CloseableIterator<RecursiveReference> iterator = result.related.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(rr2.stuff, iterator.next().stuff);
		iterator.close();
	}

	@Test
	public void testCollectionsOfCollections() throws Exception {
		Dao<CollectionWithCollection1, Object> dao1 = createDao(CollectionWithCollection1.class, true);
		Dao<CollectionWithCollection2, Object> dao2 = createDao(CollectionWithCollection2.class, true);
		Dao<CollectionWithCollection3, Object> dao3 = createDao(CollectionWithCollection3.class, true);

		CollectionWithCollection1 coll1 = new CollectionWithCollection1();
		assertEquals(1, dao1.create(coll1));

		CollectionWithCollection2 coll2 = new CollectionWithCollection2();
		coll2.collectionWithCollection1 = coll1;
		assertEquals(1, dao2.create(coll2));

		CollectionWithCollection3 coll3 = new CollectionWithCollection3();
		coll3.collectionWithCollection2 = coll2;
		assertEquals(1, dao3.create(coll3));

		CollectionWithCollection1 collResult1 = dao1.queryForId(coll1.id);
		assertNotNull(collResult1.related);
		assertEquals(1, collResult1.related.size());
		CloseableIterator<CollectionWithCollection2> iterator1 = collResult1.related.iterator();
		assertTrue(iterator1.hasNext());
		assertEquals(coll2.id, iterator1.next().id);
		collResult1.related.closeLastIterator();
		
		CollectionWithCollection2 collResult2 = dao2.queryForId(coll2.id);
		assertNotNull(collResult2.related);
		assertEquals(1, collResult2.related.size());
		CloseableIterator<CollectionWithCollection3> iterator2 = collResult2.related.iterator();
		assertTrue(iterator2.hasNext());
		assertEquals(coll3.id, iterator2.next().id);
		collResult2.related.closeLastIterator();
	}

	/* =============================================================================================== */

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

	protected static class RecursiveReference {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		String stuff;
		@DatabaseField(foreign = true)
		RecursiveReference parent;
		@ForeignCollectionField
		ForeignCollection<RecursiveReference> related;
		protected RecursiveReference() {
		}
	}

	protected static class CollectionWithCollection1 {
		@DatabaseField(generatedId = true)
		int id;
		@ForeignCollectionField
		ForeignCollection<CollectionWithCollection2> related;
		protected CollectionWithCollection1() {
		}
	}

	protected static class CollectionWithCollection2 {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(foreign = true)
		CollectionWithCollection1 collectionWithCollection1;
		@ForeignCollectionField
		ForeignCollection<CollectionWithCollection3> related;
		protected CollectionWithCollection2() {
		}
	}

	protected static class CollectionWithCollection3 {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(foreign = true)
		CollectionWithCollection2 collectionWithCollection2;
		protected CollectionWithCollection3() {
		}
	}
}
