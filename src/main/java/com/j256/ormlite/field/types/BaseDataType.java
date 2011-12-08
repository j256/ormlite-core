package com.j256.ormlite.field.types;

import java.lang.reflect.Field;
import java.sql.SQLException;

import com.j256.ormlite.field.DataPersister;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.support.DatabaseResults;

/**
 * Base data type that defines the defaults the various data types.
 * 
 * <p>
 * Here's a good page about the <a href="http://docs.codehaus.org/display/CASTOR/Type+Mapping" >mapping for a number of
 * database types</a>:
 * </p>
 * 
 * @author graywatson
 */
public abstract class BaseDataType implements DataPersister {

	private final SqlType sqlType;
	private final Class<?>[] classes;

	public BaseDataType(SqlType sqlType, Class<?>[] classes) {
		this.sqlType = sqlType;
		this.classes = classes;
	}

	public abstract Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException;

	public abstract Object resultToJava(FieldType fieldType, DatabaseResults results, int columnPos)
			throws SQLException;

	/**
	 * @throws SQLException
	 *             For subclasses.
	 */
	public Object sqlArgToJava(FieldType fieldType, Object sqlArg, int columnPos) throws SQLException {
		// noop pass-thru
		return sqlArg;
	}

	/**
	 * @throws SQLException
	 *             For subclasses.
	 */
	public Object javaToSqlArg(FieldType fieldType, Object javaObject) throws SQLException {
		// noop pass-thru
		return javaObject;
	}

	public abstract boolean isValidForField(Field field);

	/**
	 * @throws SQLException
	 *             For subclasses.
	 */
	public Object makeConfigObject(FieldType fieldType) throws SQLException {
		return null;
	}

	public SqlType getSqlType() {
		return sqlType;
	}

	public Class<?>[] getAssociatedClasses() {
		return classes;
	}

	public boolean isStreamType() {
		return false;
	}

	public Object convertIdNumber(Number number) {
		// by default the type cannot convert an id number
		return null;
	}

	public boolean isValidGeneratedType() {
		return false;
	}

	public boolean isEscapedDefaultValue() {
		// default is to not escape the type if it is a number
		return isEscapedValue();
	}

	public boolean isEscapedValue() {
		return true;
	}

	public boolean isPrimitive() {
		return false;
	}

	public boolean isComparable() {
		return true;
	}

	public boolean isAppropriateId() {
		return true;
	}

	public boolean isArgumentHolderRequired() {
		return false;
	}

	public boolean isSelfGeneratedId() {
		return false;
	}

	public Object generateId() {
		throw new IllegalStateException("Should not have tried to generate this type");
	}

	public int getDefaultWidth() {
		return 0;
	}

	public boolean dataIsEqual(Object fieldObj1, Object fieldObj2) {
		if (fieldObj1 == null) {
			return (fieldObj2 == null);
		} else if (fieldObj2 == null) {
			return false;
		} else {
			return fieldObj1.equals(fieldObj2);
		}
	}

	public boolean isValidForVersion() {
		return false;
	}

	public Object moveToNextValue(Object currentValue) {
		return null;
	}
}
