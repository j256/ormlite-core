package com.j256.ormlite.table;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.FieldType;
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
		List<String> stmts = TableUtils.getCreateTableStatements(connectionSource, Foo.class);
		assertEquals(1, stmts.size());
		assertEquals(expectedCreateStatement(), stmts.get(0));
	}

	@Test
	public void testCreateStatementsTableConfig() throws Exception {
		List<String> stmts =
				TableUtils.getCreateTableStatements(connectionSource,
						DatabaseTableConfig.fromClass(databaseType, Foo.class));
		assertEquals(1, stmts.size());
		assertEquals(expectedCreateStatement(), stmts.get(0));
	}

	@Test
	public void testCreateTable() throws Exception {
		final ConnectionSource connectionSource = createMock(ConnectionSource.class);
		testCreate(connectionSource, databaseType, 0, false, null, new Callable<Integer>() {
			public Integer call() throws Exception {
				return TableUtils.createTable(connectionSource, Foo.class);
			}
		});
	}

	@Test
	public void testCreateTableQueriesAfter() throws Exception {
		final String queryAfter = "SELECT * from foo";
		DatabaseType databaseType = new StubDatabaseType() {
			@Override
			public void appendColumnArg(StringBuilder sb, FieldType fieldType, List<String> additionalArgs,
					List<String> statementsBefore, List<String> statementsAfter, List<String> queriesAfter) {
				super.appendColumnArg(sb, fieldType, additionalArgs, statementsBefore, statementsAfter, queriesAfter);
				if (fieldType.getDbColumnName().equals(Foo.ID_FIELD_NAME)) {
					queriesAfter.add(queryAfter);
				}
			}
		};
		final ConnectionSource connectionSource = createMock(ConnectionSource.class);
		testCreate(connectionSource, databaseType, 0, false, queryAfter, new Callable<Integer>() {
			public Integer call() throws Exception {
				return TableUtils.createTable(connectionSource, Foo.class);
			}
		});
	}

	@Test(expected = SQLException.class)
	public void testCreateTableThrow() throws Exception {
		final ConnectionSource connectionSource = createMock(ConnectionSource.class);
		testCreate(connectionSource, databaseType, 1, true, null, new Callable<Integer>() {
			public Integer call() throws Exception {
				return TableUtils.createTable(connectionSource, Foo.class);
			}
		});
	}

	@Test(expected = SQLException.class)
	public void testCreateTableAboveZero() throws Exception {
		final ConnectionSource connectionSource = createMock(ConnectionSource.class);
		testCreate(connectionSource, databaseType, 1, false, null, new Callable<Integer>() {
			public Integer call() throws Exception {
				return TableUtils.createTable(connectionSource, Foo.class);
			}
		});
	}

	@Test(expected = SQLException.class)
	public void testCreateTableBelowZero() throws Exception {
		final ConnectionSource connectionSource = createMock(ConnectionSource.class);
		testCreate(connectionSource, databaseType, -1, false, null, new Callable<Integer>() {
			public Integer call() throws Exception {
				return TableUtils.createTable(connectionSource, Foo.class);
			}
		});
	}

	@Test
	public void testCreateTableTableConfig() throws Exception {
		final ConnectionSource connectionSource = createMock(ConnectionSource.class);
		testCreate(connectionSource, databaseType, 0, false, null, new Callable<Integer>() {
			public Integer call() throws Exception {
				return TableUtils.createTable(connectionSource, DatabaseTableConfig.fromClass(databaseType, Foo.class));
			}
		});
	}

	@Test
	public void testDropTable() throws Exception {
		final ConnectionSource connectionSource = createMock(ConnectionSource.class);
		testDrop(connectionSource, 0, false, new Callable<Integer>() {
			public Integer call() throws Exception {
				return TableUtils.dropTable(connectionSource, Foo.class, false);
			}
		});
	}

	@Test(expected = SQLException.class)
	public void testDropTableThrow() throws Exception {
		final ConnectionSource connectionSource = createMock(ConnectionSource.class);
		testDrop(connectionSource, 0, true, new Callable<Integer>() {
			public Integer call() throws Exception {
				return TableUtils.dropTable(connectionSource, Foo.class, false);
			}
		});
	}

	@Test
	public void testDropTableThrowIgnore() throws Exception {
		final ConnectionSource connectionSource = createMock(ConnectionSource.class);
		testDrop(connectionSource, 0, true, new Callable<Integer>() {
			public Integer call() throws Exception {
				return TableUtils.dropTable(connectionSource, Foo.class, true);
			}
		});
	}

	@Test(expected = SQLException.class)
	public void testDropTableNegRows() throws Exception {
		final ConnectionSource connectionSource = createMock(ConnectionSource.class);
		testDrop(connectionSource, -1, false, new Callable<Integer>() {
			public Integer call() throws Exception {
				return TableUtils.dropTable(connectionSource, Foo.class, false);
			}
		});
	}

	@Test
	public void testDropTableTableConfig() throws Exception {
		final ConnectionSource connectionSource = createMock(ConnectionSource.class);
		testDrop(connectionSource, 0, false, new Callable<Integer>() {
			public Integer call() throws Exception {
				return TableUtils.dropTable(connectionSource, DatabaseTableConfig.fromClass(databaseType, Foo.class),
						false);
			}
		});
	}

	private void testCreate(ConnectionSource connectionSource, DatabaseType databaseType, int rowN,
			boolean throwExecute, String queryAfter, Callable<Integer> callable) throws Exception {
		testStatement(connectionSource, databaseType, expectedCreateStatement(), queryAfter, rowN, throwExecute,
				callable);
	}

	private String expectedCreateStatement() {
		StringBuilder sb = new StringBuilder();
		sb.append("CREATE TABLE ");
		databaseType.appendEscapedEntityName(sb, "foo");
		sb.append(" (");
		databaseType.appendEscapedEntityName(sb, Foo.ID_FIELD_NAME);
		sb.append(" INTEGER , ");
		databaseType.appendEscapedEntityName(sb, Foo.NAME_FIELD_NAME);
		sb.append(" VARCHAR(255) ) ");
		return sb.toString();
	}

	private void testDrop(ConnectionSource connectionSource, int rowN, boolean throwExecute, Callable<Integer> callable)
			throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("DROP TABLE ");
		databaseType.appendEscapedEntityName(sb, "foo");
		sb.append(' ');
		testStatement(connectionSource, databaseType, sb.toString(), null, rowN, throwExecute, callable);
	}

	private void testStatement(ConnectionSource connectionSource, DatabaseType databaseType, String statement,
			String queryAfter, int rowN, boolean throwExecute, Callable<Integer> callable) throws Exception {
		DatabaseConnection conn = createMock(DatabaseConnection.class);
		CompiledStatement stmt = createMock(CompiledStatement.class);
		DatabaseResults results = null;
		final AtomicInteger rowC = new AtomicInteger(1);
		if (throwExecute) {
			expect(conn.compileStatement(statement, StatementType.EXECUTE)).andThrow(
					new SQLException("you asked us to!!"));
		} else {
			expect(conn.compileStatement(statement, StatementType.EXECUTE)).andReturn(stmt);
			expect(stmt.executeUpdate()).andReturn(rowN);
			stmt.close();
			if (queryAfter != null) {
				expect(conn.compileStatement(queryAfter, StatementType.EXECUTE)).andReturn(stmt);
				results = createMock(DatabaseResults.class);
				expect(results.next()).andReturn(false);
				expect(stmt.executeQuery()).andReturn(results);
				stmt.close();
				replay(results);
				rowC.incrementAndGet();
			}
		}
		expect(connectionSource.getDatabaseType()).andReturn(databaseType);
		expect(connectionSource.getReadWriteConnection()).andReturn(conn);
		connectionSource.releaseConnection(conn);
		replay(connectionSource, conn, stmt);
		// we have to store the value since we count the number of rows in the rowC while call() is happening
		assertEquals((Integer) rowC.get(), callable.call());
		verify(connectionSource, conn, stmt);
		if (queryAfter != null) {
			verify(results);
		}
	}

	protected static class Foo {
		public static final String ID_FIELD_NAME = "id";
		public static final String NAME_FIELD_NAME = "name";
		@DatabaseField(columnName = ID_FIELD_NAME)
		int id;
		@DatabaseField(columnName = NAME_FIELD_NAME)
		String name;
	}
}
