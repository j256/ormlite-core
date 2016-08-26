package com.j256.ormlite.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.db.BaseDatabaseType;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.stmt.QueryBuilder.JoinType;
import com.j256.ormlite.stmt.QueryBuilder.JoinWhereOperation;

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
			sb.append(", ");
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
			sb.append(", ");
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
		List<Foo> results = dao.queryBuilder()
				.orderByRaw("(" + Foo.VAL_COLUMN_NAME + "+" + Foo.EQUAL_COLUMN_NAME + ") DESC")
				.query();
		assertEquals(2, results.size());
		assertEquals(foo2.id, results.get(0).id);
		assertEquals(foo1.id, results.get(1).id);
	}

	@Test
	public void testOrderByRawArg() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		foo1.val = 1;
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		foo2.val = 2;
		assertEquals(1, dao.create(foo2));
		List<Foo> results = dao.queryBuilder()
				.orderByRaw("(" + Foo.VAL_COLUMN_NAME + " = ? ) DESC", new SelectArg(SqlType.INTEGER, 2))
				.query();
		assertEquals(2, results.size());
		assertEquals(foo2.id, results.get(0).id);
		assertEquals(foo1.id, results.get(1).id);
		results = dao.queryBuilder()
				.orderByRaw("(" + Foo.VAL_COLUMN_NAME + " = ? )", new SelectArg(SqlType.INTEGER, 2))
				.query();
		assertEquals(2, results.size());
		assertEquals(foo1.id, results.get(0).id);
		assertEquals(foo2.id, results.get(1).id);
	}

	@Test
	public void testOrderByRawAndOrderBy() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		foo1.val = 1;
		foo1.equal = 10;
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		foo2.val = 5;
		foo2.equal = 7;
		assertEquals(1, dao.create(foo2));
		Foo foo3 = new Foo();
		foo3.val = 7;
		foo3.equal = 5;
		assertEquals(1, dao.create(foo3));
		List<Foo> results = dao.queryBuilder()
				.orderByRaw("(" + Foo.VAL_COLUMN_NAME + "+" + Foo.EQUAL_COLUMN_NAME + ") DESC")
				.query();
		assertEquals(3, results.size());
		assertEquals(foo2.id, results.get(0).id);
		assertEquals(foo3.id, results.get(1).id);
		assertEquals(foo1.id, results.get(2).id);

		results = dao.queryBuilder()
				.orderByRaw("(" + Foo.VAL_COLUMN_NAME + "+" + Foo.EQUAL_COLUMN_NAME + ") DESC")
				.orderBy(Foo.VAL_COLUMN_NAME, false)
				.query();
		assertEquals(3, results.size());
		assertEquals(foo3.id, results.get(0).id);
		assertEquals(foo2.id, results.get(1).id);
		assertEquals(foo1.id, results.get(2).id);

		results = dao.queryBuilder()
				.orderBy(Foo.VAL_COLUMN_NAME, true)
				.orderByRaw("(" + Foo.VAL_COLUMN_NAME + "+" + Foo.EQUAL_COLUMN_NAME + ") DESC")
				.query();
		assertEquals(3, results.size());
		assertEquals(foo1.id, results.get(0).id);
		assertEquals(foo2.id, results.get(1).id);
		assertEquals(foo3.id, results.get(2).id);
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
		qb.reset();
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

	@SuppressWarnings("unchecked")
	@Test
	public void testMixAndOrInline() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);

		Foo foo1 = new Foo();
		foo1.val = 10;
		foo1.stringField = "zip";
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		foo2.val = foo1.val;
		foo2.stringField = foo1.stringField + "zap";
		assertEquals(1, dao.create(foo2));

		/*
		 * Inline
		 */
		QueryBuilder<Foo, String> qb = dao.queryBuilder();
		Where<Foo, String> where = qb.where();
		where.eq(Foo.VAL_COLUMN_NAME, foo1.val)
				.and()
				.eq(Foo.STRING_COLUMN_NAME, foo1.stringField)
				.or()
				.eq(Foo.STRING_COLUMN_NAME, foo2.stringField);

		List<Foo> results = dao.queryForAll();
		assertEquals(2, results.size());
		assertEquals(foo1.id, results.get(0).id);
		assertEquals(foo2.id, results.get(1).id);

		/*
		 * Arguments
		 */
		qb = dao.queryBuilder();
		where = qb.where();
		where.and(where.eq(Foo.VAL_COLUMN_NAME, foo1.val), //
				where.or(where.eq(Foo.STRING_COLUMN_NAME, foo1.stringField), //
						where.eq(Foo.STRING_COLUMN_NAME, foo2.stringField)));

		results = dao.queryForAll();
		assertEquals(2, results.size());
		assertEquals(foo1.id, results.get(0).id);
		assertEquals(foo2.id, results.get(1).id);

		/*
		 * Multiple lines
		 */
		qb = dao.queryBuilder();
		where = qb.where();
		where.eq(Foo.VAL_COLUMN_NAME, foo1.val);
		where.and();
		where.eq(Foo.STRING_COLUMN_NAME, foo1.stringField);
		where.or();
		where.eq(Foo.STRING_COLUMN_NAME, foo2.stringField);

		results = dao.queryForAll();
		assertEquals(2, results.size());
		assertEquals(foo1.id, results.get(0).id);
		assertEquals(foo2.id, results.get(1).id);

		/*
		 * Postfix
		 */
		qb = dao.queryBuilder();
		where = qb.where();
		where.eq(Foo.VAL_COLUMN_NAME, foo1.val);
		where.eq(Foo.STRING_COLUMN_NAME, foo1.stringField);
		where.eq(Foo.STRING_COLUMN_NAME, foo2.stringField);
		where.or(2);
		where.and(2);

		results = dao.queryForAll();
		assertEquals(2, results.size());
		assertEquals(foo1.id, results.get(0).id);
		assertEquals(foo2.id, results.get(1).id);
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
	public void testSimpleJoinOr() throws Exception {
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
		baz1.val = 423423;
		assertEquals(1, bazDao.create(baz1));
		Baz baz2 = new Baz();
		baz2.bar = bar2;
		baz2.val = 9570423;
		assertEquals(1, bazDao.create(baz2));

		QueryBuilder<Bar, Integer> barQb = barDao.queryBuilder();
		barQb.where().eq(Bar.VAL_FIELD, bar1.val);
		List<Baz> results = bazDao.queryBuilder().query();
		assertEquals(2, results.size());
		QueryBuilder<Baz, Integer> bazQb = bazDao.queryBuilder();
		bazQb.where().eq(Baz.VAL_FIELD, baz2.val);
		results = bazQb.joinOr(barQb).query();
		assertEquals(2, results.size());
		assertEquals(bar1.id, results.get(0).bar.id);
		assertEquals(bar2.id, results.get(1).bar.id);

		bazQb.reset();
		bazQb.where().eq(Baz.VAL_FIELD, baz2.val);
		results = bazQb.join(barQb, JoinType.INNER, JoinWhereOperation.OR).query();
		assertEquals(2, results.size());
		assertEquals(bar1.id, results.get(0).bar.id);
		assertEquals(bar2.id, results.get(1).bar.id);

		// now do join which should be an AND
		bazQb.reset();
		bazQb.where().eq(Baz.VAL_FIELD, baz2.val);
		results = bazQb.join(barQb).query();
		// should find no results
		assertEquals(0, results.size());
	}

	@Test
	public void testSimpleJoinWhere() throws Exception {
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
		QueryBuilder<Baz, Integer> bazQb = bazDao.queryBuilder();
		bazQb.where().eq(Baz.VAL_FIELD, baz1.val);
		results = bazQb.join(barQb).query();
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
		barQb.reset();
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

		results = bazDao.queryBuilder().join(barQb, JoinType.LEFT, JoinWhereOperation.AND).query();
		assertEquals(3, results.size());
	}

	@Test
	public void testInnerJoin() throws Exception {
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
		bing2.baz = baz1;
		assertEquals(1, bingDao.create(bing2));

		QueryBuilder<Bar, Integer> barQb = barDao.queryBuilder();
		barQb.where().eq(Bar.VAL_FIELD, bar1.val);
		List<Baz> results = bazDao.queryBuilder().query();
		assertEquals(2, results.size());

		QueryBuilder<Bing, Integer> bingQb = bingDao.queryBuilder();
		bingQb.where().eq(Bing.ID_FIELD, bing2.id);
		List<Baz> bingResults = bazDao.queryBuilder().query();
		assertEquals(2, bingResults.size());

		results = bazDao.queryBuilder().join(barQb).join(bingQb).query();
		assertEquals(1, results.size());
		assertEquals(bar1.id, results.get(0).bar.id);
	}

	@Test
	public void testPickTheRightJoin() throws Exception {
		Dao<One, Integer> oneDao = createDao(One.class, true);
		Dao<Two, Integer> twoDao = createDao(Two.class, true);

		One one1 = new One();
		one1.val = 2234;
		assertEquals(1, oneDao.create(one1));
		One one2 = new One();
		one2.val = 324322234;
		assertEquals(1, oneDao.create(one2));

		Two two1 = new Two();
		two1.one = one1;
		assertEquals(1, twoDao.create(two1));
		Two two2 = new Two();
		two2.one = one2;
		assertEquals(1, twoDao.create(two2));

		QueryBuilder<One, Integer> oneQb = oneDao.queryBuilder();
		oneQb.where().eq(One.VAL_FIELD, one1.val);
		List<Two> results = twoDao.queryBuilder().query();
		assertEquals(2, results.size());
		results = twoDao.queryBuilder().join(oneQb).query();
		assertEquals(1, results.size());
		assertEquals(one1.id, results.get(0).one.id);
	}

	@Test
	public void testPickTheRightJoinReverse() throws Exception {
		Dao<One, Integer> oneDao = createDao(One.class, true);
		Dao<Two, Integer> twoDao = createDao(Two.class, true);

		One one1 = new One();
		one1.val = 2234;
		assertEquals(1, oneDao.create(one1));
		One one2 = new One();
		one2.val = 324322234;
		assertEquals(1, oneDao.create(one2));

		Two two1 = new Two();
		two1.val = 431231232;
		two1.one = one1;
		assertEquals(1, twoDao.create(two1));
		Two two2 = new Two();
		two2.one = one2;
		assertEquals(1, twoDao.create(two2));

		QueryBuilder<Two, Integer> twoQb = twoDao.queryBuilder();
		twoQb.where().eq(Two.VAL_FIELD, two1.val);
		List<One> results = oneDao.queryBuilder().query();
		assertEquals(2, results.size());
		results = oneDao.queryBuilder().join(twoQb).query();
		assertEquals(1, results.size());
		assertEquals(two1.one.id, results.get(0).id);
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
	public void testColumnArgString() throws Exception {
		Dao<StringColumnArg, Integer> dao = createDao(StringColumnArg.class, true);
		StringColumnArg foo1 = new StringColumnArg();
		String val = "3123123";
		foo1.str1 = val;
		foo1.str2 = val;
		assertEquals(1, dao.create(foo1));
		StringColumnArg foo2 = new StringColumnArg();
		foo2.str1 = val;
		foo2.str2 = val + "...";
		assertEquals(1, dao.create(foo2));

		QueryBuilder<StringColumnArg, Integer> qb = dao.queryBuilder();
		qb.where().eq(StringColumnArg.STR1_FIELD, new ColumnArg(StringColumnArg.STR2_FIELD));
		List<StringColumnArg> results = qb.query();
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

	@Test
	public void testHavingOrderBy() throws Exception {
		Dao<Foo, Object> fooDao = createDao(Foo.class, true);

		Foo foo1 = new Foo();
		foo1.val = 10;
		assertEquals(1, fooDao.create(foo1));
		Foo foo2 = new Foo();
		foo2.val = 20;
		assertEquals(1, fooDao.create(foo2));
		Foo foo3 = new Foo();
		foo3.val = 30;
		assertEquals(1, fooDao.create(foo3));
		Foo foo4 = new Foo();
		foo4.val = 40;
		assertEquals(1, fooDao.create(foo4));

		QueryBuilder<Foo, Object> qb = fooDao.queryBuilder();
		qb.groupBy(Foo.ID_COLUMN_NAME);
		qb.orderBy(Foo.VAL_COLUMN_NAME, false);
		qb.having("val < " + foo3.val);
		List<Foo> results = qb.query();
		assertEquals(2, results.size());
		assertEquals(foo2.val, results.get(0).val);
		assertEquals(foo1.val, results.get(1).val);
	}

	@Test
	public void testMaxJoin() throws Exception {
		Dao<Foo, Object> dao = createDao(Foo.class, true);

		Foo foo1 = new Foo();
		foo1.val = 10;
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		foo2.val = 20;
		assertEquals(1, dao.create(foo2));
		Foo foo3 = new Foo();
		foo3.val = 30;
		assertEquals(1, dao.create(foo3));
		Foo foo4 = new Foo();
		foo4.val = 40;
		assertEquals(1, dao.create(foo4));

		QueryBuilder<Foo, Object> iqb = dao.queryBuilder();
		iqb.selectRaw("max(id)");

		QueryBuilder<Foo, Object> oqb = dao.queryBuilder();
		Foo result = oqb.where().in(Foo.ID_COLUMN_NAME, iqb).queryForFirst();
		assertNotNull(result);
		assertEquals(foo4.id, result.id);
	}

	@Test
	public void testQueryRawMax() throws Exception {
		Dao<Foo, Object> dao = createDao(Foo.class, true);

		Foo foo1 = new Foo();
		foo1.stringField = "1";
		foo1.val = 10;
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		foo2.stringField = "1";
		foo2.val = 20;
		assertEquals(1, dao.create(foo2));
		Foo foo3 = new Foo();
		foo3.stringField = "2";
		foo3.val = 30;
		assertEquals(1, dao.create(foo3));
		Foo foo4 = new Foo();
		foo4.stringField = "2";
		foo4.val = 40;
		assertEquals(1, dao.create(foo4));

		QueryBuilder<Foo, Object> qb = dao.queryBuilder();
		qb.selectRaw("string, max(val) as val");
		qb.groupBy(Foo.STRING_COLUMN_NAME);
		GenericRawResults<Foo> results = dao.queryRaw(qb.prepareStatementString(), dao.getRawRowMapper());
		assertNotNull(results);
		CloseableIterator<Foo> iterator = results.closeableIterator();
		try {
			assertTrue(iterator.hasNext());
			assertEquals(foo2.val, iterator.next().val);
			assertTrue(iterator.hasNext());
			assertEquals(foo4.val, iterator.next().val);
			assertFalse(iterator.hasNext());
		} finally {
			iterator.close();
		}
	}

	@Test
	public void testJoinTwoColumns() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		Dao<StringColumnArg, Integer> scaDao = createDao(StringColumnArg.class, true);

		Foo foo1 = new Foo();
		foo1.val = 123213213;
		foo1.stringField = "stuff";
		fooDao.create(foo1);

		Foo foo2 = new Foo();
		foo2.stringField = "not stuff";
		fooDao.create(foo2);

		StringColumnArg sca1 = new StringColumnArg();
		sca1.str1 = foo1.stringField;
		scaDao.create(sca1);

		StringColumnArg sca2 = new StringColumnArg();
		sca2.str1 = foo2.stringField;
		scaDao.create(sca2);

		StringColumnArg sca3 = new StringColumnArg();
		sca3.str1 = "some other field";
		scaDao.create(sca3);

		QueryBuilder<Foo, Integer> fooQb = fooDao.queryBuilder();
		fooQb.where().eq(Foo.VAL_COLUMN_NAME, foo1.val);

		QueryBuilder<StringColumnArg, Integer> scaQb = scaDao.queryBuilder();
		scaQb.join(StringColumnArg.STR1_FIELD, Foo.STRING_COLUMN_NAME, fooQb);
		List<StringColumnArg> results = scaQb.query();
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(sca1.id, results.get(0).id);

		fooQb.reset();
		fooQb.where().eq(Foo.VAL_COLUMN_NAME, foo2.val);

		scaQb.reset();
		scaQb.join(StringColumnArg.STR1_FIELD, Foo.STRING_COLUMN_NAME, fooQb);
		results = scaQb.query();
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(sca2.id, results.get(0).id);
	}

	@Test
	public void testLeftJoinTwoColumns() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);
		Dao<StringColumnArg, Integer> scaDao = createDao(StringColumnArg.class, true);

		Foo foo1 = new Foo();
		foo1.val = 123213213;
		foo1.stringField = "stuff";
		fooDao.create(foo1);

		StringColumnArg sca1 = new StringColumnArg();
		sca1.str1 = foo1.stringField;
		scaDao.create(sca1);

		StringColumnArg sca2 = new StringColumnArg();
		sca2.str1 = "something eles";
		scaDao.create(sca2);

		QueryBuilder<Foo, Integer> fooQb = fooDao.queryBuilder();
		QueryBuilder<StringColumnArg, Integer> scaQb = scaDao.queryBuilder();
		scaQb.join(StringColumnArg.STR1_FIELD, Foo.STRING_COLUMN_NAME, fooQb);
		List<StringColumnArg> results = scaQb.query();
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(sca1.id, results.get(0).id);

		scaQb.reset();
		scaQb.join(StringColumnArg.STR1_FIELD, Foo.STRING_COLUMN_NAME, fooQb, JoinType.LEFT, JoinWhereOperation.AND);
		results = scaQb.query();
		assertNotNull(results);
		assertEquals(2, results.size());
		assertEquals(sca1.id, results.get(0).id);
		assertEquals(sca2.id, results.get(1).id);
	}

	@Test
	public void testSpecificJoinLoggingBug() throws Exception {
		/*
		 * Test trying to specifically reproduce a reported bug. The query built in the logs was enough to show that
		 * either the bug has already been fixed or the test is not reproducing the problem adequately.
		 */

		Dao<Category, Integer> categoryDao = createDao(Category.class, true);
		Dao<Criterion, Integer> criterionDao = createDao(Criterion.class, true);

		QueryBuilder<Criterion, Integer> criteriaQb = criterionDao.queryBuilder();
		criteriaQb.where().eq("active", Boolean.valueOf(true));

		QueryBuilder<Category, Integer> categoryQb = categoryDao.queryBuilder();
		categoryQb.orderByRaw("id").join(criteriaQb).query();
	}

	@Test
	public void testSelectColumnsNoId() throws Exception {
		Dao<NoId, Void> dao = createDao(NoId.class, true);
		QueryBuilder<NoId, Void> qb = dao.queryBuilder();
		qb.selectColumns(NoId.FIELD_NAME_STUFF);
		/*
		 * Had a subtle, long-standing bug here that threw an exception when building the query if you were selecting
		 * specific columns from an entity _without_ an id field.
		 */
		qb.prepare();
	}

	@Test
	public void testRandomIsNull() throws Exception {
		Dao<SeralizableNull, Integer> dao = createDao(SeralizableNull.class, true);
		SeralizableNull sn1 = new SeralizableNull();
		assertEquals(1, dao.create(sn1));
		SeralizableNull sn2 = new SeralizableNull();
		sn2.serializable = "wow";
		assertEquals(1, dao.create(sn2));

		List<SeralizableNull> results =
				dao.queryBuilder().where().isNull(SeralizableNull.FIELD_NAME_SERIALIZABLE).query();
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(sn1.id, results.get(0).id);
		results = dao.queryBuilder().where().isNotNull(SeralizableNull.FIELD_NAME_SERIALIZABLE).query();
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(sn2.id, results.get(0).id);
	}

	@Test
	public void testCountOf() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);

		QueryBuilder<Foo, Integer> qb = dao.queryBuilder();
		assertEquals(0, qb.countOf());

		Foo foo1 = new Foo();
		int val = 123213;
		foo1.val = val;
		assertEquals(1, dao.create(foo1));
		assertEquals(1, qb.countOf());

		Foo foo2 = new Foo();
		foo2.val = val;
		assertEquals(1, dao.create(foo2));
		assertEquals(2, qb.countOf());

		String distinct = "DISTINCT(" + Foo.VAL_COLUMN_NAME + ")";
		assertEquals(1, qb.countOf(distinct));

		qb.setCountOf(distinct);
		assertEquals(1, dao.countOf(qb.prepare()));

		distinct = "DISTINCT(" + Foo.ID_COLUMN_NAME + ")";
		assertEquals(2, qb.countOf(distinct));

		qb.setCountOf(distinct);
		assertEquals(2, dao.countOf(qb.prepare()));
	}

	@Test
	public void testUtf8() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);

		Foo foo = new Foo();
		foo.stringField = "اعصاب";
		assertEquals(1, dao.create(foo));

		QueryBuilder<Foo, Integer> qb = dao.queryBuilder();

		List<Foo> results = qb.where().like(Foo.STRING_COLUMN_NAME, '%' + foo.stringField + '%').query();
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(foo.id, results.get(0).id);
		assertEquals(foo.stringField, results.get(0).stringField);

		qb.reset();
		results = qb.where().like(Foo.STRING_COLUMN_NAME, new SelectArg('%' + foo.stringField + '%')).query();
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(foo.id, results.get(0).id);
		assertEquals(foo.stringField, results.get(0).stringField);
	}

	/* ======================================================================================================== */

	private static class LimitInline extends BaseDatabaseType {
		@Override
		public boolean isDatabaseUrlThisType(String url, String dbTypePart) {
			return true;
		}

		@Override
		protected String getDriverClassName() {
			return "foo.bar.baz";
		}

		@Override
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

	protected static class StringColumnArg {
		public static final String ID_FIELD = "id";
		public static final String STR1_FIELD = "str1";
		public static final String STR2_FIELD = "str2";
		@DatabaseField(generatedId = true, columnName = ID_FIELD)
		int id;
		@DatabaseField(columnName = STR1_FIELD)
		String str1;
		@DatabaseField(columnName = STR2_FIELD)
		String str2;

		public StringColumnArg() {
		}
	}

	protected static class One {
		public static final String ID_FIELD = "id";
		public static final String VAL_FIELD = "val";
		@DatabaseField(generatedId = true, columnName = ID_FIELD)
		int id;
		@DatabaseField(columnName = VAL_FIELD)
		int val;

		public One() {
		}
	}

	protected static class Two {
		public static final String ID_FIELD = "id";
		public static final String VAL_FIELD = "val";
		@DatabaseField(generatedId = true, columnName = ID_FIELD)
		int id;
		@DatabaseField(columnName = VAL_FIELD)
		int val;
		@DatabaseField(foreign = true)
		Baz baz;
		@DatabaseField(foreign = true)
		One one;
		@DatabaseField(foreign = true)
		Bar bar;

		public Two() {
		}
	}

	protected static class BaseModel {
		@DatabaseField(generatedId = true)
		int id;
	}

	protected static class Project extends BaseModel {
		// nothing here

		@ForeignCollectionField(eager = false)
		ForeignCollection<Category> categories;
	}

	protected static class Category extends BaseModel {

		@DatabaseField(canBeNull = false, foreign = true)
		Project project;

		@ForeignCollectionField(eager = false)
		ForeignCollection<Criterion> criteria;

		public Category() {
		}
	}

	protected static class Criterion extends BaseModel {
		@DatabaseField
		boolean active;

		@DatabaseField
		Integer weight;

		@DatabaseField(canBeNull = false, foreign = true)
		Category category;

		public Criterion() {
		}
	}

	protected static class NoId {
		public static final String FIELD_NAME_STUFF = "stuff";
		@DatabaseField(columnName = FIELD_NAME_STUFF)
		String stuff;
	}

	protected static class SeralizableNull {
		public static final String FIELD_NAME_SERIALIZABLE = "serializable";
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField(columnName = FIELD_NAME_SERIALIZABLE, dataType = DataType.SERIALIZABLE)
		Serializable serializable;
	}
}
