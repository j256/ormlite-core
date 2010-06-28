package com.j256.ormlite.stmt.query;

import java.util.List;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.Where;

/**
 * Internal class handling the SQL 'IS NOT NULL' comparison query part. Used by {@link Where#isNull}.
 * 
 * @author graywatson
 */
public class IsNotNull extends BaseComparison {

	public IsNotNull(String columnName, boolean isNumber) {
		super(columnName, isNumber, null);
	}

	@Override
	public StringBuilder appendOperation(StringBuilder sb) {
		sb.append("IS NOT NULL ");
		return sb;
	}

	@Override
	public StringBuilder appendValue(DatabaseType databaseType, StringBuilder sb, List<SelectArg> selectArgList) {
		// there is no value
		return sb;
	}
}
