package com.j256.ormlite.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.ArrayList;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.DatabaseFieldConfig;

public class TableInfoTest extends BaseCoreTest {

	private final static String TABLE_NAME = "tablename";
	private final static String COLUMN_NAME = "column2";

	@Test(expected = IllegalArgumentException.class)
	public void testTableInfo() throws SQLException {
		new TableInfo<NoFieldAnnotations>(connectionSource, NoFieldAnnotations.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNoNoArgConstructor() throws SQLException {
		new TableInfo<NoNoArgConstructor>(connectionSource, NoNoArgConstructor.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testObjectNoFields() throws SQLException {
		new TableInfo<NoFields>(connectionSource, NoFields.class);
	}

	@Test(expected = SQLException.class)
	public void testObjectDoubleId() throws SQLException {
		new TableInfo<DoubleId>(connectionSource, DoubleId.class);
	}

	@Test
	public void testBasic() throws SQLException {
		TableInfo<Foo> tableInfo = new TableInfo<Foo>(connectionSource, Foo.class);
		assertEquals(Foo.class, tableInfo.getDataClass());
		assertEquals(TABLE_NAME, tableInfo.getTableName());
		assertEquals(COLUMN_NAME, tableInfo.getIdField().getDbColumnName());
		assertEquals(1, tableInfo.getFieldTypes().length);
		assertSame(tableInfo.getIdField(), tableInfo.getFieldTypes()[0]);
		assertEquals(COLUMN_NAME, tableInfo.getFieldTypeByColumnName(COLUMN_NAME).getDbColumnName());
	}

	@Test
	public void testObjectToString() throws Exception {
		String id = "f11232oo";
		Foo foo = new Foo();
		foo.id = id;
		assertEquals(id, foo.id);
		TableInfo<Foo> tableInfo = new TableInfo<Foo>(connectionSource, Foo.class);
		assertTrue(tableInfo.objectToString(foo).contains(id));
	}

	@Test
	public void testNoTableNameInAnnotation() throws Exception {
		TableInfo<NoTableNameAnnotation> tableInfo =
				new TableInfo<NoTableNameAnnotation>(connectionSource, NoTableNameAnnotation.class);
		assertEquals(NoTableNameAnnotation.class.getSimpleName().toLowerCase(), tableInfo.getTableName());
	}

	@Test(expected = SQLException.class)
	public void testZeroFieldConfigsSpecified() throws Exception {
		DatabaseTableConfig<NoTableNameAnnotation> tableConfig =
				new DatabaseTableConfig<NoTableNameAnnotation>(NoTableNameAnnotation.class,
						new ArrayList<DatabaseFieldConfig>());
		tableConfig.extractFieldTypes(connectionSource);
		new TableInfo<NoTableNameAnnotation>(databaseType, tableConfig);
	}

	@Test(expected = SQLException.class)
	public void testJustGeneratedId() throws Exception {
		new TableInfo<JustGeneratedId>(connectionSource, JustGeneratedId.class);
	}

	@Test
	public void testConstruct() throws Exception {
		TableInfo<Foo> tableInfo = new TableInfo<Foo>(connectionSource, Foo.class);
		Foo foo = tableInfo.createObject();
		assertNotNull(foo);
	}

	@Test
	public void testUnknownForeignField() throws Exception {
		TableInfo<Foreign> tableInfo = new TableInfo<Foreign>(connectionSource, Foreign.class);
		try {
			tableInfo.getFieldTypeByColumnName("foo");
			fail("expected exception");
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("'" + Foreign.FOREIGN_FIELD_NAME + "'"));
			assertTrue(e.getMessage().contains("'foo'"));
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

	/* ================================================================================================================ */

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

	protected static class JustGeneratedId {
		@DatabaseField(generatedId = true)
		public int id;
		public JustGeneratedId() {
		}
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
