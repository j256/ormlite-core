package com.j256.ormlite.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

public class LazyForeignCollectionTest extends BaseCoreTest {

	@Test
	public void test() throws Exception {
		Dao<Lazy, Integer> lazyDao = createDao(Lazy.class, true);
		Lazy lazy = new Lazy();
		assertEquals(1, lazyDao.create(lazy));

		Lazy result = lazyDao.queryForId(lazy.id);
		assertNotNull(result.foreign);
		assertFalse(result.foreign.isEager());
		assertEquals(System.identityHashCode(result.foreign), result.foreign.hashCode());
		assertEquals(result.foreign, result.foreign);

		try {
			result.foreign.updateAll();
			fail("Should have thrown");
		} catch (UnsupportedOperationException e) {
			// expected
		}
		try {
			result.foreign.refreshAll();
			fail("Should have thrown");
		} catch (UnsupportedOperationException e) {
			// expected
		}
		assertEquals(0, result.foreign.refreshCollection());
	}

	protected static class Lazy {
		@DatabaseField(generatedId = true)
		int id;
		@ForeignCollectionField
		ForeignCollection<Foreign> foreign;
		public Lazy() {
		}
	}

	protected static class Foreign {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(foreign = true)
		Lazy lazy;
		public Foreign() {
		}
	}
}
