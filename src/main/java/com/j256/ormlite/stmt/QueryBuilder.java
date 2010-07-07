package com.j256.ormlite.stmt;

import java.util.ArrayList;
import java.util.List;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.logger.LoggerFactory;
import com.j256.ormlite.stmt.mapped.MappedPreparedQuery;
import com.j256.ormlite.stmt.mapped.MappedQueryForId;
import com.j256.ormlite.stmt.query.OrderBy;
import com.j256.ormlite.table.TableInfo;

/**
 * Assists in building SQL query (select) statements for a particular table in a particular database. Uses the
 * {@link DatabaseType} to get per-database SQL statements. By default all columns are returned with a blank where
 * clause doing the equivalent of 'select * from table'.
 * 
 * <p>
 * For a good tutorial of SQL commands, see the following URL: http://www.w3schools.com/Sql/
 * </p>
 * 
 * @param T
 *            The class that the code will be operating on.
 * @param ID
 *            The class of the ID column associated with the class. The T class does not require an ID field. The class
 *            needs an ID parameter however so you can use Void or Object to satisfy the compiler.
 * @author graywatson
 */
public class QueryBuilder<T, ID> {

	private static Logger logger = LoggerFactory.getLogger(QueryBuilder.class);

	private TableInfo<T> tableInfo;
	private DatabaseType databaseType;
	private final FieldType idField;

	private boolean distinct = false;
	private boolean selectIdColumn = true;
	private List<String> columnList = null;
	private final List<OrderBy> orderByList = new ArrayList<OrderBy>();
	private final List<String> groupByList = new ArrayList<String>();
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
	public QueryBuilder(DatabaseType databaseType, TableInfo<T> tableInfo) {
		this.databaseType = databaseType;
		this.tableInfo = tableInfo;
		this.idField = tableInfo.getIdField();
	}

	/**
	 * Add columns to be returned by the query. If no column...() method called then all columns are returned by
	 * default.
	 */
	public QueryBuilder<T, ID> columns(String... columns) {
		if (columnList == null) {
			columnList = new ArrayList<String>();
		}
		for (String column : columns) {
			addColumnToList(column);
		}
		return this;
	}

	/**
	 * Add columns to be returned by the query. If no column...() method called then all columns are returned by
	 * default.
	 */
	public QueryBuilder<T, ID> columns(Iterable<String> columns) {
		if (columnList == null) {
			columnList = new ArrayList<String>();
		}
		for (String column : columns) {
			addColumnToList(column);
		}
		return this;
	}

	/**
	 * Add "GROUP BY" clauses to the SQL query statement.
	 * 
	 * NOTE: Use of this means that the resulting objects may not have a valid ID column value so cannot be deleted or
	 * updated.
	 */
	public QueryBuilder<T, ID> groupBy(String columnName) {
		verifyColumnName(columnName);
		groupByList.add(columnName);
		selectIdColumn = false;
		return this;
	}

	/**
	 * Add "ORDER BY" clauses to the SQL query statement.
	 */
	public QueryBuilder<T, ID> orderBy(String columnName, boolean ascending) {
		verifyColumnName(columnName);
		orderByList.add(new OrderBy(columnName, ascending));
		return this;
	}

	/**
	 * Add "DISTINCT" clause to the SQL query statement.
	 * 
	 * NOTE: Use of this means that the resulting objects may not have a valid ID column value so cannot be deleted or
	 * updated.
	 */
	public QueryBuilder<T, ID> distinct() {
		distinct = true;
		selectIdColumn = false;
		return this;
	}

