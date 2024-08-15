package com.j256.ormlite.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.FieldType;

public class BaseSqliteDatabaseTypeTest extends BaseCoreTest {

	@Test
	public void testConfigureGeneratedIdNotInteger() throws Exception {
		Field field = Foo.class.getField("stringField");
		FieldType fieldType = FieldType.createFieldType(databaseType, "foo", field, Foo.class);
		OurSqliteDatabaseType dbType = new OurSqliteDatabaseType();
		StringBuilder sb = new StringBuilder();
		assertThrowsExactly(IllegalArgumentException.class, () -> {
			dbType.configureGeneratedId(null, sb, fieldType, new ArrayList<String>(), null, new ArrayList<String>(),
					new ArrayList<String>());
		});
	}

	@Test
	public void testConfigureGeneratedIdInteger() throws Exception {
		Field field = Foo.class.getField("val");
		FieldType fieldType = FieldType.createFieldType(databaseType, "foo", field, Foo.class);
		OurSqliteDatabaseType dbType = new OurSqliteDatabaseType();
		StringBuilder sb = new StringBuilder();
		dbType.configureGeneratedId(null, sb, fieldType, new ArrayList<String>(), null, new ArrayList<String>(),
				new ArrayList<String>());
		assertTrue(sb.toString().contains("PRIMARY KEY AUTOINCREMENT"));
	}

	@Test
	public void testIsVarcharFieldWidthSupported() {
		assertFalse(new OurSqliteDatabaseType().isVarcharFieldWidthSupported());
	}

	@Test
	public void testIsCreateTableReturnsZero() {
		assertFalse(new OurSqliteDatabaseType().isCreateTableReturnsZero());
	}

	@Test
	public void testGeneratedIdSqlAtEnd() {
		assertFalse(new OurSqliteDatabaseType().generatedIdSqlAtEnd());
	}

	@Test
	public void testIsCreateIfNotExistsSupported() {
		assertTrue(new OurSqliteDatabaseType().isCreateIfNotExistsSupported());
	}

	@Test
	public void testGetFieldConverter() throws Exception {
		OurSqliteDatabaseType dbType = new OurSqliteDatabaseType();
		assertEquals(Byte.valueOf((byte) 1),
				dbType.getFieldConverter(DataType.BOOLEAN.getDataPersister(), null).parseDefaultString(null, "true"));
	}

	@Test
	public void testDefaultFieldConverter() {
		OurSqliteDatabaseType dbType = new OurSqliteDatabaseType();
		assertSame(DataType.STRING.getDataPersister(),
				dbType.getFieldConverter(DataType.STRING.getDataPersister(), null));
	}

	private static class OurSqliteDatabaseType extends BaseSqliteDatabaseType {
		@Override
		public boolean isDatabaseUrlThisType(String url, String dbTypePart) {
			return false;
		}

		@Override
		public String getDatabaseName() {
			return "fake";
		}

		@Override
		public boolean generatedIdSqlAtEnd() {
			return super.generatedIdSqlAtEnd();
		}

		@Override
		public boolean isCreateIfNotExistsSupported() {
			return super.isCreateIfNotExistsSupported();
		}
	}
}
