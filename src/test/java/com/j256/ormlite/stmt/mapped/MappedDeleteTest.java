package com.j256.ormlite.stmt.mapped;

import static org.junit.Assert.assertNull;

import java.sql.SQLException;

import org.junit.Test;

import com.j256.ormlite.BaseOrmLiteTest;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.StatementExecutor;
import com.j256.ormlite.table.TableInfo;

public class MappedDeleteTest extends BaseOrmLiteTest {

	@Test(expected = SQLException.class)
	public void testDeleteNoId() throws Exception {
		StatementExecutor<NoId, String> se =
				new StatementExecutor<NoId, String>(databaseType, new TableInfo<NoId>(databaseType, NoId.class));
		NoId noId = new NoId();
		noId.stuff = "1";
		se.delete(null, noId);
	}

	@Test
	public void testNoIdBuildDelete() throws Exception {
		assertNull(MappedDelete.build(databaseType, new TableInfo<NoId>(databaseType, NoId.class)));
	}

	protected static class NoId {
		@DatabaseField
		String stuff;
	}
}
