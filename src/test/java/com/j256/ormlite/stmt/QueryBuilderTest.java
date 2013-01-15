package com.j256.ormlite.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.db.BaseDatabaseType;
import com.j256.ormlite.field.DatabaseField;

public class QueryBuilderTest extends BaseCoreStmtTest {

	@Test
	public void testSelectAll() throws Exception {
		QueryBuilder<Foo, Integer> qb = new QueryBuilder<Foo, Integer>(databaseType, baseFooTableInfo, null);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(' ');
		assertEquals(sb.toString(), qb.prepareStatementString());
	}

	@Test
	public void testAddColumns() throws Exception {
		QueryBuilder<Foo, Integer> qb = new QueryBuilder<Foo, Integer>(databaseType, baseFooTableInfo, null);
		String[] columns1 = new String[] { Foo.ID_COLUMN_NAME, Foo.VAL_COLUMN_NAME };
		String column2 = "equal";
		qb.selectColumns(columns1);
		qb.selectColumns(column2);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		for (String column : columns1) {
			databaseType.appendEscapedEntityName(sb, column);
			sb.append(',');
		}
		databaseType.appendEscapedEntityName(sb, column2);
		sb.append(" FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(' ');
		assertEquals(sb.toString(), qb.prepareStatementString());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAddBadColumn() {
		QueryBuilder<Foo, Integer> qb = new QueryBuilder<Foo, Integer>(databaseType, baseFooTableInfo, null);
		qb.selectColumns("unknown-column");
	}

	@Test
	public void testDontAddIdColumn() throws Exception {
		QueryBuilder<Foo, Integer> qb = new QueryBuilder<Foo, Integer>(databaseType, baseFooTableInfo, null);
		String column = Foo.VAL_COLUMN_NAME;
		String idColumn = Foo.ID_COLUMN_NAME;
		qb.selectColumns(column);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		databaseType.appendEscapedEntityName(sb, column);
		sb.append(',');
		databaseType.appendEscapedEntityName(sb, idColumn);
		sb.append(" FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(' ');
		assertEquals(sb.toString(), qb.prepareStatementString());
	}

	@Test
	public void testAddColumnsIterable() throws Exception {
		QueryBuilder<Foo, Integer> qb = new QueryBuilder<Foo, Integer>(databaseType, baseFooTableInfo, null);
		List<String> columns1 = new ArrayList<String>();
		columns1.add(Foo.ID_COLUMN_NAME);
		columns1.add(Foo.VAL_COLUMN_NAME);
		String column2 = "equal";
		qb.selectColumns(columns1);
		qb.selectColumns(column2);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		for (String column : columns1) {
			databaseType.appendEscapedEntityName(sb, column);
			sb.append(',');
		}
		databaseType.appendEscapedEntityName(sb, column2);
		sb.append(" FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(' ');
		assertEquals(sb.toString(), qb.prepareStatementString());
	}

	@Test
	public void testGroupBy() throws Exception {
		QueryBuilder<Foo, Integer> qb = new QueryBuilder<Foo, Integer>(databaseType, baseFooTableInfo, null);
		String field1 = Foo.VAL_COLUMN_NAME;
		qb.groupBy(field1);
		String field2 = Foo.ID_COLUMN_NAME;
		qb.groupBy(field2);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(" GROUP BY ");
		databaseType.appendEscapedEntityName(sb, field1);
		sb.append(',');
		databaseType.appendEscapedEntityName(sb, field2);
		sb.append(' ');
		assertEquals(sb.toString(), qb.prepareStatementString());
	}

	@Test
	public void testOrderBy() throws Exception {
		QueryBuilder<Foo, Integer> qb = new QueryBuilder<Foo, Integer>(databaseType, baseFooTableInfo, null);
		String field1 = Foo.VAL_COLUMN_NAME;
		qb.orderBy(field1, true);
		String field2 = Foo.ID_COLUMN_NAME;
		qb.orderBy(field2, true);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(" ORDER BY ");
		databaseType.appendEscapedEntityName(sb, field1);
		sb.append(',');
		databaseType.appendEscapedEntityName(sb, field2);
		sb.append(' ');
		assertEquals(sb.toString(), qb.prepareStatementString());
	}

	@Test
	public void testOrderByDesc() throws Exception {
		QueryBuilder<Foo, Integer> qb = new QueryBuilder<Foo, Integer>(databaseType, baseFooTableInfo, null);
		String field = Foo.VAL_COLUMN_NAME;
		qb.orderBy(field, false);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(" ORDER BY ");
		databaseType.appendEscapedEntityName(sb, field);
		sb.append(" DESC ");
		assertEquals(sb.toString(), qb.prepareStatementString());
	}

	@Test
	public void testDistinct() throws Exception {
		QueryBuilder<Foo, Integer> qb = new QueryBuilder<Foo, Integer>(databaseType, baseFooTableInfo, null);
		qb.distinct();
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT DISTINCT * FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(' ');
		assertEquals(sb.toString(), qb.prepareStatementString());
	}

	@Test
	public void testLimit() throws Exception {
		QueryBuilder<Foo, Integer> qb = new QueryBuilder<Foo, Integer>(databaseType, baseFooTableInfo, null);
		long limit = 103;
		qb.limit(limit);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(" LIMIT ").append(limit).append(' ');
		assertEquals(sb.toString(), qb.prepareStatementString());
	}

	@Test
	public void testOffset() throws Exception {
		QueryBuilder<Foo, Integer> qb = new QueryBuilder<Foo, Integer>(databaseType, baseFooTableInfo, null);
		long offset = 1;
		long limit = 2;
		qb.offset(offset);
		qb.limit(limit);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(" LIMIT ").append(offset).append(',').append(limit).append(' ');
		assertEquals(sb.toString(), qb.prepareStatementString());
	}

	@Test
	public void testOffsetWorks() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		assertEquals(1, dao.create(foo2));

		assertEquals(2, dao.queryForAll().size());

		QueryBuilder<Foo, Integer> qb = dao.queryBuilder();
		long offset = 1;
		long limit = 2;
		qb.offset(offset);
		qb.limit(limit);
		List<Foo> results = dao.query(qb.prepare());

		assertEquals(1, results.size());
	}

	@Test
	public void testLimitAfterSelect() throws Exception {
		QueryBuilder<Foo, Integer> qb =
				new QueryBuilder<Foo, Integer>(new LimitAfterSelectDatabaseType(), baseFooTableInfo, null);
		long limit = 103;
		qb.limit(limit);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT LIMIT ").append(limit);
		sb.append(" * FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(' ');
		assertEquals(sb.toString(), qb.prepareStatementString());
	}

	@Test
	public void testWhere() throws Exception {
		QueryBuilder<Foo, Integer> qb = new QueryBuilder<Foo, Integer>(databaseType, baseFooTableInfo, null);
		Where<Foo, Integer> where = qb.where();
		String val = "1";
		where.eq(Foo.ID_COLUMN_NAME, val);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(" WHERE ");
		databaseType.appendEscapedEntityName(sb, Foo.ID_COLUMN_NAME);
		sb.append(" = ").append(val).append(' ');
		assertEquals(sb.toString(), qb.prepareStatementString());
	}

	@Test
	public void testWhereSelectArg() throws Exception {
		QueryBuilder<Foo, Integer> qb = new QueryBuilder<Foo, Integer>(databaseType, baseFooTableInfo, null);
		Where<Foo, Integer> where = qb.where();
		SelectArg val = new SelectArg();
		where.eq(Foo.ID_COLUMN_NAME, val);
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(" WHERE ");
		databaseType.appendEscapedEntityName(sb, Foo.ID_COLUMN_NAME);
		sb.append(" = ? ");
		assertEquals(sb.toString(), qb.prepareStatementString());

		// set the where to the previous where
		qb.setWhere(where);
		assertEquals(sb.toString(), qb.prepareStatementString());
	}

	@Test
	public void testPrepareStatement() throws Exception {
		QueryBuilder<Foo, Integer> qb = new QueryBuilder<Foo, Integer>(databaseType, baseFooTableInfo, null);
		PreparedQuery<Foo> stmt = qb.prepare();
		stmt.getStatement();
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(' ');
		assertEquals(sb.toString(), qb.prepareStatementString());
	}

	@Test
	public void testLimitInline() throws Exception {
		QueryBuilder<Foo, Integer> qb = new QueryBuilder<Foo, Integer>(new LimitInline(), baseFooTableInfo, null);
		long limit = 213;
		qb.limit(limit);
		PreparedQuery<Foo> stmt = qb.prepare();
		stmt.getStatement();
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(" LIMIT ").append(limit).append(' ');
		assertEquals(sb.toString(), qb.prepareStatementString());
	}

	@Test
	public void testOffsetAndLimit() throws Exception {
		QueryBuilder<Foo, Integer> qb = new QueryBuilder<Foo, Integer>(new LimitInline(), baseFooTableInfo, null);
		long offset = 200;
		long limit = 213;
		qb.offset(offset);
		qb.limit(limit);
		PreparedQuery<Foo> stmt = qb.prepare();
		stmt.getStatement();
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, baseFooTableInfo.getTableName());
		sb.append(" LIMIT ").append(limit);
		sb.append(" OFFSET ").append(offset).append(' ');
		assertEquals(sb.toString(), qb.prepareStatementString());
	}

	@Test
	public void testShortCuts() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		assertEquals(1, dao.create(foo2));
		List<Foo> results = dao.queryBuilder().where().eq(Foo.ID_COLUMN_NAME, foo2.id).query();
		assertEquals(1, results.size());
		assertEquals(foo2.id, results.get(0).id);
		Iterator<Foo> iterator = dao.queryBuilder().where().eq(Foo.ID_COLUMN_NAME, foo2.id).iterator();
		assertTrue(iterator.hasNext());
		assertEquals(foo2.id, iterator.next().id);
		assertFalse(iterator.hasNext());
	}

	@Test
	public void testOrderByRaw() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		foo1.val = 1;
		foo1.equal = 10;
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		foo2.val = 5;
		foo2.equal = 7;
		assertEquals(1, dao.create(foo2));
		List<Foo> results =
				dao.queryBuilder()
						.orderByRaw("(" + Foo.VAL_COLUMN_NAME + "+" + Foo.EQUAL_COLUMN_NAME + ") DESC")
						.query();
		assertEquals(2, results.size());
		assertEquals(foo2.id, results.get(0).id);
		assertEquals(foo1.id, results.get(1).id);
	}

	@Test
	public void testQueryForForeign() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		Dao<Foreign, Object> foreignDao = createDao(Foreign.class, true);

		Foo foo = new Foo();
		foo.val = 1231;
		assertEquals(1, fooDao.create(foo));

		Foreign foreign = new Foreign();
		foreign.foo = foo;
		assertEquals(1, foreignDao.create(foreign));

		// use the auto-extract method to extract by id
		List<Foreign> results = foreignDao.queryBuilder().where().eq(Foreign.FOO_COLUMN_NAME, foo).query();
		assertEquals(1, results.size());
		assertEquals(foreign.id, results.get(0).id);

		// query for the id directly
		List<Foreign> results2 = foreignDao.queryBuilder().where().eq(Foreign.FOO_COLUMN_NAME, foo.id).query();
		assertEquals(1, results2.size());
		assertEquals(foreign.id, results2.get(0).id);
	}

