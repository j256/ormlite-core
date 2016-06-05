package com.j256.ormlite.stmt;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.TableInfo;

public class StatementExecutorTest extends BaseCoreStmtTest {

	@Test
	public void testUpdateThrow() throws Exception {
		TableInfo<Foo, String> tableInfo = new TableInfo<Foo, String>(connectionSource, null, Foo.class);
		DatabaseConnection connection = createMock(DatabaseConnection.class);
		@SuppressWarnings("unchecked")
		PreparedUpdate<Foo> update = createMock(PreparedUpdate.class);
		CompiledStatement compiledStmt = createMock(CompiledStatement.class);
		expect(update.compile(connection, StatementType.UPDATE)).andReturn(compiledStmt);
		expect(compiledStmt.runUpdate()).andThrow(new SQLException("expected"));
		compiledStmt.close();
		StatementExecutor<Foo, String> statementExec =
				new StatementExecutor<Foo, String>(databaseType, tableInfo, null);
		replay(connection, compiledStmt, update);
		try {
			statementExec.update(connection, update);
			fail("should have thrown");
		} catch (SQLException e) {
			// expected
		}
		verify(connection, compiledStmt, update);
	}

	@Test
	public void testDeleteThrow() throws Exception {
		TableInfo<Foo, String> tableInfo = new TableInfo<Foo, String>(connectionSource, null, Foo.class);
		DatabaseConnection connection = createMock(DatabaseConnection.class);
		@SuppressWarnings("unchecked")
		PreparedDelete<Foo> delete = createMock(PreparedDelete.class);
		CompiledStatement compiledStmt = createMock(CompiledStatement.class);
		expect(delete.compile(connection, StatementType.DELETE)).andReturn(compiledStmt);
		expect(compiledStmt.runUpdate()).andThrow(new SQLException("expected"));
		compiledStmt.close();
		StatementExecutor<Foo, String> statementExec =
				new StatementExecutor<Foo, String>(databaseType, tableInfo, null);
		replay(connection, compiledStmt, delete);
		try {
			statementExec.delete(connection, delete);
			fail("should have thrown");
		} catch (SQLException e) {
			// expected
		}
		verify(connection, compiledStmt, delete);
	}

	@Test
	public void testCallBatchTasksNoAutoCommit() throws Exception {
		TableInfo<Foo, String> tableInfo = new TableInfo<Foo, String>(connectionSource, null, Foo.class);

		ConnectionSource connectionSource = createMock(ConnectionSource.class);
		DatabaseConnection connection = createMock(DatabaseConnection.class);
		expect(connectionSource.isSingleConnection("foo")).andReturn(false);
		expect(connectionSource.getReadWriteConnection("foo")).andReturn(connection);
		expect(connectionSource.saveSpecialConnection(connection)).andReturn(false);
		connectionSource.releaseConnection(connection);

		expect(connection.isAutoCommitSupported()).andReturn(false);
		StatementExecutor<Foo, String> statementExec =
				new StatementExecutor<Foo, String>(databaseType, tableInfo, null);
		replay(connectionSource, connection);
		final AtomicBoolean called = new AtomicBoolean(false);
		statementExec.callBatchTasks(connectionSource, new Callable<Void>() {
			@Override
			public Void call() {
				called.set(true);
				return null;
			}
		});
		assertTrue(called.get());
		verify(connectionSource, connection);
	}

	@Test
	public void testCallBatchTasksAutoCommitFalse() throws Exception {
		TableInfo<Foo, String> tableInfo = new TableInfo<Foo, String>(connectionSource, null, Foo.class);

		ConnectionSource connectionSource = createMock(ConnectionSource.class);
		DatabaseConnection connection = createMock(DatabaseConnection.class);
		expect(connectionSource.isSingleConnection("foo")).andReturn(false);
		expect(connectionSource.getReadWriteConnection("foo")).andReturn(connection);
		expect(connectionSource.saveSpecialConnection(connection)).andReturn(false);
		connectionSource.releaseConnection(connection);

		expect(connection.isAutoCommitSupported()).andReturn(true);
		expect(connection.isAutoCommit()).andReturn(false);
		StatementExecutor<Foo, String> statementExec =
				new StatementExecutor<Foo, String>(databaseType, tableInfo, null);
		replay(connectionSource, connection);
		final AtomicBoolean called = new AtomicBoolean(false);
		statementExec.callBatchTasks(connectionSource, new Callable<Void>() {
			@Override
			public Void call() {
				called.set(true);
				return null;
			}
		});
		assertTrue(called.get());
		verify(connectionSource, connection);
	}

	@Test
	public void testCallBatchTasksAutoCommitTrue() throws Exception {
		TableInfo<Foo, String> tableInfo = new TableInfo<Foo, String>(connectionSource, null, Foo.class);

		ConnectionSource connectionSource = createMock(ConnectionSource.class);
		DatabaseConnection connection = createMock(DatabaseConnection.class);
		expect(connectionSource.isSingleConnection("foo")).andReturn(false);
		expect(connectionSource.getReadWriteConnection("foo")).andReturn(connection);
		expect(connectionSource.saveSpecialConnection(connection)).andReturn(false);
		connectionSource.releaseConnection(connection);

		expect(connection.isAutoCommitSupported()).andReturn(true);
		expect(connection.isAutoCommit()).andReturn(true);
		connection.setAutoCommit(false);
		connection.setAutoCommit(true);
		StatementExecutor<Foo, String> statementExec =
				new StatementExecutor<Foo, String>(databaseType, tableInfo, null);
		replay(connectionSource, connection);
		final AtomicBoolean called = new AtomicBoolean(false);
		statementExec.callBatchTasks(connectionSource, new Callable<Void>() {
			@Override
			public Void call() {
				called.set(true);
				return null;
			}
		});
		assertTrue(called.get());
		verify(connectionSource, connection);
	}

