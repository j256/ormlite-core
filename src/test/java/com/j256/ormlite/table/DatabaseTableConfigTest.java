package com.j256.ormlite.table;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

import org.junit.Test;

import com.j256.ormlite.BaseOrmLiteTest;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.JdbcType;

public class DatabaseTableConfigTest extends BaseOrmLiteTest {

	private static final String TABLE_NAME = "sometable";

	@Test
	public void testDatabaseTableConfig() {
		DatabaseTableConfig<DatabaseTableAnno> dbTableConf =
				DatabaseTableConfig.fromClass(databaseType, DatabaseTableAnno.class);
		assertEquals(DatabaseTableAnno.class, dbTableConf.getDataClass());
		assertEquals(TABLE_NAME, dbTableConf.getTableName());
		FieldType[] fieldTypes = dbTableConf.extractFieldTypes(databaseType);
		assertEquals(1, fieldTypes.length);
		assertEquals("stuff", fieldTypes[0].getDbColumnName());
	}

	@Test
	public void testDatabaseTableWithEntity() {
		DatabaseTableConfig<EntityAnno> dbTableConf = DatabaseTableConfig.fromClass(databaseType, EntityAnno.class);
		assertEquals(EntityAnno.class, dbTableConf.getDataClass());
		assertEquals(TABLE_NAME, dbTableConf.getTableName());
	}

	@Test
	public void testSpringWiring() {
		DatabaseTableConfig<DatabaseTableAnno> dbTableConf = new DatabaseTableConfig<DatabaseTableAnno>();
		dbTableConf.setDataClass(DatabaseTableAnno.class);
		dbTableConf.setTableName(TABLE_NAME);
		dbTableConf.initialize();
		assertEquals(DatabaseTableAnno.class, dbTableConf.getDataClass());
		assertEquals(TABLE_NAME, dbTableConf.getTableName());
		FieldType[] fieldTypes = dbTableConf.extractFieldTypes(databaseType);
		assertEquals(1, fieldTypes.length);
		assertEquals("stuff", fieldTypes[0].getDbColumnName());
	}

	@Test
	public void testSpringWiringNoTableName() {
		DatabaseTableConfig<DatabaseTableAnno> dbTableConf = new DatabaseTableConfig<DatabaseTableAnno>();
		dbTableConf.setDataClass(DatabaseTableAnno.class);
		dbTableConf.initialize();
		assertEquals(DatabaseTableAnno.class, dbTableConf.getDataClass());
		assertEquals(TABLE_NAME, dbTableConf.getTableName());
	}

	@Test
	public void testFieldConfigConstructor() {
		List<DatabaseFieldConfig> fieldConfigs = new ArrayList<DatabaseFieldConfig>();
		fieldConfigs.add(new DatabaseFieldConfig("stuff", null, JdbcType.UNKNOWN, "", 0, true, false, false, null,
				false, null, false, null, false));
		DatabaseTableConfig<DatabaseTableAnno> dbTableConf =
				new DatabaseTableConfig<DatabaseTableAnno>(DatabaseTableAnno.class, fieldConfigs);
		assertEquals(DatabaseTableAnno.class, dbTableConf.getDataClass());
		assertEquals(TABLE_NAME, dbTableConf.getTableName());
		FieldType[] fieldTypes = dbTableConf.extractFieldTypes(databaseType);
		assertEquals(1, fieldTypes.length);
		assertEquals("stuff", fieldTypes[0].getDbColumnName());
	}

	@Test
	public void testSetFieldConfigs() {
		DatabaseTableConfig<DatabaseTableAnno> dbTableConf = new DatabaseTableConfig<DatabaseTableAnno>();
		dbTableConf.setDataClass(DatabaseTableAnno.class);
		dbTableConf.setTableName(TABLE_NAME);
		List<DatabaseFieldConfig> fieldConfigs = new ArrayList<DatabaseFieldConfig>();
		fieldConfigs.add(new DatabaseFieldConfig("stuff", null, JdbcType.UNKNOWN, "", 0, true, false, false, null,
				false, null, false, null, false));
		dbTableConf.setFieldConfigs(fieldConfigs);
		dbTableConf.initialize();
		assertEquals(DatabaseTableAnno.class, dbTableConf.getDataClass());
		assertEquals(TABLE_NAME, dbTableConf.getTableName());
		FieldType[] fieldTypes = dbTableConf.extractFieldTypes(databaseType);
		assertEquals(1, fieldTypes.length);
		assertEquals("stuff", fieldTypes[0].getDbColumnName());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetFieldConfigsNoMatchingField() {
		DatabaseTableConfig<DatabaseTableAnno> dbTableConf = new DatabaseTableConfig<DatabaseTableAnno>();
		dbTableConf.setDataClass(DatabaseTableAnno.class);
		dbTableConf.setTableName(TABLE_NAME);
		List<DatabaseFieldConfig> fieldConfigs = new ArrayList<DatabaseFieldConfig>();
		fieldConfigs.add(new DatabaseFieldConfig("notstuff", null, JdbcType.UNKNOWN, "", 0, true, false, false, null,
				false, null, false, null, false));
		dbTableConf.setFieldConfigs(fieldConfigs);
		dbTableConf.initialize();
		assertEquals(DatabaseTableAnno.class, dbTableConf.getDataClass());
		assertEquals(TABLE_NAME, dbTableConf.getTableName());
		dbTableConf.extractFieldTypes(databaseType);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSetNoFields() {
		DatabaseTableConfig<DatabaseTableAnno> dbTableConf = new DatabaseTableConfig<DatabaseTableAnno>();
		dbTableConf.setDataClass(DatabaseTableAnno.class);
		dbTableConf.setTableName(TABLE_NAME);
		dbTableConf.setFieldConfigs(new ArrayList<DatabaseFieldConfig>());
		dbTableConf.initialize();
		dbTableConf.extractFieldTypes(databaseType);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNoFieldsClass() {
		DatabaseTableConfig<NoFields> dbTableConf = new DatabaseTableConfig<NoFields>();
		dbTableConf.setDataClass(NoFields.class);
		dbTableConf.initialize();
		dbTableConf.extractFieldTypes(databaseType);
	}

	@Test(expected = IllegalStateException.class)
	public void testBadSpringWiring() {
		DatabaseTableConfig<NoFields> dbTableConf = new DatabaseTableConfig<NoFields>();
		dbTableConf.initialize();
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class DatabaseTableAnno {
		@DatabaseField
		public String stuff;
	}

	@Entity(name = TABLE_NAME)
	protected static class EntityAnno {
		@DatabaseField
		public String stuff;
	}

	@Entity
	protected static class NoFields {
		public String stuff;
	}
}
