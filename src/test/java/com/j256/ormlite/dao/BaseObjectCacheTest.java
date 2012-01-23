package com.j256.ormlite.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.sql.SQLException;
import java.util.List;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.field.DatabaseField;

public abstract class BaseObjectCacheTest extends BaseCoreTest {

	protected abstract ObjectCache enableCache(Dao<?, ?> dao) throws Exception;

	@Test
	public void testBasic() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		enableCache(dao);

		Foo foo = new Foo();
		int val = 12312321;
		foo.val = val;

		assertEquals(1, dao.create(foo));

		Foo result = dao.queryForId(foo.id);
		assertSame(foo, result);

		List<Foo> results = dao.queryForAll();
		assertEquals(1, results.size());
		assertSame(result, results.get(0));

		// disable cache
		dao.setObjectCache(null);

		result = dao.queryForId(foo.id);
		assertNotSame(foo, result);
	}

	@Test
	public void testUpdate() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		enableCache(dao);

		Foo foo = new Foo();
		int val = 12312321;
		foo.val = val;
		assertEquals(1, dao.create(foo));

		Foo result = dao.queryForId(foo.id);
		assertSame(foo, result);

		// update behind the back
		Foo foo2 = new Foo();
		foo2.id = foo.id;
		int val2 = 1312341412;
		foo2.val = val2;
		assertEquals(1, dao.update(foo2));

		// the result should have the same value
		assertNotSame(foo, foo2);
		assertEquals(val2, foo.val);
	}

	@Test
	public void testUpdateId() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		enableCache(dao);

		Foo foo = new Foo();
		int val = 12312321;
		foo.val = val;
		assertEquals(1, dao.create(foo));

		Foo result = dao.queryForId(foo.id);
		assertSame(foo, result);

		// updateId behind the back
		Foo foo2 = new Foo();
		foo2.id = foo.id;
		int val2 = 1312341412;
		foo2.val = val2;
		int id2 = foo.id + 1;
		assertEquals(1, dao.updateId(foo2, id2));

		// the result should _not_ have the same value
		assertNotSame(foo, foo2);
		// but the id should be the same
		assertEquals(id2, foo.id);
	}

	@Test
	public void testUpdateIdNotInCache() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);

		Foo foo1 = new Foo();
		int val = 12312321;
		foo1.val = val;
		assertEquals(1, dao.create(foo1));
		int id1 = foo1.id;

		Foo result = dao.queryForId(foo1.id);
		assertNotSame(foo1, result);

		// we enable the cache _after_ Foo was created
		ObjectCache cache = enableCache(dao);

		// updateId behind the back
		Foo foo2 = new Foo();
		foo2.id = foo1.id;
		int val2 = 1312341412;
		foo2.val = val2;
		int id2 = foo1.id + 1;
		assertEquals(1, dao.updateId(foo2, id2));

		// the result should _not_ have the same value
		assertNotSame(foo1, foo2);
		// and the id should be the old one and not the new one
		assertEquals(id1, foo1.id);

		assertEquals(0, cache.size(Foo.class));
	}

	@Test
	public void testDelete() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		ObjectCache cache = enableCache(dao);

		Foo foo = new Foo();
		int val = 12312321;
		foo.val = val;
		assertEquals(1, dao.create(foo));

		Foo result = dao.queryForId(foo.id);
		assertSame(foo, result);

		// updateId behind the back
		Foo foo2 = new Foo();
		foo2.id = foo.id;

		assertEquals(1, cache.size(Foo.class));
		assertEquals(1, dao.delete(foo2));
		// foo still exists

		assertEquals(0, cache.size(Foo.class));
	}

	@Test
	public void testClearEachClass() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		Dao<WithId, Integer> withIdDao = createDao(WithId.class, true);
		ObjectCache cache = enableCache(fooDao);
		withIdDao.setObjectCache(cache);

		Foo foo = new Foo();
		assertEquals(1, fooDao.create(foo));

		Foo fooResult = fooDao.queryForId(foo.id);
		assertSame(foo, fooResult);

		WithId withId = new WithId();
		assertEquals(1, withIdDao.create(withId));

		WithId withIdResult = withIdDao.queryForId(withId.id);
		assertSame(withId, withIdResult);

		assertEquals(1, cache.size(Foo.class));
		assertEquals(1, cache.size(WithId.class));
		assertEquals(2, cache.sizeAll());

		cache.clear(Foo.class);

		fooResult = fooDao.queryForId(foo.id);
		assertNotSame(foo, fooResult);

		withIdResult = withIdDao.queryForId(withId.id);
		// still the same
		assertSame(withId, withIdResult);

		cache.clearAll();

		withIdResult = withIdDao.queryForId(withId.id);
		// not the same now
		assertNotSame(withId, withIdResult);
	}

	@Test(expected = SQLException.class)
	public void testNoIdClass() throws Exception {
		Dao<NoId, Void> dao = createDao(NoId.class, true);
		enableCache(dao);
	}

	protected static class NoId {
		@DatabaseField
		int notId;

		@DatabaseField
		String stuff;

		public NoId() {
		}
	}

	protected static class WithId {
		@DatabaseField(generatedId = true)
		int id;

		@DatabaseField
		String stuff;

		public WithId() {
		}
	}
}
