package com.j256.ormlite.stmt.mapped;

import java.sql.SQLException;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ObjectCache;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.TableInfo;

/**
 * Mapped statement for updating an object's ID field.
 * 
 * @author graywatson
 */
public class MappedUpdateId<T, ID> extends BaseMappedStatement<T, ID> {

	private MappedUpdateId(Dao<T, ID> dao, TableInfo<T, ID> tableInfo, String statement, FieldType[] argFieldTypes) {
		super(dao, tableInfo, statement, argFieldTypes);
	}

	/**
	 * Update the id field of the object in the database.
	 */
	public int execute(DatabaseConnection databaseConnection, T data, ID newId, ObjectCache objectCache)
			throws SQLException {
		try {
			// the arguments are the new-id and old-id
			Object[] args = new Object[] { convertIdToFieldObject(newId), extractIdToFieldObject(data) };
			int rowC = databaseConnection.update(statement, args, argFieldTypes);
			if (rowC > 0) {
				if (objectCache != null) {
					Object oldId = idField.extractJavaFieldValue(data);
					T obj = objectCache.updateId(clazz, oldId, newId);
					if (obj != null && obj != data) {
						// if our cached value is not the data that will be updated then we need to update it specially
						idField.assignField(connectionSource, obj, newId, false, objectCache);
					}
				}
				// adjust the object to assign the new id
				idField.assignField(connectionSource, data, newId, false, objectCache);
			}
			logger.debug("updating-id with statement '{}' and {} args, changed {} rows", statement, args.length, rowC);
			if (args.length > 0) {
				// need to do the cast otherwise we only print the first object in args
				logger.trace("updating-id arguments: {}", (Object) args);
			}
			return rowC;
		} catch (SQLException e) {
			throw new SQLException("Unable to run update-id stmt on object " + data + ": " + statement, e);
		}
	}

	public static <T, ID> MappedUpdateId<T, ID> build(Dao<T, ID> dao, TableInfo<T, ID> tableInfo) throws SQLException {
		FieldType idField = tableInfo.getIdField();
		if (idField == null) {
			throw new SQLException(
					"Cannot update-id in " + tableInfo.getDataClass() + " because it doesn't have an id field");
		}
		StringBuilder sb = new StringBuilder(64);
		DatabaseType databaseType = dao.getConnectionSource().getDatabaseType();
		appendTableName(databaseType, sb, "UPDATE ", tableInfo);
		sb.append("SET ");
		appendFieldColumnName(databaseType, sb, idField, null);
		sb.append("= ? ");
		appendWhereFieldEq(databaseType, idField, sb, null);
		return new MappedUpdateId<T, ID>(dao, tableInfo, sb.toString(), new FieldType[] { idField, idField });
	}

	/**
	 * Return a field-object for the id extracted from the data.
	 */
	private Object extractIdToFieldObject(T data) throws SQLException {
		return idField.extractJavaFieldToSqlArgValue(data);
	}
}
