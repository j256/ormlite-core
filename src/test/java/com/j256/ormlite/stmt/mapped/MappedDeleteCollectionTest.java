package com.j256.ormlite.stmt.mapped;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.sql.SQLException;
import java.util.ArrayList;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.db.BaseDatabaseType;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.TableInfo;

public class MappedDeleteCollectionTest extends BaseCoreTest {

	private final DatabaseType databaseType = new StubDatabaseType();

	@Test(expected = SQLException.class)
	public void testNoIdBuildDelete() throws Exception {
		DatabaseConnection databaseConnection = createMock(DatabaseConnection.class);
		ConnectionSource connectionSource = createMock(ConnectionSource.class);
		expect(connectionSource.getDatabaseType()).andReturn(databaseType).anyTimes();
		replay(connectionSource);
		Dao<NoId, Void> dao = createDao(NoId.class, false);
		MappedDeleteCollection.deleteObjects(dao, new TableInfo<NoId, Void>(databaseType, NoId.class),
				databaseConnection, new ArrayList<NoId>(), null);
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
