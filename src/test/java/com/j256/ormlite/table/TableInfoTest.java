package com.j256.ormlite.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;

import org.junit.Test;

import com.j256.ormlite.BaseOrmLiteTest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.DatabaseFieldConfig;

public class TableInfoTest extends BaseOrmLiteTest {

	private final static String TABLE_NAME = "tablename";
	private final static String COLUMN_NAME = "column2";

	@Test(expected = IllegalArgumentException.class)
	public void testTableInfo() throws SQLException {
		new TableInfo<NoFieldAnnotations>(databaseType, NoFieldAnnotations.class);
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

	@Test(expected = IllegalArgumentException.class)
	public void testObjectNoFields() throws SQLException {
		new TableInfo<NoFields>(databaseType, NoFields.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testObjectDoubleId() throws SQLException {
		new TableInfo<DoubleId>(databaseType, DoubleId.class);
	}

	@Test
	public void testBasic() throws SQLException {
		TableInfo<Foo> tableInfo = new TableInfo<Foo>(databaseType, Foo.class);
		assertEquals(Foo.class, tableInfo.getDataClass());
		assertEquals(TABLE_NAME, tableInfo.getTableName());
		assertEquals(COLUMN_NAME, tableInfo.getIdField().getDbColumnName());
		assertEquals(1, tableInfo.getFieldTypes().length);
		assertSame(tableInfo.getIdField(), tableInfo.getFieldTypes()[0]);
	}

	@Test
	public void testObjectToString() throws Exception {
		String id = "f11232oo";
		Foo foo = new Foo();
		foo.id = id;
		assertEquals(id, foo.id);
		TableInfo<Foo> tableInfo = new TableInfo<Foo>(databaseType, Foo.class);
		assertTrue(tableInfo.objectToString(foo).contains(id));
	}

	@Test
	public void testNoTableNameInAnnotation() throws Exception {
		TableInfo<NoTableNameAnnotation> tableInfo =
				new TableInfo<NoTableNameAnnotation>(databaseType, NoTableNameAnnotation.class);
		assertEquals(NoTableNameAnnotation.class.getSimpleName().toLowerCase(), tableInfo.getTableName());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testZeroFieldConfigsSpecified() throws Exception {
		new TableInfo<NoTableNameAnnotation>(databaseType, new DatabaseTableConfig<NoTableNameAnnotation>(
				NoTableNameAnnotation.class, new ArrayList<DatabaseFieldConfig>()));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testJustGeneratedId() throws Exception {
		new TableInfo<JustGeneratedId>(databaseType, JustGeneratedId.class);
	}

	protected static class NoFieldAnnotations {
		String id;
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

	private static class NoFields {
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
}
