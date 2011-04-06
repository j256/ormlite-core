package com.j256.ormlite.stmt.query;

/**
 * Internal class that handles a number of OR operations in a row.
 * 
 * @author graywatson
 */
public class OrMany extends BaseManyClause {

	public OrMany(Clause first) {
		super(first, null, null);
	}

	public OrMany(Clause first, Clause second) {
		super(first, second, null);
	}

	public OrMany(Clause first, Clause second, Clause[] clauses) {
		super(first, second, clauses);
	}

	public OrMany(Clause[] clauses) {
		super(clauses);
	}

	@Override
	public void appendOperation(StringBuilder sb) {
		sb.append("OR ");
	}
}
