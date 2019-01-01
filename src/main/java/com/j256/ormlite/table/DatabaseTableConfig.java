package com.j256.ormlite.table;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.misc.JavaxPersistenceConfigurer;
import com.j256.ormlite.support.ConnectionSource;

/**
 * Database table configuration information either supplied by Spring or direct Java wiring or from a
 * {@link DatabaseTable} annotation.
 * 
 * @author graywatson
 */
public class DatabaseTableConfig<T> {

	private static JavaxPersistenceConfigurer javaxPersistenceConfigurer;

	// optional database type, may be null
	private DatabaseType databaseType;
	private Class<T> dataClass;
	private String tableName;
	private List<DatabaseFieldConfig> fieldConfigs;
	private FieldType[] fieldTypes;

	static {
		try {
			// see if we have this class at runtime
			Class.forName("javax.persistence.Entity");
			// if we do then get our JavaxPersistance class
			Class<?> clazz = Class.forName("com.j256.ormlite.misc.JavaxPersistenceImpl");
			javaxPersistenceConfigurer = (JavaxPersistenceConfigurer) clazz.getConstructor().newInstance();
		} catch (Exception e) {
			// no configurer
			javaxPersistenceConfigurer = null;
		}
	}

	public DatabaseTableConfig() {
		// for spring
	}

	/**
	 * Setup a table config associated with the dataClass and field configurations. The table-name will be extracted
	 * from the dataClass.
	 */
	public DatabaseTableConfig(DatabaseType databaseType, Class<T> dataClass, List<DatabaseFieldConfig> fieldConfigs) {
		this(dataClass, extractTableName(databaseType, dataClass), fieldConfigs);
	}

	/**
	 * Setup a table config associated with the dataClass, table-name, and field configurations.
	 */
	public DatabaseTableConfig(Class<T> dataClass, String tableName, List<DatabaseFieldConfig> fieldConfigs) {
		this.dataClass = dataClass;
		this.tableName = tableName;
		this.fieldConfigs = fieldConfigs;
	}

	private DatabaseTableConfig(DatabaseType databaseType, Class<T> dataClass, String tableName,
			FieldType[] fieldTypes) {
		// NOTE: databaseType may be null
		this.databaseType = databaseType;
		this.dataClass = dataClass;
		this.tableName = tableName;
		this.fieldTypes = fieldTypes;
	}

	/**
	 * Initialize the class if this is being called with Spring.
	 */
	public void initialize() {
		if (dataClass == null) {
			throw new IllegalStateException("dataClass was never set on " + getClass().getSimpleName());
		}
		if (tableName == null) {
			tableName = extractTableName(databaseType, dataClass);
		}
	}

