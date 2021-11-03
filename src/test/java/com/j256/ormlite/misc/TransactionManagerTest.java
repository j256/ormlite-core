package com.j256.ormlite.misc;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;

public class TransactionManagerTest extends BaseCoreTest {

	@Test
	public void testTransactionManager() throws Exception {
		ConnectionSource connectionSource = createMock(ConnectionSource.class);
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		expect(conn.isAutoCommitSupported()).andReturn(false);
		Savepoint savePoint = createMock(Savepoint.class);
		expect(savePoint.getSavepointName()).andReturn("name").anyTimes();
		expect(conn.setSavePoint(isA(String.class))).andReturn(savePoint);
		conn.commit(savePoint);
		expect(connectionSource.getDatabaseType()).andReturn(databaseType);
		expect(connectionSource.getReadWriteConnection(null)).andReturn(conn);
		expect(connectionSource.saveSpecialConnection(conn)).andReturn(true);
		connectionSource.clearSpecialConnection(conn);
		connectionSource.releaseConnection(conn);
		replay(connectionSource, conn, savePoint);
		TransactionManager tm = new TransactionManager(connectionSource);
		tm.callInTransaction(new Callable<Void>() {
			@Override
			public Void call() {
				return null;
			}
		});
		verify(connectionSource, conn, savePoint);
	}

	@Test
	public void testTransactionManagerTableName() throws Exception {
		ConnectionSource connectionSource = createMock(ConnectionSource.class);
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		expect(conn.isAutoCommitSupported()).andReturn(false);
		Savepoint savePoint = createMock(Savepoint.class);
		expect(savePoint.getSavepointName()).andReturn("name").anyTimes();
		expect(conn.setSavePoint(isA(String.class))).andReturn(savePoint);
		conn.commit(savePoint);
		expect(connectionSource.getDatabaseType()).andReturn(databaseType);
		expect(connectionSource.getReadWriteConnection(FOO_TABLE_NAME)).andReturn(conn);
		expect(connectionSource.saveSpecialConnection(conn)).andReturn(true);
		connectionSource.clearSpecialConnection(conn);
		connectionSource.releaseConnection(conn);
		replay(connectionSource, conn, savePoint);
		TransactionManager tm = new TransactionManager(connectionSource);
		tm.callInTransaction(FOO_TABLE_NAME, new Callable<Void>() {
			@Override
			public Void call() {
				return null;
			}
		});
		verify(connectionSource, conn, savePoint);
	}

	@Test
	public void testTransactionManagerSavePointNull() throws Exception {
		ConnectionSource connectionSource = createMock(ConnectionSource.class);
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		expect(conn.isAutoCommitSupported()).andReturn(false);
		expect(conn.setSavePoint(isA(String.class))).andReturn(null);
		conn.commit(null);
		expect(connectionSource.getDatabaseType()).andReturn(databaseType);
		expect(connectionSource.getReadWriteConnection(null)).andReturn(conn);
		expect(connectionSource.saveSpecialConnection(conn)).andReturn(true);
		connectionSource.clearSpecialConnection(conn);
		connectionSource.releaseConnection(conn);
		replay(connectionSource, conn);
		TransactionManager tm = new TransactionManager(connectionSource);
		tm.callInTransaction(new Callable<Void>() {
			@Override
			public Void call() {
				return null;
			}
		});
		verify(connectionSource, conn);
	}

