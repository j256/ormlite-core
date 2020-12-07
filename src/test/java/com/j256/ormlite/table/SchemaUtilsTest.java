package com.j256.ormlite.table;

import static org.junit.Assert.assertTrue;

import java.sql.SQLException;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.h2.H2DatabaseType;

public class SchemaUtilsTest extends BaseCoreTest {

	@Test
	public void testCreateSchema() throws SQLException {
		assertTrue(SchemaUtils.createSchema(connectionSource, Schema.class) > 0);
	}

	@Test
	public void testCreateSchemaName() throws SQLException {
		assertTrue(SchemaUtils.createSchema(connectionSource, "schema") > 0);
	}

	@Test
	public void testCreateSchemaDao() throws SQLException {
		Dao<Schema, Integer> dao = createDao(Schema.class, false);
		assertTrue(SchemaUtils.createSchema(dao) > 0);
	}

	@Test
	public void testCreateIfNotExists() throws SQLException {
		assertTrue(SchemaUtils.createSchemaIfNotExists(connectionSource, Schema.class) > 0);
		assertTrue(SchemaUtils.createSchemaIfNotExists(connectionSource, "schema") > 0);
	}

	@Test
	public void testCreateSchemaStatements() throws SQLException {
		assertTrue(SchemaUtils.getCreateSchemaStatements(new H2DatabaseType(), "schema").size() > 0);
	}

	@Test
	public void testDropSchema() throws SQLException {
		assertTrue(SchemaUtils.createSchema(connectionSource, Schema.class) > 0);
		assertTrue(SchemaUtils.dropSchema(connectionSource, Schema.class, true) > 0);
		assertTrue(SchemaUtils.createSchema(connectionSource, Schema.class) > 0);
		assertTrue(SchemaUtils.dropSchema(connectionSource, Schema.class, true) > 0);
		assertTrue(SchemaUtils.dropSchema(connectionSource, Schema.class, true) > 0);
	}

	@Test(expected = SQLException.class)
	public void testDropSchemaThrow() throws SQLException {
		SchemaUtils.dropSchema(connectionSource, Schema.class, false);
	}

	@DatabaseTable(tableName = "table", schemaName = "schema")
	protected static class Schema {
		@DatabaseField(generatedId = true)
		public int id;
		@DatabaseField
		public int val;

		public Schema() {
		}
	}
}