	@Test
	public void testCallBatchTasksAutoCommitTrueSynchronized() throws Exception {
		TableInfo<Foo, String> tableInfo = new TableInfo<Foo, String>(connectionSource, null, Foo.class);

		ConnectionSource connectionSource = createMock(ConnectionSource.class);
		DatabaseConnection connection = createMock(DatabaseConnection.class);
		expect(connectionSource.isSingleConnection("foo")).andReturn(true);
		expect(connectionSource.getReadWriteConnection("foo")).andReturn(connection);
		expect(connectionSource.saveSpecialConnection(connection)).andReturn(false);
		connectionSource.releaseConnection(connection);

		expect(connection.isAutoCommitSupported()).andReturn(true);
		expect(connection.isAutoCommit()).andReturn(true);
		connection.setAutoCommit(false);
		connection.setAutoCommit(true);
		StatementExecutor<Foo, String> statementExec =
				new StatementExecutor<Foo, String>(databaseType, tableInfo, null);
		replay(connectionSource, connection);
		final AtomicBoolean called = new AtomicBoolean(false);
		statementExec.callBatchTasks(connectionSource, new Callable<Void>() {
			@Override
			public Void call() {
				called.set(true);
				return null;
			}
		});
		assertTrue(called.get());
		verify(connectionSource, connection);
	}

	@Test
	public void testCallBatchTasksAutoCommitTrueThrow() throws Exception {
		TableInfo<Foo, String> tableInfo = new TableInfo<Foo, String>(connectionSource, null, Foo.class);

		ConnectionSource connectionSource = createMock(ConnectionSource.class);
		DatabaseConnection connection = createMock(DatabaseConnection.class);
		expect(connectionSource.isSingleConnection("foo")).andReturn(false);
		expect(connectionSource.getReadWriteConnection("foo")).andReturn(connection);
		expect(connectionSource.saveSpecialConnection(connection)).andReturn(false);
		connectionSource.releaseConnection(connection);

		expect(connection.isAutoCommitSupported()).andReturn(true);
		expect(connection.isAutoCommit()).andReturn(true);
		connection.setAutoCommit(false);
		connection.setAutoCommit(true);
		StatementExecutor<Foo, String> statementExec =
				new StatementExecutor<Foo, String>(databaseType, tableInfo, null);
		replay(connectionSource, connection);
		try {
			statementExec.callBatchTasks(connectionSource, new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					throw new Exception("expected");
				}
			});
			fail("Should have thrown");
		} catch (Exception e) {
			// expected
		}
		verify(connectionSource, connection);
	}

	@Test(expected = SQLException.class)
	public void testUpdateIdNoId() throws Exception {
		Dao<NoId, Object> noIdDao = createDao(NoId.class, true);
		NoId noId = new NoId();
		noId.stuff = "1";
		assertEquals(1, noIdDao.create(noId));
		noIdDao.updateId(noId, "something else");
	}

	@Test(expected = SQLException.class)
	public void testRefreshNoId() throws Exception {
		Dao<NoId, Object> noIdDao = createDao(NoId.class, true);
		NoId noId = new NoId();
		noId.stuff = "1";
		assertEquals(1, noIdDao.create(noId));
		noIdDao.refresh(noId);
	}

	@Test(expected = SQLException.class)
	public void testDeleteNoId() throws Exception {
		Dao<NoId, Object> noIdDao = createDao(NoId.class, true);
		NoId noId = new NoId();
		noId.stuff = "1";
		assertEquals(1, noIdDao.create(noId));
		noIdDao.delete(noId);
	}

	@Test(expected = SQLException.class)
	public void testDeleteObjectsNoId() throws Exception {
		Dao<NoId, Object> noIdDao = createDao(NoId.class, true);
		NoId noId = new NoId();
		noId.stuff = "1";
		assertEquals(1, noIdDao.create(noId));
		ArrayList<NoId> noIdList = new ArrayList<NoId>();
		noIdList.add(noId);
		noIdDao.delete(noIdList);
	}

	@Test(expected = SQLException.class)
	public void testDeleteIdsNoId() throws Exception {
		Dao<NoId, Object> noIdDao = createDao(NoId.class, true);
		NoId noId = new NoId();
		noId.stuff = "1";
		assertEquals(1, noIdDao.create(noId));
		ArrayList<Object> noIdList = new ArrayList<Object>();
		noIdList.add(noId);
		noIdDao.deleteIds(noIdList);
	}

	@Test
	public void testCallBatchTasksCommitted() throws Exception {
		final Dao<Foo, Integer> dao = createDao(Foo.class, true);
		final Foo foo1 = new Foo();
		DatabaseConnection conn = dao.startThreadConnection();
		try {
			dao.callBatchTasks(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					assertEquals(1, dao.create(foo1));
					assertNotNull(dao.queryForId(foo1.id));
					return null;
				}
			});
			dao.rollBack(conn);
			assertNotNull(dao.queryForId(foo1.id));
		} finally {
			dao.endThreadConnection(conn);
		}
	}

	protected static class NoId {
		@DatabaseField
		String stuff;
	}
}
