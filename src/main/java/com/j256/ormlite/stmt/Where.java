package com.j256.ormlite.stmt;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.stmt.QueryBuilder.InternalQueryBuilderWrapper;
import com.j256.ormlite.stmt.query.And;
import com.j256.ormlite.stmt.query.Between;
import com.j256.ormlite.stmt.query.Clause;
import com.j256.ormlite.stmt.query.Eq;
import com.j256.ormlite.stmt.query.Exists;
import com.j256.ormlite.stmt.query.Ge;
import com.j256.ormlite.stmt.query.Gt;
import com.j256.ormlite.stmt.query.In;
import com.j256.ormlite.stmt.query.InSubQuery;
import com.j256.ormlite.stmt.query.IsNotNull;
import com.j256.ormlite.stmt.query.IsNull;
import com.j256.ormlite.stmt.query.Le;
import com.j256.ormlite.stmt.query.Like;
import com.j256.ormlite.stmt.query.Lt;
import com.j256.ormlite.stmt.query.Ne;
import com.j256.ormlite.stmt.query.NeedsFutureClause;
import com.j256.ormlite.stmt.query.Not;
import com.j256.ormlite.stmt.query.Or;
import com.j256.ormlite.stmt.query.Raw;
import com.j256.ormlite.table.TableInfo;

/**
 * Manages the various clauses that make up the WHERE part of a SQL statement. You get one of these when you call
 * {@link StatementBuilder#where} or you can set the where clause by calling {@link StatementBuilder#setWhere}.
 * 
 * <p>
 * Here's a page with a <a href="http://www.w3schools.com/Sql/" >good tutorial of SQL commands</a>.
 * </p>
 * 
 * <p>
 * To create a query which looks up an account by name and password you would do the following:
 * </p>
 * 
 * <blockquote>
 * 
 * <pre>
 * QueryBuilder&lt;Account, String&gt; qb = accountDao.queryBuilder();
 * Where where = qb.where();
 * // the name field must be equal to &quot;foo&quot;
 * where.eq(Account.NAME_FIELD_NAME, &quot;foo&quot;);
 * // and
 * where.and();
 * // the password field must be equal to &quot;_secret&quot;
 * where.eq(Account.PASSWORD_FIELD_NAME, &quot;_secret&quot;);
 * PreparedQuery&lt;Account, String&gt; preparedQuery = qb.prepareQuery();
 * </pre>
 * 
 * </blockquote>
 * 
 * <p>
 * In this example, the SQL query that will be generated will be approximately:
 * </p>
 * 
 * <blockquote>
 * 
 * <pre>
 * SELECT * FROM account WHERE (name = 'foo' AND passwd = '_secret')
 * </pre>
 * 
 * </blockquote>
 * 
 * <p>
 * If you'd rather chain the methods onto one line (like StringBuilder), this can also be written as:
 * </p>
 * 
 * <blockquote>
 * 
 * <pre>
 * queryBuilder.where().eq(Account.NAME_FIELD_NAME, &quot;foo&quot;).and().eq(Account.PASSWORD_FIELD_NAME, &quot;_secret&quot;);
 * </pre>
 * 
 * </blockquote>
 * 
 * <p>
 * If you'd rather use parens and the like then you can call:
 * </p>
 * 
 * <blockquote>
 * 
 * <pre>
 * Where where = queryBuilder.where();
 * where.and(where.eq(Account.NAME_FIELD_NAME, &quot;foo&quot;), where.eq(Account.PASSWORD_FIELD_NAME, &quot;_secret&quot;));
 * </pre>
 * 
 * </blockquote>
 * 
 * <p>
 * All three of the above call formats produce the same SQL. For complex queries that mix ANDs and ORs, the last format
 * will be necessary to get the grouping correct. For example, here's a complex query:
 * </p>
 * 
 * <blockquote>
 * 
 * <pre>
 * Where where = queryBuilder.where();
 * where.or(where.and(where.eq(Account.NAME_FIELD_NAME, &quot;foo&quot;), where.eq(Account.PASSWORD_FIELD_NAME, &quot;_secret&quot;)),
 * 		where.and(where.eq(Account.NAME_FIELD_NAME, &quot;bar&quot;), where.eq(Account.PASSWORD_FIELD_NAME, &quot;qwerty&quot;)));
 * </pre>
 * 
 * </blockquote>
 * 
 * <p>
 * This produces the following approximate SQL:
 * </p>
 * 
 * <blockquote>
 * 
 * <pre>
 * SELECT * FROM account WHERE ((name = 'foo' AND passwd = '_secret') OR (name = 'bar' AND passwd = 'qwerty'))
 * </pre>
 * 
 * </blockquote>
 * 
 * @author graywatson
 */
