package com.j256.ormlite.stmt.query;

import com.j256.ormlite.stmt.Where;

/**
 * Internal class that handles the AND sql operation which takes two {@link Clause} parts. Used by {@link Where#and}.
 * methods.
 * 
 * @author graywatson
 */
public class And extends BaseBinaryClause {

	public And(Clause left) {
		super(left);
	}

	public And(Clause left, Clause right) {
		super(left, right);
	}

	@Override
	public void appendOperation(StringBuilder sb) {
		sb.append("AND ");
	}
}
