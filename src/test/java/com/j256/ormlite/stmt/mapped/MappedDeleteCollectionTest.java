package com.j256.ormlite.stmt.mapped;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import java.sql.SQLException;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.db.BaseDatabaseType;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.TableInfo;

public class MappedDeleteCollectionTest extends BaseCoreTest {

	private final DatabaseType databaseType = new StubDatabaseType();

	@Test
	public void testNoIdBuildDelete() throws Exception {
		DatabaseConnection databaseConnection = createMock(DatabaseConnection.class);
		ConnectionSource connectionSource = createMock(ConnectionSource.class);
		expect(connectionSource.getDatabaseType()).andReturn(databaseType).anyTimes();
		replay(connectionSource);
		Dao<NoId, Void> dao = createDao(NoId.class, false);
		assertThrowsExactly(SQLException.class, () -> {
			MappedDeleteCollection.deleteObjects(dao, new TableInfo<NoId, Void>(databaseType, NoId.class),
					databaseConnection, new ArrayList<NoId>(), null);
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