public class Where<T, ID> {

	private final TableInfo<T> tableInfo;
	private final StatementBuilder<T, ID> statementBuilder;
	private final FieldType idFieldType;
	private final String idColumnName;

	private SimpleStack<Clause> clauseList = new SimpleStack<Clause>();
	private NeedsFutureClause needsFuture = null;

	Where(TableInfo<T> tableInfo, StatementBuilder<T, ID> statementBuilder) {
		// limit the constructor scope
		this.tableInfo = tableInfo;
		this.statementBuilder = statementBuilder;
		this.idFieldType = tableInfo.getIdField();
		if (idFieldType == null) {
			this.idColumnName = null;
		} else {
			this.idColumnName = idFieldType.getDbColumnName();
		}
	}

	/**
	 * AND operation which takes the previous clause and the next clause and AND's them together.
	 */
	public Where<T, ID> and() {
		addNeedsFuture(new And(removeLastClause("AND")));
		return this;
	}

	/**
	 * AND operation which takes 2 arguments and AND's them together.
	 */
	public Where<T, ID> and(Where<T, ID> left, Where<T, ID> right) {
		Clause rightClause = removeLastClause("AND");
		Clause leftClause = removeLastClause("AND");
		addClause(new And(leftClause, rightClause));
		return this;
	}

	/**
	 * Add a BETWEEN clause so the column must be between the low and high parameters.
	 */
	public Where<T, ID> between(String columnName, Object low, Object high) throws SQLException {
		addClause(new Between(columnName, findColumnFieldType(columnName), low, high));
		return this;
	}

	/**
	 * Add a '=' clause so the column must be equal to the value.
	 */
	public Where<T, ID> eq(String columnName, Object value) throws SQLException {
		addClause(new Eq(columnName, findColumnFieldType(columnName), value));
		return this;
	}

	/**
	 * Add a '&gt;=' clause so the column must be greater-than or equals-to the value.
	 */
	public Where<T, ID> ge(String columnName, Object value) throws SQLException {
		addClause(new Ge(columnName, findColumnFieldType(columnName), value));
		return this;
	}

	/**
	 * Add a '&gt;' clause so the column must be greater-than the value.
	 */
	public Where<T, ID> gt(String columnName, Object value) throws SQLException {
		addClause(new Gt(columnName, findColumnFieldType(columnName), value));
		return this;
	}

	/**
	 * Add a IN clause so the column must be equal-to one of the objects from the list passed in.
	 */
	public Where<T, ID> in(String columnName, Iterable<?> objects) throws SQLException {
		addClause(new In(columnName, findColumnFieldType(columnName), objects));
		return this;
	}

	/**
	 * Add a IN clause so the column must be equal-to one of the objects passed in.
	 */
	public Where<T, ID> in(String columnName, Object... objects) throws SQLException {
		if (objects.length == 1 && objects[0].getClass().isArray()) {
			throw new IllegalArgumentException("in(Object... objects) seems to be an array within an array");
		}
		addClause(new In(columnName, findColumnFieldType(columnName), objects));
		return this;
	}

	/**
	 * Add a IN clause which makes sure the column is in one of the columns returned from a sub-query inside of
	 * parenthesis. The QueryBuilder must return 1 and only one column which can be set with the
	 * {@link QueryBuilder#selectColumns(String...)} method calls. That 1 argument must match the SQL type of the
	 * column-name passed to this method.
	 * 
	 * <p>
	 * <b>NOTE:</b> The sub-query will be prepared at the same time that the outside query is.
	 * </p>
	 */
	public Where<T, ID> in(String columnName, QueryBuilder<?, ?> subQueryBuilder) throws SQLException {
		if (subQueryBuilder.getSelectColumnCount() != 1) {
			throw new SQLException("Inner query must have only 1 select column specified instead of "
					+ subQueryBuilder.getSelectColumnCount());
		}
		// we do this to turn off the automatic addition of the ID column in the select column list
		subQueryBuilder.enableInnerQuery();
		addClause(new InSubQuery(columnName, findColumnFieldType(columnName), new InternalQueryBuilderWrapper(
				subQueryBuilder)));
		return this;
	}

