package com.j256.ormlite.stmt.mapped;

import static org.easymock.EasyMock.createMock;

import java.util.ArrayList;

import org.junit.Test;

import com.j256.ormlite.db.BaseDatabaseType;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.TableInfo;

public class MappedDeleteCollectionTest {

	private final DatabaseType databaseType = new StubDatabaseType();
	
	@Test(expected = IllegalArgumentException.class)
	public void testNoIdBuildDelete() throws Exception {
		DatabaseConnection databaseConnection = createMock(DatabaseConnection.class);
		MappedDeleteCollection.deleteObjects(databaseType, new TableInfo<NoId>(databaseType, NoId.class),
				databaseConnection, new ArrayList<NoId>());
	}

	protected static class NoId {
		@DatabaseField
		String stuff;
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
