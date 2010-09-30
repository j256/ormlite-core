package com.j256.ormlite.stmt.query;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.stmt.StatementBuilder;

/**
 * Internal class handling the SQL SET part used by UPDATE statements. Used by
 * {@link StatementBuilder#updateColumnValue(String, Object)}.
 * 
 * <p> It's not a comparison per se but does have a columnName = value form so it works. </p>
 * 
 * @author graywatson
 */
public class SetValue extends BaseComparison {

	public SetValue(String columnName, FieldType fieldType, Object value) {
		super(columnName, fieldType, value);
	}

	@Override
	public StringBuilder appendOperation(StringBuilder sb) {
		sb.append("= ");
		return sb;
	}
}
