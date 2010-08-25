package com.j256.ormlite.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.j256.ormlite.BaseOrmLiteTest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;

public class QueryBuilderTest extends BaseOrmLiteTest {

	private final static String ID_PREFIX = "id";
	private final static int LOW_VAL = 21114;
	private final static int HIGH_VAL = LOW_VAL + 499494;
	private final static int EQUAL_VAL = 21312312;
	private Foo foo1;
	private Foo foo2;

	@Test
	public void testAnd() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		StatementBuilder<Foo, String> qb = fooDao.statementBuilder();

		// test And + Eq
		qb.where().eq(Foo.ID_COLUMN_NAME, foo1.id).and().eq(Foo.VAL_COLUMN_NAME, foo1.val);
		List<Foo> results = fooDao.query(qb.prepareStatement());
		assertEquals(1, results.size());
		assertEquals(foo1, results.get(0));

		// test And + Eq not inline
		Where where = qb.where();
		where.eq(Foo.ID_COLUMN_NAME, foo2.id);
		where.and();
		where.eq(Foo.VAL_COLUMN_NAME, foo2.val);
		results = fooDao.query(qb.prepareStatement());
		assertEquals(1, results.size());
		assertEquals(foo2, results.get(0));

		// test And double args
		where = qb.where();
		where.and(where.eq(Foo.ID_COLUMN_NAME, foo1.id), where.eq(Foo.VAL_COLUMN_NAME, foo1.val));
		results = fooDao.query(qb.prepareStatement());
		assertEquals(1, results.size());
		assertEquals(foo1, results.get(0));
	}

	@Test
	public void testOr() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		StatementBuilder<Foo, String> qb = fooDao.statementBuilder();

		// test Or + Eq
		qb.where().eq(Foo.ID_COLUMN_NAME, foo1.id).or().eq(Foo.VAL_COLUMN_NAME, foo1.val);
		List<Foo> results = fooDao.query(qb.prepareStatement());
		assertEquals(1, results.size());
		assertEquals(foo1, results.get(0));

		// test Or + Eq not inline
		Where where = qb.where();
		where.eq(Foo.ID_COLUMN_NAME, foo2.id);
		where.or();
		where.eq(Foo.VAL_COLUMN_NAME, foo2.val);
		results = fooDao.query(qb.prepareStatement());
		assertEquals(1, results.size());
		assertEquals(foo2, results.get(0));

		// test Or of ands
		where = qb.where();
		where.or(where.and(where.eq(Foo.ID_COLUMN_NAME, foo1.id), where.eq(Foo.VAL_COLUMN_NAME, foo1.val)), where.eq(
				Foo.ID_COLUMN_NAME, foo2.id).and().eq(Foo.VAL_COLUMN_NAME, foo2.val));
		results = fooDao.query(qb.prepareStatement());
		assertEquals(2, results.size());
		assertEquals(foo1, results.get(0));
		assertEquals(foo2, results.get(1));
	}

	@Test
	public void testSelectArgs() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		StatementBuilder<Foo, String> qb = fooDao.statementBuilder();

		SelectArg idSelectArg = new SelectArg();
		qb.where().eq(Foo.ID_COLUMN_NAME, idSelectArg);
		PreparedStmt<Foo> preparedQuery = qb.prepareStatement();

		idSelectArg.setValue(foo1.id);
		List<Foo> results = fooDao.query(preparedQuery);
		assertEquals(1, results.size());
		assertEquals(foo1, results.get(0));

		idSelectArg.setValue(foo2.id);
		results = fooDao.query(qb.prepareStatement());
		assertEquals(1, results.size());
		assertEquals(foo2, results.get(0));
	}

	@Test
	public void testLike() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		StatementBuilder<Foo, String> qb = fooDao.statementBuilder();

		qb.where().like(Foo.ID_COLUMN_NAME, ID_PREFIX + "%");
		List<Foo> results = fooDao.query(qb.prepareStatement());
		assertEquals(2, results.size());
		assertEquals(foo1, results.get(0));
		assertEquals(foo2, results.get(1));
	}

	@Test
	@ExpectedBehavior(expected = SQLException.class)
	public void testSelectArgsNotSet() throws Exception {

		Dao<Foo, String> fooDao = createTestData();
		StatementBuilder<Foo, String> qb = fooDao.statementBuilder();

		SelectArg idSelectArg = new SelectArg();
		qb.where().eq(Foo.ID_COLUMN_NAME, idSelectArg);
		fooDao.query(qb.prepareStatement());
	}

	@Test
	public void testSelectNot() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		StatementBuilder<Foo, String> qb = fooDao.statementBuilder();

		qb.where().not().eq(Foo.ID_COLUMN_NAME, foo1.id);
		List<Foo> results = fooDao.query(qb.prepareStatement());
		assertEquals(1, results.size());
		assertEquals(foo2, results.get(0));
	}

	@Test
	public void testIn() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		StatementBuilder<Foo, String> qb = fooDao.statementBuilder();

		qb.where().in(Foo.ID_COLUMN_NAME, foo1.id, foo2.id);
		List<Foo> results = fooDao.query(qb.prepareStatement());
		assertEquals(2, results.size());
		assertEquals(foo1, results.get(0));
		assertEquals(foo2, results.get(1));
	}

	@Test
	public void testInIterable() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		StatementBuilder<Foo, String> qb = fooDao.statementBuilder();

		qb.where().in(Foo.ID_COLUMN_NAME, Arrays.asList(foo1.id, foo2.id));
		List<Foo> results = fooDao.query(qb.prepareStatement());
		assertEquals(2, results.size());
		assertEquals(foo1, results.get(0));
		assertEquals(foo2, results.get(1));
	}

	@Test
	public void testNotIn() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		StatementBuilder<Foo, String> qb = fooDao.statementBuilder();

		qb.where().not().in(Foo.ID_COLUMN_NAME, foo1.id);
		List<Foo> results = fooDao.query(qb.prepareStatement());
		assertEquals(1, results.size());
		assertEquals(foo2, results.get(0));
	}

	@Test
	@ExpectedBehavior(expected = IllegalArgumentException.class)
	public void testNotBad() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		StatementBuilder<Foo, String> qb = fooDao.statementBuilder();
		qb.where().not();
		fooDao.query(qb.prepareStatement());
	}

	@Test
	@ExpectedBehavior(expected = IllegalArgumentException.class)
	public void testNotNotComparison() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		StatementBuilder<Foo, String> qb = fooDao.statementBuilder();
		Where where = qb.where();
		where.not(where.and(where.eq(Foo.ID_COLUMN_NAME, foo1.id), where.eq(Foo.ID_COLUMN_NAME, foo1.id)));
		fooDao.query(qb.prepareStatement());
	}

	@Test
	public void testNotArg() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		StatementBuilder<Foo, String> qb = fooDao.statementBuilder();
		Where where = qb.where();
		where.not(where.eq(Foo.ID_COLUMN_NAME, foo1.id));
		List<Foo> results = fooDao.query(qb.prepareStatement());
		assertEquals(1, results.size());
		assertEquals(foo2, results.get(0));
	}

	@Test
	@ExpectedBehavior(expected = IllegalStateException.class)
	public void testNoWhereOperations() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		StatementBuilder<Foo, String> qb = fooDao.statementBuilder();
		qb.where();
		fooDao.query(qb.prepareStatement());
	}

	@Test
	@ExpectedBehavior(expected = IllegalStateException.class)
	public void testMissingAnd() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		StatementBuilder<Foo, String> qb = fooDao.statementBuilder();
		qb.where().eq(Foo.ID_COLUMN_NAME, foo1.id).eq(Foo.ID_COLUMN_NAME, foo1.id);
		fooDao.query(qb.prepareStatement());
	}

	@Test
	@ExpectedBehavior(expected = IllegalStateException.class)
	public void testMissingAndArg() throws Exception {
		Dao<Foo, String> fooDao = createDao(Foo.class, false);
		StatementBuilder<Foo, String> qb = fooDao.statementBuilder();
		qb.where().and();
	}

	@Test
	public void testBetween() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		StatementBuilder<Foo, String> qb = fooDao.statementBuilder();

		qb.where().between(Foo.VAL_COLUMN_NAME, LOW_VAL, HIGH_VAL);
		List<Foo> results = fooDao.query(qb.prepareStatement());
		assertEquals(2, results.size());
		assertEquals(foo1, results.get(0));
		assertEquals(foo2, results.get(1));

		qb.where().between(Foo.VAL_COLUMN_NAME, LOW_VAL + 1, HIGH_VAL);
		results = fooDao.query(qb.prepareStatement());
		assertEquals(1, results.size());
		assertEquals(foo2, results.get(0));

		qb.where().between(Foo.VAL_COLUMN_NAME, LOW_VAL, HIGH_VAL - 1);
		results = fooDao.query(qb.prepareStatement());
		assertEquals(1, results.size());
		assertEquals(foo1, results.get(0));

		qb.where().between(Foo.VAL_COLUMN_NAME, LOW_VAL + 1, HIGH_VAL - 1);
		results = fooDao.query(qb.prepareStatement());
		assertEquals(0, results.size());
	}

	@Test
	public void testBetweenSelectArg() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		StatementBuilder<Foo, String> qb = fooDao.statementBuilder();

		SelectArg lowSelectArg = new SelectArg();
		qb.where().between(Foo.VAL_COLUMN_NAME, lowSelectArg, HIGH_VAL);
		lowSelectArg.setValue(LOW_VAL);
		List<Foo> results = fooDao.query(qb.prepareStatement());
		assertEquals(2, results.size());
		assertEquals(foo1, results.get(0));
		assertEquals(foo2, results.get(1));

		SelectArg highSelectArg = new SelectArg();
		lowSelectArg.setValue(LOW_VAL + 1);
		highSelectArg.setValue(HIGH_VAL);
		qb.where().between(Foo.VAL_COLUMN_NAME, lowSelectArg, highSelectArg);
		results = fooDao.query(qb.prepareStatement());
		assertEquals(1, results.size());
		assertEquals(foo2, results.get(0));
	}

	@Test
	public void testBetweenStrings() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		StatementBuilder<Foo, String> qb = fooDao.statementBuilder();

		String low = ID_PREFIX;
		String high = ID_PREFIX + "99999";
		qb.where().between(Foo.ID_COLUMN_NAME, low, high);
		List<Foo> results = fooDao.query(qb.prepareStatement());
		assertEquals(2, results.size());
		assertEquals(foo1, results.get(0));
		assertEquals(foo2, results.get(1));
	}

	@Test
	public void testLtGtEtc() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		StatementBuilder<Foo, String> qb = fooDao.statementBuilder();

		qb.where().eq(Foo.VAL_COLUMN_NAME, foo1.val);
		List<Foo> results = fooDao.query(qb.prepareStatement());
		assertEquals(1, results.size());
		assertEquals(foo1, results.get(0));

		qb.where().ge(Foo.VAL_COLUMN_NAME, foo1.val);
		results = fooDao.query(qb.prepareStatement());
		assertEquals(2, results.size());
		assertEquals(foo1, results.get(0));
		assertEquals(foo2, results.get(1));

		qb.where().ge(Foo.VAL_COLUMN_NAME, foo1.val - 1);
		results = fooDao.query(qb.prepareStatement());
		assertEquals(2, results.size());
		assertEquals(foo1, results.get(0));
		assertEquals(foo2, results.get(1));

		qb.where().ge(Foo.VAL_COLUMN_NAME, foo2.val);
		results = fooDao.query(qb.prepareStatement());
		assertEquals(1, results.size());
		assertEquals(foo2, results.get(0));

		qb.where().gt(Foo.VAL_COLUMN_NAME, foo1.val);
		results = fooDao.query(qb.prepareStatement());
		assertEquals(1, results.size());
		assertEquals(foo2, results.get(0));

		qb.where().gt(Foo.VAL_COLUMN_NAME, foo1.val - 1);
		results = fooDao.query(qb.prepareStatement());
		assertEquals(2, results.size());
		assertEquals(foo1, results.get(0));
		assertEquals(foo2, results.get(1));

		qb.where().gt(Foo.VAL_COLUMN_NAME, foo2.val);
		results = fooDao.query(qb.prepareStatement());
		assertEquals(0, results.size());

		qb.where().le(Foo.VAL_COLUMN_NAME, foo1.val);
		results = fooDao.query(qb.prepareStatement());
		assertEquals(1, results.size());
		assertEquals(foo1, results.get(0));

		qb.where().le(Foo.VAL_COLUMN_NAME, foo1.val - 1);
		results = fooDao.query(qb.prepareStatement());
		assertEquals(0, results.size());

		qb.where().lt(Foo.VAL_COLUMN_NAME, foo1.val);
		results = fooDao.query(qb.prepareStatement());
		assertEquals(0, results.size());

		qb.where().lt(Foo.VAL_COLUMN_NAME, foo1.val + 1);
		results = fooDao.query(qb.prepareStatement());
		assertEquals(1, results.size());
		assertEquals(foo1, results.get(0));

		qb.where().ne(Foo.VAL_COLUMN_NAME, foo1.val);
		results = fooDao.query(qb.prepareStatement());
		assertEquals(1, results.size());
		assertEquals(foo2, results.get(0));

		qb.where().ne(Foo.VAL_COLUMN_NAME, foo1.val).and().ne(Foo.VAL_COLUMN_NAME, foo2.val);
		results = fooDao.query(qb.prepareStatement());
		assertEquals(0, results.size());
	}

	@Test
	public void testPartialQueryAllRetrieval() throws Exception {
		Dao<PartialData, Integer> partialDao = createDao(PartialData.class, true);
		List<String> firsts = new ArrayList<String>();
		List<String> lasts = new ArrayList<String>();
		List<Integer> ids = new ArrayList<Integer>();

		createPartial(partialDao, ids, firsts, lasts, "bill", "rambo");
		createPartial(partialDao, ids, firsts, lasts, "zippy", "dingo");
		createPartial(partialDao, ids, firsts, lasts, "crappy", "bladdero");
		checkPartialList(partialDao.queryForAll(), ids, firsts, lasts, false, false);

		Set<String> columnNames = new HashSet<String>();
		StatementBuilder<PartialData, Integer> qb = partialDao.statementBuilder();
		qb.columns(columnNames);
		List<PartialData> partialList = partialDao.query(qb.prepareStatement());
		checkPartialList(partialList, ids, firsts, lasts, true, true);

		columnNames = new HashSet<String>();
		columnNames.add(PartialData.FIRST_FIELD_NAME);
		qb.columns(columnNames);
		partialList = partialDao.query(qb.prepareStatement());
		checkPartialList(partialList, ids, firsts, lasts, false, true);

		columnNames = new HashSet<String>();
		columnNames.add(PartialData.LAST_FIELD_NAME);
		qb.columns(columnNames);
		partialList = partialDao.query(qb.prepareStatement());
		checkPartialList(partialList, ids, firsts, lasts, false, false);

		for (PartialData partialData : partialDao) {
			assertEquals(1, partialDao.delete(partialData));
		}
		assertEquals(0, partialDao.queryForAll().size());
	}

	@Test
	public void testPartialIteratorRetrieval() throws Exception {
		Dao<PartialData, Integer> partialDao = createDao(PartialData.class, true);
		List<String> firsts = new ArrayList<String>();
		List<String> lasts = new ArrayList<String>();
		List<Integer> ids = new ArrayList<Integer>();

		createPartial(partialDao, ids, firsts, lasts, "bill", "rambo");
		createPartial(partialDao, ids, firsts, lasts, "zippy", "dingo");
		createPartial(partialDao, ids, firsts, lasts, "crappy", "bladdero");
		checkPartialList(partialDao.queryForAll(), ids, firsts, lasts, false, false);
		checkPartialIterator(partialDao.iterator(), ids, firsts, lasts, false, false);

		Set<String> columnNames = new HashSet<String>();
		StatementBuilder<PartialData, Integer> qb = partialDao.statementBuilder();
		qb.columns(columnNames);
		Iterator<PartialData> iterator = partialDao.iterator(qb.prepareStatement());
		checkPartialIterator(iterator, ids, firsts, lasts, true, true);

		columnNames = new HashSet<String>();
		columnNames.add(PartialData.FIRST_FIELD_NAME);
		qb.columns(columnNames);
		iterator = partialDao.iterator(qb.prepareStatement());
		checkPartialIterator(iterator, ids, firsts, lasts, false, true);

		columnNames = new HashSet<String>();
		columnNames.add(PartialData.LAST_FIELD_NAME);
		qb.columns(columnNames);
		iterator = partialDao.iterator(qb.prepareStatement());
		checkPartialIterator(iterator, ids, firsts, lasts, false, false);

		for (PartialData partialData : partialDao) {
			assertEquals(1, partialDao.delete(partialData));
		}
		assertEquals(0, partialDao.queryForAll().size());
	}

	@Test
	public void testIteratorCustomQuery() throws Exception {
		Dao<PartialData, Integer> partialDao = createDao(PartialData.class, true);
		List<String> firsts = new ArrayList<String>();
		List<String> lasts = new ArrayList<String>();
		List<Integer> ids = new ArrayList<Integer>();

		String firstFirst = "bill";
		createPartial(partialDao, ids, firsts, lasts, firstFirst, "rambo");
		createPartial(partialDao, ids, firsts, lasts, "zippy", "dingo");
		createPartial(partialDao, ids, firsts, lasts, "crappy", "bladdero");
		checkPartialList(partialDao.queryForAll(), ids, firsts, lasts, false, false);
		checkPartialIterator(partialDao.iterator(), ids, firsts, lasts, false, false);

		StatementBuilder<PartialData, Integer> qb = partialDao.statementBuilder();
		qb.where().eq(PartialData.FIRST_FIELD_NAME, firstFirst);
		Iterator<PartialData> iterator = partialDao.iterator(qb.prepareStatement());
		assertTrue(iterator.hasNext());
		assertEquals(firstFirst, iterator.next().first);
		assertFalse(iterator.hasNext());

		SelectArg firstArg = new SelectArg();
		qb.where().eq(PartialData.FIRST_FIELD_NAME, firstArg);
		firstArg.setValue(firstFirst);
		iterator = partialDao.iterator(qb.prepareStatement());
		assertTrue(iterator.hasNext());
		assertEquals(firstFirst, iterator.next().first);
		assertFalse(iterator.hasNext());
	}

	@Test
	@ExpectedBehavior(expected = IllegalArgumentException.class)
	public void testUnknownColumn() throws Exception {
		Dao<Foo, String> fooDao = createDao(Foo.class, false);
		StatementBuilder<Foo, String> qb = fooDao.statementBuilder();
		qb.columns("unknown column");
	}

	@Test
	public void testOrderBy() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		StatementBuilder<Foo, String> qb = fooDao.statementBuilder();
		qb.orderBy(Foo.VAL_COLUMN_NAME, true);
		List<Foo> results = fooDao.query(qb.prepareStatement());
		assertEquals(2, results.size());
		assertEquals(foo1, results.get(0));
		assertEquals(foo2, results.get(1));

		qb = fooDao.statementBuilder();;
		qb.orderBy(Foo.VAL_COLUMN_NAME, false);
		results = fooDao.query(qb.prepareStatement());
		assertEquals(2, results.size());
		assertEquals(foo2, results.get(0));
		assertEquals(foo1, results.get(1));

		// should be the same order
		qb = fooDao.statementBuilder();;
		qb.orderBy(Foo.EQUAL_COLUMN_NAME, false);
		qb.orderBy(Foo.VAL_COLUMN_NAME, false);
		results = fooDao.query(qb.prepareStatement());
		assertEquals(2, results.size());
		assertEquals(foo2, results.get(0));
		assertEquals(foo1, results.get(1));
	}

	@Test
	public void testGroupBy() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		StatementBuilder<Foo, String> qb = fooDao.statementBuilder();
		qb.columns(Foo.EQUAL_COLUMN_NAME);
		qb.groupBy(Foo.EQUAL_COLUMN_NAME);
		List<Foo> results = fooDao.query(qb.prepareStatement());
		assertEquals(1, results.size());
		assertEquals(EQUAL_VAL, results.get(0).equal);
		assertNull(results.get(0).id);
	}

	@Test
	public void testGroupAndOrderBy() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		StatementBuilder<Foo, String> qb = fooDao.statementBuilder();
		qb.columns(Foo.EQUAL_COLUMN_NAME, Foo.ID_COLUMN_NAME);
		qb.groupBy(Foo.EQUAL_COLUMN_NAME);
		qb.groupBy(Foo.ID_COLUMN_NAME);
		// get strange order otherwise
		qb.orderBy(Foo.ID_COLUMN_NAME, true);
		List<Foo> results = fooDao.query(qb.prepareStatement());
		assertEquals(2, results.size());
		assertEquals(foo1, results.get(0));
		assertEquals(foo2, results.get(1));
	}

	@Test
	public void testLimit() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		StatementBuilder<Foo, String> qb = fooDao.statementBuilder();
		// no limit the default
		List<Foo> results = fooDao.query(qb.prepareStatement());
		assertEquals(2, results.size());
		assertEquals(foo1, results.get(0));
		assertEquals(foo2, results.get(1));
		qb.limit(1);
		results = fooDao.query(qb.prepareStatement());
		assertEquals(1, results.size());
		assertEquals(foo1, results.get(0));
		// set back to no-limit
		qb.limit(null);
		results = fooDao.query(qb.prepareStatement());
		assertEquals(2, results.size());
		assertEquals(foo1, results.get(0));
		assertEquals(foo2, results.get(1));
	}

	@Test
	public void testLimitDoublePrepare() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		StatementBuilder<Foo, String> qb = fooDao.statementBuilder();
		// no limit the default
		List<Foo> results = fooDao.query(qb.prepareStatement());
		assertEquals(2, results.size());
		assertEquals(foo1, results.get(0));
		assertEquals(foo2, results.get(1));
		qb.limit(1);
		results = fooDao.query(qb.prepareStatement());
		assertEquals(1, results.size());
		assertEquals(foo1, results.get(0));
		results = fooDao.query(qb.prepareStatement());
		assertEquals(1, results.size());
		assertEquals(foo1, results.get(0));
	}

	@Test
	public void testLimitAfterSelect() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		StatementBuilder<Foo, String> qb = fooDao.statementBuilder();
		// no limit the default
		List<Foo> results = fooDao.query(qb.prepareStatement());
		assertEquals(2, results.size());
		assertEquals(foo1, results.get(0));
		assertEquals(foo2, results.get(1));
		qb.limit(1);
		results = fooDao.query(qb.prepareStatement());
		assertEquals(1, results.size());
		assertEquals(foo1, results.get(0));
		// set back to no-limit
		qb.limit(null);
		results = fooDao.query(qb.prepareStatement());
		assertEquals(2, results.size());
		assertEquals(foo1, results.get(0));
		assertEquals(foo2, results.get(1));
	}

	@Test
	public void testReturnId() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		StatementBuilder<Foo, String> qb = fooDao.statementBuilder();
		qb.columns(Foo.ID_COLUMN_NAME);
		List<Foo> results = fooDao.query(qb.prepareStatement());
		assertEquals(2, results.size());
		assertEquals(foo1.id, results.get(0).id);
		assertEquals(0, results.get(0).val);
		assertEquals(foo2.id, results.get(1).id);
		assertEquals(0, results.get(1).val);
	}

	@Test
	public void testDistinct() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		StatementBuilder<Foo, String> qb = fooDao.statementBuilder();
		qb.distinct().columns(Foo.EQUAL_COLUMN_NAME);
		List<Foo> results = fooDao.query(qb.prepareStatement());
		assertEquals(1, results.size());
		assertEquals(EQUAL_VAL, results.get(0).equal);
		assertNull(results.get(0).id);
	}

	@Test
	public void testIsNull() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		StatementBuilder<Foo, String> qb = fooDao.statementBuilder();

		// null fields start off as null so 0 are not-null
		qb.where().isNotNull(Foo.NULL_COLUMN_NAME);
		List<Foo> results = fooDao.query(qb.prepareStatement());
		assertEquals(0, results.size());

		// all are null
		qb.where().isNull(Foo.NULL_COLUMN_NAME);
		results = fooDao.query(qb.prepareStatement());
		assertEquals(2, results.size());
		assertEquals(foo1, results.get(0));
		assertEquals(foo2, results.get(1));

		// set the null fields to not-null
		for (Foo foo : results) {
			foo.nullField = "not null";
			assertEquals(1, fooDao.update(foo));
		}

		// no null results should be found
		qb.where().isNull(Foo.NULL_COLUMN_NAME);
		results = fooDao.query(qb.prepareStatement());
		assertEquals(0, results.size());

		// all are not-null
		qb.where().isNotNull(Foo.NULL_COLUMN_NAME);
		results = fooDao.query(qb.prepareStatement());
		assertEquals(2, results.size());
		assertEquals(foo1, results.get(0));
		assertEquals(foo2, results.get(1));
	}

	@Test
	public void testSetWhere() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		StatementBuilder<Foo, String> qb = fooDao.statementBuilder();
		Where where = qb.where();
		where.eq(Foo.ID_COLUMN_NAME, foo1.id);
		List<Foo> list = fooDao.query(qb.prepareStatement());
		assertEquals(1, list.size());
		assertEquals(foo1, list.get(0));

		qb = fooDao.statementBuilder();
		qb.setWhere(where);
		list = fooDao.query(qb.prepareStatement());
		assertEquals(1, list.size());
		assertEquals(foo1, list.get(0));
	}

	@Test
	public void testQueryForStringInt() throws Exception {
		Dao<Foo, String> fooDao = createTestData();
		StatementBuilder<Foo, String> qb = fooDao.statementBuilder();
		Where where = qb.where();
		// testing the val column with a integer as a string
		where.eq(Foo.VAL_COLUMN_NAME, Integer.toString(foo1.val));
		List<Foo> list = fooDao.query(qb.prepareStatement());
		assertEquals(1, list.size());
		assertEquals(foo1, list.get(0));
	}

	protected void checkPartialIterator(Iterator<PartialData> iterator, List<Integer> ids, List<String> firsts,
			List<String> lasts, boolean firstNull, boolean lastNull) throws SQLException {
		int i = 0;
		while (iterator.hasNext()) {
			PartialData partialData = iterator.next();
			checkPartial(partialData, ids, firsts, lasts, i, firstNull, lastNull);
			i++;
		}
	}

	protected void createPartial(Dao<PartialData, Integer> partialDao, List<Integer> ids, List<String> firsts,
			List<String> lasts, String first, String last) throws SQLException {
		PartialData partial = new PartialData();
		partial.first = first;
		partial.last = last;
		partialDao.create(partial);
		ids.add(partial.id);
		firsts.add(partial.first);
		lasts.add(partial.last);
		checkPartial(partialDao.queryForId(partial.id), ids, firsts, lasts, ids.size() - 1, false, false);
	}

	protected void checkPartialList(List<PartialData> partialList, List<Integer> ids, List<String> firsts,
			List<String> lasts, boolean firstNull, boolean lastNull) throws SQLException {
		assertEquals(partialList.size(), ids.size());
		for (int i = 0; i < partialList.size(); i++) {
			PartialData partial = partialList.get(i);
			assertEquals((int) ids.get(i), partial.id);
			if (firstNull) {
				assertNull(partial.first);
			} else {
				assertEquals(partial.first, firsts.get(i));
			}
			if (lastNull) {
				assertNull(partial.last);
			} else {
				assertEquals(partial.last, lasts.get(i));
			}
		}
	}

	private void checkPartial(PartialData partial, List<Integer> ids, List<String> firsts, List<String> lasts,
			int which, boolean firstNull, boolean lastNull) throws SQLException {
		assertNotNull(partial);
		assertTrue(which >= 0 && which < firsts.size());
		assertEquals((int) ids.get(which), partial.id);
		if (firstNull) {
			assertNull(partial.first);
		} else {
			assertEquals(partial.first, firsts.get(which));
		}
		if (lastNull) {
			assertNull(partial.last);
		} else {
			assertEquals(partial.last, lasts.get(which));
		}
	}

	private Dao<Foo, String> createTestData() throws Exception {
		Dao<Foo, String> fooDao = createDao(Foo.class, true);
		foo1 = new Foo();
		foo1.id = ID_PREFIX + "1";
		foo1.val = LOW_VAL;
		foo1.equal = EQUAL_VAL;
		assertEquals(1, fooDao.create(foo1));
		foo2 = new Foo();
		foo2.id = ID_PREFIX + "2";
		foo2.val = HIGH_VAL;
		foo2.equal = EQUAL_VAL;
		assertEquals(1, fooDao.create(foo2));
		return fooDao;
	}

	protected static class Foo {
		public static final String ID_COLUMN_NAME = "id";
		public static final String VAL_COLUMN_NAME = "val";
		public static final String EQUAL_COLUMN_NAME = "equal";
		public static final String NULL_COLUMN_NAME = "null";

		@DatabaseField(id = true, columnName = ID_COLUMN_NAME)
		String id;
		@DatabaseField(columnName = VAL_COLUMN_NAME)
		int val;
		@DatabaseField(columnName = EQUAL_COLUMN_NAME)
		int equal;
		@DatabaseField(columnName = NULL_COLUMN_NAME)
		String nullField;
		@Override
		public String toString() {
			return "Foo:" + id;
		}
		@Override
		public boolean equals(Object other) {
			if (other == null || other.getClass() != getClass())
				return false;
			return id.equals(((Foo) other).id);
		}
	}

	protected static class PartialData {
		public static final String FIRST_FIELD_NAME = "first";
		public static final String LAST_FIELD_NAME = "last";

		@DatabaseField(generatedId = true)
		public int id;
		@DatabaseField(columnName = FIRST_FIELD_NAME)
		public String first;
		@DatabaseField(columnName = LAST_FIELD_NAME)
		public String last;
	}
}
