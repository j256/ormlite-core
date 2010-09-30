package com.j256.ormlite.stmt;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.stmt.mapped.MappedPreparedStmt;
import com.j256.ormlite.stmt.query.Clause;
import com.j256.ormlite.stmt.query.OrderBy;
import com.j256.ormlite.stmt.query.SetExpression;
import com.j256.ormlite.stmt.query.SetValue;
import com.j256.ormlite.table.TableInfo;

/**
 * Assists in building SQL query (select) statements for a particular table in a particular database. Uses the
 * {@link DatabaseType} to get per-database SQL statements. By default the resulting queries will return objects with
 * all columns -- doing the equivalent of 'select * from table'. See {@link #columns(Iterable)} or
 * {@link #columns(String...)} to return partial column lists.
 * 
 * <p>
 * Here is a <a href="http://www.w3schools.com/Sql/" >good tutorial of SQL commands</a>.
 * </p>
 * 
 * @param T
 *            The class that the code will be operating on.
 * @param ID
 *            The class of the ID column associated with the class. The T class does not require an ID field. The class
 *            needs an ID parameter however so you can use Void or Object to satisfy the compiler.
 * @author graywatson
 */
public class StatementBuilder<T, ID> {

	private static Logger logger = LoggerFactory.getLogger(StatementBuilder.class);

	private final TableInfo<T> tableInfo;
	private final DatabaseType databaseType;
	private final FieldType idField;
	private final StatementType type;

	private boolean distinct = false;
	private boolean selectIdColumn = true;
	private List<String> selectColumnList = null;
	private List<OrderBy> orderByList = null;
	private List<String> groupByList = null;
	private List<Clause> updateClauseList = null;
	private Where where = null;
	private Integer limit = null;

	/**
	 * Provides statements for various SQL operations.
	 * 
	 * @param databaseType
	 *            Database type.
	 * @param tableInfo
	 *            Information about the table/class that is being handled.
	 */
	public StatementBuilder(DatabaseType databaseType, TableInfo<T> tableInfo) {
		this(databaseType, tableInfo, StatementType.SELECT);
	}

	/**
	 * Provides statements for various SQL operations.
	 * 
	 * @param databaseType
	 *            Database type.
	 * @param tableInfo
	 *            Information about the table/class that is being handled.
	 */
	public StatementBuilder(DatabaseType databaseType, TableInfo<T> tableInfo, StatementType type) {
		this.databaseType = databaseType;
		this.tableInfo = tableInfo;
		this.idField = tableInfo.getIdField();
		this.type = type;
		if (type != StatementType.SELECT && type != StatementType.UPDATE && type != StatementType.DELETE) {
			throw new IllegalStateException("Building a statement from a " + type + " statement is not allowed");
		}
	}

	/**
	 * @deprecated Use {@link #selectColumns(String...)}
	 */
	@Deprecated
	public StatementBuilder<T, ID> columns(String... columns) {
		return selectColumns(columns);
	}

	/**
	 * @deprecated Use {@link #selectColumns(Iterable)}
	 */
	@Deprecated
	public StatementBuilder<T, ID> columns(Iterable<String> columns) {
		return selectColumns(columns);
	}

	/**
	 * Add columns to be returned by the SELECT query. If no column...() method called then all columns are returned by
	 * default.
	 */
	public StatementBuilder<T, ID> selectColumns(String... columns) {
		if (type != StatementType.SELECT) {
			throw new IllegalArgumentException("columns are only valid for SELECT queries, not " + type);
		}
		if (selectColumnList == null) {
			selectColumnList = new ArrayList<String>();
		}
		for (String column : columns) {
			addSelectColumnToList(column);
		}
		return this;
	}

	/**
	 * Add columns to be returned by the SELECT query. If no column...() method called then all columns are returned by
	 * default.
	 */
	public StatementBuilder<T, ID> selectColumns(Iterable<String> columns) {
		if (type != StatementType.SELECT) {
			throw new IllegalArgumentException("columns are only valid for SELECT queries, not " + type);
		}
		if (selectColumnList == null) {
			selectColumnList = new ArrayList<String>();
		}
		for (String column : columns) {
			addSelectColumnToList(column);
		}
		return this;
	}

