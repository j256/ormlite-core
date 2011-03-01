package com.j256.ormlite.stmt.query;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.stmt.SelectArg;

/**
 * Internal base class for all comparison operations.
 * 
 * @author graywatson
 */
abstract class BaseComparison implements Comparison {

	protected final String columnName;
	protected final FieldType fieldType;
	private final Object value;

	protected BaseComparison(String columnName, FieldType fieldType, Object value) throws SQLException {
		if (fieldType != null && !fieldType.isComparable()) {
			throw new SQLException("Field '" + columnName + "' is of data type " + fieldType.getDataType()
					+ " which can be compared");
		}
		this.columnName = columnName;
		this.fieldType = fieldType;
		this.value = value;
	}

	public abstract StringBuilder appendOperation(StringBuilder sb);

	public void appendSql(DatabaseType databaseType, StringBuilder sb, List<SelectArg> selectArgList)
			throws SQLException {
		databaseType.appendEscapedEntityName(sb, columnName);
		sb.append(' ');
		appendOperation(sb);
		// this needs to call appendValue (not appendArgOrValue) because it may be overridden
		appendValue(databaseType, sb, selectArgList);
	}

	public String getColumnName() {
		return columnName;
	}

	public StringBuilder appendValue(DatabaseType databaseType, StringBuilder sb, List<SelectArg> selectArgList)
			throws SQLException {
		appendArgOrValue(databaseType, fieldType, sb, selectArgList, value);
		return sb;
	}

	/**
	 * Append to the string builder either a {@link SelectArg} argument or a value object.
	 */
	protected void appendArgOrValue(DatabaseType databaseType, FieldType fieldType, StringBuilder sb,
			List<SelectArg> selectArgList, Object argOrValue) throws SQLException {
		boolean appendSpace = true;
		if (argOrValue == null) {
			throw new SQLException("argument to comparison of '" + fieldType.getFieldName() + "' is null");
		} else if (argOrValue instanceof SelectArg) {
			sb.append('?');
			SelectArg selectArg = (SelectArg) argOrValue;
			selectArg.setMetaInfo(columnName, fieldType);
			selectArgList.add(selectArg);
		} else if (fieldType.isSelectArgRequired()) {
			sb.append('?');
			SelectArg selectArg = new SelectArg();
			selectArg.setMetaInfo(columnName, fieldType);
			// conversion is done when the getValue() is called
			selectArg.setValue(argOrValue);
			selectArgList.add(selectArg);
		} else if (fieldType.isForeign() && fieldType.getFieldType() == argOrValue.getClass()) {
			/*
			 * If we have a foreign field and our argument is an instance of the foreign object (i.e. not its id), then
			 * we need to extract the id.
			 */
			FieldType idFieldType = fieldType.getForeignIdField();
			appendArgOrValue(databaseType, idFieldType, sb, selectArgList,
					idFieldType.extractJavaFieldValue(argOrValue));
			// no need for the space since it was done in the recursion
			appendSpace = false;
		} else if (fieldType.isEscapedValue()) {
			databaseType.appendEscapedWord(sb, fieldType.convertJavaFieldToSqlArgValue(argOrValue).toString());
		} else {
			// numbers can't have quotes around them in derby
			sb.append(fieldType.convertJavaFieldToSqlArgValue(argOrValue));
		}
		if (appendSpace) {
			sb.append(' ');
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(columnName).append(' ');
		appendOperation(sb).append(' ');
		sb.append(value);
		return sb.toString();
	}
}
