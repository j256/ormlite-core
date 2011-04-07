package com.j256.ormlite.stmt.query;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.stmt.SelectArg;

/**
 * Internal class that handles the AND sql operation which takes two {@link Clause} parts.
 * 
 * @author graywatson
 */
abstract class BaseBinaryClause implements NeedsFutureClause {

	private final Clause left;
	private Clause right;

	protected BaseBinaryClause(Clause left) {
		this.left = left;
		this.right = null;
	}

	protected BaseBinaryClause(Clause left, Clause right) {
		this.left = left;
		this.right = right;
	}

	/**
	 * Append the associated operation to the StringBuilder.
	 */
	public abstract void appendOperation(StringBuilder sb);

	public void appendSql(DatabaseType databaseType, StringBuilder sb, List<SelectArg> columnArgList)
			throws SQLException {
		sb.append('(');
		left.appendSql(databaseType, sb, columnArgList);
		appendOperation(sb);
		right.appendSql(databaseType, sb, columnArgList);
		sb.append(") ");
	}

	public void setMissingClause(Clause right) {
		if (this.right != null) {
			throw new IllegalStateException("Operation already has a right side set: " + this);
		}
		this.right = right;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(left).append(' ');
		appendOperation(sb);
		sb.append(right);
		return sb.toString();
	}
}
