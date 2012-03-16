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
	public void test() throws Exception {
		Dao<Eager, Integer> eagerDao = createDao(Eager.class, true);
		Dao<Foreign, Integer> foreignDao = createDao(Foreign.class, true);
		Eager eager = new Eager();
		assertEquals(1, eagerDao.create(eager));

		Eager result = eagerDao.queryForId(eager.id);
		assertNotNull(result.foreign);
		assertTrue(result.foreign.isEager());
		assertFalse(System.identityHashCode(result.foreign) == result.foreign.hashCode());
		assertEquals(result.foreign, result.foreign);

		Foreign f0 = new Foreign();
		f0.eager = eager;
		assertEquals(1, foreignDao.create(f0));
		// and another one
		Foreign f1 = new Foreign();
		f1.eager = eager;
		assertEquals(1, foreignDao.create(f1));

		Foreign[] foreigns = result.foreign.toArray(new Foreign[0]);
		assertNotNull(foreigns);
		assertEquals(0, foreigns.length);

		assertEquals(2, result.foreign.refreshCollection());
		foreigns = result.foreign.toArray(new Foreign[0]);
		assertNotNull(foreigns);
		assertEquals(2, foreigns.length);
		assertEquals(f0.id, foreigns[0].id);
		assertEquals(f1.id, foreigns[1].id);

		String stuff0 = "some stuff";
		f0.stuff = stuff0;
		assertEquals(1, foreignDao.update(f0));

		assertNull(foreigns[0].stuff);
		assertEquals(2, result.foreign.refreshAll());
		assertNotNull(foreigns[0].stuff);
		assertNotNull(stuff0, foreigns[0].stuff);

		String stuff1 = "other stuff";
		foreigns[1].stuff = stuff1;
		foreignDao.refresh(f1);
		assertNull(f1.stuff);

		assertEquals(2, result.foreign.updateAll());
		foreignDao.refresh(f1);
		assertNotNull(f1.stuff);
		assertEquals(stuff1, f1.stuff);
	}

	protected static class Eager {
		@DatabaseField(generatedId = true)
		int id;
		@ForeignCollectionField(eager = true)
		ForeignCollection<Foreign> foreign;
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
