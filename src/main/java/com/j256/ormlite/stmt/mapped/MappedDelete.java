package com.j256.ormlite.stmt.mapped;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.table.TableInfo;

/**
 * A mapped statement for deleting an object.
 * 
 * @author graywatson
 */
public class MappedDelete<T> extends BaseMappedStatement<T> {

	private MappedDelete(TableInfo<T> tableInfo, String statement, List<FieldType> argFieldTypeList) {
		super(tableInfo, statement, argFieldTypeList);
	}

	public static <T> MappedDelete<T> build(DatabaseType databaseType, TableInfo<T> tableInfo) throws SQLException {
		FieldType idField = tableInfo.getIdField();
		if (idField == null) {
			throw new SQLException("Cannot delete from " + tableInfo.getDataClass()
					+ " because it doesn't have an id field");
		}
		StringBuilder sb = new StringBuilder();
		List<FieldType> argFieldTypeList = new ArrayList<FieldType>();
		appendTableName(databaseType, sb, "DELETE FROM ", tableInfo.getTableName());
		appendWhereId(databaseType, idField, sb, argFieldTypeList);
		return new MappedDelete<T>(tableInfo, sb.toString(), argFieldTypeList);
	}
}
