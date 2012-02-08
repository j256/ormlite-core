package com.j256.ormlite.dao;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.GenericRowMapper;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.PreparedUpdate;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.support.DatabaseResults;

/**
 * The definition of the Database Access Objects that handle the reading and writing a class from the database. Kudos to
 * Robert A. for the general concept of this hierarchy.
 * 
 * @param T
 *            The class that the code will be operating on.
 * @param ID
 *            The class of the ID column associated with the class. The T class does not require an ID field. The class
 *            needs an ID parameter however so you can use Void or Object to satisfy the compiler.
 * @author graywatson
 */
public interface Dao<T, ID> extends CloseableIterable<T> {

	/**
	 * Retrieves an object associated with a specific ID.
	 * 
	 * @param id
	 *            Identifier that matches a specific row in the database to find and return.
	 * @return The object that has the ID field which equals id or null if no matches.
	 * @throws SQLException
	 *             on any SQL problems or if more than 1 item with the id are found in the database.
	 */
	public T queryForId(ID id) throws SQLException;

	/**
	 * Query for and return the first item in the object table which matches the PreparedQuery. See
	 * {@link #queryBuilder()} for more information. This can be used to return the object that matches a single unique
	 * column. You should use {@link #queryForId(Object)} if you want to query for the id column.
	 * 
	 * @param preparedQuery
	 *            Query used to match the objects in the database.
	 * @return The first object that matches the query.
	 * @throws SQLException
	 *             on any SQL problems.
	 */
	public T queryForFirst(PreparedQuery<T> preparedQuery) throws SQLException;

	/**
	 * Query for all of the items in the object table. For medium sized or large tables, this may load a lot of objects
	 * into memory so you should consider using the {@link #iterator()} method instead.
	 * 
	 * @return A list of all of the objects in the table.
	 * @throws SQLException
	 *             on any SQL problems.
	 */
	public List<T> queryForAll() throws SQLException;

	/**
	 * Query for the items in the object table that match a simple where with a single field = value type of WHERE
	 * clause. This is a convenience method for calling queryBuilder().where().eq(fieldName, value).query().
	 * 
	 * @return A list of the objects in the table that match the fieldName = value;
	 * @throws SQLException
	 *             on any SQL problems.
	 */
	public List<T> queryForEq(String fieldName, Object value) throws SQLException;

	/**
	 * Query for the rows in the database that match the object passed in as an argument. Any fields in the matching
	 * object that are not the default value (null, false, 0, 0.0, etc.) are used as the matching parameters with AND.
	 * If you are worried about SQL quote escaping, you should use {@link #queryForMatchingArgs(Object)}.
	 */
	public List<T> queryForMatching(T matchObj) throws SQLException;

	/**
	 * Same as {@link #queryForMatching(Object)} but this uses {@link SelectArg} and SQL ? arguments. This is slightly
	 * more expensive but you don't have to worry about SQL quote escaping.
	 */
	public List<T> queryForMatchingArgs(T matchObj) throws SQLException;

	/**
	 * Query for the rows in the database that matches all of the field to value entries from the map passed in. If you
	 * are worried about SQL quote escaping, you should use {@link #queryForFieldValuesArgs(Map)}.
	 */
	public List<T> queryForFieldValues(Map<String, Object> fieldValues) throws SQLException;

	/**
	 * Same as {@link #queryForFieldValues(Map)} but this uses {@link SelectArg} and SQL ? arguments. This is slightly
	 * more expensive but you don't have to worry about SQL quote escaping.
	 */
	public List<T> queryForFieldValuesArgs(Map<String, Object> fieldValues) throws SQLException;

	/**
	 * Query for a data item in the table that has the same ID as the data parameter.
	 */
	public T queryForSameId(T data) throws SQLException;

	/**
	 * Create and return a new query builder object which allows you to build a custom SELECT statement. You call
	 * methods on the builder to construct your statement and then call {@link QueryBuilder#prepare()} once you are
	 * ready to build. This returns a {@link PreparedQuery} object which gets passed to {@link #query(PreparedQuery)} or
	 * {@link #iterator(PreparedQuery)}.
	 */
	public QueryBuilder<T, ID> queryBuilder();

