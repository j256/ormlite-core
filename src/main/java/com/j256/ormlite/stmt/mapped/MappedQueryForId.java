package com.j256.ormlite.stmt.mapped;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.QueryBuilder.InternalQueryBuilder;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.TableInfo;

/**
 * Mapped statement for querying for an object by its ID.
 * 
 * @author graywatson
 */
public class MappedQueryForId<T, ID> extends BaseMappedQuery<T> {

	private final String label;

	protected MappedQueryForId(TableInfo<T> tableInfo, String statement, List<FieldType> argFieldTypeList,
			List<FieldType> resultFieldTypeList, String label) {
		super(tableInfo, statement, argFieldTypeList, resultFieldTypeList);
		this.label = label;
	}

	/**
	 * Query for an object in the database which matches the obj argument.
	 */
	public T execute(DatabaseConnection databaseConnection, ID id) throws SQLException {
		Object[] args = new Object[] { id };
		// @SuppressWarnings("unchecked")
		Object result =
				databaseConnection.queryForOne(statement, args, new SqlType[] { idField.getSqlTypeVal() }, this);
		if (result == DatabaseConnection.MORE_THAN_ONE) {
			logger.error("{} using '{}' and {} args, got >1 results", label, statement, args.length);
			logArgs(args);
			throw new SQLException(label + " got more than 1 result: " + statement);
		}
		logger.debug("{} using '{}' and {} args, got 1 result", label, statement, args.length);
		logArgs(args);
		@SuppressWarnings("unchecked")
		T castResult = (T) result;
		return castResult;
	}

	public static <T, ID> MappedQueryForId<T, ID> build(DatabaseType databaseType, TableInfo<T> tableInfo)
			throws SQLException {
		List<FieldType> argFieldTypeList = new ArrayList<FieldType>();
		List<FieldType> resultFieldTypeList = new ArrayList<FieldType>();
		String statement = buildStatement(databaseType, tableInfo, argFieldTypeList, resultFieldTypeList);
		return new MappedQueryForId<T, ID>(tableInfo, statement, argFieldTypeList, resultFieldTypeList, "query-for-id");
	}

	protected static <ID, T> String buildStatement(DatabaseType databaseType, TableInfo<T> tableInfo,
			List<FieldType> argFieldTypeList, List<FieldType> resultFieldTypeList) throws SQLException {
		FieldType idField = tableInfo.getIdField();
		if (idField == null) {
			throw new SQLException("Cannot query-for-id with " + tableInfo.getDataClass()
					+ " because it doesn't have an id field");
		}
		InternalQueryBuilder<T, ID> qb = new InternalQueryBuilder<T, ID>(databaseType, tableInfo);
		// this selectArg is ignored here because we pass in the id as a fixed argument
		SelectArg idSelectArg = new SelectArg();
		qb.where().eq(idField.getDbColumnName(), idSelectArg);
		List<SelectArg> selectArgList = new ArrayList<SelectArg>();
		return qb.buildStatementString(argFieldTypeList, resultFieldTypeList, selectArgList);
	}

	private void logArgs(Object[] args) {
		if (args.length > 0) {
			// need to do the (Object) cast to force args to be a single object
			logger.trace("{} arguments: {}", label, (Object) args);
		}
	}
}
