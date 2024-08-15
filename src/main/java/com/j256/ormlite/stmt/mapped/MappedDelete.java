package com.j256.ormlite.stmt.mapped;

import java.sql.SQLException;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ObjectCache;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.TableInfo;

/**
 * A mapped statement for deleting an object.
 * 
 * @author graywatson
 */
public class MappedDelete<T, ID> extends BaseMappedStatement<T, ID> {

	private MappedDelete(Dao<T, ID> dao, TableInfo<T, ID> tableInfo, String statement, FieldType[] argFieldTypes) {
		super(dao, tableInfo, statement, argFieldTypes);
	}

	public static <T, ID> MappedDelete<T, ID> build(Dao<T, ID> dao, TableInfo<T, ID> tableInfo) throws SQLException {
		FieldType idField = tableInfo.getIdField();
		if (idField == null) {
			throw new SQLException(
					"Cannot delete from " + tableInfo.getDataClass() + " because it doesn't have an id field");
		}
		StringBuilder sb = new StringBuilder(64);
		DatabaseType databaseType = dao.getConnectionSource().getDatabaseType();
		appendTableName(databaseType, sb, "DELETE FROM ", tableInfo);
		appendWhereFieldEq(databaseType, idField, sb, null);
		return new MappedDelete<T, ID>(dao, tableInfo, sb.toString(), new FieldType[] { idField });
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
			throw new SQLException("Unable to run delete stmt on object " + data + ": " + statement, e);
		}
	}

	/**
	 * Delete the object from the database.
	 */
	public int deleteById(DatabaseConnection databaseConnection, ID id, ObjectCache objectCache) throws SQLException {
		try {
			Object[] args = new Object[] { convertIdToFieldObject(id) };
			int rowC = databaseConnection.delete(statement, args, argFieldTypes);
			logger.debug("delete data with statement '{}' and {} args, changed {} rows", statement, args.length, rowC);
			if (args.length > 0) {
				// need to do the (Object) cast to force args to be a single object
				logger.trace("delete arguments: {}", (Object) args);
			}
			if (rowC > 0 && objectCache != null) {
				objectCache.remove(clazz, id);
			}
			return rowC;
		} catch (SQLException e) {
			throw new SQLException("Unable to run deleteById stmt on id " + id + ": " + statement, e);
		}
	}
}
