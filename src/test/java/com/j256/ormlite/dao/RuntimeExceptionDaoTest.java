package com.j256.ormlite.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;

public class RuntimeExceptionDaoTest extends BaseCoreTest {

	@Test
	public void testIfAllMethodsAreThere() {
		List<Method> runtimeMethods =
				new ArrayList<Method>(Arrays.asList(RuntimeExceptionDao.class.getDeclaredMethods()));

		boolean failed = false;
		List<Method> daoMethods = new ArrayList<Method>(Arrays.asList(Dao.class.getDeclaredMethods()));
		Iterator<Method> daoIterator = daoMethods.iterator();
		while (daoIterator.hasNext()) {
			Method daoMethod = daoIterator.next();
			boolean found = false;

			if (daoMethod.getName().equals("$VRi")) {
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
				System.err.println("Could not find Dao method: " + daoMethod);
				failed = true;
			}
		}

		// now see if we have any extra methods left over in RuntimeExceptionDao
		for (Method runtimeMethod : runtimeMethods) {
			if (runtimeMethod.getName().equals("$VRi")) {
				continue;
			}
			System.err.println("Unknown RuntimeExceptionDao method: " + runtimeMethod);
			failed = true;
		}

		if (failed) {
			fail("See the console for details");
		}
	}

	@Test
	public void testCoverage() throws Exception {
		Dao<Foo, String> exceptionDao = createDao(Foo.class, true);
		RuntimeExceptionDao<Foo, String> dao = new RuntimeExceptionDao<Foo, String>(exceptionDao);

		Foo foo = new Foo();
		String id = "gjerpjpoegr";
		foo.id = id;
		int val = 1232131321;
		foo.val = val;
		assertEquals(1, dao.create(foo));
		Foo result = dao.queryForId(id);
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

		results = dao.queryForEq(Foo.ID_COLUMN_NAME, id);
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
		assertNull(dao.queryForId(id));
		results = dao.queryForAll();
		assertNotNull(results);
		assertEquals(0, results.size());

		iterator = dao.iterator();
		assertFalse(iterator.hasNext());
	}

	@Test
	public void testCoverage2() throws Exception {
		Dao<Foo, String> exceptionDao = createDao(Foo.class, true);
		RuntimeExceptionDao<Foo, String> dao = new RuntimeExceptionDao<Foo, String>(exceptionDao);

		Foo foo = new Foo();
		String id = "gjerpjpoegr";
		foo.id = id;
		int val = 1232131321;
		foo.val = val;
		assertEquals(1, dao.create(foo));

		Map<String, Object> fieldValueMap = new HashMap<String, Object>();
		fieldValueMap.put(Foo.ID_COLUMN_NAME, id);
		List<Foo> results = dao.queryForFieldValues(fieldValueMap);
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(val, results.get(0).val);

		results = dao.queryForFieldValuesArgs(fieldValueMap);
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(val, results.get(0).val);

		QueryBuilder<Foo, String> qb = dao.queryBuilder();
		results = dao.query(qb.prepare());
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(val, results.get(0).val);

		UpdateBuilder<Foo, String> ub = dao.updateBuilder();
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

		String id2 = "fwojfwefwef";
		assertEquals(1, dao.updateId(foo, id2));
		assertNull(dao.queryForId(id));
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

		assertTrue(dao.objectsEqual(foo, foo));
		assertTrue(dao.objectToString(foo).contains("val=" + val));
		
		assertEquals(id2, dao.extractId(foo));
		assertEquals(Foo.class, dao.getDataClass());
		assertTrue(dao.isTableExists());
		assertTrue(dao.isUpdatable());
		assertEquals(1, dao.countOf());
		
		dao.setObjectCache(false);
		dao.setObjectCache(null);
		dao.clearObjectCache();
	}

	@Test
	public void testDeletes() throws Exception {
		Dao<Foo, String> exceptionDao = createDao(Foo.class, true);
		RuntimeExceptionDao<Foo, String> dao = new RuntimeExceptionDao<Foo, String>(exceptionDao);

		Foo foo = new Foo();
		String id = "gjerpjpoegr";
		foo.id = id;
		int val = 1232131321;
		foo.val = val;
		assertEquals(1, dao.create(foo));

		assertNotNull(dao.queryForId(id));
		assertEquals(1, dao.deleteById(id));
		assertNull(dao.queryForId(id));

		assertEquals(1, dao.create(foo));
		assertNotNull(dao.queryForId(id));
		assertEquals(1, dao.delete(Arrays.asList(foo)));
		assertNull(dao.queryForId(id));

		assertEquals(1, dao.create(foo));
		assertNotNull(dao.queryForId(id));
		assertEquals(1, dao.deleteIds(Arrays.asList(id)));
		assertNull(dao.queryForId(id));

		assertEquals(1, dao.create(foo));
		assertNotNull(dao.queryForId(id));
		DeleteBuilder<Foo, String> db = dao.deleteBuilder();
		dao.delete(db.prepare());
		assertNull(dao.queryForId(id));
	}
}
