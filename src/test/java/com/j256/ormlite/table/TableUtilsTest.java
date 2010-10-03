package com.j256.ormlite.table;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Constructor;
import java.util.List;

import org.junit.Test;

import com.j256.ormlite.BaseOrmLiteCoreTest;
import com.j256.ormlite.field.DatabaseField;

public class TableUtilsTest extends BaseOrmLiteCoreTest {

	@Test
	public void testConstructor() throws Exception {
		@SuppressWarnings("unchecked")
		Constructor[] constructors = TableUtils.class.getDeclaredConstructors();
		assertEquals(1, constructors.length);
		constructors[0].setAccessible(true);
		constructors[0].newInstance();
	}

	@Test
	public void testCreateStatements() throws Exception {
		List<String> stmts = TableUtils.getCreateTableStatements(connectionSource, Foo.class);
		assertEquals(1, stmts.size());
		assertEquals("CREATE TABLE `foo` (`name` VARCHAR(255) ) ", stmts.get(0));
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testCreateStatementsDatabaseType() throws Exception {
		List<String> stmts = TableUtils.getCreateTableStatements(databaseType, Foo.class);
		assertEquals(1, stmts.size());
		assertEquals("CREATE TABLE `foo` (`name` VARCHAR(255) ) ", stmts.get(0));
	}

	protected static class Foo {
		@DatabaseField
		String name;
	}
}
