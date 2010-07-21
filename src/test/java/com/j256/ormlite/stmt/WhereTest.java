package com.j256.ormlite.stmt;

import static org.junit.Assert.assertTrue;

import java.sql.SQLException;

import org.junit.Test;

import com.j256.ormlite.db.H2DatabaseType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.query.Eq;
import com.j256.ormlite.table.TableInfo;

public class WhereTest {

	private final static String COLUMN_NAME = "foo";
	@Test
	public void testToString() throws Exception {
		Where where = new Where(createTableInfo());
		assertTrue(where.toString().contains("empty where clause"));
		String value = "bar";
		Eq eq = new Eq(COLUMN_NAME, true, value);
		where.eq(COLUMN_NAME, value);
		assertTrue(where.toString().contains(eq.toString()));
	}

	@Test(expected = IllegalStateException.class)
	public void testAlreadyNeedsClause() throws Exception {
		Where where = new Where(createTableInfo());
		where.eq(COLUMN_NAME, "bar");
		where.and();
		where.and();
	}

	private TableInfo<?> createTableInfo() throws SQLException {
		return new TableInfo<Foo>(new H2DatabaseType(), Foo.class);
	}

	protected static class Foo {
		@DatabaseField(columnName = COLUMN_NAME)
		int val;
		Foo() {
		}
	}
}
