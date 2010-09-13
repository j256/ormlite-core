package com.j256.ormlite.stmt;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.stmt.query.And;
import com.j256.ormlite.stmt.query.Between;
import com.j256.ormlite.stmt.query.Clause;
import com.j256.ormlite.stmt.query.Eq;
import com.j256.ormlite.stmt.query.Ge;
import com.j256.ormlite.stmt.query.Gt;
import com.j256.ormlite.stmt.query.In;
import com.j256.ormlite.stmt.query.IsNotNull;
import com.j256.ormlite.stmt.query.IsNull;
import com.j256.ormlite.stmt.query.Le;
import com.j256.ormlite.stmt.query.Like;
import com.j256.ormlite.stmt.query.Lt;
import com.j256.ormlite.stmt.query.Ne;
import com.j256.ormlite.stmt.query.NeedsFutureClause;
import com.j256.ormlite.stmt.query.Not;
import com.j256.ormlite.stmt.query.Or;
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
public class Where {

	private SimpleStack<Clause> clauseList = new SimpleStack<Clause>();
	private NeedsFutureClause needsFuture = null;
	private final TableInfo<?> tableInfo;

	Where(TableInfo<?> tableInfo) {
		// limit the constructor scope
		this.tableInfo = tableInfo;
	}

	/**
	 * AND operation which takes the previous clause and the next clause and AND's them together.
	 */
	public Where and() {
		addNeedsFuture(new And(removeLastClause("AND")));
		return this;
	}

	/**
	 * AND operation which takes 2 arguments and AND's them together.
	 */
	public Where and(Where left, Where right) {
		Clause rightClause = removeLastClause("AND");
		Clause leftClause = removeLastClause("AND");
		addClause(new And(leftClause, rightClause));
		return this;
	}

	/**
	 * Add a BETWEEN clause so the column must be between the low and high parameters.
	 */
	public Where between(String columnName, Object low, Object high) throws SQLException {
		addClause(new Between(columnName, checkIfColumnIsNumber(columnName), low, high));
		return this;
	}

	/**
	 * Add a '=' clause so the column must be equal to the value.
	 */
	public Where eq(String columnName, Object value) throws SQLException {
		addClause(new Eq(columnName, checkIfColumnIsNumber(columnName), value));
		return this;
	}

	/**
	 * Add a '&gt;=' clause so the column must be greater-than or equals-to the value.
	 */
	public Where ge(String columnName, Object value) throws SQLException {
		addClause(new Ge(columnName, checkIfColumnIsNumber(columnName), value));
		return this;
	}

	/**
	 * Add a '&gt;' clause so the column must be greater-than the value.
	 */
	public Where gt(String columnName, Object value) throws SQLException {
		addClause(new Gt(columnName, checkIfColumnIsNumber(columnName), value));
		return this;
	}

	/**
	 * Add a IN clause so the column must be equal-to one of the objects from the list passed in.
	 */
	public Where in(String columnName, Iterable<?> objects) throws SQLException {
		addClause(new In(columnName, checkIfColumnIsNumber(columnName), objects));
		return this;
	}

	/**
	 * Add a IN clause so the column must be equal-to one of the objects passed in.
	 */
	public Where in(String columnName, Object... objects) throws SQLException {
		if (objects.length == 1 && objects[0].getClass().isArray()) {
			throw new IllegalArgumentException("in(Object... objects) seems to be an array within an array");
		}
		addClause(new In(columnName, checkIfColumnIsNumber(columnName), objects));
		return this;
	}

	/**
	 * Add a 'IS NULL' clause so the column must be null. '=' NULL does not work.
	 */
	public Where isNull(String columnName) throws SQLException {
		addClause(new IsNull(columnName, checkIfColumnIsNumber(columnName)));
		return this;
	}

	/**
	 * Add a 'IS NOT NULL' clause so the column must not be null. '<>' NULL does not work.
	 */
	public Where isNotNull(String columnName) throws SQLException {
		addClause(new IsNotNull(columnName, checkIfColumnIsNumber(columnName)));
		return this;
	}

	/**
	 * Add a '&lt;=' clause so the column must be less-than or equals-to the value.
	 */
	public Where le(String columnName, Object value) throws SQLException {
		addClause(new Le(columnName, checkIfColumnIsNumber(columnName), value));
		return this;
	}

	/**
	 * Add a '&lt;' clause so the column must be less-than the value.
	 */
	public Where lt(String columnName, Object value) throws SQLException {
		addClause(new Lt(columnName, checkIfColumnIsNumber(columnName), value));
		return this;
	}

	/**
	 * Add a LIKE clause so the column must be like the value (where you can specify '%' patterns.
	 */
	public Where like(String columnName, Object value) throws SQLException {
		addClause(new Like(columnName, checkIfColumnIsNumber(columnName), value));
		return this;
	}

	/**
	 * Add a '&lt;&gt;' clause so the column must be not-equal-to the value.
	 */
	public Where ne(String columnName, Object value) throws SQLException {
		addClause(new Ne(columnName, checkIfColumnIsNumber(columnName), value));
		return this;
	}

	/**
	 * Used to NOT the next clause specified.
	 */
	public Where not() {
		addNeedsFuture(new Not());
		return this;
	}

	/**
	 * Used to NOT the argument clause specified.
	 */
	public Where not(Where comparison) {
		addClause(new Not(removeLastClause("NOT")));
		return this;
	}

	/**
	 * OR operation which takes the previous clause and the next clause and OR's them together.
	 */
	public Where or() {
		addNeedsFuture(new Or(removeLastClause("OR")));
		return this;
	}

	/**
	 * OR operation which takes 2 arguments and OR's them together.
	 */
	public Where or(Where left, Where right) {
		Clause rightClause = removeLastClause("OR");
		Clause leftClause = removeLastClause("OR");
		addClause(new Or(leftClause, rightClause));
		return this;
	}

	/**
	 * Used by the internal classes to add the where SQL to the {@link StringBuilder}.
	 */
	void appendSql(DatabaseType databaseType, StringBuilder sb, List<SelectArg> columnArgList) {
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

	private boolean checkIfColumnIsNumber(String columnName) throws SQLException {
		FieldType fieldType = tableInfo.getFieldTypeByName(columnName);
		if (fieldType == null) {
			throw new IllegalArgumentException("Unknown column name '" + columnName + "' in table "
					+ tableInfo.getTableName());
		} else {
			return fieldType.isNumber();
		}
	}

	/**
	 * Little inner class to provide stack features. The java.util.Stack extends Vector which is synchronized.
	 */
	private class SimpleStack<T> extends ArrayList<T> {
		private static final long serialVersionUID = -8116427380277806666L;

		public void push(T obj) {
			add(obj);
		}

		public T pop() {
			return remove(size() - 1);
		}

		public T peek() {
			return get(size() - 1);
		}
	}
}
