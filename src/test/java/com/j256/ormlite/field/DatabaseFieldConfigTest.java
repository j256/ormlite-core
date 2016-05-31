package com.j256.ormlite.field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.lang.reflect.Field;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.db.BaseDatabaseType;
import com.j256.ormlite.db.DatabaseType;

public class DatabaseFieldConfigTest extends BaseCoreTest {

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

		assertTrue(config.isCanBeNull());
		config.setCanBeNull(false);
		assertFalse(config.isCanBeNull());

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
		assertTrue(fields.length >= 7);

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
		assertEquals(tableName + "_" + fields[0].getName() + "_idx", fieldConfig.getIndexName(tableName));
	}

	@Test
	public void testComboIndex() throws Exception {
		Field[] fields = ComboIndex.class.getDeclaredFields();
		assertTrue(fields.length >= 2);
		DatabaseFieldConfig fieldConfig = DatabaseFieldConfig.fromField(databaseType, "foo", fields[0]);
		String tableName = "foo";
		assertEquals(ComboIndex.INDEX_NAME, fieldConfig.getIndexName(tableName));
		fieldConfig = DatabaseFieldConfig.fromField(databaseType, tableName, fields[1]);
		assertEquals(ComboIndex.INDEX_NAME, fieldConfig.getIndexName(tableName));
	}

	@Test
	public void testDefaultValue() throws Exception {
		DatabaseFieldConfig fieldConfig = DatabaseFieldConfig.fromField(databaseType, "defaultstring",
				DefaultString.class.getDeclaredField("stuff"));
		assertNotNull(fieldConfig.getDefaultValue());
		assertEquals(DefaultString.STUFF_DEFAULT, fieldConfig.getDefaultValue());
		fieldConfig = DatabaseFieldConfig.fromField(databaseType, "defaultstring",
				DefaultString.class.getDeclaredField("junk"));
		assertNotNull(fieldConfig.getDefaultValue());
		assertEquals(DefaultString.JUNK_DEFAULT, fieldConfig.getDefaultValue());
		fieldConfig = DatabaseFieldConfig.fromField(databaseType, "defaultstring",
				DefaultString.class.getDeclaredField("none"));
		assertNull(fieldConfig.getDefaultValue());
	}

	@Test
	public void testFooSetGet() throws Exception {
		String tableName = "foo";
		DatabaseFieldConfig fieldConfig =
				DatabaseFieldConfig.fromField(databaseType, tableName, Foo.class.getDeclaredField("field"));

		assertNull(fieldConfig.getIndexName(tableName));
		String indexName = "hello";
		fieldConfig.setIndexName(indexName);
		assertEquals(indexName, fieldConfig.getIndexName(tableName));

		assertNull(fieldConfig.getUniqueIndexName(tableName));
		String uniqueIndex = "fpwoejfwf";
		fieldConfig.setUniqueIndexName(uniqueIndex);
		assertEquals(uniqueIndex, fieldConfig.getUniqueIndexName(tableName));

		assertNull(fieldConfig.getFormat());
		String format = "fewjfwe";
		fieldConfig.setFormat(format);
		assertEquals(format, fieldConfig.getFormat());

		assertFalse(fieldConfig.isThrowIfNull());
		boolean throwIfNull = true;
		fieldConfig.setThrowIfNull(throwIfNull);
		assertTrue(fieldConfig.isThrowIfNull());

		assertEquals(1, fieldConfig.getForeignCollectionMaxEagerLevel());
		int maxLevel = 123123;
		fieldConfig.setForeignCollectionMaxEagerLevel(maxLevel);
		assertEquals(maxLevel, fieldConfig.getForeignCollectionMaxEagerLevel());

		assertEquals(DatabaseFieldConfig.NO_MAX_FOREIGN_AUTO_REFRESH_LEVEL_SPECIFIED,
				fieldConfig.getMaxForeignAutoRefreshLevel());
		int maxRefresh = 1432323;
		fieldConfig.setMaxForeignAutoRefreshLevel(maxRefresh);
		// need to do this to get auto-refresh
		fieldConfig.setForeignAutoRefresh(true);
		assertEquals(maxRefresh, fieldConfig.getMaxForeignAutoRefreshLevel());
		fieldConfig.setForeignAutoRefresh(false);

		assertFalse(fieldConfig.isForeignCollection());
		boolean foreignCollection = true;
		fieldConfig.setForeignCollection(foreignCollection);
		assertTrue(fieldConfig.isForeignCollection());

		assertFalse(fieldConfig.isForeignAutoRefresh());
		boolean foreignAutoRefresh = true;
		fieldConfig.setForeignAutoRefresh(foreignAutoRefresh);
		assertTrue(fieldConfig.isForeignAutoRefresh());

		assertNull(fieldConfig.getForeignCollectionOrderColumnName());
		String columnName = "wpofjewfpeff";
		fieldConfig.setForeignCollectionOrderColumnName(columnName);
		assertEquals(columnName, fieldConfig.getForeignCollectionOrderColumnName());
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
		assertEquals(tableName + "_" + IndexName.INDEX_COLUMN_NAME + "_idx", fieldConfig.getIndexName(tableName));
		fieldConfig =
				DatabaseFieldConfig.fromField(databaseType, tableName, IndexName.class.getDeclaredField("field2"));
		assertEquals(tableName + "_" + IndexName.UNIQUE_INDEX_COLUMN_NAME + "_idx",
				fieldConfig.getUniqueIndexName(tableName));
	}

	@Test
	public void testGetSetIs() throws Exception {
		Field stuffField = BooleanGetSetIs.class.getDeclaredField("stuff");
		DatabaseFieldConfig.findGetMethod(stuffField, true);
		DatabaseFieldConfig.findSetMethod(stuffField, true);
		Field boolField = BooleanGetSetIs.class.getDeclaredField("bool");
		DatabaseFieldConfig.findGetMethod(boolField, true);
		DatabaseFieldConfig.findSetMethod(boolField, true);
	}

	@Test
	public void testGetSetIsErrors() throws Exception {
		Field stuffField = BooleanGetSetIsButNoMethods.class.getDeclaredField("stuff");
		try {
			DatabaseFieldConfig.findGetMethod(stuffField, true);
			fail("should have thrown");
		} catch (IllegalArgumentException iae) {
			// expected
		}
		try {
			DatabaseFieldConfig.findSetMethod(stuffField, true);
			fail("should have thrown");
		} catch (IllegalArgumentException iae) {
			// expected
		}
		Field boolField = BooleanGetSetIsButNoMethods.class.getDeclaredField("bool");
		try {
			DatabaseFieldConfig.findGetMethod(boolField, true);
			fail("should have thrown");
		} catch (IllegalArgumentException iae) {
			// expected
		}
		try {
			DatabaseFieldConfig.findSetMethod(boolField, true);
			fail("should have thrown");
		} catch (IllegalArgumentException iae) {
			// expected
		}
	}

	/* ================================================================================================ */

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

	protected static class Serial implements Serializable {
		private static final long serialVersionUID = 6826474171714263950L;
		@DatabaseField(canBeNull = true)
		String field;
	}

	protected static class JavaxAnno {
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
		SECOND,
		// end
		;
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

	protected static class BooleanGetSetIs {
		@DatabaseField(useGetSet = true)
		String stuff;
		@DatabaseField(useGetSet = true)
		boolean bool;

		public BooleanGetSetIs() {
		}

		public String getStuff() {
			return stuff;
		}

		public void setStuff(String stuff) {
			this.stuff = stuff;
		}

		public boolean isBool() {
			return bool;
		}

		public void setBool(boolean bool) {
			this.bool = bool;
		}
	}

	protected static class BooleanGetSetIsButNoMethods {
		@DatabaseField(useGetSet = true)
		String stuff;
		@DatabaseField(useGetSet = true)
		boolean bool;

		public BooleanGetSetIsButNoMethods() {
		}
	}
}
