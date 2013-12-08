package com.j256.ormlite.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;

public class VersionFieldTest extends BaseCoreTest {

	@Test
	public void testVersionField() throws Exception {
		Dao<VersionField, Integer> dao = createDao(VersionField.class, true);

		VersionField foo1 = new VersionField();
		assertEquals(1, dao.create(foo1));

		assertEquals(1, foo1.id);
		assertEquals(0, foo1.version);

		assertEquals(1, dao.update(foo1));
		assertEquals(1, foo1.version);

		assertEquals(1, dao.update(foo1));
		assertEquals(2, foo1.version);

		VersionField result = dao.queryForId(foo1.id);
		// we update this one to a new version number
		assertEquals(1, dao.update(result));
		assertEquals(3, result.version);

		// the old one doesn't change
		assertEquals(2, foo1.version);
		// but when we try to update the earlier foo, the version doesn't match
		assertEquals(0, dao.update(foo1));
	}

	@Test
	public void testVersionFieldNonDefault() throws Exception {
		Dao<VersionField, Integer> dao = createDao(VersionField.class, true);

		VersionField foo1 = new VersionField();
		foo1.version = 10;
		assertEquals(1, dao.create(foo1));

		VersionField result = dao.queryForId(foo1.id);
		// we update this one to a new version number
		assertEquals(1, dao.update(result));
		assertEquals(foo1.version + 1, result.version);

		assertEquals(1, dao.update(result));
		assertEquals(foo1.version + 2, result.version);
	}

	@Test
	public void testVersionFieldDate() throws Exception {
		Dao<VersionFieldDate, Integer> dao = createDao(VersionFieldDate.class, true);

		VersionFieldDate foo1 = new VersionFieldDate();
		long before1 = System.currentTimeMillis();
		assertEquals(1, dao.create(foo1));
		long after = System.currentTimeMillis();
		assertNotNull(foo1.version);
		assertTrue(foo1.version.getTime() >= before1 && foo1.version.getTime() <= after);

		long before2 = System.currentTimeMillis();
		assertEquals(1, dao.update(foo1));
		after = System.currentTimeMillis();
		assertTrue(before2 >= before1);
		// we do after+1 here because if previous time == now then we do a + 1
		assertTrue(foo1.version.getTime() >= before2 && foo1.version.getTime() <= after + 1);
	}

	@Test
	public void testVersionFieldDateLong() throws Exception {
		Dao<VersionFieldDateLong, Integer> dao = createDao(VersionFieldDateLong.class, true);

		VersionFieldDateLong foo1 = new VersionFieldDateLong();
		long before1 = System.currentTimeMillis();
		assertEquals(1, dao.create(foo1));
		long after = System.currentTimeMillis();
		assertNotNull(foo1.version);
		assertTrue(foo1.version.getTime() >= before1 && foo1.version.getTime() <= after);

		long before2 = System.currentTimeMillis();
		assertEquals(1, dao.update(foo1));
		after = System.currentTimeMillis();
		assertTrue(before2 >= before1);
		// we do after+1 here because if previous time == now then we do a + 1
		assertTrue(foo1.version.getTime() >= before2 && foo1.version.getTime() <= after + 1);
	}

	@Test
	public void testVersionFieldDateString() throws Exception {
		Dao<VersionFieldDateString, Integer> dao = createDao(VersionFieldDateString.class, true);

		VersionFieldDateString foo1 = new VersionFieldDateString();
		long before1 = System.currentTimeMillis();
		assertEquals(1, dao.create(foo1));
		long after = System.currentTimeMillis();
		assertNotNull(foo1.version);
		assertTrue(foo1.version.getTime() >= before1 && foo1.version.getTime() <= after);

		long before2 = System.currentTimeMillis();
		assertEquals(1, dao.update(foo1));
		after = System.currentTimeMillis();
		assertTrue(before2 >= before1);
		// we do after+1 here because if previous time == now then we do a + 1
		assertTrue(foo1.version.getTime() >= before2 && foo1.version.getTime() <= after + 1);
	}

	protected static class VersionField {
		@DatabaseField(generatedId = true)
		public int id;
		@DatabaseField
		String stuff1;
		@DatabaseField(version = true)
		public int version;
		@DatabaseField
		String stuff2;
		public VersionField() {
		}
	}

	protected static class VersionFieldDate {
		@DatabaseField(generatedId = true)
		public int id;
		@DatabaseField
		String stuff1;
		@DatabaseField(version = true)
		public Date version;
		@DatabaseField
		String stuff2;
		public VersionFieldDate() {
		}
	}

	protected static class VersionFieldDateLong {
		@DatabaseField(generatedId = true)
		public int id;
		@DatabaseField
		String stuff1;
		@DatabaseField(version = true, dataType = DataType.DATE_LONG)
		public Date version;
		@DatabaseField
		String stuff2;
		public VersionFieldDateLong() {
		}
	}

	protected static class VersionFieldDateString {
		@DatabaseField(generatedId = true)
		public int id;
		@DatabaseField
		String stuff1;
		@DatabaseField(version = true, dataType = DataType.DATE_STRING)
		public Date version;
		@DatabaseField
		String stuff2;
		public VersionFieldDateString() {
		}
	}
}
