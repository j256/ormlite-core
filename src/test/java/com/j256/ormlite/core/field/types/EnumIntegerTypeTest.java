package com.j256.ormlite.core.field.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.j256.ormlite.core.dao.Dao;
import com.j256.ormlite.core.field.DataType;
import com.j256.ormlite.core.field.DatabaseField;
import com.j256.ormlite.core.field.SqlType;
import com.j256.ormlite.core.stmt.StatementBuilder.StatementType;
import com.j256.ormlite.core.support.CompiledStatement;
import com.j256.ormlite.core.support.DatabaseConnection;
import com.j256.ormlite.core.support.DatabaseResults;
import com.j256.ormlite.core.table.DatabaseTable;

public class EnumIntegerTypeTest extends BaseTypeTest {

	private static final String ENUM_COLUMN = "ourEnum";

	@Test
	public void testEnumInt() throws Exception {
		Class<LocalEnumInt> clazz = LocalEnumInt.class;
		Dao<LocalEnumInt, Object> dao = createDao(clazz, true);
		OurEnum val = OurEnum.SECOND;
		int sqlVal = val.ordinal();
		String valStr = Integer.toString(sqlVal);
		LocalEnumInt foo = new LocalEnumInt();
		foo.ourEnum = val;
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, val, sqlVal, sqlVal, valStr, DataType.ENUM_INTEGER, ENUM_COLUMN, false, true, false,
				false, false, false, true, false);
	}

	@Test
	public void testEnumIntNull() throws Exception {
		Class<LocalEnumInt> clazz = LocalEnumInt.class;
		Dao<LocalEnumInt, Object> dao = createDao(clazz, true);
		LocalEnumInt foo = new LocalEnumInt();
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, null, null, null, null, DataType.ENUM_INTEGER, ENUM_COLUMN, false, true, false, false,
				false, false, true, false);
	}

	@Test
	public void testEnumIntResultsNoFieldType() throws Exception {
		Class<LocalEnumInt> clazz = LocalEnumInt.class;
		Dao<LocalEnumInt, Object> dao = createDao(clazz, true);
		OurEnum val = OurEnum.SECOND;
		LocalEnumInt foo = new LocalEnumInt();
		foo.ourEnum = val;
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadOnlyConnection(FOO_TABLE_NAME);
		CompiledStatement stmt = null;
		try {
			stmt = conn.compileStatement("select * from " + TABLE_NAME, StatementType.SELECT, noFieldTypes,
					DatabaseConnection.DEFAULT_RESULT_FLAGS, true);
			DatabaseResults results = stmt.runQuery(null);
			assertTrue(results.next());
			assertEquals(val.ordinal(), DataType.ENUM_INTEGER.getDataPersister().resultToJava(null, results,
					results.findColumn(ENUM_COLUMN)));
		} finally {
			if (stmt != null) {
				stmt.close();
			}
			connectionSource.releaseConnection(conn);
		}
	}

	@Test
	public void testDefaultValue() throws Exception {
		Dao<EnumDefault, Object> dao = createDao(EnumDefault.class, true);
		EnumDefault enumDefault = new EnumDefault();
		assertEquals(1, dao.create(enumDefault));
		EnumDefault result = dao.queryForId(enumDefault.id);
		assertNotNull(result);
		assertEquals(OurEnum.SECOND, result.ourEnum);
	}

	@Test
	public void testCoverage() {
		new EnumIntegerType(SqlType.INTEGER, new Class[0]);
	}

	/* ============================================================================================ */

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalEnumInt {
		@DatabaseField(columnName = ENUM_COLUMN, dataType = DataType.ENUM_INTEGER)
		OurEnum ourEnum;
	}

	private enum OurEnum {
		FIRST,
		SECOND,;
	}

	protected static class EnumDefault {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(defaultValue = "SECOND")
		OurEnum ourEnum;
	}
}
