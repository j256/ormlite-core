package com.j256.ormlite.field;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.isNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.dao.ObjectCache;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.h2.H2DatabaseType;
import com.j256.ormlite.stmt.GenericRowMapper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;
import com.j256.ormlite.table.DatabaseTableConfig;

public class FieldTypeTest extends BaseCoreTest {

	private static final String RANK_DB_COLUMN_NAME = "rank_column";
	private static final int RANK_WIDTH = 100;
	private static final String SERIAL_DEFAULT_VALUE = "7";
	private static final String SEQ_NAME = "sequence";

	@Test
	public void testFieldType() throws Exception {

		Field[] fields = LocalFoo.class.getDeclaredFields();
		assertTrue(fields.length >= 4);
		Field nameField = fields[0];
		Field rankField = fields[1];
		Field serialField = fields[2];
		Field intLongField = fields[3];

		FieldType fieldType =
				FieldType.createFieldType(databaseType, LocalFoo.class.getSimpleName(), nameField, LocalFoo.class);
		assertEquals(nameField.getName(), fieldType.getFieldName());
		assertEquals(nameField.getName(), fieldType.getColumnName());
		assertEquals(DataType.STRING.getDataPersister(), fieldType.getDataPersister());
		assertEquals(0, fieldType.getWidth());
		assertTrue(fieldType.toString().contains("Foo"));
		assertTrue(fieldType.toString().contains(nameField.getName()));

		fieldType = FieldType.createFieldType(databaseType, LocalFoo.class.getSimpleName(), rankField, LocalFoo.class);
		assertEquals(RANK_DB_COLUMN_NAME, fieldType.getColumnName());
		assertEquals(DataType.STRING.getDataPersister(), fieldType.getDataPersister());
		assertEquals(RANK_WIDTH, fieldType.getWidth());

		fieldType =
				FieldType.createFieldType(databaseType, LocalFoo.class.getSimpleName(), serialField, LocalFoo.class);
		assertEquals(serialField.getName(), fieldType.getColumnName());
		assertEquals(DataType.INTEGER_OBJ.getDataPersister(), fieldType.getDataPersister());
		assertEquals(Integer.parseInt(SERIAL_DEFAULT_VALUE), fieldType.getDefaultValue());

		String tableName = LocalFoo.class.getSimpleName();
		fieldType = FieldType.createFieldType(databaseType, tableName, intLongField, LocalFoo.class);
		assertEquals(intLongField.getName(), fieldType.getColumnName());
		assertFalse(fieldType.isGeneratedId());
		assertEquals(DataType.LONG.getDataPersister(), fieldType.getDataPersister());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUnknownFieldType() throws Exception {
		Field[] fields = UnknownFieldType.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		FieldType.createFieldType(databaseType, UnknownFieldType.class.getSimpleName(), fields[0],
				UnknownFieldType.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIdAndGeneratedId() throws Exception {
		Field[] fields = IdAndGeneratedId.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		FieldType.createFieldType(databaseType, IdAndGeneratedId.class.getSimpleName(), fields[0],
				IdAndGeneratedId.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGeneratedIdAndSequence() throws Exception {
		Field[] fields = GeneratedIdAndSequence.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		FieldType.createFieldType(databaseType, GeneratedIdAndSequence.class.getSimpleName(), fields[0],
				GeneratedIdAndSequence.class);
	}

	@Test
	public void testGeneratedIdAndSequenceWorks() throws Exception {
		Field[] fields = GeneratedIdSequence.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		connectionSource.setDatabaseType(new NeedsSequenceDatabaseType());
		FieldType fieldType = FieldType.createFieldType(databaseType, GeneratedIdSequence.class.getSimpleName(),
				fields[0], GeneratedIdSequence.class);
		assertTrue(fieldType.isGeneratedIdSequence());
		assertEquals(SEQ_NAME, fieldType.getGeneratedIdSequence());
	}

	@Test
	public void testGeneratedIdAndSequenceUppercase() throws Exception {
		Field[] fields = GeneratedIdSequence.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		DatabaseType databaseType = new NeedsUppercaseSequenceDatabaseType();
		FieldType fieldType = FieldType.createFieldType(databaseType, GeneratedIdSequence.class.getSimpleName(),
				fields[0], GeneratedIdSequence.class);
		assertTrue(fieldType.isGeneratedIdSequence());
		assertEquals(SEQ_NAME.toUpperCase(), fieldType.getGeneratedIdSequence());
	}

	@Test
	public void testGeneratedIdGetsASequence() throws Exception {
		Field[] fields = GeneratedId.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		DatabaseType databaseType = new NeedsSequenceDatabaseType();
		FieldType fieldType = FieldType.createFieldType(databaseType, GeneratedId.class.getSimpleName(), fields[0],
				GeneratedId.class);
		assertTrue(fieldType.isGeneratedIdSequence());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGeneratedIdCantBeGenerated() throws Exception {
		Field[] fields = GeneratedIdCantBeGenerated.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		FieldType.createFieldType(databaseType, GeneratedIdCantBeGenerated.class.getSimpleName(), fields[0],
				GeneratedIdCantBeGenerated.class);
	}

	@Test
	public void testFieldTypeConverter() throws Exception {
		Field[] fields = LocalFoo.class.getDeclaredFields();
		assertTrue(fields.length >= 4);
		Field nameField = fields[0];
		DatabaseType databaseType = createMock(DatabaseType.class);
		final SqlType sqlType = SqlType.DATE;
		final String nameArg = "zippy buzz";
		final String nameResult = "blabber bling";
		final AtomicBoolean resultToSqlArgCalled = new AtomicBoolean(false);
		DataPersister stringPersister = DataType.STRING.getDataPersister();
		expect(databaseType.getDataPersister(isA(DataPersister.class), isA(FieldType.class)))
				.andReturn(stringPersister);
		expect(databaseType.getFieldConverter(isA(DataPersister.class), isA(FieldType.class)))
				.andReturn(new BaseFieldConverter() {
					@Override
					public SqlType getSqlType() {
						return sqlType;
					}

					@Override
					public Object parseDefaultString(FieldType fieldType, String defaultStr) {
						return defaultStr;
					}

					@Override
					public Object resultToSqlArg(FieldType fieldType, DatabaseResults resultSet, int columnPos) {
						resultToSqlArgCalled.set(true);
						return nameResult;
					}

					@Override
					public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) {
						return nameResult;
					}

					@Override
					public Object javaToSqlArg(FieldType fieldType, Object javaObject) {
						return nameArg;
					}

					@Override
					public Object resultStringToJava(FieldType fieldType, String stringValue, int columnPos) {
						return stringValue;
					}
				});
		expect(databaseType.isEntityNamesMustBeUpCase()).andReturn(false);
		replay(databaseType);
		connectionSource.setDatabaseType(databaseType);
		FieldType fieldType =
				FieldType.createFieldType(databaseType, LocalFoo.class.getSimpleName(), nameField, LocalFoo.class);
		verify(databaseType);

		assertEquals(sqlType, fieldType.getSqlType());
		LocalFoo foo = new LocalFoo();
		// it can't be null
		foo.name = nameArg + " not that";
		assertEquals(nameArg, fieldType.extractJavaFieldToSqlArgValue(foo));

		DatabaseResults resultMock = createMock(DatabaseResults.class);
		expect(resultMock.findColumn("name")).andReturn(0);
		expect(resultMock.wasNull(0)).andReturn(false);
		replay(resultMock);
		assertEquals(nameResult, fieldType.resultToJava(resultMock, new HashMap<String, Integer>()));
		verify(resultMock);
		assertTrue(resultToSqlArgCalled.get());
	}

	@Test
	public void testFieldForeign() throws Exception {

		Field[] fields = ForeignParent.class.getDeclaredFields();
		assertTrue(fields.length >= 3);
		@SuppressWarnings("unused")
		Field idField = fields[0];
		Field nameField = fields[1];
		Field bazField = fields[2];

		FieldType fieldType = FieldType.createFieldType(databaseType, ForeignParent.class.getSimpleName(), nameField,
				ForeignParent.class);
		assertEquals(nameField.getName(), fieldType.getColumnName());
		assertEquals(DataType.STRING.getDataPersister(), fieldType.getDataPersister());
		assertFalse(fieldType.isForeign());
		assertEquals(0, fieldType.getWidth());

		fieldType = FieldType.createFieldType(databaseType, ForeignParent.class.getSimpleName(), bazField,
				ForeignParent.class);
		fieldType.configDaoInformation(connectionSource, ForeignParent.class);
		assertEquals(bazField.getName() + FieldType.FOREIGN_ID_FIELD_SUFFIX, fieldType.getColumnName());
		// this is the type of the foreign object's id
		assertEquals(DataType.INTEGER.getDataPersister(), fieldType.getDataPersister());
		assertTrue(fieldType.isForeign());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPrimitiveForeign() throws Exception {
		Field[] fields = ForeignPrimitive.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		Field idField = fields[0];
		FieldType.createFieldType(databaseType, ForeignPrimitive.class.getSimpleName(), idField,
				ForeignPrimitive.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testForeignNoId() throws Exception {
		Field[] fields = ForeignNoId.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		Field fooField = fields[0];
		FieldType fieldType =
				FieldType.createFieldType(databaseType, ForeignNoId.class.getSimpleName(), fooField, ForeignNoId.class);
		fieldType.configDaoInformation(connectionSource, ForeignNoId.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testForeignAlsoId() throws Exception {
		Field[] fields = ForeignAlsoId.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		Field fooField = fields[0];
		FieldType.createFieldType(databaseType, ForeignAlsoId.class.getSimpleName(), fooField, ForeignAlsoId.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testObjectFieldNotForeign() throws Exception {
		Field[] fields = ObjectFieldNotForeign.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		Field fooField = fields[0];
		FieldType.createFieldType(databaseType, ObjectFieldNotForeign.class.getSimpleName(), fooField,
				ObjectFieldNotForeign.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetSetNoGet() throws Exception {
		Field[] fields = GetSetNoGet.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		Field idField = fields[0];
		FieldType.createFieldType(databaseType, GetSetNoGet.class.getSimpleName(), idField, GetSetNoGet.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetSetGetWrongType() throws Exception {
		Field[] fields = GetSetGetWrongType.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		Field idField = fields[0];
		FieldType.createFieldType(databaseType, GetSetGetWrongType.class.getSimpleName(), idField,
				GetSetGetWrongType.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetSetNoSet() throws Exception {
		Field[] fields = GetSetNoSet.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		Field idField = fields[0];
		FieldType.createFieldType(databaseType, GetSetNoSet.class.getSimpleName(), idField, GetSetNoSet.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetSetSetWrongType() throws Exception {
		Field[] fields = GetSetSetWrongType.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		Field idField = fields[0];
		FieldType.createFieldType(databaseType, GetSetSetWrongType.class.getSimpleName(), idField,
				GetSetSetWrongType.class);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetSetSetReturnNotVoid() throws Exception {
		Field[] fields = GetSetReturnNotVoid.class.getDeclaredFields();
		assertNotNull(fields);
		assertTrue(fields.length >= 1);
		Field idField = fields[0];
		FieldType.createFieldType(databaseType, GetSetReturnNotVoid.class.getSimpleName(), idField,
				GetSetReturnNotVoid.class);
	}

	@Test
	public void testGetSet() throws Exception {
		Field[] fields = GetSet.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		Field idField = fields[0];
		FieldType.createFieldType(databaseType, GetSet.class.getSimpleName(), idField, GetSet.class);
	}

	@Test
	public void testGetAndSetValue() throws Exception {
		Field[] fields = GetSet.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		Field idField = fields[0];
		FieldType fieldType =
				FieldType.createFieldType(databaseType, GetSet.class.getSimpleName(), idField, GetSet.class);
		GetSet getSet = new GetSet();
		int id = 121312321;
		getSet.id = id;
		assertEquals(id, fieldType.extractJavaFieldToSqlArgValue(getSet));
		int id2 = 869544;
		fieldType.assignField(connectionSource, getSet, id2, false, null);
		assertEquals(id2, fieldType.extractJavaFieldToSqlArgValue(getSet));
	}

	@Test(expected = SQLException.class)
	public void testFieldSetNull() throws Exception {
		Field field = LocalFoo.class.getDeclaredField("intLong");
		FieldType fieldType =
				FieldType.createFieldType(databaseType, LocalFoo.class.getSimpleName(), field, LocalFoo.class);
		LocalFoo foo = new LocalFoo();
		long value1 = 13413123123L;
		foo.intLong = value1;
		assertEquals(foo.intLong, fieldType.extractJavaFieldToSqlArgValue(foo));
		long value2 = 5223423434L;
		fieldType.assignField(connectionSource, foo, value2, false, null);
		assertEquals(value2, foo.intLong);
		// this should throw a illegal argument exception _not_ a NPE, thanks @hrach
		fieldType.assignField(connectionSource, foo, null, false, null);
	}

	@Test(expected = SQLException.class)
	public void testGetWrongObject() throws Exception {
		Field[] fields = GetSet.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		Field idField = fields[0];
		FieldType fieldType =
				FieldType.createFieldType(databaseType, GetSet.class.getSimpleName(), idField, GetSet.class);
		fieldType.extractJavaFieldToSqlArgValue(new Object());
	}

	@Test(expected = SQLException.class)
	public void testSetWrongObject() throws Exception {
		Field[] fields = GetSet.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		Field idField = fields[0];
		FieldType fieldType =
				FieldType.createFieldType(databaseType, GetSet.class.getSimpleName(), idField, GetSet.class);
		fieldType.assignField(connectionSource, new Object(), 10, false, null);
	}

	@Test
	public void testCreateFieldTypeNull() throws Exception {
		Field[] fields = NoAnnotation.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		Field idField = fields[0];
		assertNull(FieldType.createFieldType(databaseType, NoAnnotation.class.getSimpleName(), idField,
				NoAnnotation.class));
	}

	@Test
	public void testSetValueField() throws Exception {
		Field[] fields = LocalFoo.class.getDeclaredFields();
		assertTrue(fields.length >= 4);
		Field nameField = fields[0];
		FieldType fieldType =
				FieldType.createFieldType(databaseType, LocalFoo.class.getSimpleName(), nameField, LocalFoo.class);
		LocalFoo foo = new LocalFoo();
		String name1 = "wfwef";
		fieldType.assignField(connectionSource, foo, name1, false, null);
		assertEquals(name1, foo.name);
	}

	@Test
	public void testSetIdField() throws Exception {
		Field[] fields = NumberId.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		Field nameField = fields[0];
		FieldType fieldType =
				FieldType.createFieldType(databaseType, NumberId.class.getSimpleName(), nameField, NumberId.class);
		NumberId foo = new NumberId();
		int id = 10;
		fieldType.assignIdValue(connectionSource, foo, id, null);
		assertEquals(id, foo.id);
	}

	@Test(expected = SQLException.class)
	public void testSetIdFieldString() throws Exception {
		Field[] fields = LocalFoo.class.getDeclaredFields();
		assertTrue(fields.length >= 4);
		Field nameField = fields[0];
		FieldType fieldType =
				FieldType.createFieldType(databaseType, LocalFoo.class.getSimpleName(), nameField, LocalFoo.class);
		fieldType.assignIdValue(connectionSource, new LocalFoo(), 10, null);
	}

	@Test
	public void testCanBeNull() throws Exception {
		Field[] fields = CanBeNull.class.getDeclaredFields();
		Arrays.sort(fields, new Comparator<Field>() {
			public int compare(Field a, Field b) {
				return b.getName().compareTo(a.getName());
			}
		});
		assertTrue(fields.length >= 2);
		Field field = fields[1];
		FieldType fieldType =
				FieldType.createFieldType(databaseType, CanBeNull.class.getSimpleName(), field, CanBeNull.class);
		assertTrue(fieldType.isCanBeNull());
		field = fields[0];
		fieldType = FieldType.createFieldType(databaseType, CanBeNull.class.getSimpleName(), field, CanBeNull.class);
		assertFalse(fieldType.isCanBeNull());
	}

	@Test
	public void testAssignForeign() throws Exception {
		Field[] fields = ForeignParent.class.getDeclaredFields();
		assertTrue(fields.length >= 3);
		Field field = fields[2];
		FieldType fieldType = FieldType.createFieldType(databaseType, ForeignParent.class.getSimpleName(), field,
				ForeignParent.class);
		fieldType.configDaoInformation(connectionSource, ForeignParent.class);
		assertTrue(fieldType.isForeign());
		int id = 10;
		ForeignParent parent = new ForeignParent();
		assertNull(parent.foreign);
		// we assign the id, not the object
		fieldType.assignField(connectionSource, parent, id, false, null);
		ForeignForeign foreign = parent.foreign;
		assertNotNull(foreign);
		assertEquals(id, foreign.id);

		// not try assigning it again
		fieldType.assignField(connectionSource, parent, id, false, null);
		// foreign field should not have been changed
		assertSame(foreign, parent.foreign);

		// now assign a different id
		int newId = id + 1;
		fieldType.assignField(connectionSource, parent, newId, false, null);
		assertNotSame(foreign, parent.foreign);
		assertEquals(newId, parent.foreign.id);
	}

	@Test(expected = SQLException.class)
	public void testGeneratedIdDefaultValue() throws Exception {
		Class<GeneratedIdDefault> clazz = GeneratedIdDefault.class;
		Field[] fields = clazz.getDeclaredFields();
		assertTrue(fields.length >= 1);
		Field idField = fields[0];
		FieldType.createFieldType(databaseType, clazz.getSimpleName(), idField, clazz);
	}

	@Test(expected = SQLException.class)
	public void testThrowIfNullNotPrimitive() throws Exception {
		Class<ThrowIfNullNonPrimitive> clazz = ThrowIfNullNonPrimitive.class;
		Field[] fields = clazz.getDeclaredFields();
		assertTrue(fields.length >= 1);
		Field field = fields[0];
		FieldType.createFieldType(databaseType, clazz.getSimpleName(), field, clazz);
	}

	@Test(expected = SQLException.class)
	public void testBadDateDefaultValue() throws Exception {
		Class<DateDefaultBad> clazz = DateDefaultBad.class;
		Field[] fields = clazz.getDeclaredFields();
		assertTrue(fields.length >= 1);
		Field field = fields[0];
		FieldType.createFieldType(databaseType, clazz.getSimpleName(), field, clazz);
	}

	@Test(expected = SQLException.class)
	public void testNullPrimitiveThrow() throws Exception {
		Field field = ThrowIfNullNonPrimitive.class.getDeclaredField("primitive");
		FieldType fieldType = FieldType.createFieldType(databaseType, ThrowIfNullNonPrimitive.class.getSimpleName(),
				field, ThrowIfNullNonPrimitive.class);
		DatabaseResults results = createMock(DatabaseResults.class);
		int fieldNum = 1;
		expect(results.findColumn(field.getName())).andReturn(fieldNum);
		expect(results.getInt(fieldNum)).andReturn(0);
		expect(results.wasNull(fieldNum)).andReturn(true);
		replay(results);
		fieldType.resultToJava(results, new HashMap<String, Integer>());
		verify(results);
	}

	@Test
	public void testSerializableNull() throws Exception {
		Field[] fields = SerializableField.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		Field field = fields[0];
		FieldType fieldType = FieldType.createFieldType(databaseType, SerializableField.class.getSimpleName(), field,
				SerializableField.class);
		DatabaseResults results = createMock(DatabaseResults.class);
		int fieldNum = 1;
		expect(results.findColumn(field.getName())).andReturn(fieldNum);
		expect(results.getTimestamp(fieldNum)).andReturn(null);
		expect(results.wasNull(fieldNum)).andReturn(true);
		replay(results);
		assertNull(fieldType.resultToJava(results, new HashMap<String, Integer>()));
		verify(results);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidFieldType() throws Exception {
		Field[] fields = InvalidType.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		Field field = fields[0];
		FieldType fieldType =
				FieldType.createFieldType(databaseType, InvalidType.class.getSimpleName(), field, InvalidType.class);
		DatabaseResults results = createMock(DatabaseResults.class);
		int fieldNum = 1;
		expect(results.findColumn(field.getName())).andReturn(fieldNum);
		expect(results.wasNull(fieldNum)).andReturn(true);
		replay(results);
		assertNull(fieldType.resultToJava(results, new HashMap<String, Integer>()));
		verify(results);
	}

	@Test
	public void testEscapeDefault() throws Exception {
		Field field = LocalFoo.class.getDeclaredField("name");
		FieldType fieldType =
				FieldType.createFieldType(databaseType, LocalFoo.class.getSimpleName(), field, LocalFoo.class);
		assertTrue(fieldType.isEscapedValue());
		assertTrue(fieldType.isEscapedDefaultValue());

		field = LocalFoo.class.getDeclaredField("intLong");
		fieldType = FieldType.createFieldType(databaseType, LocalFoo.class.getSimpleName(), field, LocalFoo.class);
		assertFalse(fieldType.isEscapedValue());
		assertFalse(fieldType.isEscapedDefaultValue());
	}

	@Test
	public void testForeignIsSerializable() throws Exception {
		Class<ForeignAlsoSerializable> clazz = ForeignAlsoSerializable.class;
		Field field = clazz.getDeclaredField("foo");
		FieldType fieldType = FieldType.createFieldType(databaseType, clazz.getSimpleName(), field, clazz);
		fieldType.configDaoInformation(connectionSource, clazz);
		assertTrue(fieldType.isForeign());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidEnumField() throws Exception {
		Class<InvalidEnumType> clazz = InvalidEnumType.class;
		Field field = clazz.getDeclaredField("stuff");
		FieldType.createFieldType(databaseType, clazz.getSimpleName(), field, clazz);
	}

	@Test
	public void testRecursiveForeign() throws Exception {
		Class<Recursive> clazz = Recursive.class;
		Field field = clazz.getDeclaredField("foreign");
		// this will throw without the recursive fix
		FieldType.createFieldType(databaseType, clazz.getSimpleName(), field, clazz);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testForeignAutoRefresh() throws Exception {
		Field field = ForeignAutoRefresh.class.getDeclaredField("foreign");
		ConnectionSource connectionSource = createMock(ConnectionSource.class);
		DatabaseConnection connection = createMock(DatabaseConnection.class);
		expect(connectionSource.getDatabaseType()).andReturn(databaseType).anyTimes();
		expect(connectionSource.getReadOnlyConnection("ForeignAutoRefresh")).andReturn(connection);
		ForeignForeign foreignForeign = new ForeignForeign();
		String stuff = "21312j3213";
		int id = 4123123;
		foreignForeign.id = id;
		foreignForeign.stuff = stuff;
		expect(connection.queryForOne(isA(String.class), isA(Object[].class), isA(FieldType[].class),
				isA(GenericRowMapper.class), (ObjectCache) isNull())).andReturn(foreignForeign);
		connectionSource.releaseConnection(connection);
		DatabaseResults results = createMock(DatabaseResults.class);
		ForeignAutoRefresh foreign = new ForeignAutoRefresh();
		replay(results, connectionSource, connection);
		FieldType fieldType = FieldType.createFieldType(databaseType, ForeignAutoRefresh.class.getSimpleName(), field,
				ForeignAutoRefresh.class);
		fieldType.configDaoInformation(connectionSource, ForeignAutoRefresh.class);
		assertNull(foreign.foreign);
		fieldType.assignField(connectionSource, foreign, id, false, null);
		assertNotNull(foreign.foreign);
		assertEquals(id, foreign.foreign.id);
		assertEquals(stuff, foreign.foreign.stuff);
		verify(results, connectionSource, connection);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testForeignAutoRefreshOnNormalField() throws Exception {
		Class<ForeignAutoRefreshWrong> clazz = ForeignAutoRefreshWrong.class;
		Field[] fields = clazz.getDeclaredFields();
		FieldType.createFieldType(databaseType, clazz.getSimpleName(), fields[0], clazz);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testForeignColumnOnNormalField() throws Exception {
		Class<ForeignColumnNameWrong> clazz = ForeignColumnNameWrong.class;
		Field[] fields = clazz.getDeclaredFields();
		FieldType.createFieldType(databaseType, clazz.getSimpleName(), fields[0], clazz);
	}

	@Test(expected = SQLException.class)
	public void testSerializableNoDataType() throws Exception {
		Class<SerializableNoDataType> clazz = SerializableNoDataType.class;
		Field field = clazz.getDeclaredField("serial");
		// this will throw without the recursive fix
		FieldType.createFieldType(databaseType, clazz.getSimpleName(), field, clazz);
	}

	@Test(expected = SQLException.class)
	public void testByteArrayNoDataType() throws Exception {
		Class<ByteArrayNoDataType> clazz = ByteArrayNoDataType.class;
		Field field = clazz.getDeclaredField("bytes");
		// this will throw without the recursive fix
		FieldType.createFieldType(databaseType, clazz.getSimpleName(), field, clazz);
	}

	@Test(expected = SQLException.class)
	public void testForeignCollectionNoGeneric() throws Exception {
		Class<ForeignCollectionNoGeneric> clazz = ForeignCollectionNoGeneric.class;
		Field field = clazz.getDeclaredField("foreignStuff");
		FieldType.createFieldType(databaseType, clazz.getSimpleName(), field, clazz);
	}

	@Test(expected = SQLException.class)
	public void testImproperId() throws Exception {
		Class<ImproperIdType> clazz = ImproperIdType.class;
		Field field = clazz.getDeclaredField("id");
		FieldType.createFieldType(databaseType, clazz.getSimpleName(), field, clazz);
	}

	@Test
	public void testDefaultValues() throws Exception {
		DefaultTypes defaultTypes = new DefaultTypes();
		Field field = DefaultTypes.class.getDeclaredField("booleanField");
		FieldType fieldType =
				FieldType.createFieldType(databaseType, DefaultTypes.class.getSimpleName(), field, DefaultTypes.class);
		assertNull(fieldType.getFieldValueIfNotDefault(defaultTypes));
		defaultTypes.booleanField = true;
		assertEquals(defaultTypes.booleanField, fieldType.getFieldValueIfNotDefault(defaultTypes));

		field = DefaultTypes.class.getDeclaredField("byteField");
		fieldType =
				FieldType.createFieldType(databaseType, DefaultTypes.class.getSimpleName(), field, DefaultTypes.class);
		assertNull(fieldType.getFieldValueIfNotDefault(defaultTypes));
		defaultTypes.byteField = 1;
		assertEquals(defaultTypes.byteField, fieldType.getFieldValueIfNotDefault(defaultTypes));

		field = DefaultTypes.class.getDeclaredField("charField");
		fieldType =
				FieldType.createFieldType(databaseType, DefaultTypes.class.getSimpleName(), field, DefaultTypes.class);
		assertNull(fieldType.getFieldValueIfNotDefault(defaultTypes));
		defaultTypes.charField = '1';
		assertEquals(defaultTypes.charField, fieldType.getFieldValueIfNotDefault(defaultTypes));

		field = DefaultTypes.class.getDeclaredField("shortField");
		fieldType =
				FieldType.createFieldType(databaseType, DefaultTypes.class.getSimpleName(), field, DefaultTypes.class);
		assertNull(fieldType.getFieldValueIfNotDefault(defaultTypes));
		defaultTypes.shortField = 32000;
		assertEquals(defaultTypes.shortField, fieldType.getFieldValueIfNotDefault(defaultTypes));

		field = DefaultTypes.class.getDeclaredField("intField");
		fieldType =
				FieldType.createFieldType(databaseType, DefaultTypes.class.getSimpleName(), field, DefaultTypes.class);
		assertNull(fieldType.getFieldValueIfNotDefault(defaultTypes));
		defaultTypes.intField = 1000000000;
		assertEquals(defaultTypes.intField, fieldType.getFieldValueIfNotDefault(defaultTypes));

		field = DefaultTypes.class.getDeclaredField("longField");
		fieldType =
				FieldType.createFieldType(databaseType, DefaultTypes.class.getSimpleName(), field, DefaultTypes.class);
		assertNull(fieldType.getFieldValueIfNotDefault(defaultTypes));
		defaultTypes.longField = 1000000000000000L;
		assertEquals(defaultTypes.longField, fieldType.getFieldValueIfNotDefault(defaultTypes));

		field = DefaultTypes.class.getDeclaredField("floatField");
		fieldType =
				FieldType.createFieldType(databaseType, DefaultTypes.class.getSimpleName(), field, DefaultTypes.class);
		assertNull(fieldType.getFieldValueIfNotDefault(defaultTypes));
		defaultTypes.floatField = 10.123213F;
		assertEquals(defaultTypes.floatField, fieldType.getFieldValueIfNotDefault(defaultTypes));

		field = DefaultTypes.class.getDeclaredField("doubleField");
		fieldType =
				FieldType.createFieldType(databaseType, DefaultTypes.class.getSimpleName(), field, DefaultTypes.class);
		assertNull(fieldType.getFieldValueIfNotDefault(defaultTypes));
		defaultTypes.doubleField = 102123123123.123213;
		assertEquals(defaultTypes.doubleField, fieldType.getFieldValueIfNotDefault(defaultTypes));
	}

	@Test
	public void testEquals() throws Exception {
		Field field1 = DefaultTypes.class.getDeclaredField("booleanField");
		FieldType fieldType1 =
				FieldType.createFieldType(databaseType, DefaultTypes.class.getSimpleName(), field1, DefaultTypes.class);
		FieldType fieldType2 =
				FieldType.createFieldType(databaseType, DefaultTypes.class.getSimpleName(), field1, DefaultTypes.class);

		Field field2 = DefaultTypes.class.getDeclaredField("byteField");
		FieldType fieldType3 =
				FieldType.createFieldType(databaseType, DefaultTypes.class.getSimpleName(), field2, DefaultTypes.class);
		FieldType fieldType4 =
				FieldType.createFieldType(databaseType, DefaultTypes.class.getSimpleName(), field2, DefaultTypes.class);

		assertTrue(fieldType1.equals(fieldType1));
		assertTrue(fieldType2.equals(fieldType2));
		assertTrue(fieldType1.equals(fieldType2));
		assertTrue(fieldType2.equals(fieldType1));
		assertEquals(fieldType1.hashCode(), fieldType2.hashCode());

		assertFalse(fieldType1.equals(null));
		assertFalse(fieldType1.equals(fieldType3));
		assertFalse(fieldType1.equals(fieldType4));
		assertFalse(fieldType3.equals(fieldType1));
		assertFalse(fieldType4.equals(fieldType1));

		assertTrue(fieldType3.equals(fieldType3));
		assertTrue(fieldType4.equals(fieldType4));
		assertTrue(fieldType3.equals(fieldType4));
		assertTrue(fieldType4.equals(fieldType3));
		assertEquals(fieldType3.hashCode(), fieldType4.hashCode());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAllowGeneratedIdInsertPrimitive() throws Exception {
		Class<AllowGeneratedIdNotGeneratedId> clazz = AllowGeneratedIdNotGeneratedId.class;
		Field field = clazz.getDeclaredField("stuff");
		FieldType.createFieldType(databaseType, clazz.getSimpleName(), field, clazz);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testVersionFieldWrongType() throws Exception {
		Class<VersionFieldWrongType> clazz = VersionFieldWrongType.class;
		Field field = clazz.getDeclaredField("version");
		FieldType.createFieldType(databaseType, clazz.getSimpleName(), field, clazz);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testForeignAutoCreateNotForeign() throws Exception {
		createDao(ForeignAutoCreateNoForeign.class, true);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testForeignAutoCreateNotGeneratedId() throws Exception {
		createDao(ForeignAutoCreateNoGeneratedId.class, true);
	}

	@Test(expected = SQLException.class)
	public void testForeignCollectionForeign() throws Exception {
		createDao(ForeignCollectionForeign.class, true);
	}

	@Test
	public void testDefaultValueFieldTypeEmptyType() throws Exception {
		Class<DefaultEmptyString> clazz = DefaultEmptyString.class;
		Field field = clazz.getDeclaredField("defaultBlank");
		FieldType fieldType = FieldType.createFieldType(databaseType, clazz.getSimpleName(), field, clazz);
		assertEquals("", fieldType.getDefaultValue());
	}

	@Test
	public void testDefaultValueEmptyStringPersist() throws Exception {
		Dao<DefaultEmptyString, Integer> dao = createDao(DefaultEmptyString.class, true);

		DefaultEmptyString foo = new DefaultEmptyString();
		assertEquals(1, dao.create(foo));

		DefaultEmptyString result = dao.queryForId(foo.id);
		assertNotNull(result);
		assertEquals("", result.defaultBlank);
	}

	@Test
	public void testForeignInCache() throws Exception {
		Dao<ForeignParent, Integer> parentDao = createDao(ForeignParent.class, true);
		Dao<ForeignForeign, Integer> foreignDao = createDao(ForeignForeign.class, true);
		foreignDao.setObjectCache(true);

		ForeignForeign foreign = new ForeignForeign();
		foreign.stuff = "hello";
		foreignDao.create(foreign);

		assertSame(foreign, foreignDao.queryForId(foreign.id));

		ForeignParent parent = new ForeignParent();
		parent.foreign = foreign;
		parentDao.create(parent);

		ForeignParent result = parentDao.queryForId(parent.id);
		assertNotSame(parent, result);
		assertSame(foreign, result.foreign);
	}

	@Test
	public void testForeignConfigured() throws Exception {

		ArrayList<DatabaseFieldConfig> foreignFieldConfigs = new ArrayList<DatabaseFieldConfig>();
		DatabaseFieldConfig fieldConfig = new DatabaseFieldConfig();
		fieldConfig.setFieldName("id");
		fieldConfig.setGeneratedId(true);
		foreignFieldConfigs.add(fieldConfig);
		fieldConfig = new DatabaseFieldConfig();
		fieldConfig.setFieldName("stuff");
		foreignFieldConfigs.add(fieldConfig);

		DatabaseTableConfig<ForeignObjectNoAnnotations> foreignTableConfig =
				new DatabaseTableConfig<ForeignObjectNoAnnotations>(databaseType, ForeignObjectNoAnnotations.class,
						foreignFieldConfigs);
		Dao<ForeignObjectNoAnnotations, Integer> foreignDao = createDao(foreignTableConfig, true);

		ArrayList<DatabaseFieldConfig> parentFieldConfigs = new ArrayList<DatabaseFieldConfig>();
		fieldConfig = new DatabaseFieldConfig();
		fieldConfig.setFieldName("id");
		fieldConfig.setGeneratedId(true);
		parentFieldConfigs.add(fieldConfig);
		fieldConfig = new DatabaseFieldConfig();
		fieldConfig.setFieldName("name");
		parentFieldConfigs.add(fieldConfig);
		fieldConfig = new DatabaseFieldConfig();
		fieldConfig.setFieldName("foreign");
		fieldConfig.setForeign(true);
		fieldConfig.setForeignTableConfig(foreignTableConfig);
		fieldConfig.setMaxForeignAutoRefreshLevel(2);
		parentFieldConfigs.add(fieldConfig);

		Dao<ObjectNoAnnotations, Integer> parentDao =
				createDao(new DatabaseTableConfig<ObjectNoAnnotations>(databaseType, ObjectNoAnnotations.class,
						parentFieldConfigs), true);

		ForeignObjectNoAnnotations foreign = new ForeignObjectNoAnnotations();
		foreign.stuff = "hello";
		foreignDao.create(foreign);

		ObjectNoAnnotations parent = new ObjectNoAnnotations();
		parent.name = "wow lookie";
		parent.foreign = foreign;
		parentDao.create(parent);

		ForeignObjectNoAnnotations foreignResult = foreignDao.queryForId(foreign.id);
		assertNotNull(foreignResult);
		assertNotSame(foreign, foreignResult);
		assertEquals(foreign.id, foreignResult.id);
		assertEquals(foreign.stuff, foreignResult.stuff);

		ObjectNoAnnotations parentResult = parentDao.queryForId(parent.id);
		assertNotNull(parentResult);
		assertEquals(parent.id, parentResult.id);
		assertEquals(parent.name, parentResult.name);
		assertNotNull(parentResult.foreign);
		assertNotSame(foreign, parentResult.foreign);
		assertEquals(foreign.id, parentResult.foreign.id);
		assertNull(parentResult.foreign.stuff);
	}

	/* ========================================================================================================= */

	protected static class LocalFoo {
		@DatabaseField
		String name;
		@DatabaseField(columnName = RANK_DB_COLUMN_NAME, width = RANK_WIDTH)
		String rank;
		@DatabaseField(defaultValue = SERIAL_DEFAULT_VALUE)
		Integer serial;
		@DatabaseField(dataType = DataType.LONG)
		long intLong;
	}

	protected static class DateDefaultBad {
		@DatabaseField(defaultValue = "bad value")
		Date date;
	}

	protected static class SerializableField {
		@DatabaseField
		Date date;
	}

	protected static class NumberId {
		@DatabaseField(id = true)
		int id;
	}

	protected static class NoId {
		@DatabaseField
		String name;
	}

	protected static class UnknownFieldType {
		@DatabaseField
		Void oops;
	}

	protected static class IdAndGeneratedId {
		@DatabaseField(id = true, generatedId = true)
		int id;
	}

	protected static class GeneratedIdAndSequence {
		@DatabaseField(generatedId = true, generatedIdSequence = "foo")
		int id;
	}

	protected static class GeneratedId {
		@DatabaseField(generatedId = true)
		int id;
	}

	protected static class GeneratedIdSequence {
		@DatabaseField(generatedIdSequence = SEQ_NAME)
		int id;
	}

	protected static class GeneratedIdCantBeGenerated {
		@DatabaseField(generatedId = true)
		String id;
	}

	protected static class ForeignParent {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		String name;
		@DatabaseField(foreign = true)
		ForeignForeign foreign;
	}

	protected static class ForeignForeign {
		@DatabaseField(id = true)
		int id;
		@DatabaseField()
		String stuff;
	}

	protected static class ForeignAutoRefresh {
		@DatabaseField
		String name;
		@DatabaseField(foreign = true, foreignAutoRefresh = true)
		ForeignForeign foreign;
	}

	protected static class ForeignAutoRefreshWrong {
		@DatabaseField(foreignAutoRefresh = true)
		String notForeign;
	}

	protected static class ForeignColumnNameWrong {
		@DatabaseField(foreignColumnName = "zip")
		String notForeign;
	}

	protected static class ForeignPrimitive {
		@DatabaseField(foreign = true)
		int id;
	}

	protected static class ForeignNoId {
		@DatabaseField(foreign = true)
		NoId foo;
	}

	protected static class ForeignAlsoId {
		@DatabaseField(foreign = true, id = true)
		ForeignForeign foo;
	}

	protected static class ForeignSerializable implements Serializable {
		private static final long serialVersionUID = -8548265783542973824L;
		@DatabaseField(id = true)
		int id;
	}

	protected static class ForeignAlsoSerializable {
		@DatabaseField(foreign = true)
		ForeignSerializable foo;
	}

	protected static class ObjectFieldNotForeign {
		@DatabaseField
		LocalFoo foo;
	}

	protected static class GetSetNoGet {
		@DatabaseField(id = true, useGetSet = true)
		int id;
	}

	protected static class GetSetGetWrongType {
		@DatabaseField(id = true, useGetSet = true)
		int id;

		public long getId() {
			return id;
		}
	}

	protected static class GetSetNoSet {
		@DatabaseField(id = true, useGetSet = true)
		int id;

		public int getId() {
			return id;
		}
	}

	protected static class GetSetSetWrongType {
		@DatabaseField(id = true, useGetSet = true)
		int id;

		public int getId() {
			return id;
		}

		public void setId(long id) {
			this.id = 0;
		}
	}

	protected static class GetSetReturnNotVoid {
		@DatabaseField(id = true, useGetSet = true)
		int id;

		public int getId() {
			return id;
		}

		public int setId(int id) {
			return this.id;
		}
	}

	protected static class GetSet {
		@DatabaseField(id = true, useGetSet = true)
		int id;

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}
	}

	protected static class NoAnnotation {
		int id;
	}

	protected static class CanBeNull {
		@DatabaseField(canBeNull = true)
		int field1;
		@DatabaseField(canBeNull = false)
		int field2;
	}

	protected static class GeneratedIdDefault {
		@DatabaseField(generatedId = true, defaultValue = "2")
		Integer id;
		@DatabaseField
		String stuff;
	}

	protected static class ThrowIfNullNonPrimitive {
		@DatabaseField(throwIfNull = true)
		Integer notPrimitive;
		@DatabaseField(throwIfNull = true)
		int primitive;
	}

	protected static class InvalidType {
		// we self reference here because we are looking for a class which isn't serializable
		@DatabaseField(dataType = DataType.SERIALIZABLE)
		InvalidType intField;
	}

	protected static class InvalidEnumType {
		@DatabaseField(dataType = DataType.ENUM_STRING)
		String stuff;
	}

	protected static class Recursive {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(foreign = true)
		Recursive foreign;

		public Recursive() {
		}
	}

	protected static class SerializableNoDataType {
		@DatabaseField
		Serializable serial;
	}

	protected static class ByteArrayNoDataType {
		@DatabaseField
		byte[] bytes;
	}

	protected static class ForeignCollectionNoGeneric {
		@DatabaseField
		int id;
		@SuppressWarnings("rawtypes")
		@ForeignCollectionField
		ForeignCollection foreignStuff;
	}

	protected static class ImproperIdType {
		@DatabaseField(id = true, dataType = DataType.SERIALIZABLE)
		Serializable id;
	}

	protected static class DefaultTypes {
		@DatabaseField
		boolean booleanField;
		@DatabaseField
		byte byteField;
		@DatabaseField
		char charField;
		@DatabaseField
		short shortField;
		@DatabaseField
		int intField;
		@DatabaseField
		long longField;
		@DatabaseField
		float floatField;
		@DatabaseField
		double doubleField;
	}

	protected static class AllowGeneratedIdNotGeneratedId {
		@DatabaseField(generatedId = true)
		int id;

		@DatabaseField(allowGeneratedIdInsert = true)
		String stuff;
	}

	protected static class VersionFieldWrongType {
		@DatabaseField(generatedId = true)
		int id;

		@DatabaseField(version = true)
		String version;
	}

	protected static class ForeignAutoCreateNoForeign {
		@DatabaseField(generatedId = true)
		long id;
		@DatabaseField(foreignAutoCreate = true)
		public long foreign;
	}

	protected static class ForeignAutoCreateNoGeneratedId {
		@DatabaseField(generatedId = true)
		long id;
		@DatabaseField(foreign = true, foreignAutoCreate = true)
		public ForeignAutoCreateForeignNotGeneratedId foreign;
	}

	protected static class ForeignAutoCreateForeignNotGeneratedId {
		@DatabaseField(id = true)
		long id;
		@DatabaseField
		String stuff;
	}

	protected static class ForeignCollectionForeign {
		@DatabaseField(id = true)
		long id;
		@DatabaseField(foreign = true)
		ForeignCollection<String> collection;
	}

	private static class NeedsUppercaseSequenceDatabaseType extends NeedsSequenceDatabaseType {
		public NeedsUppercaseSequenceDatabaseType() throws SQLException {
			super();
		}

		@Override
		public boolean isEntityNamesMustBeUpCase() {
			return true;
		}
	}

	private static class NeedsSequenceDatabaseType extends H2DatabaseType {
		public NeedsSequenceDatabaseType() throws SQLException {
			super();
		}

		@Override
		public boolean isIdSequenceNeeded() {
			return true;
		}
	}

	protected static class DefaultEmptyString {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(defaultValue = "")
		String defaultBlank;

		public DefaultEmptyString() {
			// for ormlite
		}
	}

	protected static class ObjectNoAnnotations {
		int id;
		String name;
		ForeignObjectNoAnnotations foreign;
	}

	protected static class ForeignObjectNoAnnotations {
		int id;
		String stuff;
	}
}
