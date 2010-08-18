package com.j256.ormlite.field;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.SQLException;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.misc.JavaxPersistence;
import com.j256.ormlite.table.DatabaseTableConfig;

/**
 * Database field configuration information either supplied by Spring or direct Java wiring or from a
 * {@link DatabaseField} annotation.
 * 
 * @author graywatson
 */
public class DatabaseFieldConfig {

	private String fieldName;
	private String columnName;
	private DataType jdbcType = DataType.UNKNOWN;
	private String defaultValue;
	private int width;
	private boolean canBeNull;
	private boolean id;
	private boolean generatedId;
	private String generatedIdSequence;
	private boolean foreign;
	private DatabaseTableConfig<?> foreignTableConfig;
	private boolean useGetSet;
	private Enum<?> unknownEnumvalue;
	private boolean throwIfNull;

	public DatabaseFieldConfig() {
		// for spring
	}

	public DatabaseFieldConfig(String fieldName, String columnName, DataType jdbcType, String defaultValue, int width,
			boolean canBeNull, boolean id, boolean generatedId, String generatedIdSequence, boolean foreign,
			DatabaseTableConfig<?> foreignTableConfig, boolean useGetSet, Enum<?> unknownEnumValue, boolean throwIfNull) {
		this.fieldName = fieldName;
		this.columnName = columnName;
		this.jdbcType = jdbcType;
		this.defaultValue = defaultValue;
		this.width = width;
		this.canBeNull = canBeNull;
		this.id = id;
		this.generatedId = generatedId;
		this.generatedIdSequence = generatedIdSequence;
		this.foreign = foreign;
		this.foreignTableConfig = foreignTableConfig;
		this.useGetSet = useGetSet;
		this.unknownEnumvalue = unknownEnumValue;
		this.throwIfNull = throwIfNull;
	}

	/**
	 * Return the name of the field in the class.
	 */
	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	/**
	 * @see DatabaseField#columnName()
	 */
	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	/**
	 * @see DatabaseField#jdbcType()
	 */
	public DataType getJdbcType() {
		return jdbcType;
	}

	public void setJdbcType(DataType jdbcType) {
		this.jdbcType = jdbcType;
	}

	/**
	 * @see DatabaseField#defaultValue()
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * @see DatabaseField#width()
	 */
	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @see DatabaseField#canBeNull()
	 */
	public boolean isCanBeNull() {
		return canBeNull;
	}

	public void setCanBeNull(boolean canBeNull) {
		this.canBeNull = canBeNull;
	}

	/**
	 * @see DatabaseField#id()
	 */
	public boolean isId() {
		return id;
	}

	public void setId(boolean id) {
		this.id = id;
	}

	/**
	 * @see DatabaseField#generatedId()
	 */
	public boolean isGeneratedId() {
		return generatedId;
	}

	public void setGeneratedId(boolean generatedId) {
		this.generatedId = generatedId;
	}

	/**
	 * @see DatabaseField#generatedIdSequence()
	 */
	public String getGeneratedIdSequence() {
		return generatedIdSequence;
	}

	public void setGeneratedIdSequence(String generatedIdSequence) {
		this.generatedIdSequence = generatedIdSequence;
	}

	/**
	 * @see DatabaseField#foreign()
	 */
	public boolean isForeign() {
		return foreign;
	}

	public void setForeign(boolean foreign) {
		this.foreign = foreign;
	}

	/**
	 * For a foreign class which does not use the {@link DatabaseField} annotations, you need to inject the table
	 * configuration.
	 */
	public DatabaseTableConfig<?> getForeignTableConfig() {
		return foreignTableConfig;
	}

	public void setForeignTableConfig(DatabaseTableConfig<?> foreignTableConfig) {
		this.foreignTableConfig = foreignTableConfig;
	}

	/**
	 * @see DatabaseField#useGetSet()
	 */
	public boolean isUseGetSet() {
		return useGetSet;
	}

	public void setUseGetSet(boolean useGetSet) {
		this.useGetSet = useGetSet;
	}

	public Enum<?> getUnknownEnumvalue() {
		return unknownEnumvalue;
	}

	public void setUnknownEnumvalue(Enum<?> unknownEnumvalue) {
		this.unknownEnumvalue = unknownEnumvalue;
	}

	public boolean isThrowIfNull() {
		return throwIfNull;
	}

	public void setThrowIfNull(boolean throwIfNull) {
		this.throwIfNull = throwIfNull;
	}

