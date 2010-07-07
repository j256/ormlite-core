package com.j256.ormlite.stmt.mapped;

import static org.junit.Assert.assertEquals;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.j256.ormlite.BaseOrmLiteTest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.TableInfo;

public class MappedPreparedQueryTest extends BaseOrmLiteTest {

	private final static String TABLE_NAME = "tableName";

	@Test
	public void testMapRow() throws Exception {
		Dao<Foo, Object> fooDao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		fooDao.create(foo1);

		TableInfo<Foo> tableInfo = new TableInfo<Foo>(databaseType, Foo.class);
		MappedPreparedQuery<Foo> rowMapper =
				new MappedPreparedQuery<Foo>(tableInfo, null, new ArrayList<FieldType>(),
						Arrays.asList(tableInfo.getFieldTypes()), new ArrayList<SelectArg>(), null);

		PreparedStatement stmt = dataSource.getConnection().prepareStatement("select * from " + TABLE_NAME);
		if (!stmt.execute()) {
			return;
		}

		ResultSet resultSet = stmt.getResultSet();
		while (resultSet.next()) {
			Foo foo2 = rowMapper.mapRow(resultSet, 0);
			assertEquals(foo1.id, foo2.id);
		}
	}

	@Test
	public void testLimit() throws Exception {
		Dao<Foo, Object> fooDao = createDao(Foo.class, true);
		List<Foo> foos = new ArrayList<Foo>();
		Foo foo = new Foo();
		// create foo #1
		fooDao.create(foo);
		foos.add(foo);
		foo = new Foo();
		// create foo #2
		fooDao.create(foo);
		foos.add(foo);

		TableInfo<Foo> tableInfo = new TableInfo<Foo>(databaseType, Foo.class);
		MappedPreparedQuery<Foo> preparedQuery =
				new MappedPreparedQuery<Foo>(tableInfo, "select * from " + TABLE_NAME, new ArrayList<FieldType>(),
						Arrays.asList(tableInfo.getFieldTypes()), new ArrayList<SelectArg>(), 1);

		checkResults(foos, preparedQuery, 1);
		preparedQuery =
				new MappedPreparedQuery<Foo>(tableInfo, "select * from " + TABLE_NAME, new ArrayList<FieldType>(),
						Arrays.asList(tableInfo.getFieldTypes()), new ArrayList<SelectArg>(), null);
		checkResults(foos, preparedQuery, 2);
	}

	private void checkResults(List<Foo> foos, MappedPreparedQuery<Foo> preparedQuery, int expectedNum)
			throws SQLException {
		PreparedStatement stmt = null;
		try {
			stmt = preparedQuery.prepareSqlStatement(dataSource.getConnection());
			if (!stmt.execute()) {
				return;
			}

			ResultSet resultSet = stmt.getResultSet();
			int fooC = 0;
			while (resultSet.next()) {
				Foo foo2 = preparedQuery.mapRow(resultSet, 0);
				assertEquals(foos.get(fooC).id, foo2.id);
				fooC++;
			}
			assertEquals(expectedNum, fooC);
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void testObjectNoConstructor() {
		new MappedPreparedQuery<NoConstructor>(new TableInfo<NoConstructor>(databaseType, NoConstructor.class), null,
				new ArrayList<FieldType>(), new ArrayList<FieldType>(), new ArrayList<SelectArg>(), null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDifferentArgSizes() {
		ArrayList<SelectArg> selectArgList = new ArrayList<SelectArg>();
		selectArgList.add(new SelectArg());
		new MappedPreparedQuery<Foo>(new TableInfo<Foo>(databaseType, Foo.class), null, new ArrayList<FieldType>(),
				new ArrayList<FieldType>(), selectArgList, null);
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class Foo {
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
