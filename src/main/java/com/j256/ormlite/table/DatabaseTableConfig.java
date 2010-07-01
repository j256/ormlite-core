package com.j256.ormlite.table;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.misc.JavaxPersistence;

/**
 * Database table configuration information.
 * 
 * @author graywatson
 */
public class DatabaseTableConfig<T> {

	private Class<T> dataClass;
	private String tableName;
	private List<DatabaseFieldConfig> fieldConfigs;
	private FieldType[] fieldTypes;

	public DatabaseTableConfig() {
		// for spring
	}

	public DatabaseTableConfig(Class<T> dataClass, List<DatabaseFieldConfig> fieldConfigs) {
		this(dataClass, extractTableName(dataClass), fieldConfigs);
	}

	public DatabaseTableConfig(Class<T> dataClass, String tableName, List<DatabaseFieldConfig> fieldConfigs) {
		this.dataClass = dataClass;
		this.tableName = tableName;
		this.fieldConfigs = fieldConfigs;
	}

	private DatabaseTableConfig(Class<T> dataClass, String tableName, FieldType[] fieldTypes) {
		this.dataClass = dataClass;
		this.tableName = tableName;
		this.fieldTypes = fieldTypes;
	}

	public void initialize() {
		if (dataClass == null) {
			throw new IllegalStateException("dataClass was never set on " + getClass().getSimpleName());
		}
		if (tableName == null) {
			tableName = extractTableName(dataClass);
		}
	}

	public Class<T> getDataClass() {
		return dataClass;
	}

	// @Required
	public void setDataClass(Class<T> dataClass) {
		this.dataClass = dataClass;
	}

	public String getTableName() {
		return tableName;
	}

	/**
	 * Set the table name which is turned into lowercase. If not specified then the name is gotten from the class name.
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName.toLowerCase();
	}

	public void setFieldConfigs(List<DatabaseFieldConfig> fieldConfigs) {
		this.fieldConfigs = fieldConfigs;
	}

	public FieldType[] extractFieldTypes(DatabaseType databaseType) {
		if (fieldTypes == null) {
			if (fieldConfigs == null) {
				fieldTypes = extractFieldTypes(databaseType, dataClass, tableName);
			} else {
				fieldTypes = convertFieldConfigs(databaseType, tableName, fieldConfigs);
			}
		}
		return fieldTypes;
	}

	public static <T> DatabaseTableConfig<T> fromClass(DatabaseType databaseType, Class<T> clazz) {
		String tableName = extractTableName(clazz);
		if (databaseType.isEntityNamesMustBeUpCase()) {
			tableName = tableName.toUpperCase();
		}
		return new DatabaseTableConfig<T>(clazz, tableName, extractFieldTypes(databaseType, clazz, tableName));
	}

	private static <T> FieldType[] extractFieldTypes(DatabaseType databaseType, Class<T> clazz, String tableName) {
		List<FieldType> fieldTypes = new ArrayList<FieldType>();
		for (Field field : clazz.getDeclaredFields()) {
			FieldType fieldType = FieldType.createFieldType(databaseType, tableName, field);
			if (fieldType != null) {
				fieldTypes.add(fieldType);
			}
		}
		if (fieldTypes.size() == 0) {
			throw new IllegalArgumentException("No fields have a " + DatabaseField.class.getSimpleName()
					+ " annotation in " + clazz);
		}
		return fieldTypes.toArray(new FieldType[fieldTypes.size()]);
	}

	private static <T> String extractTableName(Class<T> clazz) {
		DatabaseTable databaseTable = clazz.getAnnotation(DatabaseTable.class);
		String name = null;
		if (databaseTable != null && databaseTable.tableName() != null && databaseTable.tableName().length() > 0) {
			name = databaseTable.tableName();
		} else {
			/*
			 * NOTE: to remove javax.persistence usage, comment the following line out
			 */
			name = JavaxPersistence.getEntityName(clazz);
			if (name == null) {
				// if the name isn't specified, it is the class name lowercased
				name = clazz.getSimpleName().toLowerCase();
			}
		}
		return name;
	}

	private FieldType[] convertFieldConfigs(DatabaseType databaseType, String tableName,
			List<DatabaseFieldConfig> fieldConfigs) {
		List<FieldType> fieldTypes = new ArrayList<FieldType>();
		for (DatabaseFieldConfig fieldConfig : fieldConfigs) {
			Field field;
			try {
				field = dataClass.getDeclaredField(fieldConfig.getFieldName());
			} catch (Exception e) {
				throw new IllegalArgumentException("Could not configure field with name '" + fieldConfig.getFieldName()
						+ "' for " + dataClass, e);
			}
			FieldType fieldType = new FieldType(databaseType, tableName, field, fieldConfig);
			fieldTypes.add(fieldType);
		}
		if (fieldTypes.size() == 0) {
			throw new IllegalArgumentException("No fields have a " + DatabaseField.class + " annotation in "
					+ dataClass);
		}
		return fieldTypes.toArray(new FieldType[fieldTypes.size()]);
	}
}