	/**
	 * Like {@link #queryBuilder()} but allows you to build an UPDATE statement. You can then call call
	 * {@link UpdateBuilder#prepare()} and pass the returned {@link PreparedUpdate} to {@link #update(PreparedUpdate)}.
	 */
	public UpdateBuilder<T, ID> updateBuilder();

	/**
	 * Like {@link #queryBuilder()} but allows you to build an DELETE statement. You can then call call
	 * {@link DeleteBuilder#prepare()} and pass the returned {@link PreparedDelete} to {@link #delete(PreparedDelete)}.
	 */
	public DeleteBuilder<T, ID> deleteBuilder();

	/**
	 * Query for the items in the object table which match the prepared query. See {@link #queryBuilder} for more
	 * information.
	 * 
	 * <p>
	 * <b>NOTE:</b> For medium sized or large tables, this may load a lot of objects into memory so you should consider
	 * using the {@link #iterator(PreparedQuery)} method instead.
	 * </p>
	 * 
	 * @param preparedQuery
	 *            Query used to match the objects in the database.
	 * @return A list of all of the objects in the table that match the query.
	 * @throws SQLException
	 *             on any SQL problems.
	 */
	public List<T> query(PreparedQuery<T> preparedQuery) throws SQLException;

	/**
	 * Create a new row in the database from an object.
	 * 
	 * @param data
	 *            The data item that we are creating in the database.
	 * @return The number of rows updated in the database. This should be 1.
	 */
	public int create(T data) throws SQLException;

	/**
	 * This is a convenience method to creating a data item but only if the ID does not already exist in the table. This
	 * extracts the ID from the data parameter, does a {@link #queryForId(Object)} on it, returning the data if it
	 * exists. If it does not exist {@link #create(Object)} will be called with the parameter.
	 * 
	 * @return Either the data parameter if it was inserted or the data that existed already in the database.
	 */
	public T createIfNotExists(T data) throws SQLException;

	/**
	 * This is a convenience method for creating an item in the database if it does not exist. If it does exist then all
	 * of the fields will be updated from the fields in the parameter object.
	 * 
	 * @return Status object with the number of rows changed and whether an insert or update was performed.
	 */
	public CreateOrUpdateStatus createOrUpdate(T data) throws SQLException;

	/**
	 * Save the fields from an object to the database. If you have made changes to an object, this is how you persist
	 * those changes to the database. You cannot use this method to update the id field -- see {@link #updateId}.
	 * 
	 * @param data
	 *            The data item that we are updating in the database.
	 * @return The number of rows updated in the database. This should be 1.
	 * @throws SQLException
	 *             on any SQL problems.
	 * @throws IllegalArgumentException
	 *             If there is only an ID field in the object. See the {@link #updateId} method.
	 */
	public int update(T data) throws SQLException;

	/**
	 * Update an object in the database to change its id to the newId parameter. The data <i>must</i> have its current
	 * id set. If the id field has already changed then it cannot be updated. After the id has been updated in the
	 * database, the id field of the data object will also be changed.
	 * 
	 * <p>
	 * <b>NOTE:</b> Depending on the database type and the id type, you may be unable to change the id of the field.
	 * </p>
	 * 
	 * @param data
	 *            The data item that we are updating in the database with the current id.
	 * @param newId
	 *            The <i>new</i> id that you want to update the data with.
	 * @return The number of rows updated in the database. This should be 1.
	 * @throws SQLException
	 *             on any SQL problems.
	 */
	public int updateId(T data, ID newId) throws SQLException;

