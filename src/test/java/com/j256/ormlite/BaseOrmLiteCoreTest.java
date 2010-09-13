package com.j256.ormlite;

import static org.junit.Assert.fail;

import java.sql.SQLException;

import com.j256.ormlite.db.BaseDatabaseType;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.TableInfo;

public abstract class BaseOrmLiteCoreTest {

	protected final DatabaseType databaseType = new StubDatabaseType();
	protected TableInfo<BaseFoo> baseFooTableInfo;
	
	{
		try {
			baseFooTableInfo = new TableInfo<BaseFoo>(databaseType, BaseFoo.class);
		} catch (SQLException e) {
			fail("Constructing our base table info threw an exception");
		}
	}

	protected class StubDatabaseType extends BaseDatabaseType {
		public String getDriverClassName() {
			return "java.lang.String";
		}
		public String getDriverUrlPart() {
			return "foo";
		}
	}

	protected class LimitAfterSelectDatabaseType extends StubDatabaseType {
		public LimitAfterSelectDatabaseType() {
		}
		@Override
		public boolean isLimitAfterSelect() {
			return true;
		}
	}

	protected class NeedsSequenceDatabaseType extends StubDatabaseType {
		public NeedsSequenceDatabaseType() {
		}
		@Override
		public boolean isIdSequenceNeeded() {
			return true;
		}
	}

	protected static class BaseFoo {
		public static final String ID_COLUMN_NAME = "id";
		public static final String VAL_COLUMN_NAME = "val";
		public static final String EQUAL_COLUMN_NAME = "equal";
		public static final String NULL_COLUMN_NAME = "null";
		@DatabaseField(id = true, columnName = ID_COLUMN_NAME)
		public String id;
		@DatabaseField(columnName = VAL_COLUMN_NAME)
		public int val;
		@DatabaseField(columnName = EQUAL_COLUMN_NAME)
		public int equal;
		@DatabaseField(columnName = NULL_COLUMN_NAME)
		public String nullField;
		public BaseFoo() {
		}
		@Override
		public String toString() {
			return "Foo:" + id;
		}
		@Override
		public boolean equals(Object other) {
			if (other == null || other.getClass() != getClass())
				return false;
			return id.equals(((BaseFoo) other).id);
		}
	}
}
