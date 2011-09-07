package com.j256.ormlite.stmt.mapped;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.dao.ObjectCache;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.TableInfo;

/**
 * Abstract mapped statement which has common statements used by the subclasses.
 * 
 * @author graywatson
 */
public abstract class BaseMappedStatement<T, ID> {

	protected static Logger logger = LoggerFactory.getLogger(BaseMappedStatement.class);

	protected final TableInfo<T, ID> tableInfo;
	protected final Class<T> clazz;
	protected final FieldType idField;
	protected final String statement;
	protected final FieldType[] argFieldTypes;

	protected BaseMappedStatement(TableInfo<T, ID> tableInfo, String statement, FieldType[] argFieldTypes) {
		this.tableInfo = tableInfo;
		this.clazz = tableInfo.getDataClass();
		this.idField = tableInfo.getIdField();
		this.statement = statement;
		this.argFieldTypes = argFieldTypes;
	}

	/**
	 * Update the object in the database.
	 */
	public int update(DatabaseConnection databaseConnection, T data, ObjectCache objectCache) throws SQLException {
		try {
			Object[] args = getFieldObjects(data);
			int rowC = databaseConnection.update(statement, args, argFieldTypes);
			if (rowC > 0 && objectCache != null) {
				// if we've changed something then see if we need to update our cache
				Object id = idField.extractJavaFieldValue(data);
				T cachedData = objectCache.get(clazz, id);
				if (cachedData != null && cachedData != data) {
					// copy each field from the updated data into the cached object
					for (FieldType fieldType : tableInfo.getFieldTypes()) {
						if (fieldType != idField) {
							fieldType.assignField(cachedData, fieldType.extractJavaFieldValue(data), false, objectCache);
						}
					}
				}
			}
			logger.debug("update data with statement '{}' and {} args, changed {} rows", statement, args.length, rowC);
			if (args.length > 0) {
				// need to do the (Object) cast to force args to be a single object
				logger.trace("update arguments: {}", (Object) args);
			}
			return rowC;
		} catch (SQLException e) {
			throw SqlExceptionUtil.create("Unable to run update stmt on object " + data + ": " + statement, e);
		}
	}

	/**
	 * Delete the object from the database.
	 */
	public int delete(DatabaseConnection databaseConnection, T data, ObjectCache objectCache) throws SQLException {
		try {
			Object[] args = getFieldObjects(data);
			int rowC = databaseConnection.delete(statement, args, argFieldTypes);
			logger.debug("delete data with statement '{}' and {} args, changed {} rows", statement, args.length, rowC);
			if (args.length > 0) {
				// need to do the (Object) cast to force args to be a single object
				logger.trace("delete arguments: {}", (Object) args);
			}
			if (rowC > 0 && objectCache != null) {
				Object id = idField.extractJavaFieldToSqlArgValue(data);
				objectCache.remove(clazz, id);
			}
			return rowC;
		} catch (SQLException e) {
			throw SqlExceptionUtil.create("Unable to run delete stmt on object " + data + ": " + statement, e);
		}
	}

	public String getStatement() {
		return statement;
	}

	/**
	 * Return the array of field objects pulled from the data object.
	 */
	protected Object[] getFieldObjects(Object data) throws SQLException {
		Object[] objects = new Object[argFieldTypes.length];
		for (int i = 0; i < argFieldTypes.length; i++) {
			FieldType fieldType = argFieldTypes[i];
			if (fieldType.isAllowGeneratedIdInsert()) {
				objects[i] = fieldType.getFieldValueIfNotDefault(data);
			} else {
				objects[i] = fieldType.extractJavaFieldToSqlArgValue(data);
			}
			if (objects[i] == null && fieldType.getDefaultValue() != null) {
				objects[i] = fieldType.getDefaultValue();
			}
		}
		return objects;
	}

	/**
	 * Return a field object converted from an id.
	 */
	protected Object convertIdToFieldObject(ID id) throws SQLException {
		return idField.convertJavaFieldToSqlArgValue(id);
	}

	/**
	 * Return a field-object for the id extracted from the data.
	 */
	protected Object extractIdToFieldObject(T data) throws SQLException {
		return idField.extractJavaFieldToSqlArgValue(data);
	}

	static void appendWhereId(DatabaseType databaseType, FieldType idField, StringBuilder sb,
			List<FieldType> fieldTypeList) {
		sb.append("WHERE ");
		appendFieldColumnName(databaseType, sb, idField, fieldTypeList);
		sb.append("= ?");
	}

	static void appendTableName(DatabaseType databaseType, StringBuilder sb, String prefix, String tableName) {
		if (prefix != null) {
			sb.append(prefix);
		}
		databaseType.appendEscapedEntityName(sb, tableName);
		sb.append(' ');
	}

	static void appendFieldColumnName(DatabaseType databaseType, StringBuilder sb, FieldType fieldType,
			List<FieldType> fieldTypeList) {
		databaseType.appendEscapedEntityName(sb, fieldType.getDbColumnName());
		if (fieldTypeList != null) {
			fieldTypeList.add(fieldType);
		}
		sb.append(' ');
	}

	@Override
	public String toString() {
		return "MappedStatement: " + statement;
	}
}
