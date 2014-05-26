package com.j256.ormlite.misc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.SQLException;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Version;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.field.types.EnumIntegerType;
import com.j256.ormlite.field.types.EnumStringType;
import com.j256.ormlite.h2.H2DatabaseType;

public class JavaxPersistenceTest extends BaseCoreTest {

	private static final String STUFF_FIELD_NAME = "notstuff";
	private static final String JOIN_FIELD_NAME = "notjoinfield";
	private static final String JAVAX_ENTITY_NAME = "notjavax";
	private static final String COLUMN_DEFINITION = "column definition";

	@Test
	public void testConversions() throws Exception {
		Field[] fields = Javax.class.getDeclaredFields();
		for (Field field : fields) {
			DatabaseFieldConfig config = new JavaxPersistenceImpl().createFieldConfig(databaseType, field);
			if (field.getName().equals("generatedId")) {
				assertFalse(config.isId());
				assertTrue(config.isGeneratedId());
				assertFalse(config.isUnique());
				assertTrue(config.isCanBeNull());
				assertFalse(config.isVersion());
				assertEquals(field.getName(), config.getFieldName());
				assertNull(config.getColumnName());
				assertNull(config.getColumnDefinition());
			} else if (field.getName().equals("id")) {
				assertTrue(config.isId());
				assertFalse(config.isGeneratedId());
				assertFalse(config.isUnique());
				assertTrue(config.isCanBeNull());
				assertFalse(config.isVersion());
				assertEquals(field.getName(), config.getFieldName());
				assertNull(config.getColumnName());
				assertNull(config.getColumnDefinition());
			} else if (field.getName().equals("stuff")) {
				assertFalse(config.isId());
				assertFalse(config.isGeneratedId());
				assertFalse(config.isUnique());
				assertTrue(config.isCanBeNull());
				assertFalse(config.isVersion());
				assertEquals(field.getName(), config.getFieldName());
				assertEquals(STUFF_FIELD_NAME, config.getColumnName());
				assertNull(config.getColumnDefinition());
			} else if (field.getName().equals("unknown")) {
				assertFalse(config.isId());
				assertFalse(config.isGeneratedId());
				assertFalse(config.isUnique());
				assertTrue(config.isCanBeNull());
				assertFalse(config.isVersion());
				assertNull(config.getDataPersister());
				assertEquals(field.getName(), config.getFieldName());
				assertNull(config.getColumnName());
				assertNull(config.getColumnDefinition());
			} else if (field.getName().equals("foreignManyToOne")) {
				assertFalse(config.isId());
				assertFalse(config.isGeneratedId());
				assertTrue(config.isForeign());
				assertFalse(config.isUnique());
				assertTrue(config.isCanBeNull());
				assertFalse(config.isVersion());
				assertNull(config.getDataPersister());
				assertEquals(field.getName(), config.getFieldName());
				assertNull(config.getColumnName());
				assertNull(config.getColumnDefinition());
			} else if (field.getName().equals("foreignOneToOne")) {
				assertFalse(config.isId());
				assertFalse(config.isGeneratedId());
				assertTrue(config.isForeign());
				assertFalse(config.isUnique());
				assertTrue(config.isCanBeNull());
				assertFalse(config.isVersion());
				assertNull(config.getDataPersister());
				assertEquals(field.getName(), config.getFieldName());
				assertNull(config.getColumnName());
				assertNull(config.getColumnDefinition());
			} else if (field.getName().equals("joinFieldName")) {
				assertFalse(config.isId());
				assertFalse(config.isGeneratedId());
				assertTrue(config.isForeign());
				assertFalse(config.isUnique());
				assertTrue(config.isCanBeNull());
				assertFalse(config.isVersion());
				assertNull(config.getDataPersister());
				assertEquals(field.getName(), config.getFieldName());
				assertEquals(JOIN_FIELD_NAME, config.getColumnName());
				assertNull(config.getColumnDefinition());
			} else if (field.getName().equals("columnDefinition")) {
				assertFalse(config.isId());
				assertFalse(config.isGeneratedId());
				assertFalse(config.isForeign());
				assertFalse(config.isUnique());
				assertFalse(config.isVersion());
				assertTrue(config.isCanBeNull());
				assertEquals(COLUMN_DEFINITION, config.getColumnDefinition());
			} else if (field.getName().equals("uniqueColumn")) {
				assertFalse(config.isId());
				assertFalse(config.isGeneratedId());
				assertFalse(config.isForeign());
				assertTrue(config.isUnique());
				assertTrue(config.isCanBeNull());
				assertFalse(config.isVersion());
				assertNull(config.getColumnName());
				assertNull(config.getColumnDefinition());
			} else if (field.getName().equals("nullableColumn")) {
				assertFalse(config.isId());
				assertFalse(config.isGeneratedId());
				assertFalse(config.isForeign());
				assertFalse(config.isUnique());
				assertFalse(config.isCanBeNull());
				assertFalse(config.isVersion());
				assertNull(config.getColumnName());
				assertNull(config.getColumnDefinition());
			} else if (field.getName().equals("uniqueJoinColumn")) {
				assertFalse(config.isId());
				assertFalse(config.isGeneratedId());
				assertTrue(config.isForeign());
				assertTrue(config.isUnique());
				assertTrue(config.isCanBeNull());
				assertFalse(config.isVersion());
				assertNull(config.getColumnName());
				assertNull(config.getColumnDefinition());
			} else if (field.getName().equals("nullableJoinColumn")) {
				assertFalse(config.isId());
				assertFalse(config.isGeneratedId());
				assertTrue(config.isForeign());
				assertFalse(config.isUnique());
				assertFalse(config.isCanBeNull());
				assertFalse(config.isVersion());
				assertNull(config.getColumnName());
				assertNull(config.getColumnDefinition());
			} else if (field.getName().equals("ourEnumOrdinal")) {
				assertFalse(config.isId());
				assertFalse(config.isGeneratedId());
				assertFalse(config.isForeign());
				assertFalse(config.isUnique());
				assertFalse(config.isVersion());
				assertTrue(config.isCanBeNull());
				assertNull(config.getColumnName());
				assertNull(config.getColumnDefinition());
				assertTrue(config.getDataPersister() instanceof EnumIntegerType);
			} else if (field.getName().equals("ourEnumString")) {
				assertFalse(config.isId());
				assertFalse(config.isGeneratedId());
				assertFalse(config.isForeign());
				assertFalse(config.isUnique());
				assertFalse(config.isVersion());
				assertTrue(config.isCanBeNull());
				assertNull(config.getColumnName());
				assertNull(config.getColumnDefinition());
				assertTrue(config.getDataPersister() instanceof EnumStringType);
			} else if (field.getName().equals("version")) {
				assertFalse(config.isId());
				assertFalse(config.isGeneratedId());
				assertFalse(config.isForeign());
				assertFalse(config.isUnique());
				assertTrue(config.isCanBeNull());
				assertTrue(config.isVersion());
				assertNull(config.getColumnName());
				assertNull(config.getColumnDefinition());
			} else if (field.getName().equals("basic")) {
				assertFalse(config.isId());
				assertFalse(config.isGeneratedId());
				assertFalse(config.isForeign());
				assertFalse(config.isUnique());
				assertTrue(config.isCanBeNull());
				assertFalse(config.isVersion());
				assertNull(config.getColumnName());
				assertNull(config.getColumnDefinition());
			} else if (field.getName().equals("basicNotOptional")) {
				assertFalse(config.isId());
				assertFalse(config.isGeneratedId());
				assertFalse(config.isForeign());
				assertFalse(config.isUnique());
				assertFalse(config.isCanBeNull());
				assertFalse(config.isVersion());
				assertNull(config.getColumnName());
				assertNull(config.getColumnDefinition());
			} else {
				System.err.println("\n\n\nUnknown field: " + field.getName());
			}
		}
	}

