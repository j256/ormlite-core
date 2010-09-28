package com.j256.ormlite.stmt.mapped;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.table.TableInfo;

/**
 * Mapped statement for refreshing the fields in an object.
 * 
 * @author graywatson
 */
public class MappedRefresh<T, ID> extends MappedQueryForId<T, ID> {

	private MappedRefresh(TableInfo<T> tableInfo, String statement, List<FieldType> argFieldTypeList,
			List<FieldType> resultFieldTypeList) {
		super(tableInfo, statement, argFieldTypeList, resultFieldTypeList, "refresh");
	}

	@Override
	protected ID getId(Object obj) throws SQLException {
		@SuppressWarnings("unchecked")
		T data = (T) obj;
		// this is necessary because of a 1.6 compilation error
		@SuppressWarnings("unchecked")
		ID id = (ID) idField.getConvertedFieldValue(data);
		return id;
	}

	@Override
	protected void postProcessResult(Object obj, T result) throws SQLException {
		@SuppressWarnings("unchecked")
		T data = (T) obj;
		// copy each field into the passed in object
		for (FieldType fieldType : resultsFieldTypes) {
			if (fieldType != idField) {
				fieldType.assignField(data, fieldType.getConvertedFieldValue(result));
			}
		}
	}

	public static <T, ID> MappedRefresh<T, ID> build(DatabaseType databaseType, TableInfo<T> tableInfo)
			throws SQLException {
		List<FieldType> argFieldTypeList = new ArrayList<FieldType>();
		List<FieldType> resultFieldTypeList = new ArrayList<FieldType>();
		String statement = buildStatement(databaseType, tableInfo, argFieldTypeList, resultFieldTypeList);
		return new MappedRefresh<T, ID>(tableInfo, statement, argFieldTypeList, resultFieldTypeList);
	}
}
