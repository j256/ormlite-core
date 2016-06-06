package com.j256.ormlite.stmt.mapped;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.sql.SQLException;

import org.junit.Test;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.BaseCoreStmtTest;
import com.j256.ormlite.stmt.StatementExecutor;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.TableInfo;

public class MappedQueryForIdTest extends BaseCoreStmtTest {

	@Test(expected = SQLException.class)
	public void testQueryNoId() throws Exception {
		StatementExecutor<NoId, String> se =
				new StatementExecutor<NoId, String>(databaseType, new TableInfo<NoId, String>(connectionSource, null,
						NoId.class), null);
		se.queryForId(null, "1", null);
	}

	@Test(expected = SQLException.class)
	public void testNoIdBuildUpdater() throws Exception {
		MappedUpdate.build(databaseType, new TableInfo<NoId, Void>(connectionSource, null, NoId.class));
	}

	@Test(expected = SQLException.class)
	public void testNoIdBuildQueryForId() throws Exception {
		MappedQueryForFieldEq.build(databaseType, new TableInfo<NoId, Void>(connectionSource, null, NoId.class), null);
	}

	@Test
	public void testCreateReserverdFields() throws Exception {
		Dao<ReservedField, Object> reservedDao = createDao(ReservedField.class, true);
		String from = "from-string";
		ReservedField res = new ReservedField();
		res.from = from;
		reservedDao.create(res);
		int id = res.select;
		ReservedField res2 = reservedDao.queryForId(id);
		assertEquals(id, res2.select);
		String group = "group-string";
		for (ReservedField reserved : reservedDao) {
			assertEquals(from, reserved.from);
			reserved.group = group;
			reservedDao.update(reserved);
		}
		CloseableIterator<ReservedField> reservedIterator = reservedDao.iterator();
		while (reservedIterator.hasNext()) {
			ReservedField reserved = reservedIterator.next();
			assertEquals(from, reserved.from);
			assertEquals(group, reserved.group);
			reservedIterator.remove();
		}
		assertEquals(0, reservedDao.queryForAll().size());
		reservedIterator.close();
	}

	@Test
	public void testCreateReserverdTable() throws Exception {
		Dao<Where, String> whereDao = createDao(Where.class, true);
		String id = "from-string";
		Where where = new Where();
		where.id = id;
		whereDao.create(where);
		Where where2 = whereDao.queryForId(id);
		assertEquals(id, where2.id);
		assertEquals(1, whereDao.delete(where2));
		assertNull(whereDao.queryForId(id));
	}

	@Test(expected = SQLException.class)
	public void testTooManyFooId() throws Exception {
		Dao<LocalFoo, String> fooDao = createDao(LocalFoo.class, true);
		String stuff = "zing";
		LocalFoo foo = new LocalFoo();
		foo.id = "blah";
		foo.stuff = stuff;
		assertEquals(1, fooDao.create(foo));

		foo = new LocalFoo();
		foo.id = "ick";
		foo.stuff = stuff;
		assertEquals(1, fooDao.create(foo));

		// don't do this at home kiddies -- creates a different dao looking at the Foo table
		// it doesn't create the new table
		Dao<FakeFoo, String> fakeFooDao = createDao(FakeFoo.class, false);
		// this fails because >1 item is returned from an id search -- baaaaad
		fakeFooDao.queryForId(stuff);
	}

	// for testing reserved words as field names
	protected static class ReservedField {
		@DatabaseField(generatedId = true)
		public int select;
		@DatabaseField
		public String from;
		@DatabaseField
		public String table;
		@DatabaseField
		public String where;
		@DatabaseField
		public String group;
		@DatabaseField
		public String order;
		@DatabaseField
		public String values;
	}

	// for testing reserved table names as fields
	private static class Where {
		@DatabaseField(id = true)
		public String id;
	}

	private final static String FOO_TABLE_NAME = "foo";

	// Foo and FakeFoo have the same table name -- don't try this @ home
	@DatabaseTable(tableName = FOO_TABLE_NAME)
	protected static class LocalFoo {
		@DatabaseField(id = true)
		public String id;
		@DatabaseField
		public String stuff;
	}

	// Foo and FakeFoo have the same table name -- don't try this @ home
	@DatabaseTable(tableName = FOO_TABLE_NAME)
	protected static class FakeFoo {
		@DatabaseField
		public String id;
		@DatabaseField(id = true)
		public String stuff;
	}

	protected static class NoId {
		@DatabaseField
		String id;
	}
}
