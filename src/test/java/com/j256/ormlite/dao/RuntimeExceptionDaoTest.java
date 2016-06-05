package com.j256.ormlite.dao;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.GenericRowMapper;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.PreparedUpdate;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.ObjectFactory;

public class RuntimeExceptionDaoTest extends BaseCoreTest {

	@Test
	public void testIfAllMethodsAreThere() {
		List<String> failedMessages = new ArrayList<String>();

		List<Method> runtimeMethods =
				new ArrayList<Method>(Arrays.asList(RuntimeExceptionDao.class.getDeclaredMethods()));

		List<Method> daoMethods = new ArrayList<Method>(Arrays.asList(Dao.class.getDeclaredMethods()));
		daoMethods.addAll(Arrays.asList(CloseableIterable.class.getDeclaredMethods()));
		daoMethods.addAll(Arrays.asList(Iterable.class.getDeclaredMethods()));
		Iterator<Method> daoIterator = daoMethods.iterator();
		while (daoIterator.hasNext()) {
			Method daoMethod = daoIterator.next();
			boolean found = false;

			// coverage magic
			if (daoMethod.getName().equals("$VRi") || daoMethod.getName().equals("spliterator") /* java 8 method */
					|| daoMethod.getName().equals("forEach") /* java 8 method */) {
				continue;
			}

			Iterator<Method> runtimeIterator = runtimeMethods.iterator();
			while (runtimeIterator.hasNext()) {
				Method runtimeMethod = runtimeIterator.next();
				if (daoMethod.getName().equals(runtimeMethod.getName())
						&& Arrays.equals(daoMethod.getParameterTypes(), runtimeMethod.getParameterTypes())
						&& daoMethod.getReturnType().equals(runtimeMethod.getReturnType())) {
					found = true;
					daoIterator.remove();
					runtimeIterator.remove();
					break;
				}
			}

			// make sure we found the method in RuntimeExceptionDao
			if (!found) {
				failedMessages.add(RuntimeExceptionDao.class.getName() + " did not include method: " + daoMethod);
			}
		}

		// now see if we have any extra methods left over in RuntimeExceptionDao
		for (Method runtimeMethod : runtimeMethods) {
			// coverage magic
			if (runtimeMethod.getName().startsWith("$")) {
				continue;
			}
			// skip these
			if (runtimeMethod.getName().equals("createDao") || runtimeMethod.getName().equals("logMessage")) {
				continue;
			}
			failedMessages.add("Unknown RuntimeExceptionDao method: " + runtimeMethod);
		}

		if (!failedMessages.isEmpty()) {
			for (String message : failedMessages) {
				System.err.println(message);
			}
			fail(failedMessages.get(0) + ", see the console for more");
		}
	}

	@Test
	public void testCoverage() throws Exception {
		Dao<Foo, Integer> exceptionDao = createDao(Foo.class, true);
		RuntimeExceptionDao<Foo, Integer> dao = new RuntimeExceptionDao<Foo, Integer>(exceptionDao);

		Foo foo = new Foo();
		int val = 1232131321;
		foo.val = val;
		assertEquals(1, dao.create(foo));
		Foo result = dao.queryForId(foo.id);
		assertNotNull(result);
		assertEquals(val, result.val);
		List<Foo> results = dao.queryForAll();
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(val, results.get(0).val);

		CloseableIterator<Foo> iterator = dao.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(val, iterator.next().val);
		assertFalse(iterator.hasNext());

		results = dao.queryForEq(Foo.ID_COLUMN_NAME, foo.id);
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(val, results.get(0).val);

		results = dao.queryForMatching(foo);
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(val, results.get(0).val);

		results = dao.queryForMatchingArgs(foo);
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(val, results.get(0).val);

		result = dao.queryForSameId(foo);
		assertNotNull(results);
		assertEquals(val, result.val);

		result = dao.createIfNotExists(foo);
		assertNotSame(results, foo);
		assertNotNull(results);
		assertEquals(val, result.val);

		int val2 = 342342343;
		foo.val = val2;
		assertEquals(1, dao.update(foo));
		assertEquals(1, dao.refresh(foo));
		assertEquals(1, dao.delete(foo));
		assertNull(dao.queryForId(foo.id));
		results = dao.queryForAll();
		assertNotNull(results);
		assertEquals(0, results.size());

		iterator = dao.iterator();
		assertFalse(iterator.hasNext());
	}

