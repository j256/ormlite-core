package com.j256.ormlite.dao;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.j256.ormlite.stmt.PreparedStmt;
import com.j256.ormlite.stmt.StatementBuilder;
import com.j256.ormlite.stmt.SelectIterator;

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
	 * Query for and return the first item in the object table which matches the {@link PreparedStmt}. See
	 * {@link #queryBuilder} for more information. This can be used to return the object that matches a single unique
	 * column. You should use {@link #queryForId} if you want to query for the id column.
	 * 
	 * @param preparedQuery
	 *            Query used to match the objects in the database.
	 * @return The first object that matches the query.
	 * @throws SQLException
	 *             on any SQL problems.
	 */
	public T queryForFirst(PreparedStmt<T> preparedQuery) throws SQLException;

	/**
	 * Query for all of the items in the object table. For medium sized or large tables, this may load a lot of objects
	 * into memory so you should consider using the {@link #iterator} method instead.
	 * 
	 * @return A list of all of the objects in the table.
	 * @throws SQLException
	 *             on any SQL problems.
	 */
	public List<T> queryForAll() throws SQLException;

	/**
	 * Query for all of the items in the object table that match the SQL select query argument. Although you should use
	 * the {@link StatementBuilder} for most queries, this method allows you to do special queries that aren't supported
	 * otherwise. For medium sized or large tables, this may load a lot of objects into memory so you should consider
	 * using the {@link #iteratorRaw(String)} method instead.
	 * 
	 * @return A raw results object from which you can get the results.
	 * @throws SQLException
	 *             on any SQL problems.
	 */
	public RawResults queryForAllRaw(String query) throws SQLException;

	/**
	 * Create and return a new {@link StatementBuilder} object which allows you to build a custom statement. You call
	 * methods on the {@link StatementBuilder} to construct your statement and then call
	 * {@link StatementBuilder#prepareStatement()} once you are ready to build. This returns a {@link PreparedStmt}
	 * object which gets passed to {@link #query(PreparedStmt)} or {@link #iterator(PreparedStmt)}.
	 */
	public StatementBuilder<T, ID> statementBuilder();

	/**
	 * @deprecated Use {@link #statementBuilder()}
	 */
	@Deprecated()
	public StatementBuilder<T, ID> queryBuilder();

	/**
	 * Query for the items in the object table which match the {@link PreparedStmt}. See {@link #queryBuilder} for more
	 * information.
	 * 
	 * <p>
	 * <b>NOTE:</b> For medium sized or large tables, this may load a lot of objects into memory so you should consider
	 * using the {@link #iterator(PreparedStmt)} method instead.
	 * </p>
	 * 
	 * @param preparedQuery
	 *            Query used to match the objects in the database.
	 * @return A list of all of the objects in the table that match the query.
	 * @throws SQLException
	 *             on any SQL problems.
	 */
	public List<T> query(PreparedStmt<T> preparedQuery) throws SQLException;

	/**
	 * Create a new row in the database from an object.
	 * 
	 * @param data
	 *            The data item that we are creating in the database.
	 * @return The number of rows updated in the database. This should be 1.
	 */
	public int create(T data) throws SQLException;

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
	 * sure to catch {@link IllegalStateException} and look for a {@link SQLException} cause. See the
	 * {@link SelectIterator} source code for more details.
	 * </p>
	 * 
	 * <p>
	 * <b>WARNING</b>: The underlying results object will only be closed if you page all the way to the end of the
	 * iterator using the for() loop or if you call {@link SelectIterator#close()} directly. It is also closed when it
	 * is garbage collected but this is considered bad form.
	 * </p>
	 * 
	 * @return An iterator of the class that uses SQL to step across the database table.
	 * 
	 * @throws IllegalStateException
	 *             When it encounters a SQLException or in other cases.
	 */
	public CloseableIterator<T> iterator();

	/**
	 * Same as {@link #iterator()} but with a {@link PreparedStmt} parameter. See {@link #queryBuilder} for more
	 * information. You use it like the following:
	 * 
	 * <p>
	 * <blockquote>
	 * 
	 * <pre>
	 * QueryBuilder&lt;Account, String&gt; qb = accountDao.queryBuilder();
	 * ... custom query builder methods
	 * SelectIterator&lt;Account&gt; iterator = partialDao.iterator(qb.prepareQuery());
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
	public CloseableIterator<T> iterator(PreparedStmt<T> preparedQuery) throws SQLException;

	/**
	 * Same as {@link #iterator(PreparedStmt)} except it returns a RawResults object associated with the SQL select
	 * query argument. Although you should use the {@link #iterator()} for most queries, this method allows you to do
	 * special queries that aren't supported otherwise. Like the above iterator methods, you must call close on the
	 * returned RawResults object once you are done with it.
	 */
	public RawResults iteratorRaw(String query) throws SQLException;

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
}
