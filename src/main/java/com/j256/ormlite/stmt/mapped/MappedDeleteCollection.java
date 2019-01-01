package com.j256.ormlite.stmt.mapped;

import java.sql.SQLException;
import java.util.Collection;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ObjectCache;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.TableInfo;

/**
 * A mapped statement for deleting objects that correspond to a collection of IDs.
 * 
 * @author graywatson
 */
public class MappedDeleteCollection<T, ID> extends BaseMappedStatement<T, ID> {

	private MappedDeleteCollection(Dao<T, ID> dao, TableInfo<T, ID> tableInfo, String statement,
			FieldType[] argFieldTypes) {
		super(dao, tableInfo, statement, argFieldTypes);
	}

	/**
	 * Delete all of the objects in the collection. This builds a {@link MappedDeleteCollection} on the fly because the
	 * datas could be variable sized.
	 */
	public static <T, ID> int deleteObjects(Dao<T, ID> dao, TableInfo<T, ID> tableInfo,
			DatabaseConnection databaseConnection, Collection<T> datas, ObjectCache objectCache) throws SQLException {
		MappedDeleteCollection<T, ID> deleteCollection = MappedDeleteCollection.build(dao, tableInfo, datas.size());
		Object[] fieldObjects = new Object[datas.size()];
		FieldType idField = tableInfo.getIdField();
		int objC = 0;
		for (T data : datas) {
			fieldObjects[objC] = idField.extractJavaFieldToSqlArgValue(data);
			objC++;
		}
		return updateRows(databaseConnection, tableInfo.getDataClass(), deleteCollection, fieldObjects, objectCache);
	}

	/**
	 * Delete all of the objects in the collection. This builds a {@link MappedDeleteCollection} on the fly because the
	 * ids could be variable sized.
	 */
	public static <T, ID> int deleteIds(Dao<T, ID> dao, TableInfo<T, ID> tableInfo,
			DatabaseConnection databaseConnection, Collection<ID> ids, ObjectCache objectCache) throws SQLException {
		MappedDeleteCollection<T, ID> deleteCollection = MappedDeleteCollection.build(dao, tableInfo, ids.size());
		Object[] fieldObjects = new Object[ids.size()];
		FieldType idField = tableInfo.getIdField();
		int objC = 0;
		for (ID id : ids) {
			fieldObjects[objC] = idField.convertJavaFieldToSqlArgValue(id);
			objC++;
		}
		return updateRows(databaseConnection, tableInfo.getDataClass(), deleteCollection, fieldObjects, objectCache);
	}

	/**
	 * This is private because the execute is the only method that should be called here.
	 */
	private static <T, ID> MappedDeleteCollection<T, ID> build(Dao<T, ID> dao, TableInfo<T, ID> tableInfo, int dataSize)
			throws SQLException {
		FieldType idField = tableInfo.getIdField();
		if (idField == null) {
			throw new SQLException(
					"Cannot delete " + tableInfo.getDataClass() + " because it doesn't have an id field defined");
		}
		StringBuilder sb = new StringBuilder(128);
		DatabaseType databaseType = dao.getConnectionSource().getDatabaseType();
		appendTableName(databaseType, sb, "DELETE FROM ", tableInfo.getTableName());
		FieldType[] argFieldTypes = new FieldType[dataSize];
		appendWhereIds(databaseType, idField, sb, dataSize, argFieldTypes);
		return new MappedDeleteCollection<T, ID>(dao, tableInfo, sb.toString(), argFieldTypes);
	}

	private static <T, ID> int updateRows(DatabaseConnection databaseConnection, Class<T> clazz,
			MappedDeleteCollection<T, ID> deleteCollection, Object[] args, ObjectCache objectCache)
			throws SQLException {
		try {
			int rowC = databaseConnection.delete(deleteCollection.statement, args, deleteCollection.argFieldTypes);
			if (rowC > 0 && objectCache != null) {
				for (Object id : args) {
					objectCache.remove(clazz, id);
				}
			}
			logger.debug("delete-collection with statement '{}' and {} args, changed {} rows",
					deleteCollection.statement, args.length, rowC);
			if (args.length > 0) {
				// need to do the (Object) cast to force args to be a single object
				logger.trace("delete-collection arguments: {}", (Object) args);
			}
			return rowC;
		} catch (SQLException e) {
			throw SqlExceptionUtil.create("Unable to run delete collection stmt: " + deleteCollection.statement, e);
		}
	}

	private static void appendWhereIds(DatabaseType databaseType, FieldType idField, StringBuilder sb, int numDatas,
			FieldType[] fieldTypes) {
		sb.append("WHERE ");
		databaseType.appendEscapedEntityName(sb, idField.getColumnName());
		sb.append(" IN (");
		boolean first = true;
		for (int i = 0; i < numDatas; i++) {
			if (first) {
				first = false;
			} else {
				sb.append(',');
			}
			sb.append('?');
			if (fieldTypes != null) {
				fieldTypes[i] = idField;
			}
		}
		sb.append(") ");
	}
}
