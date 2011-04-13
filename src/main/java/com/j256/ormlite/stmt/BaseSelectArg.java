package com.j256.ormlite.stmt;

import java.sql.SQLException;

import com.j256.ormlite.field.FieldType;

/**
 * Base class for other select argument classes.
 * 
 * @author graywatson
 */
public abstract class BaseSelectArg implements ArgumentHolder {

	private String columnName = null;
	private FieldType fieldType = null;

	/**
	 * Return the stored value.
	 */
	protected abstract Object getValue();

	/**
	 * Store the value.
	 */
	public abstract void setValue(Object value);

	/**
	 * Return true if the value is set.
	 */
	protected abstract boolean isValueSet();

	/**
	 * Return the column-name associated with this argument. The name is set by the package internally.
	 */
	public String getColumnName() {
		if (columnName == null) {
			throw new IllegalArgumentException("Column name has not been set");
		} else {
			return columnName;
		}
	}

	/**
	 * Used internally by the package to set the column-name associated with this argument.
	 */
	public void setMetaInfo(String columnName, FieldType fieldType) {
		if (this.columnName == null) {
			// not set yet
		} else if (this.columnName.equals(columnName)) {
			// set to the same value as before
		} else {
			throw new IllegalArgumentException("Column name cannot be set twice from " + this.columnName + " to "
					+ columnName);
		}
		if (this.fieldType == null) {
			// not set yet
		} else if (this.fieldType == fieldType) {
			// set to the same value as before
		} else {
			throw new IllegalArgumentException("FieldType name cannot be set twice from " + this.fieldType + " to "
					+ fieldType);
		}
		this.columnName = columnName;
		this.fieldType = fieldType;
	}

	/**
	 * Return the value associated with this argument. The value should be set by the user before it is consumed.
	 */
	public Object getSqlArgValue() throws SQLException {
		if (!isValueSet()) {
			throw new SQLException("Column value has not been set for " + columnName);
		}
		Object value = getValue();
		if (value == null) {
			return null;
		} else if (fieldType == null) {
			return value;
		} else if (fieldType.isForeign() && fieldType.getFieldType() == value.getClass()) {
			FieldType idFieldType = fieldType.getForeignIdField();
			return idFieldType.extractJavaFieldValue(value);
		} else {
			return fieldType.convertJavaFieldToSqlArgValue(value);
		}
	}

	public FieldType getFieldType() {
		return fieldType;
	}

	@Override
	public String toString() {
		if (!isValueSet()) {
			return "[unset]";
		}
		Object val;
		try {
			val = getSqlArgValue();
			if (val == null) {
				return "[null]";
			} else {
				return val.toString();
			}
		} catch (SQLException e) {
			return "[could not get value: " + e.getMessage() + "]";
		}
	}
}
