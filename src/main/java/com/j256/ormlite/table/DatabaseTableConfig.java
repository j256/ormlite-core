package com.j256.ormlite.table;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.DatabaseFieldConfig;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.misc.JavaxPersistence;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.support.ConnectionSource;

/**
 * Database table configuration information either supplied by Spring or direct Java wiring or from a
 * {@link DatabaseTable} annotation.
 * 
 * @author graywatson
 */
public class DatabaseTableConfig<T> {

	private static final String CONFIG_FILE_START_MARKER = "# --table-start--";
	private static final String CONFIG_FILE_END_MARKER = "# --table-end--";
	private static final String CONFIG_FILE_FIELDS_START = "# --table-fields-start--";
	private static final String CONFIG_FILE_FIELDS_END = "# --table-fields-end--";

	private Class<T> dataClass;
	private String tableName;
	private List<DatabaseFieldConfig> fieldConfigs;
	private FieldType[] fieldTypes;
	private Constructor<T> constructor;

	public DatabaseTableConfig() {
		// for spring
	}

	/**
	 * Setup a table config associated with the dataClass and field configurations. The table-name will be extracted
	 * from the dataClass.
	 */
	public DatabaseTableConfig(Class<T> dataClass, List<DatabaseFieldConfig> fieldConfigs) {
		this(dataClass, extractTableName(dataClass), fieldConfigs);
	}

	/**
	 * Setup a table config associated with the dataClass, table-name, and field configurations.
	 */
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

	/**
	 * Initialize the class if this is being called with Spring.
	 */
	public void initialize() {
		if (dataClass == null) {
			throw new IllegalStateException("dataClass was never set on " + getClass().getSimpleName());
		}
		if (tableName == null) {
			tableName = extractTableName(dataClass);
		}
	}

	/**
	 * Load a table configuration in from a text-file reader.
	 * 
	 * @return A config if any of the fields were set otherwise null if we reach EOF.
	 */
	public static <T> DatabaseTableConfig<T> fromReader(BufferedReader reader) throws SQLException {
		DatabaseTableConfig<T> config = new DatabaseTableConfig<T>();
		boolean anything = false;
		while (true) {
			String line;
			try {
				line = reader.readLine();
			} catch (IOException e) {
				throw SqlExceptionUtil.create("Could not read DatabaseTableConfig from stream", e);
			}
			if (line == null) {
				break;
			}
			// we do this so we can support multiple class configs per file
			if (line.equals(CONFIG_FILE_END_MARKER)) {
				break;
			}
			// we do this so we can support multiple class configs per file
			if (line.equals(CONFIG_FILE_FIELDS_START)) {
				readFields(reader, config);
				continue;
			}
			// skip empty lines or comments
			if (line.length() == 0 || line.startsWith("#") || line.equals(CONFIG_FILE_START_MARKER)) {
				continue;
			}
			String[] parts = line.split("=", -2);
			if (parts.length != 2) {
				throw new SQLException("DatabaseTableConfig reading from stream cannot parse line: " + line);
			}
			readTableField(config, parts[0], parts[1]);
			anything = true;
		}
		// if we got any config lines then we return the config
		if (anything) {
			return config;
		} else {
			// otherwise we return null for none
			return null;
		}
	}

