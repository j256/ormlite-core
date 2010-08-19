package com.j256.ormlite.stmt.mapped;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.PreparedStmt;
import com.j256.ormlite.table.TableInfo;

/**
 * Mapped statement used by the {@link QueryBuilder#prepareQuery()} method.
 * 
 * @author graywatson
 */
public class MappedPreparedQuery<T> extends BaseMappedQuery<T> implements PreparedQuery<T> {

	private final SelectArg[] selectArgs;
	private final Integer limit;

	public MappedPreparedQuery(TableInfo<T> tableInfo, String statement, List<FieldType> argFieldTypeList,
			List<FieldType> resultFieldTypeList, List<SelectArg> selectArgList, Integer limit) {
		super(tableInfo, statement, argFieldTypeList, resultFieldTypeList);
		this.selectArgs = selectArgList.toArray(new SelectArg[selectArgList.size()]);
		// select args should match the field-type list
		if (argFieldTypeVals == null || selectArgs.length != argFieldTypeVals.length) {
			throw new IllegalArgumentException("Should be the same number of SelectArg and field-types in the arrays");
		}
		this.limit = limit;
	}

	public PreparedStmt prepareSqlStatement(DatabaseConnection databaseConnection) throws SQLException {
		PreparedStmt stmt = databaseConnection.prepareStatement(statement);
		if (limit != null) {
			stmt.setMaxRows(limit);
		}
		// set any arguments if there are any selectArgs
		Object[] args = null;
		if (logger.isTraceEnabled() && selectArgs.length > 0) {
			args = new Object[selectArgs.length];
		}
		for (int i = 0; i < selectArgs.length; i++) {
			Object arg = selectArgs[i].getValue();
			// sql statement arguments start at 1
			if (arg == null) {
				stmt.setNull(i + 1, argFieldTypeVals[i]);
			} else {
				stmt.setObject(i + 1, arg, argFieldTypeVals[i]);
			}
			if (args != null) {
				args[i] = arg;
			}
		}
		logger.debug("prepared statement '{}' with {} args", statement, selectArgs.length);
		if (args != null) {
			// need to do the (Object) cast to force args to be a single object
			logger.trace("prepared statement arguments: {}", (Object) args);
		}
		return stmt;
	}

	public String getStatement() {
		return statement;
	}
}
