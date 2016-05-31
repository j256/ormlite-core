package com.j256.ormlite.stmt.mapped;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.isNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.sql.SQLException;
import java.util.List;

import org.easymock.IAnswer;
import org.easymock.internal.LastControl;
import org.junit.Test;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.db.BaseDatabaseType;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.stmt.BaseCoreStmtTest;
import com.j256.ormlite.stmt.StatementExecutor;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.GeneratedKeyHolder;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.TableInfo;

public class MappedCreateTest extends BaseCoreStmtTest {

	private static final String READ_ONLY_TABLE = "readonly";

	@Test
	public void testGeneratedId() throws Exception {
		TableInfo<GeneratedId, Integer> tableInfo =
				new TableInfo<GeneratedId, Integer>(connectionSource, null, GeneratedId.class);
		StatementExecutor<GeneratedId, Integer> se =
				new StatementExecutor<GeneratedId, Integer>(databaseType, tableInfo, null);
		DatabaseConnection databaseConnection = createMock(DatabaseConnection.class);
		databaseConnection.insert(isA(String.class), isA(Object[].class), isA(FieldType[].class),
				isA(GeneratedKeyHolder.class));
		expectLastCall().andAnswer(new IAnswer<Object>() {
			@Override
			public Integer answer() throws Throwable {
				GeneratedKeyHolder keyHolder = (GeneratedKeyHolder) (LastControl.getCurrentArguments())[3];
				keyHolder.addKey(2);
				return 1;
			}
		});
		replay(databaseConnection);
		GeneratedId genIdSeq = new GeneratedId();
		se.create(databaseConnection, genIdSeq, null);
		verify(databaseConnection);
	}

	@Test
	public void testGeneratedIdSequence() throws Exception {
		DatabaseType databaseType = new NeedsSequenceDatabaseType();
		connectionSource.setDatabaseType(databaseType);
		TableInfo<GeneratedId, Integer> tableInfo =
				new TableInfo<GeneratedId, Integer>(connectionSource, null, GeneratedId.class);
		StatementExecutor<GeneratedId, Integer> se =
				new StatementExecutor<GeneratedId, Integer>(databaseType, tableInfo, null);
		DatabaseConnection databaseConnection = createMock(DatabaseConnection.class);
		expect(databaseConnection.queryForLong(isA(String.class))).andReturn(1L);
		expect(
				databaseConnection.insert(isA(String.class), isA(Object[].class), isA(FieldType[].class),
						(GeneratedKeyHolder) isNull())).andReturn(1);

		replay(databaseConnection);
		GeneratedId genIdSeq = new GeneratedId();
		se.create(databaseConnection, genIdSeq, null);
		verify(databaseConnection);
	}

	@Test
	public void testGeneratedIdSequenceLong() throws Exception {
		DatabaseType databaseType = new NeedsSequenceDatabaseType();
		connectionSource.setDatabaseType(databaseType);
		StatementExecutor<GeneratedIdLong, Long> se =
				new StatementExecutor<GeneratedIdLong, Long>(databaseType, new TableInfo<GeneratedIdLong, Long>(
						connectionSource, null, GeneratedIdLong.class), null);
		DatabaseConnection databaseConnection = createMock(DatabaseConnection.class);
		expect(databaseConnection.queryForLong(isA(String.class))).andReturn(1L);
		expect(
				databaseConnection.insert(isA(String.class), isA(Object[].class), isA(FieldType[].class),
						(GeneratedKeyHolder) isNull())).andReturn(1);

		replay(databaseConnection);
		GeneratedIdLong genIdSeq = new GeneratedIdLong();
		se.create(databaseConnection, genIdSeq, null);
		verify(databaseConnection);
	}

	@Test
	public void testNoCreateSequence() throws Exception {
		MappedCreate.build(databaseType, new TableInfo<GeneratedId, Integer>(connectionSource, null, GeneratedId.class));
	}

