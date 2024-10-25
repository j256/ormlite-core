package com.j256.ormlite.table;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.persistence.Entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
				DatabaseTableConfig.fromClass(databaseType, DatabaseTableAnno.class);
		assertEquals(DatabaseTableAnno.class, dbTableConf.getDataClass());
		assertEquals(TABLE_NAME, dbTableConf.getTableName());
		dbTableConf.extractFieldTypes(databaseType);
		FieldType[] fieldTypes = dbTableConf.getFieldTypes();
		assertEquals(1, fieldTypes.length);
		assertEquals("stuff", fieldTypes[0].getColumnName());
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
		dbTableConf.extractFieldTypes(databaseType);
		FieldType[] fieldTypes = dbTableConf.getFieldTypes();
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
				new DatabaseTableConfig<DatabaseTableAnno>(databaseType, DatabaseTableAnno.class, fieldConfigs);
		assertEquals(DatabaseTableAnno.class, dbTableConf.getDataClass());
		assertEquals(TABLE_NAME, dbTableConf.getTableName());
		dbTableConf.extractFieldTypes(databaseType);
		FieldType[] fieldTypes = dbTableConf.getFieldTypes();
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
		dbTableConf.extractFieldTypes(databaseType);
		FieldType[] fieldTypes = dbTableConf.getFieldTypes();
		assertEquals(1, fieldTypes.length);
		assertEquals("stuff", fieldTypes[0].getColumnName());
	}

	@Test
	public void testSetFieldConfigsNoMatchingField() {
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
		assertThrowsExactly(SQLException.class, () -> {
			dbTableConf.extractFieldTypes(databaseType);
		});
	}

	@Test
	public void testSetNoFields() {
		DatabaseTableConfig<DatabaseTableAnno> dbTableConf = new DatabaseTableConfig<DatabaseTableAnno>();
		dbTableConf.setDataClass(DatabaseTableAnno.class);
		dbTableConf.setTableName(TABLE_NAME);
		dbTableConf.setFieldConfigs(new ArrayList<DatabaseFieldConfig>());
		dbTableConf.initialize();
		assertThrowsExactly(SQLException.class, () -> {
			dbTableConf.extractFieldTypes(databaseType);
		});
	}

	@Test
	public void testNoFieldsClass() {
		DatabaseTableConfig<NoFields> dbTableConf = new DatabaseTableConfig<NoFields>();
		dbTableConf.setDataClass(NoFields.class);
		dbTableConf.initialize();
		assertThrowsExactly(IllegalArgumentException.class, () -> {
			dbTableConf.extractFieldTypes(databaseType);
		});
	}

	@Test
	public void testBadSpringWiring() {
		DatabaseTableConfig<NoFields> dbTableConf = new DatabaseTableConfig<NoFields>();
		assertThrowsExactly(IllegalStateException.class, () -> {
			dbTableConf.initialize();
		});
	}

	@Test
	public void testBaseClassHandling() throws Exception {
		DatabaseTableConfig<Sub> dbTableConf = new DatabaseTableConfig<Sub>();
		dbTableConf.setDataClass(Sub.class);
		dbTableConf.initialize();
		dbTableConf.extractFieldTypes(databaseType);
		FieldType[] fieldTypes = dbTableConf.getFieldTypes();
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
				Assertions.fail("Unknown field type " + fieldType);
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
				new DatabaseTableConfig<SubWithoutAnno>(databaseType, SubWithoutAnno.class, fieldConfigs);
		dbTableConf.extractFieldTypes(databaseType);

		FieldType[] fieldTypes = dbTableConf.getFieldTypes();
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

	@Test
	public void testNoFields() {
		assertThrowsExactly(SQLException.class, () -> {
			new DatabaseTableConfig<DatabaseTableAnno>().getFieldTypes();
		});
	}

	@Test
	public void testUnknownAfterField() {
		assertThrowsExactly(SQLException.class, () -> {
			DatabaseTableConfig.fromClass(databaseType, UnknownAfter.class);
		});
	}

	@Test
	public void testAllAfter() {
		assertThrowsExactly(IllegalStateException.class, () -> {
			DatabaseTableConfig.fromClass(databaseType, AllAfter.class);
		});
	}

	@Test
	public void testAfterFieldLoop() {
		assertThrowsExactly(IllegalStateException.class, () -> {
			DatabaseTableConfig.fromClass(databaseType, AfterLoop.class);
		});
	}

	@Test
	public void testMultiAfter() throws SQLException {
		DatabaseTableConfig<MultipleAfterField> config =
				DatabaseTableConfig.fromClass(databaseType, MultipleAfterField.class);
		FieldType[] fieldTypes = config.getFieldTypes();
		Arrays.sort(fieldTypes, Comparator.comparing(FieldType::getColumnName));

		assertEquals(3, fieldTypes.length);
		assertEquals(MultipleAfterField.FIELD_NAME1, fieldTypes[0].getColumnName());
		assertEquals(MultipleAfterField.FIELD_NAME2, fieldTypes[1].getColumnName());
		assertEquals(MultipleAfterField.FIELD_NAME3, fieldTypes[2].getColumnName());
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
		protected String[] getDriverClassNames() {
			return new String[] { "foo.bar.baz" };
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

	protected static class UnknownAfter {
		@DatabaseField(afterField = "unknown")
		String stuff;

		public UnknownAfter() {
			// for ormlite
		}
	}

	protected static class AllAfter {
		@DatabaseField(afterField = "a2")
		String a1;
		@DatabaseField(afterField = "a1")
		String a2;

		public AllAfter() {
			// for ormlite
		}
	}

	protected static class AfterLoop {
		@DatabaseField(afterField = "a2")
		String a1;
		@DatabaseField(afterField = "a1")
		String a2;
		@DatabaseField
		String a3;

		public AfterLoop() {
			// for ormlite
		}
	}

	protected static class MultipleAfterField {
		static final String FIELD_NAME1 = "a1";
		static final String FIELD_NAME2 = "a2";
		static final String FIELD_NAME3 = "a3";

		@DatabaseField(columnName = FIELD_NAME1, afterField = FIELD_NAME3)
		String a1;
		@DatabaseField(columnName = FIELD_NAME2, afterField = FIELD_NAME3)
		String a2;
		@DatabaseField(columnName = FIELD_NAME3)
		String a3;

		public MultipleAfterField() {
			// for ormlite
		}
	}
}
