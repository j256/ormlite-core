package com.j256.ormlite.stmt.query;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.stmt.SelectArg;

/**
 * Base class for operations with a number of them in a row.
 * 
 * @author graywatson
 */
public abstract class BaseManyClause implements Clause, NeedsFutureClause {

	private final Clause first;
	private Clause second;
	private final Clause[] others;
	private final int startOthersAt;

	public BaseManyClause(Clause first, Clause second, Clause[] others) {
		this.first = first;
		this.second = second;
		this.others = others;
		this.startOthersAt = 0;
	}

	public BaseManyClause(Clause[] others) {
		this.first = others[0];
		if (others.length < 2) {
			this.second = null;
			this.startOthersAt = others.length;
		} else {
			this.second = others[1];
			this.startOthersAt = 2;
		}
		this.others = others;
	}

	public void appendSql(DatabaseType databaseType, StringBuilder sb, List<SelectArg> selectArgList)
			throws SQLException {
		sb.append("(");
		first.appendSql(databaseType, sb, selectArgList);
		if (second != null) {
			appendOperation(sb);
			second.appendSql(databaseType, sb, selectArgList);
		}
		if (others != null) {
			for (int i = startOthersAt; i < others.length; i++) {
				appendOperation(sb);
				others[i].appendSql(databaseType, sb, selectArgList);
			}
		}
		sb.append(") ");
	}

	public void setMissingClause(Clause right) {
		second = right;
	}

	/**
	 * Append the associated operation to the StringBuilder.
	 */
	public abstract void appendOperation(StringBuilder sb);
}
