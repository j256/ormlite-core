package com.j256.ormlite.stmt.mapped;

import java.sql.SQLException;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.TableInfo;

/**
 * Mapped statement for refreshing the fields in an object.
 * 
 * @author graywatson
 */
public class MappedRefresh<T, ID> extends MappedQueryForId<T, ID> {

	private MappedRefresh(TableInfo<T, ID> tableInfo, String statement, FieldType[] argFieldTypes,
			FieldType[] resultFieldTypes) {
		super(tableInfo, statement, argFieldTypes, resultFieldTypes, "refresh");
	}

	/**
	 * Execute our refresh query statement and then update all of the fields in data with the fields from the result.
	 * 
	 * @return 1 if we found the object in the table by id or 0 if not.
	 */
	public int executeRefresh(DatabaseConnection databaseConnection, T data) throws SQLException {
		@SuppressWarnings("unchecked")
		ID id = (ID) idField.extractJavaFieldValue(data);
		T result = super.execute(databaseConnection, id);
		if (result == null) {
			return 0;
		} else {
			// copy each field from the result into the passed in object
			for (FieldType fieldType : resultsFieldTypes) {
				if (fieldType != idField) {
					fieldType.assignField(data, fieldType.extractJavaFieldValue(result));
				}
			}
			return 1;
		}
	}

	public static <T, ID> MappedRefresh<T, ID> build(DatabaseType databaseType, TableInfo<T, ID> tableInfo,
			Dao<T, ID> dao) throws SQLException {
		String statement = buildStatement(databaseType, tableInfo, dao);
		return new MappedRefresh<T, ID>(tableInfo, statement, new FieldType[] { tableInfo.getIdField() },
				tableInfo.getFieldTypes());
	}
}
