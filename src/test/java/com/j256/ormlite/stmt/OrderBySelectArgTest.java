package com.j256.ormlite.stmt;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import java.util.List;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.table.DatabaseTable;

/**
 * @author gogos-venge
 */
public class OrderBySelectArgTest extends BaseCoreTest {

	private static final String COLUMN_NAME = "column";
	private static final String[] FRUITS = { "apple", "orange", "raspberry" };

	@Test
	public void testOrderBy() throws SQLException {

		Dao<TestObject, String> testDao = createDao(TestObject.class, true);

		createData(testDao);
		// show what's in the database
		System.out.println(testDao.queryForAll().toString());

		SelectArg selectArg = new SelectArg(SqlType.STRING, COLUMN_NAME);
		QueryBuilder<TestObject, String> qb = testDao.queryBuilder();
		
		// specify the column name as a raw SelectArg
		qb.selectColumns(COLUMN_NAME).groupBy(COLUMN_NAME).orderByRaw("? IS NULL ASC", selectArg);

		List<TestObject> results1 = qb.query();
		System.out.println(results1.toString());

		// reset for another test query
		qb.reset();

		// here we hardcode the column-name into the query
		qb.selectColumns(COLUMN_NAME).groupBy(COLUMN_NAME).orderByRaw("`" + COLUMN_NAME + "` IS NULL ASC");

		List<TestObject> results2 = qb.query();
		System.out.println(results2.toString());

		// when hardcoding the query, the result takes the correct form: apple orange raspberry null
		assertEquals(results1, results2);
	}

	private void createData(Dao<TestObject, String> dao) throws SQLException {
		/*
		 * Half of the new TestObjects will have the test column as null and half a specific string. The purpose of
		 * this, is to verify that "ORDER BY `testColumn` IS NULL ASC" will sort the null group at the end of the
		 * results.
		 */
		for (int i = 0; i < 5; i++) {
			// fruit word
			TestObject randomString = new TestObject();
			randomString.column = FRUITS[i % FRUITS.length];
			dao.create(randomString);

			// null value
			TestObject nullTest = new TestObject();
			nullTest.column = null;
			dao.create(nullTest);
		}
	}

	@DatabaseTable
	public static class TestObject {

		@DatabaseField(columnName = COLUMN_NAME)
		public String column;

		public TestObject() {
			// for ormlite
		}

		@Override
		public String toString() {
			return column;
		}

		@Override
		public int hashCode() {
			return ((column == null) ? 0 : column.hashCode());
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			TestObject other = (TestObject) obj;
			if (column == null) {
				return (other.column == null);
			} else {
				return column.equals(other.column);
			}
		}
	}
}