	@Test
	public void testCoverage2() throws Exception {
		Dao<Foo, Integer> exceptionDao = createDao(Foo.class, true);
		RuntimeExceptionDao<Foo, Integer> dao = new RuntimeExceptionDao<Foo, Integer>(exceptionDao);

		Foo foo = new Foo();
		int val = 1232131321;
		foo.val = val;
		assertEquals(1, dao.create(foo));
		int id1 = foo.id;

		Map<String, Object> fieldValueMap = new HashMap<String, Object>();
		fieldValueMap.put(Foo.ID_COLUMN_NAME, foo.id);
		List<Foo> results = dao.queryForFieldValues(fieldValueMap);
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(val, results.get(0).val);

		results = dao.queryForFieldValuesArgs(fieldValueMap);
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(val, results.get(0).val);

		QueryBuilder<Foo, Integer> qb = dao.queryBuilder();
		results = dao.query(qb.prepare());
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(val, results.get(0).val);

		UpdateBuilder<Foo, Integer> ub = dao.updateBuilder();
		int val2 = 65809;
		ub.updateColumnValue(Foo.VAL_COLUMN_NAME, val2);
		assertEquals(1, dao.update(ub.prepare()));
		results = dao.queryForAll();
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(val2, results.get(0).val);

		CreateOrUpdateStatus status = dao.createOrUpdate(foo);
		assertNotNull(status);
		assertTrue(status.isUpdated());

		int id2 = foo.id + 1;
		assertEquals(1, dao.updateId(foo, id2));
		assertNull(dao.queryForId(id1));
		assertNotNull(dao.queryForId(id2));

		dao.iterator();
		dao.closeLastIterator();

		CloseableWrappedIterable<Foo> wrapped = dao.getWrappedIterable();
		try {
			for (Foo fooLoop : wrapped) {
				assertEquals(id2, fooLoop.id);
			}
		} finally {
			wrapped.close();
		}

		wrapped = dao.getWrappedIterable(dao.queryBuilder().prepare());
		try {
			for (Foo fooLoop : wrapped) {
				assertEquals(id2, fooLoop.id);
			}
		} finally {
			wrapped.close();
		}

		CloseableIterator<Foo> iterator = dao.iterator(dao.queryBuilder().prepare());
		assertTrue(iterator.hasNext());
		iterator.next();
		assertFalse(iterator.hasNext());
		dao.iterator(DatabaseConnection.DEFAULT_RESULT_FLAGS).close();
		dao.iterator(dao.queryBuilder().prepare(), DatabaseConnection.DEFAULT_RESULT_FLAGS).close();

		assertTrue(dao.objectsEqual(foo, foo));
		assertTrue(dao.objectToString(foo).contains("val=" + val));

		assertEquals((Integer) id2, dao.extractId(foo));
		assertEquals(Foo.class, dao.getDataClass());
		assertTrue(dao.isTableExists());
		assertTrue(dao.isUpdatable());
		assertEquals(1, dao.countOf());

		dao.setObjectCache(false);
		dao.setObjectCache(null);
		assertNull(dao.getObjectCache());
		dao.clearObjectCache();
	}

	@Test
	public void testDeletes() throws Exception {
		Dao<Foo, Integer> exceptionDao = createDao(Foo.class, true);
		RuntimeExceptionDao<Foo, Integer> dao = new RuntimeExceptionDao<Foo, Integer>(exceptionDao);

		Foo foo = new Foo();
		int val = 1232131321;
		foo.val = val;
		assertEquals(1, dao.create(foo));

		assertNotNull(dao.queryForId(foo.id));
		assertEquals(1, dao.deleteById(foo.id));
		assertNull(dao.queryForId(foo.id));

		assertEquals(1, dao.create(foo));
		assertNotNull(dao.queryForId(foo.id));
		assertEquals(1, dao.delete(Arrays.asList(foo)));
		assertNull(dao.queryForId(foo.id));

		assertEquals(1, dao.create(foo));
		assertNotNull(dao.queryForId(foo.id));
		assertEquals(1, dao.deleteIds(Arrays.asList(foo.id)));
		assertNull(dao.queryForId(foo.id));

		assertEquals(1, dao.create(foo));
		assertNotNull(dao.queryForId(foo.id));
		DeleteBuilder<Foo, Integer> db = dao.deleteBuilder();
		dao.delete(db.prepare());
		assertNull(dao.queryForId(foo.id));
	}

