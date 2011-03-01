package com.j256.ormlite.stmt.query;

import java.util.List;

import org.junit.Test;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.stmt.SelectArg;

public class BaseBinaryClauseTest {

	@Test(expected = IllegalStateException.class)
	public void testDoubleSet() {
		Clause clause = new Clause() {
			public void appendSql(DatabaseType databaseType, StringBuilder sb, List<SelectArg> selectArgList) {
			}
		};
		BaseBinaryClause binaryClause = new BaseBinaryClause(clause) {
			@Override
			public StringBuilder appendOperation(StringBuilder sb) {
				return sb;
			}
		};
		binaryClause.setMissingClause(clause);
		binaryClause.setMissingClause(clause);
	}
}
