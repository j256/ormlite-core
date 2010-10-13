package com.j256.ormlite.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.j256.ormlite.BaseOrmLiteCoreTest;
import com.j256.ormlite.db.BaseDatabaseType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.query.Eq;
import com.j256.ormlite.table.TableInfo;

public class WhereTest extends BaseOrmLiteCoreTest {

	private final static String COLUMN_NAME = "foo";
	private final static String STRING_COLUMN_NAME = "stringie";

	@Test
	public void testToString() throws Exception {
		Where where = new Where(createTableInfo());
		assertTrue(where.toString().contains("empty where clause"));
		String value = "bar";
		Eq eq = new Eq(COLUMN_NAME, numberFieldType, value);
		where.eq(COLUMN_NAME, value);
		assertTrue(where.toString().contains(eq.toString()));
	}

	@Test(expected = IllegalStateException.class)
	public void testAlreadyNeedsClause() throws Exception {
		Where where = new Where(createTableInfo());
		where.eq(COLUMN_NAME, "bar");
		where.and();
		where.and();
	}

	@Test(expected = IllegalStateException.class)
	public void testNoClauses() throws Exception {
		Where where = new Where(createTableInfo());
		where.appendSql(databaseType, new StringBuilder(), new ArrayList<SelectArg>());
	}

	@Test(expected = IllegalStateException.class)
	public void testMissingAndOr() throws Exception {
		Where where = new Where(createTableInfo());
		int val = 1;
		where.eq(COLUMN_NAME, val);
		where.eq(COLUMN_NAME, val);
		where.appendSql(databaseType, new StringBuilder(), new ArrayList<SelectArg>());
	}

