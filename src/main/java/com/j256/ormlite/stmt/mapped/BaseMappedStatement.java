package com.j256.ormlite.stmt.mapped;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableInfo;

/**
 * Abstract mapped statement which has common statements used by the subclasses.
 * 
 * @author graywatson
 */
public abstract class BaseMappedStatement<T, ID> {

	protected static Logger logger = LoggerFactory.getLogger(BaseMappedStatement.class);

	protected final Dao<T, ID> dao;
	protected final ConnectionSource connectionSource;
	protected final TableInfo<T, ID> tableInfo;
	protected final Class<T> clazz;
	protected final FieldType idField;
	protected final String statement;
	protected final FieldType[] argFieldTypes;

	protected BaseMappedStatement(Dao<T, ID> dao, TableInfo<T, ID> tableInfo, String statement,
			FieldType[] argFieldTypes) {
		this.dao = dao;
		this.connectionSource = dao.getConnectionSource();
		this.tableInfo = tableInfo;
		this.clazz = tableInfo.getDataClass();
		this.idField = tableInfo.getIdField();
		this.statement = statement;
		this.argFieldTypes = argFieldTypes;
	}

	/**
	 * Return the array of field objects pulled from the data object.
	 */
	protected Object[] getFieldObjects(Object data) throws SQLException {
		return getFieldObjects(data, false);
	}

	/**
	 * Return the array of field objects pulled from the data object.
	 */
	protected Object[] getFieldObjects(Object data, boolean inserting) throws SQLException {
		Object[] objects = new Object[argFieldTypes.length];
		int insertC = 0;
		for (FieldType fieldType : argFieldTypes) {
			if (fieldType.isAllowGeneratedIdInsert()) {
				Object obj = fieldType.getFieldValueIfNotDefault(data);
				if (inserting && obj == null) {
					/*
					 * If we are inserting into a field marked as allowGeneratedIdInsert then it may have a value or
					 * not. If it doesn't have a value then we should not generate an argument for it so we reduced the
					 * number of field types by one. This is then the cue for the insert runner to use the special
					 * insert generated id null statement.
					 */
					objects = Arrays.copyOf(objects, argFieldTypes.length - 1);
					continue;
				}
				objects[insertC] = obj;
			} else {
				objects[insertC] = fieldType.extractJavaFieldToSqlArgValue(data);
			}
			if (objects[insertC] == null) {
				// NOTE: the default value could be null as well
				objects[insertC] = fieldType.getDefaultValue();
			}
			insertC++;
		}
		return objects;
	}

	/**
	 * Return a field object converted from an id.
	 */
	protected Object convertIdToFieldObject(ID id) throws SQLException {
		return idField.convertJavaFieldToSqlArgValue(id);
	}

	static void appendWhereFieldEq(DatabaseType databaseType, FieldType fieldType, StringBuilder sb,
			List<FieldType> fieldTypeList) {
		sb.append("WHERE ");
		appendFieldColumnName(databaseType, sb, fieldType, fieldTypeList);
		sb.append("= ?");
	}

	static void appendTableName(DatabaseType databaseType, StringBuilder sb, String prefix, String tableName) {
		if (prefix != null) {
			sb.append(prefix);
		}
		databaseType.appendEscapedEntityName(sb, tableName);
		sb.append(' ');
	}

	static void appendTableName(DatabaseType databaseType, StringBuilder sb, String prefix, TableInfo<?, ?> tableInfo) {
		if (prefix != null) {
			sb.append(prefix);
		}
		if (tableInfo.getSchemaName() != null && tableInfo.getSchemaName().length() > 0) {
			databaseType.appendEscapedEntityName(sb, tableInfo.getSchemaName());
			sb.append('.');
		}
		databaseType.appendEscapedEntityName(sb, tableInfo.getTableName());
		sb.append(' ');
	}

	static void appendFieldColumnName(DatabaseType databaseType, StringBuilder sb, FieldType fieldType,
			List<FieldType> fieldTypeList) {
		databaseType.appendEscapedEntityName(sb, fieldType.getColumnName());
		if (fieldTypeList != null) {
			fieldTypeList.add(fieldType);
		}
		sb.append(' ');
	}

	@Override
	public String toString() {
		return statement;
	}
}
