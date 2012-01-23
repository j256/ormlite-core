package com.j256.ormlite.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class ReferenceObjectCacheTest extends BaseObjectCacheTest {

	@Test
	public void testEnableBoolean() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		dao.setObjectCache(true);

		Foo foo = new Foo();
		int val = 12312321;
		foo.val = val;
		assertEquals(1, dao.create(foo));

		Foo result = dao.queryForId(foo.id);
		assertSame(foo, result);
		dao.setObjectCache(false);
	}

	@Test
	public void testDisableAlreadyDisabled() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		dao.setObjectCache(false);

		Foo foo = new Foo();
		int val = 12312321;
		foo.val = val;
		assertEquals(1, dao.create(foo));

		Foo result = dao.queryForId(foo.id);
		assertNotSame(foo, result);
	}

	@Test
	public void testWeakGcClean() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		ReferenceObjectCache cache = ReferenceObjectCache.makeWeakCache();
		dao.setObjectCache(cache);

		Foo foo = new Foo();
		int val = 12312321;
		foo.val = val;
		assertEquals(1, dao.create(foo));
		assertEquals(1, cache.size(Foo.class));
		Foo result = dao.queryForId(foo.id);
		assertSame(foo, result);

		System.gc();
		cache.cleanNullReferences(Foo.class);
		assertEquals(1, cache.size(Foo.class));
		System.out.println("Foo = " + foo);

		foo = null;
		result = null;
		System.gc();
		assertEquals(1, cache.size(Foo.class));
		cache.cleanNullReferences(Foo.class);
		assertEquals(0, cache.size(Foo.class));
	}

	@Test
	public void testWeakGc() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		ReferenceObjectCache cache = ReferenceObjectCache.makeWeakCache();
		dao.setObjectCache(cache);

		Foo foo = new Foo();
		int val = 12312321;
		foo.val = val;
		assertEquals(1, dao.create(foo));
		assertEquals(1, cache.size(Foo.class));
		Foo result = dao.queryForId(foo.id);
		assertSame(foo, result);
		int id = foo.id;

		System.gc();
		cache.cleanNullReferences(Foo.class);
		assertEquals(1, cache.size(Foo.class));
		System.out.println("Foo = " + foo);

		foo = null;
		result = null;
		System.gc();
		assertEquals(1, cache.size(Foo.class));

		// this will cause a cache miss because of a null reference
		result = dao.queryForId(id);
		assertNotSame(foo, result);
		assertEquals(1, cache.size(Foo.class));
	}

	@Test
	public void testWeakCleanNullAll() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		ReferenceObjectCache cache = ReferenceObjectCache.makeWeakCache();
		dao.setObjectCache(cache);

		Foo foo = new Foo();
		int val = 12312321;
		foo.val = val;
		assertEquals(1, dao.create(foo));
		assertEquals(1, cache.size(Foo.class));
		Foo result = dao.queryForId(foo.id);
		assertSame(foo, result);
		int id = foo.id;

		System.gc();
		cache.cleanNullReferencesAll();
		assertEquals(1, cache.size(Foo.class));
		System.out.println("Foo = " + foo);

		foo = null;
		result = null;
		System.gc();
		assertEquals(1, cache.size(Foo.class));

		// this will cause a cache miss because of a null reference
		result = dao.queryForId(id);
		assertNotSame(foo, result);
		assertEquals(1, cache.size(Foo.class));
	}

	@Test
	public void testSoftGc() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		ReferenceObjectCache cache = ReferenceObjectCache.makeSoftCache();
		dao.setObjectCache(cache);

		Foo foo = new Foo();
		int val = 12312321;
		foo.val = val;
		assertEquals(1, dao.create(foo));
		assertEquals(1, cache.size(Foo.class));

		System.gc();
		cache.cleanNullReferences(Foo.class);
		assertEquals(1, cache.size(Foo.class));
		System.out.println("Foo = " + foo);

		foo = null;
		System.gc();
		cache.cleanNullReferences(Foo.class);
		// still there...
		assertEquals(1, cache.size(Foo.class));
	}

	@Override
	protected ObjectCache enableCache(Dao<?, ?> dao) throws Exception {
		ReferenceObjectCache cache = ReferenceObjectCache.makeWeakCache();
		dao.setObjectCache(cache);
		return cache;
	}
}
