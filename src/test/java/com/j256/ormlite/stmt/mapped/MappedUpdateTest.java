package com.j256.ormlite.stmt.mapped;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

import java.sql.SQLException;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.db.BaseDatabaseType;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.StatementExecutor;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableInfo;

public class MappedUpdateTest extends BaseCoreTest {

	private final DatabaseType databaseType = new StubDatabaseType();
	private final ConnectionSource connectionSource;

	{
		connectionSource = createMock(ConnectionSource.class);
		expect(connectionSource.getDatabaseType()).andReturn(databaseType).anyTimes();
		replay(connectionSource);
	}

	@Test(expected = SQLException.class)
	public void testUpdateNoId() throws Exception {
		StatementExecutor<NoId, String> se = new StatementExecutor<NoId, String>(databaseType,
				new TableInfo<NoId, String>(databaseType, NoId.class), null);
		NoId noId = new NoId();
		noId.stuff = "1";
		se.update(null, noId, null);
	}

	@Test
	public void testUpdateJustId() throws Exception {
		Dao<JustId, Integer> dao = createDao(JustId.class, false);
		StatementExecutor<JustId, Integer> se = new StatementExecutor<JustId, Integer>(databaseType,
				new TableInfo<JustId, Integer>(databaseType, JustId.class), dao);
		JustId justId = new JustId();
		justId.id = 1;
		assertEquals(0, se.update(null, justId, null));
	}

	@Test(expected = SQLException.class)
	public void testNoIdBuildUpdater() throws Exception {
		Dao<NoId, Void> dao = createDao(NoId.class, false);
		MappedUpdate.build(dao, new TableInfo<NoId, Void>(databaseType, NoId.class));
	}

	@Test(expected = SQLException.class)
	public void testJustIdBuildUpdater() throws Exception {
		Dao<NoId, Void> dao = createDao(NoId.class, false);
		MappedUpdate.build(dao, new TableInfo<NoId, Void>(databaseType, NoId.class));
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
