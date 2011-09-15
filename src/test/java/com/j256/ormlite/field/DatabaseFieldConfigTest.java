package com.j256.ormlite.field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.junit.Test;

import com.j256.ormlite.db.BaseDatabaseType;
import com.j256.ormlite.db.DatabaseType;

public class DatabaseFieldConfigTest {

	private DatabaseType databaseType = new StubDatabaseType();

	@Test
	public void testGetSet() {
		DatabaseFieldConfig config = new DatabaseFieldConfig();
		String str;

		assertNull(config.getFieldName());
		str = "field";
		config.setFieldName(str);
		assertEquals(str, config.getFieldName());

		assertNull(config.getColumnName());
		str = "name";
		config.setColumnName(str);
		assertEquals(str, config.getColumnName());

		assertNull(config.getDataPersister());
		DataPersister jdbcType = DataType.DOUBLE.getDataPersister();
		config.setDataPersister(jdbcType);
		assertEquals(jdbcType, config.getDataPersister());

		assertNull(config.getDefaultValue());
		str = "default";
		config.setDefaultValue(str);
		assertEquals(str, config.getDefaultValue());

		assertEquals(0, config.getWidth());
		int width = 21312312;
		config.setWidth(width);
		assertEquals(width, config.getWidth());

		assertFalse(config.isId());
		config.setId(true);
		assertTrue(config.isId());

		assertNull(config.getGeneratedIdSequence());
		str = "seq";
		config.setGeneratedIdSequence(str);
		assertEquals(str, config.getGeneratedIdSequence());

		assertFalse(config.isCanBeNull());
		config.setCanBeNull(true);
		assertTrue(config.isCanBeNull());

		assertFalse(config.isForeign());
		config.setForeign(true);
		assertTrue(config.isForeign());

		assertFalse(config.isGeneratedId());
		config.setGeneratedId(true);
		assertTrue(config.isGeneratedId());

		assertFalse(config.isUseGetSet());
		config.setUseGetSet(true);
		assertTrue(config.isUseGetSet());
	}

	@Test
	public void testFromDbField() throws Exception {
		Field[] fields = Foo.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		DatabaseFieldConfig config = DatabaseFieldConfig.fromField(databaseType, "foo", fields[0]);
		assertNotNull(config);
		assertTrue(config.isCanBeNull());
		assertEquals(fields[0].getName(), config.getFieldName());
	}

	private final static String STUFF_FIELD_NAME = "notstuff";
	private final static int LENGTH_LENGTH = 100;

	@Test
	public void testJavaxAnnotations() throws Exception {
		Field[] fields = JavaxAnno.class.getDeclaredFields();
		assertTrue(fields.length >= 8);

		// not a column
		assertNull(DatabaseFieldConfig.fromField(databaseType, "foo", fields[0]));

		DatabaseFieldConfig config = DatabaseFieldConfig.fromField(databaseType, "foo", fields[1]);
		assertNotNull(config);
		assertFalse(config.isId());
		assertTrue(config.isGeneratedId());
		assertFalse(config.isUseGetSet());
		assertEquals(fields[1].getName(), config.getFieldName());

		config = DatabaseFieldConfig.fromField(databaseType, "foo", fields[2]);
		assertNotNull(config);
		assertFalse(config.isUseGetSet());
		assertEquals(STUFF_FIELD_NAME, config.getColumnName());

		config = DatabaseFieldConfig.fromField(databaseType, "foo", fields[3]);
		assertNotNull(config);
		assertEquals(LENGTH_LENGTH, config.getWidth());
		assertFalse(config.isUseGetSet());
		assertEquals(fields[3].getName(), config.getFieldName());

		config = DatabaseFieldConfig.fromField(databaseType, "foo", fields[4]);
		assertNotNull(config);
		assertFalse(config.isCanBeNull());
		assertFalse(config.isUseGetSet());
		assertEquals(fields[4].getName(), config.getFieldName());

		config = DatabaseFieldConfig.fromField(databaseType, "foo", fields[5]);
		assertNotNull(config);
		assertTrue(config.isForeign());
		assertNull(config.getDataPersister());
		assertEquals(fields[5].getName(), config.getFieldName());

		config = DatabaseFieldConfig.fromField(databaseType, "foo", fields[6]);
		assertNotNull(config);
		assertTrue(config.isForeign());
		assertNull(config.getDataPersister());
		assertEquals(fields[6].getName(), config.getFieldName());
	}