	@Test(expected = SQLException.class)
	public void testSequenceZero() throws Exception {
		DatabaseConnection databaseConnection = createMock(DatabaseConnection.class);
		expect(databaseConnection.queryForLong(isA(String.class))).andReturn(0L);
		replay(databaseConnection);
		NeedsSequenceDatabaseType needsSequence = new NeedsSequenceDatabaseType();;
		MappedCreate<GeneratedIdSequence, Integer> mappedCreate =
				MappedCreate.build(needsSequence, new TableInfo<GeneratedIdSequence, Integer>(connectionSource, null,
						GeneratedIdSequence.class));
		mappedCreate.insert(needsSequence, databaseConnection, new GeneratedIdSequence(), null);
		verify(databaseConnection);
	}

	@Test
	public void testCreateReserverdFields() throws Exception {
		Dao<ReservedField, Object> dao = createDao(ReservedField.class, true);
		String from = "from-string";
		ReservedField res = new ReservedField();
		res.from = from;
		dao.create(res);
		int id = res.select;
		ReservedField res2 = dao.queryForId(id);
		assertEquals(id, res2.select);
		String group = "group-string";
		for (ReservedField reserved : dao) {
			assertEquals(from, reserved.from);
			reserved.group = group;
			dao.update(reserved);
		}
		CloseableIterator<ReservedField> reservedIterator = dao.iterator();
		while (reservedIterator.hasNext()) {
			ReservedField reserved = reservedIterator.next();
			assertEquals(from, reserved.from);
			assertEquals(group, reserved.group);
			reservedIterator.remove();
		}
		assertEquals(0, dao.queryForAll().size());
		reservedIterator.close();
	}

	@Test
	public void testCreateReserverdTable() throws Exception {
		Dao<Where, String> dao = createDao(Where.class, true);
		String id = "from-string";
		Where where = new Where();
		where.id = id;
		dao.create(where);
		Where where2 = dao.queryForId(id);
		assertEquals(id, where2.id);
		assertEquals(1, dao.delete(where2));
		assertNull(dao.queryForId(id));
	}

	@Test
	public void testJustIdInsert() throws Exception {
		Dao<JustId, Object> dao = createDao(JustId.class, true);
		JustId foo = new JustId();
		assertEquals(1, dao.create(foo));
		assertEquals(1, dao.refresh(foo));
		assertEquals(0, dao.update(foo));
	}

	@Test
	public void testCreateWithJustGeneratedId() throws Exception {
		Dao<GeneratedId, Integer> dao = createDao(GeneratedId.class, true);
		GeneratedId genId = new GeneratedId();
		dao.create(genId);
		GeneratedId genId2 = dao.queryForId(genId.genId);
		assertEquals(genId.genId, genId2.genId);
	}

	@Test
	public void testCreateWithAllowGeneratedIdInsert() throws Exception {
		Dao<AllowGeneratedIdInsert, Integer> dao = createDao(AllowGeneratedIdInsert.class, true);
		AllowGeneratedIdInsert foo = new AllowGeneratedIdInsert();
		assertEquals(1, dao.create(foo));
		AllowGeneratedIdInsert result = dao.queryForId(foo.id);
		assertNotNull(result);
		assertEquals(foo.id, result.id);

		AllowGeneratedIdInsert foo2 = new AllowGeneratedIdInsert();
		assertEquals(1, dao.create(foo2));
		result = dao.queryForId(foo2.id);
		assertNotNull(result);
		assertEquals(foo2.id, result.id);
		assertFalse(foo2.id == foo.id);

		AllowGeneratedIdInsert foo3 = new AllowGeneratedIdInsert();
		foo3.id = 10002;
		assertEquals(1, dao.create(foo3));
		result = dao.queryForId(foo3.id);
		assertNotNull(result);
		assertEquals(foo3.id, result.id);
		assertFalse(foo3.id == foo.id);
		assertFalse(foo3.id == foo2.id);
	}

	@Test
	public void testCreateWithAllowGeneratedIdInsertObject() throws Exception {
		Dao<AllowGeneratedIdInsertObject, Integer> dao = createDao(AllowGeneratedIdInsertObject.class, true);
		AllowGeneratedIdInsertObject foo = new AllowGeneratedIdInsertObject();
		assertEquals(1, dao.create(foo));
		AllowGeneratedIdInsertObject result = dao.queryForId(foo.id);
		assertNotNull(result);
		assertEquals(foo.id, result.id);

		AllowGeneratedIdInsertObject foo2 = new AllowGeneratedIdInsertObject();
		assertEquals(1, dao.create(foo2));
		result = dao.queryForId(foo2.id);
		assertNotNull(result);
		assertEquals(foo2.id, result.id);
		assertFalse(foo2.id == foo.id);

		AllowGeneratedIdInsertObject foo3 = new AllowGeneratedIdInsertObject();
		foo3.id = 10002;
		assertEquals(1, dao.create(foo3));
		result = dao.queryForId(foo3.id);
		assertNotNull(result);
		assertEquals(foo3.id, result.id);
		assertFalse(foo3.id == foo.id);
		assertFalse(foo3.id == foo2.id);
	}

