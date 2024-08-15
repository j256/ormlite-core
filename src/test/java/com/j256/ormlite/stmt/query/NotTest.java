package com.j256.ormlite.stmt.query;

import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.stmt.ArgumentHolder;
import com.j256.ormlite.stmt.BaseCoreStmtTest;

/**
 * Oh yes it _is_ a test, just of the NOT operation.
 */
public class NotTest extends BaseCoreStmtTest {

	@Test
	public void test() {
		Not not = new Not();
		Clause clause = new Comparison() {
			@Override
			public void appendOperation(StringBuilder sb) {
			}

			@Override
			public void appendValue(DatabaseType databaseType, StringBuilder sb, List<ArgumentHolder> argList) {
			}

			@Override
			public String getColumnName() {
				return null;
			}

			@Override
			public void appendSql(DatabaseType databaseType, String tableName, StringBuilder sb,
					List<ArgumentHolder> argList, Clause outer) {
			}
		};
		not.setMissingClause(clause);
		assertThrowsExactly(IllegalArgumentException.class, () -> {
			not.setMissingClause(clause);
		});
	}

	@Test
	public void testNoClause() {
		Not not = new Not();
		assertThrowsExactly(IllegalStateException.class, () -> {
			not.appendSql(databaseType, null, new StringBuilder(), new ArrayList<ArgumentHolder>(), null);
		});
	}

	@Test
	public void testBaseNotClause() {
		Not not = new Not();
		assertThrowsExactly(IllegalArgumentException.class, () -> {
			not.setMissingClause(new ManyClause((Clause) null, ManyClause.Operation.AND));
		});
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
