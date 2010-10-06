package com.j256.ormlite.table;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

import org.junit.Test;

import com.j256.ormlite.db.BaseDatabaseType;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.field.FieldType;

public class DatabaseTableConfigTest {

	private static final String TABLE_NAME = "sometable";
	private final DatabaseType databaseType = new StubDatabaseType();

	@Test
	public void testDatabaseTableConfig() throws SQLException {
		DatabaseTableConfig<DatabaseTableAnno> dbTableConf =
				DatabaseTableConfig.fromClass(databaseType, DatabaseTableAnno.class);
		assertEquals(DatabaseTableAnno.class, dbTableConf.getDataClass());
		assertEquals(TABLE_NAME, dbTableConf.getTableName());
		FieldType[] fieldTypes = dbTableConf.extractFieldTypes(databaseType);
		assertEquals(1, fieldTypes.length);
		assertEquals("stuff", fieldTypes[0].getDbColumnName());
	}

	@Test
	public void testDatabaseTableWithEntity() throws SQLException {
		DatabaseTableConfig<EntityAnno> dbTableConf = DatabaseTableConfig.fromClass(databaseType, EntityAnno.class);
		assertEquals(EntityAnno.class, dbTableConf.getDataClass());
		assertEquals(TABLE_NAME, dbTableConf.getTableName());
	}

	@Test
	public void testSpringWiring() throws SQLException {
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
	public void testFieldConfigConstructor() throws SQLException {
		List<DatabaseFieldConfig> fieldConfigs = new ArrayList<DatabaseFieldConfig>();
		fieldConfigs.add(new DatabaseFieldConfig("stuff", null, DataType.UNKNOWN, "", 0, true, false, false, null,
				false, null, false, null, false, null, false));
		DatabaseTableConfig<DatabaseTableAnno> dbTableConf =
				new DatabaseTableConfig<DatabaseTableAnno>(DatabaseTableAnno.class, fieldConfigs);
		assertEquals(DatabaseTableAnno.class, dbTableConf.getDataClass());
		assertEquals(TABLE_NAME, dbTableConf.getTableName());
		FieldType[] fieldTypes = dbTableConf.extractFieldTypes(databaseType);
		assertEquals(1, fieldTypes.length);
		assertEquals("stuff", fieldTypes[0].getDbColumnName());
	}

	@Test
	public void testSetFieldConfigs() throws SQLException {
		DatabaseTableConfig<DatabaseTableAnno> dbTableConf = new DatabaseTableConfig<DatabaseTableAnno>();
		dbTableConf.setDataClass(DatabaseTableAnno.class);
		dbTableConf.setTableName(TABLE_NAME);
		List<DatabaseFieldConfig> fieldConfigs = new ArrayList<DatabaseFieldConfig>();
		fieldConfigs.add(new DatabaseFieldConfig("stuff", null, DataType.UNKNOWN, "", 0, true, false, false, null,
				false, null, false, null, false, null, false));
		dbTableConf.setFieldConfigs(fieldConfigs);
		dbTableConf.initialize();
		assertEquals(DatabaseTableAnno.class, dbTableConf.getDataClass());
		assertEquals(TABLE_NAME, dbTableConf.getTableName());
		FieldType[] fieldTypes = dbTableConf.extractFieldTypes(databaseType);
		assertEquals(1, fieldTypes.length);
		assertEquals("stuff", fieldTypes[0].getDbColumnName());
	}

	@Test(expected = SQLException.class)
	public void testSetFieldConfigsNoMatchingField() throws SQLException {
		DatabaseTableConfig<DatabaseTableAnno> dbTableConf = new DatabaseTableConfig<DatabaseTableAnno>();
		dbTableConf.setDataClass(DatabaseTableAnno.class);
		dbTableConf.setTableName(TABLE_NAME);
		List<DatabaseFieldConfig> fieldConfigs = new ArrayList<DatabaseFieldConfig>();
		fieldConfigs.add(new DatabaseFieldConfig("notstuff", null, DataType.UNKNOWN, "", 0, true, false, false, null,
				false, null, false, null, false, null, false));
		dbTableConf.setFieldConfigs(fieldConfigs);
		dbTableConf.initialize();
		assertEquals(DatabaseTableAnno.class, dbTableConf.getDataClass());
		assertEquals(TABLE_NAME, dbTableConf.getTableName());
		dbTableConf.extractFieldTypes(databaseType);
	}

	@Test(expected = SQLException.class)
	public void testSetNoFields() throws SQLException {
		DatabaseTableConfig<DatabaseTableAnno> dbTableConf = new DatabaseTableConfig<DatabaseTableAnno>();
		dbTableConf.setDataClass(DatabaseTableAnno.class);
		dbTableConf.setTableName(TABLE_NAME);
		dbTableConf.setFieldConfigs(new ArrayList<DatabaseFieldConfig>());
		dbTableConf.initialize();
		dbTableConf.extractFieldTypes(databaseType);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNoFieldsClass() throws SQLException {
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

	private class StubDatabaseType extends BaseDatabaseType {
		public String getDriverClassName() {
			return "foo.bar.baz";
		}
		public String getDriverUrlPart() {
			return "foo";
		}
	}
}