	/**
	 * Limit the output to maxRows maximum number of rows. Set to null for no limit (the default). This is implemented
	 * at the database level either through a LIMIT SQL query addition or a JDBC setMaxRows method call.
	 */
	public QueryBuilder<T, ID> limit(Integer maxRows) {
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
	 * Build and return a {@link PreparedQuery} object which then can be used by {@link Dao#query(PreparedQuery)} and
	 * {@link Dao#iterator(PreparedQuery)} methods. If you change the where or make other calls you will need to re-call
	 * this method to re-prepare the query for execution.
	 */
	public PreparedQuery<T> prepareQuery() {
		List<FieldType> argFieldTypeList = new ArrayList<FieldType>();
		List<FieldType> resultFieldTypeList = new ArrayList<FieldType>();
		List<SelectArg> selectArgList = new ArrayList<SelectArg>();
		String statement = buildSelectString(argFieldTypeList, resultFieldTypeList, selectArgList);
		return new MappedPreparedQuery<T>(tableInfo, statement, argFieldTypeList, resultFieldTypeList, selectArgList,
				(databaseType.isLimitSqlSupported() ? null : limit));
	}

	/**
	 * Build and return a string version of the query. If you change the where or make other calls you will need to
	 * re-call this method to re-prepare the query for execution.
	 * 
	 * <p>
	 * This is mostly used for debugging or logging cases. The dao classes us the {@link #prepareQuery} method.
	 * </p>
	 */
	public String prepareQueryString() {
		List<FieldType> argFieldTypeList = new ArrayList<FieldType>();
		List<FieldType> resultFieldTypeList = new ArrayList<FieldType>();
		List<SelectArg> selectArgList = new ArrayList<SelectArg>();
		return buildSelectString(argFieldTypeList, resultFieldTypeList, selectArgList);
	}

	/**
	 * Internal method to build a query while tracking various arguments. Users should use the {@link #prepareQuery()}
	 * method instead.
	 */
	private String buildSelectString(List<FieldType> argFieldTypeList, List<FieldType> resultFieldTypeList,
			List<SelectArg> selectArgList) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		if (databaseType.isLimitAfterSelect()) {
			appendLimit(sb);
		}
		if (distinct) {
			sb.append("DISTINCT ");
		}
		appendColumns(sb, resultFieldTypeList);
		sb.append("FROM ");
		databaseType.appendEscapedEntityName(sb, tableInfo.getTableName());
		sb.append(' ');
		if (where != null) {
			sb.append("WHERE ");
			where.appendSql(databaseType, sb, selectArgList);
		}
		for (SelectArg selectArg : selectArgList) {
			FieldType fieldType = tableInfo.nameToFieldType(selectArg.getColumnName());
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

	private void addColumnToList(String column) {
		verifyColumnName(column);
		columnList.add(column);
	}

	private void verifyColumnName(String columnName) {
		if (tableInfo.nameToFieldType(columnName) == null) {
			throw new IllegalArgumentException("Unknown column-name " + columnName);
		}
	}

	private void appendColumns(StringBuilder sb, List<FieldType> fieldTypeList) {
		// if no columns were specified then * is the default
		if (columnList == null) {
			sb.append("* ");
			// add all of the field types
			for (FieldType fieldType : tableInfo.getFieldTypes()) {
				fieldTypeList.add(fieldType);
			}
			return;
		}

		boolean first = true;
		boolean hasId = false;
		for (String columnName : columnList) {
			if (first) {
				first = false;
			} else {
				sb.append(',');
			}
			FieldType fieldType = tableInfo.nameToFieldType(columnName);
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
		if (groupByList.size() == 0) {
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
		if (orderByList.size() == 0) {
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

	/**
	 * Inner class used to hide from the user the {@link QueryBuilder#buildSelectString} method. The buildQuery method
	 * is needed for mapped mapped statements such as {@link MappedQueryForId} but I didn't want the dao user to access
	 * it directly.
	 * 
	 * That I had to do this probably means that I have a bad type hierarchy or package layout but I don't see a better
	 * way to do it right now.
	 */
	public static class InternalQueryBuilder<T, ID> extends QueryBuilder<T, ID> {

		public InternalQueryBuilder(DatabaseType databaseType, TableInfo<T> tableInfo) {
			super(databaseType, tableInfo);
		}

		/**
		 * Internal method to build a query while tracking various arguments. Users should use the
		 * {@link QueryBuilder#prepareQuery()} method instead.
		 */
		public String buildSelectString(List<FieldType> argFieldTypeList, List<FieldType> resultFieldTypeList,
				List<SelectArg> selectArgList) {
			return super.buildSelectString(argFieldTypeList, resultFieldTypeList, selectArgList);
		}
	}
}
