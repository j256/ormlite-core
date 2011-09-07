package com.j256.ormlite.stmt.mapped;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.sql.SQLException;

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
import com.j256.ormlite.table.TableInfo;

public class MappedCreateTest extends BaseCoreStmtTest {

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
		expect(databaseConnection.insert(isA(String.class), isA(Object[].class), isA(FieldType[].class))).andReturn(1);

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
		expect(databaseConnection.insert(isA(String.class), isA(Object[].class), isA(FieldType[].class))).andReturn(1);

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

	@Test(expected = SQLException.class)
	public void testJustIdInsert() throws Exception {
		createDao(JustId.class, true);
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

	private static class GeneratedId {
		@DatabaseField(generatedId = true)
		public int genId;
		@SuppressWarnings("unused")
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

	private static class NeedsSequenceDatabaseType extends BaseDatabaseType {
		@Override
		public String getDriverClassName() {
			return "foo.bar.baz";
		}
		public String getDatabaseName() {
			return "fake";
		}
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