	/**
	 * Add a EXISTS clause with a sub-query inside of parenthesis.
	 * 
	 * <p>
	 * <b>NOTE:</b> The sub-query will be prepared at the same time that the outside query is.
	 * </p>
	 */
	public Where<T, ID> exists(QueryBuilder<?, ?> subQueryBuilder) throws SQLException {
		// we do this to turn off the automatic addition of the ID column in the select column list
		subQueryBuilder.enableInnerQuery();
		addClause(new Exists(new InternalQueryBuilderWrapper(subQueryBuilder)));
		return this;
	}

	/**
	 * Add a 'IS NULL' clause so the column must be null. '=' NULL does not work.
	 */
	public Where<T, ID> isNull(String columnName) throws SQLException {
		addClause(new IsNull(columnName, findColumnFieldType(columnName)));
		return this;
	}

	/**
	 * Add a 'IS NOT NULL' clause so the column must not be null. '<>' NULL does not work.
	 */
	public Where<T, ID> isNotNull(String columnName) throws SQLException {
		addClause(new IsNotNull(columnName, findColumnFieldType(columnName)));
		return this;
	}

	/**
	 * Add a '&lt;=' clause so the column must be less-than or equals-to the value.
	 */
	public Where<T, ID> le(String columnName, Object value) throws SQLException {
		addClause(new Le(columnName, findColumnFieldType(columnName), value));
		return this;
	}

	/**
	 * Add a '&lt;' clause so the column must be less-than the value.
	 */
	public Where<T, ID> lt(String columnName, Object value) throws SQLException {
		addClause(new Lt(columnName, findColumnFieldType(columnName), value));
		return this;
	}

	/**
	 * Add a LIKE clause so the column must mach the value using '%' patterns.
	 */
	public Where<T, ID> like(String columnName, Object value) throws SQLException {
		addClause(new Like(columnName, findColumnFieldType(columnName), value));
		return this;
	}

	/**
	 * Add a '&lt;&gt;' clause so the column must be not-equal-to the value.
	 */
	public Where<T, ID> ne(String columnName, Object value) throws SQLException {
		addClause(new Ne(columnName, findColumnFieldType(columnName), value));
		return this;
	}

	/**
	 * Used to NOT the next clause specified.
	 */
	public Where<T, ID> not() {
		addNeedsFuture(new Not());
		return this;
	}

	/**
	 * Used to NOT the argument clause specified.
	 */
	public Where<T, ID> not(Where<T, ID> comparison) {
		addClause(new Not(removeLastClause("NOT")));
		return this;
	}

	/**
	 * OR operation which takes the previous clause and the next clause and OR's them together.
	 */
	public Where<T, ID> or() {
		addNeedsFuture(new Or(removeLastClause("OR")));
		return this;
	}

	/**
	 * OR operation which takes 2 arguments and OR's them together.
	 */
	public Where<T, ID> or(Where<T, ID> left, Where<T, ID> right) {
		Clause rightClause = removeLastClause("OR");
		Clause leftClause = removeLastClause("OR");
		addClause(new Or(leftClause, rightClause));
		return this;
	}

	/**
	 * Add a clause where the ID is equal to the argument.
	 */
	public Where<T, ID> idEq(ID id) throws SQLException {
		if (idColumnName == null) {
			throw new SQLException("Object has no id column specified");
		}
		addClause(new Eq(idColumnName, idFieldType, id));
		return this;
	}

	/**
	 * Add a clause where the ID is from an existing object.
	 */
	public <OD> Where<T, ID> idEq(Dao<OD, ?> dataDao, OD data) throws SQLException {
		if (idColumnName == null) {
			throw new SQLException("Object has no id column specified");
		}
		addClause(new Eq(idColumnName, idFieldType, dataDao.extractId(data)));
		return this;
	}

	/**
	 * Add an '=' clause where the ID is the id from a foreign field in the data that is passed in.
	 */
	public <OD, OID> Where<T, ID> foreignIdEq(Dao<OD, OID> dataDao, OD data) throws SQLException {
		if (idColumnName == null) {
			throw new SQLException("Object has no id column specified");
		}
		FieldType fieldType = getForeignFieldType(dataDao);
		// extract the value of that field which should be the id already for the foreign field
		Object id = fieldType.extractJavaFieldValue(data);
		addClause(new Eq(idColumnName, idFieldType, id));
		return this;
	}

