package com.j256.ormlite.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
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
		Dao<Account, Integer> accountDao = createLazyOrderDao();
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

	@Test
	public void testFieldOrder() throws Exception {
		Dao<AccountOrdered, Integer> accountDao = createDao(AccountOrdered.class, true);
		Dao<OrderOrdered, Integer> orderDao = createDao(OrderOrdered.class, true);

		AccountOrdered account1 = new AccountOrdered();
		String name1 = "fwepfjewfew";
		account1.name = name1;
		assertEquals(1, accountDao.create(account1));

		OrderOrdered order1 = new OrderOrdered();
		int val1 = 3;
		order1.val = val1;
		order1.account = account1;
		assertEquals(1, orderDao.create(order1));

		OrderOrdered order2 = new OrderOrdered();
		int val2 = 1;
		order2.val = val2;
		order2.account = account1;
		assertEquals(1, orderDao.create(order2));

		OrderOrdered order3 = new OrderOrdered();
		int val3 = 2;
		order3.val = val3;
		order3.account = account1;
		assertEquals(1, orderDao.create(order3));

		AccountOrdered accountResult = accountDao.queryForId(account1.id);
		assertEquals(name1, accountResult.name);
		assertNotNull(accountResult.orders);
		int orderC = 0;
		for (OrderOrdered order : accountResult.orders) {
			orderC++;
			switch (orderC) {
				case 1 :
					assertEquals(val2, order.val);
					break;
				case 2 :
					assertEquals(val3, order.val);
					break;
				case 3 :
					assertEquals(val1, order.val);
					break;
			}
		}
	}

	@Test
	public void testFieldOrderDesc() throws Exception {
		Dao<AccountOrderedDesc, Integer> accountDao = createDao(AccountOrderedDesc.class, true);
		Dao<OrderOrderedDesc, Integer> orderDao = createDao(OrderOrderedDesc.class, true);

		AccountOrderedDesc account1 = new AccountOrderedDesc();
		String name1 = "fwepfjewfew";
		account1.name = name1;
		assertEquals(1, accountDao.create(account1));

		OrderOrderedDesc order1 = new OrderOrderedDesc();
		int val1 = 3;
		order1.val = val1;
		order1.account = account1;
		assertEquals(1, orderDao.create(order1));

		OrderOrderedDesc order2 = new OrderOrderedDesc();
		int val2 = 1;
		order2.val = val2;
		order2.account = account1;
		assertEquals(1, orderDao.create(order2));

		OrderOrderedDesc order3 = new OrderOrderedDesc();
		int val3 = 2;
		order3.val = val3;
		order3.account = account1;
		assertEquals(1, orderDao.create(order3));

		AccountOrderedDesc accountResult = accountDao.queryForId(account1.id);
		assertEquals(name1, accountResult.name);
		assertNotNull(accountResult.orders);
		int orderC = 0;
		for (OrderOrderedDesc order : accountResult.orders) {
			orderC++;
			switch (orderC) {
				case 1 :
					assertEquals(val1, order.val);
					break;
				case 2 :
					assertEquals(val3, order.val);
					break;
				case 3 :
					assertEquals(val2, order.val);
					break;
			}
		}
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
		rr1.stuff = "fpeewihwjytjythgwofjwe";
		assertEquals(1, dao.create(rr1));
		rr1.parent = rr1;
		assertEquals(1, dao.update(rr1));

		RecursiveReference result = dao.queryForId(rr1.id);
		assertNotNull(result);
		assertNotNull(result.related);
		assertFalse(result.related.isEager());
		assertEquals(1, result.related.size());
		CloseableIterator<RecursiveReference> iterator;

		// this would keep going forever since it is lazy
		for (int i = 0; i < 10; i++) {
			iterator = result.related.closeableIterator();
			assertTrue(iterator.hasNext());
			result = iterator.next();
			assertEquals(rr1.stuff, result.stuff);
			// Nth level is not null and is lazy
			assertNotNull(result.related);
			assertFalse(result.related.isEager());
			iterator.close();
		}
	}

	@Test
	public void testRecursiveReferenceEager() throws Exception {
		Dao<RecursiveReferenceEager, Object> dao = createDao(RecursiveReferenceEager.class, true);
		RecursiveReferenceEager rr1 = new RecursiveReferenceEager();
		rr1.stuff = "fpeewihwh13132gwofjwe";
		assertEquals(1, dao.create(rr1));
		rr1.parent = rr1;
		assertEquals(1, dao.update(rr1));

		RecursiveReferenceEager result = dao.queryForId(rr1.id);
		assertNotNull(result);
		// 0th level is not null and is eager
		assertNotNull(result.related);
		assertTrue(result.related.isEager());
		assertEquals(1, result.related.size());
		CloseableIterator<RecursiveReferenceEager> iterator = result.related.closeableIterator();
		assertTrue(iterator.hasNext());
		RecursiveReferenceEager rrResult = iterator.next();
		assertEquals(rr1.stuff, rrResult.stuff);
		// 1st level is not null but is lazy
		assertNotNull(rrResult.related);
		assertFalse(rrResult.related.isEager());
		assertFalse(iterator.hasNext());
		iterator.close();
	}

	@Test
	public void testRecursiveReferenceEagerTwo() throws Exception {
		Dao<RecursiveReferenceEagerLevelTwo, Object> dao = createDao(RecursiveReferenceEagerLevelTwo.class, true);
		RecursiveReferenceEagerLevelTwo rr1 = new RecursiveReferenceEagerLevelTwo();
		rr1.stuff = "fpeewifwfwehwhgwofjwe";
		assertEquals(1, dao.create(rr1));
		rr1.parent = rr1;
		assertEquals(1, dao.update(rr1));

		RecursiveReferenceEagerLevelTwo result = dao.queryForId(rr1.id);
		assertNotNull(result);
		// 0th level is not null and is eager
		assertNotNull(result.related);
		assertTrue(result.related.isEager());
		assertEquals(1, result.related.size());

		CloseableIterator<RecursiveReferenceEagerLevelTwo> iterator = result.related.closeableIterator();
		assertTrue(iterator.hasNext());
		RecursiveReferenceEagerLevelTwo rrResult = iterator.next();
		assertEquals(rr1.stuff, rrResult.stuff);
		// 1st level is not null and is eager
		assertNotNull(rrResult.related);
		assertTrue(rrResult.related.isEager());
		assertFalse(iterator.hasNext());
		iterator.close();
		iterator = rrResult.related.closeableIterator();
		assertTrue(iterator.hasNext());
		rrResult = iterator.next();
		assertFalse(iterator.hasNext());
		// but the 2nd level is not null but is lazy
		assertNotNull(rrResult.related);
		assertFalse(rrResult.related.isEager());
		iterator.close();
	}

	@Test
	public void testRecursiveReferenceEagerZero() throws Exception {
		/*
		 * No reason to do this in reality. You might as well say eager = false.
		 */
		Dao<RecursiveReferenceEagerLevelZero, Object> dao = createDao(RecursiveReferenceEagerLevelZero.class, true);
		RecursiveReferenceEagerLevelZero rr1 = new RecursiveReferenceEagerLevelZero();
		rr1.stuff = "fpeewifwfwehwhgwofjwe";
		assertEquals(1, dao.create(rr1));
		rr1.parent = rr1;
		assertEquals(1, dao.update(rr1));

		RecursiveReferenceEagerLevelZero result = dao.queryForId(rr1.id);
		assertNotNull(result);
		// 0th level is not null but is lazy
		assertNotNull(result.related);
		assertFalse(result.related.isEager());
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
		CloseableIterator<CollectionWithCollection2> iterator1 = collResult1.related.closeableIterator();
		assertTrue(iterator1.hasNext());
		assertEquals(coll2.id, iterator1.next().id);
		collResult1.related.closeLastIterator();

		CollectionWithCollection2 collResult2 = dao2.queryForId(coll2.id);
		assertNotNull(collResult2.related);
		assertEquals(1, collResult2.related.size());
		CloseableIterator<CollectionWithCollection3> iterator2 = collResult2.related.closeableIterator();
		assertTrue(iterator2.hasNext());
		assertEquals(coll3.id, iterator2.next().id);
		collResult2.related.closeLastIterator();
	}

	@Test
	public void testEmptyCollection() throws Exception {
		Dao<Account, Object> accountDao = createDao(Account.class, true);
		createTable(Order.class, true);

		Account account = new Account();
		String name = "another name";
		account.name = name;
		account.orders = accountDao.getEmptyForeignCollection(Account.ORDERS_FIELD_NAME);
		assertEquals(1, accountDao.create(account));

		Order order = new Order();
		int val3 = 17097;
		order.val = val3;
		order.account = account;
		account.orders.add(order);
		assertEquals(1, account.orders.size());

		Account accountResult = accountDao.queryForId(account.id);
		assertNotNull(accountResult.orders);
		assertEquals(1, accountResult.orders.size());
		CloseableIterator<Order> iterator = accountResult.orders.closeableIterator();
		assertTrue(iterator.hasNext());
		Order orderResult = iterator.next();
		assertNotNull(orderResult);
		assertEquals(order.id, orderResult.id);
		assertFalse(iterator.hasNext());
		iterator.close();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUnknownEmptyCollection() throws Exception {
		Dao<Account, Object> dao = createDao(Account.class, true);
		dao.getEmptyForeignCollection("unknown field name");
	}

	@Test
	public void testLazyContainsAll() throws Exception {
		Dao<Account, Integer> accountDao = createLazyOrderDao();
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

		Order order3 = new Order();
		int val3 = 1524587097;
		order3.val = val3;
		order3.account = account1;

		Account result = accountDao.queryForId(account1.id);
		assertEquals(2, result.orders.size());

		assertTrue(result.orders.containsAll(Arrays.asList()));
		assertTrue(result.orders.containsAll(Arrays.asList(order1)));
		assertTrue(result.orders.containsAll(Arrays.asList(order1, order2)));
		assertTrue(result.orders.containsAll(Arrays.asList(order2, order1)));
		assertTrue(result.orders.containsAll(Arrays.asList(order1, order1)));
		assertTrue(result.orders.containsAll(Arrays.asList(order1, order1, order2, order2)));
		assertFalse(result.orders.containsAll(Arrays.asList(order1, order2, order3)));
		assertFalse(result.orders.containsAll(Arrays.asList(order3)));
		assertFalse(result.orders.containsAll(Arrays.asList(order3, order1, order2)));
	}

	@Test
	public void testNullForeign() throws Exception {
		Dao<OrderOrdered, Integer> orderDao = createDao(OrderOrdered.class, true);

		int numOrders = 10;
		for (int orderC = 0; orderC < numOrders; orderC++) {
			OrderOrdered order = new OrderOrdered();
			order.val = orderC;
			assertEquals(1, orderDao.create(order));
		}

		List<OrderOrdered> results = orderDao.queryBuilder().where().isNull(Order.ACCOUNT_FIELD_NAME).query();
		assertNotNull(results);
		assertEquals(numOrders, results.size());
	}

	@Test
	public void testSelectColumnsNullForeign() throws Exception {
		Dao<Account, Integer> accountDao = createDao(Account.class, true);
		Dao<Order, Integer> orderDao = createDao(Order.class, true);

		Account account = new Account();
		account.name = "fwejpojfpofewjo";
		assertEquals(1, accountDao.create(account));

		Order order = new Order();
		order.val = 1321312;
		order.account = account;
		assertEquals(1, orderDao.create(order));

		List<Account> results = accountDao.queryBuilder().where().eq(Account.ID_FIELD_NAME, account.id).query();
		assertEquals(1, results.size());
		assertEquals(account.name, results.get(0).name);
		assertNotNull(results.get(0).orders);
		assertEquals(1, results.get(0).orders.size());

		results =
				accountDao.queryBuilder()
						.selectColumns(Account.ID_FIELD_NAME, Account.NAME_FIELD_NAME)
						.where()
						.eq(Account.ID_FIELD_NAME, account.id)
						.query();
		assertEquals(1, results.size());
		assertEquals(account.name, results.get(0).name);
		// null because the orders are not in the select columns
		assertNull(results.get(0).orders);

		results =
				accountDao.queryBuilder()
						.selectColumns(Account.ID_FIELD_NAME, Account.NAME_FIELD_NAME, Account.ORDERS_FIELD_NAME)
						.where()
						.eq(Account.ID_FIELD_NAME, account.id)
						.query();
		assertEquals(1, results.size());
		assertEquals(account.name, results.get(0).name);
		assertNotNull(results.get(0).orders);
		assertEquals(1, results.get(0).orders.size());
	}

	@Test
	public void testMultipleForeignEager() throws Exception {
		Dao<EagerConnection, Integer> connectionDao = createDao(EagerConnection.class, true);
		Dao<EagerNode, Integer> nodeDao = createDao(EagerNode.class, true);

		EagerNode node1 = new EagerNode();
		String stuff1 = "fpowjfwfw";
		node1.stuff = stuff1;
		assertEquals(1, nodeDao.create(node1));
		EagerNode node2 = new EagerNode();
		String stuff2 = "fpofwjpowjfwfw";
		node2.stuff = stuff2;
		assertEquals(1, nodeDao.create(node2));

		EagerConnection conn1 = new EagerConnection();
		String stuff3 = "fpoffewjwjpowjfwfw";
		conn1.from = node1;
		conn1.to = node2;
		conn1.stuff = stuff3;
		assertEquals(1, connectionDao.create(conn1));
		EagerConnection conn2 = new EagerConnection();
		String stuff4 = "fpoffewjwjpowjfwfjpfeww";
		conn2.from = node2;
		conn2.to = node1;
		conn2.stuff = stuff4;
		assertEquals(1, connectionDao.create(conn2));

		boolean found1 = false;
		boolean found2 = false;
		for (EagerNode node : nodeDao.queryForAll()) {
			if (node.id == node1.id) {
				assertEquals(node1.stuff, node.stuff);
				EagerConnection[] connections = node.froms.toArray(new EagerConnection[0]);
				assertEquals(1, connections.length);
				assertEquals(conn1, connections[0]);
				assertEquals(conn1.stuff, connections[0].stuff);
				assertEquals(node1, connections[0].from);
				assertEquals(node2, connections[0].to);
				connections = node.tos.toArray(new EagerConnection[0]);
				assertEquals(1, connections.length);
				assertEquals(conn2, connections[0]);
				assertEquals(conn2.stuff, connections[0].stuff);
				assertEquals(node2, connections[0].from);
				assertEquals(node1, connections[0].to);
				found1 = true;
			} else if (node.id == node2.id) {
				assertEquals(node2.stuff, node.stuff);
				EagerConnection[] connections = node.froms.toArray(new EagerConnection[0]);
				assertEquals(1, connections.length);
				assertEquals(conn2, connections[0]);
				assertEquals(conn2.stuff, connections[0].stuff);
				assertEquals(node2, connections[0].from);
				assertEquals(node1, connections[0].to);
				connections = node.tos.toArray(new EagerConnection[0]);
				assertEquals(1, connections.length);
				assertEquals(conn1, connections[0]);
				assertEquals(conn1.stuff, connections[0].stuff);
				assertEquals(node1, connections[0].from);
				assertEquals(node2, connections[0].to);
				found2 = true;
			} else {
				fail("Unknown Node " + node.id);
			}
		}
		assertTrue(found1);
		assertTrue(found2);

		found1 = false;
		found2 = false;
		for (EagerConnection result : connectionDao.queryForAll()) {
			if (result.id == conn1.id) {
				assertEquals(conn1.stuff, result.stuff);
				assertEquals(node1, result.from);
				assertEquals(node2, result.to);
				found1 = true;
			} else if (result.id == conn2.id) {
				assertEquals(conn2.stuff, result.stuff);
				assertEquals(node2, result.from);
				assertEquals(node1, result.to);
				found2 = true;
			} else {
				fail("Unknown Connection " + result.id);
			}
		}
		assertTrue(found1);
		assertTrue(found2);
	}

	@Test
	public void testMultipleForeignLazy() throws Exception {
		Dao<LazyConnection, Integer> connectionDao = createDao(LazyConnection.class, true);
		Dao<LazyNode, Integer> nodeDao = createDao(LazyNode.class, true);

		LazyNode node1 = new LazyNode();
		String stuff1 = "fpowjfwfw";
		node1.stuff = stuff1;
		assertEquals(1, nodeDao.create(node1));
		LazyNode node2 = new LazyNode();
		String stuff2 = "fpofwjpowjfwfw";
		node2.stuff = stuff2;
		assertEquals(1, nodeDao.create(node2));

		LazyConnection conn1 = new LazyConnection();
		String stuff3 = "fpoffewjwjpowjfwfw";
		conn1.from = node1;
		conn1.to = node2;
		conn1.stuff = stuff3;
		assertEquals(1, connectionDao.create(conn1));
		LazyConnection conn2 = new LazyConnection();
		String stuff4 = "fpoffewjwjpowjfwfjpfeww";
		conn2.from = node2;
		conn2.to = node1;
		conn2.stuff = stuff4;
		assertEquals(1, connectionDao.create(conn2));

		boolean found1 = false;
		boolean found2 = false;
		for (LazyNode node : nodeDao.queryForAll()) {
			if (node.id == node1.id) {
				assertEquals(node1.stuff, node.stuff);
				LazyConnection[] connections = node.froms.toArray(new LazyConnection[0]);
				assertEquals(1, connections.length);
				assertEquals(conn1, connections[0]);
				assertEquals(conn1.stuff, connections[0].stuff);
				assertEquals(node1, connections[0].from);
				assertEquals(node2, connections[0].to);
				connections = node.tos.toArray(new LazyConnection[0]);
				assertEquals(1, connections.length);
				assertEquals(conn2, connections[0]);
				assertEquals(conn2.stuff, connections[0].stuff);
				assertEquals(node2, connections[0].from);
				assertEquals(node1, connections[0].to);
				found1 = true;
			} else if (node.id == node2.id) {
				assertEquals(node2.stuff, node.stuff);
				LazyConnection[] connections = node.froms.toArray(new LazyConnection[0]);
				assertEquals(1, connections.length);
				assertEquals(conn2, connections[0]);
				assertEquals(conn2.stuff, connections[0].stuff);
				assertEquals(node2, connections[0].from);
				assertEquals(node1, connections[0].to);
				connections = node.tos.toArray(new LazyConnection[0]);
				assertEquals(1, connections.length);
				assertEquals(conn1, connections[0]);
				assertEquals(conn1.stuff, connections[0].stuff);
				assertEquals(node1, connections[0].from);
				assertEquals(node2, connections[0].to);
				found2 = true;
			} else {
				fail("Unknown Node " + node.id);
			}
		}
		assertTrue(found1);
		assertTrue(found2);

		found1 = false;
		found2 = false;
		for (LazyConnection result : connectionDao.queryForAll()) {
			if (result.id == conn1.id) {
				assertEquals(conn1.stuff, result.stuff);
				assertEquals(node1, result.from);
				assertEquals(node2, result.to);
				found1 = true;
			} else if (result.id == conn2.id) {
				assertEquals(conn2.stuff, result.stuff);
				assertEquals(node2, result.from);
				assertEquals(node1, result.to);
				found2 = true;
			} else {
				fail("Unknown Connection " + result.id);
			}
		}
		assertTrue(found1);
		assertTrue(found2);
	}

	@Test
	public void testForeignLinkage() throws Exception {
		Dao<EagerConnection, Integer> multipleDao = createDao(EagerConnection.class, true);
		Dao<EagerNode, Integer> foreignDao = createDao(EagerNode.class, true);

		EagerNode foreign1 = new EagerNode();
		assertEquals(1, foreignDao.create(foreign1));
		EagerNode foreign2 = new EagerNode();
		assertEquals(1, foreignDao.create(foreign2));

		EagerConnection multiple1 = new EagerConnection();
		multiple1.from = foreign1;
		multiple1.to = foreign2;
		assertEquals(1, multipleDao.create(multiple1));

		EagerNode result = foreignDao.queryForId(foreign1.id);
		EagerConnection[] array = result.froms.toArray(new EagerConnection[result.froms.size()]);
		assertEquals(1, array.length);
		assertSame(result, array[0].from);
	}

	@Test
	public void testForeignLinkageWithCache() throws Exception {
		Dao<EagerConnection, Integer> multipleDao = createDao(EagerConnection.class, true);
		multipleDao.setObjectCache(true);
		Dao<EagerNode, Integer> foreignDao = createDao(EagerNode.class, true);
		foreignDao.setObjectCache(true);

		EagerNode foreign1 = new EagerNode();
		assertEquals(1, foreignDao.create(foreign1));
		EagerNode foreign2 = new EagerNode();
		assertEquals(1, foreignDao.create(foreign2));

		EagerConnection multiple1 = new EagerConnection();
		multiple1.from = foreign1;
		multiple1.to = foreign2;
		assertEquals(1, multipleDao.create(multiple1));

		EagerNode result = foreignDao.queryForId(foreign1.id);
		assertNotNull(result.froms);
		EagerConnection[] array = result.froms.toArray(new EagerConnection[result.froms.size()]);
		assertEquals(1, array.length);
		assertSame(foreign1, array[0].from);
	}

	@Test(expected = SQLException.class)
	public void testMultipleForeignUnknownField() throws Exception {
		createDao(InvalidColumnNameForeign.class, true);
	}

	@Test
	public void testForeignCollectionCache() throws Exception {
		Dao<Account, Integer> accountDao = createDao(Account.class, true);
		Dao<Order, Integer> orderDao = createDao(Order.class, true);

		Account account = new Account();
		account.name = "fwejpojfpofewjo";
		assertEquals(1, accountDao.create(account));

		Order order = new Order();
		order.val = 1321312;
		order.account = account;
		assertEquals(1, orderDao.create(order));

		// turn on the cache afterwards
		orderDao.setObjectCache(true);

		Account result = accountDao.queryForId(account.id);
		assertNotSame(account, result);
		Order[] orders = result.orders.toArray(new Order[0]);
		assertEquals(1, orders.length);
		// inserted order is not in the cache
		assertNotSame(order, orders[0]);

		Account result2 = accountDao.queryForId(account.id);
		assertNotSame(result, result2);
		Order[] orders2 = result.orders.toArray(new Order[0]);
		assertEquals(1, orders.length);
		// inserted order is not in the cache
		assertNotSame(order, orders2[0]);
		// but the order from the collection is now in the cache
		assertSame(orders[0], orders2[0]);
	}

	@Test
	public void testEagerCollectionIterator() throws Exception {
		Dao<Account, Integer> accountDao = createDao(Account.class, true);
		Dao<Order, Integer> orderDao = createDao(Order.class, true);

		Account account = new Account();
		account.name = "fwejpojfpofewjo";
		assertEquals(1, accountDao.create(account));

		Order order1 = new Order();
		order1.account = account;
		order1.val = 1;
		assertEquals(1, orderDao.create(order1));
		Order order2 = new Order();
		order2.account = account;
		order2.val = 2;
		assertEquals(1, orderDao.create(order2));

		Account result = accountDao.queryForId(account.id);
		assertEquals(2, result.orders.size());
		CloseableIterator<Order> iterator = result.orders.iteratorThrow();
		assertEquals(order1, iterator.first());
		assertEquals(order1, iterator.first());
		assertEquals(order1, iterator.current());
		assertEquals(order2, iterator.next());
		assertEquals(order2, iterator.current());
		assertEquals(order1, iterator.previous());
		assertEquals(order2, iterator.next());
		assertEquals(order1, iterator.moveRelative(-1));
		assertEquals(order1, iterator.moveRelative(0));
		assertEquals(order2, iterator.next());
		assertFalse(iterator.hasNext());
		assertNull(iterator.nextThrow());
		assertNull(iterator.moveRelative(1));
		assertNull(iterator.previous());
		assertEquals(order1, iterator.first());
		iterator.closeQuietly();
	}

	@Test
	public void testEagerCollectionIteratorRemove() throws Exception {
		Dao<Account, Integer> accountDao = createDao(Account.class, true);
		Dao<Order, Integer> orderDao = createDao(Order.class, true);

		Account account = new Account();
		account.name = "fwejpojfpofewjo";
		assertEquals(1, accountDao.create(account));

		Order order1 = new Order();
		order1.account = account;
		order1.val = 1;
		assertEquals(1, orderDao.create(order1));
		Order order2 = new Order();
		order2.account = account;
		order2.val = 2;
		assertEquals(1, orderDao.create(order2));

		Account result = accountDao.queryForId(account.id);

		// test remove of the certain element and iterator's behavior after that
		Order order3 = new Order();
		order3.account = account;
		order3.val = 3;
		assertEquals(1, orderDao.create(order3));
		accountDao.refresh(result);

		CloseableIterator<Order> iterator = result.orders.iteratorThrow();
		assertEquals(order1, iterator.next());
		assertEquals(order2, iterator.next());
		iterator.remove();
		assertEquals(order3, iterator.next());
		assertEquals(order1, iterator.previous());
		iterator.closeQuietly();
	}

	@Test
	public void testLazyCollectionIteratorRemove() throws Exception {
		Dao<LazyNode, Integer> nodeDao = createDao(LazyNode.class, true);
		Dao<LazyConnection, Integer> connDao = createDao(LazyConnection.class, true);

		LazyNode node = new LazyNode();
		node.stuff = "fwejpojfpofewjo";
		assertEquals(1, nodeDao.create(node));

		LazyConnection conn1 = new LazyConnection();
		conn1.to = node;
		conn1.stuff = "1231331";
		assertEquals(1, connDao.create(conn1));
		LazyConnection conn2 = new LazyConnection();
		conn2.to = node;
		conn2.stuff = "fwgregreegr";
		assertEquals(1, connDao.create(conn2));
		LazyConnection conn3 = new LazyConnection();
		conn3.to = node;
		conn3.stuff = "beehergregr";
		assertEquals(1, connDao.create(conn3));

		LazyNode result = nodeDao.queryForId(node.id);
		CloseableIterator<LazyConnection> iterator = result.tos.iteratorThrow(ResultSet.TYPE_SCROLL_INSENSITIVE);
		assertTrue(iterator.hasNext());
		assertEquals(conn1, iterator.next());
		assertTrue(iterator.hasNext());
		assertEquals(conn2, iterator.next());
		iterator.remove();
		assertTrue(iterator.hasNext());
		assertEquals(conn3, iterator.next());
		/*
		 * NOTE: this is conn2 in the lazy test because the cursor sees the data that has been removed underneath the
		 * covers. Not sure how to fix this.
		 */
		assertEquals(conn2, iterator.previous());
		iterator.closeQuietly();
	}

	@Test
	public void testEagerFirstNone() throws Exception {
		Dao<Account, Integer> accountDao = createDao(Account.class, true);
		createTable(Order.class, true);

		Account account = new Account();
		account.name = "fwejpojfpofewjo";
		assertEquals(1, accountDao.create(account));

		Account result = accountDao.queryForId(account.id);
		assertEquals(0, result.orders.size());
		CloseableIterator<Order> iterator = result.orders.iteratorThrow();
		assertNull(iterator.first());
	}

	@Test
	public void testEagerCurrentNone() throws Exception {
		Dao<Account, Integer> accountDao = createDao(Account.class, true);
		createTable(Order.class, true);

		Account account = new Account();
		account.name = "fwejpojfpofewjo";
		assertEquals(1, accountDao.create(account));

		Account result = accountDao.queryForId(account.id);
		assertEquals(0, result.orders.size());
		CloseableIterator<Order> iterator = result.orders.iteratorThrow();
		assertNull(iterator.current());
	}

	@Test
	public void testForeignCollectionWithForeignCollection() throws Exception {
		Dao<First, Integer> firstDao = createDao(First.class, true);
		firstDao.setObjectCache(true);
		Dao<Second, Integer> secondDao = createDao(Second.class, true);
		secondDao.setObjectCache(true);
		Dao<Third, Integer> thirdDao = createDao(Third.class, true);
		thirdDao.setObjectCache(true);

		First first = new First();
		assertEquals(1, firstDao.create(first));

		Second second = new Second();
		second.first = first;
		assertEquals(1, secondDao.create(second));

		Third third = new Third();
		third.second = second;
		third.stuff = "owejfpweofj";
		assertEquals(1, thirdDao.create(third));

		First firstResult = firstDao.queryForId(first.id);
		assertNotNull(firstResult);
		assertNotNull(firstResult.seconds);
		Second[] secondResults = firstResult.seconds.toArray(new Second[0]);
		assertEquals(1, secondResults.length);
		Second secondResult = secondResults[0];
		assertEquals(second.id, secondResult.id);
		assertNotNull(secondResult.thirds);
		Third[] thirdResults = secondResult.thirds.toArray(new Third[0]);
		assertEquals(1, thirdResults.length);
		Third thirdResult = thirdResults[0];
		assertEquals(third.id, thirdResult.id);
		assertEquals(third.stuff, thirdResult.stuff);
	}

	@Test
	public void testForeignCollectionMultipleForeign() throws Exception {
		Dao<ForeignFieldName, Object> nameDao = createDao(ForeignFieldName.class, true);
		Dao<ForeignFieldNameForeign, Object> foreignDao = createDao(ForeignFieldNameForeign.class, true);

		ForeignFieldNameForeign f1 = new ForeignFieldNameForeign();
		f1.stuff = "fjpowejfwef";
		assertEquals(1, foreignDao.create(f1));
		ForeignFieldNameForeign f2 = new ForeignFieldNameForeign();
		f2.stuff = "efefefefe";
		assertEquals(1, foreignDao.create(f2));

		ForeignFieldName name1 = new ForeignFieldName();
		name1.stuff = "ewpojfwepfjwe";
		name1.foreign1 = f1;
		name1.foreign2 = f2;
		assertEquals(1, nameDao.create(name1));
		ForeignFieldName name2 = new ForeignFieldName();
		name2.stuff = "2131232312";
		name2.foreign1 = f2;
		name2.foreign2 = f1;
		assertEquals(1, nameDao.create(name2));

		ForeignFieldNameForeign result = foreignDao.queryForId(f1.id);
		ForeignFieldName[] f1s = result.f1s.toArray(new ForeignFieldName[1]);
		ForeignFieldName[] f2s = result.f2s.toArray(new ForeignFieldName[1]);
		assertEquals(1, f1s.length);
		assertEquals(name1.stuff, f1s[0].stuff);
		assertEquals(1, f2s.length);
		assertEquals(name2.stuff, f2s[0].stuff);
	}

	@Test
	public void testAddNoForeign() throws Exception {
		createTable(Order.class, true);
		Dao<Account, Integer> accountDao = createDao(Account.class, true);

		Account account = new Account();
		String name = "fwepfjewfew";
		account.name = name;
		assertEquals(1, accountDao.create(account));
		accountDao.assignEmptyForeignCollection(account, Account.ORDERS_FIELD_NAME);

		Order order1 = new Order();
		order1.val = 1453783141;
		account.orders.add(order1);

		Order order2 = new Order();
		order2.val = 247895295;
		account.orders.add(order2);

		Account result = accountDao.queryForId(account.id);
		assertNotNull(result);

		Iterator<Order> iterator = result.orders.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(order1.val, iterator.next().val);
		assertTrue(iterator.hasNext());
		assertEquals(order2.val, iterator.next().val);
		assertFalse(iterator.hasNext());
	}

	@Test
	public void testAddDontOverwriteForeign() throws Exception {
		createTable(Order.class, true);
		Dao<Account, Integer> accountDao = createDao(Account.class, true);

		Account account1 = new Account();
		account1.name = "fwepfjewfew";
		assertEquals(1, accountDao.create(account1));
		accountDao.assignEmptyForeignCollection(account1, Account.ORDERS_FIELD_NAME);

		Account account2 = new Account();
		account2.name = "fhwoifwepfjewfew";
		assertEquals(1, accountDao.create(account2));

		Order order1 = new Order();
		order1.val = 1453783141;
		order1.account = account2;
		account1.orders.add(order1);

		Order order2 = new Order();
		order2.val = 247895295;
		order2.account = account2;
		account1.orders.add(order2);

		Account result = accountDao.queryForId(account1.id);
		assertNotNull(result);

		// we don't find any because they were alreayd set with account2's id
		Iterator<Order> iterator = result.orders.iterator();
		assertFalse(iterator.hasNext());
	}

	@Test
	public void testForeignCollectionSameAutoRefresh() throws Exception {
		Dao<ForeignAutoRefreshSame, Integer> sameDao = createDao(ForeignAutoRefreshSame.class, true);
		Dao<ForeignAutoRefreshSameCollection, Integer> collectionDao =
				createDao(ForeignAutoRefreshSameCollection.class, true);

		ForeignAutoRefreshSameCollection foreign = new ForeignAutoRefreshSameCollection();
		foreign.val = 428524234;
		assertEquals(1, collectionDao.create(foreign));

		ForeignAutoRefreshSame same1 = new ForeignAutoRefreshSame();
		same1.foreign = foreign;
		assertEquals(1, sameDao.create(same1));
		ForeignAutoRefreshSame same2 = new ForeignAutoRefreshSame();
		same2.foreign = foreign;
		assertEquals(1, sameDao.create(same2));

		ForeignAutoRefreshSameCollection result = collectionDao.queryForId(foreign.id);
		assertNotNull(result);
		assertNotNull(result.collection);

		Iterator<ForeignAutoRefreshSame> iterator = result.collection.iterator();
		assertTrue(iterator.hasNext());
		ForeignAutoRefreshSame sameResult = iterator.next();
		assertSame(result, sameResult.foreign);
		assertEquals(foreign.val, sameResult.foreign.val);
		sameResult = iterator.next();
		assertSame(result, sameResult.foreign);
		assertEquals(foreign.val, sameResult.foreign.val);
		assertFalse(iterator.hasNext());
	}

	/* =============================================================================================== */

	private void testCollection(Dao<Account, Integer> accountDao, boolean eager) throws Exception {
		Dao<Order, Integer> orderDao = createDao(Order.class, true);

		Account accountOther = new Account();
		String nameOther = "fwepfjewfew";
		accountOther.name = nameOther;
		assertEquals(1, accountDao.create(accountOther));

		Order orderOther = new Order();
		int valOther = 1453783141;
		orderOther.val = valOther;
		orderOther.account = accountOther;
		assertEquals(1, orderDao.create(orderOther));

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
		account2.name = name2;
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
			assertSame(accountResult, order.account);
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

		CloseableWrappedIterable<Order> wrapped = accountResult.orders.getWrappedIterable();
		try {
			orderC = 0;
			for (Order order : wrapped) {
				orderC++;
				switch (orderC) {
					case 1 :
						assertEquals(val1, order.val);
						break;
					case 2 :
						assertEquals(val2, order.val);
						break;
				}
				assertSame(accountResult, order.account);
			}
			assertEquals(2, orderC);
		} finally {
			wrapped.close();
		}

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

		CloseableIterator<Order> iterator = accountResult.orders.closeableIterator();
		assertTrue(iterator.hasNext());
		assertEquals(order1, iterator.next());
		iterator.remove();
		assertFalse(iterator.hasNext());
		iterator.close();
		assertEquals(0, accountResult.orders.size());

		accountResult.orders.addAll(Arrays.asList(order1, order2, order5, order6));

		orderC = 0;
		boolean gotOrder5 = false;
		boolean gotOrder6 = false;
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
					if ((!gotOrder5) && order.val == val5) {
						gotOrder5 = true;
					} else if ((!gotOrder6) && order.val == val6) {
						gotOrder6 = true;
					} else {
						fail("Should have gotten order5 or order6");
					}
					break;
				case 4 :
					if ((!gotOrder5) && order.val == val5) {
						gotOrder5 = true;
					} else if ((!gotOrder6) && order.val == val6) {
						gotOrder6 = true;
					} else {
						fail("Should have gotten order5 or order6");
					}
					break;
			}
		}
		assertEquals(4, orderC);
		assertTrue(gotOrder5);
		assertTrue(gotOrder6);

		assertEquals(4, accountResult.orders.size());
		accountResult.orders.clear();
		assertEquals(0, accountResult.orders.size());
		accountDao.refresh(accountResult);
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

		Account result = accountDao.queryForId(accountOther.id);
		iterator = result.orders.closeableIterator();
		assertTrue(iterator.hasNext());
		assertEquals(valOther, iterator.next().val);
		assertFalse(iterator.hasNext());
		iterator.close();

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ObjectOutputStream objectOut = new ObjectOutputStream(outputStream);
		objectOut.writeUnshared(result);
		objectOut.close();

		ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
		ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
		result = (Account) objectInputStream.readObject();

		if (eager) {
			assertEquals(1, result.orders.size());
		} else {
			try {
				result.orders.size();
				fail("This should have thrown");
			} catch (IllegalStateException e) {
				// expected
			}
		}
	}

	private Dao<Account, Integer> createLazyOrderDao() throws SQLException, Exception {
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
		return accountDao;
	}

	/* =============================================================================================== */

	protected static class Account implements Serializable {
		private static final long serialVersionUID = 6635908110232002380L;
		public static final String ID_FIELD_NAME = "id";
		public static final String NAME_FIELD_NAME = "name";
		public static final String ORDERS_FIELD_NAME = "orders123";
		@DatabaseField(generatedId = true, columnName = ID_FIELD_NAME)
		int id;
		@DatabaseField(columnName = NAME_FIELD_NAME)
		String name;
		@ForeignCollectionField(eager = true, columnName = ORDERS_FIELD_NAME)
		ForeignCollection<Order> orders;
		protected Account() {
		}
	}

	protected static class Order implements Serializable {
		private static final long serialVersionUID = 4917817147937431643L;
		public static final String ACCOUNT_FIELD_NAME = "account_id";
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(unique = true)
		int val;
		@DatabaseField(foreign = true, columnName = ACCOUNT_FIELD_NAME)
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

	protected static class RecursiveReferenceEager {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		String stuff;
		@DatabaseField(foreign = true)
		RecursiveReferenceEager parent;
		@ForeignCollectionField(eager = true)
		ForeignCollection<RecursiveReferenceEager> related;
		protected RecursiveReferenceEager() {
		}
	}

	protected static class RecursiveReferenceEagerLevelTwo {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		String stuff;
		@DatabaseField(foreign = true)
		RecursiveReferenceEagerLevelTwo parent;
		@ForeignCollectionField(eager = true, maxEagerLevel = 2)
		ForeignCollection<RecursiveReferenceEagerLevelTwo> related;
		protected RecursiveReferenceEagerLevelTwo() {
		}
	}

	protected static class RecursiveReferenceEagerLevelZero {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		String stuff;
		@DatabaseField(foreign = true)
		RecursiveReferenceEagerLevelZero parent;
		@ForeignCollectionField(eager = true, maxEagerLevel = 0)
		ForeignCollection<RecursiveReferenceEagerLevelZero> related;
		protected RecursiveReferenceEagerLevelZero() {
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

	protected static class AccountOrdered {
		public static final String ORDERS_FIELD_NAME = "orders123";
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		String name;
		@ForeignCollectionField(eager = true, columnName = ORDERS_FIELD_NAME,
				orderColumnName = OrderOrdered.VAL_FIELD_NAME)
		ForeignCollection<OrderOrdered> orders;
		protected AccountOrdered() {
		}
	}

	protected static class OrderOrdered {
		public static final String ACCOUNT_FIELD_NAME = "account_id";
		public final static String VAL_FIELD_NAME = "val";
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(unique = true, columnName = VAL_FIELD_NAME)
		int val;
		@DatabaseField(foreign = true, columnName = ACCOUNT_FIELD_NAME)
		AccountOrdered account;
		protected OrderOrdered() {
		}
	}

	protected static class AccountOrderedDesc {
		public static final String ORDERS_FIELD_NAME = "orders123";
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		String name;
		@ForeignCollectionField(eager = true, columnName = ORDERS_FIELD_NAME,
				orderColumnName = OrderOrdered.VAL_FIELD_NAME, orderAscending = false)
		ForeignCollection<OrderOrderedDesc> orders;
		protected AccountOrderedDesc() {
		}
	}

	protected static class OrderOrderedDesc {
		public static final String ACCOUNT_FIELD_NAME = "account_id";
		public final static String VAL_FIELD_NAME = "val";
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(unique = true, columnName = VAL_FIELD_NAME)
		int val;
		@DatabaseField(foreign = true, columnName = ACCOUNT_FIELD_NAME)
		AccountOrderedDesc account;
		protected OrderOrderedDesc() {
		}
	}

	protected static class EagerNode {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		String stuff;
		@ForeignCollectionField(eager = true, foreignFieldName = "from")
		ForeignCollection<EagerConnection> froms;
		@ForeignCollectionField(eager = true, foreignFieldName = "to")
		ForeignCollection<EagerConnection> tos;
		public EagerNode() {
		}
		@Override
		public int hashCode() {
			return id;
		}
		@Override
		public boolean equals(Object obj) {
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			return id == ((EagerNode) obj).id;
		}
		@Override
		public String toString() {
			return getClass().getSimpleName() + " #" + id;
		}
	}

	protected static class EagerConnection {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		String stuff;
		@DatabaseField(foreign = true)
		EagerNode from;
		@DatabaseField(foreign = true)
		EagerNode to;
		public EagerConnection() {
		}
		@Override
		public int hashCode() {
			return id;
		}
		@Override
		public boolean equals(Object obj) {
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			return id == ((EagerConnection) obj).id;
		}
		@Override
		public String toString() {
			return getClass().getSimpleName() + " #" + id;
		}
	}

	protected static class LazyNode {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		String stuff;
		@ForeignCollectionField(eager = false, foreignFieldName = "from")
		ForeignCollection<LazyConnection> froms;
		@ForeignCollectionField(eager = false, foreignFieldName = "to")
		ForeignCollection<LazyConnection> tos;
		public LazyNode() {
		}
		@Override
		public int hashCode() {
			return id;
		}
		@Override
		public boolean equals(Object obj) {
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			return id == ((LazyNode) obj).id;
		}
		@Override
		public String toString() {
			return getClass().getSimpleName() + " #" + id;
		}
	}

	protected static class LazyConnection {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		String stuff;
		@DatabaseField(foreign = true)
		LazyNode from;
		@DatabaseField(foreign = true)
		LazyNode to;
		public LazyConnection() {
		}
		@Override
		public int hashCode() {
			return id;
		}
		@Override
		public boolean equals(Object obj) {
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			return id == ((LazyConnection) obj).id;
		}
		@Override
		public String toString() {
			return getClass().getSimpleName() + " #" + id;
		}
	}

	protected static class InvalidColumnNameForeign {
		@DatabaseField(generatedId = true)
		int id;
		@ForeignCollectionField(eager = true, foreignFieldName = "unknowncolumn")
		ForeignCollection<InvalidColumnName> froms;
		public InvalidColumnNameForeign() {
		}
	}

	protected static class InvalidColumnName {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(foreign = true)
		InvalidColumnNameForeign foreign;
		public InvalidColumnName() {
		}
	}

	protected static class First {
		@DatabaseField(generatedId = true)
		int id;
		@ForeignCollectionField(eager = false)
		ForeignCollection<Second> seconds;
		public First() {
		}
	}

	protected static class Second {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(foreign = true)
		First first;
		@ForeignCollectionField(eager = false)
		ForeignCollection<Third> thirds;
		public Second() {
		}
	}

	protected static class Third {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(foreign = true)
		Second second;
		@DatabaseField
		String stuff;
		public Third() {
		}
	}

	protected static class ForeignFieldName {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		String stuff;
		@DatabaseField(foreign = true, columnName = "f1")
		ForeignFieldNameForeign foreign1;
		@DatabaseField(foreign = true, columnName = "f2")
		ForeignFieldNameForeign foreign2;
		public ForeignFieldName() {
		}
	}

	protected static class ForeignFieldNameForeign {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		String stuff;
		@ForeignCollectionField(foreignFieldName = "foreign1")
		ForeignCollection<ForeignFieldName> f1s;
		@ForeignCollectionField(foreignFieldName = "foreign2")
		ForeignCollection<ForeignFieldName> f2s;
		public ForeignFieldNameForeign() {
		}
	}

	protected static class ForeignAutoRefreshSame {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		String stuff;
		@DatabaseField(foreign = true/* , foreignAutoRefresh = true */)
		ForeignAutoRefreshSameCollection foreign;
		public ForeignAutoRefreshSame() {
		}
	}

	protected static class ForeignAutoRefreshSameCollection {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		int val;
		@ForeignCollectionField(eager = true)
		ForeignCollection<ForeignAutoRefreshSame> collection;
		public ForeignAutoRefreshSameCollection() {
		}
	}
}
