package com.j256.ormlite.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.stmt.query.SimpleComparison;
import com.j256.ormlite.table.TableInfo;

public class WhereTest extends BaseCoreTest {

	private final static String ID_COLUMN_NAME = "id";
	private final static String STRING_COLUMN_NAME = "stringie";
	private final static String FOREIGN_COLUMN_NAME = "foreign";

	@Test
	public void testToString() throws Exception {
		Where<Foo, String> where = new Where<Foo, String>(createTableInfo(), null, databaseType);
		assertTrue(where.toString().contains("empty where clause"));
		String value = "bar";
		FieldType numberFieldType =
				FieldType.createFieldType(connectionSource, "foo", Foo.class.getDeclaredField(Foo.VAL_COLUMN_NAME),
						Foo.class);
		SimpleComparison eq =
				new SimpleComparison(Foo.VAL_COLUMN_NAME, numberFieldType, value, SimpleComparison.EQUAL_TO_OPERATION);
		where.eq(Foo.VAL_COLUMN_NAME, value);
		assertTrue(where.toString().contains(eq.toString()));
	}

	@Test(expected = IllegalStateException.class)
	public void testAlreadyNeedsClause() throws Exception {
		Where<Foo, String> where = new Where<Foo, String>(createTableInfo(), null, databaseType);
		where.eq(Foo.VAL_COLUMN_NAME, "bar");
		where.and();
		where.and();
		StringBuilder sb = new StringBuilder();
		where.appendSql(null, sb, null);
	}

	@Test(expected = IllegalStateException.class)
	public void testNoClauses() throws Exception {
		Where<Foo, String> where = new Where<Foo, String>(createTableInfo(), null, databaseType);
		where.appendSql(null, new StringBuilder(), new ArrayList<ArgumentHolder>());
	}

	@Test(expected = IllegalStateException.class)
	public void testMissingAndOr() throws Exception {
		Where<Foo, String> where = new Where<Foo, String>(createTableInfo(), null, databaseType);
		int val = 1;
		where.eq(Foo.VAL_COLUMN_NAME, val);
		where.eq(Foo.VAL_COLUMN_NAME, val);
		where.appendSql(null, new StringBuilder(), new ArrayList<ArgumentHolder>());
	}

	@Test(expected = IllegalStateException.class)
	public void testMissingClause() throws Exception {
		Where<Foo, String> where = new Where<Foo, String>(createTableInfo(), null, databaseType);
		int val = 1;
		where.and();
		where.eq(Foo.VAL_COLUMN_NAME, val);
		where.appendSql(null, new StringBuilder(), new ArrayList<ArgumentHolder>());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testComparisonUnknownField() throws Exception {
		Where<Foo, String> where = new Where<Foo, String>(createTableInfo(), null, databaseType);
		int val = 1;
		where.eq("unknown-field", val);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testComparisonFieldNameNotColumnName() throws Exception {
		Where<Foo, String> where = new Where<Foo, String>(createTableInfo(), null, databaseType);
		assertNotNull(Foo.class.getDeclaredField(Foo.ID_COLUMN_NAME));
		int val = 1;
		where.eq("stringField", val);
	}

	@Test
	public void testAndInline() throws Exception {
		Where<Foo, String> where = new Where<Foo, String>(createTableInfo(), null, databaseType);
		int val = 1;
		where.eq(Foo.VAL_COLUMN_NAME, val);
		where.and();
		where.eq(Foo.VAL_COLUMN_NAME, val);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(null, whereSb, new ArrayList<ArgumentHolder>());
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" = ").append(val);
		sb.append(" AND ");
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" = ").append(val);
		sb.append(" ) ");
		assertEquals(sb.toString(), whereSb.toString());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testAndRemoveClauses() throws Exception {
		Where<Foo, String> where = new Where<Foo, String>(createTableInfo(), null, databaseType);
		int val = 1;
		where.and(where.eq(Foo.VAL_COLUMN_NAME, val), where.eq(Foo.VAL_COLUMN_NAME, val));
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(null, whereSb, new ArrayList<ArgumentHolder>());
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" = ").append(val);
		sb.append(" AND ");
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" = ").append(val);
		sb.append(" ) ");
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test
	public void testBetween() throws Exception {
		Where<Foo, String> where = new Where<Foo, String>(createTableInfo(), null, databaseType);
		int low = 1;
		int high = 1;
		where.between(Foo.VAL_COLUMN_NAME, low, high);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(null, whereSb, new ArrayList<ArgumentHolder>());
		StringBuilder sb = new StringBuilder();
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" BETWEEN ").append(low);
		sb.append(" AND ").append(high);
		sb.append(' ');
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test
	public void testEq() throws Exception {
		Where<Foo, String> where = new Where<Foo, String>(createTableInfo(), null, databaseType);
		int val = 112;
		where.eq(Foo.VAL_COLUMN_NAME, val);
		testOperation(where, Foo.VAL_COLUMN_NAME, "=", val);
	}

