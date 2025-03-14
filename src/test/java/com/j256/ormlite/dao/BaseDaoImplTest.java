package com.j256.ormlite.dao;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.h2.api.Trigger;
import org.junit.jupiter.api.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.Dao.CreateOrUpdateStatus;
import com.j256.ormlite.dao.Dao.DaoObserver;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.misc.Supplier;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.ObjectFactory;
import com.j256.ormlite.table.TableUtils;

public class BaseDaoImplTest extends BaseCoreTest {

	@Test
	public void testDoubleInitialize() throws Exception {
		BaseDaoImpl<Foo, Integer> dao = new BaseDaoImpl<Foo, Integer>(connectionSource, Foo.class) {
		};
		dao.initialize();
		// this shouldn't barf
		dao.initialize();
	}

	@Test
	public void testInitNoConnectionSource() throws Exception {
		BaseDaoImpl<Foo, Integer> dao = new BaseDaoImpl<Foo, Integer>(Foo.class) {
		};
		assertThrowsExactly(IllegalStateException.class, () -> {
			dao.initialize();
		});
	}

	@Test
	public void testCreate() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		int equal = 21313;
		foo.equal = equal;
		assertEquals(1, dao.create(foo));
		Foo result = dao.queryForId(foo.id);
		assertNotNull(result);
		assertEquals(equal, result.equal);
		// check query-for-first
		result = dao.queryForFirst();
		assertNotNull(result);
		assertEquals(equal, result.equal);
	}

	@Test
	public void testQueryForIdThrow() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		int equal = 21313;
		foo.equal = equal;
		assertEquals(1, dao.create(foo));

		DatabaseConnection conn = connectionSource.getReadWriteConnection(FOO_TABLE_NAME);
		try {
			conn.close();
			assertThrows(SQLException.class, () -> {
				dao.queryForId(foo.id);
			});
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testQueryPrepared() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		assertEquals(1, dao.create(foo2));

		Where<Foo, Integer> qb = dao.queryBuilder().where().eq(Foo.ID_COLUMN_NAME, foo2.id);
		List<Foo> results = dao.query(qb.prepare());
		assertEquals(1, results.size());
		assertEquals(foo2.id, results.get(0).id);
	}

	@Test
	public void testIterator() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		assertEquals(1, dao.create(foo2));

		try (CloseableIterator<Foo> iterator = dao.iterator();) {
			assertTrue(iterator.hasNext());
			assertNotNull(iterator.next());
			assertTrue(iterator.hasNext());
			assertNotNull(iterator.next());
			assertFalse(iterator.hasNext());
		}

		try (CloseableIterator<Foo> iterator = dao.closeableIterator();) {
			assertTrue(iterator.hasNext());
			assertNotNull(iterator.next());
			assertTrue(iterator.hasNext());
			assertNotNull(iterator.next());
			assertFalse(iterator.hasNext());
		}

		try (CloseableWrappedIterable<Foo> iterable = dao.getWrappedIterable();) {
			Iterator<Foo> iter = iterable.iterator();
			assertTrue(iter.hasNext());
			assertNotNull(iter.next());
			assertTrue(iter.hasNext());
			assertNotNull(iter.next());
			assertFalse(iter.hasNext());
		}

		// wrapped iterable with prepared query
		PreparedQuery<Foo> pq = dao.queryBuilder().where().eq(Foo.ID_COLUMN_NAME, foo2.id).prepare();
		try (CloseableWrappedIterable<Foo> iterable = dao.getWrappedIterable(pq);) {
			Iterator<Foo> iter = iterable.iterator();
			assertTrue(iter.hasNext());
			assertNotNull(iter.next());
			assertFalse(iter.hasNext());
		}

		// wrapped iterable with prepared query
		try (CloseableIterator<Foo> iterator = dao.iterator(pq);) {
			assertTrue(iterator.hasNext());
			assertNotNull(iterator.next());
			assertFalse(iterator.hasNext());
		}

		dao.closeLastIterator();
		dao.closeLastIterator();
	}

	@Test
	public void testCreateThrow() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		DatabaseConnection conn = connectionSource.getReadWriteConnection(FOO_TABLE_NAME);
		try {
			conn.close();
			assertThrowsExactly(SQLException.class, () -> {
				dao.create(foo);
			});
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testCreateNull() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		assertEquals(0, dao.create((Foo) null));
	}

	@Test
	public void testCreateNoInit() throws Exception {
		BaseDaoImpl<Foo, Integer> dao = new BaseDaoImpl<Foo, Integer>(Foo.class) {
		};
		dao.setConnectionSource(connectionSource);
		assertThrowsExactly(IllegalStateException.class, () -> {
			dao.create((Foo) null);
		});
	}

	@Test
	public void testUpdate() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));
		foo.equal = 1;
		assertEquals(1, dao.update(foo));
	}

	@Test
	public void testUpdateThrow() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection(FOO_TABLE_NAME);
		try {
			conn.close();
			assertThrowsExactly(SQLException.class, () -> {
				dao.update(foo);
			});
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testUpdateNull() throws Exception {
		BaseDaoImpl<Foo, Integer> dao = new BaseDaoImpl<Foo, Integer>(connectionSource, Foo.class) {
		};
		dao.initialize();
		assertEquals(0, dao.update((Foo) null));
	}

	@Test
	public void testUpdateNoInit() throws Exception {
		BaseDaoImpl<Foo, Integer> dao = new BaseDaoImpl<Foo, Integer>(Foo.class) {
		};
		dao.setConnectionSource(connectionSource);
		assertThrowsExactly(IllegalStateException.class, () -> {
			dao.update((Foo) null);
		});
	}

	@Test
	public void testUpdateId() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));
		int id = foo.id;
		assertNotNull(dao.queryForId(id));
		assertNull(dao.queryForId(id + 1));
		assertEquals(1, dao.updateId(foo, id + 1));
		assertNull(dao.queryForId(id));
		assertNotNull(dao.queryForId(id + 1));
	}

	@Test
	public void testUpdateIdThrow() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection(FOO_TABLE_NAME);
		try {
			// close connection
			conn.close();
			assertThrowsExactly(SQLException.class, () -> {
				dao.updateId(foo, foo.id + 1);
			});
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testUpdateIdNull() throws Exception {
		BaseDaoImpl<Foo, Integer> dao = new BaseDaoImpl<Foo, Integer>(connectionSource, Foo.class) {
		};
		dao.initialize();
		assertEquals(0, dao.updateId(null, null));
	}

	@Test
	public void testUpdateIdNoInit() throws Exception {
		BaseDaoImpl<Foo, Integer> dao = new BaseDaoImpl<Foo, Integer>(Foo.class) {
		};
		dao.setConnectionSource(connectionSource);
		assertThrowsExactly(IllegalStateException.class, () -> {
			dao.updateId(null, null);
		});
	}

	@Test
	public void testUpdatePrepared() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		assertEquals(1, dao.create(foo2));
		int id3 = foo2.id + 1;

		assertNotNull(dao.queryForId(foo1.id));
		assertNotNull(dao.queryForId(foo2.id));
		assertNull(dao.queryForId(id3));

		UpdateBuilder<Foo, Integer> updateBuilder = dao.updateBuilder();
		updateBuilder.updateColumnValue(Foo.ID_COLUMN_NAME, id3);
		updateBuilder.where().eq(Foo.ID_COLUMN_NAME, foo1.id);
		assertEquals(1, dao.update(updateBuilder.prepare()));

		assertNull(dao.queryForId(foo1.id));
		assertNotNull(dao.queryForId(foo2.id));
		assertNotNull(dao.queryForId(id3));
		assertEquals(2, dao.queryForAll().size());
	}

	@Test
	public void testUpdatePreparedThrow() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection(FOO_TABLE_NAME);
		try {
			conn.close();
			UpdateBuilder<Foo, Integer> ub = dao.updateBuilder();
			ub.updateColumnValue(Foo.EQUAL_COLUMN_NAME, 1);
			assertThrows(SQLException.class, () -> {
				dao.update(ub.prepare());
			});
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testDelete() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));
		assertNotNull(dao.queryForId(foo.id));
		assertEquals(1, dao.delete(foo));
		assertNull(dao.queryForId(foo.id));
		assertEquals(0, dao.queryForAll().size());
	}

	@Test
	public void testDeleteThrow() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection(FOO_TABLE_NAME);
		try {
			conn.close();
			assertThrowsExactly(SQLException.class, () -> {
				dao.delete(foo);
			});
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testDeleteNull() throws Exception {
		BaseDaoImpl<Foo, Integer> dao = new BaseDaoImpl<Foo, Integer>(connectionSource, Foo.class) {
		};
		dao.initialize();
		assertEquals(0, dao.delete((Foo) null));
	}

	@Test
	public void testDeleteNoInit() throws Exception {
		BaseDaoImpl<Foo, Integer> dao = new BaseDaoImpl<Foo, Integer>(Foo.class) {
		};
		dao.setConnectionSource(connectionSource);
		assertThrowsExactly(IllegalStateException.class, () -> {
			dao.delete((Foo) null);
		});
	}

	@Test
	public void testDeleteById() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));
		assertNotNull(dao.queryForId(foo.id));
		assertEquals(1, dao.deleteById(foo.id));
		assertNull(dao.queryForId(foo.id));
		assertEquals(0, dao.queryForAll().size());
	}

	@Test
	public void testDeleteByIdThrow() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection(FOO_TABLE_NAME);
		try {
			conn.close();
			assertThrowsExactly(SQLException.class, () -> {
				dao.deleteById(foo.id);
			});
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testDeleteByIdNull() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		assertEquals(0, dao.deleteById(null));
	}

	@Test
	public void testDeleteCollection() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		assertEquals(1, dao.create(foo2));

		assertNotNull(dao.queryForId(foo1.id));
		assertNotNull(dao.queryForId(foo2.id));

		List<Foo> foos = new ArrayList<Foo>();
		foos.add(foo1);
		foos.add(foo2);

		assertEquals(2, dao.delete(foos));
		assertEquals(0, dao.queryForAll().size());
		assertNull(dao.queryForId(foo1.id));
		assertNull(dao.queryForId(foo2.id));
	}

	@Test
	public void testDeleteCollectionThrow() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection(FOO_TABLE_NAME);
		try {
			conn.close();
			List<Foo> foos = new ArrayList<Foo>();
			foos.add(foo);
			assertThrowsExactly(SQLException.class, () -> {
				dao.delete(foos);
			});
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testDeleteEmptyCollection() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		assertEquals(1, dao.create(foo2));

		assertNotNull(dao.queryForId(foo1.id));
		assertNotNull(dao.queryForId(foo2.id));

		assertEquals(0, dao.delete(new ArrayList<Foo>()));
		assertEquals(2, dao.queryForAll().size());
		assertNotNull(dao.queryForId(foo1.id));
		assertNotNull(dao.queryForId(foo2.id));
	}

	@Test
	public void testDeleteCollectionNoInit() throws Exception {
		BaseDaoImpl<Foo, Integer> dao = new BaseDaoImpl<Foo, Integer>(Foo.class) {
		};
		dao.setConnectionSource(connectionSource);
		assertThrowsExactly(IllegalStateException.class, () -> {
			dao.delete((List<Foo>) null);
		});
	}

	@Test
	public void testDeleteIds() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		assertEquals(1, dao.create(foo2));

		assertNotNull(dao.queryForId(foo1.id));
		assertNotNull(dao.queryForId(foo2.id));

		List<Integer> ids = new ArrayList<Integer>();
		ids.add(foo1.id);
		ids.add(foo2.id);
		assertEquals(2, dao.deleteIds(ids));
		assertEquals(0, dao.queryForAll().size());
		assertNull(dao.queryForId(foo1.id));
		assertNull(dao.queryForId(foo2.id));
	}

	@Test
	public void testDeleteIdsThrow() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection(FOO_TABLE_NAME);
		try {
			conn.close();
			List<Integer> foos = new ArrayList<Integer>();
			foos.add(foo.id);
			assertThrowsExactly(SQLException.class, () -> {
				dao.deleteIds(foos);
			});
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testDeleteIdsEmpty() throws Exception {
		BaseDaoImpl<Foo, Integer> dao = new BaseDaoImpl<Foo, Integer>(connectionSource, Foo.class) {
		};
		dao.initialize();
		List<Integer> fooList = new ArrayList<Integer>();
		assertEquals(0, dao.deleteIds(fooList));
	}

	@Test
	public void testDeleteIdsNoInit() throws Exception {
		BaseDaoImpl<Foo, Integer> dao = new BaseDaoImpl<Foo, Integer>(Foo.class) {
		};
		dao.setConnectionSource(connectionSource);
		assertThrowsExactly(IllegalStateException.class, () -> {
			dao.deleteIds((List<Integer>) null);
		});
	}

	@Test
	public void testDeletePreparedStatement() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		assertEquals(1, dao.create(foo2));

		assertNotNull(dao.queryForId(foo1.id));
		assertNotNull(dao.queryForId(foo2.id));

		DeleteBuilder<Foo, Integer> deleteBuilder = dao.deleteBuilder();
		deleteBuilder.where().eq(Foo.ID_COLUMN_NAME, foo1.id);
		assertEquals(1, dao.delete(deleteBuilder.prepare()));

		assertEquals(1, dao.queryForAll().size());
		assertNull(dao.queryForId(foo1.id));
		assertNotNull(dao.queryForId(foo2.id));
	}

	@Test
	public void testDeletePreparedThrow() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection(FOO_TABLE_NAME);
		try {
			conn.close();
			assertThrows(SQLException.class, () -> {
				dao.delete(dao.deleteBuilder().prepare());
			});
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testRefresh() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		int equal1 = 11312331;
		foo.equal = equal1;
		assertEquals(1, dao.create(foo));
		int equal2 = 312312;

		assertNotNull(dao.queryForId(foo.id));

		UpdateBuilder<Foo, Integer> updateBuilder = dao.updateBuilder();
		updateBuilder.updateColumnValue(Foo.EQUAL_COLUMN_NAME, equal2);
		updateBuilder.where().eq(Foo.ID_COLUMN_NAME, foo.id);
		assertEquals(1, dao.update(updateBuilder.prepare()));

		assertEquals(equal1, foo.equal);
		assertEquals(1, dao.refresh(foo));
		assertEquals(equal2, foo.equal);
	}

	@Test
	public void testRefreshThrow() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection(FOO_TABLE_NAME);
		try {
			conn.close();
			assertThrows(SQLException.class, () -> {
				dao.refresh(foo);
			});
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testRefreshNull() throws Exception {
		BaseDaoImpl<Foo, Integer> dao = new BaseDaoImpl<Foo, Integer>(connectionSource, Foo.class) {
		};
		dao.initialize();
		assertEquals(0, dao.refresh(null));
	}

	@Test
	public void testRefreshNoInit() throws Exception {
		BaseDaoImpl<Foo, Integer> dao = new BaseDaoImpl<Foo, Integer>(Foo.class) {
		};
		dao.setConnectionSource(connectionSource);
		assertThrowsExactly(IllegalStateException.class, () -> {
			dao.refresh(null);
		});
	}

	@Test
	public void testAnotherConstructor() throws Exception {
		new BaseDaoImpl<Foo, Integer>(Foo.class) {
		};
	}

	@Test
	public void testNoDatabaseType() throws Exception {
		BaseDaoImpl<Foo, Integer> dao = new BaseDaoImpl<Foo, Integer>(Foo.class) {
		};
		assertThrowsExactly(IllegalStateException.class, () -> {
			dao.initialize();
		});
	}

	@Test
	public void testQueryForIdNoInit() throws Exception {
		BaseDaoImpl<Foo, Integer> dao = new BaseDaoImpl<Foo, Integer>(Foo.class) {
		};
		assertThrowsExactly(IllegalStateException.class, () -> {
			dao.queryForId(1);
		});
	}

	@Test
	public void testQueryForFirst() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		assertEquals(1, dao.create(foo2));

		assertNotNull(dao.queryForId(foo1.id));
		assertNotNull(dao.queryForId(foo2.id));
		assertEquals(2, dao.queryForAll().size());

		Foo foo3 = dao.queryForFirst(dao.queryBuilder().prepare());
		assertNotNull(foo2);
		assertEquals(foo1.id, foo3.id);
	}

	@Test
	public void testQueryForFirstThrow() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		int equal = 21313;
		foo.equal = equal;
		assertEquals(1, dao.create(foo));

		DatabaseConnection conn = connectionSource.getReadWriteConnection(FOO_TABLE_NAME);
		try {
			conn.close();
			assertThrows(SQLException.class, () -> {
				dao.queryForFirst(dao.queryBuilder().prepare());
			});
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testQueryForFirstNoArgThrow() throws Exception {
		Dao<CreateOrUpdateObjectId, Integer> dao = createDao(CreateOrUpdateObjectId.class, true);
		CreateOrUpdateObjectId foo = new CreateOrUpdateObjectId();
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection(FOO_TABLE_NAME);
		try {
			conn.close();
			assertThrows(SQLException.class, () -> {
				dao.queryForFirst();
			});
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testQueryForFirstNoResults() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		assertNull(dao.queryForFirst(dao.queryBuilder().prepare()));
	}

	@Test
	public void testStatementBuilderNoInit() throws Exception {
		BaseDaoImpl<Foo, Integer> dao = new BaseDaoImpl<Foo, Integer>(Foo.class) {
		};
		assertThrowsExactly(IllegalStateException.class, () -> {
			dao.queryBuilder();
		});
	}

	@Test
	public void testQueryForFirstNoInit() throws Exception {
		BaseDaoImpl<Foo, Integer> dao = new BaseDaoImpl<Foo, Integer>(Foo.class) {
		};
		assertThrowsExactly(IllegalStateException.class, () -> {
			dao.queryForFirst(null);
		});
	}

	@Test
	public void testQueryForPreparedNoInit() throws Exception {
		BaseDaoImpl<Foo, Integer> dao = new BaseDaoImpl<Foo, Integer>(Foo.class) {
		};
		assertThrowsExactly(IllegalStateException.class, () -> {
			dao.query((PreparedQuery<Foo>) null);
		});
	}

	@Test
	public void testObjectToString() throws Exception {
		BaseDaoImpl<Foo, Integer> dao = new BaseDaoImpl<Foo, Integer>(connectionSource, Foo.class) {
		};
		dao.initialize();
		Foo foo = new Foo();
		String objStr = dao.objectToString(foo);
		assertTrue(objStr.contains("id=" + foo.id));
	}

	@Test
	public void testObjectToStringNoInit() throws Exception {
		BaseDaoImpl<Foo, Integer> dao = new BaseDaoImpl<Foo, Integer>(Foo.class) {
		};
		dao.setConnectionSource(connectionSource);
		assertThrowsExactly(IllegalStateException.class, () -> {
			dao.objectToString(new Foo());
		});
	}

	@Test
	public void testObjectsEqual() throws Exception {
		BaseDaoImpl<Foo, Integer> dao = new BaseDaoImpl<Foo, Integer>(connectionSource, Foo.class) {
		};
		dao.initialize();
		Foo foo = new Foo();
		foo.id = 121134243;
		foo.val = 123123;
		Foo bar = new Foo();
		assertTrue(dao.objectsEqual(foo, foo));
		assertFalse(dao.objectsEqual(foo, bar));
		assertFalse(dao.objectsEqual(bar, foo));
		assertTrue(dao.objectsEqual(bar, bar));
		bar.id = foo.id + 1;
		bar.val = foo.val;
		assertFalse(dao.objectsEqual(bar, foo));
	}

	@Test
	public void testObjectsEqualNoInit() throws Exception {
		BaseDaoImpl<Foo, Integer> dao = new BaseDaoImpl<Foo, Integer>(Foo.class) {
		};
		dao.setConnectionSource(connectionSource);
		assertThrowsExactly(IllegalStateException.class, () -> {
			dao.objectsEqual(new Foo(), new Foo());
		});
	}

	@Test
	public void testTableConfig() throws Exception {
		DatabaseTableConfig<Foo> config = DatabaseTableConfig.fromClass(databaseType, Foo.class);
		BaseDaoImpl<Foo, Integer> dao = new BaseDaoImpl<Foo, Integer>(connectionSource, config) {
		};
		assertSame(config, dao.getTableConfig());
	}

	@Test
	public void testTableExtactDataTypes() throws Exception {
		DatabaseTableConfig<Foo> config = DatabaseTableConfig.fromClass(databaseType, Foo.class);
		BaseDaoImpl<Foo, Integer> dao = new BaseDaoImpl<Foo, Integer>(connectionSource, config) {
		};
		DataType[] dataTypes = dao.extractTableDataTypes();
		assertNotNull(dataTypes);
		assertArrayEquals(new DataType[] { DataType.INTEGER, DataType.INTEGER, DataType.INTEGER, DataType.STRING },
				dataTypes);
	}

	@Test
	public void testSetters() throws Exception {
		DatabaseTableConfig<Foo> config = DatabaseTableConfig.fromClass(databaseType, Foo.class);
		BaseDaoImpl<Foo, Integer> dao = new BaseDaoImpl<Foo, Integer>(Foo.class) {
		};
		dao.setTableConfig(config);
		dao.setConnectionSource(connectionSource);
		assertSame(config, dao.getTableConfig());
	}

	@Test
	public void testQueryRaw() throws Exception {
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

		GenericRawResults<String[]> results =
				dao.queryRaw("SELECT " + Foo.ID_COLUMN_NAME + "," + Foo.EQUAL_COLUMN_NAME + " FROM FOO");
		List<String[]> resultList = results.getResults();
		assertEquals(2, resultList.size());
		String[] row = resultList.get(0);
		assertEquals(2, row.length);
		assertEquals(Integer.toString(foo1.id), row[0]);
		assertEquals(foo1.equal, Integer.parseInt(row[1]));
		row = resultList.get(1);
		assertEquals(2, row.length);
		assertEquals(Integer.toString(foo2.id), row[0]);
		assertEquals(foo2.equal, Integer.parseInt(row[1]));

		// string arguments
		results = dao.queryRaw("SELECT " + Foo.ID_COLUMN_NAME + "," + Foo.EQUAL_COLUMN_NAME + " FROM FOO WHERE "
				+ Foo.ID_COLUMN_NAME + " = ?", Integer.toString(foo1.id));
		resultList = results.getResults();
		assertEquals(1, resultList.size());
		row = resultList.get(0);
		assertEquals(2, row.length);
		assertEquals(Integer.toString(foo1.id), row[0]);
		assertEquals(foo1.equal, Integer.parseInt(row[1]));

		// argument holder arguments
		results = dao.queryRaw("SELECT " + Foo.ID_COLUMN_NAME + "," + Foo.EQUAL_COLUMN_NAME + " FROM FOO WHERE "
				+ Foo.ID_COLUMN_NAME + " = ?", new SelectArg(SqlType.INTEGER, foo1.id));
		resultList = results.getResults();
		assertEquals(1, resultList.size());
		row = resultList.get(0);
		assertEquals(2, row.length);
		assertEquals(Integer.toString(foo1.id), row[0]);
		assertEquals(foo1.equal, Integer.parseInt(row[1]));
	}

	@Test
	public void testQueryRawThrow() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection(FOO_TABLE_NAME);
		try {
			conn.close();
			assertThrowsExactly(SQLException.class, () -> {
				dao.queryRaw("SELECT * FROM FOO");
			});
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testQueryRawStringsThrow() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection(FOO_TABLE_NAME);
		try {
			conn.close();
			assertThrowsExactly(SQLException.class, () -> {
				dao.queryRaw("SELECT * FROM FOO WHERE " + Foo.ID_COLUMN_NAME + " = ?", Integer.toString(foo.id));
			});
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testQueryRawArgumentHoldersThrow() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection(FOO_TABLE_NAME);
		try {
			conn.close();
			assertThrowsExactly(SQLException.class, () -> {
				dao.queryRaw("SELECT * FROM FOO WHERE " + Foo.ID_COLUMN_NAME + " = ?",
						new SelectArg(SqlType.INTEGER, foo.id));
			});
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testQueryArgumentHolderNoSqltype() throws Exception {
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

		assertThrowsExactly(SQLException.class, () -> {
			dao.queryRaw("SELECT " + Foo.ID_COLUMN_NAME + "," + Foo.EQUAL_COLUMN_NAME + " FROM FOO WHERE "
					+ Foo.ID_COLUMN_NAME + " = ?", new SelectArg(Foo.ID_COLUMN_NAME, foo1.id));
		});
	}

	@Test
	public void testQueryRawUsingRawRowMapper() throws Exception {
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

		GenericRawResults<Foo> results =
				dao.queryRaw("SELECT " + Foo.ID_COLUMN_NAME + " FROM FOO", dao.getRawRowMapper());
		List<Foo> resultList = results.getResults();
		assertEquals(2, resultList.size());
		assertEquals(foo1.id, resultList.get(0).id);
		assertEquals(0, resultList.get(0).equal);
		assertEquals(foo2.id, resultList.get(1).id);
		assertEquals(0, resultList.get(0).equal);

		// string arguments
		results = dao.queryRaw("SELECT " + Foo.ID_COLUMN_NAME + " FROM FOO WHERE " + Foo.ID_COLUMN_NAME + " = ?",
				dao.getRawRowMapper(), Integer.toString(foo1.id));
		resultList = results.getResults();
		assertEquals(1, resultList.size());
		assertEquals(foo1.id, resultList.get(0).id);
		assertEquals(0, resultList.get(0).equal);

		// argument holder arguments
		results = dao.queryRaw("SELECT " + Foo.ID_COLUMN_NAME + " FROM FOO WHERE " + Foo.ID_COLUMN_NAME + " = ?",
				dao.getRawRowMapper(), new SelectArg(SqlType.INTEGER, foo1.id));
		resultList = results.getResults();
		assertEquals(1, resultList.size());
		assertEquals(foo1.id, resultList.get(0).id);
		assertEquals(0, resultList.get(0).equal);
	}

	@Test
	public void testQueryRawRowMapperThrow() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection(FOO_TABLE_NAME);
		try {
			conn.close();
			assertThrowsExactly(SQLException.class, () -> {
				dao.queryRaw("SELECT * FROM FOO", dao.getRawRowMapper());
			});
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testQueryRawRowMapperStringsThrow() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection(FOO_TABLE_NAME);
		try {
			conn.close();
			assertThrowsExactly(SQLException.class, () -> {
				dao.queryRaw("SELECT * FROM FOO WHERE " + Foo.ID_COLUMN_NAME + " = ?", dao.getRawRowMapper(),
						Integer.toString(foo.id));
			});
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testQueryRawRowMapperArgumentHoldersThrow() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection(FOO_TABLE_NAME);
		try {
			conn.close();
			assertThrowsExactly(SQLException.class, () -> {
				dao.queryRaw("SELECT * FROM FOO WHERE " + Foo.ID_COLUMN_NAME + " = ?", dao.getRawRowMapper(),
						new SelectArg(SqlType.INTEGER, foo.id));
			});
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testQueryRawObjects() throws Exception {
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

		GenericRawResults<Object[]> results =
				dao.queryRaw("SELECT " + Foo.ID_COLUMN_NAME + "," + Foo.EQUAL_COLUMN_NAME + " FROM FOO",
						new DataType[] { DataType.STRING, DataType.INTEGER });
		List<Object[]> resultList = results.getResults();
		assertEquals(2, resultList.size());
		Object[] row = resultList.get(0);
		assertEquals(2, row.length);
		assertEquals(Integer.toString(foo1.id), row[0]);
		assertEquals(foo1.equal, row[1]);
		row = resultList.get(1);
		assertEquals(2, row.length);
		assertEquals(Integer.toString(foo2.id), row[0]);
		assertEquals(foo2.equal, row[1]);

		// string arguments
		results =
				dao.queryRaw(
						"SELECT " + Foo.ID_COLUMN_NAME + "," + Foo.EQUAL_COLUMN_NAME + " FROM FOO WHERE "
								+ Foo.ID_COLUMN_NAME + " = ?",
						new DataType[] { DataType.STRING, DataType.INTEGER }, Integer.toString(foo2.id));
		resultList = results.getResults();
		assertEquals(1, resultList.size());
		row = resultList.get(0);
		assertEquals(2, row.length);
		assertEquals(Integer.toString(foo2.id), row[0]);
		assertEquals(foo2.equal, row[1]);

		// ArgumentHolder arguments
		results = dao.queryRaw(
				"SELECT " + Foo.ID_COLUMN_NAME + "," + Foo.EQUAL_COLUMN_NAME + " FROM FOO WHERE " + Foo.ID_COLUMN_NAME
						+ " = ?",
				new DataType[] { DataType.STRING, DataType.INTEGER }, new SelectArg(SqlType.INTEGER, foo1.id));
		resultList = results.getResults();
		assertEquals(1, resultList.size());
		row = resultList.get(0);
		assertEquals(2, row.length);
		assertEquals(Integer.toString(foo1.id), row[0]);
		assertEquals(foo1.equal, row[1]);
	}

	@Test
	public void testQueryRawObjectsThrow() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection(FOO_TABLE_NAME);
		try {
			conn.close();
			assertThrowsExactly(SQLException.class, () -> {
				dao.queryRaw("SELECT * FROM FOO", new DataType[0]);
			});
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testQueryRawObjectsStringsThrow() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection(FOO_TABLE_NAME);
		try {
			conn.close();
			assertThrowsExactly(SQLException.class, () -> {
				dao.queryRaw("SELECT * FROM FOO WHERE " + Foo.ID_COLUMN_NAME + " = ?", new DataType[0],
						Integer.toString(foo.id));
			});
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testQueryRawObjectsArgumentHoldersThrow() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection(FOO_TABLE_NAME);
		try {
			conn.close();
			assertThrowsExactly(SQLException.class, () -> {
				dao.queryRaw("SELECT * FROM FOO WHERE " + Foo.ID_COLUMN_NAME + " = ?", new DataType[0],
						new SelectArg(SqlType.INTEGER, foo.id));
			});
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testQueryRawDataTypesRawRowMapper() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		foo1.val = 1231232;
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		foo2.val = 324423;
		assertEquals(1, dao.create(foo2));

		QueryBuilder<Foo, Integer> queryBuilder = dao.queryBuilder();
		queryBuilder.where().eq(Foo.ID_COLUMN_NAME, foo1.id);

		RawRowObjectMapper<RawResult> rowMapper = new RawRowObjectMapper<RawResult>() {
			@Override
			public RawResult mapRow(String[] columnNames, DataType[] dataTypes, Object[] resultColumns) {
				RawResult result = new RawResult();
				assertEquals(1, resultColumns.length);
				result.val = (Integer) resultColumns[0];
				return result;
			}
		};
		GenericRawResults<RawResult> results = dao.queryRaw("SELECT (" + Foo.VAL_COLUMN_NAME + " * 2) FROM FOO",
				new DataType[] { DataType.INTEGER }, rowMapper);
		List<RawResult> resultList = results.getResults();
		assertEquals(2, resultList.size());
		assertEquals(foo1.val * 2, resultList.get(0).val);
		assertEquals(foo2.val * 2, resultList.get(1).val);

		results = dao.queryRaw("SELECT (" + Foo.VAL_COLUMN_NAME + " * 2) FROM FOO WHERE " + Foo.ID_COLUMN_NAME + " = ?",
				new DataType[] { DataType.INTEGER }, rowMapper, Integer.toString(foo1.id));
		resultList = results.getResults();
		assertEquals(1, resultList.size());
		assertEquals(foo1.val * 2, resultList.get(0).val);

		results = dao.queryRaw("SELECT (" + Foo.VAL_COLUMN_NAME + " * 2) FROM FOO WHERE " + Foo.ID_COLUMN_NAME + " = ?",
				new DataType[] { DataType.INTEGER }, rowMapper, new SelectArg(SqlType.INTEGER, foo2.id));
		resultList = results.getResults();
		assertEquals(1, resultList.size());
		assertEquals(foo2.val * 2, resultList.get(0).val);
	}

	@Test
	public void testQueryRawDataTypesRowMapperThrow() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection(FOO_TABLE_NAME);
		try {
			conn.close();
			assertThrowsExactly(SQLException.class, () -> {
				dao.queryRaw("SELECT * FROM FOO", new DataType[0], new RawRowObjectMapper<Object>() {
					@Override
					public Object mapRow(String[] columnNames, DataType[] dataTypes, Object[] resultColumns) {
						return new Object();
					}
				});
			});
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testQueryRawDataTypesRowMapperStringsThrow() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection(FOO_TABLE_NAME);
		try {
			conn.close();
			assertThrowsExactly(SQLException.class, () -> {
				dao.queryRaw("SELECT * FROM FOO WHERE " + Foo.ID_COLUMN_NAME + " = ?", new DataType[0],
						new RawRowObjectMapper<Object>() {
							@Override
							public Object mapRow(String[] columnNames, DataType[] dataTypes, Object[] resultColumns) {
								return new Object();
							}
						}, Integer.toString(foo.id));
			});
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testQueryRawDataTypesRowMapperArgumentHoldersThrow() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection(FOO_TABLE_NAME);
		try {
			conn.close();
			assertThrowsExactly(SQLException.class, () -> {
				dao.queryRaw("SELECT * FROM FOO WHERE " + Foo.ID_COLUMN_NAME + " = ?", new DataType[0],
						new RawRowObjectMapper<Object>() {
							@Override
							public Object mapRow(String[] columnNames, DataType[] dataTypes, Object[] resultColumns) {
								return new Object();
							}
						}, new SelectArg(SqlType.INTEGER, foo.id));
			});
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testQueryRawMapped() throws Exception {
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

		GenericRawResults<Foo> results = dao.queryRaw(
				"SELECT " + Foo.ID_COLUMN_NAME + "," + Foo.EQUAL_COLUMN_NAME + " FROM FOO", new RawRowMapper<Foo>() {
					@Override
					public Foo mapRow(String[] columnNames, String[] resultColumns) {
						assertEquals(2, columnNames.length);
						assertEquals(2, resultColumns.length);
						Foo foo = new Foo();
						foo.id = Integer.parseInt(resultColumns[0]);
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

	@Test
	public void testQueryRawMappedThrow() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection(FOO_TABLE_NAME);
		try {
			conn.close();
			assertThrowsExactly(SQLException.class, () -> {
				dao.queryRaw("SELECT * FROM FOO", new RawRowMapper<Foo>() {
					@Override
					public Foo mapRow(String[] columnNames, String[] resultColumns) {
						return null;
					}
				});
			});
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testIsUpdatable() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, false);
		assertTrue(dao.isUpdatable());
	}

	@Test
	public void testIsTableExists() throws Exception {
		Dao<TableExists, Integer> dao = createDao(TableExists.class, false);
		assertFalse(dao.isTableExists());
		TableUtils.createTable(connectionSource, TableExists.class);
		assertTrue(dao.isTableExists());
		TableUtils.dropTable(connectionSource, TableExists.class, true);
		assertFalse(dao.isTableExists());
	}

	@Test
	public void testIsTableExistsThrow() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection(FOO_TABLE_NAME);
		try {
			conn.close();
			assertThrows(SQLException.class, () -> {
				dao.isTableExists();
			});
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testBadConnectionSource() {
		ConnectionSource cs = createMock(ConnectionSource.class);
		assertThrowsExactly(IllegalStateException.class, () -> {
			new BaseDaoImpl<Foo, Integer>(cs, Foo.class) {
			};
		});
	}

	@Test
	public void testUpdateBuilder() throws Exception {
		BaseDaoImpl<Foo, Integer> dao = new BaseDaoImpl<Foo, Integer>(connectionSource, Foo.class) {
		};
		dao.updateBuilder();
	}

	@Test
	public void testDeleteBuilder() throws Exception {
		BaseDaoImpl<Foo, Integer> dao = new BaseDaoImpl<Foo, Integer>(connectionSource, Foo.class) {
		};
		dao.deleteBuilder();
	}

	@Test
	public void testDataClass() throws Exception {
		BaseDaoImpl<Foo, Integer> dao = new BaseDaoImpl<Foo, Integer>(Foo.class) {
		};
		assertEquals(Foo.class, dao.getDataClass());
	}

	@Test
	public void testUpdateRaw() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		assertEquals(1, dao.create(foo2));
		Foo foo3 = new Foo();
		assertEquals(1, dao.create(foo3));
		assertEquals(3, dao.queryForAll().size());

		dao.updateRaw("DELETE FROM FOO WHERE " + Foo.ID_COLUMN_NAME + " = " + foo1.id);
		assertEquals(2, dao.queryForAll().size());

		dao.updateRaw("DELETE FROM FOO WHERE " + Foo.ID_COLUMN_NAME + " = ?", Integer.toString(foo2.id));
		assertEquals(1, dao.queryForAll().size());

		dao.updateRaw("DELETE FROM FOO WHERE " + Foo.ID_COLUMN_NAME + " = ?", new SelectArg(SqlType.INTEGER, foo3.id));
		assertEquals(0, dao.queryForAll().size());
	}

	@Test
	public void testUpdateRawThrow() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection(FOO_TABLE_NAME);
		try {
			conn.close();
			assertThrowsExactly(SQLException.class, () -> {
				dao.updateRaw("DELETE FROM FOO");
			});
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testUpdateRawStringsThrow() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection(FOO_TABLE_NAME);
		try {
			conn.close();
			assertThrowsExactly(SQLException.class, () -> {
				dao.updateRaw("DELETE FROM FOO", "arg");
			});
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testUpdateRawArgumentHoldersThrow() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection(FOO_TABLE_NAME);
		try {
			conn.close();
			assertThrowsExactly(SQLException.class, () -> {
				dao.updateRaw("DELETE FROM FOO", new SelectArg());
			});
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testUpdateRawArgumentHolders() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		assertEquals(1, dao.create(foo2));

		assertEquals(2, dao.queryForAll().size());
		SelectArg arg = new SelectArg(SqlType.INTEGER, foo1.id);
		dao.updateRaw("DELETE FROM FOO WHERE " + Foo.ID_COLUMN_NAME + " = ?", arg);
		assertEquals(1, dao.queryForAll().size());
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testExecuteRaw() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		assertEquals(1, dao.create(foo2));
		Foo foo3 = new Foo();
		assertEquals(1, dao.create(foo3));

		assertEquals(3, dao.queryForAll().size());

		dao.executeRaw("delete from " + FOO_TABLE_NAME + " where " + Foo.ID_COLUMN_NAME + " = ?",
				Integer.toString(foo1.id));
		assertEquals(2, dao.queryForAll().size());

		dao.executeRaw("delete from " + FOO_TABLE_NAME + " where " + Foo.ID_COLUMN_NAME + " = ?",
				new SelectArg(SqlType.INTEGER, foo2.id));
		assertEquals(1, dao.queryForAll().size());

		dao.executeRaw("TRUNCATE TABLE FOO");
		// coverage
		dao.executeRawNoArgs("TRUNCATE TABLE FOO");
		assertEquals(0, dao.queryForAll().size());
	}

	@Test
	public void testExecuteRawThrow() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection(FOO_TABLE_NAME);
		try {
			conn.close();
			assertThrowsExactly(SQLException.class, () -> {
				dao.executeRaw("TRUNCATE TABLE FOO");
			});
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testExecuteRawNoArgsThrow() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection(FOO_TABLE_NAME);
		try {
			conn.close();
			assertThrowsExactly(SQLException.class, () -> {
				dao.executeRawNoArgs("TRUNCATE TABLE FOO");
			});
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testExecuteRawStringsThrow() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection(FOO_TABLE_NAME);
		try {
			conn.close();
			assertThrowsExactly(SQLException.class, () -> {
				dao.executeRaw("TRUNCATE TABLE FOO", "1");
			});
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testExecuteRawArgumentHoldersThrow() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection(FOO_TABLE_NAME);
		try {
			conn.close();
			assertThrowsExactly(SQLException.class, () -> {
				dao.executeRaw("TRUNCATE TABLE FOO", new SelectArg());
			});
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testExtractId() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals((Integer) foo.id, dao.extractId(foo));
	}

	@Test
	public void testExtractIdBadClass() throws Exception {
		Dao<NoId, Void> dao = createDao(NoId.class, true);
		NoId foo = new NoId();
		String stuff = "stuff1";
		foo.stuff = stuff;
		assertThrowsExactly(SQLException.class, () -> {
			dao.extractId(foo);
		});
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
		final Dao<Foo, Integer> dao = createDao(Foo.class, true);
		final Foo foo1 = new Foo();
		assertEquals(0, dao.queryForAll().size());

		// this should be none
		dao.callBatchTasks(new Callable<Void>() {
			@Override
			public Void call() throws Exception {
				assertEquals(1, dao.create(foo1));
				return null;
			}
		});

		assertEquals(1, dao.queryForAll().size());
	}

	@Test
	public void testCallBatchThrow() throws Exception {
		final Dao<Foo, Integer> dao = createDao(Foo.class, true);
		assertEquals(0, dao.queryForAll().size());

		// this should be none
		assertThrowsExactly(SQLException.class, () -> {
			dao.callBatchTasks(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					throw new Exception("for the hell of it");
				}
			});
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
	public void testForeignNoId() throws Exception {
		Dao<Foreign, Integer> dao = createDao(Foreign.class, true);
		Foreign foreign = new Foreign();
		foreign.foo = null;
		assertEquals(1, dao.create(foreign));
		foreign.foo = new Foo();
		assertEquals(1, dao.update(foreign));
	}

	@Test
	public void testForeignIntIdNull() throws Exception {
		Dao<ForeignIntId, Integer> dao = createDao(ForeignIntId.class, true);
		ForeignIntId foreign = new ForeignIntId();
		foreign.one = null;
		assertEquals(1, dao.create(foreign));
		ForeignIntId foreign2 = dao.queryForId(foreign.id);
		assertNotNull(foreign2);
		assertNull(foreign2.one);
	}

	@Test
	public void testForeign() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		Dao<Foreign, Integer> foreignDao = createDao(Foreign.class, true);

		Foo foo = new Foo();
		int val = 6389;
		foo.val = val;
		foo.stringField = "hello there";
		assertEquals(1, fooDao.create(foo));

		Foreign foreign = new Foreign();
		foreign.foo = foo;
		assertEquals(1, foreignDao.create(foreign));

		Foreign foreign2 = foreignDao.queryForId(foreign.id);
		assertNotNull(foreign2);
		assertNotNull(foreign2.foo);
		assertNotNull(foreign2.foo.id);
		assertEquals(foo.id, foreign2.foo.id);
		assertEquals(0, foreign2.foo.val);
		assertNull(foreign2.foo.stringField);

		assertEquals(1, fooDao.refresh(foreign2.foo));
		assertEquals(val, foreign2.foo.val);
	}

	@Test
	public void testForeignAutoRefresh() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		Dao<ForeignAutoRefresh, Integer> foreignDao = createDao(ForeignAutoRefresh.class, true);

		Foo foo = new Foo();
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

	@Test
	public void testForeignAutoRefreshFalse() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		Dao<ForeignAutoRefreshFalse, Integer> foreignDao = createDao(ForeignAutoRefreshFalse.class, true);

		Foo foo = new Foo();
		foo.val = 6389;
		foo.stringField = "not null";
		assertEquals(1, fooDao.create(foo));

		ForeignAutoRefreshFalse foreign = new ForeignAutoRefreshFalse();
		foreign.foo = foo;
		assertEquals(1, foreignDao.create(foreign));

		ForeignAutoRefreshFalse foreign2 = foreignDao.queryForId(foreign.id);
		assertNotNull(foreign2);
		assertNotNull(foreign2.foo.id);
		assertEquals(foo.id, foreign2.foo.id);
		assertEquals(0, foreign2.foo.val);
		assertNull(foreign2.foo.stringField);
	}

	@Test
	public void testForeignCantBeNull() throws Exception {
		Dao<ForeignNotNull, Integer> dao = createDao(ForeignNotNull.class, true);
		ForeignNotNull foreign = new ForeignNotNull();
		foreign.foo = null;
		assertThrowsExactly(SQLException.class, () -> {
			dao.create(foreign);
		});
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

		One anotherOne = new One();
		String anonterOneOneStuff = "e24fweggwgee";
		anotherOne.stuff = anonterOneOneStuff;
		try {
			oneDao.create(anotherOne);
			fail("should have thrown because no id was specified");
		} catch (SQLException se) {
			// expected
		}

		assertNotNull(oneDao.queryForId(one.id));
		assertNotNull(oneDao.queryForId(two.id));
		assertNull(oneDao.queryForId(anotherOne.id));
		assertEquals(2, oneDao.queryForAll().size());
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
	public void testUuidIdInsert() throws Exception {
		Dao<UuidId, UUID> dao = createDao(UuidId.class, true);
		UuidId uuid = new UuidId();
		UUID id = UUID.randomUUID();
		uuid.id = id;
		String stuff = "fopewfjefjwgw";
		uuid.stuff = stuff;
		assertEquals(1, dao.create(uuid));
		UuidId uuid2 = dao.queryForId(uuid.id);
		assertNotNull(uuid2);
		assertEquals(id, uuid2.id);
		assertEquals(stuff, uuid2.stuff);
	}

	@Test
	public void testCountOf() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		assertEquals(0, dao.countOf());
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));
		assertEquals(1, dao.countOf());
		assertEquals(1, dao.create(foo));
		assertEquals(2, dao.countOf());
	}

	@Test
	public void testCountOfPrepared() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		assertEquals(0, dao.countOf());
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));
		assertEquals(1, dao.create(foo));
		assertEquals(2, dao.countOf());

		QueryBuilder<Foo, Integer> qb = dao.queryBuilder();
		qb.setCountOf(true).where().eq(Foo.ID_COLUMN_NAME, foo.id);
		assertEquals(1, dao.countOf(qb.prepare()));
	}

	@Test
	public void testCountOfPreparedNoCountOf() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		QueryBuilder<Foo, Integer> qb = dao.queryBuilder();
		assertThrowsExactly(IllegalArgumentException.class, () -> {
			dao.countOf(qb.prepare());
		});
	}

	@Test
	public void testQueryForEq() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		assertEquals(0, dao.countOf());
		Foo foo1 = new Foo();
		foo1.val = 1231231;
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		foo2.val = foo1.val + 1;
		assertEquals(1, dao.create(foo2));

		List<Foo> results = dao.queryForEq(Foo.VAL_COLUMN_NAME, foo1.val);
		assertEquals(1, results.size());
		assertEquals(foo1.id, results.get(0).id);
	}

	@Test
	public void testQueryForForeignEq() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		Dao<Foreign, Integer> foreignDao = createDao(Foreign.class, true);

		Foo foo1 = new Foo();
		foo1.val = 1231231;
		assertEquals(1, fooDao.create(foo1));
		Foo foo2 = new Foo();
		foo1.val = 1233241231;
		assertEquals(1, fooDao.create(foo2));

		Foreign foreign = new Foreign();
		foreign.foo = foo1;
		assertEquals(1, foreignDao.create(foreign));

		List<Foreign> results = foreignDao.queryForEq(Foreign.FOO_COLUMN_NAME, foo1);
		assertEquals(1, results.size());
		assertEquals(foo1.id, results.get(0).foo.id);

		results = foreignDao.queryForEq(Foreign.FOO_COLUMN_NAME, foo2);
		assertEquals(0, results.size());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testUseOfAndMany() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		assertEquals(0, dao.countOf());
		Foo foo1 = new Foo();
		foo1.val = 1231231;
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		foo2.val = foo1.val + 1;
		assertEquals(1, dao.create(foo2));

		Where<Foo, Integer> where = dao.queryBuilder().where();
		where.and(where.eq(Foo.VAL_COLUMN_NAME, foo1.val), where.eq(Foo.ID_COLUMN_NAME, foo1.id));

		List<Foo> results = where.query();
		assertEquals(1, results.size());
		assertEquals(foo1.id, results.get(0).id);

		// this should match none
		where.reset();
		where.and(where.eq(Foo.ID_COLUMN_NAME, foo1.id), where.eq(Foo.ID_COLUMN_NAME, foo2.id),
				where.eq(Foo.VAL_COLUMN_NAME, foo1.val), where.eq(Foo.VAL_COLUMN_NAME, foo2.val));
		results = where.query();
		assertEquals(0, results.size());
	}

	@Test
	public void testUseOfAndInt() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		assertEquals(0, dao.countOf());
		Foo foo1 = new Foo();
		foo1.val = 1231231;
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		foo2.val = foo1.val + 1;
		assertEquals(1, dao.create(foo1));

		Where<Foo, Integer> where = dao.queryBuilder().where();
		where.eq(Foo.VAL_COLUMN_NAME, foo1.val);
		where.eq(Foo.ID_COLUMN_NAME, foo1.id);
		where.and(2);

		List<Foo> results = where.query();
		assertEquals(1, results.size());
		assertEquals(foo1.id, results.get(0).id);

		// this should match none
		where.reset();
		where.eq(Foo.ID_COLUMN_NAME, foo1.id);
		where.eq(Foo.ID_COLUMN_NAME, foo2.id);
		where.eq(Foo.VAL_COLUMN_NAME, foo1.val);
		where.eq(Foo.VAL_COLUMN_NAME, foo2.val);
		where.and(4);

		results = where.query();
		assertEquals(0, results.size());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testUseOfOrMany() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		assertEquals(0, dao.countOf());
		Foo foo1 = new Foo();
		int val = 1231231;
		foo1.val = val;
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		foo2.val = val + 1;
		assertEquals(1, dao.create(foo2));

		Where<Foo, Integer> where = dao.queryBuilder().where();
		where.or(where.eq(Foo.ID_COLUMN_NAME, foo1.id), where.eq(Foo.ID_COLUMN_NAME, foo2.id),
				where.eq(Foo.VAL_COLUMN_NAME, val), where.eq(Foo.VAL_COLUMN_NAME, foo2.val));

		List<Foo> results = where.query();
		assertEquals(2, results.size());
		assertEquals(foo1.id, results.get(0).id);
		assertEquals(foo2.id, results.get(1).id);
	}

	@Test
	public void testUseOfOrInt() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		assertEquals(0, dao.countOf());
		Foo foo1 = new Foo();
		int val = 1231231;
		foo1.val = val;
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		foo2.val = val + 1;
		assertEquals(1, dao.create(foo2));

		Where<Foo, Integer> where = dao.queryBuilder().where();
		where.eq(Foo.ID_COLUMN_NAME, foo1.id);
		where.eq(Foo.ID_COLUMN_NAME, foo2.id);
		where.eq(Foo.VAL_COLUMN_NAME, val);
		where.eq(Foo.VAL_COLUMN_NAME, val + 1);
		where.or(4);

		List<Foo> results = where.query();
		assertEquals(2, results.size());
		assertEquals(foo1.id, results.get(0).id);
		assertEquals(foo2.id, results.get(1).id);
	}

	@Test
	public void testQueryForMatching() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		assertEquals(0, dao.countOf());
		Foo foo1 = new Foo();
		int val = 1231231;
		foo1.val = val;
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		foo2.val = val + 1;
		assertEquals(1, dao.create(foo2));

		Foo match = new Foo();
		match.val = val;
		List<Foo> results = dao.queryForMatching(match);
		assertEquals(1, results.size());
		assertEquals(foo1.id, results.get(0).id);

		match = new Foo();
		match.id = foo2.id;
		match.val = val;
		results = dao.queryForMatching(match);
		assertEquals(0, results.size());
	}

	@Test
	public void testQueryForMatchingQuotes() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		assertEquals(0, dao.countOf());
		Foo foo = new Foo();
		foo.val = 1231231;
		assertEquals(1, dao.create(foo));

		Foo match = new Foo();
		match.stringField = "this id has a quote ' and \"";
		assertThrows(SQLException.class, () -> {
			dao.queryForMatching(match);
		});
	}

	@Test
	public void testQueryForMatchingArgs() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		assertEquals(0, dao.countOf());
		Foo foo1 = new Foo();
		int val = 1231231;
		foo1.val = val;
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		foo2.val = val + 1;
		assertEquals(1, dao.create(foo2));

		Foo match = new Foo();
		match.stringField = "this id has a quote ' and \"";
		List<Foo> results = dao.queryForMatchingArgs(match);
		assertEquals(0, results.size());
	}

	@Test
	public void testQueryForMatchingNoFields() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		assertEquals(0, dao.countOf());
		Foo foo = new Foo();
		foo.val = 1231231;
		assertEquals(1, dao.create(foo));

		Foo match = new Foo();
		List<Foo> results = dao.queryForMatching(match);
		assertEquals(0, results.size());
	}

	@Test
	public void testQueryForFieldValues() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		assertEquals(0, dao.countOf());
		Foo foo1 = new Foo();
		foo1.val = 1231231;
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		foo2.val = foo1.val + 1;
		assertEquals(1, dao.create(foo2));

		Map<String, Object> fieldValues = new HashMap<String, Object>();
		fieldValues.put(Foo.VAL_COLUMN_NAME, foo1.val);
		List<Foo> results = dao.queryForFieldValues(fieldValues);
		assertEquals(1, results.size());
		assertEquals(foo1.id, results.get(0).id);

		fieldValues.put(Foo.ID_COLUMN_NAME, foo2.id);
		fieldValues.put(Foo.VAL_COLUMN_NAME, foo1.val);
		results = dao.queryForFieldValues(fieldValues);
		assertEquals(0, results.size());
	}

	@Test
	public void testQueryForFieldValuesQuotes() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		assertEquals(0, dao.countOf());
		Foo foo1 = new Foo();
		foo1.val = 1231231;
		assertEquals(1, dao.create(foo1));

		Map<String, Object> fieldValues = new HashMap<String, Object>();
		fieldValues.put(Foo.ID_COLUMN_NAME, "this id has a quote ' \"");
		assertThrows(SQLException.class, () -> {
			dao.queryForFieldValues(fieldValues);
		});
	}

	@Test
	public void testQueryForFieldValuesArgs() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Map<String, Object> fieldValues = new HashMap<String, Object>();
		fieldValues.put(Foo.STRING_COLUMN_NAME, "this id has a quote ' \"");
		dao.queryForFieldValuesArgs(fieldValues);
	}

	@Test
	public void testQueryForFieldValuesEmpty() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		assertEquals(0, dao.countOf());
		Foo foo = new Foo();
		foo.val = 1231231;
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
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		dao.executeRaw("CREATE TRIGGER foo_trigger AFTER INSERT ON foo\n" + "FOR EACH ROW CALL " + "\""
				+ ExampleH2Trigger.class.getName() + "\"");

		assertEquals(0, ExampleH2Trigger.callC);
		assertEquals(1, dao.create(new Foo()));
		assertEquals(1, ExampleH2Trigger.callC);
		assertEquals(1, dao.create(new Foo()));
		assertEquals(2, ExampleH2Trigger.callC);
	}

	@Test
	public void testSelectRaw() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));
		QueryBuilder<Foo, Integer> qb = dao.queryBuilder();
		qb.selectRaw("COUNT(*)");
		GenericRawResults<String[]> results = dao.queryRaw(qb.prepareStatementString());
		List<String[]> list = results.getResults();
		assertEquals(1, list.size());
		String[] array = list.get(0);
		assertEquals(1, array.length);
		assertEquals("1", array[0]);
	}

	@Test
	public void testSelectRawNotQuery() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));
		QueryBuilder<Foo, Integer> qb = dao.queryBuilder();
		qb.selectRaw("COUNTOF(*)");
		assertThrowsExactly(SQLException.class, () -> {
			qb.query();
		});
	}

	/**
	 * Example of a H2 trigger.
	 */
	public static class ExampleH2Trigger implements Trigger {
		static int callC = 0;

		@Override
		public void init(Connection conn, String schemaName, String triggerName, String tableName, boolean before,
				int type) {
			// noop
		}

		@Override
		public void fire(Connection conn, Object[] oldRow, Object[] newRow) {
			callC++;
		}

		@Override
		public void close() {
			// noop
		}

		@Override
		public void remove() {
			// noop
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

	@Test
	public void testCreateOrUpdate() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		int equal1 = 21313;
		foo1.equal = equal1;
		CreateOrUpdateStatus status = dao.createOrUpdate(foo1);
		assertTrue(status.isCreated());
		assertFalse(status.isUpdated());
		assertEquals(1, status.getNumLinesChanged());

		int equal2 = 4134132;
		foo1.equal = equal2;
		status = dao.createOrUpdate(foo1);
		assertFalse(status.isCreated());
		assertTrue(status.isUpdated());
		assertEquals(1, status.getNumLinesChanged());

		Foo fooResult = dao.queryForId(foo1.id);
		assertEquals(equal2, fooResult.equal);
	}

	@Test
	public void testCreateOrUpdateNull() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		CreateOrUpdateStatus status = dao.createOrUpdate(null);
		assertFalse(status.isCreated());
		assertFalse(status.isUpdated());
		assertEquals(0, status.getNumLinesChanged());
	}

	@Test
	public void testCreateOrUpdateNullId() throws Exception {
		Dao<CreateOrUpdateObjectId, Integer> dao = createDao(CreateOrUpdateObjectId.class, true);
		CreateOrUpdateObjectId foo = new CreateOrUpdateObjectId();
		String stuff = "21313";
		foo.stuff = stuff;
		CreateOrUpdateStatus status = dao.createOrUpdate(foo);
		assertTrue(status.isCreated());
		assertFalse(status.isUpdated());
		assertEquals(1, status.getNumLinesChanged());

		CreateOrUpdateObjectId result = dao.queryForId(foo.id);
		assertNotNull(result);
		assertEquals(stuff, result.stuff);

		String stuff2 = "pwojgfwe";
		foo.stuff = stuff2;
		dao.createOrUpdate(foo);

		result = dao.queryForId(foo.id);
		assertNotNull(result);
		assertEquals(stuff2, result.stuff);
	}

	@Test
	public void testQueryForSameId() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		foo1.equal = 198412893;
		assertEquals(1, dao.create(foo1));

		Foo fooResult = dao.queryForSameId(foo1);
		assertEquals(foo1.id, fooResult.id);
		assertEquals(foo1.equal, fooResult.equal);
	}

	@Test
	public void testQueryForSameIdNull() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		assertNull(dao.queryForSameId(null));
	}

	@Test
	public void testQueryForSameIdNullId() throws Exception {
		Dao<CreateOrUpdateObjectId, Integer> dao = createDao(CreateOrUpdateObjectId.class, true);
		CreateOrUpdateObjectId foo = new CreateOrUpdateObjectId();
		assertEquals(1, dao.create(foo));
		assertNotNull(dao.queryForSameId(foo));
		foo.id = null;
		assertNull(dao.queryForSameId(foo));
	}

	@Test
	public void testCreateIfNotExists() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		foo1.equal = 198412893;

		Foo fooResult = dao.createIfNotExists(foo1);
		assertSame(foo1, fooResult);

		// now if we do it again, we should get the database copy of foo
		fooResult = dao.createIfNotExists(foo1);
		assertNotSame(foo1, fooResult);

		assertEquals(foo1.id, fooResult.id);
		assertEquals(foo1.equal, fooResult.equal);
	}

	@Test
	public void testCreateIfNotExistsNull() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, false);
		assertNull(dao.createIfNotExists(null));
	}

	@Test
	public void testCreateIfNotExistsSupplier() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		foo1.equal = 198412893;
		assertEquals(1, dao.create(foo1));

		// the one that will be returned by our supplier
		final Foo foo2 = new Foo();
		foo2.equal = 2343443;

		@SuppressWarnings("unchecked")
		Supplier<Foo> supplier = createMock(Supplier.class);

		// no calls to supplier here
		replay(supplier);

		// foo1 already created so this should be the same as foo1
		Foo fooResult = dao.createIfNotExists(foo1.id, supplier);
		assertEquals(foo1.id, fooResult.id);
		assertEquals(foo1.equal, fooResult.equal);

		verify(supplier);

		// reset our mock so we can add more expected stuff
		reset(supplier);
		expect(supplier.get()).andReturn(foo2);

		replay(supplier);

		// but if we try to create one with an id that doesn't exist then the supplier should be called
		int createdId = foo1.id + 1;
		fooResult = dao.createIfNotExists(createdId, supplier);
		assertSame(foo2, fooResult);
		assertEquals(createdId, foo2.id);

		verify(supplier);
	}

	@Test
	public void testCreateIfNotExistsSupplierKeyNull() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, false);
		assertNull(dao.createIfNotExists(null, null));
	}

	@Test
	public void testReplaceCache() throws Exception {
		Dao<Foo, Object> dao = createDao(Foo.class, true);
		ReferenceObjectCache cache1 = new ReferenceObjectCache(true);
		// coverage
		dao.clearObjectCache();
		dao.setObjectCache(cache1);
		// coverage
		dao.setObjectCache(cache1);

		Foo foo = new Foo();
		int val = 12312321;
		foo.val = val;

		assertEquals(1, dao.create(foo));

		Foo result = dao.queryForId(foo.id);
		assertSame(foo, result);

		// enable a new cache
		dao.setObjectCache(new ReferenceObjectCache(true));
		assertEquals(0, cache1.size(Foo.class));

		result = dao.queryForId(foo.id);
		assertNotSame(foo, result);

		dao.clearObjectCache();
	}

	@Test
	public void testColumnDefinition() throws Exception {
		Dao<ColumnDefinition, Integer> dao = createDao(ColumnDefinition.class, true);

		ColumnDefinition foo = new ColumnDefinition();
		String stuff = "hfejpowello";
		foo.stuff = stuff;
		assertEquals(1, dao.create(foo));

		ColumnDefinition result = dao.queryForId(foo.id);
		assertNotNull(result);
		assertEquals(stuff, foo.stuff);
	}

	@Test
	public void testIfExists() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);

		Foo foo = new Foo();

		assertFalse(dao.idExists(1));
		assertEquals(1, dao.create(foo));
		assertTrue(dao.idExists(1));
		assertFalse(dao.idExists(2));
	}

	@Test
	public void testUpdateTwoDates() throws Exception {
		Dao<TwoDates, Integer> dao = createDao(TwoDates.class, true);

		TwoDates foo = new TwoDates();
		long now = System.currentTimeMillis();
		Date date1 = new Date(now);
		Date date2 = new Date(now + 1000);
		foo.date1 = date1;
		foo.date2 = date2;

		assertEquals(1, dao.create(foo));

		UpdateBuilder<TwoDates, Integer> ub = dao.updateBuilder();
		ub.updateColumnValue(TwoDates.FIELD_NAME_DATE1, date2);
		ub.updateColumnValue(TwoDates.FIELD_NAME_DATE2, date1);
		dao.update(ub.prepare());
	}

	@Test
	public void testUpdateTwoNulls() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		UpdateBuilder<Foo, Integer> ub = dao.updateBuilder();
		ub.updateColumnValue(Foo.STRING_COLUMN_NAME, null);
		ub.updateColumnValue(Foo.EQUAL_COLUMN_NAME, null);
		dao.update(ub.prepare());
	}

	@Test
	public void testUpdateTwoNullsInSeperateStatements() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		UpdateBuilder<Foo, Integer> ub = dao.updateBuilder();
		ub.updateColumnValue(Foo.STRING_COLUMN_NAME, null);
		dao.update(ub.prepare());
		ub = dao.updateBuilder();
		ub.updateColumnValue(Foo.EQUAL_COLUMN_NAME, null);
		dao.update(ub.prepare());
	}

	@Test
	public void testHeritagePattern() throws Exception {
		Dao<BaseEntity, Integer> baseDao = createDao(BaseEntity.class, true);
		Dao<SubEntity, Integer> subDao = createDao(SubEntity.class, true);

		SubEntity sub1 = new SubEntity();
		sub1.stuff = "stuff";
		sub1.otherStuff = "other";
		assertEquals(1, subDao.create(sub1));

		SubEntity subResult = subDao.queryForId(sub1.id);
		assertEquals(sub1.id, subResult.id);
		assertEquals(sub1.stuff, subResult.stuff);
		assertEquals(sub1.otherStuff, subResult.otherStuff);

		BaseEntity baseResult = baseDao.queryForId(sub1.id);
		assertEquals(sub1.id, baseResult.id);
		assertEquals(sub1.stuff, baseResult.stuff);
	}

	@Test
	public void testForeignColumnName() throws Exception {
		Dao<ForeignColumnName, Integer> dao = createDao(ForeignColumnName.class, true);
		Dao<ForeignColumnNameForeign, Integer> foreignDao = createDao(ForeignColumnNameForeign.class, true);

		ForeignColumnNameForeign foreign = new ForeignColumnNameForeign();
		foreign.name = "Buzz Lightyear";
		assertEquals(1, foreignDao.create(foreign));

		ForeignColumnName fcn = new ForeignColumnName();
		fcn.foreign = foreign;
		assertEquals(1, dao.create(fcn));

		ForeignColumnName result = dao.queryForId(fcn.id);
		assertNotNull(result);
		assertNotNull(result.foreign);
		assertEquals(foreign.id, result.foreign.id);
		assertEquals(foreign.name, result.foreign.name);

		// this should work and not throw
		assertEquals(1, foreignDao.refresh(result.foreign));
	}

	@Test
	public void testAutoCommitClose() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		DatabaseConnection conn = null;
		try {
			conn = dao.startThreadConnection();
			dao.setAutoCommit(conn, false);

			Foo foo = new Foo();
			assertEquals(1, dao.create(foo));
			List<Foo> results = dao.queryForAll();
			assertEquals(1, results.size());

			dao.endThreadConnection(conn);
			conn = null;
			after();
			DaoManager.clearCache();
			before();

			dao = createDao(Foo.class, true);

			results = dao.queryForAll();
			// we expect there to be no results because we closed the connection to the database before a commit
			// happened
			assertEquals(0, results.size());
		} finally {
			if (conn != null) {
				dao.endThreadConnection(conn);
			}
		}
	}

	@Test
	public void testConnectionMethods() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		DatabaseConnection conn = null;
		try {
			conn = dao.startThreadConnection();
			assertTrue(dao.isAutoCommit(conn));
			dao.setAutoCommit(conn, false);
			assertFalse(dao.isAutoCommit(conn));

			Foo foo = new Foo();
			assertEquals(1, dao.create(foo));
			assertNotNull(dao.queryForId(foo.id));

			dao.rollBack(conn);
			assertNull(dao.queryForId(foo.id));

			foo = new Foo();
			assertEquals(1, dao.create(foo));
			assertNotNull(dao.queryForId(foo.id));

			dao.commit(conn);
			assertNotNull(dao.queryForId(foo.id));

			dao.rollBack(conn);
			assertNotNull(dao.queryForId(foo.id));

		} finally {
			if (conn != null) {
				conn.setAutoCommit(true);
				dao.endThreadConnection(conn);
			}
		}
	}

	@Test
	public void testJeremyNull() throws Exception {
		Jeremy1 loop1 = new Jeremy1();
		Dao<Jeremy2, Long> dao = createDao(Jeremy2.class, true);
		List<Jeremy2> bars = dao.queryBuilder()
				.where()
				.eq(Jeremy2.LOOP1_COLUMN_NAME, loop1)
				.and()
				.eq(Jeremy2.OTHER_COLUMN_NAME, "someValue")
				.query();
		assertEquals(0, bars.size());
	}

	@Test
	public void testComparisonOfForeignCollection() throws Exception {
		Dao<ForeignCollectionComparison, Long> dao = createDao(ForeignCollectionComparison.class, true);
		try {
			// we can't do a query on a foreign collection field
			dao.queryForEq("foos", "someValue");
		} catch (NullPointerException e) {
			fail("Should not get a NPE here");
		} catch (SQLException e) {
			// this is what we should get
		}
	}

	@Test
	public void testSerializableTimeStamp() throws Exception {
		Dao<TimeStampSerializable, Object> dao = createDao(TimeStampSerializable.class, true);
		TimeStampSerializable foo = new TimeStampSerializable();
		foo.timestamp = new Timestamp(System.currentTimeMillis());
		assertEquals(1, dao.create(foo));

		TimeStampSerializable result = dao.queryForId(foo.id);
		assertEquals(foo.timestamp, result.timestamp);
	}

	@Test
	public void testForeignLoop() throws Exception {
		Dao<ForeignLoop1, Object> dao1 = createDao(ForeignLoop1.class, true);
		Dao<ForeignLoop2, Object> dao2 = createDao(ForeignLoop2.class, true);
		Dao<ForeignLoop3, Object> dao3 = createDao(ForeignLoop3.class, true);
		Dao<ForeignLoop4, Object> dao4 = createDao(ForeignLoop4.class, true);
		ForeignLoop1 loop1 = new ForeignLoop1();
		ForeignLoop2 loop2 = new ForeignLoop2();
		ForeignLoop3 loop3 = new ForeignLoop3();
		ForeignLoop4 loop4 = new ForeignLoop4();
		loop4.stuff = "wow";
		assertEquals(1, dao4.create(loop4));
		loop3.loop = loop4;
		assertEquals(1, dao3.create(loop3));
		loop2.loop = loop3;
		assertEquals(1, dao2.create(loop2));
		loop1.loop = loop2;
		assertEquals(1, dao1.create(loop1));

		ForeignLoop1 result = dao1.queryForId(loop1.id);
		assertNotNull(result);
		assertNotNull(result.loop);
		assertEquals(loop2.id, result.loop.id);
		// level one
		assertNotNull(result.loop.loop);
		assertEquals(loop3.id, result.loop.loop.id);
		assertNotNull(result.loop.loop.loop);
		assertEquals(loop4.id, result.loop.loop.loop.id);
		assertNull(result.loop.loop.loop.stuff);
	}

	@Test
	public void testBaseClassForeignEq() throws Exception {
		Dao<One, Object> oneDao = createDao(One.class, true);
		Dao<ForeignSubClass, Object> foreignDao = createDao(ForeignSubClass.class, true);

		One one1 = new One();
		assertEquals(1, oneDao.create(one1));
		One one2 = new One();
		assertEquals(1, oneDao.create(one2));
		One one3 = new One();
		assertEquals(1, oneDao.create(one3));

		ForeignSubClass fii1 = new ForeignSubClass();
		fii1.one = one1;
		assertEquals(1, foreignDao.create(fii1));
		ForeignSubClass fii2 = new ForeignSubClass();
		fii2.one = one2;
		assertEquals(1, foreignDao.create(fii2));

		List<ForeignSubClass> results = foreignDao.queryBuilder().where().eq(ForeignIntId.FIELD_NAME_ONE, one1).query();
		assertEquals(1, results.size());
		assertEquals(fii1.id, results.get(0).id);

		results = foreignDao.queryBuilder().where().eq(ForeignIntId.FIELD_NAME_ONE, one2).query();
		assertEquals(1, results.size());
		assertEquals(fii2.id, results.get(0).id);

		results = foreignDao.queryBuilder().where().eq(ForeignIntId.FIELD_NAME_ONE, one3).query();
		assertEquals(0, results.size());
	}

	@Test
	public void testObjectFactory() throws Exception {
		Dao<Foo, Object> dao = createDao(Foo.class, true);
		FooFactory fooFactory = new FooFactory();
		dao.setObjectFactory(fooFactory);

		Foo foo = new Foo();
		foo.val = 1231;
		assertEquals(1, dao.create(foo));

		assertEquals(0, fooFactory.fooList.size());
		List<Foo> results = dao.queryForAll();
		assertEquals(1, results.size());
		assertEquals(foo.val, results.get(0).val);
		assertEquals(1, fooFactory.fooList.size());

		results = dao.queryForAll();
		assertEquals(1, results.size());
		assertEquals(foo.val, results.get(0).val);
		assertEquals(2, fooFactory.fooList.size());
	}

	@Test
	public void testQueryRawValue() throws Exception {
		Dao<Foo, Object> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		Foo foo2 = new Foo();
		Foo foo3 = new Foo();
		assertEquals(1, dao.create(foo1));
		assertEquals(foo1.id, dao.queryRawValue("select max(" + Foo.ID_COLUMN_NAME + ") from " + FOO_TABLE_NAME));
		assertEquals(1, dao.create(foo2));
		assertEquals(foo2.id, dao.queryRawValue("select max(" + Foo.ID_COLUMN_NAME + ") from " + FOO_TABLE_NAME));
		assertEquals(1, dao.create(foo3));
		assertEquals(foo3.id, dao.queryRawValue("select max(" + Foo.ID_COLUMN_NAME + ") from " + FOO_TABLE_NAME));

		assertEquals(foo2.id, dao.queryRawValue("select max(" + Foo.ID_COLUMN_NAME + ") from " + FOO_TABLE_NAME
				+ " where " + Foo.ID_COLUMN_NAME + " < ?", Integer.toString(foo3.id)));
		assertEquals(foo1.id, dao.queryRawValue("select max(" + Foo.ID_COLUMN_NAME + ") from " + FOO_TABLE_NAME
				+ " where " + Foo.ID_COLUMN_NAME + " < ?", new SelectArg(SqlType.INTEGER, foo2.id)));
	}

	@Test
	public void testQueryRawValueThrow() throws Exception {
		Dao<Foo, Object> dao = createDao(Foo.class, true);
		DatabaseConnection conn = connectionSource.getReadWriteConnection(FOO_TABLE_NAME);
		try {
			conn.close();
			assertThrowsExactly(SQLException.class, () -> {
				dao.queryRawValue("select max(" + Foo.ID_COLUMN_NAME + ") from " + FOO_TABLE_NAME);
			});
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testQueryRawValueStringsThrow() throws Exception {
		Dao<Foo, Object> dao = createDao(Foo.class, true);
		DatabaseConnection conn = connectionSource.getReadWriteConnection(FOO_TABLE_NAME);
		try {
			conn.close();
			assertThrowsExactly(SQLException.class, () -> {
				dao.queryRawValue("select max(" + Foo.ID_COLUMN_NAME + ") from " + FOO_TABLE_NAME, "1");
			});
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testQueryRawValueArgumentHoldersThrow() throws Exception {
		Dao<Foo, Object> dao = createDao(Foo.class, true);
		DatabaseConnection conn = connectionSource.getReadWriteConnection(FOO_TABLE_NAME);
		try {
			conn.close();
			assertThrowsExactly(SQLException.class, () -> {
				dao.queryRawValue("select max(" + Foo.ID_COLUMN_NAME + ") from " + FOO_TABLE_NAME, new SelectArg());
			});
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testFewFields() throws Exception {
		Dao<FewFields, Object> dao = createDao(FewFields.class, true);
		FewFields few = new FewFields();
		assertEquals(1, dao.create(few));

		FewFields result = dao.queryForId(few.id);
		assertNotNull(result);
	}

	@Test
	public void testCreateCollection() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		int numToCreate = 100;
		List<Foo> fooList = new ArrayList<Foo>(numToCreate);
		for (int i = 0; i < numToCreate; i++) {
			Foo foo = new Foo();
			foo.val = i;
			fooList.add(foo);
		}

		// create them all at once
		assertEquals(numToCreate, dao.create(fooList));

		for (int i = 0; i < numToCreate; i++) {
			Foo result = dao.queryForId(fooList.get(i).id);
			assertEquals(i, result.val);
		}
	}

	@Test
	public void testDaoObserver() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);

		final AtomicInteger changeCount = new AtomicInteger();
		DaoObserver obverver = new DaoObserver() {
			@Override
			public void onChange() {
				changeCount.incrementAndGet();
			}
		};
		dao.registerObserver(obverver);

		assertEquals(0, changeCount.get());
		Foo foo = new Foo();
		foo.val = 21312313;
		assertEquals(1, dao.create(foo));
		assertEquals(1, changeCount.get());

		foo.val = foo.val + 1;
		assertEquals(1, dao.create(foo));
		assertEquals(2, changeCount.get());

		// shouldn't change anything
		dao.queryForAll();
		assertEquals(2, changeCount.get());

		assertEquals(1, dao.delete(foo));
		assertEquals(3, changeCount.get());

		dao.unregisterObserver(obverver);

		assertEquals(1, dao.create(foo));
		// shouldn't change not that we have removed the observer
		assertEquals(3, changeCount.get());
	}

	@Test
	public void testQueryRawDataResultsMapper() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);

		Foo foo1 = new Foo();
		foo1.val = 1389183;
		foo1.stringField = "hoifewhoifeqwih";
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		foo2.val = 133214433;
		foo2.stringField = "hfeopuwgwhiih";
		assertEquals(1, dao.create(foo2));

		GenericRawResults<Foo> results = dao.queryRaw(dao.queryBuilder().prepareStatementString(), new ResultsMapper());
		assertNotNull(results);
		List<Foo> list = results.getResults();
		assertNotNull(list);
		assertEquals(2, list.size());

		assertNotSame(foo1, list.get(0));
		assertEquals(foo1.id, list.get(0).id);
		assertEquals(foo1.val, list.get(0).val);
		assertEquals(foo1.stringField, list.get(0).stringField);

		assertNotSame(foo2, list.get(1));
		assertEquals(foo2.id, list.get(1).id);
		assertEquals(foo2.val, list.get(1).val);
		assertEquals(foo2.stringField, list.get(1).stringField);

		SelectArg selectArg = new SelectArg(SqlType.INTEGER);
		String queryString =
				dao.queryBuilder().where().eq(Foo.ID_COLUMN_NAME, selectArg).queryBuilder().prepareStatementString();
		results = dao.queryRaw(queryString, new ResultsMapper(), Integer.toString(foo1.id));
		list = results.getResults();
		assertEquals(1, list.size());

		assertNotSame(foo1, list.get(0));
		assertEquals(foo1.id, list.get(0).id);
		assertEquals(foo1.val, list.get(0).val);
		assertEquals(foo1.stringField, list.get(0).stringField);

		selectArg.setValue(foo2.id);
		results = dao.queryRaw(queryString, new ResultsMapper(), selectArg);
		list = results.getResults();
		assertEquals(1, list.size());

		assertNotSame(foo2, list.get(0));
		assertEquals(foo2.id, list.get(0).id);
		assertEquals(foo2.val, list.get(0).val);
		assertEquals(foo2.stringField, list.get(0).stringField);
	}

	@Test
	public void testQueryRawDataResultsMapperThrow() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection(FOO_TABLE_NAME);
		try {
			conn.close();
			assertThrowsExactly(SQLException.class, () -> {
				dao.queryRaw(dao.queryBuilder().prepareStatementString(), new ResultsMapper());
			});
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testQueryRawDataResultsMapperStringsThrow() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection(FOO_TABLE_NAME);
		try {
			conn.close();
			assertThrowsExactly(SQLException.class, () -> {
				dao.queryRaw(dao.queryBuilder().prepareStatementString(), new ResultsMapper(), "arg");
			});
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testQueryRawDataResultsMapperArgumentHoldersThrow() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadWriteConnection(FOO_TABLE_NAME);
		try {
			conn.close();
			assertThrowsExactly(SQLException.class, () -> {
				dao.queryRaw(dao.queryBuilder().prepareStatementString(), new ResultsMapper(), new SelectArg());
			});
		} finally {
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testGetTableName() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		assertEquals(FOO_TABLE_NAME, dao.getTableName());
	}

	@Test
	public void testIntegerFieldNull() throws Exception {
		Dao<DateAutoCreate, Integer> dao = createDao(DateAutoCreate.class, true);
		DateAutoCreate foo = new DateAutoCreate();

		Date before = new Date();
		assertEquals(1, dao.create(foo));
		Date after = new Date();

		DateAutoCreate result = dao.queryForId(foo.id);
		assertNotNull(result);

		assertNotNull(result.createdDate);
		// !after the before time means <= before
		assertTrue(!before.after(result.createdDate));
		// !before the after time means >= after
		assertTrue(!after.before(result.createdDate));
	}

	@Test
	public void testNoConstructor() {
		assertThrowsExactly(IllegalArgumentException.class, () -> {
			createDao(NoNoArgConstructor.class, false);
		});
	}

	@Test
	public void testConstruct() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, false);
		Foo foo = dao.createObjectInstance();
		assertNotNull(foo);
	}

	@Test
	public void testInsertObjectWithQuotes() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		foo.stringField = "quotes in here \" and \'";
		assertSame(foo, dao.createIfNotExists(foo));
		List<Foo> results = dao.queryForAll();
		assertEquals(1, results.size());
		assertEquals(foo.stringField, results.get(0).stringField);
	}

	@Test
	public void testInsertDouble() throws Exception {
		Dao<DoubleField, Integer> dao = createDao(DoubleField.class, true);
		DoubleField foo = new DoubleField();
		foo.doubleField = 7.527E7D;
		assertEquals(1, dao.create(foo));
		DoubleField result = getFirstResult(dao);
		assertEquals(foo.doubleField, result.doubleField, 0.0);
		dao.delete(result);

		dao.updateRaw("INSERT INTO DoubleField" + " (" + DoubleField.DOUBLE_FIELD_NAME + ") " + "VALUES " + "(?)",
				((Double) foo.doubleField).toString());
		result = getFirstResult(dao);
		assertEquals(foo.doubleField, result.doubleField, 0.0);
		dao.delete(result);
	}

	/* ============================================================================================== */

	private <T> T getFirstResult(Dao<T, ?> dao) throws SQLException {
		List<T> results = dao.queryForAll();
		assertEquals(1, results.size());
		return results.get(0);
	}

	private static class ResultsMapper implements DatabaseResultsMapper<Foo> {
		@Override
		public Foo mapRow(DatabaseResults databaseResults) throws SQLException {
			Foo foo = new Foo();
			String[] columnNames = databaseResults.getColumnNames();
			for (int i = 0; i < columnNames.length; i++) {
				if (columnNames[i].equalsIgnoreCase(Foo.ID_COLUMN_NAME)) {
					foo.id = databaseResults.getInt(i);
				} else if (columnNames[i].equalsIgnoreCase(Foo.VAL_COLUMN_NAME)) {
					foo.val = databaseResults.getInt(i);
				} else if (columnNames[i].equalsIgnoreCase(Foo.STRING_COLUMN_NAME)) {
					foo.stringField = databaseResults.getString(i);
				}
			}
			return foo;
		}
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

	protected static class UuidId {
		@DatabaseField(id = true)
		public UUID id;
		@DatabaseField
		public String stuff;

		public UuidId() {
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

	protected static class ForeignAutoRefreshFalse {
		@DatabaseField(generatedId = true)
		public int id;
		@DatabaseField(foreign = true, foreignAutoRefresh = false)
		public Foo foo;

		public ForeignAutoRefreshFalse() {
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

	protected static class DoubleUniqueCombo {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(uniqueCombo = true)
		String stuff;
		@DatabaseField(uniqueCombo = true)
		String uniqueStuff;
	}

	protected static class NoId {
		@DatabaseField
		String stuff;
	}

	protected static class CreateOrUpdateObjectId {
		@DatabaseField(generatedId = true)
		public Integer id;
		@DatabaseField
		public String stuff;

		public CreateOrUpdateObjectId() {
		}
	}

	protected static class ColumnDefinition {
		@DatabaseField(generatedId = true)
		public int id;
		@DatabaseField(columnDefinition = "VARCHAR(200)")
		public String stuff;

		public ColumnDefinition() {
		}
	}

	protected static class TwoDates {
		public static final String FIELD_NAME_DATE1 = "date1";
		public static final String FIELD_NAME_DATE2 = "date2";
		@DatabaseField(generatedId = true)
		public int id;
		@DatabaseField(columnName = FIELD_NAME_DATE1)
		public Date date1;
		@DatabaseField(columnName = FIELD_NAME_DATE2)
		public Date date2;

		public TwoDates() {
		}
	}

	protected static class ForeignIntId {
		public static final String FIELD_NAME_ONE = "one";
		@DatabaseField(generatedId = true)
		public int id;
		@DatabaseField(foreign = true, columnName = FIELD_NAME_ONE)
		public One one;

		public ForeignIntId() {
		}
	}

	@DatabaseTable(tableName = "entity")
	protected static class BaseEntity {
		@DatabaseField(generatedId = true)
		public int id;
		@DatabaseField
		public String stuff;

		public BaseEntity() {
		}
	}

	@DatabaseTable(tableName = "entity")
	protected static class SubEntity extends BaseEntity {
		@DatabaseField
		public String otherStuff;

		public SubEntity() {
		}
	}

	protected static class ForeignColumnName {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(foreign = true, foreignColumnName = ForeignColumnNameForeign.FIELD_NAME)
		ForeignColumnNameForeign foreign;

		public ForeignColumnName() {
		}
	}

	protected static class ForeignColumnNameForeign {
		public static final String FIELD_NAME = "name";
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		String name;

		public ForeignColumnNameForeign() {
		}
	}

	protected static class Jeremy1 {
		@DatabaseField(generatedId = true)
		int id;
		@ForeignCollectionField(eager = false)
		ForeignCollection<Jeremy2> bars;

		public Jeremy1() {
		}
	}

	protected static class Jeremy2 {
		public static final String LOOP1_COLUMN_NAME = "loop1";
		public static final String OTHER_COLUMN_NAME = "other";
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(columnName = LOOP1_COLUMN_NAME, foreign = true)
		Jeremy1 loop1;
		@DatabaseField(columnName = OTHER_COLUMN_NAME)
		String other;

		public Jeremy2() {
		}
	}

	protected static class ForeignCollectionComparison {
		@DatabaseField(generatedId = true)
		int id;
		@ForeignCollectionField(eager = false)
		ForeignCollection<ForeignCollectionComparison2> foos;

		public ForeignCollectionComparison() {
		}
	}

	protected static class ForeignCollectionComparison2 {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(foreign = true)
		ForeignCollectionComparison foreign;

		public ForeignCollectionComparison2() {
		}
	}

	protected static class TimeStampSerializable {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(dataType = DataType.SERIALIZABLE)
		java.sql.Timestamp timestamp;

		public TimeStampSerializable() {
		}
	}

	protected static class ForeignLoop1 {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(foreign = true, foreignAutoRefresh = true, maxForeignAutoRefreshLevel = 2)
		ForeignLoop2 loop;

		public ForeignLoop1() {
		}
	}

	protected static class ForeignLoop2 {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(foreign = true)
		ForeignLoop3 loop;

		public ForeignLoop2() {
		}
	}

	protected static class ForeignLoop3 {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(foreign = true)
		ForeignLoop4 loop;

		public ForeignLoop3() {
		}
	}

	protected static class ForeignLoop4 {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		String stuff;

		public ForeignLoop4() {
		}
	}

	protected static class FewFields {
		@DatabaseField(generatedId = true)
		int id;

		public FewFields() {
		}
	}

	protected static class ForeignSubClass extends ForeignIntId {
	}

	protected static class DateAutoCreate {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL", readOnly = true,
				canBeNull = false)
		Date createdDate;

		public DateAutoCreate() {
		}
	}

	protected static class NoNoArgConstructor {
		public NoNoArgConstructor(String stuff) {
		}
	}

	protected static class TableExists {
		@DatabaseField(generatedId = true)
		int id;

		public TableExists() {
		}
	}

	protected static class RawResult {
		@DatabaseField(generatedId = true)
		public int id;
		@DatabaseField
		public int val;

		public RawResult() {
		}
	}

	protected static class DoubleField {

		static final String DOUBLE_FIELD_NAME = "doubleField";

		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(columnName = DOUBLE_FIELD_NAME)
		double doubleField;

		public DoubleField() {
		}
	}

	public static class FooFactory implements ObjectFactory<Foo> {

		final List<Foo> fooList = new ArrayList<Foo>();

		@Override
		public Foo createObject(Constructor<Foo> construcor, Class<Foo> dataClass) {
			Foo foo = new Foo();
			fooList.add(foo);
			return foo;
		}
	}
}
