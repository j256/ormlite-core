package com.j256.ormlite.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class ReferenceObjectCacheTest extends BaseObjectCacheTest {

	@Test
	public void testEnableBoolean() throws Exception {
		Dao<Foo, Object> dao = createDao(Foo.class, true);
		dao.enableObjectCache(true);

		Foo foo = new Foo();
		String id = "hello";
		foo.id = id;
		int val = 12312321;
		foo.val = val;
		assertEquals(1, dao.create(foo));

		Foo result = dao.queryForId(id);
		assertSame(foo, result);
		dao.enableObjectCache(false);
	}

	@Test
	public void testDisableAlreadyDisabled() throws Exception {
		Dao<Foo, Object> dao = createDao(Foo.class, true);
		dao.enableObjectCache(false);

		Foo foo = new Foo();
		String id = "hello";
		foo.id = id;
		int val = 12312321;
		foo.val = val;
		assertEquals(1, dao.create(foo));

		Foo result = dao.queryForId(id);
		assertNotSame(foo, result);
	}

	@Test
	public void testWeakGc() throws Exception {
		Dao<Foo, Object> dao = createDao(Foo.class, true);
		ReferenceObjectCache cache = ReferenceObjectCache.makeWeakCache();
		dao.enableObjectCache(cache);

		Foo foo = new Foo();
		String id = "hello";
		foo.id = id;
		int val = 12312321;
		foo.val = val;
		assertEquals(1, dao.create(foo));
		assertEquals(1, cache.size());

		System.gc();
		cache.cleanNullReferences();
		assertEquals(1, cache.size());
		System.out.println("Foo = " + foo);

		foo = null;
		System.gc();
		cache.cleanNullReferences();
		assertEquals(0, cache.size());
	}

	@Test
	public void testSoftGc() throws Exception {
		Dao<Foo, Object> dao = createDao(Foo.class, true);
		ReferenceObjectCache cache = ReferenceObjectCache.makeSoftCache();
		dao.enableObjectCache(cache);

		Foo foo = new Foo();
		String id = "hello";
		foo.id = id;
		int val = 12312321;
		foo.val = val;
		assertEquals(1, dao.create(foo));
		assertEquals(1, cache.size());

		System.gc();
		cache.cleanNullReferences();
		assertEquals(1, cache.size());
		System.out.println("Foo = " + foo);

		foo = null;
		System.gc();
		cache.cleanNullReferences();
		// still there...
		assertEquals(1, cache.size());
	}

	@Override
	protected ObjectCache enableCache(Dao<?, ?> dao) {
		ReferenceObjectCache cache = ReferenceObjectCache.makeWeakCache();
		dao.enableObjectCache(cache);
		return cache;
	}
}
