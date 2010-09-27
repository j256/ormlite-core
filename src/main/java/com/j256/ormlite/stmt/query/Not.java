package com.j256.ormlite.stmt.query;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.Where;

/**
 * Internal class handling the SQL 'NOT' boolean comparison operation. Used by {@link Where#not}.
 * 
 * @author graywatson
 */
public class Not implements Clause, NeedsFutureClause {

	private Comparison comparison = null;

	/**
	 * In this case we will consume a future clause.
	 */
	public Not() {
	}

	/**
	 * Create a Not from a {@link Clause}.
	 * 
	 * @throws IllegalArgumentException
	 *             If the {@link Clause} is not a {@link Comparison}.
	 */
	public Not(Clause clause) {
		setMissingClause(clause);
	}

	public void setMissingClause(Clause clause) {
		if (this.comparison != null) {
			throw new IllegalArgumentException("NOT operation already has a comparison set");
		} else if (clause instanceof Comparison) {
			this.comparison = (Comparison) clause;
		} else {
			throw new IllegalArgumentException("NOT operation can only work with comparison SQL clauses, not " + clause);
		}
	}

	public StringBuilder appendSql(DatabaseType databaseType, StringBuilder sb, List<SelectArg> selectArgList)
			throws SQLException {
		if (this.comparison == null) {
			throw new IllegalStateException("Comparison has not been set in NOT operation");
		}
		// this generates: (NOT 'x' = 123 )
		sb.append("(NOT ");
		databaseType.appendEscapedEntityName(sb, comparison.getColumnName());
		sb.append(' ');
		comparison.appendOperation(sb);
		comparison.appendValue(databaseType, sb, selectArgList);
		sb.append(") ");
		return sb;
	}

	@Override
	public String toString() {
		if (comparison == null) {
			return "NOT without comparison";
		} else {
			return "NOT comparison " + comparison;
		}
	}
}
