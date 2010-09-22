package com.j256.ormlite.field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.lang.reflect.Field;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.junit.Test;

import com.j256.ormlite.db.BaseDatabaseType;
import com.j256.ormlite.db.DatabaseType;

public class DatabaseFieldConfigTest {

	@Test
	public void testGetSet() {
		DatabaseFieldConfig config = new DatabaseFieldConfig();
		String str;

		assertNull(config.getColumnName());
		str = "name";
		config.setColumnName(str);
		assertEquals(str, config.getColumnName());

		assertNull(config.getDefaultValue());
		str = "default";
		config.setDefaultValue(str);
		assertEquals(str, config.getDefaultValue());

		assertNull(config.getGeneratedIdSequence());
		str = "seq";
		config.setGeneratedIdSequence(str);
		assertEquals(str, config.getGeneratedIdSequence());

		assertEquals(DataType.UNKNOWN, config.getDataType());
		DataType jdbcType = DataType.DOUBLE;
		config.setDataType(jdbcType);
		assertEquals(jdbcType, config.getDataType());

		assertEquals(0, config.getWidth());
		int width = 21312312;
		config.setWidth(width);
		assertEquals(width, config.getWidth());

		assertFalse(config.isCanBeNull());
		config.setCanBeNull(true);
		assertTrue(config.isCanBeNull());

		assertFalse(config.isForeign());
		config.setForeign(true);
		assertTrue(config.isForeign());

		assertFalse(config.isGeneratedId());
		config.setGeneratedId(true);
		assertTrue(config.isGeneratedId());

		assertFalse(config.isId());
		config.setId(true);
		assertTrue(config.isId());

		assertFalse(config.isUseGetSet());
		config.setUseGetSet(true);
		assertTrue(config.isUseGetSet());
	}

	@Test
	public void testFromDbField() throws Exception {
		Field[] fields = Foo.class.getDeclaredFields();
		DatabaseType databaseType = new StubDatabaseType();
		assertTrue(fields.length >= 1);
		DatabaseFieldConfig config = DatabaseFieldConfig.fromField(databaseType, fields[0]);
		assertNotNull(config);
		assertTrue(config.isCanBeNull());
		assertEquals(fields[0].getName(), config.getFieldName());
	}

	private final static String STUFF_FIELD_NAME = "notstuff";
	private final static int LENGTH_LENGTH = 100;

	@Test
	public void testJavaxAnnotations() throws Exception {
		DatabaseType databaseType = new StubDatabaseType();
		Field[] fields = JavaxAnno.class.getDeclaredFields();
		assertTrue(fields.length >= 8);

		// not a column
		assertNull(DatabaseFieldConfig.fromField(databaseType, fields[0]));

		DatabaseFieldConfig config = DatabaseFieldConfig.fromField(databaseType, fields[1]);
		assertNotNull(config);
		assertFalse(config.isId());
		assertTrue(config.isGeneratedId());
		assertFalse(config.isUseGetSet());
		assertEquals(fields[1].getName(), config.getFieldName());

		config = DatabaseFieldConfig.fromField(databaseType, fields[2]);
		assertNotNull(config);
		assertFalse(config.isUseGetSet());
		assertEquals(STUFF_FIELD_NAME, config.getColumnName());

		config = DatabaseFieldConfig.fromField(databaseType, fields[3]);
		assertNotNull(config);
		assertEquals(LENGTH_LENGTH, config.getWidth());
		assertFalse(config.isUseGetSet());
		assertEquals(fields[3].getName(), config.getFieldName());

		config = DatabaseFieldConfig.fromField(databaseType, fields[4]);
		assertNotNull(config);
		assertFalse(config.isCanBeNull());
		assertFalse(config.isUseGetSet());
		assertEquals(fields[4].getName(), config.getFieldName());

		config = DatabaseFieldConfig.fromField(databaseType, fields[5]);
		assertNotNull(config);
		assertTrue(config.isForeign());
		assertEquals(DataType.UNKNOWN, config.getDataType());
		assertEquals(fields[5].getName(), config.getFieldName());

		config = DatabaseFieldConfig.fromField(databaseType, fields[6]);
		assertNotNull(config);
		assertTrue(config.isForeign());
		assertEquals(DataType.UNKNOWN, config.getDataType());
		assertEquals(fields[6].getName(), config.getFieldName());

		config = DatabaseFieldConfig.fromField(databaseType, fields[7]);
		assertNotNull(config);
		assertFalse(config.isForeign());
		assertEquals(DataType.SERIALIZABLE, config.getDataType());
		assertEquals(fields[7].getName(), config.getFieldName());
	}

	@Test
	public void testJavaxJustId() throws Exception {
		DatabaseType databaseType = new StubDatabaseType();
		Field[] fields = JavaxAnnoJustId.class.getDeclaredFields();
		assertTrue(fields.length >= 1);

		DatabaseFieldConfig config = DatabaseFieldConfig.fromField(databaseType, fields[0]);
		assertNotNull(config);
		assertTrue(config.isId());
		assertFalse(config.isGeneratedId());
		assertFalse(config.isUseGetSet());
		assertEquals(fields[0].getName(), config.getFieldName());
	}

	@Test
	public void testJavaxGetSet() throws Exception {
		DatabaseType databaseType = new StubDatabaseType();
		Field[] fields = JavaxGetSet.class.getDeclaredFields();
		assertTrue(fields.length >= 1);

		DatabaseFieldConfig config = DatabaseFieldConfig.fromField(databaseType, fields[0]);
		assertNotNull(config);
		assertTrue(config.isUseGetSet());
		assertEquals(fields[0].getName(), config.getFieldName());
	}

	@Test
	public void testJavaxUnique() throws Exception {
		DatabaseType databaseType = new StubDatabaseType();
		Field[] fields = JavaxUnique.class.getDeclaredFields();
		assertTrue(fields.length >= 1);

		DatabaseFieldConfig config = DatabaseFieldConfig.fromField(databaseType, fields[0]);
		assertNotNull(config);
		assertTrue(config.isUnique());
		assertEquals(fields[0].getName(), config.getFieldName());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUnknownEnumVal() throws Exception {
		DatabaseType databaseType = new StubDatabaseType();
		Field[] fields = BadUnknownVal.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		DatabaseFieldConfig.fromField(databaseType, fields[0]);
	}

	protected class Foo {
		@DatabaseField(canBeNull = true)
		String field;
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
		// serializable
		@Column
		Serial serial;
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

	private class StubDatabaseType extends BaseDatabaseType {
		public String getDriverClassName() {
			return "foo.bar.baz";
		}
		public String getDriverUrlPart() {
			return "foo";
		}
	}
}
