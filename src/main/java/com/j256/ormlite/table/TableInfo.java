package com.j256.ormlite.table;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.FieldType;

/**
 * Information about a database table including the associated tableName, class, constructor, and the included fields.
 * 
 * @param <T>
 *            The class that the code will be operating on.
 * @param <ID>
 *            The class of the ID column associated with the class. The T class does not require an ID field. The class
 *            needs an ID parameter however so you can use Void or Object to satisfy the compiler.
 * @author graywatson
 */
public class TableInfo<T, ID> {

	private static final FieldType[] NO_FOREIGN_COLLECTIONS = new FieldType[0];

	private final DatabaseType databaseType;
	private final Class<T> dataClass;
	private final String schemaName;
	private final String tableName;
	private final FieldType[] fieldTypes;
	private final FieldType[] foreignCollections;
	private final FieldType idField;
	private final boolean foreignAutoCreate;
	private final Map<String, FieldType> fieldNameMap;

	/**
	 * Creates a holder of information about a table/class.
	 * 
	 * @param databaseType
	 *            Database type we are storing the class in.
	 * @param dataClass
	 *            Class that we are holding information about.
	 */
	public TableInfo(DatabaseType databaseType, Class<T> dataClass) throws SQLException {
		this(databaseType, DatabaseTableConfig.fromClass(databaseType, dataClass));
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

		this.databaseType = databaseType;
		this.dataClass = tableConfig.getDataClass();
		this.schemaName = tableConfig.getSchemaName();
		this.tableName = tableConfig.getTableName();
		this.fieldTypes = tableConfig.getFieldTypes(databaseType);
		// find the id field
		FieldType findIdFieldType = null;
		boolean foreignAutoCreate = false;
		int foreignCollectionCount = 0;
		Map<String, FieldType> mutableFieldNameMap = new HashMap<String, FieldType>();
		for (FieldType fieldType : fieldTypes) {
			if (fieldType.isId() || fieldType.isGeneratedId() || fieldType.isGeneratedIdSequence()) {
				if (findIdFieldType != null) {
					throw new SQLException("More than 1 idField configured for class " + dataClass + " ("
							+ findIdFieldType + "," + fieldType + ")");
				}
				findIdFieldType = fieldType;
			}
			if (fieldType.isForeignAutoCreate()) {
				foreignAutoCreate = true;
			}
			if (fieldType.isForeignCollection()) {
				foreignCollectionCount++;
			}
			mutableFieldNameMap.put(databaseType.downCaseString(fieldType.getColumnName(), true), fieldType);
		}
		this.fieldNameMap = Collections.unmodifiableMap(mutableFieldNameMap);
		// can be null if there is no id field
		this.idField = findIdFieldType;
		this.foreignAutoCreate = foreignAutoCreate;
		if (foreignCollectionCount == 0) {
			this.foreignCollections = NO_FOREIGN_COLLECTIONS;
		} else {
			this.foreignCollections = new FieldType[foreignCollectionCount];
			foreignCollectionCount = 0;
			for (FieldType fieldType : fieldTypes) {
				if (fieldType.isForeignCollection()) {
					this.foreignCollections[foreignCollectionCount] = fieldType;
					foreignCollectionCount++;
				}
			}
		}
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
	 * Return the name of the schema or null if none.
	 */
	public String getSchemaName() {
		return schemaName;
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
		String downColumnName = databaseType.downCaseString(columnName, true);
		FieldType match = fieldNameMap.get(downColumnName);
		// if column name is found, return it
		if (match != null) {
			return match;
		}
		// look to see if someone is using the field-name instead of column-name
		for (FieldType fieldType : fieldTypes) {
			String downFieldName = databaseType.downCaseString(fieldType.getFieldName(), true);
			if (downFieldName.equals(downColumnName)) {
				String downFieldColumnName = databaseType.downCaseString(fieldType.getColumnName(), true);
				throw new IllegalArgumentException("Unknown column-name '" + downColumnName
						+ "', maybe field-name instead of column-name '" + downFieldColumnName + "' from table '"
						+ tableName + "' with columns: " + fieldNameMap.keySet());
			}
		}
		throw new IllegalArgumentException("Unknown column-name '" + downColumnName + "' in table '" + tableName
				+ "' with columns: " + fieldNameMap.keySet());
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
		StringBuilder sb = new StringBuilder(64);
		sb.append(object.getClass().getSimpleName());
		for (FieldType fieldType : fieldTypes) {
			sb.append(' ').append(fieldType.getColumnName()).append('=');
			try {
				sb.append(fieldType.extractJavaFieldValue(object));
			} catch (Exception e) {
				throw new IllegalStateException("Could not generate toString of field " + fieldType, e);
			}
		}
		return sb.toString();
	}

	/**
	 * Return true if we can update this object via its ID.
	 */
	public boolean isUpdatable() {
		// to update we must have an id field and there must be more than just the id field
		return (idField != null && fieldTypes.length > 1);
	}

	/**
	 * Return true if one of the fields has {@link DatabaseField#foreignAutoCreate()} enabled.
	 */
	public boolean isForeignAutoCreate() {
		return foreignAutoCreate;
	}

	/**
	 * Return an array with the fields that are {@link ForeignCollection}s or a blank array if none.
	 */
	public FieldType[] getForeignCollections() {
		return foreignCollections;
	}

	/**
	 * Return true if this table information has a field with this columnName as set by
	 * {@link DatabaseField#columnName()} or the field name if not set.
	 */
	public boolean hasColumnName(String columnName) {
		for (FieldType fieldType : fieldTypes) {
			if (fieldType.getColumnName().equals(columnName)) {
				return true;
			}
		}
		return false;
	}
}
