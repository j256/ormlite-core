package com.j256.ormlite.stmt.mapped;

import java.sql.SQLException;

import org.junit.Test;

import com.j256.ormlite.BaseOrmLiteCoreTest;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.StatementExecutor;
import com.j256.ormlite.table.TableInfo;

public class MappedQueryForIdTest extends BaseOrmLiteCoreTest {

	@Test(expected = SQLException.class)
	public void testQueryNoId() throws Exception {
		StatementExecutor<NoId, String> se =
				new StatementExecutor<NoId, String>(databaseType, new TableInfo<NoId>(databaseType, NoId.class));
		se.queryForId(null, "1");
	}

	@Test(expected = SQLException.class)
	public void testNoIdBuildUpdater() throws Exception {
		MappedUpdate.build(databaseType, new TableInfo<NoId>(databaseType, NoId.class));
	}

	@Test(expected = SQLException.class)
	public void testNoIdBuildQueryForId() throws Exception {
		MappedQueryForId.build(databaseType, new TableInfo<NoId>(databaseType, NoId.class));
	}

	protected static class NoId {
		@DatabaseField
		String id;
	}
}
