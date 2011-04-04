package com.j256.ormlite.stmt.query;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.stmt.QueryBuilder.InternalQueryBuilderWrapper;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.Where;

/**
 * Internal class handling the SQL 'in' query part. Used by {@link Where#in}.
 * 
 * @author graywatson
 */
public class InSubQuery extends BaseComparison {

	private final InternalQueryBuilderWrapper subQueryBuilder;

	public InSubQuery(String columnName, FieldType fieldType, InternalQueryBuilderWrapper subQueryBuilder)
			throws SQLException {
		super(columnName, fieldType, null);
		this.subQueryBuilder = subQueryBuilder;
	}

	@Override
	public StringBuilder appendValue(DatabaseType databaseType, StringBuilder sb, List<SelectArg> selectArgList)
			throws SQLException {
		sb.append('(');
		subQueryBuilder.buildStatementString(sb, selectArgList);
		FieldType[] resultFieldTypes = subQueryBuilder.getResultFieldTypes();
		if (resultFieldTypes.length != 1) {
			throw new SQLException("There must be only 1 result column in sub-query but we found "
					+ resultFieldTypes.length);
		}
		if (fieldType.getSqlType() != resultFieldTypes[0].getSqlType()) {
			throw new SQLException("Outer column " + fieldType + " is not the same type as inner column "
					+ resultFieldTypes[0]);
		}
		sb.append(") ");
		return sb;
	}

	@Override
	public StringBuilder appendOperation(StringBuilder sb) {
		sb.append("IN ");
		return sb;
	}
}
