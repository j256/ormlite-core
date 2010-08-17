package com.j256.ormlite.stmt.mapped;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.support.DatabaseAccess;
import com.j256.ormlite.table.TableInfo;

/**
 * Abstract mapped statement which has common statements used by the subclasses. The
 * {@link #update(DatabaseAccess, Object, String)} method is here because it is used by create, delete, and update
 * calls.
 * 
 * @author graywatson
 */
public abstract class BaseMappedStatement<T> {

	protected static Logger logger = LoggerFactory.getLogger(BaseMappedStatement.class);

	protected final TableInfo<T> tableInfo;
	protected final FieldType idField;
	protected final String statement;
	protected final FieldType[] argFieldTypes;
	protected final int[] argFieldTypeVals;

	protected BaseMappedStatement(TableInfo<T> tableInfo, String statement, List<FieldType> argFieldTypeList) {
		this.tableInfo = tableInfo;
		this.idField = tableInfo.getIdField();
		this.statement = statement;
		this.argFieldTypes = argFieldTypeList.toArray(new FieldType[argFieldTypeList.size()]);
		this.argFieldTypeVals = getFieldTypeVals(argFieldTypes);
	}

	/**
	 * Insert the object into the database
	 */
	protected int insert(DatabaseAccess template, T data) throws SQLException {
		try {
			Object[] args = getFieldObjects(argFieldTypes, data);
			int rowC = template.insert(statement, args, argFieldTypeVals);
			logger.debug("insert data with statement '{}' and {} args, changed {} rows", statement, args.length,
					rowC);
			if (args.length > 0) {
				// need to do the (Object) cast to force args to be a single object
				logger.trace("insert arguments: {}", (Object) args);
			}
			return rowC;
		} catch (SQLException e) {
			throw SqlExceptionUtil.create("Unable to run insert stmt on object " + data + ": " + statement, e);
		}
	}

	/**
	 * Update the object in the database.
	 */
	public int update(DatabaseAccess template, T data) throws SQLException {
		try {
			Object[] args = getFieldObjects(argFieldTypes, data);
			int rowC = template.update(statement, args, argFieldTypeVals);
			logger.debug("update data with statement '{}' and {} args, changed {} rows", statement, args.length,
					rowC);
			if (args.length > 0) {
				// need to do the (Object) cast to force args to be a single object
				logger.trace("update arguments: {}", (Object) args);
			}
			return rowC;
		} catch (SQLException e) {
			throw SqlExceptionUtil.create("Unable to run update stmt on object " + data + ": " + statement, e);
		}
	}

	/**
	 * Delete the object from the database.
	 */
	public int delete(DatabaseAccess template, T data) throws SQLException {
		try {
			Object[] args = getFieldObjects(argFieldTypes, data);
			int rowC = template.delete(statement, args, argFieldTypeVals);
			logger.debug("delete data with statement '{}' and {} args, changed {} rows", statement, args.length,
					rowC);
			if (args.length > 0) {
				// need to do the (Object) cast to force args to be a single object
				logger.trace("delete arguments: {}", (Object) args);
			}
			return rowC;
		} catch (SQLException e) {
			throw SqlExceptionUtil.create("Unable to run delete stmt on object " + data + ": " + statement, e);
		}
	}

	/**
	 * Return the array of field objects pulled from the data object.
	 */
	protected Object[] getFieldObjects(FieldType[] fieldTypes, Object data) throws SQLException {
		Object[] objects = new Object[fieldTypes.length];
		for (int i = 0; i < fieldTypes.length; i++) {
			FieldType fieldType = fieldTypes[i];
			objects[i] = fieldType.getConvertedFieldValue(data);
			if (objects[i] == null && fieldType.getDefaultValue() != null) {
				objects[i] = fieldType.getDefaultValue();
			}
		}
		return objects;
	}

	static void appendWhereId(DatabaseType databaseType, FieldType idField, StringBuilder sb,
			List<FieldType> fieldTypeList) {
		sb.append("WHERE ");
		appendFieldColumnName(databaseType, sb, idField, fieldTypeList);
		sb.append("= ?");
	}

	static void appendTableName(DatabaseType databaseType, StringBuilder sb, String prefix, String tableName) {
		if (prefix != null) {
			sb.append(prefix);
		}
		databaseType.appendEscapedEntityName(sb, tableName);
		sb.append(' ');
	}

	static void appendFieldColumnName(DatabaseType databaseType, StringBuilder sb, FieldType fieldType,
			List<FieldType> fieldTypeList) {
		databaseType.appendEscapedEntityName(sb, fieldType.getDbColumnName());
		if (fieldTypeList != null) {
			fieldTypeList.add(fieldType);
		}
		sb.append(' ');
	}

	private int[] getFieldTypeVals(FieldType[] fieldTypes) {
		int[] typeVals = new int[fieldTypes.length];
		for (int i = 0; i < fieldTypes.length; i++) {
			typeVals[i] = fieldTypes[i].getJdbcTypeVal();
		}
		return typeVals;
	}

	@Override
	public String toString() {
		return "MappedStatement: " + statement;
	}
}
