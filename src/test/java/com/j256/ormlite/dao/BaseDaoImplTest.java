package com.j256.ormlite.dao;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.easymock.IAnswer;
import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.stmt.GenericRowMapper;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.PreparedUpdate;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;
import com.j256.ormlite.table.DatabaseTableConfig;

public class BaseDaoImplTest extends BaseCoreTest {

	private DatabaseConnection databaseConnection;
	private Dao<BaseFoo, String> baseFooDao;

	@Test
	public void testDoubleInitialize() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(connectionSource, BaseFoo.class) {
		};
		dao.initialize();
		// this shouldn't barf
		dao.initialize();
	}

	@Test(expected = IllegalStateException.class)
	public void testInitNoConnectionSource() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(BaseFoo.class) {
		};
		dao.initialize();
	}

	@Test
	public void testCreate() throws Exception {
		startDao(true);
		int linesAffected = 1;
		expect(databaseConnection.insert(isA(String.class), isA(Object[].class), isA(FieldType[].class))).andReturn(
				linesAffected);
		replay(databaseConnection);
		assertEquals(linesAffected, baseFooDao.create(new BaseFoo()));
		verify(databaseConnection);
	}

	@Test(expected = SQLException.class)
	public void testCreateThrow() throws Exception {
		startDao(true);
		expect(databaseConnection.insert(isA(String.class), isA(Object[].class), isA(FieldType[].class))).andThrow(
				new SQLException("expected"));
		replay(databaseConnection);
		baseFooDao.create(new BaseFoo());
	}

	@Test
	public void testCreateNull() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(connectionSource, BaseFoo.class) {
		};
		dao.initialize();
		assertEquals(0, dao.create(null));
	}

	@Test(expected = IllegalStateException.class)
	public void testCreateNoInit() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(BaseFoo.class) {
		};
		dao.setConnectionSource(connectionSource);
		assertEquals(0, dao.create(null));
	}

	@Test
	public void testUpdate() throws Exception {
		startDao(true);
		int linesAffected = 1;
		expect(databaseConnection.update(isA(String.class), isA(Object[].class), isA(FieldType[].class))).andReturn(
				linesAffected);
		replay(databaseConnection);
		assertEquals(linesAffected, baseFooDao.update(new BaseFoo()));
		verify(databaseConnection);
	}

	@Test(expected = SQLException.class)
	public void testUpdateThrow() throws Exception {
		startDao(true);
		expect(databaseConnection.update(isA(String.class), isA(Object[].class), isA(FieldType[].class))).andThrow(
				new SQLException("expected"));
		replay(databaseConnection);
		baseFooDao.update(new BaseFoo());
	}

	@Test
	public void testUpdateNull() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(connectionSource, BaseFoo.class) {
		};
		dao.initialize();
		assertEquals(0, dao.update((BaseFoo) null));
	}

	@Test(expected = IllegalStateException.class)
	public void testUpdateNoInit() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(BaseFoo.class) {
		};
		dao.setConnectionSource(connectionSource);
		assertEquals(0, dao.update((BaseFoo) null));
	}

	@Test
	public void testUpdateId() throws Exception {
		startDao(true);
		int linesAffected = 1;
		expect(databaseConnection.update(isA(String.class), isA(Object[].class), isA(FieldType[].class))).andReturn(
				linesAffected);
		replay(databaseConnection);
		assertEquals(linesAffected, baseFooDao.updateId(new BaseFoo(), "new"));
		verify(databaseConnection);
	}

	@Test
	public void testUpdateIdNull() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(connectionSource, BaseFoo.class) {
		};
		dao.initialize();
		assertEquals(0, dao.updateId(null, null));
	}

	@Test(expected = IllegalStateException.class)
	public void testUpdateIdNoInit() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(BaseFoo.class) {
		};
		dao.setConnectionSource(connectionSource);
		assertEquals(0, dao.updateId(null, null));
	}

	@Test(expected = SQLException.class)
	public void testUpdateIdThrow() throws Exception {
		startDao(true);
		expect(databaseConnection.update(isA(String.class), isA(Object[].class), isA(FieldType[].class))).andThrow(
				new SQLException("expected"));
		replay(databaseConnection);
		baseFooDao.updateId(new BaseFoo(), "new");
	}

	@Test
	public void testUpdatePrepared() throws Exception {
		startDao(false);
		CompiledStatement stmt = createMock(CompiledStatement.class);
		expect(
				databaseConnection.compileStatement(isA(String.class), isA(StatementType.class),
						isA(FieldType[].class), isA(FieldType[].class))).andReturn(stmt);
		int rowN = 123;
		expect(stmt.executeUpdate()).andReturn(rowN);
		stmt.close();
		replay(databaseConnection, stmt);
		UpdateBuilder<BaseFoo, String> builder = baseFooDao.updateBuilder();
		builder.updateColumnExpression("val", "= 2");
		PreparedUpdate<BaseFoo> preparedStmt = builder.prepare();
		int changedN = baseFooDao.update(preparedStmt);
		assertEquals(rowN, changedN);
		verify(databaseConnection, stmt);
	}

	@Test
	public void testDelete() throws Exception {
		startDao(true);
		int linesAffected = 1;
		expect(databaseConnection.delete(isA(String.class), isA(Object[].class), isA(FieldType[].class))).andReturn(
				linesAffected);
		replay(databaseConnection);
		assertEquals(linesAffected, baseFooDao.delete(new BaseFoo()));
		verify(databaseConnection);
	}

	@Test(expected = SQLException.class)
	public void testDeleteThrow() throws Exception {
		startDao(true);
		expect(databaseConnection.delete(isA(String.class), isA(Object[].class), isA(FieldType[].class))).andThrow(
				new SQLException("expected"));
		replay(databaseConnection);
		baseFooDao.delete(new BaseFoo());
	}

	@Test
	public void testDeleteNull() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(connectionSource, BaseFoo.class) {
		};
		dao.initialize();
		assertEquals(0, dao.delete((BaseFoo) null));
	}

	@Test(expected = IllegalStateException.class)
	public void testDeleteNoInit() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(BaseFoo.class) {
		};
		dao.setConnectionSource(connectionSource);
		assertEquals(0, dao.delete((BaseFoo) null));
	}

	@Test
	public void testDeleteCollection() throws Exception {
		startDao(true);
		int linesAffected = 1;
		expect(databaseConnection.delete(isA(String.class), isA(Object[].class), isA(FieldType[].class))).andReturn(
				linesAffected);
		replay(databaseConnection);
		List<BaseFoo> fooList = new ArrayList<BaseFoo>();
		BaseFoo foo = new BaseFoo();
		fooList.add(foo);
		assertEquals(linesAffected, baseFooDao.delete(fooList));
		verify(databaseConnection);
	}

	@Test
	public void testDeleteEmptyCollection() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(connectionSource, BaseFoo.class) {
		};
		dao.initialize();
		List<BaseFoo> fooList = new ArrayList<BaseFoo>();
		assertEquals(0, dao.delete(fooList));
	}

	@Test(expected = SQLException.class)
	public void testDeleteCollectionThrow() throws Exception {
		startDao(true);
		expect(databaseConnection.delete(isA(String.class), isA(Object[].class), isA(FieldType[].class))).andThrow(
				new SQLException("expected"));
		replay(databaseConnection);
		List<BaseFoo> fooList = new ArrayList<BaseFoo>();
		BaseFoo foo = new BaseFoo();
		fooList.add(foo);
		baseFooDao.delete(fooList);
	}

	@Test(expected = IllegalStateException.class)
	public void testDeleteCollectionNoInit() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(BaseFoo.class) {
		};
		dao.setConnectionSource(connectionSource);
		assertEquals(0, dao.delete((List<BaseFoo>) null));
	}

	@Test
	public void testDeleteIds() throws Exception {
		startDao(true);
		int linesAffected = 1;
		expect(databaseConnection.delete(isA(String.class), isA(Object[].class), isA(FieldType[].class))).andReturn(
				linesAffected);
		replay(databaseConnection);
		List<String> idList = new ArrayList<String>();
		BaseFoo foo = new BaseFoo();
		idList.add(foo.id);
		assertEquals(linesAffected, baseFooDao.deleteIds(idList));
		verify(databaseConnection);
	}

	@Test(expected = SQLException.class)
	public void testDeleteIdsThrow() throws Exception {
		startDao(true);
		expect(databaseConnection.delete(isA(String.class), isA(Object[].class), isA(FieldType[].class))).andThrow(
				new SQLException("expected"));
		replay(databaseConnection);
		List<String> idList = new ArrayList<String>();
		BaseFoo foo = new BaseFoo();
		idList.add(foo.id);
		baseFooDao.deleteIds(idList);
	}

	@Test
	public void testDeleteIdsEmpty() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(connectionSource, BaseFoo.class) {
		};
		dao.initialize();
		List<String> fooList = new ArrayList<String>();
		assertEquals(0, dao.deleteIds(fooList));
	}

	@Test(expected = IllegalStateException.class)
	public void testDeleteIdsNoInit() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(BaseFoo.class) {
		};
		dao.setConnectionSource(connectionSource);
		assertEquals(0, dao.deleteIds((List<String>) null));
	}

	@Test
	public void testDeletePreparedStatement() throws Exception {
		startDao(true);
		@SuppressWarnings("unchecked")
		PreparedDelete<BaseFoo> stmt = createMock(PreparedDelete.class);
		CompiledStatement compiledStmt = createMock(CompiledStatement.class);
		int deleteN = 1002;
		expect(compiledStmt.executeUpdate()).andReturn(deleteN);
		expect(stmt.compile(databaseConnection)).andReturn(compiledStmt);
		compiledStmt.close();
		replay(databaseConnection, stmt, compiledStmt);
		assertEquals(deleteN, baseFooDao.delete(stmt));
		verify(compiledStmt, stmt, databaseConnection);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testRefresh() throws Exception {
		startDao(false);
		int linesAffected = 1;
		BaseFoo foo = new BaseFoo();
		expect(
				databaseConnection.queryForOne(isA(String.class), isA(Object[].class), isA(FieldType[].class),
						isA(GenericRowMapper.class))).andReturn(foo);
		replay(databaseConnection);
		assertEquals(linesAffected, baseFooDao.refresh(foo));
		verify(databaseConnection);
	}

	@SuppressWarnings("unchecked")
	@Test(expected = SQLException.class)
	public void testRefreshThrow() throws Exception {
		startDao(false);
		expect(
				databaseConnection.queryForOne(isA(String.class), isA(Object[].class), isA(FieldType[].class),
						isA(GenericRowMapper.class))).andThrow(new SQLException("expected"));
		replay(databaseConnection);
		baseFooDao.refresh(new BaseFoo());
	}

	@Test
	public void testRefreshNull() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(connectionSource, BaseFoo.class) {
		};
		dao.initialize();
		assertEquals(0, dao.refresh(null));
	}

	@Test(expected = IllegalStateException.class)
	public void testRefreshNoInit() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(BaseFoo.class) {
		};
		dao.setConnectionSource(connectionSource);
		assertEquals(0, dao.refresh(null));
	}

	@Test
	public void testAnotherConstructor() throws Exception {
		new BaseDaoImpl<BaseFoo, String>(BaseFoo.class) {
		};
	}

	@Test
	public void testAnotherConstructor2() throws Exception {
		DatabaseTableConfig<BaseFoo> tableConfig = DatabaseTableConfig.fromClass(connectionSource, BaseFoo.class);
		new BaseDaoImpl<BaseFoo, String>(tableConfig) {
		};
	}

	@Test(expected = IllegalStateException.class)
	public void testNoDatabaseType() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(BaseFoo.class) {
		};
		dao.initialize();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testQueryForId() throws Exception {
		startDao(false);
		BaseFoo foo = new BaseFoo();
		expect(
				databaseConnection.queryForOne(isA(String.class), isA(Object[].class), isA(FieldType[].class),
						isA(GenericRowMapper.class))).andReturn(foo);
		replay(databaseConnection);
		assertSame(foo, baseFooDao.queryForId("foo"));
		verify(databaseConnection);
	}

	@SuppressWarnings("unchecked")
	@Test(expected = SQLException.class)
	public void testQueryForIdThrow() throws Exception {
		startDao(false);
		expect(
				databaseConnection.queryForOne(isA(String.class), isA(Object[].class), isA(FieldType[].class),
						isA(GenericRowMapper.class))).andThrow(new SQLException("expectd"));
		replay(databaseConnection);
		baseFooDao.queryForId("foo");
	}

	@Test(expected = IllegalStateException.class)
	public void testQueryForIdNoInit() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(BaseFoo.class) {
		};
		dao.queryForId("foo");
	}

	@Test
	public void testQueryForAll() throws Exception {
		startDao(false);
		CompiledStatement stmt = createMock(CompiledStatement.class);
		expect(
				databaseConnection.compileStatement(isA(String.class), isA(StatementType.class),
						isA(FieldType[].class), isA(FieldType[].class))).andReturn(stmt);
		DatabaseResults results = createMock(DatabaseResults.class);
		expect(results.next()).andReturn(false);
		expect(stmt.executeQuery()).andReturn(results);
		stmt.close();
		replay(databaseConnection, stmt, results);
		List<BaseFoo> list = baseFooDao.queryForAll();
		assertNotNull(list);
		assertEquals(0, list.size());
		verify(databaseConnection, stmt, results);
	}

	@Test(expected = IllegalStateException.class)
	public void testQueryForAllNoInit() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(BaseFoo.class) {
		};
		dao.queryForAll();
	}

	@Test
	public void testQueryForFirst() throws Exception {
		startDao(false);
		CompiledStatement stmt = createMock(CompiledStatement.class);
		expect(
				databaseConnection.compileStatement(isA(String.class), isA(StatementType.class),
						isA(FieldType[].class), isA(FieldType[].class))).andReturn(stmt);
		DatabaseResults results = createMock(DatabaseResults.class);
		expect(results.next()).andReturn(true);
		int idCol = 1;
		expect(results.findColumn(BaseFoo.ID_COLUMN_NAME)).andReturn(idCol);
		expect(results.isNull(idCol)).andReturn(false);
		String id = "id";
		expect(results.getString(idCol)).andReturn(id);
		int valCol = 1;
		int val = 13123123;
		expect(results.findColumn(BaseFoo.VAL_COLUMN_NAME)).andReturn(valCol);
		expect(results.getInt(valCol)).andReturn(val);
		int equalCol = 3;
		int equal = 3123341;
		expect(results.findColumn(BaseFoo.EQUAL_COLUMN_NAME)).andReturn(equalCol);
		expect(results.getInt(equalCol)).andReturn(equal);
		int nullCol = 4;
		expect(results.findColumn(BaseFoo.NULL_COLUMN_NAME)).andReturn(nullCol);
		expect(results.isNull(nullCol)).andReturn(true);
		expect(stmt.executeQuery()).andReturn(results);
		stmt.close();
		replay(databaseConnection, stmt, results);
		QueryBuilder<BaseFoo, String> builder = baseFooDao.queryBuilder();
		PreparedQuery<BaseFoo> preparedStmt = builder.prepare();
		BaseFoo baseFoo = baseFooDao.queryForFirst(preparedStmt);
		assertNotNull(baseFoo);
		assertEquals(id, baseFoo.id);
		assertEquals(val, baseFoo.val);
		assertEquals(equal, baseFoo.equal);
		assertNull(baseFoo.nullField);
		verify(databaseConnection, stmt, results);
	}

	@Test
	public void testQueryForFirstNoResults() throws Exception {
		startDao(false);
		CompiledStatement stmt = createMock(CompiledStatement.class);
		expect(
				databaseConnection.compileStatement(isA(String.class), isA(StatementType.class),
						isA(FieldType[].class), isA(FieldType[].class))).andReturn(stmt);
		DatabaseResults results = createMock(DatabaseResults.class);
		expect(results.next()).andReturn(false);
		expect(stmt.executeQuery()).andReturn(results);
		stmt.close();
		replay(databaseConnection, stmt, results);
		QueryBuilder<BaseFoo, String> builder = baseFooDao.queryBuilder();
		PreparedQuery<BaseFoo> preparedStmt = builder.prepare();
		assertNull(baseFooDao.queryForFirst(preparedStmt));
		verify(databaseConnection, stmt, results);
	}

	@Test(expected = IllegalStateException.class)
	public void testStatementBuilderNoInit() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(BaseFoo.class) {
		};
		dao.queryBuilder();
	}

	@Test(expected = SQLException.class)
	public void testQueryForFirstThrow() throws Exception {
		startDao(false);
		CompiledStatement stmt = createMock(CompiledStatement.class);
		expect(
				databaseConnection.compileStatement(isA(String.class), isA(StatementType.class),
						isA(FieldType[].class), isA(FieldType[].class))).andReturn(stmt);
		expect(stmt.executeQuery()).andThrow(new SQLException("expected"));
		stmt.close();
		replay(databaseConnection);
		replay(stmt);
		QueryBuilder<BaseFoo, String> builder = baseFooDao.queryBuilder();
		PreparedQuery<BaseFoo> preparedStmt = builder.prepare();
		baseFooDao.queryForFirst(preparedStmt);
	}

	@Test(expected = IllegalStateException.class)
	public void testQueryForFirstNoInit() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(BaseFoo.class) {
		};
		dao.queryForFirst(null);
	}

	@Test
	public void testQueryForPrepared() throws Exception {
		startDao(false);
		CompiledStatement stmt = createMock(CompiledStatement.class);
		expect(
				databaseConnection.compileStatement(isA(String.class), isA(StatementType.class),
						isA(FieldType[].class), isA(FieldType[].class))).andReturn(stmt);
		DatabaseResults results = createMock(DatabaseResults.class);
		expect(results.next()).andReturn(false);
		expect(stmt.executeQuery()).andReturn(results);
		stmt.close();
		replay(databaseConnection, stmt, results);
		QueryBuilder<BaseFoo, String> builder = baseFooDao.queryBuilder();
		PreparedQuery<BaseFoo> preparedStmt = builder.prepare();
		List<BaseFoo> list = baseFooDao.query(preparedStmt);
		assertNotNull(list);
		assertEquals(0, list.size());
		verify(databaseConnection, stmt, results);
	}

	@Test(expected = IllegalStateException.class)
	public void testQueryForPreparedNoInit() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(BaseFoo.class) {
		};
		dao.query((PreparedQuery<BaseFoo>) null);
	}

	@Test
	@SuppressWarnings("deprecation")
	public void testQueryForAllRaw() throws Exception {
		startDao(false);
		CompiledStatement stmt = createMock(CompiledStatement.class);
		expect(
				databaseConnection.compileStatement(isA(String.class), isA(StatementType.class),
						isA(FieldType[].class), isA(FieldType[].class))).andReturn(stmt);
		DatabaseResults results = createMock(DatabaseResults.class);
		expect(results.next()).andReturn(false);
		expect(stmt.executeQuery()).andReturn(results);
		int numColumns = 1;
		expect(stmt.getColumnCount()).andReturn(numColumns);
		String columnName = "foo";
		expect(stmt.getColumnName(1)).andReturn(columnName);
		stmt.close();
		replay(databaseConnection, stmt, results);
		RawResults list = baseFooDao.queryForAllRaw("SELECT * FROM basefoo");
		assertNotNull(list);
		String[] names = list.getColumnNames();
		assertNotNull(names);
		assertEquals(1, names.length);
		assertEquals(columnName, names[0]);
		assertFalse(list.iterator().hasNext());
		verify(databaseConnection, stmt, results);
	}

	@Test(expected = SQLException.class)
	@SuppressWarnings("deprecation")
	public void testQueryForAllRawThrows() throws Exception {
		startDao(false);
		expect(
				databaseConnection.compileStatement(isA(String.class), isA(StatementType.class),
						isA(FieldType[].class), isA(FieldType[].class))).andThrow(new SQLException("expected"));
		replay(databaseConnection);
		baseFooDao.queryForAllRaw("SELECT * FROM basefoo");
	}

	@Test
	@SuppressWarnings("deprecation")
	public void testQueryForAllRawList() throws Exception {
		startDao(false);
		CompiledStatement stmt = createMock(CompiledStatement.class);
		expect(
				databaseConnection.compileStatement(isA(String.class), isA(StatementType.class),
						isA(FieldType[].class), isA(FieldType[].class))).andReturn(stmt);
		DatabaseResults results = createMock(DatabaseResults.class);
		int numColumns = 1;
		expect(results.getColumnCount()).andReturn(numColumns);
		expect(results.next()).andReturn(true);
		String value = "stuff";
		expect(results.getString(1)).andReturn(value);
		expect(results.next()).andReturn(false);
		expect(stmt.executeQuery()).andReturn(results);
		expect(stmt.getColumnCount()).andReturn(numColumns);
		String columnName = "foo";
		expect(stmt.getColumnName(1)).andReturn(columnName);
		stmt.close();
		replay(databaseConnection, stmt, results);
		RawResults rawResults = baseFooDao.queryForAllRaw("SELECT * FROM basefoo");
		assertNotNull(rawResults);
		String[] names = rawResults.getColumnNames();
		assertNotNull(names);
		assertEquals(1, names.length);
		assertEquals(columnName, names[0]);
		List<String[]> resultList = rawResults.getResults();
		assertEquals(1, resultList.size());
		String[] result = resultList.get(0);
		assertEquals(1, result.length);
		assertEquals(value, result[0]);
		verify(databaseConnection, stmt, results);
	}

	@Test(expected = IllegalStateException.class)
	public void testQueryForRawNoInit() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(BaseFoo.class) {
		};
		dao.queryForAllRaw("select * from foo");
	}

	@Test
	public void testObjectToString() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(connectionSource, BaseFoo.class) {
		};
		dao.initialize();
		BaseFoo foo = new BaseFoo();
		String idStr = "qdqd";
		foo.id = idStr;
		String objStr = dao.objectToString(foo);
		assertTrue(objStr.contains("id=" + idStr));
	}

	@Test(expected = IllegalStateException.class)
	public void testObjectToStringNoInit() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(BaseFoo.class) {
		};
		dao.setConnectionSource(connectionSource);
		dao.objectToString(new BaseFoo());
	}

	@Test
	public void testObjectsEqual() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(connectionSource, BaseFoo.class) {
		};
		dao.initialize();
		BaseFoo foo = new BaseFoo();
		foo.id = "qdqd";
		foo.val = 123123;
		BaseFoo bar = new BaseFoo();
		assertTrue(dao.objectsEqual(foo, foo));
		assertFalse(dao.objectsEqual(foo, bar));
		assertFalse(dao.objectsEqual(bar, foo));
		assertTrue(dao.objectsEqual(bar, bar));
		bar.id = "wqdpq";
		bar.val = foo.val;
		assertFalse(dao.objectsEqual(bar, foo));
	}

	@Test(expected = IllegalStateException.class)
	public void testObjectsEqualNoInit() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(BaseFoo.class) {
		};
		dao.setConnectionSource(connectionSource);
		dao.objectsEqual(new BaseFoo(), new BaseFoo());
	}

	@Test
	public void testIterator() throws Exception {
		startDao(false);
		CompiledStatement stmt = createMock(CompiledStatement.class);
		DatabaseResults results = createMock(DatabaseResults.class);
		expect(results.next()).andReturn(false);
		expect(stmt.executeQuery()).andReturn(results);
		stmt.close();
		expect(
				databaseConnection.compileStatement(isA(String.class), isA(StatementType.class),
						isA(FieldType[].class), isA(FieldType[].class))).andReturn(stmt);
		replay(databaseConnection, stmt, results);
		CloseableIterator<BaseFoo> iterator = baseFooDao.iterator();
		assertFalse(iterator.hasNext());
		iterator.close();
		assertFalse(iterator.hasNext());
		assertNull(iterator.next());
		verify(databaseConnection, stmt, results);
	}

	@Test(expected = IllegalStateException.class)
	public void testIteratorThrow() throws Exception {
		startDao(false);
		expect(
				databaseConnection.compileStatement(isA(String.class), isA(StatementType.class),
						isA(FieldType[].class), isA(FieldType[].class))).andThrow(new SQLException("expected"));
		replay(databaseConnection);
		baseFooDao.iterator();
	}

	@Test(expected = IllegalStateException.class)
	public void testIteratorNoInit() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(BaseFoo.class) {
		};
		dao.iterator();
	}

	@Test
	public void testIteratorPrepared() throws Exception {
		startDao(false);
		@SuppressWarnings("unchecked")
		PreparedQuery<BaseFoo> stmt = createMock(PreparedQuery.class);
		CompiledStatement compiled = createMock(CompiledStatement.class);
		expect(stmt.compile(databaseConnection)).andReturn(compiled);
		expect(stmt.getStatement()).andReturn("select * from foo");
		DatabaseResults results = createMock(DatabaseResults.class);
		expect(results.next()).andReturn(false);
		expect(compiled.executeQuery()).andReturn(results);
		compiled.close();
		replay(databaseConnection, stmt, compiled, results);
		CloseableIterator<BaseFoo> iterator = baseFooDao.iterator(stmt);
		assertFalse(iterator.hasNext());
		verify(databaseConnection, stmt, compiled, results);
	}

	@Test(expected = SQLException.class)
	public void testIteratorPreparedThrow() throws Exception {
		startDao(false);
		@SuppressWarnings("unchecked")
		PreparedQuery<BaseFoo> stmt = createMock(PreparedQuery.class);
		expect(stmt.compile(databaseConnection)).andThrow(new SQLException("expected"));
		expect(stmt.getStatement()).andReturn("select * from foo");
		replay(databaseConnection, stmt);
		CloseableIterator<BaseFoo> iterator = baseFooDao.iterator(stmt);
		assertFalse(iterator.hasNext());
		verify(databaseConnection, stmt);
	}

	@Test(expected = IllegalStateException.class)
	public void testIteratorPreparedNoInit() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(BaseFoo.class) {
		};
		dao.iterator((PreparedQuery<BaseFoo>) null);
	}

	@Test(expected = IllegalStateException.class)
	public void testIteratorRawNoInit() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(BaseFoo.class) {
		};
		dao.iteratorRaw("select * from foo");
	}

	@Test
	public void testTableConfig() throws Exception {
		DatabaseTableConfig<BaseFoo> config = DatabaseTableConfig.fromClass(connectionSource, BaseFoo.class);
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(connectionSource, config) {
		};
		assertSame(config, dao.getTableConfig());
	}

	@Test
	public void testSetters() throws Exception {
		DatabaseTableConfig<BaseFoo> config = DatabaseTableConfig.fromClass(connectionSource, BaseFoo.class);
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(BaseFoo.class) {
		};
		dao.setTableConfig(config);
		dao.setConnectionSource(connectionSource);
		assertSame(config, dao.getTableConfig());
	}

	@Test
	public void testCreateDao() throws Exception {
		Dao<BaseFoo, String> dao = BaseDaoImpl.createDao(connectionSource, BaseFoo.class);
		DatabaseConnection databaseConnection = createMock(DatabaseConnection.class);
		connectionSource.setDatabaseConnection(databaseConnection);
		expect(databaseConnection.insert(isA(String.class), isA(Object[].class), isA(FieldType[].class))).andReturn(1);
		replay(databaseConnection);
		assertEquals(1, dao.create(new BaseFoo()));
	}

	@Test
	@SuppressWarnings("deprecation")
	public void testIteratorRaw() throws Exception {
		startDao(false);
		CompiledStatement stmt = createMock(CompiledStatement.class);
		expect(
				databaseConnection.compileStatement(isA(String.class), isA(StatementType.class),
						isA(FieldType[].class), isA(FieldType[].class))).andReturn(stmt);
		DatabaseResults results = createMock(DatabaseResults.class);
		expect(results.next()).andReturn(true);
		expect(results.next()).andReturn(false);
		int numColumns = 1;
		String columnName = "column";
		String val = "wodqjdqw";
		expect(results.getColumnCount()).andReturn(numColumns);
		expect(results.getString(1)).andReturn(val);
		expect(stmt.executeQuery()).andReturn(results);
		expect(stmt.getColumnCount()).andReturn(numColumns);
		expect(stmt.getColumnName(1)).andReturn(columnName);
		stmt.close();
		replay(databaseConnection, stmt, results);
		RawResults list = baseFooDao.iteratorRaw("SELECT * FROM basefoo");
		assertNotNull(list);
		String[] names = list.getColumnNames();
		assertNotNull(names);
		assertEquals(1, names.length);
		assertEquals(columnName, names[0]);
		CloseableIterator<String[]> iterator = list.iterator();
		assertTrue(iterator.hasNext());
		String[] result = iterator.next();
		assertEquals(1, result.length);
		assertEquals(val, result[0]);
		assertFalse(iterator.hasNext());
		iterator.close();
		verify(databaseConnection, stmt, results);
	}

	@Test(expected = SQLException.class)
	@SuppressWarnings("deprecation")
	public void testIteratorRawThrow() throws Exception {
		startDao(false);
		expect(
				databaseConnection.compileStatement(isA(String.class), isA(StatementType.class),
						isA(FieldType[].class), isA(FieldType[].class))).andThrow(new SQLException("expected"));
		replay(databaseConnection);
		baseFooDao.iteratorRaw("SELECT * FROM basefoo");
	}

	@Test
	public void testQueryRawStrings() throws Exception {
		startDao(false);
		CompiledStatement stmt = createMock(CompiledStatement.class);
		expect(
				databaseConnection.compileStatement(isA(String.class), isA(StatementType.class),
						isA(FieldType[].class), isA(FieldType[].class))).andReturn(stmt);
		DatabaseResults results = createMock(DatabaseResults.class);
		expect(results.next()).andReturn(true);
		int numColumns = 2;
		String id = "wodqjdqw";
		int val = 123213;
		expect(results.getColumnCount()).andReturn(numColumns);
		expect(results.getString(1)).andReturn(id);
		expect(results.getString(2)).andReturn(Integer.toString(val));
		expect(results.next()).andReturn(false);
		expect(stmt.executeQuery()).andReturn(results);
		expect(stmt.getColumnCount()).andReturn(numColumns);
		expect(stmt.getColumnName(1)).andReturn(BaseFoo.ID_COLUMN_NAME);
		expect(stmt.getColumnName(2)).andReturn(BaseFoo.VAL_COLUMN_NAME);
		stmt.close();

		replay(databaseConnection, stmt, results);
		GenericRawResults<String[]> list = baseFooDao.queryRaw("SELECT * FROM basefoo");
		assertNotNull(list);
		String[] names = list.getColumnNames();
		assertNotNull(names);
		assertEquals(2, names.length);
		assertEquals(BaseFoo.ID_COLUMN_NAME, names[0]);
		assertEquals(BaseFoo.VAL_COLUMN_NAME, names[1]);
		CloseableIterator<String[]> iterator = list.iterator();
		assertTrue(iterator.hasNext());
		String[] result = iterator.next();
		assertEquals(2, result.length);
		assertEquals(id, result[0]);
		assertEquals(Integer.toString(val), result[1]);
		assertFalse(iterator.hasNext());
		iterator.close();

		verify(databaseConnection, stmt, results);
	}

	@Test(expected = SQLException.class)
	public void testQueryRawStringsThrow() throws Exception {
		startDao(false);
		expect(
				databaseConnection.compileStatement(isA(String.class), isA(StatementType.class),
						isA(FieldType[].class), isA(FieldType[].class))).andThrow(new SQLException("expected"));

		replay(databaseConnection);
		baseFooDao.queryRaw("SELECT * FROM basefoo");
	}

	@Test
	public void testQueryRawObjects() throws Exception {
		startDao(false);
		CompiledStatement stmt = createMock(CompiledStatement.class);
		expect(
				databaseConnection.compileStatement(isA(String.class), isA(StatementType.class),
						isA(FieldType[].class), isA(FieldType[].class))).andReturn(stmt);
		DatabaseResults results = createMock(DatabaseResults.class);
		expect(results.next()).andReturn(true);
		int numColumns = 2;
		String id = "wodqjdqw";
		int val = 123213;
		expect(results.getColumnCount()).andReturn(numColumns);
		expect(results.getString(1)).andReturn(id);
		expect(results.getInt(2)).andReturn(val);
		expect(stmt.executeQuery()).andReturn(results);
		expect(stmt.getColumnCount()).andReturn(numColumns);
		expect(stmt.getColumnName(1)).andReturn(BaseFoo.ID_COLUMN_NAME);
		expect(stmt.getColumnName(2)).andReturn(BaseFoo.VAL_COLUMN_NAME);
		stmt.close();
		replay(databaseConnection, stmt, results);
		GenericRawResults<Object[]> rawResults =
				baseFooDao.queryRaw("SELECT * FROM basefoo", new DataType[] { DataType.STRING, DataType.INTEGER });
		assertNotNull(rawResults);
		String[] names = rawResults.getColumnNames();
		assertNotNull(names);
		assertEquals(numColumns, names.length);
		assertEquals(BaseFoo.ID_COLUMN_NAME, names[0]);
		assertEquals(BaseFoo.VAL_COLUMN_NAME, names[1]);
		CloseableIterator<Object[]> iterator = rawResults.iterator();
		assertTrue(iterator.hasNext());
		Object[] objs = iterator.next();
		assertEquals(numColumns, objs.length);
		assertEquals(id, objs[0]);
		assertEquals(val, objs[1]);
		iterator.close();
		verify(databaseConnection, stmt, results);
	}

	@Test(expected = SQLException.class)
	public void testQueryRawObjectsThrows() throws Exception {
		startDao(false);
		expect(
				databaseConnection.compileStatement(isA(String.class), isA(StatementType.class),
						isA(FieldType[].class), isA(FieldType[].class))).andThrow(new SQLException("expected"));
		replay(databaseConnection);
		baseFooDao.queryRaw("SELECT * FROM basefoo", new DataType[] { DataType.STRING, DataType.INTEGER });
	}

	@Test
	public void testQueryRawMapped() throws Exception {
		startDao(false);
		CompiledStatement stmt = createMock(CompiledStatement.class);
		expect(
				databaseConnection.compileStatement(isA(String.class), isA(StatementType.class),
						isA(FieldType[].class), isA(FieldType[].class))).andReturn(stmt);
		DatabaseResults results = createMock(DatabaseResults.class);
		expect(results.next()).andReturn(true);
		int numColumns = 2;
		final String id = "wodqjdqw";
		final int val = 123213;
		final String valStr = Integer.toString(val);
		expect(results.getColumnCount()).andReturn(numColumns);
		expect(results.getString(1)).andReturn(id);
		expect(results.getString(2)).andReturn(valStr);
		expect(stmt.executeQuery()).andReturn(results);
		expect(stmt.getColumnCount()).andReturn(numColumns);
		expect(stmt.getColumnName(1)).andReturn(BaseFoo.ID_COLUMN_NAME);
		expect(stmt.getColumnName(2)).andReturn(BaseFoo.VAL_COLUMN_NAME);
		stmt.close();
		@SuppressWarnings("unchecked")
		RawRowMapper<BaseFoo> mapper = createMock(RawRowMapper.class);
		final BaseFoo baseFoo1 = new BaseFoo();
		baseFoo1.id = id;
		baseFoo1.val = val;
		expect(mapper.mapRow(isA(String[].class), isA(String[].class))).andAnswer(new IAnswer<BaseFoo>() {
			public BaseFoo answer() throws Throwable {
				Object[] args = getCurrentArguments();
				assertEquals(2, args.length);
				assertTrue(Arrays.equals(new String[] { BaseFoo.ID_COLUMN_NAME, BaseFoo.VAL_COLUMN_NAME },
						(String[]) args[0]));
				assertTrue(Arrays.equals(new String[] { id, valStr }, (String[]) args[1]));
				return baseFoo1;
			}
		});
		replay(databaseConnection, stmt, results, mapper);
		GenericRawResults<BaseFoo> rawResults = baseFooDao.queryRaw("SELECT * FROM basefoo", mapper);
		assertNotNull(rawResults);
		String[] names = rawResults.getColumnNames();
		assertNotNull(names);
		assertEquals(numColumns, names.length);
		assertEquals(BaseFoo.ID_COLUMN_NAME, names[0]);
		assertEquals(BaseFoo.VAL_COLUMN_NAME, names[1]);
		CloseableIterator<BaseFoo> iterator = rawResults.iterator();
		assertTrue(iterator.hasNext());
		BaseFoo baseFoo2 = iterator.next();
		assertEquals(id, baseFoo2.id);
		assertEquals(val, baseFoo2.val);
		iterator.close();
		verify(databaseConnection, stmt, results, mapper);
	}

	@Test(expected = SQLException.class)
	public void testQueryRawMappedThrows() throws Exception {
		startDao(false);
		expect(
				databaseConnection.compileStatement(isA(String.class), isA(StatementType.class),
						isA(FieldType[].class), isA(FieldType[].class))).andThrow(new SQLException("expected"));
		@SuppressWarnings("unchecked")
		RawRowMapper<BaseFoo> mapper = createMock(RawRowMapper.class);
		replay(databaseConnection, mapper);
		baseFooDao.queryRaw("SELECT * FROM basefoo", mapper);
	}

	@Test(expected = IllegalStateException.class)
	public void testBadConnectionSource() throws Exception {
		ConnectionSource cs = createMock(ConnectionSource.class);
		new BaseDaoImpl<BaseFoo, String>(cs, BaseFoo.class) {
		};
	}

	@Test
	public void testUpdateBuilder() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(connectionSource, BaseFoo.class) {
		};
		dao.updateBuilder();
	}

	@Test
	public void testDeleteBuilder() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(connectionSource, BaseFoo.class) {
		};
		dao.deleteBuilder();
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
