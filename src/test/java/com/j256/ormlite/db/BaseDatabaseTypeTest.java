package com.j256.ormlite.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;

import org.junit.Test;

public class BaseDatabaseTypeTest {

	@Test(expected = SQLException.class)
	public void testDriverNotFound() throws SQLException {
		new TestDatabaseType().loadDriver();
	}

	@Test(expected = IllegalStateException.class)
	public void testConfigureGeneratedId() throws SQLException {
		new TestDatabaseType().configureGeneratedId(new StringBuilder(), null, new ArrayList<String>(),
				new ArrayList<String>(), new ArrayList<String>());
	}

	@Test
	public void testSomeCalls() {
		DatabaseType databaseType = new TestDatabaseType();
		assertTrue(databaseType.isNestedSavePointsSupported());
		assertEquals("SELECT 1", databaseType.getPingStatement());
	}

	private class TestDatabaseType extends BaseDatabaseType implements DatabaseType {

		public boolean isDatabaseUrlThisType(String url, String dbTypePart) {
			return false;
		}

		@Override
		protected String getDriverClassName() {
			return "com.class.that.doesnt.exist";
		}

		@Override
		protected String getDatabaseName() {
			return "foo";
		}
	}
}
