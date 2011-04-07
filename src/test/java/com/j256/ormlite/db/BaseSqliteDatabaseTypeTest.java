package com.j256.ormlite.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.FieldType;

public class BaseSqliteDatabaseTypeTest extends BaseCoreTest {

	@Test(expected = IllegalArgumentException.class)
	public void testConfigureGeneratedIdNotInteger() throws Exception {
		Field field = Foo.class.getField("id");
		FieldType fieldType = FieldType.createFieldType(connectionSource, "foo", field, Foo.class, 0);
		OurSqliteDatabaseType dbType = new OurSqliteDatabaseType();
		StringBuilder sb = new StringBuilder();
		dbType.configureGeneratedId(sb, fieldType, new ArrayList<String>(), new ArrayList<String>(),
				new ArrayList<String>());
	}

	@Test
	public void testConfigureGeneratedIdInteger() throws Exception {
		Field field = Foo.class.getField("val");
		FieldType fieldType = FieldType.createFieldType(connectionSource, "foo", field, Foo.class, 0);
		OurSqliteDatabaseType dbType = new OurSqliteDatabaseType();
		StringBuilder sb = new StringBuilder();
		dbType.configureGeneratedId(sb, fieldType, new ArrayList<String>(), new ArrayList<String>(),
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
	public void testGetFieldConverter() throws Exception {
		OurSqliteDatabaseType dbType = new OurSqliteDatabaseType();
		assertEquals(new Byte((byte) 1), dbType.getFieldConverter(DataType.BOOLEAN).parseDefaultString(null, "true"));
	}

	private static class OurSqliteDatabaseType extends BaseSqliteDatabaseType {
		public boolean isDatabaseUrlThisType(String url, String dbTypePart) {
			return false;
		}
		@Override
		protected String getDriverClassName() {
			return null;
		}
		@Override
		public String getDatabaseName() {
			return "fake";
		}
	}
}
