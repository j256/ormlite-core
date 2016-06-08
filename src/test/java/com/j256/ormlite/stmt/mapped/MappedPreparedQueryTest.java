package com.j256.ormlite.stmt.mapped;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.misc.IOUtils;
import com.j256.ormlite.stmt.ArgumentHolder;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.TableInfo;

public class MappedPreparedQueryTest extends BaseCoreTest {

	private final static String TABLE_NAME = "tableName";

	@Test
	public void testMapRow() throws Exception {
		Dao<LocalFoo, Object> fooDao = createDao(LocalFoo.class, true);
		LocalFoo foo1 = new LocalFoo();
		fooDao.create(foo1);

		TableInfo<LocalFoo, Integer> tableInfo =
				new TableInfo<LocalFoo, Integer>(connectionSource, null, LocalFoo.class);
		MappedPreparedStmt<LocalFoo, Integer> rowMapper = new MappedPreparedStmt<LocalFoo, Integer>(tableInfo, null,
				new FieldType[0], tableInfo.getFieldTypes(), new ArgumentHolder[0], null, StatementType.SELECT, false);

		DatabaseConnection conn = connectionSource.getReadOnlyConnection(TABLE_NAME);
		CompiledStatement stmt = null;
		try {
			stmt = conn.compileStatement("select * from " + TABLE_NAME, StatementType.SELECT, new FieldType[0],
					DatabaseConnection.DEFAULT_RESULT_FLAGS, true);

			DatabaseResults results = stmt.runQuery(null);
			while (results.next()) {
				LocalFoo foo2 = rowMapper.mapRow(results);
				assertEquals(foo1.id, foo2.id);
			}
		} finally {
			if (stmt != null) {
				stmt.close();
			}
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testLimit() throws Exception {
		Dao<LocalFoo, Object> fooDao = createDao(LocalFoo.class, true);
		List<LocalFoo> foos = new ArrayList<LocalFoo>();
		LocalFoo foo = new LocalFoo();
		// create foo #1
		fooDao.create(foo);
		foos.add(foo);
		foo = new LocalFoo();
		// create foo #2
		fooDao.create(foo);
		foos.add(foo);

		TableInfo<LocalFoo, Integer> tableInfo =
				new TableInfo<LocalFoo, Integer>(connectionSource, null, LocalFoo.class);
		MappedPreparedStmt<LocalFoo, Integer> preparedQuery =
				new MappedPreparedStmt<LocalFoo, Integer>(tableInfo, "select * from " + TABLE_NAME, new FieldType[0],
						tableInfo.getFieldTypes(), new ArgumentHolder[0], 1L, StatementType.SELECT, false);

		checkResults(foos, preparedQuery, 1);
		preparedQuery = new MappedPreparedStmt<LocalFoo, Integer>(tableInfo, "select * from " + TABLE_NAME,
				new FieldType[0], tableInfo.getFieldTypes(), new ArgumentHolder[0], null, StatementType.SELECT, false);
		checkResults(foos, preparedQuery, 2);
	}

	private void checkResults(List<LocalFoo> foos, MappedPreparedStmt<LocalFoo, Integer> preparedQuery, int expectedNum)
			throws SQLException {
		DatabaseConnection conn = connectionSource.getReadOnlyConnection(TABLE_NAME);
		CompiledStatement stmt = null;
		try {
			stmt = preparedQuery.compile(conn, StatementType.SELECT);
			DatabaseResults results = stmt.runQuery(null);
			int fooC = 0;
			while (results.next()) {
				LocalFoo foo2 = preparedQuery.mapRow(results);
				assertEquals(foos.get(fooC).id, foo2.id);
				fooC++;
			}
			assertEquals(expectedNum, fooC);
		} finally {
			IOUtils.closeThrowSqlException(stmt, "compiled statement");
			connectionSource.releaseConnection(conn);
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void testObjectNoConstructor() throws SQLException {
		new MappedPreparedStmt<NoConstructor, Void>(
				new TableInfo<NoConstructor, Void>(connectionSource, null, NoConstructor.class), null, new FieldType[0],
				new FieldType[0], new ArgumentHolder[0], null, StatementType.SELECT, false);
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalFoo {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		String stuff;
	}

	protected static class NoConstructor {
		@DatabaseField
		String id;

		NoConstructor(int someField) {
			// to stop the default no-arg constructor
		}
	}
}
