package com.j256.ormlite.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Locale;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.field.DataPersisterManager;

public class BaseDatabaseTypeTest extends BaseCoreTest {

	@Test(expected = SQLException.class)
	public void testDriverNotFound() throws SQLException {
		new TestDatabaseType().loadDriver();
	}

	@Test(expected = IllegalStateException.class)
	public void testConfigureGeneratedId() {
		new TestDatabaseType().configureGeneratedId(null, new StringBuilder(), null, new ArrayList<String>(), null,
				new ArrayList<String>(), new ArrayList<String>());
	}

	@Test
	public void testSomeCalls() {
		DatabaseType databaseType = new TestDatabaseType();
		assertTrue(databaseType.isNestedSavePointsSupported());
		assertEquals("SELECT 1", databaseType.getPingStatement());
	}

	@Test
	public void testUnknownClass() throws Exception {
		assertNull(DataPersisterManager.lookupForField(SomeFields.class.getDeclaredField("someFields")));
	}

	@Test
	public void testSerializableClass() throws Exception {
		assertNull(DataPersisterManager.lookupForField(SomeFields.class.getDeclaredField("serializable")));
	}

	@Test
	public void testClassLookupByteArray() throws Exception {
		assertNull(DataPersisterManager.lookupForField(SomeFields.class.getDeclaredField("byteArray")));
	}

	@Test
	public void testUppercase() {
		assertEquals("İ", "i".toUpperCase(new Locale("tr", "TR")));
	}

	private static class TestDatabaseType extends BaseDatabaseType {

		@Override
		public boolean isDatabaseUrlThisType(String url, String dbTypePart) {
			return false;
		}

		@Override
		protected String getDriverClassName() {
			return "com.class.that.doesnt.exist";
		}

		@Override
		public String getDatabaseName() {
			return "foo";
		}
	}

	protected static class SomeFields {
		SomeFields someFields;
		Serializable serializable;
		byte[] byteArray;
	}
}
