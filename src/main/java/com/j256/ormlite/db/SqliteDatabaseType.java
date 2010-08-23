package com.j256.ormlite.db;

import java.util.List;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.FieldType;

/**
 * Sqlite database type information used to create the tables, etc..
 * 
 * @author graywatson
 */
public class SqliteDatabaseType extends BaseDatabaseType implements DatabaseType {

	private final static String DATABASE_URL_PORTION = "sqlite";
	private final static String DRIVER_CLASS_NAME = "org.sqlite.JDBC";

	public String getDriverUrlPart() {
		return DATABASE_URL_PORTION;
	}

	public String getDriverClassName() {
		return DRIVER_CLASS_NAME;
	}

	@Override
	protected void configureGeneratedId(StringBuilder sb, FieldType fieldType, List<String> statementsBefore,
			List<String> additionalArgs, List<String> queriesAfter) {
		if (fieldType.getDataType() != DataType.INTEGER && fieldType.getDataType() != DataType.INTEGER_OBJ) {
			throw new IllegalArgumentException("Sqlite requires that auto-increment generated-id be integer types");
		}
		sb.append("PRIMARY KEY AUTOINCREMENT ");
		// no additional call to configureId here
	}

	@Override
	public boolean isVarcharFieldWidthSupported() {
		return false;
	}

	@Override
	public boolean isCreateTableReturnsZero() {
		// 'CREATE TABLE' statements seem to return 1 for some reason
		return false;
	}
}