	/**
	 * Update all rows in the table according to the prepared statement argument. To use this, the {@link UpdateBuilder}
	 * must have set-columns applied to it using the {@link UpdateBuilder#updateColumnValue(String, Object)} or
	 * {@link UpdateBuilder#updateColumnExpression(String, String)} methods.
	 * 
	 * @param preparedUpdate
	 *            A prepared statement to match database rows to be deleted and define the columns to update.
	 * @return The number of rows updated in the database.
	 * @throws SQLException
	 *             on any SQL problems.
	 * @throws IllegalArgumentException
	 *             If there is only an ID field in the object. See the {@link #updateId} method.
	 */
	public int update(PreparedUpdate<T> preparedUpdate) throws SQLException;

	/**
	 * Does a query for the object's id and copies in each of the field values from the database to refresh the data
	 * parameter. Any local object changes to persisted fields will be overwritten. If the database has been updated
	 * this brings your local object up to date.
	 * 
	 * @param data
	 *            The data item that we are refreshing with fields from the database.
	 * @return The number of rows found in the database that correspond to the data id. This should be 1.
	 * @throws SQLException
	 *             on any SQL problems or if the data item is not found in the table or if more than 1 item is found
	 *             with data's id.
	 */
	public int refresh(T data) throws SQLException;

	/**
	 * Delete an object from the database.
	 * 
	 * @param data
	 *            The data item that we are deleting from the database.
	 * @return The number of rows updated in the database. This should be 1.
	 * @throws SQLException
	 *             on any SQL problems.
	 */
	public int delete(T data) throws SQLException;

	/**
	 * Delete an object from the database that has an id.
	 * 
	 * @param id
	 *            The id of the item that we are deleting from the database.
	 * @return The number of rows updated in the database. This should be 1.
	 * @throws SQLException
	 *             on any SQL problems.
	 */
	public int deleteById(ID id) throws SQLException;

	/**
	 * Delete a collection of objects from the database using an IN SQL clause.
	 * 
	 * @param datas
	 *            A collection of data items to be deleted.
	 * @return The number of rows updated in the database. This should be the size() of the collection.
	 * @throws SQLException
	 *             on any SQL problems.
	 */
	public int delete(Collection<T> datas) throws SQLException;

	/**
	 * Delete the objects that match the collection of ids from the database using an IN SQL clause.
	 * 
	 * @param ids
	 *            A collection of data ids to be deleted.
	 * @return The number of rows updated in the database. This should be the size() of the collection.
	 * @throws SQLException
	 *             on any SQL problems.
	 */
	public int deleteIds(Collection<ID> ids) throws SQLException;

	/**
	 * Delete the objects that match the prepared statement argument.
	 * 
	 * @param preparedDelete
	 *            A prepared statement to match database rows to be deleted.
	 * @return The number of rows updated in the database.
	 * @throws SQLException
	 *             on any SQL problems.
	 */
	public int delete(PreparedDelete<T> preparedDelete) throws SQLException;

	/**
	 * This satisfies the {@link Iterable} interface for the class and allows you to iterate through the objects in the
	 * table using SQL. You can use code similar to the following:
	 * 
	 * <p>
	 * <blockquote>
	 * 
	 * <pre>
	 * for (Account account : accountDao) { ... }
	 * </pre>
	 * 
	 * </blockquote>
	 * </p>
	 * 
	 * <p>
	 * <b>WARNING</b>: because the {@link Iterator#hasNext()}, {@link Iterator#next()}, etc. methods can only throw
	 * {@link RuntimeException}, the code has to wrap any {@link SQLException} with {@link IllegalStateException}. Make
	 * sure to catch {@link IllegalStateException} and look for a {@link SQLException} cause.
	 * </p>
	 * 
	 * <p>
	 * <b>WARNING</b>: The underlying results object will only be closed if you page all the way to the end of the
	 * iterator using the for() loop or if you call {@link CloseableIterator#close()} directly. You can also call the
	 * {@link #closeLastIterator()} if you are not iterating across this DAO in multiple threads.
	 * </p>
	 * 
	 * @return An iterator of the class that uses SQL to step across the database table.
	 * 
	 * @throws IllegalStateException
	 *             When it encounters a SQLException or in other cases.
	 */
	public CloseableIterator<T> iterator();

