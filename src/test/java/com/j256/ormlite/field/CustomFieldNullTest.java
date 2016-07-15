package com.j256.ormlite.field;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import java.util.List;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.types.StringType;
import com.j256.ormlite.support.DatabaseResults;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Interesting test that checks for handling of null results in a customer persister. Thanks to Harshit Mittal.
 * 
 * See http://stackoverflow.com/questions/38371741/handle-null-case-inside-sqlargtojava-of-custom-persister-class
 */
public class CustomFieldNullTest extends BaseCoreTest {

	private static final String FOO_TABLE_NAME = "foo";

	@Test
	public void testCustomNullHandler() throws Exception {

		Dao<Foo, Long> dao = createDao(Foo.class, true);

		Foo foo = new Foo();
		assertEquals(1, dao.create(foo));

		List<Foo> results = dao.queryForAll();
		for (Foo result : results) {
			System.out.println("Result value = '" + result.getValue() + "', length = " + result.getValue().length());
		}

		// try to use insert nulls directly
		dao.executeRaw("INSERT INTO " + FOO_TABLE_NAME + " (" + Foo.ID_FIELD + "," + Foo.VALUE_FIELD
				+ ") VALUES (NULL, NULL)");

		results = dao.queryForAll();
		for (Foo result : results) {
			System.out.println("Result value = '" + result.getValue() + "', length = " + result.getValue().length());
		}

	}

	@DatabaseTable(tableName = FOO_TABLE_NAME)
	private static class Foo {

		public static final String ID_FIELD = "id";
		public static final String VALUE_FIELD = "value";

		@DatabaseField(generatedId = true, columnName = ID_FIELD)
		private long id;

		@DatabaseField(columnName = VALUE_FIELD, persisterClass = SimplePropertyPersister.class, defaultValue = "")
		private final SimpleProperty value;

		protected Foo() {
			value = new SimpleProperty();
		}

		public String getValue() {
			return value.getValue();
		}
	}

	/**
	 * Customer persister.
	 */
	private static class SimplePropertyPersister extends StringType {

		private static final SimplePropertyPersister INSTANCE = new SimplePropertyPersister();

		private SimplePropertyPersister() {
			super(SqlType.STRING, new Class<?>[] { SimpleProperty.class });
		}

		public static SimplePropertyPersister getSingleton() {
			return INSTANCE;
		}
		
		@Override
		public Object javaToSqlArg(FieldType fieldType, Object javaObject) {
			SimpleProperty property = (SimpleProperty) javaObject;
			return property.getValue();
		}

		@Override
		public Object resultToJava(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
			// we override this here because the base class does the null comparison
			Object value = resultToSqlArg(fieldType, results, columnPos);
			return sqlArgToJava(fieldType, value, columnPos);
		}

		@Override
		public boolean isStreamType() {
			// this forces the null check to be ignored
			return true;
		}

		@Override
		public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) {
			if (sqlArg == null) {
				return new SimpleProperty("");
			} else {
				return new SimpleProperty((String) sqlArg);
			}
		}
	}

	private static class SimpleProperty {
		String value;

		public SimpleProperty() {
			this.value = null;
		}

		public SimpleProperty(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}
}
