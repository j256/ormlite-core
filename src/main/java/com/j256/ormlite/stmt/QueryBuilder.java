package com.j256.ormlite.stmt;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.j256.ormlite.dao.CloseableIterator;
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
	private boolean isInnerQuery = false;
	private final Dao<T, ID> dao;

	public QueryBuilder(DatabaseType databaseType, TableInfo<T, ID> tableInfo, Dao<T, ID> dao) {
		super(databaseType, tableInfo, StatementType.SELECT);
		this.idField = tableInfo.getIdField();
		this.dao = dao;
	}

	/**
	 * This is used by the internal call structure to note when a query builder is being used as an inner query. This is
	 * necessary because by default, we add in the ID column on every query. When you are returning a data item, its ID
	 * field _must_ be set otherwise you can't do a refresh() or update(). But internal queries must have 1 select
	 * column set so we can't add the ID.
	 */
	void enableInnerQuery() throws SQLException {
		this.isInnerQuery = true;
	}

	/**
	 * Return the number of selected columns in the query.
	 */
	int getSelectColumnCount() {
		if (selectColumnList == null) {
			return 0;
		} else {
			return selectColumnList.size();
		}
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
	 * Add columns to be returned by the SELECT query. If no columns are selected then all columns are returned by
	 * default. For classes with id columns, the id column is added to the select list automagically.
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
	 * Add columns to be returned by the SELECT query. If no columns are selected then all columns are returned by
	 * default. For classes with id columns, the id column is added to the select list automagically.
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
		FieldType fieldType = verifyColumnName(columnName);
		if (fieldType.isForeignCollection()) {
			throw new IllegalArgumentException("Can't groupBy foreign colletion field: " + columnName);
		}
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
		FieldType fieldType = verifyColumnName(columnName);
		if (fieldType.isForeignCollection()) {
			throw new IllegalArgumentException("Can't orderBy foreign colletion field: " + columnName);
		}
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
	 * Limit the output to maxRows maximum number of rows. Set to null for no limit (the default).
	 */
	public QueryBuilder<T, ID> limit(Integer maxRows) {
		limit = maxRows;
		return this;
	}

	/**
	 * Start the output at this row number. Set to null for no offset (the default). If you are paging through a table,
	 * you should consider using the {@link Dao#iterator()} method instead which handles paging with a database cursor.
	 * Otherwise, if you are paging you probably want to specify a {@link #orderBy(String, boolean)}.
	 * 
	 * <p>
	 * <b>NOTE:</b> This is not supported for all databases. Also, for some databases, the limit _must_ also be
	 * specified since the offset is an argument of the limit.
	 * </p>
	 */
	public QueryBuilder<T, ID> offset(Integer startRow) throws SQLException {
		if (databaseType.isOffsetSqlSupported()) {
			offset = startRow;
			return this;
		} else {
			throw new SQLException("Offset is not supported by this database");
		}
	}

	/**
	 * A short cut for Dao.query(prepare()). {@link Dao#query(PreparedQuery)}.
	 */
	public List<T> query() throws SQLException {
		return dao.query(prepare());
	}

	/**
	 * A short cut for Dao.iterator(prepare()). {@link Dao#iterator(PreparedQuery)}.
	 */
	public CloseableIterator<T> iterator() throws SQLException {
		return dao.iterator(prepare());
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
	protected void appendStatementEnd(StringBuilder sb) throws SQLException {
		// 'group by' comes before 'order by'
		appendGroupBys(sb);
		appendOrderBys(sb);
		if (!databaseType.isLimitAfterSelect()) {
			appendLimit(sb);
		}
		appendOffset(sb);
	}

	private void addSelectColumnToList(String columnName) {
		FieldType fieldType = verifyColumnName(columnName);
		if (fieldType.isForeignCollection()) {
			throw new IllegalArgumentException("Can't select from foreign colletion field: " + columnName);
		}
		selectColumnList.add(columnName);
	}

	private void appendColumns(StringBuilder sb, List<FieldType> fieldTypeList) throws SQLException {
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
		boolean hasId;
		if (isInnerQuery) {
			hasId = true;
		} else {
			hasId = false;
		}
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
			databaseType.appendLimitValue(sb, limit, offset);
		}
	}

	private void appendOffset(StringBuilder sb) throws SQLException {
		if (offset == null) {
			return;
		}
		if (databaseType.isOffsetLimitArgument()) {
			if (limit == null) {
				throw new SQLException("If the offset is specified, limit must also be specified with this database");
			}
		} else {
			databaseType.appendOffsetValue(sb, offset);
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

	private void appendOrderBys(StringBuilder sb) throws SQLException {
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

		public InternalQueryBuilder(DatabaseType databaseType, TableInfo<T, ID> tableInfo, Dao<T, ID> dao) {
			super(databaseType, tableInfo, dao);
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

	/**
	 * Internal class used to expose the {@link QueryBuilder#buildStatementString(List, List)} method to internal
	 * classes but through a wrapper instead of a builder.
	 */
	public static class InternalQueryBuilderWrapper {

		private final QueryBuilder<?, ?> queryBuilder;

		public InternalQueryBuilderWrapper(QueryBuilder<?, ?> queryBuilder) {
			this.queryBuilder = queryBuilder;
		}

		public void buildStatementString(StringBuilder sb, List<FieldType> resultFieldTypeList,
				List<SelectArg> selectArgList) throws SQLException {
			queryBuilder.appendStatementString(sb, resultFieldTypeList, selectArgList);
		}
	}
}