	/**
	 * Add a column to be set to a value for UPDATE statements. This will generate something like columnName = 'value'
	 * with the value escaped if necessary.
	 */
	public StatementBuilder<T, ID> updateColumnValue(String columnName, Object value) {
		FieldType fieldType = tableInfo.getFieldTypeByName(columnName);
		if (fieldType == null) {
			throw new IllegalArgumentException("Unknown column name: " + columnName);
		}
		addUpdateColumnToList(columnName, new SetValue(columnName, fieldType, value));
		return this;
	}

	/**
	 * Add a column to be set to a value for UPDATE statements. This will generate something like 'columnName =
	 * expression' where the expression is built by the caller.
	 * 
	 * <p>
	 * The expression should have any strings escaped using the {@link #escapeValue(String)} or
	 * {@link #escapeValue(StringBuilder, String)} methods and should have any column names escaped using the
	 * {@link #escapeColumnName(String)} or {@link #escapeColumnName(StringBuilder, String)} methods.
	 * </p>
	 */
	public StatementBuilder<T, ID> updateColumnExpression(String columnName, String expression) {
		addUpdateColumnToList(columnName, new SetExpression(columnName, expression));
		return this;
	}

	/**
	 * When you are building the expression for {@link #updateColumnExpression(String, String)}, you may need to escape
	 * column names since they may be reserved words to the database. This will help you by adding escape characters
	 * around the word.
	 */
	public void escapeColumnName(StringBuilder sb, String columnName) {
		databaseType.appendEscapedEntityName(sb, columnName);
	}

	/**
	 * Same as {@link #escapeColumnName(StringBuilder, String)} but it will return the escaped string. The StringBuilder
	 * method is more efficient since this method creates a {@link StatementBuilder} internally.
	 */
	public String escapeColumnName(String columnName) {
		StringBuilder sb = new StringBuilder();
		databaseType.appendEscapedEntityName(sb, columnName);
		return sb.toString();
	}

	/**
	 * When you are building the expression for {@link #updateColumnExpression(String, String)}, you may need to escape
	 * values since they may be reserved words to the database. Numbers should not be escaped. This will help you by
	 * adding escape characters around the word.
	 */
	public void escapeValue(StringBuilder sb, String value) {
		databaseType.appendEscapedWord(sb, value);
	}

	/**
	 * Same as {@link #escapeValue(StringBuilder, String)} but it will return the escaped string. Numbers should not be
	 * escaped. The StringBuilder method is more efficient since this method creates a {@link StatementBuilder}
	 * internally.
	 */
	public String escapeValue(String value) {
		StringBuilder sb = new StringBuilder();
		databaseType.appendEscapedWord(sb, value);
		return sb.toString();
	}

	/**
	 * Add "GROUP BY" clause to the SQL query statement.
	 * 
	 * NOTE: Use of this means that the resulting objects may not have a valid ID column value so cannot be deleted or
	 * updated.
	 */
	public StatementBuilder<T, ID> groupBy(String columnName) {
		if (type != StatementType.SELECT) {
			throw new IllegalArgumentException("GROUPBY is only valid for SELECT queries, not " + type);
		}
		verifyColumnName(columnName);
		if (groupByList == null) {
			groupByList = new ArrayList<String>();
		}
		groupByList.add(columnName);
		selectIdColumn = false;
		return this;
	}

	/**
	 * Add "ORDER BY" clause to the SQL query statement.
	 */
	public StatementBuilder<T, ID> orderBy(String columnName, boolean ascending) {
		if (type != StatementType.SELECT) {
			throw new IllegalArgumentException("ORDERBY is only valid for SELECT queries, not " + type);
		}
		verifyColumnName(columnName);
		if (orderByList == null) {
			orderByList = new ArrayList<OrderBy>();
		}
		orderByList.add(new OrderBy(columnName, ascending));
		return this;
	}

	/**
	 * Add "DISTINCT" clause to the SQL query statement.
	 * 
	 * NOTE: Use of this means that the resulting objects may not have a valid ID column value so cannot be deleted or
	 * updated.
	 */
	public StatementBuilder<T, ID> distinct() {
		if (type != StatementType.SELECT) {
			throw new IllegalArgumentException("DISTINCT is only valid for SELECT queries, not " + type);
		}
		distinct = true;
		selectIdColumn = false;
		return this;
	}

