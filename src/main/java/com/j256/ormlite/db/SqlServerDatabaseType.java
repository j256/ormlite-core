package com.j256.ormlite.db;

import java.util.List;

import com.j256.ormlite.field.FieldType;

/**
 * Microsoft SQL server database type information used to create the tables, etc..
 * 
 * <p>
 * <b>WARNING:</b> I have not tested this unfortunately because of a lack of permanent access to a MSSQL instance.
 * </p>
 * 
 * @author graywatson
 */
public class SqlServerDatabaseType extends BaseDatabaseType implements DatabaseType {

	private final static String DATABASE_URL_PORTION = "sqlserver";
	private final static String DRIVER_CLASS_NAME = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

	public String getDriverUrlPart() {
		return DATABASE_URL_PORTION;
	}

	public String getDriverClassName() {
		return DRIVER_CLASS_NAME;
	}

	@Override
	protected void appendBooleanType(StringBuilder sb) {
		sb.append("BIT");
	}

	@Override
	protected void appendByteType(StringBuilder sb) {
		// TINYINT exists but it gives 0-255 unsigned
		// http://msdn.microsoft.com/en-us/library/ms187745.aspx
		sb.append("SMALLINT");
	}

	@Override
	protected void appendDateType(StringBuilder sb) {
		// TIMESTAMP is some sort of internal database type
		// http://www.sqlteam.com/article/timestamps-vs-datetime-data-types
		sb.append("DATETIME");
	}

	@Override
	protected void configureGeneratedId(StringBuilder sb, FieldType fieldType, List<String> statementsBefore,
			List<String> additionalArgs, List<String> queriesAfter) {
		sb.append("IDENTITY ");
		configureId(sb, fieldType, statementsBefore, additionalArgs, queriesAfter);
	}

	@Override
	public void appendEscapedEntityName(StringBuilder sb, String word) {
		sb.append('\"').append(word).append('\"');
	}

	@Override
	public boolean isLimitAfterSelect() {
		return true;
	}

	@Override
	public void appendLimitValue(StringBuilder sb, int limit) {
		sb.append("TOP ").append(limit).append(' ');
	}
}
