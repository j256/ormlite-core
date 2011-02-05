package com.j256.ormlite.stmt;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;
import com.j256.ormlite.table.TableInfo;

public class StatementExecutorTest extends BaseCoreTest {

	@Test
	public void testQueryResults() throws Exception {
		TableInfo<BaseFoo> tableInfo = new TableInfo<BaseFoo>(connectionSource, BaseFoo.class);
		StatementExecutor<BaseFoo, String> statementExec =
				new StatementExecutor<BaseFoo, String>(databaseType, tableInfo);
		@SuppressWarnings("unchecked")
		PreparedStmt<BaseFoo> preparedStmt = createMock(PreparedStmt.class);
		CompiledStatement compiledStmt = createMock(CompiledStatement.class);
		expect(preparedStmt.compile(null)).andReturn(compiledStmt);
		String stmt = "select * from basefoo";
		expect(preparedStmt.getStatement()).andReturn(stmt).anyTimes();
		DatabaseResults databaseResults = createMock(DatabaseResults.class);
		BaseFoo baseFoo = new BaseFoo();
		expect(preparedStmt.mapRow(databaseResults)).andReturn(baseFoo);
		expect(databaseResults.next()).andReturn(true);
		expect(databaseResults.next()).andReturn(false);
		expect(compiledStmt.executeQuery()).andReturn(databaseResults);
		compiledStmt.close();
		replay(preparedStmt, compiledStmt, databaseResults);

		List<BaseFoo> results = statementExec.query(connectionSource, preparedStmt);
		assertEquals(1, results.size());
		assertSame(baseFoo, results.get(0));

		verify(preparedStmt, compiledStmt, databaseResults);
	}

	@Test(expected = SQLException.class)
	public void testQueryThrow() throws Exception {
		TableInfo<BaseFoo> tableInfo = new TableInfo<BaseFoo>(connectionSource, BaseFoo.class);
		StatementExecutor<BaseFoo, String> statementExec =
				new StatementExecutor<BaseFoo, String>(databaseType, tableInfo);
		@SuppressWarnings("unchecked")
		PreparedStmt<BaseFoo> preparedStmt = createMock(PreparedStmt.class);
		expect(preparedStmt.compile(null)).andThrow(new SQLException("expected"));
		String stmt = "select * from basefoo";
		expect(preparedStmt.getStatement()).andReturn(stmt).anyTimes();
		replay(preparedStmt);
		statementExec.query(connectionSource, preparedStmt);
	}

	@Test
	public void testRawQueryOldThrow() throws Exception {
		TableInfo<BaseFoo> tableInfo = new TableInfo<BaseFoo>(connectionSource, BaseFoo.class);
		DatabaseConnection connection = createMock(DatabaseConnection.class);
		connectionSource.setDatabaseConnection(connection);
		String stmt = "select * from basefoo";
		CompiledStatement compiledStmt = createMock(CompiledStatement.class);
		expect(
				connection.compileStatement(isA(String.class), isA(StatementType.class), isA(FieldType[].class),
						isA(FieldType[].class))).andReturn(compiledStmt);
		expect(compiledStmt.getColumnCount()).andThrow(new SQLException("expected"));
		compiledStmt.close();
		StatementExecutor<BaseFoo, String> statementExec =
				new StatementExecutor<BaseFoo, String>(databaseType, tableInfo);
		replay(connection, compiledStmt);
		try {
			statementExec.queryForAllRawOld(connectionSource, stmt);
		} catch (SQLException e) {
			// expected
		}
		verify(connection, compiledStmt);
	}

	@Test
	public void testBuildOldIteratorThrow() throws Exception {
		TableInfo<BaseFoo> tableInfo = new TableInfo<BaseFoo>(connectionSource, BaseFoo.class);
		DatabaseConnection connection = createMock(DatabaseConnection.class);
		connectionSource.setDatabaseConnection(connection);
		String stmt = "select * from basefoo";
		CompiledStatement compiledStmt = createMock(CompiledStatement.class);
		expect(
				connection.compileStatement(isA(String.class), isA(StatementType.class), isA(FieldType[].class),
						isA(FieldType[].class))).andReturn(compiledStmt);
		expect(compiledStmt.getColumnCount()).andThrow(new SQLException("expected"));
		compiledStmt.close();
		StatementExecutor<BaseFoo, String> statementExec =
				new StatementExecutor<BaseFoo, String>(databaseType, tableInfo);
		replay(connection, compiledStmt);
		try {
			statementExec.buildOldIterator(connectionSource, stmt);
		} catch (SQLException e) {
			// expected
		}
		verify(connection, compiledStmt);
	}

	@Test
	public void testQueryRawMapperThrow() throws Exception {
		TableInfo<BaseFoo> tableInfo = new TableInfo<BaseFoo>(connectionSource, BaseFoo.class);
		DatabaseConnection connection = createMock(DatabaseConnection.class);
		connectionSource.setDatabaseConnection(connection);
		String stmt = "select * from basefoo";
		CompiledStatement compiledStmt = createMock(CompiledStatement.class);
		expect(
				connection.compileStatement(isA(String.class), isA(StatementType.class), isA(FieldType[].class),
						isA(FieldType[].class))).andReturn(compiledStmt);
		expect(compiledStmt.getColumnCount()).andThrow(new SQLException("expected"));
		compiledStmt.close();
		StatementExecutor<BaseFoo, String> statementExec =
				new StatementExecutor<BaseFoo, String>(databaseType, tableInfo);
		RawRowMapper<?> mapper = createMock(RawRowMapper.class);
		replay(connection, compiledStmt, mapper);
		try {
			statementExec.queryRaw(connectionSource, stmt, mapper);
		} catch (SQLException e) {
			// expected
		}
		verify(connection, compiledStmt, mapper);
	}

