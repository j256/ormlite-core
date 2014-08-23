package com.j256.ormlite.stmt.query;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.stmt.ArgumentHolder;

import java.sql.SQLException;
import java.util.List;

/**
 * Internal base class for IS NULL and IS NOT NULL test operations.
 * 
 * @author André Wachter
 */
abstract class NullTestBase implements Clause {
	protected final String columnName;
	protected final FieldType fieldType;

	protected NullTestBase(String columnName, FieldType fieldType) throws SQLException {
		this.columnName = columnName;
		this.fieldType = fieldType;
	}

	@Override
	public void appendSql(DatabaseType databaseType, String tableName, StringBuilder sb, List<ArgumentHolder> argList)
			throws SQLException {
		if (tableName != null) {
			databaseType.appendEscapedEntityName(sb, tableName);
			sb.append('.');
		}
		databaseType.appendEscapedEntityName(sb, columnName);
		sb.append(' ');
		appendOperation(sb);
	}

	public String getColumnName() {
		return columnName;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(columnName).append(' ');
		appendOperation(sb);
		return sb.toString();
	}

	protected abstract void appendOperation(StringBuilder sb);
}
