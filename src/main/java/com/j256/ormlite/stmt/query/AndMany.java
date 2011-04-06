package com.j256.ormlite.stmt.query;

/**
 * Internal class that handles a number of AND operations in a row.
 * 
 * @author graywatson
 */
public class AndMany extends BaseManyClause {

	public AndMany(Clause first) {
		super(first, null, null);
	}

	public AndMany(Clause first, Clause second, Clause[] clauses) {
		super(first, second, clauses);
	}

	public AndMany(Clause[] clauses) {
		super(clauses);
	}

	@Override
	public void appendOperation(StringBuilder sb) {
		sb.append("AND ");
	}
}
