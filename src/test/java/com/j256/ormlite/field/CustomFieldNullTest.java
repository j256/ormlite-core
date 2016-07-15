package com.j256.ormlite.field;

import java.sql.SQLException;
import java.util.List;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.types.StringType;
import com.j256.ormlite.support.DatabaseResults;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.TableUtils;

/**
 * Interesting test that checks for handling of null results in a customer persister. Thanks to Harshit Mittal.
 * 
 * See http://stackoverflow.com/questions/38371741/handle-null-case-inside-sqlargtojava-of-custom-persister-class
 */
public class CustomFieldNullTest extends BaseCoreTest {

	@Test
	public void testCustomNullHandler() throws SQLException {

		TableUtils.createTableIfNotExists(connectionSource, Foo.class);
		Dao<Foo, Long> dao = DaoManager.createDao(connectionSource, Foo.class);
		Foo foo = new Foo();
		dao.create(foo);

		List<Foo> results = dao.queryForAll();
		for (Foo result : results) {
			System.out.println("Result value = '" + result.getValue() + "', length = " + result.getValue().length());
		}

		dao.executeRaw("INSERT INTO a (id, value) VALUES (NULL, NULL)");

		results = dao.queryForAll();
		for (Foo result : results) {
			System.out.println("Result value = '" + result.getValue() + "', length = " + result.getValue().length());
		}

	}

	@DatabaseTable(tableName = "a")
	private static class Foo {

		@DatabaseField(generatedId = true, columnName = "id")
		private long id;

		@DatabaseField(columnName = "value", persisterClass = StringPropertyPersister.class)
		private final StringProperty value;

		protected Foo() {
			value = new SimpleStringProperty();
		}

		public String getValue() {
			return value.getValue();
		}
	}

	private static class StringPropertyPersister extends StringType {

		private static final StringPropertyPersister INSTANCE = new StringPropertyPersister();

		private StringPropertyPersister() {
			super(SqlType.STRING, new Class<?>[] { StringProperty.class });
		}

		public static StringPropertyPersister getSingleton() {
			return INSTANCE;
		}

		@Override
		public Object javaToSqlArg(FieldType fieldType, Object javaObject) {
			if (javaObject == null) {
				return "";
			} else {
				StringProperty property = (StringProperty) javaObject;
				return property.getValue();
			}
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
				return new SimpleStringProperty("");
			} else {
				return new SimpleStringProperty((String) sqlArg);
			}
		}
	}

	private static interface StringProperty {
		public String getValue();
	}

	private static class SimpleStringProperty implements StringProperty {
		String value;

		public SimpleStringProperty() {
			this.value = null;
		}

		public SimpleStringProperty(String value) {
			this.value = value;
		}

		@Override
		public String getValue() {
			return value;
		}
	}
}
