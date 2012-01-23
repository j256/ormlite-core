package com.j256.ormlite.field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.sql.SQLException;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.types.BaseDataType;
import com.j256.ormlite.support.DatabaseResults;

public class DataPersisterManagerTest extends BaseCoreTest {

	@Test(expected = IllegalArgumentException.class)
	public void testCustomTypeNoPersister() throws Exception {
		createDao(PersistedStored.class, true);
	}

	@Test
	public void testCustomType() throws Exception {
		DataPersisterManager.registerDataPersisters(new StoredClassPersister());
		try {
			Dao<PersistedStored, Object> dao = createDao(PersistedStored.class, true);
			PersistedStored wrapper = new PersistedStored();
			String stuff = "pfjwpfjww";
			wrapper.storedClass = new StoredClass(stuff);
			assertEquals(1, dao.create(wrapper));

			PersistedStored wrapperResult = dao.queryForId(wrapper.id);
			assertNotNull(wrapperResult.storedClass);
			assertEquals(stuff, wrapperResult.storedClass.stuff);
		} finally {
			DataPersisterManager.clear();
		}
	}

	@Test
	public void testCustomTypePersister() throws Exception {
		Dao<PersistedStoredPersister, Object> dao = createDao(PersistedStoredPersister.class, true);
		PersistedStoredPersister wrapper = new PersistedStoredPersister();
		String stuff = "pfjwpfjww";
		wrapper.storedClass = new StoredClass(stuff);
		assertEquals(1, dao.create(wrapper));

		PersistedStoredPersister wrapperResult = dao.queryForId(wrapper.id);
		assertNotNull(wrapperResult.storedClass);
		assertEquals(stuff, wrapperResult.storedClass.stuff);
	}

	@Test(expected = SQLException.class)
	public void testCustomTypeBadPersister() throws Exception {
		createDao(PersistedStoredBadPersister.class, true);
	}

	@Test
	public void testCustomEnumPersister() throws Exception {
		DataPersisterManager.registerDataPersisters(new EnumConstantPersister());
		try {
			Dao<PersistedDataType, Object> dao = createDao(PersistedDataType.class, true);
			PersistedDataType wrapper = new PersistedDataType();
			SomeEnum someEnum = SomeEnum.SECOND;
			wrapper.someEnum = someEnum;
			assertTrue(wrapper.someEnum != EnumConstantPersister.CONSTANT_VALUE);
			assertEquals(1, dao.create(wrapper));

			PersistedDataType wrapperResult = dao.queryForId(wrapper.id);
			assertTrue(someEnum != wrapperResult.someEnum);
			assertEquals(EnumConstantPersister.CONSTANT_VALUE, wrapperResult.someEnum);
		} finally {
			DataPersisterManager.clear();
		}
	}

	protected static class PersistedStored {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		StoredClass storedClass;
		PersistedStored() {
		}
	}

	protected static class PersistedStoredPersister {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(persisterClass = StoredClassPersister.class)
		StoredClass storedClass;
		PersistedStoredPersister() {
		}
	}

	protected static class PersistedStoredBadPersister {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(persisterClass = NoGetSingletonPersister.class)
		StoredClass storedClass;
		PersistedStoredBadPersister() {
		}
	}

	private static class StoredClass {
		String stuff;
		public StoredClass(String stuff) {
			this.stuff = stuff;
		}
	}

	private static class NoGetSingletonPersister extends BaseDataType {
		private NoGetSingletonPersister() {
			super(null, new Class[] { StoredClass.class });
		}
		@Override
		public Object parseDefaultString(FieldType fieldType, String defaultStr) {
			return null;
		}
		@Override
		public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos) {
			return null;
		}
		@Override
		public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) {
			return null;
		}
		@Override
		public Object javaToSqlArg(FieldType fieldType, Object obj) {
			return null;
		}
		@Override
		public boolean isValidForField(Field field) {
			return false;
		}
	}

	private static class StoredClassPersister extends BaseDataType {
		private static final StoredClassPersister singleton = new StoredClassPersister();
		@SuppressWarnings("unused")
		public static StoredClassPersister getSingleton() {
			return singleton;
		}
		public StoredClassPersister() {
			super(SqlType.STRING, new Class[] { StoredClass.class });
		}
		@Override
		public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
			throw new SQLException("Default string doesn't work");
		}
		@Override
		public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
			return results.getString(columnPos);
		}
		@Override
		public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) {
			String value = (String) sqlArg;
			return new StoredClass(value);
		}
		@Override
		public Object javaToSqlArg(FieldType fieldType, Object javaObject) {
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
	}

	protected static class PersistedDataType {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		SomeEnum someEnum;
		PersistedDataType() {
		}
	}

	private enum SomeEnum {
		FIRST,
		SECOND,
		// end
		;
	}

	private static class EnumConstantPersister extends BaseDataType {
		public static final SomeEnum CONSTANT_VALUE = SomeEnum.FIRST;
		public EnumConstantPersister() {
			super(SqlType.STRING, new Class[] {});
		}
		@Override
		public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
			throw new SQLException("Default string doesn't work");
		}
		@Override
		public Object resultToSqlArg(FieldType fieldType, DatabaseResults results, int columnPos) {
			return CONSTANT_VALUE;
		}
		@Override
		public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) {
			// constant for testing
			return CONSTANT_VALUE;
		}
		@Override
		public Object javaToSqlArg(FieldType fieldType, Object javaObject) {
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
	}
}
