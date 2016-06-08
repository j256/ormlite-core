package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataPersister;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;

public abstract class BaseTypeTest extends BaseCoreTest {

	protected static final String TABLE_NAME = "foo";
	protected static final FieldType[] noFieldTypes = new FieldType[0];

	protected <T, ID> void testType(Dao<T, ID> dao, T foo, Class<T> clazz, Object javaVal, Object defaultSqlVal,
			Object sqlArg, String defaultValStr, DataType dataType, String columnName, boolean isValidGeneratedType,
			boolean isAppropriateId, boolean isEscapedValue, boolean isPrimitive, boolean isSelectArgRequired,
			boolean isStreamType, boolean isComparable, boolean isConvertableId) throws Exception {
		DataPersister dataPersister = dataType.getDataPersister();
		DatabaseConnection conn = connectionSource.getReadOnlyConnection(TABLE_NAME);
		CompiledStatement stmt = null;
		if (sqlArg != null) {
			assertEquals(defaultSqlVal.getClass(), sqlArg.getClass());
		}
		try {
			stmt = conn.compileStatement("select * from " + TABLE_NAME, StatementType.SELECT, noFieldTypes,
					DatabaseConnection.DEFAULT_RESULT_FLAGS, true);
			DatabaseResults results = stmt.runQuery(null);
			assertTrue(results.next());
			int colNum = results.findColumn(columnName);
			Field field = clazz.getDeclaredField(columnName);
			FieldType fieldType = FieldType.createFieldType(connectionSource, TABLE_NAME, field, clazz);
			assertEquals(dataType.getDataPersister(), fieldType.getDataPersister());
			Class<?>[] classes = fieldType.getDataPersister().getAssociatedClasses();
			if (classes.length > 0) {
				assertTrue(classes[0].isAssignableFrom(fieldType.getType()));
			}
			assertTrue(fieldType.getDataPersister().isValidForField(field));
			if (javaVal instanceof byte[]) {
				assertTrue(Arrays.equals((byte[]) javaVal,
						(byte[]) dataPersister.resultToJava(fieldType, results, colNum)));
			} else {
				Map<String, Integer> colMap = new HashMap<String, Integer>();
				colMap.put(columnName, colNum);
				Object result = fieldType.resultToJava(results, colMap);
				assertEquals(javaVal, result);
			}
			if (dataType == DataType.SERIALIZABLE) {
				try {
					dataPersister.parseDefaultString(fieldType, "");
					fail("parseDefaultString should have thrown for " + dataType);
				} catch (SQLException e) {
					// expected
				}
			} else if (defaultValStr != null) {
				Object parsedDefault = dataPersister.parseDefaultString(fieldType, defaultValStr);
				assertEquals(defaultSqlVal.getClass(), parsedDefault.getClass());
				if (dataType == DataType.BYTE_ARRAY || dataType == DataType.STRING_BYTES) {
					assertTrue(Arrays.equals((byte[]) defaultSqlVal, (byte[]) parsedDefault));
				} else {
					assertEquals(defaultSqlVal, parsedDefault);
				}
			}
			if (sqlArg == null) {
				// noop
			} else if (sqlArg instanceof byte[]) {
				assertTrue(Arrays.equals((byte[]) sqlArg, (byte[]) dataPersister.javaToSqlArg(fieldType, javaVal)));
			} else {
				assertEquals(sqlArg, dataPersister.javaToSqlArg(fieldType, javaVal));
			}
			assertEquals(isValidGeneratedType, dataPersister.isValidGeneratedType());
			assertEquals(isAppropriateId, dataPersister.isAppropriateId());
			assertEquals(isEscapedValue, dataPersister.isEscapedValue());
			assertEquals(isEscapedValue, dataPersister.isEscapedDefaultValue());
			assertEquals(isPrimitive, dataPersister.isPrimitive());
			assertEquals(isSelectArgRequired, dataPersister.isArgumentHolderRequired());
			assertEquals(isStreamType, dataPersister.isStreamType());
			assertEquals(isComparable, dataPersister.isComparable());
			if (isConvertableId) {
				assertNotNull(dataPersister.convertIdNumber(10));
			} else {
				assertNull(dataPersister.convertIdNumber(10));
			}
			List<T> list = dao.queryForAll();
			assertEquals(1, list.size());
			assertTrue(dao.objectsEqual(foo, list.get(0)));
			// if we have a value then look for it, floats don't find any results because of rounding issues
			if (javaVal != null && dataPersister.isComparable() && dataType != DataType.FLOAT
					&& dataType != DataType.FLOAT_OBJ) {
				// test for inline arguments
				list = dao.queryForMatching(foo);
				assertEquals(1, list.size());
				assertTrue(dao.objectsEqual(foo, list.get(0)));
				// test for SelectArg arguments
				list = dao.queryForMatchingArgs(foo);
				assertEquals(1, list.size());
				assertTrue(dao.objectsEqual(foo, list.get(0)));
			}
			if (dataType == DataType.STRING_BYTES || dataType == DataType.BYTE_ARRAY
					|| dataType == DataType.SERIALIZABLE) {
				// no converting from string to value
			} else {
				// test string conversion
				String stringVal = results.getString(colNum);
				Object convertedJavaVal = fieldType.convertStringToJavaField(stringVal, 0);
				assertEquals(javaVal, convertedJavaVal);
			}
		} finally {
			if (stmt != null) {
				stmt.close();
			}
			connectionSource.releaseConnection(conn);
		}
	}

}
