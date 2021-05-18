package com.j256.ormlite.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.field.DataPersisterManager;

public class BaseDatabaseTypeTest extends BaseCoreTest {

	@Test
	public void testDriverNotFound() {
		assertFalse(new TestDatabaseType().loadDriver());
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
	public void testCoverage() {
		TestDatabaseType databaseType = new TestDatabaseType();
		assertNull(databaseType.getDriverClassName());
		assertFalse(databaseType.isTruncateSupported());
		assertFalse(databaseType.isCreateIfNotExistsSupported());
		assertFalse(databaseType.isSelectSequenceBeforeInsert());
		assertTrue(databaseType.isAllowGeneratedIdInsertSupported());
		assertFalse(databaseType.isSequenceNamesMustBeLowerCase());
		assertEquals(databaseType.isCreateIfNotExistsSupported(), databaseType.isCreateIndexIfNotExistsSupported());
		assertEquals(databaseType.isCreateIfNotExistsSupported(), databaseType.isCreateSchemaIfNotExistsSupported());
	}

	@Test
	public void testAppendEscapedEntityName() {
		StringBuilder sb = new StringBuilder();
		String schema = "sc";
		String table = "table";
		databaseType.appendEscapedEntityName(sb, schema + "." + table);
		assertEquals("`" + schema + "`.`" + table + "`", sb.toString());
	}

	@Test
	public void testGenerateIdSequenceName() {
		TestDatabaseType databaseType = new TestDatabaseType();
		String table = "SOMETABLE";
		assertEquals(table + "_id_seq", databaseType.generateIdSequenceName(table, null));
		databaseType.sequenceNamesMustBeLowerCase = true;
		assertEquals(table.toLowerCase() + "_id_seq", databaseType.generateIdSequenceName(table, null));
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

		Boolean sequenceNamesMustBeLowerCase;

		@Override
		public boolean isDatabaseUrlThisType(String url, String dbTypePart) {
			return false;
		}

		@Override
		public boolean isSequenceNamesMustBeLowerCase() {
			if (sequenceNamesMustBeLowerCase == null) {
				return super.isSequenceNamesMustBeLowerCase();
			} else {
				return sequenceNamesMustBeLowerCase;
			}
		}

		@Override
		protected String[] getDriverClassNames() {
			return new String[] { "com.class.that.doesnt.exist" };
		}

		@Override
		public String getDatabaseName() {
			return "foo";
		}

		@SuppressWarnings("deprecation")
		@Override
		public String getDriverClassName() {
			return super.getDriverClassName();
		}
	}

	protected static class SomeFields {
		SomeFields someFields;
		Serializable serializable;
		byte[] byteArray;
	}
}
