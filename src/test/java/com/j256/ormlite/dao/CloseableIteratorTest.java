package com.j256.ormlite.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.Iterator;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;

public class CloseableIteratorTest extends BaseCoreTest {

	@Test
	public void testIterator() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		int equal1 = 1231231232;
		foo1.equal = equal1;
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		int equal2 = 1231232;
		foo2.equal = equal2;
		assertEquals(1, dao.create(foo2));

		CloseableIterator<Foo> iterator = dao.iterator();
		assertTrue(iterator.hasNext());
		Foo foo3 = iterator.next();
		assertEquals(foo1.id, foo3.id);
		assertTrue(iterator.hasNext());
		foo3 = iterator.next();
		assertEquals(foo2.id, foo3.id);
		assertFalse(iterator.hasNext());
		iterator.close();
	}

	@Test
	public void testIteratorLastClose() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		assertEquals(1, dao.create(foo1));

		CloseableIterator<Foo> iterator = dao.iterator();
		assertTrue(iterator.hasNext());
		Foo foo3 = iterator.next();
		assertEquals(foo1.id, foo3.id);
		assertFalse(iterator.hasNext());
		dao.closeLastIterator();
	}

	@Test
	public void testWrappedIterableInvalidPreparedQueryFor() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);

		Foo foo1 = new Foo();
		assertEquals(1, dao.create(foo1));

		Foo foo2 = new Foo();
		assertEquals(1, dao.create(foo2));

		CloseableWrappedIterable<Foo> wrapped =
				dao.getWrappedIterable(dao.queryBuilder().where().eq(Foo.ID_COLUMN_NAME, new SelectArg()).prepare());
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
	public void testWrappedIterator() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		assertEquals(1, dao.create(foo1));

		CloseableWrappedIterable<Foo> wrapped = dao.getWrappedIterable();
		CloseableIterator<Foo> iterator = wrapped.closeableIterator();
		// this shouldn't close anything
		dao.closeLastIterator();

		assertTrue(iterator.hasNext());
		Foo foo3 = iterator.next();
		assertEquals(foo1.id, foo3.id);
		assertFalse(iterator.hasNext());
		wrapped.close();
	}

	@Test(expected = IllegalStateException.class)
	public void testIteratorThrow() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection(FOO_TABLE_NAME);
		try {
			conn.close();
			dao.iterator();
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test(expected = IllegalStateException.class)
	public void testIteratorNoInit() throws Exception {
		BaseDaoImpl<Foo, Integer> dao = new BaseDaoImpl<Foo, Integer>(Foo.class) {
		};
		dao.iterator();
	}

	@Test
	public void testIteratorPrepared() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		int equal1 = 1231231232;
		foo1.equal = equal1;
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		int equal2 = 1231232;
		foo2.equal = equal2;
		assertEquals(1, dao.create(foo2));

		QueryBuilder<Foo, Integer> queryBuilder = dao.queryBuilder();
		queryBuilder.where().eq(Foo.ID_COLUMN_NAME, foo1.id);

		CloseableIterator<Foo> iterator = dao.iterator(queryBuilder.prepare());
		assertTrue(iterator.hasNext());
		Foo result = iterator.next();
		assertEquals(foo1.id, result.id);
		assertFalse(iterator.hasNext());
	}

	@Test(expected = IllegalStateException.class)
	public void testIteratorPreparedNoInit() throws Exception {
		BaseDaoImpl<Foo, Integer> dao = new BaseDaoImpl<Foo, Integer>(Foo.class) {
		};
		dao.iterator((PreparedQuery<Foo>) null);
	}

	@Test(expected = SQLException.class)
	public void testIteratorPreparedThrow() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection(FOO_TABLE_NAME);
		try {
			conn.close();
			dao.iterator(dao.queryBuilder().prepare());
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testQueryRawDatabaseResults() throws Exception {
		Dao<Foo, Object> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		foo.val = 342234232;
		assertEquals(1, dao.create(foo));

		CloseableIterator<Foo> iterator =
				dao.iterator(dao.queryBuilder().where().eq(Foo.VAL_COLUMN_NAME, foo.val).prepare());
		try {
			DatabaseResults results = iterator.getRawResults();
			assertTrue(results.first());
			assertTrue(results.getColumnCount() >= 4);
			int valIndex = results.findColumn(Foo.VAL_COLUMN_NAME);
			assertEquals(foo.val, results.getInt(valIndex));
			assertFalse(results.next());
		} finally {
			iterator.closeQuietly();
		}
	}

	@Test
	public void testQueryRawMappedIterator() throws Exception {
		Dao<Foo, Object> fooDao = createDao(Foo.class, true);
		final Foo foo = new Foo();
		foo.val = 1313131;
	
		String queryString = buildFooQueryAllString(fooDao);
		Mapper mapper = new Mapper();
		GenericRawResults<Foo> rawResults = fooDao.queryRaw(queryString, mapper);
		assertEquals(0, rawResults.getResults().size());
		assertEquals(1, fooDao.create(foo));
		rawResults = fooDao.queryRaw(queryString, mapper);
		Iterator<Foo> iterator = rawResults.iterator();
		assertTrue(iterator.hasNext());
		Foo foo2 = iterator.next();
		assertEquals(foo.id, foo2.id);
		assertEquals(foo.val, foo2.val);
		assertEquals(foo.val, foo2.val);
		assertFalse(iterator.hasNext());
	}

	@Test
	public void testIteratorMoveAbsolute() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);

		Foo foo1 = new Foo();
		foo1.val = 1389183;
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		foo2.val = 133214433;
		assertEquals(1, dao.create(foo2));
		Foo foo3 = new Foo();
		foo3.val = 3232;
		assertEquals(1, dao.create(foo3));

		assertEquals(3, dao.countOf());

		// This is working fine.
		CloseableIterator<Foo> iterator = dao.queryBuilder().iterator();
		try {
			assertEquals(foo1, iterator.first());
			assertEquals(foo2, iterator.nextThrow());
			assertEquals(foo3, iterator.nextThrow());
		} finally {
			iterator.closeQuietly();
		}

		iterator = dao.queryBuilder().iterator();
		try {
			// skip over foo1
			assertEquals(foo2, iterator.moveAbsolute(2));
			assertEquals(foo3, iterator.nextThrow());
		} finally {
			iterator.closeQuietly();
		}
	}

	/* ============================================================================================== */

	private String buildFooQueryAllString(Dao<Foo, Object> fooDao) throws SQLException {
		String queryString = fooDao.queryBuilder()
				.selectColumns(Foo.ID_COLUMN_NAME, Foo.EQUAL_COLUMN_NAME, Foo.VAL_COLUMN_NAME)
				.prepareStatementString();
		return queryString;
	}

	private static class Mapper implements RawRowMapper<Foo> {
		@Override
		public Foo mapRow(String[] columnNames, String[] resultColumns) {
			Foo foo = new Foo();
			for (int i = 0; i < columnNames.length; i++) {
				if (columnNames[i].equalsIgnoreCase(Foo.ID_COLUMN_NAME)) {
					foo.id = Integer.parseInt(resultColumns[i]);
				} else if (columnNames[i].equalsIgnoreCase(Foo.VAL_COLUMN_NAME)) {
					foo.val = Integer.parseInt(resultColumns[i]);
				}
			}
			return foo;
		}
	}
}
