package com.j256.ormlite.table;

import static org.easymock.EasyMock.anyBoolean;
import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.h2.H2DatabaseType;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;

public class TableUtilsTest extends BaseCoreTest {

	@Test
	public void testConstructor() throws Exception {
		@SuppressWarnings("rawtypes")
		Constructor[] constructors = TableUtils.class.getDeclaredConstructors();
		assertEquals(1, constructors.length);
		constructors[0].setAccessible(true);
		constructors[0].newInstance();
	}

	@Test
	public void testCreateStatements() throws Exception {
		List<String> stmts = TableUtils.getCreateTableStatements(connectionSource, LocalFoo.class);
		assertEquals(1, stmts.size());
		assertEquals(expectedCreateStatement(), stmts.get(0));
	}

	@Test
	public void testCreateStatementsTableConfig() throws Exception {
		List<String> stmts = TableUtils.getCreateTableStatements(connectionSource,
				DatabaseTableConfig.fromClass(connectionSource, LocalFoo.class));
		assertEquals(1, stmts.size());
		assertEquals(expectedCreateStatement(), stmts.get(0));
	}

	@Test
	public void testCreateTableQueriesAfter() throws Exception {
		final String queryAfter = "SELECT * from foo";
		DatabaseType databaseType = new H2DatabaseType() {
			@Override
			public void appendColumnArg(String tableName, StringBuilder sb, FieldType fieldType,
					List<String> additionalArgs, List<String> statementsBefore, List<String> statementsAfter,
					List<String> queriesAfter) throws SQLException {
				super.appendColumnArg(tableName, sb, fieldType, additionalArgs, statementsBefore, statementsAfter,
						queriesAfter);
				if (fieldType.getColumnName().equals(LocalFoo.ID_FIELD_NAME)) {
					queriesAfter.add(queryAfter);
				}
			}
		};
		final ConnectionSource connectionSource = createMock(ConnectionSource.class);
		testCreate("localfoo", connectionSource, databaseType, 0, false, queryAfter, new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return TableUtils.createTable(connectionSource, LocalFoo.class);
			}
		});
	}

	@Test(expected = SQLException.class)
	public void testCreateTableThrow() throws Exception {
		final ConnectionSource connectionSource = createMock(ConnectionSource.class);
		testCreate("localfoo", connectionSource, databaseType, 1, true, null, new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return TableUtils.createTable(connectionSource, LocalFoo.class);
			}
		});
	}

	@Test(expected = SQLException.class)
	public void testCreateTableAboveZero() throws Exception {
		final ConnectionSource connectionSource = createMock(ConnectionSource.class);
		testCreate("localfoo", connectionSource, databaseType, 1, false, null, new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return TableUtils.createTable(connectionSource, LocalFoo.class);
			}
		});
	}

	@Test(expected = SQLException.class)
	public void testCreateTableBelowZero() throws Exception {
		final ConnectionSource connectionSource = createMock(ConnectionSource.class);
		testCreate("localfoo", connectionSource, databaseType, -1, false, null, new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return TableUtils.createTable(connectionSource, LocalFoo.class);
			}
		});
	}

	@Test
	public void testCreateTableTableConfig() throws Exception {
		final ConnectionSource connectionSource = createMock(ConnectionSource.class);
		testCreate("localfoo", connectionSource, databaseType, 0, false, null, new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return (int) TableUtils.createTable(connectionSource,
						DatabaseTableConfig.fromClass(connectionSource, LocalFoo.class));
			}
		});
	}

	@Test
	public void testDropTable() throws Exception {
		final ConnectionSource connectionSource = createMock(ConnectionSource.class);
		testDrop("localfoo", connectionSource, 0, false, new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return (int) TableUtils.dropTable(connectionSource, LocalFoo.class, false);
			}
		});
	}

	@Test(expected = SQLException.class)
	public void testDropTableThrow() throws Exception {
		final ConnectionSource connectionSource = createMock(ConnectionSource.class);
		testDrop("localfoo", connectionSource, 0, true, new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return (int) TableUtils.dropTable(connectionSource, LocalFoo.class, false);
			}
		});
	}

	@Test
	public void testDropTableThrowIgnore() throws Exception {
		final ConnectionSource connectionSource = createMock(ConnectionSource.class);
		testDrop("localfoo", connectionSource, 0, true, new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return (int) TableUtils.dropTable(connectionSource, LocalFoo.class, true);
			}
		});
	}

	@Test(expected = SQLException.class)
	public void testDropTableNegRows() throws Exception {
		final ConnectionSource connectionSource = createMock(ConnectionSource.class);
		testDrop("localfoo", connectionSource, -1, false, new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return (int) TableUtils.dropTable(connectionSource, LocalFoo.class, false);
			}
		});
	}

	@Test
	public void testDropTableTableConfig() throws Exception {
		final ConnectionSource connectionSource = createMock(ConnectionSource.class);
		testDrop("localfoo", connectionSource, 0, false, new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return (int) TableUtils.dropTable(connectionSource,
						DatabaseTableConfig.fromClass(connectionSource, LocalFoo.class), false);
			}
		});
	}

	@Test
	public void testIndex() throws Exception {
		final ConnectionSource connectionSource = createMock(ConnectionSource.class);
		expect(connectionSource.getDatabaseType()).andReturn(databaseType).anyTimes();
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		expect(connectionSource.getReadWriteConnection("index")).andReturn(conn);
		final CompiledStatement stmt = createMock(CompiledStatement.class);
		expect(conn.compileStatement(isA(String.class), isA(StatementType.class), isA(FieldType[].class), anyInt(),
				anyBoolean())).andAnswer(new IAnswer<CompiledStatement>() {
					private int stmtC = 0;

					@Override
					public CompiledStatement answer() {
						Object[] args = EasyMock.getCurrentArguments();
						assertNotNull(args);
						assertEquals("was expecting a call with 5 args", 5, args.length);
						if (stmtC == 0) {
							assertEquals("CREATE TABLE `index` (`stuff` VARCHAR(255) ) ", args[0]);
						} else if (stmtC == 1) {
							assertEquals("CREATE INDEX `index_stuff_idx` ON `index` ( `stuff` )", args[0]);
						} else if (stmtC == 2) {
							assertEquals("DROP INDEX `index_stuff_idx`", args[0]);
						} else if (stmtC == 3) {
							assertEquals("DROP TABLE `index` ", args[0]);
						} else {
							fail("Should only be called 4 times");
						}
						stmtC++;
						assertEquals(StatementType.EXECUTE, args[1]);
						assertEquals(0, ((FieldType[]) args[2]).length);
						return stmt;
					}
				}).anyTimes();
		expect(stmt.runExecute()).andReturn(0).anyTimes();
		connectionSource.releaseConnection(conn);
		expect(connectionSource.getReadWriteConnection("index")).andReturn(conn);
		connectionSource.releaseConnection(conn);
		expectLastCall().anyTimes();
		stmt.close();
		expectLastCall().anyTimes();
		replay(connectionSource, conn, stmt);
		TableUtils.createTable(connectionSource, Index.class);
		TableUtils.dropTable(connectionSource, Index.class, true);
		verify(connectionSource, conn, stmt);
	}

	@Test
	public void testComboIndex() throws Exception {
		final ConnectionSource connectionSource = createMock(ConnectionSource.class);
		expect(connectionSource.getDatabaseType()).andReturn(databaseType).anyTimes();
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		expect(connectionSource.getReadWriteConnection("comboindex")).andReturn(conn);
		final CompiledStatement stmt = createMock(CompiledStatement.class);
		expect(conn.compileStatement(isA(String.class), isA(StatementType.class), isA(FieldType[].class), anyInt(),
				anyBoolean())).andAnswer(new IAnswer<CompiledStatement>() {
					private int stmtC = 0;

					@Override
					public CompiledStatement answer() {
						Object[] args = EasyMock.getCurrentArguments();
						assertNotNull(args);
						assertEquals("was expecting a call with 5 args", 5, args.length);
						if (stmtC == 0) {
							assertEquals("CREATE TABLE `comboindex` (`stuff` VARCHAR(255) , `junk` BIGINT ) ", args[0]);
						} else if (stmtC == 1) {
							assertEquals(
									"CREATE INDEX `" + ComboIndex.INDEX_NAME + "` ON `comboindex` ( `stuff`, `junk` )",
									args[0]);
						} else if (stmtC == 2) {
							assertEquals("DROP INDEX `" + ComboIndex.INDEX_NAME + "`", args[0]);
						} else if (stmtC == 3) {
							assertEquals("DROP TABLE `comboindex` ", args[0]);
						} else {
							fail("Should only be called 4 times");
						}
						stmtC++;
						assertEquals(StatementType.EXECUTE, args[1]);
						assertEquals(0, ((FieldType[]) args[2]).length);
						return stmt;
					}
				}).anyTimes();
		expect(stmt.runExecute()).andReturn(0).anyTimes();
		connectionSource.releaseConnection(conn);
		expect(connectionSource.getReadWriteConnection("comboindex")).andReturn(conn);
		connectionSource.releaseConnection(conn);
		expectLastCall().anyTimes();
		stmt.close();
		expectLastCall().anyTimes();
		replay(connectionSource, conn, stmt);
		TableUtils.createTable(connectionSource, ComboIndex.class);
		TableUtils.dropTable(connectionSource, ComboIndex.class, false);
		verify(connectionSource, conn, stmt);
	}

	@Test
	public void testUniqueIndex() throws Exception {
		final ConnectionSource connectionSource = createMock(ConnectionSource.class);
		expect(connectionSource.getDatabaseType()).andReturn(databaseType).anyTimes();
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		expect(connectionSource.getReadWriteConnection("uniqueindex")).andReturn(conn);
		final CompiledStatement stmt = createMock(CompiledStatement.class);
		expect(conn.compileStatement(isA(String.class), isA(StatementType.class), isA(FieldType[].class), anyInt(),
				anyBoolean())).andAnswer(new IAnswer<CompiledStatement>() {
					private int stmtC = 0;

					@Override
					public CompiledStatement answer() {
						Object[] args = EasyMock.getCurrentArguments();
						assertNotNull(args);
						assertEquals("was expecting a call with 5 args", 5, args.length);
						if (stmtC == 0) {
							assertEquals("CREATE TABLE `uniqueindex` (`stuff` VARCHAR(255) ) ", args[0]);
						} else if (stmtC == 1) {
							assertEquals("CREATE UNIQUE INDEX `uniqueindex_stuff_idx` ON `uniqueindex` ( `stuff` )",
									args[0]);
						} else if (stmtC == 2) {
							assertEquals("DROP INDEX `uniqueindex_stuff_idx`", args[0]);
						} else if (stmtC == 3) {
							assertEquals("DROP TABLE `uniqueindex` ", args[0]);
						} else {
							fail("Should only be called 4 times");
						}
						stmtC++;
						assertEquals(StatementType.EXECUTE, args[1]);
						assertEquals(0, ((FieldType[]) args[2]).length);
						return stmt;
					}
				}).anyTimes();
		expect(stmt.runExecute()).andReturn(0).anyTimes();
		connectionSource.releaseConnection(conn);
		expect(connectionSource.getReadWriteConnection("uniqueindex")).andReturn(conn);
		connectionSource.releaseConnection(conn);
		expectLastCall().anyTimes();
		stmt.close();
		expectLastCall().anyTimes();
		replay(connectionSource, conn, stmt);
		TableUtils.createTable(connectionSource, UniqueIndex.class);
		TableUtils.dropTable(connectionSource, UniqueIndex.class, false);
		verify(connectionSource, conn, stmt);
	}

	@Test
	public void testMissingCreate() throws Exception {
		Dao<LocalFoo, Integer> fooDao = createDao(LocalFoo.class, false);
		try {
			fooDao.queryForAll();
			fail("Should have thrown");
		} catch (SQLException e) {
			// expected
		}
	}

	@Test
	public void testCreateTable() throws Exception {
		Dao<LocalFoo, Integer> fooDao = createDao(LocalFoo.class, false);
		// first we create the table
		createTable(LocalFoo.class, false);
		// test it out
		assertEquals(0, fooDao.queryForAll().size());
		// now we drop it
		dropTable(LocalFoo.class, true);
		try {
			fooDao.countOf();
			fail("Was expecting a SQL exception");
		} catch (Exception expected) {
			// expected
		}
		// now create it again
		createTable(LocalFoo.class, false);
		assertEquals(0, fooDao.queryForAll().size());
		dropTable(LocalFoo.class, true);
	}

	@Test
	public void testDropThenQuery() throws Exception {
		Dao<LocalFoo, Integer> fooDao = createDao(LocalFoo.class, true);
		assertEquals(0, fooDao.queryForAll().size());
		dropTable(LocalFoo.class, true);
		try {
			fooDao.queryForAll();
			fail("Should have thrown");
		} catch (SQLException e) {
			// expected
		}
	}

	@Test
	public void testRawExecuteDropThenQuery() throws Exception {
		Dao<LocalFoo, Integer> fooDao = createDao(LocalFoo.class, true);
		StringBuilder sb = new StringBuilder();
		sb.append("DROP TABLE ");
		if (databaseType.isEntityNamesMustBeUpCase()) {
			databaseType.appendEscapedEntityName(sb, "LOCALFOO");
		} else {
			databaseType.appendEscapedEntityName(sb, "LocalFoo");
		}
		// can't check the return value because of sql-server
		fooDao.executeRaw(sb.toString());
		try {
			fooDao.queryForAll();
			fail("Should have thrown");
		} catch (SQLException e) {
			// expected
		}
	}

	@Test
	public void testDoubleDrop() throws Exception {
		Dao<LocalFoo, Integer> fooDao = createDao(LocalFoo.class, false);
		// first we create the table
		createTable(LocalFoo.class, false);
		// test it out
		assertEquals(0, fooDao.queryForAll().size());
		// now we drop it
		dropTable(LocalFoo.class, true);
		try {
			// this should fail
			dropTable(LocalFoo.class, false);
			fail("Should have thrown");
		} catch (SQLException e) {
			// expected
		}
	}

	@Test
	public void testClearTable() throws Exception {
		Dao<LocalFoo, Integer> fooDao = createDao(LocalFoo.class, true);
		assertEquals(0, fooDao.countOf());
		LocalFoo foo = new LocalFoo();
		assertEquals(1, fooDao.create(foo));
		assertEquals(1, fooDao.countOf());
		TableUtils.clearTable(connectionSource, LocalFoo.class);
		assertEquals(0, fooDao.countOf());
	}

	@Test
	public void testCreateTableIfNotExists() throws Exception {
		dropTable(LocalFoo.class, true);
		Dao<LocalFoo, Integer> fooDao = createDao(LocalFoo.class, false);
		try {
			fooDao.countOf();
			fail("Should have thrown an exception");
		} catch (Exception e) {
			// ignored
		}
		TableUtils.createTableIfNotExists(connectionSource, LocalFoo.class);
		assertEquals(0, fooDao.countOf());
		// should not throw
		TableUtils.createTableIfNotExists(connectionSource, LocalFoo.class);
		assertEquals(0, fooDao.countOf());
	}

	@Test
	public void testCreateTableConfigIfNotExists() throws Exception {
		dropTable(LocalFoo.class, true);
		Dao<LocalFoo, Integer> fooDao = createDao(LocalFoo.class, false);
		try {
			fooDao.countOf();
			fail("Should have thrown an exception");
		} catch (Exception e) {
			// ignored
		}
		DatabaseTableConfig<LocalFoo> tableConfig = DatabaseTableConfig.fromClass(connectionSource, LocalFoo.class);
		TableUtils.createTableIfNotExists(connectionSource, tableConfig);
		assertEquals(0, fooDao.countOf());
		// should not throw
		TableUtils.createTableIfNotExists(connectionSource, tableConfig);
		assertEquals(0, fooDao.countOf());
	}

	/* ================================================================ */

	private void testCreate(String tableName, ConnectionSource connectionSource, DatabaseType databaseType, int rowN,
			boolean throwExecute, String queryAfter, Callable<Integer> callable) throws Exception {
		testStatement(tableName, connectionSource, databaseType, expectedCreateStatement(), queryAfter, rowN,
				throwExecute, callable);
	}

	private String expectedCreateStatement() {
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE ");
		databaseType.appendEscapedEntityName(sb, "localfoo");
		sb.append(" (");
		databaseType.appendEscapedEntityName(sb, LocalFoo.ID_FIELD_NAME);
		sb.append(" INTEGER , ");
		databaseType.appendEscapedEntityName(sb, LocalFoo.NAME_FIELD_NAME);
		sb.append(" VARCHAR(255) ) ");
		return sb.toString();
	}

	private void testDrop(String tableName, ConnectionSource connectionSource, int rowN, boolean throwExecute,
			Callable<Integer> callable) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("DROP TABLE ");
		databaseType.appendEscapedEntityName(sb, "foo");
		sb.append(' ');
		testStatement(tableName, connectionSource, databaseType, sb.toString(), null, rowN, throwExecute, callable);
	}

	private void testStatement(String tableName, ConnectionSource connectionSource, DatabaseType databaseType,
			String statement, String queryAfter, int rowN, boolean throwExecute, Callable<Integer> callable)
					throws Exception {
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		CompiledStatement stmt = createMock(CompiledStatement.class);
		DatabaseResults results = null;
		final AtomicInteger rowC = new AtomicInteger(1);
		if (throwExecute) {
			expect(conn.compileStatement(isA(String.class), isA(StatementType.class), isA(FieldType[].class), anyInt(),
					anyBoolean())).andThrow(new SQLException("you asked us to!!"));
		} else {
			expect(conn.compileStatement(isA(String.class), isA(StatementType.class), isA(FieldType[].class), anyInt(),
					anyBoolean())).andReturn(stmt);
			expect(stmt.runExecute()).andReturn(rowN);
			stmt.close();
			if (queryAfter != null) {
				expect(conn.compileStatement(isA(String.class), isA(StatementType.class), isA(FieldType[].class),
						anyInt(), anyBoolean())).andReturn(stmt);
				results = createMock(DatabaseResults.class);
				expect(results.first()).andReturn(false);
				expect(stmt.runQuery(null)).andReturn(results);
				stmt.close();
				replay(results);
				rowC.incrementAndGet();
			}
		}
		expect(connectionSource.getDatabaseType()).andReturn(databaseType).anyTimes();
		expect(connectionSource.getReadWriteConnection(tableName)).andReturn(conn);
		connectionSource.releaseConnection(conn);
		replay(connectionSource, conn, stmt);
		// we have to store the value since we count the number of rows in the rowC while call() is happening
		assertEquals((Integer) rowC.get(), callable.call());
		verify(connectionSource, conn, stmt);
		if (queryAfter != null) {
			verify(results);
		}
	}

	protected static class LocalFoo {
		public static final String ID_FIELD_NAME = "id";
		public static final String NAME_FIELD_NAME = "name";
		@DatabaseField(columnName = ID_FIELD_NAME)
		int id;
		@DatabaseField(columnName = NAME_FIELD_NAME)
		String name;
	}

	protected static class Index {
		@DatabaseField(index = true)
		String stuff;

		public Index() {
		}
	}

	protected static class ComboIndex {
		@DatabaseField(indexName = INDEX_NAME)
		String stuff;
		@DatabaseField(indexName = INDEX_NAME)
		long junk;

		public ComboIndex() {
		}

		public static final String INDEX_NAME = "stuffjunk";
	}

	protected static class UniqueIndex {
		@DatabaseField(uniqueIndex = true)
		String stuff;

		public UniqueIndex() {
		}
	}
}
