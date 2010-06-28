package com.j256.ormlite.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.j256.ormlite.field.FieldConverter;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.JdbcType;

/**
 * Microsoft SQL server database type information connected through the JTDS JDBC driver.
 * 
 * <p>
 * <b>NOTE:</b> Currently with 1.2.4 version of the jTDS package, I'm seeing problems with Java 1.5 because jTDS is
 * using a java.sql 1.6 class.
 * </p>
 * 
 * <p>
 * See <a href="http://jtds.sourceforge.net/" >JTDS home page</a> for more information. To use this driver, you need to
 * specify the database URL as something like the following. See the URL for more information.
 * </p>
 * 
 * <p>
 * <blockquote> jdbc:jtds:sqlserver://host-name:host-port/database-name </blockquote>
 * </p>
 * 
 * @author graywatson
 */
public class SqlServerJtdsDatabaseType extends SqlServerDatabaseType implements DatabaseType {

	private final static String DATABASE_URL_PORTION = "jtds";
	private final static String DRIVER_CLASS_NAME = "net.sourceforge.jtds.jdbc.Driver";
	private final static FieldConverter byteConverter = new ByteFieldConverter();

	@Override
	public String getDriverUrlPart() {
		return DATABASE_URL_PORTION;
	}

	@Override
	public String getDriverClassName() {
		return DRIVER_CLASS_NAME;
	}

	@Override
	public FieldConverter getFieldConverter(FieldType fieldType) {
		if (fieldType.getJdbcType() == JdbcType.BYTE) {
			// we are only overriding the BYTE type
			return byteConverter;
		} else {
			return super.getFieldConverter(fieldType);
		}
	}

	@Override
	protected void appendObjectType(StringBuilder sb) {
		sb.append("IMAGE");
	}

	/**
	 * Conversion from the byte Java field to the SMALLINT Jdbc type because TINYINT looks to be 0-255 and unsigned.
	 */
	private static class ByteFieldConverter implements FieldConverter {
		public int getJdbcTypeVal() {
			// store it as a short
			return JdbcType.SHORT.getJdbcTypeVal();
		}
		public Object javaToArg(Object javaObject) {
			// convert the Byte arg to be a short
			byte byteVal = (Byte) javaObject;
			return (short) byteVal;
		}
		public Object resultToJava(FieldType fieldType, ResultSet resultSet, int dbColumnPos) throws SQLException {
			// starts as a short and then gets converted to a byte on the way out
			short shortVal = resultSet.getShort(dbColumnPos);
			// make sure the database value doesn't overflow the byte
			if (shortVal < Byte.MIN_VALUE) {
				return Byte.MIN_VALUE;
			} else if (shortVal > Byte.MAX_VALUE) {
				return Byte.MAX_VALUE;
			} else {
				return (byte) shortVal;
			}
		}
	}
}