	/**
	 * Add a IN clause where the ID is the id from a foreign field in the data items that are passed in.
	 */
	public <OD, OID> Where<T, ID> foreignIdIn(Dao<OD, OID> dataDao, OD... datas) throws SQLException {
		if (idColumnName == null) {
			throw new SQLException("Object has no id column specified");
		}
		FieldType fieldType = getForeignFieldType(dataDao);
		ArrayList<Object> idList = new ArrayList<Object>();
		for (OD data : datas) {
			// extract the value of that field which should be the id already for the foreign field
			Object id = fieldType.extractJavaFieldValue(data);
			idList.add(id);
		}
		addClause(new In(idColumnName, idFieldType, idList));
		return this;
	}

	/**
	 * Add a IN clause where the ID is the id from a foreign field in the data items iterable that is passed in.
	 */
	public <OD, OID> Where<T, ID> foreignIdIn(Dao<OD, OID> dataDao, Iterable<OD> datas) throws SQLException {
		if (idColumnName == null) {
			throw new SQLException("Object has no id column specified");
		}
		FieldType fieldType = getForeignFieldType(dataDao);
		ArrayList<Object> idList = new ArrayList<Object>();
		for (OD data : datas) {
			// extract the value of that field which should be the id already for the foreign field
			Object id = fieldType.extractJavaFieldValue(data);
			idList.add(id);
		}
		addClause(new In(idColumnName, idFieldType, idList));
		return this;
	}

	/**
	 * Add a raw statement as part of the where that can be anything that the database supports. Using more structured
	 * methods is recommended but this gives more control over the query and allows you to utilize database specific
	 * features.
	 */
	public Where<T, ID> raw(String rawStatement) {
		addClause(new Raw(rawStatement));
		return this;
	}

	/**
	 * A short-cut for calling prepare() on the original {@link QueryBuilder#prepare()}.
	 */
	public PreparedQuery<T> prepare() throws SQLException {
		return statementBuilder.prepareStatement();
	}

	/**
	 * Clear out the Where object so it can be re-used.
	 */
	public void clear() {
		clauseList.clear();
	}

	/**
	 * Used by the internal classes to add the where SQL to the {@link StringBuilder}.
	 */
	void appendSql(DatabaseType databaseType, StringBuilder sb, List<SelectArg> columnArgList) throws SQLException {
		if (clauseList.isEmpty()) {
			throw new IllegalStateException("No where clauses defined.  Did you miss a where operation?");
		}
		if (clauseList.size() != 1) {
			throw new IllegalStateException(
					"Both the \"left-hand\" and \"right-hand\" clauses have been defined.  Did you miss an AND or OR?");
		}

		// we don't pop here because we may want to run the query multiple times
		clauseList.peek().appendSql(databaseType, sb, columnArgList);
	}

	/**
	 * find the field in that class that matches our type.
	 */
	private <OD, OID> FieldType getForeignFieldType(Dao<OD, OID> dataDao) throws SQLException {
		Class<T> clazz = tableInfo.getDataClass();
		FieldType fieldType = dataDao.findForeignFieldType(clazz);
		if (fieldType == null) {
			throw new SQLException("Could not find a field of type " + clazz + " in class " + dataDao.getDataClass());
		} else {
			return fieldType;
		}
	}

	private void addNeedsFuture(NeedsFutureClause needsFuture) {
		if (this.needsFuture == null) {
			this.needsFuture = needsFuture;
			addClause(needsFuture);
		} else {
			throw new IllegalStateException(this.needsFuture + " is already waiting for a future clause, can't add: "
					+ needsFuture);
		}
	}

	private void addClause(Clause clause) {
		if (needsFuture == null || clause == needsFuture) {
			clauseList.push(clause);
		} else {
			// we have a binary statement which was called before the right clause was defined
			needsFuture.setMissingClause(clause);
			needsFuture = null;
		}
	}

	private Clause removeLastClause(String label) {
		if (clauseList.isEmpty()) {
			throw new IllegalStateException("Expecting there to be a clause already defined for '" + label
					+ "' operation");
		} else {
			return clauseList.pop();
		}
	}

	@Override
	public String toString() {
		if (clauseList.isEmpty()) {
			return "empty where clause";
		} else {
			Clause clause = clauseList.peek();
			return "where clause: " + clause;
		}
	}

	private FieldType findColumnFieldType(String columnName) throws SQLException {
		return tableInfo.getFieldTypeByColumnName(columnName);
	}

	/**
	 * Little inner class to provide stack features. The java.util.Stack extends Vector which is synchronized.
	 */
	private class SimpleStack<E> extends ArrayList<E> {
		private static final long serialVersionUID = -8116427380277806666L;

		public void push(E obj) {
			add(obj);
		}

		public E pop() {
			return remove(size() - 1);
		}

		public E peek() {
			return get(size() - 1);
		}
	}
}
