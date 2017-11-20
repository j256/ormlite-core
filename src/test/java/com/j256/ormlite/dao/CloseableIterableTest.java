package com.j256.ormlite.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;

public class CloseableIterableTest extends BaseCoreTest {

	@Test
	public void testWrappedIterableFor() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		assertEquals(1, dao.create(foo1));

		CloseableWrappedIterable<Foo> wrapped = dao.getWrappedIterable();
		try {
			int fooC = 0;
			for (Foo foo : wrapped) {
				assertEquals(foo1.id, foo.id);
				fooC++;
			}
			assertEquals(1, fooC);
		} finally {
			wrapped.close();
		}
	}

	@Test
	public void testWrappedIterableForThrow() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		assertEquals(1, dao.create(foo1));

		CloseableWrappedIterable<Foo> wrapped = dao.getWrappedIterable();
		dropTable(Foo.class, true);
		try {
			wrapped.iterator();
			fail("Should have thrown");
		} catch (IllegalStateException e) {
			// expected
		} finally {
			wrapped.close();
		}
	}

	@Test
	public void testWrappedIterablePreparedQueryFor() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);

		Foo foo1 = new Foo();
		assertEquals(1, dao.create(foo1));

		Foo foo2 = new Foo();
		assertEquals(1, dao.create(foo2));

		CloseableWrappedIterable<Foo> wrapped =
				dao.getWrappedIterable(dao.queryBuilder().where().eq(Foo.ID_COLUMN_NAME, foo1.id).prepare());
		try {
			int fooC = 0;
			for (Foo foo : wrapped) {
				assertEquals(foo1.id, foo.id);
				fooC++;
			}
			assertEquals(1, fooC);
		} finally {
			wrapped.close();
		}
	}
}
