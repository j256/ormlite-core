package com.j256.ormlite.db;

import java.sql.SQLException;
import java.util.List;

import com.j256.ormlite.field.FieldConverter;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.support.Results;

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

	private final static FieldConverter byteConverter = new ByteFieldConverter();

	public String getDriverUrlPart() {
		return DATABASE_URL_PORTION;
	}

	public String getDriverClassName() {
		return DRIVER_CLASS_NAME;
	}

	@Override
	public FieldConverter getFieldConverter(FieldType fieldType) {
		// we are only overriding certain types
		switch (fieldType.getDataType()) {
			case BOOLEAN :
			case BOOLEAN_OBJ :
				return booleanConverter;
			case BYTE :
				return byteConverter;
			default :
				return super.getFieldConverter(fieldType);
		}
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
	protected void appendObjectType(StringBuilder sb) {
		sb.append("IMAGE");
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

	/**
	 * Conversion from the byte Java field to the SMALLINT Jdbc type because TINYINT looks to be 0-255 and unsigned.
	 */
	private static class ByteFieldConverter implements FieldConverter {
		public int getSqlTypeVal() {
			// store it as a short
			return DataType.SHORT.getSqlTypeVal();
		}
		public Object parseDefaultString(String defaultStr) {
			return Short.parseShort(defaultStr);
		}
		public Object javaToArg(Object javaObject) {
			// convert the Byte arg to be a short
			byte byteVal = (Byte) javaObject;
			return (short) byteVal;
		}
		public Object resultToJava(FieldType fieldType, Results results, int dbColumnPos) throws SQLException {
			// starts as a short and then gets converted to a byte on the way out
			short shortVal = results.getShort(dbColumnPos);
			// make sure the database value doesn't overflow the byte
			if (shortVal < Byte.MIN_VALUE) {
				return Byte.MIN_VALUE;
			} else if (shortVal > Byte.MAX_VALUE) {
				return Byte.MAX_VALUE;
			} else {
				return (byte) shortVal;
			}
		}
		public boolean isStreamType() {
			return false;
		}
	}
}
