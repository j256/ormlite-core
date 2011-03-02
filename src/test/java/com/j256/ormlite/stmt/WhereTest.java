package com.j256.ormlite.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.stmt.query.Eq;
import com.j256.ormlite.table.TableInfo;

public class WhereTest extends BaseCoreTest {

	private final static String ID_COLUMN_NAME = "id";
	private final static String STRING_COLUMN_NAME = "stringie";
	private final static String FOREIGN_COLUMN_NAME = "foreign";

	@Test
	public void testToString() throws Exception {
		Where<Foo, Void> where = new Where<Foo, Void>(createTableInfo(), null);
		assertTrue(where.toString().contains("empty where clause"));
		String value = "bar";
		FieldType numberFieldType =
				FieldType.createFieldType(connectionSource, "foo", Foo.class.getDeclaredField(Foo.VAL_COLUMN_NAME), 0);
		Eq eq = new Eq(Foo.VAL_COLUMN_NAME, numberFieldType, value);
		where.eq(Foo.VAL_COLUMN_NAME, value);
		assertTrue(where.toString().contains(eq.toString()));
	}

	@Test(expected = IllegalStateException.class)
	public void testAlreadyNeedsClause() throws Exception {
		Where<Foo, Void> where = new Where<Foo, Void>(createTableInfo(), null);
		where.eq(Foo.VAL_COLUMN_NAME, "bar");
		where.and();
		where.and();
	}

	@Test(expected = IllegalStateException.class)
	public void testNoClauses() throws Exception {
		Where<Foo, Void> where = new Where<Foo, Void>(createTableInfo(), null);
		where.appendSql(databaseType, new StringBuilder(), new ArrayList<SelectArg>());
	}

	@Test(expected = IllegalStateException.class)
	public void testMissingAndOr() throws Exception {
		Where<Foo, Void> where = new Where<Foo, Void>(createTableInfo(), null);
		int val = 1;
		where.eq(Foo.VAL_COLUMN_NAME, val);
		where.eq(Foo.VAL_COLUMN_NAME, val);
		where.appendSql(databaseType, new StringBuilder(), new ArrayList<SelectArg>());
	}

