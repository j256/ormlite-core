package com.j256.ormlite.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RawRowMapper;

public class RawResultsImplTest extends BaseCoreTest {

	@Test
	public void testQueryRaw() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		foo.val = 1;
		foo.equal = 10;
		assertEquals(1, dao.create(foo));
		QueryBuilder<Foo, Integer> qb = dao.queryBuilder();
		qb.where().eq(Foo.VAL_COLUMN_NAME, new SelectArg());
		GenericRawResults<String[]> rawResults = dao.queryRaw(qb.prepareStatementString(), Integer.toString(foo.val));
		List<String[]> results = rawResults.getResults();
		assertEquals(1, results.size());
		boolean found = false;
		String[] columnNames = rawResults.getColumnNames();
		for (int i = 0; i < rawResults.getNumberColumns(); i++) {
			if (columnNames[i].equalsIgnoreCase(Foo.ID_COLUMN_NAME)) {
				assertEquals(Integer.toString(foo.id), results.get(0)[0]);
				found = true;
			}
		}
		assertTrue(found);
	}

	@Test
	public void testQueryRawColumns() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		foo1.val = 1;
		foo1.equal = 10;
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		foo2.val = 10;
		foo2.equal = 5;
		assertEquals(1, dao.create(foo2));
		QueryBuilder<Foo, Integer> qb = dao.queryBuilder();
		qb.selectRaw("COUNT(*)");
		GenericRawResults<String[]> rawResults = dao.queryRaw(qb.prepareStatementString());
		List<String[]> results = rawResults.getResults();
		assertEquals(1, results.size());
		// 2 rows inserted
		assertEquals("2", results.get(0)[0]);

		qb = dao.queryBuilder();
		qb.selectRaw("MIN(val)", "MAX(val)");
		rawResults = dao.queryRaw(qb.prepareStatementString());
		results = rawResults.getResults();
		assertEquals(1, results.size());
		String[] result = results.get(0);
		assertEquals(2, result.length);
		// foo1 has the maximum value
		assertEquals(Integer.toString(foo1.val), result[0]);
		// foo2 has the maximum value
		assertEquals(Integer.toString(foo2.val), result[1]);
	}

	@Test
	public void testHaving() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);

		Foo foo = new Foo();
		int val1 = 243342;
		foo.val = val1;
		assertEquals(1, dao.create(foo));
		foo = new Foo();
		foo.val = val1;
		assertEquals(1, dao.create(foo));
		foo = new Foo();
		// only one of these
		int val2 = 6543;
		foo.val = val2;
		assertEquals(1, dao.create(foo));

		QueryBuilder<Foo, Integer> qb = dao.queryBuilder();
		qb.selectColumns(Foo.VAL_COLUMN_NAME);
		qb.groupBy(Foo.VAL_COLUMN_NAME);
		qb.having("COUNT(VAL) > 1");
		GenericRawResults<String[]> results = dao.queryRaw(qb.prepareStatementString());
		List<String[]> list = results.getResults();
		// only val2 has 2 of them
		assertEquals(1, list.size());
		assertEquals(String.valueOf(val1), list.get(0)[0]);

		qb.having("COUNT(VAL) > 2");
		results = dao.queryRaw(qb.prepareStatementString());
		list = results.getResults();
		assertEquals(0, list.size());
	}

	@Test
	public void testGetFirstResult() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		foo1.val = 342;
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		foo2.val = 9045342;
		assertEquals(1, dao.create(foo2));

		QueryBuilder<Foo, Integer> qb = dao.queryBuilder();
		qb.selectRaw("MAX(" + Foo.VAL_COLUMN_NAME + ")");
		GenericRawResults<String[]> results = dao.queryRaw(qb.prepareStatementString());
		String[] result = results.getFirstResult();
		int max = Integer.parseInt(result[0]);
		if (foo1.val > foo2.val) {
			assertEquals(foo1.val, max);
		} else {
			assertEquals(foo2.val, max);
		}
	}

	@Test
	public void testCustomColumnNames() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		foo.val = 1213213;
		assertEquals(1, dao.create(foo));
		final String idName = "SOME_ID";
		final String valName = "SOME_VAL";
		final AtomicBoolean gotResult = new AtomicBoolean(false);
		GenericRawResults<Object> results =
				dao.queryRaw("select id as " + idName + ", val as " + valName + " from foo",
						new RawRowMapper<Object>() {
							@Override
							public Object mapRow(String[] columnNames, String[] resultColumns) {
								assertEquals(idName, columnNames[0]);
								assertEquals(valName, columnNames[1]);
								gotResult.set(true);
								return new Object();
							}
						});
		assertEquals(1, results.getResults().size());
		results.close();
		assertTrue(gotResult.get());
	}
}
