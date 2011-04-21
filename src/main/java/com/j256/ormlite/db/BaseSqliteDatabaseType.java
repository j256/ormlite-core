package com.j256.ormlite.db;

import java.util.List;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.FieldConverter;
import com.j256.ormlite.field.FieldType;

/**
 * Sqlite database type information used to create the tables, etc..
 * 
 * <p>
 * NOTE: We need this here because the Android version subclasses it.
 * 
 * @author graywatson
 */
public abstract class BaseSqliteDatabaseType extends BaseDatabaseType implements DatabaseType {

	private final static FieldConverter booleanConverter = new BooleanNumberFieldConverter();

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
	protected boolean generatedIdSqlAtEnd() {
		return false;
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

	@Override
	public FieldConverter getFieldConverter(DataType dataType) {
		// we are only overriding certain types
		switch (dataType) {
			case BOOLEAN :
			case BOOLEAN_OBJ :
				return booleanConverter;
			default :
				return super.getFieldConverter(dataType);
		}
	}
}
