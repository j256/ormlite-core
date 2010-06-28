package com.j256.ormlite.db;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Constructor;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.Test;

public class DatabaseTypeUtilsTest {

	@Test
	public void testConstructor() throws Exception {
		@SuppressWarnings("unchecked")
		Constructor[] constructors = DatabaseTypeUtils.class.getDeclaredConstructors();
		assertEquals(1, constructors.length);
		constructors[0].setAccessible(true);
		constructors[0].newInstance();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testLoadUnknownDriver() throws Exception {
		DatabaseTypeUtils.loadDriver("jdbc:unknown-db:stuff");
	}

	@Test
	public void testLoadDriverOk() throws Exception {
		DatabaseTypeUtils.loadDriver("jdbc:h2:mem:ormlitetest");
	}

	@Test
	public void testSimpleDataSource() throws Exception {
		DatabaseTypeUtils.createSimpleDataSource("jdbc:h2:mem:ormlitetest").getConnection().close();
	}

	@Test(expected = SQLException.class)
	public void testSimpleDataSourceBadDriverArgs() throws Exception {
		DatabaseTypeUtils.createSimpleDataSource("jdbc:h2:").getConnection();
	}

	@Test
	public void testCreateDbType() throws Exception {
		DatabaseTypeUtils.createDatabaseType("jdbc:h2:mem:ormlitetest");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateDbTypeBadDriver() throws Exception {
		DatabaseTypeUtils.createDatabaseType("jdbc:unknown-db:");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateDbTypeBadUrl() throws Exception {
		DatabaseTypeUtils.createDatabaseType("bad-url");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreateDbTypeNotEnoughParts() throws Exception {
		DatabaseTypeUtils.createDatabaseType("jdbc:");
	}

	@Test
	public void testCreateDbTypeDataSource() throws Exception {
		DataSource dataSource = null;
		try {
			dataSource = DatabaseTypeUtils.createSimpleDataSource("jdbc:h2:mem:ormlitetest");
			DatabaseTypeUtils.createDatabaseType(dataSource);
		} finally {
			if (dataSource != null) {
				dataSource.getConnection().close();
			}
		}
	}
}
