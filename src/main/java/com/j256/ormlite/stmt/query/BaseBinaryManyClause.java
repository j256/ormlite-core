package com.j256.ormlite.stmt.query;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.stmt.SelectArg;

/**
 * Base class for binary operations with a number of them in a row.
 * 
 * @author graywatson
 */
public abstract class BaseBinaryManyClause implements Clause {

	private Clause[] clauseArray;
	private Collection<Clause> clauseCollection;

	public BaseBinaryManyClause(Clause[] clauses) {
		this.clauseArray = clauses;
	}

	public BaseBinaryManyClause(Collection<Clause> clauseCollection) {
		this.clauseCollection = clauseCollection;
	}

	public void appendSql(DatabaseType databaseType, StringBuilder sb, List<SelectArg> selectArgList)
			throws SQLException {
		sb.append("(");
		boolean first = true;
		if (clauseArray == null) {
			for (Clause clause : clauseCollection) {
				if (first) {
					first = false;
				} else {
					appendOperation(sb);
				}
				clause.appendSql(databaseType, sb, selectArgList);
			}
		} else {
			for (Clause clause : clauseArray) {
				if (first) {
					first = false;
				} else {
					appendOperation(sb);
				}
				clause.appendSql(databaseType, sb, selectArgList);
			}
		}
		sb.append(") ");
	}

	/**
	 * Append the associated operation to the StringBuilder.
	 */
	public abstract void appendOperation(StringBuilder sb);
}
