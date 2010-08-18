package com.j256.ormlite.jdbc;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertEquals;

import java.sql.SQLException;

import org.junit.Test;

import com.j256.ormlite.BaseOrmLiteTest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.jdbc.JdbcDatabaseAccess;
import com.j256.ormlite.support.DatabaseAccess;
import com.j256.ormlite.support.GeneratedKeyHolder;

public class JdbcDatabaseAccessImplTest extends BaseOrmLiteTest {

	@Test
	public void testQueryForLong() throws Exception {
		DatabaseAccess template = new JdbcDatabaseAccess(connectionSource);
		Dao<Foo, Object> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		long id = 21321321L;
		foo.id = id;
		assertEquals(1, dao.create(foo));
		assertEquals(id, template.queryForLong("select id from foo"));
	}

	@Test(expected = SQLException.class)
	public void testQueryForLongNoResult() throws Exception {
		DatabaseAccess template = new JdbcDatabaseAccess(connectionSource);
		createDao(Foo.class, true);
		template.queryForLong("select id from foo");
	}

	@Test(expected = SQLException.class)
	public void testQueryForLongTooManyResults() throws Exception {
		DatabaseAccess template = new JdbcDatabaseAccess(connectionSource);
		Dao<Foo, Object> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		long id = 21321321L;
		foo.id = id;
		// insert twice
		assertEquals(1, dao.create(foo));
		assertEquals(1, dao.create(foo));
		template.queryForLong("select id from foo");
	}

	@Test
	public void testQueryKeyHolderNoKeys() throws Exception {
		DatabaseAccess template = new JdbcDatabaseAccess(connectionSource);
		createDao(Foo.class, true);
		GeneratedKeyHolder keyHolder = createMock(GeneratedKeyHolder.class);
		template.insert("insert into foo (id) values (1)", new Object[0], new int[0], keyHolder);
	}

	protected static class Foo {
		@DatabaseField
		public long id;
		Foo() {
		}
	}
}
