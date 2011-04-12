package com.j256.ormlite.stmt.query;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
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
			public void appendOperation(StringBuilder sb) {
			}
			public void appendValue(DatabaseType databaseType, StringBuilder sb, List<SelectArg> selectArgList) {
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

	@Test(expected = IllegalStateException.class)
	public void testNoClause() throws Exception {
		Not not = new Not();
		not.appendSql(databaseType, new StringBuilder(), new ArrayList<SelectArg>());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBaseNotClause() throws Exception {
		Not not = new Not();
		not.setMissingClause(new ManyClause((Clause) null, "AND"));
	}

	@Test
	public void testToString() throws Exception {
		String name = "foo";
		String value = "bar";
		SimpleComparison eq = new SimpleComparison(name, numberFieldType, value, SimpleComparison.EQUAL_TO_OPERATION);
		Not not = new Not();
		assertTrue(not.toString().contains("NOT without comparison"));
		not.setMissingClause(eq);
		assertTrue(not.toString().contains("NOT comparison"));
		assertTrue(not.toString().contains(eq.toString()));
	}
}
