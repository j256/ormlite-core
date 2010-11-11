package com.j256.ormlite.stmt.mapped;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
public class MappedDeleteCollection<T, ID> extends BaseMappedStatement<T> {

	private MappedDeleteCollection(TableInfo<T> tableInfo, String statement, List<FieldType> argFieldTypeList) {
		super(tableInfo, statement, argFieldTypeList);
	}

	/**
	 * Delete all of the objects in the collection. This builds a {@link MappedDeleteCollection} on the fly because the
	 * datas could be variable sized.
	 */
	public static <T, ID> int deleteObjects(DatabaseType databaseType, TableInfo<T> tableInfo,
			DatabaseConnection databaseConnection, Collection<T> datas) throws SQLException {
		MappedDeleteCollection<T, ID> deleteCollection =
				MappedDeleteCollection.build(databaseType, tableInfo, datas.size());
		Object[] fieldObjects = new Object[datas.size()];
		int objC = 0;
		for (T data : datas) {
			fieldObjects[objC] = tableInfo.getIdField().convertJavaToSqlArgValue(data);
			objC++;
		}
		return updateRows(databaseConnection, deleteCollection, fieldObjects);
	}

	/**
	 * Delete all of the objects in the collection. This builds a {@link MappedDeleteCollection} on the fly because the
	 * ids could be variable sized.
	 */
	public static <T, ID> int deleteIds(DatabaseType databaseType, TableInfo<T> tableInfo,
			DatabaseConnection databaseConnection, Collection<ID> ids) throws SQLException {
		MappedDeleteCollection<T, ID> deleteCollection =
				MappedDeleteCollection.build(databaseType, tableInfo, ids.size());
		Object[] idsArray = ids.toArray(new Object[ids.size()]);
		return updateRows(databaseConnection, deleteCollection, idsArray);
	}

	/**
	 * This is private because the execute is the only method that should be called here.
	 */
	private static <T, ID> MappedDeleteCollection<T, ID> build(DatabaseType databaseType, TableInfo<T> tableInfo,
			int dataSize) throws SQLException {
		FieldType idField = tableInfo.getIdField();
		if (idField == null) {
			throw new SQLException("Cannot delete " + tableInfo.getDataClass()
					+ " because it doesn't have an id field defined");
		}
		StringBuilder sb = new StringBuilder();
		List<FieldType> argFieldTypeList = new ArrayList<FieldType>();
		appendTableName(databaseType, sb, "DELETE FROM ", tableInfo.getTableName());
		appendWhereIds(databaseType, idField, sb, dataSize, argFieldTypeList);
		return new MappedDeleteCollection<T, ID>(tableInfo, sb.toString(), argFieldTypeList);
	}

	private static <T, ID> int updateRows(DatabaseConnection databaseConnection,
			MappedDeleteCollection<T, ID> deleteCollection, Object[] args) throws SQLException {
		try {
			int rowC = databaseConnection.delete(deleteCollection.statement, args, deleteCollection.argSqlTypes);
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
			List<FieldType> fieldTypeList) {
		sb.append("WHERE ");
		databaseType.appendEscapedEntityName(sb, idField.getDbColumnName());
		sb.append(" IN (");
		boolean first = true;
		for (int i = 0; i < numDatas; i++) {
			if (first) {
				first = false;
			} else {
				sb.append(',');
			}
			sb.append('?');
			if (fieldTypeList != null) {
				fieldTypeList.add(idField);
			}
		}
		sb.append(") ");
	}
}
