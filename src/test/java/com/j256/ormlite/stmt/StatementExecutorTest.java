package com.j256.ormlite.stmt;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.TableInfo;

public class StatementExecutorTest extends BaseCoreStmtTest {

	@Test
	public void testUpdateThrow() throws Exception {
		TableInfo<Foo> tableInfo = new TableInfo<Foo>(connectionSource, Foo.class);
		DatabaseConnection connection = createMock(DatabaseConnection.class);
		@SuppressWarnings("unchecked")
		PreparedUpdate<Foo> update = createMock(PreparedUpdate.class);
		CompiledStatement compiledStmt = createMock(CompiledStatement.class);
		expect(update.compile(connection)).andReturn(compiledStmt);
		expect(compiledStmt.runUpdate()).andThrow(new SQLException("expected"));
		compiledStmt.close();
		StatementExecutor<Foo, String> statementExec = new StatementExecutor<Foo, String>(databaseType, tableInfo);
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
		TableInfo<Foo> tableInfo = new TableInfo<Foo>(connectionSource, Foo.class);
		DatabaseConnection connection = createMock(DatabaseConnection.class);
		@SuppressWarnings("unchecked")
		PreparedDelete<Foo> delete = createMock(PreparedDelete.class);
		CompiledStatement compiledStmt = createMock(CompiledStatement.class);
		expect(delete.compile(connection)).andReturn(compiledStmt);
		expect(compiledStmt.runUpdate()).andThrow(new SQLException("expected"));
		compiledStmt.close();
		StatementExecutor<Foo, String> statementExec = new StatementExecutor<Foo, String>(databaseType, tableInfo);
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
		TableInfo<Foo> tableInfo = new TableInfo<Foo>(connectionSource, Foo.class);
		DatabaseConnection connection = createMock(DatabaseConnection.class);
		expect(connection.isAutoCommitSupported()).andReturn(false);
		StatementExecutor<Foo, String> statementExec = new StatementExecutor<Foo, String>(databaseType, tableInfo);
		replay(connection);
		final AtomicBoolean called = new AtomicBoolean(false);
		statementExec.callBatchTasks(connection, new Callable<Void>() {
			public Void call() throws Exception {
				called.set(true);
				return null;
			}
		});
		assertTrue(called.get());
		verify(connection);
	}

	@Test
	public void testCallBatchTasksAutoCommitFalse() throws Exception {
		TableInfo<Foo> tableInfo = new TableInfo<Foo>(connectionSource, Foo.class);
		DatabaseConnection connection = createMock(DatabaseConnection.class);
		expect(connection.isAutoCommitSupported()).andReturn(true);
		expect(connection.getAutoCommit()).andReturn(false);
		StatementExecutor<Foo, String> statementExec = new StatementExecutor<Foo, String>(databaseType, tableInfo);
		replay(connection);
		final AtomicBoolean called = new AtomicBoolean(false);
		statementExec.callBatchTasks(connection, new Callable<Void>() {
			public Void call() throws Exception {
				called.set(true);
				return null;
			}
		});
		assertTrue(called.get());
		verify(connection);
	}

	@Test
	public void testCallBatchTasksAutoCommitTrue() throws Exception {
		TableInfo<Foo> tableInfo = new TableInfo<Foo>(connectionSource, Foo.class);
		DatabaseConnection connection = createMock(DatabaseConnection.class);
		expect(connection.isAutoCommitSupported()).andReturn(true);
		expect(connection.getAutoCommit()).andReturn(true);
		connection.setAutoCommit(false);
		connection.setAutoCommit(true);
		StatementExecutor<Foo, String> statementExec = new StatementExecutor<Foo, String>(databaseType, tableInfo);
		replay(connection);
		final AtomicBoolean called = new AtomicBoolean(false);
		statementExec.callBatchTasks(connection, new Callable<Void>() {
			public Void call() throws Exception {
				called.set(true);
				return null;
			}
		});
		assertTrue(called.get());
		verify(connection);
	}

	@Test
	public void testCallBatchTasksAutoCommitTrueThrow() throws Exception {
		TableInfo<Foo> tableInfo = new TableInfo<Foo>(connectionSource, Foo.class);
		DatabaseConnection connection = createMock(DatabaseConnection.class);
		expect(connection.isAutoCommitSupported()).andReturn(true);
		expect(connection.getAutoCommit()).andReturn(true);
		connection.setAutoCommit(false);
		connection.setAutoCommit(true);
		StatementExecutor<Foo, String> statementExec = new StatementExecutor<Foo, String>(databaseType, tableInfo);
		replay(connection);
		try {
			statementExec.callBatchTasks(connection, new Callable<Void>() {
				public Void call() throws Exception {
					throw new Exception("expected");
				}
			});
			fail("Should have thrown");
		} catch (Exception e) {
			// expected
		}
		verify(connection);
	}
}
