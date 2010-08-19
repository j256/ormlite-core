package com.j256.ormlite.db;

import java.sql.PreparedStatement;
import java.util.List;

import com.j256.ormlite.field.FieldConverter;
import com.j256.ormlite.field.FieldType;

/**
 * Definition of the per-database functionality needed to isolate the differences between the various databases.
 * 
 * @author graywatson
 */
public interface DatabaseType {

	/**
	 * Return the part in the database URI which identifies the particular database. Usually the URI is in the form
	 * jdbc:ddd:... where ddd is the driver url part.
	 */
	public String getDriverUrlPart();

	/**
	 * Return the class name of the database driver.
	 */
	public String getDriverClassName();

	/**
	 * Load the driver class associated with this database so it can wire itself into JDBC.
	 * 
	 * @throws ClassNotFoundException
	 *             If the driver class is not available in the classpath.
	 */
	public void loadDriver() throws ClassNotFoundException;

	/**
	 * Takes a {@link FieldType} and appends the SQL necessary to create the field to the string builder. The field may
	 * also generate additional arguments which go at the end of the insert statement or additional statements to be
	 * executed before or afterwards depending on the configurations. The database can also add to the list of queries
	 * that will be performed afterward to test portions of the config.
	 */
	public void appendColumnArg(StringBuilder sb, FieldType fieldType, List<String> additionalArgs,
			List<String> statementsBefore, List<String> statementsAfter, List<String> queriesAfter);

	/**
	 * Convert and return the column name for table and sequence creation. Often this is necessary to fix case issues.
	 */
	public String convertColumnName(String columnName);

	/**
	 * Takes a {@link FieldType} and adds the necessary statements to the before and after lists necessary so that the
	 * dropping of the table will succeed and will clear other associated sequences or other database artifacts
	 */
	public void dropColumnArg(FieldType fieldType, List<String> statementsBefore, List<String> statementsAfter);

	/**
	 * Add a entity-name word to the string builder wrapped in the proper characters to escape it. This avoids problems
	 * with table, column, and sequence-names being reserved words.
	 */
	public void appendEscapedEntityName(StringBuilder sb, String word);

	/**
	 * Add the word to the string builder wrapped in the proper characters to escape it. This avoids problems with data
	 * values being reserved words.
	 */
	public void appendEscapedWord(StringBuilder sb, String word);

	/**
	 * Return the name of an ID sequence based on the tabelName and the fieldType of the id.
	 */
	public String generateIdSequenceName(String tableName, FieldType idFieldType);

	/**
	 * Return the prefix to put at the front of a SQL line to mark it as a comment.
	 */
	public String getCommentLinePrefix();

	/**
	 * Return true if the database needs a sequence when you use generated IDs. Some databases (H2, MySQL) create them
	 * auto-magically. This also means that the database needs to query for a sequence value <i>before</i> the object is
	 * inserted. For old[er] versions of Postgres, for example, the JDBC call-back stuff to get the just-inserted id
	 * value does not work so we have to get the next sequence value by hand, assign it into the object, and then insert
	 * the object -- yes two SQL statements.
	 */
	public boolean isIdSequenceNeeded();

	/**
	 * Return the {@link FieldConverter} to associate with the {@link FieldType}. This allows the database instance to
	 * convert a field as necessary before it goes to the database.
	 */
	public FieldConverter getFieldConverter(FieldType fieldType);

	/**
	 * Return true if the database supports the width parameter on VARCHAR fields.
	 */
	public boolean isVarcharFieldWidthSupported();

	/**
	 * Return true if the database supports the LIMIT SQL command. Otherwise we have to use the
	 * {@link PreparedStatement#setMaxRows} instead. See prepareSqlStatement in MappedPreparedQuery.
	 */
	public boolean isLimitSqlSupported();

	/**
	 * Return true if the LIMIT should be called after SELECT otherwise at the end of the WHERE (the default).
	 */
	public boolean isLimitAfterSelect();

	/**
	 * Append to the string builder the necessary SQL to limit the results to a certain number.
	 */
	public void appendLimitValue(StringBuilder sb, int limit);

	/**
	 * Append the SQL necessary to get the next-value from a sequence. This is only necessary if
	 * {@link #isIdSequenceNeeded} is true.
	 */
	public void appendSelectNextValFromSequence(StringBuilder sb, String sequenceName);

	/**
	 * Append the SQL necessary to properly finish a CREATE TABLE line.
	 */
	public void appendCreateTableSuffix(StringBuilder sb);

	/**
	 * Returns true if a 'CREATE TABLE' statement should return 0. False if > 0.
	 */
	public boolean isCreateTableReturnsZero();

	/**
	 * Returns true if table and field names should be made uppercase.
	 * 
	 * <p>
	 * Turns out that Derby and Hsqldb are doing something wrong (IMO) with entity names. If you create a table with the
	 * name "footable" (with the quotes) then it will be created as lowercase footable, case sensitive. However, if you
	 * then issue the query 'select * from footable' (without quotes) it won't find the table because it gets promoted
	 * to be FOOTABLE and is searched in a case sensitive manner. So for these databases, entity names have to be forced
	 * to be uppercase so external queries will also work.
	 * </p>
	 */
	public boolean isEntityNamesMustBeUpCase();
}
