package com.j256.ormlite.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.support.DatabaseResults;

public class SelectIteratorTest extends BaseCoreStmtTest {

	@Test
	public void testIterator() throws Exception {
		Dao<Foo, Object> dao = createDao(Foo.class, true);
		CloseableIterator<Foo> iterator = dao.iterator();
		assertFalse(iterator.hasNext());

		Foo foo = new Foo();
		String id1 = "id1";
		foo.id = id1;
		assertEquals(1, dao.create(foo));

		Foo foo2 = new Foo();
		String id2 = "id2";
		foo2.id = id2;
		assertEquals(1, dao.create(foo2));

		iterator = dao.iterator();
		assertTrue(iterator.hasNext());
		Foo foo3 = iterator.next();
		assertEquals(id1, foo3.id);
		assertTrue(iterator.hasNext());

		foo3 = iterator.next();
		assertEquals(id2, foo3.id);

		assertFalse(iterator.hasNext());
		assertNull(iterator.next());
	}

	@Test
	public void testIteratorPrepared() throws Exception {
		Dao<Foo, Object> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		String id1 = "id1";
		foo1.id = id1;
		assertEquals(1, dao.create(foo1));

		Foo foo2 = new Foo();
		String id2 = "id2";
		foo2.id = id2;
		assertEquals(1, dao.create(foo2));

		PreparedQuery<Foo> query = dao.queryBuilder().where().eq(Foo.ID_COLUMN_NAME, id2).prepare();
		CloseableIterator<Foo> iterator = dao.iterator(query);
		assertTrue(iterator.hasNext());
		Foo foo3 = iterator.next();
		assertEquals(id2, foo3.id);
		assertFalse(iterator.hasNext());
		assertNull(iterator.next());
	}

	@Test(expected = IllegalStateException.class)
	public void testIteratorRemoveNoNext() throws Exception {
		Dao<Foo, Object> dao = createDao(Foo.class, true);
		CloseableIterator<Foo> iterator = dao.iterator();
		iterator.remove();
	}

	@Test
	public void testIteratorRemove() throws Exception {
		Dao<Foo, Object> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		String id1 = "id1";
		foo1.id = id1;
		assertEquals(1, dao.create(foo1));

		assertEquals(1, dao.queryForAll().size());

		CloseableIterator<Foo> iterator = dao.iterator();
		assertTrue(iterator.hasNext());
		Foo foo3 = iterator.next();
		assertEquals(id1, foo3.id);
		iterator.remove();

		assertFalse(iterator.hasNext());
		assertNull(iterator.next());

		assertEquals(0, dao.queryForAll().size());
	}

	@Test
	public void testIteratorHasNextClosed() throws Exception {
		Dao<Foo, Object> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		String id1 = "id1";
		foo1.id = id1;
		assertEquals(1, dao.create(foo1));

		assertEquals(1, dao.queryForAll().size());

		CloseableIterator<Foo> iterator = dao.iterator();
		assertTrue(iterator.hasNext());
		iterator.next();

		assertFalse(iterator.hasNext());
		assertNull(iterator.next());

		iterator.close();
		assertFalse(iterator.hasNext());
	}

	@Test
	public void testIteratorGetRawResults() throws Exception {
		Dao<Foo, Object> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		String id1 = "id1";
		foo1.id = id1;
		assertEquals(1, dao.create(foo1));

		assertEquals(1, dao.queryForAll().size());

		@SuppressWarnings("unchecked")
		SelectIterator<Foo, String> iterator = (SelectIterator<Foo, String>) dao.iterator();
		DatabaseResults results = iterator.getRawResults();
		assertTrue(results.next());
	}

	@Test(expected = IllegalStateException.class)
	public void testIteratorRawResults() throws Exception {
		Dao<Foo, Object> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		String id1 = "id1";
		foo1.id = id1;
		assertEquals(1, dao.create(foo1));

		GenericRawResults<String[]> rawResults = dao.queryRaw("SELECT " + Foo.ID_COLUMN_NAME + " FROM FOO");
		CloseableIterator<String[]> iterator = rawResults.iterator();

		assertTrue(iterator.hasNext());
		iterator.next();
		iterator.remove();
	}
}
