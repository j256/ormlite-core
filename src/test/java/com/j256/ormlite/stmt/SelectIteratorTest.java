package com.j256.ormlite.stmt;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseResults;

public class SelectIteratorTest extends BaseCoreStmtTest {

	@Test
	public void testIterator() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		CloseableIterator<Foo> iterator = dao.iterator();
		assertFalse(iterator.hasNext());

		Foo foo1 = new Foo();
		assertEquals(1, dao.create(foo1));

		Foo foo2 = new Foo();
		assertEquals(1, dao.create(foo2));

		iterator = dao.iterator();
		assertTrue(iterator.hasNext());
		Foo result = iterator.next();
		assertEquals(foo1.id, result.id);
		assertTrue(iterator.hasNext());

		result = iterator.next();
		assertEquals(foo2.id, result.id);

		assertFalse(iterator.hasNext());
		assertNull(iterator.nextThrow());
	}

	@Test
	public void testIteratorPrepared() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		assertEquals(1, dao.create(foo1));

		Foo foo2 = new Foo();
		assertEquals(1, dao.create(foo2));

		PreparedQuery<Foo> query = dao.queryBuilder().where().eq(Foo.ID_COLUMN_NAME, foo2.id).prepare();
		CloseableIterator<Foo> iterator = dao.iterator(query);
		assertTrue(iterator.hasNext());
		Foo result = iterator.next();
		assertEquals(foo2.id, result.id);
		assertFalse(iterator.hasNext());
		assertNull(iterator.nextThrow());
	}

	@Test(expected = IllegalStateException.class)
	public void testIteratorRemoveNoNext() throws Exception {
		Dao<Foo, Object> dao = createDao(Foo.class, true);
		CloseableIterator<Foo> iterator = dao.iterator();
		try {
			iterator.remove();
		} finally {
			iterator.close();
		}
	}

	@Test(expected = IllegalStateException.class)
	public void testIteratorNextRemoveRemoveNoNext() throws Exception {
		Dao<Foo, Object> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		assertEquals(1, dao.create(foo2));
		CloseableIterator<Foo> iterator = dao.iterator();
		try {
			iterator.next();
			iterator.remove();
			iterator.remove();
		} finally {
			iterator.close();
		}
	}

	@Test
	public void testIteratorRemove() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		assertEquals(1, dao.create(foo1));

		assertEquals(1, dao.queryForAll().size());

		CloseableIterator<Foo> iterator = dao.iterator();
		assertTrue(iterator.hasNext());
		Foo result = iterator.next();
		assertEquals(foo1.id, result.id);
		iterator.remove();

		assertFalse(iterator.hasNext());
		assertNull(iterator.nextThrow());

		assertEquals(0, dao.queryForAll().size());
		iterator.close();
	}

	@Test
	public void testIteratorHasNextClosed() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		assertEquals(1, dao.create(foo1));

		assertEquals(1, dao.queryForAll().size());

		CloseableIterator<Foo> iterator = dao.iterator();
		assertTrue(iterator.hasNext());
		iterator.next();

		assertFalse(iterator.hasNext());
		assertNull(iterator.nextThrow());

		iterator.close();
		assertFalse(iterator.hasNext());
	}

	@Test
	public void testIteratorGetRawResults() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		assertEquals(1, dao.create(foo1));

		assertEquals(1, dao.queryForAll().size());

		@SuppressWarnings("unchecked")
		SelectIterator<Foo, String> iterator = (SelectIterator<Foo, String>) dao.iterator();
		DatabaseResults results = iterator.getRawResults();
		assertTrue(results.next());
		iterator.close();
	}

	@Test(expected = IllegalStateException.class)
	public void testIteratorRawResults() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		assertEquals(1, dao.create(foo1));

		GenericRawResults<String[]> rawResults = dao.queryRaw("SELECT " + Foo.ID_COLUMN_NAME + " FROM FOO");
		CloseableIterator<String[]> iterator = rawResults.closeableIterator();
		try {
			assertTrue(iterator.hasNext());
			iterator.next();
			iterator.remove();
		} finally {
			iterator.close();
		}
	}

	@Test
	public void testMultipleHasNext() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		assertEquals(1, dao.create(foo1));

		CloseableIterator<Foo> iterator = dao.iterator();
		assertTrue(iterator.hasNext());
		assertTrue(iterator.hasNext());
		assertTrue(iterator.hasNext());
		iterator.moveToNext();
		assertFalse(iterator.hasNext());
	}

	@Test(expected = IllegalStateException.class)
	public void testHasNextThrow() throws Exception {
		ConnectionSource cs = createMock(ConnectionSource.class);
		cs.releaseConnection(null);
		CompiledStatement stmt = createMock(CompiledStatement.class);
		DatabaseResults results = createMock(DatabaseResults.class);
		expect(stmt.runQuery(null)).andReturn(results);
		expect(results.first()).andThrow(new SQLException("some database problem"));
		stmt.close();
		replay(stmt, results, cs);
		SelectIterator<Foo, Integer> iterator =
				new SelectIterator<Foo, Integer>(Foo.class, null, null, cs, null, stmt, "statement", null);
		try {
			iterator.hasNext();
		} finally {
			iterator.close();
		}
	}

	@Test(expected = IllegalStateException.class)
	public void testNextThrow() throws Exception {
		ConnectionSource cs = createMock(ConnectionSource.class);
		cs.releaseConnection(null);
		CompiledStatement stmt = createMock(CompiledStatement.class);
		DatabaseResults results = createMock(DatabaseResults.class);
		expect(stmt.runQuery(null)).andReturn(results);
		expect(results.first()).andThrow(new SQLException("some result problem"));
		@SuppressWarnings("unchecked")
		GenericRowMapper<Foo> mapper = (GenericRowMapper<Foo>) createMock(GenericRowMapper.class);
		stmt.close();
		replay(stmt, mapper, cs, results);
		SelectIterator<Foo, Integer> iterator =
				new SelectIterator<Foo, Integer>(Foo.class, null, mapper, cs, null, stmt, "statement", null);
		try {
			iterator.hasNext();
		} finally {
			iterator.close();
		}
		verify(stmt, mapper, cs, results);
	}

	@Test(expected = IllegalStateException.class)
	public void testRemoveThrow() throws Exception {
		ConnectionSource cs = createMock(ConnectionSource.class);
		cs.releaseConnection(null);
		CompiledStatement stmt = createMock(CompiledStatement.class);
		DatabaseResults results = createMock(DatabaseResults.class);
		expect(results.first()).andReturn(true);
		expect(stmt.runQuery(null)).andReturn(results);
		@SuppressWarnings("unchecked")
		GenericRowMapper<Foo> mapper = (GenericRowMapper<Foo>) createMock(GenericRowMapper.class);
		Foo foo = new Foo();
		expect(mapper.mapRow(results)).andReturn(foo);
		@SuppressWarnings("unchecked")
		Dao<Foo, Integer> dao = (Dao<Foo, Integer>) createMock(Dao.class);
		expect(dao.delete(foo)).andThrow(new SQLException("some dao problem"));
		stmt.close();
		replay(stmt, dao, results, mapper, cs);
		SelectIterator<Foo, Integer> iterator =
				new SelectIterator<Foo, Integer>(Foo.class, dao, mapper, cs, null, stmt, "statement", null);
		try {
			iterator.hasNext();
			iterator.next();
			iterator.remove();
		} finally {
			iterator.close();
		}
	}

	@Test
	public void testIteratorMoveAround() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		List<Foo> fooList = new ArrayList<Foo>();
		for (int i = 0; i < 10; i++) {
			Foo foo = new Foo();
			foo.val = i;
			assertEquals(1, dao.create(foo));
			fooList.add(foo);
		}

		QueryBuilder<Foo, Integer> qb = dao.queryBuilder();
		qb.orderBy(Foo.VAL_COLUMN_NAME, true);
		CloseableIterator<Foo> iterator = dao.iterator(qb.prepare(), ResultSet.TYPE_SCROLL_INSENSITIVE);
		try {
			assertEquals(fooList.get(0), iterator.first());
			assertEquals(fooList.get(0), iterator.first());
			assertEquals(fooList.get(0), iterator.current());
			assertEquals(fooList.get(1), iterator.next());
			assertEquals(fooList.get(1), iterator.current());
			assertEquals(fooList.get(0), iterator.first());
			assertEquals(fooList.get(0), iterator.current());
			assertEquals(fooList.get(1), iterator.next());
			assertTrue(iterator.hasNext());
			assertEquals(fooList.get(2), iterator.next());
			assertEquals(fooList.get(2), iterator.current());
			assertEquals(fooList.get(1), iterator.previous());
			assertEquals(fooList.get(2), iterator.next());
			assertEquals(fooList.get(1), iterator.moveRelative(-1));
			assertEquals(fooList.get(3), iterator.moveRelative(2));
			assertEquals(fooList.get(9), iterator.moveRelative(6));
			assertFalse(iterator.hasNext());
			assertNull(iterator.current());
		} finally {
			iterator.close();
		}
	}

	@Test(expected = SQLException.class)
	public void testIteratorJdbcMoveBack() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));

		QueryBuilder<Foo, Integer> qb = dao.queryBuilder();
		qb.orderBy(Foo.VAL_COLUMN_NAME, true);
		CloseableIterator<Foo> iterator = dao.iterator(qb.prepare());
		try {
			assertEquals(foo, iterator.first());
			iterator.first();
			fail("Should have thrown");
		} finally {
			iterator.close();
		}
	}

	@Test(expected = IllegalStateException.class)
	public void testIteratorNextOnly() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));

		CloseableIterator<Foo> iterator = dao.iterator();
		try {
			assertEquals(foo, iterator.next());
			iterator.next();
			fail("Should have thrown");
		} finally {
			iterator.close();
		}
	}

	@Test
	public void testMoveClosed() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);

		CloseableIterator<Foo> iterator = dao.iterator();
		try {
			assertFalse(iterator.hasNext());
			assertNull(iterator.first());
			assertNull(iterator.previous());
			assertNull(iterator.current());
			assertNull(iterator.nextThrow());
			assertNull(iterator.moveRelative(10));
		} finally {
			iterator.close();
		}
	}

	@Test
	public void testFirstCurrent() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));

		CloseableIterator<Foo> iterator = dao.iterator();
		try {
			assertEquals(foo, iterator.current());
			assertFalse(iterator.hasNext());
			assertNull(iterator.current());
		} finally {
			iterator.close();
		}
	}

	@Test
	public void testMoveNone() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);

		CloseableIterator<Foo> iterator = dao.iterator();
		try {
			assertNull(iterator.current());
		} finally {
			iterator.close();
		}

		iterator = dao.iterator();
		try {
			assertNull(iterator.previous());
		} finally {
			iterator.close();
		}

		iterator = dao.iterator();
		try {
			assertNull(iterator.moveRelative(1));
		} finally {
			iterator.close();
		}

		iterator = dao.iterator();
		try {
			assertNull(iterator.nextThrow());
		} finally {
			iterator.close();
		}
	}
}
