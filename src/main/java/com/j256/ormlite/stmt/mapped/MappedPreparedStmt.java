package com.j256.ormlite.stmt.mapped;

import java.sql.SQLException;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.logger.Log.Level;
import com.j256.ormlite.stmt.ArgumentHolder;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.PreparedUpdate;
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

	private final ArgumentHolder[] argHolders;
	private final Integer limit;
	private final StatementType type;

	public MappedPreparedStmt(TableInfo<T, ID> tableInfo, String statement, FieldType[] argFieldTypes,
			FieldType[] resultFieldTypes, ArgumentHolder[] argHolders, Integer limit, StatementType type) {
		super(tableInfo, statement, argFieldTypes, resultFieldTypes);
		this.argHolders = argHolders;
		// this is an Integer because it may be null
		this.limit = limit;
		this.type = type;
	}

	public CompiledStatement compile(DatabaseConnection databaseConnection) throws SQLException {
		CompiledStatement stmt = databaseConnection.compileStatement(statement, type, argFieldTypes, resultsFieldTypes);
		if (limit != null) {
			// we use this if SQL statement LIMITs are not supported by this database type
			stmt.setMaxRows(limit);
		}
		// set any arguments if we are logging our object
		Object[] argValues = null;
		if (logger.isLevelEnabled(Level.TRACE) && argHolders.length > 0) {
			argValues = new Object[argHolders.length];
		}
		for (int i = 0; i < argHolders.length; i++) {
			Object argValue = argHolders[i].getSqlArgValue();
			if (argValue == null) {
				stmt.setNull(i, argFieldTypes[i].getSqlType());
			} else {
				stmt.setObject(i, argValue, argFieldTypes[i].getSqlType());
			}
			if (argValues != null) {
				argValues[i] = argValue;
			}
		}
		logger.debug("prepared statement '{}' with {} args", statement, argHolders.length);
		if (argValues != null) {
			// need to do the (Object) cast to force args to be a single object
			logger.trace("prepared statement arguments: {}", (Object) argValues);
		}
		return stmt;
	}
}
