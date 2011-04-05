package com.j256.ormlite.stmt.query;

import java.util.Collection;

/**
 * Internal class that handles a number of AND operations in a row.
 * 
 * @author graywatson
 */
public class AndMany extends BaseBinaryManyClause {

	public AndMany(Clause[] clauses) {
		super(clauses);
	}

	public AndMany(Collection<Clause> clauses) {
		super(clauses);
	}

	@Override
	public void appendOperation(StringBuilder sb) {
		sb.append("AND ");
	}
}