	/**
	 * Same as {@link #iterator()} but while specifying flags for the results. This is necessary with certain database
	 * types.
	 * 
	 * <p>
	 * <b>WARNING:</b> Depending on the database type the underlying connection may never be freed -- even if you go all
	 * of the way through the results. It is <i>strongly</i> recommended that you call the
	 * {@link CloseableIterator#close()} method when you are done with the iterator.
	 * </p>
	 */
	public CloseableIterator<T> iterator(int resultFlags);

	/**
	 * Same as {@link #iterator()} but with a prepared query parameter. See {@link #queryBuilder} for more information.
	 * You use it like the following:
	 * 
	 * <p>
	 * <blockquote>
	 * 
	 * <pre>
	 * QueryBuilder&lt;Account, String&gt; qb = accountDao.queryBuilder();
	 * ... custom query builder methods
	 * CloseableIterator&lt;Account&gt; iterator = partialDao.iterator(qb.prepare());
	 * try {
	 *     while (iterator.hasNext()) {
	 *         Account account = iterator.next();
	 *         ...
	 *     }
	 * } finish {
	 *     iterator.close();
	 * }
	 * </pre>
	 * 
	 * </blockquote>
	 * </p>
	 * 
	 * @param preparedQuery
	 *            Query used to iterate across a sub-set of the items in the database.
	 * @return An iterator for T.
	 * @throws SQLException
	 *             on any SQL problems.
	 */
	public CloseableIterator<T> iterator(PreparedQuery<T> preparedQuery) throws SQLException;

	/**
	 * Same as {@link #iterator(PreparedQuery)} but while specifying flags for the results. This is necessary with
	 * certain database types.
	 */
	public CloseableIterator<T> iterator(PreparedQuery<T> preparedQuery, int resultFlags) throws SQLException;

	/**
	 * This makes a one time use iterable class that can be closed afterwards. The DAO itself is
	 * {@link CloseableWrappedIterable} but multiple threads can each call this to get their own closeable iterable.
	 * This allows you to do something like:
	 * 
	 * <blockquote>
	 * 
	 * <pre>
	 * CloseableWrappedIterable<Foo> wrappedIterable = fooDao.getWrappedIterable();
	 * try {
	 *   for (Foo foo : wrappedIterable) {
	 *       ...
	 *   }
	 * } finally {
	 *   wrappedIterable.close();
	 * }
	 * </pre>
	 * 
	 * </blockquote>
	 */
	public CloseableWrappedIterable<T> getWrappedIterable();

	/**
	 * Same as {@link #getWrappedIterable()} but with a prepared query parameter. See {@link #queryBuilder} or
	 * {@link #iterator(PreparedQuery)} for more information.
	 */
	public CloseableWrappedIterable<T> getWrappedIterable(PreparedQuery<T> preparedQuery);

	/**
	 * This closes the last iterator returned by the {@link #iterator()} method.
	 * 
	 * <p>
	 * <b>NOTE:</b> This is not reentrant. If multiple threads are getting iterators from this DAO then you should use
	 * the {@link #getWrappedIterable()} method to get a wrapped iterable for each thread instead.
	 * </p>
	 */
	public void closeLastIterator() throws SQLException;

	/**
	 * Similar to the {@link #iterator(PreparedQuery)} except it returns a GenericRawResults object associated with the
	 * SQL select query argument. Although you should use the {@link #iterator()} for most queries, this method allows
	 * you to do special queries that aren't supported otherwise. Like the above iterator methods, you must call close
	 * on the returned RawResults object once you are done with it. The arguments are optional but can be set with
	 * strings to expand ? type of SQL.
	 * 
	 * <p>
	 * You can use the {@link QueryBuilder#prepareStatementString()} method here if you want to build the query using
	 * the structure of the QueryBuilder.
	 * </p>
	 * 
	 * <p>
	 * 
	 * <pre>
	 * QueryBuilder&lt;Account, Integer&gt; qb = accountDao.queryBuilder();
	 * qb.where().ge(&quot;orderCount&quot;, 10);
	 * results = accountDao.queryRaw(qb.prepareStatementString());
	 * </pre>
	 * 
	 * </p>
	 * 
	 * If you want to use the QueryBuilder with arguments to the raw query then you should do something like:
	 * 
	 * <p>
	 * 
	 * <pre>
	 * QueryBuilder&lt;Account, Integer&gt; qb = accountDao.queryBuilder();
	 * // we specify a SelectArg here to generate a ? in the statement string below
	 * qb.where().ge(&quot;orderCount&quot;, new SelectArg());
	 * // the 10 at the end is an optional argument to fulfill the SelectArg above
	 * results = accountDao.queryRaw(qb.prepareStatementString(), rawRowMapper, 10);
	 * </pre>
	 * 
	 * </p>
	 */
	public GenericRawResults<String[]> queryRaw(String query, String... arguments) throws SQLException;