	@Test
	public void testQueryRawTypesThrow() throws Exception {
		TableInfo<BaseFoo> tableInfo = new TableInfo<BaseFoo>(connectionSource, BaseFoo.class);
		DatabaseConnection connection = createMock(DatabaseConnection.class);
		connectionSource.setDatabaseConnection(connection);
		String stmt = "select * from basefoo";
		CompiledStatement compiledStmt = createMock(CompiledStatement.class);
		expect(
				connection.compileStatement(isA(String.class), isA(StatementType.class), isA(FieldType[].class),
						isA(FieldType[].class))).andReturn(compiledStmt);
		expect(compiledStmt.getColumnCount()).andThrow(new SQLException("expected"));
		compiledStmt.close();
		StatementExecutor<BaseFoo, String> statementExec =
				new StatementExecutor<BaseFoo, String>(databaseType, tableInfo);
		RawRowMapper<?> mapper = createMock(RawRowMapper.class);
		replay(connection, compiledStmt, mapper);
		try {
			statementExec.queryRaw(connectionSource, stmt, new DataType[0]);
		} catch (SQLException e) {
			// expected
		}
		verify(connection, compiledStmt, mapper);
	}

	@Test
	public void testUpdateThrow() throws Exception {
		TableInfo<BaseFoo> tableInfo = new TableInfo<BaseFoo>(connectionSource, BaseFoo.class);
		DatabaseConnection connection = createMock(DatabaseConnection.class);
		@SuppressWarnings("unchecked")
		PreparedUpdate<BaseFoo> update = createMock(PreparedUpdate.class);
		CompiledStatement compiledStmt = createMock(CompiledStatement.class);
		expect(update.compile(connection)).andReturn(compiledStmt);
		expect(compiledStmt.executeUpdate()).andThrow(new SQLException("expected"));
		compiledStmt.close();
		StatementExecutor<BaseFoo, String> statementExec =
				new StatementExecutor<BaseFoo, String>(databaseType, tableInfo);
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
		TableInfo<BaseFoo> tableInfo = new TableInfo<BaseFoo>(connectionSource, BaseFoo.class);
		DatabaseConnection connection = createMock(DatabaseConnection.class);
		@SuppressWarnings("unchecked")
		PreparedDelete<BaseFoo> delete = createMock(PreparedDelete.class);
		CompiledStatement compiledStmt = createMock(CompiledStatement.class);
		expect(delete.compile(connection)).andReturn(compiledStmt);
		expect(compiledStmt.executeUpdate()).andThrow(new SQLException("expected"));
		compiledStmt.close();
		StatementExecutor<BaseFoo, String> statementExec =
				new StatementExecutor<BaseFoo, String>(databaseType, tableInfo);
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
		TableInfo<BaseFoo> tableInfo = new TableInfo<BaseFoo>(connectionSource, BaseFoo.class);
		DatabaseConnection connection = createMock(DatabaseConnection.class);
		expect(connection.isAutoCommitSupported()).andReturn(false);
		StatementExecutor<BaseFoo, String> statementExec =
				new StatementExecutor<BaseFoo, String>(databaseType, tableInfo);
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
		TableInfo<BaseFoo> tableInfo = new TableInfo<BaseFoo>(connectionSource, BaseFoo.class);
		DatabaseConnection connection = createMock(DatabaseConnection.class);
		expect(connection.isAutoCommitSupported()).andReturn(true);
		expect(connection.getAutoCommit()).andReturn(false);
		StatementExecutor<BaseFoo, String> statementExec =
				new StatementExecutor<BaseFoo, String>(databaseType, tableInfo);
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
		TableInfo<BaseFoo> tableInfo = new TableInfo<BaseFoo>(connectionSource, BaseFoo.class);
		DatabaseConnection connection = createMock(DatabaseConnection.class);
		expect(connection.isAutoCommitSupported()).andReturn(true);
		expect(connection.getAutoCommit()).andReturn(true);
		connection.setAutoCommit(false);
		connection.setAutoCommit(true);
		StatementExecutor<BaseFoo, String> statementExec =
				new StatementExecutor<BaseFoo, String>(databaseType, tableInfo);
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
		TableInfo<BaseFoo> tableInfo = new TableInfo<BaseFoo>(connectionSource, BaseFoo.class);
		DatabaseConnection connection = createMock(DatabaseConnection.class);
		expect(connection.isAutoCommitSupported()).andReturn(true);
		expect(connection.getAutoCommit()).andReturn(true);
		connection.setAutoCommit(false);
		connection.setAutoCommit(true);
		StatementExecutor<BaseFoo, String> statementExec =
				new StatementExecutor<BaseFoo, String>(databaseType, tableInfo);
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
