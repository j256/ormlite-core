package com.j256.ormlite.stmt.mapped;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.GeneratedKeyHolder;
import com.j256.ormlite.table.TableInfo;

/**
 * A mapped statement for creating a new instance of an object.
 * 
 * @author graywatson
 */
public class MappedCreate<T> extends BaseMappedStatement<T> {

	private final String queryNextSequenceStmt;
	private String dataClassName;

	private MappedCreate(TableInfo<T> tableInfo, String statement, List<FieldType> argFieldTypeList,
			String queryNextSequenceStmt) {
		super(tableInfo, statement, argFieldTypeList);
		this.dataClassName = tableInfo.getDataClass().getSimpleName();
		this.queryNextSequenceStmt = queryNextSequenceStmt;
	}

	/**
	 * Create an object in the database.
	 */
	@Override
	public int insert(DatabaseConnection databaseConnection, T data) throws SQLException {
		if (idField != null) {
			if (idField.isGeneratedIdSequence()) {
				assignSequenceId(databaseConnection, data);
				// fall down to do the update below
			} else if (idField.isGeneratedId()) {
				// this has to do the update first then get the generated-id from callback
				return createWithGeneratedId(databaseConnection, data);
			} else {
				// the id should have been set by the caller already
			}
		}
		return super.insert(databaseConnection, data);
	}

	public static <T> MappedCreate<T> build(DatabaseType databaseType, TableInfo<T> tableInfo) {
		StringBuilder sb = new StringBuilder();
		StringBuilder questionSb = new StringBuilder();
		List<FieldType> argFieldTypeList = new ArrayList<FieldType>();
		appendTableName(databaseType, sb, "INSERT INTO ", tableInfo.getTableName());
		sb.append('(');
		boolean first = true;
		for (FieldType fieldType : tableInfo.getFieldTypes()) {
			if (databaseType.isIdSequenceNeeded()) {
				// we need to query for the next value from the sequence and the idField is inserted afterwards
			} else if (fieldType.isGeneratedId()) {
				// skip generated-id fields because they will be auto-inserted
				continue;
			}
			if (first) {
				first = false;
			} else {
				sb.append(",");
				questionSb.append(",");
			}
			appendFieldColumnName(databaseType, sb, fieldType, argFieldTypeList);
			questionSb.append("?");
		}
		sb.append(") VALUES (").append(questionSb).append(")");
		FieldType idField = tableInfo.getIdField();
		String queryNext = buildQueryNextSequence(databaseType, idField);
		return new MappedCreate<T>(tableInfo, sb.toString(), argFieldTypeList, queryNext);
	}

	private static String buildQueryNextSequence(DatabaseType databaseType, FieldType idField) {
		if (idField == null) {
			return null;
		}
		String seqName = idField.getGeneratedIdSequence();
		if (seqName == null) {
			return null;
		} else {
			StringBuilder sb = new StringBuilder();
			databaseType.appendSelectNextValFromSequence(sb, seqName);
			return sb.toString();
		}
	}

	private void assignSequenceId(DatabaseConnection databaseConnection, T data) throws SQLException {
		// call the query-next-sequence stmt to increment the sequence
		long seqVal = databaseConnection.queryForLong(queryNextSequenceStmt);
		logger.debug("queried for sequence {} using stmt: {}", seqVal, queryNextSequenceStmt);
		if (seqVal == 0) {
			// sanity check that it is working
			throw new SQLException("Should not have returned 0 for stmt: " + queryNextSequenceStmt);
		}
		assignIdValue(data, seqVal, "sequence");
	}

	private int createWithGeneratedId(DatabaseConnection databaseConnection, T data) throws SQLException {
		Object[] args = getFieldObjects(argFieldTypes, data);
		try {
			KeyHolder keyHolder = new KeyHolder();
			// do the insert first
			int retVal = databaseConnection.insert(statement, args, argSqlTypes, keyHolder);
			logger.debug("create object using '{}' and {} args, changed {} rows", statement, args.length, retVal);
			if (args.length > 0) {
				// need to do the (Object) cast to force args to be a single object
				logger.trace("create arguments: {}", (Object) args);
			}
			// if we created 1 row, assign the id field
			if (retVal == 1) {
				// assign the key returned by JDBC to the object's id field after it was inserted
				Number key = keyHolder.getKey();
				if (key == null) {
					// may never happen but let's be careful out there
					throw new SQLException("generated-id key was not set by the update call");
				}
				if (key.longValue() == 0L) {
					// sanity check because the generated-key returned is 0 by default, may never happen
					throw new SQLException("generated-id key must not be 0 value");
				}
				assignIdValue(data, key, "keyholder");
			}
			return retVal;
		} catch (SQLException e) {
			throw SqlExceptionUtil.create("Unable to run stmt on object " + data + ": " + statement, e);
		}
	}

	private void assignIdValue(T data, Number val, String label) throws SQLException {
		// better to do this in one please with consistent logging
		idField.assignIdValue(data, val);
		logger.debug("assigned id '{}' from {} to '{}' in {} object", val, label, idField.getFieldName(), dataClassName);
	}

	private class KeyHolder implements GeneratedKeyHolder {
		Number key;

		public Number getKey() {
			return key;
		}

		public void addKey(Number key) throws SQLException {
			if (this.key == null) {
				this.key = key;
			} else {
				throw new SQLException("generated key has already been set to " + this.key + ", now set to " + key);
			}
		}
	}
}
