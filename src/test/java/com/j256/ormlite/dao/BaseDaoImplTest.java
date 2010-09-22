package com.j256.ormlite.dao;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
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
import java.util.List;

import org.junit.Test;

import com.j256.ormlite.BaseOrmLiteCoreTest;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.stmt.GenericRowMapper;
import com.j256.ormlite.stmt.PreparedStmt;
import com.j256.ormlite.stmt.StatementBuilder;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;
import com.j256.ormlite.table.DatabaseTableConfig;

public class BaseDaoImplTest extends BaseOrmLiteCoreTest {

	private ConnectionSource connectionSource;
	private DatabaseConnection databaseConnection;
	private Dao<BaseFoo, String> baseFooDao;

	@Test
	public void testDoubleInitialize() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(databaseType, BaseFoo.class) {
		};
		dao.setConnectionSource(createMock(ConnectionSource.class));
		dao.initialize();
		// this shouldn't barf
		dao.initialize();
	}

	@Test(expected = IllegalStateException.class)
	public void testInitNoConnectionSource() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(databaseType, BaseFoo.class) {
		};
		dao.initialize();
	}

	@Test
	public void testCreate() throws Exception {
		startDao(true);
		int linesAffected = 1;
		expect(databaseConnection.insert(isA(String.class), isA(Object[].class), isA(SqlType[].class))).andReturn(
				linesAffected);
		replay(databaseConnection);
		assertEquals(linesAffected, baseFooDao.create(new BaseFoo()));
		finishDao();
	}

	@Test(expected = SQLException.class)
	public void testCreateThrow() throws Exception {
		startDao(true);
		expect(databaseConnection.insert(isA(String.class), isA(Object[].class), isA(SqlType[].class))).andThrow(
				new SQLException("expected"));
		replay(databaseConnection);
		baseFooDao.create(new BaseFoo());
	}

	@Test
	public void testCreateNull() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(databaseType, BaseFoo.class) {
		};
		dao.setConnectionSource(createMock(ConnectionSource.class));
		dao.initialize();
		assertEquals(0, dao.create(null));
	}

	@Test(expected = IllegalStateException.class)
	public void testCreateNoInit() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(databaseType, BaseFoo.class) {
		};
		assertEquals(0, dao.create(null));
	}

	@Test
	public void testUpdate() throws Exception {
		startDao(true);
		int linesAffected = 1;
		expect(databaseConnection.update(isA(String.class), isA(Object[].class), isA(SqlType[].class))).andReturn(
				linesAffected);
		replay(databaseConnection);
		assertEquals(linesAffected, baseFooDao.update(new BaseFoo()));
		finishDao();
	}

	@Test(expected = SQLException.class)
	public void testUpdateThrow() throws Exception {
		startDao(true);
		expect(databaseConnection.update(isA(String.class), isA(Object[].class), isA(SqlType[].class))).andThrow(
				new SQLException("expected"));
		replay(databaseConnection);
		baseFooDao.update(new BaseFoo());
	}

	@Test
	public void testUpdateNull() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(databaseType, BaseFoo.class) {
		};
		dao.setConnectionSource(createMock(ConnectionSource.class));
		dao.initialize();
		assertEquals(0, dao.update(null));
	}

	@Test(expected = IllegalStateException.class)
	public void testUpdateNoInit() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(databaseType, BaseFoo.class) {
		};
		assertEquals(0, dao.update(null));
	}

	@Test
	public void testUpdateId() throws Exception {
		startDao(true);
		int linesAffected = 1;
		expect(databaseConnection.update(isA(String.class), isA(Object[].class), isA(SqlType[].class))).andReturn(
				linesAffected);
		replay(databaseConnection);
		assertEquals(linesAffected, baseFooDao.updateId(new BaseFoo(), "new"));
		finishDao();
	}

	@Test
	public void testUpdateIdNull() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(databaseType, BaseFoo.class) {
		};
		dao.setConnectionSource(createMock(ConnectionSource.class));
		dao.initialize();
		assertEquals(0, dao.updateId(null, null));
	}

	@Test(expected = IllegalStateException.class)
	public void testUpdateIdNoInit() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(databaseType, BaseFoo.class) {
		};
		assertEquals(0, dao.updateId(null, null));
	}

	@Test(expected = SQLException.class)
	public void testUpdateIdThrow() throws Exception {
		startDao(true);
		expect(databaseConnection.update(isA(String.class), isA(Object[].class), isA(SqlType[].class))).andThrow(
				new SQLException("expected"));
		replay(databaseConnection);
		baseFooDao.updateId(new BaseFoo(), "new");
	}

	@Test
	public void testDelete() throws Exception {
		startDao(true);
		int linesAffected = 1;
		expect(databaseConnection.delete(isA(String.class), isA(Object[].class), isA(SqlType[].class))).andReturn(
				linesAffected);
		replay(databaseConnection);
		assertEquals(linesAffected, baseFooDao.delete(new BaseFoo()));
		finishDao();
	}

	@Test(expected = SQLException.class)
	public void testDeleteThrow() throws Exception {
		startDao(true);
		expect(databaseConnection.delete(isA(String.class), isA(Object[].class), isA(SqlType[].class))).andThrow(
				new SQLException("expected"));
		replay(databaseConnection);
		baseFooDao.delete(new BaseFoo());
	}

	@Test
	public void testDeleteNull() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(databaseType, BaseFoo.class) {
		};
		dao.setConnectionSource(createMock(ConnectionSource.class));
		dao.initialize();
		assertEquals(0, dao.delete((BaseFoo) null));
	}

	@Test(expected = IllegalStateException.class)
	public void testDeleteNoInit() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(databaseType, BaseFoo.class) {
		};
		assertEquals(0, dao.delete((BaseFoo) null));
	}

	@Test
	public void testDeleteCollection() throws Exception {
		startDao(true);
		int linesAffected = 1;
		expect(databaseConnection.delete(isA(String.class), isA(Object[].class), isA(SqlType[].class))).andReturn(
				linesAffected);
		replay(databaseConnection);
		List<BaseFoo> fooList = new ArrayList<BaseFoo>();
		BaseFoo foo = new BaseFoo();
		fooList.add(foo);
		assertEquals(linesAffected, baseFooDao.delete(fooList));
		finishDao();
	}

	@Test
	public void testDeleteEmptyCollection() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(databaseType, BaseFoo.class) {
		};
		dao.setConnectionSource(createMock(ConnectionSource.class));
		dao.initialize();
		List<BaseFoo> fooList = new ArrayList<BaseFoo>();
		assertEquals(0, dao.delete(fooList));
	}

	@Test(expected = SQLException.class)
	public void testDeleteCollectionThrow() throws Exception {
		startDao(true);
		expect(databaseConnection.delete(isA(String.class), isA(Object[].class), isA(SqlType[].class))).andThrow(
				new SQLException("expected"));
		replay(databaseConnection);
		List<BaseFoo> fooList = new ArrayList<BaseFoo>();
		BaseFoo foo = new BaseFoo();
		fooList.add(foo);
		baseFooDao.delete(fooList);
	}

	@Test(expected = IllegalStateException.class)
	public void testDeleteCollectionNoInit() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(databaseType, BaseFoo.class) {
		};
		assertEquals(0, dao.delete((List<BaseFoo>) null));
	}

	@Test
	public void testDeleteIds() throws Exception {
		startDao(true);
		int linesAffected = 1;
		expect(databaseConnection.delete(isA(String.class), isA(Object[].class), isA(SqlType[].class))).andReturn(
				linesAffected);
		replay(databaseConnection);
		List<String> idList = new ArrayList<String>();
		BaseFoo foo = new BaseFoo();
		idList.add(foo.id);
		assertEquals(linesAffected, baseFooDao.deleteIds(idList));
		finishDao();
	}

	@Test(expected = SQLException.class)
	public void testDeleteIdsThrow() throws Exception {
		startDao(true);
		expect(databaseConnection.delete(isA(String.class), isA(Object[].class), isA(SqlType[].class))).andThrow(
				new SQLException("expected"));
		replay(databaseConnection);
		List<String> idList = new ArrayList<String>();
		BaseFoo foo = new BaseFoo();
		idList.add(foo.id);
		baseFooDao.deleteIds(idList);
	}

	@Test
	public void testDeleteIdsEmpty() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(databaseType, BaseFoo.class) {
		};
		dao.setConnectionSource(createMock(ConnectionSource.class));
		dao.initialize();
		List<String> fooList = new ArrayList<String>();
		assertEquals(0, dao.deleteIds(fooList));
	}

	@Test(expected = IllegalStateException.class)
	public void testDeleteIdsNoInit() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(databaseType, BaseFoo.class) {
		};
		assertEquals(0, dao.deleteIds((List<String>) null));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testRefresh() throws Exception {
		startDao(false);
		int linesAffected = 1;
		BaseFoo foo = new BaseFoo();
		expect(
				databaseConnection.queryForOne(isA(String.class), isA(Object[].class), isA(SqlType[].class),
						isA(GenericRowMapper.class))).andReturn(foo);
		replay(databaseConnection);
		assertEquals(linesAffected, baseFooDao.refresh(foo));
		finishDao();
	}

	@SuppressWarnings("unchecked")
	@Test(expected = SQLException.class)
	public void testRefreshThrow() throws Exception {
		startDao(false);
		expect(
				databaseConnection.queryForOne(isA(String.class), isA(Object[].class), isA(SqlType[].class),
						isA(GenericRowMapper.class))).andThrow(new SQLException("expected"));
		replay(databaseConnection);
		baseFooDao.refresh(new BaseFoo());
	}

	@Test
	public void testRefreshNull() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(databaseType, BaseFoo.class) {
		};
		dao.setConnectionSource(createMock(ConnectionSource.class));
		dao.initialize();
		assertEquals(0, dao.refresh(null));
	}

	@Test(expected = IllegalStateException.class)
	public void testRefreshNoInit() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(databaseType, BaseFoo.class) {
		};
		assertEquals(0, dao.refresh(null));
	}

	@Test
	public void testAnotherConstructor() throws Exception {
		new BaseDaoImpl<BaseFoo, String>(BaseFoo.class) {
		};
	}

	@Test
	public void testAnotherConstructor2() throws Exception {
		DatabaseTableConfig<BaseFoo> tableConfig = DatabaseTableConfig.fromClass(databaseType, BaseFoo.class);
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
				databaseConnection.queryForOne(isA(String.class), isA(Object[].class), isA(SqlType[].class),
						isA(GenericRowMapper.class))).andReturn(foo);
		replay(databaseConnection);
		assertSame(foo, baseFooDao.queryForId("foo"));
		finishDao();
	}

	@SuppressWarnings("unchecked")
	@Test(expected = SQLException.class)
	public void testQueryForIdThrow() throws Exception {
		startDao(false);
		expect(
				databaseConnection.queryForOne(isA(String.class), isA(Object[].class), isA(SqlType[].class),
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
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, "basefoo");
		sb.append(' ');
		CompiledStatement stmt = createMock(CompiledStatement.class);
		expect(databaseConnection.compileStatement(sb.toString())).andReturn(stmt);
		DatabaseResults results = createMock(DatabaseResults.class);
		expect(results.next()).andReturn(false);
		expect(stmt.executeQuery()).andReturn(results);
		stmt.close();
		replay(databaseConnection);
		replay(stmt);
		replay(results);
		List<BaseFoo> list = baseFooDao.queryForAll();
		assertNotNull(list);
		assertEquals(0, list.size());
		finishDao();
		verify(stmt);
		verify(results);
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
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, "basefoo");
		sb.append(' ');
		CompiledStatement stmt = createMock(CompiledStatement.class);
		expect(databaseConnection.compileStatement(sb.toString())).andReturn(stmt);
		DatabaseResults results = createMock(DatabaseResults.class);
		expect(results.next()).andReturn(false);
		expect(stmt.executeQuery()).andReturn(results);
		stmt.close();
		replay(databaseConnection);
		replay(stmt);
		replay(results);
		StatementBuilder<BaseFoo, String> builder = baseFooDao.statementBuilder();
		PreparedStmt<BaseFoo> preparedStmt = builder.prepareStatement();
		assertNull(baseFooDao.queryForFirst(preparedStmt));
		finishDao();
		verify(stmt);
		verify(results);
	}

	@Test(expected = IllegalStateException.class)
	public void testStatementBuilderNoInit() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(BaseFoo.class) {
		};
		dao.statementBuilder();
	}

	@Test(expected = SQLException.class)
	public void testQueryForFirstThrow() throws Exception {
		startDao(false);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, "basefoo");
		sb.append(' ');
		CompiledStatement stmt = createMock(CompiledStatement.class);
		expect(databaseConnection.compileStatement(sb.toString())).andReturn(stmt);
		expect(stmt.executeQuery()).andThrow(new SQLException("expected"));
		stmt.close();
		replay(databaseConnection);
		replay(stmt);
		StatementBuilder<BaseFoo, String> builder = baseFooDao.statementBuilder();
		PreparedStmt<BaseFoo> preparedStmt = builder.prepareStatement();
		baseFooDao.queryForFirst(preparedStmt);
	}

	@Test(expected = IllegalStateException.class)
	public void testQueryForFirstNoInit() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(BaseFoo.class) {
		};
		dao.queryForFirst(null);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testQueryBuilder() throws Exception {
		startDao(false);
		assertNotNull(baseFooDao.queryBuilder());
	}

	@SuppressWarnings("deprecation")
	@Test(expected = IllegalStateException.class)
	public void testQueryBuilderNoInit() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(BaseFoo.class) {
		};
		dao.queryBuilder();
	}

	@Test
	public void testQueryForPrepared() throws Exception {
		startDao(false);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, "basefoo");
		sb.append(' ');
		CompiledStatement stmt = createMock(CompiledStatement.class);
		expect(databaseConnection.compileStatement(sb.toString())).andReturn(stmt);
		DatabaseResults results = createMock(DatabaseResults.class);
		expect(results.next()).andReturn(false);
		expect(stmt.executeQuery()).andReturn(results);
		stmt.close();
		replay(databaseConnection);
		replay(stmt);
		replay(results);
		StatementBuilder<BaseFoo, String> builder = baseFooDao.statementBuilder();
		PreparedStmt<BaseFoo> preparedStmt = builder.prepareStatement();
		List<BaseFoo> list = baseFooDao.query(preparedStmt);
		assertNotNull(list);
		assertEquals(0, list.size());
		finishDao();
		verify(stmt);
		verify(results);
	}

	@Test(expected = IllegalStateException.class)
	public void testQueryForPreparedNoInit() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(BaseFoo.class) {
		};
		dao.query((PreparedStmt<BaseFoo>) null);
	}

	@Test
	public void testQueryForAllRaw() throws Exception {
		startDao(false);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, "basefoo");
		sb.append(' ');
		CompiledStatement stmt = createMock(CompiledStatement.class);
		expect(databaseConnection.compileStatement(sb.toString())).andReturn(stmt);
		DatabaseResults results = createMock(DatabaseResults.class);
		expect(results.next()).andReturn(false);
		expect(stmt.executeQuery()).andReturn(results);
		int numColumns = 1;
		expect(stmt.getColumnCount()).andReturn(numColumns);
		String columnName = "foo";
		expect(stmt.getColumnName(1)).andReturn(columnName);
		stmt.close();
		replay(databaseConnection);
		replay(stmt);
		replay(results);
		RawResults list = baseFooDao.queryForAllRaw(sb.toString());
		assertNotNull(list);
		String[] names = list.getColumnNames();
		assertNotNull(names);
		assertEquals(1, names.length);
		assertEquals(columnName, names[0]);
		assertFalse(list.iterator().hasNext());
		finishDao();
		verify(stmt);
		verify(results);
	}

	@Test(expected = IllegalStateException.class)
	public void testQueryForRawNoInit() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(BaseFoo.class) {
		};
		dao.queryForAllRaw("select * from foo");
	}

	@Test
	public void testObjectToString() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(databaseType, BaseFoo.class) {
		};
		dao.setConnectionSource(createMock(ConnectionSource.class));
		dao.initialize();
		BaseFoo foo = new BaseFoo();
		String idStr = "qdqd";
		foo.id = idStr;
		String objStr = dao.objectToString(foo);
		assertTrue(objStr.contains("id=" + idStr));
	}

	@Test(expected = IllegalStateException.class)
	public void testObjectToStringNoInit() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(databaseType, BaseFoo.class) {
		};
		dao.objectToString(new BaseFoo());
	}

	@Test
	public void testObjectsEqual() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(databaseType, BaseFoo.class) {
		};
		dao.setConnectionSource(createMock(ConnectionSource.class));
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
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(databaseType, BaseFoo.class) {
		};
		dao.objectsEqual(new BaseFoo(), new BaseFoo());
	}

	@Test
	public void testIterator() throws Exception {
		startDao(false);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, "basefoo");
		sb.append(' ');
		CompiledStatement stmt = createMock(CompiledStatement.class);
		DatabaseResults results = createMock(DatabaseResults.class);
		expect(results.next()).andReturn(false);
		expect(stmt.executeQuery()).andReturn(results);
		stmt.close();
		expect(databaseConnection.compileStatement(sb.toString())).andReturn(stmt);
		replay(databaseConnection);
		replay(stmt);
		CloseableIterator<BaseFoo> iterator = baseFooDao.iterator();
		assertFalse(iterator.hasNext());
		finishDao();
		verify(stmt);
	}

	@Test(expected = IllegalStateException.class)
	public void testIteratorThrow() throws Exception {
		startDao(false);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, "basefoo");
		sb.append(' ');
		CompiledStatement stmt = createMock(CompiledStatement.class);
		expect(stmt.executeQuery()).andThrow(new SQLException("expected"));
		stmt.close();
		expect(databaseConnection.compileStatement(sb.toString())).andReturn(stmt);
		replay(databaseConnection);
		replay(stmt);
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
		PreparedStmt<BaseFoo> stmt = createMock(PreparedStmt.class);
		CompiledStatement compiled = createMock(CompiledStatement.class);
		expect(stmt.compile(databaseConnection)).andReturn(compiled);
		expect(stmt.getStatement()).andReturn("select * from foo");
		DatabaseResults results = createMock(DatabaseResults.class);
		expect(results.next()).andReturn(false);
		expect(compiled.executeQuery()).andReturn(results);
		compiled.close();
		replay(databaseConnection);
		replay(stmt);
		replay(compiled);
		replay(results);
		CloseableIterator<BaseFoo> iterator = baseFooDao.iterator(stmt);
		assertFalse(iterator.hasNext());
		finishDao();
		verify(stmt);
		verify(compiled);
		verify(results);
	}

	@Test(expected = SQLException.class)
	public void testIteratorPreparedThrow() throws Exception {
		startDao(false);
		@SuppressWarnings("unchecked")
		PreparedStmt<BaseFoo> stmt = createMock(PreparedStmt.class);
		expect(stmt.compile(databaseConnection)).andThrow(new SQLException("expected"));
		expect(stmt.getStatement()).andReturn("select * from foo");
		replay(databaseConnection);
		replay(stmt);
		baseFooDao.iterator(stmt);
	}

	@Test(expected = IllegalStateException.class)
	public void testIteratorPreparedNoInit() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(BaseFoo.class) {
		};
		dao.iterator((PreparedStmt<BaseFoo>) null);
	}

	@Test
	public void testIteratorRaw() throws Exception {
		startDao(false);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, "basefoo");
		sb.append(' ');
		CompiledStatement stmt = createMock(CompiledStatement.class);
		expect(databaseConnection.compileStatement(sb.toString())).andReturn(stmt);
		DatabaseResults results = createMock(DatabaseResults.class);
		expect(results.next()).andReturn(false);
		expect(stmt.executeQuery()).andReturn(results);
		int numColumns = 1;
		expect(stmt.getColumnCount()).andReturn(numColumns);
		String columnName = "foo";
		expect(stmt.getColumnName(1)).andReturn(columnName);
		stmt.close();
		replay(databaseConnection);
		replay(stmt);
		replay(results);
		RawResults list = baseFooDao.iteratorRaw(sb.toString());
		assertNotNull(list);
		String[] names = list.getColumnNames();
		assertNotNull(names);
		assertEquals(1, names.length);
		assertEquals(columnName, names[0]);
		assertFalse(list.iterator().hasNext());
		finishDao();
		verify(stmt);
		verify(results);
	}

	@Test(expected = SQLException.class)
	public void testIteratorRawThrow() throws Exception {
		startDao(false);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, "basefoo");
		sb.append(' ');
		CompiledStatement stmt = createMock(CompiledStatement.class);
		expect(databaseConnection.compileStatement(sb.toString())).andThrow(new SQLException("expected"));
		int numColumns = 1;
		expect(stmt.getColumnCount()).andReturn(numColumns);
		String columnName = "foo";
		expect(stmt.getColumnName(1)).andReturn(columnName);
		stmt.close();
		replay(databaseConnection);
		replay(stmt);
		baseFooDao.iteratorRaw(sb.toString());
		finishDao();
		verify(stmt);
	}

	@Test(expected = IllegalStateException.class)
	public void testIteratorRawNoInit() throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(BaseFoo.class) {
		};
		dao.iteratorRaw("select * from foo");
	}

	@Test
	public void testTableConfig() throws Exception {
		DatabaseTableConfig<BaseFoo> config = DatabaseTableConfig.fromClass(databaseType, BaseFoo.class);
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(databaseType, config) {
		};
		assertSame(config, dao.getTableConfig());
	}

	@Test
	public void testSetters() throws Exception {
		DatabaseTableConfig<BaseFoo> config = DatabaseTableConfig.fromClass(databaseType, BaseFoo.class);
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(BaseFoo.class) {
		};
		dao.setTableConfig(config);
		dao.setDatabaseType(databaseType);
		assertSame(config, dao.getTableConfig());
	}

	@Test
	public void testCreateDao() throws Exception {
		ConnectionSource connectionSource = createMock(ConnectionSource.class);
		Dao<BaseFoo, String> dao = BaseDaoImpl.createDao(databaseType, connectionSource, BaseFoo.class);
		DatabaseConnection databaseConnection = createMock(DatabaseConnection.class);
		expect(connectionSource.getReadWriteConnection()).andReturn(databaseConnection);
		connectionSource.releaseConnection(databaseConnection);
		expect(databaseConnection.insert(isA(String.class), isA(Object[].class), isA(SqlType[].class))).andReturn(1);
		replay(connectionSource);
		replay(databaseConnection);
		assertEquals(1, dao.create(new BaseFoo()));
		verify(connectionSource);
	}

	private void startDao(boolean readWrite) throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(databaseType, BaseFoo.class) {
		};
		assertEquals(BaseFoo.class, dao.getDataClass());

		connectionSource = createMock(ConnectionSource.class);
		databaseConnection = createMock(DatabaseConnection.class);
		dao.setConnectionSource(connectionSource);
		dao.initialize();
		if (readWrite) {
			expect(connectionSource.getReadWriteConnection()).andReturn(databaseConnection);
		} else {
			expect(connectionSource.getReadOnlyConnection()).andReturn(databaseConnection);
		}
		connectionSource.releaseConnection(databaseConnection);
		baseFooDao = dao;
		replay(connectionSource);
	}

	private void finishDao() {
		verify(connectionSource);
		verify(databaseConnection);
	}
}