	@Test
	public void testCoverage3() throws Exception {
		Dao<Foo, Integer> exceptionDao = createDao(Foo.class, true);
		RuntimeExceptionDao<Foo, Integer> dao = new RuntimeExceptionDao<Foo, Integer>(exceptionDao);

		Foo foo = new Foo();
		int val = 1232131321;
		foo.val = val;
		assertEquals(1, dao.create(foo));

		GenericRawResults<String[]> rawResults = dao.queryRaw("select * from foo");
		assertEquals(1, rawResults.getResults().size());
		GenericRawResults<Foo> mappedResults = dao.queryRaw("select * from foo", new RawRowMapper<Foo>() {
			@Override
			public Foo mapRow(String[] columnNames, String[] resultColumns) {
				Foo fooResult = new Foo();
				for (int i = 0; i < resultColumns.length; i++) {
					if (columnNames[i].equals(Foo.ID_COLUMN_NAME)) {
						fooResult.id = Integer.parseInt(resultColumns[i]);
					}
				}
				return fooResult;
			}
		});
		assertEquals(1, mappedResults.getResults().size());
		GenericRawResults<Object[]> dataResults =
				dao.queryRaw("select id,val from foo", new DataType[] { DataType.STRING, DataType.INTEGER });
		assertEquals(1, dataResults.getResults().size());
		assertEquals(0, dao.executeRaw("delete from foo where id = ?", Integer.toString(foo.id + 1)));
		assertEquals(0, dao.updateRaw("update foo set val = 100 where id = ?", Integer.toString(foo.id + 1)));
		final String someVal = "fpowejfpjfwe";
		assertEquals(someVal, dao.callBatchTasks(new Callable<String>() {
			@Override
			public String call() {
				return someVal;
			}
		}));
		assertNull(dao.findForeignFieldType(Void.class));
		assertEquals(1, dao.countOf());
		assertEquals(1, dao.countOf(dao.queryBuilder().setCountOf(true).prepare()));
		PreparedQuery<Foo> prepared = dao.queryBuilder().prepare();
		DatabaseConnection conn = connectionSource.getReadOnlyConnection(FOO_TABLE_NAME);
		CompiledStatement compiled = null;
		try {
			compiled = prepared.compile(conn, StatementType.SELECT);
			DatabaseResults results = compiled.runQuery(null);
			assertTrue(results.next());
			Foo result = dao.mapSelectStarRow(results);
			assertEquals(foo.id, result.id);
			GenericRowMapper<Foo> mapper = dao.getSelectStarRowMapper();
			result = mapper.mapRow(results);
			assertEquals(foo.id, result.id);
		} finally {
			if (compiled != null) {
				compiled.close();
			}
			connectionSource.releaseConnection(conn);
		}
		assertTrue(dao.idExists(foo.id));
		Foo result = dao.queryForFirst(prepared);
		assertEquals(foo.id, result.id);
		assertNull(dao.getEmptyForeignCollection(Foo.ID_COLUMN_NAME));
		conn = dao.startThreadConnection();
		dao.setAutoCommit(conn, true);
		assertTrue(dao.isAutoCommit(conn));
		dao.commit(conn);
		dao.rollBack(conn);
		dao.endThreadConnection(conn);
		ObjectFactory<Foo> objectFactory = new ObjectFactory<Foo>() {
			@Override
			public Foo createObject(Constructor<Foo> construcor, Class<Foo> dataClass) {
				return new Foo();
			}
		};
		dao.setObjectFactory(objectFactory);
		dao.setObjectFactory(null);
		assertNotNull(dao.getRawRowMapper());
	}

