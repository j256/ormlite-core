package com.j256.ormlite.table;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.DatabaseFieldConfig;

public class TableInfoTest extends BaseCoreTest {

	private final static String TABLE_NAME = "tablename";
	private final static String COLUMN_NAME = "column2";

	@Test
	public void testTableInfo() {
		assertThrowsExactly(IllegalArgumentException.class, () -> {
			new TableInfo<NoFieldAnnotations, Void>(databaseType, NoFieldAnnotations.class);
		});
	}

	@Test
	public void testNoNoArgConstructor() {
		assertThrowsExactly(IllegalArgumentException.class, () -> {
			new TableInfo<NoNoArgConstructor, Void>(databaseType, NoNoArgConstructor.class);
		});
	}

	@Test
	public void testObjectNoFields() {
		assertThrowsExactly(IllegalArgumentException.class, () -> {
			new TableInfo<NoFields, Void>(databaseType, NoFields.class);
		});
	}

	@Test
	public void testObjectDoubleId() {
		assertThrowsExactly(SQLException.class, () -> {
			new TableInfo<DoubleId, String>(databaseType, DoubleId.class);
		});
	}

	@Test
	public void testBasic() throws SQLException {
		TableInfo<Foo, String> tableInfo = new TableInfo<Foo, String>(databaseType, Foo.class);
		assertEquals(Foo.class, tableInfo.getDataClass());
		assertEquals(TABLE_NAME, tableInfo.getTableName());
		assertEquals(COLUMN_NAME, tableInfo.getIdField().getColumnName());
		assertEquals(1, tableInfo.getFieldTypes().length);
		assertSame(tableInfo.getIdField(), tableInfo.getFieldTypes()[0]);
		assertEquals(COLUMN_NAME, tableInfo.getFieldTypeByColumnName(COLUMN_NAME).getColumnName());
	}

	@Test
	public void testObjectToString() throws Exception {
		String id = "f11232oo";
		Foo foo = new Foo();
		foo.id = id;
		assertEquals(id, foo.id);
		TableInfo<Foo, String> tableInfo = new TableInfo<Foo, String>(databaseType, Foo.class);
		assertTrue(tableInfo.objectToString(foo).contains(id));
	}

	@Test
	public void testNoTableNameInAnnotation() throws Exception {
		TableInfo<NoTableNameAnnotation, Void> tableInfo =
				new TableInfo<NoTableNameAnnotation, Void>(databaseType, NoTableNameAnnotation.class);
		assertEquals(NoTableNameAnnotation.class.getSimpleName().toLowerCase(), tableInfo.getTableName());
	}

	@Test
	public void testZeroFieldConfigsSpecified() {
		DatabaseTableConfig<NoTableNameAnnotation> tableConfig = new DatabaseTableConfig<NoTableNameAnnotation>(
				databaseType, NoTableNameAnnotation.class, new ArrayList<DatabaseFieldConfig>());
		assertThrowsExactly(SQLException.class, () -> {
			tableConfig.extractFieldTypes(databaseType);
		});
	}

	@Test
	public void testUnknownForeignField() throws Exception {
		TableInfo<Foreign, Void> tableInfo = new TableInfo<Foreign, Void>(databaseType, Foreign.class);
		String wrongName = "foo";
		try {
			tableInfo.getFieldTypeByColumnName(wrongName);
			Assertions.fail("expected exception");
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("'" + Foreign.FOREIGN_FIELD_NAME + "'"));
			assertTrue(e.getMessage().contains("'" + wrongName + "'"));
		}
	}

	/**
	 * Test to make sure that we can call a private constructor
	 */
	@Test
	public void testPrivateConstructor() throws Exception {
		Dao<PrivateConstructor, Object> packConstDao = createDao(PrivateConstructor.class, true);
		int id = 12312321;
		PrivateConstructor pack1 = PrivateConstructor.makeOne(id);
		assertEquals(id, pack1.id);
		packConstDao.create(pack1);
		// we should be able to look it up
		PrivateConstructor pack2 = packConstDao.queryForId(id);
		// and the id should match
		assertEquals(id, pack2.id);
	}

	@Test
	public void testHasColumnName() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		TableInfo<Foo, String> tableInfo = ((BaseDaoImpl<Foo, String>) dao).getTableInfo();
		assertTrue(tableInfo.hasColumnName(COLUMN_NAME));
		assertFalse(tableInfo.hasColumnName("not this name"));
	}

	/*
	 * ================================================================================================================
	 */

	protected static class NoFieldAnnotations {
		String id;
	}

	private static class NoFields {
	}

	protected static class NoNoArgConstructor {
		public NoNoArgConstructor(String arg) {
		}
	}

	protected static class DoubleId {
		@DatabaseField(id = true)
		String id1;
		@DatabaseField(id = true)
		String id2;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	private static class Foo {
		@DatabaseField(id = true, columnName = COLUMN_NAME)
		// private to test access levels
		private String id;
	}

	@DatabaseTable
	protected static class NoTableNameAnnotation {
		@DatabaseField
		String id;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class Foreign {
		public static final String FOREIGN_FIELD_NAME = "fooblah";
		@DatabaseField(foreign = true, columnName = FOREIGN_FIELD_NAME)
		public Foo foo;

		public Foreign() {
		}
	}

	private static class PrivateConstructor {
		@DatabaseField(id = true)
		int id;

		private PrivateConstructor() {
			// make it private
		}

		public static PrivateConstructor makeOne(int id) {
			PrivateConstructor pack = new PrivateConstructor();
			pack.id = id;
			return pack;
		}
	}
}
