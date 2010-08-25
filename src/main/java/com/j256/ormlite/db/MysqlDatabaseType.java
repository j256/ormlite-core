package com.j256.ormlite.db;

import java.util.List;

import com.j256.ormlite.field.FieldType;

/**
 * MySQL database type information used to create the tables, etc..
 * 
 * <p>
 * <b>NOTE:</b> By default the tables are created with the ENGINE=InnoDB suffix (see
 * {@link #DEFAULT_CREATE_TABLE_SUFFIX}. Use {@link #setCreateTableSuffix} to change that to "" to use the default
 * MyISAM storage engine, to choose another engine, or set other settings. For more information about engines, see the
 * 'SHOW ENGINES;' results from the MySQL command line tool.
 * </p>
 * 
 * @author graywatson
 */
public class MysqlDatabaseType extends BaseDatabaseType implements DatabaseType {

	private final static String DATABASE_URL_PORTION = "mysql";
	private final static String DRIVER_CLASS_NAME = "com.mysql.jdbc.Driver";

	/**
	 * Default suffix to the CREATE TABLE statement. Change with the {@link #setCreateTableSuffix} method.
	 */
	public final static String DEFAULT_CREATE_TABLE_SUFFIX = "ENGINE=InnoDB";

	private String createTableSuffix = DEFAULT_CREATE_TABLE_SUFFIX;

	public String getDriverUrlPart() {
		return DATABASE_URL_PORTION;
	}

	public String getDriverClassName() {
		return DRIVER_CLASS_NAME;
	}

	/**
	 * Set the string that is appended to the end of a CREATE TABLE statement.
	 */
	public void setCreateTableSuffix(String createTableSuffix) {
		this.createTableSuffix = createTableSuffix;
	}

	@Override
	protected void appendDateType(StringBuilder sb, int fieldWidth) {
		/**
		 * TIMESTAMP in MySQL does some funky stuff with the last-modification time. Values are 'not null' by default
		 * with an automatic default of CURRENT_TIMESTAMP. Strange design decision.
		 */
		sb.append("DATETIME");
	}

	@Override
	protected void appendBooleanType(StringBuilder sb) {
		sb.append("TINYINT(1)");
	}

	@Override
	protected void appendObjectType(StringBuilder sb) {
		sb.append("BLOB");
	}

	@Override
	protected void configureGeneratedId(StringBuilder sb, FieldType fieldType, List<String> statementsBefore,
			List<String> additionalArgs, List<String> queriesAfter) {
		sb.append("AUTO_INCREMENT ");
		configureId(sb, fieldType, statementsBefore, additionalArgs, queriesAfter);
	}

	@Override
	public void appendCreateTableSuffix(StringBuilder sb) {
		sb.append(createTableSuffix);
		sb.append(' ');
	}
}
