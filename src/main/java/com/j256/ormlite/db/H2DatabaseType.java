package com.j256.ormlite.db;

import java.util.List;

import com.j256.ormlite.field.FieldType;

/**
 * H2 database type information used to create the tables, etc..
 * 
 * @author graywatson
 */
public class H2DatabaseType extends BaseDatabaseType implements DatabaseType {

	private final static String DATABASE_URL_PORTION = "h2";
	private final static String DRIVER_CLASS_NAME = "org.h2.Driver";

	public String getDriverUrlPart() {
		return DATABASE_URL_PORTION;
	}

	public String getDriverClassName() {
		return DRIVER_CLASS_NAME;
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
}