	/**
	 * Similar to the {@link #queryRaw(String, String...)} but this iterator returns rows that you can map yourself. For
	 * every result that is returned by the database, the {@link RawRowMapper#mapRow(String[], String[])} method is
	 * called so you can convert the result columns into an object to be returned by the iterator. The arguments are
	 * optional but can be set with strings to expand ? type of SQL.
	 */
	public <UO> GenericRawResults<UO> queryRaw(String query, RawRowMapper<UO> mapper, String... arguments)
			throws SQLException;

	/**
	 * Similar to the {@link #queryRaw(String, String...)} but instead of an array of String results being returned by
	 * the iterator, this uses the column-types parameter to return an array of Objects instead. The arguments are
	 * optional but can be set with strings to expand ? type of SQL.
	 */
	public GenericRawResults<Object[]> queryRaw(String query, DataType[] columnTypes, String... arguments)
			throws SQLException;

	/**
	 * Run a raw execute SQL statement to the database.The arguments are optional but can be set with strings to expand
	 * ? type of SQL.
	 * 
	 * @return number of rows affected.
	 */
	public int executeRaw(String statement, String... arguments) throws SQLException;

	/**
	 * Run a raw update SQL statement to the database. The statement must be an SQL INSERT, UPDATE or DELETE
	 * statement.The arguments are optional but can be set with strings to expand ? type of SQL.
	 * 
	 * @return number of rows affected.
	 */
	public int updateRaw(String statement, String... arguments) throws SQLException;

	/**
	 * Call the call-able that will perform a number of batch tasks. This is for performance when you want to run a
	 * number of database operations at once -- maybe loading data from a file. This will turn off what databases call
	 * "auto-commit" mode, run the call-able and then re-enable "auto-commit". If auto-commit is not supported then a
	 * transaction will be used instead.
	 * 
	 * <p>
	 * <b>NOTE:</b> If neither auto-commit nor transactions are supported by the database type then this may just call
	 * the callable.
	 * </p>
	 */
	public <CT> CT callBatchTasks(Callable<CT> callable) throws Exception;

	/**
	 * Return the string version of the object with each of the known field values shown. Useful for testing and
	 * debugging.
	 * 
	 * @param data
	 *            The data item for which we are returning the toString information.
	 */
	public String objectToString(T data);

	/**
	 * Return true if the two arguments are equal. This checks each of the fields defined in the database to see if they
	 * are equal. Useful for testing and debugging.
	 * 
	 * @param data1
	 *            One of the data items that we are checking for equality.
	 * @param data2
	 *            The other data item that we are checking for equality.
	 */
	public boolean objectsEqual(T data1, T data2) throws SQLException;

	/**
	 * Returns the ID from the data argument passed in. This is used by some of the internal queries to be able to
	 * search by id.
	 */
	public ID extractId(T data) throws SQLException;

	/**
	 * Returns the class of the DAO. This is used by internal query operators.
	 */
	public Class<T> getDataClass();

	/**
	 * Returns the class of the DAO. This is used by internal query operators.
	 */
	public FieldType findForeignFieldType(Class<?> clazz);

	/**
	 * Returns true if we can call update on this class. This is used most likely by folks who are extending the base
	 * dao classes.
	 */
	public boolean isUpdatable();

