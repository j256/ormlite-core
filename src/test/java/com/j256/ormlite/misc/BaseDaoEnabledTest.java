package com.j256.ormlite.misc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;

public class BaseDaoEnabledTest extends BaseCoreTest {

	@Test
	public void testCreate() throws Exception {
		Dao<One, Integer> dao = createDao(One.class, true);
		One one = new One();
		String stuff = "fewpfjewfew";
		one.stuff = stuff;
		one.setDao(dao);
		assertEquals(1, one.create());
	}

	@Test(expected = SQLException.class)
	public void testCreateNoDao() throws Exception {
		One one = new One();
		String stuff = "fewpfjewfew";
		one.stuff = stuff;
		one.create();
	}

	@Test
	public void testUpdate() throws Exception {
		Dao<One, Integer> dao = createDao(One.class, true);
		One one = new One();
		String stuff1 = "fewpfjewfew";
		one.stuff = stuff1;
		assertEquals(1, dao.create(one));
		String stuff2 = "fjpfejpwewpfjewfew";
		one.stuff = stuff2;
		assertEquals(1, one.update());
		One one2 = dao.queryForId(one.id);
		assertEquals(stuff2, one2.stuff);
	}

	@Test
	public void testUpdateId() throws Exception {
		Dao<One, Integer> dao = createDao(One.class, true);
		One one = new One();
		String stuff1 = "fewpfjewfew";
		one.stuff = stuff1;
		assertEquals(1, dao.create(one));
		int id = one.id;
		assertNotNull(dao.queryForId(id));
		assertEquals(1, one.updateId(id + 1));
		assertNull(dao.queryForId(id));
		assertNotNull(dao.queryForId(id + 1));
	}

	@Test
	public void testDelete() throws Exception {
		Dao<One, Integer> dao = createDao(One.class, true);
		One one = new One();
		String stuff1 = "fewpfjewfew";
		one.stuff = stuff1;
		assertEquals(1, dao.create(one));
		assertNotNull(dao.queryForId(one.id));
		assertEquals(1, one.delete());
		assertNull(dao.queryForId(one.id));
	}

	@Test
	public void testToString() throws Exception {
		Dao<One, Integer> dao = createDao(One.class, true);
		One one = new One();
		String stuff1 = "fewpfjewfew";
		one.stuff = stuff1;
		assertEquals(1, dao.create(one));
		String str = one.objectToString();
		assertTrue(str.contains("id=" + one.id));
		assertTrue(str.contains("stuff=" + stuff1));
	}

	@Test
	public void testObjectEquals() throws Exception {
		Dao<One, Integer> dao = createDao(One.class, true);
		One one = new One();
		String stuff1 = "fewpfjewfew";
		one.stuff = stuff1;
		assertEquals(1, dao.create(one));
		assertTrue(one.objectsEqual(one));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testObjectEqualsNoDao() {
		One one = new One();
		String stuff1 = "fewpfjewfew";
		one.stuff = stuff1;
		one.objectToString();
	}

	@Test
	public void testExtractId() throws Exception {
		Dao<One, Integer> dao = createDao(One.class, true);
		One one = new One();
		String stuff1 = "fewpfjewfew";
		one.stuff = stuff1;
		assertEquals(1, dao.create(one));
		assertEquals(one.id, (int) one.extractId());
	}

	@Test
	public void testForeign() throws Exception {
		Dao<One, Integer> oneDao = createDao(One.class, true);
		Dao<ForeignDaoEnabled, Integer> foreignDao = createDao(ForeignDaoEnabled.class, true);

		One one = new One();
		String stuff = "fewpfjewfew";
		one.stuff = stuff;
		one.setDao(oneDao);
		assertEquals(1, one.create());

		ForeignDaoEnabled foreign = new ForeignDaoEnabled();
		foreign.one = one;
		foreign.setDao(foreignDao);
		assertEquals(1, foreign.create());

		ForeignDaoEnabled foreign2 = foreignDao.queryForId(foreign.id);
		assertNotNull(foreign2);
		assertEquals(one.id, foreign2.one.id);
		assertNull(foreign2.one.stuff);
		assertEquals(1, foreign2.one.refresh());
		assertEquals(stuff, foreign2.one.stuff);
	}

	/* ============================================================================================== */

	protected static class One extends BaseDaoEnabled<One, Integer> {
		@DatabaseField(generatedId = true)
		public int id;
		@DatabaseField
		public String stuff;
		public One() {
		}
	}

	protected static class ForeignDaoEnabled extends BaseDaoEnabled<ForeignDaoEnabled, Integer> {
		@DatabaseField(generatedId = true)
		public int id;
		@DatabaseField(foreign = true)
		public One one;
		public ForeignDaoEnabled() {
		}
	}
}
