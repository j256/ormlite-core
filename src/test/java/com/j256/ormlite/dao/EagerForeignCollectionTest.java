package com.j256.ormlite.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Collections;
import java.util.ListIterator;

import org.junit.jupiter.api.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;

public class EagerForeignCollectionTest extends BaseCoreTest {

	@Test
	public void testBasic() throws Exception {
		Dao<Eager, Integer> eagerDao = createDao(Eager.class, true);
		Dao<Foreign, Integer> foreignDao = createDao(Foreign.class, true);
		Eager eager = new Eager();
		assertEquals(1, eagerDao.create(eager));

		Eager result = eagerDao.queryForId(eager.id);
		assertNotNull(result.foreignCollection);
		assertTrue(result.foreignCollection.isEager());
		assertFalse(System.identityHashCode(result.foreignCollection) == result.foreignCollection.hashCode());
		assertEquals(result.foreignCollection, result.foreignCollection);

		Foreign f0 = new Foreign();
		f0.eager = eager;
		assertEquals(1, foreignDao.create(f0));
		// and another one
		Foreign f1 = new Foreign();
		f1.eager = eager;
		assertEquals(1, foreignDao.create(f1));

		Foreign[] foreigns = result.foreignCollection.toArray(new Foreign[0]);
		assertNotNull(foreigns);
		assertEquals(0, foreigns.length);

		assertEquals(2, result.foreignCollection.refreshCollection());
		foreigns = result.foreignCollection.toArray(new Foreign[0]);
		assertNotNull(foreigns);
		assertEquals(2, foreigns.length);
		assertEquals(f0.id, foreigns[0].id);
		assertEquals(f1.id, foreigns[1].id);

		String stuff0 = "some stuff";
		f0.stuff = stuff0;
		assertEquals(1, foreignDao.update(f0));

		assertNull(foreigns[0].stuff);
		assertEquals(2, result.foreignCollection.refreshAll());
		assertNotNull(foreigns[0].stuff);
		assertNotNull(stuff0, foreigns[0].stuff);

		String stuff1 = "other stuff";
		foreigns[1].stuff = stuff1;
		foreignDao.refresh(f1);
		assertNull(f1.stuff);

		assertEquals(2, result.foreignCollection.updateAll());
		foreignDao.refresh(f1);
		assertNotNull(f1.stuff);
		assertEquals(stuff1, f1.stuff);
	}

	@Test
	public void testContains() throws Exception {
		Dao<Eager, Integer> eagerDao = createDao(Eager.class, true);
		Dao<Foreign, Integer> foreignDao = createDao(Foreign.class, true);
		Eager eager = new Eager();
		assertEquals(1, eagerDao.create(eager));

		Foreign f0 = new Foreign();
		f0.eager = eager;
		assertEquals(1, foreignDao.create(f0));
		// and another one
		Foreign f1 = new Foreign();
		f1.eager = eager;
		assertEquals(1, foreignDao.create(f1));

		Eager result = eagerDao.queryForId(eager.id);
		for (Foreign foreign : result.foreignCollection) {
			assertTrue(result.foreignCollection.contains(foreign));
		}
	}

	@Test
	public void testListMethods() throws Exception {
		Dao<Eager, Integer> eagerDao = createDao(Eager.class, true);
		Dao<Foreign, Integer> foreignDao = createDao(Foreign.class, true);
		Eager eager = new Eager();
		assertEquals(1, eagerDao.create(eager));

		Foreign f0 = new Foreign();
		f0.eager = eager;
		foreignDao.create(f0);
		// and another one
		Foreign f1 = new Foreign();
		f1.eager = eager;
		foreignDao.create(f1);
		// and another one
		Foreign f2 = new Foreign();
		f2.eager = eager;
		foreignDao.create(f2);

		Eager result = eagerDao.queryForId(eager.id);
		assertNotNull(result.foreignCollection);

		try {
			result.foreignCollection.addAll(0, Collections.<Foreign> emptyList());
			fail("should have thrown UnsupportedOperationException");
		} catch (UnsupportedOperationException use) {
			// expected
		}
		assertEquals(f0.id, result.foreignCollection.get(0).id);
		try {
			result.foreignCollection.set(0, null);
			fail("should have thrown UnsupportedOperationException");
		} catch (UnsupportedOperationException use) {
			// expected
		}
		assertEquals(0, result.foreignCollection.indexOf(f0));
		assertEquals(f0.id, result.foreignCollection.remove(0).id);
		assertEquals(-1, result.foreignCollection.indexOf(f0));
		assertEquals(0, result.foreignCollection.lastIndexOf(f1));
		assertEquals(1, result.foreignCollection.subList(1, 2).size());
		try {
			result.foreignCollection.add(0, null);
			fail("should have thrown UnsupportedOperationException");
		} catch (UnsupportedOperationException use) {
			// expected
		}

		ListIterator<Foreign> iterator = result.foreignCollection.listIterator();
		assertTrue(iterator.hasNext());
		assertEquals(f1.id, iterator.next().id);
		assertTrue(iterator.hasNext());
		assertEquals(f2.id, iterator.next().id);
		assertTrue(iterator.hasPrevious());
		assertEquals(f2.id, iterator.previous().id);
		assertEquals(f1.id, iterator.previous().id);

		iterator = result.foreignCollection.listIterator(1);
		assertTrue(iterator.hasNext());
		assertEquals(f2.id, iterator.next().id);
		assertFalse(iterator.hasNext());
	}

	protected static class Eager {
		@DatabaseField(generatedId = true)
		int id;
		@ForeignCollectionField(eager = true)
		EagerForeignCollection<Foreign, Integer> foreignCollection;

		public Eager() {
		}
	}

	protected static class Foreign {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(foreign = true)
		Eager eager;
		@DatabaseField
		String stuff;

		public Foreign() {
		}

		@Override
		public int hashCode() {
			return id;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			return (id == ((Foreign) obj).id);
		}
	}
}