	@Test(expected = IllegalStateException.class)
	public void testMissingClause() throws Exception {
		Where where = new Where(createTableInfo());
		int val = 1;
		where.and();
		where.eq(COLUMN_NAME, val);
		where.appendSql(databaseType, new StringBuilder(), new ArrayList<SelectArg>());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testComparisonUnknownField() throws Exception {
		Where where = new Where(createTableInfo());
		int val = 1;
		where.eq("unknown-field", val);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testComparisonFieldNameNotColumnName() throws Exception {
		Where where = new Where(createTableInfo());
		assertNotNull(Foo.class.getDeclaredField("string"));
		int val = 1;
		where.eq("string", val);
	}

	@Test
	public void testAndInline() throws Exception {
		Where where = new Where(createTableInfo());
		int val = 1;
		where.eq(COLUMN_NAME, val);
		where.and();
		where.eq(COLUMN_NAME, val);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(databaseType, whereSb, new ArrayList<SelectArg>());
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		databaseType.appendEscapedEntityName(sb, COLUMN_NAME);
		sb.append(" = ").append(val);
		sb.append(" AND ");
		databaseType.appendEscapedEntityName(sb, COLUMN_NAME);
		sb.append(" = ").append(val);
		sb.append(" ) ");
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test
	public void testAndRemoveClauses() throws Exception {
		Where where = new Where(createTableInfo());
		int val = 1;
		where.and(where.eq(COLUMN_NAME, val), where.eq(COLUMN_NAME, val));
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(databaseType, whereSb, new ArrayList<SelectArg>());
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		databaseType.appendEscapedEntityName(sb, COLUMN_NAME);
		sb.append(" = ").append(val);
		sb.append(" AND ");
		databaseType.appendEscapedEntityName(sb, COLUMN_NAME);
		sb.append(" = ").append(val);
		sb.append(" ) ");
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test
	public void testBetween() throws Exception {
		Where where = new Where(createTableInfo());
		int low = 1;
		int high = 1;
		where.between(COLUMN_NAME, low, high);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(databaseType, whereSb, new ArrayList<SelectArg>());
		StringBuilder sb = new StringBuilder();
		databaseType.appendEscapedEntityName(sb, COLUMN_NAME);
		sb.append(" BETWEEN ").append(low);
		sb.append(" AND ").append(high);
		sb.append(' ');
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test
	public void testEq() throws Exception {
		Where where = new Where(createTableInfo());
		int val = 112;
		where.eq(COLUMN_NAME, val);
		testOperation(where, COLUMN_NAME, "=", val);
	}

	@Test
	public void testGe() throws Exception {
		Where where = new Where(createTableInfo());
		int val = 112;
		where.ge(COLUMN_NAME, val);
		testOperation(where, COLUMN_NAME, ">=", val);
	}

	@Test
	public void testGt() throws Exception {
		Where where = new Where(createTableInfo());
		int val = 112;
		where.gt(COLUMN_NAME, val);
		testOperation(where, COLUMN_NAME, ">", val);
	}

	@Test
	public void testLt() throws Exception {
		Where where = new Where(createTableInfo());
		int val = 112;
		where.lt(COLUMN_NAME, val);
		testOperation(where, COLUMN_NAME, "<", val);
	}

	@Test
	public void testLe() throws Exception {
		Where where = new Where(createTableInfo());
		int val = 112;
		where.le(COLUMN_NAME, val);
		testOperation(where, COLUMN_NAME, "<=", val);
	}

	@Test
	public void testNe() throws Exception {
		Where where = new Where(createTableInfo());
		int val = 112;
		where.ne(COLUMN_NAME, val);
		testOperation(where, COLUMN_NAME, "<>", val);
	}

	@Test
	public void testIn() throws Exception {
		Where where = new Where(createTableInfo());
		int val = 112;
		where.in(COLUMN_NAME, val);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(databaseType, whereSb, new ArrayList<SelectArg>());
		StringBuilder sb = new StringBuilder();
		databaseType.appendEscapedEntityName(sb, COLUMN_NAME);
		sb.append(" IN (");
		sb.append(val).append(" ) ");
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test
	public void testInMany() throws Exception {
		Where where = new Where(createTableInfo());
		int[] vals = new int[] { 112, 123, 61 };
		// NOTE: we can't pass in vals here
		where.in(COLUMN_NAME, vals[0], vals[1], vals[2]);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(databaseType, whereSb, new ArrayList<SelectArg>());
		StringBuilder sb = new StringBuilder();
		databaseType.appendEscapedEntityName(sb, COLUMN_NAME);
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
		Where where = new Where(createTableInfo());
		List<Integer> vals = new ArrayList<Integer>();
		vals.add(112);
		vals.add(123);
		vals.add(61);
		// NOTE: we can't pass in vals here
		where.in(COLUMN_NAME, vals);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(databaseType, whereSb, new ArrayList<SelectArg>());
		StringBuilder sb = new StringBuilder();
		databaseType.appendEscapedEntityName(sb, COLUMN_NAME);
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
		Where where = new Where(createTableInfo());
		where.isNull(COLUMN_NAME);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(databaseType, whereSb, new ArrayList<SelectArg>());
		StringBuilder sb = new StringBuilder();
		databaseType.appendEscapedEntityName(sb, COLUMN_NAME);
		sb.append(" IS NULL ");
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test
	public void testIsNotNull() throws Exception {
		Where where = new Where(createTableInfo());
		where.isNotNull(COLUMN_NAME);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(databaseType, whereSb, new ArrayList<SelectArg>());
		StringBuilder sb = new StringBuilder();
		databaseType.appendEscapedEntityName(sb, COLUMN_NAME);
		sb.append(" IS NOT NULL ");
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInArrayWithinArray() throws Exception {
		Where where = new Where(createTableInfo());
		// NOTE: we can't pass in vals here
		where.in(COLUMN_NAME, new int[] { 112 });
	}

	@Test
	public void testLike() throws Exception {
		Where where = new Where(createTableInfo());
		int val = 112;
		where.like(COLUMN_NAME, val);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(databaseType, whereSb, new ArrayList<SelectArg>());
		StringBuilder sb = new StringBuilder();
		databaseType.appendEscapedEntityName(sb, COLUMN_NAME);
		sb.append(" LIKE ");
		sb.append(val).append(' ');
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test
	public void testNotFuture() throws Exception {
		Where where = new Where(createTableInfo());
		int val = 112;
		where.not();
		where.like(COLUMN_NAME, val);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(databaseType, whereSb, new ArrayList<SelectArg>());
		StringBuilder sb = new StringBuilder();
		sb.append("(NOT ");
		databaseType.appendEscapedEntityName(sb, COLUMN_NAME);
		sb.append(" LIKE ");
		sb.append(val).append(" ) ");
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test
	public void testNotAbsorb() throws Exception {
		Where where = new Where(createTableInfo());
		int val = 112;
		where.like(COLUMN_NAME, val);
		where.not(where);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(databaseType, whereSb, new ArrayList<SelectArg>());
		StringBuilder sb = new StringBuilder();
		sb.append("(NOT ");
		databaseType.appendEscapedEntityName(sb, COLUMN_NAME);
		sb.append(" LIKE ");
		sb.append(val).append(" ) ");
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test
	public void testAndFuture() throws Exception {
		Where where = new Where(createTableInfo());
		int val = 112;
		where.eq(COLUMN_NAME, val);
		where.and();
		where.eq(COLUMN_NAME, val);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(databaseType, whereSb, new ArrayList<SelectArg>());
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		databaseType.appendEscapedEntityName(sb, COLUMN_NAME);
		sb.append(" = ").append(val);
		sb.append(" AND ");
		databaseType.appendEscapedEntityName(sb, COLUMN_NAME);
		sb.append(" = ").append(val);
		sb.append(" ) ");
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test
	public void testOrFuture() throws Exception {
		Where where = new Where(createTableInfo());
		int val = 112;
		where.eq(COLUMN_NAME, val);
		where.or();
		where.eq(COLUMN_NAME, val);
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(databaseType, whereSb, new ArrayList<SelectArg>());
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		databaseType.appendEscapedEntityName(sb, COLUMN_NAME);
		sb.append(" = ").append(val);
		sb.append(" OR ");
		databaseType.appendEscapedEntityName(sb, COLUMN_NAME);
		sb.append(" = ").append(val);
		sb.append(" ) ");
		assertEquals(sb.toString(), whereSb.toString());
	}

	@Test
	public void testOrAbsorb() throws Exception {
		Where where = new Where(createTableInfo());
		int val = 112;
		where.or(where.eq(COLUMN_NAME, val), where.eq(COLUMN_NAME, val));
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(databaseType, whereSb, new ArrayList<SelectArg>());
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		databaseType.appendEscapedEntityName(sb, COLUMN_NAME);
		sb.append(" = ").append(val);
		sb.append(" OR ");
		databaseType.appendEscapedEntityName(sb, COLUMN_NAME);
		sb.append(" = ").append(val);
		sb.append(" ) ");
		assertEquals(sb.toString(), whereSb.toString());
	}

	private void testOperation(Where where, String columnName, String operation, Object value) throws Exception {
		StringBuilder whereSb = new StringBuilder();
		where.appendSql(databaseType, whereSb, new ArrayList<SelectArg>());
		StringBuilder sb = new StringBuilder();
		databaseType.appendEscapedEntityName(sb, columnName);
		sb.append(' ').append(operation).append(' ');
		sb.append(value).append(' ');
		assertEquals(sb.toString(), whereSb.toString());
	}

	private TableInfo<?> createTableInfo() throws SQLException {
		return new TableInfo<Foo>(new StubDatabaseType(), Foo.class);
	}

	protected static class Foo {
		@DatabaseField(columnName = COLUMN_NAME)
		int val;
		@DatabaseField(columnName = STRING_COLUMN_NAME)
		String string;
		Foo() {
		}
	}

	private class StubDatabaseType extends BaseDatabaseType {
		public String getDriverClassName() {
			return "foo.bar.baz";
		}
		public String getDriverUrlPart() {
			return "foo";
		}
	}
}
