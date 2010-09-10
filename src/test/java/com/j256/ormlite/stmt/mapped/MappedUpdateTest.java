package com.j256.ormlite.stmt.mapped;

import static org.junit.Assert.assertNull;

import java.sql.SQLException;

import org.junit.Test;

import com.j256.ormlite.db.BaseDatabaseType;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.StatementExecutor;
import com.j256.ormlite.table.TableInfo;

public class MappedUpdateTest {

	private final DatabaseType databaseType = new StubDatabaseType();

	@Test(expected = SQLException.class)
	public void testUpdateNoId() throws Exception {
		StatementExecutor<NoId, String> se =
				new StatementExecutor<NoId, String>(databaseType, new TableInfo<NoId>(databaseType, NoId.class));
		NoId noId = new NoId();
		noId.id = "1";
		se.update(null, noId);
	}

	@Test
	public void testNoIdBuildUpdater() throws Exception {
		assertNull(MappedUpdate.build(databaseType, new TableInfo<NoId>(databaseType, NoId.class)));
	}

	@Test
	public void testJustIdBuildUpdater() throws Exception {
		assertNull(MappedUpdate.build(databaseType, new TableInfo<NoId>(databaseType, NoId.class)));
	}

	protected static class NoId {
		@DatabaseField
		String id;
	}

	protected static class JustId {
		@DatabaseField(generatedId = true)
		int id;
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
