package com.j256.ormlite.db;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.FieldConverter;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.support.DatabaseResults;

/**
 * Base class for all of the {@link DatabaseType} classes that provide the per-database type functionality to create
 * tables and build queries.
 * 
 * <p>
 * Here's a good page which shows some of the <a href="http://www.1keydata.com/sql/sql-primary-key.html" >differences
 * between SQL versions</a>.
 * </p>
 * 
 * @author graywatson
 */
public abstract class BaseDatabaseType implements DatabaseType {

	protected static int DEFAULT_VARCHAR_WIDTH = 255;
	protected static int DEFAULT_DATE_STRING_WIDTH = 50;
	protected static String DEFAULT_SEQUENCE_SUFFIX = "_id_seq";

	/**
	 * Return the name of the driver class associated with this database type.
	 */
	protected abstract String getDriverClassName();

	/**
	 * Return the name of the database for logging purposes.
	 */
	protected abstract String getDatabaseName();

	public void loadDriver() throws SQLException {
		String className = getDriverClassName();
		if (className != null) {
			// this instantiates the driver class which wires in the JDBC glue
			try {
				Class.forName(className);
			} catch (ClassNotFoundException e) {
				throw SqlExceptionUtil.create("Driver class was not found for " + getDatabaseName()
						+ " database.  Missing jar with class " + className + ".", e);
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void appendColumnArg(StringBuilder sb, FieldType fieldType, List<String> additionalArgs,
			List<String> statementsBefore, List<String> statementsAfter, List<String> queriesAfter) throws SQLException {
		appendEscapedEntityName(sb, fieldType.getDbColumnName());
		sb.append(' ');
		int fieldWidth;
		switch (fieldType.getDataType()) {

			case STRING :
			case UUID :
				fieldWidth = fieldType.getWidth();
				if (fieldWidth == 0) {
					fieldWidth = getDefaultVarcharWidth();
				}
				appendStringType(sb, fieldWidth);
				break;

			case LONG_STRING :
				appendLongStringType(sb);
				break;

			case STRING_BYTES :
				appendByteArrayType(sb);
				break;

			case BOOLEAN :
			case BOOLEAN_OBJ :
				appendBooleanType(sb);
				break;

			case DATE :
			case JAVA_DATE :
				fieldWidth = fieldType.getWidth();
				if (fieldWidth == 0) {
					fieldWidth = DEFAULT_DATE_STRING_WIDTH;
				}
				appendDateType(sb, fieldWidth);
				break;

			case DATE_LONG :
			case JAVA_DATE_LONG :
				appendDateLongType(sb);
				break;

			case DATE_STRING :
			case JAVA_DATE_STRING :
				fieldWidth = fieldType.getWidth();
				if (fieldWidth == 0) {
					fieldWidth = DEFAULT_DATE_STRING_WIDTH;
				}
				appendDateStringType(sb, fieldWidth);
				break;

			case CHAR :
			case CHAR_OBJ :
				appendCharType(sb);
				break;

			case BYTE :
			case BYTE_OBJ :
				appendByteType(sb);
				break;

			case BYTE_ARRAY :
				appendByteArrayType(sb);
				break;

			case SHORT :
			case SHORT_OBJ :
				appendShortType(sb);
				break;

			case INTEGER :
			case INTEGER_OBJ :
				appendIntegerType(sb);
				break;

			case LONG :
			case LONG_OBJ :
				appendLongType(sb);
				break;

			case FLOAT :
			case FLOAT_OBJ :
				appendFloatType(sb);
				break;

			case DOUBLE :
			case DOUBLE_OBJ :
				appendDoubleType(sb);
				break;

			case SERIALIZABLE :
				appendSerializableType(sb);
				break;

			case ENUM_STRING :
				appendEnumStringType(sb, fieldType);
				break;

			case ENUM_INTEGER :
				appendEnumIntType(sb, fieldType);
				break;

			case UNKNOWN :
			default :
				// shouldn't be able to get here unless we have a missing case
				throw new IllegalArgumentException("Unknown field type " + fieldType.getDataType());
		}
		sb.append(' ');
		/*
		 * NOTE: the configure id methods must be in this order since isGeneratedIdSequence is also isGeneratedId and
		 * isId. isGeneratedId is also isId.
		 */
		if (fieldType.isGeneratedIdSequence() && !fieldType.isSelfGeneratedId()) {
			configureGeneratedIdSequence(sb, fieldType, statementsBefore, additionalArgs, queriesAfter);
		} else if (fieldType.isGeneratedId() && !fieldType.isSelfGeneratedId()) {
			configureGeneratedId(sb, fieldType, statementsBefore, additionalArgs, queriesAfter);
		} else if (fieldType.isId()) {
			configureId(sb, fieldType, statementsBefore, additionalArgs, queriesAfter);
		}
		// if we have a generated-id then neither the not-null nor the default make sense and cause syntax errors
		if (!fieldType.isGeneratedId()) {
			Object defaultValue = fieldType.getDefaultValue();
			if (defaultValue != null) {
				sb.append("DEFAULT ");
				appendDefaultValue(sb, fieldType, defaultValue);
				sb.append(' ');
			}
			if (fieldType.isCanBeNull()) {
				appendCanBeNull(sb, fieldType);
			} else {
				sb.append("NOT NULL ");
			}
			if (fieldType.isUnique()) {
				appendUnique(sb, fieldType, statementsAfter);
			}
		}
	}
	/**
	 * Output the SQL type for a Java String.
	 */
	protected void appendStringType(StringBuilder sb, int fieldWidth) {
		if (isVarcharFieldWidthSupported()) {
			sb.append("VARCHAR(").append(fieldWidth).append(")");
		} else {
			sb.append("VARCHAR");
		}
	}

	/**
	 * Output the SQL type for a Java Long String.
	 */
	protected void appendLongStringType(StringBuilder sb) {
		sb.append("TEXT");
	}

	/**
	 * Output the SQL type for a Java Date.
	 */
	protected void appendDateType(StringBuilder sb, int fieldWidth) {
		sb.append("TIMESTAMP");
	}

	/**
	 * Output the SQL type for a Java Date.
	 */
	protected void appendDateLongType(StringBuilder sb) {
		appendLongType(sb);
	}

	/**
	 * Output the SQL type for a Java Date as a string.
	 */
	protected void appendDateStringType(StringBuilder sb, int fieldWidth) {
		appendStringType(sb, fieldWidth);
	}

	/**
	 * Output the SQL type for a Java boolean.
	 */
	protected void appendBooleanType(StringBuilder sb) {
		sb.append("BOOLEAN");
	}

	/**
	 * Output the SQL type for a Java char.
	 */
	protected void appendCharType(StringBuilder sb) {
		sb.append("CHAR");
	}

	/**
	 * Output the SQL type for a Java byte.
	 */
	protected void appendByteType(StringBuilder sb) {
		sb.append("TINYINT");
	}

	/**
	 * Output the SQL type for a Java short.
	 */
	protected void appendShortType(StringBuilder sb) {
		sb.append("SMALLINT");
	}

	/**
	 * Output the SQL type for a Java integer.
	 */
	protected void appendIntegerType(StringBuilder sb) {
		sb.append("INTEGER");
	}

	/**
	 * Output the SQL type for a Java long.
	 */
	protected void appendLongType(StringBuilder sb) {
		sb.append("BIGINT");
	}

	/**
	 * Output the SQL type for a Java float.
	 */
	protected void appendFloatType(StringBuilder sb) {
		sb.append("FLOAT");
	}

	/**
	 * Output the SQL type for a Java double.
	 */
	protected void appendDoubleType(StringBuilder sb) {
		sb.append("DOUBLE PRECISION");
	}

	/**
	 * Output the SQL type for either a serialized Java object or a byte[].
	 */
	protected void appendByteArrayType(StringBuilder sb) {
		sb.append("BLOB");
	}

	/**
	 * Output the SQL type for a serialized Java object.
	 */
	protected void appendSerializableType(StringBuilder sb) {
		sb.append("BLOB");
	}

	/**
	 * Output the SQL type for a Enum object stored as String.
	 */
	protected void appendEnumStringType(StringBuilder sb, FieldType fieldType) {
		// delegate to a string
		appendStringType(sb, DEFAULT_VARCHAR_WIDTH);
	}

	/**
	 * Output the SQL type for a Enum object stored as intg.
	 */
	protected void appendEnumIntType(StringBuilder sb, FieldType fieldType) {
		// delegate to an integer
		appendIntegerType(sb);
	}

	/**
	 * Output the SQL type for a Java boolean default value.
	 */
	protected void appendDefaultValue(StringBuilder sb, FieldType fieldType, Object defaultValue) {
		if (fieldType.isEscapedDefaultValue()) {
			appendEscapedWord(sb, defaultValue.toString());
		} else {
			sb.append(defaultValue);
		}
	}

	/**
	 * Output the SQL necessary to configure a generated-id column. This may add to the before statements list or
	 * additional arguments later.
	 * 
	 * NOTE: Only one of configureGeneratedIdSequence, configureGeneratedId, or configureId will be called.
	 */
	protected void configureGeneratedIdSequence(StringBuilder sb, FieldType fieldType, List<String> statementsBefore,
			List<String> additionalArgs, List<String> queriesAfter) throws SQLException {
		throw new SQLException("GeneratedIdSequence is not supported by database " + getDatabaseName() + " for field "
				+ fieldType);
	}

	/**
	 * Output the SQL necessary to configure a generated-id column. This may add to the before statements list or
	 * additional arguments later.
	 * 
	 * NOTE: Only one of configureGeneratedIdSequence, configureGeneratedId, or configureId will be called.
	 */
	protected void configureGeneratedId(StringBuilder sb, FieldType fieldType, List<String> statementsBefore,
			List<String> additionalArgs, List<String> queriesAfter) {
		throw new IllegalStateException("GeneratedId is not supported by database " + getDatabaseName() + " for field "
				+ fieldType);
	}

	/**
	 * Output the SQL necessary to configure an id column. This may add to the before statements list or additional
	 * arguments later.
	 * 
	 * NOTE: Only one of configureGeneratedIdSequence, configureGeneratedId, or configureId will be called.
	 */
	protected void configureId(StringBuilder sb, FieldType fieldType, List<String> statementsBefore,
			List<String> additionalArgs, List<String> queriesAfter) {
		StringBuilder primaryKeySb = new StringBuilder();
		primaryKeySb.append("PRIMARY KEY (");
		appendEscapedEntityName(primaryKeySb, fieldType.getDbColumnName());
		primaryKeySb.append(") ");
		additionalArgs.add(primaryKeySb.toString());
	}

	public void dropColumnArg(FieldType fieldType, List<String> statementsBefore, List<String> statementsAfter) {
		// by default this is a noop
	}

	public void appendEscapedWord(StringBuilder sb, String word) {
		sb.append('\'').append(word).append('\'');
	}

	public void appendEscapedEntityName(StringBuilder sb, String word) {
		sb.append('`').append(word).append('`');
	}

	public String generateIdSequenceName(String tableName, FieldType idFieldType) {
		String name = tableName + DEFAULT_SEQUENCE_SUFFIX;
		if (isEntityNamesMustBeUpCase()) {
			return name.toUpperCase();
		} else {
			return name;
		}
	}

	public String getCommentLinePrefix() {
		return "-- ";
	}

	public FieldConverter getFieldConverter(DataType dataType) {
		// default is to use the dataType itself
		return dataType;
	}

	public boolean isIdSequenceNeeded() {
		return false;
	}

	public boolean isVarcharFieldWidthSupported() {
		return true;
	}

	public boolean isLimitSqlSupported() {
		return true;
	}

	public boolean isOffsetSqlSupported() {
		return true;
	}

	public boolean isOffsetLimitArgument() {
		return false;
	}

	public boolean isLimitAfterSelect() {
		return false;
	}

	public void appendLimitValue(StringBuilder sb, int limit, Integer offset) {
		sb.append("LIMIT ").append(limit).append(' ');
	}

	public void appendOffsetValue(StringBuilder sb, int offset) {
		sb.append("OFFSET ").append(offset).append(' ');
	}

	/**
	 * Return the default varchar width if not specified by {@link DatabaseField#width}.
	 */
	protected int getDefaultVarcharWidth() {
		return DEFAULT_VARCHAR_WIDTH;
	}

	public void appendSelectNextValFromSequence(StringBuilder sb, String sequenceName) {
		// noop by default.
	}

	public void appendCreateTableSuffix(StringBuilder sb) {
		// noop by default.
	}

	public boolean isCreateTableReturnsZero() {
		return true;
	}

	public boolean isEntityNamesMustBeUpCase() {
		return false;
	}

	public boolean isNestedSavePointsSupported() {
		return true;
	}

	public String getPingStatement() {
		return "SELECT 1";
	}

	public boolean isBatchUseTransaction() {
		return false;
	}

	public boolean isTruncateSupported() {
		return false;
	}

	public boolean isCreateIfNotExistsSupported() {
		return false;
	}

	/**
	 * If the field can be nullable, do we need to add some sort of NULL SQL for the create table. By default it is a
	 * noop. This is necessary because MySQL has a auto default value for the TIMESTAMP type that required a default
	 * value otherwise it would stick in the current date automagically.
	 */
	protected void appendCanBeNull(StringBuilder sb, FieldType fieldType) {
		// default is a noop
	}

	/**
	 * If the fieldType is unique then added per-field unique qualifier.
	 */
	protected void appendUnique(StringBuilder sb, FieldType fieldType, List<String> statementsAfter) {
		sb.append("UNIQUE ");
	}

	/**
	 * Conversion to/from the Boolean Java field as a number because some databases like the true/false.
	 */
	protected static class BooleanNumberFieldConverter implements FieldConverter {
		public SqlType getSqlType() {
			return SqlType.BOOLEAN;
		}
		public Object parseDefaultString(FieldType fieldType, String defaultStr) throws SQLException {
			boolean bool = (boolean) Boolean.parseBoolean(defaultStr);
			return (bool ? Byte.valueOf((byte) 1) : Byte.valueOf((byte) 0));
		}
		public Object javaToSqlArg(FieldType fieldType, Object obj) throws SQLException {
			Boolean bool = (Boolean) obj;
			return (bool ? Byte.valueOf((byte) 1) : Byte.valueOf((byte) 0));
		}
		public Object resultToJava(FieldType fieldType, DatabaseResults results, int columnPos) throws SQLException {
			byte result = results.getByte(columnPos);
			return (result == 1 ? (Boolean) true : (Boolean) false);
		}
		public boolean isStreamType() {
			return false;
		}
	}
}
