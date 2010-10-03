package com.j256.ormlite.stmt;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.stmt.mapped.MappedPreparedStmt;
import com.j256.ormlite.table.TableInfo;

/**
 * Assists in building of SQL statements for a particular table in a particular database.
 * 
 * @param T
 *            The class that the code will be operating on.
 * @param ID
 *            The class of the ID column associated with the class. The T class does not require an ID field. The class
 *            needs an ID parameter however so you can use Void or Object to satisfy the compiler.
 * @author graywatson
 */
public abstract class StatementBuilder<T, ID> {

	private static Logger logger = LoggerFactory.getLogger(StatementBuilder.class);

	protected final TableInfo<T> tableInfo;
	protected final DatabaseType databaseType;
	private final StatementType type;

	private Where where = null;
	protected Integer limit = null;

	/**
	 * Provides statements for various SQL operations.
	 * 
	 * @param databaseType
	 *            Database type.
	 * @param tableInfo
	 *            Information about the table/class that is being handled.
	 * @param type
	 *            Type of statement we are building.
	 */
	public StatementBuilder(DatabaseType databaseType, TableInfo<T> tableInfo, StatementType type) {
		this.databaseType = databaseType;
		this.tableInfo = tableInfo;
		this.type = type;
		if (type != StatementType.SELECT && type != StatementType.UPDATE && type != StatementType.DELETE) {
			throw new IllegalStateException("Building a statement from a " + type + " statement is not allowed");
		}
	}

	/**
	 * Returns a {@link Where} object that should be used to add SQL where clauses to the statement. This will also
	 * reset the where object so you can use the same query builder with a different where statement.
	 */
	public Where where() {
		where = new Where(tableInfo);
		return where;
	}

	/**
	 * Set the {@link Where} object on the query. This allows someone to use the same Where object on multiple queries.
	 */
	public void setWhere(Where where) {
		this.where = where;
	}

	/**
	 * @deprecated Use {@link QueryBuilder#prepare()}
	 */
	@Deprecated
	public MappedPreparedStmt<T> prepareStatement() throws SQLException {
		List<FieldType> argFieldTypeList = new ArrayList<FieldType>();
		List<FieldType> resultFieldTypeList = new ArrayList<FieldType>();
		List<SelectArg> selectArgList = new ArrayList<SelectArg>();
		String statement = buildStatementString(argFieldTypeList, resultFieldTypeList, selectArgList);
		return new MappedPreparedStmt<T>(tableInfo, statement, argFieldTypeList, resultFieldTypeList, selectArgList,
				(databaseType.isLimitSqlSupported() ? null : limit), type);
	}

	/**
	 * Build and return a string version of the query. If you change the where or make other calls you will need to
	 * re-call this method to re-prepare the query for execution.
	 * 
	 * <p>
	 * This is mostly used for debugging or logging cases.
	 * </p>
	 */
	public String prepareStatementString() throws SQLException {
		List<FieldType> argFieldTypeList = new ArrayList<FieldType>();
		List<FieldType> resultFieldTypeList = new ArrayList<FieldType>();
		List<SelectArg> selectArgList = new ArrayList<SelectArg>();
		return buildStatementString(argFieldTypeList, resultFieldTypeList, selectArgList);
	}

	/**
	 * Internal method to build a query while tracking various arguments. Users should use the
	 * {@link #prepareStatementString()} method instead.
	 * 
	 * <p>
	 * This needs to be protected because of (WARNING: DO NOT MAKE A JAVADOC LINK) InternalQueryBuilder (WARNING: DO NOT
	 * MAKE A JAVADOC LINK).
	 * </p>
	 */
	protected String buildStatementString(List<FieldType> argFieldTypeList, List<FieldType> resultFieldTypeList,
			List<SelectArg> selectArgList) throws SQLException {
		StringBuilder sb = new StringBuilder();
		appendStatementStart(sb, resultFieldTypeList);
		if (where != null) {
			sb.append("WHERE ");
			where.appendSql(databaseType, sb, selectArgList);
		}
		for (SelectArg selectArg : selectArgList) {
			FieldType fieldType = tableInfo.getFieldTypeByName(selectArg.getColumnName());
			argFieldTypeList.add(fieldType);
		}
		appendStatementEnd(sb);
		String statement = sb.toString();
		logger.debug("built statement {}", statement);
		return statement;
	}

	/**
	 * Append the start of our statement string to the StringBuilder.
	 */
	protected abstract void appendStatementStart(StringBuilder sb, List<FieldType> resultFieldTypeList)
			throws SQLException;

	/**
	 * Append the end of our statement string to the StringBuilder.
	 */
	protected abstract void appendStatementEnd(StringBuilder sb);

	/**
	 * Verify the columnName is valid and return its FieldType.
	 * 
	 * @throws IllegalArgumentException
	 *             if the column name is not valid.
	 */
	protected FieldType verifyColumnName(String columnName) {
		FieldType fieldType = tableInfo.getFieldTypeByName(columnName);
		if (fieldType == null) {
			throw new IllegalArgumentException("Unknown column-name '" + columnName + "'");
		} else {
			return fieldType;
		}
	}

	/**
	 * Types of statements that we are building.
	 */
	public static enum StatementType {
		/** SQL statement in the form of INSERT ... FROM table ... */
		INSERT,
		/** SQL statement in the form of SELECT ... FROM table ... */
		SELECT,
		/** SQL statement in the form of UPDATE table SET ... */
		UPDATE,
		/** SQL statement in the form of DELETE FROM table ... */
		DELETE,
		/** SQL statement in the form of CREATE TABLE or something */
		EXECUTE,
		// end
		;
	}
}
