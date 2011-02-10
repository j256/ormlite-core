package com.j256.ormlite.stmt;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;

public class SelectIteratorTest extends BaseCoreTest {

	private DatabaseConnection databaseConnection;
	private Dao<BaseFoo, String> baseFooDao;

	@Test
	public void testIterator() throws Exception {
		startDao(false);
		String stmt = "select * from baseFoo";
		@SuppressWarnings("unchecked")
		PreparedStmt<BaseFoo> preparedStmt = createMock(PreparedStmt.class);
		CompiledStatement compiledStmt = createMock(CompiledStatement.class);
		DatabaseResults results = createMock(DatabaseResults.class);
		expect(results.next()).andReturn(false);
		expect(compiledStmt.runQuery()).andReturn(results);
		compiledStmt.close();
		replay(databaseConnection, compiledStmt, preparedStmt, results);
		SelectIterator<BaseFoo, String> iterator =
				new SelectIterator<BaseFoo, String>(BaseFoo.class, baseFooDao, preparedStmt, connectionSource,
						databaseConnection, compiledStmt, stmt);
		assertFalse(iterator.hasNext());
		iterator.close();
		assertFalse(iterator.hasNext());
		assertNull(iterator.next());
		verify(databaseConnection, compiledStmt, preparedStmt, results);
	}

	@Test
	public void testIteratorPrepared() throws Exception {
		startDao(false);
		@SuppressWarnings("unchecked")
		PreparedQuery<BaseFoo> preparedStmt = createMock(PreparedQuery.class);
		CompiledStatement compiledStmt = createMock(CompiledStatement.class);
		String stmt = "select * from baseFoo";
		DatabaseResults results = createMock(DatabaseResults.class);
		expect(results.next()).andReturn(false);
		expect(compiledStmt.runQuery()).andReturn(results);
		compiledStmt.close();
		replay(databaseConnection, preparedStmt, compiledStmt, results);
		SelectIterator<BaseFoo, String> iterator =
				new SelectIterator<BaseFoo, String>(BaseFoo.class, baseFooDao, preparedStmt, connectionSource,
						databaseConnection, compiledStmt, stmt);
		assertFalse(iterator.hasNext());
		verify(databaseConnection, preparedStmt, compiledStmt, results);
	}

	@Test(expected = IllegalStateException.class)
	public void testIteratorRemoveNoNext() throws Exception {
		startDao(false);
		@SuppressWarnings("unchecked")
		PreparedQuery<BaseFoo> preparedStmt = createMock(PreparedQuery.class);
		CompiledStatement compiledStmt = createMock(CompiledStatement.class);
		String stmt = "select * from baseFoo";
		DatabaseResults results = createMock(DatabaseResults.class);
		expect(results.next()).andReturn(false);
		expect(compiledStmt.runQuery()).andReturn(results);
		compiledStmt.close();
		replay(databaseConnection, preparedStmt, compiledStmt, results);
		SelectIterator<BaseFoo, String> iterator =
				new SelectIterator<BaseFoo, String>(BaseFoo.class, baseFooDao, preparedStmt, connectionSource,
						databaseConnection, compiledStmt, stmt);
		assertFalse(iterator.hasNext());
		iterator.remove();
	}

	@Test(expected = SQLException.class)
	public void testIteratorThrow() throws Exception {
		startDao(false);
		@SuppressWarnings("unchecked")
		PreparedQuery<BaseFoo> preparedStmt = createMock(PreparedQuery.class);
		CompiledStatement compiledStmt = createMock(CompiledStatement.class);
		expect(compiledStmt.runQuery()).andThrow(new SQLException("expected"));
		String stmt = "select * from baseFoo";
		expect(preparedStmt.getStatement()).andReturn(stmt);
		replay(databaseConnection, preparedStmt, compiledStmt);
		new SelectIterator<BaseFoo, String>(BaseFoo.class, baseFooDao, preparedStmt, connectionSource,
				databaseConnection, compiledStmt, stmt);
	}

	@Test
	public void testIteratorNext() throws Exception {
		startDao(false);
		@SuppressWarnings("unchecked")
		PreparedQuery<BaseFoo> preparedStmt = createMock(PreparedQuery.class);
		CompiledStatement compiledStmt = createMock(CompiledStatement.class);
		String stmt = "select * from baseFoo";
		DatabaseResults results = createMock(DatabaseResults.class);
		expect(results.next()).andReturn(true);
		expect(compiledStmt.runQuery()).andReturn(results);
		BaseFoo baseFoo = new BaseFoo();
		expect(preparedStmt.mapRow(results)).andReturn(baseFoo);
		compiledStmt.close();
		replay(databaseConnection, preparedStmt, compiledStmt, results);
		SelectIterator<BaseFoo, String> iterator =
				new SelectIterator<BaseFoo, String>(BaseFoo.class, baseFooDao, preparedStmt, connectionSource,
						databaseConnection, compiledStmt, stmt);
		assertTrue(iterator.hasNext());
		assertSame(baseFoo, iterator.next());
		iterator.close();
		verify(databaseConnection, preparedStmt, compiledStmt, results);
	}

	@Test(expected = IllegalStateException.class)
	public void testIteratorNextThrow() throws Exception {
		startDao(false);
		@SuppressWarnings("unchecked")
		PreparedQuery<BaseFoo> preparedStmt = createMock(PreparedQuery.class);
		CompiledStatement compiledStmt = createMock(CompiledStatement.class);
		String stmt = "select * from baseFoo";
		DatabaseResults results = createMock(DatabaseResults.class);
		expect(results.next()).andReturn(true);
		expect(compiledStmt.runQuery()).andReturn(results);
		expect(preparedStmt.mapRow(results)).andThrow(new SQLException("expected exception"));
		compiledStmt.close();
		replay(databaseConnection, preparedStmt, compiledStmt, results);
		SelectIterator<BaseFoo, String> iterator =
				new SelectIterator<BaseFoo, String>(BaseFoo.class, baseFooDao, preparedStmt, connectionSource,
						databaseConnection, compiledStmt, stmt);
		assertTrue(iterator.hasNext());
		iterator.next();
	}