	/**
	 * Write the table configuration to a buffered writer.
	 */
	public void write(BufferedWriter writer) throws SQLException {
		try {
			writeConfig(writer);
		} catch (IOException e) {
			throw SqlExceptionUtil.create("Could not write config to writer", e);
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
	public void extractFieldTypes(ConnectionSource connectionSource) throws SQLException {
		if (fieldTypes == null) {
			if (fieldConfigs == null) {
				fieldTypes = extractFieldTypes(connectionSource, dataClass, tableName);
			} else {
				fieldTypes = convertFieldConfigs(connectionSource, tableName, fieldConfigs);
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
	 * Return the constructor for this class. If not constructor has been set on the class then it will be found on the
	 * class through reflection.
	 */
	public Constructor<T> getConstructor() {
		if (constructor == null) {
			constructor = findNoArgConstructor(dataClass);
		}
		return constructor;
	}

	// @NotRequired
	public void setConstructor(Constructor<T> constructor) {
		this.constructor = constructor;
	}

	/**
	 * Extract the DatabaseTableConfig for a particular class by looking for class and field annotations. This is used
	 * by internal classes to configure a class.
	 */
	public static <T> DatabaseTableConfig<T> fromClass(ConnectionSource connectionSource, Class<T> clazz)
			throws SQLException {
		String tableName = extractTableName(clazz);
		if (connectionSource.getDatabaseType().isEntityNamesMustBeUpCase()) {
			tableName = tableName.toUpperCase();
		}
		return new DatabaseTableConfig<T>(clazz, tableName, extractFieldTypes(connectionSource, clazz, tableName));
	}

	/**
	 * Extract and return the table name for a class.
	 */
	public static <T> String extractTableName(Class<T> clazz) {
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

	/**
	 * Find and return the field-type of the id field in this class.
	 */
	public static <T> FieldType extractIdFieldType(ConnectionSource connectionSource, Class<T> clazz, String tableName)
			throws SQLException {
		for (Class<?> classWalk = clazz; classWalk != null; classWalk = classWalk.getSuperclass()) {
			for (Field field : classWalk.getDeclaredFields()) {
				FieldType fieldType = FieldType.createFieldType(connectionSource, tableName, field, clazz);
				if (fieldType != null
						&& (fieldType.isId() || fieldType.isGeneratedId() || fieldType.isGeneratedIdSequence())) {
					return fieldType;
				}
			}
		}
		return null;
	}

	/**
	 * Locate the no arg constructor for the class.
	 */
	public static <T> Constructor<T> findNoArgConstructor(Class<T> dataClass) {
		Constructor<T>[] constructors;
		try {
			@SuppressWarnings("unchecked")
			Constructor<T>[] consts = (Constructor<T>[]) dataClass.getDeclaredConstructors();
			// i do this [grossness] to be able to move the Suppress inside the method
			constructors = consts;
		} catch (Exception e) {
			throw new IllegalArgumentException("Can't lookup declared constructors for " + dataClass, e);
		}
		for (Constructor<T> con : constructors) {
			if (con.getParameterTypes().length == 0) {
				if (!con.isAccessible()) {
					try {
						con.setAccessible(true);
					} catch (SecurityException e) {
						throw new IllegalArgumentException("Could not open access to constructor for " + dataClass);
					}
				}
				return con;
			}
		}
		if (dataClass.getEnclosingClass() == null) {
			throw new IllegalArgumentException("Can't find a no-arg constructor for " + dataClass);
		} else {
			throw new IllegalArgumentException("Can't find a no-arg constructor for " + dataClass
					+ ".  Missing static on inner class?");
		}
	}

	private static <T> FieldType[] extractFieldTypes(ConnectionSource connectionSource, Class<T> clazz, String tableName)
			throws SQLException {
		List<FieldType> fieldTypes = new ArrayList<FieldType>();
		for (Class<?> classWalk = clazz; classWalk != null; classWalk = classWalk.getSuperclass()) {
			for (Field field : classWalk.getDeclaredFields()) {
				FieldType fieldType = FieldType.createFieldType(connectionSource, tableName, field, clazz);
				if (fieldType != null) {
					fieldTypes.add(fieldType);
				}
			}
		}
		if (fieldTypes.isEmpty()) {
			throw new IllegalArgumentException("No fields have a " + DatabaseField.class.getSimpleName()
					+ " annotation in " + clazz);
		}
		return fieldTypes.toArray(new FieldType[fieldTypes.size()]);
	}

	private FieldType[] convertFieldConfigs(ConnectionSource connectionSource, String tableName,
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
					fieldType = new FieldType(connectionSource, tableName, field, fieldConfig, dataClass);
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

	// field names in the config file
	private static final String FIELD_NAME_DATA_CLASS = "dataClass";
	private static final String FIELD_NAME_TABLE_NAME = "tableName";

	/**
	 * Write the config to the writer.
	 */
	private void writeConfig(BufferedWriter writer) throws IOException, SQLException {
		writer.append(CONFIG_FILE_START_MARKER);
		writer.newLine();
		if (dataClass != null) {
			writer.append(FIELD_NAME_DATA_CLASS).append('=').append(dataClass.getName());
			writer.newLine();
		}
		if (tableName != null) {
			writer.append(FIELD_NAME_TABLE_NAME).append('=').append(tableName);
			writer.newLine();
		}
		writer.append(CONFIG_FILE_FIELDS_START);
		writer.newLine();
		if (fieldConfigs != null) {
			for (DatabaseFieldConfig field : fieldConfigs) {
				field.write(writer);
			}
		}
		writer.append(CONFIG_FILE_FIELDS_END);
		writer.newLine();
		writer.append(CONFIG_FILE_END_MARKER);
		writer.newLine();
	}

	/**
	 * Read a field into our table configuration for field=value line.
	 */
	private static <T> void readTableField(DatabaseTableConfig<T> config, String field, String value) {
		if (field.equals(FIELD_NAME_DATA_CLASS)) {
			try {
				@SuppressWarnings("unchecked")
				Class<T> clazz = (Class<T>) Class.forName(value);
				config.dataClass = clazz;
			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException("Unknown class specified for dataClass: " + value);
			}
		} else if (field.equals(FIELD_NAME_TABLE_NAME)) {
			config.tableName = value;
		}
	}

	/**
	 * Read all of the fields information from the configuration file.
	 */
	private static <T> void readFields(BufferedReader reader, DatabaseTableConfig<T> config) throws SQLException {
		List<DatabaseFieldConfig> fields = new ArrayList<DatabaseFieldConfig>();
		while (true) {
			String line;
			try {
				line = reader.readLine();
			} catch (IOException e) {
				throw SqlExceptionUtil.create("Could not read next field from config file", e);
			}
			if (line == null || line.equals(CONFIG_FILE_FIELDS_END)) {
				break;
			}
			DatabaseFieldConfig fieldConfig = DatabaseFieldConfig.fromReader(reader);
			if (fieldConfig == null) {
				break;
			}
			fields.add(fieldConfig);
		}
		config.setFieldConfigs(fields);
	}
}
