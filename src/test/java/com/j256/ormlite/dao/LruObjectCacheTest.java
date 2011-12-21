package com.j256.ormlite.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LruObjectCacheTest extends BaseObjectCacheTest {

	@Test
	public void testStuff() throws Exception {
		Dao<Foo, Object> dao = createDao(Foo.class, true);
		LruObjectCache cache = new LruObjectCache(2);
		dao.setObjectCache(cache);

		Foo foo1 = new Foo();
		String id = "hello";
		foo1.id = id;
		int val = 12312321;
		foo1.val = val;
		assertEquals(1, dao.create(foo1));
		assertEquals(1, cache.size(Foo.class));

		Foo result = dao.queryForId(id);
		assertSame(foo1, result);

		Foo foo2 = new Foo();
		String id2 = "hello2";
		foo2.id = id2;
		int val2 = 21234761;
		foo2.val = val2;
		assertEquals(1, dao.create(foo2));
		assertEquals(2, cache.size(Foo.class));

		result = dao.queryForId(id2);
		assertSame(foo2, result);

		result = dao.queryForId(id2);
		assertSame(foo2, result);

		Foo foo3 = new Foo();
		String id3 = "hello3";
		foo3.id = id3;
		int val3 = 79834761;
		foo3.val = val3;
		assertEquals(1, dao.create(foo3));
		assertEquals(2, cache.size(Foo.class));

		result = dao.queryForId(id);
		// it is not the same
		assertNotSame(foo1, result);
		foo1 = result;
		// this pushed foo2 out of the cache

		result = dao.queryForId(id2);
		// and now this is not the same
		assertNotSame(foo2, result);
		// so this pushed foo3 out of the cache
		foo2 = result;

		result = dao.queryForId(id);
		assertSame(foo1, result);
		// foo1 is still in there

		result = dao.queryForId(id2);
		assertSame(foo2, result);
		// foo2 is still in there
	}

	@Test
	public void testClear() throws Exception {
		Dao<Foo, Object> dao = createDao(Foo.class, true);
		LruObjectCache cache = new LruObjectCache(2);
		dao.setObjectCache(cache);

		Foo foo = new Foo();
		String id = "hello";
		foo.id = id;
		int val = 12312321;
		foo.val = val;
		assertEquals(1, dao.create(foo));
		assertEquals(1, cache.size(Foo.class));

		Foo result = dao.queryForId(id);
		assertSame(foo, result);

		dao.clearObjectCache();
		result = dao.queryForId(id);
		assertNotSame(foo, result);
	}

	@Test
	public void testQueryForId() throws Exception {
		Dao<Foo, Object> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		String id = "hello";
		foo.id = id;
		int val = 12312321;
		foo.val = val;
		assertEquals(1, dao.create(foo));

		LruObjectCache cache = new LruObjectCache(1024);
		dao.setObjectCache(cache);

		Foo foo2 = dao.queryForId(id);
		assertFalse(foo == foo2);
		Foo foo3 = dao.queryForId(id);
		assertTrue(foo2 == foo3);
	}

	@Override
	protected ObjectCache enableCache(Dao<?, ?> dao) throws Exception {
		LruObjectCache cache = new LruObjectCache(10);
		dao.setObjectCache(cache);
		return cache;
	}
}