	@Test
	public void testGe() throws Exception {
		Where<Foo, String> where = new Where<Foo, String>(createTableInfo(), null, databaseType);
		int val = 112;
		where.ge(Foo.VAL_COLUMN_NAME, val);
		testOperation(where, Foo.VAL_COLUMN_NAME, ">=", val);
	}

	@Test
	public void testGt() throws Exception {
		Where<Foo, String> where = new Where<Foo, String>(createTableInfo(), null, databaseType);
		int val = 112;
		where.gt(Foo.VAL_COLUMN_NAME, val);
		testOperation(where, Foo.VAL_COLUMN_NAME, ">", val);
	}

	@Test
	public void testLt() throws Exception {
		Where<Foo, String> where = new Where<Foo, String>(createTableInfo(), null, databaseType);
		int val = 112;
		where.lt(Foo.VAL_COLUMN_NAME, val);
		testOperation(where, Foo.VAL_COLUMN_NAME, "<", val);
	}

	@Test
	public void testLe() throws Exception {
		Where<Foo, String> where = new Where<Foo, String>(createTableInfo(), null, databaseType);
		int val = 112;
		where.le(Foo.VAL_COLUMN_NAME, val);
		testOperation(where, Foo.VAL_COLUMN_NAME, "<=", val);
	}

	@Test
	public void testNe() throws Exception {
		Where<Foo, String> where = new Where<Foo, String>(createTableInfo(), null, databaseType);
		int val = 112;
		where.ne(Foo.VAL_COLUMN_NAME, val);
		testOperation(where, Foo.VAL_COLUMN_NAME, "<>", val);
	}

	@Test
	public void testIn() throws Exception {
		Where<Foo, String> where = new Where<Foo, String>(createTableInfo(), null, databaseType);
		int val = 112;
		where.in(Foo.VAL_COLUMN_NAME, val);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(null, whereSb, new ArrayList<ArgumentHolder>());
		StringBuilder sb = new StringBuilder();
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" IN (");
		sb.append(val).append(" ) ");
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test
	public void testNotIn() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		foo1.val = 63465365;
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		foo2 = new Foo();
		foo2.val = 163123;
		assertEquals(1, dao.create(foo2));

		List<Foo> results = dao.queryBuilder().where().in(Foo.ID_COLUMN_NAME, foo2.id).query();
		assertEquals(1, results.size());
		assertEquals(foo2.val, results.get(0).val);

		// support not with in
		results = dao.queryBuilder().where().not().in(Foo.ID_COLUMN_NAME, foo2.id).query();
		assertEquals(1, results.size());
		assertEquals(foo1.val, results.get(0).val);

