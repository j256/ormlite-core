package com.j256.ormlite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.sql.SQLException;

import com.j256.ormlite.db.BaseDatabaseType;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.TableInfo;

public abstract class BaseCoreTest {

	protected final DatabaseType databaseType = new StubDatabaseType();
	protected final StubConnectionSource connectionSource = new StubConnectionSource();
	protected TableInfo<BaseFoo> baseFooTableInfo;
	protected final FieldType numberFieldType;
	protected final FieldType stringFieldType;
	protected final FieldType foreignFieldType;

	{
		try {
			Field field = BaseFoo.class.getDeclaredField("id");
			assertEquals(String.class, field.getType());
			stringFieldType = FieldType.createFieldType(databaseType, "BaseFoo", field);
			field = BaseFoo.class.getDeclaredField("val");
			assertEquals(int.class, field.getType());
			numberFieldType = FieldType.createFieldType(databaseType, "BaseFoo", field);
			field = Foreign.class.getDeclaredField("baseFoo");
			assertEquals(BaseFoo.class, field.getType());
			foreignFieldType = FieldType.createFieldType(databaseType, "BaseFoo", field);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

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

	protected class StubConnectionSource implements ConnectionSource {
		private DatabaseType databaseType = new StubDatabaseType();
		private DatabaseConnection databaseConnection;
		public DatabaseConnection getReadOnlyConnection() {
			return databaseConnection;
		}
		public DatabaseConnection getReadWriteConnection() {
			return databaseConnection;
		}
		public void releaseConnection(DatabaseConnection connection) {
		}
		public void close() throws SQLException {
		}
		public DatabaseType getDatabaseType() {
			return databaseType;
		}
		public void setDatabaseConnection(DatabaseConnection databaseConnection) {
			this.databaseConnection = databaseConnection;
		}
		public boolean saveSpecialConnection(DatabaseConnection connection) {
			return true;
		}
		public void clearSpecialConnection(DatabaseConnection connection) {
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

	protected class Foreign {
		@DatabaseField(foreign = true)
		public BaseFoo baseFoo;
		public Foreign() {
		}
	}
}
