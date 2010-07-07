package com.j256.ormlite.db;

import java.util.List;

import com.j256.ormlite.field.FieldType;

/**
 * IBM DB2 database type information used to create the tables, etc..
 * 
 * <p>
 * <b>WARNING:</b> I have not tested this unfortunately because of a lack of access to a DB2 instance. Love to get 1-2
 * hours of access to an database to test/tweak this. Undoubtably is it wrong. Please contact the author if you'd like
 * to help.
 * </p>
 * 
 * @author graywatson
 */
public class Db2DatabaseType extends BaseDatabaseType implements DatabaseType {

	private final static String DATABASE_URL_PORTION = "db2";
	private final static String DRIVER_CLASS_NAME = "COM.ibm.db2.jdbc.app.DB2Driver";

	public String getDriverUrlPart() {
		return DATABASE_URL_PORTION;
	}

	public String getDriverClassName() {
		return DRIVER_CLASS_NAME;
	}

	@Override
	protected void appendBooleanType(StringBuilder sb) {
		sb.append("SMALLINT");
	}

	@Override
	protected void appendByteType(StringBuilder sb) {
		sb.append("SMALLINT");
	}

	@Override
	protected void appendObjectType(StringBuilder sb) {
		sb.append("VARCHAR [] FOR BIT DATA");
	}

	@Override
	protected void configureGeneratedId(StringBuilder sb, FieldType fieldType, List<String> statementsBefore,
			List<String> additionalArgs, List<String> queriesAfter) {
		sb.append("GENERATED ALWAYS AS IDENTITY ");
		configureId(sb, fieldType, statementsBefore, additionalArgs, queriesAfter);
	}

	@Override
	public void appendEscapedEntityName(StringBuilder sb, String word) {
		sb.append('\"').append(word).append('\"');
	}
}
