package com.j256.ormlite.field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Field;
import java.sql.SQLException;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.types.BaseDataType;
import com.j256.ormlite.support.DatabaseResults;

public class DataPersisterManagerTest extends BaseCoreTest {

	private static final SqlType FIXED_ENUM_VALUE = SqlType.BLOB;

	@Test(expected = IllegalArgumentException.class)
	public void testCustomTypeNoPersister() throws Exception {
		createDao(PersistedStoredWrapper.class, true);
	}

	@Test
	public void testCustomType() throws Exception {
		DataPersisterManager.registerDataPersisters(new StoredClassPersister());
		try {
			Dao<PersistedStoredWrapper, Object> dao = createDao(PersistedStoredWrapper.class, true);
			PersistedStoredWrapper wrapper = new PersistedStoredWrapper();
			String stuff = "pfjwpfjww";
			wrapper.storedClass = new StoredClass(stuff);
			assertEquals(1, dao.create(wrapper));

			PersistedStoredWrapper wrapperResult = dao.queryForId(wrapper.id);
			assertNotNull(wrapperResult.storedClass);
			assertEquals(stuff, wrapperResult.storedClass.stuff);
		} finally {
			DataPersisterManager.clear();
		}
	}

	@Test
	public void testCustomEnumPersister() throws Exception {
		DataPersisterManager.registerDataPersisters(new EnumConstantPersister());
		try {
			Dao<PersistedDataType, Object> dao = createDao(PersistedDataType.class, true);
			PersistedDataType wrapper = new PersistedDataType();
			SqlType sqlType = SqlType.UNKNOWN;
			wrapper.sqlType = sqlType;
			assertEquals(1, dao.create(wrapper));

			PersistedDataType wrapperResult = dao.queryForId(wrapper.id);
			assertFalse(wrapperResult.sqlType == sqlType);
			assertEquals(FIXED_ENUM_VALUE, wrapperResult.sqlType);
		} finally {
			DataPersisterManager.clear();
		}
	}

	protected static class PersistedStoredWrapper {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		StoredClass storedClass;
		PersistedStoredWrapper() {
		}
	}

	private static class StoredClass {
		String stuff;
		public StoredClass(String stuff) {
			this.stuff = stuff;
		}
	}

	private class StoredClassPersister extends BaseDataType {

		public StoredClassPersister() {
			super(null, new Class[] { StoredClass.class });
		}

		@Override
		public Object resultToJava(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
			String value = results.getString(columnPos);
			return new StoredClass(value);
		}

		@Override
		public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
			throw new SQLException("Default string doesn't work");
		}

		@Override
		public Object javaToSqlArg(FieldType fieldType, Object javaObject) throws SQLException {
			if (javaObject == null) {
				return null;
			} else {
				return ((StoredClass) javaObject).stuff;
			}
		}

		@Override
		public boolean isValidForField(Field field) {
			return field.getType() == StoredClass.class;
		}

		@Override
		public SqlType getSqlType() {
			return SqlType.STRING;
		}
	}

	protected static class PersistedDataType {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		SqlType sqlType;
		PersistedDataType() {
		}
	}

	private static class EnumConstantPersister extends BaseDataType {
		public EnumConstantPersister() {
			super(null, new Class[] {});
		}

		@Override
		public Object resultToJava(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
			return SqlType.BLOB;
		}

		@Override
		public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
			throw new SQLException("Default string doesn't work");
		}

		@Override
		public Object javaToSqlArg(FieldType fieldType, Object javaObject) throws SQLException {
			if (javaObject == null) {
				return null;
			} else {
				return javaObject.toString();
			}
		}

		@Override
		public boolean isValidForField(Field field) {
			// this matches all enums
			return field.getType().isEnum();
		}

		@Override
		public SqlType getSqlType() {
			return SqlType.STRING;
		}
	}
}