	@Test
	public void testCreateDao() throws Exception {
		createDao(Foo.class, true);
		RuntimeExceptionDao<Foo, String> dao = RuntimeExceptionDao.createDao(connectionSource, Foo.class);
		assertEquals(0, dao.countOf());
	}

	@Test
	public void testCreateDaoTableConfig() throws Exception {
		createDao(Foo.class, true);
		RuntimeExceptionDao<Foo, String> dao =
				RuntimeExceptionDao.createDao(connectionSource,
						DatabaseTableConfig.fromClass(connectionSource, Foo.class));
		assertEquals(0, dao.countOf());
	}

	@Test(expected = RuntimeException.class)
	public void testQueryForIdThrow() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		expect(dao.queryForId(isA(String.class))).andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.queryForId("wow");
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testQueryForFirstPreparedThrow() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		expect(dao.queryForFirst(null)).andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.queryForFirst(null);
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testQueryForAllThrow() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		expect(dao.queryForAll()).andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.queryForAll();
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testQueryForEqThrow() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		expect(dao.queryForEq(null, null)).andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.queryForEq(null, null);
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testQueryForMatchingThrow() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		expect(dao.queryForMatching(null)).andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.queryForMatching(null);
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testQueryForMatchingArgsThrow() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		expect(dao.queryForMatchingArgs(null)).andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.queryForMatchingArgs(null);
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testQueryForFieldsValuesThrow() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		expect(dao.queryForFieldValues(null)).andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.queryForFieldValues(null);
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testQueryForFieldsValuesArgsThrow() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		expect(dao.queryForFieldValuesArgs(null)).andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.queryForFieldValuesArgs(null);
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testQueryForSameIdThrow() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		expect(dao.queryForSameId(null)).andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.queryForSameId(null);
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testQueryThrow() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		expect(dao.query(null)).andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.query(null);
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testCreateThrow() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		expect(dao.create((Foo) null)).andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.create((Foo) null);
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testCreateIfNotExistsThrow() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		expect(dao.createIfNotExists(null)).andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.createIfNotExists(null);
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testCreateOrUpdateThrow() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		expect(dao.createOrUpdate(null)).andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.createOrUpdate(null);
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testUpdateThrow() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		expect(dao.update((Foo) null)).andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.update((Foo) null);
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testUpdateIdThrow() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		expect(dao.updateId(null, null)).andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.updateId(null, null);
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testUpdatePreparedThrow() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		expect(dao.update((PreparedUpdate<Foo>) null)).andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.update((PreparedUpdate<Foo>) null);
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testRefreshThrow() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		expect(dao.refresh(null)).andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.refresh(null);
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testDeleteThrow() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		expect(dao.delete((Foo) null)).andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.delete((Foo) null);
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testDeleteByIdThrow() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		expect(dao.deleteById(null)).andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.deleteById(null);
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testDeleteCollectionThrow() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		expect(dao.delete((Collection<Foo>) null)).andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.delete((Collection<Foo>) null);
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testDeleteIdsThrow() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		expect(dao.deleteIds(null)).andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.deleteIds(null);
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testDeletePreparedThrow() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		expect(dao.delete((PreparedDelete<Foo>) null)).andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.delete((PreparedDelete<Foo>) null);
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testCloseLastIteratorThrow() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		dao.closeLastIterator();
		expectLastCall().andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.closeLastIterator();
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testIteratorThrow() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		expect(dao.iterator(null)).andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.iterator(null);
		verify(dao);
	}

	@Test
	public void testCloseableIterator() {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		expect(dao.closeableIterator()).andReturn(null);
		replay(dao);
		rtDao.closeableIterator();
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testCloseableIteratorThrow() {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		expect(dao.closeableIterator()).andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.closeableIterator();
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testIteratorQueryFlags() {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		expect(rtDao.iterator(null, 0)).andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.iterator(null, 0);
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testQueryRawThrow() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		expect(dao.queryRaw(null)).andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.queryRaw(null);
		verify(dao);
	}

	@Test
	public void testQueryRawValue() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		String query = "fkeowjfkewfewf";
		expect(dao.queryRawValue(query, new String[0])).andReturn(0L);
		replay(dao);
		rtDao.queryRawValue(query);
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testQueryRawValueThrow() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		expect(dao.queryRawValue(null)).andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.queryRawValue(null);
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testQueryRawRowMapperThrow() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		expect(dao.queryRaw(null, (RawRowMapper<String>) null)).andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.queryRaw(null, (RawRowMapper<String>) null);
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testQueryRawDateTypesThrow() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		expect(dao.queryRaw(null, (DataType[]) null)).andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.queryRaw(null, (DataType[]) null);
		verify(dao);
	}

	@Test
	public void testExecuteRaw() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		expect(dao.executeRaw(null)).andReturn(0);
		replay(dao);
		rtDao.executeRaw(null);
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testExecuteRawThrow() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		expect(dao.executeRaw(null)).andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.executeRaw(null);
		verify(dao);
	}

	@Test
	public void testAssignEmptyForeignCollection() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		dao.assignEmptyForeignCollection(null, null);
		replay(dao);
		rtDao.assignEmptyForeignCollection(null, null);
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testAssignEmptyForeignCollectionThrow() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		dao.assignEmptyForeignCollection(null, null);
		expectLastCall().andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.assignEmptyForeignCollection(null, null);
		verify(dao);
	}

	@Test
	public void testExecuteRawNoArgs() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		expect(dao.executeRawNoArgs(null)).andReturn(0);
		replay(dao);
		rtDao.executeRawNoArgs(null);
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testExecuteRawNoArgsThrow() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		expect(dao.executeRawNoArgs(null)).andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.executeRawNoArgs(null);
		verify(dao);
	}

	@Test
	public void testSetObjectCache() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		dao.setObjectCache(false);
		replay(dao);
		rtDao.setObjectCache(false);
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testSetObjectCacheThrow() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		dao.setObjectCache(false);
		expectLastCall().andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.setObjectCache(false);
		verify(dao);
	}

	@Test
	public void testSetObjectCacheCache() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		dao.setObjectCache(null);
		replay(dao);
		rtDao.setObjectCache(null);
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testSetObjectCacheCacheThrow() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		dao.setObjectCache(null);
		expectLastCall().andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.setObjectCache(null);
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testUpdateRawThrow() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		expect(dao.updateRaw(null)).andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.updateRaw(null);
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testCallBatchTasksThrow() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		expect(dao.callBatchTasks(null)).andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.callBatchTasks(null);
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testObjectsEqualThrow() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		expect(dao.objectsEqual(null, null)).andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.objectsEqual(null, null);
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testExtractIdThrow() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		expect(dao.extractId(null)).andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.extractId(null);
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testIsTableExistsThrow() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		expect(dao.isTableExists()).andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.isTableExists();
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testCountOfThrow() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		expect(dao.countOf()).andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.countOf();
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testCountOfPreparedThrow() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		@SuppressWarnings("unchecked")
		PreparedQuery<Foo> prepared = (PreparedQuery<Foo>) createMock(PreparedQuery.class);
		expect(dao.countOf(prepared)).andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.countOf(prepared);
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testGetEmptyForeignCollectionThrow() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		expect(dao.getEmptyForeignCollection(null)).andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.getEmptyForeignCollection(null);
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testMapSelectStarRowThrow() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		DatabaseResults results = createMock(DatabaseResults.class);
		expect(dao.mapSelectStarRow(results)).andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.mapSelectStarRow(results);
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testGetSelectStarRowMapperThrow() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		expect(dao.getSelectStarRowMapper()).andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.getSelectStarRowMapper();
		verify(dao);
	}

	@Test(expected = RuntimeException.class)
	public void testIdExists() throws Exception {
		@SuppressWarnings("unchecked")
		Dao<Foo, String> dao = (Dao<Foo, String>) createMock(Dao.class);
		RuntimeExceptionDao<Foo, String> rtDao = new RuntimeExceptionDao<Foo, String>(dao);
		String id = "eopwjfpwejf";
		expect(dao.idExists(id)).andThrow(new SQLException("Testing catch"));
		replay(dao);
		rtDao.idExists(id);
		verify(dao);
	}
}
