package com.j256.ormlite.stmt;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.ForeignCollectionField;
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

	private final FieldType idField;
	private FieldType[] resultFieldTypes;

	private boolean distinct = false;
	private boolean selectIdColumn = true;
	private List<String> selectColumnList = null;
	private List<String> selectRawList = null;
	private List<OrderBy> orderByList = null;
	private String orderByRaw = null;
	private List<String> groupByList = null;
	private String groupByRaw = null;
	private boolean isInnerQuery = false;
	private boolean countOf = false;
	private String having = null;
	private Long limit = null;
	private Long offset = null;
	// NOTE: anything added here should be added to the clear() method below

	public QueryBuilder(DatabaseType databaseType, TableInfo<T, ID> tableInfo, Dao<T, ID> dao) {
		super(databaseType, tableInfo, dao, StatementType.SELECT);
		this.idField = tableInfo.getIdField();
	}

	/**
	 * This is used by the internal call structure to note when a query builder is being used as an inner query. This is
	 * necessary because by default, we add in the ID column on every query. When you are returning a data item, its ID
	 * field _must_ be set otherwise you can't do a refresh() or update(). But internal queries must have 1 select
	 * column set so we can't add the ID.
	 */
	void enableInnerQuery() {
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
		return super.prepareStatement(limit);
	}

	/**
	 * Add columns to be returned by the SELECT query. If no columns are selected then all columns are returned by
	 * default. For classes with id columns, the id column is added to the select list automagically.
	 * 
	 * <p>
	 * <b>WARNING:</b> If you specify any columns to return, then any foreign-collection fields will be returned as null
	 * <i>unless</i> their {@link ForeignCollectionField#columnName()} is also in the list.
	 * </p>
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
	 * Same as {@link #selectColumns(String...)} except the columns are specified as an iterable -- probably will be a
	 * {@link Collection}.
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
	 * Add raw columns or aggregate functions (COUNT, MAX, ...) to the query. This will turn the query into something
	 * only suitable for the {@link Dao#queryRaw(String, String...)} type of statement.
	 */
	public QueryBuilder<T, ID> selectRaw(String... columns) {
		if (selectRawList == null) {
			selectRawList = new ArrayList<String>();
		}
		for (String column : columns) {
			selectRawList.add(column);
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
	 * Add a raw SQL "GROUP BY" clause to the SQL query statement. This should not include the "GROUP BY".
	 */
	public QueryBuilder<T, ID> groupByRaw(String rawSql) {
		groupByRaw = rawSql;
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
	 * Add raw SQL "ORDER BY" clause to the SQL query statement. This should not include the "ORDER BY".
	 */
	public QueryBuilder<T, ID> orderByRaw(String rawSql) {
		orderByRaw = rawSql;
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
	 * @deprecated Should use {@link #limit(Long)}
	 */
	@Deprecated
	public QueryBuilder<T, ID> limit(int maxRows) {
		return limit((long) maxRows);
	}

	/**
	 * Limit the output to maxRows maximum number of rows. Set to null for no limit (the default).
	 */
	public QueryBuilder<T, ID> limit(Long maxRows) {
		limit = maxRows;
		return this;
	}

	/**
	 * @deprecated Should use {@link #offset(Long)}
	 */
	@Deprecated
	public QueryBuilder<T, ID> offset(int startRow) throws SQLException {
		return offset((long) startRow);
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
	public QueryBuilder<T, ID> offset(Long startRow) throws SQLException {
		if (databaseType.isOffsetSqlSupported()) {
			offset = startRow;
			return this;
		} else {
			throw new SQLException("Offset is not supported by this database");
		}
	}

	/**
	 * Set whether or not we should only return the count of the results.
	 */
	public QueryBuilder<T, ID> setCountOf(boolean countOf) {
		this.countOf = countOf;
		return this;
	}

	/**
	 * Add raw SQL "HAVING" clause to the SQL query statement. This should not include the "HAVING" string.
	 */
	public QueryBuilder<T, ID> having(String having) {
		this.having = having;
		return this;
	}

	/**
	 * A short cut to {@link Dao#query(PreparedQuery)}.
	 */
	public List<T> query() throws SQLException {
		return dao.query(prepare());
	}

	/**
	 * A short cut to {@link Dao#queryForFirst(PreparedQuery)}.
	 */
	public T queryForFirst() throws SQLException {
		return dao.queryForFirst(prepare());
	}

	/**
	 * A short cut to {@link Dao#queryRaw(String, String...)} and {@link GenericRawResults#getFirstResult()}.
	 */
	public String[] queryRawFirst() throws SQLException {
		return dao.queryRaw(prepareStatementString()).getFirstResult();
	}

	/**
	 * A short cut to {@link Dao#iterator(PreparedQuery)}.
	 */
	public CloseableIterator<T> iterator() throws SQLException {
		return dao.iterator(prepare());
	}

	@Override
	public void clear() {
		super.clear();
		distinct = false;
		selectIdColumn = true;
		selectColumnList = null;
		selectRawList = null;
		orderByList = null;
		orderByRaw = null;
		groupByList = null;
		groupByRaw = null;
		isInnerQuery = false;
		countOf = false;
		having = null;
		limit = null;
		offset = null;
	}

	@Override
	protected void appendStatementStart(StringBuilder sb, List<ArgumentHolder> argList) {
		sb.append("SELECT ");
		if (databaseType.isLimitAfterSelect()) {
			appendLimit(sb);
		}
		if (distinct) {
			sb.append("DISTINCT ");
		}
		if (countOf) {
			type = StatementType.SELECT_LONG;
			sb.append("COUNT(*) ");
		} else if (selectRawList != null && !selectRawList.isEmpty()) {
			type = StatementType.SELECT_RAW;
			appendRawColumns(sb);
		} else {
			type = StatementType.SELECT;
			appendColumns(sb);
		}
		sb.append("FROM ");
		databaseType.appendEscapedEntityName(sb, tableInfo.getTableName());
		sb.append(' ');
	}

	@Override
	protected FieldType[] getResultFieldTypes() {
		return resultFieldTypes;
	}

	@Override
	protected void appendStatementEnd(StringBuilder sb) throws SQLException {
		// 'group by' comes before 'order by'
		appendGroupBys(sb);
		appendOrderBys(sb);
		appendHaving(sb);
		if (!databaseType.isLimitAfterSelect()) {
			appendLimit(sb);
		}
		appendOffset(sb);
	}

	private void addSelectColumnToList(String columnName) {
		verifyColumnName(columnName);
		selectColumnList.add(columnName);
	}

	private void appendRawColumns(StringBuilder sb) {
		boolean first = true;
		for (String column : selectRawList) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			sb.append(column);
		}
		sb.append(' ');
	}

	private void appendColumns(StringBuilder sb) {
		// if no columns were specified then * is the default
		if (selectColumnList == null) {
			sb.append("* ");
			resultFieldTypes = tableInfo.getFieldTypes();
			return;
		}

		boolean first = true;
		boolean hasId;
		if (isInnerQuery) {
			hasId = true;
		} else {
			hasId = false;
		}
		List<FieldType> fieldTypeList = new ArrayList<FieldType>(selectColumnList.size() + 1);
		for (String columnName : selectColumnList) {
			FieldType fieldType = tableInfo.getFieldTypeByColumnName(columnName);
			/*
			 * If this is a foreign-collection then we add it to our field-list but _not_ to the select list because
			 * foreign collections don't have a column in the database.
			 */
			if (fieldType.isForeignCollection()) {
				fieldTypeList.add(fieldType);
				continue;
			}
			if (first) {
				first = false;
			} else {
				sb.append(',');
			}
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

		resultFieldTypes = fieldTypeList.toArray(new FieldType[fieldTypeList.size()]);
	}

	private void appendFieldColumnName(StringBuilder sb, FieldType fieldType, List<FieldType> fieldTypeList) {
		databaseType.appendEscapedEntityName(sb, fieldType.getColumnName());
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
		if ((groupByList == null || groupByList.isEmpty()) && groupByRaw == null) {
			return;
		}

		sb.append("GROUP BY ");
		if (groupByRaw != null) {
			sb.append(groupByRaw);
		} else {
			boolean first = true;
			for (String columnName : groupByList) {
				if (first) {
					first = false;
				} else {
					sb.append(',');
				}
				databaseType.appendEscapedEntityName(sb, columnName);
			}
		}
		sb.append(' ');
	}

	private void appendOrderBys(StringBuilder sb) {
		if ((orderByList == null || orderByList.isEmpty()) && orderByRaw == null) {
			return;
		}

		sb.append("ORDER BY ");
		if (orderByRaw != null) {
			sb.append(orderByRaw);
		} else {
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
		}
		sb.append(' ');
	}

	private void appendHaving(StringBuilder sb) {
		if (having != null) {
			sb.append("HAVING ").append(having).append(' ');
		}
	}

	/**
	 * Internal class used to expose methods to internal classes but through a wrapper instead of a builder.
	 */
	public static class InternalQueryBuilderWrapper {

		private final QueryBuilder<?, ?> queryBuilder;

		InternalQueryBuilderWrapper(QueryBuilder<?, ?> queryBuilder) {
			this.queryBuilder = queryBuilder;
		}

		public void appendStatementString(StringBuilder sb, List<ArgumentHolder> argList) throws SQLException {
			queryBuilder.appendStatementString(sb, argList);
		}

		public FieldType[] getResultFieldTypes() {
			return queryBuilder.resultFieldTypes;
		}
	}
}
