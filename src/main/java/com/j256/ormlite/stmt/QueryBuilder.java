package com.j256.ormlite.stmt;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.stmt.mapped.MappedQueryForId;
import com.j256.ormlite.stmt.query.OrderBy;
import com.j256.ormlite.table.TableInfo;

/**
 * Assists in building sql query (SELECT) statements for a particular table in a particular database.
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
public class QueryBuilder<T, ID> extends StatementBuilder<T, ID> {

	private boolean distinct = false;
	private boolean selectIdColumn = true;
	private final FieldType idField;
	private List<String> selectColumnList = null;
	private List<OrderBy> orderByList = null;
	private List<String> groupByList = null;

	public QueryBuilder(DatabaseType databaseType, TableInfo<T> tableInfo) {
		super(databaseType, tableInfo, StatementType.SELECT);
		this.idField = tableInfo.getIdField();
	}

	/**
	 * Build and return a prepared query that can be used by {@link Dao#query(PreparedQuery)} or
	 * {@link Dao#iterator(PreparedQuery)} methods. If you change the where or make other calls you will need to re-call
	 * this method to re-prepare the statement for execution.
	 */
	public PreparedQuery<T> prepare() throws SQLException {
		return super.prepareStatement();
	}

	/**
	 * Add columns to be returned by the SELECT query. If no column...() method called then all columns are returned by
	 * default.
	 */
	public QueryBuilder<T, ID> selectColumns(String... columns) {
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
	public QueryBuilder<T, ID> selectColumns(Iterable<String> columns) {
		if (selectColumnList == null) {
			selectColumnList = new ArrayList<String>();
		}
		for (String column : columns) {
			addSelectColumnToList(column);
		}
		return this;
	}

	/**
	 * Add "GROUP BY" clause to the SQL query statement.
	 * 
	 * <p>
	 * NOTE: Use of this means that the resulting objects may not have a valid ID column value so cannot be deleted or
	 * updated.
	 * </p>
	 */
	public QueryBuilder<T, ID> groupBy(String columnName) {
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
	public QueryBuilder<T, ID> orderBy(String columnName, boolean ascending) {
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
	 * <p>
	 * NOTE: Use of this means that the resulting objects may not have a valid ID column value so cannot be deleted or
	 * updated.
	 * </p>
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

	@Override
	protected void appendStatementStart(StringBuilder sb, List<FieldType> resultFieldTypeList) throws SQLException {
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
	}

	@Override
	protected void appendStatementEnd(StringBuilder sb) {
		// 'group by' comes before 'order by'
		appendGroupBys(sb);
		appendOrderBys(sb);
		if (!databaseType.isLimitAfterSelect()) {
			appendLimit(sb);
		}
	}

	private void addSelectColumnToList(String column) {
		verifyColumnName(column);
		selectColumnList.add(column);
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
			FieldType fieldType = tableInfo.getFieldTypeByColumnName(columnName);
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

	private void appendLimit(StringBuilder sb) {
		if (limit != null && databaseType.isLimitSqlSupported()) {
			databaseType.appendLimitValue(sb, limit);
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

	/**
	 * Internal class used to expose the {@link QueryBuilder#buildStatementString(List, List, List)} method to internal
	 * classes. Users should use the {@link #prepareStatementString()} method instead.
	 * 
	 * This is needed for mapped mapped statements such as {@link MappedQueryForId} but I didn't want the dao user to
	 * access it directly.
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
		 * {@link QueryBuilder#prepare()} method instead.
		 */
		@Override
		public String buildStatementString(List<FieldType> argFieldTypeList, List<FieldType> resultFieldTypeList,
				List<SelectArg> selectArgList) throws SQLException {
			return super.buildStatementString(argFieldTypeList, resultFieldTypeList, selectArgList);
		}
	}
}
