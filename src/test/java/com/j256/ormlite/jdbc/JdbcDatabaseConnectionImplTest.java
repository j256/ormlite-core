package com.j256.ormlite.jdbc;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertEquals;

import java.sql.SQLException;

import org.junit.Test;

import com.j256.ormlite.BaseOrmLiteTest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.GeneratedKeyHolder;

public class JdbcDatabaseConnectionImplTest extends BaseOrmLiteTest {

	@Test
	public void testQueryForLong() throws Exception {
		DatabaseConnection databaseConnection = connectionSource.getReadOnlyConnection();
		Dao<Foo, Object> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		long id = 21321321L;
		foo.id = id;
		assertEquals(1, dao.create(foo));
		assertEquals(id, databaseConnection.queryForLong("select id from foo"));
	}

	@Test(expected = SQLException.class)
	public void testQueryForLongNoResult() throws Exception {
		DatabaseConnection databaseConnection = connectionSource.getReadOnlyConnection();
		createDao(Foo.class, true);
		databaseConnection.queryForLong("select id from foo");
	}

	@Test(expected = SQLException.class)
	public void testQueryForLongTooManyResults() throws Exception {
		DatabaseConnection databaseConnection = connectionSource.getReadOnlyConnection();
		Dao<Foo, Object> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		long id = 21321321L;
		foo.id = id;
		// insert twice
		assertEquals(1, dao.create(foo));
		assertEquals(1, dao.create(foo));
		databaseConnection.queryForLong("select id from foo");
	}

	@Test
	public void testQueryKeyHolderNoKeys() throws Exception {
		DatabaseConnection databaseConnection = connectionSource.getReadOnlyConnection();
		createDao(Foo.class, true);
		GeneratedKeyHolder keyHolder = createMock(GeneratedKeyHolder.class);
		databaseConnection.insert("insert into foo (id) values (1)", new Object[0], new SqlType[0], keyHolder);
	}

	protected static class Foo {
		@DatabaseField
		public long id;
		Foo() {
		}
	}
}