	@Test
	public void testTableName() {
		JavaxPersistenceConfigurer configurer = new JavaxPersistenceImpl();
		assertEquals(JAVAX_ENTITY_NAME, configurer.getEntityName(Javax.class));
		assertNull(configurer.getEntityName(EntityNoName.class));
	}

	@Test
	public void testUpperCaseFieldNames() throws Exception {
		Field[] fields = Javax.class.getDeclaredFields();
		UpperCaseFieldDatabaseType ucDatabaseType = new UpperCaseFieldDatabaseType();
		for (Field field : fields) {
			DatabaseFieldConfig config = new JavaxPersistenceImpl().createFieldConfig(ucDatabaseType, field);
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
		@ManyToOne
		@JoinColumn(name = JOIN_FIELD_NAME)
		Foreign joinFieldName;
		@Column(columnDefinition = COLUMN_DEFINITION)
		String columnDefinition;
		@Column(unique = true)
		String uniqueColumn;
		@Column(nullable = false)
		String nullableColumn;
		@ManyToOne
		@JoinColumn(unique = true)
		String uniqueJoinColumn;
		@ManyToOne
		@JoinColumn(nullable = false)
		String nullableJoinColumn;
		@Enumerated
		OurEnum ourEnumOrdinal;
		@Enumerated(EnumType.STRING)
		OurEnum ourEnumString;
		@Version
		int version;
		@Basic
		int basic;
		@Basic(optional = false)
		String basicNotOptional;
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
		@Id
		@GeneratedValue
		int id;
		@Column
		String stuff;
	}

	private enum OurEnum {
		ONE,
		TWO,
		// end
		;
	}

	private static class UpperCaseFieldDatabaseType extends H2DatabaseType {
		public UpperCaseFieldDatabaseType() throws SQLException {
			super();
		}

		@Override
		public boolean isEntityNamesMustBeUpCase() {
			return true;
		}
	}
}
