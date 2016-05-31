package com.j256.ormlite.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.DatabaseResults;

public class RawRowMapperTest extends BaseCoreTest {

	@Test
	public void testBasic() throws Exception {
		Dao<Foo, Object> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		foo1.val = 12321;
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		foo2.val = 754282321;
		assertEquals(1, dao.create(foo2));

		QueryBuilder<Foo, Object> qb = dao.queryBuilder();
		qb.selectColumns(Foo.VAL_COLUMN_NAME);
		GenericRawResults<Integer> rawResults = dao.queryRaw(qb.prepareStatementString(), new IntMapper());
		List<Integer> results = rawResults.getResults();
		assertEquals(2, results.size());
		assertEquals(foo1.val, (int) results.get(0));
		assertEquals(foo2.val, (int) results.get(1));
	}

	@Test
	public void testFooMapper() throws Exception {
		Dao<Foo, Object> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		foo1.val = 12321;
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		foo2.val = 754282321;
		assertEquals(1, dao.create(foo2));

		QueryBuilder<Foo, Object> qb = dao.queryBuilder();
		qb.selectColumns(Foo.ID_COLUMN_NAME, Foo.VAL_COLUMN_NAME);
		GenericRawResults<Foo> rawResults = dao.queryRaw(qb.prepareStatementString(), new FooMapper());
		List<Foo> results = rawResults.getResults();
		assertEquals(2, results.size());
		assertEquals(foo1.id, results.get(0).id);
		assertEquals(foo1.val, results.get(0).val);
		assertEquals(foo2.id, results.get(1).id);
		assertEquals(foo2.val, results.get(1).val);
	}

	@Test
	public void testRawResultsMapper() throws Exception {
		Dao<Foo, Object> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		foo1.val = 12321;
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		foo2.val = 754282321;
		assertEquals(1, dao.create(foo2));

		QueryBuilder<Foo, Object> qb = dao.queryBuilder();
		qb.selectColumns(Foo.ID_COLUMN_NAME, Foo.VAL_COLUMN_NAME);
		CloseableIterator<Foo> iterator = dao.iterator();
		try {
			DatabaseResults results = iterator.getRawResults();
			for (int count = 0; results.next(); count++) {
				Foo foo = dao.mapSelectStarRow(results);
				switch (count) {
					case 0 :
						assertEquals(foo1.id, foo.id);
						assertEquals(foo1.val, foo.val);
						break;
					case 1 :
						assertEquals(foo2.id, foo.id);
						assertEquals(foo2.val, foo.val);
						break;
					default :
						fail("Unknown entry in list");
				}
			}
		} finally {
			iterator.close();
		}
	}

	private static class IntMapper implements RawRowMapper<Integer> {
		@Override
		public Integer mapRow(String[] columnNames, String[] resultColumns) {
			// may be more than 1 because of the id
			assertTrue(resultColumns.length >= 1);
			// id is added at the end always
			return Integer.parseInt(resultColumns[0]);
		}
	}

	private static class FooMapper implements RawRowMapper<Foo> {
		@Override
		public Foo mapRow(String[] columnNames, String[] resultColumns) {
			// may be more than 1 because of the id
			assertTrue(resultColumns.length >= 1);
			// id is added at the end always
			Foo foo = new Foo();
			foo.id = Integer.parseInt(resultColumns[0]);
			foo.val = Integer.parseInt(resultColumns[1]);
			// other fields could be converted here
			return foo;
		}
	}
}