	@Test
	public void testTransactionManagerRollback() throws Exception {
		ConnectionSource connectionSource = createMock(ConnectionSource.class);
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		expect(conn.isAutoCommitSupported()).andReturn(false);
		Savepoint savePoint = createMock(Savepoint.class);
		expect(savePoint.getSavepointName()).andReturn("name").anyTimes();
		expect(conn.setSavePoint(isA(String.class))).andReturn(savePoint);
		conn.rollback(savePoint);
		expect(connectionSource.getDatabaseType()).andReturn(databaseType);
		expect(connectionSource.getReadWriteConnection(null)).andReturn(conn);
		expect(connectionSource.saveSpecialConnection(conn)).andReturn(true);
		connectionSource.clearSpecialConnection(conn);
		connectionSource.releaseConnection(conn);
		replay(connectionSource, conn, savePoint);
		TransactionManager tm = new TransactionManager(connectionSource);
		try {
			tm.callInTransaction(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					throw new SQLException("you better roll back!!");
				}
			});
			fail("expected an exception");
		} catch (SQLException e) {
			// expected
		}
		verify(connectionSource, conn, savePoint);
	}

	@Test
	public void testTransactionManagerRollbackNullSavePoint() throws Exception {
		ConnectionSource connectionSource = createMock(ConnectionSource.class);
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		expect(conn.isAutoCommitSupported()).andReturn(false);
		expect(conn.setSavePoint(isA(String.class))).andReturn(null);
		conn.rollback(null);
		expect(connectionSource.getDatabaseType()).andReturn(databaseType);
		expect(connectionSource.getReadWriteConnection(null)).andReturn(conn);
		expect(connectionSource.saveSpecialConnection(conn)).andReturn(true);
		connectionSource.clearSpecialConnection(conn);
		connectionSource.releaseConnection(conn);
		replay(connectionSource, conn);
		TransactionManager tm = new TransactionManager(connectionSource);
		try {
			tm.callInTransaction(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					throw new SQLException("you better roll back!!");
				}
			});
			fail("expected an exception");
		} catch (SQLException e) {
			// expected
		}
		verify(connectionSource, conn);
	}

	@Test
	public void testTransactionManagerRollbackOtherException() throws Exception {
		ConnectionSource connectionSource = createMock(ConnectionSource.class);
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		expect(conn.isAutoCommitSupported()).andReturn(false);
		Savepoint savePoint = createMock(Savepoint.class);
		expect(savePoint.getSavepointName()).andReturn("name").anyTimes();
		expect(conn.setSavePoint(isA(String.class))).andReturn(savePoint);
		conn.rollback(savePoint);
		expect(connectionSource.getDatabaseType()).andReturn(databaseType);
		expect(connectionSource.getReadWriteConnection(null)).andReturn(conn);
		expect(connectionSource.saveSpecialConnection(conn)).andReturn(true);
		connectionSource.clearSpecialConnection(conn);
		connectionSource.releaseConnection(conn);
		replay(connectionSource, conn, savePoint);
		TransactionManager tm = new TransactionManager(connectionSource);
		try {
			tm.callInTransaction(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					throw new Exception("you better roll back!!");
				}
			});
			fail("expected an exception");
		} catch (Exception e) {
			// expected
		}
		verify(connectionSource, conn, savePoint);
	}

	@Test
	public void testTransactionManagerAutoCommitSupported() throws Exception {
		ConnectionSource connectionSource = createMock(ConnectionSource.class);
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		expect(conn.isAutoCommitSupported()).andReturn(true);
		expect(conn.isAutoCommit()).andReturn(false);
		Savepoint savePoint = createMock(Savepoint.class);
		expect(savePoint.getSavepointName()).andReturn("name").anyTimes();
		expect(conn.setSavePoint(isA(String.class))).andReturn(savePoint);
		conn.commit(savePoint);
		expect(connectionSource.getDatabaseType()).andReturn(databaseType);
		expect(connectionSource.getReadWriteConnection(null)).andReturn(conn);
		expect(connectionSource.saveSpecialConnection(conn)).andReturn(true);
		connectionSource.clearSpecialConnection(conn);
		connectionSource.releaseConnection(conn);
		replay(connectionSource, conn, savePoint);
		TransactionManager tm = new TransactionManager(connectionSource);
		tm.callInTransaction(new Callable<Void>() {
			@Override
			public Void call() {
				return null;
			}
		});
		verify(connectionSource, conn, savePoint);
	}

	@Test
	public void testTransactionManagerAutoCommitOn() throws Exception {
		ConnectionSource connectionSource = createMock(ConnectionSource.class);
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		expect(conn.isAutoCommitSupported()).andReturn(true);
		expect(conn.isAutoCommit()).andReturn(true);
		conn.setAutoCommit(false);
		Savepoint savePoint = createMock(Savepoint.class);
		expect(savePoint.getSavepointName()).andReturn("name").anyTimes();
		expect(conn.setSavePoint(isA(String.class))).andReturn(savePoint);
		conn.commit(savePoint);
		conn.setAutoCommit(true);
		expect(connectionSource.getDatabaseType()).andReturn(databaseType);
		expect(connectionSource.getReadWriteConnection(null)).andReturn(conn);
		expect(connectionSource.saveSpecialConnection(conn)).andReturn(true);
		connectionSource.clearSpecialConnection(conn);
		connectionSource.releaseConnection(conn);
		replay(connectionSource, conn, savePoint);
		TransactionManager tm = new TransactionManager(connectionSource);
		tm.callInTransaction(new Callable<Void>() {
			@Override
			public Void call() {
				return null;
			}
		});
		verify(connectionSource, conn, savePoint);
	}

	@Test
	public void testTransactionManagerSpringWiring() {
		TransactionManager tm = new TransactionManager();
		tm.setConnectionSource(connectionSource);
		tm.initialize();
	}

	@Test(expected = IllegalStateException.class)
	public void testTransactionManagerNoSet() {
		TransactionManager tm = new TransactionManager();
		tm.initialize();
	}

	@Test
	public void testDaoTransactionManagerCommitted() throws Exception {
		if (connectionSource == null) {
			return;
		}
		TransactionManager mgr = new TransactionManager(connectionSource);
		final Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		testTransactionManager(mgr, null, fooDao);
	}

	@Test
	public void testRollBack() throws Exception {
		if (connectionSource == null) {
			return;
		}
		TransactionManager mgr = new TransactionManager(connectionSource);
		final Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		testTransactionManager(mgr, new RuntimeException("What!!  I protest!!"), fooDao);
	}

	@Test
	public void testSpringWiredRollBack() throws Exception {
		if (connectionSource == null) {
			return;
		}
		TransactionManager mgr = new TransactionManager();
		mgr.setConnectionSource(connectionSource);
		mgr.initialize();
		final Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		testTransactionManager(mgr, new RuntimeException("What!!  I protest!!"), fooDao);
	}

	@Test
	public void testNonRuntimeExceptionWiredRollBack() throws Exception {
		if (connectionSource == null) {
			return;
		}
		TransactionManager mgr = new TransactionManager();
		mgr.setConnectionSource(connectionSource);
		mgr.initialize();
		final Dao<Foo, Integer> dao = createDao(Foo.class, true);
		testTransactionManager(mgr, new Exception("What!!  I protest via an Exception!!"), dao);
	}

	@Test
	public void testTransactionWithinTransaction() throws Exception {
		if (connectionSource == null) {
			return;
		}
		final TransactionManager mgr = new TransactionManager(connectionSource);
		final Dao<Foo, Integer> dao = createDao(Foo.class, true);
		mgr.callInTransaction(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				testTransactionManager(mgr, null, dao);
				return null;
			}
		});
	}

	@Test
	public void testTransactionWithinTransactionFails() throws Exception {
		if (connectionSource == null) {
			return;
		}
		final TransactionManager mgr = new TransactionManager(connectionSource);
		final Dao<Foo, Integer> dao = createDao(Foo.class, true);
		try {
			mgr.callInTransaction(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					dao.create(new Foo());
					mgr.callInTransaction(new Callable<Void>() {
						@Override
						public Void call() throws Exception {
							dao.create(new Foo());
							throw new SQLException("Exception ahoy!");
						}
					});
					return null;
				}
			});
			fail("Should have thrown");
		} catch (SQLException se) {
			// ignored
		}
		List<Foo> results = dao.queryForAll();
		assertNotNull(results);
		assertEquals(0, results.size());
	}

	@Test
	public void testConnectionLeakCreateList() throws Exception {
		final Dao<Foo, Integer> dao = createDao(Foo.class, true);
		final List<Foo> list = new ArrayList<Foo>();
		Foo foo1 = new Foo();
		foo1.val = 1;
		list.add(foo1);
		Foo foo2 = new Foo();
		foo2.val = 2;
		list.add(foo2);
		Foo foo3 = new Foo();
		foo3.val = 3;
		list.add(foo3);
		assertTrue(connectionSource.isEverythingClosed());
		assertEquals(0, connectionSource.getConnectionCount());
		TransactionManager.callInTransaction(connectionSource, new Callable<Boolean>() {
			@Override
			public Boolean call() throws Exception {
				return dao.create(list) >= 0;
			}
		});
		assertTrue(connectionSource.isEverythingClosed());
		assertEquals(0, connectionSource.getConnectionCount());
	}

	@Test
	public void testNestedTransactions() throws Exception {
		final Dao<Foo, Integer> dao = createDao(Foo.class, true);
		final Foo foo = new Foo();
		assertEquals(1, dao.create(foo));

		Foo result = dao.queryForId(foo.id);
		assertNotNull(result);

		try {
			TransactionManager.callInTransaction(connectionSource, new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					TransactionManager.callInTransaction(connectionSource, new Callable<Void>() {
						@Override
						public Void call() throws Exception {
							dao.delete(foo);
							return null;
						}
					});
					throw new SQLException();
				}
			});
			fail("Should have thrown");
		} catch (SQLException se) {
			// expected
		}

		result = dao.queryForId(foo.id);
		assertNotNull(result);
	}

	@Test
	public void testNestedTransactionsReleaseFails() throws Exception {
		final ConnectionSource connectionSource = createMock(ConnectionSource.class);
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		expect(conn.isAutoCommitSupported()).andReturn(true).times(2);
		expect(conn.isAutoCommit()).andReturn(true).times(2);
		conn.setAutoCommit(false);
		expectLastCall().times(2);
		Savepoint savePoint = createMock(Savepoint.class);
		expect(savePoint.getSavepointName()).andReturn("name").anyTimes();
		expect(conn.setSavePoint(isA(String.class))).andReturn(savePoint).times(2);
		expect(connectionSource.getDatabaseType()).andReturn(databaseType).times(2);
		expect(connectionSource.getReadWriteConnection(null)).andReturn(conn).times(2);
		expect(connectionSource.saveSpecialConnection(conn)).andReturn(true);
		expect(connectionSource.saveSpecialConnection(conn)).andReturn(false);
		// should only get one of these because we only returned save once
		connectionSource.clearSpecialConnection(conn);
		conn.releaseSavePoint(savePoint);
		expectLastCall().andThrow(new SQLException("testing the release to fail"));
		conn.rollback(savePoint);
		expectLastCall().times(2);
		connectionSource.releaseConnection(conn);
		expectLastCall().times(2);
		conn.setAutoCommit(true);
		expectLastCall().times(2);

		replay(connectionSource, conn, savePoint);
		try {
			TransactionManager.callInTransaction(connectionSource, new Callable<Void>() {
				@Override
				public Void call() throws SQLException {
					TransactionManager.callInTransaction(connectionSource, new Callable<Void>() {
						@Override
						public Void call() {
							return null;
						}
					});
					return null;
				}
			});
		} catch (SQLException se) {
			// expected
		}
		verify(connectionSource, conn, savePoint);
	}

	private void testTransactionManager(TransactionManager mgr, final Exception exception,
			final Dao<Foo, Integer> fooDao) throws Exception {
		final Foo foo1 = new Foo();
		int val = 13131511;
		foo1.val = val;
		assertEquals(1, fooDao.create(foo1));
		try {
			final int ret = 13431231;
			int returned = mgr.callInTransaction(new Callable<Integer>() {
				@Override
				public Integer call() throws Exception {
					// we delete it inside a transaction
					assertEquals(1, fooDao.delete(foo1));
					// we can't find it
					assertNull(fooDao.queryForId(foo1.id));
					if (exception != null) {
						// but then we throw an exception which rolls back the transaction
						throw exception;
					} else {
						return ret;
					}
				}
			});
			if (exception == null) {
				assertEquals(ret, returned);
			} else {
				fail("Should have thrown");
			}
		} catch (SQLException e) {
			if (exception == null) {
				throw e;
			} else {
				// expected
			}
		}

		if (exception == null) {
			// still doesn't find it after we delete it
			assertNull(fooDao.queryForId(foo1.id));
		} else {
			// still finds it after we delete it
			Foo foo2 = fooDao.queryForId(foo1.id);
			assertNotNull(foo2);
			assertEquals(val, foo2.val);
		}
	}
}
