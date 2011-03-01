package com.j256.ormlite.stmt.query;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.stmt.BaseCoreStmtTest;
import com.j256.ormlite.stmt.SelectArg;

/**
 * Oh yes it _is_ a test, just of the NOT operation.
 */
public class NotTest extends BaseCoreStmtTest {

	@Test(expected = IllegalArgumentException.class)
	public void test() {
		Not not = new Not();
		Clause clause = new Comparison() {
			public StringBuilder appendOperation(StringBuilder sb) {
				return sb;
			}
			public StringBuilder appendValue(DatabaseType databaseType, StringBuilder sb, List<SelectArg> selectArgList) {
				return sb;
			}
			public String getColumnName() {
				return null;
			}
			public void appendSql(DatabaseType databaseType, StringBuilder sb, List<SelectArg> selectArgList) {
			}
		};
		not.setMissingClause(clause);
		not.setMissingClause(clause);
	}

	@Test
	public void testToString() throws Exception {
		String name = "foo";
		String value = "bar";
		Eq eq = new Eq(name, numberFieldType, value);
		Not not = new Not();
		assertTrue(not.toString().contains("NOT without comparison"));
		not.setMissingClause(eq);
		assertTrue(not.toString().contains("NOT comparison"));
		assertTrue(not.toString().contains(eq.toString()));
	}
}
