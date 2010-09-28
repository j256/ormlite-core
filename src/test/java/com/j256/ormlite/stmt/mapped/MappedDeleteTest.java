package com.j256.ormlite.stmt.mapped;

import java.sql.SQLException;

import org.junit.Test;

import com.j256.ormlite.db.BaseDatabaseType;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.StatementExecutor;
import com.j256.ormlite.table.TableInfo;

public class MappedDeleteTest {

	private final DatabaseType databaseType = new StubDatabaseType();

	@Test(expected = SQLException.class)
	public void testDeleteNoId() throws Exception {
		StatementExecutor<NoId, String> se =
				new StatementExecutor<NoId, String>(databaseType, new TableInfo<NoId>(databaseType, NoId.class));
		NoId noId = new NoId();
		noId.stuff = "1";
		se.delete(null, noId);
	}

	@Test(expected = SQLException.class)
	public void testNoIdBuildDelete() throws Exception {
		MappedDelete.build(databaseType, new TableInfo<NoId>(databaseType, NoId.class));
	}

	protected static class NoId {
		@DatabaseField
		String stuff;
	}

	private class StubDatabaseType extends BaseDatabaseType {
		public String getDriverClassName() {
			return "foo.bar.baz";
		}
		public String getDriverUrlPart() {
			return "foo";
		}
	}
}
