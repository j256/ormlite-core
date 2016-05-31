package com.j256.ormlite.stmt.mapped;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import java.sql.SQLException;

import org.junit.Test;

import com.j256.ormlite.db.BaseDatabaseType;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.StatementExecutor;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableInfo;

public class MappedUpdateTest {

	private final DatabaseType databaseType = new StubDatabaseType();
	private final ConnectionSource connectionSource;

	{
		connectionSource = createMock(ConnectionSource.class);
		expect(connectionSource.getDatabaseType()).andReturn(databaseType).anyTimes();
		replay(connectionSource);
	}

	@Test(expected = SQLException.class)
	public void testUpdateNoId() throws Exception {
		StatementExecutor<NoId, String> se =
				new StatementExecutor<NoId, String>(databaseType, new TableInfo<NoId, String>(connectionSource, null,
						NoId.class), null);
		NoId noId = new NoId();
		noId.id = "1";
		se.update(null, noId, null);
	}

	@Test
	public void testUpdateJustId() throws Exception {
		StatementExecutor<JustId, Integer> se =
				new StatementExecutor<JustId, Integer>(databaseType, new TableInfo<JustId, Integer>(connectionSource,
						null, JustId.class), null);
		JustId justId = new JustId();
		justId.id = 1;
		assertEquals(0, se.update(null, justId, null));
	}

	@Test(expected = SQLException.class)
	public void testNoIdBuildUpdater() throws Exception {
		MappedUpdate.build(databaseType, new TableInfo<NoId, Void>(connectionSource, null, NoId.class));
	}

	@Test(expected = SQLException.class)
	public void testJustIdBuildUpdater() throws Exception {
		MappedUpdate.build(databaseType, new TableInfo<NoId, Void>(connectionSource, null, NoId.class));
	}

	protected static class NoId {
		@DatabaseField
		String id;
	}

	protected static class JustId {
		@DatabaseField(id = true)
		int id;
	}

	private static class StubDatabaseType extends BaseDatabaseType {
		@Override
		public String getDriverClassName() {
			return "foo.bar.baz";
		}
		@Override
		public String getDatabaseName() {
			return "fake";
		}
		@Override
		public boolean isDatabaseUrlThisType(String url, String dbTypePart) {
			return false;
		}
	}
}