	/**
	 * Returns true if the table already exists otherwise false.
	 */
	public boolean isTableExists() throws SQLException;

	/**
	 * Returns the number of rows in the table associated with the data class. Depending on the size of the table and
	 * the database type, this may be expensive and take a while.
	 */
	public long countOf() throws SQLException;

	/**
	 * Returns the number of rows in the table associated with the prepared query passed in. Depending on the size of
	 * the table and the database type, this may be expensive and take a while.
	 */
	public long countOf(PreparedQuery<T> preparedQuery) throws SQLException;

	/**
	 * Returns an empty collection that can be assigned to a collection field. This allows you to add things to the
	 * collection from the start.
	 * 
	 * For example let's say you have an Account which has the field:
	 * 
	 * <pre>
	 * &#064;ForeignCollectionField(columnName = &quot;orders&quot;)
	 * Collection&lt;Order&gt; orders;
	 * </pre>
	 * 
	 * You would then call:
	 * 
	 * <pre>
	 * account.orders = accoundDao.getEmptyForeignCollection(&quot;orders&quot;);
	 * // this would add it the collection and the internal DAO
	 * account.orders.add(order1);
	 * </pre>
	 * 
	 * @param fieldName
	 *            parameter is the field name of the foreign collection field -- you might consider using the
	 *            {@link ForeignCollectionField#columnName()} to set the name to a static name.
	 */
	public <FT> ForeignCollection<FT> getEmptyForeignCollection(String fieldName) throws SQLException;

	/**
	 * Call this with true to enable an object cache for the DAO. Set to false to disable any caching. It is (as of
	 * 9/2011) one of the newer features of ORMLite. It keeps a {@link ReferenceObjectCache} of the objects (using
	 * {@link WeakReference}) referenced by the DAO. No support for objects returned by the {@link #queryRaw} methods.
	 * 
	 * @throws SQLException
	 *             If the DAO's class does not have an id field which is required by the {@link ObjectCache}.
	 */
	public void setObjectCache(boolean enabled) throws SQLException;

	/**
	 * Same as {@link #setObjectCache(boolean)} except you specify the actual cache instance to use for the DAO. This
	 * allows you to use a {@link ReferenceObjectCache} with {@link SoftReference} setting, the {@link LruObjectCache},
	 * or inject your own cache implementation. Call it with null to disable the cache.
	 * 
	 * @throws SQLException
	 *             If the DAO's class does not have an id field which is required by the {@link ObjectCache}.
	 */
	public void setObjectCache(ObjectCache objectCache) throws SQLException;

	/**
	 * Returns the current object-cache being used by the DAO or null if none.
	 */
	public ObjectCache getObjectCache();

	/**
	 * Flush the object cache if it has been enabled. This will remove an objects that are in the cache to reclaim
	 * memory. Any future queries will re-request them from the database.
	 */
	public void clearObjectCache();

	/**
	 * Return the latest row from the database results from a query to select * (star).
	 */
	public T mapSelectStarRow(DatabaseResults results) throws SQLException;

	/**
	 * Return a row mapper that is suitable for mapping results from a query to select * (star).
	 */
	public GenericRowMapper<T> getSelectStarRowMapper() throws SQLException;

	/**
	 * Returns true if an object exists that matches this ID otherwise false.
	 */
	public boolean idExists(ID id) throws SQLException;

	/**
	 * Return class for the {@link Dao#createOrUpdate(Object)} method.
	 */
	public class CreateOrUpdateStatus {
		private boolean created;
		private boolean updated;
		private int numLinesChanged;
		public CreateOrUpdateStatus(boolean created, boolean updated, int numberLinesChanged) {
			this.created = created;
			this.updated = updated;
			this.numLinesChanged = numberLinesChanged;
		}
		public boolean isCreated() {
			return created;
		}
		public boolean isUpdated() {
			return updated;
		}
		public int getNumLinesChanged() {
			return numLinesChanged;
		}
	}
}
