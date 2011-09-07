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
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;

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
}
