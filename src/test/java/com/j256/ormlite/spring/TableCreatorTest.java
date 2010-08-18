package com.j256.ormlite.spring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.j256.ormlite.BaseOrmLiteTest;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;

public class TableCreatorTest extends BaseOrmLiteTest {

	@Test
	public void testInitialize() throws Exception {
		Dao<Foo, Object> fooDao = createDao(Foo.class, false);
		try {
			fooDao.create(new Foo());
			fail("Should have thrown an exception");
		} catch (SQLException e) {
			// expected
		}

		TableCreator tableCreator = new TableCreator();
		tableCreator.setDatabaseType(databaseType);
		tableCreator.setConnectionSource(connectionSource);

		List<BaseDaoImpl<?, ?>> daoList = new ArrayList<BaseDaoImpl<?, ?>>();
		daoList.add((BaseDaoImpl<?, ?>) fooDao);
		tableCreator.setConfiguredDaos(daoList);
		try {
			System.setProperty(TableCreator.AUTO_CREATE_TABLES, Boolean.TRUE.toString());
			tableCreator.initialize();
		} finally {
			System.clearProperty(TableCreator.AUTO_CREATE_TABLES);
		}

		assertEquals(1, fooDao.create(new Foo()));
		// shouldn't do anything
		tableCreator.destroy();
		assertEquals(1, fooDao.create(new Foo()));

		try {
			System.setProperty(TableCreator.AUTO_DROP_TABLES, Boolean.TRUE.toString());
			tableCreator.destroy();
			fooDao.create(new Foo());
			fail("Should have thrown an exception");
		} catch (SQLException e) {
			// expected
		} finally {
			System.clearProperty(TableCreator.AUTO_DROP_TABLES);
		}
	}

	@Test
	public void testAutoCreateNotSet() throws Exception {
		TableCreator tableCreator = new TableCreator();
		tableCreator.initialize();
	}

	@Test(expected = IllegalStateException.class)
	public void testNoConfiguredDaos() throws Exception {
		TableCreator tableCreator = new TableCreator();
		tableCreator.setDatabaseType(databaseType);
		tableCreator.setConnectionSource(connectionSource);

		try {
			System.setProperty(TableCreator.AUTO_CREATE_TABLES, Boolean.TRUE.toString());
			tableCreator.initialize();
			fail("should not get here");
		} finally {
			System.clearProperty(TableCreator.AUTO_CREATE_TABLES);
		}
	}

	protected static class Foo {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		String stuff;
	}
}
