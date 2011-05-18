package com.j256.ormlite.field.types;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;


public class CharTypeTest extends BaseCoreTest {

	@Test
	public void testPostgresChar() throws Exception {
		Dao<PostgresCharNull, Integer> dao = createDao(PostgresCharNull.class, true);
		PostgresCharNull nullChar = new PostgresCharNull();
		nullChar.charField = '\0';
		assertEquals(1, dao.create(nullChar));
	}

	protected static class PostgresCharNull {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		char charField;
		PostgresCharNull() {
		}
	}
}
