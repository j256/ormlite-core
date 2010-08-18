package com.j256.ormlite.misc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.concurrent.Callable;

import org.junit.Test;

import com.j256.ormlite.BaseOrmLiteTest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;

public class TransactionManagerTest extends BaseOrmLiteTest {

	@Test
	public void testDaoTransactionManagerCommitted() throws Exception {
		final Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		final Foo foo1 = new Foo();
		String stuff = "stuff";
		foo1.stuff = stuff;
		assertEquals(1, fooDao.create(foo1));
		TransactionManager mgr = new TransactionManager(connectionSource);
		final int returnVal = 284234832;
		int val = mgr.callInTransaction(new Callable<Integer>() {
			public Integer call() throws Exception {
				// we delete it inside a transaction
				assertEquals(1, fooDao.delete(foo1));
				// we can't find it
				assertNull(fooDao.queryForId(foo1.id));
				return returnVal;
			}
		});
		assertEquals(returnVal, val);

		// still doesn't find it after we delete it
		assertNull(fooDao.queryForId(foo1.id));
	}

	@Test
	public void testRollBack() throws Exception {
		if (connectionSource == null) {
			return;
		}
		TransactionManager mgr = new TransactionManager(connectionSource);
		testTransactionManager(mgr, new RuntimeException("What!!  I protest!!"));
	}

	@Test
	public void testSpringWiredRollBack() throws Exception {
		if (connectionSource == null) {
			return;
		}
		TransactionManager mgr = new TransactionManager();
		mgr.setConnectionSource(connectionSource);
		mgr.initialize();
		testTransactionManager(mgr, new RuntimeException("What!!  I protest!!"));
	}

	@Test
	public void testNonRuntimeExceptionWiredRollBack() throws Exception {
		if (connectionSource == null) {
			return;
		}
		TransactionManager mgr = new TransactionManager();
		mgr.setConnectionSource(connectionSource);
		mgr.initialize();
		testTransactionManager(mgr, new Exception("What!!  I protest via an Exception!!"));
	}

	private void testTransactionManager(TransactionManager mgr, final Exception exception) throws Exception {
		final Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		final Foo foo1 = new Foo();
		String stuff = "stuff";
		foo1.stuff = stuff;
		assertEquals(1, fooDao.create(foo1));
		try {
			mgr.callInTransaction(new Callable<Void>() {
				public Void call() throws Exception {
					// we delete it inside a transaction
					assertEquals(1, fooDao.delete(foo1));
					// we can't find it
					assertNull(fooDao.queryForId(foo1.id));
					// but then we throw an exception which rolls back the transaction
					throw exception;
				}
			});
			fail("Should have thrown");
		} catch (SQLException e) {
			// expected
		}

		// still finds it after we delete it
		Foo foo2 = fooDao.queryForId(foo1.id);
		assertNotNull(foo2);
		assertEquals(stuff, foo2.stuff);
	}

	public static class Foo {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		String stuff;
		Foo() {
			// for ormlite
		}
	}
}
