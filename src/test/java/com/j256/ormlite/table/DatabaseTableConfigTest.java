package com.j256.ormlite.table;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import com.j256.ormlite.support.ConnectionSource;

public class DatabaseTableConfigTest {

	private static final String TABLE_NAME = "sometable";
	private final DatabaseType databaseType = new StubDatabaseType();
	private final ConnectionSource connectionSource;

	{
		connectionSource = createMock(ConnectionSource.class);
		expect(connectionSource.getDatabaseType()).andReturn(databaseType).anyTimes();
		replay(connectionSource);
	}

	@Test
	public void testDatabaseTableConfig() throws SQLException {
		DatabaseTableConfig<DatabaseTableAnno> dbTableConf =
				DatabaseTableConfig.fromClass(connectionSource, DatabaseTableAnno.class);
		assertEquals(DatabaseTableAnno.class, dbTableConf.getDataClass());
		assertEquals(TABLE_NAME, dbTableConf.getTableName());
		dbTableConf.extractFieldTypes(connectionSource);
		FieldType[] fieldTypes = dbTableConf.getFieldTypes(databaseType);
		assertEquals(1, fieldTypes.length);
		assertEquals("stuff", fieldTypes[0].getColumnName());
	}

	@Test
	public void testDatabaseTableWithEntity() throws SQLException {
		DatabaseTableConfig<EntityAnno> dbTableConf = DatabaseTableConfig.fromClass(connectionSource, EntityAnno.class);
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
		dbTableConf.extractFieldTypes(connectionSource);
		FieldType[] fieldTypes = dbTableConf.getFieldTypes(databaseType);
		assertEquals(1, fieldTypes.length);
		assertEquals("stuff", fieldTypes[0].getColumnName());
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
				false, null, false, null, false, null, false, null, null, false,
				DatabaseFieldConfig.NO_MAX_FOREIGN_AUTO_REFRESH_LEVEL_SPECIFIED, 0));
		DatabaseTableConfig<DatabaseTableAnno> dbTableConf =
				new DatabaseTableConfig<DatabaseTableAnno>(DatabaseTableAnno.class, fieldConfigs);
		assertEquals(DatabaseTableAnno.class, dbTableConf.getDataClass());
		assertEquals(TABLE_NAME, dbTableConf.getTableName());
		dbTableConf.extractFieldTypes(connectionSource);
		FieldType[] fieldTypes = dbTableConf.getFieldTypes(databaseType);
		assertEquals(1, fieldTypes.length);
		assertEquals("stuff", fieldTypes[0].getColumnName());
	}

	@Test
	public void testFieldConfigConstructorDataType() {
		DataType dataType = DataType.BIG_DECIMAL;
		DatabaseFieldConfig config = new DatabaseFieldConfig("stuff", null, dataType, null, 0, true, false, false, null,
				false, null, false, null, false, null, false, null, null, false,
				DatabaseFieldConfig.NO_MAX_FOREIGN_AUTO_REFRESH_LEVEL_SPECIFIED, 0);
		assertEquals(dataType, config.getDataType());
	}

	@Test
	public void testSetFieldConfigs() throws SQLException {
		DatabaseTableConfig<DatabaseTableAnno> dbTableConf = new DatabaseTableConfig<DatabaseTableAnno>();
		dbTableConf.setDataClass(DatabaseTableAnno.class);
		dbTableConf.setTableName(TABLE_NAME);
		List<DatabaseFieldConfig> fieldConfigs = new ArrayList<DatabaseFieldConfig>();
		fieldConfigs.add(new DatabaseFieldConfig("stuff", null, DataType.UNKNOWN, "", 0, true, false, false, null,
				false, null, false, null, false, null, false, null, null, false,
				DatabaseFieldConfig.NO_MAX_FOREIGN_AUTO_REFRESH_LEVEL_SPECIFIED, 0));
		dbTableConf.setFieldConfigs(fieldConfigs);
		dbTableConf.initialize();
		assertEquals(DatabaseTableAnno.class, dbTableConf.getDataClass());
		assertEquals(TABLE_NAME, dbTableConf.getTableName());
		dbTableConf.extractFieldTypes(connectionSource);
		FieldType[] fieldTypes = dbTableConf.getFieldTypes(databaseType);
		assertEquals(1, fieldTypes.length);
		assertEquals("stuff", fieldTypes[0].getColumnName());
	}

	@Test(expected = SQLException.class)
	public void testSetFieldConfigsNoMatchingField() throws SQLException {
		DatabaseTableConfig<DatabaseTableAnno> dbTableConf = new DatabaseTableConfig<DatabaseTableAnno>();
		dbTableConf.setDataClass(DatabaseTableAnno.class);
		dbTableConf.setTableName(TABLE_NAME);
		List<DatabaseFieldConfig> fieldConfigs = new ArrayList<DatabaseFieldConfig>();
		fieldConfigs.add(new DatabaseFieldConfig("notstuff", null, DataType.UNKNOWN, "", 0, true, false, false, null,
				false, null, false, null, false, null, false, null, null, false, 0, 0));
		dbTableConf.setFieldConfigs(fieldConfigs);
		dbTableConf.initialize();
		assertEquals(DatabaseTableAnno.class, dbTableConf.getDataClass());
		assertEquals(TABLE_NAME, dbTableConf.getTableName());
		dbTableConf.extractFieldTypes(connectionSource);
	}

	@Test(expected = SQLException.class)
	public void testSetNoFields() throws SQLException {
		DatabaseTableConfig<DatabaseTableAnno> dbTableConf = new DatabaseTableConfig<DatabaseTableAnno>();
		dbTableConf.setDataClass(DatabaseTableAnno.class);
		dbTableConf.setTableName(TABLE_NAME);
		dbTableConf.setFieldConfigs(new ArrayList<DatabaseFieldConfig>());
		dbTableConf.initialize();
		dbTableConf.extractFieldTypes(connectionSource);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNoFieldsClass() throws SQLException {
		DatabaseTableConfig<NoFields> dbTableConf = new DatabaseTableConfig<NoFields>();
		dbTableConf.setDataClass(NoFields.class);
		dbTableConf.initialize();
		dbTableConf.extractFieldTypes(connectionSource);
	}

	@Test(expected = IllegalStateException.class)
	public void testBadSpringWiring() {
		DatabaseTableConfig<NoFields> dbTableConf = new DatabaseTableConfig<NoFields>();
		dbTableConf.initialize();
	}

	@Test
	public void testBaseClassHandling() throws Exception {
		DatabaseTableConfig<Sub> dbTableConf = new DatabaseTableConfig<Sub>();
		dbTableConf.setDataClass(Sub.class);
		dbTableConf.initialize();
		dbTableConf.extractFieldTypes(connectionSource);
		FieldType[] fieldTypes = dbTableConf.getFieldTypes(databaseType);
		assertEquals(2, fieldTypes.length);
		boolean seeId = false;
		boolean seeStuff = false;
		for (FieldType fieldType : fieldTypes) {
			String fieldName = fieldType.getFieldName();
			if (fieldName.equals("id")) {
				seeId = true;
			} else if (fieldType.getFieldName().equals("stuff")) {
				seeStuff = true;
			} else {
				fail("Unknown field type " + fieldType);
			}
		}
		assertTrue(seeId);
		assertTrue(seeStuff);
	}

	@Test
	public void testBaseClassHandlingWithoutAnno() throws Exception {
		List<DatabaseFieldConfig> fieldConfigs = new ArrayList<DatabaseFieldConfig>();
		DatabaseFieldConfig fieldId = new DatabaseFieldConfig("id");
		fieldId.setId(true);
		fieldConfigs.add(fieldId);
		fieldConfigs.add(new DatabaseFieldConfig("stuff"));

		DatabaseTableConfig<SubWithoutAnno> dbTableConf =
				new DatabaseTableConfig<SubWithoutAnno>(SubWithoutAnno.class, fieldConfigs);
		dbTableConf.extractFieldTypes(connectionSource);

		FieldType[] fieldTypes = dbTableConf.getFieldTypes(databaseType);
		assertTrue(fieldTypes.length >= 2);
		boolean seeId = false;
		boolean seeStuff = false;
		for (FieldType fieldType : fieldTypes) {
			String fieldName = fieldType.getFieldName();
			if (fieldName.equals("id")) {
				seeId = true;
			} else if (fieldType.getFieldName().equals("stuff")) {
				seeStuff = true;
			}
		}
		assertTrue(seeId);
		assertTrue(seeStuff);
	}

	@Test
	public void testSetTableNameCase() {
		List<DatabaseFieldConfig> fieldConfigs = new ArrayList<DatabaseFieldConfig>();
		DatabaseFieldConfig fieldId = new DatabaseFieldConfig("id");
		fieldId.setId(true);
		fieldConfigs.add(fieldId);
		fieldConfigs.add(new DatabaseFieldConfig("stuff"));

		DatabaseTableConfig<SubWithoutAnno> tableConfig = new DatabaseTableConfig<SubWithoutAnno>();
		tableConfig.setDataClass(SubWithoutAnno.class);
		String tableName = "mixEDcaSE";
		tableConfig.setTableName(tableName);
		tableConfig.setFieldConfigs(fieldConfigs);
		tableConfig.initialize();

		assertEquals(tableName, tableConfig.getTableName());
	}

	@Test(expected = SQLException.class)
	public void testNoFields() throws SQLException {
		new DatabaseTableConfig<DatabaseTableAnno>().getFieldTypes(databaseType);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNoConstructor() throws SQLException {
		DatabaseTableConfig.fromClass(connectionSource, NoConstructor.class).getConstructor();
	}

	/* ======================================================================================= */

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

	private static class StubDatabaseType extends BaseDatabaseType {
		@Override
		public String getDriverClassName() {
			return "foo.bar.baz";
		}

		@Override
		public String getDatabaseName() {
			return "fake";
		}

		@Override
		public boolean isDatabaseUrlThisType(String url, String dbTypePart) {
			return false;
		}
	}

	protected static class Base {
		@DatabaseField(id = true)
		int id;

		public Base() {
			// for ormlite
		}
	}

	protected static class Sub extends Base {
		@DatabaseField
		String stuff;

		public Sub() {
			// for ormlite
		}
	}

	protected static class BaseWithoutAnno {
		int id;

		public BaseWithoutAnno() {
			// for ormlite
		}
	}

	protected static class SubWithoutAnno extends BaseWithoutAnno {
		String stuff;

		public SubWithoutAnno() {
			// for ormlite
		}
	}
}
