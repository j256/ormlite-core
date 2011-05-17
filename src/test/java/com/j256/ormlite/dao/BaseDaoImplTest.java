package com.j256.ormlite.dao;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.h2.api.Trigger;
import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;

public class BaseDaoImplTest extends BaseCoreTest {

	@Test
	public void testDoubleInitialize() throws Exception {
		BaseDaoImpl<Foo, String> dao = new BaseDaoImpl<Foo, String>(connectionSource, Foo.class) {
		};
		dao.initialize();
		// this shouldn't barf
		dao.initialize();
	}

	@Test(expected = IllegalStateException.class)
	public void testInitNoConnectionSource() throws Exception {
		BaseDaoImpl<Foo, String> dao = new BaseDaoImpl<Foo, String>(Foo.class) {
		};
		dao.initialize();
	}

	@Test
	public void testCreate() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		String id = "stuff";
		foo.id = id;
		int equal = 21313;
		foo.equal = equal;
		assertEquals(1, dao.create(foo));
		Foo foo2 = dao.queryForId(id);
		assertNotNull(foo2);
		assertEquals(equal, foo2.equal);
	}

	@Test(expected = SQLException.class)
	public void testQueryForIdThrow() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		String id = "stuff";
		foo.id = id;
		int equal = 21313;
		foo.equal = equal;
		assertEquals(1, dao.create(foo));

		DatabaseConnection conn = connectionSource.getReadWriteConnection();
		try {
			conn.close();
			dao.queryForId(id);
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testQueryPrepared() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		String id1 = "stuff1";
		foo1.id = id1;
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		String id2 = "stuff2";
		foo2.id = id2;
		assertEquals(1, dao.create(foo2));

		Where<Foo, String> qb = dao.queryBuilder().where().eq(Foo.ID_COLUMN_NAME, id2);
		List<Foo> results = dao.query(qb.prepare());
		assertEquals(1, results.size());
		assertEquals(id2, results.get(0).id);
	}

	@Test(expected = SQLException.class)
	public void testCreateThrow() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		// no id set
		dao.create(foo);
	}

	@Test
	public void testCreateNull() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		assertEquals(0, dao.create(null));
	}

	@Test(expected = IllegalStateException.class)
	public void testCreateNoInit() throws Exception {
		BaseDaoImpl<Foo, String> dao = new BaseDaoImpl<Foo, String>(Foo.class) {
		};
		dao.setConnectionSource(connectionSource);
		assertEquals(0, dao.create(null));
	}

	@Test
	public void testUpdate() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		foo.id = "stuff";
		assertEquals(1, dao.create(foo));
		foo.equal = 1;
		assertEquals(1, dao.update(foo));
	}

	@Test(expected = SQLException.class)
	public void testUpdateThrow() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		foo.id = "stuff";
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection();
		try {
			conn.close();
			assertEquals(1, dao.update(foo));
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testUpdateNull() throws Exception {
		BaseDaoImpl<Foo, String> dao = new BaseDaoImpl<Foo, String>(connectionSource, Foo.class) {
		};
		dao.initialize();
		assertEquals(0, dao.update((Foo) null));
	}

	@Test(expected = IllegalStateException.class)
	public void testUpdateNoInit() throws Exception {
		BaseDaoImpl<Foo, String> dao = new BaseDaoImpl<Foo, String>(Foo.class) {
		};
		dao.setConnectionSource(connectionSource);
		assertEquals(0, dao.update((Foo) null));
	}

	@Test
	public void testUpdateId() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		foo.id = "stuff1";
		assertEquals(1, dao.create(foo));
		assertEquals(1, dao.updateId(foo, "stuff2"));
	}

	@Test(expected = SQLException.class)
	public void testUpdateIdThrow() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		foo.id = "stuff";
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection();
		try {
			conn.close();
			assertEquals(1, dao.updateId(foo, "new id"));
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testUpdateIdNull() throws Exception {
		BaseDaoImpl<Foo, String> dao = new BaseDaoImpl<Foo, String>(connectionSource, Foo.class) {
		};
		dao.initialize();
		assertEquals(0, dao.updateId(null, null));
	}

	@Test(expected = IllegalStateException.class)
	public void testUpdateIdNoInit() throws Exception {
		BaseDaoImpl<Foo, String> dao = new BaseDaoImpl<Foo, String>(Foo.class) {
		};
		dao.setConnectionSource(connectionSource);
		assertEquals(0, dao.updateId(null, null));
	}

	@Test
	public void testUpdatePrepared() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		String id1 = "stuff1";
		foo.id = id1;
		assertEquals(1, dao.create(foo));
		String id2 = "stuff2";
		foo.id = id2;
		assertEquals(1, dao.create(foo));
		String id3 = "stuff3";

		assertNotNull(dao.queryForId(id1));
		assertNotNull(dao.queryForId(id2));
		assertNull(dao.queryForId(id3));

		UpdateBuilder<Foo, String> updateBuilder = dao.updateBuilder();
		updateBuilder.updateColumnValue(Foo.ID_COLUMN_NAME, id3);
		updateBuilder.where().eq(Foo.ID_COLUMN_NAME, id1);
		assertEquals(1, dao.update(updateBuilder.prepare()));

		assertNull(dao.queryForId(id1));
		assertNotNull(dao.queryForId(id2));
		assertNotNull(dao.queryForId(id3));
		assertEquals(2, dao.queryForAll().size());
	}

	@Test(expected = SQLException.class)
	public void testUpdatePreparedThrow() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		foo.id = "stuff";
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection();
		try {
			conn.close();
			UpdateBuilder<Foo, String> ub = dao.updateBuilder();
			ub.updateColumnValue(Foo.EQUAL_COLUMN_NAME, 1);
			assertEquals(1, dao.update(ub.prepare()));
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testDelete() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		String id1 = "stuff1";
		foo.id = id1;
		assertEquals(1, dao.create(foo));
		assertNotNull(dao.queryForId(id1));
		assertEquals(1, dao.delete(foo));
		assertNull(dao.queryForId(id1));
		assertEquals(0, dao.queryForAll().size());
	}

	@Test(expected = SQLException.class)
	public void testDeleteThrow() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		foo.id = "stuff";
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection();
		try {
			conn.close();
			dao.delete(foo);
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testDeleteNull() throws Exception {
		BaseDaoImpl<Foo, String> dao = new BaseDaoImpl<Foo, String>(connectionSource, Foo.class) {
		};
		dao.initialize();
		assertEquals(0, dao.delete((Foo) null));
	}

	@Test(expected = IllegalStateException.class)
	public void testDeleteNoInit() throws Exception {
		BaseDaoImpl<Foo, String> dao = new BaseDaoImpl<Foo, String>(Foo.class) {
		};
		dao.setConnectionSource(connectionSource);
		assertEquals(0, dao.delete((Foo) null));
	}

	@Test
	public void testDeleteCollection() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		String id1 = "stuff1";
		foo1.id = id1;
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		String id2 = "stuff2";
		foo2.id = id2;
		assertEquals(1, dao.create(foo2));

		assertNotNull(dao.queryForId(id1));
		assertNotNull(dao.queryForId(id2));

		List<Foo> foos = new ArrayList<Foo>();
		foos.add(foo1);
		foos.add(foo2);

		assertEquals(2, dao.delete(foos));
		assertEquals(0, dao.queryForAll().size());
		assertNull(dao.queryForId(id1));
		assertNull(dao.queryForId(id2));
	}

	@Test(expected = SQLException.class)
	public void testDeleteCollectionThrow() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		foo.id = "stuff";
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection();
		try {
			conn.close();
			List<Foo> foos = new ArrayList<Foo>();
			foos.add(foo);
			dao.delete(foos);
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testDeleteEmptyCollection() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		String id1 = "stuff1";
		foo1.id = id1;
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		String id2 = "stuff2";
		foo2.id = id2;
		assertEquals(1, dao.create(foo2));

		assertNotNull(dao.queryForId(id1));
		assertNotNull(dao.queryForId(id2));

		assertEquals(0, dao.delete(new ArrayList<Foo>()));
		assertEquals(2, dao.queryForAll().size());
		assertNotNull(dao.queryForId(id1));
		assertNotNull(dao.queryForId(id2));
	}

	@Test(expected = IllegalStateException.class)
	public void testDeleteCollectionNoInit() throws Exception {
		BaseDaoImpl<Foo, String> dao = new BaseDaoImpl<Foo, String>(Foo.class) {
		};
		dao.setConnectionSource(connectionSource);
		assertEquals(0, dao.delete((List<Foo>) null));
	}

	@Test
	public void testDeleteIds() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		String id1 = "stuff1";
		foo1.id = id1;
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		String id2 = "stuff2";
		foo2.id = id2;
		assertEquals(1, dao.create(foo2));

		assertNotNull(dao.queryForId(id1));
		assertNotNull(dao.queryForId(id2));

		List<String> ids = new ArrayList<String>();
		ids.add(id1);
		ids.add(id2);
		assertEquals(2, dao.deleteIds(ids));
		assertEquals(0, dao.queryForAll().size());
		assertNull(dao.queryForId(id1));
		assertNull(dao.queryForId(id2));
	}

	@Test(expected = SQLException.class)
	public void testDeleteIdsThrow() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		foo.id = "stuff";
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection();
		try {
			conn.close();
			List<String> foos = new ArrayList<String>();
			foos.add(foo.id);
			dao.deleteIds(foos);
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testDeleteIdsEmpty() throws Exception {
		BaseDaoImpl<Foo, String> dao = new BaseDaoImpl<Foo, String>(connectionSource, Foo.class) {
		};
		dao.initialize();
		List<String> fooList = new ArrayList<String>();
		assertEquals(0, dao.deleteIds(fooList));
	}

	@Test(expected = IllegalStateException.class)
	public void testDeleteIdsNoInit() throws Exception {
		BaseDaoImpl<Foo, String> dao = new BaseDaoImpl<Foo, String>(Foo.class) {
		};
		dao.setConnectionSource(connectionSource);
		assertEquals(0, dao.deleteIds((List<String>) null));
	}

	@Test
	public void testDeletePreparedStatement() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		String id1 = "stuff1";
		foo.id = id1;
		assertEquals(1, dao.create(foo));
		String id2 = "stuff2";
		foo.id = id2;
		assertEquals(1, dao.create(foo));

		assertNotNull(dao.queryForId(id1));
		assertNotNull(dao.queryForId(id2));

		DeleteBuilder<Foo, String> deleteBuilder = dao.deleteBuilder();
		deleteBuilder.where().eq(Foo.ID_COLUMN_NAME, id1);
		assertEquals(1, dao.delete(deleteBuilder.prepare()));

		assertEquals(1, dao.queryForAll().size());
		assertNull(dao.queryForId(id1));
		assertNotNull(dao.queryForId(id2));
	}

	@Test(expected = SQLException.class)
	public void testDeletePreparedThrow() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		String id = "stuff";
		foo.id = id;
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection();
		try {
			conn.close();
			dao.delete(dao.deleteBuilder().prepare());
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testRefresh() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		String id1 = "stuff1";
		foo.id = id1;
		int equal1 = 11312331;
		foo.equal = equal1;
		assertEquals(1, dao.create(foo));
		int equal2 = 312312;

		assertNotNull(dao.queryForId(id1));

		UpdateBuilder<Foo, String> updateBuilder = dao.updateBuilder();
		updateBuilder.updateColumnValue(Foo.EQUAL_COLUMN_NAME, equal2);
		updateBuilder.where().eq(Foo.ID_COLUMN_NAME, id1);
		assertEquals(1, dao.update(updateBuilder.prepare()));

		assertEquals(equal1, foo.equal);
		assertEquals(1, dao.refresh(foo));
		assertEquals(equal2, foo.equal);
	}

	@Test(expected = SQLException.class)
	public void testRefreshThrow() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		foo.id = "stuff";
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection();
		try {
			conn.close();
			dao.refresh(foo);
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testRefreshNull() throws Exception {
		BaseDaoImpl<Foo, String> dao = new BaseDaoImpl<Foo, String>(connectionSource, Foo.class) {
		};
		dao.initialize();
		assertEquals(0, dao.refresh(null));
	}

	@Test(expected = IllegalStateException.class)
	public void testRefreshNoInit() throws Exception {
		BaseDaoImpl<Foo, String> dao = new BaseDaoImpl<Foo, String>(Foo.class) {
		};
		dao.setConnectionSource(connectionSource);
		assertEquals(0, dao.refresh(null));
	}

	@Test
	public void testAnotherConstructor() throws Exception {
		new BaseDaoImpl<Foo, String>(Foo.class) {
		};
	}

	@Test(expected = IllegalStateException.class)
	public void testNoDatabaseType() throws Exception {
		BaseDaoImpl<Foo, String> dao = new BaseDaoImpl<Foo, String>(Foo.class) {
		};
		dao.initialize();
	}

	@Test(expected = IllegalStateException.class)
	public void testQueryForIdNoInit() throws Exception {
		BaseDaoImpl<Foo, String> dao = new BaseDaoImpl<Foo, String>(Foo.class) {
		};
		dao.queryForId("foo");
	}

	@Test
	public void testQueryForFirst() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		String id1 = "stuff1";
		foo1.id = id1;
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		String id2 = "stuff2";
		foo2.id = id2;
		assertEquals(1, dao.create(foo2));

		assertNotNull(dao.queryForId(id1));
		assertNotNull(dao.queryForId(id2));
		assertEquals(2, dao.queryForAll().size());

		Foo foo3 = dao.queryForFirst(dao.queryBuilder().prepare());
		assertNotNull(foo2);
		assertEquals(foo1.id, foo3.id);
	}

	@Test(expected = SQLException.class)
	public void testQueryForFirstThrow() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		String id = "stuff";
		foo.id = id;
		int equal = 21313;
		foo.equal = equal;
		assertEquals(1, dao.create(foo));

		DatabaseConnection conn = connectionSource.getReadWriteConnection();
		try {
			conn.close();
			dao.queryForFirst(dao.queryBuilder().prepare());
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testQueryForFirstNoResults() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		assertNull(dao.queryForFirst(dao.queryBuilder().prepare()));
	}

	@Test(expected = IllegalStateException.class)
	public void testStatementBuilderNoInit() throws Exception {
		BaseDaoImpl<Foo, String> dao = new BaseDaoImpl<Foo, String>(Foo.class) {
		};
		dao.queryBuilder();
	}

	@Test(expected = IllegalStateException.class)
	public void testQueryForFirstNoInit() throws Exception {
		BaseDaoImpl<Foo, String> dao = new BaseDaoImpl<Foo, String>(Foo.class) {
		};
		dao.queryForFirst(null);
	}

	@Test(expected = IllegalStateException.class)
	public void testQueryForPreparedNoInit() throws Exception {
		BaseDaoImpl<Foo, String> dao = new BaseDaoImpl<Foo, String>(Foo.class) {
		};
		dao.query((PreparedQuery<Foo>) null);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testQueryForAllRaw() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		String id1 = "stuff1";
		int equal1 = 1231231232;
		foo1.id = id1;
		foo1.equal = equal1;
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		int equal2 = 1231232;
		String id2 = "stuff2";
		foo2.id = id2;
		foo2.equal = equal2;
		assertEquals(1, dao.create(foo2));

		RawResults rawResults =
				dao.queryForAllRaw("SELECT " + Foo.ID_COLUMN_NAME + "," + Foo.EQUAL_COLUMN_NAME + " FROM FOO");
		List<String[]> resultList = rawResults.getResults();
		assertEquals(2, resultList.size());
		String[] row = resultList.get(0);
		assertEquals(2, row.length);
		assertEquals(foo1.id, row[0]);
		assertEquals(foo1.equal, Integer.parseInt(row[1]));
		row = resultList.get(1);
		assertEquals(2, row.length);
		assertEquals(foo2.id, row[0]);
		assertEquals(foo2.equal, Integer.parseInt(row[1]));
	}

	@Test(expected = IllegalStateException.class)
	public void testQueryForRawNoInit() throws Exception {
		BaseDaoImpl<Foo, String> dao = new BaseDaoImpl<Foo, String>(Foo.class) {
		};
		dao.queryForAllRaw("select * from foo");
	}

	@Test
	public void testObjectToString() throws Exception {
		BaseDaoImpl<Foo, String> dao = new BaseDaoImpl<Foo, String>(connectionSource, Foo.class) {
		};
		dao.initialize();
		Foo foo = new Foo();
		String idStr = "qdqd";
		foo.id = idStr;
		String objStr = dao.objectToString(foo);
		assertTrue(objStr.contains("id=" + idStr));
	}

	@Test(expected = IllegalStateException.class)
	public void testObjectToStringNoInit() throws Exception {
		BaseDaoImpl<Foo, String> dao = new BaseDaoImpl<Foo, String>(Foo.class) {
		};
		dao.setConnectionSource(connectionSource);
		dao.objectToString(new Foo());
	}

	@Test
	public void testObjectsEqual() throws Exception {
		BaseDaoImpl<Foo, String> dao = new BaseDaoImpl<Foo, String>(connectionSource, Foo.class) {
		};
		dao.initialize();
		Foo foo = new Foo();
		foo.id = "qdqd";
		foo.val = 123123;
		Foo bar = new Foo();
		assertTrue(dao.objectsEqual(foo, foo));
		assertFalse(dao.objectsEqual(foo, bar));
		assertFalse(dao.objectsEqual(bar, foo));
		assertTrue(dao.objectsEqual(bar, bar));
		bar.id = "wqdpq";
		bar.val = foo.val;
		assertFalse(dao.objectsEqual(bar, foo));
	}

	@Test(expected = IllegalStateException.class)
	public void testObjectsEqualNoInit() throws Exception {
		BaseDaoImpl<Foo, String> dao = new BaseDaoImpl<Foo, String>(Foo.class) {
		};
		dao.setConnectionSource(connectionSource);
		dao.objectsEqual(new Foo(), new Foo());
	}

	@Test
	public void testIterator() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		String id1 = "stuff1";
		int equal1 = 1231231232;
		foo1.id = id1;
		foo1.equal = equal1;
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		int equal2 = 1231232;
		String id2 = "stuff2";
		foo2.id = id2;
		foo2.equal = equal2;
		assertEquals(1, dao.create(foo2));

		CloseableIterator<Foo> iterator = dao.iterator();
		assertTrue(iterator.hasNext());
		Foo foo3 = iterator.next();
		assertEquals(id1, foo3.id);
		assertTrue(iterator.hasNext());
		foo3 = iterator.next();
		assertEquals(id2, foo3.id);
		assertFalse(iterator.hasNext());
		iterator.close();
	}

	@Test
	public void testIteratorLastClose() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		foo1.id = "stuff1";
		assertEquals(1, dao.create(foo1));

		CloseableIterator<Foo> iterator = dao.iterator();
		assertTrue(iterator.hasNext());
		Foo foo3 = iterator.next();
		assertEquals(foo1.id, foo3.id);
		assertFalse(iterator.hasNext());
		dao.closeLastIterator();
	}

	@Test
	public void testWrappedIterator() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		foo1.id = "stuff1";
		assertEquals(1, dao.create(foo1));

		CloseableWrappedIterable<Foo> wrapped = dao.getWrappedIterable();
		CloseableIterator<Foo> iterator = wrapped.iterator();
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
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		String id = "stuff";
		foo.id = id;
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection();
		try {
			conn.close();
			dao.iterator();
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test(expected = IllegalStateException.class)
	public void testIteratorNoInit() throws Exception {
		BaseDaoImpl<Foo, String> dao = new BaseDaoImpl<Foo, String>(Foo.class) {
		};
		dao.iterator();
	}

	@Test
	public void testIteratorPrepared() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		String id1 = "stuff1";
		int equal1 = 1231231232;
		foo1.id = id1;
		foo1.equal = equal1;
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		int equal2 = 1231232;
		String id2 = "stuff2";
		foo2.id = id2;
		foo2.equal = equal2;
		assertEquals(1, dao.create(foo2));

		QueryBuilder<Foo, String> queryBuilder = dao.queryBuilder();
		queryBuilder.where().eq(Foo.ID_COLUMN_NAME, id1);

		CloseableIterator<Foo> iterator = dao.iterator(queryBuilder.prepare());
		assertTrue(iterator.hasNext());
		Foo foo3 = iterator.next();
		assertEquals(id1, foo3.id);
		assertFalse(iterator.hasNext());
	}

	@Test(expected = SQLException.class)
	public void testIteratorPreparedThrow() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		String id = "stuff";
		foo.id = id;
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection();
		try {
			conn.close();
			dao.iterator(dao.queryBuilder().prepare());
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test(expected = IllegalStateException.class)
	public void testIteratorPreparedNoInit() throws Exception {
		BaseDaoImpl<Foo, String> dao = new BaseDaoImpl<Foo, String>(Foo.class) {
		};
		dao.iterator((PreparedQuery<Foo>) null);
	}

	@Test(expected = IllegalStateException.class)
	public void testIteratorRawNoInit() throws Exception {
		BaseDaoImpl<Foo, String> dao = new BaseDaoImpl<Foo, String>(Foo.class) {
		};
		dao.iteratorRaw("select * from foo");
	}

	@Test
	public void testTableConfig() throws Exception {
		DatabaseTableConfig<Foo> config = DatabaseTableConfig.fromClass(connectionSource, Foo.class);
		BaseDaoImpl<Foo, String> dao = new BaseDaoImpl<Foo, String>(connectionSource, config) {
		};
		assertSame(config, dao.getTableConfig());
	}

	@Test
	public void testSetters() throws Exception {
		DatabaseTableConfig<Foo> config = DatabaseTableConfig.fromClass(connectionSource, Foo.class);
		BaseDaoImpl<Foo, String> dao = new BaseDaoImpl<Foo, String>(Foo.class) {
		};
		dao.setTableConfig(config);
		dao.setConnectionSource(connectionSource);
		assertSame(config, dao.getTableConfig());
	}

	@Test
	@SuppressWarnings("deprecation")
	public void testIteratorRaw() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		String id1 = "stuff1";
		int equal1 = 1231231232;
		foo1.id = id1;
		foo1.equal = equal1;
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		int equal2 = 1231232;
		String id2 = "stuff2";
		foo2.id = id2;
		foo2.equal = equal2;
		assertEquals(1, dao.create(foo2));

		QueryBuilder<Foo, String> queryBuilder = dao.queryBuilder();
		queryBuilder.where().eq(Foo.ID_COLUMN_NAME, id1);

		RawResults iterator =
				dao.iteratorRaw("SELECT " + Foo.ID_COLUMN_NAME + "," + Foo.EQUAL_COLUMN_NAME + " FROM FOO");
		List<String[]> resultList = iterator.getResults();
		assertEquals(2, resultList.size());
		String[] row = resultList.get(0);
		assertEquals(2, row.length);
		assertEquals(foo1.id, row[0]);
		assertEquals(foo1.equal, Integer.parseInt(row[1]));
		row = resultList.get(1);
		assertEquals(2, row.length);
		assertEquals(foo2.id, row[0]);
		assertEquals(foo2.equal, Integer.parseInt(row[1]));
	}

	@SuppressWarnings("deprecation")
	@Test(expected = SQLException.class)
	public void testIteratorRawThrow() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		String id = "stuff";
		foo.id = id;
		int equal = 21313;
		foo.equal = equal;
		assertEquals(1, dao.create(foo));

		DatabaseConnection conn = connectionSource.getReadWriteConnection();
		try {
			conn.close();
			dao.iteratorRaw("SELECT * FROM FOO");
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testQueryRawStrings() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		String id1 = "stuff1";
		int equal1 = 1231231232;
		foo1.id = id1;
		foo1.equal = equal1;
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		int equal2 = 1231232;
		String id2 = "stuff2";
		foo2.id = id2;
		foo2.equal = equal2;
		assertEquals(1, dao.create(foo2));

		QueryBuilder<Foo, String> queryBuilder = dao.queryBuilder();
		queryBuilder.where().eq(Foo.ID_COLUMN_NAME, id1);

		GenericRawResults<String[]> results =
				dao.queryRaw("SELECT " + Foo.ID_COLUMN_NAME + "," + Foo.EQUAL_COLUMN_NAME + " FROM FOO");
		List<String[]> resultList = results.getResults();
		assertEquals(2, resultList.size());
		String[] row = resultList.get(0);
		assertEquals(2, row.length);
		assertEquals(foo1.id, row[0]);
		assertEquals(foo1.equal, Integer.parseInt(row[1]));
		row = resultList.get(1);
		assertEquals(2, row.length);
		assertEquals(foo2.id, row[0]);
		assertEquals(foo2.equal, Integer.parseInt(row[1]));
	}

	@Test(expected = SQLException.class)
	public void testQueryRawStringsThrow() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		String id = "stuff";
		foo.id = id;
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection();
		try {
			conn.close();
			dao.queryRaw("SELECT * FROM FOO");
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testQueryRawStringsArguments() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		String id1 = "stuff1";
		int equal1 = 1231231232;
		foo1.id = id1;
		foo1.equal = equal1;
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		int equal2 = 1231232;
		String id2 = "stuff2";
		foo2.id = id2;
		foo2.equal = equal2;
		assertEquals(1, dao.create(foo2));

		QueryBuilder<Foo, String> queryBuilder = dao.queryBuilder();
		queryBuilder.where().eq(Foo.ID_COLUMN_NAME, id1);

		GenericRawResults<String[]> results =
				dao.queryRaw("SELECT " + Foo.ID_COLUMN_NAME + "," + Foo.EQUAL_COLUMN_NAME + " FROM FOO WHERE "
						+ Foo.ID_COLUMN_NAME + " = ?", id2);
		assertEquals(2, results.getNumberColumns());
		String[] names = results.getColumnNames();
		assertEquals(2, names.length);
		assertEquals(Foo.ID_COLUMN_NAME.toUpperCase(), names[0]);
		assertEquals(Foo.EQUAL_COLUMN_NAME.toUpperCase(), names[1]);
		List<String[]> resultList = results.getResults();
		assertEquals(1, resultList.size());
		String[] row = resultList.get(0);
		assertEquals(2, row.length);
		assertEquals(foo2.id, row[0]);
		assertEquals(foo2.equal, Integer.parseInt(row[1]));
		results.close();
	}

	@Test
	public void testQueryRawObjects() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		String id1 = "stuff1";
		int equal1 = 1231231232;
		foo1.id = id1;
		foo1.equal = equal1;
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		int equal2 = 1231232;
		String id2 = "stuff2";
		foo2.id = id2;
		foo2.equal = equal2;
		assertEquals(1, dao.create(foo2));

		QueryBuilder<Foo, String> queryBuilder = dao.queryBuilder();
		queryBuilder.where().eq(Foo.ID_COLUMN_NAME, id1);

		GenericRawResults<Object[]> results =
				dao.queryRaw("SELECT " + Foo.ID_COLUMN_NAME + "," + Foo.EQUAL_COLUMN_NAME + " FROM FOO",
						new DataType[] { DataType.STRING, DataType.INTEGER });
		List<Object[]> resultList = results.getResults();
		assertEquals(2, resultList.size());
		Object[] row = resultList.get(0);
		assertEquals(2, row.length);
		assertEquals(foo1.id, row[0]);
		assertEquals(foo1.equal, row[1]);
		row = resultList.get(1);
		assertEquals(2, row.length);
		assertEquals(foo2.id, row[0]);
		assertEquals(foo2.equal, row[1]);
	}

	@Test(expected = SQLException.class)
	public void testQueryRawObjectsThrow() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		String id = "stuff";
		foo.id = id;
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection();
		try {
			conn.close();
			dao.queryRaw("SELECT * FROM FOO", new DataType[0]);
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testQueryRawObjectsArguments() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		String id1 = "stuff1";
		int equal1 = 1231231232;
		foo1.id = id1;
		foo1.equal = equal1;
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		int equal2 = 1231232;
		String id2 = "stuff2";
		foo2.id = id2;
		foo2.equal = equal2;
		assertEquals(1, dao.create(foo2));

		QueryBuilder<Foo, String> queryBuilder = dao.queryBuilder();
		queryBuilder.where().eq(Foo.ID_COLUMN_NAME, id1);

		GenericRawResults<Object[]> results =
				dao.queryRaw("SELECT " + Foo.ID_COLUMN_NAME + "," + Foo.EQUAL_COLUMN_NAME + " FROM FOO WHERE "
						+ Foo.ID_COLUMN_NAME + " = ?", new DataType[] { DataType.STRING, DataType.INTEGER }, id2);
		List<Object[]> resultList = results.getResults();
		assertEquals(1, resultList.size());
		Object[] row = resultList.get(0);
		assertEquals(2, row.length);
		assertEquals(foo2.id, row[0]);
		assertEquals(foo2.equal, row[1]);
	}

	@Test
	public void testQueryRawMapped() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		String id1 = "stuff1";
		int equal1 = 1231231232;
		foo1.id = id1;
		foo1.equal = equal1;
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		int equal2 = 1231232;
		String id2 = "stuff2";
		foo2.id = id2;
		foo2.equal = equal2;
		assertEquals(1, dao.create(foo2));

		QueryBuilder<Foo, String> queryBuilder = dao.queryBuilder();
		queryBuilder.where().eq(Foo.ID_COLUMN_NAME, id1);

		GenericRawResults<Foo> results =
				dao.queryRaw("SELECT " + Foo.ID_COLUMN_NAME + "," + Foo.EQUAL_COLUMN_NAME + " FROM FOO",
						new RawRowMapper<Foo>() {
							public Foo mapRow(String[] columnNames, String[] resultColumns) throws SQLException {
								assertEquals(2, columnNames.length);
								assertEquals(2, resultColumns.length);
								Foo foo = new Foo();
								foo.id = resultColumns[0];
								foo.equal = Integer.parseInt(resultColumns[1]);
								return foo;
							}
						});
		List<Foo> resultList = results.getResults();
		assertEquals(2, resultList.size());
		assertEquals(foo1.id, resultList.get(0).id);
		assertEquals(foo1.equal, resultList.get(0).equal);
		assertEquals(foo2.id, resultList.get(1).id);
		assertEquals(foo2.equal, resultList.get(1).equal);
	}

	@Test(expected = SQLException.class)
	public void testQueryRawMappedThrow() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		String id = "stuff";
		foo.id = id;
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection();
		try {
			conn.close();
			dao.queryRaw("SELECT * FROM FOO", new RawRowMapper<Foo>() {
				public Foo mapRow(String[] columnNames, String[] resultColumns) throws SQLException {
					return null;
				}
			});
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testQueryRawMappedArguments() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		String id1 = "stuff1";
		int equal1 = 1231231232;
		foo1.id = id1;
		foo1.equal = equal1;
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		int equal2 = 1231232;
		String id2 = "stuff2";
		foo2.id = id2;
		foo2.equal = equal2;
		assertEquals(1, dao.create(foo2));

		QueryBuilder<Foo, String> queryBuilder = dao.queryBuilder();
		queryBuilder.where().eq(Foo.ID_COLUMN_NAME, id1);

		GenericRawResults<Foo> results =
				dao.queryRaw("SELECT " + Foo.ID_COLUMN_NAME + "," + Foo.EQUAL_COLUMN_NAME + " FROM FOO WHERE "
						+ Foo.ID_COLUMN_NAME + " = ?", new RawRowMapper<Foo>() {
					public Foo mapRow(String[] columnNames, String[] resultColumns) throws SQLException {
						assertEquals(2, columnNames.length);
						assertEquals(2, resultColumns.length);
						Foo foo = new Foo();
						foo.id = resultColumns[0];
						foo.equal = Integer.parseInt(resultColumns[1]);
						return foo;
					}
				}, id2);
		List<Foo> resultList = results.getResults();
		assertEquals(1, resultList.size());
		assertEquals(foo2.id, resultList.get(0).id);
		assertEquals(foo2.equal, resultList.get(0).equal);
	}

	@Test
	public void testIsUpdatable() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, false);
		assertTrue(dao.isUpdatable());
	}

	@Test
	public void testIsTableExists() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, false);
		assertFalse(dao.isTableExists());
		TableUtils.createTable(connectionSource, Foo.class);
		assertTrue(dao.isTableExists());
		TableUtils.dropTable(connectionSource, Foo.class, true);
		assertFalse(dao.isTableExists());
	}

	@Test(expected = SQLException.class)
	public void testIsTableExistsThrow() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		String id = "stuff";
		foo.id = id;
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection();
		try {
			conn.close();
			dao.isTableExists();
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test(expected = IllegalStateException.class)
	public void testBadConnectionSource() throws Exception {
		ConnectionSource cs = createMock(ConnectionSource.class);
		new BaseDaoImpl<Foo, String>(cs, Foo.class) {
		};
	}

	@Test
	public void testUpdateBuilder() throws Exception {
		BaseDaoImpl<Foo, String> dao = new BaseDaoImpl<Foo, String>(connectionSource, Foo.class) {
		};
		dao.updateBuilder();
	}

	@Test
	public void testDeleteBuilder() throws Exception {
		BaseDaoImpl<Foo, String> dao = new BaseDaoImpl<Foo, String>(connectionSource, Foo.class) {
		};
		dao.deleteBuilder();
	}

	@Test
	public void testDataClass() throws Exception {
		BaseDaoImpl<Foo, String> dao = new BaseDaoImpl<Foo, String>(Foo.class) {
		};
		assertEquals(Foo.class, dao.getDataClass());
	}

	@Test
	public void testUpdateRaw() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		String id1 = "stuff1";
		foo1.id = id1;
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		String id2 = "stuff2";
		foo2.id = id2;
		assertEquals(1, dao.create(foo2));

		assertEquals(2, dao.queryForAll().size());
		dao.updateRaw("DELETE FROM FOO WHERE " + Foo.ID_COLUMN_NAME + " = ?", id1);
		assertEquals(1, dao.queryForAll().size());
	}

	@Test(expected = SQLException.class)
	public void testUpdateRawThrow() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		String id = "stuff";
		foo.id = id;
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection();
		try {
			conn.close();
			dao.updateRaw("DELETE FROM FOO");
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testExecuteRaw() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		String id1 = "stuff1";
		foo1.id = id1;
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		String id2 = "stuff2";
		foo2.id = id2;
		assertEquals(1, dao.create(foo2));

		assertEquals(2, dao.queryForAll().size());
		dao.executeRaw("TRUNCATE TABLE FOO");
		assertEquals(0, dao.queryForAll().size());
	}

	@Test(expected = SQLException.class)
	public void testExecuteRawThrow() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		String id = "stuff";
		foo.id = id;
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection();
		try {
			conn.close();
			dao.executeRaw("TRUNCATE TABLE FOO");
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testExtractId() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		String id1 = "stuff1";
		foo1.id = id1;
		assertEquals(id1, dao.extractId(foo1));
	}

	@Test
	public void testFindForeign() throws Exception {
		Dao<Foreign, String> dao = createDao(Foreign.class, false);
		FieldType fieldType = dao.findForeignFieldType(Foo.class);
		assertNotNull(fieldType);
		assertEquals("foo", fieldType.getFieldName());

		// this should be none
		fieldType = dao.findForeignFieldType(Foreign.class);
		assertNull(fieldType);
	}

	@Test
	public void testCallBatch() throws Exception {
		final Dao<Foo, String> dao = createDao(Foo.class, true);
		final Foo foo1 = new Foo();
		String id1 = "stuff1";
		foo1.id = id1;
		assertEquals(0, dao.queryForAll().size());

		// this should be none
		dao.callBatchTasks(new Callable<Void>() {
			public Void call() throws Exception {
				assertEquals(1, dao.create(foo1));
				return null;
			}
		});

		assertEquals(1, dao.queryForAll().size());
	}

	@Test(expected = Exception.class)
	public void testCallBatchThrow() throws Exception {
		final Dao<Foo, String> dao = createDao(Foo.class, true);
		final Foo foo1 = new Foo();
		String id1 = "stuff1";
		foo1.id = id1;
		assertEquals(0, dao.queryForAll().size());

		// this should be none
		dao.callBatchTasks(new Callable<Void>() {
			public Void call() throws Exception {
				throw new Exception("for the hell of it");
			}
		});
	}

	@Test
	public void testForeignNull() throws Exception {
		Dao<Foreign, Integer> dao = createDao(Foreign.class, true);
		Foreign foreign = new Foreign();
		foreign.foo = null;
		assertEquals(1, dao.create(foreign));
		Foreign foreign2 = dao.queryForId(foreign.id);
		assertNotNull(foreign2);
		assertNull(foreign2.foo);
	}

	@Test
	public void testForeign() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		Dao<Foreign, Integer> foreignDao = createDao(Foreign.class, true);

		Foo foo = new Foo();
		foo.id = "jpoejfew";
		int val = 6389;
		foo.val = val;
		assertEquals(1, fooDao.create(foo));

		Foreign foreign = new Foreign();
		foreign.foo = foo;
		assertEquals(1, foreignDao.create(foreign));

		Foreign foreign2 = foreignDao.queryForId(foreign.id);
		assertNotNull(foreign2);
		assertNotNull(foreign2.foo.id);
		assertEquals(foo.id, foreign2.foo.id);
		assertEquals(0, foreign2.foo.val);

		assertEquals(1, fooDao.refresh(foreign2.foo));
		assertEquals(val, foreign2.foo.val);
	}

	@Test
	public void testForeignAutoRefresh() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		Dao<ForeignAutoRefresh, Integer> foreignDao = createDao(ForeignAutoRefresh.class, true);

		Foo foo = new Foo();
		foo.id = "jpoejfew";
		int val = 6389;
		foo.val = val;
		assertEquals(1, fooDao.create(foo));

		ForeignAutoRefresh foreign = new ForeignAutoRefresh();
		foreign.foo = foo;
		assertEquals(1, foreignDao.create(foreign));

		ForeignAutoRefresh foreign2 = foreignDao.queryForId(foreign.id);
		assertNotNull(foreign2);
		assertNotNull(foreign2.foo.id);
		assertEquals(foo.id, foreign2.foo.id);
		assertEquals(val, foreign2.foo.val);
	}

	@Test(expected = SQLException.class)
	public void testForeignCantBeNull() throws Exception {
		Dao<ForeignNotNull, Integer> dao = createDao(ForeignNotNull.class, true);
		ForeignNotNull foreign = new ForeignNotNull();
		foreign.foo = null;
		dao.create(foreign);
	}

	/**
	 * Test inserting an object either as a generated-id or just an id using another object. This really isn't testing
	 * any capabilities since it is really the underlying database which either allows or throws with this. But it's an
	 * interesting test of a question asked by a user on stackoverflow.com.
	 */
	@Test
	public void testGenIdVersusJustId() throws Exception {
		Dao<One, Integer> oneDao = createDao(One.class, true);
		Dao<Two, Integer> twoDao = createDao(Two.class, false);

		One one = new One();
		String oneStuff = "efweggwgee";
		one.stuff = oneStuff;
		assertEquals(1, oneDao.create(one));
		assertNotNull(oneDao.queryForId(one.id));
		assertEquals(1, one.id);
		assertEquals(1, oneDao.queryForAll().size());

		Two two = new Two();
		String twoStuff = "efweggwefdggwgee";
		two.id = one.id + 1;
		two.stuff = twoStuff;
		assertEquals(1, twoDao.create(two));
		assertNotNull(oneDao.queryForId(one.id));
		assertNotNull(oneDao.queryForId(two.id));
		assertEquals(2, two.id);
		assertEquals(2, oneDao.queryForAll().size());

		One anonterOne = new One();
		String anonterOneOneStuff = "e24fweggwgee";
		anonterOne.stuff = anonterOneOneStuff;
		assertEquals(1, oneDao.create(anonterOne));
		assertNotNull(oneDao.queryForId(one.id));
		assertNotNull(oneDao.queryForId(two.id));
		assertNotNull(oneDao.queryForId(anonterOne.id));
		assertEquals(3, anonterOne.id);
		assertEquals(3, oneDao.queryForAll().size());
	}

	@Test
	public void testUuidInsertQuery() throws Exception {
		Dao<UuidGeneratedId, UUID> dao = createDao(UuidGeneratedId.class, true);
		UuidGeneratedId uuid = new UuidGeneratedId();
		String stuff = "fopewfjefjwgw";
		uuid.stuff = stuff;
		assertEquals(1, dao.create(uuid));
		UuidGeneratedId uuid2 = dao.queryForId(uuid.id);
		assertNotNull(uuid2);
		assertEquals(uuid.id, uuid2.id);
		assertEquals(stuff, uuid2.stuff);
	}

	@Test
	public void testCountOf() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		assertEquals(0, dao.countOf());
		Foo foo = new Foo();
		foo.id = "1";
		assertEquals(1, dao.create(foo));
		assertEquals(1, dao.countOf());
		foo.id = "2";
		assertEquals(1, dao.create(foo));
		assertEquals(2, dao.countOf());
	}

	@Test
	public void testQueryForEq() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		assertEquals(0, dao.countOf());
		Foo foo = new Foo();
		String id = "1";
		foo.id = id;
		int val = 1231231;
		foo.val = val;
		assertEquals(1, dao.create(foo));
		foo.id = "not " + id;
		foo.val = val + 1;
		assertEquals(1, dao.create(foo));

		List<Foo> results = dao.queryForEq(Foo.VAL_COLUMN_NAME, val);
		assertEquals(1, results.size());
		assertEquals(id, results.get(0).id);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testUseOfAndMany() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		assertEquals(0, dao.countOf());
		Foo foo = new Foo();
		String id = "1";
		foo.id = id;
		int val = 1231231;
		foo.val = val;
		assertEquals(1, dao.create(foo));
		String notId = "not " + id;
		foo.id = notId;
		foo.val = val + 1;
		assertEquals(1, dao.create(foo));

		Where<Foo, String> where = dao.queryBuilder().where();
		where.and(where.eq(Foo.VAL_COLUMN_NAME, val), where.eq(Foo.ID_COLUMN_NAME, id));

		List<Foo> results = where.query();
		assertEquals(1, results.size());
		assertEquals(id, results.get(0).id);

		// this should match none
		where.clear();
		where.and(where.eq(Foo.ID_COLUMN_NAME, id), where.eq(Foo.ID_COLUMN_NAME, notId),
				where.eq(Foo.VAL_COLUMN_NAME, val), where.eq(Foo.VAL_COLUMN_NAME, val + 1));
		results = where.query();
		assertEquals(0, results.size());
	}

	@Test
	public void testUseOfAndInt() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		assertEquals(0, dao.countOf());
		Foo foo = new Foo();
		String id = "1";
		foo.id = id;
		int val = 1231231;
		foo.val = val;
		assertEquals(1, dao.create(foo));
		String notId = "not " + id;
		foo.id = notId;
		foo.val = val + 1;
		assertEquals(1, dao.create(foo));

		Where<Foo, String> where = dao.queryBuilder().where();
		where.eq(Foo.VAL_COLUMN_NAME, val);
		where.eq(Foo.ID_COLUMN_NAME, id);
		where.and(2);

		List<Foo> results = where.query();
		assertEquals(1, results.size());
		assertEquals(id, results.get(0).id);

		// this should match none
		where.clear();
		where.eq(Foo.ID_COLUMN_NAME, id);
		where.eq(Foo.ID_COLUMN_NAME, notId);
		where.eq(Foo.VAL_COLUMN_NAME, val);
		where.eq(Foo.VAL_COLUMN_NAME, val + 1);
		where.and(4);

		results = where.query();
		assertEquals(0, results.size());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testUseOfOrMany() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		assertEquals(0, dao.countOf());
		Foo foo = new Foo();
		String id = "1";
		foo.id = id;
		int val = 1231231;
		foo.val = val;
		assertEquals(1, dao.create(foo));
		String notId = "not " + id;
		foo.id = notId;
		foo.val = val + 1;
		assertEquals(1, dao.create(foo));

		Where<Foo, String> where = dao.queryBuilder().where();
		where.or(where.eq(Foo.ID_COLUMN_NAME, id), where.eq(Foo.ID_COLUMN_NAME, notId),
				where.eq(Foo.VAL_COLUMN_NAME, val), where.eq(Foo.VAL_COLUMN_NAME, val + 1));

		List<Foo> results = where.query();
		assertEquals(2, results.size());
		assertEquals(id, results.get(0).id);
		assertEquals(notId, results.get(1).id);
	}

	@Test
	public void testUseOfOrInt() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		assertEquals(0, dao.countOf());
		Foo foo = new Foo();
		String id = "1";
		foo.id = id;
		int val = 1231231;
		foo.val = val;
		assertEquals(1, dao.create(foo));
		String notId = "not " + id;
		foo.id = notId;
		foo.val = val + 1;
		assertEquals(1, dao.create(foo));

		Where<Foo, String> where = dao.queryBuilder().where();
		where.eq(Foo.ID_COLUMN_NAME, id);
		where.eq(Foo.ID_COLUMN_NAME, notId);
		where.eq(Foo.VAL_COLUMN_NAME, val);
		where.eq(Foo.VAL_COLUMN_NAME, val + 1);
		where.or(4);

		List<Foo> results = where.query();
		assertEquals(2, results.size());
		assertEquals(id, results.get(0).id);
		assertEquals(notId, results.get(1).id);
	}

	@Test
	public void testQueryForMatching() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		assertEquals(0, dao.countOf());
		Foo foo = new Foo();
		String id = "1";
		foo.id = id;
		int val = 1231231;
		foo.val = val;
		assertEquals(1, dao.create(foo));
		String notId = "not " + id;
		foo.id = notId;
		foo.val = val + 1;
		assertEquals(1, dao.create(foo));

		Foo match = new Foo();
		match.val = val;
		List<Foo> results = dao.queryForMatching(match);
		assertEquals(1, results.size());
		assertEquals(id, results.get(0).id);

		match = new Foo();
		match.id = notId;
		match.val = val;
		results = dao.queryForMatching(match);
		assertEquals(0, results.size());
	}

	@Test
	public void testQueryForMatchingNoFields() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		assertEquals(0, dao.countOf());
		Foo foo = new Foo();
		String id = "1";
		foo.id = id;
		int val = 1231231;
		foo.val = val;
		assertEquals(1, dao.create(foo));

		Foo match = new Foo();
		List<Foo> results = dao.queryForMatching(match);
		assertEquals(0, results.size());
	}

	@Test
	public void testQueryForFieldValues() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		assertEquals(0, dao.countOf());
		Foo foo = new Foo();
		String id = "1";
		foo.id = id;
		int val = 1231231;
		foo.val = val;
		assertEquals(1, dao.create(foo));
		String notId = "not " + id;
		foo.id = notId;
		foo.val = val + 1;
		assertEquals(1, dao.create(foo));

		Map<String, Object> fieldValues = new HashMap<String, Object>();
		fieldValues.put(Foo.VAL_COLUMN_NAME, val);
		List<Foo> results = dao.queryForFieldValues(fieldValues);
		assertEquals(1, results.size());
		assertEquals(id, results.get(0).id);

		fieldValues.put(Foo.ID_COLUMN_NAME, notId);
		fieldValues.put(Foo.VAL_COLUMN_NAME, val);
		results = dao.queryForFieldValues(fieldValues);
		assertEquals(0, results.size());
	}

	@Test
	public void testQueryForFieldValuesEmpty() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		assertEquals(0, dao.countOf());
		Foo foo = new Foo();
		String id = "1";
		foo.id = id;
		int val = 1231231;
		foo.val = val;
		assertEquals(1, dao.create(foo));

		Map<String, Object> fieldValues = new HashMap<String, Object>();
		List<Foo> results = dao.queryForFieldValues(fieldValues);
		assertEquals(0, results.size());
	}

	/**
	 * A little test of executeRaw that sets up a H2 trigger.
	 */
	@Test
	public void testExecuteRawTrigger() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		dao.executeRaw("CREATE TRIGGER foo_trigger AFTER INSERT ON foo\n" + "FOR EACH ROW CALL " + "\""
				+ ExampleH2Trigger.class.getName() + "\"");

		Foo foo = new Foo();
		foo.id = "1";
		assertEquals(0, ExampleH2Trigger.callC);
		assertEquals(1, dao.create(foo));
		assertEquals(1, ExampleH2Trigger.callC);
		foo.id = "2";
		assertEquals(1, dao.create(foo));
		assertEquals(2, ExampleH2Trigger.callC);
	}

	/**
	 * Example of a H2 trigger.
	 */
	public static class ExampleH2Trigger implements Trigger {
		static int callC = 0;
		public void init(Connection conn, String schemaName, String triggerName, String tableName, boolean before,
				int type) {
			// noop
		}
		public void fire(Connection conn, Object[] oldRow, Object[] newRow) throws SQLException {
			callC++;
		}
	}

	@Test
	public void testUnique() throws Exception {
		Dao<Unique, Long> dao = createDao(Unique.class, true);
		String stuff = "this doesn't need to be unique";
		String uniqueStuff = "this needs to be unique";
		Unique unique = new Unique();
		unique.stuff = stuff;
		unique.uniqueStuff = uniqueStuff;
		assertEquals(1, dao.create(unique));
		// can't create it twice with the same stuff which needs to be unique
		unique = new Unique();
		unique.stuff = stuff;
		assertEquals(1, dao.create(unique));
		unique = new Unique();
		unique.uniqueStuff = uniqueStuff;
		try {
			dao.create(unique);
			fail("Should have thrown");
		} catch (SQLException e) {
			// expected
			return;
		}
	}

	@Test
	public void testMultipleUnique() throws Exception {
		Dao<DoubleUnique, Long> dao = createDao(DoubleUnique.class, true);
		String stuff = "this doesn't need to be unique";
		String uniqueStuff = "this needs to be unique";
		DoubleUnique unique = new DoubleUnique();
		unique.stuff = stuff;
		unique.uniqueStuff = uniqueStuff;
		assertEquals(1, dao.create(unique));
		// can't create it twice with the same stuff which needs to be unique
		unique = new DoubleUnique();
		unique.stuff = stuff;
		try {
			// either one field can't be unique
			dao.create(unique);
			fail("Should have thrown");
		} catch (SQLException e) {
			// expected
		}
		unique = new DoubleUnique();
		unique.uniqueStuff = uniqueStuff;
		try {
			// or the other field can't be unique
			dao.create(unique);
			fail("Should have thrown");
		} catch (SQLException e) {
			// expected
		}
		unique = new DoubleUnique();
		unique.stuff = stuff;
		unique.uniqueStuff = uniqueStuff;
		try {
			// nor _both_ fields can't be unique
			dao.create(unique);
			fail("Should have thrown");
		} catch (SQLException e) {
			// expected
		}
	}

	@Test
	public void testMultipleUniqueCreateDrop() throws Exception {
		TableUtils.dropTable(connectionSource, DoubleUnique.class, true);
		TableUtils.createTable(connectionSource, DoubleUnique.class);
		TableUtils.dropTable(connectionSource, DoubleUnique.class, false);
		TableUtils.createTable(connectionSource, DoubleUnique.class);
		TableUtils.dropTable(connectionSource, DoubleUnique.class, false);
	}

	@Test
	public void testMultipleUniqueCombo() throws Exception {
		Dao<DoubleUniqueCombo, Long> dao = createDao(DoubleUniqueCombo.class, true);
		String stuff = "this doesn't need to be unique";
		String uniqueStuff = "this needs to be unique";
		DoubleUniqueCombo unique = new DoubleUniqueCombo();
		unique.stuff = stuff;
		unique.uniqueStuff = uniqueStuff;
		assertEquals(1, dao.create(unique));
		unique = new DoubleUniqueCombo();
		unique.stuff = stuff;
		assertEquals(1, dao.create(unique));
		unique = new DoubleUniqueCombo();
		unique.uniqueStuff = uniqueStuff;
		assertEquals(1, dao.create(unique));
		unique = new DoubleUniqueCombo();
		unique.stuff = stuff;
		unique.uniqueStuff = uniqueStuff;
		try {
			// can't create it twice with both fields
			dao.create(unique);
			fail("Should have thrown");
		} catch (SQLException e) {
			// expected
			return;
		}
	}

	@Test
	public void testForeignCollectionAutoRefresh() throws Exception {
		// this got a stack overflow error
		createDao(ForeignCollectionAutoRefresh.class, false);
	}

	/* ============================================================================================== */

	protected static class ForeignNotNull {
		@DatabaseField(generatedId = true)
		public int id;
		@DatabaseField(foreign = true, canBeNull = false)
		public Foo foo;
		public ForeignNotNull() {
		}
	}

	@DatabaseTable(tableName = "oneandtwo")
	protected static class One {
		@DatabaseField(generatedId = true)
		public int id;
		@DatabaseField
		public String stuff;
		public One() {
		}
	}

	@DatabaseTable(tableName = "oneandtwo")
	protected static class Two {
		@DatabaseField(id = true)
		public int id;
		@DatabaseField
		public String stuff;
		public Two() {
		}
	}

	protected static class UuidGeneratedId {
		@DatabaseField(generatedId = true)
		public UUID id;
		@DatabaseField
		public String stuff;
		public UuidGeneratedId() {
		}
	}

	protected static class ForeignAutoRefresh {
		@DatabaseField(generatedId = true)
		public int id;
		@DatabaseField(foreign = true, foreignAutoRefresh = true)
		public Foo foo;
		public ForeignAutoRefresh() {
		}
	}

	protected static class ForeignAutoRefresh2 {
		public int id;
		@DatabaseField(foreign = true, foreignAutoRefresh = true)
		public ForeignCollectionAutoRefresh foo;
		public ForeignAutoRefresh2() {
		}
	}

	protected static class ForeignCollectionAutoRefresh {
		@DatabaseField(generatedId = true)
		public int id;
		@ForeignCollectionField
		public ForeignCollection<ForeignAutoRefresh2> foreignAutoRefresh;
		public ForeignCollectionAutoRefresh() {
		}
	}

	protected static class Unique {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		String stuff;
		@DatabaseField(unique = true)
		String uniqueStuff;
	}

	protected static class DoubleUnique {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(unique = true)
		String stuff;
		@DatabaseField(unique = true)
		String uniqueStuff;
	}

	protected static class SingleUniqueCombo {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		String stuff;
		@DatabaseField(uniqueCombo = true)
		String uniqueStuff;
	}

	protected static class DoubleUniqueCombo {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(uniqueCombo = true)
		String stuff;
		@DatabaseField(uniqueCombo = true)
		String uniqueStuff;
	}
}
