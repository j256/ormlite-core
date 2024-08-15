package com.j256.ormlite.stmt.mapped;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import java.sql.SQLException;

import org.junit.jupiter.api.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.db.BaseDatabaseType;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.stmt.StatementExecutor;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableInfo;

public class MappedDeleteTest extends BaseCoreTest {

	private final DatabaseType databaseType = new StubDatabaseType();
	private final ConnectionSource connectionSource;

	{
		connectionSource = createMock(ConnectionSource.class);
		expect(connectionSource.getDatabaseType()).andReturn(databaseType).anyTimes();
		replay(connectionSource);
	}

	@Test
	public void testDeleteNoId() throws Exception {
		StatementExecutor<NoId, Void> se = new StatementExecutor<NoId, Void>(databaseType,
				new TableInfo<NoId, Void>(databaseType, NoId.class), null);
		NoId noId = new NoId();
		noId.stuff = "1";
		ConnectionSource connectionSource = createMock(ConnectionSource.class);
		expect(connectionSource.getReadOnlyConnection(NOID_TABLE_NAME)).andReturn(null);
		replay(connectionSource);
		assertThrowsExactly(SQLException.class, () -> {
			se.delete(connectionSource.getReadOnlyConnection(NOID_TABLE_NAME), noId, null);
		});
	}

	@Test
	public void testNoIdBuildDelete() throws Exception {
		Dao<NoId, Void> dao = createDao(NoId.class, false);
		assertThrowsExactly(SQLException.class, () -> {
			MappedDelete.build(dao, new TableInfo<NoId, Void>(databaseType, NoId.class));
		});
	}

	private static class StubDatabaseType extends BaseDatabaseType {
		@Override
		protected String[] getDriverClassNames() {
			return new String[] { "foo.bar.baz" };
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
