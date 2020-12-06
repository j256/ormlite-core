package com.j256.ormlite.stmt.query;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.stmt.ArgumentHolder;

/**
 * For operations with a number of them in a row.
 * 
 * @author graywatson
 */
public class ManyClause implements Clause, NeedsFutureClause {

	private final Clause first;
	private Clause second;
	private final Clause[] others;
	private final int startOthersAt;
	private final Operation operation;

	public ManyClause(Clause first, Operation operation) {
		this.first = first;
		// second will be set later
		this.second = null;
		this.others = null;
		this.startOthersAt = 0;
		this.operation = operation;
	}

	public ManyClause(Clause first, Clause second, Clause[] others, Operation operation) {
		this.first = first;
		this.second = second;
		this.others = others;
		this.startOthersAt = 0;
		this.operation = operation;
	}

	public ManyClause(Clause[] others, Operation operation) {
		this.first = others[0];
		if (others.length < 2) {
			this.second = null;
			this.startOthersAt = others.length;
		} else {
			this.second = others[1];
			this.startOthersAt = 2;
		}
		this.others = others;
		this.operation = operation;
	}

	@Override
	public void appendSql(DatabaseType databaseType, String tableName, StringBuilder sb,
			List<ArgumentHolder> selectArgList, Clause outer) throws SQLException {
		boolean closing;
		if (outer instanceof ManyClause && ((ManyClause) outer).operation == operation) {
			closing = false;
		} else {
			sb.append('(');
			closing = true;
		}
		first.appendSql(databaseType, tableName, sb, selectArgList, this);
		if (second != null) {
			sb.append(operation.sql);
			sb.append(' ');
			second.appendSql(databaseType, tableName, sb, selectArgList, this);
		}
		if (others != null) {
			for (int i = startOthersAt; i < others.length; i++) {
				sb.append(operation.sql);
				sb.append(' ');
				others[i].appendSql(databaseType, tableName, sb, selectArgList, this);
			}
		}
		if (closing) {
			sb.append(") ");
		}
	}

	@Override
	public void setMissingClause(Clause right) {
		second = right;
	}

	/**
	 * Type of operation for the many clause.
	 */
	public static enum Operation {
		AND("AND"),
		OR("OR"),
		// end
		;
		public final String sql;

		private Operation(String sql) {
			this.sql = sql;
		}
	}
}
