package com.j256.ormlite;

import com.j256.ormlite.db.BaseDatabaseType;
import com.j256.ormlite.db.DatabaseType;

public class BaseOrmLiteCoreTest {

	protected final DatabaseType databaseType = new StubDatabaseType();

	protected class StubDatabaseType extends BaseDatabaseType {
		public String getDriverClassName() {
			return "java.lang.String";
		}
		public String getDriverUrlPart() {
			return "foo";
		}
	}

	protected class NeedsSequenceDatabaseType extends BaseDatabaseType {
		public NeedsSequenceDatabaseType() {
		}
		public String getDriverClassName() {
			return "foo.bar.baz";
		}
		public String getDriverUrlPart() {
			return "foo";
		}
		@Override
		public boolean isIdSequenceNeeded() {
			return true;
		}
	}
}
