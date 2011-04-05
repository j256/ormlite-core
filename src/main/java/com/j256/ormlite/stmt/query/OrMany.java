package com.j256.ormlite.stmt.query;

import java.util.Collection;

/**
 * Internal class that handles a number of OR operations in a row.
 * 
 * @author graywatson
 */
public class OrMany extends BaseBinaryManyClause {

	public OrMany(Clause[] clauses) {
		super(clauses);
	}

	public OrMany(Collection<Clause> clauses) {
		super(clauses);
	}

	@Override
	public void appendOperation(StringBuilder sb) {
		sb.append("OR ");
	}
}
