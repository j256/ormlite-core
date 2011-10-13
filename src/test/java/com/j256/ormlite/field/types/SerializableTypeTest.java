package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;
import com.j256.ormlite.table.DatabaseTable;

public class SerializableTypeTest extends BaseTypeTest {

	private static final String SERIALIZABLE_COLUMN = "serializable";
	private static final String BYTE_COLUMN = "byteField";

	@Test
	public void testSerializable() throws Exception {
		Class<LocalSerializable> clazz = LocalSerializable.class;
		Dao<LocalSerializable, Object> dao = createDao(clazz, true);
		Integer val = 1331333131;
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		ObjectOutputStream objOutStream = new ObjectOutputStream(outStream);
		objOutStream.writeObject(val);
		byte[] sqlArg = outStream.toByteArray();
		String valStr = val.toString();
		LocalSerializable foo = new LocalSerializable();
		foo.serializable = val;
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, val, val, sqlArg, valStr, DataType.SERIALIZABLE, SERIALIZABLE_COLUMN, false, false,
				true, false, true, true, false, false);
	}

	@Test
	public void testSerializableNull() throws Exception {
		Class<LocalSerializable> clazz = LocalSerializable.class;
		Dao<LocalSerializable, Object> dao = createDao(clazz, true);
		LocalSerializable foo = new LocalSerializable();
		assertEquals(1, dao.create(foo));
		testType(dao, foo, clazz, null, null, null, null, DataType.SERIALIZABLE, SERIALIZABLE_COLUMN, false, false,
				true, false, true, true, false, false);
	}

	@Test
	public void testSerializableNoValue() throws Exception {
		Class<LocalSerializable> clazz = LocalSerializable.class;
		Dao<LocalSerializable, Object> dao = createDao(clazz, true);
		LocalSerializable foo = new LocalSerializable();
		foo.serializable = null;
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadOnlyConnection();
		CompiledStatement stmt = null;
		try {
			stmt = conn.compileStatement("select * from " + TABLE_NAME, StatementType.SELECT, noFieldTypes);
			DatabaseResults results = stmt.runQuery(null);
			assertTrue(results.next());
			FieldType fieldType =
					FieldType.createFieldType(connectionSource, TABLE_NAME,
							clazz.getDeclaredField(SERIALIZABLE_COLUMN), clazz);
			assertNull(DataType.SERIALIZABLE.getDataPersister().resultToJava(fieldType, results,
					results.findColumn(SERIALIZABLE_COLUMN)));
		} finally {
			if (stmt != null) {
				stmt.close();
			}
			connectionSource.releaseConnection(conn);
		}
	}

	@Test(expected = SQLException.class)
	public void testSerializableInvalidResult() throws Exception {
		Class<LocalByteArray> clazz = LocalByteArray.class;
		Dao<LocalByteArray, Object> dao = createDao(clazz, true);
		LocalByteArray foo = new LocalByteArray();
		foo.byteField = new byte[] { 1, 2, 3, 4, 5 };
		assertEquals(1, dao.create(foo));
		DatabaseConnection conn = connectionSource.getReadOnlyConnection();
		CompiledStatement stmt = null;
		try {
			stmt = conn.compileStatement("select * from " + TABLE_NAME, StatementType.SELECT, noFieldTypes);
			DatabaseResults results = stmt.runQuery(null);
			assertTrue(results.next());
			FieldType fieldType =
					FieldType.createFieldType(connectionSource, TABLE_NAME,
							LocalSerializable.class.getDeclaredField(SERIALIZABLE_COLUMN), LocalSerializable.class);
			DataType.SERIALIZABLE.getDataPersister().resultToJava(fieldType, results, results.findColumn(BYTE_COLUMN));
		} finally {
			if (stmt != null) {
				stmt.close();
			}
			connectionSource.releaseConnection(conn);
		}
	}

	@Test(expected = SQLException.class)
	public void testSerializableParseDefault() throws Exception {
		DataType.SERIALIZABLE.getDataPersister().parseDefaultString(null, null);
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalSerializable {
		@DatabaseField(columnName = SERIALIZABLE_COLUMN, dataType = DataType.SERIALIZABLE)
		Integer serializable;;
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalByteArray {
		@DatabaseField(columnName = BYTE_COLUMN, dataType = DataType.BYTE_ARRAY)
		byte[] byteField;
	}
}
