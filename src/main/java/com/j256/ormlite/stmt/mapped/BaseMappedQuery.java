package com.j256.ormlite.stmt.mapped;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.j256.ormlite.dao.BaseForeignCollection;
import com.j256.ormlite.dao.ObjectCache;
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
	private Object parent = null;

	protected BaseMappedQuery(TableInfo<T, ID> tableInfo, String statement, FieldType[] argFieldTypes,
			FieldType[] resultsFieldTypes) {
		super(tableInfo, statement, argFieldTypes);
		this.resultsFieldTypes = resultsFieldTypes;
	}

	public T mapRow(DatabaseResults results) throws SQLException {
		Map<String, Integer> colPosMap;
		if (columnPositions == null) {
			colPosMap = new HashMap<String, Integer>();
		} else {
			colPosMap = columnPositions;
		}

		ObjectCache objectCache = results.getObjectCache();
		if (objectCache != null) {
			Object id = idField.resultToJava(results, colPosMap);
			T cachedInstance = objectCache.get(clazz, id);
			if (cachedInstance != null) {
				// if we have a cached instance for this id then return it
				return cachedInstance;
			}
		}

		// create our instance
		T instance = tableInfo.createObject();
		// populate its fields
		Object id = null;
		boolean foreignCollections = false;
		for (FieldType fieldType : resultsFieldTypes) {
			if (fieldType.isForeignCollection()) {
				foreignCollections = true;
			} else {
				Object val = fieldType.resultToJava(results, colPosMap);
				if (parent != null && fieldType.getField().getType() == parent.getClass()) {
					fieldType.assignField(instance, parent, true, objectCache);
				} else {
					fieldType.assignField(instance, val, false, objectCache);
				}
				if (fieldType == idField) {
					id = val;
				}
			}
		}
		if (foreignCollections) {
			// go back and initialize any foreign collections
			for (FieldType fieldType : resultsFieldTypes) {
				if (fieldType.isForeignCollection()) {
					BaseForeignCollection<?, ?> collection = fieldType.buildForeignCollection(instance, id, false);
					if (collection != null) {
						fieldType.assignField(instance, collection, false, objectCache);
					}
				}
			}
		}
		if (columnPositions == null) {
			columnPositions = colPosMap;
		}
		return instance;
	}

	public FieldType[] getResultsFieldTypes() {
		return resultsFieldTypes;
	}

	/**
	 * If we have a foreign collection object then this sets the value on the foreign object in the class.
	 */
	public void setParentObject(Object parent) {
		this.parent = parent;
	}
}
