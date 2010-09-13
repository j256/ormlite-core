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
	public void testCreate() throws Exception {
		startDao(true);
		int linesAffected = 1;
		expect(databaseConnection.insert(isA(String.class), isA(Object[].class), isA(SqlType[].class))).andReturn(
				linesAffected);
		replay(databaseConnection);
		assertEquals(linesAffected, baseFooDao.create(new BaseFoo()));
		finishDao();
	}

	@Test
	public void testCreateNull() throws Exception {
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

	@Test
	public void testUpdateNull() throws Exception {
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
		assertEquals(0, dao.updateId(null, null));
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

	@Test
	public void testDeleteNull() throws Exception {
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
		List<BaseFoo> fooList = new ArrayList<BaseFoo>();
		assertEquals(0, dao.delete(fooList));
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

	@Test
	public void testRefreshNull() throws Exception {
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

	@Test
	public void testAnotherConstructor3() throws Exception {
		DatabaseTableConfig<BaseFoo> tableConfig = DatabaseTableConfig.fromClass(databaseType, BaseFoo.class);
		new BaseDaoImpl<BaseFoo, String>(databaseType, tableConfig) {
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

	@Test
	public void testQueryForRaw() throws Exception {
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

	private void startDao(boolean readWrite) throws Exception {
		BaseDaoImpl<BaseFoo, String> dao = new BaseDaoImpl<BaseFoo, String>(databaseType, BaseFoo.class) {
		};

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