	/**
	 * Create and return a config converted from a {@link Field} that may have either a {@link DatabaseField} annotation
	 * or the javax.persistence annotations.
	 */
	public static DatabaseFieldConfig fromField(DatabaseType databaseType, Field field) throws SQLException {
		// first we lookup the DatabaseField annotation
		DatabaseField databaseField = field.getAnnotation(DatabaseField.class);
		if (databaseField != null) {
			if (databaseField.persisted()) {
				return fromDatabaseField(databaseType, field, databaseField);
			} else {
				return null;
			}
		}

		/*
		 * NOTE: to remove javax.persistence usage, comment the following lines out
		 */
		DatabaseFieldConfig config = JavaxPersistence.createFieldConfig(databaseType, field);
		if (config != null) {
			return config;
		}

		return null;
	}

	/**
	 * Find and return the appropriate getter method for field.
	 * 
	 * @return Get method or null if none found.
	 */
	public static Method findGetMethod(Field field, boolean throwExceptions) {
		String methodName = methodFromField(field, "get");
		Method fieldGetMethod;
		try {
			fieldGetMethod = field.getDeclaringClass().getMethod(methodName);
		} catch (Exception e) {
			if (throwExceptions) {
				throw new IllegalArgumentException("Could not find appropriate get method for " + field);
			} else {
				return null;
			}
		}
		if (fieldGetMethod.getReturnType() != field.getType()) {
			if (throwExceptions) {
				throw new IllegalArgumentException("Return type of get method " + methodName + " does not return "
						+ field.getType());
			} else {
				return null;
			}
		}
		return fieldGetMethod;
	}

	/**
	 * Find and return the appropriate setter method for field.
	 * 
	 * @return Set method or null if none found.
	 */
	public static Method findSetMethod(Field field, boolean throwExceptions) {
		String methodName = methodFromField(field, "set");
		Method fieldSetMethod;
		try {
			fieldSetMethod = field.getDeclaringClass().getMethod(methodName, field.getType());
		} catch (Exception e) {
			if (throwExceptions) {
				throw new IllegalArgumentException("Could not find appropriate set method for " + field);
			} else {
				return null;
			}
		}
		if (fieldSetMethod.getReturnType() != void.class) {
			if (throwExceptions) {
				throw new IllegalArgumentException("Return type of set method " + methodName + " returns "
						+ fieldSetMethod.getReturnType() + " instead of void");
			} else {
				return null;
			}
		}
		return fieldSetMethod;
	}

	private static DatabaseFieldConfig fromDatabaseField(DatabaseType databaseType, Field field,
			DatabaseField databaseField) {
		DatabaseFieldConfig config = new DatabaseFieldConfig();
		config.fieldName = field.getName();
		if (databaseType.isEntityNamesMustBeUpCase()) {
			config.fieldName = config.fieldName.toUpperCase();
		}
		if (databaseField.columnName().length() > 0) {
			config.columnName = databaseField.columnName();
		} else {
			config.columnName = null;
		}
		config.jdbcType = databaseField.jdbcType();
		if (databaseField.defaultValue().length() > 0) {
			config.defaultValue = databaseField.defaultValue();
		} else {
			config.defaultValue = null;
		}
		config.width = databaseField.width();
		config.canBeNull = databaseField.canBeNull();
		config.id = databaseField.id();
		config.generatedId = databaseField.generatedId();
		if (databaseField.generatedIdSequence().length() > 0) {
			config.generatedIdSequence = databaseField.generatedIdSequence();
		} else {
			config.generatedIdSequence = null;
		}
		config.foreign = databaseField.foreign();
		config.useGetSet = databaseField.useGetSet();
		if (databaseField.unknownEnumName().length() > 0) {
			config.unknownEnumvalue = findMatchingEnumVal(field, databaseField.unknownEnumName());
		} else {
			config.unknownEnumvalue = null;
		}
		config.throwIfNull = databaseField.throwIfNull();
		return config;
	}

	private static String methodFromField(Field field, String prefix) {
		return prefix + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
	}

	private static Enum<?> findMatchingEnumVal(Field field, String unknownEnumName) {
		for (Enum<?> enumVal : (Enum<?>[]) field.getType().getEnumConstants()) {
			if (enumVal.name().equals(unknownEnumName)) {
				return enumVal;
			}
		}
		throw new IllegalArgumentException("Unknwown enum unknown name " + unknownEnumName + " for field " + field);
	}
}
