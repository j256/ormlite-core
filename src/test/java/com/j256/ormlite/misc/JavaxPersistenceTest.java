package com.j256.ormlite.misc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.lang.reflect.Field;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.h2.H2DatabaseType;

public class JavaxPersistenceTest extends BaseCoreTest {

	private static final String STUFF_FIELD_NAME = "notstuff";
	private static final String JAVAX_ENTITY_NAME = "notjavax";

	@Test
	public void testConversions() throws Exception {
		Field[] fields = Javax.class.getDeclaredFields();
		for (Field field : fields) {
			DatabaseFieldConfig config = JavaxPersistence.createFieldConfig(databaseType, field);
			if (field.getName().equals("generatedId")) {
				assertFalse(config.isId());
				assertTrue(config.isGeneratedId());
				assertEquals(field.getName(), config.getFieldName());
				assertNull(config.getColumnName());
			} else if (field.getName().equals("id")) {
				assertTrue(config.isId());
				assertFalse(config.isGeneratedId());
				assertEquals(field.getName(), config.getFieldName());
				assertNull(config.getColumnName());
			} else if (field.getName().equals("stuff")) {
				assertFalse(config.isId());
				assertFalse(config.isGeneratedId());
				assertEquals(field.getName(), config.getFieldName());
				assertEquals(STUFF_FIELD_NAME, config.getColumnName());
			} else if (field.getName().equals("unknown")) {
				assertFalse(config.isId());
				assertFalse(config.isGeneratedId());
				assertEquals(DataType.UNKNOWN, config.getDataType());
				assertEquals(field.getName(), config.getFieldName());
				assertNull(config.getColumnName());
			} else if (field.getName().equals("foreignManyToOne")) {
				assertFalse(config.isId());
				assertFalse(config.isGeneratedId());
				assertTrue(config.isForeign());
				assertEquals(DataType.UNKNOWN, config.getDataType());
				assertEquals(field.getName(), config.getFieldName());
				assertNull(config.getColumnName());
			} else if (field.getName().equals("foreignOneToOne")) {
				assertFalse(config.isId());
				assertFalse(config.isGeneratedId());
				assertTrue(config.isForeign());
				assertEquals(DataType.UNKNOWN, config.getDataType());
				assertEquals(field.getName(), config.getFieldName());
				assertNull(config.getColumnName());
			} else {
				System.err.println("\n\n\nUnknown field: " + field.getName());
			}
		}
	}

	@Test
	public void testTableName() throws Exception {
		assertEquals(JAVAX_ENTITY_NAME, JavaxPersistence.getEntityName(Javax.class));
		assertNull(JavaxPersistence.getEntityName(EntityNoName.class));
	}

	@Test
	public void testUpperCaseFieldNames() throws Exception {
		Field[] fields = Javax.class.getDeclaredFields();
		UpperCaseFieldDatabaseType ucDatabaseType = new UpperCaseFieldDatabaseType();
		for (Field field : fields) {
			DatabaseFieldConfig config = JavaxPersistence.createFieldConfig(ucDatabaseType, field);
			if (field.getName().equals("id")) {
				assertTrue(config.isId());
				assertFalse(config.isGeneratedId());
				assertEquals("ID", config.getFieldName());
			}
		}
	}

	/* ======================================================================================================= */

	@Entity(name = JAVAX_ENTITY_NAME)
	protected static class Javax {
		@Id
		@GeneratedValue
		public int generatedId;
		@Id
		public int id;
		@Column(name = STUFF_FIELD_NAME)
		public String stuff;
		// this thing is not serializable
		@Column
		public Javax unknown;
		@ManyToOne
		Foreign foreignManyToOne;
		@OneToOne
		Foreign foreignOneToOne;
		public Javax() {
		}
	}

	@Entity
	protected static class EntityNoName {
	}

	protected static class SerialField implements Serializable {
		private static final long serialVersionUID = -3883857119616908868L;
		String stuff;
		public SerialField() {
		}
	}

	protected static class Foreign {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		String stuff;
	}

	private static class UpperCaseFieldDatabaseType extends H2DatabaseType {
		@Override
		public boolean isEntityNamesMustBeUpCase() {
			return true;
		}
	}
}
