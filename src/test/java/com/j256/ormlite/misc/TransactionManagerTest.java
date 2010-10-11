package com.j256.ormlite.misc;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.concurrent.Callable;

import org.junit.Test;

import com.j256.ormlite.BaseOrmLiteCoreTest;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;

public class TransactionManagerTest extends BaseOrmLiteCoreTest {

	@Test
	public void testTransactionManager() throws Exception {
		ConnectionSource connectionSource = createMock(ConnectionSource.class);
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		expect(conn.isAutoCommitSupported()).andReturn(false);
		Savepoint savePoint = createMock(Savepoint.class);
		expect(savePoint.getSavepointName()).andReturn("name").anyTimes();
		expect(conn.setSavePoint(isA(String.class))).andReturn(savePoint);
		conn.commit(savePoint);
		expect(connectionSource.getReadWriteConnection()).andReturn(conn);
		connectionSource.saveTransactionConnection(conn);
		connectionSource.clearTransactionConnection(conn);
		connectionSource.releaseConnection(conn);
		TransactionManager tm = new TransactionManager(connectionSource);
		replay(connectionSource, conn, savePoint);
		tm.callInTransaction(new Callable<Void>() {
			public Void call() throws Exception {
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
		expect(connectionSource.getReadWriteConnection()).andReturn(conn);
		connectionSource.saveTransactionConnection(conn);
		connectionSource.clearTransactionConnection(conn);
		connectionSource.releaseConnection(conn);
		TransactionManager tm = new TransactionManager(connectionSource);
		replay(connectionSource, conn);
		tm.callInTransaction(new Callable<Void>() {
			public Void call() throws Exception {
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
		expect(connectionSource.getReadWriteConnection()).andReturn(conn);
		connectionSource.saveTransactionConnection(conn);
		connectionSource.clearTransactionConnection(conn);
		connectionSource.releaseConnection(conn);
		TransactionManager tm = new TransactionManager(connectionSource);
		replay(connectionSource, conn, savePoint);
		try {
			tm.callInTransaction(new Callable<Void>() {
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
		expect(connectionSource.getReadWriteConnection()).andReturn(conn);
		connectionSource.saveTransactionConnection(conn);
		connectionSource.clearTransactionConnection(conn);
		connectionSource.releaseConnection(conn);
		TransactionManager tm = new TransactionManager(connectionSource);
		replay(connectionSource, conn);
		try {
			tm.callInTransaction(new Callable<Void>() {
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
		expect(connectionSource.getReadWriteConnection()).andReturn(conn);
		connectionSource.saveTransactionConnection(conn);
		connectionSource.clearTransactionConnection(conn);
		connectionSource.releaseConnection(conn);
		TransactionManager tm = new TransactionManager(connectionSource);
		replay(connectionSource, conn, savePoint);
		try {
			tm.callInTransaction(new Callable<Void>() {
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
		expect(conn.getAutoCommit()).andReturn(false);
		Savepoint savePoint = createMock(Savepoint.class);
		expect(savePoint.getSavepointName()).andReturn("name").anyTimes();
		expect(conn.setSavePoint(isA(String.class))).andReturn(savePoint);
		conn.commit(savePoint);
		expect(connectionSource.getReadWriteConnection()).andReturn(conn);
		connectionSource.saveTransactionConnection(conn);
		connectionSource.clearTransactionConnection(conn);
		connectionSource.releaseConnection(conn);
		TransactionManager tm = new TransactionManager(connectionSource);
		replay(connectionSource, conn, savePoint);
		tm.callInTransaction(new Callable<Void>() {
			public Void call() throws Exception {
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
		expect(conn.getAutoCommit()).andReturn(true);
		conn.setAutoCommit(false);
		Savepoint savePoint = createMock(Savepoint.class);
		expect(savePoint.getSavepointName()).andReturn("name").anyTimes();
		expect(conn.setSavePoint(isA(String.class))).andReturn(savePoint);
		conn.commit(savePoint);
		conn.setAutoCommit(true);
		expect(connectionSource.getReadWriteConnection()).andReturn(conn);
		connectionSource.saveTransactionConnection(conn);
		connectionSource.clearTransactionConnection(conn);
		connectionSource.releaseConnection(conn);
		TransactionManager tm = new TransactionManager(connectionSource);
		replay(connectionSource, conn, savePoint);
		tm.callInTransaction(new Callable<Void>() {
			public Void call() throws Exception {
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
}