	@Test
	public void testJavaxJustId() throws Exception {
		Field[] fields = JavaxAnnoJustId.class.getDeclaredFields();
		assertTrue(fields.length >= 1);

		DatabaseFieldConfig config = DatabaseFieldConfig.fromField(databaseType, "foo", fields[0]);
		assertNotNull(config);
		assertTrue(config.isId());
		assertFalse(config.isGeneratedId());
		assertFalse(config.isUseGetSet());
		assertEquals(fields[0].getName(), config.getFieldName());
	}

	@Test
	public void testJavaxGetSet() throws Exception {
		Field[] fields = JavaxGetSet.class.getDeclaredFields();
		assertTrue(fields.length >= 1);

		DatabaseFieldConfig config = DatabaseFieldConfig.fromField(databaseType, "foo", fields[0]);
		assertNotNull(config);
		assertTrue(config.isUseGetSet());
		assertEquals(fields[0].getName(), config.getFieldName());
	}

	@Test
	public void testJavaxUnique() throws Exception {
		Field[] fields = JavaxUnique.class.getDeclaredFields();
		assertTrue(fields.length >= 1);

		DatabaseFieldConfig config = DatabaseFieldConfig.fromField(databaseType, "foo", fields[0]);
		assertNotNull(config);
		assertTrue(config.isUnique());
		assertEquals(fields[0].getName(), config.getFieldName());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUnknownEnumVal() throws Exception {
		Field[] fields = BadUnknownVal.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		DatabaseFieldConfig.fromField(databaseType, "foo", fields[0]);
	}

	@Test
	public void testIndex() throws Exception {
		Field[] fields = Index.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		String tableName = "foo";
		DatabaseFieldConfig fieldConfig = DatabaseFieldConfig.fromField(databaseType, tableName, fields[0]);
		assertEquals(tableName + "_" + fields[0].getName() + "_idx", fieldConfig.getIndexName());
	}

	@Test
	public void testComboIndex() throws Exception {
		Field[] fields = ComboIndex.class.getDeclaredFields();
		assertTrue(fields.length >= 2);
		DatabaseFieldConfig fieldConfig = DatabaseFieldConfig.fromField(databaseType, "foo", fields[0]);
		assertEquals(ComboIndex.INDEX_NAME, fieldConfig.getIndexName());
		fieldConfig = DatabaseFieldConfig.fromField(databaseType, "foo", fields[1]);
		assertEquals(ComboIndex.INDEX_NAME, fieldConfig.getIndexName());
	}

	@Test
	public void testDefaultValue() throws Exception {
		DatabaseFieldConfig fieldConfig =
				DatabaseFieldConfig.fromField(databaseType, "defaultstring",
						DefaultString.class.getDeclaredField("stuff"));
		assertNotNull(fieldConfig.getDefaultValue());
		assertEquals(DefaultString.STUFF_DEFAULT, fieldConfig.getDefaultValue());
		fieldConfig =
				DatabaseFieldConfig.fromField(databaseType, "defaultstring",
						DefaultString.class.getDeclaredField("junk"));
		assertNotNull(fieldConfig.getDefaultValue());
		assertEquals(DefaultString.JUNK_DEFAULT, fieldConfig.getDefaultValue());
		fieldConfig =
				DatabaseFieldConfig.fromField(databaseType, "defaultstring",
						DefaultString.class.getDeclaredField("none"));
		assertNull(fieldConfig.getDefaultValue());
	}

	@Test
	public void testFooSetGet() throws Exception {
		DatabaseFieldConfig fieldConfig =
				DatabaseFieldConfig.fromField(databaseType, "foo", Foo.class.getDeclaredField("field"));

		assertNull(fieldConfig.getIndexName());
		String indexName = "hello";
		fieldConfig.setIndexName(indexName);
		assertEquals(indexName, fieldConfig.getIndexName());

		assertNull(fieldConfig.getUniqueIndexName());
		String uniqueIndex = "fpwoejfwf";
		fieldConfig.setUniqueIndexName(uniqueIndex);
		assertEquals(uniqueIndex, fieldConfig.getUniqueIndexName());

		assertNull(fieldConfig.getFormat());
		String format = "fewjfwe";
		fieldConfig.setFormat(format);
		assertEquals(format, fieldConfig.getFormat());

		assertFalse(fieldConfig.isThrowIfNull());
		boolean throwIfNull = true;
		fieldConfig.setThrowIfNull(throwIfNull);
		assertTrue(fieldConfig.isThrowIfNull());

		assertEquals(1, fieldConfig.getMaxEagerForeignCollectionLevel());
		int maxLevel = 123123;
		fieldConfig.setMaxEagerForeignCollectionLevel(maxLevel);
		assertEquals(maxLevel, fieldConfig.getMaxEagerForeignCollectionLevel());

		assertEquals(DatabaseField.MAX_FOREIGN_AUTO_REFRESH_LEVEL, fieldConfig.getMaxForeignAutoRefreshLevel());
		int maxRefresh = 1432323;
		fieldConfig.setMaxForeignAutoRefreshLevel(maxRefresh);
		assertEquals(maxRefresh, fieldConfig.getMaxForeignAutoRefreshLevel());

		assertFalse(fieldConfig.isForeignCollection());
		boolean foreignCollection = true;
		fieldConfig.setForeignCollection(foreignCollection);
		assertTrue(fieldConfig.isForeignCollection());

		assertFalse(fieldConfig.isForeignAutoRefresh());
		boolean foreignAutoRefresh = true;
		fieldConfig.setForeignAutoRefresh(foreignAutoRefresh);
		assertTrue(fieldConfig.isForeignAutoRefresh());

		assertNull(fieldConfig.getForeignCollectionOrderColumn());
		String columnName = "wpofjewfpeff";
		fieldConfig.setForeignCollectionOrderColumn(columnName);
		assertEquals(columnName, fieldConfig.getForeignCollectionOrderColumn());
	}

	@Test
	public void testNotPersisted() throws Exception {
		DatabaseFieldConfig fieldConfig =
				DatabaseFieldConfig.fromField(databaseType, "foo", NotPersisted.class.getDeclaredField("field"));
		assertNull(fieldConfig);
	}

	@Test
	public void testIndexNames() throws Exception {
		String tableName = "table1";
		DatabaseFieldConfig fieldConfig =
				DatabaseFieldConfig.fromField(databaseType, tableName, IndexName.class.getDeclaredField("field1"));
		assertEquals(tableName + "_" + IndexName.INDEX_COLUMN_NAME + "_idx", fieldConfig.getIndexName());
		fieldConfig =
				DatabaseFieldConfig.fromField(databaseType, tableName, IndexName.class.getDeclaredField("field2"));
		assertEquals(tableName + "_" + IndexName.UNIQUE_INDEX_COLUMN_NAME + "_idx", fieldConfig.getUniqueIndexName());
	}

	private final static String FIELD_START = "# --field-start--\n";
	private final static String FIELD_END = "# --field-end--\n";
	private final static String MAX_FOREIGN_CONSTANT = "maxForeignAutoRefreshLevel=2\n";
	private final static String MAX_EAGER_CONSTANT = "maxEagerForeignCollectionLevel=1\n";

	@Test
	public void testConfigFile() throws Exception {
		DatabaseFieldConfig config = new DatabaseFieldConfig();
		StringBuilder body = new StringBuilder();
		StringWriter writer = new StringWriter();
		BufferedWriter buffer = new BufferedWriter(writer);

		String fieldName = "pwojfpweofjwefw";
		config.setFieldName(fieldName);
		body.append("fieldName=").append(fieldName).append("\n");
		checkConfigOutput(config, body, true, true, writer, buffer);

		String columnName = "pwefw";
		config.setColumnName(columnName);
		body.append("columnName=").append(columnName).append("\n");
		checkConfigOutput(config, body, true, true, writer, buffer);

		DataPersister dataPersister = DataType.BYTE_OBJ.getDataPersister();
		config.setDataPersister(dataPersister);
		body.append("dataPersister=").append(DataType.BYTE_OBJ).append("\n");
		checkConfigOutput(config, body, true, true, writer, buffer);

		String defaultValue = "pwefw";
		config.setDefaultValue(defaultValue);
		body.append("defaultValue=").append(defaultValue).append("\n");
		checkConfigOutput(config, body, true, true, writer, buffer);

		int width = 13212;
		config.setWidth(width);
		body.append("width=").append(width).append("\n");
		checkConfigOutput(config, body, true, true, writer, buffer);

		config.setCanBeNull(false);
		checkConfigOutput(config, body, true, true, writer, buffer);
		config.setCanBeNull(true);
		body.append("canBeNull=true").append("\n");

		config.setId(false);
		checkConfigOutput(config, body, true, true, writer, buffer);
		config.setId(true);
		body.append("id=true").append("\n");
		checkConfigOutput(config, body, true, true, writer, buffer);

		config.setGeneratedId(false);
		checkConfigOutput(config, body, true, true, writer, buffer);
		config.setGeneratedId(true);
		body.append("generatedId=true").append("\n");
		checkConfigOutput(config, body, true, true, writer, buffer);

		String generatedIdSequence = "24332423";
		config.setGeneratedIdSequence(generatedIdSequence);
		body.append("generatedIdSequence=").append(generatedIdSequence).append("\n");
		checkConfigOutput(config, body, true, true, writer, buffer);

		config.setForeign(false);
		checkConfigOutput(config, body, true, true, writer, buffer);
		config.setForeign(true);
		body.append("foreign=true").append("\n");
		checkConfigOutput(config, body, true, true, writer, buffer);

		config.setUseGetSet(false);
		checkConfigOutput(config, body, true, true, writer, buffer);
		config.setUseGetSet(true);
		body.append("useGetSet=true").append("\n");
		checkConfigOutput(config, body, true, true, writer, buffer);

		Enum<?> enumValue = OurEnum.FIRST;
		config.setUnknownEnumValue(enumValue);
		body.append("unknownEnumValue=")
				.append(enumValue.getClass().getName())
				.append('#')
				.append(enumValue)
				.append("\n");
		checkConfigOutput(config, body, true, true, writer, buffer);

		config.setThrowIfNull(false);
		checkConfigOutput(config, body, true, true, writer, buffer);
		config.setThrowIfNull(true);
		body.append("throwIfNull=true").append("\n");
		checkConfigOutput(config, body, true, true, writer, buffer);

		String format = "wpgjogwjpogwjp";
		config.setFormat(format);
		body.append("format=").append(format).append("\n");
		checkConfigOutput(config, body, true, true, writer, buffer);

		config.setUnique(false);
		checkConfigOutput(config, body, true, true, writer, buffer);
		config.setUnique(true);
		body.append("unique=true").append("\n");
		checkConfigOutput(config, body, true, true, writer, buffer);

		config.setUniqueCombo(false);
		checkConfigOutput(config, body, true, true, writer, buffer);
		config.setUniqueCombo(true);
		body.append("uniqueCombo=true").append("\n");
		checkConfigOutput(config, body, true, true, writer, buffer);

		String indexName = "wfewjpwepjjp";
		config.setIndexName(indexName);
		body.append("indexName=").append(indexName).append("\n");
		checkConfigOutput(config, body, true, true, writer, buffer);

		String uniqueIndexName = "w2254423fewjpwepjjp";
		config.setUniqueIndexName(uniqueIndexName);
		body.append("uniqueIndexName=").append(uniqueIndexName).append("\n");
		checkConfigOutput(config, body, true, true, writer, buffer);

		config.setForeignAutoRefresh(false);
		checkConfigOutput(config, body, true, true, writer, buffer);
		config.setForeignAutoRefresh(true);
		body.append("foreignAutoRefresh=true").append("\n");
		checkConfigOutput(config, body, true, true, writer, buffer);

		int maxForeign = 2112;
		config.setMaxForeignAutoRefreshLevel(maxForeign);
		body.append("maxForeignAutoRefreshLevel=").append(maxForeign).append("\n");
		checkConfigOutput(config, body, false, true, writer, buffer);

		config.setForeignCollection(false);
		checkConfigOutput(config, body, false, true, writer, buffer);
		config.setForeignCollection(true);
		body.append("foreignCollection=true").append("\n");
		checkConfigOutput(config, body, false, true, writer, buffer);

		config.setForeignCollectionEager(false);
		checkConfigOutput(config, body, false, true, writer, buffer);
		config.setForeignCollectionEager(true);
		body.append("foreignCollectionEager=true").append("\n");
		checkConfigOutput(config, body, false, true, writer, buffer);

		String foreignOrderColumn = "w225fwhi4jp";
		config.setForeignCollectionOrderColumn(foreignOrderColumn);
		body.append("foreignCollectionOrderColumn=").append(foreignOrderColumn).append("\n");
		checkConfigOutput(config, body, false, true, writer, buffer);

		int maxEager = 341;
		config.setMaxEagerForeignCollectionLevel(maxEager);
		body.append("maxEagerForeignCollectionLevel=").append(maxEager).append("\n");
		checkConfigOutput(config, body, false, false, writer, buffer);

		@SuppressWarnings("unchecked")
		Class<DataPersister> clazz = (Class<DataPersister>) DataType.CHAR.getDataPersister().getClass();
		config.setPersisterClass(clazz);
		body.append("persisterClass=").append(clazz.getName()).append("\n");
		checkConfigOutput(config, body, false, false, writer, buffer);

		config.setAllowGeneratedIdInsert(false);
		checkConfigOutput(config, body, false, false, writer, buffer);
		config.setAllowGeneratedIdInsert(true);
		body.append("allowGeneratedIdInsert=true").append("\n");
		checkConfigOutput(config, body, false, false, writer, buffer);
	}

	protected class Foo {
		@DatabaseField(canBeNull = true)
		String field;
	}

	protected class NotPersisted {
		@DatabaseField(persisted = false)
		String field;
	}

	protected class IndexName {
		public static final String INDEX_COLUMN_NAME = "index";
		public static final String UNIQUE_INDEX_COLUMN_NAME = "unique";
		@DatabaseField(index = true, columnName = INDEX_COLUMN_NAME)
		String field1;
		@DatabaseField(uniqueIndex = true, columnName = UNIQUE_INDEX_COLUMN_NAME)
		String field2;
	}

	protected class Serial implements Serializable {
		private static final long serialVersionUID = 6826474171714263950L;
		@DatabaseField(canBeNull = true)
		String field;
	}

	protected class JavaxAnno {
		// no annotations so is not a configured column
		int notColumn;
		@Column
		@Id
		@GeneratedValue
		int id;
		@Column(name = STUFF_FIELD_NAME)
		String stuff;
		@Column(length = LENGTH_LENGTH)
		String length;
		@Column(nullable = false)
		String nullable;
		@ManyToOne
		Foo foo;
		@OneToOne
		Foo foo2;
	}

	protected class JavaxAnnoJustId {
		@Column
		@Id
		String id;
	}

	protected class JavaxGetSet {
		@Column
		String id;
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
	}

	protected class JavaxUnique {
		@Column(unique = true)
		String id;
	}

	protected class BadUnknownVal {
		// not a valid enum name
		@DatabaseField(unknownEnumName = "THIRD")
		OurEnum ourEnum;
	}

	private enum OurEnum {
		FIRST,
		SECOND, ;
	}

	private static class StubDatabaseType extends BaseDatabaseType {
		@Override
		public String getDriverClassName() {
			return "foo.bar.baz";
		}
		public String getDatabaseName() {
			return "fake";
		}
		public boolean isDatabaseUrlThisType(String url, String dbTypePart) {
			return false;
		}
	}

	protected static class Index {
		@DatabaseField(index = true)
		String stuff;
		public Index() {
		}
	}

	protected static class ComboIndex {
		@DatabaseField(indexName = INDEX_NAME)
		String stuff;
		@DatabaseField(indexName = INDEX_NAME)
		long junk;
		public ComboIndex() {
		}
		public static final String INDEX_NAME = "stuffjunk";
	}

	protected static class DefaultString {
		public static final String STUFF_DEFAULT = "";
		public static final String JUNK_DEFAULT = "xyzzy";
		@DatabaseField(defaultValue = STUFF_DEFAULT)
		String stuff;
		@DatabaseField(defaultValue = JUNK_DEFAULT)
		String junk;
		@DatabaseField
		String none;
		public DefaultString() {
		}
	}

	private void checkConfigOutput(DatabaseFieldConfig config, StringBuilder body, boolean addMaxForeign,
			boolean addMaxEager, StringWriter writer, BufferedWriter buffer) throws Exception {
		config.write(buffer);
		buffer.flush();
		StringBuilder output = new StringBuilder();
		output.append(FIELD_START).append(body);
		if (addMaxForeign) {
			output.append(MAX_FOREIGN_CONSTANT);
		}
		if (addMaxEager) {
			output.append(MAX_EAGER_CONSTANT);
		}
		output.append(FIELD_END);
		assertEquals(output.toString(), writer.toString());
		StringReader reader = new StringReader(writer.toString());
		DatabaseFieldConfig configCopy = DatabaseFieldConfig.fromReader(new BufferedReader(reader));
		assertTrue(isSame(config, configCopy));
		writer.getBuffer().setLength(0);
	}

	private boolean isSame(DatabaseFieldConfig config1, DatabaseFieldConfig config2) {
		EqualsBuilder eb = new EqualsBuilder();
		eb.append(config1.getFieldName(), config2.getFieldName());
		eb.append(config1.getColumnName(), config2.getColumnName());
		eb.append(config1.getDataPersister(), config2.getDataPersister());
		eb.append(config1.getDefaultValue(), config2.getDefaultValue());
		eb.append(config1.getWidth(), config2.getWidth());
		eb.append(config1.isCanBeNull(), config2.isCanBeNull());
		eb.append(config1.isId(), config2.isId());
		eb.append(config1.isGeneratedId(), config2.isGeneratedId());
		eb.append(config1.getGeneratedIdSequence(), config2.getGeneratedIdSequence());
		eb.append(config1.isForeign(), config2.isForeign());
		eb.append(config1.getForeignTableConfig(), config2.getForeignTableConfig());
		eb.append(config1.isUseGetSet(), config2.isUseGetSet());
		eb.append(config1.getUnknownEnumValue(), config2.getUnknownEnumValue());
		eb.append(config1.isThrowIfNull(), config2.isThrowIfNull());
		eb.append(config1.getFormat(), config2.getFormat());
		eb.append(config1.isUnique(), config2.isUnique());
		eb.append(config1.isUniqueCombo(), config2.isUniqueCombo());
		eb.append(config1.getIndexName(), config2.getIndexName());
		eb.append(config1.getUniqueIndexName(), config2.getUniqueIndexName());
		eb.append(config1.isForeignAutoRefresh(), config2.isForeignAutoRefresh());
		eb.append(config1.getMaxForeignAutoRefreshLevel(), config2.getMaxForeignAutoRefreshLevel());
		eb.append(config1.isForeignCollection(), config2.isForeignCollection());
		eb.append(config1.isForeignCollectionEager(), config2.isForeignCollectionEager());
		eb.append(config1.getForeignCollectionOrderColumn(), config2.getForeignCollectionOrderColumn());
		eb.append(config1.getMaxEagerForeignCollectionLevel(), config2.getMaxEagerForeignCollectionLevel());
		eb.append(config1.getPersisterClass(), config2.getPersisterClass());
		eb.append(config1.isAllowGeneratedIdInsert(), config2.isAllowGeneratedIdInsert());
		return eb.isEquals();
	}
}
