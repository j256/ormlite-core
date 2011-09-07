package com.j256.ormlite.dao;

import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

public class RuntimeExceptionDaoTest {

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
			System.err.println("Unknown RuntimeExceptionDao method: " + runtimeMethod);
			failed = true;
		}

		if (failed) {
			fail("See the console for details");
		}
	}
}
