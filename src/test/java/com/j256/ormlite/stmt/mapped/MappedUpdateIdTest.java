package com.j256.ormlite.stmt.mapped;

import java.sql.SQLException;

import org.junit.Test;

import com.j256.ormlite.db.BaseDatabaseType;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.TableInfo;

public class MappedUpdateIdTest {

	private final DatabaseType databaseType = new StubDatabaseType();

	@Test(expected = SQLException.class)
	public void testUpdateIdNoId() throws Exception {
		MappedUpdateId.build(databaseType, new TableInfo<NoId>(databaseType, NoId.class));
	}

	protected static class NoId {
		@DatabaseField
		String id;
	}

	private class StubDatabaseType extends BaseDatabaseType {
		public String getDriverClassName() {
			return "foo.bar.baz";
		}
		public boolean isDatabaseUrlThisType(String url, String dbTypePart) {
			return false;
		}
	}
}
