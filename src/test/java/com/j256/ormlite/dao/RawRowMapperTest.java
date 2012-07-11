package com.j256.ormlite.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.stmt.QueryBuilder;

public class RawRowMapperTest extends BaseCoreTest {

	@Test
	public void test() throws Exception {
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

	private static class IntMapper implements RawRowMapper<Integer> {
		public Integer mapRow(String[] columnNames, String[] resultColumns) {
			// may be more than 1 because of the id
			assertTrue(resultColumns.length >= 1);
			// id is added at the end always
			return Integer.parseInt(resultColumns[0]);
		}
	}
}
