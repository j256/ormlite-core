package com.j256.ormlite.table;

import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.support.ConnectionSource;

/**
 * Information about a database table including the associated tableName, class, constructor, and the included fields.
 * 
 * @param T
 *            The class that the code will be operating on.
 * @author graywatson
 */
public class TableInfo<T> {

	private final Class<T> dataClass;
	private final String tableName;
	private final FieldType[] fieldTypes;
	private final FieldType idField;
	private final Constructor<T> constructor;
	private Map<String, FieldType> fieldNameMap;

	/**
	 * Creates a holder of information about a table/class.
	 * 
	 * @param connectionSource
	 *            Source of our database connections.
	 * @param dataClass
	 *            Class that we are holding information about.
	 */
	public TableInfo(ConnectionSource connectionSource, Class<T> dataClass) throws SQLException {
		this(connectionSource.getDatabaseType(), DatabaseTableConfig.fromClass(connectionSource, dataClass));
	}

	public TableInfo(ConnectionSource connectionSource, DatabaseType databaseType, Class<T> dataClass)
			throws SQLException {
		this(databaseType, DatabaseTableConfig.fromClass(connectionSource, dataClass));
	}

	/**
	 * Creates a holder of information about a table/class.
	 * 
	 * @param databaseType
	 *            Database type we are storing the class in.
	 * @param tableConfig
	 *            Configuration for our table.
	 */
	public TableInfo(DatabaseType databaseType, DatabaseTableConfig<T> tableConfig) throws SQLException {
		this.dataClass = tableConfig.getDataClass();
		this.tableName = tableConfig.getTableName();
		this.fieldTypes = tableConfig.getFieldTypes(databaseType);
		// find the id field
		FieldType findIdFieldType = null;
		for (FieldType fieldType : fieldTypes) {
			if (fieldType.isId()) {
				if (findIdFieldType != null) {
					throw new SQLException("More than 1 idField configured for class " + dataClass + " ("
							+ findIdFieldType + "," + fieldType + ")");
				}
				findIdFieldType = fieldType;
			}
		}
		// if we just have 1 field and it is a generated-id then inserts will be blank which is not allowed.
		if (fieldTypes.length == 1 && findIdFieldType != null && findIdFieldType.isGeneratedId()) {
			throw new SQLException("Must have more than a single field which is a generated-id for class " + dataClass);
		}
		// can be null if there is no id field
		this.idField = findIdFieldType;
		this.constructor = findNoArgConstructor(dataClass);
	}

	/**
	 * Return the class associated with this object-info.
	 */
	public Class<T> getDataClass() {
		return dataClass;
	}

	/**
	 * Return the name of the table associated with the object.
	 */
	public String getTableName() {
		return tableName;
	}

	/**
	 * Return the array of field types associated with the object.
	 */
	public FieldType[] getFieldTypes() {
		return fieldTypes;
	}

	/**
	 * Return the {@link FieldType} associated with the columnName.
	 */
	public FieldType getFieldTypeByColumnName(String columnName) {
		if (fieldNameMap == null) {
			// build our alias map if we need it
			fieldNameMap = new HashMap<String, FieldType>();
			for (FieldType fieldType : fieldTypes) {
				fieldNameMap.put(fieldType.getDbColumnName(), fieldType);
			}
		}
		FieldType fieldType = fieldNameMap.get(columnName);
		// if column name is not found
		if (fieldType == null) {
			// look to see if someone is using the field-name instead of column-name
			for (FieldType fieldType2 : fieldTypes) {
				if (fieldType2.getFieldName().equals(columnName)) {
					throw new IllegalArgumentException("You should use columnName '" + fieldType2.getDbColumnName()
							+ "' for table " + tableName + " instead of fieldName '" + columnName + "'");
				}
			}
			throw new IllegalArgumentException("Unknown column name '" + columnName + "' in table " + tableName);
		}
		return fieldType;
	}

	/**
	 * Return the id-field associated with the object.
	 */
	public FieldType getIdField() {
		return idField;
	}

	/**
	 * Return a string representation of the object.
	 */
	public String objectToString(T object) {
		StringBuilder sb = new StringBuilder();
		sb.append(object.getClass().getSimpleName());
		for (FieldType fieldType : fieldTypes) {
			sb.append(' ').append(fieldType.getDbColumnName()).append("=");
			try {
				sb.append(fieldType.extractJavaFieldValue(object));
			} catch (Exception e) {
				throw new IllegalStateException("Could not generate toString of field " + fieldType, e);
			}
		}
		return sb.toString();
	}

	/**
	 * Create and return an object of this type using our reflection constructor.
	 */
	public T createObject() throws SQLException {
		boolean accessible = constructor.isAccessible();
		try {
			if (!accessible) {
				constructor.setAccessible(true);
			}
			// create our instance
			return constructor.newInstance();
		} catch (Exception e) {
			throw SqlExceptionUtil.create("Could not create object for " + dataClass, e);
		} finally {
			if (!accessible) {
				constructor.setAccessible(false);
			}
		}
	}

	/**
	 * Return true if we can update this object via its ID.
	 */
	public boolean isUpdatable() {
		// to update we must have an id field and there must be more than just the id field
		return (idField != null && fieldTypes.length > 1);
	}

	private Constructor<T> findNoArgConstructor(Class<T> dataClass) {
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
				return con;
			}
		}
		throw new IllegalArgumentException("Can't find a no-arg constructor for " + dataClass);
	}
}
