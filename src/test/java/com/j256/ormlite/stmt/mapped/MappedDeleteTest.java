package com.j256.ormlite.stmt.mapped;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.sql.SQLException;

import org.junit.Test;

import com.j256.ormlite.db.BaseDatabaseType;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.StatementExecutor;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableInfo;

public class MappedDeleteTest {

	private final DatabaseType databaseType = new StubDatabaseType();
	private final ConnectionSource connectionSource;

	{
		connectionSource = createMock(ConnectionSource.class);
		expect(connectionSource.getDatabaseType()).andReturn(databaseType).anyTimes();
		replay(connectionSource);
	}

	@Test(expected = SQLException.class)
	public void testDeleteNoId() throws Exception {
		StatementExecutor<NoId, Void> se =
				new StatementExecutor<NoId, Void>(databaseType, new TableInfo<NoId, Void>(connectionSource, null,
						NoId.class), null);
		NoId noId = new NoId();
		noId.stuff = "1";
		se.delete(null, noId, null);
	}

	@Test(expected = SQLException.class)
	public void testNoIdBuildDelete() throws Exception {
		MappedDelete.build(databaseType, new TableInfo<NoId, Void>(connectionSource, null, NoId.class));
	}

	protected static class NoId {
		@DatabaseField
		String stuff;
	}

	private static class StubDatabaseType extends BaseDatabaseType {
		@Override
		public String getDriverClassName() {
			return "foo.bar.baz";
		}
		public String getDatabaseName() {
			return "fake";
		}
		public boolean isDatabaseUrlThisType(String url, String dbTypePart) {
			return false;
		}
	}
}