	@Test
	public void testForeignAutoCreate() throws Exception {
		Dao<ForeignAutoCreate, Long> foreignAutoCreateDao = createDao(ForeignAutoCreate.class, true);
		Dao<ForeignAutoCreateForeign, Long> foreignAutoCreateForeignDao =
				createDao(ForeignAutoCreateForeign.class, true);

		List<ForeignAutoCreateForeign> results = foreignAutoCreateForeignDao.queryForAll();
		assertEquals(0, results.size());

		ForeignAutoCreateForeign foreign = new ForeignAutoCreateForeign();
		String stuff = "fopewjfpwejfpwjfw";
		foreign.stuff = stuff;

		ForeignAutoCreate foo1 = new ForeignAutoCreate();
		foo1.foreign = foreign;
		assertEquals(1, foreignAutoCreateDao.create(foo1));

		// we should not get something from the other dao
		results = foreignAutoCreateForeignDao.queryForAll();
		assertEquals(1, results.size());
		assertEquals(foreign.id, results.get(0).id);

		// if we get foo back, we should see the foreign-id
		List<ForeignAutoCreate> foreignAutoCreateResults = foreignAutoCreateDao.queryForAll();
		assertEquals(1, foreignAutoCreateResults.size());
		assertEquals(foo1.id, foreignAutoCreateResults.get(0).id);
		assertNotNull(foreignAutoCreateResults.get(0).foreign);
		assertEquals(foo1.foreign.id, foreignAutoCreateResults.get(0).foreign.id);

		// now we create it when the foreign field already has an id set
		ForeignAutoCreate foo2 = new ForeignAutoCreate();
		foo2.foreign = foreign;
		assertEquals(1, foreignAutoCreateDao.create(foo1));

		results = foreignAutoCreateForeignDao.queryForAll();
		// no additional results should be found
		assertEquals(1, results.size());
		assertEquals(foreign.id, results.get(0).id);
	}

	@Test(expected = SQLException.class)
	public void testArgumentHolderDoubleSet() throws Exception {
		TableInfo<Foo, Integer> tableInfo = new TableInfo<Foo, Integer>(connectionSource, null, Foo.class);
		MappedCreate<Foo, Integer> mappedCreate = MappedCreate.build(databaseType, tableInfo);
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		expect(
				conn.insert(isA(String.class), isA(Object[].class), isA(FieldType[].class),
						isA(GeneratedKeyHolder.class))).andAnswer(new IAnswer<Integer>() {
			@Override
			public Integer answer() throws Throwable {
				GeneratedKeyHolder holder = (GeneratedKeyHolder) getCurrentArguments()[3];
				holder.addKey((Integer) 1);
				holder.addKey((Integer) 2);
				return 1;
			}
		});
		replay(conn);
		mappedCreate.insert(databaseType, conn, new Foo(), null);
	}

	@Test(expected = SQLException.class)
	public void testArgumentHolderSetZero() throws Exception {
		TableInfo<Foo, Integer> tableInfo = new TableInfo<Foo, Integer>(connectionSource, null, Foo.class);
		MappedCreate<Foo, Integer> mappedCreate = MappedCreate.build(databaseType, tableInfo);
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		expect(
				conn.insert(isA(String.class), isA(Object[].class), isA(FieldType[].class),
						isA(GeneratedKeyHolder.class))).andAnswer(new IAnswer<Integer>() {
			@Override
			public Integer answer() throws Throwable {
				GeneratedKeyHolder holder = (GeneratedKeyHolder) getCurrentArguments()[3];
				holder.addKey((Integer) 0);
				return 1;
			}
		});
		replay(conn);
		mappedCreate.insert(databaseType, conn, new Foo(), null);
	}