	/**
	 * Optional setting. This is here so we can control the lowercasing of the table name in the database-type.
	 */
	public void setDatabaseType(DatabaseType databaseType) {
		this.databaseType = databaseType;
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
	 * Set the table name. If not specified then the name is gotten from the class name.
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public void setFieldConfigs(List<DatabaseFieldConfig> fieldConfigs) {
		this.fieldConfigs = fieldConfigs;
	}

	/**
	 * Extract the field types from the fieldConfigs if they have not already been configured.
	 */
	public void extractFieldTypes(DatabaseType databaseType) throws SQLException {
		if (fieldTypes == null) {
			if (fieldConfigs == null) {
				fieldTypes = extractFieldTypes(databaseType, dataClass, tableName);
			} else {
				fieldTypes = convertFieldConfigs(databaseType, tableName, fieldConfigs);
			}
		}
	}

	/**
	 * Return the field types associated with this configuration.
	 */
	public FieldType[] getFieldTypes(DatabaseType databaseType) throws SQLException {
		if (fieldTypes == null) {
			throw new SQLException("Field types have not been extracted in table config");
		}
		return fieldTypes;
	}

	public List<DatabaseFieldConfig> getFieldConfigs() {
		return fieldConfigs;
	}

	/**
	 * @deprecated 
	 */
	@Deprecated
	public void setConstructor(Constructor<T> constructor) {
	}

	/**
	 * @deprecated Use {@link #fromClass(DatabaseType, Class)}
	 */
	@Deprecated
	public static <T> DatabaseTableConfig<T> fromClass(ConnectionSource connectionSource, Class<T> clazz)
			throws SQLException {
		return fromClass(connectionSource.getDatabaseType(), clazz);
	}

	/**
	 * Extract the DatabaseTableConfig for a particular class by looking for class and field annotations. This is used
	 * by internal classes to configure a class.
	 */
	public static <T> DatabaseTableConfig<T> fromClass(DatabaseType databaseType, Class<T> clazz) throws SQLException {
		String tableName = extractTableName(databaseType, clazz);
		if (databaseType.isEntityNamesMustBeUpCase()) {
			tableName = databaseType.upCaseEntityName(tableName);
		}
		return new DatabaseTableConfig<T>(databaseType, clazz, tableName,
				extractFieldTypes(databaseType, clazz, tableName));
	}

	/**
	 * Extract and return the table name for a class.
	 */
	public static <T> String extractTableName(DatabaseType databaseType, Class<T> clazz) {
		DatabaseTable databaseTable = clazz.getAnnotation(DatabaseTable.class);
		String name = null;
		if (databaseTable != null && databaseTable.tableName() != null && databaseTable.tableName().length() > 0) {
			name = databaseTable.tableName();
		}
		if (name == null && javaxPersistenceConfigurer != null) {
			name = javaxPersistenceConfigurer.getEntityName(clazz);
		}
		if (name == null) {
			// if the name isn't specified, it is the class name lowercased
			if (databaseType == null) {
				// database-type is optional so if it is not specified we just use english
				name = clazz.getSimpleName().toLowerCase(Locale.ENGLISH);
			} else {
				name = databaseType.downCaseString(clazz.getSimpleName(), true);
			}
		}
		return name;
	}

	private static <T> FieldType[] extractFieldTypes(DatabaseType databaseType, Class<T> clazz, String tableName)
			throws SQLException {
		List<FieldType> fieldTypes = new ArrayList<FieldType>();
		for (Class<?> classWalk = clazz; classWalk != null; classWalk = classWalk.getSuperclass()) {
			for (Field field : classWalk.getDeclaredFields()) {
				FieldType fieldType = FieldType.createFieldType(databaseType, tableName, field, clazz);
				if (fieldType != null) {
					fieldTypes.add(fieldType);
				}
			}
		}
		if (fieldTypes.isEmpty()) {
			throw new IllegalArgumentException(
					"No fields have a " + DatabaseField.class.getSimpleName() + " annotation in " + clazz);
		}
		return fieldTypes.toArray(new FieldType[fieldTypes.size()]);
	}

	private FieldType[] convertFieldConfigs(DatabaseType databaseType, String tableName,
			List<DatabaseFieldConfig> fieldConfigs) throws SQLException {
		List<FieldType> fieldTypes = new ArrayList<FieldType>();
		for (DatabaseFieldConfig fieldConfig : fieldConfigs) {
			FieldType fieldType = null;
			// walk up the classes until we find the field
			for (Class<?> classWalk = dataClass; classWalk != null; classWalk = classWalk.getSuperclass()) {
				Field field;
				try {
					field = classWalk.getDeclaredField(fieldConfig.getFieldName());
				} catch (NoSuchFieldException e) {
					// we ignore this and just loop hopefully finding it in a upper class
					continue;
				}
				if (field != null) {
					fieldType = new FieldType(databaseType, tableName, field, fieldConfig, dataClass);
					break;
				}
			}

			if (fieldType == null) {
				throw new SQLException("Could not find declared field with name '" + fieldConfig.getFieldName()
						+ "' for " + dataClass);
			}
			fieldTypes.add(fieldType);
		}
		if (fieldTypes.isEmpty()) {
			throw new SQLException("No fields were configured for class " + dataClass);
		}
		return fieldTypes.toArray(new FieldType[fieldTypes.size()]);
	}
}
