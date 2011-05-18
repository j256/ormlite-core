package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.sql.SQLException;
import java.util.UUID;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

public class UuidTypeTest extends BaseCoreTest {

	@Test
	public void testUuid() throws Exception {
		Dao<UuidClass, Integer> dao = createDao(UuidClass.class, true);
		UuidClass uuid = new UuidClass();
		uuid.uuid = null;
		assertEquals(1, dao.create(uuid));

		UuidClass uuidResult = dao.queryForId(uuid.id);
		assertNotNull(uuidResult);
		assertNull(uuidResult.uuid);
	}

	@Test(expected = SQLException.class)
	public void testUuidDefault() throws Exception {
		createDao(UuidClassDefault.class, true);
	}

	@Test(expected = SQLException.class)
	public void testUuidString() throws Exception {
		Dao<UuidString, Integer> stringDao = createDao(UuidString.class, true);
		UuidString uuidString = new UuidString();
		uuidString.uuid = "not a valid uuid string";
		assertEquals(1, stringDao.create(uuidString));

		Dao<UuidClass, Integer> uuidDao = createDao(UuidClass.class, false);
		uuidDao.queryForId(uuidString.id);
	}

	private static final String UUID_FILE_NAME = "uuid";
	private static final String UUID_TABLE_NAME = "uuidandstring";

	@DatabaseTable(tableName = UUID_TABLE_NAME)
	protected static class UuidClass {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(columnName = UUID_FILE_NAME)
		UUID uuid;
		UuidClass() {
		}
	}

	@DatabaseTable(tableName = UUID_TABLE_NAME)
	protected static class UuidString {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(columnName = UUID_FILE_NAME)
		String uuid;
		UuidString() {
		}
	}

	protected static class UuidClassDefault {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(defaultValue = "xxx")
		UUID uuid;
		UuidClassDefault() {
		}
	}
}
