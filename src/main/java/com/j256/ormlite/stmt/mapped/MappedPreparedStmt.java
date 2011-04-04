package com.j256.ormlite.stmt.mapped;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.logger.Log.Level;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.PreparedUpdate;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.StatementBuilder;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.TableInfo;

/**
 * Mapped statement used by the {@link StatementBuilder#prepareStatement()} method.
 * 
 * @author graywatson
 */
public class MappedPreparedStmt<T, ID> extends BaseMappedQuery<T, ID> implements PreparedQuery<T>, PreparedDelete<T>,
		PreparedUpdate<T> {

	private final SelectArg[] selectArgs;
	private final Integer limit;
	private final StatementType type;

	public MappedPreparedStmt(TableInfo<T, ID> tableInfo, String statement, FieldType[] argFieldTypes,
			List<FieldType> resultFieldTypeList, List<SelectArg> selectArgList, Integer limit, StatementType type) {
		super(tableInfo, statement, argFieldTypes, resultFieldTypeList);
		this.selectArgs = selectArgList.toArray(new SelectArg[selectArgList.size()]);
		// select args should match the field-type list
		if (argFieldTypes == null || selectArgs.length != argFieldTypes.length) {
			throw new IllegalArgumentException("Should be the same number of SelectArg and field-types in the arrays");
		}
		// this is an Integer because it may be null
		this.limit = limit;
		this.type = type;
	}

	public CompiledStatement compile(DatabaseConnection databaseConnection) throws SQLException {
		CompiledStatement stmt = databaseConnection.compileStatement(statement, type, argFieldTypes, resultsFieldTypes);
		if (limit != null) {
			stmt.setMaxRows(limit);
		}
		// set any arguments if there are any selectArgs
		Object[] args = null;
		if (logger.isLevelEnabled(Level.TRACE) && selectArgs.length > 0) {
			args = new Object[selectArgs.length];
		}
		for (int i = 0; i < selectArgs.length; i++) {
			Object arg = selectArgs[i].getSqlArgValue();
			// sql statement arguments start at 1
			if (arg == null) {
				stmt.setNull(i, argFieldTypes[i].getSqlType());
			} else {
				stmt.setObject(i, arg, argFieldTypes[i].getSqlType());
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

	public SelectArg[] getSelectArgs() {
		return selectArgs;
	}
}