		// support not in
		results = dao.queryBuilder().where().notIn(Foo.ID_COLUMN_NAME, foo2.id).query();
		assertEquals(1, results.size());
		assertEquals(foo1.val, results.get(0).val);
	}

	@Test
	public void testInMany() throws Exception {
		Where<Foo, String> where = new Where<Foo, String>(createTableInfo(), null, databaseType);
		int[] vals = new int[] { 112, 123, 61 };
		// NOTE: we can't pass in vals here
		where.in(Foo.VAL_COLUMN_NAME, vals[0], vals[1], vals[2]);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(null, whereSb, new ArrayList<ArgumentHolder>());
		StringBuilder sb = new StringBuilder();
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" IN (");
		for (int valC = 0; valC < vals.length; valC++) {
			if (valC > 0) {
				sb.append(',');
			}
			sb.append(vals[valC]).append(' ');
		}
		sb.append(") ");
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test
	public void testInManyist() throws Exception {
		Where<Foo, String> where = new Where<Foo, String>(createTableInfo(), null, databaseType);
		List<Integer> vals = new ArrayList<Integer>();
		vals.add(112);
		vals.add(123);
		vals.add(61);
		// NOTE: we can't pass in vals here
		where.in(Foo.VAL_COLUMN_NAME, vals);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(null, whereSb, new ArrayList<ArgumentHolder>());
		StringBuilder sb = new StringBuilder();
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" IN (");
		for (int valC = 0; valC < vals.size(); valC++) {
			if (valC > 0) {
				sb.append(',');
			}
			sb.append(vals.get(valC)).append(' ');
		}
		sb.append(") ");
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test
	public void testIsNull() throws Exception {
		Where<Foo, String> where = new Where<Foo, String>(createTableInfo(), null, databaseType);
		where.isNull(Foo.VAL_COLUMN_NAME);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(null, whereSb, new ArrayList<ArgumentHolder>());
		StringBuilder sb = new StringBuilder();
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" IS NULL ");
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test
	public void testIsNotNull() throws Exception {
		Where<Foo, String> where = new Where<Foo, String>(createTableInfo(), null, databaseType);
		where.isNotNull(Foo.VAL_COLUMN_NAME);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(null, whereSb, new ArrayList<ArgumentHolder>());
		StringBuilder sb = new StringBuilder();
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" IS NOT NULL ");
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInArrayWithinArray() throws Exception {
		Where<Foo, String> where = new Where<Foo, String>(createTableInfo(), null, databaseType);
		// NOTE: we can't pass in vals here
		where.in(Foo.VAL_COLUMN_NAME, new int[] { 112 });
	}

	@Test
	public void testLike() throws Exception {
		Where<Foo, String> where = new Where<Foo, String>(createTableInfo(), null, databaseType);
		int val = 112;
		where.like(Foo.VAL_COLUMN_NAME, val);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(null, whereSb, new ArrayList<ArgumentHolder>());
		StringBuilder sb = new StringBuilder();
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" LIKE ");
		sb.append(val).append(' ');
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test
	public void testNotFuture() throws Exception {
		Where<Foo, String> where = new Where<Foo, String>(createTableInfo(), null, databaseType);
		int val = 112;
		where.not();
		where.like(Foo.VAL_COLUMN_NAME, val);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(null, whereSb, new ArrayList<ArgumentHolder>());
		StringBuilder sb = new StringBuilder();
		sb.append("(NOT ");
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" LIKE ");
		sb.append(val).append(" ) ");
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test
	public void testMultipleFuture() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);

		Foo foo1 = new Foo();
		foo1.val = 123;
		foo1.stringField = "fjewpjfew";
		dao.create(foo1);

		QueryBuilder<Foo, String> qb = dao.queryBuilder();
		Where<Foo, String> where = qb.where();

		where.eq(Foo.VAL_COLUMN_NAME, foo1.val);
		where.and();
		where.not();
		where.like(Foo.STRING_COLUMN_NAME, "hello");
		List<Foo> results = where.query();
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(foo1.id, results.get(0).id);
	}

	@Test
	public void testNotAbsorb() throws Exception {
		Where<Foo, String> where = new Where<Foo, String>(createTableInfo(), null, databaseType);
		int val = 112;
		where.like(Foo.VAL_COLUMN_NAME, val);
		where.not(where);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(null, whereSb, new ArrayList<ArgumentHolder>());
		StringBuilder sb = new StringBuilder();
		sb.append("(NOT ");
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" LIKE ");
		sb.append(val).append(" ) ");
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test
	public void testAndFuture() throws Exception {
		Where<Foo, String> where = new Where<Foo, String>(createTableInfo(), null, databaseType);
		int val = 112;
		where.eq(Foo.VAL_COLUMN_NAME, val);
		where.and();
		where.eq(Foo.VAL_COLUMN_NAME, val);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(null, whereSb, new ArrayList<ArgumentHolder>());
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" = ").append(val);
		sb.append(" AND ");
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" = ").append(val);
		sb.append(" ) ");
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test
	public void testOrFuture() throws Exception {
		Where<Foo, String> where = new Where<Foo, String>(createTableInfo(), null, databaseType);
		int val = 112;
		where.eq(Foo.VAL_COLUMN_NAME, val);
		where.or();
		where.eq(Foo.VAL_COLUMN_NAME, val);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(null, whereSb, new ArrayList<ArgumentHolder>());
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" = ").append(val);
		sb.append(" OR ");
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" = ").append(val);
		sb.append(" ) ");
		assertEquals(sb.toString(), whereSb.toString());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testOrAbsorb() throws Exception {
		Where<Foo, String> where = new Where<Foo, String>(createTableInfo(), null, databaseType);
		int val = 112;
		where.or(where.eq(Foo.VAL_COLUMN_NAME, val), where.eq(Foo.VAL_COLUMN_NAME, val));
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(null, whereSb, new ArrayList<ArgumentHolder>());
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" = ").append(val);
		sb.append(" OR ");
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" = ").append(val);
		sb.append(" ) ");
		assertEquals(sb.toString(), whereSb.toString());
	}

	private void testOperation(Where<Foo, String> where, String columnName, String operation, Object value)
			throws Exception {
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(null, whereSb, new ArrayList<ArgumentHolder>());
		StringBuilder sb = new StringBuilder();
		databaseType.appendEscapedEntityName(sb, columnName);
		sb.append(' ').append(operation).append(' ');
		sb.append(value).append(' ');
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test
	public void testIdEq() throws Exception {
		Where<FooId, Integer> where =
				new Where<FooId, Integer>(new TableInfo<FooId, Integer>(connectionSource, null, FooId.class), null,
						databaseType);
		int val = 112;
		where.idEq(val);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(null, whereSb, new ArrayList<ArgumentHolder>());
		StringBuilder sb = new StringBuilder();
		databaseType.appendEscapedEntityName(sb, ID_COLUMN_NAME);
		sb.append(" = ").append(val);
		sb.append(' ');
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test(expected = SQLException.class)
	public void testIdEqNoId() throws Exception {
		new Where<FooNoId, Integer>(new TableInfo<FooNoId, Integer>(connectionSource, null, FooNoId.class), null,
				databaseType).idEq(100);
	}

	@Test
	public void testIdEqObjectId() throws Exception {
		FooId foo = new FooId();
		int id = 112132;
		foo.id = id;
		Where<FooId, Integer> where =
				new Where<FooId, Integer>(new TableInfo<FooId, Integer>(connectionSource, null, FooId.class), null,
						databaseType);
		BaseDaoImpl<FooId, Integer> fooDao = new BaseDaoImpl<FooId, Integer>(connectionSource, FooId.class) {
		};
		where.idEq(fooDao, foo);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(null, whereSb, new ArrayList<ArgumentHolder>());
		StringBuilder sb = new StringBuilder();
		databaseType.appendEscapedEntityName(sb, ID_COLUMN_NAME);
		sb.append(" = ").append(id);
		sb.append(' ');
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test(expected = SQLException.class)
	public void testIdEqObjectIdNoId() throws Exception {
		new Where<FooNoId, Integer>(new TableInfo<FooNoId, Integer>(connectionSource, null, FooNoId.class), null,
				databaseType).idEq(new BaseDaoImpl<FooNoId, Integer>(connectionSource, FooNoId.class) {
		}, new FooNoId());
	}

	@Test
	public void testInSubQuery() throws Exception {
		TableInfo<ForeignFoo, Integer> tableInfo =
				new TableInfo<ForeignFoo, Integer>(connectionSource, null, ForeignFoo.class);
		Where<ForeignFoo, Integer> where = new Where<ForeignFoo, Integer>(tableInfo, null, databaseType);
		BaseDaoImpl<ForeignFoo, Integer> foreignDao =
				new BaseDaoImpl<ForeignFoo, Integer>(connectionSource, ForeignFoo.class) {
				};
		QueryBuilder<ForeignFoo, Integer> qb = foreignDao.queryBuilder();
		qb.selectColumns(ID_COLUMN_NAME);
		where.in(ID_COLUMN_NAME, qb);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(null, whereSb, new ArrayList<ArgumentHolder>());
		StringBuilder sb = new StringBuilder();
		databaseType.appendEscapedEntityName(sb, ID_COLUMN_NAME);
		sb.append(" IN (");
		sb.append("SELECT ");
		databaseType.appendEscapedEntityName(sb, ID_COLUMN_NAME);
		sb.append(" FROM ");
		databaseType.appendEscapedEntityName(sb, tableInfo.getTableName());
		sb.append(" ) ");
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test
	public void testInSubQueryForReal() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo1 = new Foo();
		foo1.val = 785463547;
		assertEquals(1, dao.create(foo1));
		Foo foo2 = new Foo();
		foo2.val = 163547;
		assertEquals(1, dao.create(foo2));

		QueryBuilder<Foo, Integer> qb = dao.queryBuilder();
		qb.selectColumns(Foo.ID_COLUMN_NAME);
		qb.where().eq(Foo.VAL_COLUMN_NAME, foo2.val);
		List<Foo> results = dao.queryBuilder().where().in(Foo.ID_COLUMN_NAME, qb).query();
		assertEquals(1, results.size());
		assertEquals(foo2.val, results.get(0).val);

		// test not in with sub query
		results = dao.queryBuilder().where().notIn(Foo.ID_COLUMN_NAME, qb).query();
		assertEquals(1, results.size());
		assertEquals(foo1.val, results.get(0).val);
	}

	@Test(expected = SQLException.class)
	public void testInSubQueryToManySubColumns() throws Exception {
		TableInfo<ForeignFoo, Integer> tableInfo =
				new TableInfo<ForeignFoo, Integer>(connectionSource, null, ForeignFoo.class);
		Where<ForeignFoo, Integer> where = new Where<ForeignFoo, Integer>(tableInfo, null, databaseType);
		BaseDaoImpl<ForeignFoo, Integer> foreignDao =
				new BaseDaoImpl<ForeignFoo, Integer>(connectionSource, ForeignFoo.class) {
				};
		QueryBuilder<ForeignFoo, Integer> qb = foreignDao.queryBuilder();
		qb.selectColumns(ID_COLUMN_NAME, FOREIGN_COLUMN_NAME);
		where.in(ID_COLUMN_NAME, qb);
	}

	@Test
	public void testExistsSubQuery() throws Exception {
		TableInfo<ForeignFoo, Integer> tableInfo =
				new TableInfo<ForeignFoo, Integer>(connectionSource, null, ForeignFoo.class);
		Where<ForeignFoo, Integer> where = new Where<ForeignFoo, Integer>(tableInfo, null, databaseType);
		BaseDaoImpl<ForeignFoo, Integer> foreignDao =
				new BaseDaoImpl<ForeignFoo, Integer>(connectionSource, ForeignFoo.class) {
				};
		QueryBuilder<ForeignFoo, Integer> qb = foreignDao.queryBuilder();
		where.exists(qb);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(null, whereSb, new ArrayList<ArgumentHolder>());
		StringBuilder sb = new StringBuilder();
		sb.append("EXISTS (");
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, tableInfo.getTableName());
		sb.append(" ) ");
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test
	public void testNotExistsSubQuery() throws Exception {
		TableInfo<ForeignFoo, Integer> tableInfo =
				new TableInfo<ForeignFoo, Integer>(connectionSource, null, ForeignFoo.class);
		Where<ForeignFoo, Integer> where = new Where<ForeignFoo, Integer>(tableInfo, null, databaseType);
		BaseDaoImpl<ForeignFoo, Integer> foreignDao =
				new BaseDaoImpl<ForeignFoo, Integer>(connectionSource, ForeignFoo.class) {
				};
		QueryBuilder<ForeignFoo, Integer> qb = foreignDao.queryBuilder();
		where.not().exists(qb);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(null, whereSb, new ArrayList<ArgumentHolder>());
		StringBuilder sb = new StringBuilder();
		sb.append("(NOT EXISTS (");
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, tableInfo.getTableName());
		sb.append(" ) ) ");
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test
	public void testRaw() throws Exception {
		TableInfo<Foo, Integer> tableInfo = new TableInfo<Foo, Integer>(connectionSource, null, Foo.class);
		Where<Foo, Integer> where = new Where<Foo, Integer>(tableInfo, null, databaseType);
		String raw = "VAL = 1";
		int val = 17;
		where.eq(Foo.VAL_COLUMN_NAME, val).and().raw(raw);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(null, whereSb, new ArrayList<ArgumentHolder>());
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" = ").append(val);
		sb.append(" AND ").append(raw).append(" ) ");
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test
	public void testRawComparison() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		int val = 63465365;
		foo.val = val;
		assertEquals(1, dao.create(foo));

		QueryBuilder<Foo, Integer> qb = dao.queryBuilder();
		qb.where().rawComparison("id", "=", new SelectArg(foo.id));
		List<Foo> results = qb.query();
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(val, results.get(0).val);
	}

	@Test
	public void testRawArgsColumn() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		int val = 63465365;
		foo.val = val;
		assertEquals(1, dao.create(foo));

		QueryBuilder<Foo, String> qb = dao.queryBuilder();
		qb.where().raw(Foo.ID_COLUMN_NAME + " = ?", new SelectArg(Foo.ID_COLUMN_NAME, foo.id));
		List<Foo> results = qb.query();
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(val, results.get(0).val);
	}

	@Test
	public void testRawArgsColumnNoValue() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		int val = 63465365;
		foo.val = val;
		assertEquals(1, dao.create(foo));

		QueryBuilder<Foo, String> qb = dao.queryBuilder();
		SelectArg arg = new SelectArg(Foo.ID_COLUMN_NAME, null);
		qb.where().raw(Foo.ID_COLUMN_NAME + " = ?", arg);
		arg.setValue(foo.id);
		List<Foo> results = qb.query();
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(val, results.get(0).val);
	}

	@Test
	public void testRawArgsColumnSqlType() throws Exception {
		Dao<Foo, Integer> dao = createDao(Foo.class, true);
		Foo foo = new Foo();
		int val = 63465365;
		foo.val = val;
		assertEquals(1, dao.create(foo));

		QueryBuilder<Foo, Integer> qb = dao.queryBuilder();
		qb.where().raw(Foo.ID_COLUMN_NAME + " = ? and " + val + " = ?", new SelectArg(SqlType.STRING, foo.id),
				new SelectArg(SqlType.INTEGER, val));
		List<Foo> results = qb.query();
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(val, results.get(0).val);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRawArgsNoColumnName() throws Exception {
		Dao<Foo, String> dao = createDao(Foo.class, true);
		dao.queryBuilder().where().raw("id = ?", new SelectArg(7));
	}

	@Test
	public void testClear() throws Exception {
		Where<Foo, String> where = new Where<Foo, String>(createTableInfo(), null, databaseType);
		int val = 112;
		where.eq(Foo.VAL_COLUMN_NAME, val);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(null, whereSb, new ArrayList<ArgumentHolder>());
		StringBuilder sb = new StringBuilder();
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" = ");
		sb.append(val).append(' ');
		assertEquals(sb.toString(), whereSb.toString());

		where.reset();
		whereSb.setLength(0);
		where.eq(Foo.VAL_COLUMN_NAME, val + 1);
		where.appendSql(null, whereSb, new ArrayList<ArgumentHolder>());
		sb.setLength(0);
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" = ");
		sb.append(val + 1).append(' ');
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testAnyMany() throws Exception {
		int val1 = 111;
		int val2 = 112;
		int val3 = 113;
		int val4 = 114;
		Where<Foo, String> where = new Where<Foo, String>(createTableInfo(), null, databaseType);
		where.and(where.eq(Foo.VAL_COLUMN_NAME, val1), where.eq(Foo.VAL_COLUMN_NAME, val2),
				where.eq(Foo.VAL_COLUMN_NAME, val3), where.eq(Foo.VAL_COLUMN_NAME, val4));
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(null, whereSb, new ArrayList<ArgumentHolder>());
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		// NOTE: they are done in reverse order
		sb.append(" = ").append(val1);
		sb.append(" AND ");
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" = ").append(val2);
		sb.append(" AND ");
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" = ").append(val3);
		sb.append(" AND ");
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" = ").append(val4);
		sb.append(" ) ");
		assertEquals(sb.toString(), whereSb.toString());

		where.reset();
		where.eq(Foo.VAL_COLUMN_NAME, val1);
		where.eq(Foo.VAL_COLUMN_NAME, val2);
		where.eq(Foo.VAL_COLUMN_NAME, val3);
		where.eq(Foo.VAL_COLUMN_NAME, val4);
		where.and(4);
		whereSb = new StringBuilder();
		where.appendSql(null, whereSb, new ArrayList<ArgumentHolder>());
		sb = new StringBuilder();
		sb.append('(');
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		// NOTE: they are done in reverse order
		sb.append(" = ").append(val1);
		sb.append(" AND ");
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" = ").append(val2);
		sb.append(" AND ");
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" = ").append(val3);
		sb.append(" AND ");
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" = ").append(val4);
		sb.append(" ) ");
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testOrMany() throws Exception {
		Where<Foo, String> where = new Where<Foo, String>(createTableInfo(), null, databaseType);
		int val1 = 111;
		int val2 = 112;
		int val3 = 113;
		int val4 = 114;
		where.or(where.eq(Foo.VAL_COLUMN_NAME, val1), where.eq(Foo.VAL_COLUMN_NAME, val2),
				where.eq(Foo.VAL_COLUMN_NAME, val3), where.eq(Foo.VAL_COLUMN_NAME, val4));
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(null, whereSb, new ArrayList<ArgumentHolder>());
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		// NOTE: they are done in reverse order
		sb.append(" = ").append(val1);
		sb.append(" OR ");
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" = ").append(val2);
		sb.append(" OR ");
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" = ").append(val3);
		sb.append(" OR ");
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" = ").append(val4);
		sb.append(" ) ");
		assertEquals(sb.toString(), whereSb.toString());

		where = new Where<Foo, String>(createTableInfo(), null, databaseType);
		where.eq(Foo.VAL_COLUMN_NAME, val1);
		where.eq(Foo.VAL_COLUMN_NAME, val2);
		where.eq(Foo.VAL_COLUMN_NAME, val3);
		where.eq(Foo.VAL_COLUMN_NAME, val4);
		where.or(4);
		whereSb = new StringBuilder();
		where.appendSql(null, whereSb, new ArrayList<ArgumentHolder>());
		sb = new StringBuilder();
		sb.append('(');
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		// NOTE: they are done in reverse order
		sb.append(" = ").append(val1);
		sb.append(" OR ");
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" = ").append(val2);
		sb.append(" OR ");
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" = ").append(val3);
		sb.append(" OR ");
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" = ").append(val4);
		sb.append(" ) ");
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testOrManyZero() throws Exception {
		Where<Foo, String> where = new Where<Foo, String>(createTableInfo(), null, databaseType);
		where.or(0);
	}

	@Test
	public void testOrManyOne() throws Exception {
		Where<Foo, String> where = new Where<Foo, String>(createTableInfo(), null, databaseType);
		int val1 = 1312313;
		where.eq(Foo.VAL_COLUMN_NAME, val1);
		where.or(1);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(null, whereSb, new ArrayList<ArgumentHolder>());
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		// NOTE: they are done in reverse order
		sb.append(" = ").append(val1);
		sb.append(" ) ");
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAndManyZero() throws Exception {
		Where<Foo, String> where = new Where<Foo, String>(createTableInfo(), null, databaseType);
		where.and(0);
	}

	@Test
	public void testAndManyOne() throws Exception {
		Where<Foo, String> where = new Where<Foo, String>(createTableInfo(), null, databaseType);
		int val1 = 1312313;
		where.eq(Foo.VAL_COLUMN_NAME, val1);
		where.and(1);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(null, whereSb, new ArrayList<ArgumentHolder>());
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		// NOTE: they are done in reverse order
		sb.append(" = ").append(val1);
		sb.append(" ) ");
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInnerQuerySubQueryWhere() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);

		fooDao.queryBuilder().where().in(Foo.ID_COLUMN_NAME,
		// this is a problem because eq() returns a Where not a QueryBuilder
				fooDao.queryBuilder().selectColumns(Foo.ID_COLUMN_NAME).where().eq(Foo.ID_COLUMN_NAME, 1)).query();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInnerQuerySubQueryPrepared() throws Exception {
		Dao<Foo, Integer> fooDao = createDao(Foo.class, true);

		fooDao.queryBuilder().where().in(Foo.ID_COLUMN_NAME,
		// this is a problem because prepare() returns a PreparedStmt not a QueryBuilder
				fooDao.queryBuilder().selectColumns(Foo.ID_COLUMN_NAME).prepare()).query();
	}

	@Test
	public void testDateBetween() throws Exception {
		Dao<DateBetween, Object> dao = createDao(DateBetween.class, true);

		DateBetween dateBetween = new DateBetween();
		long now = System.currentTimeMillis();
		Date date = new Date(now);
		dateBetween.date = date;
		assertEquals(1, dao.create(dateBetween));

		QueryBuilder<DateBetween, Object> qb = dao.queryBuilder();
		qb.where().between(DateBetween.DATE_COLUMN_NAME, new Date(now - 1), new Date(now + 1));
		List<DateBetween> results = qb.query();
		assertEquals(1, results.size());

		qb.where().reset();
		qb.where().between(DateBetween.DATE_COLUMN_NAME, new Date(now), new Date(now + 1));
		results = qb.query();
		assertEquals(1, results.size());

		qb.where().reset();
		qb.where().between(DateBetween.DATE_COLUMN_NAME, new Date(now), new Date(now));
		results = qb.query();
		assertEquals(1, results.size());
	}

	private TableInfo<Foo, String> createTableInfo() throws SQLException {
		return new TableInfo<Foo, String>(connectionSource, null, Foo.class);
	}

	protected static class FooNoId {
		@DatabaseField(columnName = STRING_COLUMN_NAME)
		String string;
		FooNoId() {
		}
	}

	protected static class FooId {
		@DatabaseField(id = true, columnName = ID_COLUMN_NAME)
		int id;
		@DatabaseField(columnName = STRING_COLUMN_NAME)
		String string;
		FooId() {
		}
	}

	protected static class ForeignFoo {
		@DatabaseField(id = true, columnName = ID_COLUMN_NAME)
		int id;
		@DatabaseField(foreign = true, columnName = FOREIGN_COLUMN_NAME)
		FooId foo;
		ForeignFoo() {
		}
	}

	protected static class DateBetween {
		public static final String DATE_COLUMN_NAME = "date";
		@DatabaseField(id = true, columnName = ID_COLUMN_NAME)
		int id;
		@DatabaseField(columnName = DATE_COLUMN_NAME, dataType = DataType.DATE_LONG)
		Date date;
		DateBetween() {
		}
	}
}
