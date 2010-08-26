package com.j256.ormlite.field;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;

import org.junit.Test;

import com.j256.ormlite.BaseOrmLiteTest;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.db.PostgresDatabaseType;
import com.j256.ormlite.support.DatabaseResults;
import com.j256.ormlite.table.TableInfo;

public class FieldTypeTest extends BaseOrmLiteTest {

	private static final String RANK_DB_COLUMN_NAME = "rank_column";
	private static final int RANK_WIDTH = 100;
	private static final String SERIAL_DEFAULT_VALUE = "7";
	private static final String SEQ_NAME = "sequence";

	@Test
	public void testFieldType() throws Exception {

		Field[] fields = Foo.class.getDeclaredFields();
		assertTrue(fields.length >= 4);
		Field nameField = fields[0];
		Field rankField = fields[1];
		Field serialField = fields[2];
		Field intLongField = fields[3];

		FieldType fieldType = FieldType.createFieldType(databaseType, Foo.class.getSimpleName(), nameField);
		assertEquals(nameField.getName(), fieldType.getDbColumnName());
		assertEquals(DataType.STRING, fieldType.getDataType());
		assertEquals(0, fieldType.getWidth());
		assertTrue(fieldType.toString().contains("Foo"));
		assertTrue(fieldType.toString().contains(nameField.getName()));

		fieldType = FieldType.createFieldType(databaseType, Foo.class.getSimpleName(), rankField);
		assertEquals(RANK_DB_COLUMN_NAME, fieldType.getDbColumnName());
		assertEquals(DataType.STRING, fieldType.getDataType());
		assertEquals(RANK_WIDTH, fieldType.getWidth());

		fieldType = FieldType.createFieldType(databaseType, Foo.class.getSimpleName(), serialField);
		assertEquals(serialField.getName(), fieldType.getDbColumnName());
		assertEquals(DataType.INTEGER_OBJ, fieldType.getDataType());
		assertEquals(Integer.parseInt(SERIAL_DEFAULT_VALUE), fieldType.getDefaultValue());

		fieldType = FieldType.createFieldType(databaseType, Foo.class.getSimpleName(), intLongField);
		assertEquals(intLongField.getName(), fieldType.getDbColumnName());
		assertFalse(fieldType.isGeneratedId());
		assertEquals(DataType.LONG, fieldType.getDataType());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUnknownFieldType() throws Exception {
		Field[] fields = UnknownFieldType.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		FieldType.createFieldType(databaseType, UnknownFieldType.class.getSimpleName(), fields[0]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testIdAndGeneratedId() throws Exception {
		Field[] fields = IdAndGeneratedId.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		FieldType.createFieldType(databaseType, IdAndGeneratedId.class.getSimpleName(), fields[0]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGeneratedIdAndSequence() throws Exception {
		Field[] fields = GeneratedIdAndSequence.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		FieldType.createFieldType(databaseType, GeneratedIdAndSequence.class.getSimpleName(), fields[0]);
	}

	@Test
	public void testGeneratedIdAndSequenceWorks() throws Exception {
		databaseType = new PostgresDatabaseType();
		Field[] fields = GeneratedIdSequence.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		FieldType fieldType =
				FieldType.createFieldType(databaseType, GeneratedIdSequence.class.getSimpleName(), fields[0]);
		assertTrue(fieldType.isGeneratedIdSequence());
		assertEquals(SEQ_NAME, fieldType.getGeneratedIdSequence());
	}

	@Test
	public void testGeneratedIdGetsASequence() throws Exception {
		databaseType = new PostgresDatabaseType();
		Field[] fields = GeneratedId.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		FieldType fieldType = FieldType.createFieldType(databaseType, GeneratedId.class.getSimpleName(), fields[0]);
		assertTrue(fieldType.isGeneratedIdSequence());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGeneratedIdCantBeGenerated() throws Exception {
		Field[] fields = GeneratedIdCantBeGenerated.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		FieldType.createFieldType(databaseType, GeneratedIdCantBeGenerated.class.getSimpleName(), fields[0]);
	}

	@Test
	public void testFieldTypeConverter() throws Exception {
		Field[] fields = Foo.class.getDeclaredFields();
		assertTrue(fields.length >= 4);
		Field nameField = fields[0];
		DatabaseType databaseType = createMock(DatabaseType.class);
		final SqlType sqlType = SqlType.DATE;
		final String nameArg = "zippy buzz";
		final String nameResult = "blabber bling";
		expect(databaseType.getFieldConverter(isA(FieldType.class))).andReturn(new FieldConverter() {
			public SqlType getSqlType() {
				return sqlType;
			}
			public Object parseDefaultString(FieldType fieldType, String defaultStr) {
				return defaultStr;
			}
			public Object javaToArg(FieldType fieldType, Object javaObject) {
				return nameArg;
			}
			public Object resultToJava(FieldType fieldType, DatabaseResults resultSet, int columnPos)
					throws SQLException {
				return nameResult;
			}
			public boolean isStreamType() {
				return false;
			}
		});
		expect(databaseType.isEntityNamesMustBeUpCase()).andReturn(false);
		expect(databaseType.convertColumnName(isA(String.class))).andReturn("name");
		replay(databaseType);
		FieldType fieldType = FieldType.createFieldType(databaseType, Foo.class.getSimpleName(), nameField);
		verify(databaseType);

		assertEquals(sqlType, fieldType.getSqlTypeVal());
		Foo foo = new Foo();
		// it can't be null
		foo.name = nameArg + " not that";
		assertEquals(nameArg, fieldType.getConvertedFieldValue(foo));

		DatabaseResults resultMock = createMock(DatabaseResults.class);
		expect(resultMock.findColumn("name")).andReturn(0);
		expect(resultMock.isNull(0)).andReturn(false);
		replay(resultMock);
		assertEquals(nameResult, fieldType.resultToJava(resultMock, new HashMap<String, Integer>()));
		verify(resultMock);
	}

	@Test
	public void testFieldForeign() throws Exception {

		Field[] fields = ForeignParent.class.getDeclaredFields();
		assertTrue(fields.length >= 2);
		Field nameField = fields[0];
		Field bazField = fields[1];

		FieldType fieldType = FieldType.createFieldType(databaseType, ForeignParent.class.getSimpleName(), nameField);
		assertEquals(nameField.getName(), fieldType.getDbColumnName());
		assertEquals(DataType.STRING, fieldType.getDataType());
		assertNull(fieldType.getForeignTableInfo());
		assertEquals(0, fieldType.getWidth());

		fieldType = FieldType.createFieldType(databaseType, ForeignParent.class.getSimpleName(), bazField);
		assertEquals(bazField.getName() + FieldType.FOREIGN_ID_FIELD_SUFFIX, fieldType.getDbColumnName());
		// this is the type of the foreign object's id
		assertEquals(DataType.INTEGER, fieldType.getDataType());
		TableInfo<?> foreignTableInfo = fieldType.getForeignTableInfo();
		assertNotNull(foreignTableInfo);
		assertEquals(ForeignForeign.class, foreignTableInfo.getDataClass());
	}

	@Test
	@ExpectedBehavior(expected = IllegalArgumentException.class)
	public void testPrimitiveForeign() throws Exception {
		Field[] fields = ForeignPrimitive.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		Field idField = fields[0];
		FieldType.createFieldType(databaseType, ForeignPrimitive.class.getSimpleName(), idField);
	}

	@Test
	@ExpectedBehavior(expected = IllegalArgumentException.class)
	public void testForeignNoId() throws Exception {
		Field[] fields = ForeignNoId.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		Field fooField = fields[0];
		FieldType.createFieldType(databaseType, ForeignNoId.class.getSimpleName(), fooField);
	}

	@Test
	@ExpectedBehavior(expected = IllegalArgumentException.class)
	public void testForeignAlsoId() throws Exception {
		Field[] fields = ForeignAlsoId.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		Field fooField = fields[0];
		FieldType.createFieldType(databaseType, ForeignAlsoId.class.getSimpleName(), fooField);
	}

	@Test
	@ExpectedBehavior(expected = IllegalArgumentException.class)
	public void testObjectFieldNotForeign() throws Exception {
		Field[] fields = ObjectFieldNotForeign.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		Field fooField = fields[0];
		FieldType.createFieldType(databaseType, ObjectFieldNotForeign.class.getSimpleName(), fooField);
	}

	@Test
	@ExpectedBehavior(expected = IllegalArgumentException.class)
	public void testGetSetNoGet() throws Exception {
		Field[] fields = GetSetNoGet.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		Field idField = fields[0];
		FieldType.createFieldType(databaseType, GetSetNoGet.class.getSimpleName(), idField);
	}

	@Test
	@ExpectedBehavior(expected = IllegalArgumentException.class)
	public void testGetSetGetWrongType() throws Exception {
		Field[] fields = GetSetGetWrongType.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		Field idField = fields[0];
		FieldType.createFieldType(databaseType, GetSetGetWrongType.class.getSimpleName(), idField);
	}

	@Test
	@ExpectedBehavior(expected = IllegalArgumentException.class)
	public void testGetSetNoSet() throws Exception {
		Field[] fields = GetSetNoSet.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		Field idField = fields[0];
		FieldType.createFieldType(databaseType, GetSetNoSet.class.getSimpleName(), idField);
	}

	@Test
	@ExpectedBehavior(expected = IllegalArgumentException.class)
	public void testGetSetSetWrongType() throws Exception {
		Field[] fields = GetSetSetWrongType.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		Field idField = fields[0];
		FieldType.createFieldType(databaseType, GetSetSetWrongType.class.getSimpleName(), idField);
	}

	@Test
	@ExpectedBehavior(expected = IllegalArgumentException.class)
	public void testGetSetSetReturnNotVoid() throws Exception {
		Field[] fields = GetSetReturnNotVoid.class.getDeclaredFields();
		assertNotNull(fields);
		assertTrue(fields.length >= 1);
		Field idField = fields[0];
		FieldType.createFieldType(databaseType, GetSetReturnNotVoid.class.getSimpleName(), idField);
	}

	@Test
	public void testGetSet() throws Exception {
		Field[] fields = GetSet.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		Field idField = fields[0];
		FieldType.createFieldType(databaseType, GetSet.class.getSimpleName(), idField);
	}

	@Test
	public void testGetAndSetValue() throws Exception {
		Field[] fields = GetSet.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		Field idField = fields[0];
		FieldType fieldType = FieldType.createFieldType(databaseType, GetSet.class.getSimpleName(), idField);
		GetSet getSet = new GetSet();
		int id = 121312321;
		getSet.id = id;
		assertEquals(id, fieldType.getConvertedFieldValue(getSet));
		int id2 = 869544;
		fieldType.assignField(getSet, id2);
		assertEquals(id2, fieldType.getConvertedFieldValue(getSet));
	}

	@Test
	@ExpectedBehavior(expected = SQLException.class)
	public void testGetWrongObject() throws Exception {
		Field[] fields = GetSet.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		Field idField = fields[0];
		FieldType fieldType = FieldType.createFieldType(databaseType, GetSet.class.getSimpleName(), idField);
		fieldType.getConvertedFieldValue(new Object());
	}

	@Test
	@ExpectedBehavior(expected = SQLException.class)
	public void testSetWrongObject() throws Exception {
		Field[] fields = GetSet.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		Field idField = fields[0];
		FieldType fieldType = FieldType.createFieldType(databaseType, GetSet.class.getSimpleName(), idField);
		fieldType.assignField(new Object(), 10);
	}

	@Test
	public void testCreateFieldTypeNull() throws Exception {
		Field[] fields = NoAnnotation.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		Field idField = fields[0];
		assertNull(FieldType.createFieldType(databaseType, NoAnnotation.class.getSimpleName(), idField));
	}

	@Test
	public void testSetValueField() throws Exception {
		Field[] fields = Foo.class.getDeclaredFields();
		assertTrue(fields.length >= 4);
		Field nameField = fields[0];
		FieldType fieldType = FieldType.createFieldType(databaseType, Foo.class.getSimpleName(), nameField);
		Foo foo = new Foo();
		String name1 = "wfwef";
		fieldType.assignField(foo, name1);
		assertEquals(name1, foo.name);
	}

	@Test
	public void testSetIdField() throws Exception {
		Field[] fields = NumberId.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		Field nameField = fields[0];
		FieldType fieldType = FieldType.createFieldType(databaseType, NumberId.class.getSimpleName(), nameField);
		NumberId foo = new NumberId();
		int id = 10;
		fieldType.assignIdValue(foo, id);
		assertEquals(id, foo.id);
	}

	@Test
	@ExpectedBehavior(expected = SQLException.class)
	public void testSetIdFieldString() throws Exception {
		Field[] fields = Foo.class.getDeclaredFields();
		assertTrue(fields.length >= 4);
		Field nameField = fields[0];
		FieldType fieldType = FieldType.createFieldType(databaseType, Foo.class.getSimpleName(), nameField);
		fieldType.assignIdValue(new Foo(), 10);
	}

	@Test
	public void testCanBeNull() throws Exception {
		Field[] fields = CanBeNull.class.getDeclaredFields();
		assertTrue(fields.length >= 2);
		Field field = fields[0];
		FieldType fieldType = FieldType.createFieldType(databaseType, CanBeNull.class.getSimpleName(), field);
		assertTrue(fieldType.isCanBeNull());
		field = fields[1];
		fieldType = FieldType.createFieldType(databaseType, CanBeNull.class.getSimpleName(), field);
		assertFalse(fieldType.isCanBeNull());
	}

	@Test
	public void testAssignForeign() throws Exception {
		Field[] fields = ForeignParent.class.getDeclaredFields();
		assertTrue(fields.length >= 2);
		Field field = fields[1];
		FieldType fieldType = FieldType.createFieldType(databaseType, ForeignParent.class.getSimpleName(), field);
		assertTrue(fieldType.isForeign());
		int id = 10;
		ForeignParent parent = new ForeignParent();
		assertNull(parent.foreign);
		// we assign the id, not the object
		fieldType.assignField(parent, id);
		ForeignForeign foreign = parent.foreign;
		assertEquals(id, foreign.id);

		// not try assigning it again
		fieldType.assignField(parent, id);
		// foreign field should not have been changed
		assertSame(foreign, parent.foreign);

		// now assign a different id
		int newId = id + 1;
		fieldType.assignField(parent, newId);
		assertNotSame(foreign, parent.foreign);
		assertEquals(newId, parent.foreign.id);
	}

	@Test
	@ExpectedBehavior(expected = SQLException.class)
	public void testGeneratedIdDefaultValue() throws Exception {
		Field[] fields = GeneratedIdDefault.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		Field idField = fields[0];
		FieldType.createFieldType(databaseType, GeneratedIdDefault.class.getSimpleName(), idField);
	}

	@Test
	@ExpectedBehavior(expected = SQLException.class)
	public void testThrowIfNullNotPrimitive() throws Exception {
		Field[] fields = ThrowIfNullNonPrimitive.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		Field field = fields[0];
		FieldType.createFieldType(databaseType, ThrowIfNullNonPrimitive.class.getSimpleName(), field);
	}

	@Test
	@ExpectedBehavior(expected = SQLException.class)
	public void testBadDateDefaultValue() throws Exception {
		Field[] fields = DateDefaultBad.class.getDeclaredFields();
		assertTrue(fields.length >= 1);
		Field field = fields[0];
		FieldType.createFieldType(databaseType, DateDefaultBad.class.getSimpleName(), field);
	}

	protected static class Foo {
		@DatabaseField
		String name;
		@DatabaseField(columnName = RANK_DB_COLUMN_NAME, width = RANK_WIDTH)
		String rank;
		@DatabaseField(defaultValue = SERIAL_DEFAULT_VALUE)
		Integer serial;
		@DatabaseField(dataType = DataType.LONG)
		int intLong;
	}

	protected static class DateDefaultBad {
		@DatabaseField(defaultValue = "bad value")
		Date date;
	}

	protected static class SerializableDefault {
		@DatabaseField(defaultValue = "bad value")
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
		@DatabaseField
		String name;
		@DatabaseField(foreign = true)
		ForeignForeign foreign;
	}

	protected static class ForeignForeign {
		@DatabaseField(id = true)
		int id;
	}

	protected static class ForeignPrimitive {
		@DatabaseField(foreign = true)
		String id;
	}

	protected static class ForeignNoId {
		@DatabaseField(foreign = true)
		NoId foo;
	}

	protected static class ForeignAlsoId {
		@DatabaseField(foreign = true, id = true)
		ForeignForeign foo;
	}

	protected static class ObjectFieldNotForeign {
		@DatabaseField
		Foo foo;
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
	}
}
