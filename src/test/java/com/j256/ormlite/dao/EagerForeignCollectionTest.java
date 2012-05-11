package com.j256.ormlite.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

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

	protected static class Eager {
		@DatabaseField(generatedId = true)
		int id;
		@ForeignCollectionField(eager = true)
		ForeignCollection<Foreign> foreignCollection;
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
	}
}
