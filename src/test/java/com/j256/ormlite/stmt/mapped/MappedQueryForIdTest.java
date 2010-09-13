package com.j256.ormlite.stmt.mapped;

import static org.junit.Assert.assertNull;

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

	@Test
	public void testNoIdBuildUpdater() throws Exception {
		assertNull(MappedUpdate.build(databaseType, new TableInfo<NoId>(databaseType, NoId.class)));
	}

	@Test
	public void testNoIdBuildQueryForId() throws Exception {
		assertNull(MappedQueryForId.build(databaseType, new TableInfo<NoId>(databaseType, NoId.class)));
	}

	protected static class NoId {
		@DatabaseField
		String id;
	}
}
