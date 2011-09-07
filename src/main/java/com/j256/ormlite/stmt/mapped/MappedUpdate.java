package com.j256.ormlite.stmt.mapped;

import java.sql.SQLException;

import com.j256.ormlite.dao.ObjectCache;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.TableInfo;

/**
 * Mapped statement for updating an object.
 * 
 * @author graywatson
 */
public class MappedUpdate<T, ID> extends BaseMappedStatement<T, ID> {

	private MappedUpdate(TableInfo<T, ID> tableInfo, String statement, FieldType[] argFieldTypes) {
		super(tableInfo, statement, argFieldTypes);
	}

	public static <T, ID> MappedUpdate<T, ID> build(DatabaseType databaseType, TableInfo<T, ID> tableInfo)
			throws SQLException {
		FieldType idField = tableInfo.getIdField();
		if (idField == null) {
			throw new SQLException("Cannot update " + tableInfo.getDataClass() + " because it doesn't have an id field");
		}
		if (tableInfo.getFieldTypes().length == 1) {
			throw new SQLException("Cannot update " + tableInfo.getDataClass()
					+ " with only the id field.  You should use updateId().");
		}
		StringBuilder sb = new StringBuilder(64);
		appendTableName(databaseType, sb, "UPDATE ", tableInfo.getTableName());
		boolean first = true;
		int argFieldC = 0;
		// first we count up how many arguments we are going to have
		for (FieldType fieldType : tableInfo.getFieldTypes()) {
			if (isFieldUpdatable(fieldType, idField)) {
				argFieldC++;
			}
		}
		// one more for where id = ?
		argFieldC++;
		FieldType[] argFieldTypes = new FieldType[argFieldC];
		argFieldC = 0;
		for (FieldType fieldType : tableInfo.getFieldTypes()) {
			if (!isFieldUpdatable(fieldType, idField)) {
				continue;
			}
			if (first) {
				sb.append("SET ");
				first = false;
			} else {
				sb.append(", ");
			}
			appendFieldColumnName(databaseType, sb, fieldType, null);
			argFieldTypes[argFieldC++] = fieldType;
			sb.append("= ?");
		}
		sb.append(' ');
		appendWhereId(databaseType, idField, sb, null);
		argFieldTypes[argFieldC++] = idField;
		return new MappedUpdate<T, ID>(tableInfo, sb.toString(), argFieldTypes);
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

	private static boolean isFieldUpdatable(FieldType fieldType, FieldType idField) {
		if (fieldType == idField || fieldType.isForeignCollection()) {
			return false;
		} else {
			return true;
		}
	}
}
