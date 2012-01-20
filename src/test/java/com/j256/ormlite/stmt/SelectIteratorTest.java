package com.j256.ormlite.stmt;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;

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
		try {
			iterator.remove();
		} finally {
			iterator.close();
		}
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
		iterator.close();
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
		iterator.close();
	}

	@Test(expected = IllegalStateException.class)
	public void testIteratorRawResults() throws Exception {
		Dao<Foo, Object> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		String id1 = "id1";
		foo1.id = id1;
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
		Dao<Foo, Object> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		String id1 = "id1";
		foo1.id = id1;
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
		expect(results.next()).andThrow(new SQLException("some database problem"));
		stmt.close();
		replay(stmt, results, cs);
		SelectIterator<Foo, Integer> iterator =
				new SelectIterator<Foo, Integer>(Foo.class, null, null, cs, null, stmt, "statement", null);
		iterator.hasNext();
	}

	@Test(expected = IllegalStateException.class)
	public void testNextThrow() throws Exception {
		ConnectionSource cs = createMock(ConnectionSource.class);
		cs.releaseConnection(null);
		CompiledStatement stmt = createMock(CompiledStatement.class);
		expect(stmt.runQuery(null)).andReturn(null);
		@SuppressWarnings("unchecked")
		GenericRowMapper<Foo> mapper = (GenericRowMapper<Foo>) createMock(GenericRowMapper.class);
		expect(mapper.mapRow(null)).andThrow(new SQLException("some result problem"));
		stmt.close();
		replay(stmt, mapper, cs);
		SelectIterator<Foo, Integer> iterator =
				new SelectIterator<Foo, Integer>(Foo.class, null, mapper, cs, null, stmt, "statement", null);
		iterator.next();
	}

	@Test(expected = IllegalStateException.class)
	public void testRemoveThrow() throws Exception {
		ConnectionSource cs = createMock(ConnectionSource.class);
		cs.releaseConnection(null);
		CompiledStatement stmt = createMock(CompiledStatement.class);
		DatabaseResults results = createMock(DatabaseResults.class);
		expect(results.next()).andReturn(true);
		expect(stmt.runQuery(null)).andReturn(results);
		@SuppressWarnings("unchecked")
		GenericRowMapper<Foo> mapper = (GenericRowMapper<Foo>) createMock(GenericRowMapper.class);
		Foo foo = new Foo();
		foo.id = "pwjfoefjw";
		expect(mapper.mapRow(results)).andReturn(foo);
		@SuppressWarnings("unchecked")
		Dao<Foo, Integer> dao = (Dao<Foo, Integer>) createMock(Dao.class);
		expect(dao.delete(foo)).andThrow(new SQLException("some dao problem"));
		stmt.close();
		replay(stmt, dao, results, mapper, cs);
		SelectIterator<Foo, Integer> iterator =
				new SelectIterator<Foo, Integer>(Foo.class, dao, mapper, cs, null, stmt, "statement", null);
		iterator.hasNext();
		iterator.next();
		iterator.remove();
	}
}
