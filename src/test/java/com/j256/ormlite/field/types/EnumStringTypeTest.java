package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.List;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;
import com.j256.ormlite.table.DatabaseTable;

public class EnumStringTypeTest extends BaseTypeTest {

	private static final String ENUM_COLUMN = "ourEnum";

	@Test
	public void testEnumString() throws Exception {
		Class<LocalEnumString> clazz = LocalEnumString.class;
		Dao<LocalEnumString, Object> dao = createDao(clazz, true);
		OurEnum val = OurEnum.SECOND;
		String valStr = val.toString();
		String sqlVal = valStr;
		LocalEnumString foo = new LocalEnumString();
		foo.ourEnum = val;
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, val, sqlVal, sqlVal, valStr, DataType.ENUM_STRING, ENUM_COLUMN, false, true, true,
				false, false, false, true, false);
	}

	@Test
	public void testEnumStringNull() throws Exception {
		Class<LocalEnumString> clazz = LocalEnumString.class;
		Dao<LocalEnumString, Object> dao = createDao(clazz, true);
		LocalEnumString foo = new LocalEnumString();
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, null, null, null, null, DataType.ENUM_STRING, ENUM_COLUMN, false, true, true, false,
				false, false, true, false);
	}

	@Test
	public void testEnumStringResultsNoFieldType() throws Exception {
		Dao<LocalEnumString, Object> dao = createDao(LocalEnumString.class, true);
		OurEnum val = OurEnum.SECOND;
		LocalEnumString foo = new LocalEnumString();
		foo.ourEnum = val;
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadOnlyConnection(TABLE_NAME);
		CompiledStatement stmt = null;
		try {
			stmt = conn.compileStatement("select * from " + TABLE_NAME, StatementType.SELECT, noFieldTypes,
					DatabaseConnection.DEFAULT_RESULT_FLAGS, true);
			DatabaseResults results = stmt.runQuery(null);
			assertTrue(results.next());
			assertEquals(val.toString(), DataType.ENUM_STRING.getDataPersister().resultToJava(null, results,
					results.findColumn(ENUM_COLUMN)));
		} finally {
			if (stmt != null) {
				stmt.close();
			}
			connectionSource.releaseConnection(conn);
		}
	}

	@Test(expected = SQLException.class)
	public void testUnknownEnumValue() throws Exception {
		Dao<LocalEnumString, Object> dao = createDao(LocalEnumString.class, true);
		LocalEnumString localEnumString = new LocalEnumString();
		localEnumString.ourEnum = OurEnum.FIRST;
		assertEquals(1, dao.create(localEnumString));
		assertEquals(1, dao.updateRaw("UPDATE Foo set ourEnum = 'THIRD'"));
		dao.queryForAll();
	}

	@Test
	public void testUnknownValueAnnotation() throws Exception {
		Dao<LocalUnknownEnum, Object> dao = createDao(LocalUnknownEnum.class, true);
		LocalUnknownEnum localUnknownEnum = new LocalUnknownEnum();
		localUnknownEnum.ourEnum = OurEnum.SECOND;
		assertEquals(1, dao.create(localUnknownEnum));
		assertEquals(1, dao.updateRaw("UPDATE Foo set ourEnum = 'THIRD'"));
		List<LocalUnknownEnum> unknowns = dao.queryForAll();
		assertEquals(1, unknowns.size());
		assertEquals(OurEnum.FIRST, unknowns.get(0).ourEnum);
	}

	@Test
	public void testDefaultValue() throws Exception {
		Dao<EnumDefault, Object> dao = createDao(EnumDefault.class, true);
		EnumDefault enumDefault = new EnumDefault();
		assertEquals(1, dao.create(enumDefault));
		List<EnumDefault> unknowns = dao.queryForAll();
		assertEquals(1, unknowns.size());
		assertEquals(OurEnum.SECOND, unknowns.get(0).ourEnum);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNotEnum() throws Exception {
		createDao(NotEnum.class, true);
	}

	@Test
	public void testCoverage() {
		new EnumStringType(SqlType.STRING, new Class[0]);
	}

	/* ================================================================================ */

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalEnumString {
		@DatabaseField(columnName = ENUM_COLUMN)
		OurEnum ourEnum;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalUnknownEnum {
		@DatabaseField(columnName = ENUM_COLUMN, unknownEnumName = "FIRST")
		OurEnum ourEnum;
	}

	protected static class EnumDefault {
		@DatabaseField(defaultValue = "SECOND")
		OurEnum ourEnum;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class NotEnum {
		@DatabaseField(dataType = DataType.ENUM_STRING)
		String notEnum;
	}

	private enum OurEnum {
		FIRST,
		SECOND,
		// end
		;
	}
}