	/**
	 * Limit the output to maxRows maximum number of rows. Set to null for no limit (the default). This is implemented
	 * at the database level either through a LIMIT SQL query addition or a JDBC setMaxRows method call.
	 */
	public StatementBuilder<T, ID> limit(Integer maxRows) {
		if (type != StatementType.SELECT) {
			throw new IllegalArgumentException("LIMIT is only valid for SELECT queries, not " + type);
		}
		limit = maxRows;
		return this;
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
	 * Build and return a {@link PreparedStmt} object which then can be used by {@link Dao#query(PreparedStmt)} and
	 * {@link Dao#iterator(PreparedStmt)} methods. If you change the where or make other calls you will need to re-call
	 * this method to re-prepare the query for execution.
	 */
	public PreparedStmt<T> prepareStatement() throws SQLException {
		List<FieldType> argFieldTypeList = new ArrayList<FieldType>();
		List<FieldType> resultFieldTypeList = new ArrayList<FieldType>();
		List<SelectArg> selectArgList = new ArrayList<SelectArg>();
		String statement = buildStatementString(argFieldTypeList, resultFieldTypeList, selectArgList);
		return new MappedPreparedStmt<T>(tableInfo, statement, argFieldTypeList, resultFieldTypeList, selectArgList,
				(databaseType.isLimitSqlSupported() ? null : limit), type);
	}

	/**
	 * @deprecated Use {@link #prepareStatement()}
	 */
	@Deprecated
	public PreparedStmt<T> prepareQuery() throws SQLException {
		return prepareStatement();
	}

	/**
	 * Build and return a string version of the query. If you change the where or make other calls you will need to
	 * re-call this method to re-prepare the query for execution.
	 * 
	 * <p>
	 * This is mostly used for debugging or logging cases. The dao classes us the {@link #prepareQuery} method.
	 * </p>
	 */
	public String prepareStatementString() throws SQLException {
		List<FieldType> argFieldTypeList = new ArrayList<FieldType>();
		List<FieldType> resultFieldTypeList = new ArrayList<FieldType>();
		List<SelectArg> selectArgList = new ArrayList<SelectArg>();
		return buildStatementString(argFieldTypeList, resultFieldTypeList, selectArgList);
	}

	/**
	 * Internal method to build a query while tracking various arguments. Users should use the {@link #prepareQuery()}
	 * method instead.
	 */
	private String buildStatementString(List<FieldType> argFieldTypeList, List<FieldType> resultFieldTypeList,
			List<SelectArg> selectArgList) throws SQLException {
		StringBuilder sb = new StringBuilder();
		switch (type) {
			case SELECT :
				sb.append("SELECT ");
				if (databaseType.isLimitAfterSelect()) {
					appendLimit(sb);
				}
				if (distinct) {
					sb.append("DISTINCT ");
				}
				appendColumns(sb, resultFieldTypeList);
				sb.append("FROM ");
				break;
			case DELETE :
				sb.append("DELETE ");
				sb.append("FROM ");
				break;
			case UPDATE :
				if (updateClauseList == null || updateClauseList.size() == 0) {
					throw new IllegalArgumentException("UPDATE statements must have at least one SET column");
				}
				sb.append("UPDATE ");
				break;
			default :
				throw new IllegalStateException("Building a statement from a " + type + " statement is not allowed");
		}
		databaseType.appendEscapedEntityName(sb, tableInfo.getTableName());
		sb.append(' ');
		if (updateClauseList != null) {
			// for UPDATE
			sb.append("SET ");
			boolean first = true;
			for (Clause clause : updateClauseList) {
				if (first) {
					first = false;
				} else {
					sb.append(',');
				}
				clause.appendSql(databaseType, sb, null);
			}
		}
		if (where != null) {
			sb.append("WHERE ");
			where.appendSql(databaseType, sb, selectArgList);
		}
		for (SelectArg selectArg : selectArgList) {
			FieldType fieldType = tableInfo.getFieldTypeByName(selectArg.getColumnName());
			argFieldTypeList.add(fieldType);
		}
		// 'group by' comes before 'order by'
		appendGroupBys(sb);
		appendOrderBys(sb);
		if (!databaseType.isLimitAfterSelect()) {
			appendLimit(sb);
		}
		String statement = sb.toString();
		logger.debug("built statement {}", statement);
		return statement;
	}

	private void addSelectColumnToList(String column) {
		verifyColumnName(column);
		selectColumnList.add(column);
	}

	private void addUpdateColumnToList(String columnName, Clause clause) {
		if (type != StatementType.UPDATE) {
			throw new IllegalArgumentException("columns are only valid for UPDATE queries, not " + type);
		}
		verifyColumnName(columnName);
		if (updateClauseList == null) {
			updateClauseList = new ArrayList<Clause>();
		}
		updateClauseList.add(clause);
	}

	private void verifyColumnName(String columnName) {
		if (tableInfo.getFieldTypeByName(columnName) == null) {
			throw new IllegalArgumentException("Unknown column-name " + columnName);
		}
	}

	private void appendColumns(StringBuilder sb, List<FieldType> fieldTypeList) {
		// if no columns were specified then * is the default
		if (selectColumnList == null) {
			sb.append("* ");
			// add all of the field types
			for (FieldType fieldType : tableInfo.getFieldTypes()) {
				fieldTypeList.add(fieldType);
			}
			return;
		}

		boolean first = true;
		boolean hasId = false;
		for (String columnName : selectColumnList) {
			if (first) {
				first = false;
			} else {
				sb.append(',');
			}
			FieldType fieldType = tableInfo.getFieldTypeByName(columnName);
			appendFieldColumnName(sb, fieldType, fieldTypeList);
			if (fieldType == idField) {
				hasId = true;
			}
		}

		// we have to add the idField even if it isn't in the columnNameSet
		if (!hasId && selectIdColumn) {
			if (!first) {
				sb.append(',');
			}
			appendFieldColumnName(sb, idField, fieldTypeList);
		}
		sb.append(' ');
	}

	private void appendFieldColumnName(StringBuilder sb, FieldType fieldType, List<FieldType> fieldTypeList) {
		databaseType.appendEscapedEntityName(sb, fieldType.getDbColumnName());
		if (fieldTypeList != null) {
			fieldTypeList.add(fieldType);
		}
	}

	private void appendGroupBys(StringBuilder sb) {
		if (groupByList == null || groupByList.size() == 0) {
			return;
		}

		sb.append("GROUP BY ");
		boolean first = true;
		for (String columnName : groupByList) {
			if (first) {
				first = false;
			} else {
				sb.append(',');
			}
			databaseType.appendEscapedEntityName(sb, columnName);
		}
		sb.append(' ');
	}

	private void appendOrderBys(StringBuilder sb) {
		if (orderByList == null || orderByList.size() == 0) {
			return;
		}

		sb.append("ORDER BY ");
		boolean first = true;
		for (OrderBy orderBy : orderByList) {
			if (first) {
				first = false;
			} else {
				sb.append(',');
			}
			String columnName = orderBy.getColumnName();
			verifyColumnName(columnName);
			databaseType.appendEscapedEntityName(sb, columnName);
			if (orderBy.isAscending()) {
				// sb.append(" ASC");
			} else {
				sb.append(" DESC");
			}
		}
		sb.append(' ');
	}

	private void appendLimit(StringBuilder sb) {
		if (limit != null && databaseType.isLimitSqlSupported()) {
			databaseType.appendLimitValue(sb, limit);
		}
	}

	/*
	 * Inner class used to hide from the user the {@link QueryBuilder#buildSelectString} method. The buildQuery method
	 * is needed for mapped mapped statements such as {@link MappedQueryForId} but I didn't want the dao user to access
	 * it directly.
	 * 
	 * That I had to do this probably means that I have a bad type hierarchy or package layout but I don't see a better
	 * way to do it right now.
	 */
	public static class InternalQueryBuilder<T, ID> extends StatementBuilder<T, ID> {

		public InternalQueryBuilder(DatabaseType databaseType, TableInfo<T> tableInfo) {
			super(databaseType, tableInfo);
		}

		/**
		 * Internal method to build a query while tracking various arguments. Users should use the
		 * {@link StatementBuilder#prepareQuery()} method instead.
		 */
		public String buildSelectString(List<FieldType> argFieldTypeList, List<FieldType> resultFieldTypeList,
				List<SelectArg> selectArgList) throws SQLException {
			return super.buildStatementString(argFieldTypeList, resultFieldTypeList, selectArgList);
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