	@Test(expected = SQLException.class)
	public void testQueryRawColumnsNotQuery() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		QueryBuilder<Foo, String> qb = dao.queryBuilder();
		qb.selectRaw("COUNT(*)");
		// we can't get Foo objects with the COUNT(*)
		dao.query(qb.prepare());
	}

	@Test
	public void testClear() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, false);
		QueryBuilder<Foo, String> qb = dao.queryBuilder();
		qb.selectColumns(Foo.VAL_COLUMN_NAME);
		qb.groupBy(Foo.VAL_COLUMN_NAME);
		qb.having("COUNT(VAL) > 1");
		qb.where().eq(Foo.ID_COLUMN_NAME, 1);
		qb.clear();
		assertEquals("SELECT * FROM `foo` ", qb.prepareStatementString());
	}

	@Test
	public void testInnerCountOf() throws Exception {
		Dao<Foo, String> fooDao = createDao(Foo.class, true);
		Dao<Bar, String> barDao = createDao(Bar.class, true);

		Bar bar1 = new Bar();
		int val = 12;
		bar1.val = val;
		assertEquals(1, barDao.create(bar1));
		Bar bar2 = new Bar();
		bar2.val = val + 1;
		assertEquals(1, barDao.create(bar2));

		Foo foo1 = new Foo();
		foo1.val = bar1.id;
		assertEquals(1, fooDao.create(foo1));
		Foo foo2 = new Foo();
		foo2.val = bar1.id;
		assertEquals(1, fooDao.create(foo2));
		Foo foo3 = new Foo();
		foo3.val = bar1.id + 1;
		assertEquals(1, fooDao.create(foo3));

		QueryBuilder<Bar, String> barQb = barDao.queryBuilder();
		barQb.selectColumns(Bar.ID_FIELD);
		barQb.where().eq(Bar.VAL_FIELD, val);

		QueryBuilder<Foo, String> fooQb = fooDao.queryBuilder();
		List<Integer> idList = new ArrayList<Integer>();
		idList.add(foo1.id);
		idList.add(foo2.id);
		idList.add(foo3.id);
		fooQb.where().in(Foo.ID_COLUMN_NAME, idList).and().in(Foo.VAL_COLUMN_NAME, barQb);

		fooQb.setCountOf(true);
		assertEquals(2, fooDao.countOf(fooQb.prepare()));
	}

	@Test
	public void testBaseClassComparison() throws Exception {
		Dao<Bar, String> barDao = createDao(Bar.class, true);
		Dao<Baz, String> bazDao = createDao(Baz.class, true);

		BarSuperClass bar1 = new BarSuperClass();
		bar1.val = 10;
		assertEquals(1, barDao.create(bar1));

		Baz baz1 = new Baz();
		baz1.bar = bar1;
		assertEquals(1, bazDao.create(baz1));

		List<Baz> results = bazDao.queryBuilder().where().eq(Baz.BAR_FIELD, bar1).query();
		assertEquals(1, results.size());
		assertEquals(bar1.id, results.get(0).bar.id);

		try {
			// we allow a super class of the field but _not_ a sub class
			results = bazDao.queryBuilder().where().eq(Baz.BAR_FIELD, new Object()).query();
			fail("Should have thrown");
		} catch (SQLException e) {
			// expected
		}
	}

	@Test
	public void testReservedWords() throws Exception {
		Dao<Reserved, Integer> dao = createDao(Reserved.class, true);
		QueryBuilder<Reserved, Integer> sb = dao.queryBuilder();
		sb.where().eq(Reserved.FIELD_NAME_GROUP, "something");
		sb.query();
	}

	@Test
	public void testSimpleJoin() throws Exception {
		Dao<Bar, Integer> barDao = createDao(Bar.class, true);
		Dao<Baz, Integer> bazDao = createDao(Baz.class, true);

		Bar bar1 = new Bar();
		bar1.val = 2234;
		assertEquals(1, barDao.create(bar1));
		Bar bar2 = new Bar();
		bar2.val = 324322234;
		assertEquals(1, barDao.create(bar2));

		Baz baz1 = new Baz();
		baz1.bar = bar1;
		assertEquals(1, bazDao.create(baz1));
		Baz baz2 = new Baz();
		baz2.bar = bar2;
		assertEquals(1, bazDao.create(baz2));

		QueryBuilder<Bar, Integer> barQb = barDao.queryBuilder();
		barQb.where().eq(Bar.VAL_FIELD, bar1.val);
		List<Baz> results = bazDao.queryBuilder().query();
		assertEquals(2, results.size());
		results = bazDao.queryBuilder().join(barQb).query();
		assertEquals(1, results.size());
		assertEquals(bar1.id, results.get(0).bar.id);
	}

	@Test
	public void testReverseJoin() throws Exception {
		Dao<Bar, Integer> barDao = createDao(Bar.class, true);
		Dao<Baz, Integer> bazDao = createDao(Baz.class, true);

		Bar bar1 = new Bar();
		bar1.val = 2234;
		assertEquals(1, barDao.create(bar1));
		Bar bar2 = new Bar();
		bar2.val = 324322234;
		assertEquals(1, barDao.create(bar2));

		Baz baz1 = new Baz();
		baz1.bar = bar1;
		assertEquals(1, bazDao.create(baz1));
		Baz baz2 = new Baz();
		baz2.bar = bar2;
		assertEquals(1, bazDao.create(baz2));

		QueryBuilder<Baz, Integer> bazQb = bazDao.queryBuilder();
		bazQb.where().eq(Baz.ID_FIELD, baz1.id);
		List<Bar> results = barDao.queryBuilder().query();
		assertEquals(2, results.size());
		results = barDao.queryBuilder().join(bazQb).query();
		assertEquals(1, results.size());
		assertEquals(bar1.val, results.get(0).val);
	}

	@Test
	public void testJoinDoubleWhere() throws Exception {
		Dao<Bar, Integer> barDao = createDao(Bar.class, true);
		Dao<Baz, Integer> bazDao = createDao(Baz.class, true);

		Bar bar1 = new Bar();
		bar1.val = 2234;
		assertEquals(1, barDao.create(bar1));
		Bar bar2 = new Bar();
		bar2.val = 324322234;
		assertEquals(1, barDao.create(bar2));

		Baz baz1 = new Baz();
		baz1.bar = bar1;
		assertEquals(1, bazDao.create(baz1));
		Baz baz2 = new Baz();
		// both have bar1
		baz2.bar = bar1;
		assertEquals(1, bazDao.create(baz2));

		QueryBuilder<Bar, Integer> barQb = barDao.queryBuilder();
		barQb.where().eq(Bar.VAL_FIELD, bar1.val);
		QueryBuilder<Baz, Integer> bazQb = bazDao.queryBuilder();
		bazQb.where().eq(Baz.ID_FIELD, baz1.id);
		List<Baz> results = bazQb.join(barQb).query();
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(bar1.id, results.get(0).bar.id);
	}

	@Test
	public void testJoinOrder() throws Exception {
		Dao<Bar, Integer> barDao = createDao(Bar.class, true);
		Dao<Baz, Integer> bazDao = createDao(Baz.class, true);

		Bar bar1 = new Bar();
		bar1.val = 2234;
		assertEquals(1, barDao.create(bar1));
		Bar bar2 = new Bar();
		bar2.val = 324322234;
		assertEquals(1, barDao.create(bar2));

		Baz baz1 = new Baz();
		baz1.bar = bar1;
		assertEquals(1, bazDao.create(baz1));
		Baz baz2 = new Baz();
		baz2.bar = bar2;
		assertEquals(1, bazDao.create(baz2));

		QueryBuilder<Bar, Integer> barQb = barDao.queryBuilder();
		barQb.orderBy(Bar.VAL_FIELD, true);
		List<Baz> results = bazDao.queryBuilder().join(barQb).query();
		assertEquals(2, results.size());
		assertEquals(bar1.id, results.get(0).bar.id);
		assertEquals(bar2.id, results.get(1).bar.id);

		// reset the query to change the order direction
		barQb.clear();
		barQb.orderBy(Bar.VAL_FIELD, false);
		results = bazDao.queryBuilder().join(barQb).query();
		assertEquals(2, results.size());
		assertEquals(bar2.id, results.get(0).bar.id);
		assertEquals(bar1.id, results.get(1).bar.id);
	}

	@Test
	public void testJoinMultipleOrder() throws Exception {
		Dao<Bar, Integer> barDao = createDao(Bar.class, true);
		Dao<Baz, Integer> bazDao = createDao(Baz.class, true);

		Bar bar1 = new Bar();
		bar1.val = 2234;
		assertEquals(1, barDao.create(bar1));
		Bar bar2 = new Bar();
		bar2.val = 324322234;
		assertEquals(1, barDao.create(bar2));

		Baz baz1 = new Baz();
		baz1.bar = bar1;
		assertEquals(1, bazDao.create(baz1));
		Baz baz2 = new Baz();
		baz2.bar = bar2;
		assertEquals(1, bazDao.create(baz2));

		QueryBuilder<Bar, Integer> barQb = barDao.queryBuilder();
		barQb.where().eq(Bar.VAL_FIELD, bar1.val);
		barQb.orderBy(Bar.ID_FIELD, true);
		List<Baz> results = bazDao.queryBuilder().query();
		assertEquals(2, results.size());
		results = bazDao.queryBuilder().orderBy(Baz.ID_FIELD, true).join(barQb).query();
		assertEquals(1, results.size());
		assertEquals(bar1.id, results.get(0).bar.id);
	}

	@Test
	public void testJoinGroup() throws Exception {
		Dao<Bar, Integer> barDao = createDao(Bar.class, true);
		Dao<Baz, Integer> bazDao = createDao(Baz.class, true);

		Bar bar1 = new Bar();
		bar1.val = 2234;
		assertEquals(1, barDao.create(bar1));
		Bar bar2 = new Bar();
		bar2.val = 324322234;
		assertEquals(1, barDao.create(bar2));

		Baz baz1 = new Baz();
		baz1.bar = bar1;
		assertEquals(1, bazDao.create(baz1));
		Baz baz2 = new Baz();
		baz2.bar = bar2;
		assertEquals(1, bazDao.create(baz2));

		QueryBuilder<Bar, Integer> barQb = barDao.queryBuilder();
		barQb.where().eq(Bar.VAL_FIELD, bar1.val);
		barQb.groupBy(Bar.ID_FIELD);
		List<Baz> results = bazDao.queryBuilder().query();
		assertEquals(2, results.size());
		results = bazDao.queryBuilder().groupBy(Baz.ID_FIELD).join(barQb).query();
		assertEquals(1, results.size());
		assertEquals(bar1.id, results.get(0).bar.id);
	}

	@Test
	public void testLeftJoin() throws Exception {
		Dao<Bar, Integer> barDao = createDao(Bar.class, true);
		Dao<Baz, Integer> bazDao = createDao(Baz.class, true);

		Bar bar1 = new Bar();
		bar1.val = 2234;
		assertEquals(1, barDao.create(bar1));
		Bar bar2 = new Bar();
		bar2.val = 324322234;
		assertEquals(1, barDao.create(bar2));

		Baz baz1 = new Baz();
		baz1.bar = bar1;
		assertEquals(1, bazDao.create(baz1));
		Baz baz2 = new Baz();
		baz2.bar = bar2;
		assertEquals(1, bazDao.create(baz2));
		Baz baz3 = new Baz();
		// no bar
		assertEquals(1, bazDao.create(baz3));

		QueryBuilder<Bar, Integer> barQb = barDao.queryBuilder();
		List<Baz> results = bazDao.queryBuilder().query();
		assertEquals(3, results.size());
		results = bazDao.queryBuilder().join(barQb).query();
		assertEquals(2, results.size());
		results = bazDao.queryBuilder().leftJoin(barQb).query();
		assertEquals(3, results.size());
	}

	@Test
	public void testMultipleJoin() throws Exception {
		Dao<Bar, Integer> barDao = createDao(Bar.class, true);
		Dao<Baz, Integer> bazDao = createDao(Baz.class, true);
		Dao<Bing, Integer> bingDao = createDao(Bing.class, true);

		Bar bar1 = new Bar();
		bar1.val = 2234;
		assertEquals(1, barDao.create(bar1));
		Bar bar2 = new Bar();
		bar2.val = 324322234;
		assertEquals(1, barDao.create(bar2));

		Baz baz1 = new Baz();
		baz1.bar = bar1;
		assertEquals(1, bazDao.create(baz1));
		Baz baz2 = new Baz();
		baz2.bar = bar2;
		assertEquals(1, bazDao.create(baz2));

		Bing bing1 = new Bing();
		bing1.baz = baz1;
		assertEquals(1, bingDao.create(bing1));
		Bing bing2 = new Bing();
		bing2.baz = baz2;
		assertEquals(1, bingDao.create(bing2));

		QueryBuilder<Bar, Integer> barQb = barDao.queryBuilder();
		barQb.where().eq(Bar.VAL_FIELD, bar1.val);

		QueryBuilder<Baz, Integer> bazQb = bazDao.queryBuilder();
		assertEquals(2, bazQb.query().size());
		bazQb.join(barQb);

		List<Bing> results = bingDao.queryBuilder().join(bazQb).query();
		assertEquals(1, results.size());
		assertEquals(bing1.id, results.get(0).id);
		assertEquals(baz1.id, results.get(0).baz.id);
		bazDao.refresh(results.get(0).baz);
		assertEquals(bar1.id, results.get(0).baz.bar.id);
	}

	@Test(expected = SQLException.class)
	public void testBadJoin() throws Exception {
		Dao<Bar, Integer> barDao = createDao(Bar.class, true);
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		fooDao.queryBuilder().join(barDao.queryBuilder()).query();
	}

	@Test
	public void testColumnArg() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		int val = 3123123;
		foo1.val = val;
		foo1.equal = val;
		assertEquals(1, fooDao.create(foo1));
		Foo foo2 = new Foo();
		foo2.val = val;
		foo2.equal = val + 1;
		assertEquals(1, fooDao.create(foo2));

		QueryBuilder<Foo, Integer> qb = fooDao.queryBuilder();
		qb.where().eq(Foo.VAL_COLUMN_NAME, new ColumnArg(Foo.EQUAL_COLUMN_NAME));
		List<Foo> results = qb.query();
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(foo1.id, results.get(0).id);
	}

	@Test
	public void testSimpleJoinColumnArg() throws Exception {
		Dao<Bar, Integer> barDao = createDao(Bar.class, true);
		Dao<Baz, Integer> bazDao = createDao(Baz.class, true);

		Bar bar1 = new Bar();
		int val = 1313123;
		bar1.val = val;
		assertEquals(1, barDao.create(bar1));
		Bar bar2 = new Bar();
		bar2.val = val;
		assertEquals(1, barDao.create(bar2));

		Baz baz1 = new Baz();
		baz1.bar = bar1;
		baz1.val = val;
		assertEquals(1, bazDao.create(baz1));
		Baz baz2 = new Baz();
		baz2.bar = bar2;
		baz2.val = val + 1;
		assertEquals(1, bazDao.create(baz2));
		Baz baz3 = new Baz();
		baz1.bar = bar2;
		baz1.val = val;
		assertEquals(1, bazDao.create(baz3));
		Baz baz4 = new Baz();
		baz2.bar = bar1;
		baz2.val = val;
		assertEquals(1, bazDao.create(baz4));

		QueryBuilder<Bar, Integer> barQb = barDao.queryBuilder();
		barQb.where().eq(Bar.VAL_FIELD, new ColumnArg("baz", Baz.VAL_FIELD));
		List<Baz> results = bazDao.queryBuilder().query();
		assertEquals(4, results.size());
		results = bazDao.queryBuilder().join(barQb).query();
		assertEquals(1, results.size());
		assertEquals(bar1.id, results.get(0).bar.id);
	}

	/* ======================================================================================================== */

	private static class LimitInline extends BaseDatabaseType {
		public boolean isDatabaseUrlThisType(String url, String dbTypePart) {
			return true;
		}
		@Override
		protected String getDriverClassName() {
			return "foo.bar.baz";
		}
		public String getDatabaseName() {
			return "zipper";
		}
	}

	protected static class Bar {
		public static final String ID_FIELD = "id";
		public static final String VAL_FIELD = "val";
		@DatabaseField(generatedId = true, columnName = ID_FIELD)
		int id;
		@DatabaseField(columnName = VAL_FIELD)
		int val;
		public Bar() {
		}
	}

	private static class BarSuperClass extends Bar {
	}

	protected static class Baz {
		public static final String ID_FIELD = "id";
		public static final String VAL_FIELD = "val";
		public static final String BAR_FIELD = "bar";
		@DatabaseField(generatedId = true, columnName = ID_FIELD)
		int id;
		@DatabaseField(columnName = VAL_FIELD)
		int val;
		@DatabaseField(foreign = true, columnName = BAR_FIELD)
		Bar bar;
		public Baz() {
		}
	}

	protected static class Bing {
		public static final String ID_FIELD = "id";
		public static final String BAZ_FIELD = "baz";
		@DatabaseField(generatedId = true, columnName = ID_FIELD)
		int id;
		@DatabaseField(foreign = true, columnName = BAZ_FIELD)
		Baz baz;
		public Bing() {
		}
	}

	protected static class Reserved {
		public static final String FIELD_NAME_GROUP = "group";
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(columnName = FIELD_NAME_GROUP)
		String group;
		public Reserved() {
		}
	}
}
