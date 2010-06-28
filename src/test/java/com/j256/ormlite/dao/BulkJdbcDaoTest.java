package com.j256.ormlite.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.j256.ormlite.BaseOrmLiteTest;
import com.j256.ormlite.field.DatabaseField;

public class BulkJdbcDaoTest extends BaseOrmLiteTest {

	private final static int NUMBER_OBJECTS_TO_CREATE = 1000;

	private Dao<Foo, Integer> fooDao;

	@Before
	@Override
	public void before() throws Exception {
		super.before();
		fooDao = createDao(Foo.class, true);
	}

	@Test
	public void testCreateBulk() throws Exception {
		for (int i = 0; i < NUMBER_OBJECTS_TO_CREATE; i++) {
			Foo foo = new Foo();
			foo.index = i;
			assertEquals(1, fooDao.create(foo));
		}

		int fooC = 0;
		for (Foo foo : fooDao) {
			assertEquals(fooC, foo.index);
			fooC++;
		}
		assertEquals(NUMBER_OBJECTS_TO_CREATE, fooC);
	}

	@Test
	public void testObjectsEqual() throws Exception {
		Foo foo = new Foo();
		assertEquals(1, fooDao.create(foo));
		int id = foo.id;
		Foo foo2 = fooDao.queryForId(id);
		// both other are null
		assertTrue(fooDao.objectsEqual(foo, foo2));

		// set to some other number
		foo2.id = 1341313;
		assertFalse(fooDao.objectsEqual(foo2, foo));

		foo2 = fooDao.queryForId(id);
		foo.other = "not null";
		// try null checks from either direction
		assertFalse(fooDao.objectsEqual(foo, foo2));
		assertFalse(fooDao.objectsEqual(foo2, foo));
	}

	protected static class Foo {
		// @DatabaseField(generatedId = true, generatedIdSequence = "foo_id_seq")
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		int index;
		@DatabaseField
		String other;
	}
}
