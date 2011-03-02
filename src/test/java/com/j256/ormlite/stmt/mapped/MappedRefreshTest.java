package com.j256.ormlite.stmt.mapped;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

public class MappedRefreshTest extends BaseCoreTest {

	@Test
	public void testRefreshNotFound() throws Exception {
		Dao<LocalFoo, Object> fooDao = createDao(LocalFoo.class, true);
		LocalFoo foo1 = new LocalFoo();
		foo1.id = "foo";
		// hasn't been created yet
		assertEquals(0, fooDao.refresh(foo1));
		// now it is created
		assertEquals(1, fooDao.create(foo1));
		// now it should refresh
		assertEquals(1, fooDao.refresh(foo1));
	}

	@Test(expected = SQLException.class)
	public void testRefreshDouble() throws Exception {
		// don't try this at home folks

		// don't create the Foo class because it will be created by the FooNotId
		Dao<LocalFoo, Object> fooDao = createDao(LocalFoo.class, false);
		Dao<FooNotId, Object> fooNotIdDao = createDao(FooNotId.class, true);

		LocalFoo foo = new LocalFoo();
		foo.id = "foo";

		// create 1 of them which should work
		assertEquals(1, fooDao.create(foo));
		// refresh should work
		assertEquals(1, fooDao.refresh(foo));

		// behind the scenes, insert another into the foo table, EVIL!!
		FooNotId fooNotId = new FooNotId();
		fooNotId.id = "foo";

		assertEquals(1, fooNotIdDao.create(fooNotId));

		// refresh should not work becaue there are 2 classes which match this id
		fooDao.refresh(foo);
	}

	private final static String FOO_TABLE_NAME = "foo";

	@DatabaseTable(tableName = FOO_TABLE_NAME)
	protected static class LocalFoo {
		@DatabaseField(id = true)
		String id;
	}

	@DatabaseTable(tableName = FOO_TABLE_NAME)
	protected static class FooNotId {
		@DatabaseField
		String id;
	}
}
