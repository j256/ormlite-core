package com.j256.ormlite.stmt.query;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.stmt.Where;

import java.sql.SQLException;

/**
 * Internal class handling the SQL 'IS NULL' comparison query part. Used by {@link Where#isNull}.
 * 
 * @author graywatson
 */
public class IsNull extends NullTestBase {

	public IsNull(String columnName, FieldType fieldType) throws SQLException {
		super(columnName, fieldType);
	}

	@Override
	protected void appendOperation(StringBuilder sb) {
		sb.append("IS NULL ");
	}
}
