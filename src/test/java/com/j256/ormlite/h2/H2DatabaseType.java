package com.j256.ormlite.h2;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.db.BaseDatabaseType;
import com.j256.ormlite.field.FieldType;

/**
 * H2 database type.
 * 
 * @author graywatson
 */
public class H2DatabaseType extends BaseDatabaseType {

	public static final String DATABASE_URL = "jdbc:h2:mem:h2testdatabase";

	public H2DatabaseType() throws SQLException {
		setDriver(DriverManager.getDriver(DATABASE_URL));
	}

	@Override
	public boolean isDatabaseUrlThisType(String url, String dbTypePart) {
		return dbTypePart.equals("h2");
	}

	@Override
	protected String getDriverClassName() {
		return "org.h2.Driver";
	}

	@Override
	public String getDatabaseName() {
		return "h2";
	}

	@Override
	public boolean isOffsetLimitArgument() {
		return true;
	}

	@Override
	public void appendLimitValue(StringBuilder sb, long limit, Long offset) {
		sb.append("LIMIT ");
		if (offset != null) {
			sb.append(offset).append(',');
		}
		sb.append(limit).append(' ');
	}

	@Override
	public void appendOffsetValue(StringBuilder sb, long offset) {
		throw new IllegalStateException("Offset is part of the LIMIT in database type " + getClass());
	}

	@Override
	protected void configureGeneratedId(String tableName, StringBuilder sb, FieldType fieldType,
			List<String> statementsBefore, List<String> statementsAfter, List<String> additionalArgs,
			List<String> queriesAfter) {
		sb.append("AUTO_INCREMENT ");
		configureId(sb, fieldType, statementsBefore, additionalArgs, queriesAfter);
	}

	@Override
	public boolean isCreateIfNotExistsSupported() {
		return true;
	}
}
