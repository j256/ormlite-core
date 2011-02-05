package com.j256.ormlite.stmt.mapped;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.stmt.GenericRowMapper;
import com.j256.ormlite.support.DatabaseResults;
import com.j256.ormlite.table.TableInfo;

/**
 * Abstract mapped statement for queries which handle the creating of a new object and the row mapping functionality.
 * 
 * @author graywatson
 */
public abstract class BaseMappedQuery<T, ID> extends BaseMappedStatement<T, ID> implements GenericRowMapper<T> {

	protected final FieldType[] resultsFieldTypes;
	// cache of column names to results position
	private Map<String, Integer> columnPositions = null;

	protected BaseMappedQuery(TableInfo<T> tableInfo, String statement, List<FieldType> argFieldTypeList,
			List<FieldType> resultFieldTypeList) {
		super(tableInfo, statement, argFieldTypeList);
		this.resultsFieldTypes = resultFieldTypeList.toArray(new FieldType[resultFieldTypeList.size()]);
	}

	public T mapRow(DatabaseResults results) throws SQLException {
		if (columnPositions == null) {
			columnPositions = new HashMap<String, Integer>();
		}
		// create our instance
		T instance = tableInfo.createObject();
		// populate its fields
		for (FieldType fieldType : resultsFieldTypes) {
			Object val = fieldType.resultToJava(results, columnPositions);
			fieldType.assignField(instance, val);
		}
		return instance;
	}

	public FieldType[] getResultsFieldTypes() {
		return resultsFieldTypes;
	}
}