	@Test(expected = IllegalStateException.class)
	public void testMissingClause() throws Exception {
		Where<Foo, Void> where = new Where<Foo, Void>(createTableInfo(), null);
		int val = 1;
		where.and();
		where.eq(Foo.VAL_COLUMN_NAME, val);
		where.appendSql(databaseType, new StringBuilder(), new ArrayList<SelectArg>());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testComparisonUnknownField() throws Exception {
		Where<Foo, Void> where = new Where<Foo, Void>(createTableInfo(), null);
		int val = 1;
		where.eq("unknown-field", val);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testComparisonFieldNameNotColumnName() throws Exception {
		Where<Foo, Void> where = new Where<Foo, Void>(createTableInfo(), null);
		assertNotNull(Foo.class.getDeclaredField(Foo.ID_COLUMN_NAME));
		int val = 1;
		where.eq("string", val);
	}

	@Test
	public void testAndInline() throws Exception {
		Where<Foo, Void> where = new Where<Foo, Void>(createTableInfo(), null);
		int val = 1;
		where.eq(Foo.VAL_COLUMN_NAME, val);
		where.and();
		where.eq(Foo.VAL_COLUMN_NAME, val);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(databaseType, whereSb, new ArrayList<SelectArg>());
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
	public void testAndRemoveClauses() throws Exception {
		Where<Foo, Void> where = new Where<Foo, Void>(createTableInfo(), null);
		int val = 1;
		where.and(where.eq(Foo.VAL_COLUMN_NAME, val), where.eq(Foo.VAL_COLUMN_NAME, val));
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(databaseType, whereSb, new ArrayList<SelectArg>());
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
		Where<Foo, Void> where = new Where<Foo, Void>(createTableInfo(), null);
		int low = 1;
		int high = 1;
		where.between(Foo.VAL_COLUMN_NAME, low, high);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(databaseType, whereSb, new ArrayList<SelectArg>());
		StringBuilder sb = new StringBuilder();
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" BETWEEN ").append(low);
		sb.append(" AND ").append(high);
		sb.append(' ');
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test
	public void testEq() throws Exception {
		Where<Foo, Void> where = new Where<Foo, Void>(createTableInfo(), null);
		int val = 112;
		where.eq(Foo.VAL_COLUMN_NAME, val);
		testOperation(where, Foo.VAL_COLUMN_NAME, "=", val);
	}

	@Test
	public void testGe() throws Exception {
		Where<Foo, Void> where = new Where<Foo, Void>(createTableInfo(), null);
		int val = 112;
		where.ge(Foo.VAL_COLUMN_NAME, val);
		testOperation(where, Foo.VAL_COLUMN_NAME, ">=", val);
	}

	@Test
	public void testGt() throws Exception {
		Where<Foo, Void> where = new Where<Foo, Void>(createTableInfo(), null);
		int val = 112;
		where.gt(Foo.VAL_COLUMN_NAME, val);
		testOperation(where, Foo.VAL_COLUMN_NAME, ">", val);
	}

	@Test
	public void testLt() throws Exception {
		Where<Foo, Void> where = new Where<Foo, Void>(createTableInfo(), null);
		int val = 112;
		where.lt(Foo.VAL_COLUMN_NAME, val);
		testOperation(where, Foo.VAL_COLUMN_NAME, "<", val);
	}

	@Test
	public void testLe() throws Exception {
		Where<Foo, Void> where = new Where<Foo, Void>(createTableInfo(), null);
		int val = 112;
		where.le(Foo.VAL_COLUMN_NAME, val);
		testOperation(where, Foo.VAL_COLUMN_NAME, "<=", val);
	}

	@Test
	public void testNe() throws Exception {
		Where<Foo, Void> where = new Where<Foo, Void>(createTableInfo(), null);
		int val = 112;
		where.ne(Foo.VAL_COLUMN_NAME, val);
		testOperation(where, Foo.VAL_COLUMN_NAME, "<>", val);
	}

	@Test
	public void testIn() throws Exception {
		Where<Foo, Void> where = new Where<Foo, Void>(createTableInfo(), null);
		int val = 112;
		where.in(Foo.VAL_COLUMN_NAME, val);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(databaseType, whereSb, new ArrayList<SelectArg>());
		StringBuilder sb = new StringBuilder();
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" IN (");
		sb.append(val).append(" ) ");
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test
	public void testInMany() throws Exception {
		Where<Foo, Void> where = new Where<Foo, Void>(createTableInfo(), null);
		int[] vals = new int[] { 112, 123, 61 };
		// NOTE: we can't pass in vals here
		where.in(Foo.VAL_COLUMN_NAME, vals[0], vals[1], vals[2]);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(databaseType, whereSb, new ArrayList<SelectArg>());
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
		Where<Foo, Void> where = new Where<Foo, Void>(createTableInfo(), null);
		List<Integer> vals = new ArrayList<Integer>();
		vals.add(112);
		vals.add(123);
		vals.add(61);
		// NOTE: we can't pass in vals here
		where.in(Foo.VAL_COLUMN_NAME, vals);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(databaseType, whereSb, new ArrayList<SelectArg>());
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
		Where<Foo, Void> where = new Where<Foo, Void>(createTableInfo(), null);
		where.isNull(Foo.VAL_COLUMN_NAME);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(databaseType, whereSb, new ArrayList<SelectArg>());
		StringBuilder sb = new StringBuilder();
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" IS NULL ");
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test
	public void testIsNotNull() throws Exception {
		Where<Foo, Void> where = new Where<Foo, Void>(createTableInfo(), null);
		where.isNotNull(Foo.VAL_COLUMN_NAME);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(databaseType, whereSb, new ArrayList<SelectArg>());
		StringBuilder sb = new StringBuilder();
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" IS NOT NULL ");
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInArrayWithinArray() throws Exception {
		Where<Foo, Void> where = new Where<Foo, Void>(createTableInfo(), null);
		// NOTE: we can't pass in vals here
		where.in(Foo.VAL_COLUMN_NAME, new int[] { 112 });
	}

	@Test
	public void testLike() throws Exception {
		Where<Foo, Void> where = new Where<Foo, Void>(createTableInfo(), null);
		int val = 112;
		where.like(Foo.VAL_COLUMN_NAME, val);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(databaseType, whereSb, new ArrayList<SelectArg>());
		StringBuilder sb = new StringBuilder();
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" LIKE ");
		sb.append(val).append(' ');
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test
	public void testNotFuture() throws Exception {
		Where<Foo, Void> where = new Where<Foo, Void>(createTableInfo(), null);
		int val = 112;
		where.not();
		where.like(Foo.VAL_COLUMN_NAME, val);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(databaseType, whereSb, new ArrayList<SelectArg>());
		StringBuilder sb = new StringBuilder();
		sb.append("(NOT ");
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" LIKE ");
		sb.append(val).append(" ) ");
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test
	public void testNotAbsorb() throws Exception {
		Where<Foo, Void> where = new Where<Foo, Void>(createTableInfo(), null);
		int val = 112;
		where.like(Foo.VAL_COLUMN_NAME, val);
		where.not(where);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(databaseType, whereSb, new ArrayList<SelectArg>());
		StringBuilder sb = new StringBuilder();
		sb.append("(NOT ");
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" LIKE ");
		sb.append(val).append(" ) ");
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test
	public void testAndFuture() throws Exception {
		Where<Foo, Void> where = new Where<Foo, Void>(createTableInfo(), null);
		int val = 112;
		where.eq(Foo.VAL_COLUMN_NAME, val);
		where.and();
		where.eq(Foo.VAL_COLUMN_NAME, val);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(databaseType, whereSb, new ArrayList<SelectArg>());
		StringBuilder sb = new StringBuilder();
		sb.append("(");
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
		Where<Foo, Void> where = new Where<Foo, Void>(createTableInfo(), null);
		int val = 112;
		where.eq(Foo.VAL_COLUMN_NAME, val);
		where.or();
		where.eq(Foo.VAL_COLUMN_NAME, val);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(databaseType, whereSb, new ArrayList<SelectArg>());
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" = ").append(val);
		sb.append(" OR ");
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" = ").append(val);
		sb.append(" ) ");
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test
	public void testOrAbsorb() throws Exception {
		Where<Foo, Void> where = new Where<Foo, Void>(createTableInfo(), null);
		int val = 112;
		where.or(where.eq(Foo.VAL_COLUMN_NAME, val), where.eq(Foo.VAL_COLUMN_NAME, val));
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(databaseType, whereSb, new ArrayList<SelectArg>());
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" = ").append(val);
		sb.append(" OR ");
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" = ").append(val);
		sb.append(" ) ");
		assertEquals(sb.toString(), whereSb.toString());
	}