	@Test(expected = IllegalStateException.class)
	public void testIteratorHasNextThrow() throws Exception {
		startDao(false);
		@SuppressWarnings("unchecked")
		PreparedQuery<BaseFoo> preparedStmt = createMock(PreparedQuery.class);
		CompiledStatement compiledStmt = createMock(CompiledStatement.class);
		String stmt = "select * from baseFoo";
		DatabaseResults results = createMock(DatabaseResults.class);
		expect(results.next()).andThrow(new SQLException("expected exception"));
		expect(compiledStmt.runQuery()).andReturn(results);
		compiledStmt.close();
		replay(databaseConnection, preparedStmt, compiledStmt, results);
		SelectIterator<BaseFoo, String> iterator =
				new SelectIterator<BaseFoo, String>(BaseFoo.class, baseFooDao, preparedStmt, connectionSource,
						databaseConnection, compiledStmt, stmt);
		iterator.hasNext();
	}

	@Test
	public void testIteratorRemove() throws Exception {
		startDao(false);
		@SuppressWarnings("unchecked")
		PreparedQuery<BaseFoo> preparedStmt = createMock(PreparedQuery.class);
		CompiledStatement compiledStmt = createMock(CompiledStatement.class);
		String stmt = "select * from baseFoo";
		DatabaseResults results = createMock(DatabaseResults.class);
		expect(results.next()).andReturn(true);
		expect(compiledStmt.runQuery()).andReturn(results);
		BaseFoo baseFoo = new BaseFoo();
		expect(preparedStmt.mapRow(results)).andReturn(baseFoo);
		expect(databaseConnection.delete(isA(String.class), isA(Object[].class), isA(FieldType[].class))).andReturn(1);
		compiledStmt.close();
		replay(databaseConnection, preparedStmt, compiledStmt, results);
		SelectIterator<BaseFoo, String> iterator =
				new SelectIterator<BaseFoo, String>(BaseFoo.class, baseFooDao, preparedStmt, connectionSource,
						databaseConnection, compiledStmt, stmt);
		assertTrue(iterator.hasNext());
		assertSame(baseFoo, iterator.next());
		iterator.remove();
		iterator.close();
		verify(databaseConnection, preparedStmt, compiledStmt, results);
	}

	@Test(expected = IllegalStateException.class)
	public void testIteratorRemoveThrows() throws Exception {
		startDao(false);
		@SuppressWarnings("unchecked")
		PreparedQuery<BaseFoo> preparedStmt = createMock(PreparedQuery.class);
		CompiledStatement compiledStmt = createMock(CompiledStatement.class);
		String stmt = "select * from baseFoo";
		DatabaseResults results = createMock(DatabaseResults.class);
		expect(results.next()).andReturn(true);
		expect(compiledStmt.runQuery()).andReturn(results);
		BaseFoo baseFoo = new BaseFoo();
		expect(preparedStmt.mapRow(results)).andReturn(baseFoo);
		expect(databaseConnection.delete(isA(String.class), isA(Object[].class), isA(FieldType[].class))).andThrow(
				new SQLException("expected exception"));
		compiledStmt.close();
		replay(databaseConnection, preparedStmt, compiledStmt, results);
		SelectIterator<BaseFoo, String> iterator =
				new SelectIterator<BaseFoo, String>(BaseFoo.class, baseFooDao, preparedStmt, connectionSource,
						databaseConnection, compiledStmt, stmt);
		assertTrue(iterator.hasNext());
		assertSame(baseFoo, iterator.next());
		iterator.remove();
	}

	@Test(expected = IllegalStateException.class)
	public void testIteratorRemoveNoDao() throws Exception {
		startDao(false);
		@SuppressWarnings("unchecked")
		PreparedQuery<BaseFoo> preparedStmt = createMock(PreparedQuery.class);
		CompiledStatement compiledStmt = createMock(CompiledStatement.class);
		String stmt = "select * from baseFoo";
		DatabaseResults results = createMock(DatabaseResults.class);
		expect(results.next()).andReturn(true);
		expect(compiledStmt.runQuery()).andReturn(results);
		BaseFoo baseFoo = new BaseFoo();
		expect(preparedStmt.mapRow(results)).andReturn(baseFoo);
		expect(databaseConnection.delete(isA(String.class), isA(Object[].class), isA(FieldType[].class))).andReturn(1);
		compiledStmt.close();
		replay(databaseConnection, preparedStmt, compiledStmt, results);
		SelectIterator<BaseFoo, String> iterator =
				new SelectIterator<BaseFoo, String>(BaseFoo.class, null, preparedStmt, connectionSource,
						databaseConnection, compiledStmt, stmt);
		assertTrue(iterator.hasNext());
		assertSame(baseFoo, iterator.next());
		iterator.remove();
	}

	private void startDao(boolean readWrite) throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(connectionSource, BaseFoo.class) {
		};
		assertEquals(BaseFoo.class, dao.getDataClass());

		databaseConnection = createMock(DatabaseConnection.class);
		connectionSource.setDatabaseConnection(databaseConnection);
		dao.initialize();
		baseFooDao = dao;
	}
}
