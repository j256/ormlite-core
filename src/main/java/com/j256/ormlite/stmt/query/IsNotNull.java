package com.j256.ormlite.stmt.query;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.Where;

/**
 * Internal class handling the SQL 'IS NOT NULL' comparison query part. Used by {@link Where#isNull}.
 * 
 * @author graywatson
 */
public class IsNotNull extends BaseComparison {

	public IsNotNull(String columnName, FieldType fieldType) throws SQLException {
		super(columnName, fieldType, null);
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