	private void testOperation(Where<Foo, Void> where, String columnName, String operation, Object value)
			throws Exception {
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(databaseType, whereSb, new ArrayList<SelectArg>());
		StringBuilder sb = new StringBuilder();
		databaseType.appendEscapedEntityName(sb, columnName);
		sb.append(' ').append(operation).append(' ');
		sb.append(value).append(' ');
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test
	public void testIdEq() throws Exception {
		Where<FooId, Integer> where =
				new Where<FooId, Integer>(new TableInfo<FooId>(connectionSource, FooId.class), null);
		int val = 112;
		where.idEq(val);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(databaseType, whereSb, new ArrayList<SelectArg>());
		StringBuilder sb = new StringBuilder();
		databaseType.appendEscapedEntityName(sb, ID_COLUMN_NAME);
		sb.append(" = ").append(val);
		sb.append(' ');
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test(expected = SQLException.class)
	public void testIdEqNoId() throws Exception {
		new Where<FooNoId, Integer>(new TableInfo<FooNoId>(connectionSource, FooNoId.class), null).idEq(100);
	}

	@Test
	public void testIdEqObjectId() throws Exception {
		FooId foo = new FooId();
		int id = 112132;
		foo.id = id;
		Where<FooId, Integer> where =
				new Where<FooId, Integer>(new TableInfo<FooId>(connectionSource, FooId.class), null);
		BaseDaoImpl<FooId, Integer> fooDao = new BaseDaoImpl<FooId, Integer>(connectionSource, FooId.class) {
		};
		where.idEq(fooDao, foo);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(databaseType, whereSb, new ArrayList<SelectArg>());
		StringBuilder sb = new StringBuilder();
		databaseType.appendEscapedEntityName(sb, ID_COLUMN_NAME);
		sb.append(" = ").append(id);
		sb.append(' ');
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test
	public void testIdEqForiegn() throws Exception {
		FooId foo = new FooId();
		int id = 112132;
		foo.id = id;
		ForeignFoo foreign = new ForeignFoo();
		foreign.foo = foo;
		Where<FooId, Integer> where =
				new Where<FooId, Integer>(new TableInfo<FooId>(connectionSource, FooId.class), null);
		BaseDaoImpl<ForeignFoo, Integer> foreignDao =
				new BaseDaoImpl<ForeignFoo, Integer>(connectionSource, ForeignFoo.class) {
				};
		where.foreignIdEq(foreignDao, foreign);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(databaseType, whereSb, new ArrayList<SelectArg>());
		StringBuilder sb = new StringBuilder();
		databaseType.appendEscapedEntityName(sb, ID_COLUMN_NAME);
		sb.append(" = ").append(id);
		sb.append(' ');
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test(expected = SQLException.class)
	public void testIdEqForiegnNull() throws Exception {
		ForeignFoo foreign1 = new ForeignFoo();
		Where<FooId, Integer> where =
				new Where<FooId, Integer>(new TableInfo<FooId>(connectionSource, FooId.class), null);
		BaseDaoImpl<ForeignFoo, Integer> foreignDao =
				new BaseDaoImpl<ForeignFoo, Integer>(connectionSource, ForeignFoo.class) {
				};
		where.foreignIdEq(foreignDao, foreign1);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(databaseType, whereSb, new ArrayList<SelectArg>());
	}

	@Test(expected = SQLException.class)
	public void testIdEqForeignNoId() throws Exception {
		Where<Foo, Integer> where = new Where<Foo, Integer>(new TableInfo<Foo>(connectionSource, Foo.class), null);
		BaseDaoImpl<Foo, Integer> foreignDao = new BaseDaoImpl<Foo, Integer>(connectionSource, Foo.class) {
		};
		where.foreignIdEq(foreignDao, new Foo());
	}

	@Test(expected = SQLException.class)
	public void testIdEqForiegnNoFieldOfType() throws Exception {
		Where<ForeignFoo, Integer> where =
				new Where<ForeignFoo, Integer>(new TableInfo<ForeignFoo>(connectionSource, ForeignFoo.class), null);
		BaseDaoImpl<ForeignFoo, Integer> foreignDao =
				new BaseDaoImpl<ForeignFoo, Integer>(connectionSource, ForeignFoo.class) {
				};
		where.foreignIdEq(foreignDao, new ForeignFoo());
	}

	@Test
	public void testIdForiegnIn() throws Exception {
		FooId foo1 = new FooId();
		int id1 = 112132;
		foo1.id = id1;
		FooId foo2 = new FooId();
		int id2 = 113413122;
		foo2.id = id2;
		ForeignFoo foreign1 = new ForeignFoo();
		foreign1.foo = foo1;
		ForeignFoo foreign2 = new ForeignFoo();
		foreign2.foo = foo2;
		Where<FooId, Integer> where =
				new Where<FooId, Integer>(new TableInfo<FooId>(connectionSource, FooId.class), null);
		BaseDaoImpl<ForeignFoo, Integer> foreignDao =
				new BaseDaoImpl<ForeignFoo, Integer>(connectionSource, ForeignFoo.class) {
				};
		where.foreignIdIn(foreignDao, foreign1, foreign2);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(databaseType, whereSb, new ArrayList<SelectArg>());
		StringBuilder sb = new StringBuilder();
		databaseType.appendEscapedEntityName(sb, ID_COLUMN_NAME);
		sb.append(" IN (").append(id1);
		sb.append(" ,").append(id2);
		sb.append(" ) ");
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test(expected = SQLException.class)
	public void testIdForeignInNoId() throws Exception {
		Where<Foo, Integer> where = new Where<Foo, Integer>(new TableInfo<Foo>(connectionSource, Foo.class), null);
		BaseDaoImpl<Foo, Integer> foreignDao = new BaseDaoImpl<Foo, Integer>(connectionSource, Foo.class) {
		};
		where.foreignIdIn(foreignDao, new Foo());
	}

	@Test
	public void testIdEqForiegnInIterable() throws Exception {
		FooId foo1 = new FooId();
		int id1 = 112132;
		foo1.id = id1;
		FooId foo2 = new FooId();
		int id2 = 113413122;
		foo2.id = id2;
		ForeignFoo foreign1 = new ForeignFoo();
		foreign1.foo = foo1;
		ForeignFoo foreign2 = new ForeignFoo();
		foreign2.foo = foo2;
		Where<FooId, Integer> where =
				new Where<FooId, Integer>(new TableInfo<FooId>(connectionSource, FooId.class), null);
		BaseDaoImpl<ForeignFoo, Integer> foreignDao =
				new BaseDaoImpl<ForeignFoo, Integer>(connectionSource, ForeignFoo.class) {
				};
		where.foreignIdIn(foreignDao, Arrays.asList(foreign1, foreign2));
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(databaseType, whereSb, new ArrayList<SelectArg>());
		StringBuilder sb = new StringBuilder();
		databaseType.appendEscapedEntityName(sb, ID_COLUMN_NAME);
		sb.append(" IN (").append(id1);
		sb.append(" ,").append(id2);
		sb.append(" ) ");
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test(expected = SQLException.class)
	public void testIdForeignInIterableNoId() throws Exception {
		Where<Foo, Integer> where = new Where<Foo, Integer>(new TableInfo<Foo>(connectionSource, Foo.class), null);
		BaseDaoImpl<Foo, Integer> foreignDao = new BaseDaoImpl<Foo, Integer>(connectionSource, Foo.class) {
		};
		where.foreignIdIn(foreignDao, Arrays.asList(new Foo()));
	}

	@Test(expected = SQLException.class)
	public void testIdEqObjectIdNoId() throws Exception {
		new Where<FooNoId, Integer>(new TableInfo<FooNoId>(connectionSource, FooNoId.class), null).idEq(
				new BaseDaoImpl<FooNoId, Integer>(connectionSource, FooNoId.class) {
				}, new FooNoId());
	}

	@Test
	public void testInSubQuery() throws Exception {
		TableInfo<ForeignFoo> tableInfo = new TableInfo<ForeignFoo>(connectionSource, ForeignFoo.class);
		Where<ForeignFoo, Integer> where = new Where<ForeignFoo, Integer>(tableInfo, null);
		BaseDaoImpl<ForeignFoo, Integer> foreignDao =
				new BaseDaoImpl<ForeignFoo, Integer>(connectionSource, ForeignFoo.class) {
				};
		QueryBuilder<ForeignFoo, Integer> qb = foreignDao.queryBuilder();
		qb.selectColumns(ID_COLUMN_NAME);
		where.in(ID_COLUMN_NAME, qb);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(databaseType, whereSb, new ArrayList<SelectArg>());
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
	public void testExistsSubQuery() throws Exception {
		TableInfo<ForeignFoo> tableInfo = new TableInfo<ForeignFoo>(connectionSource, ForeignFoo.class);
		Where<ForeignFoo, Integer> where = new Where<ForeignFoo, Integer>(tableInfo, null);
		BaseDaoImpl<ForeignFoo, Integer> foreignDao =
				new BaseDaoImpl<ForeignFoo, Integer>(connectionSource, ForeignFoo.class) {
				};
		QueryBuilder<ForeignFoo, Integer> qb = foreignDao.queryBuilder();
		where.exists(qb);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(databaseType, whereSb, new ArrayList<SelectArg>());
		StringBuilder sb = new StringBuilder();
		sb.append("EXISTS (");
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, tableInfo.getTableName());
		sb.append(" ) ");
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test
	public void testNotExistsSubQuery() throws Exception {
		TableInfo<ForeignFoo> tableInfo = new TableInfo<ForeignFoo>(connectionSource, ForeignFoo.class);
		Where<ForeignFoo, Integer> where = new Where<ForeignFoo, Integer>(tableInfo, null);
		BaseDaoImpl<ForeignFoo, Integer> foreignDao =
				new BaseDaoImpl<ForeignFoo, Integer>(connectionSource, ForeignFoo.class) {
				};
		QueryBuilder<ForeignFoo, Integer> qb = foreignDao.queryBuilder();
		where.not().exists(qb);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(databaseType, whereSb, new ArrayList<SelectArg>());
		StringBuilder sb = new StringBuilder();
		sb.append("(NOT EXISTS (");
		sb.append("SELECT * FROM ");
		databaseType.appendEscapedEntityName(sb, tableInfo.getTableName());
		sb.append(" ) ) ");
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test
	public void testRaw() throws Exception {
		TableInfo<Foo> tableInfo = new TableInfo<Foo>(connectionSource, Foo.class);
		Where<Foo, Integer> where = new Where<Foo, Integer>(tableInfo, null);
		String raw = "VAL = 1";
		int val = 17;
		where.eq(Foo.VAL_COLUMN_NAME, val).and().raw(raw);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(databaseType, whereSb, new ArrayList<SelectArg>());
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" = ").append(val);
		sb.append(" AND ").append(raw).append(" ) ");
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test
	public void testClear() throws Exception {
		Where<Foo, Void> where = new Where<Foo, Void>(createTableInfo(), null);
		int val = 112;
		where.eq(Foo.VAL_COLUMN_NAME, val);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(databaseType, whereSb, new ArrayList<SelectArg>());
		StringBuilder sb = new StringBuilder();
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" = ");
		sb.append(val).append(' ');
		assertEquals(sb.toString(), whereSb.toString());

		where.clear();
		whereSb.setLength(0);
		where.eq(Foo.VAL_COLUMN_NAME, val + 1);
		where.appendSql(databaseType, whereSb, new ArrayList<SelectArg>());
		sb.setLength(0);
		databaseType.appendEscapedEntityName(sb, Foo.VAL_COLUMN_NAME);
		sb.append(" = ");
		sb.append(val + 1).append(' ');
		assertEquals(sb.toString(), whereSb.toString());
	}

	private TableInfo<Foo> createTableInfo() throws SQLException {
		return new TableInfo<Foo>(connectionSource, Foo.class);
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
}