	@Test(expected = SQLException.class)
	public void testArgumentHolderNotSet() throws Exception {
		TableInfo<Foo, Integer> tableInfo = new TableInfo<Foo, Integer>(connectionSource, null, Foo.class);
		MappedCreate<Foo, Integer> mappedCreate = MappedCreate.build(databaseType, tableInfo);
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		expect(
				conn.insert(isA(String.class), isA(Object[].class), isA(FieldType[].class),
						isA(GeneratedKeyHolder.class))).andReturn(1);
		replay(conn);
		mappedCreate.insert(databaseType, conn, new Foo(), null);
	}

	@Test
	public void testReadOnly() throws Exception {
		Dao<ReadOnly, Integer> readOnlyDao = createDao(ReadOnly.class, true);
		Dao<ReadOnlyInsert, Integer> readOnlyInsertDao = createDao(ReadOnlyInsert.class, true);

		ReadOnly readOnly = new ReadOnly();
		readOnly.stuff = "fpweojfew";
		readOnly.readOnly = "read read and only read";
		assertEquals(1, readOnlyDao.create(readOnly));

		ReadOnly result = readOnlyDao.queryForId(readOnly.id);
		assertNotNull(result);
		assertEquals(readOnly.id, result.id);
		assertEquals(readOnly.stuff, result.stuff);
		// this is null because the above create didn't insert it
		assertNull(result.readOnly);

		ReadOnlyInsert insert = new ReadOnlyInsert();
		insert.stuff = "wefewerwrwe";
		insert.readOnly = "insert should work here";
		assertEquals(1, readOnlyInsertDao.create(insert));

		result = readOnlyDao.queryForId(insert.id);
		assertNotNull(result);
		assertEquals(insert.id, result.id);
		assertEquals(insert.stuff, result.stuff);
		// but this is not null because it was inserted using readOnlyInsertDao
		assertEquals(insert.readOnly, result.readOnly);

		ReadOnly update = result;
		update.readOnly = "something else";
		// the update should _not_ update read-only field
		assertEquals(1, readOnlyDao.update(update));

		result = readOnlyDao.queryForId(insert.id);
		assertFalse(update.readOnly.equals(result.readOnly));
	}

	/* ================================================================================================= */

	private static class GeneratedId {
		@DatabaseField(generatedId = true)
		public int genId;
		@DatabaseField
		public String stuff;
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

	protected static class JustId {
		@DatabaseField(generatedId = true)
		int id;
	}

	protected static class AllowGeneratedIdInsert {
		@DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
		int id;

		@DatabaseField
		String stuff;
	}

	protected static class AllowGeneratedIdInsertObject {
		@DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
		Integer id;

		@DatabaseField
		String stuff;
	}

	protected static class GeneratedIdLong {
		@DatabaseField(generatedId = true)
		long id;
		@DatabaseField
		public String stuff;
	}

	protected static class GeneratedIdSequence {
		@DatabaseField(generatedIdSequence = "seq")
		int id;
		@DatabaseField
		public String stuff;
	}

	protected static class ForeignAutoCreate {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(foreign = true, foreignAutoCreate = true)
		public ForeignAutoCreateForeign foreign;
	}

	protected static class ForeignAutoCreateForeign {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		String stuff;
	}

	@DatabaseTable(tableName = READ_ONLY_TABLE)
	protected static class ReadOnly {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		String stuff;
		@DatabaseField(readOnly = true)
		String readOnly;
	}

	@DatabaseTable(tableName = READ_ONLY_TABLE)
	protected static class ReadOnlyInsert {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		String stuff;
		@DatabaseField
		String readOnly;
	}

	private static class NeedsSequenceDatabaseType extends BaseDatabaseType {
		@Override
		public String getDriverClassName() {
			return "foo.bar.baz";
		}
		@Override
		public String getDatabaseName() {
			return "fake";
		}
		@Override
		public boolean isDatabaseUrlThisType(String url, String dbTypePart) {
			return false;
		}
		@Override
		public boolean isIdSequenceNeeded() {
			return true;
		}
		@Override
		public boolean isSelectSequenceBeforeInsert() {
			return true;
		}
	}
}
